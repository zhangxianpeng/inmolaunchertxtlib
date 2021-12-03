package com.inmoglass.launcher.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.inmoglass.launcher.R;
import com.inmoglass.launcher.bean.InmoMemoData;
import com.inmoglass.launcher.util.CommonUtil;

import java.util.List;

/**
 * 备忘录适配器
 *
 * @author Administrator
 * @date 2021-12-01
 */
public class MemoAdapter extends BaseAdapter {

    private List<InmoMemoData> mData;
    private Context mContext;

    public MemoAdapter(List<InmoMemoData> mData, Context mContext) {
        this.mData = mData;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(mContext).inflate(R.layout.item_memo, parent, false);
        ImageView icon = convertView.findViewById(R.id.iv_icon);
        icon.setImageResource(position == 0 ? R.drawable.ic_yellow_circle : R.drawable.ic_blue_circle);
        TextView content = convertView.findViewById(R.id.tv_content);
        TextView time = convertView.findViewById(R.id.tv_time);
        content.setText(mData.get(position).getContent());
        time.setText(CommonUtil.getStrTime(mData.get(position).getTimestamp()));
        return convertView;
    }
}
