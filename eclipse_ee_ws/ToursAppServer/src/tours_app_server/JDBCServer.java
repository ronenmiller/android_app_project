package tours_app_server;

import java.sql.*;

import org.postgresql.util.PSQLException;

public final class JDBCServer {
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
	 * Execute a statement which updates the current state of the database.
	 * 
	 * @param cstmt the statement to execute
	 * @return 		<code>true</code> if the update completed successfully, <code>false</code> otherwise.
	 */
	private static boolean updateDB(CallableStatement cstmt) {
		boolean isSuccessful = false;
		try {
			isSuccessful = cstmt.execute();
			System.out.println("Database update executed successfully...");
		}
		catch (PSQLException pe) {
			pe.printStackTrace();
		}
		catch (SQLException se) {
			se.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return isSuccessful;
	} // end-method updateDB 
	
	/**
	 * Execute a query.
	 * 
	 * @param cstmt the statement to execute
	 * @return 		<code>ResultSet</code> containing the rows which match the query.
	 * 				If no match is found, returns <code>null</code>.
	 */
	private static ResultSet execDBQuery(CallableStatement cstmt) {
		ResultSet rs = null;
		try {
			if (cstmt.execute()) {
				rs = cstmt.executeQuery();
			}
			System.out.println("Database query executed successfully...");
		}
		catch (PSQLException pe) {
			pe.printStackTrace();
		}
		catch (SQLException se) {
			se.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return rs;
	} // end-method execDBQuery 
	
	public static boolean closeStatement(CallableStatement cstmt) {
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
	
	public static boolean closeConnection(Connection conn)
	{
		try {
			if (conn != null)
				conn.close();
			}
			catch (SQLException se) {
				se.printStackTrace();
				System.out.println("Error: closing connection failed!");
				System.exit(1);
			}
		
		return true;
	}
	   
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
	public static boolean addUser(String uname, String password, String email, String phnum, boolean utype) {
		boolean isSuccessful = false;
		CallableStatement cstmt = null;
		
		try {
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
			
			System.out.println("Adding the user to the database...");
			isSuccessful = updateDB(cstmt);
		}
		finally {
			// free the resources
			closeStatement(cstmt);
		}
		
		return isSuccessful;
		
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
		boolean isSuccessful = false;
		CallableStatement cstmt = null;
		
		try {
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
			catch(SQLException se){
				System.out.println("Error: SQL exception setting parameters in rmUser");
				System.exit(1);
			}
			
			System.out.println("Removing the user from the database...");
			isSuccessful = updateDB(cstmt);
		}
		finally {
			// free the resources
			closeStatement(cstmt);
		}
		
		return isSuccessful;
	}
	
	public static ResultSet getCityIdByName(String city, String region, String country) {
		ResultSet rs = null;
		CallableStatement cstmt = null;
		
		
		try {
			try {
			   if (conn != null) {
				   String sql = "{call find_cityid_by_name (?, ?, ?)}";
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
			catch(SQLException se) {
				System.out.println("Error: SQL exception setting parameters in getCityIdByName");
				System.exit(1);
			}
			
			System.out.println("Getting city ID from the database...");
			rs = execDBQuery(cstmt);
		}
		finally {
			// free the resources
			closeStatement(cstmt);
		}
		
		return rs;
		
		// example for using the resultSet
		/*try {	
			rs.next();
			System.out.println(rs.getString(1));
		}
		catch (SQLException se) {
			System.out.println("Error: SQL exception reading parameters in getCityIdByName");
			System.exit(1);
		}*/
	}
	
}

	  