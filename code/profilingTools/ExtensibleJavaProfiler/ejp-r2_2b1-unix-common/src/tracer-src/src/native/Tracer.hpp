/*
  $Id: Tracer.hpp,v 1.3 2005/03/18 18:16:27 vauclair Exp $
  
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

#ifndef __Tracer_h__
#define __Tracer_h__

#include "Globals.hpp"

#include "ThreadEventsOutputFile.hpp"

void handleMethodEvent(ThreadEventsOutputFile* threadContext_, jmethodID methodId_,
  bool isEntry, jlong entryTime_);
  
void handleLibraryShutdown();

#endif // __Tracer_h__
