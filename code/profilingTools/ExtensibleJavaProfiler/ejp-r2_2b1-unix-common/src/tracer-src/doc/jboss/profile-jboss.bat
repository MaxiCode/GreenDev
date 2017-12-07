@echo off

rem $Id: profile-jboss.bat,v 1.3 2005/03/10 22:18:05 vauclair Exp $

setlocal

set PATH=%~dp0\..\..\lib;%PATH%
set JAVA_OPTS=-Xruntracer:config=%~dp0jboss-filter.cfg
set NOPAUSE=true

rem ===========================================================
rem EDIT THE LINE BELLOW WITH YOUR JBOSS INSTALLATION DIRECTORY
rem ===========================================================
cd /D c:\Software\jboss-4.0.1sp1\bin

call :del *.ejp
call :del *.ejp.!
call :del hs_err_pid*.log

call run.bat

:end
pause
exit

:del
if exist %1 del %1
if exist %1 (
	echo Unable to delete %1 files
	goto end
)