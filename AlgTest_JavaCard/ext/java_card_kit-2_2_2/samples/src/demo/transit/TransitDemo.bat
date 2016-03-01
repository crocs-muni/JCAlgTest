@echo off
REM
REM Copyright 2005 Sun Microsystems, Inc. All rights reserved.
REM Use is subject to license terms.
REM

@echo off

setlocal

 if "%JAVA_HOME%" == "" goto warning1
 if "%JC_HOME%" == "" goto warning2

@echo Issue the transit card (Initialize the transit card: load and create the transit applet)

start /B %JC_HOME%\bin\cref -o TransitCard >&2

if "%1" == "-n" goto notransientkey
call %JC_HOME%\bin\apdutool TransitDemo-TransientKey.scr
goto continue
:notransientkey
call %JC_HOME%\bin\apdutool TransitDemo-NoTransientKey.scr

:continue

@echo Credit the transit card account at a POS terminal
start /B %JC_HOME%\bin\cref -i TransitCard -o TransitCard >&2
call POSTerminal.bat -k FFFFFFFFFFFFFFFF -- VERIFY 12345 CREDIT 99 GET_BALANCE

@echo Enter the transit system
start /B %JC_HOME%\bin\cref -i TransitCard -o TransitCard >&2
call TransitTerminal.bat -k FFFFFFFFFFFFFFFF -- PROCESS_ENTRY 999

@echo Exit the transit system
start /B %JC_HOME%\bin\cref -i TransitCard -o TransitCard >&2
call TransitTerminal.bat -k FFFFFFFFFFFFFFFF -- PROCESS_EXIT 10

@echo Check the balance at a POS termminal
start /B %JC_HOME%\bin\cref -i TransitCard -o TransitCard >&2
call POSTerminal.bat -k FFFFFFFFFFFFFFFF -- VERIFY 12345 GET_BALANCE

del /f TransitCard TransitCard

 goto quit

:warning1
 echo Set environment variable JAVA_HOME
 goto quit

:warning2
 echo Set environment variable JC_HOME
 goto quit

:quit
 endlocal

