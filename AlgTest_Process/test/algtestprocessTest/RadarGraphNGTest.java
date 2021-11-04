package algtestprocessTest;

import algtestprocess.RadarGraph;
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
public class RadarGraphNGTest {

	private AlgTestProcessTestUtils utils = new AlgTestProcessTestUtils(
		"fixed",
		"radar_graphs",
		"radar-graphs.html",
		true
	);

	public RadarGraphNGTest() {
	}

	@BeforeClass
	public void setUpClass() throws Exception {
		utils.setUp();
		RadarGraph.runRadarGraph(utils.getInputBasePath(), utils.getOutputBasePath());
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
