@echo off
REM
REM Copyright 2005 Sun Microsystems, Inc. All rights reserved.
REM Use is subject to license terms.
REM

@echo on
@echo This demo requires a cref with com.sun.javacard.samples.transit applet
@echo installed

@echo Start cref before running this demo
@echo off

setlocal

 if "%JAVA_HOME%" == "" goto warning1
 if "%JC_HOME%" == "" goto warning2


set JC_RMICPATH=%JC_HOME%\lib\apduio.jar;%JC_HOME%\lib\base-core.jar;%JC_HOME%\lib\base-opt.jar;%JC_HOME%\lib\jcrmiclientframework.jar;%JC_HOME%\lib\jcclientsamples.jar;%JC_HOME%\lib\apduio.jar;%JC_HOME%\samples\classes

%JAVA_HOME%\bin\java -classpath %JC_RMICPATH%;%CLASSPATH% com.sun.javacard.clientsamples.transit.TransitTerminal %1 %2 %3 %4 %5 %6 %7 %8 %9

 goto quit

:warning1
 echo Set environment variable JAVA_HOME
 goto quit

:warning2
 echo Set environment variable JC_HOME
 goto quit

:quit
 endlocal
