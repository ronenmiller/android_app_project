package tours_app_server;

import java.io.BufferedReader;
import java.io.IOException;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


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
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Instanciate Gson
		//Gson gson = new Gson();
		
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
		JDBCServer.parseJson(jsonStream);
		response.setDateHeader("asdasad", 10);
	}

}
