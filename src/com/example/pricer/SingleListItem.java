package com.example.pricer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
 
public class SingleListItem extends Activity{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.single_list_item_view);
        
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
         
        TextView name = (TextView) findViewById(R.id.product_name);
        TextView price = (TextView) findViewById(R.id.product_price);
        TextView provider = (TextView) findViewById(R.id.product_provider);
         
        final Intent in = getIntent();
        // getting attached intent data
        name.setText(in.getStringExtra("Name"));
        price.setText(in.getStringExtra("Price"));
        provider.setText(in.getStringExtra("Provider"));
        
        Button saveButton = (Button) findViewById(R.id.btnSave);
		saveButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				String myDomain = "UserSavedStore";
	            sdb.createDomain(new CreateDomainRequest(myDomain));
	            
	            ReplaceableItem usrItem = new ReplaceableItem(in.getStringExtra("Name")).withAttributes(
		                new ReplaceableAttribute("Provider", in.getStringExtra("Provider"), true),
		                new ReplaceableAttribute("Price", in.getStringExtra("Price"), true),
		                new ReplaceableAttribute("User", in.getStringExtra("User"), true),
		                new ReplaceableAttribute("RegisterId", in.getStringExtra("RegisterId"), true));
				List<ReplaceableItem> data = new ArrayList<ReplaceableItem>();
				data.add(usrItem);
				sdb.batchPutAttributes(new BatchPutAttributesRequest(myDomain, data));
	            MyAlert();
			}
		});
    }
    
    public void MyAlert(){
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(SingleListItem.this);
		alertBuilder.setTitle("Congratulations !!");
		alertBuilder.setMessage("You have successful saved the item into your wish list");
		alertBuilder.setNegativeButton("Go Back", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	dialog.cancel();
            }
		});
		AlertDialog alert = alertBuilder.create();
        alert.show();
	}
}
