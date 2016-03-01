/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.javacard.clientsamples.transit;

/**
 * This class implements a transit terminal.
 *
 * It sends transit system entry and exit events to the on-card applet for
 * processing.
 */
public class TransitTerminal extends Terminal {

    /**
     * Creates a transit terminal.
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
    TransitTerminal(String hostName, int hostPort, byte[] staticKeyData)
            throws Exception {
        super(hostName, hostPort, staticKeyData);
    }

    /**
     * Sends a transit system entry event to the on-card applet for processing.
     *
     * @param entryStationId
     *            The entry station id
     * @throws Exception
     */
    private void processEntry(short entryStationId) throws Exception {

        // Request Message: [2-bytes Entry Station ID]

        byte[] requestMessage = new byte[2];

        copyShort(entryStationId, requestMessage, 0);

        // Response Message: [[8-bytes UID], [2-bytes Correlation ID]]

        byte[] responseMessage = processRequest(PROCESS_ENTRY, requestMessage);

        if (responseMessage != null) {

            // Retrieve the UID
            byte[] uid = new byte[UID_LENGTH];
            System.arraycopy(responseMessage, 0, uid, 0, UID_LENGTH);

            // Retrieve the correlation Id
            short correlationId = getShort(responseMessage, 2);

            System.out.println("processEntry: [" + entryStationId + "] => "
                    + "[ " + new String(uid) + ", " + correlationId + "]");
        } else {

            System.out.println("processEntry: [" + entryStationId + "] => "
                    + "error");
        }
    }

    /**
     * Sends a transit system exit event to the on-card applet for processing.
     *
     * @param transitFee
     *            The transit fee to be debited from the on-card account.
     * @throws Exception
     */
    private void processExit(byte transitFee) throws Exception {

        // Request Message: [1-byte Transit Fee]

        byte[] requestMessage = new byte[1];

        requestMessage[0] = transitFee;

        // Response Message: [[8-bytes UID], [2-bytes Correlation ID]]

        byte[] responseMessage = processRequest(PROCESS_EXIT, requestMessage);

        if (responseMessage != null) {

            // Retrieve the UID
            byte[] uid = new byte[UID_LENGTH];
            System.arraycopy(responseMessage, 0, uid, 0, UID_LENGTH);

            // Retrieve the correlation Id
            short correlationId = getShort(responseMessage, 2);

            System.out.println("processEntry: [" + transitFee + "] => " + "[ "
                    + new String(uid) + ", " + correlationId + "]");
        } else {

            System.out.println("processEntry: [" + transitFee + "] => "
                    + "error");
        }
    }

    /**
     * Prints the usage.
     *
     */
    private static void usage() {
        commonUsage();
        System.out
                .println("<command list>: ([PROCESS_ENTRY <entry station id>]|[PROCESS_EXIT <transit fee>])*");
    }

    /**
     * Parses and runs the CLI command.
     *
     * @param args
     *            The CLI arguments: <command list>: ([PROCESS_ENTRY <entry
     *            station id>]|[PROCESS_EXIT <transit fee>])*
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        int i = parseCommonArgs(args);
        if (i <= 0) {
            usage();
            System.exit(3);
        }

        TransitTerminal terminal = new TransitTerminal(hostName, port,
                staticKeyData);

        terminal.powerUp();
        terminal.selectApplet();
        terminal.initializeSession();

        for (; i < args.length; i++) {
            if (args[i].equals("PROCESS_ENTRY")) {
                if (++i < args.length) {
                    short entryStationId = new Short(args[i]).shortValue();
                    terminal.processEntry(entryStationId);
                } else {
                    usage();
                    System.exit(3);
                }
            } else if (args[i].equals("PROCESS_EXIT")) {
                if (++i < args.length) {
                    byte transitFee = new Byte(args[i]).byteValue();
                    terminal.processExit(transitFee);
                } else {
                    usage();
                    System.exit(3);
                }
            } else {
                usage();
                System.exit(3);
            }
        }

        terminal.powerDown();

        System.exit(0);
    }
}
