package com.healthmgr.healthmgrv1.utils;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.services.core.AMapException;
import com.healthmgr.healthmgrv1.R;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Utils {
	static long exitTime = 0;

	public static void ExitWithToat(Context context) {
		if ((System.currentTimeMillis() - exitTime) > 2000) {
			Toast.makeText(context, context.getResources().getString(R.string.exit_tip), Toast.LENGTH_SHORT).show();
			exitTime = System.currentTimeMillis();
		} else {
			//	MainActivity.mBluetoothUtils.StopBluetoothChatService();
			((Activity) context).finish();
		}
	}

	/**
	 * 发送短信,注意,不保证信息是否被自动拆分为多条短信(部分情况下,短信内容长度限制在70字符以内)
	 * @param context
	 * @param phoneNum
	 * @param info
	 */
	public static void sendSms(Context context, String phoneNum, String info) {
		try {
//		PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(context, context.getClass()), 0);
			PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent(context, context.getClass()), 0);

			context.registerReceiver(new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {

				}
			}, new IntentFilter());
			SmsManager sms = SmsManager.getDefault();
			//sms.sendTextMessage(phoneNum,"008613800270500",info,pi,null);
			//String myPhoneNum = getMyPhoneNum(context);
			sms.sendTextMessage(phoneNum, null ,info,pi,null);

			Toast.makeText(context, "短信发送完成", Toast.LENGTH_SHORT).show();
		} catch (Exception ex) {
			Log.e("", "", ex);
		}
	}

	private static String getMyPhoneNum(Context currentActivity) {
		TelephonyManager tm = (TelephonyManager) currentActivity.getSystemService(Context.TELEPHONY_SERVICE);
//		String deviceid = tm.getDeviceId();//获取智能设备唯一编号
		String te01 = tm.getLine1Number();//获取本机号码
//		String te02 = "";
//		String imei = tm.getSimSerialNumber();//获得SIM卡的序号
//		String imsi = tm.getSubscriberId();//得到用户Id
		return te01;
	}


	/**
	 * 自动打开拨号面板,自动拨号并自动拨出电话
	 * @param context
	 * @param phoneNum
	 */
	public static void showCallPanel(Context context, String phoneNum) {
		if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			Toast.makeText(context, "无自动拨号权限,无法拨号", Toast.LENGTH_LONG).show();
			return;
		}
		context.startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNum)));
	}

	/**
	 * 判断GPS是否开启，若未开启，则进入GPS设置页面；设置完成后仍回原界面
	 * @param  currentActivity
	 * @return
	 */
	public static void openGPSSettings(Activity currentActivity){
		//获取位置服务
		LocationManager lm = (LocationManager) currentActivity.getSystemService(Context.LOCATION_SERVICE);
		//若GPS未开启
		if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			Toast.makeText(currentActivity, "请开启GPS！", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			currentActivity.startActivityForResult(intent,0); //此为设置完成后返回到获取界面
		}
	}

	//    16进制byte转换为String
	public static String hexByte2String(byte b) {
		return Integer.toHexString(b & 0xFF).toUpperCase();
	}

	//    16进制byte数组转换为String
	public static String hexByte2String(byte[] bytes, String blank) {
		StringBuilder builder = new StringBuilder();
		for (byte b : bytes) {
			String s = hexByte2String(b);
			builder.append(s).append(blank);
		}
		return new String(builder);
	}

	public static String sHA1(Context context) {
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(
					context.getPackageName(), PackageManager.GET_SIGNATURES);
			byte[] cert = info.signatures[0].toByteArray();
			MessageDigest md = MessageDigest.getInstance("SHA1");
			byte[] publicKey = md.digest(cert);
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < publicKey.length; i++) {
				String appendString = Integer.toHexString(0xFF & publicKey[i])
						.toUpperCase(Locale.US);
				if (appendString.length() == 1)
					hexString.append("0");
				hexString.append(appendString);
				hexString.append(":");
			}
			String result = hexString.toString();
			return result.substring(0, result.length()-1);
		} catch (Exception e) {
			Log.e("", "get signature sha1 info, ex info : ", e);
		}
		return null;
	}

}