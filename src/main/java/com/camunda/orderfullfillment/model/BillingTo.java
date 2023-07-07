package com.camunda.orderfullfillment.model;

public class BillingTo {

	    private String address;
	    private String email;
	    private String contactNum;
	    
	    public String getAddress() {
			return address;
		}
		public void setAddress(String address) {
			this.address = address;
		}
		public String getEmail() {
			return email;
		}
		public void setEmail(String email) {
			this.email = email;
		}
		public String getContactNum() {
			return contactNum;
		}
		public void setContactNum(String contactNum) {
			this.contactNum = contactNum;
		}
		@Override
		public String toString() {
			return "BillingTo [address=" + address + ", email=" + email + ", contactNum=" + contactNum + "]";
		}
		
}
