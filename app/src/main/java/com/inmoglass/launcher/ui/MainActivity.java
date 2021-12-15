package com.inmoglass.launcher.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
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
import com.inmoglass.launcher.util.WeatherResUtil;
import com.inmoglass.launcher.view.PowerConsumptionRankingsBatteryView;
import com.permissionx.guolindev.PermissionX;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 */
public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String SHUT_DOWN_ACTION = "com.android.systemui.ready.poweraction";

    private final String[] packageNames = new String[]{
            "com.inmolens.inmomemo",
            "com.yulong.coolcamera",
            "com.inmoglass.album",
            "com.ichano.athome.camera",
            "com.inmo.settings",
            "com.tentencent.qqlive"
    };

    private final String[] packageActivities = new String[]{
            "com.inmolens.inmomemo.MainActivity",
            "com.yulong.arcamera.MainActivity",
            "com.inmoglass.album.ui.MainActivity",
            "com.ichano.athome.camera",
            "com.inmo.settings",
            "com.tentencent.qqlive"
    };

    private PowerConsumptionRankingsBatteryView batteryView;
    private ImageView isChargingImageView;
    private ImageView weatherImageView;
    private TextView weatherTextView;
    private TextView temperatureTextView;
    private LauncherAdapter launcherAdapter;
    private RecyclerView launcherRecyclerView;
    private List<Channel> channelList;
    private CarouselLayoutManager carouselLayoutManager;

    private BatteryReceiver batteryReceiver;

    /**
     * 记录滚动选中的position
     */
    private int selectPosition = 0;

    private GestureDetector mGestureDetector;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();

        PermissionX.init(this)
                .permissions(Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                .request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        startLocation();
                    }
                });

        startSocketService();

        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                AppUtil.getInstance().openApplication(packageNames[selectPosition], packageActivities[selectPosition]);
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        batteryReceiver = new BatteryReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        intentFilter.addAction(SHUT_DOWN_ACTION);
        registerReceiver(batteryReceiver, intentFilter);
        startLocation();
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
        channelList = new ArrayList<>();
        channelList.add(new Channel(R.drawable.img_home_beiwanglu, getString(R.string.string_home_beiwanglu), R.drawable.icon_home_beiwanglu));
        channelList.add(new Channel(R.drawable.img_home_camera, getString(R.string.string_home_camera), R.drawable.icon_home_camera));
        channelList.add(new Channel(R.drawable.img_home_meitiwenjian, getString(R.string.string_home_media), R.drawable.icon_home_meiti));
        channelList.add(new Channel(R.drawable.img_home_kanjia, getString(R.string.string_home_kanjia), R.drawable.icon_home_kanjia));
        channelList.add(new Channel(R.drawable.img_home_setting, getString(R.string.string_home_setting), R.drawable.icon_home_setting));
        channelList.add(new Channel(R.drawable.img_home_tengxun, getString(R.string.string_home_tencent), R.drawable.icon_home_tengxun));
        launcherAdapter = new LauncherAdapter(this, channelList);
        carouselLayoutManager = new CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL, false);
        setLayoutManager(carouselLayoutManager, launcherAdapter);
    }

    private void setLayoutManager(CarouselLayoutManager carouselLayoutManager, LauncherAdapter launcherAdapter) {
        carouselLayoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener(0.12f));
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

    class BatteryReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case SHUT_DOWN_ACTION:
                    Intent shutdownIntent = new Intent("com.android.systemui.keyguard.shutdown");
                    sendBroadcast(shutdownIntent);
                    break;
                case Intent.ACTION_BATTERY_CHANGED:
                    int battery = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                    int isCharging = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                    isChargingImageView.setVisibility(isCharging == 0 ? View.INVISIBLE : View.VISIBLE);
                    batteryView.setLevelHeight(battery);
                    if (battery >= 0 && battery <= 5) {
                        batteryView.setOnline(getColor(R.color.color_battery_red));
                    } else if (battery > 5 && battery <= 15) {
                        batteryView.setOnline(getColor(R.color.color_battery_orange));
                    } else if (battery > 15 && battery <= 100) {
                        batteryView.setOnline(getColor(R.color.color_battery_white));
                    }
                    break;
                default:
                    break;
            }
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
                    String temp = (String) nowObj.get("temp");
                    String icon = (String) nowObj.get("icon");
                    String text = (String) nowObj.get("text");
                    if (temperatureTextView != null) {
                        temperatureTextView.setText(temp + "℃");
                    }
                    if (weatherTextView != null) {
                        weatherTextView.setText(text + "℃");
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
