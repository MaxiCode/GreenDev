package model;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import parser.FileParser;

public class PerformanceDataModel {
	
	private String MOST_EXPENSIVE_METHODES_IDENTIFIER = "Most expensive methods summarized";
	private String DATE_TIME_IDENTIFIER = "|  Date: ";
	
	
	private Date dataIdentifier = new Date();
	private Map<String, PerformanceDataset> dataSet = new HashMap<String, PerformanceDataset>();
	private float highestTimeinDataset = 0.0f;

	
	public void parseFile (String path) {
		FileParser parser = new FileParser();
		parser.readFile(path);
		
		String strLine;
		boolean mostExpensiveMethodesArea = false;
		
		try {
			while ((strLine = parser.getNextLine()) != null){
				
				if (mostExpensiveMethodesArea) {
					extractRelevantData(strLine.trim());

				} else if (strLine.contains(MOST_EXPENSIVE_METHODES_IDENTIFIER)) {
						mostExpensiveMethodesArea = true;
				
				} else if (strLine.contains(DATE_TIME_IDENTIFIER)) {
					Date dataDate = extractDateTime(strLine);
					if (dataDate != null) {
						dataIdentifier = dataDate;
					}
				}
			}
			parser.closeInStr();

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
	
	private void extractRelevantData (String line) {
		if (line.length() > 0) {
			if (Character.isDigit(line.charAt(0))) {
				PerformanceDataset data = new PerformanceDataset();
				String[] parts = line.split("\\s+");
				
				int  tmpCount = Integer.parseInt(parts[0]);
				float tmpTime = Float.parseFloat(parts[1].replace(',', '.'));
				
				data.setCount(tmpCount);
				data.setTime(tmpTime);
				data.setPct(Float.parseFloat(parts[2].replace(',', '.')));
				
				if (tmpTime/tmpCount>highestTimeinDataset) {
					highestTimeinDataset = tmpTime/tmpCount;
				}
				
				dataSet.put(parts[3].trim(), data);
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
	
	public void printData () {
		System.out.println("Printing Dataset from: " + dataIdentifier);
		for ( Map.Entry<String, PerformanceDataset> e : dataSet.entrySet() ) {
			PerformanceDataset data = e.getValue();
			System.out.println(data.getCount() + " " + 
					data.getTime() + " " + 
					data.getPct() + " " + 
					e.getKey());
		}
	}
	
	public void printPerformanceFractionPerFunction (PrintWriter writer) {
		float check = 0;
		float currentFraction = 0;
		String nameOfHighestClass = ""; 
		
		for ( Map.Entry<String, PerformanceDataset> e : dataSet.entrySet() ) {
			PerformanceDataset data = e.getValue();
			
			
			
			if (data.getTime() > 0) {
				float fraction = (data.getTime()/data.getCount())*100/highestTimeinDataset;
				
				// ----- Checking Area
				check += fraction;
				if (fraction > currentFraction) {
					currentFraction = fraction;
					nameOfHighestClass = e.getKey();
				}
				
				
				// ----- Checking Area
				writer.println("Current: " + data.getTime() + "\t Anz: " + data.getCount() + "  \t Fraction: " + 
						fraction + "\t fkt: " + e.getKey());
//				System.out.println("Current: " + data.getTime() + "\t Anz: " + data.getCount() + "  \t Fraction: " + 
//						fraction + "\t fkt: " + e.getKey());
			}
		}
		System.out.println("Check: " + check);
		System.out.println("NameOFClass: " + nameOfHighestClass);
		System.out.println("Highest Fraction: " + currentFraction);
	}
}
