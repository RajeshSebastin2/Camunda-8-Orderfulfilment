package com.camunda.orderfullfillment.javabasicprogram;

public class StringEqualExample {

	public static void main(String[] args) {
		        String s1 = "HELLO";
		        String s2 = "hello";
		        String s3 =  new String("HELLO");
		        
		        String s4 = new String("HELLO");
		        
		        //System.out.println(s3.equals(s4));
		 
		        System.out.println(s1 == s2); // true
		        System.out.println(s1 == s3); // false
		        System.out.println(s1.equals(s2)); // true
		        System.out.println(s1.equals(s3)); // true
	}
}
