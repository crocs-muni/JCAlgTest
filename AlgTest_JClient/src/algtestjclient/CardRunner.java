/*  
    Copyright (c) 2008-2024 Petr Svenda <petr@svenda.com>

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

import java.io.FileOutputStream;
import javax.smartcardio.CardTerminal;

/**
 *
 * @author petrs
 */
public class CardRunner implements Runnable {
    CardTerminal m_cardTerminal;
    byte m_dataLength;
    int m_numRepeats;
    short m_bitLength_start;
    short m_bitLength_step;
    short m_bitLength_end;
    boolean m_uploadBeforeStart;
    boolean m_useCrt;
    
    DirtyLogger m_SystemOutLogger = null;
    
    public CardRunner(CardTerminal cardTerminal, byte dataLength, int numRepeats, boolean uploadBeforeStart, short bitLength_start, short bitLength_step, short bitLength_end, boolean useCrt, DirtyLogger logger) {
        m_cardTerminal = cardTerminal;
        m_dataLength = dataLength;
        m_numRepeats = numRepeats;
        m_bitLength_start = bitLength_start;
        m_bitLength_step = bitLength_step;
        m_bitLength_end = bitLength_end;
        m_uploadBeforeStart = uploadBeforeStart;
        m_useCrt = useCrt;
        m_SystemOutLogger = logger;
    }
    
     @Override
     public void run() {
         CardMngr cardMngr = new CardMngr(m_SystemOutLogger);
         // cardMngr.m_verbose = false;
         try {
             for (short len = m_bitLength_start; len <= m_bitLength_end; len += m_bitLength_step) {
                 FileOutputStream file = cardMngr.establishConnection("", String.format("RSA%db_%s", len, m_cardTerminal.getName()), m_cardTerminal, new Args());
                 cardMngr.GenerateAndGetKeys(file, m_numRepeats, -1, m_uploadBeforeStart, len, m_useCrt);
                 cardMngr.DisconnectFromCard();
             }
         }
         catch (Exception ex) {
             m_SystemOutLogger.println("Card in terminal "+m_cardTerminal.getName()+" stoped with exception: "+ex.getMessage());
         }
    }
}
