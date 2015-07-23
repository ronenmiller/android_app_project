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
        JDBCServer.init(); // establish a connection to the database
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
		StringBuffer jb = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
		    jb.append(line);
		} 
		catch (Exception e) { 
			System.out.println("Parsing error");
		}
		System.out.println(jb.toString());
		String jsonStream = jb.toString();
		
		
<<<<<<< HEAD
		String responseJsonStr = JDBCServer.fetchResponse(jsonStream);
		System.out.println("bla: " + responseJsonStr);
		
		// Instantiate Gson
		Gson gson = new Gson();
		Message responseMessage = gson.fromJson(responseJsonStr, Message.class);
		System.out.println(responseMessage.getMessageID());
		try {
			JSONObject testJson = new JSONObject(responseMessage.getMessageJson());
			//System.out.println("Value: " + testJson.get(Message.MessageKeys.LOCATION_CITY_ID_KEY));
			System.out.println("Value: " + testJson.get(Message.MessageKeys.IS_MODIFIED));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
=======
		String reponseStream = JDBCServer.fetchResponse(jsonStream);
		ResponseContainer responseContainer =  gson.fromJson(reponseStream, ResponseContainer.class);
        String reqType = responseContainer.getType();
        response.getWriter().write(reponseStream);
        System.out.println(reqType);
        System.out.println(responseContainer.getResponse());
>>>>>>> origin/master
        
        //response.getWriter().write(responseJsonStr);
		response.setStatus(HttpServletResponse.SC_OK);
		//response.getWriter().write("Response from servlet!");
		response.getWriter().flush();
		response.getWriter().close();
	}

}
