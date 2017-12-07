/*
  $Id: InputFile.cpp,v 1.8 2005/03/18 16:11:55 vauclair Exp $

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

#include "InputFile.hpp"

#include <cerrno>
#include <iostream>

#include "EjpException.hpp"

InputFile::InputFile(char const * filename_)
{
  // open file
  FILE* file = fopen(filename_, "rb");

  if (file == NULL)
  {
    OStringStream oss;
    oss << "Unable to open file " << filename_ << " for reading (errno="
      << errno << ")";
    throw EjpException(__FILE__, __LINE__, NO_ENV_ID, oss.str());
  }

  m_file = file;
  m_eof = false;
}

InputFile::~InputFile()
{
  int errcode = fclose(m_file);
  if (errcode == EOF)
  {
    LOG_WARNING(NO_ENV_ID, "Unable to close input file (errno="
      << errno << ")");
  }
}

bool InputFile::isEof() const
{
  return m_eof;
}

String InputFile::readLine()
{
  if (m_eof)
  {
    throw EjpException(__FILE__, __LINE__, NO_ENV_ID, "EOF reached");
  }

  OStringStream buffer;

  bool done = false;
  do
  {
    char ch;
    int count = fread(&ch, 1, 1, m_file);
    if (count != 1)
    {
      if (feof(m_file))
      {
        m_eof = true;
        done = true;
      }
      else
      {
        OStringStream oss;
        oss << "Unable to read from file (errno=" << errno << ")";
        throw EjpException(__FILE__, __LINE__, NO_ENV_ID, oss.str());
      }
    }
    else
    {
      if (ch == '\n' || ch == '\r')
      {
        done = true;
      }
      else
      {
        buffer << ch;
      }
    }
  }
  while (!done);

  return buffer.str();
}
