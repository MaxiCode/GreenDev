/*
  $Id: OutputFile.hpp,v 1.3 2005/03/18 16:42:49 vauclair Exp $

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

#ifndef __OutputFile_h__
#define __OutputFile_h__

#include "Globals.hpp"

#ifdef USE_ZLIB
#include <zlib.h>
#endif

class OutputFile
{
public:
  // methods
  // TODO add direct for all
  void printU1(u1 data_, bool direct_ = false);
  void printU2(u2 data_);
  void printU4(u4 data_, bool direct_ = false);
  void printJLong(jlong value_);
  void printEncoded(u4 data_);
  void printStr(char const * str_, bool direct_ = false);

  // getters
  JNIEnv* getEnvId() const;

protected:
  // data
  String m_name;

  /**
   * Current file name
   *
   * Set by open().
   */
  String m_filename;

  JNIEnv* const m_envId;
  
  JVMPI_RawMonitor m_monitor;

  // getters
  bool isOpened() const;

  // methods
  OutputFile(JNIEnv* envId_);
  virtual ~OutputFile();

  inline void open();
  inline void close();

  String computeFileName(bool dirty_) const;
  inline String makeFileName(String const & str_) const;

private:
  // data
  char* m_buffer;
  int m_bufferSize;
  int m_bufferOffset;
  bool m_opened;
  bool m_closed;

#ifdef USE_ZLIB
  gzFile m_file;
#else
  std::FILE* m_file;
#endif

  // methods
  template<class T> inline void printValue(T value_, bool direct_ = false);
  inline void writeData(void* data_, int size_, bool direct_ = false);
  inline void writeDataDirect(void* data_, int size_);
  inline void addData(void* data_, int size_);
  inline void flushBuffer();
  
  String getStdioError() const;
};

#endif // __OutputFile_h__
