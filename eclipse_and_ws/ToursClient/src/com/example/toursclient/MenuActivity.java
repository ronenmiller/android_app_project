package com.example.toursclient;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MenuActivity extends ListActivity{

	String classes[] = {"Overview","SearchTourActivity","HomeScreen"};
	String labels[] = {"Overview","Search For Tour","Logout"};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setListAdapter(new ArrayAdapter<String>(MenuActivity.this, android.R.layout.simple_list_item_1, labels));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		
		String className = classes[position];
		try {
			Class c = Class.forName("com.example.toursclient." + className);
			Intent intent = new Intent(MenuActivity.this,c);
			startActivity(intent);
		} catch (ClassNotFoundException e){
			e.printStackTrace();
		}		
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		finish();
	}

	
}
