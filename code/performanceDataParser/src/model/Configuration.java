package model;

public class Configuration {

	private String configName;
	
	public Configuration () {
		configName = "Default";
	}
	
	public void setConfig (String config) {
		configName = config;
	}
	
	public String getName () {
		return configName;
	}
}
