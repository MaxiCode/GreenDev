/*
  $Id: MonitorGuard.cpp,v 1.4 2005/02/14 14:19:46 vauclair Exp $

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

#include "MonitorGuard.hpp"

#include <iostream>

#include "EjpException.hpp"

MonitorGuard::MonitorGuard(JNIEnv* envId_, JVMPI_RawMonitor& monitor_)
  : m_envId(envId_), m_monitor(monitor_), m_released(false)
{
  RAW_MONITOR_ENTER(m_envId, m_monitor);
}

MonitorGuard::~MonitorGuard()
{
  if (!m_released)
  {
    release();
  }
}

void MonitorGuard::release()
{
  if (m_released)
  {
    OStringStream oss;
    oss << "Attempting to release an already released monitor (" << &m_monitor
      << ")";
    throw EjpException(__FILE__, __LINE__, m_envId, oss.str());   
  }
  else
  {
    RAW_MONITOR_EXIT(m_envId, m_monitor); 
    m_released = true;
  }
}
