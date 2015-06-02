package tours_app_client;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;



public class ClientExample {

    public static void main(String[] args) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
        	// Instanciate Gson 
        	Gson gson = new Gson();
        	// JSON string
        	String json;
        	
        	// http post 
            HttpPost httpPost = new HttpPost("http://localhost:8080/ToursAppServer/tours_slet");
    		
            // create Name<->Value pairs list 
            List <NameValuePair> nvps = new ArrayList <NameValuePair>();
            nvps.add(new BasicNameValuePair("reqType", "add"));
            nvps.add(new BasicNameValuePair("uname", "HollyShit"));
            nvps.add(new BasicNameValuePair("email", "holyshit@gmail.com"));
            nvps.add(new BasicNameValuePair("phnum", "0545325689"));
            nvps.add(new BasicNameValuePair("password", "secret2013"));
            
            // convert list to JSON format string
            json = gson.toJson(nvps);
            
            System.out.println(json);
            /*
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
            CloseableHttpResponse response2 = httpclient.execute(httpPost);

            try {
            	//System.out.println(response2.getStatusLine());
                HttpEntity entity2 = response2.getEntity();
                // do something useful with the response body
                // and ensure it is fully consumed
                EntityUtils.consume(entity2);
                
            } finally {
                //response2.close();
            }
            */
        } finally {
            // httpclient.close();
        }
    }

}