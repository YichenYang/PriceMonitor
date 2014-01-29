package com.example.pricer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;

public class Register extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_page);
		
		Properties properties = new Properties();
		try {
			properties.load(getClass().getResourceAsStream( "AwsCredentials.properties" ));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String accessKey = properties.getProperty( "accessKey" );
		String secretKey = properties.getProperty( "secretKey" );
		
		BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey); 
		final AmazonSimpleDB sdb = new AmazonSimpleDBClient(awsCredentials);
		
		Region usWest2 = Region.getRegion(Regions.US_WEST_2);
		sdb.setRegion(usWest2);
		
		final String Domain = "UserAccount";
		sdb.createDomain(new CreateDomainRequest(Domain));

		
		Button loginButton = (Button) findViewById(R.id.btnRegister);
		loginButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				EditText nameEditText = (EditText) findViewById(R.id.reg_fullname);
				String usrNameString = nameEditText.getText().toString();
				
				EditText emailEditText = (EditText) findViewById(R.id.reg_email);
				String usrEmailString = emailEditText.getText().toString();
				
				EditText passwordEditText = (EditText) findViewById(R.id.reg_password);
				String usrPasswordString = passwordEditText.getText().toString();
				
				if (usrEmailString.length() == 0 || usrNameString.length() == 0 || usrPasswordString.length() == 0){
					MyAlert();
				} else {
					ReplaceableItem usr = new ReplaceableItem(usrEmailString).withAttributes(
			                new ReplaceableAttribute("Password", Integer.toString(usrPasswordString.hashCode()), true),
			                new ReplaceableAttribute("FullName", usrNameString, true));
					List<ReplaceableItem> data = new ArrayList<ReplaceableItem>();
					data.add(usr);
					sdb.batchPutAttributes(new BatchPutAttributesRequest(Domain, data));
					finish();
				}
		    }  
		});
		
		
		final TextView loginScreen = (TextView) findViewById(R.id.link_to_login);
		
		String mystring=new String("Already has account! Login here");
		SpannableString content = new SpannableString(mystring);
		content.setSpan(new UnderlineSpan(), 0, mystring.length(), 0);
		loginScreen.setText(content);
		
        // Listening to Login Screen link
        loginScreen.setOnClickListener(new View.OnClickListener() {
 
            public void onClick(View arg0) {
                // Closing registration screen
                // Switching to Login Screen/closing register screen
            	loginScreen.setTextColor(Color.GRAY);
                finish();
            }
        });
	}
	
	public void MyAlert(){
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(Register.this);
		alertBuilder.setTitle("Oops =.=");
		alertBuilder.setMessage("Sorry, you cannot register withour filling all !");
		alertBuilder.setNegativeButton("Go Back", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	dialog.cancel();
            }
		});
		AlertDialog alert = alertBuilder.create();
        alert.show();
	}
}
