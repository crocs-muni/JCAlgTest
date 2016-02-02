/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @(#)PhotoCardClient.java	1.11 06/01/06
 */

package com.sun.javacard.clientsamples.photocardclient;

// J2SE libraries
import java.rmi.*;
import javacard.framework.*;
import java.io.*;
import java.text.*;
import java.util.ResourceBundle;
import java.security.MessageDigest;

import com.sun.javacard.clientlib.*;
import com.sun.javacard.rmiclientlib.*;

// Applet JC RMI interface
import com.sun.javacard.samples.photocard.PhotoCard;


public class PhotoCardClient {
    
   private static ResourceBundle msg;
 
   
    private static final byte[] PHOTO_CARD_AID =  {
        (byte)0xA0, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x62, (byte)0x03, (byte)0x01, (byte)0x0C,
        (byte)0x07, (byte)0x01
    };
   
  
    public static void putPhotoInCard(PhotoCard theCard, byte[] data) throws Exception {
    
        short picID = theCard.requestPhotoStorage((short)data.length);
        System.out.println(MessageFormat.format(msg.getString("msg10"), 
						new Object[] { new Integer(picID) })); 
	
        for (int i = 0; i < data.length; i += PhotoCard.MAX_BUFFER_BYTES) {
            byte[] buffer = new byte[96];
            if ((data.length - i) >= PhotoCard.MAX_BUFFER_BYTES) {
                System.arraycopy(data, i, buffer, 0, PhotoCard.MAX_BUFFER_BYTES);
                theCard.loadPhoto(picID, buffer, (short)PhotoCard.MAX_BUFFER_BYTES, (short)i, true);
            } else {
                System.arraycopy(data, i, buffer, 0, (short)(data.length - i));
                theCard.loadPhoto(picID, buffer, (short)(data.length - i), (short)i, false);
            }
        }
        
    }
    
    public static byte[] recoverPhoto(PhotoCard theCard, short picID) throws Exception {
	
        short picSize = theCard.getPhotoSize(picID);
        System.out.println(MessageFormat.format(msg.getString("msg09"), 
						new Object[] { new Integer(picSize) }));
        byte[] data = new byte[picSize];
        
        for (int i = 0; i < picSize; i += PhotoCard.MAX_BUFFER_BYTES) {
            byte[] buffer = null;
            if ((picSize - i) >= PhotoCard.MAX_BUFFER_BYTES) {
                buffer = theCard.getPhoto(picID, (short)i, (short)PhotoCard.MAX_BUFFER_BYTES);
                System.arraycopy(buffer, 0, data, i, PhotoCard.MAX_BUFFER_BYTES);
            } else {
                buffer = theCard.getPhoto(picID, (short)i, (short)(picSize - i));
                System.arraycopy(buffer, 0, data, i, (short)(picSize - i));
            }
        }
        return data;
        
    }
    
   public static void verifyPhoto(PhotoCard theCard,short photoID, byte[] data)
                throws Exception{
       MessageDigest md = null;
       try{
            md = MessageDigest.getInstance("SHA-256");
       }catch(Exception e){
           System.out.println(msg.getString("msg14"));
           return;
       }
       byte[] photoDigest = md.digest(data);
       //send digest for verification
       short response = theCard.verifyPhoto(photoID, photoDigest, (short)0);
		switch(response){
			case 0: System.out.println(msg.getString("msg21"));
					 break;
			case (short)0x7110: System.out.println(msg.getString("msg16"));
					 	break;
			case (short)0x7222: System.out.println(msg.getString("msg17"));
					 	break;
			case (short)0x7333: System.out.println(msg.getString("msg18"));
					 	break; 
		    case (short)0x7444: System.out.println(msg.getString("msg19"));
					 	break;  
			default: System.out.println(msg.getString("msg20"));
			   }
   }
     
    
    public static void main(String[] argv) throws RemoteException{
        msg = 
	    ResourceBundle.getBundle("com/sun/javacard/clientsamples/photocardclient/MessagesBundle");

        CardAccessor ca = null;
        
		try {
	    
            if ((argv.length < 1) || (argv.length >4)) {
                System.out.println(msg.getString("msg00"));
            }
            
            System.out.println(msg.getString("msg01"));
           
            // open and powerup the card
            ca = new ApduIOCardAccessor();

            // create a "filter" for RMI protocol
            JCRMIConnect jcRMI = new JCRMIConnect(ca);
           
            // select the Java Card applet
            if(argv.length == 0) {
                jcRMI.selectApplet( PHOTO_CARD_AID, JCRMIConnect.REF_WITH_CLASS_NAME );
            }
            else {
                jcRMI.selectApplet( PHOTO_CARD_AID, JCRMIConnect.REF_WITH_INTERFACE_NAMES );
            }
		
           
            // obtain the initial reference
            PhotoCard myPhotoCard = (PhotoCard) jcRMI.getInitialReference();
            
            // Failed to initialize
            if(myPhotoCard == null) {
                throw new Exception(msg.getString("msg02"));
            }
            
            for (int i = 0; i < argv.length; i++) {
                // Read the file first
                String file1 = argv[i];
                byte[] file1Bytes = null;
                //System.out.println("Reading file: " + file1 + "...");
                System.out.println(MessageFormat.format(msg.getString("msg03"), 
                    new Object[] { file1 }));
                
                FileInputStream fis = new FileInputStream(new File(file1));
                int fLen = fis.available();
                System.out.println(MessageFormat.format(msg.getString("msg04"), 
                    new Object[] { new Integer(fLen) }));
                
                file1Bytes = new byte[fLen];
                int fRead = fis.read(file1Bytes);
                fis.close();
                
                System.out.println(msg.getString("msg05"));
                putPhotoInCard(myPhotoCard, file1Bytes);
            }
            
            System.out.println(msg.getString("msg06"));
            

            for (int i = 0; i < argv.length; i++) {
                
                System.out.println(MessageFormat.format(msg.getString("msg07"), 
                    new Object[] { new Integer(i + 1) }));
                byte[] recv1Bytes = recoverPhoto(myPhotoCard, (short)(i + 1));
        
                String file1 = argv[i];
                System.out.println(MessageFormat.format(msg.getString("msg08"), 
							new Object[] { new Integer(recv1Bytes.length), 
								       file1  }));
                // Write recovered content into files
                System.out.println(MessageFormat.format(msg.getString("msg12"), 
                    new Object[] { file1 }));
                FileOutputStream fos = new FileOutputStream(new File(file1 + ".recv"));
                fos.write(recv1Bytes);
                fos.flush();
                fos.close();
            }
            	  System.out.println("");
                System.out.println(msg.getString("msg13"));
				  System.out.println("");
                //read photo data from card
                String file1 = argv[0];
                byte[] file1Bytes = null;
                System.out.println(MessageFormat.format(msg.getString("msg03"), 
                    new Object[] { file1 }));                
                FileInputStream fis = new FileInputStream(new File(file1));
                int fLen = fis.available();
                file1Bytes = new byte[fLen];
                int fRead = fis.read(file1Bytes);
                fis.close();
                //send photo to card for comparison
                System.out.println(msg.getString("msg15"));
                verifyPhoto(myPhotoCard,(short) 1, file1Bytes);
                
                                    
        }
        catch(UserException e) {
			if(e.getReason()==(short)0x7110)				
					System.out.println(msg.getString("msg14"));
        }
        catch (Exception e){
            e.printStackTrace();
        }
		 finally {
            try{
                if(ca!=null){
                    ca.closeCard();
                }
				}catch (Exception ignore){
                // System.out.println(ignore);
            }
        }
    }
        
}



