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

/* BUGBUG: we need to figure out how to support JCardSim in nice way (copy of class files, directory structure...)
import com.licel.jcardsim.io.CAD;
import com.licel.jcardsim.io.JavaxSmartCardInterface;
*/
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
/* BUGBUG: we need to figure out how to support JCardSim in nice way (copy of class files, directory structure...)
//import javacard.framework.AID;
*/
import javax.smartcardio.*;

/**
 *
 * @author petrs
 */
public class CardMngr {
    /* Argument constants for choosing algorithm to test. */
    
    /* Arguments for choosing which AlgTest version to run. */
    public static final String ALGTEST_MULTIPERAPDU = "AT_MULTIPERAPDU";        // for 'old' AlgTest
    public static final String ALGTEST_SINGLEPERAPDU = "AT_SINGLEPERAPDU";      // for 'New' AlgTest
    public static final String ALGTEST_PERFORMANCE = "AT_PERFORMANCE";          // for performance testing
    
    /* Argument 'ALL_ALGS' work with every class. */
    public static final String TEST_ALL_ALGORITHMS = "ALL_ALGS";
    
    /* Following arguments work with SinglePerApdu and MultiPerApdu. */
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
    
    /* Following arguments work with PerformanceTesting. */
    public static final String TEST_EEPROM = "EEPROM";
    public static final String TEST_RAM = "RAM";
    public static final String TEST_EXTENDEDAPDU = "EXTENDEDAPDU";
    public static final String TEST_RSAEXPONENT = "RSAEXPONENT";

    public static final int MAX_SUPP_ALG            = 240;    
    public static final byte SUPP_ALG_UNTOUCHED     = (byte) 0xf0;
    public final static byte SUPP_ALG_SUPPORTED     = (byte) 0x00;
    public static final short SUPP_ALG_SEPARATOR        = 0xff;
    public static final short SUPP_ALG_SMALL_SEPARATOR  = 0xb0;    
    public final static byte EXCEPTION_CODE_OFFSET  = (byte) 0;
    
    public static final byte ALGTEST_AID_LEN       = 9;
    public static final byte FORCE_TEST = 1;        // variable to force test of alg in given method

    /* Byte values for choosing method on on-card app. */
    public static final byte CLASS_CIPHER          = 0x11;
    public static final byte CLASS_SIGNATURE       = 0x12;
    public static final byte CLASS_KEYAGREEMENT    = 0x13;
    public static final byte CLASS_MESSAGEDIGEST   = 0x15;
    public static final byte CLASS_RANDOMDATA      = 0x16;
    public static final byte CLASS_CHECKSUM        = 0x17;
    public static final byte CLASS_KEYPAIR_RSA     = 0x18;
    public static final byte CLASS_KEYPAIR_RSA_CRT = 0x19;
    public static final byte CLASS_KEYBUILDER      = 0x20;
    public static final byte CLASS_KEYPAIR_DSA     = 0x1A;
    public static final byte CLASS_KEYPAIR_EC_F2M  = 0x1B;
    public static final byte CLASS_KEYPAIR_EC_FP   = 0x1C;

      //  
      //Class javacard.security.Signature
      //
    public static final byte ALG_DES_MAC4_NOPAD                  = 1;
    public static final byte ALG_DES_MAC8_NOPAD                  = 2;
    public static final byte ALG_DES_MAC4_ISO9797_M1             = 3;
    public static final byte ALG_DES_MAC8_ISO9797_M1             = 4;
    public static final byte ALG_DES_MAC4_ISO9797_M2             = 5;
    public static final byte ALG_DES_MAC8_ISO9797_M2             = 6;
    public static final byte ALG_DES_MAC4_PKCS5                  = 7;
    public static final byte ALG_DES_MAC8_PKCS5                  = 8;
    public static final byte ALG_RSA_SHA_ISO9796                 = 9;
    public static final byte ALG_RSA_SHA_PKCS1                   = 10;
    public static final byte ALG_RSA_MD5_PKCS1                   = 11;
    public static final byte ALG_RSA_RIPEMD160_ISO9796           = 12;
    public static final byte ALG_RSA_RIPEMD160_PKCS1             = 13;
    public static final byte ALG_DSA_SHA                         = 14;
    public static final byte ALG_RSA_SHA_RFC2409                 = 15;
    public static final byte ALG_RSA_MD5_RFC2409                 = 16;
    public static final byte ALG_ECDSA_SHA                       = 17;
    public static final byte ALG_AES_MAC_128_NOPAD               = 18;
    public static final byte ALG_DES_MAC4_ISO9797_1_M2_ALG3      = 19;
    public static final byte ALG_DES_MAC8_ISO9797_1_M2_ALG3      = 20;
    public static final byte ALG_RSA_SHA_PKCS1_PSS               = 21;
    public static final byte ALG_RSA_MD5_PKCS1_PSS               = 22;
    public static final byte ALG_RSA_RIPEMD160_PKCS1_PSS         = 23;
      // JC2.2.2
    public static final byte ALG_HMAC_SHA1                       = 24;
    public static final byte ALG_HMAC_SHA_256                    = 25;
    public static final byte ALG_HMAC_SHA_384                    = 26;
    public static final byte ALG_HMAC_SHA_512                    = 27;
    public static final byte ALG_HMAC_MD5                        = 28;
    public static final byte ALG_HMAC_RIPEMD160                  = 29;
    public static final byte ALG_RSA_SHA_ISO9796_MR              = 30;
    public static final byte ALG_RSA_RIPEMD160_ISO9796_MR        = 31;
    public static final byte ALG_KOREAN_SEED_MAC_NOPAD = 32;
    // JC3.0.1
    public static final byte ALG_ECDSA_SHA_256 = 33;  
    public static final byte ALG_ECDSA_SHA_384 = 34;  
    public static final byte ALG_AES_MAC_192_NOPAD = 35;  
    public static final byte ALG_AES_MAC_256_NOPAD = 36;  
    public static final byte ALG_ECDSA_SHA_224 = 37;  
    public static final byte ALG_ECDSA_SHA_512 = 38;  
    public static final byte ALG_RSA_SHA_224_PKCS1 = 39;  
    public static final byte ALG_RSA_SHA_256_PKCS1 = 40;  
    public static final byte ALG_RSA_SHA_384_PKCS1 = 41;  
    public static final byte ALG_RSA_SHA_512_PKCS1 = 42;  
    public static final byte ALG_RSA_SHA_224_PKCS1_PSS = 43;  
    public static final byte ALG_RSA_SHA_256_PKCS1_PSS = 44;  
    public static final byte ALG_RSA_SHA_384_PKCS1_PSS = 45;  
    public static final byte ALG_RSA_SHA_512_PKCS1_PSS = 46;  
    // JC3.0.4
    public static final byte ALG_DES_MAC4_ISO9797_1_M1_ALG3 = 47;   
    public static final byte ALG_DES_MAC8_ISO9797_1_M1_ALG3 = 48;
    
    public static final String SIGNATURE_STR[] = {"javacard.crypto.Signature", 
        "ALG_DES_MAC4_NOPAD#<=2.1", "ALG_DES_MAC8_NOPAD#<=2.1", 
        "ALG_DES_MAC4_ISO9797_M1#<=2.1", "ALG_DES_MAC8_ISO9797_M1#<=2.1", "ALG_DES_MAC4_ISO9797_M2#<=2.1", "ALG_DES_MAC8_ISO9797_M2#<=2.1", 
        "ALG_DES_MAC4_PKCS5#<=2.1", "ALG_DES_MAC8_PKCS5#<=2.1", "ALG_RSA_SHA_ISO9796#<=2.1", "ALG_RSA_SHA_PKCS1#<=2.1", "ALG_RSA_MD5_PKCS1#<=2.1", 
        "ALG_RSA_RIPEMD160_ISO9796#<=2.1", "ALG_RSA_RIPEMD160_PKCS1#<=2.1", "ALG_DSA_SHA#<=2.1", "ALG_RSA_SHA_RFC2409#<=2.1", 
        "ALG_RSA_MD5_RFC2409#<=2.1", "ALG_ECDSA_SHA#2.2.0", "ALG_AES_MAC_128_NOPAD#2.2.0", "ALG_DES_MAC4_ISO9797_1_M2_ALG3#2.2.0", 
        "ALG_DES_MAC8_ISO9797_1_M2_ALG3#2.2.0", "ALG_RSA_SHA_PKCS1_PSS#2.2.0", "ALG_RSA_MD5_PKCS1_PSS#2.2.0", "ALG_RSA_RIPEMD160_PKCS1_PSS#2.2.0", 
        // 2.2.2
        "ALG_HMAC_SHA1#2.2.2", "ALG_HMAC_SHA_256#2.2.2", "ALG_HMAC_SHA_384#2.2.2", "ALG_HMAC_SHA_512#2.2.2", "ALG_HMAC_MD5#2.2.2", "ALG_HMAC_RIPEMD160#2.2.2", 
        "ALG_RSA_SHA_ISO9796_MR#2.2.2", "ALG_RSA_RIPEMD160_ISO9796_MR#2.2.2", "ALG_SEED_MAC_NOPAD#2.2.2", 
        //3.0.1
        "ALG_ECDSA_SHA_256#3.0.1", "ALG_ECDSA_SHA_384#3.0.1", "ALG_AES_MAC_192_NOPAD#3.0.1", "ALG_AES_MAC_256_NOPAD#3.0.1", "ALG_ECDSA_SHA_224#3.0.1", "ALG_ECDSA_SHA_512#3.0.1", 
        "ALG_RSA_SHA_224_PKCS1#3.0.1", "ALG_RSA_SHA_256_PKCS1#3.0.1", "ALG_RSA_SHA_384_PKCS1#3.0.1", "ALG_RSA_SHA_512_PKCS1#3.0.1", 
        "ALG_RSA_SHA_224_PKCS1_PSS#3.0.1", "ALG_RSA_SHA_256_PKCS1_PSS#3.0.1", "ALG_RSA_SHA_384_PKCS1_PSS#3.0.1", "ALG_RSA_SHA_512_PKCS1_PSS#3.0.1",
        //3.0.4
        "ALG_DES_MAC4_ISO9797_1_M1_ALG3#3.0.4", "ALG_DES_MAC8_ISO9797_1_M1_ALG3#3.0.4"
    };

      //
      //Class javacardx.crypto.Cipher
      //
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

    public static final String CIPHER_STR[] = {"javacardx.crypto.Cipher", 
        "ALG_DES_CBC_NOPAD#<=2.1", "ALG_DES_CBC_ISO9797_M1#<=2.1", "ALG_DES_CBC_ISO9797_M2#<=2.1", "ALG_DES_CBC_PKCS5#<=2.1", 
        "ALG_DES_ECB_NOPAD#<=2.1", "ALG_DES_ECB_ISO9797_M1#<=2.1", "ALG_DES_ECB_ISO9797_M2#<=2.1", "ALG_DES_ECB_PKCS5#<=2.1",
        "ALG_RSA_ISO14888#<=2.1", "ALG_RSA_PKCS1#<=2.1", "ALG_RSA_ISO9796#<=2.1", 
        //2.1.1
        "ALG_RSA_NOPAD#2.1.1", 
        //2.2.0
        "ALG_AES_BLOCK_128_CBC_NOPAD#2.2.0", "ALG_AES_BLOCK_128_ECB_NOPAD#2.2.0", "ALG_RSA_PKCS1_OAEP#2.2.0", 
        //2.2.2
        "ALG_KOREAN_SEED_ECB_NOPAD#2.2.2", "ALG_KOREAN_SEED_CBC_NOPAD#2.2.2",
        //3.0.1
        "ALG_AES_BLOCK_192_CBC_NOPAD#3.0.1", "ALG_AES_BLOCK_192_ECB_NOPAD#3.0.1", "ALG_AES_BLOCK_256_CBC_NOPAD#3.0.1", "ALG_AES_BLOCK_256_ECB_NOPAD#3.0.1", 
        "ALG_AES_CBC_ISO9797_M1#3.0.1", "ALG_AES_CBC_ISO9797_M2#3.0.1", "ALG_AES_CBC_PKCS5#3.0.1", "ALG_AES_ECB_ISO9797_M1#3.0.1", "ALG_AES_ECB_ISO9797_M2#3.0.1", "ALG_AES_ECB_PKCS5#3.0.1"         
    }; 

      //
      //Class javacard.security.KeyAgreement
      //
    public static final byte ALG_EC_SVDP_DH                        = 1;
    public static final byte ALG_EC_SVDP_DHC                       = 2;
    // JC3.0.1
    public static final byte ALG_EC_SVDP_DH_KDF                    = 1;  
    public static final byte ALG_EC_SVDP_DH_PLAIN                  = 3;     
    public static final byte ALG_EC_SVDP_DHC_KDF                   = 2;  
    public static final byte ALG_EC_SVDP_DHC_PLAIN                 = 4;  
    
    public static final String KEYAGREEMENT_STR[] = {"javacard.security.KeyAgreement", 
        //2.2.1
        "ALG_EC_SVDP_DH#2.2.1", "ALG_EC_SVDP_DHC#2.2.1",
        //3.0.1
        "ALG_EC_SVDP_DH_KDF#3.0.1", "ALG_EC_SVDP_DH_PLAIN#3.0.1", "ALG_EC_SVDP_DHC_KDF#3.0.1", "ALG_EC_SVDP_DHC_PLAIN#3.0.1"
    };
    
      //
      //Class javacard.security.KeyBuilder
      //
    public static final byte TYPE_DES_TRANSIENT_RESET              = 1;
    public static final byte TYPE_DES_TRANSIENT_DESELECT           = 2;
    public static final byte TYPE_DES                              = 3;
    public static final byte TYPE_RSA_PUBLIC                       = 4;
    public static final byte TYPE_RSA_PRIVATE                      = 5;
    public static final byte TYPE_RSA_CRT_PRIVATE                  = 6;
    public static final byte TYPE_DSA_PUBLIC                       = 7;
    public static final byte TYPE_DSA_PRIVATE                      = 8; 
    public static final byte TYPE_EC_F2M_PUBLIC                    = 9;
    public static final byte TYPE_EC_F2M_PRIVATE                   = 10;
    public static final byte TYPE_EC_FP_PUBLIC                     = 11;
    public static final byte TYPE_EC_FP_PRIVATE                    = 12;
    public static final byte TYPE_AES_TRANSIENT_RESET              = 13;
    public static final byte TYPE_AES_TRANSIENT_DESELECT           = 14;
    public static final byte TYPE_AES                              = 15;
      // JC2.2.2
    public static final byte TYPE_KOREAN_SEED_TRANSIENT_RESET      = 16;
    public static final byte TYPE_KOREAN_SEED_TRANSIENT_DESELECT   = 17;
    public static final byte TYPE_KOREAN_SEED                      = 18;
    public static final byte TYPE_HMAC_TRANSIENT_RESET             = 19;
    public static final byte TYPE_HMAC_TRANSIENT_DESELECT          = 20;
    public static final byte TYPE_HMAC                             = 21;
    // JC3.0.1
    public static final byte TYPE_RSA_PRIVATE_TRANSIENT_RESET       = 22;  
    public static final byte TYPE_RSA_PRIVATE_TRANSIENT_DESELECT    = 23;  
    public static final byte TYPE_RSA_CRT_PRIVATE_TRANSIENT_RESET   = 24;  
    public static final byte TYPE_RSA_CRT_PRIVATE_TRANSIENT_DESELECT= 25;  
    public static final byte TYPE_DSA_PRIVATE_TRANSIENT_RESET       = 26;
    public static final byte TYPE_DSA_PRIVATE_TRANSIENT_DESELECT    = 27;  
    public static final byte TYPE_EC_F2M_PRIVATE_TRANSIENT_RESET    = 28;  
    public static final byte TYPE_EC_F2M_PRIVATE_TRANSIENT_DESELECT = 29;  
    public static final byte TYPE_EC_FP_PRIVATE_TRANSIENT_RESET     = 30;  
    public static final byte TYPE_EC_FP_PRIVATE_TRANSIENT_DESELECT  = 31;  
    
    private final int LENGTH_DES            = 64;
    private final int LENGTH_DES3_2KEY      = 128;
    private final int LENGTH_DES3_3KEY      = 192;
    private final int LENGTH_RSA_512        = 512;
    private final int LENGTH_RSA_736        = 736;
    private final int LENGTH_RSA_768        = 768;
    private final int LENGTH_RSA_896        = 896;
    private final int LENGTH_RSA_1024       = 1024;
    private final int LENGTH_RSA_1280       = 1280;
    private final int LENGTH_RSA_1536       = 1536;
    private final int LENGTH_RSA_1984       = 1984;
    private final int LENGTH_RSA_2048       = 2048;
    private final int LENGTH_RSA_3072       = 3072;
    private final int LENGTH_RSA_4096       = 4096;
    private final int LENGTH_DSA_512        = 512;
    private final int LENGTH_DSA_768        = 768;
    private final int LENGTH_DSA_1024       = 1024;
    private final int LENGTH_EC_FP_112      = 112;
    private final int LENGTH_EC_F2M_113     = 113;
    private final int LENGTH_EC_FP_128      = 128;
    private final int LENGTH_EC_F2M_131     = 131;
    private final int LENGTH_EC_FP_160      = 160;
    private final int LENGTH_EC_F2M_163     = 163;
    private final int LENGTH_EC_FP_192      = 192;
    private final int LENGTH_EC_F2M_193     = 193;
    private final int LENGTH_EC_FP_224      = 224;  
    private final int LENGTH_EC_FP_256      = 256;  
    private final int LENGTH_EC_FP_384      = 384;  
    private final int LENGTH_EC_FP_521      = 521;    
    private final int LENGTH_AES_128        = 128;
    private final int LENGTH_AES_192        = 192;
    private final int LENGTH_AES_256        = 256;
      // JC2.2.2
    private final int LENGTH_KOREAN_SEED_128        = 128;
    private final int LENGTH_HMAC_SHA_1_BLOCK_64    = 64;
    private final int LENGTH_HMAC_SHA_256_BLOCK_64  = 64;
    private final int LENGTH_HMAC_SHA_384_BLOCK_64  = 128;
    private final int LENGTH_HMAC_SHA_512_BLOCK_64  = 128;
    
    public static final String KEYBUILDER_STR[] = {
        "javacard.security.KeyBuilder", 
        "@@@DES_KEY@@@", "TYPE_DES_TRANSIENT_RESET#<=2.1", "TYPE_DES_TRANSIENT_DESELECT#<=2.1", "TYPE_DES LENGTH_DES#<=2.1", "TYPE_DES LENGTH_DES3_2KEY#<=2.1", "TYPE_DES LENGTH_DES3_3KEY#<=2.1",
        //2.2.0
        "@@@AES_KEY@@@", "TYPE_AES_TRANSIENT_RESET#2.2.0", "TYPE_AES_TRANSIENT_DESELECT#2.2.0", "TYPE_AES LENGTH_AES_128#2.2.0", "TYPE_AES LENGTH_AES_192#2.2.0", "TYPE_AES LENGTH_AES_256#2.2.0",
        "@@@RSA_PUBLIC_KEY@@@", "TYPE_RSA_PUBLIC LENGTH_RSA_512#<=2.1", "TYPE_RSA_PUBLIC LENGTH_RSA_736#2.2.0", "TYPE_RSA_PUBLIC LENGTH_RSA_768#2.2.0", "TYPE_RSA_PUBLIC LENGTH_RSA_896#2.2.0",
            "TYPE_RSA_PUBLIC LENGTH_RSA_1024#<=2.1", "TYPE_RSA_PUBLIC LENGTH_RSA_1280#2.2.0", "TYPE_RSA_PUBLIC LENGTH_RSA_1536#2.2.0", "TYPE_RSA_PUBLIC LENGTH_RSA_1984#2.2.0", "TYPE_RSA_PUBLIC LENGTH_RSA_2048#<=2.1", "TYPE_RSA_PUBLIC LENGTH_RSA_3072#never#0", "TYPE_RSA_PUBLIC LENGTH_RSA_4096#3.0.1",
        "@@@RSA_PRIVATE_KEY@@@", "TYPE_RSA_PRIVATE LENGTH_RSA_512#<=2.1", "TYPE_RSA_PRIVATE LENGTH_RSA_736#2.2.0", "TYPE_RSA_PRIVATE LENGTH_RSA_768#2.2.0", "TYPE_RSA_PRIVATE LENGTH_RSA_896#2.2.0",
            "TYPE_RSA_PRIVATE LENGTH_RSA_1024#<=2.1", "TYPE_RSA_PRIVATE LENGTH_RSA_1280#2.2.0", "TYPE_RSA_PRIVATE LENGTH_RSA_1536#2.2.0", "TYPE_RSA_PRIVATE LENGTH_RSA_1984#2.2.0", "TYPE_RSA_PRIVATE LENGTH_RSA_2048#<=2.1", "TYPE_RSA_PRIVATE LENGTH_RSA_3072#never#0", "TYPE_RSA_PRIVATE LENGTH_RSA_4096#3.0.1", 
            "TYPE_RSA_PRIVATE_TRANSIENT_RESET#3.0.1", "TYPE_RSA_PRIVATE_TRANSIENT_DESELECT#3.0.1",
        "@@@RSA_CRT_PRIVATE_KEY@@@", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_512#<=2.1", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_736#2.2.0", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_768#2.2.0", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_896#2.2.0",
            "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1024#<=2.1", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1280#2.2.0", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1536#2.2.0", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1984#2.2.0", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_2048#<=2.1", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_3072#never#0", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_4096#3.0.1",
            "TYPE_RSA_CRT_PRIVATE_TRANSIENT_RESET#3.0.1", "TYPE_RSA_CRT_PRIVATE_TRANSIENT_DESELECT#3.0.1",
        "@@@DSA_PRIVATE_KEY@@@", "TYPE_DSA_PRIVATE LENGTH_DSA_512#<=2.1", "TYPE_DSA_PRIVATE LENGTH_DSA_768#<=2.1", "TYPE_DSA_PRIVATE LENGTH_DSA_1024#<=2.1", "TYPE_DSA_PRIVATE_TRANSIENT_RESET#3.0.1", "TYPE_DSA_PRIVATE_TRANSIENT_DESELECT#3.0.1", 
        "@@@DSA_PUBLIC_KEY@@@", "TYPE_DSA_PUBLIC LENGTH_DSA_512#<=2.1", "TYPE_DSA_PUBLIC LENGTH_DSA_768#<=2.1", "TYPE_DSA_PUBLIC LENGTH_DSA_1024#<=2.1", 
        "@@@EC_F2M_PRIVATE_KEY@@@", "TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_113#2.2.0", "TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_131#2.2.0", "TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_163#2.2.0", "TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_193#2.2.0", "TYPE_EC_F2M_PRIVATE_TRANSIENT_RESET#3.0.1", "TYPE_EC_F2M_PRIVATE_TRANSIENT_DESELECT#3.0.1",
        "@@@EC_FP_PRIVATE_KEY@@@", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_112#2.2.0", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_128#2.2.0", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_160#2.2.0", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_192#2.2.0", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_224#3.0.1", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_256#3.0.1", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_384#3.0.1", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_521#3.0.4", "TYPE_EC_FP_PRIVATE_TRANSIENT_RESET#3.0.1", "TYPE_EC_FP_PRIVATE_TRANSIENT_DESELECT#3.0.1",
        "@@@KOREAN_SEED_KEY@@@", "TYPE_KOREAN_SEED_TRANSIENT_RESET#2.2.2", "TYPE_KOREAN_SEED_TRANSIENT_DESELECT#2.2.2", "TYPE_KOREAN_SEED LENGTH_KOREAN_SEED_128#2.2.2", 
        "@@@HMAC_KEY@@@", "TYPE_HMAC_TRANSIENT_RESET#2.2.2", "TYPE_HMAC_TRANSIENT_DESELECT#2.2.2", "TYPE_HMAC LENGTH_HMAC_SHA_1_BLOCK_64#2.2.2", "TYPE_HMAC LENGTH_HMAC_SHA_256_BLOCK_64#2.2.2", "TYPE_HMAC LENGTH_HMAC_SHA_384_BLOCK_64#2.2.2", "TYPE_HMAC LENGTH_HMAC_SHA_512_BLOCK_64#2.2.2",
    }; 
      //
      //Class javacard.security.KeyPair
      //introduced in 2.1.1
    public static final byte ALG_RSA                       = 1;
    public static final byte ALG_RSA_CRT                   = 2;
    public static final byte ALG_DSA                       = 3;
    public static final byte ALG_EC_F2M                    = 4;
    public static final byte ALG_EC_FP                     = 5;

    public static final String KEYPAIR_RSA_STR[] = {"javacard.security.KeyPair ALG_RSA on-card generation", 
        "ALG_RSA LENGTH_RSA_512#2.1.1", "ALG_RSA LENGTH_RSA_736#2.2.0", "ALG_RSA LENGTH_RSA_768#2.1.1", "ALG_RSA LENGTH_RSA_896#2.2.0",
        "ALG_RSA LENGTH_RSA_1024#2.1.1", "ALG_RSA LENGTH_RSA_1280#2.2.0", "ALG_RSA LENGTH_RSA_1536#2.2.0", "ALG_RSA LENGTH_RSA_1984#2.2.0", "ALG_RSA LENGTH_RSA_2048#2.1.1", 
        "ALG_RSA LENGTH_RSA_3072#never#0", "ALG_RSA LENGTH_RSA_4096#3.0.1"
        };

    public static final String KEYPAIR_RSACRT_STR[] = {"javacard.security.KeyPair ALG_RSA_CRT on-card generation", 
        "ALG_RSA_CRT LENGTH_RSA_512#2.1.1", "ALG_RSA_CRT LENGTH_RSA_736#2.2.0", "ALG_RSA_CRT LENGTH_RSA_768#2.1.1", "ALG_RSA_CRT LENGTH_RSA_896#2.2.0",
        "ALG_RSA_CRT LENGTH_RSA_1024#2.1.1", "ALG_RSA_CRT LENGTH_RSA_1280#2.2.0", "ALG_RSA_CRT LENGTH_RSA_1536#2.2.0", "ALG_RSA_CRT LENGTH_RSA_1984#2.2.0", "ALG_RSA_CRT LENGTH_RSA_2048#2.1.1", 
        "ALG_RSA_CRT LENGTH_RSA_3072#never#0", "ALG_RSA_CRT LENGTH_RSA_4096#3.0.1"
        };    
  
    public static final String KEYPAIR_DSA_STR[] = {"javacard.security.KeyPair ALG_DSA on-card generation", 
        "ALG_DSA LENGTH_DSA_512#2.1.1", "ALG_DSA LENGTH_DSA_768#2.1.1", "ALG_DSA LENGTH_DSA_1024#2.1.1"
    };
  
    public static final String KEYPAIR_EC_F2M_STR[] = {"javacard.security.KeyPair ALG_EC_F2M on-card generation", 
        "ALG_EC_F2M LENGTH_EC_F2M_113#2.2.1", "ALG_EC_F2M LENGTH_EC_F2M_131#2.2.1", "ALG_EC_F2M LENGTH_EC_F2M_163#2.2.1", "ALG_EC_F2M LENGTH_EC_F2M_193#2.2.1"
    };
 
    public static final String KEYPAIR_EC_FP_STR[] = {"javacard.security.KeyPair ALG_EC_FP on-card generation", 
        "ALG_EC_FP LENGTH_EC_FP_112#2.2.1", "ALG_EC_FP LENGTH_EC_FP_128#2.2.1", "ALG_EC_FP LENGTH_EC_FP_160#2.2.1", "ALG_EC_FP LENGTH_EC_FP_192#2.2.1", "ALG_EC_FP LENGTH_EC_FP_224#3.0.1", "ALG_EC_FP LENGTH_EC_FP_256#3.0.1", "ALG_EC_FP LENGTH_EC_FP_384#3.0.1", "ALG_EC_FP LENGTH_EC_FP_521#3.0.4"
    };

    public static final byte CLASS_KEYPAIR_RSA_P2          = 11;
    public static final byte CLASS_KEYPAIR_RSACRT_P2       = 11;
    public static final byte CLASS_KEYPAIR_DSA_P2          = 3;
    public static final byte CLASS_KEYPAIR_EC_F2M_P2       = 4;
    public static final byte CLASS_KEYPAIR_EC_FP_P2        = 4;

      //Class javacard.security.MessageDigest
    public static final byte ALG_SHA                       = 1;
    public static final byte ALG_MD5                       = 2;
    public static final byte ALG_RIPEMD160                 = 3;
      // JC2.2.2
    public static final byte ALG_SHA_256                   = 4;
    public static final byte ALG_SHA_384                   = 5;
    public static final byte ALG_SHA_512                   = 6;
    // JC3.0.1
    public static final byte ALG_SHA_224 = 7;
    
    public static final String MESSAGEDIGEST_STR[] = {"javacard.security.MessageDigest", 
        "ALG_SHA#<=2.1", "ALG_MD5#<=2.1", "ALG_RIPEMD160#<=2.1", 
        //2.2.2
        "ALG_SHA_256#2.2.2", "ALG_SHA_384#2.2.2", "ALG_SHA_512#2.2.2", 
        //3.0.1
        "ALG_SHA_224#3.0.1"
    }; 


      //Class javacard.security.RandomData
    public static final byte ALG_PSEUDO_RANDOM             = 1;
    public static final byte ALG_SECURE_RANDOM             = 2;

    public static final String RANDOMDATA_STR[] = {"javacard.security.RandomData", 
        "ALG_PSEUDO_RANDOM#<=2.1", "ALG_SECURE_RANDOM#<=2.1"}; 

      // Class javacard.security.Checksum
    public static final byte ALG_ISO3309_CRC16             = 1;
    public static final byte ALG_ISO3309_CRC32             = 2;

    public static final String CHECKSUM_STR[] = {"javacard.security.Checksum", "ALG_ISO3309_CRC16#2.2.1", "ALG_ISO3309_CRC32#2.2.1"}; 
    
    public static final String JCSYSTEM_STR[] = {"javacard.framework.JCSystem", "JCSystem.getVersion()[Major.Minor]#<=2.1", 
        "JCSystem.isObjectDeletionSupported#2.2.0", "JCSystem.MEMORY_TYPE_PERSISTENT#2.2.1", "JCSystem.MEMORY_TYPE_TRANSIENT_RESET#2.2.1", 
        "JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT#2.2.1"}; 

    public static final String RAWRSA_1024_STR[] = {"Variable RSA 1024 - support for variable public exponent. If supported, user-defined fast modular exponentiation can be executed on the smart card via cryptographic coprocessor. This is very specific feature and you will probably not need it", 
        "Allocate RSA 1024 objects", "Set random modulus", "Set random public exponent", "Initialize cipher with public key with random exponent", "Use random public exponent"}; 

    public static final String EXTENDEDAPDU_STR[] = {"javacardx.apdu.ExtendedLength", "Extended APDU#2.2.2"}; 

    public static final String BASIC_INFO[] = {"Basic info", "JavaCard support version"}; 
   
    public static final String[] ALL_CLASSES_STR[] = {
        BASIC_INFO, JCSYSTEM_STR, EXTENDEDAPDU_STR, CIPHER_STR, SIGNATURE_STR, MESSAGEDIGEST_STR, RANDOMDATA_STR, KEYBUILDER_STR, 
        KEYPAIR_RSA_STR, KEYPAIR_RSACRT_STR, KEYPAIR_DSA_STR, KEYPAIR_EC_F2M_STR, 
        KEYPAIR_EC_FP_STR, KEYAGREEMENT_STR, CHECKSUM_STR, RAWRSA_1024_STR
    };

    
    public static final short 	ILLEGAL_USE         = 5;
    public static final short 	ILLEGAL_VALUE       = 1;
    public static final short 	INVALID_INIT        = 4;
    public static final short 	NO_SUCH_ALGORITHM   = 3;
    public static final short 	UNINITIALIZED_KEY   = 2; 
   
    CardTerminal m_terminal = null;
    CardChannel m_channel = null;
    Card m_card = null;
    
    public static final byte selectApplet[] = {
        (byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x00, (byte) 0x09, 
        (byte) 0x6D, (byte) 0x79, (byte) 0x70, (byte) 0x61, (byte) 0x63, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x31}; 
    
    public static final String helpString = "This program can be used with following parameters:\r\n"
            + ALGTEST_MULTIPERAPDU + " -> for using classic AlgTest with multiple tests per APDU command\r\n"
            + ALGTEST_SINGLEPERAPDU + " -> for using AlgTest with single test per APDU command\r\n"
            + ALGTEST_PERFORMANCE + " -> for using AlgTest with performance testing\r\n"
            + TEST_ALL_ALGORITHMS + " -> for testing all algorithms without further arguments necessary\r\n"
            + TEST_CLASS_CIPHER + " -> for testing class Cipher\r\n"
            + TEST_CLASS_CHECKSUM + " -> for testing class Checksum\r\n"
            + TEST_CLASS_SIGNATURE + " -> for testing class Signature\r\n"
            + TEST_CLASS_KEYBUILDER + " -> for testing class KeyBuilder\r\n"
            + TEST_CLASS_MESSAGEDIGEST + " -> for testing class Message Digest\r\n"
            + TEST_CLASS_RANDOMDATA + " -> for testing class Random Data\r\n"
            + TEST_CLASS_KEYAGREEMENT + " -> for testing class Key Agreement\r\n"
            + TEST_CLASS_KEYPAIR_ALG_RSA + " -> for testing class KeyPair algorithm RSA\r\n"
            + TEST_CLASS_KEYPAIR_ALG_RSA_CRT + " -> for testing class KeyPair algorithm RSA CRT\r\n"
            + TEST_CLASS_KEYPAIR_ALG_DSA + " -> for testing class KeyPair algorithm DSA\r\n"
            + TEST_CLASS_KEYPAIR_ALG_EC_F2M + " -> for testing class KeyPair algorithm EC F2M\r\n"
            + TEST_CLASS_KEYPAIR_ALG_EC_FP + " -> for testing class KeyPair algorithm EC FP\r\n"
            + TEST_EEPROM + " ->  for testing available EEPROM memmory\r\n"
            + TEST_RAM + " -> for testing available RAM memmory\r\n"
            + TEST_EXTENDEDAPDU + " -> for testing extended APDU support\r\n"
            + TEST_RSAEXPONENT + " -> for testing varialbe public RSA exponent.\r\n";
    
    public static final String paramList =
            ALGTEST_MULTIPERAPDU + "\r\n"
            + ALGTEST_SINGLEPERAPDU + "\r\n"
            + ALGTEST_PERFORMANCE + "\r\n\r\n"
            + TEST_ALL_ALGORITHMS + "\r\n\r\n"
            + TEST_CLASS_CIPHER + "\r\n"
            + TEST_CLASS_CHECKSUM + "\r\n"
            + TEST_CLASS_SIGNATURE + "\r\n"
            + TEST_CLASS_KEYBUILDER + "\r\n"
            + TEST_CLASS_MESSAGEDIGEST + "\r\n"
            + TEST_CLASS_RANDOMDATA + "\r\n"
            + TEST_CLASS_KEYAGREEMENT + "\r\n"
            + TEST_CLASS_KEYPAIR_ALG_RSA + "\r\n"
            + TEST_CLASS_KEYPAIR_ALG_RSA_CRT + "\r\n"
            + TEST_CLASS_KEYPAIR_ALG_DSA + "\r\n"
            + TEST_CLASS_KEYPAIR_ALG_EC_F2M + "\r\n"
            + TEST_CLASS_KEYPAIR_ALG_EC_FP + "\r\n\r\n"
            + TEST_EEPROM + "\r\n"
            + TEST_RAM + "\r\n"
            + TEST_EXTENDEDAPDU + "\r\n"
            + TEST_RSAEXPONENT + "\r\n";

    public static final byte OFFSET_CLA = 0x00;
    public static final byte OFFSET_INS = 0x01;
    public static final byte OFFSET_P1 = 0x02;
    public static final byte OFFSET_P2 = 0x03;
    public static final byte OFFSET_LC = 0x04;
    public static final byte OFFSET_DATA = 0x05;
    public static final byte HEADER_LENGTH = 0x05;
    public static final short EXTENDED_APDU_TEST_LENGTH = 0x400; // 1024
    
    public final static int STAT_OK = 0;
    public final static int STAT_DATA_CORRUPTED = 10;
    
    public StringBuilder atr = new StringBuilder(); 
    public StringBuilder reader = new StringBuilder();
    public StringBuilder protocol = new StringBuilder();
    
    /* ATR of jCardSim. */
    static final String SIMULATOR_ATR = "3BFA1800008131FE454A434F5033315632333298";

    /* BUGBUG: we need to figure out how to support JCardSim in nice way (copy of class files, directory structure...)
    JavaxSmartCardInterface simulator = null;
    */
    
    /* CLOCKS_PER_SEC also used in 'PerformanceTesting.java' */
    public final int CLOCKS_PER_SEC = 1000;
    
    public FileOutputStream establishConnection(Class ClassToTest) throws Exception{
        if (ConnectToCard(ClassToTest, reader, atr, protocol)) {
            String message = "";
            if (atr.toString().equals("")){atr.append(SIMULATOR_ATR + " (provided by jCardSimulator)");} // if atr == "" it means that simulator is running and thus simulator atr must be used
            System.out.println("ATR: " + atr);
            String fileName = "AlgTest_" + atr + ".csv";
            fileName = fileName.replace(":", "");
            fileName = fileName.replace(" ", "");
            
            FileOutputStream file = new FileOutputStream(fileName);
            
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

            message = "AlgTestJClient version; " + AlgTestJClient.ALGTEST_JCLIENT_VERSION + "\r\n";
            System.out.println(message); file.write(message.getBytes());    

            value.setLength(0);
            if (GetAppletVersion(value) == CardMngr.STAT_OK) {
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

            System.out.println("\n\n#########################");
            System.out.println("\nJCSystem information");

            if (GetJCSystemInfo(value, file) == CardMngr.STAT_OK) {}
            else { System.out.println("\nERROR: GetJCSystemInfo fail"); }
            // returns created file for output data
            return file;
            
        }
        return null;    // returns 'null' in case of error
    }

    public boolean ConnectToCard(Class ClassToTest, StringBuilder selectedReader, StringBuilder selectedATR, StringBuilder usedProtocol) throws Exception {
        boolean cardFound = false;        
        // TRY ALL READERS, FIND FIRST SELECTABLE
        List terminalList = GetReaderList();

        if(terminalList == null){   // if there are no terminals connected
            System.out.println("No terminals found");
            System.out.println("Creating simulator...");
            if (ClassToTest == null){   // if there is no class to be tested specified, simulator can't install needed applet
                System.err.println("No class to test given!");
            }
            else{       // simulator will install given class
                MakeSim(ClassToTest);
            }
            System.out.println("Simulator created.");
            cardFound = true;
        }
        else{
            //List numbers of Card readers        
            for (int i = 0; i < terminalList.size(); i++) {
                System.out.println(i + " : " + terminalList.get(i));
                m_terminal = (CardTerminal) terminalList.get(i);
                if (m_terminal.isCardPresent()) {
                    m_card = m_terminal.connect("*");
                    System.out.println("card: " + m_card);
                    m_channel = m_card.getBasicChannel();
                        System.out.println("Card Channel: " + m_channel.getChannelNumber());

                    //reset the card
                    ATR atr = m_card.getATR();
                    System.out.println(bytesToHex(atr.getBytes()));

                    // SELECT APPLET
                    ResponseAPDU resp = sendAPDU(selectApplet);
                    if (resp.getSW() != 0x9000) {
                        System.out.println("No card found.");
                    } else {
                        // CARD FOUND
                        cardFound = true;

                        selectedATR.append(bytesToHex(m_card.getATR().getBytes()));
                        selectedReader.append(terminalList.get(i).toString());
                        usedProtocol.append(m_card.getProtocol());

                        break;
                    }
                }
            }
        }
        return cardFound;        
    }

    public void DisconnectFromCard() throws Exception {
        if (m_card != null) {
            m_card.disconnect(false);
            m_card = null;
        }
    }

    public List GetReaderList() {
        try {
            TerminalFactory factory = TerminalFactory.getDefault();
            List readersList = factory.terminals().list();
            return readersList;
        } catch (CardException ex) {
            System.out.println("Exception : " + ex);
            return null;
        }
    }

    /**
     * Method which will send bytes to on-card application using smartcardio interface.
     * @param apdu Byte array containing data to be send to card.
     * @return Returns card response as ResponseAPDU.
     * @throws Exception
     */
    public ResponseAPDU sendAPDU(byte apdu[]) throws Exception {
        CommandAPDU commandAPDU = new CommandAPDU(apdu);
        ResponseAPDU responseAPDU = null;
        System.out.println(">>>>");
        System.out.println(commandAPDU);

        System.out.println(bytesToHex(commandAPDU.getBytes()));
        
        if(m_channel == null){  // in case there is no real card present -> simulator is running
            /* BUGBUG: we need to figure out how to support JCardSim in nice way (copy of class files, directory structure...)
            responseAPDU = simulator.transmitCommand(commandAPDU);
            */
            return null;
        }
        else {                  // in case there is actual card present
            responseAPDU = m_channel.transmit(commandAPDU);
        }

        System.out.println(responseAPDU);
        System.out.println(bytesToHex(responseAPDU.getBytes()));

        if (responseAPDU.getSW1() == (byte) 0x61) {
            CommandAPDU apduToSend = new CommandAPDU((byte) 0x00,
                    (byte) 0xC0, (byte) 0x00, (byte) 0x00,
                    (int) responseAPDU.getSW1());

            responseAPDU = m_channel.transmit(apduToSend);
            System.out.println(bytesToHex(responseAPDU.getBytes()));
        }

        System.out.println("<<<<");

        return (responseAPDU);
    }

    public String byteToHex(byte data) {
        StringBuilder  buf = new StringBuilder ();
        buf.append(toHexChar((data >>> 4) & 0x0F));
        buf.append(toHexChar(data & 0x0F));
        return buf.toString();
    }

    public char toHexChar(int i) {
        if ((0 <= i) && (i <= 9)) {
            return (char) ('0' + i);
        } else {
            return (char) ('a' + (i - 10));
        }
    }

    public String bytesToHex(byte[] data) {
        StringBuilder  buf = new StringBuilder ();
        for (int i = 0; i < data.length; i++) {
            buf.append(byteToHex(data[i]));
            buf.append(" ");
        }
        return (buf.toString());
    }
    
    // Parse algorithm name and version of JC which introduced it
    //algParts[0] == algorithm name
    //algParts[1] == introducing version
    //algParts[2] == should be this item included in output? 1/0
    public String GetAlgorithmName(String algorithmInfoString) {
        String[] algParts = algorithmInfoString.split("#");
        return algParts[0];
    }
     public String GetAlgorithmIntroductionVersion(String algorithmInfoString) {
        String[] algParts = algorithmInfoString.split("#");
        String algorithmVersion = (algParts.length > 1) ? algParts[1] : "";
        return algorithmVersion;
    }
    public boolean ShouldBeIncludedInOutput(String algorithmInfoString) {
        String[] algParts = algorithmInfoString.split("#");
        String includeInfo = (algParts.length > 2) ? algParts[2] : "1";
        return Integer.decode(includeInfo) != 0;
    }
                                
    public int GetAppletVersion(StringBuilder pValue) throws Exception {
        int         status = STAT_OK;
    
	// Prepare test memory apdu
        byte apdu[] = new byte[HEADER_LENGTH + 1];
        apdu[OFFSET_CLA] = (byte) 0xB0;
        apdu[OFFSET_INS] = (byte) 0x60;
        apdu[OFFSET_P1] = 0x00;
        apdu[OFFSET_P2] = 0x00;
        apdu[OFFSET_LC] = 0x01;
        apdu[OFFSET_DATA] = 0x00;

        try {
            ResponseAPDU resp = sendAPDU(apdu);
            if (resp.getSW() != 0x9000) {
                System.out.println("Fail to obtain Applet version");
                pValue.append("error");
            } else {
                byte temp[] = resp.getData();
                char version[] = new char[temp.length]; 
                for (int i = 0; i < temp.length; i++) { version[i] = (char) temp[i]; }
                pValue.append(version);
             }        
        }
        catch (Exception ex) {
            System.out.println("Fail to obtain Applet version");
            pValue.append("error");
        }
                
	return status;
    }
    
    public int GetJCSystemInfo(StringBuilder pValue, FileOutputStream pFile) throws Exception {
        int         status = STAT_OK;
        long        elapsedCard = 0;
    
	// Prepare test memory apdu
        byte apdu[] = new byte[HEADER_LENGTH+1];
        apdu[OFFSET_CLA] = (byte) 0xB0;
        apdu[OFFSET_INS] = (byte) 0x73;
        apdu[OFFSET_P1] = 0x00;
        apdu[OFFSET_P2] = 0x00;
        apdu[OFFSET_LC] = 0x01;
        apdu[OFFSET_DATA] = 0x01;

        elapsedCard -= System.currentTimeMillis();
        try {
            ResponseAPDU resp = sendAPDU(apdu);
            if (resp.getSW() != 0x9000) {
                System.out.println("Fail to obtain JCSystemInfo");
            } else {
                // SET READ DATA
                byte temp[] = resp.getData();

                // SAVE TIME OF CARD RESPONSE
                elapsedCard += System.currentTimeMillis();
                String elTimeStr;
                // OUTPUT REQUIRED TIME WHEN PARTITIONED CHECk WAS PERFORMED (NOTMULTIPLE ALGORITHMS IN SINGLE RUN)
                elTimeStr = String.valueOf((double) elapsedCard / (float) CLOCKS_PER_SEC);

                int versionMajor = temp[0];
                int versionMinor = temp[1];
                int bDeletionSupported = temp[2];
                int eepromSize = (temp[3] << 8) + (temp[4] & 0xff);
                int ramResetSize = (temp[5] << 8) + (temp[6] & 0xff);
                int ramDeselectSize = (temp[7] << 8) + (temp[8] & 0xff);



                String message;
                message = String.format("\r\n%1s;%d.%d;", GetAlgorithmName(JCSYSTEM_STR[1]), versionMajor, versionMinor); 
                System.out.println(message);
                pFile.write(message.getBytes());
                pValue.append(message);
                message = String.format("\r\n%s;%s;", GetAlgorithmName(JCSYSTEM_STR[2]),(bDeletionSupported != 0) ? "yes" : "no"); 

                System.out.println(message);
                pFile.write(message.getBytes());
                pValue.append(message);
                message = String.format("\r\n%s;%s%dB;", GetAlgorithmName(JCSYSTEM_STR[3]),(eepromSize == 32767) ? ">" : "", eepromSize); 
                System.out.println(message);
                pFile.write(message.getBytes());
                pValue.append(message);
                message = String.format("\r\n%s;%s%dB;", GetAlgorithmName(JCSYSTEM_STR[4]),(ramResetSize == 32767) ? ">" : "", ramResetSize); 
                System.out.println(message);
                pFile.write(message.getBytes());
                pValue.append(message);
                message = String.format("\r\n%s;%s%dB;\n", GetAlgorithmName(JCSYSTEM_STR[5]),(ramDeselectSize == 32767) ? ">" : "", ramDeselectSize); 
                System.out.println(message);
                message += "\r\n";

                pFile.write(message.getBytes());
                pValue.append(message);
            }        
        }
        catch (Exception ex) {
            System.out.println("Fail to obtain JCSystemInfo");
            pValue.append("error");
        }
                      
                
	return status;
    }
    
    /**
     * Method that will test all algorithms.
     * @param file FileOutputStream object for output data.
     * @throws Exception
     */
    public void testAllAtOnce (FileOutputStream file) throws Exception{
        StringBuilder value = new StringBuilder();
        int answ = 1;   // so 'GetSupportedAndParse' doesn't ask if to test given algorithm
        
        // Class javacardx.crypto.Cipher
        value.setLength(0);
        if (GetSupportedAndParse(CLASS_CIPHER, CIPHER_STR, value, file, (byte) 0, answ) == STAT_OK) {}
        else { String errorMessage = "\nERROR: javacardx.crypto.Cipher fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
        file.flush();

        // Class javacard.security.Signature
        value.setLength(0);
        if (GetSupportedAndParse(CLASS_SIGNATURE, SIGNATURE_STR, value, file, (byte) 0, answ) == STAT_OK) {}
        else { String errorMessage = "\nERROR: javacard.security.Signature fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
        file.flush();

        // Class javacard.security.MessageDigest
        value.setLength(0);
        if (GetSupportedAndParse(CLASS_MESSAGEDIGEST, MESSAGEDIGEST_STR, value, file, (byte) 0, answ) == STAT_OK) {}
        else { String errorMessage = "\nERROR: javacard.security.MessageDigest fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
        file.flush();

        // Class javacard.security.RandomData
        value.setLength(0);
        if (GetSupportedAndParse(CLASS_RANDOMDATA, RANDOMDATA_STR, value, file, (byte) 0, answ) == STAT_OK) {}
        else { String errorMessage = "\nERROR: javacard.security.RandomData fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
        file.flush();

        // Class javacard.security.KeyBuilder
        value.setLength(0);
        if (GetSupportedAndParse(CLASS_KEYBUILDER, KEYBUILDER_STR, value, file, (byte) 0, answ) == STAT_OK) {}
        else { String errorMessage = "\nERROR: javacard.security.KeyBuilder fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
        file.flush();

        // Class javacard.security.KeyPair RSA
        value.setLength(0);
        if (GetSupportedAndParse(CLASS_KEYPAIR_RSA, KEYPAIR_RSA_STR, value, file, CLASS_KEYPAIR_RSA_P2, answ) == STAT_OK) {}
        else { String errorMessage = "\nERROR: javacard.security.KeyPair RSA fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
        file.flush();
        // Class javacard.security.KeyPair RSA_CRT
        value.setLength(0);
        if (GetSupportedAndParse(CLASS_KEYPAIR_RSA_CRT, KEYPAIR_RSACRT_STR, value, file, CLASS_KEYPAIR_RSACRT_P2, answ) == STAT_OK) {}
        else { String errorMessage = "\nERROR: javacard.security.KeyPair RSA_CRT fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
        file.flush();
        // Class javacard.security.KeyPair DSA
        value.setLength(0);
        if (GetSupportedAndParse(CLASS_KEYPAIR_DSA, KEYPAIR_DSA_STR, value, file, CLASS_KEYPAIR_DSA_P2, answ) == STAT_OK) {}
        else { String errorMessage = "\nERROR: javacard.security.KeyPair DSA fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
        file.flush();
        // Class javacard.security.KeyPair EC_F2M
        value.setLength(0);
        if (GetSupportedAndParse(CLASS_KEYPAIR_EC_F2M, KEYPAIR_EC_F2M_STR, value, file,  CLASS_KEYPAIR_EC_F2M_P2, answ) == STAT_OK) {}
        else { String errorMessage = "\nERROR: javacard.security.KeyPair EC_F2M fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
        file.flush();
        // Class javacard.security.KeyPair EC_FP
        value.setLength(0);
        if (GetSupportedAndParse(CLASS_KEYPAIR_EC_FP, KEYPAIR_EC_FP_STR, value, file, CLASS_KEYPAIR_EC_FP_P2, answ) == STAT_OK) {}
        else { String errorMessage = "\nERROR: javacard.security.KeyPair EC_FP fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
        file.flush();

        // Class javacard.security.KeyAgreement
        value.setLength(0);
        if (GetSupportedAndParse(CLASS_KEYAGREEMENT, KEYAGREEMENT_STR, value, file, (byte) 0, answ) == STAT_OK) {}
        else { String errorMessage = "\nERROR: javacard.security.KeyAgreement fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
        file.flush();

        // Class javacard.security.Checksum
        value.setLength(0);
        if (GetSupportedAndParse(CLASS_CHECKSUM, CHECKSUM_STR, value, file, (byte) 0, answ) == STAT_OK) {}
        else { String errorMessage = "\nERROR: javacard.security.Checksum fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
        file.flush();
    }
    
    /**
     * Running algorithm testing with parameters from command line.
     * @param args Arguments from command line as String array to tell method which algs to test.
     * @param file File to write data to.
     * @throws Exception
     */
    public void WithArgs (String[] args, FileOutputStream file) throws Exception{
        StringBuilder value = new StringBuilder(); 
        if (args[1].equals(TEST_ALL_ALGORITHMS) ){testAllAtOnce(file);}    
        /* Class 'javacardx.crypto.Cipher'. */
        else if (Arrays.asList(args).contains(TEST_CLASS_CIPHER)){
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_CIPHER, CIPHER_STR, value, file, (byte) 0, FORCE_TEST) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacardx.crypto.Cipher fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();
        }
        /* Class 'javacard.security.Signature'. */
        else if (Arrays.asList(args).contains(TEST_CLASS_SIGNATURE)){
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_SIGNATURE, SIGNATURE_STR, value, file, (byte) 0, FORCE_TEST) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.Signature fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();
        }
        /* Class 'javacard.security.MessageDigest'. */
        else if (Arrays.asList(args).contains(TEST_CLASS_MESSAGEDIGEST)){
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_MESSAGEDIGEST, MESSAGEDIGEST_STR, value, file, (byte) 0, FORCE_TEST) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.MessageDigest fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();
        }
        /* Class 'javacard.security.RandomData'. */
        else if (Arrays.asList(args).contains(TEST_CLASS_RANDOMDATA)){
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_RANDOMDATA, RANDOMDATA_STR, value, file, (byte) 0, FORCE_TEST) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.RandomData fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();
        }
        /* Class 'javacard.security.KeyBuilder'. */
        else if (Arrays.asList(args).contains(TEST_CLASS_KEYBUILDER)){
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_KEYBUILDER, KEYBUILDER_STR, value, file, (byte) 0, FORCE_TEST) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.KeyBuilder fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();
        }
        /* Class 'javacard.security.KeyPair' RSA. */
        else if (Arrays.asList(args).contains(TEST_CLASS_KEYPAIR_ALG_RSA)){
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_KEYPAIR_RSA, KEYPAIR_RSA_STR, value, file, CLASS_KEYPAIR_RSA_P2, FORCE_TEST) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.KeyPair RSA fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();
        }
        /* Class 'javacard.security.KeyPair' RSA CRT. */
        else if (Arrays.asList(args).contains(TEST_CLASS_KEYPAIR_ALG_RSA_CRT)){
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_KEYPAIR_RSA_CRT, KEYPAIR_RSACRT_STR, value, file, CLASS_KEYPAIR_RSACRT_P2, FORCE_TEST) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.KeyPair RSA_CRT fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();
        }
        /* Class 'javacard.security.KeyPair' DSA. */
        else if (Arrays.asList(args).contains(TEST_CLASS_KEYPAIR_ALG_DSA)){
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_KEYPAIR_DSA, KEYPAIR_DSA_STR, value, file, CLASS_KEYPAIR_DSA_P2, FORCE_TEST) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.KeyPair DSA fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();
        }
        /* Class 'javacard.security.KeyPair' EC_F2M. */
        else if (Arrays.asList(args).contains(TEST_CLASS_KEYPAIR_ALG_EC_F2M)){
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_KEYPAIR_EC_F2M, KEYPAIR_EC_F2M_STR, value, file,  CLASS_KEYPAIR_EC_F2M_P2, FORCE_TEST) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.KeyPair EC_F2M fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();
        }
        /* Class 'javacard.security.KeyPair' EC_FP. */
        else if (Arrays.asList(args).contains(TEST_CLASS_KEYPAIR_ALG_EC_FP)){
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_KEYPAIR_EC_FP, KEYPAIR_EC_FP_STR, value, file, CLASS_KEYPAIR_EC_FP_P2, FORCE_TEST) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.KeyPair EC_FP fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();
        }
        else {
            PrintHelp();
        }
    }
    
    /**
     * Method which will run testing of supported algorithms using MultiPerApdu AlgTest.
     * @param args Arguments from command line as String array to tell method which algorithms to test.
     * @param answ Variable to choose if to test algorithm or to ask to test it.
     * @throws Exception
     */
    public void testClassic (String[] args, int answ, FileOutputStream file) throws Exception{
        /* In case of using simulator, this method will use AlgTest class to run tests. */
        /* BUGBUG: we need to figure out how to support JCardSim in nice way (copy of class files, directory structure...)
        Class testClassClassic = AlgTest.class;
        */
        Class testClassClassic = null;
        StringBuilder value = new StringBuilder();
        /* Testing if there is a file created for output data. */
        if(file == null){
            System.err.println("\nERROR: fail to connect to card with AlgTest applet");
        }
        /* Testing if there are arguments present and if so runs method testing algs with given parameters. */
        if (args.length > 1){
            WithArgs(args, file);
        }
        else{
            // Class javacardx.crypto.Cipher
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_CIPHER, CIPHER_STR, value, file, (byte) 0, answ) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacardx.crypto.Cipher fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();

            // Class javacard.security.Signature
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_SIGNATURE, SIGNATURE_STR, value, file, (byte) 0, answ) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.Signature fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();

            // Class javacard.security.MessageDigest
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_MESSAGEDIGEST, MESSAGEDIGEST_STR, value, file, (byte) 0, answ) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.MessageDigest fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();

            // Class javacard.security.RandomData
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_RANDOMDATA, RANDOMDATA_STR, value, file, (byte) 0, answ) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.RandomData fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();

            // Class javacard.security.KeyBuilder
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_KEYBUILDER, KEYBUILDER_STR, value, file, (byte) 0, answ) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.KeyBuilder fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();

            // Class javacard.security.KeyPair RSA
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_KEYPAIR_RSA, KEYPAIR_RSA_STR, value, file, CLASS_KEYPAIR_RSA_P2, answ) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.KeyPair RSA fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();

            // Class javacard.security.KeyPair RSA_CRT
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_KEYPAIR_RSA_CRT, KEYPAIR_RSACRT_STR, value, file, CLASS_KEYPAIR_RSACRT_P2, answ) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.KeyPair RSA_CRT fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();

            // Class javacard.security.KeyPair DSA
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_KEYPAIR_DSA, KEYPAIR_DSA_STR, value, file, CLASS_KEYPAIR_DSA_P2, answ) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.KeyPair DSA fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();

            // Class javacard.security.KeyPair EC_F2M
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_KEYPAIR_EC_F2M, KEYPAIR_EC_F2M_STR, value, file,  CLASS_KEYPAIR_EC_F2M_P2, answ) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.KeyPair EC_F2M fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();

            // Class javacard.security.KeyPair EC_FP
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_KEYPAIR_EC_FP, KEYPAIR_EC_FP_STR, value, file, CLASS_KEYPAIR_EC_FP_P2, answ) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.KeyPair EC_FP fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();

            // Class javacard.security.KeyAgreement
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_KEYAGREEMENT, KEYAGREEMENT_STR, value, file, (byte) 0, answ) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.KeyAgreement fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();

            // Class javacard.security.Checksum
            value.setLength(0);
            if (GetSupportedAndParse(CLASS_CHECKSUM, CHECKSUM_STR, value, file, (byte) 0, answ) == STAT_OK) {}
            else { String errorMessage = "\nERROR: javacard.security.Checksum fail\r\n"; System.out.println(errorMessage); file.write(errorMessage.getBytes()); }
            file.flush();
            
            // Variable public exponent
            value.setLength(0);
            if (TestVariableRSAPublicExponentSupport(value, file, (byte) 0) == STAT_OK) {}
            else { String errorMessage = "\nERROR: Test variable public exponent support fail\n"; 
                System.out.println(errorMessage); file.write(errorMessage.getBytes());
            }
            file.flush();
        }
        
        if (file != null) file.close();
    }
    
    public int TestVariableRSAPublicExponentSupport(StringBuilder pValue, FileOutputStream pFile, byte algPartP2) throws Exception {
        int         status = STAT_OK;
        
        byte apdu[] = new byte[HEADER_LENGTH];
        apdu[OFFSET_CLA] = (byte) 0xB0;
        apdu[OFFSET_INS] = (byte) 0x72;
        apdu[OFFSET_P1] = 0x00;
        apdu[OFFSET_P2] = 0x00;
        apdu[OFFSET_LC] = 0x00;
            
        String message;
        message = "\r\nSupport for variable public exponent for RSA 1024. If supported, user-defined fast modular exponentiation can be executed on the smart card via cryptographic coprocessor. This is very specific feature and you will probably not need it;"; 
        System.out.println(message);
        pFile.write(message.getBytes());
        pValue.append(message);

        // Allocate RSA 1024 objects (RSAPublicKey and ALG_RSA_NOPAD cipher)
        apdu[OFFSET_P1] = 0x01;	
        TestAction("Allocate RSA 1024 objects", apdu, pValue,pFile);
        // Try to set random modulus
        apdu[OFFSET_P1] = 0x02;	
        TestAction("Set random modulus", apdu, pValue,pFile);
        // Try to set random exponent
        apdu[OFFSET_P1] = 0x03;	
        TestAction("Set random public exponent", apdu, pValue,pFile);
        // Try to initialize cipher with public key with random exponent
        apdu[OFFSET_P1] = 0x04;	
        TestAction("Initialize cipher with public key with random exponent", apdu, pValue,pFile);
        // Try to encrypt block of data
        apdu[OFFSET_P1] = 0x05;	
        TestAction("Use random public exponent", apdu, pValue,pFile);        
 
        return status;
    }
    
    public int TestAction(String actionName, byte apdu[], StringBuilder pValue, FileOutputStream pFile) throws Exception {
	int		status = STAT_OK;

        long     elapsedCard = 0;
	elapsedCard -= System.currentTimeMillis();

	String message;
	message = String.format("\r\n%1s;", actionName); 
        System.out.println(message);
        pFile.write(message.getBytes());
        pValue.append(message);
            
        ResponseAPDU resp = sendAPDU(apdu);
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
            elTimeStr = String.format("%1f", (double) elapsedCard / (float) CLOCKS_PER_SEC);

            message = String.format("yes;%1s sec;", elTimeStr); 
            System.out.println(message);
            pFile.write(message.getBytes());
            pValue.append(message);
	}

	return status;
    }

    public int GetSupportedAndParse(byte algClass, String algNames[], StringBuilder pValue, FileOutputStream pFile, byte algPartP2, int bForceTest) throws Exception {
        int         status = STAT_OK;
        byte        suppAlg[] = new byte[MAX_SUPP_ALG];
        long       elapsedCard;
        boolean     bNamePrinted = false;
        int         runTest = 1;

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        
        if (bForceTest == 1) {
            runTest = 1;
        }
        else {
            System.out.println("\n\nQ: Do you like to test supported algorithms from class '" + algNames[0] + "'?");
            System.out.println("Type 1 for yes, 0 for no: ");	
            runTest = Integer.decode(br.readLine());                
        }
        
        if (runTest == 1) {
            // CLEAR ARRAY FOR SUPPORTED ALGORITHMS
            Arrays.fill(suppAlg, SUPP_ALG_UNTOUCHED);

            // PREPARE SEPARATE APDU FOR EVERY SIGNALIZED P2 VALUE
            // IF P2 == 0 THEN ALL ALGORITHMS WITHIN GIVEN algClass WILL BE CHECK In SINGLE APDU
            // OTHERWISE, MULTIPLE APDU WILL BE ISSUED
            byte p2Start = (algPartP2 == 0) ? (byte) 0 : (byte) 1; 
            for (byte p2 = p2Start; p2 <= algPartP2; p2++) {

                elapsedCard = -System.currentTimeMillis();

                byte apdu[] = new byte[HEADER_LENGTH];
                apdu[OFFSET_CLA] = (byte) 0xB0;
                apdu[OFFSET_INS] = (byte) 0x70;
                apdu[OFFSET_P1] = algClass;
                apdu[OFFSET_P2] = p2;
                apdu[OFFSET_LC] = 0x00;

                ResponseAPDU resp = sendAPDU(apdu);
                if (resp.getSW() != 0x9000) {
                    String message = "Fail to obtain response for GetSupportedAndParse\r\n";
                    System.out.println(message);
                    pFile.write(message.getBytes());
                } else {
                    // SAVE TIME OF CARD RESPONSE
                    elapsedCard += System.currentTimeMillis();

                    String elTimeStr = "";
                    // OUTPUT REQUIRED TIME WHEN PARTITIONED CHECk WAS PERFORMED (NOT MULTIPLE ALGORITHMS IN SINGLE RUN)
                    if (algPartP2 > 0) { elTimeStr = String.format("%1f", (double) elapsedCard / (float) CLOCKS_PER_SEC);} 

                    // OK, STORE RESPONSE TO suppAlg ARRAY
                    byte temp[] = resp.getData();

                    if (temp[0] == algClass) {
                        
                        // PRINT algClass NAME ONLY ONES
                        if (!bNamePrinted) {
                            String message = "";
                            message += "\r\n"; message += algNames[0]; message += ";\r\n";
                            System.out.println(message);
                            pFile.write(message.getBytes());
                            pValue.append(message);

                            bNamePrinted = true;
                        }
                        for (int i = 1; i < temp.length; i++) {
                            // ONLY FILLED RESPONSES ARE STORED
                            if ((temp[i] != SUPP_ALG_UNTOUCHED) && ((short) (temp[i]&0xff) != SUPP_ALG_SEPARATOR) && ((short) (temp[i]&0xf0) != SUPP_ALG_SMALL_SEPARATOR)) {
                                suppAlg[i] = temp[i];  
                                
                                // Parse algorithm name 
                                String algorithmName = GetAlgorithmName(algNames[i]);
                                
                                // ALG NAME
                                String algState = "";
                                switch (suppAlg[i]) {
                                    case SUPP_ALG_SUPPORTED: { // SUPPORTED
                                        algState += algorithmName; algState += ";"; algState += "yes;"; algState += elTimeStr; algState += "\r\n";
                                        break;
                                    }
                                    case EXCEPTION_CODE_OFFSET + NO_SUCH_ALGORITHM: {
                                        algState += algorithmName; algState += ";"; algState += "no;"; algState += "\r\n";
                                        break;
                                    }
                                    case EXCEPTION_CODE_OFFSET + ILLEGAL_USE: {
                                        algState += algorithmName; algState += ";"; algState += "error(ILLEGAL_USE);"; algState += "\r\n";
                                        break;
                                    }
                                    case EXCEPTION_CODE_OFFSET + ILLEGAL_VALUE: {
                                        algState += algorithmName; algState += ";"; algState += "error(ILLEGAL_VALUE);"; algState += "\r\n";
                                        break;
                                    }
                                    case EXCEPTION_CODE_OFFSET + INVALID_INIT: {
                                        algState += algorithmName; algState += ";"; algState += "error(INVALID_INIT);"; algState += "\r\n";
                                        break;
                                    }
                                    case EXCEPTION_CODE_OFFSET + UNINITIALIZED_KEY: {
                                        algState += algorithmName; algState += ";"; algState += "error(UNINITIALIZED_KEY);"; algState += "\r\n";
                                        break;
                                    }    
                                    case 0x6f: {
                                        algState += algorithmName; algState += ";"; algState += "maybe;"; algState += "\r\n";
                                        break;
                                    }
                                    default: {
                                        // OTHER VALUE, IGNORE 
                                        System.out.println("Unknown value detected in AlgTest applet (0x" + Integer.toHexString(suppAlg[i] & 0xff) + "). Possibly, old version of AlTestJClient is used (try update)");
                                        break;
                                    }
                                }

                                if (algState.equals("")) {
                                }
                                else {
                                    System.out.println(algState);
                                    pFile.write(algState.getBytes());
                                    pValue.append(algState);
                                }
                            }
                        }
                    }
                    else { status = STAT_DATA_CORRUPTED; }
                }
            }
        }
        else {
            String message = "Testing of algorithm class '" + algNames[0] + "' skipped by user";
            System.out.println(message);
            pFile.write(message.getBytes());
        }

        return status;
    }
    
    /**
     * Sets up simulator using jCardSim API.
     * @param m_applet Class of on-card AlgTest to be installed in simulator.
     */
    public void MakeSim(Class m_applet){
    /* BUGBUG: we need to figure out how to support JCardSim in nice way (copy of class files, directory structure...)
        System.setProperty("com.licel.jcardsim.terminal.type", "2");
        CAD cad = new CAD(System.getProperties());
        simulator = (JavaxSmartCardInterface) cad.getCardInterface();
        AID appletAID = new AID(selectApplet, (short)0, (byte) selectApplet.length);
        // installs applet
        simulator.installApplet(appletAID, m_applet);
        // selects applet
        simulator.selectApplet(appletAID);
    */
    }
    public void PrintHelp () throws FileNotFoundException, IOException{
        System.out.println(helpString);
        
        System.out.println("Do you want to print supported parameters for AlgTest to separate file? 1 = YES, 0 = NO/r/n");
        Scanner sc = new Scanner(System.in);
        int answ = sc.nextInt();
        if (answ == 1){
            FileOutputStream file = new FileOutputStream("AlgTest_supported_parameters.txt");
            file.write(paramList.getBytes());
            System.out.println("List of supported parameters for AlgTest created in project folder.");
            
            if (file != null) file.close();
        }
    }
}
