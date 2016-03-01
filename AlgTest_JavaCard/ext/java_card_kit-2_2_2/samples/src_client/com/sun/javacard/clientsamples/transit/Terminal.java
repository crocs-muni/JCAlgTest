/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.javacard.clientsamples.transit;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

import com.sun.javacard.apduio.Apdu;
import com.sun.javacard.apduio.CadT1Client;

/**
 * This class implements the common functionalities of a POS terminal and transit terminal.
 *
 */
public abstract class Terminal implements Constants {

    /**
     * The host where the cref or jcwde tool is running
     */
    protected static String hostName = DEFAULT_HOST_NAME;

    /**
     * The port the cref or jcwde is listening on
     */
    protected static int port = DEFAULT_PORT;

    /**
     * The static DES key - shared b/w the on-card and off-card applications
     */
    protected static byte[] staticKeyData = null;

    /**
     * The CAD client
     */
    private CadT1Client cad;

    /**
     * The cipher used to encrypt - using the static DES key - the derivation
     * data to form the session key
     */
    private Cipher cipher;

    /**
     * The MAC initialized with the DES key and used to verify incoming messages
     * and to sign outgoing messages
     */
    private Mac mac;

    /**
     * DES session key, generated from the derivation data
     */
    private SecretKey sessionKey;

    /**
     * The DES session key factory
     */
    private SecretKeyFactory keyFactory;

    /**
     * Creates a terminal.
     *
     * @param hostName
     *            The hostname where the cref or jcwde tools are running.
     * @param hostPort
     *            The port used by the cref or jcwde tools
     * @param staticKeyData
     *            The static DES key - secret shared by the on-card and off-card
     *            applications
     * @throws Exception
     */
    public Terminal(String hostName, int hostPort, byte[] staticKeyData)
            throws Exception {
        Socket socket = new Socket(hostName, hostPort);
        socket.setTcpNoDelay(true);
        BufferedInputStream input = new BufferedInputStream(socket
                .getInputStream());
        BufferedOutputStream output = new BufferedOutputStream(socket
                .getOutputStream());
        cad = new CadT1Client(input, output);
        KeySpec keySpec = new DESKeySpec(fixParity(staticKeyData));
        keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey staticKey = keyFactory.generateSecret(keySpec);
        cipher = Cipher.getInstance("DES/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, staticKey, new IvParameterSpec(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0}));
    }

    /**
     * Powers up the CAD.
     *
     * @throws Exception
     */
    void powerUp() throws Exception {
        cad.powerUp();
    }

    /**
     * Powers down the CAD.
     *
     */
    void powerDown() {
    try {
        cad.powerDown(true);
    } catch (Exception e) {}
    }

    /**
     * Selects the on-card transit applet.
     *
     * @throws Exception
     */
    void selectApplet() throws Exception {

        // C-APDU: [CLA, INS, P1, P2, LC, [ AID_TRANSIT ]]

        Apdu apdu = new Apdu();
        apdu.command[Apdu.CLA] = CLA_ISO7816;
        apdu.command[Apdu.INS] = INS_SELECT;
        apdu.command[Apdu.P1] = 0x04;
        apdu.command[Apdu.P2] = 0;

        apdu.setDataIn(AID_TRANSIT);

        System.out.println(apdu);
        cad.exchangeApdu(apdu);
        System.out.println(apdu);

        if (apdu.getStatus() == SW_NO_ERROR) {
            System.out.println("OK");
        } else {
            System.out.println("Error: " + apdu.getStatus());
        }
    }

    /**
     * Verifies the user-provided PIN against the on-card PIN.
     *
     * @param pin
     *            The PIN
     * @throws Exception
     */
    void verifyPIN(byte[] pin) throws Exception {

        // C-APDU: [CLA, INS, P1, P2, LC, [ PIN ]]

        Apdu apdu = new Apdu();
        apdu.command[Apdu.CLA] = CLA_ISO7816;
        apdu.command[Apdu.INS] = INS_VERIFY;
        apdu.command[Apdu.P1] = 0;
        apdu.command[Apdu.P2] = 0;

        apdu.setDataIn(pin);

        System.out.println(apdu);
        cad.exchangeApdu(apdu);
        System.out.println(apdu);

        if (apdu.getStatus() == SW_NO_ERROR) {
            System.out.println("OK");
        } else {
            System.out.println("Error: " + apdu.getStatus());
        }
    }

    /**
     * Initializes a session with the on-card applet using a mutual
     * authentication scheme.
     *
     * @throws Exception
     */
    void initializeSession() throws Exception {

        // C-APDU: [CLA, INS, P1, P2, LC, [4-bytes Host Challenge]]

        Apdu apdu = new Apdu();
        apdu.command[Apdu.CLA] = TRANSIT_CLA;
        apdu.command[Apdu.INS] = INITIALIZE_SESSION;
        apdu.command[Apdu.P1] = 0;
        apdu.command[Apdu.P2] = 0;

        // Generate card challenge

        byte[] hostChallenge = generateHostChallenge();

        byte[] data = new byte[hostChallenge.length];
        System.arraycopy(hostChallenge, 0, data, 0, hostChallenge.length);
        apdu.setDataIn(data);

        System.err.println(apdu);
        cad.exchangeApdu(apdu);
        System.err.println(apdu);

        if (apdu.getStatus() == SW_NO_ERROR) {

            // R-APDU: [[4-bytes Card Challenge], [2-bytes Status Word],
            // [8-bytes MAC]]

            data = apdu.getDataOut();

            // Check status word

            byte[] cardChallenge = new byte[CHALLENGE_LENGTH];
            System.arraycopy(data, 0, cardChallenge, 0, CHALLENGE_LENGTH);

            // Generate key derivation data from host challenge and card
            // challenge
            byte[] keyDerivationData = generateKeyDerivationData(hostChallenge,
                    cardChallenge);

            // Generate session key from derivation data
            generateSessionKey(keyDerivationData);

        // Initialize MAC with current session key for verification
        mac = new Mac(sessionKey);

            // Check response message MAC

            if (mac.checkMAC(data, apdu.getLe() - MAC_LENGTH)) {
                System.err.println("OK");
            } else {
        throw new Exception("InitializeSession: Wrong signature");
            }
        } else {
        throw new Exception("InitializeSession: Error " + apdu.getStatus());
        }
    }

    /**
     * Processes a generic request: a transit terminal request or a POS terminal
     * request.
     *
     * @param type
     *            The request type
     * @param requestMessage
     *            The request message content
     * @return The response message content or null if an error occured
     * @throws Exception
     */
    protected byte[] processRequest(byte type, byte[] requestMessage)
            throws Exception {

        Apdu apdu = new Apdu();
        apdu.command[Apdu.CLA] = TRANSIT_CLA;
        apdu.command[Apdu.INS] = PROCESS_REQUEST;
        apdu.command[Apdu.P1] = 0;
        apdu.command[Apdu.P2] = 0;

        byte[] data = new byte[(2 + requestMessage.length) + MAC_LENGTH];
        data[0] = type; // TLV Tag
        data[1] = (byte) requestMessage.length; // TLV Length
        // TLV Value
        System.arraycopy(requestMessage, 0, data, 2, requestMessage.length);
        mac.generateMAC(data, 2 + requestMessage.length);
        apdu.setDataIn(data);

        System.err.println(apdu);
        cad.exchangeApdu(apdu);
        System.err.println(apdu);

        if (apdu.getStatus() == SW_NO_ERROR) {

            // R-APDU: [[Response Message], [2-bytes Status Word], [8-bytes
            // MAC]]

            byte[] responseMessage = apdu.getDataOut();

            // Check response message MAC

            if (mac.checkMAC(responseMessage, (apdu.getLe() - MAC_LENGTH))) {

                // Check status word

                data = new byte[apdu.getLe() - (2 + MAC_LENGTH)];
                System.arraycopy(responseMessage, 0, data, 0, data.length);

                return data;
            }
        }
        return null;
    }

    /**
     * Generates a host challenge.
     *
     * @return The host challenge
     */
    protected byte[] generateHostChallenge() {
        byte[] hostChallenge = new byte[CHALLENGE_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(hostChallenge);
        return hostChallenge;
    }

    /**
     * Generates the session key derivation data from the passed-in host and
     * card challenges.
     *
     * @param hostChallenge
     *            The host challenge
     * @param cardChallenge
     *            The card challenge
     * @return The key derivation data
     */
    protected byte[] generateKeyDerivationData(byte[] hostChallenge,
            byte[] cardChallenge) {

        // Derivation data: [[4-bytes host challenge], [4-bytes card challenge]]

        byte[] keyDerivationData = new byte[CHALLENGE_LENGTH * 2];

        // Append host challenge to derivation data
        System.arraycopy(hostChallenge, 0, keyDerivationData, 0,
                CHALLENGE_LENGTH);
        // Append card challenge to derivation data
        System.arraycopy(cardChallenge, (short) 0, keyDerivationData,
                CHALLENGE_LENGTH, CHALLENGE_LENGTH);
        return keyDerivationData;
    }

    /**
     * Generates a new DES session key from the derivation data.
     *
     * @param keyDerivationData
     *            The key derivation data
     *
     */
    protected void generateSessionKey(byte[] keyDerivationData)
            throws Exception {
    byte[] paddedData = pad(keyDerivationData, 0, keyDerivationData.length, cipher.getBlockSize());
        byte[] sessionKeyData = fixParity(cipher.doFinal(paddedData));
        // Generate new session key from derivation data
        KeySpec keySpec = new DESKeySpec(sessionKeyData);
        sessionKey = keyFactory.generateSecret(keySpec);
    }

    /**
     * Gets a short integer from a byte array.
     *
     * @param buffer
     *            The byte array
     * @param offset
     *            The offset of the first byte in the buffer.
     * @return The short integer
     */
    protected short getShort(byte[] buffer, int offset) {
        return (short) ((((short) buffer[offset]) << 8) | buffer[offset + 1]);
    }

    /**
     * Copies a short integer into a byte array.
     *
     * @param i
     *            The short integer to cpy
     * @param buffer
     *            The byte array
     * @param offset
     *            The offset in the buffer where to copy the short integer
     */
    protected void copyShort(short i, byte[] buffer, int offset) {
        buffer[offset] = (byte) ((i >> 8) & 0x00ff);
        buffer[offset + 1] = (byte) (i & 0x00ff);
    }

    /**
     * Prints the common usage message.
     *
     */
    protected static void commonUsage() {
        System.out
                .println("[-?] [-h <hostname>] [-p <port>] -k <8-bytes DES static key> <command list>");
    }

    /**
     * Parses the common CLI arguments.
     *
     * @param args
     *            The arguments
     * @return The offset in the arguments array just after the common arguments
     */
    protected static int parseCommonArgs(String[] args) {
        int i = 0;
        for (; i < args.length && args[i].startsWith("-"); i++) {
            if (args[i].equals("-?")) {
                commonUsage();
                System.exit(0);
            } else if (args[i].equals("-h")) {
                if (++i < args.length) {
                    hostName = args[i];
                } else {
                    commonUsage();
                    System.exit(2);
                }
            } else if (args[i].equals("-p")) {
                if (++i < args.length) {
                    port = Integer.valueOf(args[i]).intValue();
                } else {
                    commonUsage();
                    System.exit(2);
                }
            } else if (args[i].equals("-k")) {
                if (++i < args.length) {
                    if ((args[i].length() / 2) < LENGTH_DES_BYTE) {
                        commonUsage();
                        System.exit(1);
                    }
                    staticKeyData = parseByteArray(args[i]);
                } else {
                    commonUsage();
                    System.exit(2);
                }
            } else if (args[i].equals("--")) {
                i++;
                break;
            } else {
                commonUsage();
                System.exit(2);
            }
        }
        if (staticKeyData == null) {
            return -1;
        }
        return i;
    }

    private static byte[] parseByteArray(String s) {
    byte[] array = new byte[s.length() / 2];
    for (int i = 0; i < s.length(); i += 2) {
        array[i / 2] = (byte) Integer.parseInt(s.substring(i, i + 2), 16);
    }
    return array;
    }

    private byte[] pad(byte[] msg, int offset, int length, int blockLength)  {
    // Add 1 to add 0x80 at the end.
    int paddedLength = length + 1;
    int numBlocks = (int) (paddedLength / blockLength);
    int remBytes = paddedLength - (numBlocks * blockLength);
    if (remBytes > 0) {
        numBlocks++;
    }
    byte[] paddedMsg = new byte[numBlocks * blockLength];
    System.arraycopy(msg, offset, paddedMsg, 0, length);
    paddedMsg[length] = (byte) 0x80;
    // Fill message with zeroes to fit blocks
    for (int i = (length + 1); i < paddedMsg.length; i++) {
        paddedMsg[i] = (byte) 0x00;
    }
    return paddedMsg;
    }

    private byte[] fixParity(byte[] keyData) {
    for (int i = 0; i < keyData.length; i++) {
        short parity = 0;
        keyData[i] &= 0xFE;
        for (int j = 1; j < 8; j++) {
        if ((keyData[i] & ((byte) (1 << j))) != 0) {
            parity++;
        }
        }
        if ((parity % 2) == 0) {
        keyData[i] |= 1;
        }
    }
    return keyData;
    }

    /**
     * This class implements an algorithm equivalent to
     * javacard.security.Signature.ALG_DES_MAC8_ISO9797_M2
     *
     */
    private class Mac {

    /**
     * The cipher used to encrypt the message and derive the MAC
     */
    private Cipher cipher;

    public Mac(SecretKey key)
            throws Exception {
        cipher = Cipher.getInstance("DES/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0}));
    }

    /**
     * Checks the message signature.
     *
     * @param buffer
     *            The message buffer
     * @param offset
     *            The offset of the MAC in the buffer
     * @return true if the message signature is correct; false otherwise
     */
    protected boolean checkMAC(byte[] buffer, int offset) throws Exception {
        // Generate the MAC for the response
        byte[] paddedMsg = pad(buffer, 0, offset, cipher.getBlockSize());
        byte[] encryptedMsg = cipher.doFinal(paddedMsg);
        byte[] hostMAC =  new byte[MAC_LENGTH];
        System.arraycopy(encryptedMsg, encryptedMsg.length - MAC_LENGTH, hostMAC, 0, MAC_LENGTH);
        byte[] cardMAC = new byte[MAC_LENGTH];
        System.arraycopy(buffer, offset, cardMAC, 0, MAC_LENGTH);
        // Verify message signature
        return Arrays.equals(hostMAC, cardMAC);
    }

    /**
     * Generates a message MAC: generates the MAC and appends the MAC
     * to the message.
     *
     * @param buffer
     *            The APDU buffer
     * @param offset
     *            The offset of the MAC in the buffer
     * @return The resulting length of the request message
     * @throws Exception
     */
    protected short generateMAC(byte[] buffer, int offset)
            throws Exception {
        // Sign request message and append the MAC to the request message
        byte[] paddedMsg = pad(buffer, 0, offset, cipher.getBlockSize());
        byte[] encryptedMsg = cipher.doFinal(paddedMsg);
        System.arraycopy(encryptedMsg, encryptedMsg.length - MAC_LENGTH, buffer, offset, MAC_LENGTH);
        return (short) (offset + MAC_LENGTH);
    }
    }
}
