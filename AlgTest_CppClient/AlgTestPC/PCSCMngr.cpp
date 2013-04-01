/*  
    Copyright (c) 2004-2007  Petr Svenda <petr@svenda.com>

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
#include "PCSCMngr.h"


//
//  PUBLIC
//
CPCSCMngr::CPCSCMngr() {
    m_cardContext = NULL;
    m_hCard = NULL;
    m_lastSCardError = SCARD_S_SUCCESS;
    m_scProtocol = 0;
}

CPCSCMngr::~CPCSCMngr() {
    if (m_hCard || m_cardContext) CloseSession();
}

int CPCSCMngr::OpenSession(const char* targetCard) {
	int             status = STAT_OK;

    // CLOSE PREVIOUS SESSION, 
    if (m_hCard) CloseSession();


    // OPEN NEW SESSION
    if (status == STAT_OK) {
        status = TranslateSCardError(SCardEstablishContext(SCARD_SCOPE_USER,0,0,&m_cardContext));
    }

    // DISPLAY SELECT SMARTCARD DIALOG
/*
    if (status == STAT_OK) {
        memset( &dlgStruct, 0, sizeof (dlgStruct) ); 
        dlgStruct.dwStructSize = sizeof (dlgStruct);
        dlgStruct.hSCardContext = m_cardContext;
        dlgStruct.dwFlags = SC_DLG_FORCE_UI;
        dlgStruct.lpstrRdr = szReader;
        dlgStruct.nMaxRdr = 256;
        dlgStruct.lpstrCard = szCard;
        dlgStruct.nMaxCard = 256;
        dlgStruct.lpstrTitle = "Select target card";
        dlgStruct.lpstrCardNames = "";
        dlgStruct.nMaxCardNames = 20;

        // Display the select card dialog.
        status = TranslateSCardError(GetOpenCardName(&dlgStruct));
    }
/**/
    // CONNECT TO CARD
    if (status == STAT_OK) {
        status = TranslateSCardError(SCardConnect(m_cardContext, targetCard, SCARD_SHARE_EXCLUSIVE,SCARD_PROTOCOL_T0|SCARD_PROTOCOL_T1, &m_hCard, &m_scProtocol));
    }


    return status;
}

int CPCSCMngr::CloseSession() {
    int     status = STAT_OK;

    // CLOSE ACTUAL SESION
   if (m_hCard) {
    	SCardDisconnect(m_hCard, SCARD_LEAVE_CARD);
        m_hCard = NULL;
    }
    if (m_cardContext) {
    	SCardReleaseContext(m_cardContext);
        m_cardContext = NULL;
    }
    return status;
}

int CPCSCMngr::GetAvailableReaders(char* pReaders, DWORD *pReadersLen) {
    int             status = STAT_OK;
    LONG            retStat = SCARD_S_SUCCESS;
	SCARDCONTEXT    sc;
    DWORD           len = SCARD_AUTOALLOCATE;
    char*           readers = NULL;


    if ((retStat = SCardEstablishContext(SCARD_SCOPE_USER,0,0,&sc)) == SCARD_S_SUCCESS) {		
	    if ((retStat = SCardListReaders(sc, NULL, (char *)&readers, &len)) == SCARD_S_SUCCESS) {
            // OK, READERS OBTAINED
            if (*pReadersLen >= len) {
                memcpy(pReaders, readers, len);
            }
            else {
                // NOT ENOUGHT MEMORY
                *pReadersLen = len;
                status = STAT_DATA_INCORRECT_LENGTH;
            } 

            SCardFreeMemory(sc, readers);
        }
        else {
            m_lastSCardError = retStat;
            status = STAT_VALUE_NOT_READ;
        }
	}

    SCardReleaseContext(sc);

    if (retStat != SCARD_S_SUCCESS) status = STAT_SCARD_ERROR;
    return status;
}

int CPCSCMngr::ExchangeAPDU(CARDAPDU* pAPDU) {
    int         status = STAT_OK;

    if (m_cardContext && m_hCard) {
        
        // CLEAR SOFTWARE RETURN STATUS
        pAPDU->sw = SW_NO_ERROR;

        // SEND APDU
        //if ((status = TransmitAPDU(pAPDU, dataInLenForced)) == STAT_OK) {
        if ((status = TransmitAPDUByCase(pAPDU)) == STAT_OK) {
	        // OK
    
            // CHECK FOR SOFTWARE ERROR
            if (pAPDU->sw == SW_NO_ERROR) {
                // NO SOFWARE ERROR
            }            
            else {
                // CHECK FOR 'RESPONSE DATA AVAILABLE' STATUS
                if ((pAPDU->sw & 0xFF00) == SW_BYTES_REMAINING_00) {
                    if (pAPDU->lc == 0x00) {    // SYSTEM CALL TYPE (PROBABLY TO OBTAINING RESPONCE OUTPUT DATA)
                        // INSUFFICIENT OUTPUT DATA LENGTH
                        BYTE realLen = LOWBYTE(pAPDU->sw);						
                        pAPDU->le = realLen;

                        // NEW APDU WITH REQUIRED OUTPUT DATA LENGTH
                        status = ExchangeAPDU(pAPDU);    
                    }
                    else {
                        // USER CALL TYPE, OBTAIN RESPONSE DATA
		                BYTE        realLen = LOWBYTE(pAPDU->sw);				
                        CARDAPDU    apdu;

                        // PREPARE SYSTEM APDU FOR RECIEVE RESPONSE OUTPUT DATA 
		                apdu.cla = 0x00;
		                apdu.ins = 0xC0;
		                apdu.p1 = 0x00;
		                apdu.p2 = 0x00;
		                apdu.lc = 0x00;
                        apdu.le = realLen;
		                
                        if ((status = ExchangeAPDU(&apdu)) == STAT_OK) {
                            // COPY RECIEVED APDU
                            memcpy(pAPDU->DataOut, apdu.DataOut, apdu.le); 
                    
                            pAPDU->le = apdu.le;
                        }
	                }
                }
                else {
                    // CHECK FOR 'CORRECT DATA LENGTH' STATUS
                    if (((pAPDU->sw & 0xFF00) == SW_CORRECT_LENGTH_00)){
                        pAPDU->le = LOWBYTE(pAPDU->sw);
                    }
                    else {
                        // SOFTWARE ERROR OCCURED
                        status = TranslateISO7816Error(pAPDU->sw);
                    }
                }
            }
        }
    }
    else status = STAT_SESSION_NOT_OPEN;

    return status;
}    

int CPCSCMngr::SelectApplet(BYTE* pAppletAID, BYTE appletAIDLength) {
    int         status = STAT_OK;
    CARDAPDU    apdu;

    // SELECT REQUIRED APPLET
	apdu.cla = 0x00;
	apdu.ins = 0xA4;
	apdu.p1 = 0x04;
	apdu.p2 = 0x00;
	apdu.le = 0x00;
	apdu.lc = appletAIDLength;
	memcpy(apdu.DataIn, pAppletAID, appletAIDLength);

    if ((status = ExchangeAPDU(&apdu)) == STAT_OK) {
        // OK
    } 

    return status;
}

int CPCSCMngr::GetSCStatusString(CString* pSCStatus) {
    int             status = STAT_OK;
	char            *Readers=(char *)1, selected_reader[1000],msg[10000],msg_help[1000],convention[1000];	
	unsigned long   len=SCARD_AUTOALLOCATE;
	DWORD           protocol,state,a_len,r_len;
	unsigned char   atr[40],TS,T0,TA1,TB1,TC1,TD1,FI,DI,TA2,TB2,TC2,TD2,TA3,TB3,TC3,TD3;
	int             number_of_historical,pointer,ta1_sent=0,tb1_sent=0,tc1_sent=0,td1_sent=0,F,D;
	int             transmission_protocol1,ta2_sent=0,tb2_sent=0,tc2_sent=0,td2_sent=0;
	int             transmission_protocol2,ta3_sent=0,tb3_sent=0,tc3_sent=0,td3_sent=0;
	double          work_etu,cycles;
    
    if (m_cardContext && m_hCard) {

	    r_len=sizeof(selected_reader);
	    a_len=sizeof(atr);
    
        status = TranslateSCardError(SCardStatus(m_hCard, selected_reader,&r_len,&state,&protocol,atr,&a_len));
    }
    else status = STAT_SESSION_NOT_OPEN;

    if (status == STAT_OK) {
	    sprintf_s(msg,10000,"Reader: %s\nState: %s\nProtocol: %s\nATR: %s\n\n",selected_reader,card_state(state),card_protocol(protocol),card_atr(atr,a_len));								
	    
	    TS=atr[0];
	    switch(TS){
	     case 59: strcpy_s(convention,1000,"Direct convention"); break;
	     case 63: strcpy_s(convention,1000,"Inverse convention"); break;
	     default: strcpy_s(convention,1000,"Unknown initial character"); 				 
	    }

	    T0=atr[1];
	    number_of_historical=T0&0xF;				
	    ta1_sent=T0&16;
	    tb1_sent=T0&32;
	    tc1_sent=T0&64;
	    td1_sent=T0&128;				
	    pointer=2;
	    if(ta1_sent){ TA1=atr[pointer++]; DI=TA1&0xF; FI=TA1>>4; 								  
	      switch(FI){
	      case 0: F=372; break;
	      case 1: F=372; break;
	      case 2: F=558; break;
	      case 3: F=744; break;
	      case 4: F=1116; break;
	      case 5: F=1488; break;
	      case 6: F=1860; break;
	      case 9: F=512; break;
	      case 10: F=768; break;
	      case 11: F=1024; break;
	      case 12: F=1536; break;
	      case 13: F=2048; break;
	      default: F=0;
	      }
	      switch(DI){								  
	      case 1: D=1; break;
	      case 2: D=2; break;
	      case 3: D=4; break;
	      case 4: D=8; break;
	      case 5: D=16; break;
	      case 6: D=32; break;
	      case 8: D=12; break;
	      case 9: D=20; break;								  
	      default: D=0;
	      }
	      if(D==0||F==0) strcat_s(msg,10000,"Uncorrect frequency data in ATR\n");
	      work_etu=(double)(((double)F)/(double)((double)D*4000));
	      cycles=(double)(15/4)*(double)((((double)F)/(double)D));
	    }
	    if(tb1_sent)TB1=atr[pointer++]; 								  
	    if(tc1_sent)TC1=atr[pointer++];													  
	    if(td1_sent){
            TD1=atr[pointer++];					
	        transmission_protocol1=TD1&0xF;
            ta2_sent=TD1&16;
            tb2_sent=TD1&32;
            tc2_sent=TD1&64;
            td2_sent=TD1&128;								  
            if(ta2_sent)TA2=atr[pointer++];
            if(tb2_sent)TB2=atr[pointer++];
            if(tc2_sent)TC2=atr[pointer++];
            if(td2_sent) {
                TD2=atr[pointer++];
                transmission_protocol2=TD2&0xF;
                ta3_sent=TD2&16;
                tb3_sent=TD2&32;
                tc3_sent=TD2&64;
                td3_sent=TD2&128;									  
                if(ta3_sent)TA3=atr[pointer++];
                if(tb3_sent)TB3=atr[pointer++];
                if(tc3_sent)TC3=atr[pointer++];
                if(td3_sent){ TD3=atr[pointer++]; strcat_s(msg,10000,"Unusual ATR - not handling TD3 and further\n"); }
            }
        }
	    
	    sprintf_s(msg_help,1000,"Initial character: %s\n",convention);
	    strcat_s(msg,10000,msg_help);
	    if(!ta1_sent) sprintf_s(msg_help,1000,"TA1: not present\n");
	     else sprintf_s(msg_help,1000,"TA1: %02Xh (DI: %i, FI: %i -> D: %i, F: %i -> Work etu: %f ms, 15 Mhz cycles: %i)\n",(int)TA1,DI,FI,D,F,work_etu,(int)cycles);
	    strcat_s(msg,10000,msg_help);
	    if(!tb1_sent) sprintf_s(msg_help,1000,"TB1: not present\n");
	     else sprintf_s(msg_help,1000,"TB1: %02Xh (II: %i, PI1: %i [currently not used, should always be 0])\n",(int)TB1,(int)TB1>>5,(int)TB1&0x1F);
	    strcat_s(msg,10000,msg_help);
	    if(!tc1_sent) sprintf_s(msg_help,1000,"TC1: not present\n");
	     else if((int)TC1==255) sprintf_s(msg_help,1000,"TC1: %02Xh (special value: for T=0 guard time 2 etu, for T=1 guard time 1 etu)\n",(int)TC1);
		       else sprintf_s(msg_help,1000,"TC1: %02Xh (extra guard time %i etu)\n",(int)TC1,(int)TC1);
	    strcat_s(msg,10000,msg_help);

	    if(td1_sent)
	    {
	    sprintf_s(msg_help,1000,"Protocol: %i\n",transmission_protocol1);
	    strcat_s(msg,10000,msg_help);
	    if(!ta2_sent) sprintf_s(msg_help,1000,"   TA2: not present\n");
	    else if(transmission_protocol1==1)sprintf_s(msg_help,1000,"   TA2: %02Xh (Protocol T=%i to be used, %s, %s)\n",(int)TA2,(int)TA2&0xF,TA2&0x80?"switching betweeen negotiable and specific modes not possible":"switching betweeen negotiable and specific modes possible",TA2&0x10?"transmission parameters implicitely defined":"transmission parameters explicitely defined");
		       else sprintf_s(msg_help,1000,"   TA2: %02Xh\n",(int)TA2);
	    strcat_s(msg,10000,msg_help);
	    if(!tb2_sent) sprintf_s(msg_help,1000,"   TB2: not present\n");
	     else sprintf_s(msg_help,1000,"   TB2: %02Xh\n",(int)TB2);
	    strcat_s(msg,10000,msg_help);
	    if(!tc2_sent) sprintf_s(msg_help,1000,"   TC2: not present\n");
	     else if(transmission_protocol1==0) sprintf_s(msg_help,1000,"   TC2: %02Xh (WI: %i -> work waiting time: %i work etu)\n",(int)TC2,(int)TC2,960*D*(int)TC2);
		       else sprintf_s(msg_help,1000,"   TC2: %02Xh\n",(int)TC2);
	    strcat_s(msg,10000,msg_help);
	    }

	    if(td2_sent)
	    {
	    sprintf_s(msg_help,1000,"Protocol: %i\n",transmission_protocol2);
	    strcat_s(msg,10000,msg_help);
	    if(!ta3_sent) sprintf_s(msg_help,1000,"   TA3: not present\n");
	     else if(transmission_protocol2==1) sprintf_s(msg_help,1000,"   TA3: %02Xh (IFSC: %i) \n",(int)TA3,(int)TA3);
		       else sprintf_s(msg_help,1000,"   TA3: %02Xh\n",(int)TA3);
	    strcat_s(msg,10000,msg_help);
	    if(!tb3_sent) sprintf_s(msg_help,1000,"   TB3: not present\n");
	     else if(transmission_protocol2==1)sprintf_s(msg_help,1000,"   TB3: %02Xh (BWI: %i, CWI: %i -> BWT: %f s + 11 work etu , CWT: %i work etu)\n",(int)TB3,(int)TB3>>4,(int)TB3&0xF,(double)((1<<(TB3>>4))*960*372)/4000000,(1<<(TB3&0xF))+11);
		       else sprintf_s(msg_help,1000,"   TB3: %02Xh\n",(int)TB3);
	    strcat_s(msg,10000,msg_help);
	    if(!tc3_sent) sprintf_s(msg_help,1000,"   TC3: not present\n");
	    else if(transmission_protocol2==1)sprintf_s(msg_help,1000,"   TC3: %02Xh (%s)\n",(int)TC3,TC3?"CRC used":"LRC used");
		       else sprintf_s(msg_help,1000,"   TC3: %02Xh\n",(int)TC3);
	    strcat_s(msg,10000,msg_help);
	    }

        strcpy_s(msg_help, 1000,historical(atr,pointer,number_of_historical));
  	    strcat_s(msg,10000,msg_help);

        *pSCStatus = msg;
    }

    return status;
}

int CPCSCMngr::GetActualReader(CString* pReader, CString* pATR) {
    int             status = STAT_OK;
 	char            selected_reader[1000];
    DWORD           r_len;
    DWORD           a_len;
    DWORD           protocol;
    DWORD           state;
    unsigned char   atr[40];

    if (m_cardContext && m_hCard) {

	    r_len=sizeof(selected_reader);
	    a_len=sizeof(atr);
	    
        status = TranslateSCardError(SCardStatus(m_hCard, selected_reader,&r_len,&state,&protocol,atr,&a_len));
    }
    else status = STAT_SESSION_NOT_OPEN;
    

    if (status == STAT_OK) {
        *pReader = selected_reader;
        if (pATR) {
			char* atrStr = card_atr(atr, a_len);
			*pATR = atrStr;
			free(atrStr);
        }
    }

    return status;
}


int CPCSCMngr::ResetCard(void) {
	int             status = STAT_OK;

    if (!m_hCard) 
		status = STAT_SESSION_NOT_OPEN;

    if (status == STAT_OK) {
        status = TranslateSCardError(SCardReconnect(m_hCard, SCARD_SHARE_EXCLUSIVE,SCARD_PROTOCOL_T0|SCARD_PROTOCOL_T1, SCARD_UNPOWER_CARD, &m_scProtocol));
    }

    return status;
}

int CPCSCMngr::TranslateSCardError(LONG scardStatus) {
    int     retStatus = STAT_OK;

    // STAT_OK OR GENERAL STAT_SCARD_ERROR ERROR
    retStatus = (scardStatus == SCARD_S_SUCCESS) ? STAT_OK : STAT_SCARD_ERROR; 
    
    // IF SCARD ERROR, SAVE IT'S VALUE
    if (scardStatus != SCARD_S_SUCCESS) {
        m_lastSCardError = scardStatus;
    }

    return retStatus;
}




int CPCSCMngr::BYTE_ConvertFromArrayToHexString(BYTE* pArray, DWORD pbArrayLen, CString* pHexaString) {
    int         status = STAT_OK;
    CString     hexNum;
    DWORD       i;

    *pHexaString = "";
    for (i = 0; i < pbArrayLen; i++) {
        hexNum.Format("%.2x", pArray[i]);
        hexNum += " ";

        *pHexaString += hexNum;
    }

    pHexaString->TrimRight(" ");

    return status;
}

int CPCSCMngr::PrintAPDU(CARDAPDU* pAPDU, CString* pString, BOOL toSendAPDU, BOOL bIncludeStatus) {
    int         status = STAT_OK;
    CString     message;
    CString     ioData;
    
    if (toSendAPDU) {
        // APDU to SmartCard
        BYTE_ConvertFromArrayToHexString(pAPDU->DataIn, pAPDU->lc, &ioData);
        CString strStatus;
        if (bIncludeStatus) strStatus.Format("%.2x %.2x", HIGHBYTE(pAPDU->sw), LOWBYTE(pAPDU->sw)); 
        
		switch (pAPDU->apduType) {
			case CASE1: {
				// FORMAT: INS CLA P1 P2 
				message.Format("%.2x %.2x %.2x %.2x", pAPDU->cla, pAPDU->ins, pAPDU->p1, pAPDU->p2);
				break;
			}
			case CASE2: {
				// FORMAT: INS CLA P1 P2 LE 
				message.Format("%.2x %.2x %.2x %.2x  %.2x", pAPDU->cla, pAPDU->ins, pAPDU->p1, pAPDU->p2, pAPDU->le);
				break;
			}
			case CASE3: {		
				// FORMAT: INS CLA P1 P2 LC input_data
		        message.Format("%.2x %.2x %.2x %.2x %.2x  %s  %s", pAPDU->cla, pAPDU->ins, pAPDU->p1, pAPDU->p2, pAPDU->lc, (LPCTSTR) ioData, (LPCTSTR) strStatus);
				break;
			}
			case CASE4: {		
				// FORMAT: INS CLA P1 P2 LC input_data Le 
		        message.Format("%.2x %.2x %.2x %.2x %.2x  %s  %s %.2x", pAPDU->cla, pAPDU->ins, pAPDU->p1, pAPDU->p2, pAPDU->lc, (LPCTSTR) ioData, (LPCTSTR) strStatus, pAPDU->le);
				break;
			}
		}
    }
    else {
        // APDU from SmartCard
           
        BYTE_ConvertFromArrayToHexString(pAPDU->DataOut, pAPDU->le, &ioData);
        
        // FORMAT: output_data SW
        message.Format("%s  %.2x %.2x", (LPCTSTR) ioData, HIGHBYTE(pAPDU->sw), LOWBYTE(pAPDU->sw));
    }

    *pString = message;

    return status;
}












int CPCSCMngr::TranslateISO7816Error(int iso7816Error) {
    return iso7816Error;
}

int CPCSCMngr::TransmitAPDU(CARDAPDU* pAPDU, int dataInLenForced) {
    int                 status = STAT_OK;
    DWORD               outLen = pAPDU->le;
    BYTE                sendData[260];
    BYTE                responseData[260];

    pAPDU->le = 0;

    if (m_cardContext && m_hCard) {
        memset(sendData, 0x0, sizeof(sendData));
        memset(responseData, 0, sizeof(responseData));

        sendData[0] = pAPDU->cla;
        sendData[1] = pAPDU->ins;
        sendData[2] = pAPDU->p1;
        sendData[3] = pAPDU->p2;
        sendData[4] = pAPDU->lc;
        memcpy(sendData + 5, pAPDU->DataIn, pAPDU->lc);
        
        outLen = 100;
		if (pAPDU->le != 0) {
			sendData[5 + pAPDU->lc] = pAPDU->le;
			outLen = pAPDU->le;
		}

		// Default behaviour of T1 protocol is following:
		// 1. If no data are submitted (LC==0), no LE byte is appended
		// 2. If some data are submitted (LC > 0), LE byte is appended (LE can be zero)
		// 3. Sometimes, card may require forceNoLeInT1
		int dataInT1 = (pAPDU->lc > 0) ? sendData[4] + 6 : 5;
     
		// Default behaviour of T0 protocol is following:
		// 1. Header and data are sumitted, no LE is appended => header (5 bytes) + data (LC bytes)
		// 2. Sometimes, LC is used 
		int dataInT0 = 0;
		if (dataInLenForced == 0) dataInT0 = sendData[4] + 5;
		else dataInT0 = dataInLenForced;

        // SEND APDU
		switch (m_scProtocol) {
    		case SCARD_PROTOCOL_T0: status = TranslateSCardError(SCardTransmit(m_hCard, SCARD_PCI_T0, sendData, dataInT0, NULL, responseData, &outLen)); break;
			case SCARD_PROTOCOL_T1: status = TranslateSCardError(SCardTransmit(m_hCard, SCARD_PCI_T1, sendData, dataInT1, NULL, responseData, &outLen)); break;
			default: status = STAT_UNKNOWN_SCARD_PROTOCOL; 
		}						

        if (status == STAT_OK) {
            ((BYTE*) &(pAPDU->sw))[0] = responseData[outLen-1];
            ((BYTE*) &(pAPDU->sw))[1] = responseData[outLen-2];
			pAPDU->le = (BYTE) outLen - 2;
			memcpy(pAPDU->DataOut, responseData, pAPDU->le);
        }
    }
    else status = STAT_SESSION_NOT_OPEN;

    if (status == STAT_OK) {
		// RECEIVE RESPONSE DATA, IF ANY 
		if (((pAPDU->sw & 0xFF00) == SW_BYTES_REMAINING_00) || ((pAPDU->sw & 0xFF00) == SW_CORRECT_LENGTH_00)) {
			// GET DATA APDU
			sendData[0] = 0xC0;
			sendData[1] = 0xC0;
			sendData[2] = 0x00;
			sendData[3] = 0x00;
			sendData[4] = LOWBYTE(pAPDU->sw);

			outLen = sendData[4] + 2;   // DATA OUT + STATUS
		   
			switch (m_scProtocol) {
				case SCARD_PROTOCOL_T0: status = TranslateSCardError(SCardTransmit(m_hCard, SCARD_PCI_T0, sendData, 5, NULL, responseData, &outLen)); break;
				case SCARD_PROTOCOL_T1: status = TranslateSCardError(SCardTransmit(m_hCard, SCARD_PCI_T1, sendData, 5, NULL, responseData, &outLen)); break;
				default: status = STAT_UNKNOWN_SCARD_PROTOCOL; 
			}						

			if (status == STAT_OK) {
				memcpy(pAPDU->DataOut, responseData, outLen - 2);
				pAPDU->le = (BYTE) outLen - 2;
				((BYTE*) &(pAPDU->sw))[0] = responseData[outLen - 1];   // LAST BYTE
				((BYTE*) &(pAPDU->sw))[1] = responseData[outLen - 2];   // PRE LAST BYTE
			}
		}
    }

    return status;
}


int CPCSCMngr::TransmitAPDUByCase(CARDAPDU* pAPDU) {
    int                 status = STAT_OK;
    DWORD               outLen = 260;
    BYTE                sendData[260];
    BYTE                responseData[260];
	int					dataIn = 0;

    if (m_cardContext && m_hCard) {
        memset(sendData, 0x0, sizeof(sendData));
        memset(responseData, 0, sizeof(responseData));

        sendData[0] = pAPDU->cla;
        sendData[1] = pAPDU->ins;
        sendData[2] = pAPDU->p1;
        sendData[3] = pAPDU->p2;
		dataIn = 4;
		switch (pAPDU->apduType) {
			case CASE1: break;
			case CASE2: {
				sendData[4] = pAPDU->le;
				dataIn++;
				break;
			}
			case CASE3: {
				sendData[4] = pAPDU->lc;
		        memcpy(sendData + 5, pAPDU->DataIn, pAPDU->lc);
				dataIn += 1 + pAPDU->lc;
				break;
			}
			case CASE4: {
				sendData[4] = pAPDU->lc;
		        memcpy(sendData + 5, pAPDU->DataIn, pAPDU->lc);
				dataIn += 1 + pAPDU->lc;

				if (m_scProtocol == SCARD_PROTOCOL_T1) {
					sendData[5 + pAPDU->lc] = pAPDU->le;
					dataIn++;
				}
				break;
			}
		}

		// SEND APDU
		switch (m_scProtocol) {
    		case SCARD_PROTOCOL_T0: status = TranslateSCardError(SCardTransmit(m_hCard, SCARD_PCI_T0, sendData, dataIn, NULL, responseData, &outLen)); break;
			case SCARD_PROTOCOL_T1: status = TranslateSCardError(SCardTransmit(m_hCard, SCARD_PCI_T1, sendData, dataIn, NULL, responseData, &outLen)); break;
			default: status = STAT_UNKNOWN_SCARD_PROTOCOL; 
		}						

        if (status == STAT_OK) {
            ((BYTE*) &(pAPDU->sw))[0] = responseData[outLen-1];
            ((BYTE*) &(pAPDU->sw))[1] = responseData[outLen-2];
			pAPDU->le = (BYTE) outLen - 2;
			memcpy(pAPDU->DataOut, responseData, pAPDU->le);
        }
    }
    else status = STAT_SESSION_NOT_OPEN;

    if (status == STAT_OK) {
		// RECEIVE RESPONSE DATA, IF ANY 
		if (((pAPDU->sw & 0xFF00) == SW_BYTES_REMAINING_00) || 
			((pAPDU->sw & 0xFF00) == SW_CORRECT_LENGTH_00) &&
			(m_scProtocol == SCARD_PROTOCOL_T0)) {

			// GET DATA APDU - SW_BYTES_REMAINING_00
			if ((pAPDU->sw & 0xFF00) == SW_BYTES_REMAINING_00) {
				sendData[0] = 0x00;
				sendData[1] = 0xC0;
				sendData[2] = 0x00;
				sendData[3] = 0x00;
			}
			else {
				// SW_CORRECT_LENGTH_00 - just set correct value
			}

			sendData[4] = LOWBYTE(pAPDU->sw);

			outLen = sendData[4] + 2;   // DATA OUT + STATUS
		   
			switch (m_scProtocol) {
				case SCARD_PROTOCOL_T0: status = TranslateSCardError(SCardTransmit(m_hCard, SCARD_PCI_T0, sendData, 5, NULL, responseData, &outLen)); break;
				default: status = STAT_UNKNOWN_SCARD_PROTOCOL; 
			}						

			if (status == STAT_OK) {
				memcpy(pAPDU->DataOut, responseData, outLen - 2);
				pAPDU->le = (BYTE) outLen - 2;
				((BYTE*) &(pAPDU->sw))[0] = responseData[outLen - 1];   // LAST BYTE
				((BYTE*) &(pAPDU->sw))[1] = responseData[outLen - 2];   // PRE LAST BYTE
			}
		}
    }
/**/
    return status;
}

char* CPCSCMngr::card_state(DWORD state) {
	static char buffer[100];
	if(state==SCARD_ABSENT)return "SCARD_ABSENT";
	if(state==SCARD_PRESENT)return "SCARD_PRESENT";
	if(state==SCARD_SWALLOWED)return "SCARD_SWALLOWED";
	if(state==SCARD_POWERED)return "SCARD_POWERED";
	if(state==SCARD_NEGOTIABLE)return "SCARD_NEGOTIABLE";
	if(state==SCARD_SPECIFIC)return "SCARD_SPECIFIC";
	return "Unknown";
}

char* CPCSCMngr::card_protocol(DWORD protocol)
{
	if(protocol==SCARD_PROTOCOL_RAW)return "SCARD_PROTOCOL_RAW";
	if(protocol==SCARD_PROTOCOL_T0)return "SCARD_PROTOCOL_T0";
	if(protocol==SCARD_PROTOCOL_T1)return "SCARD_PROTOCOL_T1";	
	return "";
}

char* CPCSCMngr::card_atr(BYTE *atr, DWORD a_len)
{
	unsigned int i;
	char *Atr,buf[100];

	Atr=(char*)malloc(100);
	if(Atr==NULL) return NULL; else strcpy_s(Atr,100,"");
	for(i=1;i<=a_len;i++)
	{ if(i==a_len)sprintf_s(buf,100,"%02X",(int)atr[i-1]); else sprintf_s(buf,100,"%02X:",(int)atr[i-1]); 
	  strcat_s(Atr,100,buf); }
	return Atr;
}

char* CPCSCMngr::historical(BYTE *atr,int pointer,int number_of_historical)
{
	int i;
	char *result,*p,help[100];

	result=(char*)malloc(1000);
	if(result==NULL)return NULL; else strcpy_s(result,1000,"Historical characters: ");
	for(i=1;i<=number_of_historical;i++)
	{ sprintf_s(help,100,"%02X%c",atr[pointer+i-1],i==number_of_historical?' ':':');
	  strcat_s(result,1000,help);
	}
	strcat_s(result,1000," (\"");
	p=result+strlen(result);
	for(i=1;i<=number_of_historical;i++)
		*p++=atr[pointer+i-1];
	*p=0;
	strcat_s(result,1000,"\")");
	return result;
}

