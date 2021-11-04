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
		"fixed",
		"compare",
		"similarity-table.html",
		false
	);

	public JCinfohtmlSimilarityNGTest() {
	}

	@BeforeClass
	public void setUpClass() throws Exception {
		utils.setUp();
		JCinfohtml.runCompareTable(utils.getInputBasePath(), utils.getOutputBasePath());
		JCinfohtml.runCompareGraph(utils.getInputBasePath(), utils.getOutputBasePath());
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
