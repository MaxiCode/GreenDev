/*
 $Id: MainFrame.java,v 1.26 2005/03/23 11:17:42 vauclair Exp $

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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXParseException;

import ejp.presenter.api.model.LoadedMethod;
import ejp.presenter.api.util.CustomLogger;
import ejp.presenter.model.LoadedFile;
import ejp.presenter.parser.ClassloaderParser;
import ejp.presenter.parser.EjpFile;
import ejp.presenter.parser.IMethodTimeNode;
import ejp.presenter.parser.IParser;
import ejp.presenter.parser.LazyMethodTimeNode2;
import ejp.presenter.parser.ThreadOutputParser;
import ejp.presenter.xml.ConfigurationManager;
import ejp.presenter.xml.FiltersRepository;

/**
 * Main frame of the application.
 * 
 * Also, main entry of EJP Presenter.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.26 $<br>$Date: 2005/03/23 11:17:42 $</code>
 */
public class MainFrame extends JFrame
{
  // ///////////////////////////////////////////////////////////////////////////
  // CONSTANTS
  // ///////////////////////////////////////////////////////////////////////////

  public static final boolean USE_LAZY_PARSER = true;

  public static final String TITLE = "EJP Presenter [r2_2b1]";

  public static final int FRAME_WIDTH = 800;

  public static final int FRAME_HEIGHT = 600;

  public static final int NEW_VIEW_GAP_X = 15;

  public static final int NEW_VIEW_GAP_Y = 15;

  public static final String SCREENSHOTS_FORMAT = "png"; // "jpeg" works too

  private FileChooserDialog m_fileChooserDialog = null;

  // ///////////////////////////////////////////////////////////////////////////
  // COMPONENTS
  // ///////////////////////////////////////////////////////////////////////////

  // protected final JPopupMenu jpmNode;

  protected final JMenu jmProfiles;

  protected final JMenu jmWindows;

  protected final JDesktopPane jdpDesktop;

  protected final JTextField jtfStatus;

  protected final LogFrame logFrame;

  // ///////////////////////////////////////////////////////////////////////////
  // PROTECTED MEMBERS
  // ///////////////////////////////////////////////////////////////////////////

  private static MainFrame s_instance = null;

  protected final TextPaneLogHandler logHandler;

  // protected ProgressDialog progressDialog = null;
  // protected InputStream monitoredIS = null;
  // protected long totalBytes = 0;

  /**
   * List of all spawn <code>Process</code> instances.
   */
  protected final ArrayList spawnProcesses = new ArrayList();

  /**
   * List of <code>LoadedFile</code> instances
   */
  protected final ArrayList loadedFiles = new ArrayList();

  protected final RunProgramDialog runProgramDialog;

  /**
   * The number that will be associated to next created view
   */
  protected int currentView = 0;

  // ///////////////////////////////////////////////////////////////////////////
  // CONSTRUCTOR
  // ///////////////////////////////////////////////////////////////////////////

  public static final MainFrame getInstance()
  {
    if (s_instance == null)
    {
      s_instance = new MainFrame();
    }
    return s_instance;
  }

  private MainFrame()
  {
    setTitle(TITLE);

    getContentPane().setLayout(new BorderLayout());

    // MENU BAR ///////////////////////////////////////////////////////////////

    JMenuBar jmbMain = new JMenuBar();
    Utils.setCommonProperties(jmbMain);
    setJMenuBar(jmbMain);

    // File menu

    JMenu jmFile = new JMenu("File");
    Utils.setCommonProperties(jmFile);
    jmFile.setMnemonic('F');
    jmbMain.add(jmFile);

    JMenuItem jmiFileOpen = new JMenuItem(new FileOpenAction());
    Utils.setCommonProperties(jmiFileOpen);
    jmiFileOpen.setMnemonic('O');
    jmiFileOpen.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
    jmFile.add(jmiFileOpen);

    JMenuItem jmiFileExit = new JMenuItem(new FileExitAction());
    Utils.setCommonProperties(jmiFileExit);
    jmiFileExit.setMnemonic('x');
    jmiFileExit.setAccelerator(KeyStroke.getKeyStroke("alt F4"));
    jmFile.add(jmiFileExit);

    // Profiles menu

    jmProfiles = new JMenu("Profiles");
    Utils.setCommonProperties(jmProfiles);
    jmProfiles.setMnemonic('P');
    jmProfiles.setEnabled(false);
    jmbMain.add(jmProfiles);

    // Windows menu

    jmWindows = new JMenu("Windows");
    Utils.setCommonProperties(jmWindows);
    jmWindows.setMnemonic('W');
    jmbMain.add(jmWindows);

    JMenuItem jmiTileActive = new JMenuItem(new TileFramesAction(true /*
                                                                       * only
                                                                       * active
                                                                       */
    ));
    Utils.setCommonProperties(jmiTileActive);
    jmiTileActive.setMnemonic('v');
    jmWindows.add(jmiTileActive);

    JMenuItem jmiTileAll = new JMenuItem(new TileFramesAction(false /*
                                                                     * only
                                                                     * active
                                                                     */
    ));
    Utils.setCommonProperties(jmiTileAll);
    jmiTileAll.setMnemonic('l');
    jmWindows.add(jmiTileAll);

    jmWindows.addSeparator();

    JMenuItem jmiScreenshot = new JMenuItem(new TakeScreenshotAction());
    Utils.setCommonProperties(jmiScreenshot);
    jmiScreenshot.setMnemonic('S');
    jmiScreenshot.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
    jmWindows.add(jmiScreenshot);

    JMenuItem jmiExport = new JMenuItem(new ExportToHtmlAction());
    Utils.setCommonProperties(jmiExport);
    jmiExport.setMnemonic('H');
    jmiExport.setAccelerator(KeyStroke.getKeyStroke("ctrl H"));
    jmWindows.add(jmiExport);

    JMenuItem jmiXmlExport = new JMenuItem(new ExportToXmlAction());
    Utils.setCommonProperties(jmiXmlExport);
    jmiXmlExport.setMnemonic('M');
    jmiXmlExport.setAccelerator(KeyStroke.getKeyStroke("ctrl M"));
    jmWindows.add(jmiXmlExport);

    JMenuItem jmiCsvExport = new JMenuItem(new ExportToCsvAction());
    Utils.setCommonProperties(jmiCsvExport);
    jmiXmlExport.setMnemonic('V');
    jmWindows.add(jmiCsvExport);

    jmWindows.addSeparator();

    // DESKTOP PANE ///////////////////////////////////////////////////////////

    jdpDesktop = new JDesktopPane();
    Utils.setCommonProperties(jdpDesktop);

    getContentPane().add(jdpDesktop, BorderLayout.CENTER);

    // STATUS LINE

    jtfStatus = new JTextField();
    Utils.setCommonProperties(jtfStatus);
    jtfStatus.setBorder(new EmptyBorder(0, 2, 0, 2));
    jtfStatus.setEditable(false);

    getContentPane().add(jtfStatus, BorderLayout.SOUTH);

    // ///////////////////////////////////////////////////////////

    addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent aEvent)
      {
        exitForm();
      }
    });

    // log frame
    logFrame = new LogFrame();
    jdpDesktop.add(logFrame, JLayeredPane.DEFAULT_LAYER);
    logFrame.setVisible(true);
    JMenuItem jmiLog = registerFrame(logFrame.getTitle(), logFrame);
    jmiLog.setMnemonic('L');
    jmiLog.setAccelerator(KeyStroke.getKeyStroke("control L"));

    // LOG MANAGER

    logHandler = new TextPaneLogHandler(logFrame.textPane);
    CustomLogger.INSTANCE.addHandler(logHandler);

    pack();

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    setSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
    setLocation((screenSize.width - FRAME_WIDTH) / 2, (screenSize.height - FRAME_HEIGHT) / 2);

    //
    try
    {
      FiltersRepository.createInstance(new File("../etc/filters/default.xml"));
    }
    catch (SAXParseException saxpe)
    {
      CustomLogger.INSTANCE.log(Level.SEVERE, "unable to parse XML configuration of filters - "
          + saxpe.getMessage() + " @ line " + saxpe.getLineNumber() + ", column "
          + saxpe.getColumnNumber() + ", id = " + saxpe.getPublicId() + ";" + saxpe.getSystemId(),
          saxpe);
    }
    catch (Exception e) // ParserConfigurationException, SAXException, IOExc.
    {
      CustomLogger.INSTANCE.log(Level.SEVERE, "Unable to parse XML configuration of filters", e);
    }

    //
    try
    {
      ConfigurationManager.createInstance(new File("../etc/settings/default.xml"));
    }
    catch (Exception e)
    {
      CustomLogger.INSTANCE.severe("Unable to initialize XML settings - " + e);
    }

    Exception e = ConfigurationManager.getInstance().exception;
    if (e != null)
    {
      String msg;
      if (e instanceof SAXParseException)
      {
        SAXParseException saxpe = (SAXParseException) e;
        msg = saxpe.getMessage() + " @ line " + saxpe.getLineNumber() + ", column "
            + saxpe.getColumnNumber() + ", id = " + saxpe.getPublicId() + ";" + saxpe.getSystemId();
      }
      else
        msg = e.toString();
      CustomLogger.INSTANCE.log(Level.WARNING, "Unable to parse XML settings - " + msg, e);
    }

    //

    /*
     * // TODO (dead code) does not work - remove ? // register CTRL-Tab to
     * switch windows
     * getRootPane().getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
     * InputEvent.CTRL_DOWN_MASK), "nextWindow");
     * getRootPane().getActionMap().put("nextWindow", new AbstractAction() {
     * public void actionPerformed(ActionEvent e) {
     * System.out.println("switching..."); JInternalFrame jif =
     * jdpDesktop.getSelectedFrame(); JInternalFrame[] jifs =
     * jdpDesktop.getAllFrames(); for (int i = 0; i < jifs.length; i++) { if
     * (jifs[i] == jif) { jdpDesktop.setSelectedFrame(jifs[(i + 1) %
     * jifs.length]); return; } }
     * 
     * throw new IllegalStateException("Inconsistency detected in
     * JDesktopPane"); } });
     */

    runProgramDialog = new RunProgramDialog(this);

    setStatusReady();
  }

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  public void showRunProgramDialog(LoadedMethod aMethod)
  {
    if (aMethod == null)
      throw new NullPointerException("Loaded method may not be null");

    setStatusWaiting();

    String command = runProgramDialog.showDialog(aMethod.ownerClass /*
                                                                     * loaded
                                                                     * class
                                                                     */
    , aMethod.sourceLine /* source line */
    );

    if (command == null)
    {
      // cancelled
      setStatusCancelled();
      return;
    }

    // run specified command
    setStatus("Running command " + command);
    try
    {
      Process p = Runtime.getRuntime().exec(command);
      spawnProcesses.add(p);
    }
    catch (IOException ioe)
    {
      CustomLogger.INSTANCE.log(Level.WARNING, "IO exception - " + ioe.getMessage(), ioe);
    }

    setStatusReady();
  }

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  protected void setStatus(String aMsg)
  {
    CustomLogger.INSTANCE.info(aMsg);
    Utils.setTextFieldText(jtfStatus, aMsg);
  }

  protected void setStatusReady()
  {
    setStatus("Ready");
  }

  protected void setStatusCancelled()
  {
    setStatus("Cancelled");
  }

  protected void setStatusWaiting()
  {
    setStatus("Waiting for user input");
  }

  protected void setStatusError(String aMsg, Throwable t)
  {
    aMsg = "Error: " + aMsg;
    CustomLogger.INSTANCE.log(Level.WARNING, aMsg, t);
    Utils.setTextFieldText(jtfStatus, aMsg);
  }

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  protected void showNewViewDialog(LoadedFile aLoadedFile)
  {
    setStatusWaiting();

    NewViewDialog dialog = new NewViewDialog(this, FiltersRepository.getInstance()
        .createFiltersList() /*
                               * list of Filter values
                               */
    , FiltersRepository.getInstance().createDefaultFiltersList() /* default */
    );

    Vector filters = dialog.showDialog();

    if (filters == null)
    {
      // cancelled
      dialog.dispose(); // we won't need the dialog again
      setStatusCancelled();
      return;
    }

    dialog.setReadOnly();

    // setStatus("Creating new view with " + );

    InternalFrame iFrame = new InternalFrame(currentView++, aLoadedFile, this, filters, dialog);
    iFrame.setLocation(currentView * NEW_VIEW_GAP_X, currentView * NEW_VIEW_GAP_Y);
    jdpDesktop.add(iFrame, JLayeredPane.DEFAULT_LAYER);
    iFrame.show();

    aLoadedFile.addView(iFrame);

    setStatus(iFrame.getShortTitle() + " was succesfully created [" + filters.size()
        + " filter(s)]");
  }

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  protected void tileFrames(JInternalFrame[] aFrames)
  {
    // number of remaining frames to handle
    double n_d = aFrames.length;
    int n = (int) n_d;

    // (constant) maximum number of colums in a row
    final double x_d = Math.ceil(Math.sqrt(n_d));

    // number of remaining rows to handle
    double y_d = Math.ceil(n_d / x_d);
    int y = (int) y_d;

    // (constant) total available width
    final double width_d = jdpDesktop.getWidth();

    // (constant) total available height
    final double height_d = jdpDesktop.getHeight();

    // (constant) row height
    final double h_d = height_d / y_d;
    final int h = (int) h_d;

    // current frame number
    int i = 0;

    // current row number
    int row = 0;

    while (n > 0)
    {
      // (constant) number of columns in current row
      final int cols = (int) Math.ceil(((double) n) / ((double) y));

      // (constant) row width in current row
      final double w_d = (width_d / cols);

      for (int col = 0; col < cols; col++)
        aFrames[i++].setBounds((int) (col * w_d) /* x */
        , (int) (row * h_d) /* y */
        , (int) w_d /* width */
        , h /* height */
        );

      ++row;
      --y;
      n -= cols;
    }

    // show all frames (un-iconify)
    for (int j = 0; j < aFrames.length; j++)
      showFrame(aFrames[j]);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // I / O
  // ///////////////////////////////////////////////////////////////////////////

  protected void closeIgnoreException(InputStream aStream)
  {
    try
    {
      aStream.close();
    }
    catch (IOException ioe)
    {
      // nop
    }
  }

  /*
   * ///////////////////////////////////////////////////////////////////////////// //
   * PROGRESS MONITOR CALLBACK
   * /////////////////////////////////////////////////////////////////////////////
   * 
   * public void updateProgress(int aEventCount) throws NullPointerException,
   * IOException { if (progressDialog == null) throw new
   * NullPointerException("progress dialog is null");
   * 
   * if (monitoredIS == null) throw new NullPointerException("monitored input
   * stream is null");
   * 
   * progressDialog.setProgressValue((int) (totalBytes -
   * monitoredIS.available()), aEventCount); }
   */

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////
  public JMenuItem[] registerView(String aTitle, InternalFrame aInternalFrame)
  {
    // update Profiles menu
    JMenuItem result0 = new JMenuItem(new ShowFrameAction(aTitle, aInternalFrame));
    Utils.setCommonProperties(result0);
    aInternalFrame.loadedFile.profileMenu.getItem(0).add(result0);

    // update Windows menu
    JMenuItem result1 = registerFrame(aTitle, aInternalFrame);

    return new JMenuItem[]
    { result0, result1 };
  }

  protected JMenuItem registerFrame(String aTitle, JInternalFrame aFrame)
  {
    JMenuItem result = new JMenuItem(new ShowFrameAction(aTitle, aFrame));
    Utils.setCommonProperties(result);
    jmWindows.add(result);
    return result;
  }

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  public void unregisterView(JMenuItem aViewMenuItem, JMenuItem aWindowMenuItem)
  {
    aViewMenuItem.getParent().remove(aViewMenuItem);
    jmWindows.remove(aWindowMenuItem);
  }

  protected void showFrame(JInternalFrame aFrame)
  {
    aFrame.show();
    aFrame.toFront();

    try
    {
      aFrame.setSelected(true);
    }
    catch (PropertyVetoException pve)
    {
      // nop
    }

    try
    {
      aFrame.setIcon(false);
    }
    catch (PropertyVetoException pve)
    {
      // nop
    }
  }

  protected boolean parse(File file_, IParser parser_)
  {
    setStatus("Loading file " + file_);

    try
    {
      parser_.setFile(file_);
    }
    catch (IOException ioe_)
    {
      setStatusError("I/O error - " + ioe_.getMessage(), ioe_);
      return false;
    }
    catch (ParseException pe_)
    {
      setStatusError("Parse error - " + pe_.getMessage(), pe_);
      return false;
    }
    catch (Exception e_)
    {
      setStatusError("Error - " + e_.getMessage(), e_);
      return false;
    }

    ProgressDialog progressDialog = new ProgressDialog(this, parser_);
    parser_.setProgressMonitor(progressDialog);

    // ParserThread parserThread = new ParserThread(progressDialog, parser_);
    // int prio = Thread.currentThread().getPriority();
    // parserThread.setPriority((prio > Thread.MIN_PRIORITY ? prio - 1 : prio));
    // parserThread.start();

    // display dialog until loading is done or the user cancels the process
    progressDialog.showDialog();

    boolean done = progressDialog.isSuccess();

    if (!done)
    {
      Exception exc = progressDialog.getException();

      if (exc != null)
      {
        setStatusError("Unable to load file", exc);
      }
      else
      {
        setStatusCancelled();
      }

      return false;
    }

    setStatusReady();
    return true;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // ACTION - FILE/OPEN
  // ///////////////////////////////////////////////////////////////////////////

  private FileChooserDialog getFileChooserDialog()
  {
    if (m_fileChooserDialog == null)
    {
      m_fileChooserDialog = new FileChooserDialog(MainFrame.this, null);
    }
    return m_fileChooserDialog;
  }

  protected class FileOpenAction extends AbstractAction
  {
    public FileOpenAction()
    {
      super("Open...");
    }

    public void actionPerformed(ActionEvent aEvent)
    {
      setStatusWaiting();

      File f = getFileChooserDialog().showDialog();
      if (f == null)
      {
        // cancelled
        setStatusCancelled();
        return;
      }

      openFile(f);
    }
  }

  private void openFile(File f)
  {
    EjpFile ejpFile;
    try
    {
      ejpFile = new EjpFile(f);
    }
    catch (ParseException pe_)
    {
      CustomLogger.INSTANCE.log(Level.WARNING, "File " + f
          + " does not have a valid EJP file name - " + pe_.getMessage(), pe_);
      setStatusCancelled();
      return;
    }
    if (ejpFile.isClassloader())
    {
      CustomLogger.INSTANCE.log(Level.WARNING, "Selected file {0} is a classloader file", f);
      setStatusCancelled();
      return;
    }

    File classLoaderFile = ejpFile.getClassloaderFile();
    CustomLogger.INSTANCE.finest("classLoaderFile=" + classLoaderFile);

    ClassloaderParser classloaderParser = new ClassloaderParser();

    boolean done = parse(classLoaderFile, classloaderParser);
    if (!done)
    {
      return;
    }

    CustomLogger.INSTANCE.finest("Finished parsing classloader file");

    ThreadOutputParser threadOutputParser = new ThreadOutputParser();
    threadOutputParser.setMethodTable(classloaderParser);

    // threadOutputParser.setParentNode(root);

    // long lFileSize = f.length();
    // if (lFileSize > Integer.MAX_VALUE)
    // {
    // CustomLogger.INSTANCE.log(Level.WARNING, "File {0} is too big", f);
    // setStatusCancelled();
    // return;
    // }

    done = parse(f, threadOutputParser);
    if (!done)
    {
      return;
    }

    IMethodTimeNode root = new LazyMethodTimeNode2(threadOutputParser.getChildrenNodes());

    // root.setTotalTime(threadOutputParser.getProfileTime());

    setStatus("File loaded successfully");

    /*
     * MethodTimeNode root = new MethodTimeNode(null);
     * 
     * ThreadEventHandler eventHandler; if (USE_LAZY_PARSER) { eventHandler =
     * new LazyEventHandler(); } else { eventHandler = new ThreadEventHandler(); }
     * eventHandler.setup(root); eventHandler.setMethodTable(cleh);
     * 
     * IParser threadEventParser = new LazyParser2(cleh); // new
     * DefaultParser(); //threadEventParser.setEventHandler(eventHandler);
     * 
     * try { boolean threadDone =
     * threadEventParser.parseFile(f.getAbsolutePath(), 0); if (!threadDone) {
     * CustomLogger.INSTANCE.info("Canceled"); return; } } catch (IOException
     * ioe_) { CustomLogger.INSTANCE.warning(ioe_.toString()); return; } catch
     * (ParseException pe_) { CustomLogger.INSTANCE.warning(pe_.toString());
     * return; }
     */

    // build profile menu
    JMenu jmProfile = new JMenu(LoadedFile.getFileName(f));
    Utils.setCommonProperties(jmProfile);
    jmProfiles.add(jmProfile);
    jmProfiles.setEnabled(true);

    // build new LoadedFile instance
    LoadedFile loadedFile = new LoadedFile(f, root, jmProfile);
    loadedFiles.add(loadedFile);

    // build Views menu
    JMenu jmViews = new JMenu("Views");
    Utils.setCommonProperties(jmViews);
    jmProfile.add(jmViews);
    JMenuItem jmiViewNew = new JMenuItem(new ViewsNewAction(loadedFile));
    Utils.setCommonProperties(jmiViewNew);
    jmViews.add(jmiViewNew);
    JMenuItem jmiViewTile = new JMenuItem(new ViewsTileAction(loadedFile));
    Utils.setCommonProperties(jmiViewTile);
    jmViews.add(jmiViewTile);
    jmViews.addSeparator();

    // build Close item
    JMenuItem jmiClose = new JMenuItem(new CloseAction(loadedFile));
    Utils.setCommonProperties(jmiClose);
    jmProfile.add(jmiClose);

    // open a New View dialog
    showNewViewDialog(loadedFile);
  }

  /*
   * protected boolean loadFile(IIParser parser, File f) { // check whether the
   * file is already loaded LoadedFile found = null; int nb =
   * loadedFiles.size(); for (int i = 0; found == null && i < nb; i++) {
   * LoadedFile lf = (LoadedFile) loadedFiles.get(i); if (f.equals(lf.file))
   * found = lf; }
   * 
   * if (found != null) { CustomLogger.INSTANCE.info("File " + f + " was already
   * loaded"); return true; }
   * 
   * totalBytes = f.length(); FileInputStream fis; try { fis = new
   * FileInputStream(f); } catch (FileNotFoundException fnfe) {
   * setStatusError("file not found (" + f + ")"); return false; }
   * 
   * InputStream in; try { in = new BufferedInputStream(fis); monitoredIS = in;
   * if (f.getName().toLowerCase().endsWith(".gz")) in = new
   * BufferedInputStream(new GZIPInputStream(in, 1 //buffer size ));
   * parser.setInputStream(in); } catch (IOException ioe) { setStatusError("I/O
   * exception - " + ioe.getMessage()); return false; }
   * 
   * setStatus("Loading file " + f);
   * 
   * progressDialog = new ProgressDialog(MainFrame.this, (int) totalBytes);
   * 
   * ParserThread parserThread = new ParserThread(parser); int prio =
   * Thread.currentThread().getPriority(); parserThread.setPriority((prio >
   * Thread.MIN_PRIORITY ? prio - 1 : prio)); parserThread.start(); // todo:
   * handle exception from Thread (-> close dialog, status) // display dialog
   * until loading is done or the user cancels the process boolean done =
   * progressDialog.showDialog();
   * 
   * if (!done) { if (parserThread.getException() == null &&
   * parserThread.isAlive()) { CustomLogger.INSTANCE.finest("Stopping
   * parser...");
   * 
   * parser.stopASAP(); // wait for parser thread to die try {
   * parserThread.join(); } catch (InterruptedException ie) { }
   * 
   * CustomLogger.INSTANCE.finest("Parser is stopped"); } } // try to close open
   * streams closeIgnoreException(in); closeIgnoreException(monitoredIS); //
   * reset global variables progressDialog = null; monitoredIS = null;
   * 
   * Exception e = parserThread.getException(); if (e != null) { // exception
   * during parsing setStatusError("during parsing - " + e.getMessage()); return
   * false; }
   * 
   * if (!done) { // cancelled by user setStatusCancelled(); return false; }
   * 
   * return true; }
   */

  // ///////////////////////////////////////////////////////////////////////////
  // ACTION - FILE/EXIT
  // ///////////////////////////////////////////////////////////////////////////
  protected class FileExitAction extends AbstractAction
  {
    public FileExitAction()
    {
      super("Exit");
    }

    public void actionPerformed(ActionEvent aEvent)
    {
      setStatus("Exiting");
      exitForm();
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
  // ACTION - PROFILES/TILE (ALL | ACTIVE) FRAMES
  // ///////////////////////////////////////////////////////////////////////////

  protected class TileFramesAction extends AbstractAction
  {
    protected final boolean onlyActive;

    public TileFramesAction(boolean aOnlyActive)
    {
      super("Tile " + (aOnlyActive ? "Active" : "All") + " Frames");
      onlyActive = aOnlyActive;
    }

    public void actionPerformed(ActionEvent aEvent)
    {
      JInternalFrame[] allFrames = jdpDesktop.getAllFrames();
      ArrayList framesList = new ArrayList();
      for (int i = 0; i < allFrames.length; i++)
        if (!onlyActive || !allFrames[i].isIcon())
          framesList.add(allFrames[i]);

      JInternalFrame[] frames = (JInternalFrame[]) framesList.toArray(new JInternalFrame[0]);

      tileFrames(frames);
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
  // ACTION - PROFILES/VIEWS/NEW
  // ///////////////////////////////////////////////////////////////////////////

  protected class ViewsNewAction extends AbstractAction
  {
    protected final LoadedFile loadedFile;

    public ViewsNewAction(LoadedFile aLoadedFile)
    {
      super("New...");
      loadedFile = aLoadedFile;
    }

    public void actionPerformed(ActionEvent aEvent)
    {
      showNewViewDialog(loadedFile);
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
  // ACTION - PROFILES/VIEWS/TILE
  // ///////////////////////////////////////////////////////////////////////////

  protected class ViewsTileAction extends AbstractAction
  {
    protected final LoadedFile loadedFile;

    public ViewsTileAction(LoadedFile aLoadedFile)
    {
      super("Tile");
      loadedFile = aLoadedFile;
    }

    public void actionPerformed(ActionEvent aEvent)
    {
      tileFrames(loadedFile.getViews());
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
  // ACTION - PROFILES/CLOSE
  // ///////////////////////////////////////////////////////////////////////////

  protected class CloseAction extends AbstractAction
  {
    protected final LoadedFile loadedFile;

    public CloseAction(LoadedFile aLoadedFile)
    {
      super("Close");
      loadedFile = aLoadedFile;
    }

    public void actionPerformed(ActionEvent aEvent)
    {
      setStatus("Closing profile " + loadedFile.getFileName());

      loadedFiles.remove(loadedFile);

      // update Profiles menu
      jmProfiles.remove(loadedFile.profileMenu);
      if (jmProfiles.getItemCount() == 0)
        jmProfiles.setEnabled(false);

      InternalFrame[] views = loadedFile.getViews();
      for (int i = 0; i < views.length; i++)
        views[i].dispose();

      setStatusReady();
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
  // ACTION - PROFILES/VIEWS/SHOW AND WINDOWS/SHOW
  // ///////////////////////////////////////////////////////////////////////////

  protected class ShowFrameAction extends AbstractAction
  {
    protected final JInternalFrame internalFrame;

    public ShowFrameAction(String aTitle, JInternalFrame aInternalFrame)
    {
      super(aTitle);
      internalFrame = aInternalFrame;
    }

    public void actionPerformed(ActionEvent aEvent)
    {
      showFrame(internalFrame);
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
  // ACTION - TAKE SCREENSHOT
  // ///////////////////////////////////////////////////////////////////////////

  protected class TakeScreenshotAction extends AbstractAction
  {
    public TakeScreenshotAction()
    {
      super("Take a screenshot");
    }

    public void actionPerformed(ActionEvent aEvent)
    {
      final JInternalFrame jif = jdpDesktop.getSelectedFrame();
      if (jif != null && !jif.isIcon())
      {
        InternalFrame i = (InternalFrame) jif;
        Dimension d2 = i.tree.getPreferredSize();

        final int x = (int) d2.getWidth();
        final int y = (int) d2.getHeight();
        final BufferedImage bi = new BufferedImage(x, y, BufferedImage.TYPE_INT_BGR);
        i.tree.paint(bi.getGraphics());

        try
        {
          final File file = File.createTempFile("screenshot-" /* prefix */
          , "." + SCREENSHOTS_FORMAT /* suffix */
          , new File("../") /* directory */
          );
          setStatus("Taking screenshot...");
          ImageIO.write(bi, SCREENSHOTS_FORMAT, file);
          setStatus("Screenshot of selected frame saved to file \"" + file + "\"");
        }
        catch (Exception e)
        {
          setStatusError("Unable to save image", e);
        }
      }
      else
      {
        CustomLogger.INSTANCE.warning("Ignoring screenshot, no active frame is selected.");
      }
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
  // ACTION - EXPORT TO HTML
  // ///////////////////////////////////////////////////////////////////////////

  protected class ExportToHtmlAction extends AbstractAction
  {
    public ExportToHtmlAction()
    {
      super("Export to HTML");
    }

    public void actionPerformed(ActionEvent aEvent)
    {
      final JInternalFrame jif = jdpDesktop.getSelectedFrame();
      if (jif != null)
      {
        InternalFrame f = (InternalFrame) jif;

        FileWriter fw = null;
        try
        {
          File file = File.createTempFile("export-", ".html", new File("../"));
          setStatus("Exporting data to HTML...");
          fw = new FileWriter(file);
          f.exportToHtml(fw);
          fw.close();
          fw = null;
          setStatus("Exported selected frame to file \"" + file + "\"");
        }
        catch (IOException e)
        {
          setStatusError("Unable to export data", e);
        }
        finally
        {
          if (fw != null)
          {
            try
            {
              fw.close();
            }
            catch (Throwable t)
            {
              CustomLogger.INSTANCE.warning("Throwable in finally - " + t);
            }
          }
        }
      }
      else
      {
        CustomLogger.INSTANCE.warning("Ignoring export, no frame is selected.");
      }
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
  // ACTION - EXPORT TO XML
  // ///////////////////////////////////////////////////////////////////////////

  protected class ExportToXmlAction extends AbstractAction
  {
    public ExportToXmlAction()
    {
      super("Export to XML");
    }

    public void actionPerformed(ActionEvent aEvent)
    {
      final JInternalFrame jif = jdpDesktop.getSelectedFrame();
      if (jif != null)
      {
        InternalFrame f = (InternalFrame) jif;

        FileWriter fw = null;
        try
        {
          File file = File.createTempFile("export-", ".xml", new File("../"));
          setStatus("Exporting data to XML...");
          fw = new FileWriter(file);
          f.exportToFlatXml(fw);
          fw.close();
          fw = null;
          setStatus("Exported selected frame to file \"" + file + "\"");
        }
        catch (IOException e)
        {
          setStatusError("Unable to export data", e);
        }
        catch (TransformerException e)
        {
          setStatusError("Unable to export data", e);
        }
        finally
        {
          if (fw != null)
          {
            try
            {
              fw.close();
            }
            catch (Throwable t)
            {
              CustomLogger.INSTANCE.warning("Throwable in finally - " + t);
            }
          }
        }
      }
      else
      {
        CustomLogger.INSTANCE.warning("Ignoring export, no frame is selected.");
      }
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
  // ACTION - EXPORT TO CSV
  // ///////////////////////////////////////////////////////////////////////////

  protected class ExportToCsvAction extends AbstractAction
  {
    public ExportToCsvAction()
    {
      super("Export to CSV");
    }

    public void actionPerformed(ActionEvent aEvent)
    {
      final JInternalFrame jif = jdpDesktop.getSelectedFrame();
      if (jif != null)
      {
        InternalFrame f = (InternalFrame) jif;

        FileWriter fw = null;
        try
        {
          File file = File.createTempFile("export-", ".csv", new File("../"));
          setStatus("Exporting data to CSV...");
          fw = new FileWriter(file);
          f.exportToCsv(fw);
          fw.close();
          fw = null;
          setStatus("Exported selected frame to file \"" + file + "\"");
        }
        catch (IOException e)
        {
          setStatusError("Unable to export data", e);
        }
        finally
        {
          if (fw != null)
          {
            try
            {
              fw.close();
            }
            catch (Throwable t)
            {
              CustomLogger.INSTANCE.warning("Throwable in finally - " + t);
            }
          }
        }
      }
      else
      {
        CustomLogger.INSTANCE.warning("Ignoring export, no frame is selected.");
      }
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  protected void exitForm()
  {
    System.exit(0);
  }

  /**
   * Main entry point of the program.
   * 
   * @throws InvocationTargetException
   * @throws InterruptedException
   * 
   * 
   * @param args
   *          any command line arguments (currently ignored).
   */
  public static void main(final String args[]) throws InterruptedException,
      InvocationTargetException
  {
    SwingUtilities.invokeAndWait(new Runnable()
    {
      public void run()
      {
        MainFrame.getInstance().setVisible(true);
        for (int i = 0; i < args.length; i++)
        {
          String arg = args[i];
          File file = new File(arg);
          MainFrame.getInstance().getFileChooserDialog().setFile(file);
          MainFrame.getInstance().openFile(file);
        }
      }
    });
  }
}