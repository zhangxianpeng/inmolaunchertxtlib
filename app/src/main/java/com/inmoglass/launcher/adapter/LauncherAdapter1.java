package com.inmoglass.launcher.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.TimeUtils;
import com.inmoglass.launcher.R;
import com.inmoglass.launcher.bean.Channel;
import com.inmoglass.launcher.bean.InmoMemoData;
import com.inmoglass.launcher.util.AppUtil;
import com.inmoglass.launcher.util.CommonUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by chenxiaoping on 2017/3/28.
 */
public class LauncherAdapter1 extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private List<Channel> channelList;

    public enum ITEM_TYPE {
        /**
         * 图片式卡片
         */
        ITEM_NORMAL,
        /**
         * 备忘录的卡片
         */
        ITEM_MEMO
    }

    public LauncherAdapter1(Context c, List<Channel> mData) {
        mContext = c;
        channelList = mData;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE.ITEM_NORMAL.ordinal()) {
            return new NormalViewHolder(mLayoutInflater.inflate(R.layout.item_coverflow_view, parent, false));
        } else {
            return new MemoViewHolder(mLayoutInflater.inflate(R.layout.item_coverflow_memo, parent, false));
        }
//        int layout = R.layout.item_coverflow_view;
//        View v = LayoutInflater.from(mContext).inflate(layout, parent, false);
//        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        Channel channel = channelList.get(position);
        if (channel == null) {
            return;
        }
        if (holder instanceof NormalViewHolder) {
            ((NormalViewHolder) holder).img.setImageResource(channelList.get(position).getAppImg());
            // 出厂预装跟第三方安装应用的区别
            if (AppUtil.getInstance().checkAppIsExit(channel.getPackageName())) {
                ((NormalViewHolder) holder).icon.setImageResource(channelList.get(position).getAppIcon());
            } else {
                ((NormalViewHolder) holder).icon.setImageDrawable(channelList.get(position).getAppIconDrawable());
            }
            ((NormalViewHolder) holder).name.setText(channelList.get(position).getAppName());
        } else if (holder instanceof MemoViewHolder) {
            InmoMemoData data = channel.getData();
            if (data == null) {
                ((MemoViewHolder) holder).dateTv.setText(TimeUtils.millis2String(System.currentTimeMillis(), mContext.getString(R.string.string_time_format_1)));
                ((MemoViewHolder) holder).timeTv.setText(TimeUtils.millis2String(System.currentTimeMillis(), "HH:mm"));
                ((MemoViewHolder) holder).contentTv.setVisibility(View.GONE);
                ((MemoViewHolder) holder).defaultIv.setVisibility(View.VISIBLE);
                ((MemoViewHolder) holder).addTimeTv.setVisibility(View.GONE);
                ((MemoViewHolder) holder).addSomethingTv.setVisibility(View.VISIBLE);
                ((MemoViewHolder) holder).titleRl.setBackgroundColor(mContext.getColor(R.color.color_memo_title_gray));
                return;
            }
            ((MemoViewHolder) holder).titleRl.setBackgroundColor(mContext.getColor(R.color.color_memo_title_yellow));
            ((MemoViewHolder) holder).defaultIv.setVisibility(View.GONE);
            ((MemoViewHolder) holder).contentTv.setVisibility(View.VISIBLE);
            ((MemoViewHolder) holder).addSomethingTv.setVisibility(View.GONE);
            ((MemoViewHolder) holder).addTimeTv.setVisibility(View.VISIBLE);
            ((MemoViewHolder) holder).dateTv.setText(TimeUtils.millis2String(data.getTimestamp(), mContext.getString(R.string.string_time_format_1)));
            ((MemoViewHolder) holder).timeTv.setText(TimeUtils.millis2String(data.getTimestamp(), "HH:mm"));
            ((MemoViewHolder) holder).contentTv.setText(data.getContent());
            ((MemoViewHolder) holder).addTimeTv.setText(TimeUtils.millis2String(data.getCreateTime(), mContext.getString(R.string.string_time_format_2)));
        }
    }

    private String getDate(long time) {
        if (TimeUtils.isToday(time)) {
            return mContext.getString(R.string.string_today);
        }
        if (TimeUtils.isToday(time - 86400000)) {
            return mContext.getString(R.string.string_tomorrow);
        }
        return new SimpleDateFormat("E", Locale.getDefault()).format(new Date(time));
    }

    @Override
    public int getItemViewType(int position) {
        return !CommonUtil.isEn() && isMemoType(position) ? ITEM_TYPE.ITEM_MEMO.ordinal() : ITEM_TYPE.ITEM_NORMAL.ordinal();
    }

    /**
     * 如果是备忘录类型的话为另一种type
     *
     * @param position
     * @return
     */
    private boolean isMemoType(int position) {
        boolean isMemoType;
        Channel item = channelList.get(position);
        isMemoType = item.getPackageName().equals("com.inmolens.inmomemo");
        return isMemoType;
    }

    @Override
    public int getItemCount() {
        return channelList == null ? 0 : channelList.size();
    }

    public static class NormalViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        ImageView icon;
        TextView name;

        public NormalViewHolder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.iv_channelImg);
            icon = itemView.findViewById(R.id.iv_icon);
            name = itemView.findViewById(R.id.tv_appName);
        }
    }

    public static class MemoViewHolder extends RecyclerView.ViewHolder {
        TextView dateTv;
        TextView timeTv;
        TextView contentTv;
        TextView addTimeTv;
        TextView addSomethingTv;
        ImageView defaultIv;
        RelativeLayout titleRl;

        public MemoViewHolder(View itemView) {
            super(itemView);
            dateTv = itemView.findViewById(R.id.tv_date);
            timeTv = itemView.findViewById(R.id.tv_time);
            contentTv = itemView.findViewById(R.id.tv_content);
            addTimeTv = itemView.findViewById(R.id.tv_add_time);
            addSomethingTv = itemView.findViewById(R.id.tv_add_something);
            defaultIv = itemView.findViewById(R.id.iv_default);
            titleRl = itemView.findViewById(R.id.rl_title);
        }
    }
}
