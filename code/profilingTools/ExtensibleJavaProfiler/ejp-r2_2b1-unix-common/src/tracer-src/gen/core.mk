# $Id: core.mk,v 1.2 2005/03/19 09:43:47 vauclair Exp $

# Makefiles that include this makefile must define following mandatory macros:
# - LIB_NAME_PREFIX
# - LIB_NAME_SUFFIX
# - INCLUDES
# - CC_SHARED_LIB_OPTIONS
#
# The following optional macros can also be defined:
# - MODULE_EXT
# - ADDITIONAL_CC_OPTIONS
# - ADDITIONAL_INCLUDES
# - ADDITIONAL_LIBRARIES

# Misc notes:
# Line continuations must contain a whitespace before the
# backslash character for gcc-solaris

# ============================================================================
# Constants
# ============================================================================

MODULE_NAME=tracer$(MODULE_EXT)

SRC_DIR=../src/native
LIB_DIR=../lib

OUTPUT=$(LIB_DIR)/$(LIB_NAME_PREFIX)$(MODULE_NAME)$(LIB_NAME_SUFFIX)

CC=g++
CC_OPTIONS=-g -Wall -Werror -pedantic -pedantic-errors -Wno-long-long -O3 $(CC_SHARED_LIB_OPTIONS)
LIBRARIES=

SOURCES= \
	$(SRC_DIR)/ClassloaderOutputFile.cpp \
	$(SRC_DIR)/Filter.cpp \
	$(SRC_DIR)/Globals.cpp \
	$(SRC_DIR)/EjpException.cpp \
	$(SRC_DIR)/InputFile.cpp \
	$(SRC_DIR)/MonitorGuard.cpp \
	$(SRC_DIR)/OutputFile.cpp \
	$(SRC_DIR)/ShutdownGuard.cpp \
	$(SRC_DIR)/ThreadEventsOutputFile.cpp \
	$(SRC_DIR)/Tracer.cpp

HEADERS= \
	$(SRC_DIR)/ClassloaderOutputFile.hpp \
	$(SRC_DIR)/ejp_tracer_TracerAPI.h \
	$(SRC_DIR)/EjpException.hpp \
	$(SRC_DIR)/Filter.hpp \
	$(SRC_DIR)/Globals.hpp \
	$(SRC_DIR)/InputFile.hpp \
	$(SRC_DIR)/MonitorGuard.hpp \
	$(SRC_DIR)/OutputFile.hpp \
	$(SRC_DIR)/ShutdownGuard.hpp \
	$(SRC_DIR)/ThreadEventsOutputFile.hpp \
	$(SRC_DIR)/Tracer.hpp

# ============================================================================
# Rules
# ============================================================================

all: $(OUTPUT)

$(OUTPUT): $(SOURCES) $(HEADERS)
	-mkdir -p $(LIB_DIR)
	$(CC) $(CC_OPTIONS) $(ADDITIONAL_CC_OPTIONS) \
	-I$(SRC_DIR) -I- $(INCLUDES) $(ADDITIONAL_INCLUDES) \
	$(SOURCES) \
	$(LIBRARIES) $(ADDITIONAL_LIBRARIES) \
	-o $@

clean:
	-rm -f $(OUTPUT)
