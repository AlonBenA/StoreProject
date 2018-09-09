package com.amazonaws.samples;

import java.util.ArrayList;
import java.util.Scanner;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.sqs.model.Message;

public class ShopMain {
	static String shopId = "1";

	static String queueMissName = new String("queueShortage");
	static String queueOrderName = new String("queueOrder");

	static String table_name = "DatabaseShop";
	static String order_table_name = "order_table";

	static String region = "us-east-2";
	static Scanner sc = new Scanner(System.in);

	static ProfileCredentialsProvider credentialsProvider = readCredentials();

	public static void main(String[] args) {
		initShop();
		Shop();

	}

	private static void Shop() {
		String orderId = null;
		String orderContent = null;

		ArrayList<Product> orderProducts = new ArrayList<Product>();

		// Create array of shops Inventory
		DynamoDBHandler shopInventory = connectToShopInventory();
		DynamoDBHandler order_table = connectToOrderTable();

		// Connect to queue Shortage
		SQSHandler sqsShortageHandler = new SQSHandler(credentialsProvider, region, queueMissName);
		SQSHandler sqsOrderHandler = new SQSHandler(credentialsProvider, region, queueOrderName);

		// Get message
		orderId = getMessage(sqsOrderHandler);
		orderContent = order_table.retrieveItemString(orderId);

		// order format "nameItem quantity,nameItem quantity,..."

		// get products from orderContent
		splitOrderMessage(orderContent, orderProducts);
		
		//get items order from inventory
		PrepareOrder(shopInventory,orderProducts,sqsShortageHandler);
		
		if (orderProducts.size() > 0) {
			orderProducts.clear();
		}

		//deleteAll(shopInventory, sqsShortageHandler);

	}

	private static void PrepareOrder(DynamoDBHandler shopInventory, ArrayList<Product> orderProducts,SQSHandler sqsShortageHandler) {
		String shortageMessege = shopId+",";
		int currentAmount = 0;
		for(int i=0;i < orderProducts.size();i++) {
			int orderAmount=orderProducts.get(i).getAmount();
			String nameProduct= orderProducts.get(i).getName();
			
			currentAmount= shopInventory.retrieveItemAmount(nameProduct);
			if(orderAmount<currentAmount) {
				currentAmount-=orderAmount;
				shopInventory.putItem(nameProduct, orderAmount);
			}
			else {
				//Example message
				//String message = "0,vodka 5,XL 5,beer 12,";
				shortageMessege+=nameProduct+" "+currentAmount+",";
			}	
		}
		if(shortageMessege.compareTo("0,")==0)
			sqsShortageHandler.SendMessage(shortageMessege);
		
		
	}

	private static DynamoDBHandler connectToOrderTable() {
		String orderId = "orderId";
		String orderContent = "orderContent";
		DynamoDBHandler OrderTable = new DynamoDBHandler(region, order_table_name, credentialsProvider, orderId,
				orderContent);

		return OrderTable;
	}

	public static DynamoDBHandler connectToShopInventory() {
		String itemName = "itemName";
		String amount = "amount";

		DynamoDBHandler shopsInventory = new DynamoDBHandler(region, table_name + shopId, credentialsProvider, itemName,
				amount);

		return shopsInventory;
	}

	public static String getMessage(SQSHandler sqsShortageHandler) {
		// Example message
		// String message = "0,vodka 5,XL 5,beer 12,";
		String Stringmessage = "";
		Message message = null;

		System.out.println("wait for message");
		while (message == null) {
			message = sqsShortageHandler.ReceiveMessages();
		}
		System.out.println("got message");

		Stringmessage = message.getBody();

		sqsShortageHandler.DeleteMessage(message);

		return Stringmessage;
	}

	public static void splitOrderMessage(String message, ArrayList<Product> products) {
		int i;
		Product product = null;

		String[] productList = message.split(",");

		for (i = 0; i < productList.length; i++) {
			String[] SplitProductBySpace = productList[i].split(" ");
			product = CheckProductString(SplitProductBySpace);
			if (product != null) {
				products.add(product);
			}
		}
	}

	public static Product CheckProductString(String[] productString) {
		Product product = null;
		int amount = -1;

		try {
			amount = Integer.parseInt(productString[1]);

		} catch (Exception e) {
			System.out.println("Not a number");
		}

		if (amount < 0) {

		} else {
			product = new Product(productString[0], amount);
		}

		return product;
	}

	public static ProfileCredentialsProvider readCredentials() {

		ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
		try {
			credentialsProvider.getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
					+ "Please make sure that your credentials file is at the correct "
					+ "location (C:\\Users\\BorisM\\.aws\\credentials), and is in valid format.", e);
		}
		return credentialsProvider;
	}

	public static void AddProductsToShop(int shopNumber, ArrayList<Product> products,
			DynamoDBHandler[] shopsInventory) {
		int i;
		int numberOfExtraProducts = 100;

		for (i = 0; i < products.size(); i++) {
			shopsInventory[shopNumber].putItem(products.get(i).getName(),
					numberOfExtraProducts + products.get(i).getAmount());
		}

	}

	public static void deleteAll(DynamoDBHandler[] shopsInventory, SQSHandler sqsShortageHandler) {
		int i;
		sqsShortageHandler.DeleteQueue();

		for (i = 0; i < NumberOfshops; i++) {
			shopsInventory[i].deleteTable();
		}

	}

}
