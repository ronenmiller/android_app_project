package serverDB;

import java.sql.*;
import org.postgresql.util.PSQLException;


public class JDBCExample 
{
   // JDBC driver name and database URL
   static final String JDBC_DRIVER = "org.postgresql.Driver";
   static final String HOST_NAME = "localhost"; // default host name
   static final String PORT_NUM = "5432"; // default postgreSQL port
   static final String DB_NAME = "myDB";
		   
		   // jdbc:postgresql://host:port/database
   static final String DB_URL = "jdbc:postgresql://" + HOST_NAME + ":"
		   						+ PORT_NUM + "/" + DB_NAME;

   //  Database credentials
   static final String USER = "postgres";
   static final String PASS = "****";
   
   public static void main(String[] args) 
   {
	   Connection conn = null;
	   CallableStatement cstmt = null;
	   
	   try {
		   //STEP 2: Register JDBC driver
		   Class.forName(JDBC_DRIVER);
	   }
		catch(ClassNotFoundException ex) {
		   System.out.println("Error: unable to load driver class!");
		   System.exit(1);
	   }
	   try
	   {
	      //STEP 3: Open a connection
	      System.out.println("Connecting to database...");
	      conn = DriverManager.getConnection(DB_URL, USER, PASS);

	      //STEP 4: Execute a query
	      System.out.println("Creating statment...");
	      String sql = "{call add_user (?, ?, ?, ?, ?::INT::BIT)}";
	      cstmt = conn.prepareCall(sql);
	      
	      cstmt.setString(1, "ronen"); // user name
	      cstmt.setString(2, "999mmfkd"); // user's password
	      cstmt.setString(3, "a@gmail.com"); // email
	      cstmt.setString(4, "05454948484"); // password
	      cstmt.setBoolean(5, true);
	      
	      cstmt.executeUpdate();
	      System.out.println("Statement performed successfully...");
	   }
	   catch(PSQLException se)
	   {
		   //Handle errors for JDBC
		   se.printStackTrace();
	   }
	   catch(Exception e)
	   {
	      //Handle errors for Class.forName
	      e.printStackTrace();
	   }
	   finally
	   {
	      //finally block used to close resources
	      try
	      {
	         if(cstmt != null)
	            cstmt.close();
	      }
	      catch(SQLException se2)
	      {
	    	// nothing we can do
	      }
	      try
	      {
	         if (conn != null)
	            conn.close();
	      }
	      catch(SQLException se)
	      {
	         se.printStackTrace();
	      } //end finally try
	   } //end try
	   System.out.println("Goodbye!");
	} //end main
} //end JDBCExample