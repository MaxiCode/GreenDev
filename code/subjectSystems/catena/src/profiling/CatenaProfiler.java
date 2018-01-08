package profiling;

import main.java.Catena;
import main.java.DefaultInstances;
import main.java.Helper;

public class CatenaProfiler {

	Helper helper = new Helper();
	
	public static void main(String[] args) {
		CatenaProfiler cp = new CatenaProfiler();
		
		long startTime = System.currentTimeMillis();
		
//		cp.testPerformanceButterfly();
		cp.testPerformanceButterflyFull();
//		cp.testPerformanceDragonfly();
		
		// Exception in thread "main" java.lang.OutOfMemoryError: GC overhead limit exceeded
//		cp.testPerformanceDragonflyFull();
		
		System.out.println("Time used: " + (System.currentTimeMillis()-startTime));
	}
	
	public void testPerformanceButterfly() {
		DefaultInstances instances = new DefaultInstances();
		Catena c = instances.initButterfly();
		
		byte[] pwd = helper.hex2bytes("012345");
		byte[] salt = helper.hex2bytes("6789ab");
		byte[] gamma = helper.hex2bytes("6789ab");
		byte[] aData = helper.hex2bytes("000000");
		int outputLength = 64;
		
		String actualResult;
		String expectedResult = "c061a9ebb7e0a0c7ec90"
				+ "e3114f3b6b7fa8fdfce2584ca76576411d"
				+ "8ce290c348c953ee8a985450124f2f10f9"
				+ "c415787dc76c0dc6aa95a758e516072eb1"
				+ "6b9816";
		
		actualResult = helper.bytes2hex(c.catena(pwd, salt, aData, gamma, outputLength));
		System.out.println("Butterfly Done: " + actualResult.equalsIgnoreCase(expectedResult));
	}
	
	public void testPerformanceButterflyFull() {
		DefaultInstances instances = new DefaultInstances();
		Catena c = instances.initButterflyFull();
		
		byte[] pwd = helper.hex2bytes("012345");
		byte[] salt = helper.hex2bytes("6789ab");
		byte[] gamma = helper.hex2bytes("6789ab");
		byte[] aData = helper.hex2bytes("000000");
		int outputLength = 64;
		
		String actualResult;
		String expectedResult = "9e4aa09e9db103add705"
				+ "c044b5d98ae75c6a777f4ba281aef8d25e"
				+ "d23bfbe44dfae8d95c2925569e27b2271b"
				+ "e37c0a34386f6556b795cd03d075a92974"
				+ "cc94c3";
		
		actualResult = helper.bytes2hex(c.catena(pwd, salt, aData, gamma, outputLength));
		System.out.println("Butterfly-Full Done: " + actualResult.equalsIgnoreCase(expectedResult));
	}
	
	public void testPerformanceDragonfly(){
		DefaultInstances defaultInstance = new DefaultInstances();
		Catena c = defaultInstance.initDragonfly();
		
		byte[] pwd = helper.hex2bytes("012345");
		byte[] salt = helper.hex2bytes("6789ab");
		byte[] gamma = helper.hex2bytes("6789ab");
		byte[] aData = helper.hex2bytes("000000");
		int outputLength = 64;
		
		String expectedResult = "76f4d59cd232304524ca"
				+ "7c98c68481a9cf1416e1505ee610fc60e4"
				+ "3fd102be0bdc0a443b036bec0830702bd8"
				+ "496c7805aa4eecea23adb9bd4579939e9e"
				+ "6d384d";
		
		String actualResult = helper.bytes2hex(c.catena(pwd, salt, aData, gamma, outputLength));
		System.out.println("Dragonfly Done: " + actualResult.equalsIgnoreCase(expectedResult));
	}
	
	public void testPerformanceDragonflyFull(){
		DefaultInstances defaultInstance = new DefaultInstances();
		Catena c = defaultInstance.initDragonflyFull();
		
		byte[] pwd = helper.hex2bytes("012345");
		byte[] salt = helper.hex2bytes("6789ab");
		byte[] gamma = helper.hex2bytes("6789ab");
		byte[] aData = helper.hex2bytes("000000");
		int outputLength = 64;
		
		String expectedResult = "213f58a9ccfcf5770d13bc9914"
				+ "8ff8e2167afb538f27a3a21911954a548337609e"
				+ "aaad9a4096ec2d94a0084271be0e73904141b04e"
				+ "dcb9d21f26f3344a8553c4";
		
		String actualResult = helper.bytes2hex(c.catena(pwd, salt, aData, gamma, outputLength));
		System.out.println("Dragonfly-Full Done: " + actualResult.equalsIgnoreCase(expectedResult));
	}
	
}
