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
public class Sortable {
        public static void beginSortableHTML(FileOutputStream file, String title) throws IOException {
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
        
                toFile += "<script type=\"text/javascript\" src=\"./dist/jquery.tablesorter.js\"></script>\n"
                + "<script type=\"text/javascript\" src=\"./dist/custom-sorter.js\"></script>\n"; 
        
        toFile += " </head>\n\n";
        toFile += " <body style=\"margin-top:50px; padding:20px\">\n\n";

        toFile += " \t<nav class=\"navbar navbar-inverse navbar-fixed-top\">\n\t\t<div class=\"container-fluid\">\n\t\t<script type=\"text/javascript\" src=\"header.js\"></script>\n\t\t</div>\n\t</nav>\n\n";

        file.write(toFile.getBytes());
    }
        
    public static void endSortableHTML(FileOutputStream file) throws IOException {
        String toFile = "";
        toFile += "\t<script type=\"text/javascript\" src=\"footer.js\"></script>\n"+
                "<a href=\"#\" class=\"back-to-top\"></a>" +
                "\t<script>window.jQuery || document.write('<script src=\"../assets/js/vendor/jquery.min.js\"><\\/script>')</script>\n" +
                "\t<script src=\"./dist/js/bootstrap.min.js\"></script>\n" +
                "\t<script src=\"./assets/js/ie10-viewport-bug-workaround.js\"></script>\n";   
        
        toFile += " </body>\n";
        toFile += "</html>\n";
        file.write(toFile.getBytes());
    }
        
    
    public static void generateSortableTable(String tableID, List<String> topAcronyms, List<String> topNames, List<String> files, FileOutputStream file) throws IOException {
        Integer lp = 0;
        String result = "<table id=\"" + tableID + "\" class=\"tablesorter\" cellspacing='0'>\n";
        result += "\t<thead><tr>\n\t<th style=\"min-width:300px;\">CARD/FUNCTION (ms/op)</th>";

        for (String topAcronym : topAcronyms) {
            result += "<th>" + topAcronym + "</th>";
        }

        result += "</tr>\n</thead>\n<tbody>\n";
        file.write(result.getBytes());
        result = "";

        for (String filename : files) {
            StringBuilder cardName = new StringBuilder();
            List<String> lines = initalize(filename, cardName);
            if (cardName.toString().isEmpty()) {
                // If card name is not filled, use whole file name
                cardName.append(filename.substring(filename.lastIndexOf("/") + 1, filename.lastIndexOf(".")));
            }
            result += "<tr><td><strong>" + cardName + "</strong></td>";
            file.write(result.getBytes());
            result = "";
            for (String topName : topNames) {
                boolean bTopNameFound = false;
                while (lp < lines.size() - 4) {
                    if (lines.get(lp).contains(topName)) {
                        bTopNameFound = true;
                        lp = parseOneSortable(lines, file, lp);
                    } else {
                        lp++;
                    }
                }
                // In case given algorithm (topname) is not present in measured file, put -
                if (!bTopNameFound) {
                    result = "<td>-</td>";
                    file.write(result.getBytes());
                    result = "";
                }
                lp = 0;
            }
            result += "</tr>\n";
        }

        result += "</tbody>\n</table>\n";
        file.write(result.getBytes());
    }

    public static String generateLegendHeader(List<String> topNames, List<String> topAcronyms) throws IOException {
        String header = "";
        //header = "Used notation:<br>\n";
        header += "<ul style=\"list-style-type:circle; font-size:14px; line-height:120%;\">\n";
        for (int i = 0; i < topNames.size(); i++) {
            if (topNames.get(i).equals("SWALGS SWAlgs_AES()"))
                header += "\t<li><strong><a target=\"_blank\" href=\"http://www.fi.muni.cz/~xsvenda/jcalgs.html#aes\">" + topAcronyms.get(i) + "</a></strong> &#x1f517; = " + topNames.get(i) + "</li>\n";
            else
                header += "\t<li><strong>" + topAcronyms.get(i) + "</strong> = " + topNames.get(i) + "</li>\n";
        }
        header += "</ul>\n";
        return header;
    }

    public static void sortableGenerator(String dir, FileOutputStream file, Integer lp) throws IOException {
        List<String> topNames_sym = new ArrayList<>();
        List<String> topAcronyms_sym = new ArrayList<>();
        List<String> topNames_asym = new ArrayList<>();
        List<String> topAcronyms_asym = new ArrayList<>();
        loadTopFunctions(topNames_sym, topAcronyms_sym, topNames_asym, topAcronyms_asym);
        List<String> files = listFilesForFolder(new File(dir));
        lp = 0;
        file.write("<h3>Note: Sortable tables - click on column name to sort ascendingly/descendingly</h3>".getBytes());
        //
        // Sortable table for symmetric algorithms
        //
        file.write(generateLegendHeader(topNames_sym, topAcronyms_sym).getBytes());
        generateSortableTable("sortable_sym", topAcronyms_sym, topNames_sym, files, file);
        //
        // Sortable table for asymmetric algorithms
        file.write(generateLegendHeader(topNames_asym, topAcronyms_asym).getBytes());
        generateSortableTable("sortable_asym", topAcronyms_asym, topNames_asym, files, file);
    }
    
    
        public static Integer parseOneSortable(List<String> lines, FileOutputStream file, Integer lp) throws IOException {
        String[] operation;
        String result = "";

        String title = "";
        String value = "-";
        if (lines.get(lp + 1).equals("ALREADY_MEASURED")) {
            lp += 2;
            return lp;
        } else {
            lp += 2;
        }

        if ((lines.get(lp).contains("baseline")) && (lines.get(lp + 3).contains("avg op:"))) {
            lp += 3;
            operation = lines.get(lp).trim().split(";");

            value = (Float.valueOf(operation[2].replace(",", "."))).toString();
            title += "min: " + Float.valueOf(operation[4].replace(",", ".")) + "; max: ";
            title += Float.valueOf(operation[6].replace(",", "."));
            lp++;
        } else {
            if (lines.get(lp).contains("baseline") && !(lines.get(lp).contains("error"))) {
                lp += 2;
                value = "-";
            } else if (lines.get(lp).contains("error")) {
                value = "-";
            } else {
                value = "-";
            }
        }

        result += "<td title=\"" + title + "\">" + value;
        result += "</td>";
        lp++;
        file.write(result.getBytes());
        return lp;
    }
    
    
    public static void runSortable(String dir) throws FileNotFoundException, IOException {
        Integer linePosition = 0;
        FileOutputStream file = new FileOutputStream(dir + "//" + "sortable.html");
        beginSortableHTML(file, "JCAlgTest - Similarity table");
        sortableGenerator(dir, file, linePosition);
        endSortableHTML(file);
        System.out.println("Make sure that CSS & JS files are present in output folder.");
    }
}
