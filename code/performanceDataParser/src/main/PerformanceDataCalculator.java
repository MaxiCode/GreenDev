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
		handler.readProfilerOutputCa();
		List<File> catenaProfilingSubDirs = handler.getProfileSubdirsCa();

		db = new Database(handler.getOutputDir());
		for (File dir : catenaProfilingSubDirs) {
			String configName =  dir.getName();
			String configDate = "";
			String configParameter = "";
			
			// There are 2 files now: 
			// One parameter file and one profiling file
			File[] files = dir.listFiles();
			File proFile = null;
			for(File f : files) {
				if (f.getName().equals("parameter.txt")) {
					configParameter = model.extractParameter(f);
				} else {
					configDate = f.getName().substring(0, 15);
					proFile = f;
				}
			}
			
			if (proFile == null) {
				continue;
			}
			
			Configuration config = new Configuration();
			config.setDate(configDate);
			config.setName(configName);
			config.setParameters(configParameter);
			config.setPerformanceFile(dir);
			
			model.extractData(proFile, config);
			model.writeToDb(db);
		}
		db.closeDbConnection();
	}

}
