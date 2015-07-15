package AlgTest;

import javacard.framework.APDU;
import javacard.framework.ISO7816;
import javacard.framework.Util;

/**
 * @author Petr Svenda <petr@svenda.com>
 */
public class TestSettings {
    public final static short OFFSET_ALGORITHM_CLASS           = (short) 0;
    public final static short OFFSET_ALGORITHM_SPECIFICATION   = (short) (OFFSET_ALGORITHM_CLASS + 2);
    public final static short OFFSET_ALGORITHM_PARAM1          = (short) (OFFSET_ALGORITHM_SPECIFICATION + 2);
    public final static short OFFSET_ALGORITHM_PARAM2          = (short) (OFFSET_ALGORITHM_PARAM1 + 2);
    public final static short OFFSET_ALGORITHM_PARAM3          = (short) (OFFSET_ALGORITHM_PARAM2 + 2);
    public final static short OFFSET_ALGORITHM_TESTED_OPS      = (short) (OFFSET_ALGORITHM_PARAM3 + 2);
    public final static short OFFSET_DATA_LENGTH1              = (short) (OFFSET_ALGORITHM_TESTED_OPS + 2);
    public final static short OFFSET_DATA_LENGTH2              = (short) (OFFSET_DATA_LENGTH1 + 2);
    public final static short OFFSET_DATA_INITMODE             = (short) (OFFSET_DATA_LENGTH2 + 2);
    public final static short OFFSET_NUM_REPEAT_WHOLE_OP       = (short) (OFFSET_DATA_INITMODE + 2);
    public final static short OFFSET_NUM_REPEAT_SUB_OP         = (short) (OFFSET_NUM_REPEAT_WHOLE_OP + 2);
    public final static short TEST_SETTINGS_LENGTH             = (short) (OFFSET_NUM_REPEAT_SUB_OP + 2 - OFFSET_ALGORITHM_CLASS);
    

    public short       classType = -1;                     // e.g., javacardx.crypto.Cipher
    public short       algorithmSpecification = -1;        // e.g., Cipher.ALG_AES_BLOCK_128_CBC_NOPAD
    public short       keyClass = -1;                      // unused for ALG_AES, but e.g., KeyPair.ALG_RSA for KeyBuilder.TYPE_RSA_PRIVATE
    public short       keyType = -1;                       // e.g., KeyBuilder.TYPE_AES
    public short       keyLength = -1;                     // e.g., KeyBuilder.LENGTH_AES_128
    public short       algorithmMethod = -1;               // e.g., AESKey.setKey() - our custom constant
    public short       dataLength1 = -1;                   // e.g., length of data used during measurement (e.g., for update())
    public short       dataLength2 = -1;                   // e.g., length of data used during measurement (e.g., for doFinal())
    public short       initMode = -1;                      // initialization mode for init(key, mode), e.g., Cipher.ENCRYPT 
    public short       numRepeatWholeOperation = 1;        // whole operation might be setKey, update, doFinal - numRepeatWholeOperation repeats this whole operation
    public short       numRepeatSubOperation = 1;          // relevant suboperation that should be iterated multiple times - e.g., update()
    public short       numRepeatWholeMeasurement = 1;      // whole measurement including apdu in/out repeated

    // Values below are NOT serialized into buffer
    public byte        bPerformBaselineMeasurement = Consts.TRUE; // if true, measurement of input / output overhead just before target operation is measured and substracted 
    public byte        P1 = (byte) 0;                              // P1 byte from APDU header
    public byte        P2 = (byte) 0;                              // P2 byte from APDU header
    public byte[]      inData = null; 
    
    public void clear() {
        classType = -1;               
        algorithmSpecification = -1;        
        keyClass = -1;                 
        keyType = -1;                 
        keyLength = -1;           
        algorithmMethod = -1;           
        dataLength1 = -1;                  
        dataLength2 = -1;                  
        numRepeatWholeOperation = 1;        
        numRepeatSubOperation = 1;        
        numRepeatWholeMeasurement = 1;
        bPerformBaselineMeasurement = Consts.TRUE;
        P1 = (byte) 0;  
        P2 = (byte) 0;  
        inData = null; 
    }
    public void parse(APDU apdu) {
        byte[] apdubuf = apdu.getBuffer();
        short len = apdu.setIncomingAndReceive();
        
        parse(apdubuf, ISO7816.OFFSET_CDATA, len);
    }
    public void parse(byte[] apdubuf, short offset, short len) {
        this.clear();

        if (len >= (short) (OFFSET_ALGORITHM_CLASS + 2)) { 
            classType = Util.getShort(apdubuf, (short) (OFFSET_ALGORITHM_CLASS + offset));                    
        }
        if (len >= (short) (OFFSET_ALGORITHM_SPECIFICATION + 2)) { 
            algorithmSpecification = Util.getShort(apdubuf, (short) (OFFSET_ALGORITHM_SPECIFICATION + offset));    
        }
        if (len >= (short) (OFFSET_ALGORITHM_PARAM1 + 2)) { 
            keyClass = Util.getShort(apdubuf, (short) (OFFSET_ALGORITHM_PARAM1 + offset));                    
        }
        if (len >= (short) (OFFSET_ALGORITHM_PARAM2 + 2)) { 
            keyType = Util.getShort(apdubuf, (short) (OFFSET_ALGORITHM_PARAM2 + offset));                    
        }
        if (len >= (short) (OFFSET_ALGORITHM_PARAM3 + 2)) { 
            keyLength = Util.getShort(apdubuf, (short) (OFFSET_ALGORITHM_PARAM3 + offset));               
        }
        if (len >= (short) (OFFSET_ALGORITHM_TESTED_OPS + 2)) { 
            algorithmMethod = Util.getShort(apdubuf, (short) (OFFSET_ALGORITHM_TESTED_OPS + offset));           
        }
        if (len >= (short) (OFFSET_DATA_LENGTH1 + 2)) { 
            dataLength1 = Util.getShort(apdubuf, (short) (OFFSET_DATA_LENGTH1 + offset));                         
        }
        if (len >= (short) (OFFSET_DATA_LENGTH2 + 2)) { 
            dataLength2 = Util.getShort(apdubuf, (short) (OFFSET_DATA_LENGTH2 + offset));                          
        }
        if (len >= (short) (OFFSET_DATA_INITMODE + 2)) { 
            initMode = Util.getShort(apdubuf, (short) (OFFSET_DATA_INITMODE + offset));                          
        }
        if (len >= (short) (OFFSET_NUM_REPEAT_WHOLE_OP + 2)) { 
            numRepeatWholeOperation = Util.getShort(apdubuf, (short) (OFFSET_NUM_REPEAT_WHOLE_OP + offset));  
        }
        if (len >= (short) (OFFSET_NUM_REPEAT_SUB_OP + 2)) { 
            numRepeatSubOperation = Util.getShort(apdubuf, (short) (OFFSET_NUM_REPEAT_SUB_OP + offset));  
        }
    }

    public short serializeToApduBuff(byte[] apdubuf, short offset) {
        Util.setShort(apdubuf, (short) (offset + OFFSET_ALGORITHM_CLASS), classType);                    
        Util.setShort(apdubuf, (short) (offset + OFFSET_ALGORITHM_SPECIFICATION), algorithmSpecification);                    
        Util.setShort(apdubuf, (short) (offset + OFFSET_ALGORITHM_PARAM1), keyClass);                  
        Util.setShort(apdubuf, (short) (offset + OFFSET_ALGORITHM_PARAM2), keyType);               
        Util.setShort(apdubuf, (short) (offset + OFFSET_ALGORITHM_PARAM3), keyLength);               
        Util.setShort(apdubuf, (short) (offset + OFFSET_ALGORITHM_TESTED_OPS), algorithmMethod);                   
        Util.setShort(apdubuf, (short) (offset + OFFSET_DATA_LENGTH1), dataLength1);                   
        Util.setShort(apdubuf, (short) (offset + OFFSET_DATA_LENGTH2), dataLength2);                   
        Util.setShort(apdubuf, (short) (offset + OFFSET_DATA_INITMODE), initMode);                   
        Util.setShort(apdubuf, (short) (offset + OFFSET_NUM_REPEAT_WHOLE_OP), numRepeatWholeOperation);                
        Util.setShort(apdubuf, (short) (offset + OFFSET_NUM_REPEAT_SUB_OP), numRepeatSubOperation);                   
        
        return TestSettings.TEST_SETTINGS_LENGTH;
    }
}
