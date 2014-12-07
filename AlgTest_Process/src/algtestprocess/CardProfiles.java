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
                + "card_connect -readerNumber 1\r\n";
        
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
