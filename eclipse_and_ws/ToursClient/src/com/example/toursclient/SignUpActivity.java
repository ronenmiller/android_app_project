package com.example.toursclient;

import java.util.HashMap;
import java.util.Map;

import android.support.v7.app.ActionBarActivity;
//import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
//import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.toursclient.Message;

import org.json.JSONException;
//import org.apache.commons.validator.routines.EmailValidator;
import org.json.JSONObject;

/**
 * Class for sign up activity
 */
public class SignUpActivity extends ActionBarActivity {
	//EmailValidator emailValidator = EmailValidator.getInstance();
	//private ProgressDialog executingDialog;
	//Handler h;
	public final static boolean isValidEmail(String target) {
	    if (target == null) {
	        return false;
	    } else {
	        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
	    }
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_up);
		
		/* declare homescreen objects*/ 
        Button backButton = (Button) findViewById(R.id.backFromSUButton);
        Button submitSignUpBotton = (Button) findViewById(R.id.submitSignUpButton);
        
        final EditText signUpEmailInput = (EditText)findViewById(R.id.signUpEmailInput);
        final EditText signUpUNameInput = (EditText)findViewById(R.id.signUpUNameInput);
        final EditText signUpPhoneInput = (EditText)findViewById(R.id.signUpPhoneInput);
        final EditText signUpPassInput = (EditText)findViewById(R.id.signUpPassInput);
        final EditText signUpREPassInput = (EditText)findViewById(R.id.signUpREPassInput);
        
        //TODO: remove defaults for signup once debug is finished
        signUpEmailInput.setText("ale@gmail.com");
        signUpUNameInput.setText("alehandro1");
        signUpPhoneInput.setText("0542442688");
        signUpPassInput.setText("abc123456");
        signUpREPassInput.setText("abc123456");
		
        
        // Back to home screen intent
        final Intent goToHomeScreen = new Intent(this,HomeScreen.class);
        
        // LoginButton onClick listener
        submitSignUpBotton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//executingDialog = ProgressDialog.show(SignUpActivity.this, "", getString(R.string.message_executingSignup), true);
				new Thread(new Runnable() {
				    public void run() {
				    	System.out.println("Successfully running");
				    	
						Boolean checkStatus = true;
						
			            String email = signUpEmailInput.getText().toString();
						String uname = signUpUNameInput.getText().toString();
						String phnum = signUpPhoneInput.getText().toString();
						String pass = signUpPassInput.getText().toString();
						String rEPass = signUpREPassInput.getText().toString();
						Boolean utype = false;
						
						/******************************
						 *  check input correctness
						 ******************************/
						// validate email
						if (!isValidEmail(email)){
							Toast.makeText(getApplicationContext(), "Error- Email is not valid!", 
									   Toast.LENGTH_LONG).show();
							checkStatus = false;
							return;
						}
						// validate password
						if (pass.isEmpty() || rEPass.isEmpty()){
							Toast.makeText(getApplicationContext(), "Error - Please fill in password!", 
									   Toast.LENGTH_LONG).show();
							checkStatus = false;
							return;
						}
						if (!(pass.equals(rEPass))){
							Toast.makeText(getApplicationContext(), "Mismatch in password inputs!", 
									   Toast.LENGTH_LONG).show();
							checkStatus = false;
							return;
						}
						//TODO: add phone validation
						//TODO: add user name validation
						
						
						/******************************
						 *  pack input into JSON
						 ******************************/
						Map<String, String> addUserMap = new HashMap<String, String>();
						
						addUserMap.put(Message.MessageKeys.USER_NAME_KEY, uname);
						addUserMap.put(Message.MessageKeys.USER_PASSWORD_KEY, pass);
						addUserMap.put(Message.MessageKeys.USER_EMAIL_KEY, email);
						addUserMap.put(Message.MessageKeys.USER_PHONE_KEY, phnum);
						addUserMap.put(Message.MessageKeys.USER_TYPE_KEY, utype.toString());
			    		JSONObject jsonObjectM = new JSONObject(addUserMap);
			    		
						/********************************************
						 *  if all checks are OK dispatch query
						 *******************************************/
						if(checkStatus){
							String res = QueryDispacher.dispatchQuery(Message.MessageTypes.ADD_USER,jsonObjectM,getApplicationContext());
							Message resM = QueryDispacher.gson.fromJson(res, Message.class);
							System.out.println(resM);
							/*Boolean resB = ResultConverter.getBoolResult(resM, Message.MessageTypes.ADD_USER);
							//executingDialog.dismiss();
							if (resB){
								//h.post(loginDone);
								//Toast.makeText(getApplicationContext(), "Sign up successful!", Toast.LENGTH_LONG).show();
							}
							else {
								//h.post(loginFailed);
								//Toast.makeText(getApplicationContext(), "Sign up failed!", Toast.LENGTH_LONG).show();
							}
							*/
							
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
/*
	private final Runnable loginDone = new Runnable() {
	    public void run() {
	        executingDialog.dismiss();
	        setResult(RESULT_OK);
	        finish();
	    }
	};

	private final Runnable loginFailed = new Runnable() {
	    public void run() {
	        executingDialog.dismiss();
	        setResult(RESULT_CANCELED);
	    }
	};
*/
	
	 
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
