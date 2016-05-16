/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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

/**
 *
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
        toFile.append("</br></div>\n</div>\n\t<script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script>\n");
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

        toFile.append("</div>\n<script type=\"text/javascript\" src=\"https://www.google.com/jsapi?autoload={'modules':[{'name':'visualization','version':'1.1','packages':['corechart']}]}\"></script>\n");

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
        toFile.append("\t\t\t<p>This file was generated by JCAlgTest tool</p>\n");
        file.write(toFile.toString().getBytes());
    }
     
    public static String generateScalabilityFile(String input, Boolean toponly) throws IOException {
        StringBuilder cardName = new StringBuilder();
        String cardNameFile = "noname_scalability";
        
        List<String> lines = initalize(input, cardName);                            //load lines to from file to List
        String resultsDir = new File(input).getParentFile().toString();
        if (!(cardName.toString().equals("")) && !(cardName.toString().equals(" "))){
            cardNameFile = cardName.toString().replaceAll(" ", "");
            cardNameFile = cardNameFile.replaceAll("_", "");
        } 
        
        new File(resultsDir+"/scalability").mkdirs(); 
        FileOutputStream file = new FileOutputStream(resultsDir + "/scalability/" + cardNameFile + ".html");
        beginScalabilityHTML(file, "JCAlgTest - Scalability graph - " + cardName.toString());
        parseGraphsPage(lines, file, cardName.toString(), toponly);
        endRunTimeHTML(file);
        //System.out.println("Make sure that CSS file & JS files (\"Source\" folder) is present in output folder.");
        return cardName.toString();
    }
    
    public static List<String> generateScalabilityFolder(String dir, Boolean toponly) throws IOException {
        List<String> files = listFilesForFolder(new File(dir));
        List<String> namesOfCards = new ArrayList<>();

        //load data from input files (fixed-size perf data) and store name of card
        for(String filePath : files)
            namesOfCards.add(generateScalabilityFile(filePath, toponly));
               
        return namesOfCards;
    }
    
    public static void generateScalabilityMain(String dir, List<String> namesOfCards) throws IOException {
        FileOutputStream page = new FileOutputStream(dir + "/scalability/scalability.html");
        beginRunTimeHTML(page, "JCAlgTest - Scalability graphs");
        StringBuilder toFile = new StringBuilder();
        toFile.append("\t<div class=\"container\">\n");
        toFile.append("\t\t<div class=\"row\">\n");
        toFile.append("\t\t\t<div class=\"col-md-7 col-xs-7\">\n");
        toFile.append("\t\t<h1>Scalability graphs</h1>\n");
        toFile.append("\t\t<h4>Data dependency graphs can be used to reveal the time required to complete operation relying on data length. </h4>\n");
        toFile.append("\t\t<p>It is build upon candle chart. In default, 6 data lengths (16, 32, 64, 128, 256, 512 bytes) are tested. As the run time tests are performed several times, graphs are also showing minimal and maximal time of execution.</p>\n");
        toFile.append("\t\t<p> While hovering a cursor above point, a tooltip with run time values is displayed.</p><br>\n");
        toFile.append("\t\t<div class=\"alert alert-warning\" role=\"alert\">You can find detailed test results in <a href=\"./run_time/execution-time.html\">It may take a few seconds to load all graphs on page (depends on the number of graphs).</a> section.\n </div>\n");
        toFile.append("\t\t\t</div>\n");
        toFile.append("\t\t\t<div class=\"col-md-5 col-xs-5\" style=\"overflow:hidden; margin:2em auto;\">\n");
        toFile.append("\t\t\t<img src=\"../pics/scalability_example.png\" alt=\"Run time table example\" class=\"img-responsive\" align=\"right\">\n");
        toFile.append("\t\t\t</div>\n");
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
        
    public static void runScalability(String dir, Boolean toponly) throws FileNotFoundException, IOException{
        List<String> namesOfCards = generateScalabilityFolder(dir, false);
        generateScalabilityMain(dir, namesOfCards);
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
