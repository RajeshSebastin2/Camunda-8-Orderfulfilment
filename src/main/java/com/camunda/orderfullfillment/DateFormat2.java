package com.camunda.orderfullfillment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class DateFormat2 {
	public static void main(String[] args) {

		// local time :

		LocalDateTime myDateObj = LocalDateTime.now();
		DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

		String formattedDate = myDateObj.format(myFormatObj);


		System.out.println(formattedDate + " is the date before adding days");

		
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");

		// create instance of the Calendar class and set the date to the given date
		Calendar cal = Calendar.getInstance();
		try {
			
			cal.setTime(sdf.parse(formattedDate));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		// use add() method to add the days to the given date
		cal.add(Calendar.DAY_OF_MONTH, 7);
		String dateAfter = sdf.format(cal.getTime());

		// date after adding three days to the given date
		System.out.println(dateAfter + " is the date after adding 7 days.");

	}

}
