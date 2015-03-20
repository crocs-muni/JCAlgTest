package AlgTest;

import static AlgTest.Consts.ALG_AES_BLOCK_128_CBC_NOPAD;
import static AlgTest.Consts.CLASS_CIPHER;
import static AlgTest.Consts.Cipher_doFinal;
import static AlgTest.Consts.Cipher_init;
import static AlgTest.Consts.Cipher_update;
import static AlgTest.Consts.LENGTH_AES_128;
import static AlgTest.Consts.TEST_DATA_LENGTH;
import static AlgTest.Consts.TYPE_AES;
import static AlgTest.Consts.UNUSED;
import static AlgTest.Consts.method_setKey;
import static algtestjclient.CardMngr.CLASS_KEYBUILDER;
import algtestjclient.PerformanceTesting;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;

import algtestjclient.SingleModeTest;
import static algtestjclient.SingleModeTest.cardManager;
import java.io.FileOutputStream;
import static java.lang.System.out;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.type.PrimitiveType;
import javax.smartcardio.ResponseAPDU;
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
    }
    

    @Test
    void perftest_testClass_AESKey() throws Exception {
        // Prepare connection to simulated card
        FileOutputStream file = cardManager.establishConnection(AlgTestSinglePerApdu.class);
        PerformanceTesting.file = file;
        assertNotEquals(file, null);

        // Prepare test
        TestSettings testSet = null;
        testSet = PerformanceTesting.prepareTestSettings(CLASS_KEYBUILDER, UNUSED, TYPE_AES, LENGTH_AES_128, method_setKey, 
                UNUSED, UNUSED, (short) 1, UNUSED, (short) 1);      

        PerformanceTesting.perftest_prepareClass(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_KEY, testSet);
        
        testSet.algorithmMethod = method_setKey;
        PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PERF_TEST_CLASS_KEY, testSet, "AESKey TYPE_AES LENGTH_AES_128 setKey()");
        testSet.algorithmMethod = Consts.method_clearKey;
        PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PERF_TEST_CLASS_KEY, testSet, "AESKey TYPE_AES LENGTH_AES_128 clearKey()");
        testSet.algorithmMethod = Consts.method_getKey;
        PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PERF_TEST_CLASS_KEY, testSet, "AESKey TYPE_AES LENGTH_AES_128 getKey()");
    }
    
    @Test
    void perftest_testClass_Cipher() throws Exception {
        // Prepare connection to simulated card
        FileOutputStream file = cardManager.establishConnection(AlgTestSinglePerApdu.class);
        PerformanceTesting.file = file;
        assertNotEquals(file, null);

        // Prepare test
        TestSettings testSet = null;
        testSet = PerformanceTesting.prepareTestSettings(CLASS_CIPHER, ALG_AES_BLOCK_128_CBC_NOPAD, TYPE_AES, LENGTH_AES_128, Cipher_update, 
                TEST_DATA_LENGTH, UNUSED, (short) 1, (short) 1, (short) 1);      

        PerformanceTesting.perftest_prepareClass(Consts.CLA_CARD_ALGTEST, Consts.INS_PREPARE_TEST_CLASS_CIPHER, testSet);
        
        // Test single execution of operation
        testSet.numRepeatSubOperation = 1;
        testSet.numRepeatWholeMeasurement = 5;
        testSet.algorithmMethod = Cipher_update;
        PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PERF_TEST_CLASS_CIPHER, testSet, "Cipher ALG_AES_BLOCK_128_CBC_NOPAD LENGTH_AES_128 Cipher_update()");
        testSet.algorithmMethod = Cipher_doFinal;
        PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PERF_TEST_CLASS_CIPHER, testSet, "Cipher ALG_AES_BLOCK_128_CBC_NOPAD LENGTH_AES_128 Cipher_doFinal()");
        testSet.algorithmMethod = Cipher_init;
        PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PERF_TEST_CLASS_CIPHER, testSet, "Cipher ALG_AES_BLOCK_128_CBC_NOPAD LENGTH_AES_128 Cipher_init()");

        // Test operation on splitted chunks of data (256 / 8)
        testSet.numRepeatSubOperation = 8;
        testSet.algorithmMethod = Cipher_update;
        PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PERF_TEST_CLASS_KEY, testSet, "Cipher ALG_AES_BLOCK_128_CBC_NOPAD LENGTH_AES_128 8x Cipher_update()");
        testSet.algorithmMethod = Cipher_doFinal;
        PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PERF_TEST_CLASS_KEY, testSet, "Cipher ALG_AES_BLOCK_128_CBC_NOPAD LENGTH_AES_128 8x Cipher_doFinal()");
        testSet.algorithmMethod = Cipher_init;
        PerformanceTesting.perftest_measure(Consts.CLA_CARD_ALGTEST, Consts.INS_PERF_TEST_CLASS_KEY, testSet, "Cipher ALG_AES_BLOCK_128_CBC_NOPAD LENGTH_AES_128 8x Cipher_init()");
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
    @Test
    void iterateOverFields() throws Exception {    
        String longClass = "javacard.security.Signature";
        String shortClass = "Signature";
        Class<?> c = Class.forName(longClass);
        Field[] fields = c.getDeclaredFields();
        printMembers(fields, "Fields", longClass, shortClass);
        Method[] methods = c.getDeclaredMethods();
        printMembers(methods, "Methods", longClass, shortClass);
    }    
        
}
