/*
    Copyright (c) 2008-2016 Petr Svenda <petr@svenda.com>

     LICENSE TERMS

     The free distribution and use of this software in both source and binary
     form is allowed (with or without changes) provided that:

       1. distributions of this source code include the above copyright
          notice, this list of conditions and the following disclaimer;

       2. distributions in binary form include the above copyright
          notice, this list of conditions and the following disclaimer
          in the documentation and/or other associated materials;

       3. the copyright holder's name is not used to endorse products
          built using this software without specific written permission.

     ALTERNATIVELY, provided that this notice is retained in full, this product
     may be distributed under the terms of the GNU General Public License (GPL),
     in which case the provisions of the GPL apply INSTEAD OF those given above.

     DISCLAIMER

     This software is provided 'as is' with no explicit or implied warranties
     in respect of its properties, including, but not limited to, correctness
     and/or fitness for purpose.

    Please, report any bugs to author <petr@svenda.com>
*/

package algtestprocess;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 *
 * @author petr
 */
public class AlgTestProcess {
    /* Arguments for AlgTestProcess. */
    public static final String GENERATE_HTML = "HTML";
    public static final String GENERATE_TPM_HTML = "TPM_HTML";
    public static final String COMPARE_CARDS = "COMPARE";
    public static final String GENERATE_JCCONSTANTS = "JCCONSTS";

    public static final String GENERATE_JCINFO = "JCINFO";              //TABLE WITH PERF RESULTS
    public static final String GENERATE_TPMINFO = "TPMINFO";              //TABLE WITH PERF RESULTS
    public static final String GENERATE_SORTABLE = "SORTABLE";          //SORTABLE TABLE
    public static final String GENERATE_GRAPHS = "GRAPHS";              //GENERATE SINGLE GRAPHS PAGE FOR JCINFO
    public static final String GENERATE_GRAPHS_ONEPAGE = "SCALABILITY"; //PAGE WITH VARIABLE PERFTEST GRAPHS
    public static final String GENERATE_COMPARE_GRAPH = "COMPAREGRAPH"; //ONE BIG GRAPH FOR COMPARE CARDS
    public static final String GENERATE_COMPARE_TABLE = "SIMILARITY";   //SIMILARITY TABLE FOR COMPARE CARDS
    public static final String GENERATE_UNKNOWN_RESULTS = "UNKNOWN";    //RESULTS FOR UNKNOWN CARD
    public static final String GENERATE_RADAR_GRAPHS = "RADAR";         //ONE BIG TABLE FOR COMPARE CARDS      

    public static String m_inputBasePath = "";
    public static String m_outputBasePath = "";
    /**
     * @param args the command line arguments
     * First argument is part to file/folder second is type of processing
     */
    public static void main(String[] args) throws FileNotFoundException {
        System.out.println("Make sure to have JavaCard support version in your CSV file!");
        try {
            if (args.length == 0) { // in case there are no arguments present
                printHelp();
            }
            else {
                // Prepare ointput path
                m_inputBasePath = (args.length > 1) ? args[0] : Paths.get(".").toAbsolutePath().normalize().toString();
                char lastChar = m_inputBasePath.charAt(m_inputBasePath.length() - 1);
                if ((lastChar != '\\') && (lastChar != '/')) {
                    m_inputBasePath = m_inputBasePath + "/";     // adding '\' if not present
                }
                
                // Prepare output path
                m_outputBasePath = (args.length > 2) ? args[2] : Paths.get(".").toAbsolutePath().normalize().toString();
                lastChar = m_outputBasePath.charAt(m_outputBasePath.length() - 1);                
                if ((lastChar != '\\') && (lastChar != '/')) {
                    m_outputBasePath = m_outputBasePath + "/";     // adding '\' if not present
                }

                if(args.length > 1){
                    if (args[1].equals(GENERATE_HTML)){
                        // generating HTML
                        System.out.println("Generating HTML tables");
                        // partial tables
                        HashMap<String, String> filteredDirs = new HashMap<>();
                        HashMap<String, ArrayList<Integer>> filteredCards = new HashMap<>();

                        SupportTable.splitBySupport(m_inputBasePath, filteredDirs, filteredCards);
                        // All items table    
                        SupportTable.generateHTMLTable(m_inputBasePath, m_outputBasePath, "", true, filteredCards);
/*
                        for (String dirName : filteredDirs.keySet()) {
                            System.out.println(String.format("\nGenerating HTML table for %s.\n", filteredDirs.get(dirName)));
                            SupportTable.generateHTMLTable(filteredDirs.get(dirName), dirName, false, null);
                        }
*/
                    }
                    else if (args[1].equals(GENERATE_TPM_HTML)) {
                        System.out.println("Generating HTML table for TPMs.");
                        TPMSupportTable.generateHTMLTable(m_inputBasePath);
                    }
                    else if (args[1].equals(COMPARE_CARDS)){
                        System.out.println("Comparing cards.");
                        SupportTable.compareSupportedAlgs(m_inputBasePath);}
                    else if (args[1].equals(GENERATE_JCCONSTANTS)){
                        System.out.println("Generating file with JC constants.");
                        SupportTable.generateJCConstantsFile(m_inputBasePath);}
                    else if (args[1].equals(GENERATE_SORTABLE)){
                        System.out.println("Generating sortable table from all files in directory.");
                        Sortable.runSortable(m_inputBasePath, m_outputBasePath);}
                    else if (args[1].equals(GENERATE_GRAPHS)){
                        System.out.println("Generating graphs from input file to new directory.");
                        JCinfohtml.runGraphs(m_inputBasePath, m_outputBasePath);}
                    else if (args[1].equals(GENERATE_COMPARE_GRAPH)){
                        System.out.println("Generating compare graph from input dir.");
                        JCinfohtml.runCompareGraph(m_inputBasePath, m_outputBasePath);}
                    else if (args[1].equals(GENERATE_RADAR_GRAPHS)){
                        System.out.println("Generating radar graphs from input dir.");
                        RadarGraph.runRadarGraph(m_inputBasePath, m_outputBasePath);}
                    else if (args[1].equals(GENERATE_COMPARE_TABLE)){
                        System.out.println("Generating compare table from input dir.");
                        JCinfohtml.runCompareTable(m_inputBasePath, m_outputBasePath);}
                    else if (args[1].equals(GENERATE_UNKNOWN_RESULTS)){
                        if (args.length <= 2) {
                            System.out.println("AlgTestProcess.jar base_path_folder UNKNOWN unknown_csv_path");
                        } else {
                            System.out.println("Generating results page for unknown card.");
                            JCinfohtml.runUnknownCard(m_inputBasePath, m_outputBasePath, args[3]);
                        }
                    }
                    else if (args[1].equals(GENERATE_GRAPHS_ONEPAGE)){
                        System.out.println("Generating graphs page from input file / folder.");
                        File file = new File(m_inputBasePath);
                        if (file.exists() && file.isDirectory())
                            if((args.length>2) && (args[2].toLowerCase().equals("toponly")))
                                ScalabilityGraph.runScalability(m_inputBasePath, m_outputBasePath, true);
                            else
                                ScalabilityGraph.runScalability(m_inputBasePath, m_outputBasePath, false);
                        else if (file.exists() && file.isFile())
                            if((args.length>2) && (args[2].toLowerCase().equals("toponly")))
                                ScalabilityGraph.generateScalabilityFile(m_inputBasePath, m_outputBasePath, true);
                            else
                                ScalabilityGraph.generateScalabilityFile(m_inputBasePath, m_outputBasePath, false);
                        else
                            System.out.println("ERR: Wrong path to the source file / folder.");
                    }
                    else if (args[1].equals(GENERATE_JCINFO)){
                        System.out.println("Generating JC performance testing to HTML from input file / folder.");
                        File file = new File(m_inputBasePath);
                        if (file.exists() && file.isDirectory())
                             RunTime.runRunTime(m_inputBasePath, m_outputBasePath);
                        else if (file.exists() && file.isFile() && (m_inputBasePath.contains("csv")))
                            RunTime.generateRunTimeFile(m_inputBasePath, m_outputBasePath);
                        else
                            System.out.println("ERR: Wrong path to the source file / folder.");
                    }
                    else if (args[1].equals(GENERATE_TPMINFO)) {
                        System.out.println("Generating TPM performance testing to HTML from input file / folder.");
                        File file = new File(m_inputBasePath);
                        if (file.exists() && file.isDirectory())
                            TPMRunTime.runRunTime(m_inputBasePath);
                        else if (file.exists() && file.isFile() && (m_inputBasePath.endsWith(".csv")))
                            TPMRunTime.generateRunTimeFile(m_inputBasePath);
                        else
                            System.out.println("ERR: Wrong path to the source file / folder.");
                    }
                    else {System.err.println("Incorrect arguments!");}
                }
                else{
                    System.out.println("Do you want to generate HTML table or compare supported algs in existing table?");
                    System.out.println("1 = Generate new HTML; 0 = Compare algs in existing HTML");
                    Scanner sc = new Scanner(System.in);
                    int answ = sc.nextInt();
                    if(answ == 1){
                        SupportTable.generateHTMLTable(m_inputBasePath);}
                    else if (answ == 0) {
                        SupportTable.compareSupportedAlgs(m_inputBasePath);
                    }
                    else {
                        System.err.println("Incorrect parameter!");
                    }
                }
            }

        //generateGPShellScripts();
        }
        catch (IOException ex) {
            System.out.println("IOException : " + ex);
            ex.printStackTrace();
        }
        catch (Exception ex) {
            System.out.println("Exception : " + ex);
            ex.printStackTrace();
        }
    }

    private static void printHelp() {
        System.out.println("Usage: java -jar AlgTestProcess.jar INPUT_BASE_PATH [ACTION] OUTPUT_BASE_PATH\n"
                + "  ACTION can be one of following:"
                + "    HTML ... generates AlgTest_html_table.html file with supported algorithms matrix\n"
                + "  AlgTestProcess.jar base_path_folder [JCINFO, RADAR, SIMILARITY, GRAPHSPAGE, SORTABLE, (UNKNOWN unknown_csv_path)]"        
        );
    }


}

