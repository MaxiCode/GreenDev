package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class Database {
	
	private String dbPath;
	private String dbNameCa = "performanceCatena.db";
	private String dbNameh2 = "performanceh2.db";
	private String dbNameSunflow = "performanceSunflow.db";
	
	private Connection cCa = null;
	private Connection cH2 = null;
	private Connection cSun = null;
	
	private Statement insertStmt = null;
	
	private Map<String, Integer> primaryKeyOfFunctions = new HashMap<String, Integer>();
	private Map<String, Integer> primaryKeyOfConfigs = new HashMap<String, Integer>();

	public Database(String path) {
		this.dbPath = path;
		createDatabaseConnections();
		initTables();
	}
	
	/**
	 * Opens existing database or creates one
	 */
	private void createDatabaseConnections() {
		
		try {
			Class.forName("org.sqlite.JDBC");
			cCa = DriverManager.getConnection("jdbc:sqlite:" + dbPath + dbNameCa);
			cCa.setAutoCommit(false);
			cH2 = DriverManager.getConnection("jdbc:sqlite:" + dbPath + dbNameh2);
			cH2.setAutoCommit(false);
			cSun = DriverManager.getConnection("jdbc:sqlite:" + dbPath + dbNameSunflow);
			cSun.setAutoCommit(false);
		} catch (SQLException e) {
			System.out.println("SQL Exception while creating database");
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		} catch (ClassNotFoundException e) {
			System.out.println("ClassNotFound Exception while creating database");
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		}
	}
	
	/**
	 * Creates 3 Tables (FUNCTION_NAME, CONFIG, PERFORMANCE)
	 */
	private void initTables() {
		Statement stmtCa = null;
		Statement stmtH2 = null;
		Statement stmtSun = null;
		
		try {
			stmtCa = cCa.createStatement();
			stmtH2 = cH2.createStatement();
			stmtSun = cSun.createStatement();
		
			String sqlTableFunctionName = "CREATE TABLE IF NOT EXISTS FUNCTION_NAME "
					+ "(ID INTEGER PRIMARY KEY NOT NULL,"
					+ " NAME TEXT NOT NULL)";
			
			stmtCa.addBatch(sqlTableFunctionName);
			stmtH2.addBatch(sqlTableFunctionName);
			stmtSun.addBatch(sqlTableFunctionName);
			
			String sqlTableConfig = "CREATE TABLE IF NOT EXISTS CONFIG "
					+ "(ID INTEGER PRIMARY KEY NOT NULL,"
					+ " NAME TEXT NOT NULL,"
					+ " DATE TEXT NOT NULL,"
					+ " PARAMETER TEXT NOT NULL)";
			
			stmtCa.addBatch(sqlTableConfig);
			stmtH2.addBatch(sqlTableConfig);
			stmtSun.addBatch(sqlTableConfig);
			
			String sqlTablePerformance = "CREATE TABLE IF NOT EXISTS PERFORMANCE "
					+ "(ID INTEGER PRIMARY KEY NOT NULL,"
					+ " TIME_VALUE 		REAL 	NOT NULL,"
					+ " FRACTION_VALUE 	REAL 	NOT NULL,"
					+ " C_ID 			INTEGER NOT NULL,"
					+ " F_ID 			INTEGER NOT NULL)";
			
			stmtCa.addBatch(sqlTablePerformance);
			stmtH2.addBatch(sqlTablePerformance);
			stmtSun.addBatch(sqlTablePerformance);
			
			String sqlDeleteContentFunction = "delete from FUNCTION_NAME";
			stmtCa.addBatch(sqlDeleteContentFunction);
			stmtH2.addBatch(sqlDeleteContentFunction);
			stmtSun.addBatch(sqlDeleteContentFunction);
			String sqlDeleteContentConfig = "delete from CONFIG";
			stmtCa.addBatch(sqlDeleteContentConfig);
			stmtH2.addBatch(sqlDeleteContentConfig);
			stmtSun.addBatch(sqlDeleteContentConfig);
			String sqlDeleteContentPerformance = "delete from PERFORMANCE";
			stmtCa.addBatch(sqlDeleteContentPerformance);
			stmtH2.addBatch(sqlDeleteContentPerformance);
			stmtSun.addBatch(sqlDeleteContentPerformance);
			
			stmtCa.executeBatch();
			stmtH2.executeBatch();
			stmtSun.executeBatch();
			stmtCa.close();
			stmtH2.close();
			stmtSun.close();
			cCa.commit();
			cH2.commit();
			cSun.commit();
		} catch (SQLException e) {
			System.out.println("SQL Exception while executing init statement");
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		}
	}

	public Connection getConnectionCa() {
		return cCa;
	}
	
	public Connection getConnectionH2() {
		return cH2;
	}
	
	public Connection getConnectionSun() {
		return cSun;
	}
	
	
	
	/**
	 * Closes existing database connection if exist
	 */
	public void closeDbConnection(Connection c) {
		if (c==null)return;
		try {
			c.close();
		} catch (SQLException e) {
			System.out.println("SQL Exception while closing database");
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		}
	}
	
	public void initBatch(Connection c) {
		try {
			insertStmt = c.createStatement();
		} catch (SQLException e) {
			System.out.println("SQL Exception while initialize batch statement");
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		}
	}
	
	public void insertData(String fName, String cName, String cDate, String cParameter, float time, float fraction) {
		if (!primaryKeyOfFunctions.containsKey(fName)) {
			try {
				insertStmt.addBatch("INSERT INTO FUNCTION_NAME (NAME) "
						+ "VALUES ('"+fName+"');");

				primaryKeyOfFunctions.put(fName, primaryKeyOfFunctions.size()+1);
			} catch (SQLException e) {
				System.out.println("SQL Exception while writing function names");
				System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			}
		}
		
		if (!primaryKeyOfConfigs.containsKey(cName)) {
			try {
				
				insertStmt.addBatch("INSERT INTO CONFIG "
						+ "(NAME, DATE, PARAMETER) VALUES "
						+ "('"+cName+"', '"+cDate+"', '"+cParameter+"')");

				primaryKeyOfConfigs.put(cName, primaryKeyOfConfigs.size()+1);
			} catch (SQLException e) {
				System.out.println("SQL Exception while writing config data");
				System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			}
		}
		
		// insert data in PERFORMANCE with IDs of function and config
		try {
			int cID = primaryKeyOfConfigs.get(cName);
			int fID = primaryKeyOfFunctions.get(fName);
			
			insertStmt.addBatch("INSERT INTO PERFORMANCE "
					+ "(TIME_VALUE, FRACTION_VALUE, C_ID, F_ID) VALUES "
					+ "("+time+", "+fraction+", "+cID+", "+fID+")");
			
		} catch (SQLException e) {
			System.out.println("SQL Exception while writing performance data");
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		}
	}
	
	public void executeBatch(Connection c) {
		try {
			insertStmt.executeBatch();
			insertStmt.close();
			c.commit();
			
		} catch (SQLException e) {
			System.out.println("SQL Exception while executing insert batch");
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		}
	}
	
}
