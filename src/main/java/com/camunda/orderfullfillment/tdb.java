package com.camunda.orderfullfillment;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import io.camunda.tasklist.exception.TaskListException;

public class tdb {
	
	
 public void authenticate() throws TaskListException {
		 
		 String authUrl="https://login.cloud.camunda.io/oauth/token";
		 String clientId = "k9EUs6Ul6PjyBu_WZED7X2ugzjrdz_5_";
		 String clientSecret="C2Fzom-kB2ih_ILVhbIJzitw8OAzB2z7Mhx.jCH7GYJKTQXwagRpdsZTUEDVMqED";
		 String baseUrl = "https://bru-2.tasklist.camunda.io/1a8d8e18-4054-4bd2-afad-6f2adf8c58b8";
	        try {
	            URL url = new URL(baseUrl);
	            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	            conn.setUseCaches(false);
	            conn.setConnectTimeout(20000000 * 5);
	            conn.setDoOutput(true);
	            conn.setDoInput(true);
	            conn.setRequestMethod("POST");
	            conn.setRequestProperty("Content-Type", "application/json");
	            conn.setRequestProperty("Accept", "application/json");
	            conn.setRequestProperty("charset", "utf-8");
	            String data = "{\"grant_type\":\"client_credentials\", \"audience\":\""+baseUrl+"\", \"client_id\": \""
	                    + clientId + "\", \"client_secret\":\"" + clientSecret + "\"}";
	            conn.getOutputStream().write(data.getBytes(StandardCharsets.UTF_8));
	            conn.connect();

	            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
	                System.out.println("Connection Received....");
	            } else {
	                throw new TaskListException("Error "+conn.getResponseCode()+" obtaining access token : "+conn.getResponseMessage());
	            }
	        } catch (IOException e) {
	            throw new TaskListException(e);
	        }
	    }

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		new tdb().authenticate();
	}

}
