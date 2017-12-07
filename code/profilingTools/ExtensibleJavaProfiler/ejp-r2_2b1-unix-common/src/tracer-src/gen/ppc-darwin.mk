# $Id: ppc-darwin.mk,v 1.2 2005/03/20 13:53:23 vauclair Exp $

# The following optional macros can also be defined:
# - MODULE_EXT
# - ADDITIONAL_CC_OPTIONS
# - ADDITIONAL_INCLUDES
# - ADDITIONAL_LIBRARIES

LIB_NAME_PREFIX=lib
LIB_NAME_SUFFIX=.jnilib
CC_SHARED_LIB_OPTIONS=-dynamic -dynamiclib

# update this to the directory containing jvmpi.h
INCLUDES=-I/System/Library/Frameworks/JavaVM.framework/Versions/A/Headers
LIBRARIES=

include core.mk
