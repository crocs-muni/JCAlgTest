/*
    Copyright (c) 2004-2014  Petr Svenda <petr@svenda.com>

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
*/

/**
 *
 * @author Petr Svenda, Lenka Kunikova, Lukas Srom
 */
package AlgTest;

import javacard.framework.*;
import javacard.security.*;
import javacardx.crypto.*;

 public class AlgPerformanceTest {
    //
    // Performance testing
    //
    final static byte SUPP_ALG_UNTOUCHED = (byte) 0xf0;
    final static byte SUPP_ALG_SUPPORTED = (byte) 0x00;
    final static byte SUPP_ALG_EXCEPTION_CODE_OFFSET = (byte) 0;
    final static byte SUCCESS =                    (byte) 0xAA;

    public final static short SW_ALG_TYPE_NOT_SUPPORTED    = (short) 0x6001;
    public final static short SW_ALG_OPS_NOT_SUPPORTED     = (short) 0x6002;
    public final static short SW_ALG_TYPE_UNKNOWN          = (short) 0x6003;
    
    public final static short RAM1_ARRAY_LENGTH = (short) 600;
    public final static short RAM2_ARRAY_LENGTH = (short) 16;
    
    
    TestSettings    m_testSettings = null;
    
    MessageDigest    m_digest = null;
    RandomData       m_random = null;
    KeyPair          m_keyPair1 = null;
    KeyPair          m_keyPair2 = null;
    Checksum         m_checksum = null;
    KeyAgreement     m_keyAgreement = null;   
    RandomData       m_trng = null; 
    // class Key 
    AESKey              m_aes_key = null;
    DESKey              m_des_key = null;
///*  // comment out for JC2.2.1 convert  
    KoreanSEEDKey       m_koreanseed_key = null; 
    KoreanSEEDKey       m_koreanseed_key2 = null;
    HMACKey             m_hmac_key = null; 
    HMACKey             m_hmac_key2 = null; 
/**/    
    DSAKey              m_dsa_key = null;
    DSAPrivateKey       m_dsaprivate_key = null;
    DSAPublicKey        m_dsapublic_key = null;
    ECKey               m_ex_key = null;
    ECPrivateKey        m_ecprivate_key = null;
    ECPublicKey         m_ecpublic_key = null;
    RSAPrivateCrtKey    m_rsaprivatecrt_key = null;
    RSAPrivateKey       m_rsaprivate_key = null;
    RSAPublicKey        m_rsapublic_key = null;
    AESKey              m_aes_key2 = null;
    DESKey              m_des_key2 = null;
    DSAKey              m_dsa_key2 = null;
    DSAPrivateKey       m_dsaprivate_key2 = null;
    DSAPublicKey        m_dsapublic_key2 = null;
    ECKey               m_ex_key2 = null;
    ECPrivateKey        m_ecprivate_key2 = null;
    ECPublicKey         m_ecpublic_key2 = null;
    RSAPrivateCrtKey    m_rsaprivatecrt_key2 = null;
    RSAPrivateKey       m_rsaprivate_key2 = null;
    RSAPublicKey        m_rsapublic_key2 = null;
    Key                 m_key1 = null;
    Key                 m_key2 = null;
    PrivateKey          m_privateKey = null;
    PublicKey           m_publicKey = null;
    
    Cipher              m_cipher = null;
    Signature           m_signatureSign = null;
    Signature           m_signatureVerify = null;
    byte[]              m_ram1 = null;
    byte[]              m_ram2 = null;
    byte[]              m_eeprom1 = null;
    

    // Objects for various software implementation of algorithms
    Cipher              m_swAlgsEncCipher1 = null;
    Cipher              m_swAlgsDecCipher1 = null;
    Cipher              m_swAlgsEncCipher2 = null;
    Cipher              m_swAlgsDecCipher2 = null;
    Signature           m_swAlgsSignSignature1 = null;
    Signature           m_swAlgsVerifySignature1 = null;
    Signature           m_swAlgsSignSignature2 = null;
    Signature           m_swAlgsVerifySignature2 = null;
    
    AESKey              m_swAlgsKey1 = null;
    AESKey              m_swAlgsKey2 = null;
    AESKey              m_swAlgsKey3 = null;
    AESKey              m_swAlgsKey4 = null;
    AESKey              m_swAlgsKey5 = null;
    AESKey              m_swAlgsKey6 = null;
    AESKey              m_swAlgsKey7 = null;
    AESKey              m_swAlgsKey8 = null;
    
    JavaCardAES         m_aesCipher = null;    

    AlgPerformanceTest() {
        m_testSettings = new TestSettings();
        
        m_ram1 = JCSystem.makeTransientByteArray(RAM1_ARRAY_LENGTH, JCSystem.CLEAR_ON_RESET);
        m_ram2 = JCSystem.makeTransientByteArray(RAM2_ARRAY_LENGTH, JCSystem.CLEAR_ON_RESET);    
        m_eeprom1 = new byte[RAM1_ARRAY_LENGTH];
        
        m_trng = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
        
        m_aesCipher = new JavaCardAES();    // aes software cipher
    }

    public byte process(APDU apdu) throws ISOException {
        byte bProcessed = (byte) 0;
        byte[] apduBuffer = apdu.getBuffer();

        if (apduBuffer[ISO7816.OFFSET_CLA] == Consts.CLA_CARD_ALGTEST) {
            bProcessed = (byte) 1;
            switch ( apduBuffer[ISO7816.OFFSET_INS]) {
                case Consts.INS_PREPARE_TEST_CLASS_KEY: prepare_class_Key(apdu); break;        
                case Consts.INS_PREPARE_TEST_CLASS_CIPHER: prepare_class_Cipher(apdu);break;
                case Consts.INS_PREPARE_TEST_CLASS_SIGNATURE: prepare_class_Signature(apdu);break;
                case Consts.INS_PREPARE_TEST_CLASS_RANDOMDATA: prepare_class_RandomData(apdu);break;
                case Consts.INS_PREPARE_TEST_CLASS_MESSAGEDIGEST: prepare_class_MessageDigest(apdu);break;
                case Consts.INS_PREPARE_TEST_CLASS_CHECKSUM: prepare_class_Checksum(apdu);break;
                case Consts.INS_PREPARE_TEST_CLASS_KEYPAIR: prepare_class_KeyPair(apdu);break;
                case Consts.INS_PREPARE_TEST_CLASS_KEYAGREEMENT: prepare_class_KeyAgreement(apdu);break;
                case Consts.INS_PREPARE_TEST_CLASS_UTIL: prepare_class_Util(apdu);break;

                case Consts.INS_PREPARE_TEST_SWALG_HOTP: prepare_swalg_HOTP(apdu); break;
                case Consts.INS_PREPARE_TEST_SWALGS: prepare_swalgs(apdu); break;

        
                case Consts.INS_PERF_TEST_CLASS_KEY: perftest_class_Key(apdu); break;        
                case Consts.INS_PERF_TEST_CLASS_CIPHER: perftest_class_Cipher(apdu); break;        
                case Consts.INS_PERF_TEST_CLASS_SIGNATURE: perftest_class_Signature(apdu); break;        
                case Consts.INS_PERF_TEST_CLASS_RANDOMDATA: perftest_class_RandomData(apdu); break;        
                case Consts.INS_PERF_TEST_CLASS_MESSAGEDIGEST: perftest_class_MessageDigest(apdu); break;        
                case Consts.INS_PERF_TEST_CLASS_CHECKSUM: perftest_class_Checksum(apdu); break;        
                case Consts.INS_PERF_TEST_CLASS_KEYAGREEMENT: perftest_class_KeyAgreement(apdu); break;        
                case Consts.INS_PERF_TEST_CLASS_KEYPAIR: perftest_class_KeyPair(apdu); break;        
                case Consts.INS_PERF_TEST_CLASS_UTIL: perftest_class_Util(apdu);break;
                 
                case Consts.INS_PERF_TEST_CLASS_CIPHER_SETKEYINITDOFINAL: perftest_class_Cipher_setKeyInitDoFinal(apdu); break;
                case Consts.INS_PERF_TEST_CLASS_SIGNATURE_SETKEYINITSIGN: perftest_class_Signature_setKeyInitSign(apdu); break;
                case Consts.INS_PERF_TEST_SWALG_HOTP: perftest_swalg_HOTP(apdu); break;
                case Consts.INS_PERF_TEST_SWALGS: perftest_swalgs(apdu); break;
                    
                default : {
                    // The INS code is not supported by the dispatcher
                    bProcessed = (byte) 0;
                    break;
                }
            }
        }
        
        return bProcessed;
    }

    void prepare_class_Util(APDU apdu) {
        byte[] apdubuf = apdu.getBuffer();
        m_testSettings.parse(apdu);  
        short offset = ISO7816.OFFSET_CDATA;
        
        short chunkDataLen = (short) (m_testSettings.dataLength1 / m_testSettings.numRepeatSubOperation);
        
        switch (m_testSettings.algorithmMethod) {
            case JCConsts.Util_arrayCopy_RAM: 
            case JCConsts.Util_arrayCopy_EEPROM: 
            case JCConsts.Util_arrayCopy_RAM2EEPROM: 
            case JCConsts.Util_arrayCopy_EEPROM2RAM: 
            case JCConsts.Util_arrayCopyNonAtomic_RAM: 
            case JCConsts.Util_arrayCopyNonAtomic_EEPROM: 
            case JCConsts.Util_arrayCopyNonAtomic_RAM2EEPROM: 
            case JCConsts.Util_arrayCopyNonAtomic_EEPROM2RAM: 
            case JCConsts.Util_arrayFillNonAtomic_RAM: 
            case JCConsts.Util_arrayFillNonAtomic_EEPROM: {
                m_trng.generateData(m_ram1, (short) 0, (short) (2 * chunkDataLen)); 
                m_trng.generateData(m_eeprom1, (short) 0, (short) (2 * chunkDataLen)); 
                break;
            }

            case JCConsts.Util_arrayCompare_RAM: {
                m_trng.generateData(m_ram1, (short) 0, chunkDataLen); 
                Util.arrayCopyNonAtomic(m_ram1, (short) 0, m_ram1, chunkDataLen, chunkDataLen);    // prepare same second part to measure full operation
            }
            case JCConsts.Util_arrayCompare_EEPROM: {
                m_trng.generateData(m_eeprom1, (short) 0, chunkDataLen); 
                Util.arrayCopyNonAtomic(m_eeprom1, (short) 0, m_eeprom1, chunkDataLen, chunkDataLen);    // prepare same second part to measure full operation
                break;
            }
            case JCConsts.Util_arrayCompare_RAM2EEPROM: // no break
            case JCConsts.Util_arrayCompare_EEPROM2RAM: {
                m_trng.generateData(m_ram1, (short) 0, chunkDataLen); 
                Util.arrayCopyNonAtomic(m_ram1, (short) 0, m_eeprom1, (short) 0, chunkDataLen);    // prepare same second part to measure full operation
                break;
            }
            default: ISOException.throwIt(SW_ALG_OPS_NOT_SUPPORTED);
        }
    
        apdubuf[offset] = SUCCESS;
        apdu.setOutgoingAndSend(offset, (byte)1);
    }

   void perftest_class_Util(APDU apdu) {  
        byte[] apdubuf = apdu.getBuffer();
        m_testSettings.parse(apdu);
        
        short chunkDataLen = (short) (m_testSettings.dataLength1 / m_testSettings.numRepeatSubOperation);

        switch (m_testSettings.algorithmMethod) {
            case JCConsts.Util_arrayCopy_RAM: {
                for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { 
                    Util.arrayCopy(m_ram1, (short) 0, m_ram1, chunkDataLen, chunkDataLen);   
                }
                break;
            }
            case JCConsts.Util_arrayCopy_EEPROM: {
                for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { 
                    Util.arrayCopy(m_eeprom1, (short) 0, m_eeprom1, chunkDataLen, chunkDataLen);  
                }
                break;
            }
            case JCConsts.Util_arrayCopy_RAM2EEPROM: {
                for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { 
                    Util.arrayCopy(m_ram1, (short) 0, m_eeprom1, (short) 0, chunkDataLen);  
                }
                break;
            }
            case JCConsts.Util_arrayCopy_EEPROM2RAM: {
                for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { 
                    Util.arrayCopy(m_eeprom1, (short) 0, m_ram1, (short) 0, chunkDataLen);  
                }
                break;
            }
            case JCConsts.Util_arrayCopyNonAtomic_RAM: {
                for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { 
                    Util.arrayCopyNonAtomic(m_ram1, (short) 0, m_ram1, chunkDataLen, chunkDataLen);   
                }
                break;
            }
            case JCConsts.Util_arrayCopyNonAtomic_EEPROM: {
                for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { 
                    Util.arrayCopyNonAtomic(m_eeprom1, (short) 0, m_eeprom1, chunkDataLen, chunkDataLen);  
                }
                break;
            }
            case JCConsts.Util_arrayCopyNonAtomic_RAM2EEPROM: {
                for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { 
                    Util.arrayCopyNonAtomic(m_ram1, (short) 0, m_eeprom1, (short) 0, chunkDataLen);  
                }
                break;
            }
            case JCConsts.Util_arrayCopyNonAtomic_EEPROM2RAM: {
                for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { 
                    Util.arrayCopyNonAtomic(m_eeprom1, (short) 0, m_ram1, (short) 0, chunkDataLen);  
                }
                break;
            }
           case JCConsts.Util_arrayFillNonAtomic_RAM: {
                for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { 
                    Util.arrayFillNonAtomic(m_ram1, (short) 0, chunkDataLen, (byte) 0x55);   
                }
                break;
            }
            case JCConsts.Util_arrayFillNonAtomic_EEPROM: {
                for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { 
                    Util.arrayFillNonAtomic(m_eeprom1, (short) 0, chunkDataLen, (byte) 0x55);  
                }
                break;
            }
            case JCConsts.Util_arrayCompare_RAM: {
                for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { 
                    Util.arrayCopy(m_ram1, (short) 0, m_ram1, chunkDataLen, chunkDataLen);   
                }
                break;
            }
            case JCConsts.Util_arrayCompare_EEPROM: {
                for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { 
                    Util.arrayCopy(m_eeprom1, (short) 0, m_eeprom1, chunkDataLen, chunkDataLen);  
                }
                break;
            }
            case JCConsts.Util_arrayCompare_RAM2EEPROM: {
                for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { 
                    Util.arrayCopy(m_ram1, (short) 0, m_eeprom1, (short) 0, chunkDataLen);  
                }
                break;
            }
            case JCConsts.Util_arrayCompare_EEPROM2RAM: {
                for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { 
                    Util.arrayCopy(m_eeprom1, (short) 0, m_ram1, (short) 0, chunkDataLen);  
                }
                break;
            }
            default: ISOException.throwIt(SW_ALG_OPS_NOT_SUPPORTED);
        }
                

        apdubuf[ISO7816.OFFSET_CDATA] = SUCCESS;
        apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte) 1);            
    }  
   
   
   
   
    void prepare_class_Key(APDU apdu) {
        //ISOException.throwIt((short) 0x666);
        
        byte[] apdubuf = apdu.getBuffer();
        m_testSettings.parse(apdu);  
        
        short len = prepare_Key(apdu, m_testSettings, Consts.TRUE);
        
        apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, len);                
    }
    short prepare_Key(APDU apdu, TestSettings testSet, byte bSetKeyValue) {
        byte[] apdubuf = apdu.getBuffer();
        short offset = ISO7816.OFFSET_CDATA;
        
        try {
            switch (m_testSettings.keyType) {
                case JCConsts.KeyBuilder_TYPE_AES:
                case JCConsts.KeyBuilder_TYPE_AES_TRANSIENT_RESET:
                case JCConsts.KeyBuilder_TYPE_AES_TRANSIENT_DESELECT:
                    m_aes_key = (AESKey) KeyBuilder.buildKey((byte) m_testSettings.keyType, m_testSettings.keyLength, false);
                    m_aes_key2 = (AESKey) KeyBuilder.buildKey((byte) m_testSettings.keyType, m_testSettings.keyLength, false);
                    if (bSetKeyValue == Consts.TRUE) {  
                        m_aes_key.setKey(m_ram1, (byte) 0); 
                        m_key1 = m_aes_key;
                        m_aes_key2.setKey(m_ram1, (byte) 1); 
                        m_key2 = m_aes_key2;
                    }
                    break;
                    
                case JCConsts.KeyBuilder_TYPE_DES:
                case JCConsts.KeyBuilder_TYPE_DES_TRANSIENT_RESET: 
                case JCConsts.KeyBuilder_TYPE_DES_TRANSIENT_DESELECT: 
                    m_des_key = (DESKey) KeyBuilder.buildKey((byte) m_testSettings.keyType, m_testSettings.keyLength, false);
                    m_des_key2 = (DESKey) KeyBuilder.buildKey((byte) m_testSettings.keyType, m_testSettings.keyLength, false);
                    if (bSetKeyValue == Consts.TRUE) {  
                        m_des_key.setKey(m_ram1, (byte) 0); 
                        m_key1 = m_des_key;
                        m_des_key2.setKey(m_ram1, (byte) 1); 
                        m_key2 = m_des_key2;
                    }                    
                    break;
                case JCConsts.KeyBuilder_TYPE_KOREAN_SEED: 
                case JCConsts.KeyBuilder_TYPE_KOREAN_SEED_TRANSIENT_RESET: 
                case JCConsts.KeyBuilder_TYPE_KOREAN_SEED_TRANSIENT_DESELECT: 
//                    throw new CryptoException(CryptoException.NO_SUCH_ALGORITHM);   // enable for JC 2.2.1
///*                    
                    m_koreanseed_key = (KoreanSEEDKey) KeyBuilder.buildKey((byte) m_testSettings.keyType, m_testSettings.keyLength, false);
                    m_koreanseed_key2 = (KoreanSEEDKey) KeyBuilder.buildKey((byte) m_testSettings.keyType, m_testSettings.keyLength, false);
                    if (bSetKeyValue == Consts.TRUE) {  
                        m_koreanseed_key.setKey(m_ram1, (byte) 0); 
                        m_key1 = m_koreanseed_key;
                        m_koreanseed_key2.setKey(m_ram1, (byte) 1); 
                        m_key2 = m_koreanseed_key2;
                    } 
                    break;
/**/                    
                case JCConsts.KeyBuilder_TYPE_HMAC:
                case JCConsts.KeyBuilder_TYPE_HMAC_TRANSIENT_RESET:
                case JCConsts.KeyBuilder_TYPE_HMAC_TRANSIENT_DESELECT:
//                    throw new CryptoException(CryptoException.NO_SUCH_ALGORITHM);   // enable for JC 2.2.1
///*                    
                    m_hmac_key = (HMACKey) KeyBuilder.buildKey((byte) m_testSettings.keyType, m_testSettings.keyLength, false);
                    m_hmac_key2 = (HMACKey) KeyBuilder.buildKey((byte) m_testSettings.keyType, m_testSettings.keyLength, false);
                    if (bSetKeyValue == Consts.TRUE){
                        m_hmac_key.setKey(m_ram1, (byte) 0, m_testSettings.keyLength);
                        m_key1 = m_hmac_key;
                        m_hmac_key2.setKey(m_ram1, (byte) 1, m_testSettings.keyLength);
                        m_key2 = m_hmac_key2;
                    }
                    break;
/**/                    
                case JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE:
                    if (bSetKeyValue == Consts.TRUE) {
                        m_keyPair1 = new KeyPair(KeyPair.ALG_RSA_CRT, m_testSettings.keyLength);
                        m_keyPair1.genKeyPair();
                        m_key1 = m_keyPair1.getPrivate();
                        m_rsaprivatecrt_key = (RSAPrivateCrtKey) m_keyPair1.getPrivate();
                        m_keyPair2 = new KeyPair(KeyPair.ALG_RSA_CRT, m_testSettings.keyLength);
                        m_keyPair2.genKeyPair();
                        m_key2 = m_keyPair2.getPrivate();
                    }
                    break;
                case JCConsts.KeyBuilder_TYPE_RSA_PRIVATE:
                    if (bSetKeyValue == Consts.TRUE) {
                        m_keyPair1 = new KeyPair(KeyPair.ALG_RSA, m_testSettings.keyLength);
                        m_keyPair1.genKeyPair();
                        m_key1 = m_keyPair1.getPrivate();
                        m_rsaprivate_key = (RSAPrivateKey) m_keyPair1.getPrivate();
                        m_keyPair2 = new KeyPair(KeyPair.ALG_RSA, m_testSettings.keyLength);
                        m_keyPair2.genKeyPair();
                        m_key2 = m_keyPair2.getPrivate();
                    }
                    break;                
                case JCConsts.KeyBuilder_TYPE_RSA_PUBLIC:   
                    if (bSetKeyValue == Consts.TRUE){
                        m_keyPair1 = new KeyPair((byte) m_testSettings.keyClass, m_testSettings.keyLength);
                        m_keyPair1.genKeyPair(); // TODO: use fixed key value to shorten time required for key generation?
                        m_key1 = m_keyPair1.getPublic();                
                        m_rsapublic_key = (RSAPublicKey) m_keyPair1.getPublic();
                        m_keyPair2 = new KeyPair((byte) m_testSettings.keyClass, m_testSettings.keyLength);
                        m_keyPair2.genKeyPair(); // TODO: use fixed key value to shorten time required for key generation?
                        m_key2 = m_keyPair2.getPublic();                
                    }
                    break;
                case JCConsts.KeyBuilder_TYPE_EC_F2M_PRIVATE:
                    if (bSetKeyValue == Consts.TRUE) {
                        m_keyPair1 = new KeyPair(KeyPair.ALG_EC_F2M, m_testSettings.keyLength);
                        m_keyPair1.genKeyPair();
                        m_key1 = m_keyPair1.getPrivate();
                        m_ecprivate_key = (ECPrivateKey) m_keyPair1.getPrivate();
                        m_keyPair2 = new KeyPair(KeyPair.ALG_EC_F2M, m_testSettings.keyLength);
                        m_keyPair2.genKeyPair();
                        m_key2 = m_keyPair2.getPrivate();
                    }
                    break;
                case JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE:
                    if (bSetKeyValue == Consts.TRUE) {
                        m_keyPair1 = new KeyPair(KeyPair.ALG_EC_FP, m_testSettings.keyLength);
                        m_keyPair1.genKeyPair(); // TODO: use fixed key value to shorten time required for key generation?
                        m_key1 = m_keyPair1.getPrivate();                
                        m_ecprivate_key= (ECPrivateKey) m_keyPair1.getPrivate();
                        m_keyPair2 = new KeyPair(KeyPair.ALG_EC_FP, m_testSettings.keyLength);
                        m_keyPair2.genKeyPair(); // TODO: use fixed key value to shorten time required for key generation?
                        m_key2 = m_keyPair2.getPrivate();                
                    }
                    break;
                case JCConsts.KeyBuilder_TYPE_DSA_PRIVATE:
                    if (bSetKeyValue == Consts.TRUE){
                        m_keyPair1 = new KeyPair(KeyPair.ALG_DSA, m_testSettings.keyLength);
                        m_keyPair1.genKeyPair();
                        m_key1 = m_keyPair1.getPrivate();
                        m_dsaprivate_key = (DSAPrivateKey) m_keyPair1.getPrivate();
                        m_keyPair2 = new KeyPair(KeyPair.ALG_DSA, m_testSettings.keyLength);
                        m_keyPair2.genKeyPair();
                        m_key2 = m_keyPair2.getPrivate();
                    }
                    break;
                case JCConsts.KeyBuilder_TYPE_DSA_PUBLIC:
                    if (bSetKeyValue == Consts.TRUE){
                        m_keyPair1 = new KeyPair(KeyPair.ALG_DSA, m_testSettings.keyLength);
                        m_keyPair1.genKeyPair();
                        m_key1 = m_keyPair1.getPrivate();
                        m_dsapublic_key = (DSAPublicKey) m_keyPair1.getPublic();
                        m_keyPair2 = new KeyPair(KeyPair.ALG_DSA, m_testSettings.keyLength);
                        m_keyPair2.genKeyPair();
                        m_key2 = m_keyPair2.getPrivate();
                    }
                    break;
                case JCConsts.KeyBuilder_TYPE_EC_F2M_PUBLIC:
                    if (bSetKeyValue == Consts.TRUE){
                        m_keyPair1 = new KeyPair(KeyPair.ALG_EC_F2M, m_testSettings.keyLength);
                        m_keyPair1.genKeyPair();
                        m_key1 = m_keyPair1.getPublic();
                        m_ecpublic_key = (ECPublicKey) m_keyPair1.getPublic();
                        m_keyPair2 = new KeyPair(KeyPair.ALG_EC_F2M, m_testSettings.keyLength);
                        m_keyPair2.genKeyPair();
                        m_key2 = m_keyPair2.getPublic();
                    }
                    break;
                case JCConsts.KeyBuilder_TYPE_EC_FP_PUBLIC:
                    if (bSetKeyValue == Consts.TRUE){
                        m_keyPair1 = new KeyPair(KeyPair.ALG_EC_FP, m_testSettings.keyLength);
                        m_keyPair1.genKeyPair(); // TODO: use fixed key value to shorten time required for key generation?
                        m_key1 = m_keyPair1.getPublic();                        
                        m_ecpublic_key = (ECPublicKey) m_keyPair1.getPublic();              
                        m_keyPair2 = new KeyPair(KeyPair.ALG_EC_FP, m_testSettings.keyLength);
                        m_keyPair2.genKeyPair(); // TODO: use fixed key value to shorten time required for key generation?
                        m_key2 = m_keyPair2.getPublic();
                    }
                    break;                
                default:
                    ISOException.throwIt(SW_ALG_TYPE_UNKNOWN);
            }

            // If we got here, we were able to sucesfully allocate object
            apdubuf[offset] = SUCCESS; offset++;
            apdubuf[offset] = SUPP_ALG_SUPPORTED; offset++;
        }
        catch (CryptoException e) { 
            apdubuf[offset] = (byte) (e.getReason() + SUPP_ALG_EXCEPTION_CODE_OFFSET); offset++;
            apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte) 1);
        }
        
        return (short) (offset - ISO7816.OFFSET_CDATA);
    }

    void perftest_class_Key(APDU apdu) {
        byte[] apdubuf = apdu.getBuffer();
        m_testSettings.parse(apdu);  

        switch (m_testSettings.keyType) {
            case JCConsts.KeyBuilder_TYPE_AES:
                switch (m_testSettings.algorithmMethod) {
                    case JCConsts.AESKey_setKey: 
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { m_aes_key.setKey(m_ram1, (byte) (i % 10)); } // i % 10 => different offset to ensure slightly different key every time
                        break;
                    case JCConsts.AESKey_clearKey:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) {
                            m_aes_key.setKey(m_ram1, (byte) (i % 10));  // we need to set key before calling clear - postprocessing is on client side is required substract setKey time
                            m_aes_key.clearKey();
                        } 
                        break;
                    case JCConsts.AESKey_getKey:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { m_aes_key.getKey(m_ram1, (short) 0); }
                        break;
                    default:
                        ISOException.throwIt(SW_ALG_OPS_NOT_SUPPORTED);
                }
                break;
                case JCConsts.KeyBuilder_TYPE_DES:
                switch (m_testSettings.algorithmMethod){
                    case JCConsts.DESKey_setKey: 
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { m_des_key.setKey(m_ram1, (byte) (i % 10)); } // i % 10 => different offset to ensure slightly different key every time
                        break;
                    case JCConsts.DESKey_clearKey:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) {
                            m_des_key.setKey(m_ram1, (byte) (i % 10)); // we need to set key before calling clear - postprocessing is on client side is required substract setKey time
                            m_des_key.clearKey();
                        }
                        break;
                    case JCConsts.DESKey_getKey:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { m_des_key.getKey(m_ram1, (short) 0); }
                        break;
                    default:
                        ISOException.throwIt(SW_ALG_OPS_NOT_SUPPORTED);
                    break;
                }
            break;
            
            case JCConsts.KeyBuilder_TYPE_KOREAN_SEED:
//                throw new CryptoException(CryptoException.NO_SUCH_ALGORITHM);   // enable for JC 2.2.1
///*                    
                switch (m_testSettings.algorithmMethod){
                    case JCConsts.KoreanSEEDKey_setKey: 
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { m_koreanseed_key.setKey(m_ram1, (byte) (i % 10)); } // i % 10 => different offset to ensure slightly different key every time
                        break;                    
                    case JCConsts.KoreanSEEDKey_clearKey:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) {
                            m_koreanseed_key.setKey(m_ram1, (byte) (i % 10)); // we need to set key before calling clear - postprocessing is on client side is required substract setKey time
                            m_koreanseed_key.clearKey();
                        } 
                        break;
                    case JCConsts.KoreanSEEDKey_getKey:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { m_koreanseed_key.getKey(m_ram1, (short) 0); }
                        break;
                    default:
                        ISOException.throwIt(SW_ALG_OPS_NOT_SUPPORTED);
                    break;
                }
            break;
/**/                    
                
            case JCConsts.KeyBuilder_TYPE_EC_F2M_PRIVATE:
                switch (m_testSettings.algorithmMethod){
                    case JCConsts.ECPrivateKey_setS:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++){m_ecprivate_key.setS(m_ram1, (byte) (i % 10), m_testSettings.keyLength);}
                        break;
                    case JCConsts.ECPrivateKey_getS:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++){m_ecprivate_key.getS(m_ram1, (short) 0);}
                        break;
                    case JCConsts.ECPrivateKey_clearKey:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) {
                            // Bugbug: we are settings only S
                            m_ecprivate_key.setS(m_ram1, (byte) (i % 10), m_testSettings.keyLength); // we need to set key before calling clear - postprocessing is on client side is required substract setKey time
                            m_ecprivate_key.clearKey();
                        }
                        break;
                    default:
                        ISOException.throwIt(SW_ALG_OPS_NOT_SUPPORTED);
                    break;
                }
            break;
            case JCConsts.KeyBuilder_TYPE_EC_F2M_PUBLIC:
                switch (m_testSettings.algorithmMethod){
                    case JCConsts.ECPublicKey_setW:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++){m_ecpublic_key.setW(m_ram1, (byte) (i % 10), m_testSettings.keyLength);}
                        break;
                    case JCConsts.ECPublicKey_getW:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++){m_ecpublic_key.getW(m_ram1, (short) 0);}
                        break;
                    case JCConsts.ECPublicKey_clearKey:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) {
                            m_ecpublic_key.setW(m_ram1, (byte) (i % 10), m_testSettings.keyLength); // we need to set key before calling clear - postprocessing is on client side is required substract setKey time
                            m_ecpublic_key.clearKey();
                        }
                        break;
                    default:
                        ISOException.throwIt(SW_ALG_OPS_NOT_SUPPORTED);
                    break;
                }
            break;
            case JCConsts.KeyBuilder_TYPE_EC_FP_PRIVATE:
                switch (m_testSettings.algorithmMethod){
                    case JCConsts.ECPrivateKey_setS:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++){m_ecprivate_key.setS(m_ram1, (short) (i %10), m_testSettings.keyLength);}
                        break;
                    case JCConsts.ECPrivateKey_getS:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++){m_ecprivate_key.getS(m_ram1, (short) 0);}
                        break;
                    case JCConsts.ECPrivateKey_clearKey:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) {
                            m_ecprivate_key.setS(m_ram1, (byte) (i % 10), m_testSettings.keyLength); // we need to set key before calling clear - postprocessing is on client side is required substract setKey time
                            m_ecprivate_key.clearKey();
                        }
                        break;
                    default:
                        ISOException.throwIt(SW_ALG_OPS_NOT_SUPPORTED);
                    break;
                }
            break;
            case JCConsts.KeyBuilder_TYPE_EC_FP_PUBLIC:
                switch (m_testSettings.algorithmMethod){
                    case JCConsts.ECPublicKey_setW:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++){m_ecpublic_key.setW(m_ram1, (short) (i %10), m_testSettings.keyLength);}
                        break;
                    case JCConsts.ECPublicKey_getW:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++){m_ecpublic_key.getW(m_ram1, (short) 0);}
                        break;
                    case JCConsts.ECPublicKey_clearKey:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) {
                            m_ecpublic_key.setW(m_ram1, (byte) (i % 10), m_testSettings.keyLength);
                            m_ecpublic_key.clearKey();
                        }
                    break;
                    default:
                        ISOException.throwIt(SW_ALG_OPS_NOT_SUPPORTED);
                    break;
                }
                break;
                
            case JCConsts.KeyBuilder_TYPE_HMAC:
//                throw new CryptoException(CryptoException.NO_SUCH_ALGORITHM);   // enable for JC 2.2.1
///*                    
                switch (m_testSettings.algorithmMethod){
                    case JCConsts.HMACKey_setKey: 
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) {m_hmac_key.setKey(m_ram1, (byte) (i % 10), m_testSettings.keyLength); } // i % 10 => different offset to ensure slightly different key every time
                        break;
                    case JCConsts.HMACKey_getKey:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) {m_hmac_key.getKey(m_ram1, (short) 0); }
                        break;
                    case JCConsts.HMACKey_clearKey:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) {
                            m_hmac_key.setKey(m_ram1, (byte) (i % 10), m_testSettings.keyLength);
                            m_hmac_key.clearKey();
                        }
                        break;
                    default:
                        ISOException.throwIt(SW_ALG_OPS_NOT_SUPPORTED);
                    break;
                }
                break;
/**/                
                
            case JCConsts.KeyBuilder_TYPE_DSA_PRIVATE:
                switch (m_testSettings.algorithmMethod){
                    case JCConsts.DSAPrivateKey_setX: 
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { m_dsaprivate_key.setX(m_ram1, (byte) (i % 10), m_testSettings.keyLength); } // i % 10 => different offset to ensure slightly different key every time
                        break;                    
                    case JCConsts.DSAPrivateKey_getX:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { m_dsaprivate_key.getX(m_ram1, (short) 0); }
                        break;
                    case JCConsts.DSAPrivateKey_clearX:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) {
                            m_dsaprivate_key.setX(m_ram1, (byte) (i % 10), m_testSettings.keyLength);
                            m_dsaprivate_key.clearKey();
                        }
                        break;
                    default:
                        ISOException.throwIt(SW_ALG_OPS_NOT_SUPPORTED);
                        break;
                }
                break;
                
            case JCConsts.KeyBuilder_TYPE_DSA_PUBLIC:
                switch (m_testSettings.algorithmMethod){
                    case JCConsts.DSAPublicKey_setY: 
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { m_dsapublic_key.setY(m_ram1, (byte) (i % 10), m_testSettings.keyLength); } // i % 10 => different offset to ensure slightly different key every time
                        break;                    
                    case JCConsts.DSAPublicKey_getY:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { m_dsapublic_key.getY(m_ram1, (short) 0); }
                        break;
                    case JCConsts.DSAPublicKey_clearY:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) {
                            m_dsapublic_key.setY(m_ram1, (byte) (i % 10), m_testSettings.keyLength);
                            m_dsapublic_key.clearKey();
                        }
                        break;
                    default:
                        ISOException.throwIt(SW_ALG_OPS_NOT_SUPPORTED);
                        break;
                }
                break;
                
            case JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE:                
                switch (m_testSettings.algorithmMethod){
                    case JCConsts.RSAPrivateCrtKey_setDP1: 
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { m_rsaprivatecrt_key.setDP1(m_ram1, (byte) (i % 10), m_testSettings.keyLength); } // i % 10 => different offset to ensure slightly different key every time
                        break;
                    case JCConsts.RSAPrivateCrtKey_setDQ1: 
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { m_rsaprivatecrt_key.setDQ1(m_ram1, (byte) (i % 10), m_testSettings.keyLength); } // i % 10 => different offset to ensure slightly different key every time
                        break;
                    case JCConsts.RSAPrivateCrtKey_setP: 
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { m_rsaprivatecrt_key.setP(m_ram1, (byte) (i % 10), m_testSettings.keyLength); } // i % 10 => different offset to ensure slightly different key every time
                        break;
                    case JCConsts.RSAPrivateCrtKey_setPQ: 
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { m_rsaprivatecrt_key.setPQ(m_ram1, (byte) (i % 10), m_testSettings.keyLength); } // i % 10 => different offset to ensure slightly different key every time
                        break;  
                    case JCConsts.RSAPrivateCrtKey_setQ: 
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { m_rsaprivatecrt_key.setQ(m_ram1, (byte) (i % 10), m_testSettings.keyLength); } // i % 10 => different offset to ensure slightly different key every time
                        break;  
                    case JCConsts.RSAPrivateCrtKey_getDP1:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { m_rsaprivatecrt_key.getDP1(m_ram1, (short) 0); }
                        break;
                    case JCConsts.RSAPrivateCrtKey_getDQ1:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { m_rsaprivatecrt_key.getDQ1(m_ram1, (short) 0); }
                        break;
                    case JCConsts.RSAPrivateCrtKey_getP:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { m_rsaprivatecrt_key.getP(m_ram1, (short) 0); }
                        break;
                    case JCConsts.RSAPrivateCrtKey_getPQ:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { m_rsaprivatecrt_key.getPQ(m_ram1, (short) 0); }
                        break;
                    case JCConsts.RSAPrivateCrtKey_getQ:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { m_rsaprivatecrt_key.getQ(m_ram1, (short) 0); }
                        break;
                    case JCConsts.RSAPrivateCrtKey_clearKey:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) {
                            // BUGBUG: is setting only DP1 enough?
                            m_rsaprivatecrt_key.setDP1(m_ram1, (byte) (i % 10), m_testSettings.keyLength);
                            m_rsaprivatecrt_key.clearKey();
                        }
                    break;
                    default:
                        ISOException.throwIt(SW_ALG_OPS_NOT_SUPPORTED);
                    break;
                }
                        break;
            case JCConsts.KeyBuilder_TYPE_RSA_PRIVATE:
                switch (m_testSettings.algorithmMethod){
                    case JCConsts.RSAPrivateKey_setExponent:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { m_rsaprivate_key.setExponent(m_ram1, (byte) (i % 10), m_testSettings.keyLength);}
                        break;
                    case JCConsts.RSAPrivateKey_setModulus:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++){m_rsaprivate_key.setModulus(m_ram1, (byte) (i % 10), m_testSettings.keyLength);}
                        break;
                    case JCConsts.RSAPrivateKey_getExponent:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++){m_rsaprivate_key.getExponent(m_ram1, (short) 0);}
                        break;
                    case JCConsts.RSAPrivateKey_getModulus:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++){m_rsaprivate_key.getModulus(m_ram1, (short) 0);}
                        break;
                    case JCConsts.RSAPrivateKey_clearKey:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) {
                            m_rsaprivate_key.setModulus(m_ram1, (byte) (i % 10), m_testSettings.keyLength);
                            m_rsaprivate_key.clearKey();}
                        break;
                    default:
                        ISOException.throwIt(SW_ALG_OPS_NOT_SUPPORTED);
                    break;
                }
                break;
            case JCConsts.KeyBuilder_TYPE_RSA_PUBLIC:
                switch (m_testSettings.algorithmMethod){
                    case JCConsts.RSAPublicKey_setExponent:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++){m_rsapublic_key.setExponent(m_ram1, (byte) (i % 10), m_testSettings.keyLength);}
                        break;
                    case JCConsts.RSAPublicKey_setModulus:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++){m_rsapublic_key.setModulus(m_ram1, (byte) (i % 10), m_testSettings.keyLength);}
                        break;
                    case JCConsts.RSAPublicKey_getExponent:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++){m_rsapublic_key.getExponent(m_ram1, (short) 0);}
                        break;
                    case JCConsts.RSAPublicKey_getModulus:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++){m_rsapublic_key.getModulus(m_ram1, (short) 0);}
                        break;
                    case JCConsts.RSAPublicKey_clearKey:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) {
                            m_rsapublic_key.setExponent(m_ram1, (byte) (i % 10), m_testSettings.keyLength);
                            m_rsapublic_key.clearKey();
                        }
                        break;
                    default:
                        ISOException.throwIt(SW_ALG_OPS_NOT_SUPPORTED);
                        break;
                }
                break;
            default:
                ISOException.throwIt(SW_ALG_TYPE_NOT_SUPPORTED);
        }

        apdubuf[ISO7816.OFFSET_CDATA] = SUCCESS;
        apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte) 1);            
   }   
   
    void prepare_class_Cipher(APDU apdu) {
        byte[] apdubuf = apdu.getBuffer();
        m_testSettings.parse(apdu);  
        short offset = ISO7816.OFFSET_CDATA;
        
        // Prepare required key object into m_key
        short len = prepare_Key(apdu, m_testSettings, Consts.TRUE);
        
        try {
            m_cipher = Cipher.getInstance((byte) m_testSettings.algorithmSpecification, false);
            m_cipher.init(m_key1, (byte) m_testSettings.initMode);
            short chunkDataLen = (short) (m_testSettings.dataLength1 / m_testSettings.numRepeatSubOperation);
            m_trng.generateData(m_ram1, (short) 0, chunkDataLen); // fill input with random data
            
            apdubuf[(short) (ISO7816.OFFSET_CDATA)] = SUCCESS;
            apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA,(byte)1);
        }
        catch (CryptoException e) {
            apdubuf[(short) (ISO7816.OFFSET_CDATA)] = (byte) e.getReason();
            apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte)1);
        }  
    }

    void perftest_class_Cipher(APDU apdu) {  
        byte[] apdubuf = apdu.getBuffer();
        m_testSettings.parse(apdu); 
        // Operation is performed either in single call with (dataLength1)
        //   or multiple times (numRepeatSubOperation) on smaller chunks 
        short repeats = (short) (m_testSettings.numRepeatWholeOperation * m_testSettings.numRepeatSubOperation);
        short chunkDataLen = (short) (m_testSettings.dataLength1 / m_testSettings.numRepeatSubOperation);
       
        
        switch (m_testSettings.algorithmMethod) {
            case JCConsts.Cipher_update:  
                for (short i = 0; i < repeats; i++) { 
                    m_ram1[(short) 0] = (byte) 0x00; //Note: for raw RSA, most significant bit must be != 1
                    m_cipher.update(m_ram1, (short) 0, chunkDataLen, m_ram1, (short) 0); 
                } 
                break;
            case JCConsts.Cipher_doFinal: 
                for (short i = 0; i < repeats; i++) { 
                    m_ram1[(short) 0] = (byte) 0x00; //Note: for raw RSA, most significant bit must be != 1
                    m_cipher.doFinal(m_ram1, (short) 0, chunkDataLen, m_ram1, (short) 0); 
                } 
                break;
            case JCConsts.Cipher_init:    
                for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { 
                    m_cipher.init((i % 2 == 0) ? m_key1 : m_key2, (byte) m_testSettings.initMode); // (i % 2 == 0) ? m_key1 : m_key2 alteration between keys for forcing to init with new key
                } 
                break;
            default: ISOException.throwIt(SW_ALG_OPS_NOT_SUPPORTED);
        }
        
        apdubuf[ISO7816.OFFSET_CDATA] = SUCCESS;
        
        apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte) 1);            
    }
    
    /**
     * Performs Cipher.doFinal including setKey and Cipher.init
     * @param apdu 
     */
    void perftest_class_Cipher_setKeyInitDoFinal(APDU apdu) {  
        byte[] apdubuf = apdu.getBuffer();
        m_testSettings.parse(apdu); 

        short repeats = (short) (m_testSettings.numRepeatWholeOperation * m_testSettings.numRepeatSubOperation);
        short chunkDataLen = (short) (m_testSettings.dataLength1 / m_testSettings.numRepeatSubOperation);
        m_ram1[(short) 0] = (byte) 0x00; //Note: for raw RSA, most significant bit must be != 1
        
        switch (m_testSettings.keyType) {
            case JCConsts.KeyBuilder_TYPE_DES:  
                m_cipher.init(m_des_key2, (byte) m_testSettings.initMode); // init with m_des_key2 to enforce first clean init with m_des_key
                for (short i = 0; i < repeats; i++) { 
                    if (i % 2 == 0) {   // initialize new key every time
                        m_des_key.setKey(m_ram1, (byte) 0); 
                        m_cipher.init(m_des_key, (byte) m_testSettings.initMode);
                    }
                    else {
                        m_des_key2.setKey(m_ram1, (byte) 1);
                        m_cipher.init(m_des_key2, (byte) m_testSettings.initMode);
                    } 
                    m_cipher.doFinal(m_ram1, (short) 0, chunkDataLen, m_ram1, (short) 0); 
                } 
                break;
            case JCConsts.KeyBuilder_TYPE_AES:  
                m_cipher.init(m_aes_key2, (byte) m_testSettings.initMode); // init with m_aes_key to enforce first clean init with m_aes_key
                for (short i = 0; i < repeats; i++) { 
                    if (i % 2 == (short) 0) {   // initialize new key every time
                        m_aes_key.setKey(m_ram1, (byte) 0); 
                        m_cipher.init(m_aes_key, (byte) m_testSettings.initMode);
                    }
                    else {
                        m_aes_key2.setKey(m_ram1, (byte) 1);
                        m_cipher.init(m_aes_key2, (byte) m_testSettings.initMode);
                    } 
                    m_cipher.doFinal(m_ram1, (short) 0, chunkDataLen, m_ram1, (short) 0); 
                } 
                break;
            default: ISOException.throwIt(SW_ALG_OPS_NOT_SUPPORTED);
        }
        apdubuf[ISO7816.OFFSET_CDATA] = SUCCESS;
        apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte) 1);            
    }      
    
    void prepare_class_Signature(APDU apdu) {
        byte[] apdubuf = apdu.getBuffer();
        m_testSettings.parse(apdu);  
        short offset = ISO7816.OFFSET_CDATA;
        
        // Prepare required key object into m_key
        short len = prepare_Key(apdu, m_testSettings, Consts.TRUE);
        
        try {
            m_signatureSign = Signature.getInstance((byte) m_testSettings.algorithmSpecification, false);
            m_signatureVerify = Signature.getInstance((byte) m_testSettings.algorithmSpecification, false);
            m_signatureSign.init(m_key1, Signature.MODE_SIGN);
            m_signatureVerify.init(m_key1, Signature.MODE_VERIFY);
            m_trng.generateData(m_ram1, (short) 0, (short) m_ram1.length); // fill input with random data
            
            apdubuf[(short) (ISO7816.OFFSET_CDATA)] = SUCCESS;
            apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte)1);
        }
        catch(CryptoException e)
        {
            apdubuf[(short) (ISO7816.OFFSET_CDATA)] = (byte)e.getReason();
            apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte)1);
        }  
    }    
    void perftest_class_Signature(APDU apdu) {  
        byte[] apdubuf = apdu.getBuffer();
        m_testSettings.parse(apdu); 
        
        // Compute correct cryptogram for later verification
        // Operation is performed either in single call with (dataLength1)
        //   or multiple times (numRepeatSubOperation) on smaller chunks 
        short repeats = (short) (m_testSettings.numRepeatWholeOperation * m_testSettings.numRepeatSubOperation);
        short chunkDataLen = (short) (m_testSettings.dataLength1 / m_testSettings.numRepeatSubOperation);
        switch (m_testSettings.algorithmMethod) {
            case JCConsts.Signature_update:   for (short i = 0; i < repeats; i++) { m_signatureSign.update(m_ram1, (short) 0, chunkDataLen); } break;
            case JCConsts.Signature_sign:     for (short i = 0; i < repeats; i++) { m_signatureSign.sign(m_ram1, (short) 0, chunkDataLen, m_ram1, (short) chunkDataLen); } break;
            case JCConsts.Signature_verify:   
                // Compute valid signature once (used later for verification)
                m_signatureSign.sign(m_ram1, (short) 0, chunkDataLen, m_ram1, (short) chunkDataLen);                 
                for (short i = 0; i < repeats; i++) { m_signatureVerify.verify(m_ram1, (short) 0, chunkDataLen, m_ram1, (short) chunkDataLen, m_signatureSign.getLength()); } 
                break;
            case JCConsts.Signature_init:     
                for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { 
                    m_signatureSign.init((i % 2 == 0) ? m_key1 : m_key2, Signature.MODE_SIGN); // (i % 2 == 0) ? m_key1 : m_key2 alteration between keys for forcing to init with new key
                } 
                break;
/* JC 3.0.1                        
            case Consts.Signature_signPreComputedHash: for (short i = 0; i < repeats; i++) { m_signature.signPreComputedHash(m_ram1, (short) 0, chunkDataLen); } break;
            case Consts.Signature_setInitialDigest: for (short i = 0; i < repeats; i++) { m_signature.setInitialDigest(m_ram1, (short) 0, chunkDataLen); } break;
*/        
            default: ISOException.throwIt(SW_ALG_OPS_NOT_SUPPORTED);
        }

        apdubuf[ISO7816.OFFSET_CDATA] = SUCCESS;
        apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte) 1);            
    }    
    
    /**
     * Performs Signature.doFinal including setKey and Signature.init
     * @param apdu 
     */
    void perftest_class_Signature_setKeyInitSign(APDU apdu) {  
        byte[] apdubuf = apdu.getBuffer();
        m_testSettings.parse(apdu); 

        short repeats = (short) (m_testSettings.numRepeatWholeOperation * m_testSettings.numRepeatSubOperation);
        short chunkDataLen = (short) (m_testSettings.dataLength1 / m_testSettings.numRepeatSubOperation);
        //m_trng.generateData(m_ram1, (short) 0, chunkDataLen); // fill input with random data
        
        switch (m_testSettings.keyType) {
            case JCConsts.KeyBuilder_TYPE_DES:  
                for (short i = 0; i < repeats; i++) { 
                    if (i % 2 == (short) 0) {   // initialize new key every time
                        m_des_key.setKey(m_ram1, (byte) 0); 
                        m_signatureSign.init(m_des_key, Signature.MODE_SIGN);
                    }
                    else {
                        m_des_key2.setKey(m_ram1, (byte) 1);
                        m_signatureSign.init(m_des_key2, (byte) Signature.MODE_SIGN);
                    } 
                    m_signatureSign.sign(m_ram1, (short) 0, chunkDataLen, m_ram1, (short) chunkDataLen);  
                } 
                break;
            case JCConsts.KeyBuilder_TYPE_AES:  
                for (short i = 0; i < repeats; i++) { 
                    if (i % 2 == (short) 0) {   // initialize new key every time
                        m_aes_key.setKey(m_ram1, (byte) 0); 
                        m_signatureSign.init(m_aes_key, Signature.MODE_SIGN);
                    }
                    else {
                        m_aes_key2.setKey(m_ram1, (byte) 1);
                        m_signatureSign.init(m_aes_key2, Signature.MODE_SIGN);
                    } 
                    m_signatureSign.sign(m_ram1, (short) 0, chunkDataLen, m_ram1, (short) chunkDataLen);   
                } 
                break;
            default: ISOException.throwIt(SW_ALG_OPS_NOT_SUPPORTED);
        }
        apdubuf[ISO7816.OFFSET_CDATA] = SUCCESS;
        apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte) 1);            
    }       
    
    void prepare_class_RandomData(APDU apdu) {
        byte[] apdubuf = apdu.getBuffer();
        m_testSettings.parse(apdu);  

        try {
            m_random = RandomData.getInstance((byte) m_testSettings.algorithmSpecification);
            apdubuf[(short) (ISO7816.OFFSET_CDATA)] = SUCCESS;
            apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte)1);
        }
        catch(CryptoException e)
        {
            apdubuf[(short) (ISO7816.OFFSET_CDATA)] = (byte)e.getReason();
            apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte)1);
        }  
    }      
    void perftest_class_RandomData(APDU apdu) {  
        byte[] apdubuf = apdu.getBuffer();
        m_testSettings.parse(apdu); 
        short repeats = (short) (m_testSettings.numRepeatWholeOperation * m_testSettings.numRepeatSubOperation);
        short chunkDataLen = (short) (m_testSettings.dataLength1 / m_testSettings.numRepeatSubOperation);

        switch (m_testSettings.algorithmMethod) {
            case JCConsts.RandomData_generateData:for (short i = 0; i < repeats; i++) { m_random.generateData(m_ram1, (short) 0, chunkDataLen); } break;
            case JCConsts.RandomData_setSeed:     for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { m_random.setSeed(m_ram1, (short) 0,m_testSettings.dataLength1); } break;
    
            default: ISOException.throwIt(SW_ALG_OPS_NOT_SUPPORTED);
        }

        apdubuf[ISO7816.OFFSET_CDATA] = SUCCESS;
        apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte) 1);            
    }    
    
    void prepare_class_MessageDigest(APDU apdu) {
        byte[] apdubuf = apdu.getBuffer();
        m_testSettings.parse(apdu);  

        try {
            m_digest = MessageDigest.getInstance((byte) m_testSettings.algorithmSpecification, false);
            apdubuf[(short) (ISO7816.OFFSET_CDATA)] = SUCCESS;
            apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte)1);
        }
        catch(CryptoException e)
        {
            apdubuf[(short) (ISO7816.OFFSET_CDATA)] = (byte)e.getReason();
            apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte)1);
        }  
    }      
    void perftest_class_MessageDigest(APDU apdu) {  
        byte[] apdubuf = apdu.getBuffer();
        m_testSettings.parse(apdu); 
        short repeats = (short) (m_testSettings.numRepeatWholeOperation * m_testSettings.numRepeatSubOperation);
        short chunkDataLen = (short) (m_testSettings.dataLength1 / m_testSettings.numRepeatSubOperation);

        switch (m_testSettings.algorithmMethod) {
            case JCConsts.MessageDigest_update:   for (short i = 0; i < repeats; i++) { m_digest.update(m_ram1, (short) 0, chunkDataLen); } break;
            case JCConsts.MessageDigest_doFinal:  for (short i = 0; i < repeats; i++) { m_digest.doFinal(m_ram1, (short) 0, chunkDataLen, m_ram1, chunkDataLen); } break;
            case JCConsts.MessageDigest_reset:  
                for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { 
                    m_digest.doFinal(m_ram1, (short) 0, chunkDataLen, m_ram1, chunkDataLen);    // NOTE: time substraction needed
                    m_digest.reset(); 
                } 
                break; 
    
            default: ISOException.throwIt(SW_ALG_OPS_NOT_SUPPORTED);
        }

        apdubuf[ISO7816.OFFSET_CDATA] = SUCCESS;
        apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte) 1);            
    }    
    
    void prepare_class_Checksum(APDU apdu) {
        byte[] apdubuf = apdu.getBuffer();
        m_testSettings.parse(apdu);  

        try {
            m_checksum = Checksum.getInstance((byte) m_testSettings.algorithmSpecification, false);
            apdubuf[(short) (ISO7816.OFFSET_CDATA)] = SUCCESS;
            apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte)1);
        }
        catch(CryptoException e) {
            apdubuf[(short) (ISO7816.OFFSET_CDATA)] = (byte)e.getReason(); 
            apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte)1);
        }  
    }      
    void perftest_class_Checksum(APDU apdu) {  
        byte[] apdubuf = apdu.getBuffer();
        m_testSettings.parse(apdu); 
        short repeats = (short) (m_testSettings.numRepeatWholeOperation * m_testSettings.numRepeatSubOperation);
        short chunkDataLen = (short) (m_testSettings.dataLength1 / m_testSettings.numRepeatSubOperation);

        switch (m_testSettings.algorithmMethod) {
            case JCConsts.Checksum_update:   for (short i = 0; i < repeats; i++) { m_checksum.update(m_ram1, (short) 0, chunkDataLen); } break;
            case JCConsts.Checksum_doFinal:  for (short i = 0; i < repeats; i++) { m_checksum.doFinal(m_ram1, (short) 0, chunkDataLen, m_ram1, chunkDataLen); } break;
    
            default: ISOException.throwIt(SW_ALG_OPS_NOT_SUPPORTED);
        }

        apdubuf[ISO7816.OFFSET_CDATA] = SUCCESS;
        apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte) 1);            
    }   
    
    void prepare_class_KeyPair(APDU apdu) {
        byte[] apdubuf = apdu.getBuffer();
        m_testSettings.parse(apdu);  
        
        try {
            m_keyPair1 = new KeyPair((byte) m_testSettings.keyClass, m_testSettings.keyLength);
            apdubuf[(short) (ISO7816.OFFSET_CDATA)] = SUCCESS;
            apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte)1);
        }
        catch(CryptoException e) {
            apdubuf[(short) (ISO7816.OFFSET_CDATA)] = (byte)e.getReason(); 
            apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte)1);
        }  
    }      
    void perftest_class_KeyPair(APDU apdu) {  
        byte[] apdubuf = apdu.getBuffer();
        m_testSettings.parse(apdu); 

        switch (m_testSettings.algorithmMethod) {
            case JCConsts.KeyPair_genKeyPair:   for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { m_keyPair1.genKeyPair(); } break;
    
            default: ISOException.throwIt(SW_ALG_OPS_NOT_SUPPORTED);
        }

        apdubuf[ISO7816.OFFSET_CDATA] = SUCCESS;
        apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte) 1);            
    }            
    
    void prepare_class_KeyAgreement(APDU apdu) {
        byte[] apdubuf = apdu.getBuffer();
        m_testSettings.parse(apdu);  

        // Prepare required key object into m_key
        short len = prepare_Key(apdu, m_testSettings, Consts.TRUE);
        
        try {
            m_keyAgreement = KeyAgreement.getInstance((byte) m_testSettings.algorithmSpecification, false);
            apdubuf[(short) (ISO7816.OFFSET_CDATA)] = SUCCESS;
            apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte)1);
        }
        catch(CryptoException e) {
            apdubuf[(short) (ISO7816.OFFSET_CDATA)] = (byte)e.getReason(); 
            apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte)1);
        }  
    }      
    void perftest_class_KeyAgreement(APDU apdu) {  
        byte[] apdubuf = apdu.getBuffer();
        m_testSettings.parse(apdu); 

        switch (m_testSettings.algorithmMethod) {
            case JCConsts.KeyAgreement_init:   
                for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { 
                    // BUGBUG: add key alteration (m_privateKey1 and m_privateKey2)
                    m_keyAgreement.init(m_privateKey);
                } 
                break;
            case JCConsts.KeyAgreement_generateSecret:   for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { m_keyAgreement.generateSecret(m_ram1, (short) 0, m_testSettings.dataLength1, m_ram1, m_testSettings.dataLength1); } break;
    
            default: ISOException.throwIt(SW_ALG_OPS_NOT_SUPPORTED);
        }

        apdubuf[ISO7816.OFFSET_CDATA] = SUCCESS;
        apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte) 1);            
    }  
    
    
    
    //
    // PERFORMANCE TESTING - various software implementation of algorithms 
    //
    void prepare_swalg_HOTP(APDU apdu) {
        byte[] apdubuf = apdu.getBuffer();
        m_testSettings.parse(apdu);  
        short offset = ISO7816.OFFSET_CDATA;

        m_trng.generateData(m_ram1, (short) 0, (short) 256); // fill input with random data
        
        // Prepare engines for wrap and unwrap of auth. server context, $K_{authServerCtxEnc}$ and $K_{authServerCtxMAC}$        
        m_swAlgsEncCipher1 = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
        m_swAlgsDecCipher1 = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
        m_swAlgsEncCipher2 = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
        m_swAlgsDecCipher2 = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
        m_swAlgsSignSignature1 = Signature.getInstance(Signature.ALG_AES_MAC_128_NOPAD, false);
        m_swAlgsVerifySignature1 = Signature.getInstance(Signature.ALG_AES_MAC_128_NOPAD, false);
        m_swAlgsSignSignature2 = Signature.getInstance(Signature.ALG_AES_MAC_128_NOPAD, false);
        m_swAlgsVerifySignature2 = Signature.getInstance(Signature.ALG_AES_MAC_128_NOPAD, false);
        
        m_swAlgsKey1 = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, m_testSettings.keyLength, false);
        m_swAlgsKey1.setKey(m_ram1, (byte) 0);
        m_swAlgsKey2 = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, m_testSettings.keyLength, false);
        m_swAlgsKey2.setKey(m_ram1, (byte) 1);
        m_swAlgsKey3 = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, m_testSettings.keyLength, false);
        m_swAlgsKey3.setKey(m_ram1, (byte) 2);
        m_swAlgsKey4 = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, m_testSettings.keyLength, false);
        m_swAlgsKey4.setKey(m_ram1, (byte) 3);
        m_swAlgsKey5 = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, m_testSettings.keyLength, false);
        m_swAlgsKey5.setKey(m_ram1, (byte) 4);
        m_swAlgsKey6 = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, m_testSettings.keyLength, false);
        m_swAlgsKey6.setKey(m_ram1, (byte) 5);
        m_swAlgsKey7 = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, m_testSettings.keyLength, false);
        m_swAlgsKey7.setKey(m_ram1, (byte) 6);
        m_swAlgsKey8 = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, m_testSettings.keyLength, false);
        m_swAlgsKey8.setKey(m_ram1, (byte) 7);
        
        // Init long term pre-prepared keys and engines (used to protect Authentication server context)
        m_swAlgsEncCipher1.init(m_swAlgsKey1, Cipher.MODE_ENCRYPT);
        m_swAlgsDecCipher1.init(m_swAlgsKey2, Cipher.MODE_DECRYPT);
        m_swAlgsSignSignature1.init(m_swAlgsKey3, Signature.MODE_SIGN);
        m_swAlgsVerifySignature1.init(m_swAlgsKey4, Signature.MODE_VERIFY);        
        m_swAlgsEncCipher2.init(m_swAlgsKey5, Cipher.MODE_ENCRYPT);
        m_swAlgsDecCipher2.init(m_swAlgsKey6, Cipher.MODE_DECRYPT);
        m_swAlgsSignSignature2.init(m_swAlgsKey7, Signature.MODE_SIGN);
        m_swAlgsVerifySignature2.init(m_swAlgsKey8, Signature.MODE_VERIFY);        
        
        apdubuf[offset] = SUCCESS;
        apdu.setOutgoingAndSend(offset, (short) 1);
    }

    // HOTP 
    public static final short HOTP_SERVER_CTX_OFFSET  = (short) (ISO7816.OFFSET_CDATA + TestSettings.TEST_SETTINGS_LENGTH);     
    public static final short HOTP_SERVER_CTX_LENGTH  = (short) 80;     
    public static final short HOTP_USER_CTX_OFFSET = (short) (HOTP_SERVER_CTX_OFFSET + HOTP_SERVER_CTX_LENGTH + Consts.AUTH_TAG_LENGTH);     
    public static final short HOTP_USER_CTX_LENGTH  = (short) 32;     
    public static final short HOTP_USER_CODE_OFFSET = (short) (HOTP_USER_CTX_OFFSET + HOTP_USER_CTX_LENGTH + Consts.AUTH_TAG_LENGTH);     
    public static final short HOTP_USER_CODE_LENGTH  = (short) 16;     
    
    void perftest_swalg_HOTP(APDU apdu) {  
        // Transfer authentication server context, input data and state data into card
        byte[] apdubuf = apdu.getBuffer();
        short len = apdu.setIncomingAndReceive();
        short offset = ISO7816.OFFSET_CDATA;
        
        if (apdubuf[ISO7816.OFFSET_P1] == (byte) 0x20) { apdubuf[(short) 0] = SUCCESS; apdu.setOutgoingAndSend((short) 0, (short) 1); ISOException.throwIt(ISO7816.SW_NO_ERROR);} // Interrupt to enable measurement of suboperation 
        // Unwrap authentication server context -- use: $K_{authServerCtxEnc}$ and $K_{authServerCtxMAC}$
        // Note: no checking of padding
        m_swAlgsVerifySignature1.verify(apdubuf, HOTP_SERVER_CTX_OFFSET, HOTP_SERVER_CTX_LENGTH, apdubuf, (short) (HOTP_SERVER_CTX_OFFSET + HOTP_SERVER_CTX_LENGTH), Consts.AUTH_TAG_LENGTH);
        len = m_swAlgsDecCipher1.doFinal(apdubuf, HOTP_SERVER_CTX_OFFSET, HOTP_SERVER_CTX_LENGTH, apdubuf, HOTP_SERVER_CTX_OFFSET);

        if (apdubuf[ISO7816.OFFSET_P1] == (byte) 0x21) { apdubuf[(short) 0] = SUCCESS; apdu.setOutgoingAndSend((short) 0, (short) 1); ISOException.throwIt(ISO7816.SW_NO_ERROR);} // Interrupt to enable measurement of suboperation 
        
        // Prepare engines for unwrap of user HOTP context
        if (apdubuf[ISO7816.OFFSET_P2] == (byte) 0x00) { // setting P2 != allows to simulate second call with already prepared keys
            offset = HOTP_SERVER_CTX_OFFSET;
            m_swAlgsKey5.setKey(apdubuf, (byte) offset);
            offset += Consts.AES128_KEY_LENGTH;
            m_swAlgsDecCipher2.init(m_swAlgsKey5, Cipher.MODE_DECRYPT);
            m_swAlgsKey6.setKey(apdubuf, (byte) offset);
            offset += Consts.AES128_KEY_LENGTH;
            m_swAlgsVerifySignature2.init(m_swAlgsKey6, Signature.MODE_VERIFY);
        }
        
        if (apdubuf[ISO7816.OFFSET_P1] == (byte) 0x22) { apdubuf[(short) 0] = SUCCESS; apdu.setOutgoingAndSend((short) 0, (short) 1); ISOException.throwIt(ISO7816.SW_NO_ERROR);} // Interrupt to enable measurement of suboperation 
        
        // Unwrap user HOTP context
        m_swAlgsVerifySignature1.verify(apdubuf, HOTP_USER_CTX_OFFSET, HOTP_USER_CTX_LENGTH, apdubuf, (short) (HOTP_USER_CTX_OFFSET + HOTP_USER_CTX_LENGTH), Consts.AUTH_TAG_LENGTH);
        len = m_swAlgsDecCipher1.doFinal(apdubuf, HOTP_USER_CTX_OFFSET, HOTP_USER_CTX_LENGTH, apdubuf, HOTP_USER_CTX_OFFSET);

        if (apdubuf[ISO7816.OFFSET_P1] == (byte) 0x23) { apdubuf[(short) 0] = SUCCESS; apdu.setOutgoingAndSend((short) 0, (short) 1); ISOException.throwIt(ISO7816.SW_NO_ERROR);} // Interrupt to enable measurement of suboperation 
        
        // Prepare engines for unwrap of input data (user's HOTP code)
        if (apdubuf[ISO7816.OFFSET_P2] == (byte) 0x00) { // setting P2 != allows to simulate second call with already prepared keys
            m_swAlgsKey7.setKey(apdubuf, (byte) offset);
            offset += Consts.AES128_KEY_LENGTH;
            m_swAlgsDecCipher2.init(m_swAlgsKey7, Cipher.MODE_DECRYPT);
            m_swAlgsKey8.setKey(apdubuf, (byte) offset);
            offset += Consts.AES128_KEY_LENGTH;
            m_swAlgsVerifySignature2.init(m_swAlgsKey8, Signature.MODE_VERIFY);
        }        
        
        if (apdubuf[ISO7816.OFFSET_P1] == (byte) 0x24) { apdubuf[(short) 0] = SUCCESS; apdu.setOutgoingAndSend((short) 0, (short) 1); ISOException.throwIt(ISO7816.SW_NO_ERROR);} // Interrupt to enable measurement of suboperation 
        
        // Unwrap HOTP code provided by user
        m_swAlgsVerifySignature2.verify(apdubuf, HOTP_USER_CODE_OFFSET, HOTP_USER_CODE_LENGTH, apdubuf, (short) (HOTP_USER_CODE_OFFSET + HOTP_USER_CODE_LENGTH), Consts.AUTH_TAG_LENGTH);
        len = m_swAlgsDecCipher2.doFinal(apdubuf, HOTP_USER_CODE_OFFSET, HOTP_USER_CODE_LENGTH, apdubuf, HOTP_USER_CODE_OFFSET);
        
        if (apdubuf[ISO7816.OFFSET_P1] == (byte) 0x25) { apdubuf[(short) 0] = SUCCESS; apdu.setOutgoingAndSend((short) 0, (short) 1); ISOException.throwIt(ISO7816.SW_NO_ERROR);} // Interrupt to enable measurement of suboperation 

        // TODO: verify HOTP - 24ms (NXP JA2081), set result into HOTP_USER_CODE_OFFSET        
        
        // Prepare engines for wrap verification response
        if (apdubuf[ISO7816.OFFSET_P2] == (byte) 0x00) { // setting P2 != allows to simulate second call with already prepared keys
            m_swAlgsEncCipher2.init(m_swAlgsKey7, Cipher.MODE_ENCRYPT);
            m_swAlgsSignSignature2.init(m_swAlgsKey8, Signature.MODE_SIGN);
        }

        if (apdubuf[ISO7816.OFFSET_P1] == (byte) 0x26) { apdubuf[(short) 0] = SUCCESS; apdu.setOutgoingAndSend((short) 0, (short) 1); ISOException.throwIt(ISO7816.SW_NO_ERROR);} // Interrupt to enable measurement of suboperation 
        
        // Wrap verification response (expected at offset HOTP_USER_CODE_OFFSET)
        m_swAlgsEncCipher2.doFinal(apdubuf, HOTP_USER_CODE_OFFSET, HOTP_USER_CODE_LENGTH, apdubuf, HOTP_USER_CODE_OFFSET);
        m_swAlgsSignSignature2.sign(apdubuf, HOTP_USER_CODE_OFFSET, HOTP_USER_CODE_LENGTH, apdubuf, (short) (HOTP_USER_CODE_OFFSET + HOTP_USER_CODE_LENGTH));
        
        if (apdubuf[ISO7816.OFFSET_P1] == (byte) 0x27) { apdubuf[(short) 0] = SUCCESS; apdu.setOutgoingAndSend((short) 0, (short) 1); ISOException.throwIt(ISO7816.SW_NO_ERROR);} // Interrupt to enable measurement of suboperation 
        
        // Prepare engines for wrap user HOTP context
        if (apdubuf[ISO7816.OFFSET_P2] == (byte) 0x00) { // setting P2 != allows to simulate second call with already prepared keys
            m_swAlgsEncCipher2.init(m_swAlgsKey5, Cipher.MODE_ENCRYPT);
            m_swAlgsSignSignature2.init(m_swAlgsKey6, Signature.MODE_SIGN);
        }
        
        if (apdubuf[ISO7816.OFFSET_P1] == (byte) 0x28) { apdubuf[(short) 0] = SUCCESS; apdu.setOutgoingAndSend((short) 0, (short) 1); ISOException.throwIt(ISO7816.SW_NO_ERROR);} // Interrupt to enable measurement of suboperation 

        // Wrap updated user HOTP context (expected at offset HOTP_USER_CTX_OFFSET)
        m_swAlgsEncCipher2.doFinal(apdubuf, HOTP_USER_CTX_OFFSET, HOTP_USER_CTX_LENGTH, apdubuf, HOTP_USER_CTX_OFFSET);
        m_swAlgsSignSignature2.sign(apdubuf, HOTP_USER_CTX_OFFSET, HOTP_USER_CTX_LENGTH, apdubuf, (short) (HOTP_USER_CTX_OFFSET + HOTP_USER_CTX_LENGTH));
        
        if (apdubuf[ISO7816.OFFSET_P1] == (byte) 0x29) { apdubuf[(short) 0] = SUCCESS; apdu.setOutgoingAndSend((short) 0, (short) 1); ISOException.throwIt(ISO7816.SW_NO_ERROR);} // Interrupt to enable measurement of suboperation 

        // Signalize success 
        apdubuf[(short) (HOTP_USER_CTX_OFFSET - 1)] = SUCCESS;
        len = 1;
        // Transmit user HOTP context and user HOTP verification status
        len += HOTP_USER_CTX_LENGTH;
        len += Consts.AUTH_TAG_LENGTH;
        len += HOTP_USER_CODE_LENGTH;
        len += Consts.AUTH_TAG_LENGTH;
        apdu.setOutgoingAndSend((short) (HOTP_USER_CTX_OFFSET - 1), len);            
    }      
    
    
    void prepare_swalgs(APDU apdu) {
        byte[] apdubuf = apdu.getBuffer();
        m_testSettings.parse(apdu);  

        try {
            switch (m_testSettings.algorithmMethod) {
                case JCConsts.SWAlgs_AES: {
                    // allocate engine
                    m_aesCipher.RoundKeysSchedule(m_ram2, (short) 0, m_ram1);   // schedule keys into m_ram1                   
                    break;
                }
                case JCConsts.SWAlgs_xor:   
                    // No preparation
                    break;
                default: ISOException.throwIt(SW_ALG_OPS_NOT_SUPPORTED);
            }
            apdubuf[(short) (ISO7816.OFFSET_CDATA)] = SUCCESS;
            apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte)1);            
        }
        catch(CryptoException e) {
            apdubuf[(short) (ISO7816.OFFSET_CDATA)] = (byte)e.getReason();
            apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte)1);
        }  
    }      
    void perftest_swalgs(APDU apdu) {  
        byte[] apdubuf = apdu.getBuffer();
        m_testSettings.parse(apdu); 
        short repeats = (short) (m_testSettings.numRepeatWholeOperation * m_testSettings.numRepeatSubOperation);
        short chunkDataLen = (short) (m_testSettings.dataLength1 / m_testSettings.numRepeatSubOperation);

        switch (m_testSettings.algorithmMethod) {
            case JCConsts.SWAlgs_AES:   
                for (short i = 0; i < repeats; i++) { 
                    m_aesCipher.AESEncryptBlock(m_ram2, (short) 0, m_ram1); // only one 16B block, scheduled keys in m_ram1
                } 
                break;
            case JCConsts.SWAlgs_xor:   
                for (short i = 0; i < repeats; i++) {
                    // XOR 16 bytes in fully unrolled loop
                    m_ram1[(byte) 0] ^=  m_ram1[(byte) 16];
                    m_ram1[(byte) 1] ^=  m_ram1[(byte) 17];
                    m_ram1[(byte) 2] ^=  m_ram1[(byte) 18];
                    m_ram1[(byte) 3] ^=  m_ram1[(byte) 19];
                    m_ram1[(byte) 4] ^=  m_ram1[(byte) 20];
                    m_ram1[(byte) 5] ^=  m_ram1[(byte) 21];
                    m_ram1[(byte) 6] ^=  m_ram1[(byte) 22];
                    m_ram1[(byte) 7] ^=  m_ram1[(byte) 23];
                    m_ram1[(byte) 8] ^=  m_ram1[(byte) 24];
                    m_ram1[(byte) 9] ^=  m_ram1[(byte) 25];
                    m_ram1[(byte) 10] ^=  m_ram1[(byte) 26];
                    m_ram1[(byte) 11] ^=  m_ram1[(byte) 27];
                    m_ram1[(byte) 12] ^=  m_ram1[(byte) 28];
                    m_ram1[(byte) 13] ^=  m_ram1[(byte) 29];
                    m_ram1[(byte) 14] ^=  m_ram1[(byte) 30];
                    m_ram1[(byte) 15] ^=  m_ram1[(byte) 31];
                } 
                break;
            default: ISOException.throwIt(SW_ALG_OPS_NOT_SUPPORTED);
        }

        apdubuf[ISO7816.OFFSET_CDATA] = SUCCESS;
        apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte) 1);            
    }        
    
 }