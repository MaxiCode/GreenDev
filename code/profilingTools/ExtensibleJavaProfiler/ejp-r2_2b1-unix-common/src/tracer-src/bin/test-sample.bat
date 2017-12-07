@echo off

rem $Id: test-sample.bat,v 1.5 2005/03/24 09:59:13 vauclair Exp $


setlocal

set OLDPWD=%CD%

set SAMPLES_ROOT=..\samples
set PKG=ejp
set CLASSNAME=Sample%1
set SRC=%SAMPLES_ROOT%\%CLASSNAME%.java

rem check command-line arguments
if "%1" == "" goto syntaxError
if not exist %SRC% goto notFoundError

rem build tracer
cd ..\gen
echo Building EJP-Tracer...
set JAVA_HOME=%JAVA_HOME_U%
make -f x86-win32-cygwin.mk
if errorlevel 1 goto error

rem build samples
cd %SAMPLES_ROOT%
echo Compiling %PKG%.%CLASSNAME%...
javac -classpath ../src/java -d . %CLASSNAME%.java
if errorlevel 1 goto error

cd ..\bin

rem remove previous files
echo Cleaning...
call :del *.ejp.gz
call :del *.ejp
call :del hs_err_pid*.log

rem run tracer
echo Running...
call run-enabled.bat -cp %SAMPLES_ROOT% %PKG%.%CLASSNAME%
goto end

:syntaxError
echo Syntax: test-sample number
echo E.g. test-sample 0
goto error

:notFoundError
echo File %SRC% not found
goto error

:error
echo.
echo Error!

:end
cd %OLDPWD%
goto :eof

rem Procedures

:del
if exist "%1" del "%1"
goto :eof