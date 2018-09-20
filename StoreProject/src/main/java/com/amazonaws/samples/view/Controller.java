package com.amazonaws.samples.view;

import java.util.HashMap;

import com.amazonaws.samples.DynamoDBHandler;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class Controller {
	
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
	private void initialize() {
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
			
		}
		catch(NumberFormatException e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error!");
			alert.setContentText("Quantity must be an integer! jerk off");
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
		/* When server is up
		for (String cat : categoriesList) {
			String str = myDynamoDBHandler.retrieveItemString(cat);
			ObservableList<String> productList = FXCollections.observableArrayList(str.split(","));
			map.put(cat, productList);
		}
		*/
		//meanwhile just for testing
		String str = "vodka,beer,arak,whiskey,wine";
		ObservableList<String> alcoLst = FXCollections.observableArrayList(str.split(","));
		ObservableList<String> drnkLst = FXCollections.observableArrayList("XL");
		map.put("alcohol", alcoLst);
		map.put("drinks", drnkLst);
	}

}
