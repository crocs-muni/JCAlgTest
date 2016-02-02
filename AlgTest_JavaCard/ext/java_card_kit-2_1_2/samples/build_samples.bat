@echo off
REM
REM Copyright © 2001 Sun Microsystems, Inc.  All rights reserved.
REM Use is subject to license terms.
REM

REM Workfile:@(#)build_samples.bat	1.3
REM Version:1.3
REM Modified:03/26/01 13:37:49
REM Original author:  Oleg Danilov

 setlocal

 if "%JAVA_HOME%" == "" goto warning1
 if "%JC21_HOME%" == "" goto warning2

:: Help

 if "%1" == "help" goto help
 if "%1" == "-help" goto help

 cd /d %JC21_HOME%\samples

:: Clean

 if exist classes\nul rmdir /s/q classes
 if exist src\demo\*.txt del src\demo\*.txt
 if exist src\demo\demo2.scr del src\demo\demo2.scr
 if exist src\demo\JavaLoyalty.scr del src\demo\JavaLoyalty.scr
 if exist src\demo\JavaPurse.scr del src\demo\JavaPurse.scr
 if exist src\demo\SampleLibrary.scr del src\demo\SampleLibrary.scr
 if exist src\demo\Wallet.scr del src\demo\Wallet.scr

 if "%1" == "clean" goto quit
 if "%1" == "-clean" goto quit

 set JC_PATH=.;%JC21_HOME%\samples\classes
 set JCFLAGS=-g -d %JC21_HOME%\samples\classes -classpath "%JC_PATH%"

:: Extract classes from api21.jar

 mkdir classes
 cd classes
 %JAVA_HOME%\bin\jar -xvf %JC21_HOME%\lib\api21.jar
 rmdir /s/q META-INF
 cd ..

:: Copy export files

 xcopy /s %JC21_HOME%\api21_export_files\*.* classes\

:: Compile samples

 %JAVA_HOME%\bin\javac %JCFLAGS% src\com\sun\javacard\samples\HelloWorld\*.java
 %JAVA_HOME%\bin\javac %JCFLAGS% src\com\sun\javacard\samples\SampleLibrary\*.java
 %JAVA_HOME%\bin\javac %JCFLAGS% src\com\sun\javacard\samples\JavaLoyalty\*.java
 %JAVA_HOME%\bin\javac %JCFLAGS% src\com\sun\javacard\samples\JavaPurse\*.java
 %JAVA_HOME%\bin\javac %JCFLAGS% src\com\sun\javacard\samples\NullApp\*.java
 %JAVA_HOME%\bin\javac %JCFLAGS% src\com\sun\javacard\samples\Wallet\*.java

:: Convert samples

 cd classes
 call %JC21_HOME%\bin\converter -config ..\src\com\sun\javacard\samples\HelloWorld\HelloWorld.opt
 call %JC21_HOME%\bin\converter -config ..\src\com\sun\javacard\samples\SampleLibrary\SampleLibrary.opt
 call %JC21_HOME%\bin\converter -config ..\src\com\sun\javacard\samples\JavaLoyalty\JavaLoyalty.opt
 call %JC21_HOME%\bin\converter -config ..\src\com\sun\javacard\samples\JavaPurse\JavaPurse.opt
 call %JC21_HOME%\bin\converter -config ..\src\com\sun\javacard\samples\NullApp\NullApp.opt
 call %JC21_HOME%\bin\converter -config ..\src\com\sun\javacard\samples\wallet\Wallet.opt
 cd ..

:: Create SCR for demo2 in cref mode

 cd src\demo
 call %JC21_HOME%\bin\scriptgen -o JavaLoyalty.scr ..\..\classes\com\sun\javacard\samples\JavaLoyalty\javacard\JavaLoyalty.cap
 call %JC21_HOME%\bin\scriptgen -o JavaPurse.scr ..\..\classes\com\sun\javacard\samples\JavaPurse\javacard\JavaPurse.cap
 call %JC21_HOME%\bin\scriptgen -o SampleLibrary.scr ..\..\classes\com\sun\javacard\samples\SampleLibrary\javacard\SampleLibrary.cap
 call %JC21_HOME%\bin\scriptgen -o Wallet.scr ..\..\classes\com\sun\javacard\samples\wallet\javacard\wallet.cap
 copy /b Header.scr+SampleLibrary.scr+JavaLoyalty.scr+JavaPurse.scr+Wallet.scr+AppletTest.scr+Footer.scr demo2.scr
 cd ..\..

 goto quit

:warning1
 echo Set environment variable JAVA_HOME
 goto quit

:warning2
 echo Set environment variable JC21_HOME
 goto quit

:help
 echo Usage: build_samples [options]
 echo Where options include:
 echo        -help     print out this message
 echo        -clean    remove all produced files
 goto quit

:quit
 endlocal
