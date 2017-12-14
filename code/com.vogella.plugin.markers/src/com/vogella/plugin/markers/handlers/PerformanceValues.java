package com.vogella.plugin.markers.handlers;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PerformanceValues {
	
	private String performanceValueSource = "./../profilingOutput/";
	private List<File> performanceFiles = new ArrayList<File>();
	
	private Map<String, Double> dataSet = new HashMap<String, Double>();
	
	public void readPerformanceFiles() {
		File folder = new File(performanceValueSource);
		if (folder.isDirectory()) {
			File[] files = folder.listFiles();
			if (files.length == 0) {
				return;
			}
			for (File f : files) {
				performanceFiles.add(f);
			}
		}
		
		readData();
	}
	
	private void readData() {
		for (File f : performanceFiles) {
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(
						new DataInputStream(new FileInputStream(f))));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			String strLine = "";
			try {
				while((strLine = br.readLine()) != null) {
//					System.out.println(strLine);
					String[] parts = strLine.split("\\s+");
					// 1 - Time | 3 - Count | 5 - Fraction | 7 - Key
					if (parts.length != 8) {
						continue;
					}
					dataSet.put(parts[7], Double.valueOf(parts[5]));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public Map<String, Double> getData(){
		return dataSet;
	}
}
