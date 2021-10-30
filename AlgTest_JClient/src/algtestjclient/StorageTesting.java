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

package algtestjclient;

import algtest.Consts;
import algtest.TestSettings;
import static algtestjclient.CardMngr.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;
import javacard.framework.ISO7816;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.ResponseAPDU;

public class StorageTesting {
    public static CardMngr m_cardManager = null;
    public FileOutputStream m_perfResultsFile = null;

    DirtyLogger m_SystemOutLogger = null;

    public StorageTesting(DirtyLogger logger) {
        m_SystemOutLogger = logger;
        m_cardManager = new CardMngr(m_SystemOutLogger);
    }
    
    public void TestStorage (String[] args, CardTerminal selectedReader, Args cmdArgs) throws IOException, Exception{
        Class testClassSingleApdu = null;
        Scanner br = new Scanner(System.in);  
        String answ = "";   // When set to 0, program will ask for each algorithm to test.
                
        m_SystemOutLogger.println("Specify type of your card (e.g., NXP JCOP CJ2A081):");
        String cardName = br.next();
        cardName += br.nextLine();
        m_SystemOutLogger.println(String.format("%s", cardName));
        if (cardName.isEmpty()) {
            cardName = "noname";
        }            
        FileOutputStream file = m_cardManager.establishConnection(cardName, cardName + "_STORAGE_", selectedReader, cmdArgs);
        StringBuilder value = new StringBuilder();
        long elapsedTimeWholeTest = -System.currentTimeMillis();
        //testKeysStorage(file, value);
        elapsedTimeWholeTest += System.currentTimeMillis();
        String message = "\n\nTotal test time:; " + elapsedTimeWholeTest / 1000 + " seconds."; 
        m_SystemOutLogger.println(message);
        file.write(message.getBytes());

        file.close();
    }
    
    public int testKeysStorage(byte INS, TestSettings testSet, String info, FileOutputStream pFile, StringBuilder pValue) {
        int         numObjects = -1;
        long        elapsedCard = 0;
    
        byte apdu[] = new byte[HEADER_LENGTH + TestSettings.TEST_SETTINGS_LENGTH];
        apdu[OFFSET_CLA] = Consts.CLA_CARD_ALGTEST;
        apdu[OFFSET_INS] = INS;
        apdu[OFFSET_P1] = testSet.P1;
        apdu[OFFSET_P2] = testSet.P2;
        apdu[OFFSET_LC] = (byte) (apdu.length - HEADER_LENGTH);
        CardMngr.serializeToApduBuff(testSet, apdu, ISO7816.OFFSET_CDATA);

        elapsedCard -= System.currentTimeMillis();
        try {
            ResponseAPDU resp = m_cardManager.sendAPDU(apdu);
            if (resp.getSW() != 0x9000) {
                m_SystemOutLogger.println(info + " Fail to obtain storage data");
            } else {
                // SET READ DATA
                byte data[] = resp.getData();

                // SAVE TIME OF CARD RESPONSE
                elapsedCard += System.currentTimeMillis();
                String elTimeStr;
                elTimeStr = String.valueOf((double) elapsedCard / (float) CLOCKS_PER_SEC);

                int keysNum = (short) ((data[0] << 8) + (data[1] & 0xff));
                //int keysUsableNum = (short) ((data[2] << 8) + (data[3] & 0xff));

                String message;
                message = String.format("\r\nOBJNUM: %1s;%d;%s", info, keysNum, elTimeStr); 
                m_SystemOutLogger.println(message);
                pFile.write(message.getBytes());
                
                numObjects = keysNum;
            }        
        }
        catch (Exception ex) {
            m_SystemOutLogger.println(info + "Fail to obtain storage data");
            pValue.append("error");
        }
    
        return numObjects;
    }
    
}


