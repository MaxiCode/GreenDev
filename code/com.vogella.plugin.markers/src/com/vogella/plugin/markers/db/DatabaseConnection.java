package com.vogella.plugin.markers.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DatabaseConnection {

	private String dbPath = "/home/max/uni/GreenDev/code/profilingOutput/";
	private String dbName = "";
	private String dbNameCa = "performanceCatena.db";
	private String dbNameH2 = "performanceh2.db";
	private String dbNameSun = "performanceSunflow.db";
	
	private Connection c = null;
	private Statement stm = null;
	
	public DatabaseConnection() {
		// default Catena
		dbName = dbNameCa;
		initConnection();
	}
	
	public DatabaseConnection(String file) {
		if (file.equals("catena")) {
			System.out.println("Adding catena connection");
			dbName = dbNameCa;
		} else if(file.equals("h2")) {
			System.out.println("Adding h2 connection");
			dbName = dbNameH2;
		} else {
			System.out.println("Adding sunflow connection");
			dbName = dbNameSun;
		}
//		this.dbName = file;
		initConnection();
	}
	
	private void initConnection() {
		try {
			Class.forName("org.sqlite.JDBC");
			System.out.println("Connection to db: " + dbPath + dbName);
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
	
	public void initBatchStmt() {
		try {
			stm = c.createStatement();
		} catch (SQLException e) {
			System.out.println("SQL Exception while initialize batch statement");
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		}
	}
	
	public void executeBatch() {
		try {
			stm.executeBatch();
			stm.close();
			c.commit();
			
		} catch (SQLException e) {
			System.out.println("SQL Exception while executing insert batch");
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		}
	}

	public HashMap<String, Integer> getFunctionNames() {
		HashMap<String, Integer> functionNames = new HashMap<String, Integer>();
		try {
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM FUNCTION_NAME");
			while(rs.next()) {
				functionNames.put(rs.getString("NAME"), rs.getInt("ID"));
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return functionNames;
	}
	
	public HashMap<String, Integer> getConfigs(){
		HashMap<String, Integer> configs = new HashMap<String, Integer>();
		try {
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM CONFIG");
			while(rs.next()) {
				configs.put(rs.getString("NAME"), rs.getInt("ID"));
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return configs;
	}

	/**
	 * TODO: Rework greedy parameter filter
	 * 
	 * @param fID
	 * @return
	 */
	public List<Float> getTimeValues(int fID){
		List<Float> l = new ArrayList<>();
		String sql = "SELECT * FROM PERFORMANCE WHERE F_ID = ?";
		List<Integer> configs = new ArrayList<>();
		try {
			PreparedStatement pStmt = c.prepareStatement(sql);
			// set the value
			pStmt.setInt(1, fID);
//			pStmt.setInt(2, cID);
			ResultSet rs = pStmt.executeQuery();
			
			float tmpTime;
			int tmpCID; 
			while(rs.next()) {
				tmpTime = rs.getFloat("TIME_VALUE");
				tmpCID = rs.getInt("C_ID");
				if (!configs.contains(tmpCID)) {
					configs.add(tmpCID);
					l.add(tmpTime);
//					System.out.println("\t Values: " + tmpTime);
//					System.out.println("\t Config: " + tmpCID);
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return l;
	}
	
	public List<Float> getFraction(int fID, int cID){
		List<Float> l = new ArrayList<>();
		String sql = "SELECT * FROM PERFORMANCE WHERE F_ID = ? AND C_ID = ?";
		try {
			PreparedStatement pStmt = c.prepareStatement(sql);
			// set the value
			pStmt.setInt(1, fID);
			pStmt.setInt(2, cID);
			ResultSet rs = pStmt.executeQuery();
			
			float tmpTime;
			while(rs.next()) {
				
				tmpTime = rs.getFloat("FRACTION_VALUE");
				l.add(tmpTime);
				
//				System.out.println("\t Values: " + tmpTime);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return l;
	}
	
	public List<Float> getFraction(int fID){
		List<Float> l = new ArrayList<>();
		String sql = "SELECT * FROM PERFORMANCE WHERE F_ID = ?";
		try {
			PreparedStatement pStmt = c.prepareStatement(sql);
			// set the value
			pStmt.setInt(1, fID);
			ResultSet rs = pStmt.executeQuery();
			
			float tmpTime;
			while(rs.next()) {
				
				tmpTime = rs.getFloat("FRACTION_VALUE");
				l.add(tmpTime);
				
//				System.out.println("\t Values: " + tmpTime);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return l;
	}
}
