package fileHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PerformanceFileHandler {

	// contains the 3 paths relative to the performance data parser to
	// H2 - Java Database
	// Gs - Java Graph Library
	// Ca - Java Catena Password Hashing Framework
	private String pathToSunProfile = "./sunflow.jar/";
	private String pathToH2Profile = "./h2.jar/";
	private String pathToCaProfile = "./catenaExtract.jar/";
	private String pathToProfiles = "/home/max/uni/GreenDev/code/rawProfilingOutput/";
	
	private List<File> profilerOutputCa = new ArrayList<File>();
	private List<File> profilerOutputH2 = new ArrayList<File>();
	private List<File> profilerOutputSun = new ArrayList<File>();

	private String outputDir = "/home/max/uni/GreenDev/code/profilingOutput/";
	
	/**
	 * Read sub-directories of profiling output 
	 */
	public void readProfilerOutput() {
		File dir = new File(pathToProfiles+pathToCaProfile);
		if (dir.isDirectory()) {
			File[] dirsOfProfiles = dir.listFiles();
			for(File f : dirsOfProfiles) {
				if (f.isDirectory()) {
					profilerOutputCa.add(f);
				}
			}
		}
		dir = new File(pathToProfiles+pathToH2Profile);
		if (dir.isDirectory()) {
			File[] dirsOfProfiles = dir.listFiles();
			for(File f : dirsOfProfiles) {
				if (f.isDirectory()) {
					profilerOutputH2.add(f);
				}
			}
		}
		dir = new File(pathToProfiles+pathToSunProfile);
		if (dir.isDirectory()) {
			File[] dirsOfProfiles = dir.listFiles();
			for(File f : dirsOfProfiles) {
				if (f.isDirectory()) {
					profilerOutputSun.add(f);
				}
			}
		}
		System.out.println("Num Files: " + profilerOutputCa.size());
		System.out.println("Num Files: " + profilerOutputH2.size());
		System.out.println("Num Files: " + profilerOutputSun.size());
	}
	
	public List<File> getProfileSubdirsCa(){
		return profilerOutputCa;
	}
	
	public List<File> getProfileSubdirsH2(){
		return profilerOutputH2;
	}
	
	public List<File> getProfileSubdirsSun(){
		return profilerOutputSun;
	}
	
	public String getOutputDir() {
		return outputDir;
	}
}
