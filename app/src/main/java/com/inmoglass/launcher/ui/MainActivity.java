package com.inmoglass.launcher.ui;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.blankj.utilcode.util.LogUtils;
import com.inmo.network.AndroidNetworking;
import com.inmo.network.error.ANError;
import com.inmo.network.interfaces.JSONObjectRequestListener;
import com.inmoglass.launcher.R;
import com.inmoglass.launcher.adapter.LauncherAdapter;
import com.inmoglass.launcher.base.BaseActivity;
import com.inmoglass.launcher.bean.Channel;
import com.inmoglass.launcher.carousellayoutmanager.CarouselLayoutManager;
import com.inmoglass.launcher.carousellayoutmanager.CarouselZoomPostLayoutListener;
import com.inmoglass.launcher.carousellayoutmanager.CenterScrollListener;
import com.inmoglass.launcher.service.SocketService;
import com.inmoglass.launcher.util.AppUtil;
import com.inmoglass.launcher.util.LauncherManager;
import com.inmoglass.launcher.util.ToastUtil;
import com.inmoglass.launcher.util.WeatherResUtil;
import com.inmoglass.launcher.util.WindowUtils;
import com.inmoglass.launcher.view.PowerConsumptionRankingsBatteryView;
import com.permissionx.guolindev.PermissionX;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 此类为动态配置排列顺序使用
 *
 * @author Administrator
 */
public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private PowerConsumptionRankingsBatteryView batteryView;
    private ImageView isChargingImageView;
    private ImageView weatherImageView;
    private TextView weatherTextView;
    private TextView temperatureTextView;
    private LauncherAdapter launcherAdapter;
    private RecyclerView launcherRecyclerView;
    private ArrayList<Channel> channelList;
    private CarouselLayoutManager carouselLayoutManager;

    private BatteryReceiver batteryReceiver;
    private AppReceiver appReceiver;

    /**
     * 记录滚动选中的position
     */
    private int selectPosition = 0;

    private GestureDetector mGestureDetector;
    private static final String SHUT_DOWN_ACTION = "com.android.systemui.ready.poweraction";
    private static final String ALARM_MEMO_LOG = "com.inmolens.intent.action.ALARM_MEMO";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        channelList = new ArrayList<>();
        initViews();
        needOverlayPermission();
        PermissionX.init(this)
                .permissions(Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        startLocation();
                        writeCardList2File();
                    }
                });

        startSocketService();

        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                LogUtils.i(TAG, "selectPosition = " + selectPosition);
                if (selectPosition == 1) { // camera 特殊处理
                    AppUtil.getInstance().openApplication("com.yulong.coolcamera", "com.yulong.arcamera.MainActivity");
                } else {
                    AppUtil.getInstance().openApplicationByPkgName(channelList.get(selectPosition).getPackageName());
                }
                return true;
            }
        });

        subscribeBroadCast();
    }

    /**
     * 获取弹窗权限，否则windowManager addView报错
     */
    // TODO: 2021/12/24 弹窗权限需要打到系统里边去，避免需要自己去申请
    private void needOverlayPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(MainActivity.this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 10);
            }
        }
    }

    @Override
    protected void onResume() {
        LogUtils.i(TAG, "onResume");
        super.onResume();

        startLocation();
        channelList.clear();
        ArrayList<Channel> local = LauncherManager.getInstance().getLauncherCardList();
        LogUtils.i(TAG, "local = " + local.size());
        channelList.addAll(local);
        updateAdapter();

        isShowMemo = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (batteryReceiver != null) {
            unregisterReceiver(batteryReceiver);
        }
        if (myHandler != null) {
            myHandler = null;
        }
        if (mLocationClient != null) {
            mLocationClient = null;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        launcherRecyclerView.dispatchTouchEvent(ev);
        mGestureDetector.onTouchEvent(ev);
        return true;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // 屏蔽返回键，fix bug：下拉返回闪屏原生界面
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

    private void initViews() {
        batteryView = findViewById(R.id.sbb_battery);
        isChargingImageView = findViewById(R.id.iv_isCharging);
        weatherImageView = findViewById(R.id.iv_temperature);
        weatherTextView = findViewById(R.id.tv_weather);
        temperatureTextView = findViewById(R.id.tv_temperature);
        launcherRecyclerView = findViewById(R.id.list);
        initAdapter();
    }

    private void initAdapter() {
        launcherAdapter = new LauncherAdapter(this, channelList);
        carouselLayoutManager = new CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL, false);
        setLayoutManager(carouselLayoutManager, launcherAdapter);
    }

    private void setLayoutManager(CarouselLayoutManager carouselLayoutManager, LauncherAdapter launcherAdapter) {
        carouselLayoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener(0.20f, 0.20f));
        carouselLayoutManager.setMaxVisibleItems(3);
        launcherRecyclerView.setLayoutManager(carouselLayoutManager);
        launcherRecyclerView.setScrollingTouchSlop(250);
        launcherRecyclerView.setHasFixedSize(true);
        launcherRecyclerView.setAdapter(launcherAdapter);
        launcherRecyclerView.addOnScrollListener(new CenterScrollListener());
        carouselLayoutManager.addOnItemSelectionListener(adapterPosition -> selectPosition = adapterPosition);
    }

    private void startSocketService() {
        Intent intent = new Intent(this, SocketService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private LocationClient mLocationClient;
    private MyLocationListener myListener;

    private void startLocation() {
        mLocationClient = new LocationClient(getApplicationContext());
        myListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myListener);
        LocationClientOption locationOption = new LocationClientOption();
        locationOption.setIsNeedAddress(true);
        locationOption.setNeedNewVersionRgc(true);
        mLocationClient.setLocOption(locationOption);
        mLocationClient.start();
    }

    private void getLocationWeather(double latitude, double longitude) {
        String baseUrl = "https://devapi.qweather.com/v7/weather/now";
        String key = "b9754523666e4abeabef84808c64ed2b";
        AndroidNetworking.get(baseUrl)
                .addQueryParameter("key", key)
                .addQueryParameter("location", longitude + "," + latitude)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String code = response.getString("code");
                            if (!code.equals("200")) {
                                return;
                            }
                            Message msg = new Message();
                            msg.what = 0;
                            msg.obj = response;
                            myHandler.sendMessage(msg);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        LogUtils.e(TAG, "ANError = " + anError.getMessage());
                    }
                });
    }

    class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            double latitude = bdLocation.getLatitude();
            double longitude = bdLocation.getLongitude();
            getLocationWeather(latitude, longitude);
        }
    }

    private void writeCardList2File() {
        ArrayList<Channel> local = LauncherManager.getInstance().getLauncherCardList();
        if (local == null || local.isEmpty()) {
            LauncherManager.getInstance().setLauncherCardList();
        }
    }

    private void subscribeBroadCast() {
        batteryReceiver = new BatteryReceiver();
        IntentFilter batteryFilter = new IntentFilter();
        // 电量变化
        batteryFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        // 关机广播
        batteryFilter.addAction(SHUT_DOWN_ACTION);
        // 备忘录
        batteryFilter.addAction(ALARM_MEMO_LOG);
        // wifi
        batteryFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        batteryFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        batteryFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        // 蓝牙
        batteryFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        batteryFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        // 熄屏、亮屏
        batteryFilter.addAction(Intent.ACTION_SCREEN_ON);
        batteryFilter.addAction(Intent.ACTION_SCREEN_OFF);

        registerReceiver(batteryReceiver, batteryFilter);

        // 卸载、安装
        appReceiver = new AppReceiver();
        IntentFilter appFilter = new IntentFilter();
        appFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        appFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        appFilter.addDataScheme("package");
        registerReceiver(appReceiver, appFilter);
    }

    // 确保收到多次广播回调只执行一次
    private boolean isAddSuccess = false;
    private boolean isRemoveSuccess = false;
    private boolean isShowMemo = false;
    private boolean isBatteryBelow15 = false;
    private boolean isBatteryBelow6 = false;
    private boolean isBatteryBelow2 = false;
    private boolean isShowCharging = false;

    class BatteryReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtils.i(TAG, "onReceive Broadcast =" + action);
            switch (action) {
                case ALARM_MEMO_LOG:
                    // 接收到备忘录传过来的内容
                    String content = intent.getStringExtra("MemoContent");
                    long memoTime = intent.getLongExtra("MemoShowTime", -1);
                    if (System.currentTimeMillis() > memoTime + 10000) {
                        // 对已经超时的备忘录不做提示
                        return;
                    }
                    if (!isShowMemo) {
                        WindowUtils.showPopupWindow(getApplicationContext(), WindowUtils.UI_STATE.MEMO_STATE, content);
                    }
                    break;
                case SHUT_DOWN_ACTION:
                    startActivity(new Intent(MainActivity.this, PopupWindowActivity.class));
                    break;
                case Intent.ACTION_BATTERY_CHANGED:
                    int battery = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                    LogUtils.i(TAG, "当前电量 = " + battery);
                    int isCharging = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                    isChargingImageView.setVisibility(isCharging == 0 ? View.INVISIBLE : View.VISIBLE);
                    if (!isShowCharging) {
                        if (isCharging != 0) {
                            WindowUtils.showPopupWindow(getApplicationContext(), WindowUtils.UI_STATE.CHARGING, battery + "");
                        } else {
                            if (WindowUtils.isShown) {
                                WindowUtils.hidePopupWindow();
                                isShowCharging = false;
                            }
                        }
                    }
                    if (battery < 15 && !isBatteryBelow15) {
                        // 电量低于15时显示Toast提示电量低
                        isBatteryBelow15 = true;
                        ToastUtil.showImageToast(getApplicationContext());
                    }
                    if (battery < 6 && !isBatteryBelow6) {
                        // 电量低于6时显示低电量提示
                        isBatteryBelow6 = true;
                        WindowUtils.showPopupWindow(getApplicationContext(), WindowUtils.UI_STATE.BATTERY_BELOW_6, "");
                    }
                    if (battery < 2 && !isBatteryBelow2) {
                        // 电量低于2时倒计时30S关机
                        isBatteryBelow2 = true;
                        WindowUtils.showPopupWindow(getApplicationContext(), WindowUtils.UI_STATE.BATTERY_BELOW_2, "");
                    }
                    if (battery >= 0 && battery <= 5) {
                        batteryView.setLevelHeight(battery);
                        batteryView.setOnline(getColor(R.color.color_battery_red));
                    } else if (battery > 5 && battery <= 15) {
                        // 假数据，不然不明显
                        batteryView.setLevelHeight(battery + 20);
                        batteryView.setOnline(getColor(R.color.color_battery_orange));
                    } else if (battery > 15 && battery <= 100) {
                        // 假数据，不然不明显
                        batteryView.setLevelHeight(battery + 20);
                        batteryView.setOnline(getColor(R.color.color_battery_white));
                    }
                    break;
                case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                    NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                        ToastUtil.showToast(getApplicationContext(), getString(R.string.string_wlan_disconnected));
                        LogUtils.i(TAG, "wifi断开");
                    } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                        ToastUtil.showToast(getApplicationContext(), getString(R.string.string_wlan_connected));
                        LogUtils.i(TAG, "wifi连接");
                    }
                    break;
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    ToastUtil.showToast(getApplicationContext(), getString(R.string.string_bluetooth_connected));
                    LogUtils.i(TAG, "蓝牙设备已连接");
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    ToastUtil.showToast(getApplicationContext(), getString(R.string.string_bluetooth_disconnected));
                    LogUtils.i(TAG, "蓝牙设备已断开");
                    break;
                case Intent.ACTION_SCREEN_ON:
                    LogUtils.i(TAG, "设备亮屏");
                    break;
                case Intent.ACTION_SCREEN_OFF:
                    LogUtils.i(TAG, "设备灭屏");
                    break;
                default:
                    break;
            }
        }
    }

    class AppReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String packageName = intent.getDataString();
            switch (action) {
                case Intent.ACTION_PACKAGE_REMOVED:  // 卸载
                    updateAppList(false, packageName);
                    break;
                case Intent.ACTION_PACKAGE_ADDED:    // 安装
                    updateAppList(true, packageName);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 更新列表顺序
     *
     * @param isAdd       是安装还是卸载
     * @param packageName
     */
    private void updateAppList(boolean isAdd, String packageName) {
        // 截去package:
        String realPackageName = packageName.substring(8);
        if (isAdd && !isAddSuccess) {
            // 安装
            LogUtils.i(TAG, "isAdd = " + isAdd + ",packageName = " + realPackageName);
            isAddSuccess = true;
            if (AppUtil.getInstance().checkAppIsExit(realPackageName)) {
                return;
            }
            Channel bean = AppUtil.getInstance().getRecentInstallApp(this, realPackageName);
            LogUtils.i(TAG, "recentInstall App = " + bean.getAppName() + "," + bean.getPackageName());
            channelList.add(bean);
            updateAdapter();
        }
        if (!isAdd && !isRemoveSuccess) {
            // 卸载
            LogUtils.i(TAG, "isAdd = " + isAdd + ",packageName = " + realPackageName);
            if (channelList != null && !channelList.isEmpty()) {
                for (int i = 0; i < channelList.size(); i++) {
                    if (channelList.get(i).getPackageName().equals(realPackageName)) {
                        channelList.remove(i);
                        isRemoveSuccess = true;
                    }
                }
                updateAdapter();
            }
        }

        // 更新卡片排列顺序
        LauncherManager.getInstance().updateCardList(channelList);
    }

    private void updateAdapter() {
        if (launcherAdapter != null) {
            launcherAdapter.notifyDataSetChanged();
        }
    }

    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            int action = msg.what;
            if (action == 0) {
                JSONObject response = (JSONObject) msg.obj;
                try {
                    JSONObject nowObj = response.getJSONObject("now");
                    LogUtils.i(TAG, "weather = " + nowObj.toString());
                    String temp = (String) nowObj.get("temp");
                    String icon = (String) nowObj.get("icon");
                    String text = (String) nowObj.get("text");
                    if (temperatureTextView != null) {
                        temperatureTextView.setText(temp + "℃");
                    }
                    if (weatherTextView != null) {
                        weatherTextView.setText(text);
                    }
                    if (weatherImageView != null) {
                        weatherImageView.setImageResource(WeatherResUtil.getEqualRes(icon));
                    }
                } catch (JSONException e) {
                    LogUtils.e(TAG, "JSONException,e=" + e.getMessage());
                }
            }
        }
    };

}
