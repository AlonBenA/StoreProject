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
		workingShop();
	}

	private static void workingShop() {
		int choise = 1;

		System.out.println("wellcome to store program");
		System.out.println("-------------------------------");
		System.out.println("1) add products to shop inventory");
		System.out.println("2) start prepare orders");
		System.out.println("enter your choise:");

		try {
			choise =  Integer.parseInt(sc.nextLine());

			if (choise == 1) {
				addProductsToShopInventory();
				System.out.println("shop Inventory ready");
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

	private static void addProductsToShopInventory() {
		DynamoDBHandler shopInventory = connectToShopInventory();
		ArrayList<Product> Products = new ArrayList<Product>();
		// [0] name [1] amount
		String[] newProduct = new String[2];
		int numOfProducts = 0;
		System.out.println("how much products do you want to add the inventory? (number > 0)");
		try {
			numOfProducts = sc.nextInt();
			if (numOfProducts > 0) {
				for (int i = 0; i < numOfProducts; i++) {
					sc.nextLine();
					System.out.println("enter product name");
					newProduct[0] = sc.nextLine();
					System.out.println("enter product amount");
					newProduct[1] = sc.nextLine();
					Products.add(CheckProductString(newProduct));
				}
			} else {
				System.out.println("num Of Products not valid must > 0 ");
			}
		} catch (Exception e) {
			System.out.println("Exception it's not number");
		}
		for (Product p : Products) {
			int currentAmount = shopInventory.retrieveItemAmount(p.getName());
			shopInventory.putItem(p.getName(), p.getAmount() + currentAmount);
		}
	}

	private static void Shop() {
		int continueWork = 1;
		while (continueWork == 1) {
			String orderId = null;
			String orderContent = null;
			String newStatusOrder = null;
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

			// get items order from inventory
			newStatusOrder = PrepareOrder(shopInventory, orderProducts, sqsShortageHandler);

			// update status
			order_table.putOrderToTable(orderId, orderContent, newStatusOrder);

			if (orderProducts.size() > 0) {
				orderProducts.clear();
			}
			continueWork = continuePrepareOrder();
		}
	}

	private static int continuePrepareOrder() {
		int val = -1;
		System.out.println("continue PrepareOrder:");
		System.out.println("1) yes");
		System.out.println("0) no");
		while (val < 0 || val > 1) {
			try {
				val = sc.nextInt();

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
			if (orderAmount < currentAmount) {
				currentAmount -= orderAmount;
				shopInventory.putItem(nameProduct, orderAmount);
			} else {
				// Example message
				// String message = "0,vodka 5,XL 5,beer 12,";
				shortageMessege += nameProduct + " " + currentAmount + ",";
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
		DynamoDBHandler OrderTable = new DynamoDBHandler(region, order_table_name, credentialsProvider, orderId,
				orderContent, orderStatus);

		return OrderTable;
	}

	public static DynamoDBHandler connectToShopInventory() {
		String itemName = "itemName";
		String amount = "amount";

		DynamoDBHandler shopsInventory = new DynamoDBHandler(region, table_name + shopId, credentialsProvider, itemName,
				amount);

		return shopsInventory;
	}

	public static String getMessage(SQSHandler sqs) {
		// Example message
		// String message = "0,vodka 5,XL 5,beer 12,";
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

}
