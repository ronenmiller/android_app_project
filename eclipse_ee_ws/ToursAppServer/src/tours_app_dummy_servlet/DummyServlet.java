package tours_app_dummy_servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;

import tours_app_server.Message;
import tours_app_server.ResultSetConverter;
import tours_app_server.JDBCServer;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;


/**
 * Servlet implementation class DummyServlet
 */
@WebServlet(name = "dummy_slet", urlPatterns = { "/dummy_slet" })
public class DummyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DummyServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

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
		System.out.println("Server received request JsonStr: " + requestJsonStr);
	}

}
