package com.example.toursclient;

public class QueryContainer {
	
	QueryContainer(String reqType)
	{
		_reqType = reqType;
	}
	
	private String _reqType; 
	private String _jsonQuery;
	
	public String getType(){
		return _reqType;
	}
	
	public void setQuery(String jsonQuery){
		_jsonQuery = jsonQuery;
	}
	
	public String getQuery(){
		return _jsonQuery;
	}
}
