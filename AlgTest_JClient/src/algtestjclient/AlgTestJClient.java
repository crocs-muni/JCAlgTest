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

import java.io.*;
import java.util.Scanner;

/**
 *
 * @author petr
 */
public class AlgTestJClient {
    static CardMngr cardManager = new CardMngr();
    static SingleModeTest singleTest = new SingleModeTest();
    static PerformanceTesting testingPerformance = new PerformanceTesting();
    static KeyHarvest keyHarvest = new KeyHarvest();
    
    
    /* Arguments for choosing which AlgTest version to run. */
    public static final String ALGTEST_MULTIPERAPDU = "AT_MULTIPERAPDU";        // for 'old' AlgTest
    public static final String ALGTEST_SINGLEPERAPDU = "AT_SINGLEPERAPDU";      // for 'New' AlgTest
    public static final String ALGTEST_PERFORMANCE = "AT_PERFORMANCE";          // for performance testing
    
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
    public final static String ALGTEST_JCLIENT_VERSION = ALGTEST_JCLIENT_VERSION_1_3_0;
    
    public final static int STAT_OK = 0;    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, Exception {
        /* If arguments are present. */
        if(args.length > 0){
            if (args[0].equals(ALGTEST_MULTIPERAPDU)){
                cardManager.testClassic(args, 0);}  // 0 means ask for every alg to test
                                                    // possibly change for constant?
                                                    // or maybe change for 1 and test all algs at once?
            else if (args[0].equals(ALGTEST_SINGLEPERAPDU)){singleTest.TestSingleAlg(args);}
            else if (args[0].equals(ALGTEST_PERFORMANCE)){testingPerformance.testPerformance(args, false);}
            /* In case of incorect parameter, program will report error and shut down. */
            else {
                System.err.println("Incorect parameter!");
                cardManager.PrintHelp();
            }
        }
        // If there are no arguments present
        else {   
            // Test available card - if more present, let user to select
            
            
            System.out.println("Choose which type of AlgTest you want to use.");
            System.out.println("NOTE that you need to have installed coresponding applet on your card! (Not true if you are using simulator.)");
            System.out.append("1 -> Classic AlgTest MultiPerApdu\n2 -> SinglePerApdu\n3 -> Performance Testing\n4 -> Performance Testing variable data length\n5 -> Harvest RSA keys\n");
            
            Scanner sc = new Scanner(System.in);
            int answ = sc.nextInt();
            switch (answ){
                /* In this case, classic version of AlgTest is used. */
                case 1:
                    /* BUGBUG: we need to figure out how to support JCardSim in nice way (copy of class files, directory structure...)
                    Class testClassClassic = AlgTest.class;
                    */
                    FileOutputStream file = cardManager.establishConnection(null);
                    System.out.println("\n\n#########################");
                    System.out.println("\n\nQ: Do you like to test all supported algorithms or be asked separately for every class? Separate questions help when testing all algorithms at once will provide incorrect answers due too many internal allocation of cryptographic objects (e.g., KeyBuilder class).");
                    System.out.println("Type \"y\" for test all algorithms, \"n\" for asking for every class separately: ");	
                    answ = sc.nextInt();
                    cardManager.testClassic(args, answ);
                    break;
                /* In this case, SinglePerApdu version of AlgTest is used. */
                case 2:
                    singleTest.TestSingleAlg(args);
                    break;
                /* In this case Performance tests are used. */
                case 3:
                    testingPerformance.testPerformance(args, false);
                    break;
                case 4:
                    testingPerformance.testPerformance(args, true);
                    break;
                case 5:
                    keyHarvest.gatherRSAKeys();
                    break;
                /* In this case, user pressed wrong key */
                default:
                    System.err.println("Incorrect parameter!");
                break;
            }
        
        }
    }
}
