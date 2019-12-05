package test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class TestURL {
	public static void main(String[] args) {
		TestURL testURL = new TestURL();
		try {
//			testURL.get("https://search-restaurants-cqeyenxgif3p2ayepihy77mpsy.us-west-2.es.amazonaws.com/_search");
			testURL.post("https://search-test-bxlqo7ebjtufuka2qibvm7lyve.us-east-1.es.amazonaws.com/_search");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void post(String urlQuery) throws Exception {
	    URL url = new URL(urlQuery);

	    URLConnection urlConnection = url.openConnection();
	    urlConnection.setRequestProperty("Content-Type", "application/json");
	    HttpURLConnection connection = (HttpURLConnection) urlConnection;
	    connection.setRequestMethod("POST");
	    
	    connection.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
//        wr.writeBytes("{	\"query\": \"SELECT * FROM restaurants WHERE City = 'Brooklyn' and Cuisine = 'pizza'\" }");
        wr.writeBytes("{\r\n" + 
        		"  \"query\": {\r\n" + 
        		"    \"bool\": {\r\n" + 
        		"      \"should\": [\r\n" + 
        		"        { \"match\": { \"genre\":  \""+"Action"+"\" }},\r\n" + 
        		"        { \"match\": { \"genre\": \"Mystery\"   }}\r\n" + 
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
	
	public void get(String urlquery) throws Exception{
		URL url = new URL(urlquery);
	    URLConnection urlConnection = url.openConnection();                                                    // 打开连接
	    BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),"utf-8")); // 获取输入流
	    String line = null;
	    StringBuilder sb = new StringBuilder();
	    while ((line = br.readLine()) != null) {
	        sb.append(line + "\n");
	    }
	    System.out.println(sb.toString().length());
	    System.out.println(sb.toString());
	}
}
