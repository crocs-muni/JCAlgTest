/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 /*
 * @(#)SigMsgRecApplet.java	1.3 06/01/03
 */

 package com.sun.javacard.samples.sigMsgRec;
 
 
import javacard.framework.*;
import javacard.security.RSAPublicKey;
import javacard.security.RSAPrivateKey;
import javacard.security.SignatureMessageRecovery;
import javacard.security.KeyPair;
import javacard.security.Signature;
import javacard.security.KeyBuilder;

/**
* package AID 0xA0:0x00:0x00:0x00:0x62:0x03:0x01:0x0C:0x0C
**/

public class SigMsgRecApplet extends Applet{
    public final static byte CLA=(byte)0xCF;
    public final static byte INS_SIGN=(byte)0x10;
    public final static byte INS_VERIFY_FULL_MSG_REC=(byte)0x11;
    public final static byte INS_VERIFY_PART_MSG_REC=(byte)0x12;

    //--Error codes
    public static final short ERROR_BAD_SIG_LEN=(short)0x9101;
    public static final short ERROR_VERIFICATION_FAILED=(short)0x9102;
    
    public final static short SIG_LENGTH=64;
    

    //--RSA Keypair data
    private static final byte[] RSA_PUB_KEY_EXP = {(byte)0x01, (byte)0x00, (byte)0x01};
    private static final byte[] RSA_PUB_PRIV_KEY_MOD = { (byte)0xbe, (byte)0xdf, 
        (byte)0xd3, (byte)0x7a, (byte)0x08, (byte)0xe2, (byte)0x9a, (byte)0x58, 
        (byte)0x27, (byte)0x54, (byte)0x2a, (byte)0x49, (byte)0x18, (byte)0xce, 
        (byte)0xe4, (byte)0x1a, (byte)0x60, (byte)0xdc, (byte)0x62, (byte)0x75, 
        (byte)0xbd, (byte)0xb0, (byte)0x8d, (byte)0x15, (byte)0xa3, (byte)0x65, 
        (byte)0xe6, (byte)0x7b, (byte)0xa9, (byte)0xdc, (byte)0x09, (byte)0x11, 
        (byte)0x5f, (byte)0x9f, (byte)0xbf, (byte)0x29, (byte)0xe6, (byte)0xc2, 
        (byte)0x82, (byte)0xc8, (byte)0x35, (byte)0x6b, (byte)0x0f, (byte)0x10, 
        (byte)0x9b, (byte)0x19, (byte)0x62, (byte)0xfd, (byte)0xbd, (byte)0x96, 
        (byte)0x49, (byte)0x21, (byte)0xe4, (byte)0x22, (byte)0x08, (byte)0x08, 
        (byte)0x80, (byte)0x6c, (byte)0xd1, (byte)0xde, (byte)0xa6, (byte)0xd3, 
        (byte)0xc3, (byte)0x8f};

    private static final byte[] RSA_PRIV_KEY_EXP = { (byte)0x84, (byte)0x21, 
        (byte)0xfe, (byte)0x0b, (byte)0xa4, (byte)0xca, (byte)0xf9, (byte)0x7d, 
        (byte)0xbc, (byte)0xfc, (byte)0x0e, (byte)0xa9, (byte)0xbb, (byte)0x7a, 
        (byte)0xbd, (byte)0x7d, (byte)0x65, (byte)0x40, (byte)0x2b, (byte)0x08, 
        (byte)0xc6, (byte)0xdf, (byte)0xc9, (byte)0x4b, (byte)0x09, (byte)0x6a, 
        (byte)0x29, (byte)0x3b, (byte)0xc2, (byte)0x42, (byte)0x88, (byte)0x23, 
        (byte)0x44, (byte)0xaf, (byte)0x08, (byte)0x82, (byte)0x4c, (byte)0xff, 
        (byte)0x42, (byte)0xa4, (byte)0xb8, (byte)0xd2, (byte)0xda, (byte)0xcc, 
        (byte)0xee, (byte)0xc5, (byte)0x34, (byte)0xed, (byte)0x71, (byte)0x01, 
        (byte)0xab, (byte)0x3b, (byte)0x76, (byte)0xde, (byte)0x6c, (byte)0xa2, 
        (byte)0xcb, (byte)0x7c, (byte)0x38, (byte)0xb6, (byte)0x9a, (byte)0x4b, 
        (byte)0x28, (byte)0x01};


    RSAPublicKey pubKey;
    RSAPrivateKey privKey;
    
    SignatureMessageRecovery sig;
    /**
    * the sigBuff buffer would hold the signature output of the Signature
    * class and an extra 2 bytes for the length of recoverable message
    * sent back to the caller
    **/
    byte []sigBuff;
    short sigLen;
    /**
    * the following state variable has the following value:
    * 0 => recovery has not yet started
    * 1 => signature recovered. Waiting for rest of message
    **/
    byte recState;
    
     /**
     * Only this class's install method should create the applet object.
     */
    protected SigMsgRecApplet(byte[] bArray, short bOffset, byte bLength){       
        byte aidLen = bArray[bOffset];
        if (aidLen== (byte)0){
            //System.out.println("using dfault");
            register();
        } else {
            //System.out.println("using provided");
            register(bArray, (short)(bOffset+1), aidLen);
        }                    
        pubKey = (RSAPublicKey)KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PUBLIC,KeyBuilder.LENGTH_RSA_512,false);
        privKey = (RSAPrivateKey)KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PRIVATE,KeyBuilder.LENGTH_RSA_512,false);
        privKey.setExponent(RSA_PRIV_KEY_EXP,(short)0,(short)RSA_PRIV_KEY_EXP.length);
        privKey.setModulus(RSA_PUB_PRIV_KEY_MOD,(short)0,(short)RSA_PUB_PRIV_KEY_MOD.length);
        pubKey.setExponent(RSA_PUB_KEY_EXP,(short)0,(short)RSA_PUB_KEY_EXP.length);
        pubKey.setModulus(RSA_PUB_PRIV_KEY_MOD,(short)0,(short)RSA_PUB_PRIV_KEY_MOD.length);
        sigBuff = JCSystem.makeTransientByteArray((short)(SIG_LENGTH+2),JCSystem.CLEAR_ON_DESELECT);
        sig = (SignatureMessageRecovery)Signature.getInstance(Signature.ALG_RSA_SHA_ISO9796_MR,false);
        recState = (byte)0;
    }
    /**
     * Installs this applet.
     * @param bArray the array containing installation parameters
     * @param bOffset the starting offset in bArray
     * @param bLength the length in bytes of the parameter data in bArray
     */
    public static void install(byte[] bArray, short bOffset, byte bLength)
    {
        new SigMsgRecApplet(bArray,bOffset,bLength);
    }
    
     /**
     * Processes an incoming APDU.
     * @see APDU
     * @param apdu the incoming APDU
     * @exception ISOException with the response bytes per ISO 7816-4
     */
    public void process(APDU apdu)
    {
        byte buffer[] = apdu.getBuffer();

        // check SELECT APDU command
        if ((buffer[ISO7816.OFFSET_CLA] == 0) &&
        (buffer[ISO7816.OFFSET_INS] == (byte)(0xA4))){
            return;
        }
        
        switch (buffer[ISO7816.OFFSET_INS]){
            case INS_SIGN:
            testCryptoSign(apdu);
            break;
            case INS_VERIFY_FULL_MSG_REC:
            testCryptoVerifyFullMsgRecovery(apdu);
            break;
            case INS_VERIFY_PART_MSG_REC:
            testCryptoVerifyPartMsgRecovery(apdu);
            break;
            default:
            ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
        
    }
    
    /**
    * For the purposes of this sample, Assumed that all the data to be
    * signed fits into one APDU buffer
    **/
    private void testCryptoSign(APDU apdu){
        byte []buffer = apdu.getBuffer();
        short bytesRead = apdu.setIncomingAndReceive();
        short []m1Data = JCSystem.makeTransientShortArray((short)1,JCSystem.CLEAR_ON_DESELECT);

        sig.init(privKey,Signature.MODE_SIGN);
        sigLen=sig.sign(buffer,ISO7816.OFFSET_CDATA,bytesRead,sigBuff,(short)0,m1Data,(short)0);
        
        //set m1Length into sigBuff array
        sigBuff[sigLen] = (byte)((short)(m1Data[(short)0] & ((short)0xFF00)) >> ((short)8));
        sigBuff[(short)(sigLen+1)] = (byte)(m1Data[(short)0] & ((short)0x00FF));
        
        apdu.setOutgoing();
        apdu.setOutgoingLength((short)(sigLen+2));//The extra 2 bytes for m1Length
        apdu.sendBytesLong(sigBuff,(short)0,(short)(sigLen+2));
    }
    
    /**
    * in this case, all the message is inside the signature.
    * We only expect one APDU with signature
    **/
    private void testCryptoVerifyFullMsgRecovery(APDU apdu){
        sig.init(pubKey,Signature.MODE_VERIFY);
        boolean verified=false;
        byte []buffer = apdu.getBuffer();
        short dataLength = (short)(buffer[ISO7816.OFFSET_LC] & (short)0xFF);
         //get the signature from APDU
        short bytesRead = apdu.setIncomingAndReceive();
        Util.arrayCopyNonAtomic(buffer, ISO7816.OFFSET_CDATA, sigBuff, (short)0,bytesRead);
        short m1Length = sig.beginVerify(sigBuff,(short)0,bytesRead);
        verified = sig.verify(sigBuff,(short)0,(short)0);        
        //In either case m1 is consumed by this applet
        if(!verified){
            ISOException.throwIt(ERROR_VERIFICATION_FAILED);
        }
    }
    
    /**
    * This method is called when there is partial message recovery. The recoverable
    * message inside the signature is consumed by this applet. In this case, the
    * first APDU contains the signature and returns true if recovery successful
    * the second APDU contains the remainder of the message and the return value
    * represents signature verification
    **/
    private void testCryptoVerifyPartMsgRecovery(APDU apdu){
        byte []buffer = apdu.getBuffer();
        short dataLength = (short)(buffer[ISO7816.OFFSET_LC] & (short)0xFF);
        //get the signature from APDU
        short bytesRead = apdu.setIncomingAndReceive();
        Util.arrayCopyNonAtomic(buffer, ISO7816.OFFSET_CDATA, sigBuff, (short)0,bytesRead);
        if(recState == 0){
            //recover the recoverable message from signature
            sig.init(pubKey,Signature.MODE_VERIFY);
            short m1Length = sig.beginVerify(sigBuff,(short)0,bytesRead);
            //consume the recoverable message. Theen discard it
            sigBuff[0] = (byte)((short)(m1Length & ((short)0xFF00)) >> ((short)8));
            sigBuff[1] = (byte)(m1Length & ((short)0x00FF));
            //return back the length of recoverable message
            apdu.setOutgoing();
            apdu.setOutgoingLength((short)2);
            apdu.sendBytesLong(sigBuff,(short)0,(short)2);
            recState = 1;
        }else{
            recState = 0;
            //rest of message sent. verify
            if(!sig.verify(sigBuff,(short)0,bytesRead)){
                ISOException.throwIt(ERROR_VERIFICATION_FAILED);
            }
        }
    }
}
    
    
