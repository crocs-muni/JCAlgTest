/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.javacard.samples.transit;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.OwnerPIN;
import javacard.framework.Util;
import javacard.security.DESKey;
import javacard.security.KeyBuilder;
import javacard.security.RandomData;
import javacard.security.Signature;
import javacardx.crypto.Cipher;

/**
 * This applet implements the on-card part of a transit system solution. The
 * on-card applet and the off-card applications (transit terminal and POS
 * terminal) use a mutual authentication scheme based on a dynamically generated
 * DES session key to ensure data integrity and origin authentication during a
 * session.
 * 
 * When interacting with a POS terminal, the account maintained on the card can
 * be credited or queried for the current balance.
 * 
 * When interacting with a transit terminal, the transit system entry and the
 * exit events are checked for consistency and processed - the account
 * maintained on the card is debited upon proper exit from the transit system.
 *
 * Design notes:
 * - This sample transit applet does not account for any admin or self-admin use cases such as 
 * resetting the card of a transit system user when it is in an inconsistent transit
 * state. Such an inconsistent state can, for example, result from the user jumping the gates when
 * the turnstile is out of order...
 * - This sample transit applet does not account for any system-wide transactional
 * operations. For example, during a credit operation, if the user removes his card
 * just after the balance has been updated but before the APDU response gets to
 * the terminal, the account on the card will remain credited but the terminal will
 * only be able to detect an IO error b/w the card and the card reader.
 * - The constants defined for this class should have been shared through
 * an additional class or interface with the terminal code
 * (see com.sun.javacard.clientsamples.transit.Constants).
 * - This applet could be refactored so that the mutual authentication code
 * be moved in a base abstract class and the transit system specific behavior be
 * implemented in a subclass of this base class. This refactoring would facilitate
 * the reuse of the mutual authentication scheme in other application domain.
 */
public class TransitApplet extends Applet {

    // Codes of INS byte in the command APDU header

    /**
     * INS value for ISO 7816-4 VERIFY command
     */
    final static byte VERIFY = (byte) 0x20;

    /**
     * INS value for INITIALIZE_SESSION command
     */
    final static byte INITIALIZE_SESSION = (byte) 0x30;

    /**
     * INS value for PROCESS_REQUEST command
     */
    final static byte PROCESS_REQUEST = (byte) 0x40;

    // Tags for TLV records in PROCESS_REQUEST C-APDU

    /**
     * TLV Tag for PROCESS_ENTRY request
     */
    final static byte PROCESS_ENTRY = (byte) 0xC1;

    /**
     * TLV Tag for PROCESS_EXIT request
     */
    final static byte PROCESS_EXIT = (byte) 0xC2;

    /**
     * TLV Tag for CREDIT request
     */
    final static byte CREDIT = (byte) 0xC3;

    /**
     * TLV Tag for GET_BALANCE request
     */
    final static byte GET_BALANCE = (byte) 0xC4;

    // Offsets of TLV components in PROCESS_REQUEST C-APDU [CLA, INS, P1, P2, LC
    // T L V...]

    /**
     * TLV tag offset
     */
    final static short TLV_TAG_OFFSET = ISO7816.OFFSET_CDATA;

    /**
     * TLV length offset
     */
    final static short TLV_LENGTH_OFFSET = TLV_TAG_OFFSET + 1;

    /**
     * TLV value offset
     */
    final static short TLV_VALUE_OFFSET = TLV_LENGTH_OFFSET + 1;

    /**
     * Maximum allowed balance
     */
    final static short MAX_BALANCE = (short) 500;

    /**
     * Minimum balance to start transit
     */
    final static short MIN_TRANSIT_BALANCE = (short) 10;

    /**
     * Maximum amount to be credited
     */
    final static short MAX_CREDIT_AMOUNT = (short) 100;

    /**
     * Maximum number of incorrect tries before the PIN is blocked
     */
    final static byte MAX_PIN_TRIES = (byte) 0x03;

    /**
     * Maximum PIN size
     */
    final static byte MAX_PIN_SIZE = (byte) 0x08;

    /**
     * SW bytes for PIN verification failure
     */
    final static short SW_VERIFICATION_FAILED = 0x6300;

    /**
     * SW bytes for PIN validation required
     */
    final static short SW_PIN_VERIFICATION_REQUIRED = 0x6301;

    /**
     * SW bytes for invalid credit amount (amount > MAX_CREDIT_AMOUNT or amount <
     * 0)
     */
    final static short SW_INVALID_TRANSACTION_AMOUNT = 0x6A83;

    /**
     * SW bytes for maximum balance exceeded
     */
    final static short SW_EXCEED_MAXIMUM_BALANCE = 0x6A84;

    /**
     * SW bytes for negative balance reached
     */
    final static short SW_NEGATIVE_BALANCE = 0x6A85;

    /**
     * SW bytes for wrong signature condition
     */
    final static short SW_WRONG_SIGNATURE = (short) 0x9105;

    /**
     * SW bytes for minimum transit balance not met
     */
    final static short SW_MIN_TRANSIT_BALANCE = (short) 0x9106;

    /**
     * SW bytes for invalid transit state
     */
    final static short SW_INVALID_TRANSIT_STATE = (short) 0x9107;

    /**
     * SW bytes for success, used in MAC
     */
    final static short SW_SUCCESS = (short) 0x9000;

    /**
     * Unique ID length
     */
    final static short UID_LENGTH = (short) 8;

    /**
     * DES key length in bytes
     */
    final static short LENGTH_DES_BYTE = (short) (KeyBuilder.LENGTH_DES / 8);

    /**
     * Host and card challenge length (note: (2 * CHALLENGE_LENGTH) * 8 ==
     * KeyBuilder.LENGTH_DES
     */
    final static short CHALLENGE_LENGTH = (short) 4;

    /**
     * MAC length as generated by Signature.ALG_DES_MAC8_ISO9797_M2
     */
    final static short MAC_LENGTH = (short) 8;

    /**
     * Unique ID
     */
    private byte[] uid;

    // Signature/key objects

    /**
     * Cipher used to encrypt - using the static DES key - the derivation data
     * to form the session key
     */
    private Cipher cipher;

    /**
     * DES static key, shared b/w host and card
     */
    private DESKey staticKey;

    /**
     * 4-bytes Card challenge
     */
    private byte[] cardChallenge; // Transient

    /**
     * 8-bytes key derivation data, generated from the host challenge and the
     * card challenge
     */
    private byte[] keyDerivationData; // Transient

    /**
     * 8-bytes session key data, generated from the derivation data
     */
    private byte[] sessionKeyData; // Transient

    /**
     * DES session key, generated from the derivation data
     */
    private DESKey sessionKey; // Transient key

    /**
     * Indicates whether or not to use transient session key - for performance
     * measurement only
     */
    private boolean useTransientKey = true;

    /**
     * Signature initialized with the DES key and used to verify incoming
     * messages and to sign outgoing messages
     */
    private Signature signature;

    /**
     * Random data generator, used to generate the card challenge
     */
    private RandomData random;

    /**
     * The user PIN
     */
    private OwnerPIN pin;

    /**
     * The balance
     */
    private short balance = (short) 0;

    /**
     * The entry ststion id, set to (-1) when not in transit
     */
    private short entryStationId = (short) -1;

    /**
     * A correlation id that may be used by the backend system to correlate
     * entry and exit events
     */
    private byte correlationId = (byte) 0;

    /**
     * Creates a new Transit applet instance.
     * 
     * @param bArray
     *            The array containing installation parameters
     * @param bOffset
     *            The starting offset in bArray
     * @param bLength
     *            The length in bytes of the parameter data in bArray
     */
    protected TransitApplet(byte[] bArray, short bOffset, byte bLength) {

        // Create static DES key
        staticKey = (DESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_DES,
                KeyBuilder.LENGTH_DES, false);

        // Create cipher
        cipher = Cipher.getInstance(Cipher.ALG_DES_CBC_ISO9797_M2, false);

        // Create card challenge transient buffer
        cardChallenge = JCSystem.makeTransientByteArray(CHALLENGE_LENGTH,
                JCSystem.CLEAR_ON_DESELECT);

        // Create key derivation data transient buffer
        keyDerivationData = JCSystem.makeTransientByteArray(
                (short) (2 * CHALLENGE_LENGTH), JCSystem.CLEAR_ON_DESELECT);

        // Create session key data transient buffer
        sessionKeyData = JCSystem.makeTransientByteArray(
	    (short) (2 * keyDerivationData.length),
	    JCSystem.CLEAR_ON_DESELECT);
        // XXX: Allocates more than actual key to contain the complete
        // encrypted key derivation data

        // Create signature
        signature = Signature.getInstance(Signature.ALG_DES_MAC8_ISO9797_M2,
                false);

        byte aidLen = bArray[bOffset]; // aid length
        if (aidLen == (byte) 0) {
            register();
        } else {
            register(bArray, (short) (bOffset + 1), aidLen);
        }

        // Ignore control info
        bOffset = (short) (bOffset + aidLen + 1);
        byte infoLen = bArray[bOffset]; // control info length
        bOffset = (short) (bOffset + infoLen + 1);

        byte paramLen = bArray[bOffset++]; // applet parameters length

        // Retrieve UID, static key data and the PIN initialization values from
        // installation parameters

        if (paramLen <= (LENGTH_DES_BYTE + UID_LENGTH)
                || paramLen > (LENGTH_DES_BYTE + UID_LENGTH + MAX_PIN_SIZE)) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        // Retrieve the UID
        uid = new byte[UID_LENGTH];
        Util.arrayCopy(bArray, bOffset, uid, (short) 0, UID_LENGTH);
        bOffset += UID_LENGTH;

        // Retrieve the static key data
        staticKey.setKey(fixParity(bArray, bOffset, LENGTH_DES_BYTE), bOffset);
        bOffset += LENGTH_DES_BYTE;

	// Retrieve the flag indicating whether or not to use a transient key
	useTransientKey = (bArray[bOffset] != (byte) 0);
	bOffset++;
	    
        // Retrieve the PIN
        pin = new OwnerPIN(MAX_PIN_TRIES, MAX_PIN_SIZE);
        pin.update(bArray, bOffset,
                (byte) (paramLen - UID_LENGTH - LENGTH_DES_BYTE - 1));

        // Create transient DES session key
	if (useTransientKey) {
	    sessionKey = (DESKey) KeyBuilder.buildKey(
                KeyBuilder.TYPE_DES_TRANSIENT_DESELECT, KeyBuilder.LENGTH_DES,
                false);
	} else {
	    sessionKey = (DESKey) KeyBuilder.buildKey(
                KeyBuilder.TYPE_DES, KeyBuilder.LENGTH_DES,
                false);
	}

        // Create and initialize the ramdom data generator with the UID (seed)
        random = RandomData.getInstance(RandomData.ALG_PSEUDO_RANDOM);
        random.setSeed(uid, (short) 0, UID_LENGTH);

        // Initialize the cipher with the static key
        cipher.init(staticKey, Cipher.MODE_ENCRYPT);

    }

    public static void install(byte[] bArray, short bOffset, byte bLength) {
        // Create a Transit applet instance
        new TransitApplet(bArray, bOffset, bLength);
    }

    public boolean select() {
        // The applet declines to be selected
        // if the PIN is blocked.
        if (pin.getTriesRemaining() == 0) {
            return false;
        }
        return true;
    }

    public void deselect() {
        // Reset the PIN value
        pin.reset();
	if (!useTransientKey) {
	    sessionKey.clearKey();
	}
    }

    public void process(APDU apdu) {

        // C-APDU: [CLA, INS, P1, P2, LC, ...]

        byte[] buffer = apdu.getBuffer();

        // Dispatch C-APDU for processing
        if (!apdu.isISOInterindustryCLA()) {
            switch (buffer[ISO7816.OFFSET_INS]) {
            case INITIALIZE_SESSION:
                initializeSession(apdu);
                return;
            case PROCESS_REQUEST:
                processRequest(apdu);
                return;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
            }
        } else {
            if (buffer[ISO7816.OFFSET_INS] == (byte)(0xA4)) {
                return;
            } else if (buffer[ISO7816.OFFSET_INS] == VERIFY) {
                verify(apdu);
            } else {
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
            }
        }
    }

    /**
     * Initializes a CAD/card interaction session. This is the first step of
     * mutual authentication. A new card challenge is generated and used along
     * with the passed-in host challenge to generate the derivation data from
     * which a new session key is derived. The card challenge is appended to the
     * response message. The response message is signed using the newly
     * generated session key then sent back. Note that mutual authentication is
     * subsequently completed upon succesful verification of the signature of
     * the first request received.
     * 
     * @param apdu
     *            The APDU
     */
    private void initializeSession(APDU apdu) {

        // C-APDU: [CLA, INS, P1, P2, LC, [4-bytes Host Challenge]]

        byte[] buffer = apdu.getBuffer();

        if ((buffer[ISO7816.OFFSET_P1] != 0)
                || (buffer[ISO7816.OFFSET_P2] != 0)) {
            ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
        }

        byte numBytes = buffer[ISO7816.OFFSET_LC];

        byte count = (byte) apdu.setIncomingAndReceive();

        if (numBytes != CHALLENGE_LENGTH || count != CHALLENGE_LENGTH) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        // Generate card challenge
        generateCardChallenge();

        // Generate key derivation data from host challenge and card challenge
        generateKeyDerivationData(buffer);

        // Generate session key from derivation data
        generateSessionKey();

        // R-APDU: [[4-bytes Card Challenge], [2-bytes Status Word], [8-bytes
        // MAC]]

        short offset = 0;

        // Append card challenge to response message
        offset = Util.arrayCopyNonAtomic(cardChallenge, (short) 0, buffer,
                offset, CHALLENGE_LENGTH);

        // Append status word to response message
        offset = Util.setShort(buffer, offset, SW_SUCCESS);

        // Sign response message and append MAC to response message
        offset = generateMAC(buffer, offset);

        // Send R-APDU
        apdu.setOutgoingAndSend((short) 0, offset);
    }

    /**
     * Processes an incoming request. The request message signature is verified,
     * then it is dispatched to the relevant handling method. The response
     * message is then signed and sent back.
     * 
     * @param apdu
     *            The APDU
     */
    private void processRequest(APDU apdu) {

        // C-APDU: [CLA, INS, P1, P2, LC, [Request Message], [8-bytes MAC]]
        // Request Message: [T, L, [V...]]

        byte[] buffer = apdu.getBuffer();

        if ((buffer[ISO7816.OFFSET_P1] != 0)
                || (buffer[ISO7816.OFFSET_P2] != 0)) {
            ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
        }

        byte numBytes = buffer[ISO7816.OFFSET_LC];

        byte count = (byte) apdu.setIncomingAndReceive();

        if (numBytes != count) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        // Check request message signature
        if (!checkMAC(buffer)) {
            ISOException.throwIt(SW_WRONG_SIGNATURE);
        }

        if ((numBytes - MAC_LENGTH) != (buffer[TLV_LENGTH_OFFSET] + 2)) {
            ISOException.throwIt(ISO7816.SW_WRONG_DATA);
        }

        // R-APDU: [[Response Message], [2-bytes Status Word], [8-bytes MAC]]

        short offset = 0;

        // Dispatch request message for processing
        switch (buffer[TLV_TAG_OFFSET]) {
        case PROCESS_ENTRY:
            offset = processEntry(buffer, TLV_VALUE_OFFSET,
                    buffer[TLV_LENGTH_OFFSET]);
            break;
        case PROCESS_EXIT:
            offset = processExit(buffer, TLV_VALUE_OFFSET,
                    buffer[TLV_LENGTH_OFFSET]);
            break;
        case CREDIT:
            offset = credit(buffer, TLV_VALUE_OFFSET, buffer[TLV_LENGTH_OFFSET]);
            break;
        case GET_BALANCE:
            offset = getBalance(buffer, TLV_VALUE_OFFSET,
                    buffer[TLV_LENGTH_OFFSET]);
            break;
        default:
            ISOException.throwIt(ISO7816.SW_FUNC_NOT_SUPPORTED);
        }

        // Append status word to response message
        offset = Util.setShort(buffer, offset, SW_SUCCESS);

        // Sign response message and append MAC to response message
        offset = generateMAC(buffer, offset);

        // Send R-APDU
        apdu.setOutgoingAndSend((short) 0, offset);
    }

    /**
     * Verifies the PIN.
     * 
     * @param apdu
     *            The APDU
     */
    private void verify(APDU apdu) {

        byte[] buffer = apdu.getBuffer();

        byte numBytes = buffer[ISO7816.OFFSET_LC];

        byte count = (byte) apdu.setIncomingAndReceive();

        if (numBytes != count) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        // Verify PIN
        if (pin.check(buffer, ISO7816.OFFSET_CDATA, numBytes) == false) {
            ISOException.throwIt(SW_VERIFICATION_FAILED);
        }
    }

    /**
     * Generates a new random card challenge.
     *  
     */
    private void generateCardChallenge() {
        // Generate random card challenge
        random.generateData(cardChallenge, (short) 0, CHALLENGE_LENGTH);
    }

    /**
     * Generates the session key derivation data from the passed-in host
     * challenge and the card challenge.
     * 
     * @param buffer
     *            The APDU buffer
     */
    private void generateKeyDerivationData(byte[] buffer) {
        byte numBytes = buffer[ISO7816.OFFSET_LC];

        if (numBytes < CHALLENGE_LENGTH) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        // Derivation data: [[8-bytes host challenge], [8-bytes card challenge]]

        // Append host challenge (from buffer) to derivation data
        Util.arrayCopy(buffer, ISO7816.OFFSET_CDATA, keyDerivationData,
                (short) 0, CHALLENGE_LENGTH);
        // Append card challenge to derivation data
        Util.arrayCopy(cardChallenge, (short) 0, keyDerivationData,
                CHALLENGE_LENGTH, CHALLENGE_LENGTH);
    }

    /**
     * Generates a new DES session key from the derivation data.
     *  
     */
    private void generateSessionKey() {
        cipher.doFinal(keyDerivationData, (short) 0, (short) keyDerivationData.length,
                sessionKeyData, (short) 0);
        // Generate new session key from encrypted derivation data
        sessionKey.setKey(fixParity(sessionKeyData, (short) 0, (short) sessionKeyData.length /*LENGTH_DES_BYTE*/), (short) 0);
    }

    /**
     * Checks the request message signature.
     * 
     * @param buffer
     *            The APDU buffer
     * @return true if the message signature is correct; false otherwise
     */
    private boolean checkMAC(byte[] buffer) {
        byte numBytes = buffer[ISO7816.OFFSET_LC];

        if (numBytes <= MAC_LENGTH) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        // Initialize signature with current session key for verification
        signature.init(sessionKey, Signature.MODE_VERIFY);
        // Verify request message signature
        return signature.verify(buffer, ISO7816.OFFSET_CDATA,
                (short) (numBytes - MAC_LENGTH), buffer,
                (short) (ISO7816.OFFSET_CDATA + numBytes - MAC_LENGTH),
	        MAC_LENGTH);
    }

    /**
     * Generates the response message MAC: generates the MAC and appends the MAC
     * to the response message.
     * 
     * @param buffer
     *            The APDU buffer
     * @param offset
     *            The offset of the MAC in the buffer
     * @return The resulting length of the response message
     */
    private short generateMAC(byte[] buffer, short offset) {
        // Initialize signature with current session key for signing
        signature.init(sessionKey, Signature.MODE_SIGN);
        // Sign response message and append the MAC to the response message
        short sigLength = signature.sign(buffer, (short) 0, offset, buffer,
                offset);
        return (short) (offset + sigLength);
    }

    /**
     * Processes a transit entry event. The passed-in entry station ID is
     * recorded and the correlation ID is incremented. The UID and the
     * correlation ID are returned in the response message.
     * 
     * Request Message: [2-bytes Entry Station ID]
     * 
     * Response Message: [[2-bytes UID], [2-bytes Correlation ID]]
     * 
     * @param buffer
     *            The APDU buffer
     * @param messageOffset
     *            The offset of the request message content in the APDU buffer
     * @param messageLength
     *            The length of the request message content.
     * @return The offset at which content can be appended to the response
     *         message
     */
    private short processEntry(byte[] buffer, short messageOffset,
            short messageLength) {

        // Request Message: [2-bytes Entry Station ID]

        if (messageLength != 2) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        // Check minimum balance
        if (balance < MIN_TRANSIT_BALANCE) {
            ISOException.throwIt(SW_MIN_TRANSIT_BALANCE);
        }

        // Check consistent transit state: should not currently be in transit
        if (entryStationId >= 0) {
            ISOException.throwIt(SW_INVALID_TRANSIT_STATE);
        }

        JCSystem.beginTransaction();

        // Get/assign entry station ID from request message
        entryStationId = Util.getShort(buffer, messageOffset);

        // Increment correlation ID
        correlationId++;

        JCSystem.commitTransaction();

        // Response Message: [[8-bytes UID], [2-bytes Correlation ID]]

        short offset = 0;

        // Append UID to response message
        offset = Util.arrayCopy(uid, (short) 0, buffer, offset, UID_LENGTH);

        // Append correlation ID to response message
        offset = Util.setShort(buffer, offset, correlationId);

        return offset;
    }

    /**
     * Processes a transit exit event. The passed-in transit fee is debited from
     * the account. The UID and the correlation ID are returned in the response
     * message.
     * 
     * Request Message: [1-byte Transit Fee]
     * 
     * Response Message: [[2-bytes UID], [2-bytes Correlation ID]]
     * 
     * @param buffer
     *            The APDU buffer
     * @param messageOffset
     *            The offset of the request message content in the APDU buffer
     * @param messageLength
     *            The length of the request message content.
     * @return The offset at which content can be appended to the response
     *         message
     */
    private short processExit(byte[] buffer, short messageOffset,
            short messageLength) {

        // Request Message: [1-byte Transit Fee]

        if (messageLength != 1) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        // Check minimum balance
        if (balance < MIN_TRANSIT_BALANCE) {
            ISOException.throwIt(SW_MIN_TRANSIT_BALANCE);
        }

        // Check consistent transit state: should be currently in transit
        if (entryStationId < 0) {
            ISOException.throwIt(SW_INVALID_TRANSIT_STATE);
        }

        // Get transit fee from request message
        byte transitFee = buffer[messageOffset];

        // Check potential negative balance
        if (balance < transitFee) {
            ISOException.throwIt(SW_NEGATIVE_BALANCE);
        }

        JCSystem.beginTransaction();

        // Debit transit fee
        balance -= transitFee;

        // Reset entry station ID
        entryStationId = -1;

        JCSystem.commitTransaction();

        // Response Message: [[8-bytes UID], [2-bytes Correlation ID]]

        short offset = 0;

        // Append UID to response message
        offset = Util.arrayCopy(uid, (short) 0, buffer, offset, UID_LENGTH);

        // Append correlation ID to response message
        offset = Util.setShort(buffer, offset, correlationId);

        return offset;
    }

    /**
     * Credits the account of the passed-in amount.
     * 
     * Request Message: [1-byte Credit Amount]
     * 
     * Response Message: []
     * 
     * @param buffer
     *            The APDU buffer
     * @param messageOffset
     *            The offset of the request message content in the APDU buffer
     * @param messageLength
     *            The length of the request message content.
     * @return The offset at which content can be appended to the response
     *         message
     */
    private short credit(byte[] buffer, short messageOffset, short messageLength) {

        // Check access authorization
        if (!pin.isValidated()) {
            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
        }

        // Request Message: [1-byte Credit Amount]

        if (messageLength != 1) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        // Get credit amount from request message
        byte creditAmount = buffer[messageOffset];

        // Check credit amount
        if ((creditAmount > MAX_CREDIT_AMOUNT) || (creditAmount < 0)) {
            ISOException.throwIt(SW_INVALID_TRANSACTION_AMOUNT);
        }

        // Check the new balance
        if ((short) (balance + creditAmount) > MAX_BALANCE) {
            ISOException.throwIt(SW_EXCEED_MAXIMUM_BALANCE);
        }

        // Credit the amount
        balance += creditAmount;

        // Response Message: []

        return 0;
    }

    /**
     * Gets/returns the balance.
     * 
     * Request Message: []
     * 
     * Response Message: [2-bytes Balance]
     * 
     * @param buffer
     *            The APDU buffer
     * @param messageOffset
     *            The offset of the request message content in the APDU buffer
     * @param messageLength
     *            The length of the request message content.
     * @return The offset at which content can be appended to the response
     *         message
     */
    private short getBalance(byte[] buffer, short messageOffset,
            short messageLength) {

        // Check access authorization
        if (!pin.isValidated()) {
            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
        }

        // Request Message: []

        if (messageLength != 0) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        // Response Message: [2-bytes Balance]

        short offset = 0;

        // Append balance to response message
        offset = Util.setShort(buffer, offset, balance);

        return offset;
    }

    /**
     * Fixes the parity on DES key data.
     * 
     * @param buffer
     *            The buffer containing the DES key data
     * @param offset
     *            The offset of the DES key data in the buffer
     * @param messageLength
     *            The length of the DES key data
     * @return The passed-in buffer with the DES key data parity fixed
     */
    private byte[] fixParity(byte[] buffer, short offset, short length) {
	for (byte i = 0; i < length; i++) {
	    short parity = 0;
	    buffer[(short) (offset + i)] &= 0xFE;
	    for (byte j = 1; j < 8; j++) {
		if ((buffer[(short) (offset + i)] & (byte) (1 << j)) != 0) {
		    parity++;
		}
	    }
	    if ((parity % 2) == 0) {
		buffer[(short) (offset + i)] |= 1;
	    }
	}
	return buffer;
    }
}
