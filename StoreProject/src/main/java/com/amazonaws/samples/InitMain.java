package com.amazonaws.samples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.UUID;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import com.amazonaws.services.sqs.model.Message;
import com.sun.javafx.geom.PickRay;
import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.model.S3Object;

public class InitMain {
	
	//this class is for initial the infrastructure of the shop in empty aws system
static AWSCredentials credentials =  new ProfileCredentialsProvider("default").getCredentials();
static int NumberOfshops = 0;
static String region = "us-east-2";
static HashMap<String, String> itemCate = new HashMap<String, String>();

//bucketPicName 
static 	String bucketPicName = "afekapicturebucketalonitayoran";

//queue names
static String queueMissName = new String("queueShortage");
static String queueOrderName = new String("queueOrder");

//tables names
static String table_name = "DatabaseShop";
static String order_table_name = "order_table";
static String items_table_name = "items_table";

// columns name of tables
static String orderId = "orderId";
static String orderContent = "orderContent";
static String category = "category";
static String items = "items";
static String itemName = "itemName";
static String amount = "amount";

static ProfileCredentialsProvider credentialsProvider  = readCredentials(); 
static Scanner sc = new Scanner(System.in);
	

	public static void main(String[] args) {
			
		store();
		
	}
	
	
	public static void store()
	{
		int choise = 1;

		System.out.println("wellcome to init store program");
		System.out.println("-------------------------------");
		System.out.println("1) init a new store");
		System.out.println("2) shutdown store");
		System.out.println("enter your choise:");
		
		
		try {
			choise= sc.nextInt();
						
			
			if(choise == 1)
			{
				initStore();
				System.out.println("store is up");
			}
			else if(choise == 2)
			{

			 	DeleteStore();
			 	System.out.println("Store is Down");
			}
			else
			{
				System.out.println("it's not 1 or 2");
			}	
		}catch(Exception e)
		{
			System.out.println("Exception it's not 1 or 2");
		}
	}
	
	 public static void initItemsList()
	 {
		 itemCate.put("alcohol", "vodka,beer,arak,whiskey,wine,");
		 itemCate.put("drinks" , "XL,");
	 }
	 
	 public static void initStore()
	 {
		 initItemsList();

	        try {
	            
	            System.out.println("Enter number of shops ");
	            
	            while(NumberOfshops < 1)
	            {
	            	NumberOfshops= sc.nextInt();
	            }
	            
	        } catch (Exception e) {
	            throw new AmazonClientException(
	                    "Cannot load the credentials from the credential profiles file. " +
	                    "Please make sure that your credentials file is at the correct " +
	                    "location (C:\\Users\\alon\\.aws\\credentials), and is in valid format.",
	                    e);
	        }
		 
		 
			//init bucket for pictures of products
		 	
		 	initDynamoDB();
		 	initpicbucketS3();
		 	initSQS();
		 	enterItemsNames();
			
	 }
	
	 private static void enterItemsNames() {
		 DynamoDBHandler DDBH = new DynamoDBHandler(region, items_table_name, credentialsProvider,category,items);

		 Set set = itemCate.entrySet();
         Iterator iterator = set.iterator();
         while(iterator.hasNext()) {
            Map.Entry mentry = (Map.Entry)iterator.next();
   		 	DDBH.putStringToTable(mentry.getKey().toString(), mentry.getValue().toString());

            }
         }


	public static void  initSQS()
	 {
		//to open new sqs
	        SQSHandler sqsHandler = new SQSHandler(credentialsProvider, region, queueMissName);
	        SQSHandler sqsHandler2 = new SQSHandler(credentialsProvider, region, queueOrderName);
	 }
	 
	 public static void  initDynamoDB()
	 {
		 	int i;
	        DynamoDBHandler DDBH;
	        DDBH = new DynamoDBHandler(region, order_table_name, credentialsProvider,orderId,orderContent);
	        DDBH = new DynamoDBHandler(region, items_table_name, credentialsProvider,category,items);

	        for(i=0 ; i<NumberOfshops ; i++)
	        {
	        	DDBH = new DynamoDBHandler(region, table_name+i, credentialsProvider,itemName,amount);
	        }

	 }

	
	public static void initpicbucketS3()
	 {

        S3Handler s3Handler = new S3Handler(credentials, region, bucketPicName);
        
        try {
        	 /* Display content using Iterator*/
            Set set = itemCate.entrySet();
            Iterator iterator = set.iterator();
            while(iterator.hasNext()) {
               Map.Entry mentry = (Map.Entry)iterator.next();
               String[] itemlist = mentry.getValue().toString().split(",");
               for(String s:itemlist) {
            	   if(s.compareTo("")!=0)
    	               s3Handler.putFile(new File(s+".jpg"), s);
               }
            }
        					
		} catch (Exception e) {
			// 
			e.printStackTrace();
		}
	 }
	
	
	 
	 private static void DeleteStore() {
		 
		 	int i;
	        DynamoDBHandler DDBH;
	        
	        getNumberOfStores();
	        
	        
	        	
	        S3Handler s3Handler = new S3Handler(credentials, region, bucketPicName);
	        SQSHandler sqsHandler = new SQSHandler(credentialsProvider, region, queueMissName);
	        SQSHandler sqsHandler2 = new SQSHandler(credentialsProvider, region, queueOrderName);
	        
	        
			 initItemsList();
	        
			 Set set = itemCate.entrySet();
	            Iterator iterator = set.iterator();
	            while(iterator.hasNext()) {
	               Map.Entry mentry = (Map.Entry)iterator.next();
	               String[] itemlist = mentry.getValue().toString().split(",");
	               for(String s:itemlist) {
	            	   if(s.compareTo("")!=0)
	            		   s3Handler.DeleteObjectFromBucket(s);
	            }
	            }
	    		    	
	    	s3Handler.DeleteBucket();
	    	
	        
	        sqsHandler.DeleteQueue();
	        sqsHandler2.DeleteQueue();
	        
	        DDBH = new DynamoDBHandler(region, order_table_name, credentialsProvider,orderId,orderContent);
	        DDBH.deleteTable();

	        DDBH = new DynamoDBHandler(region, items_table_name, credentialsProvider,category,items);
	        DDBH.deleteTable();

	        for(i=0 ; i<NumberOfshops ; i++)
	        {
	        DDBH = new DynamoDBHandler(region, table_name+i, credentialsProvider,itemName,amount);
	        DDBH.deleteTable();
	        }
	    	
	            
	}
	
	
	 public static void testS3()
	 {
		 
	        AWSCredentials credentials = null;
			String region = "us-east-2";
	        String bucketName = "first-s3-bucket" + UUID.randomUUID();
	        String key = "VODKA";
	        
	        try {
	            credentials = new ProfileCredentialsProvider("default").getCredentials();
	        } catch (Exception e) {
	            throw new AmazonClientException(
	                    "Cannot load the credentials from the credential profiles file. " +
	                    "Please make sure that your credentials file is at the correct " +
	                    "location (C:\\Users\\alon\\.aws\\credentials), and is in valid format.",
	                    e);
	        }
	        
	        S3Handler s3Handler = new S3Handler(credentials, region, bucketName);
	        

	        try {
	        	
	        	
				s3Handler.putFile(new File("VODKA.jpg"), key);
				
				S3Object S3object = s3Handler.getItem(key);
				
				//displayTextInputStream(S3object.getObjectContent());
				
				
				s3Handler.DeleteObjectFromBucket(key);
				
				s3Handler.DeleteBucket();
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	        
	        
	 }
	
	
	 public static void testSQS()
	 {
			String region = "us-east-2";
			String queueName = new String("myQueue");
			ProfileCredentialsProvider credentialsProvider;
			
			// Read Credentials
			credentialsProvider = readCredentials();
			
	  
	        SQSHandler sqsHandler = new SQSHandler(credentialsProvider, region, queueName);

	        

	        sqsHandler.SendMessage("Alon 1");
	        sqsHandler.SendMessage("Alon 2");
	        sqsHandler.SendMessage("Alon 3");
	        
	        
	        Message message = sqsHandler.ReceiveMessages();
	        
	        displayMessage(message);
	        
	        
	        sqsHandler.DeleteMessage(message);
	        
	        
	        sqsHandler.DeleteQueue();
	 }
	 
	 
	    public static void testDynamoDB()
	    {
	        /* Read the name from command args */
	        String table_name = "Table";
	        String region = new String("us-east-2");
	        ProfileCredentialsProvider credentialsProvider;
	        String ItemName = "VODKA";
		    int Amount;
	        
	   		
	   	 // Read Credentials
	   	 	credentialsProvider = readCredentials();
	   	 	
	   	 	DynamoDBHandler DDBH = new DynamoDBHandler(region, table_name, credentialsProvider,itemName,amount);
	   	 	
	   	 	System.out.println("put 10 VODKA in table");
	   	 	DDBH.putItem(ItemName, 10);
	   	 	
	   	 	System.out.println("put 30 VODKA in table");
	   	 	DDBH.putItem(ItemName, 30);
	   	 	
	   	 	
	   	 	Amount = DDBH.retrieveItemAmount(ItemName);
	   	 	System.out.println("The Amount of " + ItemName + " is " + Amount);

	   	 	DDBH.deleteItem(ItemName);
	   	 	
	   	 	DDBH.deleteTable();
	   	 
	   	 
	    }
	    
	 
	
    public static ProfileCredentialsProvider readCredentials() {
    	
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        try {
            credentialsProvider.getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (C:\\Users\\BorisM\\.aws\\credentials), and is in valid format.",
                    e);
        }
        return credentialsProvider;
    }
	
	
	
    private static File createSampleFile() throws IOException {
        File file = File.createTempFile("aws-java-sdk-", ".txt");
        file.deleteOnExit();

        Writer writer = new OutputStreamWriter(new FileOutputStream(file));
        writer.write("abcdefghijklmnopqrstuvwxyz\n");
        writer.write("01234567890112345678901234\n");
        writer.write("!@#$%^&*()-=[]{};':',.<>/?\n");
        writer.write("01234567890112345678901234\n");
        writer.write("abcdefghijklmnopqrstuvwxyz\n");
        writer.close();

        return file;
    }

    /**
     * Displays the contents of the specified input stream as text.
     *
     * @param input
     *            The input stream to display as text.
     *
     * @throws IOException
     */
    private static void displayTextInputStream(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        while (true) {
            String line = reader.readLine();
            if (line == null) break;

            System.out.println("    " + line);
        }
        System.out.println();
    }
	

    public static void displayMessage(Message message) {

        System.out.println("Message");
        System.out.println("    MessageId:     " + message.getMessageId());
        System.out.println("    Body:          " + message.getBody());
        for (Entry<String, String> entry : message.getAttributes().entrySet()) {
            System.out.println("  Attribute");
            System.out.println("    Name:  " + entry.getKey());
            System.out.println("    Value: " + entry.getValue());
        }
    }
    
    public static void getNumberOfStores()
    {

        try {      
            System.out.println("Enter number of shops ");
            
            while(NumberOfshops < 1)
            {
            	NumberOfshops= sc.nextInt();
            }
            
        }catch (InputMismatchException  e) {

        	System.out.println("wrong number");
        	NumberOfshops = 1;
        	
        	
		} 
    }
    
	public static int choseFormUser()
	{
		int choise = 0;
		
		while(choise != 0) {
			choise = getNumberFromUser();
			if(choise == 1 || choise == 2)
			{
				
			}
			else
			{
				choise = 0;
			}
		}
		
		return choise;
	}
	
	public static int getNumberFromUser()
	{
		int number = 0;
		
		
		
		try {
			number= sc.nextInt();
						
		}catch(Exception e)
		{
			System.out.println("not a number");
		}
	
		
		
		return number;
	}
    
}
