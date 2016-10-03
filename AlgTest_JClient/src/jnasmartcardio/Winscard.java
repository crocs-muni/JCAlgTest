/*
 * To the extent possible under law, contributors have waived all
 * copyright and related or neighboring rights to work.
 */
package jnasmartcardio;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sun.jna.FunctionMapper;
import com.sun.jna.IntegerType;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.NativeLong;
import com.sun.jna.NativeMappedConverter;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.ByReference;

/**
 * Wrapper for the PC/SC (aka WinSCard) API. Abstracts over differences in the
 * ABIs among the implementations on Windows, OS X, and Linux.
 */
class Winscard {
	/**
	 * The DWORD type used by WinSCard.h, used wherever an integer is needed in
	 * SCard functions. On Windows and OS X, this is always typedef'd to a
	 * uint32_t. In the pcsclite library on Linux, it is a long
	 * instead, which is 64 bits on 64-bit Linux.
	 */
	public static class Dword extends IntegerType {
		public static final int SIZE = Platform.isWindows() || Platform.isMac() ? 4 : NativeLong.SIZE;
		private static final long serialVersionUID = 1L;
		public Dword() {
			this(0l);
		}
		public Dword(long value) {
			super(SIZE, value);
		}
		@Override public String toString() {return String.format("%d", longValue());}
	}

	/** Pointer to a DWORD (LPDWORD) type used by WinSCard.h. */
	public static class DwordByReference extends ByReference {
		public static final int SIZE = Platform.isWindows() || Platform.isMac() ? 4 : NativeLong.SIZE;
		public DwordByReference() {
			this(new Dword());
		}
		public DwordByReference(Dword value) {
			super(Dword.SIZE);
			setValue(value);
		}
		public void setValue(Dword value) {
			if (Dword.SIZE == 4)
				getPointer().setInt(0, value.intValue());
			else
				getPointer().setLong(0, value.longValue());
		}
		public Dword getValue() {
			long v;
			if (Dword.SIZE == 4)
				v = 0xffffffffl & getPointer().getInt(0);
			else
				v = getPointer().getLong(0);
			return new Dword(v);
		}
	}

	/**
	 * Base class for handles used in PC/SC. On Windows, it is a handle
	 * (ULONG_PTR which cannot be dereferenced). On PCSC, it is an integer
	 * (int32_t on OS X, long on Linux).
	 */
	public static class Handle extends IntegerType {
		private static final long serialVersionUID = 1L;
		public static final int SIZE = Platform.isWindows() ? Pointer.SIZE : Dword.SIZE;
		public Handle(long value) {
			super(SIZE, value);
		}
		@Override public String toString() {
			return String.format("%s{%x}", getClass().getSimpleName(), longValue());
		}
	}
	/** Pointer to a handle. */
	public static class HandleByReference extends ByReference {
		public HandleByReference() {super(Handle.SIZE);}
		protected long getLong() {
			long v = Handle.SIZE == 4 ? getPointer().getInt(0) : getPointer().getLong(0);
			return v;
		}
		protected void setLong(long value) {
			if (Handle.SIZE == 4) {
				getPointer().setInt(0, (int)value);
			} else {
				getPointer().setLong(0, value);
			}
		}
	}

	/**
	 * The SCARDCONTEXT type defined in WinSCard.h, used for most SCard
	 * functions.
	 */
	public static class SCardContext extends Handle {
		private static final long serialVersionUID = 1L;
		/** no-arg constructor needed for {@link NativeMappedConverter#defaultValue()}*/
		public SCardContext() {this(0l);}
		public SCardContext(long value) {super(value);}
	}
	/** PSCARDCONTEXT used for SCardEstablishContext. */
	public static class SCardContextByReference extends HandleByReference {
		public SCardContextByReference() {super();}
		public SCardContext getValue() { return new SCardContext(getLong()); }
		public void setValue(SCardContext context) { setLong(context.longValue()); }
	}
	
	/**
	 * The SCARDHANDLE type defined in WinSCard.h. It represents a connection to
	 * a card.
	 */
	public static class SCardHandle extends Handle {
		private static final long serialVersionUID = 1L;
		/** no-arg constructor needed for {@link NativeMappedConverter#defaultValue()}*/
		public SCardHandle() {this(0l);}
		public SCardHandle(long value) {super(value);}
	}
	/** PSCARDHANDLE used for SCardConnect. */
	public static class SCardHandleByReference extends HandleByReference {
		public SCardHandleByReference() {super();}
		public SCardHandle getValue() { return new SCardHandle(getLong()); }
		public void setValue(SCardHandle context) { setLong(context.longValue()); }
	}

	public static class ScardIoRequest extends Structure {
		public Dword dwProtocol;
		public Dword cbPciLength;
		public ScardIoRequest() {super();}
		public ScardIoRequest(Pointer p) {super(p);}
		@Override protected List<String> getFieldOrder() {
			return Arrays.asList("dwProtocol", "cbPciLength");
		}
		@Override public String toString() {return String.format("%s{dwProtocol: %s, cbPciLength: %s}", getClass().getSimpleName(), dwProtocol, cbPciLength);}
	}

	/**
	 * The SCARD_READERSTATE struct used by SCardGetStatusChange. On each
	 * platform, the sizeof and alignment is different.
	 * 
	 * On Windows, SCardReaderState is explicitly aligned to word boundaries.
	 * <ul>
	 * <li>Windows has extra padding after rgbAtr, so that the structure is
	 * aligned at word boundaries even when it is in an array
	 * SCARD_READERSTATE[]<br>
	 * sizeof(SCARD_READERSTATE_A):<br>
	 * windows x86: 4+4+4+4+4+36 = 56<br>
	 * windows x64: 8+8+4+4+4+36 = 64<br>
	 * structure alignment: not sure (but it doesn't matter)
	 * <li>OSX has no extra padding around rgbAtr, and pcsclite.h contains
	 * "#pragma pack(1)", so it is not word-aligned.<br>
	 * sizeof(SCARD_READERSTATE_A):<br>
	 * osx x86: 4+4+4+4+4+33 = 53<br>
	 * osx x64: 8+8+4+4+4+33 = 61<br>
	 * structure alignment: packed
	 * <li>Linux pcsclite has no extra padding around rgbAtr, but it is aligned
	 * by default. In addition, DWORD is typedef'd to long instead of int.<br>
	 * sizeof(SCARD_READERSTATE_A):<br>
	 * linux x86: 4+4+4+4+4+33 = 53<br>
	 * linux x64: 8+8+8+8+8+33 = 73<br>
	 * structure alignment: default
	 * </ul>
	 * 
	 * @see http://gcc.gnu.org/onlinedocs/gcc/Structure_002dPacking-Pragmas.html
	 */
	public static class SCardReaderState extends Structure {
		private static final int ALIGN = Platform.isMac() ? ALIGN_NONE : ALIGN_DEFAULT;
		public String szReader;
		public Pointer pvUserData;
		public Dword dwCurrentState;
		public Dword dwEventState;
		public Dword cbAtr;
		public byte[] rgbAtr = new byte[WinscardConstants.MAX_ATR_SIZE];
		public SCardReaderState(){
			super(null, ALIGN);
			dwCurrentState = dwEventState = cbAtr = new Dword(0);
		}
		public SCardReaderState(String szReader) {this(); this.szReader = szReader;}
		@Override protected List<String> getFieldOrder() {
			return Arrays.asList("szReader", "pvUserData", "dwCurrentState", "dwEventState", "cbAtr", "rgbAtr");
		}
	}

	public static final String WINDOWS_PATH = "WinSCard.dll";
	public static final String MAC_PATH = "/System/Library/Frameworks/PCSC.framework/PCSC";
	public static final String PCSC_PATH = "libpcsclite.so.1";

	/**
	 * The winscard API, also known as PC/SC. Implementations of this API exist
	 * on Windows, OS X, and Linux, although the symbol names and sizeof
	 * parameters differs on different platforms.
	 */
	public interface WinscardLibrary extends Library {
		Dword SCardEstablishContext (Dword dwScope, Pointer pvReserved1, Pointer pvReserved2, SCardContextByReference phContext);
		Dword SCardReleaseContext(SCardContext hContext);
		Dword SCardConnect(SCardContext hContext, String szReader, Dword dwSharMode, Dword dwPreferredProtocols, SCardHandleByReference phCard, DwordByReference pdwActiveProtocol);
		Dword SCardReconnect(SCardHandle hCard, Dword dwShareMode, Dword dwPreferredProtocols, Dword dwInitialization, DwordByReference pdwActiveProtocol);
		Dword SCardDisconnect (SCardHandle hCard, Dword dwDisposition);
		Dword SCardBeginTransaction(SCardHandle hCard);
		Dword SCardEndTransaction(SCardHandle hCard, Dword dwDisposition);
		Dword SCardStatus(SCardHandle hCard, ByteBuffer mszReaderName, DwordByReference pcchReaderLen, DwordByReference pdwState, DwordByReference pdwProtocol, ByteBuffer pbAtr, DwordByReference pcbAtrLen);
		Dword SCardGetStatusChange(SCardContext hContext, Dword dwTimeout, SCardReaderState[] rgReaderStates, Dword cReaders);
		Dword SCardControl(SCardHandle hCard, Dword dwControlCode, ByteBuffer pbSendBuffer, Dword cbSendLength, ByteBuffer pbRecvBuffer, Dword cbRecvLength, DwordByReference lpBytesReturned);
		Dword SCardGetAttrib(SCardHandle hCard, Dword dwAttrId, ByteBuffer pbAttr, DwordByReference pcbAttrLen);
		Dword SCardSetAttrib(SCardHandle hCard, Dword dwAttrId, ByteBuffer pbAttr, Dword cbAttrLen);
		Dword SCardTransmit(SCardHandle hCard, ScardIoRequest pioSendPci, ByteBuffer pbSendBuffer, Dword cbSendLength, ScardIoRequest pioRecvPci, ByteBuffer pbRecvBuffer, DwordByReference pcbRecvLength);
		Dword SCardListReaders(SCardContext hContext, ByteBuffer mszGroups, ByteBuffer mszReaders, DwordByReference pcchReaders);
		Dword SCardFreeMemory(SCardContext hContext, Pointer pvMem);
		Dword SCardListReaderGroups(SCardContext hContext, ByteBuffer mszGroups, DwordByReference pcchGroups);
		Dword SCardCancel(SCardContext hContext);
		Dword SCardIsValidContext (SCardContext hContext);
	}
	public static class WinscardLibInfo {
		public final WinscardLibrary lib;
		public final ScardIoRequest SCARD_PCI_T0;
		public final ScardIoRequest SCARD_PCI_T1;
		public final ScardIoRequest SCARD_PCI_RAW;
		public WinscardLibInfo(WinscardLibrary lib, ScardIoRequest SCARD_PCI_T0, ScardIoRequest SCARD_PCI_T1, ScardIoRequest SCARD_PCI_RAW) {
			this.lib = lib;
			this.SCARD_PCI_T0 = SCARD_PCI_T0;
			this.SCARD_PCI_T1 = SCARD_PCI_T1;
			this.SCARD_PCI_RAW = SCARD_PCI_RAW;
		}
	}

	/**
	 * FunctionMapper from identifier in WinSCard.h to the symbol in the
	 * WinSCard.dll shared library on Windows that implements it.
	 *
	 * <p>
	 * Each function that takes a string has an implementation taking char and a
	 * different implementation that takes wchar_t. We use the ASCII version,
	 * since it is unlikely for reader names to contain non-ASCII.
	 */
	private static class WindowsFunctionMapper implements FunctionMapper {
		static final Set<String> asciiSuffixNames = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
			"SCardListReaderGroups",
			"SCardListReaders",
			"SCardGetStatusChange",
			"SCardConnect",
			"SCardStatus"
		)));
		@Override public String getFunctionName(NativeLibrary library, Method method) {
			String name = method.getName();
			if (asciiSuffixNames.contains(name))
				name = name + 'A';
			return name;
		}
	}

	/**
	 * FunctionMapper from identifier in winscard.h to the symbol in the PCSC
	 * shared library on OSX that implements it.
	 *
	 * <p>
	 * The SCardControl identifier is implemented by the SCardControl132 symbol,
	 * since it appeared in pcsc-lite 1.3.2 and replaced an old function with a
	 * different signature.
	 */
	private static class MacFunctionMapper implements FunctionMapper {
		@Override public String getFunctionName(NativeLibrary library, Method method) {
			String name = method.getName();
			if ("SCardControl".equals(name))
				name = "SCardControl132";
			return name;
		}
	}
	public static WinscardLibInfo openLib() {
		String libraryName = Platform.isWindows() ? WINDOWS_PATH : Platform.isMac() ? MAC_PATH : PCSC_PATH;
		HashMap<Object, Object> options = new HashMap<Object, Object>();
		if (Platform.isWindows()) {
			options.put(Library.OPTION_FUNCTION_MAPPER, new WindowsFunctionMapper());
		} else if (Platform.isMac()) {
			options.put(Library.OPTION_FUNCTION_MAPPER, new MacFunctionMapper());
		}
		WinscardLibrary lib = (WinscardLibrary) Native.loadLibrary(libraryName, WinscardLibrary.class, options);
		NativeLibrary nativeLibrary = NativeLibrary.getInstance(libraryName);
		// SCARD_PCI_* is #defined to the following symbols (both pcsclite and winscard)
		ScardIoRequest SCARD_PCI_T0 = new ScardIoRequest(nativeLibrary.getGlobalVariableAddress("g_rgSCardT0Pci"));
		ScardIoRequest SCARD_PCI_T1 = new ScardIoRequest(nativeLibrary.getGlobalVariableAddress("g_rgSCardT1Pci"));
		ScardIoRequest SCARD_PCI_RAW = new ScardIoRequest(nativeLibrary.getGlobalVariableAddress("g_rgSCardRawPci"));
		SCARD_PCI_T0.read();
		SCARD_PCI_T1.read();
		SCARD_PCI_RAW.read();
		SCARD_PCI_T0.setAutoSynch(false);
		SCARD_PCI_T1.setAutoSynch(false);
		SCARD_PCI_RAW.setAutoSynch(false);
		return new WinscardLibInfo(lib, SCARD_PCI_T0, SCARD_PCI_T1, SCARD_PCI_RAW);
	}
}
