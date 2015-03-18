/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package AlgTest;

import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author xsvenda
 */
public class ConstsNGTest {
    
    public ConstsNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }
    
    
    /**
     * Verify that our custom defined constants are same as constants from standard
     * @throws Exception 
     */
    @Test
    void verifyConstantsValidity() throws Exception {
        // NOTE: for checking, we need to have latest JavaCard library (3.0.4 at the moment). 
        // NOTE: This may interfere with jcardsim in outher tests - add javacard library only for this test and then remove
/*
        // javacard.security.Signature.*
        assertEquals(Consts.ALG_DES_MAC4_NOPAD, javacard.security.Signature.ALG_DES_MAC4_NOPAD);
        assertEquals(Consts.ALG_DES_MAC8_NOPAD, javacard.security.Signature.ALG_DES_MAC8_NOPAD);
        assertEquals(Consts.ALG_DES_MAC4_ISO9797_M1, javacard.security.Signature.ALG_DES_MAC4_ISO9797_M1);
        assertEquals(Consts.ALG_DES_MAC8_ISO9797_M1, javacard.security.Signature.ALG_DES_MAC8_ISO9797_M1);
        assertEquals(Consts.ALG_DES_MAC4_ISO9797_M2, javacard.security.Signature.ALG_DES_MAC4_ISO9797_M2);
        assertEquals(Consts.ALG_DES_MAC8_ISO9797_M2, javacard.security.Signature.ALG_DES_MAC8_ISO9797_M2);
        assertEquals(Consts.ALG_DES_MAC4_PKCS5, javacard.security.Signature.ALG_DES_MAC4_PKCS5);
        assertEquals(Consts.ALG_DES_MAC8_PKCS5, javacard.security.Signature.ALG_DES_MAC8_PKCS5);
        assertEquals(Consts.ALG_RSA_SHA_ISO9796, javacard.security.Signature.ALG_RSA_SHA_ISO9796);
        assertEquals(Consts.ALG_RSA_SHA_PKCS1, javacard.security.Signature.ALG_RSA_SHA_PKCS1);
        assertEquals(Consts.ALG_RSA_MD5_PKCS1, javacard.security.Signature.ALG_RSA_MD5_PKCS1);
        assertEquals(Consts.ALG_RSA_RIPEMD160_ISO9796, javacard.security.Signature.ALG_RSA_RIPEMD160_ISO9796);
        assertEquals(Consts.ALG_RSA_RIPEMD160_PKCS1, javacard.security.Signature.ALG_RSA_RIPEMD160_PKCS1);
        assertEquals(Consts.ALG_DSA_SHA, javacard.security.Signature.ALG_DSA_SHA);
        assertEquals(Consts.ALG_RSA_SHA_RFC2409, javacard.security.Signature.ALG_RSA_SHA_RFC2409);
        assertEquals(Consts.ALG_RSA_MD5_RFC2409, javacard.security.Signature.ALG_RSA_MD5_RFC2409);
        assertEquals(Consts.ALG_ECDSA_SHA, javacard.security.Signature.ALG_ECDSA_SHA);
        assertEquals(Consts.ALG_AES_MAC_128_NOPAD, javacard.security.Signature.ALG_AES_MAC_128_NOPAD);
        assertEquals(Consts.ALG_DES_MAC4_ISO9797_1_M2_ALG3, javacard.security.Signature.ALG_DES_MAC4_ISO9797_1_M2_ALG3);
        assertEquals(Consts.ALG_DES_MAC8_ISO9797_1_M2_ALG3, javacard.security.Signature.ALG_DES_MAC8_ISO9797_1_M2_ALG3);
        assertEquals(Consts.ALG_RSA_SHA_PKCS1_PSS, javacard.security.Signature.ALG_RSA_SHA_PKCS1_PSS);
        assertEquals(Consts.ALG_RSA_MD5_PKCS1_PSS, javacard.security.Signature.ALG_RSA_MD5_PKCS1_PSS);
        assertEquals(Consts.ALG_RSA_RIPEMD160_PKCS1_PSS, javacard.security.Signature.ALG_RSA_RIPEMD160_PKCS1_PSS);
        assertEquals(Consts.ALG_HMAC_SHA1, javacard.security.Signature.ALG_HMAC_SHA1);
        assertEquals(Consts.ALG_HMAC_SHA_256, javacard.security.Signature.ALG_HMAC_SHA_256);
        assertEquals(Consts.ALG_HMAC_SHA_384, javacard.security.Signature.ALG_HMAC_SHA_384);
        assertEquals(Consts.ALG_HMAC_SHA_512, javacard.security.Signature.ALG_HMAC_SHA_512);
        assertEquals(Consts.ALG_HMAC_MD5, javacard.security.Signature.ALG_HMAC_MD5);
        assertEquals(Consts.ALG_HMAC_RIPEMD160, javacard.security.Signature.ALG_HMAC_RIPEMD160);
        assertEquals(Consts.ALG_RSA_SHA_ISO9796_MR, javacard.security.Signature.ALG_RSA_SHA_ISO9796_MR);
        assertEquals(Consts.ALG_RSA_RIPEMD160_ISO9796_MR, javacard.security.Signature.ALG_RSA_RIPEMD160_ISO9796_MR);
        assertEquals(Consts.ALG_KOREAN_SEED_MAC_NOPAD, javacard.security.Signature.ALG_KOREAN_SEED_MAC_NOPAD);
        assertEquals(Consts.ALG_ECDSA_SHA_256, javacard.security.Signature.ALG_ECDSA_SHA_256);
        assertEquals(Consts.ALG_ECDSA_SHA_384, javacard.security.Signature.ALG_ECDSA_SHA_384);
        assertEquals(Consts.ALG_AES_MAC_192_NOPAD, javacard.security.Signature.ALG_AES_MAC_192_NOPAD);
        assertEquals(Consts.ALG_AES_MAC_256_NOPAD, javacard.security.Signature.ALG_AES_MAC_256_NOPAD);
        assertEquals(Consts.ALG_ECDSA_SHA_224, javacard.security.Signature.ALG_ECDSA_SHA_224);
        assertEquals(Consts.ALG_ECDSA_SHA_512, javacard.security.Signature.ALG_ECDSA_SHA_512);
        assertEquals(Consts.ALG_RSA_SHA_224_PKCS1, javacard.security.Signature.ALG_RSA_SHA_224_PKCS1);
        assertEquals(Consts.ALG_RSA_SHA_256_PKCS1, javacard.security.Signature.ALG_RSA_SHA_256_PKCS1);
        assertEquals(Consts.ALG_RSA_SHA_384_PKCS1, javacard.security.Signature.ALG_RSA_SHA_384_PKCS1);
        assertEquals(Consts.ALG_RSA_SHA_512_PKCS1, javacard.security.Signature.ALG_RSA_SHA_512_PKCS1);
        assertEquals(Consts.ALG_RSA_SHA_224_PKCS1_PSS, javacard.security.Signature.ALG_RSA_SHA_224_PKCS1_PSS);
        assertEquals(Consts.ALG_RSA_SHA_256_PKCS1_PSS, javacard.security.Signature.ALG_RSA_SHA_256_PKCS1_PSS);
        assertEquals(Consts.ALG_RSA_SHA_384_PKCS1_PSS, javacard.security.Signature.ALG_RSA_SHA_384_PKCS1_PSS);
        assertEquals(Consts.ALG_RSA_SHA_512_PKCS1_PSS, javacard.security.Signature.ALG_RSA_SHA_512_PKCS1_PSS);
        assertEquals(Consts.ALG_DES_MAC4_ISO9797_1_M1_ALG3, javacard.security.Signature.ALG_DES_MAC4_ISO9797_1_M1_ALG3);
        assertEquals(Consts.ALG_DES_MAC8_ISO9797_1_M1_ALG3, javacard.security.Signature.ALG_DES_MAC8_ISO9797_1_M1_ALG3);
        
        // javacardx.crypto.Cipher.*
        assertEquals(Consts.ALG_DES_CBC_NOPAD, javacardx.crypto.Cipher.ALG_DES_CBC_NOPAD);
        assertEquals(Consts.ALG_DES_CBC_ISO9797_M1, javacardx.crypto.Cipher.ALG_DES_CBC_ISO9797_M1);
        assertEquals(Consts.ALG_DES_CBC_ISO9797_M2, javacardx.crypto.Cipher.ALG_DES_CBC_ISO9797_M2);
        assertEquals(Consts.ALG_DES_CBC_PKCS5, javacardx.crypto.Cipher.ALG_DES_CBC_PKCS5);
        assertEquals(Consts.ALG_DES_ECB_NOPAD, javacardx.crypto.Cipher.ALG_DES_ECB_NOPAD);
        assertEquals(Consts.ALG_DES_ECB_ISO9797_M1, javacardx.crypto.Cipher.ALG_DES_ECB_ISO9797_M1);
        assertEquals(Consts.ALG_DES_ECB_ISO9797_M2, javacardx.crypto.Cipher.ALG_DES_ECB_ISO9797_M2);
        assertEquals(Consts.ALG_DES_ECB_PKCS5, javacardx.crypto.Cipher.ALG_DES_ECB_PKCS5);
        assertEquals(Consts.ALG_RSA_ISO14888, javacardx.crypto.Cipher.ALG_RSA_ISO14888);
        assertEquals(Consts.ALG_RSA_PKCS1, javacardx.crypto.Cipher.ALG_RSA_PKCS1);
        assertEquals(Consts.ALG_RSA_ISO9796, javacardx.crypto.Cipher.ALG_RSA_ISO9796);
        assertEquals(Consts.ALG_RSA_NOPAD, javacardx.crypto.Cipher.ALG_RSA_NOPAD);
        assertEquals(Consts.ALG_AES_BLOCK_128_CBC_NOPAD, javacardx.crypto.Cipher.ALG_AES_BLOCK_128_CBC_NOPAD);
        assertEquals(Consts.ALG_AES_BLOCK_128_ECB_NOPAD, javacardx.crypto.Cipher.ALG_AES_BLOCK_128_ECB_NOPAD);
        assertEquals(Consts.ALG_RSA_PKCS1_OAEP, javacardx.crypto.Cipher.ALG_RSA_PKCS1_OAEP);
        assertEquals(Consts.ALG_KOREAN_SEED_ECB_NOPAD, javacardx.crypto.Cipher.ALG_KOREAN_SEED_ECB_NOPAD);
        assertEquals(Consts.ALG_KOREAN_SEED_CBC_NOPAD, javacardx.crypto.Cipher.ALG_KOREAN_SEED_CBC_NOPAD);
        assertEquals(Consts.ALG_AES_BLOCK_192_CBC_NOPAD, javacardx.crypto.Cipher.ALG_AES_BLOCK_192_CBC_NOPAD);
        assertEquals(Consts.ALG_AES_BLOCK_192_ECB_NOPAD, javacardx.crypto.Cipher.ALG_AES_BLOCK_192_ECB_NOPAD);
        assertEquals(Consts.ALG_AES_BLOCK_256_CBC_NOPAD, javacardx.crypto.Cipher.ALG_AES_BLOCK_256_CBC_NOPAD);
        assertEquals(Consts.ALG_AES_BLOCK_256_ECB_NOPAD, javacardx.crypto.Cipher.ALG_AES_BLOCK_256_ECB_NOPAD);
        assertEquals(Consts.ALG_AES_CBC_ISO9797_M1, javacardx.crypto.Cipher.ALG_AES_CBC_ISO9797_M1);
        assertEquals(Consts.ALG_AES_CBC_ISO9797_M2, javacardx.crypto.Cipher.ALG_AES_CBC_ISO9797_M2);
        assertEquals(Consts.ALG_AES_CBC_PKCS5, javacardx.crypto.Cipher.ALG_AES_CBC_PKCS5);
        assertEquals(Consts.ALG_AES_ECB_ISO9797_M1, javacardx.crypto.Cipher.ALG_AES_ECB_ISO9797_M1);
        assertEquals(Consts.ALG_AES_ECB_ISO9797_M2, javacardx.crypto.Cipher.ALG_AES_ECB_ISO9797_M2);
        assertEquals(Consts.ALG_AES_ECB_PKCS5, javacardx.crypto.Cipher.ALG_AES_ECB_PKCS5);
        
        //javacard.security.KeyAgreement.*
        assertEquals(Consts.ALG_EC_SVDP_DH, javacard.security.KeyAgreement.ALG_EC_SVDP_DH);
        assertEquals(Consts.ALG_EC_SVDP_DHC, javacard.security.KeyAgreement.ALG_EC_SVDP_DHC);
        assertEquals(Consts.ALG_EC_SVDP_DH_KDF, javacard.security.KeyAgreement.ALG_EC_SVDP_DH_KDF);
        assertEquals(Consts.ALG_EC_SVDP_DH_PLAIN, javacard.security.KeyAgreement.ALG_EC_SVDP_DH_PLAIN);
        assertEquals(Consts.ALG_EC_SVDP_DHC_KDF, javacard.security.KeyAgreement.ALG_EC_SVDP_DHC_KDF);
        assertEquals(Consts.ALG_EC_SVDP_DHC_PLAIN, javacard.security.KeyAgreement.ALG_EC_SVDP_DHC_PLAIN);        

        //javacard.security.KeyAgreement.*
        assertEquals(Consts.TYPE_DES_TRANSIENT_RESET, javacard.security.KeyBuilder.TYPE_DES_TRANSIENT_RESET);
        assertEquals(Consts.TYPE_DES_TRANSIENT_DESELECT, javacard.security.KeyBuilder.TYPE_DES_TRANSIENT_DESELECT);
        assertEquals(Consts.TYPE_DES, javacard.security.KeyBuilder.TYPE_DES);
        assertEquals(Consts.TYPE_RSA_PUBLIC, javacard.security.KeyBuilder.TYPE_RSA_PUBLIC);
        assertEquals(Consts.TYPE_RSA_PRIVATE, javacard.security.KeyBuilder.TYPE_RSA_PRIVATE);
        assertEquals(Consts.TYPE_RSA_CRT_PRIVATE, javacard.security.KeyBuilder.TYPE_RSA_CRT_PRIVATE);
        assertEquals(Consts.TYPE_DSA_PUBLIC, javacard.security.KeyBuilder.TYPE_DSA_PUBLIC);
        assertEquals(Consts.TYPE_DSA_PRIVATE, javacard.security.KeyBuilder.TYPE_DSA_PRIVATE);
        assertEquals(Consts.TYPE_EC_F2M_PUBLIC, javacard.security.KeyBuilder.TYPE_EC_F2M_PUBLIC);
        assertEquals(Consts.TYPE_EC_F2M_PRIVATE, javacard.security.KeyBuilder.TYPE_EC_F2M_PRIVATE);
        assertEquals(Consts.TYPE_EC_FP_PUBLIC, javacard.security.KeyBuilder.TYPE_EC_FP_PUBLIC);
        assertEquals(Consts.TYPE_EC_FP_PRIVATE, javacard.security.KeyBuilder.TYPE_EC_FP_PRIVATE);
        assertEquals(Consts.TYPE_AES_TRANSIENT_RESET, javacard.security.KeyBuilder.TYPE_AES_TRANSIENT_RESET);
        assertEquals(Consts.TYPE_AES_TRANSIENT_DESELECT, javacard.security.KeyBuilder.TYPE_AES_TRANSIENT_DESELECT);
        assertEquals(Consts.TYPE_AES, javacard.security.KeyBuilder.TYPE_AES);
        assertEquals(Consts.TYPE_KOREAN_SEED_TRANSIENT_RESET, javacard.security.KeyBuilder.TYPE_KOREAN_SEED_TRANSIENT_RESET);
        assertEquals(Consts.TYPE_KOREAN_SEED_TRANSIENT_DESELECT, javacard.security.KeyBuilder.TYPE_KOREAN_SEED_TRANSIENT_DESELECT);
        assertEquals(Consts.TYPE_KOREAN_SEED, javacard.security.KeyBuilder.TYPE_KOREAN_SEED);
        assertEquals(Consts.TYPE_HMAC_TRANSIENT_RESET, javacard.security.KeyBuilder.TYPE_HMAC_TRANSIENT_RESET);
        assertEquals(Consts.TYPE_HMAC_TRANSIENT_DESELECT, javacard.security.KeyBuilder.TYPE_HMAC_TRANSIENT_DESELECT);
        assertEquals(Consts.TYPE_HMAC, javacard.security.KeyBuilder.TYPE_HMAC);
        assertEquals(Consts.TYPE_RSA_PRIVATE_TRANSIENT_RESET, javacard.security.KeyBuilder.TYPE_RSA_PRIVATE_TRANSIENT_RESET);
        assertEquals(Consts.TYPE_RSA_PRIVATE_TRANSIENT_DESELECT, javacard.security.KeyBuilder.TYPE_RSA_PRIVATE_TRANSIENT_DESELECT);
        assertEquals(Consts.TYPE_RSA_CRT_PRIVATE_TRANSIENT_RESET, javacard.security.KeyBuilder.TYPE_RSA_CRT_PRIVATE_TRANSIENT_RESET);
        assertEquals(Consts.TYPE_DSA_PRIVATE_TRANSIENT_RESET, javacard.security.KeyBuilder.TYPE_DSA_PRIVATE_TRANSIENT_RESET);
        assertEquals(Consts.TYPE_DSA_PRIVATE_TRANSIENT_DESELECT, javacard.security.KeyBuilder.TYPE_DSA_PRIVATE_TRANSIENT_DESELECT);
        assertEquals(Consts.TYPE_EC_F2M_PRIVATE_TRANSIENT_RESET, javacard.security.KeyBuilder.TYPE_EC_F2M_PRIVATE_TRANSIENT_RESET);
        assertEquals(Consts.TYPE_EC_F2M_PRIVATE_TRANSIENT_DESELECT, javacard.security.KeyBuilder.TYPE_EC_F2M_PRIVATE_TRANSIENT_DESELECT);
        assertEquals(Consts.TYPE_EC_FP_PRIVATE_TRANSIENT_RESET, javacard.security.KeyBuilder.TYPE_EC_FP_PRIVATE_TRANSIENT_RESET);
        assertEquals(Consts.TYPE_EC_FP_PRIVATE_TRANSIENT_DESELECT, javacard.security.KeyBuilder.TYPE_EC_FP_PRIVATE_TRANSIENT_DESELECT);
        assertEquals(Consts.LENGTH_DES, javacard.security.KeyBuilder.LENGTH_DES);
        assertEquals(Consts.LENGTH_DES3_2KEY, javacard.security.KeyBuilder.LENGTH_DES3_2KEY);
        assertEquals(Consts.LENGTH_DES3_3KEY, javacard.security.KeyBuilder.LENGTH_DES3_3KEY);
        assertEquals(Consts.LENGTH_RSA_512, javacard.security.KeyBuilder.LENGTH_RSA_512);
        assertEquals(Consts.LENGTH_RSA_736, javacard.security.KeyBuilder.LENGTH_RSA_736);
        assertEquals(Consts.LENGTH_RSA_768, javacard.security.KeyBuilder.LENGTH_RSA_768);
        assertEquals(Consts.LENGTH_RSA_896, javacard.security.KeyBuilder.LENGTH_RSA_896);
        assertEquals(Consts.LENGTH_RSA_1024, javacard.security.KeyBuilder.LENGTH_RSA_1024);
        assertEquals(Consts.LENGTH_RSA_1280, javacard.security.KeyBuilder.LENGTH_RSA_1280);
        assertEquals(Consts.LENGTH_RSA_1536, javacard.security.KeyBuilder.LENGTH_RSA_1536);
        assertEquals(Consts.LENGTH_RSA_1984, javacard.security.KeyBuilder.LENGTH_RSA_1984);
        assertEquals(Consts.LENGTH_RSA_2048, javacard.security.KeyBuilder.LENGTH_RSA_2048);
        assertEquals(Consts.LENGTH_RSA_4096, javacard.security.KeyBuilder.LENGTH_RSA_4096);
        assertEquals(Consts.LENGTH_DSA_512, javacard.security.KeyBuilder.LENGTH_DSA_512);
        assertEquals(Consts.LENGTH_DSA_768, javacard.security.KeyBuilder.LENGTH_DSA_768);
        assertEquals(Consts.LENGTH_DSA_1024, javacard.security.KeyBuilder.LENGTH_DSA_1024);
        assertEquals(Consts.LENGTH_EC_FP_112, javacard.security.KeyBuilder.LENGTH_EC_FP_112);
        assertEquals(Consts.LENGTH_EC_F2M_113, javacard.security.KeyBuilder.LENGTH_EC_F2M_113);
        assertEquals(Consts.LENGTH_EC_FP_128, javacard.security.KeyBuilder.LENGTH_EC_FP_128);
        assertEquals(Consts.LENGTH_EC_F2M_131, javacard.security.KeyBuilder.LENGTH_EC_F2M_131);
        assertEquals(Consts.LENGTH_EC_FP_160, javacard.security.KeyBuilder.LENGTH_EC_FP_160);
        assertEquals(Consts.LENGTH_EC_F2M_163, javacard.security.KeyBuilder.LENGTH_EC_F2M_163);
        assertEquals(Consts.LENGTH_EC_FP_192, javacard.security.KeyBuilder.LENGTH_EC_FP_192);
        assertEquals(Consts.LENGTH_EC_F2M_193, javacard.security.KeyBuilder.LENGTH_EC_F2M_193);
        assertEquals(Consts.LENGTH_EC_FP_224, javacard.security.KeyBuilder.LENGTH_EC_FP_224);
        assertEquals(Consts.LENGTH_EC_FP_256, javacard.security.KeyBuilder.LENGTH_EC_FP_256);
        assertEquals(Consts.LENGTH_EC_FP_384, javacard.security.KeyBuilder.LENGTH_EC_FP_384);
        assertEquals(Consts.LENGTH_EC_FP_521, javacard.security.KeyBuilder.LENGTH_EC_FP_521);
        assertEquals(Consts.LENGTH_AES_128, javacard.security.KeyBuilder.LENGTH_AES_128);
        assertEquals(Consts.LENGTH_AES_192, javacard.security.KeyBuilder.LENGTH_AES_192);
        assertEquals(Consts.LENGTH_AES_256, javacard.security.KeyBuilder.LENGTH_AES_256);
        assertEquals(Consts.LENGTH_KOREAN_SEED_128, javacard.security.KeyBuilder.LENGTH_KOREAN_SEED_128);
        assertEquals(Consts.LENGTH_HMAC_SHA_1_BLOCK_64, javacard.security.KeyBuilder.LENGTH_HMAC_SHA_1_BLOCK_64);
        assertEquals(Consts.LENGTH_HMAC_SHA_256_BLOCK_64, javacard.security.KeyBuilder.LENGTH_HMAC_SHA_256_BLOCK_64);
        assertEquals(Consts.LENGTH_HMAC_SHA_384_BLOCK_128, javacard.security.KeyBuilder.LENGTH_HMAC_SHA_384_BLOCK_128);
        assertEquals(Consts.LENGTH_HMAC_SHA_512_BLOCK_128, javacard.security.KeyBuilder.LENGTH_HMAC_SHA_512_BLOCK_128);      
        
        // javacard.security.KeyPair.*
        assertEquals(Consts.ALG_RSA, javacard.security.KeyPair.ALG_RSA);
        assertEquals(Consts.ALG_RSA_CRT, javacard.security.KeyPair.ALG_RSA_CRT);
        assertEquals(Consts.ALG_DSA, javacard.security.KeyPair.ALG_DSA);
        assertEquals(Consts.ALG_EC_F2M, javacard.security.KeyPair.ALG_EC_F2M);
        assertEquals(Consts.ALG_EC_FP, javacard.security.KeyPair.ALG_EC_FP);
        
        // javacard.security.MessageDigest.*
        assertEquals(Consts.ALG_SHA, javacard.security.MessageDigest.ALG_SHA);
        assertEquals(Consts.ALG_MD5, javacard.security.MessageDigest.ALG_MD5);
        assertEquals(Consts.ALG_RIPEMD160, javacard.security.MessageDigest.ALG_RIPEMD160);
        assertEquals(Consts.ALG_SHA_256, javacard.security.MessageDigest.ALG_SHA_256);
        assertEquals(Consts.ALG_SHA_384, javacard.security.MessageDigest.ALG_SHA_384);
        assertEquals(Consts.ALG_SHA_512, javacard.security.MessageDigest.ALG_SHA_512);
        assertEquals(Consts.ALG_SHA_224, javacard.security.MessageDigest.ALG_SHA_224);        
        
        //Class javacard.security.RandomData.*
        assertEquals(Consts.ALG_PSEUDO_RANDOM, javacard.security.RandomData.ALG_PSEUDO_RANDOM);        
        assertEquals(Consts.ALG_SECURE_RANDOM, javacard.security.RandomData.ALG_SECURE_RANDOM);        

        // Class javacard.security.Checksum.*
        assertEquals(Consts.ALG_ISO3309_CRC16, javacard.security.Checksum.ALG_ISO3309_CRC16);        
        assertEquals(Consts.ALG_ISO3309_CRC32, javacard.security.Checksum.ALG_ISO3309_CRC32);        
*/
    }
    
    
}
