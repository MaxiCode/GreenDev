/*
  $Id: OutputFile.cpp,v 1.15 2005/03/18 18:12:55 vauclair Exp $

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
#include "OutputFile.hpp"

#include <cerrno>
#include <cstdio>
#include <iostream>
#include <memory>

#include "EjpException.hpp"

#ifdef MUST_DEFINE_SYS_ERR
// apparently not defined by g++ sparc-sun-solaris2.9/3.3.2
extern int sys_nerr;
extern char *sys_errlist[];
#endif

OutputFile::OutputFile(JNIEnv* envId_) : m_envId(envId_)
{
  TRACE_ENTER(m_envId, "OutputFile::OutputFile");
  
  // create monitor
  OStringStream oss;
  oss << "_ejp.tracer.outputFile" << this;  
  m_monitor = Globals::s_jvmpiInterface->RawMonitorCreate(const_cast<char*>(oss.str().c_str()));

  m_opened = false;
  m_closed = false;
  m_buffer = NULL;
  m_bufferSize = Globals::OUTPUT_FILES_BUFFER_SIZE;
  m_bufferOffset = 0;

  TRACE_EXIT(m_envId, "OutputFile::OutputFile");
}

// warning: invoked virtual methods are not invoked virtually
OutputFile::~OutputFile()
{
  TRACE_ENTER(m_envId, "OutputFile::~OutputFile");

  flushBuffer();

  if (m_opened)
  {
    close();

    // rename file using latest data, without dirty extension
    
    String oldFilename = m_filename;        
    String newFilename = computeFileName(false);
    
    int maxTries = 100;
    bool success = false;
    for (int i = 0; !success && i < maxTries; i++)
    {
      int errcode = std::rename(oldFilename.c_str(), newFilename.c_str());
      if (errcode == 0) 
      {
        success = true;
      }
    }
    
    if (success)
    {
      LOG_DEBUG_FORCED(m_envId, "Successfully renamed file " << oldFilename
        << " to " << newFilename);
    }
    else 
    {
      LOG_WARNING(m_envId, "Unable to rename file " << oldFilename
        << " to " << newFilename << " after " << maxTries << " attempt(s). " 
        << getStdioError());
    }
  }

  if (m_buffer != NULL)
  {
    delete[] m_buffer;
  }
  
  if (Globals::s_jvmpiInterface != NULL)
  {
    Globals::s_jvmpiInterface->RawMonitorDestroy(m_monitor);  
  }

  TRACE_EXIT(m_envId, "OutputFile::~OutputFile");
}

String OutputFile::getStdioError() const
{
  int no = errno;
  OStringStream msg;
  msg << "Stdio error: #" << no;
  if (no >=0 && no < sys_nerr && sys_errlist[no] != NULL)
  {
    msg << " " << sys_errlist[no];
  }
  return msg.str();
}

JNIEnv* OutputFile::getEnvId() const
{
  return m_envId;
}

String OutputFile::computeFileName(bool dirty_) const
{
  OStringStream oss;
  oss << "00000000"; // TODO session
  oss << "_";
  oss << makeFileName(m_name);
  oss << ".ejp";
#ifdef USE_ZLIB
  oss << ".gz";
#endif
  if (dirty_)
  {
    oss << ".!";
  }

  return oss.str();
}

inline String OutputFile::makeFileName(String const & str_) const
{

  int len = str_.size();
  char* out = new char[len + 1];
  if (out == NULL)
  {
    // TODO have an allocator method
    OStringStream oss;
    oss << "Unable to allocate " << (len + 1) << " bytes of memory";
    throw EjpException(__FILE__, __LINE__, m_envId, oss.str());
  }
  for (int i = 0; i < len + 1; i++)
  {
    char c = str_[i];
    if (c == 0 || c == ' ' || c == '-' || c == '_' || (c >= 'a' && c <= 'z') ||  (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9'))
    {
      // nop
    }
    else
    {
      c = '_';
    }
    out[i] = c;
   }
   return String(out);
}

inline void OutputFile::open()
{
  TRACE_ENTER(m_envId, "OutputFile::open");
  
  EJP_ASSERT_IS_TRUE(m_envId, !m_closed);
  
  m_filename = computeFileName(true);
  LOG_DEBUG_FORCED(m_envId, "Creating file " << m_filename);

  // open file
#ifdef USE_ZLIB
  m_file = gzopen(m_filename.c_str(), "wb9");
#else
  m_file = std::fopen(m_filename.c_str(), "wb");
#endif

  if (m_file == NULL)
  {
    OStringStream oss;
    oss << "Unable to open file " << m_filename << " for writing. ";
    oss << getStdioError();
    throw EjpException(__FILE__, __LINE__, m_envId, oss.str());
  }

  LOG_DEBUG_FORCED(m_envId, "m_file=" << m_file);

  m_opened = true;

  // write version number
  printU1(ID_VERSION, true);
  printStr(Globals::EJP_TRACER_VERSION.c_str(), true);
  
  // write endian check
  printU1(ID_ENDIAN, true);
  printU4((u4) 0x12345678L, true);

  TRACE_EXIT(m_envId, "OutputFile::open");
}

inline void OutputFile::close()
{
  TRACE_ENTER(m_envId, "OutputFile::close");
  
  EJP_ASSERT_IS_TRUE(m_envId, m_file != NULL);

  LOG_DEBUG_FORCED(m_envId, "Closing file...");

#ifdef USE_ZLIB
  gzclose(m_file);
#else
  int errcode = std::fclose(m_file);
  if (errcode != 0)
  {
    LOG_WARNING(m_envId, "Unable to close file " << m_filename  << ". "
      << getStdioError());
  }
#endif

  m_file = NULL;
  m_opened = false;
  m_closed = true;

  TRACE_EXIT(m_envId, "OutputFile::close");
}

void OutputFile::printU1(u1 data, bool direct_)
{
  printValue(data, direct_);
}

void OutputFile::printU2(u2 data)
{
  printValue(data);
}

void OutputFile::printU4(u4 data, bool direct_)
{
  printValue(data, direct_);
}

void OutputFile::printJLong(jlong value_)
{
  printValue(value_);
}

void OutputFile::printEncoded(u4 data)
{
  while (true)
  {
    if (data < 0x80)
    {
      printU1(data);
      return;
    }
    else
    {
      printU1((data & 0xFF) | 0x80);
      data = data >> 7;
    }
  }
}

inline void OutputFile::writeDataDirect(void* data_, int size_)
{
  EJP_ASSERT_IS_TRUE(m_envId, m_file != NULL);
 
  // TODO gzip version
  int writtenCount = std::fwrite(data_, size_, 1, m_file);
  if (writtenCount != 1)
  {
    LOG_WARNING(m_envId, "Unable to write " << size_ << " byte(s) to file " 
      << m_filename << ". "
      << getStdioError());
  }
}

void OutputFile::printStr(const char* str, bool direct_)
{
  int len = (str != NULL ? strlen(str) : 0);

  if (len > 0)
  {
    writeData((void*) str, len + 1, direct_); // string chars with trailing '\0'
  }
  else
  {
    // empty string
    printU1(0, direct_);
  }
}

template<class T> inline void OutputFile::printValue(T value_, bool direct_)
{
  writeData(&value_, sizeof(value_), direct_);
}

inline void OutputFile::writeData(void* data_, int size_, bool direct_)
{
  // TODO have a level
  TRACE_ENTER(m_envId, "OutputFile::writeData");

#ifdef DEBUG_WRITTEN_DATA
  LOG_DEBUG(m_envId, "Data: " << data_ << "; size: " << size_
    << "; file: " << m_file);
#endif

#ifndef DEBUG_SKIP_OUTPUT
#ifdef USE_ZLIB
  EJP_ASSERT_IS_TRUE(m_envId, m_file != NULL);
  gzwrite(m_file, data_, size_);
#else
  if (direct_)
  {
    writeDataDirect(data_, size_);
  }
  else
  {
    addData(data_, size_);
  }
#endif
#endif // DEBUG_SKIP_OUTPUT

  // TODO have a level
  TRACE_EXIT(m_envId, "OutputFile::writeData");
}

bool OutputFile::isOpened() const
{
  return m_opened;
}

inline void OutputFile::flushBuffer()
{
  if (m_bufferOffset > 0)
  {
    if (!m_opened)
    {
      open();
    }

    LOG_DEBUG(m_envId, "Flushing " << m_bufferOffset << " bytes");
    writeDataDirect(m_buffer, m_bufferOffset);
    m_bufferOffset = 0;
  }
}

inline void OutputFile::addData(void* data_, int size_)
{
  EJP_ASSERT_IS_TRUE(m_envId, data_ != NULL);
  EJP_ASSERT_IS_TRUE(m_envId, size_ > 0);

  if (m_buffer == NULL)
  {
    // lazy buffer allocation
    // TODO use STL allocator?
    m_buffer = new char[m_bufferSize];
    if (m_buffer == NULL)
    {
      OStringStream oss;
      oss << "Unable to allocate " << m_bufferSize << " bytes for output buffer";
      throw EjpException(__FILE__, __LINE__, m_envId, oss.str());
    }
  }

  int remain = size_;

  do
  {
    int copied = (remain <= (m_bufferSize - m_bufferOffset) ? remain : (m_bufferSize - m_bufferOffset));
    memcpy(m_buffer + m_bufferOffset, static_cast<char*>(data_) + (size_ - remain), copied);

    m_bufferOffset += copied;
    EJP_ASSERT_IS_TRUE(m_envId, m_bufferOffset <= m_bufferSize);

    remain -= copied;
    EJP_ASSERT_IS_TRUE(m_envId, remain >= 0);

    // flush buffer if full
    if (m_bufferOffset == m_bufferSize)
    {
      flushBuffer();
    }
  }
  while (remain > 0);

  // un-buffered version
  // fwrite(data_, size_, 1, m_file);
}
