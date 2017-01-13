package com.cute.meido;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Cute on 2016/10/25.
 * 应用信息适配器类
 */

public class AppInfoAdapter extends ArrayAdapter<AppInfo> {

    private int resourceId;

    public AppInfoAdapter(Context context, int textViewResourceId, List<AppInfo> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        AppInfo appInfo = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId,null);
        ImageView imageView = (ImageView)view.findViewById(R.id.imageView);
        TextView textView = (TextView)view.findViewById(R.id.textView);
        TextView textView2 = (TextView)view.findViewById(R.id.textView2);
        imageView.setImageDrawable(appInfo.getAppIcon());
        textView.setText(appInfo.getAppLabel());
        textView2.setText(appInfo.getPkgName());
        return view;
    }
}
