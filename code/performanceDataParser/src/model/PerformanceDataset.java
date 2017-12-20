package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.config.Configuration;

public class PerformanceDataset {

	private Map<Configuration, List<PerformanceData>> dataPerConfig = new HashMap<Configuration, List<PerformanceData>>();
	
	public void add(Configuration c, PerformanceData d) {
		if (dataPerConfig.containsKey(c)) {
			List<PerformanceData> tmpData = dataPerConfig.get(c);
			tmpData.add(d);
		} else {
			List<PerformanceData> dataList = new ArrayList<PerformanceData>();
			dataList.add(d);
			dataPerConfig.put(c, dataList);
		}
	}
	
	public void remove(Configuration c) {
		if (dataPerConfig.containsKey(c)) {
			dataPerConfig.remove(c);
		}
	}
	
	public void clearAll() {
		dataPerConfig = new HashMap<Configuration, List<PerformanceData>>();
	}
	
	public Map<Configuration, List<PerformanceData>> getAll() {
		return dataPerConfig;
	}
	
	public int getMapSize() {
		return dataPerConfig.size();
	}
	
	public List<PerformanceData> get(Configuration c) {
		return dataPerConfig.get(c);
	}
}
