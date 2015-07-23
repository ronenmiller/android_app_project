package tours_app_server;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;


//

/**
 * Servlet implementation class ToursServerServlet
 */
@WebServlet("/tours_slet")
public class ToursServerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	// JDBC server instance
	//private JDBCServer server = new JDBCServer();
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ToursServerServlet() {
        super();
        //JDBCServer.initConnection(); // establish a connection to the database
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		StringBuffer jsonBuffer = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
		    jsonBuffer.append(line);
		} 
		catch (Exception e) { 
			System.err.println("Parsing error: " + e);
		}
		System.out.println(jsonBuffer.toString());
		String requestJsonStr = jsonBuffer.toString();
		
		
		String responseJsonStr = JDBCServer.fetchResponse(requestJsonStr);
		System.out.println("responseJsonStr: " + responseJsonStr);
		
		// Instantiate Gson
		Gson gson = new Gson();
		Message responseMessage = gson.fromJson(responseJsonStr, Message.class);
		if (responseMessage != null) {
			System.out.println(responseMessage.getMessageID());
			try {
				JSONObject testJson = new JSONObject(responseMessage.getMessageJson());
				System.out.println("Value: " + testJson.get(Message.MessageKeys.LOCATION_CITY_ID_KEY));
				//System.out.println("Value: " + testJson.get(Message.MessageKeys.IS_MODIFIED));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
        
        //response.getWriter().write(responseJsonStr);
		response.setStatus(HttpServletResponse.SC_OK);
		//response.getWriter().write("Response from servlet!");
		response.getWriter().flush();
		response.getWriter().close();
	}

}
