package parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PerformanceDataModel {
	
	private List<PerformanceDataset> dataSet = new ArrayList<PerformanceDataset>();

	private String MOST_EXPENSIVE_METHODES_IDENTIFIER = "Most expensive methods summarized";
	
	public void parseFile (String path) {
		FileParser parser = new FileParser();
		parser.readFile(path);
		
		String strLine;
		boolean aemsArea = false;
		
		try {
			while ((strLine = parser.getNextLine()) != null){
				
				if (aemsArea) {
					extractRelevantData(strLine.trim());
				} else {
					if (strLine.contains(MOST_EXPENSIVE_METHODES_IDENTIFIER)) {
						aemsArea = true;
					}
				}
			}
			parser.closeInStr();

		} catch (IOException e) {
			System.out.println("EOF or smth else");
			e.printStackTrace();
		}
	}
	
	public List<PerformanceDataset> getDataSet () {
		return dataSet;
	}
	
	private void extractRelevantData (String line) {
		if (line.length() > 0) {
			if (Character.isDigit(line.charAt(0))) {
				PerformanceDataset data = new PerformanceDataset();
				String[] parts = line.split("\\s+");
				
				data.setCount(Integer.parseInt(parts[0]));
				data.setTime(Float.parseFloat(parts[1].replace(',', '.')));
				data.setPct(Float.parseFloat(parts[2].replace(',', '.')));
				data.addLocation(parts[3]);
				
				dataSet.add(data);
			}
		}
	}
	
	public void printData () {
		for (PerformanceDataset data : dataSet) {
			String location = "";
			for (String l : data.getLocation()) {
				location+= " "+l;
			}
			System.out.println(data.getCount() + " " + data.getTime() + " " + data.getPct() + " " + location.trim());
		}
	}
}
