/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @(#)SamplePasswdBioApplet.java	1.4 06/01/03
 */

package com.sun.javacard.samples.biometry.bioApplet;

/**
 * package AID: 0xA0:0x00:0x00:0x00:0x62:0x03:0x01:0x0C:0x0f:0x02
 * applet AID: 0xA0:0x00:0x00:0x00:0x62:0x03:0x01:0x0C:0x0f:0x02:0x01
 **/
import javacard.framework.*;
import javacardx.biometry.*;

public class SamplePasswdBioApplet extends Applet{
    public final static byte CLA=(byte)0xCF;
    public final static byte INS_GET_REQ = (byte)0x10;
    public final static byte INS_MATCH = (byte)0x11;
    
    //--Error codes
    public static final short ERROR_MATCH_FAILED = (short)0x9101;    
    
    private SharedBioTemplate bioImpl;
    private static final byte[] BIO_SERVER_AID = {(byte)0xA0, (byte)0x00, 
        (byte)0x00, (byte)0x00, (byte)0x62, (byte)0x03, (byte)0x01, (byte)0x0C, 
        (byte)0x0f, (byte)0x01, (byte)0x01};
    /**
     * Only this class's install method should create the applet object.
     */
    protected SamplePasswdBioApplet(byte[] bArray, short bOffset, byte bLength){   
        byte aidLen = bArray[bOffset];
        if (aidLen== (byte)0){
            register();
        } else {
            register(bArray, (short)(bOffset+1), aidLen);
        }
        AID bioServerAID = JCSystem.lookupAID(BIO_SERVER_AID,(short)0,(byte)BIO_SERVER_AID.length);
        bioImpl = (SharedBioTemplate)JCSystem.getAppletShareableInterfaceObject(bioServerAID,(byte)0);
    }
    
     /**
     * Installs this applet.
     * @param bArray the array containing installation parameters
     * @param bOffset the starting offset in bArray
     * @param bLength the length in bytes of the parameter data in bArray
     */
    public static void install(byte[] bArray, short bOffset, byte bLength)
    {
        new SamplePasswdBioApplet(bArray,bOffset,bLength);
    }
     /**
     * Processes an incoming APDU.
     * @see APDU
     * @param apdu the incoming APDU
     * @exception ISOException with the response bytes per ISO 7816-4
     */
    public void process(APDU apdu){
         byte buffer[] = apdu.getBuffer();
         // check SELECT APDU command
         if(selectingApplet())
             return;
         switch (buffer[ISO7816.OFFSET_INS]){
             case INS_GET_REQ:
             getRequirements(apdu);
             break;
             case INS_MATCH:
             match(apdu);
             break;
             default:
            ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }
    
    /**
    * method returns the public template data.
    **/
    public void getRequirements(APDU apdu){
        byte []buffer = apdu.getBuffer();
        //byte []pubTemp = JCSystem.makeTransientByteArray((short)4,JCSystem.CLEAR_ON_DESELECT);
        
        short length = bioImpl.getPublicTemplateData((short)0, buffer, (short)0, (short)buffer.length);
        apdu.setOutgoing();
        apdu.setOutgoingLength(length);
        apdu.sendBytes((short)0,length);
    }
    
    /**
    * following match method matches the password passed in with the reference
    * template
    **/
    public void match(APDU apdu){
        byte []buffer = apdu.getBuffer();
        short bytesRead = apdu.setIncomingAndReceive();
        short result = bioImpl.initMatch(buffer, ISO7816.OFFSET_CDATA, bytesRead);
        if(result < BioTemplate.MINIMUM_SUCCESSFUL_MATCH_SCORE){
            ISOException.throwIt(ERROR_MATCH_FAILED);
        }
    }
}
    
        
