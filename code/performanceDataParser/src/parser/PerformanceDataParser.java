package parser;

public class PerformanceDataParser {

	private static String path = "/home/max/uni/GreenDev/code/subjectSystems/h2database/h2/profile.txt";
	
	public static void main (String[] args) {
		System.out.println("Start parsing performance data.");
		
		PerformanceDataModel model = new PerformanceDataModel();
		model.parseFile(path);
		
		model.printData();
		
		System.out.println("Finished parsing performance data.");
	}
}
