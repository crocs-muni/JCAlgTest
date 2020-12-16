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
package algtest;


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
    
    
    private byte[] m_ramArray = null;
    private byte[] m_ramArray2 = null;

    private short[] memPersistent = null;
    private short[] memDeselect = null;
    private short[] memReset = null;

    private RSAPublicKey       m_rsaPublicKey = null;
    private RSAPrivateCrtKey   m_rsaPrivateKey = null;
    
    
    short m_freeRAMReset = 0;
    short m_freeRAMDeselect = 0;
    short[] m_freeEEPROM = null;

    
    // for class 'javacard.security.KeyAgreement'
    public static final byte ALG_EC_SVDP_DH = 1;
    
    final static short EXPONENT_LENGTH = (short) 128;
    final static short MODULUS_LENGTH = (short) 128;
    final static short ADDITIONAL_ARGUMENTS_LENGTH = (short) (ISO7816.OFFSET_CDATA + 4); // two short arguments
    
    final static byte SUPP_ALG_UNTOUCHED = (byte) 0xf0;
    final static byte SUPP_ALG_SUPPORTED = (byte) 0x00;
    final static byte SUPP_ALG_EXCEPTION_CODE_OFFSET = (byte) 0;
    
    public final static byte RETURN_INSTALL_TIME_MEMORY_SIZE = (byte) 1;
    
    
    
    final static byte SUCCESS =                    (byte) 0xAA;

    public final static short SW_STAT_OK                   = (short) 0x9000;
    public final static short SW_ALG_TYPE_NOT_SUPPORTED    = (short) 0x6001;
    public final static short SW_ALG_OPS_NOT_SUPPORTED     = (short) 0x6002;
    public final static short SW_ALG_TYPE_UNKNOWN          = (short) 0x6003;
    
    
    /* Auxiliary variables to choose class - used in APDU as P1 byte. */
    public static final byte CLASS_CIPHER          = 0x11;
    public static final byte CLASS_SIGNATURE       = 0x12;
    public static final byte CLASS_KEYAGREEMENT    = 0x13;
    public static final byte CLASS_MESSAGEDIGEST   = 0x15;
    public static final byte CLASS_RANDOMDATA      = 0x16;
    public static final byte CLASS_CHECKSUM        = 0x17;
    public static final byte CLASS_KEYPAIR         = 0x19;
    public static final byte CLASS_KEYBUILDER      = 0x20;

    AlgSupportTest(byte[] auxRAMArray, byte[] auxRAMArray2, short installFreeRAMReset, short installFreeRAMDeselect, short[] installFreeEEPROM) { 
        m_ramArray = auxRAMArray;
        m_ramArray2 = auxRAMArray2;
        m_freeRAMReset = installFreeRAMReset;
        m_freeRAMDeselect = installFreeRAMDeselect;
        m_freeEEPROM = installFreeEEPROM;
        
        memPersistent = JCSystem.makeTransientShortArray((short) 2, JCSystem.CLEAR_ON_RESET);
        memDeselect = JCSystem.makeTransientShortArray((short) 2, JCSystem.CLEAR_ON_RESET);
        memReset = JCSystem.makeTransientShortArray((short) 2, JCSystem.CLEAR_ON_RESET);
    }

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
                //case Consts.INS_CARD_RESET: JCSystem.requestObjectDeletion(); break;
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
       
       byte     algorithmClass = apdubuf[ISO7816.OFFSET_CDATA];
       short    algorithmParam1 = Util.makeShort(apdubuf[(short) (ISO7816.OFFSET_CDATA + 1)], apdubuf[(short) (ISO7816.OFFSET_CDATA + 2)]);
       byte     modular_param1 = apdubuf[ISO7816.OFFSET_CDATA];
       byte     modular_param2 = apdubuf[(short) (ISO7816.OFFSET_CDATA + 1)];
       byte     modular_param3 = apdubuf[(short) (ISO7816.OFFSET_CDATA + 2)];

       Util.arrayFillNonAtomic(apdubuf, ISO7816.OFFSET_CDATA, (short) 240, SUPP_ALG_UNTOUCHED);
       offset++;
       apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = apdubuf[ISO7816.OFFSET_P1];

       short persistentMemStart = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_PERSISTENT);
       short deselectMemStart = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT);
       short resetMemStart = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_RESET);
       
       JCSystem.getAvailableMemory(memPersistent, (short) 0, JCSystem.MEMORY_TYPE_PERSISTENT); //jc304
       JCSystem.getAvailableMemory(memDeselect, (short) 0, JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT); //jc304
       JCSystem.getAvailableMemory(memReset, (short) 0, JCSystem.MEMORY_TYPE_TRANSIENT_RESET); //jc304

       // Place "preventively" NO_SUCH_ALGORITHM as a response - will be replaced by actual result later
       // The reason is to return NO_SUCH_ALGORITHM for the items like CLASS_AEADCIPHER which are removed 
       // by postprocessing in some versions of testing applet based on the target JC API version
       offset++;  
       apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = (byte) CryptoException.NO_SUCH_ALGORITHM; 
               
       switch (apdubuf[ISO7816.OFFSET_P1]) {
           case Consts.CLASS_CIPHER: {
             try {m_encryptCipher = Cipher.getInstance(algorithmClass, false); apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = SUPP_ALG_SUPPORTED;}
             catch (CryptoException e) {apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = (byte) (e.getReason() + SUPP_ALG_EXCEPTION_CODE_OFFSET); }
             break;
           }
           case Consts.CLASS_CIPHER_MODULAR: { 
             // Uses getInstance(byte cipherAlgorithm, byte paddingAlgorithm, boolean externalAccess) //jc304
             try {m_encryptCipher = Cipher.getInstance(modular_param1, modular_param2, false); apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = SUPP_ALG_SUPPORTED;} //jc304
             catch (CryptoException e) {apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = (byte) (e.getReason() + SUPP_ALG_EXCEPTION_CODE_OFFSET); } //jc304
             break; 
           } 
           case Consts.CLASS_SIGNATURE: {
             try {m_sign = Signature.getInstance(algorithmClass, false); apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = SUPP_ALG_SUPPORTED;}
             catch (CryptoException e) {apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = (byte) (e.getReason() + SUPP_ALG_EXCEPTION_CODE_OFFSET); }
             break;
           }
           case Consts.CLASS_SIGNATURE_MODULAR: { 
             // Uses getInstance(byte messageDigestAlgorithm,byte cipherAlgorithm,byte paddingAlgorithm,boolean externalAccess) //jc304
             try {m_sign = Signature.getInstance(modular_param1, modular_param2, modular_param3, false); apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = SUPP_ALG_SUPPORTED;} //jc304
             catch (CryptoException e) {apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = (byte) (e.getReason() + SUPP_ALG_EXCEPTION_CODE_OFFSET); } //jc304
             break; 
           } 
           case Consts.CLASS_MESSAGEDIGEST: {
             try {m_digest = MessageDigest.getInstance(algorithmClass, false); apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = SUPP_ALG_SUPPORTED;}
             catch (CryptoException e) {apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = (byte) (e.getReason() + SUPP_ALG_EXCEPTION_CODE_OFFSET); }
             break;
           }
           case Consts.CLASS_RANDOMDATA: {
             try {m_random = RandomData.getInstance(algorithmClass); apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = SUPP_ALG_SUPPORTED;}
             catch (CryptoException e) {apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = (byte) (e.getReason() + SUPP_ALG_EXCEPTION_CODE_OFFSET); }
             break;
           }
           case Consts.CLASS_KEYBUILDER: {
             try {m_key1 = KeyBuilder.buildKey(algorithmClass, algorithmParam1, false); apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = SUPP_ALG_SUPPORTED;}
             catch (CryptoException e) {apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = (byte) (e.getReason() + SUPP_ALG_EXCEPTION_CODE_OFFSET); }
             break;
           }
           // BUGBUG: not implemented yet
           //case Consts.CLASS_KEYBUILDER_MODULAR: {
           //    break;
           //}
           case Consts.CLASS_KEYPAIR: {
             try {
                 m_keyPair1 = null;
                 if ((algorithmClass == KeyPair.ALG_EC_FP) || (algorithmClass == KeyPair.ALG_EC_F2M)) {
                     // Try two alternatives for EC key construction - new KeyPair() emit exception on some cards
                     try {
                         // Make KeyPair object first, then initialize curve
                         m_keyPair1 = new KeyPair(algorithmClass, algorithmParam1);
                         EC_Consts.ensureInitializedECCurve(algorithmClass, algorithmParam1, m_keyPair1, m_ramArray);
                     } catch (Exception e) {
                         // Make public and private keys first, then iniatlize curve and finally create KeyPair
                         ECPrivateKey ecPrivKey = null;
                         ECPublicKey ecPubKey = null;
                         if (algorithmClass == KeyPair.ALG_EC_FP) {
                             ecPrivKey = (ECPrivateKey) KeyBuilder.buildKey(KeyBuilder.TYPE_EC_FP_PRIVATE, algorithmParam1, false);
                             ecPubKey = (ECPublicKey) KeyBuilder.buildKey(KeyBuilder.TYPE_EC_FP_PUBLIC, algorithmParam1, false);
                         }
                         if (algorithmClass == KeyPair.ALG_EC_F2M) {
                             ecPrivKey = (ECPrivateKey) KeyBuilder.buildKey(KeyBuilder.TYPE_EC_F2M_PRIVATE, algorithmParam1, false);
                             ecPubKey = (ECPublicKey) KeyBuilder.buildKey(KeyBuilder.TYPE_EC_F2M_PUBLIC, algorithmParam1, false);
                         }
                         if ((ecPrivKey != null) && (ecPubKey != null)) {
                             EC_Consts.setECKeyParams(ecPubKey, ecPrivKey, algorithmClass, algorithmParam1, m_ramArray);
                             m_keyPair1 = new KeyPair(ecPubKey, ecPrivKey);
                         }
                     }
                 }
                 else{
                     // Other non-ECC keypairs
                     m_keyPair1 = new KeyPair(algorithmClass, algorithmParam1);
                 }

                 m_keyPair1.genKeyPair();
                 apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = SUPP_ALG_SUPPORTED;
             }
             catch (CryptoException e) {apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = (byte) (e.getReason() + SUPP_ALG_EXCEPTION_CODE_OFFSET); }
             break;
           }
           case Consts.CLASS_KEYAGREEMENT: {
             try {m_object = KeyAgreement.getInstance(algorithmClass, false); apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = SUPP_ALG_SUPPORTED;}
             catch (CryptoException e) { apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = (byte) (e.getReason() + SUPP_ALG_EXCEPTION_CODE_OFFSET); }
             break;
           }
           case Consts.CLASS_CHECKSUM: {
             try {m_object = Checksum.getInstance(algorithmClass, false); apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = SUPP_ALG_SUPPORTED;}
             catch (CryptoException e) {apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = (byte) (e.getReason() + SUPP_ALG_EXCEPTION_CODE_OFFSET); }
             break;
           }
           //case Consts.CLASS_BIOBUILDER: { 
           //  try { m_object = javacardx.biometry.BioBuilder.getInstance(algorithmClass, false); apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = SUPP_ALG_SUPPORTED;}  //jc305
           //  catch (CryptoException e) { apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = (byte) (e.getReason() + SUPP_ALG_EXCEPTION_CODE_OFFSET);} //jc305
           //  break; 
           //}
           case Consts.CLASS_AEADCIPHER: {
               try {m_object = javacardx.crypto.AEADCipher.getInstance(algorithmClass, false); apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = SUPP_ALG_SUPPORTED;}  //jc305
               catch (CryptoException e) { apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = (byte) (e.getReason() + SUPP_ALG_EXCEPTION_CODE_OFFSET);} //jc305
               break;
           }
           case Consts.CLASS_OWNERPINBUILDER: { 
               try {m_object = javacard.framework.OwnerPINBuilder.buildOwnerPIN((byte) 3, (byte) 4, algorithmClass); apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = SUPP_ALG_SUPPORTED;}  //jc305
               catch (CryptoException e) { apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = (byte) (e.getReason() + SUPP_ALG_EXCEPTION_CODE_OFFSET);} //jc305
               break;
           } 
           default:
               ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
        }
       
       short persistentMemEnd = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_PERSISTENT);
       short deselectMemEnd = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT);
       short resetMemEnd = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_RESET);

       // ENDING 0xFF
       offset++;
       apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = (byte) 0xFF;
       offset++;
       
       boolean bReadExtMemory = false; 
       bReadExtMemory = true; //jc304
       if (bReadExtMemory) { 
           // If JCSystem.getAvailableMemory from jc304 is available, then use it for extended resolution
           Util.setShort(apdubuf, (short) (ISO7816.OFFSET_CDATA + offset), memPersistent[0]); offset += 2; 
           Util.setShort(apdubuf, (short) (ISO7816.OFFSET_CDATA + offset), memPersistent[1]); offset += 2; 
           JCSystem.getAvailableMemory(memPersistent, (short) 0, JCSystem.MEMORY_TYPE_PERSISTENT); //jc304
           Util.setShort(apdubuf, (short) (ISO7816.OFFSET_CDATA + offset), memPersistent[0]); offset += 2; 
           Util.setShort(apdubuf, (short) (ISO7816.OFFSET_CDATA + offset), memPersistent[1]); offset += 2; 

           Util.setShort(apdubuf, (short) (ISO7816.OFFSET_CDATA + offset), memDeselect[0]); offset += 2; 
           Util.setShort(apdubuf, (short) (ISO7816.OFFSET_CDATA + offset), memDeselect[1]); offset += 2; 
           JCSystem.getAvailableMemory(memDeselect, (short) 0, JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT); //jc304
           Util.setShort(apdubuf, (short) (ISO7816.OFFSET_CDATA + offset), memDeselect[0]); offset += 2; 
           Util.setShort(apdubuf, (short) (ISO7816.OFFSET_CDATA + offset), memDeselect[1]); offset += 2; 

           Util.setShort(apdubuf, (short) (ISO7816.OFFSET_CDATA + offset), memReset[0]); offset += 2; 
           Util.setShort(apdubuf, (short) (ISO7816.OFFSET_CDATA + offset), memReset[1]); offset += 2; 
           JCSystem.getAvailableMemory(memReset, (short) 0, JCSystem.MEMORY_TYPE_TRANSIENT_RESET); //jc304
           Util.setShort(apdubuf, (short) (ISO7816.OFFSET_CDATA + offset), memReset[0]); offset += 2; 
           Util.setShort(apdubuf, (short) (ISO7816.OFFSET_CDATA + offset), memReset[1]); offset += 2; 
       }
       else {
           // Setting single short as byte - write only lower two bytes, fill with zeroes
           Util.arrayFillNonAtomic(apdubuf, offset, (short) (7 * 4), (byte) 0);
           offset += 2; // Shift to lower two bytes of first entry
           Util.setShort(apdubuf, (short) (ISO7816.OFFSET_CDATA + offset), persistentMemStart);
           offset += 4;
           Util.setShort(apdubuf, (short) (ISO7816.OFFSET_CDATA + offset), persistentMemEnd);
           offset += 4;
           Util.setShort(apdubuf, (short) (ISO7816.OFFSET_CDATA + offset), deselectMemStart);
           offset += 4;
           Util.setShort(apdubuf, (short) (ISO7816.OFFSET_CDATA + offset), deselectMemEnd);
           offset += 4;
           Util.setShort(apdubuf, (short) (ISO7816.OFFSET_CDATA + offset), resetMemStart);
           offset += 4;
           Util.setShort(apdubuf, (short) (ISO7816.OFFSET_CDATA + offset), resetMemEnd);
           offset += 2; // last is not skipping whole byte
       }
       
       apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, offset);
    }
    
    void JCSystemInfo(APDU apdu) {
       byte[]    apdubuf = apdu.getBuffer();
       apdu.setIncomingAndReceive();
       short     offset = (short) 0;
       byte      p1 = apdubuf[ISO7816.OFFSET_P1];   

        Util.setShort(apdubuf, offset, JCSystem.getVersion());
        offset = (short)(offset + 2);
        apdubuf[offset] = (JCSystem.isObjectDeletionSupported() ? (byte) 1: (byte) 0);
        offset++;

        Util.setShort(apdubuf, offset, JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_PERSISTENT));
        offset = (short)(offset + 2);
        
        if (p1 == RETURN_INSTALL_TIME_MEMORY_SIZE) {
            Util.setShort(apdubuf, offset, m_freeRAMReset);
            offset = (short) (offset + 2);
            Util.setShort(apdubuf, offset, m_freeRAMDeselect);
            offset = (short) (offset + 2);
        }
        else {
            short ramMemorySize = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_RESET);
            ramMemorySize += (short) m_ramArray.length; // This array is allocated by this applet and therefore consumed some space
            ramMemorySize += (short) m_ramArray2.length; // This array is allocated by this applet and therefore consumed some space
            Util.setShort(apdubuf, offset, ramMemorySize);
            offset = (short)(offset + 2);
            Util.setShort(apdubuf, offset, JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT));
            offset = (short)(offset + 2);
        }
        Util.setShort(apdubuf, offset, JCSystem.getMaxCommitCapacity());
        offset = (short)(offset + 2);
        // APDU information
        Util.setShort(apdubuf, offset, APDU.getInBlockSize());
        offset = (short) (offset + 2);
        Util.setShort(apdubuf, offset, APDU.getOutBlockSize());
        offset = (short) (offset + 2);
        apdubuf[offset] = APDU.getProtocol();
        offset++;
        apdubuf[offset] = apdu.getNAD();
        offset++;
        // Extended memory information
        if (p1 == RETURN_INSTALL_TIME_MEMORY_SIZE) { //jc304
            Util.setShort(apdubuf, offset, m_freeEEPROM[0]); offset += 2; //jc304
            Util.setShort(apdubuf, offset, m_freeEEPROM[1]); offset += 2; //jc304
        } //jc304
        else { //jc304
            JCSystem.getAvailableMemory(memPersistent, (short) 0, JCSystem.MEMORY_TYPE_PERSISTENT); //jc304
            Util.setShort(apdubuf, offset, memPersistent[0]); offset += 2; //jc304
            Util.setShort(apdubuf, offset, memPersistent[1]); offset += 2; //jc304
        } //jc304
        JCSystem.getAvailableMemory(memDeselect, (short) 0, JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT); //jc304
        Util.setShort(apdubuf, offset, memDeselect[0]); offset += 2; //jc304
        Util.setShort(apdubuf, offset, memDeselect[1]); offset += 2; //jc304
        JCSystem.getAvailableMemory(memReset, (short) 0, JCSystem.MEMORY_TYPE_TRANSIENT_RESET); //jc304
        Util.setShort(apdubuf, offset, memReset[0]); offset += 2; //jc304
        Util.setShort(apdubuf, offset, memReset[1]); offset += 2; //jc304      

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
