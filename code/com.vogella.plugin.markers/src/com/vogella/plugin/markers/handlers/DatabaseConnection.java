package com.vogella.plugin.markers.handlers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

	private String dbPath = "./../profilingOutput/";
	private String dbName = "performance.db";
	
	private Connection c = null;
	private Statement stmt = null;
	
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
			stmt = c.createStatement();
		} catch (SQLException e) {
			System.out.println("SQL Exception while initialize batch statement");
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		}
	}
	
	public void executeBatch() {
		try {
			stmt.executeBatch();
			stmt.close();
			c.commit();
			
		} catch (SQLException e) {
			System.out.println("SQL Exception while executing insert batch");
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		}
	}
	
	
}
