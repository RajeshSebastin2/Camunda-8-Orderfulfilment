//package com.camunda.orderfullfillment.controller;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileReader;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.UUID;
//
//import org.json.simple.JSONArray;
//import org.json.simple.parser.JSONParser;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.client.RestTemplate;
//
//import com.camunda.orderfullfillment.model.Cart;
//import com.camunda.orderfullfillment.model.ProductDetailss;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import io.camunda.tasklist.CamundaTaskListClient;
//import io.camunda.tasklist.auth.SaasAuthentication;
//import io.camunda.tasklist.dto.Task;
//import io.camunda.tasklist.dto.TaskState;
//import io.camunda.tasklist.exception.TaskListException;
//import io.camunda.zeebe.client.ZeebeClient;
//import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
//
//@RestController
//public class OrderFullfillmentController_All {
//
//	@Value("${multi-instance-example.number-of-buckets}")
//	public long numberOfBuckets;
//
//	@Value("${multi-instance-example.number-of-elements}")
//	public long numberOfElements;
//
//	@Autowired
//	ZeebeClient zeebeClient;
//
//	@Autowired
//	ProductDetailss ds;
//
//	final RestTemplate rest = new RestTemplate();
//
////Demo for //ProcessInstanceResult :-
//
//	@GetMapping("/test")
//	public String demo() {
//
//		return "Working";
//
//	}
//
//	// *****************************************************************************
//	// : 1 :
//	/////////////// cloud start :-
//
////////////////////////Start WorkFlow //////////////	
//
//// @PostMapping("/submitProductList")
//	@CrossOrigin
//	@RequestMapping(value = "/submitProductList", method = RequestMethod.POST, headers = "Accept=*/*", produces = "application/json", consumes = "application/json")
//	public Map<String, Object> startWorkflow(@RequestBody String reqBody) throws TaskListException {
//
//		Map inputVaribale = new HashMap<>();
//
//		System.out.println("Input 1 :-" + reqBody);
//		SaasAuthentication sa = new SaasAuthentication("zCCx7DH6.mn_tGC5O1mNrPg-q6qVNsMg",
//				"u4Y-WP8SmmVik3E.PJ.iE0-09wSX_rj5LLGSKVhL~Y23ZcXP_QXPgnPiAVCcezM_");
//
//		CamundaTaskListClient client = new CamundaTaskListClient.Builder()
//				.taskListUrl("https://bru-2.tasklist.camunda.io/1a8d8e18-4054-4bd2-afad-6f2adf8c58b8")
//				.shouldReturnVariables().authentication(sa).build();
//
//		ObjectMapper mapper = new ObjectMapper();
//
//		try {
//			List<ProductDetailss> reqBodyMap = mapper.readValue(reqBody, new TypeReference<List<ProductDetailss>>() {
//			});
//			inputVaribale.put("inputVaribale", reqBodyMap);
//
//		} catch (JsonProcessingException e) {
//			e.printStackTrace();
//		}
//
//		ProcessInstanceEvent processInstEvnt = zeebeClient.newCreateInstanceCommand().bpmnProcessId("OrderFullfillment")
//				.latestVersion().variables(inputVaribale).send().join();
//
//		long pID = processInstEvnt.getProcessInstanceKey();
//
//		long processDefinitionKey = processInstEvnt.getProcessDefinitionKey();
//		System.out.println("processDefinitionKey" + processDefinitionKey);
//		System.out.println("pId : " + pID);
//
//		System.out.println("flow started");
//
//		Map<String, Object> processIdMap = new HashMap<String, Object>();
//		processIdMap.put("processInstanceKey", pID);
//		return processIdMap;
//
//	}
//
//	// Step 1 : Identify All Active Task :-
//
//	// Get All active Task :[-
//
//////////////////////////Get Active User Task List //////////////////////
//
//	@GetMapping("/getActivedTaskList")
//	public List<Task> getActivedTaskList() throws TaskListException {
//
//		SaasAuthentication sa = new SaasAuthentication("CwDKV~wHD2FcVeHiA_Nvcl.GSVsQdFSf",
//				"qY.Cw2iy52u7KSqumTNcjlOKiBnPK_68qpQtDKK_8SpEiG.WthMXAv2~fZIdT9v2");
//
//		CamundaTaskListClient client = new CamundaTaskListClient.Builder()
//				.taskListUrl("https://bru-2.tasklist.camunda.io/1a8d8e18-4054-4bd2-afad-6f2adf8c58b8")
//				.shouldReturnVariables().authentication(sa).build();
//
//		return client.getTasks(true, TaskState.CREATED, 50, true);
//
//	}
//
//	// Complete User Task :-
//
//	@CrossOrigin
//	@PostMapping("/completeTaskWithTaskId/{processInstanceKey}")
//	public String completeTaskWithTaskId(@PathVariable String processInstanceKey, @RequestBody Map variable)
//			throws Exception {
//
//		// String inputTraceID = (String) traceId.get("traceId");
//		// Thread.sleep(5000);
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
//						System.out.println("Enter ");
//					}
//
//					System.out.println("getIds---" + getIds);
//
//					System.out.println("MAp" + activeTaskMap);
//
//					String jobKey = (String) activeTaskMap.get("id");
//					System.out.println(jobKey);
//
//					finalJobkey.add(jobKey);
//
//				}
//			}
//
//		}
//		System.out.println("finalJobkey-----" + finalJobkey);
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
//		return "Task Completed";
//
//	}
//
//	// getVariable :-
//
//	@PostMapping("/getVariable/{processInstanceKey}")
//	public List getVariable(@PathVariable String processInstanceKey) {
//
//		String activeUrl = "http://localhost:8080/getActivedTaskList";
//
//		ResponseEntity<List> getActiveTaskList = rest.getForEntity(activeUrl, List.class);
//
//		Map mp = new HashMap();
//		List finalInsuranceVaiableList = new ArrayList();
//		List activeTaskList = getActiveTaskList.getBody();
//
//		for (Object getTraceId : activeTaskList) {
//
//			Map activeTaskMap = (Map) getTraceId;
//
//			List<Object> getVariableList = (List<Object>) activeTaskMap.get("variables");
//
//			for (Object getVariable : getVariableList) {
//
//				Map getVariableMap = (Map) getVariable;
//
//				String getIds = (String) getVariableMap.get("id");
//
//				String[] str = getIds.split("-");
//
//				String stringGetprocessInstanceKey = str[0];
//
//				if (processInstanceKey.equals(stringGetprocessInstanceKey)) {
//					System.out.println("Enter");
//
//					System.out.println("getVariableMap" + getVariableMap);
//					finalInsuranceVaiableList.add(getVariableMap);
//				}
//
//			}
//
//		}
//
//		return finalInsuranceVaiableList;
//
//	}
//
//	// *****************************************************************************
//	// : 2 :
//	// Message Trigger API :-
////	// paymentProcess :- 
//
//	@PostMapping("/paymentProcess")
//	public String paymentReceived(@RequestBody Map paymentDetails) {
//
//		System.out.println("PaymentProcess Enter" + paymentDetails);
//
//		String messageName = (String) paymentDetails.get("messageName");
//
//		String key = (String) paymentDetails.get("correlationKey");
//
//		zeebeClient.newPublishMessageCommand().messageName(messageName).correlationKey(key).send().join();
//		return "Payment Received";
//	}
//
//	// OrderCancelled API :-
//	@PostMapping("/orderCancelled")
//	public String OrderCancelled() {
//
//		System.out.println("OrderCancelled Enter");
//
//		zeebeClient.newPublishMessageCommand().messageName("OrderCancelled").correlationKey("123").send().join();
//		return "OrderCancelled Successfully";
//	}
//
//	// Event Based Gateway :-
//	@PostMapping("/newRegistration")
//	public String newRegistration() {
//
//		System.out.println("OrderCancelled Enter");
//
//		zeebeClient.newPublishMessageCommand().messageName("NewRegistration").correlationKey("1234").send().join();
//		return "OrderCancelled Successfully";
//	}
//
//	// Event Based Gateway :-
//	@PostMapping("/logOut")
//	public String logOut() {
//
//		System.out.println("OrderCancelled Enter");
//
//		zeebeClient.newPublishMessageCommand().messageName("LogOut").correlationKey("234").send().join();
//		return "OrderCancelled Successfully";
//	}
//
//	// Event Based Gateway :-
//	@PostMapping("/placeOrder")
//	public String placeOrder() {
//
//		System.out.println("OrderCancelled Enter");
//
//		zeebeClient.newPublishMessageCommand().messageName("PlaceOrder").correlationKey("678").send().join();
//		return "OrderCancelled Successfully";
//	}
//
//	// Procurement Process :-
//	@GetMapping("/lateShipment")
//	public String lateShipment() {
//
//		System.out.println("LateShipment Enter");
//
//		zeebeClient.newPublishMessageCommand().messageName("LateShipment").correlationKey("").send().join();
//		return "LateShipment Successfully Completed";
//	}
//
//	// Crud Operation :-
//
//	@PostMapping("/createProduct")
//	public List<ProductDetailss> createProduct(@RequestBody String var) throws Exception {
//		ObjectMapper mp = new ObjectMapper();
//
//		System.out.println(var);
//		List readValue = mp.readValue(var, List.class);
//		System.out.println(readValue);
//
//		return readValue;
//	}
//
//	// Json reader List Of Product Details :-
//	@GetMapping("/getProductDeatils")
//	public JSONArray getProductDeatils() {
//		JSONParser parser = new JSONParser();
//		try {
//			Object obj = parser.parse(new FileReader(
//					"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\ProductDetails.txt"));
//			JSONArray jsonObject = (JSONArray) obj;
//			return jsonObject;
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//
////read Write Json :-	
//
//	// Add Product OR Create Product for admin :
//	@PostMapping("/createProducts")
//	public String createProducts(@RequestBody ProductDetailss employee) throws Exception {
//		List newList = new ArrayList<>();
//		ObjectMapper objectMapper = new ObjectMapper();
//		ProductDetailss readValue = objectMapper.convertValue(employee, ProductDetailss.class);
//		File file = new File(
//				"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\ProductDetails.json");
//		// Create the file if it doesn't exist
//		if (!file.exists()) {
//			file.createNewFile();
//		}
//		// Read the existing data from the file into a list
//		List<ProductDetailss> employees = new ArrayList<>();
//		try {
//			FileInputStream fileInputStream = new FileInputStream(file);
//			TypeReference<List<ProductDetailss>> typeReference = new TypeReference<List<ProductDetailss>>() {
//			};
//			employees = objectMapper.readValue(fileInputStream, typeReference);
//			fileInputStream.close();
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//
//		List lst1 = new ArrayList<>();
//
//		List addList = new ArrayList<>();
//
//		for (ProductDetailss existProdID : employees) {
//
//			if (existProdID.getProduct_ID().equals(readValue.getProduct_ID())) {
//				System.out.println("new record added");
//
//				System.out.println("newList" + newList);
//
//				for (ProductDetailss lst : employees) {
//
//					if (lst.getProduct_ID().equals(readValue.getProduct_ID())
//							&& lst.getProduct_Name().equals(readValue.getProduct_Name())) {
//
//						Long product_Qnty = lst.getProduct_Qnty();
//						Long product_QntyInput = (Long) readValue.getProduct_Qnty();
//
//						Long sum = product_Qnty + product_QntyInput;
//						System.out.println(sum);
//						lst.setProduct_Qnty(sum);
//
//						lst1.add(lst);
//
//						lst1.remove(lst);
//
//						employees.addAll(lst1);
//					}
//
//					else {
//						addList.add(lst);
//						addList.add(employee);
//
//					}
//				}
//
//			}
//
//			else {
//
//				for (ProductDetailss alreadyExist : employees) {
//					if (alreadyExist.equals(readValue)) {
//						System.out.println("already Exist");
//					} else {
//						employees.add(readValue);
//					}
//				}
//
//				try {
//
//					FileWriter fileWriter = new FileWriter(file);
//					objectMapper.writeValue(fileWriter, employees);
//					fileWriter.close();
//					System.out.println("Employee Created");
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//
//				return "done";
//			}
//
//			System.out.println("validation happened");
//		}
//		return "done";
//	}
//
//	/// sample try must remove:
//	// validate list of product and input list :-
//
//	@PostMapping("/createProductdetails")
//	public List<ProductDetailss> createProductdetails(@RequestBody List<ProductDetailss> inputList) throws Exception {
//
//		ObjectMapper mp = new ObjectMapper();
//
//		List<ProductDetailss> inputList1 = mp.convertValue(inputList, new TypeReference<List<ProductDetailss>>() {
//		});
//		File file = new File(
//				"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\ProductDetailssss.json");
//		if (!file.exists()) {
//			file.createNewFile();
//		}
//		List<ProductDetailss> productList = new ArrayList<>();
//		try {
//			FileInputStream fileInputStream = new FileInputStream(file);
//			TypeReference<List<ProductDetailss>> typeReference = new TypeReference<List<ProductDetailss>>() {
//			};
//			productList = mp.readValue(fileInputStream, typeReference);
//			fileInputStream.close();
//		} catch (Exception e) {
//		}
//		System.out.println("List of ProductList" + productList);
//
//		List addition = new ArrayList<>();
//
//		for (Object ele : inputList1)
//
//		{
//
//			if ((productList.contains(ele) && !addition.contains(ele)))
//
//			{
//
//				continue;
//
//			}
//
//			else
//
//			{
//
//				if (!addition.contains(addition))
//
//				{
//
//					addition.add(ele);
//
//				}
//
//			}
//
//		}
//
//		productList.addAll(addition);
//
//		Set rel = new HashSet<>(productList);
//
//		productList.addAll(addition);
//
//		return productList;
//	}
//
//	/// Add Product Sample temprary without validation :-
//
//	@PostMapping("/addProduct")
//	public ResponseEntity<String> addProduct(@RequestBody ProductDetailss input) throws Exception {
//
//		System.out.println(input);
//		ObjectMapper objectMapper = new ObjectMapper();
//		File file = new File(
//				"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Backend_Services_May04_Version\\May04_23_Version\\vms\\src\\main\\resources\\AddProduct.json");
//
//		// Create the file if it doesn't exist
//		if (!file.exists()) {
//			file.createNewFile();
//		}
//		// Read the existing data from the file into a list
//		List<ProductDetailss> addProduct = new ArrayList<>();
//		try {
//			FileInputStream fileInputStream = new FileInputStream(file);
//			TypeReference<List<ProductDetailss>> typeReference = new TypeReference<List<ProductDetailss>>() {
//			};
//			addProduct = objectMapper.readValue(fileInputStream, typeReference);
//			fileInputStream.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		// Set the ID for the new employee
//
//		// Add the new employee to the list
//		addProduct.add(input);
//
//		// Write the updated data to the file
//		try {
//			FileWriter fileWriter = new FileWriter(file);
//			objectMapper.writeValue(fileWriter, addProduct);
//			fileWriter.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		return ResponseEntity.ok("Product created");
//	}
//
//	// System.out.println("added" +addList);
//
//	// }
//
//	// employees.remove(lst1);
//
//	/// GetById ProductDetails GetById :-
//
//	// List
//	// update or create by ID :-
//
//	@PostMapping("/updateByID")
//	public List<ProductDetailss> updateByID(@RequestBody List<ProductDetailss> input) throws Exception {
//
//		System.out.println(input);
//
//		ObjectMapper objectMapper = new ObjectMapper();
//		File file = new File(
//				"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\updateByIDPathVariables.json");
//
//		// Create the file if it doesn't exist
//		if (!file.exists()) {
//			file.createNewFile();
//		}
//		// Read the existing data from the file into a list
//		List<ProductDetailss> productList = new ArrayList<>();
//		try {
//			FileInputStream fileInputStream = new FileInputStream(file);
//			TypeReference<List<ProductDetailss>> typeReference = new TypeReference<List<ProductDetailss>>() {
//			};
//			productList = objectMapper.readValue(fileInputStream, typeReference);
//			fileInputStream.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		// my logics :-
//
//		for (ProductDetailss lst1 : input) {
//			boolean found = false;
//			String itemId2 = lst1.getProduct_ID();
//			for (ProductDetailss lst : productList) {
//				String itemId1 = lst.getProduct_ID();
//
//				if (itemId1.equals(itemId2)) {
//					System.out.println("Already Exist");
//					int stock = lst.getStock() + lst1.getStock();
//					System.out.println("availabe Stocks" + stock);
//
//					for (ProductDetailss obj : productList) {
//						if (obj.getProduct_ID() == itemId1) {
//							obj.setStock(stock);
//							System.out.println("Final Object" + obj);
//							break; // Stop iterating after updating the desired object
//						}
//					}
//
//					found = true;
//					break;
//				}
//			}
//			if (!found) {
//				productList.add(lst1);
//			}
//		}
//
//		try {
//			FileWriter fileWriter = new FileWriter(file);
//			objectMapper.writeValue(fileWriter, productList);
//			fileWriter.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		return productList;
//		// return ResponseEntity.ok("Product created");
//	}
//
//	// Delete By ID :-
//
//	@PostMapping("/deleteByID")
//	public List<ProductDetailss> deleteByID(@RequestBody List<ProductDetailss> input) throws Exception {
//
//		System.out.println(input);
//
//		ObjectMapper objectMapper = new ObjectMapper();
//
//		File file = new File(
//				"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\updateByID.json");
//
//		if (!file.exists()) {
//			file.createNewFile();
//		}
//		List<ProductDetailss> productList = new ArrayList<>();
//		try {
//			FileInputStream fileInputStream = new FileInputStream(file);
//			TypeReference<List<ProductDetailss>> typeReference = new TypeReference<List<ProductDetailss>>() {
//			};
//			productList = objectMapper.readValue(fileInputStream, typeReference);
//			fileInputStream.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		Iterator<ProductDetailss> iterator = productList.iterator();
//		while (iterator.hasNext()) {
//			ProductDetailss number = iterator.next();
//			String product_ID = number.getProduct_ID();
//
//			for (ProductDetailss ref1 : input) {
//				String child = ref1.getProduct_ID();
//
//				if (product_ID.equals(child)) {
//					System.out.println("If condition entered");
//					// productList.remove(pro);
//					iterator.remove();
//
//				}
//			}
//		}
//
//		try {
//			FileWriter fileWriter = new FileWriter(file);
//			objectMapper.writeValue(fileWriter, productList);
//			fileWriter.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		System.out.println("After Deleting By ID" + productList);
//
//		return productList;
//	}
//
//	// DeleteByID by using Path varibales :-
//
//	// Update By ID using 1 record to update :-
//
////My API Live :-
//
//// 1. Product Add / Edit  :-
//
//	// a. Admin Parts :
//
//	// Before Changes Given to PP:-
//
//	// After Changes :-
//
//	// update or create by ID :-
//
//	@PostMapping("/updateByIDRequestBody")
//	public List<ProductDetailss> updateByIDRequestBody(@RequestBody String input) throws Exception {
//
//
//		ObjectMapper objectMapper = new ObjectMapper();
//		Map inputValue = objectMapper.readValue(input, Map.class);
//		File file = new File(
//				"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\TotalAvailableProduct.json");
//
//		// Create the file if it doesn't exist
//		if (!file.exists()) {
//			file.createNewFile();
//		}
//		// Read the existing data from the file into a list
//		List<ProductDetailss> productList = new ArrayList<>();
//		try {
//			FileInputStream fileInputStream = new FileInputStream(file);
//			TypeReference<List<ProductDetailss>> typeReference = new TypeReference<List<ProductDetailss>>() {
//			};
//			productList = objectMapper.readValue(fileInputStream, typeReference);
//			fileInputStream.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		// my logics :-
//
//		String inputId = (String) inputValue.get("product_ID");
//		String inputName = (String) inputValue.get("product_Name");
//
//		int stock = (int) inputValue.get("stock");
//
//		List finalList = new ArrayList<>();
//
//		finalList.add(inputValue);
//		boolean found = false;
//
//		for (ProductDetailss lst : productList) {
//			String itemId1 = lst.getProduct_ID();
//			String itemName = lst.getProduct_Name();
//
//			if (itemId1.equals(inputId) && itemName.equals(inputName)) {
//				int stock1 = lst.getStock() + stock;
//
//				for (ProductDetailss obj : productList) {
//					if (obj.getProduct_ID() == itemId1) {
//						obj.setStock(stock1);
//						break; // Stop iterating after updating the desired object
//					}
//				}
//
//				found = true;
//				break;
//			}
//			if (itemId1.equals(inputId) && !itemName.equals(inputName)) {
//				System.out.println("not equls");
//				found = true;
//			}
//
//		}
//
//		if (!found) {
//			productList.addAll(finalList);
//		}
//
//		try {
//
//			System.out.println("Final List ---->" + productList);
//			FileWriter fileWriter = new FileWriter(file);
//			objectMapper.writeValue(fileWriter, productList);
//			fileWriter.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		return productList;
//	}
//
//// 2. Product Delete ById   :-
//
//	// Delete By ID :-
//
//	@PostMapping("/deleteByIDPathVariable/{inputID}")
//	public List<ProductDetailss> deleteByIDPathVariable(@PathVariable String inputID) throws Exception {
//
//		System.out.println(inputID);
//
//		ObjectMapper objectMapper = new ObjectMapper();
//
//		File file = new File(
//				"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\TotalAvailableProduct.json");
//
//		if (!file.exists()) {
//			file.createNewFile();
//		}
//		List<ProductDetailss> productList = new ArrayList<>();
//		try {
//			FileInputStream fileInputStream = new FileInputStream(file);
//			TypeReference<List<ProductDetailss>> typeReference = new TypeReference<List<ProductDetailss>>() {
//			};
//			productList = objectMapper.readValue(fileInputStream, typeReference);
//			fileInputStream.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		Iterator<ProductDetailss> iterator = productList.iterator();
//		while (iterator.hasNext()) {
//			ProductDetailss number = iterator.next();
//			String product_ID = number.getProduct_ID();
//
//			// String child = ref1.getProduct_ID();
//
//			if (product_ID.equals(inputID)) {
//				System.out.println("If condition entered");
//				// productList.remove(pro);
//				iterator.remove();
//
//			}
//
//		}
//
//		try {
//			FileWriter fileWriter = new FileWriter(file);
//			objectMapper.writeValue(fileWriter, productList);
//			fileWriter.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		System.out.println("After Deleting By ID" + productList);
//
//		return productList;
//	}
//
//	// User Part :
//
//	// 3. Product Get All :-
//
//	// Getall AddProduct JSON for Admin :-
//	@GetMapping("/getAllProductDeatils")
//	public JSONArray getAllProductDeatils() {
//		JSONParser parser = new JSONParser();
//		try {
//			Object obj = parser.parse(new FileReader(
//					"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\TotalAvailableProduct.json"));
//			JSONArray jsonObject = (JSONArray) obj;
//			return jsonObject;
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//
//	// 4. productDetailsGetById
//
//	// productDetailsGetById :-
//// Request Body :-
//
//	@PostMapping("/getProductDetailsById")
//	public List getProductDetailsById(@RequestBody String id) throws Exception {
//		ObjectMapper mp = new ObjectMapper();
//		List readValue = mp.readValue(id, List.class);
//
//		List finalList = new ArrayList<>();
//		List sls = new ArrayList<>();
//
//		for (Object ls : readValue) {
//			Map pro = (Map) ls;
//			String product_ID = (String) pro.get("product_ID");
//			Map<String, String> mpm = new HashMap<>();
//
//			mpm.put("product_ID", product_ID);
//
//			sls.add(mpm);
//
//		}
//		String url = "http://localhost:8080/getAllProductDeatils";
//		ResponseEntity<List> forEntity = rest.getForEntity(url, List.class);
//		List res = forEntity.getBody();
//
//		for (Object firstObject : sls) {
//
//			for (Object proList : res) {
//
//				Map ms = (Map) firstObject;
//
//				Map proMap = (Map) proList;
//
//				if (ms.get("product_ID").equals(proMap.get("product_ID"))) {
//
//					Map mpl = new HashMap<>();
//					String product_ID = (String) proMap.get("product_ID");
//					int product_Qnty = (int) proMap.get("product_Qnty");
//					String product_Price = (String) proMap.get("product_Price");
//					String product_Name = (String) proMap.get("product_Name");
//
//					mpl.put("product_ID", product_ID);
//					mpl.put("product_Qnty", product_Qnty);
//					mpl.put("product_Price", product_Price);
//					mpl.put("product_Name", product_Name);
//					finalList.add(mpl);
//				}
//
//			}
//
//		}
//
//		System.out.println(finalList);
//		return finalList;
//	}
//
//	// @PathVariable :-
//
//	// productDetailsGetById :-
//
//	@GetMapping("/getProductDetailsByIdInput/{id}")
//	public List getProductDetailsByIdInput(@PathVariable String id) throws Exception {
//		ObjectMapper mp = new ObjectMapper();
//		List finalList = new ArrayList<>();
//		List sls = new ArrayList<>();
//		String url = "http://localhost:8080/getAllProductDeatils";
//		ResponseEntity<List> forEntity = rest.getForEntity(url, List.class);
//
//		List res = forEntity.getBody();
//
//		for (Object proList : res) {
//
//			Map ms = (Map) proList;
//
//			Map proMap = (Map) proList;
//
//			if (id.equals(proMap.get("product_ID"))) {
//
//				Map mpl = new HashMap<>();
//				String product_ID = (String) proMap.get("product_ID");
//				int product_Qnty = (int) proMap.get("product_Qnty");
//				String product_Price = (String) proMap.get("product_Price");
//				String product_Name = (String) proMap.get("product_Name");
//
//				mpl.put("product_ID", product_ID);
//				mpl.put("product_Qnty", product_Qnty);
//				mpl.put("product_Price", product_Price);
//				mpl.put("product_Name", product_Name);
//				finalList.add(mpl);
//			}
//
//		}
//
//		return finalList;
//	}
//
//	// Method 2 : duplicate of previous :-
//	// No Need:
//
//	@GetMapping("/getProductDetailsByIdInputMethodTwo/{id}")
//	public List getProductDetailsByIdInputMethodTwo(@PathVariable String id) throws Exception {
//		ObjectMapper mp = new ObjectMapper();
//		List finalList = new ArrayList<>();
//		List sls = new ArrayList<>();
//		String url = "http://localhost:8080/getAllProductDeatils";
//		ResponseEntity<List> forEntity = rest.getForEntity(url, List.class);
//
//		List res = forEntity.getBody();
//
//		List<ProductDetailss> productList = mp.convertValue(res, new TypeReference<List<ProductDetailss>>() {
//		});
//
//		for (ProductDetailss proList : productList) {
//
//			if (proList.getProduct_ID().equals(id)) {
//				finalList.add(proList);
//			}
//
//		}
//
//		return finalList;
//	}
//
//	// Add User to Cart :-
//
//	@PostMapping("/addCartJson/{userid}/{cartid}")
//	public List<Cart> addCartJson(@PathVariable String userid, @PathVariable String cartid,
//			@RequestBody String userProductList) throws Exception {
//
//		ObjectMapper objectMapper = new ObjectMapper();
//		List inputValue = objectMapper.readValue(userProductList, List.class);
//
//		File file = new File(
//				"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\AddCart.json");
//
//		// Create the file if it doesn't exist
//		if (!file.exists()) {
//			file.createNewFile();
//		}
//		// Read the existing data from the file into a list
//		List<Cart> cartList = new ArrayList<>();
//		try {
//			FileInputStream fileInputStream = new FileInputStream(file);
//			TypeReference<List<Cart>> typeReference = new TypeReference<List<Cart>>() {
//			};
//			cartList = objectMapper.readValue(fileInputStream, typeReference);
//			fileInputStream.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		boolean found = false;
//		List userList = new ArrayList<>();
//
//		for (Cart lst : cartList) {
//			String userID = lst.getUserid();
//
//			if (userid.equals(userID)) {
//				lst.setProductList(inputValue);
//				found = true;
//				break;
//			}
//
//		}
//
//		if (!found) {
//
//			Cart newCart = new Cart();
//			newCart.setCartid(cartid);
//			newCart.setUserid(userid);
//			newCart.setProductList(inputValue);
//			cartList.add(newCart);
//
//		}
//
//		try {
//
//			System.out.println("Final List ---->" + cartList);
//			FileWriter fileWriter = new FileWriter(file);
//			objectMapper.writeValue(fileWriter, cartList);
//			fileWriter.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		return cartList;
//
//	}
//
//	// add to cart List from input :
//	// Duplicate of previous :-
//	// Working Fine :-
//	// Add User to Cart :-
//
//	@PostMapping("/addCartJsonDuplicate/{userid}/{cartid}")
//	public List addCartJsonDuplicate(@PathVariable String userid, @PathVariable String cartid,
//			@RequestBody String userProductList) throws Exception {
//		ObjectMapper objectMapper = new ObjectMapper();
//		List inputValue = objectMapper.readValue(userProductList, List.class);
//		File file = new File(
//				"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\TotalAvailableProduct.json");
//
//		// Create the file if it doesn't exist
//		if (!file.exists()) {
//			file.createNewFile();
//		}
//		// Read the existing data from the file into a list
//		List<ProductDetailss> totalAvailableProduct = new ArrayList<>();
//		try {
//			FileInputStream fileInputStream = new FileInputStream(file);
//			TypeReference<List<ProductDetailss>> typeReference = new TypeReference<List<ProductDetailss>>() {
//			};
//			totalAvailableProduct = objectMapper.readValue(fileInputStream, typeReference);
//			fileInputStream.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		List getUserList = new ArrayList<>();
//
//		for (Object input : inputValue) {
//			for (ProductDetailss lst : totalAvailableProduct) {
//				String product_ID = lst.getProduct_ID();
//				String s = String.valueOf(input);
//
//				if (s.equals(product_ID)) {
//					System.out.println("Enter");
//					System.out.println(lst);
//					getUserList.add(lst);
//				}
//
//			}
//
//		}
//		File file1 = new File(
//				"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\AddCart.json");
//
//		// Create the file if it doesn't exist
//		if (!file1.exists()) {
//			file1.createNewFile();
//		}
//		// Read the existing data from the file into a list
//		List<Cart> cartList = new ArrayList<>();
//		try {
//			FileInputStream fileInputStream = new FileInputStream(file1);
//			TypeReference<List<Cart>> typeReference = new TypeReference<List<Cart>>() {
//			};
//			cartList = objectMapper.readValue(fileInputStream, typeReference);
//			fileInputStream.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		boolean found = false;
//		List userList = new ArrayList<>();
//
//		for (Cart lst : cartList) {
//			String userID = lst.getUserid();
//
//			if (userid.equals(userID)) {
//				lst.setProductList(getUserList);
//				found = true;
//				break;
//			}
//
//		}
//
//		if (!found) {
//
//			Cart newCart = new Cart();
//			newCart.setCartid(cartid);
//			newCart.setUserid(userid);
//			newCart.setProductList(getUserList);
//			cartList.add(newCart);
//
//		}
//
//		try {
//
//			FileWriter fileWriter = new FileWriter(file1);
//			objectMapper.writeValue(fileWriter, cartList);
//			fileWriter.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		List finalResult = new ArrayList<>();
//
//		for (Cart view : cartList) {
//			if (userid.equals(view.getUserid())) {
//				finalResult.add(view);
//
//			}
//
//		}
//		return finalResult;
//	}
//
//	// Add to Cart :- // pathVariable
//	// Input from path Variable :-
//	// deuplicate from previous one
//
//	@PostMapping("/addCartJsonDup/{userid}/{cartid}/{productID}")
//	public List addCartJsonDup(@PathVariable String userid, @PathVariable String cartid, @PathVariable String productID
//	// @RequestBody String userProductList
//	) throws Exception {
//		ObjectMapper objectMapper = new ObjectMapper();
//		// List inputValue = objectMapper.readValue(userProductList, List.class);
//		File file = new File(
//				"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\TotalAvailableProduct.json");
//
//		// Create the file if it doesn't exist
//		if (!file.exists()) {
//			file.createNewFile();
//		}
//		// Read the existing data from the file into a list
//		List<ProductDetailss> totalAvailableProduct = new ArrayList<>();
//		try {
//			FileInputStream fileInputStream = new FileInputStream(file);
//			TypeReference<List<ProductDetailss>> typeReference = new TypeReference<List<ProductDetailss>>() {
//			};
//			totalAvailableProduct = objectMapper.readValue(fileInputStream, typeReference);
//			fileInputStream.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		List<ProductDetailss> getUserList = new ArrayList<>();
//		// for (Object input : inputValue) {
//		for (ProductDetailss lst : totalAvailableProduct) {
//			String product_ID = lst.getProduct_ID();
//			// String s = String.valueOf(input);
//
//			if (productID.equals(product_ID)) {
//				System.out.println("Enter");
//				System.out.println(lst);
//				getUserList.add(lst);
//
//			}
//
//		}
//
//		// }
//		File file1 = new File(
//				"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\AddCart.json");
//
//		// Create the file if it doesn't exist
//		if (!file1.exists()) {
//			file1.createNewFile();
//		}
//		// Read the existing data from the file into a list
//		List<Cart> cartList = new ArrayList<>();
//		try {
//			FileInputStream fileInputStream = new FileInputStream(file1);
//			TypeReference<List<Cart>> typeReference = new TypeReference<List<Cart>>() {
//			};
//			cartList = objectMapper.readValue(fileInputStream, typeReference);
//			fileInputStream.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		boolean found = false;
//		List userList = new ArrayList<>();
//
//		for (Cart lst : cartList) {
//			String userID = lst.getUserid();
//			if (userid.equals(userID)) {
//				List<ProductDetailss> productList = lst.getProductList();
//
//				System.out.println(productList);
//
//				List finalList = new ArrayList<>();
//				for (ProductDetailss rs : getUserList) {
//					for (ProductDetailss lststr : productList) {
//
//						String product_ID = rs.getProduct_ID();
//						String product_ID2 = lststr.getProduct_ID();
//
//						if (product_ID.equals(product_ID2)) {
//							found = true;
//						}
//
//					}
//
//					if (!found) {
//						productList.add(rs);
//					}
//				}
//				System.out.println("Final +++" + productList);
//
//				// lst.setProductList(getUserList);
//				found = true;
//				break;
//			}
//
//		}
//
//		if (!found) {
//
//			Cart newCart = new Cart();
//			newCart.setCartid(cartid);
//			newCart.setUserid(userid);
//			newCart.setProductList(getUserList);
//			cartList.add(newCart);
//
//		}
//
//		try {
//
//			FileWriter fileWriter = new FileWriter(file1);
//			objectMapper.writeValue(fileWriter, cartList);
//			fileWriter.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		List finalResult = new ArrayList<>();
//
//		for (Cart view : cartList) {
//			if (userid.equals(view.getUserid())) {
//				finalResult.add(view);
//
//			}
//
//		}
//		return finalResult;
//	}
//
//	// Remove from cart using ID : // pathvariable :-
//
//	@PostMapping("/removeCart/{userid}/{cartid}/{productID}")
//	public List removeCart(@PathVariable String userid, @PathVariable String cartid, @PathVariable String productID
//	// @RequestBody String userProductList
//	) throws Exception {
//		ObjectMapper objectMapper = new ObjectMapper();
//		// List inputValue = objectMapper.readValue(userProductList, List.class);
//		File file = new File(
//				"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\TotalAvailableProduct.json");
//
//		// Create the file if it doesn't exist
//		if (!file.exists()) {
//			file.createNewFile();
//		}
//		// Read the existing data from the file into a list
//		List<ProductDetailss> totalAvailableProduct = new ArrayList<>();
//		try {
//			FileInputStream fileInputStream = new FileInputStream(file);
//			TypeReference<List<ProductDetailss>> typeReference = new TypeReference<List<ProductDetailss>>() {
//			};
//			totalAvailableProduct = objectMapper.readValue(fileInputStream, typeReference);
//			fileInputStream.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		List<ProductDetailss> getUserList = new ArrayList<>();
//		// for (Object input : inputValue) {
//		for (ProductDetailss lst : totalAvailableProduct) {
//			String product_ID = lst.getProduct_ID();
//			// String s = String.valueOf(input);
//
//			if (productID.equals(product_ID)) {
//				System.out.println("Enter");
//				System.out.println(lst);
//				getUserList.add(lst);
//
//			}
//
//		}
//
//		// }
//		File file1 = new File(
//				"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\AddCart.json");
//
//		// Create the file if it doesn't exist
//		if (!file1.exists()) {
//			file1.createNewFile();
//		}
//		// Read the existing data from the file into a list
//		List<Cart> cartList = new ArrayList<>();
//		try {
//			FileInputStream fileInputStream = new FileInputStream(file1);
//			TypeReference<List<Cart>> typeReference = new TypeReference<List<Cart>>() {
//			};
//			cartList = objectMapper.readValue(fileInputStream, typeReference);
//			fileInputStream.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		boolean found = false;
//		List userList = new ArrayList<>();
//
//		for (Cart lst : cartList) {
//			String userID = lst.getUserid();
//			if (userid.equals(userID)) {
//				List<ProductDetailss> productList = lst.getProductList();
//
//				System.out.println(productList);
//
//				List finalList = new ArrayList<>();
//				for (ProductDetailss rs : getUserList) {
//					for (ProductDetailss lststr : productList) {
//
//						String product_ID = rs.getProduct_ID();
//						String product_ID2 = lststr.getProduct_ID();
//
//						if (product_ID.equals(product_ID2)) {
//							found = true;
//						}
//
//					}
//
//					if (!found) {
//						productList.add(rs);
//					}
//				}
//				System.out.println("Final +++" + productList);
//
//				// lst.setProductList(getUserList);
//				found = true;
//				break;
//			}
//
//		}
//
//		if (!found) {
//
//			Cart newCart = new Cart();
//			newCart.setCartid(cartid);
//			newCart.setUserid(userid);
//			newCart.setProductList(getUserList);
//			cartList.add(newCart);
//
//		}
//
//		try {
//
//			FileWriter fileWriter = new FileWriter(file1);
//			objectMapper.writeValue(fileWriter, cartList);
//			fileWriter.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		List finalResult = new ArrayList<>();
//
//		for (Cart view : cartList) {
//			if (userid.equals(view.getUserid())) {
//				finalResult.add(view);
//
//			}
//
//		}
//		return finalResult;
//	}
//
//	///
//
//	@PostMapping("/remove/{userid}/{cartid}/{productID}")
//	public List<Cart> remove(@PathVariable String userid, @PathVariable String cartid, @PathVariable String productID)
//			throws Exception {
//
//		// System.out.println(inputID);
//
//		ObjectMapper objectMapper = new ObjectMapper();
//
//		File file = new File(
//				"D:\\Rajesh_Document\\Internal_Project\\VMS_Camunda_8_Project\\VMS_Camunda_8_Workspace\\Order_Fullfillment_Cloud_Version\\src\\main\\resources\\jsonreader\\AddCart.json");
//
//		if (!file.exists()) {
//			file.createNewFile();
//		}
//		List<Cart> productList = new ArrayList<>();
//		try {
//			FileInputStream fileInputStream = new FileInputStream(file);
//			TypeReference<List<Cart>> typeReference = new TypeReference<List<Cart>>() {
//			};
//			productList = objectMapper.readValue(fileInputStream, typeReference);
//			fileInputStream.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		for (Cart ref : productList) {
//
//			if (ref.getUserid().equals(userid)) {
//				List<ProductDetailss> productList2 = ref.getProductList();
//
//				Iterator<ProductDetailss> iterator = productList2.iterator();
//				while (iterator.hasNext()) {
//					ProductDetailss number = iterator.next();
//
//					if (number.getProduct_ID().equals(productID)) {
//						iterator.remove();
//					}
//
//					System.out.println(productList);
//
//				}
//			}
//
//		}
//
//		try {
//			FileWriter fileWriter = new FileWriter(file);
//			objectMapper.writeValue(fileWriter, productList);
//			fileWriter.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		List finalResult = new ArrayList<>();
//
//		for (Cart view : productList) {
//			if (userid.equals(view.getUserid())) {
//				finalResult.add(view);
//
//			}
//
//		}
//		return finalResult;
//	}
//
//	// my logics :-
////
////		String inputId = (String) inputValue.get("product_ID");
////		String inputName = (String) inputValue.get("product_Name");
////
////		int stock = (int) inputValue.get("stock");
////
////		List finalList = new ArrayList<>();
////
////		finalList.add(inputValue);
////		boolean found = false;
////
////		for (ProductDetailss lst :cartList) {
////			String itemId1 = lst.getProduct_ID();
////			String itemName = lst.getProduct_Name();
////
////			if (itemId1.equals(inputId) && itemName.equals(inputName)) {
////				// System.out.println("Already Exist");
////				int stock1 = lst.getStock() + stock;
////				// System.out.println("availabe Stocks" + stock);
////
////				for (ProductDetailss obj : cartListList) {
////					if (obj.getProduct_ID() == itemId1) {
////						obj.setStock(stock1);
////						// System.out.println("Final Object" + obj);
////						break; // Stop iterating after updating the desired object
////					}
////				}
////
////				found = true;
////				break;
////			}
////			if (itemId1.equals(inputId) && !itemName.equals(inputName)) {
////				System.out.println("not equls");
////				found = true;
////			}
//////			else {
//////				System.out.println("Invalid ID");
//////			}
////		}
////
////		if (!found) {
////			productList.addAll(finalList);
////		}
////
////		try {
////
////			System.out.println("Final List ---->" + productList);
////			FileWriter fileWriter = new FileWriter(file);
////			objectMapper.writeValue(fileWriter, productList);
////			fileWriter.close();
////		} catch (IOException e) {
////			e.printStackTrace();
////		}
////
////		return productList;
//}
