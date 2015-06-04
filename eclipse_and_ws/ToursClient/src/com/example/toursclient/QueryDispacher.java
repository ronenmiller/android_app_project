package com.example.toursclient;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class QueryDispacher {
	
	QueryDispacher(){
	}
	static Integer dispatchQuery(Query q){
        try {
        	HttpClient httpclient = new DefaultHttpClient();
        	
        	// Instanciate Gson 
        	//Gson gson = new Gson();
        	GsonBuilder gsonBuilder = new GsonBuilder();
        	Gson gson = gsonBuilder.create();
        	// JSON string
        	String json;
        	/*
        	// http post 
        	HttpPost httpPost = new HttpPost("http://localhost:8080/ToursAppServer/tours_slet");
            
            // convert list to JSON format string
            json = gson.toJson(q);
            
            httpPost.setEntity(new StringEntity(json));
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            HttpResponse response = httpclient.execute(httpPost);
            */
        }
        catch (Exception e){}
        /* catch (ClientProtocolException e) {
            // TODO print error
        	return 1;
        } catch (IOException e) {
            // TODO print error
        	return 1;
        }
        */
        return 0;
	}
}
