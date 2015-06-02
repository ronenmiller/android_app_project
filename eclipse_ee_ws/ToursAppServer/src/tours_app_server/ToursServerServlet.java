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
	/*
		try {
			List <NameValuePair> nvps = gson.fromJson(jb.toString(),List.class);
		} catch (Exception e) {
		  // crash and burn
			throw new IOException("Error parsing JSON request string");
		}
		*/
		/*
		
		String reqType = request.getParameter("reqType");
		String uname = request.getParameter("uname");
		String password = request.getParameter("password");
		String email = request.getParameter("email");
		String phnum = request.getParameter("phnum");
		
		String cityname = request.getParameter("city");
		String regionname = request.getParameter("region");
		String countryname = request.getParameter("country");
		boolean utype = request.getParameter("utype") != null ? true:false;
		System.out.println("Servlet doPost> Passing following values to server"
				+ "\n"+reqType+ "\n"+uname+ "\n"+ password+ "\n" + email+ "\n" + phnum+ "\n" + utype+ "\n");
		if (reqType.equals("add"))
			JDBCServer.addUser(uname, password, email, phnum, utype);
		else if (reqType.equals("rm"))
			JDBCServer.rmUser(uname, password);
		else if (reqType.equals("find_cityid"))
			JDBCServer.getCityIdByName(cityname, regionname, countryname);
		else if (reqType.equals("validate_username"))
			JDBCServer.validateUniqueUsername(uname);
			*/
	}

}
