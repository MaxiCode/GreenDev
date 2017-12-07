/*
 $Id: LoadedFile.java,v 1.6 2005/02/14 12:06:23 vauclair Exp $

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

package ejp.presenter.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JMenu;

import ejp.presenter.gui.InternalFrame;
import ejp.presenter.parser.IMethodTimeNode;

/**
 * Loaded profile, which is associated to a file, a root node and a menu item.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.6 $<br>$Date: 2005/02/14 12:06:23 $</code>
 */
public class LoadedFile
{
  public final File file;

  public final IMethodTimeNode rootNode;

  public final JMenu profileMenu;

  protected final ArrayList internalFrames = new ArrayList();

  public LoadedFile(File aFile, IMethodTimeNode aRootNode, JMenu aProfileMenu)
  {
    file = aFile;
    rootNode = aRootNode;
    profileMenu = aProfileMenu;
  }

  public static String getFileName(File aFile)
  {
    String result;
    try
    {
      result = aFile.getCanonicalPath();
    }
    catch (IOException ioe)
    {
      result = aFile.getName();
    }
    return result;
  }

  public String getFileName()
  {
    return getFileName(file);
  }

  public boolean addView(InternalFrame aInternalFrame)
  {
    return internalFrames.add(aInternalFrame);
  }

  public boolean removeView(InternalFrame aInternalFrame)
  {
    return internalFrames.remove(aInternalFrame);
  }

  public InternalFrame[] getViews()
  {
    return (InternalFrame[]) internalFrames.toArray(new InternalFrame[0]);
  }

  public String toString()
  {
    return getFileName();
  }
}
