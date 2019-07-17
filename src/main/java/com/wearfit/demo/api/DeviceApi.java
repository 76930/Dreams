package com.wearfit.demo.api;

import java.util.List;

import com.wearfit.demo.conf.WxConfig;
import com.wearfit.demo.json.AccessToken;
import com.wearfit.demo.json.DeviceAuth;
import com.wearfit.demo.utils.HttpUtil;

import net.sf.json.JSONObject;


/**
 * 设备相关 API
 * <p>
 * https://api.weixin.qq.com/device/ 下的API为设备相关API， 测试号可以调用，正式服务号需要申请权限后才能调用。
 */
public class DeviceApi {

	// 获取access_token 
	private static final String GetAccessTokenUrl = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
			+ WxConfig.APPID + "&secret=" + WxConfig.APPSECRET;
	// 向设备推送消息
	private static final String TransMsgUrl = "https://api.weixin.qq.com/device/transmsg?access_token=ACCESS_TOKEN";
	// 批量授权/更新设备属性
	private static final String AuthorizeUrl = "https://api.weixin.qq.com/device/authorize_device?access_token=ACCESS_TOKEN";
	// 批量授权/更新设备属性
	private static final String CreateQrcode = "https://api.weixin.qq.com/device/authorize_device?access_token=ACCESS_TOKEN";
	// 获取设备授权二维码
	private static final String GetQrcode = "https://api.weixin.qq.com/device/getqrcode?access_token=ACCESS_TOKEN";
	// 设备状态查询
	private static final String GetStatUrl = "https://api.weixin.qq.com/device/get_stat?access_token=ACCESS_TOKEN&device_id=DEVICE_ID";
	// 验证二维码 获取二维码对应设备属性
	private static final String VerifyQrcodeUrl = "https://api.weixin.qq.com/device/verify_qrcode?access_token=ACCESS_TOKEN";
	// 根据设备类型和设备id查询绑定的openid
	private static final String GetOpenidUrl = "https://api.weixin.qq.com/device/get_openid?access_token=ACCESS_TOKEN&device_type=DEVICE_TYPE&device_id=DEVICE_ID";

	
	
	/**
	 * 获取访问凭证
	 * <p>
	 * 正常情况下access_token有效期为7200秒，重复获取将导致上次获取的access_token失效。
	 * 由于获取access_token的api调用次数非常有限，需要全局存储与更新access_token
	 * <br/>
	 * 文档位置：基础支持->获取access token
	 */
	public static AccessToken getAccessToken() {
		String resultContent = HttpUtil.executeGet(GetAccessTokenUrl);
		return AccessToken.fromJson(resultContent);
	}
	
	/**
	 * 向设备推送消息
	 */
	public static String transMsg(String deviceType, String deviceID,
			String openID, String content) {
		JSONObject json = new JSONObject();
		json.put("device_type", deviceType);
		json.put("device_id", deviceID);
		json.put("open_id", openID);
		json.put("content", content);
		String body = json.toString();
		System.out.println("transMsg body=" + body);
		String ret = HttpUtil.doPost(TransMsgUrl, body);
		System.out.println("transMsg ret=" + ret);
		return ret;
	}

	/**
	 * 获取设备授权二维码
	 * @author jie
	 */
	public static String GetQrcode(Integer product_id) {
		return HttpUtil.doGet(GetQrcode + (product_id == null ? "" : "&product_id=" + product_id ));
	}

	/**
	 * 批量授权/更新设备属性
	 * <p>
	 * 授权后设备才能进行绑定操作
	 * 
	 * @param devices
	 *            设备属性列表
	 * @param isCreate
	 *            是否首次授权： true 首次授权； false 更新设备属性
	 */
	public static String authorize(List<DeviceAuth> devices, String isCreate) {
		JSONObject json = new JSONObject();
		json.put("device_num", String.valueOf(devices.size()));
													// 1：设备更新（更新已授权设备的各属性值）
		json.put("device_list", devices);
		json.put("op_type", isCreate);// 请求操作的类型 0：设备授权（缺省值为0）
		//json.put("product_id", WxConfig.PRODUCTID);
		return HttpUtil.doPost(AuthorizeUrl, json.toString());
	}
	
	/**
	 * 根据设备id获取二维码生成串
	 * @param deviceIds
	 * @return
	 */
	public static String createQrcode(List<String> deviceIds) {
		JSONObject json = new JSONObject();
		json.put("device_num", deviceIds.size());
		json.put("device_id_list", deviceIds);
		return HttpUtil.doPost(CreateQrcode, json.toString());
	}

	/**
	 * 设备状态查询
	 * <p>
	 * status 0：未授权 1：已经授权（尚未被用户绑定） 2：已经被用户绑定<br/>
	 * {"errcode":0,"errmsg":"ok","status":1,"status_info":"authorized"}
	 */
	public static String getStat(String deviceId) {
		String url = GetStatUrl.replace("DEVICE_ID", deviceId);
		return HttpUtil.doGet(url);
	}

	/**
	 * 验证二维码 获取二维码对应设备属性
	 * 
	 * @param ticket
	 *            二维码生成串
	 */
	public static String verifyQrcode(String ticket) {
		JSONObject json = new JSONObject();
		json.put("ticket", ticket);
		return HttpUtil.doPost(VerifyQrcodeUrl, json.toString());
	}

	/**
	 * 根据设备类型和设备id查询绑定的openid
	 */
	public static String getOpenId(String deviceType, String deviceId) {
		String url = GetOpenidUrl.replace("DEVICE_TYPE", deviceType).replace(
				"DEVICE_ID", deviceId);
		return HttpUtil.doGet(url);
	}

}
