package tours_app_server;

import java.sql.*;
import java.util.ArrayList;

import org.apache.commons.validator.routines.EmailValidator;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.postgresql.util.PSQLException;

import tours_app_client.AddUserQuery;
import tours_app_client.GeoQuery;
import tours_app_client.QueryContainer;

public final class JDBCServer {
	static EmailValidator emailValidator = EmailValidator.getInstance();
	/*****************************************************
	 * definitions to connect to postgres server
	 *****************************************************/
	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "org.postgresql.Driver";
	static final String HOST_NAME = "localhost"; // default host name
	static final String PORT_NUM = "5432"; // default postgreSQL port
	static final String DB_NAME = "project_db1";
			   
	// jdbc:postgresql://host:port/database
	static final String DB_URL = "jdbc:postgresql://" + HOST_NAME + ":"
			   						+ PORT_NUM + "/" + DB_NAME;
	
	//  Database credentials
	static final String USER = "postgres";
	static final String PASS = "abc";
	
	// the connection to the DB
	static Connection conn = null;
	static CallableStatement cstmt = null;
	
	/**
	 * TODO: move to parser class
	 */
	
	public static String fetchResponse(String jsonStream) {
		
		try {
			// fetch request type
			Gson gson = new Gson();
			QueryContainer queryContainer =  gson.fromJson(jsonStream, QueryContainer.class);
	        String reqType = queryContainer.getType();
	        
	        if (reqType.equals("addUser")) {
	        	AddUserQuery addUserQuery = gson.fromJson(queryContainer.getQuery(), AddUserQuery.class);
	        	String username = addUserQuery.getUname();
	        	String password = addUserQuery.getPass();
	        	String email = addUserQuery.getEmail();
	        	String phoneNumber = addUserQuery.getPhnum();
	        	boolean userType = addUserQuery.getUtype();
	        	return addUser(username, password, email, phoneNumber, userType);
	        }
	        
	        if (reqType.equals("getCityId"))
	        {
	        	GeoQuery geoQuery = gson.fromJson(queryContainer.getQuery(), GeoQuery.class);
	        	String city = geoQuery.getCity();
	        	String region = geoQuery.getRegion();
	        	String country = geoQuery.getCountry();
	        	return getCityIdByName(city, region, country);
	        	
	        }
			/*else if (q.reqType.equals("rmUser"))
				JDBCServer.rmUser(q.uname, q.password);
			else if (q.reqType.equals("find_cityid"))
				JDBCServer.getCityIdByName(q.city, q.region, q.country);
			else if (q.reqType.equals("validate_username"))
				JDBCServer.validateUniqueUsername(q.uname);*/
		} catch (Exception e) {
		  // crash and burn
			System.out.println("Error parsing JSON request string");
		}
		
		return "abc";
	}
	/**
	 * Constructor: Initialize connection and class variables.
	 */
	public static void init() {
		/******************************************************
		 * Establish connection to server and driver
		 ******************************************************/
		System.out.println("JDBCServer> init...");
		// Register JDBC driver
		try {
		   Class.forName(JDBC_DRIVER);
		}
		catch (ClassNotFoundException ex) {
		   System.out.println("Error: unable to load driver class!");
		   System.exit(1);
		}
		// Get JDBC Driver Manager connection
		try {
		   System.out.println("Connecting to database...");
		   conn = DriverManager.getConnection(DB_URL, USER, PASS);
		}
		catch (SQLException se) {
		   System.out.println("Error: unable to get driver connection!");
		   System.exit(1);
		}
	}
	
	/**
	 * Close the class's statement object in order to free up resources.
	 *   
	 * @return	<code>true</code> if the statement was closed successfully, <code>false</code> otherwise.
	 */
	public static boolean closeStatement() {
		try {
			if (cstmt != null)
				cstmt.close();
		}
		catch (SQLException se) {
			System.out.println("Error: closing statement failed!");
			return false;
		}
		
		return true;
	}
	
	/**
	 * Close the class's connection object in order to free up resources.
	 *   
	 * @return	<code>true</code> if the connection was closed successfully, <code>false</code> otherwise.
	 */
	public static boolean closeConnection()
	{
		try {
			if (conn != null) {
				conn.close();
			}
		}
		catch (SQLException se) {
			se.printStackTrace();
			System.out.println("Error: closing connection failed!");
			System.exit(1);
		}
		
		return true;
	}
	
	/**
	 * Execute a statement which modifies the current state of the database.
	 * 
	 * @param cstmt the statement to execute
	 * @return 		<code>true</code> if the modification completed successfully, <code>false</code> otherwise.
	 */
	private static boolean modifyDB(CallableStatement cstmt) {
		
		try {
			System.out.println("Updating database...");
			return cstmt.execute();
		}
		catch (PSQLException pe) {
			pe.printStackTrace();
			return false;
		}
		catch (SQLException se) {
			se.printStackTrace();
			return false;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
	} // end-method updateDB 
	
	/**
	 * Execute a query.
	 * 
	 * @param cstmt the statement to execute
	 * @return 		<code>ResultSet</code> containing the rows which match the query.
	 * 				If no match is found, returns an empty <code>ResultSet</code>. 
	 * 				If an error occurred, returns <code>null</code>.
	 */
	private static ResultSet queryDB(CallableStatement cstmt) {
		try {
			System.out.println("Running query...");
			return cstmt.executeQuery();
		}
		catch (PSQLException pe) {
			pe.printStackTrace();
			return null;
		}
		catch (SQLException se) {
			se.printStackTrace();
			return null;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	} // end-method execDBQuery 
	   
   /**
    * Add a new user to the database.
    * 
    * @param uname 		the user's name in the application
    * @param password	the user's chosen password
    * @param email		the user's email address
    * @param phnum		the user's phone number
    * @param utype		the user can specify whether he is also a guide
    * @return 			<code>true</code> if the operation succeeded, <code>false</code> otherwise.
    */
	public static String addUser(String uname, String password, String email, String phnum, boolean utype) {
		// if exists, clear previous statement
		try {
			if (cstmt != null) {
				cstmt.clearBatch(); // empties this statement object's current list of SQL commands
				cstmt.clearParameters(); // clears the current parameter values immediately.
			}
		}
		catch (SQLException se) {
			se.printStackTrace();
		}
		
		try {
		   if (conn != null) {
			   String sql = "{call add_user (?, ?, ?, ?, ?::INT::BIT)}";
			   cstmt = conn.prepareCall(sql);
		   }
		   else { 
			   System.out.println("Error: connection in addUser is null!");
		   }
		}
		catch (SQLException se) {
			//TODO: nothing we can do?
			System.out.println("Error: SQL exception at prepareCall in add user\n" + se);
			System.exit(1);
		}
		try {
			cstmt.setString(1, uname); // user name
			cstmt.setString(2, password); // password
			cstmt.setString(3, email); // email
			cstmt.setString(4, phnum); // phone number
			cstmt.setBoolean(5, utype); // user type
		}
		catch (SQLException se) {
			System.out.println("Error: SQL exception setting parameters in addUser");
			System.exit(1);
		}
		
		// modify database
		System.out.println("Adding the user to the database...");
		BooleanResponse booleanResponse = new BooleanResponse(modifyDB(cstmt));
		
		// pack the result into JSON format
		Gson gson = new Gson();
		ResponseContainer responseContainer = new ResponseContainer("addUser");
		responseContainer.setResponse(gson.toJson(booleanResponse));
		return gson.toJson(responseContainer); 
	}
	
   /**
    * remove an existing user from the database.
    * <p>
    * A user can remove himself from the application. Still, the personal
    * details supplied by the user remain in the database.
    * <p>
    * 
    * @param uname 		the user's name in the application
    * @param password	the user's chosen password
    * @return 			<code>true</code> if the operation succeeded, <code>false</code> otherwise.
    */
	public static boolean rmUser(String uname,String password) {
		// if exists, clear previous statement
		try {
			if (cstmt != null) {
				cstmt.clearBatch(); // empties this statement object's current list of SQL commands
				cstmt.clearParameters(); // clears the current parameter values immediately.
			}
		}
		catch (SQLException se) {
			se.printStackTrace();
		}
		
		try {
		   if (conn != null) {
			   String sql = "{call rm_user (?, ?)}";
			   cstmt = conn.prepareCall(sql);
		   }
		   else { 
			   System.out.println("Error: connection in rmUser is null!");
		   }
		}
		catch (SQLException se) {
			//TODO: nothing we can do?
			System.out.println("Error: SQL exception at prepareCall in rmUser\n" + se);
			System.exit(1);
		}
		try {
			cstmt.setString(1, uname); // user name
			cstmt.setString(2, password); // password
		}
		catch (SQLException se) {
			System.out.println("Error: SQL exception setting parameters in rmUser");
			System.exit(1);
		}
		
		System.out.println("Removing the user from the database...");
		return modifyDB(cstmt);
	}
	
	/**
	 * Finds a city's unique ID based on the names of the city, region/state and country.
	 *  
	 * @param city		 the name of the requested city
	 * @param region	 the name of the region/state where the city is located
	 * @param country	 the name of the country where the city is located
	 * @return ResultSet <code>ResultSet</code> containing a <code>String</code> with the city's ID.
	 * 					 If no match is found, the returned string is empty.
	 * 					 If an error occurred, returns <code>null</code>.
	 */
	// the returned city ID will help find queries related to the requested city faster.
	public static String getCityIdByName(String city, String region, String country) {
		// if exists, clear previous statement
		try {
			if (cstmt != null) {
				cstmt.clearBatch(); // empties this statement object's current list of SQL commands
				cstmt.clearParameters(); // clears the current parameter values immediately.
			}
		}
		catch (SQLException se) {
			se.printStackTrace();
		}
			
		try {
		   if (conn != null) {
			   String sql = "{call query_cityid_by_name (?, ?, ?)}";
			   cstmt = conn.prepareCall(sql);
		   }
		   else { 
			   System.out.println("Error: connection in getCityIdByName is null!");
		   }
		}
		catch (SQLException se) {
			//TODO: nothing we can do?
			System.out.println("Error: SQL exception at prepareCall in getCityIdByName\n" + se);
			System.exit(1);
		}
		try {
			cstmt.setString(1, city); // city name
			cstmt.setString(2, region); // region or state name
			cstmt.setString(3, country); // country name
		}
		catch (SQLException se) {
			System.out.println("Error: SQL exception setting parameters in getCityIdByName");
			System.exit(1);
		}
		
		// query database
		System.out.println("Getting city ID from the database...");
		ResultSet resultSet = queryDB(cstmt);
		String cityId;
		try {
			cityId =  ResultSetConverter.convertResultSetIntoString(resultSet);
		}
		catch (SQLException e)
		{
			cityId = null;
		}
		
		// TODO: handle exception when return value is null
		StringResponse stringResponse = new StringResponse(cityId);
		
		// pack the result into JSON format
		Gson gson = new Gson();
		ResponseContainer responseContainer = new ResponseContainer("getCityId");
		responseContainer.setResponse(gson.toJson(stringResponse));
		return gson.toJson(responseContainer);
	}
	
	/**
	 * Validates that the desired user name is not already taken.
	 * <p>
	 * Invoke as soon as the user is done typing his requested user name.
	 * <p>
	 *  	
	 * @param username   the desired user name
	 * @return ResultSet <code>ResultSet</code> containing a <code>boolean</code>:
	 * 					 <code>true</code> if the user name is not taken, <code>false</code> false otherwise.
	 * 					 If an error occurred, returns <code>null</code>.
	 */
	public static ResultSet validateUniqueUsername(String username) {
		// if exists, clear previous statement
		try {
			if (cstmt != null) {
				cstmt.clearBatch(); // empties this statement object's current list of SQL commands
				cstmt.clearParameters(); // clears the current parameter values immediately.
			}
		}
		catch (SQLException se) {
			se.printStackTrace();
		}
		
		try {
			if (conn != null) {
				   String sql = "{call validate_unique_username (?)}";
				   cstmt = conn.prepareCall(sql);
			   }
			   else { 
				   System.out.println("Error: connection in validateUsername is null!");
			   }
		}
		catch (SQLException se) {
			//TODO: nothing we can do?
			System.out.println("Error: SQL exception at prepareCall in validateUsername\n" + se);
			System.exit(1);
		}
		try {
			cstmt.setString(1, username); // user name
		}
		catch (SQLException se) {
			System.out.println("Error: SQL exception setting parameters in validateUsername");
			System.exit(1);
		}
			
		System.out.println("Validating unique username using the database...");
		return queryDB(cstmt);
	}
	
}

	  