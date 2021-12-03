package com.inmoglass.launcher.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.inmoglass.launcher.R;

/**
 * @author Administrator
 * @github : https://192.168.3.113:8443/IMC-ROM/imc-launcher.git
 * @time : 2019/09/14
 * @desc : 全局状态栏View
 */
public class StatusBarView {
    private Context mContext;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParams;
    /**
     * 状态栏View
     */
    private RelativeLayout mContentBarView;

    public StatusBarView(Context context, WindowManager windowManager) {
        this.mContext = context;
        this.mWindowManager = windowManager;
    }

    public RelativeLayout getContentView() {
        mParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        mParams.format = PixelFormat.RGBA_8888;
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mParams.x = 0;
        mParams.y = 0;
        mParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        mParams.height = (int) mContext.getResources().getDimension(R.dimen.pt_48);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        mContentBarView = (RelativeLayout) inflater.inflate(R.layout.layout_status_bar, null);
        mParams.gravity = Gravity.TOP;
        mWindowManager.addView(mContentBarView, mParams);
        return mContentBarView;
    }

    public void clearAll() {
        if (mContentBarView != null) {
            mWindowManager.removeView(mContentBarView);
        }
    }
}
