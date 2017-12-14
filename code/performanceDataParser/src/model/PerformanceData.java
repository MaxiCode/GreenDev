package model;

public class PerformanceData {

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
	private double time;
	
	/**
	 * The net time for this method expressed as a 
	 * percent of the total time for the given thread.
	 */
	private double pct;
	
	
	
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}

	public double getPct() {
		return pct;
	}

	public void setPct(double pct) {
		this.pct = pct;
	}
}
