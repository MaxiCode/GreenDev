/*
  $Id: ClassloaderOutputFile.hpp,v 1.1 2005/02/14 14:19:46 vauclair Exp $

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

#ifndef __ClassloaderOutputFile_h__
#define __ClassloaderOutputFile_h__

#include "Globals.hpp"
#include "OutputFile.hpp"

class ClassloaderOutputFile : public OutputFile
{
public:
  ClassloaderOutputFile();

private:
  static String const NAME;
};

#endif // __ClassloaderOutputFile_h__
