/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.javacard.clientsamples.transit;

/**
 * This class implements a Point Of Sale terminal.
 *
 * It allows for crediting the on-card account and querying the current account
 * balance.
 */
public class POSTerminal extends Terminal {

    /**
     * Creates a Point Of Sale terminal.
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
    POSTerminal(String hostName, int hostPort, byte[] staticKeyData)
            throws Exception {
        super(hostName, hostPort, staticKeyData);
    }

    /**
     * Gets the on-card account balance.
     *
     * @throws Exception
     */
    private void getBalance() throws Exception {

        // Request Message: []

        byte[] requestMessage = new byte[0];

        // Response Message: [2-bytes Balance]

        byte[] responseMessage = processRequest(GET_BALANCE, requestMessage);

        if (responseMessage != null) {

            // Retrieve the balance
            short balance = getShort(responseMessage, 0);

            System.out.println("getBalance: [] => " + "[ " + balance + " ]");
        } else {

            System.out.println("getBalance: [] => " + "error");
        }
    }

    /**
     * Credits the on-card account.
     *
     * @param amount
     *            The credited amount
     * @throws Exception
     */
    private void credit(byte amount) throws Exception {

        // Request Message: [1-byte Credit Amount]

        byte[] requestMessage = new byte[1];

        requestMessage[0] = amount;

        // Response Message: []

        byte[] responseMessage = processRequest(CREDIT, requestMessage);

        if (responseMessage != null) {

            System.out.println("credit: [" + amount + "] => " + "OK");
        } else {

            System.out.println("credit: [" + amount + "] => " + "error");
        }
    }

    /**
     * Prints the usage.
     *
     */
    private static void usage() {
        commonUsage();
        System.out
                .println("<command list>: ([VERIFY <pin>]|[GET_BALANCE]|[CREDIT <credit amount>])*");
    }

    /**
     * Parses and runs the CLI command.
     *
     * @param args
     *            The CLI arguments: <command list>: ([VERIFY
     *            <pin>]|[GET_BALANCE]|[CREDIT <credit amount>])*
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        int i = parseCommonArgs(args);
        if (i <= 0) {
            usage();
            System.exit(3);
        }

        POSTerminal terminal = new POSTerminal(hostName, port, staticKeyData);

        terminal.powerUp();
        terminal.selectApplet();
        terminal.initializeSession();

        for (; i < args.length; i++) {
            if (args[i].equals("VERIFY")) {
                if (++i < args.length) {
                    byte[] pin = args[i].getBytes();
                    terminal.verifyPIN(pin);
                } else {
                    usage();
                    System.exit(3);
                }
            } else if (args[i].equals("GET_BALANCE")) {
                terminal.getBalance();
            } else if (args[i].equals("CREDIT")) {
                if (++i < args.length) {
                    byte creditAmount = new Byte(args[i]).byteValue();
                    terminal.credit(creditAmount);
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
