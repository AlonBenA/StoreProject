package com.amazonaws.samples;

public class Product {

	private String name;
	private int amount;

	
	public Product(String name,int amount)
	{
		this.name = name;
		this.amount = amount;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Product [name=" + name + ", amount=" + amount + "]";
	}
	

}
