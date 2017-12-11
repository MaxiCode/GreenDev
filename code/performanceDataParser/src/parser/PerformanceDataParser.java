package parser;

public class PerformanceDataParser {

	private static String performanceOutputFolder = 
			"/home/max/uni/GreenDev/code/subject"
			+ "Systems/h2database/h2/";
	private static String path = 
			"/home/max/uni/GreenDev/code/subject"
			+ "Systems/h2database/h2/profile.txt";
	private static String profileFile = 
			"profile.txt";
	
	public static void main (String[] args) {
		System.out.println("Start parsing performance data.");
//		
//		if 
		
		PerformanceDataModel model = new PerformanceDataModel();
		model.parseFile(path);
		
//		model.printData();
		model.printPerformanceFractionPerFunction();
		
		System.out.println("Finished parsing performance data.");
	}
	
	private static void runAllProfiles (String path, String file) {
		
	}
	
	private static void runOneProfile (String file) {
		
	}
}
