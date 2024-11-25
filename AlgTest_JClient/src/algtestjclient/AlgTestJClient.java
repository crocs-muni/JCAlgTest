/*  
    Copyright (c) 2008-2021 Petr Svenda <petr@svenda.com>

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

import algtest.JCConsts;
import cardTools.SimulatedCardTerminal;
import com.beust.jcommander.JCommander;
import java.io.*;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;
import java.util.HashMap;
import java.util.Map;
import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardNotPresentException;
import javax.smartcardio.CardTerminal;

/**
 *
 * @author petr
 */
public class AlgTestJClient {
    /**
     * Version 1.8.2 (17.11.2024)
     * - Update to match applet version with delayed allocation by default 
     * - Add detailed info for submitting results at beginning  
     * - Add testing of ECC 640b keys
     * - Fix display of help info
     * - Add always send Le=256 to support certain cards (like Gemalto) expecting it
     * - Add sanity check for returned algtest buffer
     * - Improved used and fixed issues with CLI parameters  
     * - Fix missing JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT in results
     */
    public final static String ALGTEST_JCLIENT_VERSION = "1.8.2";
    /**
     * Version 1.8.1 (27.10.2021)
     * + Add better CLI control, interactive and non-interactive mode 
     * + Fixed incorrect names for measurements
     */
    //public final static String ALGTEST_JCLIENT_VERSION = "1.8.1";
    /**
     * Version 1.8.0 (19.12.2020)
     * + testing of modular Cipher and Signature .getInstance variants
     * + testing of OneShot variants
     * + testing of InitializedMessageDigest 
     */
    //public final static String ALGTEST_JCLIENT_VERSION_1_8_0 = "1.8.0";
    /**
     * Version 1.7.10 (29.11.2020)
     * + parsing memory overhead for object allocation
     * + added request for sending data to public database
     */
    //public final static String ALGTEST_JCLIENT_VERSION_1_7_10 = "1.7.10";
    /**
     * Version 1.7.9 (22.07.2019)
     * - no changes, updating version with card applet
     */
    //public final static String ALGTEST_JCLIENT_VERSION_1_7_9 = "1.7.9";
    /**
     * Version 1.7.8 (18.05.2019)
     * - no changes, updating version with card applet
     */
    //public final static String ALGTEST_JCLIENT_VERSION_1_7_8 = "1.7.8";
    /**
     * Version 1.7.7 (17.04.2019) 
     * - removed testing for high-power mode for SIM cards as some cards stop responding till reset 
     */
    //public final static String ALGTEST_JCLIENT_VERSION_1_7_7 = "1.7.7";
    /**
     * Version 1.7.6 (7.12.2018) 
     * + Added support for jCardSim
     * + Added testing of AEAD ciphers
     * + added attempt to enable high-power mode for SIM cards
     * - remove testing RSA arbitrary exponent (problem with J3H081 card)
     * - fixed bugs
     */
    //public final static String ALGTEST_JCLIENT_VERSION_1_7_6 = "1.7.6";

    /**
     * Version 1.7.5 (17.09.2018) 
     * - update of preallocated size of RAM (applet updated)
     */
    //public final static String ALGTEST_JCLIENT_VERSION_1_7_5 = "1.7.5";
    
    /**
     * Version 1.7.4 (20.04.2018) 
     * - fixed occasional freeze on some cards when testing MessageDigest performance
     */
    //public final static String ALGTEST_JCLIENT_VERSION_1_7_4 = "1.7.4";
    
    /**
     * Version 1.7.3 (10.06.2017) 
     * + added new constants from JC3.0.5
     * - fixed bug with incorrect testing of KeyAgreement 
     */
    //public final static String ALGTEST_JCLIENT_VERSION_1_7_3 = "1.7.3";

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
     
    public final static int STAT_OK = 0;    
    
    // Unique start time in milisconds
    static long m_appStartTime = 0;

    static Map<String, Map<String, String>> allResultsMap = new HashMap<>();

    
    /**
     * @param args the command line arguments
     */
    
    static DirtyLogger m_SystemOutLogger = null;
    public static void main(String[] args) throws IOException, Exception {
        Map<String, String> tempInfo = new HashMap<>();

        Args cmdArgs = new Args();
        if (args.length > 0) {
            // cli version of tool
            JCommander.newBuilder()
              .addObject(cmdArgs)
              .build()
              .parse(args); 

            if (!cmdArgs.baseOutPath.isEmpty()) {
                char lastChar = cmdArgs.baseOutPath.charAt(cmdArgs.baseOutPath.length() - 1);
                if ((lastChar != '\\') && (lastChar != '/')) {
                    cmdArgs.baseOutPath = cmdArgs.baseOutPath + "/";     // adding '/' if not present
                }                
            }
        }
        System.out.print("Command line arguments: ");
        for (int i = 0; i < args.length; i++) {
            System.out.print(args[i] + " ");
        }
        System.out.println();
        
        String logFileName = String.format(cmdArgs.baseOutPath + "ALGTEST_log_%s.log", AlgTestJClient.getStartTime()); 
        FileOutputStream    systemOutLogger = new FileOutputStream(logFileName);
        tempInfo.put("out_file_name", logFileName);
        allResultsMap.put("main", tempInfo);
        m_SystemOutLogger = new DirtyLogger(systemOutLogger, true);
        
        m_SystemOutLogger.println("\n-----------------------------------------------------------------------   ");
        m_SystemOutLogger.println("JCAlgTest " + ALGTEST_JCLIENT_VERSION + " - comprehensive tool for JavaCard smart card testing.");
        m_SystemOutLogger.println("Visit jcalgtest.org for results from 100+ cards. CRoCS lab 2007-2024.");
        m_SystemOutLogger.println("Please check if you use the latest version at\n  https://github.com/crocs-muni/JCAlgTest/releases/latest.");
        m_SystemOutLogger.println("Type 'java -jar jcalgtestclient --help' to display help and available commands.");
        m_SystemOutLogger.println("-----------------------------------------------------------------------\n");
        
        CardTerminal selectedTerminal = null;
        PerformanceTesting testingPerformance = new PerformanceTesting(m_SystemOutLogger);
        m_SystemOutLogger.println("NOTE: JCAlgTest applet (AlgTest.cap) must be already installed on tested card.");
        m_SystemOutLogger.println("  java -jar gp.jar --install AlgTest_***_jc***.cap");
        m_SystemOutLogger.println("The results are stored in CSV files. Use JCAlgProcess for HTML conversion.");
        m_SystemOutLogger.println();

        if (cmdArgs.help) {
            JCommander.newBuilder().addObject(cmdArgs).build().usage();     
            return;
        }

        // If selftest is enabled, then prepare testing session with simulator, execute and check for results
        if (cmdArgs.selftest) {
            cmdArgs.simulator = true;
            cmdArgs.fresh = true;
            cmdArgs.cardName = Args.SELFTEST_CARD_NAME;
            cmdArgs.operations.clear();
            cmdArgs.operations.add(Args.OP_ALG_SUPPORT_EXTENDED);
            cmdArgs.operations.add(Args.OP_ALG_ECC_PERFORMANCE);
            cmdArgs.operations.add(Args.OP_ALG_FINGERPRINT);
            cmdArgs.operations.add(Args.OP_ALG_PERFORMANCE_STATIC);
            cmdArgs.operations.add(Args.OP_ALG_PERFORMANCE_VARIABLE);
        }

        printSendRequest();
        if (cmdArgs.operations.size() > 0) {
            m_SystemOutLogger.println("Running in non-interactive mode. Run without any parameter to enter interactive mode. Run 'java -jar AlgTestJClient.jar --help' to obtain list of supported arguments.");
            for (String operation : cmdArgs.operations) {
                if (operation.compareTo(Args.OP_ALG_SUPPORT_BASIC) == 0 || 
                    operation.compareTo(Args.OP_ALG_SUPPORT_EXTENDED) == 0) {
                    selectedTerminal = selectTargetReader(cmdArgs);
                    if (selectedTerminal != null) {
                        SingleModeTest singleTest = new SingleModeTest(m_SystemOutLogger);
                        Map<String, String> testResults = singleTest.TestSingleAlg(operation, cmdArgs, selectedTerminal);
                        allResultsMap.put(operation, testResults);
                    }
                }
                else if (operation.compareTo(Args.OP_ALG_PERFORMANCE_STATIC) == 0 || 
                    operation.compareTo(Args.OP_ALG_PERFORMANCE_VARIABLE) == 0) {
                    selectedTerminal = selectTargetReader(cmdArgs);
                    if (selectedTerminal != null) {
                        Map<String, String> testResults = testingPerformance.testPerformance(false, operation, selectedTerminal, cmdArgs);
                        allResultsMap.put(operation, testResults);
                    }
                }
                else if (operation.compareTo(Args.OP_ALG_ECC_PERFORMANCE) == 0) {
                    selectedTerminal = selectTargetReader(cmdArgs);
                    if (selectedTerminal != null) {
                        Map<String, String> testResults = testingPerformance.testECCPerformance(args, true, selectedTerminal, cmdArgs);
                        allResultsMap.put(operation, testResults);
                    }
                }
                else if (operation.compareTo(Args.OP_ALG_FINGERPRINT) == 0) {
                    selectedTerminal = selectTargetReader(cmdArgs);
                    if (selectedTerminal != null) {
                        Map<String, String> testResults = testingPerformance.testPerformanceFingerprint(args, selectedTerminal, cmdArgs);
                        allResultsMap.put(operation, testResults);
                    }
                }
                else {
                    m_SystemOutLogger.println("ERROR: unknown option " + operation);
                }
                
            }
        }
        else {
            // Interactive mode of tool
            m_SystemOutLogger.println("Running in interactive mode. Run 'java -jar AlgTestJClient.jar --help' to obtain list of supported arguments.");
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
            switch (answ) {
                // In this case, SinglePerApdu version of AlgTest is used.
                case 1:
                    selectedTerminal = selectTargetReader(cmdArgs);
                    if (selectedTerminal != null) {
                        SingleModeTest singleTest = new SingleModeTest(m_SystemOutLogger);
                        singleTest.TestSingleAlg(Args.OP_ALG_SUPPORT_EXTENDED, cmdArgs, selectedTerminal);
                    }
                    break;
                // In this case Performance tests are used. 
                case 2:
                    selectedTerminal = selectTargetReader(cmdArgs);
                    if (selectedTerminal != null) {
                        testingPerformance.testPerformance(true, Args.OP_ALG_PERFORMANCE_STATIC, selectedTerminal, cmdArgs);
                    }
                    break;
                case 3:
                    selectedTerminal = selectTargetReader(cmdArgs);
                    if (selectedTerminal != null) {
                        testingPerformance.testPerformance(true, Args.OP_ALG_PERFORMANCE_VARIABLE, selectedTerminal, cmdArgs);
                    }
                    break;
                case 4:
                    performKeyHarvest(cmdArgs);
                    break;
                case 5:
                    selectedTerminal = selectTargetReader(cmdArgs);
                    if (selectedTerminal != null) {
                        testingPerformance.testPerformanceFingerprint(args, selectedTerminal, cmdArgs);
                    }
                    break;
                case 6:
                    selectedTerminal = selectTargetReader(cmdArgs);
                    if (selectedTerminal != null) {
                        testingPerformance.testECCPerformance(args, true, selectedTerminal, cmdArgs);
                    }
                    break;
                default:
                    // In this case, user pressed wrong key 
                    System.err.println("Incorrect parameter!");
                break;
            }
        }
        printSendRequest();

        if (cmdArgs.selftest) {
            checkSelfTestResults(allResultsMap);
        }   
    }
    
    static long getStartTime() {
        if (m_appStartTime == 0) {
            m_appStartTime = System.currentTimeMillis();
        }
        return m_appStartTime;
    }
    
    static void printSendRequest() {
        System.out.println("\n-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*");
        System.out.println("KIND REQUEST: Please consider sending us your results to extend info openly");
        System.out.println("available to all JavaCard enthusiasts at http://jcalgtest.org.");
        System.out.println("The results are important even if a card of same type is already in database.");
        System.out.println("Send *.log and *.csv files from the current directory to <petr@svenda.com>.");
        System.out.println("ESPECIALLY if testing fails, please let us know so we can fix it for you and others.");
        System.out.println("Thank you very much.");
        System.out.println("-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*\n");
    }
    
    static void performKeyHarvest(Args cmdArgs) throws CardException {
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
                        bitLength_step = (short) 1;
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
            m_SystemOutLogger.println("Ìncorrect answer. CRT is not used.");
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

        keyHarvest.gatherRSAKeys(autoUploadBefore, bitLength_start, bitLength_step, bitLength_end, useCrt, numOfKeys, cmdArgs.simulator);        
    }
    
    static CardTerminal selectTargetReader(Args cmdArgs) {
        // If required, return simulated card (jCardSim), otherwise let user to choose
        if (cmdArgs.simulator) {
            return new SimulatedCardTerminal();
        }
        else {
            // Test available card - if more present, let user to select one
            List<CardTerminal> terminalList = CardMngr.GetReaderList(false);
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
                    m_SystemOutLogger.println("\nAvailable readers:");
                    for (CardTerminal terminal : terminalList) {
                        Card card;
                        String protocol = System.getenv().getOrDefault("ALGTEST_PROTO", "*");
                        try {
                            card = terminal.connect(protocol);
                            //card = terminal.connect("*");
                            ATR atr = card.getATR();
                            m_SystemOutLogger.println(String.format("%d : [*] %s - %s", terminalIndex, terminal.getName(), CardMngr.bytesToHex(atr.getBytes())));
                            terminalIndex++;                        
                        } catch (CardNotPresentException ex) {
                            m_SystemOutLogger.println(String.format("%d : [ ] %s - NO CARD", terminalIndex, terminal.getName()));
                            terminalIndex++;
                        } catch (CardException ex) {
                            Logger.getLogger(AlgTestJClient.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }   
                    m_SystemOutLogger.print("Select index of target reader you like to use [1.." + (terminalIndex - 1) + "]: ");
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
  
    private static boolean checkFileExistence(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            m_SystemOutLogger.println(String.format("    OK: the file '%s' exists.", fileName));
        } else {
            m_SystemOutLogger.println(String.format("    ERROR: the file '%s' does not exist.", fileName));
        }
        return file.exists();
    } 

    private static boolean checkExpectedValue(Map<String, Map<String, String>> allResults, String op, String key, String expectedValue) {
        if (allResults.get(op).get(key).equalsIgnoreCase(expectedValue)){
            m_SystemOutLogger.println(String.format("    OK: %s->%s = %s matches.", op, key, expectedValue));
        } else {
            m_SystemOutLogger.println(String.format("    ERROR: %s->%s = %s mismatch (expected=%s).", op, key, allResults.get(op).get(key), expectedValue));
        }
        return allResults.get(op).get(key).equalsIgnoreCase(expectedValue);
    } 

    public static boolean checkSelfTestResults(Map<String, Map<String, String>> allResults) {
        boolean bSelftestSuccess = true;
        // Print all collected info keys
        m_SystemOutLogger.println("###### SELFTEST ####################################");
        m_SystemOutLogger.println("All collected selftest results:");
        for (String operation : allResults.keySet()) { 
            m_SystemOutLogger.println(String.format("  %s:", operation));
            Map<String, String> oneOpResults = allResults.get(operation);
            for (String key : oneOpResults.keySet()) { 
                m_SystemOutLogger.println(String.format("    %s = %s", key, oneOpResults.get(key)));
            }    
        }

        // Check existence of output files for separate operations
        m_SystemOutLogger.println("\nSelftest tests finished, now checking results...");
        for (String op : allResults.keySet()) { 
            m_SystemOutLogger.println(String.format("  Checking operation '%s':", op));
            // Always check existence of output file
            if (!checkFileExistence(allResults.get(op).get("out_file_name"))) { bSelftestSuccess = false; }
            // Check selected values for selected results
            if (op.equals(Args.OP_ALG_PERFORMANCE_STATIC)) {
                if (!checkExpectedValue(allResults, op, "NO_SUCH_ALGORITHM", "290")) { bSelftestSuccess = false; }   
                if (!checkExpectedValue(allResults, op, "CANT_BE_MEASURED", "204")) { bSelftestSuccess = false; }   
                if (!checkExpectedValue(allResults, op, "ILLEGAL_VALUE", "66")) { bSelftestSuccess = false; }
                if (!checkExpectedValue(allResults, op, "errors_observed", "560")) { bSelftestSuccess = false; }   
            }   
            if (op.equals(Args.OP_ALG_PERFORMANCE_VARIABLE)) {
                if (!checkExpectedValue(allResults, op, "NO_SUCH_ALGORITHM", "145")) { bSelftestSuccess = false; }   
                if (!checkExpectedValue(allResults, op, "CANT_BE_MEASURED", "327")) { bSelftestSuccess = false; }   
                if (!checkExpectedValue(allResults, op, "ILLEGAL_VALUE", "35")) { bSelftestSuccess = false; }
                if (!checkExpectedValue(allResults, op, "errors_observed", "507")) { bSelftestSuccess = false; }   
            } 
            if (op.equals(Args.OP_ALG_FINGERPRINT)) {
                if (!checkExpectedValue(allResults, op, "NO_SUCH_ALGORITHM", "1")) { bSelftestSuccess = false; }   
                if (!checkExpectedValue(allResults, op, "CANT_BE_MEASURED", "18")) { bSelftestSuccess = false; }   
                if (!checkExpectedValue(allResults, op, "ILLEGAL_VALUE", "8")) { bSelftestSuccess = false; }
                if (!checkExpectedValue(allResults, op, "errors_observed", "27")) { bSelftestSuccess = false; }   
            } 
            if (op.equals(Args.OP_ALG_ECC_PERFORMANCE)) {
                if (!checkExpectedValue(allResults, op, "errors_observed", "0")) { bSelftestSuccess = false; }   
            } 
        }
        if (!bSelftestSuccess) {
            m_SystemOutLogger.println("ERROR: some test(s) failed");
        }
        m_SystemOutLogger.println("###### END SELFTEST ####################################");

        return bSelftestSuccess;    
    }

}
