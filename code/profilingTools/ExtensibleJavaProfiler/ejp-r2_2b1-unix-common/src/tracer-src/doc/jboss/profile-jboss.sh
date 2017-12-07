#!/bin/sh

# $Id: profile-jboss.sh,v 1.6 2005/03/24 09:18:44 vauclair Exp $

DIRNAME=`dirname $0`
export EJP_HOME=`cd $DIRNAME/../..; pwd`

LD_LIBRARY_PATH=${EJP_HOME}/lib:${LD_LIBRARY_PATH}
export LD_LIBRARY_PATH

# for MacOSX:
DYLD_LIBRARY_PATH=${EJP_HOME}/lib:${DYLD_LIBRARY_PATH}
export DYLD_LIBRARY_PATH

JAVA_OPTS=-Xruntracer${MODULE_EXT}:config=${EJP_HOME}/doc/jboss/jboss-filter.cfg
export JAVA_OPTS

# ===========================================================
# EDIT THE LINE BELLOW WITH YOUR JBOSS INSTALLATION DIRECTORY
# ===========================================================
~/jboss-4.0.1sp1/bin/run.sh
