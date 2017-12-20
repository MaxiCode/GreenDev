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
	private String txtFileIdentifier = ".txt";
	
	private Map<String, Float> dataSet = new HashMap<String, Float>();
	
	public void readPerformanceFiles() {
		File folder = new File(performanceValueSource);
		if (folder.isDirectory()) {
			File[] files = folder.listFiles();
			if (files.length == 0) {
				return;
			}
			for (File f : files) {
				if (f.getName().contains(txtFileIdentifier)) {
					performanceFiles.add(f);
				}
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
					// read from 2 different file formats
					// 1 - Time | 3 - Count | 5 - Fraction | 7 - Key
					String[] parts = strLine.split("\\s+");
					// 0- Key | 1 - config | 2 - highest Value | 3 - current Value
					String[] parts2 = strLine.split(",");
					
					if (parts.length == 8) {
						dataSet.put(parts[7], Float.valueOf(parts[5]));
					} else if (parts2.length == 4) {
						dataSet.put(parts2[0], (Float.valueOf(parts2[3]) * 
								100 / Float.valueOf(parts2[2])));
					} else {
						continue;
					}
					
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public Map<String, Float> getData(){
		return dataSet;
	}
}
