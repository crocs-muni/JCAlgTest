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

package AlgTest;

import javacard.framework.*;


public class JCAlgTestApplet extends javacard.framework.Applet {
    public final static byte INS_CARD_TESTAVAILABLE_MEMORY = (byte) 0x71;
    
    private byte[] m_eepromArray1 = null;
    private byte[] m_eepromArray2 = null;
    private byte[] m_eepromArray3 = null;
    private byte[] m_eepromArray4 = null;
    private byte[] m_eepromArray5 = null;
    private byte[] m_eepromArray6 = null;
    private byte[] m_eepromArray7 = null;
    private byte[] m_eepromArray8 = null;
    
            
    protected JCAlgTestApplet(byte[] buffer, short offset, byte length) {
        // data offset is used for application specific parameter.
        // initialization with default offset (AID offset).
        short dataOffset = offset;
        boolean isOP2 = false;

        if(length > 9) {
            // Install parameter detail. Compliant with OP 2.0.1.
            // shift to privilege offset
            dataOffset += (short)( 1 + buffer[offset]);
            // finally shift to Application specific offset
            dataOffset += (short)( 1 + buffer[dataOffset]);

            // go to proprietary data
            dataOffset++;
            // update flag
            isOP2 = true;
       } else {}

       
        if (isOP2) { register(buffer, (short)(offset + 1), buffer[offset]); }
        else { register(); }
    }

    public static void install(byte[] bArray, short bOffset, byte bLength) throws ISOException {
        new JCAlgTestApplet (bArray, bOffset, bLength );
    }

    public boolean select() {
        return true;
    }

    public void deselect() {
    }

    public void process(APDU apdu) throws ISOException {
        // get the APDU buffer
        byte[] apduBuffer = apdu.getBuffer();

        // ignore the applet select command dispached to the process
        if (selectingApplet()) { return; }
        
        
        switch (apduBuffer[ISO7816.OFFSET_INS]) {
            case INS_CARD_TESTAVAILABLE_MEMORY: {
                TestAvailableMemory(apdu);
                break;
            }
            default: {
                JCSystem.requestObjectDeletion();

                short offset = 0;
                Util.setShort(apduBuffer, offset, JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_PERSISTENT));
                offset += 2;
                Util.setShort(apduBuffer, offset, JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_RESET));
                offset += 2;
                Util.setShort(apduBuffer, offset, JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT));
                offset += 2;
                apdu.setOutgoingAndSend((short) 0, offset);
            }
        }
    }
    
    void TestAvailableMemory(APDU apdu) {
        byte[] apdubuf = apdu.getBuffer();
        apdu.setIncomingAndReceive();
        short offset = (short) 0;

        //
        // EEPROM TEST
        //
        short toAllocateEEPROM = (short) 15000;    // at maximum 15KB allocated into single array 
        if (m_eepromArray1 == null) {
            while (true) {
                if (toAllocateEEPROM < 100) {
                    break;
                } // We will stop when less then 100 remain to be allocated
                try {
                    if (m_eepromArray1 == null) {
                        m_eepromArray1 = new byte[toAllocateEEPROM];
                    }
                    if (m_eepromArray2 == null) {
                        m_eepromArray2 = new byte[toAllocateEEPROM];
                    }
                    if (m_eepromArray3 == null) {
                        m_eepromArray3 = new byte[toAllocateEEPROM];
                    }
                    if (m_eepromArray4 == null) {
                        m_eepromArray4 = new byte[toAllocateEEPROM];
                    }
                    if (m_eepromArray5 == null) {
                        m_eepromArray5 = new byte[toAllocateEEPROM];
                    }
                    if (m_eepromArray6 == null) {
                        m_eepromArray6 = new byte[toAllocateEEPROM];
                    }
                    if (m_eepromArray7 == null) {
                        m_eepromArray7 = new byte[toAllocateEEPROM];
                    }
                    if (m_eepromArray8 == null) {
                        m_eepromArray8 = new byte[toAllocateEEPROM];
                    }
                    // ALLOCATION OF ALL ARRAYS WAS SUCESSFULL

                    break;
                } catch (Exception e) {
                    // DECREASE TESTED ALLOCATION LENGTH BY 10%
                    toAllocateEEPROM = (short) (toAllocateEEPROM - (short) (toAllocateEEPROM / 10));
                }
            }
        } else {
            // ARRAY(s) ALREADY ALLOCATED, JUST RETURN THEIR COMBINED LENGTH
        }

        if (m_eepromArray1 != null) {
            Util.setShort(apdubuf, offset, (short) m_eepromArray1.length);
        } else {
            Util.setShort(apdubuf, offset, (short) 0);
        }
        offset = (short) (offset + 2);
        if (m_eepromArray2 != null) {
            Util.setShort(apdubuf, offset, (short) m_eepromArray2.length);
        } else {
            Util.setShort(apdubuf, offset, (short) 0);
        }
        offset = (short) (offset + 2);
        if (m_eepromArray3 != null) {
            Util.setShort(apdubuf, offset, (short) m_eepromArray3.length);
        } else {
            Util.setShort(apdubuf, offset, (short) 0);
        }
        offset = (short) (offset + 2);
        if (m_eepromArray4 != null) {
            Util.setShort(apdubuf, offset, (short) m_eepromArray4.length);
        } else {
            Util.setShort(apdubuf, offset, (short) 0);
        }
        offset = (short) (offset + 2);
        if (m_eepromArray5 != null) {
            Util.setShort(apdubuf, offset, (short) m_eepromArray5.length);
        } else {
            Util.setShort(apdubuf, offset, (short) 0);
        }
        offset = (short) (offset + 2);
        if (m_eepromArray6 != null) {
            Util.setShort(apdubuf, offset, (short) m_eepromArray6.length);
        } else {
            Util.setShort(apdubuf, offset, (short) 0);
        }
        offset = (short) (offset + 2);
        if (m_eepromArray7 != null) {
            Util.setShort(apdubuf, offset, (short) m_eepromArray7.length);
        } else {
            Util.setShort(apdubuf, offset, (short) 0);
        }
        offset = (short) (offset + 2);
        if (m_eepromArray8 != null) {
            Util.setShort(apdubuf, offset, (short) m_eepromArray8.length);
        } else {
            Util.setShort(apdubuf, offset, (short) 0);
        }
        offset = (short) (offset + 2);
        apdu.setOutgoingAndSend((short) 0, offset);
    }      
}