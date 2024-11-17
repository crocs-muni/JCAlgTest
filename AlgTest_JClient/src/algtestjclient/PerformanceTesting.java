/*  
    Copyright (c) 2008-2024 Petr Svenda <petr@svenda.com>

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

import java.io.*;
import java.io.FileOutputStream;
import java.util.Scanner;
import javax.smartcardio.ResponseAPDU;
import algtest.Consts;
import algtest.JCAlgTestApplet;
import algtest.JCConsts;
import algtest.TestSettings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javacard.framework.ISO7816;
import javax.smartcardio.CardTerminal;

public class PerformanceTesting {
    
    // Argument constants for choosing algorithm to test. 
    public static final String TEST_ALL_ALGORITHMS = "ALL_ALGS";
    
    public static final String TEST_EEPROM = "EEPROM";
    public static final String TEST_RAM = "RAM";
    public static final String TEST_EXTENDEDAPDU = "EXTENDEDAPDU";
    public static final String TEST_RSAEXPONENT = "RSAEXPONENT";
    
    // Generic test constants
    public static final int MAX_FAILED_REPEATS = 3;
    public static final int NUM_BASELINE_CALIBRATION_RUNS = 5;
    
    // 
    public CardMngr m_cardManager = null;
    String m_cardATR = "";
    String m_cardName = "";
    long   m_elapsedTimeWholeTest = 0;
    long   m_numHumanInterventions = 0;
    long   m_numReconnects = 0;
    
    //public static final byte mask = 0b01111111;
    public FileOutputStream m_perfResultsFile;
    public FileOutputStream m_algsMeasuredFile;
    public List<String> m_algsMeasuredList = new ArrayList<>();
    public HashMap<String, double[]> m_algsAvgTime = new HashMap<>();
    public boolean m_bAlgsMeasuredSomeNew = false;
    public HashMap<String, ArrayList<String>> m_errorsObserved = new HashMap<>();
    
    private boolean m_bTestSymmetricAlgs = true;
    private boolean m_bTestAsymmetricAlgs = true;
    public boolean m_bTestVariableData = false;
    
    public List<Integer> m_testDataLengths = new ArrayList<>();
    
    DirtyLogger m_SystemOutLogger = null;

    public PerformanceTesting(DirtyLogger logger) {
        m_SystemOutLogger = logger;
        m_cardManager = new CardMngr(m_SystemOutLogger);
        // data lengths to be tested (only for variable data length test)
        m_testDataLengths.add(16);
        m_testDataLengths.add(32);
        m_testDataLengths.add(64);
        m_testDataLengths.add(128);
        m_testDataLengths.add(256);
        m_testDataLengths.add(512);        
    }
    
    String getCurrentTestInfoString(boolean bAppendTime) {
        String testInfo = m_cardName + "___";
        testInfo += "_PERFORMANCE_";

        if (m_bTestSymmetricAlgs) {
            testInfo += "SYMMETRIC_";
        }
        if (m_bTestAsymmetricAlgs) {
            testInfo += "ASYMMETRIC_";
        }
        if (m_bTestVariableData) {
            testInfo += "DATADEPEND_";
        } else {
            testInfo += "DATAFIXED_";
        }

        if (bAppendTime) {
            testInfo += AlgTestJClient.getStartTime() + "_";   // add unique time counter 
        }
        
        return testInfo;
    }
    
    String requestCardName(Scanner sc, Args cmdArgs) {
        String cardName;
        if (cmdArgs.cardName.isEmpty()) {
            m_SystemOutLogger.print("Specify type of your card (e.g., NXP JCOP CJ2A081): ");
            cardName = sc.next();
            cardName += sc.nextLine();
            if (cardName.isEmpty()) {
                cardName = "noname";
            }        
        } else {
          cardName = cmdArgs.cardName;  
        }
        
        return cardName;
    }
    /**
     * Calls methods testing card performance.
     * @param args
     * @param bTestVariableDataLengths
     * @param selectedTerminal
     * @throws IOException
     * @throws Exception
     */        
    public void testPerformance(boolean bInteractive, String operation, CardTerminal selectedTerminal, Args cmdArgs) throws IOException, Exception{
        Scanner sc = new Scanner(System.in);
        
        if (operation.compareTo(Args.OP_ALG_PERFORMANCE_STATIC) == 0) { m_bTestVariableData = false; }
        if (operation.compareTo(Args.OP_ALG_PERFORMANCE_VARIABLE) == 0) { m_bTestVariableData = true; }
        
                
        if (bInteractive) {
            m_SystemOutLogger.println("\nCHOOSE which type of performance test you like to execute:");
            m_SystemOutLogger.println("1 -> All algorithms (estimated time 5-8 hours)\n" + 
                    "2 -> Only algorithms WITHOUT asymmetric cryptography (estimated time 1-2 hours)\n" + 
                    "3 -> Only algorithms WITH asymmetric crypto (estimated time 4-6 hours)");
            m_SystemOutLogger.print("Test option number: ");
            int answ = sc.nextInt();
            m_SystemOutLogger.println(String.format("%d", answ));
            switch (answ){
                case 1:
                    m_bTestSymmetricAlgs = true;
                    m_bTestAsymmetricAlgs = true;
                    break;
                case 2:
                    m_bTestSymmetricAlgs = true;
                    m_bTestAsymmetricAlgs = false;
                    break;
                case 3:
                    m_bTestSymmetricAlgs = false;
                    m_bTestAsymmetricAlgs = true;
                    break;
                default:
                    System.err.println("Incorrect parameter, running all tests!");
                    break;
            }
        }
        else {
            // Measure both options
            m_bTestSymmetricAlgs = true;
            m_bTestAsymmetricAlgs = true;
        }
                
        // Set number of operation repeats
        int numRepeatWholeOperation = Consts.NUM_REPEAT_WHOLE_OPERATION;
        if (m_bTestVariableData) {
            numRepeatWholeOperation = Consts.NUM_REPEAT_WHOLE_OPERATION_VARIABLE_DATA;                
        }

        m_cardName = requestCardName(sc, cmdArgs);

        // Try to open and load list of already measured algorithms (if provided)
        String testType = getCurrentTestInfoString(false);
        // Interactive mode allows to continue with previous measurement if found (needs to be confirmed), cmdArgs.fresh can force it to start new measurement
        boolean bForceFreshMeasurement = bInteractive ? false : cmdArgs.fresh; 
        LoadAlreadyMeasuredAlgs(m_cardName, testType, bForceFreshMeasurement);

        String testInfo = getCurrentTestInfoString(true);
        
        m_elapsedTimeWholeTest = -System.currentTimeMillis();
        
        // Connect to card
        this.m_perfResultsFile = m_cardManager.establishConnection(m_cardName, testInfo, selectedTerminal, cmdArgs);
        m_cardATR = m_cardManager.getATR();

        // Run all required tests
        testAllMessageDigests(numRepeatWholeOperation, Consts.NUM_REPEAT_WHOLE_MEASUREMENT);
        testAllRandomGenerators(numRepeatWholeOperation, Consts.NUM_REPEAT_WHOLE_MEASUREMENT);
        testAllCiphers(numRepeatWholeOperation, Consts.NUM_REPEAT_WHOLE_MEASUREMENT);
        testAllSignatures(numRepeatWholeOperation, Consts.NUM_REPEAT_WHOLE_MEASUREMENT);
        testAllChecksums(numRepeatWholeOperation, Consts.NUM_REPEAT_WHOLE_MEASUREMENT);     
        testAllKeys(numRepeatWholeOperation, Consts.NUM_REPEAT_WHOLE_MEASUREMENT);
        testAllUtil(numRepeatWholeOperation, Consts.NUM_REPEAT_WHOLE_MEASUREMENT);
        testAllSWAlgs(numRepeatWholeOperation, Consts.NUM_REPEAT_WHOLE_MEASUREMENT);
        testAllKeyPairs(1, Consts.NUM_REPEAT_WHOLE_MEASUREMENT_KEYPAIRGEN);     // for keypair, different repeat settings is used
        testAllKeyAgreement(10, Consts.NUM_REPEAT_WHOLE_MEASUREMENT);           // for KeyAgrement, different repeat settings is used
        finalizeMeasurement();
    }
    
    /**
     * Calls methods testing card performance limited to fingerprinting functions.
     *
     * @param args
     * @param selectedTerminal
     * @throws IOException
     * @throws Exception
     */
    public void testPerformanceFingerprint(String[] args, CardTerminal selectedTerminal, Args cmdArgs) throws IOException, Exception {
        Scanner sc = new Scanner(System.in);

        // Fingeprint wants to test only main operation speed => variable data option with 256B only
        m_bTestVariableData = true;
        m_testDataLengths.clear(); 
        m_testDataLengths.add(256);
        
        m_bTestSymmetricAlgs = true;
        m_bTestAsymmetricAlgs = true;

        m_cardName = requestCardName(sc, cmdArgs);

        // Try to open and load list of already measured algorithms (if provided)
        // NOTE: measure always full: LoadAlreadyMeasuredAlgs(m_cardName);
        String testInfo = m_cardName + "___";
        testInfo += "_FINGERPRINT_";
        testInfo += System.currentTimeMillis() + "_";   // add unique time counter 
        
        m_elapsedTimeWholeTest = -System.currentTimeMillis();
        // Connect to card
        this.m_perfResultsFile = m_cardManager.establishConnection(m_cardName, testInfo, selectedTerminal,cmdArgs);
        m_cardATR = m_cardManager.getATR();

        short numRepeatWholeMeasurement = (short) 3;
        short numRepeatWholeOperation = (short) 3;
        
        // Run all tests selected for fingeprinting
        testAllMessageDigests(numRepeatWholeOperation, numRepeatWholeMeasurement);
        testAllRandomGenerators(numRepeatWholeOperation, numRepeatWholeOperation);
        
        //
        // Cipher
        //
        testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES, JCConsts.Cipher_ALG_DES_CBC_NOPAD, "TYPE_DES LENGTH_DES ALG_DES_CBC_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_3KEY, JCConsts.Cipher_ALG_DES_CBC_NOPAD, "TYPE_DES LENGTH_DES3_3KEY ALG_DES_CBC_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_128, JCConsts.Cipher_ALG_AES_BLOCK_128_CBC_NOPAD, "TYPE_AES LENGTH_AES_128 ALG_AES_BLOCK_128_CBC_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256, JCConsts.Cipher_ALG_AES_BLOCK_128_CBC_NOPAD, "TYPE_AES LENGTH_AES_256 ALG_AES_BLOCK_128_CBC_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_512, JCConsts.Cipher_ALG_AES_BLOCK_128_CBC_NOPAD, "TYPE_AES LENGTH_AES_512 ALG_AES_BLOCK_128_CBC_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024, JCConsts.Cipher_ALG_RSA_NOPAD, "TYPE_RSA_PRIVATE LENGTH_RSA_1024 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048, JCConsts.Cipher_ALG_RSA_NOPAD, "TYPE_RSA_PRIVATE LENGTH_RSA_2048 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096, JCConsts.Cipher_ALG_RSA_NOPAD, "TYPE_RSA_PRIVATE LENGTH_RSA_4096 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024, JCConsts.Cipher_ALG_RSA_NOPAD, "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1024 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048, JCConsts.Cipher_ALG_RSA_NOPAD, "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_2048 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096, JCConsts.Cipher_ALG_RSA_NOPAD, "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_4096 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1024, JCConsts.Cipher_ALG_RSA_NOPAD, "TYPE_RSA_PUBLIC LENGTH_RSA_1024 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_2048, JCConsts.Cipher_ALG_RSA_NOPAD, "TYPE_RSA_PUBLIC LENGTH_RSA_2048 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_4096, JCConsts.Cipher_ALG_RSA_NOPAD, "TYPE_RSA_PUBLIC LENGTH_RSA_4096 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
        
        // Ask for free of RSA keys cache
        m_cardManager.resetApplet(Consts.CLA_CARD_ALGTEST, Consts.INS_CARD_RESET, Consts.P1_CARD_RESET_FREE_CACHE);

        //
        // Signature
        //
        // ALG_EC_F2M
        testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_F2M, JCConsts.KeyBuilder_TYPE_EC_F2M_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_F2M_113, JCConsts.Signature_ALG_ECDSA_SHA, "KeyPair_ALG_EC_F2M KeyBuilder_LENGTH_EC_F2M_113 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_F2M, JCConsts.KeyBuilder_TYPE_EC_F2M_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_F2M_193, JCConsts.Signature_ALG_ECDSA_SHA, "KeyPair_ALG_EC_F2M KeyBuilder_LENGTH_EC_F2M_193 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
        // ALG_EC_FP
        testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_128, JCConsts.Signature_ALG_ECDSA_SHA, "KeyPair_ALG_EC_FP KeyBuilder_LENGTH_EC_FP_128 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_192, JCConsts.Signature_ALG_ECDSA_SHA, "KeyPair_ALG_EC_FP KeyBuilder_LENGTH_EC_FP_192 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_256, JCConsts.Signature_ALG_ECDSA_SHA, "KeyPair_ALG_EC_FP KeyBuilder_LENGTH_EC_FP_256 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_320, JCConsts.Signature_ALG_ECDSA_SHA, "KeyPair_ALG_EC_FP KeyBuilder_LENGTH_EC_FP_320 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_384, JCConsts.Signature_ALG_ECDSA_SHA, "KeyPair_ALG_EC_FP KeyBuilder_LENGTH_EC_FP_384 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_512, JCConsts.Signature_ALG_ECDSA_SHA, "KeyPair_ALG_EC_FP KeyBuilder_LENGTH_EC_FP_512 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_521, JCConsts.Signature_ALG_ECDSA_SHA, "KeyPair_ALG_EC_FP KeyBuilder_LENGTH_EC_FP_521 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
        // DSA
        testSignatureWithKeyClass(JCConsts.KeyPair_ALG_DSA, JCConsts.KeyBuilder_TYPE_DSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_DSA_512, JCConsts.Signature_ALG_DSA_SHA, "ALG_DSA LENGTH_DSA_512 ALG_DSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testSignatureWithKeyClass(JCConsts.KeyPair_ALG_DSA, JCConsts.KeyBuilder_TYPE_DSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_DSA_1024, JCConsts.Signature_ALG_DSA_SHA, "ALG_DSA LENGTH_DSA_1024 ALG_DSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
        
        // Checksums
        testAllChecksums(numRepeatWholeOperation, Consts.NUM_REPEAT_WHOLE_MEASUREMENT);

        testAllSWAlgs(numRepeatWholeOperation, Consts.NUM_REPEAT_WHOLE_MEASUREMENT);

        // Several keypair generations to measure time distribution
        numRepeatWholeMeasurement = Consts.NUM_REPEAT_WHOLE_MEASUREMENT_KEYPAIRGEN;
        numRepeatWholeOperation = (short) 1;
        testKeyPair(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_1024, "ALG_RSA LENGTH_RSA_1024", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testKeyPair(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_1024, "ALG_RSA_CRT LENGTH_RSA_1024", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testKeyPair(JCConsts.KeyPair_ALG_DSA, JCConsts.KeyBuilder_LENGTH_DSA_512, "ALG_DSA LENGTH_DSA_512", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testKeyPair(JCConsts.KeyPair_ALG_EC_F2M, JCConsts.KeyBuilder_LENGTH_EC_F2M_163, "ALG_EC_F2M LENGTH_EC_F2M_163", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testKeyPair(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_LENGTH_EC_FP_192, "ALG_EC_FP LENGTH_EC_FP_192", numRepeatWholeOperation, numRepeatWholeMeasurement);

        finalizeMeasurement();
    }    
    
    /**
     * Calls methods testing card performance limited to fingerprinting
     * functions.
     *
     * @param args
     * @param selectedTerminal
     * @throws IOException
     * @throws Exception
     */
    public void testDebug(String[] args, CardTerminal selectedTerminal, Args cmdArgs) throws IOException, Exception {
        Scanner sc = new Scanner(System.in);

        // Fingeprint wants to test only main operation speed => variable data option with 256B only
        m_bTestVariableData = true;
        m_testDataLengths.clear();
        m_testDataLengths.add(256);

        m_bTestSymmetricAlgs = true;
        m_bTestAsymmetricAlgs = true;

        m_cardName = "debugcard";

        // Try to open and load list of already measured algorithms (if provided)
        // NOTE: measure always full: LoadAlreadyMeasuredAlgs(m_cardName);
        String testInfo = m_cardName + "___";
        testInfo += "_DEBUG_";
        testInfo += System.currentTimeMillis() + "_";   // add unique time counter 

        m_elapsedTimeWholeTest = -System.currentTimeMillis();
        // Connect to card
        this.m_perfResultsFile = m_cardManager.establishConnection(m_cardName, testInfo, selectedTerminal, cmdArgs);
        m_cardATR = m_cardManager.getATR();

        short numRepeatWholeMeasurement = (short) 1;
        short numRepeatWholeOperation = (short) 1;

        // Debug exception ff01 during preparation - FIXED
        //testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_512, JCConsts.Cipher_ALG_RSA_ISO14888, "TYPE_RSA_CRT_PUBLIC LENGTH_RSA_512 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
        //testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_512, JCConsts.Cipher_ALG_RSA_PKCS1, "TYPE_RSA_CRT_PUBLIC LENGTH_RSA_512 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);

/*        
        // Debug problem on Taysis card 
        // 1024b OK
        testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024, JCConsts.Cipher_ALG_RSA_NOPAD, "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1024 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
        // 1984b fails during use UNKONWN_ERROR-card_has_return_value_6f00
        testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984, JCConsts.Cipher_ALG_RSA_NOPAD, "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1984 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
        // 2048b and longer fails with preparation 01 90 00<< ILLEGAL_VALUE
        testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048, JCConsts.Cipher_ALG_RSA_NOPAD, "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_2048 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
        //testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048, JCConsts.Cipher_ALG_RSA_NOPAD, "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_2048 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
        //testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096, JCConsts.Cipher_ALG_RSA_NOPAD, "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_4096 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
        //testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096, JCConsts.Cipher_ALG_RSA_NOPAD, "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_4096 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
*/

        // Debug 0xff01 error when switching from 1.6.0 to 1.7.0
        testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1024, JCConsts.Cipher_ALG_RSA_NOPAD, "TYPE_RSA_CRT_PUBLIC LENGTH_RSA_1024 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
        //testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1024, JCConsts.Cipher_ALG_RSA_PKCS1, "TYPE_RSA_CRT_PUBLIC LENGTH_RSA_1024 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);

        finalizeMeasurement();
    }    
    
    
    
    /**
     * Calls methods testing card performance on ECC operations.
     *
     * @param args
     * @param bTestVariableDataLengths
     * @param selectedTerminal
     * @throws IOException
     * @throws Exception
     */
    public void testECCPerformance(String[] args, boolean bTestVariableDataLengths, CardTerminal selectedTerminal, Args cmdArgs) throws IOException, Exception {
        // ECC wants to test only main operation speed => variable data option with 256B only
        m_bTestVariableData = false;
        m_bTestSymmetricAlgs = true;
        m_bTestAsymmetricAlgs = true;

        Scanner sc = new Scanner(System.in);
        m_cardName = requestCardName(sc, cmdArgs);

        String testInfo = m_cardName + "___";
        testInfo += "_ECCPERF_";
        testInfo += System.currentTimeMillis() + "_";   // add unique time counter 

        m_elapsedTimeWholeTest = -System.currentTimeMillis();
        // Connect to card
        this.m_perfResultsFile = m_cardManager.establishConnection(m_cardName, testInfo, selectedTerminal, cmdArgs);
        m_cardATR = m_cardManager.getATR();

        short numRepeatWholeMeasurement = (short) 3;
        short numRepeatWholeOperation = (short) 3;
        
        testKeyAgreementWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_256, JCConsts.KeyAgreement_ALG_EC_SVDP_DH_PLAIN, "ALG_EC_FP LENGTH_EC_FP_256 ALG_EC_SVDP_DH_PLAIN", numRepeatWholeOperation, numRepeatWholeMeasurement);
        // Test speed of message digest - applied in some options of KeyAgreement.generateSecret()
        testRandomGenerator(JCConsts.RandomData_ALG_SECURE_RANDOM, "ALG_SECURE_RANDOM", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_256, JCConsts.Signature_ALG_ECDSA_SHA, "KeyPair_ALG_EC_FP KeyBuilder_LENGTH_EC_FP_256 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testKeyPair(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_LENGTH_EC_FP_256, "ALG_EC_FP LENGTH_EC_FP_256", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testKeyAgreementWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_256, JCConsts.KeyAgreement_ALG_EC_SVDP_DH, "ALG_EC_FP LENGTH_EC_FP_256 ALG_EC_SVDP_DH", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testKeyAgreementWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_256, JCConsts.KeyAgreement_ALG_EC_SVDP_DH_PLAIN, "ALG_EC_FP LENGTH_EC_FP_256 ALG_EC_SVDP_DH_PLAIN", numRepeatWholeOperation, numRepeatWholeMeasurement);
        //testKeyAgreementWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_256, JCConsts.KeyAgreement_ALG_EC_PACE_GM, "ALG_EC_FP LENGTH_EC_FP_256 ALG_EC_PACE_GM", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testKeyAgreementWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_256, JCConsts.KeyAgreement_ALG_EC_SVDP_DH_PLAIN_XY, "ALG_EC_FP LENGTH_EC_FP_256 ALG_EC_SVDP_DH_PLAIN_XY", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testMessageDigest(JCConsts.MessageDigest_ALG_SHA_256, "ALG_SHA_256", numRepeatWholeOperation, numRepeatWholeMeasurement);

    
        for (String opName : m_algsAvgTime.keySet()) {
            double[] measuredTimes = m_algsAvgTime.get(opName);
            m_SystemOutLogger.print(String.format("%s : \t avg=%4.1f,\t[", opName, measuredTimes[0]));
            for (int i = 1; i < measuredTimes.length; i++) {
                m_SystemOutLogger.print(String.format("%.1f,", measuredTimes[i]));
            }
            m_SystemOutLogger.println("]");
        }
        
        ArrayList<String> wantedOps = new ArrayList<>();
        wantedOps.add("KeyPair_ALG_EC_FP KeyBuilder_LENGTH_EC_FP_256 Signature_ALG_ECDSA_SHA Signature_sign()");
        wantedOps.add("KeyPair_ALG_EC_FP KeyBuilder_LENGTH_EC_FP_256 Signature_ALG_ECDSA_SHA Signature_verify()");
        wantedOps.add("ALG_EC_FP LENGTH_EC_FP_256 ALG_EC_SVDP_DH KeyAgreement_generateSecret()");
        wantedOps.add("ALG_EC_FP LENGTH_EC_FP_256 ALG_EC_SVDP_DH_PLAIN KeyAgreement_generateSecret()");
        wantedOps.add("ALG_EC_FP LENGTH_EC_FP_256 ALG_EC_SVDP_DH_PLAIN_XY KeyAgreement_generateSecret()");
        wantedOps.add("ALG_EC_FP LENGTH_EC_FP_256 KeyPair_genKeyPair()");
        
        // Generate CSV line
        String csvHeader = "cardName, ";
        String csvLine = String.format("%s, ", m_cardName);
        for (String wantedOp : wantedOps) {
            double[] measuredTimes = m_algsAvgTime.get(wantedOp);
            csvHeader += String.format("%s, ", wantedOp);
            if (measuredTimes != null) {
                csvLine += String.format("%4.1f, ", measuredTimes[0]); // supported, store time
            }
            else {
                csvLine += "-1, "; // not supported
            }
        }
        
        m_SystemOutLogger.println(csvHeader);
        m_SystemOutLogger.println(csvLine);
        
        finalizeMeasurement();
    }

    void finalizeMeasurement() throws IOException {
        m_elapsedTimeWholeTest += System.currentTimeMillis();
        String message = "";
        message += "\n\nTotal test time:; " + m_elapsedTimeWholeTest / 1000 + " seconds."; 
        m_SystemOutLogger.println(message);
        m_perfResultsFile.write(message.getBytes());
        message = "\n\nTotal human interventions (retries with physical resets etc.):; " + m_numHumanInterventions; 
        m_SystemOutLogger.println(message);
        m_perfResultsFile.write(message.getBytes());
        message = "\n\nTotal reconnects to card:; " + m_numReconnects; 
        m_SystemOutLogger.println(message);
        m_perfResultsFile.write(message.getBytes());
        
        // Print all observed errors
        message = "\n\nTotal errors found:; "; 
        m_SystemOutLogger.println(message);
        for (String error : m_errorsObserved.keySet()) {
            m_SystemOutLogger.println(error + ": " + m_errorsObserved.get(error).size());
            if (error != "NO_SUCH_ALGORITHM") { // do not print benign NO_SUCH_ALGORITHM
                for (String item : m_errorsObserved.get(error)) {
                    m_SystemOutLogger.println("  " + item);
                }
            }
        }

        // Print only aggregated statistics    
        m_SystemOutLogger.println(message);
        for (String error : m_errorsObserved.keySet()) {
            m_SystemOutLogger.println("  " + error + ": " + m_errorsObserved.get(error).size());
        }

        message = "\n\nCard used: " + m_cardName; 
        m_SystemOutLogger.println(message);
    }
    
    void LoadAlreadyMeasuredAlgs(String cardName, String testType, boolean bForceFreshmeasurement) {
        String filePath = testType + "_already_measured.list";
        String filePathOld = filePath + ".old";
        File f = new File(filePath);
        File fOld = new File(filePathOld);
        
        try {
            if (!bForceFreshmeasurement && f.exists() && !f.isDirectory()) {
                // Ask user for continuation
                String message = "File '" + filePath + "' with already measured algorithms found. Do you like to use it and measure only missing algorithms? (y/n)\n";
                m_SystemOutLogger.print(message);
                Scanner sc = new Scanner(System.in);
                String answ = sc.next();
                m_SystemOutLogger.println(String.format("%s", answ));
                if (answ.equals("y")) {
                    m_SystemOutLogger.println("\tContinue was selected. Only algorithms NOT present " + filePath + " in will be measured");
                    // Read all measured algorithms earlier
                    m_SystemOutLogger.println("Following algorithms will NOT be measured again (listed in " + filePath + " file):");
                    BufferedReader br = new BufferedReader(new FileReader(filePath));
                    String strLine;
                    while ((strLine = br.readLine()) != null) {
                        if (!strLine.isEmpty()) {
                            m_algsMeasuredList.add(strLine);
                            m_SystemOutLogger.println(strLine);
                        }
                    }
                    br.close();
                }
                else {
                    m_SystemOutLogger.println("\tContinue was NOT selected. All algorithms will be measured");
                    m_algsMeasuredList.clear();
                }
                // Create backup of file with measured algorithms
                f.renameTo(fOld);
            }

            // Create new file with list of measured algorithms (already measured and newly measured will be included)
            m_algsMeasuredFile = new FileOutputStream(filePath);
        } catch (IOException ex) {
            m_SystemOutLogger.println("No read of file with already measured algs: " + filePath);
        }
    }
    
    /**
     * Method that will test all algorithms in PerformanceTesting class
     * @param file FileOutputStream object for output data.
     * @throws Exception
     */
    public void testAllAtOnce (FileOutputStream file) throws Exception{
        /* Variable RSA public exponent support */
        StringBuilder value = new StringBuilder();
        String message;
        
        value.setLength(0);            
        if (this.TestVariableRSAPublicExponentSupport(value, file, (byte) 0) == CardMngr.STAT_OK) {}
        else { 
            message = "\nERROR: Test variable public exponent support fail\n"; 
            m_SystemOutLogger.println(message); file.write(message.getBytes());
        }
        if (file != null) file.flush();
        
        /* Available RAM memory. */
        value.setLength(0);
        if (this.TestAvailableRAMMemory(value, file, (byte) 0) == CardMngr.STAT_OK) {}
        else { 
            message = "\nERROR: Get available RAM memory fail\n"; 
            m_SystemOutLogger.println(message); file.write(message.getBytes());
        }
        if (file != null) file.flush();
        
        /* Available EEPROM memory. */
        value.setLength(0);
        if (this.TestAvailableEEPROMMemory(value, file, (byte) 0) == CardMngr.STAT_OK) {}
        else { 
            message = "\nERROR: Get available EEPROM memory fail\n"; 
            m_SystemOutLogger.println(message); file.write(message.getBytes());
        }
        if (file != null) file.flush();
        
        if (file != null) file.close();
    }
    
    public int TestAvailableRAMMemory(StringBuilder pValue, FileOutputStream pFile, byte algPartP2) throws Exception {
        int         status = CardMngr.STAT_OK;
        long     elapsedCard;

        // Prepare test memory apdu
        byte apdu[] = new byte[CardMngr.HEADER_LENGTH];
        apdu[CardMngr.OFFSET_CLA] = Consts.CLA_CARD_ALGTEST;
        apdu[CardMngr.OFFSET_INS] = (byte) 0x71;
        apdu[CardMngr.OFFSET_P1] = 0x00;
        apdu[CardMngr.OFFSET_P2] = 0x00;
        apdu[CardMngr.OFFSET_LC] = 0x00;
            
        elapsedCard = -System.currentTimeMillis();

        ResponseAPDU resp = m_cardManager.sendAPDU(apdu);
        if (resp.getSW() != 0x9000) {
            m_SystemOutLogger.println("Fail to obtain response for TestAvailableRAMMemory");
        } else {
            // SAVE TIME OF CARD RESPONSE
            elapsedCard += System.currentTimeMillis();
            
            // OK, STORE RESPONSE TO suppAlg ARRAY
            byte temp[] = resp.getData();
                
            //String elTimeStr = "";
            // OUTPUT REQUIRED TIME WHEN PARTITIONED CHECk WAS PERFORMED (NOTMULTIPLE ALGORITHMS IN SINGLE RUN)
            String elTimeStr = String.format("%1f", (double) elapsedCard / (float) CardMngr.CLOCKS_PER_SEC);

            String message = "";
            message += "\r\n\r\nAvailable RAM memory;"; 
            m_SystemOutLogger.println(message);
            pFile.write(message.getBytes());
            pValue.append(message);

            int ramSize = (temp[0] << 8) + (temp[1] & 0xff);
            message = String.format("%1d B;", ramSize); 
            m_SystemOutLogger.println(message);
            pFile.write(message.getBytes());
            pValue.append(message);
        }

        return status;
    }

    
    public int TestIOSpeed(StringBuilder pValue, FileOutputStream pFile, byte algPartP2) throws Exception {
        int         status = CardMngr.STAT_OK;
        long     elapsedCard;

        // Test of speed dependant on data length
        String tableName = "\n\nIOSPEED - variable data - BEGIN\n";
        m_perfResultsFile.write(tableName.getBytes());
        for (Integer length : m_testDataLengths) {
            short dataLength = length.shortValue();
            
            if (dataLength < 250) {
                // Prepare test memory apdu
                byte apdu[] = new byte[CardMngr.HEADER_LENGTH + dataLength];
                apdu[CardMngr.OFFSET_CLA] = Consts.CLA_CARD_ALGTEST;
                apdu[CardMngr.OFFSET_INS] = Consts.INS_CARD_DATAINOUT;
                apdu[CardMngr.OFFSET_P1] = 0x00;
                apdu[CardMngr.OFFSET_P2] = 0x00;
                apdu[CardMngr.OFFSET_LC] = (byte) dataLength;

                elapsedCard = -System.currentTimeMillis();

                ResponseAPDU resp = m_cardManager.sendAPDU(apdu);
                elapsedCard += System.currentTimeMillis();

                if (resp.getSW() != 0x9000) {
                    m_SystemOutLogger.println("Fail to obtain response for IOSpeed");
                } else {
                    // SAVE TIME OF CARD RESPONSE

                    // OK, STORE RESPONSE TO suppAlg ARRAY
                    byte temp[] = resp.getData();

                    String elTimeStr = "";
                    // OUTPUT REQUIRED TIME WHEN PARTITIONED CHECk WAS PERFORMED (NOTMULTIPLE ALGORITHMS IN SINGLE RUN)
                    elTimeStr = String.format("%1f", (double) elapsedCard / (float) CardMngr.CLOCKS_PER_SEC);
                }
            }
        }
        tableName = "\n\nIOSPEED - variable data - END\n";
        m_perfResultsFile.write(tableName.getBytes());
        return status;
    }    

    public int TestAvailableEEPROMMemory(StringBuilder pValue, FileOutputStream pFile, byte algPartP2) throws Exception {
        int         status = CardMngr.STAT_OK;
        long     elapsedCard;

        // Prepare test memory apdu
        byte apdu[] = new byte[CardMngr.HEADER_LENGTH];
        apdu[CardMngr.OFFSET_CLA] = Consts.CLA_CARD_ALGTEST;
        apdu[CardMngr.OFFSET_INS] = (byte) 0x71;
        apdu[CardMngr.OFFSET_P1] = 0x01;
        apdu[CardMngr.OFFSET_P2] = 0x00;
        apdu[CardMngr.OFFSET_LC] = 0x00;
            
        elapsedCard = -System.currentTimeMillis();

        ResponseAPDU resp = m_cardManager.sendAPDU(apdu);
        if (resp.getSW() != 0x9000) {
            m_SystemOutLogger.println("Fail to obtain response for TestAvailableEEPROMMemory");
        } else {
            // SAVE TIME OF CARD RESPONSE
            elapsedCard += System.currentTimeMillis();
            
            // OK, STORE RESPONSE TO suppAlg ARRAY
            byte temp[] = resp.getData();
                        
            // SAVE TIME OF CARD RESPONSE
            elapsedCard += System.currentTimeMillis();
            String elTimeStr = "";
            // OUTPUT REQUIRED TIME WHEN PARTITIONED CHECk WAS PERFORMED (NOTMULTIPLE ALGORITHMS IN SINGLE RUN)
            elTimeStr = String.format("%1f", (double) elapsedCard / (float) CardMngr.CLOCKS_PER_SEC);

            String message = "";
            message += "\"\\r\\n\r\nAvailable EEPROM memory;"; 
            m_SystemOutLogger.println(message);
            pFile.write(message.getBytes());
            pValue.append(message);

            int eepromSize = (temp[2] << 8) + (temp[3] & 0xff);
            eepromSize += (temp[4] << 8) + (temp[5] & 0xff);
            eepromSize += (temp[6] << 8) + (temp[7] & 0xff);
            eepromSize += (temp[8] << 8) + (temp[9] & 0xff);
            eepromSize += (temp[10] << 8) + (temp[11] & 0xff);
            eepromSize += (temp[12] << 8) + (temp[13] & 0xff);
            eepromSize += (temp[14] << 8) + (temp[15] & 0xff);
            eepromSize += (temp[16] << 8) + (temp[17] & 0xff);
            message = String.format("%1d B;\r\n", eepromSize); 
            m_SystemOutLogger.println(message);
            pFile.write(message.getBytes());
            pValue.append(message);

        }

        return status;
    }

    public int TestAction(String actionName, byte apdu[], StringBuilder pValue, FileOutputStream pFile) throws Exception {
	int		status = CardMngr.STAT_OK;

        long     elapsedCard = 0;
	elapsedCard -= System.currentTimeMillis();

	String message;
	message = String.format("\r\n%1s;", actionName); 
        m_SystemOutLogger.println(message);
        pFile.write(message.getBytes());
        pValue.append(message);
            
        ResponseAPDU resp = m_cardManager.sendAPDU(apdu);
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
            elTimeStr = String.format("%1f", (double) elapsedCard / (float) CardMngr.CLOCKS_PER_SEC);

            message = String.format("yes;%1s sec;", elTimeStr); 
            m_SystemOutLogger.println(message);
            pFile.write(message.getBytes());
            pValue.append(message);
	}

	return status;
    }

    public int TestVariableRSAPublicExponentSupport(StringBuilder pValue, FileOutputStream pFile, byte algPartP2) throws Exception {
        int         status = CardMngr.STAT_OK;
        
        byte apdu[] = new byte[CardMngr.HEADER_LENGTH];
        apdu[CardMngr.OFFSET_CLA] = Consts.CLA_CARD_ALGTEST;
        apdu[CardMngr.OFFSET_INS] = (byte) 0x72;
        apdu[CardMngr.OFFSET_P1] = 0x00;
        apdu[CardMngr.OFFSET_P2] = 0x00;
        apdu[CardMngr.OFFSET_LC] = 0x00;
            
        String message;
        message = "\r\nSupport for variable public exponent for RSA 1024. If supported, user-defined fast modular exponentiation can be executed on the smart card via cryptographic coprocessor. This is very specific feature and you will probably not need it;"; 
        m_SystemOutLogger.println(message);
        pFile.write(message.getBytes());
        pValue.append(message);

        // Allocate RSA 1024 objects (RSAPublicKey and ALG_RSA_NOPAD cipher)
        apdu[CardMngr.OFFSET_P1] = 0x01;	
        TestAction("Allocate RSA 1024 objects", apdu, pValue,pFile);
        // Try to set random modulus
        apdu[CardMngr.OFFSET_P1] = 0x02;	
        TestAction("Set random modulus", apdu, pValue,pFile);
        // Try to set random exponent
        apdu[CardMngr.OFFSET_P1] = 0x03;	
        TestAction("Set random public exponent", apdu, pValue,pFile);
        // Try to initialize cipher with public key with random exponent
        apdu[CardMngr.OFFSET_P1] = 0x04;	
        TestAction("Initialize cipher with public key with random exponent", apdu, pValue,pFile);
        // Try to encrypt block of data
        apdu[CardMngr.OFFSET_P1] = 0x05;	
        TestAction("Use random public exponent", apdu, pValue,pFile);        
 
        return status;
    }
    
    public int TestExtendedAPDUSupportSupport(StringBuilder pValue, FileOutputStream pFile, byte algPartP2) throws Exception {
        int         status = CardMngr.STAT_OK;
        
        byte apdu[] = new byte[CardMngr.HEADER_LENGTH + 2 + CardMngr.EXTENDED_APDU_TEST_LENGTH]; // + 2 is because of encoding of LC length into three bytes total
        apdu[CardMngr.OFFSET_CLA] = Consts.CLA_CARD_ALGTEST;
        apdu[CardMngr.OFFSET_INS] = (byte) 0x74;
        apdu[CardMngr.OFFSET_P1] = 0x00;
        apdu[CardMngr.OFFSET_P2] = 0x00;
        apdu[CardMngr.OFFSET_LC] = 0x00;
        apdu[CardMngr.OFFSET_LC+1] = (byte)(CardMngr.EXTENDED_APDU_TEST_LENGTH & 0xff00 >> 8);
        apdu[CardMngr.OFFSET_LC+2] = (byte)(CardMngr.EXTENDED_APDU_TEST_LENGTH & 0xff);
            
        String message;
        message = "\r\nSupport for extended APDU. If supported, APDU longer than 255 bytes can be send.;"; 
        m_SystemOutLogger.println(message);
        pFile.write(message.getBytes());
        pValue.append(message);
        
        ResponseAPDU resp = m_cardManager.sendAPDU(apdu);
        if (resp.getSW() != 0x9000) {
            message = String.format("no;"); 
        }
        else {
            // OK, STORE RESPONSE TO suppAlg ARRAY
            byte temp[] = resp.getData();
            
            short LC = (short) ((temp[0] << 8) + (temp[1] & 0xff));
            short realLC = (short) ((temp[2] << 8) + (temp[3] & 0xff));
            
            if (LC == CardMngr.EXTENDED_APDU_TEST_LENGTH && realLC == CardMngr.EXTENDED_APDU_TEST_LENGTH) {
                message = String.format("yes;"); 
            }
            else {
                message = String.format("no;");                 
            }
        }
        m_SystemOutLogger.println(message);
        pFile.write(message.getBytes());
        pValue.append(message);            

        return status;
    }

    
        

    public TestSettings prepareTestSettings(short classType, short algorithmSpecification, short keyType, short keyLength, short algorithmMethod, short dataLength1, short dataLength2, short initMode, short numRepeatWholeOperation, short numRepeatSubOperation, short numRepeatWholeMeasurement) {
        TestSettings    testSet = new TestSettings();
        
        testSet.classType = classType;                              // custom constant signalizing javacard class - e.g., custom constant for javacardx.crypto.Cipher
        testSet.algorithmSpecification = algorithmSpecification;    // e.g., Cipher.ALG_AES_BLOCK_128_CBC_NOPAD
        testSet.keyType = keyType;                                  // e.g., KeyBuilder.TYPE_AES
        testSet.keyLength = keyLength;                              // e.g., KeyBuilder.LENGTH_AES_128
        testSet.algorithmMethod = algorithmMethod;                  // custom constant signalizing target javacard method e.g., 
        testSet.dataLength1 = dataLength1;                          // e.g., length of data used during measurement (e.g., for update())
        testSet.dataLength2 = dataLength2;                          // e.g., length of data used during measurement (e.g., for doFinal())
        testSet.initMode = initMode;                                // initialization mode for init(key, mode), e.g., Cipher.ENCRYPT
        testSet.numRepeatWholeOperation = numRepeatWholeOperation;  // whole operation might be setKey, update, doFinal - numRepeatWholeOperation repeats this whole operation
        testSet.numRepeatSubOperation = numRepeatSubOperation;      // relevant suboperation that should be iterated multiple times - e.g., update()
        testSet.numRepeatWholeMeasurement = numRepeatWholeMeasurement;  // whole operation might be setKey, update, doFinal - numRepeatWholeOperation repeats this whole operation
                
        return testSet;
    }
    public void perftest_prepareClass(byte appletCLA, byte appletINS, short classType, short algorithmSpecification, short keyType, short keyLength, short algorithmMethod, short dataLength1, short dataLength2, short initMode, short numRepeatWholeOperation, short numRepeatSubOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        TestSettings testSet = prepareTestSettings(classType, algorithmSpecification, keyType, keyLength, algorithmMethod, dataLength1, dataLength2, initMode, numRepeatWholeOperation, numRepeatSubOperation, numRepeatWholeMeasurement);
        perftest_prepareClass(appletCLA, appletINS, testSet);
    }
    public void perftest_prepareClass(byte appletCLA, byte appletINS, TestSettings testSet) throws IOException, Exception {
        // Free previously allocated objects
        boolean success = m_cardManager.resetApplet(appletCLA, Consts.INS_CARD_RESET);
        // Prepare new set
        m_cardManager.PerfTestCommand(appletCLA, appletINS, testSet, Consts.INS_CARD_RESET);
    }
   
    public double perftest_measure(byte appletCLA, byte appletPrepareINS, byte appletMeasureINS, TestSettings testSet, String info) throws IOException, Exception {
        return perftest_measure(appletCLA, appletPrepareINS, appletMeasureINS, testSet, info, 0); 
    }
    public double perftest_measure(byte appletCLA, byte appletPrepareINS, byte appletMeasureINS, TestSettings testSet, String info, double substractTime) throws IOException, Exception {
        double measureTime = -1;
        StringBuilder result = new StringBuilder();
        int numFailedRepeats = 0;
        
        // Check if this measurement should be performed or was already measured before
        if (m_algsMeasuredList.contains(info)) {
            // we already measured this algorithm before, just log it into new measurementsDone file
            String message = info + "\n";
            if (m_algsMeasuredFile != null) { 
                m_algsMeasuredFile.write(message.getBytes());
                m_algsMeasuredFile.flush();
            }        
            
            message = "\nmethod name:; " + info + "\n";    
            message += "ALREADY_MEASURED\n";
            m_perfResultsFile.write(message.getBytes());
            return -1; // was not measured
        }
        else {
            while (numFailedRepeats < MAX_FAILED_REPEATS) {
                try {
                    result.setLength(0);
                    double[] measuredTimes = perftest_measure(appletCLA, appletPrepareINS, appletMeasureINS, testSet, info, result, substractTime);
                    measureTime = measuredTimes[0];
                    // Measurement success, reset failed attempts counter
                    numFailedRepeats = 0;
                    // Write result string into file
                    m_perfResultsFile.write(result.toString().getBytes());

                    // log succesfull measurement of current algorithm 
                    m_bAlgsMeasuredSomeNew = true;
                    String message = info + "\n";
                    if (m_algsMeasuredFile != null) { m_algsMeasuredFile.write(message.getBytes()); }        

                    m_algsAvgTime.put(info, measuredTimes);
                    
                    // end loop 
                    return measureTime;
                }
                catch (CardCommunicationException ex) {
                    // Normal exception like NO_SUCH_ALGORITHM  - just print it 
                    String message = ex.toString();
                    // Store error for statistics
                    if (!m_errorsObserved.containsKey(message)) {
                        // New error observed
                        m_errorsObserved.put(message, new ArrayList<String>());
                    }
                    m_errorsObserved.get(message).add(info); 
                            
                    message += "\n";
                    if (ex.getReason() == ISO7816.SW_INS_NOT_SUPPORTED) {
                        message += ";ERROR: Invalid instruction was send to card. "
                                + "Possibly, card contains only restricted version of JCAlgTest applet. "
                                + "If you like to do performance testing, upload full version of  JCAlgTest applet (e.g. AlgTest_v1.6_jc212.cap instead of AlgTest_v1.6_supportOnly_jc212.cap)";    
                    }
                    m_SystemOutLogger.println(message); 
                    // Write result string into file
                    m_perfResultsFile.write(result.toString().getBytes());
                    // Write exception into file
                    m_perfResultsFile.write(message.getBytes());

                    // log succesfull measurement of current algorithm - although exception ocurred, it is expected value like NO_SUCH_ALGORITHM
                    m_bAlgsMeasuredSomeNew = true;
                    message = info + "\n";
                    if (m_algsMeasuredFile != null) {
                        m_algsMeasuredFile.write(message.getBytes()); 
                        m_algsMeasuredFile.flush();
                    }       

                    return -1;
                }
                catch (Exception ex) {
                    // Unexpected exception
                    m_SystemOutLogger.println(ex.toString() + "\n"); 
                    numFailedRepeats++; 
                    
                    if (numFailedRepeats == 1) {
                        // For first fail, try to reconnect to card automatically
                        try {
                            m_numReconnects++;
                            m_cardManager.ConnectToCard();
                        }
                        catch (Exception ex2) {
                            m_SystemOutLogger.println(ex2.toString()); 
                            numFailedRepeats++;
                        }
                    }
                    
                    if (numFailedRepeats > 1) {
                        // For second fail, ask user 
                        m_SystemOutLogger.println("ERROR: unable to measure operation '" + info + "' properly because of exception (" + ex.toString() + ")");
                        m_SystemOutLogger.println("Current reader is: " + m_cardManager.getTerminalName());
                        m_SystemOutLogger.println("Current card is: " + m_cardName + " - " + m_cardManager.getATR());
                        m_SystemOutLogger.println("Try to physically remove card and/or upload applet manually and insert it again. Press 'r' to retry or 's' to skip this algorithm (if retry fails)\n");
                        Scanner sc = new Scanner(System.in);
                        String answ = sc.next();
                        m_SystemOutLogger.println(String.format("%s", answ));
                        if (answ.equals("r")) {
                            m_numHumanInterventions++;
                            
                            try {
                                m_numReconnects++;
                                m_cardManager.ConnectToCard();
                                // Card was physically removed, reset retries counter
                                numFailedRepeats = 0;
                            }
                            catch (Exception ex2) {
                                m_SystemOutLogger.println(ex2.toString()); 
                                numFailedRepeats++;
                            }
                        }
                        else {
                            // Skip this algorithm 
                            m_SystemOutLogger.println("Skipping algorithm " + info); 
                            m_perfResultsFile.write(ex.toString().getBytes());

                            return -1;
                        }
                    }
                }
            }
        }
        
        m_SystemOutLogger.print("ERROR: unable to measure operation '" + info + "'");
                
        return -1;
    }
    
    double computeMedian(double[] rawMeasureList) {
        return computeMedian(rawMeasureList, 0, rawMeasureList.length);
    }
    double computeMedian(double[] rawMeasureList, int startOffset, int length) {
         // Compute median
        
        Arrays.sort(rawMeasureList);
        
        double median = 0;
        if (length % 2 == 1) {
            median = rawMeasureList[startOffset + (length / 2)]; // middle item
        } else {
            median = (rawMeasureList[startOffset + (length / 2)] + rawMeasureList[startOffset + (length / 2) + 1]) / 2; // avg of two middle items
        }
        
        return median;
    }
    double[] computeMedianQuartils(double[] rawMeasureList) {
         // Compute median

        double median = computeMedian(rawMeasureList);
        
        double lowerQuartile = computeMedian(rawMeasureList, 0, rawMeasureList.length / 2);
        double higherQuartile = computeMedian(rawMeasureList, rawMeasureList.length / 2, rawMeasureList.length / 2);            

        double[] result = new double[3];
        result[0] = median;
        result[1] = lowerQuartile;
        result[2] = higherQuartile;
        
        return result;
    }    
    
    
    double getAverageDropOutliers(double[] rawMeasureList) {
        // Compute stats
        double[] stats = computeMedianQuartils(rawMeasureList);
        
        double sumTimes = 0;
        // Filter out outliers (Tukey's test)
        double TukeyConst = 1.5;
        double lowBound = stats[1] - TukeyConst * (stats[2] - stats[1]);
        double highBound = stats[2] + TukeyConst * (stats[2] - stats[1]);
        int numValid = 0;
        for (double val : rawMeasureList) {
            if (val < lowBound || val > highBound) {
                // Outlier
            }
            else {
                // valid value
                sumTimes += val;
                numValid++;
            }
        }
        
        return sumTimes / numValid;
    }
    public double[] perftest_measure(byte appletCLA, byte appletPrepareINS, byte appletMeasureINS, TestSettings testSet, String info, StringBuilder result) throws IOException, Exception {
        return perftest_measure(appletCLA, appletPrepareINS, appletMeasureINS, testSet, info, result, 0);
    }
    public double[] perftest_measure(byte appletCLA, byte appletPrepareINS, byte appletMeasureINS, TestSettings testSet, String info, StringBuilder result, double substractTime) throws IOException, Exception {
        double check = 0.05; // Maximum percentage difference in which should be all times of individual operations, 0.1 = 10%
        double avgOpTime = -1;
        String message = "";
        // Tested method name
        message += "\nmethod name:; " + info + "\n";
        m_SystemOutLogger.print(message);
        result.append(message);
        
        // Test settings
        byte[] settings = new byte[TestSettings.TEST_SETTINGS_LENGTH];  
        CardMngr.serializeToApduBuff(testSet, settings, (short) 0);
        message = "measurement config:;" + "appletPrepareINS;" + CardMngr.byteToHex(appletPrepareINS) + ";appletMeasureINS;" + CardMngr.byteToHex(appletMeasureINS) + ";config;" + CardMngr.bytesToHex(settings) + "\n";
        m_SystemOutLogger.print(message);
        result.append(message);
        
        message = "";

        //            
        // Prepare fresh set of objects
        //
        perftest_prepareClass(appletCLA, appletPrepareINS, testSet);        


        double sumTimes = 0;
        double avgOverhead = 0;
        double minOverhead = Double.MAX_VALUE;
        double maxOverhead = -Double.MAX_VALUE;
        String timeStr;

        //
        // Measure processing time without actually calling measured operation (testSet.numRepeatWholeOperation set to 0)
        //
        double[] rawMeasureList = new double[NUM_BASELINE_CALIBRATION_RUNS];   
        if (testSet.bPerformBaselineMeasurement == Consts.TRUE) {
            short bkpNumRepeatWholeOperation = testSet.numRepeatWholeOperation;
            testSet.numRepeatWholeOperation = 0;
            message +=  "baseline measurements (ms):;";
            for (int i = 0; i < NUM_BASELINE_CALIBRATION_RUNS;i++) {
                m_cardManager.resetApplet(appletCLA, Consts.INS_CARD_RESET);
                double overheadTime = m_cardManager.PerfTestCommand(appletCLA, appletMeasureINS, testSet, Consts.INS_CARD_RESET);
                rawMeasureList[i] = overheadTime;
                sumTimes += overheadTime;
                timeStr = String.format("%.2f", overheadTime);
                message +=  timeStr + ";" ;
                m_SystemOutLogger.print(timeStr + " ");
                if (overheadTime<minOverhead) minOverhead=overheadTime;
                if (overheadTime>maxOverhead) maxOverhead=overheadTime;
            }
            
            //double avgSimpleOverhead = sumTimes / NUM_BASELINE_CALIBRATION_RUNS; // Simple average with all measurements
            avgOverhead = getAverageDropOutliers(rawMeasureList);
            
            
            message += "\nbaseline stats (ms):;avg:;" + String.format("%.2f", avgOverhead);
            message += ";min:;" + String.format("%.2f", minOverhead);
            message += ";max:;" + String.format("%.2f", maxOverhead);
            message += ";";
            if ((minOverhead/avgOverhead < (1-check)) || (maxOverhead/avgOverhead > (1+check)))  message += ";;CHECK";
            m_SystemOutLogger.print("\nbaseline avg time: " + avgOverhead);
            m_SystemOutLogger.println();     
            m_SystemOutLogger.println(); message += "\n";
            result.append(message);
            message = "";
            // Restore required number of required measurements 
            testSet.numRepeatWholeOperation = bkpNumRepeatWholeOperation;
        }

        //
        // Measure operations
        //

        double minOpTime = Double.MAX_VALUE;
        double maxOpTime = -Double.MAX_VALUE;
        double[] times = new double[testSet.numRepeatWholeMeasurement + 1]; // first value is average, rest are computed times    
        double time = 0;
        sumTimes = 0;
        message += "operation raw measurements (ms):;";
        for (int i = 0; i < testSet.numRepeatWholeMeasurement; i++) {
            m_cardManager.resetApplet(appletCLA, Consts.INS_CARD_RESET);
            time = m_cardManager.PerfTestCommand(appletCLA, appletMeasureINS, testSet, Consts.INS_CARD_RESET);
            time -= avgOverhead;
            if (Math.abs(substractTime) > 0.0005) { // comparing double to 0
                // Substract given time (if measured operation requires to perform another unrelated operation, e.g., setKey before clearKey)
                m_SystemOutLogger.println("Substracting substractTime = " + substractTime + " from measured time"); 
                time -= substractTime;
            }
            
            sumTimes += time;
            timeStr = String.format("%.2f", time);
            message +=  timeStr + ";";
            m_SystemOutLogger.print(timeStr + " ");
            if (time<minOpTime) minOpTime=time;
            if (time>maxOpTime) maxOpTime=time;
            times[i + 1] = time / testSet.numRepeatWholeOperation; // store current measurement (start from 1, times[0]] reserved for average)
        }
        m_SystemOutLogger.println();     

        m_SystemOutLogger.println(); message += "\n";
        result.append(message);
        message = "";

        // Compute average per operation       
        String messageOpTime =  "operation stats (ms/op):";
        int totalIterations = testSet.numRepeatWholeOperation * testSet.numRepeatWholeMeasurement;
        avgOpTime = (totalIterations != 0) ? sumTimes/totalIterations : 0;
        times[0] = avgOpTime;
        minOpTime = (totalIterations != 0) ? minOpTime/testSet.numRepeatWholeOperation : 0;
        maxOpTime = (totalIterations != 0) ? maxOpTime/testSet.numRepeatWholeOperation : 0;
        messageOpTime += ";avg op:;" + String.format("%.2f", avgOpTime);
        messageOpTime += ";min op:;" + String.format("%.2f", minOpTime);
        messageOpTime += ";max op:;" + String.format("%.2f", maxOpTime);
        messageOpTime += ";";
        if ((minOpTime/avgOpTime < (1-check)) || (maxOpTime/avgOpTime >(1+check)))  messageOpTime += ";CHECK";
        messageOpTime += "\noperation info:;";
        messageOpTime += "data length;" + testSet.dataLength1 + ";";
        messageOpTime += "total iterations;" + totalIterations + ";";
        messageOpTime += "total invocations;" + (totalIterations * testSet.numRepeatSubOperation) + ";\n";
        result.append(messageOpTime);
        m_SystemOutLogger.println(messageOpTime);  

        m_SystemOutLogger.println(); message += "\n";
        m_SystemOutLogger.println(message); 
        
        return times;
    }
    
    
    public void testUtil(String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        TestSettings testSet = this.prepareTestSettings(Consts.CLASS_UTIL, Consts.UNUSED, Consts.UNUSED, Consts.UNUSED, JCConsts.RandomData_generateData, 
                Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);      

        ArrayList<Pair<Short, String>> testedOps = new ArrayList<>();
        testedOps.add(new Pair(JCConsts.Util_arrayCompare_RAM_matching, "Util_arrayCompare_RAM_matching()"));
        testedOps.add(new Pair(JCConsts.Util_arrayCompare_RAM_mismatching, "Util_arrayCompare_RAM_mismatching()"));

        testedOps.add(new Pair(JCConsts.Util_arrayCopy_RAM, "Util_arrayCopy_RAM()"));
        testedOps.add(new Pair(JCConsts.Util_arrayCopy_EEPROM, "Util_arrayCopy_EEPROM()"));
        testedOps.add(new Pair(JCConsts.Util_arrayCopy_RAM2EEPROM, "Util_arrayCopy_RAM2EEPROM()"));
        testedOps.add(new Pair(JCConsts.Util_arrayCopy_EEPROM2RAM, "Util_arrayCopy_EEPROM2RAM()"));
        testedOps.add(new Pair(JCConsts.Util_arrayCopyNonAtomic_RAM, "Util_arrayCopyNonAtomic_RAM()"));
        testedOps.add(new Pair(JCConsts.Util_arrayCopyNonAtomic_EEPROM, "Util_arrayCopyNonAtomic_EEPROM()"));
        testedOps.add(new Pair(JCConsts.Util_arrayCopyNonAtomic_RAM2EEPROM, "Util_arrayCopyNonAtomic_RAM2EEPROM()"));
        testedOps.add(new Pair(JCConsts.Util_arrayCopyNonAtomic_EEPROM2RAM, "Util_arrayCopyNonAtomic_EEPROM2RAM()"));
        testedOps.add(new Pair(JCConsts.Util_arrayFillNonAtomic_RAM, "Util_arrayFillNonAtomic_RAM()"));
        testedOps.add(new Pair(JCConsts.Util_arrayFillNonAtomic_EEPROM, "Util_arrayFillNonAtomic_EEPROM()"));
        testedOps.add(new Pair(JCConsts.Util_arrayCompare_RAM, "Util_arrayCompare_RAM()"));
        testedOps.add(new Pair(JCConsts.Util_arrayCompare_EEPROM, "Util_arrayCompare_EEPROM()"));
        testedOps.add(new Pair(JCConsts.Util_arrayCompare_RAM2EEPROM, "Util_arrayCompare_RAM2EEPROM()"));
        testedOps.add(new Pair(JCConsts.Util_arrayCompare_EEPROM2RAM, "Util_arrayCompare_EEPROM2RAM()"));
            
        if (!m_bTestVariableData) {
            // Ordinary test of all available methods
            assert(testSet.dataLength1 <= JCAlgTestApplet.RAM1_ARRAY_LENGTH / 2);  // some methods will operate on same array (copy) so at maxiumum, half of array can be used as input
            for (Pair op : testedOps) {
                testSet.algorithmMethod = (Short) op.getL();
                this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_UTIL, Consts.INS_PERF_TEST_CLASS_UTIL, testSet, info + " " + (String) op.getR());
            }
        }
        else {
            // Test of speed dependant on data length
            String tableName = "\n\nUTIL - "  + info + " - variable data - BEGIN\n";
            m_perfResultsFile.write(tableName.getBytes());

            for (Pair op : testedOps) {
                testSet.algorithmMethod = (Short) op.getL();
                for (Integer length : m_testDataLengths) {
                    testSet.dataLength1 = length.shortValue();
                    if (testSet.dataLength1 <= JCAlgTestApplet.RAM1_ARRAY_LENGTH / 2) {
                        this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_UTIL, Consts.INS_PERF_TEST_CLASS_UTIL, testSet, info + " " + (String) op.getR() + ";" + length + ";");
                    }
                }
            }
            tableName = "\n\nUTIL - "  + info + " - variable data - END\n";
            m_perfResultsFile.write(tableName.getBytes());
        }
    }    
    public void testAllUtil(int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception {
        testAllUtil((short) numRepeatWholeOperation, (short) numRepeatWholeMeasurement);
    }
    public void testAllUtil(short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        String tableName = "\n\nUTIL\n";
        m_perfResultsFile.write(tableName.getBytes());
        testUtil("UTIL", numRepeatWholeOperation, numRepeatWholeMeasurement);
        tableName = "\n\nUTIL - END\n";
        m_perfResultsFile.write(tableName.getBytes());
    }    

    public void testKeyPair(byte alg, short length, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        TestSettings testSet = prepareTestSettings(Consts.CLASS_KEYPAIR, Consts.UNUSED, Consts.UNUSED, length, JCConsts.KeyPair_genKeyPair, 
                Consts.UNUSED, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);      

        testSet.keyClass = alg;
        testSet.algorithmMethod = JCConsts.KeyPair_genKeyPair;
        this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEYPAIR, Consts.INS_PERF_TEST_CLASS_KEYPAIR, testSet, info + " KeyPair_genKeyPair()");
    }
    
    public void testAllKeyPairs(int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception {
        testAllKeyPairs((short) numRepeatWholeOperation, (short) numRepeatWholeMeasurement);
    }
    public void testAllKeyPairs(short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        String tableName = "\n\nKEY PAIR";
        m_perfResultsFile.write(tableName.getBytes());
        if (m_bTestAsymmetricAlgs) {
            String message = "\n";
            m_perfResultsFile.write(message.getBytes());
            testKeyPair(JCConsts.KeyPair_ALG_RSA,JCConsts.KeyBuilder_LENGTH_RSA_512,"ALG_RSA LENGTH_RSA_512", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyPair(JCConsts.KeyPair_ALG_RSA,JCConsts.KeyBuilder_LENGTH_RSA_736,"ALG_RSA LENGTH_RSA_736", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyPair(JCConsts.KeyPair_ALG_RSA,JCConsts.KeyBuilder_LENGTH_RSA_768,"ALG_RSA LENGTH_RSA_768", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyPair(JCConsts.KeyPair_ALG_RSA,JCConsts.KeyBuilder_LENGTH_RSA_896,"ALG_RSA LENGTH_RSA_896", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyPair(JCConsts.KeyPair_ALG_RSA,JCConsts.KeyBuilder_LENGTH_RSA_1024,"ALG_RSA LENGTH_RSA_1024", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyPair(JCConsts.KeyPair_ALG_RSA,JCConsts.KeyBuilder_LENGTH_RSA_1280,"ALG_RSA LENGTH_RSA_1280", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyPair(JCConsts.KeyPair_ALG_RSA,JCConsts.KeyBuilder_LENGTH_RSA_1536,"ALG_RSA LENGTH_RSA_1536", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyPair(JCConsts.KeyPair_ALG_RSA,JCConsts.KeyBuilder_LENGTH_RSA_1984,"ALG_RSA LENGTH_RSA_1984", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyPair(JCConsts.KeyPair_ALG_RSA,JCConsts.KeyBuilder_LENGTH_RSA_2048,"ALG_RSA LENGTH_RSA_2048", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyPair(JCConsts.KeyPair_ALG_RSA,JCConsts.KeyBuilder_LENGTH_RSA_3072,"ALG_RSA LENGTH_RSA_3072", numRepeatWholeOperation, numRepeatWholeMeasurement);        
            testKeyPair(JCConsts.KeyPair_ALG_RSA,JCConsts.KeyBuilder_LENGTH_RSA_4096,"ALG_RSA LENGTH_RSA_4096", numRepeatWholeOperation, numRepeatWholeMeasurement);        
            testKeyPair(JCConsts.KeyPair_ALG_RSA_CRT,JCConsts.KeyBuilder_LENGTH_RSA_512,"ALG_RSA_CRT LENGTH_RSA_512", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyPair(JCConsts.KeyPair_ALG_RSA_CRT,JCConsts.KeyBuilder_LENGTH_RSA_736,"ALG_RSA_CRT LENGTH_RSA_736", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyPair(JCConsts.KeyPair_ALG_RSA_CRT,JCConsts.KeyBuilder_LENGTH_RSA_768,"ALG_RSA_CRT LENGTH_RSA_768", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyPair(JCConsts.KeyPair_ALG_RSA_CRT,JCConsts.KeyBuilder_LENGTH_RSA_896,"ALG_RSA_CRT LENGTH_RSA_896", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyPair(JCConsts.KeyPair_ALG_RSA_CRT,JCConsts.KeyBuilder_LENGTH_RSA_1024,"ALG_RSA_CRT LENGTH_RSA_1024", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyPair(JCConsts.KeyPair_ALG_RSA_CRT,JCConsts.KeyBuilder_LENGTH_RSA_1280,"ALG_RSA_CRT LENGTH_RSA_1280", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyPair(JCConsts.KeyPair_ALG_RSA_CRT,JCConsts.KeyBuilder_LENGTH_RSA_1536,"ALG_RSA_CRT LENGTH_RSA_1536", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyPair(JCConsts.KeyPair_ALG_RSA_CRT,JCConsts.KeyBuilder_LENGTH_RSA_1984,"ALG_RSA_CRT LENGTH_RSA_1984", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyPair(JCConsts.KeyPair_ALG_RSA_CRT,JCConsts.KeyBuilder_LENGTH_RSA_2048,"ALG_RSA_CRT LENGTH_RSA_2048", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyPair(JCConsts.KeyPair_ALG_RSA_CRT,JCConsts.KeyBuilder_LENGTH_RSA_3072,"ALG_RSA_CRT LENGTH_RSA_3072", numRepeatWholeOperation, numRepeatWholeMeasurement);        
            testKeyPair(JCConsts.KeyPair_ALG_RSA_CRT,JCConsts.KeyBuilder_LENGTH_RSA_4096,"ALG_RSA_CRT LENGTH_RSA_4096", numRepeatWholeOperation, numRepeatWholeMeasurement);        

            // Ask for free of RSA keys cache
            m_cardManager.resetApplet(Consts.CLA_CARD_ALGTEST, Consts.INS_CARD_RESET, Consts.P1_CARD_RESET_FREE_CACHE);

            testKeyPair(JCConsts.KeyPair_ALG_DSA,JCConsts.KeyBuilder_LENGTH_DSA_512,"ALG_DSA LENGTH_DSA_512", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyPair(JCConsts.KeyPair_ALG_DSA,JCConsts.KeyBuilder_LENGTH_DSA_768,"ALG_DSA LENGTH_DSA_768", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyPair(JCConsts.KeyPair_ALG_DSA,JCConsts.KeyBuilder_LENGTH_DSA_1024,"ALG_DSA LENGTH_DSA_1024", numRepeatWholeOperation, numRepeatWholeMeasurement);        
            testKeyPair(JCConsts.KeyPair_ALG_EC_F2M,JCConsts.KeyBuilder_LENGTH_EC_F2M_113,"ALG_EC_F2M LENGTH_EC_F2M_113", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyPair(JCConsts.KeyPair_ALG_EC_F2M,JCConsts.KeyBuilder_LENGTH_EC_F2M_131,"ALG_EC_F2M LENGTH_EC_F2M_131", numRepeatWholeOperation, numRepeatWholeMeasurement);    
            testKeyPair(JCConsts.KeyPair_ALG_EC_F2M,JCConsts.KeyBuilder_LENGTH_EC_F2M_163,"ALG_EC_F2M LENGTH_EC_F2M_163", numRepeatWholeOperation, numRepeatWholeMeasurement);    
            testKeyPair(JCConsts.KeyPair_ALG_EC_F2M,JCConsts.KeyBuilder_LENGTH_EC_F2M_193,"ALG_EC_F2M LENGTH_EC_F2M_193", numRepeatWholeOperation, numRepeatWholeMeasurement);    
            testKeyPair(JCConsts.KeyPair_ALG_EC_FP,JCConsts.KeyBuilder_LENGTH_EC_FP_112,"ALG_EC_FP LENGTH_EC_FP_112", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyPair(JCConsts.KeyPair_ALG_EC_FP,JCConsts.KeyBuilder_LENGTH_EC_FP_128,"ALG_EC_FP LENGTH_EC_FP_128", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyPair(JCConsts.KeyPair_ALG_EC_FP,JCConsts.KeyBuilder_LENGTH_EC_FP_160,"ALG_EC_FP LENGTH_EC_FP_160", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyPair(JCConsts.KeyPair_ALG_EC_FP,JCConsts.KeyBuilder_LENGTH_EC_FP_192,"ALG_EC_FP LENGTH_EC_FP_192", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyPair(JCConsts.KeyPair_ALG_EC_FP,JCConsts.KeyBuilder_LENGTH_EC_FP_224,"ALG_EC_FP LENGTH_EC_FP_224", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyPair(JCConsts.KeyPair_ALG_EC_FP,JCConsts.KeyBuilder_LENGTH_EC_FP_256,"ALG_EC_FP LENGTH_EC_FP_256", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyPair(JCConsts.KeyPair_ALG_EC_FP,JCConsts.KeyBuilder_LENGTH_EC_FP_320,"ALG_EC_FP LENGTH_EC_FP_320", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyPair(JCConsts.KeyPair_ALG_EC_FP,JCConsts.KeyBuilder_LENGTH_EC_FP_384,"ALG_EC_FP LENGTH_EC_FP_384", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyPair(JCConsts.KeyPair_ALG_EC_FP,JCConsts.KeyBuilder_LENGTH_EC_FP_512,"ALG_EC_FP LENGTH_EC_FP_512", numRepeatWholeOperation, numRepeatWholeMeasurement); 
            testKeyPair(JCConsts.KeyPair_ALG_EC_FP,JCConsts.KeyBuilder_LENGTH_EC_FP_521,"ALG_EC_FP LENGTH_EC_FP_521", numRepeatWholeOperation, numRepeatWholeMeasurement); 
        }
        else {
            String message = "\n# Measurements excluded for asymmetric algorithms\n";
            m_perfResultsFile.write(message.getBytes());
        }
        tableName = "\n\nKEY PAIR - END\n";
        m_perfResultsFile.write(tableName.getBytes());
    }
    
    public void testMessageDigest(byte alg, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        TestSettings testSet = this.prepareTestSettings(Consts.CLASS_MESSAGEDIGEST, alg, Consts.UNUSED, Consts.UNUSED, 
                JCConsts.MessageDigest_update, Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);       

        if (!m_bTestVariableData) {
            // Ordinary test of all available methods
            testSet.algorithmMethod = JCConsts.MessageDigest_doFinal;
            double doFinalTime = this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_MESSAGEDIGEST, Consts.INS_PERF_TEST_CLASS_MESSAGEDIGEST, testSet, info + " MessageDigest_doFinal()");
            testSet.algorithmMethod = JCConsts.MessageDigest_update;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_MESSAGEDIGEST, Consts.INS_PERF_TEST_CLASS_MESSAGEDIGEST, testSet, info + " MessageDigest_update()", doFinalTime);
            testSet.algorithmMethod = JCConsts.MessageDigest_reset;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_MESSAGEDIGEST, Consts.INS_PERF_TEST_CLASS_MESSAGEDIGEST, testSet, info + " MessageDigest_reset()", doFinalTime);
        }
        else {
            // Test of speed dependant on data length
            String tableName = "\n\nMESSAGE DIGEST - "  + info + " - variable data - BEGIN\n";
            m_perfResultsFile.write(tableName.getBytes());
            testSet.algorithmMethod = JCConsts.MessageDigest_doFinal;
            for (Integer length : m_testDataLengths) {
                testSet.dataLength1 = length.shortValue();
                this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_MESSAGEDIGEST, Consts.INS_PERF_TEST_CLASS_MESSAGEDIGEST, testSet, info + " MessageDigest_doFinal();" + length + ";");
            }
            tableName = "\n\nMESSAGE DIGEST - "  + info + " - variable data - END\n";
            m_perfResultsFile.write(tableName.getBytes());
        }
    }   
    

    public void testAllMessageDigests(int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception {
        testAllMessageDigests((short) numRepeatWholeOperation, (short) numRepeatWholeMeasurement);
    }
    public void testAllMessageDigests(short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        String tableName = "\n\nMESSAGE DIGEST\n";
        m_perfResultsFile.write(tableName.getBytes());
        if (m_bTestSymmetricAlgs) {
            testMessageDigest(JCConsts.MessageDigest_ALG_SHA,"ALG_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testMessageDigest(JCConsts.MessageDigest_ALG_MD5,"ALG_MD5", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testMessageDigest(JCConsts.MessageDigest_ALG_RIPEMD160,"ALG_RIPEMD160", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testMessageDigest(JCConsts.MessageDigest_ALG_SHA_256,"ALG_SHA_256", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testMessageDigest(JCConsts.MessageDigest_ALG_SHA_384,"ALG_SHA_384", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testMessageDigest(JCConsts.MessageDigest_ALG_SHA_512,"ALG_SHA_512", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testMessageDigest(JCConsts.MessageDigest_ALG_SHA_224, "ALG_SHA_224", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testMessageDigest(JCConsts.MessageDigest_ALG_SHA3_224, "ALG_SHA3_224", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testMessageDigest(JCConsts.MessageDigest_ALG_SHA3_256, "ALG_SHA3_256", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testMessageDigest(JCConsts.MessageDigest_ALG_SHA3_384, "ALG_SHA3_384", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testMessageDigest(JCConsts.MessageDigest_ALG_SHA3_512, "ALG_SHA3_512", numRepeatWholeOperation, numRepeatWholeMeasurement);
        }
        else {
            String message = "\n# Measurements excluded for symmetric algorithms\n";
            m_perfResultsFile.write(message.getBytes());
        }
        tableName = "\n\nMESSAGE DIGEST - END\n";
        m_perfResultsFile.write(tableName.getBytes());
    }

    public void testRandomGenerator(byte alg, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        TestSettings testSet = this.prepareTestSettings(Consts.CLASS_RANDOMDATA, alg, Consts.UNUSED, Consts.UNUSED, JCConsts.RandomData_generateData, 
                Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);      

        if (!m_bTestVariableData) {
            // Ordinary test of all available methods
            testSet.algorithmMethod = JCConsts.RandomData_generateData;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_RANDOMDATA, Consts.INS_PERF_TEST_CLASS_RANDOMDATA, testSet, info + " RandomData_generateData()");
            testSet.algorithmMethod = JCConsts.RandomData_setSeed;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_RANDOMDATA, Consts.INS_PERF_TEST_CLASS_RANDOMDATA, testSet, info + " RandomData_setSeed()");        
        }
        else {
            // Test of speed dependant on data length
            String tableName = "\n\nRANDOM GENERATOR - "  + info + " - variable data - BEGIN\n";
            m_perfResultsFile.write(tableName.getBytes());
            testSet.algorithmMethod = JCConsts.RandomData_generateData;
            for (Integer length : m_testDataLengths) {
                testSet.dataLength1 = length.shortValue();
                this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_RANDOMDATA, Consts.INS_PERF_TEST_CLASS_RANDOMDATA, testSet, info + " RandomData_generateData();" + length + ";");
            }
            tableName = "\n\nRANDOM GENERATOR - "  + info + " - variable data - END\n";
            m_perfResultsFile.write(tableName.getBytes());
        }
    }    
    public void testAllRandomGenerators(int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception {
        testAllRandomGenerators((short) numRepeatWholeOperation, (short) numRepeatWholeMeasurement);
    }
    public void testAllRandomGenerators(short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        String tableName = "\n\nRANDOM GENERATOR\n";
        m_perfResultsFile.write(tableName.getBytes());
        if (m_bTestSymmetricAlgs) {
            testRandomGenerator(JCConsts.RandomData_ALG_PSEUDO_RANDOM,"ALG_PSEUDO_RANDOM", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRandomGenerator(JCConsts.RandomData_ALG_SECURE_RANDOM, "ALG_SECURE_RANDOM", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRandomGenerator(JCConsts.RandomData_ALG_TRNG, "ALG_TRNG", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRandomGenerator(JCConsts.RandomData_ALG_ALG_PRESEEDED_DRBG, "ALG_ALG_PRESEEDED_DRBG", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRandomGenerator(JCConsts.RandomData_ALG_FAST, "ALG_FAST", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRandomGenerator(JCConsts.RandomData_ALG_KEYGENERATION, "ALG_KEYGENERATION", numRepeatWholeOperation, numRepeatWholeMeasurement);
        }
        else {
            String message = "\n# Measurements excluded for symmetric algorithms\n";
            m_perfResultsFile.write(message.getBytes());
        }
        tableName = "\n\nRANDOM GENERATOR - END\n";
        m_perfResultsFile.write(tableName.getBytes());
    }    

    public void testCipher(byte key, short keyLength, byte alg, String info, short initMode, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        testCipherWithKeyClass(Consts.UNUSED, key, keyLength, alg, info, initMode, numRepeatWholeOperation, numRepeatWholeMeasurement);
    }
    public void testCipherWithKeyClass(short keyClass, byte key, short keyLength, byte alg, String info, short initMode, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        short testDataLength = Consts.TEST_DATA_LENGTH; // default test length
        switch (key) {
            case JCConsts.KeyBuilder_TYPE_RSA_PRIVATE:
            case JCConsts.KeyBuilder_TYPE_RSA_PUBLIC:
            case JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE:
                // For RSA, we need test length equal to modulus size 
                if (alg == JCConsts.Cipher_ALG_RSA_NOPAD) {
                    testDataLength = (short) (keyLength / 8);
                }
                else {
                    // or lower if padding is used
                    testDataLength = (short) ((short) (keyLength / 8) / 2);
                }
                break;
        }    
        
        TestSettings testSet = this.prepareTestSettings(Consts.CLASS_CIPHER, alg, key, keyLength, JCConsts.Cipher_doFinal, 
                testDataLength, Consts.UNUSED, initMode, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);      
        testSet.keyClass = keyClass;        

        
        if (!m_bTestVariableData) {
            // Ordinary test of all available methods
            //testSet.algorithmMethod = JCConsts.Cipher_update; // NOTE: Cipher_update is disabled as call on most cards will cause 6f00
            //this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CIPHER, Consts.INS_PERF_TEST_CLASS_CIPHER, testSet, info + " Cipher_update()");
            testSet.algorithmMethod = JCConsts.Cipher_doFinal;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CIPHER, Consts.INS_PERF_TEST_CLASS_CIPHER, testSet, info + " Cipher_doFinal()");
            testSet.algorithmMethod = JCConsts.Cipher_init;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CIPHER, Consts.INS_PERF_TEST_CLASS_CIPHER, testSet, info + " Cipher_init()");
        }
        else {
            // Test of speed dependent on data length
            String tableName = "\n\nCIPHER - " + info + " - variable data - BEGIN\n";
            m_perfResultsFile.write(tableName.getBytes());
            switch (key) {
                case JCConsts.KeyBuilder_TYPE_RSA_PRIVATE:
                case JCConsts.KeyBuilder_TYPE_RSA_PUBLIC:
                case JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE:
                    // For RSA, variable length perf test is not supported - use given fixed length
                    //m_perfResultsFile.write(tableName.getBytes());
                    //m_SystemOutLogger.print(tableName);
                    // Not supported => execute only with single data length
                    testSet.dataLength1 = testDataLength;
                    this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CIPHER, Consts.INS_PERF_TEST_CLASS_CIPHER, testSet, info + " Cipher_doFinal()");
                    return;
            }    
            // Measurement of only doFinal operation
            testSet.algorithmMethod = JCConsts.Cipher_doFinal;
            for (Integer length : m_testDataLengths) {
                testSet.dataLength1 = length.shortValue();
                this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CIPHER, Consts.INS_PERF_TEST_CLASS_CIPHER, testSet, info + " Cipher_doFinal();" + length + ";");
            }
            // Measurement of full process - Key.setKey, Cipher.init, Cipher.doFinal
            for (Integer length : m_testDataLengths) {
                testSet.dataLength1 = length.shortValue();
                this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CIPHER, Consts.INS_PERF_TEST_CLASS_CIPHER_SETKEYINITDOFINAL, testSet, info + " Cipher_setKeyInitDoFinal();" + length + ";");
            }
            tableName = "\n\nCIPHER - " + info + " - variable data - END\n";
            m_perfResultsFile.write(tableName.getBytes());
        }
    }
  
    public void testAllCiphers(int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception {
        testAllCiphers((short) numRepeatWholeOperation, (short) numRepeatWholeMeasurement);
    }
    public void testAllCiphers(short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        String tableName = "\n\nCIPHER\n";
        m_perfResultsFile.write(tableName.getBytes());
        if (m_bTestSymmetricAlgs) {
            testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES,JCConsts.Cipher_ALG_DES_CBC_NOPAD,"TYPE_DES LENGTH_DES ALG_DES_CBC_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES,JCConsts.Cipher_ALG_DES_CBC_ISO9797_M1,"TYPE_DES LENGTH_DES ALG_DES_CBC_ISO9797_M1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES,JCConsts.Cipher_ALG_DES_CBC_ISO9797_M2,"TYPE_DES LENGTH_DES ALG_DES_CBC_ISO9797_M2", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES,JCConsts.Cipher_ALG_DES_CBC_PKCS5,"TYPE_DES LENGTH_DES ALG_DES_CBC_PKCS5", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES,JCConsts.Cipher_ALG_DES_ECB_NOPAD,"TYPE_DES LENGTH_DES ALG_DES_ECB_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES,JCConsts.Cipher_ALG_DES_ECB_ISO9797_M1,"TYPE_DES LENGTH_DES ALG_DES_ECB_ISO9797_M1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES,JCConsts.Cipher_ALG_DES_ECB_ISO9797_M2,"TYPE_DES LENGTH_DES ALG_DES_ECB_ISO9797_M2", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES,JCConsts.Cipher_ALG_DES_ECB_PKCS5,"TYPE_DES LENGTH_DES ALG_DES_ECB_PKCS5", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement); 
            testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_2KEY,JCConsts.Cipher_ALG_DES_CBC_NOPAD,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_CBC_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_2KEY,JCConsts.Cipher_ALG_DES_CBC_ISO9797_M1,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_CBC_ISO9797_M1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_2KEY,JCConsts.Cipher_ALG_DES_CBC_ISO9797_M2,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_CBC_ISO9797_M2", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_2KEY,JCConsts.Cipher_ALG_DES_CBC_PKCS5,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_CBC_PKCS5", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_2KEY,JCConsts.Cipher_ALG_DES_ECB_NOPAD,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_ECB_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_2KEY,JCConsts.Cipher_ALG_DES_ECB_ISO9797_M1,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_ECB_ISO9797_M1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_2KEY,JCConsts.Cipher_ALG_DES_ECB_ISO9797_M2,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_ECB_ISO9797_M2", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_2KEY,JCConsts.Cipher_ALG_DES_ECB_PKCS5,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_ECB_PKCS5", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_3KEY,JCConsts.Cipher_ALG_DES_CBC_NOPAD,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_CBC_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_3KEY,JCConsts.Cipher_ALG_DES_CBC_ISO9797_M1,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_CBC_ISO9797_M1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_3KEY,JCConsts.Cipher_ALG_DES_CBC_ISO9797_M2,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_CBC_ISO9797_M2", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_3KEY,JCConsts.Cipher_ALG_DES_CBC_PKCS5,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_CBC_PKCS5", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_3KEY,JCConsts.Cipher_ALG_DES_ECB_NOPAD,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_ECB_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_3KEY,JCConsts.Cipher_ALG_DES_ECB_ISO9797_M1,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_ECB_ISO9797_M1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_3KEY,JCConsts.Cipher_ALG_DES_ECB_ISO9797_M2,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_ECB_ISO9797_M2", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_3KEY,JCConsts.Cipher_ALG_DES_ECB_PKCS5,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_ECB_PKCS5", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement); 

            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_128,JCConsts.Cipher_ALG_AES_BLOCK_128_CBC_NOPAD,"TYPE_AES LENGTH_AES_128 ALG_AES_BLOCK_128_CBC_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_128,JCConsts.Cipher_ALG_AES_BLOCK_128_ECB_NOPAD,"TYPE_AES LENGTH_AES_128 ALG_AES_BLOCK_128_ECB_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_128,JCConsts.Cipher_ALG_AES_BLOCK_192_CBC_NOPAD,"TYPE_AES LENGTH_AES_128 ALG_AES_BLOCK_192_CBC_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_128,JCConsts.Cipher_ALG_AES_BLOCK_192_ECB_NOPAD,"TYPE_AES LENGTH_AES_128 ALG_AES_BLOCK_192_ECB_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_128,JCConsts.Cipher_ALG_AES_BLOCK_256_CBC_NOPAD,"TYPE_AES LENGTH_AES_128 ALG_AES_BLOCK_256_CBC_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_128,JCConsts.Cipher_ALG_AES_BLOCK_256_ECB_NOPAD,"TYPE_AES LENGTH_AES_128 ALG_AES_BLOCK_256_ECB_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_128,JCConsts.Cipher_ALG_AES_CBC_ISO9797_M1,"TYPE_AES LENGTH_AES_128 ALG_AES_CBC_ISO9797_M1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_128,JCConsts.Cipher_ALG_AES_CBC_ISO9797_M2,"TYPE_AES LENGTH_AES_128 ALG_AES_CBC_ISO9797_M2", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_128,JCConsts.Cipher_ALG_AES_CBC_PKCS5,"TYPE_AES LENGTH_AES_128 ALG_AES_CBC_PKCS5", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_128,JCConsts.Cipher_ALG_AES_ECB_ISO9797_M1,"TYPE_AES LENGTH_AES_128 ALG_AES_ECB_ISO9797_M1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_128,JCConsts.Cipher_ALG_AES_ECB_ISO9797_M2,"TYPE_AES LENGTH_AES_128 ALG_AES_ECB_ISO9797_M2", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_128,JCConsts.Cipher_ALG_AES_ECB_PKCS5,"TYPE_AES LENGTH_AES_128 ALG_AES_ECB_PKCS5", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement); 
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_192,JCConsts.Cipher_ALG_AES_BLOCK_128_CBC_NOPAD,"TYPE_AES LENGTH_AES_192 ALG_AES_BLOCK_128_CBC_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_192,JCConsts.Cipher_ALG_AES_BLOCK_128_ECB_NOPAD,"TYPE_AES LENGTH_AES_192 ALG_AES_BLOCK_128_ECB_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_192,JCConsts.Cipher_ALG_AES_BLOCK_192_CBC_NOPAD,"TYPE_AES LENGTH_AES_192 ALG_AES_BLOCK_192_CBC_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_192,JCConsts.Cipher_ALG_AES_BLOCK_192_ECB_NOPAD,"TYPE_AES LENGTH_AES_192 ALG_AES_BLOCK_192_ECB_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_192,JCConsts.Cipher_ALG_AES_BLOCK_256_CBC_NOPAD,"TYPE_AES LENGTH_AES_192 ALG_AES_BLOCK_256_CBC_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_192,JCConsts.Cipher_ALG_AES_BLOCK_256_ECB_NOPAD,"TYPE_AES LENGTH_AES_192 ALG_AES_BLOCK_256_ECB_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_192,JCConsts.Cipher_ALG_AES_CBC_ISO9797_M1,"TYPE_AES LENGTH_AES_192 ALG_AES_CBC_ISO9797_M1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_192,JCConsts.Cipher_ALG_AES_CBC_ISO9797_M2,"TYPE_AES LENGTH_AES_192 ALG_AES_CBC_ISO9797_M2", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_192,JCConsts.Cipher_ALG_AES_CBC_PKCS5,"TYPE_AES LENGTH_AES_192 ALG_AES_CBC_PKCS5", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_192,JCConsts.Cipher_ALG_AES_ECB_ISO9797_M1,"TYPE_AES LENGTH_AES_192 ALG_AES_ECB_ISO9797_M1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_192,JCConsts.Cipher_ALG_AES_ECB_ISO9797_M2,"TYPE_AES LENGTH_AES_192 ALG_AES_ECB_ISO9797_M2", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_192,JCConsts.Cipher_ALG_AES_ECB_PKCS5,"TYPE_AES LENGTH_AES_192 ALG_AES_ECB_PKCS5", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement); 
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Cipher_ALG_AES_BLOCK_128_CBC_NOPAD,"TYPE_AES LENGTH_AES_256 ALG_AES_BLOCK_128_CBC_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_512,JCConsts.Cipher_ALG_AES_BLOCK_128_CBC_NOPAD,"TYPE_AES LENGTH_AES_512 ALG_AES_BLOCK_128_CBC_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Cipher_ALG_AES_BLOCK_128_ECB_NOPAD,"TYPE_AES LENGTH_AES_256 ALG_AES_BLOCK_128_ECB_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_512,JCConsts.Cipher_ALG_AES_BLOCK_128_ECB_NOPAD,"TYPE_AES LENGTH_AES_512 ALG_AES_BLOCK_128_ECB_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Cipher_ALG_AES_BLOCK_192_CBC_NOPAD,"TYPE_AES LENGTH_AES_256 ALG_AES_BLOCK_192_CBC_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_512,JCConsts.Cipher_ALG_AES_BLOCK_192_CBC_NOPAD,"TYPE_AES LENGTH_AES_512 ALG_AES_BLOCK_192_CBC_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Cipher_ALG_AES_BLOCK_192_ECB_NOPAD,"TYPE_AES LENGTH_AES_256 ALG_AES_BLOCK_192_ECB_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_512,JCConsts.Cipher_ALG_AES_BLOCK_192_ECB_NOPAD,"TYPE_AES LENGTH_AES_512 ALG_AES_BLOCK_192_ECB_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Cipher_ALG_AES_BLOCK_256_CBC_NOPAD,"TYPE_AES LENGTH_AES_256 ALG_AES_BLOCK_256_CBC_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_512,JCConsts.Cipher_ALG_AES_BLOCK_256_CBC_NOPAD,"TYPE_AES LENGTH_AES_512 ALG_AES_BLOCK_256_CBC_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Cipher_ALG_AES_BLOCK_256_ECB_NOPAD,"TYPE_AES LENGTH_AES_256 ALG_AES_BLOCK_256_ECB_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_512,JCConsts.Cipher_ALG_AES_BLOCK_256_ECB_NOPAD,"TYPE_AES LENGTH_AES_512 ALG_AES_BLOCK_256_ECB_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Cipher_ALG_AES_CBC_ISO9797_M1,"TYPE_AES LENGTH_AES_256 ALG_AES_CBC_ISO9797_M1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_512,JCConsts.Cipher_ALG_AES_CBC_ISO9797_M1,"TYPE_AES LENGTH_AES_512 ALG_AES_CBC_ISO9797_M1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Cipher_ALG_AES_CBC_ISO9797_M2,"TYPE_AES LENGTH_AES_256 ALG_AES_CBC_ISO9797_M2", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_512,JCConsts.Cipher_ALG_AES_CBC_ISO9797_M2,"TYPE_AES LENGTH_AES_512 ALG_AES_CBC_ISO9797_M2", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Cipher_ALG_AES_CBC_PKCS5,"TYPE_AES LENGTH_AES_256 ALG_AES_CBC_PKCS5", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_512,JCConsts.Cipher_ALG_AES_CBC_PKCS5,"TYPE_AES LENGTH_AES_512 ALG_AES_CBC_PKCS5", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Cipher_ALG_AES_ECB_ISO9797_M1,"TYPE_AES LENGTH_AES_256 ALG_AES_ECB_ISO9797_M1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_512,JCConsts.Cipher_ALG_AES_ECB_ISO9797_M1,"TYPE_AES LENGTH_AES_512 ALG_AES_ECB_ISO9797_M1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Cipher_ALG_AES_ECB_ISO9797_M2,"TYPE_AES LENGTH_AES_256 ALG_AES_ECB_ISO9797_M2", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_512,JCConsts.Cipher_ALG_AES_ECB_ISO9797_M2,"TYPE_AES LENGTH_AES_512 ALG_AES_ECB_ISO9797_M2", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Cipher_ALG_AES_ECB_PKCS5,"TYPE_AES LENGTH_AES_256 ALG_AES_ECB_PKCS5", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement); 
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_512,JCConsts.Cipher_ALG_AES_ECB_PKCS5,"TYPE_AES LENGTH_AES_512 ALG_AES_ECB_PKCS5", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement); 
        }
        else {
            String message = "\n# Measurements excluded for symmetric algorithms\n";
            m_perfResultsFile.write(message.getBytes());
        }
        if (m_bTestAsymmetricAlgs) {
            // ALG_RSA TYPE_RSA_PRIVATE
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_PRIVATE LENGTH_RSA_512 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_PRIVATE LENGTH_RSA_512 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_PRIVATE LENGTH_RSA_512 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_PRIVATE LENGTH_RSA_512 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_PRIVATE LENGTH_RSA_512 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_PRIVATE LENGTH_RSA_736 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_PRIVATE LENGTH_RSA_736 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_PRIVATE LENGTH_RSA_736 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_PRIVATE LENGTH_RSA_736 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_PRIVATE LENGTH_RSA_736 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_PRIVATE LENGTH_RSA_768 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_PRIVATE LENGTH_RSA_768 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_PRIVATE LENGTH_RSA_768 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_PRIVATE LENGTH_RSA_768 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_PRIVATE LENGTH_RSA_768 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_PRIVATE LENGTH_RSA_896 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_PRIVATE LENGTH_RSA_896 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_PRIVATE LENGTH_RSA_896 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_PRIVATE LENGTH_RSA_896 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_PRIVATE LENGTH_RSA_896 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_PRIVATE LENGTH_RSA_1024 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_PRIVATE LENGTH_RSA_1024 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_PRIVATE LENGTH_RSA_1024 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_PRIVATE LENGTH_RSA_1024 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_PRIVATE LENGTH_RSA_1024 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_PRIVATE LENGTH_RSA_1280 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_PRIVATE LENGTH_RSA_1280 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_PRIVATE LENGTH_RSA_1280 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_PRIVATE LENGTH_RSA_1280 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_PRIVATE LENGTH_RSA_1280 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_PRIVATE LENGTH_RSA_1536 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_PRIVATE LENGTH_RSA_1536 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_PRIVATE LENGTH_RSA_1536 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_PRIVATE LENGTH_RSA_1536 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_PRIVATE LENGTH_RSA_1536 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_PRIVATE LENGTH_RSA_1984 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_PRIVATE LENGTH_RSA_1984 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_PRIVATE LENGTH_RSA_1984 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_PRIVATE LENGTH_RSA_1984 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_PRIVATE LENGTH_RSA_1984 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_PRIVATE LENGTH_RSA_2048 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_PRIVATE LENGTH_RSA_2048 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_PRIVATE LENGTH_RSA_2048 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_PRIVATE LENGTH_RSA_2048 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_PRIVATE LENGTH_RSA_2048 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_PRIVATE LENGTH_RSA_3072 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_PRIVATE LENGTH_RSA_3072 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_PRIVATE LENGTH_RSA_3072 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_PRIVATE LENGTH_RSA_3072 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_PRIVATE LENGTH_RSA_3072 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_PRIVATE LENGTH_RSA_4096 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_PRIVATE LENGTH_RSA_4096 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_PRIVATE LENGTH_RSA_4096 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_PRIVATE LENGTH_RSA_4096 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_PRIVATE LENGTH_RSA_4096 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement); 
            // ALG_RSA TYPE_RSA_PUBLIC
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_PUBLIC LENGTH_RSA_512 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_PUBLIC LENGTH_RSA_512 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_PUBLIC LENGTH_RSA_512 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_PUBLIC LENGTH_RSA_512 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_PUBLIC LENGTH_RSA_512 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_PUBLIC LENGTH_RSA_736 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_PUBLIC LENGTH_RSA_736 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_PUBLIC LENGTH_RSA_736 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_PUBLIC LENGTH_RSA_736 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_PUBLIC LENGTH_RSA_736 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_PUBLIC LENGTH_RSA_768 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_PUBLIC LENGTH_RSA_768 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_PUBLIC LENGTH_RSA_768 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_PUBLIC LENGTH_RSA_768 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_PUBLIC LENGTH_RSA_768 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_PUBLIC LENGTH_RSA_896 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_PUBLIC LENGTH_RSA_896 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_PUBLIC LENGTH_RSA_896 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_PUBLIC LENGTH_RSA_896 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_PUBLIC LENGTH_RSA_896 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_PUBLIC LENGTH_RSA_1024 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_PUBLIC LENGTH_RSA_1024 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_PUBLIC LENGTH_RSA_1024 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_PUBLIC LENGTH_RSA_1024 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_PUBLIC LENGTH_RSA_1024 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_PUBLIC LENGTH_RSA_1280 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_PUBLIC LENGTH_RSA_1280 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_PUBLIC LENGTH_RSA_1280 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_PUBLIC LENGTH_RSA_1280 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_PUBLIC LENGTH_RSA_1280 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_PUBLIC LENGTH_RSA_1536 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_PUBLIC LENGTH_RSA_1536 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_PUBLIC LENGTH_RSA_1536 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_PUBLIC LENGTH_RSA_1536 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_PUBLIC LENGTH_RSA_1536 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_PUBLIC LENGTH_RSA_1984 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_PUBLIC LENGTH_RSA_1984 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_PUBLIC LENGTH_RSA_1984 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_PUBLIC LENGTH_RSA_1984 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_PUBLIC LENGTH_RSA_1984 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_PUBLIC LENGTH_RSA_2048 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_PUBLIC LENGTH_RSA_2048 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_PUBLIC LENGTH_RSA_2048 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_PUBLIC LENGTH_RSA_2048 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_PUBLIC LENGTH_RSA_2048 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_PUBLIC LENGTH_RSA_3072 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_PUBLIC LENGTH_RSA_3072 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_PUBLIC LENGTH_RSA_3072 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_PUBLIC LENGTH_RSA_3072 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_PUBLIC LENGTH_RSA_3072 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_PUBLIC LENGTH_RSA_4096 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_PUBLIC LENGTH_RSA_4096 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_PUBLIC LENGTH_RSA_4096 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_PUBLIC LENGTH_RSA_4096 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_PUBLIC LENGTH_RSA_4096 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement); 
            // ALG_RSA_CRT
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_512 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_512 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_512 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_512 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_512 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_736 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_736 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_736 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_736 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_736 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_768 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_768 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_768 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_768 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_768 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_896 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_896 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_896 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_896 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_896 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1024 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1024 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1024 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1024 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1024 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1280 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1280 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1280 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1280 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1280 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1536 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1536 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1536 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1536 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1536 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1984 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1984 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1984 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1984 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1984 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_2048 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_2048 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_2048 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_2048 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_2048 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_3072 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_3072 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_3072 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_3072 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_3072 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_4096 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_4096 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_4096 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_4096 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_CRT_PRIVATE LENGTH_RSA_4096 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_DECRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement); 
            // KeyBuilder_TYPE_RSA_PUBLIC CRT
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_512 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_512 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_512 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_512 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_512 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_736 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_736 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_736 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_736 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_736 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_768 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_768 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_768 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_768 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_768 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_896 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_896 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_896 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_896 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_896 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_1024 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_1024 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_1024 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_1024 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_1024 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_1280 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_1280 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_1280 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_1280 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_1280 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_1536 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_1536 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_1536 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_1536 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_1536 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_1984 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_1984 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_1984 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_1984 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_1984 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_2048 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_2048 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_2048 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_2048 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_2048 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_3072 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_3072 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_3072 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_3072 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_3072 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_4096 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_4096 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_4096 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_4096 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipherWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_CRT_PUBLIC LENGTH_RSA_4096 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement); 
            
            // Ask for free of RSA keys cache
            m_cardManager.resetApplet(Consts.CLA_CARD_ALGTEST, Consts.INS_CARD_RESET, Consts.P1_CARD_RESET_FREE_CACHE);
         }
        else {
            String message = "\n# Measurements excluded for asymmetric algorithms\n";
            m_perfResultsFile.write(message.getBytes());
        }        
        tableName = "\n\nCIPHER - END\n";
        m_perfResultsFile.write(tableName.getBytes());
    }
    
    public void testSignature(byte keyType, short keyLength, byte alg, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        testSignatureWithKeyClass(Consts.UNUSED, keyType, keyLength, alg, info, numRepeatWholeOperation, numRepeatWholeMeasurement);
    }   
    public void testSignatureWithKeyClass(byte keyClass, byte keyType, short keyLength, byte alg, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        TestSettings testSet = this.prepareTestSettings(Consts.CLASS_SIGNATURE, alg, keyType, keyLength, JCConsts.Signature_sign, 
            Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);      
        testSet.keyClass = keyClass;
        
        if (!m_bTestVariableData) {
            //testSet.algorithmMethod = JCConsts.Signature_update; // NOTE: Signature_update is disabled as call on most cards will cause 6f00
            //this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_SIGNATURE, Consts.INS_PERF_TEST_CLASS_SIGNATURE, testSet, info + " Signature_update()");
            testSet.algorithmMethod = JCConsts.Signature_sign;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_SIGNATURE, Consts.INS_PERF_TEST_CLASS_SIGNATURE, testSet, info + " Signature_sign()");
            testSet.algorithmMethod = JCConsts.Signature_verify;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_SIGNATURE, Consts.INS_PERF_TEST_CLASS_SIGNATURE, testSet, info + " Signature_verify()");
            testSet.algorithmMethod = JCConsts.Signature_init;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_SIGNATURE, Consts.INS_PERF_TEST_CLASS_SIGNATURE, testSet, info + " Signature_init()");
        }
        else {
            // Test of speed dependant on data length
            String tableName = "\n\nSIGNATURE - "  + info + " - variable data - BEGIN\n";
            m_perfResultsFile.write(tableName.getBytes());
            testSet.algorithmMethod = JCConsts.Signature_sign;
            for (Integer length : m_testDataLengths) {
                testSet.dataLength1 = length.shortValue();
                this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_SIGNATURE, Consts.INS_PERF_TEST_CLASS_SIGNATURE, testSet, info + " Signature_sign();" + length + ";");
            }
            // Measurement of full process - Key.setKey, Signature.init, Signature.sign
            for (Integer length : m_testDataLengths) {
                testSet.dataLength1 = length.shortValue();
                this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_SIGNATURE, Consts.INS_PERF_TEST_CLASS_SIGNATURE_SETKEYINITSIGN, testSet, info + " Signature_setKeyInitSign();" + length + ";");
            }
            
            tableName = "\n\nSIGNATURE - "  + info + " - variable data - END\n";
            m_perfResultsFile.write(tableName.getBytes());
        }
    }
    public void testAllSignatures(int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception {
        testAllSignatures((short) numRepeatWholeOperation, (short) numRepeatWholeMeasurement);
    }
    public void testAllSignatures(short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        String tableName = "\n\nSIGNATURE\n";
        m_perfResultsFile.write(tableName.getBytes());
        if (m_bTestSymmetricAlgs) {
            testSignature(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_128,JCConsts.Signature_ALG_AES_MAC_128_NOPAD,"TYPE_AES LENGTH_AES_128 ALG_AES_MAC_128_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignature(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_192,JCConsts.Signature_ALG_AES_MAC_128_NOPAD,"TYPE_AES LENGTH_AES_192 ALG_AES_MAC_128_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignature(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Signature_ALG_AES_MAC_128_NOPAD,"TYPE_AES LENGTH_AES_256 ALG_AES_MAC_128_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignature(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_512,JCConsts.Signature_ALG_AES_MAC_128_NOPAD,"TYPE_AES LENGTH_AES_512 ALG_AES_MAC_128_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignature(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_2KEY,JCConsts.Signature_ALG_DES_MAC4_ISO9797_1_M2_ALG3,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_MAC4_ISO9797_1_M2_ALG3", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignature(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES,JCConsts.Signature_ALG_DES_MAC4_ISO9797_M1,"TYPE_DES LENGTH_DES ALG_DES_MAC4_ISO9797_M1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignature(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_2KEY,JCConsts.Signature_ALG_DES_MAC4_ISO9797_M1,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_MAC4_ISO9797_M1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignature(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_3KEY,JCConsts.Signature_ALG_DES_MAC4_ISO9797_M1,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_MAC4_ISO9797_M1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignature(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES,JCConsts.Signature_ALG_DES_MAC4_ISO9797_M2,"TYPE_DES LENGTH_DES ALG_DES_MAC4_ISO9797_M2", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignature(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_2KEY,JCConsts.Signature_ALG_DES_MAC4_ISO9797_M2,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_MAC4_ISO9797_M2", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignature(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_3KEY,JCConsts.Signature_ALG_DES_MAC4_ISO9797_M2,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_MAC4_ISO9797_M2", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignature(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES,JCConsts.Signature_ALG_DES_MAC4_NOPAD,"TYPE_DES LENGTH_DES ALG_DES_MAC4_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignature(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_2KEY,JCConsts.Signature_ALG_DES_MAC4_NOPAD,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_MAC4_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignature(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_3KEY,JCConsts.Signature_ALG_DES_MAC4_NOPAD,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_MAC4_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignature(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES,JCConsts.Signature_ALG_DES_MAC4_PKCS5,"TYPE_DES LENGTH_DES ALG_DES_MAC4_PKCS5", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignature(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_2KEY,JCConsts.Signature_ALG_DES_MAC4_PKCS5,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_MAC4_PKCS5", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignature(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_3KEY,JCConsts.Signature_ALG_DES_MAC4_PKCS5,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_MAC4_PKCS5", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignature(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_2KEY,JCConsts.Signature_ALG_DES_MAC8_ISO9797_1_M2_ALG3,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_MAC8_ISO9797_1_M2_ALG3", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignature(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES,JCConsts.Signature_ALG_DES_MAC8_ISO9797_M1,"TYPE_DES LENGTH_DES ALG_DES_MAC8_ISO9797_M1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignature(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_2KEY,JCConsts.Signature_ALG_DES_MAC8_ISO9797_M1,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_MAC8_ISO9797_M1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignature(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_3KEY,JCConsts.Signature_ALG_DES_MAC8_ISO9797_M1,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_MAC8_ISO9797_M1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignature(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES,JCConsts.Signature_ALG_DES_MAC8_ISO9797_M2,"TYPE_DES LENGTH_DES ALG_DES_MAC8_ISO9797_M2", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignature(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_2KEY,JCConsts.Signature_ALG_DES_MAC8_ISO9797_M2,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_MAC8_ISO9797_M2", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignature(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_3KEY,JCConsts.Signature_ALG_DES_MAC8_ISO9797_M2,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_MAC8_ISO9797_M2", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignature(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES,JCConsts.Signature_ALG_DES_MAC8_NOPAD,"TYPE_DES LENGTH_DES ALG_DES_MAC8_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignature(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_2KEY,JCConsts.Signature_ALG_DES_MAC8_NOPAD,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_MAC8_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignature(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_3KEY,JCConsts.Signature_ALG_DES_MAC8_NOPAD,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_MAC8_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignature(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES,JCConsts.Signature_ALG_DES_MAC8_PKCS5,"TYPE_DES LENGTH_DES ALG_DES_MAC8_PKCS5", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignature(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_2KEY,JCConsts.Signature_ALG_DES_MAC8_PKCS5,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_MAC8_PKCS5", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignature(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_3KEY,JCConsts.Signature_ALG_DES_MAC8_PKCS5,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_MAC8_PKCS5", numRepeatWholeOperation, numRepeatWholeMeasurement);
        }
        else {
            String message = "\n# Measurements excluded for symmetric algorithms\n";
            m_perfResultsFile.write(message.getBytes());
        }
        
        if (m_bTestAsymmetricAlgs) {
            // ALG_EC_F2M
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_F2M, JCConsts.KeyBuilder_TYPE_EC_F2M_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_F2M_113,JCConsts.Signature_ALG_ECDSA_SHA,"KeyPair_ALG_EC_F2M KeyBuilder_LENGTH_EC_F2M_113 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_F2M, JCConsts.KeyBuilder_TYPE_EC_F2M_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_F2M_131,JCConsts.Signature_ALG_ECDSA_SHA,"KeyPair_ALG_EC_F2M KeyBuilder_LENGTH_EC_F2M_131 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_F2M, JCConsts.KeyBuilder_TYPE_EC_F2M_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_F2M_163,JCConsts.Signature_ALG_ECDSA_SHA,"KeyPair_ALG_EC_F2M KeyBuilder_LENGTH_EC_F2M_163 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_F2M, JCConsts.KeyBuilder_TYPE_EC_F2M_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_F2M_193,JCConsts.Signature_ALG_ECDSA_SHA,"KeyPair_ALG_EC_F2M KeyBuilder_LENGTH_EC_F2M_193 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);

            // ALG_EC_FP
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_112,JCConsts.Signature_ALG_ECDSA_SHA,"KeyPair_ALG_EC_FP KeyBuilder_LENGTH_EC_FP_112 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_128,JCConsts.Signature_ALG_ECDSA_SHA,"KeyPair_ALG_EC_FP KeyBuilder_LENGTH_EC_FP_128 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_160,JCConsts.Signature_ALG_ECDSA_SHA,"KeyPair_ALG_EC_FP KeyBuilder_LENGTH_EC_FP_160 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_192,JCConsts.Signature_ALG_ECDSA_SHA,"KeyPair_ALG_EC_FP KeyBuilder_LENGTH_EC_FP_192 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_224,JCConsts.Signature_ALG_ECDSA_SHA,"KeyPair_ALG_EC_FP KeyBuilder_LENGTH_EC_FP_224 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_256,JCConsts.Signature_ALG_ECDSA_SHA,"KeyPair_ALG_EC_FP KeyBuilder_LENGTH_EC_FP_256 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_320,JCConsts.Signature_ALG_ECDSA_SHA,"KeyPair_ALG_EC_FP KeyBuilder_LENGTH_EC_FP_320 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_384,JCConsts.Signature_ALG_ECDSA_SHA,"KeyPair_ALG_EC_FP KeyBuilder_LENGTH_EC_FP_384 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_512,JCConsts.Signature_ALG_ECDSA_SHA,"KeyPair_ALG_EC_FP KeyBuilder_LENGTH_EC_FP_512 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_521,JCConsts.Signature_ALG_ECDSA_SHA,"KeyPair_ALG_EC_FP KeyBuilder_LENGTH_EC_FP_521 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);

            // DSA
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_DSA, JCConsts.KeyBuilder_TYPE_DSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_DSA_512,JCConsts.Signature_ALG_DSA_SHA,"ALG_DSA LENGTH_DSA_512 ALG_DSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_DSA, JCConsts.KeyBuilder_TYPE_DSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_DSA_768,JCConsts.Signature_ALG_DSA_SHA,"ALG_DSA LENGTH_DSA_768 ALG_DSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_DSA, JCConsts.KeyBuilder_TYPE_DSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_DSA_1024,JCConsts.Signature_ALG_DSA_SHA,"ALG_DSA LENGTH_DSA_1024 ALG_DSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);

            // Ask for free of RSA keys cache
            m_cardManager.resetApplet(Consts.CLA_CARD_ALGTEST, Consts.INS_CARD_RESET, Consts.P1_CARD_RESET_FREE_CACHE);

            // ALG_RSA
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Signature_ALG_RSA_MD5_PKCS1,"ALG_RSA LENGTH_RSA_512 ALG_RSA_MD5_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Signature_ALG_RSA_MD5_PKCS1,"ALG_RSA LENGTH_RSA_736 ALG_RSA_MD5_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Signature_ALG_RSA_MD5_PKCS1,"ALG_RSA LENGTH_RSA_768 ALG_RSA_MD5_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Signature_ALG_RSA_MD5_PKCS1,"ALG_RSA LENGTH_RSA_896 ALG_RSA_MD5_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Signature_ALG_RSA_MD5_PKCS1,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_MD5_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Signature_ALG_RSA_MD5_PKCS1,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_MD5_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Signature_ALG_RSA_MD5_PKCS1,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_MD5_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Signature_ALG_RSA_MD5_PKCS1,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_MD5_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);  
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Signature_ALG_RSA_MD5_PKCS1,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_MD5_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Signature_ALG_RSA_MD5_PKCS1,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_MD5_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Signature_ALG_RSA_MD5_PKCS1,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_MD5_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);

            // Ask for free of RSA keys cache
            m_cardManager.resetApplet(Consts.CLA_CARD_ALGTEST, Consts.INS_CARD_RESET, Consts.P1_CARD_RESET_FREE_CACHE);
            
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Signature_ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA LENGTH_RSA_512 ALG_RSA_MD5_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Signature_ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA LENGTH_RSA_736 ALG_RSA_MD5_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Signature_ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA LENGTH_RSA_768 ALG_RSA_MD5_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Signature_ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA LENGTH_RSA_896 ALG_RSA_MD5_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Signature_ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_MD5_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Signature_ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_MD5_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Signature_ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_MD5_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Signature_ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_MD5_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);  
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Signature_ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_MD5_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Signature_ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_MD5_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Signature_ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_MD5_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);

            // Ask for free of RSA keys cache
            m_cardManager.resetApplet(Consts.CLA_CARD_ALGTEST, Consts.INS_CARD_RESET, Consts.P1_CARD_RESET_FREE_CACHE);
            
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Signature_ALG_RSA_MD5_RFC2409,"ALG_RSA LENGTH_RSA_512 ALG_RSA_MD5_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Signature_ALG_RSA_MD5_RFC2409,"ALG_RSA LENGTH_RSA_736 ALG_RSA_MD5_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Signature_ALG_RSA_MD5_RFC2409,"ALG_RSA LENGTH_RSA_768 ALG_RSA_MD5_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Signature_ALG_RSA_MD5_RFC2409,"ALG_RSA LENGTH_RSA_896 ALG_RSA_MD5_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Signature_ALG_RSA_MD5_RFC2409,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_MD5_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Signature_ALG_RSA_MD5_RFC2409,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_MD5_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Signature_ALG_RSA_MD5_RFC2409,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_MD5_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Signature_ALG_RSA_MD5_RFC2409,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_MD5_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);  
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Signature_ALG_RSA_MD5_RFC2409,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_MD5_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Signature_ALG_RSA_MD5_RFC2409,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_MD5_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Signature_ALG_RSA_MD5_RFC2409,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_MD5_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);

            // Ask for free of RSA keys cache
            m_cardManager.resetApplet(Consts.CLA_CARD_ALGTEST, Consts.INS_CARD_RESET, Consts.P1_CARD_RESET_FREE_CACHE);
            
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Signature_ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA LENGTH_RSA_512 ALG_RSA_RIPEMD160_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Signature_ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA LENGTH_RSA_736 ALG_RSA_RIPEMD160_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Signature_ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA LENGTH_RSA_768 ALG_RSA_RIPEMD160_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Signature_ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA LENGTH_RSA_896 ALG_RSA_RIPEMD160_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Signature_ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_RIPEMD160_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Signature_ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_RIPEMD160_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Signature_ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_RIPEMD160_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Signature_ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_RIPEMD160_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);  
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Signature_ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_RIPEMD160_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Signature_ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_RIPEMD160_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Signature_ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_RIPEMD160_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);

            // Ask for free of RSA keys cache
            m_cardManager.resetApplet(Consts.CLA_CARD_ALGTEST, Consts.INS_CARD_RESET, Consts.P1_CARD_RESET_FREE_CACHE);
            
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA LENGTH_RSA_512 ALG_RSA_RIPEMD160_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA LENGTH_RSA_736 ALG_RSA_RIPEMD160_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA LENGTH_RSA_768 ALG_RSA_RIPEMD160_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA LENGTH_RSA_896 ALG_RSA_RIPEMD160_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_RIPEMD160_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_RIPEMD160_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_RIPEMD160_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_RIPEMD160_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);  
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_RIPEMD160_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_RIPEMD160_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_RIPEMD160_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);

            // Ask for free of RSA keys cache
            m_cardManager.resetApplet(Consts.CLA_CARD_ALGTEST, Consts.INS_CARD_RESET, Consts.P1_CARD_RESET_FREE_CACHE);
            
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA LENGTH_RSA_512 ALG_RSA_RIPEMD160_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA LENGTH_RSA_736 ALG_RSA_RIPEMD160_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA LENGTH_RSA_768 ALG_RSA_RIPEMD160_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA LENGTH_RSA_896 ALG_RSA_RIPEMD160_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_RIPEMD160_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_RIPEMD160_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_RIPEMD160_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_RIPEMD160_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);  
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_RIPEMD160_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_RIPEMD160_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_RIPEMD160_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);

            // Ask for free of RSA keys cache
            m_cardManager.resetApplet(Consts.CLA_CARD_ALGTEST, Consts.INS_CARD_RESET, Consts.P1_CARD_RESET_FREE_CACHE);
            
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Signature_ALG_RSA_SHA_ISO9796,"ALG_RSA LENGTH_RSA_512 ALG_RSA_SHA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Signature_ALG_RSA_SHA_ISO9796,"ALG_RSA LENGTH_RSA_736 ALG_RSA_SHA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Signature_ALG_RSA_SHA_ISO9796,"ALG_RSA LENGTH_RSA_768 ALG_RSA_SHA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Signature_ALG_RSA_SHA_ISO9796,"ALG_RSA LENGTH_RSA_896 ALG_RSA_SHA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Signature_ALG_RSA_SHA_ISO9796,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_SHA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Signature_ALG_RSA_SHA_ISO9796,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_SHA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Signature_ALG_RSA_SHA_ISO9796,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_SHA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Signature_ALG_RSA_SHA_ISO9796,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_SHA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);  
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Signature_ALG_RSA_SHA_ISO9796,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_SHA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Signature_ALG_RSA_SHA_ISO9796,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_SHA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Signature_ALG_RSA_SHA_ISO9796,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_SHA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);

            // Ask for free of RSA keys cache
            m_cardManager.resetApplet(Consts.CLA_CARD_ALGTEST, Consts.INS_CARD_RESET, Consts.P1_CARD_RESET_FREE_CACHE);
            
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Signature_ALG_RSA_SHA_PKCS1,"ALG_RSA LENGTH_RSA_512 ALG_RSA_SHA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Signature_ALG_RSA_SHA_PKCS1,"ALG_RSA LENGTH_RSA_736 ALG_RSA_SHA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Signature_ALG_RSA_SHA_PKCS1,"ALG_RSA LENGTH_RSA_768 ALG_RSA_SHA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Signature_ALG_RSA_SHA_PKCS1,"ALG_RSA LENGTH_RSA_896 ALG_RSA_SHA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Signature_ALG_RSA_SHA_PKCS1,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_SHA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Signature_ALG_RSA_SHA_PKCS1,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_SHA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Signature_ALG_RSA_SHA_PKCS1,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_SHA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Signature_ALG_RSA_SHA_PKCS1,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_SHA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);  
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Signature_ALG_RSA_SHA_PKCS1,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_SHA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Signature_ALG_RSA_SHA_PKCS1,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_SHA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Signature_ALG_RSA_SHA_PKCS1,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_SHA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);

            // Ask for free of RSA keys cache
            m_cardManager.resetApplet(Consts.CLA_CARD_ALGTEST, Consts.INS_CARD_RESET, Consts.P1_CARD_RESET_FREE_CACHE);
            
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Signature_ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA LENGTH_RSA_512 ALG_RSA_SHA_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Signature_ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA LENGTH_RSA_736 ALG_RSA_SHA_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Signature_ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA LENGTH_RSA_768 ALG_RSA_SHA_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Signature_ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA LENGTH_RSA_896 ALG_RSA_SHA_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Signature_ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_SHA_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Signature_ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_SHA_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Signature_ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_SHA_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Signature_ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_SHA_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);  
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Signature_ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_SHA_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Signature_ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_SHA_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Signature_ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_SHA_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);

            // Ask for free of RSA keys cache
            m_cardManager.resetApplet(Consts.CLA_CARD_ALGTEST, Consts.INS_CARD_RESET, Consts.P1_CARD_RESET_FREE_CACHE);
            
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Signature_ALG_RSA_SHA_RFC2409,"ALG_RSA LENGTH_RSA_512 ALG_RSA_SHA_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Signature_ALG_RSA_SHA_RFC2409,"ALG_RSA LENGTH_RSA_736 ALG_RSA_SHA_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Signature_ALG_RSA_SHA_RFC2409,"ALG_RSA LENGTH_RSA_768 ALG_RSA_SHA_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Signature_ALG_RSA_SHA_RFC2409,"ALG_RSA LENGTH_RSA_896 ALG_RSA_SHA_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Signature_ALG_RSA_SHA_RFC2409,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_SHA_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Signature_ALG_RSA_SHA_RFC2409,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_SHA_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Signature_ALG_RSA_SHA_RFC2409,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_SHA_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Signature_ALG_RSA_SHA_RFC2409,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_SHA_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);  
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Signature_ALG_RSA_SHA_RFC2409,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_SHA_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Signature_ALG_RSA_SHA_RFC2409,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_SHA_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Signature_ALG_RSA_SHA_RFC2409,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_SHA_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);

            // Ask for free of RSA keys cache
            m_cardManager.resetApplet(Consts.CLA_CARD_ALGTEST, Consts.INS_CARD_RESET, Consts.P1_CARD_RESET_FREE_CACHE);

            // ALG_RSA_CRT
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Signature_ALG_RSA_MD5_PKCS1,"ALG_RSA_CRT LENGTH_RSA_512 ALG_RSA_MD5_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Signature_ALG_RSA_MD5_PKCS1,"ALG_RSA_CRT LENGTH_RSA_736 ALG_RSA_MD5_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Signature_ALG_RSA_MD5_PKCS1,"ALG_RSA_CRT LENGTH_RSA_768 ALG_RSA_MD5_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Signature_ALG_RSA_MD5_PKCS1,"ALG_RSA_CRT LENGTH_RSA_896 ALG_RSA_MD5_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Signature_ALG_RSA_MD5_PKCS1,"ALG_RSA_CRT LENGTH_RSA_1024 ALG_RSA_MD5_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Signature_ALG_RSA_MD5_PKCS1,"ALG_RSA_CRT LENGTH_RSA_1280 ALG_RSA_MD5_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Signature_ALG_RSA_MD5_PKCS1,"ALG_RSA_CRT LENGTH_RSA_1536 ALG_RSA_MD5_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Signature_ALG_RSA_MD5_PKCS1,"ALG_RSA_CRT LENGTH_RSA_1984 ALG_RSA_MD5_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);  
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Signature_ALG_RSA_MD5_PKCS1,"ALG_RSA_CRT LENGTH_RSA_2048 ALG_RSA_MD5_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Signature_ALG_RSA_MD5_PKCS1,"ALG_RSA_CRT LENGTH_RSA_3072 ALG_RSA_MD5_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Signature_ALG_RSA_MD5_PKCS1,"ALG_RSA_CRT LENGTH_RSA_4096 ALG_RSA_MD5_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);

            // Ask for free of RSA keys cache
            m_cardManager.resetApplet(Consts.CLA_CARD_ALGTEST, Consts.INS_CARD_RESET, Consts.P1_CARD_RESET_FREE_CACHE);
            
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Signature_ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA_CRT LENGTH_RSA_512 ALG_RSA_MD5_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Signature_ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA_CRT LENGTH_RSA_736 ALG_RSA_MD5_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Signature_ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA_CRT LENGTH_RSA_768 ALG_RSA_MD5_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Signature_ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA_CRT LENGTH_RSA_896 ALG_RSA_MD5_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Signature_ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA_CRT LENGTH_RSA_1024 ALG_RSA_MD5_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Signature_ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA_CRT LENGTH_RSA_1280 ALG_RSA_MD5_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Signature_ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA_CRT LENGTH_RSA_1536 ALG_RSA_MD5_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Signature_ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA_CRT LENGTH_RSA_1984 ALG_RSA_MD5_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);  
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Signature_ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA_CRT LENGTH_RSA_2048 ALG_RSA_MD5_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Signature_ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA_CRT LENGTH_RSA_3072 ALG_RSA_MD5_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Signature_ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA_CRT LENGTH_RSA_4096 ALG_RSA_MD5_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);

            // Ask for free of RSA keys cache
            m_cardManager.resetApplet(Consts.CLA_CARD_ALGTEST, Consts.INS_CARD_RESET, Consts.P1_CARD_RESET_FREE_CACHE);
            
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Signature_ALG_RSA_MD5_RFC2409,"ALG_RSA_CRT LENGTH_RSA_512 ALG_RSA_MD5_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Signature_ALG_RSA_MD5_RFC2409,"ALG_RSA_CRT LENGTH_RSA_736 ALG_RSA_MD5_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Signature_ALG_RSA_MD5_RFC2409,"ALG_RSA_CRT LENGTH_RSA_768 ALG_RSA_MD5_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Signature_ALG_RSA_MD5_RFC2409,"ALG_RSA_CRT LENGTH_RSA_896 ALG_RSA_MD5_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Signature_ALG_RSA_MD5_RFC2409,"ALG_RSA_CRT LENGTH_RSA_1024 ALG_RSA_MD5_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Signature_ALG_RSA_MD5_RFC2409,"ALG_RSA_CRT LENGTH_RSA_1280 ALG_RSA_MD5_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Signature_ALG_RSA_MD5_RFC2409,"ALG_RSA_CRT LENGTH_RSA_1536 ALG_RSA_MD5_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Signature_ALG_RSA_MD5_RFC2409,"ALG_RSA_CRT LENGTH_RSA_1984 ALG_RSA_MD5_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);  
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Signature_ALG_RSA_MD5_RFC2409,"ALG_RSA_CRT LENGTH_RSA_2048 ALG_RSA_MD5_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Signature_ALG_RSA_MD5_RFC2409,"ALG_RSA_CRT LENGTH_RSA_3072 ALG_RSA_MD5_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Signature_ALG_RSA_MD5_RFC2409,"ALG_RSA_CRT LENGTH_RSA_4096 ALG_RSA_MD5_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);

            // Ask for free of RSA keys cache
            m_cardManager.resetApplet(Consts.CLA_CARD_ALGTEST, Consts.INS_CARD_RESET, Consts.P1_CARD_RESET_FREE_CACHE);
            
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Signature_ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA_CRT LENGTH_RSA_512 ALG_RSA_RIPEMD160_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Signature_ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA_CRT LENGTH_RSA_736 ALG_RSA_RIPEMD160_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Signature_ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA_CRT LENGTH_RSA_768 ALG_RSA_RIPEMD160_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Signature_ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA_CRT LENGTH_RSA_896 ALG_RSA_RIPEMD160_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Signature_ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA_CRT LENGTH_RSA_1024 ALG_RSA_RIPEMD160_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Signature_ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA_CRT LENGTH_RSA_1280 ALG_RSA_RIPEMD160_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Signature_ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA_CRT LENGTH_RSA_1536 ALG_RSA_RIPEMD160_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Signature_ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA_CRT LENGTH_RSA_1984 ALG_RSA_RIPEMD160_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);  
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Signature_ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA_CRT LENGTH_RSA_2048 ALG_RSA_RIPEMD160_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Signature_ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA_CRT LENGTH_RSA_3072 ALG_RSA_RIPEMD160_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Signature_ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA_CRT LENGTH_RSA_4096 ALG_RSA_RIPEMD160_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);

            // Ask for free of RSA keys cache
            m_cardManager.resetApplet(Consts.CLA_CARD_ALGTEST, Consts.INS_CARD_RESET, Consts.P1_CARD_RESET_FREE_CACHE);

            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA_CRT LENGTH_RSA_512 ALG_RSA_RIPEMD160_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA_CRT LENGTH_RSA_736 ALG_RSA_RIPEMD160_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA_CRT LENGTH_RSA_768 ALG_RSA_RIPEMD160_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA_CRT LENGTH_RSA_896 ALG_RSA_RIPEMD160_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA_CRT LENGTH_RSA_1024 ALG_RSA_RIPEMD160_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA_CRT LENGTH_RSA_1280 ALG_RSA_RIPEMD160_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA_CRT LENGTH_RSA_1536 ALG_RSA_RIPEMD160_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA_CRT LENGTH_RSA_1984 ALG_RSA_RIPEMD160_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);  
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA_CRT LENGTH_RSA_2048 ALG_RSA_RIPEMD160_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA_CRT LENGTH_RSA_3072 ALG_RSA_RIPEMD160_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA_CRT LENGTH_RSA_4096 ALG_RSA_RIPEMD160_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);

            // Ask for free of RSA keys cache
            m_cardManager.resetApplet(Consts.CLA_CARD_ALGTEST, Consts.INS_CARD_RESET, Consts.P1_CARD_RESET_FREE_CACHE);
            
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA_CRT LENGTH_RSA_512 ALG_RSA_RIPEMD160_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA_CRT LENGTH_RSA_736 ALG_RSA_RIPEMD160_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA_CRT LENGTH_RSA_768 ALG_RSA_RIPEMD160_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA_CRT LENGTH_RSA_896 ALG_RSA_RIPEMD160_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA_CRT LENGTH_RSA_1024 ALG_RSA_RIPEMD160_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA_CRT LENGTH_RSA_1280 ALG_RSA_RIPEMD160_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA_CRT LENGTH_RSA_1536 ALG_RSA_RIPEMD160_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA_CRT LENGTH_RSA_1984 ALG_RSA_RIPEMD160_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);  
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA_CRT LENGTH_RSA_2048 ALG_RSA_RIPEMD160_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA_CRT LENGTH_RSA_3072 ALG_RSA_RIPEMD160_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA_CRT LENGTH_RSA_4096 ALG_RSA_RIPEMD160_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);

            // Ask for free of RSA keys cache
            m_cardManager.resetApplet(Consts.CLA_CARD_ALGTEST, Consts.INS_CARD_RESET, Consts.P1_CARD_RESET_FREE_CACHE);
            
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Signature_ALG_RSA_SHA_ISO9796,"ALG_RSA_CRT LENGTH_RSA_512 ALG_RSA_SHA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Signature_ALG_RSA_SHA_ISO9796,"ALG_RSA_CRT LENGTH_RSA_736 ALG_RSA_SHA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Signature_ALG_RSA_SHA_ISO9796,"ALG_RSA_CRT LENGTH_RSA_768 ALG_RSA_SHA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Signature_ALG_RSA_SHA_ISO9796,"ALG_RSA_CRT LENGTH_RSA_896 ALG_RSA_SHA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Signature_ALG_RSA_SHA_ISO9796,"ALG_RSA_CRT LENGTH_RSA_1024 ALG_RSA_SHA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Signature_ALG_RSA_SHA_ISO9796,"ALG_RSA_CRT LENGTH_RSA_1280 ALG_RSA_SHA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Signature_ALG_RSA_SHA_ISO9796,"ALG_RSA_CRT LENGTH_RSA_1536 ALG_RSA_SHA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Signature_ALG_RSA_SHA_ISO9796,"ALG_RSA_CRT LENGTH_RSA_1984 ALG_RSA_SHA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);  
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Signature_ALG_RSA_SHA_ISO9796,"ALG_RSA_CRT LENGTH_RSA_2048 ALG_RSA_SHA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Signature_ALG_RSA_SHA_ISO9796,"ALG_RSA_CRT LENGTH_RSA_3072 ALG_RSA_SHA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Signature_ALG_RSA_SHA_ISO9796,"ALG_RSA_CRT LENGTH_RSA_4096 ALG_RSA_SHA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);

            // Ask for free of RSA keys cache
            m_cardManager.resetApplet(Consts.CLA_CARD_ALGTEST, Consts.INS_CARD_RESET, Consts.P1_CARD_RESET_FREE_CACHE);
            
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Signature_ALG_RSA_SHA_PKCS1,"ALG_RSA_CRT LENGTH_RSA_512 ALG_RSA_SHA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Signature_ALG_RSA_SHA_PKCS1,"ALG_RSA_CRT LENGTH_RSA_736 ALG_RSA_SHA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Signature_ALG_RSA_SHA_PKCS1,"ALG_RSA_CRT LENGTH_RSA_768 ALG_RSA_SHA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Signature_ALG_RSA_SHA_PKCS1,"ALG_RSA_CRT LENGTH_RSA_896 ALG_RSA_SHA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Signature_ALG_RSA_SHA_PKCS1,"ALG_RSA_CRT LENGTH_RSA_1024 ALG_RSA_SHA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Signature_ALG_RSA_SHA_PKCS1,"ALG_RSA_CRT LENGTH_RSA_1280 ALG_RSA_SHA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Signature_ALG_RSA_SHA_PKCS1,"ALG_RSA_CRT LENGTH_RSA_1536 ALG_RSA_SHA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Signature_ALG_RSA_SHA_PKCS1,"ALG_RSA_CRT LENGTH_RSA_1984 ALG_RSA_SHA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);  
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Signature_ALG_RSA_SHA_PKCS1,"ALG_RSA_CRT LENGTH_RSA_2048 ALG_RSA_SHA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Signature_ALG_RSA_SHA_PKCS1,"ALG_RSA_CRT LENGTH_RSA_3072 ALG_RSA_SHA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Signature_ALG_RSA_SHA_PKCS1,"ALG_RSA_CRT LENGTH_RSA_4096 ALG_RSA_SHA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);

            // Ask for free of RSA keys cache
            m_cardManager.resetApplet(Consts.CLA_CARD_ALGTEST, Consts.INS_CARD_RESET, Consts.P1_CARD_RESET_FREE_CACHE);
            
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Signature_ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA_CRT LENGTH_RSA_512 ALG_RSA_SHA_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Signature_ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA_CRT LENGTH_RSA_736 ALG_RSA_SHA_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Signature_ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA_CRT LENGTH_RSA_768 ALG_RSA_SHA_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Signature_ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA_CRT LENGTH_RSA_896 ALG_RSA_SHA_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Signature_ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA_CRT LENGTH_RSA_1024 ALG_RSA_SHA_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Signature_ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA_CRT LENGTH_RSA_1280 ALG_RSA_SHA_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Signature_ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA_CRT LENGTH_RSA_1536 ALG_RSA_SHA_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Signature_ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA_CRT LENGTH_RSA_1984 ALG_RSA_SHA_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);  
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Signature_ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA_CRT LENGTH_RSA_2048 ALG_RSA_SHA_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Signature_ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA_CRT LENGTH_RSA_3072 ALG_RSA_SHA_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Signature_ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA_CRT LENGTH_RSA_4096 ALG_RSA_SHA_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);

            // Ask for free of RSA keys cache
            m_cardManager.resetApplet(Consts.CLA_CARD_ALGTEST, Consts.INS_CARD_RESET, Consts.P1_CARD_RESET_FREE_CACHE);

            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Signature_ALG_RSA_SHA_RFC2409,"ALG_RSA_CRT LENGTH_RSA_512 ALG_RSA_SHA_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Signature_ALG_RSA_SHA_RFC2409,"ALG_RSA_CRT LENGTH_RSA_736 ALG_RSA_SHA_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Signature_ALG_RSA_SHA_RFC2409,"ALG_RSA_CRT LENGTH_RSA_768 ALG_RSA_SHA_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Signature_ALG_RSA_SHA_RFC2409,"ALG_RSA_CRT LENGTH_RSA_896 ALG_RSA_SHA_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Signature_ALG_RSA_SHA_RFC2409,"ALG_RSA_CRT LENGTH_RSA_1024 ALG_RSA_SHA_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Signature_ALG_RSA_SHA_RFC2409,"ALG_RSA_CRT LENGTH_RSA_1280 ALG_RSA_SHA_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Signature_ALG_RSA_SHA_RFC2409,"ALG_RSA_CRT LENGTH_RSA_1536 ALG_RSA_SHA_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Signature_ALG_RSA_SHA_RFC2409,"ALG_RSA_CRT LENGTH_RSA_1984 ALG_RSA_SHA_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);  
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Signature_ALG_RSA_SHA_RFC2409,"ALG_RSA_CRT LENGTH_RSA_2048 ALG_RSA_SHA_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Signature_ALG_RSA_SHA_RFC2409,"ALG_RSA_CRT LENGTH_RSA_3072 ALG_RSA_SHA_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Signature_ALG_RSA_SHA_RFC2409,"ALG_RSA_CRT LENGTH_RSA_4096 ALG_RSA_SHA_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);

            // Ask for free of RSA keys cache
            m_cardManager.resetApplet(Consts.CLA_CARD_ALGTEST, Consts.INS_CARD_RESET, Consts.P1_CARD_RESET_FREE_CACHE);
        }
        else {
            String message = "\n# Measurements excluded for asymmetric algorithms\n";
            m_perfResultsFile.write(message.getBytes());
        }          
        
        tableName = "\n\nSIGNATURE - END\n";
        m_perfResultsFile.write(tableName.getBytes());        
    }    
    
    public void testChecksum(byte alg, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        TestSettings testSet = this.prepareTestSettings(Consts.CLASS_CHECKSUM, alg, Consts.UNUSED, Consts.UNUSED, JCConsts.Signature_update, 
            Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);      
        
        if (!m_bTestVariableData) {
            testSet.algorithmMethod = JCConsts.Checksum_update;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CHECKSUM, Consts.INS_PERF_TEST_CLASS_CHECKSUM, testSet, info + " Checksum_update()");
            testSet.algorithmMethod = JCConsts.Checksum_doFinal;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CHECKSUM, Consts.INS_PERF_TEST_CLASS_CHECKSUM, testSet, info + " Checksum_doFinal()");
        }
        else {
            // Test of speed dependant on data length
            String tableName = "\n\nCHECKSUM - "  + info + " - variable data - BEGIN\n";
            m_perfResultsFile.write(tableName.getBytes());
            testSet.algorithmMethod = JCConsts.Checksum_doFinal;
            for (Integer length : m_testDataLengths) {
                testSet.dataLength1 = length.shortValue();
                this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CHECKSUM, Consts.INS_PERF_TEST_CLASS_CHECKSUM, testSet, info + " Checksum_doFinal();" + length + ";");
            }
            tableName = "\n\nCHECKSUM - "  + info + " - variable data - END\n";
            m_perfResultsFile.write(tableName.getBytes());
        }
    }   
    public void testAllChecksums(int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception {
        testAllChecksums((short) numRepeatWholeOperation, (short) numRepeatWholeMeasurement);
    }
    public void testAllChecksums(short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        String tableName = "\n\nCHECKSUM\n";
        m_perfResultsFile.write(tableName.getBytes());
        if (m_bTestSymmetricAlgs) {
            testChecksum(JCConsts.Checksum_ALG_ISO3309_CRC16,"ALG_ISO3309_CRC16", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testChecksum(JCConsts.Checksum_ALG_ISO3309_CRC32,"ALG_ISO3309_CRC32", numRepeatWholeOperation, numRepeatWholeMeasurement);
        }
        else {
            String message = "\n# Measurements excluded for symmetric algorithms\n";
            m_perfResultsFile.write(message.getBytes());
        }
        tableName = "\n\nCHECKSUM - END\n";
        m_perfResultsFile.write(tableName.getBytes());
    }    

    public void testKeyAgreementWithKeyClass(byte keyClass, byte keyType, short keyLength, byte alg, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        TestSettings testSet = this.prepareTestSettings(Consts.CLASS_KEYAGREEMENT, alg, keyType, keyLength, JCConsts.KeyAgreement_init,
                Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);
        testSet.keyClass = keyClass;

        if (!m_bTestVariableData) {
            testSet.algorithmMethod = JCConsts.KeyAgreement_init;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEYAGREEMENT, Consts.INS_PERF_TEST_CLASS_KEYAGREEMENT, testSet, info + " KeyAgreement_init()");
            testSet.algorithmMethod = JCConsts.KeyAgreement_generateSecret;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEYAGREEMENT, Consts.INS_PERF_TEST_CLASS_KEYAGREEMENT, testSet, info + " KeyAgreement_generateSecret()");
        } else {
            // Test of speed dependant on data length
            String tableName = "\n\nKEYAGREEMENT - " + info + " - variable data - BEGIN\n";
            m_perfResultsFile.write(tableName.getBytes());
            m_SystemOutLogger.print(tableName);
            tableName = "For KeyAgreement, variable length perf test is not supported\n";
            m_perfResultsFile.write(tableName.getBytes());
            m_SystemOutLogger.print(tableName);
            
            tableName = "\n\nKEYAGREEMENT - " + info + " - variable data - END\n";
            m_perfResultsFile.write(tableName.getBytes());
        }
    }    
    public void testAllKeyAgreement(int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception {
        testAllKeyAgreement((short) numRepeatWholeOperation, (short) numRepeatWholeMeasurement);
    }
    public void testAllKeyAgreement(short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        String tableName = "\n\nKEYAGREEMENT\n";
        m_perfResultsFile.write(tableName.getBytes());
        if (m_bTestAsymmetricAlgs) {
            testKeyAgreementWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_112, JCConsts.KeyAgreement_ALG_EC_SVDP_DH, "ALG_EC_FP LENGTH_EC_FP_112 ALG_EC_SVDP_DH", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyAgreementWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_128, JCConsts.KeyAgreement_ALG_EC_SVDP_DH, "ALG_EC_FP LENGTH_EC_FP_128 ALG_EC_SVDP_DH", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyAgreementWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_160, JCConsts.KeyAgreement_ALG_EC_SVDP_DH, "ALG_EC_FP LENGTH_EC_FP_160 ALG_EC_SVDP_DH", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyAgreementWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_192, JCConsts.KeyAgreement_ALG_EC_SVDP_DH, "ALG_EC_FP LENGTH_EC_FP_192 ALG_EC_SVDP_DH", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyAgreementWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_224, JCConsts.KeyAgreement_ALG_EC_SVDP_DH, "ALG_EC_FP LENGTH_EC_FP_224 ALG_EC_SVDP_DH", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyAgreementWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_256, JCConsts.KeyAgreement_ALG_EC_SVDP_DH, "ALG_EC_FP LENGTH_EC_FP_256 ALG_EC_SVDP_DH", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyAgreementWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_320, JCConsts.KeyAgreement_ALG_EC_SVDP_DH, "ALG_EC_FP LENGTH_EC_FP_320 ALG_EC_SVDP_DH", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyAgreementWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_384, JCConsts.KeyAgreement_ALG_EC_SVDP_DH, "ALG_EC_FP LENGTH_EC_FP_384 ALG_EC_SVDP_DH", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyAgreementWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_512, JCConsts.KeyAgreement_ALG_EC_SVDP_DH, "ALG_EC_FP LENGTH_EC_FP_512 ALG_EC_SVDP_DH", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyAgreementWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_521, JCConsts.KeyAgreement_ALG_EC_SVDP_DH, "ALG_EC_FP LENGTH_EC_FP_521 ALG_EC_SVDP_DH", numRepeatWholeOperation, numRepeatWholeMeasurement);

            testKeyAgreementWithKeyClass(JCConsts.KeyPair_ALG_EC_F2M, JCConsts.KeyBuilder_TYPE_EC_F2M_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_F2M_113, JCConsts.KeyAgreement_ALG_EC_SVDP_DH, "ALG_EC_F2M LENGTH_EC_F2M_113 ALG_EC_SVDP_DH", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyAgreementWithKeyClass(JCConsts.KeyPair_ALG_EC_F2M, JCConsts.KeyBuilder_TYPE_EC_F2M_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_F2M_131, JCConsts.KeyAgreement_ALG_EC_SVDP_DH, "ALG_EC_F2M LENGTH_EC_F2M_131 ALG_EC_SVDP_DH", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyAgreementWithKeyClass(JCConsts.KeyPair_ALG_EC_F2M, JCConsts.KeyBuilder_TYPE_EC_F2M_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_F2M_163, JCConsts.KeyAgreement_ALG_EC_SVDP_DH, "ALG_EC_F2M LENGTH_EC_F2M_163 ALG_EC_SVDP_DH", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyAgreementWithKeyClass(JCConsts.KeyPair_ALG_EC_F2M, JCConsts.KeyBuilder_TYPE_EC_F2M_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_F2M_193, JCConsts.KeyAgreement_ALG_EC_SVDP_DH, "ALG_EC_F2M LENGTH_EC_F2M_193 ALG_EC_SVDP_DH", numRepeatWholeOperation, numRepeatWholeMeasurement);
        } else {
            String message = "\n# Measurements excluded for asymmetric algorithms\n";
            m_perfResultsFile.write(message.getBytes());
        }
        tableName = "\n\nKEYAGREEMENT - END\n";
        m_perfResultsFile.write(tableName.getBytes());
    }
    
    public void testAESKey(byte keyType, short keyLength, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        TestSettings testSet = null;
        testSet = this.prepareTestSettings(Consts.CLASS_KEYBUILDER, Consts.UNUSED, keyType, keyLength, JCConsts.AESKey_setKey, 
                Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);      
        
        if (!m_bTestVariableData) {
            testSet.algorithmMethod = JCConsts.AESKey_setKey;
            double setKeyTime = this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setKey()");
            testSet.algorithmMethod = JCConsts.AESKey_getKey;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getKey()");
            // Note: clear internally executes also setKey - substract setKey time from result
            testSet.algorithmMethod = JCConsts.AESKey_clearKey;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " clearKey()", setKeyTime);
        }
        else {
            String message = "No variable data test for " + info + "\n";
            m_perfResultsFile.write(message.getBytes());
            m_SystemOutLogger.print(message);
        }
    }
    
    public void testAllAESKeys(int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception {
        testAllAESKeys((short) numRepeatWholeOperation, (short) numRepeatWholeMeasurement);
    }
    public void testAllAESKeys(short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        String tableName = "\n\nAESKey\n";
        m_perfResultsFile.write(tableName.getBytes());
        if (m_bTestSymmetricAlgs) {
            testAESKey(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_128,"TYPE_AES LENGTH_AES_128", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testAESKey(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_192,"TYPE_AES LENGTH_AES_192", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testAESKey(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,"TYPE_AES LENGTH_AES_256", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testAESKey(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_512,"TYPE_AES LENGTH_AES_512", numRepeatWholeOperation, numRepeatWholeMeasurement);
        }
        else {
            String message = "\n# Measurements excluded for symmetric algorithms\n";
            m_perfResultsFile.write(message.getBytes());
        }
        
        tableName = "\n\nAESKey - END\n";
        m_perfResultsFile.write(tableName.getBytes());
    }
    
    public void testDESKey (byte keyType, short keyLength, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws Exception{
        TestSettings testSet = null;
        testSet = this.prepareTestSettings(Consts.CLASS_KEYBUILDER, Consts.UNUSED, keyType, keyLength, JCConsts.DESKey_setKey,
                Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);
        if (!m_bTestVariableData) {
            testSet.algorithmMethod = JCConsts.DESKey_setKey;
            double setKeyTime = this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setKey()");
            testSet.algorithmMethod = JCConsts.DESKey_getKey;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getKey()");
            testSet.algorithmMethod = JCConsts.DESKey_clearKey;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " clearKey()");
        }
        else {
            String message = "No variable data test for " + info + "\n";
            m_perfResultsFile.write(message.getBytes());
            m_SystemOutLogger.print(message);
        }
    }
    
    public void testAllDESKeys (int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws Exception{
        testAllDESKeys((short)numRepeatWholeOperation, (short)numRepeatWholeMeasurement);
    }
    
    public void testAllDESKeys (short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception{
        String tableName = "\n\nDESKey";
        m_perfResultsFile.write(tableName.getBytes());
        
        if (m_bTestSymmetricAlgs) {
            testDESKey(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES, "TYPE_DES LENGTH_DES_64", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testDESKey(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_2KEY, "TYPE_DES LENGTH_DES_128", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testDESKey(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_3KEY, "TYPE_DES LENGTH_DES_192", numRepeatWholeOperation, numRepeatWholeMeasurement);
        }
        else {
            String message = "\n# Measurements excluded for symmetric algorithms\n";
            m_perfResultsFile.write(message.getBytes());
        }
        
        tableName = "\n\nDESKey - END\n";
        m_perfResultsFile.write(tableName.getBytes());
    }
    
    public void testKoreanSEEDKey (byte keyType, short keyLength, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws Exception{
        TestSettings testSet = null;
        testSet = this.prepareTestSettings(Consts.CLASS_KEYBUILDER, Consts.UNUSED, keyType, keyLength, JCConsts.KoreanSEEDKey_setKey,
                Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);
        
        if (!m_bTestVariableData) {
            testSet.algorithmMethod = JCConsts.KoreanSEEDKey_setKey;
            double setKeyTime = this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setKey()");
            testSet.algorithmMethod = JCConsts.KoreanSEEDKey_getKey;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getKey()");
            testSet.algorithmMethod = JCConsts.KoreanSEEDKey_clearKey;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " clearKey()");
        }
        else {
            String message = "No variable data test for " + info + "\n";
            m_perfResultsFile.write(message.getBytes());
            m_SystemOutLogger.print(message);
        }
    }
    public void testAllKoreanSEEDKeys (int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception{
        testAllKoreanSEEDKeys((short)numRepeatWholeOperation, (short)numRepeatWholeMeasurement);
    }
    public void testAllKoreanSEEDKeys (short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception{
        String tableName = "\n\nKoreanSEEDKey";
        m_perfResultsFile.write(tableName.getBytes());
        
        if (m_bTestSymmetricAlgs) {
            testKoreanSEEDKey(JCConsts.KeyBuilder_TYPE_KOREAN_SEED, JCConsts.KeyBuilder_LENGTH_KOREAN_SEED_128, "TYPE_KOREAN_SEED LENGTH_KOREAN_SEED_128", numRepeatWholeOperation, numRepeatWholeMeasurement);
        }
        else {
            String message = "\n# Measurements excluded for symmetric algorithms\n";
            m_perfResultsFile.write(message.getBytes());
        }
        
        tableName = "\n\nKoreanSEEDKey - END\n";
        m_perfResultsFile.write(tableName.getBytes());
    }
    
    public void testDSAPrivateKey (byte keyType, short keyLength, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws Exception{
        TestSettings testSet = null;
        testSet = this.prepareTestSettings(Consts.CLASS_KEYBUILDER, Consts.UNUSED, keyType, keyLength, JCConsts.DSAPrivateKey_getX,
                Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);
        
        if (!m_bTestVariableData) {
            testSet.algorithmMethod = JCConsts.DSAPrivateKey_getX;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setX()");
            testSet.algorithmMethod = JCConsts.DSAPrivateKey_setX;
            double setXTime = this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getX()");
            testSet.algorithmMethod = JCConsts.DSAPrivateKey_clearKey;
            TestSettings SINGLEOP_testSet = testSet.duplicate();
            SINGLEOP_testSet.numRepeatWholeOperation = 1; SINGLEOP_testSet.numRepeatWholeMeasurement = 1;// BUGBUG: calling clearKey twice for asym. algs will fail
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, SINGLEOP_testSet, info + " clearKey()");
        }
        else {
            String message = "No variable data test for " + info + "\n";
            m_perfResultsFile.write(message.getBytes());
            m_SystemOutLogger.print(message);
        }
    }
    public void testAllDSAPrivateKeys (int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception{
        testAllDSAPrivateKeys((short)numRepeatWholeOperation, (short)numRepeatWholeMeasurement);
    }
    public void testAllDSAPrivateKeys (short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception{
        String tableName = "\n\nDSAPrivateKey";
        m_perfResultsFile.write(tableName.getBytes());
        if (m_bTestAsymmetricAlgs) {
            testDSAPrivateKey(JCConsts.KeyBuilder_TYPE_DSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_DSA_512, "TYPE_DSA_PRIVATE LENGTH_DSA_512", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testDSAPrivateKey(JCConsts.KeyBuilder_TYPE_DSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_DSA_768, "TYPE_DSA_PRIVATE LENGTH_DSA_768", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testDSAPrivateKey(JCConsts.KeyBuilder_TYPE_DSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_DSA_1024, "TYPE_DSA_PRIVATE LENGTH_DSA_1024", numRepeatWholeOperation, numRepeatWholeMeasurement);
        }
        else {
            String message = "\n# Measurements excluded for asymmetric algorithms\n";
            m_perfResultsFile.write(message.getBytes());
        }           
        tableName = "\n\nDSAPrivateKey - END\n";
        m_perfResultsFile.write(tableName.getBytes());
    }
    
    public void testDSAPublicKey (byte keyType, short keyLength, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws Exception{
        TestSettings testSet = null;
        testSet = this.prepareTestSettings(Consts.CLASS_KEYBUILDER, Consts.UNUSED, keyType, keyLength, JCConsts.DSAPublicKey_getY,
                Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);
        
        if (!m_bTestVariableData) {
            testSet.algorithmMethod = JCConsts.DSAPublicKey_getY;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setY()");
            testSet.algorithmMethod = JCConsts.DSAPublicKey_setY;
            double setXTime = this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getY()");
            testSet.algorithmMethod = JCConsts.DSAPublicKey_clearKey;
            TestSettings SINGLEOP_testSet = testSet.duplicate(); SINGLEOP_testSet.numRepeatWholeOperation = 1; // BUGBUG: calling clearKey twice for asym. algs will fail
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, SINGLEOP_testSet, info + " clearKey()");
        }
        else {
            String message = "No variable data test for " + info + "\n";
            m_perfResultsFile.write(message.getBytes());
            m_SystemOutLogger.print(message);
        }
    }
    public void testAllDSAPublicKeys (int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception{
        testAllDSAPublicKeys((short)numRepeatWholeOperation, (short)numRepeatWholeMeasurement);
    }
    public void testAllDSAPublicKeys (short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception{
        String tableName = "\n\nDSAPublicKey";
        m_perfResultsFile.write(tableName.getBytes());
        if (m_bTestAsymmetricAlgs) {
            testDSAPublicKey(JCConsts.KeyBuilder_TYPE_DSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_DSA_512, "TYPE_DSA_PUBLIC LENGTH_DSA_512", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testDSAPublicKey(JCConsts.KeyBuilder_TYPE_DSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_DSA_768, "TYPE_DSA_PUBLIC LENGTH_DSA_768", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testDSAPublicKey(JCConsts.KeyBuilder_TYPE_DSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_DSA_1024, "TYPE_DSA_PUBLIC LENGTH_DSA_1024", numRepeatWholeOperation, numRepeatWholeMeasurement);
        }
        else {
            String message = "\n# Measurements excluded for asymmetric algorithms\n";
            m_perfResultsFile.write(message.getBytes());
        }           
        tableName = "\n\nDSAPublicKey - END\n";
        m_perfResultsFile.write(tableName.getBytes());
    }
    
    public void testECPublicKey (byte keyClass, byte keyType, short keyLength, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws Exception{
        TestSettings testSet = null;
        testSet = this.prepareTestSettings(Consts.CLASS_KEYBUILDER, Consts.UNUSED, keyType, keyLength, JCConsts.ECPublicKey_setW,
                Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);
        testSet.keyClass = keyClass;
        
        if (!m_bTestVariableData) {
            testSet.algorithmMethod = JCConsts.ECPublicKey_setW;
            double setWTime = this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setW()");
            testSet.algorithmMethod = JCConsts.ECPublicKey_getW;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getW()");
            testSet.algorithmMethod = JCConsts.ECPublicKey_clearKey;
            TestSettings SINGLEOP_testSet = testSet.duplicate();
            SINGLEOP_testSet.numRepeatWholeOperation = 1; SINGLEOP_testSet.numRepeatWholeMeasurement = 1;// BUGBUG: calling clearKey twice for asym. algs will fail
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, SINGLEOP_testSet, info + " clearKey()");
        }
        else {
            String message = "No variable data test for " + info + "\n";
            m_perfResultsFile.write(message.getBytes());
            m_SystemOutLogger.print(message);
        }
    }
    public void testAllECF2MPublicKeys (int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception{
        testAllECF2MPublicKeys((short)numRepeatWholeOperation, (short)numRepeatWholeMeasurement);
    }
    public void testAllECF2MPublicKeys (short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception{
        String tableName = "\n\nECF2MPublicKey";
        m_perfResultsFile.write(tableName.getBytes());
        if (m_bTestAsymmetricAlgs) {
            testECPublicKey(JCConsts.KeyPair_ALG_EC_F2M, JCConsts.KeyBuilder_TYPE_EC_F2M_PUBLIC, JCConsts.KeyBuilder_LENGTH_EC_F2M_113, "TYPE_EC_F2M_PUBLIC LENGTH_EC_F2M_113", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECPublicKey(JCConsts.KeyPair_ALG_EC_F2M, JCConsts.KeyBuilder_TYPE_EC_F2M_PUBLIC, JCConsts.KeyBuilder_LENGTH_EC_F2M_131, "TYPE_EC_F2M_PUBLIC LENGTH_EC_F2M_131", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECPublicKey(JCConsts.KeyPair_ALG_EC_F2M, JCConsts.KeyBuilder_TYPE_EC_F2M_PUBLIC, JCConsts.KeyBuilder_LENGTH_EC_F2M_163, "TYPE_EC_F2M_PUBLIC LENGTH_EC_F2M_163", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECPublicKey(JCConsts.KeyPair_ALG_EC_F2M, JCConsts.KeyBuilder_TYPE_EC_F2M_PUBLIC, JCConsts.KeyBuilder_LENGTH_EC_F2M_193, "TYPE_EC_F2M_PUBLIC LENGTH_EC_F2M_193", numRepeatWholeOperation, numRepeatWholeMeasurement);
        }
        else {
            String message = "\n# Measurements excluded for asymmetric algorithms\n";
            m_perfResultsFile.write(message.getBytes());
        }           
        tableName = "\n\nECF2MPublicKey - END\n";
        m_perfResultsFile.write(tableName.getBytes());
    }
    
    public void testECPrivateKey (byte keyClass, byte keyType, short keyLength, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws Exception{
        TestSettings testSet = null;
        testSet = this.prepareTestSettings(Consts.CLASS_KEYBUILDER, Consts.UNUSED, keyType, keyLength, JCConsts.ECPrivateKey_setS,
                Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);
        testSet.keyClass = keyClass;
        
        if (!m_bTestVariableData) {
            testSet.algorithmMethod = JCConsts.ECPrivateKey_setS;
            double setSTime = this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setS()");
            testSet.algorithmMethod = JCConsts.ECPrivateKey_getS;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getS()");
            testSet.algorithmMethod = JCConsts.ECPrivateKey_clearKey;
            TestSettings SINGLEOP_testSet = testSet.duplicate();
            SINGLEOP_testSet.numRepeatWholeOperation = 1; SINGLEOP_testSet.numRepeatWholeMeasurement = 1;// BUGBUG: calling clearKey twice for asym. algs will fail
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, SINGLEOP_testSet, info + " clearKey()");
        }
        else {
            String message = "No variable data test for " + info + "\n";
            m_perfResultsFile.write(message.getBytes());
            m_SystemOutLogger.print(message);
        }
    }

    public void testAllECF2MPrivateKeys (int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception{
        testAllECF2MPrivateKeys((short)numRepeatWholeOperation, (short)numRepeatWholeMeasurement);
    }
    public void testAllECF2MPrivateKeys (short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception{
        String tableName = "\n\nECF2MPrivateKey";
        m_perfResultsFile.write(tableName.getBytes());
        if (m_bTestAsymmetricAlgs) {
            testECPublicKey(JCConsts.KeyPair_ALG_EC_F2M, JCConsts.KeyBuilder_TYPE_EC_F2M_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_F2M_113, "TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_113", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECPublicKey(JCConsts.KeyPair_ALG_EC_F2M, JCConsts.KeyBuilder_TYPE_EC_F2M_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_F2M_131, "TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_131", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECPublicKey(JCConsts.KeyPair_ALG_EC_F2M, JCConsts.KeyBuilder_TYPE_EC_F2M_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_F2M_163, "TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_163", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECPublicKey(JCConsts.KeyPair_ALG_EC_F2M, JCConsts.KeyBuilder_TYPE_EC_F2M_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_F2M_193, "TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_193", numRepeatWholeOperation, numRepeatWholeMeasurement);
        }
        else {
            String message = "\n# Measurements excluded for asymmetric algorithms\n";
            m_perfResultsFile.write(message.getBytes());
        }           
        tableName = "\n\nECF2MPrivateKey - END\n";
        m_perfResultsFile.write(tableName.getBytes());
    }
    
    public void testAllECFPPublicKeys (int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception{
        testAllECFPPublicKeys((short)numRepeatWholeOperation, (short)numRepeatWholeMeasurement);
    }
    public void testAllECFPPublicKeys (short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception{
        String tableName = "\n\nECFPPublicKey";
        m_perfResultsFile.write(tableName.getBytes());
        if (m_bTestAsymmetricAlgs) {
            testECPublicKey(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PUBLIC, JCConsts.KeyBuilder_LENGTH_EC_FP_112, "TYPE_EC_FP_PUBLIC LENGTH_EC_FP_112", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECPublicKey(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PUBLIC, JCConsts.KeyBuilder_LENGTH_EC_FP_128, "TYPE_EC_FP_PUBLIC LENGTH_EC_FP_128", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECPublicKey(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PUBLIC, JCConsts.KeyBuilder_LENGTH_EC_FP_160, "TYPE_EC_FP_PUBLIC LENGTH_EC_FP_160", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECPublicKey(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PUBLIC, JCConsts.KeyBuilder_LENGTH_EC_FP_192, "TYPE_EC_FP_PUBLIC LENGTH_EC_FP_192", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECPublicKey(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PUBLIC, JCConsts.KeyBuilder_LENGTH_EC_FP_224, "TYPE_EC_FP_PUBLIC LENGTH_EC_FP_224", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECPublicKey(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PUBLIC, JCConsts.KeyBuilder_LENGTH_EC_FP_256, "TYPE_EC_FP_PUBLIC LENGTH_EC_FP_256", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECPublicKey(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PUBLIC, JCConsts.KeyBuilder_LENGTH_EC_FP_320, "TYPE_EC_FP_PUBLIC LENGTH_EC_FP_320", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECPublicKey(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PUBLIC, JCConsts.KeyBuilder_LENGTH_EC_FP_384, "TYPE_EC_FP_PUBLIC LENGTH_EC_FP_384", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECPublicKey(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PUBLIC, JCConsts.KeyBuilder_LENGTH_EC_FP_512, "TYPE_EC_FP_PUBLIC LENGTH_EC_FP_512", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECPublicKey(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PUBLIC, JCConsts.KeyBuilder_LENGTH_EC_FP_521, "TYPE_EC_FP_PUBLIC LENGTH_EC_FP_521", numRepeatWholeOperation, numRepeatWholeMeasurement);
        }
        else {
            String message = "\n# Measurements excluded for asymmetric algorithms\n";
            m_perfResultsFile.write(message.getBytes());
        }           
        tableName = "\n\nECFPPublicKey - END\n";
        m_perfResultsFile.write(tableName.getBytes());
    }
    
    public void testAllECFPPrivateKeys (int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception{
        testAllECFPPrivateKeys((short)numRepeatWholeOperation, (short)numRepeatWholeMeasurement);
    }
    public void testAllECFPPrivateKeys (short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception{
        String tableName = "\n\nECFPPrivateKey";
        m_perfResultsFile.write(tableName.getBytes());
        if (m_bTestAsymmetricAlgs) {
            testECPrivateKey(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_112, "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_112", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECPrivateKey(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_128, "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_128", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECPrivateKey(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_160, "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_160", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECPrivateKey(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_192, "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_192", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECPrivateKey(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_224, "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_224", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECPrivateKey(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_256, "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_256", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECPrivateKey(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_320, "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_320", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECPrivateKey(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_384, "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_384", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECPrivateKey(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_512, "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_512", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECPrivateKey(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_521, "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_521", numRepeatWholeOperation, numRepeatWholeMeasurement);
        }
        else {
            String message = "\n# Measurements excluded for asymmetric algorithms\n";
            m_perfResultsFile.write(message.getBytes());
        }           
        tableName = "\n\nECFPPrivateKey - END\n";
        m_perfResultsFile.write(tableName.getBytes());
    }
    
    public void testHMACKey (byte keyType, short keyLength, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws Exception{
        TestSettings testSet = null;
        testSet = this.prepareTestSettings(Consts.CLASS_KEYBUILDER, Consts.UNUSED, keyType, keyLength, JCConsts.HMACKey_getKey,
                Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);
        
        if (!m_bTestVariableData) {
            testSet.algorithmMethod = JCConsts.HMACKey_setKey;
            double setKeyTime = this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setKey()");
            testSet.algorithmMethod = JCConsts.HMACKey_getKey;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getKey()");
            testSet.algorithmMethod = JCConsts.HMACKey_clearKey;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " clearKey()", setKeyTime);
        }
        else {
            String message = "No variable data test for " + info + "\n";
            m_perfResultsFile.write(message.getBytes());
            m_SystemOutLogger.print(message);
        }
    }
    public void testAllHMACKeys (int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception{
        testAllHMACKeys((short)numRepeatWholeOperation, (short)numRepeatWholeMeasurement);
    }
    public void testAllHMACKeys (short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception{
        String tableName = "\n\nHMACKey";
        m_perfResultsFile.write(tableName.getBytes());

        if (m_bTestSymmetricAlgs) {
            testHMACKey(JCConsts.KeyBuilder_TYPE_HMAC, JCConsts.KeyBuilder_LENGTH_HMAC_SHA_1_BLOCK_64, "TYPE_HMAC_SHA-1 LENGTH_HMAC_64", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testHMACKey(JCConsts.KeyBuilder_TYPE_HMAC, JCConsts.KeyBuilder_LENGTH_HMAC_SHA_256_BLOCK_64, "TYPE_HMAC_SHA-256 LENGTH_HMAC_64", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testHMACKey(JCConsts.KeyBuilder_TYPE_HMAC, JCConsts.KeyBuilder_LENGTH_HMAC_SHA_384_BLOCK_128, "TYPE_HMAC_SHA-384 LENGTH_HMAC_128", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testHMACKey(JCConsts.KeyBuilder_TYPE_HMAC, JCConsts.KeyBuilder_LENGTH_HMAC_SHA_512_BLOCK_128, "TYPE_HMAC_SHA-512 LENGTH_HMAC_128", numRepeatWholeOperation, numRepeatWholeMeasurement);
        }
        else {
            String message = "\n# Measurements excluded for symmetric algorithms\n";
            m_perfResultsFile.write(message.getBytes());
        }
        
        tableName = "\n\nHMACKey - END\n";
        m_perfResultsFile.write(tableName.getBytes());
    }
    
    public void testRSAPrivateCrtKey (byte keyType, short keyLength, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws Exception{
        TestSettings testSet = null;
        testSet = this.prepareTestSettings(Consts.CLASS_KEYBUILDER, Consts.UNUSED, keyType, keyLength, JCConsts.RSAPrivateCrtKey_setDP1,
                Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);
        
        if (!m_bTestVariableData) {
            testSet.algorithmMethod = JCConsts.RSAPrivateCrtKey_setDP1;
            double setDP1Time = this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setDP1()");
            testSet.algorithmMethod = JCConsts.RSAPrivateCrtKey_setDQ1;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setDQ1()");
            testSet.algorithmMethod = JCConsts.RSAPrivateCrtKey_setP;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setP()");
            testSet.algorithmMethod = JCConsts.RSAPrivateCrtKey_setPQ;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setPQ()");
            testSet.algorithmMethod = JCConsts.RSAPrivateCrtKey_setQ;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setQ()");

            testSet.algorithmMethod = JCConsts.RSAPrivateCrtKey_getDP1;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getDP1()");
            testSet.algorithmMethod = JCConsts.RSAPrivateCrtKey_getDQ1;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getDQ1()");
            testSet.algorithmMethod = JCConsts.RSAPrivateCrtKey_getP;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getP()");
            testSet.algorithmMethod = JCConsts.RSAPrivateCrtKey_getPQ;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getPQ()");
            testSet.algorithmMethod = JCConsts.RSAPrivateCrtKey_getQ;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getQ()");

            testSet.algorithmMethod = JCConsts.RSAPrivateCrtKey_clearKey;
            TestSettings SINGLEOP_testSet = testSet.duplicate();
            SINGLEOP_testSet.numRepeatWholeOperation = 1; SINGLEOP_testSet.numRepeatWholeMeasurement = 1;// BUGBUG: calling clearKey twice for asym. algs will fail
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, SINGLEOP_testSet, info + " clearKey()");
        }
        else {
            String message = "No variable data test for " + info + "\n";
            m_perfResultsFile.write(message.getBytes());
            m_SystemOutLogger.print(message);
        }
    }
    public void testAllRSAPrivateCrtKeys (int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception{
        testAllRSAPrivateCrtKeys((short)numRepeatWholeOperation, (short)numRepeatWholeMeasurement);
    }
    public void testAllRSAPrivateCrtKeys (short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception{
        String tableName = "\n\nRSAPrivateCRTKey";
        m_perfResultsFile.write(tableName.getBytes());
        if (m_bTestAsymmetricAlgs) {
            testRSAPrivateCrtKey(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512, "TYPE_RSA_PRIVATE_CRT LENGTH_RSA_512", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateCrtKey(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736, "TYPE_RSA_PRIVATE_CRT LENGTH_RSA_736", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateCrtKey(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768, "TYPE_RSA_PRIVATE_CRT LENGTH_RSA_768", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateCrtKey(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896, "TYPE_RSA_PRIVATE_CRT LENGTH_RSA_896", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateCrtKey(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024, "TYPE_RSA_PRIVATE_CRT LENGTH_RSA_1024", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateCrtKey(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280, "TYPE_RSA_PRIVATE_CRT LENGTH_RSA_1280", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateCrtKey(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536, "TYPE_RSA_PRIVATE_CRT LENGTH_RSA_1536", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateCrtKey(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984, "TYPE_RSA_PRIVATE_CRT LENGTH_RSA_1984", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateCrtKey(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048, "TYPE_RSA_PRIVATE_CRT LENGTH_RSA_2048", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateCrtKey(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072, "TYPE_RSA_PRIVATE_CRT LENGTH_RSA_3072", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateCrtKey(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096, "TYPE_RSA_PRIVATE_CRT LENGTH_RSA_4096", numRepeatWholeOperation, numRepeatWholeMeasurement);
            // Ask for free of RSA keys cache
            m_cardManager.resetApplet(Consts.CLA_CARD_ALGTEST, Consts.INS_CARD_RESET, Consts.P1_CARD_RESET_FREE_CACHE);
        }
        else {
            String message = "\n# Measurements excluded for asymmetric algorithms\n";
            m_perfResultsFile.write(message.getBytes());
        }   
        tableName = "\n\nRSAPrivateCRTKey - END\n";
        m_perfResultsFile.write(tableName.getBytes());
    }
    
    public void testRSAPrivateKey (byte keyType, short keyLength, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws Exception{
        TestSettings testSet = null;
        testSet = this.prepareTestSettings(Consts.CLASS_KEYBUILDER, Consts.UNUSED, keyType, keyLength, JCConsts.RSAPrivateKey_setExponent,
                Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);
        
        if (!m_bTestVariableData) {
            testSet.algorithmMethod = JCConsts.RSAPrivateKey_setExponent;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setExponent()");
            testSet.algorithmMethod = JCConsts.RSAPrivateKey_setModulus;
            double setModulusTime = this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setModulus()");

            testSet.algorithmMethod = JCConsts.RSAPrivateKey_getExponent;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getExponent()");
            testSet.algorithmMethod = JCConsts.RSAPrivateKey_getModulus;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getModulus()");

            testSet.algorithmMethod = JCConsts.RSAPrivateKey_clearKey;
            TestSettings SINGLEOP_testSet = testSet.duplicate();
            SINGLEOP_testSet.numRepeatWholeOperation = 1; SINGLEOP_testSet.numRepeatWholeMeasurement = 1;// BUGBUG: calling clearKey twice for asym. algs will fail
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, SINGLEOP_testSet, info + " clearKey()");
        }
        else {
            String message = "No variable data test for " + info + "\n";
            m_perfResultsFile.write(message.getBytes());
            m_SystemOutLogger.print(message);
        }
    }
    public void testAllRSAPrivateKeys (int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception{
        testAllRSAPrivateKeys((short)numRepeatWholeOperation, (short)numRepeatWholeMeasurement);
    }
    public void testAllRSAPrivateKeys (short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception{
        String tableName = "\n\nRSAPrivateKey";
        m_perfResultsFile.write(tableName.getBytes());
        if (m_bTestAsymmetricAlgs) {
            testRSAPrivateKey(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512, "TYPE_RSA_PRIVATE LENGTH_RSA_512", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateKey(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736, "TYPE_RSA_PRIVATE LENGTH_RSA_736", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateKey(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768, "TYPE_RSA_PRIVATE LENGTH_RSA_768", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateKey(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896, "TYPE_RSA_PRIVATE LENGTH_RSA_896", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateKey(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024, "TYPE_RSA_PRIVATE LENGTH_RSA_1024", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateKey(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280, "TYPE_RSA_PRIVATE LENGTH_RSA_1280", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateKey(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536, "TYPE_RSA_PRIVATE LENGTH_RSA_1536", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateKey(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984, "TYPE_RSA_PRIVATE LENGTH_RSA_1984", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateKey(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048, "TYPE_RSA_PRIVATE LENGTH_RSA_2048", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateKey(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072, "TYPE_RSA_PRIVATE LENGTH_RSA_3072", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateKey(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096, "TYPE_RSA_PRIVATE LENGTH_RSA_4096", numRepeatWholeOperation, numRepeatWholeMeasurement);
            // Ask for free of RSA keys cache
            m_cardManager.resetApplet(Consts.CLA_CARD_ALGTEST, Consts.INS_CARD_RESET, Consts.P1_CARD_RESET_FREE_CACHE);
        }
        else {
            String message = "\n# Measurements excluded for asymmetric algorithms\n";
            m_perfResultsFile.write(message.getBytes());
        }   
        tableName = "\n\nRSAPrivateKey - END\n";
        m_perfResultsFile.write(tableName.getBytes());
    }
    
    public void testRSAPublicKey (byte keyType, short keyLength, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws Exception{
        TestSettings testSet = null;
        testSet = this.prepareTestSettings(Consts.CLASS_KEYBUILDER, Consts.UNUSED, keyType, keyLength, JCConsts.RSAPublicKey_setExponent,
                Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);
        
        if (!m_bTestVariableData) {
            testSet.algorithmMethod = JCConsts.RSAPublicKey_setExponent;
            double setExponentTime = this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setExponent()");
            testSet.algorithmMethod = JCConsts.RSAPublicKey_setModulus;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setModulus()");

            testSet.algorithmMethod = JCConsts.RSAPublicKey_getExponent;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getExponent()");
            testSet.algorithmMethod = JCConsts.RSAPublicKey_getModulus;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getModulus()");

            testSet.algorithmMethod = JCConsts.RSAPublicKey_clearKey;
            TestSettings SINGLEOP_testSet = testSet.duplicate();
            SINGLEOP_testSet.numRepeatWholeOperation = 1; SINGLEOP_testSet.numRepeatWholeMeasurement = 1;// BUGBUG: calling clearKey twice for asym. algs will fail
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, SINGLEOP_testSet, info + " clearKey()");
        }
        else {
            String message = "No variable data test for " + info + "\n";
            m_perfResultsFile.write(message.getBytes());
            m_SystemOutLogger.print(message);
        }
    }
    public void testAllRSAPublicKeys (int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception{
        testAllRSAPublicKeys((short)numRepeatWholeOperation, (short)numRepeatWholeMeasurement);
    }
    public void testAllRSAPublicKeys (short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception{
        String tableName = "\n\nRSAPublicKey";
        m_perfResultsFile.write(tableName.getBytes());
        
        if (m_bTestAsymmetricAlgs) {
            testRSAPublicKey(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_512, "TYPE_RSA_PUBLIC LENGTH_RSA_512", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPublicKey(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_736, "TYPE_RSA_PUBLIC LENGTH_RSA_736", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPublicKey(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_768, "TYPE_RSA_PUBLIC LENGTH_RSA_768", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPublicKey(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_896, "TYPE_RSA_PUBLIC LENGTH_RSA_896", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPublicKey(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1024, "TYPE_RSA_PUBLIC LENGTH_RSA_1024", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPublicKey(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1280, "TYPE_RSA_PUBLIC LENGTH_RSA_1280", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPublicKey(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1536, "TYPE_RSA_PUBLIC LENGTH_RSA_1536", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPublicKey(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1984, "TYPE_RSA_PUBLIC LENGTH_RSA_1984", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPublicKey(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_2048, "TYPE_RSA_PUBLIC LENGTH_RSA_2048", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPublicKey(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_3072, "TYPE_RSA_PUBLIC LENGTH_RSA_3072", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPublicKey(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_4096, "TYPE_RSA_PUBLIC LENGTH_RSA_4096", numRepeatWholeOperation, numRepeatWholeMeasurement);
            // Ask for free of RSA keys cache
            m_cardManager.resetApplet(Consts.CLA_CARD_ALGTEST, Consts.INS_CARD_RESET, Consts.P1_CARD_RESET_FREE_CACHE);
        }
        else {
            String message = "\n# Measurements excluded for asymmetric algorithms\n";
            m_perfResultsFile.write(message.getBytes());
        }   
        tableName = "\n\nRSAPublicKey - END\n";
        m_perfResultsFile.write(tableName.getBytes());
    }
    
    public void testAllKeys(int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception {
        testAllKeys((short) numRepeatWholeOperation, (short) numRepeatWholeMeasurement);
    }
    public void testAllKeys(short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        testAllAESKeys(numRepeatWholeOperation, numRepeatWholeMeasurement);
        testAllDESKeys(numRepeatWholeOperation, numRepeatWholeMeasurement);
        testAllKoreanSEEDKeys(numRepeatWholeOperation, numRepeatWholeMeasurement);
        testAllDSAPrivateKeys(numRepeatWholeOperation, numRepeatWholeMeasurement);
        testAllDSAPublicKeys(numRepeatWholeOperation, numRepeatWholeMeasurement);
        testAllECF2MPublicKeys(numRepeatWholeOperation, numRepeatWholeMeasurement);
        testAllECF2MPrivateKeys(numRepeatWholeOperation, numRepeatWholeMeasurement);
        testAllECFPPublicKeys(numRepeatWholeOperation, numRepeatWholeMeasurement);
        testAllECFPPrivateKeys(numRepeatWholeOperation, numRepeatWholeMeasurement);
        testAllHMACKeys(numRepeatWholeOperation, numRepeatWholeMeasurement);
        testAllRSAPrivateCrtKeys(numRepeatWholeOperation, numRepeatWholeMeasurement);
        testAllRSAPrivateKeys(numRepeatWholeOperation, numRepeatWholeMeasurement);
        testAllRSAPublicKeys(numRepeatWholeOperation, numRepeatWholeMeasurement);
    }
    
    
    

    public void testCipher_setKeyInitDoFinal(byte key, short keyLength, byte alg, String info, short initMode, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        short testDataLength = Consts.TEST_DATA_LENGTH; // default test length

        TestSettings testSet = this.prepareTestSettings(Consts.CLASS_CIPHER, alg, key, keyLength, JCConsts.Cipher_update, 
                testDataLength, Consts.UNUSED, initMode, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);      

        // Test of speed dependant on data length
        String tableName = "\n\nCIPHER_setKeyInitDoFinal - " + info + " - variable data - BEGIN\n";
        m_perfResultsFile.write(tableName.getBytes());
        testSet.algorithmMethod = JCConsts.Cipher_doFinal;
        for (Integer length : m_testDataLengths) {
            testSet.dataLength1 = length.shortValue();
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CIPHER, Consts.INS_PERF_TEST_CLASS_CIPHER_SETKEYINITDOFINAL, testSet, info + " Cipher_doFinal();" + length + ";");
        }
        tableName = "\n\nCIPHER_setKeyInitDoFinal - " + info + " - variable data - END\n";
        m_perfResultsFile.write(tableName.getBytes());
    }
    

    public void testDataTransferSpeed(String info, int numRepeatWholeMeasurement) throws IOException, Exception {
        short testDataLength = Consts.TEST_DATA_LENGTH; // default test length

        TestSettings testSet = this.prepareTestSettings(Consts.UNUSED, Consts.UNUSED, Consts.UNUSED, Consts.UNUSED, Consts.UNUSED, 
                testDataLength, Consts.UNUSED, Consts.UNUSED, (short) 1, (short) 1, (short) numRepeatWholeMeasurement);      

        String tableName = "\n\nDataTransfer - " + info + " - BEGIN\n";
        m_perfResultsFile.write(tableName.getBytes());
        testSet.bPerformBaselineMeasurement = Consts.FALSE; // disable measurement of baseline overhead (we like to measure also input data time etc.)

        testSet.inData = new byte[152];
        this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_SWALG_HOTP, Consts.INS_PERF_TEST_SWALG_HOTP, testSet, info + " HOTP_verification() first call");

        tableName = "\n\nDataTransfer - " + info + " - END\n";
        m_perfResultsFile.write(tableName.getBytes());
    }    
    
    public void testSWAlg_HOTP(String info, int numRepeatWholeMeasurement) throws IOException, Exception {
        short testDataLength = Consts.TEST_DATA_LENGTH; // default test length

        TestSettings testSet = this.prepareTestSettings(Consts.UNUSED, Consts.UNUSED, Consts.UNUSED, JCConsts.KeyBuilder_LENGTH_AES_128, Consts.UNUSED, 
                testDataLength, Consts.UNUSED, Consts.UNUSED, (short) 1, (short) 1, (short) numRepeatWholeMeasurement);      

        String tableName = "\n\nHOTP_verification - " + info + " - BEGIN\n";
        m_perfResultsFile.write(tableName.getBytes());
        testSet.bPerformBaselineMeasurement = Consts.FALSE; // disable measurement of baseline overhead (we like to measure also input data time etc.)
        testSet.inData = new byte[152];
        
        // Measure parts of operation (first call)
        testSet.P1 = (byte) 0x00;
        testSet.P2 = (byte) 0x00;
        this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_SWALG_HOTP, Consts.INS_PERF_TEST_SWALG_HOTP, testSet, info + " HOTP_verification() first call");

        // Measure full operation excluding preparation of used contexts (second call)
        testSet.P1 = (byte) 0x00;
        testSet.P2 = (byte) 0x01;
        this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_SWALG_HOTP, Consts.INS_PERF_TEST_SWALG_HOTP, testSet, info + " HOTP_verification() second call");

        // Try to measure time required to finish particular part of operation
        for (byte p1 = 0x20; p1 < 0x2a; p1++) { 
            testSet.P1 = p1;
            testSet.P2 = (byte) 0x00;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_SWALG_HOTP, Consts.INS_PERF_TEST_SWALG_HOTP, testSet, info + " HOTP_verification_part(0x" + CardMngr.byteToHex(p1) + ")");
        }
        // Measure parts of operation excluding preparation of used contexts (second call)
        for (byte p1 = 0x20; p1 < 0x2a; p1++) { 
            testSet.P1 = p1;
            testSet.P2 = (byte) 0x01;
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_SWALG_HOTP, Consts.INS_PERF_TEST_SWALG_HOTP, testSet, info + " HOTP_verification_part(0x" + CardMngr.byteToHex(p1) + ")");
        }
        tableName = "\n\nHOTP_verification - " + info + " - END\n";
        m_perfResultsFile.write(tableName.getBytes());
    }    
    

    public void testSWAlgs(String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        TestSettings testSet = this.prepareTestSettings(Consts.CLASS_RANDOMDATA, Consts.UNUSED, Consts.UNUSED, Consts.UNUSED, JCConsts.RandomData_generateData, 
                Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);      

        if (!m_bTestVariableData) {
            // Ordinary test of all available methods
            testSet.algorithmMethod = JCConsts.SWAlgs_AES;
            testSet.dataLength1 = 16; // sw aes is tested only on 16 bytes
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_SWALGS, Consts.INS_PERF_TEST_SWALGS, testSet, info + " SWAlgs_AES()");
            
            testSet.algorithmMethod = JCConsts.SWAlgs_xor;
            testSet.dataLength1 = 16; // xor is tested only on 16 bytes
            this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_SWALGS, Consts.INS_PERF_TEST_SWALGS, testSet, info + " SWAlgs_xor()");
        }
        else {
            // Test of speed dependant on data length
            String tableName = "\n\nSWALGS - "  + info + " - variable data - BEGIN\n";
            m_perfResultsFile.write(tableName.getBytes());
/* at the moment, no swalgs works on variable data - add later
            testSet.algorithmMethod = JCConsts.SWAlgs_xor;
            for (Integer length : m_testDataLengths) {
                testSet.dataLength1 = length.shortValue();
                this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_SWALGS, Consts.INS_PERF_TEST_SWALGS, testSet, info + " SWAlgs_AES();" + length + ";");
            }
*/
            tableName = "\n\nSWALGS - "  + info + " - variable data - END\n";
            m_perfResultsFile.write(tableName.getBytes());
        }
    }    
    public void testAllSWAlgs(int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception {
        testAllSWAlgs((short) numRepeatWholeOperation, (short) numRepeatWholeMeasurement);
    }
    public void testAllSWAlgs(short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        String tableName = "\n\nSWALGS\n";
        m_perfResultsFile.write(tableName.getBytes());
        testSWAlgs("SWALGS", numRepeatWholeOperation, numRepeatWholeMeasurement);
        tableName = "\n\nSWALGS - END\n";
        m_perfResultsFile.write(tableName.getBytes());
    }       
    
/*  TODO: add dedicated test for obtaining default curve parameters  
    public void getDefaultECParameters(byte keyType, short keyLength, String info) throws IOException, Exception {
        TestSettings testSet = null;
        testSet = this.prepareTestSettings(Consts.CLASS_KEYBUILDER, Consts.UNUSED, keyType, keyLength, JCConsts.ECPrivateKey_getS,
                Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, (short) 0, (short) 1, (short) 1);
        
        testSet.algorithmMethod = JCConsts.ECPrivateKey_getS;
        this.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PREPARE_TEST_DEFAULT_PARAMS, testSet, info + " getS()");
    }
    public void getDefaultECParametersAllECF2MKeys() throws IOException, Exception {
        String tableName = "\n\nECF2M Default curve";
        m_perfResultsFile.write(tableName.getBytes());
        getDefaultECParameters(JCConsts.KeyBuilder_TYPE_EC_F2M_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_F2M_113, "TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_113");
        getDefaultECParameters(JCConsts.KeyBuilder_TYPE_EC_F2M_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_F2M_131, "TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_131");
        getDefaultECParameters(JCConsts.KeyBuilder_TYPE_EC_F2M_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_F2M_163, "TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_163");
        getDefaultECParameters(JCConsts.KeyBuilder_TYPE_EC_F2M_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_F2M_193, "TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_193");

        tableName = "\n\nECF2M Default curve - END\n";
        m_perfResultsFile.write(tableName.getBytes());
    }
    public void getDefaultECParametersAllECFPKeys() throws IOException, Exception {
        String tableName = "\n\nECFP Default curve";
        m_perfResultsFile.write(tableName.getBytes());
        getDefaultECParameters(JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_128, "TYPE_EC_F2M_PRIVATE LENGTH_EC_FP_128");
        getDefaultECParameters(JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_160, "TYPE_EC_F2M_PRIVATE LENGTH_EC_FP_160");
        getDefaultECParameters(JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_192, "TYPE_EC_F2M_PRIVATE LENGTH_EC_FP_192");
        getDefaultECParameters(JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_224, "TYPE_EC_F2M_PRIVATE LENGTH_EC_FP_224");
        getDefaultECParameters(JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_256, "TYPE_EC_F2M_PRIVATE LENGTH_EC_FP_256");
        getDefaultECParameters(JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_384, "TYPE_EC_F2M_PRIVATE LENGTH_EC_FP_384");
        getDefaultECParameters(JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_521, "TYPE_EC_F2M_PRIVATE LENGTH_EC_FP_521");

        tableName = "\n\nECFP Default curve - END\n";
        m_perfResultsFile.write(tableName.getBytes());
    }
*/   
}
