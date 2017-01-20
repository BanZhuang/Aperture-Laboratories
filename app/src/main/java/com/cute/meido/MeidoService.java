package com.cute.meido;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMapUtils;
import com.amap.api.maps2d.model.LatLng;
import com.cute.meido.utils.RegularDBHelper;
import com.cute.meido.utils.ToolBox;

/*
移除对双卡的支持

 */

public class MeidoService extends Service {

    public MyReceiver myReceiver;
    private AMapLocationClient locationClient;
    private AMapLocationClientOption locationOption = new AMapLocationClientOption();
    private static LatLng latlngNow;
    private static String telNumber = "10010";
    private String TAG = "MAYU";
    private static int LOC_SU = 0;


    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction("com.cute.meido.MISSED_CALL");
        intentFilter.addAction("com.cute.meido.unmote");
        myReceiver = new MyReceiver();
        registerReceiver(myReceiver, intentFilter);
        locationClient = new AMapLocationClient(this.getApplicationContext());
        locationClient.setLocationOption(getDefaultOption());
        locationClient.setLocationListener(locationListener);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            /*
            预期接收抵达位置离开位置打开蓝牙
             插入耳机未接来电充电连接
             */
            /* 使用action会引发歧义 更改了以前的变量名
              接受的广播形式（也就是触发条件）是 "android.intent.action.xxx" 好吧这是我胡写的，
              因为存储和显示（和搜索）的形式是 "打开蓝牙"
              用之前写了一个键值字典在这里进行转化
              部分监听代码删除去以前版本可以查找
             */
            switch (intent.getAction()) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    if (state == BluetoothAdapter.STATE_ON){
                        searchAction(ToolBox.regularMap.get(BluetoothAdapter.ACTION_STATE_CHANGED));
                    }
                    break;
                case Intent.ACTION_HEADSET_PLUG:
                    if ((1 == intent.getIntExtra("state", 0))) {
                        searchAction(ToolBox.regularMap.get(Intent.ACTION_HEADSET_PLUG));
                        abortBroadcast();
                    }
                    break;
                case Intent.ACTION_POWER_CONNECTED:
                    searchAction(ToolBox.regularMap.get(Intent.ACTION_POWER_CONNECTED));
                    break;
                case "com.cute.meido.MISSED_CALL":
                    telNumber = intent.getStringExtra("number");
                    searchAction(ToolBox.regularMap.get("com.cute.meido.MISSED_CALL"));
                    break;
                case "com.cute.meido.unmote":
                    doActionUnMote();
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myReceiver);
    }

    public void searchAction(String premise) {
        Log.d(TAG, "searchAction: 测试用 接受的转换后的函数值" + premise);
        String status = "1";
        String date;
        String time;
        String location;
        String premiseInfo;
        String action;
        String actionInfo;
        RegularDBHelper dbHelper = new RegularDBHelper(MeidoService.this, "regular.db", null, 1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from RegularInfo where premise = ? and status = ?", new String[]{premise, status});
        if (cursor.moveToFirst()) {
            do {
                date = cursor.getString(cursor.getColumnIndex("date"));
                time = cursor.getString(cursor.getColumnIndex("time"));
                location = cursor.getString(cursor.getColumnIndex("location"));
                premiseInfo = cursor.getString(cursor.getColumnIndex("premiseInfo"));
                action = cursor.getString(cursor.getColumnIndex("action"));
                actionInfo = cursor.getString(cursor.getColumnIndex("actionInfo"));
                if (ToolBox.checkDate(date) && ToolBox.checkTime(time) &&
                        checkLocation(location) && ToolBox.checkPremiseInfo(premiseInfo)) {
                    doAction(action, actionInfo);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
    }

    public void doAction(String action,String actionInfo){
         /*
        "打开Wi-Fi","关闭Wi-Fi","打开蓝牙","关闭蓝牙","打开应用","回复短信","静音模式",
         */
        WifiManager wifiManager;
        BluetoothAdapter mBluetoothAdapter;
        int getBluetoothStatus;
        Context context = getBaseContext();
        AudioManager audioManager;
        switch (action) {
            case "关闭Wi-Fi":
                wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                if (wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(false);
                }
                break;

            case "打开Wi-Fi":
                wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                if (!wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(true);
                }
                break;
            case "关闭蓝牙":
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                getBluetoothStatus = mBluetoothAdapter.getState();
                switch (getBluetoothStatus) {
                    case BluetoothAdapter.STATE_ON:
                        mBluetoothAdapter.disable();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        mBluetoothAdapter.disable();
                        break;
                }
                break;

            case "打开蓝牙":
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                getBluetoothStatus = mBluetoothAdapter.getState();
                switch (getBluetoothStatus) {
                    case BluetoothAdapter.STATE_OFF:
                        mBluetoothAdapter.enable();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        mBluetoothAdapter.enable();
                        break;
                }
                break;

            case "打开应用":
                PackageManager pm = context.getPackageManager();
                String pkg = actionInfo;
                Intent intent = pm.getLaunchIntentForPackage(pkg);
                startActivity(intent);
                break;
            case "静音模式":
                audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);


                final int version = Build.VERSION.SDK_INT;
                if (version >= 23) {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE,AudioManager.FLAG_SHOW_UI);
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE,AudioManager.FLAG_SHOW_UI);
                    audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE,AudioManager.FLAG_SHOW_UI);
                } else {
                    audioManager.setStreamMute(AudioManager.STREAM_RING, true);
                    audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
                    audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
                }
                break;
            case "回复短信":
                SmsManager smsManager =SmsManager.getDefault();
                smsManager.sendTextMessage(telNumber,null,"[自动回复]您呼叫的用户暂时无法处理你的来电。---本短信由Android智能助手Meido生成",null,null);
                telNumber = "10010";
                break;
        }
    }

     boolean checkLocation(String location){
        /*
         * 检查当前位置是否在在规则的范围内
         * 定位精度不太准确
          */
         if (location.equals("未使用地理位置限制")){
             return true;
         }
         startLocation();
         String locData[] = location.split(" ");
         LatLng latlngReg = new LatLng(Double.valueOf(locData[0]), Double.valueOf(locData[1]));
         Log.d(TAG, "checkLocation: " +latlngReg + latlngNow);
         double distance = AMapUtils.calculateLineDistance( latlngNow, latlngReg);
         Log.d(TAG, "地理距离判断模块 传入的地址: " + location + "地理距离: " + distance);
         if(LOC_SU != 0){
             stopLocation();
         }
         if (LOC_SU == 0){
             Log.e(TAG, "checkLocation: [金丝雀] 之前定位失败!");
             return false;
         }
         return (distance < 200);
    }

    private void stopLocation(){
        locationClient.stopLocation();
    }

    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation loc) {
            if (null != loc) {
                Log.e(TAG, "meido 服务 定位回调结果" + loc.getLatitude() + " " + loc.getLongitude());
                Log.e(TAG, "onLocationChanged: " + loc.getAddress() );
                if (loc.getLatitude() != 0){
                    LOC_SU = 1;
                }
            } else {
                Log.e(TAG, "定位失败");
                LOC_SU = 0;
            }
        }
    };

    private void startLocation(){
        locationClient.setLocationOption(locationOption);
        locationClient.startLocation();
    }
    private AMapLocationClientOption getDefaultOption(){
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(500);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(true);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        return mOption;
    }
    private void doActionUnMote(){
        AudioManager audioManager;
        Context context = getBaseContext();
        audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_UNMUTE,AudioManager.FLAG_SHOW_UI);
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE,AudioManager.FLAG_SHOW_UI);
            audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_UNMUTE,AudioManager.FLAG_SHOW_UI);
            audioManager.setStreamVolume(AudioManager.STREAM_RING, 2,AudioManager.FLAG_SHOW_UI);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 2,AudioManager.FLAG_SHOW_UI);
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 2,AudioManager.FLAG_SHOW_UI);
        } else {
            audioManager.setStreamMute(AudioManager.STREAM_RING, false);
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
            audioManager.setStreamVolume(AudioManager.STREAM_RING, 2,AudioManager.FLAG_SHOW_UI);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 2,AudioManager.FLAG_SHOW_UI);
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 2,AudioManager.FLAG_SHOW_UI);

        }
    }

}

