package com.example.toursclient;

public class GenericQuery {
	GenericQuery(String username, String password)
	{
		_username = username;
		_password = password;
	}
	private String _username;
	private String _password;
	
	public String getUname(){
		return _username;
	}
	public String getPass(){
		return _password;
	}
}
