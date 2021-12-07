package com.inmoglass.launcher.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.inmoglass.coverflowlib.ICoverFlowAdapter;
import com.inmoglass.launcher.R;
import com.inmoglass.launcher.bean.Channel;

import java.util.List;

/**
 * CoverFlowAdapter
 *
 * @author Administrator
 * @date 2021-12-03
 */
public class CoverFlowAdapter implements ICoverFlowAdapter {

    private List<Channel> mArray;
    private Context context;


    public CoverFlowAdapter(Context context, List<Channel> mArray) {
        this.context = context;
        this.mArray = mArray;
    }

    @Override
    public int getCount() {
        return mArray == null ? 0 : mArray.size();
    }

    @Override
    public Object getItem(int position) {
        return mArray.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            holder = new Holder();
            convertView = View.inflate(context, R.layout.item_coverflow_view, null);
            holder.ivChannelImg = (ImageView) convertView.findViewById(R.id.iv_channelImg);
            holder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
            holder.tvAppName = (TextView) convertView.findViewById(R.id.tv_appName);
        } else {
            holder = (Holder) convertView.getTag();
        }

        convertView.setTag(holder);
        return convertView;
    }

    @Override
    public void getData(View view, int position) {
        Holder holder = (Holder) view.getTag();

        Channel channelBean = mArray.get(position);
        Glide.with(context)
                .load(channelBean.getAppImg())
                .centerCrop()
                .into(holder.ivChannelImg);
        Glide.with(context)
                .load(channelBean.getAppIcon())
                .centerCrop()
                .into(holder.ivIcon);
        holder.tvAppName.setText(channelBean.getAppName());
    }

    public static class Holder {
        ImageView ivChannelImg;
        ImageView ivIcon;
        TextView tvAppName;
    }

}
