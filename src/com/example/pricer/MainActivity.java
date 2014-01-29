package com.example.pricer;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class MainActivity extends Activity {
	
	private static final String TAG = "Push Notification Demo Activity";
    private static final String SENDER_ID = "412886239406";
    
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    TextView mDisplay;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    Context context;

    String regid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_page);
		setTitle("Login to your account");
		
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
		
		//GCM register
        context = getApplicationContext();

        //Check device for Play Services APK. If check succeeds, proceed with
        //GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
        

		
		
		Button loginButton = (Button) findViewById(R.id.btnLogin);
		loginButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				EditText emailEditText = (EditText) findViewById(R.id.usr_email);
				String usrEmailString = emailEditText.getText().toString();
				
				EditText passwordEditText = (EditText) findViewById(R.id.usr_password);
				String usrPasswordString = passwordEditText.getText().toString();
				
				if (usrEmailString.length() == 0 || usrPasswordString.length() == 0)
					MyAlert();
				else {
					String selectExpression = "select * from `" + Domain + "` where itemName() = '" + usrEmailString + "'" ;
					
					SelectRequest selectRequest = new SelectRequest(selectExpression);
					String record = sdb.select(selectRequest).getItems().get(0).getAttributes().get(1).getValue().trim();
					String input = Integer.toString(usrPasswordString.hashCode()).trim();
					
					if (input.equals(record)){
						Intent in = new Intent(MainActivity.this, GetPriceActivity.class);
						in.putExtra("User", usrEmailString);
						final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
					    String registrationId = prefs.getString(PROPERTY_REG_ID, "");
					    in.putExtra("RegisterId", registrationId);
				        MainActivity.this.startActivity(in);
					} else {
						MyAlert();
					}
				}
				
		    }  
		});
		
		final TextView registerScreen = (TextView) findViewById(R.id.link_to_register);

		String mystring=new String("New to Pricer? Register here");
		SpannableString content = new SpannableString(mystring);
		content.setSpan(new UnderlineSpan(), 0, mystring.length(), 0);
		registerScreen.setText(content);
		
        // Listening to register new account link
        registerScreen.setOnClickListener(new View.OnClickListener() {
 
            public void onClick(View v) {
                // Switching to Register screen
            	registerScreen.setTextColor(Color.GRAY);
                Intent i = new Intent(getApplicationContext(), Register.class);
                startActivity(i);
            }
        });
        
        
        
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void MyAlert(){
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
		alertBuilder.setTitle("Oops !!");
		alertBuilder.setMessage("Sorry, your password is not accepted !");
		alertBuilder.setNegativeButton("Go Back", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	dialog.cancel();
            }
		});
		AlertDialog alert = alertBuilder.create();
        alert.show();
	}
	
	@Override
	protected void onResume() {
	    super.onResume();
	    checkPlayServices();
	}
	
	private String getRegistrationId(Context context) {
	    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	    String registrationId = prefs.getString(PROPERTY_REG_ID, "");
	    if (registrationId.isEmpty()) {
	        Log.i(TAG, "Registration not found.");
	        return "";
	    }
	    // Check if app was updated; if so, it must clear the registration ID
	    // since the existing regID is not guaranteed to work with the new
	    // app version.
	    int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
	    int currentVersion = getAppVersion(context);
	    if (registeredVersion != currentVersion) {
	        Log.i(TAG, "App version changed.");
	        return "";
	    }
	    return registrationId;
	}
	
	
	private static int getAppVersion(Context context) {
	    try {
	        PackageInfo packageInfo = context.getPackageManager()
	                .getPackageInfo(context.getPackageName(), 0);
	        return packageInfo.versionCode;
	    } catch (NameNotFoundException e) {
	        // should never happen
	        throw new RuntimeException("Could not get package name: " + e);
	    }
	}
	
	private boolean checkPlayServices() {
	    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	    if (resultCode != ConnectionResult.SUCCESS) {
	        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
	            GooglePlayServicesUtil.getErrorDialog(resultCode, this,
	                    PLAY_SERVICES_RESOLUTION_REQUEST).show();
	        } else {
	            Log.i(TAG, "This device is not supported.");
	            finish();
	        }
	        return false;
	    }
	    return true;
	}
	
	private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    //sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

//            @Override
//            protected void onPostExecute(String msg) {
//                mDisplay.append(msg + "\n");
//            }
        }.execute(null, null, null);
    }
	
	
	private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }
	
	@Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
