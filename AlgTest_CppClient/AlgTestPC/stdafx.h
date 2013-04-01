// stdafx.h : include file for standard system include files,
// or project specific include files that are used frequently, but
// are changed infrequently
//

#pragma once

// Modify the following defines if you have to target a platform prior to the ones specified below.
// Refer to MSDN for the latest info on corresponding values for different platforms.
#ifndef WINVER				// Allow use of features specific to Windows XP or later.
#define WINVER 0x0501		// Change this to the appropriate value to target other versions of Windows.
#endif

#ifndef _WIN32_WINNT		// Allow use of features specific to Windows XP or later.                   
#define _WIN32_WINNT 0x0501	// Change this to the appropriate value to target other versions of Windows.
#endif						

#ifndef _WIN32_WINDOWS		// Allow use of features specific to Windows 98 or later.
#define _WIN32_WINDOWS 0x0410 // Change this to the appropriate value to target Windows Me or later.
#endif

#ifndef _WIN32_IE			// Allow use of features specific to IE 6.0 or later.
#define _WIN32_IE 0x0600	// Change this to the appropriate value to target other versions of IE.
#endif

#define WIN32_LEAN_AND_MEAN		// Exclude rarely-used stuff from Windows headers
#include <stdio.h>
#include <tchar.h>
#define _ATL_CSTRING_EXPLICIT_CONSTRUCTORS	// some CString constructors will be explicit

#ifndef VC_EXTRALEAN
#define VC_EXTRALEAN		// Exclude rarely-used stuff from Windows headers
#endif

#include <afx.h>
#include <afxwin.h>         // MFC core and standard components
#include <afxext.h>         // MFC extensions
#ifndef _AFX_NO_OLE_SUPPORT
#include <afxdtctl.h>		// MFC support for Internet Explorer 4 Common Controls
#endif
#ifndef _AFX_NO_AFXCMN_SUPPORT
#include <afxcmn.h>			// MFC support for Windows Common Controls
#endif // _AFX_NO_AFXCMN_SUPPORT

#include <iostream>


enum APDU_TYPE_CASE {
	CASE1,	// 4 bytes total, header (CLA, INS, P1, P2), no LC, no LE
	CASE2,  // 5 bytes total, header (CLA, INS, P1, P2), no LC, LE
	CASE3,  // 5 + LC bytes total, header (CLA, INS, P1, P2), LC, data in (LC bytes), no LE
	CASE4   // 6 + LC bytes total, header (CLA, INS, P1, P2), LC, data in (LC bytes), LE
};

typedef struct CARDAPDU {
	unsigned char   cla;
	unsigned char   ins;
	unsigned char   p1;
	unsigned char   p2;
	unsigned char   lc;
	unsigned char   le;
	unsigned char   DataIn[256];
	unsigned char   DataOut[256];
	unsigned short  sw;
	APDU_TYPE_CASE  apduType;

	CARDAPDU() {
		clear();
	}
	void clear() {
		cla = 0;
		ins = 0;
		p1 = 0;
		p2 = 0;
		lc = 0;
		le = 0;
		memset(DataIn, 0, sizeof(DataIn));
		memset(DataOut, 0, sizeof(DataOut));
		sw = 0x9000;
		apduType = CASE3;
	}

} CARDAPDU;


#ifndef HIGHBYTE
    #define HIGHBYTE(x)  x >> 8 
#endif

#ifndef LOWBYTE
    #define LOWBYTE(x)   x & 0xFF 
#endif

#define STAT_OK                                 0       // OK
#define STAT_SESSION_NOT_OPEN                   2       // SESSION IS NOT OPEN, 
#define STAT_DATA_CORRUPTED                     10        
#define STAT_DATA_INCORRECT_LENGTH              11      // LENGTH OF GIVEN DATA IS INCORRECT
#define STAT_SCARD_ERROR                        34      // SCARD FUNCTIONS ERROR 
#define STAT_UNKNOWN_SCARD_PROTOCOL             36
#define STAT_VALUE_NOT_READ                     104

    // ISO7816 ERRORS
#define SW_FILE_FULL                        0x6A84
#define SW_UNKNOWN  				        0x6F00
#define SW_CLA_NOT_SUPPORTED 		        0x6E00
#define SW_INS_NOT_SUPPORTED 		        0x6D00
#define SW_CORRECT_LENGTH_00 		        0x6C00
#define SW_WRONG_P1P2   			        0x6B00
#define SW_LC_INCONSISTENT_WITH_P1P2        0x6A87
#define SW_INCORRECT_P1P2			        0x6A86
#define SW_RECORD_NOT_FOUND 		        0x6A83	
#define SW_FILE_NOT_FOUND 			        0x6A82
#define SW_FUNC_NOT_SUPPORTED		        0x6A81
#define SW_WRONG_DATA   			        0x6A80
#define SW_APPLET_SELECT_FAILED 	        0x6999
#define SW_COMMAND_NOT_ALLOWED 		        0x6986
#define SW_CONDITIONS_NOT_SATISFIED         0x6985
#define SW_DATA_INVALID 			        0x6984
#define SW_FILE_INVALID 			        0x6983
#define SW_SECURITY_STATUS_NOT_SATISFIED    0x6982
#define SW_WRONG_LENGTH         			0x6700
#define SW_BYTES_REMAINING_00       		0x6100
#define SW_NO_ERROR             			0x9000	
     
    // JCSTATUS (used in OpenPlatform cards)
#define SW_JCDOMAIN_ALGORITHM_NOT_SUPPORTED     0x9484
#define SW_JCDOMAIN_APPLET_INVALIDATED          0x6283
#define SW_JCDOMAIN_AUTHENTICATION_FAILED       0x6300
#define SW_JCDOMAIN_AUTHORIZATION_FAILED        0x9482
#define SW_JCDOMAIN_CHECKSUM_FAILED             0x9584
#define SW_JCDOMAIN_DECRYPTION_FAILED           0x9583
#define SW_JCDOMAIN_INSTALLATION_FAILED         0x9585
#define SW_JCDOMAIN_INVALID_STATE               0x9481
#define SW_JCDOMAIN_NO_SPECIFIC_DIAGNOSIS       0x6400
#define SW_JCDOMAIN_REFERENCE_DATA_NOT_FOUND    0x6A88
#define SW_JCDOMAIN_REGISTRATION_FAILED         0x9586
#define SW_JCDOMAIN_SIGNATURE_CHECK_FAILED      0x9582
#define SW_JCDOMAIN_SM_INCORRECT                0x6988
#define SW_JCDOMAIN_SM_MISSING                  0x6987

