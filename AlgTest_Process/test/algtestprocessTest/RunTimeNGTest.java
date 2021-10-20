package algtestprocessTest;

import algtestprocess.RunTime;
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
public class RunTimeNGTest {

	private AlgTestProcessTestUtils utils = new AlgTestProcessTestUtils(
		"/../Profiles/performance/fixed",
		"run_time",
		"execution-time.html",
		true
	);

	public RunTimeNGTest() {
	}

	@BeforeClass
	public void setUpClass() throws Exception {
		RunTime.runRunTime(utils.getDataFolderPath());
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
