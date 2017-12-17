package main;

import java.io.File;
import java.util.List;

import fileHandler.PerformanceFileHandler;
import model.PerformanceDataModel;
import model.config.Configuration;

public class PerformanceDataCalculator {
	
	private static PerformanceFileHandler handler; 

	public static void main(String[] args) {
		System.out.println("Start parsing performance files.");
		
		PerformanceDataModel model = new PerformanceDataModel();
		
		handler = new PerformanceFileHandler();
		handler.readFiles();
		handler.initOutputWriter();
		
		// pFiles contain all performance files of the H2 project
		List<File> pFiles = handler.getFilesH2();
		
		for (File f : pFiles) {
			System.out.println();
			System.out.println("Processing File: " + f.getAbsolutePath());
			
			Configuration config = new Configuration();
			config.setPerformanceFile(f);
			config.setName("Default Configuration");
			
			model.extractData(f, config);
//			
//			System.out.println("Highest Value: " + model.getHighest());
//			System.out.println("Highest Element: " + model.getIdHighestElement());
//			System.out.println("Dataset Size: " + model.getDatasetSize());
//
//			System.out.println("Start writing output.");
//			model.printPerformanceFractionPerFunction(handler.getOutputWriter());
			handler.closeWriter();
		}
		
	}

}
