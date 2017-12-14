package model;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import parser.FileParser;

public class PerformanceDataModel {
	
	private String MOST_EXPENSIVE_METHODES_IDENTIFIER = "Most expensive methods (by net time)";
	private String MOST_EXPENSIVE_METHODES_END_IDENTIFIER = "Most expensive methods summarized";
	private String DATE_TIME_IDENTIFIER = "|  Date: ";
	
	
	private Date dataIdentifier = new Date();
	private Map<String, PerformanceDataset> dataSet = new HashMap<String, PerformanceDataset>();
	private double highestTimeinDataset = 0.0d;
	private String idOfHighestDataset = "";

	
	public void createModel (File path) {
		FileParser parser = new FileParser();
		parser.readFile(path);
		
		String strLine;
		boolean mostExpensiveMethodesArea = false;
		
		System.out.println("Inside creating model.");
		int count = 0;
		try {
			while ((strLine = parser.getNextLine()) != null){
				
				if (strLine.contains(MOST_EXPENSIVE_METHODES_END_IDENTIFIER)) {
					mostExpensiveMethodesArea = false;
				} else if (mostExpensiveMethodesArea) {
					count++;
					extractRelevantData(strLine.trim());

				} else if (strLine.contains(MOST_EXPENSIVE_METHODES_IDENTIFIER)) {
					System.out.println("Found Most expensive methodes id.");
						mostExpensiveMethodesArea = true;
				
				} else if (strLine.contains(DATE_TIME_IDENTIFIER)) {
					Date dataDate = extractDateTime(strLine);
					if (dataDate != null) {
						dataIdentifier = dataDate;
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
	
	public Date getID () {
		return dataIdentifier;
	}
	
	public void printData () {
		System.out.println("Printing Dataset from: " + dataIdentifier);
		for ( Map.Entry<String, PerformanceDataset> e : dataSet.entrySet() ) {
			PerformanceDataset data = e.getValue();
//			System.out.println(data.getCount() + " " + 
//					data.getTime() + " " + 
//					data.getPct() + " " + 
//					e.getKey());
		}
	}
	
	public void printPerformanceFractionPerFunction (PrintWriter writer) {
		
		for ( Map.Entry<String, PerformanceDataset> e : dataSet.entrySet() ) {
			PerformanceDataset data = e.getValue();
			
//			if (data.getTime() > 0) {
//				double fraction = (data.getTime()/data.getCount())*100/highestTimeinDataset;
//				
//				DecimalFormat df = new DecimalFormat("#.####");
////				writer.println("Current: " + df.format(data.getTime()) + "\t Anz: " + data.getCount() + "  \t Fraction: " + 
////						df.format(fraction) + "\t fkt: " + e.getKey());
//			} else {
//				System.out.println("What happened here");
//			}
		}
	}
	
	public double getHighest() {
		return highestTimeinDataset;
	}
	
	public String getIdHighestElement() {
		return idOfHighestDataset;
	}
	
	public int getDatasetSize() {
		return dataSet.size();
	}
	
	
	private void extractRelevantData (String line) {
		if (line.length() > 0) {
			if (Character.isDigit(line.charAt(0))) {
				String[] parts = line.split("\\s+");
				if (parts.length != 4) {
					return;
				}
				PerformanceDataset data = new PerformanceDataset();
				
				int  tmpCount = Integer.parseInt(parts[0]);
				float tmpTime = Float.parseFloat(parts[1].replace(',', '.'));
				if (tmpTime < 0.001) {
					return;
					
				}
//				data.setCount(tmpCount);
//				data.setTime(tmpTime);
//				data.setPct(Float.parseFloat(parts[2].replace(',', '.')));
				
				// TODO : Remove Hack
				if (parts[3].contains("org.h2.test.mvcc.TestMvccMultiThreaded2:testSelectForUpdateConcurrency") ||
						parts[3].contains("org.h2.test.TestBase$4:invoke") || 
						parts[3].contains("org.h2.test.utils.SelfDestructor$1:run")) {
					return;
				}
				
				// id and time of highest function
				if (tmpTime/tmpCount>highestTimeinDataset) {
					highestTimeinDataset = tmpTime/tmpCount;
					idOfHighestDataset = parts[3];
				}
				
				// don't overwrite bigger hashmap values
//				if (dataSet.containsKey(parts[3])) {
//					PerformanceDataset tmpData = dataSet.get(parts[3]);
//					double ratio = tmpData.getTime()/tmpData.getCount();
//					if (ratio < tmpTime/tmpCount) {
//						dataSet.put(parts[3].trim(), data);
//					}
//				} else {
//					dataSet.put(parts[3].trim(), data);
//				}
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
