 /*
 * When finished, remove name 'AlgTestII_' and set it back to 'AlgTest_' !!!
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
    
    /* Response values */
    public static final short   SUPP_ALG_SUPPORTED  = 0;
    public static final short 	ILLEGAL_USE         = 5;
    public static final short 	ILLEGAL_VALUE       = 1;
    public static final short 	INVALID_INIT        = 4;
    public static final short 	NO_SUCH_ALGORITHM   = 3;
    public static final short 	UNINITIALIZED_KEY   = 2; 
    
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
        // JC 2.2.2
        "ALG_KOREAN_SEED_ECB_NOPAD",        // [16]
        "ALG_KOREAN_SEED_CBC_NOPAD",        // [17]
        // JC 3.0.1
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
    
    /**
     * String array containing class 'javacard.security.Signature'
     * AlgTest on the card will be called using 'i' from cycle FOR and to the output file will be written the corresponding string from this array
     */
    public static final String[] Signature = {
        "javacard.crypto.Signature",        // [00]
        "ALG_DES_MAC4_NOPAD",               // [01]
        "ALG_DES_MAC8_NOPAD",               // [02]
        "ALG_DES_MAC4_ISO9797_M1",          // [03]
        "ALG_DES_MAC8_ISO9797_M1",          // [04]
        "ALG_DES_MAC4_ISO9797_M2",          // [05]
        "ALG_DES_MAC8_ISO9797_M2",          // [06]
        "ALG_DES_MAC4_PKCS5",               // [07]
        "ALG_DES_MAC8_PKCS5",               // [08]
        "ALG_RSA_SHA_ISO9796",              // [09]
        "ALG_RSA_SHA_PKCS1",                // [10]
        "ALG_RSA_MD5_PKCS1",                // [11]
        "ALG_RSA_RIPEMD160_ISO9796",        // [12]
        "ALG_RSA_RIPEMD160_PKCS1",          // [13]
        "ALG_DSA_SHA",                      // [14]
        "ALG_RSA_SHA_RFC2409",              // [15]
        "ALG_RSA_MD5_RFC2409",              // [16]
        "ALG_ECDSA_SHA",                    // [17]
        "ALG_AES_MAC_128_NOPAD",            // [18]
        "ALG_DES_MAC4_ISO9797_1_M2_ALG3",   // [19]
        "ALG_DES_MAC8_ISO9797_1_M2_ALG3",   // [20]
        "ALG_RSA_SHA_PKCS1_PSS",            // [21]
        "ALG_RSA_MD5_PKCS1_PSS",            // [22]
        "ALG_RSA_RIPEMD160_PKCS1_PSS",      // [23]
        // JC 2.2.2
        "ALG_HMAC_SHA1",                    // [24]
        "ALG_HMAC_SHA_256",                 // [25]
        "ALG_HMAC_SHA_384",                 // [26]
        "ALG_HMAC_SHA_512",                 // [27]
        "ALG_HMAC_MD5",                     // [28]
        "ALG_HMAC_RIPEMD160",               // [29]
        "ALG_RSA_SHA_ISO9796_MR",           // [30]
        "ALG_RSA_RIPEMD160_ISO9796_MR",     // [31]
        "ALG_SEED_MAC_NOPAD",               // [32]
        // JC 3.0.1
        "ALG_ECDSA_SHA_256",                // [33]
        "ALG_ECDSA_SHA_384",                // [34]
        "ALG_AES_MAC_192_NOPAD",            // [35]
        "ALG_AES_MAC_256_NOPAD",            // [36]
        "ALG_ECDSA_SHA_224",                // [37]
        "ALG_ECDSA_SHA_512",                // [38]
        "ALG_RSA_SHA_224_PKCS1",            // [39]
        "ALG_RSA_SHA_256_PKCS1",            // [40]
        "ALG_RSA_SHA_384_PKCS1",            // [41]
        "ALG_RSA_SHA_512_PKCS1",            // [42]
        "ALG_RSA_SHA_224_PKCS1_PSS",        // [43]
        "ALG_RSA_SHA_256_PKCS1_PSS",        // [44]
        "ALG_RSA_SHA_384_PKCS1_PSS",        // [45]
        "ALG_RSA_SHA_512_PKCS1_PSS",        // [46]
        // JC 3.0.4
        "ALG_DES_MAC4_ISO9797_1_M1_ALG3",   // [47]
        "ALG_DES_MAC8_ISO9797_1_M1_ALG3"    // [48]
    };
    
    /**
     * String array containing class 'javacard.security.MessageDigest'
     * AlgTest on the card will be called using 'i' from cycle FOR and to the output file will be written the corresponding string from this array
     */
    public static final String[] MessageDigest= {
        "javacard.security.MessageDigest",  // [00]
        "ALG_SHA",                          // [01]
        "ALG_MD5",                          // [02]
        "ALG_RIPEMD160",                    // [03]
        // JC 2.2.2
        "ALG_SHA_256",                      // [04]
        "ALG_SHA_384",                      // [05]
        "ALG_SHA_512",                      // [06]
        // JC 3.0.1
        "ALG_SHA_224"                       // [07]
    };

    /**
     * String array containing class 'javacard.security.KeyAgreement'
     * AlgTest on the card will be called using 'i' from cycle FOR and to the output file will be written the corresponding string from this array
     * Last 4 algorithms might need to be renumbered from 1
     */
    public static final String[] KeyAgreement = {
        "javacard.security.KeyAgreement",   // [00]
        // JC 2.2.1
        "ALG_EC_SVDP_DH",             // [01]
        "ALG_EC_SVDP_DHC",            // [02]
        // JC 3.0.1
        "ALG_EC_SVDP_DH_KDF",         // [03]
        "ALG_EC_SVDP_DH_PLAIN",       // [04]
        "ALG_EC_SVDP_DHC_KDF",        // [05]
        "ALG_EC_SVDP_DHC_PLAIN"       // [06]
    };
    
    /**
     * String array containing class 'javacard.security.RandomData'
     * AlgTest on the card will be called using 'i' from cycle FOR and to the output file will be written the corresponding string from this array
     */
    public static final String[] RandomData = {
        "javacard.security.RandomData",     // [00]
        "ALG_PSEUDO_RANDOM#<=2.1",          // [01]
        "ALG_SECURE_RANDOM#<=2.1"};         // [02]
        
    /**
     * String array containing class 'javacard.security.KeyBuilder'
     * AlgTest on the card will be called using 'i' from cycle FOR and to the output file will be written the corresponding string from this array
     */
    public static final String[] KeyBuilder = {
        "javacard.security.KeyBuilder",             // [00]
        "TYPE_DES_TRANSIENT_RESET",                 // [01]
        "TYPE_DES_TRANSIENT_DESELECT",              // [02]
        "TYPE_DES",                                 // [03]
        "TYPE_RSA_PUBLIC",                          // [04]
        "TYPE_RSA_PRIVATE",                         // [05]
        "TYPE_RSA_CRT_PRIVATE",                     // [06]
        "TYPE_DSA_PUBLIC",                          // [07]
        "TYPE_DSA_PRIVATE",                         // [08]
        "TYPE_EC_F2M_PUBLIC",                       // [09]
        "TYPE_EC_F2M_PRIVATE",                      // [10]
        "TYPE_EC_FP_PUBLIC",                        // [11]
        "TYPE_EC_FP_PRIVATE",                       // [12]
        "TYPE_AES_TRANSIENT_RESET",                 // [13]
        "TYPE_AES_TRANSIENT_DESELECT",              // [14]
        "TYPE_AES",
        // JC 2.2.2
        "TYPE_KOREAN_SEED_TRANSIENT_RESET",         // [15]
        "TYPE_KOREAN_SEED_TRANSIENT_DESELECT",      // [16]
        "TYPE_KOREAN_SEED",                         // [17]
        "TYPE_HMAC_TRANSIENT_RESET",                // [18]
        "TYPE_HMAC_TRANSIENT_DESELECT",             // [19]
        "TYPE_HMAC",
        // JC 3.0.1
        "TYPE_RSA_PRIVATE_TRANSIENT_RESET",         // [20]
        "TYPE_RSA_PRIVATE_TRANSIENT_DESELECT",      // [21]
        "TYPE_RSA_CRT_PRIVATE_TRANSIENT_RESET",     // [22]
        "TYPE_RSA_CRT_PRIVATE_TRANSIENT_DESELECT",  // [23]
        "TYPE_DSA_PRIVATE_TRANSIENT_RESET",         // [24]
        "TYPE_DSA_PRIVATE_TRANSIENT_DESELECT",      // [25]
        "TYPE_EC_F2M_PRIVATE_TRANSIENT_RESET",      // [26]
        "TYPE_EC_F2M_PRIVATE_TRANSIENT_DESELECT",   // [27]
        "TYPE_EC_FP_PRIVATE_TRANSIENT_RESET",       // [28]
        "TYPE_EC_FP_PRIVATE_TRANSIENT_DESELECT"     // [29]        
    };
    
    /**
     * String array containing class 'javacard.security.Checksum'
     * AlgTest on the card will be called using 'i' from cycle FOR and to the output file will be written the corresponding string from this array
     */
    public static final String[] Checksum = {
        "javacard.security.Checksum",               // [00]
        "ALG_ISO3309_CRC16",                        // [01]
        "ALG_ISO3309_CRC32"                         // [02]
    };
    public static final byte ALG_ISO3309_CRC16             = 1;
    public static final byte ALG_ISO3309_CRC32             = 2;
    
       
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
        String fileName = "AlgTestII_" + atr + ".csv";
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
                
                /* Class 'javacard.security.Singnature' */
                System.out.println("Do you want to test algorithms from class 'Signature'?\n1 = YES, 0 = NO");
                    answ = Integer.decode(br.readLine());
                    if (answ == 1){TestClassSignature(file);}
                    else{ClassSkipped(file, "javacard.security.Signature");}
                
                /* Class 'javacard.security.MessageDigest' */
                System.out.println("Do you want to test algorithms from class 'MessageDigest'?\n1 = YES, 0 = NO");
                    answ = Integer.decode(br.readLine());
                    if (answ == 1){TestClassMessageDigest(file);}
                    else{ClassSkipped(file, "javacard.security.MessageDigest");}
                    
                /* Class 'javacard.security.RandomData' */
                System.out.println("Do you want to test algorithms from class 'RandomData'?\n1 = YES, 0 = NO");
                    answ = Integer.decode(br.readLine());
                    if (answ == 1){TestClassRandomData(file);}
                    else{ClassSkipped(file, "javacard.security.RandomData");}
                    
                /* Class 'javacard.security.KeyBuilder' */
                System.out.println("Do you want to test algorithms from class 'KeyBuilder'?\n1 = YES, 0 = NO");
                    answ = Integer.decode(br.readLine());
                    if (answ == 1){TestClassKeyBuilder(file);}
                    else{ClassSkipped(file, "javacard.security.KeyBuilder");}
                    
                /* Class 'javacard.security.KePair' */    // TODO
                System.out.println("Do you want to test algorithms from class 'KeyPair'?\n1 = YES, 0 = NO");
                    answ = Integer.decode(br.readLine());
                    if (answ == 1){}
                    else{ClassSkipped(file, "javacard.security.KeyPair");}
                    
                /* Class 'javacard.security.KeyAgreement' */
                System.out.println("Do you want to test algorithms from class 'KeyAgreement'?\n1 = YES, 0 = NO");
                    answ = Integer.decode(br.readLine());
                    if (answ == 1){TestClassKeyAgreement(file);}
                    else{ClassSkipped(file, "javacard.security.KeyAgreement");}
                    
                /* Class 'javacard.security.Checksum' */
                System.out.println("Do you want to test algorithms from class 'Checksum'?\n1 = YES, 0 = NO");
                    answ = Integer.decode(br.readLine());
                    if (answ == 1){TestClassChecksum(file);}
                    else{ClassSkipped(file, "javacard.security.Checksum");}
                
                /* Closing file */
                CloseFile(file);
            break;
            
            /* Program will test all algorithms at once */
            case 1:
                TestClassCipher(file);
                TestClassSignature(file);
                TestClassMessageDigest(file);
                TestClassRandomData(file);
                TestClassKeyBuilder(file);
                
                TestClassKeyAgreement(file);
                TestClassChecksum(file);
                
                CloseFile(file);
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
        
        /* Prints info on the screen and into the file */
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
     * Checks result of algorithm testing on smart card
     * @param file FileOutputStream object containing output file
     * @param name String containing algorithm name
     * @param response Response byte of APDU (second byte of incoming APDU) 
     * @throws IOException
     */
    public void CheckResult (FileOutputStream file, String name, byte response) throws IOException{
        String message = "";
        switch (response){
            case SUPP_ALG_SUPPORTED:
                message = name + ";" + "yes;" + "\r\n";
                System.out.println(message);
                file.write(message.getBytes());
            break;
            
            case NO_SUCH_ALGORITHM:
                message = name + ";" + "no;" + "\r\n";
                System.out.println(message);
                file.write(message.getBytes());
            break;
                
            case ILLEGAL_USE:
                message = name + ";" + "error(ILLEGAL_USE);" + "\r\n";
                System.out.println(message);
                file.write(message.getBytes());
            break;
                
            case ILLEGAL_VALUE:
                message = name + ";" + "error(ILLEGAL_VALUE);" + "\r\n";
                System.out.println(message);
                file.write(message.getBytes());
            break;
                
            case INVALID_INIT:
                message = name + ";" + "error(INVALID_INIT);" + "\r\n";
                System.out.println(message);
                file.write(message.getBytes());
            break;
                
            case UNINITIALIZED_KEY:
                message = name + ";" + "error(UNINITIALIZED_KEY);" + "\r\n";
                System.out.println(message);
                file.write(message.getBytes());
            break;
            
            case 0x6f:
                message = name + ";" + "maybe;" + "\r\n";
                System.out.println(message);
                file.write(message.getBytes());
                
            break;
                
            default:
                // OTHER VALUE, IGNORE 
                //System.out.println("Unknown value detected in AlgTest applet (0x" + Integer.toHexString(response[i] & 0xff) + "). Possibly, old version of AlTestJClient is used (try update)");
            break;
                
            
        
        }
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
        
        /* Creates message with class name and writes it in the output file and on the screen */
        String message = Cipher[0] + "\r\n";
        System.out.println(message);
        file.write(message.getBytes());
        
        for (int i=1; i<Cipher.length; i++){    // i = 1 because Cipher[0] is class name
            // TODO: implement IF statement to decide if test JC2.2.2 and JC3.0.1??
            apdu[OFFSET_DATA] = (byte)i;
            ResponseAPDU response = cardManager.sendAPDU(apdu);
            byte[] resp = response.getBytes();
            
            /* Calls method CheckResult - should add to output error messages */
            CheckResult(file, Cipher[0], resp[1]);
        }
        
    }
    
    /**
     * Tests all algorithms in class 'javacardx.crypto.Signature' and results writes into the output file
     * @param file FileOutputStream object containing output file
     * @throws IOException
     * @throws Exception
     */
    public void TestClassSignature (FileOutputStream file) throws IOException, Exception{
        byte[] apdu = new byte[6];
        apdu[OFFSET_CLA] = (byte)0xB0;  // for ALgTest applet
        apdu[OFFSET_INS] = (byte)0x75;  // for AlgTest applet switch to 'TestSupportedModeSingle'
        apdu[OFFSET_P1] = (byte)0x12;   // to test class Signature
        apdu[OFFSET_P2] = (byte)0x00;
        apdu[OFFSET_LC] = (byte)0x01;
        
        /* Creates message with class name and writes it in the output file and on the screen */
        String message = Signature[0] + "\r\n";
        System.out.println(message);
        file.write(message.getBytes());
        
        for (int i=1; i<Signature.length; i++){    // i = 1 because Signature[0] is class name
            // TODO: implement IF statement to decide if test JC2.2.2 and JC3.0.1??
            apdu[OFFSET_DATA] = (byte)i;
            ResponseAPDU response = cardManager.sendAPDU(apdu);
            byte[] resp = response.getBytes();
            
            /* Calls method CheckResult - should add to output error messages */
            CheckResult(file, Signature[0], resp[1]);
        }
        
    }
    
    /**
     * Tests all algorithms in class 'javacardx.crypto.MessageDigest' and results writes into the output file
     * @param file FileOutputStream object containing output file
     * @throws IOException
     * @throws Exception
     */
    public void TestClassMessageDigest (FileOutputStream file) throws IOException, Exception{
        byte[] apdu = new byte[6];
        apdu[OFFSET_CLA] = (byte)0xB0;  // for ALgTest applet
        apdu[OFFSET_INS] = (byte)0x75;  // for AlgTest applet switch to 'TestSupportedModeSingle'
        apdu[OFFSET_P1] = (byte)0x15;   // to test class MessageDigest
        apdu[OFFSET_P2] = (byte)0x00;
        apdu[OFFSET_LC] = (byte)0x01;
        
        /* Creates message with class name and writes it in the output file and on the screen */
        String message = MessageDigest[0] + "\r\n";
        System.out.println(message);
        file.write(message.getBytes());
        
        for (int i=1; i<MessageDigest.length; i++){    // i = 1 because MessageDigest[0] is class name
            // TODO: implement IF statement to decide if test JC2.2.2 and JC3.0.1??
            apdu[OFFSET_DATA] = (byte)i;
            ResponseAPDU response = cardManager.sendAPDU(apdu);
            byte[] resp = response.getBytes();
            
            /* Calls method CheckResult - should add to output error messages */
            CheckResult(file, MessageDigest[0], resp[1]);
        }
    }
    
    /**
     * Tests all algorithms in class 'javacardx.crypto.RandomData' and results writes into the output file
     * @param file FileOutputStream object containing output file
     * @throws IOException
     * @throws Exception
     */
    public void TestClassRandomData (FileOutputStream file) throws IOException, Exception{
        byte[] apdu = new byte[6];
        apdu[OFFSET_CLA] = (byte)0xB0;  // for ALgTest applet
        apdu[OFFSET_INS] = (byte)0x75;  // for AlgTest applet switch to 'TestSupportedModeSingle'
        apdu[OFFSET_P1] = (byte)0x16;   // to test class RandomData
        apdu[OFFSET_P2] = (byte)0x00;
        apdu[OFFSET_LC] = (byte)0x01;
        
        /* Creates message with class name and writes it in the output file and on the screen */
        String message = RandomData[0] + "\r\n";
        System.out.println(message);
        file.write(message.getBytes());
        
        for (int i=1; i<RandomData.length; i++){    // i = 1 because RandomData[0] is class name
            // TODO: implement IF statement to decide if test JC2.2.2 and JC3.0.1??
            apdu[OFFSET_DATA] = (byte)i;
            ResponseAPDU response = cardManager.sendAPDU(apdu);
            byte[] resp = response.getBytes();
            
            /* Calls method CheckResult - should add to output error messages */
            CheckResult(file, RandomData[0], resp[1]);
        }
    }
    
    /**
     * Tests all algorithms in class 'javacardx.crypto.KeyBuilder' and results writes into the output file
     * @param file FileOutputStream object containing output file
     * @throws IOException
     * @throws Exception
     */
    public void TestClassKeyBuilder (FileOutputStream file) throws IOException, Exception{
        byte[] apdu = new byte[6];
        apdu[OFFSET_CLA] = (byte)0xB0;  // for ALgTest applet
        apdu[OFFSET_INS] = (byte)0x75;  // for AlgTest applet switch to 'TestSupportedModeSingle'
        apdu[OFFSET_P1] = (byte)0x20;   // to test class KeyBuilder
        apdu[OFFSET_P2] = (byte)0x00;
        apdu[OFFSET_LC] = (byte)0x01;
        
        /* Creates message with class name and writes it in the output file and on the screen */
        String message = KeyBuilder[0] + "\r\n";
        System.out.println(message);
        file.write(message.getBytes());
        
        for (int i=1; i<KeyBuilder.length; i++){    // i = 1 because KeyBuilder[0] is class name
            // TODO: implement IF statement to decide if test JC2.2.2 and JC3.0.1??
            apdu[OFFSET_DATA] = (byte)i;
            ResponseAPDU response = cardManager.sendAPDU(apdu);
            byte[] resp = response.getBytes();
            
            /* Calls method CheckResult - should add to output error messages */
            CheckResult(file, KeyBuilder[0], resp[1]);
        }
    }
    
    /**
     * Tests all algorithms in class 'javacardx.crypto.KeyAgreement' and results writes into the output file
     * @param file FileOutputStream object containing output file
     * @throws IOException
     * @throws Exception
     */
    public void TestClassKeyAgreement (FileOutputStream file) throws IOException, Exception{
        byte[] apdu = new byte[6];
        apdu[OFFSET_CLA] = (byte)0xB0;  // for ALgTest applet
        apdu[OFFSET_INS] = (byte)0x75;  // for AlgTest applet switch to 'TestSupportedModeSingle'
        apdu[OFFSET_P1] = (byte)0x13;   // to test class KeyAgreement
        apdu[OFFSET_P2] = (byte)0x00;
        apdu[OFFSET_LC] = (byte)0x01;
    
        /* Creates message with class name and writes it in the output file and on the screen */
        String message = KeyAgreement[0] + "\r\n";
        System.out.println(message);
        file.write(message.getBytes());
        
        for (int i=1; i<KeyAgreement.length; i++){    // i = 1 because KeyAgreement[0] is class name
            // TODO: implement IF statement to decide if test JC2.2.2 and JC3.0.1??
            apdu[OFFSET_DATA] = (byte)i;
            ResponseAPDU response = cardManager.sendAPDU(apdu);
            byte[] resp = response.getBytes();
            
            /* Calls method CheckResult - should add to output error messages */
            CheckResult(file, KeyAgreement[0], resp[1]);
        }
    }
    
    /**
     * Tests all algorithms in class 'javacardx.crypto.Checksum' and results writes into the output file
     * @param file FileOutputStream object containing output file
     * @throws IOException
     * @throws Exception
     */
    public void TestClassChecksum (FileOutputStream file) throws IOException, Exception{
        byte[] apdu = new byte[6];
        apdu[OFFSET_CLA] = (byte)0xB0;  // for ALgTest applet
        apdu[OFFSET_INS] = (byte)0x75;  // for AlgTest applet switch to 'TestSupportedModeSingle'
        apdu[OFFSET_P1] = (byte)0x17;   // to test class KeyAgreement
        apdu[OFFSET_P2] = (byte)0x00;
        apdu[OFFSET_LC] = (byte)0x01;
    
        /* Creates message with class name and writes it in the output file and on the screen */
        String message = Checksum[0] + "\r\n";
        System.out.println(message);
        file.write(message.getBytes());
        
        for (int i=1; i<Checksum.length; i++){    // i = 1 because Checksum[0] is class name
            // TODO: implement IF statement to decide if test JC2.2.2 and JC3.0.1??
            apdu[OFFSET_DATA] = (byte)i;
            ResponseAPDU response = cardManager.sendAPDU(apdu);
            byte[] resp = response.getBytes();
            
            /* Calls method CheckResult - should add to output error messages */
            CheckResult(file, Checksum[0], resp[1]);
        }
    }
}   // END OF CLASS 'SINGLEMODETEST'


