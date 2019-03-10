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

import cardTools.SimulatedCardTerminal;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

public class KeyHarvest {
    public static CardMngr cardManager = null;
    public static FileOutputStream file;
    
    DirtyLogger m_SystemOutLogger = null;

    public KeyHarvest(DirtyLogger logger) {
        m_SystemOutLogger = logger;
        cardManager = new CardMngr(m_SystemOutLogger);
    }
    
    void gatherRSAKeys(boolean autoUploadBefore, short bitLength_start, short bitLength_step, short bitLength_end, boolean useCrt, int numOfKeys, boolean bUseSimulator) throws CardException {
            ArrayList<CardTerminal> readersWithCardList = new ArrayList();
            // Add simulated card if required
            if (bUseSimulator) {
                readersWithCardList.add(new SimulatedCardTerminal());
            }
            else {
                //
                // Obtain all readers with cards
                //
                List<CardTerminal> readersList = CardMngr.GetReaderList();
                if (readersList.isEmpty()) { m_SystemOutLogger.println("No terminals found"); }
                for (int i = 0; i < readersList.size(); i++) {
                    CardTerminal terminal = (CardTerminal) readersList.get(i);
                    if (terminal.isCardPresent()) {
                        // store readers with cards
                        readersWithCardList.add(readersList.get(i));
                        m_SystemOutLogger.println(i + " : " + readersList.get(i) + " : card present");
                    }
                    else {
                        m_SystemOutLogger.println(i + " : " + readersList.get(i) + " : card NOT present");
                    }
                }
            }
            
            m_SystemOutLogger.println("TOTAL cards: " + readersWithCardList.size());
            
	    // Run separate thread for every reader / card		
            ExecutorService executor = Executors.newCachedThreadPool();
  
            for (int i = 0; i < readersWithCardList.size(); i++) {
                executor.execute(new CardRunner((CardTerminal) readersWithCardList.get(i), (byte) 0, numOfKeys, autoUploadBefore, bitLength_start, bitLength_step, bitLength_end, useCrt, m_SystemOutLogger));
            }
        
            executor.shutdown();
            // Wait until all threads are finish
            while (!executor.isTerminated()) {}
            m_SystemOutLogger.println("\nFinished all threads");           
    }
    
}
