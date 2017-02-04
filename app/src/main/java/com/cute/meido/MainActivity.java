package com.cute.meido;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import com.bilibili.magicasakura.utils.ThemeUtils;
import com.cute.meido.adapter.RegularInfoAdapter;
import com.cute.meido.adapter.RegularInfo;
import com.cute.meido.dialog.ThemePickerDialog;
import com.cute.meido.utils.MCCObserver;
import com.cute.meido.utils.RegularDBHelper;
import com.cute.meido.utils.ThemeHelper;

import android.support.design.widget.Snackbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.CallLog;
import android.support.v7.app.AlertDialog;
import android.widget.AdapterView;
import android.widget.ListView;

import junit.framework.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ThemePickerDialog.ClickListener {

    public static RegularInfoAdapter adapter;
    public static List<RegularInfo> regList;
    public static HashMap<String,String> pkgMap;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        buildDatabase();
        buildListview();
        buildPkgMap();
        initObserver();
        initPreferences();
    }
    public void initPreferences(){
        SharedPreferences.Editor editor = getSharedPreferences("settings",MODE_PRIVATE).edit();
    }
    public void initObserver(){
        MCCObserver missedCallObserver = new MCCObserver(this.getApplicationContext(),null);
        getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, false,
                missedCallObserver);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ThemeUtils.getColorById(this, R.color.theme_color_primary_dark));
            ActivityManager.TaskDescription description = new ActivityManager.TaskDescription(null, null, ThemeUtils.getThemeAttrColor(this, android.R.attr.colorPrimary));
            setTaskDescription(description);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.change_theme) {
            ThemePickerDialog dialog = new ThemePickerDialog();
            dialog.setClickListener(this);
            dialog.show(getSupportFragmentManager(), ThemePickerDialog.TAG);
            return true;
        }
        if (item.getItemId() == R.id.add_regular){
            Intent intent = new Intent(MainActivity.this, NewRegularActivity.class);
            startActivity(intent);
            return true;
        }
        if (item.getItemId() == R.id.set_unMute){
            Intent intent = new Intent("com.cute.meido.unmote");
            MainActivity.this.sendBroadcast(intent);
            return true;
        }
        if (item.getItemId() == R.id.set_loc){
            startActivity(new Intent(MainActivity.this, TestActivity.class));
            return true;
        }
        if (item.getItemId() == R.id.about){
            startActivity(new Intent(MainActivity.this,AboutActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(MainActivity.this,MeidoService.class);
        startService(intent);
    }

    private void buildDatabase() {
        RegularDBHelper dbHelper = new RegularDBHelper(this, "regular.db", null, 1);
        dbHelper.getWritableDatabase();
    }

    private void buildListview() {
        RegularDBHelper dbHelper = new RegularDBHelper(this, "regular.db", null, 1);
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query("RegularInfo", null, null, null, null, null, null);
        regList = new ArrayList<>();
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
                MainActivity.regList.add(new RegularInfo(id, status, date, time, location, address,premise,
                        premiseInfo, action, actionInfo));
            } while (cursor.moveToNext());
            cursor.close();
        }
        adapter = new RegularInfoAdapter(MainActivity.this, R.layout.listview_regular, regList);
        ListView listView = (ListView) findViewById(R.id.main_listView);
        listView.setVerticalScrollBarEnabled(false);
        listView.setFastScrollEnabled(false);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Snackbar.make(view, "提示:长按可以删除规则", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final RegularInfo RegularInfo = regList.get(position);
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("确认删除");
                dialog.setMessage("你将删除你建立的规则,该过程不可逆,是否删除？");
                dialog.setCancelable(true);
                dialog.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String reg_id = RegularInfo.getId();
                        db.delete("RegularInfo","id = ?",new String[]{reg_id});
                        Cursor cursor = db.query("RegularInfo", null, null, null, null, null, null);
                        regList.clear();
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
                                MainActivity.regList.add(new RegularInfo(id, status,
                                        date, time, location, address,premise,
                                        premiseInfo, action, actionInfo));
                            } while (cursor.moveToNext());
                            cursor.close();
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                dialog.show();
                return false;
            }
        });
    }

    void buildPkgMap(){
        pkgMap = new HashMap<>();
        PackageManager pm = this.getPackageManager(); // 获得PackageManager对象
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfo = pm.queryIntentActivities(mainIntent, 0);
        Collections.sort(resolveInfo, new ResolveInfo.DisplayNameComparator(pm));
        for (ResolveInfo reInfo : resolveInfo) {
            String pkgName = reInfo.activityInfo.packageName; // 获得应用程序的包名
            String appLabel = (String) reInfo.loadLabel(pm); // 获得应用程序的Label
            pkgMap.put(pkgName,appLabel);
        }
    }

    @Override
    public void onConfirm(int currentTheme) {
        if (ThemeHelper.getTheme(MainActivity.this) != currentTheme) {
            ThemeHelper.setTheme(MainActivity.this, currentTheme);
            ThemeUtils.refreshUI(MainActivity.this, new ThemeUtils.ExtraRefreshable() {
                        @Override
                        public void refreshGlobal(Activity activity) {
                            //for global setting, just do once
                            if (Build.VERSION.SDK_INT >= 21) {
                                final MainActivity context = MainActivity.this;
                                ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription(null, null, ThemeUtils.getThemeAttrColor(context, android.R.attr.colorPrimary));
                                setTaskDescription(taskDescription);
                                getWindow().setStatusBarColor(ThemeUtils.getColorById(context, R.color.theme_color_primary_dark));
                            }
                        }

                        @Override
                        public void refreshSpecificView(View view) {
                            //TODO: will do this for each traversal
                        }
                    }
            );
        }
    }
}

