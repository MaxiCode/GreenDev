#!/bin/bash

# $Id: test-sample.sh,v 1.1 2005/02/22 10:41:34 vauclair Exp $

#set OLDPWD=%CD%

SAMPLES_ROOT=../samples
PKG=ejp
CLASSNAME=Sample$1
SRC=$SAMPLES_ROOT/$CLASSNAME.java

#rem check command-line arguments
#if "%1" == "" goto syntaxError
#if not exist %SRC% goto notFoundError

# build sample
cd $SAMPLES_ROOT
echo Compiling $PKG.$CLASSNAME...
javac -classpath ../src/java -d . $CLASSNAME.java

cd ../bin

# remove previous files
echo Cleaning...
#call :del *.ejp.gz
#call :del *.ejp
#call :del hs_err_pid*.log

# run tracer
echo Running...
./run-enabled.sh -cp $SAMPLES_ROOT $PKG.$CLASSNAME

