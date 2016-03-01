@echo off
REM
REM Copyright 2005 Sun Microsystems, Inc. All rights reserved.
REM SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
REM

REM Workfile:@(#)build_samples.bat	1.50
REM Version:1.50
REM Modified:01/03/06 19:01:01 

 setlocal

 if "%JAVA_HOME%" == "" goto warning1

:: Help

 if "%1" == "help" goto help
 if "%1" == "-help" goto help

 if "%1" == "clean" goto cleanit
 if "%1" == "-clean" goto cleanit

 ant build_samples
 goto quit

:warning1
 echo Set environment variable JAVA_HOME
 goto quit


:help
 ant samples_usage
 goto quit

:cleanit
 ant clean
 goto quit


:quit
 endlocal
