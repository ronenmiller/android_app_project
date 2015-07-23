package tours_app_client;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import tours_app_server.Message;

import com.google.gson.Gson;



public class ClientExample {

    public static void main(String[] args) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
        	// Instanciate Gson 
        	Gson gson = new Gson();
        	// JSON string
        	String Address = "http://localhost:8080/ToursAppServer/tours_slet";
        	//String Address = "http://10.0.0.1:8080/ToursAppServer/tours_slet";
        	// http post 
            HttpPost httpPost = new HttpPost(Address);
<<<<<<< HEAD

    		Map<String, String> mapa = new HashMap<String, String>();
    		mapa.put(Message.MessageKeys.USER_NAME_KEY, "Moti_Ban");
    		mapa.put(Message.MessageKeys.USER_PASSWORD_KEY, "secret2013");
    		mapa.put(Message.MessageKeys.USER_EMAIL_KEY, "bannan@gmail.com");
    		mapa.put(Message.MessageKeys.USER_PHONE_KEY, "0545555689");
    		mapa.put(Message.MessageKeys.USER_TYPE_KEY, "false");
    		JSONObject jsonObjectM = new JSONObject(mapa);
    		Message messageM = new Message(Message.MessageTypes.ADD_USER, jsonObjectM.toString());
    		String messageJsonM = gson.toJson(messageM);
=======
            /*
    		 * Create addUser queryContainer:
    		 */
    		// Container for addUser
    		QueryContainer qC = new QueryContainer("addUser");
    		// Query to turn into JSON
    		AddUserQuery q = new AddUserQuery("Moti_Ban","secret2013","bannan@gmail.com","0545555689",false);
            
            
            /*
             * pack Query into JSON format String and put into container
             * then pack entire container into JSON
             */
            Gson g = new Gson();
            String j1 = g.toJson(q);
            System.out.println("Packed JSON QueryAddUser String is:\n "+ j1);
            qC.setQuery(j1);
            String j2 = g.toJson(qC);//, QueryContainer.class);
            System.out.println("\nPacked JSON QueryContainer String is:\n "+ j2);
            
            
            
            /*
    		 * Create getCityid queryContainer:
    		 */
    		// Container for addUser
    		QueryContainer qC2 = new QueryContainer("getCityId");
    		// Query to turn into JSON
    		GeoQuery q2 = new GeoQuery("Los Angeles","California","United States");
    		
            
            
            /*
             * pack Query into JSON format String and put into container
             * then pack entire container into JSON
             */
            String j_1 = g.toJson(q2);
            System.out.println("1-Packed JSON QueryAddUser String is:\n "+ j_1);
            qC2.setQuery(j_1);
            String j_2 = g.toJson(qC2);//, QueryContainer.class);
            System.out.println("\n2-Packed JSON QueryContainer String is:\n "+ j_2);
>>>>>>> origin/master
            
    		Map<String, String> map = new HashMap<String, String>();
    		map.put(Message.MessageKeys.LOCATION_CITY_NAME_KEY, "Los Angeles");
    		map.put(Message.MessageKeys.LOCATION_STATE_NAME_KEY, "California");
    		map.put(Message.MessageKeys.LOCATION_COUNTRY_NAME_KEY, "United States");
    		JSONObject jsonObject = new JSONObject(map);
    		Message message = new Message(Message.MessageTypes.GET_CITY_ID, jsonObject.toString());
    		String messageJson = gson.toJson(message);
       
            httpPost.setEntity(new StringEntity(messageJsonM));
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