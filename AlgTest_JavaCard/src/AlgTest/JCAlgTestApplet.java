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
 * Package AID: 4a 43 41 6c 67 54 65 73 74 (4a43416c6754657374)
 * Applet AID:  4a 43 41 6c 67 54 65 73 74 31 (4a43416c675465737431)
 */
package algtest;

/*
 * Imported packages
 */
// specific import for Javacard API access
import javacard.framework.*;
import javacard.security.CryptoException;


// JC 2.2.2 only
//import javacardx.apdu.ExtendedLength; 
//public class AlgTest extends javacard.framework.Applet implements ExtendedLength 

public class JCAlgTestApplet extends javacard.framework.Applet 
{
    // NOTE: when incrementing version, don't forget to update ALGTEST_JAVACARD_VERSION_CURRENT value
     /**
     * Version 1.8.0 (19.12.2020)
     * 
     */
    final static byte ALGTEST_JAVACARD_VERSION_1_8_0__JC222[] = {(byte) 0x31, (byte) 0x2e, (byte) 0x38, (byte) 0x2e, (byte) 0x30, (byte) 0x5f, (byte) 0x6a, (byte) 0x63, (byte) 0x32, (byte) 0x32, (byte) 0x32};
    final static byte ALGTEST_JAVACARD_VERSION_1_8_0__JC304[] = {(byte) 0x31, (byte) 0x2e, (byte) 0x38, (byte) 0x2e, (byte) 0x30, (byte) 0x5f, (byte) 0x6a, (byte) 0x63, (byte) 0x33, (byte) 0x30, (byte) 0x34}; //jc304
    final static byte ALGTEST_JAVACARD_VERSION_1_8_0__JC305[] = {(byte) 0x31, (byte) 0x2e, (byte) 0x38, (byte) 0x2e, (byte) 0x30, (byte) 0x5f, (byte) 0x6a, (byte) 0x63, (byte) 0x33, (byte) 0x30, (byte) 0x35}; //jc305
     /**
     * Version 1.7.10 (17.12.2020)
     * + added testing of modular Cipher and Signature getInstance variants (separate specification of alg, padd, hash)
     * + added option for delayed allocation of resources as some cards cannot handle too many allocations in constructor 
     * + added collection of memory overhead during allocation of cryptographic objects
     * + added support for automatic conversion with different JC versions 
     * - optimized usage 
     */
    //final static byte ALGTEST_JAVACARD_VERSION_1_7_10__JC222[] = {(byte) 0x31, (byte) 0x2e, (byte) 0x37, (byte) 0x2e, (byte) 0x31, (byte) 0x30, (byte) 0x5f, (byte) 0x6a, (byte) 0x63, (byte) 0x32, (byte) 0x32, (byte) 0x32};
    //final static byte ALGTEST_JAVACARD_VERSION_1_7_10__JC304[] = {(byte) 0x31, (byte) 0x2e, (byte) 0x37, (byte) 0x2e, (byte) 0x31, (byte) 0x30, (byte) 0x5f, (byte) 0x6a, (byte) 0x63, (byte) 0x33, (byte) 0x30, (byte) 0x34}; //jc304
    //final static byte ALGTEST_JAVACARD_VERSION_1_7_10__JC305[] = {(byte) 0x31, (byte) 0x2e, (byte) 0x37, (byte) 0x2e, (byte) 0x31, (byte) 0x30, (byte) 0x5f, (byte) 0x6a, (byte) 0x63, (byte) 0x33, (byte) 0x30, (byte) 0x35}; //jc305
     /**
     * Version 1.7.9 (22.07.2019)
     * + added collection of APDU information
     * + improved ECC testing for cards failing at new KeyPair()
     */
    //final static byte ALGTEST_JAVACARD_VERSION_1_7_9[] = {(byte) 0x31, (byte) 0x2e, (byte) 0x37, (byte) 0x2e, (byte) 0x39};
    /**
     * Version 1.7.8 (18.05.2019)
     * + added caching of already generated RSA keys to speedup perf test preparation
     * - fixed bug preventing preparation of performance measurement for RSA_CRT (introduced in version 1.7.0)
     */
    //final static byte ALGTEST_JAVACARD_VERSION_1_7_8[] = {(byte) 0x31, (byte) 0x2e, (byte) 0x37, (byte) 0x2e, (byte) 0x38};
    /**
     * Version 1.7.7 (17.04.2019) 
     * - fixed problem with incorrect reporting for KeyAgreement object
     */
    //final static byte ALGTEST_JAVACARD_VERSION_1_7_7[] = {(byte) 0x31, (byte) 0x2e, (byte) 0x37, (byte) 0x2e, (byte) 0x37};
    /**
     * Version 1.7.6 (12.12.2018) 
     * + added free RAM measurement before all objects allocation
     */
    //final static byte ALGTEST_JAVACARD_VERSION_1_7_6[] = {(byte) 0x31, (byte) 0x2e, (byte) 0x37, (byte) 0x2e, (byte) 0x36};
    /**
     * Version 1.7.5 (20.04.2018) + fixed occasional freeze on some cards when
     * testing MessageDigest performance
     */
    //final static byte ALGTEST_JAVACARD_VERSION_1_7_5[] = {(byte) 0x31, (byte) 0x2e, (byte) 0x37, (byte) 0x2e, (byte) 0x35};

    /**
     * Version 1.7.4 (20.04.2018) + fixed occasional freeze on some cards when
     * testing MessageDigest performance
     */
    //final static byte ALGTEST_JAVACARD_VERSION_1_7_4[] = {(byte) 0x31, (byte) 0x2e, (byte) 0x37, (byte) 0x2e, (byte) 0x34};
    
    /**
     * Version 1.7.3 (10.06.2017) + fixed issue with incorrect test for KeyAgreement support 
     */
    //final static byte ALGTEST_JAVACARD_VERSION_1_7_3[] = {(byte) 0x31, (byte) 0x2e, (byte) 0x37, (byte) 0x2e, (byte) 0x33};
    /**
     * Version 1.7.2 (06.05.2017) 
     * + better support for RSA key collection
     */
    //final static byte ALGTEST_JAVACARD_VERSION_1_7_2[] = {(byte) 0x31, (byte) 0x2e, (byte) 0x37, (byte) 0x2e, (byte) 0x32};
    /**
     * Version 1.7.1 (04.10.2016) 
     * + fixed issue with detection of ECFP-384 curve
     */
    //final static byte ALGTEST_JAVACARD_VERSION_1_7_1[] = {(byte) 0x31, (byte) 0x2e, (byte) 0x37, (byte) 0x2e, (byte) 0x31};
    /**
     * Version 1.7.0 (16.9.2016) 
     * + applet/package AID change (now jcalgtest in hexa)
     * + added working support for ECDH KeyAgreement
     * + EC curves support tests and measurements now works
     * - fixed various issues with measurement of methods for asymmetric crypto algorithms
     * - various refactoring
     */
    //final static byte ALGTEST_JAVACARD_VERSION_1_7_0[] = {(byte) 0x31, (byte) 0x2e, (byte) 0x37, (byte) 0x2e, (byte) 0x30};
    /**
     * Version 1.6.1 (28.1.2016) 
     * Reset of applet moved directly into main JCAlgTestApplet
     */
    //final static byte ALGTEST_JAVACARD_VERSION_1_6_1[] = {(byte) 0x31, (byte) 0x2e, (byte) 0x36, (byte) 0x2e, (byte) 0x31};
    /**
     * Version 1.6.0 (20.7.2015)
     * + source code split into AlgSupportTest and AlgPerformanceTest 
     * + support for RSA encryption in RSA and RSA_CRT (was only RSA)
     * + added test for class Util
     * + added test for software implementation of AES (basically JavaCard code speed test)
     * - fixed minor issues in initialization of engines
     */
    //final static byte ALGTEST_JAVACARD_VERSION_1_6_0[] = {(byte) 0x31, (byte) 0x2e, (byte) 0x36, (byte) 0x2e, (byte) 0x30};
    /**
     * Version 1.5.1 (15.7.2015)
     * + added testing of Cipher/Signature sequence setKey, init, doFinal
     * + added test for different stages of HOTP verification algorithm
     * + added test for XOR speed 
     * - fixed minor issues (byte) in setKey
     */
    //final static byte ALGTEST_JAVACARD_VERSION_1_5_1[] = {(byte) 0x31, (byte) 0x2e, (byte) 0x35, (byte) 0x2e, (byte) 0x31};    
    /**
     * Version 1.5 (30.6.2015)
     * + added external setting of init mode for Cipher
     * + added improved clerKey testing
     * + added key alteration for init() methods
     * + added valid signature before verification 
     * - fixed bugs in tests (i = 10 instead of i % 10), improper breaks...
     */
    //final static byte ALGTEST_JAVACARD_VERSION_1_5[] = {(byte) 0x31, (byte) 0x2e, (byte) 0x35};
    /**
     * Version 1.4 (15.3.2015)
     * + Merged separate javacard applet codes into AlgTestSinglePerApdu.java
     * + Added performance testing from L. Kunikova
     */
    //final static byte ALGTEST_JAVACARD_VERSION_1_4[] = {(byte) 0x31, (byte) 0x2e, (byte) 0x34};
    /**
     * Version 1.3 (30.11.2014)
     * + Possibility to test single algorithm at single apdu command (possibility for reset in between) via TestSupportedModeSingle()
     * - fixed bug with exact specification of Checksum.getInstance(ALG_ISO3309_CRC16... inside TestSupportedModeSingle
     */
    //final static byte ALGTEST_JAVACARD_VERSION_1_3[] = {(byte) 0x31, (byte) 0x2e, (byte) 0x33};
    /**
     * Version 1.2 (3.11.2013)
     * + All relevant constants from JC2.2.2, JC3.0.1 & JC3.0.4 added
     * + Refactoring of exception capture (all try with two catch). Disabled at the moment due to JC conversion error:  Package contains more than 255 exception handlers.
     * + Refactoring of version reporting
     * + Fixed incorrect test during TYPE_RSA_PRIVATE_KEY of LENGTH_RSA_3072 (mistake) of instead of LENGTH_RSA_4096 (correct)
     * + Changed format of values reported in return array. Unused values are now marked as 0xf0 (change from 0x05). 
     *   Supported algorithm is now designated as 0x00 (change from 0x01). When CryptoException is thrown and captured, value of CryptoException is stored (range from 0x01-0x05). 
     */
    //final static byte ALGTEST_JAVACARD_VERSION_1_2[] = {(byte) 0x31, (byte) 0x2e, (byte) 0x32};
    /**
     * Version 1.1 (28.6.2013)
     * + information about version added, command for version retrieval
     */
    //final static byte ALGTEST_JAVACARD_VERSION_1_1[] = {(byte) 0x31, (byte) 0x2e, (byte) 0x31};
    /**
     * Version 1.0 (2004-2013)
     * + initial version for version-tracking enabled (all features implemented in 2004-2013)
     */
    //final static byte ALGTEST_JAVACARD_VERSION_1_0[] = {(byte) 0x31, (byte) 0x2e, (byte) 0x30};

    byte[] ALGTEST_JAVACARD_VERSION_CURRENT = null; // Note: change assignemnt in applet constructor
    
    // lower byte of exception is value as defined in JCSDK/api_classic/constant-values.htm
    final static short SW_Exception = (short) 0xff01;
    final static short SW_ArrayIndexOutOfBoundsException = (short) 0xff02;
    final static short SW_ArithmeticException = (short) 0xff03;
    final static short SW_ArrayStoreException = (short) 0xff04;
    final static short SW_NullPointerException = (short) 0xff05;
    final static short SW_NegativeArraySizeException = (short) 0xff06;
    final static short SW_CryptoException_prefix = (short) 0xf100;
    final static short SW_SystemException_prefix = (short) 0xf200;
    final static short SW_PINException_prefix = (short) 0xf300;
    final static short SW_TransactionException_prefix = (short) 0xf400;
    final static short SW_CardRuntimeException_prefix = (short) 0xf500;    
    
    AlgKeyHarvest       m_keyHarvest = null;
    AlgSupportTest      m_supportTest = null;
    AlgPerformanceTest  m_perfTest = null;
    AlgStorageTest      m_storageTest = null;

    short m_freeRAMReset = 0;
    short m_freeRAMDeselect = 0;
    short[] m_freeEEPROM = null;
    
    public final static short RAM1_ARRAY_LENGTH = (short) 600;
    public final static short RAM2_ARRAY_LENGTH = (short) 528;
    byte[] m_ramArray = null;  // auxalarity array used for various purposes. Length of this array is added to value returned as amount of available RAM memory
    byte[] m_ramArray2 = null;  // auxalarity array used for various purposes. Length of this array is added to value returned as amount of available RAM memory
    
    boolean m_allocationsPerformed = false;
    
    protected JCAlgTestApplet(byte[] buffer, short offset, byte length) {
        ALGTEST_JAVACARD_VERSION_CURRENT = ALGTEST_JAVACARD_VERSION_1_8_0__JC222;
        ALGTEST_JAVACARD_VERSION_CURRENT = ALGTEST_JAVACARD_VERSION_1_8_0__JC304; //jc304
        ALGTEST_JAVACARD_VERSION_CURRENT = ALGTEST_JAVACARD_VERSION_1_8_0__JC305; //jc305

        // data offset is used for application specific parameter.
        // initialization with default offset (AID offset).
        short dataOffset = offset;
        boolean isOP2 = false;
        boolean bPerformAllocationsNow = true;

        if(length > 9) {
            // Install parameter detail. Compliant with OP 2.0.1.
            // shift to privilege offset
            dataOffset += (short)( 1 + buffer[offset]);
            // finally shift to Application specific offset
            dataOffset += (short)( 1 + buffer[dataOffset]);

            // go to proprietary data
            dataOffset++;
            
            // Note: NXP JCOP4 J3R180 card fails with TransactionException.BUFFER_FULL exception
            // if some transaction is executed in constructor or too many allocations are performed.
            if (buffer[dataOffset] == Consts.TAG_DELAYED_ALLOCATION) {
                // Do not allocate now, delay for the first run
                bPerformAllocationsNow = false;
            }

                
            // update flag
            isOP2 = true;
       } else {}
        
        // Save free RAM before allocation of objects
        m_freeRAMReset = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_RESET);
        m_freeRAMDeselect = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT);
        m_freeEEPROM = new short[2];
        JCSystem.getAvailableMemory(m_freeEEPROM, (short) 0, JCSystem.MEMORY_TYPE_PERSISTENT); //jc304

        if (bPerformAllocationsNow) {
            allocateResources();
        }
        
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
    
    void allocateResources() {
        // Allocate all engines
        m_ramArray = JCSystem.makeTransientByteArray(RAM1_ARRAY_LENGTH, JCSystem.CLEAR_ON_RESET);
        m_ramArray2 = JCSystem.makeTransientByteArray(RAM2_ARRAY_LENGTH, JCSystem.CLEAR_ON_RESET);
    
        m_supportTest = new AlgSupportTest(m_ramArray, m_ramArray2, m_freeRAMReset, m_freeRAMDeselect, m_freeEEPROM);
        m_keyHarvest = new AlgKeyHarvest();
        m_perfTest = new AlgPerformanceTest(m_ramArray, m_ramArray2);
        m_storageTest = new AlgStorageTest();

        m_allocationsPerformed = true;
    }
    
    void performDelayedAllocations() {
        if (!m_allocationsPerformed) {
            allocateResources();
        }
    }

    public void process(APDU apdu) throws ISOException {
        // get the APDU buffer
        byte[] apduBuffer = apdu.getBuffer();

        // ignore the applet select command dispached to the process
        if (selectingApplet()) { return; }
        
        performDelayedAllocations();
        
        byte bProcessed = (byte) 0;
        
        try {
            // Serve get version
            if (apduBuffer[ISO7816.OFFSET_CLA] == Consts.CLA_CARD_ALGTEST) {
                if (apduBuffer[ISO7816.OFFSET_INS] == Consts.INS_CARD_GETVERSION) {
                    GetVersion(apdu); 
                    bProcessed = (byte) 1;
                }
                if (apduBuffer[ISO7816.OFFSET_INS] == Consts.INS_CARD_RESET) {
                    JCSystem.requestObjectDeletion();
                    if (apduBuffer[ISO7816.OFFSET_P1] == Consts.P1_CARD_RESET_FREE_CACHE) {
                        // If required, free also RSA objects cache to free some resources
                        m_perfTest.eraseCachedRSAObjectsExceptSpecifiedTypeLength((byte) -1, (short) -1);                        
                    }
                    bProcessed = (byte) 1;
                }
            }

            if (bProcessed == 0) {
                bProcessed = m_supportTest.process(apdu);
            }
            if (bProcessed == 0) {
                bProcessed = m_keyHarvest.process(apdu);
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
            // Capture all reasonable exceptions and change into readable ones (instead of 0x6f00) 
        } catch (ISOException e) {
            throw e; // Our exception from code, just re-emit
        } catch (ArrayIndexOutOfBoundsException e) {
            ISOException.throwIt(SW_ArrayIndexOutOfBoundsException);
        } catch (ArithmeticException e) {
            ISOException.throwIt(SW_ArithmeticException);
        } catch (ArrayStoreException e) {
            ISOException.throwIt(SW_ArrayStoreException);
        } catch (NullPointerException e) {
            ISOException.throwIt(SW_NullPointerException);
        } catch (NegativeArraySizeException e) {
            ISOException.throwIt(SW_NegativeArraySizeException);
        } catch (CryptoException e) {
            ISOException.throwIt((short) (SW_CryptoException_prefix | e.getReason()));
        } catch (SystemException e) {
            ISOException.throwIt((short) (SW_SystemException_prefix | e.getReason()));
        } catch (PINException e) {
            ISOException.throwIt((short) (SW_PINException_prefix | e.getReason()));
        } catch (TransactionException e) {
            ISOException.throwIt((short) (SW_TransactionException_prefix | e.getReason()));
        } catch (CardRuntimeException e) {
            ISOException.throwIt((short) (SW_CardRuntimeException_prefix | e.getReason()));
        } catch (Exception e) {
            ISOException.throwIt(SW_Exception);
        }        
    }

    void GetVersion(APDU apdu) {
        byte[]    apdubuf = apdu.getBuffer();
        apdu.setIncomingAndReceive();

        Util.arrayCopyNonAtomic(ALGTEST_JAVACARD_VERSION_CURRENT, (short) 0, apdubuf, (short) 0, (short) ALGTEST_JAVACARD_VERSION_CURRENT.length);

        apdu.setOutgoingAndSend((byte) 0, (short) ALGTEST_JAVACARD_VERSION_CURRENT.length);
    }    
}