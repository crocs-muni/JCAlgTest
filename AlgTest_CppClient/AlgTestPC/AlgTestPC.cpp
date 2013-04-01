// AlgTestPC.cpp : Defines the entry point for the console application.
/*  
    Copyright (c) 2004-2010 Petr Svenda <petr@svenda.com>

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

#include "stdafx.h"
#include "AlgTestPC.h"
#include "PCSCMngr.h"
#include <list>
#include <time.h>

using namespace std;


#ifdef _DEBUG
#define new DEBUG_NEW
#endif

#define MAX_SUPP_ALG            240    
#define SUPP_ALG_UNTOUCHED      5

#define ALGTEST_AID_LEN         9
char ALGTEST_AID[] = {0x6D, 0x79, 0x70, 0x61, 0x63, 0x30, 0x30, 0x30, 0x31};

#define CLASS_CIPHER            0x11
#define CLASS_SIGNATURE         0x12 
#define CLASS_KEYAGREEMENT      0x13
#define CLASS_MESSAGEDIGEST     0x15
#define CLASS_RANDOMDATA        0x16
#define CLASS_CHECKSUM          0x17
#define CLASS_KEYPAIR_RSA       0x18
#define CLASS_KEYPAIR_RSA_CRT   0x19
#define CLASS_KEYPAIR_DSA       0x1A
#define CLASS_KEYPAIR_EC_F2M    0x1B
#define CLASS_KEYPAIR_EC_FP     0x1C

#define CLASS_KEYBUILDER        0x20


  //  
  //Class javacard.security.Signature
  //
#define ALG_DES_MAC4_NOPAD                    1
#define ALG_DES_MAC8_NOPAD                    2
#define ALG_DES_MAC4_ISO9797_M1               3
#define ALG_DES_MAC8_ISO9797_M1               4
#define ALG_DES_MAC4_ISO9797_M2               5
#define ALG_DES_MAC8_ISO9797_M2               6
#define ALG_DES_MAC4_PKCS5                    7
#define ALG_DES_MAC8_PKCS5                    8
#define ALG_RSA_SHA_ISO9796                   9
#define ALG_RSA_SHA_PKCS1                     10
#define ALG_RSA_MD5_PKCS1                     11
#define ALG_RSA_RIPEMD160_ISO9796             12
#define ALG_RSA_RIPEMD160_PKCS1               13
#define ALG_DSA_SHA                           14
#define ALG_RSA_SHA_RFC2409                   15
#define ALG_RSA_MD5_RFC2409                   16
#define ALG_ECDSA_SHA                         17
#define ALG_AES_MAC_128_NOPAD                 18
#define ALG_DES_MAC4_ISO9797_1_M2_ALG3        19
#define ALG_DES_MAC8_ISO9797_1_M2_ALG3        20
#define ALG_RSA_SHA_PKCS1_PSS                 21
#define ALG_RSA_MD5_PKCS1_PSS                 22
#define ALG_RSA_RIPEMD160_PKCS1_PSS           23
  // JC2.2.2
#define ALG_HMAC_SHA1                         24
#define ALG_HMAC_SHA_256                      25
#define ALG_HMAC_SHA_384                      26
#define ALG_HMAC_SHA_512                      27
#define ALG_HMAC_MD5                          28
#define ALG_HMAC_RIPEMD160                    29
#define ALG_RSA_SHA_ISO9796_MR                30
#define ALG_RSA_RIPEMD160_ISO9796_MR          31
#define ALG_SEED_MAC_NOPAD                    32

char* SIGNATURE_STR[] = {"javacard.crypto.Signature", "ALG_DES_MAC4_NOPAD", "ALG_DES_MAC8_NOPAD", 
    "ALG_DES_MAC4_ISO9797_M1", "ALG_DES_MAC8_ISO9797_M1", "ALG_DES_MAC4_ISO9797_M2", "ALG_DES_MAC8_ISO9797_M2", 
    "ALG_DES_MAC4_PKCS5", "ALG_DES_MAC8_PKCS5", "ALG_RSA_SHA_ISO9796", "ALG_RSA_SHA_PKCS1", "ALG_RSA_MD5_PKCS1", 
    "ALG_RSA_RIPEMD160_ISO9796", "ALG_RSA_RIPEMD160_PKCS1", "ALG_DSA_SHA", "ALG_RSA_SHA_RFC2409", 
    "ALG_RSA_MD5_RFC2409", "ALG_ECDSA_SHA", "ALG_AES_MAC_128_NOPAD", "ALG_DES_MAC4_ISO9797_1_M2_ALG3", 
    "ALG_DES_MAC8_ISO9797_1_M2_ALG3", "ALG_RSA_SHA_PKCS1_PSS", "ALG_RSA_MD5_PKCS1_PSS", "ALG_RSA_RIPEMD160_PKCS1_PSS", 
    "ALG_HMAC_SHA1", "ALG_HMAC_SHA_256", "ALG_HMAC_SHA_384", "ALG_HMAC_SHA_512", "ALG_HMAC_MD5", "ALG_HMAC_RIPEMD160", 
    "ALG_RSA_SHA_ISO9796_MR", "ALG_RSA_RIPEMD160_ISO9796_MR", "ALG_SEED_MAC_NOPAD"
};

  //
  //Class javacardx.crypto.Cipher
  //
#define ALG_DES_CBC_NOPAD                       1
#define ALG_DES_CBC_ISO9797_M1                  2
#define ALG_DES_CBC_ISO9797_M2                  3
#define ALG_DES_CBC_PKCS5                       4
#define ALG_DES_ECB_NOPAD                       5
#define ALG_DES_ECB_ISO9797_M1                  6
#define ALG_DES_ECB_ISO9797_M2                  7
#define ALG_DES_ECB_PKCS5                       8
#define ALG_RSA_ISO14888                        9
#define ALG_RSA_PKCS1                           10
#define ALG_RSA_ISO9796                         11
#define ALG_RSA_NOPAD                           12
#define ALG_AES_BLOCK_128_CBC_NOPAD             13
#define ALG_AES_BLOCK_128_ECB_NOPAD             14
#define ALG_RSA_PKCS1_OAEP                      15
  // JC2.2.2
#define ALG_KOREAN_SEED_ECB_NOPAD               16
#define ALG_KOREAN_SEED_CBC_NOPAD               17

char* CIPHER_STR[] = {"javacardx.crypto.Cipher", "ALG_DES_CBC_NOPAD", "ALG_DES_CBC_ISO9797_M1", "ALG_DES_CBC_ISO9797_M2", "ALG_DES_CBC_PKCS5", 
    "ALG_DES_ECB_NOPAD", "ALG_DES_ECB_ISO9797_M1", "ALG_DES_ECB_ISO9797_M2", "ALG_DES_ECB_PKCS5",
    "ALG_RSA_ISO14888", "ALG_RSA_PKCS1", "ALG_RSA_ISO9796", "ALG_RSA_NOPAD", "ALG_AES_BLOCK_128_CBC_NOPAD", 
    "ALG_AES_BLOCK_128_ECB_NOPAD", "ALG_RSA_PKCS1_OAEP", "ALG_KOREAN_SEED_ECB_NOPAD", "ALG_KOREAN_SEED_CBC_NOPAD"
}; 

  //
  //Class javacard.security.KeyAgreement
  //
#define ALG_EC_SVDP_DH                          1
#define ALG_EC_SVDP_DHC                         2

char* KEYAGREEMENT_STR[] = {"javacard.security.KeyAgreement", "ALG_EC_SVDP_DH", "ALG_EC_SVDP_DHC"}; 

  //
  //Class javacard.security.KeyBuilder
  //
#define TYPE_DES_TRANSIENT_RESET                2
#define TYPE_DES_TRANSIENT_DESELECT             3
#define TYPE_DES                                4
#define TYPE_RSA_PUBLIC                         4
#define TYPE_RSA_PRIVATE                        5
#define TYPE_RSA_CRT_PRIVATE                    6
#define TYPE_DSA_PUBLIC                         7
#define TYPE_DSA_PRIVATE                        8
#define TYPE_EC_F2M_PUBLIC                      9
#define TYPE_EC_F2M_PRIVATE                     10
#define TYPE_EC_FP_PUBLIC                       11
#define TYPE_EC_FP_PRIVATE                      12
#define TYPE_AES_TRANSIENT_RESET                13
#define TYPE_AES_TRANSIENT_DESELECT             14
#define TYPE_AES                                15
  // JC2.2.2
#define TYPE_KOREAN_SEED_TRANSIENT_RESET        16
#define TYPE_KOREAN_SEED_TRANSIENT_DESELECT     17
#define TYPE_KOREAN_SEED                        18
#define TYPE_HMAC_TRANSIENT_RESET               19
#define TYPE_HMAC_TRANSIENT_DESELECT            20
#define TYPE_HMAC                               21

#define LENGTH_DES              64
#define LENGTH_DES3_2KEY        128
#define LENGTH_DES3_3KEY        192
#define LENGTH_RSA_512          512
#define LENGTH_RSA_736          736
#define LENGTH_RSA_768          768
#define LENGTH_RSA_896          896
#define LENGTH_RSA_1024         1024
#define LENGTH_RSA_1280         1280
#define LENGTH_RSA_1536         1536
#define LENGTH_RSA_1984         1984
#define LENGTH_RSA_2048         2048
#define LENGTH_DSA_512          512
#define LENGTH_DSA_768          768
#define LENGTH_DSA_1024         1024
#define LENGTH_EC_FP_112        112
#define LENGTH_EC_F2M_113       113
#define LENGTH_EC_FP_128        128
#define LENGTH_EC_F2M_131       131
#define LENGTH_EC_FP_160        160
#define LENGTH_EC_F2M_163       163
#define LENGTH_EC_FP_192        192
#define LENGTH_EC_F2M_193       193
#define LENGTH_AES_128          128
#define LENGTH_AES_192          192
#define LENGTH_AES_256          256
  // JC2.2.2
#define LENGTH_KOREAN_SEED_128          128
#define LENGTH_HMAC_SHA_1_BLOCK_64      64
#define LENGTH_HMAC_SHA_256_BLOCK_64    64
#define LENGTH_HMAC_SHA_384_BLOCK_64    128
#define LENGTH_HMAC_SHA_512_BLOCK_64    128

char* KEYBUILDER_STR[] = {"javacard.security.KeyBuilder", 
    "###DES_KEY###", "TYPE_DES_TRANSIENT_RESET", "TYPE_DES_TRANSIENT_DESELECT", "TYPE_DES LENGTH_DES", "TYPE_DES LENGTH_DES3_2KEY", "TYPE_DES LENGTH_DES3_3KEY",
    "###AES_KEY###", "TYPE_AES_TRANSIENT_RESET", "TYPE_AES_TRANSIENT_DESELECT", "TYPE_AES LENGTH_AES_128", "TYPE_AES LENGTH_AES_192", "TYPE_AES LENGTH_AES_256",
    "###RSA_PUBLIC_KEY###", "TYPE_RSA_PUBLIC LENGTH_RSA_512", "TYPE_RSA_PUBLIC LENGTH_RSA_736", "TYPE_RSA_PUBLIC LENGTH_RSA_768", "TYPE_RSA_PUBLIC LENGTH_RSA_896",
        "TYPE_RSA_PUBLIC LENGTH_RSA_1024", "TYPE_RSA_PUBLIC LENGTH_RSA_1280", "TYPE_RSA_PUBLIC LENGTH_RSA_1536", "TYPE_RSA_PUBLIC LENGTH_RSA_1984", "TYPE_RSA_PUBLIC LENGTH_RSA_2048",
    "###RSA_PRIVATE_KEY###", "TYPE_RSA_PRIVATE LENGTH_RSA_512", "TYPE_RSA_PRIVATE LENGTH_RSA_736", "TYPE_RSA_PRIVATE LENGTH_RSA_768", "TYPE_RSA_PRIVATE LENGTH_RSA_896",
        "TYPE_RSA_PRIVATE LENGTH_RSA_1024", "TYPE_RSA_PRIVATE LENGTH_RSA_1280", "TYPE_RSA_PRIVATE LENGTH_RSA_1536", "TYPE_RSA_PRIVATE LENGTH_RSA_1984", "TYPE_RSA_PRIVATE LENGTH_RSA_2048",
    "###RSA_CRT_PRIVATE_KEY###", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_512", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_736", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_768", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_896",
        "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1024", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1280", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1536", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1984", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_2048",
    "###DSA_PRIVATE_KEY###", "TYPE_DSA_PRIVATE LENGTH_DSA_512", "TYPE_DSA_PRIVATE LENGTH_DSA_768", "TYPE_DSA_PRIVATE LENGTH_DSA_1024", 
    "###DSA_PUBLIC_KEY###", "TYPE_DSA_PUBLIC LENGTH_DSA_512", "TYPE_DSA_PUBLIC LENGTH_DSA_768", "TYPE_DSA_PUBLIC LENGTH_DSA_1024", 
    "###EC_F2M_PRIVATE_KEY###", "TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_113", "TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_131", "TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_163", "TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_193",
    "###EC_FP_PRIVATE_KEY###", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_112", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_128", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_160", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_192",
    "###KOREAN_SEED_KEY###", "TYPE_KOREAN_SEED_TRANSIENT_RESET", "TYPE_KOREAN_SEED_TRANSIENT_DESELECT", "TYPE_KOREAN_SEED LENGTH_KOREAN_SEED_128", 
    "###HMAC_KEY###", "TYPE_HMAC_TRANSIENT_RESET", "TYPE_HMAC_TRANSIENT_DESELECT", "TYPE_HMAC LENGTH_HMAC_SHA_1_BLOCK_64", "TYPE_HMAC LENGTH_HMAC_SHA_256_BLOCK_64", "TYPE_HMAC LENGTH_HMAC_SHA_384_BLOCK_64", "TYPE_HMAC LENGTH_HMAC_SHA_512_BLOCK_64",
}; 

  //
  //Class javacard.security.KeyPair
  //
#define ALG_RSA                         1
#define ALG_RSA_CRT                     2
#define ALG_DSA                         3
#define ALG_EC_F2M                      4
#define ALG_EC_FP                       5

char* KEYPAIR_RSA_STR[] = {"javacard.security.KeyPair ALG_RSA on-card generation", 
    "ALG_RSA LENGTH_RSA_512", "ALG_RSA LENGTH_RSA_736", "ALG_RSA LENGTH_RSA_768", "ALG_RSA LENGTH_RSA_896",
    "ALG_RSA LENGTH_RSA_1024", "ALG_RSA LENGTH_RSA_1280", "ALG_RSA LENGTH_RSA_1536", "ALG_RSA LENGTH_RSA_1984", "ALG_RSA LENGTH_RSA_2048"
    };
    
char* KEYPAIR_RSACRT_STR[] = {"javacard.security.KeyPair ALG_RSA_CRT on-card generation", 
    "ALG_RSA_CRT LENGTH_RSA_512", "ALG_RSA_CRT LENGTH_RSA_736", "ALG_RSA_CRT LENGTH_RSA_768", "ALG_RSA_CRT LENGTH_RSA_896",
    "ALG_RSA_CRT LENGTH_RSA_1024", "ALG_RSA_CRT LENGTH_RSA_1280", "ALG_RSA_CRT LENGTH_RSA_1536", "ALG_RSA_CRT LENGTH_RSA_1984", "ALG_RSA_CRT LENGTH_RSA_2048"
};    
char* KEYPAIR_DSA_STR[] = {"javacard.security.KeyPair ALG_DSA on-card generation", 
    "ALG_DSA LENGTH_DSA_512", "ALG_DSA LENGTH_DSA_768", "ALG_DSA LENGTH_DSA_1024"
};
char* KEYPAIR_EC_F2M_STR[] = {"javacard.security.KeyPair ALG_EC_F2M on-card generation", 
    "ALG_EC_F2M LENGTH_EC_F2M_113", "ALG_EC_F2M LENGTH_EC_F2M_131", "ALG_EC_F2M LENGTH_EC_F2M_163", "ALG_EC_F2M LENGTH_EC_F2M_193"
};
char* KEYPAIR_EC_FP_STR[] = {"javacard.security.KeyPair ALG_EC_FP on-card generation", 
    "ALG_EC_FP LENGTH_EC_FP_112", "ALG_EC_FP LENGTH_EC_FP_128", "ALG_EC_FP LENGTH_EC_FP_160", "ALG_EC_FP LENGTH_EC_FP_192"
};
    
#define CLASS_KEYPAIR_RSA_P2            9
#define CLASS_KEYPAIR_RSACRT_P2         9
#define CLASS_KEYPAIR_DSA_P2            3
#define CLASS_KEYPAIR_EC_F2M_P2         4
#define CLASS_KEYPAIR_EC_FP_P2          4
    
    
  //Class javacard.security.MessageDigest
#define ALG_SHA                         1
#define ALG_MD5                         2
#define ALG_RIPEMD160                   3
  // JC2.2.2
#define ALG_SHA_256                     4
#define ALG_SHA_384                     5
#define ALG_SHA_512                     6

char* MESSAGEDIGEST_STR[] = {"javacard.security.MessageDigest", "ALG_SHA", "ALG_MD5", "ALG_RIPEMD160", 
    "ALG_SHA_256", "ALG_SHA_384", "ALG_SHA_512"
}; 


  //Class javacard.security.RandomData
#define ALG_PSEUDO_RANDOM               1
#define ALG_SECURE_RANDOM               2

char* RANDOMDATA_STR[] = {"javacard.security.RandomData", "ALG_PSEUDO_RANDOM", "ALG_SECURE_RANDOM"}; 

  // Class javacard.security.Checksum
#define ALG_ISO3309_CRC16               1
#define ALG_ISO3309_CRC32               2

char* CHECKSUM_STR[] = {"javacard.security.Checksum", "ALG_ISO3309_CRC16", "ALG_ISO3309_CRC32"}; 



// The one and only application object

CWinApp theApp;

using namespace std;

int GetJCSystemInfo(CPCSCMngr* pScManager, CString* pValue, CFile* pFile) {
    int         status = STAT_OK;
    CARDAPDU    apdu;
    clock_t     elapsedCard;
    
	// Prepare test memory apdu
    apdu.cla = 0xB0;
    apdu.ins = 0x73;
    apdu.p1 = 0x00;	
    apdu.p2 = 0x00;
    apdu.le = 240;
    apdu.lc = 0x00;

    elapsedCard = -clock();
                
    if ((status = pScManager->ExchangeAPDU(&apdu)) == STAT_OK) {
        // SAVE TIME OF CARD RESPONSE
        elapsedCard += clock();
        CString elTimeStr = "";
        // OUTPUT REQUIRED TIME WHEN PARTITIONED CHECk WAS PERFORMED (NOTMULTIPLE ALGORITHMS IN SINGLE RUN)
        elTimeStr.Format("%f", (double) elapsedCard / (float) CLOCKS_PER_SEC);

		int versionMajor = apdu.DataOut[0];
		int versionMinor = apdu.DataOut[1];
		int bDeletionSupported = apdu.DataOut[2];
		int eepromSize = (apdu.DataOut[3] << 8) + (apdu.DataOut[4] & 0xff);
		int ramResetSize = (apdu.DataOut[5] << 8) + (apdu.DataOut[6] & 0xff);
		int ramDeselectSize = (apdu.DataOut[7] << 8) + (apdu.DataOut[8] & 0xff);

		CString message;
        message.Format("\r\nJCSystem.getVersion()[Major.Minor]; %d.%d;", versionMajor, versionMinor); 
        cout << message;
        pFile->Write((LPCTSTR) message, message.GetLength());
        *pValue += message;
		message.Format("\r\nJCSystem.isObjectDeletionSupported; %s;", bDeletionSupported ? "yes" : "no"); 
        cout << message;
        pFile->Write((LPCTSTR) message, message.GetLength());
        *pValue += message;


		message.Format("\r\nJCSystem.MEMORY_TYPE_PERSISTENT; %s%d B;", (eepromSize == 32767) ? "more then " : "", eepromSize); 
        cout << message;
        pFile->Write((LPCTSTR) message, message.GetLength());
        *pValue += message;
        message.Format("\r\nJCSystem.MEMORY_TYPE_TRANSIENT_RESET; %s%d B;", (ramResetSize == 32767) ? "more then " : "", ramResetSize); 
        cout << message;
        pFile->Write((LPCTSTR) message, message.GetLength());
        *pValue += message;
        message.Format("\r\nJCSystem.MEMORY_TYPE_TRANSIENT_DESELECT; %s%d B;", (ramDeselectSize == 32767) ? "more then " : "", ramDeselectSize); 
        cout << message;
        pFile->Write((LPCTSTR) message, message.GetLength());
        *pValue += message;

    }
	
	return status;
}

int GetSupportedAndParse(CPCSCMngr* pScManager, BYTE algClass, char** algNames, CString* pValue, CFile* pFile, BYTE algPartP2 = 0) {
    int         status = STAT_OK;
    CARDAPDU    apdu;
    BYTE        suppAlg[MAX_SUPP_ALG];
    clock_t     elapsedCard;
    BOOL        bNamePrinted = FALSE;
    
    // CLEAR ARRAY FOR SUPPORTED ALGORITHMS
    memset(suppAlg, SUPP_ALG_UNTOUCHED, MAX_SUPP_ALG);

    // PREPARE SEPARATE APDU FOR EACH SIGNALIZED P2 VALUE
    // IF P2 == 0 THEN ALL ALGORITHMS WITHIN GIVEN algClass WILL BE CHECK In SINGLE APDU
    // OTHERWISE, MULTIPLE APDU WILL BE ISSUED
    int p2Start = 0;
    if (algPartP2 == 0) p2Start = 0;
    else p2Start = 1; 
    for (int p2 = p2Start; p2 <= algPartP2; p2++) {
        apdu.cla = 0xB0;
        apdu.ins = 0x70;
        apdu.p1 = algClass;
        apdu.p2 = (BYTE) p2;
        apdu.le = 240;
        apdu.lc = 0x00;

        elapsedCard = -clock();
                    
        if ((status = pScManager->ExchangeAPDU(&apdu)) == STAT_OK) {
            // SAVE TIME OF CARD RESPONSE
            elapsedCard += clock();
            CString elTimeStr = "";
            // OUTPUT REQUIRED TIME WHEN PARTITIONED CHECk WAS PERFORMED (NOTMULTIPLE ALGORITHMS IN SINGLE RUN)
            if (algPartP2 > 0) elTimeStr.Format("%f", (double) elapsedCard / (float) CLOCKS_PER_SEC);

            // OK, STORE RESPONSE TO suppAlg ARRAY
            if (apdu.DataOut[0] == algClass) {
                
                // PRINT algClass NAME ONLY ONES
                if (!bNamePrinted) {
                    CString message;
                    message += "\r\n"; message += algNames[0]; message += ";\r\n";
                    cout << message;
                    pFile->Write((LPCTSTR) message, message.GetLength());
                    *pValue = message;
                    
                    bNamePrinted = TRUE;
                }
            
                for (int i = 1; i < apdu.le; i++) {
                    // ONLY FILLED RESPONSES ARE STORED
                    if (apdu.DataOut[i] != SUPP_ALG_UNTOUCHED) {
                        suppAlg[i] = apdu.DataOut[i];    

                        // ALG NAME
                        CString algState = "";
                        switch (suppAlg[i]) {
                            case 0: {
                                algState += algNames[i]; algState += ";"; algState += "no;"; algState += "\r\n";
                                break;
                            }
                            case 1: {
                                algState += algNames[i]; algState += ";"; algState += "yes;"; algState += elTimeStr; algState += "\r\n";
                                break;
                            }
                            case 2: {
                                algState += algNames[i]; algState += ";"; algState += "error;"; algState += "\r\n";
                                break;
                            }
                            case 0x6f: {
                                algState += algNames[i]; algState += ";"; algState += "maybe;"; algState += "\r\n";
                                break;
                            }
                            default: {
                                // OTHER VALUE, IGNORE 
                                break;
                            }
                        }
                        
                        if (algState != "") {
                            *pValue += algState;
                            cout << algState;
                            pFile->Write((LPCTSTR) algState, algState.GetLength());
                        }
                    }
                }
            }
            else status = STAT_DATA_CORRUPTED;
        }
    }
    
    return status;
}


int TestAvailableRAMMemory(CPCSCMngr* pScManager, CString* pValue, CFile* pFile, BYTE algPartP2 = 0) {
    int         status = STAT_OK;
    CARDAPDU    apdu;
    clock_t     elapsedCard;
    
	// Prepare test memory apdu
    apdu.cla = 0xB0;
    apdu.ins = 0x71;
    apdu.p1 = 0x00;	
    apdu.p2 = 0x00;
    apdu.le = 240;
    apdu.lc = 0x00;

    elapsedCard = -clock();
                
    if ((status = pScManager->ExchangeAPDU(&apdu)) == STAT_OK) {
        // SAVE TIME OF CARD RESPONSE
        elapsedCard += clock();
        CString elTimeStr = "";
        // OUTPUT REQUIRED TIME WHEN PARTITIONED CHECk WAS PERFORMED (NOTMULTIPLE ALGORITHMS IN SINGLE RUN)
        elTimeStr.Format("%f", (double) elapsedCard / (float) CLOCKS_PER_SEC);

        CString message;
        message += "\r\nAvailable RAM memory;"; 
        cout << message;
        pFile->Write((LPCTSTR) message, message.GetLength());
        *pValue = message;

		int ramSize = (apdu.DataOut[0] << 8) + (apdu.DataOut[1] & 0xff);
        message.Format("%d B;", ramSize); 
        cout << message;
        pFile->Write((LPCTSTR) message, message.GetLength());
        *pValue += message;
    }

    return status;
}


int TestAvailableEEPROMMemory(CPCSCMngr* pScManager, CString* pValue, CFile* pFile, BYTE algPartP2 = 0) {
    int         status = STAT_OK;
    CARDAPDU    apdu;
    clock_t     elapsedCard;
    
	// Prepare test memory apdu
    apdu.cla = 0xB0;
    apdu.ins = 0x71;
    apdu.p1 = 0x01;	
    apdu.p2 = 0x00;
    apdu.le = 240;
    apdu.lc = 0x00;

    elapsedCard = -clock();
                
	if (status == STAT_OK) {
		apdu.p1 = 0x01;	// get also EEPROM size	
		if ((status = pScManager->ExchangeAPDU(&apdu)) == STAT_OK) {
			// SAVE TIME OF CARD RESPONSE
			elapsedCard += clock();
			CString elTimeStr = "";
			// OUTPUT REQUIRED TIME WHEN PARTITIONED CHECk WAS PERFORMED (NOTMULTIPLE ALGORITHMS IN SINGLE RUN)
			elTimeStr.Format("%f", (double) elapsedCard / (float) CLOCKS_PER_SEC);

			CString message;
			message += "\r\nAvailable EEPROM memory;"; 
			cout << message;
			pFile->Write((LPCTSTR) message, message.GetLength());
			*pValue += message;

			int eepromSize = (apdu.DataOut[2] << 8) + (apdu.DataOut[3] & 0xff);
			eepromSize += (apdu.DataOut[4] << 8) + (apdu.DataOut[5] & 0xff);
			eepromSize += (apdu.DataOut[6] << 8) + (apdu.DataOut[7] & 0xff);
			eepromSize += (apdu.DataOut[8] << 8) + (apdu.DataOut[9] & 0xff);
			eepromSize += (apdu.DataOut[10] << 8) + (apdu.DataOut[11] & 0xff);
			eepromSize += (apdu.DataOut[12] << 8) + (apdu.DataOut[13] & 0xff);
			eepromSize += (apdu.DataOut[14] << 8) + (apdu.DataOut[15] & 0xff);
			eepromSize += (apdu.DataOut[16] << 8) + (apdu.DataOut[17] & 0xff);
			message.Format("%d B;\r\n", eepromSize); 
			cout << message;
			pFile->Write((LPCTSTR) message, message.GetLength());
			*pValue += message;
		}
	}

    return status;
}

int TestAction(CString actionName, CARDAPDU& apdu, CPCSCMngr* pScManager, CString* pValue, CFile* pFile) {
	int		status = STAT_OK;

    clock_t     elapsedCard = 0;
	elapsedCard -= clock();

	CString message;
	message.Format("\r\n%s;", actionName); 
	cout << message;
	pFile->Write((LPCTSTR) message, message.GetLength());
	*pValue += message;
	if ((status = pScManager->ExchangeAPDU(&apdu)) == STAT_OK) {
		// SAVE TIME OF CARD RESPONSE
		elapsedCard += clock();
		CString elTimeStr = "";
		// OUTPUT REQUIRED TIME WHEN PARTITIONED CHECk WAS PERFORMED (NOTMULTIPLE ALGORITHMS IN SINGLE RUN)
		elTimeStr.Format("%f", (double) elapsedCard / (float) CLOCKS_PER_SEC);

		message.Format("yes;%s sec;", elTimeStr); 
		cout << message;
		pFile->Write((LPCTSTR) message, message.GetLength());
		*pValue += message;
	}
	else {
		message.Format("no;"); 
		cout << message;
		pFile->Write((LPCTSTR) message, message.GetLength());
		*pValue += message;
	}
	return status;
}

int TestVariableRSAPublicExponentSupport(CPCSCMngr* pScManager, CString* pValue, CFile* pFile, BYTE algPartP2 = 0) {
    int         status = STAT_OK;
    CARDAPDU    apdu;
    
	// Prepare test memory apdu
    apdu.cla = 0xB0;
    apdu.ins = 0x72;
    apdu.p1 = 0x00;	
    apdu.p2 = 0x00;
    apdu.le = 240;
    apdu.lc = 0x00;

	CString message;
	message.Format("\r\nSupport for variable public exponent for RSA 1024. If supported, user-defined fast modular exponentiation can be executed on the smart card via cryptographic coprocessor. This is very specific feature and you will probably not need it;"); 
	cout << message;
	pFile->Write((LPCTSTR) message, message.GetLength());
	*pValue += message;

	// Allocate RSA 1024 objects (RSAPublicKey and ALG_RSA_NOPAD cipher)
	apdu.p1 = 0x01;	
	TestAction("Allocate RSA 1024 objects", apdu, pScManager,pValue,pFile);
    // Try to set random modulus
	apdu.p1 = 0x02;	
	TestAction("Set random modulus", apdu, pScManager,pValue,pFile);
    // Try to set random exponent
	apdu.p1 = 0x03;	
	TestAction("Set random public exponent", apdu, pScManager,pValue,pFile);
	// Try to initialize cipher with public key with random exponent
	apdu.p1 = 0x04;	
	TestAction("Initialize cipher with public key with random exponent", apdu, pScManager,pValue,pFile);
	// Try to encrypt block of data
	apdu.p1 = 0x05;	
	TestAction("Use random public exponent", apdu, pScManager,pValue,pFile);

    return status;
}

int _tmain(int argc, TCHAR* argv[], TCHAR* envp[])
{
	int nRetCode = 0;
    CPCSCMngr   scManager;
    char*       readers = NULL;
    DWORD       readersLen = 1000;  
    CARDAPDU    apdu;
    CString     value;
    CFile       file;
	int			status = STAT_OK;

	// initialize MFC and print and error on failure
	if (!AfxWinInit(::GetModuleHandle(NULL), NULL, ::GetCommandLine(), 0))
	{
		// TODO: change error code to suit your needs
		cerr << _T("Fatal Error: MFC initialization failed") << endl;
		nRetCode = 1;
	}
	else
	{
        readers = new char[readersLen];
        memset(readers, 0, readersLen);
        list<CString>       readersList;
        list<CString>::iterator iter;
        int                 readerCount = 1;

		cout << "#########################" << endl;
        if ((status = scManager.GetAvailableReaders(readers, &readersLen)) == STAT_OK) {
            char*       reader;
            int         pos;
    		// PARSE NULL SEPARATED ARRAY
            cout << endl << "Available readers:" << endl;
            pos = 0;
            while ((DWORD) pos < readersLen) {
                reader = &(readers[pos]);
                if (strlen(reader) == 0) {
                    pos++;
                }
                else {
                    // DISPLAY READER
                    cout << readerCount << ". " << reader << endl;

                    readersList.push_back(reader);

                    pos = pos + (DWORD) strlen(reader) + 1;
                    readerCount++; 
                }

            }
        }
        else {
            cerr << "Fail to obtain available readers" << endl;
        }

		if (status == STAT_OK) {
			// CONNECT TO FIRST AVAILABLE READER AND EXCHANGE APDU COMMAND
			int     targetReader;
			cout << endl << "Select index of target reader: ";
			cin >> targetReader;

			CString  reader = "";
			if (targetReader > 0 && targetReader < readerCount) {  
				// GET target-TH READER
				iter = readersList.begin();
				for (int i = 1; i < targetReader; i++) iter++;
				reader = *iter;
			}
	        
			// OPEN SESSION    
			if (scManager.OpenSession(reader) == STAT_OK) {
				CString atr; 
				CString reader;
				scManager.GetActualReader(&reader, &atr);
	            
				CString fileName;
				CString message;
				fileName.Format("AlgTest_%s.csv", atr);
				fileName.Replace(":", "");

				file.Open(fileName, CFile::modeReadWrite | CFile::modeCreate);

				message.Format("Used reader;%s\n", reader);
				cout << message;
				file.Write((LPCTSTR) message, message.GetLength());
				message.Format("Card ATR;%s\n", atr);
				cout << message;
				file.Write((LPCTSTR) message, message.GetLength());
	            
	            
	            
				// SELECT ALGTEST 
				apdu.cla = 0x00;
				apdu.ins = 0xA4;
				apdu.p1 = 0x04;
				apdu.p2 = 0x00;
				apdu.le = 0x00;
				apdu.lc = ALGTEST_AID_LEN;
				memcpy(apdu.DataIn, ALGTEST_AID, ALGTEST_AID_LEN);
				if (scManager.ExchangeAPDU(&apdu) == STAT_OK) {
					cout << endl << endl << "#########################";
					cout << endl << "JCSystem information" << endl;
					value = "";
					if (GetJCSystemInfo(&scManager, &value, &file) == STAT_OK) {
					}
					else cerr << "\nERROR: GetJCSystemInfo fail\n";
					file.Flush();


					cout << endl << endl << "#########################";
					cout << endl << "Q: Do you like to test supported algorithms?" << endl;
					cout << "Type 1 for yes, 0 for no: ";	
					int	answ = 0;
					cin >> answ;
					if (answ == 1) {

						cout << "#########################" << endl;
		
						// Class javacardx.crypto.Cipher
						value = "";
						if (GetSupportedAndParse(&scManager, CLASS_CIPHER, CIPHER_STR, &value, &file) == STAT_OK) {
						}
						else cerr << "\nERROR: javacardx.crypto.Cipher fail\n";
						file.Flush();
		                
						// Class javacard.security.Signature
						value = "";
						if (GetSupportedAndParse(&scManager, CLASS_SIGNATURE, SIGNATURE_STR, &value, &file) == STAT_OK) {
						}
						else cerr << "\nERROR: jav1acard.security.Signature fail\n";
						file.Flush();
		                
						// Class javacard.security.MessageDigest
						value = "";
						if (GetSupportedAndParse(&scManager, CLASS_MESSAGEDIGEST, MESSAGEDIGEST_STR, &value, &file) == STAT_OK) {
						}
						else cerr << "\nERROR: javacard.security.MessageDigest fail\n";
						file.Flush();
		                
						// Class javacard.security.RandomData
						value = "";
						if (GetSupportedAndParse(&scManager, CLASS_RANDOMDATA, RANDOMDATA_STR, &value, &file) == STAT_OK) {
						}
						else cerr << "\nERROR: javacard.security.RandomData fail\n";
						file.Flush();

						// Class javacard.security.KeyBuilder
						value = "";
						if (GetSupportedAndParse(&scManager, CLASS_KEYBUILDER, KEYBUILDER_STR, &value, &file) == STAT_OK) {
						}
						else cerr << "\nERROR: javacard.security.KeyBuilder fail\n";
						file.Flush();
		                
						// Class javacard.security.KeyPair RSA
						value = "";
						if (GetSupportedAndParse(&scManager, CLASS_KEYPAIR_RSA, KEYPAIR_RSA_STR, &value, &file, CLASS_KEYPAIR_RSA_P2) == STAT_OK) {
						}
						else cerr << "\nERROR: javacard.security.KeyPair RSA fail\n";
						file.Flush();
						// Class javacard.security.KeyPair RSA_CRT
						value = "";
						if (GetSupportedAndParse(&scManager, CLASS_KEYPAIR_RSA_CRT, KEYPAIR_RSACRT_STR, &value, &file, CLASS_KEYPAIR_RSACRT_P2) == STAT_OK) {
						}
						else cerr << "\nERROR: javacard.security.KeyPair RSA_CRT fail\n";
						file.Flush();
						// Class javacard.security.KeyPair DSA
						value = "";
						if (GetSupportedAndParse(&scManager, CLASS_KEYPAIR_DSA, KEYPAIR_DSA_STR, &value, &file, CLASS_KEYPAIR_DSA_P2) == STAT_OK) {
						}
						else cerr << "\nERROR: javacard.security.KeyPair DSA fail\n";
						file.Flush();
						// Class javacard.security.KeyPair EC_F2M
						value = "";
						if (GetSupportedAndParse(&scManager, CLASS_KEYPAIR_EC_F2M, KEYPAIR_EC_F2M_STR, &value, &file, CLASS_KEYPAIR_EC_F2M_P2) == STAT_OK) {
						}
						else cerr << "\nERROR: javacard.security.KeyPair EC_F2M fail\n";
						file.Flush();
						// Class javacard.security.KeyPair EC_FP
						value = "";
						if (GetSupportedAndParse(&scManager, CLASS_KEYPAIR_EC_FP, KEYPAIR_EC_FP_STR, &value, &file, CLASS_KEYPAIR_EC_FP_P2) == STAT_OK) {
						}
						else cerr << "\nERROR: javacard.security.KeyPair EC_FP fail\n";
						file.Flush();
		                
						// Class javacard.security.KeyAgreement
						value = "";
						if (GetSupportedAndParse(&scManager, CLASS_KEYAGREEMENT, KEYAGREEMENT_STR, &value, &file) == STAT_OK) {
						}
						else cerr << "\nERROR: javacard.security.KeyAgreement fail\n";
						file.Flush();

						// Class javacard.security.Checksum
						value = "";
						if (GetSupportedAndParse(&scManager, CLASS_CHECKSUM, CHECKSUM_STR, &value, &file) == STAT_OK) {
						}
						else cerr << "\nERROR: javacard.security.Checksum fail\n";
						file.Flush();
					}

					cout << endl << endl << "#########################";
					cout << endl << "Q: Do you like to test support for variable RSA public exponent?" << endl;
					cout << "Type 1 for yes, 0 for no: ";	
					cin >> answ;
					if (answ == 1) {
						// Variable public exponent
						value = "";
						if (TestVariableRSAPublicExponentSupport(&scManager, &value, &file) == STAT_OK) {
						}
						else cerr << "\nERROR: Test variable public exponnet support fail\n";
						file.Flush();
					}

					cout << endl << endl << "#########################";
					cout << endl << "Q: Do you like to test RAM memory available for allocation?" << endl;
					cout << "STRONG WARNING: There is possibility that your card become unresponsive after this test. All cards I tested required just to delete AlgTest applet to reclaim allocated memory. But it might be possible that your card will be unusuable after this test." << endl;
					cout << "WARNING: Your card should be free from other applets - otherwise memory already claimed by existing applets will not be included. Value is approximate +- 100B" << endl;
					cout << "Type 1 for yes, 0 for no: ";	
					cin >> answ;
					if (answ == 1) {
						// Available memory
						value = "";
						if (TestAvailableRAMMemory(&scManager, &value, &file) == STAT_OK) {
						}
						else cerr << "\nERROR: Get available RAM memory fail\n";
						file.Flush();
					}

					cout << endl << endl << "#########################";
					cout << endl << "Q: Do you like to test EEPROM memory available for allocation?" << endl;
					cout << "STRONG WARNING: There is possibility that your card become unresponsive after this test. All cards I tested required just to delete AlgTest applet to reclaim allocated memory. But it might be possible that your card will be unusuable after this test." << endl;
					cout << "WARNING: Your card should be free from other applets - otherwise memory already claimed by existing applets will not be included. Value is approximate +- 5KB" << endl;
					cout << "Type 1 for yes, 0 for no: ";	
					cin >> answ;
					if (answ == 1) {
						// Available memory
						value = "";
						if (TestAvailableEEPROMMemory(&scManager, &value, &file) == STAT_OK) {
						}
						else cerr << "\nERROR: Get available EEPROM memory fail\n";
						file.Flush();
					}
				}
				else cerr << "\nERROR: fail to select AlgTest applet";
	            
				file.Close();

				scManager.CloseSession();
			}
			else cerr << "\nERROR: fail to open session on target reader (used by other application?)\n";
		}

        if (readers) delete[] readers;
	}


	return nRetCode;
}
