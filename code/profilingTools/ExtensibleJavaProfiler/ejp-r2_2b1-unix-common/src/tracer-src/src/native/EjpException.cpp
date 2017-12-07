/*
  $Id: EjpException.cpp,v 1.9 2005/02/14 14:19:47 vauclair Exp $

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
#include "EjpException.hpp"

#include <sstream>

EjpException::EjpException(String filename_, int line_, JNIEnv const * envId_,
  String message_)
  : m_envId(envId_)
{
  // pre-compute message to ensure no exception is thrown in what()
  OStringStream oss;
  oss << filename_ << ':' << line_ << ' ' << message_;
  m_what = oss.str();
}

EjpException::~EjpException() throw()
{
  // nop
}

char const * EjpException::what() const throw()
{
  return m_what.c_str();
}

JNIEnv const * EjpException::getEnvId() const
{
  return m_envId;
}
