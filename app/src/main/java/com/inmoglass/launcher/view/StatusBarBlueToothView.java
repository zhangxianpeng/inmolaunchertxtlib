package com.inmoglass.launcher.view;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.inmoglass.launcher.R;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

/**
 * Created by Administrator on 2016/10/8 0008.
 */
public class StatusBarBlueToothView extends androidx.appcompat.widget.AppCompatImageView {
    private static final String TAG = StatusBarBlueToothView.class.getSimpleName();
    private BluetoothHandler bluetoothHandler;

    private static final int MSG_BLETOOTH = 0X01;

    public StatusBarBlueToothView(Context context) {
        this(context, null);
    }

    public StatusBarBlueToothView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i("ljwtest:", "StatusBarBlueToothView被新建了");
        bluetoothHandler = new BluetoothHandler(this);
        init();
    }

    public void update() {
        init();
    }

    private void init() {
        int state = BluetoothAdapter.getDefaultAdapter().getState();
        if (state == BluetoothAdapter.STATE_ON) {
            setVisibility(View.VISIBLE);
            this.setImageResource(isConnected() ? R.drawable.ic_baseline_bluetooth_connected_24 : R.drawable.ic_baseline_bluetooth_24);
        } else if (state == BluetoothAdapter.STATE_OFF) {
            setVisibility(View.GONE);
        }
    }

    private void setImageState(int state) {
        Log.i(TAG, "setImageState: " + state);
        //只管需要的状态
        if (state == BluetoothAdapter.STATE_ON) {
            setVisibility(View.VISIBLE);
            setImageResource(R.drawable.ic_baseline_bluetooth_24);
        } else if (state == BluetoothAdapter.STATE_OFF) {
            setVisibility(View.GONE);
        } else if (state == BluetoothAdapter.STATE_CONNECTED) {
            setVisibility(View.VISIBLE);
            setImageResource(R.drawable.ic_baseline_bluetooth_connected_24);
        } else if (state == BluetoothAdapter.STATE_DISCONNECTED) {
            setVisibility(View.VISIBLE);
            setImageResource(R.drawable.ic_baseline_bluetooth_24);
        } else if (state == BluetoothAdapter.STATE_CONNECTING) {
            //动画
        }
    }

    private boolean isConnected() {
//        int a2dp = BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(BluetoothProfile.A2DP);
//        int headset = BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(BluetoothProfile.HEADSET);
//        int health = BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(BluetoothProfile.HEALTH);
//        int gatt = BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(BluetoothProfile.GATT);
//        int avrcp = BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(13);
//        Log.i(TAG, "getProfileConnectionState a2dp: " + a2dp +
//                ", headset: " + headset + ", health: " + health + ", gatt: " + gatt
//        + ", avrcp: " + avrcp);
//        Set<BluetoothDevice> list = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
//        if(list != null && !list.isEmpty()) {
//            Iterator<BluetoothDevice> it = list.iterator();
//            while(it.hasNext()) {
//                BluetoothDevice device = it.next();
//                device.get
//            }
//        }
        Class<BluetoothAdapter> bluetoothAdapterClass = BluetoothAdapter.class;
        try {
            Method method = bluetoothAdapterClass.getDeclaredMethod("getConnectionState", (Class[]) null);
            method.setAccessible(true);
            int state = (int) method.invoke(BluetoothAdapter.getDefaultAdapter(), (Object[]) null);
            if (state == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected");
                return true;
            } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static class BluetoothHandler extends Handler {
        WeakReference<StatusBarBlueToothView> mView;

        public BluetoothHandler(StatusBarBlueToothView view) {
            mView = new WeakReference<StatusBarBlueToothView>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (mView.get() == null) {
                return;
            }
            StatusBarBlueToothView view = mView.get();
            switch (msg.what) {
                case MSG_BLETOOTH:
                    view.setImageState(msg.arg1);
                    break;
            }
//            view.setImageResource(msg.what == STATE_ON ? R.mipmap.bluetooth_on : R.mipmap.bluetooth_off);
        }
    }

    private BroadcastReceiver bluetoothReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                Log.i(TAG, "BluetoothAdapter.ACTION_STATE_CHANGED");
                int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
//                switch (blueState) {
//                    case BluetoothAdapter.STATE_ON:
//
//                        break;
//                    case BluetoothAdapter.STATE_OFF:
//                        break;
//                }
                Message.obtain(bluetoothHandler, MSG_BLETOOTH, blueState, 0).sendToTarget();
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Log.i(TAG, "蓝牙设备已连接");
                Message.obtain(bluetoothHandler, MSG_BLETOOTH,
                        BluetoothAdapter.STATE_CONNECTED, 0).sendToTarget();

            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {

                Message.obtain(bluetoothHandler, MSG_BLETOOTH,
                        BluetoothAdapter.STATE_DISCONNECTED, 0).sendToTarget();
                Log.i(TAG, "蓝牙设备已断开");
            } else if (action.equals("android.bluetooth.device.action.SDP_RECORD")) {
                Log.i(TAG, "SDP_RECORD");
                BluetoothDevice dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                ParcelUuid uuid = intent.getParcelableExtra(BluetoothDevice.EXTRA_UUID);
                if (uuid.equals(ParcelUuid.fromString("00001132-0000-1000-8000-00805F9B34FB"))) {
                    Log.i(TAG, "SDP_RECORD BluetoothUuid.MAS");
//                    SdpMasRecord masrec =
//                            intent.getParcelableExtra(BluetoothDevice.EXTRA_SDP_RECORD);
//                    BluetoothMasClient mapclient = new BluetoothMasClient(mDevice, masrec,
//                            mMapHandler);
                }
            }
        }
    };

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction("android.bluetooth.BluetoothAdapter.STATE_OFF");
        intentFilter.addAction("android.bluetooth.BluetoothAdapter.STATE_ON");
        intentFilter.addAction(BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT);
        intentFilter.addAction("android.bluetooth.device.action.SDP_RECORD");
        getContext().registerReceiver(bluetoothReceive, intentFilter);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        bluetoothHandler.removeCallbacksAndMessages(null);
        getContext().unregisterReceiver(bluetoothReceive);
    }

}
