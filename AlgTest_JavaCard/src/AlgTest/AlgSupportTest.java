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


public class AlgSupportTest {
    private   Cipher           m_encryptCipher = null;
    private   Cipher           m_encryptCipherRSA = null;
    private   Signature        m_sign = null;
    private   MessageDigest    m_digest = null;
    private   RandomData       m_random = null;
    private   Object           m_object = null;
    private   KeyPair          m_keyPair1 = null;
    private   KeyPair          m_keyPair2 = null;
    private   Checksum         m_checksum = null;
    private   KeyAgreement     m_keyAgreement = null;   
    private   RandomData       m_trng = null; 
    private   Key              m_key1 = null;
    
  
  
    private   byte[]           m_ramArray = null;
    private   byte[]           m_eepromArray1 = null;
    private   byte[]           m_eepromArray2 = null;
    private   byte[]           m_eepromArray3 = null;
    private   byte[]           m_eepromArray4 = null;
    private   byte[]           m_eepromArray5 = null;
    private   byte[]           m_eepromArray6 = null;
    private   byte[]           m_eepromArray7 = null;
    private   byte[]           m_eepromArray8 = null;
    private   RSAPublicKey     m_rsaPublicKey = null;
    private   RSAPrivateCrtKey m_rsaPrivateKey = null;   
  
    private   Key[]            m_keyArray1 = null;
    private   Key[]            m_keyArray2 = null;
    private   Key[]            m_keyArray3 = null;
    private   Key[]            m_keyArray4 = null;
    private   Key[]            m_keyArray5 = null;
    private   Key[]            m_keyArray6 = null;
    private   Key[]            m_keyArray7 = null;
    
    // for class 'javacard.security.KeyAgreement'
    public static final byte ALG_EC_SVDP_DH = 1;
    
    final static short EXPONENT_LENGTH = (short) 128;
    final static short MODULUS_LENGTH = (short) 128;
    final static short ADDITIONAL_ARGUMENTS_LENGTH = (short) (ISO7816.OFFSET_CDATA + 4); // two short arguments
    
    final static byte SUPP_ALG_UNTOUCHED = (byte) 0xf0;
    final static byte SUPP_ALG_SUPPORTED = (byte) 0x00;
    final static byte SUPP_ALG_EXCEPTION_CODE_OFFSET = (byte) 0;
    
    
    final static byte SUCCESS =                    (byte) 0xAA;

    public final static short SW_STAT_OK                   = (short) 0x9000;
    public final static short SW_ALG_TYPE_NOT_SUPPORTED    = (short) 0x6001;
    public final static short SW_ALG_OPS_NOT_SUPPORTED     = (short) 0x6002;
    public final static short SW_ALG_TYPE_UNKNOWN          = (short) 0x6003;
    
    public final static short RAM1_ARRAY_LENGTH = (short) 600;
    public final static short RAM2_ARRAY_LENGTH = (short) 16;
    
    
    /* Auxiliary variables to choose class - used in APDU as P1 byte. */
    public static final byte CLASS_CIPHER          = 0x11;
    public static final byte CLASS_SIGNATURE       = 0x12;
    public static final byte CLASS_KEYAGREEMENT    = 0x13;
    public static final byte CLASS_MESSAGEDIGEST   = 0x15;
    public static final byte CLASS_RANDOMDATA      = 0x16;
    public static final byte CLASS_CHECKSUM        = 0x17;
    public static final byte CLASS_KEYPAIR         = 0x19;
    public static final byte CLASS_KEYBUILDER      = 0x20;

    AlgSupportTest() { }

    public byte process(APDU apdu) throws ISOException {
        byte bProcessed = 0;
        byte[] apduBuffer = apdu.getBuffer();

        if (apduBuffer[ISO7816.OFFSET_CLA] == Consts.CLA_CARD_ALGTEST) {
            bProcessed = 1;
            switch ( apduBuffer[ISO7816.OFFSET_INS]) {
                case Consts.INS_CARD_TESTRSAEXPONENTSET: TestRSAExponentSet(apdu); break;
                case Consts.INS_CARD_JCSYSTEM_INFO: JCSystemInfo(apdu); break;
                case Consts.INS_CARD_TESTSUPPORTEDMODES_SINGLE: TestSupportedModeSingle(apdu); break;
                // case INS_CARD_TESTEXTAPDU: TestExtendedAPDUSupport(apdu); break; // this has to be tested by separate applet with ExtAPDU enabled - should succedd during upload and run
                case Consts.INS_CARD_DATAINOUT: TestIOSpeed(apdu); break;
                case Consts.INS_CARD_RESET: JCSystem.requestObjectDeletion(); break;
                // case Consts.INS_CARD_GETRSAKEY: GetRSAKey(apdu); break; // moved to AlgKeyHarvest class
                default : {
                    bProcessed = 0;
                    break;
                }
            }
        }
        
        return bProcessed;
    }

    void TestSupportedModeSingle(APDU apdu) {
       byte[]    apdubuf = apdu.getBuffer();
       apdu.setIncomingAndReceive();
       short     offset = -1;
       
       byte      algorithmClass = apdubuf[ISO7816.OFFSET_CDATA];
       short     algorithmParam1 = Util.makeShort(apdubuf[(short) (ISO7816.OFFSET_CDATA + 1)], apdubuf[(short) (ISO7816.OFFSET_CDATA + 2)]);
       
       Util.arrayFillNonAtomic(apdubuf, ISO7816.OFFSET_CDATA, (short) 240, SUPP_ALG_UNTOUCHED);
       offset++;
       apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = apdubuf[ISO7816.OFFSET_P1];

       switch (apdubuf[ISO7816.OFFSET_P1]) {
           case (byte) 0x11: {
             try {offset++;m_encryptCipher = Cipher.getInstance(algorithmClass, false); apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = SUPP_ALG_SUPPORTED;}
             catch (CryptoException e) {apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = (byte) (e.getReason() + SUPP_ALG_EXCEPTION_CODE_OFFSET); }
             break;
           }
           case (byte) 0x12: {
             try {offset++;m_sign = Signature.getInstance(algorithmClass, false); apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = SUPP_ALG_SUPPORTED;}
             catch (CryptoException e) {apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = (byte) (e.getReason() + SUPP_ALG_EXCEPTION_CODE_OFFSET); }
             break;
           }
           case (byte) 0x15: {
             try {offset++;m_digest = MessageDigest.getInstance(algorithmClass, false); apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = SUPP_ALG_SUPPORTED;}
             catch (CryptoException e) {apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = (byte) (e.getReason() + SUPP_ALG_EXCEPTION_CODE_OFFSET); }
             break;
           }
           case (byte) 0x16: {
             try {offset++;m_random = RandomData.getInstance(algorithmClass); apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = SUPP_ALG_SUPPORTED;}
             catch (CryptoException e) {apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = (byte) (e.getReason() + SUPP_ALG_EXCEPTION_CODE_OFFSET); }
             break;
           }
           case (byte) 0x20: {
             try {offset++; m_key1 = KeyBuilder.buildKey(algorithmClass, algorithmParam1, false); apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = SUPP_ALG_SUPPORTED;}
             catch (CryptoException e) {apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = (byte) (e.getReason() + SUPP_ALG_EXCEPTION_CODE_OFFSET); }
             break;
           }
           case (byte) 0x18: // no break
           case (byte) 0x19: // no break
           case (byte) 0x1C: { // no break
             try {
               offset++;m_keyPair1 = new KeyPair(algorithmClass, algorithmParam1);
               m_keyPair1.genKeyPair();
               apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = SUPP_ALG_SUPPORTED;
             }
             catch (CryptoException e) {apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = (byte) (e.getReason() + SUPP_ALG_EXCEPTION_CODE_OFFSET); }
             break;
           }
           case (byte) 0x13: {
             try {offset++;m_object = KeyAgreement.getInstance(ALG_EC_SVDP_DH, false); apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = SUPP_ALG_SUPPORTED;}
             catch (CryptoException e) { apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = (e.getReason() == CryptoException.NO_SUCH_ALGORITHM) ? (byte) 0 : (byte) 2;  }
             break;
           }
           case (byte) 0x17: {
             try {offset++;m_object = Checksum.getInstance(algorithmClass, false); apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = SUPP_ALG_SUPPORTED;}
             catch (CryptoException e) {apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = (byte) (e.getReason() + SUPP_ALG_EXCEPTION_CODE_OFFSET); }
             break;
           }
        }
       // ENDING 0xFF
       offset++;
       apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = (byte) 0xFF;

       apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (short) 240);
    }
    

    void JCSystemInfo(APDU apdu) {
       byte[]    apdubuf = apdu.getBuffer();
       apdu.setIncomingAndReceive();
       short     offset = (short) 0;

        Util.setShort(apdubuf, offset, JCSystem.getVersion());
        offset = (short)(offset + 2);
        apdubuf[offset] = (JCSystem.isObjectDeletionSupported() ? (byte) 1: (byte) 0);
        offset++;

        Util.setShort(apdubuf, offset, JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_PERSISTENT));
        offset = (short)(offset + 2);
        Util.setShort(apdubuf, offset, JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_RESET));
        offset = (short)(offset + 2);
        Util.setShort(apdubuf, offset, JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT));
        offset = (short)(offset + 2);
        Util.setShort(apdubuf, offset, JCSystem.getMaxCommitCapacity());
        offset = (short)(offset + 2);

        apdu.setOutgoingAndSend((byte) 0, offset);
      }
 
   /**
    * Note - Whole process is differentiated into separate steps to distinguish
    * between different situation when random exponent cannot be set.
    * E.g. Some cards allow to set random exponent, but throw Exception when public key
    * is used for encryption (rsa_PublicKey.setExponent). Other cards fail directly
    * during exponent setting (rsa_PublicKey.setExponent). One card (PalmeraV5) successfully
    * passed all steps, but didn't returned encrypted data (resp. length of returned
    * data was 0 and status 90 00)
    */
   void TestRSAExponentSet(APDU apdu) {
       byte[]    apdubuf = apdu.getBuffer();
       short     dataLen = apdu.setIncomingAndReceive();

       switch (apdubuf[ISO7816.OFFSET_P1]) {
         case 1: {
           // Allocate objects if not allocated yet
           if (m_rsaPublicKey == null) { m_rsaPublicKey = (RSAPublicKey) KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PUBLIC,KeyBuilder.LENGTH_RSA_1024,false); }
           if (m_random == null) { m_random = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM); } 
           if (m_encryptCipherRSA == null) { m_encryptCipherRSA = Cipher.getInstance(Cipher.ALG_RSA_NOPAD, false); }
           break;
         }
         case 2: {
           // Try to set random modulus
           m_random.generateData(apdubuf, ISO7816.OFFSET_CDATA, MODULUS_LENGTH);
           m_rsaPublicKey.setModulus(apdubuf, ISO7816.OFFSET_CDATA, MODULUS_LENGTH);
           break;
         }
         case 3: {
           // Try to set random exponent
           m_random.generateData(apdubuf, ISO7816.OFFSET_CDATA, EXPONENT_LENGTH);
           // repair exponent
           apdubuf[ISO7816.OFFSET_CDATA+EXPONENT_LENGTH-1] |= 0x01; // exponent must be odd - set LSB
           apdubuf[ISO7816.OFFSET_CDATA] |= 0x01 << 7; // exponent must be EXPONENT_LENGTH bytes long - set MSB

           // set exponent part of public key
           m_rsaPublicKey.setExponent(apdubuf, ISO7816.OFFSET_CDATA, EXPONENT_LENGTH);
           break;
         }
         case 4: {
           // Try to initialize cipher with public key with random exponent
           m_encryptCipherRSA.init(m_rsaPublicKey, Cipher.MODE_ENCRYPT);
           break;
         }
         case 5: {
           // Try to encrypt block of data
           short offset = m_encryptCipherRSA.doFinal(apdubuf, (byte) 0, MODULUS_LENGTH, apdubuf, (byte) 0);
           apdu.setOutgoingAndSend((byte) 0, offset);
           break;
         }
       }
   }
   
   void TestIOSpeed(APDU apdu) {
      byte[]    apdubuf = apdu.getBuffer();
      short     dataLen = apdu.setIncomingAndReceive();

      // RETURN INPUT DATA UNCHANGED
      apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, dataLen);
   }  
   
  void TestExtendedAPDUSupport(APDU apdu) {
/* ONLY FOR JC2.2.2  
    byte[]    apdubuf = apdu.getBuffer();
    short     LC = apdu.getIncomingLength();
    short     receivedDataTotal = 0;
    short     dataLen = apdu.setIncomingAndReceive();
    short     dataOffset = apdu.getOffsetCdata();
    short     offset = (short) 0;
    // Receive all chunks of data
    while (dataLen > 0) {
        receivedDataTotal += dataLen;
        dataLen = apdu.receiveBytes(dataOffset);
    }
    // Write length indicated by apdu.getIncomingLength()
    Util.setShort(apdubuf, offset, LC);
    offset = (short)(offset + 2);
    
    // Write actual length received
    Util.setShort(apdubuf, offset, receivedDataTotal);
    offset = (short)(offset + 2);
    apdu.setOutgoingAndSend((byte) 0, offset);
*/   }
   
}