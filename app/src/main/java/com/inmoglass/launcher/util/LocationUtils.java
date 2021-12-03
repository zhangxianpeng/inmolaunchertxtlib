package com.inmoglass.launcher.util;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;

import com.blankj.utilcode.util.LogUtils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * 定位工具类
 *
 * @author Administrator
 * @date 2021-12-01
 */
public class LocationUtils {
    private volatile static LocationUtils uniqueInstance;
    private LocationManager locationManager;
    private Context mContext;
    private AddressCallback addressCallback;

    public void setAddressCallback(AddressCallback addressCallback) {
        this.addressCallback = addressCallback;
        getLocation();
    }

    private static Location location;

    private LocationUtils(Context context) {
        mContext = context;
    }

    /**
     * 采用Double CheckLock(DCL)实现单例
     *
     * @param context
     * @return
     */
    public static LocationUtils getInstance(Context context) {
        if (uniqueInstance == null) {
            synchronized (LocationUtils.class) {
                if (uniqueInstance == null) {
                    uniqueInstance = new LocationUtils(context);
                }
            }
        }
        return uniqueInstance;
    }

    private void getLocation() {
        // 1.获取位置管理器
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        // 添加用户权限申请判断
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // 2.获取位置提供器，GPS或是NetWork
        // 获取所有可用的位置提供器
        List<String> providerList = locationManager.getProviders(true);
        String locationProvider;
        if (providerList.contains(LocationManager.GPS_PROVIDER)) {
            // GPS 定位的精准度比较高，但是非常耗电。
            locationProvider = LocationManager.GPS_PROVIDER;
        } else if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
            // Google服务被墙不可用
            // 网络定位的精准度稍差，但耗电量比较少。
            LogUtils.i("=====NETWORK_PROVIDER=====");
            locationProvider = LocationManager.NETWORK_PROVIDER;
        } else {
            LogUtils.i("=====NO_PROVIDER=====");
            // 当没有可用的位置提供器时，弹出Toast提示用户
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            mContext.startActivity(intent);
            return;
        }

        // 3.获取上次的位置，一般第一次运行，此值为null
        location = locationManager.getLastKnownLocation(locationProvider);
        if (location != null) {
            // 显示当前设备的位置信息
            showLocation();
        } else {
            // 当GPS信号弱没获取到位置的时候可从网络获取
            // Google服务被墙的解决办法
            getLngAndLatWithNetwork();
        }
        locationManager.requestLocationUpdates(locationProvider, 5000, 10, locationListener);
    }

    /**
     * 获取位置
     */
    private void showLocation() {
        if (location == null) {
            return;
        } else {
            // 纬度
            double latitude = location.getLatitude();
            // 经度
            double longitude = location.getLongitude();
            if (addressCallback != null) {
                addressCallback.onGetLocation(latitude, longitude);
            }
            getAddress(latitude, longitude);
        }
    }

    private void getAddress(double latitude, double longitude) {
        // Geocoder通过经纬度获取具体信息
        Geocoder gc = new Geocoder(mContext, Locale.getDefault());
        try {
            List<Address> locationList = gc.getFromLocation(latitude, longitude, 1);

            if (locationList != null) {
                Address address = locationList.get(0);
                // 国家
                String countryName = address.getCountryName();
                String countryCode = address.getCountryCode();
                // 省
                String adminArea = address.getAdminArea();
                // 市
                String locality = address.getLocality();
                // 区
                String subLocality = address.getSubLocality();
                // 街道
                String featureName = address.getFeatureName();

                for (int i = 0; address.getAddressLine(i) != null; i++) {
                    String addressLine = address.getAddressLine(i);
                    // 街道名称:广东省深圳市南山区木屋商学院TCL高科技工业园
                    LogUtils.i("addressLine=====" + addressLine);
                }
                if (addressCallback != null) {
                    addressCallback.onGetAddress(address);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeLocationUpdatesListener() {
        if (locationManager != null) {
            uniqueInstance = null;
            locationManager.removeUpdates(locationListener);
        }
    }

    private LocationListener locationListener = new LocationListener() {
        // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
        @Override
        public void onStatusChanged(String provider, int status, Bundle arg2) {
        }

        // Provider被enable时触发此函数，比如GPS被打开
        @Override
        public void onProviderEnabled(String provider) {
        }

        // Provider被disable时触发此函数，比如GPS被关闭
        @Override
        public void onProviderDisabled(String provider) {
        }

        // 当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
        @Override
        public void onLocationChanged(Location loc) {
            LogUtils.i("==onLocationChanged==");
        }
    };

    private void getLngAndLatWithNetwork() {
        // 添加用户权限申请判断
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, locationListener);
        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        showLocation();
    }

    public interface AddressCallback {
        /**
         * 获取详细地址
         *
         * @param address
         */
        void onGetAddress(Address address);

        /**
         * 获取经纬度
         *
         * @param lat
         * @param lng
         */
        void onGetLocation(double lat, double lng);
    }
}

