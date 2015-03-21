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
import AlgTest.JCConsts;
import AlgTest.TestSettings;
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
    
    public static CardMngr cardManager = new CardMngr();
    
    public StringBuilder value = new StringBuilder();
    public String message = "";
    
    public static final byte mask = 0b01111111;
    public static FileOutputStream file;
    
    
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
*/        
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
                testAllMessageDigests(Consts.NUM_REPEAT_WHOLE_OPERATION, Consts.NUM_REPEAT_WHOLE_MEASUREMENT);
                testAllRandomGenerators(Consts.NUM_REPEAT_WHOLE_OPERATION, Consts.NUM_REPEAT_WHOLE_MEASUREMENT);
                testAllCiphers(Consts.NUM_REPEAT_WHOLE_OPERATION, Consts.NUM_REPEAT_WHOLE_MEASUREMENT);
                testAllSignatures(Consts.NUM_REPEAT_WHOLE_OPERATION, Consts.NUM_REPEAT_WHOLE_MEASUREMENT);
                testAllChecksums(Consts.NUM_REPEAT_WHOLE_OPERATION, Consts.NUM_REPEAT_WHOLE_MEASUREMENT);     

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

        }
    //}
    
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

    
        

    public static TestSettings prepareTestSettings(short classType, short algorithmSpecification, short keyType, short keyLength, short algorithmMethod, short dataLength1, short dataLength2, short numRepeatWholeOperation, short numRepeatSubOperation, short numRepeatWholeMeasurement) {
        TestSettings    testSet = new TestSettings();
        
        testSet.classType = classType;                              // custom constant signalizing javacard class - e.g., custom constant for javacardx.crypto.Cipher
        testSet.algorithmSpecification = algorithmSpecification;    // e.g., Cipher.ALG_AES_BLOCK_128_CBC_NOPAD
        testSet.keyType = keyType;                            // e.g., KeyBuilder.TYPE_AES
        testSet.keyLength = keyLength;                     // e.g., KeyBuilder.LENGTH_AES_128
        testSet.algorithmMethod = algorithmMethod;                  // custom constant signalizing target javacard method e.g., 
        testSet.dataLength1 = dataLength1;                          // e.g., length of data used during measurement (e.g., for update())
        testSet.dataLength2 = dataLength2;                          // e.g., length of data used during measurement (e.g., for doFinal())
        testSet.numRepeatWholeOperation = numRepeatWholeOperation;  // whole operation might be setKey, update, doFinal - numRepeatWholeOperation repeats this whole operation
        testSet.numRepeatSubOperation = numRepeatSubOperation;      // relevant suboperation that should be iterated multiple times - e.g., update()
        testSet.numRepeatWholeMeasurement = numRepeatWholeMeasurement;  // whole operation might be setKey, update, doFinal - numRepeatWholeOperation repeats this whole operation
        
        return testSet;
    }
    public static void perftest_prepareClass(byte appletCLA, byte appletINS, short classType, short algorithmSpecification, short keyType, short keyLength, short algorithmMethod, short dataLength1, short dataLength2, short numRepeatWholeOperation, short numRepeatSubOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        TestSettings testSet = prepareTestSettings(classType, algorithmSpecification, keyType, keyLength, algorithmMethod, dataLength1, dataLength2, numRepeatWholeOperation, numRepeatSubOperation, numRepeatWholeMeasurement);
        perftest_prepareClass(appletCLA, appletINS, testSet);
    }
    public static void perftest_prepareClass(byte appletCLA, byte appletINS, TestSettings testSet) throws IOException, Exception {
        // Free previously allocated objects
        boolean succes = cardManager.resetApplet(appletCLA, Consts.INS_CARD_RESET);
        // Prepare new set
        cardManager.PerfTestCommand(appletCLA, appletINS, testSet, Consts.INS_CARD_RESET);
    }
    
    public static double perftest_measure(byte appletCLA, byte appletPrepareINS, byte appletMeasureINS, short classType, short algorithmSpecification, short keyType, short keyLength, short algorithmMethod, short dataLength1, short dataLength2, short numRepeatWholeOperation, short numRepeatSubOperation, short numRepeatWholeMeasurement, String info) throws IOException, Exception {
        TestSettings testSet = prepareTestSettings(classType, algorithmSpecification, keyType, keyLength, algorithmMethod, dataLength1, dataLength2, numRepeatWholeOperation, numRepeatSubOperation, numRepeatWholeMeasurement);
        return perftest_measure(appletCLA, appletPrepareINS, appletMeasureINS, testSet, info);
    }
    
    public static double perftest_measure(byte appletCLA, byte appletPrepareINS, byte appletMeasureINS, TestSettings testSet, String info) throws IOException, Exception {
        double avgOpTime = -1;
        String message = "";
        message += "\n" + info + "\n";
        System.out.print(message);
        file.write(message.getBytes());
        message = "";

        try {
            //            
            // Prepare fresh set of objects
            //
            perftest_prepareClass(appletCLA, appletPrepareINS, testSet);        
    

            double sumTimes = 0;
            double avgOverhead = 0;
            String timeStr;
            
            //
            // Measure processing time without actually calling measured operation
            //
            short bkpNumRepeatWholeOperation = testSet.numRepeatWholeOperation;
            testSet.numRepeatWholeOperation = 0;
            message +=  "debug overhead:";
            for(int i = 0; i < testSet.numRepeatWholeMeasurement;i++) {
                cardManager.resetApplet(appletCLA, Consts.INS_CARD_RESET);
                double overheadTime = cardManager.PerfTestCommand(appletCLA, appletMeasureINS, testSet, Consts.INS_CARD_RESET);
                sumTimes += overheadTime;
                timeStr = String.format(" %1f", overheadTime);
                message +=  timeStr + " " ;
                System.out.print(timeStr + " ");
            }
            avgOverhead = sumTimes / testSet.numRepeatWholeMeasurement;
            message += "\ndebug avg overhead time: " + avgOverhead;
            System.out.print("\ndebug avg overhead time: " + avgOverhead);
            System.out.println();     
            System.out.println(); message += "\n";
            file.write(message.getBytes());
            message = "";

            
            //
            // Measure operations
            //
            // Restore required number of required measurements 
            testSet.numRepeatWholeOperation = bkpNumRepeatWholeOperation;
            
            double time = 0;
            sumTimes = 0;
            for(int i = 0; i < testSet.numRepeatWholeMeasurement;i++) {
                cardManager.resetApplet(appletCLA, Consts.INS_CARD_RESET);
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
            message += ex.toString();
            System.out.println(ex.toString()); 
        }
        System.out.println(); message += "\n";
        System.out.println(message); 
        file.write(message.getBytes());
        
        return avgOpTime;
    }
    
    
    
        

    public static void testKeyPair(byte alg, short length, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        TestSettings testSet = prepareTestSettings(Consts.CLASS_KEYPAIR, Consts.UNUSED, Consts.UNUSED, length, JCConsts.KeyPair_genKeyPair, 
                Consts.UNUSED, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);      

        testSet.keyClass = alg;
        testSet.algorithmMethod = JCConsts.KeyPair_genKeyPair;
        PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEYPAIR, Consts.INS_PERF_TEST_CLASS_KEYPAIR, testSet, info + " KeyPair_genKeyPair()");
    }
    
    public static void testAllKeyPairs(int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception {
        testAllKeyPairs((short) numRepeatWholeOperation, (short) numRepeatWholeMeasurement);
    }
    public static void testAllKeyPairs(short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        String tableName = "\n\nKEY PAIR\n";
        file.write(tableName.getBytes());
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
        tableName = "KEY PAIR - END\n";
        file.write(tableName.getBytes());
    }
    
    public static void testMessageDigest(byte alg, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        TestSettings testSet = PerformanceTesting.prepareTestSettings(Consts.CLASS_MESSAGEDIGEST, alg, Consts.UNUSED, Consts.UNUSED, 
                JCConsts.MessageDigest_update, Consts.TEST_DATA_LENGTH, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);       

        testSet.algorithmMethod = JCConsts.MessageDigest_update;
        PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_MESSAGEDIGEST, Consts.INS_PERF_TEST_CLASS_MESSAGEDIGEST, testSet, info + " MessageDigest_update()");
        testSet.algorithmMethod = JCConsts.MessageDigest_doFinal;
        PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_MESSAGEDIGEST, Consts.INS_PERF_TEST_CLASS_MESSAGEDIGEST, testSet, info + " MessageDigest_doFinal()");
        testSet.algorithmMethod = JCConsts.MessageDigest_reset;
        PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_MESSAGEDIGEST, Consts.INS_PERF_TEST_CLASS_MESSAGEDIGEST, testSet, info + " MessageDigest_reset()");
    }   
    

    public static void testAllMessageDigests(int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception {
        testAllMessageDigests((short) numRepeatWholeOperation, (short) numRepeatWholeMeasurement);
    }
    public static void testAllMessageDigests(short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        String tableName = "\n\nMESSAGE DIGEST\n";
        file.write(tableName.getBytes());
        testMessageDigest(JCConsts.MessageDigest_ALG_SHA,"ALG_SHA", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testMessageDigest(JCConsts.MessageDigest_ALG_MD5,"ALG_MD5", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testMessageDigest(JCConsts.MessageDigest_ALG_RIPEMD160,"ALG_RIPEMD160", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testMessageDigest(JCConsts.MessageDigest_ALG_SHA_256,"ALG_SHA_256", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testMessageDigest(JCConsts.MessageDigest_ALG_SHA_384,"ALG_SHA_384", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testMessageDigest(JCConsts.MessageDigest_ALG_SHA_512,"ALG_SHA_512", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testMessageDigest(JCConsts.MessageDigest_ALG_SHA_224,"ALG_SHA_224", numRepeatWholeOperation, numRepeatWholeMeasurement);
        tableName = "MESSAGE DIGEST - END\n";
        file.write(tableName.getBytes());
    }

    public static void testRandomGenerator(byte alg, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        TestSettings testSet = PerformanceTesting.prepareTestSettings(Consts.CLASS_RANDOMDATA, alg, Consts.UNUSED, Consts.UNUSED, JCConsts.RandomData_generateData, 
                Consts.TEST_DATA_LENGTH, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);      

        testSet.algorithmMethod = JCConsts.RandomData_generateData;
        PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_RANDOMDATA, Consts.INS_PERF_TEST_CLASS_RANDOMDATA, testSet, info + " RandomData_generateData()");
        testSet.algorithmMethod = JCConsts.RandomData_setSeed;
        PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_RANDOMDATA, Consts.INS_PERF_TEST_CLASS_RANDOMDATA, testSet, info + " RandomData_setSeed()");        
    }    
    public static void testAllRandomGenerators(int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception {
        testAllRandomGenerators((short) numRepeatWholeOperation, (short) numRepeatWholeMeasurement);
    }
    public static void testAllRandomGenerators(short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        String tableName = "\n\nRANDOM GENERATOR\n";
        file.write(tableName.getBytes());
        testRandomGenerator(JCConsts.RandomData_ALG_PSEUDO_RANDOM,"ALG_PSEUDO_RANDOM", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testRandomGenerator(JCConsts.RandomData_ALG_SECURE_RANDOM,"ALG_SECURE_RANDOM", numRepeatWholeOperation, numRepeatWholeMeasurement);     
        tableName = "RANDOM GENERATOR - END\n";
        file.write(tableName.getBytes());
    }    

    public static void testCipher(byte key, short keyLength, byte alg, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        TestSettings testSet = PerformanceTesting.prepareTestSettings(Consts.CLASS_CIPHER, alg, key, keyLength, JCConsts.Cipher_update, 
                Consts.TEST_DATA_LENGTH, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);      

        testSet.algorithmMethod = JCConsts.Cipher_update;
        PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CIPHER, Consts.INS_PERF_TEST_CLASS_CIPHER, testSet, info + " Cipher_update()");
        testSet.algorithmMethod = JCConsts.Cipher_doFinal;
        PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CIPHER, Consts.INS_PERF_TEST_CLASS_CIPHER, testSet, info + " Cipher_doFinal()");
        testSet.algorithmMethod = JCConsts.Cipher_init;
        PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CIPHER, Consts.INS_PERF_TEST_CLASS_CIPHER, testSet, info + " Cipher_init()");
        
    }
    
    public static void testAllCiphers(int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception {
        testAllCiphers((short) numRepeatWholeOperation, (short) numRepeatWholeMeasurement);
    }
    public static void testAllCiphers(short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        String tableName = "\n\nCIPHER\n";
        file.write(tableName.getBytes());
        testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES,JCConsts.Cipher_ALG_DES_CBC_NOPAD,"TYPE_DES LENGTH_DES ALG_DES_CBC_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES,JCConsts.Cipher_ALG_DES_CBC_ISO9797_M1,"TYPE_DES LENGTH_DES ALG_DES_CBC_ISO9797_M1", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES,JCConsts.Cipher_ALG_DES_CBC_ISO9797_M2,"TYPE_DES LENGTH_DES ALG_DES_CBC_ISO9797_M2", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES,JCConsts.Cipher_ALG_DES_CBC_PKCS5,"TYPE_DES LENGTH_DES ALG_DES_CBC_PKCS5", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES,JCConsts.Cipher_ALG_DES_ECB_NOPAD,"TYPE_DES LENGTH_DES ALG_DES_ECB_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES,JCConsts.Cipher_ALG_DES_ECB_ISO9797_M1,"TYPE_DES LENGTH_DES ALG_DES_ECB_ISO9797_M1", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES,JCConsts.Cipher_ALG_DES_ECB_ISO9797_M2,"TYPE_DES LENGTH_DES ALG_DES_ECB_ISO9797_M2", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES,JCConsts.Cipher_ALG_DES_ECB_PKCS5,"TYPE_DES LENGTH_DES ALG_DES_ECB_PKCS5", numRepeatWholeOperation, numRepeatWholeMeasurement); 
        testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_2KEY,JCConsts.Cipher_ALG_DES_CBC_NOPAD,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_CBC_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_2KEY,JCConsts.Cipher_ALG_DES_CBC_ISO9797_M1,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_CBC_ISO9797_M1", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_2KEY,JCConsts.Cipher_ALG_DES_CBC_ISO9797_M2,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_CBC_ISO9797_M2", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_2KEY,JCConsts.Cipher_ALG_DES_CBC_PKCS5,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_CBC_PKCS5", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_2KEY,JCConsts.Cipher_ALG_DES_ECB_NOPAD,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_ECB_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_2KEY,JCConsts.Cipher_ALG_DES_ECB_ISO9797_M1,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_ECB_ISO9797_M1", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_2KEY,JCConsts.Cipher_ALG_DES_ECB_ISO9797_M2,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_ECB_ISO9797_M2", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_2KEY,JCConsts.Cipher_ALG_DES_ECB_PKCS5,"TYPE_DES LENGTH_DES3_2KEY ALG_DES_ECB_PKCS5", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_3KEY,JCConsts.Cipher_ALG_DES_CBC_NOPAD,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_CBC_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_3KEY,JCConsts.Cipher_ALG_DES_CBC_ISO9797_M1,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_CBC_ISO9797_M1", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_3KEY,JCConsts.Cipher_ALG_DES_CBC_ISO9797_M2,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_CBC_ISO9797_M2", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_3KEY,JCConsts.Cipher_ALG_DES_CBC_PKCS5,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_CBC_PKCS5", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_3KEY,JCConsts.Cipher_ALG_DES_ECB_NOPAD,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_ECB_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_3KEY,JCConsts.Cipher_ALG_DES_ECB_ISO9797_M1,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_ECB_ISO9797_M1", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_3KEY,JCConsts.Cipher_ALG_DES_ECB_ISO9797_M2,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_ECB_ISO9797_M2", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_DES, JCConsts.KeyBuilder_LENGTH_DES3_3KEY,JCConsts.Cipher_ALG_DES_ECB_PKCS5,"TYPE_DES LENGTH_DES3_3KEY ALG_DES_ECB_PKCS5", numRepeatWholeOperation, numRepeatWholeMeasurement); 
        
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_128,JCConsts.Cipher_ALG_AES_BLOCK_128_CBC_NOPAD,"TYPE_AES LENGTH_AES_128 ALG_AES_BLOCK_128_CBC_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_128,JCConsts.Cipher_ALG_AES_BLOCK_128_ECB_NOPAD,"TYPE_AES LENGTH_AES_128 ALG_AES_BLOCK_128_ECB_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_128,JCConsts.Cipher_ALG_AES_BLOCK_192_CBC_NOPAD,"TYPE_AES LENGTH_AES_128 ALG_AES_BLOCK_192_CBC_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_128,JCConsts.Cipher_ALG_AES_BLOCK_192_ECB_NOPAD,"TYPE_AES LENGTH_AES_128 ALG_AES_BLOCK_192_ECB_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_128,JCConsts.Cipher_ALG_AES_BLOCK_256_CBC_NOPAD,"TYPE_AES LENGTH_AES_128 ALG_AES_BLOCK_256_CBC_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_128,JCConsts.Cipher_ALG_AES_BLOCK_256_ECB_NOPAD,"TYPE_AES LENGTH_AES_128 ALG_AES_BLOCK_256_ECB_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_128,JCConsts.Cipher_ALG_AES_CBC_ISO9797_M1,"TYPE_AES LENGTH_AES_128 ALG_AES_CBC_ISO9797_M1", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_128,JCConsts.Cipher_ALG_AES_CBC_ISO9797_M2,"TYPE_AES LENGTH_AES_128 ALG_AES_CBC_ISO9797_M2", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_128,JCConsts.Cipher_ALG_AES_CBC_PKCS5,"TYPE_AES LENGTH_AES_128 ALG_AES_CBC_PKCS5", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_128,JCConsts.Cipher_ALG_AES_ECB_ISO9797_M1,"TYPE_AES LENGTH_AES_128 ALG_AES_ECB_ISO9797_M1", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_128,JCConsts.Cipher_ALG_AES_ECB_ISO9797_M2,"TYPE_AES LENGTH_AES_128 ALG_AES_ECB_ISO9797_M2", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_128,JCConsts.Cipher_ALG_AES_ECB_PKCS5,"TYPE_AES LENGTH_AES_128 ALG_AES_ECB_PKCS5", numRepeatWholeOperation, numRepeatWholeMeasurement); 
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_192,JCConsts.Cipher_ALG_AES_BLOCK_128_CBC_NOPAD,"TYPE_AES LENGTH_AES_192 ALG_AES_BLOCK_128_CBC_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_192,JCConsts.Cipher_ALG_AES_BLOCK_128_ECB_NOPAD,"TYPE_AES LENGTH_AES_192 ALG_AES_BLOCK_128_ECB_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_192,JCConsts.Cipher_ALG_AES_BLOCK_192_CBC_NOPAD,"TYPE_AES LENGTH_AES_192 ALG_AES_BLOCK_192_CBC_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_192,JCConsts.Cipher_ALG_AES_BLOCK_192_ECB_NOPAD,"TYPE_AES LENGTH_AES_192 ALG_AES_BLOCK_192_ECB_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_192,JCConsts.Cipher_ALG_AES_BLOCK_256_CBC_NOPAD,"TYPE_AES LENGTH_AES_192 ALG_AES_BLOCK_256_CBC_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_192,JCConsts.Cipher_ALG_AES_BLOCK_256_ECB_NOPAD,"TYPE_AES LENGTH_AES_192 ALG_AES_BLOCK_256_ECB_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_192,JCConsts.Cipher_ALG_AES_CBC_ISO9797_M1,"TYPE_AES LENGTH_AES_192 ALG_AES_CBC_ISO9797_M1", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_192,JCConsts.Cipher_ALG_AES_CBC_ISO9797_M2,"TYPE_AES LENGTH_AES_192 ALG_AES_CBC_ISO9797_M2", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_192,JCConsts.Cipher_ALG_AES_CBC_PKCS5,"TYPE_AES LENGTH_AES_192 ALG_AES_CBC_PKCS5", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_192,JCConsts.Cipher_ALG_AES_ECB_ISO9797_M1,"TYPE_AES LENGTH_AES_192 ALG_AES_ECB_ISO9797_M1", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_192,JCConsts.Cipher_ALG_AES_ECB_ISO9797_M2,"TYPE_AES LENGTH_AES_192 ALG_AES_ECB_ISO9797_M2", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_192,JCConsts.Cipher_ALG_AES_ECB_PKCS5,"TYPE_AES LENGTH_AES_192 ALG_AES_ECB_PKCS5", numRepeatWholeOperation, numRepeatWholeMeasurement); 
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Cipher_ALG_AES_BLOCK_128_CBC_NOPAD,"TYPE_AES LENGTH_AES_256 ALG_AES_BLOCK_128_CBC_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Cipher_ALG_AES_BLOCK_128_ECB_NOPAD,"TYPE_AES LENGTH_AES_256 ALG_AES_BLOCK_128_ECB_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Cipher_ALG_AES_BLOCK_192_CBC_NOPAD,"TYPE_AES LENGTH_AES_256 ALG_AES_BLOCK_192_CBC_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Cipher_ALG_AES_BLOCK_192_ECB_NOPAD,"TYPE_AES LENGTH_AES_256 ALG_AES_BLOCK_192_ECB_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Cipher_ALG_AES_BLOCK_256_CBC_NOPAD,"TYPE_AES LENGTH_AES_256 ALG_AES_BLOCK_256_CBC_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Cipher_ALG_AES_BLOCK_256_ECB_NOPAD,"TYPE_AES LENGTH_AES_256 ALG_AES_BLOCK_256_ECB_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Cipher_ALG_AES_CBC_ISO9797_M1,"TYPE_AES LENGTH_AES_256 ALG_AES_CBC_ISO9797_M1", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Cipher_ALG_AES_CBC_ISO9797_M2,"TYPE_AES LENGTH_AES_256 ALG_AES_CBC_ISO9797_M2", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Cipher_ALG_AES_CBC_PKCS5,"TYPE_AES LENGTH_AES_256 ALG_AES_CBC_PKCS5", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Cipher_ALG_AES_ECB_ISO9797_M1,"TYPE_AES LENGTH_AES_256 ALG_AES_ECB_ISO9797_M1", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Cipher_ALG_AES_ECB_ISO9797_M2,"TYPE_AES LENGTH_AES_256 ALG_AES_ECB_ISO9797_M2", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyBuilder_TYPE_AES, JCConsts.KeyBuilder_LENGTH_AES_256,JCConsts.Cipher_ALG_AES_ECB_PKCS5,"TYPE_AES LENGTH_AES_256 ALG_AES_ECB_PKCS5", numRepeatWholeOperation, numRepeatWholeMeasurement); 
        // ALG_RSA
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Cipher_ALG_RSA_ISO14888,"ALG_RSA LENGTH_RSA_512 ALG_RSA_ISO14888", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Cipher_ALG_RSA_ISO9796,"ALG_RSA LENGTH_RSA_512 ALG_RSA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Cipher_ALG_RSA_NOPAD,"ALG_RSA LENGTH_RSA_512 ALG_RSA_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Cipher_ALG_RSA_PKCS1,"ALG_RSA LENGTH_RSA_512 ALG_RSA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"ALG_RSA LENGTH_RSA_512 ALG_RSA_PKCS1_OAEP", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Cipher_ALG_RSA_ISO14888,"ALG_RSA LENGTH_RSA_736 ALG_RSA_ISO14888", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Cipher_ALG_RSA_ISO9796,"ALG_RSA LENGTH_RSA_736 ALG_RSA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Cipher_ALG_RSA_NOPAD,"ALG_RSA LENGTH_RSA_736 ALG_RSA_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Cipher_ALG_RSA_PKCS1,"ALG_RSA LENGTH_RSA_736 ALG_RSA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"ALG_RSA LENGTH_RSA_736 ALG_RSA_PKCS1_OAEP", numRepeatWholeOperation, numRepeatWholeMeasurement);
        
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Cipher_ALG_RSA_ISO14888,"ALG_RSA LENGTH_RSA_768 ALG_RSA_ISO14888", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Cipher_ALG_RSA_ISO9796,"ALG_RSA LENGTH_RSA_768 ALG_RSA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Cipher_ALG_RSA_NOPAD,"ALG_RSA LENGTH_RSA_768 ALG_RSA_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Cipher_ALG_RSA_PKCS1,"ALG_RSA LENGTH_RSA_768 ALG_RSA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"ALG_RSA LENGTH_RSA_768 ALG_RSA_PKCS1_OAEP", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Cipher_ALG_RSA_ISO14888,"ALG_RSA LENGTH_RSA_896 ALG_RSA_ISO14888", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Cipher_ALG_RSA_ISO9796,"ALG_RSA LENGTH_RSA_896 ALG_RSA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Cipher_ALG_RSA_NOPAD,"ALG_RSA LENGTH_RSA_896 ALG_RSA_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Cipher_ALG_RSA_PKCS1,"ALG_RSA LENGTH_RSA_896 ALG_RSA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"ALG_RSA LENGTH_RSA_896 ALG_RSA_PKCS1_OAEP", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Cipher_ALG_RSA_ISO14888,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_ISO14888", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Cipher_ALG_RSA_ISO9796,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Cipher_ALG_RSA_NOPAD,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Cipher_ALG_RSA_PKCS1,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"ALG_RSA LENGTH_RSA_1024 ALG_RSA_PKCS1_OAEP", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Cipher_ALG_RSA_ISO14888,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_ISO14888", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Cipher_ALG_RSA_ISO9796,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Cipher_ALG_RSA_NOPAD,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Cipher_ALG_RSA_PKCS1,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"ALG_RSA LENGTH_RSA_1280 ALG_RSA_PKCS1_OAEP", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Cipher_ALG_RSA_ISO14888,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_ISO14888", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Cipher_ALG_RSA_ISO9796,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Cipher_ALG_RSA_NOPAD,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Cipher_ALG_RSA_PKCS1,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"ALG_RSA LENGTH_RSA_1536 ALG_RSA_PKCS1_OAEP", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Cipher_ALG_RSA_ISO14888,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_ISO14888", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Cipher_ALG_RSA_ISO9796,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Cipher_ALG_RSA_NOPAD,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Cipher_ALG_RSA_PKCS1,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"ALG_RSA LENGTH_RSA_1984 ALG_RSA_PKCS1_OAEP", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Cipher_ALG_RSA_ISO14888,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_ISO14888", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Cipher_ALG_RSA_ISO9796,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Cipher_ALG_RSA_NOPAD,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Cipher_ALG_RSA_PKCS1,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"ALG_RSA LENGTH_RSA_2048 ALG_RSA_PKCS1_OAEP", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Cipher_ALG_RSA_ISO14888,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_ISO14888", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Cipher_ALG_RSA_ISO9796,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Cipher_ALG_RSA_NOPAD,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Cipher_ALG_RSA_PKCS1,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"ALG_RSA LENGTH_RSA_3072 ALG_RSA_PKCS1_OAEP", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Cipher_ALG_RSA_ISO14888,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_ISO14888", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Cipher_ALG_RSA_ISO9796,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Cipher_ALG_RSA_NOPAD,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Cipher_ALG_RSA_PKCS1,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"ALG_RSA LENGTH_RSA_4096 ALG_RSA_PKCS1_OAEP", numRepeatWholeOperation, numRepeatWholeMeasurement); 
        // ALG_RSA_CRT
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Cipher_ALG_RSA_ISO14888,"ALG_RSA_CRT LENGTH_RSA_512 ALG_RSA_ISO14888", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Cipher_ALG_RSA_ISO9796,"ALG_RSA_CRT LENGTH_RSA_512 ALG_RSA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Cipher_ALG_RSA_NOPAD,"ALG_RSA_CRT LENGTH_RSA_512 ALG_RSA_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Cipher_ALG_RSA_PKCS1,"ALG_RSA_CRT LENGTH_RSA_512 ALG_RSA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_512,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"ALG_RSA_CRT LENGTH_RSA_512 ALG_RSA_PKCS1_OAEP", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Cipher_ALG_RSA_ISO14888,"ALG_RSA_CRT LENGTH_RSA_736 ALG_RSA_ISO14888", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Cipher_ALG_RSA_ISO9796,"ALG_RSA_CRT LENGTH_RSA_736 ALG_RSA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Cipher_ALG_RSA_NOPAD,"ALG_RSA_CRT LENGTH_RSA_736 ALG_RSA_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Cipher_ALG_RSA_PKCS1,"ALG_RSA_CRT LENGTH_RSA_736 ALG_RSA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_736,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"ALG_RSA_CRT LENGTH_RSA_736 ALG_RSA_PKCS1_OAEP", numRepeatWholeOperation, numRepeatWholeMeasurement);
        
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Cipher_ALG_RSA_ISO14888,"ALG_RSA_CRT LENGTH_RSA_768 ALG_RSA_ISO14888", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Cipher_ALG_RSA_ISO9796,"ALG_RSA_CRT LENGTH_RSA_768 ALG_RSA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Cipher_ALG_RSA_NOPAD,"ALG_RSA_CRT LENGTH_RSA_768 ALG_RSA_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Cipher_ALG_RSA_PKCS1,"ALG_RSA_CRT LENGTH_RSA_768 ALG_RSA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_768,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"ALG_RSA_CRT LENGTH_RSA_768 ALG_RSA_PKCS1_OAEP", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Cipher_ALG_RSA_ISO14888,"ALG_RSA_CRT LENGTH_RSA_896 ALG_RSA_ISO14888", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Cipher_ALG_RSA_ISO9796,"ALG_RSA_CRT LENGTH_RSA_896 ALG_RSA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Cipher_ALG_RSA_NOPAD,"ALG_RSA_CRT LENGTH_RSA_896 ALG_RSA_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Cipher_ALG_RSA_PKCS1,"ALG_RSA_CRT LENGTH_RSA_896 ALG_RSA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_896,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"ALG_RSA_CRT LENGTH_RSA_896 ALG_RSA_PKCS1_OAEP", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Cipher_ALG_RSA_ISO14888,"ALG_RSA_CRT LENGTH_RSA_1024 ALG_RSA_ISO14888", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Cipher_ALG_RSA_ISO9796,"ALG_RSA_CRT LENGTH_RSA_1024 ALG_RSA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Cipher_ALG_RSA_NOPAD,"ALG_RSA_CRT LENGTH_RSA_1024 ALG_RSA_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Cipher_ALG_RSA_PKCS1,"ALG_RSA_CRT LENGTH_RSA_1024 ALG_RSA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_1024,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"ALG_RSA_CRT LENGTH_RSA_1024 ALG_RSA_PKCS1_OAEP", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Cipher_ALG_RSA_ISO14888,"ALG_RSA_CRT LENGTH_RSA_1280 ALG_RSA_ISO14888", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Cipher_ALG_RSA_ISO9796,"ALG_RSA_CRT LENGTH_RSA_1280 ALG_RSA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Cipher_ALG_RSA_NOPAD,"ALG_RSA_CRT LENGTH_RSA_1280 ALG_RSA_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Cipher_ALG_RSA_PKCS1,"ALG_RSA_CRT LENGTH_RSA_1280 ALG_RSA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_1280,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"ALG_RSA_CRT LENGTH_RSA_1280 ALG_RSA_PKCS1_OAEP", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Cipher_ALG_RSA_ISO14888,"ALG_RSA_CRT LENGTH_RSA_1536 ALG_RSA_ISO14888", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Cipher_ALG_RSA_ISO9796,"ALG_RSA_CRT LENGTH_RSA_1536 ALG_RSA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Cipher_ALG_RSA_NOPAD,"ALG_RSA_CRT LENGTH_RSA_1536 ALG_RSA_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Cipher_ALG_RSA_PKCS1,"ALG_RSA_CRT LENGTH_RSA_1536 ALG_RSA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_1536,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"ALG_RSA_CRT LENGTH_RSA_1536 ALG_RSA_PKCS1_OAEP", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Cipher_ALG_RSA_ISO14888,"ALG_RSA_CRT LENGTH_RSA_1984 ALG_RSA_ISO14888", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Cipher_ALG_RSA_ISO9796,"ALG_RSA_CRT LENGTH_RSA_1984 ALG_RSA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Cipher_ALG_RSA_NOPAD,"ALG_RSA_CRT LENGTH_RSA_1984 ALG_RSA_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Cipher_ALG_RSA_PKCS1,"ALG_RSA_CRT LENGTH_RSA_1984 ALG_RSA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_1984,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"ALG_RSA_CRT LENGTH_RSA_1984 ALG_RSA_PKCS1_OAEP", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Cipher_ALG_RSA_ISO14888,"ALG_RSA_CRT LENGTH_RSA_2048 ALG_RSA_ISO14888", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Cipher_ALG_RSA_ISO9796,"ALG_RSA_CRT LENGTH_RSA_2048 ALG_RSA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Cipher_ALG_RSA_NOPAD,"ALG_RSA_CRT LENGTH_RSA_2048 ALG_RSA_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Cipher_ALG_RSA_PKCS1,"ALG_RSA_CRT LENGTH_RSA_2048 ALG_RSA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_2048,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"ALG_RSA_CRT LENGTH_RSA_2048 ALG_RSA_PKCS1_OAEP", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Cipher_ALG_RSA_ISO14888,"ALG_RSA_CRT LENGTH_RSA_3072 ALG_RSA_ISO14888", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Cipher_ALG_RSA_ISO9796,"ALG_RSA_CRT LENGTH_RSA_3072 ALG_RSA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Cipher_ALG_RSA_NOPAD,"ALG_RSA_CRT LENGTH_RSA_3072 ALG_RSA_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Cipher_ALG_RSA_PKCS1,"ALG_RSA_CRT LENGTH_RSA_3072 ALG_RSA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_3072,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"ALG_RSA_CRT LENGTH_RSA_3072 ALG_RSA_PKCS1_OAEP", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Cipher_ALG_RSA_ISO14888,"ALG_RSA_CRT LENGTH_RSA_4096 ALG_RSA_ISO14888", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Cipher_ALG_RSA_ISO9796,"ALG_RSA_CRT LENGTH_RSA_4096 ALG_RSA_ISO9796", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Cipher_ALG_RSA_NOPAD,"ALG_RSA_CRT LENGTH_RSA_4096 ALG_RSA_NOPAD", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Cipher_ALG_RSA_PKCS1,"ALG_RSA_CRT LENGTH_RSA_4096 ALG_RSA_PKCS1", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testCipher(JCConsts.KeyPair_ALG_RSA_CRT, JCConsts.KeyBuilder_LENGTH_RSA_4096,JCConsts.Cipher_ALG_RSA_PKCS1_OAEP,"ALG_RSA_CRT LENGTH_RSA_4096 ALG_RSA_PKCS1_OAEP", numRepeatWholeOperation, numRepeatWholeMeasurement); 
        
        tableName = "CIPHER - END\n";
        file.write(tableName.getBytes());
    }
    
    public static void testSignature(byte keyType, short keyLength, byte alg, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        testSignatureWithKeyClass(Consts.UNUSED, keyType, keyLength, alg, info, numRepeatWholeOperation, numRepeatWholeMeasurement);
    }   
    public static void testSignatureWithKeyClass(byte keyClass, byte keyType, short keyLength, byte alg, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        TestSettings testSet = PerformanceTesting.prepareTestSettings(Consts.CLASS_SIGNATURE, alg, keyType, keyLength, JCConsts.Signature_update, 
            Consts.TEST_DATA_LENGTH, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);      
        testSet.keyClass = keyClass;
        
        testSet.algorithmMethod = JCConsts.Signature_update;
        PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_SIGNATURE, Consts.INS_PERF_TEST_CLASS_SIGNATURE, testSet, info + " Signature_update()");
        testSet.algorithmMethod = JCConsts.Signature_sign;
        PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_SIGNATURE, Consts.INS_PERF_TEST_CLASS_SIGNATURE, testSet, info + " Signature_sign()");
        testSet.algorithmMethod = JCConsts.Signature_init;
        PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_SIGNATURE, Consts.INS_PERF_TEST_CLASS_SIGNATURE, testSet, info + " Signature_init()");
        testSet.algorithmMethod = JCConsts.Signature_verify;
        PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_SIGNATURE, Consts.INS_PERF_TEST_CLASS_SIGNATURE, testSet, info + " Signature_verify()");
    }
    public static void testAllSignatures(int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception {
        testAllSignatures((short) numRepeatWholeOperation, (short) numRepeatWholeMeasurement);
    }
    public static void testAllSignatures(short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        String tableName = "\n\nSIGNATURE\n";
        file.write(tableName.getBytes());
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
        
        // test signatures for JCConsts.KeyPair_ALG_RSA and CRT keys, ECC keys
        
        
        tableName = "SIGNATURE - END\n";
        file.write(tableName.getBytes());        
    }    
    
    public static void testChecksum(byte alg, String info, short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        TestSettings testSet = PerformanceTesting.prepareTestSettings(Consts.CLASS_CHECKSUM, alg, Consts.UNUSED, Consts.UNUSED, JCConsts.Signature_update, 
            Consts.TEST_DATA_LENGTH, Consts.UNUSED, numRepeatWholeOperation, (short) 1, numRepeatWholeMeasurement);      
        
        testSet.algorithmMethod = JCConsts.Checksum_update;
        PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CHECKSUM, Consts.INS_PERF_TEST_CLASS_CHECKSUM, testSet, info + " Checksum_update()");
        testSet.algorithmMethod = JCConsts.Checksum_doFinal;
        PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CHECKSUM, Consts.INS_PERF_TEST_CLASS_CHECKSUM, testSet, info + " Checksum_doFinal()");

    }   
    public static void testAllChecksums(int numRepeatWholeOperation, int numRepeatWholeMeasurement) throws IOException, Exception {
        testAllChecksums((short) numRepeatWholeOperation, (short) numRepeatWholeMeasurement);
    }
    public static void testAllChecksums(short numRepeatWholeOperation, short numRepeatWholeMeasurement) throws IOException, Exception {
        String tableName = "\n\nCHECKSUM\n";
        file.write(tableName.getBytes());
        testChecksum(JCConsts.Checksum_ALG_ISO3309_CRC16,"ALG_ISO3309_CRC16", numRepeatWholeOperation, numRepeatWholeMeasurement);
        testChecksum(JCConsts.Checksum_ALG_ISO3309_CRC32,"ALG_ISO3309_CRC32", numRepeatWholeOperation, numRepeatWholeMeasurement);
        tableName = "CHECKSUM - END\n";
        file.write(tableName.getBytes());
    }    
    
    
    // TODO: KeyAgreement tests
    
    // TODO: all *Key tests
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
/* del
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
            cardManager.BasicTest(Consts.CLA_CARD_ALGTEST,Consts.INS_PREPARE_TEST_CLASS_KEYPAIR,alg,(byte)0,cdata,(byte) 2, Consts.INS_CARD_RESET);
            for(int i = 0;i<max;i++)
            {
                time = cardManager.BasicTest(Consts.CLA_CARD_ALGTEST,Consts.INS_PERF_TEST_CLASS_KEYPAIR,alg, (byte)0, cdata, (byte) 2, Consts.INS_CARD_RESET);
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
*/    
/* del    
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
            cardManager.BasicTest(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_MESSAGEDIGEST,alg, (byte) 0, cdata, (byte) 0, Consts.INS_CARD_RESET); 
            for (int i = step;i<maxLength;i+=step)
            {            
                cdata[1] = (byte) (i & mask);
                int pom = i >> 7;
                cdata[0] = (byte) (pom & mask);
                double dia = 0;            
                System.out.print("(");
                for (int j = 0;j<cycle;j++)
                {
                    time1 = cardManager.BasicTest(Consts.CLA_CARD_ALGTEST, Consts.INS_PERF_TEST_CLASS_MESSAGEDIGEST,alg,(byte)(count * 2), cdata, (byte) 2, Consts.INS_CARD_RESET);
                    time2 = cardManager.BasicTest(Consts.CLA_CARD_ALGTEST, Consts.INS_PERF_TEST_CLASS_MESSAGEDIGEST,alg, count, cdata, (byte) 2, Consts.INS_CARD_RESET);
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
            cardManager.BasicTest(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_RANDOMDATA,alg, (byte)0, cdata, (byte) 0, Consts.INS_CARD_RESET);
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
                        time1 = cardManager.BasicTest(Consts.CLA_CARD_ALGTEST, Consts.INS_PERF_TEST_CLASS_RANDOMDATA,alg, (byte)(count * 2), cdata, (byte) 4, Consts.INS_CARD_RESET);
                        time2 = cardManager.BasicTest(Consts.CLA_CARD_ALGTEST, Consts.INS_PERF_TEST_CLASS_RANDOMDATA,alg, count, cdata, (byte) 4, Consts.INS_CARD_RESET);
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
            cardManager.BasicTest(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CIPHER,key, alg, cdata, (byte) 2, Consts.INS_CARD_RESET);
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
                    time1 = cardManager.BasicTest(Consts.CLA_CARD_ALGTEST, Consts.INS_PERF_TEST_CLASS_SIGNATURE,(byte)0, (byte)(count * 2), cdata, (byte) 2, Consts.INS_CARD_RESET);
                    time2 = cardManager.BasicTest(Consts.CLA_CARD_ALGTEST, Consts.INS_PERF_TEST_CLASS_SIGNATURE,(byte)0, count, cdata, (byte) 2, Consts.INS_CARD_RESET);
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
                    time1 = cardManager.BasicTest(Consts.CLA_CARD_ALGTEST, Consts.INS_PERF_TEST_CLASS_CHECKSUM,alg, (byte)(count * 2), cdata, (byte) 2, Consts.INS_CARD_RESET);
                    time2 = cardManager.BasicTest(Consts.CLA_CARD_ALGTEST, Consts.INS_PERF_TEST_CLASS_CHECKSUM,alg, count, cdata, (byte) 2, Consts.INS_CARD_RESET);
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
*/    
        
}
