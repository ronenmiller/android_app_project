package tours_app_server;

//Basic container class for server-client messages-based communication.
public class Message {
	
	private final int mMessageID;
	private final String mMessageJsonStr;
	
	public Message(int messageID, String messageJsonStr) {
		mMessageID = messageID;
		mMessageJsonStr = messageJsonStr;
	}
	
	public int getMessageID() {
		return mMessageID;
	}
	
	public String getMessageJson() {
		return mMessageJsonStr;
	}
	
	public class MessageTypes {
		public static final int VALIDATE_UNIQUE_USERNAME = 0;
		public static final int VALIDATE_UNIQUE_EMAIL = 1;
		public static final int ADD_USER = 2;
		public static final int VALIDATE_CREDENTIALS = 3;
     public static final int QUERY_TOURS_BY_OSM_ID = 4;
     public static final int QUERY_SLOTS_BY_TOUR_ID = 5;
		public static final int RESERVE_SLOT = 6;
		public static final int CREATE_TOUR = 7;
		public static final int CREATE_SLOT = 8;
		public static final int ADD_LOCATION = 9;
		public static final int QUERY_TOURS_BY_MANAGER_ID = 10;
     public static final int DELETE_TOUR = 11;
     public static final int QUERY_SLOTS_BY_GUIDE_ID = 12;
     public static final int QUERY_RESERVATIONS_BY_SLOT_ID = 13;
     public static final int EDIT_SLOT = 14;
     public static final int DELETE_SLOT = 15;
//		public static final int UPDATE_USER = 1;
//		public static final int REMOVE_USER = 2;
//		public static final int UPDATE_TOUR = 4;
//		public static final int REMOVE_SLOT = 7;

	}
	
	public class MessageKeys {
		public static final String IS_MODIFIED = "is_modified";
		public static final String IS_EXISTS = "is_exists";
     public static final String IS_UNIQUE = "is_unique";

		public static final String LOCATION_OSM_ID_KEY = "osm_id";
		public static final String LOCATION_OSM_NAME_KEY = "display_name";
		public static final String LOCATION_OSM_TYPE_KEY = "type";
		public static final String LOCATION_OSM_LATITUDE_KEY = "lat";
		public static final String LOCATION_OSM_LONGITUDE_KEY = "lon";

		public static final String USER_ID_KEY = "u_id";
		public static final String USER_NAME_KEY = "u_name";
		public static final String USER_PASSWORD_KEY = "u_pass";
		public static final String USER_EMAIL_KEY = "email";
		public static final String USER_TYPE_KEY = "u_type";
		public static final String USER_RATING_KEY = "u_rating";
		
		public static final String TOUR_ID_KEY = "t_id";
		public static final String TOUR_OSM_ID = "t_osm_id";
		public static final String TOUR_MANAGER_KEY = "u_id";
     public static final String TOUR_TITLE_KEY = "t_title";
		public static final String TOUR_DURATION_KEY = "t_duration";
     public static final String TOUR_LANGUAGE_KEY = "t_language";
		public static final String TOUR_LOCATION_KEY = "t_location";
     public static final String TOUR_RATING_KEY = "t_rating";
		public static final String TOUR_DESCRIPTION_KEY = "t_description";
		public static final String TOUR_PHOTOS_KEY = "t_photos";
		public static final String TOUR_COMMENTS_KEY = "t_comments";

		public static final String SLOT_ID_KEY = "s_id";
		public static final String SLOT_GUIDE_ID_KEY = "u_id";
		public static final String SLOT_TOUR_ID_KEY = "t_id";
		public static final String SLOT_DATE_KEY = "s_date";
		public static final String SLOT_TIME_KEY = "s_time";
		public static final String SLOT_CURRENT_CAPACITY_KEY = "s_capacity";
		public static final String SLOT_TOTAL_CAPACITY_KEY = "s_total_capacity";
		public static final String SLOTS_ACTIVE_KEY = "s_active";

		public static final String RESERVATION_USER_ID_KEY = "u_id";
		public static final String RESERVATION_SLOT_ID_KEY = "s_id";
		public static final String RESERVATION_OCCUPIED_KEY = "r_occupied";
		public static final String RESERVATION_ACTIVE_KEY = "r_active";
	}

}
