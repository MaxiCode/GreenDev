# $Id: x86-win32-mingw.mk,v 1.1 2005/03/11 14:35:11 vauclair Exp $

# Makefiles that include this makefile must define following mandatory macros:
# - JAVA_HOME
#
# The following optional macros can also be defined:
# - MODULE_EXT
# - ADDITIONAL_INCLUDES
# - ADDITIONAL_LIBRARIES

OS_SPECIFIC=win32
LIB_NAME_PREFIX=
LIB_NAME_SUFFIX=.dll

ADDITIONAL_CC_OPTIONS=-D_JNI_IMPLEMENTATION_ -Wl,--kill-at -Wl,--export-all-symbols

include common.mk
