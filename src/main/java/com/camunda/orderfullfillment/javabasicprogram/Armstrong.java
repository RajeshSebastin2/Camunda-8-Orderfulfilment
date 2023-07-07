package com.camunda.orderfullfillment.javabasicprogram;



public class Armstrong {

	public static void main(String[] args) {
  
		
		int  count=153, r=0, sum=0, n1=count;
		
		while(count!=0) {
			r=count%10;
			sum=sum+(r*r*r);
			count=count/10;
		}
		if(n1==sum) {
			System.out.println("Armstrong number");
		}else {
			System.out.println("not");
		}
		
	}

}
