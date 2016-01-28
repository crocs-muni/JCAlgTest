@echo off
REM #
REM # @(#)template.bat	1.7 00/12/19
REM
REM Copyright © 2001 Sun Microsystems, Inc.  All rights reserved.
REM Use is subject to license terms.
REM

if "%OS%" == "Windows_NT" setlocal

if not "%JAVA_HOME%" == "" goto check_tool
	echo Please set the JAVA_HOME environment variable.
	goto end

:check_tool
if not "%JC21_HOME%" == "" goto doit
	echo Please set the JC21_HOME environment variable.
	goto end

:doit
set _CLASSES=%JC21_HOME%\lib\apduio.jar;%JC21_HOME%\lib\apdutool.jar;%JC21_HOME%\lib\jcwde.jar;%JC21_HOME%\lib\converter.jar;%JC21_HOME%\lib\scriptgen.jar;%JC21_HOME%\lib\offcardverifier.jar;%JC21_HOME%\lib\api21.jar;%JC21_HOME%\lib\capdump.jar;%JC21_HOME%\samples\classes;%CLASSPATH%;

%JAVA_HOME%\bin\java -classpath %_CLASSES% com.sun.javacard.jcasm.cap.Main %1 %2 %3 %4 %5 %6 %7 %8 %9
goto end

:end
if "%OS%" == "Windows_NT" endlocal
