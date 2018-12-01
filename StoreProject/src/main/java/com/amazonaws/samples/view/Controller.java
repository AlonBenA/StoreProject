package com.amazonaws.samples.view;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;
import com.amazonaws.samples.DynamoDBHandler;
import com.amazonaws.samples.S3Handler;
import com.amazonaws.services.fms.model.InvalidInputException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

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
	static String bucketName = "afekapicturebucketalonitayoran";
	
	private DynamoDBHandler itemsDynamoDBHandler;
	private DynamoDBHandler ordersDynamoDBHandler;
	private S3Handler S3HproductImages;
	ObservableList<String> categoriesList = FXCollections.observableArrayList("alcohol", "drinks");
	
	private HashMap<String, ObservableList<String>> productsByCategoryMap = new HashMap<String, ObservableList<String>>();

	@FXML
	private ComboBox<String> categoriesBox;
	
	@FXML
	private ComboBox<String> productsBox;
	
	@FXML
	private TextField qntField;
	
	@FXML
	private ListView<String> prodList;
	
	@FXML
	private ImageView imageView;
	
	
	@FXML
	private void initialize() {
		
		itemsDynamoDBHandler = new DynamoDBHandler(region, items_table_name, category,items);
		ordersDynamoDBHandler = new DynamoDBHandler(region, order_table_name, orderId,orderContent,orderStatus);
		S3HproductImages = new S3Handler(region, bucketName);
		categoriesBox.setItems(categoriesList);
		initMap();
		
		
	}
	
	@FXML
	private void categoriesChoice() {
		String cat = categoriesBox.getValue();
		if(cat != null) {
			productsBox.setItems(productsByCategoryMap.get(cat));
		}
	}
	
	@FXML
	private void productChoide() {
		try {
			String choice = productsBox.getValue();
			if(choice!=null) {
				BufferedImage bufferedImage = ImageIO.read(S3HproductImages.getItem(choice).getObjectContent());
				Image image = SwingFXUtils.toFXImage(bufferedImage, null);
				imageView.setImage(image);
			}
			
		} catch (IOException e) {
			System.err.println("Failed to read Image: " + productsBox.getValue());
			e.printStackTrace();
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
			alert.setContentText("Quantity must be an integer!");
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
		System.out.println("order_keyGen: " + order_keyGen);
		
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
	
	private void initMap() {
		 
		for (String cat : categoriesList) {
			String str = itemsDynamoDBHandler.retrieveItemString(cat);
			ObservableList<String> productList = FXCollections.observableArrayList(str.split(","));
			productsByCategoryMap.put(cat, productList);
			
		}
	
	}

}
