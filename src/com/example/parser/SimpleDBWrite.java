package com.example.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;


public class SimpleDBWrite {

    public List<ReplaceableItem> run(String searchItem, AmazonSimpleDB _sdb) throws Exception {

        AmazonSimpleDB sdb = _sdb;
//		Region usWest2 = Region.getRegion(Regions.US_WEST_2);
//		sdb.setRegion(usWest2);

        List<ReplaceableItem> resItems = null;
        try {
            // Create a domain
            String myDomain = "WanningStore";
            //sdb.createDomain(new CreateDomainRequest(myDomain));
            
            // Put data into a domain
            //System.out.println("Putting data into " + myDomain + " domain.\n");
            resItems = createData(searchItem);
            sdb.batchPutAttributes(new BatchPutAttributesRequest(myDomain, resItems));
        } catch (AmazonServiceException ase) {
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
        
        return resItems;
    }


    public static List<ReplaceableItem> createData(String searchItem) throws IOException {
        List<ReplaceableItem> Data = new ArrayList<ReplaceableItem>();
        
        PriceParser myParser = new PriceParser();
        ArrayList<resultItem> prodResultItems = myParser.Query2(searchItem, "1");
        for (resultItem e:prodResultItems){
        	Data.add(new ReplaceableItem(e.name).withAttributes(
                new ReplaceableAttribute("Keyword", searchItem, true),
                new ReplaceableAttribute("Price", e.price, true),
                new ReplaceableAttribute("Site", "Walmart", true)));
        }
        return Data;
    }
}
