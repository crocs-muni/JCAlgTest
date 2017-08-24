package algtestjclient;

import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
/**
 *
 * @author xsvenda
 */
public class KeyHarvestNGTest {
    
    static boolean bTestRealCards = true;
    
    public KeyHarvestNGTest() {
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
    void gatherRSAKeys() throws Exception {
        KeyHarvest.file = (bTestRealCards) ? cardManager.establishConnection(null) : cardManager.establishConnection(AlgPerformanceTest.class);   
        assertNotEquals(KeyHarvest.file, null);

        String fileName = cardManager.getTerminalName() + "__" + cardManager.getATR() + "__" + Long.toString(System.currentTimeMillis()) + ".csv";
        fileName = fileName.replace(' ', '_');
        cardManager.GenerateAndGetKeys(fileName, -1, -1, false, (short) 512, true);
        cardManager.DisconnectFromCard();
    }
}
