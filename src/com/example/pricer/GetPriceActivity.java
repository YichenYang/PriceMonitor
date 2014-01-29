package com.example.pricer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.example.adapter.ItemAdapter;
import com.example.adapter.ReplaceableItemAdapter;
import com.example.parser.SimpleDBWrite;

public class GetPriceActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.price_page);
		setTitle("Price Search");
		//RequestTask myTask = new RequestTask();
		//myTask.execute("http://stackoverflow.com", null, null);
		final Intent rec = getIntent();
		
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
		
		final ListView itemListView = (ListView) findViewById(R.id.listview);
		
		Button loginButton = (Button) findViewById(R.id.btnSearch);
		loginButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				EditText searchET = (EditText) findViewById(R.id.inputSearch);
				String searchItem = searchET.getText().toString();
				
				String myDomain = "WanningStore";
				String selectExpression = "select * from `" + myDomain + "` where Keyword = '"+searchItem+"'" ;
		        SelectRequest selectRequest = new SelectRequest(selectExpression);
		        
		        List<ReplaceableItem> resItems = null;
				
		        if (sdb.select(selectRequest).getItems().isEmpty()){
					SimpleDBWrite writer = new SimpleDBWrite();
					try {
						resItems = writer.run(searchItem, sdb);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//ShowDirectly(resItems);
				} 
				ItemAdapter adapter = ShowFromSimpleDb(searchItem, sdb);
				itemListView.setAdapter(adapter);
		    }  
		});
        
        itemListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                int position, long id) {
            	Item i = (Item)itemListView.getItemAtPosition(position);
	            Intent in = new Intent(getApplicationContext(), SingleListItem.class);
	            // sending data to new activity
	            in.putExtra("Name", i.getName());
	            in.putExtra("Price", i.getAttributes().get(2).getValue().toString());
	            in.putExtra("Provider", i.getAttributes().get(1).getValue().toString());
	            in.putExtras(rec);
	            startActivity(in);
            }
        });
        
        
	}
	
	public void ShowDirectly(List<ReplaceableItem> resItems){
		
		ListView itemListView = (ListView) findViewById(R.id.listview);
		ReplaceableItemAdapter adapter = new ReplaceableItemAdapter(this, R.layout.simple_list_item, resItems);
        itemListView.setAdapter(adapter);
	}
	
	public ItemAdapter ShowFromSimpleDb(String s, AmazonSimpleDB _sdb){
		String searchItem = s;
		AmazonSimpleDB sdb = _sdb;
		
		try {
			String myDomain = "WanningStore";
			String selectExpression = "select * from `" + myDomain + "` where Keyword = '"+searchItem+"'" ;
	        SelectRequest selectRequest = new SelectRequest(selectExpression);

	        final ArrayList<Item> ItemNameList = new ArrayList<Item>();
	        for (Item item : sdb.select(selectRequest).getItems()) {
	        	ItemNameList.add(item);
	        }
	        
	        return new ItemAdapter(this, R.layout.simple_list_item, ItemNameList);
	        
	        
	        
		}catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon SimpleDB, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with SimpleDB, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
		return null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
