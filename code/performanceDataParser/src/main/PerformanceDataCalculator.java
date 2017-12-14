package main;

import java.io.File;
import java.util.List;

import fileHandler.PerformanceFileHandler;
import model.PerformanceDataModel;

public class PerformanceDataCalculator {
	
	private static PerformanceFileHandler handler; 

	public static void main(String[] args) {
		System.out.println("Start parsing performance files.");
		
		handler = new PerformanceFileHandler();
		handler.readFiles();
		List<File> pFiles = handler.getFiles();
		
		for (File f : pFiles) {
			System.out.println();
			System.out.println("Processing File: " + f.getAbsolutePath());
			handler.initOutputWriter();
			
			PerformanceDataModel model = new PerformanceDataModel();
			
			model.createModel(f);
			
			System.out.println("Highest Value: " + model.getHighest());
			System.out.println("Highest Element: " + model.getIdHighestElement());
			System.out.println("Dataset Size: " + model.getDatasetSize());

			System.out.println("Start writing output.");
			model.printPerformanceFractionPerFunction(handler.getOutputWriter());
			handler.closeWriter();
		}
		
	}

}
