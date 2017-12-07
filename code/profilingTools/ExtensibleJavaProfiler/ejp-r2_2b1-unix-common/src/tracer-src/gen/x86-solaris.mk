# $Id: x86-solaris.mk,v 1.2 2005/03/11 14:27:13 vauclair Exp $

# Makefiles that include this makefile must define following mandatory macros:
# - JAVA_HOME
#
# The following optional macros can also be defined:
# - MODULE_EXT
# - ADDITIONAL_INCLUDES
# - ADDITIONAL_LIBRARIES

# -fPIC option avoids lots of "Text relocation remains" errors
# see http://www.scipy.net/pipermail/scipy-dev/2004-November/002512.html

OS_SPECIFIC=solaris
LIB_NAME_PREFIX=lib
LIB_NAME_SUFFIX=.so

ADDITIONAL_CC_OPTIONS=-fPIC -DMUST_DEFINE_SYS_ERR

include common.mk
