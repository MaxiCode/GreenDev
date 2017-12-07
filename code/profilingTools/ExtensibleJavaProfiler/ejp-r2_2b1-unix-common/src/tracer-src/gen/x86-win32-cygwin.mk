# $Id: x86-win32-cygwin.mk,v 1.1 2005/03/16 10:19:27 vauclair Exp $

# Makefiles that include this makefile must define following mandatory macros:
# - JAVA_HOME (unix path)
#
# The following optional macros can also be defined:
# - MODULE_EXT
# - ADDITIONAL_INCLUDES
# - ADDITIONAL_LIBRARIES

OS_SPECIFIC=win32
LIB_NAME_PREFIX=
LIB_NAME_SUFFIX=.dll

ADDITIONAL_CC_OPTIONS=-mno-cygwin -D__int64="long long" -Wl,--kill-at -Wl,--export-all-symbols

include common.mk
