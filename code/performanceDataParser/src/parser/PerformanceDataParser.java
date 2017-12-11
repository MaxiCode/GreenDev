package parser;

import java.io.File;

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
	
//	private Map<Integer, PerformanceDataModel> models = new HashMap<Integer, PerformanceDataModel>();
	
	
	private static boolean parsingFolder = false;
	
	
	public static void main (String[] args) {
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
					model.printPerformanceFractionPerFunction();
				}
			}
			
		} else {
		
			File file = new File(pathToProfile);
			System.out.println("profile exists: " + (file.exists() && file.isFile()));
			
			PerformanceDataModel model = new PerformanceDataModel();
			model.parseFile(pathToProfile);
			
//			model.printData();
			model.printPerformanceFractionPerFunction();
		}
		
		System.out.println("Finished parsing performance data.");
	}
}
