package main;

import java.io.File;
import java.util.List;

import fileHandler.PerformanceFileHandler;

public class PerformanceDataCalculator {
	
	private static PerformanceFileHandler handler; 

	public static void main(String[] args) {
		handler = new PerformanceFileHandler();
		
		handler.readFiles();
		
		List<File> blaFiles = handler.getFiles();
		
		System.out.println("blaF: " + blaFiles.size());
		
		// output performance values
		handler.initOutputWriter();
		
//		handler.writeOutput("Test");
		
		handler.closeWriter();
		
	}

}
