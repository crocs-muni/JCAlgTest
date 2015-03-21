package AlgTest;

/**
 * @author Petr Svenda <petr@svenda.com>
 */


public class Consts {

    public final static byte TRUE                  = (byte) 0x01; 
    public final static byte FALSE                 = (byte) 0x00; 
    
/*
    // Declares the annotation Twizzle.
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Twizzle { }
    
    @Twizzle
*/    
    public final static byte CLA_CARD_ALGTEST                  = (byte) 0xB0; 
    public final static byte INS_CARD_GETVERSION               = (byte) 0x60;
    public final static byte INS_CARD_RESET                    = (byte) 0x69;
    
    public final static byte INS_CARD_TESTSUPPORTEDMODES       = (byte) 0x70;
    public final static byte INS_CARD_TESTAVAILABLE_MEMORY     = (byte) 0x71;
    public final static byte INS_CARD_TESTRSAEXPONENTSET       = (byte) 0x72;
    public final static byte INS_CARD_JCSYSTEM_INFO            = (byte) 0x73;
    public final static byte INS_CARD_TESTEXTAPDU              = (byte) 0x74;
    public final static byte INS_CARD_TESTSUPPORTEDMODES_SINGLE= (byte) 0x75;    
    public final static byte INS_CARD_GETRSAKEY                = (byte) 0x77;    
    
    
    // BUGBUG: refactor codes
    public final static byte INS_PERF_TEST_CLASS_KEY           = (byte) 0x40;
    public final static byte INS_PERF_TEST_CLASS_MESSAGEDIGEST = (byte) 0x41;
    public final static byte INS_PERF_TEST_CLASS_RANDOMDATA    = (byte) 0x42;
    public final static byte INS_PERF_TEST_CLASS_CIPHER        = (byte) 0x43;
    public final static byte INS_PERF_TEST_CLASS_KEYPAIR       = (byte) 0x45;
    public final static byte INS_PERF_TEST_CLASS_CHECKSUM      = (byte) 0x46;
    public final static byte INS_PERF_TEST_CLASS_KEYAGREEMENT  = (byte) 0x47;
    public final static byte INS_PERF_TEST_CLASS_SIGNATURE     = (byte) 0x49;
    
    
    
/*
    public final static byte INS_PERF_TEST_KEY_BUILDER         = (byte) 0x44;
    public final static byte INS_PERF_PREPARE_KEY              = (byte) 0x48;
*/    
    
    
    public final static byte INS_PREPARE_TEST_CLASS_KEY        = (byte) 0x30;
    public final static byte INS_PREPARE_TEST_CLASS_CIPHER     = (byte) 0x31;
    public final static byte INS_PREPARE_TEST_CLASS_SIGNATURE  = (byte) 0x32;
    public final static byte INS_PREPARE_TEST_CLASS_RANDOMDATA = (byte) 0x33;
    public final static byte INS_PREPARE_TEST_CLASS_MESSAGEDIGEST = (byte) 0x34;
    public final static byte INS_PREPARE_TEST_CLASS_CHECKSUM    = (byte) 0x35;
    public final static byte INS_PREPARE_TEST_CLASS_KEYPAIR     = (byte) 0x36;
    public final static byte INS_PREPARE_TEST_CLASS_KEYAGREEMENT= (byte) 0x37;
    
    
    
    
    
    public static final byte MY_DSA = 88;       // bugbug: introduced probably because of value duplicity inside prepareSignature()
    
    public static final short CLASS_CIPHER                      = (short) 0x11;      
    public static final short CLASS_SIGNATURE                   = (short) 0x12;      
    public static final short CLASS_KEYAGREEMENT                = (short) 0x13;      
    public static final short CLASS_MESSAGEDIGEST               = (short) 0x15;      
    public static final short CLASS_RANDOMDATA                  = (short) 0x16;      
    public static final short CLASS_CHECKSUM                    = (short) 0x17;      
    public static final short CLASS_KEYPAIR                     = (short) 0x18;      
    public static final short CLASS_KEYENCRYPTION               = (short) 0x19;      
    public static final short CLASS_KEYBUILDER                  = (short) 0x20;      
    
    public static final short UNUSED    = (short) -1;     
    
    public static final short TEST_DATA_LENGTH    = (short) 256;     
    
	// TODO: refactor - do we need this?
    public static final byte CLASS_KEYPAIR_RSA_P2          = 11;
    public static final byte CLASS_KEYPAIR_RSACRT_P2       = 11;
    public static final byte CLASS_KEYPAIR_DSA_P2          = 3;
    public static final byte CLASS_KEYPAIR_EC_F2M_P2       = 4;
    public static final byte CLASS_KEYPAIR_EC_FP_P2        = 4;	
	// end refactor - do we need this?
        
/* del   
    
    public final static byte method_setKey                      = (byte) 1;
    public final static byte method_clearKey                    = (byte) 2;
    public final static byte method_getKey                      = (byte) 3;
         
    public final static byte overhead = 0;
    public final static byte Cipher_update                      = (byte) 1;
    public final static byte Cipher_doFinal                     = (byte) 2;
    public final static byte Cipher_init                        = (byte) 3;
    
    public final static byte Signature_update                   = (byte) 1;
    public final static byte Signature_sign                     = (byte) 2;
    public final static byte Signature_init                     = (byte) 3;
    public final static byte Signature_verify                   = (byte) 4;
    public final static byte Signature_signPreComputedHash      = (byte) 5;
    public final static byte Signature_setInitialDigest         = (byte) 6;
            
    public final static byte RandomData_generateData            = (byte) 1;
    public final static byte RandomData_setSeed                 = (byte) 2;
    
    public final static byte MessageDigest_update               = (byte) 1;
    public final static byte MessageDigest_doFinal              = (byte) 2;
    public final static byte MessageDigest_reset                = (byte) 3;
    
    public final static byte Checksum_update                    = (byte) 1;
    public final static byte Checksum_doFinal                   = (byte) 2;
    
    public final static byte KeyPair_genKeyPair                 = (byte) 1;

    public final static byte KeyAgreement_init                 = (byte) 1;
    public final static byte KeyAgreement_generateSecret       = (byte) 2;
    
    
    
      //  
      // Class javacard.security.Signature
      // Search: public static final (byte|short) ([A-Z0-9_]*) 
      // Collect: assertEquals(Consts.\1, javacard.security.Signature.\1); 
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
    
    public static final short LENGTH_DES            = 64;
    public static final short LENGTH_DES3_2KEY      = 128;
    public static final short LENGTH_DES3_3KEY      = 192;
    public static final short LENGTH_RSA_512        = 512;
    public static final short LENGTH_RSA_736        = 736;
    public static final short LENGTH_RSA_768        = 768;
    public static final short LENGTH_RSA_896        = 896;
    public static final short LENGTH_RSA_1024       = 1024;
    public static final short LENGTH_RSA_1280       = 1280;
    public static final short LENGTH_RSA_1536       = 1536;
    public static final short LENGTH_RSA_1984       = 1984;
    public static final short LENGTH_RSA_2048       = 2048;
    public static final short LENGTH_RSA_3072       = 3072;
    public static final short LENGTH_RSA_4096       = 4096;
    public static final short LENGTH_DSA_512        = 512;
    public static final short LENGTH_DSA_768        = 768;
    public static final short LENGTH_DSA_1024       = 1024;
    public static final short LENGTH_EC_FP_112      = 112;
    public static final short LENGTH_EC_F2M_113     = 113;
    public static final short LENGTH_EC_FP_128      = 128;
    public static final short LENGTH_EC_F2M_131     = 131;
    public static final short LENGTH_EC_FP_160      = 160;
    public static final short LENGTH_EC_F2M_163     = 163;
    public static final short LENGTH_EC_FP_192      = 192;
    public static final short LENGTH_EC_F2M_193     = 193;
    public static final short LENGTH_EC_FP_224      = 224;  
    public static final short LENGTH_EC_FP_256      = 256;  
    public static final short LENGTH_EC_FP_384      = 384;  
    public static final short LENGTH_EC_FP_521      = 521;    
    public static final short LENGTH_AES_128        = 128;
    public static final short LENGTH_AES_192        = 192;
    public static final short LENGTH_AES_256        = 256;
      // JC2.2.2
    public static final short LENGTH_KOREAN_SEED_128        = 128;
    public static final short LENGTH_HMAC_SHA_1_BLOCK_64    = 64;
    public static final short LENGTH_HMAC_SHA_256_BLOCK_64  = 64;
    public static final short LENGTH_HMAC_SHA_384_BLOCK_128  = 128;
    public static final short LENGTH_HMAC_SHA_512_BLOCK_128  = 128;
    
      //
      //Class javacard.security.KeyPair
      //introduced in 2.1.1
    public static final byte ALG_RSA                       = 1;
    public static final byte ALG_RSA_CRT                   = 2;
    public static final byte ALG_DSA                       = 3;
    public static final byte ALG_EC_F2M                    = 4;
    public static final byte ALG_EC_FP                     = 5;

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
    

      //Class javacard.security.RandomData
    public static final byte ALG_PSEUDO_RANDOM             = 1;
    public static final byte ALG_SECURE_RANDOM             = 2;


      // Class javacard.security.Checksum
    public static final byte ALG_ISO3309_CRC16             = 1;
    public static final byte ALG_ISO3309_CRC32             = 2;

*/
}
