package com.vogella.plugin.markers.performance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

import com.vogella.plugin.markers.db.DatabaseConnection;
import com.vogella.plugin.markers.testing.PerfItemSelection;
import com.vogella.plugin.markers.testing.PerfModel;

public class PerformanceHandlerUtils {

	/**
	 * mode: color source code
	 * 
	 * mode 0: standard mode
	 * mode 1: with one configuration
	 * mode 2: with all configuration (MAX)
	 * mode 3: with all configuration (MEAN)
	 * mode 4: with all configuration (STANDARD DEVIATION)
	 * mode 5: with all configuration (SENSITIVITY) 
	 */
	private int mode = 0;
	private String projectID = "catena";
	
	private static final String JDT_NATURE = "org.eclipse.jdt.core.javanature";
	private static final String viewID = "com.vogella.plugin.markers.view1";
	
	private int configOfInterest = 3;
	private List<ICompilationUnit> units = new ArrayList<ICompilationUnit>();
	
	private DatabaseConnection conn;
	private Map<String, Integer> functionNames;
	
	private PerfItemSelection myView;
	private List<IResource> resources = new ArrayList<IResource>();
	
	private List<PerfModel> modelArray;
	private IWorkspace workspace;
	
	public PerformanceHandlerUtils(int mode) {
		workspace = ResourcesPlugin.getWorkspace();
		this.mode = mode;
		this.modelArray = new ArrayList<>();
		
		myView = getView(viewID);
		
		conn = null;
		functionNames = null;
		initDatabase();
	}
	
	private void getResources(String project) {
        IWorkspaceRoot root = workspace.getRoot();
        IProject[] projects = root.getProjects();
        for (IProject p : projects) {
        	try {
				if (!p.isNatureEnabled(JDT_NATURE)) return;
					
				IPackageFragment[] packages = JavaCore.create(p).getPackageFragments();
		        for (IPackageFragment mypackage : packages) {
		        	if (!mypackage.getPath().toString().contains(project)) return;
		        	
	        		if (mypackage.getKind() != IPackageFragmentRoot.K_SOURCE) return;
	                
	        		for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
	        			System.out.println("Unit: " + unit.getElementName().toString());
	                	System.out.println("Resource: " + unit.getResource().toString());
	        			resources.add(unit.getResource());
	                	units.add(unit);
	                }
	        	}
			} catch (CoreException e) {
				e.printStackTrace();
			}
        }
	}
	
	public void analyze() {
		getResources(projectID);
		if (units.isEmpty()) return;
		
		for (IResource resource : resources) {
			analyze(resource);
		}
		updateView();
	}
	
	private void updateView() {
		// update view with colored data:
		if (myView.viewExist()) {
			System.out.println("Size of array " + modelArray.size());
			
			PerfModel[] array = new PerfModel[modelArray.size()]; 
			array = modelArray.toArray(array);
			Arrays.sort(array);
			
			// sort it by value
			myView.updateContent(array);
		} else {
			System.out.println("View does not exist");
		}
	}
	
	private void analyze(IResource resource) {
        for (ICompilationUnit unit : units) {
        	
            // now create the AST for the ICompilationUnits
            CompilationUnit parse = parse(unit);
            MethodVisitor visitor = new MethodVisitor();
            parse.accept(visitor);

            // Filter current class == looped class
            if (!unit.getElementName().equalsIgnoreCase(resource.getName())) {
            	continue;
            }
            
            System.out.println("\nNew unit.");
            
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
            	
            	System.out.println("Methods: " + method.getName());
            	System.out.println("Visitor size: " + visitor.getMethods().size());
            	
            	if (functionsPrimaryKey == null) {
            		continue;
            	}
            	System.out.println("\nFkt: " + functionIdentifier);
            	int start = method.getStartPosition();
            	int end = start + method.getLength();
            	
            	// -------------------------------------------------------------------------
            	// Decide between different modes while coloring
            	// -------------------------------------------------------------------------
            	// TODO put this in functions --->
            	
            	float value;
            	
            	if (mode == 1) {
            		System.out.println("Do coloring max with one config.");
            		List<Float> fractions = conn.getFraction(functionsPrimaryKey, configOfInterest);
            		System.out.println(fractions.size() + " values to compare.");
                	
                	float highest = 0;
                	for (float el : fractions) {
                		if (highest<el) highest = el;
                	}
                	value = highest;
                    
            	} else if (mode == 2) {
            		System.out.println("Do max of all confs");
            		List<Float> fractions = conn.getFraction(functionsPrimaryKey);
            		System.out.println(fractions.size() + " values to compare.");
            		float highest = 0;
                	for (float el : fractions) {
                		if (highest<el) highest = el;
                	}
                	value = highest;
                    
            	} else if (mode == 3) {
            		System.out.println("Do mean of all confs");
            		List<Float> fractions = conn.getFraction(functionsPrimaryKey);
            		System.out.println(fractions.size() + " values to compare.");
                	float sum = 0;
                	int number = 0;
                	for (float el : fractions) {
                		number+=1;
                		sum += el;
                	}
                	value = (sum/number);
                    
            	} else if (mode == 4) {
            		System.out.println("Do std dev of all confs");
            		Statistics s = new Statistics();
                	List<Float> timeValues = conn.getTimeValues(functionsPrimaryKey);
            		System.out.println(timeValues.size() + " values to compare.");
                	timeValues = conn.getFraction(functionsPrimaryKey);

                	for (float el : timeValues) {
                		s.update(el);
                	}
                	value = (float)s.getStandardDeviation();
                    
            	} else if (mode == 5) {
            		System.out.println("Do sensitivity of input");
            		List<Float> fractions = conn.getFraction(functionsPrimaryKey);
            		
            		value = calcAbsMeanMedian(fractions);
            		System.out.println("Sensitivity is: " + value);
            	} else {
            		value = 0.0f;
            	}
            	
            	MarkerUtils.writeMarkerUnderline(resource, start, end, value);
            	
            	PerfModel m = new PerfModel(unit, functionIdentifier, value);
            	modelArray.add(m);
            }
        }
    }
    
    private void initDatabase() {
    	System.out.println("Init Database and read all functions");
    	conn = new DatabaseConnection();
    	functionNames = conn.getFunctionNames();
    }
	
    public String extractFileIdentifier(String input) {
//    	=catena/src<main.java.components.graph.algorithms{DoubleButterflyGraph.java
//    	=h2/src\/main<org.h2.engine{Database.java
    	String startOfPackage = input.substring(
    			(input.indexOf("<")+1),	// find start of package path 
    			input.length()-5);		// removing '.java'
    	return startOfPackage.replace('{', '.');
    }
    
	/**
     * Reads a ICompilationUnit and creates the AST DOM for manipulating the
     * Java source file
     *
     * @param unit
     * @return
     */
    public CompilationUnit parse(ICompilationUnit unit) {
        ASTParser parser = ASTParser.newParser(AST.JLS9);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(unit);
        parser.setResolveBindings(true);
        
        return (CompilationUnit) parser.createAST(null); // parse
    }
    
    /**
     * calculates mean and median of List of floats
     * 
     * @param fractions	- are mapped relative to all 
     * 					  execution times of one configuration 
     * 					  and one project 
     * @return			- abs(mean, median) * 2 -> mapping from 0 to 100
     * 					- max(abs(mean, median)) possible is 50  
     */
    private float calcAbsMeanMedian(List<Float> fractions){
    	float sum = 0;
    	int number = 0;
    	for (float el : fractions) {
    		number+=1;
    		sum += el;
    	}
    	float mean = (sum/number);
    	
    	Float[] fArray = fractions.toArray(new Float[fractions.size()]);
    	Arrays.sort(fArray);
		float median = fArray[Math.round(fArray.length / 2.0f)];

    	return (Math.abs(mean-median)*2);
    	
    }
    
    public static PerfItemSelection getView(String id) {
		IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(viewID);
		if (view instanceof PerfItemSelection) {
			System.out.println("Found view.");
			return (PerfItemSelection) view;
		}
		return null;
	}
    
    public void clearMarker() {
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
	    modelArray = new ArrayList<>();
	    updateView();
    }
    
}
