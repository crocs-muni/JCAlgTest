package algtestjclient;

import com.beust.jcommander.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Petr Svenda
 */
public class Args {
    // If extending, do not forget to add into @Parameter(names = below
    public static final String OP_ALG_SUPPORT_BASIC = "ALG_SUPPORT_BASIC";          // Test of basic algorithmic support
    public static final String OP_ALG_SUPPORT_EXTENDED = "ALG_SUPPORT_EXTENDED";    // Test of basic + extended algorithmic support
    public static final String OP_ALG_PERFORMANCE_STATIC = "ALG_PERFORMANCE_STATIC";    // Test of performance on static data length
    public static final String OP_ALG_PERFORMANCE_VARIABLE = "ALG_PERFORMANCE_VARIABLE";    // Test of performance with variable data lengths

    @Parameter
    public List<String> parameters = new ArrayList<>();

    @Parameter(names = { "-op"}, description = "Operation(s) to execute (" + OP_ALG_SUPPORT_BASIC + " | " + OP_ALG_SUPPORT_EXTENDED 
                        + " | " + OP_ALG_PERFORMANCE_STATIC + " | " + OP_ALG_PERFORMANCE_VARIABLE + ")")
    public List<String> operations = new ArrayList<>();

    @Parameter(names = "-cardname", description = "Name of the tested card")
    public String cardName = "";

    @Parameter(names = "-simulator", description = "Use simulator")
    public boolean simulator = false;

    @Parameter(names = "-outpath", description = "Base output path")
    public String baseOutPath = "";

    @Parameter(names = "--help", help = true, description = "Print program usage")
    public boolean help;

    @Parameter(names = "-fresh", description = "Force generating new complete measurements")
    public boolean fresh = false;
}