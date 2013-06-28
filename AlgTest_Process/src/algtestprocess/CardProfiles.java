/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author petr
 */
package algtestprocess;

import java.io.FileOutputStream;
import java.io.IOException;

public class CardProfiles {
    public static void generateScript(String capFileName, String packageAID, String appletAID, String cardIdentification, String modeHeader, String CardManager_AID, String open_sc) {
        try {
            FileOutputStream file = new FileOutputStream("installAlgTest_" + cardIdentification + ".txt");                   
            String script = getScript(capFileName, packageAID, appletAID, cardIdentification, modeHeader, CardManager_AID, open_sc);
            file.write(script.getBytes()); file.flush(); file.close();           
        }
        catch (IOException ex) {
            System.out.println("Exception : " + ex);
        }
    }
    
    public static String getScript(String capFileName, String packageAID, String appletAID, String cardIdentification, String modeHeader, String CardManager_AID, String open_sc) {
        String script;
        
        script = "# Usable for: " + cardIdentification + "\r\n"
                + modeHeader + "\r\n"
                + "enable_trace\r\n"
                + "establish_context\r\n"
                + "card_connect -readerNumber 0\r\n";
        
        if (CardManager_AID.isEmpty()) { script += "# preselected CM"; }
        else {script += "select -AID " + CardManager_AID;}
        
        script += "\r\n"
                + "open_sc -security 1 " + open_sc + "\r\n\r\n"
                
                + "delete -AID " + appletAID + "\r\n"
                + "delete -AID " + packageAID + "\r\n"
                
                + "install -file " + capFileName + " -nvDataLimit 2000 -instParam 00 \r\n\r\n" //-priv 21
                
                + "card_disconnect\r\n"
                + "release_context\r\n";   
        return script;
    }
}
