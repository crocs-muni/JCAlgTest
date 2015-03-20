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
/*
 * Package AID: 6D 79 70 61 63 6B 61 67 31 (6D797061636B616731)
 * Applet AID:  6D 79 70 61 63 30 30 30 31 (6D7970616330303031)
 */
package AlgTest;

/*
 * Imported packages
 */
// specific import for Javacard API access
import javacard.framework.*;
import javacard.security.*;
import javacardx.crypto.*;


// JC 2.2.2 only
//import javacardx.apdu.ExtendedLength; 
//public class AlgTest extends javacard.framework.Applet implements ExtendedLength 

public class AlgTestSinglePerApdu extends javacard.framework.Applet 
{
    // NOTE: when incrementing version, don't forget to update ALGTEST_JAVACARD_VERSION_CURRENT value

    /**
     * Version 1.4 (15.3.2015)
     * + Merged separate javacard applet codes into AlgTestSinglePerApdu.java
     * + Added performance testing from L. Kunikova
     */
    final static byte ALGTEST_JAVACARD_VERSION_1_4[] = {(byte) 0x31, (byte) 0x2e, (byte) 0x34};
    /**
     * Version 1.3 (30.11.2014)
     * + Possibility to test single algorithm at single apdu command (possibility for reset in between) via TestSupportedModeSingle()
     * - fixed bug with exact specification of Checksum.getInstance(ALG_ISO3309_CRC16... inside TestSupportedModeSingle
     */
    final static byte ALGTEST_JAVACARD_VERSION_1_3[] = {(byte) 0x31, (byte) 0x2e, (byte) 0x33};
    /**
     * Version 1.2 (3.11.2013)
     * + All relevant constants from JC2.2.2, JC3.0.1 & JC3.0.4 added
     * + Refactoring of exception capture (all try with two catch). Disabled at the moment due to JC conversion error:  Package contains more than 255 exception handlers.
     * + Refactoring of version reporting
     * + Fixed incorrect test during TYPE_RSA_PRIVATE_KEY of LENGTH_RSA_3072 (mistake) of instead of LENGTH_RSA_4096 (correct)
     * + Changed format of values reported in return array. Unused values are now marked as 0xf0 (change from 0x05). 
     *   Supported algorithm is now designated as 0x00 (change from 0x01). When CryptoException is thrown and captured, value of CryptoException is stored (range from 0x01-0x05). 
     */
    final static byte ALGTEST_JAVACARD_VERSION_1_2[] = {(byte) 0x31, (byte) 0x2e, (byte) 0x32};
    /**
     * Version 1.1 (28.6.2013)
     * + information about version added, command for version retrieval
     */
    final static byte ALGTEST_JAVACARD_VERSION_1_1[] = {(byte) 0x31, (byte) 0x2e, (byte) 0x31};
    /**
     * Version 1.0 (2004-2013)
     * + initial version for version-tracking enabled (all features implemented in 2004-2013)
     */
    final static byte ALGTEST_JAVACARD_VERSION_1_0[] = {(byte) 0x31, (byte) 0x2e, (byte) 0x30};

    byte ALGTEST_JAVACARD_VERSION_CURRENT[] = ALGTEST_JAVACARD_VERSION_1_4;

    private   Cipher           m_encryptCipher = null;
    private   Cipher           m_encryptCipherRSA = null;
    private   Signature        m_sign = null;
    private   MessageDigest    m_digest = null;
    private   RandomData       m_random = null;
    private   Object           m_object = null;
    private   KeyPair          m_keyPair = null;
  
  
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
  
    
    // for class 'javacard.security.KeyAgreement'
    public static final byte ALG_EC_SVDP_DH = 1;
    
    final static short EXPONENT_LENGTH = (short) 128;
    final static short MODULUS_LENGTH = (short) 128;
    final static short ADDITIONAL_ARGUMENTS_LENGTH = (short) (ISO7816.OFFSET_CDATA + 4); // two short arguments
    
    final static byte SUPP_ALG_UNTOUCHED = (byte) 0xf0;
    final static byte SUPP_ALG_SUPPORTED = (byte) 0x00;
    final static byte SUPP_ALG_EXCEPTION_CODE_OFFSET = (byte) 0;
    
    
    final static byte SUCCESS =                    (byte) 0xAA;

    final static short SW_STAT_OK                   = (short) 0x9000;
    final static short SW_ALG_TYPE_NOT_SUPPORTED    = (short) 0x6001;
    final static short SW_ALG_OPS_NOT_SUPPORTED     = (short) 0x6002;
    final static short SW_ALG_TYPE_UNKNOWN          = (short) 0x6003;
    
    final static short RAM1_ARRAY_LENGTH = (short) 500;
    
    
    /* Auxiliary variables to choose class - used in APDU as P1 byte. */
    public static final byte CLASS_CIPHER          = 0x11;
    public static final byte CLASS_SIGNATURE       = 0x12;
    public static final byte CLASS_KEYAGREEMENT    = 0x13;
    public static final byte CLASS_MESSAGEDIGEST   = 0x15;
    public static final byte CLASS_RANDOMDATA      = 0x16;
    public static final byte CLASS_CHECKSUM        = 0x17;
    public static final byte CLASS_KEYPAIR         = 0x19;
    public static final byte CLASS_KEYBUILDER      = 0x20;

    
    //
    // Performance testing
    //
    TestSettings    m_testSettings = null;
    
    // class Key 
    AESKey          m_aes_key = null;
    DESKey          m_des_key = null;
    KoreanSEEDKey   m_koreanseed_key = null;
    // TODO:  DSAKey, DSAKeyPrivateKey, DSAPublicKey, ECKey, ECPrivateKey, ECPublicKey, HMACKey, RSAPrivateCrtKey, RSAPrivateKey, RSAPublicKey
    Key             m_key = null;
    
    Cipher          m_cipher = null;
    byte[]          m_ram1 = null;
    

    /**
     * AlgTest default constructor
     * Only this class's install method should create the applet object.
     */
    protected AlgTestSinglePerApdu(byte[] buffer, short offset, byte length)
    {
        // data offset is used for application specific parameter.
        // initialization with default offset (AID offset).
        short dataOffset = offset;
        boolean isOP2 = false;

        if(length > 9) {
            // Install parameter detail. Compliant with OP 2.0.1.

            // | size | content
            // |------|---------------------------
            // |  1   | [AID_Length]
            // | 5-16 | [AID_Bytes]
            // |  1   | [Privilege_Length]
            // | 1-n  | [Privilege_Bytes] (normally 1Byte)
            // |  1   | [Application_Proprietary_Length]
            // | 0-m  | [Application_Proprietary_Bytes]

            // shift to privilege offset
            dataOffset += (short)( 1 + buffer[offset]);
            // finally shift to Application specific offset
            dataOffset += (short)( 1 + buffer[dataOffset]);

            // go to proprietary data
            dataOffset++;

            // update flag
            isOP2 = true;

        } else {
       }

        m_testSettings = new TestSettings();
        
        m_ram1 = JCSystem.makeTransientByteArray(RAM1_ARRAY_LENGTH, JCSystem.CLEAR_ON_RESET);
        
        if (isOP2) { register(buffer, (short)(offset + 1), (byte)buffer[offset]); }
        else { register(); }
    }

    /**
     * Method installing the applet.
     * @param bArray the array constaining installation parameters
     * @param bOffset the starting offset in bArray
     * @param bLength the length in bytes of the data parameter in bArray
     */
    public static void install(byte[] bArray, short bOffset, byte bLength) throws ISOException
    {
        /* applet  instance creation */
        new AlgTestSinglePerApdu (bArray, bOffset, (byte)bLength );
    }

    /**
     * Select method returns true if applet selection is supported.
     * @return boolean status of selection.
     */
    public boolean select()
    {
        return true;
    }

    /**
     * Deselect method called by the system in the deselection process.
     */
    public void deselect()
    {

        // <PUT YOUR DESELECTION ACTION HERE>
    }

    /**
     * Method processing an incoming APDU.
     * @see APDU
     * @param apdu the incoming APDU
     * @exception ISOException with the response bytes defined by ISO 7816-4
     */
    public void process(APDU apdu) throws ISOException
    {
        // get the APDU buffer
        byte[] apduBuffer = apdu.getBuffer();

        // ignore the applet select command dispached to the process
        if (selectingApplet()) { return; }

        if (apduBuffer[ISO7816.OFFSET_CLA] == Consts.CLA_CARD_ALGTEST) {
            switch ( apduBuffer[ISO7816.OFFSET_INS]) {
                case Consts.INS_CARD_GETVERSION: GetVersion(apdu); break;
                case Consts.INS_CARD_TESTAVAILABLE_MEMORY: TestAvailableMemory(apdu); break;
                case Consts.INS_CARD_TESTRSAEXPONENTSET: TestRSAExponentSet(apdu); break;
                case Consts.INS_CARD_JCSYSTEM_INFO: JCSystemInfo(apdu); break;
                case Consts.INS_CARD_TESTSUPPORTEDMODES_SINGLE: TestSupportedModeSingle(apdu); break;
                // case INS_CARD_TESTEXTAPDU: TestExtendedAPDUSupport(apdu); break; // this has to be tested by separate applet with ExtAPDU enabled - should succedd during upload and run
                case Consts.INS_CARD_RESET: JCSystem.requestObjectDeletion(); break;

                    
                case Consts.INS_PREPARE_TEST_CLASS_KEY: prepare_class_Key(apdu); break;        
                case Consts.INS_PREPARE_TEST_CLASS_CIPHER: prepare_class_Cipher(apdu);break;
                case Consts.INS_PERF_TEST_CLASS_KEY: perftest_class_Key(apdu); break;        
                case Consts.INS_PERF_TEST_CLASS_CIPHER: perftest_class_Cipher(apdu); break;        
                    
                case Consts.INS_PERF_TEST_MESSAGE_DIGEST: messageDigestTest(apdu); break;
                case Consts.INS_PERF_TEST_RANDOM_DATA: randomDataTest(apdu); break;
                case Consts.INS_PERF_TEST_KEY_PAIR: keyPairTest(apdu);break;
                case Consts.INS_PERF_TEST_CHECKSUM: checksumTest(apdu);break;
                case Consts.INS_PERF_PREPARE_MESSAGE_DIGEST: prepareMessageDigest(apdu);break;
                case Consts.INS_PERF_PREPARE_SIGNATURE: prepareSignature(apdu);break;
                case Consts.INS_PERF_PREPARE_KEY_PAIR: prepareKeyPair(apdu);break;
                case Consts.INS_PERF_PREPARE_RANDOM_DATA: prepareRandomData(apdu);break;
                case Consts.INS_PERF_TEST_SIGNATURE:signatureTest(apdu);break;
                    
                default : {
                    // The INS code is not supported by the dispatcher
                    ISOException.throwIt( ISO7816.SW_INCORRECT_P1P2  ) ;
                    break;
                }
            }
        }
    }

    void GetVersion(APDU apdu) {
        byte[]    apdubuf = apdu.getBuffer();

        Util.arrayCopyNonAtomic(ALGTEST_JAVACARD_VERSION_CURRENT, (short) 0, apdubuf, (short) 0, (short) ALGTEST_JAVACARD_VERSION_CURRENT.length);

        apdu.setOutgoingAndSend((byte) 0, (short) ALGTEST_JAVACARD_VERSION_CURRENT.length);
    }    
    
    void TestSupportedModeSingle(APDU apdu) {
       byte[]    apdubuf = apdu.getBuffer();

       short     dataLen = apdu.setIncomingAndReceive();
       short     offset = -1;
       
       byte      algorithmClass = apdubuf[ISO7816.OFFSET_CDATA];
       short     algorithmParam1 = Util.makeShort(apdubuf[(short) (ISO7816.OFFSET_CDATA + 1)], apdubuf[(short) (ISO7816.OFFSET_CDATA + 2)]);
       
       Util.arrayFillNonAtomic(apdubuf, ISO7816.OFFSET_CDATA, (short) 240, (byte) SUPP_ALG_UNTOUCHED);
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
             try {offset++;m_key = KeyBuilder.buildKey(algorithmClass, algorithmParam1, false); apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = SUPP_ALG_SUPPORTED;}
             catch (CryptoException e) {apdubuf[(short) (ISO7816.OFFSET_CDATA + offset)] = (byte) (e.getReason() + SUPP_ALG_EXCEPTION_CODE_OFFSET); }
             break;
           }
           case (byte) 0x18: // no break
           case (byte) 0x19: // no break
           case (byte) 0x1C: { // no break
             try {
               offset++;m_keyPair = new KeyPair(algorithmClass, algorithmParam1);
               m_keyPair.genKeyPair();
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
       /**
        * Sends back APDU of length 'offset'
        * APDU of length 240 is too long for no reason
        */
       //apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (short) offset);
    }
    

    void JCSystemInfo(APDU apdu) {
       byte[]    apdubuf = apdu.getBuffer();
       short     dataLen = apdu.setIncomingAndReceive();
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

        apdu.setOutgoingAndSend((byte) 0, offset);
      }
  
   void TestAvailableMemory(APDU apdu) {
       byte[]    apdubuf = apdu.getBuffer();
       short     dataLen = apdu.setIncomingAndReceive();
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
       apdu.setOutgoingAndSend((short) 0, (short) (offset));
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
    void GetRSAKey(APDU apdu) {
      byte[]    apdubuf = apdu.getBuffer();
      //apdu.setIncomingAndReceive();

      // Generate new object if not before yet
      if (m_keyPair == null) {
          m_keyPair = new KeyPair(KeyPair.ALG_RSA_CRT, KeyBuilder.LENGTH_RSA_1024);	  
      }	        

      switch (apdubuf[ISO7816.OFFSET_P1]) {
        case 0: {
            m_keyPair.genKeyPair();           
            m_rsaPublicKey = (RSAPublicKey) m_keyPair.getPublic();

            /*            
            short offset = rsa_PublicKey.getExponent(apdubuf, (short) 0);
            offset += rsa_PublicKey.getModulus(apdubuf, offset);
            */
            
            short offset = 0;
            apdubuf[offset] = (byte)0x82; offset++;
            short len = m_rsaPublicKey.getExponent(apdubuf, (short)(offset + 2));
            Util.setShort(apdubuf, offset, len); 
            offset += 2;    // length
            offset += len;  // value
            
            apdubuf[offset] = (byte)0x82; offset++;
            len = m_rsaPublicKey.getModulus(apdubuf, (short) (offset + 2));
            Util.setShort(apdubuf, offset, len); 
            offset += 2;    // length
            offset += len;  // value

            apdu.setOutgoingAndSend((short) 0, offset);
        
            break;
        }
        case 1: {
            m_rsaPrivateKey = (RSAPrivateCrtKey) m_keyPair.getPrivate();
            
            short offset = 0;
            short len = m_rsaPrivateKey.getP(apdubuf, (short)(offset + 3));
            apdubuf[offset] = (byte)0x82; offset++;
            Util.setShort(apdubuf, offset, len); offset += 2;
            offset += len;
            
            len = m_rsaPrivateKey.getQ(apdubuf, (short)(offset + 3));
            apdubuf[offset] = (byte)0x82; offset++;
            Util.setShort(apdubuf, offset, len); offset += 2;
            offset += len;
                    
            apdu.setOutgoingAndSend((short) 0, offset);
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
   
   
   
    void prepare_class_Key(APDU apdu) {
        byte[] apdubuf = apdu.getBuffer();
        m_testSettings.parse(apdu);  
        
        short len = prepare_Key(apdu, m_testSettings, Consts.FALSE);
        
        apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, len);                
    }
    short prepare_Key(APDU apdu, TestSettings testSet, byte bSetKeyValue) {
        byte[] apdubuf = apdu.getBuffer();
        short offset = ISO7816.OFFSET_CDATA;
        try {
            switch (m_testSettings.algorithmType) {
                case KeyBuilder.TYPE_AES:
                case KeyBuilder.TYPE_AES_TRANSIENT_RESET:
                case KeyBuilder.TYPE_AES_TRANSIENT_DESELECT:
                    m_aes_key = (AESKey) KeyBuilder.buildKey((byte) m_testSettings.algorithmType, m_testSettings.algorithmKeyLength, false);
                    if (bSetKeyValue == Consts.TRUE) {  
                        m_aes_key.setKey(m_ram1, (byte)0); 
                        m_key = m_aes_key;
                    }
                    break;
                case KeyBuilder.TYPE_DES: 
                case KeyBuilder.TYPE_DES_TRANSIENT_RESET: 
                case KeyBuilder.TYPE_DES_TRANSIENT_DESELECT: 
                    m_des_key = (DESKey) KeyBuilder.buildKey((byte) m_testSettings.algorithmType, m_testSettings.algorithmKeyLength, false);
                    if (bSetKeyValue == Consts.TRUE) {  
                        m_des_key.setKey(m_ram1, (byte)0); 
                        m_key = m_des_key;
                    }                    
                    break;
                case KeyBuilder.TYPE_KOREAN_SEED: 
                case KeyBuilder.TYPE_KOREAN_SEED_TRANSIENT_RESET: 
                case KeyBuilder.TYPE_KOREAN_SEED_TRANSIENT_DESELECT: 
                    m_koreanseed_key = (KoreanSEEDKey) KeyBuilder.buildKey((byte) m_testSettings.algorithmType, m_testSettings.algorithmKeyLength, false);
                    if (bSetKeyValue == Consts.TRUE) {  
                        m_koreanseed_key.setKey(m_ram1, (byte)0); 
                        m_key = m_koreanseed_key;
                    } 
                    break;
/* TODO: use custom constants
                case Consts.KeyPair_ALG_RSA:                  
                    m_keyPair = new KeyPair((byte) m_testSettings.algorithmType, m_testSettings.algorithmKeyLength);
                    m_keyPair.genKeyPair(); // TODO: use fixed key value to shorten time required for key generation?
                    m_key = m_keyPair.getPublic(); // TODO: selection of public / private key                  
                    break;
*/                    
                // TODO: DSAKey, DSAKeyPrivateKey, DSAPublicKey, ECKey, ECPrivateKey, ECPublicKey, HMACKey, RSAPrivateCrtKey, RSAPrivateKey, RSAPublicKey
                
                default:
                    ISOException.throwIt(SW_ALG_TYPE_UNKNOWN);
            }

            // If we got here, we were able to sucesfully allocate object
            apdubuf[offset] = SUCCESS; offset++;
            apdubuf[offset] = SUPP_ALG_SUPPORTED; offset++;
        }
        catch (CryptoException e) { 
            apdubuf[offset] = (byte) (e.getReason() + SUPP_ALG_EXCEPTION_CODE_OFFSET); offset++;
        }
        
        return (short) (offset - ISO7816.OFFSET_CDATA);
    }

    void perftest_class_Key(APDU apdu) {
        byte[] apdubuf = apdu.getBuffer();
        m_testSettings.parse(apdu);  
        short repeatWhole = m_testSettings.numRepeatWholeOperation;
        
        // TODO: pack code once correct
        for (short i = 0; i < repeatWhole; i++) { 
            switch (m_testSettings.algorithmType) {
                case KeyBuilder.TYPE_AES:
                    switch (m_testSettings.algorithmMethod) {
                        case Consts.method_setKey: 
                            m_aes_key.setKey(m_ram1, (short) (i % 10)); // i % 10 => different offset to ensure slightly different key every time
                            break;
                        case Consts.method_clearKey:
                                m_aes_key.clearKey(); // BUGBUG: we should set key before clearing (clearing already cleared key may be very fast)
                            break;
                        case Consts.method_getKey:
                            m_aes_key.getKey(m_ram1, (short) 0);
                            break;
                        default:
                            ISOException.throwIt(SW_ALG_OPS_NOT_SUPPORTED);
                    }
                    break;

                    // TODO: DESKey, KoreanSEEDKey, DSAKey, DSAKeyPrivateKey, DSAPublicKey, ECKey, ECPrivateKey, ECPublicKey, HMACKey, RSAPrivateCrtKey, RSAPrivateKey, RSAPublicKey

                default:
                    ISOException.throwIt(SW_ALG_TYPE_NOT_SUPPORTED);
            }
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
            m_cipher.init(m_key, Cipher.MODE_ENCRYPT);  // TODO: selection of Cipher.MODE_ENCRYPT vs. Cipher.MODE_DECRYPT
            apdubuf[(short) (ISO7816.OFFSET_CDATA)] = SUCCESS;
            apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA,(byte)1);
        }
        catch(CryptoException e)
        {
            apdubuf[(short) (ISO7816.OFFSET_CDATA)] = (byte)e.getReason();
            apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte)1);
        }  
    }

    void perftest_class_Cipher(APDU apdu) {  
        byte[] apdubuf = apdu.getBuffer();
        m_testSettings.parse(apdu); 
        // Operation is performed either in single call with (dataLength1)
        //   or multiple times (numRepeatSubOperation) on smaller chunks 
        short chunkDataLen = (short) (m_testSettings.dataLength1 / m_testSettings.numRepeatSubOperation);
        
        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { 
            for (short j = 0; j < m_testSettings.numRepeatSubOperation; j++) { 
                switch (m_testSettings.algorithmMethod) {
                    case Consts.Cipher_update: m_cipher.update(m_ram1, (short) 0, chunkDataLen, m_ram1, (short) 0); break;
                    case Consts.Cipher_doFinal: m_cipher.doFinal(m_ram1, (short) 0, chunkDataLen, m_ram1, (short) 0); break;
                    case Consts.Cipher_init: m_cipher.init(m_key, Cipher.MODE_ENCRYPT); break;
                    default: ISOException.throwIt(SW_ALG_OPS_NOT_SUPPORTED);
                }
            }
        }
/* del       
        switch (m_testSettings.algorithmMethod) {
            case Consts.Cipher_update: 
                for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { 
                    for (short j = 0; j < m_testSettings.numRepeatSubOperation; j++) { 
                        m_cipher.update(m_ram1, (short) 0, chunkDataLen, m_ram1, (short) 0);
                    }
                } 
                break;
            case Consts.Cipher_doFinal: 
                for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { 
                    for (short j = 0; j < m_testSettings.numRepeatSubOperation; j++) { 
                        m_cipher.doFinal(m_ram1, (short) 0, chunkDataLen, m_ram1, (short) 0);
                    }
                } 
                break;
            case Consts.Cipher_init: 
                for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) { 
                    for (short j = 0; j < m_testSettings.numRepeatSubOperation; j++) { 
                        m_cipher.init(m_key, Cipher.MODE_ENCRYPT);  // TODO: selection of Cipher.MODE_ENCRYPT vs. Cipher.MODE_DECRYPT
                    }
                } 
                break;
            default:
                ISOException.throwIt(SW_ALG_OPS_NOT_SUPPORTED);
        }
*/        
        apdubuf[ISO7816.OFFSET_CDATA] = SUCCESS;
        apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte) 1);            
    }
    
    
    
    
    
    
   //
   // TODO: Original codes for performance - to be refactored
   //

    final static short PLTEXT_LENGTH                 = (short) 64; 
    final static short CFTEXT_LENGTH                 = (short) 128;
    final static short KEYTEXT_LENGTH                = (short) 1024; 

    private   DESKey           des_key = null;
    private   Checksum         m_check = null;
    private   AESKey           aes_key = null;

    private byte[] plText, cfText, initVector;
    private byte count;
    private byte alg;
    private short dataLength;
    private short seedLength;
    private short randomLength;
    private short cycles;
    private short rest;   
    void checksumTest(APDU apdu)
    {
        byte[] apdubuf = apdu.getBuffer();
        alg = apdubuf[ISO7816.OFFSET_P1];
        byte infoLength;
        if (alg == Checksum.ALG_ISO3309_CRC16) infoLength = 2; else infoLength = 4;
        count = apdubuf[ISO7816.OFFSET_P2];
        apdu.setIncomingAndReceive();
        dataLength = (short) ((apdubuf[ISO7816.OFFSET_CDATA] << 7) | apdubuf[ISO7816.OFFSET_CDATA +1]);
        cycles = (short)(dataLength / PLTEXT_LENGTH);
        rest = (short)(dataLength % PLTEXT_LENGTH);
        try
        {
            m_check = Checksum.getInstance(alg, false);
            m_check.init(plText, (byte)0, infoLength);
            for(short i = 0;i<count;i++)
            {
                for(short j = 0;j<cycles;j++)
                {
                    m_check.update(plText, (short)0, PLTEXT_LENGTH);
                }
                m_check.doFinal(plText, (short)0, rest, cfText, (short) 0);              
            }
            apdubuf[(short) (ISO7816.OFFSET_CDATA)] = SUCCESS;
            apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA,(byte)1); 
        }
        catch(CryptoException e)
        {
            apdubuf[(short) (ISO7816.OFFSET_CDATA)] = (byte)e.getReason();
            apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA,(byte)1);
        }
    }
    void signatureTest(APDU apdu)
    {
        byte[] apdubuf = apdu.getBuffer();
        count = apdubuf[ISO7816.OFFSET_P2]; 
        apdu.setIncomingAndReceive();
        dataLength = (short) ((apdubuf[ISO7816.OFFSET_CDATA] << 7) | apdubuf[ISO7816.OFFSET_CDATA +1]);
        cycles = (short)(dataLength / (short)PLTEXT_LENGTH);
        rest = (short)(dataLength % (short)PLTEXT_LENGTH);
        try
        {          
            for(short i = 0;i<count;i++)
            {
                for(short j = 0;j<cycles;j++)
                {
                    m_sign.update(plText, (short)0, (short)PLTEXT_LENGTH);
                }
                m_sign.sign(plText, (short)0, rest, plText, (short) 0);              
            }
            apdubuf[(short) (ISO7816.OFFSET_CDATA)] = SUCCESS;
            apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA,(byte)1); 
        }
        catch(CryptoException e)
        {
            apdubuf[(short) (ISO7816.OFFSET_CDATA)] = (byte)e.getReason();
            apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA,(byte)1);
        }
    }
    void prepareSignature(APDU apdu)
    {
        Util.arrayFillNonAtomic(initVector, (short) 0, (short)KEYTEXT_LENGTH, (byte) 0x33);
        byte[] apdubuf = apdu.getBuffer();
        byte keyType = apdubuf[ISO7816.OFFSET_P1];
        byte cipherType = apdubuf[ISO7816.OFFSET_P2];
        apdu.setIncomingAndReceive();
        short keyLength = (short) ((apdubuf[ISO7816.OFFSET_CDATA] << 7) | apdubuf[ISO7816.OFFSET_CDATA +1]);
        try
        {
            switch(keyType)
            {
                case KeyBuilder.TYPE_AES:
                    aes_key = (AESKey)KeyBuilder.buildKey(keyType,keyLength,false);
                    aes_key.setKey(initVector, (byte)0);
                    m_key = aes_key;
                    break;
                case KeyBuilder.TYPE_DES:
                    des_key = (DESKey)KeyBuilder.buildKey(keyType,keyLength,false);
                    des_key.setKey(initVector, (byte)0);
                    m_key = des_key;
                    break;
                case KeyPair.ALG_RSA:
                    m_keyPair = new KeyPair(keyType, keyLength);
                    m_keyPair.genKeyPair();
                    m_key = m_keyPair.getPrivate();
                    break;
                case Consts.MY_DSA:
                    m_keyPair = new KeyPair(KeyPair.ALG_DSA, keyLength);
                    m_keyPair.genKeyPair();
                    m_key = m_keyPair.getPrivate();              
            }
            m_sign = Signature.getInstance(cipherType, false);
            m_sign.init(m_key, Signature.MODE_SIGN);
            apdubuf[(short) (ISO7816.OFFSET_CDATA)] = SUCCESS;
            apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA,(byte)1);
        }
        catch(CryptoException e)
        {
            apdubuf[(short) (ISO7816.OFFSET_CDATA)] = (byte)e.getReason();
            apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA,(byte)1);
        }
    }

    void prepareKeyPair(APDU apdu)
    {
        byte[] apdubuf = apdu.getBuffer();
        alg = apdubuf[ISO7816.OFFSET_P1]; 
        apdu.setIncomingAndReceive();
        short keyLength = (short) ((apdubuf[ISO7816.OFFSET_CDATA] << 7) | apdubuf[ISO7816.OFFSET_CDATA +1]);
        try
        {
            m_keyPair = new KeyPair(alg, keyLength);
        }
        catch(CryptoException e)
        {
            apdubuf[(short) (ISO7816.OFFSET_CDATA)] = (byte)e.getReason();
            apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA,(byte)1);
        }
    }
    void keyPairTest(APDU apdu)
    {
        byte[] apdubuf = apdu.getBuffer();
        try
        {
            m_keyPair.genKeyPair();
        }
        catch(CryptoException e)
        {
            apdubuf[(short) (ISO7816.OFFSET_CDATA)] = (byte)e.getReason();
            apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA,(byte)1);
        }
    }  
    void prepareRandomData(APDU apdu)
    {
        byte[] apdubuf = apdu.getBuffer();
        alg = apdubuf[ISO7816.OFFSET_P1];
        try
        {
            m_random = RandomData.getInstance(alg);
        }
        catch(CryptoException e)
        {
            apdubuf[(short) (ISO7816.OFFSET_CDATA)] = (byte)e.getReason();
            apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA,(byte)1);
        }  
    }
    void randomDataTest(APDU apdu){
        Util.arrayFillNonAtomic(cfText, (short) 0, (short) CFTEXT_LENGTH, (byte) 0x33);
        byte[] apdubuf = apdu.getBuffer();
        alg = apdubuf[ISO7816.OFFSET_P1];
        count = apdubuf[ISO7816.OFFSET_P2];
        apdu.setIncomingAndReceive();
        seedLength = (short) ((apdubuf[ISO7816.OFFSET_CDATA] << 7) | apdubuf[ISO7816.OFFSET_CDATA +1]);
        randomLength = (short) ((apdubuf[ISO7816.OFFSET_CDATA +2] << 7) | apdubuf[ISO7816.OFFSET_CDATA +3]);
        cycles = (short)(randomLength / CFTEXT_LENGTH);
        rest = (short)(randomLength % CFTEXT_LENGTH);
        try
        {
            for(short i=0;i<count;i++)
            {
                if (seedLength != 0)
                {
                    m_random.setSeed(cfText,(short)0,seedLength);
                }
                for(short j = 0;j<cycles;j++)
                {
                    m_random.generateData(cfText,(short) 0,CFTEXT_LENGTH);
                }
                if (rest!=0) m_random.generateData(cfText,(short)0,rest);
            }
            apdubuf[(short) (ISO7816.OFFSET_CDATA)] = SUCCESS;
            apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA,(byte)1);
        }
        catch(CryptoException e)
        {
            apdubuf[(short) (ISO7816.OFFSET_CDATA)] = (byte)e.getReason();
            apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA,(byte)1);
        }            
    }
    void prepareMessageDigest(APDU apdu)
    {
        byte[] apdubuf = apdu.getBuffer();
        alg = apdubuf[ISO7816.OFFSET_P1];
        try
        {
            m_digest = MessageDigest.getInstance(alg, true);
        }
        catch(CryptoException e)
        {
            apdubuf[(short) (ISO7816.OFFSET_CDATA)] = (byte)e.getReason();
            apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA,(byte)1);
        }
    }
    void messageDigestTest(APDU apdu) {
        byte[] apdubuf = apdu.getBuffer();
        alg = apdubuf[ISO7816.OFFSET_P1];
        count = apdubuf[ISO7816.OFFSET_P2];
        apdu.setIncomingAndReceive();
        dataLength = (short) ((apdubuf[ISO7816.OFFSET_CDATA] << 7) | apdubuf[ISO7816.OFFSET_CDATA +1]);
        cycles = (short)(dataLength / PLTEXT_LENGTH);
        rest = (short)(dataLength % PLTEXT_LENGTH);
        try
        {
            for(short i = 0;i<count;i++)
            {
                for(short j = 0;j<cycles;j++)
                {
                    m_digest.update(plText, (short)0, PLTEXT_LENGTH);
                }
                m_digest.doFinal(plText, (short)0, rest, cfText, (short) 0);
            }
            apdubuf[(short) (ISO7816.OFFSET_CDATA)] = SUCCESS;
            apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA,(byte)1);
        }
        catch(CryptoException e)
        {
            apdubuf[(short) (ISO7816.OFFSET_CDATA)] = (byte)e.getReason();
            apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA,(byte)1);
        }
    }   
   
}