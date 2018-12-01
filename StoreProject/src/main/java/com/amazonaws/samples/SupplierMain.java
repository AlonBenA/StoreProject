package com.amazonaws.samples;

import java.util.ArrayList;
import java.util.Scanner;
import com.amazonaws.services.sqs.model.Message;

public class SupplierMain {
	//
	static String queueMissName = new String("queueShortage");
	static String table_name = "DatabaseShop";
	static String region = "us-east-2";
	static Scanner sc = new Scanner(System.in);
	static int NumberOfshops;
	

	public static void main(String[] args) {
		
		 Supplier();

	}
	
	
	public static void Supplier()
	{
		String message = null;
		ArrayList<Product> products = new ArrayList<Product>();
    	int shopNumber = -1;
		int continueWork = 1;
		
		getNumberOfShops();
    	
		//Create array of shops Inventory
		DynamoDBHandler[] shopsInventory = null;
		shopsInventory = connectToShopsInventory();
		
		//Connect to queue Shortage
		SQSHandler sqsShortageHandler = new SQSHandler( region, queueMissName);
		
		while(continueWork == 1) {

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
			
			continueWork = SendMoreSupply();
		}
		
		System.out.println("Goodbye");

	}
	
	
	public static void getNumberOfShops()
	{
		try {
			NumberOfshops = -1;
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

		DynamoDBHandler[] shopsInventory = new DynamoDBHandler[NumberOfshops];
		
        for(i=1; i<= NumberOfshops ; i++)
        {
        	shopsInventory[i-1] = new DynamoDBHandler(region, table_name+i,itemName,amount);
        }
		
		return shopsInventory;
	}
	
	//Getting a new message about store shortage
	public static String getMessage(SQSHandler sqsShortageHandler)
	{
		//Example message
		//String message = "2,vodka 5,XL 5,beer 12,";
		String Stringmessage = "";
		Message message = null;
		
		System.out.println("wait for message");
		while(message == null)
		{
			message = sqsShortageHandler.ReceiveMessages();
		}
		System.out.println("got message");
		
		Stringmessage = message.getBody();
		
		System.out.println(Stringmessage);
		
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
		
		if(number < 1 || number > NumberOfshops)
		{
			number = -1;
		}
		
		
		return number;
	}
    
    //added the missing Amount + 100 of item to the shop database 
    public static void AddProductsToShop(int shopNumber,ArrayList<Product> products,DynamoDBHandler[] shopsInventory)
    {
    	int i;
    	int numberOfExtraProducts = 100;
    	
    	int shopNumberInArray = shopNumber-1;
    	System.out.println("shopNumber " + shopNumber);
    	System.out.println("shopNumberInArray " + shopNumberInArray);
    	
    	for(i = 0 ; i < products.size(); i++)
    	{
    		int currentAmount = shopsInventory[shopNumberInArray].retrieveItemAmount(products.get(i).getName());
    		shopsInventory[shopNumberInArray].putItem(products.get(i).getName(), currentAmount + numberOfExtraProducts + products.get(i).getAmount());
    	}
    	
    	
    }
    
    //ask the user if he waht to get send more supply to the srores
    private static int SendMoreSupply() {
		int val = -1;
		System.out.println("Send More Supply:");
		System.out.println("1) yes");
		System.out.println("0) no");
		while (val < 0 || val > 1) {
			try {
				val = Integer.valueOf(sc.nextLine());

				if (val == 1 || val == 0) {
					return val;
				} else {
					System.out.println("it's not 1 or 0");
				}
			} catch (Exception e) {
				System.out.println("Exception it's not a number");
			}
		}
		return -1;
	}
    

}
