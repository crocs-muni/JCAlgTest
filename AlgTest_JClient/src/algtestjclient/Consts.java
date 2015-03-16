/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package algtestjclient;

/**
 * Copyright 2012-2014, SmartArchitects
 * @author Petr Svenda <petr@svenda.com>
 */
public class Consts {
    final static byte CLA_CARD_ALGTEST                  = (byte) 0xB0;
    final static byte INS_CARD_GETVERSION               = (byte) 0x60;
    final static byte INS_CARD_RESET                    = (byte) 0x69;
    
    final static byte INS_CARD_TESTSUPPORTEDMODES       = (byte) 0x70;
    final static byte INS_CARD_TESTAVAILABLE_MEMORY     = (byte) 0x71;
    final static byte INS_CARD_TESTRSAEXPONENTSET       = (byte) 0x72;
    final static byte INS_CARD_JCSYSTEM_INFO            = (byte) 0x73;
    final static byte INS_CARD_TESTEXTAPDU              = (byte) 0x74;
    final static byte INS_CARD_TESTSUPPORTEDMODES_SINGLE= (byte) 0x75;    
    
// BUGBUG: select only used instructions, set proper codes    
    final static byte INS_TEST_MESSAGE_DIGEST =   (byte) 0x70;
    final static byte INS_TEST_RANDOM_DATA =      (byte) 0x71;
    final static byte INS_TEST_CIPHER =           (byte) 0x72;
    final static byte INS_TEST_KEY_BUILDER =      (byte) 0x73;
    final static byte INS_TEST_KEY_PAIR =         (byte) 0x74;
    final static byte INS_TEST_CHECKSUM =         (byte) 0x75;
    final static byte INS_RESET =                 (byte) 0x69;
    final static byte INS_PREPARE_KEY =           (byte) 0x40;
    final static byte INS_PREPARE_SIGNATURE =     (byte) 0x42;
    final static byte INS_PREPARE_KEY_PAIR =      (byte) 0x44;
    final static byte INS_PREPARE_MESSAGE_DIGEST = (byte) 0x46;
    final static byte INS_PREPARE_RANDOM_DATA =   (byte) 0x48;
    final static byte INS_TEST_SIGNATURE =        (byte) 0x76;
// end bugbug    
    final static byte INS_CARD_PERF_TEST_CLASS_KEY      = (byte) 0x40;    
    
    public static final byte MY_DSA = 88;       // bugbug: introduced probably because of value duplicity inside prepareSignature()
    
    
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
    
    public static final int LENGTH_DES            = 64;
    public static final int LENGTH_DES3_2KEY      = 128;
    public static final int LENGTH_DES3_3KEY      = 192;
    public static final int LENGTH_RSA_512        = 512;
    public static final int LENGTH_RSA_736        = 736;
    public static final int LENGTH_RSA_768        = 768;
    public static final int LENGTH_RSA_896        = 896;
    public static final int LENGTH_RSA_1024       = 1024;
    public static final int LENGTH_RSA_1280       = 1280;
    public static final int LENGTH_RSA_1536       = 1536;
    public static final int LENGTH_RSA_1984       = 1984;
    public static final int LENGTH_RSA_2048       = 2048;
    public static final int LENGTH_RSA_3072       = 3072;
    public static final int LENGTH_RSA_4096       = 4096;
    public static final int LENGTH_DSA_512        = 512;
    public static final int LENGTH_DSA_768        = 768;
    public static final int LENGTH_DSA_1024       = 1024;
    public static final int LENGTH_EC_FP_112      = 112;
    public static final int LENGTH_EC_F2M_113     = 113;
    public static final int LENGTH_EC_FP_128      = 128;
    public static final int LENGTH_EC_F2M_131     = 131;
    public static final int LENGTH_EC_FP_160      = 160;
    public static final int LENGTH_EC_F2M_163     = 163;
    public static final int LENGTH_EC_FP_192      = 192;
    public static final int LENGTH_EC_F2M_193     = 193;
    public static final int LENGTH_EC_FP_224      = 224;  
    public static final int LENGTH_EC_FP_256      = 256;  
    public static final int LENGTH_EC_FP_384      = 384;  
    public static final int LENGTH_EC_FP_521      = 521;    
    public static final int LENGTH_AES_128        = 128;
    public static final int LENGTH_AES_192        = 192;
    public static final int LENGTH_AES_256        = 256;
      // JC2.2.2
    public static final int LENGTH_KOREAN_SEED_128        = 128;
    public static final int LENGTH_HMAC_SHA_1_BLOCK_64    = 64;
    public static final int LENGTH_HMAC_SHA_256_BLOCK_64  = 64;
    public static final int LENGTH_HMAC_SHA_384_BLOCK_64  = 128;
    public static final int LENGTH_HMAC_SHA_512_BLOCK_64  = 128;
    
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

}
