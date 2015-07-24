package com.example.toursclient;

import org.json.JSONArray;

public class QueryResponse {
	
	private final JSONArray queryResults;

	public QueryResponse(JSONArray queryResults) {
		this.queryResults = queryResults;
	}
	
	public JSONArray getQueryResult() {
		return queryResults;
	}

}
