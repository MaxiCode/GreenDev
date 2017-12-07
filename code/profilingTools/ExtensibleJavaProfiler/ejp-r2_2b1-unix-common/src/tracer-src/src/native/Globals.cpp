/*
  $Id: Globals.cpp,v 1.14 2005/02/18 15:38:45 vauclair Exp $

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

#include <algorithm>
#include <iostream>

#include "MonitorGuard.hpp"

#ifdef DEBUG_TRACE_STACK
Globals::StackFrames Globals::s_stackFrames;
#endif

String const Globals::EJP_TRACER_VERSION = "r2_2b1";
int const Globals::OUTPUT_FILES_BUFFER_SIZE = 4096;

bool Globals::s_error = false;

JVMPI_RawMonitor Globals::s_stdoutMonitor = NULL;
JVMPI_Interface* Globals::s_jvmpiInterface = NULL;

JVMPI_RawMonitor Globals::s_tracingEnabledCounterMonitor = NULL;
int Globals::s_tracingEnabledCounter = 0;

Globals::ClassSet Globals::s_classSet;
u4 Globals::s_currentClassId = 0;
JVMPI_RawMonitor Globals::s_classSetMonitor;

Globals::MethodMap Globals::s_methodMap;
u4 Globals::s_currentMethodId = 0;
JVMPI_RawMonitor Globals::s_methodMapMonitor;

Globals::Debug_MethodNames Globals::s_debug_methodNames;

bool Globals::containsDefinedClass(JNIEnv* envId_, jobjectID classId_)
{
  TRACE_ENTER(envId_, "containsDefinedClass");
  
  MonitorGuard classSetMonitorGuard(envId_, s_classSetMonitor);
  bool result = (s_classSet.count(classId_) > 0);
  classSetMonitorGuard.release();

  TRACE_EXIT(envId_, "containsDefinedClass");

  return result;
}

u4 Globals::addDefinedClass(JNIEnv* envId_, jobjectID classId_, bool filteredOut_)
{
  TRACE_ENTER(envId_, "addDefinedClass");

  MonitorGuard classSetMonitorGuard(envId_, s_classSetMonitor);

  u4 result;
  if (filteredOut_)
  {
    result = FILTERED_OUT;
  }
  else
  {
    result = s_currentClassId++;
  }
  s_classSet[classId_] = result;
  // TODO: check result in an assertion

  classSetMonitorGuard.release();

  LOG_DEBUG(envId_, "Class ID " << classId_ << " -> " << result);

  TRACE_EXIT(envId_, "addDefinedClass");

  return result;
}

u4 Globals::removeDefinedClass(JNIEnv* envId_, jobjectID classId_)
{
  TRACE_ENTER(envId_, "removeDefinedClass");

  MonitorGuard classSetMonitorGuard(envId_, s_classSetMonitor);
 
  ClassSet::iterator it = s_classSet.find(classId_);
  
  u4 result;
  if (it != s_classSet.end()) 
  {
    result = (*it).second;
    s_classSet.erase(it);   
  }
  else
  {
    result = FILTERED_OUT;
  }
    
  classSetMonitorGuard.release();

  TRACE_EXIT(envId_, "removeDefinedClass");

  return result;
}

u4 Globals::addDefinedMethod(JNIEnv* envId_, jmethodID methodId_, bool filteredOut_)
{
  TRACE_ENTER(envId_, "addDefinedMethod");

  MonitorGuard methodMapMonitorGuard(envId_, s_methodMapMonitor);

  u4 result;
  if (filteredOut_)
  {
    result = FILTERED_OUT;
  }
  else
  {
    result = s_currentMethodId++;
  }
  s_methodMap[methodId_] = result;
  // TODO: check result in an assertion

  methodMapMonitorGuard.release();

  LOG_DEBUG(envId_, "Method ID " << methodId_ << " (" << s_debug_methodNames[methodId_] << ") -> " << result);

  TRACE_EXIT(envId_, "addDefinedMethod");

  return result;
}

u4 Globals::findDefinedMethod(JNIEnv* envId_, jmethodID methodId_)
{
  TRACE_ENTER(envId_, "findDefinedMethod");

  MonitorGuard methodMapMonitorGuard(envId_, s_methodMapMonitor);
  u4 result = s_methodMap[methodId_];
  // TODO check result
  methodMapMonitorGuard.release();

  TRACE_EXIT(envId_, "findDefinedMethod");

  return result;
}

String Globals::getMethodName(jmethodID methodId_)
{
  Globals::Debug_MethodNames::iterator it = Globals::s_debug_methodNames.find(methodId_);
  String result;
  if (it == s_debug_methodNames.end())
  {
   result = "?";
  }
  else
  {
   result = String((*it).second.c_str());
  }
  return result;
}

void Globals::debug_logMethodName(JNIEnv* envId_, jmethodID methodId_)
{
  LOG_DEBUG_FORCED(envId_, "Method " << methodId_ << ": " << getMethodName(methodId_));
}

// str1.startsWith(str2_)
bool Globals::startsWith(JNIEnv* envId_, char const * str1_, char const * str2_)
{
  int len = strlen(str2_);
  for (int i = 0; i < len; i++)
  {
    if (str1_[i] != str2_[i])
    {
      return false;
    }
  }

  return true;
}

String Globals::trimLeft(String const & str_)
{
  // TODO unittests
  String::size_type ofs = str_.find_first_not_of(" ");
  if (ofs == String::npos)
  {
    return "";
  }
  return str_.substr(ofs);
}

String Globals::trimRight(String const & str_)
{
  // TODO unittests
  String::size_type ofs = str_.find_last_not_of(" ");
  if (ofs == String::npos)
  {
    return "";
  }
  return str_.substr(0, ofs + 1);
}

void Globals::remove(String& str_, char const ch_)
{
  str_.erase(std::remove(str_.begin(), str_.end(), ch_), str_.end());
}

bool Globals::isTracingEnabled(JNIEnv* envId_)
{
  MonitorGuard tracingEnabledCounterMonitorGuard(envId_, s_tracingEnabledCounterMonitor);
  bool result = (s_tracingEnabledCounter > 0);
  tracingEnabledCounterMonitorGuard.release();
  return result;
}

void Globals::addTracingEnabledCounter(JNIEnv* envId_, int val_)
{
  MonitorGuard tracingEnabledCounterMonitorGuard(envId_, s_tracingEnabledCounterMonitor);
  s_tracingEnabledCounter += val_;
  tracingEnabledCounterMonitorGuard.release();
}
