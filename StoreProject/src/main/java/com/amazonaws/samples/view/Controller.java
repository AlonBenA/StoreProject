package com.amazonaws.samples.view;

import java.util.HashMap;

import com.amazonaws.samples.DynamoDBHandler;
import com.amazonaws.services.fms.model.InvalidInputException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class Controller {
	
	// static names for DynamoDBHandler
	static String region = "us-east-2";
	static String orderId = "orderId";
	static String orderContent = "orderContent";
	static String orderStatus = "status";
	static String items_table_name = "items_table";
	static String category = "category";
	static String items = "items";
	static String order_table_name = "order_table";
	static String order_status = "undone";
	static String order_keyGen = "0";
	
	DynamoDBHandler itemsDynamoDBHandler;
	DynamoDBHandler ordersDynamoDBHandler;
	
	ObservableList<String> categoriesList = FXCollections.observableArrayList("alcohol", "drinks");
	
	private HashMap<String, ObservableList<String>> map = new HashMap<String, ObservableList<String>>();
	
	@FXML
	private ComboBox<String> categoriesBox;
	
	@FXML
	private ComboBox<String> productsBox;
	
	@FXML
	private TextField qntField;
	
	@FXML
	private ListView<String> prodList;
	
	
	@FXML
	private void initialize() {
		
		itemsDynamoDBHandler = new DynamoDBHandler(region, items_table_name, null,category,items);
		ordersDynamoDBHandler = new DynamoDBHandler(region, order_table_name, null,orderId,orderContent,orderStatus);
		categoriesBox.setItems(categoriesList);
		setMap();
		
	}
	
	@FXML
	private void categoriesChoice() {
		String cat = categoriesBox.getValue();
		if(cat != null) {
			productsBox.setItems(map.get(cat));
		}
	}
	
	@FXML
	private void add() {
		try {
			int qnt = Integer.parseInt(qntField.getText());
			String prodName = productsBox.getValue();
			
			if(prodName == null)
				throw new InvalidInputException(prodName);
			// check if item already exists in the list, if it does update the quantity
			for (String str : prodList.getItems()) {
				String[] item = str.split(" ");
				System.out.println("now in item - " + str);
				if (item[0].equals(prodName)) {
					System.out.println(prodName + " already exists in the list");
					qnt += Integer.parseInt(item[1]);
					System.out.println("qnt updated");
					prodList.getItems().remove(str);
					System.out.println(str + " was removed");
					break;
				}
			}
			
			String toList = prodName + " " + qnt;
			prodList.getItems().add(toList);
			System.out.println(toList + " was added to list");
			
		}
		catch(NumberFormatException e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error!");
			alert.setContentText("Quantity must be an integer! jerk off");
			alert.showAndWait();
		}
		catch(InvalidInputException e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error!");
			alert.setContentText("You Must choose a product first");
			alert.showAndWait();
		}
	}
	
	@FXML
	private void buy() {
		String value = "";
		String key;
		int order_id = Integer.parseInt(ordersDynamoDBHandler.retrieveItemString(order_keyGen));
		System.out.println("current order keyGen at DB is: " + order_id);
		
		if(prodList.getItems().isEmpty()) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error!");
			alert.setContentText("Product list is empty!");
			alert.showAndWait();
			return;
		}
		
		for (String str : prodList.getItems()) {
			value += str + ",";
		}
		
		key = "" + (++order_id);
		
		ordersDynamoDBHandler.putOrderToTable(key, value, order_status);
		ordersDynamoDBHandler.putOrderToTable(order_keyGen, key, "keyGen");
		
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Tank you!");
		alert.setContentText("Your order has been submited!");
		alert.showAndWait();
		
		prodList.getItems().clear();
	}
	
	@FXML
	private void delete() {
		prodList.getItems().removeAll(prodList.getSelectionModel().getSelectedItems());
		
	}
	
	private void setMap() {
		 //When server is up
		for (String cat : categoriesList) {
			String str = itemsDynamoDBHandler.retrieveItemString(cat);
			ObservableList<String> productList = FXCollections.observableArrayList(str.split(","));
			map.put(cat, productList);
		}
		/*
		//meanwhile just for testing
		String str = "vodka,beer,arak,whiskey,wine";
		ObservableList<String> alcoLst = FXCollections.observableArrayList(str.split(","));
		ObservableList<String> drnkLst = FXCollections.observableArrayList("XL");
		map.put("alcohol", alcoLst);
		map.put("drinks", drnkLst);
		*/
	}

}
