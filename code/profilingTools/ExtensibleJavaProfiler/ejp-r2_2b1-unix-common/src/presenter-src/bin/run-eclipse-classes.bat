@echo off
pushd
cd /d %~dp0
java -Xmx1000m -classpath ../classes-eclipse ejp.presenter.gui.MainFrame %*
popd