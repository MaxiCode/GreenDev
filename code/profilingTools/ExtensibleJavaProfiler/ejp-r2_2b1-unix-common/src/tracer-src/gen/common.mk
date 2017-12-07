# $Id: common.mk,v 1.16 2005/03/11 14:32:48 vauclair Exp $

# Makefiles that include this makefile must define following mandatory macros:
# - JAVA_HOME
# - OS_SPECIFIC
# - LIB_NAME_PREFIX
# - LIB_NAME_SUFFIX
#
# The following optional macros can also be defined:
# - MODULE_EXT
# - ADDITIONAL_CC_OPTIONS
# - ADDITIONAL_INCLUDES
# - ADDITIONAL_LIBRARIES

CC_SHARED_LIB_OPTIONS=-shared
INCLUDES=-I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/$(OS_SPECIFIC)

include core.mk
