package model;

public class PerformanceData {
	
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
}
