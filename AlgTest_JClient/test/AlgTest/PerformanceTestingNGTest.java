package AlgTest;

import static AlgTest.AlgTestSinglePerApdu.CLASS_SIGNATURE;
import static AlgTest.Consts.ALG_AES_BLOCK_128_CBC_NOPAD;
import static AlgTest.Consts.ALG_AES_MAC_128_NOPAD;
import static AlgTest.Consts.CLASS_CIPHER;
import static AlgTest.Consts.Cipher_doFinal;
import static AlgTest.Consts.Cipher_init;
import static AlgTest.Consts.Cipher_update;
import static AlgTest.Consts.LENGTH_AES_128;
import static AlgTest.Consts.Signature_sign;
import static AlgTest.Consts.Signature_update;
import static AlgTest.Consts.TEST_DATA_LENGTH;
import static AlgTest.Consts.TYPE_AES;
import static AlgTest.Consts.UNUSED;
import static AlgTest.Consts.method_setKey;
import static algtestjclient.CardMngr.CLASS_KEYBUILDER;
import algtestjclient.PerformanceTesting;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;

import static algtestjclient.PerformanceTesting.cardManager;
import java.io.FileOutputStream;
import static java.lang.System.out;
import java.lang.reflect.*;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author xsvenda
 */
public class PerformanceTestingNGTest {
    
    public PerformanceTestingNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        if (PerformanceTesting.file != null) PerformanceTesting.file.close();
    }
    

    @Test
    void perftest_testClass_AESKey() throws Exception {
        // Prepare connection to simulated card
//        FileOutputStream file = cardManager.establishConnection(AlgTestSinglePerApdu.class);
        FileOutputStream file = cardManager.establishConnection(null);
        PerformanceTesting.file = file;
        assertNotEquals(file, null);

        // Prepare test
        TestSettings testSet = null;
        testSet = PerformanceTesting.prepareTestSettings(CLASS_KEYBUILDER, UNUSED, TYPE_AES, LENGTH_AES_128, method_setKey, 
                UNUSED, UNUSED, (short) 1, UNUSED, (short) 1);      

        //PerformanceTesting.perftest_prepareClass(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, testSet);
        
        testSet.numRepeatWholeOperation = 100;
        testSet.numRepeatSubOperation = 1;
        testSet.numRepeatWholeMeasurement = 5;
        testSet.algorithmMethod = method_setKey;
        assertTrue(PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, "AESKey TYPE_AES LENGTH_AES_128 setKey()") > -1);
        testSet.algorithmMethod = Consts.method_getKey;
        assertTrue(PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, "AESKey TYPE_AES LENGTH_AES_128 getKey()") > -1);
        testSet.algorithmMethod = Consts.method_clearKey;
        assertTrue(PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, "AESKey TYPE_AES LENGTH_AES_128 clearKey()") > -1);
        
        testSet.numRepeatWholeOperation = 3;
        testSet.algorithmMethod = method_setKey;
        assertTrue(PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, "AESKey TYPE_AES LENGTH_AES_128 setKey()") > -1);
        testSet.algorithmMethod = Consts.method_getKey;
        assertTrue(PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, "AESKey TYPE_AES LENGTH_AES_128 getKey()") > -1);
        testSet.algorithmMethod = Consts.method_clearKey;
        assertTrue(PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, Consts.INS_PERF_TEST_CLASS_KEY, testSet, "AESKey TYPE_AES LENGTH_AES_128 clearKey()") > -1);
        
        file.close();
        
        // BUGBUG: add check for Exception in output
    }
    
    
    @Test
    void perftest_testClass_Cipher() throws Exception {
        // Prepare connection to simulated card
//        FileOutputStream file = cardManager.establishConnection(AlgTestSinglePerApdu.class);
        FileOutputStream file = cardManager.establishConnection(null);
        PerformanceTesting.file = file;
        assertNotEquals(file, null);

        // Prepare test
        TestSettings testSet = null;
        testSet = PerformanceTesting.prepareTestSettings(CLASS_CIPHER, ALG_AES_BLOCK_128_CBC_NOPAD, TYPE_AES, LENGTH_AES_128, Cipher_update, 
                TEST_DATA_LENGTH, UNUSED, (short) 1, (short) 1, (short) 1);      

        
        //PerformanceTesting.perftest_prepareClass(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CIPHER, testSet);
        
        testSet.numRepeatWholeMeasurement = 2;
        testSet.numRepeatWholeOperation = 100;
        testSet.numRepeatSubOperation = 1;
        testSet.algorithmMethod = Cipher_update;
        assertTrue(PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CIPHER, Consts.INS_PERF_TEST_CLASS_CIPHER, testSet, "Cipher ALG_AES_BLOCK_128_CBC_NOPAD LENGTH_AES_128 Cipher_update()") > -1);
        testSet.algorithmMethod = Cipher_doFinal;
        assertTrue(PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CIPHER, Consts.INS_PERF_TEST_CLASS_CIPHER, testSet, "Cipher ALG_AES_BLOCK_128_CBC_NOPAD LENGTH_AES_128 Cipher_doFinal()") > -1);
        testSet.algorithmMethod = Cipher_init;
        assertTrue(PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CIPHER, Consts.INS_PERF_TEST_CLASS_CIPHER, testSet, "Cipher ALG_AES_BLOCK_128_CBC_NOPAD LENGTH_AES_128 Cipher_init()") > -1);

        testSet.numRepeatWholeOperation = 3;
        testSet.algorithmMethod = Cipher_update;
        assertTrue(PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CIPHER, Consts.INS_PERF_TEST_CLASS_CIPHER, testSet, "Cipher ALG_AES_BLOCK_128_CBC_NOPAD LENGTH_AES_128 Cipher_update()") > -1);
        testSet.algorithmMethod = Cipher_doFinal;
        assertTrue(PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CIPHER, Consts.INS_PERF_TEST_CLASS_CIPHER, testSet, "Cipher ALG_AES_BLOCK_128_CBC_NOPAD LENGTH_AES_128 Cipher_doFinal()") > -1);
        testSet.algorithmMethod = Cipher_init;
        assertTrue(PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CIPHER, Consts.INS_PERF_TEST_CLASS_CIPHER, testSet, "Cipher ALG_AES_BLOCK_128_CBC_NOPAD LENGTH_AES_128 Cipher_init()") > -1);
        
/*        
        // Test operation on splitted chunks of data (256 / 8)
        testSet.numRepeatSubOperation = 8;
        testSet.algorithmMethod = Cipher_update;
        PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CIPHER, Consts.INS_PERF_TEST_CLASS_CIPHER, testSet, "Cipher ALG_AES_BLOCK_128_CBC_NOPAD LENGTH_AES_128 8x Cipher_update()");
        testSet.algorithmMethod = Cipher_doFinal;
        PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CIPHER, Consts.INS_PERF_TEST_CLASS_CIPHER, testSet, "Cipher ALG_AES_BLOCK_128_CBC_NOPAD LENGTH_AES_128 8x Cipher_doFinal()");
        testSet.algorithmMethod = Cipher_init;
        PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CIPHER, Consts.INS_PERF_TEST_CLASS_CIPHER, testSet, "Cipher ALG_AES_BLOCK_128_CBC_NOPAD LENGTH_AES_128 8x Cipher_init()");
*/        
    }    

    @Test
    void perftest_testClass_Signature() throws Exception {
        // Prepare connection to simulated card
        //FileOutputStream file = cardManager.establishConnection(AlgTestSinglePerApdu.class);
        FileOutputStream file = cardManager.establishConnection(null);

        PerformanceTesting.file = file;
        assertNotEquals(file, null);

        // Prepare test
        TestSettings testSet = null;
        testSet = PerformanceTesting.prepareTestSettings(CLASS_SIGNATURE, ALG_AES_MAC_128_NOPAD, TYPE_AES, LENGTH_AES_128, Signature_sign, 
                TEST_DATA_LENGTH, UNUSED, (short) 1, (short) 1, (short) 1);      

        //PerformanceTesting.perftest_prepareClass(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_SIGNATURE, testSet);
        
        // Test single execution of operation
        testSet.numRepeatWholeMeasurement = 2;
        testSet.numRepeatWholeOperation = 100;
        testSet.numRepeatSubOperation = 1;
        testSet.algorithmMethod = Consts.Signature_update;
        assertTrue(PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_SIGNATURE, Consts.INS_PERF_TEST_CLASS_SIGNATURE, testSet, "Cipher ALG_AES_MAC_128_NOPAD LENGTH_AES_128 Signature_update()") > -1);
        testSet.algorithmMethod = Signature_sign;
        assertTrue(PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_SIGNATURE, Consts.INS_PERF_TEST_CLASS_SIGNATURE, testSet, "Cipher ALG_AES_MAC_128_NOPAD LENGTH_AES_128 Signature_sign()") > -1);
        testSet.algorithmMethod = Consts.Signature_init;
        assertTrue(PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_SIGNATURE, Consts.INS_PERF_TEST_CLASS_SIGNATURE, testSet, "Cipher ALG_AES_MAC_128_NOPAD LENGTH_AES_128 Signature_init()") > -1);
        testSet.algorithmMethod = Consts.Signature_verify;
        assertTrue(PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_SIGNATURE, Consts.INS_PERF_TEST_CLASS_SIGNATURE, testSet, "Cipher ALG_AES_MAC_128_NOPAD LENGTH_AES_128 Signature_verify()") > -1);

        testSet.numRepeatWholeOperation = 3;
        testSet.algorithmMethod = Consts.Signature_update;
        assertTrue(PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_SIGNATURE, Consts.INS_PERF_TEST_CLASS_SIGNATURE, testSet, "Cipher ALG_AES_MAC_128_NOPAD LENGTH_AES_128 Signature_update()") > -1);
        testSet.algorithmMethod = Signature_sign;
        assertTrue(PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_SIGNATURE, Consts.INS_PERF_TEST_CLASS_SIGNATURE, testSet, "Cipher ALG_AES_MAC_128_NOPAD LENGTH_AES_128 Signature_sign()") > -1);
        testSet.algorithmMethod = Consts.Signature_init;
        assertTrue(PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_SIGNATURE, Consts.INS_PERF_TEST_CLASS_SIGNATURE, testSet, "Cipher ALG_AES_MAC_128_NOPAD LENGTH_AES_128 Signature_init()") > -1);
        testSet.algorithmMethod = Consts.Signature_verify;
        assertTrue(PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_SIGNATURE, Consts.INS_PERF_TEST_CLASS_SIGNATURE, testSet, "Cipher ALG_AES_MAC_128_NOPAD LENGTH_AES_128 Signature_verify()") > -1);
    
    }
    
    @Test
    void perftest_testClass_RandomData() throws Exception {
        // Prepare connection to simulated card
        PerformanceTesting.file = cardManager.establishConnection(AlgTestSinglePerApdu.class);
        //PerformanceTesting.file = cardManager.establishConnection(null);
        assertNotEquals(PerformanceTesting.file, null);

        // Prepare test
        TestSettings testSet = null;
        testSet = PerformanceTesting.prepareTestSettings(Consts.CLASS_RANDOMDATA, Consts.ALG_SECURE_RANDOM, UNUSED, UNUSED, Consts.RandomData_generateData, 
                TEST_DATA_LENGTH, UNUSED, (short) 1, (short) 1, (short) 1);      

        // Test single execution of operation
        testSet.numRepeatWholeMeasurement = 2;
        testSet.numRepeatWholeOperation = 100;
        testSet.numRepeatSubOperation = 1;
        testSet.algorithmMethod = Consts.RandomData_generateData;
        assertTrue(PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_RANDOMDATA, Consts.INS_PERF_TEST_CLASS_RANDOMDATA, testSet, "RandomData ALG_SECURE_RANDOM RandomData_generateData()") > -1);
        testSet.algorithmMethod = Consts.RandomData_setSeed;
        assertTrue(PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_RANDOMDATA, Consts.INS_PERF_TEST_CLASS_RANDOMDATA, testSet, "RandomData ALG_SECURE_RANDOM RandomData_setSeed()") > -1);

        testSet.algorithmSpecification = Consts.ALG_PSEUDO_RANDOM;
        testSet.algorithmMethod = Consts.RandomData_generateData;
        assertTrue(PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_RANDOMDATA, Consts.INS_PERF_TEST_CLASS_RANDOMDATA, testSet, "RandomData ALG_PSEUDO_RANDOM RandomData_generateData()") > -1);
        testSet.algorithmMethod = Consts.RandomData_setSeed;
        assertTrue(PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_RANDOMDATA, Consts.INS_PERF_TEST_CLASS_RANDOMDATA, testSet, "RandomData ALG_PSEUDO_RANDOM RandomData_setSeed()") > -1);
    }    
    
   @Test
    void perftest_testClass_MessageDigest() throws Exception {
        // Prepare connection to simulated card
        PerformanceTesting.file = cardManager.establishConnection(AlgTestSinglePerApdu.class);
        //PerformanceTesting.file = cardManager.establishConnection(null);
        assertNotEquals(PerformanceTesting.file, null);

        // BUGBUG: other types from MessageDigest
        
        // Prepare test
        TestSettings testSet = null;
        testSet = PerformanceTesting.prepareTestSettings(Consts.CLASS_MESSAGEDIGEST, Consts.ALG_SHA, Consts.ALG_SHA, UNUSED, Consts.MessageDigest_update, 
                TEST_DATA_LENGTH, UNUSED, (short) 1, (short) 1, (short) 1);      

        // Test single execution of operation
        testSet.numRepeatWholeMeasurement = 2;
        testSet.numRepeatWholeOperation = 100;
        testSet.numRepeatSubOperation = 1;
        testSet.algorithmMethod = Consts.MessageDigest_update;
        assertTrue(PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_MESSAGEDIGEST, Consts.INS_PERF_TEST_CLASS_MESSAGEDIGEST, testSet, "MessageDigest ALG_SHA MessageDigest_update()") > -1);
        testSet.algorithmMethod = Consts.MessageDigest_doFinal;
        assertTrue(PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_MESSAGEDIGEST, Consts.INS_PERF_TEST_CLASS_MESSAGEDIGEST, testSet, "MessageDigest ALG_SHA MessageDigest_doFinal()") > -1);
        testSet.algorithmMethod = Consts.MessageDigest_reset;
        assertTrue(PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_MESSAGEDIGEST, Consts.INS_PERF_TEST_CLASS_MESSAGEDIGEST, testSet, "MessageDigest ALG_SHA MessageDigest_reset()") > -1);

    }    
    
   @Test
    void perftest_testClass_Checksum() throws Exception {
        // Prepare connection to simulated card
        PerformanceTesting.file = cardManager.establishConnection(AlgTestSinglePerApdu.class);
        //PerformanceTesting.file = cardManager.establishConnection(null);
        assertNotEquals(PerformanceTesting.file, null);

        // BUGBUG: other types from MessageDigest
        
        // Prepare test
        TestSettings testSet = null;
        testSet = PerformanceTesting.prepareTestSettings(Consts.CLASS_CHECKSUM, Consts.ALG_ISO3309_CRC16, Consts.ALG_ISO3309_CRC16, UNUSED, Consts.Checksum_update, 
                TEST_DATA_LENGTH, UNUSED, (short) 1, (short) 1, (short) 1);      
        // BUGBUG: ALG_ISO3309_CRC32
        
        // Test single execution of operation
        testSet.numRepeatWholeMeasurement = 2;
        testSet.numRepeatWholeOperation = 100;
        testSet.numRepeatSubOperation = 1;
        testSet.algorithmMethod = Consts.Checksum_update;
        assertTrue(PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CHECKSUM, Consts.INS_PERF_TEST_CLASS_CHECKSUM, testSet, "Checksum ALG_ISO3309_CRC16 Checksum_update()") > -1);
        testSet.algorithmMethod = Consts.Checksum_doFinal;
        assertTrue(PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CHECKSUM, Consts.INS_PERF_TEST_CLASS_CHECKSUM, testSet, "Checksum ALG_ISO3309_CRC16 Checksum_doFinal()") > -1);

        testSet.numRepeatSubOperation = 1;
        testSet.algorithmType = testSet.algorithmSpecification = Consts.ALG_ISO3309_CRC32;
        testSet.algorithmMethod = Consts.Checksum_update;
        assertTrue(PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CHECKSUM, Consts.INS_PERF_TEST_CLASS_CHECKSUM, testSet, "Checksum ALG_ISO3309_CRC32 Checksum_update()") > -1);
        testSet.algorithmMethod = Consts.Checksum_doFinal;
        assertTrue(PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CHECKSUM, Consts.INS_PERF_TEST_CLASS_CHECKSUM, testSet, "Checksum ALG_ISO3309_CRC32 Checksum_doFinal()") > -1);
        
    }     
    
   @Test
    void perftest_testClass_KeyPair() throws Exception {
        // Prepare connection to simulated card
        PerformanceTesting.file = cardManager.establishConnection(AlgTestSinglePerApdu.class);
        //PerformanceTesting.file = cardManager.establishConnection(null);
        assertNotEquals(PerformanceTesting.file, null);

        // BUGBUG: other types from MessageDigest
        
        // Prepare test
        TestSettings testSet = null;
        testSet = PerformanceTesting.prepareTestSettings(Consts.CLASS_KEYPAIR, Consts.ALG_RSA_CRT, Consts.ALG_RSA_CRT, Consts.LENGTH_RSA_1024, Consts.KeyPair_genKeyPair, 
                UNUSED, UNUSED, (short) 1, (short) 1, (short) 1);      

        // BUGBUG: ALG_RSA, ALG_DSA, ALG_EC_F2M, ALG_EC_FP
        
        // Test single execution of operation
        testSet.numRepeatWholeMeasurement = 2;  // TODO: measure repeatedly
        testSet.algorithmMethod = Consts.KeyPair_genKeyPair;
        assertTrue(PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEYPAIR, Consts.INS_PERF_TEST_CLASS_KEYPAIR, testSet, "KeyPair ALG_RSA_CRT LENGTH_RSA_1024 KeyPair_genKeyPair()") > -1);
    }        
    
   @Test
    void perftest_testClass_KeyAgreement() throws Exception {
        // Prepare connection to simulated card
        PerformanceTesting.file = cardManager.establishConnection(AlgTestSinglePerApdu.class);
        //PerformanceTesting.file = cardManager.establishConnection(null);
        assertNotEquals(PerformanceTesting.file, null);

        // BUGBUG: other types from KeyAgreement
        
        // Prepare test
        TestSettings testSet = null;
        testSet = PerformanceTesting.prepareTestSettings(Consts.CLASS_KEYAGREEMENT, Consts.ALG_EC_SVDP_DH, Consts.ALG_EC_FP, Consts.LENGTH_EC_FP_192, Consts.KeyAgreement_init, 
                UNUSED, UNUSED, (short) 1, (short) 1, (short) 1);      

        // BUGBUG: ALG_EC_SVDP_DHC, ALG_EC_SVDP_DH_PLAIN, ALG_EC_SVDP_DHC_PLAIN
        
        testSet.numRepeatWholeMeasurement = 2;  
        testSet.algorithmMethod = Consts.KeyAgreement_init;
        assertTrue(PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEYAGREEMENT, Consts.INS_PERF_TEST_CLASS_KEYAGREEMENT, testSet, "CLASS_KEYAGREEMENT ALG_EC_SVDP_DH LENGTH_EC_FP_192 KeyAgreement_init()") > -1);
        testSet.algorithmMethod = Consts.KeyAgreement_generateSecret;
        assertTrue(PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEYAGREEMENT, Consts.INS_PERF_TEST_CLASS_KEYAGREEMENT, testSet, "CLASS_KEYAGREEMENT ALG_EC_SVDP_DH LENGTH_EC_FP_192 KeyAgreement_generateSecret()") > -1);
    }     
        
    private static void printMembers(Member[] mbrs, String s, String longClassName, String shortClassName) throws IllegalArgumentException, IllegalAccessException {
	int methodIndex = 0;
        out.format("%s:%n", s);
	for (Member mbr : mbrs) {
	    if (mbr instanceof Field) {
                Field value = (Field) mbr;
                value.setAccessible(true);                
                
                String result = value.toGenericString();
                result = result.replace(longClassName, shortClassName);
                result = result.replace(".", "_");
                
		out.format("  %s = %d;%n", result, value.get(null));
		//out.format("  %s%n", value.getName());
/*
                try {
                    byte bVal = value.getByte(null);
                    short sVal = value.getShort(null);
                    out.format("MY public final static byte %s = (byte) %d;%n", value.getName(), bVal);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(PerformanceTestingNGTest.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(PerformanceTestingNGTest.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                Annotation[] anot = value.getAnnotationsByType(Consts.Twizzle.class);
		if (anot != null && anot.length > 0) {
                    out.format("  %s%n", anot[0].toString());
                }
*/        
            }
	    else if (mbr instanceof Constructor) 
		out.format("  %s%n", ((Constructor)mbr).toGenericString());
	    else if (mbr instanceof Method) {
                methodIndex++;
                Method value = (Method) mbr;
               
                String result = value.toGenericString();
                result = result.replace(longClassName, shortClassName);
                result = result.replace(".", "_");
                
		out.format("  public static final short %s_%s = %d;%n", shortClassName, value.getName(), methodIndex);
            }
	}
	if (mbrs.length == 0)
	    out.format("  -- No %s --%n", s);
	out.format("%n");
    }    
    
    void formatClass(String longClassName, String shortClassName) throws ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
        out.format("// Class %s\n", longClassName);
        Class<?> c = Class.forName(longClassName);
        Field[] fields = c.getDeclaredFields();
        printMembers(fields, "Fields", longClassName, shortClassName);
        Method[] methods = c.getDeclaredMethods();
        printMembers(methods, "Methods", longClassName, shortClassName);
    }
    @Test
    void debug_iterateOverFields() throws Exception {   
        formatClass("javacard.security.Signature", "Signature");
        formatClass("javacardx.crypto.Cipher", "Cipher");
        formatClass("javacard.security.KeyAgreement", "KeyAgreement");
        formatClass("javacard.security.KeyBuilder", "KeyBuilder");
        formatClass("javacard.security.KeyPair", "KeyPair");
        formatClass("javacard.security.MessageDigest", "MessageDigest");
        formatClass("javacard.security.RandomData", "RandomData");
        formatClass("javacard.security.Checksum", "Checksum");
        formatClass("javacardx.crypto.KeyEncryption", "KeyEncryption");
        
        
    }    
        
}
