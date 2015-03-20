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
import java.util.Arrays;
import java.util.Scanner;
import javax.smartcardio.ResponseAPDU;
import AlgTest.Consts;
import AlgTest.TestSettings;
/**
 *
 * @author lukas.srom
 */
public class PerformanceTesting {
/*
    final static byte CLA_CARD_ALGTEST                  = CLA_CARD_ALGTEST;
    
    final static byte INS_CARD_GETVERSION               = (byte) 0x60;
    final static byte Consts.INS_CARD_RESET                    = (byte) 0x69;
    
    final static byte INS_CARD_PERF_TEST_CLASS_KEY      = (byte) 0x40;
*/    
    
    
    
    // Argument constants for choosing algorithm to test. 
    public static final String TEST_ALL_ALGORITHMS = "ALL_ALGS";
    
    public static final String TEST_EEPROM = "EEPROM";
    public static final String TEST_RAM = "RAM";
    public static final String TEST_EXTENDEDAPDU = "EXTENDEDAPDU";
    public static final String TEST_RSAEXPONENT = "RSAEXPONENT";
    
    public static CardMngr cardManager = new CardMngr();
    
    public StringBuilder value = new StringBuilder();
    public String message = "";
    
    public static final byte mask = 0b01111111;
    public static FileOutputStream file;
    
    
    //static String CLASS_AESKEY_TEST_SETTINGS = "";
    public static final short[] CLASS_AESKEY_TEST_SETTINGS = {Consts.CLASS_KEYBUILDER, (short) 0, Consts.TYPE_AES, };
        
    
    
    /**
     * Calls methods testing card performance.
     * @throws IOException
     * @throws Exception
     */        
    public void testPerformance(String[] args) throws IOException, Exception{
        /* BUGBUG: we need to figure out how to support JCardSim in nice way (copy of class files, directory structure...)
        Class testClassPerformance = AlgTestPerformance.class;
        */
        Class testClassPerformance = null;
        Scanner sc = new Scanner(System.in);
                
        StringBuilder value = new StringBuilder();
        String message = "";
        
        /* Variable 'file' for output data. */
        FileOutputStream file = cardManager.establishConnection(testClassPerformance);
        
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
            System.out.println("Type 1 for yes, 0 for no: ");	
            int rsa_answ = sc.nextInt();

            if (rsa_answ == 1) {
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
            System.out.println("Type 1 for yes, 0 for no: ");	
            int ram_answ = sc.nextInt();

            if (ram_answ == 1){

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
            System.out.println("Type 1 for yes, 0 for no: ");	
            int eeprom_answ = sc.nextInt();

            if (eeprom_answ == 1){
                // Available memory
                value.setLength(0);
                if (testingPerformance.TestAvailableEEPROMMemory(value, file, (byte) 0) == CardMngr.STAT_OK) {}
                else { 
                    message = "\nERROR: Get available EEPROM memory fail\n"; 
                    System.out.println(message); file.write(message.getBytes());
                }
                file.flush();
            }
            
            boolean fast = false; 
            String res = "";
            System.out.println("Do you want to test every class? WARNING: It could take quite a long time (type 'y' for yes or 'n' for no)");
            res = sc.nextLine();
            if(res.equals("y"))
            {
                System.out.println("Do you want to past the long key pair test? WARNING: It can take few hours! (type 'l' for long or type 'f' for fast)");
                res = sc.nextLine();
                if(res.equals("f")) fast = true;
                testAllMessageDigests();
                testAllRandomGenerators();
                testAllCiphers();
                testAllChecksums();                    
                testAllKeyPairs(fast);                    
            }
            else
            {
                System.out.println("Do you want to test class messageDigest? (y/n)");
                res = sc.nextLine();
                if(res.equals("y")) testAllMessageDigests();
                System.out.println("Do you want to test class randomData? (y/n)");
                res = sc.nextLine();
                if(res.equals("y")) testAllRandomGenerators();
                System.out.println("Do you want to test class cipher? (y/n)");
                res = sc.nextLine();
                if(res.equals("y")) testAllCiphers();
                System.out.println("Do you want to test class signature? (y/n)");
                res = sc.nextLine();
                if(res.equals("y")) testAllSignatures();
                System.out.println("Do you want to test class checksum? (y/n)");
                res = sc.nextLine();
                if(res.equals("y")) testAllChecksums();
                System.out.println("Do you want to test class keyPair? (y/n)");
                res = sc.nextLine();
                if(res.equals("y")) 
                {
                    System.out.println("Do you want to past the long key pair test? WARNING: It can take few hours! (type 'l' for long or type 'f' for fast)");
                    res = sc.nextLine();
                    if(res.equals("f")) fast = true;
                    testAllKeyPairs(fast);
                }
            }

        }
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

    
        

    public static TestSettings prepareTestSettings(short classType, short algorithmSpecification, short algorithmType, short algorithmKeyLength, short algorithmMethod, short dataLength1, short dataLength2, short numRepeatWholeOperation, short numRepeatSubOperation, short numRepeatWholeMeasurement) {
        TestSettings    testSet = new TestSettings();
        
        testSet.classType = classType;                              // custom constant signalizing javacard class - e.g., javacardx.crypto.Cipher
        testSet.algorithmSpecification = algorithmSpecification;   // e.g., Cipher.ALG_AES_BLOCK_128_CBC_NOPAD
        testSet.algorithmType = algorithmType;                    // e.g., KeyBuilder.TYPE_AES
        testSet.algorithmKeyLength = algorithmKeyLength;            // e.g., KeyBuilder.LENGTH_AES_128
        testSet.algorithmMethod = algorithmMethod;                   // custom constant signalizing target javacard method e.g., 
        testSet.dataLength1 = dataLength1;                       // e.g., length of data used during measurement (e.g., for update())
        testSet.dataLength2 = dataLength2;                       // e.g., length of data used during measurement (e.g., for doFinal())
        testSet.numRepeatWholeOperation = numRepeatWholeOperation;  // whole operation might be setKey, update, doFinal - numRepeatWholeOperation repeats this whole operation
        testSet.numRepeatSubOperation = numRepeatSubOperation;              // relevant suboperation that should be iterated multiple times - e.g., update()
        testSet.numRepeatWholeMeasurement = numRepeatWholeMeasurement;  // whole operation might be setKey, update, doFinal - numRepeatWholeOperation repeats this whole operation
        
        return testSet;
    }
    public static void perftest_prepareClass(byte appletCLA, byte appletINS, short classType, short algorithmSpecification, short algorithmType, short algorithmKeyLength, short algorithmMethod, short dataLength1, short dataLength2, short numRepeatWholeOperation, short numRepeatSubOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        TestSettings testSet = prepareTestSettings(classType, algorithmSpecification, algorithmType, algorithmKeyLength, algorithmMethod, dataLength1, dataLength2, numRepeatWholeOperation, numRepeatSubOperation, numRepeatWholeMeasurement);
        perftest_prepareClass(appletCLA, appletINS, testSet);
    }
    public static void perftest_prepareClass(byte appletCLA, byte appletINS, TestSettings testSet) throws IOException, Exception {
        // Free previously allocated objects
        boolean succes = cardManager.resetApplet(appletCLA, Consts.INS_CARD_RESET);
        // Prepare new set
        cardManager.PerfTestCommand(appletCLA, appletINS, testSet, Consts.INS_CARD_RESET);
    }
    
    public static double perftest_measure(byte appletCLA, byte appletPrepareINS, byte appletMeasureINS, short classType, short algorithmSpecification, short algorithmType, short algorithmKeyLength, short algorithmMethod, short dataLength1, short dataLength2, short numRepeatWholeOperation, short numRepeatSubOperation, short numRepeatWholeMeasurement, String info) throws IOException, Exception {
        TestSettings testSet = prepareTestSettings(classType, algorithmSpecification, algorithmType, algorithmKeyLength, algorithmMethod, dataLength1, dataLength2, numRepeatWholeOperation, numRepeatSubOperation, numRepeatWholeMeasurement);
        return perftest_measure(appletCLA, appletPrepareINS, appletMeasureINS, testSet, info);
    }
    
    public static double perftest_measure(byte appletCLA, byte appletPrepareINS, byte appletMeasureINS, TestSettings testSet, String info) throws IOException, Exception {
        // Prepare fresh set of objects
        perftest_prepareClass(appletCLA, appletPrepareINS, testSet);        
        
        // Do measurements
        String message = "";
        double sumTimes = 0;
        double avgOverhead = 0;
        double avgOpTime = -1;
        try {            
            String timeStr;
            message += "\n" + info + "\n";
            
            // Measure processing time without actually calling measured operation
            short bkpNumRepeatWholeOperation = testSet.numRepeatWholeOperation;
            testSet.numRepeatWholeOperation = 0;
            for(int i = 0; i < testSet.numRepeatWholeMeasurement;i++) {
                double overheadTime = cardManager.PerfTestCommand(appletCLA, appletMeasureINS, testSet, Consts.INS_CARD_RESET);
                sumTimes += overheadTime;
                timeStr = String.format("%1f", overheadTime);
                message +=  timeStr + " " ;
                System.out.print(timeStr + " ");
            }
            avgOverhead = sumTimes / testSet.numRepeatWholeMeasurement;
            System.out.print("Avg overhead time: " + avgOverhead);
            System.out.println();     
            System.out.println(); message += "\n";
            file.write(message.getBytes());
            message = "";

            
            
            
            // Restore required number of required measurements 
            testSet.numRepeatWholeOperation = bkpNumRepeatWholeOperation;
            
            double time = 0;
            sumTimes = 0;
            for(int i = 0; i < testSet.numRepeatWholeMeasurement;i++) {
                time = cardManager.PerfTestCommand(appletCLA, appletMeasureINS, testSet, Consts.INS_CARD_RESET);
                time -= avgOverhead;
                sumTimes += time;
                timeStr = String.format("%1f", time);
                message +=  timeStr + " " ;
                System.out.print(timeStr + " ");
            }
            System.out.println();     
            
            System.out.println(); message += "\n";
            file.write(message.getBytes());
            message = "";
        
            // Compute average per operation
            int totalIterations = testSet.numRepeatWholeOperation * testSet.numRepeatWholeMeasurement;
            avgOpTime = (totalIterations != 0) ? sumTimes / totalIterations : 0;       
            String messageOpTime = String.format("%f ms/op (%d total iterations, %d total invocations)\n", avgOpTime, totalIterations, totalIterations * testSet.numRepeatSubOperation);
            file.write(messageOpTime.getBytes());
            System.out.println(messageOpTime);  
        }
        catch (CardCommunicationException ex) 
        {
            message += ex.toString();
            System.out.println(ex.toString()); 
        }
        catch (Exception ex) 
        {
            System.out.println("Exception: " + ex);
        }
        System.out.println(); message += "\n";
        file.write(message.getBytes());
        
        return avgOpTime;
    }
    
    
    
        
    //
    // TODO refactor:
    //
    
    
    private static void testKeyPair(byte alg, int length, String info, int round, boolean fast) throws IOException
    {
        int max = 500;
        if (fast) {max = 10; round = 0;}
        double time;
        String timeStr;
        String message = info + " " + round + "\n"; System.out.println(info);
        byte[] cdata = new byte[2];
        cdata[1] = (byte) (length & mask);
        int pom = length >> 7;
        cdata[0] = (byte) (pom & mask);
        try
        {            
            cardManager.BasicTest(Consts.CLA_CARD_ALGTEST,Consts.INS_PERF_PREPARE_KEY_PAIR,alg,(byte)0,cdata,(byte) 2, Consts.INS_CARD_RESET);
            for(int i = 0;i<max;i++)
            {
                time = cardManager.BasicTest(Consts.CLA_CARD_ALGTEST,Consts.INS_PERF_TEST_KEY_PAIR,alg, (byte)0, cdata, (byte) 2, Consts.INS_CARD_RESET);
                timeStr = String.format("%1f", time);
                message +=  timeStr + " " ;
                System.out.print(timeStr + " ");
            }
            System.out.println();      
        }
        catch (CardCommunicationException ex)
        {
            message += ex.toString();
            System.out.println(ex.toString()); 
        }
        catch (Exception ex) 
        {
            System.out.println("Exception: " + ex);
        }
        System.out.println(); message += "\n";
        file.write(message.getBytes());
    }
    
    private static void testAllKeyPairs(boolean fast) throws IOException
    {
        String tableName = "KEY PAIR\n";
        file.write(tableName.getBytes());
        testKeyPair(Consts.ALG_RSA,Consts.LENGTH_RSA_512,"ALG_RSA LENGTH_RSA_512",50,fast);
        testKeyPair(Consts.ALG_RSA,Consts.LENGTH_RSA_736,"ALG_RSA LENGTH_RSA_736",100,fast);
        testKeyPair(Consts.ALG_RSA,Consts.LENGTH_RSA_768,"ALG_RSA LENGTH_RSA_768",100,fast);
        testKeyPair(Consts.ALG_RSA,Consts.LENGTH_RSA_896,"ALG_RSA LENGTH_RSA_896",120,fast);
        testKeyPair(Consts.ALG_RSA,Consts.LENGTH_RSA_1024,"ALG_RSA LENGTH_RSA_1024",150,fast);
        testKeyPair(Consts.ALG_RSA,Consts.LENGTH_RSA_1280,"ALG_RSA LENGTH_RSA_1280",250,fast);
        testKeyPair(Consts.ALG_RSA,Consts.LENGTH_RSA_1536,"ALG_RSA LENGTH_RSA_1536",300,fast);
        testKeyPair(Consts.ALG_RSA,Consts.LENGTH_RSA_1984,"ALG_RSA LENGTH_RSA_1984",500,fast);
        testKeyPair(Consts.ALG_RSA,Consts.LENGTH_RSA_2048,"ALG_RSA LENGTH_RSA_2048",500,fast);
        testKeyPair(Consts.ALG_RSA,Consts.LENGTH_RSA_3072,"ALG_RSA LENGTH_RSA_3072",500,fast);        
        testKeyPair(Consts.ALG_RSA,Consts.LENGTH_RSA_4096,"ALG_RSA LENGTH_RSA_4096",500,fast);        
        testKeyPair(Consts.ALG_RSA_CRT,Consts.LENGTH_RSA_512,"ALG_RSA_CRT LENGTH_RSA_512",50,fast);
        testKeyPair(Consts.ALG_RSA_CRT,Consts.LENGTH_RSA_736,"ALG_RSA_CRT LENGTH_RSA_736",100,fast);
        testKeyPair(Consts.ALG_RSA_CRT,Consts.LENGTH_RSA_768,"ALG_RSA_CRT LENGTH_RSA_768",100,fast);
        testKeyPair(Consts.ALG_RSA_CRT,Consts.LENGTH_RSA_896,"ALG_RSA_CRT LENGTH_RSA_896",120,fast);
        testKeyPair(Consts.ALG_RSA_CRT,Consts.LENGTH_RSA_1024,"ALG_RSA_CRT LENGTH_RSA_1024",150,fast);
        testKeyPair(Consts.ALG_RSA_CRT,Consts.LENGTH_RSA_1280,"ALG_RSA_CRT LENGTH_RSA_1280",250,fast);
        testKeyPair(Consts.ALG_RSA_CRT,Consts.LENGTH_RSA_1536,"ALG_RSA_CRT LENGTH_RSA_1536",300,fast);
        testKeyPair(Consts.ALG_RSA_CRT,Consts.LENGTH_RSA_1984,"ALG_RSA_CRT LENGTH_RSA_1984",500,fast);
        testKeyPair(Consts.ALG_RSA_CRT,Consts.LENGTH_RSA_2048,"ALG_RSA_CRT LENGTH_RSA_2048",500,fast);
        testKeyPair(Consts.ALG_RSA_CRT,Consts.LENGTH_RSA_3072,"ALG_RSA_CRT LENGTH_RSA_3072",500,fast);        
        testKeyPair(Consts.ALG_RSA_CRT,Consts.LENGTH_RSA_4096,"ALG_RSA_CRT LENGTH_RSA_4096",500,fast);        
        testKeyPair(Consts.ALG_DSA,Consts.LENGTH_DSA_512,"ALG_DSA LENGTH_DSA_512",100,fast);
        testKeyPair(Consts.ALG_DSA,Consts.LENGTH_DSA_768,"ALG_DSA LENGTH_DSA_768",100,fast);
        testKeyPair(Consts.ALG_DSA,Consts.LENGTH_DSA_1024,"ALG_DSA LENGTH_DSA_1024",100,fast);        
        testKeyPair(Consts.ALG_EC_F2M,Consts.LENGTH_EC_F2M_113,"ALG_EC_F2M LENGTH_EC_F2M_113",20,fast);
        testKeyPair(Consts.ALG_EC_F2M,Consts.LENGTH_EC_F2M_131,"ALG_EC_F2M LENGTH_EC_F2M_131",20,fast);    
        testKeyPair(Consts.ALG_EC_F2M,Consts.LENGTH_EC_F2M_163,"ALG_EC_F2M LENGTH_EC_F2M_163",20,fast);    
        testKeyPair(Consts.ALG_EC_F2M,Consts.LENGTH_EC_F2M_193,"ALG_EC_F2M LENGTH_EC_F2M_193",20,fast);    
        testKeyPair(Consts.ALG_EC_FP,Consts.LENGTH_EC_FP_112,"ALG_EC_FP LENGTH_EC_FP_112",5,fast);
        testKeyPair(Consts.ALG_EC_FP,Consts.LENGTH_EC_FP_128,"ALG_EC_FP LENGTH_EC_FP_128",5,fast);
        testKeyPair(Consts.ALG_EC_FP,Consts.LENGTH_EC_FP_160,"ALG_EC_FP LENGTH_EC_FP_160",5,fast);
        testKeyPair(Consts.ALG_EC_FP,Consts.LENGTH_EC_FP_192,"ALG_EC_FP LENGTH_EC_FP_192",5,fast);
        testKeyPair(Consts.ALG_EC_FP,Consts.LENGTH_EC_FP_224,"ALG_EC_FP LENGTH_EC_FP_224",5,fast);
        testKeyPair(Consts.ALG_EC_FP,Consts.LENGTH_EC_FP_256,"ALG_EC_FP LENGTH_EC_FP_256",5,fast);
        testKeyPair(Consts.ALG_EC_FP,Consts.LENGTH_EC_FP_384,"ALG_EC_FP LENGTH_EC_FP_384",5,fast);
        testKeyPair(Consts.ALG_EC_FP,Consts.LENGTH_EC_FP_521,"ALG_EC_FP LENGTH_EC_FP_521",5,fast); 
        tableName = "KEY PAIR - END\n";
        file.write(tableName.getBytes());
    }
    private static void testMessageDigest(byte alg, String info) throws IOException
    {
        String message = info; System.out.print(info);
        byte count = 40; 
        int cycle = 5;
        double time1;
        double time2;
        double time;
        String timeStr;
        int maxLength = 150;
        int step = 2;
        message += " " + step + "\n"; System.out.println(" " + step);
        byte[] cdata = new byte[2];
        try
        {
            cardManager.BasicTest(Consts.CLA_CARD_ALGTEST, Consts.INS_PERF_PREPARE_MESSAGE_DIGEST,alg, (byte) 0, cdata, (byte) 0, Consts.INS_CARD_RESET); 
            for (int i = step;i<maxLength;i+=step)
            {            
                cdata[1] = (byte) (i & mask);
                int pom = i >> 7;
                cdata[0] = (byte) (pom & mask);
                double dia = 0;            
                System.out.print("(");
                for (int j = 0;j<cycle;j++)
                {
                    time1 = cardManager.BasicTest(Consts.CLA_CARD_ALGTEST, Consts.INS_PERF_TEST_MESSAGE_DIGEST,alg,(byte)(count * 2), cdata, (byte) 2, Consts.INS_CARD_RESET);
                    time2 = cardManager.BasicTest(Consts.CLA_CARD_ALGTEST, Consts.INS_PERF_TEST_MESSAGE_DIGEST,alg, count, cdata, (byte) 2, Consts.INS_CARD_RESET);
                    time = time1-time2;
                    time = time/count;
                    dia+= time;
                    timeStr = String.format("%1f", time);
                    System.out.print(timeStr + ",");
                }
                System.out.print(")");
                dia = dia/cycle;
                timeStr = String.format("%1f", dia);
                message +=   timeStr + " "; System.out.print(" " + i + "B=" + timeStr);            
            }
        }
        catch(CardCommunicationException ex)
        {
            message += ex.toString();
            System.out.println(ex.toString()); 
        }
        catch(Exception ex)
        {
            System.out.println("Exception: " + ex);
        }
        System.out.println(); message += "\n";        
        file.write(message.getBytes());        
    }
    private static void testAllMessageDigests()throws IOException
    {
        String tableName = "MESSAGE DIGEST\n";
        file.write(tableName.getBytes());
        testMessageDigest(Consts.ALG_SHA,"ALG_SHA");
        testMessageDigest(Consts.ALG_MD5,"ALG_MD5");
        testMessageDigest(Consts.ALG_RIPEMD160,"ALG_RIPEMD160");
        testMessageDigest(Consts.ALG_SHA_256,"ALG_SHA_256");
        testMessageDigest(Consts.ALG_SHA_384,"ALG_SHA_384");
        testMessageDigest(Consts.ALG_SHA_512,"ALG_SHA_512");
        testMessageDigest(Consts.ALG_SHA_224,"ALG_SHA_224");
        tableName = "MESSAGE DIGEST - END\n";
        file.write(tableName.getBytes());
    }
    private static void testRandomGenerator(byte alg, String info) throws IOException
    {
        byte count = 30;
        double time;
        double time1;
        double time2;
        String timeStr;
        int seedCount = 3;
        int seedStep = 30;
        int step = 10;
        int maxLength = 500;
        int cycle = 5;
        byte[] cdata = new byte[4];
        String message = info + " " + step + " " + seedStep + " " + seedCount + "\n"; System.out.println(info);
        try
        {
            cardManager.BasicTest(Consts.CLA_CARD_ALGTEST, Consts.INS_PERF_PREPARE_RANDOM_DATA,alg, (byte)0, cdata, (byte) 0, Consts.INS_CARD_RESET);
            for(int seed = 0;seed<seedCount*seedStep+1;seed+=seedStep)
            {            
                if (seed == 0) System.out.print("NOSEED ");
                else System.out.print("SEED=" + seed + " ");
                for (int length = step;length<maxLength;length+=step)
                {
                    cdata[1] = (byte) (seed & mask);
                    int pom = seed >> 7;
                    cdata[0] = (byte) (pom & mask);
                    cdata[3] = (byte) (length & mask);
                    pom = length >> 7;
                    cdata[2] = (byte) (pom & mask);
                    double dia = 0;
                    System.out.print("(");
                    for (int j = 0;j<cycle;j++)
                    {
                        time1 = cardManager.BasicTest(Consts.CLA_CARD_ALGTEST, Consts.INS_PERF_TEST_RANDOM_DATA,alg, (byte)(count * 2), cdata, (byte) 4, Consts.INS_CARD_RESET);
                        time2 = cardManager.BasicTest(Consts.CLA_CARD_ALGTEST, Consts.INS_PERF_TEST_RANDOM_DATA,alg, count, cdata, (byte) 4, Consts.INS_CARD_RESET);
                        time = time1-time2;
                        time = time/count;
                        dia+= time;
                        timeStr = String.format("%1f", time);
                        System.out.print(timeStr + ",");
                    }
                    System.out.print(")");
                    dia = dia/cycle;
                    timeStr = String.format("%1f", dia);
                    message += timeStr + " ";
                    System.out.print(" " + length + "B=" + timeStr);
                }                           
            }
        }
        catch(CardCommunicationException ex)
        {
            message += ex.toString();
            System.out.println(ex.toString());                 
        }
        catch(Exception ex)
        {
            System.out.println("Exception: " + ex);
        }
        System.out.println(); message += "\n"; 
        file.write(message.getBytes());
    }    
    private static void testAllRandomGenerators() throws IOException
    {
        String tableName = "RANDOM GENERATOR\n";
        file.write(tableName.getBytes());
        testRandomGenerator(Consts.ALG_PSEUDO_RANDOM,"ALG_PSEUDO_RANDOM");
        testRandomGenerator(Consts.ALG_SECURE_RANDOM,"ALG_SECURE_RANDOM");     
        tableName = "RANDOM GENERATOR - END\n";
        file.write(tableName.getBytes());
    }    
    private static void testCipher(byte key, int keyLength, byte alg, String info, int step, int max) throws IOException
    {
        byte count = 30;
        int cycle = 5;
        double time1;
        double time2;
        double time;
        String timeStr;  
        byte[] cdata = new byte[2];
        String message = info + " " + step + "\n";System.out.println(info);
        try
        {
            cdata[1] = (byte) (keyLength & mask);
            int pom = keyLength >> 7;
            cdata[0] = (byte) (pom & mask);
            cardManager.BasicTest(Consts.CLA_CARD_ALGTEST, Consts.INS_PERF_PREPARE_KEY,key, alg, cdata, (byte) 2, Consts.INS_CARD_RESET);
            for(int length = step;length<max;length+=step)
            {                
                double dia = 0;
                cdata[1] = (byte) (length & mask);
                pom = length >> 7;
                cdata[0] = (byte) (pom & mask);
                System.out.print("(");
                for (int j = 0;j<cycle;j++)
                {
                    time1 = cardManager.BasicTest(Consts.CLA_CARD_ALGTEST, Consts.INS_PERF_TEST_CLASS_CIPHER,(byte)alg, (byte)(count * 2), cdata, (byte) 2, Consts.INS_CARD_RESET);
                    time2 = cardManager.BasicTest(Consts.CLA_CARD_ALGTEST, Consts.INS_PERF_TEST_CLASS_CIPHER,(byte)alg, count, cdata, (byte) 2, Consts.INS_CARD_RESET);
                    time = time1-time2;
                    time = time/count;
                    timeStr = String.format("%1f", time);
                    System.out.print(timeStr + ",");
                    dia+=time;
                }
                System.out.print(")");
                dia = dia/cycle;
                timeStr = String.format("%1f", dia);
                message += timeStr +" "; System.out.print(length + "B=" + timeStr +" ");
            }
        }
        catch(CardCommunicationException ex)
        {
            message += ex.toString();
            System.out.println(ex.toString()); 
        }
        catch(Exception ex)
        {
            System.out.println("Exception: " + ex);
        }
        message += "\n"; System.out.println("\n");
        file.write(message.getBytes());
        
    }
    private static void testAllCiphers() throws IOException
    {
        String tableName = "CIPHER\n";
        file.write(tableName.getBytes());
        testCipher(Consts.TYPE_DES, Consts.LENGTH_DES,Consts.ALG_DES_CBC_NOPAD,"TYPE_DES LENGTH_DES ALG_DES_CBC_NOPAD",8,200);
        testCipher(Consts.TYPE_DES, Consts.LENGTH_DES,Consts.ALG_DES_CBC_ISO9797_M1,"TYPE_DES LENGTH_DES ALG_DES_CBC_ISO9797_M1",2,100);
        testCipher(Consts.TYPE_DES, Consts.LENGTH_DES,Consts.ALG_DES_CBC_ISO9797_M2,"TYPE_DES LENGTH_DES ALG_DES_CBC_ISO9797_M2",2,100);
        testCipher(Consts.TYPE_DES, Consts.LENGTH_DES,Consts.ALG_DES_CBC_PKCS5,"TYPE_DES LENGTH_DES ALG_DES_CBC_PKCS5",2,100);
        testCipher(Consts.TYPE_DES, Consts.LENGTH_DES,Consts.ALG_DES_ECB_NOPAD,"TYPE_DES LENGTH_DES ALG_DES_ECB_NOPAD",8,200);
        testCipher(Consts.TYPE_DES, Consts.LENGTH_DES,Consts.ALG_DES_ECB_ISO9797_M1,"TYPE_DES LENGTH_DES ALG_DES_ECB_ISO9797_M1",2,100);
        testCipher(Consts.TYPE_DES, Consts.LENGTH_DES,Consts.ALG_DES_ECB_ISO9797_M2,"TYPE_DES LENGTH_DES ALG_DES_ECB_ISO9797_M2",2,100);
        testCipher(Consts.TYPE_DES, Consts.LENGTH_DES,Consts.ALG_DES_ECB_PKCS5,"TYPE_DES LENGTH_DES ALG_DES_ECB_PKCS5",2,100); 
        testCipher(Consts.TYPE_DES, Consts.LENGTH_DES3_2KEY,Consts.ALG_DES_CBC_NOPAD,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_CBC_NOPAD",8,200);
        testCipher(Consts.TYPE_DES, Consts.LENGTH_DES3_2KEY,Consts.ALG_DES_CBC_ISO9797_M1,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_CBC_ISO9797_M1",2,100);
        testCipher(Consts.TYPE_DES, Consts.LENGTH_DES3_2KEY,Consts.ALG_DES_CBC_ISO9797_M2,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_CBC_ISO9797_M2",2,100);
        testCipher(Consts.TYPE_DES, Consts.LENGTH_DES3_2KEY,Consts.ALG_DES_CBC_PKCS5,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_CBC_PKCS5",2,100);
        testCipher(Consts.TYPE_DES, Consts.LENGTH_DES3_2KEY,Consts.ALG_DES_ECB_NOPAD,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_ECB_NOPAD",8,200);
        testCipher(Consts.TYPE_DES, Consts.LENGTH_DES3_2KEY,Consts.ALG_DES_ECB_ISO9797_M1,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_ECB_ISO9797_M1",2,100);
        testCipher(Consts.TYPE_DES, Consts.LENGTH_DES3_2KEY,Consts.ALG_DES_ECB_ISO9797_M2,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_ECB_ISO9797_M2",2,100);
        testCipher(Consts.TYPE_DES, Consts.LENGTH_DES3_2KEY,Consts.ALG_DES_ECB_PKCS5,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_ECB_PKCS5",2,100);
        testCipher(Consts.TYPE_DES, Consts.LENGTH_DES3_3KEY,Consts.ALG_DES_CBC_NOPAD,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_CBC_NOPAD",8,200);
        testCipher(Consts.TYPE_DES, Consts.LENGTH_DES3_3KEY,Consts.ALG_DES_CBC_ISO9797_M1,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_CBC_ISO9797_M1",2,100);
        testCipher(Consts.TYPE_DES, Consts.LENGTH_DES3_3KEY,Consts.ALG_DES_CBC_ISO9797_M2,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_CBC_ISO9797_M2",2,100);
        testCipher(Consts.TYPE_DES, Consts.LENGTH_DES3_3KEY,Consts.ALG_DES_CBC_PKCS5,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_CBC_PKCS5",2,100);
        testCipher(Consts.TYPE_DES, Consts.LENGTH_DES3_3KEY,Consts.ALG_DES_ECB_NOPAD,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_ECB_NOPAD",8,200);
        testCipher(Consts.TYPE_DES, Consts.LENGTH_DES3_3KEY,Consts.ALG_DES_ECB_ISO9797_M1,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_ECB_ISO9797_M1",2,100);
        testCipher(Consts.TYPE_DES, Consts.LENGTH_DES3_3KEY,Consts.ALG_DES_ECB_ISO9797_M2,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_ECB_ISO9797_M2",2,100);
        testCipher(Consts.TYPE_DES, Consts.LENGTH_DES3_3KEY,Consts.ALG_DES_ECB_PKCS5,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_ECB_PKCS5",2,100); 
        
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_128,Consts.ALG_AES_BLOCK_128_CBC_NOPAD,"TYPE_AES LENGTH_AES_128 ALG_AES_BLOCK_128_CBC_NOPAD",16,200);
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_128,Consts.ALG_AES_BLOCK_128_ECB_NOPAD,"TYPE_AES LENGTH_AES_128 ALG_AES_BLOCK_128_ECB_NOPAD",16,200);
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_128,Consts.ALG_AES_BLOCK_192_CBC_NOPAD,"TYPE_AES LENGTH_AES_128 ALG_AES_BLOCK_192_CBC_NOPAD",24,200);
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_128,Consts.ALG_AES_BLOCK_192_ECB_NOPAD,"TYPE_AES LENGTH_AES_128 ALG_AES_BLOCK_192_ECB_NOPAD",24,200);
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_128,Consts.ALG_AES_BLOCK_256_CBC_NOPAD,"TYPE_AES LENGTH_AES_128 ALG_AES_BLOCK_256_CBC_NOPAD",32,200);
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_128,Consts.ALG_AES_BLOCK_256_ECB_NOPAD,"TYPE_AES LENGTH_AES_128 ALG_AES_BLOCK_256_ECB_NOPAD",32,200);
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_128,Consts.ALG_AES_CBC_ISO9797_M1,"TYPE_AES LENGTH_AES_128 ALG_AES_CBC_ISO9797_M1",2,100);
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_128,Consts.ALG_AES_CBC_ISO9797_M2,"TYPE_AES LENGTH_AES_128 ALG_AES_CBC_ISO9797_M2",2,100);
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_128,Consts.ALG_AES_CBC_PKCS5,"TYPE_AES LENGTH_AES_128 ALG_AES_CBC_PKCS5",2,100);
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_128,Consts.ALG_AES_ECB_ISO9797_M1,"TYPE_AES LENGTH_AES_128 ALG_AES_ECB_ISO9797_M1",2,100);
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_128,Consts.ALG_AES_ECB_ISO9797_M2,"TYPE_AES LENGTH_AES_128 ALG_AES_ECB_ISO9797_M2",2,100);
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_128,Consts.ALG_AES_ECB_PKCS5,"TYPE_AES LENGTH_AES_128 ALG_AES_ECB_PKCS5",2,100); 
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_192,Consts.ALG_AES_BLOCK_128_CBC_NOPAD,"TYPE_AES LENGTH_AES_192 ALG_AES_BLOCK_128_CBC_NOPAD",16,200);
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_192,Consts.ALG_AES_BLOCK_128_ECB_NOPAD,"TYPE_AES LENGTH_AES_192 ALG_AES_BLOCK_128_ECB_NOPAD",16,200);
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_192,Consts.ALG_AES_BLOCK_192_CBC_NOPAD,"TYPE_AES LENGTH_AES_192 ALG_AES_BLOCK_192_CBC_NOPAD",24,200);
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_192,Consts.ALG_AES_BLOCK_192_ECB_NOPAD,"TYPE_AES LENGTH_AES_192 ALG_AES_BLOCK_192_ECB_NOPAD",24,200);
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_192,Consts.ALG_AES_BLOCK_256_CBC_NOPAD,"TYPE_AES LENGTH_AES_192 ALG_AES_BLOCK_256_CBC_NOPAD",32,200);
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_192,Consts.ALG_AES_BLOCK_256_ECB_NOPAD,"TYPE_AES LENGTH_AES_192 ALG_AES_BLOCK_256_ECB_NOPAD",32,200);
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_192,Consts.ALG_AES_CBC_ISO9797_M1,"TYPE_AES LENGTH_AES_192 ALG_AES_CBC_ISO9797_M1",2,100);
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_192,Consts.ALG_AES_CBC_ISO9797_M2,"TYPE_AES LENGTH_AES_192 ALG_AES_CBC_ISO9797_M2",2,100);
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_192,Consts.ALG_AES_CBC_PKCS5,"TYPE_AES LENGTH_AES_192 ALG_AES_CBC_PKCS5",2,100);
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_192,Consts.ALG_AES_ECB_ISO9797_M1,"TYPE_AES LENGTH_AES_192 ALG_AES_ECB_ISO9797_M1",2,100);
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_192,Consts.ALG_AES_ECB_ISO9797_M2,"TYPE_AES LENGTH_AES_192 ALG_AES_ECB_ISO9797_M2",2,100);
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_192,Consts.ALG_AES_ECB_PKCS5,"TYPE_AES LENGTH_AES_192 ALG_AES_ECB_PKCS5",2,100); 
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_256,Consts.ALG_AES_BLOCK_128_CBC_NOPAD,"TYPE_AES LENGTH_AES_256 ALG_AES_BLOCK_128_CBC_NOPAD",16,200);
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_256,Consts.ALG_AES_BLOCK_128_ECB_NOPAD,"TYPE_AES LENGTH_AES_256 ALG_AES_BLOCK_128_ECB_NOPAD",16,200);
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_256,Consts.ALG_AES_BLOCK_192_CBC_NOPAD,"TYPE_AES LENGTH_AES_256 ALG_AES_BLOCK_192_CBC_NOPAD",24,200);
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_256,Consts.ALG_AES_BLOCK_192_ECB_NOPAD,"TYPE_AES LENGTH_AES_256 ALG_AES_BLOCK_192_ECB_NOPAD",24,200);
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_256,Consts.ALG_AES_BLOCK_256_CBC_NOPAD,"TYPE_AES LENGTH_AES_256 ALG_AES_BLOCK_256_CBC_NOPAD",32,200);
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_256,Consts.ALG_AES_BLOCK_256_ECB_NOPAD,"TYPE_AES LENGTH_AES_256 ALG_AES_BLOCK_256_ECB_NOPAD",32,200);
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_256,Consts.ALG_AES_CBC_ISO9797_M1,"TYPE_AES LENGTH_AES_256 ALG_AES_CBC_ISO9797_M1",2,100);
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_256,Consts.ALG_AES_CBC_ISO9797_M2,"TYPE_AES LENGTH_AES_256 ALG_AES_CBC_ISO9797_M2",2,100);
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_256,Consts.ALG_AES_CBC_PKCS5,"TYPE_AES LENGTH_AES_256 ALG_AES_CBC_PKCS5",2,100);
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_256,Consts.ALG_AES_ECB_ISO9797_M1,"TYPE_AES LENGTH_AES_256 ALG_AES_ECB_ISO9797_M1",2,100);
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_256,Consts.ALG_AES_ECB_ISO9797_M2,"TYPE_AES LENGTH_AES_256 ALG_AES_ECB_ISO9797_M2",2,100);
        testCipher(Consts.TYPE_AES, Consts.LENGTH_AES_256,Consts.ALG_AES_ECB_PKCS5,"TYPE_AES LENGTH_AES_256 ALG_AES_ECB_PKCS5",2,100); 
        
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_512,Consts.ALG_RSA_ISO14888,"ALG_RSA LENGTH_RSA_512 ALG_RSA_ISO14888",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_512,Consts.ALG_RSA_ISO9796,"ALG_RSA LENGTH_RSA_512 ALG_RSA_ISO9796",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_512,Consts.ALG_RSA_NOPAD,"ALG_RSA LENGTH_RSA_512 ALG_RSA_NOPAD",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_512,Consts.ALG_RSA_PKCS1,"ALG_RSA LENGTH_RSA_512 ALG_RSA_PKCS1",2,54);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_512,Consts.ALG_RSA_PKCS1_OAEP,"ALG_RSA LENGTH_RSA_512 ALG_RSA_PKCS1_OAEP",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_736,Consts.ALG_RSA_ISO14888,"ALG_RSA LENGTH_RSA_736 ALG_RSA_ISO14888",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_736,Consts.ALG_RSA_ISO9796,"ALG_RSA LENGTH_RSA_736 ALG_RSA_ISO9796",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_736,Consts.ALG_RSA_NOPAD,"ALG_RSA LENGTH_RSA_736 ALG_RSA_NOPAD",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_736,Consts.ALG_RSA_PKCS1,"ALG_RSA LENGTH_RSA_736 ALG_RSA_PKCS1",2,82);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_736,Consts.ALG_RSA_PKCS1_OAEP,"ALG_RSA LENGTH_RSA_736 ALG_RSA_PKCS1_OAEP",2,100);
        
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_768,Consts.ALG_RSA_ISO14888,"ALG_RSA LENGTH_RSA_768 ALG_RSA_ISO14888",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_768,Consts.ALG_RSA_ISO9796,"ALG_RSA LENGTH_RSA_768 ALG_RSA_ISO9796",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_768,Consts.ALG_RSA_NOPAD,"ALG_RSA LENGTH_RSA_768 ALG_RSA_NOPAD",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_768,Consts.ALG_RSA_PKCS1,"ALG_RSA LENGTH_RSA_768 ALG_RSA_PKCS1",2,86);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_768,Consts.ALG_RSA_PKCS1_OAEP,"ALG_RSA LENGTH_RSA_768 ALG_RSA_PKCS1_OAEP",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_896,Consts.ALG_RSA_ISO14888,"ALG_RSA LENGTH_RSA_896 ALG_RSA_ISO14888",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_896,Consts.ALG_RSA_ISO9796,"ALG_RSA LENGTH_RSA_896 ALG_RSA_ISO9796",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_896,Consts.ALG_RSA_NOPAD,"ALG_RSA LENGTH_RSA_896 ALG_RSA_NOPAD",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_896,Consts.ALG_RSA_PKCS1,"ALG_RSA LENGTH_RSA_896 ALG_RSA_PKCS1",2,102);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_896,Consts.ALG_RSA_PKCS1_OAEP,"ALG_RSA LENGTH_RSA_896 ALG_RSA_PKCS1_OAEP",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_1024,Consts.ALG_RSA_ISO14888,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_ISO14888",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_1024,Consts.ALG_RSA_ISO9796,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_ISO9796",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_1024,Consts.ALG_RSA_NOPAD,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_NOPAD",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_1024,Consts.ALG_RSA_PKCS1,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_PKCS1",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_1024,Consts.ALG_RSA_PKCS1_OAEP,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_PKCS1_OAEP",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_1280,Consts.ALG_RSA_ISO14888,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_ISO14888",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_1280,Consts.ALG_RSA_ISO9796,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_ISO9796",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_1280,Consts.ALG_RSA_NOPAD,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_NOPAD",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_1280,Consts.ALG_RSA_PKCS1,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_PKCS1",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_1280,Consts.ALG_RSA_PKCS1_OAEP,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_PKCS1_OAEP",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_1536,Consts.ALG_RSA_ISO14888,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_ISO14888",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_1536,Consts.ALG_RSA_ISO9796,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_ISO9796",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_1536,Consts.ALG_RSA_NOPAD,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_NOPAD",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_1536,Consts.ALG_RSA_PKCS1,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_PKCS1",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_1536,Consts.ALG_RSA_PKCS1_OAEP,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_PKCS1_OAEP",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_1984,Consts.ALG_RSA_ISO14888,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_ISO14888",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_1984,Consts.ALG_RSA_ISO9796,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_ISO9796",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_1984,Consts.ALG_RSA_NOPAD,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_NOPAD",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_1984,Consts.ALG_RSA_PKCS1,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_PKCS1",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_1984,Consts.ALG_RSA_PKCS1_OAEP,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_PKCS1_OAEP",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_2048,Consts.ALG_RSA_ISO14888,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_ISO14888",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_2048,Consts.ALG_RSA_ISO9796,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_ISO9796",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_2048,Consts.ALG_RSA_NOPAD,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_NOPAD",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_2048,Consts.ALG_RSA_PKCS1,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_PKCS1",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_2048,Consts.ALG_RSA_PKCS1_OAEP,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_PKCS1_OAEP",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_3072,Consts.ALG_RSA_ISO14888,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_ISO14888",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_3072,Consts.ALG_RSA_ISO9796,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_ISO9796",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_3072,Consts.ALG_RSA_NOPAD,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_NOPAD",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_3072,Consts.ALG_RSA_PKCS1,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_PKCS1",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_3072,Consts.ALG_RSA_PKCS1_OAEP,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_PKCS1_OAEP",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_4096,Consts.ALG_RSA_ISO14888,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_ISO14888",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_4096,Consts.ALG_RSA_ISO9796,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_ISO9796",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_4096,Consts.ALG_RSA_NOPAD,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_NOPAD",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_4096,Consts.ALG_RSA_PKCS1,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_PKCS1",2,100);
        testCipher(Consts.ALG_RSA, Consts.LENGTH_RSA_4096,Consts.ALG_RSA_PKCS1_OAEP,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_PKCS1_OAEP",2,100); 
        tableName = "CIPHER - END\n";
        file.write(tableName.getBytes());
    }
    private static void testSignature(byte key, int keyLength, byte alg, String info, int step, int max) throws IOException
    {
        String message;
        byte count = 30;
        int cycle = 5;
        double time1;
        double time2;
        double time;
        String timeStr; 
        byte[] cdata = new byte[2];
        message = info + " " + step + "\n";System.out.println(info);
        cdata[1] = (byte) (keyLength & mask);
        int pom = keyLength >> 7;
        cdata[0] = (byte) (pom & mask);
        try
        {
            cardManager.BasicTest(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_SIGNATURE,key, alg, cdata, (byte) 2, Consts.INS_CARD_RESET);
            for(int length = step;length<max;length+=step)
            {           
                double dia = 0;
                cdata[1] = (byte) (length & mask);
                pom = length >> 7;
                cdata[0] = (byte) (pom & mask);
                System.out.print("(");
                for (int j = 0;j<cycle;j++)
                {
                    time1 = cardManager.BasicTest(Consts.CLA_CARD_ALGTEST, Consts.INS_PERF_TEST_SIGNATURE,(byte)0, (byte)(count * 2), cdata, (byte) 2, Consts.INS_CARD_RESET);
                    time2 = cardManager.BasicTest(Consts.CLA_CARD_ALGTEST, Consts.INS_PERF_TEST_SIGNATURE,(byte)0, count, cdata, (byte) 2, Consts.INS_CARD_RESET);
                    time = time1-time2;
                    time = time/count;
                    dia+=time;
                    timeStr = String.format("%1f", time);
                    System.out.print(timeStr + ",");
                }
                System.out.print(")");
                dia = dia/cycle;
                timeStr = String.format("%1f", dia);
                message += timeStr + " "; System.out.print(length + "B=" + timeStr +" ");
            } 
        }
        catch(CardCommunicationException ex)
        {
            message += ex.toString();
            System.out.println(ex.toString()); 
        }
        catch(Exception ex)
        {
            System.out.println("Exception: " + ex);
        }
        message += "\n"; System.out.println("\n");
        file.write(message.getBytes());
    }
    private static void testAllSignatures() throws IOException
    {
        String tableName = "SIGNATURE\n";
        file.write(tableName.getBytes());
        testSignature(Consts.TYPE_AES, Consts.LENGTH_AES_128,Consts.ALG_AES_MAC_128_NOPAD,"TYPE_AES LENGTH_AES_128 ALG_AES_MAC_128_NOPAD",16,200);
        testSignature(Consts.TYPE_AES, Consts.LENGTH_AES_192,Consts.ALG_AES_MAC_128_NOPAD,"TYPE_AES LENGTH_AES_192 ALG_AES_MAC_128_NOPAD",16,200);
        testSignature(Consts.TYPE_AES, Consts.LENGTH_AES_256,Consts.ALG_AES_MAC_128_NOPAD,"TYPE_AES LENGTH_AES_256 ALG_AES_MAC_128_NOPAD",16,200);
        testSignature(Consts.TYPE_DES, Consts.LENGTH_DES3_2KEY,Consts.ALG_DES_MAC4_ISO9797_1_M2_ALG3,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_MAC4_ISO9797_1_M2_ALG3",2,100);
        testSignature(Consts.TYPE_DES, Consts.LENGTH_DES,Consts.ALG_DES_MAC4_ISO9797_M1,"TYPE_DES LENGTH_DES ALG_DES_MAC4_ISO9797_M1",2,100);
        testSignature(Consts.TYPE_DES, Consts.LENGTH_DES3_2KEY,Consts.ALG_DES_MAC4_ISO9797_M1,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_MAC4_ISO9797_M1",2,100);
        testSignature(Consts.TYPE_DES, Consts.LENGTH_DES3_3KEY,Consts.ALG_DES_MAC4_ISO9797_M1,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_MAC4_ISO9797_M1",2,100);
        testSignature(Consts.TYPE_DES, Consts.LENGTH_DES,Consts.ALG_DES_MAC4_ISO9797_M2,"TYPE_DES LENGTH_DES ALG_DES_MAC4_ISO9797_M2",2,100);
        testSignature(Consts.TYPE_DES, Consts.LENGTH_DES3_2KEY,Consts.ALG_DES_MAC4_ISO9797_M2,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_MAC4_ISO9797_M2",2,100);
        testSignature(Consts.TYPE_DES, Consts.LENGTH_DES3_3KEY,Consts.ALG_DES_MAC4_ISO9797_M2,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_MAC4_ISO9797_M2",2,100);
        testSignature(Consts.TYPE_DES, Consts.LENGTH_DES,Consts.ALG_DES_MAC4_NOPAD,"TYPE_DES LENGTH_DES ALG_DES_MAC4_NOPAD",8,100);
        testSignature(Consts.TYPE_DES, Consts.LENGTH_DES3_2KEY,Consts.ALG_DES_MAC4_NOPAD,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_MAC4_NOPAD",8,100);
        testSignature(Consts.TYPE_DES, Consts.LENGTH_DES3_3KEY,Consts.ALG_DES_MAC4_NOPAD,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_MAC4_NOPAD",8,100);
        testSignature(Consts.TYPE_DES, Consts.LENGTH_DES,Consts.ALG_DES_MAC4_PKCS5,"TYPE_DES LENGTH_DES ALG_DES_MAC4_PKCS5",2,100);
        testSignature(Consts.TYPE_DES, Consts.LENGTH_DES3_2KEY,Consts.ALG_DES_MAC4_PKCS5,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_MAC4_PKCS5",2,100);
        testSignature(Consts.TYPE_DES, Consts.LENGTH_DES3_3KEY,Consts.ALG_DES_MAC4_PKCS5,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_MAC4_PKCS5",2,100);
        testSignature(Consts.TYPE_DES, Consts.LENGTH_DES3_2KEY,Consts.ALG_DES_MAC8_ISO9797_1_M2_ALG3,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_MAC8_ISO9797_1_M2_ALG3",2,100);
        testSignature(Consts.TYPE_DES, Consts.LENGTH_DES,Consts.ALG_DES_MAC8_ISO9797_M1,"TYPE_DES LENGTH_DES ALG_DES_MAC8_ISO9797_M1",2,100);
        testSignature(Consts.TYPE_DES, Consts.LENGTH_DES3_2KEY,Consts.ALG_DES_MAC8_ISO9797_M1,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_MAC8_ISO9797_M1",2,100);
        testSignature(Consts.TYPE_DES, Consts.LENGTH_DES3_3KEY,Consts.ALG_DES_MAC8_ISO9797_M1,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_MAC8_ISO9797_M1",2,100);
        testSignature(Consts.TYPE_DES, Consts.LENGTH_DES,Consts.ALG_DES_MAC8_ISO9797_M2,"TYPE_DES LENGTH_DES ALG_DES_MAC8_ISO9797_M2",2,100);
        testSignature(Consts.TYPE_DES, Consts.LENGTH_DES3_2KEY,Consts.ALG_DES_MAC8_ISO9797_M2,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_MAC8_ISO9797_M2",2,100);
        testSignature(Consts.TYPE_DES, Consts.LENGTH_DES3_3KEY,Consts.ALG_DES_MAC8_ISO9797_M2,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_MAC8_ISO9797_M2",2,100);
        testSignature(Consts.TYPE_DES, Consts.LENGTH_DES,Consts.ALG_DES_MAC8_NOPAD,"TYPE_DES LENGTH_DES ALG_DES_MAC8_NOPAD",8,100);
        testSignature(Consts.TYPE_DES, Consts.LENGTH_DES3_2KEY,Consts.ALG_DES_MAC8_NOPAD,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_MAC8_NOPAD",8,100);
        testSignature(Consts.TYPE_DES, Consts.LENGTH_DES3_3KEY,Consts.ALG_DES_MAC8_NOPAD,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_MAC8_NOPAD",8,100);
        testSignature(Consts.TYPE_DES, Consts.LENGTH_DES,Consts.ALG_DES_MAC8_PKCS5,"TYPE_DES LENGTH_DES ALG_DES_MAC8_PKCS5",2,100);
        testSignature(Consts.TYPE_DES, Consts.LENGTH_DES3_2KEY,Consts.ALG_DES_MAC8_PKCS5,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_MAC8_PKCS5",2,100);
        testSignature(Consts.TYPE_DES, Consts.LENGTH_DES3_3KEY,Consts.ALG_DES_MAC8_PKCS5,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_MAC8_PKCS5",2,100);
        testSignature(Consts.MY_DSA, Consts.LENGTH_DSA_512,Consts.ALG_DSA_SHA,"MY_DSA LENGTH_DSA_512 ALG_DSA_SHA",2,100);
        testSignature(Consts.MY_DSA, Consts.LENGTH_DSA_768,Consts.ALG_DSA_SHA,"MY_DSA LENGTH_DSA_768 ALG_DSA_SHA",2,100);
        testSignature(Consts.MY_DSA, Consts.LENGTH_DSA_1024,Consts.ALG_DSA_SHA,"MY_DSA LENGTH_DSA_1024 ALG_DSA_SHA",2,100);
        
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_512,Consts.ALG_RSA_MD5_PKCS1,"ALG_RSA LENGTH_RSA_512 ALG_RSA_MD5_PKCS1",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_736,Consts.ALG_RSA_MD5_PKCS1,"ALG_RSA LENGTH_RSA_736 ALG_RSA_MD5_PKCS1",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_768,Consts.ALG_RSA_MD5_PKCS1,"ALG_RSA LENGTH_RSA_768 ALG_RSA_MD5_PKCS1",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_896,Consts.ALG_RSA_MD5_PKCS1,"ALG_RSA LENGTH_RSA_896 ALG_RSA_MD5_PKCS1",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1024,Consts.ALG_RSA_MD5_PKCS1,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_MD5_PKCS1",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1280,Consts.ALG_RSA_MD5_PKCS1,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_MD5_PKCS1",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1536,Consts.ALG_RSA_MD5_PKCS1,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_MD5_PKCS1",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1984,Consts.ALG_RSA_MD5_PKCS1,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_MD5_PKCS1",2,100);  
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_2048,Consts.ALG_RSA_MD5_PKCS1,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_MD5_PKCS1",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_3072,Consts.ALG_RSA_MD5_PKCS1,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_MD5_PKCS1",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_4096,Consts.ALG_RSA_MD5_PKCS1,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_MD5_PKCS1",2,100);
        
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_512,Consts.ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA LENGTH_RSA_512 ALG_RSA_MD5_PKCS1_PSS",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_736,Consts.ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA LENGTH_RSA_736 ALG_RSA_MD5_PKCS1_PSS",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_768,Consts.ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA LENGTH_RSA_768 ALG_RSA_MD5_PKCS1_PSS",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_896,Consts.ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA LENGTH_RSA_896 ALG_RSA_MD5_PKCS1_PSS",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1024,Consts.ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_MD5_PKCS1_PSS",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1280,Consts.ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_MD5_PKCS1_PSS",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1536,Consts.ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_MD5_PKCS1_PSS",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1984,Consts.ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_MD5_PKCS1_PSS",2,100);  
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_2048,Consts.ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_MD5_PKCS1_PSS",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_3072,Consts.ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_MD5_PKCS1_PSS",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_4096,Consts.ALG_RSA_MD5_PKCS1_PSS,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_MD5_PKCS1_PSS",2,100);
        
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_512,Consts.ALG_RSA_MD5_RFC2409,"ALG_RSA LENGTH_RSA_512 ALG_RSA_MD5_RFC2409",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_736,Consts.ALG_RSA_MD5_RFC2409,"ALG_RSA LENGTH_RSA_736 ALG_RSA_MD5_RFC2409",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_768,Consts.ALG_RSA_MD5_RFC2409,"ALG_RSA LENGTH_RSA_768 ALG_RSA_MD5_RFC2409",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_896,Consts.ALG_RSA_MD5_RFC2409,"ALG_RSA LENGTH_RSA_896 ALG_RSA_MD5_RFC2409",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1024,Consts.ALG_RSA_MD5_RFC2409,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_MD5_RFC2409",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1280,Consts.ALG_RSA_MD5_RFC2409,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_MD5_RFC2409",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1536,Consts.ALG_RSA_MD5_RFC2409,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_MD5_RFC2409",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1984,Consts.ALG_RSA_MD5_RFC2409,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_MD5_RFC2409",2,100);  
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_2048,Consts.ALG_RSA_MD5_RFC2409,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_MD5_RFC2409",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_3072,Consts.ALG_RSA_MD5_RFC2409,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_MD5_RFC2409",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_4096,Consts.ALG_RSA_MD5_RFC2409,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_MD5_RFC2409",2,100);
        
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_512,Consts.ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA LENGTH_RSA_512 ALG_RSA_RIPEMD160_ISO9796",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_736,Consts.ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA LENGTH_RSA_736 ALG_RSA_RIPEMD160_ISO9796",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_768,Consts.ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA LENGTH_RSA_768 ALG_RSA_RIPEMD160_ISO9796",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_896,Consts.ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA LENGTH_RSA_896 ALG_RSA_RIPEMD160_ISO9796",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1024,Consts.ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_RIPEMD160_ISO9796",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1280,Consts.ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_RIPEMD160_ISO9796",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1536,Consts.ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_RIPEMD160_ISO9796",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1984,Consts.ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_RIPEMD160_ISO9796",2,100);  
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_2048,Consts.ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_RIPEMD160_ISO9796",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_3072,Consts.ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_RIPEMD160_ISO9796",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_4096,Consts.ALG_RSA_RIPEMD160_ISO9796,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_RIPEMD160_ISO9796",2,100);
        
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_512,Consts.ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA LENGTH_RSA_512 ALG_RSA_RIPEMD160_PKCS1",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_736,Consts.ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA LENGTH_RSA_736 ALG_RSA_RIPEMD160_PKCS1",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_768,Consts.ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA LENGTH_RSA_768 ALG_RSA_RIPEMD160_PKCS1",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_896,Consts.ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA LENGTH_RSA_896 ALG_RSA_RIPEMD160_PKCS1",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1024,Consts.ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_RIPEMD160_PKCS1",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1280,Consts.ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_RIPEMD160_PKCS1",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1536,Consts.ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_RIPEMD160_PKCS1",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1984,Consts.ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_RIPEMD160_PKCS1",2,100);  
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_2048,Consts.ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_RIPEMD160_PKCS1",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_3072,Consts.ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_RIPEMD160_PKCS1",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_4096,Consts.ALG_RSA_RIPEMD160_PKCS1,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_RIPEMD160_PKCS1",2,100);
        
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_512,Consts.ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA LENGTH_RSA_512 ALG_RSA_RIPEMD160_PKCS1_PSS",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_736,Consts.ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA LENGTH_RSA_736 ALG_RSA_RIPEMD160_PKCS1_PSS",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_768,Consts.ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA LENGTH_RSA_768 ALG_RSA_RIPEMD160_PKCS1_PSS",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_896,Consts.ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA LENGTH_RSA_896 ALG_RSA_RIPEMD160_PKCS1_PSS",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1024,Consts.ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_RIPEMD160_PKCS1_PSS",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1280,Consts.ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_RIPEMD160_PKCS1_PSS",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1536,Consts.ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_RIPEMD160_PKCS1_PSS",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1984,Consts.ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_RIPEMD160_PKCS1_PSS",2,100);  
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_2048,Consts.ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_RIPEMD160_PKCS1_PSS",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_3072,Consts.ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_RIPEMD160_PKCS1_PSS",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_4096,Consts.ALG_RSA_RIPEMD160_PKCS1_PSS,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_RIPEMD160_PKCS1_PSS",2,100);
        
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_512,Consts.ALG_RSA_SHA_ISO9796,"ALG_RSA LENGTH_RSA_512 ALG_RSA_SHA_ISO9796",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_736,Consts.ALG_RSA_SHA_ISO9796,"ALG_RSA LENGTH_RSA_736 ALG_RSA_SHA_ISO9796",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_768,Consts.ALG_RSA_SHA_ISO9796,"ALG_RSA LENGTH_RSA_768 ALG_RSA_SHA_ISO9796",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_896,Consts.ALG_RSA_SHA_ISO9796,"ALG_RSA LENGTH_RSA_896 ALG_RSA_SHA_ISO9796",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1024,Consts.ALG_RSA_SHA_ISO9796,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_SHA_ISO9796",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1280,Consts.ALG_RSA_SHA_ISO9796,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_SHA_ISO9796",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1536,Consts.ALG_RSA_SHA_ISO9796,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_SHA_ISO9796",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1984,Consts.ALG_RSA_SHA_ISO9796,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_SHA_ISO9796",2,100);  
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_2048,Consts.ALG_RSA_SHA_ISO9796,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_SHA_ISO9796",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_3072,Consts.ALG_RSA_SHA_ISO9796,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_SHA_ISO9796",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_4096,Consts.ALG_RSA_SHA_ISO9796,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_SHA_ISO9796",2,100);
        
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_512,Consts.ALG_RSA_SHA_PKCS1,"ALG_RSA LENGTH_RSA_512 ALG_RSA_SHA_PKCS1",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_736,Consts.ALG_RSA_SHA_PKCS1,"ALG_RSA LENGTH_RSA_736 ALG_RSA_SHA_PKCS1",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_768,Consts.ALG_RSA_SHA_PKCS1,"ALG_RSA LENGTH_RSA_768 ALG_RSA_SHA_PKCS1",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_896,Consts.ALG_RSA_SHA_PKCS1,"ALG_RSA LENGTH_RSA_896 ALG_RSA_SHA_PKCS1",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1024,Consts.ALG_RSA_SHA_PKCS1,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_SHA_PKCS1",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1280,Consts.ALG_RSA_SHA_PKCS1,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_SHA_PKCS1",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1536,Consts.ALG_RSA_SHA_PKCS1,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_SHA_PKCS1",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1984,Consts.ALG_RSA_SHA_PKCS1,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_SHA_PKCS1",2,100);  
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_2048,Consts.ALG_RSA_SHA_PKCS1,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_SHA_PKCS1",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_3072,Consts.ALG_RSA_SHA_PKCS1,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_SHA_PKCS1",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_4096,Consts.ALG_RSA_SHA_PKCS1,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_SHA_PKCS1",2,100);
        
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_512,Consts.ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA LENGTH_RSA_512 ALG_RSA_SHA_PKCS1_PSS",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_736,Consts.ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA LENGTH_RSA_736 ALG_RSA_SHA_PKCS1_PSS",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_768,Consts.ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA LENGTH_RSA_768 ALG_RSA_SHA_PKCS1_PSS",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_896,Consts.ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA LENGTH_RSA_896 ALG_RSA_SHA_PKCS1_PSS",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1024,Consts.ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_SHA_PKCS1_PSS",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1280,Consts.ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_SHA_PKCS1_PSS",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1536,Consts.ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_SHA_PKCS1_PSS",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1984,Consts.ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_SHA_PKCS1_PSS",2,100);  
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_2048,Consts.ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_SHA_PKCS1_PSS",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_3072,Consts.ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_SHA_PKCS1_PSS",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_4096,Consts.ALG_RSA_SHA_PKCS1_PSS,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_SHA_PKCS1_PSS",2,100);
        
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_512,Consts.ALG_RSA_SHA_RFC2409,"ALG_RSA LENGTH_RSA_512 ALG_RSA_SHA_RFC2409",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_736,Consts.ALG_RSA_SHA_RFC2409,"ALG_RSA LENGTH_RSA_736 ALG_RSA_SHA_RFC2409",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_768,Consts.ALG_RSA_SHA_RFC2409,"ALG_RSA LENGTH_RSA_768 ALG_RSA_SHA_RFC2409",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_896,Consts.ALG_RSA_SHA_RFC2409,"ALG_RSA LENGTH_RSA_896 ALG_RSA_SHA_RFC2409",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1024,Consts.ALG_RSA_SHA_RFC2409,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_SHA_RFC2409",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1280,Consts.ALG_RSA_SHA_RFC2409,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_SHA_RFC2409",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1536,Consts.ALG_RSA_SHA_RFC2409,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_SHA_RFC2409",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_1984,Consts.ALG_RSA_SHA_RFC2409,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_SHA_RFC2409",2,100);  
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_2048,Consts.ALG_RSA_SHA_RFC2409,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_SHA_RFC2409",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_3072,Consts.ALG_RSA_SHA_RFC2409,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_SHA_RFC2409",2,100);
        testSignature(Consts.ALG_RSA, Consts.LENGTH_RSA_4096,Consts.ALG_RSA_SHA_RFC2409,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_SHA_RFC2409",2,100);
               
        tableName = "SIGNATURE - END\n";
        file.write(tableName.getBytes());        
    }    
    private static void testChecksum(byte alg, String info, int step) throws IOException
    {
        String message  = info + " " + step + "\n"; System.out.println(info);
        byte count = 60;
        double time;
        double time1;
        double time2;
        String timeStr;
        int maxLength = 300;
        int cycle = 10;
        byte[] cdata = new byte[2];
        for (int i = step;i<maxLength;i+=step)
        {            
            cdata[1] = (byte) (i & mask);
            int pom = i >> 7;
            cdata[0] = (byte) (pom & mask);
            double dia = 0;
            try
            {
                System.out.print("(");
                for (int j = 0;j<cycle;j++)
                {
                    time1 = cardManager.BasicTest(Consts.CLA_CARD_ALGTEST, Consts.INS_PERF_TEST_CHECKSUM,alg, (byte)(count * 2), cdata, (byte) 2, Consts.INS_CARD_RESET);
                    time2 = cardManager.BasicTest(Consts.CLA_CARD_ALGTEST, Consts.INS_PERF_TEST_CHECKSUM,alg, count, cdata, (byte) 2, Consts.INS_CARD_RESET);
                    time = time1-time2;
                    time = time/count;
                    dia+=time;
                    timeStr = String.format("%1f", time);
                    System.out.print(timeStr + ",");
                }
                System.out.print(")");
                dia = dia/cycle;
                timeStr = String.format("%1f", dia);
                message += timeStr+" ";System.out.print(" " + i + "B=" + timeStr);
            }
            catch(CardCommunicationException ex)
            {
                message += ex.toString();
                System.out.println(ex.toString()); 
            }
            catch(Exception ex)
            {
                System.out.println("Exception: " + ex);
            }            
        }
        System.out.println(); message += "\n";
        file.write(message.getBytes());
    }
    private static void testAllChecksums() throws IOException
    {
        String tableName = "CHECKSUM\n";
        file.write(tableName.getBytes());
        testChecksum(Consts.ALG_ISO3309_CRC16,"ALG_ISO3309_CRC16",2);
        testChecksum(Consts.ALG_ISO3309_CRC32,"ALG_ISO3309_CRC32",2);
        tableName = "CHECKSUM - END\n";
        file.write(tableName.getBytes());
    }    
}
