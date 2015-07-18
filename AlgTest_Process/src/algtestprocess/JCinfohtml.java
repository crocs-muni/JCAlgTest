/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algtestprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


/**
 *
 * @author rk
 */
public class JCinfohtml {
    
    public static final String TABLE_HEAD = "<table cellspacing='0'> <!-- cellspacing='0' is important, must stay -->\n\t<tr><th style=\"width: 330px;\">Name of function</th><th><b>Operation average (ms/op)</b></th><th>Operation minimum (ms/op)</th><th>Operation maximum (ms/op)</th><th>Prepare average (ms/op)</th><th>Prepare minimum (ms/op)</th><th>Prepare maximum (ms/op)</th><th>Data length</th><th>Iterations & Invocations</th></tr><!-- Table Header -->\n";
    public static final List<String> category = Arrays.asList("MESSAGE DIGEST", "RANDOM GENERATOR", "CIPHER", "SIGNATURE", "CHECKSUM", "AESKey", "DESKey", "KoreanSEEDKey", "DSAPrivateKey", "DSAPublicKey", "ECF2MPublicKey", "ECF2MPrivateKey", "ECFPPublicKey", "HMACKey", "RSAPrivateKey", "RSAPublicKey", "RSAPrivateCRTKey", "KEY PAIR", "UTIL", "SWALGS");
    public static final String topFunctionsFile = "top.txt";  
    private static int lp = 0; //parse file line position
    private static String toFile = "";
    
    public static List<String> initalize(String input, StringBuilder cardName) throws FileNotFoundException, IOException{
        BufferedReader reader = new BufferedReader(new FileReader(input));
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {               
            lines.add(line);
            if (lines.get(lines.size()-1).startsWith("#"))
                lines.remove(lines.size()-1);
            if (lines.get(lines.size()-1).equals(""))
                lines.remove(lines.size()-1);

            String cardNameKey = "Card name;";
            if (line.contains(cardNameKey)) {
                String[] info = line.split(";");
                if ((info.length > 1) && (cardName != null)){
                    cardName.setLength(0);
                    cardName.append(info[1].trim());
                }
            }
        }
        reader.close();
        return lines;
    }
    
    public static void parse(List<String> lines, FileOutputStream file) throws IOException{           
            int tp = 0;
            
            while(lp<lines.size()){
               for (String cat : category)
                   if((cat.equals(lines.get(lp))) || (lines.get(lp).contains("END"))){
                           lp--;                           
                           return;
                   } 
               
               parseOne(lines, file, tp);
               tp++;
               file.write(toFile.getBytes());
               toFile = ""; 
        }
    }
    
    public static void details(List<String> lines, FileOutputStream file) throws IOException{ 
        String[] info;
        
        // Transform lines into hashmap
        HashMap<String, String> infoMap = new HashMap<>();
        for(int i=0; i<150; i++){
            info = lines.get(i).split(";"); 
            if (info.length > 1)
                infoMap.put(info[0], info[1]);            
        }
    
        toFile = "";
        toFile += "<div class=\"pageColumnRight\">\n"; 
        toFile += "<h3>Test details</h3>\n";        
        toFile +="<p>Execution date/time: <strong>"+infoMap.get("Execution date/time")+"</strong></p>\n";
        toFile +="<p>AlgTestJClient version: <strong>"+infoMap.get("AlgTestJClient version")+"</strong></p>\n";
        toFile +="<p>AlgTest applet version: <strong>"+infoMap.get("AlgTest applet version")+"</strong></p>\n";
        toFile +="<p>Used reader: <strong>"+infoMap.get("Used reader")+"</strong></p>\n";
        toFile +="<p><strong>Card ATR: "+infoMap.get("Card ATR")+"</strong></p></br>\n";
        toFile +="<p><u><a href=\"https://smartcard-atr.appspot.com/parse?ATR="+infoMap.get("Card ATR").replaceAll(" ","")+"\" target=\"_blank\">Smart card ATR parsing link</a></u></p>\n</br>\n";
        file.write(toFile.getBytes());
        
        toFile = "";
        toFile +="<p>JavaCard version: <strong>"+infoMap.get("JCSystem.getVersion()[Major.Minor]")+"</strong></p>\n";
        toFile +="<p>MEMORY_TYPE_PERSISTENT: <strong>"+infoMap.get("JCSystem.MEMORY_TYPE_PERSISTENT")+"</strong></p>\n";
        toFile +="<p>MEMORY_TYPE_TRANSIENT_RESET: <strong>"+infoMap.get("JCSystem.MEMORY_TYPE_TRANSIENT_RESET")+"</strong></p>\n";
        toFile +="<p>MEMORY_TYPE_TRANSIENT_DESELECT: <strong>"+infoMap.get("JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT")+"</strong></p>\n";  
        toFile +="</br>\n<h3>How it works</h3>\n";
        toFile +="<p>You can find information about testing on <a href=\"http://www.fi.muni.cz/~xsvenda/jcsupport.html\">GitHub wiki</a>.</p>\n</br>\n"; 
        toFile +="</div>\n</div>\n";
        file.write(toFile.getBytes());
        toFile = "";
    }
    
    
     public static void quickLinks(FileOutputStream file) throws IOException{ 
        toFile= "<div class=\"pageColumnLeft\">\n";
        toFile+= "<h3>Quick links</h3>\n<ul style=\"list-style-type: circle;\">\n";
        toFile+="\t<li>"+ "<a href=\"#TOP\">TOP FUNCTIONS</a>"+"</li>\n";
        
        for (String cat : category) {
            toFile+="\t<li>"+ "<a href=\"#"+cat.replaceAll(" ", "_")+"\">"+cat+"</a>" +"</li>\n";
        }
        
        toFile+= "</ul>\n</div>\n";
        file.write(toFile.getBytes());
        toFile = "";      
     }
     
     public static void begin(FileOutputStream file, String headline) throws IOException{ 
        toFile = "";
        toFile += "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n";
        toFile += "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n";
        toFile += "<head>\n" + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" ;
        toFile += "<title>JCAlgTest Performance test</title>\n";
        toFile += "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\">\n";
        toFile += "<script src=\"http://ajax.googleapis.com/ajax/libs/jquery/2.0.0/jquery.min.js\"></script>\n";
        toFile += "<script type=\"text/javascript\" src=\"jquery-latest.js\"></script>\n" +
                  "<script type=\"text/javascript\" src=\"jquery.tablesorter.js\"></script>\n" +
                  "<script type=\"text/javascript\" id=\"js\">\n" +
                  "\t$.tablesorter.addParser({\n" +
                  "\t\tid: 'Error',\n" +
                  "\t\tis: function(s) {\n" +
                  "\t\t\treturn true;\n" +
                  "\t\t},\n" +
                  "\tformat: function(s) {\n" +
                  "\t\treturn s.toLowerCase().replace(/-/,99999);\n" +
                  "\t},\n\t\ttype: 'numeric'\n\t});\n\n" +
 		  "\t$(function() {\n" +
                  "\t$(\"#sortable\").tablesorter({\n" +
                  "\t\theaders: { \n" +
                  "\t\t\t1:{sorter:'error'}, 2:{sorter:'error'}, 3:{sorter:'error'}, 4:{sorter:'error'}, 5:{sorter:'error'}, " +
                  "6:{sorter:'error'}, 7:{sorter:'error'}, 8:{sorter:'error'}, 9:{sorter:'error'}, 10:{sorter:'error'}, \n" +
                  "\t\t\t11:{sorter:'error'}, 12:{sorter:'error'}, 13:{sorter:'error'}, 14:{sorter:'error'}, 15:{sorter:'error'}, " +
                  "16:{sorter:'error'}, 17:{sorter:'error'}, 18:{sorter:'error'}, 19:{sorter:'error'}, 20:{sorter:'error'}, \n" +
                  "\t\t\t21:{sorter:'error'}, 22:{sorter:'error'}, 23:{sorter:'error'}, 24:{sorter:'error'}, 25:{sorter:'error'}, " +
                  "26:{sorter:'error'}, 27:{sorter:'error'}, 28:{sorter:'error'}, 29:{sorter:'error'}, 30:{sorter:'error'} \n" +
                  "\t\t\t}\t\t});\n\t});\n</script>\n";
        toFile += "<script>\n" + "\tjQuery(document).ready(function(){\n\tvar offset = 220;var duration = 500;\n" +
                  "\t\tjQuery(window).scroll(function(){\n\tif (jQuery(this).scrollTop()>offset){jQuery('.back-to-top').fadeIn(duration);\n" +
                  "\t}else{jQuery('.back-to-top').fadeOut(duration);}});\n" + 
                  "\t\tjQuery('.back-to-top').click(function(event){event.preventDefault();\n" +
                  "\t\tjQuery('html, body').animate({scrollTop: 0}, duration);\n" +
                  "\treturn false;})});\n</script>\n";
        toFile += "</head>\n"; 
        toFile += "<body>\n</br>\n<a href=\"#\" class=\"back-to-top\">Back to Top</a>\n<div class=\"main\">\n";
        toFile += "<div style=\"margin:15px 20px 10px 20px;\">\n";
        toFile += "\t<a href=\"#\"><img  src=\"data:image/png;base64,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\" alt=\"logo\" border=\"0\" align=\"left\"></a>\n</div>\n";
        toFile += "<div style=\"margin:10px 20px 10px 150px;\">\n" + "\t<h1>"+ headline +"</h1>\n";
        toFile += "\t<p style=\"margin-left:20px;\">INFO: This file was generated by AlgTest utility. See <a href=\"http://www.fi.muni.cz/~xsvenda/jcsupport.html\">this website</a>  for more results, source codes and other details.</p>\n" 
                + "</div>\n </br>\n";
        
        file.write(toFile.getBytes());
        toFile = "";      
     }
     
     public static void tableGenerator(List<String> lines, FileOutputStream file) throws IOException{ 
        while(lp < lines.size()-2) {
            lp++;            
            for (String cat : category) {
                if (lines.get(lp).equals(cat) && !((lines.get(lp+1)).contains("END") || (lines.get(lp+2)).contains("END"))) {                    
                    toFile += "<h3 id=\""+cat.replaceAll(" ", "_")+"\">"+cat+"</h3>\n";     //test category name
                    lp++;
                    if (lines.get(lp).contains("data")){
                        toFile += "<p>" +lines.get(lp)+ "</p>\n";           // info about length of data
                        lp++;
                    }
                    
                    toFile += TABLE_HEAD;                                   // head of table 
                    JCinfohtml.parse(lines, file);                          //parsing info of separate tests
                    toFile += "</table>\n</br>\n";                          //end of table
                    file.write(toFile.getBytes());
                    toFile = "";
                }                
            }            
        }   
     }
     
     public static void loadTopFunctions(List<String> topNames, List<String> topAcronyms) throws IOException{
        BufferedReader reader;
        reader = new BufferedReader(new FileReader(topFunctionsFile));        
        String [] lineArray;        
        String line;
        
        while ((line = reader.readLine()) != null) {
            lineArray = line.split(";");
            topNames.add(lineArray[0]);
            if (topAcronyms != null)
                topAcronyms.add(lineArray[1]);            
        }
        reader.close();
     }
     
     public static List<String> listFilesForFolder(final File folder) {
        List<String> files = new ArrayList<>();  
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                if (fileEntry.getName().contains(".csv"))
                    files.add(folder+"//"+fileEntry.getName());
            }
        }
        return files;
    }
     
     public static void sortableGenerator(String dir, FileOutputStream file) throws IOException{ 
        List<String> topNames = new ArrayList<>(); 
        List<String> topAcronyms = new ArrayList<>();
        loadTopFunctions(topNames, topAcronyms);
        List<String> files = listFilesForFolder(new File(dir)); 
        lp=0;
        toFile +="<ul style=\"list-style-type:circle; font-size:14px; line-height:120%;\">\n";
        for (int i=0; i<topNames.size(); i++){
            toFile +="\t<li><strong>"+topAcronyms.get(i)+"</strong> = "+topNames.get(i)+"</li>\n";
        }
        toFile +="</ul>\n</br>\n";         

        toFile += "<table id=\"sortable\" class=\"tablesorter\" cellspacing='0'>\n";
        toFile += "\t<thead><tr>\n\t<th>CARD/FUNCTION</th>";
         
        for (String topAcronym : topAcronyms) 
            toFile += "<th>" + topAcronym + "</th>";    
         
        toFile += "</tr>\n</thead>\n<tbody>\n";         
        
        for (String filename : files){
            String name = filename.substring(filename.lastIndexOf("/")+1, filename.indexOf("."));
            toFile += "<tr><td><strong>"+name+"</strong></td>";
            List<String> lines = initalize(filename, null);
            for (String topName : topNames){
                while(lp<lines.size()-4){
                    if(lines.get(lp).contains(topName)){
                        parseOneSortable(lines, file);
                        file.write(toFile.getBytes());
                        toFile = "";
                    } else {
                        lp++;
                    }
                }
                lp=0;
            }
            toFile += "</tr>\n";
         }
        
        toFile += "</tbody>\n</table>\n</br>\n";                                  //end of table
        file.write(toFile.getBytes());
        toFile = "";
        lp=0;
     }
     
     public static void topFunction(List<String> lines, FileOutputStream file) throws IOException{
        int tp = 0;
        List<String> topNames = new ArrayList<>();        
        loadTopFunctions(topNames, null);
        
        toFile += "<h3 id=\"TOP\">TOP FUNCTIONS</h3>\n";                              //name of table
        toFile += TABLE_HEAD;
        
        for (String top : topNames){
            while(lp<lines.size()-4){
            if(lines.get(lp).contains(top)){
                parseOne(lines, file, tp);
                file.write(toFile.getBytes());
                toFile = "";
            } else {
                lp++;
            }            
        }
            lp=0;
        }
        
        toFile += "</table>\n</br>\n";                                  //end of table
        file.write(toFile.getBytes());
        toFile = "";
        lp=0;
     }
     
     public static void parseOne(List<String> lines, FileOutputStream file, int tp){
        String [] prepare;
        String [] operation;
         
        if (lines.get(lp+1).equals("ALREADY_MEASURED")){
            lp+=2;
            return;
        } else {
        toFile += ((tp % 2)==0) ? "\t<tr>" : "\t<tr class='even'>";
        prepare = lines.get(lp).trim().split(";");
        toFile += "<td><b>"+prepare[1]+"</b></td>";
        lp+=2;
        }
               
         if ((lines.get(lp).contains("baseline")) && (lines.get(lp+3).contains("avg op:"))){
              lp++;           
              prepare = lines.get(lp).trim().split(";");           
              lp +=2;
              operation = lines.get(lp).trim().split(";");
              
              toFile += "<td style=\"font-size: 110%; font-weight: bold;\">"+Float.valueOf(operation[2].replace(",","."))+"</td>";
              toFile += "<td>"+Float.valueOf(operation[4].replace(",","."))+"</td>";
              toFile += "<td>"+Float.valueOf(operation[6].replace(",","."))+"</td>";
              toFile += "<td>"+Float.valueOf(prepare[2].replace(",","."))+"</td>";
              toFile += "<td>"+Float.valueOf(prepare[4].replace(",","."))+"</td>";
              toFile += "<td>"+Float.valueOf(prepare[6].replace(",","."))+"</td>"; 
              lp++;
              prepare = lines.get(lp).trim().split(";");
              toFile += "<td>"+ Integer.parseInt(prepare[2]) +"</td>";
              toFile += "<td>"+ Integer.parseInt(prepare[4]) + "/" + Integer.parseInt(prepare[6])+"</td>";
         } else {             
             if(lines.get(lp).contains("baseline") && !(lines.get(lp).contains("error"))){
                 lp++;
                 prepare = lines.get(lp).trim().split(";");
                 lp++;
                 toFile += "<td colspan=\"3\">"+lines.get(lp)+"</td>";
                 toFile += "<td>"+Float.valueOf(prepare[2].replace(",","."))+"</td>";
                 toFile += "<td>"+Float.valueOf(prepare[4].replace(",","."))+"</td>";
                 toFile += "<td>"+Float.valueOf(prepare[6].replace(",","."))+"</td>"; 
                 
             } else 
             if (lines.get(lp).contains("error")){
                 prepare = lines.get(lp).trim().split(";"); 
                 toFile += "<td>"+prepare[prepare.length-1]+"</td>";
             }
             else
             {
             toFile += "<td>"+lines.get(lp)+"</td>";
             }                   
         }  

         toFile += "</tr>\n";
         lp++;
     }
     
    public static void parseOneSortable(List<String> lines, FileOutputStream file){
        String [] prepare;
        String [] operation;
        
        String title = "";
        String value = "";
        if (lines.get(lp+1).equals("ALREADY_MEASURED")){
            lp+=2;
            return;
        } else {
            lp+=2;
        }
               
         if ((lines.get(lp).contains("baseline")) && (lines.get(lp+3).contains("avg op:"))){                      
              lp+=3;
              operation = lines.get(lp).trim().split(";");
              
              value += Float.valueOf(operation[2].replace(",","."));
              title += "min: " + Float.valueOf(operation[4].replace(",",".")) + "; max: ";
              title += Float.valueOf(operation[6].replace(",","."));
              lp++;
              
         } else {             
             if(lines.get(lp).contains("baseline") && !(lines.get(lp).contains("error"))){
                 lp+=2;                
                 value += "-";
             } else 
             if (lines.get(lp).contains("error")){
                 value += "-";
             }
             else
             {
             value += "-";
             }                   
         }  
         toFile += "<td title=\""+title+"\">"+value;
         toFile += "</td>";
         lp++;
     }
     
     public static void run(String input, String name) throws FileNotFoundException, IOException{
        String output = name;
        if (name.isEmpty()) {
            output = "output";    
        }
        
        FileOutputStream file = new FileOutputStream(output+".html");       
        StringBuilder cardName = new StringBuilder();
        List<String> lines = initalize(input, cardName);   
        begin(file, "Test results card: "+cardName.toString());
        quickLinks(file);
        details(lines, file);
        topFunction(lines, file);
        tableGenerator(lines, file);        
        toFile +="<p>© 2015 - JCAlgTest</p>\n</body>\n" +"</html>";
        file.write(toFile.getBytes());
        file.close();
        System.out.println("Make sure that CSS file is present in output folder.");
     }
     
     public static void runSortable(String dir) throws FileNotFoundException, IOException{
        FileOutputStream file = new FileOutputStream(dir+"//"+"sortable.html");
        begin(file, "Card performance - comparative table");
        
        sortableGenerator(dir, file);  
        toFile +="<p>© 2015 - JCAlgTest</p>\n</body>\n" +"</html>";
        file.write(toFile.getBytes());
        file.close();
        System.out.println("Make sure that CSS file & JS files is present in output folder.");
     } 
    
    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        FileOutputStream file = new FileOutputStream("output.html");       
        StringBuilder cardName = new StringBuilder();
        List<String> lines = initalize("AlgTest_3b_7a_94_00_00_80_65_a2_01_01_01_3d_72_d6_43_.csv", cardName);
        
        begin(file, "Test results card: "+cardName.toString());
        quickLinks(file);
        details(lines, file);
        topFunction(lines, file);
        tableGenerator(lines, file);
        
        toFile +="<p>© 2015 - JCAlgTest</p>\n</body>\n" +"</html>";
        file.write(toFile.getBytes());
        file.close();
    }
}

