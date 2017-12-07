/*
  $Id: Globals.hpp,v 1.8 2005/03/23 09:31:52 vauclair Exp $

  Copyright (C) 2002-2005 Sebastien Vauclair

  This file is part of Extensible Java Profiler.

  Extensible Java Profiler is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  Extensible Java Profiler is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with Extensible Java Profiler; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

#ifndef __Globals_h__
#define __Globals_h__

// TODO this is really hacky
#include "ShutdownGuard.hpp"
static ShutdownGuard s_shutdownGuard;

#include <jvmpi.h>

#include <deque>
#include <set>

#ifdef _MSC_VER // MSC specific
// disable warning about truncated STL indentifier names in debug information
#pragma warning(disable: 4786)
#endif

#include <sstream>

///////////////////////////////////////////////////////////////////////////////
// CUSTOMIZABLE DEFINITIONS
///////////////////////////////////////////////////////////////////////////////

#define VERBOSE // display initialization and cleanup messages
//#define DEBUG_FORCED // display some developer information
//#define DEBUG // display all developer information

//#define DEBUG_MONITORS
//#define DEBUG_WRITTEN_DATA
//#define DEBUG_LOADED_CLASSES

//#define DEBUG_THREAD_CONTEXTS

//#define DEBUG_ENABLE_GC_TRACE
//#define DEBUG_ENABLE_TRACE

//#define DEBUG_SKIP_CALL_TRACE_SYNCH
//#define DEBUG_SKIP_DISABLING_GC
//#define DEBUG_SKIP_OUTPUT

//#define DEBUG_TRACE_STACK

//#define USE_HASHMAPS // use hashmaps instead of maps (currently inefficient because hash = key)


//#define USE_ZLIB // use zlib to compress output files

//#define USE_METHODMAP // use old, <= 1.0beta3 system for entropy reduction


#ifdef _MSC_VER // MSC specific

#define STL_ALLOCATOR(T_) std::allocator< T_ >

#else

// warning: MUST be defined to avoid various containers to collide with each
// others, thus resulting in access violations (at least on Win32-MinGW)

// test for GCC >= 3.4
#if __GNUC__ > 3 || (__GNUC__ == 3 && __GNUC_MINOR__ > 3)

//#error GCC >= 3.4 does not yet have debug allocators (class template named 'rebind' is missing as of 3.4.2)
//#include <ext/debug_allocator.h>
#include <ext/malloc_allocator.h>
//#define STL_ALLOCATOR(T_) __gnu_cxx::debug_allocator< __gnu_cxx::malloc_allocator< T_ > >
#define STL_ALLOCATOR(T_) __gnu_cxx::malloc_allocator< T_ >

#else

#define STL_ALLOCATOR(T_) std::__allocator< T_, std::__debug_alloc< std::__malloc_alloc_template<0> > >

#endif // GCC >= 3.4


#endif



typedef std::basic_string<char, std::char_traits<char>, STL_ALLOCATOR(char) > String;

typedef std::basic_ostringstream<char, std::char_traits<char>, STL_ALLOCATOR(char) > OStringStream;

///////////////////////////////////////////////////////////////////////////////
// DATA TYPES
///////////////////////////////////////////////////////////////////////////////

#ifdef _MSC_VER // MSC specific

typedef unsigned __int8 u1;
typedef unsigned __int16 u2;
typedef unsigned __int32 u4;

#define STRCMP_IC strcmpi

#else // GCC and others

typedef unsigned char u1;
typedef unsigned short u2;
typedef unsigned int u4;

#define STRCMP_IC strcasecmp

#endif


///////////////////////////////////////////////////////////////////////////////
// PROFILES FORMAT
///////////////////////////////////////////////////////////////////////////////


#define ID_VERSION 'v' // file header, since 1.0beta3
#define ID_CLASS_LOAD 'C'
#define ID_CLASS_UNLOAD 'c'
#define ID_METHOD_ENTRY 'M'
#define ID_METHOD_EXIT 'm'

#define ID_ENDIAN 'e' // endian check, added after 2.1beta2


///////////////////////////////////////////////////////////////////////////////
// LOG MACROS
///////////////////////////////////////////////////////////////////////////////

#ifdef _WIN32
#define EOL "\r\n" // DOS (CR+LF)
#else
#define EOL "\n" // Unix, Mac (LF)
#endif

#define NO_CONTEXT -1 // TODO remove
#define NO_ENV_ID 0 // NULL causes arithmetic warnings
#define FILTERED_OUT ((u4) -1)

#define STDOUT_LOCK_NAME "_ejp.tracer.stdout"

#define LOG(LEVEL_, CONTEXT_, MESSAGE_)\
{\
  bool monitorAvailable = Globals::s_jvmpiInterface != NULL\
    && Globals::s_stdoutMonitor != NULL;\
  if (monitorAvailable)\
  {\
    Globals::s_jvmpiInterface->RawMonitorEnter(Globals::s_stdoutMonitor);\
  }\
  JNIEnv const * __envId__ = CONTEXT_;/* TODO remove */\
  std::cout << "ejp> [" LEVEL_ "] (" << __envId__ << ")"\
    << (CONTEXT_ == NO_ENV_ID ? "\t\t" : "\t")\
    << MESSAGE_ << EOL << std::flush;\
  if (monitorAvailable)\
  {\
    Globals::s_jvmpiInterface->RawMonitorExit(Globals::s_stdoutMonitor);\
  }\
}

#define LOG_INFO(CONTEXT_, MESSAGE_)\
{\
  LOG("I", CONTEXT_, MESSAGE_);\
}

#ifdef VERBOSE
#define LOG_VERBOSE(CONTEXT_, MESSAGE_)\
{\
  LOG("V", CONTEXT_, MESSAGE_);\
}
#else
#define LOG_VERBOSE(CONTEXT_, MESSAGE_)
#endif

#ifdef DEBUG_FORCED
#define LOG_DEBUG_FORCED(CONTEXT_, MESSAGE_)\
{\
  LOG("D", CONTEXT_, MESSAGE_);\
}
#else
#define LOG_DEBUG_FORCED(CONTEXT_, MESSAGE_)
#endif

#ifdef DEBUG
#define LOG_DEBUG(CONTEXT_, MESSAGE_)\
{\
  LOG_DEBUG_FORCED(CONTEXT_, MESSAGE_);\
}
#else
#define LOG_DEBUG(CONTEXT_, MESSAGE_)
#endif

#ifdef DEBUG_MONITORS
#define LOG_DEBUG_MONITOR(CONTEXT_, MESSAGE_)\
{\
  LOG_DEBUG(CONTEXT_, MESSAGE_)\
}
#else
#define LOG_DEBUG_MONITOR(CONTEXT_, MESSAGE_)
#endif

#ifdef DEBUG_LOADED_CLASSES
#define LOG_DEBUG_CLASS_LOAD(CONTEXT_, MESSAGE_)\
{\
  LOG_DEBUG_FORCED(CONTEXT_, MESSAGE_)\
}
#else
#define LOG_DEBUG_CLASS_LOAD(CONTEXT_, MESSAGE_)
#endif

// TODO make disable-able?
#define LOG_WARNING(CONTEXT_, MESSAGE_)\
{\
  LOG("W", CONTEXT_, "Warning: " MESSAGE_);\
}

#define LOG_ERROR(CONTEXT_, MESSAGE_)\
{\
  LOG("E", CONTEXT_, "Error: " MESSAGE_);\
}

///////////////////////////////////////////////////////////////////////////////
// TRACE MACROS
///////////////////////////////////////////////////////////////////////////////

#ifdef DEBUG_ENABLE_TRACE
#define TRACE(CONTEXT_, MESSAGE_)\
{\
  LOG("T", CONTEXT_, MESSAGE_);\
}
#else
#define TRACE(CONTEXT_, MESSAGE_)
#endif

#ifdef DEBUG_TRACE_STACK
#define PUSH_STACK_FRAME(CONTEXT_, MESSAGE_)\
{\
  OStringStream __oss__;\
  __oss__ << "[" << CONTEXT_ << "] " << __FILE__ << ":" << __LINE__;\
  __oss__ << " " << MESSAGE_;\
  Globals::s_stackFrames.push_back(__oss__.str());\
}
#else
#define PUSH_STACK_FRAME(CONTEXT_, MESSAGE_)
#endif

#ifdef DEBUG_TRACE_STACK
#define POP_STACK_FRAME(CONTEXT_, MESSAGE_)\
{\
  Globals::s_stackFrames.pop_back();\
}
#else
#define POP_STACK_FRAME(CONTEXT_, MESSAGE_)
#endif

#define TRACE_ENTER(CONTEXT_, MESSAGE_)\
{\
  TRACE(CONTEXT_, "ENTER " MESSAGE_);\
  PUSH_STACK_FRAME(CONTEXT_, MESSAGE_);\
}

#define TRACE_EXIT(CONTEXT_, MESSAGE_)\
{\
  TRACE(CONTEXT_, "EXIT " MESSAGE_);\
  POP_STACK_FRAME(CONTEXT_, MESSAGE_);\
}

///////////////////////////////////////////////////////////////////////////////
// ASSERT MACROS
///////////////////////////////////////////////////////////////////////////////

#define EJP_ASSERT_IS_TRUE(CONTEXT_, X_)\
{\
  if (!(X_))\
  {\
    OStringStream oss;\
    oss << "Assertion failed - " #X_;\
    throw EjpException(__FILE__, __LINE__, CONTEXT_, oss.str());\
  }\
}

#define EJP_ASSERT_EQUALS(CONTEXT_, X_, Y_)\
{\
  if (X_ != Y_)\
  {\
    OStringStream oss;\
    oss << "Assertion failed - " #X_ " (" << X_ << ") == " #Y_ " ("\
      << Y_ << ")";\
    throw EjpException(__FILE__, __LINE__, CONTEXT_, oss.str());\
  }\
}

#define EJP_ASSERT_IS_LOWER_THAN(CONTEXT_, X_, Y_)\
{\
  if (X_ >= Y_)\
  {\
    OStringStream oss;\
    oss << "Assertion failed - " #X_ " (" << X_ << ") < " #Y_ " ("\
      << Y_ << ")";\
    throw EjpException(__FILE__, __LINE__, CONTEXT_, oss.str());\
  }\
}

#ifdef USE_HASHMAPS

#include <ext/hash_map>

namespace __gnu_cxx
{
template<> struct hash<_jobjectID*>
{
  size_t operator()(_jobjectID* __x) const
  {
    return (size_t) __x;
  }
};
template<> struct hash<_jmethodID*>
{
  size_t operator()(_jmethodID* __x) const
  {
    return (size_t) __x;
  }
};
}

#define STL_MAP(KEY_, TP_, ITEM_) __gnu_cxx::hash_map< KEY_, TP_, \
  __gnu_cxx::hash< KEY_ >, std::equal_to< KEY_ >, STL_ALLOCATOR(ITEM_) >

#else

#include <map>
#define STL_MAP(KEY_, TP_, ITEM_) std::map<KEY_, TP_, std::less<KEY_>, STL_ALLOCATOR(ITEM_) >

#endif


#define BREAKPOINT { asm("int3"); }


class Globals
{
public:
  static String const EJP_TRACER_VERSION;

  static JVMPI_RawMonitor s_stdoutMonitor;
  static JVMPI_Interface* s_jvmpiInterface; // JVMPI interface pointer
  
  static bool s_error;

  // TODO rename
  typedef std::pair<jobjectID, u4> ClassMapItem;
  // TODO rename to e.g. ClassMap
  typedef STL_MAP(jobjectID, u4, ClassMapItem) ClassSet;
  static ClassSet s_classSet;
  static JVMPI_RawMonitor s_classSetMonitor;

  typedef std::pair<jmethodID, u4> MethodMapItem;
  typedef STL_MAP(jmethodID, u4, MethodMapItem) MethodMap;
  static MethodMap s_methodMap;
  static JVMPI_RawMonitor s_methodMapMonitor;

  static u4 addDefinedMethod(JNIEnv* envId_, jmethodID methodId_, bool filteredOut_);
  static u4 findDefinedMethod(JNIEnv* envId_, jmethodID methodId_);
  // TODO removeDefinedMethod

  static bool containsDefinedClass(JNIEnv* envId_, jobjectID classId_);
  static u4 addDefinedClass(JNIEnv* envId_, jobjectID classId_, bool filteredOut_);
  static u4 removeDefinedClass(JNIEnv* envId_, jobjectID classId_);

  typedef std::pair<jmethodID, std::string> Debug_MethodNamesItem;
  typedef STL_MAP(jmethodID, std::string, Debug_MethodNamesItem) Debug_MethodNames;
  static Debug_MethodNames s_debug_methodNames;

  // returns "?" iff the method is unknown
  static String getMethodName(jmethodID methodId_);
  static void debug_logMethodName(JNIEnv* envId_, jmethodID methodId_);

  static int const OUTPUT_FILES_BUFFER_SIZE;

  static bool startsWith(JNIEnv* envId_, char const * str1_, char const * str2_);

  static String trimLeft(String const & str_);
  static String trimRight(String const & str_);
  static void remove(String& str_, char const ch_);

  static JVMPI_RawMonitor s_tracingEnabledCounterMonitor;
  static bool isTracingEnabled(JNIEnv* envId_);
  static void addTracingEnabledCounter(JNIEnv* envId_, int val_);
  
#ifdef DEBUG_TRACE_STACK
  typedef std::deque<String, STL_ALLOCATOR(String) > StackFrames;
  static StackFrames s_stackFrames;
#endif

private:
  static u4 s_currentClassId;
  static u4 s_currentMethodId;

  static int s_tracingEnabledCounter;

};

// this is a define to easily display monitor name
#define RAW_MONITOR_ENTER(CONTEXT_, X_)\
{\
  if (Globals::s_jvmpiInterface != NULL)\
  {\
    LOG_DEBUG_MONITOR(CONTEXT_, "ENTER_MONITOR " #X_);\
    Globals::s_jvmpiInterface->RawMonitorEnter(X_);\
  }\
}

// this is a define to easily display monitor name
#define RAW_MONITOR_EXIT(CONTEXT_, X_)\
{\
  if (Globals::s_jvmpiInterface != NULL)\
  {\
    LOG_DEBUG_MONITOR(CONTEXT_, "EXIT_MONITOR " #X_);\
    Globals::s_jvmpiInterface->RawMonitorExit(X_);\
  }\
}

#endif // __Globals_h__
