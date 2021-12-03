package com.inmoglass.launcher.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inmoglass.launcher.R;
import com.inmoglass.launcher.bean.AppInfo;

import java.util.List;

/**
 * 应用商店Adapter
 *
 * @author Administrator
 * @date 2021-11-17
 */
public class AppstoreAdapter extends RecyclerView.Adapter<AppstoreAdapter.ViewHolder> {
    private static final String TAG = AppstoreAdapter.class.getSimpleName();
    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private List<AppInfo> mData;

    public AppstoreAdapter(Context context, List<AppInfo> data) {
        mLayoutInflater = LayoutInflater.from(context);
        this.mContext = context;
        this.mData = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(mLayoutInflater.inflate(R.layout.item_app_store, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(mData.get(position).getAppName());
        holder.icon.setImageDrawable(mData.get(position).getAppIcon());
        holder.downloadBtn.setOnClickListener(v -> {
            if (onRecyclerViewItemClickListener != null) {
                onRecyclerViewItemClickListener.onItemClick(holder.downloadBtn, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    private OnRecyclerViewItemClickListener onRecyclerViewItemClickListener;

    public void setOnItemClickListener(OnRecyclerViewItemClickListener onRecyclerViewItemClickListener) {
        this.onRecyclerViewItemClickListener = onRecyclerViewItemClickListener;
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView icon;
        FrameLayout downloadBtn;

        public ViewHolder(@NonNull View view) {
            super(view);
            name = view.findViewById(R.id.iv_app_name);
            icon = view.findViewById(R.id.iv_app);
            downloadBtn = view.findViewById(R.id.btn_download);
        }
    }
}
