package com.inmoglass.launcher.util;

import static android.content.Intent.ACTION_BATTERY_CHANGED;
import static com.inmoglass.launcher.global.AppGlobals.GLASS_CONNECTED_INMOLENS_APP;
import static com.inmoglass.launcher.global.AppGlobals.GLASS_CONNECTING_INMOLENS_APP;
import static com.inmoglass.launcher.util.PhoneUtils.AG_CALL_CHANGED;
import static com.inmoglass.launcher.util.PhoneUtils.EXTRA_CALL;
import static com.inmoglass.launcher.util.PhoneUtils.KEY_CALL_STATE;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.google.gson.Gson;
import com.inmo.inmodata.AbstractInfo;
import com.inmo.inmodata.contacts.Contacts;
import com.inmo.inmodata.contacts.ContactsInfo;
import com.inmo.inmodata.device.BatteryInfo;
import com.inmo.inmodata.device.BondedInfo;
import com.inmo.inmodata.device.BrightnessInfo;
import com.inmo.inmodata.device.CalenderEventInfo;
import com.inmo.inmodata.device.CalenderEvents;
import com.inmo.inmodata.device.DateTimeInfo;
import com.inmo.inmodata.device.FileTransferInfo;
import com.inmo.inmodata.device.LeBoCommandInfo;
import com.inmo.inmodata.device.VolumeInfo;
import com.inmo.inmodata.device.WifiSSIDInfo;
import com.inmo.inmodata.message.Dispatcher;
import com.inmo.inmodata.notify.NotifyInfo;
import com.inmo.inmodata.weather.WeatherInfo;
import com.inmoglass.launcher.R;
import com.inmoglass.launcher.base.BaseApplication;
import com.inmoglass.launcher.bean.BluttohPhoneBean;
import com.inmoglass.launcher.global.AppGlobals;
import com.inmoglass.launcher.lecast.LeCastController;
import com.tencent.mmkv.MMKV;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

/**
 * @author Administrator
 * 专门用于处理眼镜端和手机端连接时产生的数据交互
 */
public class BluetoothController {
    private static final String TAG = BluetoothController.class.getSimpleName();
    private static volatile BluetoothController bluetoothController;
    private BluetoothSPP bt;

    private Context context;

    private String phoneMac;
    private String phoneName;
    private Gson gson;

    private boolean naviStarted = false;
    private int lastLevel;

    /**
     * 消息通知
     */
    private static final int NOTIFICATION_ID = 1;
    NotificationManager notificationManager;
    String CHANNEL_ID = "channel_id";

    /**
     * 监听眼镜端音量亮度变化
     */
    private static final String VOLUME_CHANGE = "android.media.VOLUME_CHANGED_ACTION";
    private static final String BRIGHTNESS_CHANGE = "android.screen.DISPLAY_CHANGED_ACTION";
    private AudioManager audioManager;

    private BluetoothController() {
    }

    public static BluetoothController getInstance() {
        if (bluetoothController == null) {
            synchronized (BluetoothController.class) {
                if (bluetoothController == null) {
                    bluetoothController = new BluetoothController();
                }
            }
        }
        return bluetoothController;
    }

    public void init(Context context) {
        // 初始化对象
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "name", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // 设置广播监听
        subscribeBroadCast();

        // 初始化蓝牙传输相关
        if (bt != null) {
            return;
        }
        this.context = context;
        bt = new BluetoothSPP(context);
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

                // 设备连接成功时传输数据
                int volumeLevel = SystemVolumeUtil.getVolume(BaseApplication.mContext);
                sendMessage2Phone(volumeLevel + "", Dispatcher.VOLUME_INFO);
                int brightnessLevel = SystemBrightnessUtil.getBrightness(BaseApplication.mContext);
                sendMessage2Phone(brightnessLevel + "", Dispatcher.BRIGHTNESS_INFO);
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

        startBt();
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

    /**
     * 监听各种广播
     */
    private void subscribeBroadCast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_BATTERY_CHANGED);
        filter.addAction(AG_CALL_CHANGED);
        filter.addAction(VOLUME_CHANGE);
        filter.addAction(BRIGHTNESS_CHANGE);
        BaseApplication.mContext.registerReceiver(receiver, filter);
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
                        sendMessage2Phone(level + "", Dispatcher.BIND_BATTERY_INFO);
                    }
                    break;
                case VOLUME_CHANGE:
                    int volumeLevel = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    LogUtils.d(TAG, "volume = " + volumeLevel);
                    sendMessage2Phone(volumeLevel + "", Dispatcher.VOLUME_INFO);
                    break;
                case BRIGHTNESS_CHANGE:
                    int brightnessLevel = SystemBrightnessUtil.getBrightness(context);
                    LogUtils.d(TAG, "brightness = " + brightnessLevel);
                    sendMessage2Phone(brightnessLevel + "", Dispatcher.BRIGHTNESS_INFO);
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

    /**
     * 分发手机端发送过来的数据
     *
     * @param msg 手机端发来的消息
     */
    private void dispatchMessage(String msg) {
        LogUtils.d(TAG, msg);
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
                    String mac = localMac.getAddress();
                    LogUtils.d(TAG, "deviceMac=" + mac);
                    MMKVUtils.setString(AppGlobals.BLUETOOTH_MAC_ADDRESS, mac);

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

                    // 同步电池信息
                    lastLevel = SystemUtils.getBatteryLevel(BaseApplication.mContext);
                    sendMessage2Phone(lastLevel + "", Dispatcher.BIND_BATTERY_INFO);

//                    checkBondedDeviceInfo(phoneMac);
//                    if (checkBondedDeviceInfo(phoneMac)) {
//                        LogUtils.d(TAG, "此眼镜已经被绑定");
//                    } else {

//                    }
                } else if (Dispatcher.BIND_DEVICE_STATE_BONDED.equals(state)) {
                    // 保存绑定的设备信息
                    String bondedDeviceInfo = new Gson().toJson(bondedInfo);
                    LogUtils.d(TAG, "bond device msg = " + bondedDeviceInfo);
                    MMKVUtils.setString(AppGlobals.BOND_DEVICE_INFO, bondedDeviceInfo);
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
            } else if (info instanceof BrightnessInfo) {
                // 亮度
                BrightnessInfo brightnessInfo = (BrightnessInfo) info;
                int brightness = brightnessInfo.getLevel();
                int realBrightness = (255 * brightness) / 100;
                SystemBrightnessUtil.setBrightness(BaseApplication.mContext, realBrightness);
            } else if (info instanceof VolumeInfo) {
                // 声音
                VolumeInfo volumeInfo = (VolumeInfo) info;
                int volume = volumeInfo.getLevel();
                int realVolume = (volume * 15) / 100;
                SystemVolumeUtil.setVolume(BaseApplication.mContext, realVolume);
            } else if (info instanceof CalenderEventInfo) {
                // 手机日历数据
                CalenderEventInfo calenderEventInfo = (CalenderEventInfo) info;
                if (calenderEventInfo == null) {
                    return;
                }
                ThreadUtils.getCachedPool().execute(() -> {
                    List<CalenderEvents> result = calenderEventInfo.getCalenderEvents();
                    if (result != null && !result.isEmpty()) {
                        for (int i = 0; i < result.size(); i++) {
                            ContentProviderUtils.insertMemoRecords(result.get(i));
                        }
                    }
                });
            } else if (info instanceof WifiSSIDInfo) {
                WifiSSIDInfo wifiInfo = (WifiSSIDInfo) info;
                boolean isApOpen = wifiInfo.isApOpen();
                String phoneConnectionWifiName = wifiInfo.getSsid();
                MMKVUtils.setBoolean(AppGlobals.IS_PHONE_AP_OPEN, isApOpen);
                MMKVUtils.setString(AppGlobals.PHONE_WIFI_NAME, phoneConnectionWifiName);
                LogUtils.d(TAG, "手机端是否打开热点=" + isApOpen + ",手机端连接的wifi=" + phoneConnectionWifiName);
            } else if (info instanceof LeBoCommandInfo) {
                LeBoCommandInfo leboCommandInfo = (LeBoCommandInfo) info;
                String command = leboCommandInfo.getCommand();
                LogUtils.d(TAG, "手机端发送的指令是 " + command);
                if (command.equals("startMirror")) {
                    LeCastController.startCastServer(context);
                } else if (command.equals("stopMirror")) {
                    // 收到断开指令之后,30S之后断开
                    LogUtils.d(TAG, "断开连接");
                    LeCastController.stopCastServer();
                } else if (AppGlobals.UNBIND_DEVICE_COMMAND.equals(command)) {
                    // 设备解绑
                    LogUtils.d(TAG, "设备解绑");
                    MMKVUtils.removeKey(AppGlobals.BOND_DEVICE_INFO);
                }
            }
        }
    }

    /**
     * 查询想要绑定的手机设备的mac是否是之前的设备，如果是，允许连接，如果不是，不允许连接
     *
     * @param phoneMac 想要绑定的手机的mac
     * @return
     */
    private boolean checkBondedDeviceInfo(String phoneMac) {
        LogUtils.d(TAG, "想要绑定的手机的mac=" + phoneMac);
        boolean isSameDevice = false;
        String localBindDeviceInfo = MMKVUtils.getString(AppGlobals.BOND_DEVICE_INFO);
        LogUtils.d(TAG, "localBindDeviceInfo=" + localBindDeviceInfo);
        AbstractInfo info = Dispatcher.get(localBindDeviceInfo);
        if (info instanceof BondedInfo) {
            BondedInfo bondedInfo = (BondedInfo) info;
            String localMac = bondedInfo.getLocal().getAddress();
            LogUtils.d(TAG, "local device mac = " + localMac);
            isSameDevice = localMac.equals(phoneMac);
        }
        LogUtils.d(TAG, "checkBondedDeviceInfo= " + isSameDevice);
        return isSameDevice;
    }

    /**
     * 发数据管理
     *
     * @param message 发送的信息
     */
    public void sendMessage2Phone(String message, String msgType) {
        if (bt != null && bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
            switch (msgType) {
                case Dispatcher.BIND_BATTERY_INFO:
                    LogUtils.d(TAG, "发送眼镜端电池信息=" + message);
                    BatteryInfo batteryInfo = new BatteryInfo();
                    batteryInfo.setBattery(message);
                    bt.send(getGson().toJson(batteryInfo), true);
                    break;
                case Dispatcher.VOLUME_INFO:
                    LogUtils.d(TAG, "发送眼镜端声音值=" + message);
                    VolumeInfo volumeInfo = new VolumeInfo();
                    volumeInfo.setLevel(Integer.parseInt(message));
                    bt.send(getGson().toJson(volumeInfo), true);
                    break;
                case Dispatcher.BRIGHTNESS_INFO:
                    LogUtils.d(TAG, "发送眼镜端亮度值=" + message);
                    BrightnessInfo brightnessInfo = new BrightnessInfo();
                    brightnessInfo.setLevel(Integer.parseInt(message));
                    bt.send(getGson().toJson(brightnessInfo), true);
                    break;
                case Dispatcher.LEBO_CAST_COMMAND:
                    LogUtils.d(TAG, "接收服务已成功启动，回复消息给手机端");
                    LeBoCommandInfo command = new LeBoCommandInfo();
                    command.setCommand(message);
                    bt.send(getGson().toJson(command), true);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 打开眼镜端相册
     *
     * @param instruction      指令
     * @param fileTransferInfo 数据
     */
    private void openGallery(String instruction, FileTransferInfo fileTransferInfo) {
//        LogUtils.i(TAG, "openGallery,instruction=" + instruction);
//        if (instruction.equals(GET_WIFIP2P_INFO)) {
//            return;
//        }
//        ComponentName component = new ComponentName(PACKAGE_NAME, ACTIVITY_NAME);
//        Intent intent = new Intent();
//        Intent broadcastIntent = new Intent("com.inmoglass.launcher.MyBroadCastReceiver");
//        if (instruction.equals(OPEN_GALLRY_APP)) {
//            String ssid = fileTransferInfo.getSSID();
//            String pwd = fileTransferInfo.getPWD();
//            LogUtils.i(TAG, "进入图库的编辑模式,手机端已经创建热点,SSID=" + ssid + ",PWD=" + pwd);
//            broadcastIntent.putExtra("open_edit_mode", true);
//            broadcastIntent.putExtra("create_wifip2p_group", false);
//            broadcastIntent.putExtra("SSID", ssid);
//            broadcastIntent.putExtra("PWD", pwd);
//        } else if (instruction.equals(INSTRUCTION_CREATE_WIFIP2P_GROUP)) {
//            broadcastIntent.putExtra("open_edit_mode", false);
//            broadcastIntent.putExtra("create_wifip2p_group", true);
//        }
//        sendBroadcast(broadcastIntent);
//        intent.setComponent(component);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
    }

    private Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    /**
     * 保存手机通讯录（提供蓝牙电话APP使用）
     *
     * @param info
     */
    private void savePhoneBook(AbstractInfo info) {
        List<Contacts> contacts = ((ContactsInfo) info).getContacts();
        StringBuilder sb = new StringBuilder();
        Uri uri_phonebook = Uri.parse("content://com.inmo.auth/phonebookuser");
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            cursor = resolver.query(uri_phonebook, null, null, null);
        }
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

    /**
     * 手机端通知同步到眼镜端
     * 通过notification传给系统层做展示
     *
     * @param info 消息
     */
    private void showNotification(NotifyInfo info) {
        if (info == null) {
            return;
        }
        String pkgName = info.getPackageName();
        String title = info.getTitle();
        String content = info.getContent();
        LogUtils.d("NotifyMsg=" + pkgName + ", title=" + title + ", content=" + content);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(AppUtil.getAppIconFromPkgName(pkgName))
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build());
    }

    /**
     * 跟随应用的生命周期，实际不需要调用
     */
    public void finish() {
        if (receiver != null) {
            context.unregisterReceiver(receiver);
        }
    }
}
