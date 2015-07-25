package tours_app_server;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.validator.routines.EmailValidator;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
	
	// Database credentials
	static final String USER = "postgres";
	static final String PASS = "abc";
	
	public static String fetchResponse(String jsonStream) {
	
		try {
			// fetch request type
			Gson gson = new Gson();
			Message requestMessage = gson.fromJson(jsonStream, Message.class);
			if (requestMessage.getMessageJson() == null) {
				throw new IllegalArgumentException("Incoming message is empty!");
			}
			
			JSONObject requestJSON = new JSONObject(requestMessage.getMessageJson());
			
			switch (requestMessage.getMessageID()) {
				case Message.MessageTypes.ADD_USER: {
					String username = requestJSON.getString(Message.MessageKeys.USER_NAME_KEY);
					String password = requestJSON.getString(Message.MessageKeys.USER_PASSWORD_KEY);
					String email    = requestJSON.getString(Message.MessageKeys.USER_EMAIL_KEY);
					String phone    = requestJSON.getString(Message.MessageKeys.USER_PHONE_KEY);
					boolean isGuide = Boolean.getBoolean(requestJSON.
							getString(Message.MessageKeys.USER_TYPE_KEY));
					return addUser(username, password, email, phone, isGuide);
				}
				case Message.MessageTypes.REMOVE_USER: {
					String username = requestJSON.getString(Message.MessageKeys.USER_NAME_KEY);
					String password = requestJSON.getString(Message.MessageKeys.USER_PASSWORD_KEY);
					return rmUser(username, password);
				}
				case Message.MessageTypes.GET_CITY_ID: {
		        	String city = requestJSON.getString(Message.MessageKeys.LOCATION_CITY_NAME_KEY);
		        	String region = requestJSON.getString(Message.MessageKeys.LOCATION_STATE_NAME_KEY);
		        	String country = requestJSON.getString(Message.MessageKeys.LOCATION_COUNTRY_NAME_KEY);
		        	return getCityIdByName(city, region, country);
				}
				case Message.MessageTypes.ADD_TOUR: {
					String uuid = requestJSON.getString(Message.MessageKeys.USER_ID_KEY);
		        	String city = requestJSON.getString(Message.MessageKeys.LOCATION_CITY_NAME_KEY);
		        	String region = requestJSON.getString(Message.MessageKeys.LOCATION_STATE_NAME_KEY);
		        	String country = requestJSON.getString(Message.MessageKeys.LOCATION_COUNTRY_NAME_KEY);
		        	int duration = requestJSON.getInt(Message.MessageKeys.TOURS_DURATION_KEY);
		        	String description = requestJSON.getString(Message.MessageKeys.TOURS_DESCRIPTION_KEY);
		        	// TODO: figure out photos
		        	String photos = requestJSON.getString(Message.MessageKeys.TOURS_PHOTOS_KEY);
		        	int language = requestJSON.getInt(Message.MessageKeys.TOURS_LANGUAGE_KEY);
		        	return addTour(uuid, city, region, country, duration, description, photos, language);
				}
				case Message.MessageTypes.FIND_TOURS_BY_CITY_NAME: {
					String city = requestJSON.getString(Message.MessageKeys.LOCATION_CITY_NAME_KEY);
		        	String region = requestJSON.getString(Message.MessageKeys.LOCATION_STATE_NAME_KEY);
		        	String country = requestJSON.getString(Message.MessageKeys.LOCATION_COUNTRY_NAME_KEY);
					return findToursByCityName(city, region, country);
				}
				case Message.MessageTypes.VALIDATE_UNIQUE_USERNAME: {
					String username = requestJSON.getString(Message.MessageKeys.USER_NAME_KEY);
					return isUniqueUsername(username);
				}
				default: {
					throw new IllegalArgumentException("Illegal message ID!");
				}
			}
		} catch (Exception e) {
		  // crash and burn
			System.out.println("Error parsing JSON request string");
		}
		
		return null;
	}
	
	/**
	 * Constructor: Initialize connection and class variables.
	 */
	public static Connection initConnection() {
		/******************************************************
		 * Establish connection to server and driver
		 ******************************************************/
		System.out.println("JDBCServer> init...");
		Connection connection = null;
		// Register JDBC driver
		try {
		   Class.forName(JDBC_DRIVER);
		}
		catch (ClassNotFoundException ex) {
		   System.err.println("Error: unable to load driver class!");
		   System.exit(1);
		}
		// Get JDBC Driver Manager connection
		try {
		   System.out.println("Connecting to database...");
		   connection = DriverManager.getConnection(DB_URL, USER, PASS);
		}
		catch (SQLException se) {
		   System.err.println("Error: unable to get driver connection!");
		   System.exit(1);
		}
		
		return connection;
	}
	
	/**
	 * Close the given <code>ResultSet></code> object in order to free up resources.
	 *   
	 * @param resultSet the <code>ResultSet</code> to be closed 
	 * @return			<code>true</code> if the statement was closed successfully,
	 * 					<code>false</code> otherwise.
	 */
	public static boolean closeResultSet(ResultSet resultSet) {
		try {
			if (resultSet != null)
				resultSet.close();
		}
		catch (SQLException se) {
			System.err.println("Error closing result set: " + se);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Close the given <code>CallableSatement</code> object in order to free up resources.
	 *   
	 * @param cstmt the <code>CallableSatement</code> to be closed 
	 * @return		<code>true</code> if the statement was closed successfully,
	 * 				<code>false</code> otherwise.
	 */
	public static boolean closeStatement(CallableStatement cstmt) {
		try {
			if (cstmt != null)
				cstmt.close();
		}
		catch (SQLException se) {
			System.err.println("Error closing statement: " + se);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Close the given <code>Connection</code> object in order to free up resources.
	 * 
	 * @param connection the <code>Connection</code> to be closed 
	 * @return			 <code>true</code> if the connection was closed successfully, 
	 * 					 <code>false</code> otherwise.
	 */
	public static boolean closeConnection(Connection connection)
	{
		try {
			if (connection != null) {
				connection.close();
			}
		}
		catch (SQLException se) {
			System.err.println("Error closing connection: " + se);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Execute a statement which modifies the current state of the database.
	 * 
	 * @param cstmt the statement to execute
	 * @return 		<code>true</code> if the modification completed successfully, <code>false</code> otherwise.
	 */
	private static boolean modifyServerDB(CallableStatement cstmt) {
		
		try {
			System.out.println("Updating database...");
			return cstmt.execute();
		}
		catch (PSQLException pe) {
			//pe.printStackTrace();
			System.err.println(pe);
			return false;
		}
		catch (SQLException se) {
			System.err.println(se);
			return false;
		}
		catch (Exception e) {
			System.err.println(e);
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
			System.err.println(pe);
			return null;
		}
		catch (SQLException se) {
			System.err.println(se);
			return null;
		}
		catch (Exception e) {
			System.err.println(e);
			return null;
		}
		
	} // end-method execDBQuery 
	   
   /**
    * Add a new user to the database.
    * 
    * @param username 	the user's name in the application
    * @param password	the user's chosen password
    * @param email		the user's email address
    * @param phone		the user's phone number
    * @param isGuide	indicates whether the user is also a guide
    * @return 			{@link tours_app_server.Message} in JSON format which contains <code>true</code>
    * 					if the operation succeeded, or <code>false</code> otherwise.
	* 					If an error occurred, returns <code>null</code>.
	* @throws NullPointerException if the connection to the database failed.
    */
	public static String addUser(String username, String password, String email, String phone, boolean isGuide) {
		Connection connection = initConnection();
		CallableStatement cstmt = null;
		boolean isModified;
		
		try {
		   if (connection != null) {
			   String sql = "{call add_user (?, ?, ?, ?, ?::INT::BIT)}";
			   cstmt = connection.prepareCall(sql);
			   cstmt.setString(1, username);
			   cstmt.setString(2, password);
			   cstmt.setString(3, email);
			   cstmt.setString(4, phone);
			   cstmt.setBoolean(5, isGuide);
			   
			   // modify database
			   System.out.println("Adding the user to the database...");
			   isModified = modifyServerDB(cstmt);
		   }
		   else { 
			   throw new NullPointerException("Error: connection is null in addUser!");
		   }
		} catch (SQLException se) {
			System.err.println("SQL exception: " + se);
			return null;
		} finally {
			// release resources
			closeStatement(cstmt);
			closeConnection(connection);
		}
		
		// generate JSON message with the results
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(Message.MessageKeys.IS_MODIFIED, isModified);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String messageJsonStr = jsonObject.toString();
		// put the message in an envelope
		Message message = new Message(Message.MessageTypes.ADD_USER, messageJsonStr);
		// convert envelope to JSON format
		Gson gson = new Gson();
		return gson.toJson(message);
	}
	
   /**
    * Remove an existing user from the database.
    * <p>
    * A user can remove himself from the application. Still, the personal
    * details supplied by the user remain in the database.
    * <p>
    * 
    * @param username 	the user's name in the application
    * @param password	the user's chosen password
    * @return 			{@link tours_app_server.Message} in JSON format which contains <code>true</code>
    * 					if the operation succeeded, or <code>false</code> otherwise.
	* 					If an error occurred, returns <code>null</code>.
	* @throws NullPointerException if the connection to the database failed.
    */
	public static String rmUser(String username, String password) {
		Connection connection = initConnection();
		CallableStatement cstmt = null;
		boolean isModified;
		
		try {
		   if (connection != null) {
			   String sql = "{call rm_user (?, ?)}";
			   cstmt = connection.prepareCall(sql);
			   cstmt.setString(1, username);
			   cstmt.setString(2, password);
			   
			   // modify database
			   System.out.println("Removing the user from the database...");
			   isModified = modifyServerDB(cstmt);
		   }
		   else { 
			   throw new NullPointerException("Error: connection is null in rmUser!");
		   }
		} catch (SQLException se) {
			System.err.println("SQL exception: " + se);
			return null;
		} finally {
			// release resources
			closeStatement(cstmt);
			closeConnection(connection);
		}
					
		// generate JSON message with the results
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(Message.MessageKeys.IS_MODIFIED, isModified);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String messageJsonStr = jsonObject.toString();
		// put the message in an envelope
		Message message = new Message(Message.MessageTypes.REMOVE_USER, messageJsonStr);
		// convert envelope to JSON format
		Gson gson = new Gson();
		return gson.toJson(message);
	}
	
	/**
	 * Finds a city's unique ID based on the names of the city, region/state and country.
	 *  
	 * @param city		 the name of the requested city
	 * @param region	 the name of the region/state where the city is located
	 * @param country	 the name of the country where the city is located
	 * @return           {@link tours_app_server.Message} in JSON format which contains the city's ID.
	 * 					 If no match is found, the returned string is empty.
	 * 					 If an error occurred, returns <code>null</code>.
	 * @throws NullPointerException if the connection to the database failed.
	 */
	// the returned city ID will help find queries related to the requested city faster.
	public static String getCityIdByName(String city, String region, String country) {
		Connection connection = initConnection();
		CallableStatement cstmt = null;
		ResultSet resultSet = null;
		String cityId;
			
		try {
		   if (connection != null) {
			   String sql = "{call query_cityid_by_name (?, ?, ?)}";
			   cstmt = connection.prepareCall(sql);
			   cstmt.setString(1, city);
			   cstmt.setString(2, region);
			   cstmt.setString(3, country);
			   
			   // query database
			   System.out.println("Getting city ID from the database...");
			   resultSet = queryDB(cstmt);
			   cityId = ResultSetConverter.convertResultSetIntoString(resultSet);
		   }
		   else { 
			   throw new NullPointerException("Error: connection is null in getCityIdByName!");
		   }
		} catch (SQLException se) {
			System.err.println("SQL exception: " + se);
			return null;
		} finally {
			// release resources
			closeResultSet(resultSet);
			closeStatement(cstmt);
			closeConnection(connection);
		}
		
		// generate JSON message with the results
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(Message.MessageKeys.LOCATION_CITY_ID_KEY, cityId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String messageJsonStr = jsonObject.toString();
		// put the message in an envelope
		Message message = new Message(Message.MessageTypes.GET_CITY_ID, messageJsonStr);
		// convert envelope to JSON format
		Gson gson = new Gson();
		return gson.toJson(message);
	}
	
	public static String addTour(String uuid, String city, String region, String country, 
			int duration, String description, String photos, int language) {
		Connection connection = initConnection();
		CallableStatement cstmt = null;
		boolean isModified;
		
		try {
		   if (connection != null) {
			   String sql = "{call add_tour (?, ?, ?, ?, ?, ?, ?, ?)}";
			   cstmt = connection.prepareCall(sql);
			   cstmt.setString(1, uuid);
			   cstmt.setString(2, city);
			   cstmt.setString(3, region);
			   cstmt.setString(4, country);
			   cstmt.setInt   (5, duration);
			   cstmt.setString(6, description);
			   cstmt.setString(7, photos);
			   cstmt.setInt   (8, language);
			   
			   // modify database
			   System.out.println("Adding tour to the database...");
			   isModified = modifyServerDB(cstmt);
		   }
		   else { 
			   throw new NullPointerException("Error: connection is null in addTour!");
		   }
		} catch (SQLException se) {
			System.err.println("SQL exception: " + se);
			return null;
		} finally {
			// release resources
			closeStatement(cstmt);
			closeConnection(connection);
		}
					
		// generate JSON message with the results
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(Message.MessageKeys.IS_MODIFIED, isModified);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String messageJsonStr = jsonObject.toString();
		// put the message in an envelope
		Message message = new Message(Message.MessageTypes.ADD_TOUR, messageJsonStr);
		// convert envelope to JSON format
		Gson gson = new Gson();
		return gson.toJson(message);
	}
	
	/**
	 * Finds tours in a city based on its name, region/state and country.
	 *  
	 * @param city		 the name of the city
	 * @param region	 the name of the region/state where the city is located
	 * @param country	 the name of the country where the city is located
	 * @return           {@link tours_app_server.Message} in JSON format which contains a JSONArray
	 * 					 object with all the tours.
	 * 					 If no match is found, the returned string is empty.
	 * 					 If an error occurred, returns <code>null</code>.
	 * @throws NullPointerException if the connection to the database failed.
	 */
	public static String findToursByCityName(String city, String region, String country) {
		Connection connection = initConnection();
		CallableStatement cstmt = null;
		ResultSet resultSet = null;
		JSONArray toursJsonArray = null; 
			
		try {
		   if (connection != null) {
			   String sql = "{call query_tour_by_city (?, ?, ?)}";
			   cstmt = connection.prepareCall(sql);
			   cstmt.setString(1, city);
			   cstmt.setString(2, region);
			   cstmt.setString(3, country);
			   
			   // query database
			   System.out.println("Getting city ID from the database...");
			   resultSet = queryDB(cstmt);
			   toursJsonArray = ResultSetConverter.convertResultSetIntoJSON(resultSet);
		   }
		   else { 
			   throw new NullPointerException("Error: connection is null in getCityIdByName!");
		   }
		} catch (SQLException se) {
			System.err.println("SQL exception: " + se);
			return null;
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			// release resources
			closeResultSet(resultSet);
			closeStatement(cstmt);
			closeConnection(connection);
		}
		
		// generate JSON message with the results
		JSONObject jsonObject = new JSONObject();
		try {
			// TODO: continue here
			jsonObject.put(Message.MessageKeys.TOURS_BY_CITY_KEY, toursJsonArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String messageJsonStr = jsonObject.toString();
		// put the message in an envelope
		Message message = new Message(Message.MessageTypes.FIND_TOURS_BY_CITY_NAME, messageJsonStr);
		// convert envelope to JSON format
		Gson gson = new Gson();
		return gson.toJson(message);
	}
	
	/**
	 * Validates that the desired user name is not already taken.
	 * <p>
	 * Invoke as soon as the user is done typing his requested user name.
	 * <p>
	 *  	
	 * @param  username  the desired user name
	 * @return  		 {@link tours_app_server.Message} in JSON format which contains <code>true</code>
     * 					 if the user name is unique (not found in the database), or <code>false</code> 
     * 					 otherwise. If an error occurred, returns <code>null</code>.
	 * @throws NullPointerException if the connection to the database failed.
	 */
	public static String isUniqueUsername(String username) {
		Connection connection = initConnection();
		CallableStatement cstmt = null;
		ResultSet resultSet = null;
		boolean isUnique;
		
		try {
			if (connection != null) {
			   String sql = "{call validate_unique_username (?)}";
			   cstmt = connection.prepareCall(sql);
			   cstmt.setString(1, username);
			   resultSet = queryDB(cstmt);
			   isUnique = !ResultSetConverter.convertResultSetIntoBoolean(resultSet);
			}
			else { 
				throw new NullPointerException("Error: connection is null in validateUniqueUsername!");
			}
		} catch (SQLException se) {
			//TODO: nothing we can do?
			System.err.println("SQL exception: " + se);
			return null;
		} finally {
			closeResultSet(resultSet);
			closeStatement(cstmt);
			closeConnection(connection);
		}

		// generate JSON message with the results
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(Message.MessageKeys.IS_EXISTS, isUnique);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String messageJsonStr = jsonObject.toString();
		// put the message in an envelope
		Message message = new Message(Message.MessageTypes.VALIDATE_UNIQUE_USERNAME, messageJsonStr);
		// convert envelope to JSON format
		Gson gson = new Gson();
		return gson.toJson(message);
	}
	
}

	  