package algtestprocessTest;

import org.testng.annotations.Test;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import algtestprocess.ScalabilityGraph;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.testng.Assert;

/**
 *
 * @author tjaros
 */
public class ScalabilityGraphNGTest {

	private AlgTestProcessTestUtils utils = new AlgTestProcessTestUtils(
		"variable",
		"scalability",
		"scalability.html",
		true
	);

	public ScalabilityGraphNGTest() {
	}

	@BeforeClass
	public void setUpClass() throws Exception {
		utils.setUp();
		ScalabilityGraph.runScalability(utils.getInputBasePath(), utils.getOutputBasePath(), false);
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

	@Test
	public void checkApiAvailability() throws Exception {
		String html = new String(
			Files.readAllBytes(
				utils.getPaths().stream().findAny().orElseThrow(AssertionError::new)
			),
			StandardCharsets.UTF_8);
		Jsoup.parse(html)
			.getElementsByAttributeValueContaining("src", "http")
			.stream()
			.map((Element x) -> x.attr("src"))
			.forEach(x -> {
				try {
					URL url = new URL(x);
					HttpURLConnection con = (HttpURLConnection) url.openConnection();
					con.setRequestMethod("HEAD");
					con.connect();
					Assert.assertTrue(con.getResponseCode() == 200);
				} catch (IOException e) {
					Assert.assertTrue(false);
				}
			});

	}
}
