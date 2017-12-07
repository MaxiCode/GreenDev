/*
  $Id: ClassloaderOutputFile.cpp,v 1.8 2005/02/14 14:19:46 vauclair Exp $

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
#include "ClassloaderOutputFile.hpp"

String const ClassloaderOutputFile::NAME("Classloader");

ClassloaderOutputFile::ClassloaderOutputFile() : OutputFile(NO_ENV_ID)
{
  m_name = NAME;
}
