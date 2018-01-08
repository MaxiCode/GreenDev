package model;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import db.Database;
import model.config.Configuration;
import parser.FileParser;

public class PerformanceDataModel {
	
	private String MOST_EXPENSIVE_METHODES_IDENTIFIER = "Most expensive methods (by net time)";
	private String MOST_EXPENSIVE_METHODES_END_IDENTIFIER = "Most expensive methods summarized";
	private String DATE_TIME_IDENTIFIER = "|  Date: ";
	
	
	// Contains the identifier for functions as String and the corresponding Performance Dataset
	private Map<String, PerformanceDataset> dataSet = new HashMap<String, PerformanceDataset>();
	
	public void extractData (File path, Configuration config) {
		FileParser parser = new FileParser();
		parser.readFile(path);
		
		float highestValueInDataset = 0;
		
		String strLine;
		boolean mostExpensiveMethodesArea = false;
		
		try {
			while ((strLine = parser.getNextLine()) != null) {
				
				if (strLine.contains(MOST_EXPENSIVE_METHODES_END_IDENTIFIER)) {
					mostExpensiveMethodesArea = false;
				} else if (mostExpensiveMethodesArea) {
					float actualValue = extractRelevantData(strLine.trim(), config);
					if (highestValueInDataset < actualValue) {
						highestValueInDataset = actualValue;
					}

				} else if (strLine.contains(MOST_EXPENSIVE_METHODES_IDENTIFIER)) {
						mostExpensiveMethodesArea = true;
				
				} else if (strLine.contains(DATE_TIME_IDENTIFIER)) {
					Date dataDate = extractDateTime(strLine);
					if (dataDate != null) {
						config.setDate(dataDate);
					}
				}
			}
			parser.closeInStream();
		} catch (IOException e) {
			System.out.println("EOF or smth else");
			e.printStackTrace();
		}
		config.setHighestValue(highestValueInDataset);
	}
	
	public Map<String, PerformanceDataset> getDataSet () {
		return dataSet;
	}
	
	/**
	 * Use only for debug issues
	 * Output will be to much for syso
	 * Use writeToFile() instead
	 */
	public void printData () {
		System.out.println("Printing Dataset:");
		for ( Map.Entry<String, PerformanceDataset> e : dataSet.entrySet() ) {
			System.out.println("Function: " + e.getKey());
			PerformanceDataset data = e.getValue();
			
			Map<Configuration, List<PerformanceData>> printData = data.getAll();
			for (Entry<Configuration, List<PerformanceData>> d : printData.entrySet()) {
				System.out.println("Configuration: " + d.getKey().getName());
				System.out.println("PerformanceData Ammount: " + d.getValue().size());
			}
		}
	}
	
	public int getDatasetSize() {
		int counter = 0;
		Collection<PerformanceDataset> tmpValues = dataSet.values();
		for (PerformanceDataset v : tmpValues) {
			Map<Configuration, List<PerformanceData>> dataWithConfig = v.getAll();
			for (List<PerformanceData> data : dataWithConfig.values()) {
				counter += data.size();
			}
		}
		return counter;
	}
	
	public int getMapSize() {
		return dataSet.size();
	}
	
	private float extractRelevantData (String line, Configuration config) {
		if (line.length() > 0) {
			if (!Character.isDigit(line.charAt(0))) {
				return 0;
			}
			
			String[] parts = line.split("\\s+");
			if (parts.length != 4) {
				return 0;
			}
			
			// TODO : Remove Hack
			if (parts[3].contains("org.h2.test.mvcc.TestMvccMultiThreaded2:testSelectForUpdateConcurrency") ||
					parts[3].contains("org.h2.test.TestBase$4:invoke") || 
					parts[3].contains("org.h2.test.utils.SelfDestructor$1:run")) {
				return 0;
			}
			
//			int  tmpCount = Integer.parseInt(parts[0]);
			long tmpCountLong = Long.parseLong(parts[0]);
			float tmpPct  = Float.parseFloat(parts[2].replace(',', '.'));
			float tmpTime = Float.parseFloat(parts[1].replace(',', '.'));
			
			PerformanceData data = new PerformanceData();
			data.setPct(tmpPct);
			data.setTime(tmpTime/tmpCountLong);
			
			if (dataSet.containsKey(parts[3])) {
				// if currently read function was read before - append values
				PerformanceDataset tmpData = dataSet.get(parts[3]);
				tmpData.add(config, data);
			} else {
				// else create new data set
				PerformanceDataset tmpData = new PerformanceDataset();
				tmpData.add(config, data);
				dataSet.put(parts[3], tmpData);
			}
			return (tmpTime/tmpCountLong);
		}
		return 0;
	}
	
	private Date extractDateTime (String line) {
		// 2017.11.30 14:37:38 PM
		int index = line.indexOf(DATE_TIME_IDENTIFIER);		
		String dateString = line.substring(DATE_TIME_IDENTIFIER.length() + index);
		
		DateFormat formatter = new SimpleDateFormat( "yyyy.MM.dd hh:mm:ss" );
		Date date = null;
		try {
			date = formatter.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}
	
	/**
	 * Saves performance values to sqlite database
	 */
	public void writeToFile (PrintWriter writer) {
		for ( Map.Entry<String, PerformanceDataset> e : dataSet.entrySet() ) {
			String functionName = e.getKey();
			
			PerformanceDataset data = e.getValue();
			Map<Configuration, List<PerformanceData>> printData = data.getAll();
			for (Entry<Configuration, List<PerformanceData>> d : printData.entrySet()) {
				String configName = d.getKey().getPerformanceFile().getAbsolutePath();
				float highestInConfig = d.getKey().getHighestValue();
				for (PerformanceData element : d.getValue()) {
					writer.println(functionName + "," + configName + "," + highestInConfig + "," + element.getTime());
				}
			}
		}
	}
	public void writeToDb(Database db) {
		System.out.println("Start writing to db.");
		
		String functionName;
		String configName;
		float  time;
		float  fraction;
		float  highestInConfig;
		
		db.initBatch();
		
		for ( Map.Entry<String, PerformanceDataset> e : dataSet.entrySet() ) {
			functionName = e.getKey();
			
			PerformanceDataset data = e.getValue();
			Map<Configuration, List<PerformanceData>> printData = data.getAll();
			for (Entry<Configuration, List<PerformanceData>> d : printData.entrySet()) {
				configName = d.getKey().getPerformanceFile().getAbsolutePath();
				highestInConfig = d.getKey().getHighestValue();
				for (PerformanceData element : d.getValue()) {
					time = element.getTime();
					fraction = time*100/highestInConfig;
					db.insertData(functionName, configName, time, fraction);
				}
			}
		}
		
		db.executeBatch();
	}
}
 