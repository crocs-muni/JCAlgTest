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

/**
 *
 * @author lukas.srom
 */
public class PerformanceTesting {
    /* Argument constants for choosing algorithm to test. */
    public static final String TEST_ALL_ALGORITHMS = "ALL_ALGS";
    
    public static final String TEST_EEPROM = "EEPROM";
    public static final String TEST_RAM = "RAM";
    public static final String TEST_EXTENDEDAPDU = "EXTENDEDAPDU";
    public static final String TEST_RSAEXPONENT = "RSAEXPONENT";
    
    static CardMngr cardManager = new CardMngr();
    
    public StringBuilder value = new StringBuilder();
    public String message = "";
    
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
                if (testingPerformance.TestVariableRSAPublicExponentSupport(value, file, (byte) 0) == cardManager.STAT_OK) {}
                else { 
                    message = "\nERROR: Test variable public exponent support fail\n"; 
                    System.out.println(message); file.write(message.getBytes());
                }
                file.flush();
            }
            else if (Arrays.asList(args).contains(TEST_RAM)){
                value.setLength(0);
                if (testingPerformance.TestAvailableRAMMemory(value, file, (byte) 0) == cardManager.STAT_OK) {}
                else { 
                    message = "\nERROR: Get available RAM memory fail\n"; 
                    System.out.println(message); file.write(message.getBytes());
                }
                file.flush();
            }
            else if (Arrays.asList(args).contains(TEST_EEPROM)){
                value.setLength(0);
                if (testingPerformance.TestAvailableEEPROMMemory(value, file, (byte) 0) == cardManager.STAT_OK) {}
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
            
            if (testingPerformance.TestVariableRSAPublicExponentSupport(value, file, (byte) 0) == cardManager.STAT_OK) {}
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
            if (testingPerformance.TestAvailableRAMMemory(value, file, (byte) 0) == cardManager.STAT_OK) {}
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
            if (testingPerformance.TestAvailableEEPROMMemory(value, file, (byte) 0) == cardManager.STAT_OK) {}
            else { 
                message = "\nERROR: Get available EEPROM memory fail\n"; 
                System.out.println(message); file.write(message.getBytes());
            }
            file.flush();
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
        if (testingPerformance.TestVariableRSAPublicExponentSupport(value, file, (byte) 0) == cardManager.STAT_OK) {}
        else { 
            message = "\nERROR: Test variable public exponent support fail\n"; 
            System.out.println(message); file.write(message.getBytes());
        }
        file.flush();
        
        /* Available RAM memory. */
        value.setLength(0);
        if (testingPerformance.TestAvailableRAMMemory(value, file, (byte) 0) == cardManager.STAT_OK) {}
        else { 
            message = "\nERROR: Get available RAM memory fail\n"; 
            System.out.println(message); file.write(message.getBytes());
        }
        file.flush();
        
        /* Available EEPROM memory. */
        value.setLength(0);
        if (testingPerformance.TestAvailableEEPROMMemory(value, file, (byte) 0) == cardManager.STAT_OK) {}
        else { 
            message = "\nERROR: Get available EEPROM memory fail\n"; 
            System.out.println(message); file.write(message.getBytes());
        }
        file.flush();
    }
    
    public int TestAvailableRAMMemory(StringBuilder pValue, FileOutputStream pFile, byte algPartP2) throws Exception {
        int         status = cardManager.STAT_OK;
        long     elapsedCard;

        // Prepare test memory apdu
        byte apdu[] = new byte[cardManager.HEADER_LENGTH];
        apdu[cardManager.OFFSET_CLA] = (byte) 0xB0;
        apdu[cardManager.OFFSET_INS] = (byte) 0x71;
        apdu[cardManager.OFFSET_P1] = 0x00;
        apdu[cardManager.OFFSET_P2] = 0x00;
        apdu[cardManager.OFFSET_LC] = 0x00;
            
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
            elTimeStr = String.format("%1f", (double) elapsedCard / (float) cardManager.CLOCKS_PER_SEC);

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
        int         status = cardManager.STAT_OK;
        long     elapsedCard;

        // Prepare test memory apdu
        byte apdu[] = new byte[cardManager.HEADER_LENGTH];
        apdu[cardManager.OFFSET_CLA] = (byte) 0xB0;
        apdu[cardManager.OFFSET_INS] = (byte) 0x71;
        apdu[cardManager.OFFSET_P1] = 0x01;
        apdu[cardManager.OFFSET_P2] = 0x00;
        apdu[cardManager.OFFSET_LC] = 0x00;
            
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
            elTimeStr = String.format("%1f", (double) elapsedCard / (float) cardManager.CLOCKS_PER_SEC);

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
	int		status = cardManager.STAT_OK;

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
            elTimeStr = String.format("%1f", (double) elapsedCard / (float) cardManager.CLOCKS_PER_SEC);

            message = String.format("yes;%1s sec;", elTimeStr); 
            System.out.println(message);
            pFile.write(message.getBytes());
            pValue.append(message);
	}

	return status;
    }

    public int TestVariableRSAPublicExponentSupport(StringBuilder pValue, FileOutputStream pFile, byte algPartP2) throws Exception {
        int         status = cardManager.STAT_OK;
        
        byte apdu[] = new byte[cardManager.HEADER_LENGTH];
        apdu[cardManager.OFFSET_CLA] = (byte) 0xB0;
        apdu[cardManager.OFFSET_INS] = (byte) 0x72;
        apdu[cardManager.OFFSET_P1] = 0x00;
        apdu[cardManager.OFFSET_P2] = 0x00;
        apdu[cardManager.OFFSET_LC] = 0x00;
            
        String message;
        message = "\r\nSupport for variable public exponent for RSA 1024. If supported, user-defined fast modular exponentiation can be executed on the smart card via cryptographic coprocessor. This is very specific feature and you will probably not need it;"; 
        System.out.println(message);
        pFile.write(message.getBytes());
        pValue.append(message);

        // Allocate RSA 1024 objects (RSAPublicKey and ALG_RSA_NOPAD cipher)
        apdu[cardManager.OFFSET_P1] = 0x01;	
        TestAction("Allocate RSA 1024 objects", apdu, pValue,pFile);
        // Try to set random modulus
        apdu[cardManager.OFFSET_P1] = 0x02;	
        TestAction("Set random modulus", apdu, pValue,pFile);
        // Try to set random exponent
        apdu[cardManager.OFFSET_P1] = 0x03;	
        TestAction("Set random public exponent", apdu, pValue,pFile);
        // Try to initialize cipher with public key with random exponent
        apdu[cardManager.OFFSET_P1] = 0x04;	
        TestAction("Initialize cipher with public key with random exponent", apdu, pValue,pFile);
        // Try to encrypt block of data
        apdu[cardManager.OFFSET_P1] = 0x05;	
        TestAction("Use random public exponent", apdu, pValue,pFile);        
 
        return status;
    }
    
    public int TestExtendedAPDUSupportSupport(StringBuilder pValue, FileOutputStream pFile, byte algPartP2) throws Exception {
        int         status = cardManager.STAT_OK;
        
        byte apdu[] = new byte[cardManager.HEADER_LENGTH + 2 + cardManager.EXTENDED_APDU_TEST_LENGTH]; // + 2 is because of encoding of LC length into three bytes total
        apdu[cardManager.OFFSET_CLA] = (byte) 0xB0;
        apdu[cardManager.OFFSET_INS] = (byte) 0x74;
        apdu[cardManager.OFFSET_P1] = 0x00;
        apdu[cardManager.OFFSET_P2] = 0x00;
        apdu[cardManager.OFFSET_LC] = 0x00;
        apdu[cardManager.OFFSET_LC+1] = (byte)(cardManager.EXTENDED_APDU_TEST_LENGTH & 0xff00 >> 8);
        apdu[cardManager.OFFSET_LC+2] = (byte)(cardManager.EXTENDED_APDU_TEST_LENGTH & 0xff);
            
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
            
            if (LC == cardManager.EXTENDED_APDU_TEST_LENGTH && realLC == cardManager.EXTENDED_APDU_TEST_LENGTH) {
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
}
