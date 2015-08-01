package AlgTest;

/**
 * @author Petr Svenda <petr@svenda.com>
 */


public class Consts {

    public final static byte TRUE                  = (byte) 0x01; 
    public final static byte FALSE                 = (byte) 0x00; 
    
        
    public final static byte CLA_CARD_ALGTEST                  = (byte) 0xB0; 
    public final static byte INS_CARD_GETVERSION               = (byte) 0x60;
    public final static byte INS_CARD_RESET                    = (byte) 0x69;
    
    public final static byte INS_CARD_TESTSUPPORTEDMODES       = (byte) 0x70;
    public final static byte INS_CARD_TESTAVAILABLE_MEMORY     = (byte) 0x71;
    public final static byte INS_CARD_TESTRSAEXPONENTSET       = (byte) 0x72;
    public final static byte INS_CARD_JCSYSTEM_INFO            = (byte) 0x73;
    public final static byte INS_CARD_TESTEXTAPDU              = (byte) 0x74;
    public final static byte INS_CARD_TESTSUPPORTEDMODES_SINGLE= (byte) 0x75;    
    public final static byte INS_CARD_GETRSAKEY                = (byte) 0x77;    
    public final static byte INS_CARD_DATAINOUT                = (byte) 0x78;    
    public final static byte INS_CARD_ALLOWED_KEYS             = (byte) 0x79;    
    public final static byte INS_CARD_ALLOWED_ENGINES          = (byte) 0x80;    
    
    
    
    // BUGBUG: refactor codes
    public final static byte INS_PERF_TEST_CLASS_KEY           = (byte) 0x40;
    public final static byte INS_PERF_TEST_CLASS_MESSAGEDIGEST = (byte) 0x41;
    public final static byte INS_PERF_TEST_CLASS_RANDOMDATA    = (byte) 0x42;
    public final static byte INS_PERF_TEST_CLASS_CIPHER        = (byte) 0x43;
    public final static byte INS_PERF_TEST_CLASS_KEYPAIR       = (byte) 0x45;
    public final static byte INS_PERF_TEST_CLASS_CHECKSUM      = (byte) 0x46;
    public final static byte INS_PERF_TEST_CLASS_KEYAGREEMENT  = (byte) 0x47;
    public final static byte INS_PERF_TEST_CLASS_SIGNATURE     = (byte) 0x49;
    public final static byte INS_PERF_TEST_CLASS_UTIL          = (byte) 0x50;
    
    public final static byte INS_PERF_TEST_CLASS_CIPHER_SETKEYINITDOFINAL     = (byte) 0xa3;
    public final static byte INS_PERF_TEST_CLASS_SIGNATURE_SETKEYINITSIGN     = (byte) 0xa9;
    
    public final static byte INS_PERF_TEST_SWALG_HOTP     = (byte) 0xa4;
    public final static byte INS_PERF_TEST_SWALGS         = (byte) 0xa5;
    
    
    
    
    
/*
    public final static byte INS_PERF_TEST_KEY_BUILDER         = (byte) 0x44;
    public final static byte INS_PERF_PREPARE_KEY              = (byte) 0x48;
*/    
    
    
    public final static byte INS_PREPARE_TEST_CLASS_KEY        = (byte) 0x30;
    public final static byte INS_PREPARE_TEST_CLASS_CIPHER     = (byte) 0x31;
    public final static byte INS_PREPARE_TEST_CLASS_SIGNATURE  = (byte) 0x32;
    public final static byte INS_PREPARE_TEST_CLASS_RANDOMDATA = (byte) 0x33;
    public final static byte INS_PREPARE_TEST_CLASS_MESSAGEDIGEST = (byte) 0x34;
    public final static byte INS_PREPARE_TEST_CLASS_CHECKSUM    = (byte) 0x35;
    public final static byte INS_PREPARE_TEST_CLASS_KEYPAIR     = (byte) 0x36;
    public final static byte INS_PREPARE_TEST_CLASS_KEYAGREEMENT= (byte) 0x37;
    public final static byte INS_PREPARE_TEST_CLASS_UTIL        = (byte) 0x38;
    
    public final static byte INS_PREPARE_TEST_SWALG_HOTP     = (byte) 0x39;
    public final static byte INS_PREPARE_TEST_SWALGS         = (byte) 0xc0;
    
    
    
    
   
    public static final short CLASS_CIPHER                      = (short) 0x11;      
    public static final short CLASS_SIGNATURE                   = (short) 0x12;      
    public static final short CLASS_KEYAGREEMENT                = (short) 0x13;      
    public static final short CLASS_MESSAGEDIGEST               = (short) 0x15;      
    public static final short CLASS_RANDOMDATA                  = (short) 0x16;      
    public static final short CLASS_CHECKSUM                    = (short) 0x17;      
    public static final short CLASS_KEYENCRYPTION               = (short) 0x18;      
    public static final short CLASS_KEYPAIR                     = (short) 0x19;      
    public static final short CLASS_KEYBUILDER                  = (short) 0x20;  
    public static final short CLASS_UTIL                        = (short) 0x21;  
    
    
    public static final byte UNUSED    = (byte) -1;     
    
    public static final short TEST_DATA_LENGTH    = (short) 256;     
    //public static final short TEST_DATA_LENGTH    = (short) 64;     
    
    public static final short AUTH_TAG_LENGTH    = (short) 8;     
    public static final short AES128_KEY_LENGTH  = (short) 16;     
    
    
    
    
	// TODO: refactor - do we need this?
    public static final byte CLASS_KEYPAIR_RSA_P2          = 11;
    public static final byte CLASS_KEYPAIR_RSACRT_P2       = 11;
    public static final byte CLASS_KEYPAIR_DSA_P2          = 3;
    public static final byte CLASS_KEYPAIR_EC_F2M_P2       = 4;
    public static final byte CLASS_KEYPAIR_EC_FP_P2        = 4;	
	// end refactor - do we need this?
        
    
    public static final short NUM_REPEAT_WHOLE_OPERATION        = (short) 50;
    public static final short NUM_REPEAT_WHOLE_OPERATION_VARIABLE_DATA = (short) 5;
    public static final short NUM_REPEAT_WHOLE_MEASUREMENT      = (short) 5;
    public static final short NUM_REPEAT_WHOLE_MEASUREMENT_KEYPAIRGEN      = (short) 10;
}
