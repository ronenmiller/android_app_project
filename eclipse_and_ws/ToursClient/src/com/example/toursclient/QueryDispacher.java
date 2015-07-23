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
	// Instanciate Gson 
	static Gson gson = new Gson();
	static boolean dispatchQuery(QueryContainer q){
		System.out.println("Successfully dispatching");
		HttpClient httpclient = new DefaultHttpClient();
        try {
        	// JSON string
        	String json;
        	
        	// http post 
        	HttpPost httpPost = new HttpPost("http://10.0.0.3:8080/ToursAppServer/tours_slet");
            
            // convert list to JSON format string
            json = gson.toJson(q);
            System.out.println(json);
            httpPost.setEntity(new StringEntity(json));
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            HttpResponse response = httpclient.execute(httpPost);
            BufferedReader result = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String resJson = result.readLine();
            ResponseContainer resC = gson.fromJson(resJson, ResponseContainer.class);
            BooleanResponse bRes = gson.fromJson(resC.getResponse(), BooleanResponse.class);
            System.out.println("response:" + Boolean.toString(bRes.getResponse()));
            System.out.println("Successfully Executed");
        }
        /*
        catch (ClientProtocolException e) {
        	System.out.println("Caught ex1: "+ e.toString());
        	return false;
        } */catch (IOException e) {
        	System.out.println("Caught ex2: "+ e.toString());
        	return false;
        }
        catch (Exception e){
        	System.out.println("Caught Ex3: "+e.toString());
        	return false;
		}
        return true;
	}
}
