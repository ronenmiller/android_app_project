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
//		public static final int UPDATE_USER = 1;
//		public static final int REMOVE_USER = 2;
//		public static final int ADD_TOUR = 3;
//		public static final int UPDATE_TOUR = 4;
//		public static final int REMOVE_TOUR = 5;
//		public static final int ADD_SLOT = 6;
//		public static final int REMOVE_SLOT = 7;
//		public static final int GET_CITY_ID = 8;

	}
	
	public class MessageKeys {
		public static final String IS_MODIFIED = "IsModified";
		public static final String IS_EXISTS = "IsExists";
     public static final String IS_UNIQUE = "IsUnique";
		
		public static final String LOCATION_OSM_ID_KEY = "CityID";
		
		public static final String USER_ID_KEY = "UserID";
		public static final String USER_NAME_KEY = "UserName";
		public static final String USER_PASSWORD_KEY = "UserPass";
		public static final String USER_EMAIL_KEY = "UserEmail";
		public static final String USER_TYPE_KEY = "UserType";
		
		public static final String TOURS_ID_KEY = "TourID";
     public static final String TOUR_TITLE_KEY = "TourTitle";
		public static final String TOUR_DURATION_KEY = "TourDuration";
     public static final String TOUR_LANGUAGE_KEY = "TourLanguage";
		public static final String TOUR_LOCATION_KEY = "TourLocation";
     public static final String TOUR_RATING_KEY = "TourRating";
		public static final String TOUR_DESCRIPTION_KEY = "TourDescription";
		public static final String TOUR_PHOTOS_KEY = "TourPhotos";
		public static final String TOUR_COMMENTS_KEY = "TourComments";
		
		public static final String SLOT_DATE_KEY = "SlotDate";
		public static final String SLOT_TIME_KEY = "SlotTime";
		public static final String SLOT_NUM_VACANT_KEY = "SlotVacant";
		public static final String SLOTS_BY_CITY_KEY = "SlotsByCity";
	}

}
