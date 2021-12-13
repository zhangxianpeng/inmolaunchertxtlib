package com.inmoglass.launcher.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inmoglass.launcher.R;
import com.inmoglass.launcher.bean.AppInfo;

import java.util.List;

/**
 * @author Administrator
 */
public class ApplistAdapter extends RecyclerView.Adapter<ApplistAdapter.ViewHolder> implements View.OnClickListener {
    private Context context;
    private List<AppInfo> srcList;

    /**
     * 页数下标,从0开始(通俗讲第几页)
     */
    private int mIndex;

    /**
     * 每页显示最大条目个数
     */
    private int mPageSize;

    private OnRecyclerViewItemClickListener onRecyclerViewItemClickListener;

    private RecyclerView rvParent;

    private int selectedPosition;

    public void setOnItemClickListener(OnRecyclerViewItemClickListener onRecyclerViewItemClickListener) {
        this.onRecyclerViewItemClickListener = onRecyclerViewItemClickListener;
    }

    public ApplistAdapter(Context context, List<AppInfo> srcList, int index, int pageSize) {
        this.context = context;
        this.srcList = srcList;
        this.mPageSize = pageSize;
        this.mIndex = index;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        rvParent = (RecyclerView) parent;
        View view = LayoutInflater.from(context).inflate(R.layout.item_recyclerview, parent, false);
        view.setOnClickListener(this);
        return new ViewHolder(view);
    }

    private boolean isSelected = false;

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final int pos = position + mIndex * mPageSize;
        AppInfo bean = srcList.get(pos);
        holder.imageView.setImageDrawable(bean.getAppIcon());
        String name = bean.getPkgName();
        holder.textView.setText(bean.getAppName());
        int selectedPosition = getSelectedPosition();
        AppInfo currentApp = srcList.get(selectedPosition);
        String currentName = currentApp.getPkgName();
//        if (currentName.equals(name)) {
//            holder.containerRl.setBackground(context.getResources().getDrawable(R.drawable.item_menu_selected));
//        } else {
//            if (mIndex != 0 && position == 0 && !isSelected) {
//                isSelected = true;
//                holder.containerRl.setBackground(context.getResources().getDrawable(R.drawable.item_menu_selected));
//            } else {
//                holder.containerRl.setBackground(context.getResources().getDrawable(R.drawable.bg_app_item));
//            }
//        }
    }

    @Override
    public int getItemCount() {
        return srcList.size() > (mIndex + 1) * mPageSize ? mPageSize : (srcList.size() - mIndex * mPageSize);
    }

    @Override
    public long getItemId(int position) {
        return position + mIndex * mPageSize;
    }

    @Override
    public void onClick(View view) {
        int position = rvParent.getChildAdapterPosition(view);
        if (onRecyclerViewItemClickListener != null) {
            onRecyclerViewItemClickListener.onItemClick(rvParent, view, position);
        }
    }

    public void setSelecteded(int position) {
        this.selectedPosition = position;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.icon);
            textView = itemView.findViewById(R.id.appname);
        }
    }

    public interface OnRecyclerViewItemClickListener {
        /**
         * item 点击
         *
         * @param parent   父布局
         * @param view     view
         * @param position position
         */
        void onItemClick(RecyclerView parent, View view, int position);
    }
}
