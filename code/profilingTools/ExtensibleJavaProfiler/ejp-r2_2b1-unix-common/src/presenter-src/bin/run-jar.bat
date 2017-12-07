@echo off

rem $Id: run-jar.bat,v 1.3 2005/02/18 15:12:39 vauclair Exp $

pushd
cd /d %~dp0
java -Xmx1000m -classpath ../lib/presenter.jar ejp.presenter.gui.MainFrame %*
popd