package com.example.toursclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class HomeScreen extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        
        
        /* declare homescreen objects*/ 
        Button loginButton = (Button) findViewById(R.id.loginButton);
        Button signUpButton = (Button) findViewById(R.id.signupButton);
        
        final EditText usernameInput = (EditText)findViewById(R.id.usernameInput);
        final EditText passwordInput = (EditText)findViewById(R.id.passwordInput);
        
        final Intent goToSignUpScreen = new Intent(this,SignUpActivity.class);
        final Intent goToMenuScreen = new Intent(this,MenuActivity.class);
        
        /* LoginButton onClick listeners */
        //TODO : login
        loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String username = usernameInput.getText().toString();
				String password = passwordInput.getText().toString();
				String output = "User Name: "+username+" Password: "+password;
				Toast.makeText(getApplicationContext(), output, Toast.LENGTH_LONG).show();
				startActivity(goToSignUpScreen);
			}
		});
        
        /* SignUpButton onClick listener */
        signUpButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(goToSignUpScreen);
						   
			}
		});
        
    }
    /*
    @Override
    public void onPause(){
    	super.onPause();
    	finish();
    }
    */
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
