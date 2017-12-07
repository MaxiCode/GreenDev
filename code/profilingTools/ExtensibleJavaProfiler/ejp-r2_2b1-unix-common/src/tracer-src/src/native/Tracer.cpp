/*
  $Id: Tracer.cpp,v 1.32 2005/03/23 09:31:53 vauclair Exp $

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

/*
  Author: Sebastien Vauclair

  Contributors:
  - Dirk Hoffmann <dirkey@dirkey.de> (STL map based implementation)
  - Sebastien Caille <seb_sky@users.sourceforge.net> (thread-safe contexts)
*/



// TODO rename *Monitor to *Lock?

/*
 * Notes:
 *
 * Avoid using clauses.
 *
 * Do NOT try to request JVMPI_EVENT_THREAD_END to get thread names, as it
 * results in access violations on some rare cases - apparently because GC is
 * disabled (source: JMP).
 *
 * Idea: to avoid locking on known classes, have a lazy local copy on each
 * thread context.
 */

///////////////////////////////////////////////////////////////////////////////
// INCLUDES
///////////////////////////////////////////////////////////////////////////////

#include "Globals.hpp"

#include "ClassloaderOutputFile.hpp"
#include "EjpException.hpp"
#include "Filter.hpp"
#include "MonitorGuard.hpp"
#include "Tracer.hpp"

// STL includes
#include <list>
#include <sstream>

#ifdef USE_METHODMAP
#include <map>
#endif

#include <algorithm>
#include <iostream>
#include <set>
#include <string>

#include "ejp_tracer_TracerAPI.h"

#ifdef USE_ZLIB
#include <zlib.h>
#endif

///////////////////////////////////////////////////////////////////////////////
// DEFINITIONS
///////////////////////////////////////////////////////////////////////////////

// can be used to check that time spent inside event handler is correctly
// ignored
//#define FAKE_DELAY 5000000


// names begin with an underscore, so that monitor contention events
// are not sent to the profiler agent
#define CLASSLOADER_OUTPUT_FILE_LOCK_NAME \
"_ejp.tracer.classloaderOutputFile"

#define THREAD_LOCAL_STORAGE_LOCK_NAME \
"_ejp.tracer.threadLocalStorage"

#ifdef USE_METHODMAP
#define METHOD_MAP_LOCK_NAME "_ejp.tracer.methodMap"
#endif

#define THREAD_CONTEXT_LIST_LOCK_NAME "_ejp.tracer.threadContextList"

// TODO move to Globals?
#define CLASS_SET_LOCK_NAME "_ejp.tracer.classSet"
#define METHOD_MAP_LOCK_NAME "_ejp.tracer.methodMap"
#define TRACING_ENABLED_COUNTER_LOCK_NAME "_ejp.tracer.tracingEnabledCounter"

///////////////////////////////////////////////////////////////////////////////
// GLOBAL VARIABLES
///////////////////////////////////////////////////////////////////////////////

// does not support JNIEXPORT
static bool s_shutdown = false; // set to true in handleJVMShutdown()

// does not support JNIEXPORT
static JVMPI_RawMonitor s_classloaderOutputFileMonitor;

// does not support JNIEXPORT
static ClassloaderOutputFile* s_classloaderOutputFile;

// does not support JNIEXPORT
static JVMPI_RawMonitor s_threadLocalStorageMonitor;

#ifdef USE_METHODMAP
static int s_methodsListLast = -1;

typedef std::map<int, u4, STL_ALLOCATOR(std::pair<int, u4>) > MethodMap;
static MethodMap s_methodMap;
static JVMPI_RawMonitor s_methodMapMonitor;
#endif

typedef std::list<ThreadEventsOutputFile*, STL_ALLOCATOR(ThreadEventsOutputFile*) > ThreadEventsOutputFileList;

/**
 * TODOC
 *
 * Might contain NULL values (those are used to preserve their index).
 */
// does not support JNIEXPORT
static ThreadEventsOutputFileList s_threadContextList;

// does not support JNIEXPORT
static JVMPI_RawMonitor s_threadContextListMonitor;


///////////////////////////////////////////////////////////////////////////////
//
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
//
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// CORE FUNCTIONS IMPLEMENTING TRACER API
///////////////////////////////////////////////////////////////////////////////

inline void enableTracing(JNIEnv* envId_)
{
  TRACE_ENTER(envId_, "enableTracing");

  bool wasEnabled = Globals::isTracingEnabled(envId_);

  Globals::addTracingEnabledCounter(envId_, 1);

  if (!wasEnabled && Globals::isTracingEnabled(envId_))
  {
    LOG_VERBOSE(envId_, "Tracing enabled");
  }

  TRACE_EXIT(envId_, "enableTracing");
}

inline void disableTracing(JNIEnv* envId_)
{
  TRACE_ENTER(envId_, "disableTracing");

  bool wasEnabled = Globals::isTracingEnabled(envId_);

  Globals::addTracingEnabledCounter(envId_, -1);

  if (wasEnabled && !Globals::isTracingEnabled(envId_))
  {
    LOG_VERBOSE(envId_, "Tracing disabled");
  }

  TRACE_EXIT(envId_, "disableTracing");
}

///////////////////////////////////////////////////////////////////////////////
// TRACER API INTERFACE
///////////////////////////////////////////////////////////////////////////////

JNIEXPORT void JNICALL Java_ejp_tracer_TracerAPI_nativeEnableTracing
(JNIEnv* envId_, jclass)
{
  enableTracing(envId_);
}

JNIEXPORT void JNICALL Java_ejp_tracer_TracerAPI_nativeDisableTracing
(JNIEnv* envId_, jclass)
{
  disableTracing(envId_);
}

///////////////////////////////////////////////////////////////////////////////

// TODO move somewhere else?
Filter s_filter;

///////////////////////////////////////////////////////////////////////////////
// SPECIFIC HANDLER - CLASS LOAD
///////////////////////////////////////////////////////////////////////////////

inline void handleClassLoad(JVMPI_Event* event, ThreadEventsOutputFile* threadContext_)
{
  JNIEnv* envId = event->env_id;
  TRACE_ENTER(envId, "handleClassLoad");

  jobjectID classId = event->u.class_load.class_id;

  LOG_DEBUG_CLASS_LOAD(envId, "CLASS LOAD for classId=" << classId << " in thread context "
    << threadContext_);
  LOG_DEBUG_CLASS_LOAD(envId, "  class name = " << event->u.class_load.class_name);

  bool filteredOut = s_filter.isFilteredOut(envId, event->u.class_load.class_name);

  // update s_classSet
  u4 ejpClassId = Globals::addDefinedClass(envId, classId, filteredOut);
  
  // TODO find a way to avoid locking if class is filtered out
  MonitorGuard classloaderOutputFileMonitorGuard(envId, s_classloaderOutputFileMonitor);
  
  if (filteredOut)
  {
    LOG_DEBUG_CLASS_LOAD(envId, "  -> class is filtered out");
  }
  else
  {
    s_classloaderOutputFile->printU1(ID_CLASS_LOAD); // event ID
    s_classloaderOutputFile->printStr(event->u.class_load.class_name); // class name
    s_classloaderOutputFile->printStr(event->u.class_load.source_name); // source name
    s_classloaderOutputFile->printU4(ejpClassId); // class ID
  }

  jint nb = event->u.class_load.num_methods;
  LOG_DEBUG_CLASS_LOAD(envId, "  nb of methods = " << nb);

  if (!filteredOut)
  {
    s_classloaderOutputFile->printEncoded(nb); // number of methods
  }

  for (jint i = 0; i < nb; i++)
  {
    JVMPI_Method meth = event->u.class_load.methods[i];

      LOG_DEBUG_CLASS_LOAD(envId, "    " << meth.method_name << "("
        << meth.method_signature
        << ") = " << meth.method_id);

    OStringStream oss;
    oss << event->u.class_load.class_name << "." << meth.method_name << meth.method_signature;
    Globals::s_debug_methodNames[meth.method_id] = std::string(oss.str().c_str());


    u4 ejpMethodId = Globals::addDefinedMethod(envId, meth.method_id, filteredOut);

    if (!filteredOut)
    {
      s_classloaderOutputFile->printStr(meth.method_name); // method name
      s_classloaderOutputFile->printStr(meth.method_signature); // method signature
      s_classloaderOutputFile->printEncoded(meth.start_lineno + 1); // method start line + 1
      s_classloaderOutputFile->printU4(ejpMethodId); // method id
    }
  } // for (i)
  
  classloaderOutputFileMonitorGuard.release();

  TRACE_EXIT(envId, "handleClassLoad");
}

///////////////////////////////////////////////////////////////////////////////
// SPECIFIC HANDLER - CLASS UNLOAD
///////////////////////////////////////////////////////////////////////////////

inline void handleClassUnload(JVMPI_Event* event)
{
  JNIEnv* envId = event->env_id;
  TRACE_ENTER(envId, "handleClassUnload");

  jobjectID classId = event->u.class_unload.class_id;
  LOG_DEBUG(envId, "CLASS UNLOAD for classId=" << classId);

  // remove class from defined set
  u4 ejpClassId = Globals::removeDefinedClass(envId, classId);
  
  if (ejpClassId != FILTERED_OUT)
  {
    MonitorGuard classloaderOutputFileMonitorGuard(envId, s_classloaderOutputFileMonitor);

    s_classloaderOutputFile->printU1(ID_CLASS_UNLOAD); // event ID
    s_classloaderOutputFile->printU4(ejpClassId); // class ID
    
    classloaderOutputFileMonitorGuard.release();
  }

  TRACE_EXIT(envId, "handleClassUnload");
}

///////////////////////////////////////////////////////////////////////////////
// SPECIFIC HANDLER - JVM SHUT DOWN
///////////////////////////////////////////////////////////////////////////////

inline void handleJVMShutDown(JNIEnv* envId_, jlong entryTime_)
{
  TRACE_ENTER(envId_, "handleJVMShutDown");
  
  LOG_VERBOSE(envId_, "JVM is shutting down");

  disableTracing(envId_);

  Globals::s_jvmpiInterface->DisableEvent(JVMPI_EVENT_GC_START, NULL);
  Globals::s_jvmpiInterface->DisableEvent(JVMPI_EVENT_GC_FINISH, NULL);
  Globals::s_jvmpiInterface->DisableEvent(JVMPI_EVENT_CLASS_LOAD, NULL);
  Globals::s_jvmpiInterface->DisableEvent(JVMPI_EVENT_CLASS_UNLOAD, NULL);
  Globals::s_jvmpiInterface->DisableEvent(JVMPI_EVENT_JVM_SHUT_DOWN, NULL);
  Globals::s_jvmpiInterface->DisableEvent(JVMPI_EVENT_THREAD_START, NULL);
  Globals::s_jvmpiInterface->DisableEvent(JVMPI_EVENT_THREAD_END, NULL);

  MonitorGuard threadContextListMonitorGuard(envId_, s_threadContextListMonitor);

  // purge thread context list
  for (ThreadEventsOutputFileList::iterator it = s_threadContextList.begin();
       it != s_threadContextList.end();
       it++)
  {
    ThreadEventsOutputFile* context = *it;

    if (context == NULL)
    {
      continue;
    }

    LOG_DEBUG_FORCED(envId_, "Deleting thread context " << context->getName());

    /*
    // TODO - this does NOT work (additional stack frames are reported by GetCallTrace (because envId cannot be stored for later use), but result in methodID stack underflows
    LOG_DEBUG_FORCED(envId_, "(before synch)");
    context->dumpMethodIdStack();

    #ifndef DEBUG_SKIP_CALL_TRACE_SYNCH
    synchronizeCallTrace(context, entryTime_, false);
    #endif

    LOG_DEBUG_FORCED(envId_, "(after synch)");
    context->dumpMethodIdStack();
    */

    // acquire lock and release it to ensure no code is running on that thread
    MonitorGuard monitorGuard(envId_, context->getLock());
    monitorGuard.release();
    
    delete context;
  }
  s_threadContextList.clear();
  threadContextListMonitorGuard.release();
  
  s_shutdown = true;  

  // free context list monitor
  Globals::s_jvmpiInterface->RawMonitorDestroy(s_threadContextListMonitor);

#ifdef USE_METHODMAP
  // free method map monitor
  Globals::s_jvmpiInterface->RawMonitorDestroy(s_methodMapMonitor);
#endif

  // free classloader output file monitor
  Globals::s_jvmpiInterface->RawMonitorDestroy(s_classloaderOutputFileMonitor);

  // delete classloader output file
  delete s_classloaderOutputFile;
  s_classloaderOutputFile = NULL;
  
  // free thread local storage monitor
  Globals::s_jvmpiInterface->RawMonitorDestroy(s_threadLocalStorageMonitor);  

  // free class set monitor
  Globals::s_jvmpiInterface->RawMonitorDestroy(Globals::s_classSetMonitor);

  // free method map monitor
  Globals::s_jvmpiInterface->RawMonitorDestroy(Globals::s_methodMapMonitor);

  // free tracing enabled counter monitor
  Globals::s_jvmpiInterface->RawMonitorDestroy(Globals::s_tracingEnabledCounterMonitor);

  // free stdout monitor
  Globals::s_jvmpiInterface->RawMonitorDestroy(Globals::s_stdoutMonitor);

  LOG_VERBOSE(envId_, "Cleanup OK");
  
  TRACE_EXIT(envId_, "handleJVMShutDown");
}

///////////////////////////////////////////////////////////////////////////////
// SPECIFIC HANDLER - METHOD ENTRY / EXIT
///////////////////////////////////////////////////////////////////////////////

inline unsigned int registerThreadEventsOutputFile(ThreadEventsOutputFile* context_)
{
  TRACE_ENTER(context_->getEnvId(), "registerThreadEventsOutputFile");

  MonitorGuard threadContextListMonitorGuard(context_->getEnvId(), s_threadContextListMonitor);

  unsigned int result = s_threadContextList.size();

  s_threadContextList.push_back(context_);

  threadContextListMonitorGuard.release();

  TRACE_EXIT(context_->getEnvId(), "registerThreadEventsOutputFile");

  return result;
}

inline void unregisterThreadEventsOutputFile(ThreadEventsOutputFile* context_)
{
  TRACE_ENTER(context_->getEnvId(), "unregisterThreadEventsOutputFile");

  MonitorGuard threadContextListMonitorGuard(context_->getEnvId(), s_threadContextListMonitor);

  ThreadEventsOutputFileList::iterator it
    = std::find(s_threadContextList.begin(), s_threadContextList.end(),
      context_);

  if (it != s_threadContextList.end())
  {
    // do NOT erase, as this would free the index
    *it = NULL;
  }
  else
  {
    LOG_WARNING(context_->getEnvId(), "thread end for unregistered thread with context "
      << context_);
  }

  threadContextListMonitorGuard.release();

  TRACE_EXIT(context_->getEnvId(), "unregisterThreadEventsOutputFile");
}

///////////////////////////////////////////////////////////////////////////////
// FUNCTION FOR HANDLING EVENT NOTIFICATIONS
///////////////////////////////////////////////////////////////////////////////

ThreadEventsOutputFile* getOrCreateThreadContext(JNIEnv* currentEnvId_, JNIEnv* targetEnvId_)
{
  MonitorGuard monitorGuard(currentEnvId_, s_threadLocalStorageMonitor);
  
    // get or create thread context
    ThreadEventsOutputFile* result = static_cast<ThreadEventsOutputFile*>
      (Globals::s_jvmpiInterface->GetThreadLocalStorage(targetEnvId_));

    if (result == NULL)
    {
      // create a new thread context
      result = new ThreadEventsOutputFile(targetEnvId_);
      if (result == NULL)
      {
        LOG_ERROR(currentEnvId_, "unable to allocate thread context");
        // TODO throw an exc
      }

      // register it in thread-local storage
      Globals::s_jvmpiInterface->SetThreadLocalStorage(targetEnvId_, result);

      // TODO no need to lock (already locked by thread local storage)
      // add it to thread context list
      int index = registerThreadEventsOutputFile(result);

      // set thread index
      result->setThreadIndex(index);

      LOG_DEBUG_FORCED(currentEnvId_, "Created thread context " << index);
    }

    //LOG_DEBUG(envId, "result=" << result);

    EJP_ASSERT_IS_TRUE(currentEnvId_, result != NULL);  
    
    monitorGuard.release();
    
    return result;
}

void notifyEvent2(JVMPI_Event* event_)
{
  TRACE_ENTER(event_->env_id, "notifyEvent2");

  try
  {
    // must be the very first statement
    jlong const entryTime
      = Globals::s_jvmpiInterface->GetCurrentThreadCpuTime();
    LOG_DEBUG(event_->env_id, "entryTime=" << entryTime);

    // get env ID
    JNIEnv * const envId = event_->env_id;

    /*
    //TODO remove and document - causes a mysterious lock

    // check whether thread is suspended (this restrictive mode forbids memory
    // allocation, monitor acquisition, etc.)
    bool const isThreadSuspended =
    (Globals::s_jvmpiInterface->GetThreadStatus(envId) & JVMPI_THREAD_SUSPENDED);
    if (isThreadSuspended)
    {
    LOG_ERROR(envId, "thread " << envId << " is in suspended mode!");
    // we can't safely do anything - skip this method event and
    // skip counting spent time
    return;

    // TODO we don't even have a log of such cases!
    }
    */

    // get event type
    jint eventType = event_->event_type;
    bool isRequestedEvent = (eventType & JVMPI_REQUESTED_EVENT) != 0;
    if (isRequestedEvent)
    {
      eventType ^= JVMPI_REQUESTED_EVENT; // clear this bit
    }

    // get or create thread context
    ThreadEventsOutputFile* threadContext = getOrCreateThreadContext(envId, envId);


    /* TODO remove and document - causes an access violation, apparently because GC is disabled
       OStringStream oss;
       oss << "Requesting start event for unknown thread (eventType="
       << eventType;
       if (eventType == JVMPI_EVENT_METHOD_ENTRY
       || eventType == JVMPI_EVENT_METHOD_EXIT)
       {
       oss << "; methodID=" << event_->u.method.method_id;
       }
       oss << ")";
       LOG_DEBUG_FORCED(envId, (oss.str()));

       #ifndef DEBUG_SKIP_DISABLING_GC
       // protect thread object ID
       Globals::s_jvmpiInterface->DisableGC();
       #ifdef DEBUG_ENABLE_GC_TRACE
       LOG_DEBUG(envId, "GC disabled");
       #endif
       #endif

       jobjectID const threadOId
       = Globals::s_jvmpiInterface->GetThreadObject(envId);
       LOG_DEBUG(envId, "Thread OID: " << threadOId);

       // request thread start event
       jint const errcode = Globals::s_jvmpiInterface
       ->RequestEvent(JVMPI_EVENT_THREAD_START, threadOId);

       LOG_DEBUG(envId, "RequestEvent() returned " << errcode);

       EJP_ASSERT_EQUALS(envId, errcode, JVMPI_SUCCESS);

       #ifndef DEBUG_SKIP_DISABLING_GC
       Globals::s_jvmpiInterface->EnableGC();
       #ifdef DEBUG_ENABLE_GC_TRACE
       LOG_DEBUG(envId, "GC enabled");
       #endif
       #endif

       threadContext = static_cast<ThreadEventsOutputFile*>
       (Globals::s_jvmpiInterface->GetThreadLocalStorage(envId));
    */

    EJP_ASSERT_EQUALS(envId, threadContext->getEnvId(), envId);

    if (isRequestedEvent)
    {
      LOG_DEBUG(envId, "Received a requested event: " << eventType);
    }


    switch (eventType)
    {
    case JVMPI_EVENT_CLASS_LOAD:
      handleClassLoad(event_, threadContext);
      break;

    case JVMPI_EVENT_CLASS_UNLOAD:
      handleClassUnload(event_);
      break;

    case JVMPI_EVENT_JVM_SHUT_DOWN:
      LOG_DEBUG_FORCED(envId, "JVM shutdown");
      LOG_VERBOSE(envId, "JVM is shutting down");      
      //handleJVMShutDown(envId, entryTime);
      break;

    case JVMPI_EVENT_THREAD_START:
      {
        LOG_DEBUG_FORCED(envId, "ENTER case JVMPI_EVENT_THREAD_START");

        // get thread name
        char* threadName = event_->u.thread_start.thread_name;
        LOG_DEBUG_FORCED(envId, "threadName=" << threadName);
        
        JNIEnv* threadEnvId = event_->u.thread_start.thread_env_id;
        ThreadEventsOutputFile* targetThreadContext;
        if (threadEnvId == envId)
        {
          targetThreadContext = threadContext;
        }
        else 
        {
          targetThreadContext = getOrCreateThreadContext(envId, threadEnvId);
          LOG_DEBUG(envId, "Name of thread with env id " << threadEnvId 
            << " (\"" << threadName << "\") has been saved");
        }  
        targetThreadContext->setThreadName(threadName);

        LOG_DEBUG_FORCED(envId, "EXIT case JVMPI_EVENT_THREAD_START");
      }
      break;

    case JVMPI_EVENT_THREAD_END:
    {
      LOG_DEBUG_FORCED(envId, "ENTER case JVMPI_EVENT_THREAD_END");
      
      MonitorGuard monitorGuard(envId, threadContext->getLock());
      
      threadContext->m_ended = true;
      
//          
//      if (!s_shutdown)
//      {
//        Globals::s_jvmpiInterface->SetThreadLocalStorage(envId, NULL);
//        unregisterThreadEventsOutputFile(threadContext);
//
//        LOG_DEBUG_FORCED(envId, "Deleting thread context "
//          << threadContext->getName());
//        delete threadContext;
//        threadContext = NULL;
//      }
//      else
//      {
//        LOG_WARNING(envId, "thread end event after shutdown");
//      }
//      
      monitorGuard.release();

      LOG_DEBUG_FORCED(envId, "EXIT case JVMPI_EVENT_THREAD_END");
    }
      break;

    case JVMPI_EVENT_METHOD_ENTRY:
    case JVMPI_EVENT_METHOD_EXIT:
      {
        if (!threadContext->m_ignored) 
        {       
          bool isMethodEntry = (eventType == JVMPI_EVENT_METHOD_ENTRY);
          jmethodID methodId = event_->u.method.method_id;
  
          // lock before checking s_shutdown to avoid using a thread context
          // while shutdown is in progress
//          MonitorGuard monitorGuard(envId, threadContext->getLock());
          
          if (!threadContext->m_ended)
          {
          
          if (!s_shutdown)
          {        
            threadContext->handleMethodEvent(methodId, isMethodEntry, entryTime);
          }
          else
          {
            LOG_WARNING(envId, "method " << (isMethodEntry ? "entry" : "exit")
              << " event after shutdown (method ID: " << methodId << " - " << Globals::s_debug_methodNames[methodId] << ")"
              << " on thread " << envId << " " << threadContext->getName());
          }
          
          }
          else
          {
            LOG_WARNING(envId, "method " << (isMethodEntry ? "entry" : "exit")
              << " event after thread end (method ID: " << methodId << " - " << Globals::s_debug_methodNames[methodId] << ")"
              << " on thread " << envId << " " << threadContext->getName());
          }
          
//          monitorGuard.release();
        }
      }
      break;

    case JVMPI_EVENT_GC_START:
#ifdef DEBUG_ENABLE_GC_TRACE
      LOG_DEBUG_FORCED(envId, "Ignored JVMPI_EVENT_GC_START");
#endif
      // nop
      break;

    case JVMPI_EVENT_GC_FINISH:
#ifdef DEBUG_ENABLE_GC_TRACE
      LOG_DEBUG_FORCED(envId, "Ignored JVMPI_EVENT_GC_FINISH");
#endif
      // nop
      break;

    default:
      LOG_ERROR(envId, "invalid event type " << eventType);
    }

    // update time spent in profiler
    if (threadContext != NULL && !isRequestedEvent)
    {
      jlong timeSpentInProfiler
        = (Globals::s_jvmpiInterface->GetCurrentThreadCpuTime() - entryTime);
      if (timeSpentInProfiler > 0)
      {
        threadContext->addTimeSpentInProfiler(timeSpentInProfiler);
      }
    }
  }
  catch (EjpException& ee_)
  {
    LOG_ERROR(ee_.getEnvId(), "caught an EJP exception: " << ee_.what());
    Globals::s_jvmpiInterface->ProfilerExit(-1);
  }
  catch (std::exception& e_)
  {
    LOG_ERROR(event_->env_id, "caught a STD exception: " << e_.what());
    Globals::s_jvmpiInterface->ProfilerExit(-2);
  }
  catch (...)
  {
    LOG_ERROR(event_->env_id, "caught an unknown exception (...)");
    Globals::s_jvmpiInterface->ProfilerExit(-3);
  }

  TRACE_EXIT(event_->env_id, "notifyEvent2");
}

///////////////////////////////////////////////////////////////////////////////
// PROFILER AGENT ENTRY POINT
///////////////////////////////////////////////////////////////////////////////

std::string getOption(char* options, std::string optionName)
{
    std::string result;
    size_t optionLen = 0;
    size_t tokenIdx = 0;
    size_t i = 0;

    if (options == NULL) {
        return "";
    }
    std::string optionStr(options);
    optionLen = optionStr.size();

    for (tokenIdx = 0; tokenIdx < optionLen; tokenIdx++) {
        // get the start of an option -- either the first character in the
        // options, or the first character after a comma
        if (tokenIdx == 0 || optionStr[tokenIdx-1] == ',') {
            // check if this is the option we're looking for
            if (optionStr.substr(tokenIdx, optionName.size()) == optionName) {
                if (optionName[optionName.size() - 1] != '=') {
                    // for options that don't have key=value syntax, 
                    // just return 'key'. this will allow tests against NULL
                    // to pass (e.g. if(getOption(options, "valuelesskey") != NULL) {blah}
                    result = optionName;
                    return result;
                }
                else {
                    // parse it and return it.
                    i = tokenIdx + optionName.size();
                    do {
                        if (optionStr[i] == ',') {
                            if (result.size() == 0) {
                                // key=, is bogus. return not found.
                                return "";
                            }
                            else {
                                break;
                            }
                        }
                        result += optionStr[i++];
                    } while (i < optionLen);
                    return result;
                }
            }
        }
    }
    return "";
}


extern "C" {
  JNIEXPORT jint JNICALL JVM_OnLoad(JavaVM* jvm, char* options, void* reserved)
  {
    //BREAKPOINT;
    
    LOG_VERBOSE(NO_ENV_ID, "EJP Tracer [" << Globals::EJP_TRACER_VERSION
      << "] (Tracer.cpp $Revision: 1.32 $)");

    try 
    {
        EJP_ASSERT_EQUALS(NO_ENV_ID, sizeof(jlong), 8);
        EJP_ASSERT_EQUALS(NO_ENV_ID, sizeof(void*), 4);
        EJP_ASSERT_EQUALS(NO_ENV_ID, sizeof(jobjectID), 4);
        EJP_ASSERT_EQUALS(NO_ENV_ID, sizeof(jmethodID), 4);
        EJP_ASSERT_EQUALS(NO_ENV_ID, sizeof(char), 1);
    }
    catch (EjpException& ee_) 
    {
      LOG_ERROR(ee_.getEnvId(), "caught an EJP exception: " << ee_.what());
      return JNI_ERR;
    }

    try
    {
      std::string filename = getOption(options, "config=");
      if (filename != "") {
          s_filter.parseRules(NO_ENV_ID, filename.c_str());    
      }
    }
    catch (EjpException& ee_)
    {
      LOG_ERROR(ee_.getEnvId(), "caught an EJP exception: " << ee_.what());
      return JNI_ERR;
    }
    catch (std::exception& e_)
    {
      LOG_ERROR(NO_ENV_ID, "caught a STD exception: " << e_.what());
      return JNI_ERR;
    }
    catch (...)
    {
      LOG_ERROR(NO_ENV_ID, "caught an unknown exception (...)");
      return JNI_ERR;
    }

    // get jvmpi interface pointer
    if (jvm->GetEnv((void**) &Globals::s_jvmpiInterface, JVMPI_VERSION_1) < 0)
    {
      LOG_ERROR(NO_ENV_ID, "unable to obtain JVMPI interface pointer");
      return JNI_ERR;
    }

    // PARSE OPTIONS

    bool option_enable = true;

    if (options && STRCMP_IC(options, "help") == 0)
    {
      LOG_INFO(NO_ENV_ID,
        "Usage: java -Xruntracer:options <class-to-profile>");
      LOG_INFO(NO_ENV_ID, "Options:");
      LOG_INFO(NO_ENV_ID,
        "  help           Display this message and exit");
      LOG_INFO(NO_ENV_ID,
        "  enabled=false  Disable tracing at startup (default=true)");
      LOG_INFO(NO_ENV_ID,
        "  config=<filename>  Path to config file (default=filter.cfg)");

      Globals::s_jvmpiInterface->ProfilerExit(1 /* error code */);
    }
    else
    {
      // create stdout monitor
      Globals::s_stdoutMonitor
        = Globals::s_jvmpiInterface->RawMonitorCreate(STDOUT_LOCK_NAME);

      // create tracing enabled counter monitor
      Globals::s_tracingEnabledCounterMonitor
        = Globals::s_jvmpiInterface->RawMonitorCreate(TRACING_ENABLED_COUNTER_LOCK_NAME);
        
      // create thread local storage monitor
      s_threadLocalStorageMonitor
        = Globals::s_jvmpiInterface->RawMonitorCreate
        (THREAD_LOCAL_STORAGE_LOCK_NAME);

      // create classloader output file monitor
      s_classloaderOutputFileMonitor
        = Globals::s_jvmpiInterface->RawMonitorCreate
        (CLASSLOADER_OUTPUT_FILE_LOCK_NAME);

      // create classloader output file
      s_classloaderOutputFile = new ClassloaderOutputFile();
      if (s_classloaderOutputFile == NULL)
      {
        LOG_ERROR(NO_ENV_ID, "unable to allocate Classloader output file");
      }

      if (getOption(options, "enabled=") == "false") {
          option_enable = false;
      }

      // initialize jvmpi interface
      Globals::s_jvmpiInterface->NotifyEvent = notifyEvent2;

#ifdef USE_METHODMAP
      // create monitor for method map
      s_methodMapMonitor
        = Globals::s_jvmpiInterface->RawMonitorCreate(METHOD_MAP_LOCK_NAME);
#endif

      // create monitor for thread context list
      s_threadContextListMonitor = Globals::s_jvmpiInterface
        ->RawMonitorCreate(THREAD_CONTEXT_LIST_LOCK_NAME);

      // create monitor for class set
      Globals::s_classSetMonitor
        = Globals::s_jvmpiInterface->RawMonitorCreate(CLASS_SET_LOCK_NAME);

      // create monitor for method map
      Globals::s_methodMapMonitor
        = Globals::s_jvmpiInterface->RawMonitorCreate(METHOD_MAP_LOCK_NAME);

      Globals::s_jvmpiInterface->EnableEvent(JVMPI_EVENT_GC_START, NULL);
      Globals::s_jvmpiInterface->EnableEvent(JVMPI_EVENT_GC_FINISH, NULL);
      Globals::s_jvmpiInterface->EnableEvent(JVMPI_EVENT_CLASS_LOAD, NULL);
      Globals::s_jvmpiInterface->EnableEvent(JVMPI_EVENT_CLASS_UNLOAD, NULL);
      Globals::s_jvmpiInterface->EnableEvent(JVMPI_EVENT_JVM_SHUT_DOWN, NULL);
      Globals::s_jvmpiInterface->EnableEvent(JVMPI_EVENT_THREAD_START, NULL);
      Globals::s_jvmpiInterface->EnableEvent(JVMPI_EVENT_THREAD_END, NULL);
      Globals::s_jvmpiInterface->EnableEvent(JVMPI_EVENT_METHOD_ENTRY, NULL);
      Globals::s_jvmpiInterface->EnableEvent(JVMPI_EVENT_METHOD_EXIT, NULL);

      LOG_VERBOSE(NO_ENV_ID, "Initialization OK");

      if (option_enable)
      {
        enableTracing(NO_ENV_ID); // sets m_prevTime and enables tracing
      }
    }

    return JNI_OK;
  }
}

void handleLibraryShutdown()
{
  if (Globals::s_jvmpiInterface != NULL)
  {
  Globals::s_jvmpiInterface = NULL;
  LOG_VERBOSE(NO_ENV_ID, "Library shutdown...");  
  
  // purge thread context list
  for (ThreadEventsOutputFileList::iterator it = s_threadContextList.begin();
       it != s_threadContextList.end();
       it++)
  {
    ThreadEventsOutputFile* context = *it;

    if (context == NULL)
    {
      continue;
    }

    LOG_DEBUG_FORCED(envId_, "Deleting thread context " << context->getName());

    /*
    // TODO - this does NOT work (additional stack frames are reported by GetCallTrace (because envId cannot be stored for later use), but result in methodID stack underflows
    LOG_DEBUG_FORCED(envId_, "(before synch)");
    context->dumpMethodIdStack();

    #ifndef DEBUG_SKIP_CALL_TRACE_SYNCH
    synchronizeCallTrace(context, entryTime_, false);
    #endif

    LOG_DEBUG_FORCED(envId_, "(after synch)");
    context->dumpMethodIdStack();
    */
   
    delete context;
  }
  s_threadContextList.clear();
    
  // delete classloader output file
  delete s_classloaderOutputFile;
  s_classloaderOutputFile = NULL;
  
  LOG_VERBOSE(NO_ENV_ID, "Cleanup OK");
  }
  else
  {
    LOG_DEBUG(NO_ENV_ID, "multiple calls to handleLibraryShutdown !");
  }
}
