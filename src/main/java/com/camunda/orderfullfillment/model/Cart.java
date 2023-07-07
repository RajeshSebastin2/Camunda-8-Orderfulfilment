package com.camunda.orderfullfillment.model;

import java.util.List;

public class Cart {
	
	private String cartid;
	private String userid;
	private List<ProductDetailss> productList;
	private int count;

	public String getCartid() {
		return cartid;
	}
	public void setCartid(String cartid) {
		this.cartid = cartid;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public List<ProductDetailss> getProductList() {
		return productList;
	}
	public void setProductList(List<ProductDetailss> productList) {
		this.productList = productList;
	}
	@Override
	public String toString() {
		return "Cart [cartid=" + cartid + ", userid=" + userid + ", productList=" + productList + ", count=" + count
				+ "]";
	}
	
	
}
