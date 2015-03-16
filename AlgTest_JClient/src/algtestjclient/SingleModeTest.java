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

/* Import 'ALGTEST_JCLIENT_VERSION' variable - possibly replace with actual import of those variables later? */
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.ResponseAPDU;

/**
 * AlgTest class to test smart card supported algorithms using multiple APDU's each testing one algorithm.
 * Requires 'AlgTestSinglePerApdu' applet installed on card.
 * Supports running without connected card using jCardSim. (www.jcardsim.org)
 * @author lukas.srom
 * @version 1.0
 */
public class SingleModeTest {

    
    
    CardMngr cardManager = new CardMngr();
    
    /* Argument constants for choosing algorithm to test. */
    public static final String TEST_ALL_ALGORITHMS = "ALL_ALGS";
    
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
    public static final String TEST_RSA_PUBLIC_EXPONENT_SUPPORT = "RSA_PUBLIC_EXP";
    
    /* APDU auxiliary variables. */
    public static final byte OFFSET_CLA = 0x00;
    public static final byte OFFSET_INS = 0x01;
    public static final byte OFFSET_P1 = 0x02;
    public static final byte OFFSET_P2 = 0x03;
    public static final byte OFFSET_LC = 0x04;
    public static final byte OFFSET_DATA = 0x05;
    public static final byte HEADER_LENGTH = 0x05;
    
    /* Auxiliary variables to choose class - used in APDU as P1 byte. */
    public static final byte CLASS_CIPHER          = 0x11;
    public static final byte CLASS_SIGNATURE       = 0x12;
    public static final byte CLASS_KEYAGREEMENT    = 0x13;
    public static final byte CLASS_MESSAGEDIGEST   = 0x15;
    public static final byte CLASS_RANDOMDATA      = 0x16;
    public static final byte CLASS_CHECKSUM        = 0x17;
    public static final byte CLASS_KEYPAIR         = 0x19;
    public static final byte CLASS_KEYBUILDER      = 0x20;
    
    /* Response values - send back by on card application as response to command APDU. */
    public static final short   SUPP_ALG_SUPPORTED  = 0;
    public static final short 	ILLEGAL_USE         = 5;
    public static final short 	ILLEGAL_VALUE       = 1;
    public static final short 	INVALID_INIT        = 4;
    public static final short 	NO_SUCH_ALGORITHM   = 3;
    public static final short 	UNINITIALIZED_KEY   = 2; 
    
    /* Algorithms from class 'javacard.security.KeyPair'. */
    public static final byte ALG_RSA = 1;
    public static final byte ALG_RSA_CRT = 2;
    public static final byte ALG_DSA = 3;
    public static final byte ALG_EC_F2M = 4;
    public static final byte ALG_EC_FP = 5;
    
    /**
     * String array used in KeyBuilder testing for printing alg names.
     */
    public static final String KEYBUILDER_STR[] = {
        "javacard.security.KeyBuilder", 
        "TYPE_DES_TRANSIENT_RESET#<=2.1", "TYPE_DES_TRANSIENT_DESELECT#<=2.1", "TYPE_DES LENGTH_DES#<=2.1", "TYPE_DES LENGTH_DES3_2KEY#<=2.1", "TYPE_DES LENGTH_DES3_3KEY#<=2.1",
        //2.2.0
        "TYPE_AES_TRANSIENT_RESET#2.2.0", "TYPE_AES_TRANSIENT_DESELECT#2.2.0", "TYPE_AES LENGTH_AES_128#2.2.0", "TYPE_AES LENGTH_AES_192#2.2.0", "TYPE_AES LENGTH_AES_256#2.2.0",
        "TYPE_RSA_PUBLIC LENGTH_RSA_512#<=2.1", "TYPE_RSA_PUBLIC LENGTH_RSA_736#2.2.0", "TYPE_RSA_PUBLIC LENGTH_RSA_768#2.2.0", "TYPE_RSA_PUBLIC LENGTH_RSA_896#2.2.0",
            "TYPE_RSA_PUBLIC LENGTH_RSA_1024#<=2.1", "TYPE_RSA_PUBLIC LENGTH_RSA_1280#2.2.0", "TYPE_RSA_PUBLIC LENGTH_RSA_1536#2.2.0", "TYPE_RSA_PUBLIC LENGTH_RSA_1984#2.2.0", "TYPE_RSA_PUBLIC LENGTH_RSA_2048#<=2.1", "TYPE_RSA_PUBLIC LENGTH_RSA_3072#never#0", "TYPE_RSA_PUBLIC LENGTH_RSA_4096#3.0.1",
        "TYPE_RSA_PRIVATE LENGTH_RSA_512#<=2.1", "TYPE_RSA_PRIVATE LENGTH_RSA_736#2.2.0", "TYPE_RSA_PRIVATE LENGTH_RSA_768#2.2.0", "TYPE_RSA_PRIVATE LENGTH_RSA_896#2.2.0",
            "TYPE_RSA_PRIVATE LENGTH_RSA_1024#<=2.1", "TYPE_RSA_PRIVATE LENGTH_RSA_1280#2.2.0", "TYPE_RSA_PRIVATE LENGTH_RSA_1536#2.2.0", "TYPE_RSA_PRIVATE LENGTH_RSA_1984#2.2.0", "TYPE_RSA_PRIVATE LENGTH_RSA_2048#<=2.1", "TYPE_RSA_PRIVATE LENGTH_RSA_3072#never#0", "TYPE_RSA_PRIVATE LENGTH_RSA_4096#3.0.1", 
            "TYPE_RSA_PRIVATE_TRANSIENT_RESET#3.0.1", "TYPE_RSA_PRIVATE_TRANSIENT_DESELECT#3.0.1",
        "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_512#<=2.1", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_736#2.2.0", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_768#2.2.0", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_896#2.2.0",
            "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1024#<=2.1", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1280#2.2.0", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1536#2.2.0", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1984#2.2.0", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_2048#<=2.1", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_3072#never#0", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_4096#3.0.1",
            "TYPE_RSA_CRT_PRIVATE_TRANSIENT_RESET#3.0.1", "TYPE_RSA_CRT_PRIVATE_TRANSIENT_DESELECT#3.0.1",
        "TYPE_DSA_PRIVATE LENGTH_DSA_512#<=2.1", "TYPE_DSA_PRIVATE LENGTH_DSA_768#<=2.1", "TYPE_DSA_PRIVATE LENGTH_DSA_1024#<=2.1", "TYPE_DSA_PRIVATE_TRANSIENT_RESET#3.0.1", "TYPE_DSA_PRIVATE_TRANSIENT_DESELECT#3.0.1", 
        "TYPE_DSA_PUBLIC LENGTH_DSA_512#<=2.1", "TYPE_DSA_PUBLIC LENGTH_DSA_768#<=2.1", "TYPE_DSA_PUBLIC LENGTH_DSA_1024#<=2.1", 
        "TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_113#2.2.0", "TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_131#2.2.0", "TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_163#2.2.0", "TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_193#2.2.0", "TYPE_EC_F2M_PRIVATE_TRANSIENT_RESET#3.0.1", "TYPE_EC_F2M_PRIVATE_TRANSIENT_DESELECT#3.0.1",
        "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_112#2.2.0", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_128#2.2.0", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_160#2.2.0", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_192#2.2.0", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_224#3.0.1", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_256#3.0.1", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_384#3.0.1", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_521#3.0.4", "TYPE_EC_FP_PRIVATE_TRANSIENT_RESET#3.0.1", "TYPE_EC_FP_PRIVATE_TRANSIENT_DESELECT#3.0.1",
        "TYPE_KOREAN_SEED_TRANSIENT_RESET#2.2.2", "TYPE_KOREAN_SEED_TRANSIENT_DESELECT#2.2.2", "TYPE_KOREAN_SEED LENGTH_KOREAN_SEED_128#2.2.2", 
        "TYPE_HMAC_TRANSIENT_RESET#2.2.2", "TYPE_HMAC_TRANSIENT_DESELECT#2.2.2", "TYPE_HMAC LENGTH_HMAC_SHA_1_BLOCK_64#2.2.2", "TYPE_HMAC LENGTH_HMAC_SHA_256_BLOCK_64#2.2.2", "TYPE_HMAC LENGTH_HMAC_SHA_384_BLOCK_64#2.2.2", "TYPE_HMAC LENGTH_HMAC_SHA_512_BLOCK_64#2.2.2",
    };
    
    /**
     * Array of bytes used in KeyBuilder testing.
     */
    public static final byte[] KEYBUILDER_LENGTHS ={
        (byte)0xFF,
        (byte)0x00, (byte)0x40,     // [01] 64
        (byte)0x00, (byte)0x40,     // [02] 64
        (byte)0x00, (byte)0x40,     // [03] 64
        (byte)0x00, (byte)0x80,     // [04] 128
        (byte)0x00, (byte)0xC0,     // [05] 192
        (byte)0x00, (byte)0x80,     // [06] 128
        (byte)0x00, (byte)0x80,     // [07] 128
        (byte)0x00, (byte)0x80,     // [08] 128
        (byte)0x00, (byte)0xC0,     // [09] 192
        (byte)0x01, (byte)0x00,     // [10] 256
        (byte)0x02, (byte)0x00,     // [11] 512
        (byte)0x02, (byte)0xE0,     // [12] 736
        (byte)0x03, (byte)0x00,     // [13] 768
        (byte)0x03, (byte)0x80,     // [14] 896
        (byte)0x04, (byte)0x00,     // [15] 1024
        (byte)0x05, (byte)0x00,     // [16] 1280
        (byte)0x06, (byte)0x00,     // [17] 1536
        (byte)0x07, (byte)0xC0,     // [18] 1984
        (byte)0x08, (byte)0x00,     // [19] 2048
        (byte)0x0C, (byte)0x00,     // [20] 3072
        (byte)0x10, (byte)0x00,     // [21] 4096
        (byte)0x02, (byte)0x00,     // [22] 512
        (byte)0x02, (byte)0xE0,     // [23] 736
        (byte)0x03, (byte)0x00,     // [24] 768
        (byte)0x03, (byte)0x80,     // [25] 896
        (byte)0x04, (byte)0x00,     // [26] 1024
        (byte)0x05, (byte)0x00,     // [27] 1280
        (byte)0x06, (byte)0x00,     // [28] 1536
        (byte)0x07, (byte)0xC0,     // [29] 1984
        (byte)0x08, (byte)0x00,     // [30] 2048
        (byte)0x0C, (byte)0x00,     // [31] 3072
        (byte)0x10, (byte)0x00,     // [32] 4096
        (byte)0x04, (byte)0x00,     // [33] 1024
        (byte)0x04, (byte)0x00,     // [34] 1024
        (byte)0x02, (byte)0x00,     // [35] 512
        (byte)0x02, (byte)0xE0,     // [36] 736
        (byte)0x03, (byte)0x00,     // [37] 768
        (byte)0x03, (byte)0x80,     // [38] 896
        (byte)0x04, (byte)0x00,     // [39] 1024
        (byte)0x05, (byte)0x00,     // [40] 1280
        (byte)0x06, (byte)0x00,     // [41] 1536
        (byte)0x07, (byte)0xC0,     // [42] 1984
        (byte)0x08, (byte)0x00,     // [43] 2048
        (byte)0x0C, (byte)0x00,     // [44] 3072
        (byte)0x10, (byte)0x00,     // [45] 4096
        (byte)0x04, (byte)0x00,     // [46] 1024
        (byte)0x04, (byte)0x00,     // [47] 1024
        (byte)0x02, (byte)0x00,     // [48] 512
        (byte)0x03, (byte)0x00,     // [49] 768
        (byte)0x04, (byte)0x00,     // [50] 1024
        (byte)0x04, (byte)0x00,     // [51] 1024
        (byte)0x04, (byte)0x00,     // [52] 1024
        (byte)0x02, (byte)0x00,     // [53] 512
        (byte)0x03, (byte)0x00,     // [54] 768
        (byte)0x04, (byte)0x00,     // [55] 1024
        (byte)0x00, (byte)0x71,     // [56] 113
        (byte)0x00, (byte)0x83,     // [57] 131
        (byte)0x00, (byte)0xA3,     // [58] 163
        (byte)0x00, (byte)0xC1,     // [59] 193
        (byte)0x00, (byte)0xC1,     // [60] 193
        (byte)0x00, (byte)0xC1,     // [61] 193
        (byte)0x00, (byte)0x70,     // [62] 112
        (byte)0x00, (byte)0x80,     // [63] 128
        (byte)0x00, (byte)0xA0,     // [64] 160
        (byte)0x00, (byte)0xC0,     // [65] 192
        (byte)0x00, (byte)0xE0,     // [66] 224
        (byte)0x01, (byte)0x00,     // [67] 256
        (byte)0x01, (byte)0x80,     // [68] 384
        (byte)0x02, (byte)0x09,     // [69] 521
        (byte)0x00, (byte)0x80,     // [70] 128
        (byte)0x00, (byte)0x80,     // [71] 128
        (byte)0x00, (byte)0x80,     // [72] 128
        (byte)0x00, (byte)0x80,     // [73] 128
        (byte)0x00, (byte)0x80,     // [74] 128
        (byte)0x00, (byte)0x40,     // [75] 64
        (byte)0x00, (byte)0x40,     // [76] 64
        (byte)0x00, (byte)0x40,     // [77] 64
        (byte)0x00, (byte)0x40,     // [78] 64
        (byte)0x00, (byte)0x80,     // [79] 128
        (byte)0x00, (byte)0x80,     // [80] 128
    };
    
    public static final byte[] KEYBUILDER_CONST = {
        (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x03, (byte)0x03, (byte)0x0D, (byte)0x0E, (byte)0x0F, (byte)0x0F, (byte)0x0F,
        (byte)0x04, (byte)0x04, (byte)0x04, (byte)0x04, (byte)0x04, (byte)0x04, (byte)0x04, (byte)0x04, (byte)0x04, (byte)0x04,
        (byte)0x04, (byte)0x05, (byte)0x05, (byte)0x05, (byte)0x05, (byte)0x05, (byte)0x05, (byte)0x05, (byte)0x05, (byte)0x05,
        (byte)0x05, (byte)0x05, (byte)0x16, (byte)0x17, (byte)0x06, (byte)0x06, (byte)0x06, (byte)0x06, (byte)0x06, (byte)0x06,
        (byte)0x06, (byte)0x06, (byte)0x06, (byte)0x06, (byte)0x06, (byte)0x18, (byte)0x19, (byte)0x08, (byte)0x08, (byte)0x08,
        (byte)0x1A, (byte)0x1B, (byte)0x07, (byte)0x07, (byte)0x07, (byte)0x0A, (byte)0x0A, (byte)0x0A, (byte)0x0A, (byte)0x1C,
        (byte)0x1D, (byte)0x0C, (byte)0x0C, (byte)0x0C, (byte)0x0C, (byte)0x0C, (byte)0x0C, (byte)0x0C, (byte)0x0C, (byte)0x1E,
        (byte)0x1F, (byte)0x10, (byte)0x11, (byte)0x12, (byte)0x13, (byte)0x14, (byte)0x15, (byte)0x15, (byte)0x15, (byte)0x15
    };
    
    /**
     * Byte array containing lengths of keys in 'javacard.security.KeyPair' class in hexadecimal form.
     * Every line is one key length value.
     */
    public static final byte[] KEY_LENGTHS_HEX = {
        // class EC FP [00 - 15]
        (byte)0x00, (byte)0x70,         // [00,01] - 112
        (byte)0x00, (byte)0x80,         // [02,03] - 128
        (byte)0x00, (byte)0xA0,         // [04,05] - 160
        (byte)0x00, (byte)0xC0,         // [06,07] - 192
        (byte)0x00, (byte)0xE0,         // [08,09] - 224
        (byte)0x01, (byte)0x00,         // [10,11] - 256
        (byte)0x01, (byte)0x80,         // [12,13] - 384
        (byte)0x02, (byte)0x09,         // [14,15] - 521
        // class EC F2M [16 - 23]
        (byte)0x00, (byte)0x71,         // [16,17] - 113
        (byte)0x00, (byte)0x83,         // [18,19] - 131
        (byte)0x00, (byte)0xA3,         // [20,21] - 163
        (byte)0x00, (byte)0xC1,         // [22,23] - 193
        // classes RSA, RSACRT [24 - 43]
        (byte)0x02, (byte)0x00,         // [24,25] - 512
        (byte)0x02, (byte)0xE0,         // [26,27] - 736
        (byte)0x03, (byte)0x00,         // [28,29] - 768
        (byte)0x03, (byte)0x80,         // [30,31] - 896
        (byte)0x04, (byte)0x00,         // [32,33] - 1024
        (byte)0x05, (byte)0x00,         // [34,35] - 1280
        (byte)0x06, (byte)0x00,         // [36,37] - 1536
        (byte)0x07, (byte)0xC0,         // [38,39] - 1984
        (byte)0x08, (byte)0x00,         // [40,41] - 2048
        (byte)0x10, (byte)0x00,         // [42,43] - 4096
        // class DES [44 - 49]
        (byte)0x00, (byte)0x40,         // [44,45] - 64
        (byte)0x00, (byte)0x80,         // [46,47] - 128
        (byte)0x00, (byte)0xC0,         // [48,49] - 192
        // class AES [50 - 55]
        (byte)0x00, (byte)0x80,         // [50,51] - 128
        (byte)0x00, (byte)0xC0,         // [52,53] - 192
        (byte)0x01, (byte)0x00,         // [54,55] - 256
        // class HMAC [56 - 63]
        (byte)0x00, (byte)0x01,         // [56,57] - 1
        (byte)0x01, (byte)0x00,         // [58,59] - 256
        (byte)0x01, (byte)0x80,         // [60,61] - 384
        (byte)0x02, (byte)0x00,         // [62,63] - 512
    };
    
    public final int CLOCKS_PER_SEC = 1000;
       
    /**
     * Method containing 'menu'.
     * Calls all other methods in this class.
     * @throws IOException
     * @throws Exception
     */
    public void TestSingleAlg (String[] args) throws IOException, Exception{
        /* BUGBUG: we need to figure out how to support JCardSim in nice way (copy of class files, directory structure...)
        Class testClassSingleApdu = AlgTestSinglePerApdu.class;
        */
        Class testClassSingleApdu = null;
        /* Reads text from a character-input stream, buffering characters so as to provide
           for the efficient reading of characters, arrays, and lines. */
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));  
        int answ = 0;   // When set to 0, program will ask for each algorithm to test.
                
        FileOutputStream file = cardManager.establishConnection(testClassSingleApdu);
        
        /* Checking for arguments. */
        if (args.length > 1){       // in case there are arguments from command line present
            if (Arrays.asList(args).contains(TEST_ALL_ALGORITHMS)){testAllAtOnce(file);}
            else if (Arrays.asList(args).contains(TEST_CLASS_CIPHER)){TestClassCipher(file);}
            else if (Arrays.asList(args).contains(TEST_CLASS_CHECKSUM)){TestClassChecksum(file);}
            else if (Arrays.asList(args).contains(TEST_CLASS_SIGNATURE)){TestClassSignature(file);}
            else if (Arrays.asList(args).contains(TEST_CLASS_KEYBUILDER)){TestClassKeyBuilder(file);}
            else if (Arrays.asList(args).contains(TEST_CLASS_MESSAGEDIGEST)){TestClassMessageDigest(file);}
            else if (Arrays.asList(args).contains(TEST_CLASS_RANDOMDATA)){TestClassRandomData(file);}
            else if (Arrays.asList(args).contains(TEST_CLASS_KEYAGREEMENT)){TestClassKeyAgreement(file);}
            else if (Arrays.asList(args).contains(TEST_CLASS_KEYPAIR_ALG_RSA)){TestClassKeyPair_ALG_RSA(file);}
            else if (Arrays.asList(args).contains(TEST_CLASS_KEYPAIR_ALG_RSA_CRT)){TestClassKeyPair_ALG_RSA_CRT(file);}
            else if (Arrays.asList(args).contains(TEST_CLASS_KEYPAIR_ALG_DSA)){TestClassKeyPair_ALG_DSA(file);}
            else if (Arrays.asList(args).contains(TEST_CLASS_KEYPAIR_ALG_EC_F2M)){TestClassKeyPair_ALG_EC_F2M(file);}
            else if (Arrays.asList(args).contains(TEST_CLASS_KEYPAIR_ALG_EC_FP)){TestClassKeyPair_ALG_EC_FP(file);}
            else if (Arrays.asList(args).contains(TEST_RSA_PUBLIC_EXPONENT_SUPPORT)){
                StringBuilder value = new StringBuilder();
                value.setLength(0);
                cardManager.TestVariableRSAPublicExponentSupport(value, file, OFFSET_P2);}
            else{
                System.err.println("Incorect parameter!");
                cardManager.PrintHelp();
            }
        }
        else{       // in case there are no arguments from command line present
            System.out.println("Do you want to test all possible algorithms at once?\n1 = YES, 0 = NO");
            answ = Integer.decode(br.readLine());       
        
            /* Chooses action based on input argument 'answ' (1/0). */
            switch (answ) {
                /* Program will ask for every class. */
                case 0:
                    /* Class 'javacardx.crypto.Cipher'. */
                    System.out.println("Do you want to test algorithms from class 'Cipher'?\n1 = YES, 0 = NO");
                        answ = Integer.decode(br.readLine());
                        if (answ == 1){TestClassCipher(file);}
                        else{ClassSkipped(file, "javacardx.crypto.Cipher");}

                    /* Class 'javacard.security.Singnature'. */
                    System.out.println("Do you want to test algorithms from class 'Signature'?\n1 = YES, 0 = NO");
                        answ = Integer.decode(br.readLine());
                        if (answ == 1){TestClassSignature(file);}
                        else{ClassSkipped(file, "javacard.security.Signature");}

                    /* Class 'javacard.security.MessageDigest'. */
                    System.out.println("Do you want to test algorithms from class 'MessageDigest'?\n1 = YES, 0 = NO");
                        answ = Integer.decode(br.readLine());
                        if (answ == 1){TestClassMessageDigest(file);}
                        else{ClassSkipped(file, "javacard.security.MessageDigest");}

                    /* Class 'javacard.security.RandomData'. */
                    System.out.println("Do you want to test algorithms from class 'RandomData'?\n1 = YES, 0 = NO");
                        answ = Integer.decode(br.readLine());
                        if (answ == 1){TestClassRandomData(file);}
                        else{ClassSkipped(file, "javacard.security.RandomData");}

                    /* Class 'javacard.security.KeyBuilder'. */
                    System.out.println("Do you want to test algorithms from class 'KeyBuilder'?\n1 = YES, 0 = NO");
                        answ = Integer.decode(br.readLine());
                        if (answ == 1){TestClassKeyBuilder(file);}
                        else{ClassSkipped(file, "javacard.security.KeyBuilder");}

                    /* Class 'javacard.security.KeyAgreement'. */
                    System.out.println("Do you want to test algorithms from class 'KeyAgreement'?\n1 = YES, 0 = NO");
                        answ = Integer.decode(br.readLine());
                        if (answ == 1){TestClassKeyAgreement(file);}
                        else{ClassSkipped(file, "javacard.security.KeyAgreement");}

                    /* Class 'javacard.security.Checksum'. */
                    System.out.println("Do you want to test algorithms from class 'Checksum'?\n1 = YES, 0 = NO");
                        answ = Integer.decode(br.readLine());
                        if (answ == 1){TestClassChecksum(file);}
                        else{ClassSkipped(file, "javacard.security.Checksum");}

                    /* Class 'javacard.security.KeyPair_RSA'. */
                    System.out.println("Do you want to test algorithms from class 'javacard.security.KeyPair ALG_RSA on-card generation'?\n1 = YES, 0 = NO");
                        answ = Integer.decode(br.readLine());
                        if (answ == 1){TestClassKeyPair_ALG_RSA(file);}
                        else{ClassSkipped(file, "javacard.security.KeyPair ALG_RSA on-card generation");}

                    /* Class 'javacard.security.KeyPair_RSA_CRT'. */
                    System.out.println("Do you want to test algorithms from class 'javacard.security.KeyPair ALG_RSA_CRT on-card generation'?\n1 = YES, 0 = NO");
                        answ = Integer.decode(br.readLine());
                        if (answ == 1){TestClassKeyPair_ALG_RSA_CRT(file);}
                        else{ClassSkipped(file, "javacard.security.KeyPair ALG_RSA_CRT on-card generation");}   

                    /* Class 'javacard.security.KeyPair_DSA'. */
                    System.out.println("Do you want to test algorithms from class 'javacard.security.KeyPair ALG_DSA on-card generation'?\n1 = YES, 0 = NO");
                        answ = Integer.decode(br.readLine());
                        if (answ == 1){TestClassKeyPair_ALG_DSA(file);}
                        else{ClassSkipped(file, "javacard.security.KeyPair ALG_DSA on-card generation");} 

                    /* Class 'javacard.security.KeyPair_DSA'. */
                    System.out.println("Do you want to test algorithms from class 'javacard.security.KeyPair ALG_EC_F2M on-card generation'?\n1 = YES, 0 = NO");
                        answ = Integer.decode(br.readLine());
                        if (answ == 1){TestClassKeyPair_ALG_EC_F2M(file);}
                        else{ClassSkipped(file, "javacard.security.KeyPair ALG_EC_F2M on-card generation");}

                    /* Class 'javacard.security.KeyPair_DSA'. */
                    System.out.println("Do you want to test algorithms from class 'javacard.security.KeyPair ALG_EC_FP on-card generation'?\n1 = YES, 0 = NO");
                        answ = Integer.decode(br.readLine());
                        if (answ == 1){TestClassKeyPair_ALG_EC_FP(file);}
                        else{ClassSkipped(file, "javacard.security.KeyPair ALG_EC_FP on-card generation");}
                    /* RSA exponent */
                    System.out.println("\n\nQ: Do you like to test support for variable RSA public exponent?\n1 = YES, 0 = NO");
                        answ = Integer.decode(br.readLine());
                        if (answ == 1) {
                        // Variable public exponent
                        StringBuilder value = new StringBuilder();
                        value.setLength(0);
                        cardManager.TestVariableRSAPublicExponentSupport(value, file, (byte) 0);}
                        else{
                            String message = "\nERROR: Test variable public exponent support fail\n"; 
                            System.out.println(message); file.write(message.getBytes());
                        }
                    /* Closing file. */
                    CloseFile(file);
                break;

                /* Program will test all algorithms at once. */
                case 1:
                    /*
                    TestClassCipher(file);
                    TestClassSignature(file);
                    TestClassMessageDigest(file);
                    TestClassRandomData(file);
                    TestClassKeyBuilder(file);
                    TestClassKeyAgreement(file);
                    TestClassChecksum(file);
                    TestClassKeyPair_ALG_RSA(file);
                    TestClassKeyPair_ALG_RSA_CRT(file);
                    TestClassKeyPair_ALG_DSA(file);
                    TestClassKeyPair_ALG_EC_F2M(file);
                    TestClassKeyPair_ALG_EC_FP(file);
                    */
                    testAllAtOnce(file);
                    CloseFile(file);
                break;

                /* In case of wrong argument. */
                default:
                    System.err.println("First argument must be 0 or 1!");
                break;
            }
        }
    }
    
    /**
     * Closes file given in parameter.
     * @param file FileOutputStream object to close.
     */
    public void CloseFile(FileOutputStream file){
        try {
            file.close();
        } catch (IOException ex) {
            Logger.getLogger(SingleModeTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Prints to the file and on the screen message about skipping specified algorithm class.
     * @param file FileOutputStream object containing output file.
     * @param className Name of the skipped class.
     * @throws IOException
     */
    public void ClassSkipped(FileOutputStream file, String className) throws IOException{
        /* Message to be send on the screen and to the output file. */
        String message = "Testing of algorithm class " + className + " skipped by user\r\n";
        /* Prints given message on screen and in output file. */
        System.out.println(message);
        file.write(message.getBytes());
    }
    
    /**
     * Checks result of algorithm testing on smart card.
     * @param file FileOutputStream object containing output file.
     * @param name String containing algorithm name.
     * @param response Response byte of APDU (second byte of incoming APDU) .
     * @throws IOException
     */
    public void CheckResult (FileOutputStream file, String name, byte response, long elapsedCard) throws IOException{
        String message = "";
        String elTimeStr = "";
        switch (response){
            case SUPP_ALG_SUPPORTED:
                elTimeStr = String.format("%1f", (double) elapsedCard / (float) CLOCKS_PER_SEC);
                message += name + ";" + "yes;" + elTimeStr + "\r\n";
                System.out.println(message);
                file.write(message.getBytes());
            break;
            
            case NO_SUCH_ALGORITHM:
                message += name + ";" + "no;" + "\r\n";
                System.out.println(message);
                file.write(message.getBytes());
            break;
                
            case ILLEGAL_USE:
                message += name + ";" + "error(ILLEGAL_USE);" + "\r\n";
                System.out.println(message);
                file.write(message.getBytes());
            break;
                
            case ILLEGAL_VALUE:
                message += name + ";" + "error(ILLEGAL_VALUE);" + "\r\n";
                System.out.println(message);
                file.write(message.getBytes());
            break;
                
            case INVALID_INIT:
                message += name + ";" + "error(INVALID_INIT);" + "\r\n";
                System.out.println(message);
                file.write(message.getBytes());
            break;
                
            case UNINITIALIZED_KEY:
                message += name + ";" + "error(UNINITIALIZED_KEY);" + "\r\n";
                System.out.println(message);
                file.write(message.getBytes());
            break;
            
            case 0x6f:
                message += name + ";" + "maybe;" + "\r\n";
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
     * Tests all algorithms in class 'javacardx.crypto.Cipher' and results writes into the output file.
     * @param file FileOutputStream object containing output file.
     * @throws Exception
     */
    public void TestClassCipher(FileOutputStream file) throws Exception{
        long       elapsedCard = 0;
        byte[] apdu = new byte[6];
            apdu[OFFSET_CLA] = (byte)0xB0;  // for AlgTest applet
            apdu[OFFSET_INS] = (byte)0x75;  // for AlgTest applet switch to 'TestSupportedModeSingle'
            apdu[OFFSET_P1] = CLASS_CIPHER;   // 0x11
            apdu[OFFSET_P2] = (byte)0x00;
            apdu[OFFSET_LC] = (byte)0x01;

        /* Creates message with class name and writes it in the output file and on the screen. */
        String message = "\n" + cardManager.GetAlgorithmName(Consts.CIPHER_STR[0]) + "\r\n";
        System.out.println(message);
        file.write(message.getBytes());
        
        for (int i=1; i< Consts.CIPHER_STR.length; i++){    // i = 1 because Cipher[0] is class name
            // get starting time of communication cycle
            elapsedCard = -System.currentTimeMillis();
            apdu[OFFSET_DATA] = (byte)i;
            ResponseAPDU response = cardManager.sendAPDU(apdu);
            
            byte[] resp = response.getBytes();
            System.out.println("RESPONSE: " + resp[0]);
            
            if(response.getSW() == 0x9000){
                // save time of card response
                elapsedCard += System.currentTimeMillis();
            }
            /* Calls method CheckResult - should add to output error messages. */
            CheckResult(file, cardManager.GetAlgorithmName(Consts.CIPHER_STR[i]), resp[1], elapsedCard);
        }        
    }
    
    /**
     * Tests all algorithms in class 'javacardx.crypto.Signature' and results writes into the output file.
     * @param file FileOutputStream object containing output file.
     * @throws IOException
     * @throws Exception
     */
    public void TestClassSignature (FileOutputStream file) throws IOException, Exception{
        long       elapsedCard = 0;
        byte[] apdu = new byte[6];
            apdu[OFFSET_CLA] = (byte)0xB0;  // for AlgTest applet
            apdu[OFFSET_INS] = (byte)0x75;  // for AlgTest applet switch to 'TestSupportedModeSingle'
            apdu[OFFSET_P1] = CLASS_SIGNATURE;   // 0x12
            apdu[OFFSET_P2] = (byte)0x00;
            apdu[OFFSET_LC] = (byte)0x01;
        
        /* Creates message with class name and writes it in the output file and on the screen. */
        String message = "\n" + cardManager.GetAlgorithmName(Consts.SIGNATURE_STR[0]) + "\r\n";
        System.out.println(message);
        file.write(message.getBytes());
        
        for (int i=1; i<Consts.SIGNATURE_STR.length; i++){    // i = 1 because Signature[0] is class name
            // get starting time of communication cycle
            elapsedCard = -System.currentTimeMillis();
            apdu[OFFSET_DATA] = (byte)i;
            ResponseAPDU response = cardManager.sendAPDU(apdu);
            byte[] resp = response.getBytes();
            
            if(response.getSW() == 0x9000){
                // save time of card response
                elapsedCard += System.currentTimeMillis();
            }
            /* Calls method CheckResult - should add to output error messages. */
            CheckResult(file, cardManager.GetAlgorithmName(Consts.SIGNATURE_STR[i]), resp[1], elapsedCard);
        }        
    }
    
    /**
     * Tests all algorithms in class 'javacardx.crypto.MessageDigest' and results writes into the output file.
     * @param file FileOutputStream object containing output file.
     * @throws IOException
     * @throws Exception
     */
    public void TestClassMessageDigest (FileOutputStream file) throws IOException, Exception{
        long       elapsedCard = 0;
        byte[] apdu = new byte[6];
            apdu[OFFSET_CLA] = (byte)0xB0;  // for AlgTest applet
            apdu[OFFSET_INS] = (byte)0x75;  // for AlgTest applet switch to 'TestSupportedModeSingle'
            apdu[OFFSET_P1] = CLASS_MESSAGEDIGEST;   // 0x15
            apdu[OFFSET_P2] = (byte)0x00;
            apdu[OFFSET_LC] = (byte)0x01;
        
        /* Creates message with class name and writes it in the output file and on the screen. */
        String message = "\n" + cardManager.GetAlgorithmName(Consts.MESSAGEDIGEST_STR[0]) + "\r\n";
        System.out.println(message);
        file.write(message.getBytes());
        
        for (int i=1; i<Consts.MESSAGEDIGEST_STR.length; i++){    // i = 1 because MessageDigest[0] is class name
            // get starting time of communication cycle
            elapsedCard = -System.currentTimeMillis();
            apdu[OFFSET_DATA] = (byte)i;
            ResponseAPDU response = cardManager.sendAPDU(apdu);
            byte[] resp = response.getBytes();
            
            if(response.getSW() == 0x9000){
                // save time of card response
                elapsedCard += System.currentTimeMillis();
            }
            /* Calls method CheckResult - should add to output error messages. */
            CheckResult(file, cardManager.GetAlgorithmName(Consts.MESSAGEDIGEST_STR[i]), resp[1], elapsedCard);
        }
    }
    
    /**
     * Tests all algorithms in class 'javacardx.crypto.RandomData' and results writes into the output file.
     * @param file FileOutputStream object containing output file.
     * @throws IOException
     * @throws Exception
     */
    public void TestClassRandomData (FileOutputStream file) throws IOException, Exception{
        long       elapsedCard = 0;
        byte[] apdu = new byte[6];
            apdu[OFFSET_CLA] = (byte)0xB0;  // for AlgTest applet
            apdu[OFFSET_INS] = (byte)0x75;  // for AlgTest applet switch to 'TestSupportedModeSingle'
            apdu[OFFSET_P1] = CLASS_RANDOMDATA;   // 0x16
            apdu[OFFSET_P2] = (byte)0x00;
            apdu[OFFSET_LC] = (byte)0x01;
        
        /* Creates message with class name and writes it in the output file and on the screen. */
        String message = "\r\n" + cardManager.GetAlgorithmName(Consts.RANDOMDATA_STR[0]) + "\r\n";
        System.out.println(message);
        file.write(message.getBytes());
        
        for (int i=1; i<Consts.RANDOMDATA_STR.length; i++){    // i = 1 because RandomData[0] is class name
            // get starting time of communication cycle
            elapsedCard = -System.currentTimeMillis();
            apdu[OFFSET_DATA] = (byte)i;
            ResponseAPDU response = cardManager.sendAPDU(apdu);
            byte[] resp = response.getBytes();
            if(response.getSW() == 0x9000){
                // save time of card response
                elapsedCard += System.currentTimeMillis();
            }
            /* Calls method CheckResult - should add to output error messages. */
            CheckResult(file, cardManager.GetAlgorithmName(Consts.RANDOMDATA_STR[i]), resp[1], elapsedCard);
        }
    }
    
    /**
     * Tests all algorithms in class 'javacardx.crypto.KeyBuilder' and results writes into the output file.
     * @param file FileOutputStream object containing output file.
     * @throws IOException
     * @throws Exception
     */
    public void TestClassKeyBuilder (FileOutputStream file) throws IOException, Exception{
        long       elapsedCard = 0;
        byte[] apdu = new byte[8];
            apdu[OFFSET_CLA] = (byte)0xB0;  // for AlgTest applet
            apdu[OFFSET_INS] = (byte)0x75;  // for AlgTest applet switch to 'TestSupportedModeSingle'
            apdu[OFFSET_P1] = CLASS_KEYBUILDER;   // 0x20
            apdu[OFFSET_P2] = (byte)0x00;
            apdu[OFFSET_LC] = (byte)0x03;
            
        /* Creates message with class name and writes it in the output file and on the screen. */
        String message = "\n" + cardManager.GetAlgorithmName(KEYBUILDER_STR[0]) + "\r\n";
        System.out.println(message);
        file.write(message.getBytes());

        for (int i = 1; i < KEYBUILDER_STR.length; i++){
            
                // get starting time of communication cycle
                elapsedCard = -System.currentTimeMillis();
                // byte to choose subclass
                apdu[OFFSET_DATA] = (byte)KEYBUILDER_CONST[i-1];    // (byte)3 => TYPE DES
                // bytes to carry the length of tested key
                apdu[OFFSET_DATA + 1] = KEYBUILDER_LENGTHS[(i*2)-1];
                apdu[OFFSET_DATA + 2] = KEYBUILDER_LENGTHS[(i*2)];
                
                ResponseAPDU response = cardManager.sendAPDU(apdu);
                byte[] resp = response.getBytes();
            
                if(response.getSW() == 0x9000){
                    // save time of card response
                    elapsedCard += System.currentTimeMillis();
                }
                /* Calls method CheckResult - should add to output error messages. */
                CheckResult(file, cardManager.GetAlgorithmName(KEYBUILDER_STR[i]), resp[1], elapsedCard);
            
        }
    }
    
    /**
     * Tests all algorithms in class 'javacardx.crypto.KeyAgreement' and results writes into the output file.
     * @param file FileOutputStream object containing output file.
     * @throws IOException
     * @throws Exception
     */
    public void TestClassKeyAgreement (FileOutputStream file) throws IOException, Exception{
        long       elapsedCard = 0;
        byte[] apdu = new byte[6];
            apdu[OFFSET_CLA] = (byte)0xB0;  // for AlgTest applet
            apdu[OFFSET_INS] = (byte)0x75;  // for AlgTest applet switch to 'TestSupportedModeSingle'
            apdu[OFFSET_P1] = CLASS_KEYAGREEMENT;   // 0x13
            apdu[OFFSET_P2] = (byte)0x00;
            apdu[OFFSET_LC] = (byte)0x01;
    
        /* Creates message with class name and writes it in the output file and on the screen. */
        String message = "\n" + cardManager.GetAlgorithmName(Consts.KEYAGREEMENT_STR[0]) + "\r\n";
        System.out.println(message);
        file.write(message.getBytes());
        
        for (int i=1; i<Consts.KEYAGREEMENT_STR.length; i++){    // i = 1 because KeyAgreement[0] is class name
            // get starting time of communication cycle
            elapsedCard = -System.currentTimeMillis();
            apdu[OFFSET_DATA] = (byte)i;
            ResponseAPDU response = cardManager.sendAPDU(apdu);
            byte[] resp = response.getBytes();
            
            if(response.getSW() == 0x9000){
                // save time of card response
                elapsedCard += System.currentTimeMillis();
            }
            /* Calls method CheckResult - should add to output error messages. */
            CheckResult(file, cardManager.GetAlgorithmName(Consts.KEYAGREEMENT_STR[i]), resp[1], elapsedCard);
        }
    }
    
    /**
     * Tests all algorithms in class 'javacardx.crypto.Checksum' and results writes into the output file.
     * @param file FileOutputStream object containing output file.
     * @throws IOException
     * @throws Exception
     */
    public void TestClassChecksum (FileOutputStream file) throws IOException, Exception{
        long       elapsedCard = 0;
        byte[] apdu = new byte[6];
            apdu[OFFSET_CLA] = (byte)0xB0;  // for AlgTest applet
            apdu[OFFSET_INS] = (byte)0x75;  // for AlgTest applet switch to 'TestSupportedModeSingle'
            apdu[OFFSET_P1] = CLASS_CHECKSUM;   // 0x17
            apdu[OFFSET_P2] = (byte)0x00;
            apdu[OFFSET_LC] = (byte)0x01;
    
        /* Creates message with class name and writes it in the output file and on the screen. */
        String message = "\n" + cardManager.GetAlgorithmName(Consts.CHECKSUM_STR[0]) + "\r\n";
        System.out.println(message);
        file.write(message.getBytes());
        
        for (int i=1; i<Consts.CHECKSUM_STR.length; i++){    // i = 1 because Checksum[0] is class name
            // get starting time of communication cycle
            elapsedCard = -System.currentTimeMillis();
            apdu[OFFSET_DATA] = (byte)i;
            ResponseAPDU response = cardManager.sendAPDU(apdu);
            byte[] resp = response.getBytes();
            
            if(response.getSW() == 0x9000){
                // save time of card response
                elapsedCard += System.currentTimeMillis();
            }
            /* Calls method CheckResult - should add to output error messages. */
            CheckResult(file, cardManager.GetAlgorithmName(Consts.CHECKSUM_STR[i]), resp[1], elapsedCard);
        }
    }
    
    /**
     * Tests all algorithms in class 'javacard.security.KeyPair_RSA' and results writes into the output file.
     * @param file FileOutputStream object containing output file.
     * @throws IOException
     * @throws Exception
     */
    public void TestClassKeyPair_ALG_RSA (FileOutputStream file) throws IOException, Exception{
        long       elapsedCard = 0;
        byte[] apdu = new byte[8];
            apdu[OFFSET_CLA] = (byte)0xB0;      // for AlgTest applet
            apdu[OFFSET_INS] = (byte)0x75;      // for AlgTest applet switch to 'TestSupportedModeSingle'
            apdu[OFFSET_P1] = CLASS_KEYPAIR;    // 0x19
            apdu[OFFSET_P2] = (byte)0x00;
            apdu[OFFSET_LC] = (byte)0x03;
            apdu[OFFSET_DATA] = ALG_RSA;        // 1
        
        /* Creates message with class name and writes it in the output file and on the screen. */
        String message = "\n" + cardManager.GetAlgorithmName(Consts.KEYPAIR_RSA_STR[0]) + "\r\n";
        System.out.println(message);
        file.write(message.getBytes());
        
        int counter = 24;
        for (int i=1; i<Consts.KEYPAIR_RSA_STR.length; i++){    // i = 1 because KeyPair_RSA_STR[0] is class name
            // get starting time of communication cycle
            elapsedCard = -System.currentTimeMillis();
            apdu[OFFSET_DATA + 1] = KEY_LENGTHS_HEX[counter];
            apdu[OFFSET_DATA + 2] = KEY_LENGTHS_HEX[counter + 1];
            counter = counter + 2;

            ResponseAPDU response = cardManager.sendAPDU(apdu);
            byte[] resp = response.getBytes();
            
            if(response.getSW() == 0x9000){
                // save time of card response
                elapsedCard += System.currentTimeMillis();
            }
            /* Calls method CheckResult - should add to output error messages. */
            CheckResult(file, cardManager.GetAlgorithmName(Consts.KEYPAIR_RSA_STR[i]), resp[1], elapsedCard);
        }
    }
    
    /**
     * Tests all algorithms in class 'javacard.security.KeyPair_RSA_CRT' and results writes into the output file.
     * @param file FileOutputStream object containing output file.
     * @throws IOException
     * @throws Exception
     */
    public void TestClassKeyPair_ALG_RSA_CRT (FileOutputStream file) throws IOException, Exception{
        long       elapsedCard = 0;
        byte[] apdu = new byte[8];
        apdu[OFFSET_CLA] = (byte)0xB0;      // for AlgTest applet
        apdu[OFFSET_INS] = (byte)0x75;      // for AlgTest applet switch to 'TestSupportedModeSingle'
        apdu[OFFSET_P1] = CLASS_KEYPAIR;    // 0x19
        apdu[OFFSET_P2] = (byte)0x00;
        apdu[OFFSET_LC] = (byte)0x03;
        apdu[OFFSET_DATA] = ALG_RSA_CRT;    // 2
        
        /* Creates message with class name and writes it in the output file and on the screen. */
        String message = "\n" + cardManager.GetAlgorithmName(Consts.KEYPAIR_RSACRT_STR[0]) + "\r\n";
        System.out.println(message);
        file.write(message.getBytes());
        
        int counter = 24;
        for (int i=1; i<Consts.KEYPAIR_RSACRT_STR.length; i++){    // i = 1 because KeyPair_RSACRT_STR[0] is class name
            // get starting time of communication cycle
            elapsedCard = -System.currentTimeMillis();
            
            apdu[OFFSET_DATA + 1] = KEY_LENGTHS_HEX[counter];
            apdu[OFFSET_DATA + 2] = KEY_LENGTHS_HEX[counter + 1];
            counter = counter + 2;
            
            ResponseAPDU response = cardManager.sendAPDU(apdu);
            byte[] resp = response.getBytes();
            
            if(response.getSW() == 0x9000){
                // save time of card response
                elapsedCard += System.currentTimeMillis();
            }
            /* Calls method CheckResult - should add to output error messages. */
            CheckResult(file, cardManager.GetAlgorithmName(Consts.KEYPAIR_RSACRT_STR[i]), resp[1], elapsedCard);
        }
    }
    
    /**
     * Tests all algorithms in class 'javacard.security.KeyPair_DSA' and results writes into the output file.
     * @param file FileOutputStream object containing output file.
     * @throws IOException
     * @throws Exception
     */
    public void TestClassKeyPair_ALG_DSA (FileOutputStream file) throws IOException, Exception{
        long       elapsedCard = 0;
        byte[] apdu = new byte[8];
        apdu[OFFSET_CLA] = (byte)0xB0;      // for AlgTest applet
        apdu[OFFSET_INS] = (byte)0x75;      // for AlgTest applet switch to 'TestSupportedModeSingle'
        apdu[OFFSET_P1] = CLASS_KEYPAIR;    // 0x19
        apdu[OFFSET_P2] = (byte)0x00;
        apdu[OFFSET_LC] = (byte)0x03;
        apdu[OFFSET_DATA] = ALG_DSA;        // 3
        
        /* Creates message with class name and writes it in the output file and on the screen. */
        String message = "\n" + cardManager.GetAlgorithmName(Consts.KEYPAIR_DSA_STR[0]) + "\r\n";
        System.out.println(message);
        file.write(message.getBytes());
        
        int counter = 24;
        for (int i=1; i<Consts.KEYPAIR_DSA_STR.length; i++){    // i = 1 because KeyPair_DSA_STR[0] is class name
            // get starting time of communication cycle
            elapsedCard = -System.currentTimeMillis();
            apdu[OFFSET_DATA + 1] = KEY_LENGTHS_HEX[counter];
            apdu[OFFSET_DATA + 2] = KEY_LENGTHS_HEX[counter + 1];
            counter = counter + 4;
            
            ResponseAPDU response = cardManager.sendAPDU(apdu);
            byte[] resp = response.getBytes();
            
            if(response.getSW() == 0x9000){
                // save time of card response
                elapsedCard += System.currentTimeMillis();
            }
            /* Calls method CheckResult - should add to output error messages. */
            CheckResult(file, cardManager.GetAlgorithmName(Consts.KEYPAIR_DSA_STR[i]), resp[1], elapsedCard);
        }
    }
    
    /**
     * Tests all algorithms in class 'javacard.security.KeyPair_EC_F2M' and results writes into the output file.
     * @param file FileOutputStream object containing output file.
     * @throws IOException
     * @throws Exception
     */
    public void TestClassKeyPair_ALG_EC_F2M (FileOutputStream file) throws IOException, Exception{
        long       elapsedCard = 0;
        byte[] apdu = new byte[8];
        apdu[OFFSET_CLA] = (byte)0xB0;      // for AlgTest applet
        apdu[OFFSET_INS] = (byte)0x75;      // for AlgTest applet switch to 'TestSupportedModeSingle'
        apdu[OFFSET_P1] = CLASS_KEYPAIR;    // 0x19
        apdu[OFFSET_P2] = (byte)0x00;
        apdu[OFFSET_LC] = (byte)0x03;
        apdu[OFFSET_DATA] = ALG_EC_F2M;     // 4
        
        /* Creates message with class name and writes it in the output file and on the screen. */
        String message = "\n" + cardManager.GetAlgorithmName(Consts.KEYPAIR_EC_F2M_STR[0]) + "\r\n";
        System.out.println(message);
        file.write(message.getBytes());
        
        int counter = 16;
        for (int i=1; i<Consts.KEYPAIR_EC_F2M_STR.length; i++){    // i = 1 because KeyPair_EC_F2M_STR[0] is class name
            // get starting time of communication cycle
            elapsedCard = -System.currentTimeMillis();
            apdu[OFFSET_DATA + 1] = KEY_LENGTHS_HEX[counter];
            apdu[OFFSET_DATA + 2] = KEY_LENGTHS_HEX[counter + 1];
            counter = counter + 4;
            
            ResponseAPDU response = cardManager.sendAPDU(apdu);
            byte[] resp = response.getBytes();
            
            if(response.getSW() == 0x9000){
                // save time of card response
                elapsedCard += System.currentTimeMillis();
            }
            /* Calls method CheckResult - should add to output error messages. */
            CheckResult(file, cardManager.GetAlgorithmName(Consts.KEYPAIR_EC_F2M_STR[i]), resp[1], elapsedCard);
        }
    }
    
    /**
     * Tests all algorithms in class 'javacard.security.KeyPair_EC_FP' and results writes into the output file.
     * @param file FileOutputStream object containing output file.
     * @throws IOException
     * @throws Exception
     */
    public void TestClassKeyPair_ALG_EC_FP (FileOutputStream file) throws IOException, Exception{
        long       elapsedCard = 0;
        byte[] apdu = new byte[8];
        apdu[OFFSET_CLA] = (byte)0xB0;      // for AlgTest applet
        apdu[OFFSET_INS] = (byte)0x75;      // for AlgTest applet switch to 'TestSupportedModeSingle'
        apdu[OFFSET_P1] = CLASS_KEYPAIR;    // 0x19
        apdu[OFFSET_P2] = (byte)0x00;
        apdu[OFFSET_LC] = (byte)0x03;
        apdu[OFFSET_DATA] = ALG_EC_FP;      // 5
        
        /* Creates message with class name and writes it in the output file and on the screen */
        String message = "\n" + cardManager.GetAlgorithmName(Consts.KEYPAIR_EC_FP_STR[0]) + "\r\n";
        System.out.println(message);
        file.write(message.getBytes());
        
        int counter = 0;
        for (int i=1; i<Consts.KEYPAIR_EC_FP_STR.length; i++){    // i = 1 because KeyPair_EC_FP_STR[0] is class name
            // get starting time of communication cycle
            elapsedCard = -System.currentTimeMillis();
            apdu[OFFSET_DATA + 1] = KEY_LENGTHS_HEX[counter];
            apdu[OFFSET_DATA + 2] = KEY_LENGTHS_HEX[counter + 1];
            counter = counter + 2;
            
            ResponseAPDU response = cardManager.sendAPDU(apdu);
            byte[] resp = response.getBytes();
            
            if(response.getSW() == 0x9000){
                // save time of card response
                elapsedCard += System.currentTimeMillis();
            }
            /* Calls method CheckResult - should add to output error messages. */
            CheckResult(file, cardManager.GetAlgorithmName(Consts.KEYPAIR_EC_FP_STR[i]), resp[1], elapsedCard);
        }
    }
    
    /**
     * Method that will test all algorithms in SingleModeTest class.
     * @param file FileOutputStream object containing file for output data.
     * @throws Exception
     */
    public void testAllAtOnce (FileOutputStream file) throws Exception{
        TestClassCipher(file);
        TestClassSignature(file);
        TestClassMessageDigest(file);
        TestClassRandomData(file);
        TestClassKeyBuilder(file);
        TestClassKeyAgreement(file);
        TestClassChecksum(file);
        TestClassKeyPair_ALG_RSA(file);
        TestClassKeyPair_ALG_RSA_CRT(file);
        TestClassKeyPair_ALG_DSA(file);
        TestClassKeyPair_ALG_EC_F2M(file);
        TestClassKeyPair_ALG_EC_FP(file);
        // test RSA exponent
        StringBuilder value = new StringBuilder();
        value.setLength(0);
        cardManager.TestVariableRSAPublicExponentSupport(value, file, OFFSET_P2);

        CloseFile(file);
    }
}   // END OF CLASS 'SINGLEMODETEST'


