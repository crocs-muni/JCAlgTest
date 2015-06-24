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

import static algtestjclient.AlgTestJClient.testingPerformance;
import java.io.*;
import java.io.FileOutputStream;
import java.util.Scanner;
import javax.smartcardio.ResponseAPDU;
import AlgTest.Consts;
import AlgTest.JCConsts;
import AlgTest.TestSettings;
import static algtestjclient.PerformanceTesting.file;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.smartcardio.CardTerminal;
/**
 *
 * @author lukas.srom
 */
public class PerformanceTesting {
    
    // Argument constants for choosing algorithm to test. 
    public static final String TEST_ALL_ALGORITHMS = "ALL_ALGS";
    
    public static final String TEST_EEPROM = "EEPROM";
    public static final String TEST_RAM = "RAM";
    public static final String TEST_EXTENDEDAPDU = "EXTENDEDAPDU";
    public static final String TEST_RSAEXPONENT = "RSAEXPONENT";
    
    public static final int MAX_FAILED_REPEATS = 2;
    public static final int NUM_BASELINE_CALIBRATION_RUNS = 5;
    
    
    public static CardMngr cardManager = new CardMngr();
    static String m_cardATR = "";
    
    public StringBuilder value = new StringBuilder();
    public String message = "";
    
    //public static final byte mask = 0b01111111;
    public static FileOutputStream file;
    
    private static boolean bTestSymmetricAlgs = true;
    private static boolean bTestAsymmetricAlgs = true;
    public static boolean bTestVariableData = false;
    
    public static List<Integer> m_testDataLengths = new ArrayList<>();
    
    
    
    /**
     * Calls methods testing card performance.
     * @param args
     * @param bTestVariableDataLengths
     * @param selectedTerminal
     * @throws IOException
     * @throws Exception
     */        
    public void testPerformance(String[] args, boolean bTestVariableDataLengths, CardTerminal selectedTerminal) throws IOException, Exception{
        /* BUGBUG: we need to figure out how to support JCardSim in nice way (copy of class files, directory structure...)
        Class testClassPerformance = AlgTestPerformance.class;
        */
        Class testClassPerformance = null;
        Scanner sc = new Scanner(System.in);
        
        bTestVariableData = bTestVariableDataLengths;
                
        StringBuilder value = new StringBuilder();
        String message = "";
        
        //testCipher_dataDependency(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_128,JCConsts.Cipher_ALG_AES_BLOCK_128_CBC_NOPAD,"TYPE_AES LENGTH_AES_128 ALG_AES_BLOCK_128_CBC_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, Consts.NUM_REPEAT_WHOLE_OPERATION, Consts.NUM_REPEAT_WHOLE_MEASUREMENT);
        
/*        
        if(args.length > 1){    // in case there are arguments present
            if(Arrays.asList(args).contains(TEST_ALL_ALGORITHMS)){testAllAtOnce(file);}
            else if (Arrays.asList(args).contains(TEST_RSAEXPONENT)){
                value.setLength(0);            
                if (testingPerformance.TestVariableRSAPublicExponentSupport(value, file, (byte) 0) == CardMngr.STAT_OK) {}
                else { 
                    message = "\nERROR: Test variable public exponent support fail\n"; 
                    System.out.println(message); file.write(message.getBytes());
                }
                file.flush();
            }
            else if (Arrays.asList(args).contains(TEST_RAM)){
                value.setLength(0);
                if (testingPerformance.TestAvailableRAMMemory(value, file, (byte) 0) == CardMngr.STAT_OK) {}
                else { 
                    message = "\nERROR: Get available RAM memory fail\n"; 
                    System.out.println(message); file.write(message.getBytes());
                }
                file.flush();
            }
            else if (Arrays.asList(args).contains(TEST_EEPROM)){
                value.setLength(0);
                if (testingPerformance.TestAvailableEEPROMMemory(value, file, (byte) 0) == CardMngr.STAT_OK) {}
                else { 
                    message = "\nERROR: Get available EEPROM memory fail\n"; 
                    System.out.println(message); file.write(message.getBytes());
                }
                file.flush();
            }
            else{
                System.err.println("Incorect parameter!");
                cardManager.PrintHelp();
            }
        }
        else{   
            System.out.println("\n\n#########################");
            System.out.println("\n\nQ: Do you like to test support for variable RSA public exponent?");
            System.out.println("Type \"y\" for yes, \"n\" for no: ");	
            String rsa_answ = sc.nextLine();

            if (rsa_answ.equals("y")) {
                // Variable public exponent
                value.setLength(0);

                if (testingPerformance.TestVariableRSAPublicExponentSupport(value, file, (byte) 0) == CardMngr.STAT_OK) {}
                else { 
                    message = "\nERROR: Test variable public exponent support fail\n"; 
                    System.out.println(message); file.write(message.getBytes());
                }
                file.flush();
            }

            System.out.println("\n\n#########################");
            System.out.println("\n\nQ: Do you like to test RAM memory available for allocation?");
            System.out.println("\n\nSTRONG WARNING: There is possibility that your card become unresponsive after this test. All cards I tested required just to delete AlgTest applet to reclaim allocated memory. But it might be possible that your card will be unusuable after this test.");
            System.out.println("\n\nWARNING: Your card should be free from other applets - otherwise memory already claimed by existing applets will not be included in measurement. Value is approximate +- 100B");
            System.out.println("Type \"y\" for yes, \"n\" for no: ");	
            String ram_answ = sc.nextLine();

            if (ram_answ.equals("y")){

                // Available memory
                value.setLength(0);
                if (testingPerformance.TestAvailableRAMMemory(value, file, (byte) 0) == CardMngr.STAT_OK) {}
                else { 
                    message = "\nERROR: Get available RAM memory fail\n"; 
                    System.out.println(message); file.write(message.getBytes());
                }
                file.flush();
            }

            System.out.println("\n\n#########################");
            System.out.println("\n\nQ: Do you like to test EEPROM memory available for allocation?");
            System.out.println("\n\nSTRONG WARNING: There is possibility that your card become unresponsive after this test. All cards I tested required just to delete AlgTest applet to reclaim allocated memory. But it might be possible that your card will be unusuable after this test.");
            System.out.println("\n\nWARNING: Your card should be free from other applets - otherwise memory already claimed by existing applets will not be included in measurement. Value is approximate +- 5KB");
            System.out.println("Type y for yes, n for no: ");	
            String eeprom_answ = sc.nextLine();

            if (eeprom_answ.equals("y")){
                // Available memory
                value.setLength(0);
                if (testingPerformance.TestAvailableEEPROMMemory(value, file, (byte) 0) == CardMngr.STAT_OK) {}
                else { 
                    message = "\nERROR: Get available EEPROM memory fail\n"; 
                    System.out.println(message); file.write(message.getBytes());
                }
                file.flush();
            }
*/        
            System.out.println("Choose which type of performance tests you like to execute:");
            System.out.append("1 -> Only algorithms WITHOUT asymmetric cryptography (estimated time 1-2 hours)\n2 -> Only algorithms WITH asymmetric crypto (estimated time 4-6 hours)\n3 -> All algorithms (estimated time 5-8 hours)\n");
            
            int answ = sc.nextInt();
            switch (answ){
                case 1:
                    bTestSymmetricAlgs = true;
                    bTestAsymmetricAlgs = false;
                break;
                case 2:
                    bTestSymmetricAlgs = false;
                    bTestAsymmetricAlgs = true;
                break;
                case 3:
                    bTestSymmetricAlgs = true;
                    bTestAsymmetricAlgs = true;
                break;
                default:
                    System.err.println("Incorrect parameter, running all tests!");
                break;
            }
        
            // data lengths to be tested (only for variable data length test)
            m_testDataLengths.add(16);
            m_testDataLengths.add(32);
            m_testDataLengths.add(64);
            m_testDataLengths.add(128);
            m_testDataLengths.add(256);
            m_testDataLengths.add(512);
            
            
            int numRepeatWholeOperation = Consts.NUM_REPEAT_WHOLE_OPERATION;
            if (bTestVariableData) {
                numRepeatWholeOperation = Consts.NUM_REPEAT_WHOLE_OPERATION_VARIABLE_DATA;                
            }
            
            System.out.println("Specify type of your card (e.g., NXP JCOP CJ2A081):");
            String testInfo = sc.next();
            
            testInfo += "_PERFORMACE_";
            
            if (bTestSymmetricAlgs) { testInfo += "SYMMETRIC_"; }
            if (bTestAsymmetricAlgs) { testInfo += "ASYMMETRIC_"; }
            if (bTestVariableData) { testInfo += "DATADEPEND_"; }
            else { testInfo += "DATAFIXED_"; }
            

            // Connect to card
            PerformanceTesting.file = cardManager.establishConnection(testClassPerformance, testInfo, selectedTerminal);
            m_cardATR = cardManager.getATR();
            
            
            testAllMessageDigests(numRepeatWholeOperation, Consts.NUM_REPEAT_WHOLE_MEASUREMENT);
            testAllRandomGenerators(numRepeatWholeOperation, Consts.NUM_REPEAT_WHOLE_MEASUREMENT);
            testAllCiphers(numRepeatWholeOperation, Consts.NUM_REPEAT_WHOLE_MEASUREMENT);
            testAllSignatures(numRepeatWholeOperation, Consts.NUM_REPEAT_WHOLE_MEASUREMENT);
            testAllChecksums(numRepeatWholeOperation, Consts.NUM_REPEAT_WHOLE_MEASUREMENT);     
            testAllKeys(numRepeatWholeOperation, Consts.NUM_REPEAT_WHOLE_MEASUREMENT);     

            
/*        
        
            
            boolean fast = false; 
            String res = "";
            System.out.println("Do you want to test every class? WARNING: It could take quite a long time (type 'y' for yes or 'n' for no)");
            res = sc.nextLine();
            if(res.equals("y"))
            {
                System.out.println("Do you want to past the long key pair test? WARNING: It can take few hours! (type 'l' for long or type 'f' for fast)");
                res = sc.nextLine();
                if(res.equals("f")) fast = true;
                testAllMessageDigests(Consts.NUM_REPEAT_WHOLE_OPERATION, Consts.NUM_REPEAT_WHOLE_MEASUREMENT);
                testAllRandomGenerators(Consts.NUM_REPEAT_WHOLE_OPERATION, Consts.NUM_REPEAT_WHOLE_MEASUREMENT);
                testAllCiphers(Consts.NUM_REPEAT_WHOLE_OPERATION, Consts.NUM_REPEAT_WHOLE_MEASUREMENT);
                testAllSignatures(Consts.NUM_REPEAT_WHOLE_OPERATION, Consts.NUM_REPEAT_WHOLE_MEASUREMENT);
                testAllChecksums(Consts.NUM_REPEAT_WHOLE_OPERATION, Consts.NUM_REPEAT_WHOLE_MEASUREMENT);     
                testAllKeys(Consts.NUM_REPEAT_WHOLE_OPERATION, Consts.NUM_REPEAT_WHOLE_MEASUREMENT);     

                if (fast) {
                    testAllKeyPairs(1, Consts.NUM_REPEAT_WHOLE_MEASUREMENT);    // repeat on-card only once (long operation), but perform 5x                                   
                }
                else {
                    testAllKeyPairs(1, 100);
                }
            }
            else
            {
                System.out.println("Do you want to test class messageDigest? (y/n)");
                res = sc.nextLine();
                if(res.equals("y")) { testAllMessageDigests(Consts.NUM_REPEAT_WHOLE_OPERATION, Consts.NUM_REPEAT_WHOLE_MEASUREMENT); }
                System.out.println("Do you want to test class randomData? (y/n)");
                res = sc.nextLine();
                if(res.equals("y")) { testAllRandomGenerators(Consts.NUM_REPEAT_WHOLE_OPERATION, Consts.NUM_REPEAT_WHOLE_MEASUREMENT); }
                System.out.println("Do you want to test class cipher? (y/n)");
                res = sc.nextLine();
                if(res.equals("y")) { testAllCiphers(Consts.NUM_REPEAT_WHOLE_OPERATION, Consts.NUM_REPEAT_WHOLE_MEASUREMENT); }
                System.out.println("Do you want to test class signature? (y/n)");
                res = sc.nextLine();
                if(res.equals("y")) { testAllSignatures(Consts.NUM_REPEAT_WHOLE_OPERATION, Consts.NUM_REPEAT_WHOLE_MEASUREMENT); }
                System.out.println("Do you want to test class checksum? (y/n)");
                res = sc.nextLine();
                if(res.equals("y")) { testAllChecksums(Consts.NUM_REPEAT_WHOLE_OPERATION, Consts.NUM_REPEAT_WHOLE_MEASUREMENT); }
                System.out.println("Do you want to test class Key? (y/n)");
                res = sc.nextLine();
                if(res.equals("y")) { testAllKeys(Consts.NUM_REPEAT_WHOLE_OPERATION, Consts.NUM_REPEAT_WHOLE_MEASUREMENT); }
                System.out.println("Do you want to test class keyPair? (y/n)");
                res = sc.nextLine();
                if(res.equals("y")) 
                {
                    System.out.println("Do you want to past the long key pair test? WARNING: It can take few hours! (type 'l' for long or type 'f' for fast)");
                    res = sc.nextLine();
                    if(res.equals("f")) {
                        testAllKeyPairs(1, 5);                    
                    }
                    else {
                        testAllKeyPairs(1, 100);
                    }
                }
            }
*/
        }
    
    /**
     * Method that will test all algorithms in PerformanceTesting class
     * @param file FileOutputStream object for output data.
     * @throws Exception
     */
    public void testAllAtOnce (FileOutputStream file) throws Exception{
        /* Variable RSA public exponent support */
        value.setLength(0);            
        if (testingPerformance.TestVariableRSAPublicExponentSupport(value, file, (byte) 0) == CardMngr.STAT_OK) {}
        else { 
            message = "\nERROR: Test variable public exponent support fail\n"; 
            System.out.println(message); file.write(message.getBytes());
        }
        file.flush();
        
        /* Available RAM memory. */
        value.setLength(0);
        if (testingPerformance.TestAvailableRAMMemory(value, file, (byte) 0) == CardMngr.STAT_OK) {}
        else { 
            message = "\nERROR: Get available RAM memory fail\n"; 
            System.out.println(message); file.write(message.getBytes());
        }
        file.flush();
        
        /* Available EEPROM memory. */
        value.setLength(0);
        if (testingPerformance.TestAvailableEEPROMMemory(value, file, (byte) 0) == CardMngr.STAT_OK) {}
        else { 
            message = "\nERROR: Get available EEPROM memory fail\n"; 
            System.out.println(message); file.write(message.getBytes());
        }
        file.flush();
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

        ResponseAPDU resp = cardManager.sendAPDU(apdu);
        if (resp.getSW() != 0x9000) {
            System.out.println("Fail to obtain response for TestAvailableRAMMemory");
        } else {
            // SAVE TIME OF CARD RESPONSE
            elapsedCard += System.currentTimeMillis();
            
            // OK, STORE RESPONSE TO suppAlg ARRAY
            byte temp[] = resp.getData();
                
            String elTimeStr = "";
            // OUTPUT REQUIRED TIME WHEN PARTITIONED CHECk WAS PERFORMED (NOTMULTIPLE ALGORITHMS IN SINGLE RUN)
            elTimeStr = String.format("%1f", (double) elapsedCard / (float) CardMngr.CLOCKS_PER_SEC);

            String message = "";
            message += "\r\n\r\nAvailable RAM memory;"; 
            System.out.println(message);
            pFile.write(message.getBytes());
            pValue.append(message);

            int ramSize = (temp[0] << 8) + (temp[1] & 0xff);
            message = String.format("%1d B;", ramSize); 
            System.out.println(message);
            pFile.write(message.getBytes());
            pValue.append(message);
        }

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

        ResponseAPDU resp = cardManager.sendAPDU(apdu);
        if (resp.getSW() != 0x9000) {
            System.out.println("Fail to obtain response for TestAvailableEEPROMMemory");
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
            System.out.println(message);
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
            System.out.println(message);
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
        System.out.println(message);
        pFile.write(message.getBytes());
        pValue.append(message);
            
        ResponseAPDU resp = cardManager.sendAPDU(apdu);
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
            elTimeStr = String.format("%1f", (double) elapsedCard / (float) CardMngr.CLOCKS_PER_SEC);

            message = String.format("yes;%1s sec;", elTimeStr); 
            System.out.println(message);
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
        System.out.println(message);
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
    
    public static int TestExtendedAPDUSupportSupport(StringBuilder pValue, FileOutputStream pFile, byte algPartP2) throws Exception {
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
        System.out.println(message);
        pFile.write(message.getBytes());
        pValue.append(message);
        
        ResponseAPDU resp = cardManager.sendAPDU(apdu);
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
        System.out.println(message);
        pFile.write(message.getBytes());
        pValue.append(message);            

        return status;
    }

    
        

    public static TestSettings prepareTestSettings(short classType, short algorithmSpecification, short keyType, short keyLength, short algorithmMethod, short dataLength1, short dataLength2, short initMode, short numRepeatWholeOperation, short numRepeatSubOperation, short numRepeatWholeMeasurement) {
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
    public static void perftest_prepareClass(byte appletCLA, byte appletINS, short classType, short algorithmSpecification, short keyType, short keyLength, short algorithmMethod, short dataLength1, short dataLength2, short initMode, short numRepeatWholeOperation, short numRepeatSubOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        TestSettings testSet = prepareTestSettings(classType, algorithmSpecification, keyType, keyLength, algorithmMethod, dataLength1, dataLength2, initMode, numRepeatWholeOperation, numRepeatSubOperation, numRepeatWholeMeasurement);
        perftest_prepareClass(appletCLA, appletINS, testSet);
    }
    public static void perftest_prepareClass(byte appletCLA, byte appletINS, TestSettings testSet) throws IOException, Exception {
        // Free previously allocated objects
        boolean succes = cardManager.resetApplet(appletCLA, Consts.INS_CARD_RESET);
        // Prepare new set
        cardManager.PerfTestCommand(appletCLA, appletINS, testSet, Consts.INS_CARD_RESET);
    }
/*    
    public static double perftest_measure(byte appletCLA, byte appletPrepareINS, byte appletMeasureINS, short classType, short algorithmSpecification, short keyType, short keyLength, short algorithmMethod, short dataLength1, short dataLength2, short initMode, short numRepeatWholeOperation, short numRepeatSubOperation, short numRepeatWholeMeasurement, String info) throws IOException, Exception {
        TestSettings testSet = prepareTestSettings(classType, algorithmSpecification, keyType, keyLength, algorithmMethod, dataLength1, dataLength2, initMode, numRepeatWholeOperation, numRepeatSubOperation, numRepeatWholeMeasurement);
        return perftest_measure(appletCLA, appletPrepareINS, appletMeasureINS, testSet, info);
    }
*/    
    public static double perftest_measure(byte appletCLA, byte appletPrepareINS, byte appletMeasureINS, TestSettings testSet, String info) throws IOException, Exception {
        return perftest_measure(appletCLA, appletPrepareINS, appletMeasureINS, testSet, info, 0); 
    }
    public static double perftest_measure(byte appletCLA, byte appletPrepareINS, byte appletMeasureINS, TestSettings testSet, String info, double substractTime) throws IOException, Exception {
        double measureTime = -1;
        StringBuilder result = new StringBuilder();
        int numFailedRepeats = 0;
        
        while (numFailedRepeats < MAX_FAILED_REPEATS) {
            try {
                result.setLength(0);
                measureTime = perftest_measure(appletCLA, appletPrepareINS, appletMeasureINS, testSet, info, result, substractTime);
                // Measurement success, reset failed attempts counter
                numFailedRepeats = 0;
                // Write result string into file
                file.write(result.toString().getBytes());
                
                // end loop 
                return measureTime;
            }
            catch (CardCommunicationException ex) {
                // Normal exception like NO_SUCH_ALGORITHM  - just print it 
                String message = ex.toString();
                System.out.println(ex.toString()); 
                // Write result string into file
                file.write(result.toString().getBytes());
                // Write execption into file
                file.write(message.getBytes());
                
                return -1;
            }
            catch (Exception ex) {
                // Unexpected exception
                System.out.println(ex.toString()); 
                numFailedRepeats++;
                
                if (numFailedRepeats == MAX_FAILED_REPEATS) {
                    String message = "ERROR: unable to measure operation properly even after applet upload\n";
                    message += "Try to physically remove card and insert it again. Press any key and enter to to continue\n";
                    System.out.print(message);
                    Scanner sc = new Scanner(System.in);
                    sc.next();
                    // Card was physically removed, reset retries counter
                    numFailedRepeats = 0;
                }
                
                // Upload applet again and run measurement again
                CardMngr.UploadApplet(0, m_cardATR);
                CardMngr.ConnectToCard();
            }
        }
        
        return -1;
    }
    
    public static double perftest_measure(byte appletCLA, byte appletPrepareINS, byte appletMeasureINS, TestSettings testSet, String info, StringBuilder result) throws IOException, Exception {
        return perftest_measure(appletCLA, appletPrepareINS, appletMeasureINS, testSet, info, result, 0);
    }
    public static double perftest_measure(byte appletCLA, byte appletPrepareINS, byte appletMeasureINS, TestSettings testSet, String info, StringBuilder result, double substractTime) throws IOException, Exception {
        double check = 0.05; // Maximum percentage difference in which should be all times of individual operations, 0.1 = 10%
        double avgOpTime = -1;
        String message = "";
        // Tested method name
        message += "\nmethod name:; " + info + "\n";
        System.out.print(message);
        result.append(message);
        
        // Test settings
        byte[] settings = new byte[TestSettings.TEST_SETTINGS_LENGTH];  
        testSet.serializeToApduBuff(settings, (short) 0);
        message = "measurement config:;" + "appletPrepareINS;" + CardMngr.byteToHex(appletPrepareINS) + ";appletMeasureINS;" + CardMngr.byteToHex(appletMeasureINS) + ";config;" + CardMngr.bytesToHex(settings) + "\n";
        System.out.print(message);
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
        // Measure processing time without actually calling measured operation
        //
        short bkpNumRepeatWholeOperation = testSet.numRepeatWholeOperation;
        testSet.numRepeatWholeOperation = 0;
        message +=  "baseline measurements (ms):;";
        for(int i = 0; i < NUM_BASELINE_CALIBRATION_RUNS;i++) {
            cardManager.resetApplet(appletCLA, Consts.INS_CARD_RESET);
            double overheadTime = cardManager.PerfTestCommand(appletCLA, appletMeasureINS, testSet, Consts.INS_CARD_RESET);
            sumTimes += overheadTime;
            timeStr = String.format("%.2f", overheadTime);
            message +=  timeStr + ";" ;
            System.out.print(timeStr + " ");
            if (overheadTime<minOverhead) minOverhead=overheadTime;
            if (overheadTime>maxOverhead) maxOverhead=overheadTime;
        }
        avgOverhead = sumTimes / NUM_BASELINE_CALIBRATION_RUNS;
        message += "\nbaseline stats (ms):;avg:;" + String.format("%.2f", avgOverhead);
        message += ";min:;" + String.format("%.2f", minOverhead);
        message += ";max:;" + String.format("%.2f", maxOverhead);
        message += ";";
        if ((minOverhead/avgOverhead < (1-check)) || (maxOverhead/avgOverhead > (1+check)))  message += ";;CHECK";
        System.out.print("\nbaseline avg time: " + avgOverhead);
        System.out.println();     
        System.out.println(); message += "\n";
        result.append(message);
        message = "";


        //
        // Measure operations
        //
        // Restore required number of required measurements 
        testSet.numRepeatWholeOperation = bkpNumRepeatWholeOperation;

        double minOpTime = 9999;
        double maxOpTime = -9999;

        double time = 0;
        sumTimes = 0;
        message += "operation raw measurements (ms):;";
        for (int i = 0; i < testSet.numRepeatWholeMeasurement; i++) {
            cardManager.resetApplet(appletCLA, Consts.INS_CARD_RESET);
            time = cardManager.PerfTestCommand(appletCLA, appletMeasureINS, testSet, Consts.INS_CARD_RESET);
            time -= avgOverhead;
            if (Math.abs(substractTime) > 0.0005) { // comparing double to 0
                // Substract given time (if measured operation requires to perform another unrelated operation, e.g., setKey before clearKey)
                System.out.println("Substracting substractTime = " + substractTime + " from measured time"); 
                time -= substractTime;
            }
            
            sumTimes += time;
            timeStr = String.format("%.2f", time);
            message +=  timeStr + ";";
            System.out.print(timeStr + " ");
            if (time<minOpTime) minOpTime=time;
            if (time>maxOpTime) maxOpTime=time;
        }
        System.out.println();     

        System.out.println(); message += "\n";
        result.append(message);
        message = "";

        // Compute average per operation       
        String messageOpTime =  "operation stats (ms/op):";
        int totalIterations = testSet.numRepeatWholeOperation * testSet.numRepeatWholeMeasurement;
        avgOpTime = (totalIterations != 0) ? sumTimes/totalIterations : 0;
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
        System.out.println(messageOpTime);  

        System.out.println(); message += "\n";
        System.out.println(message); 
        
        return avgOpTime;
    }
    
    
    
        

    public static void testKeyPair(byte alg, short length, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        TestSettings testSet = prepareTestSettings(Consts.CLASS_KEYPAIR, Consts.UNUSED, Consts.UNUSED, length, JCConsts.KeyPair_genKeyPair, 
                Consts.UNUSED, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);      

        testSet.keyClass = alg;
        testSet.algorithmMethod = JCConsts.KeyPair_genKeyPair;
        PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEYPAIR, Consts.INS_PERF_TEST_CLASS_KEYPAIR, testSet, info + " KeyPair_genKeyPair()");
    }
    
    public static void testAllKeyPairs(int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception {
        testAllKeyPairs((short) numRepeatWholeOperation, (short) numRepeatWholeMeasurement);
    }
    public static void testAllKeyPairs(short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        String tableName = "\n\nKEY PAIR";
        file.write(tableName.getBytes());
        if (bTestAsymmetricAlgs) {
            String message = "\n";
            file.write(message.getBytes());
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
            testKeyPair(JCConsts.KeyPair_ALG_EC_FP,JCConsts.KeyBuilder_LENGTH_EC_FP_384,"ALG_EC_FP LENGTH_EC_FP_384", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testKeyPair(JCConsts.KeyPair_ALG_EC_FP,JCConsts.KeyBuilder_LENGTH_EC_FP_521,"ALG_EC_FP LENGTH_EC_FP_521", numRepeatWholeOperation, numRepeatWholeMeasurement); 
        }
        else {
            String message = "# Measurements excluded for asymmetric algorithms\n";
            file.write(message.getBytes());
        }
        tableName = "KEY PAIR - END\n";
        file.write(tableName.getBytes());
    }
    
    public static void testMessageDigest(byte alg, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        TestSettings testSet = PerformanceTesting.prepareTestSettings(Consts.CLASS_MESSAGEDIGEST, alg, Consts.UNUSED, Consts.UNUSED, 
                JCConsts.MessageDigest_update, Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);       

        if (!bTestVariableData) {
            // Ordinary test of all available methods
            testSet.algorithmMethod = JCConsts.MessageDigest_update;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_MESSAGEDIGEST, Consts.INS_PERF_TEST_CLASS_MESSAGEDIGEST, testSet, info + " MessageDigest_update()");
            testSet.algorithmMethod = JCConsts.MessageDigest_doFinal;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_MESSAGEDIGEST, Consts.INS_PERF_TEST_CLASS_MESSAGEDIGEST, testSet, info + " MessageDigest_doFinal()");
            testSet.algorithmMethod = JCConsts.MessageDigest_reset;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_MESSAGEDIGEST, Consts.INS_PERF_TEST_CLASS_MESSAGEDIGEST, testSet, info + " MessageDigest_reset()");
        }
        else {
            // Test of speed dependant on data length
            String tableName = "\n\nMESSAGE DIGEST - "  + info + " - variable data - BEGIN\n";
            file.write(tableName.getBytes());
            testSet.algorithmMethod = JCConsts.MessageDigest_doFinal;
            for (Integer length : m_testDataLengths) {
                testSet.dataLength1 = length.shortValue();
                PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_MESSAGEDIGEST, Consts.INS_PERF_TEST_CLASS_MESSAGEDIGEST, testSet, info + " MessageDigest_doFinal()");
            }
            tableName = "\n\nMESSAGE DIGEST - "  + info + " - variable data - END\n";
            file.write(tableName.getBytes());
        }
    }   
    

    public static void testAllMessageDigests(int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception {
        testAllMessageDigests((short) numRepeatWholeOperation, (short) numRepeatWholeMeasurement);
    }
    public static void testAllMessageDigests(short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        String tableName = "\n\nMESSAGE DIGEST\n";
        file.write(tableName.getBytes());
        if (bTestSymmetricAlgs) {
            testMessageDigest(JCConsts.MessageDigest_ALG_SHA,"ALG_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testMessageDigest(JCConsts.MessageDigest_ALG_MD5,"ALG_MD5", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testMessageDigest(JCConsts.MessageDigest_ALG_RIPEMD160,"ALG_RIPEMD160", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testMessageDigest(JCConsts.MessageDigest_ALG_SHA_256,"ALG_SHA_256", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testMessageDigest(JCConsts.MessageDigest_ALG_SHA_384,"ALG_SHA_384", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testMessageDigest(JCConsts.MessageDigest_ALG_SHA_512,"ALG_SHA_512", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testMessageDigest(JCConsts.MessageDigest_ALG_SHA_224,"ALG_SHA_224", numRepeatWholeOperation, numRepeatWholeMeasurement);
        }
        else {
            String message = "# Measurements excluded for symmetric algorithms\n";
            file.write(message.getBytes());
        }
        tableName = "MESSAGE DIGEST - END\n";
        file.write(tableName.getBytes());
    }

    public static void testRandomGenerator(byte alg, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        TestSettings testSet = PerformanceTesting.prepareTestSettings(Consts.CLASS_RANDOMDATA, alg, Consts.UNUSED, Consts.UNUSED, JCConsts.RandomData_generateData, 
                Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);      

        if (!bTestVariableData) {
            // Ordinary test of all available methods
            testSet.algorithmMethod = JCConsts.RandomData_generateData;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_RANDOMDATA, Consts.INS_PERF_TEST_CLASS_RANDOMDATA, testSet, info + " RandomData_generateData()");
            testSet.algorithmMethod = JCConsts.RandomData_setSeed;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_RANDOMDATA, Consts.INS_PERF_TEST_CLASS_RANDOMDATA, testSet, info + " RandomData_setSeed()");        
        }
        else {
            // Test of speed dependant on data length
            String tableName = "\n\nRANDOM GENERATOR - "  + info + " - variable data - BEGIN\n";
            file.write(tableName.getBytes());
            testSet.algorithmMethod = JCConsts.RandomData_generateData;
            for (Integer length : m_testDataLengths) {
                testSet.dataLength1 = length.shortValue();
                PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_RANDOMDATA, Consts.INS_PERF_TEST_CLASS_RANDOMDATA, testSet, info + " RandomData_generateData();" + length + ";");
            }
            tableName = "\n\nRANDOM GENERATOR - "  + info + " - variable data - END\n";
            file.write(tableName.getBytes());
        }
    }    
    public static void testAllRandomGenerators(int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception {
        testAllRandomGenerators((short) numRepeatWholeOperation, (short) numRepeatWholeMeasurement);
    }
    public static void testAllRandomGenerators(short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        String tableName = "\n\nRANDOM GENERATOR\n";
        file.write(tableName.getBytes());
        if (bTestSymmetricAlgs) {
            testRandomGenerator(JCConsts.RandomData_ALG_PSEUDO_RANDOM,"ALG_PSEUDO_RANDOM", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRandomGenerator(JCConsts.RandomData_ALG_SECURE_RANDOM,"ALG_SECURE_RANDOM", numRepeatWholeOperation, numRepeatWholeMeasurement);     
        }
        else {
            String message = "# Measurements excluded for symmetric algorithms\n";
            file.write(message.getBytes());
        }
        tableName = "RANDOM GENERATOR - END\n";
        file.write(tableName.getBytes());
    }    

    public static void testCipher(byte key, short keyLength, byte alg, String info, short initMode, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        short testDataLength = Consts.TEST_DATA_LENGTH; // default test length
        switch (key) {
            case JCConsts.KeyBuilder_TYPE_RSA_PRIVATE:
            case JCConsts.KeyBuilder_TYPE_RSA_PUBLIC:
            case JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE:
                // For RSA, we need test length equal to modulus size
                testDataLength = (short) (keyLength / 8);
                break;
        }    
        
        TestSettings testSet = PerformanceTesting.prepareTestSettings(Consts.CLASS_CIPHER, alg, key, keyLength, JCConsts.Cipher_update, 
                testDataLength, Consts.UNUSED, initMode, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);      

        
        if (!bTestVariableData) {
            // Ordinary test of all available methods
            testSet.algorithmMethod = JCConsts.Cipher_update;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CIPHER, Consts.INS_PERF_TEST_CLASS_CIPHER, testSet, info + " Cipher_update()");
            testSet.algorithmMethod = JCConsts.Cipher_doFinal;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CIPHER, Consts.INS_PERF_TEST_CLASS_CIPHER, testSet, info + " Cipher_doFinal()");
            testSet.algorithmMethod = JCConsts.Cipher_init;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CIPHER, Consts.INS_PERF_TEST_CLASS_CIPHER, testSet, info + " Cipher_init()");
        }
        else {
            // Test of speed dependant on data length
            String tableName = "\n\nCIPHER - " + info + " - variable data - BEGIN\n";
            file.write(tableName.getBytes());
            switch (key) {
                case JCConsts.KeyBuilder_TYPE_RSA_PRIVATE:
                case JCConsts.KeyBuilder_TYPE_RSA_PUBLIC:
                case JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE:
                    // For RSA, variable length perf test is not supported
                    tableName = "For RSA, variable length perf test is not supported\n";
                    file.write(tableName.getBytes());
                    System.out.print(tableName);
                    return;
            }    
            
            testSet.algorithmMethod = JCConsts.Cipher_doFinal;
            for (Integer length : m_testDataLengths) {
                testSet.dataLength1 = length.shortValue();
                PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CIPHER, Consts.INS_PERF_TEST_CLASS_CIPHER, testSet, info + " Cipher_doFinal()");
            }
            tableName = "\n\nCIPHER - " + info + " - variable data - END\n";
            file.write(tableName.getBytes());
        }
        
    }
    
    public static double testCipher(byte key, short keyLength, byte alg, String info, short initMode, short numRepeatWholeOperation, short numRepeatWholeMeasurement, short dataLength) throws IOException, Exception {
        double result;
        short testDataLength = dataLength;
        switch (key) {
            case JCConsts.KeyBuilder_TYPE_RSA_PRIVATE:
            case JCConsts.KeyBuilder_TYPE_RSA_PUBLIC:
            case JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE:
                // For RSA, we need test length equal to modulus size
                testDataLength = (short) (keyLength / 8);
                break;
        }    
        
        TestSettings testSet = PerformanceTesting.prepareTestSettings(Consts.CLASS_CIPHER, alg, key, keyLength, JCConsts.Cipher_update, 
                testDataLength, Consts.UNUSED, initMode, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);     

        testSet.algorithmMethod = JCConsts.Cipher_update;
        PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CIPHER, Consts.INS_PERF_TEST_CLASS_CIPHER, testSet, info + " Cipher_update()");
        testSet.algorithmMethod = JCConsts.Cipher_doFinal;
        result = PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CIPHER, Consts.INS_PERF_TEST_CLASS_CIPHER, testSet, info + " Cipher_doFinal()");
        testSet.algorithmMethod = JCConsts.Cipher_init;
        PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CIPHER, Consts.INS_PERF_TEST_CLASS_CIPHER, testSet, info + " Cipher_init()");

        return result;
    }   
    
    public static void testAllCiphers(int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception {
        testAllCiphers((short) numRepeatWholeOperation, (short) numRepeatWholeMeasurement);
    }
    public static void testAllCiphers(short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        String tableName = "\n\nCIPHER\n";
        file.write(tableName.getBytes());
        if (bTestSymmetricAlgs) {
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
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Cipher_ALG_AES_BLOCK_128_ECB_NOPAD,"TYPE_AES LENGTH_AES_256 ALG_AES_BLOCK_128_ECB_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Cipher_ALG_AES_BLOCK_192_CBC_NOPAD,"TYPE_AES LENGTH_AES_256 ALG_AES_BLOCK_192_CBC_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Cipher_ALG_AES_BLOCK_192_ECB_NOPAD,"TYPE_AES LENGTH_AES_256 ALG_AES_BLOCK_192_ECB_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Cipher_ALG_AES_BLOCK_256_CBC_NOPAD,"TYPE_AES LENGTH_AES_256 ALG_AES_BLOCK_256_CBC_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Cipher_ALG_AES_BLOCK_256_ECB_NOPAD,"TYPE_AES LENGTH_AES_256 ALG_AES_BLOCK_256_ECB_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Cipher_ALG_AES_CBC_ISO9797_M1,"TYPE_AES LENGTH_AES_256 ALG_AES_CBC_ISO9797_M1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Cipher_ALG_AES_CBC_ISO9797_M2,"TYPE_AES LENGTH_AES_256 ALG_AES_CBC_ISO9797_M2", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Cipher_ALG_AES_CBC_PKCS5,"TYPE_AES LENGTH_AES_256 ALG_AES_CBC_PKCS5", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Cipher_ALG_AES_ECB_ISO9797_M1,"TYPE_AES LENGTH_AES_256 ALG_AES_ECB_ISO9797_M1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Cipher_ALG_AES_ECB_ISO9797_M2,"TYPE_AES LENGTH_AES_256 ALG_AES_ECB_ISO9797_M2", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Cipher_ALG_AES_ECB_PKCS5,"TYPE_AES LENGTH_AES_256 ALG_AES_ECB_PKCS5", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement); 
        }
        else {
            String message = "# Measurements excluded for symmetric algorithms\n";
            file.write(message.getBytes());
        }
        if (bTestAsymmetricAlgs) {
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
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_PUBLIC LENGTH_RSA_512 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_PUBLIC LENGTH_RSA_512 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_PUBLIC LENGTH_RSA_512 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_PUBLIC LENGTH_RSA_512 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_PUBLIC LENGTH_RSA_512 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_PUBLIC LENGTH_RSA_736 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_PUBLIC LENGTH_RSA_736 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_PUBLIC LENGTH_RSA_736 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_PUBLIC LENGTH_RSA_736 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_PUBLIC LENGTH_RSA_736 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_PUBLIC LENGTH_RSA_768 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_PUBLIC LENGTH_RSA_768 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_PUBLIC LENGTH_RSA_768 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_PUBLIC LENGTH_RSA_768 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_PUBLIC LENGTH_RSA_768 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_PUBLIC LENGTH_RSA_896 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_PUBLIC LENGTH_RSA_896 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_PUBLIC LENGTH_RSA_896 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_PUBLIC LENGTH_RSA_896 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_PUBLIC LENGTH_RSA_896 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_PUBLIC LENGTH_RSA_1024 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_PUBLIC LENGTH_RSA_1024 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_PUBLIC LENGTH_RSA_1024 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_PUBLIC LENGTH_RSA_1024 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_PUBLIC LENGTH_RSA_1024 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_PUBLIC LENGTH_RSA_1280 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_PUBLIC LENGTH_RSA_1280 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_PUBLIC LENGTH_RSA_1280 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_PUBLIC LENGTH_RSA_1280 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_PUBLIC LENGTH_RSA_1280 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_PUBLIC LENGTH_RSA_1536 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_PUBLIC LENGTH_RSA_1536 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_PUBLIC LENGTH_RSA_1536 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_PUBLIC LENGTH_RSA_1536 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_PUBLIC LENGTH_RSA_1536 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_PUBLIC LENGTH_RSA_1984 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_PUBLIC LENGTH_RSA_1984 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_PUBLIC LENGTH_RSA_1984 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_PUBLIC LENGTH_RSA_1984 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_PUBLIC LENGTH_RSA_1984 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_PUBLIC LENGTH_RSA_2048 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_PUBLIC LENGTH_RSA_2048 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_PUBLIC LENGTH_RSA_2048 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_PUBLIC LENGTH_RSA_2048 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_PUBLIC LENGTH_RSA_2048 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_PUBLIC LENGTH_RSA_3072 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_PUBLIC LENGTH_RSA_3072 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_PUBLIC LENGTH_RSA_3072 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_PUBLIC LENGTH_RSA_3072 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_PUBLIC LENGTH_RSA_3072 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Cipher_ALG_RSA_ISO14888,"TYPE_RSA_PUBLIC LENGTH_RSA_4096 ALG_RSA_ISO14888", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Cipher_ALG_RSA_ISO9796,"TYPE_RSA_PUBLIC LENGTH_RSA_4096 ALG_RSA_ISO9796", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Cipher_ALG_RSA_NOPAD,"TYPE_RSA_PUBLIC LENGTH_RSA_4096 ALG_RSA_NOPAD", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Cipher_ALG_RSA_PKCS1,"TYPE_RSA_PUBLIC LENGTH_RSA_4096 ALG_RSA_PKCS1", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement);
            testCipher(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"TYPE_RSA_PUBLIC LENGTH_RSA_4096 ALG_RSA_PKCS1_OAEP", JCConsts.Cipher_MODE_ENCRYPT, numRepeatWholeOperation, numRepeatWholeMeasurement); 
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
        }
        else {
            String message = "# Measurements excluded for asymmetric algorithms\n";
            file.write(message.getBytes());
        }        
        tableName = "CIPHER - END\n";
        file.write(tableName.getBytes());
    }
    
    public static void testSignature(byte keyType, short keyLength, byte alg, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        testSignatureWithKeyClass(Consts.UNUSED, keyType, keyLength, alg, info, numRepeatWholeOperation, numRepeatWholeMeasurement);
    }   
    public static void testSignatureWithKeyClass(byte keyClass, byte keyType, short keyLength, byte alg, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        TestSettings testSet = PerformanceTesting.prepareTestSettings(Consts.CLASS_SIGNATURE, alg, keyType, keyLength, JCConsts.Signature_update, 
            Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);      
        testSet.keyClass = keyClass;
        
        if (!bTestVariableData) {
            testSet.algorithmMethod = JCConsts.Signature_update;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_SIGNATURE, Consts.INS_PERF_TEST_CLASS_SIGNATURE, testSet, info + " Signature_update()");
            testSet.algorithmMethod = JCConsts.Signature_sign;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_SIGNATURE, Consts.INS_PERF_TEST_CLASS_SIGNATURE, testSet, info + " Signature_sign()");
            testSet.algorithmMethod = JCConsts.Signature_verify;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_SIGNATURE, Consts.INS_PERF_TEST_CLASS_SIGNATURE, testSet, info + " Signature_verify()");
            testSet.algorithmMethod = JCConsts.Signature_init;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_SIGNATURE, Consts.INS_PERF_TEST_CLASS_SIGNATURE, testSet, info + " Signature_init()");
        }
        else {
            // Test of speed dependant on data length
            String tableName = "\n\nSIGNATURE - "  + info + " - variable data - BEGIN\n";
            file.write(tableName.getBytes());
            testSet.algorithmMethod = JCConsts.Signature_sign;
            for (Integer length : m_testDataLengths) {
                testSet.dataLength1 = length.shortValue();
                PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_SIGNATURE, Consts.INS_PERF_TEST_CLASS_SIGNATURE, testSet, info + " Signature_sign()");
            }
            tableName = "\n\nSIGNATURE - "  + info + " - variable data - END\n";
            file.write(tableName.getBytes());
        }
    }
    public static void testAllSignatures(int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception {
        testAllSignatures((short) numRepeatWholeOperation, (short) numRepeatWholeMeasurement);
    }
    public static void testAllSignatures(short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        String tableName = "\n\nSIGNATURE\n";
        file.write(tableName.getBytes());
        if (bTestSymmetricAlgs) {
            testSignature(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_128,JCConsts.Signature_ALG_AES_MAC_128_NOPAD,"TYPE_AES LENGTH_AES_128 ALG_AES_MAC_128_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignature(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_192,JCConsts.Signature_ALG_AES_MAC_128_NOPAD,"TYPE_AES LENGTH_AES_192 ALG_AES_MAC_128_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignature(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Signature_ALG_AES_MAC_128_NOPAD,"TYPE_AES LENGTH_AES_256 ALG_AES_MAC_128_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
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
            String message = "# Measurements excluded for symmetric algorithms\n";
            file.write(message.getBytes());
        }
        
        if (bTestAsymmetricAlgs) {
            // ALG_EC_F2M
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_F2M, JCConsts.KeyBuilder_ALG_TYPE_EC_F2M_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_F2M_113,JCConsts.Signature_ALG_ECDSA_SHA,"KeyPair_ALG_EC_F2M KeyBuilder_LENGTH_EC_F2M_113 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_F2M, JCConsts.KeyBuilder_ALG_TYPE_EC_F2M_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_F2M_131,JCConsts.Signature_ALG_ECDSA_SHA,"KeyPair_ALG_EC_F2M KeyBuilder_LENGTH_EC_F2M_131 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_F2M, JCConsts.KeyBuilder_ALG_TYPE_EC_F2M_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_F2M_163,JCConsts.Signature_ALG_ECDSA_SHA,"KeyPair_ALG_EC_F2M KeyBuilder_LENGTH_EC_F2M_163 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_F2M, JCConsts.KeyBuilder_ALG_TYPE_EC_F2M_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_F2M_193,JCConsts.Signature_ALG_ECDSA_SHA,"KeyPair_ALG_EC_F2M KeyBuilder_LENGTH_EC_F2M_193 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);

            // ALG_EC_FP
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_ALG_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_112,JCConsts.Signature_ALG_ECDSA_SHA,"KeyPair_ALG_EC_FP KeyBuilder_LENGTH_EC_FP_112 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_ALG_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_128,JCConsts.Signature_ALG_ECDSA_SHA,"KeyPair_ALG_EC_FP KeyBuilder_LENGTH_EC_FP_128 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_ALG_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_160,JCConsts.Signature_ALG_ECDSA_SHA,"KeyPair_ALG_EC_FP KeyBuilder_LENGTH_EC_FP_160 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_ALG_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_192,JCConsts.Signature_ALG_ECDSA_SHA,"KeyPair_ALG_EC_FP KeyBuilder_LENGTH_EC_FP_192 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_ALG_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_224,JCConsts.Signature_ALG_ECDSA_SHA,"KeyPair_ALG_EC_FP KeyBuilder_LENGTH_EC_FP_224 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_ALG_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_256,JCConsts.Signature_ALG_ECDSA_SHA,"KeyPair_ALG_EC_FP KeyBuilder_LENGTH_EC_FP_256 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_ALG_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_384,JCConsts.Signature_ALG_ECDSA_SHA,"KeyPair_ALG_EC_FP KeyBuilder_LENGTH_EC_FP_384 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_EC_FP, JCConsts.KeyBuilder_ALG_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_521,JCConsts.Signature_ALG_ECDSA_SHA,"KeyPair_ALG_EC_FP KeyBuilder_LENGTH_EC_FP_521 Signature_ALG_ECDSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);

            // DSA
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_DSA, JCConsts.KeyBuilder_ALG_TYPE_DSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_DSA_512,JCConsts.Signature_ALG_DSA_SHA,"ALG_DSA LENGTH_DSA_512 ALG_DSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_DSA, JCConsts.KeyBuilder_ALG_TYPE_DSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_DSA_768,JCConsts.Signature_ALG_DSA_SHA,"ALG_DSA LENGTH_DSA_768 ALG_DSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_DSA, JCConsts.KeyBuilder_ALG_TYPE_DSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_DSA_1024,JCConsts.Signature_ALG_DSA_SHA,"ALG_DSA LENGTH_DSA_1024 ALG_DSA_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);

            // ALG_RSA
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Signature_ALG_RSA_MD5_PKCS1,"ALG_RSA LENGTH_RSA_512 ALG_RSA_MD5_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Signature_ALG_RSA_MD5_PKCS1,"ALG_RSA LENGTH_RSA_736 ALG_RSA_MD5_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Signature_ALG_RSA_MD5_PKCS1,"ALG_RSA LENGTH_RSA_768 ALG_RSA_MD5_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Signature_ALG_RSA_MD5_PKCS1,"ALG_RSA LENGTH_RSA_896 ALG_RSA_MD5_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Signature_ALG_RSA_MD5_PKCS1,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_MD5_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Signature_ALG_RSA_MD5_PKCS1,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_MD5_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Signature_ALG_RSA_MD5_PKCS1,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_MD5_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Signature_ALG_RSA_MD5_PKCS1,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_MD5_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);  
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Signature_ALG_RSA_MD5_PKCS1,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_MD5_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Signature_ALG_RSA_MD5_PKCS1,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_MD5_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Signature_ALG_RSA_MD5_PKCS1,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_MD5_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);

            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Signature_ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA LENGTH_RSA_512 ALG_RSA_MD5_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Signature_ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA LENGTH_RSA_736 ALG_RSA_MD5_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Signature_ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA LENGTH_RSA_768 ALG_RSA_MD5_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Signature_ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA LENGTH_RSA_896 ALG_RSA_MD5_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Signature_ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_MD5_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Signature_ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_MD5_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Signature_ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_MD5_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Signature_ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_MD5_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);  
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Signature_ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_MD5_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Signature_ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_MD5_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Signature_ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_MD5_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);

            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Signature_ALG_RSA_MD5_RFC2409,"ALG_RSA LENGTH_RSA_512 ALG_RSA_MD5_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Signature_ALG_RSA_MD5_RFC2409,"ALG_RSA LENGTH_RSA_736 ALG_RSA_MD5_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Signature_ALG_RSA_MD5_RFC2409,"ALG_RSA LENGTH_RSA_768 ALG_RSA_MD5_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Signature_ALG_RSA_MD5_RFC2409,"ALG_RSA LENGTH_RSA_896 ALG_RSA_MD5_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Signature_ALG_RSA_MD5_RFC2409,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_MD5_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Signature_ALG_RSA_MD5_RFC2409,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_MD5_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Signature_ALG_RSA_MD5_RFC2409,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_MD5_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Signature_ALG_RSA_MD5_RFC2409,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_MD5_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);  
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Signature_ALG_RSA_MD5_RFC2409,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_MD5_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Signature_ALG_RSA_MD5_RFC2409,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_MD5_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Signature_ALG_RSA_MD5_RFC2409,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_MD5_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);

            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Signature_ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA LENGTH_RSA_512 ALG_RSA_RIPEMD160_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Signature_ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA LENGTH_RSA_736 ALG_RSA_RIPEMD160_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Signature_ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA LENGTH_RSA_768 ALG_RSA_RIPEMD160_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Signature_ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA LENGTH_RSA_896 ALG_RSA_RIPEMD160_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Signature_ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_RIPEMD160_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Signature_ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_RIPEMD160_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Signature_ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_RIPEMD160_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Signature_ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_RIPEMD160_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);  
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Signature_ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_RIPEMD160_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Signature_ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_RIPEMD160_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Signature_ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_RIPEMD160_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);

            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA LENGTH_RSA_512 ALG_RSA_RIPEMD160_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA LENGTH_RSA_736 ALG_RSA_RIPEMD160_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA LENGTH_RSA_768 ALG_RSA_RIPEMD160_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA LENGTH_RSA_896 ALG_RSA_RIPEMD160_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_RIPEMD160_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_RIPEMD160_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_RIPEMD160_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_RIPEMD160_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);  
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_RIPEMD160_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_RIPEMD160_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_RIPEMD160_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);

            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA LENGTH_RSA_512 ALG_RSA_RIPEMD160_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA LENGTH_RSA_736 ALG_RSA_RIPEMD160_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA LENGTH_RSA_768 ALG_RSA_RIPEMD160_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA LENGTH_RSA_896 ALG_RSA_RIPEMD160_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_RIPEMD160_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_RIPEMD160_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_RIPEMD160_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_RIPEMD160_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);  
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_RIPEMD160_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_RIPEMD160_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Signature_ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_RIPEMD160_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);

            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Signature_ALG_RSA_SHA_ISO9796,"ALG_RSA LENGTH_RSA_512 ALG_RSA_SHA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Signature_ALG_RSA_SHA_ISO9796,"ALG_RSA LENGTH_RSA_736 ALG_RSA_SHA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Signature_ALG_RSA_SHA_ISO9796,"ALG_RSA LENGTH_RSA_768 ALG_RSA_SHA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Signature_ALG_RSA_SHA_ISO9796,"ALG_RSA LENGTH_RSA_896 ALG_RSA_SHA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Signature_ALG_RSA_SHA_ISO9796,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_SHA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Signature_ALG_RSA_SHA_ISO9796,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_SHA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Signature_ALG_RSA_SHA_ISO9796,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_SHA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Signature_ALG_RSA_SHA_ISO9796,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_SHA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);  
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Signature_ALG_RSA_SHA_ISO9796,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_SHA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Signature_ALG_RSA_SHA_ISO9796,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_SHA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Signature_ALG_RSA_SHA_ISO9796,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_SHA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);

            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Signature_ALG_RSA_SHA_PKCS1,"ALG_RSA LENGTH_RSA_512 ALG_RSA_SHA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Signature_ALG_RSA_SHA_PKCS1,"ALG_RSA LENGTH_RSA_736 ALG_RSA_SHA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Signature_ALG_RSA_SHA_PKCS1,"ALG_RSA LENGTH_RSA_768 ALG_RSA_SHA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Signature_ALG_RSA_SHA_PKCS1,"ALG_RSA LENGTH_RSA_896 ALG_RSA_SHA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Signature_ALG_RSA_SHA_PKCS1,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_SHA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Signature_ALG_RSA_SHA_PKCS1,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_SHA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Signature_ALG_RSA_SHA_PKCS1,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_SHA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Signature_ALG_RSA_SHA_PKCS1,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_SHA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);  
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Signature_ALG_RSA_SHA_PKCS1,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_SHA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Signature_ALG_RSA_SHA_PKCS1,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_SHA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Signature_ALG_RSA_SHA_PKCS1,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_SHA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);

            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Signature_ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA LENGTH_RSA_512 ALG_RSA_SHA_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Signature_ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA LENGTH_RSA_736 ALG_RSA_SHA_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Signature_ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA LENGTH_RSA_768 ALG_RSA_SHA_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Signature_ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA LENGTH_RSA_896 ALG_RSA_SHA_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Signature_ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_SHA_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Signature_ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_SHA_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Signature_ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_SHA_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Signature_ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_SHA_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);  
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Signature_ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_SHA_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Signature_ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_SHA_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Signature_ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_SHA_PKCS1_PSS", numRepeatWholeOperation, numRepeatWholeMeasurement);

            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Signature_ALG_RSA_SHA_RFC2409,"ALG_RSA LENGTH_RSA_512 ALG_RSA_SHA_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Signature_ALG_RSA_SHA_RFC2409,"ALG_RSA LENGTH_RSA_736 ALG_RSA_SHA_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Signature_ALG_RSA_SHA_RFC2409,"ALG_RSA LENGTH_RSA_768 ALG_RSA_SHA_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Signature_ALG_RSA_SHA_RFC2409,"ALG_RSA LENGTH_RSA_896 ALG_RSA_SHA_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Signature_ALG_RSA_SHA_RFC2409,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_SHA_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Signature_ALG_RSA_SHA_RFC2409,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_SHA_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Signature_ALG_RSA_SHA_RFC2409,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_SHA_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Signature_ALG_RSA_SHA_RFC2409,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_SHA_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);  
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Signature_ALG_RSA_SHA_RFC2409,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_SHA_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Signature_ALG_RSA_SHA_RFC2409,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_SHA_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testSignatureWithKeyClass(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Signature_ALG_RSA_SHA_RFC2409,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_SHA_RFC2409", numRepeatWholeOperation, numRepeatWholeMeasurement);

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
        }
        else {
            String message = "# Measurements excluded for asymmetric algorithms\n";
            file.write(message.getBytes());
        }          
        
        tableName = "SIGNATURE - END\n";
        file.write(tableName.getBytes());        
    }    
    
    public static void testChecksum(byte alg, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        TestSettings testSet = PerformanceTesting.prepareTestSettings(Consts.CLASS_CHECKSUM, alg, Consts.UNUSED, Consts.UNUSED, JCConsts.Signature_update, 
            Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);      
        
        if (!bTestVariableData) {
            testSet.algorithmMethod = JCConsts.Checksum_update;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CHECKSUM, Consts.INS_PERF_TEST_CLASS_CHECKSUM, testSet, info + " Checksum_update()");
            testSet.algorithmMethod = JCConsts.Checksum_doFinal;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CHECKSUM, Consts.INS_PERF_TEST_CLASS_CHECKSUM, testSet, info + " Checksum_doFinal()");
        }
        else {
            // Test of speed dependant on data length
            String tableName = "\n\nCHECKSUM - "  + info + " - variable data - BEGIN\n";
            file.write(tableName.getBytes());
            testSet.algorithmMethod = JCConsts.Checksum_doFinal;
            for (Integer length : m_testDataLengths) {
                testSet.dataLength1 = length.shortValue();
                PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CHECKSUM, Consts.INS_PERF_TEST_CLASS_CHECKSUM, testSet, info + " Checksum_doFinal()");
            }
            tableName = "\n\nCHECKSUM - "  + info + " - variable data - END\n";
            file.write(tableName.getBytes());
        }
    }   
    public static void testAllChecksums(int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception {
        testAllChecksums((short) numRepeatWholeOperation, (short) numRepeatWholeMeasurement);
    }
    public static void testAllChecksums(short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        String tableName = "\n\nCHECKSUM\n";
        file.write(tableName.getBytes());
        if (bTestSymmetricAlgs) {
            testChecksum(JCConsts.Checksum_ALG_ISO3309_CRC16,"ALG_ISO3309_CRC16", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testChecksum(JCConsts.Checksum_ALG_ISO3309_CRC32,"ALG_ISO3309_CRC32", numRepeatWholeOperation, numRepeatWholeMeasurement);
        }
        else {
            String message = "# Measurements excluded for symmetric algorithms\n";
            file.write(message.getBytes());
        }
        tableName = "CHECKSUM - END\n";
        file.write(tableName.getBytes());
    }    
    
    // TODO: KeyAgreement tests
    
    public static void testAESKey(byte keyType, short keyLength, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        TestSettings testSet = null;
        testSet = PerformanceTesting.prepareTestSettings(Consts.CLASS_KEYBUILDER, Consts.UNUSED, keyType, keyLength, JCConsts.AESKey_setKey, 
                Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);      
        
        if (!bTestVariableData) {
            testSet.algorithmMethod = JCConsts.AESKey_setKey;
            double setKeyTime = PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setKey()");
            testSet.algorithmMethod = JCConsts.AESKey_getKey;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getKey()");
            // Note: clear internally executes also setKey - substract setKey time from result
            testSet.algorithmMethod = JCConsts.AESKey_clearKey;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " clearKey()", setKeyTime);
        }
        else {
            String message = "No variable data test for " + info + "\n";
            file.write(message.getBytes());
            System.out.print(message);
        }
    }
    
    public static void testAllAESKeys(int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception {
        testAllAESKeys((short) numRepeatWholeOperation, (short) numRepeatWholeMeasurement);
    }
    public static void testAllAESKeys(short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        String tableName = "\n\nAESKey\n";
        file.write(tableName.getBytes());
        if (bTestSymmetricAlgs) {
            testAESKey(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_128,"TYPE_AES LENGTH_AES_128", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testAESKey(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_192,"TYPE_AES LENGTH_AES_192", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testAESKey(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,"TYPE_AES LENGTH_AES_256", numRepeatWholeOperation, numRepeatWholeMeasurement);
        }
        else {
            String message = "# Measurements excluded for symmetric algorithms\n";
            file.write(message.getBytes());
        }
        
        tableName = "AESKey - END\n";
        file.write(tableName.getBytes());
    }
    
    public static void testDESKey (byte keyType, short keyLength, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws Exception{
        TestSettings testSet = null;
        testSet = PerformanceTesting.prepareTestSettings(Consts.CLASS_KEYBUILDER, Consts.UNUSED, keyType, keyLength, JCConsts.DESKey_setKey,
                Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);
        if (!bTestVariableData) {
            testSet.algorithmMethod = JCConsts.DESKey_setKey;
            double setKeyTime = PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setKey()");
            testSet.algorithmMethod = JCConsts.DESKey_getKey;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getKey()");
            testSet.algorithmMethod = JCConsts.DESKey_clearKey;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " clearKey()");
        }
        else {
            String message = "No variable data test for " + info + "\n";
            file.write(message.getBytes());
            System.out.print(message);
        }
    }
    
    public static void testAllDESKeys (int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws Exception{
        testAllDESKeys((short)numRepeatWholeOperation, (short)numRepeatWholeMeasurement);
    }
    
    public static void testAllDESKeys (short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception{
        String tableName = "\n\nDESKey";
        file.write(tableName.getBytes());
        
        if (bTestSymmetricAlgs) {
            testDESKey(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES, "TYPE_DES LENGTH_DES_64", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testDESKey(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_2KEY, "TYPE_DES LENGTH_DES_128", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testDESKey(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_3KEY, "TYPE_DES LENGTH_DES_192", numRepeatWholeOperation, numRepeatWholeMeasurement);
        }
        else {
            String message = "# Measurements excluded for symmetric algorithms\n";
            file.write(message.getBytes());
        }
        
        tableName = "DESKey - END\n";
        file.write(tableName.getBytes());
    }
    
    public static void testKoreanSEEDKey (byte keyType, short keyLength, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws Exception{
        TestSettings testSet = null;
        testSet = PerformanceTesting.prepareTestSettings(Consts.CLASS_KEYBUILDER, Consts.UNUSED, keyType, keyLength, JCConsts.KoreanSEEDKey_setKey,
                Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);
        
        if (!bTestVariableData) {
            testSet.algorithmMethod = JCConsts.KoreanSEEDKey_setKey;
            double setKeyTime = PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setKey()");
            testSet.algorithmMethod = JCConsts.KoreanSEEDKey_getKey;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getKey()");
            testSet.algorithmMethod = JCConsts.KoreanSEEDKey_clearKey;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " clearKey()");
        }
        else {
            String message = "No variable data test for " + info + "\n";
            file.write(message.getBytes());
            System.out.print(message);
        }
    }
    public static void testAllKoreanSEEDKeys (int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception{
        testAllKoreanSEEDKeys((short)numRepeatWholeOperation, (short)numRepeatWholeMeasurement);
    }
    public static void testAllKoreanSEEDKeys (short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception{
        String tableName = "\n\nKoreanSEEDKey";
        file.write(tableName.getBytes());
        
        if (bTestSymmetricAlgs) {
            testKoreanSEEDKey(JCConsts.KeyBuilder_TYPE_KOREAN_SEED, JCConsts.KeyBuilder_LENGTH_KOREAN_SEED_128, "TYPE KOREAN SEED LENGTH KOREAN SEED 128", numRepeatWholeOperation, numRepeatWholeMeasurement);
        }
        else {
            String message = "# Measurements excluded for symmetric algorithms\n";
            file.write(message.getBytes());
        }
        
        tableName = "KoreanSEEDKey - END\n";
        file.write(tableName.getBytes());
    }
    
    public static void testDSAPrivateKey (byte keyType, short keyLength, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws Exception{
        TestSettings testSet = null;
        testSet = PerformanceTesting.prepareTestSettings(Consts.CLASS_KEYBUILDER, Consts.UNUSED, keyType, keyLength, JCConsts.DSAPrivateKey_getX,
                Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);
        
        if (!bTestVariableData) {
            testSet.algorithmMethod = JCConsts.DSAPrivateKey_getX;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setX()");
            testSet.algorithmMethod = JCConsts.DSAPrivateKey_setX;
            double setXTime = PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getX()");
            testSet.algorithmMethod = JCConsts.DSAPrivateKey_clearX;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " clearX()", setXTime);
        }
        else {
            String message = "No variable data test for " + info + "\n";
            file.write(message.getBytes());
            System.out.print(message);
        }
    }
    public static void testAllDSAPrivateKeys (int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception{
        testAllDSAPrivateKeys((short)numRepeatWholeOperation, (short)numRepeatWholeMeasurement);
    }
    public static void testAllDSAPrivateKeys (short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception{
        String tableName = "\n\nDSAPrivateKey";
        file.write(tableName.getBytes());
        if (bTestAsymmetricAlgs) {
            testDSAPrivateKey(JCConsts.KeyBuilder_TYPE_DSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_DSA_512, "TYPE DSA PRIVATE LENGTH DSA 512", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testDSAPrivateKey(JCConsts.KeyBuilder_TYPE_DSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_DSA_768, "TYPE DSA PRIVATE LENGTH DSA 768", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testDSAPrivateKey(JCConsts.KeyBuilder_TYPE_DSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_DSA_1024, "TYPE DSA PRIVATE LENGTH DSA 1024", numRepeatWholeOperation, numRepeatWholeMeasurement);
        }
        else {
            String message = "# Measurements excluded for asymmetric algorithms\n";
            file.write(message.getBytes());
        }           
        tableName = "DSAPrivateKey - END\n";
        file.write(tableName.getBytes());
    }
    
    public static void testDSAPublicKey (byte keyType, short keyLength, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws Exception{
        TestSettings testSet = null;
        testSet = PerformanceTesting.prepareTestSettings(Consts.CLASS_KEYBUILDER, Consts.UNUSED, keyType, keyLength, JCConsts.DSAPublicKey_getY,
                Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);
        
        if (!bTestVariableData) {
            testSet.algorithmMethod = JCConsts.DSAPublicKey_getY;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setY()");
            testSet.algorithmMethod = JCConsts.DSAPublicKey_setY;
            double setXTime = PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getY()");
            testSet.algorithmMethod = JCConsts.DSAPublicKey_clearY;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " clearY()", setXTime);
        }
        else {
            String message = "No variable data test for " + info + "\n";
            file.write(message.getBytes());
            System.out.print(message);
        }
    }
    public static void testAllDSAPublicKeys (int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception{
        testAllDSAPublicKeys((short)numRepeatWholeOperation, (short)numRepeatWholeMeasurement);
    }
    public static void testAllDSAPublicKeys (short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception{
        String tableName = "\n\nDSAPublicKey";
        file.write(tableName.getBytes());
        if (bTestAsymmetricAlgs) {
            testDSAPublicKey(JCConsts.KeyBuilder_TYPE_DSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_DSA_512, "TYPE DSA PUBLIC LENGTH DSA 512", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testDSAPublicKey(JCConsts.KeyBuilder_TYPE_DSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_DSA_768, "TYPE DSA PUBLIC LENGTH DSA 768", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testDSAPublicKey(JCConsts.KeyBuilder_TYPE_DSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_DSA_1024, "TYPE DSA PUBLIC LENGTH DSA 1024", numRepeatWholeOperation, numRepeatWholeMeasurement);
        }
        else {
            String message = "# Measurements excluded for asymmetric algorithms\n";
            file.write(message.getBytes());
        }           
        tableName = "DSAPublicKey - END\n";
        file.write(tableName.getBytes());
    }
    
    public static void testECF2MPublicKey (byte keyType, short keyLength, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws Exception{
        TestSettings testSet = null;
        testSet = PerformanceTesting.prepareTestSettings(Consts.CLASS_KEYBUILDER, Consts.UNUSED, keyType, keyLength, JCConsts.ECPublicKey_setW,
                Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);
        
        if (!bTestVariableData) {
            testSet.algorithmMethod = JCConsts.ECPublicKey_setW;
            double setWTime = PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setW()");
            testSet.algorithmMethod = JCConsts.ECPublicKey_getW;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getW()");
            testSet.algorithmMethod = JCConsts.ECPublicKey_clearKey;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " clearW()", setWTime);
        }
        else {
            String message = "No variable data test for " + info + "\n";
            file.write(message.getBytes());
            System.out.print(message);
        }
    }
    public static void testAllECF2MPublicKeys (int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception{
        testAllECF2MPublicKeys((short)numRepeatWholeOperation, (short)numRepeatWholeMeasurement);
    }
    public static void testAllECF2MPublicKeys (short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception{
        String tableName = "\n\nECF2MPublicKey";
        file.write(tableName.getBytes());
        if (bTestAsymmetricAlgs) {
            testECF2MPublicKey(JCConsts.KeyBuilder_TYPE_EC_F2M_PUBLIC, JCConsts.KeyBuilder_LENGTH_EC_F2M_113, "TYPE EC F2M PUBLIC LENGTH EC F2M 113", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECF2MPublicKey(JCConsts.KeyBuilder_TYPE_EC_F2M_PUBLIC, JCConsts.KeyBuilder_LENGTH_EC_F2M_131, "TYPE EC F2M PUBLIC LENGTH EC F2M 131", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECF2MPublicKey(JCConsts.KeyBuilder_TYPE_EC_F2M_PUBLIC, JCConsts.KeyBuilder_LENGTH_EC_F2M_163, "TYPE EC F2M PUBLIC LENGTH EC F2M 163", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECF2MPublicKey(JCConsts.KeyBuilder_TYPE_EC_F2M_PUBLIC, JCConsts.KeyBuilder_LENGTH_EC_F2M_193, "TYPE EC F2M PUBLIC LENGTH EC F2M 193", numRepeatWholeOperation, numRepeatWholeMeasurement);
        }
        else {
            String message = "# Measurements excluded for asymmetric algorithms\n";
            file.write(message.getBytes());
        }           
        tableName = "ECF2MPublicKey - END\n";
        file.write(tableName.getBytes());
    }
    
    public static void testECF2mPrivateKey (byte keyType, short keyLength, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws Exception{
        TestSettings testSet = null;
        testSet = PerformanceTesting.prepareTestSettings(Consts.CLASS_KEYBUILDER, Consts.UNUSED, keyType, keyLength, JCConsts.ECPrivateKey_setS,
                Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);
        
        if (!bTestVariableData) {
            testSet.algorithmMethod = JCConsts.ECPrivateKey_setS;
            double setSTime = PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setS()");
            testSet.algorithmMethod = JCConsts.ECPrivateKey_getS;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getS()");
            testSet.algorithmMethod = JCConsts.ECPrivateKey_clearKey;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " clearS()", setSTime);
        }
        else {
            String message = "No variable data test for " + info + "\n";
            file.write(message.getBytes());
            System.out.print(message);
        }
    }
    public static void testAllECF2MPrivateKeys (int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception{
        testAllECF2MPrivateKeys((short)numRepeatWholeOperation, (short)numRepeatWholeMeasurement);
    }
    public static void testAllECF2MPrivateKeys (short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception{
        String tableName = "\n\nECF2MPrivateKey";
        file.write(tableName.getBytes());
        if (bTestAsymmetricAlgs) {
            testECF2MPublicKey(JCConsts.KeyBuilder_TYPE_EC_F2M_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_F2M_113, "TYPE EC F2M PRIVATE LENGTH EC F2M 113", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECF2MPublicKey(JCConsts.KeyBuilder_TYPE_EC_F2M_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_F2M_131, "TYPE EC F2M PRIVATE LENGTH EC F2M 131", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECF2MPublicKey(JCConsts.KeyBuilder_TYPE_EC_F2M_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_F2M_163, "TYPE EC F2M PRIVATE LENGTH EC F2M 163", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECF2MPublicKey(JCConsts.KeyBuilder_TYPE_EC_F2M_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_F2M_193, "TYPE EC F2M PRIVATE LENGTH EC F2M 193", numRepeatWholeOperation, numRepeatWholeMeasurement);
        }
        else {
            String message = "# Measurements excluded for asymmetric algorithms\n";
            file.write(message.getBytes());
        }           
        tableName = "ECF2MPrivateKey - END\n";
        file.write(tableName.getBytes());
    }
    
    public static void testECFPPublicKey (byte keyType, short keyLength, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws Exception{
        TestSettings testSet = null;
        testSet = PerformanceTesting.prepareTestSettings(Consts.CLASS_KEYBUILDER, Consts.UNUSED, keyType, keyLength, JCConsts.ECPublicKey_setW,
                Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);
        
        if (!bTestVariableData) {
            testSet.algorithmMethod = JCConsts.ECPublicKey_setW;
            double setWTime = PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setW()");
            testSet.algorithmMethod = JCConsts.ECPublicKey_getW;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getW()");
            testSet.algorithmMethod = JCConsts.ECPublicKey_clearKey;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " clearW()", setWTime);
        }
        else {
            String message = "No variable data test for " + info + "\n";
            file.write(message.getBytes());
            System.out.print(message);
        }
    }
    public static void testAllECFPPublicKeys (int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception{
        testAllECFPPublicKeys((short)numRepeatWholeOperation, (short)numRepeatWholeMeasurement);
    }
    public static void testAllECFPPublicKeys (short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception{
        String tableName = "\n\nECFPPublicKey";
        file.write(tableName.getBytes());
        if (bTestAsymmetricAlgs) {
            testECF2MPublicKey(JCConsts.KeyBuilder_TYPE_EC_FP_PUBLIC, JCConsts.KeyBuilder_LENGTH_EC_FP_112, "TYPE EC FP PUBLIC LENGTH EC FP 112", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECF2MPublicKey(JCConsts.KeyBuilder_TYPE_EC_FP_PUBLIC, JCConsts.KeyBuilder_LENGTH_EC_FP_128, "TYPE EC FP PUBLIC LENGTH EC FP 128", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECF2MPublicKey(JCConsts.KeyBuilder_TYPE_EC_FP_PUBLIC, JCConsts.KeyBuilder_LENGTH_EC_FP_160, "TYPE EC FP PUBLIC LENGTH EC FP 160", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECF2MPublicKey(JCConsts.KeyBuilder_TYPE_EC_FP_PUBLIC, JCConsts.KeyBuilder_LENGTH_EC_FP_192, "TYPE EC FP PUBLIC LENGTH EC FP 192", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECF2MPublicKey(JCConsts.KeyBuilder_TYPE_EC_FP_PUBLIC, JCConsts.KeyBuilder_LENGTH_EC_FP_224, "TYPE EC FP PUBLIC LENGTH EC FP 224", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECF2MPublicKey(JCConsts.KeyBuilder_TYPE_EC_FP_PUBLIC, JCConsts.KeyBuilder_LENGTH_EC_FP_256, "TYPE EC FP PUBLIC LENGTH EC FP 256", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECF2MPublicKey(JCConsts.KeyBuilder_TYPE_EC_FP_PUBLIC, JCConsts.KeyBuilder_LENGTH_EC_FP_384, "TYPE EC FP PUBLIC LENGTH EC FP 384", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECF2MPublicKey(JCConsts.KeyBuilder_TYPE_EC_FP_PUBLIC, JCConsts.KeyBuilder_LENGTH_EC_FP_521, "TYPE EC FP PUBLIC LENGTH EC FP 521", numRepeatWholeOperation, numRepeatWholeMeasurement);
        }
        else {
            String message = "# Measurements excluded for asymmetric algorithms\n";
            file.write(message.getBytes());
        }           
        tableName = "ECFPPublicKey - END\n";
        file.write(tableName.getBytes());
    }
    
    public static void testECFPPrivateKey (byte keyType, short keyLength, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws Exception{
        TestSettings testSet = null;
        testSet = PerformanceTesting.prepareTestSettings(Consts.CLASS_KEYBUILDER, Consts.UNUSED, keyType, keyLength, JCConsts.ECPrivateKey_setS,
                Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);
        
        if (!bTestVariableData) {
            testSet.algorithmMethod = JCConsts.ECPrivateKey_setS;
            double setSTime = PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setS()");
            testSet.algorithmMethod = JCConsts.ECPrivateKey_getS;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getS()");
            testSet.algorithmMethod = JCConsts.ECPrivateKey_clearKey;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " clearS()", setSTime);
        }
        else {
            String message = "No variable data test for " + info + "\n";
            file.write(message.getBytes());
            System.out.print(message);
        }
    }
    public static void testAllECFPPrivateKeys (int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception{
        testAllECFPPrivateKeys((short)numRepeatWholeOperation, (short)numRepeatWholeMeasurement);
    }
    public static void testAllECFPPrivateKeys (short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception{
        String tableName = "\n\nECFPPublicKey";
        file.write(tableName.getBytes());
        if (bTestAsymmetricAlgs) {
            testECF2MPublicKey(JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_112, "TYPE EC FP PRIVATE LENGTH EC FP 112", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECF2MPublicKey(JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_128, "TYPE EC FP PRIVATE LENGTH EC FP 128", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECF2MPublicKey(JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_160, "TYPE EC FP PRIVATE LENGTH EC FP 160", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECF2MPublicKey(JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_192, "TYPE EC FP PRIVATE LENGTH EC FP 192", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECF2MPublicKey(JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_224, "TYPE EC FP PRIVATE LENGTH EC FP 224", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECF2MPublicKey(JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_256, "TYPE EC FP PRIVATE LENGTH EC FP 256", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECF2MPublicKey(JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_384, "TYPE EC FP PRIVATE LENGTH EC FP 384", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testECF2MPublicKey(JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, JCConsts.KeyBuilder_LENGTH_EC_FP_521, "TYPE EC FP PRIVATE LENGTH EC FP 521", numRepeatWholeOperation, numRepeatWholeMeasurement);
        }
        else {
            String message = "# Measurements excluded for asymmetric algorithms\n";
            file.write(message.getBytes());
        }           
        tableName = "ECFPPublicKey - END\n";
        file.write(tableName.getBytes());
    }
    
    public static void testHMACKey (byte keyType, short keyLength, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws Exception{
        TestSettings testSet = null;
        testSet = PerformanceTesting.prepareTestSettings(Consts.CLASS_KEYBUILDER, Consts.UNUSED, keyType, keyLength, JCConsts.HMACKey_getKey,
                Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);
        
        if (!bTestVariableData) {
            testSet.algorithmMethod = JCConsts.HMACKey_setKey;
            double setKeyTime = PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setKey()");
            testSet.algorithmMethod = JCConsts.HMACKey_getKey;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getKey()");
            testSet.algorithmMethod = JCConsts.HMACKey_clearKey;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " clearKey()", setKeyTime);
        }
        else {
            String message = "No variable data test for " + info + "\n";
            file.write(message.getBytes());
            System.out.print(message);
        }
    }
    public static void testAllHMACKeys (int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception{
        testAllHMACKeys((short)numRepeatWholeOperation, (short)numRepeatWholeMeasurement);
    }
    public static void testAllHMACKeys (short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception{
        String tableName = "\n\nHMACKey";
        file.write(tableName.getBytes());

        if (bTestSymmetricAlgs) {
            testHMACKey(JCConsts.KeyBuilder_TYPE_HMAC, JCConsts.KeyBuilder_LENGTH_HMAC_SHA_1_BLOCK_64, "TYPE HMAC SHA-1 LENGTH HMAC 64", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testHMACKey(JCConsts.KeyBuilder_TYPE_HMAC, JCConsts.KeyBuilder_LENGTH_HMAC_SHA_256_BLOCK_64, "TYPE HMAC SHA-256 LENGTH HMAC 64", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testHMACKey(JCConsts.KeyBuilder_TYPE_HMAC, JCConsts.KeyBuilder_LENGTH_HMAC_SHA_384_BLOCK_128, "TYPE HMAC SHA-384 LENGTH HMAC 128", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testHMACKey(JCConsts.KeyBuilder_TYPE_HMAC, JCConsts.KeyBuilder_LENGTH_HMAC_SHA_512_BLOCK_128, "TYPE HMAC SHA-512 LENGTH HMAC 128", numRepeatWholeOperation, numRepeatWholeMeasurement);
        }
        else {
            String message = "# Measurements excluded for symmetric algorithms\n";
            file.write(message.getBytes());
        }
        
        tableName = "HMACKey - END\n";
        file.write(tableName.getBytes());
    }
    
    public static void testRSAPrivateCrtKey (byte keyType, short keyLength, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws Exception{
        TestSettings testSet = null;
        testSet = PerformanceTesting.prepareTestSettings(Consts.CLASS_KEYBUILDER, Consts.UNUSED, keyType, keyLength, JCConsts.RSAPrivateCrtKey_setDP1,
                Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);
        
        if (!bTestVariableData) {
            testSet.algorithmMethod = JCConsts.RSAPrivateCrtKey_setDP1;
            double setDP1Time = PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setDP1()");
            testSet.algorithmMethod = JCConsts.RSAPrivateCrtKey_setDQ1;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setDQ1()");
            testSet.algorithmMethod = JCConsts.RSAPrivateCrtKey_setP;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setP()");
            testSet.algorithmMethod = JCConsts.RSAPrivateCrtKey_setPQ;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setPQ()");
            testSet.algorithmMethod = JCConsts.RSAPrivateCrtKey_setQ;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setQ()");

            testSet.algorithmMethod = JCConsts.RSAPrivateCrtKey_getDP1;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getDP1()");
            testSet.algorithmMethod = JCConsts.RSAPrivateCrtKey_getDQ1;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getDQ1()");
            testSet.algorithmMethod = JCConsts.RSAPrivateCrtKey_getP;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getP()");
            testSet.algorithmMethod = JCConsts.RSAPrivateCrtKey_getPQ;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getPQ()");
            testSet.algorithmMethod = JCConsts.RSAPrivateCrtKey_getQ;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getQ()");

            testSet.algorithmMethod = JCConsts.RSAPrivateCrtKey_clearKey;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " clearKey()", setDP1Time);
        }
        else {
            String message = "No variable data test for " + info + "\n";
            file.write(message.getBytes());
            System.out.print(message);
        }
    }
    public static void testAllRSAPrivateCrtKeys (int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception{
        testAllRSAPrivateCrtKeys((short)numRepeatWholeOperation, (short)numRepeatWholeMeasurement);
    }
    public static void testAllRSAPrivateCrtKeys (short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception{
        String tableName = "\n\nRSAPrivateCRTKey";
        file.write(tableName.getBytes());
        if (bTestAsymmetricAlgs) {
            testRSAPrivateCrtKey(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512, "TYPE RSA PRIVATE CRT LENGTH RSA 512", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateCrtKey(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736, "TYPE RSA PRIVATE CRT LENGTH RSA 736", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateCrtKey(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768, "TYPE RSA PRIVATE CRT LENGTH RSA 768", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateCrtKey(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896, "TYPE RSA PRIVATE CRT LENGTH RSA 896", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateCrtKey(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024, "TYPE RSA PRIVATE CRT LENGTH RSA 1024", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateCrtKey(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280, "TYPE RSA PRIVATE CRT LENGTH RSA 1280", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateCrtKey(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536, "TYPE RSA PRIVATE CRT LENGTH RSA 1536", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateCrtKey(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984, "TYPE RSA PRIVATE CRT LENGTH RSA 1984", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateCrtKey(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048, "TYPE RSA PRIVATE CRT LENGTH RSA 2048", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateCrtKey(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072, "TYPE RSA PRIVATE CRT LENGTH RSA 3072", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateCrtKey(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096, "TYPE RSA PRIVATE CRT LENGTH RSA 4096", numRepeatWholeOperation, numRepeatWholeMeasurement);
        }
        else {
            String message = "# Measurements excluded for asymmetric algorithms\n";
            file.write(message.getBytes());
        }   
        tableName = "RSAPrivateCRTKey - END\n";
        file.write(tableName.getBytes());
    }
    
    public static void testRSAPrivateKey (byte keyType, short keyLength, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws Exception{
        TestSettings testSet = null;
        testSet = PerformanceTesting.prepareTestSettings(Consts.CLASS_KEYBUILDER, Consts.UNUSED, keyType, keyLength, JCConsts.RSAPrivateKey_setExponent,
                Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);
        
        if (!bTestVariableData) {
            testSet.algorithmMethod = JCConsts.RSAPrivateKey_setExponent;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setExponent()");
            testSet.algorithmMethod = JCConsts.RSAPrivateKey_setModulus;
            double setModulusTime = PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setModulus()");

            testSet.algorithmMethod = JCConsts.RSAPrivateKey_getExponent;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getExponent()");
            testSet.algorithmMethod = JCConsts.RSAPrivateKey_getModulus;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getModulus()");

            testSet.algorithmMethod = JCConsts.RSAPrivateKey_clearKey;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " clearKey()", setModulusTime);
        }
        else {
            String message = "No variable data test for " + info + "\n";
            file.write(message.getBytes());
            System.out.print(message);
        }
    }
    public static void testAllRSAPrivateKeys (int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception{
        testAllRSAPrivateKeys((short)numRepeatWholeOperation, (short)numRepeatWholeMeasurement);
    }
    public static void testAllRSAPrivateKeys (short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception{
        String tableName = "\n\nRSAPrivateKey";
        file.write(tableName.getBytes());
        if (bTestAsymmetricAlgs) {
            testRSAPrivateKey(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_512, "TYPE RSA PRIVATE LENGTH RSA 512", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateKey(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_736, "TYPE RSA PRIVATE LENGTH RSA 736", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateKey(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_768, "TYPE RSA PRIVATE LENGTH RSA 768", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateKey(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_896, "TYPE RSA PRIVATE LENGTH RSA 896", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateKey(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1024, "TYPE RSA PRIVATE LENGTH RSA 1024", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateKey(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1280, "TYPE RSA PRIVATE LENGTH RSA 1280", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateKey(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1536, "TYPE RSA PRIVATE LENGTH RSA 1536", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateKey(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_1984, "TYPE RSA PRIVATE LENGTH RSA 1984", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateKey(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_2048, "TYPE RSA PRIVATE LENGTH RSA 2048", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateKey(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_3072, "TYPE RSA PRIVATE LENGTH RSA 3072", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPrivateKey(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, JCConsts.KeyBuilder_LENGTH_RSA_4096, "TYPE RSA PRIVATE LENGTH RSA 4096", numRepeatWholeOperation, numRepeatWholeMeasurement);
        }
        else {
            String message = "# Measurements excluded for asymmetric algorithms\n";
            file.write(message.getBytes());
        }   
        tableName = "RSAPrivateKey - END\n";
        file.write(tableName.getBytes());
    }
    
    public static void testRSAPublicKey (byte keyType, short keyLength, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws Exception{
        TestSettings testSet = null;
        testSet = PerformanceTesting.prepareTestSettings(Consts.CLASS_KEYBUILDER, Consts.UNUSED, keyType, keyLength, JCConsts.RSAPublicKey_setExponent,
                Consts.TEST_DATA_LENGTH, Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);
        
        if (!bTestVariableData) {
            testSet.algorithmMethod = JCConsts.RSAPublicKey_setExponent;
            double setExponentTime = PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setExponent()");
            testSet.algorithmMethod = JCConsts.RSAPublicKey_setModulus;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " setModulus()");

            testSet.algorithmMethod = JCConsts.RSAPublicKey_getExponent;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getExponent()");
            testSet.algorithmMethod = JCConsts.RSAPublicKey_getModulus;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " getModulus()");

            testSet.algorithmMethod = JCConsts.RSAPublicKey_clearKey;
            PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, info + " clearKey()", setExponentTime);
        }
        else {
            String message = "No variable data test for " + info + "\n";
            file.write(message.getBytes());
            System.out.print(message);
        }
    }
    public static void testAllRSAPublicKeys (int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception{
        testAllRSAPublicKeys((short)numRepeatWholeOperation, (short)numRepeatWholeMeasurement);
    }
    public static void testAllRSAPublicKeys (short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception{
        String tableName = "\n\nRSAPublicKey";
        file.write(tableName.getBytes());
        
        if (bTestAsymmetricAlgs) {
            testRSAPublicKey(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_512, "TYPE RSA PUBLIC LENGTH RSA 512", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPublicKey(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_736, "TYPE RSA PUBLIC LENGTH RSA 736", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPublicKey(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_768, "TYPE RSA PUBLIC LENGTH RSA 768", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPublicKey(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_896, "TYPE RSA PUBLIC LENGTH RSA 896", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPublicKey(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1024, "TYPE RSA PUBLIC LENGTH RSA 1024", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPublicKey(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1280, "TYPE RSA PUBLIC LENGTH RSA 1280", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPublicKey(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1536, "TYPE RSA PUBLIC LENGTH RSA 1536", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPublicKey(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_1984, "TYPE RSA PUBLIC LENGTH RSA 1984", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPublicKey(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_2048, "TYPE RSA PUBLIC LENGTH RSA 2048", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPublicKey(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_3072, "TYPE RSA PUBLIC LENGTH RSA 3072", numRepeatWholeOperation, numRepeatWholeMeasurement);
            testRSAPublicKey(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, JCConsts.KeyBuilder_LENGTH_RSA_4096, "TYPE RSA PUBLIC LENGTH RSA 4096", numRepeatWholeOperation, numRepeatWholeMeasurement);
        }
        else {
            String message = "# Measurements excluded for asymmetric algorithms\n";
            file.write(message.getBytes());
        }   
        tableName = "RSAPublicKey - END\n";
        file.write(tableName.getBytes());
    }
    
    public static void testAllKeys(int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception {
        testAllKeys((short) numRepeatWholeOperation, (short) numRepeatWholeMeasurement);
    }
    public static void testAllKeys(short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
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
    
    public static Map<Short, Double> testCipher_dataDependency(byte key, short keyLength, byte alg, String info, short initMode, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws Exception {
        Map<Short, Double> results = new TreeMap<>();        
        
      /*  for(short i = 1; i<33; i++)       // length 1-32 - 1 step
            results.put(i,testCipher(key, keyLength, alg,info+" - "+i, numRepeatWholeOperation, numRepeatWholeMeasurement, i));    
        for(short i = 5; i<16; i++)         // length 33-128 - 8 step
            results.put(i,testCipher(key, keyLength, alg,info+" - "+(short)(i*8), numRepeatWholeOperation, numRepeatWholeMeasurement, (short)(i*8)));
      */
        
        for(short i = 1; i<8; i++)          // length 16-112 - 16 step
            results.put((short)(i*16),testCipher(key, keyLength, alg, info+" - "+(short)(i*16), initMode, numRepeatWholeOperation, numRepeatWholeMeasurement, (short)(i*16)));
        
        for(short i = 1; i<5; i++)          // length 128-512 - 128 step
            results.put((short)(i*128),testCipher(key, keyLength, alg,info+" - "+(short)(i*128), initMode, numRepeatWholeOperation, numRepeatWholeMeasurement, (short)(i*128)));
       
        for(Map.Entry<Short, Double> entry : results.entrySet())        //temporary output for graph
            System.out.println("[\"" + entry.getKey() + "\", " + entry.getValue() + "],");
        
        return results;
    } 
}
