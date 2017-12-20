package model.config;

import java.io.File;
import java.util.Date;

public class Configuration {

	private String configName;
	private Date date;
	private File performanceFile;
	private float highestValueOfConfig;
	
	
	public void setHighestValue(float value) {
		highestValueOfConfig = value;
	}
	
	public float getHighestValue() {
		return highestValueOfConfig;
	}
	
	public void setPerformanceFile(File f) {
		performanceFile = f;
	}
	
	public File getPerformanceFile() {
		return performanceFile;
	}
	
	public void setName(String config) {
		configName = config;
	}

	public String getName() {
		return configName;
	}
	
	public void setDate(Date d) {
		date = d;
	}
	
	public Date getDate() {
		return date;
	}
}
