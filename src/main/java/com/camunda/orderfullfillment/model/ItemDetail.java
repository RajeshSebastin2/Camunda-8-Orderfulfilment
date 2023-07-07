package com.camunda.orderfullfillment.model;

public class ItemDetail {

	    private String product_Name;
	    private long product_Price;
	    private int product_Qnty;
		public String getProduct_Name() {
			return product_Name;
		}
		public void setProduct_Name(String product_Name) {
			this.product_Name = product_Name;
		}
		public long getProduct_Price() {
			return product_Price;
		}
		public void setProduct_Price(long product_Price) {
			this.product_Price = product_Price;
		}
		public int getProduct_Qnty() {
			return product_Qnty;
		}
		public void setProduct_Qnty(int product_Qnty) {
			this.product_Qnty = product_Qnty;
		}
		@Override
		public String toString() {
			return "ItemDetail [product_Name=" + product_Name + ", product_Price=" + product_Price + ", product_Qnty="
					+ product_Qnty + "]";
		}
	  
	    
	    
	    
}
