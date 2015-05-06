/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package algtestjclient;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;

/**
 * Copyright 2012-2014, SmartArchitects
 * @author Petr Svenda <petr@svenda.com>
 */
public class KeyHarvest {
    public static CardMngr cardManager = new CardMngr();
    public static FileOutputStream file;
    
    void gatherRSAKeys() throws CardException {
	    //
            // Obtain all readers with cards
            //
            TerminalFactory factory = TerminalFactory.getDefault();
            List<CardTerminal> readersList = factory.terminals().list();
            ArrayList<CardTerminal> readersWithCardList = new ArrayList();
            if (readersList.isEmpty()) { System.out.println("No terminals found"); }
            for (int i = 0; i < readersList.size(); i++) {
                CardTerminal terminal = (CardTerminal) readersList.get(i);
                if (terminal.isCardPresent()) {
                    // store readers with cards
                    readersWithCardList.add(readersList.get(i));
                    System.out.println(i + " : " + readersList.get(i) + " : card present");
                }
                else {
                    System.out.println(i + " : " + readersList.get(i) + " : card NOT present");
                }
            }
        
            System.out.println("TOTAL cards: " + readersWithCardList.size());
            
	    // Run separate thread for every reader / card		
            ExecutorService executor = Executors.newCachedThreadPool();
///*            
            for (int i = 0; i < readersWithCardList.size(); i++) {
                executor.execute(new CardRunner((CardTerminal) readersWithCardList.get(i), (byte) 0, 1000, i));
            }
/**/        
            executor.shutdown();
            // Wait until all threads are finish
            while (!executor.isTerminated()) {}
            System.out.println("\nFinished all threads");           
    }
    
}
