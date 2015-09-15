
/*
    Copyright (c) 2004-2014  Petr Svenda <petr@svenda.com>
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
*/

/**
 *
 * @author Petr Svenda, Lenka Kunikova, Lukas Srom
 */
package AlgTest;


import javacard.framework.*;
import javacard.security.*;


public class AlgKeyHarvest {
    private   KeyPair          m_keyPair = null;
    private   AlgTest.TestSettings     m_testSettings = null;
    private   RSAPublicKey     m_rsaPublicKey = null;
    private   RSAPrivateCrtKey m_rsaPrivateCrtKey = null; 
    private   RSAPrivateKey    m_rsaPrivateKey = null; 
    
    private static final byte KeyPair_ALG_RSA                       = 1;
    private static final byte KeyPair_ALG_RSA_CRT                   = 2;
    private static final byte KeyBuilder_ALG_TYPE_RSA_PUBLIC        = 11;
    private static final byte KeyBuilder_ALG_TYPE_RSA_PRIVATE       = 12;
    private static final byte KeyBuilder_ALG_TYPE_RSA_CRT_PRIVATE   = 13;


    AlgKeyHarvest() { 
    	try
    	{
	    	m_testSettings = new AlgTest.TestSettings();
    	}
        catch (Exception e)
        {
	        ;
        }
    }

    public byte process(APDU apdu) throws ISOException {
        byte bProcessed = 0;
        byte[] apduBuffer = apdu.getBuffer();

        if (apduBuffer[ISO7816.OFFSET_CLA] == AlgTest.Consts.CLA_CARD_ALGTEST) {
            bProcessed = 1;
            switch (apduBuffer[ISO7816.OFFSET_INS]) {
                case AlgTest.Consts.INS_CARD_GETRSAKEY: GetRSAKey(apdu); break;
                default : {
                    bProcessed = 0;
                    break;
                }
            }
        }
        
        return bProcessed;
    }

   /**
    * Method for on-card generation of RSA keypair and export of result outside (in two apdu)
    * @param apdu 
    */
   void GetRSAKey(APDU apdu) {
      byte[]    apdubuf = apdu.getBuffer();
      m_testSettings.parse(apdu);

      // Generate new object if not before yet
      if (m_keyPair == null) {
          m_keyPair = new KeyPair((byte)m_testSettings.keyClass, m_testSettings.keyLength);
//          m_keyPair = new KeyPair(KeyPair.ALG_RSA_CRT, KeyBuilder.LENGTH_RSA_512);
      }	       
      
      switch (m_testSettings.keyType) {
        case KeyBuilder_ALG_TYPE_RSA_PUBLIC: {
            m_keyPair.genKeyPair();           
            m_rsaPublicKey = (RSAPublicKey) m_keyPair.getPublic();

            short offset = 0;
            apdubuf[offset] = (byte)0x82; offset++;
            short len = m_rsaPublicKey.getExponent(apdubuf, (short)(offset + 2));
            Util.setShort(apdubuf, offset, len); 
            offset += 2;    // length
            offset += len;  // value
            
            apdubuf[offset] = (byte)0x82; offset++;
            len = m_rsaPublicKey.getModulus(apdubuf, (short) (offset + 2));
            Util.setShort(apdubuf, offset, len); 
            offset += 2;    // length
            offset += len;  // value

            apdu.setOutgoingAndSend((short) 0, offset);
			JCSystem.requestObjectDeletion();
            break;
        }
        case KeyBuilder_ALG_TYPE_RSA_PRIVATE: {
            short offset = 0;
            if(m_testSettings.keyClass == KeyPair_ALG_RSA_CRT) {
                m_rsaPrivateCrtKey = (RSAPrivateCrtKey) m_keyPair.getPrivate();
                
                short len = m_rsaPrivateCrtKey.getP(apdubuf, (short)(offset + 3));
                apdubuf[offset] = (byte)0x82; offset++;
                Util.setShort(apdubuf, offset, len); offset += 2;
                offset += len;

                len = m_rsaPrivateCrtKey.getQ(apdubuf, (short)(offset + 3));
                apdubuf[offset] = (byte)0x82; offset++;
                Util.setShort(apdubuf, offset, len); offset += 2;
                offset += len;
            }
            else if(m_testSettings.keyClass == KeyPair_ALG_RSA) {
                m_rsaPrivateKey = (RSAPrivateKey) m_keyPair.getPrivate();
                
                short len = m_rsaPrivateKey.getExponent(apdubuf, (short)(offset + 3));
                apdubuf[offset] = (byte)0x82; offset++;
                Util.setShort(apdubuf, offset, len); offset += 2;
                offset += len;

                len = m_rsaPrivateKey.getModulus(apdubuf, (short)(offset + 3));
                apdubuf[offset] = (byte)0x82; offset++;
                Util.setShort(apdubuf, offset, len); offset += 2;
                offset += len;
            }
            else ISOException.throwIt( ISO7816.SW_COMMAND_NOT_ALLOWED) ;
                    
            apdu.setOutgoingAndSend((short) 0, offset);
            JCSystem.requestObjectDeletion();
            break;
         }
      }
    }
}
