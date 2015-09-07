package AlgTest;

public class JCConsts { 

    
     // Class javacard.security.Signature
    // javacard.security.Signature Fields:
    public static final byte Signature_ALG_DES_MAC4_NOPAD                           = 1;
    public static final byte Signature_ALG_DES_MAC8_NOPAD                           = 2;
    public static final byte Signature_ALG_DES_MAC4_ISO9797_M1                      = 3;
    public static final byte Signature_ALG_DES_MAC8_ISO9797_M1                      = 4;
    public static final byte Signature_ALG_DES_MAC4_ISO9797_M2                      = 5;
    public static final byte Signature_ALG_DES_MAC8_ISO9797_M2                      = 6;
    public static final byte Signature_ALG_DES_MAC4_PKCS5                           = 7;
    public static final byte Signature_ALG_DES_MAC8_PKCS5                           = 8;
    public static final byte Signature_ALG_RSA_SHA_ISO9796                          = 9;
    public static final byte Signature_ALG_RSA_SHA_PKCS1                            = 10;
    public static final byte Signature_ALG_RSA_MD5_PKCS1                            = 11;
    public static final byte Signature_ALG_RSA_RIPEMD160_ISO9796                    = 12;
    public static final byte Signature_ALG_RSA_RIPEMD160_PKCS1                      = 13;
    public static final byte Signature_ALG_DSA_SHA                                  = 14;
    public static final byte Signature_ALG_RSA_SHA_RFC2409                          = 15;
    public static final byte Signature_ALG_RSA_MD5_RFC2409                          = 16;
    public static final byte Signature_ALG_ECDSA_SHA                                = 17;
    public static final byte Signature_ALG_AES_MAC_128_NOPAD                        = 18;
    public static final byte Signature_ALG_DES_MAC4_ISO9797_1_M2_ALG3               = 19;
    public static final byte Signature_ALG_DES_MAC8_ISO9797_1_M2_ALG3               = 20;
    public static final byte Signature_ALG_RSA_SHA_PKCS1_PSS                        = 21;
    public static final byte Signature_ALG_RSA_MD5_PKCS1_PSS                        = 22;
    public static final byte Signature_ALG_RSA_RIPEMD160_PKCS1_PSS                  = 23;
      // JC2.2.2
    public static final byte Signature_ALG_HMAC_SHA1                                = 24;
    public static final byte Signature_ALG_HMAC_SHA_256                             = 25;
    public static final byte Signature_ALG_HMAC_SHA_384                             = 26;
    public static final byte Signature_ALG_HMAC_SHA_512                             = 27;
    public static final byte Signature_ALG_HMAC_MD5                                 = 28;
    public static final byte Signature_ALG_HMAC_RIPEMD160                           = 29;
    public static final byte Signature_ALG_RSA_SHA_ISO9796_MR                       = 30;
    public static final byte Signature_ALG_RSA_RIPEMD160_ISO9796_MR                 = 31;
    public static final byte Signature_ALG_KOREAN_SEED_MAC_NOPAD                    = 32;
    // JC3.0.1
    public static final byte Signature_ALG_ECDSA_SHA_256                            = 33;
    public static final byte Signature_ALG_ECDSA_SHA_384                            = 34;
    public static final byte Signature_ALG_AES_MAC_192_NOPAD                        = 35;
    public static final byte Signature_ALG_AES_MAC_256_NOPAD                        = 36;
    public static final byte Signature_ALG_ECDSA_SHA_224                            = 37;
    public static final byte Signature_ALG_ECDSA_SHA_512                            = 38;
    public static final byte Signature_ALG_RSA_SHA_224_PKCS1                        = 39;
    public static final byte Signature_ALG_RSA_SHA_256_PKCS1                        = 40;
    public static final byte Signature_ALG_RSA_SHA_384_PKCS1                        = 41;
    public static final byte Signature_ALG_RSA_SHA_512_PKCS1                        = 42;
    public static final byte Signature_ALG_RSA_SHA_224_PKCS1_PSS                    = 43;
    public static final byte Signature_ALG_RSA_SHA_256_PKCS1_PSS                    = 44;
    public static final byte Signature_ALG_RSA_SHA_384_PKCS1_PSS                    = 45;
    public static final byte Signature_ALG_RSA_SHA_512_PKCS1_PSS                    = 46;
    // JC3.0.4
    public static final byte Signature_ALG_DES_MAC4_ISO9797_1_M1_ALG3               = 47;
    public static final byte Signature_ALG_DES_MAC8_ISO9797_1_M1_ALG3               = 48;
    public static final byte Signature_SIG_CIPHER_DES_MAC4                          = 1;
    public static final byte Signature_SIG_CIPHER_DES_MAC8                          = 2;
    public static final byte Signature_SIG_CIPHER_RSA                               = 3;
    public static final byte Signature_SIG_CIPHER_DSA                               = 4;
    public static final byte Signature_SIG_CIPHER_ECDSA                             = 5;
    public static final byte Signature_SIG_CIPHER_AES_MAC128                        = 6;
    public static final byte Signature_SIG_CIPHER_HMAC                              = 7;
    public static final byte Signature_SIG_CIPHER_KOREAN_SEED_MAC                   = 8;
    
    public static final byte Signature_MODE_SIGN                                    = 1;
    public static final byte Signature_MODE_VERIFY                                  = 2;

    // javacard.security.Signature Methods:
    public static final short Signature_getLength                                   = 1;
    public static final short Signature_update                                      = 2;
    public static final short Signature_getInstance                                 = 3;
    public static final short Signature_getInstance2                                = 4;
    public static final short Signature_init                                        = 5;
    public static final short Signature_init2                                       = 6;
    public static final short Signature_verify                                      = 7;
    public static final short Signature_getAlgorithm                                = 8;
    public static final short Signature_signPreComputedHash                         = 9;
    public static final short Signature_sign                                        = 10;
    public static final short Signature_setInitialDigest                            = 11;

    // Class javacardx.crypto.Cipher
    // javacardx.crypto.Cipher Fields:
    public static final byte Cipher_ALG_DES_CBC_NOPAD                               = 1;
    public static final byte Cipher_ALG_DES_CBC_ISO9797_M1                          = 2;
    public static final byte Cipher_ALG_DES_CBC_ISO9797_M2                          = 3;
    public static final byte Cipher_ALG_DES_CBC_PKCS5                               = 4;
    public static final byte Cipher_ALG_DES_ECB_NOPAD                               = 5;
    public static final byte Cipher_ALG_DES_ECB_ISO9797_M1                          = 6;
    public static final byte Cipher_ALG_DES_ECB_ISO9797_M2                          = 7;
    public static final byte Cipher_ALG_DES_ECB_PKCS5                               = 8;
    public static final byte Cipher_ALG_RSA_ISO14888                                = 9;
    public static final byte Cipher_ALG_RSA_PKCS1                                   = 10;
    public static final byte Cipher_ALG_RSA_ISO9796                                 = 11;
    public static final byte Cipher_ALG_RSA_NOPAD                                   = 12;
    public static final byte Cipher_ALG_AES_BLOCK_128_CBC_NOPAD                     = 13;
    public static final byte Cipher_ALG_AES_BLOCK_128_ECB_NOPAD                     = 14;
    public static final byte Cipher_ALG_RSA_PKCS1_OAEP                              = 15;
      // JC2.2.2
    public static final byte Cipher_ALG_KOREAN_SEED_ECB_NOPAD                       = 16;
    public static final byte Cipher_ALG_KOREAN_SEED_CBC_NOPAD                       = 17;
    // JC3.0.1
    public static final byte Cipher_ALG_AES_BLOCK_192_CBC_NOPAD                     = 18;
    public static final byte Cipher_ALG_AES_BLOCK_192_ECB_NOPAD                     = 19;
    public static final byte Cipher_ALG_AES_BLOCK_256_CBC_NOPAD                     = 20;
    public static final byte Cipher_ALG_AES_BLOCK_256_ECB_NOPAD                     = 21;
    public static final byte Cipher_ALG_AES_CBC_ISO9797_M1                          = 22;
    public static final byte Cipher_ALG_AES_CBC_ISO9797_M2                          = 23;
    public static final byte Cipher_ALG_AES_CBC_PKCS5                               = 24;
    public static final byte Cipher_ALG_AES_ECB_ISO9797_M1                          = 25;
    public static final byte Cipher_ALG_AES_ECB_ISO9797_M2                          = 26;
    public static final byte Cipher_ALG_AES_ECB_PKCS5                               = 27;
    public static final byte Cipher_CIPHER_AES_CBC                                  = 1;
    public static final byte Cipher_CIPHER_AES_ECB                                  = 2;
    public static final byte Cipher_CIPHER_DES_CBC                                  = 3;
    public static final byte Cipher_CIPHER_DES_ECB                                  = 4;
    public static final byte Cipher_CIPHER_KOREAN_SEED_CBC                          = 5;
    public static final byte Cipher_CIPHER_KOREAN_SEED_ECB                          = 6;
    public static final byte Cipher_CIPHER_RSA                                      = 7;
    public static final byte Cipher_PAD_NULL                                        = 0;
    public static final byte Cipher_PAD_NOPAD                                       = 1;
    public static final byte Cipher_PAD_ISO9797_M1                                  = 2;
    public static final byte Cipher_PAD_ISO9797_M2                                  = 3;
    public static final byte Cipher_PAD_ISO9797_1_M1_ALG3                           = 4;
    public static final byte Cipher_PAD_ISO9797_1_M2_ALG3                           = 5;
    public static final byte Cipher_PAD_PKCS5                                       = 6;
    public static final byte Cipher_PAD_PKCS1                                       = 7;
    public static final byte Cipher_PAD_PKCS1_PSS                                   = 8;
    public static final byte Cipher_PAD_PKCS1_OAEP                                  = 9;
    public static final byte Cipher_PAD_ISO9796                                     = 10;
    public static final byte Cipher_PAD_ISO9796_MR                                  = 11;
    public static final byte Cipher_PAD_RFC2409                                     = 12;

    public static final byte Cipher_MODE_DECRYPT                                    = 1;
    public static final byte Cipher_MODE_ENCRYPT                                    = 2;

    // javacardx.crypto.Cipher Methods:
    public static final short Cipher_update                                         = 1;
    public static final short Cipher_getInstance                                    = 2;
    public static final short Cipher_getInstance2                                   = 3;
    public static final short Cipher_init                                           = 4;
    public static final short Cipher_init2                                          = 5;
    public static final short Cipher_getAlgorithm                                   = 6;
    public static final short Cipher_doFinal                                        = 7;

    // Class javacard.security.KeyAgreement
    // javacard.security.KeyAgreement Fields:
    public static final byte KeyAgreement_ALG_EC_SVDP_DH                            = 1;
    public static final byte KeyAgreement_ALG_EC_SVDP_DH_KDF                        = 1;
    public static final byte KeyAgreement_ALG_EC_SVDP_DHC                           = 2;
    public static final byte KeyAgreement_ALG_EC_SVDP_DHC_KDF                       = 2;
    public static final byte KeyAgreement_ALG_EC_SVDP_DH_PLAIN                      = 3;
    public static final byte KeyAgreement_ALG_EC_SVDP_DHC_PLAIN                     = 4;

    // javacard.security.KeyAgreement Methods:
    public static final short KeyAgreement_getInstance                              = 1;
    public static final short KeyAgreement_init                                     = 2;
    public static final short KeyAgreement_getAlgorithm                             = 3;
    public static final short KeyAgreement_generateSecret                           = 4;

    // Class javacard.security.KeyBuilder
    // javacard.security.KeyBuilder Fields:
    public static final byte KeyBuilder_TYPE_DES_TRANSIENT_RESET                    = 1;
    public static final byte KeyBuilder_TYPE_DES_TRANSIENT_DESELECT                 = 2;
    public static final byte KeyBuilder_TYPE_DES                                    = 3;
    public static final byte KeyBuilder_TYPE_RSA_PUBLIC                             = 4;
    public static final byte KeyBuilder_TYPE_RSA_PRIVATE                            = 5;
    public static final byte KeyBuilder_TYPE_RSA_CRT_PRIVATE                        = 6;
    public static final byte KeyBuilder_TYPE_DSA_PUBLIC                             = 7;
    public static final byte KeyBuilder_TYPE_DSA_PRIVATE                            = 8;
    public static final byte KeyBuilder_TYPE_EC_F2M_PUBLIC                          = 9;
    public static final byte KeyBuilder_TYPE_EC_F2M_PRIVATE                         = 10;
    public static final byte KeyBuilder_TYPE_EC_FP_PUBLIC                           = 11;
    public static final byte KeyBuilder_TYPE_EC_FP_PRIVATE                          = 12;
    public static final byte KeyBuilder_TYPE_AES_TRANSIENT_RESET                    = 13;
    public static final byte KeyBuilder_TYPE_AES_TRANSIENT_DESELECT                 = 14;
    public static final byte KeyBuilder_TYPE_AES                                    = 15;
      // JC2.2.2
    public static final byte KeyBuilder_TYPE_KOREAN_SEED_TRANSIENT_RESET            = 16;
    public static final byte KeyBuilder_TYPE_KOREAN_SEED_TRANSIENT_DESELECT         = 17;
    public static final byte KeyBuilder_TYPE_KOREAN_SEED                            = 18;
    public static final byte KeyBuilder_TYPE_HMAC_TRANSIENT_RESET                   = 19;
    public static final byte KeyBuilder_TYPE_HMAC_TRANSIENT_DESELECT                = 20;
    public static final byte KeyBuilder_TYPE_HMAC                                   = 21;
    // JC3.0.1
    public static final byte KeyBuilder_TYPE_RSA_PRIVATE_TRANSIENT_RESET            = 22;
    public static final byte KeyBuilder_TYPE_RSA_PRIVATE_TRANSIENT_DESELECT         = 23;
    public static final byte KeyBuilder_TYPE_RSA_CRT_PRIVATE_TRANSIENT_RESET        = 24;
    public static final byte KeyBuilder_TYPE_RSA_CRT_PRIVATE_TRANSIENT_DESELECT     = 25;
    public static final byte KeyBuilder_TYPE_DSA_PRIVATE_TRANSIENT_RESET            = 26;
    public static final byte KeyBuilder_TYPE_DSA_PRIVATE_TRANSIENT_DESELECT         = 27;
    public static final byte KeyBuilder_TYPE_EC_F2M_PRIVATE_TRANSIENT_RESET         = 28;
    public static final byte KeyBuilder_TYPE_EC_F2M_PRIVATE_TRANSIENT_DESELECT      = 29;
    public static final byte KeyBuilder_TYPE_EC_FP_PRIVATE_TRANSIENT_RESET          = 30;
    public static final byte KeyBuilder_TYPE_EC_FP_PRIVATE_TRANSIENT_DESELECT       = 31;
    // JC3.0.4
    public static final byte KeyBuilder_ALG_TYPE_DES                                = 1;
    public static final byte KeyBuilder_ALG_TYPE_AES                                = 2;
    public static final byte KeyBuilder_ALG_TYPE_DSA_PUBLIC                         = 3;
    public static final byte KeyBuilder_ALG_TYPE_DSA_PRIVATE                        = 4;
    public static final byte KeyBuilder_ALG_TYPE_EC_F2M_PUBLIC                      = 5;
    public static final byte KeyBuilder_ALG_TYPE_EC_F2M_PRIVATE                     = 6;
    public static final byte KeyBuilder_ALG_TYPE_EC_FP_PUBLIC                       = 7;
    public static final byte KeyBuilder_ALG_TYPE_EC_FP_PRIVATE                      = 8;
    public static final byte KeyBuilder_ALG_TYPE_HMAC                               = 9;
    public static final byte KeyBuilder_ALG_TYPE_KOREAN_SEED                        = 10;
    public static final byte KeyBuilder_ALG_TYPE_RSA_PUBLIC                         = 11;
    public static final byte KeyBuilder_ALG_TYPE_RSA_PRIVATE                        = 12;
    public static final byte KeyBuilder_ALG_TYPE_RSA_CRT_PRIVATE                    = 13;

    public static final short KeyBuilder_LENGTH_DES                                 = 64;
    public static final short KeyBuilder_LENGTH_DES3_2KEY                           = 128;
    public static final short KeyBuilder_LENGTH_DES3_3KEY                           = 192;
    public static final short KeyBuilder_LENGTH_RSA_512                             = 512;
    public static final short KeyBuilder_LENGTH_RSA_736                             = 736;
    public static final short KeyBuilder_LENGTH_RSA_768                             = 768;
    public static final short KeyBuilder_LENGTH_RSA_896                             = 896;
    public static final short KeyBuilder_LENGTH_RSA_1024                            = 1024;
    public static final short KeyBuilder_LENGTH_RSA_1280                            = 1280;
    public static final short KeyBuilder_LENGTH_RSA_1536                            = 1536;
    public static final short KeyBuilder_LENGTH_RSA_1984                            = 1984;
    public static final short KeyBuilder_LENGTH_RSA_2048                            = 2048;
    public static final short KeyBuilder_LENGTH_RSA_3072                            = 3072;
    public static final short KeyBuilder_LENGTH_RSA_4096                            = 4096;
    public static final short KeyBuilder_LENGTH_DSA_512                             = 512;
    public static final short KeyBuilder_LENGTH_DSA_768                             = 768;
    public static final short KeyBuilder_LENGTH_DSA_1024                            = 1024;
    public static final short KeyBuilder_LENGTH_EC_FP_112                           = 112;
    public static final short KeyBuilder_LENGTH_EC_F2M_113                          = 113;
    public static final short KeyBuilder_LENGTH_EC_FP_128                           = 128;
    public static final short KeyBuilder_LENGTH_EC_F2M_131                          = 131;
    public static final short KeyBuilder_LENGTH_EC_FP_160                           = 160;
    public static final short KeyBuilder_LENGTH_EC_F2M_163                          = 163;
    public static final short KeyBuilder_LENGTH_EC_FP_192                           = 192;
    public static final short KeyBuilder_LENGTH_EC_F2M_193                          = 193;
    public static final short KeyBuilder_LENGTH_EC_FP_224                           = 224;
    public static final short KeyBuilder_LENGTH_EC_FP_256                           = 256;
    public static final short KeyBuilder_LENGTH_EC_FP_384                           = 384;
    public static final short KeyBuilder_LENGTH_EC_FP_521                           = 521;
    public static final short KeyBuilder_LENGTH_AES_128                             = 128;
    public static final short KeyBuilder_LENGTH_AES_192                             = 192;
    public static final short KeyBuilder_LENGTH_AES_256                             = 256;
      // JC2.2.2
    public static final short KeyBuilder_LENGTH_KOREAN_SEED_128                     = 128;
    public static final short KeyBuilder_LENGTH_HMAC_SHA_1_BLOCK_64                 = 64;
    public static final short KeyBuilder_LENGTH_HMAC_SHA_256_BLOCK_64               = 64;
    public static final short KeyBuilder_LENGTH_HMAC_SHA_384_BLOCK_128              = 128;
    public static final short KeyBuilder_LENGTH_HMAC_SHA_512_BLOCK_128              = 128;

    // javacard.security.KeyBuilder Methods:
    public static final short KeyBuilder_buildKey                                   = 1;
    public static final short KeyBuilder_buildKey2                                  = 2;

    // Class javacard.security.KeyPair
    // javacard.security.KeyPair Fields:
    // JC2.1.1
    public static final byte KeyPair_ALG_RSA                                        = 1;
    public static final byte KeyPair_ALG_RSA_CRT                                    = 2;
    public static final byte KeyPair_ALG_DSA                                        = 3;
    public static final byte KeyPair_ALG_EC_F2M                                     = 4;
    public static final byte KeyPair_ALG_EC_FP                                      = 5;

    // javacard.security.KeyPair Methods:
    public static final short KeyPair_genKeyPair                                    = 1;
    public static final short KeyPair_getPrivate                                    = 2;
    public static final short KeyPair_getPublic                                     = 3;

    // Class javacard.security.MessageDigest
    // javacard.security.MessageDigest Fields:
    public static final byte MessageDigest_ALG_NULL                                 = 0;
    public static final byte MessageDigest_ALG_SHA                                  = 1;
    public static final byte MessageDigest_ALG_MD5                                  = 2;
    public static final byte MessageDigest_ALG_RIPEMD160                            = 3;
      // JC2.2.2
    public static final byte MessageDigest_ALG_SHA_256                              = 4;
    public static final byte MessageDigest_ALG_SHA_384                              = 5;
    public static final byte MessageDigest_ALG_SHA_512                              = 6;
    // JC3.0.1
    public static final byte MessageDigest_ALG_SHA_224                              = 7;
    public static final byte MessageDigest_LENGTH_MD5                               = 16;
    public static final byte MessageDigest_LENGTH_RIPEMD160                         = 20;
    public static final byte MessageDigest_LENGTH_SHA                               = 20;
    public static final byte MessageDigest_LENGTH_SHA_224                           = 28;
    public static final byte MessageDigest_LENGTH_SHA_256                           = 32;
    public static final byte MessageDigest_LENGTH_SHA_384                           = 48;
    public static final byte MessageDigest_LENGTH_SHA_512                           = 64;

    // javacard.security.MessageDigest Methods:
    public static final short MessageDigest_getLength                               = 1;
    public static final short MessageDigest_update                                  = 2;
    public static final short MessageDigest_getInstance                             = 3;
    public static final short MessageDigest_reset                                   = 4;
    public static final short MessageDigest_getAlgorithm                            = 5;
    public static final short MessageDigest_doFinal                                 = 6;
    public static final short MessageDigest_getInitializedMessageDigestInstance     = 7;

    // Class javacard.security.RandomData
    // javacard.security.RandomData Fields:
    public static final byte RandomData_ALG_PSEUDO_RANDOM                           = 1;
    public static final byte RandomData_ALG_SECURE_RANDOM                           = 2;

    // javacard.security.RandomData Methods:
    public static final short RandomData_getInstance                                = 1;
    public static final short RandomData_setSeed                                    = 2;
    public static final short RandomData_generateData                               = 3;

    // Class javacard.security.Checksum
    // javacard.security.Checksum Fields:
    public static final byte Checksum_ALG_ISO3309_CRC16                             = 1;
    public static final byte Checksum_ALG_ISO3309_CRC32                             = 2;

    // javacard.security.Checksum Methods:
    public static final short Checksum_update                                       = 1;
    public static final short Checksum_getInstance                                  = 2;
    public static final short Checksum_init                                         = 3;
    public static final short Checksum_getAlgorithm                                 = 4;
    public static final short Checksum_doFinal                                      = 5;

    // Class javacardx.crypto.KeyEncryption
    // javacardx.crypto.KeyEncryption Fields:
    //  -- No Fields --
    // javacardx.crypto.KeyEncryption Methods:
    public static final short KeyEncryption_setKeyCipher                            = 1;
    public static final short KeyEncryption_getKeyCipher                            = 2;

    // Class javacard.security.AESKey
    // javacard.security.AESKey Fields:
    //  -- No Fields --
    // javacardx.crypto.KeyEncryption Methods:
    public static final short AESKey_setKey                                         = 1;
    public static final short AESKey_clearKey                                       = 2;
    public static final short AESKey_getKey                                         = 3;
    
    // Class javacard.security.DESKey
    // javacard.security.DESKey Fields:
    //  -- No Fields --
    // javacard.security.DESKey Methods:
    public static final short DESKey_getKey                                         = 1;
    public static final short DESKey_setKey                                         = 2;
    public static final short DESKey_clearKey                                       = 3;
    
    // Class javacard.security.KoreanSEEDKey
    // javacard.security.KoreanSEEDKey Fields:
    //  -- No Fields --
    // javacard.security.KoreanSEEDKey Methods:
    public static final short KoreanSEEDKey_getKey                                  = 1;
    public static final short KoreanSEEDKey_setKey                                  = 2;
    public static final short KoreanSEEDKey_clearKey                                = 3;

    // Class javacard.security.DSAKey
    // javacard.security.DSAKey Fields:
    //  -- No Fields --
    // javacard.security.DSAKey Methods:
    public static final short DSAKey_setQ                                           = 1;
    public static final short DSAKey_getP                                           = 2;
    public static final short DSAKey_getG                                           = 3;
    public static final short DSAKey_setG                                           = 4;
    public static final short DSAKey_setP                                           = 5;
    public static final short DSAKey_getQ                                           = 6;
    public static final short DSAKey_clearKey                                       = 7;

    // Class javacard.security.DSAPrivateKey
    // javacard.security.DSAPrivateKey Fields:
    //  -- No Fields --
    // javacard.security.DSAPrivateKey Methods:
    public static final short DSAPrivateKey_getX                                    = 1;
    public static final short DSAPrivateKey_setX                                    = 2;
    public static final short DSAPrivateKey_clearX                                  = 3;

    // Class javacard.security.DSAPublicKey
    // javacard.security.DSAPublicKey Fields:
    //  -- No Fields --
    // javacard.security.DSAPublicKey Methods:
    public static final short DSAPublicKey_setY                                     = 1;
    public static final short DSAPublicKey_getY                                     = 2;
    public static final short DSAPublicKey_clearY                                   = 3;

    // Class javacard.security.ECKey
    // javacard.security.ECKey Fields:
    //  -- No Fields --
    // javacard.security.ECKey Methods:
    public static final short ECKey_getField                                        = 1;
    public static final short ECKey_getB                                            = 2;
    public static final short ECKey_getK                                            = 3;
    public static final short ECKey_setFieldFP                                      = 4;
    public static final short ECKey_setR                                            = 5;
    public static final short ECKey_getA                                            = 6;
    public static final short ECKey_setFieldF2M                                     = 7;
    //public static final short ECKey_setFieldF2M                                     = 8;  // @askpetr
    public static final short ECKey_setB                                            = 9;
    public static final short ECKey_getR                                            = 10;
    public static final short ECKey_setA                                            = 11;
    public static final short ECKey_setK                                            = 12;
    public static final short ECKey_getG                                            = 13;
    public static final short ECKey_setG                                            = 14;
    public static final short ECKey_clearKey                                        = 15;

    // Class javacard.security.ECPrivateKey
    // javacard.security.ECPrivateKey Fields:
    //  -- No Fields --
    // javacard.security.ECPrivateKey Methods:
    public static final short ECPrivateKey_setS                                     = 1;
    public static final short ECPrivateKey_getS                                     = 2;
    public static final short ECPrivateKey_clearKey                                 = 3;
    
    // Class javacard.security.ECPublicKey
    // javacard.security.ECPublicKey Fields:
    //  -- No Fields --
    // javacard.security.ECPublicKey Methods:
    public static final short ECPublicKey_setW                                      = 1;
    public static final short ECPublicKey_getW                                      = 2;
    public static final short ECPublicKey_clearKey                                  = 3;
    
    // Class javacard.security.HMACKey
    // javacard.security.HMACKey Fields:
    //  -- No Fields --
    // javacard.security.HMACKey Methods:
    public static final short HMACKey_getKey                                        = 1;
    public static final short HMACKey_setKey                                        = 2;
    public static final short HMACKey_clearKey                                      = 3;

    // Class javacard.security.RSAPrivateCrtKey
    // javacard.security.RSAPrivateCrtKey Fields:
    //  -- No Fields --
    // javacard.security.RSAPrivateCrtKey Methods:
    public static final short RSAPrivateCrtKey_setDP1                               = 1;
    public static final short RSAPrivateCrtKey_setPQ                                = 2;
    public static final short RSAPrivateCrtKey_getDP1                               = 3;
    public static final short RSAPrivateCrtKey_setDQ1                               = 4;
    public static final short RSAPrivateCrtKey_getDQ1                               = 5;
    public static final short RSAPrivateCrtKey_getPQ                                = 6;
    public static final short RSAPrivateCrtKey_setQ                                 = 7;
    public static final short RSAPrivateCrtKey_getP                                 = 8;
    public static final short RSAPrivateCrtKey_setP                                 = 9;
    public static final short RSAPrivateCrtKey_getQ                                 = 10;
    public static final short RSAPrivateCrtKey_clearKey                             = 11;

    // Class javacard.security.RSAPrivateKey
    // javacard.security.RSAPrivateKey Fields:
    //  -- No Fields --
    // javacard.security.RSAPrivateKey Methods:
    public static final short RSAPrivateKey_getExponent                             = 1;
    public static final short RSAPrivateKey_setExponent                             = 2;
    public static final short RSAPrivateKey_getModulus                              = 3;
    public static final short RSAPrivateKey_setModulus                              = 4;
    public static final short RSAPrivateKey_clearKey                                = 5;

    // Class javacard.security.RSAPublicKey
    // javacard.security.RSAPublicKey Fields:
    //  -- No Fields --
    // javacard.security.RSAPublicKey Methods:
    public static final short RSAPublicKey_getExponent                              = 1;
    public static final short RSAPublicKey_setExponent                              = 2;
    public static final short RSAPublicKey_getModulus                               = 3;
    public static final short RSAPublicKey_setModulus                               = 4;
    public static final short RSAPublicKey_clearKey                                 = 5;

    // Class javacard.security.SignatureMessageRecovery
    // javacard.security.SignatureMessageRecovery Fields:
    //  -- No Fields --
    // javacard.security.SignatureMessageRecovery Methods:
    public static final short SignatureMessageRecovery_getLength                    = 1;
    public static final short SignatureMessageRecovery_update                       = 2;
    public static final short SignatureMessageRecovery_init                         = 3;
    public static final short SignatureMessageRecovery_verify                       = 4;
    public static final short SignatureMessageRecovery_getAlgorithm                 = 5;
    public static final short SignatureMessageRecovery_beginVerify                  = 6;
    public static final short SignatureMessageRecovery_sign                         = 7;    
    
    // Class javacard.framework.Util
    // javacard.framework.Util Fields:
    //  -- No Fields --
    // javacard.framework.Util Methods:
    public static final short Util_arrayCopy_RAM                                    = 1;
    public static final short Util_arrayCopy_EEPROM                                 = 2;
    public static final short Util_arrayCopy_RAM2EEPROM                             = 3;
    public static final short Util_arrayCopy_EEPROM2RAM                             = 4;
    public static final short Util_arrayCopyNonAtomic_RAM                           = 5;
    public static final short Util_arrayCopyNonAtomic_EEPROM                        = 6;
    public static final short Util_arrayCopyNonAtomic_RAM2EEPROM                    = 7;
    public static final short Util_arrayCopyNonAtomic_EEPROM2RAM                    = 8;
    public static final short Util_arrayFillNonAtomic_RAM                           = 9;
    public static final short Util_arrayFillNonAtomic_EEPROM                        = 10;
    public static final short Util_arrayCompare_RAM                                 = 11;
    public static final short Util_arrayCompare_EEPROM                              = 12;
    public static final short Util_arrayCompare_RAM2EEPROM                          = 13;
    public static final short Util_arrayCompare_EEPROM2RAM                          = 14;
    //public static final short Util_makeShort                                        = 15; // not tested
    //public static final short Util_getShort                                         = 16; // not tested
    //public static final short Util_setShort                                         = 17; // not tested
    
    public static final short SWAlgs_xor                                            = 1;    
    public static final short SWAlgs_AES                                            = 2;    
    
} 
