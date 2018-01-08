package main;

import java.io.File;
import java.util.List;

import db.Database;
import fileHandler.PerformanceFileHandler;
import model.PerformanceDataModel;
import model.config.Configuration;

public class PerformanceDataCalculator {
	
	private static PerformanceFileHandler handler;
	private static Database db;

	public static void main(String[] args) {
		System.out.println("Start parsing performance files.");
		
		PerformanceDataModel model = new PerformanceDataModel();
		
		handler = new PerformanceFileHandler();
		handler.readFiles();
		handler.initOutputWriter();
		
		db = new Database(handler.getOutputDir());
		
		// pFiles contain all performance files of the H2 project
//		List<File> pFiles = handler.getFilesH2();
		
		// pFiles contain all performance files of the Catena project
		List<File> pFiles = handler.getFilesCa();
		
		for (File f : pFiles) {
			System.out.println();
			System.out.println("Processing File: " + f.getAbsolutePath());
			
			Configuration config = new Configuration();
			config.setPerformanceFile(f);
			config.setName("Default Configuration");
						
			model.extractData(f, config);

			System.out.println("Start writing output.");
//			model.writeToFile(handler.getOutputWriter());
			model.writeToDb(db);
			System.out.println("Done writing output.");
			
		}
		handler.closeWriter();
		db.closeDbConnection();
	}

}
