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
     * Version 1.3.1 (15.3.2015)
     * + Merged separate javacard applet codes into AlgTestSinglePerApdu.java
     * + Added performance testing from L. Kunikova
     */
    final static byte ALGTEST_JAVACARD_VERSION_1_3_1[] = {(byte) 0x31, (byte) 0x2e, (byte) 0x34};
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

    byte ALGTEST_JAVACARD_VERSION_CURRENT[] = ALGTEST_JAVACARD_VERSION_1_3_1;

    
    final static byte CLA_CARD_ALGTEST               = (byte) 0xB0;
    final static byte INS_CARD_GETVERSION            = (byte) 0x60;
    final static byte INS_CARD_RESET                 = (byte) 0x69;
    
    final static byte INS_CARD_TESTSUPPORTEDMODES    = (byte) 0x70;
    final static byte INS_CARD_TESTAVAILABLE_MEMORY  = (byte) 0x71;
    final static byte INS_CARD_TESTRSAEXPONENTSET    = (byte) 0x72;
    final static byte INS_CARD_JCSYSTEM_INFO         = (byte) 0x73;
    final static byte INS_CARD_TESTEXTAPDU           = (byte) 0x74;
    final static byte INS_CARD_TESTSUPPORTEDMODES_SINGLE    = (byte) 0x75;
    
/*    
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
*/    
    final static byte INS_CARD_PERF_TEST_CLASS_KEY      = (byte) 0x40;

    private   Cipher           m_encryptCipher = null;
    private   Cipher           m_encryptCipherRSA = null;
    private   Signature        m_sign = null;
    private   Key              m_key = null;
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
    
    final static short RAM1_ARRAY_LENGTH = (short) 300;
    
    
    /* Auxiliary variables to choose class - used in APDU as P1 byte. */
    public static final byte CLASS_CIPHER          = 0x11;
    public static final byte CLASS_SIGNATURE       = 0x12;
    public static final byte CLASS_KEYAGREEMENT    = 0x13;
    public static final byte CLASS_MESSAGEDIGEST   = 0x15;
    public static final byte CLASS_RANDOMDATA      = 0x16;
    public static final byte CLASS_CHECKSUM        = 0x17;
    public static final byte CLASS_KEYPAIR         = 0x19;
    public static final byte CLASS_KEYBUILDER      = 0x20;
    
    class TEST_SETTINGS {
        final static short OFFSET_ALGORITHM_CLASS           = ISO7816.OFFSET_CDATA;
        final static short OFFSET_ALGORITHM_SPECIFICATION   = (short) (OFFSET_ALGORITHM_CLASS + 2);
        final static short OFFSET_ALGORITHM_PARAM1          = (short) (OFFSET_ALGORITHM_SPECIFICATION + 2);
        final static short OFFSET_ALGORITHM_PARAM2          = (short) (OFFSET_ALGORITHM_PARAM1 + 2);
        final static short OFFSET_ALGORITHM_TESTED_OPS      = (short) (OFFSET_ALGORITHM_PARAM2 + 2);
        final static short OFFSET_DATA_LENGTH1              = (short) (OFFSET_ALGORITHM_TESTED_OPS + 2);
        final static short OFFSET_DATA_LENGTH2              = (short) (OFFSET_DATA_LENGTH1 + 2);
        final static short OFFSET_NUM_REPEAT_WHOLE_OP       = (short) (OFFSET_DATA_LENGTH2 + 2);
        final static short OFFSET_NUM_REPEAT_SUB_OP         = (short) (OFFSET_NUM_REPEAT_WHOLE_OP + 2);
        
        final static byte method_setKey         = (byte) 1;
        final static byte method_clearKey       = (byte) 2;
        final static byte method_getKey         = (byte) 3;
        
        short       algorithmClass = -1;                // e.g., javacardx.crypto.Cipher
        short       algorithmSpecification = -1;        // e.g., Cipher.ALG_AES_BLOCK_128_CBC_NOPAD
        short       algorithmType = -1;                 // e.g., KeyBuilder.TYPE_AES
        short       algorithmKeyLength = -1;            // e.g., KeyBuilder.LENGTH_AES_128
        short       algorithmOperation = -1;            // e.g., AESKey.setKey() - our custom constant
        short       dataLength1 = -1;                   // e.g., length of data used during measurement (e.g., for update())
        short       dataLength2 = -1;                   // e.g., length of data used during measurement (e.g., for doFinal())
        short       numRepeatWholeOperation = 1;        // whole operation might be setKey, update, doFinal - numRepeatWholeOperation repeats this whole operation
        short       numRepeatSubOperation = 1;          // relevant suboperation that should be iterated multiple times - e.g., update()
        
        void clear() {
            algorithmClass = -1;               
            algorithmSpecification = -1;        
            algorithmType = -1;                 
            algorithmKeyLength = -1;           
            algorithmOperation = -1;           
            dataLength1 = -1;                  
            dataLength2 = -1;                  
            numRepeatWholeOperation = -1;        
            numRepeatSubOperation = -1;          
        }
        void parse(APDU apdu) {
            byte[] apdubuf = apdu.getBuffer();
            short len = apdu.setIncomingAndReceive();

            this.clear();
            
            if (len >= (short) (OFFSET_ALGORITHM_CLASS - ISO7816.OFFSET_CDATA + 2)) { 
                algorithmClass = Util.getShort(apdubuf, OFFSET_ALGORITHM_CLASS);                    
            }
            if (len >= (short) (OFFSET_ALGORITHM_SPECIFICATION - ISO7816.OFFSET_CDATA + 2)) { 
                algorithmSpecification = Util.getShort(apdubuf, OFFSET_ALGORITHM_SPECIFICATION);    
            }
            if (len >= (short) (OFFSET_ALGORITHM_PARAM1 - ISO7816.OFFSET_CDATA + 2)) { 
                algorithmType = Util.getShort(apdubuf, OFFSET_ALGORITHM_PARAM1);                    
            }
            if (len >= (short) (OFFSET_ALGORITHM_PARAM2 - ISO7816.OFFSET_CDATA + 2)) { 
                algorithmKeyLength = Util.getShort(apdubuf, OFFSET_ALGORITHM_PARAM2);               
            }
            if (len >= (short) (OFFSET_ALGORITHM_TESTED_OPS - ISO7816.OFFSET_CDATA + 2)) { 
                algorithmOperation = Util.getShort(apdubuf, OFFSET_ALGORITHM_TESTED_OPS);           
            }
            if (len >= (short) (OFFSET_DATA_LENGTH1 - ISO7816.OFFSET_CDATA + 2)) { 
                dataLength1 = Util.getShort(apdubuf, OFFSET_DATA_LENGTH1);                         
            }
            if (len >= (short) (OFFSET_DATA_LENGTH2 - ISO7816.OFFSET_CDATA + 2)) { 
                dataLength2 = Util.getShort(apdubuf, OFFSET_DATA_LENGTH2);                          
            }
            if (len >= (short) (OFFSET_NUM_REPEAT_WHOLE_OP - ISO7816.OFFSET_CDATA + 2)) { 
                numRepeatWholeOperation = Util.getShort(apdubuf, OFFSET_NUM_REPEAT_WHOLE_OP);  
            }
            if (len >= (short) (OFFSET_NUM_REPEAT_SUB_OP - ISO7816.OFFSET_CDATA + 2)) { 
                numRepeatSubOperation = Util.getShort(apdubuf, OFFSET_NUM_REPEAT_SUB_OP);  
            }
        }
    }
    
    // Performance testing
    TEST_SETTINGS   m_testSettings = null;
    AESKey          m_aes_key = null;
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

        m_testSettings = new TEST_SETTINGS();
        
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

        if (apduBuffer[ISO7816.OFFSET_CLA] == CLA_CARD_ALGTEST) {
            switch ( apduBuffer[ISO7816.OFFSET_INS]) {
                case INS_CARD_GETVERSION: GetVersion(apdu); break;
                case INS_CARD_TESTAVAILABLE_MEMORY: TestAvailableMemory(apdu); break;
                case INS_CARD_TESTRSAEXPONENTSET: TestRSAExponentSet(apdu); break;
                case INS_CARD_JCSYSTEM_INFO: JCSystemInfo(apdu); break;
                case INS_CARD_TESTSUPPORTEDMODES_SINGLE: TestSupportedModeSingle(apdu); break;
                // case INS_CARD_TESTEXTAPDU: TestExtendedAPDUSupport(apdu); break; // this has to be tested by separate applet with ExtAPDU enabled - should succedd during upload and run
                case INS_CARD_PERF_TEST_CLASS_KEY: class_Key_test(apdu); break;        
                case INS_CARD_RESET: JCSystem.requestObjectDeletion(); break;
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
    
    void class_Key_test(APDU apdu) {
        m_testSettings.parse(apdu);  
        byte[] apdubuf = apdu.getBuffer();
        short repeatWhole = m_testSettings.numRepeatWholeOperation;

        switch (m_testSettings.algorithmType) {
            case KeyBuilder.TYPE_AES:
                m_aes_key = (AESKey) KeyBuilder.buildKey((byte) m_testSettings.algorithmType, m_testSettings.algorithmKeyLength, false);

                switch (m_testSettings.algorithmOperation) {
                    case TEST_SETTINGS.method_setKey: 
                        // i % 10 => different offset to ensure slightly different key every time
                        for (short i = 0; i < repeatWhole; i++) { 
                            m_aes_key.setKey(m_ram1, (short) (i % 10)); 
                        } 
                        break;
                    case TEST_SETTINGS.method_clearKey:
                        for (short i = 0; i < repeatWhole; i++) {
                            m_aes_key.clearKey(); // BUGBUG: we should set key before clearing (clearing already cleared key may be very fast)
                        }
                        break;
                    case TEST_SETTINGS.method_getKey:
                        for (short i = 0; i < m_testSettings.numRepeatWholeOperation; i++) {
                            m_aes_key.getKey(m_ram1, (short) 0);
                        }
                        break;
                    default:
                        ISOException.throwIt(SW_ALG_OPS_NOT_SUPPORTED);
                }
                break;
            default:
                ISOException.throwIt(SW_ALG_TYPE_NOT_SUPPORTED);
        }
        
        apdubuf[ISO7816.OFFSET_CDATA] = SUCCESS;
        apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte) 2);            
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
   
    void PerformanceTests(APDU apdu) {
       byte[]    apdubuf = apdu.getBuffer();
       short     dataLen = apdu.setIncomingAndReceive();
       short     offset = (short) 0;

       // TODO:

       switch (apdubuf[ISO7816.OFFSET_P1]) {
         case 1: {
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
}