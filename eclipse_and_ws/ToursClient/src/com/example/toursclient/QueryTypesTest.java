package com.example.toursclient;

import com.google.gson.Gson;

/**********************************************************************************
 * I created a test to check the ability to use different types of queries:
 *  - currently created a classes for QueryContainer, GenericQuery and AddUserQuery.
 *  - see below the use for AddUserQuery which is packed into JSON string and put
 *  	 inside a  QueryContainer.
 *  - Notice that for rmUser query we can use the GenericQuery class as it has 
 *  	the needed  (username,password).
 **********************************************************************************/
public class QueryTypesTest {

	public static void main(String[] args) {
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
         * Extract JSON
         */
        QueryContainer qCExtract =  g.fromJson(j2, QueryContainer.class);
        String reqType = qCExtract.getType();
        
        if (reqType.equals("addUser")){
        	AddUserQuery qExtract = g.fromJson(qCExtract.getQuery(),AddUserQuery.class);  
        	System.out.println("\nExtracted class for request type " + reqType + " is:\n "+ 
        			qExtract.getUname() +" "+ qExtract.getPass() +" "+ qExtract.getEmail() +" "+ 
        			qExtract.getPhnum() +" "+ qExtract.getUtype());
        }
	}

}
