package com.vogella.plugin.markers.handlers;

import org.eclipse.e4.core.di.annotations.Execute;

import com.vogella.plugin.markers.performance.PerformanceHandlerUtils;

public class ClearMarkerHandler {
	
	@Execute
    public void execute() {
		PerformanceHandlerUtils pHandler = new PerformanceHandlerUtils(0);
		pHandler.clearMarker();
	}
	
}
