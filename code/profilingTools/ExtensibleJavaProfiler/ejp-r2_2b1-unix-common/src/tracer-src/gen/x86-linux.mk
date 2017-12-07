# $Id: x86-linux.mk,v 1.1 2005/03/11 14:35:11 vauclair Exp $

# Makefiles that include this makefile must define following mandatory macros:
# - JAVA_HOME
#
# The following optional macros can also be defined:
# - MODULE_EXT
# - ADDITIONAL_CC_OPTIONS
# - ADDITIONAL_INCLUDES
# - ADDITIONAL_LIBRARIES

OS_SPECIFIC=linux
LIB_NAME_PREFIX=lib
LIB_NAME_SUFFIX=.so

include common.mk
