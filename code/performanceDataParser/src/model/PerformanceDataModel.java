package model;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import model.config.Configuration;
import parser.FileParser;

public class PerformanceDataModel {
	
	private String MOST_EXPENSIVE_METHODES_IDENTIFIER = "Most expensive methods (by net time)";
	private String MOST_EXPENSIVE_METHODES_END_IDENTIFIER = "Most expensive methods summarized";
	private String DATE_TIME_IDENTIFIER = "|  Date: ";
	
	
//	private Date dataIdentifier = new Date();
	// Contains the identifier for functions as String and the corresponding Performance Dataset
	private Map<String, PerformanceDataset> dataSet = new HashMap<String, PerformanceDataset>();
//	private double highestTimeinDataset = 0.0d;
//	private String idOfHighestDataset = "";

	
	public void extractData (File path, Configuration config) {
		FileParser parser = new FileParser();
		parser.readFile(path);
		
		String strLine;
		boolean mostExpensiveMethodesArea = false;
		
		System.out.println("Inside creating model.");
		int count = 0;
		try {
			while ((strLine = parser.getNextLine()) != null) {
				
				if (strLine.contains(MOST_EXPENSIVE_METHODES_END_IDENTIFIER)) {
					mostExpensiveMethodesArea = false;
				} else if (mostExpensiveMethodesArea) {
					count++;
					extractRelevantData(strLine.trim(), config);

				} else if (strLine.contains(MOST_EXPENSIVE_METHODES_IDENTIFIER)) {
					System.out.println("Found Most expensive methodes id.");
						mostExpensiveMethodesArea = true;
				
				} else if (strLine.contains(DATE_TIME_IDENTIFIER)) {
					Date dataDate = extractDateTime(strLine);
					if (dataDate != null) {
						config.setDate(dataDate);
					}
				}
			}
			parser.closeInStream();
			System.out.println(count + " Lines processed.");
		} catch (IOException e) {
			System.out.println("EOF or smth else");
			e.printStackTrace();
		}
	}
	
	public Map<String, PerformanceDataset> getDataSet () {
		return dataSet;
	}
	
//	public void printData () {
//		System.out.println("Printing Dataset from: " + dataIdentifier);
//		for ( Map.Entry<String, PerformanceDataset> e : dataSet.entrySet() ) {
//			PerformanceDataset data = e.getValue();
//			System.out.println(data.getCount() + " " + 
//					data.getTime() + " " + 
//					data.getPct() + " " + 
//					e.getKey());
//		}
//	}
	
//	public void printPerformanceFractionPerFunction (PrintWriter writer) {
//		
//		for ( Map.Entry<String, PerformanceDataset> e : dataSet.entrySet() ) {
//			PerformanceDataset data = e.getValue();
//			
//			if (data.getTime() > 0) {
//				double fraction = (data.getTime()/data.getCount())*100/highestTimeinDataset;
//				
//				DecimalFormat df = new DecimalFormat("#.####");
////				writer.println("Current: " + df.format(data.getTime()) + "\t Anz: " + data.getCount() + "  \t Fraction: " + 
////						df.format(fraction) + "\t fkt: " + e.getKey());
//			} else {
//				System.out.println("What happened here");
//			}
//		}
//	}
	
	public int getDatasetSize() {
		return dataSet.size();
	}
	
	
	private void extractRelevantData (String line, Configuration config) {
		if (line.length() > 0) {
			if (!Character.isDigit(line.charAt(0))) {
				return;
			}
			
			String[] parts = line.split("\\s+");
			if (parts.length != 4) {
				return;
			}
			
			// TODO : Remove Hack
			if (parts[3].contains("org.h2.test.mvcc.TestMvccMultiThreaded2:testSelectForUpdateConcurrency") ||
					parts[3].contains("org.h2.test.TestBase$4:invoke") || 
					parts[3].contains("org.h2.test.utils.SelfDestructor$1:run")) {
				return;
			}
			
			int  tmpCount = Integer.parseInt(parts[0]);
			float tmpPct  = Float.parseFloat(parts[2].replace(',', '.'));
			float tmpTime = Float.parseFloat(parts[1].replace(',', '.'));
			
			PerformanceData data = new PerformanceData();
			data.setCount(tmpCount);
			data.setPct(tmpPct);
			data.setTime(tmpTime);
			
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
		}
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
}
