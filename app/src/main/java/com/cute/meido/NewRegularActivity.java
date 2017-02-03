package com.cute.meido;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.cute.meido.adapter.RegularInfo;
import com.cute.meido.utils.RegularDBHelper;
import com.cute.meido.utils.ToolBox;

import java.util.ArrayList;

/*
修复条件为空是显示N/A
修复不启用地理位置检查时规则数据错误
时间安全检查整理为函数一直工具类

 */



public class NewRegularActivity  extends AppCompatActivity implements LocationSource,
        AMapLocationListener,View.OnClickListener{

    private AMap aMap;
    private MapView mapView;
    private OnLocationChangedListener mListener;
    private AMapLocationClient locationClient;
    private AMapLocationClientOption mLocationOption;



    private String time = "00:00";

    // 没有文本交互控件
    private String premise = "打开蓝牙";
    private String premiseInfo = " ";
    private String action = "关闭蓝牙";
    private String actionInfo = " ";

    private String address = " ";
    private String location = " ";

    private TextView date;
    private TextView sTime;
    private TextView eTime;
    private Switch locSwitch;
    private TextView uipremise;
    private TextView uiaction;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_regular);
        setTitle("新建规则");
        initView();
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        init();
    }
    private void initView(){
        //需要和文本交互的控件
        date = (TextView)findViewById(R.id.nrdate);
        sTime = (TextView)findViewById(R.id.nrstartTime);
        eTime = (TextView)findViewById(R.id.nrendTime);
        locSwitch = (Switch)findViewById(R.id.locSwitch);
        uipremise = (TextView)findViewById(R.id.uipremise);
        uiaction = (TextView)findViewById(R.id.uiaction);


        //
        View viewList[]  = {
                // 文本交互
                findViewById(R.id.nrdate),
                findViewById(R.id.nrstartTime),
                findViewById(R.id.nrendTime),
                // 行为交互
                //findViewById(R.id.locSwitch),
                // 不需要文本交互1
                findViewById(R.id.cancel_button),
                findViewById(R.id.add_button),
                // na 系

                //new
                findViewById(R.id.uipremise),
                findViewById(R.id.uiaction)
        };
        for (View v : viewList){
            v.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        View  itemView;
        AlertDialog.Builder dialog;
        TimePicker timePicker;
        switch (v.getId()){

            case R.id.uipremise:
                final AlertDialog.Builder builder = new AlertDialog.Builder(NewRegularActivity.this);
                builder.setTitle("触发条件");
                final String[] dateSource = new String[] {"打开蓝牙","插入耳机","未接来电","充电连接"};
                builder.setSingleChoiceItems(dateSource, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       premise= dateSource[which];
                    }
                });
                builder.setPositiveButton("确定",null);
                builder.show();
                break;

            case R.id.uiaction:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(NewRegularActivity.this);
                builder2.setTitle("触发条件");
                final String[] dateSource2 = new String[] {"关闭蓝牙","打开蓝牙","打开Wi-Fi","关闭Wi-Fi","回复短信","静音模式","打开应用"};
                builder2.setSingleChoiceItems(dateSource2, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        action = dateSource2[which];
                        if(action.equals("打开应用")){
                              Intent intent = new Intent(NewRegularActivity.this,PickAppActivity.class);
                                startActivityForResult(intent, ToolBox.PICK_START_APP);
                        }
                    }
                });
                builder2.setPositiveButton("确定",null);
                builder2.show();
                break;

            case R.id.nrstartTime:
                 time = "00:00";
                itemView = getLayoutInflater().inflate(R.layout.dialog_pick_time,null);
                dialog = new AlertDialog.Builder(NewRegularActivity.this);
                dialog.setView(itemView);
                timePicker = (TimePicker) itemView.findViewById(R.id.timePicker);
                timePicker.setIs24HourView(true);
                timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                    @Override
                    public void onTimeChanged(TimePicker timePicker, int i, int i1) {
                        time = i + ":" + i1;
                    }
                });
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sTime.setText(time);
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sTime.setText("00:00");
                    }
                });
                dialog.show();
                break;
            case  R.id.nrendTime:
                time = "23:59";
                itemView = getLayoutInflater().inflate(R.layout.dialog_pick_time,null);
                dialog = new AlertDialog.Builder(NewRegularActivity.this);
                dialog.setView(itemView);
                timePicker = (TimePicker) itemView.findViewById(R.id.timePicker);
                timePicker.setIs24HourView(true);
                timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                    @Override
                    public void onTimeChanged(TimePicker timePicker, int i, int i1) {
                        time = i + ":" + i1;
                    }
                });
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        eTime.setText(time);
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        eTime.setText("23:59");
                    }
                });
                dialog.show();
                break;
            case R.id.nrdate:
                final ArrayList<String> dateList = new ArrayList<>();
                final AlertDialog.Builder builder3 = new AlertDialog.Builder(NewRegularActivity.this);
                builder3.setTitle("选择执行日期");
                final String[] dateSource3 = new String[] {"星期一","星期二","星期三","星期四","星期五","星期六","星期日"};
                builder3.setMultiChoiceItems(dateSource3, new boolean[]{false,false,false,false,false,false,false}, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if(isChecked){
                            if(dateList.indexOf(dateSource3[which]) == -1)
                                dateList.add(dateSource3[which]);
                        }else{
                            if(dateList.indexOf(dateSource3[which]) != -1)
                                dateList.remove(dateSource3[which]);
                        }
                    }
                });
                builder3.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(dateList.size() == 7){
                            date.setText("每天");
                        }else{
                            String tempDate = "";
                            for (String s : dateList) {
                                tempDate = tempDate + " " + s;
                            }
                            date.setText(tempDate);
                        }
                    }
                });
                builder3.show();
                break;
            case R.id.add_button:
                if(!locSwitch.isChecked()){
                    location = "未使用地理位置限制";
                    address = "未使用地理位置限制";
                }

                if(!ToolBox.checkTimeSe(sTime.getText().toString(),eTime.getText().toString())){
                    Toast.makeText(NewRegularActivity.this, "规则执行起止时间设置不合法", Toast.LENGTH_SHORT).show();
                    return ;
                }
                if(actionInfo.equals("Error:你没有选择要打开的应用")){
                    Toast.makeText(NewRegularActivity.this, "你没有选择要打开的应用", Toast.LENGTH_SHORT).show();
                    return;
                }

                RegularDBHelper dbHelper = new RegularDBHelper(NewRegularActivity.this, "regular.db", null, 1);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("status", 1);
                values.put("date", date.getText().toString());
                values.put("time", sTime.getText().toString() + " " + eTime.getText().toString());
                values.put("location", location);
                values.put("address",address);
                values.put("premise", premise);
                values.put("premiseInfo", premiseInfo);
                values.put("action", action);
                values.put("actionInfo", actionInfo);
                db.insert("RegularInfo", null, values);
                values.clear();
                Cursor cursor = db.query("RegularInfo", null, null, null, null, null, null);
                MainActivity.regList.clear();
                if (cursor.moveToFirst()) {
                    do {
                        String id = cursor.getString(cursor.getColumnIndex("id"));
                        String status = cursor.getString(cursor.getColumnIndex("status"));
                        String date = cursor.getString(cursor.getColumnIndex("date"));
                        String time = cursor.getString(cursor.getColumnIndex("time"));
                        String location = cursor.getString(cursor.getColumnIndex("location"));
                        String address = cursor.getString(cursor.getColumnIndex("address"));
                        String premise = cursor.getString(cursor.getColumnIndex("premise"));
                        String premiseInfo = cursor.getString(cursor.getColumnIndex("premiseInfo"));
                        String action = cursor.getString(cursor.getColumnIndex("action"));
                        String actionInfo = cursor.getString(cursor.getColumnIndex("actionInfo"));
                        MainActivity.regList.add(new RegularInfo(id, status, date, time, location,address, premise,
                                premiseInfo, action, actionInfo));
                    } while (cursor.moveToNext());
                    cursor.close();
                }
                MainActivity.adapter.notifyDataSetChanged();
                NewRegularActivity.this.finish();
                break;
            case R.id.cancel_button:
                NewRegularActivity.this.finish();
                break;




        }
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case ToolBox.PICK_START_APP:
                actionInfo = data.getStringExtra("Pkg");
                break;
        }
    }
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap();
        }
    }
    private void setUpMap() {
        // 自定义系统定位小蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory
                .fromResource(R.drawable.location_marker));// 设置小蓝点的图标
        myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));// 设置圆形的填充颜色
        // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
        myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // aMap.setMyLocationType()
    }
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        deactivate();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
                aMap.animateCamera(CameraUpdateFactory.zoomTo(16));//动画移动缩放
                // 获取位置信息
                address = amapLocation.getAddress();
                location = amapLocation.getLatitude() + " " + amapLocation.getLongitude();
            } else {
                String errText = "定位失败," + amapLocation.getErrorCode()+ ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr",errText);
            }
        }
    }
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (locationClient == null) {
            locationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            locationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            locationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            locationClient.startLocation();
        }
    }
    @Override
    public void deactivate() {
        mListener = null;
        if (locationClient != null) {
            locationClient.stopLocation();
            locationClient.onDestroy();
        }
        locationClient = null;
    }
}




