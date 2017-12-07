/*
 $Id: StringListParameter.java,v 1.7 2005/02/14 12:06:19 vauclair Exp $
 
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

package ejp.presenter.api.filters.parameters;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ScrollPaneConstants;

import ejp.presenter.gui.Utils;

/**
 * A String list parameter, graphically represented as an editable list.
 * 
 * <p>
 * TODOC (later)
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.7 $<br>$Date: 2005/02/14 12:06:19 $</code>
 */
public class StringListParameter extends AbstractParameter
{
  /**
   * Separator character between strings in text descriptions.
   */
  public static final String STRING_SEPARATOR = ";";

  protected final DefaultListModel m_listModel = new DefaultListModel();

  public static final int PREFERRED_LIST_WIDTH = 250;

  protected final JList jlValue;

  protected final JPopupMenu m_popupMenu;

  protected final JMenuItem m_jmiRemove;

  protected final JMenuItem m_jmiEdit;

  public StringListParameter(String aName, String aTitle, String aToolTipText)
  {
    super(aName, aTitle, aToolTipText);

    jlValue = new JList(m_listModel);
    Utils.setCommonProperties(jlValue);
    jlValue
        .setPreferredSize(new Dimension(PREFERRED_LIST_WIDTH, jlValue.getPreferredSize().height));

    jlValue.addMouseListener(new MouseAdapter()
    {
      public void mouseClicked(MouseEvent e_)
      {
        if (e_.getClickCount() == 2)
        {
          int idx = jlValue.locationToIndex(e_.getPoint());
          if (idx == -1)
          {
            // nothing to edit
            return;
          }

          doEditItem(idx);
        }
      }
    });

    m_popupMenu = new JPopupMenu();
    Utils.setCommonProperties(m_popupMenu);

    JMenuItem tmpjmi = null;

    tmpjmi = new JMenuItem(new AbstractAction("Add...")
    {
      public void actionPerformed(ActionEvent e_)
      {
        PackageNameDialog pnd = new PackageNameDialog("");
        pnd.setVisible(true);
        String newText = pnd.getText();
        if (newText == null)
        {
          // cancelled
          return;
        }

        m_listModel.addElement(newText);

        organizeItems();
      }
    });
    Utils.setCommonProperties(tmpjmi);
    m_popupMenu.add(tmpjmi);

    tmpjmi = new JMenuItem(new AbstractAction("Edit")
    {
      public void actionPerformed(ActionEvent e_)
      {
        int idx = jlValue.getSelectedIndex();
        if (idx == -1)
        {
          // nothing to edit
          return;
        }

        doEditItem(idx);
      }
    });
    Utils.setCommonProperties(tmpjmi);
    m_popupMenu.add(tmpjmi);
    m_jmiEdit = tmpjmi;

    tmpjmi = new JMenuItem(new AbstractAction("Remove")
    {
      public void actionPerformed(ActionEvent e_)
      {
        int idx = jlValue.getSelectedIndex();
        if (idx == -1)
        {
          // nothing to remove
          return;
        }
        m_listModel.remove(idx);
      }
    });
    Utils.setCommonProperties(tmpjmi);
    m_popupMenu.add(tmpjmi);
    m_jmiRemove = tmpjmi;

    jlValue.addMouseListener(new PopupListener());

    JScrollPane jsp = new JScrollPane(jlValue);
    jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    addLine("Packages to hide", "TODO (comment).", jsp, null);
  }

  /**
   * Locks the list's state.
   */
  public void setReadOnly()
  {
    jlValue.setEnabled(false);
  }

  public Object getValue()
  {
    ListModel model = jlValue.getModel();
    int size = model.getSize();
    Vector result = new Vector(size);
    for (int i = 0; i < size; i++)
    {
      result.add(model.getElementAt(i));
    }
    return result;
  }

  public void setValue(Object aObject) throws ClassCastException
  {
    m_listModel.clear();
    List l = (List) aObject;
    for (Iterator it = l.iterator(); it.hasNext();)
    {
      m_listModel.addElement(it.next());
    }
  }

  /**
   * Gets value as text.
   * 
   * <p>
   * Strings are separated by {@link #STRING_SEPARATOR}characters.
   */
  public String getValueAsText()
  {
    ListModel model = jlValue.getModel();
    int size = model.getSize();

    StringBuffer buffer = new StringBuffer();

    for (int i = 0; i < size; i++)
    {
      if (i > 0)
        buffer.append(STRING_SEPARATOR);
      buffer.append(model.getElementAt(i));
    }

    return buffer.toString();
  }

  /**
   * Sets value from text.
   * 
   * <p>
   * Input text is tokenized using {@link #STRING_SEPARATOR}.
   * 
   * @exception IllegalStateException
   *              if an error occurs in the tokenizer.
   */
  public void setValueAsText(String aTextValue) throws IllegalStateException
  {
    StringTokenizer st = new StringTokenizer(aTextValue, STRING_SEPARATOR, false);
    int count = st.countTokens();

    ArrayList items = new ArrayList(count);
    while (st.hasMoreTokens())
      items.add(st.nextToken());

    if (items.size() != count)
      throw new IllegalStateException("Actual count of strings (" + items.size()
          + ") is not as expected (" + count + ")");

    setValue(new ArrayList(items));

    organizeItems();
  }

  public boolean matches(String name_)
  {
    ListModel model = jlValue.getModel();
    int size = model.getSize();

    for (int i = 0; i < size; i++)
    {
      if (name_.startsWith((String) model.getElementAt(i)))
        return true;
    }
    return false;
  }

  protected void updatePopupMenu()
  {
    boolean enabled = jlValue.getSelectedIndex() != -1;
    m_jmiRemove.setEnabled(enabled);
    m_jmiEdit.setEnabled(enabled);
  }

  protected void doEditItem(int index_)
  {
    PackageNameDialog pnd = new PackageNameDialog((String) m_listModel.getElementAt(index_));
    pnd.setVisible(true);
    String newText = pnd.getText();
    if (newText == null)
    {
      // cancelled
      return;
    }

    m_listModel.setElementAt(newText, index_);

    organizeItems();
  }

  protected void organizeItems()
  {
    List items = (List) getValue();
    Collections.sort(items);
    setValue(items);
  }

  protected class PopupListener extends MouseAdapter
  {
    public void mousePressed(MouseEvent e_)
    {
      maybeShowPopup(e_);
    }

    public void mouseReleased(MouseEvent e_)
    {
      maybeShowPopup(e_);
    }

    private void maybeShowPopup(MouseEvent e_)
    {
      if (e_.isPopupTrigger() && e_.getComponent().isEnabled())
      {
        updatePopupMenu();
        m_popupMenu.show(e_.getComponent(), e_.getX(), e_.getY());
      }
    }
  }

  protected class PackageNameDialog extends JDialog
  {
    protected String m_text = null;

    public static final int PREFERRED_WIDTH = 250;

    protected final JTextField m_textField;

    public PackageNameDialog(String initial_)
    {
      super(dialog, "Package name", true);

      m_textField = new JTextField(initial_);
      Utils.setCommonProperties(m_textField);
      m_textField.setPreferredSize(new Dimension(PREFERRED_WIDTH,
          m_textField.getPreferredSize().height));
      getContentPane().add(m_textField);

      m_textField.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e_)
        {
          m_text = m_textField.getText();
          dispose();
        }
      });

      pack();
      Utils.centerDialog(this, dialog);
      setResizable(false);

      addWindowListener(new WindowAdapter()
      {
        public void windowClosing(WindowEvent e_)
        {
          handleWindowClosing();
        }
      });

    }

    protected String getText()
    {
      return m_text;
    }

    protected void handleWindowClosing()
    {
      m_text = null;
      dispose();
    }
  }
}
