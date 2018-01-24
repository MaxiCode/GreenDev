package com.vogella.plugin.markers.handlers;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.adapter.Adapter;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.vogella.plugin.markers.performance.PerformanceHandlerUtils;

@SuppressWarnings("restriction")
public class AddMarkerHandlerOneCfgMax {
	
	@Execute
    public void execute(@Named(IServiceConstants.ACTIVE_SELECTION) IStructuredSelection selection, Adapter adapter) {

		int mode = 1;
		PerformanceHandlerUtils pHandler = new PerformanceHandlerUtils(mode);
		pHandler.analyzeSourceCode(selection, adapter);
    }
}