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


public class AlgStorageTest {
    private   byte[]           m_ramArray = null;
    private   byte[]           m_eepromArray1 = null;
    private   byte[]           m_eepromArray2 = null;
    private   byte[]           m_eepromArray3 = null;
    private   byte[]           m_eepromArray4 = null;
    private   byte[]           m_eepromArray5 = null;
    private   byte[]           m_eepromArray6 = null;
    private   byte[]           m_eepromArray7 = null;
    private   byte[]           m_eepromArray8 = null;
  
    private   Key[]            m_keyArray1 = null;
    private   short            m_keyArray1Num = 0; 
    private   DESKey           m_desKey = null; 
    private   AESKey           m_aesKey = null; 
    private   Cipher           m_aesCipher = null;
    private   Cipher           m_desCipher = null;
    private   Cipher[]         m_cipherArray1 = null;
    private   short            m_cipherArray1Num = 0; 
    
    TestSettings               m_testSettings = null;
    
    AlgStorageTest() { 
        m_aesCipher = Cipher.getInstance(JCConsts.Cipher_ALG_AES_BLOCK_128_CBC_NOPAD, false);
        m_desCipher = Cipher.getInstance(JCConsts.Cipher_ALG_DES_CBC_NOPAD, false);
        
        m_desKey = (DESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_DES, KeyBuilder.LENGTH_DES3_3KEY, false);
        m_aesKey = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
        m_testSettings = new TestSettings();
        
        //m_ramArray = JCSystem.makeTransientByteArray((short) 600, JCSystem.CLEAR_ON_RESET);
    }

    public byte process(APDU apdu) throws ISOException {
        byte bProcessed = 0;
        byte[] apduBuffer = apdu.getBuffer();

        if (apduBuffer[ISO7816.OFFSET_CLA] == Consts.CLA_CARD_ALGTEST) {
            bProcessed = 1;
            switch ( apduBuffer[ISO7816.OFFSET_INS]) {
                case Consts.INS_CARD_TESTAVAILABLE_MEMORY: TestAvailableMemory(apdu); break;
                case Consts.INS_CARD_ALLOWED_KEYS: TestAllowedKeys(apdu); break;
                case Consts.INS_CARD_ALLOWED_ENGINES: TestAllowedEngines(apdu); break;
                case Consts.INS_CARD_RESET: JCSystem.requestObjectDeletion(); break;
                default : {
                    bProcessed = 0;
                    break;
                }
            }
        }
        
        return bProcessed;
    }

   void TestAvailableMemory(APDU apdu) {
       byte[]    apdubuf = apdu.getBuffer();
       apdu.setIncomingAndReceive();
       short     offset = (short) 0;

       short     toAllocateRAM = (short) 30000;
       if (apdubuf[ISO7816.OFFSET_P1] == 0x00) {
           if (m_ramArray == null) {
             while (true) {
               if (toAllocateRAM < 20) { break; }
               try {
                 m_ramArray = JCSystem.makeTransientByteArray(toAllocateRAM, JCSystem.CLEAR_ON_DESELECT);
                 // ALLOCATION WAS SUCESSFULL
                 break;
               }
               catch (Exception e) {
                 // DECREASE TESTED ALLOCATION LENGTH BY 1%
                 toAllocateRAM = (short) (toAllocateRAM - (short) (toAllocateRAM / 100));
               }
             }
           }
           else {
             // ARRAY ALREADY ALLOCATED, JUST RETURN ITS LENGTH
             toAllocateRAM = (short) m_ramArray.length;
           }
       }
       Util.setShort(apdubuf, offset, toAllocateRAM);
       offset = (short)(offset + 2);
       //
       // EEPROM TEST
       //
       if (apdubuf[ISO7816.OFFSET_P1] == 0x01) {
         short     toAllocateEEPROM = (short) 15000;    // at maximum 15KB allocated into single array 
         if (m_eepromArray1 == null) {
           while (true) {
             if (toAllocateEEPROM < 100) { break; } // We will stop when less then 100 remain to be allocated
             try {
               if (m_eepromArray1 == null) { m_eepromArray1 = new byte[toAllocateEEPROM]; }
               if (m_eepromArray2 == null) { m_eepromArray2 = new byte[toAllocateEEPROM]; }
               if (m_eepromArray3 == null) { m_eepromArray3 = new byte[toAllocateEEPROM]; }
               if (m_eepromArray4 == null) { m_eepromArray4 = new byte[toAllocateEEPROM]; }
               if (m_eepromArray5 == null) { m_eepromArray5 = new byte[toAllocateEEPROM]; }
               if (m_eepromArray6 == null) { m_eepromArray6 = new byte[toAllocateEEPROM]; }
               if (m_eepromArray7 == null) { m_eepromArray7 = new byte[toAllocateEEPROM]; }
               if (m_eepromArray8 == null) { m_eepromArray8 = new byte[toAllocateEEPROM]; }
               // ALLOCATION OF ALL ARRAYS WAS SUCESSFULL

               break;
             }
             catch (Exception e) {
               // DECREASE TESTED ALLOCATION LENGTH BY 10%
               toAllocateEEPROM = (short) (toAllocateEEPROM - (short) (toAllocateEEPROM / 10));
             }
           }
         }
         else {
           // ARRAY(s) ALREADY ALLOCATED, JUST RETURN THEIR COMBINED LENGTH
         }

         if (m_eepromArray1 != null) { Util.setShort(apdubuf, offset, (short) m_eepromArray1.length); }
         else { Util.setShort(apdubuf, offset, (short) 0); }
         offset = (short)(offset + 2); 
         if (m_eepromArray2 != null) { Util.setShort(apdubuf, offset, (short) m_eepromArray2.length); }
         else { Util.setShort(apdubuf, offset, (short) 0); }
         offset = (short)(offset + 2);
         if (m_eepromArray3 != null) { Util.setShort(apdubuf, offset, (short) m_eepromArray3.length); }
         else { Util.setShort(apdubuf, offset, (short) 0); }
         offset = (short)(offset + 2);
         if (m_eepromArray4 != null) { Util.setShort(apdubuf, offset, (short) m_eepromArray4.length); }
         else { Util.setShort(apdubuf, offset, (short) 0); }
         offset = (short)(offset + 2);
         if (m_eepromArray5 != null) { Util.setShort(apdubuf, offset, (short) m_eepromArray5.length); }
         else { Util.setShort(apdubuf, offset, (short) 0); }
         offset = (short)(offset + 2);
         if (m_eepromArray6 != null) { Util.setShort(apdubuf, offset, (short) m_eepromArray6.length); }
         else { Util.setShort(apdubuf, offset, (short) 0); }
         offset = (short)(offset + 2);
         if (m_eepromArray7 != null) { Util.setShort(apdubuf, offset, (short) m_eepromArray7.length); }
         else { Util.setShort(apdubuf, offset, (short) 0); }
         offset = (short)(offset + 2);
         if (m_eepromArray8 != null) { Util.setShort(apdubuf, offset, (short) m_eepromArray8.length); }
         else { Util.setShort(apdubuf, offset, (short) 0); }
         offset = (short)(offset + 2);
/**/
       }
       apdu.setOutgoingAndSend((short) 0, offset);
   }  
   
   
   void TestAllowedKeys(APDU apdu) {
        byte[] apdubuf = apdu.getBuffer();

        m_testSettings.parse(apdu);  

        short offset = 0;
        short toAllocateLength = (short) 3000;     
        if (m_keyArray1 == null) {
            m_keyArray1 = new Key[toAllocateLength];    
        }
        
        switch (apdubuf[ISO7816.OFFSET_P1]) {
            case 0:
                short keysToAllocate = (m_testSettings.dataLength1 > 0) ? m_testSettings.dataLength1 : (short) m_keyArray1.length;
                m_keyArray1Num = (short) 0;
                for (short i = (short) 0; i < keysToAllocate; i++) {
                    try {
                        m_keyArray1[i] = KeyBuilder.buildKey((byte) m_testSettings.keyType, m_testSettings.keyLength, false);
                        switch (m_testSettings.keyType) {
                            case JCConsts.KeyBuilder_TYPE_AES: 
                                ((AESKey) m_keyArray1[i]).setKey(apdubuf, (short) (i % 10));
                                break;
                            case JCConsts.KeyBuilder_TYPE_DES: 
                                ((DESKey) m_keyArray1[i]).setKey(apdubuf, (short) (i % 10));
                                break;
                            case JCConsts.KeyBuilder_TYPE_RSA_PRIVATE: 
                            case JCConsts.KeyBuilder_TYPE_RSA_CRT_PRIVATE: 
                                // do nothing
/*
        short offset_key = 0;
        offset_key++; // 0x83
        short keyLen = Util.getShort(TEST_PRIVATE_KEY, offset_key);
        offset_key += 2;
        if (TEST_PRIVATE_KEY[offset_key] == (byte) 0) { offset_key++; keyLen--; }  //compensate for potential first zero byte
        ((RSAPrivateCrtKey) m_keyArray1[i]).setDP1(TEST_PRIVATE_KEY, offset_key, keyLen);
        offset_key += keyLen;
        offset_key++; // 0x84
        keyLen = Util.getShort(TEST_PRIVATE_KEY, offset_key);
        offset_key += 2;
        if (TEST_PRIVATE_KEY[offset_key] == (byte) 0) { offset_key++; keyLen--; }  //compensate for potential first zero byte
        ((RSAPrivateCrtKey) m_keyArray1[i]).setDQ1(TEST_PRIVATE_KEY, offset_key, keyLen);
        offset_key += keyLen;
        offset_key++; // 0x85
        keyLen = Util.getShort(TEST_PRIVATE_KEY, offset_key);
        offset_key += 2;
        if (TEST_PRIVATE_KEY[offset_key] == (byte) 0) { offset_key++; keyLen--; }  //compensate for potential first zero byte
        ((RSAPrivateCrtKey) m_keyArray1[i]).setP(TEST_PRIVATE_KEY, offset_key, keyLen);
        offset_key += keyLen;
        offset_key++; // 0x86
        keyLen = Util.getShort(TEST_PRIVATE_KEY, offset_key);
        offset_key += 2;
        if (TEST_PRIVATE_KEY[offset_key] == (byte) 0) { offset_key++; keyLen--; }  //compensate for potential first zero byte
        ((RSAPrivateCrtKey) m_keyArray1[i]).setQ(TEST_PRIVATE_KEY, offset_key, keyLen);
        offset_key += keyLen;
        offset_key++; // 0x87
        keyLen = Util.getShort(TEST_PRIVATE_KEY, offset_key);
        offset_key += 2;
        if (TEST_PRIVATE_KEY[offset_key] == (byte) 0) { offset_key++; keyLen--; }  //compensate for potential first zero byte
        ((RSAPrivateCrtKey) m_keyArray1[i]).setPQ(TEST_PRIVATE_KEY, offset_key, keyLen);
        offset_key += keyLen;
*/                                
                                break;
                            default:
                                ISOException.throwIt(ISO7816.SW_WRONG_DATA);
                        }
                        m_keyArray1Num++;
                    }
                    catch (Exception e) {
                        // we can't allocate anymore
                        break;
                    }
                }
                Util.setShort(apdubuf, offset, m_keyArray1Num);
                offset += 2;  
                break;
                
            case 1:
                // TODO: Now verify how many keys we can actually use
                short numKeysUsable = 0;
                for (short i = 0; i < m_keyArray1Num; i++) {
                    try {
                        switch (m_testSettings.keyType) {
                            case JCConsts.KeyBuilder_TYPE_AES: 
                                ((AESKey) m_keyArray1[i]).setKey(apdubuf, (short) (i % 10));
                                m_aesCipher.init(m_keyArray1[i], Cipher.MODE_ENCRYPT);
                                m_aesCipher.doFinal(apdubuf, (short) 0, (short) 16, apdubuf, (short) 0);
                                break;
                            case JCConsts.KeyBuilder_TYPE_DES: 
                                ((DESKey) m_keyArray1[i]).setKey(apdubuf, (short) (i % 10));
                                m_desCipher.init(m_keyArray1[i], Cipher.MODE_ENCRYPT);
                                m_desCipher.doFinal(apdubuf, (short) 0, (short) 16, apdubuf, (short) 0);
                                break;
                            case JCConsts.KeyBuilder_ALG_TYPE_RSA_CRT_PRIVATE: 
                            case JCConsts.KeyBuilder_ALG_TYPE_RSA_PRIVATE: 
                                // do nothing
                                break;
                            default:
                                ISOException.throwIt(ISO7816.SW_WRONG_DATA);
                        }
                        numKeysUsable++;
                    }
                    catch (Exception e) {
                        // we can't use anymore - store index
                        break;
                    }
                }
                Util.setShort(apdubuf, offset, numKeysUsable);
                offset += 2;    
                break;
        }

        apdu.setOutgoingAndSend((short) 0, offset);
   }
   
   
   void TestAllowedEngines(APDU apdu) {
        byte[] apdubuf = apdu.getBuffer();

        m_testSettings.parse(apdu);  
        
        m_aesKey.setKey(apdubuf, (short) 0);
        m_desKey.setKey(apdubuf, (short) 0);

        short offset = 0;
        short toAllocateLength = (short) 1000;     
        if (m_cipherArray1 == null) {
            m_cipherArray1 = new Cipher[toAllocateLength];    
        }
        
        switch (apdubuf[ISO7816.OFFSET_P1]) {
            case 0:
                short enginesToAllocate = (m_testSettings.dataLength1 > 0) ? m_testSettings.dataLength1 : (short) m_cipherArray1.length;
                m_cipherArray1Num = (short) 0;
                for (short i = (short) 0; i < enginesToAllocate; i++) {
                    try {
                        m_cipherArray1[i] = Cipher.getInstance((byte) m_testSettings.algorithmSpecification, false);
                        switch (m_testSettings.algorithmSpecification) {
                            case JCConsts.Cipher_ALG_AES_BLOCK_128_CBC_NOPAD: 
                                m_cipherArray1[i].init(m_aesKey, Cipher.MODE_ENCRYPT);
                                break;
                            case JCConsts.Cipher_ALG_DES_CBC_NOPAD: 
                                m_cipherArray1[i].init(m_desKey, Cipher.MODE_ENCRYPT);
                                break;
                            default:
                                ISOException.throwIt(ISO7816.SW_WRONG_DATA);

                        }
                        m_cipherArray1Num++;
                    }
                    catch (Exception e) {
                        // we can't allocate anymore
                        break;
                    }
                }
                Util.setShort(apdubuf, offset, m_cipherArray1Num);
                offset += 2;  
                break;
                
            case 1:
                // TODO: Now verify how many engines we can actually use
                short numEnginesUsable = 0;
                for (short i = 0; i < m_cipherArray1Num; i++) {
                    try {
                        switch (m_testSettings.algorithmSpecification) {
                            case JCConsts.Cipher_ALG_AES_BLOCK_128_CBC_NOPAD: 
                                m_cipherArray1[i].init(m_aesKey, Cipher.MODE_ENCRYPT);
                                m_cipherArray1[i].doFinal(apdubuf, (short) 0, (short) 16, apdubuf, (short) 0);
                                break;
                            case JCConsts.Cipher_ALG_DES_CBC_NOPAD: 
                                m_cipherArray1[i].init(m_desKey, Cipher.MODE_ENCRYPT);
                                m_cipherArray1[i].doFinal(apdubuf, (short) 0, (short) 16, apdubuf, (short) 0);
                                break;
                            default:
                                ISOException.throwIt(ISO7816.SW_WRONG_DATA);
                        }
                        numEnginesUsable++;
                    }
                    catch (Exception e) {
                        // we can't use anymore - store index
                        break;
                    }
                }
                Util.setShort(apdubuf, offset, numEnginesUsable);
                offset += 2;    
                break;
        }

        apdu.setOutgoingAndSend((short) 0, offset);   
   }
}