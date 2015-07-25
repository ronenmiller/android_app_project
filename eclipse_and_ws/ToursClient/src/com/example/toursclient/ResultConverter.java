package com.example.toursclient;

import org.json.JSONException;
import org.json.JSONObject;

public class ResultConverter {
	
	/**
	 * 
	 * @param m - Message object
	 * @return True - if message is of MessageTypes type and result is true, False otherwise. 
	 */
	public static Boolean getBoolResult(Message m, int type){
		if (m.getMessageID() != type){
			System.out.println("ResultConverter:getBoolResult> Error not a boolean result message!");
			return false;
		}
		else
		{
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(m.getMessageJson());
				Boolean b = jsonObject.getBoolean("IsModified");
				return b;
			} catch (JSONException e) {
				System.out.println("Signup> Error - Could not parse Boolean result JSON!");
				e.printStackTrace();
				return false;
			}
		}
	}
	
}
