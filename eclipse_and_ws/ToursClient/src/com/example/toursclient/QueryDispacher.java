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

import com.google.gson.Gson;

public class QueryDispacher {
	
	QueryDispacher(){
	}
	static Gson gson = new Gson();
	static boolean dispatchQuery(Query q){
		System.out.println("Successfully dispatching");
        try {
        	HttpClient httpclient = new DefaultHttpClient();
        	
        	// Instanciate Gson 
        	
        	// JSON string
        	String json;
        	
        	// http post 
        	// my ip: 85.250.73.34
        	HttpPost httpPost = new HttpPost("http://10.0.0.1:8080/ToursAppServer/tours_slet");
            
            // convert list to JSON format string
            json = gson.toJson(q);
            
            httpPost.setEntity(new StringEntity(json));
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            HttpResponse response = httpclient.execute(httpPost);
            BufferedReader result = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            System.out.println(result.readLine()+ "  dfdsfsd");
            System.out.println("Successfully Executed");
            
        }
        catch (Exception e){
        	System.out.println("Caught Ex: "+e.toString());
        	return false;
        }
        /* catch (ClientProtocolException e) {
            // TODO print error
        	return 1;
        } catch (IOException e) {
            // TODO print error
        	return 1;
        }
        */
        return true;
	}
}
