/*  
    Copyright (c) 2008-2014 Petr Svenda <petr@svenda.com>

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
import java.util.HashMap;
import java.util.Scanner;

/**
 *
 * @author petr 
 */
public class AlgTestProcess {
    /* Arguments for AlgTestProcess. */
    public static final String GENERATE_HTML = "HTML";
    public static final String COMPARE_CARDS = "COMPARE";
    public static final String GENERATE_JCCONSTANTS = "JCCONSTS";
    
    public static final String GENERATE_JCINFO = "JCINFO";              //TABLE WITH PERF RESULTS
    public static final String GENERATE_SORTABLE = "SORTABLE";          //SORTABLE TABLE
    public static final String GENERATE_GRAPHS = "GRAPHS";              //GENERATE SINGLE GRAPHS PAGE FOR JCINFO
    public static final String GENERATE_GRAPHS_ONEPAGE = "GRAPHSPAGE";  //PAGE WITH VARIABLE PERFTEST GRAPHS
    public static final String GENERATE_COMPARE_GRAPH = "COMPAREGRAPH"; //ONE BIG GRAPH FOR COMPARE CARDS
    public static final String GENERATE_COMPARE_TABLE = "COMPARETABLE"; //ONE BIG TABLE FOR COMPARE CARDS      

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Make sure to have JavaCard support version in your CSV file!");
        try {
            if (args.length == 0) { // in case there are no arguments present
                PrintHelp();
            }
            else {
                /* To be able to run the program even with incomplete parameter (missing '\'). */
                String arg = new String();
                if(args[0].length() != 0){ // testing if there is any argument present
                    int pathLength = args[0].length();
                    char lastChar = args[0].charAt(pathLength - 1);    // last character in string
                    if (lastChar != ('\\' | '/')){
                    args[0] = args[0] + "/";     // adding '\' if not present
                    }
                }

                if(args.length > 1){
                    if (args[1].equals(GENERATE_HTML)){
                        // generating HTML
                        System.out.println("Generating HTML table.");
                        SupportTable.generateHTMLTable(args[0]);
                    }
                    else if (args[1].equals(COMPARE_CARDS)){
                        System.out.println("Comparing cards.");
                        SupportTable.compareSupportedAlgs(args[0]);}
                    else if (args[1].equals(GENERATE_JCCONSTANTS)){
                        System.out.println("Generating file with JC constants.");
                        SupportTable.generateJCConstantsFile(args[0]);}
                    else if (args[1].equals(GENERATE_SORTABLE)){
                        System.out.println("Generating sortable table from all files in directory.");
                        JCinfohtml.runSortable(args[0]);}
                    else if (args[1].equals(GENERATE_GRAPHS)){
                        System.out.println("Generating graphs from input file to new directory.");
                        JCinfohtml.runGraphs(args[0]);}
                    else if (args[1].equals(GENERATE_COMPARE_GRAPH)){
                        System.out.println("Generating compare graph from input dir.");
                        JCinfohtml.runCompareGraph(args[0]);}
                    else if (args[1].equals(GENERATE_COMPARE_TABLE)){
                        System.out.println("Generating compare table from input dir.");
                        JCinfohtml.runCompareTable(args[0]);}
                    else if (args[1].equals(GENERATE_GRAPHS_ONEPAGE)){
                        System.out.println("Generating graphs page from input file / folder.");
                        File file = new File(args[0]);                        
                        if (file.exists() && file.isDirectory())
                            if((args.length>2) && (args[2].toLowerCase().equals("toponly")))
                                generateGraphsPages(args[0], true);
                            else
                                generateGraphsPages(args[0], false);
                        else if (file.exists() && file.isFile())
                            if((args.length>2) && (args[2].toLowerCase().equals("toponly")))
                                JCinfohtml.runGraphsOnePage(args[0], true); 
                            else
                                JCinfohtml.runGraphsOnePage(args[0], false);                        
                        else
                            System.out.println("ERR: Wrong path to the source file / folder.");                        
                    }
                    else if (args[1].equals(GENERATE_JCINFO)){
                        System.out.println("Generating JC performance testing to HTML from input file / folder.");
                        File file = new File(args[0]);
                        if (file.exists() && file.isDirectory())
                            generateJCInfoHTMLTable(args[0]);
                        else if (file.exists() && file.isFile())
                            JCinfohtml.run(args[0],"noname");
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
                        SupportTable.generateHTMLTable(args[0]);}
                    else if (answ == 0) {
                        SupportTable.compareSupportedAlgs(args[0]);
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
        }
        catch (Exception ex) {
            System.out.println("Exception : " + ex);
        }
    }
    
            
    
    private static void generateJCInfoHTMLTable(String basePath) throws IOException {        
        File dir = new File(basePath);
        String[] filesArray = dir.list();
        
        if ((filesArray != null) && (dir.isDirectory() == true)) {    
            
            HashMap filesSupport[] = new HashMap[filesArray.length]; 
            
            for (int i = 0; i < filesArray.length; i++) {
                filesSupport[i] = new HashMap();
                if(filesArray[i].contains("csv"))
                    JCinfohtml.run(basePath + filesArray[i], filesArray[i]);
            }   
        }
    }
    
    private static void generateGraphsPages(String basePath, Boolean toponly) throws IOException {        
        File dir = new File(basePath);
        String[] filesArray = dir.list();
        
        if ((filesArray != null) && (dir.isDirectory() == true)) {    
            
            HashMap filesSupport[] = new HashMap[filesArray.length]; 
            
            for (int i = 0; i < filesArray.length; i++) {
                filesSupport[i] = new HashMap();
                if(filesArray[i].contains("csv"))
                    JCinfohtml.runGraphsOnePage(basePath + filesArray[i], toponly);
            }   
        }
    }
        
    private static void PrintHelp() {
        System.out.println("Usage: java AlgTestProcess.jar base_path\n" 
                + "  base_path/results/directory should contain *.csv files with results \n"
                + "  html table will be generated into base_path/AlgTest_html_table.html \n");
    }
        
        
}

