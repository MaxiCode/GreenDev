package com.vogella.plugin.markers.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.adapter.Adapter;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.viewers.IStructuredSelection;

@SuppressWarnings("restriction")
public class AddMarkerHandler {
	
	private static final String JDT_NATURE = "org.eclipse.jdt.core.javanature";
	private static final List<String> predefinedMarker = new ArrayList<String>();
	
	private Map<String, Double> performanceValues = new HashMap<String, Double>();
	
	@Execute
    public void execute(@Named(IServiceConstants.ACTIVE_SELECTION) IStructuredSelection selection, Adapter adapter) {

		Object firstElement = selection.getFirstElement();
		IResource resource = adapter.adapt(firstElement, IResource.class);
		
		initMarkerMap();
		readPerformanceFile();
		
		System.out.println("Values Size: " + performanceValues.size());
//		for (String bla : performanceValues.keySet()) {
//    		System.out.println("Key in Map: " + bla);
//    	}
		colorWithAST(resource);
    }
	
	/**
	 * initialize list of colors
	 * care sequence: add colors from green to red
	 */
	private void initMarkerMap() {
		predefinedMarker.add("org.eclipse.max.slicemarker.green");
		predefinedMarker.add("org.eclipse.max.slicemarker.yellow");
		predefinedMarker.add("org.eclipse.max.slicemarker.red");
	}
	
	private void colorWithAST(IResource resource) {
		System.out.println("Start coloring project with AST...");
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        // Get all projects in the workspace
        IProject[] projects = root.getProjects();
        // Loop over all projects
        for (IProject project : projects) {
            try {
                if (project.isNatureEnabled(JDT_NATURE)) {
                    analyseMethods(project, resource);
                }
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
	}
	
	private void analyseMethods(IProject project, IResource resource) throws JavaModelException {
        IPackageFragment[] packages = JavaCore.create(project)
                .getPackageFragments();
        // parse(JavaCore.create(project));
        for (IPackageFragment mypackage : packages) {
            if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
                createAST(mypackage, resource);
            }

        }
    }
	
	private void createAST(IPackageFragment mypackage, IResource resource)
            throws JavaModelException {
        for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
        	
            // now create the AST for the ICompilationUnits
            CompilationUnit parse = parse(unit);
            MethodVisitor visitor = new MethodVisitor();
            parse.accept(visitor);

            if (!unit.getElementName().equalsIgnoreCase(resource.getName())) {
            	continue;
            }
        	String handleIdentifier =unit.getHandleIdentifier();
        	String startOfPackage = handleIdentifier.substring(
        			handleIdentifier.indexOf("org."), 
        			handleIdentifier.length()-5);
        	String classString = startOfPackage.replace('{', '.');
        	
            // Get Methods
            for (MethodDeclaration method : visitor.getMethods()) {
            	// Color function
            	String functionIdentifier = classString+":"+method.getName();
            	Double pValue = performanceValues.get(functionIdentifier);
            	
            	if (pValue == null) {
            		continue;
            	}
            	
        		int start = method.getStartPosition();
            	int end = start + method.getLength();
            	double d = pValue;
            	float performanceFraction = (float) d;
                writeMarkerUnderline(resource, start, end, performanceFraction);	            		
            }
        }
    }
	
	private void writeMarkerUnderline(IResource resource, int st, int end, float performance) {
		// performance is a value from 0 to 100
		// map it to nearest values in hash map predefinedMarker
		int availableColors = predefinedMarker.size();
		int targetColor = Math.round(performance*(availableColors-1)/100);
//		System.out.println("Number: " + targetColor);
//		System.out.println("Corresp. Marker: " + predefinedMarker.get(targetColor));
//		System.out.println("Start: " + st + " end: " + end);
		
		try {
			IMarker marker = resource.createMarker(predefinedMarker.get(targetColor));
			marker.setAttribute(IMarker.CHAR_START, st);
			marker.setAttribute(IMarker.CHAR_END, end);
			System.out.println("Done Coloring " + resource.getName() + " with " + predefinedMarker.get(targetColor));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
     * Reads a ICompilationUnit and creates the AST DOM for manipulating the
     * Java source file
     *
     * @param unit
     * @return
     */
    private static CompilationUnit parse(ICompilationUnit unit) {
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(unit);
        parser.setResolveBindings(true);
        
        return (CompilationUnit) parser.createAST(null); // parse
    }
    
    private void readPerformanceFile() {
    	PerformanceValues pV = new PerformanceValues();
    	pV.readPerformanceFiles();
    	performanceValues = pV.getData();
    }
    
    
    // ------------------------------------------------------------------------------------
    // testing area
    // ------------------------------------------------------------------------------------
    
//	private void writeMarkerTask(IResource resource) {
//		try {
//            IMarker marker = resource.createMarker(IMarker.TASK);
//            marker.setAttribute(IMarker.MESSAGE, "This is a task");
//            marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//	}
//	
//	private void writeColouredMarker(IResource resource) {
//		try {
//			IMarker marker = resource.createMarker("org.eclipse.viatra2.slicemarker");
//			marker.setAttribute(IMarker.CHAR_START, 50);
//			marker.setAttribute(IMarker.CHAR_END, 65);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
//	private void writeMarkerLineInfo(IResource resource) {
//	    IMarker marker;
//		try {
//			marker = resource.createMarker("org.eclipse.viatra2.loaders.vtclparsermarker");
//			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
//			marker.setAttribute(IMarker.MESSAGE, "Line 8");
//			marker.setAttribute(IMarker.LINE_NUMBER, 8);
//			
//		} catch (CoreException e) {
//			e.printStackTrace();
//		}
//    	 
//	}
//	
//	private void writeMarkerUnderline(IResource resource) {
//	    IMarker marker;
//		try {
//			marker = resource.createMarker("org.eclipse.viatra2.loaders.vtclparsermarker");
//			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
//			marker.setAttribute(IMarker.MESSAGE, "Underline it");
//			
//			// Need position information from AST (Abstract syntax tree)
//		   	marker.setAttribute(IMarker.CHAR_START, 2);
//			marker.setAttribute(IMarker.CHAR_END, 50);
//			
//		} catch (CoreException e) {
//			e.printStackTrace();
//		}
//    	 
//	}

}
