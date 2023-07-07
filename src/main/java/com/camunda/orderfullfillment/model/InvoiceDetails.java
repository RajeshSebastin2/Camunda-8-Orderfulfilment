package com.camunda.orderfullfillment.model;

import java.util.ArrayList;
import java.util.Date;

public class InvoiceDetails {

	private Date curruntDate;
	private String invoiceNumber;
	private BillingTo billingTo;
	private ArrayList<ProductDetailss> productList;
	private String count;
	//private String calculation;
	private String subtotal;
	private String discount;
	private String tax;
	private long total;
	
	
	
	public Date getCurruntDate() {
		return curruntDate;
	}
	public void setCurruntDate(Date curruntDate) {
		this.curruntDate = curruntDate;
	}
	public String getInvoiceNumber() {
		return invoiceNumber;
	}
	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}
	public BillingTo getBillingTo() {
		return billingTo;
	}
	public void setBillingTo(BillingTo billingTo) {
		this.billingTo = billingTo;
	}
	public ArrayList<ProductDetailss> getProductList() {
		return productList;
	}
	public void setProductList(ArrayList<ProductDetailss> productList) {
		this.productList = productList;
	}
	
	
	public String getSubtotal() {
		return subtotal;
	}
	public void setSubtotal(String subtotal) {
		this.subtotal = subtotal;
	}
	public String getDiscount() {
		return discount;
	}
	public void setDiscount(String discount) {
		this.discount = discount;
	}
	public String getTax() {
		return tax;
	}
	public void setTax(String tax) {
		this.tax = tax;
	}
	public String getCount() {
		return count;
	}
	public void setCount(String count) {
		this.count = count;
	}
	public long getTotal() {
		return total;
	}
	public void setTotal(long total) {
		this.total = total;
	}
	@Override
	public String toString() {
		return "InvoiceDetails [curruntDate=" + curruntDate + ", invoiceNumber=" + invoiceNumber + ", billingTo="
				+ billingTo + ", productList=" + productList + ", count=" + count + ", subtotal=" + subtotal
				+ ", discount=" + discount + ", tax=" + tax + ", total=" + total + "]";
	}
	
	
	
	
}
