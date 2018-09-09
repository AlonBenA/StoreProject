package com.amazonaws.samples;

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
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;

public class DynamoDBHandler {
	
	private String region;
	private String table_name;
	private AmazonDynamoDB ddb;
	private DynamoDB dynamoDB;
	private String colKey;
	private String colVal;
	private String colVal2;

	
	public DynamoDBHandler(String region,String table_name, ProfileCredentialsProvider credentialsProvider,String colKey,String colVal)
	{
		this.region = region;
		this.colVal = colVal;
		this.colKey = colKey;
		this.colVal2 = "unused";

		this.table_name = table_name;

		initTable(credentialsProvider);
	}
	
	public DynamoDBHandler(String region,String table_name, ProfileCredentialsProvider credentialsProvider,String colKey,String colVal,String colVal2)
	{
		this.region = region;
		this.colVal = colVal;
		this.colKey = colKey;
		this.colVal2 = colVal2;

		this.table_name = table_name;

		initTable(credentialsProvider);
	}
	
	
	private void initTable(ProfileCredentialsProvider credentialsProvider)
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
		        .withAttributeDefinitions(new AttributeDefinition(colKey, ScalarAttributeType.S))
		         .withKeySchema(new KeySchemaElement(colKey, KeyType.HASH))
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
	    	Item item = new Item().withPrimaryKey(colKey, Name).withNumber(colVal, Amount);
	        table.putItem(item);         
	        
	    }
	    catch (Exception e) {
	        System.err.println("Create items failed.");
	        System.err.println(e.getMessage());

	    }
	    
	}

	//Put a new Category with the items on the dynamodb table
	//If the Category already exists then its values are overridden by the new ones
	public void putCategoryToTable(String key, String value)
	{
	    Table table = dynamoDB.getTable(table_name);
	    
	    try {
	    	Item item = new Item().withPrimaryKey(colKey, key).withString(colVal, value);
	        table.putItem(item);         
	        
	    }
	    catch (Exception e) {
	        System.err.println("Create items failed.");
	        System.err.println(e.getMessage());

	    }
    
	}
	
	
	//Put a new order with the items and status on the dynamodb table
	//If the key already exists then its values are overridden by the new ones
	public void putOrderToTable(String key, String value,String status)
	{
	    Table table = dynamoDB.getTable(table_name);
	    
	    try {
	    	Item item = new Item().withPrimaryKey(colKey, key).withString(colVal, value)
	    			.withString(colVal2, status);
	        table.putItem(item);         
	        
	    }
	    catch (Exception e) {
	        System.err.println("Create items failed.");
	        System.err.println(e.getMessage());

	    }
    
	}
	
	

	public String retrieveItemString(String key)
	{
		    Table table = dynamoDB.getTable(table_name);

		    String value = "";
		    
		    try {
		    	
		        Item item = table.getItem(colKey,key);
		        value = item.getString(colVal);
		    }
		    catch (Exception e) {
		        System.err.println("Get Item failed.");
		        System.err.println(e.getMessage());
		    }
		    return value;
		}
	
	
	
	public String retrieveItemStatus(String key)
	{
		    Table table = dynamoDB.getTable(table_name);

		    String value = "";
		    
		    try {
		    	
		        Item item = table.getItem(colKey,key);
		        value = item.getString(colVal2);
		    }
		    catch (Exception e) {
		        System.err.println("Get Item failed.");
		        System.err.println(e.getMessage());
		    }
		    return value;
		}
	
	
	public void deleteItem(String ItemName) 
	{
	    Table table = dynamoDB.getTable(table_name);

	        try {

	            DeleteItemSpec deleteItemSpec = new DeleteItemSpec().withPrimaryKey(colKey, ItemName);

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

	        Item item = table.getItem(colKey, ItemName);
	        Amount = Integer.parseInt(item.get(colVal).toString());

	    }
	    catch (Exception e) {
	        System.err.println("GetItem failed.");
	        System.err.println(e.getMessage());
	    }
	    
	    return Amount;

	}
	
	

}
