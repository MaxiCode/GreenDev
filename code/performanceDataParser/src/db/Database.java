package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class Database {
	
	private String dbPath;
	private String dbName = "performance.db";
	
	private Connection c = null;
	private Statement insertStmt = null;
	
	private Map<String, Integer> primaryKeyOfFunctions = new HashMap<String, Integer>();
	private Map<String, Integer> primaryKeyOfConfigs = new HashMap<String, Integer>();

	public Database(String path) {
		this.dbPath = path;
		createDatabaseConnection();
		initTables();
	}
	
	/**
	 * Opens existing database or creates one
	 */
	private void createDatabaseConnection() {
		
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:" + dbPath + dbName);
			c.setAutoCommit(false);
			
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
		Statement stmt = null;
		
		try {
			stmt = c.createStatement();
		
			String sqlTableFunctionName = "CREATE TABLE IF NOT EXISTS FUNCTION_NAME "
					+ "(ID INTEGER PRIMARY KEY NOT NULL,"
					+ " NAME TEXT NOT NULL)";
			
			stmt.addBatch(sqlTableFunctionName);
			
			String sqlTableConfig = "CREATE TABLE IF NOT EXISTS CONFIG "
			+ "(ID INTEGER PRIMARY KEY NOT NULL,"
			+ " NAME TEXT NOT NULL)";
			
			stmt.addBatch(sqlTableConfig);
			
			String sqlTablePerformance = "CREATE TABLE IF NOT EXISTS PERFORMANCE "
			+ "(ID INTEGER PRIMARY KEY NOT NULL,"
			+ " TIME_VALUE 		REAL 	NOT NULL,"
			+ " FRACTION_VALUE 	REAL 	NOT NULL,"
			+ " C_ID 			INTEGER NOT NULL,"
			+ " F_ID 			INTEGER NOT NULL)";
			
			stmt.addBatch(sqlTablePerformance);
			
			
			String sqlDeleteContentFunction = "delete from FUNCTION_NAME";
			stmt.addBatch(sqlDeleteContentFunction);
			String sqlDeleteContentConfig = "delete from CONFIG";
			stmt.addBatch(sqlDeleteContentConfig);
			String sqlDeleteContentPerformance = "delete from PERFORMANCE";
			stmt.addBatch(sqlDeleteContentPerformance);
			
			stmt.executeBatch();
			stmt.close();
			c.commit();
		} catch (SQLException e) {
			System.out.println("SQL Exception while executing init statement");
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		}
	}

	/**
	 * Closes existing database connection if exist
	 */
	public void closeDbConnection() {
		if (c==null)return;
		
		try {
			c.close();
		} catch (SQLException e) {
			System.out.println("SQL Exception while closing database");
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		}
	}
	
	public void initBatch() {
		try {
			insertStmt = c.createStatement();
		} catch (SQLException e) {
			System.out.println("SQL Exception while initialize batch statement");
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		}
	}
	
	
	public void insertData(String fName, String cName, float time, float fraction) {
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
				
				insertStmt.addBatch("INSERT INTO CONFIG (NAME) VALUES ('"+cName+"')");

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
	
	public void executeBatch() {
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
