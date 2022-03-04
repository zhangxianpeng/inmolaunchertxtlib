package com.inmoglass.launcher.lecast;

import static com.inmoglass.launcher.global.AppGlobals.GLASS_CONNECTED_LE_CAST;
import static com.inmoglass.launcher.global.AppGlobals.GLASS_CONNECTED_THIRD_APP_VIDEO;
import static com.inmoglass.launcher.global.AppGlobals.disconnectLecastServiceTimestamp;
import static com.inmoglass.launcher.global.AppGlobals.startLecastServiceTimestamp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.blankj.utilcode.util.LogUtils;
import com.hpplay.sdk.sink.api.CastInfo;
import com.hpplay.sdk.sink.api.ClientInfo;
import com.hpplay.sdk.sink.api.IAPI;
import com.hpplay.sdk.sink.api.IMiniProgramQRListener;
import com.hpplay.sdk.sink.api.IServerListener;
import com.hpplay.sdk.sink.api.InitBean;
import com.hpplay.sdk.sink.api.LelinkCast;
import com.hpplay.sdk.sink.api.Option;
import com.hpplay.sdk.sink.api.ServerInfo;
import com.hpplay.sdk.sink.api.UploadLogBean;
import com.hpplay.sdk.sink.dmp.DeviceBean;
import com.hpplay.sdk.sink.dmp.OnDMPListener;
import com.hpplay.sdk.sink.feature.IAuthCodeCallback;
import com.hpplay.sdk.sink.feature.IFpsListener;
import com.inmo.inmodata.message.Dispatcher;
import com.inmoglass.launcher.base.BaseApplication;
import com.inmoglass.launcher.global.AppGlobals;
import com.inmoglass.launcher.util.BluetoothController;
import com.inmoglass.launcher.util.MMKVUtils;

import java.util.Map;

/**
 * @author Administrator
 * 乐播接收端
 */
public class LelinkHelper {
    private final String TAG = LelinkHelper.class.getSimpleName();

    private static final int SDK_AUTH_FAILED = 1;
    private static final int SDK_AUTH_SERVER_FAILED = 2;
    private static final int SDK_DISCONNECT = 4;

    private static LelinkHelper mLelinkHelper;
    private Context mContext;
    private LelinkCast mLelinkCast;
    private CastBean mCastBean = CastBean.getInstance();
    private ServerInfo mServerInfo = null;

    private boolean isConnected = false;

    public static boolean isLelinkServiceRunning = false;
    /**
     * 是否正在镜像中
     */
    private static boolean isMirroring = false;

    private LelinkHelper(Context context) {
        mContext = context;
        mLelinkCast = LelinkCast.getInstance();
        InitBean bean = new InitBean();
        bean.appKey = AppGlobals.APP_ID_RECEIVER;
        bean.appSecret = AppGlobals.APP_SECRET_RECEIVER;
        mLelinkCast.initSDK(context, bean);
    }

    public static boolean isIsMirroring() {
        return isMirroring;
    }

    public static LelinkHelper getInstance(Context context) {
        if (mLelinkHelper == null) {
            mLelinkHelper = new LelinkHelper(context);
        }
        return mLelinkHelper;
    }

    void startServer() {
//        if (mCastBean == null || TextUtils.isEmpty(mCastBean.currentName)) {
//            return;
//        }
//        startServer(mCastBean.currentName);
    }

    void startServer(String deviceName) {
        mAction = IDLE;
        LogUtils.i(TAG, "startServer mCurrentName: " + mCastBean.currentName + " newName: " + deviceName);
        ServerInfo info = mLelinkCast.getServerInfo();
        if (info == null || info.serviceStatus == ServerInfo.SERVER_IDLE) {// 服务未启动，则启动服务
            // 设置设备名称
            mLelinkCast.setDeviceName(deviceName);
            // 设置投屏码模式： 无密码     在服务启动前设置生效，服务启动后设置则下次生效
            if (mCastBean.authMode == IAPI.AUTH_MODE_FIXED) {
                mLelinkCast.setAuthMode(mCastBean.authMode, mCastBean.authPsd);
            } else {
                mLelinkCast.setAuthMode(mCastBean.authMode, "");
            }
            // 设置投屏码监听
            mLelinkCast.setAuthCodeCallback(mAuthCallback);
            // 设置显示帧率
            mLelinkCast.showFps(mCastBean.showMirrorFps);
            // 设置最大帧率
            mLelinkCast.setMaxFps(mCastBean.mirrorMaxFps);
            // 设置服务发布监听
            mLelinkCast.setServerListener(mServerListener);
            // 设置帧率监听
            mLelinkCast.setFpsListener(mIFpsListener);
            // 设置播放器类型
            choosePlayer(mCastBean.videoPlayerType);
            // 设置Surface类型
            setMirrorSurfaceType(mCastBean.mirrorSurfaceType);
            // 设置云播模式
            mLelinkCast.setOption(Option.LEBO_OPTION_12, Option.PERMISSION_MODE_CLOUD_LICENSE);
            mLelinkCast.setOption(Option.LEBO_OPTION_14, getSerialNumber());
            // 启动服务
            mLelinkCast.startServer();
            //测试用
            /*
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.hpplay.sdk.sink.test");
            mContext.registerReceiver(myReceiver,filter);
            */
            LogUtils.i(TAG, "从初始化状态启动");
        } else { // 服务已经启动，修改名称即可
            if (deviceName.equals(mCastBean.currentName)) {
                LogUtils.i(TAG, "相同的设备名，忽略本次启动请求");
                BluetoothController.getInstance().sendMessage2Phone("success", Dispatcher.LEBO_CAST_COMMAND);
                return;
            }
            // 修改设备名称
            mLelinkCast.changeDeviceName(deviceName);
            LogUtils.i(TAG, "以新的名字启动");
        }
        mCastBean.currentName = deviceName;
    }

    public static String getSerialNumber() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ActivityCompat.checkSelfPermission(BaseApplication.mContext, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return "";
            }
            try {
                return Build.getSerial();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Build.SERIAL;
    }

    public void stopServer() {
        mAction = IDLE;
        mLelinkCast.stopServer();
        // 防止内存泄漏
        if (disConnectHandler != null) {
            disConnectHandler = null;
        }

        if (startServiceHandler != null) {
            startServiceHandler = null;
        }
    }

    public ServerInfo getServerInfo() {
        return mLelinkCast.getServerInfo();
    }

    private static final int IDLE = 1; // 未初始化状态
    private static final int RESTARTING = 2; // 一键修复中
    private int mAction = IDLE; // Restarting

    public void restartServer() {
        mLelinkCast.stopServer();
        mLelinkCast.startServer();
        mAction = RESTARTING;
    }

    public void setMiniProgramQRListener(IMiniProgramQRListener miniProgramQRListener) {
        LogUtils.i(TAG, "setMiniProgramQRListener");
        mLelinkCast.setMiniProgramQRListener(miniProgramQRListener);
    }

    public void setPreemptMode(int preemptMode, int netType) {
        mLelinkCast.setPreemptModel(preemptMode, netType);
    }

    public void changeAuthMode(int authMode, String pwd) {
        if (mServerInfo == null || mServerInfo.serviceStatus != ServerInfo.SERVER_STARTED) {
            return;
        }
        mCastBean.authMode = authMode;
        mCastBean.authPsd = pwd;
        if (authMode == IAPI.AUTH_MODE_FIXED) {
            if (!TextUtils.isEmpty(pwd)) {
                mLelinkCast.changeAuthMode(authMode, pwd);
            }
        } else {
            mLelinkCast.changeAuthMode(authMode, null);
        }
    }

    public void showDeviceList(int netType) {
        mLelinkCast.showPreemptDeviceList(netType);
    }

    public void setShowFps(boolean show) {
        mCastBean.showMirrorFps = show;
        mLelinkCast.showFps(show);
    }

    /**
     * @param maxFps 30 60 0
     */
    public void setMaxFps(int maxFps) {
        mCastBean.mirrorMaxFps = maxFps;
        mLelinkCast.setMaxFps(maxFps);
    }

    public void choosePlayer(int type) {
        mCastBean.videoPlayerType = type;
        mLelinkCast.choosePlayer(type);
    }

    public void setMirrorSurfaceType(int surfaceType) {
        mCastBean.mirrorSurfaceType = surfaceType;
        mLelinkCast.setMirrorSurfaceType(surfaceType);
    }

    public void setMirrorSmooth(int type) {
        mCastBean.mirrorSmoothMode = type;
        mLelinkCast.setMirrorSmoothMode(type);
    }

    public void resetMirrorPlayer(int mode) {
        mCastBean.mirrorResetMode = mode;
        mLelinkCast.resetPlayerWhenMirrorRotate(mode);
    }

    public void setMultiMirrorMode(int mode) {
        mCastBean.multiMirror = mode;
        mLelinkCast.setMultiMirrorMode(mode);
    }

    public void setLelinkFpAssistant(int status) {
        mLelinkCast.setLelinkFPAssistant(status);
    }

    public void setDisplayMode(int mode) {
        mLelinkCast.setDisplayMode(mode);
    }

    private Handler mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_AUTH_FAILED:
                    Toast.makeText(mContext, "认证失败", Toast.LENGTH_SHORT).show();
                    break;
                case SDK_AUTH_SERVER_FAILED:
                case IServerListener.SDK_AUTH_SERVER_FAILED:
                    Toast.makeText(mContext, "连接认证服务器失败", Toast.LENGTH_SHORT).show();
                    break;
                case SDK_DISCONNECT:
                    Toast.makeText(mContext, "连接断开了", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            return false;
        }

    });

    /**
     * 定时器检测连接状态
     */


    private IAuthCodeCallback mAuthCallback = new IAuthCodeCallback() {

        @Override
        public void onShowAuthCode(String authCode, int expiry) {
            String msg = "onShowAuthCode" + " authCode: " + authCode + "\nexpiry: " + expiry;
            LogUtils.d(TAG, msg);
        }

        @Override
        public void onDismissAuthCode() {
            LogUtils.i(TAG, "onDismissAuthCode");
        }
    };

    private IFpsListener mIFpsListener = new IFpsListener() {
        @Override
        public void onFpsData(long[] fps) {

        }

        @Override
        public void onFps(Map<Integer, Integer> fps) {
            LogUtils.i(TAG, "mIFpsListener onFps decodeFps:" + fps.get(IFpsListener.KEY_FPS_OUT) + ", receiveFps:" + fps.get(IFpsListener.KEY_FPS_IN) + ", mirrorFps:" + fps.get(IFpsListener.KEY_FPS_MIRROR_NET) + ", sysFps:" + fps.get(IFpsListener.KEY_FPS_SYS_NET));
        }

        @Override
        public void onNetDelay(int netDelay) {
            LogUtils.i(TAG, "mIFpsListener netDelay:" + netDelay);
        }
    };

    private IServerListener mServerListener = new IServerListener() {
        @Override
        public void onStart(int id, ServerInfo info) {
            LogUtils.d(TAG, "接收服务启动成功");
            mServerInfo = mLelinkCast.getServerInfo();
            mCastBean.currentName = info.deviceName;
            isLelinkServiceRunning = true;
            String deviceName = "onStart service: " + id + " deviceName: " + info.deviceName + "\ncurrentBUVersion: " + com.hpplay.sdk.sink.util.BuildConfig.sBUVersion;
            BluetoothController.getInstance().sendMessage2Phone("success", Dispatcher.LEBO_CAST_COMMAND);
            startLecastServiceTimestamp = System.currentTimeMillis();
            if (startServiceHandler != null) {
                startServiceHandler.sendEmptyMessageDelayed(0, 5000);
            }
            LogUtils.i(TAG, deviceName);
        }

        @Override
        public void onStop(int id) {
            String info = "onStop service: " + id;
            LogUtils.i(TAG, "onstop() =  " + info);
            isLelinkServiceRunning = false;
            if (mAction == RESTARTING) {
                mAction = IDLE;
                startServer(mCastBean.currentName);
            } else {
                mServerInfo = null;
            }
        }

        @Override
        public void onError(int id, int what, int extra) {
            String info = "onError service: " + id + " what: " + what + " extra: " + extra;
            LogUtils.i(TAG, "onError() =  " + info);
            BluetoothController.getInstance().sendMessage2Phone("error", Dispatcher.LEBO_CAST_COMMAND);
            mServerInfo = null;
        }

        @Override
        public void onAuthSDK(int id, int status) {
            LogUtils.i(TAG, "onAuthSDK status: " + status);
            switch (status) {
                case IServerListener.SDK_AUTH_SUCCESS:
                    LogUtils.i(TAG, "SDK_AUTH_SUCCESS");
                    break;
                case IServerListener.SDK_AUTH_FAILED:
                    mHandler.sendEmptyMessage(SDK_AUTH_FAILED);
                    break;
                case IServerListener.SDK_AUTH_SERVER_FAILED:
                    mHandler.sendEmptyMessage(SDK_AUTH_SERVER_FAILED);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onCast(int id, CastInfo info) {
            isConnected = true;
            isMirroring = info.infoType == CastInfo.TYPE_START && info.castType == IAPI.CASTTYPE_MIRROR;

            String msg = "onCast service: " + id
                    + "\ninfoType: " + info.infoType
                    + "\nkey: " + info.key
                    + "\nurl: " + info.url
                    + "\ncastType: " + info.castType
                    + "\nmimetype: " + info.mimeType
                    + "\nprotocol: " + info.protocol
                    + "\nstartPosition: " + info.startPosition;

            int castType = info.castType;
            if (castType == 1) { // 实测B站推送视频时这个值为1
                MMKVUtils.setBoolean(GLASS_CONNECTED_THIRD_APP_VIDEO, true);
            } else if (castType == 2) { // 镜像时这个值为2
                MMKVUtils.setBoolean(GLASS_CONNECTED_LE_CAST, true);
            }

            LogUtils.i(TAG, msg);
        }

        @Override
        public void onAuthConnect(int id, String authCode, int expiry) {

        }

        @Override
        public void onConnect(int id, ClientInfo info) {
            String msg = "onConnect name " + info.name + "  id: " + info.clientID;
            isConnected = true;
            LogUtils.i(TAG, msg);
        }

        @Override
        public void onDisconnect(int i, ClientInfo clientInfo) {
            isConnected = false;
            disconnectLecastServiceTimestamp = System.currentTimeMillis();
            disConnectHandler.sendEmptyMessageDelayed(0, 5000);
            mHandler.sendEmptyMessage(SDK_DISCONNECT);
        }
    };

    private Handler disConnectHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            LogUtils.d(TAG, "检测断开连接的连接状态，" + System.currentTimeMillis());
            if (disConnectHandler != null) {
                disConnectHandler.sendEmptyMessageDelayed(0, 5000);
            }
            if (System.currentTimeMillis() - disconnectLecastServiceTimestamp > 30000 && !isConnected) {
                LogUtils.d(TAG, "断开连接后30S未被重新连接，停止服务");
                stopServer();
            }
            return false;
        }
    });

    private Handler startServiceHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            LogUtils.d(TAG, "检测开始服务的连接状态，" + System.currentTimeMillis());
            if (startServiceHandler != null) {
                startServiceHandler.sendEmptyMessageDelayed(0, 5000);
            }
            if (System.currentTimeMillis() - startLecastServiceTimestamp > 30000 && !isConnected) {
                LogUtils.d(TAG, "开始服务后30S未连接，停止服务");
                stopServer();
            }
            return false;
        }
    });

    public void startDMP() {
        mLelinkCast.startDMPServer();
    }

    public void stopDMP() {
        mLelinkCast.stopDMPServer();
    }

    public void searchDMP() {
        mLelinkCast.searchDMPDevices();
    }

    public void browseDevice(DeviceBean deviceBean) {
        mLelinkCast.browseDMPDeviceDir(deviceBean);
    }

    public void browseFolder(String actionUrl, String folderId) {
        mLelinkCast.browseDMPFolder(actionUrl, folderId);
    }

    public void setDMPListener(OnDMPListener dmpListener) {
        mLelinkCast.setDMPListener(dmpListener);
    }

    public void uploadLog(UploadLogBean logBean) {
        mLelinkCast.uploadLog(logBean);
    }

//    public String getLogUrl() {
//        if (mServerInfo == null) {
//            return null;
//        }
//        return "http://" + Utils.getIP(mContext) + ":" + mServerInfo.remotePort + "/log";
//    }

    public int setOption(int option, Object... values) {
        Object result = mLelinkCast.setOption(option, values);
        int callResult = Integer.parseInt(result.toString());
        if (callResult == IAPI.INVALID_CALL) {
            LogUtils.w(TAG, "setOption invalid call, option: " + option);
        }
        return callResult;
    }

    public int performAction(int action, Object... values) {
        Object result = mLelinkCast.performAction(action, values);
        int callResult = Integer.parseInt(result.toString());
        if (callResult == IAPI.INVALID_CALL) {
            LogUtils.w(TAG, "performAction invalid call, action: " + action);
        }
        return callResult;
    }

    public <T> T getOption(int option, Class<T> classOfT) {
        if (mLelinkCast != null) {
            Object object = mLelinkCast.getOption(option);
            try {
                return classOfT.cast(object);
            } catch (Exception e) {
                LogUtils.w(TAG, e);
            }
        }
        return null;
    }

//测试用
//private MyReceiver myReceiver = new MyReceiver();

    public class MyReceiver extends BroadcastReceiver {
        public MyReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtils.i(TAG, "onReceive action: " + action);
            int t = intent.getIntExtra("value", 0);
            LogUtils.i(TAG, "onReceive value: " + t);
            //有些api接口测试，需要在播放界面时候调用，可以通过shell发广播形式
            //adb shell am broadcast -a com.hpplay.sdk.sink.test --es test_string "this is test string" --ei test_int 100 --ez test_boolean true
            //setDisplayMode(t);
        }
    }
}