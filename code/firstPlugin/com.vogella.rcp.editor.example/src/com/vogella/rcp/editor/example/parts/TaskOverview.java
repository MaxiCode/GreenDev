package com.vogella.rcp.editor.example.parts;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;

import com.vogella.rcp.editor.example.model.Task;
import com.vogella.rcp.editor.example.model.TaskService;

public class TaskOverview extends ViewPart {

	private static final String ID = "com.vogella.rcp.editor.example.taskoverview";
	private ListViewer viewer;
	
	@Override
	public void createPartControl(Composite parent) {
		viewer = new ListViewer(parent);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object obj) {
				Task p = (Task) obj;
				return p.getSummary();
			};
		});
		viewer.setInput(TaskService.getInstance().getTasks());
		getSite().setSelectionProvider(viewer);
		hookDoubleKlickCommand();
	}
	
	private void hookDoubleKlickCommand (){
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IHandlerService handlerService = getSite().getService(IHandlerService.class);
				try {
					handlerService.executeCommand("com.vogella.rcp.editor.example.openEditor", null);
				} catch (Exception e) {
					throw new RuntimeException(e.getMessage());
				}
			}
		});
		
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

}
