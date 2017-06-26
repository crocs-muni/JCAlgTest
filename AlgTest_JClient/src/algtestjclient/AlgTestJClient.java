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

import AlgTest.JCConsts;
import java.io.*;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;
import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

/**
 *
 * @author petr
 */
public class AlgTestJClient {
    /* Arguments for choosing which AlgTest version to run. */
    public static final String ALGTEST_MULTIPERAPDU = "AT_MULTIPERAPDU";        // for 'old' AlgTest
    public static final String ALGTEST_SINGLEPERAPDU = "AT_SINGLEPERAPDU";      // for 'New' AlgTest
    public static final String ALGTEST_PERFORMANCE = "AT_PERFORMANCE";          // for performance testing
    
    /**
     * Version 1.7.3 (10.06.2017) 
     * + added new constants from JC3.0.5
     * - fixed bug with incorrect testing of KeyAgreement 
     */
    public final static String ALGTEST_JCLIENT_VERSION_1_7_3 = "1.7.3";

    /**
     * Version 1.7.2 (06.05.2017) 
     * + support for RSA key generation and export within given range
     * + minor improvements of interface
     */
    //public final static String ALGTEST_JCLIENT_VERSION_1_7_2 = "1.7.2";

    /**
     * Version 1.7.1 (03.10.2016) 
     * + support for reader access via JNA
     */
    //public final static String ALGTEST_JCLIENT_VERSION_1_7_1 = "1.7.1";
    /**
     * Version 1.7.0 (19.09.2016) + Updates to support EC and asym. key
     * operations properly
     */
    //public final static String ALGTEST_JCLIENT_VERSION_1_7_0 = "1.7.0";
    /**
     * Version 1.6.0 (19.07.2015)
     * + Many updates, performance tests
     */
    //public final static String ALGTEST_JCLIENT_VERSION_1_6_0 = "1.6.0";        
        
    /**
     * Version 1.3 (30.11.2014)
     * + Improved gathering of data, single command per single algorithm instance possible
     */
    //public final static String ALGTEST_JCLIENT_VERSION_1_3_0 = "1.3.0";    
    /**
     * Version 1.2.1 (29.1.2014)
     * + added support for TYPE_RSA_PRIVATE_TRANSIENT_RESET and TYPE_RSA_PRIVATE_TRANSIENT_DESELECT parsing
     * + added possibility to run test for every class separately
     * - more information for user, small refactoring
     */
    //public final static String ALGTEST_JCLIENT_VERSION_1_2_1 = "1.2.1";
    /**
     * Version 1.2 (3.11.2013)
     * + All relevant constants from JC2.2.2, JC3.0.1 and JC3.0.4 added
     */
    //public final static String ALGTEST_JCLIENT_VERSION_1_2 = "1.2";
    /**
     * Version 1.1 (28.6.2013)
     * + information about version added
     * + link to project added into resulting file 
     */
    //public final static String ALGTEST_JCLIENT_VERSION_1_1 = "1.1";
    /**
     * Version 1.0 (27.11.2012)
     * + initial version of AlgTestJClient, clone of AlgTestCppClient
     */
    //public final static String ALGTEST_JCLIENT_VERSION_1_0 = "1.0";
 
    /**
     * Current version
     */
    public final static String ALGTEST_JCLIENT_VERSION = ALGTEST_JCLIENT_VERSION_1_7_3;
    
    public final static int STAT_OK = 0;    
    /**
     * @param args the command line arguments
     */
    
    static DirtyLogger m_SystemOutLogger = null;
    public static void main(String[] args) throws IOException, Exception {
        String logFileName = String.format("ALGTEST_log_%d.log", System.currentTimeMillis()); 
        FileOutputStream    systemOutLogger = new FileOutputStream(logFileName);
        m_SystemOutLogger = new DirtyLogger(systemOutLogger, true);
        
        m_SystemOutLogger.println("\n-----------------------------------------------------------------------   ");
        m_SystemOutLogger.println("JCAlgTest " + ALGTEST_JCLIENT_VERSION + " - comprehensive tool for JavaCard smart card testing.");
        m_SystemOutLogger.println("Visit jcalgtest.org for results from 60+ cards. CRoCS lab 2007-2017.");
        m_SystemOutLogger.println("Please check if you use the latest version at\n  https://github.com/crocs-muni/JCAlgTest/releases/latest.");
        
        m_SystemOutLogger.println("-----------------------------------------------------------------------\n");
        // If arguments are present. 
        if(args.length > 0){
            if (args[0].equals(ALGTEST_MULTIPERAPDU)){
                CardMngr cardManager = new CardMngr(m_SystemOutLogger);
                cardManager.testClassic(args, 0, null);
            }  
            else if (args[0].equals(ALGTEST_SINGLEPERAPDU)){
                SingleModeTest singleTest = new SingleModeTest(m_SystemOutLogger);
                singleTest.TestSingleAlg(args, null);
            }
            else if (args[0].equals(ALGTEST_PERFORMANCE)){
                PerformanceTesting testingPerformance = new PerformanceTesting(m_SystemOutLogger);
                testingPerformance.testPerformance(args, false, null);
            }
            // In case of incorect parameter, program will report error and shut down.
            else {
                System.err.println("Incorect parameter!");
                CardMngr.PrintHelp();
            }
        }
        // If there are no arguments present
        else {   
            CardTerminal selectedTerminal = null;
            PerformanceTesting testingPerformance = new PerformanceTesting(m_SystemOutLogger);
            m_SystemOutLogger.println("NOTE: JCAlgTest applet (AlgTest.cap) must be already installed on tested card.");
            m_SystemOutLogger.println("The results are stored in CSV files. Use JCAlgProcess for HTML conversion.");
            m_SystemOutLogger.println("CHOOSE test you want to run:");
            m_SystemOutLogger.println("1 -> SUPPORTED ALGORITHMS\n    List all supported JC API algorithms (2-10 minutes)\n" + 
                              "2 -> PERFORMANCE TEST\n    Test all JC API methods with 256B data length (1-3 hours)\n" + 
                              "3 -> PERFORMANCE VARIABLE DATA\n    Performance test with 16/32/64/128/256/512B data lengths (2-10 hours)\n" + 
                              "4 -> HARVEST RSA KEYS\n    Generate RSA keys on card, export to host and store to file (no limit)\n" + 
                              "5 -> FINGERPRINT\n    Performance measurement of selected methods for fingeprint (10 minutes)\n" +                              
                              "6 -> ECC PERFORMANCE\n    Performance measurement of eliptic curve operations (10 minutes)\n");
            m_SystemOutLogger.print("Test option number: ");
            Scanner sc = new Scanner(System.in);
            int answ = sc.nextInt();
            m_SystemOutLogger.println(String.format("%d", answ));
            switch (answ){
                // In this case, SinglePerApdu version of AlgTest is used.
                case 1:
                    selectedTerminal = selectTargetReader();
                    if (selectedTerminal != null) {
                        SingleModeTest singleTest = new SingleModeTest(m_SystemOutLogger);
                        singleTest.TestSingleAlg(args, selectedTerminal);
                    }
                    break;
                // In this case Performance tests are used. 
                case 2:
                    selectedTerminal = selectTargetReader();
                    if (selectedTerminal != null) {
                        testingPerformance.testPerformance(args, false, selectedTerminal);
                    }
                    break;
                case 3:
                    selectedTerminal = selectTargetReader();
                    if (selectedTerminal != null) {
                        testingPerformance.testPerformance(args, true, selectedTerminal);
                    }
                    break;
                case 4:
                    performKeyHarvest();
                    break;
                case 5:
                    selectedTerminal = selectTargetReader();
                    if (selectedTerminal != null) {
                        testingPerformance.testPerformanceFingerprint(args, selectedTerminal);
                    }
                    break;
                case 6:
                    selectedTerminal = selectTargetReader();
                    if (selectedTerminal != null) {
                        testingPerformance.testECCPerformance(args, true, selectedTerminal);
                    }
                    break;
                default:
                    // In this case, user pressed wrong key 
                    System.err.println("Incorrect parameter!");
                break;
            }
        
        }
    }
    
    static void performKeyHarvest() throws CardException {
        KeyHarvest keyHarvest = new KeyHarvest(m_SystemOutLogger);
        Scanner sc = new Scanner(System.in);
        // Remove new line character from stream after load integer as type of test
        //sc.nextLine();
        m_SystemOutLogger.print("\nUpload applet before harvest (y/n): ");
        String autoUploadBeforeString = sc.nextLine();
        m_SystemOutLogger.println(autoUploadBeforeString);
        boolean autoUploadBefore = false;
        if (autoUploadBeforeString.toLowerCase().equals("y")) {
            autoUploadBefore = true;
        } else if (!autoUploadBeforeString.toLowerCase().equals("n")) {
            m_SystemOutLogger.println("Wrong answer. Auto upload applet before harvest is disabled.");
        }

        m_SystemOutLogger.println("\nBit length of key to generate.");
        m_SystemOutLogger.println("Can be any number between 512 and 4096 bits (based on the card support)\n\t or range given as [start_bit_length:step_bits:end_bit_length]");
        m_SystemOutLogger.println("Example inputs: '2048' or '1024:32:2048'");
        m_SystemOutLogger.print("Bit length of key to generate: ");
        String bitLengthString = sc.nextLine();
        m_SystemOutLogger.println(bitLengthString);
        int acceptedInputs[] = {512, 736, 768, 896, 960, 1024, 1280, 1536, 1984, 2048, 3072, 4096};
        //short bitLength = JCConsts.KeyBuilder_LENGTH_RSA_512;
        short bitLength_start = JCConsts.KeyBuilder_LENGTH_RSA_512;
        short bitLength_step = (short) 0;
        short bitLength_end = JCConsts.KeyBuilder_LENGTH_RSA_512;
        try {
            // Detect range if submitted
            if (bitLengthString.contains(":")) {
                bitLengthString = bitLengthString.trim();
                String[] rangeVals = bitLengthString.split(":");
                int input = Integer.parseInt(rangeVals[0]);
                bitLength_start = (short) input;
                input = Integer.parseInt(rangeVals[1]);
                bitLength_step = (short) input;
                input = Integer.parseInt(rangeVals[2]);
                bitLength_end = (short) input;
            }
            else {
                // Single bit length submitted
                int input = Integer.parseInt(bitLengthString);
                boolean isAcceptedInput = false;
                for (int acceptedInput : acceptedInputs) {
                    if (input == acceptedInput) {
                        isAcceptedInput = true;
                        // Simulated range with single value only
                        bitLength_start = (short) acceptedInput;
                        bitLength_step = (short) 0;
                        bitLength_end = (short) acceptedInput;                        
                        break;
                    }
                }
                if (!isAcceptedInput) {
                    throw new NumberFormatException();
                }
            }
        } 
        catch (NumberFormatException ex) {
            m_SystemOutLogger.println("Wrong number. Bit length is set to " + bitLength_start + ".");
        }
        catch (PatternSyntaxException ex) {
            m_SystemOutLogger.println("Wrong range input. Correct format is '1024:32:2048'. Bit length is set to " + bitLength_start + ".");
        }

        m_SystemOutLogger.print("\nUse RSA harvest with CRT (y/n): ");
        String useCrtString = sc.nextLine();
        m_SystemOutLogger.println(useCrtString);
        boolean useCrt = false;
        if (useCrtString.toLowerCase().equals("y")) {
            useCrt = true;
        } else if (!useCrtString.toLowerCase().equals("n")) {
            m_SystemOutLogger.println("Wrong answer. CRT is disabled.");
        }

        // Check if folder !card_uploaders is correctly set
        if (autoUploadBeforeString.toLowerCase().equals("y")) {
            File fileCardUploadersFolder = new File(CardMngr.cardUploadersFolder);
            if (!fileCardUploadersFolder.exists()) {
                m_SystemOutLogger.println("Cannot find folder with card uploaders. Default folder: " + CardMngr.cardUploadersFolder);
                m_SystemOutLogger.print("Card uploaders folder path: ");
                String newPath = sc.nextLine();
                fileCardUploadersFolder = new File(CardMngr.cardUploadersFolder);
                // If new path is also incorrect
                if (!fileCardUploadersFolder.exists()) {
                    System.err.println("Folder " + newPath + " does not exist. Cannot start gathering RSA keys.");
                    return;
                }
                // Set new path to !card_uploaders folder
                CardMngr.cardUploadersFolder = newPath;
            }
        }
        
        m_SystemOutLogger.print("\nNumber of keys to generate: ");
        String numOfKeysString = sc.nextLine();
        m_SystemOutLogger.println(numOfKeysString);
        int numOfKeys = 10;
        try {
            numOfKeys = Integer.parseInt(numOfKeysString);
        } catch (NumberFormatException ex) {
            m_SystemOutLogger.println("Wrong number. Number of keys to generate is set to " + numOfKeys + ".");
        }

        keyHarvest.gatherRSAKeys(autoUploadBefore, bitLength_start, bitLength_step, bitLength_end, useCrt, numOfKeys);        
    }
    
    static CardTerminal selectTargetReader() {
        // Test available card - if more present, let user to select one
        List<CardTerminal> terminalList = CardMngr.GetReaderList(true);
        CardTerminal selectedTerminal = null;
        if (terminalList.isEmpty()) {
            m_SystemOutLogger.println("ERROR: No suitable reader with card detected. Please check your reader connection");
            return null;
        }
        else {
            if (terminalList.size() == 1) {
                selectedTerminal = terminalList.get(0); // return first and only reader
            }
            else {
                int terminalIndex = 1;
                // Let user select target terminal
                for (CardTerminal terminal : terminalList) {
                    Card card;
                    try {
                        card = terminal.connect("*");
                        ATR atr = card.getATR();
                        m_SystemOutLogger.println(terminalIndex + " : " + terminal.getName() + " - " + CardMngr.bytesToHex(atr.getBytes()));    
                        terminalIndex++;
                    } catch (CardException ex) {
                        Logger.getLogger(AlgTestJClient.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }   
                m_SystemOutLogger.print("Select index of target reader you like to use 1.." + (terminalIndex - 1) + ": ");
                Scanner sc = new Scanner(System.in);
                int answ = sc.nextInt();
                m_SystemOutLogger.println(String.format("%d", answ));
                answ--; // is starting with 0 
                // BUGBUG; verify allowed index range
                selectedTerminal = terminalList.get(answ); 
            }
        }
        
        return selectedTerminal;
    }
  
}
