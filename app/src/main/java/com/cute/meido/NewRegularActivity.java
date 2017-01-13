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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.PopupWindow;
import android.widget.Spinner;
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
import java.util.ArrayList;

/*
修复条件为空是显示N/A
修复不启用地理位置检查时规则数据错误
时间安全检查整理为函数一直工具类

 */



public class NewRegularActivity  extends AppCompatActivity implements LocationSource,
        AMapLocationListener{

    private AMap aMap;
    private MapView mapView;
    private OnLocationChangedListener mListener;
    private AMapLocationClient locationClient;
    private AMapLocationClientOption mLocationOption;
    private Spinner spPremise;
    private Spinner spAction;
    private TextView startTime;
    private TextView endTime;
    private Switch setLocation;
    private TextView getDate;
    private Button cancel_btn;
    private Button add_btn;
    private String time;
    private String address = " ";
    private String premiseInfo = " ";
    private String actionInfo = " ";
    private String location = " ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_regular);
        setTitle("新建规则");
        initView();
        setAdapter();
        setListener();
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        init();
    }

    void initView(){
        spPremise = (Spinner) findViewById(R.id.premise);
        spAction = (Spinner) findViewById(R.id.action);
        startTime = (TextView) findViewById(R.id.startTimePicker);
        endTime = (TextView) findViewById(R.id.endTimePicker);
        cancel_btn = (Button) findViewById(R.id.cancel_button);
        add_btn = (Button) findViewById(R.id.add_button);
        getDate = (TextView) findViewById(R.id.getDate);
        setLocation = (Switch)findViewById(R.id.switch1);
    }

    void setAdapter(){
        Log.d("MAYU", "setAdapter: " + ToolBox.regularMap.size());
        String[] test = {};
        test = (ToolBox.regularMap.values()).toArray(test);
        ArrayAdapter premise_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, test);
        premise_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPremise.setAdapter(premise_adapter);

        ArrayAdapter action_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ToolBox.ACTION_SET);
        action_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAction.setAdapter(action_adapter);
    }

    void setListener(){


        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            time = "00:00";
            View  itemView = getLayoutInflater().inflate(R.layout.dialog_pick_time,null);
            AlertDialog.Builder dialog = new AlertDialog.Builder(NewRegularActivity.this);
            dialog.setView(itemView);
            TimePicker timePicker = (TimePicker) itemView.findViewById(R.id.timePicker);
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
                    startTime.setText(time);
                }
            });
            dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startTime.setText("00:00");
                }
            });
            dialog.show();
            }
        });

        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                time = "23:59";
                View  itemView = getLayoutInflater().inflate(R.layout.dialog_pick_time,null);
                AlertDialog.Builder dialog = new AlertDialog.Builder(NewRegularActivity.this);
                dialog.setView(itemView);
                TimePicker timePicker = (TimePicker) itemView.findViewById(R.id.timePicker);
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
                        endTime.setText(time);
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        endTime.setText("23:59");
                    }
                });
                dialog.show();
            }
        });

        spAction.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (ToolBox.ACTION_SET[i].equals("打开应用")){
                    Intent intent = new Intent(NewRegularActivity.this,PickAppActivity.class);
                    startActivityForResult(intent, ToolBox.PICK_START_APP);
                }else{
                    actionInfo = " ";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }

        });

        getDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                View contentView = LayoutInflater.from(NewRegularActivity.this).inflate(R.layout.pop_pick_date, null);

                final PopupWindow popupWindow = new PopupWindow(contentView,
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                popupWindow.setTouchable(true);
                popupWindow.setBackgroundDrawable(new ColorDrawable());
                Button fiveDay = (Button)contentView.findViewById(R.id.fiveDay);
                Button everyDay = (Button)contentView.findViewById(R.id.everyDay);
                Button aHa = (Button)contentView.findViewById(R.id.aHa);
                fiveDay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getDate.setText("星期一 星期二 星期三 星期四 星期五");
                        popupWindow.dismiss();
                    }
                });
                everyDay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getDate.setText("每天");
                        popupWindow.dismiss();
                    }
                });
                // aHa 确定按钮ID 但是使用其他变量名时运行出现问题 改为aha后没有问题 虽然知道问题所在 但是没有更改
                aHa.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        View  itemView = getLayoutInflater().inflate(R.layout.dialog_pick_date,null);
                        AlertDialog.Builder dialog = new AlertDialog.Builder(NewRegularActivity.this);
                        dialog.setView(itemView);
                        dialog.setTitle("请选择规则执行时间");
                        final CheckBox checkBox1 = (CheckBox)itemView.findViewById(R.id.checkBox1);
                        final CheckBox checkBox2 = (CheckBox)itemView.findViewById(R.id.checkBox2);
                        final CheckBox checkBox3 = (CheckBox)itemView.findViewById(R.id.checkBox3);
                        final CheckBox checkBox4 = (CheckBox)itemView.findViewById(R.id.checkBox4);
                        final CheckBox checkBox5 = (CheckBox)itemView.findViewById(R.id.checkBox5);
                        final CheckBox checkBox6 = (CheckBox)itemView.findViewById(R.id.checkBox6);
                        final CheckBox checkBox7 = (CheckBox)itemView.findViewById(R.id.checkBox7);
                        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ArrayList<String> dateList = new ArrayList<>();
                                if (checkBox1.isChecked()) {
                                    dateList.add("星期日");
                                }
                                if (checkBox2.isChecked()) {
                                    dateList.add("星期一");
                                }
                                if (checkBox3.isChecked()) {
                                    dateList.add("星期二");
                                }
                                if (checkBox4.isChecked()) {
                                    dateList.add("星期三");
                                }
                                if (checkBox5.isChecked()) {
                                    dateList.add("星期四");
                                }
                                if (checkBox6.isChecked()) {
                                    dateList.add("星期五");
                                }
                                if (checkBox7.isChecked()) {
                                    dateList.add("星期六");
                                }
                                if(dateList.size() == 7){
                                    getDate.setText("每天");
                                }else{
                                    String tempDate = "";
                                    for (String s : dateList) {
                                        tempDate = tempDate + " " + s;
                                    }
                                    getDate.setText(tempDate);
                                }
                            }
                        });
                        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                getDate.setText("每天");
                            }
                        });
                        dialog.show();
                        popupWindow.dismiss();
                    }
                });
                popupWindow.showAtLocation(view, Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
            }
        });

        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(!setLocation.isChecked()){
                    location = "未使用地理位置限制";
                    address = "未使用地理位置限制";
                }

                if(!ToolBox.checkTimeSe(startTime.getText().toString(),endTime.getText().toString())){
                    Toast.makeText(NewRegularActivity.this, "规则执行起止时间设置不合法", Toast.LENGTH_SHORT).show();
                    return ;
                }
                if(actionInfo.equals("Error:你没有选择要打开的应用")){
                    Toast.makeText(NewRegularActivity.this, "你没有选择要打开的应用", Toast.LENGTH_SHORT).show();
                    return;
                }

                String status = "1";
                String date = getDate.getText().toString();
                time = startTime.getText().toString() + " " + endTime.getText().toString();
                String premise = spPremise.getSelectedItem().toString();
                String action = spAction.getSelectedItem().toString();

                RegularDBHelper dbHelper = new RegularDBHelper(NewRegularActivity.this, "regular.db", null, 1);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("status", status);
                values.put("date", date);
                values.put("time", time);
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
                        status = cursor.getString(cursor.getColumnIndex("status"));
                        date = cursor.getString(cursor.getColumnIndex("date"));
                        time = cursor.getString(cursor.getColumnIndex("time"));
                        location = cursor.getString(cursor.getColumnIndex("location"));
                        address = cursor.getString(cursor.getColumnIndex("address"));
                        premise = cursor.getString(cursor.getColumnIndex("premise"));
                        premiseInfo = cursor.getString(cursor.getColumnIndex("premiseInfo"));
                        action = cursor.getString(cursor.getColumnIndex("action"));
                        actionInfo = cursor.getString(cursor.getColumnIndex("actionInfo"));
                        MainActivity.regList.add(new RegularInfo(id, status, date, time, location,address, premise,
                                premiseInfo, action, actionInfo));
                    } while (cursor.moveToNext());
                    cursor.close();
                }
                MainActivity.adapter.notifyDataSetChanged();
                NewRegularActivity.this.finish();
            }

        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewRegularActivity.this.finish();
            }
        });
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



