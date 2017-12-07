/*
  $Id: ThreadEventsOutputFile.hpp,v 1.5 2005/03/18 18:15:33 vauclair Exp $

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

#ifndef __ThreadEventsOutputFile_h__
#define __ThreadEventsOutputFile_h__

#include "Globals.hpp"
#include "OutputFile.hpp"

#include <list>

class ThreadEventsOutputFile : public OutputFile
{
public:
  ThreadEventsOutputFile(JNIEnv* envId_);
  ~ThreadEventsOutputFile();
  
  // must be set in a separate method because index is known after insertion
  void setThreadIndex(unsigned int threadIndex_);
  void setThreadName(char const * threadName_);
  
  /**
   * @param time_ time to add (nanoseconds).
   */
  void addTimeSpentInProfiler(jlong time_);

  /**
   * @return time_ total time for this thread context (nanoseconds).
   */
  jlong getTimeSpentInProfiler() const;  

  void handleMethodEvent(jmethodID methodId_, bool isEntry_, jlong entryTime_);
  
  // TODO refactor to be only a getter for OutputFile::getName() and have an update method on thread index and name
  String getName() const; 
  
  JVMPI_RawMonitor& getLock();   
  
public:  
  bool m_ignored;
  bool m_ended;
  

private:
  typedef std::list< jmethodID, STL_ALLOCATOR(jmethodID) > MethodIdStack;

  typedef u4 EventIndex;

  /**
   * TODOC
   *
   * <p>Must use a vector backend, as default is deque, which does not grow
   * automatically. TODO old comment!
   */
  typedef std::list< EventIndex, STL_ALLOCATOR(EventIndex) > EventIndexStack;
   
  JVMPI_RawMonitor m_lock;

  static String const NAME_BASE;
  static EventIndex const NO_EVENT_INDEX;   
  static jlong const NO_TIMESTAMP;
  
  long const m_threadStartTimestamp;  
  
  MethodIdStack m_methodIdStack;
  EventIndex m_currentEventIndex;
  unsigned int m_threadIndex;
  String m_threadName;
  EventIndexStack m_stack;
  jlong m_timeSpentInProfiler;
  bool m_callTraceSynchronized;

  template<typename S> static String toString(S const & stack_);
  void getCallTrace(JVMPI_CallTrace& callTrace_) const;
  void dumpCallTrace(JVMPI_CallTrace const & callTrace_);
  void synchronizeCallTrace(bool lock_);
  void synchronizeCallTrace3(jmethodID lastFrameToSkip_, bool lock_);
  
  char const * getThreadName() const;  
   
  void dumpMethodIdStack() const;
  void dumpEventIndexStack() const;  

  void handleMethodEvent_p(jmethodID methodId_, bool isEntry_, jlong entryTime_, bool lock_);
  void hme2(jmethodID methodId_, bool isEntry_, jlong entryTime_, bool lock_);
  
  void writeEntry(bool isEntry_, u4 ejpMethodId_, jlong timestamp_, EventIndex prevEventIndex_);
};

#endif // __ThreadEventsOutputFile_h__
