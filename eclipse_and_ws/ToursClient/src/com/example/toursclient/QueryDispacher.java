package com.example.toursclient;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.Context;

import com.google.gson.Gson;

/**
 * Class QueryDispacher
 * In charge of sending HTTP requests to DB server
 * and returning the result.
 */
public class QueryDispacher {
	static Context c;
	
	QueryDispacher(){
		
	}
	// Instantiate Gson member for conversion 
	static Gson gson = new Gson();
	
	static String dispatchQuery(int type, JSONObject jsonObjectM, Context context){
		c = context;
		try {
/*
			String urlStr = "https://10.0.0.3:443/ToursAppServer/tours_slet";
			URL url = new URL(urlStr);
		    HttpsURLConnection urlConnection  = (HttpsURLConnection) url.openConnection();
		    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
		    
		 // Instantiate the custom HttpClient
		    DefaultHttpClient client = new MyHttpClient();
		    client.setCo
		    HttpPost httpPost = new HttpPost("https://10.0.0.3:443/ToursAppServer/tours_slet");
		    // Execute the GET call and obtain the response
		    HttpResponse getResponse = client.execute(httpPost);
		    HttpEntity responseEntity = getResponse.getEntity();
			/*/
			/*
			//KeyStore keyStore = "C:\Users\RONEN\Desktop\app_project\git\android_app_project\connection\myKeyStore";
		   String algorithm = TrustManagerFactory.getDefaultAlgorithm();
		   TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
		   tmf.init((KeyStore) null);
			System.out.println("Successfully dispatching");
			//HttpClient httpclient = new DefaultHttpClient();
			String urlStr = "https://10.0.0.3:8443/ToursAppServer/tours_slet";
			URL url = new URL(urlStr);
		    HttpsURLConnection httpclient = (HttpsURLConnection) url.openConnection();
		 // set Timeout and method
        	httpclient.setReadTimeout(15000);
        	httpclient.setConnectTimeout(15000);
        	httpclient.setRequestMethod("POST");
        	httpclient.setDoInput(true);
        	httpclient.setDoOutput(true);
		 // Create the SSL connection
		    SSLContext sc;
		    sc = SSLContext.getInstance("TLS");
		    sc.init(null, tmf.getTrustManagers(), null);
		    httpclient.setSSLSocketFactory(sc.getSocketFactory());
		    httpclient.setRequestProperty("Content-Type", "application/json");
		    httpclient.setRequestProperty("Accept", "application/json");
	    */

        
       	 

    	    // Add any data you wish to post here

        	//httpclient.connect();
			HttpClient httpclient = new DefaultHttpClient();
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
            
	        
	        
            /*
             DataOutputStream output = new DataOutputStream(httpclient.getOutputStream());  
             
            output.writeBytes(messageJsonM);
            output.close();
            */
            System.out.println("response:" + resJson);
            System.out.println("Successfully Executed");
            return resJson;
            
            //return "stam";
        }
        catch (ClientProtocolException e) {
        	System.out.println("dispatchQuery> Caught exception ClientProtocolException: "+ e.toString());
        	return "Error";
        } catch (IOException e) {
        	System.out.println("dispatchQuery> Caught exception IOException: "+ e.toString());
        	return "Error";
        }
        catch (Exception e){
        	System.out.println("dispatchQuery> Caught exception: "+e.toString());
        	return "Error";
		}
	}
}
