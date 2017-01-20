package com.cute.meido.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cute.meido.MainActivity;
import com.cute.meido.R;

import java.util.List;

/*
*  规则适配器
*  用于Listview中规则的显示
 */


public class RegularInfoAdapter extends ArrayAdapter<RegularInfo> {
    private int resourceId;
    public RegularInfoAdapter(Context context, int textViewResourceId, List<RegularInfo> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 状态的问题 稍后解决
        RegularInfo RegularInfo = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId,null);
        TextView id = (TextView)view.findViewById(R.id.id);
        TextView date = (TextView) view.findViewById(R.id.date);
        TextView time = (TextView) view.findViewById(R.id.time);
        TextView address = (TextView) view.findViewById(R.id.address);
        TextView premise = (TextView) view.findViewById(R.id.premise);
        TextView premiseInfo = (TextView) view.findViewById(R.id.premiseInfo);
        TextView action = (TextView) view.findViewById(R.id.action);
        TextView actionInfo = (TextView) view.findViewById(R.id.actionInfo);
        ImageView imageView = (ImageView)view.findViewById(R.id.imageView3);
        int colorSet[] = {
            R.color.momo,
            R.color.seiheki,
            R.color.asagi,
            R.color.sora,
            R.color.shinbashi,
            R.color.kuchiba,
            R.color.kohbai,
            R.color.kyara,
            R.color.usugaki,
            R.color.yamabukicha,
            R.color.oitake,
            R.color.kitsune,
            R.color.aonibi,
            R.color.shironeri,
            R.color.ebizome,
            R.color.tsuyukusa,
            R.color.wasurenagusa,
            R.color.mizu,
            R.color.umezome
        };
        imageView.setBackgroundColor(getContext().getResources().getColor(colorSet[Integer.parseInt(RegularInfo.getId())%(colorSet.length)]));
        id.setText(RegularInfo.getId());
        date.setText("日期 " + RegularInfo.getDate());
        time.setText("时间 " + RegularInfo.getTime());
        address.setText("位置 " + RegularInfo.getAddress());
        premise.setText("条件 " + RegularInfo.getPremise());
        premiseInfo.setText(" " + RegularInfo.getPremiseInfo());
        action.setText("行为 " + RegularInfo.getAction());
        if(RegularInfo.getAction().equals("打开应用")){
            actionInfo.setText(" " + MainActivity.pkgMap.get(RegularInfo.getActionInfo()));
        }else{
            actionInfo.setText(" " + RegularInfo.getActionInfo());
        }
        return view;
    }
}