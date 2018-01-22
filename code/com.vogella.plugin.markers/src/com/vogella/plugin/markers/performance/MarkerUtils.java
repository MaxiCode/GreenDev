package com.vogella.plugin.markers.performance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class MarkerUtils {
	
	private static final List<String> predefinedMarker = Arrays.asList(
			"org.eclipse.max.slicemarker.green", 
			"org.eclipse.max.slicemarker.green1",
			"org.eclipse.max.slicemarker.yellowgreen",
			"org.eclipse.max.slicemarker.yellow1",
			"org.eclipse.max.slicemarker.yellow",
			"org.eclipse.max.slicemarker.yellow2",
			"org.eclipse.max.slicemarker.redyellow",
			"org.eclipse.max.slicemarker.red1",
			"org.eclipse.max.slicemarker.red");
	
	/**
	 * Removes all markers for all projects in the list
	 * 
	 * @param projectList
	 * @throws CoreException
	 */
	public static void cleanProjects(List<IProject> projectList) throws CoreException {
		for (IProject p : projectList)
			cleanProject(p);
	}

	/**
	 * Removes all markers for the project
	 * 
	 * @param project
	 * @throws CoreException
	 */
	public static void cleanProject(IProject project) throws CoreException {
		List<IMarker> markers = getMarkers(project);
		for (IMarker marker : markers) {
			try {
				marker.delete();
			} catch (CoreException e) {
				throw e;
			}
		}
		System.out.println("Cleaning Marker Done.");
	}
	
	private static List<IMarker> getMarkers(IResource res) throws CoreException {
		List<IMarker> returnList = new ArrayList<IMarker>();
		if (res != null) {
			for (String marker : predefinedMarker) {
				try {
					returnList.addAll(Arrays.asList(res.findMarkers(marker, true, IResource.DEPTH_INFINITE)));
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
		return returnList;
	}
	
	public static void writeMarkerUnderline(IResource resource, int st, int end, float performance) {
		// performance is a value from 0 to 100
		// map it to nearest values in hash map predefinedMarker
		int availableColors = predefinedMarker.size();
		int targetColor = Math.round(performance*(availableColors-1)/100);

		try {
			IMarker marker = resource.createMarker(predefinedMarker.get(targetColor));
			marker.setAttribute(IMarker.CHAR_START, st);
			marker.setAttribute(IMarker.CHAR_END, end);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
