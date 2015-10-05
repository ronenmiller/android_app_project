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
					int numOfPlacesRequested = requestJSON.getInt(Message.MessageKeys.RESERVATION_OCCUPIED_KEY);
					return reserveSlot(slotId, userId, numOfPlacesRequested);
				}
				case Message.MessageTypes.CREATE_TOUR: {
					Long osmId = requestJSON.getLong(Message.MessageKeys.LOCATION_OSM_ID_KEY);
					String userId = requestJSON.getString(Message.MessageKeys.USER_ID_KEY);
					String title = requestJSON.getString(Message.MessageKeys.TOUR_TITLE_KEY);
		        	int language = requestJSON.getInt(Message.MessageKeys.TOUR_LANGUAGE_KEY);
		        	int duration = requestJSON.getInt(Message.MessageKeys.TOUR_DURATION_KEY);
		        	String location = requestJSON.getString(Message.MessageKeys.TOUR_LOCATION_KEY);
		        	String description = requestJSON.getString(Message.MessageKeys.TOUR_DESCRIPTION_KEY);
		        	return createTour(osmId, userId, title, language, duration, location, description);
				}
				case Message.MessageTypes.CREATE_SLOT: {
					String guideId = requestJSON.getString(Message.MessageKeys.SLOT_GUIDE_ID_KEY);
					int tourId = requestJSON.getInt(Message.MessageKeys.SLOT_TOUR_ID_KEY);
					int date = requestJSON.getInt(Message.MessageKeys.SLOT_DATE_KEY);
					long time = requestJSON.getLong(Message.MessageKeys.SLOT_TIME_KEY);
					int capacity = requestJSON.getInt(Message.MessageKeys.SLOT_CAPACITY_KEY);
					return createSlot(guideId, tourId, date, time, capacity);
				}
				case Message.MessageTypes.ADD_LOCATION: {
					long osmId = requestJSON.getLong(Message.MessageKeys.LOCATION_OSM_ID_KEY);
					String locationName = requestJSON.getString(Message.MessageKeys.LOCATION_OSM_NAME_KEY);
					String locationType = requestJSON.getString(Message.MessageKeys.LOCATION_OSM_TYPE_KEY);
					double latitude = requestJSON.getDouble(Message.MessageKeys.LOCATION_OSM_LATITUDE_KEY);
					double longitude = requestJSON.getDouble(Message.MessageKeys.LOCATION_OSM_LONGITUDE_KEY);
					return addLocation(osmId, locationName, locationType, latitude, longitude);
				}
				case Message.MessageTypes.QUERY_TOURS_BY_MANAGER_ID: {
					String managerId = requestJSON.getString(Message.MessageKeys.TOUR_MANAGER_KEY);
					return queryToursByManagerId(managerId);
				}
				case Message.MessageTypes.DELETE_TOUR: {
					int tourId = requestJSON.getInt(Message.MessageKeys.TOUR_ID_KEY);
					return deleteTour(tourId);
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
	
	public static String createTour(long osmId, String userId, String title, int language, 
			int duration, String location, String description) {
		Connection connection = initConnection();
		CallableStatement cstmt = null;
		boolean isModified;
		
		try {
		   if (connection != null) {
			   String sql = "{call create_tour (?, ?, ?, ?, ?, ?, ?)}";
			   cstmt = connection.prepareCall(sql);
			   cstmt.setLong  (1, osmId);
			   cstmt.setString(2, userId);
			   cstmt.setString(3, title);
			   cstmt.setInt   (4, language);
			   cstmt.setInt   (5, duration);
			   cstmt.setString(6, location);
			   cstmt.setString(7, description);
			   
			   // modify database
			   System.out.println("Adding tour to the database...");
			   isModified = modifyServerDB(cstmt);
		   }
		   else { 
			   throw new NullPointerException("Error: connection is null in createTour!");
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
		Message message = new Message(Message.MessageTypes.CREATE_TOUR, messageJsonStr);
		// convert envelope to JSON format
		Gson gson = new Gson();
		return gson.toJson(message);
	}
	
	
	public static String createSlot(String guideId, int tourID, int date, long time, int capacity) {
		Connection connection = initConnection();
		CallableStatement cstmt = null;
		boolean isModified;
		
		try {
		   if (connection != null) {
			   String sql = "{call create_slot (?, ?, ?, ?, ?)}";
			   cstmt = connection.prepareCall(sql);
			   cstmt.setString(1, guideId);
			   cstmt.setInt(2, tourID);
			   cstmt.setInt(3, date);
			   cstmt.setLong(4, time);
			   cstmt.setInt(5, capacity);
			   
			   // modify database
			   System.out.println("Adding slot to the database...");
			   isModified = modifyServerDB(cstmt);
		   }
		   else { 
			   throw new NullPointerException("Error: connection is null in createSlot!");
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
		Message message = new Message(Message.MessageTypes.CREATE_SLOT, messageJsonStr);
		// convert envelope to JSON format
		Gson gson = new Gson();
		return gson.toJson(message);
	}
	
	public static String addLocation(long osmId, String locationName, String locationType, double latitude, double longitude) {
		Connection connection = initConnection();
		CallableStatement cstmt = null;
		boolean isModified = true;
		
		try {
		   if (connection != null) {
			   String sql = "{call add_location (?, ?, ?, ?, ?)}";
			   cstmt = connection.prepareCall(sql);
			   cstmt.setLong(1, osmId);
			   cstmt.setString(2, locationName);
			   cstmt.setString(3, locationType);
			   cstmt.setFloat(4, (float)latitude);
			   cstmt.setFloat(5, (float)longitude);
			   
			   // modify database
			   System.out.println("Adding location to the database...");
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
		Message message = new Message(Message.MessageTypes.ADD_LOCATION, messageJsonStr);
		// convert envelope to JSON format
		Gson gson = new Gson();
		return gson.toJson(message);
	}
	
	
	public static String queryToursByManagerId(String managerId) throws Exception {
		Connection connection = initConnection();
		CallableStatement cstmt = null;
		ResultSet resultSet = null;
		JSONArray tours = null;
		
		try {
			if (connection != null) {
			   String sql = "{call query_tours_by_manager_id (?)}";
			   cstmt = connection.prepareCall(sql);
			   cstmt.setString(1, managerId);
			   resultSet = queryDB(cstmt);
			   tours = ResultSetConverter.convertResultSetIntoJSON(resultSet);
			}
			else { 
				throw new NullPointerException("Error: connection is null in queryToursByManagerId!");
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
			Message message = new Message(Message.MessageTypes.QUERY_TOURS_BY_MANAGER_ID, messageJsonStr);
			// convert envelope to JSON format
			Gson gson = new Gson();
			return gson.toJson(message);
		}
		else {
			throw new Exception("Error: Something went wrong while converting result set to JSONArray");
		}
	}
	
	
	public static String deleteTour(int tourId) {
		Connection connection = initConnection();
		CallableStatement cstmt = null;
		boolean isModified;
		
		try {
		   if (connection != null) {
			   String sql = "{call delete_tour (?)}";
			   cstmt = connection.prepareCall(sql);
			   cstmt.setInt(1, tourId);
			   
			   // modify database
			   System.out.println("Deleting tour from the database...");
			   isModified = modifyServerDB(cstmt);
		   }
		   else { 
			   throw new NullPointerException("Error: connection is null in deleteTour!");
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
		Message message = new Message(Message.MessageTypes.DELETE_TOUR, messageJsonStr);
		// convert envelope to JSON format
		Gson gson = new Gson();
		return gson.toJson(message);
	}
	
}

	  