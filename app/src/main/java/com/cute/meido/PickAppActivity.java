package com.cute.meido;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PickAppActivity extends AppCompatActivity {

    private ListView listView;
    private List<AppInfo> appList = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_app);
        setTitle("选择启动的应用");
        listView = (ListView) findViewById(R.id.showapp);
        appList = new ArrayList<AppInfo>();
        queryAppInfo(); // 查询所有应用程序信息
        AppInfoAdapter adapter = new AppInfoAdapter(this, R.layout.listview_appinfo,appList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AppInfo appInfo = appList.get(position);
                Intent intent = new Intent();
                intent.putExtra("Pkg", appInfo.getPkgName());
                intent.putExtra("AppLabel",appInfo.getAppLabel());
                setResult(1,intent);
                finish();
            }
        });
    }
    public void queryAppInfo() {
        PackageManager pm = this.getPackageManager(); // 获得PackageManager对象
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfo = pm.queryIntentActivities(mainIntent, 0);

        Collections.sort(resolveInfo, new ResolveInfo.DisplayNameComparator(pm));
        if (appList != null) {
            appList.clear();
            for (ResolveInfo reInfo : resolveInfo) {
                // 获得该应用程序的启动Activity的name
                String pkgName = reInfo.activityInfo.packageName; // 获得应用程序的包名
                String appLabel = (String) reInfo.loadLabel(pm); // 获得应用程序的Label
                Drawable icon = reInfo.loadIcon(pm); // 获得应用程序图标
                AppInfo AppInfo = new AppInfo();
                AppInfo.setAppLabel(appLabel);
                AppInfo.setPkgName(pkgName);
                AppInfo.setAppIcon(icon);
                appList.add(AppInfo); // 添加至列表中
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("Pkg", "Error:你没有选择要打开的应用");
        intent.putExtra("AppLabel","Error:你没有选择要打开的应用");
        setResult(1,intent);
        finish();
    }
}
