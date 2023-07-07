package com.camunda.orderfullfillment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateFormat {
	public static void main(String[] args) {
		   LocalDateTime myDateObj = LocalDateTime.now(); 
		
DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MMM-yyyy");  
	    
	    String formattedDate = myDateObj.format(myFormatObj);  
	    
	  //  myFormatObj.parse(dateBefore);
	    
	    
	    
	    System.out.println("After Formatting: " + formattedDate); 
	}
	 

}
