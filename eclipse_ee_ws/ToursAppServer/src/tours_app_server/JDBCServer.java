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
	 * Constructor: Initialize connection and class vars
	 */
	public static void init(){
		/******************************************************
		 * Establish connection to server and driver
		 ******************************************************/
		System.out.println("JDBCServer> init...");
		// Register JDBC driver
		try {
		   Class.forName(JDBC_DRIVER);
		}
		catch(ClassNotFoundException ex) {
		   System.out.println("Error: unable to load driver class!");
		   System.exit(1);
		}
		// Get JDBC Driver Manager connection
		try {
		   System.out.println("Connecting to database...");
		   conn = DriverManager.getConnection(DB_URL, USER, PASS);
		}
		catch (SQLException e){
		   System.out.println("Error: unable to get driver connection!");
		   System.exit(1);
		}
	}
	
	/**
	 * Execute the query
	 */
	private static void execDBQuery(CallableStatement cstmt){
		   try{
			  
			   cstmt.executeUpdate();
			   System.out.println("Statement executed successfully...");
		   }
		   catch(PSQLException se){
			   se.printStackTrace();
		   }
		   catch(Exception e){
			   e.printStackTrace();
		   }
		   finally{
			   //finally block used to close resources
			   try{
				   if(cstmt != null)
					   cstmt.close();
			   }
			   catch(SQLException se2){
				// TODO: nothing we can do?
			   }
			   /*
			   try{
				   if (conn != null)
					   conn.close();
			   }
		      catch(SQLException se){
		    	  se.printStackTrace();
		    	  System.out.println("Error: SQL exception at conn.close() in exec query");
		    	  System.exit(1);
		      } //end finally try
		      */
		   } //end try
	}// end-method execDBQuery 
	   
   /**
    * Add user to database with given parameters
    */
	public static void addUser(String uname,String password,String email,String phnum,boolean utype){
		// setup request and exec
		
		CallableStatement cstmt = null;
		
		String sql = "{call add_user (?, ?, ?, ?, ?::INT::BIT)}";
		
		System.out.println("Creating add user statment...");
		try{
		   if(conn != null)
			   cstmt = conn.prepareCall(sql);
		   else 
			   System.out.println("Error: in add user conn is null!");
		}
		catch(SQLException e1){
			//TODO: nothing we can do?
			System.out.println("Error: SQL exception at prepareCall in add user\n"+e1);
			System.exit(1);
		}
		try{
			cstmt.setString(1, uname); // user name
			cstmt.setString(2, password); // password
			cstmt.setString(3, email); // email
			cstmt.setString(4, phnum); // phone number
			cstmt.setBoolean(5, utype); // user type
		}
		catch(SQLException e2){
			System.out.println("Error: SQL exception at cstmt - set params in add user");
			System.exit(1);
		}
		execDBQuery(cstmt);
	}
	
	   /**
	    * Add user to database with given parameters
	    */
		public static void rmUser(String uname,String password){
		// setup request and exec
		CallableStatement cstmt = null;
		
		String sql = "{call rm_user (?, ?)}";
		
		System.out.println("Creating remove user statment...");
		try{
		   if(conn != null)
			   cstmt = conn.prepareCall(sql);
		   else 
			   System.out.println("Error: in rm user conn is null!");
		}
		catch(SQLException e1){
			//TODO: nothing we can do?
			System.out.println("Error: SQL exception at prepareCall in rm user\n"+e1);
			System.exit(1);
		}
		try{
			cstmt.setString(1, uname); // user name
			cstmt.setString(2, password); // password
		}
		catch(SQLException e2){
			System.out.println("Error: SQL exception at cstmt - set params in rm user");
			System.exit(1);
		}
		execDBQuery(cstmt);
	}
}

	  