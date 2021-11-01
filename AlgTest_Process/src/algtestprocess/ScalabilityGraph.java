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

import static algtestprocess.JCinfohtml.*;
import static algtestprocess.RunTime.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;

/**
 * Class provide generation of scalability graphs which shows dependency between length of processed data and operation time.
 * @author rk
 */
public class ScalabilityGraph {  
    public static final String descFunctionsFile = "desc.txt";
    
    public static void beginScalabilityHTML(FileOutputStream file, String title) throws IOException {
        String toFile = "";
        toFile += "<html lang=\"en\">\n";
        toFile += " <head>\n";
        toFile += "\t<meta charset=\"utf-8\">\n" +
                    "\t<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n"+
                    "\t<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n\n";
        
        toFile += "\t<meta name=\"description\" content=\"The JCAlgTest is a tool designed for automatic gathering various performance properties of Java cards. \">\n" +
                    "\t<meta name=\"author\" content=\"JCAlgTest\">\n"  +
                    "\t<title>"+title+"</title>\n";

        toFile += "\t<link href=\"../dist/css/bootstrap.min.css\" rel=\"stylesheet\">\n"
                + "<script type=\"text/javascript\" src=\"../dist/jquery-2.2.3.min.js\"></script>\n"
                + "\t<link href=\"../assets/css/ie10-viewport-bug-workaround.css\" rel=\"stylesheet\">\n"                
                + "\t<link rel=\"stylesheet\" type=\"text/css\" href=\"../dist/style.css\">\n";
        
        toFile += " </head>\n\n";
        toFile += " <body style=\"margin-top:50px; padding:20px\">\n\n";

        toFile += " \t<nav class=\"navbar navbar-inverse navbar-fixed-top\">\n\t\t<div class=\"container-fluid\">\n\t\t<script type=\"text/javascript\" src=\"../header-1.js\"></script>\n\t\t</div>\n\t</nav>\n\n";

        file.write(toFile.getBytes());
    }
    
    private static HashMap<String, String> loadDescription() throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(descFunctionsFile));
        } catch (IOException e) {
           // System.out.println("INFO: Description of functions file not found");
        }

        String[] lineArray;
        String line;
        HashMap<String, String> descMap = new HashMap<>();

        if (reader != null) {
            String name = "";
            Snippet snippet = new Snippet();
            while ((line = reader.readLine()) != null) {
                if (!(line.trim().isEmpty())) { 
                    if(line.contains("/////")){
                        if(!(name.isEmpty())){
                            descMap.put(name, snippet.getSnippet());
                            snippet.clear();
                        }
                        name = line.substring(5).trim();
                    } else {
                        snippet.addline(line);
                    }                              
                }            
            }
        }
        return descMap;        
    }
    
    public static void parseGraphsPage(List<String> lines, FileOutputStream file, String nameOfCard, Boolean toponly) throws FileNotFoundException, IOException {
        List<String> topFunctions = new ArrayList<>();
        List<String> usedFunctions = new ArrayList<>();
        //List<String> dFunctions = new ArrayList<>();
        if(toponly)
            loadTopFunctions(topFunctions, null);
        
        StringBuilder toFile = new StringBuilder();
        StringBuilder chart = new StringBuilder();
        //end of test details (1st div), end of beginning (2nd div)
        toFile.append("</br></div>\n</div>\n\t<script type=\"text/javascript\" src=\"https://www.gstatic.com/charts/loader.js\"></script>\n");
        toFile.append("\n<script>\ngoogle.charts.load('current', {packages: ['corechart']});\n</script>\n");
        Integer lp = 0;
        String methodName = "";
        
        while (lp < lines.size() - 20) {
            if (lines.get(lp).contains("method name:")) {
                methodName = lines.get(lp).split(";")[1];
                if (methodName.startsWith(" ")) {
                    methodName = methodName.substring(1);
                }
                
                if ((topFunctions.contains(methodName)) || (topFunctions.size() == 0)) {
                    chart.append("\t<script type=\"text/javascript\">\n"
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

                    while (lp < lines.size() - 6) {
                        if (lines.get(lp).contains(methodName)) {
                            lp = parseOneForGraph(lines, chart, lp);
                        } else if (lines.get(lp).contains("method name:")) {
                            break;
                        } else {
                            lp++;
                        }
                    }

                    chart.append("]);\n\n"
                            + "\tvar options = {\n"
                           // + "\t\ttitle: '" + methodName + "',\n"
                            //+ "\t\ttitleTextStyle: {fontSize: 15},\n"
                            + "\t\tbackgroundColor: \"transparent\",\n"
                            + "\t\thAxis: {title: 'length of data (bytes)', viewWindow: {min: 0, max: "+ (chart.toString().contains("512") ? "530" : "265") +"} },\n"
                            + "\t\tvAxis: {title: 'duration of operation (ms)' },\n"
                            + "\t\tlegend:'none',};\n\n"
                            + "\tvar chart = new google.visualization.LineChart(document.getElementById('" + methodName.replaceAll(" ", "_") + "'));\n"
                            + "\tchart.draw(data, options);\n"
                            + "\t}\n\t</script>\n");

                    if (!(chart.toString().contains("data.addRows([]);"))) {
                        usedFunctions.add(methodName);
                        toFile.append(chart.toString());
                    }
                    chart.delete(0, chart.length() - 1);
                } else {
                    lp++;
                }
            } else {
                lp++;
            }
        }
        
        HashMap<String, String> descMap = loadDescription();

        BigDecimal sec = new BigDecimal(2 + usedFunctions.size()*0.15);
        sec = sec.setScale(2, BigDecimal.ROUND_HALF_UP);
        toFile.append("<div class=\"row\">\n");
        for (String usedFunction : usedFunctions) {
            toFile.append("\t<div class=\"graph\">\n");
            toFile.append("\t<h4 style=\"margin-left:40px\">"+usedFunction+"</h4>\n");
            
            if(descMap.containsKey(usedFunction)){
                toFile.append("\t<div style=\"margin-left: 40px;\">");
                toFile.append(descMap.get(usedFunction));
                toFile.append("\n\t</div>\n");
            }
            
            if(descMap.containsKey(usedFunction))
                toFile.append("\t<div id=\"" + usedFunction.replaceAll(" ", "_") + "\" style=\"min-height:400px; margin-top:-50px;\">");
            else
                toFile.append("\t<div id=\"" + usedFunction.replaceAll(" ", "_") + "\" style=\"min-height:479px; margin-top:-50px;\">");
            
            // <h4 style=\"text-align: center;\">" + usedFunction + "</h4>
            toFile.append("<p style=\"text-align: center; margin-top:70px\"><strong>GRAPH IS LOADING. </br></br> THIS MAY TAKE <u>"+ sec +"</u> SECONDS DEPENDING ON THE NUMBER OF GRAPHS.</strong></p></div>\n");
            toFile.append("\t</div>\n\n");
        }

        toFile.append("</div>\n<script type=\"text/javascript\" src=\"https://www.gstatic.com/charts/loader.js\"></script>\n");

        //quick links to generated charts at the beginning of html file
        String toFileBegin;
        toFileBegin = "<div class=\"col-md-7 col-xs-7\">\n";        
        toFileBegin += "<h3>Quick links | number of graphs: "+usedFunctions.size()+" | est. load time: "+ sec +" s</h3>\n<ul style=\"list-style-type: circle;\">\n";
        for (String usedFunction : usedFunctions) {
            toFileBegin += "\t<li>" + "<a href=\"#" + usedFunction.replaceAll(" ", "_") + "\">" + usedFunction + "</a>" + "</li>\n";
        }
        toFileBegin += "</ul>\n</div>\n";
        addCardInfo(file, nameOfCard);       
        file.write(toFileBegin.getBytes());             //quick links written

        //test details generated at the beginning of html file
        detailsBasic(lines, file);                      //details written
        file.write("\t\t</div>\n\t\t<div class=\"row\">\n".getBytes());        
        file.write(toFile.toString().getBytes());       //charts written
        file.write("\t\t</div>\n\t</div>".getBytes());
    }
    
    private static void addCardInfo(FileOutputStream file, String name) throws IOException{
        StringBuilder toFile = new StringBuilder();
        toFile.append("\t<div class=\"container-fluid\">\n\t\t<div class=\"row\">\n");
        toFile.append("\t\t\t<h1>Run time results - "+name+"</h1>\n");
        toFile.append("\t\t\t<p>The performance of the card and given algorithm changes with the length of processed data. Here we provide detailed performance for relevant methods expected to process input with variable lengths (e.g., Cipher.doFinal()). We measured the execution time for data lengths of 16, 32, 64, 128, 256 and 512 bytes and visualize in a graph. Multiple measurements of the same method and fixed data length are performed to capture its variability. Read more about how the measurement was done <a href=\"scalability.html\">here</a>.\n</p>\n");
        file.write(toFile.toString().getBytes());
    }
     
    public static String generateScalabilityFile(String input, String outDir, Boolean toponly) throws IOException {
        StringBuilder cardName = new StringBuilder();
        String cardNameFile = "noname_scalability";
        
        List<String> lines = initalize(input, cardName);                            //load lines to from file to List
        String resultsDir = new File(outDir).getParentFile().toString();
        if (!(cardName.toString().equals("")) && !(cardName.toString().equals(" "))){
            cardNameFile = cardName.toString().replaceAll(" ", "");
            cardNameFile = cardNameFile.replaceAll("_", "");
        } 
        
        new File(outDir + "/scalability").mkdirs(); 
        FileOutputStream file = new FileOutputStream(outDir + "/scalability/" + cardNameFile + ".html");
        beginScalabilityHTML(file, "JCAlgTest - Scalability graph - " + cardName.toString());
        parseGraphsPage(lines, file, cardName.toString(), toponly);
        endRunTimeHTML(file);
        //System.out.println("Make sure that CSS file & JS files (\"Source\" folder) is present in output folder.");
        return cardName.toString();
    }
    
    public static List<String> generateScalabilityFolder(String dir, String outDir, Boolean toponly) throws IOException {
        List<String> files = listFilesForFolder(new File(dir));
        List<String> namesOfCards = new ArrayList<>();

        //load data from input files (fixed-size perf data) and store name of card
        for(String filePath : files)
            namesOfCards.add(generateScalabilityFile(filePath, outDir, toponly));
               
        return namesOfCards;
    }
    
    public static void generateScalabilityMain(String dir, String outDir, List<String> namesOfCards) throws IOException {
        FileOutputStream page = new FileOutputStream(outDir + "/scalability/scalability.html");
        beginRunTimeHTML(page, "JCAlgTest - Scalability graphs");
        StringBuilder toFile = new StringBuilder();
        toFile.append("\t<div class=\"container\">\n");
        toFile.append("\t\t<div class=\"row\">\n");        
        toFile.append("\t\t<h1>Algorithm performance for different length of processed data</h1>\n<br>\n");
        toFile.append("\t\t\t<img src=\"../pics/scalability_example.png\" alt=\"Run time table example\" class=\"img-responsive\" align=\"right\" style=\"margin-left:20px; max-width:45%\">\n");
        toFile.append("\t\t<p>The performance of the card and given algorithm changes with the length of processed data. Here we provide detailed performance for relevant methods expected to process input with variable lengths (e.g., Cipher.doFinal()). We measured the execution time for data lengths of 16, 32, 64, 128, 256 and 512 bytes and visualize in a graph. Multiple measurements of the same method and fixed data length are performed to capture its variability. </p>\n");
        toFile.append("\t\t<p>We also included measurements of several methods usually used together in single sequence (e.g., set key, init the engine and sign the data). Note that resulting time is not simply the sum of operations measured separately as JavaCard Virtual Machine itself influence the results same way as caching and other optimizations do for ordinary CPUs. Used key values are randomly generated to prevent JCVM optimizations by skipping already performed initialization.</p>\n");
        toFile.append("\t\t<p>Note that as many algorithms are block-based, you will experience almost the same execution time until the length of the process data exceeds the length of internal algorithm block (e.g, 64  bytes for SHA-2 hash) after which significantly more operations are executed to process another full block. Similar behavior but with a different underlying cause can be observed when the length in processed data exceeds the length of engine's internal memory buffers. Finally, it often makes sense to concatenate multiple independent blocks into a buffer and then call processing method only once as actual processing (e.g., encryption) is only part of the operation - context switch between card's main processor and crypto co-processor may be significant.</p><br>\n");
        toFile.append("\t\t<div class=\"alert alert-warning\" role=\"alert\">It may take a few seconds to load all graphs on page.<br> Click on the target smart card to display its length-dependent performance. If you would like to see measurements of all operations please visit <a href=\"../run_time/execution-time.html\">Performance details page</a>.\n </div>\n");
        toFile.append("\t\t\t</div>\n");
        toFile.append("\t\t<div>\n");
        toFile.append("\t\t<h3>List of tested Java Cards: </h3>\n");
        toFile.append("\t\t<ul class=\"list-group\">\n");
        for(String name : namesOfCards){
            String cardNameFile = name;
            if (!(cardNameFile.equals("")) && !(cardNameFile.equals(" "))){
                cardNameFile = cardNameFile.replaceAll(" ", "");
                cardNameFile = cardNameFile.replaceAll("_", "");
            }
            toFile.append("\t\t\t<li class=\"list-group-item\"><a href=\""+cardNameFile+".html\">"+name+"</a></li>\n");            
        }
        toFile.append("\t\t</ul>\n");
        
        toFile.append("\t\t</div>\n");
        toFile.append("\t</div>\n");
        page.write(toFile.toString().getBytes());
        
        endRunTimeHTML(page);
        page.close();
    }
        
    public static void runScalability(String dir, String outDir, Boolean toponly) throws FileNotFoundException, IOException{
        List<String> namesOfCards = generateScalabilityFolder(dir, outDir, false);
        // sort cards by names
        Collections.sort(namesOfCards);
        generateScalabilityMain(dir, outDir, namesOfCards);
      //  System.out.println("ADD all necessary scripts (header-1.js, RadarChart.js) to new generated folder.");        
    }
    /*
    public static void main(String [] args) throws FileNotFoundException, IOException{
        String dir = "D:/JCAlgTest/variable/";
        List<String> namesOfCards = generateScalabilityFolder(dir, false);
        generateScalabilityMain(dir, namesOfCards);
      //  System.out.println("ADD all necessary scripts (header-1.js, RadarChart.js) to new generated folder.");        
    }*/
    
}
