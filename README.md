JCAlgTest
=========

[![Build status](https://travis-ci.org/crocs-muni/JCAlgTest.svg?branch=master)](https://travis-ci.org/crocs-muni/JCAlgTest) [![Latest release](https://img.shields.io/github/release/crocs-muni/JCAlgTest.svg)](https://github.com/crocs-muni/JCAlgTest/releases/latest)

An automated testing tool for cryptographic algorithms supported by a particular smart card with JavaCard platform. 

*Contribution with results for YOUR card is wanted! (and highly appreciated :))*

The processed data are available at http://jcalgtest.org

## Usage

### 1. Prepare card with testing applet (AlgTest_*.cap)
Upload proper cap file (based on the supported JavaCard version) file to your
   smart card using uploader like [GlobalPlatformPro](https://github.com/martinpaljak/GlobalPlatformPro). 

```
java -jar gp.jar --install AlgTest_v1.7.10_jc305.cap
```

If unsure about the supported version for your card, simply try to upload from the highest version down to the lower one (e.g., start with AlgTest_v1.7.10_jc305.cap, then AlgTest_v1.7.10_jc304.cap, then AlgTest_v1.7.10_jc222.cap). Stop when the card upload succeed.

Check that the applet was correctly uploaded and installed:
```
java -jar gp.jar --list
```

Expected output:
```
ISD: A000000003000000 (OP_READY)
     Parent:  A000000003000000
     From:    A0000000620001
     Privs:   SecurityDomain, CardLock, CardTerminate, CardReset, CVMManagement, TrustedPath, AuthorizedManagement, TokenVerification, GlobalDelete, GlobalLock, GlobalRegistry, FinalApplication, ReceiptGeneration

APP: 4A43416C675465737431 (SELECTABLE)
     Parent:  A000000003000000
     From:    4A43416C6754657374
     Privs:

PKG: 4A43416C6754657374 (LOADED)
     Applet:  4A43416C675465737431
```

### 2. Run data collection application (AlgTestJClient)

```
java -jar AlgTestJClient.jar
```

On Linux you might need to set the smartcardio library, for example use: 
```
java -Dsun.security.smartcardio.library=/usr/lib64/libpcsclite.so.1 -jar AlgTestJClient.jar'
```

Choose the target reader for the card with the uploaded AlgTest applet, select the testing mode (e.g., 1 -> SUPPORTED ALGORITHMS) and let it run. CSV file with values separated by the semicolon is created (card_name_ALGSUPPORT__ATR....csv).

### 3. Contribute your results, please

Please consider to send us (petr@svenda.com) your results in case your card (*.csv and *.log file). The open database benefit both from the cards not yet in an open public database at https://www.fi.muni.cz/~xsvenda/jcalgtest/, but also from the new measurements for the already included ones (set of supported algorithms can expand in later revisions of the same card).


Results for (at least) the following smartcards are currently in the database
(https://www.fi.muni.cz/~xsvenda/jcalgtest/):

```
   c0 ACS ACOSJ (Combi) , ATR=3b 69 00 02 41 43 4f 53 4a 76 31 30 31 (provided by Alexandre Bouvier),
   c1 ACS ACOSJ 40K , ATR=3b 69 00 02 41 43 4f 53 4a 76 31 30 31 (provided by PetrS),
   c2 Athena IDprotect , ATR=3B D5 18 FF 80 91 FE 1F C3 80 73 C8 21 13 08 (provided by Cosmo),
   c3	Athena IDProtect ICFabDate 2015 , ATR=3b d5 18 ff 81 91 fe 1f c3 80 73 c8 21 13 09 (provided by PetrS),
   c4	Axalto Cyberflex32 , ATR=3B 75 94 00 00 62 02 02 02 01 (provided by PetrS),
   c5	Axalto Cyberflex PalmeraV5 , ATR=3B E6 00 00 81 21 45 32 4B 01 01 01 01 7A (provided by PetrS),
   c6	Credentsys Lite , ATR=3b df 95 ff 80 91 fe 1f c3 80 25 a0 00 00 00 68 53 19 00 01 73 c8 21 13 29 (provided by Kate Gray),
   c7	Feitian-FTJCOS ICFabDate 2018 , ATR=3b fc 18 00 00 81 31 80 45 90 67 46 4a 01 00 87 06 00 00 00 00 ea (provided by Toporin),
   c8	Feitian A40 ICFabDate 2018 , ATR=3b 9f 95 81 31 fe 9f 00 66 46 53 05 10 00 ff 71 df 00 00 00 00 00 ec (provided by Radboud University),
   c9	Feitian A40CR ICFabDate 2018 , ATR=3b 9c 95 80 81 1f 03 90 67 46 4a 01 00 41 06 f2 72 7e 00 57,
   c10	Feitian C21C Samsung S3FS91J , ATR=3b fc 18 00 00 81 31 80 45 90 67 46 4a 01 00 05 24 c0 72 7e 00 86 (provided by Thotheolh Tay),
   c11	Feitian eJava Token , ATR=3b fc 18 00 00 81 31 80 45 90 67 46 4a 01 64 2f 70 c1 72 fe e0 fd (provided by Razvan Dragomirescu),
   c12	Feitian Fingerprint card , ATR=3b 61 00 00 80,
   c13	Feitian Java Card D11CR , ATR=3b 6a 00 00 09 44 31 31 43 52 02 00 25 c3 (provided by PetrS),
   c14	Feitian JavaCOS A22 ICFabDate 2015 , ATR=3b fc 18 00 00 81 31 80 45 90 67 46 4a 00 68 08 04 00 00 00 00 0e (provided by Ivo Kubjas and PetrS),
   c15	Feitian JavaCOS A22CR-ECC-SHA-2 ICFabDate 2015 , ATR=3b fc 18 00 00 81 31 80 45 90 67 46 4a 01 00 10 04 f2 72 fe 00 01 (provided by Kenneth Benson),
   c16	Feitian JavaCOS A22CR ICFabDate 2016 084 , ATR=3b 8c 80 01 90 67 46 4a 01 00 25 04 00 00 00 00 d6 (provided by Josh Harvey),
   c17	Feitian JavaCOS A22CR ICFabDate 2016 257 , ATR=3b 9c 95 80 81 1f 03 90 67 46 4a 01 00 35 04 f2 72 fe 00 a1 (provided by PetrS),
   c18	Feitian JavaCOS A40 ICFabDate 2016 201 , ATR=3b fc 18 00 00 81 31 80 45 90 67 46 4a 01 00 20 05 00 00 00 00 4e (provided by PetrS and Keneth Benson),
   c19	Feitian K9 NXPJ3E081 , ATR=3b f9 13 00 00 81 31 fe 45 4a 43 4f 50 32 34 32 52 33 a2 (provided by Thotheolh Tay),
   c20	FeiTian Ltd JavaCard Token V1.0 0 , ATR=3b fc 18 00 00 81 31 80 45 90 67 46 4a 01 01 68 06 00 00 00 00 04 (provided by Thoth Tay),
   c21	G+D Smart Cafe Expert 4.x V2 ICFabDate 2007 079 , ATR=3b f8 18 00 00 80 31 fe 45 00 73 c8 40 13 00 90 00 92 (provided by PetrS), Performance, Graphs
   c22	G+D Smartcafe 6.0 80K ICFabDate 2015 024 , ATR=3b fe 18 00 00 80 31 fe 45 53 43 45 36 30 2d 43 44 30 38 31 2d 6e 46 a9 (provided by PetrS),
   c23	G+D SmartCafe 7.0 215K USB Token S , ATR=3b f9 96 00 00 81 31 fe 45 53 43 45 37 20 0e 00 20 20 28 (provided by PetrS),
   c24	G+D Smartcafe 7.0 , ATR=3b f9 96 00 00 80 31 fe 45 53 43 45 37 20 00 00 20 20 27 (provided by Radboud University),
   c25	G+D SmartCafe Expert 144k Dual , ATR=3b fd 18 00 00 80 31 fe 45 73 66 74 65 20 63 64 31 34 34 2d 6e 66 d8 (provided by Diego NdK),
   c26	G+D Smartcafe Expert 3.2 72K ICFabDate 2003 126 , ATR=3b f7 18 00 00 80 31 fe 45 73 66 74 65 2d 6e 66 c4 (provided by Cosmo and PetrS),
   c27	G+D StarSign Crypto USB token S , ATR=3b f0 96 00 00 81 31 fe 45 6d (provided by Luka Logar),
   c28	Gemalto IDCore 10 , ATR=3b 7d 96 00 00 80 31 80 65 b0 83 11 d0 a9 83 00 90 00 (provided by Martin Paljak),
   c29	Gemalto IDCore 3010 CC , ATR=3b 7d 96 00 00 80 31 80 65 b0 85 02 00 cf 83 01 90 00 (provided by Martin Paljak),
   c30	Gemalto TOP IM GXP4 , ATR=3b 7d 94 00 00 80 31 80 65 b0 83 11 d0 a9 83 00 90 00 (provided by PetrS),
   c31	Gemalto TwinGCX4 72k ICFabDate 2006 005 , ATR=3b 7d 94 00 00 80 31 80 65 b0 83 11 11 ac 83 00 90 00 (provided by PetrS),
   c32	Gemplus GXP R4 72K ICFabDate 2007 291 , ATR=3b 7d 94 00 00 80 31 80 65 b0 83 11 c0 a9 83 00 90 00 (provided by PetrS), Performance, Graphs
   c33	Gemplus GXPE64PK TOP IM GX3 , ATR=3B 7E 94 00 00 80 25 A0 00 00 00 28 56 80 10 21 00 01 08 (provided by PetrS),
   c34	Gemplus GXPLiteGeneric , ATR=3B 7D 94 00 00 80 31 80 65 B0 83 01 02 90 83 00 90 00 (provided by PetrS),
   c35	Gemplus GXPR3 , ATR=3B 7B 94 00 00 80 65 B0 83 01 01 74 83 00 90 00 (provided by PetrS),
   c36	Gemplus GXPR3r32 TOP IS GX3 , ATR=3B 7D 94 00 00 80 31 80 65 B0 83 01 02 90 83 00 90 00 (provided by PetrS),
   c37	Idemia COSMO FLY v5.8 ICFabDate 2016 253 , ATR=3b 8b 80 01 00 31 c0 64 08 44 03 04 00 90 00 44 (provided by Kevin Osborn),
   c38	Infineon CJTOP 80K INF SLJ 52GLA080AL M8.4 ICFabDate 2012 001 , ATR=3b fe 18 00 00 80 31 fe 45 80 31 80 66 40 90 a5 10 2e 10 83 01 90 00 f2 (provided by PetrS), Performance, Graphs
   c39	Infineon jTOP ID SLJ 52GCA150CL ICFabDate 2015 , ATR=3b fe 18 00 00 80 31 fe 45 80 31 80 66 40 90 a5 10 2e 10 83 07 90 00 f4 (provided by Luka Logar),
   c40	Infineon JTOPV2 16K , ATR=3B 6D 00 00 80 31 80 65 40 90 86 01 51 83 07 90 00 (provided by PetrS),
   c41	Infineon SLE78 Universal JCard , ATR=3b fd 96 00 00 81 31 fe 45 53 4c 4a 35 32 47 44 4c 31 32 38 43 52 57 (provided by Till Maas RedTeamPentesting and PetrS),
   c42	Infineon SLJ52GCA150 ICFabDate 2015 , ATR=3b fe 18 00 00 80 31 fe 45 80 31 80 66 40 90 a5 10 2e 10 83 01 90 00 f2 (provided by Toporin),
   c43	JavaCardOS Infineon JC30M48CR , ATR=3b 80 80 01 01 (provided by JavaCardOS and Thotheolh Tay),
   c44	JavaCardOS JC10M24R , ATR=3b 80 80 01 01 (provided by JavaCardOS),
   c45	jCardSim-2.2.1-all , ATR=3B FA 18 00 00 81 31 FE 45 4A 43 4F 50 33 31 56 32 33 32 98 (provided by PetrS),
   c46	jCardSim-2.2.2-all , ATR=3B FA 18 00 00 81 31 FE 45 4A 43 4F 50 33 31 56 32 33 32 98 (provided by PetrS),
   c47	jCardSim-3.0.4-SNAPSHOT , ATR=3B FA 18 00 00 81 31 FE 45 4A 43 4F 50 33 31 56 32 33 32 98 (provided by PetrS),
   c48	jCardSim-3.0.5-SNAPSHOT , ATR=3B FA 18 00 00 81 31 FE 45 4A 43 4F 50 33 31 56 32 33 32 98 (provided by PetrS),
   c49	Nokia 6131 , ATR=3B 88 80 01 00 73 C8 40 13 00 90 00 71 (provided by Hakan Karahan),
   c50	NXP JCOP J2A080 80K ICFabDate 2011 070 , ATR=3b f8 18 00 00 81 31 fe 45 4a 43 4f 50 76 32 34 31 bc (provided by PetrS),
   c51	NXP J2E081 , ATR=3b f9 13 00 00 81 31 fe 45 4a 43 4f 50 32 34 32 52 33 a2 (provided by PetrS),
   c52	NXP J2E145G ICFabDate 2013 025 , ATR=3b f9 13 00 00 81 31 fe 45 4a 43 4f 50 32 34 32 52 33 a2 (provided by PetrS and Lukas Malina),
   c53	NXP J3A080 ICFabDate 2011 035 , ATR=3b f8 13 00 00 81 31 fe 45 4a 43 4f 50 76 32 34 31 b7 (provided by PetrS),
   c54	NXP J3R150 EMV 4K RSA no OKBG no ECC , ATR=3b 6a 00 ff 00 31 c1 73 c8 40 00 00 90 00 (provided by dilucide),
   c55	NXP J3R180 SECID 4K RSA OBKG ECC , ATR=3b d5 18 ff 81 91 fe 1f c3 80 73 c8 21 10 0a (provided by dilucide),
   c56	NXP JCOP10 (DES only version) , ATR=3b e9 00 00 81 31 fe 45 4a 43 4f 50 31 30 56 32 32 a3 (provided by Henrik),
   c57	NXP JCOP3 J3E145 , ATR=3b f9 18 00 00 81 31 fe 45 50 56 5f 4a 33 45 30 38 32 b5 (provided by Anonymous),
   c58	NXP JCOP3 J3H081 EMV ICFabDate 2016 355 , ATR=3b f8 18 00 00 81 31 fe 45 00 73 c8 40 00 00 90 00 80 (provided by Adam Zhang and Richard Mitev),
   c59	NXP JCOP3 J3H081 EMV ICFabDate 2018 , ATR=3b fa 18 00 00 81 31 fe 45 50 56 4a 43 4f 50 33 45 4d 56 94 (provided by Rowland Watkins and PetrS),
   c60	NXP JCOP3 J3H145 (JCOP 3 SECID P60) , ATR=3b 9e 95 81 01 41 4a 43 4f 53 01 45 48 54 01 52 01 56 00 83 (provided by Kenneth Benson),
   c61	NXP JCOP3 J3H145 SECID P60 , ATR=3b 11 95 80 (provided by Luka Logar and Rowland Watkins and PetrS),
   c62	NXP JCOP41 v2.3.1 ICFabDate 2008 , ATR=3b fa 18 00 ff 81 31 fe 45 4a 43 4f 50 34 31 56 32 33 31 63 (provided by Radboud University),
   c63	NXP JCOP41 v221 , ATR=3b fa 18 00 00 81 31 fe 45 4a 43 4f 50 34 31 56 32 32 31 9d (provided by PetrS), Performance, Graphs
   c64	NXP JCOP4 J3R110 , ATR=3b fe 95 00 00 81 31 fe 45 ff 43 52 59 50 54 4e 4f 58 20 43 41 52 44 be (provided by Francesco Gugliuzza and PetrS),
   c65	NXP JCOP4 J3R180 P71 , ATR=3b fa 18 00 ff 10 00 4a 54 61 78 43 6f 72 65 56 31 (provided by PetrS),
   c66	NXP JCOP4 J3R180 SecID Feitian , ATR=3b d5 18 ff 81 91 fe 1f c3 80 73 c8 21 10 0a (provided by PetrS),
   c67	NXP JCOP4 P71 NoECC , ATR=3b ea 00 00 81 31 fe 45 00 31 c1 73 c8 40 00 00 90 00 7a,
   c68	NXP JCOP4 P71D321 , ATR=3b 8a 80 01 50 56 4a 43 4f 50 34 53 49 44 71 (provided by Riley Gall),
   c69	NXP JCOP 10.18 v2.3.1 ICFabDate 2008 163 , ATR=3b fa 13 00 00 81 31 fe 45 4a 43 4f 50 31 30 56 32 33 31 93 (provided by PetrS),
   c70	NXP JCOP 21 v2.4.2R3 ICFabDate 2013 025 , ATR=3b f9 13 00 00 81 31 fe 45 4a 43 4f 50 32 34 32 52 33 a2 (provided by PetrS), Performance, Graphs
   c71	NXP JCOP 21 V2.2 36K ICFabDate 2008 015 , ATR=3b f9 18 00 00 81 31 fe 45 4a 43 4f 50 32 31 56 32 32 a9 (provided by PetrS), Performance, Graphs
   c72	NXP JCOP 31 V2.2 36K ICFabDate 2006 306 , ATR=3b eb 00 00 81 31 20 45 4a 43 4f 50 33 31 33 36 47 44 54 78 (provided by PetrS), Performance, Graphs
   c73	NXP JCOP 31 V2.3.2 ICFabDate 2011 016 , ATR=3b 8a 80 01 4a 43 4f 50 33 31 56 32 33 32 7a (provided by Martin Omacka),
   c74	NXP JCOP 31 V2.4.1 72K ICFabDate 2012 240 , ATR=3b f8 13 00 00 81 31 fe 45 4a 43 4f 50 76 32 34 31 b7 (provided by PetrS), Performance, Graphs
   c75	NXP JCOP CJ2A081 JC222 ICFabDate 2012 240 , ATR=3b f8 13 00 00 81 31 fe 45 4a 43 4f 50 76 32 34 31 b7 (provided by PetrS), Performance, Graphs
   c76	NXP JCOP CJ3A080v241 , ATR=3B F8 13 00 00 81 31 FE 45 4A 43 4F 50 76 32 34 31 B7 (provided by Lazuardi Nasution), Performance, Graphs
   c77	NXP JCOP CJ3A081 JC222 , ATR=3b f8 13 00 00 81 31 fe 45 4a 33 41 30 38 31 56 32 34 31 89 (provided by PetrS), Performance, Graphs
   c78	NXP JCOP J2A080 , ATR=3b f6 18 00 ff 81 31 fe 45 4a 32 41 30 38 30 1b (provided by Pierre-d), Performance, Graphs
   c79	NXP JCOP J2A080 80K ICFabDate 2011 070 , ATR=3b f8 18 00 00 81 31 fe 45 4a 43 4f 50 76 32 34 31 bc (provided by PetrS),
   c80	NXP JCOP J2A080 ICFabDate 2018 , ATR=3b f9 13 00 00 81 31 fe 45 4a 43 4f 50 76 32 34 31 b7 01 (provided by Toporin),
   c81	NXP JCOP J2D081 80K ICFabDate 2014 126 , ATR=3b f9 18 00 00 81 31 fe 45 4a 32 44 30 38 31 5f 50 56 b6 (provided by PetrS and Paul Crocker), Performance, Graphs
   c82	NXP JCOP J2D081 ICFabDate 2017 , ATR=3b f9 18 00 00 81 31 fe 45 4a 32 44 30 38 31 5f 50 56 b6 (provided by Toporin),
   c83	NXP JCOP J3A040 ICFabDate 2010 071 , ATR=3b 88 80 01 4a 43 4f 50 76 32 34 31 5e (provided by Lukas Malina),
   c84	NXP JCOP J3A081 ICFabDate 2013 , ATR=3b f9 18 00 ff 81 31 fe 45 50 56 5f 4a 33 41 30 38 31 4d (provided by Toporin),
   c85	NXP JCOP J3D081 v242r2 ICFabDate 2012 334 , ATR=3b f9 13 00 00 81 31 fe 45 4a 43 4f 50 32 34 32 52 32 a3 (provided by Martin Paljak and Arnis UT),
   c86	NXP JCOP J3H145G C4 , ATR=3b 94 95 81 01 46 54 56 01 c4 (provided by Jhony Melendez),
   c87	NXP JCOP J3H145G C5 , ATR=3b 94 95 81 01 46 54 56 00 c5 (provided by Jhony Melendez),
   c88	NXP JCOP NXP250A v242r3 , ATR=3b f9 13 00 00 81 31 fe 45 4a 43 4f 50 32 34 32 52 33 a2 (provided by Amir Digar Nemikhandad),
   c89	Oberthur Cosmo V7 64K Dual 128K , ATR=3B DB 18 00 80 B1 FE 45 1F 83 00 31 C0 64 C7 FC 10 00 01 90 00 FA (provided by Cosmo),
   c90	Oberthur Cosmo v7 , ATR=3b db 96 00 80 b1 fe 45 1f 83 00 31 c0 64 c3 08 01 00 01 90 00 95 (provided by PetrS),
   c91	Oberthur CosmoDual72K , ATR=3B 7B 18 00 00 00 31 C0 64 77 E3 03 00 82 90 00 (provided by PetrS),
   c92	Oberthur ID-ONE Cosmo 64 RSA v5.4 ICFabDate 2007 031 , ATR=3b 7b 18 00 00 00 31 c0 64 77 e9 10 00 01 90 00 (provided by PetrS), Performance, Graphs
   c93	PIVKey C910 , ATR=3b fc 18 00 00 81 31 80 45 90 67 46 4a 00 64 16 06 f2 72 7e 00 e0 (provided by Anonymous),
   c94	Softlock SLCOS InfineonSLE78 , ATR=3b 8a 80 01 53 4c 43 4f 53 20 54 3d 43 4c 0d (provided by Ahmed Mamdouh),
   c95	sysmocom sysmoUSIM-SJS1-3FF , ATR=3b 9f 96 80 1f c7 80 31 a0 73 be 21 13 67 43 20 07 18 00 00 01 a5 (provided by promovicz),
   c96	Taisys SIMoME VAULT ICFabDate 2016 , ATR=3b 9f 95 80 3f c7 a0 80 31 e0 73 fa 21 10 63 00 00 00 83 f0 90 00 bb (provided by PetrS),
   c97	Tongxin Microelectronics THD89 T101 , ATR=3b 1b 96 50 6f 6c 61 72 69 73 20 19 01 21 (provided by Thoth Tay),
   c98	Unknown , ATR=3b 68 00 00 00 73 c8 40 12 00 90 00 (provided by Amir Digar Nemikhandad),
   c99	Yubikey Neo (Warning not open JavaCard) , ATR=3b fa 13 00 00 81 31 fe 15 59 75 62 69 6b 65 79 4e 45 4f a6 (provided by Pierre-d and Cosmo),
   c100	[undisclosed1] , ATR=3b xx xx xx xx xx xx xx xx xx xx xx xx xx xx (provided by Cosmo),
   c101	[undisclosed2] , ATR=3b xx xx xx xx xx xx xx xx xx xx xx xx xx xx xx xx xx xx xx xx xx xx xx (provided by Cosmo),
   c102	[undisclosed3] , ATR=3b xx xx xx xx xx xx xx xx xx xx xx xx xx xx xx xx xx xx xx xx xx xx xx (provided by Cosmo),
   c103	[undisclosed4] , ATR=3b xx xx xx xx xx xx xx xx xx xx xx xx xx xx xx xx xx xx xx xx xx xx xx (provided by Cosmo),
   c104	[undisclosed5] , ATR=3b 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 (provided by Metro),
   c105	[undisclosed6] , ATR=3b 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 (provided by Metro),
```

See more details at http://jcalgtest.org
