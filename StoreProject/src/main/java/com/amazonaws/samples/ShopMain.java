package com.amazonaws.samples;

import java.util.ArrayList;
import java.util.Scanner;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.sqs.model.Message;

public class ShopMain {
	static String shopId;
	
	static String queueMissName = new String("queueShortage");
	static String queueOrderName = new String("queueOrder");

	static String table_name = "DatabaseShop";
	static String order_table_name = "order_table";

	static String region = "us-east-2";
	static Scanner sc = new Scanner(System.in);
	
	
	public static void main(String[] args) {
		workingShop();
	}
	
	private static void workingShop() {
		int choise = 1;

		System.out.println("wellcome to store program");
		System.out.println("-------------------------------");
		System.out.println("please enter your shopID:");
		shopId=sc.nextLine();
		System.out.println("1) add products to shop inventory");
		System.out.println("2) start prepare orders");
		System.out.println("enter your choise:");

		try {
			choise =  Integer.valueOf(sc.nextLine());

			if (choise == 1) {
				addProductsToShopInventory();
			} else if (choise == 2) {
				Shop();
				System.out.println("shop exit");
			} else {
				System.out.println("it's not 1 or 2");
			}
		} catch (Exception e) {
			System.out.println("Exception it's not 1 or 2");
		}
	}
	
	//add Products To Shop Inventory 
	private static void addProductsToShopInventory() {
		DynamoDBHandler shopInventory = connectToShopInventory();
		ArrayList<Product> Products = new ArrayList<Product>();
		// [0] name [1] amount
		String[] newProduct = new String[2];
		int numOfProducts = 0;
		System.out.println("how much products do you want to add the inventory? (number > 0)");
		try {
			numOfProducts = Integer.valueOf(sc.nextLine());
			if (numOfProducts > 0) {
				for (int i = 0; i < numOfProducts; i++) {
					System.out.println("enter product name");
					newProduct[0] = sc.nextLine();
					System.out.println("enter product amount");
					newProduct[1] = sc.nextLine();
					Products.add(CheckProductString(newProduct));
				}
				
				for (Product p : Products) {
					int currentAmount = shopInventory.retrieveItemAmount(p.getName());
					shopInventory.putItem(p.getName(), p.getAmount() + currentAmount);
				}
				System.out.println("shop Inventory ready");
			} else {
				System.out.println("num Of Products not valid must > 0 ");
			}
		} catch (Exception e) {
			System.out.println("Exception it's not number");
		}
		
	}
	
	//Prepare to get another order from sqs order id
	private static void Shop() {
		int continueWork = 1;
		while (continueWork == 1) {
			String orderId = null;
			String orderContent = null;
			String newStatusOrder = null;
			ArrayList<Product> orderProducts = new ArrayList<Product>();

			// connect to shops Inventory and order table
			DynamoDBHandler shopInventory = connectToShopInventory();
			DynamoDBHandler order_table = connectToOrderTable();

			// Connect to queue Shortage
			SQSHandler sqsShortageHandler = new SQSHandler(region, queueMissName);
			SQSHandler sqsOrderHandler = new SQSHandler( region, queueOrderName);

			// Get message
			orderId = getMessage(sqsOrderHandler);
			orderContent = order_table.retrieveItemString(orderId);
			// order format "nameItem quantity,nameItem quantity,..."
			System.out.println("The order content is: "+orderContent);
			// get products from orderContent
			splitOrderMessage(orderContent, orderProducts);

			// get items order from inventory
			newStatusOrder = PrepareOrder(shopInventory, orderProducts, sqsShortageHandler);

			// update status
			order_table.putOrderToTable(orderId, orderContent, newStatusOrder);
			System.out.println("prepere order is finish");
			
			if (orderProducts.size() > 0) {
				orderProducts.clear();
			}
			continueWork = continuePrepareOrder();
		}
	}
	//Prepare another order?
	private static int continuePrepareOrder() {
		int val = -1;
		System.out.println("Prepare another Order:");
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
				System.out.println("Exception it's not 1 or 0");
			}
		}
		return -1;
	}

	private static String PrepareOrder(DynamoDBHandler shopInventory, ArrayList<Product> orderProducts,
			SQSHandler sqsShortageHandler) {
		String startOfShortageMessege = shopId + ",";
		String shortageMessege = "";

		int currentAmount = 0;
		for (int i = 0; i < orderProducts.size(); i++) {
			int orderAmount = orderProducts.get(i).getAmount();
			String nameProduct = orderProducts.get(i).getName();

			currentAmount = shopInventory.retrieveItemAmount(nameProduct);
			if (orderAmount <= currentAmount) {
				currentAmount -= orderAmount;
				shopInventory.putItem(nameProduct, currentAmount);
			} else {
				// Example message
				// String message = "1,vodka 5,XL 5,beer 12,";
				shortageMessege += nameProduct + " " + orderAmount + ",";
			}
		}
		if (shortageMessege.compareTo("") != 0) {
			sqsShortageHandler.SendMessage(startOfShortageMessege + shortageMessege);
			return "miss: " + shortageMessege;
		} else
			return "finish";
	}

	private static DynamoDBHandler connectToOrderTable() {
		String orderId = "orderId";
		String orderContent = "orderContent";
		String orderStatus = "status";
		DynamoDBHandler OrderTable = new DynamoDBHandler(region, order_table_name, orderId,
				orderContent, orderStatus);

		return OrderTable;
	}

	public static DynamoDBHandler connectToShopInventory() {
		String itemName = "itemName";
		String amount = "amount";

		DynamoDBHandler shopsInventory = new DynamoDBHandler(region, table_name + shopId, itemName,
				amount);

		return shopsInventory;
	}
	//Getting a new message- orderid
	public static String getMessage(SQSHandler sqs) {
		String Stringmessage = "";
		Message message = null;

		System.out.println("wait for message");
		while (message == null) {
			message = sqs.ReceiveMessages();
		}
		System.out.println("got message");

		Stringmessage = message.getBody();

		sqs.DeleteMessage(message);

		return Stringmessage;
	}
	
	//spliit the message from the store to products and return the shop id
	public static void splitOrderMessage(String message, ArrayList<Product> products) {
		int i;
		String[] productList = message.split(",");

		for (i = 0; i < productList.length; i++) {
			Product product = null;
			String[] SplitProductBySpace = productList[i].split(" ");
			product = CheckProductString(SplitProductBySpace);
			if (product != null) {
				products.add(product);
			}
		}
	}
    // Check if the number that is ask from the store is a number and not negative 
	public static Product CheckProductString(String[] productString) {
		Product product = null;
		int amount = -1;

		try {
			amount = Integer.parseInt(productString[1]);

		} catch (Exception e) {
			System.out.println("Not a number");
			amount = -1;

		}

		if (amount < 0) {
			
		} else {
			product = new Product(productString[0], amount);
		}

		return product;
	}

}
