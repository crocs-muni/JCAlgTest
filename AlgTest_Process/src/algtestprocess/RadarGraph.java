/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algtestprocess;


import static algtestprocess.JCinfohtml.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author rk
 */
public class RadarGraph {
    public static void beginBootstrap(FileOutputStream file, String title) throws IOException {
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
                + "\t<link href=\"../assets/css/ie10-viewport-bug-workaround.css\" rel=\"stylesheet\">\n"
                + "\t<link href=\"../jumbotron.css\" rel=\"stylesheet\">\n"
                + "\t<script src=\"http://d3js.org/d3.v3.min.js\"></script>\n"
                + "\t<script src=\"RadarChart.js\"></script>\n\n";
        
        toFile += " </head>\n\n";
        toFile += " <body>\n\n";

        toFile += " \t<nav class=\"navbar navbar-inverse navbar-fixed-top\">\n\t\t<div class=\"container\">\n\t\t<script type=\"text/javascript\" src=\"header-1.js\"></script>\n\t\t</div>\n\t</nav>\n\n";

        file.write(toFile.getBytes());
    }
    
    public static void endBootstrap(FileOutputStream file) throws IOException {
        String toFile = "";
        toFile += "\t<script type=\"text/javascript\" src=\"../footer.js\"></script>\n"+
                "\t<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js\"></script>\n" +
                "\t<script>window.jQuery || document.write('<script src=\"../assets/js/vendor/jquery.min.js\"><\\/script>')</script>\n" +
                "\t<script src=\"../dist/js/bootstrap.min.js\"></script>\n" +
                "\t<script src=\"../assets/js/ie10-viewport-bug-workaround.js\"></script>\n";   
        
        toFile += " </body>\n";
        toFile += "</html>\n";
        file.write(toFile.getBytes());
    }
    
    public static List<String> generateRadarGraphs(String dir) throws IOException {
        // prepare input data - topFunctions, perf results
        List<String> topNames_sym = new ArrayList<>();
        List<String> topAcronyms_sym = new ArrayList<>();
        List<String> topNames_asym = new ArrayList<>();
        List<String> topAcronyms_asym = new ArrayList<>();
        loadTopFunctions(topNames_sym, topAcronyms_sym, topNames_asym, topAcronyms_asym);
        List<String> files = listFilesForFolder(new File(dir));
        List<String> namesOfCards = new ArrayList<>();
        List<List<Float>> rowsData = new ArrayList<>();
        List<List<Float>> rowsDataCopy = new ArrayList<>();
        
        topNames_sym.addAll(topNames_asym);
        topAcronyms_sym.addAll(topAcronyms_asym);
        
        //prepare rows (data added later)
        //one row(ArrayList<Float>) represent one algorithm, one column (get(i) in each row) represent one card
        for(String acr : topAcronyms_sym){            
            rowsData.add(new ArrayList<Float>());
            rowsDataCopy.add(new ArrayList<Float>());
        }
       
        //load data from input files (fixed-size perf data) and store name of card
        for(String fileName : files){
            namesOfCards.add(addCard(topNames_sym, rowsData, fileName)); 
            addCard(topNames_sym, rowsDataCopy, fileName);
        }

        //normalize data
        for(int i=0; i<rowsData.size(); i++){
            Float max = 0.0F;
            for (Float value : rowsData.get(i)){
                if (value>max)
                    max=value;
            }
            
            for(int j=0; j<rowsData.get(i).size(); j++){
                if(rowsData.get(i).get(j) != 0.0F)
                    rowsData.get(i).set(j, rowsData.get(i).get(j)/(max*1.11F));
            }            
        }
        
        new File(dir+"/radar_graphs").mkdirs(); 
    
        for(int i=0; i<namesOfCards.size(); i++){
            String cardNameFile = namesOfCards.get(i);
            if (!(namesOfCards.get(i).equals("")) && !(namesOfCards.get(i).equals(" "))){
                cardNameFile = cardNameFile.replaceAll(" ", "");
                cardNameFile = cardNameFile.replaceAll("_", "");
            }
         
            FileOutputStream html = new FileOutputStream(dir + "/radar_graphs/" + cardNameFile + ".html");
            FileOutputStream script = new FileOutputStream(dir + "/radar_graphs/" + cardNameFile + ".js");

            StringBuilder toFile = new StringBuilder();

            toFile.append("var w = document.getElementById('chart').offsetWidth,\n");
            toFile.append("    h = window.innerHeight -70;\n");
            toFile.append("var colorscale = d3.scale.category10();\n");
            toFile.append("var data = [\n[\n");

            for(int k=0; k<topAcronyms_sym.size(); k++)
                if(rowsData.get(k).get(i) == 0.0F){
                    toFile.append("{axis:\"" +topAcronyms_sym.get(k)+"\",value:"+0.0F+",title:\""+"NS"+"\"},\n");    
                } else {
                    toFile.append("{axis:\"" +topAcronyms_sym.get(k)+"\",value:"+String.format("%.3f", Math.abs(1-rowsData.get(k).get(i))).replaceAll(",", ".")+",title:\""+rowsDataCopy.get(k).get(i)+" ms\"},\n");
                }

            toFile.append("],\n];\n\n");
            toFile.append("var config = {");
            toFile.append(" w: w-175,\n h: h-175,\n maxValue: 1.0,\n levels: 10,\n }\n\n");
            toFile.append("RadarChart.draw(\"#chart\", data, config);");

            script.write(toFile.toString().getBytes());
            script.close();

            toFile = new StringBuilder();
            beginBootstrap(html, "JCAlgTest - " + namesOfCards.get(i) + "radar graph");
            
            toFile.append("\t<div class=\"container\">\n");
            toFile.append("\t\t<div class=\"row\">\n");
            toFile.append("\t\t<h2>"+namesOfCards.get(i)+"</h2>\n");
            toFile.append("\t\t<p>Radar graph provides visual overview of Java Card performance. It is composed of 25 frequently used functions (TOP FUNCTIONS).</p>\n");
            toFile.append("\t\t<p>The closest value to 100% represents the fastest result in particular method from all tested cards. Values closer to 10% supply slower results. 0% value means unsupported or not tested algorithms.</p><br>\n");
            toFile.append("\t\t<div id=\"chart\"></div>\n\t\t</div>\n");
            toFile.append("\t\t<script type=\"text/javascript\" src=\""+cardNameFile+".js\"></script>\n");
            toFile.append("\t</div>\n");
            html.write(toFile.toString().getBytes());
            
            endBootstrap(html);
            html.close();            
        }
        
        /*
        //Calculate overal performance value for each card (input for graph generation in Excel)
        for(int i=0; i<namesOfCards.size(); i++){
            float avg = 0.0F;
            int sum = 0;            
            for(int k=0; k<topNames_sym.size(); k++){
                if(rowsData.get(k).get(i) != 0.0F)
                    sum++;                
                avg += rowsData.get(k).get(i);
            }                
            System.out.println(namesOfCards.get(i)+"\t"+ Float.toString(Math.abs(1-avg/sum)).replaceAll(".", ","));
        }
       */
        
        return namesOfCards;
    }
    
    public static void generateRadarMain(String dir, List<String> namesOfCards) throws IOException {
        FileOutputStream page = new FileOutputStream(dir + "/radar_graphs/radar_graphs.html");
        beginBootstrap(page, "JCAlgTest - performance radar graphs");
        StringBuilder toFile = new StringBuilder();
        toFile.append("\t<div class=\"container\">\n");
        toFile.append("\t\t<div class=\"row\">\n");
        toFile.append("\t\t\t<div class=\"col-md-8 col-xs-8\">\n");
        toFile.append("\t\t<h2>Performance radar graphs</h2>\n");
        toFile.append("\t\t<p>Radar graph provides visual overview of Java Card performance.</p>\n");
        toFile.append("\t\t<p>It is composed of 25 frequently used functions (TOP FUNCTIONS).</p>\n");
        toFile.append("\t\t<p>The closest value to 100% represents the fastest result in particular method from all tested cards. Values closer to 10% supply slower results. 0% value means unsupported or not tested algorithms.</p><br>\n");
        toFile.append("\t\t<div class=\"alert alert-warning\" role=\"alert\">Radar graphs are only approximate. For accurate results check run-times in the Performance testing section.</div>\n");
        toFile.append("\t\t\t</div>\n");
        toFile.append("\t\t\t<div class=\"col-md-4 col-xs-4\" style=\"overflow:hidden; margin:2em auto;\">\n");
        toFile.append("\t\t\t<img src=\"../pics/radar_chart_example.png\" alt=\"Radar chart example\" class=\"img-responsive\" align=\"right\">\n");
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
        
        endBootstrap(page);
        page.close();
    }
    
    
    public static void main(String [] args) throws FileNotFoundException, IOException{
        String dir = "D:/JCAlgTest/fixed/";
        List<String> namesOfCards = generateRadarGraphs(dir);
        generateRadarMain(dir, namesOfCards);
        System.out.println("ADD all necessary scripts (header-1.js, RadarChart.js) to new generated folder.");        
    }
}

