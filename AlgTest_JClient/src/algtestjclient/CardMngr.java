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

/* BUGBUG: we need to figure out how to support JCardSim in nice way (copy of class files, directory structure...)
import com.licel.jcardsim.io.CAD;
import com.licel.jcardsim.io.JavaxSmartCardInterface;
*/
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
/* BUGBUG: we need to figure out how to support JCardSim in nice way (copy of class files, directory structure...)
//import javacard.framework.AID;
*/
import javax.smartcardio.*;
import AlgTest.Consts;
import AlgTest.TestSettings;

import com.licel.jcardsim.io.CAD;
import com.licel.jcardsim.io.JavaxSmartCardInterface;
import javacard.framework.AID;
import javacard.framework.ISO7816;


/**
 *
 * @author petrs
 */
public class CardMngr {
    public static CAD m_cad = null;
    public static JavaxSmartCardInterface m_simulator = null;
    
    final static byte SUCCESS =                    (byte) 0xAA;
    
    // TODO: unification of errors
    final static int CANT_BE_MEASURED              = 256;   
    final static byte ILLEGAL_USE = (byte) 5;
    final static byte ILLEGAL_VALUE = (byte) 1;
    final static byte INVALID_INIT = (byte) 4;
    final static byte NO_SUCH_ALGORITHM = (byte) 3;
    final static byte UNINITIALIZED_KEY = (byte) 2;       

    /* Argument constants for choosing algorithm to test. */
    
    /* Arguments for choosing which AlgTest version to run. */
    public static final String ALGTEST_MULTIPERAPDU = "AT_MULTIPERAPDU";        // for 'old' AlgTest
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

    /* Byte values for choosing method on on-card app. */
    public static final byte CLASS_CIPHER          = 0x11;
    public static final byte CLASS_SIGNATURE       = 0x12;
    public static final byte CLASS_KEYAGREEMENT    = 0x13;
    public static final byte CLASS_MESSAGEDIGEST   = 0x15;
    public static final byte CLASS_RANDOMDATA      = 0x16;
    public static final byte CLASS_CHECKSUM        = 0x17;
    public static final byte CLASS_KEYPAIR_RSA     = 0x18;
    public static final byte CLASS_KEYPAIR_RSA_CRT = 0x19;
    public static final byte CLASS_KEYBUILDER      = 0x20;
    public static final byte CLASS_KEYPAIR_DSA     = 0x1A;
    public static final byte CLASS_KEYPAIR_EC_F2M  = 0x1B;
    public static final byte CLASS_KEYPAIR_EC_FP   = 0x1C;


    
    CardTerminal m_terminal = null;
    CardChannel m_channel = null;
    Card m_card = null;
    
    public static final byte selectApplet[] = {
        (byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x00, (byte) 0x09, 
        (byte) 0x6D, (byte) 0x79, (byte) 0x70, (byte) 0x61, (byte) 0x63, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x31}; 
    
    public static final String helpString = "This program can be used with following parameters:\r\n"
            + ALGTEST_MULTIPERAPDU + " -> for using classic AlgTest with multiple tests per APDU command\r\n"
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
            ALGTEST_MULTIPERAPDU + "\r\n"
            + ALGTEST_SINGLEPERAPDU + "\r\n"
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
    
    /* ATR of jCardSim. */
    static final String SIMULATOR_ATR = "3BFA1800008131FE454A434F5033315632333298";

    /* BUGBUG: we need to figure out how to support JCardSim in nice way (copy of class files, directory structure...)
    JavaxSmartCardInterface simulator = null;
    */
    
    /* CLOCKS_PER_SEC also used in 'PerformanceTesting.java' */
    public static final int CLOCKS_PER_SEC = 1000;
    
    public FileOutputStream establishConnection(Class ClassToTest) throws Exception{
        if (ConnectToCard(ClassToTest, reader, atr, protocol)) {
            String message = "";
            if (atr.toString().equals("")){atr.append(SIMULATOR_ATR + " (provided by jCardSimulator)");} // if atr == "" it means that simulator is running and thus simulator atr must be used
            System.out.println("ATR: " + atr);
            String fileName = "AlgTest_" + atr + ".csv";
            fileName = fileName.replace(":", "");
            fileName = fileName.replace(" ", "_");
            
            FileOutputStream file = new FileOutputStream(fileName);
            
            StringBuilder value = new StringBuilder();
                 
            message += "INFO: This file was generated by AlgTest utility. See http://www.fi.muni.cz/~xsvenda/jcsupport.html for more results, source codes and other details.;\r\n";
            System.out.println(message); file.write(message.getBytes());                
                
            message = "Tested and provided by; insert your name please.;\r\n";
            System.out.println(message); file.write(message.getBytes());                

            message = "Execution date/time; ";
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            message += dateFormat.format(date) + "\r\n";
            System.out.println(message); file.write(message.getBytes()); 

            message = "AlgTestJClient version; " + AlgTestJClient.ALGTEST_JCLIENT_VERSION + "\r\n";
            System.out.println(message); file.write(message.getBytes());    

            value.setLength(0);
            if (GetAppletVersion(value) == CardMngr.STAT_OK) {
                message = "AlgTest applet version; " + value + "\r\n";
                System.out.println(message); file.write(message.getBytes()); 
            }
            else { 
                message = "\nERROR: GetAppletVersion fail"; 
                System.out.println(message); file.write(message.getBytes());
            }

            message = "Used reader; " + reader + "\r\n";
            System.out.println(message); file.write(message.getBytes());
            message = "Card ATR; " + atr + "\r\n";
            System.out.println(message); file.write(message.getBytes());
            message = "Used protocol; " + protocol + "\r\n";
            System.out.println(message); file.write(message.getBytes());

            System.out.println("\n\n#########################");
            System.out.println("\nJCSystem information");

            if (GetJCSystemInfo(value, file) == CardMngr.STAT_OK) {}
            else { System.out.println("\nERROR: GetJCSystemInfo fail"); }
            
            return file; // if succesfull, returns open file for AlgTest output
        }
        return null;    // returns 'null' in case of error
    }
    
    public boolean ConnectToCard(Class ClassToTest, StringBuilder selectedReader, StringBuilder selectedATR, StringBuilder usedProtocol) throws Exception {
        boolean cardFound = false;        
        
        if (ClassToTest != null) {
            System.out.println("No terminals found");
            System.out.println("Creating simulator...");
            MakeSim(ClassToTest);

            System.out.println("Simulator created.");
            cardFound = true;
        }
        else {
            // TRY ALL READERS, FIND FIRST SELECTABLE
            List terminalList = GetReaderList();
            //List numbers of Card readers        
            for (int i = 0; i < terminalList.size(); i++) {
                System.out.println(i + " : " + terminalList.get(i));
                m_terminal = (CardTerminal) terminalList.get(i);
                if (m_terminal.isCardPresent()) {
                    m_card = m_terminal.connect("*");
                    System.out.println("card: " + m_card);
                    m_channel = m_card.getBasicChannel();
                        System.out.println("Card Channel: " + m_channel.getChannelNumber());

                    //reset the card
                    ATR atr = m_card.getATR();
                    System.out.println(bytesToHex(atr.getBytes()));

                    // SELECT APPLET
                    ResponseAPDU resp = sendAPDU(selectApplet);
                    if (resp.getSW() != 0x9000) {
                        System.out.println("No card found.");
                    } else {
                        // CARD FOUND
                        cardFound = true;

                        selectedATR.append(bytesToHex(m_card.getATR().getBytes()));
                        selectedReader.append(terminalList.get(i).toString());
                        usedProtocol.append(m_card.getProtocol());

                        break;
                    }
                }
            }
        }
        return cardFound;        
    }

    public void DisconnectFromCard() throws Exception {
        if (m_card != null) {
            m_card.disconnect(false);
            m_card = null;
        }
    }

    public List GetReaderList() {
        try {
            TerminalFactory factory = TerminalFactory.getDefault();
            List readersList = factory.terminals().list();
            return readersList;
        } catch (CardException ex) {
            System.out.println("Exception : " + ex);
            return null;
        }
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
        System.out.println(">>>>");
        System.out.println(commandAPDU);

        System.out.println(bytesToHex(commandAPDU.getBytes()));
        
        if (m_simulator != null){  // in case simulator is running
            responseAPDU = m_simulator.transmitCommand(commandAPDU);
        }
        else {                  // in case there is actual card present
            responseAPDU = m_channel.transmit(commandAPDU);
        }

        System.out.println(responseAPDU);
        System.out.println(bytesToHex(responseAPDU.getBytes()));

        if (responseAPDU.getSW1() == (byte) 0x61) {
            CommandAPDU apduToSend = new CommandAPDU((byte) 0x00,
                    (byte) 0xC0, (byte) 0x00, (byte) 0x00,
                    (int) responseAPDU.getSW1());

            responseAPDU = m_channel.transmit(apduToSend);
            System.out.println(bytesToHex(responseAPDU.getBytes()));
        }

        System.out.println("<<<<");

        return (responseAPDU);
    }

    public String byteToHex(byte data) {
        StringBuilder  buf = new StringBuilder ();
        buf.append(toHexChar((data >>> 4) & 0x0F));
        buf.append(toHexChar(data & 0x0F));
        return buf.toString();
    }

    public char toHexChar(int i) {
        if ((0 <= i) && (i <= 9)) {
            return (char) ('0' + i);
        } else {
            return (char) ('a' + (i - 10));
        }
    }

    public String bytesToHex(byte[] data) {
        StringBuilder  buf = new StringBuilder ();
        for (int i = 0; i < data.length; i++) {
            buf.append(byteToHex(data[i]));
            buf.append(" ");
        }
        return (buf.toString());
    }
    
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
                System.out.println("Fail to obtain Applet version");
                pValue.append("error");
            } else {
                byte temp[] = resp.getData();
                char version[] = new char[temp.length]; 
                for (int i = 0; i < temp.length; i++) { version[i] = (char) temp[i]; }
                pValue.append(version);
             }        
        }
        catch (Exception ex) {
            System.out.println("Fail to obtain Applet version");
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
        apdu[OFFSET_INS] = (byte) 0x73;
        apdu[OFFSET_P1] = 0x00;
        apdu[OFFSET_P2] = 0x00;
        apdu[OFFSET_LC] = 0x01;
        apdu[OFFSET_DATA] = 0x01;

        elapsedCard -= System.currentTimeMillis();
        try {
            ResponseAPDU resp = sendAPDU(apdu);
            if (resp.getSW() != 0x9000) {
                System.out.println("Fail to obtain JCSystemInfo");
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



                String message;
                message = String.format("\r\n%1s;%d.%d;", GetAlgorithmName(SingleModeTest.JCSYSTEM_STR[1]), versionMajor, versionMinor); 
                System.out.println(message);
                pFile.write(message.getBytes());
                pValue.append(message);
                message = String.format("\r\n%s;%s;", GetAlgorithmName(SingleModeTest.JCSYSTEM_STR[2]),(bDeletionSupported != 0) ? "yes" : "no"); 

                System.out.println(message);
                pFile.write(message.getBytes());
                pValue.append(message);
                message = String.format("\r\n%s;%s%dB;", GetAlgorithmName(SingleModeTest.JCSYSTEM_STR[3]),(eepromSize == 32767) ? ">" : "", eepromSize); 
                System.out.println(message);
                pFile.write(message.getBytes());
                pValue.append(message);
                message = String.format("\r\n%s;%s%dB;", GetAlgorithmName(SingleModeTest.JCSYSTEM_STR[4]),(ramResetSize == 32767) ? ">" : "", ramResetSize); 
                System.out.println(message);
                pFile.write(message.getBytes());
                pValue.append(message);
                message = String.format("\r\n%s;%s%dB;\n", GetAlgorithmName(SingleModeTest.JCSYSTEM_STR[5]),(ramDeselectSize == 32767) ? ">" : "", ramDeselectSize); 
                System.out.println(message);
                message += "\r\n";

                pFile.write(message.getBytes());
                pValue.append(message);
            }        
        }
        catch (Exception ex) {
            System.out.println("Fail to obtain JCSystemInfo");
            pValue.append("error");
        }
                      
                
	return status;
    }
    
    /**
     * Method that will test all algorithms.
     * @param file FileOutputStream object for output data.
     * @throws Exception
     */
    public void testAllAtOnce (FileOutputStream file) throws Exception{
        StringBuilder value = new StringBuilder();
        int answ = 1;   // so 'GetSupportedAndParse' doesn't ask if to test given algorithm
        
        // Class javacardx.crypto.Cipher
        value.setLength(0);
        if (GetSupportedAndParse(CLASS_CIPHER, SingleModeTest.CIPHER_STR, value, file, (byte) 0, answ) == STAT_OK) {}
        else { String errorMessage = "\nERROR: javacardx.crypto.Cipher fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
        file.flush();

        // Class javacard.security.Signature
        value.setLength(0);
        if (GetSupportedAndParse(CLASS_SIGNATURE, SingleModeTest.SIGNATURE_STR, value, file, (byte) 0, answ) == STAT_OK) {}
        else { String errorMessage = "\nERROR: javacard.security.Signature fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
        file.flush();

        // Class javacard.security.MessageDigest
        value.setLength(0);
        if (GetSupportedAndParse(CLASS_MESSAGEDIGEST, SingleModeTest.MESSAGEDIGEST_STR, value, file, (byte) 0, answ) == STAT_OK) {}
        else { String errorMessage = "\nERROR: javacard.security.MessageDigest fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
        file.flush();

        // Class javacard.security.RandomData
        value.setLength(0);
        if (GetSupportedAndParse(CLASS_RANDOMDATA, SingleModeTest.RANDOMDATA_STR, value, file, (byte) 0, answ) == STAT_OK) {}
        else { String errorMessage = "\nERROR: javacard.security.RandomData fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
        file.flush();

        // Class javacard.security.KeyBuilder
        value.setLength(0);
        if (GetSupportedAndParse(CLASS_KEYBUILDER, SingleModeTest.KEYBUILDER_STR, value, file, (byte) 0, answ) == STAT_OK) {}
        else { String errorMessage = "\nERROR: javacard.security.KeyBuilder fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
        file.flush();

        // Class javacard.security.KeyPair RSA
        value.setLength(0);
        if (GetSupportedAndParse(CLASS_KEYPAIR_RSA, SingleModeTest.KEYPAIR_RSA_STR, value, file, Consts.CLASS_KEYPAIR_RSA_P2, answ) == STAT_OK) {}
        else { String errorMessage = "\nERROR: javacard.security.KeyPair RSA fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
        file.flush();
        // Class javacard.security.KeyPair RSA_CRT
        value.setLength(0);
        if (GetSupportedAndParse(CLASS_KEYPAIR_RSA_CRT, SingleModeTest.KEYPAIR_RSACRT_STR, value, file, Consts.CLASS_KEYPAIR_RSACRT_P2, answ) == STAT_OK) {}
        else { String errorMessage = "\nERROR: javacard.security.KeyPair RSA_CRT fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
        file.flush();
        // Class javacard.security.KeyPair DSA
        value.setLength(0);
        if (GetSupportedAndParse(CLASS_KEYPAIR_DSA, SingleModeTest.KEYPAIR_DSA_STR, value, file, Consts.CLASS_KEYPAIR_DSA_P2, answ) == STAT_OK) {}
        else { String errorMessage = "\nERROR: javacard.security.KeyPair DSA fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
        file.flush();
        // Class javacard.security.KeyPair EC_F2M
        value.setLength(0);
        if (GetSupportedAndParse(CLASS_KEYPAIR_EC_F2M, SingleModeTest.KEYPAIR_EC_F2M_STR, value, file,  Consts.CLASS_KEYPAIR_EC_F2M_P2, answ) == STAT_OK) {}
        else { String errorMessage = "\nERROR: javacard.security.KeyPair EC_F2M fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
        file.flush();
        // Class javacard.security.KeyPair EC_FP
        value.setLength(0);
        if (GetSupportedAndParse(CLASS_KEYPAIR_EC_FP, SingleModeTest.KEYPAIR_EC_FP_STR, value, file, Consts.CLASS_KEYPAIR_EC_FP_P2, answ) == STAT_OK) {}
        else { String errorMessage = "\nERROR: javacard.security.KeyPair EC_FP fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
        file.flush();

        // Class javacard.security.KeyAgreement
        value.setLength(0);
        if (GetSupportedAndParse(CLASS_KEYAGREEMENT, SingleModeTest.KEYAGREEMENT_STR, value, file, (byte) 0, answ) == STAT_OK) {}
        else { String errorMessage = "\nERROR: javacard.security.KeyAgreement fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
        file.flush();

        // Class javacard.security.Checksum
        value.setLength(0);
        if (GetSupportedAndParse(CLASS_CHECKSUM, SingleModeTest.CHECKSUM_STR, value, file, (byte) 0, answ) == STAT_OK) {}
        else { String errorMessage = "\nERROR: javacard.security.Checksum fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
        file.flush();
    }
    
    /**
     * Running algorithm testing with parameters from command line.
     * @param args Arguments from command line as String array to tell method which algs to test.
     * @param file File to write data to.
     * @throws Exception
     */
    public void WithArgs (String[] args, FileOutputStream file) throws Exception{
        StringBuilder value = new StringBuilder(); 
        if (args[1].equals(TEST_ALL_ALGORITHMS) ){testAllAtOnce(file);}    
        /* Class 'javacardx.crypto.Cipher'. */
        else if (Arrays.asList(args).contains(TEST_CLASS_CIPHER)){
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_CIPHER, SingleModeTest.CIPHER_STR, value, file, (byte) 0, FORCE_TEST) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacardx.crypto.Cipher fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();
        }
        /* Class 'javacard.security.Signature'. */
        else if (Arrays.asList(args).contains(TEST_CLASS_SIGNATURE)){
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_SIGNATURE, SingleModeTest.SIGNATURE_STR, value, file, (byte) 0, FORCE_TEST) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.Signature fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();
        }
        /* Class 'javacard.security.MessageDigest'. */
        else if (Arrays.asList(args).contains(TEST_CLASS_MESSAGEDIGEST)){
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_MESSAGEDIGEST, SingleModeTest.MESSAGEDIGEST_STR, value, file, (byte) 0, FORCE_TEST) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.MessageDigest fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();
        }
        /* Class 'javacard.security.RandomData'. */
        else if (Arrays.asList(args).contains(TEST_CLASS_RANDOMDATA)){
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_RANDOMDATA, SingleModeTest.RANDOMDATA_STR, value, file, (byte) 0, FORCE_TEST) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.RandomData fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();
        }
        /* Class 'javacard.security.KeyBuilder'. */
        else if (Arrays.asList(args).contains(TEST_CLASS_KEYBUILDER)){
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_KEYBUILDER, SingleModeTest.KEYBUILDER_STR, value, file, (byte) 0, FORCE_TEST) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.KeyBuilder fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();
        }
        /* Class 'javacard.security.KeyPair' RSA. */
        else if (Arrays.asList(args).contains(TEST_CLASS_KEYPAIR_ALG_RSA)){
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_KEYPAIR_RSA, SingleModeTest.KEYPAIR_RSA_STR, value, file, Consts.CLASS_KEYPAIR_RSA_P2, FORCE_TEST) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.KeyPair RSA fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();
        }
        /* Class 'javacard.security.KeyPair' RSA CRT. */
        else if (Arrays.asList(args).contains(TEST_CLASS_KEYPAIR_ALG_RSA_CRT)){
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_KEYPAIR_RSA_CRT, SingleModeTest.KEYPAIR_RSACRT_STR, value, file, Consts.CLASS_KEYPAIR_RSACRT_P2, FORCE_TEST) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.KeyPair RSA_CRT fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();
        }
        /* Class 'javacard.security.KeyPair' DSA. */
        else if (Arrays.asList(args).contains(TEST_CLASS_KEYPAIR_ALG_DSA)){
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_KEYPAIR_DSA, SingleModeTest.KEYPAIR_DSA_STR, value, file, Consts.CLASS_KEYPAIR_DSA_P2, FORCE_TEST) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.KeyPair DSA fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();
        }
        /* Class 'javacard.security.KeyPair' EC_F2M. */
        else if (Arrays.asList(args).contains(TEST_CLASS_KEYPAIR_ALG_EC_F2M)){
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_KEYPAIR_EC_F2M, SingleModeTest.KEYPAIR_EC_F2M_STR, value, file,  Consts.CLASS_KEYPAIR_EC_F2M_P2, FORCE_TEST) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.KeyPair EC_F2M fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();
        }
        /* Class 'javacard.security.KeyPair' EC_FP. */
        else if (Arrays.asList(args).contains(TEST_CLASS_KEYPAIR_ALG_EC_FP)){
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_KEYPAIR_EC_FP, SingleModeTest.KEYPAIR_EC_FP_STR, value, file, Consts.CLASS_KEYPAIR_EC_FP_P2, FORCE_TEST) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.KeyPair EC_FP fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();
        }
        else {
            PrintHelp();
        }
    }
    
    /**
     * Method which will run testing of supported algorithms using MultiPerApdu AlgTest.
     * @param args Arguments from command line as String array to tell method which algorithms to test.
     * @param answ Variable to choose if to test algorithm or to ask to test it.
     * @throws Exception
     */
    public void testClassic (String[] args, int answ) throws Exception{
        /* In case of using simulator, this method will use AlgTest class to run tests. */
        /* BUGBUG: we need to figure out how to support JCardSim in nice way (copy of class files, directory structure...)
        Class testClassClassic = AlgTest.class;
        */
        Class testClassClassic = null;
        FileOutputStream file = establishConnection(testClassClassic);
        StringBuilder value = new StringBuilder();
        /* Testing if there is a file created for output data. */
        if(file == null){
            System.err.println("\nERROR: fail to connect to card with AlgTest applet");
        }
        /* Testing if there are arguments present and if so runs method testing algs with given parameters. */
        if (args.length > 1){
            WithArgs(args, file);
        }
        else{
            // Class javacardx.crypto.Cipher
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_CIPHER, SingleModeTest.CIPHER_STR, value, file, (byte) 0, answ) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacardx.crypto.Cipher fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();

            // Class javacard.security.Signature
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_SIGNATURE, SingleModeTest.SIGNATURE_STR, value, file, (byte) 0, answ) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.Signature fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();

            // Class javacard.security.MessageDigest
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_MESSAGEDIGEST, SingleModeTest.MESSAGEDIGEST_STR, value, file, (byte) 0, answ) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.MessageDigest fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();

            // Class javacard.security.RandomData
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_RANDOMDATA, SingleModeTest.RANDOMDATA_STR, value, file, (byte) 0, answ) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.RandomData fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();

            // Class javacard.security.KeyBuilder
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_KEYBUILDER, SingleModeTest.KEYBUILDER_STR, value, file, (byte) 0, answ) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.KeyBuilder fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();

            // Class javacard.security.KeyPair RSA
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_KEYPAIR_RSA, SingleModeTest.KEYPAIR_RSA_STR, value, file, Consts.CLASS_KEYPAIR_RSA_P2, answ) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.KeyPair RSA fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();

            // Class javacard.security.KeyPair RSA_CRT
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_KEYPAIR_RSA_CRT, SingleModeTest.KEYPAIR_RSACRT_STR, value, file, Consts.CLASS_KEYPAIR_RSACRT_P2, answ) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.KeyPair RSA_CRT fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();

            // Class javacard.security.KeyPair DSA
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_KEYPAIR_DSA, SingleModeTest.KEYPAIR_DSA_STR, value, file, Consts.CLASS_KEYPAIR_DSA_P2, answ) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.KeyPair DSA fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();

            // Class javacard.security.KeyPair EC_F2M
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_KEYPAIR_EC_F2M, SingleModeTest.KEYPAIR_EC_F2M_STR, value, file,  Consts.CLASS_KEYPAIR_EC_F2M_P2, answ) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.KeyPair EC_F2M fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();

            // Class javacard.security.KeyPair EC_FP
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_KEYPAIR_EC_FP, SingleModeTest.KEYPAIR_EC_FP_STR, value, file, Consts.CLASS_KEYPAIR_EC_FP_P2, answ) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.KeyPair EC_FP fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();

            // Class javacard.security.KeyAgreement
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_KEYAGREEMENT, SingleModeTest.KEYAGREEMENT_STR, value, file, (byte) 0, answ) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.KeyAgreement fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();

            // Class javacard.security.Checksum
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_CHECKSUM, SingleModeTest.CHECKSUM_STR, value, file, (byte) 0, answ) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.Checksum fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();
            
            // Variable public exponent
            value.setLength(0);
            if (TestVariableRSAPublicExponentSupport(value, file, (byte) 0) == STAT_OK) {}
            else { String errorMessage = "\nERROR: Test variable public exponent support fail\n"; 
                System.out.println(errorMessage); file.write(errorMessage.getBytes());
            }
            file.flush();
        }
    }
    
    public int TestVariableRSAPublicExponentSupport(StringBuilder pValue, FileOutputStream pFile, byte algPartP2) throws Exception {
        int         status = STAT_OK;
        
        byte apdu[] = new byte[HEADER_LENGTH];
        apdu[OFFSET_CLA] = Consts.CLA_CARD_ALGTEST;
        apdu[OFFSET_INS] = (byte) 0x72;
        apdu[OFFSET_P1] = 0x00;
        apdu[OFFSET_P2] = 0x00;
        apdu[OFFSET_LC] = 0x00;
            
        String message;
        message = "\r\nSupport for variable public exponent for RSA 1024. If supported, user-defined fast modular exponentiation can be executed on the smart card via cryptographic coprocessor. This is very specific feature and you will probably not need it;"; 
        System.out.println(message);
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
        System.out.println(message);
        pFile.write(message.getBytes());
        pValue.append(message);
            
        ResponseAPDU resp = sendAPDU(apdu);
        if (resp.getSW() != 0x9000) {
            message = String.format("no;"); 
            System.out.println(message);
            pFile.write(message.getBytes());
            pValue.append(message);
        } else {
            // SAVE TIME OF CARD RESPONSE
            elapsedCard += System.currentTimeMillis();
            String elTimeStr;
            // OUTPUT REQUIRED TIME WHEN PARTITIONED CHECk WAS PERFORMED (NOTMULTIPLE ALGORITHMS IN SINGLE RUN)
            elTimeStr = String.format("%1f", (double) elapsedCard / (float) CLOCKS_PER_SEC);

            message = String.format("yes;%1s sec;", elTimeStr); 
            System.out.println(message);
            pFile.write(message.getBytes());
            pValue.append(message);
	}

	return status;
    }

    public int GetSupportedAndParse(byte algClass, String algNames[], StringBuilder pValue, FileOutputStream pFile, byte algPartP2, int bForceTest) throws Exception {
        int         status = STAT_OK;
        byte        suppAlg[] = new byte[MAX_SUPP_ALG];
        long       elapsedCard;
        boolean     bNamePrinted = false;
        int         runTest = 1;

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        
        if (bForceTest == 1) {
            runTest = 1;
        }
        else {
            System.out.println("\n\nQ: Do you like to test supported algorithms from class '" + algNames[0] + "'?");
            System.out.println("Type 1 for yes, 0 for no: ");	
            runTest = Integer.decode(br.readLine());                
        }
        
        if (runTest == 1) {
            // CLEAR ARRAY FOR SUPPORTED ALGORITHMS
            Arrays.fill(suppAlg, SUPP_ALG_UNTOUCHED);

            // PREPARE SEPARATE APDU FOR EVERY SIGNALIZED P2 VALUE
            // IF P2 == 0 THEN ALL ALGORITHMS WITHIN GIVEN algClass WILL BE CHECK In SINGLE APDU
            // OTHERWISE, MULTIPLE APDU WILL BE ISSUED
            byte p2Start = (algPartP2 == 0) ? (byte) 0 : (byte) 1; 
            for (byte p2 = p2Start; p2 <= algPartP2; p2++) {

                elapsedCard = -System.currentTimeMillis();

                byte apdu[] = new byte[HEADER_LENGTH];
                apdu[OFFSET_CLA] = Consts.CLA_CARD_ALGTEST;
                apdu[OFFSET_INS] = (byte) 0x70;
                apdu[OFFSET_P1] = algClass;
                apdu[OFFSET_P2] = p2;
                apdu[OFFSET_LC] = 0x00;

                ResponseAPDU resp = sendAPDU(apdu);
                if (resp.getSW() != 0x9000) {
                    String message = "Fail to obtain response for GetSupportedAndParse\r\n";
                    System.out.println(message);
                    pFile.write(message.getBytes());
                } else {
                    // SAVE TIME OF CARD RESPONSE
                    elapsedCard += System.currentTimeMillis();

                    String elTimeStr = "";
                    // OUTPUT REQUIRED TIME WHEN PARTITIONED CHECk WAS PERFORMED (NOT MULTIPLE ALGORITHMS IN SINGLE RUN)
                    if (algPartP2 > 0) { elTimeStr = String.format("%1f", (double) elapsedCard / (float) CLOCKS_PER_SEC);} 

                    // OK, STORE RESPONSE TO suppAlg ARRAY
                    byte temp[] = resp.getData();

                    if (temp[0] == algClass) {
                        
                        // PRINT algClass NAME ONLY ONES
                        if (!bNamePrinted) {
                            String message = "";
                            message += "\r\n"; message += algNames[0]; message += ";\r\n";
                            System.out.println(message);
                            pFile.write(message.getBytes());
                            pValue.append(message);

                            bNamePrinted = true;
                        }
                        for (int i = 1; i < temp.length; i++) {
                            // ONLY FILLED RESPONSES ARE STORED
                            if ((temp[i] != SUPP_ALG_UNTOUCHED) && ((short) (temp[i]&0xff) != SUPP_ALG_SEPARATOR) && ((short) (temp[i]&0xf0) != SUPP_ALG_SMALL_SEPARATOR)) {
                                suppAlg[i] = temp[i];  
                                
                                // Parse algorithm name 
                                String algorithmName = GetAlgorithmName(algNames[i]);
                                
                                // ALG NAME
                                String algState = "";
                                switch (suppAlg[i]) {
                                    case SUPP_ALG_SUPPORTED: { // SUPPORTED
                                        algState += algorithmName; algState += ";"; algState += "yes;"; algState += elTimeStr; algState += "\r\n";
                                        break;
                                    }
                                    case EXCEPTION_CODE_OFFSET + NO_SUCH_ALGORITHM: {
                                        algState += algorithmName; algState += ";"; algState += "no;"; algState += "\r\n";
                                        break;
                                    }
                                    case EXCEPTION_CODE_OFFSET + ILLEGAL_USE: {
                                        algState += algorithmName; algState += ";"; algState += "error(ILLEGAL_USE);"; algState += "\r\n";
                                        break;
                                    }
                                    case EXCEPTION_CODE_OFFSET + ILLEGAL_VALUE: {
                                        algState += algorithmName; algState += ";"; algState += "error(ILLEGAL_VALUE);"; algState += "\r\n";
                                        break;
                                    }
                                    case EXCEPTION_CODE_OFFSET + INVALID_INIT: {
                                        algState += algorithmName; algState += ";"; algState += "error(INVALID_INIT);"; algState += "\r\n";
                                        break;
                                    }
                                    case EXCEPTION_CODE_OFFSET + UNINITIALIZED_KEY: {
                                        algState += algorithmName; algState += ";"; algState += "error(UNINITIALIZED_KEY);"; algState += "\r\n";
                                        break;
                                    }    
                                    case 0x6f: {
                                        algState += algorithmName; algState += ";"; algState += "maybe;"; algState += "\r\n";
                                        break;
                                    }
                                    default: {
                                        // OTHER VALUE, IGNORE 
                                        System.out.println("Unknown value detected in AlgTest applet (0x" + Integer.toHexString(suppAlg[i] & 0xff) + "). Possibly, old version of AlTestJClient is used (try update)");
                                        break;
                                    }
                                }

                                if (algState.equals("")) {
                                }
                                else {
                                    System.out.println(algState);
                                    pFile.write(algState.getBytes());
                                    pValue.append(algState);
                                }
                            }
                        }
                    }
                    else { status = STAT_DATA_CORRUPTED; }
                }
            }
        }
        else {
            String message = "Testing of algorithm class '" + algNames[0] + "' skipped by user";
            System.out.println(message);
            pFile.write(message.getBytes());
        }

        return status;
    }
    
    private boolean resetApplet(byte cla, byte ins) {
        try {
            System.out.println("\nReseting card...");
            byte apdu[] = {cla,ins,0,0};
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
        byte apdu[] = new byte[HEADER_LENGTH + TestSettings.TEST_SETTINGS_LENGTH];
        apdu[OFFSET_CLA] = (byte) cla;
        apdu[OFFSET_INS] = (byte) ins;
        apdu[OFFSET_LC] = TestSettings.TEST_SETTINGS_LENGTH;

        testSet.serializeToApduBuff(apdu, (short) 0);

        elapsedCard = -System.currentTimeMillis();
        ResponseAPDU resp = sendAPDU(apdu);
        if (resp.getSW() != 0x9000) {           
            boolean succes = resetApplet(cla, resetIns);
            if (succes) {
                elapsedCard = -System.currentTimeMillis();
                resp = sendAPDU(apdu);
                elapsedCard += System.currentTimeMillis();
                if (resp.getNr()!=0) {
                    byte data[] = resp.getData();          
                    if (data[0] != SUCCESS) throw new CardCommunicationException(data[0]);
                }                
            }
            else
            {
                System.out.println("Reset applet didn't work, speed of algorithm couldn't be mesured");
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
    
    public double BasicTest(byte algClass, byte alg, byte p1, byte p2, byte[] cdata, byte dataLength, byte resetIns) throws Exception
    {
        long elapsedCard;
        byte apdu[] = new byte[HEADER_LENGTH + dataLength];
        apdu[OFFSET_CLA] = (byte) algClass;
        apdu[OFFSET_INS] = (byte) alg;
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
                System.out.println("Reset applet didn't work, speed of algorithm couldn't be mesured");
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
    
    
    /**
     * Sets up simulator using jCardSim API.
     * @param m_applet Class of on-card AlgTest to be installed in simulator.
     */
    public void MakeSim(Class applet){
    ///* BUGBUG: we need to figure out how to support JCardSim in nice way (copy of class files, directory structure...)
        System.setProperty("com.licel.jcardsim.terminal.type", "2");
        CAD cad = new CAD(System.getProperties());
        m_simulator = (JavaxSmartCardInterface) cad.getCardInterface();
        AID appletAID = new AID(selectApplet, (short)0, (byte) selectApplet.length);
        // installs applet
        m_simulator.installApplet(appletAID, applet);
        // selects applet
        m_simulator.selectApplet(appletAID);
    }
    public void PrintHelp () throws FileNotFoundException, IOException{
        System.out.println(helpString);
        
        System.out.println("Do you want to print supported parameters for AlgTest to separate file? 1 = YES, 0 = NO/r/n");
        Scanner sc = new Scanner(System.in);
        int answ = sc.nextInt();
        if (answ == 1){
            FileOutputStream file = new FileOutputStream("AlgTest_supported_parameters.txt");
            file.write(paramList.getBytes());
            System.out.println("List of supported parameters for AlgTest created in project folder.");
        }
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
}
