package com.example.toursclient;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
public class SignUpActivity extends ActionBarActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_up);
		
		/* declare homescreen objects*/ 
        Button backButton = (Button) findViewById(R.id.backFromSUButton);
        Button submitSignUpBotton = (Button) findViewById(R.id.submitSignUpButton);
        
        final EditText signUpEmailInput = (EditText)findViewById(R.id.signUpEmailInput);
        final EditText signUpUNameInput = (EditText)findViewById(R.id.signUpUNameInput);
        final EditText signUpCountryInput = (EditText)findViewById(R.id.signUpCountryInput);
        final EditText signUpStateInput = (EditText)findViewById(R.id.signUpStateInput);
        final EditText signUpCityInput = (EditText)findViewById(R.id.signUpCityInput);
        final EditText signUpPhoneInput = (EditText)findViewById(R.id.signUpPhoneInput);
        final EditText signUpPassInput = (EditText)findViewById(R.id.signUpPassInput);
        final EditText signUpREPassInput = (EditText)findViewById(R.id.signUpREPassInput);
        
        //defaults
        signUpEmailInput.setText("ale@gmail.com");
        signUpUNameInput.setText("alehandro1");
        signUpPhoneInput.setText("0542442688");
        signUpPassInput.setText("abc123456");
        signUpREPassInput.setText("abc123456");
		
        
        
        final Intent goToHomeScreen = new Intent(this,HomeScreen.class);
        
        /* LoginButton onClick listeners */
        submitSignUpBotton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				System.out.println("Successfully running1 ");
				new Thread(new Runnable() {
				    public void run() {
				    	System.out.println("Successfully running");
						Boolean checkStatus = true;
						Query q = new Query();
			            q.reqType = "addUser";
			            String email = signUpEmailInput.getText().toString();
						q.uname = signUpUNameInput.getText().toString();
						q.country = signUpCountryInput.getText().toString();
						q.state = signUpStateInput.getText().toString();
						q.city = signUpCityInput.getText().toString();
						q.phnum = signUpPhoneInput.getText().toString();
						q.email = email;
						
						// check parameters
						String pass = signUpPassInput.getText().toString();
						String rEPass = signUpREPassInput.getText().toString();
						if (pass.isEmpty() || rEPass.isEmpty()){
							Toast.makeText(getApplicationContext(), "Error - Please fill in password!", 
									   Toast.LENGTH_LONG).show();
							checkStatus = false;
							return;
						}
						if (pass.equals(rEPass)){
							q.password = pass;
						}
						else{
							Toast.makeText(getApplicationContext(), "Mismatch in password inputs!", 
									   Toast.LENGTH_LONG).show();
							checkStatus = false;
							return;
						}
						
						// if all checks are OK dispatch query
						if(checkStatus){
							boolean status = QueryDispacher.dispatchQuery(q);
						}
						
					}
				}).start();
			}
		});
        
        /* SignUpButton onClick listener */
        backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(goToHomeScreen);		   
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sign_up, menu);
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
