package com.vogella.plugin.markers.handlers;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.di.annotations.Execute;

import com.vogella.plugin.markers.performance.MarkerUtils;

public class ClearMarkerHandler {
	
	@Execute
    public void execute() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
	    IWorkspaceRoot root = workspace.getRoot();
	    // Get all projects in the workspace
	    IProject[] projects = root.getProjects();
	    // Loop over all projects
	    for (IProject project : projects) {
	    	try {
	    		MarkerUtils.cleanProject(project);
			} catch (CoreException e) {
				e.printStackTrace();
			}
	    }
	}
	
}
