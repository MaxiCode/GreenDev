package fileHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class PerformanceFileHandler {

	// contains the 3 paths relative to the performance data parser to
	// H2 - Java Database
	// Gs - Java Graph Library
	// Ca - Java Catena Password Hashing Framework
	private String pathToH2Profile = "./../subjectSystems/h2database/h2/";
	private String pathToGsProfile = "./../subjectSystems/gs-core/";
	private String pathToCaProfile = "./../subjectSystems/catena/";

	private String profileFileName = "profile";
	private String profileFileExtension = ".txt";
	
	private String pathToProfileOutput = "./../profilingOutput/";
	
	// oFN - output file name
	// oFE - output file extension
	private String oFN = "test";
	private String oFE = ".txt";
	
	private List<File> performanceFilesH2 = new ArrayList<File>();
	private List<File> performanceFilesGs = new ArrayList<File>();
	private List<File> performanceFilesCa = new ArrayList<File>();
	
	private PrintWriter outputWriter;

	
	//TODO Add output writer for different projects
	
	/**
	 * Initializes default output File Writer
	 * @return true if Print Writer is initialized successfully
	 */
	public boolean initOutputWriter() {
		File outputDir = new File(pathToProfileOutput);
		int numFiles = 0;
		if (outputDir.isDirectory()) {
			File[] filesInOutputDir = outputDir.listFiles();
			numFiles = filesInOutputDir.length;
			try {
				outputWriter = new PrintWriter(pathToProfileOutput+oFN+(numFiles+1)+oFE, "UTF-8");
				System.out.println("Output Writer Done.");
				return true;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public void writeOutput(String line) {
		outputWriter.println(line);
	}
	
	public PrintWriter getOutputWriter() {
		return outputWriter;
	}
	
	public void closeWriter() {
		outputWriter.close();
	}
	
	public void readFiles() {
		readAllFiles(performanceFilesH2, pathToH2Profile);
		readAllFiles(performanceFilesGs, pathToGsProfile);
		readAllFiles(performanceFilesCa, pathToCaProfile);
	}
	
	private void readAllFiles(List<File> pf, String path) {
		File file = new File(path);
		if (file.exists() && file.isDirectory()) {
			File[] files = file.listFiles();
			
			for (File f : files) {
				if (f.getName().contains(profileFileExtension) && 
						f.getName().contains(profileFileName)) {
					pf.add(f);
				}
			}
		}
	}
	
	/**
	 * List of Performance Files of the H2 Database 
	 * @return	List of Files
	 */
	public List<File> getFilesH2() {
		return performanceFilesH2;
	}
	
	/**
	 * List of Performance Files of the Java Graph Library 
	 * @return	List of Files
	 */
	public List<File> getFilesGs() {
		return performanceFilesGs;
	}
	
	/**
	 * List of Performance Files of the Catena Password Hashing Framework 
	 * @return	List of Files
	 */
	public List<File> getFilesCa() {
		return performanceFilesCa;
	}
	
	public String getOutputDir() {
		return pathToProfileOutput;
	}
}
