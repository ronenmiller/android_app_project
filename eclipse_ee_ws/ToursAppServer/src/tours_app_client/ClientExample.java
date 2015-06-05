package tours_app_client;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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
            HttpPost httpPost = new HttpPost("http://10.0.0.1:8080/ToursAppServer/tours_slet");
            Query q = new Query();
            q.reqType = "addUser";
            q.uname = "Moti_Ban";
            q.email = "bannan@gmail.com";
            q.phnum = "0545555689";
            q.password = "secret2013";
            
            // convert list to JSON format string
            json = gson.toJson(q);
            
            //System.out.println(json);
       
            httpPost.setEntity(new StringEntity(json));
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            CloseableHttpResponse response = httpclient.execute(httpPost);

            try {
            	//System.out.println(response.getStatusLine());
                HttpEntity entity2 = response.getEntity();
                // do something useful with the response body
                // and ensure it is fully consumed
                EntityUtils.consume(entity2);
                
            } finally {
                response.close();
            }
        } finally {
        	httpclient.close();
        }
    }

}