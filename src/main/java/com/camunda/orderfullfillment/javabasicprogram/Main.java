package com.camunda.orderfullfillment.javabasicprogram;

 class Singleton {

	private static Singleton instance;

	private Singleton() {
		
	}
	
	public static Singleton getA(){  
		  return instance;  
		 }  
	
	
	public static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
	
	
	 public void doSomething() {
	        System.out.println("Singleton instance is doing something.");
	    }
	 
	 
}

public class Main{
	public static void main(String[] args) {
		
		Singleton singleton1 = Singleton.getInstance();
		
		
		
		Singleton singleton2 = Singleton.getInstance();
		
		System.out.println(singleton1.equals(singleton2));

        // Both instances are the same
        System.out.println(singleton1 == singleton2);  // Output: true

        singleton1.doSomething();
	}
	
	 

}
