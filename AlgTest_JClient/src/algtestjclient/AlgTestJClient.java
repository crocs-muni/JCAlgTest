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

import AlgTest.JCConsts;
import java.io.*;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

/**
 *
 * @author petr
 */
public class AlgTestJClient {
    //static CardMngr cardManager = new CardMngr();
    //static SingleModeTest singleTest = new SingleModeTest();
    //static PerformanceTesting testingPerformance = new PerformanceTesting();
    //static KeyHarvest keyHarvest = new KeyHarvest();
    
    
    /* Arguments for choosing which AlgTest version to run. */
    public static final String ALGTEST_MULTIPERAPDU = "AT_MULTIPERAPDU";        // for 'old' AlgTest
    public static final String ALGTEST_SINGLEPERAPDU = "AT_SINGLEPERAPDU";      // for 'New' AlgTest
    public static final String ALGTEST_PERFORMANCE = "AT_PERFORMANCE";          // for performance testing
    
    /**
     * Version 1.6.0 (19.07.5)
     * + Many updates, performance tests
     */
    public final static String ALGTEST_JCLIENT_VERSION_1_6_0 = "1.6.0";        
        
    /**
     * Version 1.3 (30.11.2014)
     * + Improved gathering of data, single command per single algorithm instance possible
     */
    public final static String ALGTEST_JCLIENT_VERSION_1_3_0 = "1.3.0";    
    /**
     * Version 1.2.1 (29.1.2014)
     * + added support for TYPE_RSA_PRIVATE_TRANSIENT_RESET and TYPE_RSA_PRIVATE_TRANSIENT_DESELECT parsing
     * + added possibility to run test for every class separately
     * - more information for user, small refactoring
     */
    public final static String ALGTEST_JCLIENT_VERSION_1_2_1 = "1.2.1";
    /**
     * Version 1.2 (3.11.2013)
     * + All relevant constants from JC2.2.2, JC3.0.1 and JC3.0.4 added
     */
    public final static String ALGTEST_JCLIENT_VERSION_1_2 = "1.2";
    /**
     * Version 1.1 (28.6.2013)
     * + information about version added
     * + link to project added into resulting file 
     */
    public final static String ALGTEST_JCLIENT_VERSION_1_1 = "1.1";
    /**
     * Version 1.0 (27.11.2012)
     * + initial version of AlgTestJClient, clone of AlgTestCppClient
     */
    public final static String ALGTEST_JCLIENT_VERSION_1_0 = "1.0";
 
    /**
     * Current version
     */
    public final static String ALGTEST_JCLIENT_VERSION = ALGTEST_JCLIENT_VERSION_1_6_0;
    
    public final static int STAT_OK = 0;    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, Exception {
        // If arguments are present. 
        if(args.length > 0){
            if (args[0].equals(ALGTEST_MULTIPERAPDU)){
                CardMngr cardManager = new CardMngr();
                cardManager.testClassic(args, 0, null);
            }  // 0 means ask for every alg to test
                                                    // possibly change for constant?
                                                    // or maybe change for 1 and test all algs at once?
            else if (args[0].equals(ALGTEST_SINGLEPERAPDU)){
                SingleModeTest singleTest = new SingleModeTest();
                singleTest.TestSingleAlg(args, null);
            }
            else if (args[0].equals(ALGTEST_PERFORMANCE)){
                PerformanceTesting testingPerformance = new PerformanceTesting();
                testingPerformance.testPerformance(args, false, null);
            }
            // In case of incorect parameter, program will report error and shut down.
            else {
                System.err.println("Incorect parameter!");
                CardMngr.PrintHelp();
            }
        }
        // If there are no arguments present
        else {   
            CardTerminal selectedTerminal = null;
            PerformanceTesting testingPerformance = new PerformanceTesting();
            
            System.out.println("Choose which type of AlgTest you want to use.");
            System.out.println("NOTE that you need to have installed coresponding applet on your card! (Not true if you are using simulator.)");
            System.out.append("1 -> List of supported algorithms (2-10 minutes, algorithm detected if object allocation succeds)\n" + 
                              "2 -> Performance Testing (1-3 hours, every method (e.g., doFinal) of given class (e.g., Cipher) of given algorithm is tested for performance (usually over 256 bytes if applicable)\n" + 
                              "3 -> Performance Testing variable data length (2-10 hours, same as option 2, but lengths {16, 32, 64, 128, 256 and 512} bytes are tested)\n" + 
                              "4 -> Harvest RSA keys (unlimited, on-card generated keypairs are exported and stored in file)\n");
            
            Scanner sc = new Scanner(System.in);
            int answ = sc.nextInt();
            switch (answ){
/*  not supported anymore              
                // In this case, classic version of AlgTest is used
                case 1:
                    System.out.println("\n\n#########################");
                    System.out.println("\n\nQ: Do you like to test all supported algorithms or be asked separately for every class? Separate questions help when testing all algorithms at once will provide incorrect answers due too many internal allocation of cryptographic objects (e.g., KeyBuilder class).");
                    System.out.println("Type \"y\" for test all algorithms, \"n\" for asking for every class separately: ");	
                    answ = sc.nextInt();
                    CardMngr cardManager = new CardMngr();
                    cardManager.testClassic(args, answ);
                    break;
*/                
                // In this case, SinglePerApdu version of AlgTest is used.
                case 1:
                    selectedTerminal = selectTargetReader();
                    SingleModeTest singleTest = new SingleModeTest();
                    singleTest.TestSingleAlg(args, selectedTerminal);
                    break;
                // In this case Performance tests are used. 
                case 2:
                    selectedTerminal = selectTargetReader();
                    testingPerformance.testPerformance(args, false, selectedTerminal);
                    break;
                case 3:
                    selectedTerminal = selectTargetReader();
                    testingPerformance.testPerformance(args, true, selectedTerminal);
                    break;
                case 4:
                    KeyHarvest keyHarvest = new KeyHarvest();    
                    // Remove new line character from stream after load integer as type of test
                    sc.nextLine();       
                    System.out.print("Upload applet before harvest (y/n): ");
                    String autoUploadBeforeString = sc.nextLine();
                    boolean autoUploadBefore = false;
                    if (autoUploadBeforeString.toLowerCase().equals("y")) autoUploadBefore = true;
                    else if (!autoUploadBeforeString.toLowerCase().equals("n")) {
                        System.out.println("Wrong answer. Auto upload applet before harvest is disabled.");
                    }
                    
                    System.out.print("Bit length of key to generate (512, 1024 or 2048): ");
                    String bitLengthString = sc.nextLine();
                    int acceptedInputs[] = {512, 1024};
                    short bitLength = JCConsts.KeyBuilder_LENGTH_RSA_512;
                    try {
                        int input = Integer.parseInt(bitLengthString);
                        boolean isAcceptedInput = false;
                        for (int acceptedInput : acceptedInputs) {
                            if (input == acceptedInput) {
                                isAcceptedInput = true;
                                bitLength = (short)acceptedInput;
                                break;
                            }
                        }
                        if (!isAcceptedInput) {
                            throw new NumberFormatException();
                        }
                    }
                    catch(NumberFormatException ex) {
                        System.out.println("Wrong number. Bit length is set to "+bitLength+".");
                    }
                    
                    System.out.print("Use RSA harvest with CRT (y/n): ");
                    String useCrtString = sc.nextLine();
                    boolean useCrt = false;
                    if (useCrtString.toLowerCase().equals("y")) useCrt = true;
                    else if (!useCrtString.toLowerCase().equals("n")) {
                        System.out.println("Wrong answer. CRT is disabled.");
                    }
                    
                    // Check if folder !card_uploaders is correctly set
                    File fileCardUploadersFolder = new File(CardMngr.cardUploadersFolder);
                    if (!fileCardUploadersFolder.exists()) {
                        System.out.println("Cannot find folder with card uploaders. Default folder: " + CardMngr.cardUploadersFolder);
                        System.out.print("Card uploaders folder path: ");
                        String newPath = sc.nextLine();
                        fileCardUploadersFolder = new File(CardMngr.cardUploadersFolder);
                        // If new path is also incorrect
                        if (!fileCardUploadersFolder.exists()) {
                            System.err.println("Folder " + newPath + " does not exist. Cannot start gathering RSA keys.");
                            return;
                        }
                        // Set new path to !card_uploaders folder
                        CardMngr.cardUploadersFolder = newPath;
                    } 
                    
                    System.out.print("Number of keys to generate: ");
                    String numOfKeysString = sc.nextLine();
                    int numOfKeys = 10;
                    try {
                        numOfKeys = Integer.parseInt(numOfKeysString);
                    }
                    catch(NumberFormatException ex) {
                        System.out.println("Wrong number. Number of keys to generate is set to "+numOfKeys+".");
                    }
                  
                    
                    keyHarvest.gatherRSAKeys(autoUploadBefore, bitLength, useCrt, numOfKeys);
                    break;
                // In this case, user pressed wrong key 
                default:
                    System.err.println("Incorrect parameter!");
                break;
            }
        
        }
    }
    
    static CardTerminal selectTargetReader() {
        // Test available card - if more present, let user to select one
        List<CardTerminal> terminalList = CardMngr.GetReaderList(true);
        CardTerminal selectedTerminal = null;
        if (terminalList.isEmpty()) {
            System.out.println("ERROR: No reader detected. Please check your reader connection");
            return null;
        }
        else {
            if (terminalList.size() == 1) {
                selectedTerminal = terminalList.get(0); // return first and only reader
            }
            else {
                int terminalIndex = 0;
                // Let user select target terminal
                for (CardTerminal terminal : terminalList) {
                    Card card;
                    try {
                        card = terminal.connect("*");
                        ATR atr = card.getATR();
                        System.out.println(terminalIndex + " : " + terminal.getName() + " - " + CardMngr.bytesToHex(atr.getBytes()));    
                        terminalIndex++;
                    } catch (CardException ex) {
                        Logger.getLogger(AlgTestJClient.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }   
                System.out.println("Select index of target reader you like to use 0.." + (terminalIndex - 1));
                Scanner sc = new Scanner(System.in);
                int answ = sc.nextInt();
                selectedTerminal = terminalList.get(answ); 
            }
        }
        
        return selectedTerminal;
    }
  
}
