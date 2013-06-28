/*  
    Copyright (c) 2008-2013 Petr Svenda <petr@svenda.com>

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

import java.io.*;
import algtestjclient.CardMngr;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 *
 * @author petr
 */
public class AlgTestProcess {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            if (args.length == 0) {PrintHelp();}
            else { generateHTMLTable(args[0]);}
        } 
        catch (IOException ex) {
            System.out.println("IOException : " + ex);
        }
        catch (Exception ex) {
            System.out.println("Exception : " + ex);
        }
    }
    
    private static void PrintHelp() {
        System.out.println("Usage: java AlgTestProcess.jar base_path\n" 
                + "  base_path\\results\\directory should contain *.csv files with results \n"
                + "  html table will be generated into base_path\\AlgTest_html_table.html \n");
    }
    private static void generateHTMLTable(String basePath) throws IOException {
        String filesPath = basePath + "results\\";
        File dir = new File(filesPath);
        String[] array = dir.list();

        
        if ((array != null) && (dir.isDirectory() == true)) {    
            
            HashMap filesSupport[] = new HashMap[array.length]; 
            
            for (int i = 0; i < array.length; i++) {
                filesSupport[i] = new HashMap();
                parseSupportFile(filesPath + array[i], filesSupport[i]);
            }            
        
            //
            // HTML HEADER
            //
            String fileName = basePath + "AlgTest_html_table.html";
            FileOutputStream file = new FileOutputStream(fileName);                   
            String header = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\r\n<html>\r\n<head>"
                    + "<meta content=\"text/html; charset=utf-8\" http-equiv=\"Content-Type\">\r\n"
                    + "<link type=\"text/css\" href=\"style.css\" rel=\"stylesheet\"><title>JavaCard support test</title></head>\r\n<body>\r\n\r\n"; 

            String cardList = "<b>Tested cards abbreviations:</b><br>\r\n";
            for (int i = 0; i < array.length; i++) {
                cardList += "<b>c" + i + "</b>	" + array[i] + "<br>\r\n";
            }
            cardList += "\r\n\r\n\r\n"; 
            
            file.write(header.getBytes());
            file.write(cardList.getBytes());
            file.flush();          
            
            String table = "<table width=\"730\" border=\"0\" cellspacing=\"2\" cellpadding=\"4\">\r\n";
            file.write(table.getBytes()); file.flush();                  

            //
            // HTML TABLE BODY
            //
            for (String[] classStr : CardMngr.ALL_CLASSES_STR) {
                formatTableAlgorithm_HTML(classStr, filesSupport, file);
            }
            
            //
            // FOOTER
            //
            String footer = "</table>\r\n\r\n\r\n</body></html>";
            file.write(footer.getBytes());

            file.flush();
            file.close();            
        }
        else {
            System.out.println("directory is empty");
        }
        

    }
    
    static void formatTableAlgorithm_HTML(String[] classInfo, HashMap[] filesSupport, FileOutputStream file) throws IOException {
        // class (e.g., javacardx.crypto.Cipher)
        String algorithm = "<tr style='height:12.75pt'>\r\n" + "<td class='dark'>" + classInfo[0] + "</td>\r\n";
        for (int i = 0; i < filesSupport.length; i++) { algorithm += "  <td class='dark_index'>c" + i + "</td>\r\n"; }
        algorithm += "</tr>\r\n";
        // support for particular algorithm from given class
        for (int i = 1; i < classInfo.length; i++) {
            if (!classInfo[i].startsWith("###")) { // ignore special informative types
                algorithm += "<tr style='height:12.75pt'>\r\n";
                algorithm += "  <td class='light'>" + classInfo[i] + "</td>\r\n";

                // Process all files
                for (int fileIndex = 0; fileIndex < filesSupport.length; fileIndex++) { 
                    algorithm += "  ";
                    HashMap fileSuppMap = filesSupport[fileIndex];
                    if (fileSuppMap.containsKey(classInfo[i])) {
                        String secondToken = (String) fileSuppMap.get(classInfo[i]);
                        switch (secondToken) {
                            case "no": algorithm += "<td class='light_no'>no</td>\r\n"; break;
                            case "yes": algorithm += "<td class='light_yes'>yes</td>\r\n"; break;
                            case "error": algorithm += "<td class='light_error'>error</td>\r\n"; break;
                            case "maybe": algorithm += "<td class='light_error'>maybe</td>\r\n"; break;
                            default: {
                                algorithm += "<td class='light_info'>" + secondToken + "</td>\r\n";
                            }
                        }
                    }
                    else {
                        // algorithm not found in support list
                        algorithm += "<td class='light_error'>?</td>\r\n";
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

            //read comma separated file line by line
            while ((strLine = br.readLine()) != null) {
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
        }
        catch(Exception e) {
                System.out.println("Exception while reading csv file: " + e);                  
        }
    }
}
