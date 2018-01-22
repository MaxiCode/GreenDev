package model.config;

import java.io.File;

public class Configuration {

	private String configName;
	private String date;
	private File performanceFile;
	private float highestValueOfConfig;
	private String parameter;
	
	
	public void setParameters(String parameter) {
		this.parameter = parameter;
	}
	
	public String getParameter() {
		return this.parameter;
	}
	
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
	
	public void setDate(String d) {
		date = d;
	}
	
	public String getDate() {
		return date;
	}
}
