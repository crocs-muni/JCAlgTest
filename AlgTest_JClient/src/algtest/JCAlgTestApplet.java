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


// JC 2.2.2 only
//import javacardx.apdu.ExtendedLength; 
//public class AlgTest extends javacard.framework.Applet implements ExtendedLength 

public class JCAlgTestApplet extends javacard.framework.Applet 
{
    // NOTE: when incrementing version, don't forget to update ALGTEST_JAVACARD_VERSION_CURRENT value
    /**
     * Version 1.6.0 (20.7.2015)
     * + source code split into AlgSupportTest and AlgPerformanceTest 
     * + support for RSA encryption in RSA and RSA_CRT (was only RSA)
     * + added test for class Util
     * + added test for software implementation of AES (basically JavaCard code speed test)
     * - fixed minor issues in initialization of engines
     */
    final static byte ALGTEST_JAVACARD_VERSION_1_6_0[] = {(byte) 0x31, (byte) 0x2e, (byte) 0x36, (byte) 0x2e, (byte) 0x30};
    /**
     * Version 1.5.1 (15.7.2015)
     * + added testing of Cipher/Signature sequence setKey, init, doFinal
     * + added test for different stages of HOTP verification algorithm
     * + added test for XOR speed 
     * - fixed minor issues (byte) in setKey
     */
    final static byte ALGTEST_JAVACARD_VERSION_1_5_1[] = {(byte) 0x31, (byte) 0x2e, (byte) 0x35, (byte) 0x2e, (byte) 0x31};    
    /**
     * Version 1.5 (30.6.2015)
     * + added external setting of init mode for Cipher
     * + added improved clerKey testing
     * + added key alteration for init() methods
     * + added valid signature before verification 
     * - fixed bugs in tests (i = 10 instead of i % 10), improper breaks...
     */
    final static byte ALGTEST_JAVACARD_VERSION_1_5[] = {(byte) 0x31, (byte) 0x2e, (byte) 0x35};
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

    byte ALGTEST_JAVACARD_VERSION_CURRENT[] = ALGTEST_JAVACARD_VERSION_1_6_0;

    AlgKeyHarvest       m_keyHarvest = null;
    AlgSupportTest      m_supportTest = null;
    AlgPerformanceTest  m_perfTest = null;
    AlgStorageTest      m_storageTest = null;

    protected JCAlgTestApplet(byte[] buffer, short offset, byte length) {
        // data offset is used for application specific parameter.
        // initialization with default offset (AID offset).
        short dataOffset = offset;
        boolean isOP2 = false;

        if(length > 9) {
            // Install parameter detail. Compliant with OP 2.0.1.
            // shift to privilege offset
            dataOffset += (short)( 1 + buffer[offset]);
            // finally shift to Application specific offset
            dataOffset += (short)( 1 + buffer[dataOffset]);

            // go to proprietary data
            dataOffset++;
            // update flag
            isOP2 = true;
       } else {}

        m_keyHarvest = new AlgKeyHarvest();
        m_supportTest = new AlgSupportTest();
        m_perfTest = new AlgPerformanceTest();
        m_storageTest = new AlgStorageTest();
        
        if (isOP2) { register(buffer, (short)(offset + 1), buffer[offset]); }
        else { register(); }
    }

    public static void install(byte[] bArray, short bOffset, byte bLength) throws ISOException {
        new JCAlgTestApplet (bArray, bOffset, bLength );
    }

    public boolean select() {
        return true;
    }

    public void deselect() {
    }

    public void process(APDU apdu) throws ISOException {
        // get the APDU buffer
        byte[] apduBuffer = apdu.getBuffer();

        // ignore the applet select command dispached to the process
        if (selectingApplet()) { return; }
        
        byte bProcessed = (byte) 0;
        
        // Serve get version
        if ((apduBuffer[ISO7816.OFFSET_CLA] == Consts.CLA_CARD_ALGTEST) && 
            (apduBuffer[ISO7816.OFFSET_INS] == Consts.INS_CARD_GETVERSION)) {
                GetVersion(apdu); 
                bProcessed = (byte) 1;
        }

        if (bProcessed == 0) {
            bProcessed = m_keyHarvest.process(apdu);
        }
        if (bProcessed == 0) {
            bProcessed = m_supportTest.process(apdu);
        }
        if (bProcessed == 0) {
            bProcessed = m_perfTest.process(apdu);
        }
        if (bProcessed == 0) {
            bProcessed = m_storageTest.process(apdu);
        }
        
        
        // If not processed by any of module, then emit exception
        if (bProcessed == 0) {
            ISOException.throwIt( ISO7816.SW_INS_NOT_SUPPORTED) ;
        }
    }

    void GetVersion(APDU apdu) {
        byte[]    apdubuf = apdu.getBuffer();
        apdu.setIncomingAndReceive();

        Util.arrayCopyNonAtomic(ALGTEST_JAVACARD_VERSION_CURRENT, (short) 0, apdubuf, (short) 0, (short) ALGTEST_JAVACARD_VERSION_CURRENT.length);

        apdu.setOutgoingAndSend((byte) 0, (short) ALGTEST_JAVACARD_VERSION_CURRENT.length);
    }    
}