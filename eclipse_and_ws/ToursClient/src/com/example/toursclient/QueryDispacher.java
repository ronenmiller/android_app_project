package com.example.toursclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.google.gson.Gson;

/**
 * Class QueryDispacher
 * In charge of sending HTTP requests to DB server
 * and returning the result.
 */
public class QueryDispacher {
	
	QueryDispacher(){
	}
	// Instantiate Gson member for conversion 
	static Gson gson = new Gson();
	
	static String dispatchQuery(int type, JSONObject jsonObjectM){
		System.out.println("Successfully dispatching");
		HttpClient httpclient = new DefaultHttpClient();
        try {
        	 /*********************************
             *  convert JSONObject to Message
             ********************************/
            Message messageM = new Message(type, jsonObjectM.toString());
    		String messageJsonM = gson.toJson(messageM);
    		
    		/*********************************
             *  Setup httpPost
             ********************************/ 
        	HttpPost httpPost = new HttpPost("http://10.0.0.3:8080/ToursAppServer/tours_slet");
	    	httpPost.setEntity(new StringEntity(messageJsonM));
	        httpPost.setHeader("Accept", "application/json");
	        httpPost.setHeader("Content-type", "application/json");
           
	        /*********************************
             *  send httpPost request and
             *  get response
             ********************************/
            HttpResponse response = httpclient.execute(httpPost);
            BufferedReader result = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String resJson = result.readLine();
            
            System.out.println("response:" + resJson);
            System.out.println("Successfully Executed");
            return resJson;
        }
        
        catch (ClientProtocolException e) {
        	System.out.println("Caught ex1: "+ e.toString());
        	return "Error";
        } catch (IOException e) {
        	System.out.println("Caught ex2: "+ e.toString());
        	return "Error";
        }
        catch (Exception e){
        	System.out.println("Caught Ex3: "+e.toString());
        	return "Error";
		}
	}
}
