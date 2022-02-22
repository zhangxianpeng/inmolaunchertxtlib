package com.inmoglass.launcher.util;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.inmoglass.launcher.R;
import com.inmoglass.launcher.base.BaseApplication;

/**
 * @author Administrator
 * Toast工具类
 */
public class ToastUtil {

    public enum STATE {
        /**
         * 低电
         */
        LOW_BATTERY,
        /**
         * 智能戒指
         */
        INMO_RING,
    }

    /**
     * 图文Toast,类似于低电量提醒
     *
     * @param context
     */
    public static void showImageToast(Context context, STATE uiState, String deviceName) {
        View root = LayoutInflater.from(context).inflate(R.layout.layout_image_text_toast, null);
        ImageView imageView = root.findViewById(R.id.imageView);
        TextView content = root.findViewById(R.id.tv_content);
        if (uiState == STATE.LOW_BATTERY) {
            imageView.setImageResource(R.drawable.icon_low_battery);
            content.setText(BaseApplication.mContext.getString(R.string.string_low_battery));
        } else if (uiState == STATE.INMO_RING) {
            imageView.setImageResource(R.drawable.ic_icon_set_ring02);
            content.setText(deviceName + BaseApplication.mContext.getString(R.string.string_inmo_ring_disconnected));
        }
        Toast toast = new Toast(context);
        toast.setGravity(Gravity.TOP, 0, 4);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(root);
        toast.show();
    }

    /**
     * 纯文本Toast
     *
     * @param context
     * @param content
     */
    public static void showToast(Context context, String content) {
        View root = LayoutInflater.from(context).inflate(R.layout.layout_text_toast, null);
        TextView contentTextView = root.findViewById(R.id.tv_content);
        contentTextView.setText(content);
        Toast toast = new Toast(context);
        toast.setGravity(Gravity.TOP, 0, 4);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(root);
        toast.show();
    }
}
