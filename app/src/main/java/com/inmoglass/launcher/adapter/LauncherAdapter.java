package com.inmoglass.launcher.adapter;

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
import com.inmoglass.launcher.util.AppUtil;

import java.util.List;

/**
 * Created by chenxiaoping on 2017/3/28.
 */
public class LauncherAdapter extends RecyclerView.Adapter<LauncherAdapter.ViewHolder> {

    private Context mContext;

    private List<Channel> channelList;

    public LauncherAdapter(Context c, List<Channel> mData) {
        mContext = c;
        channelList = mData;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = R.layout.item_coverflow_view;
        View v = LayoutInflater.from(mContext).inflate(layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Channel channel = channelList.get(position);
        if (channel == null) {
            return;
        }
        Glide.with(mContext).load(channelList.get(position).getAppImg()).into(holder.img);
        // 出厂预装跟第三方安装应用的区别
        if (AppUtil.getInstance().checkAppIsExit(channel.getPackageName())) {
            Glide.with(mContext).load(channelList.get(position).getAppIcon()).into(holder.icon);
        } else {
            Glide.with(mContext).load(channelList.get(position).getAppIconDrawable()).into(holder.icon);
        }
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
