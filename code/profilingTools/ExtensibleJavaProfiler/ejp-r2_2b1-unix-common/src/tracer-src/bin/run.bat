@echo off

rem $Id: run.bat,v 1.2 2005/03/24 09:58:23 vauclair Exp $

setlocal

set EJP_TRACER_HOME=%~dp0..
call :debug EJP_TRACER_HOME=%EJP_TRACER_HOME%

set PATH=%EJP_TRACER_HOME%\lib;%PATH%
call :debug PATH=%PATH%

set CLASSPATH=%EJP_TRACER_HOME%\lib\tracerapi.jar;%CLASSPATH%
call :debug CLASSPATH=%CLASSPATH%

rem process ENABLED option

set EJP_TRACER_OPT_ENABLED=true

if "%EJP_TRACER_ENABLED%" == "0" goto disabled
goto run

:disabled
set EJP_TRACER_OPT_ENABLED=false
goto run

:run
call :debug EJP_TRACER_OPT_ENABLED=%EJP_TRACER_OPT_ENABLED%
java -Xruntracer:enabled=%EJP_TRACER_OPT_ENABLED%,config=%EJP_TRACER_HOME%\bin\filter.cfg %*
goto :eof

:debug
rem (un)comment the following line to (not) show debug messages
rem echo DEBUG - %*

goto :eof