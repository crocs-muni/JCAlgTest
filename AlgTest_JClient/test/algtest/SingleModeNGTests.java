package algtest;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import algtestjclient.SingleModeTest;
import static algtestjclient.SingleModeTest.cardManager;
import java.io.FileOutputStream;

import AlgTest.JCAlgTestApplet;
import static org.testng.Assert.*;

/**
 *
 * @author xsvenda
 */
public class SingleModeNGTests {
    
    public SingleModeNGTests() {
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
    void getSupportedAlgorithms() throws Exception {
        // Prepare connection to simulated card
        FileOutputStream file = cardManager.establishConnection(JCAlgTestApplet.class);
        assertNotEquals(file, null);
        
        // Run test 
        SingleModeTest.testAllAtOnce(file);
        
        // BUGBUG: Check content of file
        assertTrue(false);
    }
    

    
    
    
}
