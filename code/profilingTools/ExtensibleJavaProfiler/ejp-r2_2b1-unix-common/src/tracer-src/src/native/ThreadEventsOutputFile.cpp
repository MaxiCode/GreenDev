/*
  $Id: ThreadEventsOutputFile.cpp,v 1.20 2005/03/20 08:45:21 vauclair Exp $

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

#include "Globals.hpp"

#include "ThreadEventsOutputFile.hpp"

#include <iomanip>
#include <iostream>

#include "EjpException.hpp"

// uncomment to simulate the cause of bug #926898 (JVM does not send exit event for some methods)
//#define DEBUG_SIMULATE_MISSED_EXIT_EVENTS

// TODO move somewhere else?
String const ThreadEventsOutputFile::NAME_BASE("Thread-");
ThreadEventsOutputFile::EventIndex const ThreadEventsOutputFile::NO_EVENT_INDEX = (EventIndex) -1;

jlong const ThreadEventsOutputFile::NO_TIMESTAMP = -1;


ThreadEventsOutputFile::ThreadEventsOutputFile(JNIEnv* envId_)
  : OutputFile(envId_),
  m_ended(false),
  m_threadStartTimestamp(Globals::s_jvmpiInterface->GetCurrentThreadCpuTime()) // TODO remove (unused)
{
  TRACE_ENTER(m_envId, "ThreadEventsOutputFile::ThreadEventsOutputFile");
  
  OStringStream oss;
  oss << "__ejp.tracer.thread-";
  oss << envId_;
  m_lock = Globals::s_jvmpiInterface->RawMonitorCreate((char*) oss.str().c_str());   // TODO use static_cast
  
  m_ignored = false;

  m_currentEventIndex = 0;
  m_threadIndex = (unsigned int) -1;

  // clear stack and push initial value
  m_stack.clear();
  m_stack.push_back(NO_EVENT_INDEX);

  m_timeSpentInProfiler = 0;

  m_callTraceSynchronized = false;

  // update name in superclass
  m_name = getName();

  TRACE_EXIT(m_envId, "ThreadEventsOutputFile::ThreadEventsOutputFile");
}

ThreadEventsOutputFile::~ThreadEventsOutputFile()
{
  TRACE_ENTER(m_envId, "ThreadEventsOutputFile::~ThreadEventsOutputFile");

  LOG_DEBUG_FORCED(m_envId, "Closing thread context " << m_threadName);

  LOG_DEBUG(m_envId, "m_currentEventIndex=" << m_currentEventIndex);

  EJP_ASSERT_IS_LOWER_THAN(m_envId, 0, m_stack.size());

  // unwind event count stack
  while (!m_methodIdStack.empty())
  {
    jmethodID methodId = m_methodIdStack.back();
    LOG_DEBUG_FORCED(m_envId, "Simulating method exit for " << methodId << " " << Globals::s_debug_methodNames[methodId]);
    hme2(methodId, false, NO_TIMESTAMP, false);
  }
  EJP_ASSERT_EQUALS(m_envId, m_stack.size(), 1);

  LOG_DEBUG_FORCED(m_envId, "Total time spent in profiler: "
    << m_timeSpentInProfiler / 1000000L << " ms");


//  monitorGuard.release();
    
  // super dtor is invoked after this one
  
  if (Globals::s_jvmpiInterface != NULL)
  {
    LOG_DEBUG(m_envId, "Destroying lock");
    Globals::s_jvmpiInterface->RawMonitorDestroy(m_lock);  
  }

  TRACE_EXIT(m_envId, "ThreadEventsOutputFile::~ThreadEventsOutputFile");
}

String ThreadEventsOutputFile::getName() const
{
  TRACE_ENTER(m_envId, "ThreadEventsOutputFile::getName()");

  OStringStream oss;
  oss << NAME_BASE;
  oss << std::right << std::setw(4) << std::setfill('0') << m_threadIndex;
  if (!m_threadName.empty())
  {
    oss << '_';
    oss << m_threadName;
  }
  String result = oss.str();

  TRACE_EXIT(m_envId, "ThreadEventsOutputFile::getName()");

  return result;
}

void ThreadEventsOutputFile::setThreadIndex(unsigned int threadIndex_)
{
  m_threadIndex = threadIndex_;

  // update name in superclass
  m_name = getName();
}

void ThreadEventsOutputFile::setThreadName(char const * threadName_)
{
  TRACE_ENTER(m_envId, "ThreadEventsOutputFile::setName");

  m_threadName.assign(threadName_);
  LOG_DEBUG_FORCED(m_envId, "New name: " << threadName_);

  // update name in superclass
  m_name = getName();
  
   /*
  if (m_name.find("RMI TCP Connection") == String::npos)
  {
    m_ignored = true;
    LOG_VERBOSE(m_envId, "Ignoring thread " << m_name); // TODO set level to debug_forced
  }
   */

  TRACE_EXIT(m_envId, "ThreadEventsOutputFile::setName");
}

char const * ThreadEventsOutputFile::getThreadName() const
{
  return m_threadName.c_str();
}

void ThreadEventsOutputFile::addTimeSpentInProfiler(jlong time_)
{
  m_timeSpentInProfiler += time_;
}

jlong ThreadEventsOutputFile::getTimeSpentInProfiler() const
{
  return m_timeSpentInProfiler;
}

void ThreadEventsOutputFile::dumpMethodIdStack() const
{
  LOG_DEBUG(m_envId, "Method ID stack: " << toString(m_methodIdStack));
}

void ThreadEventsOutputFile::dumpEventIndexStack() const
{
  LOG_DEBUG(m_envId, "Event count stack: " << toString(m_stack));
}

template<typename S> String ThreadEventsOutputFile::toString(S const & stack_)
{
  OStringStream oss;
  oss << "[";
  for (typename S::const_iterator it = stack_.begin();
       it != stack_.end(); it++)
  {
    oss << " " << (*it);
  }
  oss << " ]";
  return oss.str();
}

void ThreadEventsOutputFile::getCallTrace(JVMPI_CallTrace& callTrace_) const
{
  TRACE_ENTER(m_envId, "getCallTrace");

  callTrace_.env_id = m_envId;

  // warning: initial depth must be > 1 (otherwise it will hang)
  for (int depth = 16; ; depth = (int) ((float) depth * 1.5f))
  {
    LOG_DEBUG_FORCED(m_envId, "Current depth: " << depth);

    callTrace_.num_frames = -1;
    callTrace_.frames = new JVMPI_CallFrame[depth];
    if (callTrace_.frames == NULL)
    {
      LOG_ERROR(m_envId, "could not allocate the memory for "
        << depth << " stack frames");
      // TODO throw exc
    }
    // TODO auto_ptr for frames

    LOG_DEBUG_FORCED(m_envId, "Getting stack trace");
    Globals::s_jvmpiInterface->GetCallTrace(&callTrace_, depth);

    int frameCount = callTrace_.num_frames;
    LOG_DEBUG_FORCED(m_envId, "Obtained stack frames: " << frameCount);

    if (frameCount < depth)
    {
      LOG_DEBUG_FORCED(m_envId, "Enough stack frames were obtained, exiting");
      break;
    }
    else
    {
      delete [] callTrace_.frames;
      callTrace_.frames = NULL;
    }
  }

  TRACE_EXIT(m_envId, "getCallTrace");
}

void ThreadEventsOutputFile::dumpCallTrace(JVMPI_CallTrace const & callTrace_)
{
  TRACE_ENTER(m_envId, "dumpCallTrace");

  int frameCount = callTrace_.num_frames;

  // dump stack frames
  OStringStream oos;
  oos << "[";
  {
    for (int i = 0; i < frameCount; i++)
    {
      jmethodID methodId = callTrace_.frames[i].method_id;
      oos << " " << methodId;
      oos << " (" << Globals::getMethodName(methodId) << ")";
    }
  }
  oos << " ]";
  LOG_DEBUG_FORCED(m_envId, "JVMPI available stack frames: " << oos.str());

  TRACE_EXIT(m_envId, "dumpCallTrace");
}

void ThreadEventsOutputFile::synchronizeCallTrace3(jmethodID lastFrameToSkip_, bool lock_)
{
  TRACE_ENTER(m_envId, "synchronizeCallTrace3");

  JVMPI_CallTrace callTrace;
  getCallTrace(callTrace);
  dumpCallTrace(callTrace);
  int frameCount = callTrace.num_frames;


  LOG_DEBUG_FORCED(m_envId, "before synch3");
  dumpMethodIdStack();
  dumpEventIndexStack();
   
  // entry:
  // my:  A1 A2 A3
  // ref: A1 A2 A3 B1 B2

  int mysize = m_methodIdStack.size();
  EJP_ASSERT_EQUALS(m_envId, mysize, 0);

  // process call trace frames (which contains callee, followed by callers)

  for (int i = frameCount - 1; i >= 0; i--)
  {
    JVMPI_CallFrame const & frame = callTrace.frames[i];
    jmethodID methodId = frame.method_id;
    
    if (i == 0 && lastFrameToSkip_ != NULL)
    {
      EJP_ASSERT_EQUALS(m_envId, methodId, lastFrameToSkip_);
      LOG_DEBUG(m_envId, "OK: " << methodId << " " << Globals::s_debug_methodNames[methodId]);
    }
    else 
    {
      LOG_DEBUG_FORCED(m_envId, "Manually registering entry event for method "
        << methodId << " (" << Globals::s_debug_methodNames[methodId] << ")");
      hme2(methodId, true, NO_TIMESTAMP, lock_);
    }
  }

  LOG_DEBUG_FORCED(m_envId, "after synch3");
  dumpMethodIdStack();
  dumpEventIndexStack();

  // free call trace
  delete [] callTrace.frames;
  callTrace.frames = NULL;

  TRACE_EXIT(m_envId, "synchronizeCallTrace3");
}

void ThreadEventsOutputFile::synchronizeCallTrace(bool lock_)
{
  TRACE_ENTER(m_envId, "synchronizeCallTrace");

  JVMPI_CallTrace callTrace;
  getCallTrace(callTrace);
  dumpCallTrace(callTrace);
  int frameCount = callTrace.num_frames;


  LOG_DEBUG_FORCED(m_envId, "before synch");
  dumpMethodIdStack();
  dumpEventIndexStack();
   
  // entry:
  // my:  A1 A2 A3
  // ref: A1 A2 A3 B1 B2

  int mysize = m_methodIdStack.size();
  EJP_ASSERT_IS_TRUE(m_envId, mysize <= frameCount);

  // compare common frames
  MethodIdStack::const_iterator it = m_methodIdStack.begin();
  for (int i = mysize - 1; i >= 0; i--)
  {
    LOG_DEBUG_FORCED(m_envId, "Comparing frame #" << i);
    EJP_ASSERT_EQUALS(m_envId, *it, callTrace.frames[i].method_id);
    it++;
  }

  // add remaining frames
  // process call trace frames (which contains callee, followed by callers)

  for (int i = frameCount - 1; i >= mysize; i--)
  {
    JVMPI_CallFrame const & frame = callTrace.frames[i];
    jmethodID methodId = frame.method_id;

    LOG_DEBUG_FORCED(m_envId, "Manually registering entry event for method "
      << methodId);
    Globals::debug_logMethodName(m_envId, methodId);
    hme2(methodId, true, NO_TIMESTAMP, lock_);
  }

  LOG_DEBUG_FORCED(m_envId, "after synch");
  dumpMethodIdStack();
  dumpEventIndexStack();

  // free call trace
  delete [] callTrace.frames;
  callTrace.frames = NULL;

  TRACE_EXIT(m_envId, "synchronizeCallTrace");
}

void ThreadEventsOutputFile::handleMethodEvent(jmethodID methodId_, bool isEntry_, jlong entryTime_)
{
  TRACE_ENTER(m_envId, "handleMethodEvent");
  
#ifdef DEBUG_SIMULATE_MISSED_EXIT_EVENTS
  if (!isEntry_ && (((u4) methodId_ & 0xFF) == 0))
  {
    LOG_DEBUG(m_envId, "TESTS: ignoring method exit " << methodId_);
  }
  else
#endif  
  {
    handleMethodEvent_p(methodId_, isEntry_, entryTime_, true);
  }
  
  TRACE_EXIT(m_envId, "handleMethodEvent");
}

void ThreadEventsOutputFile::handleMethodEvent_p(jmethodID methodId_, bool isEntry_, jlong entryTime_, bool lock_)
{
  TRACE_ENTER(m_envId, "handleMethodEvent_p");



  // 1 - ensure method is defined

#ifndef DEBUG_SKIP_DISABLING_GC
  // protect class object ID
  Globals::s_jvmpiInterface->DisableGC();
#ifdef DEBUG_ENABLE_GC_TRACE
  LOG_DEBUG_FORCED(m_envId, "GC disabled");
#endif
#endif

  {
    jobjectID classId = Globals::s_jvmpiInterface->GetMethodClass(methodId_);

    if (!Globals::containsDefinedClass(m_envId, classId))
    {
      LOG_DEBUG_FORCED(m_envId, "Requesting definition of unknown class " << classId);
     
      // request class definition
      Globals::s_jvmpiInterface->RequestEvent(JVMPI_EVENT_CLASS_LOAD, classId);

      if (!Globals::containsDefinedClass(m_envId, classId))
      {
        LOG_ERROR(m_envId, "RequestEvent failed for definition of class " << classId
          << " (which declares method " << methodId_ << ")");
      }
    }
  }

#ifndef DEBUG_SKIP_DISABLING_GC
  Globals::s_jvmpiInterface->EnableGC();
#ifdef DEBUG_ENABLE_GC_TRACE
  LOG_DEBUG_FORCED(m_envId, "GC enabled");
#endif
#endif



  // 1b - synch call trace if necessary

  if (!m_callTraceSynchronized)
  {
    LOG_DEBUG_FORCED(m_envId, "Synchronizing call stack");
    synchronizeCallTrace3(isEntry_ ? methodId_ : NULL, lock_);
    
    LOG_DEBUG_FORCED(m_envId, "" << this << " Next method to " << (isEntry_ ? "enter" : "exit") 
      << ": " << methodId_ << " " << Globals::s_debug_methodNames[methodId_]);

    m_callTraceSynchronized = true;
  }


  hme2(methodId_, isEntry_, entryTime_, lock_);


  TRACE_EXIT(m_envId, "handleMethodEvent_p");
}

void ThreadEventsOutputFile::hme2(jmethodID methodId_, bool isEntry_, jlong entryTime_, bool lock_)
{
  EJP_ASSERT_IS_LOWER_THAN(m_envId, 0, m_stack.size());

  u4 ejpMethodId = Globals::findDefinedMethod(m_envId, methodId_);
  bool filteredOut = ejpMethodId == FILTERED_OUT;


  //LOG_DEBUG_FORCED(m_envId, "1. filteredOut=" << filteredOut);
  /* TODO reactivate! (deactivated for Catalina bug)
  if (!filteredOut)
  {
    dumpEventIndexStack();

    if (isEntry_)
    {
      filteredOut |= !Globals::isTracingEnabled(m_envId);
    }
    else
    {
      filteredOut |= (m_stack.size() == 1);
    }
  }
  */
  //LOG_DEBUG_FORCED(m_envId, "2. filteredOut=" << filteredOut);


  // -1                                                          m1() {
  // 0 -1                                                          m2() {
  // 0 1 -1                                                          ...
  // 0 2                                                           }
  // 3                                                           }
  //  
  // m_methodIdStack: [ m1 m2 ]
  // m_stack: 

  // 2 - update stacks and get previous event index

  if (!filteredOut)
  {
    LOG_DEBUG(m_envId, "Not filtered out: " << (isEntry_ ? "ENTRY" : "EXIT") << " " << Globals::s_debug_methodNames[methodId_] << " " << methodId_);
    dumpEventIndexStack();
    
    // get previous event count and update stack
    // (previous event index on same stack level)
    EventIndex prevEventIndex = NO_EVENT_INDEX;
    if (isEntry_)
    {
      // push entered method
      m_methodIdStack.push_back(methodId_);
  
      // update event index stack
      prevEventIndex = m_stack.back();
      m_stack.back() = m_currentEventIndex;
      m_stack.push_back(NO_EVENT_INDEX);    
    }
    else
    {
//      if (m_methodIdStack.empty())
//      {
//        LOG_WARNING(m_envId, "Ignoring exit event (entry event not received)"
//          << " for method " << methodId_ << " (" << Globals::s_debug_methodNames[methodId_] << ")");
//        return;
//      }
      
      // loop until method stack end is synchronized (fix for bug #926898)
      for (;;)
      {
        EJP_ASSERT_IS_TRUE(m_envId, !(m_methodIdStack.empty()));
      
        jmethodID expectedMethodId = m_methodIdStack.back();
        if (methodId_ == expectedMethodId)
        {
          break;
        }
        
        // this log cannot be a WARNING, it happens for every System.arrayCopy
        // on ppc-osx2
        LOG_DEBUG_FORCED(m_envId, "Stack synchronisation lost, simulating method "
          << "exit (expected " 
          << expectedMethodId << " " << Globals::s_debug_methodNames[expectedMethodId] << ", now exiting " << methodId_ << " " << Globals::s_debug_methodNames[methodId_] << ")");
          
        hme2(expectedMethodId, false, NO_TIMESTAMP, lock_);
      }
      
      EJP_ASSERT_IS_TRUE(m_envId, !(m_methodIdStack.empty()));
      EJP_ASSERT_EQUALS(m_envId, m_methodIdStack.back(), methodId_);
      m_methodIdStack.pop_back();
  
      EJP_ASSERT_IS_TRUE(m_envId, m_stack.size() > 1);
      m_stack.pop_back();
      prevEventIndex = m_stack.back();
      m_stack.back() = m_currentEventIndex;
    }


    // check size
    // TODO reactivate?
    //  EJP_ASSERT_IS_TRUE(!m_stack.empty());
  
  
    // 3 - write data to file


    jlong timestamp = entryTime_ == NO_TIMESTAMP ? NO_TIMESTAMP : entryTime_ - getTimeSpentInProfiler();

    LOG_DEBUG(m_envId, "Method " << (isEntry_ ? "entry" : "exit") << ": " << methodId_
      << " " << Globals::s_debug_methodNames[methodId_] << " (ts=" << timestamp << ")");

    LOG_DEBUG(m_envId, "m_currentEventIndex=" << m_currentEventIndex << "; prevEventIndex=" << prevEventIndex);

    // another assert
    if (prevEventIndex != NO_EVENT_INDEX)
    {
      EJP_ASSERT_IS_LOWER_THAN(m_envId, prevEventIndex, m_currentEventIndex);
    }

//    if (lock_)
//    {
//      MonitorGuard monitorGuard(m_envId, m_monitor);
//      writeEntry(isEntry_, ejpMethodId, timestamp, prevEventIndex);
//      monitorGuard.release();
//    }
//    else
//    {
      writeEntry(isEntry_, ejpMethodId, timestamp, prevEventIndex);
//    }

    ++m_currentEventIndex;
    EJP_ASSERT_IS_TRUE(m_envId, m_currentEventIndex != 0);
  }  
}

void ThreadEventsOutputFile::writeEntry(bool isEntry_, u4 ejpMethodId_, jlong timestamp_, EventIndex prevEventIndex_)
{
  printU1(((isEntry_) ? ID_METHOD_ENTRY : ID_METHOD_EXIT)); // event id
  printU4(ejpMethodId_); // method id
  printJLong(timestamp_); // time stamp
  printU4(prevEventIndex_); // previous event index  
}

JVMPI_RawMonitor& ThreadEventsOutputFile::getLock()
{
  return m_lock;
}
