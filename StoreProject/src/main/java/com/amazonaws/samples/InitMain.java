package com.amazonaws.samples;

import java.io.File;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import com.amazonaws.AmazonClientException;

public class InitMain {
	
	//this class is for initial the infrastructure of the shop in empty aws system
//
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
static String orderStatus = "status";

static String category = "category";
static String items = "items";
static String itemName = "itemName";
static String amount = "amount";

static Scanner sc = new Scanner(System.in); 
static String categoryArray[] = {"alcohol","drinks"};
static String categoryItemsArray[] = {"vodka,beer,arak,whiskey,wine,","XL,"};    




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
			choise= Integer.valueOf(sc.nextLine());
						
			
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
			e.printStackTrace();
		}
	}
	
	 public static void initItemsList()
	 {
		 int i;
		 
		 for(i=0; i<categoryArray.length;i++)
		 {
			 itemCate.put(categoryArray[i],categoryItemsArray[i]);
		 }

	 }
	 
	 public static void initStore()
	 {
		 initItemsList();

	        try {
	            
	            System.out.println("Enter number of shops ");
	            
	            while(NumberOfshops < 1)
	            {
	            	NumberOfshops=Integer.valueOf(sc.nextLine());
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
		 	putIDNumDynamoDB();
			
	 }
	
	 private static void enterItemsNames() {
		 DynamoDBHandler DDBH = new DynamoDBHandler(region, items_table_name,category,items);

		 Set<?> set = itemCate.entrySet();
         Iterator<?> iterator = set.iterator();
         while(iterator.hasNext()) {
            Map.Entry mentry = (Map.Entry)iterator.next();
   		 	DDBH.putCategoryToTable(mentry.getKey().toString(), mentry.getValue().toString());

            }
         }


	public static void  initSQS()
	 {
		//to open new sqs
	        SQSHandler sqsHandler = new SQSHandler( region, queueMissName);
	        SQSHandler sqsHandler2 = new SQSHandler( region, queueOrderName);
	 }
	 
	 public static void  initDynamoDB()
	 {
		 	int i;
	        DynamoDBHandler DDBH;
	        
	        DDBH = new DynamoDBHandler(region, order_table_name,orderId,orderContent,orderStatus);;
	        DDBH = new DynamoDBHandler(region, items_table_name,category,items);


	        for(i=1 ; i<=NumberOfshops ; i++)
	        {
	        	DDBH = new DynamoDBHandler(region, table_name+i,itemName,amount);
	        }
	 }
	 
	 public static void  putIDNumDynamoDB() {
	        DynamoDBHandler DDBH_Order_table=new DynamoDBHandler(region, order_table_name,orderId,orderContent,orderStatus);;
	        DDBH_Order_table.putOrderToTable("0", "0", "unused");
	 }

	
	public static void initpicbucketS3()
	 {
        S3Handler s3Handler = new S3Handler( region, bucketPicName);
        
        try {
        	 /* Display content using Iterator*/
            Set<?> set = itemCate.entrySet();
            Iterator<?> iterator = set.iterator();
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
	        
	        
	        	
	        S3Handler s3Handler = new S3Handler( region, bucketPicName);
	        SQSHandler sqsHandler = new SQSHandler(region, queueMissName);
	        SQSHandler sqsHandler2 = new SQSHandler(region, queueOrderName);
	        
	        
			 initItemsList();
	        
			 Set<?> set = itemCate.entrySet();
	            Iterator<?> iterator = set.iterator();
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
	        
	        DDBH = new DynamoDBHandler(region, order_table_name,orderId,orderContent,orderStatus);
	        DDBH.deleteTable();

	        DDBH = new DynamoDBHandler(region, items_table_name,category,items);
	        DDBH.deleteTable();

	        for(i=1 ; i<=NumberOfshops ; i++)
	        {
	        DDBH = new DynamoDBHandler(region, table_name+i,itemName,amount);
	        DDBH.deleteTable();
	        }
	    	
	            
	}
	
    
    public static void getNumberOfStores()
    {

        try {      
            System.out.println("Enter number of shops ");
            
            while(NumberOfshops < 1)
            {
            	NumberOfshops= Integer.valueOf(sc.nextLine());
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
			number= Integer.valueOf(sc.nextLine());
						
		}catch(Exception e)
		{
			System.out.println("not a number");
		}
	
		
		
		return number;
	}
    
}
