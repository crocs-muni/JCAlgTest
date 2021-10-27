/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package algtestjclient;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
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
             
/* del 20170506             
            if (m_bitLength == -1) {
                // test all possible values between 
                int RSA_LEN_STEP = 32;
                ArrayList<Pair<Integer, Boolean>> supported = new ArrayList<>();
//                for (short len = 1152; len <= 2016; len += RSA_LEN_STEP) {
                for (short len = 2048; len <= 4096; len += RSA_LEN_STEP) {
                   FileOutputStream file = cardMngr.establishConnection(null, "", String.format("RSA%db_%s", len, m_cardTerminal.getName()), m_cardTerminal);
                   cardMngr.GenerateAndGetKeys(file, m_numRepeats, -1, m_uploadBeforeStart, len, m_useCrt);
                   cardMngr.DisconnectFromCard();
                   
                   if (cardMngr.PrepareRSAEngine(file, len, m_useCrt)) {
                       supported.add(new Pair(len, true));
                       m_SystemOutLogger.println("OK");
                   }
                   else {
                       supported.add(new Pair(len, false));
                   }
        
                }
                for (Pair<Integer, Boolean> suppLen : supported) {
                   m_SystemOutLogger.println(String.format("RSA length %db:\t%s", suppLen.getL(), suppLen.getR() ? "OK" : "unsupported"));
                }
            }
            else {
               FileOutputStream file = cardMngr.establishConnection(null, "", String.format("RSA%db_%s", m_bitLength, m_cardTerminal.getName()), m_cardTerminal);
               cardMngr.GenerateAndGetKeys(file, m_numRepeats, -1, m_uploadBeforeStart, m_bitLength, m_useCrt);
               cardMngr.DisconnectFromCard();
            }
*/        
         }
         catch (Exception ex) {
             m_SystemOutLogger.println("Card in terminal "+m_cardTerminal.getName()+" stoped with exception: "+ex.getMessage());
         }
    }
}
