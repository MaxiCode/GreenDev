/*
  $Id: EjpException.hpp,v 1.2 2005/03/17 10:36:57 vauclair Exp $

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

#ifndef __EjpException_h__
#define __EjpException_h__

#include "Globals.hpp"

#include <exception>

// do NOT attempt to remove throw() declarations for dtor and what() - those
// are required by std::exception

class EjpException : public std::exception
{
public:
  EjpException(String filename_, int line_, JNIEnv const * envId_,
    String message_);
  virtual ~EjpException() throw();
  virtual char const * what() const throw();
  JNIEnv const * getEnvId() const;

protected:
  JNIEnv const * const m_envId;
  String m_what;
};

#endif // __EjpException_h__
