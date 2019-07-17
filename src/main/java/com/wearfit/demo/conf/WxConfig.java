package com.wearfit.demo.conf;

import java.io.InputStream;
import java.util.Properties;


/**
 * 微信公众账号开发者配置
 */
public abstract class WxConfig {
	
	public static final String TOKEN;
	public static final Integer PRODUCTID;
	public static final String APPID;
	public static final String APPSECRET;

	static {
		try {
			InputStream in = WxConfig.class.getClassLoader()
					.getResourceAsStream("application.properties");
			Properties props = new Properties();
			props.load(in);
			PRODUCTID = Integer.valueOf(props.getProperty("productID", ""));
			APPID = props.getProperty("appID", "");
			APPSECRET = props.getProperty("appSecret", "");
			TOKEN = props.getProperty("token", "");
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("load config error " + e.getMessage());
		}
	}
	
	
}
