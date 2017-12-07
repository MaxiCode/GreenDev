/*
 $Id: IMethodTimeNode.java,v 1.4 2005/03/23 11:14:08 vauclair Exp $

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

import java.util.List;

import ejp.presenter.api.model.LoadedMethod;

public interface IMethodTimeNode
{
  static final long NO_TIMESTAMP = -1;

  /**
   * Gets the associated method, if any.
   * 
   * @return <code>null</code> iff this node is a root node.
   */
  LoadedMethod getMethod();

  /**
   * Gets current children list as an array.
   * 
   * @return a <code>MethodTimeNode[]</code> value.
   */
  IMethodTimeNode[] getChildren();

  /**
   * Gets total time.
   * 
   * @return a <code>long</code> value.
   */
  public long getTotalTime();

  /**
   * Gets own time.
   * 
   * @return a <code>long</code> value.
   */
  public long getTime();

  void setChildren(List chilren_);

  void setTotalTime(long time_);

  long getEntryTime();

  long getExitTime();
}