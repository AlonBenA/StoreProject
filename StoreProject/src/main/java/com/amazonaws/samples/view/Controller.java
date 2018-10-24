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
	
	
	static String region = "us-east-2";
	
	// columns name of tables
	static String orderId = "orderId";
	static String orderContent = "orderContent";
	static String orderStatus = "status";
	
	static String items_table_name = "items_table";
	
	static String category = "category";
	static String items = "items";
			
			//tables names
			static String order_table_name = "order_table";
	
	DynamoDBHandler myDynamoDBHandler;
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
		
		myDynamoDBHandler = new DynamoDBHandler(region, items_table_name, null,category,items);
		
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
			if(productsBox.getValue()==null)
				throw new InvalidInputException(productsBox.getValue());
			
			String toList = productsBox.getValue() + " " + qnt;
			prodList.getItems().add(toList);
			
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
			alert.setContentText("you nust choose a product first");
			alert.showAndWait();
		}
	}
	
	@FXML
	private void buy() {
		
	}
	
	@FXML
	private void delete() {
		
	}
	
	private void setMap() {
		 //When server is up
		for (String cat : categoriesList) {
			String str = myDynamoDBHandler.retrieveItemString(cat);
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
