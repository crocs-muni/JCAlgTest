/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package algtestjclient;

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
            String cardATR = cardMngr.ConnectToCard(m_cardTerminal, null, null);
            //cardMngr.TestCardIO(m_dataLength, m_numRepeats);
            String fileName = m_cardTerminal.getName() + "__" + cardATR + "__" + Long.toString(System.currentTimeMillis()) + ".csv";
            fileName = fileName.replace(' ', '_');
            cardMngr.GenerateAndGetKeys(fileName, m_numRepeats, -1, m_readerIndex);
            cardMngr.DisconnectFromCard();
         }
         catch (Exception ex) {
             System.out.println(ex.getMessage());
         }
     }
}
