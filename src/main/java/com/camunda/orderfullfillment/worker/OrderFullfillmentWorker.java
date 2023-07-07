package com.camunda.orderfullfillment.worker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.client.RestTemplate;

import com.camunda.orderfullfillment.model.CustomerOrderHistory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;

@SpringBootApplication
public class OrderFullfillmentWorker {

	final RestTemplate rest = new RestTemplate();

	@Autowired
	ZeebeClient zeebeClient;

	@Autowired
	private JavaMailSender javaMailSender;

	public long numberOfBuckets = 2;

	@ZeebeWorker(type = "bucketCreation", name = "bucketCreation")
	public void bucketCreation(JobClient client, ActivatedJob job) throws Exception {
		System.out.println("bucketCreation Worker Enter --->");
		Map<String, Object> variablesAsMap = job.getVariablesAsMap();
		Object object = variablesAsMap.get("inputVaribale");
		System.out.println("inputVaribale" + object);

		Map<String, Object> variables = new HashMap<>();
		System.out.println("enter bucket");
		List<String> bucketList = new ArrayList<String>();
		System.out.println("numberOfBuckets" + numberOfBuckets);
		for (int i = 1; i < numberOfBuckets + 1; i++) {
			bucketList.add("bucket" + i);
		}
		variables.put("bucketList", bucketList);

		client.newCompleteCommand(job.getKey()).variables(variables).send().join();
		// return variables;
	}

	@ZeebeWorker(type = "ScheduleDelivery")
	public void rejectedMail(final JobClient client, final ActivatedJob job) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();

		Map<String, Object> variables = job.getVariablesAsMap();
		System.out.println("ScheduleDelivery------------>>> Start");

		// Schedule Delivery :

		// Add 7 days from order a items :-

		LocalDateTime myDateObj = LocalDateTime.now();
		DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

		String formattedDate = myDateObj.format(myFormatObj);

		System.out.println(formattedDate + " is the date before adding days");

		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");

		// create instance of the Calendar class and set the date to the given date
		Calendar cal = Calendar.getInstance();
		cal.setTime(sdf.parse(formattedDate));

		// use add() method to add the days to the given date
		cal.add(Calendar.DAY_OF_MONTH, 7);
		String dateAfter = sdf.format(cal.getTime());

		// date after adding three days to the given date
		System.out.println(dateAfter + " is the date after adding 7 days.");

		// Your Order is scheduled on after 7 days : dateAfter

		variables.put("OrderShouldBeDeliveredON", dateAfter);

		// Status :- Scheduling Delivery Date :

		File file = new File(
				"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\CustomerOrderHistory.json");

// Create the file if it doesn't exist
		if (!file.exists()) {
			file.createNewFile();
		}
		List<CustomerOrderHistory> productList = new ArrayList<>();
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			TypeReference<List<CustomerOrderHistory>> typeReference = new TypeReference<List<CustomerOrderHistory>>() {
			};
			productList = objectMapper.readValue(fileInputStream, typeReference);
			fileInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String Status = "Scheduling Delivery Date";
		List finalist = new ArrayList<>();
		for (CustomerOrderHistory productDetails : productList) {
			String taskState = productDetails.getTaskState();
			productDetails.setTaskState(Status);
			finalist.add(productDetails);
			System.out.println(productDetails);
			break;

		}

		try {

			FileWriter fileWriter = new FileWriter(file);
			objectMapper.writeValue(fileWriter, finalist);
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		client.newCompleteCommand(job.getKey()).variables(variables).send().join();

		System.out.println("ScheduleDelivery flow end");

	}

	// SendInvoice Worker :-

	@ZeebeWorker(type = "SendInvoice", name = "Send Invoice")
	public void sendInvoiceWorker(final JobClient client, final ActivatedJob job) throws Exception {
		System.out.println("SendInvoiceWorker Worker Enter-------------------> 4");
		Map<String, Object> variablesAsMap = job.getVariablesAsMap();
		System.out.println(variablesAsMap);
		ObjectMapper objectMapper = new ObjectMapper();

		// Status Change :-
		File file = new File(
				"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\CustomerOrderHistory.json");

		// Create the file if it doesn't exist
		if (!file.exists()) {
			file.createNewFile();
		}
		List<CustomerOrderHistory> productList = new ArrayList<>();
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			TypeReference<List<CustomerOrderHistory>> typeReference = new TypeReference<List<CustomerOrderHistory>>() {
			};
			productList = objectMapper.readValue(fileInputStream, typeReference);
			fileInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String Status = "Generating Invoice Bill";
		List finalist = new ArrayList<>();
		for (CustomerOrderHistory productDetails : productList) {
			productDetails.setTaskState(Status);
			finalist.add(productDetails);
			break;

		}

		try {

			FileWriter fileWriter = new FileWriter(file);
			objectMapper.writeValue(fileWriter, finalist);
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Email Send Invoice Bill :-
		String url = "http://localhost:8080/getInvoiceDetails/rajesh";
		JSONArray jsonArray = null;
		JSONParser parser = new JSONParser();
		ResponseEntity<List> forEntity = rest.getForEntity(url, List.class);
		List responseBody = forEntity.getBody();
		String OrderShouldBeDeliveredON = (String) variablesAsMap.get("OrderShouldBeDeliveredON");
		System.out.println("OrderShouldBeDeliveredON" + OrderShouldBeDeliveredON);

//		List<InvoiceDetails> reqBodyMap = objectMapper.convertValue(responseBody,
//				new TypeReference<List<InvoiceDetails>>() {
//				});
//		
//		// Have to get product List only 
//		for(InvoiceDetails ref:reqBodyMap) {
//			Date curruntDate = ref.getCurruntDate();
//			String invoiceNumber = ref.getInvoiceNumber();
//			BillingTo billingTo = ref.getBillingTo();
//			ArrayList<ProductDetailss> productList2 = ref.getProductList();
//			String count = ref.getCount();
//			String subtotal = ref.getSubtotal();
//			String discount = ref.getDiscount();
//			String tax = ref.getTax();
//			long total = ref.getTotal();
//		}

		String str = responseBody + OrderShouldBeDeliveredON;
		String sender = "rajeshsurgetech@gmail.com";
		String receiver = "rajeshsebastin.s@gmail.com";
		String subject = "Auto-Generate Invoice Bill for Your Recent Purchase";
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setFrom(sender);
		mailMessage.setTo(receiver);
		mailMessage.setSubject(subject);

		mailMessage.setText(str);

		javaMailSender.send(mailMessage);

		variablesAsMap.put("Send Email", mailMessage);

		zeebeClient.newCompleteCommand(job.getKey()).variables(variablesAsMap).send().join();

	}

	// InformCustomer Worker :-----InformCustomer
	@ZeebeWorker(type = "InformCustomer", name = "Inform Customer")
	public void informCustomerWorker(final JobClient client, final ActivatedJob job) {
		System.out.println("InformCustomerWorker Worker Enter-------------------> 2");

		Map<String, Object> variablesAsMap = job.getVariablesAsMap();
		String stockAvailableCheck = (String) variablesAsMap.get("stockAvailable");

		if (stockAvailableCheck.equals("no")) {

			String Body = "Dear Customer,\r\n" + "\r\n"
					+ "We regret to inform you that the item you ordered is currently out of stock. Unfortunately, we are unable to fulfill your order at this time. We apologize for any inconvenience this may have caused you.\r\n"
					+ "\r\n"
					+ "Due to unforeseen circumstances, our stock levels have been depleted, and we are unable to restock the item in the near future. As a result, we have had to cancel your order.\r\n"
					+ "\r\n"
					+ "We understand that this is disappointing news, and we sincerely apologize for any inconvenience caused. We value your business and would like to offer our assistance in finding an alternative solution. Our sales team is available to help you explore other options or suggest similar products that may meet your requirements.\r\n"
					+ "\r\n"
					+ "Please feel free to contact our sales team at [insert contact details] for further assistance. We appreciate your understanding in this matter.\r\n"
					+ "\r\n"
					+ "Once again, we apologize for any inconvenience caused by the cancellation of your order. We hope to have the opportunity to serve you in the future.\r\n"
					+ "\r\n" + "Thank you for your understanding.\r\n" + "\r\n" + "Best regards,\r\n" + "Sales Team";

			String sender = "rajeshsurgetech@gmail.com";
			String receiver = "rajeshsebastin.s@gmail.com";
			String subject = "Order Status ";
			String body = Body;
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setFrom(sender);
			mailMessage.setTo(receiver);
			mailMessage.setSubject(subject);
			mailMessage.setText(body);
			javaMailSender.send(mailMessage);
			// }
			variablesAsMap.put("Send Email", mailMessage);
		}

//		String url = "http://localhost:8080/orderCancelled";
//
//		ResponseEntity<String> forEntity = rest.getForEntity(url, String.class);
//		String response = forEntity.getBody();

		zeebeClient.newCompleteCommand(job.getKey()).variables("").send().join();

	}

	// Message Throw Event :-

	@ZeebeWorker(type = "Message", name = "messageThrowEvent")
	public void messageThrowEvent(final JobClient client, final ActivatedJob job) {
		System.out.println("MessageWorker Enter-------------------> 2");

		Map<String, Object> variablesAsMap = job.getVariablesAsMap();
		String stockAvailableCheck = (String) variablesAsMap.get("stockAvailable");

		String url = "http://localhost:8080/orderCancelled";

		ResponseEntity<Object> forEntity = rest.getForEntity(url, Object.class);
		Object response = forEntity.getBody();
		System.out.println(response);

		zeebeClient.newCompleteCommand(job.getKey()).variables("").send().join();

	}

	@ZeebeWorker(type = "CancelDeliveries", name = "Cancel Deliveries")
	public void cancelDeliveriesWorker(final JobClient client, final ActivatedJob job) {
		System.out.println("CancelDeliveriesWorker Worker Enter-------------------> 3");

		zeebeClient.newCompleteCommand(job.getKey()).variables("").send().join();
	}

	// SendRemainder Worker :-

	@ZeebeWorker(type = "SendRemainder", name = "Send Remainder")
	public void sendRemainderWoker(final JobClient client, final ActivatedJob job) {
		System.out.println("SendRemainderWoker Worker Enter-------------------> 5");
		String Body = "Dear Customer,\r\n" + "\r\n"
				+ "Thank you for choosing our services. We appreciate your business.\r\n" + "\r\n"
				+ "We regret to inform you that your payment for the order is still pending. Our records indicate that the payment process was not completed successfully. To ensure the completion of your order, we kindly request you to continue with the payment process and confirm your order.\r\n"
				+ "\r\n" + "Please follow the instructions below to complete your payment:\r\n" + "\r\n"
				+ "Visit our website [insert website URL] and log into your account.\r\n"
				+ "Navigate to the \"Payment\" section or \"My Orders\" section.\r\n"
				+ "Locate your pending order and click on the \"Continue Payment\" or \"Complete Order\" button.\r\n"
				+ "Follow the on-screen instructions to proceed with the payment and confirm your order.\r\n"
				+ "If you encounter any difficulties during the payment process or have any questions regarding your order, please don't hesitate to contact our customer support team at [insert customer support contact details]. We will be happy to assist you.\r\n"
				+ "\r\n"
				+ "Please note that your order will remain on hold until the payment is successfully completed. Failure to complete the payment within [insert time frame] may result in the cancellation of your order.\r\n"
				+ "\r\n"
				+ "We apologize for any inconvenience caused and appreciate your prompt attention to this matter. Thank you for your cooperation.\r\n"
				+ "\r\n" + "Best regards,\r\n" + "Sales Team\r\n" + "\r\n" + "\r\n" + "\r\n" + "\r\n" + "";

		String sender = "rajeshsurgetech@gmail.com";
		String receiver = "rajeshsebastin.s@gmail.com";
		String subject = "Payment Pending ";
		String body = Body;
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setFrom(sender);
		mailMessage.setTo(receiver);
		mailMessage.setSubject(subject);
		mailMessage.setText(body);
		javaMailSender.send(mailMessage);
		// }
		zeebeClient.newCompleteCommand(job.getKey()).variables("").send().join();

	}

	// This email is auto generated to payment remaining
	// Your payment is not completed. continue your payment confirm your order
}
