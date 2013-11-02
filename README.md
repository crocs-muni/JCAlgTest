AlgTest
=======

Automated testing tool for algorithms supported by particular smart card with JavaCard platform. 

Results for (at least) following smartcards are currently in database (http://www.fi.muni.cz/~xsvenda/jcsupport.html):

- Axalto Cyberflex32 , ATR=3B 75 94 00 00 62 02 02 02 01
- Axalto Cyberflex PalmeraV5 , ATR=3B E6 00 00 81 21 45 32 4B 01 01 01 01 7A
- G+D SmartCafe Expert 144k Dual , ATR=3b fd 18 00 00 80 31 fe 45 73 66 74 65 20 63 64 31 34 34 2d 6e 66 d8
- Gemalto TOP IM GXP4 , ATR=3b 7d 94 00 00 80 31 80 65 b0 83 11 d0 a9 83 00 90 00
- Gemalto TwinGCX4 72k PK , ATR=3B 7A 94 00 00 80 65 A2 01 01 01 3D 72 D6 43
- Gemplus GXPE64PK , ATR=3B 7E 94 00 00 80 25 A0 00 00 00 28 56 80 10 21 00 01 08
- Gemplus GXPLiteGeneric , ATR=3B 7D 94 00 00 80 31 80 65 B0 83 01 02 90 83 00 90 00
- Gemplus GXPR3r32 , ATR=3B 7D 94 00 00 80 31 80 65 B0 83 01 02 90 83 00 90 00
- Gemplus GXPR3 , ATR=3B 7B 94 00 00 80 65 B0 83 01 01 74 83 00 90 00
- Infineon JTOPV2 16K , ATR=3B 6D 00 00 80 31 80 65 40 90 86 01 51 83 07 90 00
- Nokia 6131 , ATR=3B 88 80 01 00 73 C8 40 13 00 90 00 71
- NXP JCOP31 , ATR=3B EB 00 00 81 31 20 45 4A 43 4F 50 33 31 33 36 47 44 54 78
- NXP JCOP41 v221 , ATR=3b fa 18 00 00 81 31 fe 45 4a 43 4f 50 34 31 56 32 32 31 9d
-	NXP JCOP CJ2A081 JC222 , ATR=3b f8 18 00 ff 81 31 fe 45 4a 43 4f 50 76 32 34 31 43
-	NXP JCOP CJ3A080v241 , ATR=3B F8 13 00 00 81 31 FE 45 4A 43 4F 50 76 32 34 31 B7
-	NXP JCOP CJ3A081 JC222 , ATR=3b fa 18 00 00 81 31 fe 45 4a 33 41 30 38 31 56 32 34 31 89
-	NXP JCOP J2A080 , ATR=3b f6 18 00 ff 81 31 fe 45 4a 32 41 30 38 30 1b
-	Oberthur CosmoDual72K , ATR=3B 7B 18 00 00 00 31 C0 64 77 E3 03 00 82 90 00
-	Yubikey Neo , ATR=3b fa 13 00 00 81 31 fe 15 59 75 62 69 6b 65 79 4e 45 4f a6

Usage:

1. Download prepared version (AlgTest_JavaCard/AlgTest_***.cap) or compile your own modification of AlgTest applet. In case of use of provided *.cap file, use version that is supported by your card or simply start with version converted with highest version of converter (e.g., AlgTest_v1.1_jc2.2.2.cap) and then use lower version if card refuses to accept this file.    
2. If you are unable to upload the package to card or install it, then see Caveats, use lower version of converter, comment out unsupported classes or compile your own limited version of AlgTest applet.
3. Upload AlgTest package to your smart card and install it - Use uploader supplied by your card vendor (e.g., GPShell, Gemplus RADIII, IBM JCOP or Cyberflex Access Toolkit). Package AID: 6D 79 70 61 63 6B 61 67 31, Applet AID: 6D 79 70 61 63 30 30 30 31. No special installation parameters are given.
4. Run application AlgTestJClient.jar - Choose the target reader for card with uploaded AlgTest applet and let it run. CSV file with values separated by the semicolon is created (AlgTest_ATR.csv).
5. Please consider to send me (petr@svenda.com) your results in case your card is not yet in the database at http://www.fi.muni.cz/~xsvenda/jcsupport.html

See more details at http://www.fi.muni.cz/~xsvenda/jcsupport.html
