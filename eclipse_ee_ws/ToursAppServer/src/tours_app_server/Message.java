package tours_app_server;

// Basic container class for server-client messages-based communication.
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
		public static final int ADD_USER = 0;
		public static final int UPDATE_USER = 1;
		public static final int REMOVE_USER = 2;
		public static final int ADD_TOUR = 3;
		public static final int UPDATE_TOUR = 4;
		public static final int REMOVE_TOUR = 5;
		public static final int ADD_SLOT = 6;
		public static final int REMOVE_SLOT = 7;
		public static final int GET_CITY_ID = 8;
		public static final int VALIDATE_UNIQUE_USERNAME = 9;
        public static final int VALIDATE_UNIQUE_EMAIL = 10;
		public static final int FIND_TOURS_BY_CITY_NAME = 11;
		public static final int FIND_SLOTS_BY_CITY_NAME = 12;
		public static final int VALIDATE_CREDENTIALS = 13;
	}
	
	public class MessageKeys {
		public static final String IS_MODIFIED = "IsModified";
		public static final String IS_EXISTS = "IsExists";
		
		public static final String LOCATION_CITY_ID_KEY = "CityID";
		public static final String LOCATION_CITY_NAME_KEY = "CityName";
		public static final String LOCATION_STATE_ID_KEY = "StateID";
		public static final String LOCATION_STATE_NAME_KEY = "StateName";
		public static final String LOCATION_COUNTRY_ID_KEY = "CountryID";
		public static final String LOCATION_COUNTRY_NAME_KEY = "CountryName";
		
		public static final String USER_ID_KEY = "UserID";
		public static final String USER_NAME_KEY = "UserName";
		public static final String USER_PASSWORD_KEY = "UserPass";
		public static final String USER_EMAIL_KEY = "UserEmail";
		public static final String USER_PHONE_KEY = "UserPhone";
		public static final String USER_TYPE_KEY = "UserType";
		
		public static final String TOURS_ID_KEY = "TourID";
		public static final String TOURS_DURATION_KEY = "ToursDuration";
		public static final String TOURS_LOCATION_KEY = "ToursLocation";
		public static final String TOURS_DESCRIPTION_KEY = "ToursDescription";
		public static final String TOURS_PHOTOS_KEY = "ToursPhotos";
		public static final String TOURS_LANGUAGE_KEY = "ToursLanguage";
		public static final String TOURS_BY_CITY_KEY = "ToursByCity";
		
		public static final String SLOT_DATE_KEY = "SlotDate";
		public static final String SLOT_TIME_KEY = "SlotTime";
		public static final String SLOT_NUM_VACANT_KEY = "SlotVacant";
		public static final String SLOTS_BY_CITY_KEY = "SlotsByCity";
	}

}
