@echo off

rem $Id: run-disabled.bat,v 1.4 2003/08/05 15:30:49 vauclair Exp $

setlocal

set EJP_TRACER_ENABLED=0

call "%~dp0run.bat" %*