/**
 * 
 */
package com.wearfit.demo.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.wearfit.demo.api.DeviceApi;
import com.wearfit.demo.conf.WxConfig;
import com.wearfit.demo.json.DeviceAuth;

import net.sf.json.JSONObject;


/**
 * 设备授权、获取二维码接口，供安卓、iOS调用
 * @author jie
 * @date 2019年7月11日
 */
@RestController
@RequestMapping("/qrcode")
public class BraceletQrCodeController {

	// product_id 产品ID
	
	
	@RequestMapping(value = "/getQrcode", method = RequestMethod.GET)
	@ResponseBody
	public String getQrcode(String mac) {
		JSONObject parames = new JSONObject();
		int success = 200;
		parames.put("deviceid", "");
		parames.put("qrticket", "");
		try {
			// 1. 获取access_token
			// JSONObject access
			// 2. 获取deviceid和二维码
			JSONObject obj = JSONObject.fromObject(DeviceApi.GetQrcode(WxConfig.PRODUCTID));
			
			// 3. 利用deviceid和mac地址更新设备属性
				
			List<DeviceAuth> list = new ArrayList<DeviceAuth>();
			
			DeviceAuth deviceAuth = new DeviceAuth();
			deviceAuth.setId(obj.getString("deviceid")); // 设备授权deviceid
			deviceAuth.setMac(mac.replace(":", "")); // 手环mac地址
			deviceAuth.setConnect_protocol("3");
			deviceAuth.setAuth_key("");
			deviceAuth.setClose_strategy("2");
			deviceAuth.setConn_strategy("1");
			deviceAuth.setCrypt_method("0");
			deviceAuth.setAuth_ver("0");
			deviceAuth.setManu_mac_pos("-1");
			deviceAuth.setSer_mac_pos("-2");
			deviceAuth.setBle_simple_protocol("1");
			list.add(deviceAuth);
			
			// 更新设备属性
			String str = DeviceApi.authorize(list, "1");
			
			// 4. 返回二维码和deviceid
			
			if(success != 0) {
				// 获取二维码成功
				parames.put("deviceid", obj.getString("deviceid"));
				parames.put("qrticket", obj.getString("qrticket"));
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			success = 0;
		}
		parames.put("errcode", success);
		return parames.toString();
	}
	
	@RequestMapping(value = "/getstat", method = RequestMethod.GET)
	@ResponseBody
	public String getstat(String deviceId ) {
		try {
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		return DeviceApi.getStat(deviceId);
	}
	
}
