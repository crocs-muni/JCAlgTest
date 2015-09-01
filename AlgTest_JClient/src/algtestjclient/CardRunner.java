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
    short m_bitLength;
    boolean m_uploadBeforeStart;
    boolean m_useCrt;
    
    public CardRunner(CardTerminal cardTerminal, byte dataLength, int numRepeats, boolean uploadBeforeStart, short bitLength, boolean useCrt) {
        m_cardTerminal = cardTerminal;
        m_dataLength = dataLength;
        m_numRepeats = numRepeats;
        m_bitLength = bitLength;
        m_uploadBeforeStart = uploadBeforeStart;
        m_useCrt = useCrt;
    }
    
     @Override
     public void run() {
         CardMngr cardMngr = new CardMngr();
         // cardMngr.m_verbose = false;
         try {
             FileOutputStream file = cardMngr.establishConnection(null, "", m_cardTerminal.getName(), m_cardTerminal);
             if(file != null) {
                cardMngr.GenerateAndGetKeys(file, m_numRepeats, -1, m_uploadBeforeStart, m_bitLength, m_useCrt);
                cardMngr.DisconnectFromCard();
             }
         }
         catch (Exception ex) {
             System.out.println("Card in terminal "+m_cardTerminal.getName()+" stoped with exception: "+ex.getMessage());
         }
     }
}
