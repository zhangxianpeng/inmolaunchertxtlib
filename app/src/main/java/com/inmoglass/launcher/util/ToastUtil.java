package com.inmoglass.launcher.util;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.inmoglass.launcher.R;

/**
 * @author Administrator
 * Toast工具类
 */
public class ToastUtil {
    /**
     * 图文Toast,类似于低电量提醒
     *
     * @param context
     */
    public static void showImageToast(Context context) {
        View root = LayoutInflater.from(context).inflate(R.layout.layout_image_text_toast, null);
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
