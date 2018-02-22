package main;

import java.io.File;
import java.sql.Connection;
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
		
		
		
		handler = new PerformanceFileHandler();
		handler.readProfilerOutput();
		
		List<File> catenaProfilingSubDirs = handler.getProfileSubdirsCa();
		List<File> h2ProfilingSubDirs = handler.getProfileSubdirsH2();
		List<File> sunflowProfilingSubDirs = handler.getProfileSubdirsSun();
		String outputDir = handler.getOutputDir();

		PerformanceDataCalculator dataCalc = new PerformanceDataCalculator();
		db = new Database(outputDir);
		Connection cCa = db.getConnectionCa();
		Connection cH2 = db.getConnectionH2();
		Connection cSun = db.getConnectionSun();
		
		
		dataCalc.parse(catenaProfilingSubDirs, db, cCa);
		dataCalc.parse(h2ProfilingSubDirs, db, cH2);
		dataCalc.parse(sunflowProfilingSubDirs, db, cSun);
	}
	
	private void parse(List<File> pfileSubDirs, Database db, Connection c) {
		PerformanceDataModel model = new PerformanceDataModel(c);
		for (File dir : pfileSubDirs) {
			String configName =  dir.getName();
			System.out.println("Config: " + configName);
			String configDate = "";
			String[] configParameter = null;
			
			// There are many files now: 
			// One parameter file and several other files (profiling files with date 
			// and possibly images for quality measurement)
			
			File[] files = dir.listFiles();
			File[] proFiles = new File[3];
			int i = 0;
			for(File f : files) {
				if (f.getName().equals("parameter.txt")) {
					configParameter = model.extractParameter(f);
				} else if (f.getName().endsWith(".png")) {
				} else {
					proFiles[i] = f;
					i++;
				}
			}
			
			if (proFiles.length!=3) {
				System.out.println("Number files does not match");
				continue;
			}
			for(i=0; i < proFiles.length; ++i) {
				configDate = proFiles[i].getName().substring(0, 15);
				Configuration config = new Configuration();
				config.setDate(configDate);
				config.setName(configName+"__"+configParameter[i]);
				config.setParameters(configParameter[i]);

				model.extractData(proFiles[i], config);
				model.writeToDb(db);
				model.clearData();
			}
		}
		db.closeDbConnection(c);
	}

}
