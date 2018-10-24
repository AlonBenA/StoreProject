package com.amazonaws.samples;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.Map.Entry;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.sqs.model.Message;

public class SupplierMain {
	//
	static String queueMissName = new String("queueShortage");
	static String table_name = "DatabaseShop";
	static String region = "us-east-2";
	static Scanner sc = new Scanner(System.in);
	static int NumberOfshops = 2;
	
	static ProfileCredentialsProvider credentialsProvider  = readCredentials(); 

	

	public static void main(String[] args) {
		
		 Supplier();

	}
	
	
	public static void Supplier()
	{
		String message = null;
		ArrayList<Product> products = new ArrayList<Product>();
    	int shopNumber = -1;
		
		getNumberOfShops();
    	
		//Create array of shops Inventory
		DynamoDBHandler[] shopsInventory = null;
		shopsInventory = connectToShopsInventory();
		
		//Connect to queue Shortage
		SQSHandler sqsShortageHandler = new SQSHandler(credentialsProvider, region, queueMissName);

		

			//Get message
			message = getMessage(sqsShortageHandler);
			
			//Split the message 
			shopNumber = spliitShortageMessage(message,products);
				
				
			// Add 100 + The number that are in miss from the store to the shop
			if(shopNumber != -1 && products.size() > 0)
			{
				AddProductsToShop(shopNumber,products,shopsInventory);
			}
			
			if(products.size() > 0)
			{
				products.clear();
			}

	}
	
	
	public static void getNumberOfShops()
	{
		try {
            
            System.out.println("Enter number of shops ");
            
            while(NumberOfshops < 1)
            {
            	NumberOfshops=Integer.valueOf(sc.nextLine());
            }
            
        } catch (Exception e) {
        	System.out.println("this is not a number, 2 is defualt");
        	NumberOfshops = 2;
        }
	}
	
	//Connects to the store database
	public static DynamoDBHandler[] connectToShopsInventory()
	{
		int i;
		String itemName = "itemName";
		String amount = "amount";

		DynamoDBHandler[] shopsInventory = new DynamoDBHandler[2];
		
        for(i=1; i<= NumberOfshops ; i++)
        {
        	shopsInventory[i-1] = new DynamoDBHandler(region, table_name+i, credentialsProvider,itemName,amount);
        }
		
		return shopsInventory;
	}
	
	//Getting a new message about store shortage
	public static String getMessage(SQSHandler sqsShortageHandler)
	{
		//Example message
		//String message = "0,vodka 5,XL 5,beer 12,";
		String Stringmessage = "";
		Message message = null;
		
		System.out.println("wait for message");
		while(message == null)
		{
			message = sqsShortageHandler.ReceiveMessages();
		}
		System.out.println("got message");
		
		Stringmessage = message.getBody();
		
		sqsShortageHandler.DeleteMessage(message);
		
		return Stringmessage;
	}
	
	//spliit the message from the store to products and return the shop id
    public static int spliitShortageMessage(String message,ArrayList<Product> products)
    {
    	int i;
    	int shopNumber = 0;
    	int placeOfShopNumberInMessage = 0;
    	int placeOfProductsInMessage = 1;
		Product product = null;
    	
    	String[] SplitMessageByComma = message.split(",");
    	shopNumber = CheckNumber(SplitMessageByComma[placeOfShopNumberInMessage]);
    	shopNumber = CheckNumberOfShop(shopNumber);
    	
    	
    	for(i=placeOfProductsInMessage; i < SplitMessageByComma.length; i++)
    	{
    		String[] SplitProductBySpace = SplitMessageByComma[i].split(" ");
    		product = CheckProductString(SplitProductBySpace);
    		if(product != null)
    		{
    			products.add(product);
    		}
    	}
    	
    	
    	return shopNumber;
    }
    
    
    // Check if string is number
	public static int CheckNumber(String stringNumber)
	{
		int number = -1;

		try {
			number= Integer.parseInt(stringNumber);
						
		}catch(Exception e)
		{
			System.out.println("Not a number");
		}
		

	
		return number;
	}
	
    // Check if the number that is ask from the store is a number and not negative 
	public static Product CheckProductString(String[] productString)
	{
		Product product = null;
		int amount = -1;

		try {
			amount = Integer.parseInt(productString[1]);
			
						
		}catch(Exception e)
		{
			System.out.println("Not a number");
			amount = -1;
		}
		
		if(amount < 0)
		{
			
		}
		else
		{
			product = new Product(productString[0], amount);
		}
	
		return product;
	}
	
	//check if the shop id is one of the shops Correct
	public static int CheckNumberOfShop(int number)
	{
		
		if(number < 1 || number >= NumberOfshops)
		{
			number = -1;
		}
		
		
		return number;
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
    
    //added the missing Amount + 100 of item to the shop database 
    public static void AddProductsToShop(int shopNumber,ArrayList<Product> products,DynamoDBHandler[] shopsInventory)
    {
    	int i;
    	int numberOfExtraProducts = 100;
    	int shopNumberInArray = shopNumber-1;
    	
    	
    	for(i = 0 ; i < products.size(); i++)
    	{
    		shopsInventory[shopNumberInArray].putItem(products.get(i).getName(), numberOfExtraProducts + products.get(i).getAmount());
    	}
    	
    	
    }
    

}
