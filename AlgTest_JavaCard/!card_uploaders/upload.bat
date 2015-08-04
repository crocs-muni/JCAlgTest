::TO ADD/CHANGE CARD INSTALLATION SCRIPT, MODIFY ONLY SECTION UPLOAD


@ECHO OFF
CLS
SET THIS_SCRIPT=%0
SET CARD_ATR="%~1"
SET READER_NAME="%~2"

::COMMANDS FOR REMOVE QUOTES FROM VARIABLES (CREATED BY BRADLEY MOUNTFORD)
SETLOCAL ENABLEDELAYEDEXPANSION
SET "CARD_ATR=%CARD_ATR:"=%"
SET "READER_NAME=%READER_NAME:"=%"
FOR /F "TOKENS=* DELIMS= " %%A in ("%CARD_ATR%") do set CARD_ATR=%%A
FOR /L %%A IN (1,1,100) DO IF "!CARD_ATR:~-1!"==" " SET CARD_ATR=!CARD_ATR:~0,-1!
FOR /F "TOKENS=* DELIMS= " %%A in ("%READER_NAME%") do set READER_NAME=%%A
FOR /L %%A IN (1,1,100) DO IF "!READER_NAME:~-1!"==" " SET READER_NAME=!READER_NAME:~0,-1!

::CHECK IF ALL ARGUMENTS ARE SET
IF "%CARD_ATR%"=="" GOTO ERROR
IF "%READER_NAME%"=="" GOTO ERROR
GOTO UPLOAD

:ERROR
ECHO use: %THIS_SCRIPT% card_atr reader_name
EXIT 1 
                    
                    

::UPLOAD SECTION
::YOU CAN USE VARIABLES CARD_ATR AND READER_NAME, ALL ARE WITHOUT QUOTES

:UPLOAD

IF "%CARD_ATR%"=="3b_7a_94_00_00_80_65_a2_01_01_01_3d_72_d6_43" GOTO Gemalto_TOP_IM_GXP4
IF "%CARD_ATR%"=="3b_7d_94_00_00_80_31_80_65_b0_83_11_11_ac_83_00_90_00" GOTO Gemalto_TOP_IM_GXP4 
IF "%CARD_ATR%"=="3b_7e_94_00_00_80_25_a0_00_00_00_28_56_80_10_21_00_01_08" GOTO Gemalto_GXP_E64_PK
IF "%CARD_ATR%"=="3b_fa_18_00_00_81_31_fe_45_4a_33_41_30_38_31_56_32_34_31_89" GOTO NXP_JCOP_CJ3A081

ECHO This script cannot be use with card ATR == %CARD_ATR%
EXIT 2 



::GPSHELL COMMANDS FOR CARDS

::USEABLE FOR TWIN GCX4 72K PK
:Gemalto_TOP_IM_GXP4
    (
    ECHO # Usable for: Gemalto_TOP_IM_GXP4
    ECHO mode_201
    ECHO gemXpressoPro
    ECHO enable_trace
    ECHO establish_context
    ECHO card_connect -reader "%READER_NAME%"
    ECHO select -AID A000000018434D00
    ECHO open_sc -security 1 -keyind 0 -keyver 0 -key 47454d5850524553534f53414d504c45
    ECHO delete -AID 6D7970616330303031
    ECHO delete -AID 6D797061636B616731
    ECHO install -file AlgTest_jc2.1.2.cap -nvDataLimit 2000 -instParam 00 
    ECHO card_disconnect
    ECHO release_context
    ) | GPShell.exe && EXIT 0
    EXIT 3
 
:Gemalto_GXP_E64_PK    
    (
    ECHO # Usable for: Gemalto_GXP_E64_PK
    ECHO mode_201
    ECHO enable_trace
    ECHO establish_context
    ECHO card_connect -reader "%READER_NAME%"
    ECHO select -AID A000000018434D00
    ECHO open_sc -security 1 -keyind 0 -keyver 0 -mac_key 404142434445464748494a4b4c4d4e4f -enc_key 404142434445464748494a4b4c4d4e4f
    ECHO delete -AID 6D7970616330303031
    ECHO delete -AID 6D797061636B616731
    ECHO install -file AlgTest_jc2.1.2.cap -nvDataLimit 2000 -instParam 00 
    ECHO card_disconnect
    ECHO release_context
    ) | GPShell.exe && EXIT 0
    EXIT 3
    
:NXP_JCOP_CJ3A081
    (
    ECHO # Usable for: NXP_JCOP_CJ3A081
    ECHO mode_211
    ECHO enable_trace
    ECHO establish_context
    ECHO card_connect -reader "%READER_NAME%"
    ECHO select -AID a000000003000000
    ECHO open_sc -security 0 -keyind 0 -keyver 0 -mac_key 404142434445464748494a4b4c4d4e4f -enc_key 404142434445464748494a4b4c4d4e4f
    ECHO delete -AID 6D7970616330303031
    ECHO delete -AID 6D797061636B616731
    ECHO install -file AlgTest_jc2.1.2.cap -nvDataLimit 2000 -instParam 00 
    ECHO # select -AID 6D7970616330303031
    ECHO # send_apdu -sc 0 -APDU B060000000 
    ECHO card_disconnect
    ECHO release_context
    ) | GPShell.exe && EXIT 0
    EXIT 3
