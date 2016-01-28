/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @(#)SampleBioServer.java	1.4 06/01/03
 */

package com.sun.javacard.samples.biometry.bioServer;

import javacard.framework.*;
import javacardx.biometry.*;

/**
* package AID: A0:00:00:00:62:03:01:0C:0f:01
* applet AID: A0:00:00:00:62:03:01:0C:0f:01:01
**/
public class SampleBioServer extends Applet implements SharedBioTemplate{
    public final static byte CLA=(byte)0xCF;
    public final static byte INS_ENROLL = (byte)0x10;
    public final static byte MATCH_TRY_LIMIT = (byte)5;
    private OwnerBioTemplate impl;
    /**
     * Only this class's install method should create the applet object.
     */
    protected SampleBioServer(byte[] bArray, short bOffset, byte bLength){   
        byte aidLen = bArray[bOffset];
        if (aidLen== (byte)0){
            register();
        } else {
            register(bArray, (short)(bOffset+1), aidLen);
        }
        impl = BioBuilder.buildBioTemplate(BioBuilder.PASSWORD,MATCH_TRY_LIMIT);
    }
    
    /**
     * Installs this applet.
     * @param bArray the array containing installation parameters
     * @param bOffset the starting offset in bArray
     * @param bLength the length in bytes of the parameter data in bArray
     */
    public static void install(byte[] bArray, short bOffset, byte bLength)
    {
        new SampleBioServer(bArray,bOffset,bLength);
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
             case INS_ENROLL:
             enrollData(apdu);
             break;
             default:
            ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
         }
    }
    /**
    * method takes in the data
    * This implementation assumes that all the password data is given in the 
    * enroll method.
    **/
    public void enrollData(APDU apdu){
        byte []buffer = apdu.getBuffer();
        short bytesRead = apdu.setIncomingAndReceive();           
        impl.init(buffer, ISO7816.OFFSET_CDATA, bytesRead);
        impl.doFinal();
        //enrollment complete
    }
    
    public Shareable getShareableInterfaceObject(AID clientAID, byte parameter){
        return this;        
    }
    
    //--- the nethods below implement the SharedBioTemplate interface
    public boolean isInitialized(){
        return impl.isInitialized();
    }
    public boolean isValidated(){
        return impl.isValidated();
    }
    public void reset(){
        impl.reset();
    }
    public byte getTriesRemaining(){
        return impl.getTriesRemaining();
    }
    public byte getBioType(){
        return impl.getBioType();
    }
    public short getVersion(byte[] dest, short offset){
        return impl.getVersion(dest,offset);
    }
    public short getPublicTemplateData(short publicOffset, byte[] dest,
    short destOffset,
    short length) throws BioException{
        return impl.getPublicTemplateData(publicOffset,dest,destOffset,length);
    }
    public short initMatch(byte[] candidate, short offset, short length)
    throws BioException{
        return impl.initMatch(candidate,offset,length);
    }
    public short match(byte[] candidate, short offset, short length) 
    throws BioException{
        return impl.match(candidate,offset,length);
    }
}
