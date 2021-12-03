package com.inmoglass.launcher.util;

import static android.content.Context.BATTERY_SERVICE;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.LocaleList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Locale;

public class SystemUtils {
    public static String getProcessName() {
        try {
            File file = new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline");
            BufferedReader mBufferedReader = new BufferedReader(new FileReader(file));
            String processName = mBufferedReader.readLine().trim();
            mBufferedReader.close();
            return processName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int getBatteryLevel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BatteryManager batteryManager = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
            return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        } else {
            Intent intent = new ContextWrapper(context.getApplicationContext()).
                    registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            return (intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100) /
                    intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        }
    }

//    implementation 'net.vidageek:mirror:1.6.1'
    public String getBluetoothMac() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
//        BluetoothAdapter adapter = (BluetoothAdapter) getApplicationContext().getSystemService(BLUETOOTH_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return adapter.getAddress();
        } else {
//        Object bluetoothManageService = new Mirror().on(adapter).get().field("mService");
//        if (bluetoothManageService == null)
//            return null;
//        Object address = new Mirror().on(bluetoothManageService).invoke().method("getAddress").withoutArgs();
//        if (address != null && address instanceof String) {
//            return (String) address;
//        } else {
            return null;
//        }
        }
    }

    public static String getSysLanguage() {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = LocaleList.getDefault().get(0);
        } else {
            locale = Locale.getDefault();
        }
        String language = locale.getLanguage()/* + "-" + locale.getCountry()*/;
        return language;
    }
}
