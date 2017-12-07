/*
 $Id: ProgressDialog.java,v 1.11 2005/02/17 12:15:36 vauclair Exp $
 
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

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import ejp.presenter.parser.IProgressMonitor;
import ejp.presenter.parser.ITask;

/**
 * Loading progress dialog.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.11 $<br>$Date: 2005/02/17 12:15:36 $</code>
 */
public class ProgressDialog extends JDialog implements IProgressMonitor, ActionListener, Runnable
{
  public static final DecimalFormat percentFormat = new DecimalFormat("0.00 %");

  /**
   * Minimum delay (in ms) between two progress updates.
   */
  public static final long UPDATE_PERIOD = 250;

  // ///////////////////////////////////////////////////////////////////////////
  // COMPONENTS
  // ///////////////////////////////////////////////////////////////////////////

  protected final JProgressBar jpbProgress;

  protected final JTextField jtfEventCount;

  protected final JButton jbCancel;

  // ///////////////////////////////////////////////////////////////////////////
  // FIELDS
  // ///////////////////////////////////////////////////////////////////////////

  private final ITask m_task;

  private Exception m_exception = null;

  private boolean m_success = false;

  // protected boolean cancelled = false;

  // protected boolean disposed = false;

  protected long m_total = -1;

  protected double m_ratio = 1;

  protected long prevUpdateTime = 0;

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  public ProgressDialog(Frame aParent, ITask task_)
  {
    super(aParent, true); // create a modal dialog

    m_task = task_;
    m_task.setProgressMonitor(this);

    GridBagConstraints gbcTemp;
    JLabel jlTemp;

    getContentPane().setLayout(new GridBagLayout());

    // PROGRESS BAR

    jpbProgress = new JProgressBar();
    Utils.setCommonProperties(jpbProgress);
    jpbProgress.setMinimum(0);
    jpbProgress.setStringPainted(task_.isMonitorable());
    jpbProgress.setIndeterminate(!task_.isMonitorable());

    gbcTemp = new GridBagConstraints();
    gbcTemp.gridx = 0;
    gbcTemp.gridy = 0;
    gbcTemp.gridwidth = 2;
    gbcTemp.insets = Utils.INSETS_5;
    gbcTemp.anchor = GridBagConstraints.NORTHWEST;
    getContentPane().add(jpbProgress, gbcTemp);

    // EVENT COUNT (LABEL)

    jlTemp = new JLabel(task_.getLabel());
    Utils.setCommonProperties(jlTemp);

    gbcTemp = new GridBagConstraints();
    gbcTemp.gridx = 0;
    gbcTemp.gridy = 1;
    gbcTemp.insets = Utils.INSETS_5;
    gbcTemp.anchor = GridBagConstraints.NORTHWEST;
    getContentPane().add(jlTemp, gbcTemp);

    // EVENT COUNT (VALUE)

    jtfEventCount = new JTextField();
    Utils.setCommonProperties(jtfEventCount);
    jtfEventCount.setHorizontalAlignment(SwingConstants.RIGHT);
    jtfEventCount.setBorder(new EmptyBorder(0, 0, 0, 0));
    jtfEventCount.setEditable(false);
    jtfEventCount.setVisible(task_.isMonitorable());

    gbcTemp = new GridBagConstraints();
    gbcTemp.gridx = 1;
    gbcTemp.gridy = 1;
    gbcTemp.insets = Utils.INSETS_5;
    gbcTemp.anchor = GridBagConstraints.NORTHWEST;
    gbcTemp.fill = GridBagConstraints.HORIZONTAL;
    getContentPane().add(jtfEventCount, gbcTemp);

    // CANCEL BUTTON

    jbCancel = new JButton("Cancel");
    Utils.setCommonProperties(jbCancel);
    jbCancel.addActionListener(this);
    jbCancel.setEnabled(task_.isStoppable());

    gbcTemp = new GridBagConstraints();
    gbcTemp.gridx = 0;
    gbcTemp.gridy = 2;
    gbcTemp.gridwidth = 2;
    gbcTemp.insets = Utils.INSETS_5;
    gbcTemp.anchor = GridBagConstraints.CENTER;
    getContentPane().add(jbCancel, gbcTemp);

    // DIALOG

    setTitle(task_.getTitle());
    setResizable(false);
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    notifyTotal(100);
    notifyProgress(0);

    pack();

    // set location
    Utils.centerDialog(this, aParent);
  }

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  public void showDialog()
  {
    Thread thread = new Thread(this);
    thread.setPriority(Math.max(Thread.currentThread().getPriority() - 1, Thread.MIN_PRIORITY));
    thread.start();
    super.setVisible(true);
    // TODO (later) keep a track of thread?
  }

  public synchronized boolean isSuccess()
  {
    return m_success;
  }

  public synchronized Exception getException()
  {
    return m_exception;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // ActionListener INTERFACE
  // ///////////////////////////////////////////////////////////////////////////

  public void actionPerformed(ActionEvent aEvent)
  {
    Object src = aEvent.getSource();
    if (src == jbCancel)
    {
      // cancelled = true;
      // closeDialog();
      m_task.stop();
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
  // 
  // ///////////////////////////////////////////////////////////////////////////

  public void run()
  {
    boolean done = false;
    try
    {
      done = m_task.start();
    }
    catch (Exception e_)
    {
      synchronized (this)
      {
        m_exception = e_;
      }
    }

    synchronized (this)
    {
      m_success = done;
    }

    // unblock the thread that invoked show()
    dispose();
  }

  // ///////////////////////////////////////////////////////////////////////////
  // 
  // ///////////////////////////////////////////////////////////////////////////

  public void notifyTotal(long total_)
  {
    m_total = total_;

    int max;
    double ratio;
    if (total_ > Integer.MAX_VALUE)
    {
      max = Integer.MAX_VALUE;
      ratio = (double) total_ / (double) Integer.MAX_VALUE;
    }
    else
    {
      max = (int) total_;
      ratio = 1;
    }

    jpbProgress.setMaximum(max);
    m_ratio = ratio;
  }

  public void notifyProgress(long current_)
  {
    long time = System.currentTimeMillis();
    if (time - prevUpdateTime >= UPDATE_PERIOD)
    {
      prevUpdateTime = time;

      jpbProgress.setValue((int) (current_ / m_ratio));
      jpbProgress.setString(percentFormat.format((double) current_ / (double) m_total));

      Utils.setTextFieldText(jtfEventCount, new Long(current_ / 1024).toString());
    }
  }
}
