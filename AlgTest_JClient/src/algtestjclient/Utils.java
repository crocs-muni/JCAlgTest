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

import algtest.JCConsts;

/**
 *
 * @author github.com/petrs
 */
public class Utils {

    // Parse algorithm name and version of JC which introduced it
    //algParts[0] == algorithm name
    //algParts[1] == introducing version
    //algParts[2] == should be this item included in output? 1/0
    public static String GetAlgorithmName(String algorithmInfoString) {
        String[] algParts = algorithmInfoString.split("#");
        return algParts[0];
    }

    public static String GetAlgorithmIntroductionVersion(String algorithmInfoString) {
        String[] algParts = algorithmInfoString.split("#");
        String algorithmVersion = (algParts.length > 1) ? algParts[1] : "";
        return algorithmVersion;
    }

    public static boolean ShouldBeIncludedInOutput(String algorithmInfoString) {
        String[] algParts = algorithmInfoString.split("#");
        String includeInfo = (algParts.length > 2) ? algParts[2] : "1";
        return Integer.decode(includeInfo) != 0;
    }
    

    /**
     * Convert error from card to text string
     * @param swStatus
     * @return 
     */
    public static String ErrorToString(int swStatus) {
        // lower byte of exception is value as defined in JCSDK/api_classic/constant-values.htm
        //https://docs.oracle.com/javacard/3.0.5/api/constant-values.html
        
        short sw1 = (short) (swStatus & 0xff00);
        short sw2 = (short) (swStatus & 0x00ff);
        switch (sw1) {
            case JCConsts.SW_Exception_prefix: 
                switch (sw2) { 
                    case JCConsts.SW_Exception:
                        return "Exception";
                    case JCConsts.SW_ArrayIndexOutOfBoundsException:
                        return "ArrayIndexOutOfBoundsException";
                    case JCConsts.SW_ArithmeticException:
                        return "ArithmeticException"; 
                    case JCConsts.SW_ArrayStoreException:
                        return "ArrayStoreException";
                    case JCConsts.SW_NullPointerException:
                        return "NullPointerException";
                    case JCConsts.SW_NegativeArraySizeException:
                        return "NegativeArraySizeException";
                }
            case JCConsts.SW_CryptoException_prefix:
                switch (sw2) {
                    case JCConsts.CryptoException_ILLEGAL_VALUE:
                        return "CryptoException_ILLEGAL_VALUE";
                    case JCConsts.CryptoException_UNINITIALIZED_KEY:
                        return "CryptoException_UNINITIALIZED_KEY";
                    case JCConsts.CryptoException_NO_SUCH_ALGORITHM:
                        return "CryptoException_NO_SUCH_ALGORITHM";
                    case JCConsts.CryptoException_INVALID_INIT:
                        return "CryptoException_INVALID_INIT";
                    case JCConsts.CryptoException_ILLEGAL_USE:
                        return "CryptoException_ILLEGAL_USE";
                    default: return "CryptoException_" + Integer.toHexString(sw2);                
                }
            case JCConsts.SW_SystemException_prefix:
                switch (sw2) {
                    case JCConsts.SystemException_ILLEGAL_VALUE:
                        return "SystemException_ILLEGAL_VALUE";
                    case JCConsts.SystemException_NO_TRANSIENT_SPACE:
                        return "SystemException_NO_TRANSIENT_SPACE";
                    case JCConsts.SystemException_ILLEGAL_TRANSIENT:
                        return "SystemException_ILLEGAL_TRANSIENT";
                    case JCConsts.SystemException_ILLEGAL_AID:
                        return "SystemException_ILLEGAL_AID";
                    case JCConsts.SystemException_NO_RESOURCE:
                        return "SystemException_NO_RESOURCE";
                    case JCConsts.SystemException_ILLEGAL_USE:
                        return "SystemException_ILLEGAL_USE";
                    default:
                        return "SystemException_" + Integer.toHexString(sw2);
                }
            case JCConsts.SW_PINException_prefix:
                switch (sw2) {
                    case JCConsts.PINException_ILLEGAL_VALUE:
                        return "PINException_ILLEGAL_VALUE";
                    case JCConsts.PINException_ILLEGAL_STATE:
                        return "PINException_ILLEGAL_STATE";
                    default:
                        return "PINException_" + Integer.toHexString(sw2);
                }
            case JCConsts.SW_TransactionException_prefix:
                switch (sw2) {
                    case JCConsts.TransactionException_IN_PROGRESS:
                        return "TransactionException_IN_PROGRESS";
                    case JCConsts.TransactionException_NOT_IN_PROGRESS:
                        return "TransactionException_NOT_IN_PROGRESS";
                    case JCConsts.TransactionException_BUFFER_FULL:
                        return "TransactionException_BUFFER_FULL";
                    case JCConsts.TransactionException_INTERNAL_FAILURE:
                        return "TransactionException_INTERNAL_FAILURE";
                    case JCConsts.TransactionException_ILLEGAL_USE:
                        return "TransactionException_ILLEGAL_USE";
                    default:
                        return "TransactionException_" + Integer.toHexString(sw2);
                }
            case JCConsts.SW_CardRuntimeException_prefix:
                return "CardRuntimeException_" + Integer.toHexString(sw2);
            case (short) 0x6a00:
                switch (sw2) {
                    case (short) 0x0081:
                        return "FUNC_NOT_SUPPORTED_" + Integer.toHexString(swStatus);
                }
            default:
                return "UNKONWN_ERROR-card_has_return_value_" + Integer.toHexString(swStatus);    
        }
    }    
}

