package com.vogella.plugin.markers.testing;

import org.eclipse.jdt.core.ICompilationUnit;

public class PerfModel implements Comparable<PerfModel> {
	public ICompilationUnit unit;
	public float value;
	public String path;

	public PerfModel(ICompilationUnit unit, String path, float value) {
		this.unit = unit;
		this.path = path;
		this.value = value;
	}
	
	public String getPath() {
		return this.path;
	}
	
	public String getValue() {
		return String.valueOf(this.value);
	}

	@Override
	public int compareTo(PerfModel o) {
		Float f1 = this.value;
		Float f2 = o.value;
		return f2.compareTo(f1);
	}
}
