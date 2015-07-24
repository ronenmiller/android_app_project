package com.example.toursclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Email extends Activity implements View.OnClickListener {

	EditText personsEmail, intro, personsName, stupidThings, hatefulAction,
			outro;
	String emailAdd, beginning, name, stupidAction, hatefulAct, out;
	Button sendEmail;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_email);		
		initializeVars();
		sendEmail.setOnClickListener(this);
		
	}

	private void initializeVars() {
		// TODO Auto-generated method stub
		personsEmail = (EditText) findViewById(R.id.etEmails);
		personsEmail.setText("ronen.miller1@gmail.com");
		personsName = (EditText) findViewById(R.id.etName);
		personsName.setText("Ronen");
		intro = (EditText) findViewById(R.id.etIntro);
		intro.setText("I LOVE U");
		stupidThings = (EditText) findViewById(R.id.etThings);
		stupidThings.setText("are angry!");
		hatefulAction = (EditText) findViewById(R.id.etAction);
		hatefulAction.setText("pleased");
		outro = (EditText) findViewById(R.id.etOutro);
		outro.setText("remember who is the best chul!");
		sendEmail = (Button) findViewById(R.id.bSentEmail);
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub

		convertET2String();
		String emailaddress[] = { emailAdd };
		String message = "Well hello "
				+ name
				+ " I just wanted to say "
				+ beginning
				+ ".  Not only that but I hate when you "
				+ stupidAction
				+ ", that just really makes me crazy.  I just want to make you "
				+ hatefulAct
				+ ".  Welp, thats all I wanted to chit-chatter about, oh and"
				+ out
				+ "."
				+ '\n' + "PS. I think I love you...   :(";
		System.out.println(message);
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,emailaddress);
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,"Love Mail");
		emailIntent.setType("plain/text");
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,message);
		startActivity(emailIntent);
	}

	private void convertET2String() {
		// TODO Auto-generated method stub
		emailAdd = personsEmail.getText().toString();
		beginning = intro.getText().toString();
		name = personsName.getText().toString();
		stupidAction = stupidThings.getText().toString();
		hatefulAct = hatefulAction.getText().toString();
		out = outro.getText().toString();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		finish();
	}

}