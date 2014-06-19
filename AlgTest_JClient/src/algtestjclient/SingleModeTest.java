/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package algtestjclient;

/* Import 'ALGTEST_JCLIENT_VERSION' variable - possibly replace with actual import of those variables later? */
import static algtestjclient.AlgTestJClient.ALGTEST_JCLIENT_VERSION;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.ResponseAPDU;

/**
 *
 * @author lukas.srom
 */
public class SingleModeTest {
    CardMngr cardManager = new CardMngr();
    
    /* APDU auxiliary variables */
    public static final byte OFFSET_CLA = 0x00;
    public static final byte OFFSET_INS = 0x01;
    public static final byte OFFSET_P1 = 0x02;
    public static final byte OFFSET_P2 = 0x03;
    public static final byte OFFSET_LC = 0x04;
    public static final byte OFFSET_DATA = 0x05;
    public static final byte HEADER_LENGTH = 0x05;
    
    /* Auxiliary variables to choose class */
    public static final byte CLASS_CIPHER          = 0x11;
    public static final byte CLASS_SIGNATURE       = 0x12;
    public static final byte CLASS_KEYAGREEMENT    = 0x13;
    public static final byte CLASS_MESSAGEDIGEST   = 0x15;
    public static final byte CLASS_RANDOMDATA      = 0x16;
    public static final byte CLASS_CHECKSUM        = 0x17;
    //public static final byte CLASS_KEYPAIR_RSA     = 0x18;
    //public static final byte CLASS_KEYPAIR_RSA_CRT = 0x19;
    //public static final byte CLASS_KEYPAIR_DSA     = 0x1A;
    //public static final byte CLASS_KEYPAIR_EC_F2M  = 0x1B;
    public static final byte CLASS_KEYPAIR_EC_FP   = 0x1C;
    public static final byte CLASS_KEYBUILDER      = 0x20;
    
    /* Class 'javacardx.crypto.Cipher' */
    /* REMOVE THIS IF FOUND UNNECESSARY */
    public static final byte ALG_DES_CBC_NOPAD                     = 1;
    public static final byte ALG_DES_CBC_ISO9797_M1                = 2;
    public static final byte ALG_DES_CBC_ISO9797_M2                = 3;
    public static final byte ALG_DES_CBC_PKCS5                     = 4;
    public static final byte ALG_DES_ECB_NOPAD                     = 5;
    public static final byte ALG_DES_ECB_ISO9797_M1                = 6;
    public static final byte ALG_DES_ECB_ISO9797_M2                = 7;
    public static final byte ALG_DES_ECB_PKCS5                     = 8;
    public static final byte ALG_RSA_ISO14888                      = 9;
    public static final byte ALG_RSA_PKCS1                         = 10;
    public static final byte ALG_RSA_ISO9796                       = 11;
    public static final byte ALG_RSA_NOPAD                         = 12;
    public static final byte ALG_AES_BLOCK_128_CBC_NOPAD           = 13;
    public static final byte ALG_AES_BLOCK_128_ECB_NOPAD           = 14;
    public static final byte ALG_RSA_PKCS1_OAEP                    = 15;
    // JC2.2.2
    public static final byte ALG_KOREAN_SEED_ECB_NOPAD             = 16;
    public static final byte ALG_KOREAN_SEED_CBC_NOPAD             = 17;
    // JC3.0.1
    public static final byte ALG_AES_BLOCK_192_CBC_NOPAD = 18;  
    public static final byte ALG_AES_BLOCK_192_ECB_NOPAD = 19;  
    public static final byte ALG_AES_BLOCK_256_CBC_NOPAD = 20;  
    public static final byte ALG_AES_BLOCK_256_ECB_NOPAD = 21;  
    public static final byte ALG_AES_CBC_ISO9797_M1 = 22;  
    public static final byte ALG_AES_CBC_ISO9797_M2 = 23;  
    public static final byte ALG_AES_CBC_PKCS5 = 24;  
    public static final byte ALG_AES_ECB_ISO9797_M1 = 25;  
    public static final byte ALG_AES_ECB_ISO9797_M2 = 26;  
    public static final byte ALG_AES_ECB_PKCS5 = 27;
    
    /**
     * String array containing class 'javacardx.crypto.Cipher'
     * AlgTest on the card will be called using 'i' from cycle FOR and to the output file will be written the corresponding string from this array
     */
    public static final String[] Cipher = {
        "javacardx.crypto.Cipher",          // [00]
        "ALG_DES_CBC_NOPAD",                // [01]
        "ALG_DES_CBC_ISO9797_M1",           // [02]
        "ALG_DES_CBC_ISO9797_M2",           // [03]
        "ALG_DES_CBC_PKCS5",                // [04]
        "ALG_DES_ECB_NOPAD",                // [05]
        "ALG_DES_ECB_ISO9797_M1",           // [06]
        "ALG_DES_ECB_ISO9797_M2",           // [07]
        "ALG_DES_ECB_PKCS5",                // [08]
        "ALG_RSA_ISO14888",                 // [09]
        "ALG_RSA_PKCS1",                    // [10]
        "ALG_RSA_ISO9796",                  // [11]
        "ALG_RSA_NOPAD",                    // [12]
        "ALG_AES_BLOCK_128_CBC_NOPAD",      // [13]
        "ALG_AES_BLOCK_128_ECB_NOPAD",      // [14]
        "ALG_RSA_PKCS1_OAEP",               // [15]
        // JC2.2.2
        "ALG_KOREAN_SEED_ECB_NOPAD",        // [16]
        "ALG_KOREAN_SEED_CBC_NOPAD",        // [17]
        // JC3.0.1
        "ALG_AES_BLOCK_192_CBC_NOPAD",      // [18]
        "ALG_AES_BLOCK_192_ECB_NOPAD",      // [19]
        "ALG_AES_BLOCK_256_CBC_NOPAD",      // [20]
        "ALG_AES_BLOCK_256_ECB_NOPAD",      // [21]
        "ALG_AES_CBC_ISO9797_M1",           // [22]
        "ALG_AES_CBC_ISO9797_M2",           // [23]
        "ALG_AES_CBC_PKCS5",                // [24]
        "ALG_AES_ECB_ISO9797_M1",           // [25]
        "ALG_AES_ECB_ISO9797_M2",           // [26]
        "ALG_AES_ECB_PKCS5"                 // [27]
    };
    
    //public static final String Cipher[0]="javacardx.crypto.Cipher";
    
    public void TestSingleAlg () throws IOException, Exception{
        /* Reads text from a character-input stream, buffering characters so as to provide
           for the efficient reading of characters, arrays, and lines */
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));  
        int answ = 0;   // When set to 0, program will ask for each algorithm to test
        
        System.out.println("Do you want to test all possible algorithms at once?\n1 = YES, 0 = NO");
        answ = Integer.decode(br.readLine());
        
        /* Creating output file */
        StringBuilder atr = new StringBuilder(); 
        StringBuilder reader = new StringBuilder();
        StringBuilder protocol = new StringBuilder();
        cardManager.ConnectToCard(reader, atr, protocol);   // connecting to card and getting reader, atr and protocol info
        String fileName = "AlgTestNew_" + atr + ".csv";
        fileName = fileName.replace(":", "");

        FileOutputStream file = new FileOutputStream(fileName);
        CreateFile(file, reader, atr, protocol);
        
        /* Chooses action based on input argument 'answ' (1/0) */
        switch (answ) {
            /* Program will ask for every class */
            case 0:
                /* Class 'javacardx.crypto.Cipher' */
                System.out.println("Do you want to test algorithms from class 'Cipher'?\n1 = YES, 0 = NO");
                    answ = Integer.decode(br.readLine());
                    if (answ == 1){TestClassCipher(file);}
                    else{ClassSkipped(file, "javacardx.crypto.Cipher");}
                
                /* Class 'javacard.security.Singnature' */    // TODO
                System.out.println("Do you want to test algorithms from class 'Signature'?\n1 = YES, 0 = NO");
                    answ = Integer.decode(br.readLine());
                    if (answ == 1){}
                    else{ClassSkipped(file, "javacard.security.Signature");}
                
                /* Class 'javacard.security.MessageDigest' */    // TODO
                System.out.println("Do you want to test algorithms from class 'MessageDigest'?\n1 = YES, 0 = NO");
                    answ = Integer.decode(br.readLine());
                    if (answ == 1){}
                    else{ClassSkipped(file, "javacard.security.MessageDigest");}
                    
                /* Class 'javacard.security.RandomData' */    // TODO
                System.out.println("Do you want to test algorithms from class 'RandomData'?\n1 = YES, 0 = NO");
                    answ = Integer.decode(br.readLine());
                    if (answ == 1){}
                    else{ClassSkipped(file, "javacard.security.RandomData");}
                    
                /* Class 'javacard.security.KeyBuilder' */    // TODO
                System.out.println("Do you want to test algorithms from class 'KeyBuilder'?\n1 = YES, 0 = NO");
                    answ = Integer.decode(br.readLine());
                    if (answ == 1){}
                    else{ClassSkipped(file, "javacard.security.KeyBuilder");}
                    
                /* Class 'javacard.security.KePair' */    // TODO
                System.out.println("Do you want to test algorithms from class 'KeyPair'?\n1 = YES, 0 = NO");
                    answ = Integer.decode(br.readLine());
                    if (answ == 1){}
                    else{ClassSkipped(file, "javacard.security.KeyPair");}
                    
                /* Class 'javacard.security.KeyAgreement' */    // TODO
                System.out.println("Do you want to test algorithms from class 'KeyAgreement'?\n1 = YES, 0 = NO");
                    answ = Integer.decode(br.readLine());
                    if (answ == 1){}
                    else{ClassSkipped(file, "javacard.security.KeyAgreement");}
                    
                /* Class 'javacard.security.Checksum' */    // TODO
                System.out.println("Do you want to test algorithms from class 'Checksum'?\n1 = YES, 0 = NO");
                    answ = Integer.decode(br.readLine());
                    if (answ == 1){}
                    else{ClassSkipped(file, "javacard.security.Checksum");}
                
                /* Closing file */
                CloseFile(file);
            break;
            
            /* Program will test all algorithms at once */
            case 1:
            break;
                
            /* In case of wrong argument */
            default:
                System.err.println("First argument must be 0 or 1!");
            break;
        }
    }
    
    /**
     * Creates file and writes basic info in it, i.e. used reader, card ATR, card communication protocol, current date and time and who runs the test
     * @param file FileOutputStream object containing output file
     * @param reader Reader device to which the card is connected
     * @param atr Card's 'Answer To Reset' bytes
     * @param protocol Protocol used to communicate with current smart card
     * @throws FileNotFoundException TODO: add try{}catch()
     * @throws IOException TODO: add try{}catch()
     * @throws Exception TODO: add try{}catch()
     */
    public void CreateFile (FileOutputStream file, StringBuilder reader, StringBuilder atr, StringBuilder protocol) throws FileNotFoundException, IOException, Exception{            
        String message = "";
                        
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
                
        message = "AlgTestJClient version; " + ALGTEST_JCLIENT_VERSION + "\r\n";
        System.out.println(message); file.write(message.getBytes());    
             
        value.setLength(0);
        if (cardManager.GetAppletVersion(value) == CardMngr.STAT_OK) {
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
        
        
        //file.close();    
    }
    
    /**
     * Closes file given in parameter
     * @param file FileOutputStream object to close
     */
    public void CloseFile(FileOutputStream file){
        try {
            file.close();
        } catch (IOException ex) {
            Logger.getLogger(SingleModeTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Prints to the file and on the screen message about skipping specified algorithm class
     * @param file FileOutputStream object containing output file
     * @param className Name of the skipped class
     * @throws IOException
     */
    public void ClassSkipped(FileOutputStream file, String className) throws IOException{
        /* Message to be send on the screen and to the output file */
        String message = "\nTesting of algorithm class " + className + " skipped by user\r\n";

        System.out.println(message);
        file.write(message.getBytes());
    }
    
    /**
     * Tests all algorithms in class 'javacardx.crypto.Cipher' and results writes into the output file
     * @param file FileOutputStream object containing output file
     * @throws Exception
     */
    public void TestClassCipher(FileOutputStream file) throws Exception{
        byte[] apdu = new byte[6];
        apdu[OFFSET_CLA] = (byte)0xB0;  // for ALgTest applet
        apdu[OFFSET_INS] = (byte)0x75;  // for AlgTest applet switch to 'TestSupportedModeSingle'
        apdu[OFFSET_P1] = (byte)0x11;   // to test class Cipher
        apdu[OFFSET_P2] = (byte)0x00;
        apdu[OFFSET_LC] = (byte)0x01;
        
        String message = Cipher[0] + "\r\n";
        System.out.println(message);
        file.write(message.getBytes());
        
        for (int i=1; i<Cipher.length; i++){    // i = 1 because Cipher[0] is class name
            // TODO: implement IF statement to decide if test JC2.2.2 and JC3.0.1??
            apdu[OFFSET_DATA] = (byte)i;
            ResponseAPDU response = cardManager.sendAPDU(apdu);
            byte[] resp = response.getBytes();
            
            /* This block writes into the file and on the screen
               possibly replace with 'GetSupportedAndParse' from 'CardMngr'?*/
            if(resp[1]== 0x00){ // SUPPORTED
                message = Cipher[i] + ";" + "yes" + "\r\n";
                System.out.println(message);
                file.write(message.getBytes());
            }
            else{   // NOT SUPPORTED
                message = Cipher[i] + ";" + "no" + "\r\n";
                System.out.println(message);
                file.write(message.getBytes());
            }
        }
        
    }
}
