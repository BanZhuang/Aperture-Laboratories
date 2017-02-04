
package com.cute.meido.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;


import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.util.Log;

public class ToolBox {

	/*
	抵达位置 离开位置 打开蓝牙 插入耳机 未接来电 充电连接
        */

	public static String DEBUG_TAG = "Meido_Debug";
	public final static int PICK_START_APP = 1;
	public final static int PICK_SEND_SMS = 2;
	public final static int PICK_START_TIME_MSG_CODE = 3;
	public final static int PICK_END_TIME_MSG_CODE = 4;
	public final static String PICK_TIME = "PICK_TIME";
	public static Map<String,String> regularMap = new HashMap<>();


	static {
        regularMap.put(BluetoothAdapter.ACTION_STATE_CHANGED,"打开蓝牙");
        regularMap.put("android.intent.action.HEADSET_PLUG","插入耳机");
        regularMap.put("com.cute.meido.MISSED_CALL","未接来电");
        regularMap.put(Intent.ACTION_POWER_CONNECTED,"充电连接");

	}

	public final static String[] ACTION_SET =
			{
			"打开Wi-Fi",
			"关闭Wi-Fi",
			"打开蓝牙",
			"关闭蓝牙",
			"打开应用",
			"回复短信",
			"静音模式",
			};

	public static boolean checkDate(String date){
		//检查当前日期是否在在规则的范围内
		//数据处理开始
		if(date.equals("每天")){
			return true;
		}
		List<String> listDate = new ArrayList<>();
		String[] tempDate;
		tempDate = date.split(" ");
		for (String s : tempDate) {
			listDate.add(s);
		}
		//数据处理结束
		final Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		String mWay = String.valueOf(c.get(Calendar.DAY_OF_WEEK));
		if("1".equals(mWay)){
			mWay ="星期日";
		}else if("2".equals(mWay)){
			mWay ="星期一";
		}else if("3".equals(mWay)){
			mWay ="星期二";
		}else if("4".equals(mWay)){
			mWay ="星期三";
		}else if("5".equals(mWay)){
			mWay ="星期四";
		}else if("6".equals(mWay)){
			mWay ="星期五";
		}else if("7".equals(mWay)){
			mWay ="星期六";
		}
		if (date.contains(mWay))
			return true;
		else
			return false;
	}
	public static boolean checkTime(String time){
		//检查当前时间是否在在规则的范围内
		//数据处理开始
		String[] tempTime;
		tempTime = time.split(" ");
		String startTime = tempTime[0];
		String endTime = tempTime[1];
		//数据处理结束
		Date date = new Date();
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String currentTime = format.format(date);
		String[] timeSet = currentTime.split(" ");
		String headDate = timeSet[0];
		startTime = headDate + " " + startTime;
		endTime = headDate + " " + endTime;
		try {
			Date sTime = format.parse(startTime);
			Date eTime = format.parse(endTime);
			Date cTime = format.parse(currentTime);
			if(cTime.getTime() > sTime.getTime() && cTime.getTime() < eTime.getTime()){
				return true;
			}
			else
				return false;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean checkPremiseInfo(String premise){
		Log.d(ToolBox.DEBUG_TAG, "checkPremiseInfo: " + true);
		return true;
	}

	public static boolean checkTimeSe(String startTime,String endTime){
		Date nowDate = new Date();
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String nowDateStr = format.format(nowDate);
		String[] getDateFromStr = nowDateStr.split(" ");
		String date4cmp = getDateFromStr[0];
		String startTime4cmp = date4cmp + " " + startTime;
		String endTime4cmp = date4cmp + " " + endTime;
		try {
			Date sTime = format.parse(startTime4cmp);
			Date eTime = format.parse(endTime4cmp);
			if (sTime.getTime() > eTime.getTime()){
				return false;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return true;
	}
}
