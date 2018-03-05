/*******************************************************************************
 * Copyright (c) 2006, 2014 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 414565
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 448143
 *******************************************************************************/

package com.vogella.plugin.markers.testing;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;

/**
 * TableViewer: Hide full selection
 *
 */
public class PerfItemSelection extends ViewPart {

	TableViewer v;
	
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
	}
	
	public boolean viewExist() {
		if (v == null) {
			return false;
		} else {
			return true;
		}
	}
	
	public PerfItemSelection() {
		super();
	}
	
	public void updateContent(PerfModel[] m) {
		PerfModel[] model = m;
		v.setInput(model);
		v.getTable().setLinesVisible(true);
		v.getTable().setHeaderVisible(true);
	}

	@Override
	public void createPartControl(Composite parent) {
		v = new TableViewer(parent);
		v.setContentProvider(ArrayContentProvider.getInstance());

		TableColumn column = new TableColumn(v.getTable(),SWT.NONE);
		column.setWidth(150);
		column.setText("Performance Value");
		TableViewerColumn viewerColumn1 = new TableViewerColumn(v, column);
		viewerColumn1.setLabelProvider(new ColumnLabelProvider() {
			
			@Override
			public String getText(Object element) {
				return ((PerfModel) element).getValue();
			}
		});

		column = new TableColumn(v.getTable(),SWT.NONE);
		column.setWidth(200);
		column.setText("Path");
		TableViewerColumn viewerColumn2 = new TableViewerColumn(v, column);
		viewerColumn2.setLabelProvider(new ColumnLabelProvider() {
			
			@Override
			public String getText(Object element) {
				return ((PerfModel) element).getPath();
			}
		});
		
		v.addDoubleClickListener(new IDoubleClickListener() {
			
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection clickedObject = (IStructuredSelection) event.getSelection();
				Object eventObj = clickedObject.getFirstElement();
				
				if (eventObj instanceof PerfModel) {
					PerfModel m = (PerfModel) eventObj;
					ICompilationUnit uToOpen = m.unit;
					
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            		IWorkbenchPage page = window.getActivePage();
            		
            		IFile file = (IFile) uToOpen.getResource();
            		try {
						IDE.openEditor(page, file);
					} catch (PartInitException e) {
						e.printStackTrace();
            		}
				}
			}
		});
	}

	@Override
	public void setFocus() {
		
	}
}
