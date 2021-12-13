package com.inmoglass.launcher.recyclerCoverFlow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.inmoglass.launcher.R;
import com.inmoglass.launcher.bean.Channel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenxiaoping on 2017/3/28.
 */
public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private Context mContext;

    private List<Channel> channelList;

    public Adapter(Context c,List<Channel> mData) {
        mContext = c;
        channelList = mData;
//        setData();
    }

    private void setData() {
        Channel channel = new Channel(R.drawable.img_home_beiwanglu, mContext.getString(R.string.string_home_beiwanglu), R.drawable.icon_home_beiwanglu, "dsadsa");
        Channel channel1 = new Channel(R.drawable.img_home_camera, mContext.getString(R.string.string_home_camera), R.drawable.icon_home_camera, "dsadsa");
        Channel channel2 = new Channel(R.drawable.img_home_meitiwenjian, mContext.getString(R.string.string_home_media), R.drawable.icon_home_meiti, "dsadsa");
        Channel channel3 = new Channel(R.drawable.img_home_setting, mContext.getString(R.string.string_home_setting), R.drawable.icon_home_setting, "dsadsa");
        Channel channel4 = new Channel(R.drawable.img_home_tengxun, mContext.getString(R.string.string_home_tencent), R.drawable.icon_home_tengxun, "dsadsa");
        channelList.add(channel);
        channelList.add(channel1);
        channelList.add(channel2);
        channelList.add(channel3);
        channelList.add(channel4);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = R.layout.item_coverflow_view2;
        View v = LayoutInflater.from(mContext).inflate(layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Glide.with(mContext).load(channelList.get(position).getAppImg()).into(holder.img);
        Glide.with(mContext).load(channelList.get(position).getAppIcon()).into(holder.icon);
        holder.name.setText(channelList.get(position).getAppName());
    }

    @Override
    public int getItemCount() {
        return channelList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        ImageView icon;
        TextView name;

        public ViewHolder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.iv_channelImg);
            icon = itemView.findViewById(R.id.iv_icon);
            name = itemView.findViewById(R.id.tv_appName);
        }
    }
}
