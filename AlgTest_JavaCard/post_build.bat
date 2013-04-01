SET PRGMAKER_BASE_PATH=d:\Apps\PRGMaker
SET PROJECT_BASE_PATH=d:\Documents\Develop\AlgTest\
SET GPSHELL_BASE_PATH=d:\Documents\Develop\AlgTest\!card_uploaders\

d:
cd %PRGMAKER_BASE_PATH%
prgmaker.exe /file AlgTest_jc221.pmf /create /exit

copy %PROJECT_BASE_PATH%\dist\AlgTest\javacard\AlgTest.ijc %GPSHELL_BASE_PATH%

cd %GPSHELL_BASE_PATH%
gpshell.exe JCOP41_AlgTestInstall.txt
