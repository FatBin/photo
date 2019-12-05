package com.amazonaws.lambda.demo;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.lexruntime.AmazonLexRuntime;
import com.amazonaws.services.lexruntime.AmazonLexRuntimeClientBuilder;
import com.amazonaws.services.lexruntime.model.PostTextRequest;
import com.amazonaws.services.lexruntime.model.PostTextResult;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;

import model.Request;
import test.testLex;

public class Lf1 implements RequestStreamHandler {
    private static final String botName =  "photoSearch";

    private static final String botAlias = "photoSearch";
    
    private static final String es_url = "https://vpc-xl-es-single-iweyxmypvbdfodxpdfmxpy5c6i.us-east-1.es.amazonaws.com/_search";

    private static final AmazonLexRuntime client = AmazonLexRuntimeClientBuilder.standard()
            .withRegion(Regions.US_EAST_1).build();
    
    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException{
    	//get userID and message from request
    	String json = new String(ByteStreams.toByteArray(inputStream));
    	System.out.println(json);
    	Request request = new Gson().fromJson(json, Request.class);
        String userId = request.getId();
        String message = request.getMessage();
        
        //post message to lex and get the photo types
    	PostTextRequest textRequest = new PostTextRequest();
        textRequest.setBotName(botName);
        textRequest.setBotAlias(botAlias);
        textRequest.setUserId(userId);
        textRequest.setInputText(message.replace("and", " "));
        System.out.println("call lex: " + botName);
        PostTextResult textResult = client.postText(textRequest);
        System.out.println("state" + textResult.getDialogState());
        System.out.println("message" + textResult.getMessage());
        //make the elasticsearch query
        String esQuery = "{\"query\": {\"bool\": {\"should\": [";
        String esTail = "]}}}";

        Set<Entry<String, String>> types = new HashSet<Map.Entry<String,String>>();
        try {
			types = textResult.getSlots().entrySet();
		} catch (Exception e) {
			System.out.println(e);
		}
        int count = 0;
        for(Entry<String, String> entry: types) {
        	if(entry.getValue() != null) {
        		if(count != 0) esQuery += ",";
        		esQuery += "{ \"match\": { \"labels\": \""+ entry.getValue()+"\"}}";
            	count ++;
        	}

        }
        esQuery += esTail;
		System.out.println(esQuery);
        //call the es API and analyze the result
        List<String> result = new ArrayList<String>();
        if (types.size() > 0) {
            try {
    			String esJson = post(es_url, esQuery);

    			JSONObject jsonObject = new JSONObject(esJson);
    			JSONArray jsonArray = jsonObject.getJSONObject("hits").getJSONArray("hits");
    			for (int i = 0; i < jsonArray.length(); i++) {
    					JSONObject hit = jsonArray.getJSONObject(i);
    					JSONObject source = hit.getJSONObject("_source");
    					String objectKey = source.getString("objectKey");
    					if (!result.contains(objectKey)) {
    						result.add(objectKey);
    						System.out.println(objectKey);
    					}
    			}
    		} catch (IOException e) {
    			System.out.println("cannot get data from es");
    			e.printStackTrace();
    		} catch (Exception e) {
    			System.out.println("formation failed");
    			e.printStackTrace();
    		}
		}
        System.out.print(result.size());
        try (PrintWriter p = new PrintWriter(outputStream)) {
            p.println(new Gson().toJson(result));
        }
    }
    
    
    
    
	public String post(String urlQuery, String esQuery) throws IOException {
	    URL url = new URL(urlQuery);

	    URLConnection urlConnection = url.openConnection();
	    urlConnection.setRequestProperty("Content-Type", "application/json");
	    HttpURLConnection connection = (HttpURLConnection) urlConnection;
	    connection.setRequestMethod("POST");
	    
	    connection.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(esQuery);
        wr.flush();
        wr.close();
	    // 打开连接
	    BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),"utf-8")); // 获取输入流
	    String line = null;
	    StringBuilder sb = new StringBuilder();
	    while ((line = br.readLine()) != null) {
	        sb.append(line + "\n");
	    }
	    System.out.println(sb.toString());
	    return sb.toString();

	}

}
