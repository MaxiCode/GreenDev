/*
 $Id: NewViewDialog.java,v 1.9 2005/02/14 12:06:20 vauclair Exp $ 
 
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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import ejp.presenter.api.util.CustomLogger;
import ejp.presenter.xml.Filter;

/**
 * View creation dialog.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.9 $<br>$Date: 2005/02/14 12:06:20 $</code>
 */
public class NewViewDialog extends JDialog implements TreeSelectionListener, ListSelectionListener,
    ActionListener
{
  // ///////////////////////////////////////////////////////////////////////////
  // CONSTANTS
  // ///////////////////////////////////////////////////////////////////////////

  public static final Dimension TREE_SIZE = new Dimension(225, 250);

  public static final boolean ROOT_VISIBLE = true;

  public static final boolean SHOW_ROOT_HANDLES = true;

  public static final String DEFAULT_TEXTFIELD_TEXT = "-";

  public static final Color COLOR_OK = Color.black;

  public static final Color COLOR_OK_DISABLED = Color.lightGray;

  public static final Color COLOR_CONFLICT = Color.red.darker();

  public static final Color COLOR_CONFLICT_DISABLED = Color.pink;

  public static final Color[] COLORS =
  { COLOR_OK_DISABLED, COLOR_OK, COLOR_CONFLICT_DISABLED, COLOR_CONFLICT };

  // ///////////////////////////////////////////////////////////////////////////
  // COMPONENTS
  // ///////////////////////////////////////////////////////////////////////////

  protected final JTree jtAvailable;

  protected final JList jlActive;

  protected final JButton jbMove;

  protected final JButton jbUp;

  protected final JButton jbDown;

  protected final JButton jbCustomize;

  protected final JPanel jpInformations;

  protected final JTextField jtfDescription;

  protected final JTextField jtfRequires;

  protected final JTextField jtfMustPrecede;

  protected final JTextField jtfMustFollow;

  protected final JButton jbOK;

  protected final JButton jbCancel;

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * List of <code>Filter</code> values
   */
  protected final Vector availableFilters;

  protected Vector activeFilterCells = new Vector();

  protected Filter selectedFilter = null;

  protected boolean operationIsAdd = true;

  /**
   * List of <code>Filter</code> values
   */
  protected Vector returnValue = null;

  /**
   * 
   * Avoids reentrancy into <code>valueChanged()</code>
   */
  protected boolean valueChangedIsWorking = false;

  protected boolean readOnly = false;

  // ///////////////////////////////////////////////////////////////////////////
  // CONSTRUCTOR
  // ///////////////////////////////////////////////////////////////////////////

  public NewViewDialog(Frame aParent, Vector aAvailableFilters, ArrayList aDefaultFilters)
  {
    super(aParent, true /* modal */
    );

    availableFilters = aAvailableFilters;

    //

    GridBagConstraints gbcTemp;
    TitledBorder tbTemp;

    // MAIN PANEL /////////////////////////////////////////////////////////////

    JPanel jpMain = new JPanel();
    jpMain.setDoubleBuffered(true);
    jpMain.setLayout(new GridBagLayout());
    getContentPane().add(jpMain, BorderLayout.CENTER);

    // AVAILABLE TREE

    // add all defined filters to tree
    DefaultTreeModel dtmTemp = new DefaultTreeModel(new SortableTreeNode("<Root>"));
    int nb = aAvailableFilters.size();
    for (int i = 0; i < nb; i++)
      ((Filter) aAvailableFilters.get(i)).addToTree(dtmTemp);

    jtAvailable = new JTree(dtmTemp);
    Utils.setCommonProperties(jtAvailable);
    jtAvailable.setRootVisible(ROOT_VISIBLE);
    jtAvailable.setShowsRootHandles(SHOW_ROOT_HANDLES);
    jtAvailable.setExpandsSelectedPaths(true);
    jtAvailable.setScrollsOnExpand(true);
    jtAvailable.addTreeSelectionListener(this);
    jtAvailable.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

    JScrollPane jspAvailable = new JScrollPane();
    jspAvailable.setViewportView(jtAvailable);
    jspAvailable.setPreferredSize(TREE_SIZE);

    JPanel jpAvailable = new JPanel();
    jpAvailable.setDoubleBuffered(true);
    jpAvailable.setLayout(new GridLayout(1, 1));
    jpAvailable.add(jspAvailable);

    tbTemp = new TitledBorder("Available filters");
    tbTemp.setTitleFont(Utils.FONT_11);
    jpAvailable.setBorder(tbTemp);

    gbcTemp = new GridBagConstraints();
    gbcTemp.gridx = 0;
    gbcTemp.gridy = 0;
    gbcTemp.gridheight = 10;
    gbcTemp.insets = Utils.INSETS_5;
    jpMain.add(jpAvailable, gbcTemp);

    // ACTIVE LIST

    jlActive = new JList();
    Utils.setCommonProperties(jlActive);
    jlActive.setCellRenderer(new FilterCellRenderer());
    jlActive.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    jlActive.addListSelectionListener(this);

    JScrollPane jspActive = new JScrollPane(jlActive);
    Utils.setCommonProperties(jspActive);
    jspActive.setPreferredSize(TREE_SIZE);

    JPanel jpActive = new JPanel();
    jpActive.setDoubleBuffered(true);
    jpActive.setLayout(new GridLayout(1, 1));
    jpActive.add(jspActive);

    tbTemp = new TitledBorder("Active filters");
    tbTemp.setTitleFont(Utils.FONT_11);
    jpActive.setBorder(tbTemp);

    gbcTemp = new GridBagConstraints();
    gbcTemp.gridx = 2;
    gbcTemp.gridy = 0;
    gbcTemp.gridheight = 10;
    gbcTemp.insets = Utils.INSETS_5;
    jpMain.add(jpActive, gbcTemp);

    // BUTTONS

    // move button

    jbMove = new JButton("Remove");
    Utils.setCommonProperties(jbMove);
    jbMove.addActionListener(this);
    jbMove.setEnabled(false);

    Dimension size = jbMove.getPreferredSize();
    jbMove.setText("Add");
    jbMove.setMnemonic('A');
    jbMove.setPreferredSize(size);

    gbcTemp = new GridBagConstraints();
    gbcTemp.gridx = 1;
    gbcTemp.gridy = 0;
    gbcTemp.insets = Utils.INSETS_5;
    gbcTemp.anchor = GridBagConstraints.NORTH;
    jpMain.add(jbMove, gbcTemp);

    // up button

    jbUp = new JButton("Up");
    Utils.setCommonProperties(jbUp);
    jbUp.setMnemonic('U');
    jbUp.addActionListener(this);
    jbUp.setEnabled(false);

    gbcTemp = new GridBagConstraints();
    gbcTemp.gridx = 1;
    gbcTemp.gridy = 1;
    gbcTemp.insets = Utils.INSETS_5;
    gbcTemp.anchor = GridBagConstraints.NORTH;
    jpMain.add(jbUp, gbcTemp);

    // down button

    jbDown = new JButton("Down");
    Utils.setCommonProperties(jbDown);
    jbDown.setMnemonic('D');
    jbDown.addActionListener(this);
    jbDown.setEnabled(false);

    gbcTemp = new GridBagConstraints();
    gbcTemp.gridx = 1;
    gbcTemp.gridy = 2;
    gbcTemp.insets = Utils.INSETS_5;
    gbcTemp.anchor = GridBagConstraints.NORTH;
    jpMain.add(jbDown, gbcTemp);

    // customize button

    jbCustomize = new JButton("Customize...");
    Utils.setCommonProperties(jbCustomize);
    jbCustomize.setMnemonic('z');
    jbCustomize.addActionListener(this);
    jbCustomize.setEnabled(false);

    gbcTemp = new GridBagConstraints();
    gbcTemp.gridx = 1;
    gbcTemp.gridy = 3;
    gbcTemp.insets = Utils.INSETS_5;
    gbcTemp.anchor = GridBagConstraints.NORTH;
    jpMain.add(jbCustomize, gbcTemp);

    // set same preferred size for all buttons
    Utils.setSameSize(new JButton[]
    { jbMove, jbUp, jbDown, jbCustomize });

    // INFORMATIONS PANEL

    jpInformations = new JPanel();
    tbTemp = new TitledBorder("Filter information");
    tbTemp.setTitleFont(Utils.FONT_11);
    jpInformations.setBorder(tbTemp);
    jpInformations.setLayout(new GridBagLayout());

    gbcTemp = new GridBagConstraints();
    gbcTemp.gridx = 0;
    gbcTemp.gridy = 10;
    gbcTemp.gridwidth = 3;
    gbcTemp.fill = GridBagConstraints.HORIZONTAL;
    jpMain.add(jpInformations, gbcTemp);

    int y = 0;

    jtfDescription = addTextField("Description", y++, "Textual description of the filter.");
    jtfRequires = addTextField("Requires", y++, "Filters required by this filter.");
    jtfMustPrecede = addTextField("Must precede", y++, "Filters that this filter must precede.");
    jtfMustFollow = addTextField("Must follow", y++, "Filters that this filter must follow.");

    // BUTTONS PANEL //////////////////////////////////////////////////////////

    JPanel jpButtons = new JPanel();
    jpButtons.setDoubleBuffered(true);
    jpButtons.setLayout(new FlowLayout(FlowLayout.RIGHT, 5 /* hgap */
    , 5 /* vgap */
    ));

    // OK BUTTON

    jbOK = new JButton("OK");
    Utils.setCommonProperties(jbOK);
    jbOK.setMnemonic('K');
    jbOK.addActionListener(this);

    jpButtons.add(jbOK);

    // CANCEL BUTTON

    jbCancel = new JButton("Cancel");
    Utils.setCommonProperties(jbCancel);
    jbCancel.setMnemonic('C');
    jbCancel.addActionListener(this);

    jpButtons.add(jbCancel);

    // make both buttons same size
    Utils.setSameSize(new JButton[]
    { jbOK, jbCancel });

    getContentPane().add(jpButtons, BorderLayout.SOUTH);
    getRootPane().setDefaultButton(jbOK);

    // /////////////////////////////////////////////////////////////////////////

    // activate default filters
    for (Iterator it = aDefaultFilters.iterator(); it.hasNext();)
    {
      Filter f = (Filter) it.next();
      activateFilter(f, true);
    }

    // re-order filters
    activeFilterCells = order(activeFilterCells);

    // redraw list
    jlActive.setListData(activeFilterCells);

    // DIALOG /////////////////////////////////////////////////////////////////

    setResizable(false);
    setTitle("Design new view...");
    addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent aEvent)
      {
        closeDialog(true /* cancel */
        );
      }
    });

    Utils.registerEscapeKey(this, jbCancel);

    setDefaultInformations();

    pack();

    Utils.centerDialog(this, aParent);
  }

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Wrapper to <code>Utils.addTextField()</code>. Requires
   * <code>jpInformations</code> to be initialized.
   */
  protected JTextField addTextField(String aDescription, int aGridY, String aToolTip)
  {
    JTextField result = Utils.addTextField(jpInformations, aDescription, 0, true, aGridY, aToolTip);
    result.setBorder(new EmptyBorder(0, 0, 0, 0));
    return result;
  }

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Blocks until one of the OK or Cancel buttons is clicked or the window is
   * closed.
   * 
   * @return the selected filters, or <code>null</code> if the user clicked
   *         the Cancel button or closed the dialog
   */
  public Vector showDialog()
  {
    setVisible(true);
    return returnValue;
  }

  protected void closeDialog(boolean aCancel)
  {
    if (!aCancel)
    {
      // build return value
      returnValue = new Vector();
      int nb = activeFilterCells.size();
      for (int i = 0; i < nb; i++)
        returnValue.add(((FilterCell) activeFilterCells.get(i)).filter);
    }

    setVisible(false);

    // warning: do NOT call dispose(), as we might need the dialog again
  }

  public void setReadOnly()
  {
    setTitle("Active filters (read-only)");
    readOnly = true;
    JButton[] allButtons =
    { jbMove, jbUp, jbDown };
    for (int i = 0; i < allButtons.length; i++)
      allButtons[i].setEnabled(false);

    if (returnValue != null)
    {
      int nb = returnValue.size();
      for (int i = 0; i < nb; i++)
        ((Filter) returnValue.get(i)).getImpl().setReadOnly();
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  protected void setTextFieldText(JTextField aTextField, String aText)
  {
    Utils.setTextFieldText(aTextField, aText, DEFAULT_TEXTFIELD_TEXT);
  }

  protected void setDefaultInformations()
  {
    JTextField[] allTFs =
    { jtfDescription, jtfRequires, jtfMustPrecede, jtfMustFollow };
    for (int i = 0; i < allTFs.length; i++)
      setTextFieldText(allTFs[i], null /* use default text */
      );
  }

  protected void showFilterInformation(Filter aFilter)
  {
    setTextFieldText(jtfDescription, aFilter.description);
    setTextFieldText(jtfRequires, renderList(aFilter.getRequiresFilters()));
    setTextFieldText(jtfMustPrecede, renderList(aFilter.getMustPrecedeFilters()));
    setTextFieldText(jtfMustFollow, renderList(aFilter.getMustFollowFilters()));
  }

  // ///////////////////////////////////////////////////////////////////////////
  // ListSelectionListener INTERFACE
  // ///////////////////////////////////////////////////////////////////////////

  public void valueChanged(ListSelectionEvent aEvent)
  {
    if (!valueChangedIsWorking)
    {
      valueChangedIsWorking = true;

      jtAvailable.clearSelection();
      operationIsAdd = false;
      Utils.setButtonText(jbMove, "Remove");
      jbMove.setMnemonic('R');

      FilterCell cell = (FilterCell) jlActive.getSelectedValue();
      if (cell != null)
      {
        // a cell is selected
        selectedFilter = cell.filter;
        showFilterInformation(selectedFilter);
        jbMove.setEnabled(!readOnly && cell.getManuallyAdded());

        jbUp.setEnabled(!readOnly);
        jbDown.setEnabled(!readOnly);

        // possibly create the instance of Filter.impl
        jbCustomize.setEnabled(selectedFilter.getImpl().isCustomizable());
      }
      else
      {
        // nothing is selected
        selectedFilter = null;
        setDefaultInformations();
        jbMove.setEnabled(false);

        jbUp.setEnabled(false);
        jbDown.setEnabled(false);
        jbCustomize.setEnabled(false);
      }

      valueChangedIsWorking = false;
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
  // TreeSelectionListener INTERFACE
  // ///////////////////////////////////////////////////////////////////////////

  public void valueChanged(TreeSelectionEvent aEvent)
  {
    if (!valueChangedIsWorking)
    {
      valueChangedIsWorking = true;

      jlActive.clearSelection();
      operationIsAdd = true;
      Utils.setButtonText(jbMove, "Add");
      jbMove.setMnemonic('A');

      jbUp.setEnabled(false);
      jbDown.setEnabled(false);
      jbCustomize.setEnabled(false);

      TreePath path = jtAvailable.getSelectionPath();
      if (path != null)
      {
        SortableTreeNode node = (SortableTreeNode) path.getLastPathComponent();
        Object o = node.getUserObject();
        if (o instanceof Filter)
        {
          // a filter is selected
          selectedFilter = (Filter) o;
          showFilterInformation(selectedFilter);
          jbMove.setEnabled(!readOnly);
        }
        else
        {
          // a path node is selected
          selectedFilter = null;
          setDefaultInformations();
          jbMove.setEnabled(!readOnly);
        }
      }
      else
      {
        // nothing is selected
        selectedFilter = null;
        setDefaultInformations();
        jbMove.setEnabled(false);
      }

      valueChangedIsWorking = false;
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  public static String renderList(ArrayList aFilterList)
  {
    StringBuffer result = new StringBuffer();
    Iterator it = aFilterList.iterator();
    boolean first = true;
    while (it.hasNext())
    {
      if (first)
        first = false;
      else
        result.append(", ");

      result.append(((Filter) it.next()).getFullName());
    }
    return result.toString();
  }

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * @return <code>null</code> iff the filter was not found
   */
  protected FilterCell findActiveFilter(Filter aFilter)
  {
    int nb = activeFilterCells.size();
    for (int i = 0; i < nb; i++)
    {
      FilterCell fc = (FilterCell) activeFilterCells.get(i);
      if (fc.filter.equals(aFilter))
        return fc;
    }
    return null;
  }

  /**
   * @return the (possibly created) <code>FilterCell</code>
   */
  protected FilterCell activateFilter(Filter aFilter, boolean aManually)
  {
    // remove filter from available filters tree
    availableFilters.remove(aFilter);
    aFilter.removeFromTree((DefaultTreeModel) jtAvailable.getModel());

    FilterCell result = findActiveFilter(aFilter);
    if (result != null)
    {
      if (!aManually)
      {
        // do not allow a manually=true to override a manually=false value
        // (for default filters, which are activated in the constructor)
        result.setManuallyAdded(false);
      }
    }
    else
    {
      // no matching cell, create a new one
      result = new FilterCell(aFilter, aManually);

      // add links for must-precede
      Iterator mp = aFilter.getMustPrecedeFilters().iterator();
      while (mp.hasNext())
      {
        FilterCell cell2 = findActiveFilter((Filter) mp.next());
        if (cell2 != null)
        {
          result.addMustPrecede(cell2);
          cell2.addMustFollow(result);
        }
      }
      mp = null;

      // add links for must-follow
      Iterator mf = aFilter.getMustFollowFilters().iterator();
      while (mf.hasNext())
      {
        FilterCell cell2 = findActiveFilter((Filter) mf.next());
        if (cell2 != null)
        {
          result.addMustFollow(cell2);
          cell2.addMustPrecede(result);
        }
      }
      mf = null;

      // add new cell to active filters list
      activeFilterCells.add(result);
    }

    // recursively add required filters to active list
    Iterator required = aFilter.getRequiresFilters().iterator();
    while (required.hasNext())
    {
      FilterCell cell2 = activateFilter((Filter) required.next(), false /* manual */
      );
      cell2.addIsRequiredBy(result);
    }

    return result;
  }

  protected SortableTreeNode deactivateFilter(Filter aFilter)
  {
    // remove filter from active list
    FilterCell cell = findActiveFilter(aFilter);
    activeFilterCells.remove(cell);

    // add filter to available tree
    SortableTreeNode result = aFilter.addToTree((DefaultTreeModel) jtAvailable.getModel());
    availableFilters.add(aFilter);

    // recursively remove all no longer required filters
    Iterator it = aFilter.getRequiresFilters().iterator();
    while (it.hasNext())
    {
      Filter f = (Filter) it.next();
      FilterCell cell2 = findActiveFilter(f);
      cell2.removeIsRequiredBy(cell);
      if (cell2.getIsRequiredByCount() == 0)
      {
        if (cell2.getOnceManuallyAdded())
          cell2.setManuallyAdded(true);
        else
          deactivateFilter(f);
      }
    }

    return result;
  }

  /**
   * Recursively activates all filters in given directory node.
   * 
   * @return cell of last activated filter (<code>null</code> iff none
   *         activated)
   */
  protected FilterCell activateDirectory(SortableTreeNode aNode)
  {
    FilterCell result = null;
    while (!aNode.isLeaf()) // children will be progressively removed
    {
      SortableTreeNode child = (SortableTreeNode) aNode.getFirstChild();
      Object obj = child.getUserObject();
      if (obj == null || !(obj instanceof Filter))
        result = activateDirectory(child);
      else
      {
        Filter filter = (Filter) obj;
        result = activateFilter(filter, true /* manual */
        );
      }
    }
    return result;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // ActionListener INTERFACE
  // ///////////////////////////////////////////////////////////////////////////

  public void actionPerformed(ActionEvent aEvent)
  {
    Object src = aEvent.getSource();
    if (src == jbMove)
    {
      // MOVE BUTTON

      boolean savedValueChangedIsWorking = valueChangedIsWorking;
      valueChangedIsWorking = true;

      if (selectedFilter == null)
      {
        // a directory node was selected
        TreePath path = jtAvailable.getSelectionPath();
        if (path == null)
        {
          CustomLogger.INSTANCE.warning("Internal error - "
              + "the Move button should be disabled if no filter " + "or directory is selected");
          valueChangedIsWorking = savedValueChangedIsWorking;
        }
        else
        {
          SortableTreeNode node = (SortableTreeNode) path.getLastPathComponent();
          FilterCell cell = activateDirectory(node);

          // re-order filters
          activeFilterCells = order(activeFilterCells);

          // redraw list
          jlActive.setListData(activeFilterCells);

          valueChangedIsWorking = savedValueChangedIsWorking;

          // ensure last added item is selected and visible
          int index = activeFilterCells.indexOf(cell);
          jlActive.setSelectedIndex(index);
          jlActive.ensureIndexIsVisible(index);
        }

        return;
      }

      if (operationIsAdd)
      {
        // OPERATION IS ADD

        FilterCell cell = activateFilter(selectedFilter, true /* manual */
        );

        // re-order filters
        activeFilterCells = order(activeFilterCells);

        // redraw list
        jlActive.setListData(activeFilterCells);

        valueChangedIsWorking = savedValueChangedIsWorking;

        // ensure new item is selected and visible
        int index = activeFilterCells.indexOf(cell);
        jlActive.setSelectedIndex(index);
        jlActive.ensureIndexIsVisible(index);
      }
      else
      {
        // OPERATION IS REMOVE

        SortableTreeNode newNode = deactivateFilter(selectedFilter);

        // re-order filters
        activeFilterCells = order(activeFilterCells);

        // redraw list
        jlActive.setListData(activeFilterCells);

        valueChangedIsWorking = savedValueChangedIsWorking;

        // ensure new node is selected and visible
        TreePath path = new TreePath(newNode.getPath());
        jtAvailable.setSelectionPath(path);
        jtAvailable.expandPath(path);
        jtAvailable.scrollPathToVisible(path);
      }
    }
    else if (src == jbUp || src == jbDown)
    {
      // UP / DOWN BUTTONS

      boolean savedValueChangedIsWorking = valueChangedIsWorking;
      valueChangedIsWorking = true;

      // if allowed, move selected cell up / down in data vector
      boolean isUp = (src == jbUp);
      FilterCell cell = findActiveFilter(selectedFilter);
      int index = activeFilterCells.indexOf(cell);
      if ((isUp && index > 0) || (!isUp && index < activeFilterCells.size() - 1))
      {
        activeFilterCells.remove(index);
        activeFilterCells.insertElementAt(cell, (isUp ? index - 1 : index + 1));

        // re-order filters
        activeFilterCells = order(activeFilterCells);

        // redraw list
        jlActive.setListData(activeFilterCells);

        valueChangedIsWorking = savedValueChangedIsWorking;

        // ensure new item is selected and visible
        index = activeFilterCells.indexOf(cell);
        jlActive.setSelectedIndex(index);
        jlActive.ensureIndexIsVisible(index);
      }
      else
        valueChangedIsWorking = savedValueChangedIsWorking;
    }
    else if (src == jbOK || src == jbCancel)
    {
      // OK / CANCEL BUTTONS

      closeDialog(src == jbCancel);
    }
    else if (src == jbCustomize)
    {
      // CUSTOMIZE BUTTON

      selectedFilter.getImpl().showParametersDialog(this);
    }
    else
      CustomLogger.INSTANCE.warning("Unable to handle action event from an unknown source");
  }

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  protected static Vector order(Vector aFilterCellVector)
  {
    Vector result = new Vector();

    Vector in = (Vector) aFilterCellVector.clone();

    // start with nothing conflicting
    Iterator it1 = in.iterator();
    while (it1.hasNext())
      ((FilterCell) it1.next()).setConflicting(false);

    while (!in.isEmpty())
    {
      // try to find a cell FC which must-follow is empty (or minimal)
      FilterCell bestCell = null;
      int min = Integer.MAX_VALUE;
      for (int i = 0; min > 0 && i < in.size(); i++)
      {
        FilterCell fc = (FilterCell) in.get(i);
        Set mf = (HashSet) fc.getMustFollow().clone();
        mf.retainAll(in);
        int count = mf.size();

        if (count < min)
        {
          // better count, update best value
          min = count;
          bestCell = fc;
        }
      }

      if (min > 0)
      {
        Iterator it = bestCell.getMustFollow().iterator();
        while (it.hasNext())
          ((FilterCell) it.next()).setConflicting(true);
      }

      // move BESTCELL from IN to RESULT
      in.remove(bestCell);
      result.add(bestCell);
    }

    return result;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // NESTED CLASS - FilterCellRenderer
  // ///////////////////////////////////////////////////////////////////////////

  protected class FilterCellRenderer extends JLabel implements ListCellRenderer
  {
    public FilterCellRenderer()
    {
      Utils.setCommonProperties(this);
      setBorder(new EmptyBorder(0, 2, 0, 2)); // use small insets
      setOpaque(true);
    }

    public Component getListCellRendererComponent(JList aList, Object aValue, int aIndex,
        boolean aIsSelected, boolean aCellHasFocus)
    {
      FilterCell cell = (FilterCell) aValue;

      Filter filter = cell.filter;
      setText(filter.getFullName());

      setBackground(aIsSelected ? aList.getSelectionBackground() : aList.getBackground());

      int id = ((cell.getConflicting() ? 1 : 0) << 1) | (cell.getManuallyAdded() ? 1 : 0);
      setForeground(COLORS[id]);

      return this;
    }
  }
}
