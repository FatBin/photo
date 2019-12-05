package com.amazonaws.lambda.demo;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lexruntime.AmazonLexRuntime;
import com.amazonaws.services.lexruntime.AmazonLexRuntimeClientBuilder;
import com.amazonaws.services.lexruntime.model.PostTextRequest;
import com.amazonaws.services.lexruntime.model.PostTextResult;
import com.google.gson.Gson;

import model.Request;

public class LF2 implements RequestHandler<Object, String> {
    private static final String botName =  "photoSearch";

    private static final String botAlias = "photoSearch";

    private static final AmazonLexRuntime client = AmazonLexRuntimeClientBuilder.standard()
            .withRegion(Regions.US_EAST_1).build();
    @Override
    public String handleRequest(Object input, Context context) {
//        context.getLogger().log("Input: " + input);
		Request request = new Gson().fromJson("{\r\n" + 
				"  \"id\": \"asdf\",\r\n" + 
				"  \"message\": \"asdccf\"\r\n" + 
				"}", Request.class);
		System.out.print(request.getMessage()+ request.getId());
		List<String> list = new ArrayList<String>();
		list.add("hello");
		list.add("hello2");
		list.add("hello3");
		String jsonString = new Gson().toJson(list);
		System.out.println(jsonString);
        PostTextRequest textRequest = new PostTextRequest();
        textRequest.setBotName(botName);
        textRequest.setBotAlias(botAlias);
        textRequest.setUserId("120");
        textRequest.setInputText("search for dog cat dogcat asdf");
        System.out.println("call lex: " + botName);
        PostTextResult textResult = client.postText(textRequest);
        String state = textResult.getDialogState();
        String response = textResult.getMessage();
        
        System.out.println(textResult.getSlots().entrySet());
        // TODO: implement your handler
        
        try {
//			post("https://search-test-bxlqo7ebjtufuka2qibvm7lyve.us-east-1.es.amazonaws.com/_search");
        	post("https://vpc-xl-es-single-iweyxmypvbdfodxpdfmxpy5c6i.us-east-1.es.amazonaws.com/_search");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e);
		}
        return "Hello from Lambda!";
    }

    
	public void post(String urlQuery) throws Exception {
	    URL url = new URL(urlQuery);

	    URLConnection urlConnection = url.openConnection();
	    urlConnection.setRequestProperty("Content-Type", "application/json");
	    HttpURLConnection connection = (HttpURLConnection) urlConnection;
	    connection.setRequestMethod("POST");
	    
	    connection.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes("{\r\n" + 
        		"  \"query\": {\r\n" + 
        		"    \"bool\": {\r\n" + 
        		"      \"should\": [\r\n" + 
        		"        { \"match\": { \"labels\": \"text\"}}\r\n" + 
        		"      ]\r\n" + 
        		"    }\r\n" + 
        		"  }\r\n" + 
        		"}");
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
	}
}
