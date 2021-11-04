package algtestprocessTest;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

/**
 *
 * @author tjaros
 */
public class AlgTestProcessTestUtils {

	private String dataLength = null;
	private String inputBasePath = null;
	private String outputBasePath = "/tmp";
	private String mainFolderName = null;
	private String mainFileName = null;
	private boolean mainFileInMainFolder = false;

	public AlgTestProcessTestUtils(
		String dataLength,
		String mainFolderName,
		String mainFileName,
		boolean mainFileInMainFolder
	) {
		this.dataLength = dataLength;
		this.mainFolderName = mainFolderName;
		this.mainFileName = mainFileName;
		this.mainFileInMainFolder = mainFileInMainFolder;
	}

	public void setUp() throws IOException, InterruptedException {
		if (!Files.exists(Paths.get("/tmp/jcalgtest_results"))) {
			String[] command = {"/bin/bash", "-c",
				"cd /tmp && git clone https://github.com/crocs-muni/jcalgtest_results.git"};
			Process p = Runtime.getRuntime().exec(command);
			p.waitFor();
			if (p.exitValue() != 0) {
				throw new IOException("Could not clone results for test.");
			}
		}
		this.inputBasePath = "/tmp/jcalgtest_results/javacard/Profiles/performance/" + this.dataLength;
		this.outputBasePath = "/tmp";
	}

	public String getInputBasePath() {
		return this.inputBasePath;
	}

	public String getOutputBasePath() {
		return this.outputBasePath;
	}

	public String getMainFolderPath() {
		return getOutputBasePath() + "/" + mainFolderName;
	}

	public String getMainFilePath() {
		return ((mainFileInMainFolder)
			? getMainFolderPath() : getOutputBasePath())
			+ "/" + mainFileName + "/";

	}

	public List<Path> getPaths() throws IOException {
		try ( Stream<Path> walk = Files.walk(Paths.get(getMainFolderPath()))) {
			return walk
				.filter(x -> !x.toString().contains(mainFileName) && !Files.isDirectory(x))
				.collect(Collectors.toList());
		}
	}

	public void cleanUp() throws IOException {
		try ( Stream<Path> walk = Files.walk(Paths.get(getMainFolderPath()))) {
			walk.sorted(Comparator.reverseOrder())
				.map(Path::toFile)
				.forEach(File::delete);
		}
		if (!mainFileInMainFolder) {
			new File(getMainFilePath()).delete();
		}
	}

	public boolean allFilesCorrectlyReferenced() throws IOException {
		String mainHtml = new String(
			Files.readAllBytes(Paths.get(getMainFilePath())),
			StandardCharsets.UTF_8
		);
		List<String> hrefs = Jsoup.parse(mainHtml)
			.getElementsByAttributeValueMatching("href", ".*html")
			.stream()
			.map((Element x) -> x.attr("href"))
			.filter(x -> x.matches("^([.]/){0,1}[a-zA-Z0-9]+.*"))
			.collect(Collectors.toList());
		List<String> paths = getPaths().stream()
			.map(x -> x.toString())
			.collect(Collectors.toList());
		for (String href : hrefs) {
			List<String> found = paths.stream()
				.filter((String x) -> x.contains(href))
				.collect(Collectors.toList());
			if (found.isEmpty() && !(!mainFileInMainFolder && href.contains("./"))) {
				System.err.println(getMainFolderPath() + " does not contain " + href);
				return false;
			}
		}
		return true;
	}
}
