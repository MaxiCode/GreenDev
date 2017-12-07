# $Id: sparc-solaris.mk,v 1.1 2005/03/11 14:35:11 vauclair Exp $

# Makefiles that include this makefile must define following mandatory macros:
# - JAVA_HOME
#
# The following optional macros can also be defined:
# - MODULE_EXT
# - ADDITIONAL_INCLUDES
# - ADDITIONAL_LIBRARIES

# Thanks to Richard Collingridge for suggesting the "-mimpure-text" option

OS_SPECIFIC=solaris
LIB_NAME_PREFIX=lib
LIB_NAME_SUFFIX=.so

ADDITIONAL_CC_OPTIONS=-mimpure-text -DMUST_DEFINE_SYS_ERR

include common.mk
