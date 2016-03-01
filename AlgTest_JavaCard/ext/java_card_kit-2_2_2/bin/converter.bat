@echo off
REM
REM Copyright 2005 Sun Microsystems, Inc. All rights reserved.
REM Use is subject to license terms.
REM

if "%OS%" == "Windows_NT" setlocal

if not "%JAVA_HOME%" == "" goto check_tool
	echo Please set the JAVA_HOME environment variable.
	goto end

:check_tool
if not "%JC_HOME%" == "" goto doit
	echo Please set the JC_HOME environment variable.
	goto end

:doit
set _CLASSES=%JC_HOME%\lib\apduio.jar;%JC_HOME%\lib\apdutool.jar;%JC_HOME%\lib\jcwde.jar;%JC_HOME%\lib\converter.jar;%JC_HOME%\lib\scriptgen.jar;%JC_HOME%\lib\offcardverifier.jar;%JC_HOME%\lib\api.jar;%JC_HOME%\lib\installer.jar;%JC_HOME%\lib\capdump.jar;%JC_HOME%\samples\classes;%CLASSPATH%;

%JAVA_HOME%\bin\java -classpath %_CLASSES% com.sun.javacard.converter.Converter %*
goto end

:end
if "%OS%" == "Windows_NT" endlocal
