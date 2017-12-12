package parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import fileHandler.PerformanceFileHandler;
import model.PerformanceDataModel;

public class PerformanceDataParser {

	private static String performanceOutputFolder = 
			"/home/max/uni/GreenDev/code/subject"
			+ "Systems/h2database/h2/";
	private static String pathToProfile = 
			"/home/max/uni/GreenDev/code/subject"
			+ "Systems/h2database/h2/profile1.txt";
	private static String profileFileName = 
			"profile";
	private static String profileFileExtension =
			".txt";
	
	private static String outputDir =
			"/home/max/uni/GreenDev/code/profilingOutput/";
	
//	private Map<Integer, PerformanceDataModel> models = new HashMap<Integer, PerformanceDataModel>();
	
	
	private static boolean parsingFolder = false;
	
	
	public static void main (String[] args) {
		
		PerformanceFileHandler r = new PerformanceFileHandler();
		
//		r.printHere();
		
		File outputFolder = new File(outputDir);
		if (outputFolder.exists() && outputFolder.isDirectory()) {
			File[] listOfFiles = outputFolder.listFiles();
			
			testParser("test"+(listOfFiles.length+1)+".txt");
		}
		
	}
	
	private static void testParser(String oFileName) {
		System.out.println("Start parsing performance data.");
		
		
		if (parsingFolder) {
			File folder = new File(performanceOutputFolder);
			System.out.println("profile Folder exists: " + (folder.exists() && folder.isDirectory()));
			
			File[] listOfFiles = folder.listFiles();
			
			for (File file : listOfFiles) {
				if (file.isFile() && file.getName().contains(profileFileName) && 
						file.getName().contains(profileFileExtension)) {
					
					System.out.println(file.getName());
					PerformanceDataModel model = new PerformanceDataModel();
					model.parseFile(file.getAbsolutePath());
//					model.printPerformanceFractionPerFunction();
				}
			}
			
		} else {
		
			File file = new File(pathToProfile);
			System.out.println("profile exists: " + (file.exists() && file.isFile()));
			
			PerformanceDataModel model = new PerformanceDataModel();
			model.parseFile(pathToProfile);
			
			PrintWriter writer = null;
			try {
				writer = new PrintWriter(outputDir+oFileName, "UTF-8");
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
//			model.printData();
			if (writer != null) {
				model.printPerformanceFractionPerFunction(writer);
			}
			writer.close();
			
		}
		
		System.out.println("Finished parsing performance data.");
	}
}
