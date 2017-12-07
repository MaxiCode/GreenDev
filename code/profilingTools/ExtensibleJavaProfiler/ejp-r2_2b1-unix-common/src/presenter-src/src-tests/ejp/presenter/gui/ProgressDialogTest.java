/*
 $Id: ProgressDialogTest.java,v 1.6 2005/02/14 12:12:22 vauclair Exp $
 
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;

import ejp.presenter.parser.IProgressMonitor;
import ejp.presenter.parser.ITask;

public class ProgressDialogTest
{
  public static void main(String[] args_)
  {
    final JFrame jd = new JFrame();
    jd.addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent e)
      {
        System.exit(0);
      }
    });

    final JButton jb = new JButton("Start");
    jb.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        ProgressDialog spd = new ProgressDialog(jd, new TestTask());
        spd.showDialog();
        System.out.println("show done; success: " + spd.isSuccess() + "; exception: "
            + spd.getException());
      }

    });
    jd.getContentPane().add(jb);
    jd.pack();
    jd.setVisible(true);

  }

  // public static void main(String[] args_)
  // {
  // final int NB = 10000;
  //
  // final ProgressDialog progressDialog = new ProgressDialog(new JFrame(), new
  // TestTask());
  //		
  //    
  // final IProgressMonitor progressMonitor = (IProgressMonitor) progressDialog;
  //
  // progressMonitor.notifyTotal(NB);
  //
  // (new Thread()
  // {
  // public void run()
  // {
  // for (int i = 1; i < NB; i++)
  // {
  // try
  // {
  // sleep(1);
  // }
  // catch (InterruptedException ie_)
  // {
  // }
  // progressMonitor.notifyProgress(i);
  // }
  // progressDialog.closeDialog();
  // }
  // }).start();
  //
  // System.out.println(progressDialog.showDialog());
  // System.exit(0);
  // }

  private static class TestTask implements ITask
  {
    private static final int LAST = 100;

    private IProgressMonitor m_progressMonitor = null;

    private boolean m_stopRequested = false;

    public boolean isStoppable()
    {
      return true;
    }

    public boolean isMonitorable()
    {
      return true;
    }

    public void setProgressMonitor(IProgressMonitor progressMonitor_)
    {
      m_progressMonitor = progressMonitor_;
    }

    public synchronized void stop()
    {
      m_stopRequested = true;
    }

    private synchronized boolean isStopRequested()
    {
      return m_stopRequested;
    }

    public String getTitle()
    {
      return "Test task";
    }

    public String getLabel()
    {
      return "Test task";
    }

    public boolean start() throws Exception
    {
      boolean result = true;

      m_progressMonitor.notifyTotal(LAST);
      for (int i = 1; i <= LAST; i++)
      {
        if (isStopRequested())
        {
          result = false;
          break;
        }

        long start = System.currentTimeMillis();
        do
        {
          // nop
        }
        while (System.currentTimeMillis() < start + 75);
        m_progressMonitor.notifyProgress(i);

        if (i == 95)
        {
          // throw new Exception("95% payload!");
        }
      }

      return result;
    }
  }
}
