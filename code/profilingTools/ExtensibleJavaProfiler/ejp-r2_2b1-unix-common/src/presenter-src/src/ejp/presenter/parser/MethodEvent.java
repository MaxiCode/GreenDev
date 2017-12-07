/*
 $Id: MethodEvent.java,v 1.5 2005/02/17 12:51:10 vauclair Exp $
 
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

package ejp.presenter.parser;

public class MethodEvent
{
  protected boolean m_entry;

  protected int m_methodId;

  protected long m_timestamp;

  protected long m_prevEventIndex;

  protected MethodEvent(boolean entry_, int methodId_, long timestamp_, long prevEventIndex_)
  {
    m_entry = entry_;
    m_methodId = methodId_;
    m_timestamp = timestamp_;
    m_prevEventIndex = prevEventIndex_;
  }

  public boolean isEntry()
  {
    return m_entry;
  }

  public int getMethodId()
  {
    return m_methodId;
  }

  public long getTimestamp()
  {
    return m_timestamp;
  }

  public long getPrevEventIndex()
  {
    return m_prevEventIndex;
  }

  public String toString()
  {
    return "(" + (m_entry ? "E" : "X") + " " + m_methodId + " " + m_prevEventIndex + " "
        + m_timestamp + ")";
  }
}