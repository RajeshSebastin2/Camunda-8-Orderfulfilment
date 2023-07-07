package com.camunda.orderfullfillment.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletResponse;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.camunda.orderfullfillment.model.BillingTo;
import com.camunda.orderfullfillment.model.Cart;
import com.camunda.orderfullfillment.model.CustomerDetails;
import com.camunda.orderfullfillment.model.CustomerOrderHistory;
import com.camunda.orderfullfillment.model.InvoiceDetails;
import com.camunda.orderfullfillment.model.ProductDetailss;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.auth.SaasAuthentication;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.dto.TaskState;
import io.camunda.tasklist.dto.Variable;
import io.camunda.tasklist.dto.VariableType;
import io.camunda.tasklist.exception.TaskListException;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;

@RestController
public class OrderFulfilmentController {

//	@Value("${multi-instance-example.number-of-buckets}")
//	public long numberOfBuckets;
//
//	@Value("${multi-instance-example.number-of-elements}")
//	public long numberOfElements;

	@Autowired
	ZeebeClient zeebeClient;

	@Autowired
	ProductDetailss ds;

	ObjectMapper objectMapper = new ObjectMapper();

	final RestTemplate rest = new RestTemplate();

	@GetMapping("/test")
	public String demo() {
		return "Working";

	}

	// All API's :

	// OrderFulfilment API :-

	// 1.

	// ****************************** Start_WorkFlow_API's
	// ****************************

	@CrossOrigin
	@RequestMapping(value = "/submitProductList", method = RequestMethod.POST, headers = "Accept=*/*", produces = "application/json", consumes = "application/json")
	public Map<String, Object> startWorkflow(@RequestBody String reqBody) throws TaskListException {

		Map inputVaribale = new HashMap<>();

		SaasAuthentication sa = new SaasAuthentication("zCCx7DH6.mn_tGC5O1mNrPg-q6qVNsMg",
				"u4Y-WP8SmmVik3E.PJ.iE0-09wSX_rj5LLGSKVhL~Y23ZcXP_QXPgnPiAVCcezM_");

		CamundaTaskListClient client = new CamundaTaskListClient.Builder()
				.taskListUrl("https://bru-2.tasklist.camunda.io/1a8d8e18-4054-4bd2-afad-6f2adf8c58b8")
				.shouldReturnVariables().authentication(sa).build();

		try {
			List<ProductDetailss> reqBodyMap = objectMapper.readValue(reqBody,
					new TypeReference<List<ProductDetailss>>() {
					});
			inputVaribale.put("inputVaribale", reqBodyMap);

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		ProcessInstanceEvent processInstEvnt = zeebeClient.newCreateInstanceCommand().bpmnProcessId("OrderFullfillment")
				.latestVersion().variables(inputVaribale).send().join();

		long pID = processInstEvnt.getProcessInstanceKey();

		// long processDefinitionKey = processInstEvnt.getProcessDefinitionKey();

		Map<String, Object> processIdMap = new HashMap<String, Object>();
		processIdMap.put("processInstanceKey", pID);
		return processIdMap;

	}

	@GetMapping("/download")
	public void downloadInvoicePdf(HttpServletResponse response) throws IOException {
		// Set the response headers
		response.setContentType(MediaType.APPLICATION_PDF_VALUE);
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice.pdf");

		// Create a new PDF document
		PDDocument document = new PDDocument();
		PDPage page = new PDPage();
		document.addPage(page);

		// Write content to the PDF document
		PDPageContentStream contentStream = new PDPageContentStream(document, page);
		contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
		contentStream.beginText();
		contentStream.newLineAtOffset(100, 700);
		contentStream.showText("Invoice content goes here");
		contentStream.endText();
		contentStream.close();

		// Write the PDF document to the response output stream
		document.save(response.getOutputStream());

		// Close the PDF document
		document.close();

		// Flush and close the response output stream
		response.getOutputStream().flush();
		response.getOutputStream().close();
	}

	// send invoice proper format :-

	@GetMapping("/sendInvoiceProperFormat")
	public List sendInvoiceProperFormat() {
		String url = "http://localhost:8080/getInvoiceDetails/rajesh";
		JSONArray jsonArray = null;
		JSONParser parser = new JSONParser();
		ResponseEntity<List> forEntity = rest.getForEntity(url, List.class);
		List responseBody = forEntity.getBody();
		// String OrderShouldBeDeliveredON = (String)
		// variablesAsMap.get("OrderShouldBeDeliveredON");
		// System.out.println("OrderShouldBeDeliveredON" + OrderShouldBeDeliveredON);

		List<InvoiceDetails> reqBodyMap = objectMapper.convertValue(responseBody,
				new TypeReference<List<InvoiceDetails>>() {
				});

		List finalList = new ArrayList<>();
		// Have to get product List only
		for (InvoiceDetails ref : reqBodyMap) {
			Map mp = new HashMap<>();
			Date curruntDate = ref.getCurruntDate();
			String invoiceNumber = ref.getInvoiceNumber();
			BillingTo billingTo = ref.getBillingTo();
			ArrayList<ProductDetailss> productList2 = ref.getProductList();
			String count = ref.getCount();
			String subtotal = ref.getSubtotal();
			String discount = ref.getDiscount();
			String tax = ref.getTax();
			long total = ref.getTotal();
			mp.put("curruntDate", curruntDate);
			mp.put("invoiceNumber", invoiceNumber);
			mp.put("billingTo", billingTo);
			mp.put("productList", productList2);
			mp.put("count", count);
			mp.put("subtotal", subtotal);
			mp.put("discount", discount);
			mp.put("tax", tax);
			mp.put("total", total);
			finalList.add(mp);
		}
		return finalList;
	}

	// Send invoice page to customer through mail
	// It is working :-

	@GetMapping("/page")
	public String page() {

		final String senderEmail = "rajeshsurgetech@gmail.com";
		final String senderPassword = "hqcelookkyynwjoa";
		String recipientEmail = "rajeshsebastin.s@gmail.com";
		String subject = "Auto-Generate Invoice Bill for Your Recent Purchase";
		// String body = "Please find the attached PDF file.";

		String body = "Dear Rajesh,\r\n" + "\r\n"
				+ "I hope this email finds you well. Thank you for choosing our website for your recent purchase. We appreciate your business and aim to provide you with the best possible service.\r\n"
				+ "\r\n"
				+ "We are pleased to inform you that our sales team has prepared the invoice for your purchased item(s), which is attached to this email. The invoice includes comprehensive details about the products/services you have selected, along with their respective costs and any applicable taxes.\r\n"
				+ "\r\n"
				+ "Please take a moment to review the attached invoice. If you find that all the information is correct and in order, we kindly request that you proceed with the payment as per the instructions provided on the invoice.\r\n"
				+ "\r\n"
				+ "Should you have any queries, concerns, or require further clarification regarding the invoice or the payment process, please do not hesitate to contact our dedicated customer support team at [customer support contact details]. We are here to assist you and ensure a smooth experience.\r\n"
				+ "\r\n"
				+ "We appreciate your cooperation in promptly attending to this matter, as it will facilitate the completion of the transaction smoothly. Your satisfaction is of utmost importance to us, and we look forward to serving you again in the future.\r\n"
				+ "\r\n" + "Here we attached the invoice bill in this email.\r\n"
				+ "Kindly please check the attachment\r\n" + "\r\n"
				+ "Thank you for choosing our website for your purchase.\r\n" + "\r\n" + "Warm regards," + "\r\n"
				+ "Sales Team.";

		// Set up mail server properties
		Properties properties = new Properties();
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.port", "587");
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");

		// Create a session with authentication
		Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(senderEmail, senderPassword);
			}
		});

		try {
			// Create a new message
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(senderEmail));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
			message.setSubject(subject);

			// Create the message body
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(body);

			// Attach the PDF file
			MimeMultipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);

			// Path to the PDF file
			String filePath = "C:\\Users\\STS123\\Downloads\\Resume.pdf";

			// Attach the PDF file from the specified folder
			messageBodyPart = new MimeBodyPart();
			DataSource source = new FileDataSource(filePath);
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName(source.getName());
			multipart.addBodyPart(messageBodyPart);

			// Set the content of the message
			message.setContent(multipart);

			// Send the message
			Transport.send(message);

			System.out.println("Email sent successfully.");
		} catch (MessagingException e) {
			e.printStackTrace();
		}

		return "page";
	}

	// 2.

//	****************************** Get_Active_UserTask_List ****************************	

	//////////////////////// Get Active User Task List //////////////////////

	@GetMapping("/getActivedTaskList")
	public List<Task> getActivedTaskList() throws TaskListException {

		SaasAuthentication sa = new SaasAuthentication("zCCx7DH6.mn_tGC5O1mNrPg-q6qVNsMg",
				"u4Y-WP8SmmVik3E.PJ.iE0-09wSX_rj5LLGSKVhL~Y23ZcXP_QXPgnPiAVCcezM_");

		CamundaTaskListClient client = new CamundaTaskListClient.Builder()
				.taskListUrl("https://bru-2.tasklist.camunda.io/1a8d8e18-4054-4bd2-afad-6f2adf8c58b8")
				.shouldReturnVariables().authentication(sa).build();

		return client.getTasks(true, TaskState.CREATED, 50, true);

	}

	// This API is TaskState is Created OrderHistory API Currently USE :

	// Filter By AssigneeName & ProcessName & Instance ID

	@GetMapping("/orderHistory/{assingneeName}/{processName}/{instanceId}")
	public List orderHistory(@PathVariable String assingneeName, @PathVariable String processName,
			@PathVariable String instanceId) throws Exception {

		File file = new File(
				"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\CustomerOrderHistory.json");

		// Create the file if it doesn't exist
		if (!file.exists()) {
			file.createNewFile();
		}
		// Read the existing data from the file into a list
		List productList = new ArrayList<>();
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			TypeReference<List> typeReference = new TypeReference<List>() {
			};
			productList = objectMapper.readValue(fileInputStream, typeReference);
			fileInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		SaasAuthentication sa = new SaasAuthentication("zCCx7DH6.mn_tGC5O1mNrPg-q6qVNsMg",
				"u4Y-WP8SmmVik3E.PJ.iE0-09wSX_rj5LLGSKVhL~Y23ZcXP_QXPgnPiAVCcezM_");

		CamundaTaskListClient client = new CamundaTaskListClient.Builder()
				.taskListUrl("https://bru-2.tasklist.camunda.io/1a8d8e18-4054-4bd2-afad-6f2adf8c58b8")
				.shouldReturnVariables().authentication(sa).build();

		List<Task> assigneeTasks = client.getTasks(true, TaskState.CREATED, 50, true);

		List sizelist = new ArrayList<>();
		List filterByAssigneeList = new ArrayList();
		int size = assigneeTasks.size();
		for (Task getprocessNameList : assigneeTasks) {
			String assignee = getprocessNameList.getAssignee();
			if (assingneeName.equals(assignee)) {
				String processNam = getprocessNameList.getProcessName();

				if (processName.equals(processNam)) {

					List<Variable> variables = getprocessNameList.getVariables();
					List variableList = new ArrayList<>();
					List totalPriceAmount = new ArrayList<>();
					// Map mpTotal = new HashMap<>();
					int size1 = variables.size();

					for (Variable getVariableList : variables) {

						String id = getVariableList.getId();

						String[] str = id.split("-");

						String getId = str[0];

						if (instanceId.equals(getId)) {
							int sum = 0;

							sizelist.add(getprocessNameList);
							int size2 = sizelist.size();
							VariableType type = getVariableList.getType();
							String name = type.name();
							if (name.equals("LIST")) {
								Object value = getVariableList.getValue();
								List lst = (List) value;
								for (Object reference : lst) {
									Map mp = (Map) reference;
									Object object = mp.get("product_Price");
									int i = (int) object;
									sum = sum + i;
								}

							}
							Map ml = new HashMap<>();
							variableList.add(getVariableList);
							String id2 = getprocessNameList.getId();
							String creationTime = getprocessNameList.getCreationTime();
							DateFormat formatter6 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
							Date date6 = formatter6.parse(creationTime);
							String str1 = formatter6.format(date6);
							TaskState taskState = getprocessNameList.getTaskState();
							String state = "Checking Stock Availability";
							List<Variable> variables2 = getprocessNameList.getVariables();
							ml.put("id", id2);
							ml.put("creationTime", str1);
							ml.put("taskState", state);
							ml.put("variables", variableList);
							ml.put("totalPrice", sum);
							filterByAssigneeList.add(ml);

						}

					}
				}

			}

		}

		// my logics :-
		try {

			FileWriter fileWriter = new FileWriter(file);
			objectMapper.writeValue(fileWriter, filterByAssigneeList);
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return filterByAssigneeList;
	}

	// GetOrderHistoryDetails:
	@GetMapping("/getOrderHistoryDetails")
	public JSONArray getOrderHistoryDetails() {
		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(new FileReader(
					"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\CustomerOrderHistory.json"));
			JSONArray jsonObject = (JSONArray) obj;
			return jsonObject;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// It is for ScheduleDelivery Worker :

	@GetMapping("/scheduleDelivery")
	public List ScheduleDelivery() throws Exception {

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
		return finalist;
	}

	// 3.
	// ****************************** Complete_UserTask ****************************

	// Complete User Task :-

	@CrossOrigin
	@PostMapping("/completeTaskWithInstanceId/{processInstanceKey}")
	public String completeTaskWithInstanceId(@PathVariable String processInstanceKey, @RequestBody Map variable)
			throws Exception {

		String activeUrl = "http://localhost:8080/getActivedTaskList";
		ResponseEntity<List> getActiveTaskList = rest.getForEntity(activeUrl, List.class);

		Map mp = new HashMap();
		List activeTaskList = getActiveTaskList.getBody();

		List finalJobkey = new ArrayList();

		for (Object getTraceId : activeTaskList) {

			Map activeTaskMap = (Map) getTraceId;

			List<Object> getVariableList = (List<Object>) activeTaskMap.get("variables");

			if (getVariableList != null) {

				for (Object getVariable : getVariableList) {

					Map getVariableMap = (Map) getVariable;

					String getIds = (String) getVariableMap.get("id");

					String[] str = getIds.split("-");

					String stringGetprocessInstanceKey = str[0];

					if (processInstanceKey.equals(stringGetprocessInstanceKey)) {

					}

					String jobKey = (String) activeTaskMap.get("id");

					finalJobkey.add(jobKey);

				}
			}

		}

		String jobKey = (String) finalJobkey.get(0);

		SaasAuthentication sa = new SaasAuthentication("zCCx7DH6.mn_tGC5O1mNrPg-q6qVNsMg",
				"u4Y-WP8SmmVik3E.PJ.iE0-09wSX_rj5LLGSKVhL~Y23ZcXP_QXPgnPiAVCcezM_");

		CamundaTaskListClient client = new CamundaTaskListClient.Builder()
				.taskListUrl("https://bru-2.tasklist.camunda.io/1a8d8e18-4054-4bd2-afad-6f2adf8c58b8")
				.shouldReturnVariables().authentication(sa).build();

		Task task = client.completeTask(jobKey, variable);

		return " User Task Completed Successfully ";

	}

	// get

	// getActiveTaskByUser(String userId) :-

//	@CrossOrigin
//	@GetMapping("/getActiveTaskByUser/{userId}")
//	public String getActiveTaskByUser(@PathVariable String userId)
//			throws Exception {
//
//		String activeUrl = "http://localhost:8080/getActivedTaskList";
//		ResponseEntity<List> getActiveTaskList = rest.getForEntity(activeUrl, List.class);
//
//		Map mp = new HashMap();
//		List activeTaskList = getActiveTaskList.getBody();
//
//		List finalJobkey = new ArrayList();
//
//		for (Object getTraceId : activeTaskList) {
//
//			Map activeTaskMap = (Map) getTraceId;
//
//			List<Object> getVariableList = (List<Object>) activeTaskMap.get("variables");
//
//			if (getVariableList != null) {
//
//				for (Object getVariable : getVariableList) {
//
//					Map getVariableMap = (Map) getVariable;
//
//					String getIds = (String) getVariableMap.get("id");
//
//					String[] str = getIds.split("-");
//
//					String stringGetprocessInstanceKey = str[0];
//
//					if (processInstanceKey.equals(stringGetprocessInstanceKey)) {
//
//					}
//
//					String jobKey = (String) activeTaskMap.get("id");
//
//					finalJobkey.add(jobKey);
//
//				}
//			}
//
//		}
//
//		String jobKey = (String) finalJobkey.get(0);
//
//		SaasAuthentication sa = new SaasAuthentication("CwDKV~wHD2FcVeHiA_Nvcl.GSVsQdFSf",
//				"qY.Cw2iy52u7KSqumTNcjlOKiBnPK_68qpQtDKK_8SpEiG.WthMXAv2~fZIdT9v2");
//
//		CamundaTaskListClient client = new CamundaTaskListClient.Builder()
//				.taskListUrl("https://bru-2.tasklist.camunda.io/1a8d8e18-4054-4bd2-afad-6f2adf8c58b8")
//				.shouldReturnVariables().authentication(sa).build();
//
//		Task task = client.completeTask(jobKey, variable);
//
//		return " User Task Completed Successfully ";
//
//	}

	// 4.

	// ****************************** Get_All_Variable ****************************

	// getVariable :-

	@PostMapping("/getVariable/{processInstanceKey}")
	public List getVariable(@PathVariable String processInstanceKey) {

		// All Activated Task :-
		// String activeUrl = "http://localhost:8080/getActivedTaskList";

		// particular User or Admin & processName :-
		String activeUrl = "http://localhost:8080/getActivedTaskList";

		ResponseEntity<List> getActiveTaskList = rest.getForEntity(activeUrl, List.class);

		Map mp = new HashMap();
		List finalInsuranceVaiableList = new ArrayList();
		List activeTaskList = getActiveTaskList.getBody();

		for (Object getTraceId : activeTaskList) {

			Map activeTaskMap = (Map) getTraceId;

			List<Object> getVariableList = (List<Object>) activeTaskMap.get("variables");

			for (Object getVariable : getVariableList) {

				Map getVariableMap = (Map) getVariable;

				String getIds = (String) getVariableMap.get("id");

				String[] str = getIds.split("-");

				String stringGetprocessInstanceKey = str[0];

				if (processInstanceKey.equals(stringGetprocessInstanceKey)) {

					Object object = getVariableMap.get("value");
					List obj = (List) object;
					for (Object variable : obj) {

						finalInsuranceVaiableList.add(variable);

					}

				}

			}

		}

		return finalInsuranceVaiableList;

	}

	///// PP :-

	@CrossOrigin()
	@GetMapping("/getVariablep/{processInstanceKey}")

	public List getVariablepp(@PathVariable String processInstanceKey) {

		String activeUrl = "http://localhost:8080/getActivedTaskList";

		ResponseEntity<List> getActiveTaskList = rest.getForEntity(activeUrl, List.class);

		Map mp = new HashMap();

		List finalInsuranceVaiableList = new ArrayList();

		List activeTaskList = getActiveTaskList.getBody();

		for (Object getTraceId : activeTaskList) {

			Map activeTaskMap = (Map) getTraceId;

			List<Object> getVariableList = (List<Object>) activeTaskMap.get("variables");

			for (Object getVariable : getVariableList) {

				Map getVariableMap = (Map) getVariable;

				String getIds = (String) getVariableMap.get("id");

				String[] str = getIds.split("-");

				String stringGetprocessInstanceKey = str[0];

				if (processInstanceKey.equals(stringGetprocessInstanceKey)) {

					Object object = getVariableMap.get("value");

					finalInsuranceVaiableList.add(object);

				}

			}

		}

		return finalInsuranceVaiableList;

	}

	// Extra API's

	// Assigned TaskBy Admin :-

	@CrossOrigin
	@GetMapping("/getAssignedTask/{processName}/{adminName}")
	public List<Task> getAssignedTask(@PathVariable String processName, @PathVariable String adminName)
			throws TaskListException {

		SaasAuthentication sa = new SaasAuthentication("zCCx7DH6.mn_tGC5O1mNrPg-q6qVNsMg",
				"u4Y-WP8SmmVik3E.PJ.iE0-09wSX_rj5LLGSKVhL~Y23ZcXP_QXPgnPiAVCcezM_");

		CamundaTaskListClient client = new CamundaTaskListClient.Builder().authentication(sa)
				.taskListUrl("https://bru-2.tasklist.camunda.io/1a8d8e18-4054-4bd2-afad-6f2adf8c58b8/").build();

		List<Task> assigneeTasks = client.getAssigneeTasks(adminName, TaskState.CREATED, 50, true);

		List ls = new ArrayList<>();
		List variable = new ArrayList<>();

		Map mp = new HashMap<>();
		for (Task processNameList : assigneeTasks) {
			String string = processNameList.toString();
			String processNames = processNameList.getProcessName();
			if (processNames.equals(processName)) {
				// ls.add(processNameList);

				String id = processNameList.getId();
				String name = processNameList.getName();
				TaskState taskState = processNameList.getTaskState();
				List<Variable> variables = processNameList.getVariables();

				// iterating variable :-
				for (Variable variablesCart : variables) {
					VariableType type = variablesCart.getType();
					// VariableType type = variablesCart.getType();
					String variableType = type.name();
					if (variableType.equals("LIST")) {
						variable.add(variablesCart);
					}

				}

				Thread th = new Thread();

				mp.put("id", id);
				mp.put("name", name);
				mp.put("taskState", taskState);
				mp.put("variables", variable);
				ls.add(mp);
			}

		}

		return ls;

	}

	// duplicat of previos ONE :-

	@CrossOrigin
	@GetMapping("/getAssigned/{processName}/{adminName}/{taskID}")
	public List<Task> getAssignedTaskTaskID(@PathVariable String processName, @PathVariable String adminName,
			@PathVariable String taskID) throws TaskListException {

		SaasAuthentication sa = new SaasAuthentication("zCCx7DH6.mn_tGC5O1mNrPg-q6qVNsMg",
				"u4Y-WP8SmmVik3E.PJ.iE0-09wSX_rj5LLGSKVhL~Y23ZcXP_QXPgnPiAVCcezM_");

		CamundaTaskListClient client = new CamundaTaskListClient.Builder().authentication(sa)
				.taskListUrl("https://bru-2.tasklist.camunda.io/1a8d8e18-4054-4bd2-afad-6f2adf8c58b8/").build();

		List<Task> assigneeTasks = client.getAssigneeTasks(adminName, TaskState.CREATED, 50, true);

		List ls = new ArrayList<>();
		List variable = new ArrayList<>();
		List findList = new ArrayList<>();

		Map mp = new HashMap<>();

		for (Task processNameList : assigneeTasks) {
			String string = processNameList.toString();
			String processNames = processNameList.getProcessName();
			if (processNames.equals(processName)) {

				String id = processNameList.getId();
				String name = processNameList.getName();
				TaskState taskState = processNameList.getTaskState();
				List<Variable> variables = processNameList.getVariables();

				// iterating variable :-
				for (Variable variablesCart : variables) {
					VariableType type = variablesCart.getType();
					// VariableType type = variablesCart.getType();
					String variableType = type.name();
					if (variableType.equals("LIST")) {
						// variable.add(variablesCart);
						String idFromProductList = variablesCart.getId();
						String[] str = idFromProductList.split("-");

						String stringGetprocessInstanceKey = str[0];

						if (idFromProductList.equals(taskID))
							;
						findList.add(processNameList);

						// variable.add(stringGetprocessInstanceKey);

					}

				}

			}

		}

		List convertValueTask = objectMapper.convertValue(findList, List.class);

		List lsst = new ArrayList<>();

		for (Object finalVariableList : convertValueTask) {

			// finalVariableList.ge
			Map mpm = (Map) finalVariableList;

			System.out.println(mpm);
			Object id = mpm.get("id");
			Object name = mpm.get("name");
			Object assignee = mpm.get("assignee");

			Object taskState = mpm.get("taskState");

			Object variables = mpm.get("variables");

			List lsl = (List) variables;
			List targetVariableList = new ArrayList<>();
			for (Object targetVariable : lsl) {
				Map mps = (Map) targetVariable;
				String object = (String) mps.get("type");
				if (object.equals("LIST")) {

					System.out.println("All Variable" + targetVariable);

					targetVariableList.add(targetVariable);
				}
			}

			Map finalVariableMap = new HashMap<>();

			finalVariableMap.put("id", id);
			finalVariableMap.put("name", name);
			finalVariableMap.put("taskState", taskState);
			finalVariableMap.put("assignee", assignee);
			finalVariableMap.put("variables", targetVariableList);

			lsst.add(finalVariableMap);

			// Object variables = mpm.get("variables");

		}

		return lsst;

	}

	// Attempt 2 previous one try to finished :-;

	@CrossOrigin
	@GetMapping("/getAssignedAttempt/{processName}/{adminName}/{taskID}")
	public List<Task> getAssignedTaskTaskIDAttempt(@PathVariable String processName, @PathVariable String adminName,
			@PathVariable String taskID) throws TaskListException {

		SaasAuthentication sa = new SaasAuthentication("zCCx7DH6.mn_tGC5O1mNrPg-q6qVNsMg",
				"u4Y-WP8SmmVik3E.PJ.iE0-09wSX_rj5LLGSKVhL~Y23ZcXP_QXPgnPiAVCcezM_");

		CamundaTaskListClient client = new CamundaTaskListClient.Builder().authentication(sa)
				.taskListUrl("https://bru-2.tasklist.camunda.io/1a8d8e18-4054-4bd2-afad-6f2adf8c58b8/").build();

		List<Task> assigneeTasks = client.getAssigneeTasks(adminName, TaskState.CREATED, 50, true);
		List sampleTest = new ArrayList<>();

		for (Task processNameList : assigneeTasks) {
			String processNames = processNameList.getProcessName();
			if (processNames.equals(processName)) {

				String id = processNameList.getId();
				String name = processNameList.getName();
				String processNameExist = processNameList.getProcessName();
				TaskState taskState = processNameList.getTaskState();
				String assignee = processNameList.getAssignee();

				List<Variable> variables = processNameList.getVariables();
				Map mp = new HashMap<>();
				mp.put("id", id);
				mp.put("name", name);
				mp.put("processName", processNameExist);
				mp.put("taskState", taskState);
				mp.put("assignee", assignee);
				mp.put("variable", variables);
				sampleTest.add(mp);
			}

		}

		List convertValueTask = objectMapper.convertValue(sampleTest, List.class);

		List lsst = new ArrayList<>();

		List matchList = new ArrayList<>();

		for (Object finalVariableList : convertValueTask) {

			Map mpm = (Map) finalVariableList;

			Object id = mpm.get("id");
			Object name = mpm.get("name");
			Object assignee = mpm.get("assignee");

			Object taskState = mpm.get("taskState");

			Object object2 = mpm.get("variable");

			List lsl = (List) object2;

			for (Object lsstt : lsl) {

				Map getInstanceID = (Map) lsstt;

				String object3 = (String) getInstanceID.get("id");
				String[] str = object3.split("-");
				String version = str[0];

				if (version.equals(taskID)) {

					matchList.add(finalVariableList);
				}

			}

		}

		List instanceIDList = new ArrayList<>();

		for (Object getVariableTypeList : matchList) {

			Map maplist = (Map) getVariableTypeList;
			Object id = maplist.get("id");
			Object name = maplist.get("name");
			Object processNames = maplist.get("processName");
			Object assignee = maplist.get("assignee");
			Object taskState = maplist.get("taskState");

			Object object = maplist.get("variable");
			List matchVariable = (List) object;

			for (Object finalist : matchVariable) {
				Map mpm = (Map) finalist;
				String type = (String) mpm.get("type");
				if (type.equals("LIST")) {
					Map finalMap = new HashMap<>();
					finalMap.put("id", id);
					finalMap.put("name", name);
					finalMap.put("processName", processNames);
					finalMap.put("assignee", assignee);
					finalMap.put("taskState", taskState);
					List variableList = new ArrayList<>();
					variableList.add(finalist);
					finalMap.put("variable", variableList);
					instanceIDList.add(finalMap);
				}

			}

		}

		return instanceIDList;

	}

	// Complete API :-
	// Get all variable from Complete Task need TaskID:
//	@CrossOrigin
//	@GetMapping("/completeAPI/{userName}")
//	public List<Task> completeAPI(@PathVariable String userName) throws TaskListException {
//		Map mp = new HashMap<>();
//		//System.out.println(taskId);
//		SaasAuthentication sa = new SaasAuthentication("jiIaOU5bGP1HJbyR3jZ.bhqsiCpTMTZZ",
//				"wz0YxMw.oapyIi48t8aUrqOMXfubR9953gBuwa8cMqMG-595cyhM16wPAhNIKdJf");
//
//		CamundaTaskListClient client = new CamundaTaskListClient.Builder().authentication(sa)
//				.taskListUrl("https://bru-2.operate.camunda.io/1a8d8e18-4054-4bd2-afad-6f2adf8c58b8/").build();
//
//
//		
//		
//		return client.getAssigneeTasks(userName, TaskState.COMPLETED, 50);

	// OrderHistory :-

	@GetMapping("/completeAPI/{userName}")
	public List<Task> completeAPI(@PathVariable String userName) throws TaskListException {

		// System.out.println(taskId);
		SaasAuthentication sa = new SaasAuthentication("zCCx7DH6.mn_tGC5O1mNrPg-q6qVNsMg",
				"u4Y-WP8SmmVik3E.PJ.iE0-09wSX_rj5LLGSKVhL~Y23ZcXP_QXPgnPiAVCcezM_");

		CamundaTaskListClient client = new CamundaTaskListClient.Builder().authentication(sa)
				.taskListUrl("https://bru-2.tasklist.camunda.io/1a8d8e18-4054-4bd2-afad-6f2adf8c58b8/").build();

		// return client.getAssigneeTasks(userName, TaskState.COMPLETED, 50, true);

		List<Task> assigneeTasks = client.getAssigneeTasks(userName, TaskState.COMPLETED, 50, true);

		List finalList = new ArrayList<>();

		List variableLists = new ArrayList<>();
		for (Task orderHistoryList : assigneeTasks) {
			Map mp = new HashMap<>();
			TaskState taskState = orderHistoryList.getTaskState();
			String creationTime = orderHistoryList.getCreationTime();

			mp.put("taskState", taskState);
			mp.put("creationTime", creationTime);

			List<Variable> variables = orderHistoryList.getVariables();
			for (Variable variableList : variables) {
				VariableType type = variableList.getType();
				String name = type.name();
				if (name.equals("LIST")) {
					Variable variableList2 = variableList;
					// variableLists.add(variableList2);
					// variableLists.add(mp);
					Map mpm = new HashMap<>();
					mpm.put("variables", variableList2);
					mpm.put("taskState", taskState);
					mpm.put("creationTime", creationTime);
					finalList.add(mpm);
				}

			}

		}

		return finalList;

	}

	// Order History Filter by ID :-

	// OrderHistory :-

	// This API is TaskState is Completed :
	@GetMapping("/orderHistoryFilterByID/{userName}/{instanceID}")
	public Task orderHistoryFilterByID(@PathVariable String userName, @PathVariable String instanceID)
			throws TaskListException {

		SaasAuthentication sa = new SaasAuthentication("zCCx7DH6.mn_tGC5O1mNrPg-q6qVNsMg",
				"u4Y-WP8SmmVik3E.PJ.iE0-09wSX_rj5LLGSKVhL~Y23ZcXP_QXPgnPiAVCcezM_");

		CamundaTaskListClient client = new CamundaTaskListClient.Builder().authentication(sa)
				.taskListUrl("https://bru-2.tasklist.camunda.io/1a8d8e18-4054-4bd2-afad-6f2adf8c58b8/").build();

		List<Task> assigneeTasks = client.getAssigneeTasks(userName, TaskState.COMPLETED, 50, true);

		List finalList = new ArrayList<>();

		List variableLists = new ArrayList<>();
		for (Task orderHistoryList : assigneeTasks) {
			Map mp = new HashMap<>();
			TaskState taskState = orderHistoryList.getTaskState();
			String creationTime = orderHistoryList.getCreationTime();

			mp.put("taskState", taskState);
			mp.put("creationTime", creationTime);

			List<Variable> variables = orderHistoryList.getVariables();
			for (Variable variableList : variables) {
				VariableType type = variableList.getType();
				String name = type.name();
				if (name.equals("LIST")) {
					Variable variableList2 = variableList;
					Map mpm = new HashMap<>();
					mpm.put("variables", variableList2);
					mpm.put("taskState", taskState);
					mpm.put("creationTime", creationTime);
					finalList.add(mpm);
				}

			}

		}

		Task getTaskList = (Task) finalList;

		List objects = new ArrayList<>();
		for (Object getListByID : finalList) {

			Map mapVariable = (Map) getListByID;

			Variable object = (Variable) mapVariable.get("variables");
			String id = object.getId();
			String[] str = id.split("-");
			String string = str[0];
			if (instanceID.equals(string)) {
				objects.add(getListByID);

			}
		}

		return getTaskList;

	}

	// getTaskvariable
	//////////////////////////////////////////////
	@CrossOrigin
	@GetMapping("/getTaskvariable/{taskId}")
	public Task getvariabletask(@PathVariable String taskId) throws TaskListException {

		System.out.println(taskId);
		SaasAuthentication sa = new SaasAuthentication("zCCx7DH6.mn_tGC5O1mNrPg-q6qVNsMg",
				"u4Y-WP8SmmVik3E.PJ.iE0-09wSX_rj5LLGSKVhL~Y23ZcXP_QXPgnPiAVCcezM_");

		CamundaTaskListClient client = new CamundaTaskListClient.Builder().authentication(sa)
				.taskListUrl("https://bru-2.tasklist.camunda.io/1a8d8e18-4054-4bd2-afad-6f2adf8c58b8/").build();

		return client.getTask(taskId, true);

	}

	// Filter From Active UserTakList :-

	@GetMapping("/filterDataFromUserTasklist")
	public List filterDataFromUserTasklist() {

		String processName = "OrderFullfillment";

		String adminName = "murali.muthu@surgetechinc.in";
		String url = "http://localhost:8080/getAssignedTask/" + adminName;

		ResponseEntity<List> forEntity = rest.getForEntity(url, List.class);
		List activeList = forEntity.getBody();

		List activeList1 = new ArrayList<>();
		for (Object activeInstance : activeList) {

			Map mpInstance = (Map) activeInstance;
			Object processNameFromList = mpInstance.get("processName");
			if (processNameFromList.equals(processName)) {

				activeList1.add(activeInstance);
			}

		}

		return activeList1;

	}

	// 5.

	// ****************************** Payment_Process_Complete
	// ****************************

	@PostMapping("/paymentProcess")
	public String paymentReceived(@RequestBody Map paymentDetails) {

		String messageName = (String) paymentDetails.get("messageName");

		String key = (String) paymentDetails.get("correlationKey");

		zeebeClient.newPublishMessageCommand().messageName(messageName).correlationKey(key).send().join();
		return "Payment Done Successfully";
	}

//	// OrderCancelled API :-
	@GetMapping("/orderCancelled")
	public Map orderCancelled() throws Exception {
		System.out.println("Enter Contoller");
		Map mp = new HashMap<>();

		String messageName = "OrderCancelled";

		String key = "123";
		mp.put("messageName", messageName);
		mp.put("key", key);

		zeebeClient.newPublishMessageCommand().messageName(messageName).correlationKey(key).send().join();

		return mp;
	}

	// User_Side :-
	// Product _Updates or Create :-

	// 6.

	// ****************************** Create or Updates ****************************

	@PostMapping("/createProductByAdmin")
	public List<ProductDetailss> createProductByAdmin(@RequestBody String input) throws Exception {

		Map inputValue = objectMapper.readValue(input, Map.class);
		File file = new File(
				"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\TotalAvailableProduct.json");

		// Create the file if it doesn't exist
		if (!file.exists()) {
			file.createNewFile();
		}
		// Read the existing data from the file into a list
		List<ProductDetailss> productList = new ArrayList<>();
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			TypeReference<List<ProductDetailss>> typeReference = new TypeReference<List<ProductDetailss>>() {
			};
			productList = objectMapper.readValue(fileInputStream, typeReference);
			fileInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// my logics :-

		String inputId = (String) inputValue.get("product_ID");
		String inputName = (String) inputValue.get("product_Name");

		int stock = (int) inputValue.get("stock");

		List finalList = new ArrayList<>();

		finalList.add(inputValue);
		boolean found = false;

		for (ProductDetailss lst : productList) {
			String itemId1 = lst.getProduct_ID();
			String itemName = lst.getProduct_Name();

			if (itemId1.equals(inputId) && itemName.equals(inputName)) {
				int stock1 = lst.getStock() + stock;

				for (ProductDetailss obj : productList) {
					if (obj.getProduct_ID() == itemId1) {
						obj.setStock(stock1);
						break; // Stop iterating after updating the desired object
					}
				}

				found = true;
				break;
			}
			if (itemId1.equals(inputId) && !itemName.equals(inputName)) {

				found = true;
			}

		}

		if (!found) {
			productList.addAll(finalList);
		}

		try {

			FileWriter fileWriter = new FileWriter(file);
			objectMapper.writeValue(fileWriter, productList);
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return productList;
	}

	// 7.

	// ****************************** EditProductDetailsByID
	// ****************************

	// Edit a Product from Add_ProductLIst BY ID :-

	// editProductDetailsByID
	@PostMapping("/editProductDetailsByID")
	public List<ProductDetailss> editProductDetailsByID(@RequestBody String input) throws Exception {

		Map inputValue = objectMapper.readValue(input, Map.class);
		File file = new File(
				"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\TotalAvailableProduct.json");

		// Create the file if it doesn't exist
		if (!file.exists()) {
			file.createNewFile();
		}
		// Read the existing data from the file into a list
		List<ProductDetailss> productList = new ArrayList<>();
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			TypeReference<List<ProductDetailss>> typeReference = new TypeReference<List<ProductDetailss>>() {
			};
			productList = objectMapper.readValue(fileInputStream, typeReference);
			fileInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// my logics :-

		String inputProductId = (String) inputValue.get("product_ID");
		// String inputName = (String) inputValue.get("product_Name");

		// int stock = (int) inputValue.get("stock");

		List finalList = new ArrayList<>();

		finalList.add(inputValue);
		boolean found = false;

		// iterate Products :-
		for (ProductDetailss lst : productList) {

			String productIdFromProductList = lst.getProduct_ID();

			if (inputProductId.equals(productIdFromProductList)) {
				// ProductDetailss editObject = new ProductDetailss();

				// editObject.getProduct_Name(lst.setProduct_Name(inputProductId));
				String product_Name = (String) inputValue.get("product_Name");
				int price = (int) inputValue.get("product_Price");
				Long product_Price = Long.valueOf(price);

				int product_Qnty = (int) inputValue.get("product_Qnty");
				int stock = (int) inputValue.get("stock");

				lst.setProduct_Name(product_Name);
				lst.setProduct_Price(product_Price);
				// Long l2 = Long.valueOf(product_Qnty);
				lst.setProduct_Qnty(product_Qnty);
				lst.setStock(stock);
				break;

			}

		}

		try {

			FileWriter fileWriter = new FileWriter(file);
			objectMapper.writeValue(fileWriter, productList);
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return productList;
	}

	// 7.

	// ****************************** Delete_Porduct_ByID
	// ****************************

	// Delete By ID :-

	@DeleteMapping("/deleteProductByID/{inputID}")
	public List<ProductDetailss> deleteProductByID(@PathVariable String inputID) throws Exception {
		File file = new File(
				"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\TotalAvailableProduct.json");

		if (!file.exists()) {
			file.createNewFile();
		}
		List<ProductDetailss> productList = new ArrayList<>();
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			TypeReference<List<ProductDetailss>> typeReference = new TypeReference<List<ProductDetailss>>() {
			};
			productList = objectMapper.readValue(fileInputStream, typeReference);
			fileInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Iterator<ProductDetailss> iterator = productList.iterator();
		while (iterator.hasNext()) {
			ProductDetailss number = iterator.next();
			String product_ID = number.getProduct_ID();

			// String child = ref1.getProduct_ID();

			if (product_ID.equals(inputID)) {

				iterator.remove();

			}

		}

		try {
			FileWriter fileWriter = new FileWriter(file);
			objectMapper.writeValue(fileWriter, productList);
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return productList;
	}

	// 8.
	// ******************************
	// Get_All_Product_List****************************

	// Product Get All :-

	// Getall AddProduct JSON for Admin :-
	@GetMapping("/getAllProductDeatils")
	public JSONArray getAllProductDeatils() {
		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(new FileReader(
					"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\TotalAvailableProduct.json"));
			JSONArray jsonObject = (JSONArray) obj;
			return jsonObject;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// 9.

	// ****************************** Get_ProductList_ByID
	// ****************************

	// getProductDetailsById

	@GetMapping("/getProductDetailsById/{id}")
	public List getProductDetailsById(@PathVariable String id) throws Exception {
		List finalList = new ArrayList<>();
		List sls = new ArrayList<>();
		String url = "http://localhost:8080/getAllProductDeatils";
		ResponseEntity<List> forEntity = rest.getForEntity(url, List.class);

		List res = forEntity.getBody();

		for (Object proList : res) {

			Map ms = (Map) proList;

			Map proMap = (Map) proList;

			if (id.equals(proMap.get("product_ID"))) {

				Map mpl = new HashMap<>();
				String product_ID = (String) proMap.get("product_ID");
				int product_Qnty = (int) proMap.get("product_Qnty");
				int price = (int) proMap.get("product_Price");
				Long product_Price = Long.valueOf(price);
				String product_Name = (String) proMap.get("product_Name");
				int stock = (int) proMap.get("stock");

				mpl.put("product_ID", product_ID);
				mpl.put("product_Qnty", product_Qnty);
				mpl.put("product_Price", product_Price);
				mpl.put("product_Name", product_Name);
				mpl.put("stock", stock);
				finalList.add(mpl);
			}

		}

		return finalList;
	}

	// ****************************** Cart ******************************

	// 10.

	// ****************************** Add_A_Product_To_Cart
	// ****************************

	@GetMapping("/addProductToCart/{userid}/{cartid}/{productID}")
	public List addProductToCart(@PathVariable String userid, @PathVariable String cartid,
			@PathVariable String productID
	// @RequestBody String userProductList
	) throws Exception {
		// List inputValue = objectMapper.readValue(userProductList, List.class);
		File file = new File(
				"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\TotalAvailableProduct.json");

		// Create the file if it doesn't exist
		if (!file.exists()) {
			file.createNewFile();
		}
		// Read the existing data from the file into a list
		List<ProductDetailss> totalAvailableProduct = new ArrayList<>();
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			TypeReference<List<ProductDetailss>> typeReference = new TypeReference<List<ProductDetailss>>() {
			};
			totalAvailableProduct = objectMapper.readValue(fileInputStream, typeReference);
			fileInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<ProductDetailss> getUserList = new ArrayList<>();

		for (ProductDetailss lst : totalAvailableProduct) {
			String product_ID = lst.getProduct_ID();

			if (productID.equals(product_ID)) {

				getUserList.add(lst);

			}

		}

		// }
		File file1 = new File(
				"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\AddCart.json");

		// Create the file if it doesn't exist
		if (!file1.exists()) {
			file1.createNewFile();
		}
		// Read the existing data from the file into a list
		List<Cart> cartList = new ArrayList<>();
		try {
			FileInputStream fileInputStream = new FileInputStream(file1);
			TypeReference<List<Cart>> typeReference = new TypeReference<List<Cart>>() {
			};
			cartList = objectMapper.readValue(fileInputStream, typeReference);
			fileInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		boolean found = false;
		List userList = new ArrayList<>();

		for (Cart lst : cartList) {
			String userID = lst.getUserid();
			if (userid.equals(userID)) {
				List<ProductDetailss> productList = lst.getProductList();

				List finalList = new ArrayList<>();
				for (ProductDetailss rs : getUserList) {
					for (ProductDetailss lststr : productList) {

						String product_ID = rs.getProduct_ID();
						String product_ID2 = lststr.getProduct_ID();

						if (product_ID.equals(product_ID2)) {
							found = true;
						}

					}

					if (!found) {
						productList.add(rs);
					}
				}

				// lst.setProductList(getUserList);
				found = true;
				break;
			}

		}

		if (!found) {

			Cart newCart = new Cart();
			newCart.setCartid(cartid);
			newCart.setUserid(userid);
			newCart.setProductList(getUserList);
			cartList.add(newCart);

		}

		try {

			FileWriter fileWriter = new FileWriter(file1);
			objectMapper.writeValue(fileWriter, cartList);
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		List finalResult = new ArrayList<>();

		for (Cart view : cartList) {
			if (userid.equals(view.getUserid())) {
				finalResult.add(view);

			}

		}
		return finalResult;
	}

	// AddTOCartSingleUser :-

	// ****************************** addToCartSingleUser
	// ****************************

	@GetMapping("/addToCartSingleUser/{productID}")
	public List<Cart> addToCartSingleUser(@PathVariable String productID
	// @RequestBody String userProductList
	) throws Exception {
		// List inputValue = objectMapper.readValue(userProductList, List.class);
		File file = new File(
				"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\TotalAvailableProduct.json");

		// Create the file if it doesn't exist
		if (!file.exists()) {
			file.createNewFile();
		}
		// Read the existing data from the file into a list
		List<ProductDetailss> totalAvailableProduct = new ArrayList<>();
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			TypeReference<List<ProductDetailss>> typeReference = new TypeReference<List<ProductDetailss>>() {
			};
			totalAvailableProduct = objectMapper.readValue(fileInputStream, typeReference);
			fileInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<ProductDetailss> getUserList = new ArrayList<>();
		// for (Object input : inputValue) {
		for (ProductDetailss lst : totalAvailableProduct) {
			String product_ID = lst.getProduct_ID();
			// String s = String.valueOf(input);

			if (productID.equals(product_ID)) {

				getUserList.add(lst);

			}

		}

		// }
		File file1 = new File(
				"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\AddToCartSingleUser.json");

		// Create the file if it doesn't exist
		if (!file1.exists()) {
			file1.createNewFile();
		}
		// Read the existing data from the file into a list
		List<Cart> cartList = new ArrayList<>();
		try {
			FileInputStream fileInputStream = new FileInputStream(file1);
			TypeReference<List<Cart>> typeReference = new TypeReference<List<Cart>>() {
			};
			cartList = objectMapper.readValue(fileInputStream, typeReference);
			fileInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		boolean found = false;
		List userList = new ArrayList<>();

		for (Cart lst : cartList) {
			String userID = lst.getUserid();
			List<ProductDetailss> productList = lst.getProductList();

			List finalList = new ArrayList<>();
			for (ProductDetailss rs : getUserList) {
				for (ProductDetailss lststr : productList) {

					String product_ID = rs.getProduct_ID();
					String product_ID2 = lststr.getProduct_ID();

					if (product_ID.equals(product_ID2)) {
						found = true;
					}

				}

				if (!found) {
					productList.add(rs);
				}
			}

			// lst.setProductList(getUserList);
			found = true;
			break;

		}

		if (!found) {

			Cart newCart = new Cart();
			// newCart.setCartid(cartid);
			// newCart.setUserid(userid);
			newCart.setProductList(getUserList);
			cartList.add(newCart);

		}

		try {

			FileWriter fileWriter = new FileWriter(file1);
			objectMapper.writeValue(fileWriter, cartList);
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		List finalResult = new ArrayList<>();

		for (Cart view : cartList) {
			List<ProductDetailss> productList = view.getProductList();

			finalResult.add(productList);

		}
		return finalResult;
	}

	// another API : POST Method :-

	// AddTOCartSingleUser :-

	// ****************************** addToCartSingleUser
	// ****************************

	@PostMapping("/addToCart")
	public List<Cart> addToCart(@RequestBody Map userCartDetails) throws Exception {

		Map priceFinal = new HashMap<>();
		Map inputValue = objectMapper.convertValue(userCartDetails, Map.class);
		String inputReqproduct_ID = (String) inputValue.get("product_ID");
		String userid = (String) inputValue.get("userid");
		String inputCartid = (String) inputValue.get("cartid");
		int product_Qnty = (int) inputValue.get("product_Qnty");

		File file = new File(
				"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\TotalAvailableProduct.json");

		// Create the file if it doesn't exist
		if (!file.exists()) {
			file.createNewFile();
		}
		// Read the existing data from the file into a list
		List<ProductDetailss> totalAvailableProduct = new ArrayList<>();
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			TypeReference<List<ProductDetailss>> typeReference = new TypeReference<List<ProductDetailss>>() {
			};
			totalAvailableProduct = objectMapper.readValue(fileInputStream, typeReference);
			fileInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<ProductDetailss> getUserList = new ArrayList<>();

		for (ProductDetailss lst : totalAvailableProduct) {
			String product_ID = lst.getProduct_ID();

			if (product_ID.equals(inputReqproduct_ID)) {

				long product_Price2 = lst.getProduct_Price();
				priceFinal.put("priceFinal", product_Price2);
				getUserList.add(lst);

			}

		}

		File file1 = new File(
				"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\AddCart.json");

		// Create the file if it doesn't exist
		if (!file1.exists()) {
			file1.createNewFile();
		}
		// Read the existing data from the file into a list
		List<Cart> cartList = new ArrayList<>();
		try {
			FileInputStream fileInputStream = new FileInputStream(file1);
			TypeReference<List<Cart>> typeReference = new TypeReference<List<Cart>>() {
			};
			cartList = objectMapper.readValue(fileInputStream, typeReference);
			fileInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		boolean found = false;
		List userList = new ArrayList<>();
		Object object = priceFinal.get("priceFinal");
		String string = object.toString();
		int i = Integer.parseInt(string);

		for (Cart lst : cartList) {
			String userID = lst.getUserid();
			String cartid = lst.getCartid();
			if (userid.equals(userID)) {
				List<ProductDetailss> productList = lst.getProductList();

				List finalList = new ArrayList<>();

				for (ProductDetailss rs : getUserList) {
					for (ProductDetailss lststr : productList) {

						String product_ID = rs.getProduct_ID();
						String product_ID2 = lststr.getProduct_ID();

						// my part :-

						int product_Qnty2 = lststr.getProduct_Qnty();
						int totalQuantity = product_Qnty2 + product_Qnty;

						if (product_ID.equals(product_ID2)) {
							lststr.setProduct_Qnty(product_Qnty);
							long product_Price2 = lststr.getProduct_Price();

							Long l2 = Long.valueOf(i);
							long prices = l2 * product_Qnty;
							lststr.setProduct_Price(prices);
							found = true;
							break;
						}

					}

					if (cartid.equals(inputCartid)) {
						if (!found) {
							rs.setProduct_Qnty(product_Qnty);
							productList.add(rs);

						}
					}

				}
				lst.setCount(productList.size());
				found = true;
				break;
			}

		}

		if (!found) {

			Cart newCart = new Cart();
			newCart.setCartid(userid + "_cart");
			newCart.setUserid(userid);
			newCart.setProductList(getUserList);
			newCart.setCount(getUserList.size());
			cartList.add(newCart);

		}

		try {

			FileWriter fileWriter = new FileWriter(file1);
			objectMapper.writeValue(fileWriter, cartList);
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		List finalResult = new ArrayList<>();

		for (Cart view : cartList) {
			if (userid.equals(view.getUserid())) {
				finalResult.add(view);

			}

		}
		return finalResult;
	}

	// Get AddToCart :
	@GetMapping("/getAddToCartDetail/{userName}")
	public List getAddToCartDetail(@PathVariable String userName) throws Exception {

		File file = new File(
				"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\AddCart.json");

		// Create the file if it doesn't exist
		if (!file.exists()) {
			file.createNewFile();
		}
		// Read the existing data from the file into a list
		List<Cart> addCartList = new ArrayList<>();
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			TypeReference<List<Cart>> typeReference = new TypeReference<List<Cart>>() {
			};
			addCartList = objectMapper.readValue(fileInputStream, typeReference);
			fileInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		List cartListUser = new ArrayList<>();
		for (Cart cartList : addCartList) {
			String userid = cartList.getUserid();
			if (userid.equals(userName)) {

				List<ProductDetailss> productList = cartList.getProductList();

				for (ProductDetailss ref : productList) {
					String product_ID = ref.getProduct_ID();
					String product_Name = ref.getProduct_Name();
					long product_Price = ref.getProduct_Price();
					int product_Qnty = ref.getProduct_Qnty();
					Map mp = new HashMap<>();
					mp.put("product_ID", product_ID);
					mp.put("product_Name", product_Name);
					mp.put("product_Price", product_Price);
					mp.put("product_Qnty", product_Qnty);

					cartListUser.add(mp);
				}

			}

		}

		return cartListUser;
	}

	// Remove a Cart from RequestBody :------

	@GetMapping("/removeFromCart")
	public List<Cart> removeFromCart(@RequestBody Map input) throws Exception {

		Map inputValue = objectMapper.convertValue(input, Map.class);
		String inputReqproduct_ID = (String) inputValue.get("product_ID");
		String userid = (String) inputValue.get("userid");
		String inputCartid = (String) inputValue.get("cartid");
		int product_Qnty = (int) inputValue.get("product_Qnty");

		File file = new File(
				"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\AddCart.json");

		if (!file.exists()) {
			file.createNewFile();
		}
		List<Cart> productList = new ArrayList<>();
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			TypeReference<List<Cart>> typeReference = new TypeReference<List<Cart>>() {
			};
			productList = objectMapper.readValue(fileInputStream, typeReference);
			fileInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (Cart ref : productList) {

			if (ref.getUserid().equals(userid)) {
				List<ProductDetailss> productList2 = ref.getProductList();

				Iterator<ProductDetailss> iterator = productList2.iterator();
				while (iterator.hasNext()) {
					ProductDetailss number = iterator.next();

					if (number.getProduct_ID().equals(inputReqproduct_ID)) {
						iterator.remove();
					}

				}
				ref.setCount(productList2.size());
			}

		}

		try {
			FileWriter fileWriter = new FileWriter(file);
			objectMapper.writeValue(fileWriter, productList);
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		List finalResult = new ArrayList<>();

		for (Cart view : productList) {
			if (userid.equals(view.getUserid())) {
				finalResult.add(view);

			}

		}
		return finalResult;
	}

	// Remove Single User:-

	// 11.

	// ****************************** RemoveProductFromCartSingleUser
	// ****************************

	@GetMapping("/removeProductFromCartSingleUser/{productID}")
	public List<Cart> removeProductFromCartSingleUser(@PathVariable String productID) throws Exception {
		File file = new File(
				"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\AddToCartSingleUser.json");

		if (!file.exists()) {
			file.createNewFile();
		}
		List<Cart> productList = new ArrayList<>();
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			TypeReference<List<Cart>> typeReference = new TypeReference<List<Cart>>() {
			};
			productList = objectMapper.readValue(fileInputStream, typeReference);
			fileInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (Cart ref : productList) {

			List<ProductDetailss> productList2 = ref.getProductList();

			Iterator<ProductDetailss> iterator = productList2.iterator();
			while (iterator.hasNext()) {
				ProductDetailss number = iterator.next();

				if (number.getProduct_ID().equals(productID)) {
					iterator.remove();
				}

			}

		}

		try {
			FileWriter fileWriter = new FileWriter(file);
			objectMapper.writeValue(fileWriter, productList);
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		List finalResult = new ArrayList<>();

		for (Cart view : productList) {
			List<ProductDetailss> productList2 = view.getProductList();
			finalResult.add(productList2);

		}
		return finalResult;
	}

	// 11.

	// ****************************** Remove_A_Product_From_Cart_ByID
	// ****************************

	@GetMapping("/removeProductFromCart/{userid}/{cartid}/{productID}")
	public List<Cart> removeProductFromCart(@PathVariable String userid, @PathVariable String cartid,
			@PathVariable String productID) throws Exception {

		File file = new File(
				"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\AddCart.json");

		if (!file.exists()) {
			file.createNewFile();
		}
		List<Cart> productList = new ArrayList<>();
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			TypeReference<List<Cart>> typeReference = new TypeReference<List<Cart>>() {
			};
			productList = objectMapper.readValue(fileInputStream, typeReference);
			fileInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (Cart ref : productList) {

			if (ref.getUserid().equals(userid)) {
				List<ProductDetailss> productList2 = ref.getProductList();

				Iterator<ProductDetailss> iterator = productList2.iterator();
				while (iterator.hasNext()) {
					ProductDetailss number = iterator.next();

					if (number.getProduct_ID().equals(productID)) {
						iterator.remove();
					}

				}
			}

		}

		try {
			FileWriter fileWriter = new FileWriter(file);
			objectMapper.writeValue(fileWriter, productList);
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		List finalResult = new ArrayList<>();

		for (Cart view : productList) {
			if (userid.equals(view.getUserid())) {
				finalResult.add(view);

			}

		}
		return finalResult;
	}

	// Get Invoice Details :

	@CrossOrigin
	@GetMapping("/getInvoiceDetails/{userName}")
	public List<InvoiceDetails> getInvoiceDetails(@PathVariable String userName) throws Exception {

		File file = new File(
				"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\AddCart.json");

		if (!file.exists()) {
			file.createNewFile();
		}
		List<Cart> cartList = new ArrayList<>();
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			TypeReference<List<Cart>> typeReference = new TypeReference<List<Cart>>() {
			};
			cartList = objectMapper.readValue(fileInputStream, typeReference);
			fileInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		InvoiceDetails invoiceDetails = new InvoiceDetails();

		BillingTo billingTo = invoiceDetails.getBillingTo();

		// String curruntDate = invoiceDetails.getCurruntDate();
		String invoiceNumber = invoiceDetails.getInvoiceNumber();

		// invoiceDetails.setCurruntDate("22-jun-2023");
		invoiceDetails.setInvoiceNumber("786565454678");

		List userLists = new ArrayList<>();

		for (Cart listFromCart : cartList) {

			String userid = listFromCart.getUserid();
			if (userid.equals(userName)) {
				Map userList = new HashMap<>();

				List<ProductDetailss> productList = listFromCart.getProductList();

				long sum = 0;
				for (ProductDetailss price : productList) {
					long product_Price = price.getProduct_Price();
					sum = sum + product_Price;

					System.out.println(sum);

				}

				int count = listFromCart.getCount();
				userList.put("productList", productList);
				userList.put("count", count);

				SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				Date date = new Date();

				System.out.println(date);
				userList.put("curruntDate", date);
				userList.put("invoiceNumber", "786565454678");

				Map billTos = new HashMap<>();
				billTos.put("address", "chennai");
				billTos.put("email", userName + "@gmail.com");
				billTos.put("contactNum", "12345676789");
				billTos.put("address", "chennai");

				userList.put("billingTo", billTos);

				userList.put("subtotal", "786565454678");
				userList.put("discount", "786565454678");
				userList.put("tax", "786565454678");

				userList.put("total", sum);

				userLists.add(userList);

			}

		}

		List<InvoiceDetails> reqBodyMap = objectMapper.convertValue(userLists,
				new TypeReference<List<InvoiceDetails>>() {

				});
		return reqBodyMap;

	}

	// Login Register Parts :-
	// Register :
	@GetMapping("/getCustomerDeatils/{userName}")
	public List<CustomerDetails> getCustomerDeatils(@RequestBody CustomerDetails customerDetails) throws Exception {

		File file = new File(
				"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\CustomerRegistration.json");

		if (!file.exists()) {
			file.createNewFile();
		}
		List<CustomerDetails> cartList = new ArrayList<>();
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			TypeReference<List<CustomerDetails>> typeReference = new TypeReference<List<CustomerDetails>>() {
			};
			cartList = objectMapper.readValue(fileInputStream, typeReference);
			fileInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return cartList;
	}

	/// Stock Check:-

	@GetMapping("/stockCheck")
	public void stockCheck() {

	}

}
