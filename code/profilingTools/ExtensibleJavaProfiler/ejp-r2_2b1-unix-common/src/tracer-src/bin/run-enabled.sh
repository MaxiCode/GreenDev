#!/bin/sh

# $Id: run-enabled.sh,v 1.2 2003/07/11 07:46:48 vauclair Exp $

LD_LIBRARY_PATH=../lib:${LD_LIBRARY_PATH}
export LD_LIBRARY_PATH
CLASSPATH=../lib/tracerapi.jar:${CLASSPATH}
export CLASSPATH

java -Xruntracer:enabled=true $*
