package com.camunda.orderfullfillment.model;

import java.util.ArrayList;

public class Variable {

	private String id;
	private String name;
	private ArrayList<Value> value;
	private String type;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<Value> getValue() {
		return value;
	}
	public void setValue(ArrayList<Value> value) {
		this.value = value;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	@Override
	public String toString() {
		return "Variable [id=" + id + ", name=" + name + ", value=" + value + ", type=" + type + "]";
	}
}
