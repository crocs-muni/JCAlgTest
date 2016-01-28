JCAlgTest
=======

Automated testing tool for algorithms supported by particular smart card with JavaCard platform. 

*Contribution with results for YOUR card is wanted! (and highly appreciated :))*

1. Upload proper cap file (based on supported JavaCard version) file to your smart card. For uploading, you may use GPShell (http://sourceforge.net/projects/globalplatform/) or GlobalPlatform (https://github.com/martinpaljak/GlobalPlatform) tools. Upload can be done via GPShell scripts that are available for selected cards in AlgTest_JavaCard/!card_uploaders. If unsure about supported version for your card, try to upload one with the highest version (AlgTest_v1.6_jc2.2.2.cap) and if upload fail, use lower version).

2. Run Java application AlgTestJClient ('java -jar AlgTestJClient.jar'). Choose the target reader for card with uploaded AlgTest applet and let it run. CSV file with values separated by the semicolon is created (AlgTest_ATR.csv). 

3. Please consider to send me (petr@svenda.com) your results in case your card is not yet in the database at http://www.fi.muni.cz/~xsvenda/jcsupport.html



Results for (at least) following smartcards are currently in database (http://www.fi.muni.cz/~xsvenda/jcsupport.html):

- Athena IDprotect , ATR=3B D5 18 FF 80 91 FE 1F C3 80 73 C8 21 13 08
- Axalto Cyberflex32 , ATR=3B 75 94 00 00 62 02 02 02 01
- Axalto Cyberflex PalmeraV5 , ATR=3B E6 00 00 81 21 45 32 4B 01 01 01 01 7A
- G+D SmartCafe Expert 144k Dual , ATR=3b fd 18 00 00 80 31 fe 45 73 66 74 65 20 63 64 31 34 34 2d 6e 66 d8 
- G+D SmartCafe Expert 3.2 72K , ATR=3B F7 18 00 00 80 31 FE 45 73 66 74 65 2D 6E 66 C4
- G+D Smart Cafe Expert 4.x V2 , ATR=3b f8 18 00 00 80 31 fe 45 00 73 c8 40 13 00 90 00 92
- Gemalto IDCore 10 , ATR=3b 7d 96 00 00 80 31 80 65 b0 83 11 d0 a9 83 00 90 00 
- Gemalto IDCore 3010 CC , ATR=3b 7d 96 00 00 80 31 80 65 b0 85 02 00 cf 83 01 90 00 
- Gemalto TOP IM GXP4 , ATR=3b 7d 94 00 00 80 31 80 65 b0 83 11 d0 a9 83 00 90 00
- Gemalto TwinGCX4 72k PK , ATR=3B 7A 94 00 00 80 65 A2 01 01 01 3D 72 D6 43 
- Gemplus GXPE64PK , ATR=3B 7E 94 00 00 80 25 A0 00 00 00 28 56 80 10 21 00 01 08 
- Gemplus GXPLiteGeneric , ATR=3B 7D 94 00 00 80 31 80 65 B0 83 01 02 90 83 00 90 00 
- Gemplus GXPR3r32 , ATR=3B 7D 94 00 00 80 31 80 65 B0 83 01 02 90 83 00 90 00
- Gemplus GXPR3 , ATR=3B 7B 94 00 00 80 65 B0 83 01 01 74 83 00 90 00 
- Gemplus GXP R4 72K , ATR=3b 7d 94 00 00 80 31 80 65 b0 83 11 c0 a9 83 00 90 00 
- Infineon CJTOP 80K INF SLJ 52GLA080AL M8.4 , ATR=3b fe 18 00 00 80 31 fe 45 80 31 80 66 40 90 a5 10 2e 10 83 01 90 00 f2 
- Infineon JTOPV2 16K , ATR=3B 6D 00 00 80 31 80 65 40 90 86 01 51 83 07 90 00 
- JavaCOS A22 150K , ATR=3b fc 18 00 00 81 31 80 45 90 67 46 4a 00 68 08 04 00 00 00 00 0e 
- Nokia 6131 , ATR=3B 88 80 01 00 73 C8 40 13 00 90 00 71
- NXP JCOP J2A080 80K , ATR=3b f8 18 00 00 81 31 fe 45 4a 43 4f 50 76 32 34 31 bc 
- NXP JCOP21 v2.4.2R3 , ATR=3b f9 13 00 00 81 31 fe 45 4a 43 4f 50 32 34 32 52 33 a2 
- NXP JCOP10 (DES only version) , ATR=3b e9 00 00 81 31 fe 45 4a 43 4f 50 31 30 56 32 32 a3 
- NXP JCOP31 , ATR=3B EB 00 00 81 31 20 45 4A 43 4F 50 33 31 33 36 47 44 54 78 
- NXP JCOP41 v221 , ATR=3b fa 18 00 00 81 31 fe 45 4a 43 4f 50 34 31 56 32 32 31 9d 
- NXP JCOP 21 V2.2 36K , ATR=3b f9 18 00 00 81 31 fe 45 4a 43 4f 50 32 31 56 32 32 a9
- NXP JCOP 31 V2.2 36K , ATR=3b eb 00 00 81 31 20 45 4a 43 4f 50 33 31 33 36 47 44 54 78 
- NXP JCOP 31 V2.4.1 72K , ATR=3b f8 13 00 00 81 31 fe 45 4a 43 4f 50 76 32 34 31 b7 
- NXP JCOP CJ2A081 JC222 , ATR=3b f8 18 00 ff 81 31 fe 45 4a 43 4f 50 76 32 34 31 43 
- NXP JCOP CJ3A080v241 , ATR=3B F8 13 00 00 81 31 FE 45 4A 43 4F 50 76 32 34 31 B7 
- NXP JCOP CJ3A081 JC222 , ATR=3b fa 18 00 00 81 31 fe 45 4a 33 41 30 38 31 56 32 34 31 89 
- NXP JCOP J2A080 , ATR=3b f6 18 00 ff 81 31 fe 45 4a 32 41 30 38 30 1b 
- NXP JCOP J2D081 , ATR=3b f9 18 00 00 81 31 fe 45 4a 32 44 30 38 31 5f 50 56 b6 
- NXP JCOP J3D081 v242 , ATR=3b f9 13 00 00 81 31 fe 45 4a 43 4f 50 32 34 32 52 32 a3 
- NXP JCOP NXP250A v242r3 , ATR=3b f9 13 00 00 81 31 fe 45 4a 43 4f 50 32 34 32 52 33 a2 
- Oberthur CosmoDual72K , ATR=3B 7B 18 00 00 00 31 C0 64 77 E3 03 00 82 90 00 
- Oberthur Cosmo V7 64K Dual 128K , ATR=3B DB 18 00 80 B1 FE 45 1F 83 00 31 C0 64 C7 FC 10 00 01 90 00 FA 
- Oberthur ID-ONE Cosmo 64 RSA v5.4 , ATR=3b 7b 18 00 00 00 31 c0 64 77 e9 10 00 01 90 00
- Yubikey Neo , ATR=3b fa 13 00 00 81 31 fe 15 59 75 62 69 6b 65 79 4e 45 4f a6 

More detailed usage:

1. Download prepared version (AlgTest_JavaCard/AlgTest_***.cap) or compile your own modification of AlgTest applet. In case of use of provided *.cap file, use version that is supported by your card or simply start with version converted with highest version of converter (e.g., AlgTest_v1.1_jc2.2.2.cap) and then use lower version if card refuses to accept this file.    
2. If you are unable to upload the package to card or install it, then see Caveats, use lower version of converter, comment out unsupported classes or compile your own limited version of AlgTest applet.
3. Upload AlgTest package to your smart card and install it - Use uploader supplied by your card vendor (e.g., GPShell, Gemplus RADIII, IBM JCOP or Cyberflex Access Toolkit). Package AID: 6D 79 70 61 63 6B 61 67 31, Applet AID: 6D 79 70 61 63 30 30 30 31. No special installation parameters are given.
4. Run application AlgTestJClient.jar as 'java -jar AlgTestJClient.jar' - Choose the target reader for card with uploaded AlgTest applet and let it run. CSV file with values separated by the semicolon is created (AlgTest_ATR.csv). 
  
5. Please consider to send me (petr@svenda.com) your results in case your card is not yet in the database at http://www.fi.muni.cz/~xsvenda/jcsupport.html

See more details at http://www.fi.muni.cz/~xsvenda/jcsupport.html
