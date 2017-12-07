package parser;

import java.util.ArrayList;
import java.util.List;

public class PerformanceDataset {

	/**
	 * The number of times that the given method 
	 * called by the enclosing method.
	 */
	private int count;
	
	/**
	 * Net Time. The amount of time that was actually 
	 * spent executing this method if you factor out 
	 * the total time taken by calling other (listed) 
	 * methods.
	 */
	private float time;
	
	/**
	 * The net time for this method expressed as a 
	 * percent of the total time for the given thread.
	 */
	private float pct;
	
	/**
	 * Location path of called function.
	 */
	private List<String> location = new ArrayList<String>();
	
	
	
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public float getTime() {
		return time;
	}

	public void setTime(float time) {
		this.time = time;
	}

	public float getPct() {
		return pct;
	}

	public void setPct(float pct) {
		this.pct = pct;
	}
	
	public List<String> getLocation(){
		return location;
	}
	
	// TODO: Divide locations into parts
	public void addLocations(String locations) {
//		List<String> tmpLocations = new ArrayList<String>();
		String[] tmp = locations.split(".");
		for (String t : tmp) {
			System.out.println(t);
		}
	}
	
	// TODO: Divide locations into parts
	public void addLocation(String l) {
		location.add(l);
	}
	
}
