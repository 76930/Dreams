package com.wearfit.demo.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import net.sf.json.JSONObject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * 服务器端http请求工具类
 */
public class HttpUtil {
	
	/**
	 * 发起get请求
	 * <p>
	 * 替换ACCESS_TOKEN，访问凭证过期时，重新获取凭证并发起原有调用
	 */
	public static String doGet(String url){
		String realUrl = url.replace("ACCESS_TOKEN",
				AccessTokenUtil.getTokenStr());
		String rs = executeGet(realUrl);
		JSONObject json = JSONObject.fromObject(rs);
		// 访问凭证失效时，重新进行一次获取凭证并发起原来业务调用
		if (json.containsKey("errcode") 
				&& (json.getInt("errcode") == 40001
						|| json.getInt("errcode") == 40014
						|| json.getInt("errcode") == 41001 
						|| json.getInt("errcode") == 42001)) {
			realUrl = url.replace("ACCESS_TOKEN",
					AccessTokenUtil.refreshAndGetToken());
			rs = executeGet(realUrl);
		}
		return rs;
	}

	/**
	 * access_token 接口直接调用，其它调用doGet
	 */
	public static String executeGet(String url){
		try {
			HttpGet httpGet = new HttpGet(url);
			HttpClient httpclient = new DefaultHttpClient();
			httpclient = WebClientDevWrapper.wrapClient(httpclient);
			HttpResponse response = httpclient.execute(httpGet);
			String resultContent = new Utf8ResponseHandler()
					.handleResponse(response);
			System.out.println("result=" + resultContent);
			return resultContent;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 发起post请求
	 * <p>
	 * 替换ACCESS_TOKEN，访问凭证过期时，重新获取凭证并发起原有调用
	 */
	public static String doPost(String url, String body) {
		String realUrl = url.replace("ACCESS_TOKEN",
				AccessTokenUtil.getTokenStr());
		String rs = executePost(realUrl, body);
		JSONObject json = JSONObject.fromObject(rs);
		// 访问凭证失效时，重新进行一次获取凭证并发起原来业务调用
		if (json.containsKey("errcode") 
				&& (json.getInt("errcode") == 40001
						|| json.getInt("errcode") == 40014
						|| json.getInt("errcode") == 41001 
						|| json.getInt("errcode") == 42001)) {
			realUrl = url.replace("ACCESS_TOKEN",
					AccessTokenUtil.refreshAndGetToken());
			rs = executePost(realUrl, body);
		}
		return rs;
	}

	private static String executePost(String url, String body){
		try {
			HttpPost httpPost = new HttpPost(url);
			StringEntity entity = new StringEntity(body, "UTF-8");
			httpPost.setEntity(entity);
			HttpClient httpclient = new DefaultHttpClient();
			httpclient = WebClientDevWrapper.wrapClient(httpclient);
			HttpResponse response = httpclient.execute(httpPost);
			String resultContent = new Utf8ResponseHandler()
					.handleResponse(response);
			System.out.println("result=" + resultContent);
			return resultContent;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * utf-8编码
	 */
	static class Utf8ResponseHandler implements ResponseHandler<String> {
		public String handleResponse(final HttpResponse response)
				throws HttpResponseException, IOException {
			final StatusLine statusLine = response.getStatusLine();
			final HttpEntity entity = response.getEntity();
			if (statusLine.getStatusCode() >= 300) {
				EntityUtils.consume(entity);
				throw new HttpResponseException(statusLine.getStatusCode(),
						statusLine.getReasonPhrase());
			}
			return entity == null ? null : EntityUtils
					.toString(entity, "UTF-8");
		}

	}
	
	// 普通get请求，天气接口在用
	private static final String USER_AGENT = "Mozilla/5.0";
	// HTTP GET请求
    public static String sendGet(String url) throws Exception {


        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //默认值我GET
        con.setRequestMethod("GET");

        //添加请求头
        con.setRequestProperty("User-Agent", USER_AGENT);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        //打印结果
        return response.toString();
    }

}
