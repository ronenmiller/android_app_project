package tours_app_server;

import java.sql.*;
import java.time.LocalDate;
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
//				case Message.MessageTypes.REMOVE_USER: {
//					String username = requestJSON.getString(Message.MessageKeys.USER_NAME_KEY);
//					String password = requestJSON.getString(Message.MessageKeys.USER_PASSWORD_KEY);
//					return rmUser(username, password);
//				}
//				case Message.MessageTypes.GET_CITY_ID: {
//		        	String city = requestJSON.getString(Message.MessageKeys.LOCATION_CITY_NAME_KEY);
//		        	String region = requestJSON.getString(Message.MessageKeys.LOCATION_STATE_NAME_KEY);
//		        	String country = requestJSON.getString(Message.MessageKeys.LOCATION_COUNTRY_NAME_KEY);
//		        	return getCityIdByName(city, region, country);
//				}
//				case Message.MessageTypes.ADD_TOUR: {
//					String uuid = requestJSON.getString(Message.MessageKeys.USER_ID_KEY);
//		        	String city = requestJSON.getString(Message.MessageKeys.LOCATION_CITY_NAME_KEY);
//		        	String region = requestJSON.getString(Message.MessageKeys.LOCATION_STATE_NAME_KEY);
//		        	String country = requestJSON.getString(Message.MessageKeys.LOCATION_COUNTRY_NAME_KEY);
//		        	int duration = requestJSON.getInt(Message.MessageKeys.TOURS_DURATION_KEY);
//		        	String location = requestJSON.getString(Message.MessageKeys.TOURS_LOCATION_KEY);
//		        	String description = requestJSON.getString(Message.MessageKeys.TOURS_DESCRIPTION_KEY);
//		        	// TODO: figure out photos
//		        	String photos = requestJSON.getString(Message.MessageKeys.TOURS_PHOTOS_KEY);
//		        	int language = requestJSON.getInt(Message.MessageKeys.TOURS_LANGUAGE_KEY);
//		        	return addTour(uuid, city, region, country, duration, location, description, photos, language);
//				}
//				case Message.MessageTypes.FIND_TOURS_BY_CITY_NAME: {
//					String city = requestJSON.getString(Message.MessageKeys.LOCATION_CITY_NAME_KEY);
//		        	String region = requestJSON.getString(Message.MessageKeys.LOCATION_STATE_NAME_KEY);
//		        	String country = requestJSON.getString(Message.MessageKeys.LOCATION_COUNTRY_NAME_KEY);
//					return findToursByCityName(city, region, country);
//				}
//				case Message.MessageTypes.ADD_SLOT: {
//					int tourID = requestJSON.getInt(Message.MessageKeys.TOURS_ID_KEY);
//					Date date = (Date)requestJSON.get(Message.MessageKeys.SLOT_DATE_KEY);
//					Time time = (Time)requestJSON.get(Message.MessageKeys.SLOT_TIME_KEY);
//					int vacant = requestJSON.getInt(Message.MessageKeys.SLOT_NUM_VACANT_KEY);
//					return addSlot(tourID, date, time, vacant);
//				}
//				case Message.MessageTypes.FIND_SLOTS_BY_CITY_NAME: {
//					String city = requestJSON.getString(Message.MessageKeys.LOCATION_CITY_NAME_KEY);
//		        	String region = requestJSON.getString(Message.MessageKeys.LOCATION_STATE_NAME_KEY);
//		        	String country = requestJSON.getString(Message.MessageKeys.LOCATION_COUNTRY_NAME_KEY);
//					return findSlotsByCityName(city, region, country);
//				}
				case Message.MessageTypes.VALIDATE_UNIQUE_USERNAME: {
					String username = requestJSON.getString(Message.MessageKeys.USER_NAME_KEY);
					return isUniqueUsername(username);
				}
				case Message.MessageTypes.VALIDATE_UNIQUE_EMAIL: {
					String email = requestJSON.getString(Message.MessageKeys.USER_EMAIL_KEY);
					return isUniqueEmail(email);
				}
				case Message.MessageTypes.ADD_USER: {
					String username = requestJSON.getString(Message.MessageKeys.USER_NAME_KEY);
					String password = requestJSON.getString(Message.MessageKeys.USER_PASSWORD_KEY);
					String email    = requestJSON.getString(Message.MessageKeys.USER_EMAIL_KEY);
					boolean isGuide = Boolean.parseBoolean(requestJSON.
							getString(Message.MessageKeys.USER_TYPE_KEY));
					return addUser(username, password, email, isGuide);
				}
				case Message.MessageTypes.VALIDATE_CREDENTIALS: {
					String username = requestJSON.getString(Message.MessageKeys.USER_NAME_KEY);
					String password = requestJSON.getString(Message.MessageKeys.USER_PASSWORD_KEY);
					return validateCredentials(username, password);
				}
				case Message.MessageTypes.QUERY_TOURS_BY_OSM_ID: {
					long osmId = requestJSON.getLong(Message.MessageKeys.LOCATION_OSM_ID_KEY);
					return queryToursByOsmId(osmId);
				}
				case Message.MessageTypes.QUERY_SLOTS_BY_TOUR_ID: {
					int tourId = requestJSON.getInt(Message.MessageKeys.TOUR_ID_KEY);
					return querySlotsByTourId(tourId);
				}
				case Message.MessageTypes.RESERVE_SLOT: {
					long slotId = requestJSON.getLong(Message.MessageKeys.SLOT_ID_KEY);
					String userId = requestJSON.getString(Message.MessageKeys.USER_ID_KEY);
					int numOfPlacesRequested = requestJSON.getInt(Message.MessageKeys.RESERVATION_NUM_PARTICIPANTS_KEY);
					return reserveSlot(slotId, userId, numOfPlacesRequested);
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
	
//   /**
//    * Remove an existing user from the database.
//    * <p>
//    * A user can remove himself from the application. Still, the personal
//    * details supplied by the user remain in the database.
//    * <p>
//    * 
//    * @param username 	the user's name in the application
//    * @param password	the user's chosen password
//    * @return 			{@link tours_app_server.Message} in JSON format which contains <code>true</code>
//    * 					if the operation succeeded, or <code>false</code> otherwise.
//	* 					If an error occurred, returns <code>null</code>.
//	* @throws NullPointerException if the connection to the database failed.
//    */
//	public static String rmUser(String username, String password) {
//		Connection connection = initConnection();
//		CallableStatement cstmt = null;
//		boolean isModified;
//		
//		try {
//		   if (connection != null) {
//			   String sql = "{call rm_user (?, ?)}";
//			   cstmt = connection.prepareCall(sql);
//			   cstmt.setString(1, username);
//			   cstmt.setString(2, password);
//			   
//			   // modify database
//			   System.out.println("Removing the user from the database...");
//			   isModified = modifyServerDB(cstmt);
//		   }
//		   else { 
//			   throw new NullPointerException("Error: connection is null in rmUser!");
//		   }
//		} catch (SQLException se) {
//			System.err.println("SQL exception: " + se);
//			return null;
//		} finally {
//			// release resources
//			closeStatement(cstmt);
//			closeConnection(connection);
//		}
//					
//		// generate JSON message with the results
//		JSONObject jsonObject = new JSONObject();
//		try {
//			jsonObject.put(Message.MessageKeys.IS_MODIFIED, isModified);
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		String messageJsonStr = jsonObject.toString();
//		// put the message in an envelope
//		Message message = new Message(Message.MessageTypes.REMOVE_USER, messageJsonStr);
//		// convert envelope to JSON format
//		Gson gson = new Gson();
//		return gson.toJson(message);
//	}
//	
//	/**
//	 * Finds a city's unique ID based on the names of the city, region/state and country.
//	 *  
//	 * @param city		 the name of the requested city
//	 * @param region	 the name of the region/state where the city is located
//	 * @param country	 the name of the country where the city is located
//	 * @return           {@link tours_app_server.Message} in JSON format which contains the city's ID.
//	 * 					 If no match is found, the returned string is empty.
//	 * 					 If an error occurred, returns <code>null</code>.
//	 * @throws NullPointerException if the connection to the database failed.
//	 */
//	// the returned city ID will help find queries related to the requested city faster.
//	public static String getCityIdByName(String city, String region, String country) {
//		Connection connection = initConnection();
//		CallableStatement cstmt = null;
//		ResultSet resultSet = null;
//		String cityId;
//			
//		try {
//		   if (connection != null) {
//			   String sql = "{call query_cityid_by_name (?, ?, ?)}";
//			   cstmt = connection.prepareCall(sql);
//			   cstmt.setString(1, city);
//			   cstmt.setString(2, region);
//			   cstmt.setString(3, country);
//			   
//			   // query database
//			   System.out.println("Getting city ID from the database...");
//			   resultSet = queryDB(cstmt);
//			   cityId = ResultSetConverter.convertResultSetIntoString(resultSet);
//		   }
//		   else { 
//			   throw new NullPointerException("Error: connection is null in getCityIdByName!");
//		   }
//		} catch (SQLException se) {
//			System.err.println("SQL exception: " + se);
//			return null;
//		} finally {
//			// release resources
//			closeResultSet(resultSet);
//			closeStatement(cstmt);
//			closeConnection(connection);
//		}
//		
//		// generate JSON message with the results
//		JSONObject jsonObject = new JSONObject();
//		try {
//			jsonObject.put(Message.MessageKeys.LOCATION_CITY_ID_KEY, cityId);
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		String messageJsonStr = jsonObject.toString();
//		// put the message in an envelope
////		Message message = new Message(Message.MessageTypes.GET_CITY_ID, messageJsonStr);
//		// convert envelope to JSON format
//		Gson gson = new Gson();
//		return gson.toJson(message);
//	}
//	
//	public static String addTour(String uuid, String city, String region, String country, 
//			int duration, String location, String description, String photos, int language) {
//		Connection connection = initConnection();
//		CallableStatement cstmt = null;
//		boolean isModified;
//		
//		try {
//		   if (connection != null) {
//			   String sql = "{call add_tour (?, ?, ?, ?, ?, ?, ?, ?, ?)}";
//			   cstmt = connection.prepareCall(sql);
//			   cstmt.setString(1, uuid);
//			   cstmt.setString(2, city);
//			   cstmt.setString(3, region);
//			   cstmt.setString(4, country);
//			   cstmt.setInt   (5, duration);
//			   cstmt.setString(6, location);
//			   cstmt.setString(7, description);
//			   cstmt.setString(8, photos);
//			   cstmt.setInt   (9, language);
//			   
//			   // modify database
//			   System.out.println("Adding tour to the database...");
//			   isModified = modifyServerDB(cstmt);
//		   }
//		   else { 
//			   throw new NullPointerException("Error: connection is null in addTour!");
//		   }
//		} catch (SQLException se) {
//			System.err.println("SQL exception: " + se);
//			return null;
//		} finally {
//			// release resources
//			closeStatement(cstmt);
//			closeConnection(connection);
//		}
//					
//		// generate JSON message with the results
//		JSONObject jsonObject = new JSONObject();
//		try {
//			jsonObject.put(Message.MessageKeys.IS_MODIFIED, isModified);
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		String messageJsonStr = jsonObject.toString();
//		// put the message in an envelope
//		Message message = new Message(Message.MessageTypes.ADD_TOUR, messageJsonStr);
//		// convert envelope to JSON format
//		Gson gson = new Gson();
//		return gson.toJson(message);
//	}
//	
//	/**
//	 * Finds tours in a city based on its name, region/state and country.
//	 *  
//	 * @param city		 the name of the city
//	 * @param region	 the name of the region/state where the city is located
//	 * @param country	 the name of the country where the city is located
//	 * @return           {@link tours_app_server.Message} in JSON format which contains a JSONArray
//	 * 					 object with all the tours.
//	 * 					 If no match is found, the returned string is empty.
//	 * 					 If an error occurred, returns <code>null</code>.
//	 * @throws NullPointerException if the connection to the database failed.
//	 */
//	public static String findToursByCityName(String city, String region, String country) {
//		Connection connection = initConnection();
//		CallableStatement cstmt = null;
//		ResultSet resultSet = null;
//		JSONArray toursJsonArray = null; 
//			
//		try {
//		   if (connection != null) {
//			   String sql = "{call query_tour_by_city (?, ?, ?)}";
//			   cstmt = connection.prepareCall(sql);
//			   cstmt.setString(1, city);
//			   cstmt.setString(2, region);
//			   cstmt.setString(3, country);
//			   
//			   // query database
//			   System.out.println("Getting tours from the database...");
//			   resultSet = queryDB(cstmt);
//			   toursJsonArray = ResultSetConverter.convertResultSetIntoJSON(resultSet);
//		   }
//		   else { 
//			   throw new NullPointerException("Error: connection is null in getCityIdByName!");
//		   }
//		} catch (SQLException se) {
//			System.err.println("SQL exception: " + se);
//			return null;
//		} catch (JSONException e) {
//			e.printStackTrace();
//		} finally {
//			// release resources
//			closeResultSet(resultSet);
//			closeStatement(cstmt);
//			closeConnection(connection);
//		}
//		
//		// generate JSON message with the results
//		JSONObject jsonObject = new JSONObject();
//		try {
//			// TODO: continue here
//			jsonObject.put(Message.MessageKeys.TOURS_BY_CITY_KEY, toursJsonArray);
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		String messageJsonStr = jsonObject.toString();
//		// put the message in an envelope
//		Message message = new Message(Message.MessageTypes.FIND_TOURS_BY_CITY_NAME, messageJsonStr);
//		// convert envelope to JSON format
//		Gson gson = new Gson();
//		return gson.toJson(message);
//	}
//	
//	public static String addSlot(int tourID, Date date, Time time, int vacant) {
//		Connection connection = initConnection();
//		CallableStatement cstmt = null;
//		boolean isModified;
//		
//		try {
//		   if (connection != null) {
//			   String sql = "{call add_slot (?, ?, ?, ?)}";
//			   cstmt = connection.prepareCall(sql);
//			   cstmt.setInt(1, tourID);
//			   cstmt.setDate(2, date);
//			   cstmt.setTime(3, time);
//			   cstmt.setInt(4, vacant);
//			   
//			   // modify database
//			   System.out.println("Adding slot to the database...");
//			   isModified = modifyServerDB(cstmt);
//		   }
//		   else { 
//			   throw new NullPointerException("Error: connection is null in addSlot!");
//		   }
//		} catch (SQLException se) {
//			System.err.println("SQL exception: " + se);
//			return null;
//		} finally {
//			// release resources
//			closeStatement(cstmt);
//			closeConnection(connection);
//		}
//					
//		// generate JSON message with the results
//		JSONObject jsonObject = new JSONObject();
//		try {
//			jsonObject.put(Message.MessageKeys.IS_MODIFIED, isModified);
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		String messageJsonStr = jsonObject.toString();
//		// put the message in an envelope
//		Message message = new Message(Message.MessageTypes.ADD_SLOT, messageJsonStr);
//		// convert envelope to JSON format
//		Gson gson = new Gson();
//		return gson.toJson(message);
//	}
//	
//	/**
//	 * Finds open slots for tours in a city based on its name, region/state and country.
//	 *  
//	 * @param city		 the name of the city
//	 * @param region	 the name of the region/state where the city is located
//	 * @param country	 the name of the country where the city is located
//	 * @return           {@link tours_app_server.Message} in JSON format which contains a JSONArray
//	 * 					 object with all the open slots.
//	 * 					 If no match is found, the returned string is empty.
//	 * 					 If an error occurred, returns <code>null</code>.
//	 * @throws NullPointerException if the connection to the database failed.
//	 */
//	public static String findSlotsByCityName(String city, String region, String country) {
//		Connection connection = initConnection();
//		CallableStatement cstmt = null;
//		ResultSet resultSet = null;
//		JSONArray slotsJsonArray = null; 
//			
//		try {
//		   if (connection != null) {
//			   String sql = "{call query_slots_by_city (?, ?, ?)}";
//			   cstmt = connection.prepareCall(sql);
//			   cstmt.setString(1, city);
//			   cstmt.setString(2, region);
//			   cstmt.setString(3, country);
//			   Date date = Date.valueOf(LocalDate.now());
//			   cstmt.setDate(4, date);
//			   
//			   // query database
//			   System.out.println("Getting slots from the database...");
//			   resultSet = queryDB(cstmt);
//			   slotsJsonArray = ResultSetConverter.convertResultSetIntoJSON(resultSet);
//		   }
//		   else { 
//			   throw new NullPointerException("Error: connection is null in getCityIdByName!");
//		   }
//		} catch (SQLException se) {
//			System.err.println("SQL exception: " + se);
//			return null;
//		} catch (JSONException e) {
//			e.printStackTrace();
//		} catch (NullPointerException npe) {
//			npe.printStackTrace();
//		} finally {
//			// release resources
//			closeResultSet(resultSet);
//			closeStatement(cstmt);
//			closeConnection(connection);
//		}
//		
//		// generate JSON message with the results
//		JSONObject jsonObject = new JSONObject();
//		try {
//			// TODO: continue here
//			jsonObject.put(Message.MessageKeys.SLOTS_BY_CITY_KEY, slotsJsonArray);
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		String messageJsonStr = jsonObject.toString();
//		// put the message in an envelope
//		Message message = new Message(Message.MessageTypes.FIND_SLOTS_BY_CITY_NAME, messageJsonStr);
//		// convert envelope to JSON format
//		Gson gson = new Gson();
//		return gson.toJson(message);
//	}

	
	/**
	 * Validates that the desired user name is not already taken.
	 * <p>
	 * Invoke when the user clicks on the sign up button.
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
			jsonObject.put(Message.MessageKeys.IS_UNIQUE, isUnique);
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
	
	/**
	 * Validates that the desired email is not already taken.
	 * <p>
	 * Invoke when the user clicks on the sign up button.
	 * <p>
	 *  	
	 * @param  email  the desired email address
	 * @return  		 {@link tours_app_server.Message} in JSON format which contains <code>true</code>
     * 					 if the email address is unique (not found in the database), or <code>false</code> 
     * 					 otherwise. If an error occurred, returns <code>null</code>.
	 * @throws NullPointerException if the connection to the database failed.
	 */
	public static String isUniqueEmail(String email) {
		Connection connection = initConnection();
		CallableStatement cstmt = null;
		ResultSet resultSet = null;
		boolean isUnique;
		
		try {
			if (connection != null) {
			   String sql = "{call validate_unique_email (?)}";
			   cstmt = connection.prepareCall(sql);
			   cstmt.setString(1, email);
			   resultSet = queryDB(cstmt);
			   isUnique = !ResultSetConverter.convertResultSetIntoBoolean(resultSet);
			}
			else { 
				throw new NullPointerException("Error: connection is null in validateUniqueEmail!");
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
			jsonObject.put(Message.MessageKeys.IS_UNIQUE, isUnique);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String messageJsonStr = jsonObject.toString();
		// put the message in an envelope
		Message message = new Message(Message.MessageTypes.VALIDATE_UNIQUE_EMAIL, messageJsonStr);
		// convert envelope to JSON format
		Gson gson = new Gson();
		return gson.toJson(message);
	}
	
	/**
    * Add a new user to the database.
    * 
    * @param username 	the user's name in the application
    * @param password	the user's chosen password
    * @param email		the user's email address
    * @param isGuide	indicates whether the user is also a guide
    * @return 			{@link tours_app_server.Message} in JSON format which contains <code>true</code>
    * 					if the operation succeeded, or <code>false</code> otherwise.
	* 					If an error occurred, returns <code>null</code>.
	* @throws NullPointerException if the connection to the database failed.
    */
	public static String addUser(String username, String password, String email, boolean isGuide) {
		Connection connection = initConnection();
		CallableStatement cstmt = null;
		boolean isModified;
		
		try {
		   if (connection != null) {
			   String sql = "{call add_user (?, ?, ?, ?::INT::BIT)}";
			   cstmt = connection.prepareCall(sql);
			   cstmt.setString(1, username);
			   cstmt.setString(2, password);
			   cstmt.setString(3, email);
			   cstmt.setBoolean(4, isGuide);
			   
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
	
	public static String validateCredentials(String username, String password) throws Exception {
		Connection connection = initConnection();
		CallableStatement cstmt = null;
		ResultSet resultSet = null;
		JSONArray userAttributes = null;
		
		try {
			if (connection != null) {
			   String sql = "{call validate_credentials (?, ?)}";
			   cstmt = connection.prepareCall(sql);
			   cstmt.setString(1, username);
			   cstmt.setString(2, password);
			   resultSet = queryDB(cstmt);
			   System.out.println("User attr1: " + resultSet.toString());
			   userAttributes = ResultSetConverter.convertResultSetIntoJSON(resultSet);
			   System.out.println("User attr2: " + userAttributes.toString());
			}
			else { 
				throw new NullPointerException("Error: connection is null in validateCredentials!");
			}
		} catch (SQLException se) {
			//TODO: nothing we can do?
			System.err.println("SQL exception: " + se);
			return null;
		} catch (JSONException je) {
			System.err.println("JSON exception: " + je);
		} finally {
			closeResultSet(resultSet);
			closeStatement(cstmt);
			closeConnection(connection);
		}
		
		if (userAttributes != null) {
			// generate JSON message with the results
			String messageJsonStr = userAttributes.toString();
			// put the message in an envelope
			Message message = new Message(Message.MessageTypes.VALIDATE_CREDENTIALS, messageJsonStr);
			// convert envelope to JSON format
			Gson gson = new Gson();
			return gson.toJson(message);
		}
		else {
			throw new Exception("Error: Something went wrong while converting result set to JSONArray");
		}
	}
	
	public static String queryToursByOsmId(long osmId) throws Exception {
		Connection connection = initConnection();
		CallableStatement cstmt = null;
		ResultSet resultSet = null;
		JSONArray tours = null;
		
		try {
			if (connection != null) {
			   String sql = "{call query_tours_by_osm_id (?)}";
			   cstmt = connection.prepareCall(sql);
			   cstmt.setLong(1, osmId);
			   resultSet = queryDB(cstmt);
			   tours = ResultSetConverter.convertResultSetIntoJSON(resultSet);
			}
			else { 
				throw new NullPointerException("Error: connection is null in queryToursByOsmId!");
			}
		} catch (SQLException se) {
			System.err.println("SQL exception: " + se);
			return null;
		} catch (JSONException je) {
			System.err.println("JSON exception: " + je);
		} finally {
			closeResultSet(resultSet);
			closeStatement(cstmt);
			closeConnection(connection);
		}
		
		if (tours != null) {
			// generate JSON message with the results
			String messageJsonStr = tours.toString();
			// put the message in an envelope
			Message message = new Message(Message.MessageTypes.QUERY_TOURS_BY_OSM_ID, messageJsonStr);
			// convert envelope to JSON format
			Gson gson = new Gson();
			return gson.toJson(message);
		}
		else {
			throw new Exception("Error: Something went wrong while converting result set to JSONArray");
		}
	}
	
	public static String querySlotsByTourId(int tourId) throws Exception {
		Connection connection = initConnection();
		CallableStatement cstmt = null;
		ResultSet resultSet = null;
		JSONArray slots = null;
		
		try {
			if (connection != null) {
			   String sql = "{call query_slots_by_tour_id (?)}";
			   cstmt = connection.prepareCall(sql);
			   cstmt.setInt(1, tourId);
			   resultSet = queryDB(cstmt);
			   slots = ResultSetConverter.convertResultSetIntoJSON(resultSet);
			}
			else { 
				throw new NullPointerException("Error: connection is null in querySlotsByTourId!");
			}
		} catch (SQLException se) {
			System.err.println("SQL exception: " + se);
			return null;
		} catch (JSONException je) {
			System.err.println("JSON exception: " + je);
		} finally {
			closeResultSet(resultSet);
			closeStatement(cstmt);
			closeConnection(connection);
		}
		
		if (slots != null) {
			// generate JSON message with the results
			String messageJsonStr = slots.toString();
			// put the message in an envelope
			Message message = new Message(Message.MessageTypes.QUERY_SLOTS_BY_TOUR_ID, messageJsonStr);
			// convert envelope to JSON format
			Gson gson = new Gson();
			return gson.toJson(message);
		}
		else {
			throw new Exception("Error: Something went wrong while converting result set to JSONArray");
		}
	}
	
	public static String reserveSlot(long slotId, String userId, int numOfPlacesRequested) {
		Connection connection = initConnection();
		CallableStatement cstmt = null;
		boolean isModified;
		
		try {
		   if (connection != null) {
			   String sql = "{call reserve_slot (?, ?, ?)}";
			   cstmt = connection.prepareCall(sql);
			   cstmt.setLong(1, slotId);
			   cstmt.setString(2, userId);
			   cstmt.setInt(3, numOfPlacesRequested);
			   isModified = modifyServerDB(cstmt);
		   }
		   else { 
			   throw new NullPointerException("Error: connection is null in reserveSlot!");
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
		Message message = new Message(Message.MessageTypes.RESERVE_SLOT, messageJsonStr);
		// convert envelope to JSON format
		Gson gson = new Gson();
		return gson.toJson(message);
	}
	
}

	  