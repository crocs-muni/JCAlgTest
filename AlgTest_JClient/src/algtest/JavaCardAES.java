/*
Create by Petr Svenda http://www.svenda.com/petr

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

   1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
   2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
   3. The name of the author may not be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

Based on non-optimized example code for Rijndael by J. Daemon and V. Rijmen

USAGE:
    // allocate engine
    JavaCardAES aesCipher = new JavaCardAES();
    // set array with initiualization vector
    aesCipher.m_IV = array_with_IV;
    aesCipher.m_IVOffset = 0;

    // schedule keys for first key into array array_for_round_keys_1
    aesCipher.RoundKeysSchedule(array_with_key1, (short) 0, array_for_round_keys_1);
    // encrypt block with first key
    aesCipher.AESEncryptBlock(data_to_encrypt, start_offset_of_data, array_for_round_keys_1);

    // schedule keys for second key into array array_for_round_keys_2
    aesCipher.RoundKeysSchedule(array_with_key_2, (short) 0, array_for_round_keys_2);
    // decrypt block with second key
    aesCipher.AESDecryptBlock(data_to_decrypt_2, start_offset_of_data, array_for_round_keys_2);

    // encrypt again with first key
    aesCipher.AESEncryptBlock(data_to_decrypt_2, start_offset_of_data, array_for_round_keys_1);


APPLIED OPTIMIZATIONS:
- UNROLLED LOOPS (only minor effect as compiler is doing that also)
- PRE-COMPUTED Alogtable and Logtable (common)
- PRE-COMPUTED Alogtable_mul2 and Alogtable_mul3 (will speed-up MixColumn computation
   with 'mul((byte) 2, a[(short) (i + hlp)])' and 'mul((byte) 3, a[(short) (i + hlp)])' commands)
   * due to space-constraints, InvMixColumn is NOT optimized this way (separate tables for 0xe, 0xb, 0xd, 0x9 are needed)
   * note, on Cyberflex 32K e-gate there is time saving only 1 second from 9 sec to 8 sec (and tables needs 512B)
   * if have to be used, then uncomment parts  ALOG_MUL

SPEED (Cyberflex 32k e-gate):
- encryption (one block) on 9 second  (when MixColumn "removed", then only 4 sec => so you may try to optimize MixColumn)
- key schedule 4 seconds
- reduced version with 7 rounds only - 6 seconds (!! see note located above N_ROUNDS)

SPEED (GXP E64PK):
- encryption (one block) less than 1 second

/**/

package AlgTest;
import javacard.framework.*;

public class JavaCardAES {
  final static short SW_IV_BAD                        = (short) 0x6709;   // BAD INICIALIZATION VECTOR
  final static short SW_CIPHER_DATA_LENGTH_BAD        = (short) 0x6710;   // BAD LENGTH OF DATA USED DURING CIPHER OPERATION

    // NOTE: BLOCKN & KEYN CONSTANTS ARE DEFINED
    // ONLY FOR BETTER READIBILITY OF CODE AND CANNOT BE CHANGED!!!
    final public static byte BLOCKLEN                  = (byte) (128 / 8);
    final static byte BLOCKN    		= (byte) (128 / 32);
    final static byte KEYN    		        = (byte) (128 / 32);
    final static short STATELEN                 = (short) (4 * BLOCKN);

    // IMPORTANT: THIS IMPLEMENTATION IS CONSTRUCTED FOR 128bit KEY and 128bit BLOCK
    // FOR THIS SETTING, 10 ITERATION ROUNDS ARE GIVEN IN SPECIFICATION
    // HOWEVER, NUMBER OF THESE ROUNDS CAN BE DECREASED - CURRENTLY (2006) BEST KNOWN PRACTICALLY REALISABLE ATTACK
    // IS AGAINST REDUCED ALG. WITH 6 ROUNDS AND REQUIRE: 2^32 choosen plaintexts and 2^44 time steps (http://www.schneier.com/paper-rijndael.pdf)
    // THEREFORE 7 ROUNDS CANNOT BE ATTACKED RIGHT NOW (2006) ANF IF YOU *KNOW WHAT YOUR ARE DOING*,
    // THEN REDUCE ROUNDS AND GET 30% SPEED-UP
    // NOTE THAT ALGORITHM WILL NOT BE BINARY COMPATIBLE WITH AES TEST VECTORS ANYMORE
    public static byte N_ROUNDS    		= (byte) 10;

    final static byte rcon[] = {(byte) 0x01, (byte) 0x02, (byte) 0x04, (byte) 0x08, (byte) 0x10, (byte) 0x20, (byte) 0x40, (byte) 0x80, (byte) 0x1b, (byte) 0x36};

    // shifts[0..3] -> ENCRYPT, shifts[4..7] ... DECRYPT
    final static byte shifts[] = { 0, 1, 2, 3, 0, 3, 2, 1};

    // NOTE: NEXT ARRAYS COULD BE DECLARED STATIC, BUT UNKNOWN PROBLEM OCCURES
    // DURING APPLET INSTALATION ON Gemplus GXPPro-R3.
    private byte SBox[] = null;
    private byte SiBox[] = null;
    private byte Alogtable[] = null;
// ALOG_MUL    private byte Alogtable_mul2[] = null;
// ALOG_MUL    private byte Alogtable_mul3[] = null;
    private short Logtable[] = null;

    // SCHEDULED ROUND KEYS
    //private byte roundKeys[] = null;

    // PREALOCATED REUSED TRANSIENT BUFFER
    private byte tempBuffer[] = null;

    // INICIALIZATION VECTOR
    public byte       m_IV[] = null;
    public short      m_IVOffset = 0;

    public JavaCardAES() {
      // ALLOCATE AND COMPUTE LOOKUP TABLES
      SBox = new byte[256];
      SiBox = new byte[256];
      Alogtable = new byte[256];
// ALOG_MUL     Alogtable_mul2 = new byte[256];
// ALOG_MUL     Alogtable_mul3 = new byte[256];
// ALOG_MUL     Alogtable_mul2 = JCSystem.makeTransientByteArray((short)256, JCSystem.CLEAR_ON_RESET);
// ALOG_MUL     Alogtable_mul3 = JCSystem.makeTransientByteArray((short)256, JCSystem.CLEAR_ON_RESET);
      Logtable = new short[256];
      tempBuffer = JCSystem.makeTransientByteArray(STATELEN, JCSystem.CLEAR_ON_RESET);
      MakeSBox();
    }

    // CALCULATION OF LOOKUP TABLES FOR REDUCING CODE SIZE
    private void MakeSBox() {
      byte   p = 1;
      short  q;
      short  i;

      // Alogtable AND Logtable TABLES
      for (i=0; i<256; ++i) {
          Alogtable[i]= (byte) p;
          Logtable[(p >= 0) ? p : (short) (256 + p)]= (byte) i;
          p=(byte) (p^(p<<1)^(((p&0x80) == 0) ? 0: 0x01b));
      }
      // CORRECTION OF GENERATED LOG TABLE IS NEEDED
      Logtable[1] = 0;

      // SBox AND SiBox TABLES
      for (i=0; i<256; ++i)  {
         p= ((i == 0) ? 0 : (Alogtable[(short) (255-((Logtable[i] >= 0) ? Logtable[i] : (short) (256 + Logtable[i])))]));
         q= (p >= 0) ? p : (short) (256 + p);
         q= (short) ((q>>7) | (q<<1)); p^= (byte) q;
         q= (short) ((q>>7) | (q<<1)); p^= (byte) q;
         q= (short) ((q>>7) | (q<<1)); p^= (byte) q;
         q= (short) ((q>>7) | (q<<1)); p^= (byte) q;
         p= (byte) (p^0x63);
         SBox[i] = (byte) p;
         SiBox[(p >= 0) ? p : (short) (256 + p)] = (byte) i;
      }

      // CONVERT LogTable FROM byte-oriented value into short-oriented
      for (i=0; i<256; ++i) {
        if (Logtable[i] < 0) Logtable[i] = (short) (256 + Logtable[i]);
      }

/*// ALOG_MUL
      // PRE-COMPUTE Alogtable_mul2 and Alogtable_mul3
      Alogtable_mul2[0] = 0;
      for (i=1; i < 256; i++) Alogtable_mul2[i] = (byte) Alogtable[(short) ((short) (Logtable[2] + Logtable[i]) % 255)];
      Alogtable_mul3[0] = 0;
      for (i=1; i < 256; i++) Alogtable_mul3[i] = (byte) Alogtable[(short) ((short) (Logtable[3] + Logtable[i]) % 255)];
/**/
    }

    /**
     * Schedule AES round keys fro given key material
     * @param key ... key array
     * @param keyOffset ... start offset in key array
     * @param aesRoundKeys ... array to hold scheduled keys
     */
    public void RoundKeysSchedule(byte key[], short keyOffset, byte aesRoundKeys[]) {
      byte     i;
      byte     j;
      byte     round;
      byte     rconpointer = 0;
      short    sourceOffset = 0;
      short    targetOffset = 0;
      // hlp CONTAINS PRECALCULATED EXPRESSION (round * (4 * KEYN))
      short    hlp = 0;

      // FIRST KEY (SAME AS INPUT KEY)
      Util.arrayCopyNonAtomic(key, keyOffset, aesRoundKeys, (short) 0, STATELEN);

      // 10 ROUNDS KEYS
      for (round = 1; round <= N_ROUNDS; round++) {
          // TIME REDUCING PRECALCULATION
          hlp += STATELEN;

          // COPY KEY FOR round - 1 TO BUFFER FOR round
          Util.arrayCopyNonAtomic(aesRoundKeys, (short) ((round - 1) * STATELEN), aesRoundKeys, hlp, STATELEN);

          rconpointer = (byte) (round - 1);

          for (i = 0; i < 4; i++) {
            sourceOffset = (short) ( ((i + 1) % 4) + ((KEYN-1) * 4) + hlp );
            targetOffset = (short) ( i + (0 * 4) + hlp );
            aesRoundKeys[targetOffset] ^= SBox[(aesRoundKeys[sourceOffset] >= 0) ? aesRoundKeys[sourceOffset] : (short) (256 + aesRoundKeys[sourceOffset])];
          }

          aesRoundKeys[hlp] ^= rcon[rconpointer];

          for (j = 1; j < KEYN; j++) {
              for (i = 0; i < 4; i++) {
                sourceOffset = (short) (i + ((j - 1) * 4) + hlp);
                targetOffset = (short) ((i + (j * 4)) + hlp);
                aesRoundKeys[targetOffset] ^= aesRoundKeys[sourceOffset];
              }
          }
      }
    }
/*
    //
    // NOT USED IN THIS IMPLEMENTATION EXCEPT UNOPTIMIZED VERSION
    //
    private static short TAUB(byte a) {
      // RETURN SHORT VALUE CONSTRUCTED FROM SIGNED REPRESENTATION OF UNSIGNED VALUE
      // EXAMPLE: byte val = (byte) 250; // val == -6
      //          ASSERT(TAUB(val) == 250);
      return ((a >= 0) ? a : (short) (256 + a));
    }

    // MULTIPLY TWO ELEMENTS OF GF(2^m)
    private byte mul(short a, short b) {
      if ((a != 0) && (b != 0)) {
        return (byte) Alogtable[(short) ((short) (Logtable[a] + Logtable[b]) % 255)];
      }
      else return (byte) 0;
    }

    // ADD ROUND KEY USING XOR
    private static void KeyAddition(byte a[], short dataOffset, byte rk[], short keyOffset) {
      byte i;
      for (i = 0; i < STATELEN; i++) a[(short) (i + dataOffset)] ^= rk[(short) (i + keyOffset)];
    }

    // SBox OR SiBox SUBSTITUTION
    private static void Substitution(byte a[], short dataOffset, byte box[]) {
      byte i;
      for (i = 0; i < STATELEN; i++) a[(short) (i + dataOffset)] = box[((a[(short) (i + dataOffset)] >= 0) ? a[(short) (i + dataOffset)] : (short) (256 + a[(short) (i + dataOffset)]))] ;
    }


/**/

    // SHIFTING ROWS
    private void ShiftRow(byte a[], short dataOffset, byte d) {
      byte i, j;
      // ALSO FIRST ROUND IS SHIFTED (BUT BY 0 POSITIONS) DUE TO POSSIBILITY FOR USING Util.arrayCopy() LATER
      // tempBuffer WILL CONTAINS SHIFTED STATE a
      for(i = 0; i < 4; i++) {
          for(j = 0; j < BLOCKN; j++) tempBuffer[(short) (i + j * 4)] = a[(short) (((i + (byte) ((j + shifts[(short) (i + d*4)] % BLOCKN) * 4)) % STATELEN) + dataOffset)];
      }
      Util.arrayCopyNonAtomic(tempBuffer, (short) 0, a, dataOffset, STATELEN);
    }


    // MIXING COLUMNS
    private void MixColumn(byte a[], short dataOffset) {
      byte  i = 0, j = 0;
      // hlp CONTAINS PRECALCULATED EXPRESSION ((j * 4) + dataOffset)
      short hlp = dataOffset;
      // hlp2 CONTAINS PRECALCULATED EXPRESSION (j * 4)
      byte hlp2 = -4;
      byte hlp3 = 0;
      short tempVal = 0;
      short tempVal2 = 0;
      short a0 = 0;
      short a1 = 0;
      short a2 = 0;
      short a3 = 0;

      hlp -= 4;
      for(j = 0; j < BLOCKN; j++) {
        // TIME REDUCING PRECALCULATION
        hlp += 4; hlp2 += 4;

/*        // UNROLL THIS LOOP:
//        for(i = 0; i < 4; i++) {
   // NOT OPTIMISED
          tempBuffer[(byte) (i + hlp2)] = (byte) mul((short) 2, TAUB(a[(short) (i + hlp)]));
          tempBuffer[(byte) (i + hlp2)] ^= (byte) mul((short) 3, TAUB(a[(short) (((i + 1) % 4) + hlp)]));
          tempBuffer[(byte) (i + hlp2)] ^= (byte) a[(short) (((i + 2) % 4) + hlp)];
          tempBuffer[(byte) (i + hlp2)] ^= (byte) a[(short) (((i + 3) % 4) + hlp)];
     // END NOT OPTIMISED
        }
/**/

     // *** OPT2: OPTIMIZED VERSION WITH UNROLLED LOOPS AND EXPANDED mul() FUNCTION
        // UNROLLED LOOP: for (i = 0; i < 4; i++)
        // ax WILL CONTAIN VALUE OF 'a[(short) (((i + x) % 4) + hlp)];' TRANSFORMED FROM byte TO short (via TAUB-like function)
          a0 = a[hlp]; a0 = (a0 >= 0) ? a0 : (short) (256 + a0);
          a1 = a[(short) (1 + hlp)]; a1 = (a1 >= 0) ? a1 : (short) (256 + a1);
          a2 = a[(short) (2 + hlp)]; a2 = (a2 >= 0) ? a2 : (short) (256 + a2);
          a3 = a[(short) (3 + hlp)]; a3 = (a3 >= 0) ? a3 : (short) (256 + a3);

          // i == 0
          // tempBuffer[hlp2] = (byte) mul((byte) 2, a0);
          tempBuffer[hlp2] = (a0 != 0) ? (byte) Alogtable[(short) ((short) (Logtable[2] + Logtable[a0]) % 255)] : (byte) 0;
          // tempBuffer[hlp2] ^= (byte) mul((byte) 3, a1);
          if (a1 != 0) tempBuffer[hlp2] ^= (byte) Alogtable[(short) ((short) (Logtable[3] + Logtable[a1]) % 255)];
          tempBuffer[hlp2] ^= a2;
          tempBuffer[hlp2] ^= a3;

          // i == 1
          hlp3 = (byte) (hlp2 + 1);
          //tempBuffer[hlp3] = (byte) mul((byte) 2, a1);
          tempBuffer[hlp3] = (a1 != 0) ? (byte) Alogtable[(short) ((short) (Logtable[2] + Logtable[a1]) % 255)] : (byte) 0;
          //tempBuffer[hlp3] ^= (byte) mul((byte) 3, a2);
          if (a2 != 0) tempBuffer[hlp3] ^= (byte) Alogtable[(short) ((short) (Logtable[3] + Logtable[a2]) % 255)];
          tempBuffer[hlp3] ^= a3;
          tempBuffer[hlp3] ^= a0;

          // i == 2
          hlp3 = (byte) (hlp2 + 2);
          //tempBuffer[hlp3] = (byte) mul((byte) 2, a2);
          tempBuffer[hlp3] = (a2 != 0) ? (byte) Alogtable[(short) ((short) (Logtable[2] + Logtable[a2]) % 255)] : (byte) 0;
          //tempBuffer[hlp3] ^= (byte) mul((byte) 3, a3);
          if (a3 != 0) tempBuffer[hlp3] ^= (byte) Alogtable[(short) ((short) (Logtable[3] + Logtable[a3]) % 255)];
          tempBuffer[hlp3] ^= a0;
          tempBuffer[hlp3] ^= a1;

          // i == 3
          hlp3 = (byte) (hlp2 + 3);
          //tempBuffer[hlp3] = (byte) mul((byte) 2, a3);
          tempBuffer[hlp3] = (a3 != 0) ? (byte) Alogtable[(short) ((short) (Logtable[2] + Logtable[a3]) % 255)] : (byte) 0;
          //tempBuffer[hlp3] ^= (byte) mul((byte) 3, a0);
          if (a0 != 0) tempBuffer[hlp3] ^= (byte) Alogtable[(short) ((short) (Logtable[3] + Logtable[a0]) % 255)];
          tempBuffer[hlp3] ^= a1;
          tempBuffer[hlp3] ^= a2;

       //***  OPT2: END OPTIMIZED VERSION WITH UNROLLED LOOPS AND EXPANDED mul() FUNCTION /**/

/* // ALOG_MUL - disable OPT2: block when use OPT3
// *** OPT3: OPTIMIZED VERSION WITH UNROLLED LOOPS AND PRE-COMPUTED mul(2,x) AND  mul(3,x) FUNCTION
   // UNROLLED LOOP: for (i = 0; i < 4; i++)
   // ax WILL CONTAIN VALUE OF 'a[(short) (((i + x) % 4) + hlp)];' TRANSFORMED FROM byte TO short (via TAUB-like function)
          a0 = a[hlp]; a0 = (a0 >= 0) ? a0 : (short) (256 + a0);
          a1 = a[(short) (1 + hlp)]; a1 = (a1 >= 0) ? a1 : (short) (256 + a1);
          a2 = a[(short) (2 + hlp)]; a2 = (a2 >= 0) ? a2 : (short) (256 + a2);
          a3 = a[(short) (3 + hlp)]; a3 = (a3 >= 0) ? a3 : (short) (256 + a3);

          // i == 0
          tempBuffer[hlp2] = Alogtable_mul2[a0];
          tempBuffer[hlp2] ^= Alogtable_mul3[a1];
          tempBuffer[hlp2] ^= a2;
          tempBuffer[hlp2] ^= a3;

          // i == 1
          hlp3 = (byte) (hlp2 + 1);
          tempBuffer[hlp3] = Alogtable_mul2[a1];
          tempBuffer[hlp3] ^= Alogtable_mul3[a2];
          tempBuffer[hlp3] ^= a3;
          tempBuffer[hlp3] ^= a0;

          // i == 2
          hlp3 = (byte) (hlp2 + 2);
          tempBuffer[hlp3] = Alogtable_mul2[a2];
          tempBuffer[hlp3] ^= Alogtable_mul3[a3];
          tempBuffer[hlp3] ^= a0;
          tempBuffer[hlp3] ^= a1;

          // i == 3
          hlp3 = (byte) (hlp2 + 3);
          tempBuffer[hlp3] = Alogtable_mul2[a3];
          tempBuffer[hlp3] ^= Alogtable_mul3[a0];
          tempBuffer[hlp3] ^= a1;
          tempBuffer[hlp3] ^= a2;

     //*** OPT3: END OPTIMIZED VERSION WITH UNROLLED LOOPS AND PRE-COMPUTED mul(2,x) AND  mul(3,x) FUNCTION /**/
      }

      Util.arrayCopyNonAtomic(tempBuffer, (short) 0, a, dataOffset, STATELEN);
    }

    // INVERSE OF MIXING COLUMNS
    private void InvMixColumn(byte a[], short dataOffset) {
      byte i = 0, j = 0;
      // hlp CONTAINS PRECALCULATED EXPRESSION ((j * 4) + dataOffset)
      short hlp = dataOffset;
      // hlp2 CONTAINS PRECALCULATED EXPRESSION (j * 4)
      byte hlp2 = -4;
      byte hlp3 = 0;
      short a0 = 0;
      short a1 = 0;
      short a2 = 0;
      short a3 = 0;

      hlp -= 4;
      for(j = 0; j < BLOCKN; j++) {
        // TIME REDUCING PRECALCULATION
        hlp += 4; hlp2 += 4;
/*
        // TODO: UNROLL THIS LOOP:
        for(i = 0; i < 4; i++) {
          tempBuffer[(byte) (i + hlp2)] = (byte) mul((byte) 0xe, a[(short) (i + hlp)]);
          tempBuffer[(byte) (i + hlp2)] ^= (byte) mul((byte) 0xb, a[(short) (((i + 1) % 4) + hlp)]);
          tempBuffer[(byte) (i + hlp2)] ^= (byte) mul((byte) 0xd, a[(short) (((i + 2) % 4) + hlp)]);
          tempBuffer[(byte) (i + hlp2)] ^= (byte) mul((byte) 0x9, a[(short) (((i + 3) % 4) + hlp)]);
        }
/**/
          // UNROLLED LOOP
          a0 = a[hlp]; a0 = (a0 >= 0) ? a0 : (short) (256 + a0);
          a1 = a[(short) (1 + hlp)]; a1 = (a1 >= 0) ? a1 : (short) (256 + a1);
          a2 = a[(short) (2 + hlp)]; a2 = (a2 >= 0) ? a2 : (short) (256 + a2);
          a3 = a[(short) (3 + hlp)]; a3 = (a3 >= 0) ? a3 : (short) (256 + a3);

          // i == 0
          //tempBuffer[hlp2] = (byte) mul((byte) 0xe, a0);
          tempBuffer[hlp2] = (a0 != 0) ? (byte) Alogtable[(short) ((short) (Logtable[0xe] + Logtable[a0]) % 255)] : (byte) 0;
          //tempBuffer[hlp2] ^= (byte) mul((byte) 0xb, a1);
          tempBuffer[hlp2] ^= (a1 != 0) ? (byte) Alogtable[(short) ((short) (Logtable[0xb] + Logtable[a1]) % 255)] : (byte) 0;
          //tempBuffer[hlp2] ^= (byte) mul((byte) 0xd, a2);
          tempBuffer[hlp2] ^= (a2 != 0) ? (byte) Alogtable[(short) ((short) (Logtable[0xd] + Logtable[a2]) % 255)] : (byte) 0;
          //tempBuffer[hlp2] ^= (byte) mul((byte) 0x9, a3);
          tempBuffer[hlp2] ^= (a3 != 0) ? (byte) Alogtable[(short) ((short) (Logtable[0x9] + Logtable[a3]) % 255)] : (byte) 0;

          // i == 1
          hlp3 = (byte) (hlp2 + 1);
          //tempBuffer[hlp3] = (byte) mul((byte) 0xe, a1);
          tempBuffer[hlp3] = (a1 != 0) ? (byte) Alogtable[(short) ((short) (Logtable[0xe] + Logtable[a1]) % 255)] : (byte) 0;
          //tempBuffer[hlp3] ^= (byte) mul((byte) 0xb, a2);
          tempBuffer[hlp3] ^= (a2 != 0) ? (byte) Alogtable[(short) ((short) (Logtable[0xb] + Logtable[a2]) % 255)] : (byte) 0;
          //tempBuffer[hlp3] ^= (byte) mul((byte) 0xd, a3);
          tempBuffer[hlp3] ^= (a3 != 0) ? (byte) Alogtable[(short) ((short) (Logtable[0xd] + Logtable[a3]) % 255)] : (byte) 0;
          //tempBuffer[hlp3] ^= (byte) mul((byte) 0x9, a0);
          tempBuffer[hlp3] ^= (a0 != 0) ? (byte) Alogtable[(short) ((short) (Logtable[0x9] + Logtable[a0]) % 255)] : (byte) 0;

          // i == 2
          hlp3 = (byte) (hlp2 + 2);
          //tempBuffer[hlp3] = (byte) mul((byte) 0xe, a2);
          tempBuffer[hlp3] = (a2 != 0) ? (byte) Alogtable[(short) ((short) (Logtable[0xe] + Logtable[a2]) % 255)] : (byte) 0;
          //tempBuffer[hlp3] ^= (byte) mul((byte) 0xb, a3);
          tempBuffer[hlp3] ^= (a3 != 0) ? (byte) Alogtable[(short) ((short) (Logtable[0xb] + Logtable[a3]) % 255)] : (byte) 0;
          //tempBuffer[hlp3] ^= (byte) mul((byte) 0xd, a0);
          tempBuffer[hlp3] ^= (a0 != 0) ? (byte) Alogtable[(short) ((short) (Logtable[0xd] + Logtable[a0]) % 255)] : (byte) 0;
          //tempBuffer[hlp3] ^= (byte) mul((byte) 0x9, a1);
          tempBuffer[hlp3] ^= (a1 != 0) ? (byte) Alogtable[(short) ((short) (Logtable[0x9] + Logtable[a1]) % 255)] : (byte) 0;

          // i == 3
          hlp3 = (byte) (hlp2 + 3);
          //tempBuffer[hlp3] = (byte) mul((byte) 0xe, a3);
          tempBuffer[hlp3] = (a3 != 0) ? (byte) Alogtable[(short) ((short) (Logtable[0xe] + Logtable[a3]) % 255)] : (byte) 0;
          //tempBuffer[hlp3] ^= (byte) mul((byte) 0xb, a0);
          tempBuffer[hlp3] ^= (a0 != 0) ? (byte) Alogtable[(short) ((short) (Logtable[0xb] + Logtable[a0]) % 255)] : (byte) 0;
          //tempBuffer[hlp3] ^= (byte) mul((byte) 0xd, a1);
          tempBuffer[hlp3] ^= (a1 != 0) ? (byte) Alogtable[(short) ((short) (Logtable[0xd] + Logtable[a1]) % 255)] : (byte) 0;
          //tempBuffer[hlp3] ^= (byte) mul((byte) 0x9, a2);
          tempBuffer[hlp3] ^= (a2 != 0) ? (byte) Alogtable[(short) ((short) (Logtable[0x9] + Logtable[a2]) % 255)] : (byte) 0;
        // END OF UNROLLED LOOP /**/
      }

      Util.arrayCopyNonAtomic(tempBuffer, (short) 0, a, dataOffset, STATELEN);
    }


     /**
      * Encrypt one block, key schedule must be already processed
      * @param data ... data array to be encrypted
      * @param dataOffset ... start offset in data array
      * @param aesRoundKeys ... scheduled keys for AES (from RoundKeysSchedule() function)
      * @return true if encrypt success, false otherwise.
      */
     public boolean AESEncryptBlock(byte data[], short dataOffset, byte[] aesRoundKeys) {
        byte r;
        byte i;
        short keysOffset = 0;

        // *** ADD ROUND KEY
        //KeyAddition(data, dataOffset, roundKeys, (byte) 0);
        for (i = 0; i < STATELEN; i++) data[(short) (i + dataOffset)] ^= aesRoundKeys[i];

        // N_ROUNDS-1 ORDINARY ROUNDS
        for(r = 1; r < N_ROUNDS; r++) {
            keysOffset += STATELEN;

            // *** SUBSTITUTION
            //Substitution(data, dataOffset, SBox);
            for (i = 0; i < STATELEN; i++) data[(short) (i + dataOffset)] = SBox[((data[(short) (i + dataOffset)] >= 0) ? data[(short) (i + dataOffset)] : (short) (256 + data[(short) (i + dataOffset)]))] ;

            // *** SHIFT ROW
            ShiftRow(data, dataOffset, (byte) 0);

            // *** MIX COLUMN
            MixColumn(data, dataOffset);

            // *** ADD ROUND KEY
            // KeyAddition(data, dataOffset, roundKeys, (short) (r * STATELEN));
            for (i = 0; i < STATELEN; i++) data[(short) (i + dataOffset)] ^= aesRoundKeys[(short) (i + keysOffset)];
        }

        // *** NO MIXCOLUMN IN LAST ROUND

        // *** SUBSTITUTION
        //Substitution(data, dataOffset, SBox);
        for (i = 0; i < STATELEN; i++) data[(short) (i + dataOffset)] = SBox[((data[(short) (i + dataOffset)] >= 0) ? data[(short) (i + dataOffset)] : (short) (256 + data[(short) (i + dataOffset)]))] ;

        // *** SHIFT ROW
        ShiftRow(data, dataOffset, (byte) 0);

        // *** ADD ROUND KEY
        //KeyAddition(data, dataOffset, roundKeys, (short) (N_ROUNDS * STATELEN));
        keysOffset += STATELEN;
        for (i = 0; i < STATELEN; i++) data[(short) (i + dataOffset)] ^= aesRoundKeys[(short) (i + keysOffset)];

        return true;
     }

     /**
      * Decrypt one block, key schedule must be already processed
      * @param data
      * @param dataOffset
      * @param aesRoundKeys ... scheduled keys for AES (from RoundKeysSchedule() function)
      * @return true if decrypt success, false otherwise.
      */
     public boolean AESDecryptBlock(byte data[], short dataOffset, byte[] aesRoundKeys) {
        byte  r;
        short i;
        short keysOffset = 0;

        // *** ADD ROUND KEY
        //KeyAddition(data, dataOffset, roundKeys, (short) (N_ROUNDS * STATELEN));
        keysOffset = (short) (N_ROUNDS * STATELEN);
        for (i = 0; i < STATELEN; i++) data[(short) (i + dataOffset)] ^= aesRoundKeys[(short) (i + keysOffset)];

        // *** SHIFT ROW
        ShiftRow(data, dataOffset, (byte) 1);

        // *** SUBSTITUTION
        // Substitution(data, dataOffset, SiBox);
        for (i = 0; i < STATELEN; i++) data[(short) (i + dataOffset)] = SiBox[((data[(short) (i + dataOffset)] >= 0) ? data[(short) (i + dataOffset)] : (short) (256 + data[(short) (i + dataOffset)]))] ;

        for(r = (byte) (N_ROUNDS-1); r > 0; r--) {
            keysOffset -= STATELEN;

            // *** ADD ROUND KEY
            // KeyAddition(data, dataOffset, roundKeys, (short) (r * STATELEN));
            for (i = 0; i < STATELEN; i++) data[(short) (i + dataOffset)] ^= aesRoundKeys[(short) (i + keysOffset)];

            // *** INVERSE MIX COLUMN
            InvMixColumn(data, dataOffset);

            // *** SHIFT ROW
            ShiftRow(data, dataOffset, (byte) 1);

            // *** SUBSTITUTION
            // Substitution(data, dataOffset, SiBox);
            for (i = 0; i < STATELEN; i++) data[(short) (i + dataOffset)] = SiBox[((data[(short) (i + dataOffset)] >= 0) ? data[(short) (i + dataOffset)] : (short) (256 + data[(short) (i + dataOffset)]))] ;
        }

        // *** ADD ROUND KEY
        //KeyAddition(data, dataOffset, roundKeys, (byte) 0);
        for (i = 0; i < STATELEN; i++) data[(short) (i + dataOffset)] ^= aesRoundKeys[i];

        return true;
     }
}