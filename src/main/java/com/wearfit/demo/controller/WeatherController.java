package com.wearfit.demo.controller;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.wearfit.demo.utils.HttpUtil;

import net.sf.json.JSONObject;

/**
 * API天气接口，供安卓、iOS调用
 * @author jie
 * @date 2019年7月6日
 */
@RestController
@RequestMapping("/weather")
public class WeatherController {
	// 应用appid
	final String appId = "n5e0os36";
	// 使用者秘钥
	final String consumerKey = "dj0yJmk9UDlLVGVoaEdqcmRKJmQ9WVdrOWJqVmxNRzl6TXpZbWNHbzlNQS0tJnM9Y29uc3VtZXJzZWNyZXQmc3Y9MCZ4PTA1";
	// 消费者密码
	final String consumerSecret = "8fc74d5002dad0397db1938db1f3e307e58f5868";
	// 天气API接口URL
	final String url = "https://weather-ydn-yql.media.yahoo.com/forecastrss";
	// 百度地图api，根据地址获取经纬度或者根据经纬度过去所属市区（洲）
	final String baiduUrl = "http://api.map.baidu.com/geocoder?output=json";

	/**
	 * 获取天气API接口，供Android和iOS调用
	 * @param lat 维度
	 * @param lon 经度
	 * @return
	 */
	@RequestMapping(value = "/getWeather", method = RequestMethod.GET)
	@ResponseBody
	public String getWeather(Double lat, Double lon) {
		JSONObject webObject = new JSONObject();
		JSONObject weatherObject = new JSONObject();
		// 默认请求成功
		Integer code = 200;
		try {
			
			String authorizationLine = getAuthorizationLine(lat,lon);
			
			String result =  request(url + "?lat="+lat+"&lon="+lon+"&format=json",authorizationLine,appId);
			
			// 转JSONObject
			
			if(result != null) {
				
				JSONObject obj = JSONObject.fromObject(result);
				// 获取当前天气信息JSONArray
				JSONObject info = obj.getJSONObject("current_observation");
				// 获取不到所在地的天气
				boolean flag = info.isEmpty() || info.isNullObject();
				if(!flag) {
					// 获取到天气
					String tempCode = info.getJSONObject("condition").getString("code");
					Double temperature = info.getJSONObject("condition").getDouble("temperature");
					int temp = formatWeather(Integer.valueOf(tempCode));
					weatherObject.put("code", temp);
					// 湿度（°F）转华摄氏度（°C）
					weatherObject.put("temperature", (int)((5/9.0)*(temperature-32)));
				}else {
					String getUrl = baiduUrl + "&location=" + lat +"," + lon;
					// 根据经纬度获取地址json
					JSONObject object = JSONObject.fromObject(HttpUtil.sendGet(getUrl));
					// 数据不为空，并请求成功
					if(!(object.isEmpty() || object.isNullObject()) && "ok".equalsIgnoreCase(object.getString("status"))) {
						// 取出所在地区的地级市
						JSONObject o = object.getJSONObject("result").getJSONObject("addressComponent");
						// 使用所在地地级市获取地级市经纬度
						getUrl = baiduUrl + "&address=" + o.getString("city");
						object = JSONObject.fromObject(HttpUtil.sendGet(getUrl));
						// 数据不为空并请求成功
						if(!(object.isEmpty() || object.isNullObject()) && "ok".equalsIgnoreCase(object.getString("status"))) {
							// 获取地级市经纬度
							o = object.getJSONObject("result").getJSONObject("location");
							// 取出地级市经纬度再次请求雅虎天气
							// 重新获取验证签
							lat = o.getDouble("lat");
							lon = o.getDouble("lng");
							authorizationLine = getAuthorizationLine(lat,lon);
							result =  request(url + "?lat="+ lat +"&lon="+ lon +"&format=json",authorizationLine,appId);
							obj = JSONObject.fromObject(result);
							info = obj.getJSONObject("current_observation");
							flag = info.isEmpty() || info.isNullObject();
							// 所在地地级市获取不到天气不再进行请求
							if(!flag) {
								// 获取到天气
								String tempCode = info.getJSONObject("condition").getString("code");
								Double temperature = info.getJSONObject("condition").getDouble("temperature");
								int temp = formatWeather(Integer.valueOf(tempCode));
								weatherObject.put("code", temp);
								// 湿度转华摄氏度
								weatherObject.put("temperature", (int)((5/9.0)*(temperature-32)));
							}
						}
					}
					
					
				}
				
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			code = 404;
		}
		webObject.put("code", code);
		webObject.put("data", weatherObject);
		webObject.put("errors", "[\"string\"]");
		webObject.put("msg", "ok");
		
	        // jdk 11 的请求方法
	       /* HttpClient client = HttpClient.newHttpClient();
	        HttpRequest request = HttpRequest.newBuilder()
	            .uri(URI.create(url + "?location=sunnyvale,ca&format=json"))
	            .header("Authorization", authorizationLine)
	            .header("X-Yahoo-App-Id", appId)
	            .header("Content-Type", "application/json")
	            .build();

	        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
	        System.out.println(response.body());*/
		return weatherObject.toString();
	}
	
	/**
	 * 生成验证签字符串
	 * @param lat 维度
	 * @param lon 经度
	 * @return 返回验证签
	 * @throws UnsupportedEncodingException
	 */
	public String getAuthorizationLine(Double lat, Double lon) throws UnsupportedEncodingException {
		
		// 执行 
		long timestamp = new Date().getTime() / 1000;
		byte[] nonce = new byte[32];
		Random rand = new Random();
		rand.nextBytes(nonce);
		String oauthNonce = new String(nonce).replaceAll("\\W", "");
		
		// 设置参数
		List<String> parameters = new ArrayList<>();
		parameters.add("oauth_consumer_key=" + consumerKey);
		parameters.add("oauth_nonce=" + oauthNonce);
		parameters.add("oauth_signature_method=HMAC-SHA1");
		parameters.add("oauth_timestamp=" + timestamp);
		parameters.add("oauth_version=1.0");
		// Make sure value is encoded
		parameters.add("lat="+lat);
		parameters.add("lon="+lon);
		parameters.add("format=json");
		Collections.sort(parameters);
		
		// 用&符号拼接参数
		StringBuffer parametersList = new StringBuffer();
		for (int i = 0; i < parameters.size(); i++) {
			parametersList.append(((i > 0) ? "&" : "") + parameters.get(i));
		}
		
		String signatureString = "GET&" +
				URLEncoder.encode(url, "UTF-8") + "&" +
				URLEncoder.encode(parametersList.toString(), "UTF-8");
		// 生成HmacSHA1
		String signature = null;
		try {
			SecretKeySpec signingKey = new SecretKeySpec((consumerSecret + "&").getBytes(), "HmacSHA1");
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(signingKey);
			byte[] rawHMAC = mac.doFinal(signatureString.getBytes());
			Encoder encoder = Base64.getEncoder();
			signature = encoder.encodeToString(rawHMAC);
		} catch (Exception e) {
			System.err.println("Unable to append signature");
			System.exit(0);
		}
		
		String authorizationLine = "OAuth " +
				"oauth_consumer_key=\"" + consumerKey + "\", " +
				"oauth_nonce=\"" + oauthNonce + "\", " +
				"oauth_timestamp=\"" + timestamp + "\", " +
				"oauth_signature_method=\"HMAC-SHA1\", " +
				"oauth_signature=\"" + signature + "\", " +
				"oauth_version=\"1.0\"";
		
		return authorizationLine;
	}
	
	/**
	 * 	请求API接口返回天气数据
	 * @param httpUrl API接口URL
	 * @param authorizationLine 参数
	 * @param appId appid
	 * @return
	 */
	public static String request(String httpUrl, String authorizationLine,String appId) {
		BufferedReader reader = null;
		String result = null;
		StringBuffer sbf = new StringBuffer();
		try {
			// 用java JDK自带的URL去请求
			URL url = new URL(httpUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			// 设置该请求的消息头
			// 设置HTTP方法：GET
			connection.setRequestMethod("GET");
			// 设置其Header的Content-Type参数为application/json
			connection.setRequestProperty("Content-Type", "application/json");
			// 设置地理位置等参数
			connection.setRequestProperty("Authorization", authorizationLine);
			// 设置appid参数
			connection.setRequestProperty("X-Yahoo-App-Id", appId);
			
			connection.setDoOutput(true);
			connection.connect();
			InputStream is = connection.getInputStream();
			reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String strRead = null;
			while ((strRead = reader.readLine()) != null) {
				sbf.append(strRead);
				sbf.append("\r\n");
			}
			reader.close();
			result = sbf.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;

	}
	// 根据编号转为其他天气
	public static int formatWeather(int code) {
		int weatherCode = 0;
		switch (code) {
		case 0:
		case 1:
		case 2:	
		case 23:	
			weatherCode = 6;// "风";
			break;
		case 26:
		case 27:
		case 28:
		case 29:
		case 30:
		case 31:
		case 33:
		case 34:
		case 44:
			weatherCode = 0; // "多云";
			break;
		case 32:	
			weatherCode = 1;// "晴天";
			break;
		case 13:	
		case 15:
		case 16:	
		case 17:
		case 18:	
		case 36:
		case 42:
		case 43:
			weatherCode = 2;// "雪天";
			break;
		case 5:	
		case 6:
		case 7:	
		case 8:
		case 9:	
		case 10:
		case 11:	
		case 12:
		case 14:	
		case 35:
		case 37:
		case 38:
		case 39:
		case 40:
		case 41:
		case 45:
		case 46:
		case 47:
			weatherCode = 3;// "雨天";
			break;
		case 3:	
		case 4:	
		case 24:	
		case 25:
			weatherCode = 4;// "阴天";
			break;
		case 19:	
			weatherCode = 5;// "沙尘";
			break;
		case 20:
		case 21:	
		case 22:
			weatherCode = 7;// "雾霾";
			break;
	}
		return weatherCode;
	}
	
	// 0.多云1.晴天2.雪天3.雨天4.阴天5.沙尘6.风7.雾霾
	/**
	 * 0龙卷风    1·
	 * 1热带风暴  1·
	 * 2飓风  1·
	 * 3严重雷暴6·
	 * 4雷暴6·
	 * 5混合雨雪  5·
	 * 6混合雨夹雪5·
	 * 7混合雪和雨夹雪5·
	 * 8冻结小雨5·
	 * 9冻结细雨5·
	 * 10冻结雨5·
	 * 11阵雨5·
	 * 12雨5·
	 * 13雪花4·
	 * 14小雪阵雨5·
	 * 15吹雪4·
	 * 16雪4·
	 * 17冰雹4·
	 * 18冰雹4·
	 * 19沙尘7·
	 * 20雾8·
	 * 21烟雾8·
	 * 22烟雾8·
	 * 23狂风1·
	 * 24寒冷6·
	 * 25寒冷6·
	 * 26大部分多云(夜间)2·
	 * 28大部分多云(白天)2·
	 * 29部分多云(夜间)2·
	 * 29部分多云(夜间)2·
	 * 30部分多云混合雨和冰雹5
	 * 36炎热3
	 * 37局部雷暴5
	 * 38零散雷雨5
	 * 39零散阵雨(白天)5
	 * 40大雨5
	 * 41零散雪阵雨(白天)5
	 * 42大雪4
	 * 43暴雪4
	 * 44不可用2
	 * 45零散阵雨(夜间)5
	 * 46零散雪阵雨(夜间)5
	 * 47零散雷阵雨5
	 */
}
