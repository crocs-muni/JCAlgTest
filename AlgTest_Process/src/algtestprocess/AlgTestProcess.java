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
/**/

package algtestprocess;

import algtestjclient.CardMngr;
import algtestjclient.SingleModeTest;
import java.io.*;
import static java.lang.System.out;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 *
 * @author petr
 */
public class AlgTestProcess {
    /* Arguments for AlgTestProcess. */
    public static final String GENERATE_HTML = "HTML";
    public static final String GENERATE_JCINFO = "JCINFO";
    public static final String GENERATE_SORTABLE = "SORTABLE";
    public static final String COMPARE_CARDS = "COMPARE";
    public static final String GENERATE_JCCONSTANTS = "JCCONSTS";
    public static final String GENERATE_GRAPHS = "GRAPHS";
    public static final String GENERATE_GRAPHS_ONEPAGE = "GRAPHSPAGE";
    
    
    
    
    // if one card results are generated
    public static final String[] JAVA_CARD_VERSION = {"2.1.2", "2.2.1", "2.2.2"};
    public static int jcv = -1;
    
    // if multiple card results are generated
    private static List<String> java_card_version_array = new ArrayList<String>();
    private static String appletVersion = "";
    
    private static final int JC_SUPPORT_OFFSET = 25;
    private static final int AT_APPLET_OFFSET = 23;

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
                    if (lastChar != '\\'){
                    args[0] = args[0] + "\\";     // adding '\' if not present
                    }
                }

                if(args.length > 1){
                    if (args[1].equals(GENERATE_HTML)){
                        // generating HTML
                        System.out.println("Generating HTML table.");
                        generateHTMLTable(args[0]);
                    }
                    else if (args[1].equals(COMPARE_CARDS)){
                        System.out.println("Comparing cards.");
                        compareSupportedAlgs(args[0]);}
                    else if (args[1].equals(GENERATE_JCCONSTANTS)){
                        System.out.println("Generating file with JC constants.");
                        generateJCConstantsFile(args[0]);}
                    else if (args[1].equals(GENERATE_SORTABLE)){
                        System.out.println("Generating sortable table from all files in directory.");
                        JCinfohtml.runSortable(args[0]);}
                    else if (args[1].equals(GENERATE_GRAPHS)){
                        System.out.println("Generating graphs from input file to new directory.");
                        JCinfohtml.runGraphs(args[0]);}
                    else if (args[1].equals(GENERATE_GRAPHS_ONEPAGE)){
                        System.out.println("Generating gprahs page from input file.");
                        if (args.length > 2){ 
                            generateGraphsPages(args[0]);
                        }else{
                            JCinfohtml.runGraphsOnePage(args[0]);
                        }
                        JCinfohtml.runGraphsOnePage(args[0]);}
                    else if (args[1].equals(GENERATE_JCINFO)){
                        System.out.println("Generating JC performance testing to HTML.");
                        if (args.length > 2){ 
                            generateJCInfoHTMLTable(args[0]);
                        }else{
                            JCinfohtml.run(args[0],args[1]);
                        }
                    }
                    else {System.err.println("Incorrect arguments!");}
                }
                else{                
                    System.out.println("Do you want to generate HTML table or compare supported algs in existing table?");
                    System.out.println("1 = Generate new HTML; 0 = Compare algs in existing HTML");
                    Scanner sc = new Scanner(System.in);
                    int answ = sc.nextInt();
                    if(answ == 1){
                        generateHTMLTable(args[0]);}
                    else if (answ == 0) {
                        compareSupportedAlgs(args[0]);
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
    
    /**
     * Method takes HTML file with two smart card algorithm support results and marks differences between them.
     * @param basePath Path to folder with HTML file which must be named 'AlgTest_html_table.html'.
     * @throws FileNotFoundException
     * @throws IOException 
     */
    private static void compareSupportedAlgs (String basePath) throws FileNotFoundException, IOException{
        /* String containing input file path. */
        String inputFileName = basePath + "AlgTest_html_table.html";
        /* String containing output file path. */
        String outputFileName = basePath + "AlgTest_html_table_comparison.html";
        /* String containing line to search for in HTML file. */
        String lineToSearch = "<tr style='height:12.75pt'>";
        /* String containing style information for not matching algorithms in HTML file. */
        String styleInfo = "<tr style='height:12.75pt;outline: solid'>";
        
        /* String array for loaded file. */
        ArrayList<String> loadedFile = new ArrayList<>();
        
        /* Creating object of FileReader. */
        FileReader inputFile = new FileReader(inputFileName);
        BufferedReader reader = new BufferedReader(inputFile);
        
        String line = null;     // buffer for input file
        /* Loading file to ArrayList object. */
        while ((line = reader.readLine()) != null) {    // read if there is another line to read
            loadedFile.add(line);
        }
        /* Searching for algs in loaded file. */
        for (int i = 0; i < loadedFile.size(); i++){
            if (loadedFile.get(i).contains(lineToSearch)){  // checking if line[i] is HTML row definition
                if(!loadedFile.get(i + 3).contains(">c")){  // so the program doesn't check algorithm's class names
                    String aux = loadedFile.get(i+3).substring(loadedFile.get(i+3).indexOf(">") + 1);   // getting first occurence of '>' char and rest of the string behinf him
                    if (!loadedFile.get(i + 4).contains(aux)){  // checking if next algorithm support is the same
                        loadedFile.set(i, styleInfo);           // setting new string to ArrayList (with border)
                    }
                }
            }
        }
        
        FileOutputStream output = new FileOutputStream(outputFileName);
        /* Writing to output file. */
        for (int i = 0; i< loadedFile.size(); i++){
            String aux = loadedFile.get(i);
            aux = aux + "\r\n";     // adding end of line to every line written to HTML file
            output.write(aux.getBytes());
            output.flush();
        }
            output.close();
    }
    
    private static void PrintHelp() {
        System.out.println("Usage: java AlgTestProcess.jar base_path\n" 
                + "  base_path\\results\\directory should contain *.csv files with results \n"
                + "  html table will be generated into base_path\\AlgTest_html_table.html \n");
    }
    
    private static void generateHTMLTable(String basePath) throws IOException {
        String filesPath = basePath + "results\\";
        File dir = new File(filesPath);
        String[] filesArray = dir.list();
        
        if ((filesArray != null) && (dir.isDirectory() == true)) {    
            
            HashMap filesSupport[] = new HashMap[filesArray.length]; 
            
            for (int i = 0; i < filesArray.length; i++) {
                filesSupport[i] = new HashMap();
                parseSupportFile(filesPath + filesArray[i], filesSupport[i]);
            }            
        
            //
            // HTML HEADER
            //
            String fileName = basePath + "AlgTest_html_table.html";
            FileOutputStream file = new FileOutputStream(fileName);                   
            String header = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\r\n<html>\r\n<head>"
                    + "<meta content=\"text/html; charset=utf-8\" http-equiv=\"Content-Type\">\r\n"
                    + "<link type=\"text/css\" href=\"style.css\" rel=\"stylesheet\">\r\n"
                    + "<script class=\"jsbin\" src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js\"></script>\r\n"
                    + "<title>JavaCard support test</title>\r\n"
                    + "<script>$(function(){ $(\"#tab td\").hover(function(){$(\"#tab col\").eq($(this).index()).css({\"border\":\" 3px solid red\"});$(this).closest(\"tr\").css({\"border\":\" 3px solid red\"});},function(){$(\"#tab col\").eq($(this).index()).css({\"border\":\" 0px\"}); $(this).closest(\"tr\").css({\"border\":\" 0px\"});});});</script>\r\n"
                    + "</head>\r\n"
                    + "<body>\r\n\r\n"; 

            String cardList = "<b id=\"LIST\">Tested cards abbreviations:</b><br>\r\n";
            for (int i = 0; i < filesArray.length; i++) {
                String cardIdentification = filesArray[i];
                cardIdentification = cardIdentification.replace('_', ' ');
                cardIdentification = cardIdentification.replace(".csv", "");
                cardIdentification = cardIdentification.replace("3B", ", ATR=3B");
                cardIdentification = cardIdentification.replace("3b", ", ATR=3b");
                cardList += "<b>c" + i + "</b>	" + cardIdentification + ",";
                String cardName = "";
                if (filesSupport[i].containsKey("Card name")) { 
                    cardName = (String) filesSupport[i].get("Card name");
                    cardName = cardName.replace(" ", ""); cardName = cardName.replace("_", ""); 
                    cardList += "&nbsp;<a target=\"_blank\" href=\".fixData/" + cardName + ".html\">PERFORMANCE</a>,&nbsp;";
                    cardList += "<a target=\"_blank\" href=\".variableData/" + cardName + ".html\">GRAPHS</a>";
                }
                cardList += "<br>\r\n";
            }
            cardList += "<br>\r\n"; 
            
            file.write(header.getBytes());
            file.write(cardList.getBytes());
            file.flush();        
            
            String note = "Note: Some cards in the table come without full identification and ATR (\'undisclosed\') as submitters prefered not to disclose it at the momment. I'm publishing it anyway as the information that some card supporting particular algorithm exists is still interesting. Full identification might be added in future.<br><br>\r\n\r\n"; 
            file.write(note.getBytes());
            
            note = "Note: If you have card of unknown type, try to obtain ATR and take a look at smartcard list available here: <a href=\"http://smartcard-atr.appspot.com/\"> http://smartcard-atr.appspot.com/</a><br><br>\r\n\r\n"; 
            file.write(note.getBytes());

            String explain = "<table id=\"explanation\" width=\"1000\" border=\"0\" cellspacing=\"2\" cellpadding=\"4\" >\r\n" 
                    + "<tr style='height:12.75pt'>\r\n"
                    + "  <td class='dark_index'>Symbol</td>\r\n"
                    + "  <td class='dark_index'>Meaning</td>\r\n"
                    + "</tr>\r\n"
                    + "<tr style='height:12.75pt'>\r\n"
                    + "  <td class='light_yes'>yes</td>\r\n"
                    + "  <td class='light_info_left'>This particular algorithm was tested and IS supported by given card.</td>\r\n"
                    + "</tr>\r\n"
                    + "<tr style='height:12.75pt'>\r\n"
                    + "  <td class='light_no'>no</td>\r\n"
                    + "  <td class='light_info_left'>This particular algorithm was tested and is NOT supported by given card.</td>\r\n"
                    + "</tr>\r\n"
                    + "<tr style='height:12.75pt'>\r\n"
                    + "  <td class='light_suspicious'>suspicious yes</td>\r\n"
                    + "  <td class='light_info_left'>This particular algorithm was tested and is REPORTED as supported by given card. However, given algorithm was introduced in later version of JavaCard specification than version declared by the card as supported one. Mostly, algorithm is really supported. But it might be possible, that given algorithm is NOT actually supported by card as some cards may create object for requested algorithm and fail only later when object is actually used. Future version of the JCAlgTest will make more thorough tests regarding this behaviour.</td>\r\n"
                    + "</tr>\r\n"
                    + "<tr style='height:12.75pt'>\r\n"
                    + "  <td class='light_info'>error(ERROR_CODE)</td>\r\n"
                    + "  <td class='light_info_left'>Card returned specific error other then raising CryptoException.NO_SUCH_ALGORITHM. Most probably, algorithm is NOT supported by given card.</td>\r\n"
                    + "</tr>\r\n"
                    + "<tr style='height:12.75pt'>\r\n"
                    + "  <td class='light_info'>?</td>\r\n"
                    + "  <td class='light_info_left'>Card returned unspecific error. Most probably, algorithm is NOT supported by given card.</td>\r\n"
                    + "</tr>\r\n"
                    + "<tr style='height:12.75pt'>\r\n"
                    + "  <td class='light_maybe'>-</td>\r\n"
                    + "  <td class='light_info_left'>This particular algorithm was NOT tested. Usually, this equals to unsupported algorithm. Typical example is the addition of new constants introduced by the newer version of JavaCard standard, which are not supported by cards tested before apperance of of new version of specification. The exceptions to this rule are classes that have to be tested manually (at the moment, following information: JavaCard support version, javacardx.apdu.ExtendedLength Extended APDU) where not tested doesn't automatically means not supported. Automated upload and testing of these features will solve this in future.</td>\r\n"
                    + "</tr>\r\n"
                    + "</table>\r\n"
                    + "<br><br>\r\n";
            file.write(explain.getBytes());
            
            
            String table = "<table id=\"tab\" width=\"730\" border=\"0\" cellspacing=\"2\" cellpadding=\"4\">\r\n";
            // Insert helper column identification for mouseover row & column jquery highlight
            table += "<colgroup>";        
            for (int i = 0; i < filesArray.length + 2; i++) { table += "<col />"; } // + 2 because of column with algorithm name and introducing version
            table += "</colgroup>\r\n";               
            file.write(table.getBytes()); file.flush();                  

            //
            // HTML TABLE BODY
            //
            for (String[] classStr : SingleModeTest.ALL_CLASSES_STR) {
                formatTableAlgorithm_HTML(filesArray, classStr, filesSupport, file);
            }
            
            //
            // FOOTER
            //
            String footer = "</table>\r\n\r\n";
            footer += "\r\n\r\n</body></html>";
            file.write(footer.getBytes());

            file.flush();
            file.close();            
        }
        else {
            System.out.println("directory is empty");
        }
    }
    
    static String[] parseCardName(String fileName) {
        String[] names = new String[2];
        
        String shortCardName = "";
        String cardName = fileName;
        if (cardName.indexOf("_3B ") != -1) shortCardName = cardName.substring(0, cardName.indexOf("_3B "));
        if (cardName.indexOf("_3b ") != -1) shortCardName = cardName.substring(0, cardName.indexOf("_3b "));
        shortCardName = shortCardName.replace('_', ' ');
        cardName = cardName.replace('_', ' ');   
        if (cardName.indexOf("(provided") != -1) cardName = cardName.substring(0, cardName.indexOf("(provided"));
        
        names[0] = cardName;
        names[1] = shortCardName;
        return names;
    }
    
    static String getShortCardName(String fileName) {
        String[] names = parseCardName(fileName);
        return names[1];
    }
    static String getLongCardName(String fileName) {
        String[] names = parseCardName(fileName);
        return names[0];
    }    
    static void formatTableAlgorithm_HTML(String[] filesArray, String[] classInfo, HashMap[] filesSupport, FileOutputStream file) throws IOException {
        // class (e.g., javacardx.crypto.Cipher)
        String algorithm = "<tr style='height:12.75pt'>\r\n" + "<td class='dark'>" + classInfo[0] + "</td>\r\n";
        algorithm += "  <td class='dark_index'>introduced in JavaCard version</td>\r\n"; 
        for (int i = 0; i < filesSupport.length; i++) { algorithm += "  <td class='dark_index' title = '" + getLongCardName(filesArray[i]) + "'>c" + i + "</td>\r\n"; }
        
        String[] jcvArray = java_card_version_array.toArray(new String[java_card_version_array.size()]);
        algorithm += "</tr>\r\n";
        // support for particular algorithm from given class
        for (int i = 0; i < classInfo.length; i++) {
            if (!classInfo[i].startsWith("@@@")) { // ignore special informative types
                String algorithmName = "";
                String algorithmVersion = "";
                
                if (appletVersion != ""){
                    algorithmName = "AlgTest applet version";
                    algorithmVersion = appletVersion;
                }
                else{
                    // Parse algorithm name and version of JC which introduced it
                    if (i == 0){continue;}
                    CardMngr    cman = new CardMngr();
                    algorithmName = cman.GetAlgorithmName(classInfo[i]);
                    algorithmVersion = cman.GetAlgorithmIntroductionVersion(classInfo[i]);
                    if (!cman.ShouldBeIncludedInOutput(classInfo[i])) continue; // ignore types with ignore flag set (algorith#version#include 1/0) 
                }
                
                algorithm += "<tr style='height:12.75pt'>\r\n";
                // Add algorithm name
                algorithm += "  <td class='light'>" + algorithmName + "</td>\r\n";
                // Add version of JavaCard standard that introduced given algorithm
                if (algorithmVersion == appletVersion){
                    algorithm += "  <td class='light_error'>" + "</td>\r\n";
                    appletVersion ="";
                }
                else{
                algorithm += "  <td class='light_error'>" + algorithmVersion + "</td>\r\n";}
                
                // Process all files
                for (int fileIndex = 0; fileIndex < filesSupport.length; fileIndex++) {
                    algorithm += "  ";
                    HashMap fileSuppMap = filesSupport[fileIndex];
                    if (fileSuppMap.containsKey(algorithmName)) {
                        String secondToken = (String) fileSuppMap.get(algorithmName);
                        String title = "title='" + getShortCardName(filesArray[fileIndex]) + " : " + algorithmName + " : " + secondToken + "'";
                        switch (secondToken) {
                            case "no": algorithm += "<td class='light_no' " + title + "'>no</td>\r\n"; break;
                            case "yes":
                                if (java_card_version_array.size() > 0){
                                    if (algorithmVersion.compareTo(jcvArray[fileIndex]) == 1){
                                        // given algorithm is not present in JavaCard specification used to convert uploaded JCAlgTest applet
                                        // make warning
                                        algorithm += "<td class='light_suspicious' " + title + "'>suspicious yes</td>\r\n";
                                    }
                                    else {
                                        if (jcvArray[fileIndex].compareTo("not supplied") == 0) {
                                            // version of JavaCard API information was not supplied, assuming valid response
                                        }
                                        algorithm += "<td class='light_yes' " + title + "'>yes</td>\r\n";
                                    }
                                }
                                else{
                                    algorithm += "<td class='light_yes' " + title + "'>yes</td>\r\n";
                                }
                            break;
                            case "error": algorithm += "<td class='light_error' " + title + "'>error</td>\r\n"; break;
                            case "maybe": algorithm += "<td class='light_error' " + title + "'>maybe</td>\r\n"; break;
                            default: {
                                algorithm += "<td class='light_info' " + title + "'>" + secondToken + "</td>\r\n";
                            }
                        }
                    }
                    else {
                        // algorithm not found in support list
                        algorithm += "<td class='light_maybe'>-</td>\r\n";
                        //algorithm += "<td >&nbsp;</td>\r\n";
                    } 
                }
                algorithm += "</tr>\r\n";
            }
        }
        file.write(algorithm.getBytes());            
    }
    
    static void parseSupportFile(String filePath, HashMap suppMap) throws IOException {
        try {
            //create BufferedReader to read csv file
            BufferedReader br = new BufferedReader( new FileReader(filePath));
            String strLine;
            int lineNumber = 0;
            int tokenNumber = 0;
            boolean bJCSupportVersionPresent = false;

            //read comma separated file line by line
            while ((strLine = br.readLine()) != null) {
                // in case valid JavaCard support version is present
                if (strLine.contains("JavaCard support version")){
                    String jcSupportVersion = (String)strLine.subSequence(JC_SUPPORT_OFFSET, strLine.length()-1);
                    if (jcSupportVersion.length() > 0) {
                        java_card_version_array.add(jcSupportVersion);
                        bJCSupportVersionPresent = true;
                    }
                }
                if (strLine.contains("AlgTest applet version")){
                    appletVersion = strLine.substring(AT_APPLET_OFFSET, strLine.length() - 1);                    
                }
                lineNumber++;

                //break comma separated line using ";"
                StringTokenizer st = new StringTokenizer(strLine, ";,");

                String firstToken = "";
                String secondToken = "";
                while(st.hasMoreTokens()) {
                    tokenNumber++;
                    String tokenValue = st.nextToken();
                    tokenValue = tokenValue.trim();
                    if (tokenNumber == 1) { firstToken = tokenValue; }
                    if (tokenNumber == 2) { secondToken = tokenValue; }
                }
                if (!firstToken.isEmpty()) {
                    suppMap.put(firstToken, secondToken);
                }

                //reset token number
                tokenNumber = 0;
            }
            
            if (!bJCSupportVersionPresent) {
                System.out.println("PROBLEM: " + filePath + " does not have 'JavaCard support version' inserted!");
                java_card_version_array.add("not supplied");
            }
        }
        catch(Exception e) {
                System.out.println("Exception while reading csv file: " + e);                  
        }
    }
    
    static void generateGPShellScripts() throws IOException {
        
        String capFileName = "AlgTest_v1.3_";
        String packageAID = "6D797061636B616731";
        String appletAID = "6D7970616330303031";
        
        // NXP JCOP CJ3A081
        CardProfiles.generateScript(capFileName + "jc2.2.2.cap", packageAID, appletAID, "NXP_JCOP_CJ3A081", "mode_211", "a000000003000000", "-keyind 0 -keyver 0 -mac_key 404142434445464748494a4b4c4d4e4f -enc_key 404142434445464748494a4b4c4d4e4f");
        // NXP JCOP CJ2A081
        CardProfiles.generateScript(capFileName + "jc2.2.2.cap", packageAID, appletAID, "NXP_JCOP_CJ2A081", "mode_211", "a000000003000000", "-keyind 0 -keyver 0 -mac_key 404142434445464748494a4b4c4d4e4f -enc_key 404142434445464748494a4b4c4d4e4f");
        // NXP JCOP 41 v2.2.1 72K
        CardProfiles.generateScript(capFileName + "jc2.2.1.cap", packageAID, appletAID, "NXP_JCOP_41_v221_72K", "mode_211", "a000000003000000", "-keyind 0 -keyver 0 -mac_key 404142434445464748494a4b4c4d4e4f -enc_key 404142434445464748494a4b4c4d4e4f");
        // NXP JCOP CJ3A080
        CardProfiles.generateScript(capFileName + "jc2.2.1.cap", packageAID, appletAID, "NXP_JCOP_CJ3A080", "mode_211", "a000000003000000", "-keyind 0 -keyver 0 -mac_key 404142434445464748494a4b4c4d4e4f -enc_key 404142434445464748494a4b4c4d4e4f");
        
        // Gemalto_TOP_IM_GXP4
        CardProfiles.generateScript(capFileName + "jc2.2.1.cap", packageAID, appletAID, "Gemalto_TOP_IM_GXP4", "mode_201\r\ngemXpressoPro", "A000000018434D00", "-keyind 0 -keyver 0 -key 47454d5850524553534f53414d504c45");
        // Gemalto_GXP_E64_PK
        CardProfiles.generateScript(capFileName + "jc2.1.2.cap", packageAID, appletAID, "Gemalto_GXP_E64_PK", "mode_201", "A000000018434D00", "-keyind 0 -keyver 0 -mac_key 404142434445464748494a4b4c4d4e4f -enc_key 404142434445464748494a4b4c4d4e4f");
        // Gemalto_GXP_R4_72K
        CardProfiles.generateScript(capFileName + "jc2.2.1.cap", packageAID, appletAID, "Gemalto_GXP_R4_72K", "mode201\r\ngemXpressoPro\n", "A000000018434D00\n", "-keyind 0 -keyver 0 -key 47454d5850524553534f53414d504c45\n");
        // Gemalto_GXP_E32_PK
        CardProfiles.generateScript(capFileName+ "jc2.1.2.cap", packageAID, appletAID, "Gemalto_GXP_E32_PK", "mode_201\r\ngemXpressoPro", "A000000018434D00\n", "-keyind 0 -keyver 0 -key 47454d5850524553534f53414d504c45\n");
        
        // Oberthur Cosmo Dual 72K
        CardProfiles.generateScript(capFileName + "jc2.1.2.cap", packageAID, appletAID, "Oberthur_Cosmo_Dual_72K", "mode_211", "a000000003000000", "-keyind 0 -keyver 0 -mac_key 404142434445464748494a4b4c4d4e4f -enc_key 404142434445464748494a4b4c4d4e4f");
        // TODO: Oberthur Cosmo V7
        // NOTE: neither authentication, nor upload work
        //CardProfiles.generateScript(capFileName + "jc2.2.2.cap", packageAID, appletAID, "Oberthur_Cosmo_V7", "mode_211", "A0000001510000", "-keyind 0 -keyver 0 -mac_key 404142434445464748494a4b4c4d4e4f -enc_key 404142434445464748494a4b4c4d4e4f");

        // Infineon JTOP V2 16K
        CardProfiles.generateScript(capFileName + "jc2.1.2.cap", packageAID, appletAID, "Infineon_JTOP_V2_16K", "mode_201", "a000000003000000", "-keyind 0 -keyver 0 -mac_key 404142434445464748494a4b4c4d4e4f -enc_key 404142434445464748494a4b4c4d4e4f");
        // Infineon JTOP Dual Interface 80k - SLJ 52GLA080AL M8.4
        // NOTE: authentication works, but upload fails with 'install_for_load() returns 0x80206A88 (6A88: Referenced data not found.)' 
        CardProfiles.generateScript(capFileName + "jc2.2.2.cap", packageAID, appletAID, "Infineon_JTOP_Dual_Interface_80k", "mode_211", "", "-keyind 0 -keyver 0 -mac_key 404142434445464748494a4b4c4d4e4f -enc_key 404142434445464748494a4b4c4d4e4f");

        // Cyberflex Palmera V5
        CardProfiles.generateScript(capFileName + "jc2.1.2.cap", packageAID, appletAID, "Cyberflex_Palmera_V5", "mode_201", "a000000003000000", "-keyind 0 -keyver 0 -mac_key 404142434445464748494a4b4c4d4e4f -enc_key 404142434445464748494a4b4c4d4e4f");
        
        // Twin_GCX4_72K_PK
        CardProfiles.generateScript(capFileName + "jc2.2.1.cap", packageAID, appletAID, "Twin_GCX4_72K_PK", "mode_201\r\ngemXpressoPro", "-AID A000000018434D00", "-keyind 0 -keyver 0 -key 47454d5850524553534f53414d504c45");
    }
    
        
    private static void printMembers(Member[] mbrs, String s, String longClassName, String shortClassName) throws IllegalArgumentException, IllegalAccessException {
        int allignSpaceLength = 80;
        int methodIndex = 0;
        out.format("    // %s %s:\n", longClassName, s);
	for (Member mbr : mbrs) {
	    if (mbr instanceof Field) {
                Field value = (Field) mbr;
                value.setAccessible(true);                
                
                String result = value.toGenericString();
                result = result.replace(longClassName, shortClassName);
                result = result.replace(".", "_");
                try {
                    Object o = value.get(null);
                    out.format("    %s", result);
                    for (int i = 0; i < allignSpaceLength - result.length(); i++) out.print(" ");
                    out.format("= %d;\n", o);                
                }
                catch (NullPointerException e) {
                    // not reasonable value
                    int a = 0;                
                }
/*
                try {
                    byte bVal = value.getByte(null);
                    short sVal = value.getShort(null);
                    out.format("MY public final static byte %s = (byte) %d;%n", value.getName(), bVal);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(PerformanceTestingNGTest.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(PerformanceTestingNGTest.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                Annotation[] anot = value.getAnnotationsByType(Consts.Twizzle.class);
		if (anot != null && anot.length > 0) {
                    out.format("  %s%n", anot[0].toString());
                }
*/        
            }
	    else if (mbr instanceof Constructor) 
		out.format("  %s%n", ((Constructor)mbr).toGenericString());
	    else if (mbr instanceof Method) {
                methodIndex++;
                Method value = (Method) mbr;
               
                String result = value.toGenericString();
                result = result.replace(longClassName, shortClassName);
                result = result.replace(".", "_");
                
                String msg = String.format("public static final short %s_%s", shortClassName, value.getName());
                out.format("    %s", msg);
                for (int i = 0; i < allignSpaceLength - msg.length(); i++) out.print(" ");
                out.format("= %d;\n", methodIndex);                 
            }
	}
	if (mbrs.length == 0)
	    out.format("    //  -- No %s --%n", s);
	out.format("%n");
    }    
    
    static void formatClass(String longClassName, String shortClassName) throws ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
        out.format("\n\n\n    // Class %s\n", longClassName);
        Class<?> c = Class.forName(longClassName);
        Field[] fields = c.getDeclaredFields();
        printMembers(fields, "Fields", longClassName, shortClassName);
        Method[] methods = c.getDeclaredMethods();
        printMembers(methods, "Methods", longClassName, shortClassName);
    }

    static void generateJCConstantsFile(String fileName) throws Exception {   
        // NOTE: constants will be generated only for JC library version you included
        // BUGBUG: now only to stdout, store into fileName
        out.format("package AlgTest;\n\n");
        out.format("public class JCConsts { \n");
   
        formatClass("javacard.security.Signature", "Signature");
        formatClass("javacardx.crypto.Cipher", "Cipher");
        formatClass("javacard.security.KeyAgreement", "KeyAgreement");
        formatClass("javacard.security.KeyBuilder", "KeyBuilder");
        formatClass("javacard.security.KeyPair", "KeyPair");
        formatClass("javacard.security.MessageDigest", "MessageDigest");
        formatClass("javacard.security.RandomData", "RandomData");
        formatClass("javacard.security.Checksum", "Checksum");
        formatClass("javacardx.crypto.KeyEncryption", "KeyEncryption");
        formatClass("javacard.security.AESKey", "AESKey");
        formatClass("javacard.security.DESKey", "DESKey");
        formatClass("javacard.security.DSAKey", "DSAKey");
        formatClass("javacard.security.DSAPrivateKey", "DSAPrivateKey");
        formatClass("javacard.security.DSAPublicKey", "DSAPublicKey");
        formatClass("javacard.security.ECKey", "ECKey");
        formatClass("javacard.security.ECPrivateKey", "ECPrivateKey");
        formatClass("javacard.security.ECPublicKey", "ECPublicKey");
        formatClass("javacard.security.HMACKey", "HMACKey");
        formatClass("javacard.security.RSAPrivateCrtKey", "RSAPrivateCrtKey");
        formatClass("javacard.security.RSAPrivateKey", "RSAPrivateKey");
        formatClass("javacard.security.RSAPublicKey", "RSAPublicKey");
        formatClass("javacard.security.SignatureMessageRecovery", "SignatureMessageRecovery");
        
        out.format("} \n");
    }    
            
    
    private static void generateJCInfoHTMLTable(String basePath) throws IOException {        
        File dir = new File(basePath);
        String[] filesArray = dir.list();
        
        if ((filesArray != null) && (dir.isDirectory() == true)) {    
            
            HashMap filesSupport[] = new HashMap[filesArray.length]; 
            
            for (int i = 0; i < filesArray.length; i++) {
                filesSupport[i] = new HashMap();
                JCinfohtml.run(basePath + filesArray[i], filesArray[i]);
            }   
        }
    }
    
    private static void generateGraphsPages(String basePath) throws IOException {        
        File dir = new File(basePath);
        String[] filesArray = dir.list();
        
        if ((filesArray != null) && (dir.isDirectory() == true)) {    
            
            HashMap filesSupport[] = new HashMap[filesArray.length]; 
            
            for (int i = 0; i < filesArray.length; i++) {
                filesSupport[i] = new HashMap();
                JCinfohtml.runGraphsOnePage(basePath + filesArray[i]);
            }   
        }
    }
}

