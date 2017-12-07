/*
 $Id: InternalFrame.java,v 1.11 2005/02/17 12:19:19 vauclair Exp $
 
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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ejp.presenter.api.filters.AbstractFilterImpl;
import ejp.presenter.api.model.LoadedMethod;
import ejp.presenter.api.model.tree.LazyNode;
import ejp.presenter.model.LoadedFile;
import ejp.presenter.parser.IProgressMonitor;
import ejp.presenter.parser.ITask;
import ejp.presenter.xml.DomUtils;
import ejp.presenter.xml.Filter;

/**
 * Internal frame presenting a profile (or <i>view </i>).
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.11 $<br>$Date: 2005/02/17 12:19:19 $</code>
 */
public class InternalFrame extends JInternalFrame
{
  public static final Dimension DEFAULT_SIZE = new Dimension(590, 470);

  protected final String m_shortTitle;

  protected final LoadedFile loadedFile;

  protected final MainFrame mainFrame;

  protected final NewViewDialog filtersDialog;

  protected final JMenuItem viewMenuItem;

  protected final JMenuItem windowMenuItem;

  protected final NodePopupListener popupListener = new NodePopupListener(new NodePopupMenu());

  protected final JTree tree;

  // ///////////////////////////////////////////////////////////////////////////
  // CONSTRUCTOR
  // ///////////////////////////////////////////////////////////////////////////

  public InternalFrame(int index_, LoadedFile aLoadedFile, MainFrame aMainFrame, Vector aFilters,
      NewViewDialog aFiltersDialog)
  {
    m_shortTitle = "View #" + index_;

    loadedFile = aLoadedFile;

    mainFrame = aMainFrame;
    filtersDialog = aFiltersDialog;

    JMenuItem[] temp = aMainFrame.registerView(m_shortTitle, this);
    viewMenuItem = temp[0];
    windowMenuItem = temp[1];

    // create mnemonic + accel for view/window menu item
    if (index_ >= 0 && index_ <= 9)
    {
      char chr = (char) ('0' + index_);

      viewMenuItem.setMnemonic(chr);
      windowMenuItem.setMnemonic(chr);

      KeyStroke accel = KeyStroke.getKeyStroke("control " + chr);
      viewMenuItem.setAccelerator(accel);
      windowMenuItem.setAccelerator(accel);
    }

    // initialize frame
    Utils.setCommonProperties(this);
    setMaximizable(true);
    setTitle(m_shortTitle + " [" + aLoadedFile.file.getName() + "] " + "<" + renderList(aFilters)
        + ">");
    setIconifiable(true);
    setResizable(true);
    setClosable(true);
    setSize(DEFAULT_SIZE);

    // build array of filter implementations
    int nb = aFilters.size();
    AbstractFilterImpl[] filterImpls = new AbstractFilterImpl[nb];
    for (int i = 0; i < nb; i++)
      filterImpls[i] = ((Filter) aFilters.get(i)).getImpl();

    // build lazy root node
    LazyNode lazyRootNode = new LazyNode(aLoadedFile.rootNode, filterImpls);

    // build tree component
    tree = new JTree(lazyRootNode);
    Utils.setCommonProperties(tree);
    tree.setShowsRootHandles(true);
    tree.setRootVisible(true);
    tree.setCellRenderer(new CustomTreeCellRenderer());
    tree.addMouseListener(popupListener);
    tree.addMouseListener(new MouseAdapter()
    {
      public void mouseClicked(MouseEvent aEvent)
      {
        if (aEvent.getClickCount() == 2)
        {
          Component c = aEvent.getComponent();
          JTree jt = (JTree) c;
          int x = aEvent.getX();
          int y = aEvent.getY();
          TreePath path = jt.getPathForLocation(x, y);
          if (path != null)
          {
            LazyNode node = (LazyNode) path.getLastPathComponent();
            LoadedMethod method = node.getMethod();
            if (method != null)
              mainFrame.showRunProgramDialog(method);
          }
        }
      }
    });
    tree.addKeyListener(new KeyAdapter()
    {
      public void keyTyped(KeyEvent e_)
      {
        TreePath selectedPath = tree.getSelectionPath();
        char k = e_.getKeyChar();
        switch (k)
        {
        case '+':
          expandNodesWithDialog(selectedPath, 1);
          break;

        case '-':
          tree.collapsePath(selectedPath);
          break;

        case '*':
          expandNodesWithDialog(selectedPath, -1);
          break;

        default:
        // nop
        }
      }
    });

    // allow tool tips for tree
    ToolTipManager.sharedInstance().registerComponent(tree);

    // build scroll pane
    JScrollPane jspNew = new JScrollPane();
    Utils.setCommonProperties(jspNew);
    jspNew.setViewportView(tree);
    jspNew.setWheelScrollingEnabled(true);

    getContentPane().add(jspNew, BorderLayout.CENTER);
  }

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  private class ExpandNodesTask implements ITask
  {
    private final TreePath m_path;

    private final int m_levels;

    private boolean m_stopRequested = false;

    public ExpandNodesTask(TreePath path, final int levels)
    {
      m_path = path;
      m_levels = levels;
    }

    public String getTitle()
    {
      return "Working...";
    }

    public String getLabel()
    {
      return "Expanding nodes";
    }

    public boolean isStoppable()
    {
      return true;
    }

    public boolean isMonitorable()
    {
      // TODO implement monitoring
      return false;
    }

    public void setProgressMonitor(IProgressMonitor aProgressMonitor_)
    {
      // TODO implement monitoring
    }

    public boolean start() throws Exception
    {
      return expandNode(m_path, m_levels);
    }

    public synchronized void stop()
    {
      m_stopRequested = true;
    }

    /**
     * @param aLevels
     *          might be set to <code>-1</code> to request complete expansion
     */
    private boolean expandNode(TreePath aPath, int aLevels)
    {
      boolean result;
      if (aLevels == 0)
      {
        result = true;
      }
      else
      {
        TreeNode node = (TreeNode) aPath.getLastPathComponent();
        if (node.isLeaf())
        {
          result = true;
        }
        else
        {

          // due to an error in Swing, path should be expanded before
          // recursion; this might also allow better optimization since call to
          // expandNode() is last statement
          tree.expandPath(aPath);

          result = true;
          Enumeration enumeration = node.children();
          while (enumeration.hasMoreElements())
          {
            boolean subResult = expandNode(aPath.pathByAddingChild(enumeration.nextElement()),
                aLevels - 1);
            if (!subResult || isStopRequested())
            {
              result = false;
              break;
            }
          }
        }
      }
      return result;
    }

    private synchronized boolean isStopRequested()
    {
      return m_stopRequested;
    }
  }

  private boolean expandNodesWithDialog(final TreePath path, final int levels)
  {
    return doPotentiallyLongOperation(new ExpandNodesTask(path, levels));
  }

  private boolean doPotentiallyLongOperation(ITask task)
  {
    final ProgressDialog progressDialog = new ProgressDialog(mainFrame, task);
    progressDialog.showDialog();
    // TODO check getException()
    return progressDialog.isSuccess();
  }

  //

  void exportToFlatXml(Writer out) throws TransformerException
  {

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder;
    try
    {
      builder = factory.newDocumentBuilder();
    }
    catch (ParserConfigurationException e)
    {
      throw new RuntimeException("DOM configuration error", e);
    }
    Document doc = builder.newDocument();

    Element root = doc.createElement("ejp-export");
    doc.appendChild(root);

    int count = tree.getRowCount();
    for (int i = 0; i < count; i++)
    {
      TreePath path = tree.getPathForRow(i);
      LazyNode node = (LazyNode) path.getLastPathComponent();

      Element child = doc.createElement("node");

      LoadedMethod meth = node.getMethod();
      String label = meth != null ? meth.toNodeLabel() : "<root>";
      child.setAttribute("name", label);

      double timeSecs = ((double) node.getTotalTime()) / 1000000000;
      child.setAttribute("time" + (path.getPathCount() - 1), Double.toString(timeSecs));

      root.appendChild(child);
    }

    DomUtils.exportDocument(doc, null, null, out);
  }

  void exportToCsv(Writer out) throws IOException
  {
    StringBuffer sb = new StringBuffer();

    int count = tree.getRowCount();

    // find max path length
    int maxLen = 0;
    for (int i = 0; i < count; i++)
    {
      TreePath path = tree.getPathForRow(i);
      int len = path.getPathCount() - 1;
      if (len > maxLen)
      {
        maxLen = len;
      }
    }

    for (int i = 0; i < count; i++)
    {
      TreePath path = tree.getPathForRow(i);
      LazyNode node = (LazyNode) path.getLastPathComponent();

      int len = path.getPathCount() - 1;

      LoadedMethod meth = node.getMethod();
      String label = meth != null ? meth.toNodeLabel() : "<root>";
      sb.append(label);

      double timeSecs = ((double) node.getOwnTime()) / 1000000000;

      sb.append(';');
      sb.append(timeSecs * 1000D);

      sb.append(';');
      sb.append(path.getPathCount());

      for (int j = 0; j <= maxLen; j++)
      {
        sb.append(';');
        if (len == j)
        {
          sb.append(Double.toString(timeSecs));
        }
      }

      sb.append(System.getProperty("line.separator"));
    }

    out.write(sb.toString());
  }

  void exportToHtml(Writer out) throws IOException
  {
    StringBuffer sb = new StringBuffer();

    sb.append("<html><body>").append(System.getProperty("line.separator"));
    // sb.append("<table>").append(System.getProperty("line.separator"));

    int count = tree.getRowCount();
    for (int i = 0; i < count; i++)
    {
      TreePath path = tree.getPathForRow(i);
      LazyNode node = (LazyNode) path.getLastPathComponent();

      int len = path.getPathCount() - 1;
      // for (int j = 0; j < len; j++) {
      // sb.append('x');
      // }
      // sb.append(' ');
      // sb.append("<tr>");
      // sb.append("<td width=\"").append(len * 10).append("\"/>");
      // sb.append("<td>");
      sb.append("<table><tr>");
      sb.append("<td width=\"").append(len * 10).append("\"><hr/></td>");
      sb.append("<td>");
      sb.append("<font color=\"#");
      sb.append(Integer.toHexString(node.getColor().getRGB()).substring(2));
      sb.append("\">");
      sb.append(node.toString().replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
      sb.append("</font>");
      sb.append("</td>");
      sb.append("</tr></table>");
      // sb.append("</td>");
      // sb.append("</tr>");
      sb.append(System.getProperty("line.separator"));
    }

    // sb.append("</table>").append(System.getProperty("line.separator"));
    sb.append("</body></html>");

    out.write(sb.toString());
  }

  String getShortTitle()
  {
    return m_shortTitle;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // NESTED CLASS - CustomTreeCellRenderer
  // ///////////////////////////////////////////////////////////////////////////

  protected class CustomTreeCellRenderer extends DefaultTreeCellRenderer
  {
    public Component getTreeCellRendererComponent(JTree aTree, Object aValue, boolean aSel,
        boolean aExpanded, boolean aLeaf, int aRow, boolean aHasFocus)
    {
      super.getTreeCellRendererComponent(aTree, aValue, aSel, aExpanded, aLeaf, aRow, aHasFocus);

      // build tool tip text
      if (aValue instanceof LazyNode)
      {
        LazyNode ln = (LazyNode) aValue;
        setForeground(ln.getColor());
        setToolTipText("<html><small>" + ln.getToolTipText() + "</small></html>");
      }

      return this;
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
  // NESTED CLASS - NodePopupMenu
  // ///////////////////////////////////////////////////////////////////////////

  protected class NodePopupMenu extends JPopupMenu
  {
    protected TreePath selectedPath;

    protected LoadedMethod method;

    protected final JMenuItem jmiRunProgram;

    protected final ArrayList needNode = new ArrayList();

    protected final ArrayList needMethod = new ArrayList();

    public NodePopupMenu()
    {
      Utils.setCommonProperties(this);

      jmiRunProgram = add("Run program...");
      Utils.setCommonProperties(jmiRunProgram);
      jmiRunProgram.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent aEvent)
        {
          mainFrame.showRunProgramDialog(method);
        }
      });
      needMethod.add(jmiRunProgram);

      addSeparator();

      addExpandItem("Expand 2 levels", 2);
      addExpandItem("Expand 5 levels", 5);
      addExpandItem("Expand 10 levels", 10);
      addExpandItem("Expand 50 levels", 50);
      addExpandItem("Expand 100 levels", 100);
      addExpandItem("Expand 1000 levels", 1000);
      addExpandItem("Expand all the way", -1);
      addSeparator();

      JMenuItem jmiTemp = add("View active filters");
      Utils.setCommonProperties(jmiTemp);
      jmiTemp.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent aEvent)
        {
          filtersDialog.setReadOnly();
          filtersDialog.setVisible(true);
        }
      });

      addSeparator();

      jmiTemp = new JMenuItem("Cancel");
      Utils.setCommonProperties(jmiTemp);
      add(jmiTemp);

      pack(); // useless since popup is null (Swing error?)
    }

    protected JMenuItem addExpandItem(String aLabel, final int aLevels)
    {
      JMenuItem result = add(aLabel);
      Utils.setCommonProperties(result);
      result.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent aEvent)
        {
          boolean done = expandNodesWithDialog(selectedPath, aLevels);
          if (done)
          {
            mainFrame.setStatus("Done automatically expanding node");
          }
          else
          {
            mainFrame.setStatus("Cancelled automatically expanding node");
          }
        }
      });
      needNode.add(result);
      return result;
    }

    public void setSource(TreePath aSelectedPath)
    {
      if (aSelectedPath == null)
      {
        selectedPath = null;
        method = null;
      }
      else
      {
        selectedPath = aSelectedPath;
        method = ((LazyNode) selectedPath.getLastPathComponent()).getMethod();
      }

      setEnabled(needMethod, method != null);
      setEnabled(needNode, selectedPath != null);
    }

    protected void setEnabled(ArrayList aJMIList, boolean aEnabled)
    {
      int nb = aJMIList.size();
      for (int i = 0; i < nb; i++)
        ((JMenuItem) aJMIList.get(i)).setEnabled(aEnabled);
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
  // NESTED CLASS - NodePopupListener
  // ///////////////////////////////////////////////////////////////////////////

  protected class NodePopupListener extends MouseAdapter
  {
    protected final NodePopupMenu popup;

    public NodePopupListener(NodePopupMenu aPopup)
    {
      popup = aPopup;
    }

    public void mousePressed(MouseEvent aEvent)
    {
      // for compatibility reasons, popup must be detected in press and release
      maybeShowPopup(aEvent);
    }

    public void mouseReleased(MouseEvent aEvent)
    {
      // for compatibility reasons, popup must be detected in press and release
      maybeShowPopup(aEvent);
    }

    protected void maybeShowPopup(MouseEvent aEvent)
    {
      if (aEvent.isPopupTrigger())
      {
        int x = aEvent.getX();
        int y = aEvent.getY();
        popup.setSource(tree.getPathForLocation(x, y));

        // get reference to parent JInternalFrame and location relative to it
        int parent_x = 0;
        int parent_y = 0;
        Container cont = (Container) aEvent.getComponent();
        for (int i = 0; i < 7; i++)
        {
          Point p = cont.getLocation();
          parent_x += (int) p.getX();
          parent_y += (int) p.getY();
          cont = cont.getParent();
        }

        // due to an error in Swing, popup.getWidth() and popup.getHeight()
        // return 0 before show() is called; note: calling pack() does
        // not help
        int pw = 0;
        for (int i = 0; pw == 0 && i < 2; i++)
        {
          pw = popup.getWidth();
          popup.show(mainFrame.jdpDesktop, Math.min(mainFrame.jdpDesktop.getWidth() - pw, parent_x
              + x), Math.min(mainFrame.jdpDesktop.getHeight() - popup.getHeight(), parent_y + y));
        }
      }
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  public void dispose()
  {
    super.dispose();
    loadedFile.removeView(this);
    mainFrame.unregisterView(viewMenuItem, windowMenuItem);
  }

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  public static String renderList(Vector aFiltersList)
  {
    StringBuffer result = new StringBuffer();
    Iterator it = aFiltersList.iterator();
    boolean first = true;
    while (it.hasNext())
    {
      if (first)
        first = false;
      else
        result.append(", ");

      result.append(((Filter) it.next()).getAbbrevName());
    }
    return result.toString();
  }
}