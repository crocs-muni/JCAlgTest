/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * %W% %E%
 */

package com.sun.javacard.clientsamples.purseclient;

import java.rmi.*;
import javacard.framework.*;

import com.sun.javacard.clientlib.*;
import com.sun.javacard.rmiclientlib.*;

import com.sun.javacard.samples.RMIDemo.Purse;

import java.util.ResourceBundle;

public class PurseClient {
    
    private static final byte[] RMI_DEMO_AID = {
        (byte)0xa0, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x62, (byte)0x03,
        (byte)0x01, (byte)0xc, (byte)0x8,
        (byte)0x01
    };
    
    public static void main(String[] argv) throws RemoteException{
        
        ResourceBundle msg
        = ResourceBundle.getBundle("com/sun/javacard/clientsamples/purseclient/MessagesBundle");
        
        CardAccessor ca = null;
        
        try {
            
            // open and powerup the card
            ca = new ApduIOCardAccessor();
            
            // create a "filter" for RMI protocol
            JCRMIConnect jcRMI = new JCRMIConnect(ca);
            
            // select the Java Card applet
            if(argv.length == 0) {
                jcRMI.selectApplet( RMI_DEMO_AID, JCRMIConnect.REF_WITH_CLASS_NAME );
            }
            else {
                jcRMI.selectApplet( RMI_DEMO_AID, JCRMIConnect.REF_WITH_INTERFACE_NAMES );
            }
            
            // obtain the initial reference
            System.out.print(msg.getString("msg01")+" ");
            Purse myPurse = (Purse) jcRMI.getInitialReference();
            if(myPurse != null) {
                System.out.println(msg.getString("msg02"));
            }
            else {
                throw new Exception(msg.getString("msg03"));
            }
            
            // get the balance amount
            System.out.print(msg.getString("msg04"));
            short balance = myPurse.getBalance();
            System.out.println(msg.getString("msg05") + balance);  // prints 0
            
            System.out.println(msg.getString("msg06"));
            myPurse.credit((short)20);
            System.out.println(msg.getString("msg07"));
            myPurse.debit((short)15);
            
            System.out.print(msg.getString("msg08"));
            balance = myPurse.getBalance();
            System.out.println(msg.getString("msg05") + balance);  // prints 5
            
            System.out.println(msg.getString("msg09"));
            myPurse.setAccountNumber(new byte[]{5,4,3,2,1});  // expecting OK
            
            System.out.print(msg.getString("msg10"));
            byte[] acct_number = myPurse.getAccountNumber();
            printArray(acct_number);  // prints 5 4 3 2 1
            
            System.out.println(msg.getString("msg11"));
            myPurse.setAccountNumber(new byte[]{6,7,8,9,10,11});
            
        }
        catch(UserException e) {
            System.out.println(msg.getString("msg12") + e.toString());
            System.out.println(msg.getString("msg13") + Integer.toHexString(0x00FFFF & e.getReason()));
        }
        catch (Exception e){
            System.out.println(e);
        }
        finally {
            try{
                if(ca!=null){
                    ca.closeCard();
                }
            }
            catch (Exception ignore){
                // System.out.println(ignore);
            }
        }
    }
    
    private static void printArray(byte[] arr) {
        for(int i=0; i<arr.length; ++i) System.out.print(" " + arr[i]);
        System.out.println();
    }
    
}



