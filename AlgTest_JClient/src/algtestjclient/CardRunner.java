/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

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
    int m_readerIndex;
    
    public CardRunner(CardTerminal cardTerminal, byte dataLength, int numRepeats, int readerIndex) {
        m_cardTerminal = cardTerminal;
        m_dataLength = dataLength;
        m_numRepeats = numRepeats;
        m_readerIndex = readerIndex;
    }
    
     @Override
     public void run() {
         CardMngr cardMngr = new CardMngr();
         // cardMngr.m_verbose = false;
         try {
             FileOutputStream file = cardMngr.establishConnection(null, "", m_cardTerminal.getName(), m_cardTerminal);
             if(file != null) {
                cardMngr.GenerateAndGetKeys(file, m_numRepeats, -1);
                cardMngr.DisconnectFromCard();
             }
         }
         catch (Exception ex) {
             System.out.println(ex.getMessage());
         }
     }
}
