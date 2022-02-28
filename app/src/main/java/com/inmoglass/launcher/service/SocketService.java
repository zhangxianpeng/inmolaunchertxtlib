package com.inmoglass.launcher.service;

import static com.inmoglass.launcher.global.AppGlobals.GLASS_CONNECTED_INMOLENS_APP;
import static com.inmoglass.launcher.global.AppGlobals.GLASS_CONNECTING_INMOLENS_APP;
import static com.inmoglass.launcher.util.PhoneUtils.AG_CALL_CHANGED;
import static com.inmoglass.launcher.util.PhoneUtils.EXTRA_CALL;
import static com.inmoglass.launcher.util.PhoneUtils.KEY_CALL_STATE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.blankj.utilcode.util.LogUtils;
import com.google.gson.Gson;
import com.inmo.inmodata.AbstractInfo;
import com.inmo.inmodata.contacts.Contacts;
import com.inmo.inmodata.contacts.ContactsInfo;
import com.inmo.inmodata.device.BatteryInfo;
import com.inmo.inmodata.device.BondedInfo;
import com.inmo.inmodata.device.DateTimeInfo;
import com.inmo.inmodata.device.FileTransferInfo;
import com.inmo.inmodata.message.Dispatcher;
import com.inmo.inmodata.notify.NotifyInfo;
import com.inmo.inmodata.weather.WeatherInfo;
import com.inmoglass.launcher.R;
import com.inmoglass.launcher.base.BaseApplication;
import com.inmoglass.launcher.bean.BluttohPhoneBean;
import com.inmoglass.launcher.util.AppUtil;
import com.inmoglass.launcher.util.BleCallManager;
import com.inmoglass.launcher.util.ContentProviderUtils;
import com.inmoglass.launcher.util.MMKVUtils;
import com.inmoglass.launcher.util.MobileNumberUtils;
import com.inmoglass.launcher.util.SystemUtils;
import com.tencent.mmkv.MMKV;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

/**
 * @author Administrator
 */
public class SocketService extends Service {
    private static final String TAG = SocketService.class.getSimpleName();
    public static final String CHANNEL_ID_STRING = "socket_server";
    public static final String CHANNEL_ID_NAME = "socket_server";

    private BluetoothSPP bt;
    private String phoneMac;
    private String phoneName;
    private Gson gson;

    private boolean naviStarted = false;
    private int lastLevel;

    private static final String PACKAGE_NAME = "com.yulong.coolgallery";
    private static final String ACTIVITY_NAME = "com.yulong.coolgallery.PreviewActivity2";
    /**
     * 与手机通信的指令
     */
    private static final String INSTRUCTION_CREATE_WIFIP2P_GROUP = "instruction_create_group";
    private static final String OPEN_GALLRY_APP = "open_media";
    private static final String GET_WIFIP2P_INFO = "get_wifip2p_info";

    private static final int NOTIFICATION_ID = 1;
    NotificationManager notificationManager;
    String CHANNEL_ID = "channel_id";

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.i(TAG, "SocketService oncreate");
        startForeground();
        initBt();

        // 监听电量变化
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        intentFilter.addAction(AG_CALL_CHANGED);
        registerReceiver(receiver, intentFilter);

        notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "name", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case AG_CALL_CHANGED:
                    // 蓝牙电话监听
                    Object obj = intent.getParcelableExtra(EXTRA_CALL);
                    String phoneNumber = BleCallManager.getInstance().getNumber(obj);
                    String numberGeo = TextUtils.isEmpty(phoneNumber) ? "" : MobileNumberUtils.getGeo(phoneNumber);
                    LogUtils.d(TAG, "EXTRA_CALL: " + obj.toString());
                    LogUtils.d(TAG, "phoneNumber=" + BleCallManager.getInstance().getNumber(obj) + ",geo=" + numberGeo);
                    if (obj != null) {
                        String callState = parseCallStates(obj.toString()).trim();
                        if (!TextUtils.isEmpty(callState)) {
                            LogUtils.d(TAG, "callState: " + callState);
                            NotifyInfo blePhoneNotify = new NotifyInfo();
                            blePhoneNotify.setPackageName("com.android.phone");
                            blePhoneNotify.setTitle(phoneNumber + " " + numberGeo);
                            blePhoneNotify.setContent(BaseApplication.mContext.getString(R.string.string_new_call_msg));
                            showNotification(blePhoneNotify);
                        }
                    }
                    break;
                case Intent.ACTION_BATTERY_CHANGED:
                    int level = (intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100) / intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    if (level != lastLevel) {
                        lastLevel = level;
                        sendBatteryLevel(level);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 获取蓝牙电话状态
     *
     * @param callInfo
     * @return
     */
    private String parseCallStates(String callInfo) {
        String kvs[] = callInfo.split(",");
        String callState = "";
        String state = "";
        if (kvs.length != 0) {
            for (int i = 0; i < kvs.length; i++) {
                if (kvs[i].contains(KEY_CALL_STATE)) {
                    callState = kvs[i];
                    break;
                }
            }
            if (!TextUtils.isEmpty(":")) {
                int index = callState.indexOf(":");
                state = callState.substring(index + 1, callState.length());
                Log.i(TAG, "call state: " + state);
            }
        }
        return state;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.i(TAG, "onStartCommand");
        startBt();
        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        LogUtils.e(TAG, "socket service is stop!!");
        Intent intent = new Intent(this, SocketService.class);
        startService(intent);
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    private void startBt() {
        if (!bt.isBluetoothEnabled()) {
        } else {
            if (!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
            }
        }
    }

    private void initBt() {
        if (bt == null) {
            bt = new BluetoothSPP(this);
            bt.isBluetoothAvailable();

            bt.setBluetoothStateListener(state -> {
                if (state == BluetoothState.STATE_CONNECTED) {
                    LogUtils.i(TAG, "State : Connected");
                } else if (state == BluetoothState.STATE_CONNECTING) {
                    LogUtils.i(TAG, "State : Connecting");
                } else if (state == BluetoothState.STATE_LISTEN) {
                    LogUtils.i(TAG, "State : Listen");
                } else if (state == BluetoothState.STATE_NONE) {
                    LogUtils.i(TAG, "State : None");
                }
            });

            bt.setOnDataReceivedListener((data, message) -> {
                LogUtils.i(TAG, "Message : " + message);
                dispatchMessage(message);
            });

            bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
                @Override
                public void onDeviceConnected(String name, String address) {
                    LogUtils.i(TAG, "Device Connected!!");
                    // 获取到手机的设备信息
                    phoneName = name;
                    phoneMac = address;
                    MMKVUtils.setBoolean(GLASS_CONNECTING_INMOLENS_APP, true);
                }

                @Override
                public void onDeviceDisconnected() {
                    LogUtils.i(TAG, "Device Disconnected!!");
                    MMKVUtils.setBoolean(GLASS_CONNECTING_INMOLENS_APP, false);
                    if (naviStarted) {
                        naviStarted = false;
                    }
                }

                @Override
                public void onDeviceConnectionFailed() {
                    LogUtils.i(TAG, "Unable to Connected!!");
                    MMKVUtils.setBoolean(GLASS_CONNECTING_INMOLENS_APP, false);
                }
            });
        }
    }

    public void sendBatteryLevel(int level) {
        if (bt != null && bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
            BatteryInfo batteryInfo = new BatteryInfo();
            batteryInfo.setBattery(level + "");
            bt.send(getGson().toJson(batteryInfo), true);
        }
    }

    private void dispatchMessage(String msg) {
        // 保存眼镜跟手机端APP连接过的标志
        if (msg.contains(GLASS_CONNECTED_INMOLENS_APP)) {
            String[] connectedFlag = msg.split("=");
            boolean isConnectedPhone = Boolean.parseBoolean(connectedFlag[1]);
            MMKVUtils.setBoolean(GLASS_CONNECTED_INMOLENS_APP, isConnectedPhone);
        }
        AbstractInfo info = Dispatcher.get(msg);
        if (info != null) {
            if (info instanceof BondedInfo) {
                // 绑定设备
                BondedInfo bondedInfo = (BondedInfo) info;
                String state = bondedInfo.getState();
                if (Dispatcher.BIND_DEVICE_STATE_BINDING.equals(state)) {
                    BondedInfo.DeviceInfo localMac = bondedInfo.getLocal();
                    BondedInfo.DeviceInfo remoteMac = new BondedInfo.DeviceInfo();
                    if (!TextUtils.isEmpty(phoneMac)) {
                        remoteMac.setAddress(phoneMac);
                    }
                    if (!TextUtils.isEmpty(phoneName)) {
                        remoteMac.setName(phoneName);
                    }
                    BondedInfo sendInfo = new BondedInfo(remoteMac, localMac);
                    sendInfo.setState(Dispatcher.BIND_DEVICE_STATE_BONDED);
                    bt.send(getGson().toJson(sendInfo), true);
                    BatteryInfo batteryInfo = new BatteryInfo();
                    lastLevel = SystemUtils.getBatteryLevel(getApplicationContext());
                    batteryInfo.setBattery(lastLevel + "");
                    bt.send(getGson().toJson(batteryInfo), true);
                }
            } else if (info instanceof ContactsInfo) {
                // 同步通讯录
                LogUtils.i(TAG, "ContactsInfo: " + info.toString());
                savePhoneBook(info);
            } else if (info instanceof NotifyInfo) {
                // 手机的通知
                NotifyInfo notifyInfo = (NotifyInfo) info;
                LogUtils.i(TAG, "NotifyInfo: " + notifyInfo.toString());
                showNotification(notifyInfo);
            } else if (info instanceof WeatherInfo) {
                // 天气
                WeatherInfo weatherInfo = (WeatherInfo) info;
                LogUtils.i(TAG, "WeatherInfo: " + weatherInfo.toString());
                EventBus.getDefault().post(weatherInfo);
            } else if (info instanceof DateTimeInfo) {
                // 时间
                DateTimeInfo dateTimeInfo = (DateTimeInfo) info;
                EventBus.getDefault().post(dateTimeInfo);
            } else if (info instanceof FileTransferInfo) {
                // 文件传输相关
                FileTransferInfo fileTransferInfo = (FileTransferInfo) info;
                String instruction = fileTransferInfo.getInstruction();
                openGallery(instruction, fileTransferInfo);
            } else if (info instanceof DateTimeInfo) {
                // TODO: 2022/2/23 手机日历信息同步到眼镜
                new Thread(() -> {
                    // 往眼镜端的备忘录APP存数据
                }).start();
            }
        }
    }

    /**
     * @param info
     */
    private void showNotification(NotifyInfo info) {
        if (info == null) {
            return;
        }
        String pkgName = info.getPackageName();
        String title = info.getTitle();
        String content = info.getContent();
        LogUtils.d("NotifyMsg=" + pkgName + ", title=" + title + ", content=" + content);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(AppUtil.getAppIconFromPkgName(pkgName))
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build());
    }

    /**
     * 打开媒体管理
     *
     * @param instruction 伴随操作
     */
    private void openGallery(String instruction, FileTransferInfo fileTransferInfo) {
        LogUtils.i(TAG, "openGallery,instruction=" + instruction);
        if (instruction.equals(GET_WIFIP2P_INFO)) {
            return;
        }
        ComponentName component = new ComponentName(PACKAGE_NAME, ACTIVITY_NAME);
        Intent intent = new Intent();
        Intent broadcastIntent = new Intent("com.inmoglass.launcher.MyBroadCastReceiver");
        if (instruction.equals(OPEN_GALLRY_APP)) {
            String ssid = fileTransferInfo.getSSID();
            String pwd = fileTransferInfo.getPWD();
            LogUtils.i(TAG, "进入图库的编辑模式,手机端已经创建热点,SSID=" + ssid + ",PWD=" + pwd);
            broadcastIntent.putExtra("open_edit_mode", true);
            broadcastIntent.putExtra("create_wifip2p_group", false);
            broadcastIntent.putExtra("SSID", ssid);
            broadcastIntent.putExtra("PWD", pwd);
        } else if (instruction.equals(INSTRUCTION_CREATE_WIFIP2P_GROUP)) {
            broadcastIntent.putExtra("open_edit_mode", false);
            broadcastIntent.putExtra("create_wifip2p_group", true);
        }
        sendBroadcast(broadcastIntent);
        intent.setComponent(component);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void savePhoneBook(AbstractInfo info) {
        List<Contacts> contacts = ((ContactsInfo) info).getContacts();
        StringBuilder sb = new StringBuilder();
        Uri uri_phonebook = Uri.parse("content://com.inmo.auth/phonebookuser");
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(uri_phonebook, null, null, null);
        if (cursor == null) {
            return;
        }
        List<BluttohPhoneBean> bluttohPhoneBeanList = new ArrayList<>();
        List<BluttohPhoneBean> passBeanList = new ArrayList<>();
        while (cursor.moveToNext()) {
            // 把数据取出来
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String phone = cursor.getString(cursor.getColumnIndex("number"));
            LogUtils.d(TAG, "name" + cursor.getString(cursor.getColumnIndex("name")));
            BluttohPhoneBean bluttohPhoneBean = new BluttohPhoneBean();
            bluttohPhoneBean.setPhone(phone);
            bluttohPhoneBean.setName(name);
            bluttohPhoneBeanList.add(bluttohPhoneBean);
        }
        LogUtils.d(TAG, "bluttohPhoneBeanList" + bluttohPhoneBeanList.toString());
        for (int i = 0; i < bluttohPhoneBeanList.size(); i++) {
            LogUtils.d(TAG, "nameis" + bluttohPhoneBeanList.get(i).getName());
            resolver.delete(uri_phonebook, "name" + "='" + bluttohPhoneBeanList.get(i).getName() + "'", null);
        }
        LogUtils.d(TAG, "datashuju" + bluttohPhoneBeanList.size());
        for (int i = 0; i < contacts.size(); i++) {
            ContentValues values = new ContentValues();
            values.put(Dispatcher.PHONEBOOK_NAME, contacts.get(i).getName());
            values.put(Dispatcher.PHONEBOOK_NUMBER, contacts.get(i).getPhone());

            sb.append(contacts.get(i).getName());
            if (i < (contacts.size() - 1)) {
                sb.append("|");
            }

            if (TextUtils.isEmpty(contacts.get(i).getNote())) {
                values.put(Dispatcher.PHONEBOOK_NOTE, "");
            } else {
                values.put(Dispatcher.PHONEBOOK_NOTE, contacts.get(i).getNote());
            }
            resolver.insert(uri_phonebook, values);
        }
        MMKV.defaultMMKV().putBoolean("updatephonebook", true);
        MMKV.defaultMMKV().putString("localphonebook", sb.toString());
        LogUtils.i(TAG, "savePhoneBook sb : " + sb.toString());
    }

    private Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class MyBinder extends Binder {
        public SocketService getService() {
            return SocketService.this;
        }
    }

    private void startForeground() {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager = (NotificationManager) getSystemService(ns);
        NotificationChannel mChannel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(CHANNEL_ID_STRING, CHANNEL_ID_NAME,
                    NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(mChannel);
            Notification notification = new Notification.Builder(getApplicationContext(), CHANNEL_ID_STRING).build();
            startForeground(NOTIFICATION_ID, notification);
        }
    }
}
