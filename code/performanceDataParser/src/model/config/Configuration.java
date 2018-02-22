package model.config;

public class Configuration {

	private String configName;
	private String date;
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
