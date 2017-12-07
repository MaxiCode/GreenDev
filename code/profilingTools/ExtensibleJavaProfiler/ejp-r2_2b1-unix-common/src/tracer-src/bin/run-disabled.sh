#!/bin/sh

# $Id: run-disabled.sh,v 1.2 2003/07/11 07:47:07 vauclair Exp $

LD_LIBRARY_PATH=../lib:${LD_LIBRARY_PATH}
export LD_LIBRARY_PATH
CLASSPATH=../lib/tracerapi.jar:${CLASSPATH}
export CLASSPATH

java -Xruntracer:enabled=false $*
