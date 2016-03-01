/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @(#)SecurePurseClient.java	1.21 06/01/03
 *
 */

package com.sun.javacard.clientsamples.securepurseclient;

import java.rmi.*;
import javacard.framework.*;

import com.sun.javacard.clientlib.*;
import com.sun.javacard.rmiclientlib.*;

import com.sun.javacard.samples.SecureRMIDemo.Purse;

import java.util.ResourceBundle;


public class SecurePurseClient {
    
    private static final byte[] SECURE_RMI_DEMO_AID = { 
	(byte)0xa0, (byte)0x00, (byte)0x00, (byte)0x00, 
	(byte)0x62, (byte)0x03, (byte)0x01, (byte)0xc, 
	(byte)0xa, (byte)0x01 
    };
    
    private static final short PRINCIPAL_APP_PROVIDER_ID = 0x1234;
    private static final short PRINCIPAL_CARDHOLDER_ID = 0x4321;
    
    
    public static void main(java.lang.String[] argv) {
        
        ResourceBundle msg
	    = ResourceBundle.getBundle("com/sun/javacard/clientsamples/securepurseclient/MessagesBundle");
        
        CustomCardAccessor cca = null;
        
        try {
            
            // open and powerup the card - using CustomCardAccessor
            cca = new CustomCardAccessor(new ApduIOCardAccessor());
            
            // create a "filter" for RMI protocol
            JCRMIConnect jcRMI = new JCRMIConnect(cca);

            // select the Java Card applet
            if(argv.length == 0) {
                jcRMI.selectApplet( SECURE_RMI_DEMO_AID, JCRMIConnect.REF_WITH_CLASS_NAME );
            }
            else {
                jcRMI.selectApplet( SECURE_RMI_DEMO_AID, JCRMIConnect.REF_WITH_INTERFACE_NAMES );
            }
            
            // give your PIN
            System.out.print(msg.getString("msg03"));
            if (! cca.authenticateUser( PRINCIPAL_APP_PROVIDER_ID )){
                throw new RemoteException(msg.getString("msg04"));
            }
            System.out.println(msg.getString("msg05"));
            
            System.out.print(msg.getString("msg06"));
            Purse myPurse = (Purse) jcRMI.getInitialReference();
            if(myPurse != null) {
                System.out.println(msg.getString("msg07"));
            }
            else {
                throw new Exception(msg.getString("msg08"));
            }
            
            System.out.print(msg.getString("msg09"));
            short balance = myPurse.getBalance();
            System.out.println(msg.getString("msg10") + balance);
            
            System.out.println(msg.getString("msg11"));
            myPurse.credit((short)20);
            
            System.out.print(msg.getString("msg12"));
            balance = myPurse.getBalance();
            System.out.println(msg.getString("msg10") + balance);
            
            System.out.println(msg.getString("msg13"));
            myPurse.debit((short)15);
            
            
        }
        catch(UserException e) {
            System.out.println(msg.getString("msg14") + e);
            System.out.println(msg.getString("msg15") + 
			       Integer.toHexString(0x00FFFF & e.getReason()));
        }
        catch (Exception e){
            System.out.println(e);
        } 
	finally {
            try{
                if(cca!=null){
                    cca.closeCard();
                }
            }
	    catch (Exception ignore){
                //System.out.println(ignore);
            }
        }
    }
    
}


