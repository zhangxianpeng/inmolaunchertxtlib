package com.inmoglass.launcher.ui;

import static com.inmoglass.launcher.global.AppGlobals.NOVICE_TEACHING_VIDEO_PLAY_FLAG;
import static com.inmoglass.launcher.util.AppUtil.isInstalled;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.inmo.inmodata.notify.NotifyInfo;
import com.inmo.network.AndroidNetworking;
import com.inmo.network.error.ANError;
import com.inmo.network.interfaces.JSONObjectRequestListener;
import com.inmoglass.launcher.R;
import com.inmoglass.launcher.adapter.LauncherAdapter1;
import com.inmoglass.launcher.base.BaseActivity;
import com.inmoglass.launcher.base.BaseApplication;
import com.inmoglass.launcher.bean.Channel;
import com.inmoglass.launcher.bean.InmoMemoData;
import com.inmoglass.launcher.bean.ScreenFlagMsgBean;
import com.inmoglass.launcher.carousellayoutmanager.CarouselLayoutManager;
import com.inmoglass.launcher.carousellayoutmanager.CarouselZoomPostLayoutListener;
import com.inmoglass.launcher.carousellayoutmanager.CenterScrollListener;
import com.inmoglass.launcher.service.SocketService;
import com.inmoglass.launcher.util.AppUtil;
import com.inmoglass.launcher.util.BtUtil;
import com.inmoglass.launcher.util.CommonUtil;
import com.inmoglass.launcher.util.LauncherManager;
import com.inmoglass.launcher.util.MMKVUtils;
import com.inmoglass.launcher.util.SoundPoolUtil;
import com.inmoglass.launcher.util.ToastUtil;
import com.inmoglass.launcher.util.WeatherResUtil;
import com.inmoglass.launcher.util.WindowUtils;
import com.inmoglass.launcher.view.PowerConsumptionRankingsBatteryView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;

/**
 * 此类为动态配置排列顺序使用
 *
 * @author Administrator
 */
public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private TextView batteryLevelTextView;
    private PowerConsumptionRankingsBatteryView batteryView;
    private ImageView isChargingImageView;
    private ImageView weatherImageView;
    private TextView weatherTextView;
    private TextView temperatureTextView;
    private LauncherAdapter1 launcherAdapter;
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

    public static final int NOTIFICATION_ID = 1;
    NotificationManager notificationManager;
    String CHANNEL_ID = "channel_id";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        channelList = new ArrayList<>();
        notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "name", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        initViews();
        boolean isPlayed = MMKVUtils.getBoolean(NOVICE_TEACHING_VIDEO_PLAY_FLAG);
        LogUtils.d("isPlayed=" + isPlayed);
        // 监听开机广播不可靠，舍弃这个判断
        if (!isPlayed) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            WindowUtils.showPopupWindow(BaseApplication.mContext, WindowUtils.UI_STATE.BEGINNER_VIDEO, "");
        }
        // fix bug:权限请求交由系统端做,动态请求代码可注释。
//        needOverlayPermission();
//        PermissionX.init(this)
//                .permissions(Manifest.permission.ACCESS_COARSE_LOCATION,
//                        Manifest.permission.ACCESS_FINE_LOCATION,
//                        Manifest.permission.READ_EXTERNAL_STORAGE,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                .request((allGranted, grantedList, deniedList) -> {
//                    if (allGranted) {
//                        startLocation();
//                        writeCardList2File();
//                    }
//                });
        startLocation();
        writeCardList2File();
        startSocketService();

        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                LogUtils.i(TAG, "selectPosition = " + selectPosition);
                // fix bug:camera 特殊处理,防止卡死
                if (selectPosition == 1) {
                    openApplication("com.yulong.coolcamera", "com.yulong.arcamera.MainActivity");
                } else {
                    openApplicationByPkgName(channelList.get(selectPosition).getPackageName());
                }
                return true;
            }
        });

        subscribeBroadCast();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        LogUtils.i(TAG, "onResume");
        super.onResume();
        startLocation();
        getMemoData();
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
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getNotifyMsg(NotifyInfo msg) {
        if (msg == null) {
            return;
        }
        String pkgName = msg.getPackageName();
        String title = msg.getTitle();
        String content = msg.getContent();
        LogUtils.d("NotifyMsg=" + pkgName + ", title=" + title + ", content=" + content);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void clearScreenFlag(ScreenFlagMsgBean msg) {
        if (msg == null) {
            return;
        }
        boolean isNeedClear = msg.isNeedClear();
        if (isNeedClear) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mGestureDetector.onTouchEvent(ev);
        MotionEvent ev2 = MotionEvent.obtain(ev.getDownTime(), ev.getEventTime(), ev.getAction(), -(ev.getX()), ev.getY(), ev.getMetaState());
        launcherRecyclerView.dispatchTouchEvent(ev2);
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
        batteryLevelTextView = findViewById(R.id.tv_battery_level);
        weatherImageView = findViewById(R.id.iv_temperature);
        weatherTextView = findViewById(R.id.tv_weather);
        temperatureTextView = findViewById(R.id.tv_temperature);
        launcherRecyclerView = findViewById(R.id.list);
        initAdapter();
    }

    private void initAdapter() {
        launcherAdapter = new LauncherAdapter1(this, channelList);
        carouselLayoutManager = new CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL, false);
        setLayoutManager(carouselLayoutManager, launcherAdapter);
        // 解决刷新闪烁动画
        ((DefaultItemAnimator) launcherRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    private void setLayoutManager(CarouselLayoutManager carouselLayoutManager, LauncherAdapter1 launcherAdapter) {
        carouselLayoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener(0.20f, 0.20f));
        carouselLayoutManager.setMaxVisibleItems(2);
        launcherRecyclerView.setLayoutManager(carouselLayoutManager);
        launcherRecyclerView.setScrollingTouchSlop(250);
        launcherRecyclerView.setHasFixedSize(true);
        launcherRecyclerView.setAdapter(launcherAdapter);
        launcherRecyclerView.addOnScrollListener(new CenterScrollListener());
        setMaxFlingVelocity(launcherRecyclerView, 2000);
        carouselLayoutManager.addOnItemSelectionListener(adapterPosition -> selectPosition = adapterPosition);
        carouselLayoutManager.setSoundManagerListener(() -> SoundPoolUtil.getInstance(MainActivity.this).play(R.raw.swipe_card));
        // 不让左边留白
        launcherRecyclerView.scrollToPosition(CommonUtil.isEn() ? 1 : 2);
    }

    public static void setMaxFlingVelocity(RecyclerView recyclerView, int velocity) {
        try {
            Field field = recyclerView.getClass().getDeclaredField("mMaxFlingVelocity");
            field.setAccessible(true);
            field.set(recyclerView, velocity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startSocketService() {
        Intent intent = new Intent(this, SocketService.class);
        startService(intent);
    }

    private void setLauncherCard() {
        ArrayList<Channel> local = CommonUtil.isEn() ? LauncherManager.getInstance().getLauncherCardList_EN() : LauncherManager.getInstance().getLauncherCardList();
        channelList.addAll(local);
        updateAdapter();
    }

    private String uri = "content://com.inmolens.inmomemo.data.memodb/memo_info";
    private InmoMemoData showData;
    private Handler memoHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            int action = msg.what;
            channelList.clear();
            ArrayList<Channel> local = new ArrayList<>();
            if (action == 1) {
                InmoMemoData data = (InmoMemoData) msg.obj;
                Channel memoDataChannel = new Channel("com.inmolens.inmomemo", data);
                if (CommonUtil.isEn()) {
                    local = LauncherManager.getInstance().getLauncherCardList_EN();
                    channelList.addAll(local);
                } else {
                    local = LauncherManager.getInstance().getLauncherCardList();
                    channelList.addAll(local);
                    Channel oldChannel = channelList.get(2);
                    Collections.replaceAll(channelList, oldChannel, memoDataChannel);
                }
                LogUtils.i(TAG, "local = " + local.size());
            } else {
                local = CommonUtil.isEn() ? LauncherManager.getInstance().getLauncherCardList_EN() : LauncherManager.getInstance().getLauncherCardList();
                channelList.addAll(local);
            }
            updateAdapter();
        }
    };

    private void getMemoData() {
        showData = new InmoMemoData();
        new Thread(() -> {
            Cursor cursor = getContentResolver().query(Uri.parse(uri), null, null, null, "_id DESC");
            if (cursor == null) {
                return;
            }
            boolean hasData = cursor.moveToNext();
            if (hasData) {
                while (true) {
                    String id = cursor.getString(cursor.getColumnIndex("_id"));
                    String content = cursor.getString(cursor.getColumnIndex("info_content"));
                    long time = cursor.getLong(cursor.getColumnIndex("info_time"));
                    long createTime = cursor.getLong(cursor.getColumnIndex("info_create_time"));
                    showData.setId(Integer.parseInt(id));
                    showData.setContent(content);
                    showData.setTimestamp(time);
                    showData.setCreateTime(createTime);
                    LogUtils.i(TAG, "memoData,id=" + id + "content=" + content + ",time=" + CommonUtil.getStrTime(time));
                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = showData;
                    memoHandler.sendMessage(msg);
                    break;
                }
            } else {
                // 没有备忘录数据的时候直接展示空界面
                memoHandler.sendEmptyMessage(0);
            }

        }).start();

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
                .addQueryParameter("lang", CommonUtil.isEn() ? "en" : "zh")
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
            // 异步获取天气信息，不然可能造成anr
            new Thread(() -> getLocationWeather(latitude, longitude)).start();
        }
    }

    private void writeCardList2File() {
        ArrayList<Channel> local = CommonUtil.isEn() ? LauncherManager.getInstance().getLauncherCardList_EN() : LauncherManager.getInstance().getLauncherCardList();
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
        batteryFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        // 蓝牙
        batteryFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        batteryFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        batteryFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
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
    private boolean isBatteryBelow15 = false;
    private boolean isBatteryBelow6 = false;
    private boolean isBatteryBelow2 = false;
    private boolean isShowCharging = false;

    /**
     * 是否在充电
     **/
    public static boolean isChargingNow = false;

    class BatteryReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            isChargingNow = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) != 0;
            String action = intent.getAction();
            switch (action) {
                case ALARM_MEMO_LOG:
                    // 接收到备忘录传过来的内容
                    String content = intent.getStringExtra("MemoContent");
                    long memoTime = intent.getLongExtra("MemoShowTime", 0);
                    String date = TimeUtils.millis2String(memoTime, getString(R.string.string_time_format_1));
                    String time = TimeUtils.millis2String(memoTime, "HH:mm");
                    String completeMemoContent = getString(R.string.string_memo_data) + "," + date + "," + time + "," + content;
                    LogUtils.i(TAG, "Memo content = " + completeMemoContent);
                    WindowUtils.showPopupWindow(getApplicationContext(), WindowUtils.UI_STATE.MEMO_STATE, completeMemoContent);
                    break;
                case SHUT_DOWN_ACTION:
                    WindowUtils.showPopupWindow(getApplicationContext(), WindowUtils.UI_STATE.SHUT_DOWN_ACTION, "");
                    break;
                case Intent.ACTION_BATTERY_CHANGED:
                    int battery = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                    batteryLevelTextView.setText(battery + "%");
                    isChargingImageView.setVisibility(isChargingNow ? View.VISIBLE : View.GONE);
//                    if (isChargingNow) {
//                        if (!isShowCharging) {
//                            // 保证一次插入移除充电器显示一次
//                            WindowUtils.showPopupWindow(getApplicationContext(), WindowUtils.UI_STATE.CHARGING, battery + "");
//                            isShowCharging = true;
//                        }
//                    } else {
//                        isShowCharging = false;
//                        if (WindowUtils.isShown) {
//                            WindowUtils.hidePopupWindow();
//                        }
//                    }
                    if (battery > 5 && battery < 15 && !isBatteryBelow15) {
                        // 电量低于15时显示Toast提示电量低
                        isBatteryBelow15 = true;
                        isBatteryBelow6 = false;
                        ToastUtil.showImageToast(getApplicationContext(), ToastUtil.STATE.LOW_BATTERY, "");
                        // fix bug: 低电量提示后不做操作，充电非低电量后弹框不消失
                        WindowUtils.dismissLowBatteryWindow(WindowUtils.UI_STATE.BATTERY_BELOW_6);
                    }
                    if (battery >= 2 && battery < 6 && !isBatteryBelow6) {
                        // 电量低于6时显示低电量提示
                        isBatteryBelow6 = true;
                        isBatteryBelow2 = false;
                        WindowUtils.showPopupWindow(getApplicationContext(), WindowUtils.UI_STATE.BATTERY_BELOW_6, "");
                        SoundPoolUtil.getInstance(MainActivity.this).playSoundUnfinished(R.raw.low_battery);
                    }
                    if (battery < 2 && !isBatteryBelow2) {
                        // 电量低于2时倒计时15S关机
                        isBatteryBelow2 = true;
                        WindowUtils.showPopupWindow(getApplicationContext(), WindowUtils.UI_STATE.BATTERY_BELOW_2, "");
                        SoundPoolUtil.getInstance(MainActivity.this).playSoundUnfinished(R.raw.will_shut_down);
                    }
                    if (battery >= 0 && battery <= 5) {
                        // 假数据，不然不到一个等级不明显
                        isBatteryBelow2 = false;
                        batteryView.setLevelHeight(battery + 20);
                        batteryView.setOnline(getColor(R.color.color_battery_red));
                    } else if (battery > 5 && battery <= 15) {
                        isBatteryBelow6 = false;
                        // 假数据，不然不到一个等级不明显
                        batteryView.setLevelHeight(battery + 20);
                        batteryView.setOnline(getColor(R.color.color_battery_orange));
                    } else if (battery > 15 && battery <= 100) {
                        isBatteryBelow15 = false;
                        // 假数据，不然不到一个等级不明显
                        batteryView.setLevelHeight(battery + 20);
                        if (isChargingNow) {
                            batteryView.setOnline(getColor(R.color.color_battery_green));
                        } else {
                            batteryView.setOnline(getColor(R.color.color_battery_white));
                        }
                    }
                    break;
                case ConnectivityManager.CONNECTIVITY_ACTION:
                    ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo info = manager.getActiveNetworkInfo();
                    if (info != null && info.isAvailable()) {
                        ToastUtil.showToast(getApplicationContext(), getString(R.string.string_wlan_connected));
                        SoundPoolUtil.getInstance(MainActivity.this).playSoundUnfinished(R.raw.connect);
                    } else {
                        ToastUtil.showToast(getApplicationContext(), getString(R.string.string_wlan_disconnected));
                        SoundPoolUtil.getInstance(MainActivity.this).playSoundUnfinished(R.raw.disconnect);
                    }
                    break;
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    LogUtils.d("ACTION_ACL_CONNECTED");
                    // 已经配对过的设备不会再配对，直接自动连接
                    BluetoothDevice connectDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (connectDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                        SoundPoolUtil.getInstance(MainActivity.this).playSoundUnfinished(R.raw.connect);
                        ToastUtil.showToast(getApplicationContext(), getString(R.string.string_bluetooth_connected));
                    }
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    BluetoothDevice disConnectDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String disConnectDeviceName = TextUtils.isEmpty(disConnectDevice.getName()) ? "" : disConnectDevice.getName();
                    LogUtils.d("ACTION_ACL_DISCONNECTED=" + disConnectDevice.getBondState());
                    if (disConnectDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                        SoundPoolUtil.getInstance(MainActivity.this).playSoundUnfinished(R.raw.disconnect);
                        if (BtUtil.isInmoRing(disConnectDeviceName)) {
                            ToastUtil.showImageToast(getApplicationContext(), ToastUtil.STATE.INMO_RING, disConnectDeviceName);
                        } else {
                            ToastUtil.showToast(getApplicationContext(), getString(R.string.string_bluetooth_disconnected));
                        }
                    }
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
                    if (state == BluetoothDevice.BOND_NONE) {
                        // 配对没有成功，配对框的时候选择取消按钮的回调
                        SoundPoolUtil.getInstance(MainActivity.this).playSoundUnfinished(R.raw.disconnect);
                        ToastUtil.showToast(getApplicationContext(), getString(R.string.string_bluetooth_disconnected_retry));
                    } else if (state == BluetoothDevice.BOND_BONDED) {
                        // 配对成功
                        ToastUtil.showToast(getApplicationContext(), getString(R.string.string_bluetooth_connected));
                        SoundPoolUtil.getInstance(MainActivity.this).playSoundUnfinished(R.raw.connect);
                    }
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
                    LogUtils.i(TAG, "卸载应用");
                    updateAppList(false, packageName);
                    break;
                case Intent.ACTION_PACKAGE_ADDED:    // 安装
                    LogUtils.i(TAG, "安装应用");
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
            // fix bug:安装语音APK的时候闪退
            if (bean == null) {
                return;
            }
            LogUtils.i(TAG, "recentInstall App = " + bean.getAppName() + "," + bean.getPackageName());
            channelList.add(bean);
            updateAdapter();
        }
        if (!isAdd && !isRemoveSuccess) {
            // 卸载
            LogUtils.i(TAG, "isAdd = " + isAdd + ",packageName = " + realPackageName);
            if (channelList != null && !channelList.isEmpty()) {
                for (int i = 0; i < channelList.size(); i++) {
                    if (channelList.get(i).getPackageName().equals(realPackageName) && !AppUtil.getInstance().checkAppIsExit(realPackageName)) {
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
        isAddSuccess = false;
        isRemoveSuccess = false;
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

    private void openApplicationByPkgName(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return;
        }

        if (!isInstalled(MainActivity.this, pkgName)) {
            ToastUtils.showShort(getString(R.string.string_app_not_installed));
            return;
        }

        LogUtils.i(TAG, "The package will be open : " + pkgName);
        Intent intent = getPackageManager().getLaunchIntentForPackage(pkgName);
        if (intent == null) {
            return;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
//        ActivityOptionsCompat compat = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.anim_in, R.anim.anim_out);
//        ActivityCompat.startActivity(this, intent, compat.toBundle());
    }

    private void openApplication(String pkgName, String activityName) {
        if (TextUtils.isEmpty(pkgName)) {
            return;
        }
        if (!isInstalled(MainActivity.this, pkgName)) {
            ToastUtils.showShort(getString(R.string.string_app_not_installed));
            return;
        }
        Intent intent1 = getPackageManager().getLaunchIntentForPackage(pkgName);
        if (intent1 == null) {
            return;
        }
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName(pkgName, activityName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(componentName);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
//        ActivityOptionsCompat compat = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.anim_in, R.anim.anim_out);
//        ActivityCompat.startActivity(this, intent, compat.toBundle());
    }

    public static void clearScreenFlag() {
        ToastUtils.showShort("测试");
    }
}
