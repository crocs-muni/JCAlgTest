/*
 * To the extent possible under law, contributors have waived all
 * copyright and related or neighboring rights to work.
 */
package jnasmartcardio;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.Provider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardNotPresentException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactorySpi;

import jnasmartcardio.Winscard.Dword;
import jnasmartcardio.Winscard.DwordByReference;
import jnasmartcardio.Winscard.SCardReaderState;

import com.sun.jna.Platform;
import com.sun.jna.Structure;


public class Smartcardio extends Provider {
	
	private static final long serialVersionUID = 1L;
	
	static final int MAX_ATR_SIZE = 33;

	public static final String PROVIDER_NAME = "JNA2PCSC";
	
	public Smartcardio() {
		super(PROVIDER_NAME, 0.2d, "JNA-to-PCSC Provider");
		put("TerminalFactory.PC/SC", JnaTerminalFactorySpi.class.getName());
	}
	
	public static class JnaTerminalFactorySpi extends TerminalFactorySpi {
		public static final int SCARD_SCOPE_USER = 0;
		public static final int SCARD_SCOPE_TERMINAL = 1;
		public static final int SCARD_SCOPE_SYSTEM = 2;
		private final Winscard.WinscardLibInfo libInfo;

		public JnaTerminalFactorySpi(Object parameter) {
			this(Winscard.openLib());
		}
		
		public JnaTerminalFactorySpi(Winscard.WinscardLibInfo libInfo) {
			this.libInfo = libInfo;
		}
		/**
		 * Likely exceptions
		 * <ul>
		 * <li>EstablishContextException(JnaPCSCException(
		 * {@link WinscardConstants#SCARD_E_NO_READERS_AVAILABLE})) the Daemon
		 * is not running (Windows 8, Linux). On Windows 8, the daemon is shut
		 * down when there are no readers plugged in. New PCSC versions (at
		 * least 1.7) also allow no new connections when there are no readers
		 * plugged in.
		 * <li>EstablishContextException(JnaPCSCException(
		 * {@link WinscardConstants#SCARD_E_NO_SERVICE})) the Daemon is not
		 * running (OS X). On OS X (pcscd 1.4), the daemon is shut down when
		 * there are no readers plugged in, and the library gives this error.
		 * Can also happen on Windows when you don't have permission.
		 * </ul>
		 */
		@Override public CardTerminals engineTerminals() throws EstablishContextException {
			Winscard.SCardContextByReference phContext = new Winscard.SCardContextByReference();
			try {
				check("SCardEstablishContext", libInfo.lib.SCardEstablishContext(new Dword(SCARD_SCOPE_SYSTEM), null, null, phContext));
			} catch (JnaPCSCException e) {
				throw new EstablishContextException(e);
			}
			Winscard.SCardContext scardContext = phContext.getValue();
			return new JnaCardTerminals(libInfo, scardContext);
		}
	}

	public static class JnaCardTerminals extends CardTerminals {
		private final Winscard.SCardContext scardContext;
		private final Winscard.WinscardLibInfo libInfo;
		/**
		 * The readers that waitForChange observed in its last invocation, plus
		 * the PNP reader if {@link #usePnp}. This must have a contiguous native
		 * backing allocated using {@link Structure#toArray(Structure[])}.
		 */
		private SCardReaderState[] knownReaders;
		/**
		 * Readers that previously existed, which you can get using
		 * list(State.CARD_REMOVAL). Stored until the next
		 * {@link #waitForChange(long)} call.
		 */
		private final List<SCardReaderState> zombieReaders;
		/**
		 * Whether to use the PNP device to detect when new readers are plugged
		 * in. Unfortunately, this is now almost useless, because the smartcard
		 * service exits and gives errors when there are no readers.
		 */
		private final boolean usePnp = true;
		private boolean isClosed;
		public JnaCardTerminals(Winscard.WinscardLibInfo libInfo, Winscard.SCardContext scardContext) {
			this.libInfo = libInfo;
			this.scardContext = scardContext;
			this.knownReaders = createScardReaderStates(Collections.<String>emptyList(), usePnp, (SCardReaderState[])null);
			this.zombieReaders = new ArrayList<SCardReaderState>();
		}

		/**
		 * With {@link State#ALL}, {@link State#CARD_PRESENT}, or
		 * {@link State#CARD_ABSENT}, returns the current list of terminals
		 * filtered accordingly. With {@link State#CARD_INSERTION} or
		 * {@link State#CARD_REMOVAL}, returns the list of terminals that were
		 * added or removed at the end of the most recent
		 * {@link #waitForChange(long)}/{@link #waitForChange()} call (it is not
		 * safe to call waitForChange concurrently).
		 *
		 * <p>
		 * Deviation from Sun's version: the first invocation using
		 * State.CARD_REMOVAL or State.CARD_INSERTION will return an empty list
		 * if there was no prior {@link #waitForChange(long)} instead of
		 * returning a list that is possibly inconsistent with the internal
		 * waitForChange state.
		 */
		@Override public List<CardTerminal> list(State state) throws CardException {
			if (null == state)
				throw new NullPointerException("State must be non-null. To get all terminals, call list() or list(State.ALL).");
			if (state == State.CARD_REMOVAL || state == State.CARD_INSERTION) {
				List<CardTerminal> r = new ArrayList<CardTerminal>();
				for (int i = 0; i < knownReaders.length; i++) {
					SCardReaderState readerState = knownReaders[i];
					if (WinscardConstants.PNP_READER_ID.equals(readerState.szReader))
						continue;
					boolean wasPresent = 0 != (readerState.dwCurrentState.intValue() & WinscardConstants.SCARD_STATE_PRESENT);
					boolean isPresent = 0 != (readerState.dwEventState.intValue() & WinscardConstants.SCARD_STATE_PRESENT);
					int oldCounter = (readerState.dwCurrentState.intValue() >> 16) & 0xffff;
					int newCounter = (readerState.dwEventState.intValue() >> 16) & 0xffff;
					boolean cardInserted = ! wasPresent && isPresent ||
						isPresent && oldCounter < newCounter ||
						oldCounter + 1 < newCounter;
					boolean cardRemoved = wasPresent && !isPresent ||
						! isPresent && oldCounter < newCounter ||
						oldCounter + 1 < newCounter;
					boolean shouldAdd = state == State.CARD_INSERTION && cardInserted ||
							state == State.CARD_REMOVAL && cardRemoved;
					if (shouldAdd)
						r.add(new JnaCardTerminal(libInfo, this, readerState.szReader));
				}
				if (state == State.CARD_REMOVAL) {
					for (int i = 0; i < zombieReaders.size(); i++) {
						SCardReaderState readerState = zombieReaders.get(i);
						boolean wasPresent = 0 != (readerState.dwCurrentState.intValue() & WinscardConstants.SCARD_STATE_PRESENT);
						if (wasPresent)
							r.add(new JnaCardTerminal(libInfo, this, readerState.szReader));
					}
				}
				return r;
			}

			List<String> readerNames = listReaderNames();
			if (readerNames.isEmpty())
				return Collections.emptyList();
			List<String> filteredReaderNames;
			if (state == State.ALL) {
				filteredReaderNames = readerNames;
			} else {
				SCardReaderState[] readers = new SCardReaderState[readerNames.size()];
				new SCardReaderState().toArray((Structure[])readers);
				for (int i = 0; i < readers.length; i++) {
					readers[i].szReader = readerNames.get(i);
				}
				check("SCardGetStatusChange", libInfo.lib.SCardGetStatusChange(scardContext, new Dword(0), readers, new Dword(readers.length)));
				filteredReaderNames = new ArrayList<String>();
				boolean wantPresent = state == State.CARD_PRESENT;
				for (int i = 0; i < readers.length; i++) {
					boolean isPresent = 0 != (WinscardConstants.SCARD_STATE_PRESENT & readers[i].dwEventState.intValue());
					if (wantPresent == isPresent)
						filteredReaderNames.add(readers[i].szReader);
				}
			}
			CardTerminal[] cardTerminals = new CardTerminal[filteredReaderNames.size()];
			for (int i = 0; i < filteredReaderNames.size(); i++) {
				String name = filteredReaderNames.get(i);
				cardTerminals[i] = new JnaCardTerminal(libInfo, this, name);
			}
			return Collections.unmodifiableList(Arrays.asList(cardTerminals));
		}

		/** Simple wrapper around SCardListReaders. */
		private List<String> listReaderNames() throws JnaPCSCException {
			DwordByReference pcchReaders = new DwordByReference();
			byte[] mszReaders = null;
			long err;
			ByteBuffer mszReaderGroups = ByteBuffer.allocate("SCard$AllReaders".length() + 2);
			mszReaderGroups.put("SCard$AllReaders".getBytes(Charset.forName("ascii")));
			while (true) {
				err = libInfo.lib.SCardListReaders(scardContext, mszReaderGroups, null, pcchReaders).longValue();
				if (err != 0)
					break;
				mszReaders = new byte[pcchReaders.getValue().intValue()];
				err = libInfo.lib.SCardListReaders(scardContext, mszReaderGroups, ByteBuffer.wrap(mszReaders), pcchReaders).longValue();
				if ((int)err != WinscardConstants.SCARD_E_INSUFFICIENT_BUFFER)
					break;
			}
			switch ((int)err) {
			case SCARD_S_SUCCESS:
				List<String> readerNames = pcsc_multi2jstring(mszReaders);
				return readerNames;
			case SCARD_E_NO_READERS_AVAILABLE:
			case SCARD_E_READER_UNAVAILABLE:
				return Collections.emptyList();
			default:
				check("SCardListReaders", err);
				throw new IllegalStateException();
			}
		}

		/**
		 * @param oldKnownReaders
		 *            old list of known readers to copy state variables from.
		 *            May be null if readerNames is empty.
		 */
		private static SCardReaderState[] createScardReaderStates(List<String> readerNames, boolean usePnp, SCardReaderState[] oldKnownReaders) {
			SCardReaderState[] newKnownReaders = new SCardReaderState[readerNames.size() + (usePnp?1:0)];
			new SCardReaderState().toArray(newKnownReaders);
			int i = 0;
			if (usePnp) {
				newKnownReaders[i].szReader = WinscardConstants.PNP_READER_ID;
				i++;
			}
			for (String readerName: readerNames) {
				SCardReaderState newReader = newKnownReaders[i];
				newReader.szReader = readerName;
				SCardReaderState oldReader = null;
				for (int j = 0; j < oldKnownReaders.length; j++) {
					if (readerName.equals(oldKnownReaders[j].szReader)) {
						oldReader = oldKnownReaders[j];
						break;
					}
				}
				if (oldReader != null) {
					newReader.dwCurrentState = oldReader.dwCurrentState;
					newReader.dwEventState  = oldReader.dwEventState;
					newReader.cbAtr = oldReader.cbAtr;
					newReader.pvUserData = oldReader.pvUserData;
					System.arraycopy(oldReader.rgbAtr, 0, newReader.rgbAtr, 0, oldReader.cbAtr.intValue());
				}
				i++;
			}
			return newKnownReaders;
		}
		/**
		 * Helper function for {@link #waitForChange(long)}. Lists the readers
		 * and updates 3 variables:
		 * <ul>
		 * <li>Any new readers are appended to {@link #knownReaders}.
		 * <li>Any old readers are moved from {@link #knownReaders} to
		 * {@link #zombieReaders}.
		 * <li>If any change is made, {@link #knownReadersChanged} is set so
		 * that the JNA array-of-struct can be reallocated.
		 * </ul>
		 * 
		 * @return true if a reader was added or removed.
		 */
		private boolean updateKnownReaders() throws JnaPCSCException {
			List<String> currentReaderNames = listReaderNames();
			boolean isReaderAddedOrRemoved = false;
			int oldReaderCount = 0;
			for (SCardReaderState oldReader: knownReaders) {
				if (WinscardConstants.PNP_READER_ID.equals(oldReader.szReader)) {
					continue;
				} else {
					oldReaderCount++;
					if (!currentReaderNames.contains(oldReader.szReader)) {
						isReaderAddedOrRemoved = true;
						zombieReaders.add(oldReader);
					}
				}
			}
			isReaderAddedOrRemoved = isReaderAddedOrRemoved || oldReaderCount != currentReaderNames.size();
			if (!isReaderAddedOrRemoved) {
				return isReaderAddedOrRemoved;
			}
			this.knownReaders = createScardReaderStates(currentReaderNames, usePnp, knownReaders);
			
			SCardReaderState[] readers;
			if (knownReaders.length == 0) {
				// create array containing null, to avoid JNA exception:
				// Structure array must have non-zero length
				readers = new SCardReaderState[1];
			} else {
				readers = knownReaders;
			}
			check("SCardGetStatusChange", libInfo.lib.SCardGetStatusChange(scardContext, new Dword(0), readers, new Dword(knownReaders.length)));
			return true;
		}

		/**
		 * Block until any card is inserted or removed, or until the timeout.
		 *
		 * <p>
		 * Deviation from the Sun version: the first
		 * {@link #waitForChange(long)} call always returns immediately. In
		 * Sun's version, if the card is inserted between your {@link #list()}
		 * call and the first {@link #waitForChange(long)} call, then your
		 * application can wait forever.
		 *
		 * <p>
		 * Note: this method returns early when any smartcard state has changed
		 * (e.g. smartcard becomes in-use or idle). The caller cannot observe
		 * these changes though. So the caller should be able to handle changes
		 * that appear spurious.
		 *
		 * <p>
		 * Likely exceptions
		 * <ul>
		 * <li>JnaPCSCException(
		 * {@link WinscardConstants#SCARD_E_SERVICE_STOPPED}) On Windows 8+, the
		 * service shuts down immediately when the last reader is unplugged.
		 * Then, you have to start polling because there is no daemon to
		 * subscribe to.
		 * </ul>
		 */
		@Override public boolean waitForChange(long timeoutMs) throws CardException {
			if (timeoutMs < 0)
				throw new IllegalArgumentException("Negative timeout " + timeoutMs);
			else if (timeoutMs == 0)
				timeoutMs = WinscardConstants.INFINITE;

			zombieReaders.clear();
			// On Linux pcsclite 1.7.4, and Mac OSX 10.10, the PNP reader does
			// not return immediately when there is already a reader present
			// that isn't in the array. Thus there is a race condition between
			// updateKnownReaders() and SCardGetStatusChange; if a reader is
			// plugged in then, I think this function will block forever.
			if (!usePnp || Platform.isLinux() || Platform.isMac())
				if (updateKnownReaders())
					return true;  // # of readers changed; return early.

			for (SCardReaderState reader: knownReaders) {
				reader.dwCurrentState = reader.dwEventState;
				reader.dwEventState = new Dword(0);
			}
			SCardReaderState[] readers;
			if (knownReaders.length == 0) {
				// create array containing null, to avoid JNA exception:
				// Structure array must have non-zero length
				readers = new SCardReaderState[1];
			} else {
				readers = knownReaders;
			}
			Dword statusError = libInfo.lib.SCardGetStatusChange(scardContext, new Dword(timeoutMs), readers, new Dword(readers.length));
			if (WinscardConstants.SCARD_E_TIMEOUT == statusError.intValue())
				return false;
			else check("SCardGetStatusChange", statusError);

			if (usePnp) {
				SCardReaderState pnpReader = knownReaders[0];
				boolean pnpChange = 0 != (pnpReader.dwEventState.intValue() & WinscardConstants.SCARD_STATE_CHANGED);
				if (pnpChange)
					updateKnownReaders();
			}
			return true;
		}
		@Override public String toString() {return String.format("%s{scardContext=%s}", getClass().getSimpleName(), scardContext);}
		public void close() throws JnaPCSCException {
			synchronized (this) {
				if (isClosed) return;
				else isClosed = true;
			}
			check("SCardReleaseContext", libInfo.lib.SCardReleaseContext(scardContext));
		}
		@Override public void finalize() throws JnaPCSCException {
			close();
		}
	}

	public static class JnaCardTerminal extends CardTerminal {
		private final Winscard.WinscardLibInfo libInfo;
		private final JnaCardTerminals cardTerminals;
		private final String name;
		public static final int SCARD_SHARE_EXCLUSIVE = 1;
		public static final int SCARD_SHARE_SHARED = 2;
		public static final int SCARD_SHARE_DIRECT = 3;
		public static final int SCARD_PROTOCOL_T0 = 1;
		public static final int SCARD_PROTOCOL_T1 = 2;

		//win32 public static final int SCARD_PROTOCOL_RAW = 0x0010000;
		//pcsclite public static final int SCARD_PROTOCOL_RAW = 4;
		//pcsclite public static final int SCARD_PROTOCOL_T15 = 8;

		// aka SCARD_PROTOCOL_Tx in Windows
		public static final int SCARD_PROTOCOL_ANY = SCARD_PROTOCOL_T0 | SCARD_PROTOCOL_T1;

		public static final int SCARD_UNKNOWN = 0x01;
		public static final int SCARD_ABSENT = 0x02;
		public static final int SCARD_PRESENT = 0x04;
		public static final int SCARD_SWALLOWED = 0x08;
		public static final int SCARD_POWERED = 0x10;
		public static final int SCARD_NEGOTIABLE = 0x20;
		public static final int SCARD_SPECIFIC = 0x40;

		public JnaCardTerminal(Winscard.WinscardLibInfo libInfo, JnaCardTerminals cardTerminals, String name) {
			this.libInfo = libInfo;
			this.cardTerminals = cardTerminals;
			this.name = name;
		}
		@Override public String getName() {return name;}
		/**
		 * Supported protocols:
		 * <ul>
		 * <li><code>T=0</code>
		 * <li><code>T=1</code>
		 * <li><code>*</code>
		 * <li><code>DIRECT</code>
		 * </ul>
		 */
		@Override public Card connect(String protocol) throws CardException {
			int dwPreferredProtocols, dwShareMode;
			if ("T=0".equals(protocol)) {
				dwPreferredProtocols = SCARD_PROTOCOL_T0;
				dwShareMode = SCARD_SHARE_SHARED;
			} else if ("T=1".equals(protocol)) {
				dwPreferredProtocols = SCARD_PROTOCOL_T1;
				dwShareMode = SCARD_SHARE_SHARED;
			} else if ("*".equals(protocol)) {
				dwPreferredProtocols = SCARD_PROTOCOL_ANY;
				dwShareMode = SCARD_SHARE_SHARED;
			} else if ("DIRECT".equalsIgnoreCase(protocol)) {
				// Connect directly to reader to send control commands.
				dwPreferredProtocols = 0;
				dwShareMode = SCARD_SHARE_DIRECT;
			} else {
				throw new IllegalArgumentException("Protocol should be one of T=0, T=1, *, DIRECT. Got " + protocol);
			}
			Winscard.SCardHandleByReference phCard = new Winscard.SCardHandleByReference();
			DwordByReference pdwActiveProtocol = new DwordByReference();

			long err = libInfo.lib.SCardConnect(cardTerminals.scardContext, name, new Dword(dwShareMode), new Dword(dwPreferredProtocols), phCard, pdwActiveProtocol).longValue();
			switch ((int)err) {
			case SCARD_S_SUCCESS:
				Winscard.SCardHandle scardHandle = phCard.getValue();
				DwordByReference readerLength = new DwordByReference();
				DwordByReference currentState = new DwordByReference();
				DwordByReference currentProtocol = new DwordByReference();
				ByteBuffer atrBuf = ByteBuffer.allocate(Smartcardio.MAX_ATR_SIZE);
				DwordByReference atrLength = new DwordByReference(new Dword(Smartcardio.MAX_ATR_SIZE));
				check("SCardStatus", libInfo.lib.SCardStatus(scardHandle, null, readerLength, currentState, currentProtocol, atrBuf, atrLength));
				int atrLengthInt = atrLength.getValue().intValue();
				atrBuf.limit(atrLengthInt);
				byte[] atrBytes = new byte[atrBuf.remaining()];
				atrBuf.get(atrBytes);
				ATR atr = new ATR(atrBytes);
				int currentProtocolInt = currentProtocol.getValue().intValue();
				return new JnaCard(libInfo, this, scardHandle, atr, currentProtocolInt);
			case WinscardConstants.SCARD_W_REMOVED_CARD:
				throw new JnaCardNotPresentException(err, "Card not present.");
			default:
				check("SCardConnect", err);
				throw new RuntimeException("Should not reach here.");
			}
		}
		@Override public boolean isCardPresent() throws CardException {
			SCardReaderState[] rgReaderStates = new SCardReaderState[1];
			new SCardReaderState().toArray((Structure[])rgReaderStates);
			rgReaderStates[0].szReader = name;
			SCardReaderState readerState = rgReaderStates[0];
			check("SCardGetStatusChange", libInfo.lib.SCardGetStatusChange(cardTerminals.scardContext, new Dword(0), rgReaderStates, new Dword(rgReaderStates.length)));
			return 0 != (readerState.dwEventState.intValue() & WinscardConstants.SCARD_STATE_PRESENT);
		}
		private boolean waitHelper(long timeoutMs, boolean cardPresent) throws JnaPCSCException {
			if (timeoutMs < 0)
				throw new IllegalArgumentException("Negative timeout " + timeoutMs);
			if (timeoutMs == 0)
				timeoutMs = WinscardConstants.INFINITE;
			SCardReaderState[] rgReaderStates = new SCardReaderState[1];
			new SCardReaderState().toArray((Structure[])rgReaderStates);
			SCardReaderState readerState = rgReaderStates[0];
			readerState.szReader = name;
			check("SCardGetStatusChange", libInfo.lib.SCardGetStatusChange(cardTerminals.scardContext, new Dword(0), rgReaderStates, new Dword(rgReaderStates.length)));
			int remainingTimeout = (int)timeoutMs;
			while (cardPresent != (0 != (readerState.dwEventState.intValue() & WinscardConstants.SCARD_STATE_PRESENT))) {
				readerState.dwCurrentState = readerState.dwEventState;
				readerState.dwEventState = new Dword(0);
				long startTime = System.currentTimeMillis();
				Dword err = libInfo.lib.SCardGetStatusChange(cardTerminals.scardContext, new Dword(remainingTimeout), rgReaderStates, new Dword(rgReaderStates.length));
				long endTime = System.currentTimeMillis();
				if (WinscardConstants.SCARD_E_TIMEOUT == err.intValue())
					return false;
				check("SCardGetStatusChange", err);
				if (remainingTimeout != WinscardConstants.INFINITE) {
					if (remainingTimeout < endTime - startTime)
						return false;
					remainingTimeout -= endTime - startTime;
				}
			}
			return true;
		}
		@Override public boolean waitForCardAbsent(long timeoutMs) throws CardException {
			return waitHelper(timeoutMs, false);
		}
		@Override public boolean waitForCardPresent(long timeoutMs) throws CardException {
			return waitHelper(timeoutMs, true);
		}
		@Override public String toString() {return String.format("%s{scardHandle=%s, name=%s}", getClass().getSimpleName(), cardTerminals.scardContext, name);}
	}

	public static class JnaCard extends Card {
		private final Winscard.WinscardLibInfo libInfo;
		@SuppressWarnings("unused")  // prevent context from being finalized.
		private final CardTerminal cardTerminal;
		private final Winscard.SCardHandle scardHandle;
		private final ATR atr;
		/**
		 * One of {@link JnaCardTerminal#SCARD_PROTOCOL_RAW},
		 * {@link JnaCardTerminal#SCARD_PROTOCOL_T0},
		 * {@link JnaCardTerminal#SCARD_PROTOCOL_T1}
		 */
		private final int protocol;
		public JnaCard(Winscard.WinscardLibInfo libInfo, JnaCardTerminal cardTerminal, Winscard.SCardHandle scardHandle, ATR atr, int protocol) {
			this.libInfo = libInfo;
			this.cardTerminal = cardTerminal;
			this.scardHandle = scardHandle;
			this.atr = atr;
			this.protocol = protocol;
			getProtocol();  // make sure it is valid.
		}

		@Override public void beginExclusive() throws CardException {
			check("SCardBeginTransaction", libInfo.lib.SCardBeginTransaction(scardHandle));
		}
		public static final int SCARD_LEAVE_CARD = 0;
		public static final int SCARD_RESET_CARD = 1;
		public static final int SCARD_UNPOWER_CARD = 2;
		public static final int SCARD_EJECT_CARD = 3;
		@Override public void endExclusive() throws CardException {
			check("SCardEndTransaction", libInfo.lib.SCardEndTransaction(scardHandle, new Dword(SCARD_LEAVE_CARD)));
			// TODO: handle error SCARD_W_RESET_CARD esp. in Windows
		}

		@Override public void disconnect(boolean reset) throws CardException {
			int dwDisposition = reset ? SCARD_RESET_CARD : SCARD_LEAVE_CARD;
			check("SCardDisconnect", libInfo.lib.SCardDisconnect(scardHandle, new Dword(dwDisposition)));
		}

		@Override public ATR getATR() {return atr;}
		@Override public String getProtocol() {
			switch (protocol) {
			case JnaCardTerminal.SCARD_PROTOCOL_T0: return "T=0";
			case JnaCardTerminal.SCARD_PROTOCOL_T1: return "T=1";
			default: return "DIRECT";  // TODO: is this right?
			}
		}

		@Override public JnaCardChannel getBasicChannel() {
			return new JnaCardChannel(this, (byte)0);
		}

		/**
		 * Open a logical channel.
		 *
		 * <p>
		 * Common exceptions:
		 * <ul>
		 * <li>JnaCardException(6200): processing warning
		 * <li>JnaCardException(6881): logical channel not supported
		 * <li>JnaCardException(6a81): function not supported
		 * </ul>
		 */
		@Override public CardChannel openLogicalChannel() throws CardException {
			// manage channel: request a new logical channel from 0x01 to 0x13
			JnaCardChannel basicChannel = getBasicChannel();
			ResponseAPDU response = basicChannel.transmit(new CommandAPDU(0, 0x70, 0x00, 0x00, 1));
			int sw = response.getSW();
			if (0x9000 == sw) {
				byte[] body = response.getData();
				if (body.length == 1) {
					int channel = 0xff & body[0];
					if (channel == 0 || channel > 0x13)
						throw new JnaCardException(sw, String.format("Expected manage channel response to contain channel number in 1-19; got %d", channel));
					return new JnaCardChannel(this, channel);
				} else {
					throw new JnaCardException(sw, String.format("Expected body of length 1 in response to manage channel request; got %d", body.length));
				}
			} else {
				throw new JnaCardException(sw, String.format("Error: sw=%04x in response to manage channel command.", sw));
			}
		}
		
		/**
		 * @param controlCode
		 *            one of the IOCTL_SMARTCARD_* constants from WinSmCrd.h
		 */
		@Override
		public byte[] transmitControlCommand(int controlCode, byte[] arg1) throws CardException {
			// there's no way from the API to know how big a receive buffer to use.
			// Sun uses 8192 bytes, so we'll do the same.
			ByteBuffer receiveBuf = ByteBuffer.allocate(8192);
			DwordByReference lpBytesReturned = new DwordByReference();
			ByteBuffer arg1Wrapped = ByteBuffer.wrap(arg1);
			check("SCardControl", libInfo.lib.SCardControl(scardHandle, new Dword(controlCode), arg1Wrapped, new Dword(arg1.length), receiveBuf, new Dword(receiveBuf.remaining()), lpBytesReturned));
			int bytesReturned = lpBytesReturned.getValue().intValue();
			receiveBuf.limit(bytesReturned);
			byte[] r = new byte[bytesReturned];
			receiveBuf.get(r);
			return r;
		}
		@Override public String toString() {return String.format("%s{scardHandle=%s}", getClass().getSimpleName(), scardHandle);}
	}

	public static class JnaCardChannel extends CardChannel {
		private final JnaCard card;
		private final int channel;
		private boolean isClosed;
	
		public JnaCardChannel(JnaCard card, int channel) {
			this.card = card;
			this.channel = channel;
		}
		@Override public void close() throws CardException {
			if (isClosed)
				return;
			isClosed = true;
			if (channel != 0) {
				// manage channel: close
				ByteBuffer command = ByteBuffer.wrap(new CommandAPDU(0, 0x70, 0x80, channel).getBytes());
				ByteBuffer response = ByteBuffer.allocate(2);
				transmitRaw(command, response);
				response.rewind();
				int sw = 0xffff & response.getShort();
				if (sw != 0x9000) {
					throw new JnaCardException(sw, "Could not close channel.");
				}
			} else {
				// Mimick SUN with self-protection
				throw new IllegalStateException("Basic channel can not be closed");
			}
		}
		@Override public Card getCard() {return card;}
		@Override public int getChannelNumber() {return channel;}

		/**
		 * Transmit the command and return the result APDU.
		 *
		 * <p>
		 * Note: currently, the response (including status bytes) is limited to
		 * 8192 bytes.
		 *
		 * <p>
		 * The command sent to the card is the same as the given command, except
		 * that:
		 * <ul>
		 * <li>The class byte (CLA) is modified to contain the channel number.
		 * The secure messaging indication and command chaining control are not
		 * modified, but they must already be in the correct bits depending on
		 * the channel number!
		 * <li>If T=0 and there is request data, then the Le byte is removed.
		 * </ul>
		 *
		 * <p>
		 * Automatically handles sw=61xx (get response) and sw=6cxx (Le)
		 * responses by re-sending the appropriate request.
		 */
		@Override public ResponseAPDU transmit(CommandAPDU command) throws CardException {
			if (command == null) {
				throw new IllegalArgumentException("command is null");
			}
			byte[] commandCopy = command.getBytes();
			ByteBuffer response = transmitImpl(commandCopy, null);

			ResponseAPDU responseApdu = convertResponse(response);
			return responseApdu;
		}

		/**
		 * Transmit the given command APDU and store the response APDU. Returns
		 * the length of the response APDU.
		 *
		 * <p>
		 * The response (including status bytes) must fit within the given
		 * response buffer.
		 *
		 * <p>
		 * The command sent to the card is the same as the given command, except
		 * that:
		 * <ul>
		 * <li>The class byte (CLA) is modified to contain the channel number.
		 * The secure messaging indication and command chaining control are not
		 * modified, but they must already be in the correct bits depending on
		 * the channel number!
		 * <li>If T=0 and there is request data, then the Le byte is removed.
		 * </ul>
		 *
		 * <p>
		 * Automatically handles sw=61xx (get response) and sw=6cxx (Le)
		 * responses by re-sending the appropriate request.
		 */
		@Override public int transmit(ByteBuffer command, ByteBuffer response) throws CardException {
			if (command == null) {
				throw new IllegalArgumentException("command is null");
			}
			if (response == null) {
				throw new IllegalArgumentException("response is null");
			}
			byte[] commandCopy = new byte[command.remaining()];
			command.get(commandCopy);
			int startPosition = response.position();
			transmitImpl(commandCopy, response);
			int endPosition = response.position();
			return endPosition - startPosition;
		}

		private boolean isExtendedApdu(byte[] commandApdu) {
			return commandApdu.length >= 7 && commandApdu[4] == 0;
		}

		/**
		 * Set the CLA byte, transmit the command, send Get Response commands as
		 * needed, and return the response ByteBuffer.
		 *
		 * <p>
		 * The command is modified as is convenient, since it is assumed to
		 * already be a copy.
		 *
		 * <p>
		 * Reminder: there are several forms of APDU:<br>
		 * 1. CLA INS P1 P2. No body, no response body.<br>
		 * 2s. CLA INS P1 P2 Le. No body. Le in [1,00 (256)]<br>
		 * 2e. CLA INS P1 P2 00 Le1 Le2. No body. Le in [1,0000 (65536)]<br>
		 * T=0: use Le = 00<br>
		 * 3s. CLA INS P1 P2 Lc &lt;body&gt;. No response. Lc in [1,255]<br>
		 * 3e. CLA INS P1 P2 00 Lc1 Lc2 &lt;body&gt;. No response. Lc in [1,ffff]<br>
		 * T=0: if Nc &lt;= 255, then use short form. Else, use envelope.<br>
		 * 4s. CLA INS P1 P2 Lc &lt;body&gt; Le. No response. Lc in [1,00]. Le in [1,ff]<br>
		 * 4e. CLA INS P1 P2 00 Lc1 Lc2 &lt;body&gt; 00 Le1 Le2. Lc in [1,0000]. Le in [1,ffff]
		 * 
		 * <p>
		 * This method handles:
		 * <ul>
		 * <li>Set the channel number bits in the class byte.
		 * <li>If T=0, then convert APDU to T=0 TPDU (ISO 7816-3). In
		 * particular, if T=0 and there is request data, then strip the Le field
		 * <li>If sw = 61xx, then call c0 get response and concatenate
		 * <li>If sw = 6cxx, then retransmit with Le = xx
		 * </ul>
		 * Q: Should it also handle
		 * <ul>
		 * <li>Command chaining (if mentioned in historic bytes) (bit 5 of cla =
		 * true)
		 * <li>Envelope (ins = c2 or c3)
		 * </ul>
		 * 
		 * <p>
		 * T=0 protocol: 3 cases
		 * <ul>
		 * <li>CLA INS P1 P2. Response will always be 2 bytes.
		 * <li>CLA INS P1 P2 Le. Response will be up to Le+2 bytes. (Le=0 means
		 * 256 bytes). Use get response commands to get all the data.
		 * <li>CLA INS P1 P2 Lc. &lt;outgoing data&gt. Response will always be 2
		 * bytes. Long commands need to be enclosed in envelope commands.
		 * </ul>
		 * 
		 * <p>
		 * T=1 protocol: CLA INS P1 P2 [Lc Data] [Le].<br>
		 * Lc is either 01-ff or 000001-00ffff.<br>
		 * Le is either 00-ff (00=256) or 0000-ffff. (0000=65536)
		 */
		private ByteBuffer transmitImpl(byte[] command, ByteBuffer response) throws CardException, JnaPCSCException {
			// Mimic SUN with self-defense 
			if (card.protocol == JnaCardTerminal.SCARD_PROTOCOL_T0 && isExtendedApdu(command))
				throw new CardException("Extended APDU requires T=1");
			
			command[0] = getClassByte(command[0], getChannelNumber());
			ByteBuffer commandBuffer = ByteBuffer.wrap(command);
	
			// Allocate memory if not given: 8K
			if (response == null)
				response = ByteBuffer.allocate(8192);

			// TODO: implement compatibility with SUN properties
			// Don't loop forever.
			for (int i=0; i<8; i++) {
				int posBeforeTransmit = response.position();
				transmitRaw(commandBuffer, response);

				// Roll back to read SW
				response.position(response.position() - 2);
				byte sw1 = response.get();
				byte sw2 = response.get();
				if (0x6c == sw1) {
					command[command.length - 1] = sw2;
					response.position(posBeforeTransmit);
					commandBuffer.rewind();
				} else if (0x61 == sw1) {
					// send Get Response command.
					// Don't touch CLA as per 7816-4
					command[1] = (byte) 0xc0;
					command[2] = (byte) 0x00;
					command[3] = (byte) 0x00;
					command[4] = sw2;
					commandBuffer.position(0);
					commandBuffer.limit(5);
					// concatenate new response to the same buffer.
					// Roll back to overwrite current SW.
					response.position(response.position() - 2);
				} else {
					break;
				}
			}
			return response;
		}

		/**
		 * Set the channel number on the class byte. Does not touch the command
		 * chaining control or secure messaging indication, but they must be in
		 * the correct bits for this channel number.
		 */
		static byte getClassByte(byte origCla, int channelNumber) {
			if ((0x80 & origCla) != 0) {
				// Not an interindustry class; don't touch it.
				return origCla;
			}
			int cla;
			// 7816-4/2005 5.1.1 Class byte
			if (0 <= channelNumber && channelNumber <= 3) {
				// First interindustry values of CLA
				// Class byte is 000x xxcc, where cc is channel number
				cla = (origCla & 0x1c) | channelNumber;
			} else if (0x04 <= channelNumber && channelNumber <= 0x13) {
				// Further interindustry values of CLA
				// Class byte is 01xx cccc, where cccc is channel number - 4.
				int channelBits = channelNumber - 4;
				cla = (origCla & 0x30) | channelBits | 0x40;
			} else {
				throw new IllegalStateException("Bad channel number; expected 0-19; got " + channelNumber);
			}
			return (byte) cla;
		}
		private static ResponseAPDU convertResponse(ByteBuffer responseBuf) {
			byte[] responseBytes = new byte[responseBuf.position()];
			responseBuf.rewind();
			responseBuf.get(responseBytes);
			return new ResponseAPDU(responseBytes);
		}

		/**
		 * Transmit the given apdu. On success, the command buffer is advanced
		 * to its limit, and the response buffer is advanced by the number of
		 * bytes received from the card.
		 */
		private int transmitRaw(ByteBuffer command, ByteBuffer response) throws JnaPCSCException {
			Winscard.ScardIoRequest pioSendPci = new Winscard.ScardIoRequest();
			pioSendPci.dwProtocol = new Dword(card.protocol);
			pioSendPci.cbPciLength = new Dword(pioSendPci.size());

			DwordByReference recvLength = new DwordByReference(new Dword(response.remaining()));
			check("SCardTransmit", card.libInfo.lib.SCardTransmit(card.scardHandle, pioSendPci, command, new Dword(command.remaining()), null, response, recvLength));
			int recvLengthInt = recvLength.getValue().intValue();
			assert recvLengthInt >= 0;

			command.position(command.limit());
			int newPosition = response.position() + recvLengthInt;
			response.position(newPosition);
			return recvLengthInt;
		}
		@Override public String toString() {return String.format("%s{card=%s, channel=%d}", getClass().getSimpleName(), this.card, this.channel);}
	}

	public static class JnaPCSCException extends CardException {
		private static final long serialVersionUID = 1L;
		public final long code;
		public JnaPCSCException(String message) {this(0, message, null);}
		public JnaPCSCException(Throwable cause) {this(0, null, cause);}
		public JnaPCSCException(long code, String message) {this(code, message, null);}
		public JnaPCSCException(long code, String message, Throwable cause) {super(message, cause); this.code = code;}
	}

	/**
	 * Wrapper for {@link JnaPCSCException} because TerminalFactory.terminals()
	 * is not allowed to throw checked exceptions.
	 */
	public static class EstablishContextException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		public EstablishContextException(JnaPCSCException cause) {super(cause);}
		/** Overridden with more specific return type so you don't have to cast,*/
		@Override public JnaPCSCException getCause() {return (JnaPCSCException) super.getCause();}
	}

	public static class JnaCardNotPresentException extends CardNotPresentException {
		private static final long serialVersionUID = 1L;
		public final long code;
		public JnaCardNotPresentException(long code, String message) {super(message); this.code = code;}
	}

	public static class JnaCardException extends CardException {
		private static final long serialVersionUID = 1L;
		public final int sw;
		public JnaCardException(int sw, String message) {this(sw, message, null);}
		public JnaCardException(int sw, String message, Throwable cause) {super(message, cause); this.sw = sw;}
	}

	public static final int SCARD_S_SUCCESS = 0x0;
	public static final int SCARD_E_NO_READERS_AVAILABLE = 0x8010002E;
	public static final int SCARD_E_READER_UNAVAILABLE = 0x80100017;
	public static final int SCARD_E_NO_SMARTCARD = 0x8010000C;

	/**
	 * Named affectionately after the function I've seen in crash logs so often
	 * from libj2pcsc on OS X java7.
	 * @param  
	 */
	private static List<String> pcsc_multi2jstring(byte[] multiString, Charset charset) {
		List<String> r = new ArrayList<String>();
		int from = 0, to = 0;
		for (; to < multiString.length; to++) {
			if (multiString[to] != '\0')
				continue;
			if (from == to)
				return r;
			byte[] bytes = Arrays.copyOfRange(multiString, from, to);
			r.add(new String(bytes, charset));
			from = to + 1;
		}
		throw new IllegalArgumentException("Multistring must be end with a null-terminated empty string.");
	}

	private static List<String> pcsc_multi2jstring(byte[] multiString) {
		return pcsc_multi2jstring(multiString, Charset.forName("UTF-8"));
	}

	private static void check(String message, Dword code) throws JnaPCSCException {
		check(message, code.longValue());
	}

	private static void check(String message, long code) throws JnaPCSCException {
		if (code == 0)
			return;
		int icode = (int)code;
		String codeName = WinscardConstants.ERROR_TO_VARIABLE_NAME.get(icode);
		String codeDescription = WinscardConstants.ERROR_TO_DESCRIPTION.get(icode);
		throw new JnaPCSCException(code, String.format("%s got response 0x%x (%s: %s)", message, icode, codeName, codeDescription));
	}
}
