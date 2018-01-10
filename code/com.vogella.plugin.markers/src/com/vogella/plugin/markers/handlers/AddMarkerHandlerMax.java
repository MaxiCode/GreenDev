package com.vogella.plugin.markers.handlers;

import java.util.List;
import java.util.Map;

import javax.inject.Named;

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
public class AddMarkerHandlerMax {
	
	private static final String JDT_NATURE = "org.eclipse.jdt.core.javanature";
	private DatabaseConnection conn;
	private Map<String, Integer> functionNames;
	
	@Execute
    public void execute(@Named(IServiceConstants.ACTIVE_SELECTION) IStructuredSelection selection, Adapter adapter) {

		Object firstElement = selection.getFirstElement();
		IResource resource = adapter.adapt(firstElement, IResource.class);
		
		conn = null;
		functionNames = null;
		
//		readPerformanceFile();
		initFunctionNames();
		
		colorWithAST(resource);
    }
	
	private void colorWithAST(IResource resource) {
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

            // Filter current class == looped class
            if (!unit.getElementName().equalsIgnoreCase(resource.getName())) {
            	continue;
            }
            
        	String classString = extractFileIdentifier(unit.getHandleIdentifier());
        	String fullPathOfRecource = resource.getFullPath().toString();
        	fullPathOfRecource = fullPathOfRecource.replace('/', '.');
        	// filter wrong resources if class names in projects are the same
        	if (!fullPathOfRecource.contains(classString)) {
            	continue;
            }
        	
            // Get Methods
            for (MethodDeclaration method : visitor.getMethods()) {
            	// Color function
            	String functionIdentifier = classString+":"+method.getName();
            	Integer functionsPrimaryKey = functionNames.get(functionIdentifier);
            	
            	if (functionsPrimaryKey == null) {
            		continue;
            	}
            	
            	List<Float> fractions = conn.getFraction(functionsPrimaryKey);
            	float highest = 0;
            	for (float el : fractions) {
            		System.out.println("El: " + el);
            		if (highest<el) highest = el;
            	}
            	
        		int start = method.getStartPosition();
            	int end = start + method.getLength();
                MarkerUtils.writeMarkerUnderline(resource, start, end, highest);
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
        ASTParser parser = ASTParser.newParser(AST.JLS9);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(unit);
        parser.setResolveBindings(true);
        
        return (CompilationUnit) parser.createAST(null); // parse
    }
    
    private String extractFileIdentifier(String input) {
//    	=catena/src<main.java.components.graph.algorithms{DoubleButterflyGraph.java
//    	=h2/src\/main<org.h2.engine{Database.java
    	String startOfPackage = input.substring(
    			(input.indexOf("<")+1),	// find start of package path 
    			input.length()-5);		// removing '.java'
    	return startOfPackage.replace('{', '.');
    }
    
    private void initFunctionNames() {
    	conn = new DatabaseConnection();
    	functionNames = conn.getFunctionNames();
    }
    
}
