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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author rk
 */
public class JCinfohtml {    
    public static final String topFunctionsFile = "top.txt";

    public static void beginHTML(FileOutputStream file, String title) throws IOException {
        String toFile = "";
        toFile += "<html lang=\"en\">\n";
        toFile += " <head>\n";
        toFile += "\t<meta charset=\"utf-8\">\n" +
                    "\t<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n"+
                    "\t<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n\n";
        
        toFile += "\t<meta name=\"description\" content=\"The JCAlgTest is a tool designed for automatic gathering various performance properties of Java cards. \">\n" +
                    "\t<meta name=\"author\" content=\"JCAlgTest\">\n"  +
                    "\t<title>"+title+"</title>\n";

        toFile += "\t<link href=\"./dist/css/bootstrap.min.css\" rel=\"stylesheet\">\n"
                + "<script type=\"text/javascript\" src=\"./dist/jquery-2.2.3.min.js\"></script>\n"
                + "\t<link href=\"./assets/css/ie10-viewport-bug-workaround.css\" rel=\"stylesheet\">\n"                
                + "\t<link rel=\"stylesheet\" type=\"text/css\" href=\"./dist/style.css\">\n";
        
        toFile += " </head>\n\n";
        toFile += " <body style=\"margin-top:50px; padding:20px\">\n\n";

        toFile += " \t<nav class=\"navbar navbar-inverse navbar-fixed-top\">\n\t\t<div class=\"container-fluid\">\n\t\t<script type=\"text/javascript\" src=\"header.js\"></script>\n\t\t</div>\n\t</nav>\n\n";

        file.write(toFile.getBytes());
    }
    
    public static void endHTML(FileOutputStream file) throws IOException {
        String toFile = "";
        toFile += "\t<script type=\"text/javascript\" src=\"footer.js\"></script>\n"+
                "<a href=\"#\" class=\"back-to-top\"></a>" +
                "\t<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js\"></script>\n" +
                "\t<script>window.jQuery || document.write('<script src=\"../assets/js/vendor/jquery.min.js\"><\\/script>')</script>\n" +
                "\t<script src=\"./dist/js/bootstrap.min.js\"></script>\n" +
                "\t<script src=\"./assets/js/ie10-viewport-bug-workaround.js\"></script>\n";   
        
        toFile += " </body>\n";
        toFile += "</html>\n";
        file.write(toFile.getBytes());
    }
    
    public static List<String> initalize(String input, StringBuilder cardName) throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader(input));
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
            if (lines.get(lines.size() - 1).startsWith("#")) {
                lines.remove(lines.size() - 1);
            }
            if (lines.get(lines.size() - 1).equals("")) {
                lines.remove(lines.size() - 1);
            }
            if (lines.get(lines.size() - 1).contains("javax.smartcardio.CardException")) {
                lines.remove(lines.size() - 1);
            }

            String cardNameKey = "Card name;";
            if (line.contains(cardNameKey)) {
                String[] info = line.split(";");
                if ((info.length > 1) && (cardName != null)) {
                    cardName.setLength(0);
                    cardName.append(info[1].trim());
                }
            }
        }
        reader.close();
        return lines;
    }

    public static HashMap<String, String> detailsBasic(List<String> lines, FileOutputStream file) throws IOException {
        String toFile;
        String[] info;

        // Transform lines into hashmap
        HashMap<String, String> infoMap = new HashMap<>();
        for (int i = 0; i < 150; i++) {
            info = lines.get(i).split(";");
            if (info.length > 1) {
                infoMap.put(info[0], info[1]);
            }
        }

        toFile = "";
        toFile += "<div class=\"col-md-5 col-xs-5\">\n";
        toFile += "<h3>Test details</h3>\n";
        toFile += "<p>Execution date/time: <strong>" + infoMap.get("Execution date/time") + ", <a href=\"https://github.com/crocs-muni/JCAlgTest/tree/master/Profiles/performance\" target=\"_blank\">CSV source data</a></strong></p>\n";
        toFile += "<p>AlgTestJClient version: <strong>" + infoMap.get("AlgTestJClient version") + "</strong></p>\n";
        toFile += "<p>AlgTest applet version: <strong>" + infoMap.get("AlgTest applet version") + "</strong></p>\n";
        toFile += "<p>Used reader: <strong>" + infoMap.get("Used reader") + "</strong></p>\n";
        toFile += "<p><strong>Card ATR: " + infoMap.get("Card ATR") + "</strong></p>\n";
        toFile += "<p><u><a href=\"https://smartcard-atr.appspot.com/parse?ATR=" + infoMap.get("Card ATR").replaceAll(" ", "") + "\" target=\"_blank\">More information parsed from ATR</a></u></p>\n</br>\n";

        toFile += "<p>JavaCard version: <strong>" + infoMap.get("JCSystem.getVersion()[Major.Minor]") + "</strong></p>\n";
        toFile += "<p>MEMORY_TYPE_PERSISTENT: <strong>" + infoMap.get("JCSystem.MEMORY_TYPE_PERSISTENT") + "</strong></p>\n";
        toFile += "<p>MEMORY_TYPE_TRANSIENT_RESET: <strong>" + infoMap.get("JCSystem.MEMORY_TYPE_TRANSIENT_RESET") + "</strong></p>\n";
        toFile += "<p>MEMORY_TYPE_TRANSIENT_DESELECT: <strong>" + infoMap.get("JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT") + "</strong></p>\n";
        toFile += "\n<h3>How it works</h3>\n";        
        toFile += "<p><strong>If you will spot any discrepancies in the presented data </strong></br>(e.g. strange error, too fast or too slow operation time etc.), "
                + "</br>please open an issue (click at New issue) at <u><a target=\"_blank\" href=\"https://github.com/crocs-muni/JCAlgTest/issues\">GitHub</a></u>. <strong>Thank you!</strong></p>\n";
        toFile += "<p>You can find information about testing on <u><a target=\"_blank\" href=\"https://github.com/crocs-muni/JCAlgTest/wiki\">GitHub wiki</a></u>.</p>\n";
        file.write(toFile.getBytes());

        return infoMap;
    }

    public static void details(List<String> lines, FileOutputStream file) throws IOException {
        HashMap<String, String> infoMap = detailsBasic(lines, file);
        String toFile = "";        
        toFile += "<p>The <b>Operation avg/min/max</b> is the exact number<br> of how many milliseconds the function takes.</p>\n\n";
        toFile += "<p><b>ILLEGAL_VALUE</b>, <b>NO_SUCH_ALGORITHM</b><br> or <b>UNKONWN_ERROR</b> means that the function is <u>not supported.</u></p>\n\n";
        toFile += "</br></div>\n";

        if (infoMap.containsKey("CPLC")) {
            toFile += "<div class=\"col-md-4 col-xs-4\">\n";
            toFile += "<h3>CPLC info</h3>\n";
            toFile += "<p>IC Fabricator: <strong>" + infoMap.get("CPLC.ICFabricator") + "</strong></p>\n";
            toFile += "<p>IC Type: <strong>" + infoMap.get("CPLC.ICType") + "</strong></p>\n";
            toFile += "<p>OS ID: <strong>" + infoMap.get("CPLC.OperatingSystemID") + "</strong></p>\n";
            toFile += "<p>OS Release Date: <strong>" + infoMap.get("CPLC.OperatingSystemReleaseDate") + "</strong></p>\n";
            toFile += "<p>OS Release Level: <strong>" + infoMap.get("CPLC.OperatingSystemReleaseLevel") + "</strong></p>\n";
            toFile += "<p>IC Fabrication Date ((Y DDD) date in that year): <strong>" + infoMap.get("CPLC.ICFabricationDate") + "</strong></p>\n";
            toFile += "<p>IC Serial Number: <strong>" + infoMap.get("CPLC.ICSerialNumber") + "</strong></p>\n";
            toFile += "<p>IC Batch Identifier: <strong>" + infoMap.get("CPLC.ICBatchIdentifier") + "</strong></p>\n";
            toFile += "<p>IC Module Fabricator: <strong>" + infoMap.get("CPLC.ICModuleFabricator") + "</strong></p>\n";
            toFile += "<p>IC Module Packaging Date: <strong>" + infoMap.get("CPLC.ICModulePackagingDate") + "</strong></p>\n";
            toFile += "<p>IC Manufacturer: <strong>" + infoMap.get("CPLC.ICCManufacturer") + "</strong></p>\n";
            toFile += "<p>IC Embedding Date: <strong>" + infoMap.get("CPLC.ICEmbeddingDate") + "</strong></p>\n";
            toFile += "<p>IC Pre Personalizer: <strong>" + infoMap.get("CPLC.ICPrePersonalizer") + "</strong></p>\n";
            toFile += "<p>IC Pre Personalization Equipment Date: <strong>" + infoMap.get("CPLC.ICPrePersonalizationEquipmentDate") + "</strong></p>\n";
            toFile += "<p>IC Pre Personalization Equipment ID: <strong>" + infoMap.get("CPLC.ICPrePersonalizationEquipmentID") + "</strong></p>\n";
            toFile += "<p>IC Personalizer: <strong>" + infoMap.get("CPLC.ICPersonalizer") + "</strong></p>\n";
            toFile += "<p>IC Personalization Date: <strong>" + infoMap.get("CPLC.ICPersonalizationDate") + "</strong></p>\n";
            toFile += "<p>IC Personalization Equipment ID: <strong>" + infoMap.get("CPLC.ICPersonalizationEquipmentID") + "</strong></p>\n";;
        }

        toFile += "</div>\n";
        file.write(toFile.getBytes());
    }

    public static int loadTopFunctions(List<String> topNames, List<String> topAcronyms) throws IOException {
        List<String> topNames_sym = new ArrayList<>();
        List<String> topAcronyms_sym = new ArrayList<>();
        List<String> topNames_asym = new ArrayList<>();
        List<String> topAcronyms_asym = new ArrayList<>();
        loadTopFunctions(topNames_sym, topAcronyms_sym, topNames_asym, topAcronyms_asym);
        topNames.addAll(topNames_sym);
        topNames.addAll(topNames_asym);
        if (topAcronyms != null) {
            topAcronyms.addAll(topAcronyms_sym);
            topAcronyms.addAll(topAcronyms_asym);
        }
        return topNames.size();
    }

    public static void loadTopFunctions(List<String> topNames_sym, List<String> topAcronyms_sym, List<String> topNames_asym, List<String> topAcronyms_asym) throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(topFunctionsFile));
        } catch (IOException e) {
            System.out.println("INFO: Top Functions file not found");
        }

        String[] lineArray;
        String line;

        if (reader != null) {
            while ((line = reader.readLine()) != null) {
                if (!(line.trim().isEmpty())) {
                    lineArray = line.split(";");

                    if (lineArray.length > 2) {
                        if (lineArray[2].trim().equalsIgnoreCase("SYM")) {
                            topNames_sym.add(lineArray[0]);
                            if (topAcronyms_sym != null) {
                                topAcronyms_sym.add(lineArray[1]);
                            }
                        } else if (lineArray[2].trim().equalsIgnoreCase("ASYM")) {
                            topNames_asym.add(lineArray[0]);
                            if (topAcronyms_asym != null) {
                                topAcronyms_asym.add(lineArray[1]);
                            }
                        } else {
                            // if no indication of type of lagorith was provided, put it into topNames_sym list
                            System.out.println("ERROR: Unknown type of algorithm detected when parsting top.txt: " + lineArray[2]);
                        }
                    } else {
                        // if no indication of type of algorithm was provided, put it into topNames_sym list
                        topNames_sym.add(lineArray[0]);
                        if (topAcronyms_sym != null) {
                            topAcronyms_sym.add(lineArray[1]);
                        }
                    }
                }
            }
            reader.close();
        }
    }

    public static List<String> listFilesForFolder(final File folder) {
        List<String> files = new ArrayList<>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                if (fileEntry.getName().contains(".csv")) {
                    files.add(folder + "//" + fileEntry.getName());
                }
            }
        }
        return files;
    }
   
    public static void compareTable(String dir, FileOutputStream file) throws IOException {
        // prepare input data - topFunctions, perf results
        List<String> topNames_sym = new ArrayList<>();
        List<String> topAcronyms_sym = new ArrayList<>();
        List<String> topNames_asym = new ArrayList<>();
        List<String> topAcronyms_asym = new ArrayList<>();
        loadTopFunctions(topNames_sym, topAcronyms_sym, topNames_asym, topAcronyms_asym);
        List<String> files = listFilesForFolder(new File(dir));
        List<String> namesOfCards = new ArrayList<>();
        List<List<Float>> rowsData = new ArrayList<>();
        
        topNames_sym.addAll(topNames_asym);
        topAcronyms_sym.addAll(topAcronyms_asym);
        
        //prepare rows (data added later)
        //one row(ArrayList<Float>) represent one algorithm, one column (get(i) in each row) represent one card
        for(String acr : topAcronyms_sym)            
            rowsData.add(new ArrayList<Float>());
                
        //beginning of graph table
        StringBuilder toFile = new StringBuilder();        
        toFile.append("\t<strong><table class=\"compare\" cellspacing=\"0\" style=\"background:#FEFEFE; border-collapse: separate\"><tbody>\n" +
                        "\t\t<tr>\n\t\t\t<th>"+"Higher percentage = more similar"+"</th>\n");
       
        //load data from input files (fixed-size perf data) and store name of card
        for(String fileName : files)
            namesOfCards.add(addCard(topNames_sym, rowsData, fileName));
           
        //head of table with cards names
        for(String name : namesOfCards)
            toFile.append("\t\t\t<th style=\"text-align: center;\">"+name+"</th>\n");
        
        //end of head of table
        toFile.append("\t\t</tr>\n");
        
        //normalize data
        for(int i=0; i<rowsData.size(); i++){
            Float max = 0.0F;
            for (Float value : rowsData.get(i)){
                if (value>max)
                    max=value;
            }
            
            for(int j=0; j<rowsData.get(i).size(); j++){
                if(rowsData.get(i).get(j) != 0.0F)
                    rowsData.get(i).set(j, rowsData.get(i).get(j)/max);
            }            
        }
        
       
        for(int i=0; i<namesOfCards.size(); i++){
            float avg = 0.0F;
            int sum = 0;
            
            for(int k=0; k<topNames_sym.size(); k++){
                if(rowsData.get(k).get(i) != 0.0F)
                    sum++;
                
                avg += rowsData.get(k).get(i);
            }
                
            System.out.println(namesOfCards.get(i)+"\t"+Math.abs(1-avg/sum));
        }
         
        for(int i=0; i<namesOfCards.size(); i++){
            toFile.append("\t\t<tr>\n");
            toFile.append("\t\t\t<th>"+namesOfCards.get(i)+"</th>\n");
            
            for(int j=0; j<namesOfCards.size(); j++){
                List<String> notSupp = new ArrayList<>();
                float sum = 0.0F;
                int num = 0;
                if(i==j) 
                    toFile.append("\t\t\t<th></th>\n");
                else {
                    for(int k=0; k<topNames_sym.size(); k++){
                        if((rowsData.get(k).get(i) == 0.0F) && (rowsData.get(k).get(j) == 0.0F))
                            {} else {
                                //add method to unsupported list between two cards
                                if((rowsData.get(k).get(i) == 0.0F) || (rowsData.get(k).get(j) == 0.0F)){
                                    notSupp.add(topNames_sym.get(k));  
                            } else {   
                                sum += (rowsData.get(k).get(i) - rowsData.get(k).get(j))*(rowsData.get(k).get(i) - rowsData.get(k).get(j));
                                num++;
                            }
                        }
                    }
                     sum = sum/num;
                     sum = (float)Math.sqrt(sum);
                     sum = Math.abs(sum-1); //convert to percentage, close to 100% = very similar, close to 0% = not similar
                    
                    // toFile.append("\t\t\t<td>"+String.format("%.0f", sum).replace(",", ".")+"</td>\n");
                    toFile.append("\t\t\t<td data-toggle=\"tooltip\" class=\"table-tooltip\" data-html=\"true\" data-original-title=\"");
                    toFile.append("Difference in num. of supported algs: <b>"+notSupp.size()+"/"+topNames_sym.size()+"</b></br>");
                    for(String ns:notSupp)
                        toFile.append(ns+"</br>");
                    
                    if(sum>0.5F)
                        toFile.append("\" style=\"background:rgba(140,200,120,"+String.format("%.2f", (sum*sum*sum*sum*sum*sum)).replace(",", ".")+");\">"+String.format("%.2f", sum*100).replace(",", ".")+"</td>\n");
                    else 
                        toFile.append("\" style=\"background:rgba(200,120,140,"+String.format("%.2f", (Math.abs(sum-1)*Math.abs(sum-1)*Math.abs(sum-1)*Math.abs(sum-1))).replace(",", ".")+");\">"+String.format("%.2f", sum*100).replace(",", ".")+"</td>\n");
                    } 
            }
            toFile.append("\t\t</tr>\n");            
        }
        
        toFile.append("\t\t</tbody></table></strong>\n");
        toFile.append("<script>\n$(document).ready(function () {\n" +
            "  $(\"body\").tooltip({   \n" +
            "    selector: \"[data-toggle='tooltip']\",\n" +
            "    container: \"body\"\n" +
            "  })\n" +
            "});\n</script>\n");        
        file.write(toFile.toString().getBytes());
    }
      
    public static void compareGraph(String dir, FileOutputStream file) throws IOException {
        // prepare input data - topFunctions, perf results
        List<String> topNames_sym = new ArrayList<>();
        List<String> topAcronyms_sym = new ArrayList<>();
        List<String> topNames_asym = new ArrayList<>();
        List<String> topAcronyms_asym = new ArrayList<>();
        loadTopFunctions(topNames_sym, topAcronyms_sym, topNames_asym, topAcronyms_asym);
        List<String> files = listFilesForFolder(new File(dir));
        List<String> rows = new ArrayList<>();
        List<List<Float>> rowsData = new ArrayList<>();
        
        topNames_sym.addAll(topNames_asym);
        topAcronyms_sym.addAll(topAcronyms_asym);
        
        //beginning of graph script
        StringBuilder toFile = new StringBuilder();        
        toFile.append("\t<script type=\"text/javascript\">\n" +
                        "\tgoogle.setOnLoadCallback(drawChart);\n" +
                        "\tfunction drawChart() {\n" +
                        "\t\tvar data = new google.visualization.DataTable();\n" +
                        "\t\tdata.addColumn('string', 'function');\n");
        
        
        //prepare rows - add first value - name of function (data added later)
        for(String acr : topAcronyms_sym){
            rows.add("\t\t['"+acr+"', ");
            rowsData.add(new ArrayList<Float>());
        }
        
        //prepare columns - add cards names to graph
        for(String fileName : files)
            toFile.append("\t\tdata.addColumn('number', '"+addCard(topNames_sym, rowsData, fileName)+"');\n");
        
        toFile.append("\t\tdata.addRows([\n");
        
        //prepare and process data
        for(int i=0; i<rowsData.size(); i++){
            Float max = 0.0F;
            for (Float value : rowsData.get(i)) {
                if (value>max)
                    max=value;
            }            
            
            for(int j=0; j<rowsData.get(i).size(); j++){
                if(rowsData.get(i).get(j) != 0.0F)
                    rowsData.get(i).set(j, 1.0F - rowsData.get(i).get(j)/max);
                
                if(rowsData.get(i).get(j) == 0.0F)
                    rowsData.get(i).set(j, -0.1F);
                
               // rowsData.get(i).set(j, (-rowsData.get(i).get(j)+1)*100);
            }            
        }
        
        //adds data to rows
        for(int i=0; i<rows.size(); i++){
            for(Float value: rowsData.get(i))               
                rows.set(i, rows.get(i) + String.format("%.2f", value).replace(",", ".") + ", ");
        }  
            
        for(String row : rows)
            toFile.append(row+"],\n");
        
        toFile.append("\t\t]);\n\n");
        toFile.append("\t\tvar options = {\n" +
                        "\t\ttitle: 'Card compare',\n" +
                        "\t\tlineWidth: 1,\n" +
                        //"\t\tcurveType: 'function',\n" +
                        "\t\thAxis: {title: 'Name of function' },\n" +
                        "\t\tvAxis: {title: 'Speed | higher=faster | -0.1=Not Supported', viewWindow: {min: -0.15, max: 1.0} },\n" +
                        "\t\tlegend: { position: 'right' }};\n" +
                        "\t\tvar chart = new google.visualization.LineChart(document.getElementById('card_compare'));\n" +
                        "\t\tchart.draw(data, options);}\n" +
                        "\t</script>\n\n" +
                        "\t<div id=\"card_compare\" style=\"width: 100%; height: 100%; min-width:1300px; min-height:700px; margin-left:-50px;\"></div>\n");
                
        file.write(toFile.toString().getBytes());
       }
        
    public static void compareGraphForFunction(String algName, String dir, FileOutputStream file) throws IOException {
        List<String> files = listFilesForFolder(new File(dir));
        List<String> rows = new ArrayList<>();
        List<String> rowsData = new ArrayList<>();

        //beginning of graph script
        StringBuilder toFile = new StringBuilder();        
               toFile.append("\t<script type=\"text/javascript\">\n" +
                        "\tgoogle.setOnLoadCallback(drawChart);\n" +
                        "\tfunction drawChart() {\n" +
                        "\t\tvar data = new google.visualization.DataTable();\n" +
                        "\t\tdata.addColumn('number', 'Cardname');\n");
        
        rowsData.add("[16, "); rowsData.add("[32, "); rowsData.add("[64, ");
        rowsData.add("[128, "); rowsData.add("[256, "); rowsData.add("[512, ");
        
        //prepare columns - add cards names to graph
        for(String fileName : files){
            String cardname = addCardVariable(algName, rowsData, fileName);
            if(cardname != "unsupported")
                toFile.append("\t\tdata.addColumn('number', '"+cardname+"');\n");
        }
        
        toFile.append("\t\tdata.addRows([\n");
        
        for(String row : rowsData)
            toFile.append("\t\t"+row+"],\n");
                
        toFile.append("\t\t]);\n\n");
        toFile.append("\t\tvar options = {\n" +
                        "\t\ttitle: '"+algName+"',\n" +
                        "\t\tlineWidth: 2,\n" +
                        //"\t\tcurveType: 'function',\n" +
                        "\t\thAxis: {title: 'Data length' },\n" +
                        "\t\tvAxis: {title: 'Time of execution'},\n" +
                        "\t\tpointSize: 8,\n" +
                        "\t\tlegend: { position: 'right' }};\n" +
                        "\t\tvar chart = new google.visualization.LineChart(document.getElementById('cardcompare'));\n" +
                        "\t\tchart.draw(data, options);}\n" +
                        "\t</script>\n\n" +
                        "\t<div id=\"cardcompare\" style=\"width: 100%; height: 90%; min-width:1200px; min-height:700px; margin-left:-50px;\"></div>\n");
                
        file.write(toFile.toString().getBytes());
       }  
    
    public static void compareGraphForRSA(String dir, FileOutputStream file) throws IOException {

        List<String> files = listFilesForFolder(new File(dir));
        List<String> rows = new ArrayList<>();
        List<String> rowsData = new ArrayList<>();

        //beginning of graph script
        StringBuilder toFile = new StringBuilder();        
               toFile.append("\t<script type=\"text/javascript\">\n" +
                        "\tgoogle.setOnLoadCallback(drawChart);\n" +
                        "\tfunction drawChart() {\n" +
                        "\t\tvar data = new google.visualization.DataTable();\n" +
                        "\t\tdata.addColumn('number', 'Cardname');\n");
        
        rowsData.add("[512, "); rowsData.add("[768, ");
        rowsData.add("[896, "); rowsData.add("[1024, "); rowsData.add("[1280, ");
        rowsData.add("[1536, "); rowsData.add("[2048, ");
        
        //prepare columns - add cards names to graph
        for(String fileName : files){
            String cardname = addCardVariableRSA(rowsData, fileName);
            if(cardname != "unsupported")
                toFile.append("\t\tdata.addColumn('number', '"+cardname+"');\n");
        }
        
        toFile.append("\t\tdata.addRows([\n");
        
        for(String row : rowsData)
            toFile.append("\t\t"+row+"],\n");
                
        toFile.append("\t\t]);\n\n");
        toFile.append("\t\tvar options = {\n" +
                        "\t\ttitle: '"+"ALG_RSA LENGTH_RSA KeyPair_genKeyPair()"+"',\n" +
                        "\t\tlineWidth: 2,\n" +
                        //"\t\tcurveType: 'function',\n" +
                        "\t\thAxis: {title: 'Data length' },\n" +
                        "\t\tvAxis: {title: 'Time of execution'},\n" +
                        "\t\tpointSize: 8,\n" +
                        "\t\tlegend: { position: 'right' }};\n" +
                        "\t\tvar chart = new google.visualization.LineChart(document.getElementById('cardcompare'));\n" +
                        "\t\tchart.draw(data, options);}\n" +
                        "\t</script>\n\n" +
                        "\t<div id=\"cardcompare\" style=\"width: 100%; height: 90%; min-width:1200px; min-height:700px; margin-left:-50px;\"></div>\n");
                
        file.write(toFile.toString().getBytes());
       } 
    
     /**
     * Parse one card file for compare cards graph
     * @param algs - algs which perf result should be obtained
     * @param rowsData - where data from file will be stored 
     * @param fileName - input file
     * @return card of name
     * @throws IOException 
     */
    public static String addCard(List<String> algs, List<List<Float>> rowsData, String fileName) throws IOException{
        int lp = 15;
        StringBuilder cardName = new StringBuilder();
        List<String> lines = initalize(fileName, cardName);
        if (cardName.toString().isEmpty())
            cardName.append(fileName.substring(fileName.lastIndexOf("/") + 1, fileName.lastIndexOf(".")));
       
        for (int i=0; i<algs.size(); i++) {
                boolean bTopNameFound = false;
                while (lp < lines.size() - 4) {
                    if (lines.get(lp).contains(algs.get(i))) {
                        bTopNameFound = true;
                        rowsData.get(i).add(parseOneForCompareGraph(lines, lp));                                                
                        break;
                    } else {
                        lp++;
                    }
                }
                // In case given algorithm (topname) is not present in measured file, put: "0.0"
                if (!bTopNameFound) 
                    rowsData.get(i).add(0.0F);
                //rows.set(i, rows.get(i) + "0.0, ");                
                lp = 15;                
            }
        
        return cardName.toString();
    }
    
    private static String addCardVariable(String algName, List<String> rows, String fileName) throws IOException{
        int lp = 15;
        StringBuilder cardName = new StringBuilder();
        List<String> lines = initalize(fileName, cardName);
        if (cardName.toString().isEmpty())
            cardName.append(fileName.substring(fileName.lastIndexOf("/") + 1, fileName.lastIndexOf(".")));
                 
        boolean bTopNameFound = false;
        int i=0;
        while (lp < lines.size() - 4) {
            if (lines.get(lp).contains(algName)) { 
                float value = parseOneForCompareGraph(lines, lp);
                //if(value == 0.0F)
                 //   break;
                
                rows.set(i, rows.get(i) + value + ", ");  
                i++;
                lp++;
                bTopNameFound = true; 
            } else {
                lp++;
            }
        }

        // In case given algorithm (topname) is not present in measured file, put -
        if (!bTopNameFound) 
            return "unsupported";
            /*for(String row: rows)
                row+="null, "; */
        
        return cardName.toString();
    } 
    
    private static String addCardVariableRSA(List<String> rows, String fileName) throws IOException{
        int lp = 15;
        StringBuilder cardName = new StringBuilder();
        List<String> lines = initalize(fileName, cardName);
        if (cardName.toString().isEmpty())
            cardName.append(fileName.substring(fileName.lastIndexOf("/") + 1, fileName.lastIndexOf(".")));
        
        String[] sizes = {"512", "768", "896", "1024", "1280", "1536", "2048"};
        
        boolean bTopNameFound = false;
        int i=0;
        while (lp < lines.size() - 4) {            
            if (lines.get(lp).contains("ALG_RSA_CRT LENGTH_RSA_"+sizes[i]+" KeyPair_genKeyPair()")) {
                bTopNameFound = true;
                rows.set(i, rows.get(i) + parseOneForCompareGraph(lines, lp) + ", ");                        
                i++;
                lp++; 
              
                if(i==7)
                    break;
            } else {
                lp++;
            }
        }

        // In case given algorithm (topname) is not present in measured file, put -
        if (!bTopNameFound) 
            return "unsupported";
            /*for(String row: rows)
                row+="null, "; */
        
        return cardName.toString();
    }
     
    public static Float parseOneForCompareGraph(List<String> lines, Integer lp) throws IOException {
        String[] operation;
        Float result = 0.0F;

        if (lines.get(lp + 1).equals("ALREADY_MEASURED"))          
            return result;
         else            
            lp += 2;        

        if ((lines.get(lp).contains("baseline")) && (lines.get(lp + 3).contains("avg op:"))) {
            lp += 3;
            operation = lines.get(lp).trim().split(";");
            result = Float.valueOf(operation[2].replace(",", "."));
            return result;
        } else {
            if (lines.get(lp).contains("baseline") && !(lines.get(lp).contains("error"))) {                
                return result;
            } else if (lines.get(lp).contains("error")) {
                return result;
            } else {
                return result;
            }
        }
    }
    
    public static Integer parseOneForGraph(List<String> lines, StringBuilder toFile, Integer lp) throws IOException {
        String[] operation;
        Float min = -1.0F;
        Float max = -1.0F;
        Float avg = -1.0F;
        String length = "-999";

        if (lines.get(lp + 1).equals("ALREADY_MEASURED")) {
            lp += 2;
            return lp;
        } else {
            lp += 5;
        }

        if ((lines.get(lp).contains("avg op:"))) {
            operation = lines.get(lp).trim().split(";");
            avg = Float.valueOf(operation[2].replace(",", "."));
            min = Float.valueOf(operation[4].replace(",", "."));
            max = Float.valueOf(operation[6].replace(",", "."));
            lp++;
            length = lines.get(lp).trim().split(";")[2];
        }

        if ((min == -1.0) || (max == -1.0) || (avg == -1.0) || (length.equals("-1"))) {
            //toFile.append("\t\t[0, 0, 0, 0, '0', false],\n");
        } else {
            toFile.append(("\t\t[" + length + ", " + avg.toString() + ", " + min.toString() + ", " + max.toString() + ", '" + length + "', false],\n"));
        }

        lp++;
        return lp;
    }
   
    public static Integer parseOneGraph(List<String> lines, String dir, String name, Integer lp) throws FileNotFoundException, IOException {
        StringBuilder toFile = new StringBuilder();
        String methodName = lines.get(lp).split(";")[1];

        toFile.append("<!DOCTYPE html>\n"
                + "<html>\n<head>\n"
                + "\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n"
                + "\t<script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script>\n"
                + "\t<script type=\"text/javascript\">\n"
                + "\tgoogle.setOnLoadCallback(drawFancyVisualization);\n"
                + "\tfunction drawFancyVisualization() {\n"
                + "\t\tvar data = new google.visualization.DataTable();\n"
                + "\t\tdata.addColumn('number', 'length of data (bytes)');\n"
                + "\t\tdata.addColumn('number', 'Time (ms)');\n"
                + "\t\tdata.addColumn({type:'number', role:'interval'});\n"
                + "\t\tdata.addColumn({type:'number', role:'interval'});\n"
                + "\t\tdata.addColumn({type:'string', role:'annotation'});\n"
                + "\t\tdata.addColumn({type:'boolean',role:'certainty'});\n"
                + "\t\tdata.addRows([");

        while (lp < lines.size() - 4) {
            if (lines.get(lp).contains(methodName)) {
                lp = parseOneForGraph(lines, toFile, lp);
            } else {
                if (lines.get(lp).contains("method name:")) {
                    break;
                } else {
                    lp++;
                }
            }
        }

        toFile.append("]);\n\n"
                + "\tvar options = {\n"
                + "\t\ttitle: '" + methodName + "',\n"
                + "\t\ttitleTextStyle: {fontSize: 15},\n"
                + "\t\thAxis: {title: 'length of data (bytes)', viewWindow: {min: 0, max: "+ (toFile.toString().contains("512") ? "530" : "265") +"} },\n"
                + "\t\tvAxis: {title: 'duration of operation (ms)' },\n"
                + "\t\tlegend:'none',};\n\n"
                + "\tvar chart = new google.visualization.LineChart(document.getElementById('" + methodName.trim() + "'));\n"
                + "\tchart.draw(data, options);\n"
                + "\t}\n\n\n\t</script>\n</head>\n<body>\n"
                + "\t<script type=\"text/javascript\" src=\"https://www.google.com/jsapi?autoload={'modules':[{'name':'visualization','version':'1.1','packages':['corechart']}]}\"></script>\n"
                + "\t<div id=\"" + methodName.trim() + "\" style=\"width: 790px; height: 500px;\"></div>\n"
                + "</body>\n</html>");

        if (!(toFile.toString().contains("data.addRows([]);"))) {
            FileOutputStream file = new FileOutputStream(dir + "/" + methodName + ".html");
            file.write(toFile.toString().getBytes());
            file.close();
        }

        return lp;
    }

    public static void parseGraphs(List<String> lines, String dir, String name) throws FileNotFoundException, IOException {
        Integer lp = 15;

        while (lp < lines.size() - 4) {
            if (lines.get(lp).contains("method name:")) {
                lp = parseOneGraph(lines, dir, name, lp);
            } else {
                lp++;
            }
        }
    }
    
    private static void addInfoSimilarity(FileOutputStream file) throws IOException {
        StringBuilder toFile = new StringBuilder();
        toFile.append("<div class=\"container-fluid\">\n");
        toFile.append("<div class=\"row\">\n");
        toFile.append("<h1>Similarity of smart cards based on their performance</h1>\n");
        toFile.append("<h4>The actual performance of tested card when executing required algorithm can be used as useful side-channel for fingerprinting purposes.</h4>\n<br>\n"
                + "<p>Using performance results for many cards, we calculated how much individual pairs of cards differ in the performance of selected important operations (so-called <a href=\"./top-functions.html\">TOP FUNCTIONS</a>).</p>\n"
                + "<p>Each cell in the table represents the (di-) similarity of two cards. A value close to <strong>100%</strong> means that these two cards are very similar, whereas going to <strong>0%</strong> mean significant dissimilarity. When a cursor is placed above value, tooltip showing difference in supported algorithms of exact two cards appears.</p>\n"
                + "<p>From the practical experience, we can say that pair of cards with similarity value over <strong>95%</strong> means either the identical card type or both being members of the same product family with same underlying hardware and very similar implementation of JavaCard Virtual Machine. The similarity in the range of <strong>85% - 95%</strong> usually signals the same family of cards yet with detectable differences (possibly different co-processor for some of the supported algorithms). The global average is about <strong>70%</strong>, with similarity below <strong>50%</strong> encountered for cards from completely different manufacturers.</p>\n");
        
        toFile.append("<br>\n<h4>Why does it work? </h4>\n");
        toFile.append("<p>In contrast to ordinary computers, smart cards are on one side more deterministic (usually, no processes running in parallel) yet more specialized on the hardware level. There is not \"just\" single general purpose CPU running compiled cryptographic algorithm, but a set of specialized circuits dedicated to the acceleration of particular cryptographic algorithm (DES, AES, RSA, ECC co-processor), all optimized for the maximum speed and minimum die space. Performance measurements of cryptographic algorithms can be therefore used as a card's fingerprint which cannot be easily manipulated on the higher software level. "
                + "For example, one cannot re-implement faster RSA on the card's main CPU to mimic the speed of another card. The fastest achievable modular multiplication, on that particular card, is given by the performance of card's co-processor circuit and cannot be improved by the main CPU. </p>\n<br>\n");
                
        file.write(toFile.toString().getBytes());
    }
   
    public static void runGraphs(String input) throws IOException {
        StringBuilder cardName = new StringBuilder();
        List<String> lines = initalize(input, cardName);
        String resultsDir = new File(input).getParentFile().toString();
        resultsDir = resultsDir.substring(0, resultsDir.lastIndexOf("/")) + "/graphs";
        File dir = new File(resultsDir);
        dir.mkdirs();
        parseGraphs(lines, (dir.getAbsolutePath()), cardName.toString());
        System.out.println("Make sure that CSS file & JS files (\"Source\" folder) is present in output folder.");
    }
    
    public static void runCompareGraph(String dir) throws FileNotFoundException, IOException {
        FileOutputStream file = new FileOutputStream(dir + "//" + "compareGraph.html");        
        beginHTML(file, "Card performance - comparative graph");
        compareGraph(dir, file);
        endHTML(file);
        System.out.println("Make sure that CSS file & JS files (\"Source\" folder) is present in output folder.");
    }
    
    public static void runCompareTable(String dir) throws FileNotFoundException, IOException {
        FileOutputStream file = new FileOutputStream(dir + "//" + "similarity-table.html");
        beginHTML(file, "JCAlgTest - Similarity of smart cards");
        addInfoSimilarity(file);
        compareTable(dir, file);
        endHTML(file);
        System.out.println("Make sure that CSS & JS files are present in output folder.");
    }
    /*
    public static void main(String [] args) throws FileNotFoundException, IOException{
        FileOutputStream file = new FileOutputStream("D:/JCAlgTest/variable_03_2016/comparealgSHA.html");
        
        beginHTML(file, "Card performance - algorithm");
        // compareGraphForFunction("ALG_SHA MessageDigest_doFinal()", "D:/JCAlgTest/variable_03_2016/", file);
        // compareGraphForRSA("D:/JCAlgTest/fixed_03_2016/", file);
         endHTML(file);
    }*/
}

