package test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lexruntime.AmazonLexRuntime;
import com.amazonaws.services.lexruntime.AmazonLexRuntimeClientBuilder;
import com.amazonaws.services.lexruntime.model.PostTextRequest;
import com.amazonaws.services.lexruntime.model.PostTextResult;
import com.google.gson.Gson;

import model.Request;


public class testLex {
    private static final String botName =  "photoSearch";

    private static final String botAlias = "photoSearch";

    private static final AmazonLexRuntime client = AmazonLexRuntimeClientBuilder.standard()
            .withRegion(Regions.US_EAST_1).build();
    
	public static void main(String[] args) {
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
        textRequest.setInputText("search for dog cat dog    cat asdf");
        System.out.println("call lex: " + botName);
        PostTextResult textResult = client.postText(textRequest);
        String state = textResult.getDialogState();
        String response = textResult.getMessage();
        System.out.println(textResult.getSlots().entrySet());
        String esQuery = "{\"query\": {\"bool\": {\"should\": [";
        String esTail = "]}}}";
        int count = 0;
        for(Entry<String, String> entry: textResult.getSlots().entrySet()) {
        	if(entry.getValue() != null) {
        		if(count != 0) esQuery += ",";
        		esQuery += "{ \"match\": { \"genre\": \""+ entry.getValue()+"\"}}";
        	}
        	count ++;
        }
        
        esQuery += esTail;
        System.out.println(esQuery);
//        esQuery = "";
        try {
			new testLex().post("https://search-test-bxlqo7ebjtufuka2qibvm7lyve.us-east-1.es.amazonaws.com/_search", esQuery);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public void post(String urlQuery, String esQuery) throws Exception {
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
	}
}
