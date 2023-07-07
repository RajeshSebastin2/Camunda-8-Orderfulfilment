package com.camunda.orderfullfillment;

public class ListValidationExample {
    public static void main(String[] args) {
    	
    	//Scanner sc=new Scanner(System.in);
    	
    	
    	
    	int a[]= {10,20,30,40,50},value=50,pos=2;
    	
    	int max=a[0],temp;
    	
    	for(int i=0;i<a.length;i++)
    	{
    	
    		if(max<a[i])
    		{
    			temp=a[i];
    			a[i]=max;
    			max=temp;
    		}
    		
    	}
    	System.out.println(max);
    
    }
}
  

