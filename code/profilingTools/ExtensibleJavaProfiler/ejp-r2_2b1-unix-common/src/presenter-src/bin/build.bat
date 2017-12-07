@echo off
echo Compiling...
if not exist ..\classes\. md ..\classes
javac -g -d ..\classes -classpath ..\src ..\src\ejp\presenter\gui\MainFrame.java ..\src\ejp\presenter\api\filters\*.java
if errorlevel 1 pause