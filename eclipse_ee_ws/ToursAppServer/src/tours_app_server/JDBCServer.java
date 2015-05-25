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
		boolean successful = false;
		try {
			successful = cstmt.execute();
			System.out.println("Database update executed successfully...");
		}
		catch (SQLException se) {
			se.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			//finally block used to close resources
			try {
				if (cstmt != null)
					cstmt.close();
			}
			catch (SQLException se) {
				// TODO: nothing we can do?
			}
			try {
				if (conn != null)
					conn.close();
			}
		    catch (SQLException se) {
		    	se.printStackTrace();
		    	System.out.println("Error: SQL exception at conn.close() in exec query");
		    	System.exit(1);
		    } //end finally-try
		} //end finally
		
		return successful;
	} // end-method execDBQuery 
	
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
			rs = cstmt.executeQuery();
			System.out.println("Database query executed successfully...");
		}
		catch (SQLException se) {
			se.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			//finally block used to close resources
			try {
				if (cstmt != null)
					cstmt.close();
			}
			catch (SQLException se) {
				// TODO: nothing we can do?
			}
			try {
				if (conn != null)
					conn.close();
			}
		    catch (SQLException se) {
		    	se.printStackTrace();
		    	System.out.println("Error: SQL exception at conn.close() in exec query");
		    	System.exit(1);
		    } //end finally-try
		} //end finally
		
		return rs;
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
	public static boolean addUser(String uname, String password, String email, String phnum, boolean utype) {
		init(); // establish a connection to the database
		
		CallableStatement cstmt = null;
		String sql = "{call add_user (?, ?, ?, ?, ?::INT::BIT)}";
		System.out.println("Adding the user to the database...");
		
		try {
		   if (conn != null) {
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
		catch (SQLException se){
			System.out.println("Error: SQL exception setting parameters in addUser");
			System.exit(1);
		}
		
		return updateDB(cstmt);
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
		init(); // establish a connection to the database
		
		CallableStatement cstmt = null;
		String sql = "{call rm_user (?, ?)}";
		System.out.println("Removing the user from the database...");
		
		try {
		   if (conn != null)
			   cstmt = conn.prepareCall(sql);
		   else 
			   System.out.println("Error: connection in rmUser is null!");
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
		
		return updateDB(cstmt);
	}
}

	  