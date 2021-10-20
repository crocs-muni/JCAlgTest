package algtestprocessTest;

import algtestprocess.JCinfohtml;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author tjaros
 */
public class JCinfohtmlSimilarityNGTest {
	private AlgTestProcessTestUtils utils = new AlgTestProcessTestUtils(
		"/../Profiles/performance/fixed",
		"compare",
		"similarity-table.html",
		false
	);
	
	public JCinfohtmlSimilarityNGTest() {
	}

	@BeforeClass
	public void setUpClass() throws Exception {
		JCinfohtml.runCompareTable(utils.getDataFolderPath());
		JCinfohtml.runCompareGraph(utils.getDataFolderPath());
	}

	@AfterClass
	public void tearDownClass() throws Exception {
		utils.cleanUp();
	}

	@BeforeMethod
	public void setUpMethod() throws Exception {
	}

	@AfterMethod
	public void tearDownMethod() throws Exception {
	}

	@Test
	public void allFilesReferencedTest() throws Exception {
		Assert.assertTrue(utils.allFilesCorrectlyReferenced());
	}
}
