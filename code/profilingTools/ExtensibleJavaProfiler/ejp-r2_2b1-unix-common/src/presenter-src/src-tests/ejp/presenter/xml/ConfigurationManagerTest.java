/*
 $Id: ConfigurationManagerTest.java,v 1.3 2005/02/14 12:12:22 vauclair Exp $

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

package ejp.presenter.xml;

import java.io.File;

import org.xml.sax.SAXParseException;

import ejp.presenter.api.util.CustomLogger;
import ejp.presenter.gui.RunProgramDialog;

/**
 * TODOC
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.3 $<br>$Date: 2005/02/14 12:12:22 $</code>
 */
public class ConfigurationManagerTest
{
  /**
   * Convenience method for testing purpose.
   */
  public static void main(String[] aArgs) throws Exception
  {
    try
    {
      FiltersRepository.createInstance(new File("../../../../etc/filters/default.xml"));
    }
    catch (SAXParseException saxpe)
    {
      System.out.println(saxpe);

      // copied and modified from MainFrame.java
      CustomLogger.INSTANCE.severe("unable to parse XML file - " + saxpe.getMessage() + " @ line "
          + saxpe.getLineNumber() + ", column " + saxpe.getColumnNumber() + ", id = "
          + saxpe.getPublicId() + ";" + saxpe.getSystemId());
    }

    try
    {
      ConfigurationManager.createInstance(new File("../../../../etc/settings/default.xml"));
    }
    catch (Exception e)
    {
      System.out.println("FATAL EXCEPTION - " + e);
      return;
    }

    if (ConfigurationManager.getInstance().exception != null)
      System.out.println("warning: " + ConfigurationManager.getInstance().exception);

    RunProgramDialog rpd = new RunProgramDialog(new javax.swing.JFrame());
    rpd.showDialog(null, -1);

    System.exit(0);
  }
}
