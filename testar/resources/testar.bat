@echo off
where java> __javajdkbin.tmp
set /p javajdkbin=<__javajdkbin.tmp
del __javajdkbin.tmp
%javajdkbin% -ea -server -Xmx1g -jar testar.jar
PAUSE