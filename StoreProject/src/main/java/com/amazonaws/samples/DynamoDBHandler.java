package com.amazonaws.samples;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ListTablesRequest;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;

public class DynamoDBHandler {
	
	private String region;
	private String table_name;
	private AmazonDynamoDB ddb;
	private DynamoDB dynamoDB;
	
	public DynamoDBHandler(String region,String table_name, ProfileCredentialsProvider credentialsProvider)
	{
		this.region = region;
		this.table_name = table_name;
		init(credentialsProvider);
	}
	
	
	private void init(ProfileCredentialsProvider credentialsProvider)
	{
		ddb = AmazonDynamoDBClientBuilder.standard()
	    		 .withCredentials(credentialsProvider)
	             .withRegion(region)
	             .build();	
		
		
		dynamoDB = new DynamoDB(ddb);
		
		System.out.println("check if the table " + table_name + " exists");
		if(!tableExists(this.table_name))
		{
			System.out.println("The table " + table_name + " doesn't exists");
			System.out.println("Create table " + table_name);
			createTable();
		}
		else
		{
			System.out.println("The table " + table_name + " does exists");
		}

	}
	
	
	public boolean tableExists(String table_name)
	{
		
		try {
            TableDescription tableDescription = dynamoDB.getTable(table_name).describe();

            return true;
        } catch (com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException rnfe) {
            System.out.println("");
        }
        return false;



	}
	
	
	private void createTable()
	{
		
		 // Create the Request
		CreateTableRequest request = new CreateTableRequest()
		        .withAttributeDefinitions(new AttributeDefinition("Name", ScalarAttributeType.S))
		         .withKeySchema(new KeySchemaElement("Name", KeyType.HASH))
		         .withProvisionedThroughput(new ProvisionedThroughput(
		                  new Long(5), new Long(5)))
		         .withTableName(table_name);
				
	     // Create a Table
	     try {
	         CreateTableResult result = ddb.createTable(request);
	         
	         System.out.println(result.getTableDescription().getTableName());
	         
	     } catch (AmazonServiceException e) {
	         System.err.println(e.getErrorMessage());
	         System.exit(1);
	     }
		
	}
	
	
	
	public void deleteTable()
	{
		DeleteTableRequest request = new DeleteTableRequest().withTableName(table_name);
		
		
		DeleteTableResult result = ddb.deleteTable(request);
		
		System.out.println("The table " + result.getTableDescription().getTableName() + " was deleted");
		System.out.println(result.getTableDescription());
		
		
	}
	
	//Put a new item with the Amount on the dynamodb table
	//If the name already exists then its values are overridden by the new ones
	public void putItem(String Name, int Amount)
	{
	    Table table = dynamoDB.getTable(table_name);
	    
	    try {
	    	Item item = new Item().withPrimaryKey("Name", Name).withNumber("Amount", Amount);
	        table.putItem(item);         
	        
	    }
	    catch (Exception e) {
	        System.err.println("Create items failed.");
	        System.err.println(e.getMessage());

	    }
	    
	}
	
	
	
	public void deleteItem(String ItemName) 
	{
	    Table table = dynamoDB.getTable(table_name);
	    int Amount = retrieveItemAmount(ItemName);

	        try {

	            DeleteItemSpec deleteItemSpec = new DeleteItemSpec().withPrimaryKey("Name", ItemName);

	            DeleteItemOutcome outcome = table.deleteItem(deleteItemSpec);
	            
	            System.out.println("the item " + ItemName + " was deleteed");

	        }
	        catch (Exception e) {
	            System.err.println("Error deleting item in " + table_name);
	            System.err.println(e.getMessage());
	        }
	    
	    
	}
	
	
	public int retrieveItemAmount(String ItemName) {
	    Table table = dynamoDB.getTable(table_name);
	    int Amount = 0;

	    try {

	        Item item = table.getItem("Name", ItemName, "Amount", null);
	        Amount = Integer.parseInt(item.get("Amount").toString());

	    }
	    catch (Exception e) {
	        System.err.println("GetItem failed.");
	        System.err.println(e.getMessage());
	    }
	    
	    return Amount;
	    

	}
	
	

}
