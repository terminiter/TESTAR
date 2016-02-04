@echo off
where java | findstr "jdk1.8" > __javajdkbin.tmp
set /p javajdkbin=<__javajdkbin.tmp
del __javajdkbin.tmp
echo TESTAR requires JDK 1.8 x64, trying to run with:
"%javajdkbin%" -version
"%javajdkbin%" -ea -server -Xmx1g -jar testar.jar
PAUSE