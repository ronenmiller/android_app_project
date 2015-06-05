package tours_app_client;

public class AddUserQuery extends GenericQuery {
	/* Constructor */
	AddUserQuery( String uname, String pass, String email, String phnum, boolean utype) {
		super(uname, pass);
		_email = email;
		_phnum = phnum;
		_utype = utype;
	}
	
	private String _email;
	private String _phnum;
	private boolean _utype;
	
	public String getEmail(){
		return _email;
	}
	public String getPhnum(){
		return _phnum;
	}
	public boolean getUtype(){
		return _utype;
	}
}
