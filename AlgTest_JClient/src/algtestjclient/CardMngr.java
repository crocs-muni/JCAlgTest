/*  
    Copyright (c) 2008-2012 Petr Svenda <petr@svenda.com>

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

import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;
import javax.smartcardio.*;

/**
 *
 * @author petrs
 */
public class CardMngr {

    public static final int MAX_SUPP_ALG          = 240;    
    public static final byte SUPP_ALG_UNTOUCHED    = 5;
    public static final short SUPP_ALG_SEPARATOR    = 0xff;

    public static final byte ALGTEST_AID_LEN       = 9;

    public static final byte CLASS_CIPHER          = 0x11;
    public static final byte CLASS_SIGNATURE       = 0x12;
    public static final byte CLASS_KEYAGREEMENT    = 0x13;
    public static final byte CLASS_MESSAGEDIGEST   = 0x15;
    public static final byte CLASS_RANDOMDATA      = 0x16;
    public static final byte CLASS_CHECKSUM        = 0x17;
    public static final byte CLASS_KEYPAIR_RSA     = 0x18;
    public static final byte CLASS_KEYPAIR_RSA_CRT = 0x19;
    public static final byte CLASS_KEYPAIR_DSA     = 0x1A;
    public static final byte CLASS_KEYPAIR_EC_F2M  = 0x1B;
    public static final byte CLASS_KEYPAIR_EC_FP   = 0x1C;

    public static final byte CLASS_KEYBUILDER      = 0x20;


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
    
    public static final String SIGNATURE_STR[] = {"javacard.crypto.Signature", "ALG_DES_MAC4_NOPAD", "ALG_DES_MAC8_NOPAD", 
        "ALG_DES_MAC4_ISO9797_M1", "ALG_DES_MAC8_ISO9797_M1", "ALG_DES_MAC4_ISO9797_M2", "ALG_DES_MAC8_ISO9797_M2", 
        "ALG_DES_MAC4_PKCS5", "ALG_DES_MAC8_PKCS5", "ALG_RSA_SHA_ISO9796", "ALG_RSA_SHA_PKCS1", "ALG_RSA_MD5_PKCS1", 
        "ALG_RSA_RIPEMD160_ISO9796", "ALG_RSA_RIPEMD160_PKCS1", "ALG_DSA_SHA", "ALG_RSA_SHA_RFC2409", 
        "ALG_RSA_MD5_RFC2409", "ALG_ECDSA_SHA", "ALG_AES_MAC_128_NOPAD", "ALG_DES_MAC4_ISO9797_1_M2_ALG3", 
        "ALG_DES_MAC8_ISO9797_1_M2_ALG3", "ALG_RSA_SHA_PKCS1_PSS", "ALG_RSA_MD5_PKCS1_PSS", "ALG_RSA_RIPEMD160_PKCS1_PSS", 
        "ALG_HMAC_SHA1", "ALG_HMAC_SHA_256", "ALG_HMAC_SHA_384", "ALG_HMAC_SHA_512", "ALG_HMAC_MD5", "ALG_HMAC_RIPEMD160", 
        "ALG_RSA_SHA_ISO9796_MR", "ALG_RSA_RIPEMD160_ISO9796_MR", "ALG_SEED_MAC_NOPAD", "ALG_ECDSA_SHA_256", 
        "ALG_ECDSA_SHA_384", "ALG_AES_MAC_192_NOPAD", "ALG_AES_MAC_256_NOPAD", "ALG_ECDSA_SHA_224", "ALG_ECDSA_SHA_512", 
        "ALG_RSA_SHA_224_PKCS1", "ALG_RSA_SHA_256_PKCS1", "ALG_RSA_SHA_384_PKCS1", "ALG_RSA_SHA_512_PKCS1", 
        "ALG_RSA_SHA_224_PKCS1_PSS", "ALG_RSA_SHA_256_PKCS1_PSS", "ALG_RSA_SHA_384_PKCS1_PSS", "ALG_RSA_SHA_512_PKCS1_PSS",
        "ALG_DES_MAC4_ISO9797_1_M1_ALG3", "ALG_DES_MAC8_ISO9797_1_M1_ALG3"
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

    public static final String CIPHER_STR[] = {"javacardx.crypto.Cipher", "ALG_DES_CBC_NOPAD", "ALG_DES_CBC_ISO9797_M1", "ALG_DES_CBC_ISO9797_M2", "ALG_DES_CBC_PKCS5", 
        "ALG_DES_ECB_NOPAD", "ALG_DES_ECB_ISO9797_M1", "ALG_DES_ECB_ISO9797_M2", "ALG_DES_ECB_PKCS5",
        "ALG_RSA_ISO14888", "ALG_RSA_PKCS1", "ALG_RSA_ISO9796", "ALG_RSA_NOPAD", "ALG_AES_BLOCK_128_CBC_NOPAD", 
        "ALG_AES_BLOCK_128_ECB_NOPAD", "ALG_RSA_PKCS1_OAEP", "ALG_KOREAN_SEED_ECB_NOPAD", "ALG_KOREAN_SEED_CBC_NOPAD",
        "ALG_AES_BLOCK_192_CBC_NOPAD", "ALG_AES_BLOCK_192_ECB_NOPAD", "ALG_AES_BLOCK_256_CBC_NOPAD", "ALG_AES_BLOCK_256_ECB_NOPAD", 
        "ALG_AES_CBC_ISO9797_M1", "ALG_AES_CBC_ISO9797_M2", "ALG_AES_CBC_PKCS5", "ALG_AES_ECB_ISO9797_M1", "ALG_AES_ECB_ISO9797_M2", "ALG_AES_ECB_PKCS5"         
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
    
    public static final String KEYAGREEMENT_STR[] = {"javacard.security.KeyAgreement", "ALG_EC_SVDP_DH", "ALG_EC_SVDP_DHC",
        "ALG_EC_SVDP_DH_KDF", "ALG_EC_SVDP_DH_PLAIN", "ALG_EC_SVDP_DHC_KDF", "ALG_EC_SVDP_DHC_PLAIN"
    }; 

      //
      //Class javacard.security.KeyBuilder
      //
    public static final byte TYPE_DES_TRANSIENT_RESET              = 2;
    public static final byte TYPE_DES_TRANSIENT_DESELECT           = 3;
    public static final byte TYPE_DES                              = 4;
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
    private final int LENGTH_AES_128        = 128;
    private final int LENGTH_AES_192        = 192;
    private final int LENGTH_AES_256        = 256;
      // JC2.2.2
    private final int LENGTH_KOREAN_SEED_128        = 128;
    private final int LENGTH_HMAC_SHA_1_BLOCK_64    = 64;
    private final int LENGTH_HMAC_SHA_256_BLOCK_64  = 64;
    private final int LENGTH_HMAC_SHA_384_BLOCK_64  = 128;
    private final int LENGTH_HMAC_SHA_512_BLOCK_64  = 128;

    public static final String KEYBUILDER_STR[] = {"javacard.security.KeyBuilder", 
        "###DES_KEY###", "TYPE_DES_TRANSIENT_RESET", "TYPE_DES_TRANSIENT_DESELECT", "TYPE_DES LENGTH_DES", "TYPE_DES LENGTH_DES3_2KEY", "TYPE_DES LENGTH_DES3_3KEY",
        "###AES_KEY###", "TYPE_AES_TRANSIENT_RESET", "TYPE_AES_TRANSIENT_DESELECT", "TYPE_AES LENGTH_AES_128", "TYPE_AES LENGTH_AES_192", "TYPE_AES LENGTH_AES_256",
        "###RSA_PUBLIC_KEY###", "TYPE_RSA_PUBLIC LENGTH_RSA_512", "TYPE_RSA_PUBLIC LENGTH_RSA_736", "TYPE_RSA_PUBLIC LENGTH_RSA_768", "TYPE_RSA_PUBLIC LENGTH_RSA_896",
            "TYPE_RSA_PUBLIC LENGTH_RSA_1024", "TYPE_RSA_PUBLIC LENGTH_RSA_1280", "TYPE_RSA_PUBLIC LENGTH_RSA_1536", "TYPE_RSA_PUBLIC LENGTH_RSA_1984", "TYPE_RSA_PUBLIC LENGTH_RSA_2048", "TYPE_RSA_PUBLIC LENGTH_RSA_3072", "TYPE_RSA_PUBLIC LENGTH_RSA_4096",
        "###RSA_PRIVATE_KEY###", "TYPE_RSA_PRIVATE LENGTH_RSA_512", "TYPE_RSA_PRIVATE LENGTH_RSA_736", "TYPE_RSA_PRIVATE LENGTH_RSA_768", "TYPE_RSA_PRIVATE LENGTH_RSA_896",
            "TYPE_RSA_PRIVATE LENGTH_RSA_1024", "TYPE_RSA_PRIVATE LENGTH_RSA_1280", "TYPE_RSA_PRIVATE LENGTH_RSA_1536", "TYPE_RSA_PRIVATE LENGTH_RSA_1984", "TYPE_RSA_PRIVATE LENGTH_RSA_2048", "TYPE_RSA_PRIVATE LENGTH_RSA_3072", "TYPE_RSA_PRIVATE LENGTH_RSA_4096",
        "###RSA_CRT_PRIVATE_KEY###", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_512", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_736", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_768", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_896",
            "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1024", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1280", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1536", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1984", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_2048", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_3072", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_4096",
        "###DSA_PRIVATE_KEY###", "TYPE_DSA_PRIVATE LENGTH_DSA_512", "TYPE_DSA_PRIVATE LENGTH_DSA_768", "TYPE_DSA_PRIVATE LENGTH_DSA_1024", 
        "###DSA_PUBLIC_KEY###", "TYPE_DSA_PUBLIC LENGTH_DSA_512", "TYPE_DSA_PUBLIC LENGTH_DSA_768", "TYPE_DSA_PUBLIC LENGTH_DSA_1024", 
        "###EC_F2M_PRIVATE_KEY###", "TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_113", "TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_131", "TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_163", "TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_193",
        "###EC_FP_PRIVATE_KEY###", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_112", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_128", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_160", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_192",
        "###KOREAN_SEED_KEY###", "TYPE_KOREAN_SEED_TRANSIENT_RESET", "TYPE_KOREAN_SEED_TRANSIENT_DESELECT", "TYPE_KOREAN_SEED LENGTH_KOREAN_SEED_128", 
        "###HMAC_KEY###", "TYPE_HMAC_TRANSIENT_RESET", "TYPE_HMAC_TRANSIENT_DESELECT", "TYPE_HMAC LENGTH_HMAC_SHA_1_BLOCK_64", "TYPE_HMAC LENGTH_HMAC_SHA_256_BLOCK_64", "TYPE_HMAC LENGTH_HMAC_SHA_384_BLOCK_64", "TYPE_HMAC LENGTH_HMAC_SHA_512_BLOCK_64",
    }; 

      //
      //Class javacard.security.KeyPair
      //
    public static final byte ALG_RSA                       = 1;
    public static final byte ALG_RSA_CRT                   = 2;
    public static final byte ALG_DSA                       = 3;
    public static final byte ALG_EC_F2M                    = 4;
    public static final byte ALG_EC_FP                     = 5;

    public static final String KEYPAIR_RSA_STR[] = {"javacard.security.KeyPair ALG_RSA on-card generation", 
        "ALG_RSA LENGTH_RSA_512", "ALG_RSA LENGTH_RSA_736", "ALG_RSA LENGTH_RSA_768", "ALG_RSA LENGTH_RSA_896",
        "ALG_RSA LENGTH_RSA_1024", "ALG_RSA LENGTH_RSA_1280", "ALG_RSA LENGTH_RSA_1536", "ALG_RSA LENGTH_RSA_1984", "ALG_RSA LENGTH_RSA_2048", "ALG_RSA LENGTH_RSA_3072", "ALG_RSA LENGTH_RSA_4096"
        };

    public static final String KEYPAIR_RSACRT_STR[] = {"javacard.security.KeyPair ALG_RSA_CRT on-card generation", 
        "ALG_RSA_CRT LENGTH_RSA_512", "ALG_RSA_CRT LENGTH_RSA_736", "ALG_RSA_CRT LENGTH_RSA_768", "ALG_RSA_CRT LENGTH_RSA_896",
        "ALG_RSA_CRT LENGTH_RSA_1024", "ALG_RSA_CRT LENGTH_RSA_1280", "ALG_RSA_CRT LENGTH_RSA_1536", "ALG_RSA_CRT LENGTH_RSA_1984", "ALG_RSA_CRT LENGTH_RSA_2048", 
        "ALG_RSA_CRT LENGTH_RSA_3072", "ALG_RSA_CRT LENGTH_RSA_4096"
    };    
    public static final String KEYPAIR_DSA_STR[] = {"javacard.security.KeyPair ALG_DSA on-card generation", 
        "ALG_DSA LENGTH_DSA_512", "ALG_DSA LENGTH_DSA_768", "ALG_DSA LENGTH_DSA_1024"
    };
    public static final String KEYPAIR_EC_F2M_STR[] = {"javacard.security.KeyPair ALG_EC_F2M on-card generation", 
        "ALG_EC_F2M LENGTH_EC_F2M_113", "ALG_EC_F2M LENGTH_EC_F2M_131", "ALG_EC_F2M LENGTH_EC_F2M_163", "ALG_EC_F2M LENGTH_EC_F2M_193"
    };
    public static final String KEYPAIR_EC_FP_STR[] = {"javacard.security.KeyPair ALG_EC_FP on-card generation", 
        "ALG_EC_FP LENGTH_EC_FP_112", "ALG_EC_FP LENGTH_EC_FP_128", "ALG_EC_FP LENGTH_EC_FP_160", "ALG_EC_FP LENGTH_EC_FP_192"
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
    
    public static final String MESSAGEDIGEST_STR[] = {"javacard.security.MessageDigest", "ALG_SHA", "ALG_MD5", "ALG_RIPEMD160", 
        "ALG_SHA_256", "ALG_SHA_384", "ALG_SHA_512", "ALG_SHA_224"
    }; 


      //Class javacard.security.RandomData
    public static final byte ALG_PSEUDO_RANDOM             = 1;
    public static final byte ALG_SECURE_RANDOM             = 2;

    public static final String RANDOMDATA_STR[] = {"javacard.security.RandomData", "ALG_PSEUDO_RANDOM", "ALG_SECURE_RANDOM"}; 

      // Class javacard.security.Checksum
    public static final byte ALG_ISO3309_CRC16             = 1;
    public static final byte ALG_ISO3309_CRC32             = 2;

    public static final String CHECKSUM_STR[] = {"javacard.security.Checksum", "ALG_ISO3309_CRC16", "ALG_ISO3309_CRC32"}; 
    
    public static final String JCSYSTEM_STR[] = {"javacard.framework.JCSystem", "JCSystem.getVersion()[Major.Minor]", 
        "JCSystem.isObjectDeletionSupported", "JCSystem.MEMORY_TYPE_PERSISTENT", "JCSystem.MEMORY_TYPE_TRANSIENT_RESET", 
        "JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT"}; 

    public static final String RAWRSA_1024_STR[] = {"Variable RSA 1024 - support for variable public exponent. If supported, user-defined fast modular exponentiation can be executed on the smart card via cryptographic coprocessor. This is very specific feature and you will probably not need it", 
        "Allocate RSA 1024 objects", "Set random modulus", "Set random public exponent", "Initialize cipher with public key with random exponent", "Use random public exponent"}; 

    public static final String EXTENDEDAPDU_STR[] = {"javacardx.apdu.ExtendedLength", "Extended APDU"}; 

    public static final String BASIC_INFO[] = {"Basic info", "JavaCard support version"}; 
   
    public static final String[] ALL_CLASSES_STR[] = {
        BASIC_INFO, JCSYSTEM_STR, EXTENDEDAPDU_STR, CIPHER_STR, SIGNATURE_STR, MESSAGEDIGEST_STR, RANDOMDATA_STR, KEYBUILDER_STR, 
        KEYPAIR_RSA_STR, KEYPAIR_RSACRT_STR, KEYPAIR_DSA_STR, KEYPAIR_EC_F2M_STR, 
        KEYPAIR_EC_FP_STR, KEYAGREEMENT_STR, CHECKSUM_STR, RAWRSA_1024_STR
    };
    
    
    
    CardTerminal m_terminal = null;
    CardChannel m_channel = null;
    Card m_card = null;
    
    public static final byte selectApplet[] = {
        (byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x00, (byte) 0x09, 
        (byte) 0x6D, (byte) 0x79, (byte) 0x70, (byte) 0x61, (byte) 0x63, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x31}; 

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

    public final int CLOCKS_PER_SEC = 1000;
    
    public boolean ConnectToCard(StringBuilder selectedReader, StringBuilder selectedATR, StringBuilder usedProtocol) throws Exception {
        // TRY ALL READERS, FIND FIRST SELECTABLE
        List terminalList = GetReaderList();

        if (terminalList.isEmpty()) { System.out.println("No terminals found"); }

        //List numbers of Card readers
        boolean cardFound = false;
        for (int i = 0; i < terminalList.size(); i++) {
            System.out.println(i + " : " + terminalList.get(i));
            m_terminal = (CardTerminal) terminalList.get(i);
            if (m_terminal.isCardPresent()) {
                m_card = m_terminal.connect("*");
                System.out.println("card: " + m_card);
                m_channel = m_card.getBasicChannel();

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
        } catch (Exception ex) {
            System.out.println("Exception : " + ex);
            return null;
        }
    }

    private ResponseAPDU sendAPDU(byte apdu[]) throws Exception {
        CommandAPDU commandAPDU = new CommandAPDU(apdu);

        System.out.println(">>>>");
        System.out.println(commandAPDU);

        System.out.println(bytesToHex(commandAPDU.getBytes()));

        ResponseAPDU responseAPDU = m_channel.transmit(commandAPDU);

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
                message = String.format("\r\n%1s;%d.%d;", JCSYSTEM_STR[1], versionMajor, versionMinor); 
                System.out.println(message);
                pFile.write(message.getBytes());
                pValue.append(message);
                message = String.format("\r\n%s;%s;", JCSYSTEM_STR[2],(bDeletionSupported != 0) ? "yes" : "no"); 

                System.out.println(message);
                pFile.write(message.getBytes());
                pValue.append(message);
                message = String.format("\r\n%s;%s%dB;", JCSYSTEM_STR[3],(eepromSize == 32767) ? ">" : "", eepromSize); 
                System.out.println(message);
                pFile.write(message.getBytes());
                pValue.append(message);
                message = String.format("\r\n%s;%s%dB;", JCSYSTEM_STR[4],(ramResetSize == 32767) ? ">" : "", ramResetSize); 
                System.out.println(message);
                pFile.write(message.getBytes());
                pValue.append(message);
                message = String.format("\r\n%s;%s%dB;\n", JCSYSTEM_STR[5],(ramDeselectSize == 32767) ? ">" : "", ramDeselectSize); 
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

    public int GetSupportedAndParse(byte algClass, String algNames[], StringBuilder pValue, FileOutputStream pFile, byte algPartP2) throws Exception {
        int         status = STAT_OK;
        byte        suppAlg[] = new byte[MAX_SUPP_ALG];
        long       elapsedCard;
        boolean     bNamePrinted = false;

        // CLEAR ARRAY FOR SUPPORTED ALGORITHMS
        Arrays.fill(suppAlg, SUPP_ALG_UNTOUCHED);

        // PREPARE SEPARATE APDU FOR EACH SIGNALIZED P2 VALUE
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
                System.out.println("Fail to obtain response for GetSupportedAndParse");
            } else {
                // SAVE TIME OF CARD RESPONSE
                elapsedCard += System.currentTimeMillis();

                String elTimeStr = "";
                // OUTPUT REQUIRED TIME WHEN PARTITIONED CHECk WAS PERFORMED (NOTMULTIPLE ALGORITHMS IN SINGLE RUN)
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
                        if ((temp[i] != SUPP_ALG_UNTOUCHED) && ((short) (temp[i]&0xff) != SUPP_ALG_SEPARATOR)) {
                            suppAlg[i] = temp[i];    

                            // ALG NAME
                            String algState = "";
                            switch (suppAlg[i]) {
                                case 0: {
                                    algState += algNames[i]; algState += ";"; algState += "no;"; algState += "\r\n";
                                    break;
                                }
                                case 1: {
                                    algState += algNames[i]; algState += ";"; algState += "yes;"; algState += elTimeStr; algState += "\r\n";
                                    break;
                                }
                                case 2: {
                                    algState += algNames[i]; algState += ";"; algState += "error;"; algState += "\r\n";
                                    break;
                                }
                                case 0x6f: {
                                    algState += algNames[i]; algState += ";"; algState += "maybe;"; algState += "\r\n";
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

        return status;
    }


    public int TestAvailableRAMMemory(StringBuilder pValue, FileOutputStream pFile, byte algPartP2) throws Exception {
        int         status = STAT_OK;
        long     elapsedCard;

        // Prepare test memory apdu
        byte apdu[] = new byte[HEADER_LENGTH];
        apdu[OFFSET_CLA] = (byte) 0xB0;
        apdu[OFFSET_INS] = (byte) 0x71;
        apdu[OFFSET_P1] = 0x00;
        apdu[OFFSET_P2] = 0x00;
        apdu[OFFSET_LC] = 0x00;
            
        elapsedCard = -System.currentTimeMillis();

        ResponseAPDU resp = sendAPDU(apdu);
        if (resp.getSW() != 0x9000) {
            System.out.println("Fail to obtain response for TestAvailableRAMMemory");
        } else {
            // SAVE TIME OF CARD RESPONSE
            elapsedCard += System.currentTimeMillis();
            
            // OK, STORE RESPONSE TO suppAlg ARRAY
            byte temp[] = resp.getData();
                
            String elTimeStr = "";
            // OUTPUT REQUIRED TIME WHEN PARTITIONED CHECk WAS PERFORMED (NOTMULTIPLE ALGORITHMS IN SINGLE RUN)
            elTimeStr = String.format("%1f", (double) elapsedCard / (float) CLOCKS_PER_SEC);

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
        int         status = STAT_OK;
        long     elapsedCard;

        // Prepare test memory apdu
        byte apdu[] = new byte[HEADER_LENGTH];
        apdu[OFFSET_CLA] = (byte) 0xB0;
        apdu[OFFSET_INS] = (byte) 0x71;
        apdu[OFFSET_P1] = 0x01;
        apdu[OFFSET_P2] = 0x00;
        apdu[OFFSET_LC] = 0x00;
            
        elapsedCard = -System.currentTimeMillis();

        ResponseAPDU resp = sendAPDU(apdu);
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
            elTimeStr = String.format("%1f", (double) elapsedCard / (float) CLOCKS_PER_SEC);

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
    
    public int TestExtendedAPDUSupportSupport(StringBuilder pValue, FileOutputStream pFile, byte algPartP2) throws Exception {
        int         status = STAT_OK;
        
        byte apdu[] = new byte[HEADER_LENGTH + 2 + EXTENDED_APDU_TEST_LENGTH]; // + 2 is because of encoding of LC length into three bytes total
        apdu[OFFSET_CLA] = (byte) 0xB0;
        apdu[OFFSET_INS] = (byte) 0x74;
        apdu[OFFSET_P1] = 0x00;
        apdu[OFFSET_P2] = 0x00;
        apdu[OFFSET_LC] = 0x00;
        apdu[OFFSET_LC+1] = EXTENDED_APDU_TEST_LENGTH & 0xff00 >> 8;
        apdu[OFFSET_LC+2] = EXTENDED_APDU_TEST_LENGTH & 0xff;
            
        String message;
        message = "\r\nSupport for extended APDU. If supported, APDU longer than 255 bytes can be send.;"; 
        System.out.println(message);
        pFile.write(message.getBytes());
        pValue.append(message);
        
        ResponseAPDU resp = sendAPDU(apdu);
        if (resp.getSW() != 0x9000) {
            message = String.format("no;"); 
        }
        else {
            // OK, STORE RESPONSE TO suppAlg ARRAY
            byte temp[] = resp.getData();
            
            short LC = (short) ((temp[0] << 8) + (temp[1] & 0xff));
            short realLC = (short) ((temp[2] << 8) + (temp[3] & 0xff));
            
            if (LC == EXTENDED_APDU_TEST_LENGTH && realLC == EXTENDED_APDU_TEST_LENGTH) {
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
