package com.inmoglass.otaupgrade.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.inmoglass.otaupgrade.R;


public class ToastUtil {

    //显示文本的Toast
    public static void showTextToats(Context context, String message) {
        View toastview = LayoutInflater.from(context).inflate(R.layout.toast_text_layout, null);
        TextView text = (TextView) toastview.findViewById(R.id.tv_toolst);
        text.setText(message);
        Toast toast = new Toast(context);
        toast.setGravity(Gravity.CENTER, 0, 1000);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(toastview);
        toast.show();
    }
}
