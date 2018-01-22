package profiling;

import main.java.Catena;
import main.java.DefaultInstances;
import main.java.Helper;
import main.java.components.gamma.GammaInterface;
import main.java.components.gamma.algorithms.IdentityGamma;
import main.java.components.gamma.algorithms.SaltMix;
import main.java.components.graph.GraphInterface;
import main.java.components.graph.algorithms.DoubleButterflyGraph;
import main.java.components.graph.algorithms.GenericGraph;
import main.java.components.graph.algorithms.IdxInterface;
import main.java.components.graph.algorithms.index.IndexBRG;
import main.java.components.graph.algorithms.index.IndexDBG;
import main.java.components.graph.algorithms.index.IndexGRG;
import main.java.components.hash.HashInterface;
import main.java.components.hash.algorithms.Blake2b;
import main.java.components.hash.algorithms.Blake2b_1;
import main.java.components.phi.PhiInterface;
import main.java.components.phi.algorithms.CatenaPhi;
import main.java.components.phi.algorithms.IdentityPhi;
import main.java.components.phi.algorithms.IdxStateInterface;
import main.java.components.phi.algorithms.index.LSBIndex;

public class CatenaProfiler {

	Helper helper = new Helper();
	
	public static void main(String[] args) {
		
		CatenaProfiler cp = new CatenaProfiler();
		
		if (args.length != (7+5)) {
			System.out.println("Need to initialize Catena with 7 parameters and run it with 5. You transfered " + args.length);
		}
		// TODO parameter exception
		boolean useFullHash = Boolean.parseBoolean(args[0]);
		boolean useGamma 	= Boolean.parseBoolean(args[1]);
		int useGraph 		= Integer.parseInt(args[2]);
		boolean usePhi 		= Boolean.parseBoolean(args[3]);
		int gInp 			= Integer.parseInt(args[4]);
		int lambdaInp		= Integer.parseInt(args[5]);
		String vIDInp		= args[6];
		
		String pwd 			= args[7];
		String salt 		= args[8];
		String gamma 		= args[9];
		String aData 		= args[10];
		int outputLength 	= Integer.parseInt(args[11]);
		
		long startTime = System.currentTimeMillis();
		
		Catena c = cp.initCatenaByConfig(useFullHash, useGamma, 
				useGraph, usePhi, gInp, lambdaInp, vIDInp);
		cp.testPerformanceByConfig(c, pwd, salt, gamma, aData, outputLength);
		
		System.out.println("Time used: " + (System.currentTimeMillis()-
				startTime));
	}
	
	private Catena initCatenaByConfig(boolean useFullHash, 
			boolean useGamma, int useGraph, boolean usePhi, int gInp, 
			int lambdaInp, String vIDInp) {
		Catena c = new Catena();
		
		HashInterface h = new Blake2b();
		
		HashInterface hPrime;
		if(useFullHash) {
			hPrime = new Blake2b();
		} else {
			hPrime = new Blake2b_1();
		}
		
		GammaInterface gamma;
		if (useGamma) {
			gamma = new SaltMix();
		} else {
			gamma = new IdentityGamma();
		}
		
		GraphInterface f;
		IdxInterface idx;
		
		// 1-DBG, 2-BRG, 3-GRG, 4-SBRG
		if (useGraph == 1) {
			f = new DoubleButterflyGraph();
			idx = new IndexDBG();
		} else if (useGraph == 2) {
			f = new GenericGraph();
			idx = new IndexBRG();
		} else if (useGraph == 3) {
			f = new GenericGraph();
			int l = 3;
			idx = new IndexGRG(l);
		} else if (useGraph == 4) {
			f = new GenericGraph();
			int l = 0;
			idx = new IndexGRG(l);
		} else {
			System.out.println("There are 4 different Graphs in Catena. "
					+ "You choose none of them. So BRG is used by default.");
			f = new GenericGraph();
			idx = new IndexBRG();
		}
		
		IdxStateInterface idxState;
		PhiInterface phi;
		if (usePhi) {
			idxState = new LSBIndex();
			phi = new CatenaPhi(idxState);
		} else {
			phi = new IdentityPhi();
		}
		
		int gLow = gInp;
		int gHigh = gInp;
		
		int lambda = lambdaInp;
		
		String vID = vIDInp;
		
		c.init(h, hPrime, gamma, f, idx, phi, gLow, gHigh, lambda, vID);
		
		return c;
	}
	
	private void testPerformanceByConfig(Catena instance, String pwdStr, 
			String saltStr, String gammaStr, String aDataStr, int outLen) {
		byte[] pwd = helper.hex2bytes(pwdStr);
		byte[] salt = helper.hex2bytes(saltStr);
		byte[] gamma = helper.hex2bytes(gammaStr);
		byte[] aData = helper.hex2bytes(aDataStr);
		int outputLength = outLen;
		
		// Here maybe catch output
		instance.catena(pwd, salt, aData, gamma, outputLength);
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
