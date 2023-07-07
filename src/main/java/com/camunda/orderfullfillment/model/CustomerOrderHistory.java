package com.camunda.orderfullfillment.model;

import java.util.ArrayList;
import java.util.Date;

public class CustomerOrderHistory {

	
	 private ArrayList<Variable> variables;
	 private String taskState;
	 private Date creationTime;
	 private String id;
	public ArrayList<Variable> getVariables() {
		return variables;
	}
	public void setVariables(ArrayList<Variable> variables) {
		this.variables = variables;
	}
	public String getTaskState() {
		return taskState;
	}
	public void setTaskState(String taskState) {
		this.taskState = taskState;
	}
	public Date getCreationTime() {
		return creationTime;
	}
	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Override
	public String toString() {
		return "CustomerOrderHistory [variables=" + variables + ", taskState=" + taskState + ", creationTime="
				+ creationTime + ", id=" + id + "]";
	}
	 
	 
	 
}
