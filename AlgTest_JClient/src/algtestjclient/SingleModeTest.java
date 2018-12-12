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
import algtest.Consts;
import algtest.JCAlgTestApplet;
import algtest.JCConsts;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.ResponseAPDU;

/**
 * AlgTest class to test smart card supported algorithms using multiple APDU's each testing one algorithm.
 * Requires 'AlgTestSinglePerApdu' applet installed on card.
 * Supports running without connected card using jCardSim. (www.jcardsim.org)
 * @author lukas.srom, github.com/petrs
 * @version 1.0
 */
public class SingleModeTest {
    public static CardMngr cardManager = null;
    
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
    

    
    /* Response values - send back by on card application as response to command APDU. */
    public static final short   UNKNOWN_ERROR       = -1;
    public static final short   SUPP_ALG_SUPPORTED  = 0;
    public static final short 	ILLEGAL_USE         = 5;
    public static final short 	ILLEGAL_VALUE       = 1;
    public static final short 	INVALID_INIT        = 4;
    public static final short 	NO_SUCH_ALGORITHM   = 3;
    public static final short 	UNINITIALIZED_KEY   = 2; 
    
    
    public static final String SIGNATURE_STR[] = {"javacard.crypto.Signature", 
        "ALG_DES_MAC4_NOPAD#&le;2.1", "ALG_DES_MAC8_NOPAD#&le;2.1", 
        "ALG_DES_MAC4_ISO9797_M1#&le;2.1", "ALG_DES_MAC8_ISO9797_M1#&le;2.1", "ALG_DES_MAC4_ISO9797_M2#&le;2.1", "ALG_DES_MAC8_ISO9797_M2#&le;2.1", 
        "ALG_DES_MAC4_PKCS5#&le;2.1", "ALG_DES_MAC8_PKCS5#&le;2.1", "ALG_RSA_SHA_ISO9796#&le;2.1", "ALG_RSA_SHA_PKCS1#&le;2.1", "ALG_RSA_MD5_PKCS1#&le;2.1", 
        "ALG_RSA_RIPEMD160_ISO9796#&le;2.1", "ALG_RSA_RIPEMD160_PKCS1#&le;2.1", "ALG_DSA_SHA#&le;2.1", "ALG_RSA_SHA_RFC2409#&le;2.1", 
        "ALG_RSA_MD5_RFC2409#&le;2.1", "ALG_ECDSA_SHA#2.2.0", "ALG_AES_MAC_128_NOPAD#2.2.0", "ALG_DES_MAC4_ISO9797_1_M2_ALG3#2.2.0", 
        "ALG_DES_MAC8_ISO9797_1_M2_ALG3#2.2.0", "ALG_RSA_SHA_PKCS1_PSS#2.2.0", "ALG_RSA_MD5_PKCS1_PSS#2.2.0", "ALG_RSA_RIPEMD160_PKCS1_PSS#2.2.0", 
        // 2.2.2
        "ALG_HMAC_SHA1#2.2.2", "ALG_HMAC_SHA_256#2.2.2", "ALG_HMAC_SHA_384#2.2.2", "ALG_HMAC_SHA_512#2.2.2", "ALG_HMAC_MD5#2.2.2", "ALG_HMAC_RIPEMD160#2.2.2", 
        "ALG_RSA_SHA_ISO9796_MR#2.2.2", "ALG_RSA_RIPEMD160_ISO9796_MR#2.2.2", "ALG_SEED_MAC_NOPAD#2.2.2", 
        //3.0.1
        "ALG_ECDSA_SHA_256#3.0.1", "ALG_ECDSA_SHA_384#3.0.1", "ALG_AES_MAC_192_NOPAD#3.0.1", "ALG_AES_MAC_256_NOPAD#3.0.1", "ALG_ECDSA_SHA_224#3.0.1", "ALG_ECDSA_SHA_512#3.0.1", 
        "ALG_RSA_SHA_224_PKCS1#3.0.1", "ALG_RSA_SHA_256_PKCS1#3.0.1", "ALG_RSA_SHA_384_PKCS1#3.0.1", "ALG_RSA_SHA_512_PKCS1#3.0.1", 
        "ALG_RSA_SHA_224_PKCS1_PSS#3.0.1", "ALG_RSA_SHA_256_PKCS1_PSS#3.0.1", "ALG_RSA_SHA_384_PKCS1_PSS#3.0.1", "ALG_RSA_SHA_512_PKCS1_PSS#3.0.1",
        //3.0.4
        "ALG_DES_MAC4_ISO9797_1_M1_ALG3#3.0.4", "ALG_DES_MAC8_ISO9797_1_M1_ALG3#3.0.4",
        //3.0.5
        "ALG_AES_CMAC_128#3.0.5"
    };
    public static final int SIGNATURE_STR_LAST_INDEX = JCConsts.Signature_ALG_AES_CMAC_128;
    
    
    public static final String CIPHER_STR[] = {"javacardx.crypto.Cipher", 
        "ALG_DES_CBC_NOPAD#&le;2.1", "ALG_DES_CBC_ISO9797_M1#&le;2.1", "ALG_DES_CBC_ISO9797_M2#&le;2.1", "ALG_DES_CBC_PKCS5#&le;2.1", 
        "ALG_DES_ECB_NOPAD#&le;2.1", "ALG_DES_ECB_ISO9797_M1#&le;2.1", "ALG_DES_ECB_ISO9797_M2#&le;2.1", "ALG_DES_ECB_PKCS5#&le;2.1",
        "ALG_RSA_ISO14888#&le;2.1", "ALG_RSA_PKCS1#&le;2.1", "ALG_RSA_ISO9796#&le;2.1", 
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
    public static final int CIPHER_STR_LAST_INDEX = JCConsts.Cipher_ALG_AES_ECB_PKCS5;
    

    public static final String KEYAGREEMENT_STR[] = {"javacard.security.KeyAgreement", 
        //2.2.1
        "ALG_EC_SVDP_DH/ALG_EC_SVDP_DH_KDF#2.2.1", "ALG_EC_SVDP_DHC/ALG_EC_SVDP_DHC_KDF#2.2.1",
        //3.0.1
        //was incorrectly like this: "ALG_EC_SVDP_DH_KDF#3.0.1", "ALG_EC_SVDP_DH_PLAIN#3.0.1", "ALG_EC_SVDP_DHC_KDF#3.0.1", "ALG_EC_SVDP_DHC_PLAIN#3.0.1",
        "ALG_EC_SVDP_DH_PLAIN#3.0.1", "ALG_EC_SVDP_DHC_PLAIN#3.0.1", 
        //3.0.5    
        "ALG_EC_PACE_GM#3.0.5", "ALG_EC_SVDP_DH_PLAIN_XY#3.0.5", "ALG_DH_PLAIN#3.0.5"
    };
    public static final int KEYAGREEMENT_STR_LAST_INDEX = JCConsts.KeyAgreement_ALG_DH_PLAIN;
    
    public static final String BIOBUILDER_STR[] = {"javacardx.biometry.BioBuilder",
        //2.2.2
        "FACIAL_FEATURE#2.2.2", "VOICE_PRINT#2.2.2", "FINGERPRINT#2.2.2", "IRIS_SCAN#2.2.2", "RETINA_SCAN#2.2.2", "HAND_GEOMETRY#2.2.2",
        "SIGNATURE#2.2.2", "KEYSTROKES#2.2.2", "LIP_MOVEMENT#2.2.2", "THERMAL_FACE#2.2.2", "THERMAL_HAND#2.2.2", "GAIT_STYLE#2.2.2",
        "BODY_ODOR#2.2.2", "DNA_SCAN#2.2.2", "EAR_GEOMETRY#2.2.2", "FINGER_GEOMETRY#2.2.2", "PALM_GEOMETRY#2.2.2", "VEIN_PATTERN#2.2.2"
        // ommit as password has constant 31 which is not continuious with previous ones "PASSWORD#2.2.2"
    };
    
    public static final String AEADCIPHER_STR[] = {"javacardx.crypto.AEADCipher",
        //3.0.5
        "CIPHER_AES_CCM#3.0.5", "CIPHER_AES_GCM#3.0.5", "ALG_AES_CCM#3.0.5", "ALG_AES_GCM#3.0.5"
    };    
    
    /**
     * String array used in KeyBuilder testing for printing alg names.
     */
    public static final String KEYBUILDER_STR[] = {
        "javacard.security.KeyBuilder", 
        "TYPE_DES_TRANSIENT_RESET#&le;2.1", "TYPE_DES_TRANSIENT_DESELECT#&le;2.1", "TYPE_DES LENGTH_DES#&le;2.1", "TYPE_DES LENGTH_DES3_2KEY#&le;2.1", "TYPE_DES LENGTH_DES3_3KEY#&le;2.1",
        //2.2.0
        "TYPE_AES_TRANSIENT_RESET#2.2.0", "TYPE_AES_TRANSIENT_DESELECT#2.2.0", "TYPE_AES LENGTH_AES_128#2.2.0", "TYPE_AES LENGTH_AES_192#2.2.0", "TYPE_AES LENGTH_AES_256#2.2.0",
        "TYPE_RSA_PUBLIC LENGTH_RSA_512#&le;2.1", "TYPE_RSA_PUBLIC LENGTH_RSA_736#2.2.0", "TYPE_RSA_PUBLIC LENGTH_RSA_768#2.2.0", "TYPE_RSA_PUBLIC LENGTH_RSA_896#2.2.0",
            "TYPE_RSA_PUBLIC LENGTH_RSA_1024#&le;2.1", "TYPE_RSA_PUBLIC LENGTH_RSA_1280#2.2.0", "TYPE_RSA_PUBLIC LENGTH_RSA_1536#2.2.0", "TYPE_RSA_PUBLIC LENGTH_RSA_1984#2.2.0", "TYPE_RSA_PUBLIC LENGTH_RSA_2048#&le;2.1", "TYPE_RSA_PUBLIC LENGTH_RSA_3072#never#0", "TYPE_RSA_PUBLIC LENGTH_RSA_4096#3.0.1",
        "TYPE_RSA_PRIVATE LENGTH_RSA_512#&le;2.1", "TYPE_RSA_PRIVATE LENGTH_RSA_736#2.2.0", "TYPE_RSA_PRIVATE LENGTH_RSA_768#2.2.0", "TYPE_RSA_PRIVATE LENGTH_RSA_896#2.2.0",
            "TYPE_RSA_PRIVATE LENGTH_RSA_1024#&le;2.1", "TYPE_RSA_PRIVATE LENGTH_RSA_1280#2.2.0", "TYPE_RSA_PRIVATE LENGTH_RSA_1536#2.2.0", "TYPE_RSA_PRIVATE LENGTH_RSA_1984#2.2.0", "TYPE_RSA_PRIVATE LENGTH_RSA_2048#&le;2.1", "TYPE_RSA_PRIVATE LENGTH_RSA_3072#never#0", "TYPE_RSA_PRIVATE LENGTH_RSA_4096#3.0.1", 
            "TYPE_RSA_PRIVATE_TRANSIENT_RESET#3.0.1", "TYPE_RSA_PRIVATE_TRANSIENT_DESELECT#3.0.1",
        "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_512#&le;2.1", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_736#2.2.0", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_768#2.2.0", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_896#2.2.0",
            "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1024#&le;2.1", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1280#2.2.0", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1536#2.2.0", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1984#2.2.0", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_2048#&le;2.1", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_3072#never#0", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_4096#3.0.1",
            "TYPE_RSA_CRT_PRIVATE_TRANSIENT_RESET#3.0.1", "TYPE_RSA_CRT_PRIVATE_TRANSIENT_DESELECT#3.0.1",
        "TYPE_DSA_PRIVATE LENGTH_DSA_512#&le;2.1", "TYPE_DSA_PRIVATE LENGTH_DSA_768#&le;2.1", "TYPE_DSA_PRIVATE LENGTH_DSA_1024#&le;2.1", "TYPE_DSA_PRIVATE_TRANSIENT_RESET#3.0.1", "TYPE_DSA_PRIVATE_TRANSIENT_DESELECT#3.0.1", 
        "TYPE_DSA_PUBLIC LENGTH_DSA_512#&le;2.1", "TYPE_DSA_PUBLIC LENGTH_DSA_768#&le;2.1", "TYPE_DSA_PUBLIC LENGTH_DSA_1024#&le;2.1", 
        "TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_113#2.2.0", "TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_131#2.2.0", "TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_163#2.2.0", "TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_193#2.2.0", "TYPE_EC_F2M_PRIVATE_TRANSIENT_RESET#3.0.1", "TYPE_EC_F2M_PRIVATE_TRANSIENT_DESELECT#3.0.1",
        "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_112#2.2.0", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_128#2.2.0", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_160#2.2.0", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_192#2.2.0", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_224#3.0.1", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_256#3.0.1", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_320#never#0", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_384#3.0.1", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_512#never#0", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_521#3.0.4", "TYPE_EC_FP_PRIVATE_TRANSIENT_RESET#3.0.1", "TYPE_EC_FP_PRIVATE_TRANSIENT_DESELECT#3.0.1",
        "TYPE_KOREAN_SEED_TRANSIENT_RESET#2.2.2", "TYPE_KOREAN_SEED_TRANSIENT_DESELECT#2.2.2", "TYPE_KOREAN_SEED LENGTH_KOREAN_SEED_128#2.2.2", 
        "TYPE_HMAC_TRANSIENT_RESET#2.2.2", "TYPE_HMAC_TRANSIENT_DESELECT#2.2.2", "TYPE_HMAC LENGTH_HMAC_SHA_1_BLOCK_64#2.2.2", "TYPE_HMAC LENGTH_HMAC_SHA_256_BLOCK_64#2.2.2", "TYPE_HMAC LENGTH_HMAC_SHA_384_BLOCK_64#2.2.2", "TYPE_HMAC LENGTH_HMAC_SHA_512_BLOCK_64#2.2.2",
    };
    
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
//        "ALG_EC_FP LENGTH_EC_FP_112#2.2.1", "ALG_EC_FP LENGTH_EC_FP_128#2.2.1", "ALG_EC_FP LENGTH_EC_FP_160#2.2.1", "ALG_EC_FP LENGTH_EC_FP_192#2.2.1", "ALG_EC_FP LENGTH_EC_FP_224#3.0.1", "ALG_EC_FP LENGTH_EC_FP_256#3.0.1", "ALG_EC_FP LENGTH_EC_FP_320#never#0", "ALG_EC_FP LENGTH_EC_FP_384#3.0.1", "ALG_EC_FP LENGTH_EC_FP_512#never#0", "ALG_EC_FP LENGTH_EC_FP_521#3.0.4"
        "ALG_EC_FP LENGTH_EC_FP_112#2.2.1", "ALG_EC_FP LENGTH_EC_FP_128#2.2.1", "ALG_EC_FP LENGTH_EC_FP_160#2.2.1", "ALG_EC_FP LENGTH_EC_FP_192#2.2.1", "ALG_EC_FP LENGTH_EC_FP_224#3.0.1", "ALG_EC_FP LENGTH_EC_FP_256#3.0.1", "ALG_EC_FP LENGTH_EC_FP_384#3.0.1", "ALG_EC_FP LENGTH_EC_FP_521#3.0.4"
    };
    
    public static final String MESSAGEDIGEST_STR[] = {"javacard.security.MessageDigest", 
        "ALG_SHA#&le;2.1", "ALG_MD5#&le;2.1", "ALG_RIPEMD160#&le;2.1", 
        //2.2.2
        "ALG_SHA_256#2.2.2", "ALG_SHA_384#2.2.2", "ALG_SHA_512#2.2.2", 
        //3.0.1
        "ALG_SHA_224#3.0.1",
        //3.0.5
        "ALG_SHA3_224#3.0.5", "ALG_SHA3_256#3.0.5", "ALG_SHA3_384#3.0.5", "ALG_SHA3_512#3.0.5"   
    }; 
    public static final int MESSAGEDIGEST_STR_LAST_INDEX = JCConsts.MessageDigest_ALG_SHA3_512;    
            
    public static final String RANDOMDATA_STR[] = {"javacard.security.RandomData", 
        "ALG_PSEUDO_RANDOM#&le;2.1", "ALG_SECURE_RANDOM#&le;2.1",
        //3.0.5
        "ALG_TRNG#3.0.5", "ALG_ALG_PRESEEDED_DRBG#3.0.5", "ALG_FAST#3.0.5", "ALG_KEYGENERATION#3.0.5"
    }; 
    public static final int RANDOMDATA_STR_LAST_INDEX = JCConsts.RandomData_ALG_KEYGENERATION;
            
    public static final String CHECKSUM_STR[] = {"javacard.security.Checksum", "ALG_ISO3309_CRC16#2.2.1", "ALG_ISO3309_CRC32#2.2.1"}; 
    public static final int CHECKSUM_STR_LAST_INDEX = JCConsts.Checksum_ALG_ISO3309_CRC32;
    
    
    public static final String JCSYSTEM_STR[] = {"javacard.framework.JCSystem", "JCSystem.getVersion()[Major.Minor]#&le;2.1", 
        "JCSystem.isObjectDeletionSupported#2.2.0", "JCSystem.MEMORY_TYPE_PERSISTENT#2.2.1", "JCSystem.MEMORY_TYPE_TRANSIENT_RESET#2.2.1", 
        "JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT#2.2.1", "JCSystem.getMaxCommitCapacity()#2.1"}; 

    public static final String RAWRSA_1024_STR[] = {"Variable RSA 1024 - support for variable public exponent. If supported, user-defined fast modular exponentiation can be executed on the smart card via cryptographic coprocessor. This is very specific feature and you will probably not need it", 
        "Allocate RSA 1024 objects", "Set random modulus", "Set random public exponent", "Initialize cipher with public key with random exponent", "Use random public exponent"}; 

    public static final String PACKAGE_AID_STR[] = {"<a name=\"package_support\"></a>Package AID support test - a direct testing of supported packages from the standard JavaCard API including version. Not all constants from supported package are necessarily supported.",
        "000107A0000000620001#2.1", // java.lang
        "000107A0000000620002#2.2.0",  // java.io
        "000107A0000000620003#2.2.0", // java.rmi
        // javacard.framework
        "000107A0000000620101#2.1", "010107A0000000620101#2.2.0", "020107A0000000620101#2.2.1",
        "030107A0000000620101#2.2.2", "040107A0000000620101#3.0.1", "050107A0000000620101#3.0.4",
        "060107A0000000620101#3.0.5",
        // javacard.framework.service
        "000108A000000062010101#2.2.0",
        // javacard.security
        "000107A0000000620102#2.1", "010107A0000000620102#2.1.1", "020107A0000000620102#2.2.1",
        "030107A0000000620102#2.2.2", "040107A0000000620102#3.0.1", "050107A0000000620102#3.0.4",
        "060107A0000000620102#3.0.5",
        // javacardx.crypto
        "000107A0000000620201#2.1", "010107A0000000620201#2.1.1", "020107A0000000620201#2.2.1",
        "030107A0000000620201#2.2.2", "040107A0000000620201#3.0.1", "050107A0000000620201#3.0.4",
        "060107A0000000620201#3.0.5",
        // javacardx.biometry (starting directly from version 1.2 - previous versions all from 2.2.2)
        "000107A0000000620202#2.2.2", "010107A0000000620202#2.2.2", "020107A0000000620202#2.2.2",
        "030107A0000000620202#3.0.5",
        "000107A0000000620203#2.2.2",  // javacardx.external
        "000107A0000000620204#3.0.5",  // javacardx.biometry1toN
        "000107A0000000620205#3.0.5",  // javacardx.security
        // javacardx.framework.util
        "000108A000000062020801#2.2.2", "010108A000000062020801#3.0.5",
        "000109A00000006202080101#2.2.2",  // javacardx.framework.util.intx
        "000108A000000062020802#2.2.2",  // javacardx.framework.math
        "000108A000000062020803#2.2.2",  // javacardx.framework.tlv
        "000108A000000062020804#3.0.4",  // javacardx.framework.string
        "000107A0000000620209#2.2.2",  // javacardx.apdu
        "000108A000000062020901#3.0.5"  // javacardx.apdu.util
    };

    public static final Map<String, String> PACKAGE_AID_NAMES_STR;
    static {
        PACKAGE_AID_NAMES_STR = new HashMap<>();
        PACKAGE_AID_NAMES_STR.put("000107A0000000620001", "java.lang v1.0"); 
        PACKAGE_AID_NAMES_STR.put("000107A0000000620002", "java.io v1.0");
        PACKAGE_AID_NAMES_STR.put("000107A0000000620003", "java.rmi v1.0");
        // javacard.framework
        PACKAGE_AID_NAMES_STR.put("000107A0000000620101", "javacard.framework v1.0");
        PACKAGE_AID_NAMES_STR.put("010107A0000000620101", "javacard.framework v1.1");
        PACKAGE_AID_NAMES_STR.put("020107A0000000620101", "javacard.framework v1.2");
        PACKAGE_AID_NAMES_STR.put("030107A0000000620101", "javacard.framework v1.3");
        PACKAGE_AID_NAMES_STR.put("040107A0000000620101", "javacard.framework v1.4");
        PACKAGE_AID_NAMES_STR.put("050107A0000000620101", "javacard.framework v1.5");
        PACKAGE_AID_NAMES_STR.put("060107A0000000620101", "javacard.framework v1.6");
        // javacard.framework.service
        PACKAGE_AID_NAMES_STR.put("000108A000000062010101", "javacard.framework.service v1.0");
        // javacard.security
        PACKAGE_AID_NAMES_STR.put("000107A0000000620102", "javacard.security v1.0");
        PACKAGE_AID_NAMES_STR.put("010107A0000000620102", "javacard.security v1.1");
        PACKAGE_AID_NAMES_STR.put("020107A0000000620102", "javacard.security v1.2");
        PACKAGE_AID_NAMES_STR.put("030107A0000000620102", "javacard.security v1.3");
        PACKAGE_AID_NAMES_STR.put("040107A0000000620102", "javacard.security v1.4");
        PACKAGE_AID_NAMES_STR.put("050107A0000000620102", "javacard.security v1.5");
        PACKAGE_AID_NAMES_STR.put("060107A0000000620102", "javacard.security v1.6");
        // javacardx.crypto
        PACKAGE_AID_NAMES_STR.put("000107A0000000620201", "javacardx.crypto v1.0");
        PACKAGE_AID_NAMES_STR.put("010107A0000000620201", "javacardx.crypto v1.1");
        PACKAGE_AID_NAMES_STR.put("020107A0000000620201", "javacardx.crypto v1.2");
        PACKAGE_AID_NAMES_STR.put("030107A0000000620201", "javacardx.crypto v1.3");
        PACKAGE_AID_NAMES_STR.put("040107A0000000620201", "javacardx.crypto v1.4");
        PACKAGE_AID_NAMES_STR.put("050107A0000000620201", "javacardx.crypto v1.5");
        PACKAGE_AID_NAMES_STR.put("060107A0000000620201", "javacardx.crypto v1.6");
        // javacardx.biometry (starting directly from version 1.2 - previous versions all from 2.2.2)
        PACKAGE_AID_NAMES_STR.put("000107A0000000620202", "javacardx.biometry v1.0");
        PACKAGE_AID_NAMES_STR.put("010107A0000000620202", "javacardx.biometry v1.1");
        PACKAGE_AID_NAMES_STR.put("020107A0000000620202", "javacardx.biometry v1.2");
        PACKAGE_AID_NAMES_STR.put("030107A0000000620202", "javacardx.biometry v1.3");

        PACKAGE_AID_NAMES_STR.put("000107A0000000620203", "javacardx.external v1.0");
        PACKAGE_AID_NAMES_STR.put("000107A0000000620204", "javacardx.biometry1toN v1.0");
        PACKAGE_AID_NAMES_STR.put("000107A0000000620205", "javacardx.security v1.0");
        // javacardx.framework.util
        PACKAGE_AID_NAMES_STR.put("000108A000000062020801", "javacardx.framework.util v1.0");
        PACKAGE_AID_NAMES_STR.put("010108A000000062020801", "javacardx.framework.util v1.1");
        PACKAGE_AID_NAMES_STR.put("000109A00000006202080101", "javacardx.framework.util.intx v1.0");
        PACKAGE_AID_NAMES_STR.put("000108A000000062020802", "javacardx.framework.math v1.0");
        PACKAGE_AID_NAMES_STR.put("000108A000000062020803", "javacardx.framework.tlv v1.0");
        PACKAGE_AID_NAMES_STR.put("000108A000000062020804", "javacardx.framework.string v1.0"); 
        PACKAGE_AID_NAMES_STR.put("000107A0000000620209", "javacardx.apdu v1.0");
        PACKAGE_AID_NAMES_STR.put("000108A000000062020901", "javacardx.apdu.util v1.0");
    }
    
    public static final String EXTENDEDAPDU_STR[] = {"javacardx.apdu.ExtendedLength", "Extended APDU#2.2.2"}; 

    public static final String BASIC_INFO[] = {"Basic info", "JavaCard support version"}; 
   
    public static final String[] ALL_CLASSES_STR[] = {
        BASIC_INFO, JCSYSTEM_STR, EXTENDEDAPDU_STR, CIPHER_STR, SIGNATURE_STR, MESSAGEDIGEST_STR, RANDOMDATA_STR, KEYBUILDER_STR, 
        KEYPAIR_RSA_STR, KEYPAIR_RSACRT_STR, KEYPAIR_DSA_STR, KEYPAIR_EC_F2M_STR, 
        KEYPAIR_EC_FP_STR, KEYAGREEMENT_STR, CHECKSUM_STR, RAWRSA_1024_STR, PACKAGE_AID_STR
    };
    
    
    /* Algorithms from class 'javacard.security.KeyPair'. */
    public static final byte ALG_RSA = 1;
    public static final byte ALG_RSA_CRT = 2;
    public static final byte ALG_DSA = 3;
    public static final byte ALG_EC_F2M = 4;
    public static final byte ALG_EC_FP = 5;
    
    public static final Map<String, KBTestCfg> KEYBUILDER_TEST_CFGS;
    static {
        KEYBUILDER_TEST_CFGS = new HashMap<>();
        KEYBUILDER_TEST_CFGS.put("TYPE_DES_TRANSIENT_RESET#&le;2.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_DES_TRANSIENT_RESET, (short) 64));
        KEYBUILDER_TEST_CFGS.put("TYPE_DES_TRANSIENT_DESELECT#&le;2.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_DES_TRANSIENT_DESELECT, (short) 64));
        KEYBUILDER_TEST_CFGS.put("TYPE_DES LENGTH_DES#&le;2.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_DES, (short) 64));
        KEYBUILDER_TEST_CFGS.put("TYPE_DES LENGTH_DES3_2KEY#&le;2.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_DES, (short) 128));
        KEYBUILDER_TEST_CFGS.put("TYPE_DES LENGTH_DES3_3KEY#&le;2.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_DES, (short) 192));
        KEYBUILDER_TEST_CFGS.put("TYPE_AES_TRANSIENT_RESET#2.2.0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_AES_TRANSIENT_RESET, (short) 128));
        KEYBUILDER_TEST_CFGS.put("TYPE_AES_TRANSIENT_DESELECT#2.2.0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_AES_TRANSIENT_DESELECT, (short) 128));
        KEYBUILDER_TEST_CFGS.put("TYPE_AES LENGTH_AES_128#2.2.0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_AES, (short) 128));
        KEYBUILDER_TEST_CFGS.put("TYPE_AES LENGTH_AES_192#2.2.0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_AES, (short) 192));
        KEYBUILDER_TEST_CFGS.put("TYPE_AES LENGTH_AES_256#2.2.0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_AES, (short) 256));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_PUBLIC LENGTH_RSA_512#&le;2.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, (short) 512));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_PUBLIC LENGTH_RSA_736#2.2.0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, (short) 736));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_PUBLIC LENGTH_RSA_768#2.2.0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, (short) 768));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_PUBLIC LENGTH_RSA_896#2.2.0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, (short) 896));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_PUBLIC LENGTH_RSA_1024#&le;2.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, (short) 1024));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_PUBLIC LENGTH_RSA_1280#2.2.0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, (short) 1280));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_PUBLIC LENGTH_RSA_1536#2.2.0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, (short) 1536));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_PUBLIC LENGTH_RSA_1984#2.2.0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, (short) 1984));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_PUBLIC LENGTH_RSA_2048#&le;2.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, (short) 2048));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_PUBLIC LENGTH_RSA_3072#never#0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, (short) 3072));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_PUBLIC LENGTH_RSA_4096#3.0.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_PUBLIC, (short) 4096));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_PRIVATE LENGTH_RSA_512#&le;2.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, (short) 512));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_PRIVATE LENGTH_RSA_736#2.2.0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, (short) 736));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_PRIVATE LENGTH_RSA_768#2.2.0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, (short) 768));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_PRIVATE LENGTH_RSA_896#2.2.0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, (short) 896));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_PRIVATE LENGTH_RSA_1024#&le;2.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, (short) 1024));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_PRIVATE LENGTH_RSA_1280#2.2.0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, (short) 1280));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_PRIVATE LENGTH_RSA_1536#2.2.0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, (short) 1536));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_PRIVATE LENGTH_RSA_1984#2.2.0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, (short) 1984));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_PRIVATE LENGTH_RSA_2048#&le;2.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, (short) 2048));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_PRIVATE LENGTH_RSA_3072#never#0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, (short) 3072));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_PRIVATE LENGTH_RSA_4096#3.0.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE, (short) 4096));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_PRIVATE_TRANSIENT_RESET#3.0.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE_TRANSIENT_RESET, (short) 1024));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_PRIVATE_TRANSIENT_DESELECT#3.0.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_PRIVATE_TRANSIENT_DESELECT, (short) 1024));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_CRT_PRIVATE LENGTH_RSA_512#&le;2.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, (short) 512));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_CRT_PRIVATE LENGTH_RSA_736#2.2.0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, (short) 736));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_CRT_PRIVATE LENGTH_RSA_768#2.2.0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, (short) 768));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_CRT_PRIVATE LENGTH_RSA_896#2.2.0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, (short) 896));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1024#&le;2.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, (short) 1024));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1280#2.2.0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, (short) 1280));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1536#2.2.0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, (short) 1536));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1984#2.2.0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, (short) 1984));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_CRT_PRIVATE LENGTH_RSA_2048#&le;2.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, (short) 2048));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_CRT_PRIVATE LENGTH_RSA_3072#never#0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, (short) 3072));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_CRT_PRIVATE LENGTH_RSA_4096#3.0.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE, (short) 4096));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_CRT_PRIVATE_TRANSIENT_RESET#3.0.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE_TRANSIENT_RESET, (short) 1024));
        KEYBUILDER_TEST_CFGS.put("TYPE_RSA_CRT_PRIVATE_TRANSIENT_DESELECT#3.0.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE_TRANSIENT_DESELECT, (short) 1024));
        KEYBUILDER_TEST_CFGS.put("TYPE_DSA_PRIVATE LENGTH_DSA_512#&le;2.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_DSA_PRIVATE, (short) 512));
        KEYBUILDER_TEST_CFGS.put("TYPE_DSA_PRIVATE LENGTH_DSA_768#&le;2.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_DSA_PRIVATE, (short) 768));
        KEYBUILDER_TEST_CFGS.put("TYPE_DSA_PRIVATE LENGTH_DSA_1024#&le;2.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_DSA_PRIVATE, (short) 1024));
        KEYBUILDER_TEST_CFGS.put("TYPE_DSA_PRIVATE_TRANSIENT_RESET#3.0.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_DSA_PRIVATE_TRANSIENT_RESET, (short) 1024));
        KEYBUILDER_TEST_CFGS.put("TYPE_DSA_PRIVATE_TRANSIENT_DESELECT#3.0.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_DSA_PRIVATE_TRANSIENT_DESELECT, (short) 1024));
        KEYBUILDER_TEST_CFGS.put("TYPE_DSA_PUBLIC LENGTH_DSA_512#&le;2.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_DSA_PUBLIC, (short) 512));
        KEYBUILDER_TEST_CFGS.put("TYPE_DSA_PUBLIC LENGTH_DSA_768#&le;2.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_DSA_PUBLIC, (short) 768));
        KEYBUILDER_TEST_CFGS.put("TYPE_DSA_PUBLIC LENGTH_DSA_1024#&le;2.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_DSA_PUBLIC, (short) 1024));
        KEYBUILDER_TEST_CFGS.put("TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_113#2.2.0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_EC_F2M_PRIVATE, (short) 113));
        KEYBUILDER_TEST_CFGS.put("TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_131#2.2.0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_EC_F2M_PRIVATE, (short) 131));
        KEYBUILDER_TEST_CFGS.put("TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_163#2.2.0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_EC_F2M_PRIVATE, (short) 163));
        KEYBUILDER_TEST_CFGS.put("TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_193#2.2.0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_EC_F2M_PRIVATE, (short) 193));
        KEYBUILDER_TEST_CFGS.put("TYPE_EC_F2M_PRIVATE_TRANSIENT_RESET#3.0.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_EC_F2M_PRIVATE_TRANSIENT_RESET, (short) 193));
        KEYBUILDER_TEST_CFGS.put("TYPE_EC_F2M_PRIVATE_TRANSIENT_DESELECT#3.0.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_EC_F2M_PRIVATE_TRANSIENT_DESELECT, (short) 193));
        KEYBUILDER_TEST_CFGS.put("TYPE_EC_FP_PRIVATE LENGTH_EC_FP_112#2.2.0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, (short) 112));
        KEYBUILDER_TEST_CFGS.put("TYPE_EC_FP_PRIVATE LENGTH_EC_FP_128#2.2.0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, (short) 128));
        KEYBUILDER_TEST_CFGS.put("TYPE_EC_FP_PRIVATE LENGTH_EC_FP_160#2.2.0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, (short) 160));
        KEYBUILDER_TEST_CFGS.put("TYPE_EC_FP_PRIVATE LENGTH_EC_FP_192#2.2.0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, (short) 192));
        KEYBUILDER_TEST_CFGS.put("TYPE_EC_FP_PRIVATE LENGTH_EC_FP_224#3.0.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, (short) 224));
        KEYBUILDER_TEST_CFGS.put("TYPE_EC_FP_PRIVATE LENGTH_EC_FP_256#3.0.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, (short) 256));
        KEYBUILDER_TEST_CFGS.put("TYPE_EC_FP_PRIVATE LENGTH_EC_FP_320#never#0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, (short) 320));
        KEYBUILDER_TEST_CFGS.put("TYPE_EC_FP_PRIVATE LENGTH_EC_FP_384#3.0.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, (short) 384));
        KEYBUILDER_TEST_CFGS.put("TYPE_EC_FP_PRIVATE LENGTH_EC_FP_512#never#0", new KBTestCfg(JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, (short) 512));
        KEYBUILDER_TEST_CFGS.put("TYPE_EC_FP_PRIVATE LENGTH_EC_FP_521#3.0.4", new KBTestCfg(JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE, (short) 521));
        KEYBUILDER_TEST_CFGS.put("TYPE_EC_FP_PRIVATE_TRANSIENT_RESET#3.0.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE_TRANSIENT_RESET, (short) 128));
        KEYBUILDER_TEST_CFGS.put("TYPE_EC_FP_PRIVATE_TRANSIENT_DESELECT#3.0.1", new KBTestCfg(JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE_TRANSIENT_DESELECT, (short) 128));
        KEYBUILDER_TEST_CFGS.put("TYPE_KOREAN_SEED_TRANSIENT_RESET#2.2.2", new KBTestCfg(JCConsts.KeyBuilder_TYPE_KOREAN_SEED_TRANSIENT_RESET, (short) 128));
        KEYBUILDER_TEST_CFGS.put("TYPE_KOREAN_SEED_TRANSIENT_DESELECT#2.2.2", new KBTestCfg(JCConsts.KeyBuilder_TYPE_KOREAN_SEED_TRANSIENT_DESELECT, (short) 128));
        KEYBUILDER_TEST_CFGS.put("TYPE_KOREAN_SEED LENGTH_KOREAN_SEED_128#2.2.2", new KBTestCfg(JCConsts.KeyBuilder_TYPE_KOREAN_SEED, (short) 128));
        KEYBUILDER_TEST_CFGS.put("TYPE_HMAC_TRANSIENT_RESET#2.2.2", new KBTestCfg(JCConsts.KeyBuilder_TYPE_HMAC_TRANSIENT_RESET, (short) 64));
        KEYBUILDER_TEST_CFGS.put("TYPE_HMAC_TRANSIENT_DESELECT#2.2.2", new KBTestCfg(JCConsts.KeyBuilder_TYPE_HMAC_TRANSIENT_DESELECT, (short) 64));
        KEYBUILDER_TEST_CFGS.put("TYPE_HMAC LENGTH_HMAC_SHA_1_BLOCK_64#2.2.2", new KBTestCfg(JCConsts.KeyBuilder_TYPE_HMAC, (short) 64));
        KEYBUILDER_TEST_CFGS.put("TYPE_HMAC LENGTH_HMAC_SHA_256_BLOCK_64#2.2.2", new KBTestCfg(JCConsts.KeyBuilder_TYPE_HMAC, (short) 64));
        KEYBUILDER_TEST_CFGS.put("TYPE_HMAC LENGTH_HMAC_SHA_384_BLOCK_64#2.2.2", new KBTestCfg(JCConsts.KeyBuilder_TYPE_HMAC, (short) 128));
        KEYBUILDER_TEST_CFGS.put("TYPE_HMAC LENGTH_HMAC_SHA_512_BLOCK_64#2.2.2", new KBTestCfg(JCConsts.KeyBuilder_TYPE_HMAC, (short) 128));
    }
    
    
    /**
     * Byte array containing lengths of keys in 'javacard.security.KeyPair' class in hexadecimal form.
     * Every line is one key length value.
     */
    public static final byte[] KEY_LENGTHS_HEX = {
        // class EC FP [00 - 15]
        (byte) 0x00, (byte) 0x70, // [00,01] - 112
        (byte) 0x00, (byte) 0x80, // [02,03] - 128
        (byte) 0x00, (byte) 0xA0, // [04,05] - 160
        (byte) 0x00, (byte) 0xC0, // [06,07] - 192
        (byte) 0x00, (byte) 0xE0, // [08,09] - 224
        (byte) 0x01, (byte) 0x00, // [10,11] - 256
        (byte) 0x01, (byte) 0x80, // [12,13] - 384
        (byte) 0x02, (byte) 0x09, // [14,15] - 521
        // class EC F2M [16 - 23]
        (byte) 0x00, (byte) 0x71, // [16,17] - 113
        (byte) 0x00, (byte) 0x83, // [18,19] - 131
        (byte) 0x00, (byte) 0xA3, // [20,21] - 163
        (byte) 0x00, (byte) 0xC1, // [22,23] - 193
        // classes RSA, RSACRT [24 - 43]
        (byte) 0x02, (byte) 0x00, // [24,25] - 512
        (byte) 0x02, (byte) 0xE0, // [26,27] - 736
        (byte) 0x03, (byte) 0x00, // [28,29] - 768
        (byte) 0x03, (byte) 0x80, // [30,31] - 896
        (byte) 0x04, (byte) 0x00, // [32,33] - 1024
        (byte) 0x05, (byte) 0x00, // [34,35] - 1280
        (byte) 0x06, (byte) 0x00, // [36,37] - 1536
        (byte) 0x07, (byte) 0xC0, // [38,39] - 1984
        (byte) 0x08, (byte) 0x00, // [40,41] - 2048
        (byte) 0x10, (byte) 0x00, // [42,43] - 4096
        // class DES [44 - 49]
        (byte) 0x00, (byte) 0x40, // [44,45] - 64
        (byte) 0x00, (byte) 0x80, // [46,47] - 128
        (byte) 0x00, (byte) 0xC0, // [48,49] - 192
        // class AES [50 - 55]
        (byte) 0x00, (byte) 0x80, // [50,51] - 128
        (byte) 0x00, (byte) 0xC0, // [52,53] - 192
        (byte) 0x01, (byte) 0x00, // [54,55] - 256
        // class HMAC [56 - 63]
        (byte) 0x00, (byte) 0x01, // [56,57] - 1
        (byte) 0x01, (byte) 0x00, // [58,59] - 256
        (byte) 0x01, (byte) 0x80, // [60,61] - 384
        (byte) 0x02, (byte) 0x00, // [62,63] - 512
    };    
/* 20181201 Version with added EC FP 320 and 512 key lengths
    public static final byte[] KEY_LENGTHS_HEX = {
        // class EC FP [00 - 15]
        (byte)0x00, (byte)0x70,         // [00,01] - 112
        (byte)0x00, (byte)0x80,         // [02,03] - 128
        (byte)0x00, (byte)0xA0,         // [04,05] - 160
        (byte)0x00, (byte)0xC0,         // [06,07] - 192
        (byte)0x00, (byte)0xE0,         // [08,09] - 224
        (byte)0x01, (byte)0x00,         // [10,11] - 256
        (byte)0x01, (byte)0x40,         // [12,13] - 320
        (byte)0x01, (byte)0x80,         // [14,15] - 384
        (byte)0x02, (byte)0x00,         // [16,17] - 512
        (byte)0x02, (byte)0x09,         // [18,19] - 521
        // class EC F2M [16 - 23]
        (byte)0x00, (byte)0x71,         // [20,21] - 113
        (byte)0x00, (byte)0x83,         // [22,23] - 131
        (byte)0x00, (byte)0xA3,         // [24,25] - 163
        (byte)0x00, (byte)0xC1,         // [26,27] - 193
        // classes RSA, RSACRT [24 - 43]
        (byte)0x02, (byte)0x00,         // [28,29] - 512
        (byte)0x02, (byte)0xE0,         // [30,31] - 736
        (byte)0x03, (byte)0x00,         // [32,33] - 768
        (byte)0x03, (byte)0x80,         // [34,35] - 896
        (byte)0x04, (byte)0x00,         // [36,37] - 1024
        (byte)0x05, (byte)0x00,         // [38,39] - 1280
        (byte)0x06, (byte)0x00,         // [40,41] - 1536
        (byte)0x07, (byte)0xC0,         // [42,43] - 1984
        (byte)0x08, (byte)0x00,         // [44,45] - 2048
        (byte)0x10, (byte)0x00,         // [46,47] - 4096
        // class DES [44 - 49]
        (byte)0x00, (byte)0x40,         // [48,49] - 64
        (byte)0x00, (byte)0x80,         // [50,51] - 128
        (byte)0x00, (byte)0xC0,         // [52,53] - 192
        // class AES [50 - 55]
        (byte)0x00, (byte)0x80,         // [54,55] - 128
        (byte)0x00, (byte)0xC0,         // [56,57] - 192
        (byte)0x01, (byte)0x00,         // [58,59] - 256
        // class HMAC [56 - 63]
        (byte)0x00, (byte)0x01,         // [60,61] - 1
        (byte)0x01, (byte)0x00,         // [62,63] - 256
        (byte)0x01, (byte)0x80,         // [64,65] - 384
        (byte)0x02, (byte)0x00,         // [66,67] - 512
    };
*/    
    
    public final static int CLOCKS_PER_SEC = 1000;
    
    public final static byte[] RESET_APDU = {(byte) 0xb0, (byte) 0xe2, (byte) 0x00, (byte) 0x00, (byte) 0x00};
       
    static DirtyLogger m_SystemOutLogger = null;
    public SingleModeTest(DirtyLogger logger) throws Exception {
        m_SystemOutLogger = logger;
        cardManager = new CardMngr(m_SystemOutLogger);       
    }
    
    /**
     * Method containing 'menu'.
     * Calls all other methods in this class.
     * @throws IOException
     * @throws Exception
     */
    public void TestSingleAlg (String[] args, CardTerminal selectedReader) throws IOException, Exception{
        Scanner br = new Scanner(System.in);  
        String answ = "";   // When set to 0, program will ask for each algorithm to test.
                
        m_SystemOutLogger.print("Specify type of your card (e.g., NXP JCOP CJ2A081): ");
        String cardName = br.next();
        m_SystemOutLogger.println(String.format("%s", cardName));
        cardName += br.nextLine();
        if (cardName.isEmpty()) {
            cardName = "noname";
        }            
        FileOutputStream file = cardManager.establishConnection(cardName, cardName + "_ALGSUPPORT_", selectedReader);
    
        // Checking for arguments 
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
                CardMngr.PrintHelp();
            }
        }
        else{       
            long elapsedTimeWholeTest = -System.currentTimeMillis();
            testAllAtOnce(file);
            elapsedTimeWholeTest += System.currentTimeMillis();
            String message = "\n\nTotal test time:; " + elapsedTimeWholeTest / 1000 + " seconds."; 
            m_SystemOutLogger.println(message);
            file.write(message.getBytes());

            CloseFile(file);
        }
        CloseFile(file);
    }
    
    /**
     * Closes file given in parameter.
     * @param file FileOutputStream object to close.
     */
    public static void CloseFile(FileOutputStream file){
        try {
            if (file != null) file.close();
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
        m_SystemOutLogger.println(message);
        file.write(message.getBytes());
    }

    /**
     * Convert error from card to text string
     * @param swStatus
     * @return 
     */
    public static String ErrorToString(int swStatus) {
        // lower byte of exception is value as defined in JCSDK/api_classic/constant-values.htm
        //https://docs.oracle.com/javacard/3.0.5/api/constant-values.html
        
        short sw1 = (short) (swStatus & 0xff00);
        short sw2 = (short) (swStatus & 0x00ff);
        switch (sw1) {
            case JCConsts.SW_Exception_prefix: 
                switch (sw2) { 
                    case JCConsts.SW_Exception:
                        return "Exception";
                    case JCConsts.SW_ArrayIndexOutOfBoundsException:
                        return "ArrayIndexOutOfBoundsException";
                    case JCConsts.SW_ArithmeticException:
                        return "ArithmeticException"; 
                    case JCConsts.SW_ArrayStoreException:
                        return "ArrayStoreException";
                    case JCConsts.SW_NullPointerException:
                        return "NullPointerException";
                    case JCConsts.SW_NegativeArraySizeException:
                        return "NegativeArraySizeException";
                }
            case JCConsts.SW_CryptoException_prefix:
                switch (sw2) {
                    case JCConsts.CryptoException_ILLEGAL_VALUE:
                        return "CryptoException_ILLEGAL_VALUE";
                    case JCConsts.CryptoException_UNINITIALIZED_KEY:
                        return "CryptoException_UNINITIALIZED_KEY";
                    case JCConsts.CryptoException_NO_SUCH_ALGORITHM:
                        return "CryptoException_NO_SUCH_ALGORITHM";
                    case JCConsts.CryptoException_INVALID_INIT:
                        return "CryptoException_INVALID_INIT";
                    case JCConsts.CryptoException_ILLEGAL_USE:
                        return "CryptoException_ILLEGAL_USE";
                    default: return "CryptoException_" + Integer.toHexString(sw2);                
                }
            case JCConsts.SW_SystemException_prefix:
                switch (sw2) {
                    case JCConsts.SystemException_ILLEGAL_VALUE:
                        return "SystemException_ILLEGAL_VALUE";
                    case JCConsts.SystemException_NO_TRANSIENT_SPACE:
                        return "SystemException_NO_TRANSIENT_SPACE";
                    case JCConsts.SystemException_ILLEGAL_TRANSIENT:
                        return "SystemException_ILLEGAL_TRANSIENT";
                    case JCConsts.SystemException_ILLEGAL_AID:
                        return "SystemException_ILLEGAL_AID";
                    case JCConsts.SystemException_NO_RESOURCE:
                        return "SystemException_NO_RESOURCE";
                    case JCConsts.SystemException_ILLEGAL_USE:
                        return "SystemException_ILLEGAL_USE";
                    default:
                        return "SystemException_" + Integer.toHexString(sw2);
                }
            case JCConsts.SW_PINException_prefix:
                switch (sw2) {
                    case JCConsts.PINException_ILLEGAL_VALUE:
                        return "PINException_ILLEGAL_VALUE";
                    case JCConsts.PINException_ILLEGAL_STATE:
                        return "PINException_ILLEGAL_STATE";
                    default:
                        return "PINException_" + Integer.toHexString(sw2);
                }
            case JCConsts.SW_TransactionException_prefix:
                switch (sw2) {
                    case JCConsts.TransactionException_IN_PROGRESS:
                        return "TransactionException_IN_PROGRESS";
                    case JCConsts.TransactionException_NOT_IN_PROGRESS:
                        return "TransactionException_NOT_IN_PROGRESS";
                    case JCConsts.TransactionException_BUFFER_FULL:
                        return "TransactionException_BUFFER_FULL";
                    case JCConsts.TransactionException_INTERNAL_FAILURE:
                        return "TransactionException_INTERNAL_FAILURE";
                    case JCConsts.TransactionException_ILLEGAL_USE:
                        return "TransactionException_ILLEGAL_USE";
                    default:
                        return "TransactionException_" + Integer.toHexString(sw2);
                }
            case JCConsts.SW_CardRuntimeException_prefix:
                return "CardRuntimeException_" + Integer.toHexString(sw2);
                
            default:
                return Integer.toHexString(swStatus);
        }
    }
    
    /**
     * Checks result of algorithm testing on smart card.
     * @param file FileOutputStream object containing output file.
     * @param name String containing algorithm name.
     * @param response Response byte of APDU (second byte of incoming APDU) .
     * @param elapsedCard
     * @throws IOException
     */
    public static void CheckResult (FileOutputStream file, String name, byte[] responseBuffer, long elapsedCard, int swStatus) throws IOException{
        String message = "";
        String elTimeStr = "";
        
        byte response = UNKNOWN_ERROR;
        if (responseBuffer != null) {
            if (responseBuffer.length > 0) {
                m_SystemOutLogger.println("RESPONSE: " + responseBuffer[0]);
            }
            if (responseBuffer.length > 1) {
                response = responseBuffer[1];
            }
        }        
        
        if (swStatus == 0x9000) {
            switch (response){
                case SUPP_ALG_SUPPORTED:
                    // in case negative value is returned as timestamp
                    if (elapsedCard < 0){
                        elTimeStr = "Not executed!";
                        message += name + ";" + "no;" + elTimeStr + "\r\n";
                    }
                    else{
                        elTimeStr = String.format("%1f", (double) elapsedCard / (float) CLOCKS_PER_SEC);
                        message += name + ";" + "yes;" + elTimeStr + "\r\n";
                    }
                    break;

                case NO_SUCH_ALGORITHM:
                    message += name + ";" + "no;" + "\r\n";
                    break;

                case ILLEGAL_USE:
                    message += name + ";" + "error(ILLEGAL_USE);" + "\r\n";
                    break;

                case ILLEGAL_VALUE:
                    message += name + ";" + "error(ILLEGAL_VALUE);" + "\r\n";
                    break;

                case INVALID_INIT:
                    message += name + ";" + "error(INVALID_INIT);" + "\r\n";
                break;

                case UNINITIALIZED_KEY:
                    message += name + ";" + "error(UNINITIALIZED_KEY);" + "\r\n";
                break;
                case UNKNOWN_ERROR: 
                    message += name + ";" + "error(UNKNOWN_ERROR);" + "\r\n";
                    break;
                case 0x6f:
                    message += name + ";" + "maybe;" + "\r\n";
                break;

                default:
                    // OTHER VALUE, IGNORE 
                m_SystemOutLogger.println("Unknown value detected in AlgTest applet (0x" + Integer.toHexString(response & 0xff) + "). Possibly, old version of AlTestJClient is used (try update)");
                break;        
            }
        }
        else {
            message += name + ";" + ErrorToString(swStatus) + ";" + "\r\n";
        }
        m_SystemOutLogger.println(message);
        file.write(message.getBytes());                
    }
        
    /**
     * Tests all algorithms in class 'javacardx.crypto.Cipher' and results writes into the output file.
     * @param file FileOutputStream object containing output file.
     * @throws Exception
     */
    public static void TestClassCipher(FileOutputStream file) throws Exception{
        long       elapsedCard = 0;
        byte[] apdu = new byte[6];
        apdu[OFFSET_CLA] = Consts.CLA_CARD_ALGTEST;  // for AlgTest applet
        apdu[OFFSET_INS] = Consts.INS_CARD_TESTSUPPORTEDMODES_SINGLE;  // for AlgTest applet switch to 'TestSupportedModeSingle'
        apdu[OFFSET_P1] = Consts.CLASS_CIPHER;   // 0x11
        apdu[OFFSET_P2] = (byte)0x00;
        apdu[OFFSET_LC] = (byte)0x01;

        /* Creates message with class name and writes it in the output file and on the screen. */
        String message = "\n" + Utils.GetAlgorithmName(SingleModeTest.CIPHER_STR[0]) + "\r\n";
        m_SystemOutLogger.println(message);
        file.write(message.getBytes());
        
        assert (CIPHER_STR_LAST_INDEX == (CIPHER_STR.length - 1)); // Sanity check as we will construct constant value based on position inside string
        for (int i=1; i< SingleModeTest.CIPHER_STR.length; i++){    // i = 1 because Cipher[0] is class name
            // Reset applet before call
            cardManager.sendAPDU(RESET_APDU);

            // get starting time of communication cycle
            apdu[OFFSET_DATA] = (byte) i;

            elapsedCard = -System.currentTimeMillis();
            ResponseAPDU response = cardManager.sendAPDU(apdu);
            // save time of card response
            elapsedCard += System.currentTimeMillis();
            
            byte[] resp = response.getData();

            // Calls method CheckResult - should add to output error messages.
            CheckResult(file, Utils.GetAlgorithmName(SingleModeTest.CIPHER_STR[i]), resp, elapsedCard, response.getSW());
        }        
    }
    
    /**
     * Tests all algorithms in class 'javacardx.crypto.Signature' and results writes into the output file.
     * @param file FileOutputStream object containing output file.
     * @throws IOException
     * @throws Exception
     */
    public static void TestClassSignature (FileOutputStream file) throws IOException, Exception{
        long       elapsedCard = 0;
        byte[] apdu = new byte[6];
        apdu[OFFSET_CLA] = Consts.CLA_CARD_ALGTEST;  // for AlgTest applet
        apdu[OFFSET_INS] = Consts.INS_CARD_TESTSUPPORTEDMODES_SINGLE;  // for AlgTest applet switch to 'TestSupportedModeSingle'
        apdu[OFFSET_P1] = Consts.CLASS_SIGNATURE;   // 0x12
        apdu[OFFSET_P2] = (byte)0x00;
        apdu[OFFSET_LC] = (byte)0x01;
        
        /* Creates message with class name and writes it in the output file and on the screen. */
        String message = "\n" + Utils.GetAlgorithmName(SingleModeTest.SIGNATURE_STR[0]) + "\r\n";
        m_SystemOutLogger.println(message);
        file.write(message.getBytes());
        
        assert (SIGNATURE_STR_LAST_INDEX == (SIGNATURE_STR.length - 1)); // Sanity check as we will construct constant value based on position inside string
        for (int i=1; i<SingleModeTest.SIGNATURE_STR.length; i++){    // i = 1 because Signature[0] is class name
            // Reset applet before call
            cardManager.sendAPDU(RESET_APDU);
            // get starting time of communication cycle
            apdu[OFFSET_DATA] = (byte) i;
            elapsedCard = -System.currentTimeMillis();
            ResponseAPDU response = cardManager.sendAPDU(apdu);
            // save time of card response
            elapsedCard += System.currentTimeMillis();

            byte[] resp = response.getData();
       
            // Calls method CheckResult - should add to output error messages. 
            CheckResult(file, Utils.GetAlgorithmName(SingleModeTest.SIGNATURE_STR[i]), resp, elapsedCard, response.getSW());
        }        
    }
    
    /**
     * Tests all algorithms in class 'javacardx.crypto.MessageDigest' and results writes into the output file.
     * @param file FileOutputStream object containing output file.
     * @throws IOException
     * @throws Exception
     */
    public static void TestClassMessageDigest (FileOutputStream file) throws IOException, Exception{
        long       elapsedCard = 0;
        byte[] apdu = new byte[6];
        apdu[OFFSET_CLA] = Consts.CLA_CARD_ALGTEST;  // for AlgTest applet
        apdu[OFFSET_INS] = Consts.INS_CARD_TESTSUPPORTEDMODES_SINGLE;  // for AlgTest applet switch to 'TestSupportedModeSingle'
        apdu[OFFSET_P1] = Consts.CLASS_MESSAGEDIGEST;   // 0x15
        apdu[OFFSET_P2] = (byte)0x00;
        apdu[OFFSET_LC] = (byte)0x01;
        
        /* Creates message with class name and writes it in the output file and on the screen. */
        String message = "\n" + Utils.GetAlgorithmName(SingleModeTest.MESSAGEDIGEST_STR[0]) + "\r\n";
        m_SystemOutLogger.println(message);
        file.write(message.getBytes());
        
        assert (MESSAGEDIGEST_STR_LAST_INDEX == (MESSAGEDIGEST_STR.length - 1)); // Sanity check as we will construct constant value based on position inside string
        for (int i=1; i<SingleModeTest.MESSAGEDIGEST_STR.length; i++){    // i = 1 because MessageDigest[0] is class name
            // Reset applet before call
            cardManager.sendAPDU(RESET_APDU);
            // get starting time of communication cycle
            apdu[OFFSET_DATA] = (byte)i;
            elapsedCard = -System.currentTimeMillis();
            ResponseAPDU response = cardManager.sendAPDU(apdu);
            // save time of card response
            elapsedCard += System.currentTimeMillis();
            byte[] resp = response.getData();
            
            // Calls method CheckResult - should add to output error messages. 
            CheckResult(file, Utils.GetAlgorithmName(SingleModeTest.MESSAGEDIGEST_STR[i]), resp, elapsedCard, response.getSW());
        }
    }
    
    /**
     * Tests all algorithms in class 'javacardx.crypto.RandomData' and results writes into the output file.
     * @param file FileOutputStream object containing output file.
     * @throws IOException
     * @throws Exception
     */
    public static void TestClassRandomData(FileOutputStream file) throws IOException, Exception{
        long       elapsedCard = 0;
        byte[] apdu = new byte[6];
        apdu[OFFSET_CLA] = Consts.CLA_CARD_ALGTEST;  // for AlgTest applet
        apdu[OFFSET_INS] = Consts.INS_CARD_TESTSUPPORTEDMODES_SINGLE;  // for AlgTest applet switch to 'TestSupportedModeSingle'
        apdu[OFFSET_P1] = Consts.CLASS_RANDOMDATA;   // 0x16
        apdu[OFFSET_P2] = (byte)0x00;
        apdu[OFFSET_LC] = (byte)0x01;
        
        /* Creates message with class name and writes it in the output file and on the screen. */
        String message = "\r\n" + Utils.GetAlgorithmName(SingleModeTest.RANDOMDATA_STR[0]) + "\r\n";
        m_SystemOutLogger.println(message);
        file.write(message.getBytes());
        
        assert (RANDOMDATA_STR_LAST_INDEX == (SingleModeTest.RANDOMDATA_STR.length - 1)); // Sanity check as we will construct constant value based on position inside string
        for (int i=1; i<SingleModeTest.RANDOMDATA_STR.length; i++){    // i = 1 because RandomData[0] is class name
            // Reset applet before call
            cardManager.sendAPDU(RESET_APDU);
            // get starting time of communication cycle
            apdu[OFFSET_DATA] = (byte)i;
            elapsedCard = -System.currentTimeMillis();
            ResponseAPDU response = cardManager.sendAPDU(apdu);
            // save time of card response
            elapsedCard += System.currentTimeMillis();
            byte[] resp = response.getData();
            // Calls method CheckResult - should add to output error messages. 
            CheckResult(file, Utils.GetAlgorithmName(SingleModeTest.RANDOMDATA_STR[i]), resp, elapsedCard, response.getSW());
        }
    }
    
    /**
     * Tests all algorithms in class 'javacardx.crypto.KeyBuilder' and results writes into the output file.
     * @param file FileOutputStream object containing output file.
     * @throws IOException
     * @throws Exception
     */
    public static void TestClassKeyBuilder (FileOutputStream file) throws IOException, Exception{
        long       elapsedCard = 0;
        byte[] apdu = new byte[8];
        apdu[OFFSET_CLA] = Consts.CLA_CARD_ALGTEST;  // for AlgTest applet
        apdu[OFFSET_INS] = Consts.INS_CARD_TESTSUPPORTEDMODES_SINGLE;  // for AlgTest applet switch to 'TestSupportedModeSingle'
        apdu[OFFSET_P1] = Consts.CLASS_KEYBUILDER;   // 0x20
        apdu[OFFSET_P2] = (byte)0x00;
        apdu[OFFSET_LC] = (byte)0x03;
            
        // Creates message with class name and writes it in the output file and on the screen. 
        String message = "\n" + Utils.GetAlgorithmName(KEYBUILDER_STR[0]) + "\r\n";
        m_SystemOutLogger.println(message);
        file.write(message.getBytes());

        for (int i = 1; i < KEYBUILDER_STR.length; i++){
            // Reset applet before call
            cardManager.sendAPDU(RESET_APDU);
            
            String keyBuilderStr = KEYBUILDER_STR[i];
            KBTestCfg cfg = KEYBUILDER_TEST_CFGS.get(keyBuilderStr);
            if (cfg == null) {
                m_SystemOutLogger.println(keyBuilderStr);
            }
            // byte to choose subclass
            apdu[OFFSET_DATA] = cfg.keyBuilderType;
            // bytes to carry the length of tested key
            CardMngr.setShort(apdu, (short) (OFFSET_DATA + 1), cfg.keyBuilderLength);
            
/* REMOVE 20181201: original approach with direct array access - error prone and inflexible for addition of new constants             
            apdu[OFFSET_DATA] = KEYBUILDER_CONST[i-1];    // (byte)3 => TYPE DES
            // bytes to carry the length of tested key
            apdu[OFFSET_DATA + 1] = KEYBUILDER_LENGTHS[(i*2)-1];
            apdu[OFFSET_DATA + 2] = KEYBUILDER_LENGTHS[(i*2)];
*/
            // get starting time of communication cycle
            elapsedCard = -System.currentTimeMillis();
            ResponseAPDU response = cardManager.sendAPDU(apdu);
            // save time of card response
            elapsedCard += System.currentTimeMillis();
            byte[] resp = response.getData();

            // Calls method CheckResult - should add to output error messages. 
            CheckResult(file, Utils.GetAlgorithmName(keyBuilderStr), resp, elapsedCard, response.getSW());
        }
    }
    
 
    
    /**
     * Tests all algorithms in class 'javacardx.crypto.KeyAgreement' and results writes into the output file.
     * @param file FileOutputStream object containing output file.
     * @throws IOException
     * @throws Exception
     */
    public static void TestClassKeyAgreement (FileOutputStream file) throws IOException, Exception{
        long       elapsedCard = 0;
        byte[] apdu = new byte[6];
        apdu[OFFSET_CLA] = Consts.CLA_CARD_ALGTEST;  // for AlgTest applet
        apdu[OFFSET_INS] = Consts.INS_CARD_TESTSUPPORTEDMODES_SINGLE;  // for AlgTest applet switch to 'TestSupportedModeSingle'
        apdu[OFFSET_P1] = Consts.CLASS_KEYAGREEMENT;   // 0x13
        apdu[OFFSET_P2] = (byte)0x00;
        apdu[OFFSET_LC] = (byte)0x01;
    
        // Creates message with class name and writes it in the output file and on the screen.
        String message = "\n" + Utils.GetAlgorithmName(SingleModeTest.KEYAGREEMENT_STR[0]) + "\r\n";
        m_SystemOutLogger.println(message);
        file.write(message.getBytes());
        
        assert (KEYAGREEMENT_STR_LAST_INDEX == (SingleModeTest.KEYAGREEMENT_STR.length - 1)); // Sanity check as we will construct constant value based on position inside string
        for (int i = 1; i<SingleModeTest.KEYAGREEMENT_STR.length; i++){    // i = 1 because KeyAgreement[0] is class name
            // Reset applet before call
            cardManager.sendAPDU(RESET_APDU);
            apdu[OFFSET_DATA] = (byte) i;
            // get starting time of communication cycle
            elapsedCard = -System.currentTimeMillis();
            ResponseAPDU response = cardManager.sendAPDU(apdu);
            // save time of card response
            elapsedCard += System.currentTimeMillis();
            byte[] resp = response.getData();
            
            // Calls method CheckResult - should add to output error messages. 
            CheckResult(file, Utils.GetAlgorithmName(SingleModeTest.KEYAGREEMENT_STR[i]), resp, elapsedCard, response.getSW());
        }
    }
    
    /**
     * Tests all algorithms in class 'javacardx.crypto.Checksum' and results writes into the output file.
     * @param file FileOutputStream object containing output file.
     * @throws IOException
     * @throws Exception
     */
    public static void TestClassChecksum (FileOutputStream file) throws IOException, Exception{
        long       elapsedCard = 0;
        byte[] apdu = new byte[6];
        apdu[OFFSET_CLA] = Consts.CLA_CARD_ALGTEST;  // for AlgTest applet
        apdu[OFFSET_INS] = Consts.INS_CARD_TESTSUPPORTEDMODES_SINGLE;  // for AlgTest applet switch to 'TestSupportedModeSingle'
        apdu[OFFSET_P1] = Consts.CLASS_CHECKSUM;   // 0x17
        apdu[OFFSET_P2] = (byte)0x00;
        apdu[OFFSET_LC] = (byte)0x01;
    
        // Creates message with class name and writes it in the output file and on the screen. 
        String message = "\n" + Utils.GetAlgorithmName(SingleModeTest.CHECKSUM_STR[0]) + "\r\n";
        m_SystemOutLogger.println(message);
        file.write(message.getBytes());
        
        assert (CHECKSUM_STR_LAST_INDEX == (CHECKSUM_STR.length - 1)); // Sanity check as we will construct constant value based on position inside string
        for (int i=1; i< CHECKSUM_STR.length; i++){    // i = 1 because Checksum[0] is class name
            // Reset applet before call
            cardManager.sendAPDU(RESET_APDU);
            apdu[OFFSET_DATA] = (byte) i;
            // get starting time of communication cycle
            elapsedCard = -System.currentTimeMillis();
            ResponseAPDU response = cardManager.sendAPDU(apdu);
            // save time of card response
            elapsedCard += System.currentTimeMillis();
            byte[] resp = response.getData();
            /* Calls method CheckResult - should add to output error messages. */
            CheckResult(file, Utils.GetAlgorithmName(SingleModeTest.CHECKSUM_STR[i]), resp, elapsedCard, response.getSW());
        }
    }
    
    /**
     * Tests all algorithms in class 'javacard.security.KeyPair_RSA' and results writes into the output file.
     * @param file FileOutputStream object containing output file.
     * @throws IOException
     * @throws Exception
     */
    public static void TestClassKeyPair_ALG_RSA (FileOutputStream file) throws IOException, Exception{
        long       elapsedCard = 0;
        byte[] apdu = new byte[8];
        apdu[OFFSET_CLA] = Consts.CLA_CARD_ALGTEST;      // for AlgTest applet
        apdu[OFFSET_INS] = Consts.INS_CARD_TESTSUPPORTEDMODES_SINGLE;      // for AlgTest applet switch to 'TestSupportedModeSingle'
        apdu[OFFSET_P1] = Consts.CLASS_KEYPAIR;    // 0x19
        apdu[OFFSET_P2] = (byte)0x00;
        apdu[OFFSET_LC] = (byte)0x03;
        apdu[OFFSET_DATA] = ALG_RSA;        // 1
        
        /* Creates message with class name and writes it in the output file and on the screen. */
        String message = "\n" + Utils.GetAlgorithmName(SingleModeTest.KEYPAIR_RSA_STR[0]) + "\r\n";
        m_SystemOutLogger.println(message);
        file.write(message.getBytes());
        
        int counter = 24;
        for (int i=1; i<SingleModeTest.KEYPAIR_RSA_STR.length; i++){    // i = 1 because KeyPair_RSA_STR[0] is class name
            // Reset applet before call
            cardManager.sendAPDU(RESET_APDU);
            apdu[OFFSET_DATA + 1] = KEY_LENGTHS_HEX[counter];
            apdu[OFFSET_DATA + 2] = KEY_LENGTHS_HEX[counter + 1];
            counter = counter + 2;

            // get starting time of communication cycle
            elapsedCard = -System.currentTimeMillis();
            ResponseAPDU response = cardManager.sendAPDU(apdu);
            // save time of card response
            elapsedCard += System.currentTimeMillis();
            byte[] resp = response.getData();
            
            // Calls method CheckResult - should add to output error messages. */
            CheckResult(file, Utils.GetAlgorithmName(SingleModeTest.KEYPAIR_RSA_STR[i]), resp, elapsedCard, response.getSW());
        }
    }
    
    /**
     * Tests all algorithms in class 'javacard.security.KeyPair_RSA_CRT' and results writes into the output file.
     * @param file FileOutputStream object containing output file.
     * @throws IOException
     * @throws Exception
     */
    public static void TestClassKeyPair_ALG_RSA_CRT (FileOutputStream file) throws IOException, Exception{
        long       elapsedCard = 0;
        byte[] apdu = new byte[8];
        apdu[OFFSET_CLA] = Consts.CLA_CARD_ALGTEST;      // for AlgTest applet
        apdu[OFFSET_INS] = Consts.INS_CARD_TESTSUPPORTEDMODES_SINGLE;      // for AlgTest applet switch to 'TestSupportedModeSingle'
        apdu[OFFSET_P1] = Consts.CLASS_KEYPAIR;    // 0x19
        apdu[OFFSET_P2] = (byte)0x00;
        apdu[OFFSET_LC] = (byte)0x03;
        apdu[OFFSET_DATA] = ALG_RSA_CRT;    // 2
        
        /* Creates message with class name and writes it in the output file and on the screen. */
        String message = "\n" + Utils.GetAlgorithmName(SingleModeTest.KEYPAIR_RSACRT_STR[0]) + "\r\n";
        m_SystemOutLogger.println(message);
        file.write(message.getBytes());
        
        int counter = 24;
        for (int i=1; i<SingleModeTest.KEYPAIR_RSACRT_STR.length; i++){    // i = 1 because KeyPair_RSACRT_STR[0] is class name
            // Reset applet before call
            cardManager.sendAPDU(RESET_APDU);            
            apdu[OFFSET_DATA + 1] = KEY_LENGTHS_HEX[counter];
            apdu[OFFSET_DATA + 2] = KEY_LENGTHS_HEX[counter + 1];
            counter = counter + 2;
            
            // get starting time of communication cycle
            elapsedCard = -System.currentTimeMillis();
            ResponseAPDU response = cardManager.sendAPDU(apdu);
            // save time of card response
            elapsedCard += System.currentTimeMillis();
            byte[] resp = response.getData();
            
            // Calls method CheckResult - should add to output error messages.
            CheckResult(file, Utils.GetAlgorithmName(SingleModeTest.KEYPAIR_RSACRT_STR[i]), resp, elapsedCard, response.getSW());
        }
    }
    
    /**
     * Tests all algorithms in class 'javacard.security.KeyPair_DSA' and results writes into the output file.
     * @param file FileOutputStream object containing output file.
     * @throws IOException
     * @throws Exception
     */
    public static void TestClassKeyPair_ALG_DSA (FileOutputStream file) throws IOException, Exception{
        long       elapsedCard = 0;
        byte[] apdu = new byte[8];
        apdu[OFFSET_CLA] = Consts.CLA_CARD_ALGTEST;      // for AlgTest applet
        apdu[OFFSET_INS] = Consts.INS_CARD_TESTSUPPORTEDMODES_SINGLE;      // for AlgTest applet switch to 'TestSupportedModeSingle'
        apdu[OFFSET_P1] = Consts.CLASS_KEYPAIR;    // 0x19
        apdu[OFFSET_P2] = (byte)0x00;
        apdu[OFFSET_LC] = (byte)0x03;
        apdu[OFFSET_DATA] = ALG_DSA;        // 3
        
        /* Creates message with class name and writes it in the output file and on the screen. */
        String message = "\n" + Utils.GetAlgorithmName(SingleModeTest.KEYPAIR_DSA_STR[0]) + "\r\n";
        m_SystemOutLogger.println(message);
        file.write(message.getBytes());
        
        int counter = 24;
        for (int i=1; i<SingleModeTest.KEYPAIR_DSA_STR.length; i++){    // i = 1 because KeyPair_DSA_STR[0] is class name
            // Reset applet before call
            cardManager.sendAPDU(RESET_APDU);
            apdu[OFFSET_DATA + 1] = KEY_LENGTHS_HEX[counter];
            apdu[OFFSET_DATA + 2] = KEY_LENGTHS_HEX[counter + 1];
            counter = counter + 4;
            
            // get starting time of communication cycle
            elapsedCard = -System.currentTimeMillis();
            ResponseAPDU response = cardManager.sendAPDU(apdu);
            // save time of card response
            elapsedCard += System.currentTimeMillis();
            byte[] resp = response.getData();
            
            // Calls method CheckResult - should add to output error messages. 
            CheckResult(file, Utils.GetAlgorithmName(SingleModeTest.KEYPAIR_DSA_STR[i]), resp, elapsedCard, response.getSW());
        }
    }
    
    /**
     * Tests all algorithms in class 'javacard.security.KeyPair_EC_F2M' and results writes into the output file.
     * @param file FileOutputStream object containing output file.
     * @throws IOException
     * @throws Exception
     */
    public static void TestClassKeyPair_ALG_EC_F2M (FileOutputStream file) throws IOException, Exception{
        long       elapsedCard = 0;
        byte[] apdu = new byte[8];
        apdu[OFFSET_CLA] = Consts.CLA_CARD_ALGTEST;      // for AlgTest applet
        apdu[OFFSET_INS] = Consts.INS_CARD_TESTSUPPORTEDMODES_SINGLE;      // for AlgTest applet switch to 'TestSupportedModeSingle'
        apdu[OFFSET_P1] = Consts.CLASS_KEYPAIR;    // 0x19
        apdu[OFFSET_P2] = (byte)0x00;
        apdu[OFFSET_LC] = (byte)0x03;
        apdu[OFFSET_DATA] = ALG_EC_F2M;     // 4
        
        /* Creates message with class name and writes it in the output file and on the screen. */
        String message = "\n" + Utils.GetAlgorithmName(SingleModeTest.KEYPAIR_EC_F2M_STR[0]) + "\r\n";
        m_SystemOutLogger.println(message);
        file.write(message.getBytes());
        
        int counter = 16;
        for (int i=1; i<SingleModeTest.KEYPAIR_EC_F2M_STR.length; i++){    // i = 1 because KeyPair_EC_F2M_STR[0] is class name
            // Reset applet before call
            cardManager.sendAPDU(RESET_APDU);
            apdu[OFFSET_DATA + 1] = KEY_LENGTHS_HEX[counter];
            apdu[OFFSET_DATA + 2] = KEY_LENGTHS_HEX[counter + 1];
            counter = counter + 4;
            
            // get starting time of communication cycle
            elapsedCard = -System.currentTimeMillis();
            ResponseAPDU response = cardManager.sendAPDU(apdu);
            // save time of card response
            elapsedCard += System.currentTimeMillis();
            byte[] resp = response.getData();
            
            // Calls method CheckResult - should add to output error messages.
            CheckResult(file, Utils.GetAlgorithmName(SingleModeTest.KEYPAIR_EC_F2M_STR[i]), resp, elapsedCard, response.getSW());
        }
    }
    
    /**
     * Tests all algorithms in class 'javacard.security.KeyPair_EC_FP' and results writes into the output file.
     * @param file FileOutputStream object containing output file.
     * @throws IOException
     * @throws Exception
     */
    public static void TestClassKeyPair_ALG_EC_FP (FileOutputStream file) throws IOException, Exception{
        long       elapsedCard = 0;
        byte[] apdu = new byte[8];
        apdu[OFFSET_CLA] = Consts.CLA_CARD_ALGTEST;      // for AlgTest applet
        apdu[OFFSET_INS] = Consts.INS_CARD_TESTSUPPORTEDMODES_SINGLE;      // for AlgTest applet switch to 'TestSupportedModeSingle'
        apdu[OFFSET_P1] = Consts.CLASS_KEYPAIR;    // 0x19
        apdu[OFFSET_P2] = (byte)0x00;
        apdu[OFFSET_LC] = (byte)0x03;
        apdu[OFFSET_DATA] = ALG_EC_FP;      // 5
        
        // Creates message with class name and writes it in the output file and on the screen 
        String message = "\n" + Utils.GetAlgorithmName(SingleModeTest.KEYPAIR_EC_FP_STR[0]) + "\r\n";
        m_SystemOutLogger.println(message);
        file.write(message.getBytes());
        
        int counter = 0;
        for (int i=1; i<SingleModeTest.KEYPAIR_EC_FP_STR.length; i++){    // i = 1 because KeyPair_EC_FP_STR[0] is class name
            // Reset applet before call
            cardManager.sendAPDU(RESET_APDU);
            apdu[OFFSET_DATA + 1] = KEY_LENGTHS_HEX[counter];
            apdu[OFFSET_DATA + 2] = KEY_LENGTHS_HEX[counter + 1];
            counter = counter + 2;
            
            // get starting time of communication cycle
            elapsedCard = -System.currentTimeMillis();
            ResponseAPDU response = cardManager.sendAPDU(apdu);
            // save time of card response
            elapsedCard += System.currentTimeMillis();
            byte[] resp = response.getData();
            
            // Calls method CheckResult - should add to output error messages. 
            CheckResult(file, Utils.GetAlgorithmName(SingleModeTest.KEYPAIR_EC_FP_STR[i]), resp, elapsedCard, response.getSW());
        }
    }
    
    /**
     * Tests all algorithms in class 'javacardx.biometry.BioBuilder' and results
     * writes into the output file.
     *
     * @param file FileOutputStream object containing output file.
     * @throws IOException
     * @throws Exception
     */
    public static void TestClassBioBuilder(FileOutputStream file) throws IOException, Exception {
        long elapsedCard = 0;
        byte[] apdu = new byte[6];
        apdu[OFFSET_CLA] = Consts.CLA_CARD_ALGTEST;  // for AlgTest applet
        apdu[OFFSET_INS] = Consts.INS_CARD_TESTSUPPORTEDMODES_SINGLE;  // for AlgTest applet switch to 'TestSupportedModeSingle'
        apdu[OFFSET_P1] = Consts.CLASS_BIOBUILDER;   
        apdu[OFFSET_P2] = (byte) 0x00;
        apdu[OFFSET_LC] = (byte) 0x01;

        // Creates message with class name and writes it in the output file and on the screen.
        String message = "\n" + Utils.GetAlgorithmName(SingleModeTest.BIOBUILDER_STR[0]) + "\r\n";
        m_SystemOutLogger.println(message);
        file.write(message.getBytes());

        for (int i = 1; i < SingleModeTest.BIOBUILDER_STR.length; i++) {    // i = 1 because BIOBUILDER_STR[0] is class name
            // Reset applet before call
            cardManager.sendAPDU(RESET_APDU);
            apdu[OFFSET_DATA] = (byte) i;
            // get starting time of communication cycle
            elapsedCard = -System.currentTimeMillis();
            ResponseAPDU response = cardManager.sendAPDU(apdu);
            // save time of card response
            elapsedCard += System.currentTimeMillis();
            byte[] resp = response.getData();

            // Calls method CheckResult - should add to output error messages. 
            CheckResult(file, Utils.GetAlgorithmName(SingleModeTest.BIOBUILDER_STR[i]), resp, elapsedCard, response.getSW());
        }
    }    
    
    /**
     * Tests all algorithms in class 'javacardx.crypto.AEADCipher' and results
     * writes into the output file.
     *
     * @param file FileOutputStream object containing output file.
     * @throws IOException
     * @throws Exception
     */
    public static void TestClassAEADCipher(FileOutputStream file) throws IOException, Exception {
        long elapsedCard = 0;
        byte[] apdu = new byte[6];
        apdu[OFFSET_CLA] = Consts.CLA_CARD_ALGTEST;  // for AlgTest applet
        apdu[OFFSET_INS] = Consts.INS_CARD_TESTSUPPORTEDMODES_SINGLE;  // for AlgTest applet switch to 'TestSupportedModeSingle'
        apdu[OFFSET_P1] = Consts.CLASS_CIPHER;
        apdu[OFFSET_P2] = (byte) 0x00;
        apdu[OFFSET_LC] = (byte) 0x01;

        // Creates message with class name and writes it in the output file and on the screen.
        String message = "\n" + Utils.GetAlgorithmName(SingleModeTest.AEADCIPHER_STR[0]) + "\r\n";
        m_SystemOutLogger.println(message);
        file.write(message.getBytes());
        // Prepare list of indexes for testing together with algorithm name
        ArrayList<Pair<Integer, String>> algsToTest = new ArrayList<>();
        algsToTest.add(new Pair(new Integer(JCConsts.AEADCipher_CIPHER_AES_CCM), AEADCIPHER_STR[1]));
        algsToTest.add(new Pair(new Integer(JCConsts.AEADCipher_CIPHER_AES_GCM), AEADCIPHER_STR[2]));
 
        for (Pair algToTest : algsToTest) {    
            // Reset applet before call
            cardManager.sendAPDU(RESET_APDU);
            apdu[OFFSET_DATA] = ((Integer) algToTest.getL()).byteValue();
            // get starting time of communication cycle
            elapsedCard = -System.currentTimeMillis();
            ResponseAPDU response = cardManager.sendAPDU(apdu);
            // save time of card response
            elapsedCard += System.currentTimeMillis();
            byte[] resp = response.getData();

            // Calls method CheckResult - should add to output error messages. 
            CheckResult(file, Utils.GetAlgorithmName((String) algToTest.getR()), resp, elapsedCard, response.getSW());
        }
    }    
    
    /**
     * Method that will test all algorithms in SingleModeTest class.
     * @param file FileOutputStream object containing file for output data.
     * @throws Exception
     */
    public static void testAllAtOnce (FileOutputStream file) throws Exception{
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
        //TestClassBioBuilder(file);
        TestClassAEADCipher(file);
        /* Disabled for now, at it seems to be causing crash for J3H081 cards
        // test RSA exponent
        StringBuilder value = new StringBuilder();
        value.setLength(0);
        cardManager.TestVariableRSAPublicExponentSupport(value, file, OFFSET_P2);
        */
    }
    
/*    
    // OBSOLETE, REMOVE 20181201
    // Array of bytes used in KeyBuilder testing.
    public static final byte[] KEYBUILDER_LENGTHS = {
        (byte) 0xFF,
        (byte) 0x00, (byte) 0x40, // [01] 64
        (byte) 0x00, (byte) 0x40, // [02] 64
        (byte) 0x00, (byte) 0x40, // [03] 64
        (byte) 0x00, (byte) 0x80, // [04] 128
        (byte) 0x00, (byte) 0xC0, // [05] 192
        (byte) 0x00, (byte) 0x80, // [06] 128
        (byte) 0x00, (byte) 0x80, // [07] 128
        (byte) 0x00, (byte) 0x80, // [08] 128
        (byte) 0x00, (byte) 0xC0, // [09] 192
        (byte) 0x01, (byte) 0x00, // [10] 256
        (byte) 0x02, (byte) 0x00, // [11] 512
        (byte) 0x02, (byte) 0xE0, // [12] 736
        (byte) 0x03, (byte) 0x00, // [13] 768
        (byte) 0x03, (byte) 0x80, // [14] 896
        (byte) 0x04, (byte) 0x00, // [15] 1024
        (byte) 0x05, (byte) 0x00, // [16] 1280
        (byte) 0x06, (byte) 0x00, // [17] 1536
        (byte) 0x07, (byte) 0xC0, // [18] 1984
        (byte) 0x08, (byte) 0x00, // [19] 2048
        (byte) 0x0C, (byte) 0x00, // [20] 3072
        (byte) 0x10, (byte) 0x00, // [21] 4096
        (byte) 0x02, (byte) 0x00, // [22] 512
        (byte) 0x02, (byte) 0xE0, // [23] 736
        (byte) 0x03, (byte) 0x00, // [24] 768
        (byte) 0x03, (byte) 0x80, // [25] 896
        (byte) 0x04, (byte) 0x00, // [26] 1024
        (byte) 0x05, (byte) 0x00, // [27] 1280
        (byte) 0x06, (byte) 0x00, // [28] 1536
        (byte) 0x07, (byte) 0xC0, // [29] 1984
        (byte) 0x08, (byte) 0x00, // [30] 2048
        (byte) 0x0C, (byte) 0x00, // [31] 3072
        (byte) 0x10, (byte) 0x00, // [32] 4096
        (byte) 0x04, (byte) 0x00, // [33] 1024
        (byte) 0x04, (byte) 0x00, // [34] 1024
        (byte) 0x02, (byte) 0x00, // [35] 512
        (byte) 0x02, (byte) 0xE0, // [36] 736
        (byte) 0x03, (byte) 0x00, // [37] 768
        (byte) 0x03, (byte) 0x80, // [38] 896
        (byte) 0x04, (byte) 0x00, // [39] 1024
        (byte) 0x05, (byte) 0x00, // [40] 1280
        (byte) 0x06, (byte) 0x00, // [41] 1536
        (byte) 0x07, (byte) 0xC0, // [42] 1984
        (byte) 0x08, (byte) 0x00, // [43] 2048
        (byte) 0x0C, (byte) 0x00, // [44] 3072
        (byte) 0x10, (byte) 0x00, // [45] 4096
        (byte) 0x04, (byte) 0x00, // [46] 1024
        (byte) 0x04, (byte) 0x00, // [47] 1024
        (byte) 0x02, (byte) 0x00, // [48] 512
        (byte) 0x03, (byte) 0x00, // [49] 768
        (byte) 0x04, (byte) 0x00, // [50] 1024
        (byte) 0x04, (byte) 0x00, // [51] 1024
        (byte) 0x04, (byte) 0x00, // [52] 1024
        (byte) 0x02, (byte) 0x00, // [53] 512
        (byte) 0x03, (byte) 0x00, // [54] 768
        (byte) 0x04, (byte) 0x00, // [55] 1024
        (byte) 0x00, (byte) 0x71, // [56] 113
        (byte) 0x00, (byte) 0x83, // [57] 131
        (byte) 0x00, (byte) 0xA3, // [58] 163
        (byte) 0x00, (byte) 0xC1, // [59] 193
        (byte) 0x00, (byte) 0xC1, // [60] 193
        (byte) 0x00, (byte) 0xC1, // [61] 193
        (byte) 0x00, (byte) 0x70, // [62] 112
        (byte) 0x00, (byte) 0x80, // [63] 128
        (byte) 0x00, (byte) 0xA0, // [64] 160
        (byte) 0x00, (byte) 0xC0, // [65] 192
        (byte) 0x00, (byte) 0xE0, // [66] 224
        (byte) 0x01, (byte) 0x00, // [67] 256
        (byte) 0x01, (byte) 0x80, // [68] 384
        (byte) 0x02, (byte) 0x09, // [69] 521
        (byte) 0x00, (byte) 0x80, // [70] 128
        (byte) 0x00, (byte) 0x80, // [71] 128
        (byte) 0x00, (byte) 0x80, // [72] 128
        (byte) 0x00, (byte) 0x80, // [73] 128
        (byte) 0x00, (byte) 0x80, // [74] 128
        (byte) 0x00, (byte) 0x40, // [75] 64
        (byte) 0x00, (byte) 0x40, // [76] 64
        (byte) 0x00, (byte) 0x40, // [77] 64
        (byte) 0x00, (byte) 0x40, // [78] 64
        (byte) 0x00, (byte) 0x80, // [79] 128
        (byte) 0x00, (byte) 0x80 // [80] 128
    };

    public static final byte[] KEYBUILDER_CONST = {
        (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x03, (byte) 0x03, (byte) 0x0D, (byte) 0x0E, (byte) 0x0F, (byte) 0x0F, (byte) 0x0F,
        (byte) 0x04, (byte) 0x04, (byte) 0x04, (byte) 0x04, (byte) 0x04, (byte) 0x04, (byte) 0x04, (byte) 0x04, (byte) 0x04, (byte) 0x04,
        (byte) 0x04, (byte) 0x05, (byte) 0x05, (byte) 0x05, (byte) 0x05, (byte) 0x05, (byte) 0x05, (byte) 0x05, (byte) 0x05, (byte) 0x05,
        (byte) 0x05, (byte) 0x05, (byte) 0x16, (byte) 0x17, (byte) 0x06, (byte) 0x06, (byte) 0x06, (byte) 0x06, (byte) 0x06, (byte) 0x06,
        (byte) 0x06, (byte) 0x06, (byte) 0x06, (byte) 0x06, (byte) 0x06, (byte) 0x18, (byte) 0x19, (byte) 0x08, (byte) 0x08, (byte) 0x08,
        (byte) 0x1A, (byte) 0x1B, (byte) 0x07, (byte) 0x07, (byte) 0x07, (byte) 0x0A, (byte) 0x0A, (byte) 0x0A, (byte) 0x0A, (byte) 0x1C,
        (byte) 0x1D, (byte) 0x0C, (byte) 0x0C, (byte) 0x0C, (byte) 0x0C, (byte) 0x0C, (byte) 0x0C, (byte) 0x0C, (byte) 0x0C, (byte) 0x1E,
        (byte) 0x1F, (byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14, (byte) 0x15, (byte) 0x15, (byte) 0x15, (byte) 0x15
    };
    
    // Create human-readable list of testing tuples for class
    public static void TestClassKeyBuilder_DumpNames() throws IOException, Exception {
        ArrayList<Pair<String, Integer>> keyBuilderList = new ArrayList<>();
        keyBuilderList.add(new Pair("bogus", 0));
        keyBuilderList.add(new Pair("JCConsts.KeyBuilder_TYPE_DES_TRANSIENT_RESET", 1));
        keyBuilderList.add(new Pair("JCConsts.KeyBuilder_TYPE_DES_TRANSIENT_DESELECT", 2));
        keyBuilderList.add(new Pair("JCConsts.KeyBuilder_TYPE_DES", 3));
        keyBuilderList.add(new Pair("JCConsts.KeyBuilder_TYPE_RSA_PUBLIC", 4));
        keyBuilderList.add(new Pair("JCConsts.KeyBuilder_TYPE_RSA_PRIVATE", 5));
        keyBuilderList.add(new Pair("JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE", 6));
        keyBuilderList.add(new Pair("JCConsts.KeyBuilder_TYPE_DSA_PUBLIC", 7));
        keyBuilderList.add(new Pair("JCConsts.KeyBuilder_TYPE_DSA_PRIVATE", 8));
        keyBuilderList.add(new Pair("JCConsts.KeyBuilder_TYPE_EC_F2M_PUBLIC", 9));
        keyBuilderList.add(new Pair("JCConsts.KeyBuilder_TYPE_EC_F2M_PRIVATE", 10));
        keyBuilderList.add(new Pair("JCConsts.KeyBuilder_TYPE_EC_FP_PUBLIC", 11));
        keyBuilderList.add(new Pair("JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE", 12));
        keyBuilderList.add(new Pair("JCConsts.KeyBuilder_TYPE_AES_TRANSIENT_RESET", 13));
        keyBuilderList.add(new Pair("JCConsts.KeyBuilder_TYPE_AES_TRANSIENT_DESELECT", 14));
        keyBuilderList.add(new Pair("JCConsts.KeyBuilder_TYPE_AES", 15));
        keyBuilderList.add(new Pair("JCConsts.KeyBuilder_TYPE_KOREAN_SEED_TRANSIENT_RESET", 16));
        keyBuilderList.add(new Pair("JCConsts.KeyBuilder_TYPE_KOREAN_SEED_TRANSIENT_DESELECT", 17));
        keyBuilderList.add(new Pair("JCConsts.KeyBuilder_TYPE_KOREAN_SEED", 18));
        keyBuilderList.add(new Pair("JCConsts.KeyBuilder_TYPE_HMAC_TRANSIENT_RESET", 19));
        keyBuilderList.add(new Pair("JCConsts.KeyBuilder_TYPE_HMAC_TRANSIENT_DESELECT", 20));
        keyBuilderList.add(new Pair("JCConsts.KeyBuilder_TYPE_HMAC", 21));
        keyBuilderList.add(new Pair("JCConsts.KeyBuilder_TYPE_RSA_PRIVATE_TRANSIENT_RESET", 22));
        keyBuilderList.add(new Pair("JCConsts.KeyBuilder_TYPE_RSA_PRIVATE_TRANSIENT_DESELECT", 23));
        keyBuilderList.add(new Pair("JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE_TRANSIENT_RESET", 24));
        keyBuilderList.add(new Pair("JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE_TRANSIENT_DESELECT", 25));
        keyBuilderList.add(new Pair("JCConsts.KeyBuilder_TYPE_DSA_PRIVATE_TRANSIENT_RESET", 26));
        keyBuilderList.add(new Pair("JCConsts.KeyBuilder_TYPE_DSA_PRIVATE_TRANSIENT_DESELECT", 27));
        keyBuilderList.add(new Pair("JCConsts.KeyBuilder_TYPE_EC_F2M_PRIVATE_TRANSIENT_RESET", 28));
        keyBuilderList.add(new Pair("JCConsts.KeyBuilder_TYPE_EC_F2M_PRIVATE_TRANSIENT_DESELECT", 29));
        keyBuilderList.add(new Pair("JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE_TRANSIENT_RESET", 30));
        keyBuilderList.add(new Pair("JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE_TRANSIENT_DESELECT", 31));

        // Creates message with class name and writes it in the output file and on the screen. 
        String message = "\n" + Utils.GetAlgorithmName(KEYBUILDER_STR[0]) + "\r\n";
        m_SystemOutLogger.println(message);

        HashMap<String, KBTestCfg> KEYBUILDER_TEST_CFGS = new HashMap<>();
        m_SystemOutLogger.println("public static final Map<String, KBTestCfg> KEYBUILDER_TEST_CFGS;");
        m_SystemOutLogger.println("static {");
        m_SystemOutLogger.println("    KEYBUILDER_TEST_CFGS = new HashMap<>();");
        for (int i = 1; i < KEYBUILDER_STR.length; i++) {
            short keyLength = KEYBUILDER_LENGTHS[(i * 2) - 1];
            keyLength *= 256;
            keyLength += KEYBUILDER_LENGTHS[(i * 2)] & 0xff;

            String keyTypeStr = keyBuilderList.get(KEYBUILDER_CONST[i - 1]).getL();

            KEYBUILDER_TEST_CFGS.put(KEYBUILDER_STR[i], new KBTestCfg(KEYBUILDER_CONST[i - 1], keyLength)); // KEYBUILDER_STR[i], KEYBUILDER_CONST[i-1], KEYBUILDER_LENGTHS[(i*2)-1], KEYBUILDER_LENGTHS[(i*2)]
            message = String.format("    KEYBUILDER_TEST_CFGS.put(\"%s\", new KBTestCfg(%s, (short) %d));", KEYBUILDER_STR[i], keyTypeStr, keyLength);
            m_SystemOutLogger.println(message);
        }
        m_SystemOutLogger.println("}\r\n");
    }    
*/    
    
    
    
    
    
    
}   // END OF CLASS 'SINGLEMODETEST'


