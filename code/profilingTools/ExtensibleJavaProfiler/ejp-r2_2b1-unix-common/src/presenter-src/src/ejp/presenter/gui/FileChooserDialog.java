/*
 $Id: FileChooserDialog.java,v 1.8 2005/02/23 09:08:24 vauclair Exp $

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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * Custom file chooser dialog.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.8 $<br>$Date: 2005/02/23 09:08:24 $</code>
 */
public class FileChooserDialog extends JDialog implements ActionListener
{
  protected final JFileChooser jfcChooser;

  // ///////////////////////////////////////////////////////////////////////////
  // FIELDS
  // ///////////////////////////////////////////////////////////////////////////

  protected File selectedFile = null;

  // ///////////////////////////////////////////////////////////////////////////
  // CONSTRUCTOR
  // ///////////////////////////////////////////////////////////////////////////

  public FileChooserDialog(Frame aParent, File aCurrDir)
  {
    super(aParent, true /* modal */);

    // FILE CHOOSER

    jfcChooser = new JFileChooser(aCurrDir);
    Utils.setCommonProperties(jfcChooser);
    Utils.recursiveSetFont(jfcChooser.getAccessibleContext());

    jfcChooser.addChoosableFileFilter(new ExtensionFileFilter(new String[]
    { "ejp", "ejp.gz" }, "Tracer output"));
    jfcChooser.addActionListener(this);

    jfcChooser.setPreferredSize(new Dimension(750, 550));

    getContentPane().add(jfcChooser, BorderLayout.CENTER);

    // DIALOG

    setTitle("Choose a trace file to open...");
    setResizable(false);

    addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent aEvent)
      {
        closeDialog();
      }
    });

    pack();

    // set location
    Utils.centerDialog(this, aParent);
  }

  public void setFile(File file)
  {
    jfcChooser.setSelectedFile(file);
  }

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Blocks until a valid file is selected, the Cancel button is clicked or the
   * window is closed.
   * 
   * @return <code>null</code> if no file was selected (Cancel or Close used)
   */
  public File showDialog()
  {
    selectedFile = null;
    setVisible(true);
    return selectedFile;
  }

  protected void closeDialog()
  {
    setVisible(false);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // ActionListener INTERFACE
  // ///////////////////////////////////////////////////////////////////////////

  public void actionPerformed(ActionEvent aEvent)
  {
    String command = aEvent.getActionCommand();
    if (JFileChooser.APPROVE_SELECTION.equals(command))
    {
      selectedFile = jfcChooser.getSelectedFile();
      if (selectedFile.exists() && !selectedFile.isDirectory())
        closeDialog();
    }
    else if (JFileChooser.CANCEL_SELECTION.equals(command))
    {
      selectedFile = null;
      closeDialog();
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
  // FILE FILTER
  // ///////////////////////////////////////////////////////////////////////////

  protected class ExtensionFileFilter extends FileFilter
  {
    /**
     * 
     * Extensions are stored in lowercase, with leading dot.
     */
    protected String[] extensions;

    protected String description;

    /**
     * @param aExtensions
     *          possibly many extensions (without leading dot)
     */
    public ExtensionFileFilter(String[] aExtensions, String aDescription)
        throws IllegalArgumentException
    {
      int nb = aExtensions.length;
      if (nb == 0)
        throw new IllegalArgumentException("empty extensions list");

      extensions = new String[nb];
      description = aDescription + " (";
      for (int i = 0; i < nb; i++)
      {
        extensions[i] = "." + aExtensions[i].toLowerCase();
        if (i > 0)
          description += ", ";
        description += "*" + extensions[i];
      }
      description += ")";
    }

    public ExtensionFileFilter(String aExtension, String aDescription)
    {
      this(new String[]
      { aExtension }, aDescription);
    }

    public boolean accept(File aFile)
    {
      if (aFile.isDirectory())
        return true;

      for (int i = 0; i < extensions.length; i++)
        if (aFile.getPath().toLowerCase().endsWith(extensions[i]))
          return true;

      return false;
    }

    public String getDescription()
    {
      return description;
    }
  }
}