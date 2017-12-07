package com.vogella.plugin.markers.handlers;

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

/** <b>Warning</b> : 
  As explained in <a href="http://wiki.eclipse.org/Eclipse4/RCP/FAQ#Why_aren.27t_my_handler_fields_being_re-injected.3F">this wiki page</a>, it is not recommended to define @Inject fields in a handler. <br/><br/>
  <b>Inject the values in the @Execute methods</b>
*/
@SuppressWarnings("restriction")
public class AddMarkerHandler {
	
	private static final int IRESOURCE_TYPE_FILE = 1;
	private static final int IRESOURCE_TYPE_FOLDER = 2;
	private static final int IRESOURCE_TYPE_PROJECT = 4;
	
	private static final String JDT_NATURE = "org.eclipse.jdt.core.javanature";
	
	@Execute
    public void execute(@Named(IServiceConstants.ACTIVE_SELECTION) IStructuredSelection selection, Adapter adapter) {

      Object firstElement = selection.getFirstElement();
      IResource resource = adapter.adapt(firstElement, IResource.class);
		
		colorWithAST(resource);
		
//		System.out.println("Make everything colorful...");
//		
//        if (selection == null || selection.isEmpty()) {
//            return;
//        }
//

//
//        if (resource != null) {
//        	if (resource.getType() == IRESOURCE_TYPE_FILE) {
//        		writeMarkerLineInfo(resource);
//        		writeMarkerUnderline(resource);
//        		writeColouredMarker(resource);
//        	} else if (resource.getType() == IRESOURCE_TYPE_FOLDER) {
//        		writeMarkerTask(resource);
//        	} else if (resource.getType() == IRESOURCE_TYPE_PROJECT) {
//        		writeMarkerTask(resource);
//        	}
//        }
    }

	private void writeMarkerLineInfo(IResource resource) {
	    IMarker marker;
		try {
			marker = resource.createMarker("org.eclipse.viatra2.loaders.vtclparsermarker");
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
			marker.setAttribute(IMarker.MESSAGE, "Line 8");
			marker.setAttribute(IMarker.LINE_NUMBER, 8);
			
		} catch (CoreException e) {
			e.printStackTrace();
		}
    	 
	}
	
	private void writeMarkerUnderline(IResource resource) {
	    IMarker marker;
		try {
			marker = resource.createMarker("org.eclipse.viatra2.loaders.vtclparsermarker");
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
			marker.setAttribute(IMarker.MESSAGE, "Underline it");
			
			// Need position information from AST (Abstract syntax tree)
		   	marker.setAttribute(IMarker.CHAR_START, 2);
			marker.setAttribute(IMarker.CHAR_END, 50);
			
		} catch (CoreException e) {
			e.printStackTrace();
		}
    	 
	}
	
	private void writeMarkerUnderline(IResource resource, int st, int end) {
		try {
			IMarker marker = resource.createMarker("org.eclipse.viatra2.slicemarker");
			marker.setAttribute(IMarker.CHAR_START, st);
			marker.setAttribute(IMarker.CHAR_END, end);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	 
	}
	
	private void writeMarkerTask(IResource resource) {
		try {
            IMarker marker = resource.createMarker(IMarker.TASK);
            marker.setAttribute(IMarker.MESSAGE, "This is a task");
            marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	private void writeColouredMarker(IResource resource) {
		try {
			IMarker marker = resource.createMarker("org.eclipse.viatra2.slicemarker");
			marker.setAttribute(IMarker.CHAR_START, 50);
			marker.setAttribute(IMarker.CHAR_END, 65);
		} catch (Exception e) {
			e.printStackTrace();
		}
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

            
            // Get Methods
            for (MethodDeclaration method : visitor.getMethods()) {
            	// Color function
            	int start = method.getStartPosition();
            	int end = start + method.getLength();
                writeMarkerUnderline(resource, start, end);
            	
                System.out.println("Method name: " + method.getName()
                        + " Return type: " + method.getReturnType2());
            }
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
	
	 
}
