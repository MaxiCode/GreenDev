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
	private String dbName = "performance.db";
	
	private Connection c = null;
	private Statement stm = null;
	
	public DatabaseConnection() {
		initConnection();
	}
	
	public DatabaseConnection(String file) {
		this.dbName = file;
		initConnection();
	}
	
	public DatabaseConnection(String file, String path) {
		this.dbPath = path;
		this.dbName = file;
		initConnection();
	}
	
	private void initConnection() {
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
	
	public List<Float> getFraction(int fID){
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
				tmpTime = rs.getFloat("FRACTION_VALUE");
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
}
