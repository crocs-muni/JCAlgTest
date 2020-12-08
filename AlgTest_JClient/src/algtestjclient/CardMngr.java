/*  
    Copyright (c) 2008-2014 Petr Svenda <petr@svenda.com>

     LICENSE TERMS

     The free distribution and use of this software in both source and binary
     form is allowed (with or without changes) provided that:

       1. distributions of this source code include the above copyright
          notice, this list of conditions and the following disclaimer;

       2. distributions in binary form include the above copyright
          notice, this list of conditions and the following disclaimer
          in the documentation and/or other associated materials;

       3. the copyright holder's name is not used to endorse products
          built using this software without specific written permission.

     ALTERNATIVELY, provided that this notice is retained in full, this product
     may be distributed under the terms of the GNU General Public License (GPL),
     in which case the provisions of the GPL apply INSTEAD OF those given above.

     DISCLAIMER

     This software is provided 'as is' with no explicit or implied warranties
     in respect of its properties, including, but not limited to, correctness
     and/or fitness for purpose.

    Please, report any bugs to author <petr@svenda.com>
/**/
package algtestjclient;

import algtest.AlgSupportTest;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import javax.smartcardio.*;
import algtest.Consts;
import algtest.JCAlgTestApplet;
import algtest.JCConsts;
import algtest.TestSettings;
import cardTools.SimulatedCardChannelLocal;
import cardTools.SimulatedCardTerminal;
import cardTools.Util;
import com.licel.jcardsim.io.CAD;
import com.licel.jcardsim.io.JavaxSmartCardInterface;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.HashMap;
import javacard.framework.AID;
import javacard.framework.ISO7816;


/**
 *
 * @author petrs
 */
public class CardMngr {
    //public static CardManager cardVirtManager = null;
    
    final static byte SUCCESS =                    (byte) 0xAA;
    
    // TODO: unification of errors
    final static int CANT_BE_MEASURED              = 256;   
    final static byte ILLEGAL_USE = (byte) 5;
    final static byte ILLEGAL_VALUE = (byte) 1;
    final static byte INVALID_INIT = (byte) 4;
    final static byte NO_SUCH_ALGORITHM = (byte) 3;
    final static byte UNINITIALIZED_KEY = (byte) 2;       

    public final int MAX_SERIOUS_PROBLEMS_IN_ROW = 2;
    
    /* Argument constants for choosing algorithm to test. */
    
    /* Arguments for choosing which AlgTest version to run. */
    public static final String ALGTEST_SINGLEPERAPDU = "AT_SINGLEPERAPDU";      // for 'New' AlgTest
    public static final String ALGTEST_PERFORMANCE = "AT_PERFORMANCE";          // for performance testing
    
    /* Argument 'ALL_ALGS' work with every class. */
    public static final String TEST_ALL_ALGORITHMS = "ALL_ALGS";
    
    /* Following arguments work with SinglePerApdu and MultiPerApdu. */
    public static final String TEST_CLASS_CIPHER = "CLASS_CIPHER";
    public static final String TEST_CLASS_CHECKSUM = "CLASS_CHECKSUM";
    public static final String TEST_CLASS_SIGNATURE = "CLASS_SIGNATURE";
    public static final String TEST_CLASS_KEYBUILDER = "CLASS_KEYBUILDER";
    public static final String TEST_CLASS_MESSAGEDIGEST = "CLASS_MESSAGEDIGEST";
    public static final String TEST_CLASS_RANDOMDATA = "CLASS_RANDOMDATA";
    public static final String TEST_CLASS_KEYAGREEMENT = "CLASS_KEYAGREEMENT";
    public static final String TEST_CLASS_KEYPAIR_ALG_RSA = "CLASS_KEYPAIR_ALG_RSA";
    public static final String TEST_CLASS_KEYPAIR_ALG_RSA_CRT = "CLASS_KEYPAIR_ALG_RSA_CRT";
    public static final String TEST_CLASS_KEYPAIR_ALG_DSA = "CLASS_KEYPAIR_ALG_DSA";
    public static final String TEST_CLASS_KEYPAIR_ALG_EC_F2M = "CLASS_KEYPAIR_ALG_EC_F2M";
    public static final String TEST_CLASS_KEYPAIR_ALG_EC_FP = "CLASS_KEYPAIR_ALG_EC_FP";
    
    /* Following arguments work with PerformanceTesting. */
    public static final String TEST_EEPROM = "EEPROM";
    public static final String TEST_RAM = "RAM";
    public static final String TEST_EXTENDEDAPDU = "EXTENDEDAPDU";
    public static final String TEST_RSAEXPONENT = "RSAEXPONENT";

    public static final int MAX_SUPP_ALG            = 240;    
    public static final byte SUPP_ALG_UNTOUCHED     = (byte) 0xf0;
    public final static byte SUPP_ALG_SUPPORTED     = (byte) 0x00;
    public static final short SUPP_ALG_SEPARATOR        = 0xff;
    public static final short SUPP_ALG_SMALL_SEPARATOR  = 0xb0;    
    public final static byte EXCEPTION_CODE_OFFSET  = (byte) 0;
    
    public static final byte ALGTEST_AID_LEN       = 9;
    public static final byte FORCE_TEST = 1;        // variable to force test of alg in given method
 
    CardTerminal m_terminal = null;
    CardChannel m_channel = null;
    Card m_card = null;
    public static String cardUploadersFolder = System.getProperty("user.dir")+File.separator+"!card_uploaders";
    
    public static final byte selectAppletLegacy[] = {
        (byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x00, (byte) 0x09, 
        (byte) 0x6D, (byte) 0x79, (byte) 0x70, (byte) 0x61, (byte) 0x63, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x31}; 
    
    public static final byte selectApplet[] = {
        (byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x00, (byte) 0x0a,
        (byte) 0x4a, (byte) 0x43, (byte) 0x41, (byte) 0x6c, (byte) 0x67, (byte) 0x54, (byte) 0x65, (byte) 0x73, (byte) 0x74, (byte) 0x31};

    // ETSI 102 221 power consumption rules allow to consume only up to 4mA at 1.8 Volt. Up to 10mA may be consumed if ADFusim is selected.
    // ADFusim AID == 00 A4 04 05 07 A0 00 00 00 87 10 02
    public static final byte selectADFusim[] = { 
        (byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x05, (byte) 0x07,
        (byte) 0xA0, (byte)0x00, (byte) 0x00, (byte) 0x00, (byte) 0x87, (byte) 0x10, (byte) 0x02};
    
    public static final String helpString = "This program can be used with following parameters:\r\n"
            + ALGTEST_SINGLEPERAPDU + " -> for using AlgTest with single test per APDU command\r\n"
            + ALGTEST_PERFORMANCE + " -> for using AlgTest with performance testing\r\n"
            + TEST_ALL_ALGORITHMS + " -> for testing all algorithms without further arguments necessary\r\n"
            + TEST_CLASS_CIPHER + " -> for testing class Cipher\r\n"
            + TEST_CLASS_CHECKSUM + " -> for testing class Checksum\r\n"
            + TEST_CLASS_SIGNATURE + " -> for testing class Signature\r\n"
            + TEST_CLASS_KEYBUILDER + " -> for testing class KeyBuilder\r\n"
            + TEST_CLASS_MESSAGEDIGEST + " -> for testing class Message Digest\r\n"
            + TEST_CLASS_RANDOMDATA + " -> for testing class Random Data\r\n"
            + TEST_CLASS_KEYAGREEMENT + " -> for testing class Key Agreement\r\n"
            + TEST_CLASS_KEYPAIR_ALG_RSA + " -> for testing class KeyPair algorithm RSA\r\n"
            + TEST_CLASS_KEYPAIR_ALG_RSA_CRT + " -> for testing class KeyPair algorithm RSA CRT\r\n"
            + TEST_CLASS_KEYPAIR_ALG_DSA + " -> for testing class KeyPair algorithm DSA\r\n"
            + TEST_CLASS_KEYPAIR_ALG_EC_F2M + " -> for testing class KeyPair algorithm EC F2M\r\n"
            + TEST_CLASS_KEYPAIR_ALG_EC_FP + " -> for testing class KeyPair algorithm EC FP\r\n"
            + TEST_EEPROM + " ->  for testing available EEPROM memmory\r\n"
            + TEST_RAM + " -> for testing available RAM memmory\r\n"
            + TEST_EXTENDEDAPDU + " -> for testing extended APDU support\r\n"
            + TEST_RSAEXPONENT + " -> for testing varialbe public RSA exponent.\r\n";
    
    public static final String paramList =
            ALGTEST_SINGLEPERAPDU + "\r\n"
            + ALGTEST_PERFORMANCE + "\r\n\r\n"
            + TEST_ALL_ALGORITHMS + "\r\n\r\n"
            + TEST_CLASS_CIPHER + "\r\n"
            + TEST_CLASS_CHECKSUM + "\r\n"
            + TEST_CLASS_SIGNATURE + "\r\n"
            + TEST_CLASS_KEYBUILDER + "\r\n"
            + TEST_CLASS_MESSAGEDIGEST + "\r\n"
            + TEST_CLASS_RANDOMDATA + "\r\n"
            + TEST_CLASS_KEYAGREEMENT + "\r\n"
            + TEST_CLASS_KEYPAIR_ALG_RSA + "\r\n"
            + TEST_CLASS_KEYPAIR_ALG_RSA_CRT + "\r\n"
            + TEST_CLASS_KEYPAIR_ALG_DSA + "\r\n"
            + TEST_CLASS_KEYPAIR_ALG_EC_F2M + "\r\n"
            + TEST_CLASS_KEYPAIR_ALG_EC_FP + "\r\n\r\n"
            + TEST_EEPROM + "\r\n"
            + TEST_RAM + "\r\n"
            + TEST_EXTENDEDAPDU + "\r\n"
            + TEST_RSAEXPONENT + "\r\n";

    public static final byte OFFSET_CLA = 0x00;
    public static final byte OFFSET_INS = 0x01;
    public static final byte OFFSET_P1 = 0x02;
    public static final byte OFFSET_P2 = 0x03;
    public static final byte OFFSET_LC = 0x04;
    public static final byte OFFSET_DATA = 0x05;
    public static final byte HEADER_LENGTH = 0x05;
    public static final short EXTENDED_APDU_TEST_LENGTH = 0x400; // 1024
    
    public final static int STAT_OK = 0;
    public final static int STAT_DATA_CORRUPTED = 10;
    
    public StringBuilder atr = new StringBuilder(); 
    public StringBuilder reader = new StringBuilder();
    public StringBuilder protocol = new StringBuilder();
    public boolean bHighPowerMode = false; // If true, high power mode was enabled using ETSI 102 221 ADFusim select == 00 A4 04 05 07 A0 00 00 00 87 10 02
    
    /* ATR of jCardSim. */
    static final String SIMULATOR_ATR = "3B FA 18 00 00 81 31 FE 45 4A 43 4F 50 33 31 56 32 33 32 98";
    
    /* CLOCKS_PER_SEC also used in 'PerformanceTesting.java' */
    public static final int CLOCKS_PER_SEC = 1000;
    
    static DirtyLogger m_SystemOutLogger = null;
    public CardMngr(DirtyLogger logger) {
        m_SystemOutLogger = logger;
    }
    
    public static void PrintHelp () throws FileNotFoundException, IOException{
        m_SystemOutLogger.println(CardMngr.helpString);
        
        m_SystemOutLogger.println("Do you want to print supported parameters for AlgTest to separate file? 1 = YES, 0 = NO/r/n");
        Scanner sc = new Scanner(System.in);
        int answ = sc.nextInt();
        m_SystemOutLogger.println(String.format("%d", answ));
        if (answ == 1){
            FileOutputStream file = new FileOutputStream("AlgTest_supported_parameters.txt");
            file.write(CardMngr.paramList.getBytes());
            m_SystemOutLogger.println("List of supported parameters for AlgTest created in project folder.");
        }
    }  
    
    public String getTerminalName() {
        if (m_terminal != null) {
            return m_terminal.getName();
        }
        return "No terminal found";    
    }
    public String getATR() {
        if (m_card != null) {
           return bytesToHex(m_card.getATR().getBytes());
        } else {
            return "No card available";
        }
    }    
    public String getProtocol() {
        if (m_card != null) {
            return m_card.getProtocol();
        } else {
            return "Not connected yet";
        }
    }    
    public FileOutputStream establishConnection() throws Exception{
        return establishConnection("", "", null);
    }
    public FileOutputStream establishConnection(String cardName, String testInfo, CardTerminal selectedTerminal) throws Exception{
        boolean bConnected = false;
        // Connnect to targer card to obtain information
        reader.setLength(0);
        atr.setLength(0);
        protocol.setLength(0);
        if (selectedTerminal != null) {
            bConnected = ConnectToCard(selectedTerminal, reader, atr, protocol);            
        } 
        else {
            bConnected = ConnectToFirstCard(reader, atr, protocol);            
        }
        if (bConnected) {
            String message = "";
            if (atr.toString().equals("")){atr.append(SIMULATOR_ATR + " (provided_by_Licel_jCardSim)");} // if atr == "" it means that simulator is running and thus simulator atr must be used
            m_SystemOutLogger.println("ATR: " + atr);
            String fileName = testInfo + "_" + atr + ".csv";
            fileName = fileName.replace(":", "");
            fileName = fileName.replace(" ", "_");
            
            FileOutputStream file = new FileOutputStream(fileName);
            
            StringBuilder value = new StringBuilder();
            
            message += "INFO: This file was generated by AlgTest utility. See http://www.fi.muni.cz/~xsvenda/jcsupport.html for more results, source codes and other details.;\r\n";
            m_SystemOutLogger.println(message); file.write(message.getBytes());                
                
            message = "Tested and provided by; insert your name please.;\r\n";
            m_SystemOutLogger.println(message); file.write(message.getBytes());                

            message = "Execution date/time; ";
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            message += dateFormat.format(date) + "\r\n";
            m_SystemOutLogger.println(message); file.write(message.getBytes()); 
            
            message = "AlgTestJClient version; " + AlgTestJClient.ALGTEST_JCLIENT_VERSION + "\r\n";
            m_SystemOutLogger.println(message); file.write(message.getBytes());    

            value.setLength(0);
            if (GetAppletVersion(value) == CardMngr.STAT_OK) {
                message = "AlgTest applet version; " + value + "\r\n";
                m_SystemOutLogger.println(message); file.write(message.getBytes()); 
            }
            else { 
                message = "\nERROR: GetAppletVersion fail"; 
                m_SystemOutLogger.println(message); file.write(message.getBytes());
            }
        
            message = "Used reader; " + reader + "\r\n";
            m_SystemOutLogger.println(message); file.write(message.getBytes());
            message = "Card ATR; " + atr + "\r\n";
            m_SystemOutLogger.println(message); file.write(message.getBytes());
            message = "Card name; " + cardName + "\r\n";
            m_SystemOutLogger.println(message); file.write(message.getBytes());
            message = "Card provider; please insert link/description of shop where card was bought\r\n";
            m_SystemOutLogger.println(message); file.write(message.getBytes());
            message = "Used protocol; " + protocol + "\r\n";
            m_SystemOutLogger.println(message); file.write(message.getBytes());
            message = String.format("High-power mode supported (relevant only to SIM cards according to ETSI 102 221); %s\r\n", bHighPowerMode ? "yes" : "no");
            m_SystemOutLogger.println(message); file.write(message.getBytes());

            
            m_SystemOutLogger.println("\n\n#########################");
            m_SystemOutLogger.println("\nJCSystem information");
            if (GetJCSystemInfo(value, file) == CardMngr.STAT_OK) {}
            else { m_SystemOutLogger.println("\nERROR: GetJCSystemInfo fail"); }
            
            m_SystemOutLogger.println("\n\n#########################");
            m_SystemOutLogger.println("\nGlobalPlatform information");
            if (GetGPInfo(value, file) == CardMngr.STAT_OK) {}
            else { m_SystemOutLogger.println("\nERROR: GetGPInfo fail"); }       
            
            // Connnect to target card again 
            if (selectedTerminal != null) {
                bConnected = ConnectToCard(selectedTerminal, reader, atr, protocol);            
            } 
            else {
                bConnected = ConnectToFirstCard(reader, atr, protocol);            
            }        
            
            return file; // if succesfull, returns open file for AlgTest output
        }
        
        return null;    // returns 'null' in case of error
    }
    public boolean ConnectToFirstCard() throws Exception {
        StringBuilder selectedReader = new StringBuilder();
        StringBuilder selectedATR = new StringBuilder();
        StringBuilder usedProtocol = new StringBuilder();
        return ConnectToFirstCard(selectedReader, selectedATR, usedProtocol);
    }
    
    public boolean ConnectToFirstCard(StringBuilder selectedReader, StringBuilder selectedATR, StringBuilder usedProtocol) throws Exception {
        boolean cardFound = false;       
        
        CardTerminal targetReader = null;
        // TRY ALL READERS, FIND FIRST SELECTABLE
        List<CardTerminal> terminalList = GetReaderList();
        if (terminalList.isEmpty()) {
            m_SystemOutLogger.println("No terminals found");
        }
        else {
            //List numbers of Card readers        
            for (int i = 0; i < terminalList.size(); i++) {
                m_SystemOutLogger.println(i + " : " + terminalList.get(i));
                targetReader = (CardTerminal) terminalList.get(i);
                if (targetReader.isCardPresent()) {     
                    // Connect to this one
                    break;
                }
            }
        }

        if (targetReader != null) {
            cardFound = ConnectToCard(targetReader, selectedReader, selectedATR, usedProtocol);
        }
        else {
            cardFound = false;
        }
        
        return cardFound;        
    }
    
    private CardChannel ConnectJCardSimLocalSimulator(Class appletClass, byte[] appletId, byte[] installData) throws Exception {
        System.setProperty("com.licel.jcardsim.terminal.type", "2");
        CAD cad = new CAD(System.getProperties());
        JavaxSmartCardInterface simulator = (JavaxSmartCardInterface) cad.getCardInterface();
        if (installData == null) {
            installData = new byte[0];
        }
        AID appletAID = new AID(appletId, (short) 0, (byte) appletId.length);

        AID appletAIDRes = simulator.installApplet(appletAID, appletClass, installData, (short) 0, (byte) installData.length);
        simulator.selectApplet(appletAID);

        return new SimulatedCardChannelLocal(simulator);
    }    
    
    public boolean ConnectToCard() throws Exception {
        StringBuilder selectedReader = new StringBuilder();
        StringBuilder selectedATR = new StringBuilder();
        StringBuilder usedProtocol = new StringBuilder();
        reader.setLength(0);
        atr.setLength(0);
        protocol.setLength(0);
        return ConnectToCard(m_terminal, selectedReader, selectedATR, usedProtocol);
    }
    
    public boolean ConnectToCard(CardTerminal targetReader, StringBuilder selectedReader, StringBuilder selectedATR, StringBuilder usedProtocol) throws Exception {
        boolean cardFound = false;        
        bHighPowerMode = false;
        
        m_terminal = targetReader;

        if (targetReader instanceof SimulatedCardTerminal) {
            m_SystemOutLogger.print("\n\n!!!!!!!!!!!!!!!!!!!!!!!!\n\t\tRUNNING WITH jCardSim SIMULATOR\n!!!!!!!!!!!!!!!!!!!!!!!! \n\n");
            m_channel = ConnectJCardSimLocalSimulator(JCAlgTestApplet.class, selectApplet, new byte[1]); // bogus install data used
            if (m_channel != null) {
                cardFound = true;
            }
        }
        else {
            if (m_terminal.isCardPresent()) {
                for (int maxAttempts = 2; maxAttempts > 0; ) {
                    maxAttempts--;
                    m_card = null;
                    try {
                        m_card = m_terminal.connect("*");
                    }
                    catch (javax.smartcardio.CardException e) {
                        m_SystemOutLogger.print("Fail to connect(*), trying connect(T=0)...");
                        m_card = m_terminal.connect("T=0");
                        m_SystemOutLogger.println("done.");
                    }
                    if (m_card != null) {
                        m_SystemOutLogger.println("card: " + m_card);
                        m_channel = m_card.getBasicChannel();
                        m_SystemOutLogger.println("Card Channel: " + m_channel.getChannelNumber());

                        //reset the card
                        ATR atr = m_card.getATR();
                        m_SystemOutLogger.println(bytesToHex(atr.getBytes()));

                        /* Supressed as some cards stops responding till reset after this command   
                        // Attempt to allow for high-power mode by selecting specific applet called 
                        try {
                            ResponseAPDU resp2 = sendAPDU(selectADFusim);
                            if (resp2.getSW() == 0x9000) {
                                bHighPowerMode = true;
                            }
                        }
                        catch (Exception e) {
                            m_SystemOutLogger.print("Exception when testing high-power mode");
                        }
                        */

                        // SELECT APPLET
                        cardFound = false;
                        ResponseAPDU resp = sendAPDU(selectApplet);
                        if (resp.getSW() == 0x9000) {
                            cardFound = true;
                        } else {
                            m_SystemOutLogger.println("JCAlgTest applet with new AID not found - trying legacy AID...");
                            resp = sendAPDU(selectAppletLegacy);
                            if (resp.getSW() == 0x9000) {
                                cardFound = true;
                            } else {
                                m_SystemOutLogger.println("No JCAlgTest applet found");
                            }
                        }
                    }

                    if (selectedATR != null) { selectedATR.append(getATR()); }
                    if (selectedReader != null) { selectedReader.append(getTerminalName()); }
                    if (usedProtocol != null) { usedProtocol.append(getProtocol()); }

                    if (cardFound) {
                        return true;
                    }
                }
            }
        }
        return cardFound;        
    }    

    public void DisconnectFromCard() throws Exception {
        if (m_channel != null) {
            m_channel.getCard().disconnect(false); // Disconnect from the card
        }
    }

    public static List<CardTerminal> GetReaderList() {
        try {
            // Try to get list of readers first via default SunPCSC provider. 
            // If no readers are detected then try via JNA
            TerminalFactory termFactory = TerminalFactory.getDefault(); 
            List<CardTerminal> readersList = null;
            boolean bReadersFound = false;
            try {
                CardTerminals ct = termFactory.terminals();
                readersList = ct.list();
                if (readersList.size() > 0) {
                    m_SystemOutLogger.println("Total " + readersList.size() + " readers detected via " + termFactory.getProvider().getName());
                    bReadersFound = true;
                }
            }
            catch(CardException ex) {
                m_SystemOutLogger.println("Exception : " + ex);
            }
            
            if (!bReadersFound) {
                m_SystemOutLogger.println("No readers detected via SunPCSC, trying JNA2PCSC...");
                termFactory = TerminalManager.getTerminalFactory(true);
                CardTerminals ct = termFactory.terminals();
                readersList = ct.list();
            }
            
            // Print all detected readers and terminals
            m_SystemOutLogger.println("Following readers are available via '" + termFactory.getProvider().getName() + "' provider:");
            if (readersList.isEmpty()) {
                m_SystemOutLogger.println("No readers");
            }
            else {
                for (CardTerminal terminal : readersList) {
                    m_SystemOutLogger.println((terminal.isCardPresent() ? "[*] " : "[ ] ") + terminal.getName());
                }
            }
            return readersList;
        } catch (Exception ex) {
            m_SystemOutLogger.println("Exception : " + ex);
            return new ArrayList<>();
        }
    }
    
    public static List<CardTerminal> GetReaderList(boolean bTerminalWithCardsOnly) {
        List<CardTerminal> readersList = new ArrayList<>();
        List<CardTerminal> readersListAll = GetReaderList();
        if (bTerminalWithCardsOnly) {
            try {
                for (CardTerminal terminal : readersListAll) {
                    if (terminal.isCardPresent()) {
                        readersList.add(terminal);
                    }
                }
                
                return readersList;
            } catch (CardException ex) {
                m_SystemOutLogger.println("Exception : " + ex);
                return null;
            }
        }
        else { return readersListAll; }
    }    

    /**
     * Method which will send bytes to on-card application using smartcardio interface.
     * @param apdu Byte array containing data to be send to card.
     * @return Returns card response as ResponseAPDU.
     * @throws Exception
     */
    public ResponseAPDU sendAPDU(byte apdu[]) throws Exception {
        CommandAPDU commandAPDU = new CommandAPDU(apdu);
        ResponseAPDU responseAPDU = null;
        m_SystemOutLogger.println(">>>>");
        m_SystemOutLogger.println(commandAPDU.toString());

        m_SystemOutLogger.println(bytesToHex(commandAPDU.getBytes()));
        long elapsedCard = -System.currentTimeMillis();
        responseAPDU = m_channel.transmit(commandAPDU);
        elapsedCard += System.currentTimeMillis();
        
        m_SystemOutLogger.println(String.format("%s, elapsed=%d ms", responseAPDU.toString(), elapsedCard));
        m_SystemOutLogger.println(bytesToHex(responseAPDU.getBytes()));

        if (responseAPDU.getSW1() == (byte) 0x61) {
            CommandAPDU apduToSend = new CommandAPDU((byte) 0x00,
                    (byte) 0xC0, (byte) 0x00, (byte) 0x00,
                    responseAPDU.getSW2());

            responseAPDU = m_channel.transmit(apduToSend);
            m_SystemOutLogger.println(bytesToHex(responseAPDU.getBytes()));
        }

        m_SystemOutLogger.println("<<<<");

        return (responseAPDU);
    }

    public static String byteToHex(byte data) {
        StringBuilder  buf = new StringBuilder ();
        buf.append(toHexChar((data >>> 4) & 0x0F));
        buf.append(toHexChar(data & 0x0F));
        return buf.toString();
    }

    public static char toHexChar(int i) {
        if ((0 <= i) && (i <= 9)) {
            return (char) ('0' + i);
        } else {
            return (char) ('a' + (i - 10));
        }
    }

    public static String bytesToHex(byte[] data) {
        return bytesToHex(data, true);            
    }    
    public static String bytesToHex(byte[] data, boolean bInsertSpace) {
        StringBuilder  buf = new StringBuilder ();
        for (int i = 0; i < data.length; i++) {
            buf.append(byteToHex(data[i]));
            if (i != data.length - 1) { 
                if (bInsertSpace) { buf.append(" "); }
            }
        }
        return (buf.toString());
    }
        
/*
    
    // Parse algorithm name and version of JC which introduced it
    //algParts[0] == algorithm name
    //algParts[1] == introducing version
    //algParts[2] == should be this item included in output? 1/0
    public String GetAlgorithmName(String algorithmInfoString) {
        String[] algParts = algorithmInfoString.split("#");
        return algParts[0];
    }
     public String GetAlgorithmIntroductionVersion(String algorithmInfoString) {
        String[] algParts = algorithmInfoString.split("#");
        String algorithmVersion = (algParts.length > 1) ? algParts[1] : "";
        return algorithmVersion;
    }
    public boolean ShouldBeIncludedInOutput(String algorithmInfoString) {
        String[] algParts = algorithmInfoString.split("#");
        String includeInfo = (algParts.length > 2) ? algParts[2] : "1";
        return Integer.decode(includeInfo) != 0;
    }
*/                                
    public int GetAppletVersion(StringBuilder pValue) throws Exception {
        int         status = STAT_OK;
    
	// Prepare test memory apdu
        byte apdu[] = new byte[HEADER_LENGTH + 1];
        apdu[OFFSET_CLA] = Consts.CLA_CARD_ALGTEST;
        apdu[OFFSET_INS] = Consts.INS_CARD_GETVERSION;
        apdu[OFFSET_P1] = 0x00;
        apdu[OFFSET_P2] = 0x00;
        apdu[OFFSET_LC] = 0x01;
        apdu[OFFSET_DATA] = 0x00;

        try {
            ResponseAPDU resp = sendAPDU(apdu);
            if (resp.getSW() != 0x9000) {
                m_SystemOutLogger.println("Fail to obtain Applet version");
                pValue.append("error");
            } else {
                byte temp[] = resp.getData();
                char version[] = new char[temp.length]; 
                for (int i = 0; i < temp.length; i++) { version[i] = (char) temp[i]; }
                pValue.append(version);
             }        
        }
        catch (Exception ex) {
            m_SystemOutLogger.println("Fail to obtain Applet version");
            pValue.append("error");
        }
                
	return status;
    }
    
    public int GetJCSystemInfo(StringBuilder pValue, FileOutputStream pFile) throws Exception {
        int         status = STAT_OK;
        long        elapsedCard = 0;
    
	// Prepare test memory apdu
        byte apdu[] = new byte[HEADER_LENGTH+1];
        apdu[OFFSET_CLA] = Consts.CLA_CARD_ALGTEST;
        apdu[OFFSET_INS] = Consts.INS_CARD_JCSYSTEM_INFO;
        //apdu[OFFSET_P1] = 0x00;
        apdu[OFFSET_P1] = AlgSupportTest.RETURN_INSTALL_TIME_MEMORY_SIZE;
        apdu[OFFSET_P2] = 0x00;
        apdu[OFFSET_LC] = 0x01;
        apdu[OFFSET_DATA] = 0x01;

        elapsedCard -= System.currentTimeMillis();
        try {
            ResponseAPDU resp = sendAPDU(apdu);
            if (resp.getSW() != 0x9000) {
                m_SystemOutLogger.println("Fail to obtain JCSystemInfo");
            } else {
                // SET READ DATA
                byte temp[] = resp.getData();

                // SAVE TIME OF CARD RESPONSE
                elapsedCard += System.currentTimeMillis();
                String elTimeStr;
                // OUTPUT REQUIRED TIME WHEN PARTITIONED CHECk WAS PERFORMED (NOTMULTIPLE ALGORITHMS IN SINGLE RUN)
                elTimeStr = String.valueOf((double) elapsedCard / (float) CLOCKS_PER_SEC);

                int versionMajor = temp[0];
                int versionMinor = temp[1];
                int bDeletionSupported = temp[2];
                int eepromSize = (temp[3] << 8) + (temp[4] & 0xff);
                int ramResetSize = (temp[5] << 8) + (temp[6] & 0xff);
                int ramDeselectSize = (temp[7] << 8) + (temp[8] & 0xff);
                int maxCommitSize = (temp[9] << 8) + (temp[10] & 0xff);

                int apduInBlockSize = -1;
                int apduOutBlockSize = -1;
                int apduProtocol = -1;
                int apduNAD = -1;
                if (temp.length > 11) { // info about blocks is available
                    apduInBlockSize = (temp[11] << 8) + (temp[12] & 0xff);
                    apduOutBlockSize = (temp[13] << 8) + (temp[14] & 0xff);
                    apduProtocol = temp[15];
                    apduNAD = temp[16];
                }
                if (temp.length > 17) { // more detailed info about memory is available
                    int offset = 17;
                    eepromSize = Util.getInt(temp, offset); offset += 4;
                    // RAM values are unused now, as we already have them and are measured before all allocations
                    int unused_ramDeselectSize2 = Util.getInt(temp, offset); offset += 4;
                    int unused_ramResetSize2 = Util.getInt(temp, offset); offset += 4;
                }



                String message;
                message = String.format("\r\n%1s;%d.%d;", Utils.GetAlgorithmName(SingleModeTest.JCSYSTEM_STR[1]), versionMajor, versionMinor); 
                m_SystemOutLogger.println(message);
                pFile.write(message.getBytes());
                pValue.append(message);
                message = String.format("\r\n%s;%s;", Utils.GetAlgorithmName(SingleModeTest.JCSYSTEM_STR[2]),(bDeletionSupported != 0) ? "yes" : "no"); 

                m_SystemOutLogger.println(message);
                pFile.write(message.getBytes());
                pValue.append(message);
                message = String.format("\r\n%s;%s%dB;", Utils.GetAlgorithmName(SingleModeTest.JCSYSTEM_STR[3]),(eepromSize == 32767) ? ">" : "", eepromSize); 
                m_SystemOutLogger.println(message);
                pFile.write(message.getBytes());
                pValue.append(message);
                message = String.format("\r\n%s;%s%dB;", Utils.GetAlgorithmName(SingleModeTest.JCSYSTEM_STR[4]),(ramResetSize == 32767) ? ">" : "", ramResetSize); 
                m_SystemOutLogger.println(message);
                pFile.write(message.getBytes());
                pValue.append(message);
                message = String.format("\r\n%s;%s%dB;\n", Utils.GetAlgorithmName(SingleModeTest.JCSYSTEM_STR[5]),(ramDeselectSize == 32767) ? ">" : "", ramDeselectSize); 
                m_SystemOutLogger.println(message);
                message = String.format("\r\n%s;%dB;", Utils.GetAlgorithmName(SingleModeTest.JCSYSTEM_STR[6]), maxCommitSize); 
                m_SystemOutLogger.println(message);
                pFile.write(message.getBytes());
                pValue.append(message);
                
                // APDU properties    
                message = String.format("\r\n%s;%dB;", Utils.GetAlgorithmName(SingleModeTest.JCSYSTEM_STR[7]), apduInBlockSize);
                m_SystemOutLogger.println(message);
                pFile.write(message.getBytes());
                pValue.append(message);
                message = String.format("\r\n%s;%dB;", Utils.GetAlgorithmName(SingleModeTest.JCSYSTEM_STR[8]), apduOutBlockSize);
                m_SystemOutLogger.println(message);
                pFile.write(message.getBytes());
                pValue.append(message);
                message = String.format("\r\n%s;%dB;", Utils.GetAlgorithmName(SingleModeTest.JCSYSTEM_STR[9]), apduProtocol);
                m_SystemOutLogger.println(message);
                pFile.write(message.getBytes());
                pValue.append(message);
                message = String.format("\r\n%s;%dB;", Utils.GetAlgorithmName(SingleModeTest.JCSYSTEM_STR[10]), apduNAD);
                m_SystemOutLogger.println(message);
                pFile.write(message.getBytes());
                pValue.append(message);
                
                
                message = "\r\n";

                pFile.write(message.getBytes());
                pValue.append(message);
            }        
        }
        catch (Exception ex) {
            m_SystemOutLogger.println("Fail to obtain JCSystemInfo");
            pValue.append("error");
        }
                      
                
	return status;
    }
    public static int intCode(short code) {
        int intCode = code & 0xffff;
        if (intCode < 0) {
            assert (intCode >= 0);
            intCode = -1;
        }
        return intCode;
    }    
    // Functions for CPLC taken and modified from https://github.com/martinpaljak/GlobalPlatformPro 
    private static final byte CLA_GP = (byte) 0x80;     
    private static final byte ISO7816_INS_GET_DATA = (byte) 0xCA;   
    private static final byte[] SELECT_CM = {(byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x00, (byte) 0x00};
    private static final byte[] FETCH_GP_CPLC_APDU = {CLA_GP, ISO7816_INS_GET_DATA, (byte) 0x9F, (byte) 0x7F, (byte) 0x00};
    private static final byte[] FETCH_ISO_CPLC_APDU = {ISO7816.CLA_ISO7816, ISO7816_INS_GET_DATA, (byte) 0x9F, (byte) 0x7F, (byte) 0x00};
    private static final byte[] FETCH_GP_CARDDATA_APDU = {CLA_GP, ISO7816_INS_GET_DATA, (byte) 0x00, (byte) 0x66, (byte) 0x00};
    public byte[] fetchCPLC() throws Exception {
        ResponseAPDU resp = sendAPDU(SELECT_CM);
        // Try CPLC via GP 
        resp = sendAPDU(FETCH_GP_CPLC_APDU);
        // If GP CLA fails, try with ISO
        if (resp.getSW() == intCode(ISO7816.SW_CLA_NOT_SUPPORTED)) {
            resp = sendAPDU(FETCH_ISO_CPLC_APDU);
        }
        if (resp.getSW() == intCode(ISO7816.SW_NO_ERROR)) {
            return resp.getData();
        } 
        return null;
    } 
    public byte[] fetchCardData() throws Exception {
        ResponseAPDU resp = sendAPDU(SELECT_CM);
        // Try CardData via GP 
        resp = sendAPDU(FETCH_GP_CARDDATA_APDU);

        if (resp.getSW() == intCode(ISO7816.SW_NO_ERROR)) {
            return resp.getData();
        } 
        return null;
    } 
    
    public static final class CPLC {
            public enum Field {
                    ICFabricator,
                    ICType,
                    OperatingSystemID,
                    OperatingSystemReleaseDate,
                    OperatingSystemReleaseLevel,
                    ICFabricationDate,
                    ICSerialNumber,
                    ICBatchIdentifier,
                    ICModuleFabricator,
                    ICModulePackagingDate,
                    ICCManufacturer,
                    ICEmbeddingDate,
                    ICPrePersonalizer,
                    ICPrePersonalizationEquipmentDate,
                    ICPrePersonalizationEquipmentID,
                    ICPersonalizer,
                    ICPersonalizationDate,
                    ICPersonalizationEquipmentID
            };
            private HashMap<Field, byte[]> m_values = null;

            public CPLC(byte [] data) {
                    if (data == null) {
                            return;
                    }
                    if (data.length < 3 || data[2] != 0x2A)
                            throw new IllegalArgumentException("CPLC must be 0x2A bytes long");
                    //offset = TLVUtils.skipTag(data, offset, (short)0x9F7F);
                    short offset = 3;
                    m_values = new HashMap<>();
                    m_values.put(Field.ICFabricator, Arrays.copyOfRange(data, offset, offset + 2)); offset += 2;
                    m_values.put(Field.ICType, Arrays.copyOfRange(data, offset, offset + 2)); offset += 2;
                    m_values.put(Field.OperatingSystemID, Arrays.copyOfRange(data, offset, offset + 2)); offset += 2;
                    m_values.put(Field.OperatingSystemReleaseDate, Arrays.copyOfRange(data, offset, offset + 2)); offset += 2;
                    m_values.put(Field.OperatingSystemReleaseLevel, Arrays.copyOfRange(data, offset, offset + 2)); offset += 2;
                    m_values.put(Field.ICFabricationDate, Arrays.copyOfRange(data, offset, offset + 2)); offset += 2;
                    m_values.put(Field.ICSerialNumber, Arrays.copyOfRange(data, offset, offset + 4)); offset += 4;
                    m_values.put(Field.ICBatchIdentifier, Arrays.copyOfRange(data, offset, offset + 2)); offset += 2;
                    m_values.put(Field.ICModuleFabricator, Arrays.copyOfRange(data, offset, offset + 2)); offset += 2;
                    m_values.put(Field.ICModulePackagingDate, Arrays.copyOfRange(data, offset, offset + 2)); offset += 2;
                    m_values.put(Field.ICCManufacturer, Arrays.copyOfRange(data, offset, offset + 2)); offset += 2;
                    m_values.put(Field.ICEmbeddingDate, Arrays.copyOfRange(data, offset, offset + 2)); offset += 2;
                    m_values.put(Field.ICPrePersonalizer, Arrays.copyOfRange(data, offset, offset + 2)); offset += 2;
                    m_values.put(Field.ICPrePersonalizationEquipmentDate, Arrays.copyOfRange(data, offset, offset + 2)); offset += 2;
                    m_values.put(Field.ICPrePersonalizationEquipmentID, Arrays.copyOfRange(data, offset, offset + 4)); offset += 4;
                    m_values.put(Field.ICPersonalizer, Arrays.copyOfRange(data, offset, offset + 2)); offset += 2;
                    m_values.put(Field.ICPersonalizationDate, Arrays.copyOfRange(data, offset, offset + 2)); offset += 2;
                    m_values.put(Field.ICPersonalizationEquipmentID, Arrays.copyOfRange(data, offset, offset + 4)); offset += 4;
            }
            
            public HashMap<Field, byte[]> values() {
                return m_values;
            }
    }    
    
    public void PrintCPLCInfo(StringBuilder pValue, FileOutputStream pFile, byte[] cplcData) throws IOException {
        String  message = "";
        
        message = "\r\nCPLC; " + bytesToHex(cplcData);
        m_SystemOutLogger.println(message);
        pFile.write(message.getBytes());
        pValue.append(message);            
        
        
        CPLC cplc = new CPLC(cplcData);

        HashMap<CPLC.Field, byte[]> cplValues = cplc.values();
        
        for (CPLC.Field f : CPLC.Field.values()) {
            byte[] value = (byte[]) cplValues.get(f);
            
            switch (f) {
                case ICFabricationDate: {
                    message = "\r\nCPLC." + f.name() + ";" + bytesToHex(value, false) + ";(Y DDD) date in that year";
                    break;
                }
                case ICFabricator: {
                    String id = bytesToHex(value, false);
                    String fabricatorName = "unknown";
                    if (id.equals("3060")) { fabricatorName = "Renesas"; }
                    if (id.equals("4090")) { fabricatorName = "Infineon"; }
                    if (id.equals("4180")) { fabricatorName = "Atmel"; }
                    if (id.equals("4250")) { fabricatorName = "Samsung"; }
                    if (id.equals("4790")) { fabricatorName = "NXP"; }

                    message = "\r\nCPLC." + f.name() + ";" + bytesToHex(value, false) + ";" + fabricatorName;
                    break;
                }
                default: {
                    message = "\r\nCPLC." + f.name() + ";" + bytesToHex(value, false);
                    break;
                }
            }
            m_SystemOutLogger.println(message);
            pFile.write(message.getBytes());
            pValue.append(message);            
        }            

        message += "\r\n";

        pFile.write(message.getBytes());
        pValue.append(message);
    }
    public int GetGPInfo(StringBuilder pValue, FileOutputStream pFile) throws Exception {
        int         status = STAT_OK;

        // CPLC
        try {
            byte[] cplcData = fetchCPLC();
            if (cplcData == null) {
                m_SystemOutLogger.println("Fail to obtain CPLC info");
                String message = "CPLC; failed to obtain CPLC";
                pFile.write(message.getBytes());
                pValue.append(message);
            } 
            else {
                PrintCPLCInfo(pValue, pFile, cplcData);
            }
        }        
        catch (Exception ex) {
            m_SystemOutLogger.println("Fail to obtain GPInfo - CPLC");
            pValue.append("error");
        }
/*        
        // CardData
        try {
            byte[] cardData = fetchCardData();
            if (cardData == null) {
                m_logger.println("Fail to obtain cardData info");
            } 
            else {
                PrintGPInfo(pValue, pFile, cardData);
            }
        }        
        catch (Exception ex) {
            m_logger.println("Fail to obtain GPInfo - cardData");
            pValue.append("error");
        }
/*        
        // CardData
        try {
            byte[] cardData = fetchCardData();
            if (cardData == null) {
                m_logger.println("Fail to obtain cardData info");
            } 
            else {
                PrintGPInfo(pValue, pFile, cardData);
            }
        }        
        catch (Exception ex) {
            m_logger.println("Fail to obtain GPInfo - cardData");
            pValue.append("error");
        }
*/                      
                
	return status;
    }    

    public int TestVariableRSAPublicExponentSupport(StringBuilder pValue, FileOutputStream pFile, byte algPartP2) throws Exception {
        int         status = STAT_OK;
        
        byte apdu[] = new byte[HEADER_LENGTH];
        apdu[OFFSET_CLA] = Consts.CLA_CARD_ALGTEST;
        apdu[OFFSET_INS] = Consts.INS_CARD_TESTRSAEXPONENTSET;
        apdu[OFFSET_P1] = 0x00;
        apdu[OFFSET_P2] = 0x00;
        apdu[OFFSET_LC] = 0x00;
            
        String message;
        message = "\r\nSupport for variable public exponent for RSA 1024. If supported, user-defined fast modular exponentiation can be executed on the smart card via cryptographic coprocessor. This is very specific feature and you will probably not need it;"; 
        m_SystemOutLogger.println(message);
        pFile.write(message.getBytes());
        pValue.append(message);

        // Allocate RSA 1024 objects (RSAPublicKey and ALG_RSA_NOPAD cipher)
        apdu[OFFSET_P1] = 0x01;	
        TestAction("Allocate RSA 1024 objects", apdu, pValue,pFile);
        // Try to set random modulus
        apdu[OFFSET_P1] = 0x02;	
        TestAction("Set random modulus", apdu, pValue,pFile);
        // Try to set random exponent
        apdu[OFFSET_P1] = 0x03;	
        TestAction("Set random public exponent", apdu, pValue,pFile);
        // Try to initialize cipher with public key with random exponent
        apdu[OFFSET_P1] = 0x04;	
        TestAction("Initialize cipher with public key with random exponent", apdu, pValue,pFile);
        // Try to encrypt block of data
        apdu[OFFSET_P1] = 0x05;	
        TestAction("Use random public exponent", apdu, pValue,pFile);        
 
        return status;
    }
    
    public int TestAction(String actionName, byte apdu[], StringBuilder pValue, FileOutputStream pFile) throws Exception {
	int		status = STAT_OK;

        long     elapsedCard = 0;
	elapsedCard -= System.currentTimeMillis();

	String message;
	message = String.format("\r\n%1s;", actionName); 
        m_SystemOutLogger.println(message);
        pFile.write(message.getBytes());
        pValue.append(message);
            
        ResponseAPDU resp = sendAPDU(apdu);
        if (resp.getSW() != 0x9000) {
            message = String.format("no;"); 
            m_SystemOutLogger.println(message);
            pFile.write(message.getBytes());
            pValue.append(message);
        } else {
            // SAVE TIME OF CARD RESPONSE
            elapsedCard += System.currentTimeMillis();
            String elTimeStr;
            // OUTPUT REQUIRED TIME WHEN PARTITIONED CHECk WAS PERFORMED (NOTMULTIPLE ALGORITHMS IN SINGLE RUN)
            elTimeStr = String.format("%1f", (double) elapsedCard / (float) CLOCKS_PER_SEC);

            message = String.format("yes;%1s sec;", elTimeStr); 
            m_SystemOutLogger.println(message);
            pFile.write(message.getBytes());
            pValue.append(message);
	}

	return status;
    }

    public boolean resetApplet(byte cla, byte ins) {
        return resetApplet(cla, ins, (byte) 0); 
    }
    public boolean resetApplet(byte cla, byte ins, byte p1) {
        try {
            m_SystemOutLogger.println("\nFree unused card objects...\n");
            byte apdu[] = {cla,ins,p1,0};
            ResponseAPDU resp = sendAPDU(apdu);
            if (resp.getSW() != 0x9000) {
                return false;
            }
            return true;
        }
        catch(Exception e) {
            return false;
        }
    }    
    
    public double PerfTestCommand(byte cla, byte ins, TestSettings testSet, byte resetIns) throws Exception {
        long elapsedCard;
        byte apdu[] = new byte[HEADER_LENGTH + TestSettings.TEST_SETTINGS_LENGTH + ((testSet.inData == null) ? 0 : testSet.inData.length)];
        apdu[OFFSET_CLA] = cla;
        apdu[OFFSET_INS] = ins;
        apdu[OFFSET_P1] = testSet.P1;
        apdu[OFFSET_P2] = testSet.P2;
        apdu[OFFSET_LC] = (byte) (apdu.length - HEADER_LENGTH);
        CardMngr.serializeToApduBuff(testSet, apdu, ISO7816.OFFSET_CDATA);

        if (testSet.inData != null) {
            System.arraycopy(testSet.inData, 0, apdu, OFFSET_DATA + TestSettings.TEST_SETTINGS_LENGTH, (short) testSet.inData.length);
        }

        elapsedCard = -System.currentTimeMillis();
        ResponseAPDU resp = sendAPDU(apdu);
        if (resp.getSW() != 0x9000) {           
            boolean succes = resetApplet(cla, resetIns);
            if (succes) {
                elapsedCard = -System.currentTimeMillis();
                resp = sendAPDU(apdu);
                elapsedCard += System.currentTimeMillis();
                if (resp.getNr() != 0) {
                    byte data[] = resp.getData();          
                    if (data[0] != SUCCESS) throw new CardCommunicationException(data[0]);
                } 
                else {
                    throw new CardCommunicationException(resp.getSW());
                }
            }
            else
            {
                m_SystemOutLogger.println("Reset applet didn't work, speed of algorithm couldn't be mesured");
                throw new CardCommunicationException(CANT_BE_MEASURED);
            }
        }
        else {
            elapsedCard += System.currentTimeMillis();
            if (resp.getNr()!=0) {
                byte data[];
                data = resp.getData();          
                if(data[0] != SUCCESS) throw new CardCommunicationException(data[0]);
            }            
            else {
                throw new CardCommunicationException(resp.getSW());
            }
            
        }
        return (double) elapsedCard ;
    }    
    
    public double BasicTest(byte algClass, byte alg, byte p1, byte p2, byte[] cdata, byte dataLength, byte resetIns) throws Exception
    {
        long elapsedCard;
        byte apdu[] = new byte[HEADER_LENGTH + dataLength];
        apdu[OFFSET_CLA] = algClass;
        apdu[OFFSET_INS] = alg;
        apdu[OFFSET_P1] = p1;
        apdu[OFFSET_P2] = p2;
        apdu[OFFSET_LC] = dataLength;
        System.arraycopy(cdata, 0, apdu, OFFSET_DATA, dataLength);
        elapsedCard = -System.currentTimeMillis();
        ResponseAPDU resp = sendAPDU(apdu);
        if (resp.getSW() != 0x9000) 
        {           
            boolean succes = resetApplet(algClass, resetIns);
            if (succes)
            {
                elapsedCard = -System.currentTimeMillis();
                resp = sendAPDU(apdu);
                elapsedCard += System.currentTimeMillis();
                if (resp.getNr()!=0)
                {
                    byte data[];
                    data = resp.getData();          
                    if(data[0] != SUCCESS) throw new CardCommunicationException(data[0]);
                }                
            }
            else
            {
                m_SystemOutLogger.println("Reset applet didn't work, speed of algorithm couldn't be mesured");
                throw new CardCommunicationException(CANT_BE_MEASURED);
            }
        }
        else
        {
            elapsedCard += System.currentTimeMillis();
            if (resp.getNr()!=0)
            {
                byte data[];
                data = resp.getData();          
                if(data[0] != SUCCESS) throw new CardCommunicationException(data[0]);
            }            
        }
        return (double) elapsedCard ;
    }       
    
    public static byte[] hexStringToByteArray(String s) {
        String sanitized = s.replace(" ", "");
        byte[] b = new byte[sanitized.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(sanitized.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }
 
    public static byte[] valuesStringToByteArray(String s) {
        String sanitized = s.replace(" ", "");
        byte[] b = new byte[sanitized.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(sanitized.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }    
    
    public int RestartCardWithUpload(int seriousProblemCounter, FileOutputStream file) throws Exception {
        seriousProblemCounter++;
        if (seriousProblemCounter >= MAX_SERIOUS_PROBLEMS_IN_ROW) {
            throw new Exception("Too many problems with card, stopping.");
        }

        try {
            Thread.sleep(3000);
            // Some problem, upload applet again
            UploadApplet();
            file.write("# Applet uploaded\n\n".toString().getBytes());

            Thread.sleep(3000);

            ConnectToCard(m_terminal, null, null, null);
        }
        catch (Exception ex) {
            m_SystemOutLogger.println(getTerminalName() + " : Failed with " + ex.getMessage());
            seriousProblemCounter = RestartCardWithUpload(seriousProblemCounter, file);
        }
        return seriousProblemCounter;
    }
        
    public void UploadApplet() throws Exception {
        m_SystemOutLogger.println("Uploading applet to card on terminal " + getTerminalName() + "...");
        String cardAtr = getATR().replace(" ", "_");
        
        //Check if folder !card_uploaders is correctly set
        File fileCardUploadersFolder = new File(CardMngr.cardUploadersFolder);
        if(!fileCardUploadersFolder.exists()) {
            throw new Exception("Cannot find !card_uploaders folder. Folder " + CardMngr.cardUploadersFolder + " does not exist.");
        }

        //Set path to run bat file
        String batFileName;
        if(CardMngr.cardUploadersFolder.endsWith(File.separator)) batFileName = CardMngr.cardUploadersFolder + "upload.bat";
        else batFileName = CardMngr.cardUploadersFolder + File.separator + "upload.bat";
        
        //Run bat file with arguments cardAtr and readerName
        String[] commands = new String[]{batFileName, cardAtr, getTerminalName()};
        ProcessBuilder pb = new ProcessBuilder(commands);
        pb.directory(fileCardUploadersFolder);
        pb.redirectErrorStream(true);
        pb.redirectOutput(Redirect.appendTo(new File("upload_log_" + getTerminalName() + ".txt")));

        Process p = pb.start();
        p.waitFor();   
        //Check if process ended successful
        if(p.exitValue()!=0) {
            m_SystemOutLogger.println("Uploading applet: Error");
            throw new Exception("Cannot upload applet. Process of file " + batFileName + " ended with " + p.exitValue());
        }
        else {
            m_SystemOutLogger.println("Uploading applet: Done");
        }                
    } 
    
    public int GenerateAndGetKeys(String fileName, int numRepeats, int resetFrequency, boolean uploadBeforeStart, short bitLength, boolean useCrt) throws Exception {
        FileOutputStream file = new FileOutputStream(fileName);
        int ret = GenerateAndGetKeys(file, numRepeats, resetFrequency, uploadBeforeStart, bitLength, useCrt);   
        file.close();  
        return ret;
    }
    
    public TestSettings prepareKeyHarvestSettings(short keyClass,short keyType, short keyLength) {
        TestSettings    testSet = new TestSettings();
        testSet.classType = Consts.CLASS_KEYBUILDER;
        testSet.algorithmSpecification = Consts.UNUSED;
        testSet.keyType = keyType;                                  // e.g., KeyBuilder.TYPE_AES
        testSet.keyLength = keyLength;                              // e.g., KeyBuilder.LENGTH_AES_128
        testSet.keyClass = keyClass;                                // e.g., KeyPair.ALG_RSA_CRT
        testSet.algorithmMethod = Consts.UNUSED;                  
        testSet.dataLength1 = Consts.TEST_DATA_LENGTH;                     
        testSet.dataLength2 = Consts.UNUSED;                        
        testSet.initMode = Consts.UNUSED;                               
        testSet.numRepeatSubOperation = 1;      
        testSet.numRepeatWholeMeasurement = 0;                  
        return testSet;
    }
    
    public byte[] prepareApduForKeyHarvest(TestSettings setting) {
        byte apdu[] = new byte[HEADER_LENGTH + TestSettings.TEST_SETTINGS_LENGTH + ((setting.inData == null) ? 0 : setting.inData.length)];
        apdu[OFFSET_CLA] = Consts.CLA_CARD_ALGTEST;
        apdu[OFFSET_INS] = Consts.INS_CARD_GETRSAKEY;
        apdu[OFFSET_P1] = setting.P1;
        apdu[OFFSET_P2] = setting.P2;
        apdu[OFFSET_LC] = (byte) (apdu.length - HEADER_LENGTH);
        CardMngr.serializeToApduBuff(setting, apdu, ISO7816.OFFSET_CDATA);
        return apdu;
    }
    
    public byte[] prepareApduCipherEngine(TestSettings setting) {
        byte apdu[] = new byte[HEADER_LENGTH + TestSettings.TEST_SETTINGS_LENGTH + ((setting.inData == null) ? 0 : setting.inData.length)];
        apdu[OFFSET_CLA] = Consts.CLA_CARD_ALGTEST;
        apdu[OFFSET_INS] = Consts.INS_PREPARE_CIPHERENGINE;
        apdu[OFFSET_P1] = setting.P1;
        apdu[OFFSET_P2] = setting.P2;
        apdu[OFFSET_LC] = (byte) (apdu.length - HEADER_LENGTH);
        CardMngr.serializeToApduBuff(setting, apdu, ISO7816.OFFSET_CDATA);
        return apdu;
    }    
    
        
    public boolean PrepareRSAEngine(FileOutputStream file, short bitLength, boolean useCrt) throws Exception {
        short keyClass = JCConsts.KeyPair_ALG_RSA;
        if (useCrt) {
            keyClass = JCConsts.KeyPair_ALG_RSA_CRT;
        }

        TestSettings publicKeySetting = this.prepareKeyHarvestSettings(keyClass, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, bitLength);
        byte apduEngine[] = this.prepareApduCipherEngine(publicKeySetting);

        // Prepare for new key generation
        long elapsedCard = -System.currentTimeMillis();

        ResponseAPDU resp = sendAPDU(apduEngine);
        elapsedCard += System.currentTimeMillis();

        if (resp.getSW() != 0x9000) {
            m_SystemOutLogger.println(getTerminalName() + " : Failed to prepare new RSA engine " + Integer.toHexString(resp.getSW()));
            return false;
        } else {
            // We have engine prepared
            m_SystemOutLogger.println("RSA engine prepared");
        }
        return true;
    }
            
    public int GenerateAndGetKeys(FileOutputStream file, int numRepeats, int resetFrequency, boolean uploadBeforeStart, short bitLength, boolean useCrt) throws Exception { 
        String message;
        int numKeysGenerated = 0;                  
        StringBuilder key = new StringBuilder();
        boolean bResetCard = false;
        if (numRepeats == -1) numRepeats = 300000;
        if (uploadBeforeStart) {
            UploadApplet();
            ConnectToCard(m_terminal, null, null, null);
        }
        
        if (PrepareRSAEngine(file, bitLength, useCrt)) {
            int seriousProblemCounter = 0;

            short keyClass = JCConsts.KeyPair_ALG_RSA;
            if (useCrt) keyClass = JCConsts.KeyPair_ALG_RSA_CRT;
            TestSettings publicKeySetting = this.prepareKeyHarvestSettings(keyClass, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, bitLength);
            byte apduPublic[] = this.prepareApduForKeyHarvest(publicKeySetting);

            TestSettings privateKeySetting = this.prepareKeyHarvestSettings(keyClass, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, bitLength);
            byte apduPrivate[] = this.prepareApduForKeyHarvest(privateKeySetting);

            int errors = 0;
            while (numKeysGenerated < numRepeats) {
                try {
                    key.setLength(0);
                    if ((resetFrequency > 0) && (numKeysGenerated % resetFrequency == 0)) { bResetCard = true; }

                    // Reset card if required
                    if (bResetCard) {
                        m_SystemOutLogger.println(getTerminalName() + " : Reseting card...");
                        key.append("# Card reseted\n\n");
                        file.write(key.toString().getBytes());

                        m_channel.getCard().disconnect(true);
                        ConnectToCard(m_terminal, null, null, null);
                        bResetCard = false;
                    }

                    // Prepare for new key generation
                    long elapsedCard = - System.currentTimeMillis();

                    ResponseAPDU resp = sendAPDU(apduPublic);
                    elapsedCard += System.currentTimeMillis();

                    if (resp.getSW() != 0x9000) {
                        m_SystemOutLogger.println(getTerminalName() + " : Failed to generate new key with " + Integer.toHexString(resp.getSW()));
                        // Some problem, upload applet again
                        UploadApplet();
                        errors++;
                        if (errors>=2) throw new Exception("Cannot upload applet.");

                        key.append("# Applet uploaded\n\n");
                        file.write(key.toString().getBytes());

                        ConnectToCard(m_terminal, null, null, null);
                        continue;
                    }
                    else {
                        // We got public key out
                        byte pubKey[] = resp.getData();
                        key.append("PUBL: ");
                        key.append(bytesToHex(pubKey, false));
                        key.append("\n");
                        errors = 0;
                    }


                    // Ask for private key
                    privateKeySetting.P1 = 1;
                    byte apduPrivateP[] = this.prepareApduForKeyHarvest(privateKeySetting);
                    privateKeySetting.P1 = 2;
                    byte apduPrivateQ[] = this.prepareApduForKeyHarvest(privateKeySetting);
                    ResponseAPDU respPrivateP = sendAPDU(apduPrivateP);
                    ResponseAPDU respPrivateQ = sendAPDU(apduPrivateQ);
                    //ResponseAPDU respPrivate = sendAPDU(apduPrivate);

                    if ((respPrivateP.getSW() != 0x9000) || (respPrivateQ.getSW() != 0x9000)) {
                        m_SystemOutLogger.println(getTerminalName() + " : Failed to obtain private key P with " + Integer.toHexString(respPrivateP.getSW()));
                        m_SystemOutLogger.println(getTerminalName() + " : Failed to obtain private key Q with " + Integer.toHexString(respPrivateQ.getSW()));
                        continue;
                    }  
                    else {
                        // We got private key out
                        byte privKeyP[] = respPrivateP.getData();
                        byte privKeyQ[] = respPrivateQ.getData();
                        byte privKey[] = new byte[privKeyP.length + privKeyQ.length]; 

                        System.arraycopy(privKeyP, 0, privKey, 0, privKeyP.length);
                        System.arraycopy(privKeyQ, 0, privKey, privKeyP.length, privKeyQ.length);
                        key.append("PRIV: ");
                        key.append(bytesToHex(privKey, false));
                        key.append("\n");
                    }


                    String elTimeStr;
                    // OUTPUT REQUIRED TIME WHEN PARTITIONED CHECK WAS PERFORMED (NOTMULTIPLE ALGORITHMS IN SINGLE RUN)
                    elTimeStr = String.valueOf((double) elapsedCard / (float) CLOCKS_PER_SEC);
                    key.append("# ");
                    key.append(numKeysGenerated + 1);
                    key.append(":");
                    key.append(elTimeStr);
                    key.append("\n\n");

                    // Save key to file
                    file.write(key.toString().getBytes());


                    numKeysGenerated++; 

                    message = getTerminalName() + " | " + numKeysGenerated + " : " ;
                    message += elTimeStr;
                    m_SystemOutLogger.println(message);

                    file.flush();

                    seriousProblemCounter = 0;   // problems were solved now
                }
                catch (Exception ex) {
                    m_SystemOutLogger.println(getTerminalName() + " : Failed with " + ex.getMessage());
                    seriousProblemCounter = RestartCardWithUpload(seriousProblemCounter, file);
                }
            }   
        }
        else {
            // Failed to prepare engine for given length => no keys
            numKeysGenerated = 0;
            message = String.format("\nFailed to prepare RSA engine for key length '%d'\n\n", bitLength);
            file.write(message.getBytes());
        }
        return numKeysGenerated;
    }    
   
    public static short setShort(byte[] buffer, int offset, short value) {
        buffer[offset] = (byte) (value >> 8 & 0xff);
        buffer[offset + 1] = (byte) (value & 0xff);
        return (short) (offset + 2); // size of short == 2
    }    
    public static short serializeToApduBuff(TestSettings testSet, byte[] apdubuf, short offset) {
        setShort(apdubuf, (short) (offset + TestSettings.OFFSET_ALGORITHM_CLASS), testSet.classType);
        setShort(apdubuf, (short) (offset + TestSettings.OFFSET_ALGORITHM_SPECIFICATION), testSet.algorithmSpecification);
        setShort(apdubuf, (short) (offset + TestSettings.OFFSET_ALGORITHM_PARAM1), testSet.keyClass);
        setShort(apdubuf, (short) (offset + TestSettings.OFFSET_ALGORITHM_PARAM2), testSet.keyType);
        setShort(apdubuf, (short) (offset + TestSettings.OFFSET_ALGORITHM_PARAM3), testSet.keyLength);
        setShort(apdubuf, (short) (offset + TestSettings.OFFSET_ALGORITHM_TESTED_OPS), testSet.algorithmMethod);
        setShort(apdubuf, (short) (offset + TestSettings.OFFSET_DATA_LENGTH1), testSet.dataLength1);
        setShort(apdubuf, (short) (offset + TestSettings.OFFSET_DATA_LENGTH2), testSet.dataLength2);
        setShort(apdubuf, (short) (offset + TestSettings.OFFSET_DATA_INITMODE), testSet.initMode);
        setShort(apdubuf, (short) (offset + TestSettings.OFFSET_NUM_REPEAT_WHOLE_OP), testSet.numRepeatWholeOperation);
        setShort(apdubuf, (short) (offset + TestSettings.OFFSET_NUM_REPEAT_SUB_OP), testSet.numRepeatSubOperation);

        return TestSettings.TEST_SETTINGS_LENGTH;
    }    
}
