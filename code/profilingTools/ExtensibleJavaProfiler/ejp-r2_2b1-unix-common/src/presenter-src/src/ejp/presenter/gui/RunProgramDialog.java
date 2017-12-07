/*
 $Id: RunProgramDialog.java,v 1.8 2005/02/14 12:06:21 vauclair Exp $

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

package ejp.presenter.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import ejp.presenter.api.model.LoadedClass;
import ejp.presenter.api.util.CustomLogger;
import ejp.presenter.xml.ConfigurationManager;

/**
 * <i>Run program </i> dialog.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.8 $<br>$Date: 2005/02/14 12:06:21 $</code>
 */
public class RunProgramDialog extends JDialog implements ActionListener, DocumentListener
{
  // ///////////////////////////////////////////////////////////////////////////
  // DEFAULT VALUES
  // ///////////////////////////////////////////////////////////////////////////

  protected static String defaultSourcePaths = null;

  protected static String defaultCommandTemplate = null;

  // ///////////////////////////////////////////////////////////////////////////
  // CONSTANTS
  // ///////////////////////////////////////////////////////////////////////////

  public static final int TEXT_FIELD_COLUMNS = 50;

  public static final char FORMAT_CLASS_NAME = 'c';

  public static final char FORMAT_SOURCE_FILE = 'f';

  public static final char FORMAT_SOURCE_LINE = 'l';

  public static final char FORMAT_FOUND_PATH = 'p';

  public static final String PATHS_DELIMITER = ";";

  // ///////////////////////////////////////////////////////////////////////////
  // COMPONENTS
  // ///////////////////////////////////////////////////////////////////////////

  protected final JPanel jpMain;

  protected final JTextField jtfClassName;

  protected final JTextField jtfSourceFile;

  protected final JTextField jtfSourceLine;

  protected final JTextField jtfSourcePaths;

  protected final JTextField jtfFoundPath;

  protected final JTextField jtfCommandTemplate;

  protected final JTextField jtfCommandLine;

  protected final JPanel jpButtons;

  protected final JButton jbOK;

  protected final JButton jbDefault;

  protected final JButton jbCancel;

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  protected LoadedClass loadedClass = null;

  protected int sourceLine = -1;

  protected String returnValue = null;

  // ///////////////////////////////////////////////////////////////////////////
  // CONSTRUCTOR
  // ///////////////////////////////////////////////////////////////////////////

  public RunProgramDialog(Frame aParent)
  {
    super(aParent, true /* modal */);

    // MAIN PANEL /////////////////////////////////////////////////////////////

    jpMain = new JPanel();
    jpMain.setDoubleBuffered(true);
    jpMain.setLayout(new GridBagLayout());
    getContentPane().add(jpMain, BorderLayout.CENTER);

    int y = 0;
    jtfClassName = addTextField("Class name (%" + FORMAT_CLASS_NAME + ")", true, y++,
        "Fully qualified class name");

    jtfSourceFile = addTextField("Source file (%" + FORMAT_SOURCE_FILE + ")", true, y++,
        "Source file name, as returned by JVM");

    jtfSourceLine = addTextField("Source line (%" + FORMAT_SOURCE_LINE + ")", true, y++,
        "Line of method in source file, as returned by JVM");

    jtfSourcePaths = addTextField("Source paths", false, y++, "List of paths (separated by '"
        + PATHS_DELIMITER + "') in which the program should search for the source file");

    jtfFoundPath = addTextField("First found path (%" + FORMAT_FOUND_PATH + ")", true, y++,
        "Fully qualified name of path in which the source file was found");

    jtfCommandTemplate = addTextField("Command template", false, y++,
        "All abbreviations defined above are allowed, including \"%%\" which "
            + "will be replaced by a single '%' character");

    jtfCommandLine = addTextField("Resulting command line", true, y++,
        "Computed command line of program to run");

    // BUTTONS PANEL //////////////////////////////////////////////////////////

    jpButtons = new JPanel();
    jpButtons.setDoubleBuffered(true);
    jpButtons.setLayout(new FlowLayout(FlowLayout.RIGHT));
    getContentPane().add(jpButtons, BorderLayout.SOUTH);

    jbOK = addButton("OK");
    jbOK.setMnemonic('K');
    jbDefault = addButton("Set Default");
    jbDefault.setMnemonic('D');
    jbCancel = addButton("Cancel");
    jbCancel.setMnemonic('C');

    // make all buttons same size
    Utils.setSameSize(new JButton[]
    { jbOK, jbDefault, jbCancel });

    getRootPane().setDefaultButton(jbOK);

    // DIALOG /////////////////////////////////////////////////////////////////

    setResizable(false);
    setTitle("Run program...");
    addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent aEvent)
      {
        closeDialog(true /* cancel */);
      }
    });

    Utils.registerEscapeKey(this, jbCancel);

    displayValues();

    pack();

    Utils.centerDialog(this, aParent);
  }

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Wrapper to <code>Utils.addTextField()</code>. Requires
   * <code>jpMain</code> to be initialized.
   */
  protected JTextField addTextField(String aDescription, boolean aReadOnly, int aGridY,
      String aToolTip)
  {
    JTextField result = Utils.addTextField(jpMain, aDescription, TEXT_FIELD_COLUMNS, aReadOnly,
        aGridY, aToolTip + (aReadOnly ? " (read-only)" : ""));
    if (!aReadOnly)
      result.getDocument().addDocumentListener(this);
    return result;
  }

  /**
   * 
   * Requires <code>jpButtons</code> to be initialized.
   */
  protected JButton addButton(String aText)
  {
    JButton result = new JButton(aText);
    Utils.setCommonProperties(result);
    result.addActionListener(this);

    JPanel jpTemp = new JPanel();
    jpTemp.setDoubleBuffered(true);
    jpTemp.add(result);
    jpButtons.add(jpTemp);

    return result;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // DEFAULT VALUES
  // ///////////////////////////////////////////////////////////////////////////

  public static void setDefaultSourcePaths(String aNewValue)
  {
    defaultSourcePaths = aNewValue;
  }

  public static void setDefaultCommandTemplate(String aNewValue)
  {
    defaultCommandTemplate = aNewValue;
  }

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  protected void displayValues()
  {
    Utils.setTextFieldText(jtfClassName, getClassName());
    Utils.setTextFieldText(jtfSourceFile, getSourceFile());
    Utils.setTextFieldText(jtfSourceLine, getSourceLine());

    searchSourceFile();
    computeCommandLine();
  }

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Blocks until one of the OK or Cancel buttons is clicked or the window is
   * closed.
   * 
   * @return <code>null</code> if dialog was discarded (Cancel or Close used)
   */
  public String showDialog(LoadedClass aLoadedClass, int aSourceLine)
  {
    loadedClass = aLoadedClass;
    sourceLine = aSourceLine;

    // default values
    if (defaultSourcePaths != null)
    {
      Utils.setTextFieldText(jtfSourcePaths, defaultSourcePaths);
      defaultSourcePaths = null;
    }
    if (defaultCommandTemplate != null)
    {
      Utils.setTextFieldText(jtfCommandTemplate, defaultCommandTemplate);
      defaultCommandTemplate = null;
    }

    returnValue = null;
    displayValues();
    setVisible(true);

    return returnValue;
  }

  protected void closeDialog(boolean aCancel)
  {
    if (!aCancel)
    {
      // build return value
      returnValue = jtfCommandLine.getText();
    }

    setVisible(false);

    // warning: do NOT call dispose(), as we might need the dialog again
  }

  /**
   * @return <code>""</code> iff loaded class is <code>null</code>
   */
  protected String getClassName()
  {
    return (loadedClass == null ? "" : loadedClass.name);
  }

  /**
   * @return <code>""</code> iff loaded class is <code>null</code>
   */
  protected String getSourceFile()
  {
    return (loadedClass == null ? "" : loadedClass.sourceFile);
  }

  /**
   * @return <code>""</code> iff source line is <code>-1</code>
   */
  protected String getSourceLine()
  {
    return (sourceLine == -1 ? "" : new Integer(sourceLine).toString());
  }

  protected void searchSourceFile()
  {
    if (loadedClass == null)
      return;

    String packPath = loadedClass.getPackage().replace('.', File.separatorChar)
        + File.separatorChar;

    String paths = jtfSourcePaths.getText();
    StringTokenizer st = new StringTokenizer(paths, PATHS_DELIMITER, false /*
                                                                             * return
                                                                             * delims
                                                                             */);

    String found = null;
    while (found == null && st.hasMoreTokens())
    {
      try
      {
        File parent = new File(st.nextToken());
        if (parent.exists() && parent.isDirectory())
        {
          File f = new File(parent /* parent */, packPath + loadedClass.sourceFile /* child */);
          if (f.exists() && f.isFile() && f.canRead())
            found = f.getParentFile().getCanonicalPath() + File.separator;
        }
      }
      catch (Exception e)
      {
        // nop
      }
    }

    Utils.setTextFieldText(jtfFoundPath, found);
  }

  protected void computeCommandLine()
  {
    String template = jtfCommandTemplate.getText();
    int nb = template.length();
    StringBuffer buffer = new StringBuffer(nb);

    boolean escape = false;
    for (int i = 0; i < nb; i++)
    {
      char c = template.charAt(i);
      if (escape)
      {
        // escape is true

        switch (c)
        {
        case FORMAT_CLASS_NAME:
          buffer.append(getClassName());
          break;
        case FORMAT_SOURCE_FILE:
          buffer.append(getSourceFile());
          break;
        case FORMAT_SOURCE_LINE:
          buffer.append(getSourceLine());
          break;
        case FORMAT_FOUND_PATH:
          buffer.append(jtfFoundPath.getText());
          break;
        case '%':
          buffer.append(c);
          break;
        default:
          CustomLogger.INSTANCE.warning("Ignoring invalid format character '" + c + "'");
        }
        escape = false;
      }
      else
      {
        // escape is false

        if (c == '%')
          escape = true;
        else
          buffer.append(c);
      }

      Utils.setTextFieldText(jtfCommandLine, buffer.toString());
    }

  }

  // ///////////////////////////////////////////////////////////////////////////
  // ActionListener INTERFACE
  // ///////////////////////////////////////////////////////////////////////////

  public void actionPerformed(ActionEvent aEvent)
  {
    Object src = aEvent.getSource();
    if (src == jbOK || src == jbCancel)
    {
      // OK / CANCEL BUTTONS

      closeDialog(src == jbCancel);
    }
    else if (src == jbDefault)
    {
      // SET DEFAULT BUTTON

      ConfigurationManager.getInstance().saveRunProgramDialogValues(jtfSourcePaths.getText(),
          jtfCommandTemplate.getText());
    }
    else
      CustomLogger.INSTANCE.warning("Unable to handle event from unknown source " + src);
  }

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  protected void handleUpdate(DocumentEvent aEvent)
  {
    Document doc = aEvent.getDocument();
    if (doc == jtfSourcePaths.getDocument())
    {
      searchSourceFile();
      computeCommandLine();
    }
    else if (doc == jtfCommandTemplate.getDocument())
    {
      computeCommandLine();
    }
    else
      CustomLogger.INSTANCE.warning("Unable to handle update to unknown document " + doc);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // DocumentListener INTERFACE
  // ///////////////////////////////////////////////////////////////////////////

  public void changedUpdate(DocumentEvent aEvent)
  {
    handleUpdate(aEvent);
  }

  public void insertUpdate(DocumentEvent aEvent)
  {
    handleUpdate(aEvent);
  }

  public void removeUpdate(DocumentEvent aEvent)
  {
    handleUpdate(aEvent);
  }
}
