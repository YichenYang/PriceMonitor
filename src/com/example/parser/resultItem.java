package com.example.parser;

public class resultItem {
	public String name;
	public String price;
	
	public resultItem(String _name, String _price){
		this.name = _name;
		this.price = _price;
	}
	
	public resultItem(){
		this.name = null;
		this.price = null;
	}
}
