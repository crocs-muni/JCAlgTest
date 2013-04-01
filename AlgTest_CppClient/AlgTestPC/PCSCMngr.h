#ifndef PCSCMANAGER_H
#define PCSCMANAGER_H

#include <afxmt.h>
#include "stdafx.h"
#include <Winscard.h>

class CPCSCMngr {
    //
    // ATTRIBUTES
    //
private:
    SCARDCONTEXT     m_cardContext;   

    LONG             m_lastSCardError;   

public:
    SCARDHANDLE      m_hCard;                 // ACTUAL OPENED SESSION HANDLE
    DWORD            m_scProtocol;              

    //
    // METHODS
    //
public:
    CPCSCMngr();
    ~CPCSCMngr();
    
    // OPEN SESSION TO GIVEN SLOT AND CARD IDENTIFICATION
    // SLOT ID UNUSED AND SHOUL BE SET TO 0
    int OpenSession(const char* targetCard);
    
    // CLOSE ACTUAL OPENED SESSION
    // IF NO SESSION IS OPENED, SUCCESS IS RETURNED
    int CloseSession();
    
    // GET AVAILABLE READERS
    int GetAvailableReaders(char* readers, DWORD *pReadersLen);

    // EXCHANGE APDU WITH CARD AND RECEIVE RESPONSE
    int ExchangeAPDU(CARDAPDU* pAPDU);    

    // SELECT JAVA CARD APPLET
    int SelectApplet(BYTE* pAppletAID, BYTE appletAIDLength);

    // GET ACTUALLY SELECTED READER
    int GetSCStatusString(CString* pSCStatus);
    int GetActualReader(CString* pReader, CString* pATR = NULL);

	// RESET CARD
	int ResetCard();

    inline LONG GetLastSCardError() { return m_lastSCardError; }

	static int PrintAPDU(CARDAPDU* pAPDU, CString* pString, BOOL toSendAPDU = TRUE, BOOL bIncludeStatus = TRUE);
	static int BYTE_ConvertFromArrayToHexString(BYTE* pArray, DWORD pbArrayLen, CString* pHexaString);

protected:
    int TranslateSCardError(LONG scardStatus);
    int TranslateISO7816Error(int iso7816Error);
    
    int TransmitAPDU(CARDAPDU* pAPDU, int dataInLenForced = 0);
    int TransmitAPDUByCase(CARDAPDU* pAPDU);

    static char* card_state(DWORD state);
    static char* card_protocol(DWORD protocol);
    static char* card_atr(BYTE *atr, DWORD a_len);
    static char* historical(BYTE *atr,int pointer,int number_of_historical);

};

#endif