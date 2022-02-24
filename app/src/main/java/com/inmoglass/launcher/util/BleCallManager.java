package com.inmoglass.launcher.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import com.inmoglass.launcher.base.BaseApplication;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

public class BleCallManager {
    private final String TAG = BleCallManager.class.getSimpleName();
    private final static String BLUETOOTH_HEADSET_CLIENT = "android.bluetooth.BluetoothHeadsetClient";
    private final static String BLUETOOTH_HEADSET_CLIENT_CALL = "android.bluetooth.BluetoothHeadsetClientCall";

    private final static String CONNECT = "connect";
    private final static String DIAL = "dial";
    private final static String ACCEPT_CALL = "acceptCall";
    private final static String REJECT_CALL = "rejectCall";
    private final static String TERMINATE_CALL = "terminateCall";
    private final static String BLUETOOTHHEADSETCLIENTCALL_TOSTRING = "toString";

    private final static int BLUETOOTH_PROFILE_HEADSET_CLIENT = 16;

    private static Class clzBluetoothHeadsetClient = null;
    private static Class clzBluetoothHeadsetClientCall = null;
    private static Method methodDial = null;
    private static Method methodConnect = null;
    private static Method methodRejectCall = null;
    private static Method methodAcceptCall = null;
    private static Method methodTerminateCall = null;
    private static Method toString = null;
    private static Method getNumber = null;

    private static BleCallManager instance;
    public OnCallResultListener onCallResultListener;

    public interface OnCallResultListener {
        //蓝牙未打开
        void onBluetoothIsClosed();

        //蓝牙未连接
        void onDeviceIsEmpty();

        //无效的电话号码
        void onPhoneIsInValid();

        void onError(String errorMsg);
    }

    public static BluetoothDevice getConnectedDevice() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        Class<BluetoothAdapter> bluetoothAdapterClass = BluetoothAdapter.class;//得到BluetoothAdapter的Class对象
        try {//得到连接状态的方法
            Method method = bluetoothAdapterClass.getDeclaredMethod("getConnectionState", (Class[]) null);
            //打开权限
            method.setAccessible(true);
            int state = (int) method.invoke(adapter, (Object[]) null);

            if(state == BluetoothAdapter.STATE_CONNECTED){
                Log.i("BLUETOOTH","BluetoothAdapter.STATE_CONNECTED");
                Set<BluetoothDevice> devices = adapter.getBondedDevices();
                Log.i("BLUETOOTH","devices:"+devices.size());

                for(BluetoothDevice device : devices){
                    Method isConnectedMethod = BluetoothDevice.class.getDeclaredMethod("isConnected", (Class[]) null);
                    method.setAccessible(true);
                    boolean isConnected = (boolean) isConnectedMethod.invoke(device, (Object[]) null);
                    if(isConnected){
                        Log.i("BLUETOOTH","connected:"+device.getName());
//                        deviceList.add(device);
                        return device;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static BleCallManager getInstance() {
        if (instance == null) {
            instance = new BleCallManager();
            try {
                clzBluetoothHeadsetClient = Class.forName(BLUETOOTH_HEADSET_CLIENT);
                clzBluetoothHeadsetClientCall = Class.forName(BLUETOOTH_HEADSET_CLIENT_CALL);

                methodConnect = clzBluetoothHeadsetClient.getMethod(CONNECT, BluetoothDevice.class);
                methodDial = clzBluetoothHeadsetClient.getMethod(DIAL, BluetoothDevice.class, String.class);
                methodRejectCall = clzBluetoothHeadsetClient.getMethod(REJECT_CALL, BluetoothDevice.class);
                methodAcceptCall = clzBluetoothHeadsetClient.getMethod(ACCEPT_CALL, BluetoothDevice.class, int.class);
                methodTerminateCall = clzBluetoothHeadsetClient.getMethod(TERMINATE_CALL, BluetoothDevice.class, clzBluetoothHeadsetClientCall);

//                toString = clzBluetoothHeadsetClientCall.getMethod(BLUETOOTHHEADSETCLIENTCALL_TOSTRING, boolean.class);
            } catch (Exception e) {

            }
        }
        return instance;
    }

    private String res;

    public String getCallString() {
        getConnectStatus(new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                if (toString == null) {
                    try {
                        try {
                            toString = clzBluetoothHeadsetClientCall.getDeclaredMethod("toString", boolean.class);
                        } catch (NoSuchMethodException e) {
                            Log.e(TAG, "can not find method: " + e.toString());
                            e.printStackTrace();
                        }
                        Method[] ms = clzBluetoothHeadsetClientCall.getMethods();
                        for (int i = 0; i < ms.length; i++) {
                            Log.i(TAG, "ClientCall methods: " + ms[i].getName());
                        }
//                        toString.setAccessible(true);
                        if (toString != null)
                            res = (String) toString.invoke(proxy);
                        Log.i(TAG, "getCallString: " + res);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
//                    catch (InstantiationException e) {
//                        Log.e(TAG, "InstantiationException: " + e.toString());
//                        e.printStackTrace();
//                    }
                }
            }

            @Override
            public void onServiceDisconnected(int profile) {

            }
        });

        return res;
    }

    public String getNumber(Object object) {
        String num = "";
        try {
            try {
                getNumber = clzBluetoothHeadsetClientCall.getMethod("getNumber");
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "can not find method: " + e.toString());
                e.printStackTrace();
            }
//                Method[] ms = clzBluetoothHeadsetClientCall.getMethods();
//                for (int i = 0; i < ms.length; i++) {
//                    Log.i(TAG, "ClientCall methods: " + ms[i].getName());
//                }
            getNumber.setAccessible(true);
            if (getNumber != null)
                num = (String) getNumber.invoke(object);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
//                    catch (InstantiationException e) {
//                        Log.e(TAG, "InstantiationException: " + e.toString());
//                        e.printStackTrace();
//                    }
        return num;
    }

    public void dial(final String number, final OnCallResultListener onCallResultListener) {
        this.onCallResultListener = onCallResultListener;

        if (number.compareToIgnoreCase("") == 0) {
            if (onCallResultListener != null) {
                onCallResultListener.onPhoneIsInValid();
            }
        }

        getConnectStatus(new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                List<BluetoothDevice> mDevices = proxy.getConnectedDevices();
                if (mDevices.size() != 0) {
                    for (BluetoothDevice device : mDevices) {
                        dial(proxy, device, number);
                        break;
                    }
                } else {
                    if (onCallResultListener != null) {
                        onCallResultListener.onDeviceIsEmpty();
                    }
                }
            }

            @Override
            public void onServiceDisconnected(int profile) {
                if (onCallResultListener != null) {
                    onCallResultListener.onDeviceIsEmpty();
                }
            }
        });
    }

    public void terminateCall(final OnCallResultListener onCallResultListener) {
        this.onCallResultListener = onCallResultListener;

        if (onCallResultListener != null) {
            onCallResultListener.onPhoneIsInValid();
        }
        getConnectStatus(new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                List<BluetoothDevice> mDevices = proxy.getConnectedDevices();

                if (mDevices.size() != 0) {
                    for (BluetoothDevice device : mDevices) {
                        terminateCall(proxy, device);

                        break;
                    }
                } else {
                    if (onCallResultListener != null) {
                        onCallResultListener.onDeviceIsEmpty();
                    }
                }
            }

            @Override
            public void onServiceDisconnected(int profile) {
                if (onCallResultListener != null) {
                    onCallResultListener.onDeviceIsEmpty();
                }
            }
        });
    }

    //获取蓝牙的连接状态
    private void getConnectStatus(BluetoothProfile.ServiceListener serviceListener) {
        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (!adapter.isEnabled()) {
                if (onCallResultListener != null) {
                    onCallResultListener.onBluetoothIsClosed();
                    return;
                }
            }

            int isConnected = adapter.getProfileConnectionState(BLUETOOTH_PROFILE_HEADSET_CLIENT);

            if (isConnected == BluetoothProfile.STATE_DISCONNECTED) {
                if (onCallResultListener != null) {
                    onCallResultListener.onDeviceIsEmpty();
                    return;
                }
            }
            adapter.getProfileProxy(BaseApplication.mContext, serviceListener, BLUETOOTH_PROFILE_HEADSET_CLIENT);

        } catch (Exception e) {
            if (onCallResultListener != null) {
                onCallResultListener.onError(e.getMessage());
            }
        }
    }

    /***
     * 拨号
     * @param proxy
     * @param bluetoothDevice
     * @param number
     */
    private void dial(BluetoothProfile proxy, BluetoothDevice bluetoothDevice, String number) {
        try {
            if (proxy == null || bluetoothDevice == null) {
                return;
            }
            if (methodDial != null) {
                methodDial.invoke(proxy, bluetoothDevice, number);
            }
        } catch (Exception e) {
            if (onCallResultListener != null) {
                onCallResultListener.onError(e.getMessage());
            }
        }
    }

    public void connect(BluetoothDevice dev) {
        if (dev == null) {
            return;
        }

        try {
            if (methodConnect != null) {
                methodConnect.invoke(dev);
            }
        } catch (Exception e) {
            if (onCallResultListener != null) {
                onCallResultListener.onError(e.getMessage());
            }
        }
    }

    public boolean rejectCall() {
        getConnectStatus(new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                List<BluetoothDevice> mDevices = proxy.getConnectedDevices();
                if (mDevices.size() != 0) {
                    for (BluetoothDevice dev : mDevices) {
                        try {
                            if (methodRejectCall != null) {
                                methodRejectCall.invoke(proxy, dev);
                            }
                        } catch (Exception e) {
                            if (onCallResultListener != null) {
                                onCallResultListener.onError(e.getMessage());
                            }
                        }
                        break;
                    }
                } else {
                    if (onCallResultListener != null) {
                        onCallResultListener.onDeviceIsEmpty();
                    }
                }
            }

            @Override
            public void onServiceDisconnected(int profile) {
                if (onCallResultListener != null) {
                    onCallResultListener.onDeviceIsEmpty();
                }
            }
        });

        return true;
    }

    public boolean acceptCall() {
        getConnectStatus(new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                List<BluetoothDevice> mDevices = proxy.getConnectedDevices();
                if (mDevices.size() != 0) {
                    for (BluetoothDevice dev : mDevices) {
                        try {
                            if (methodAcceptCall != null) {
                                methodAcceptCall.invoke(proxy, dev, 0);
                            }
                        } catch (Exception e) {
                            if (onCallResultListener != null) {
                                onCallResultListener.onError(e.getMessage());
                            }
                        }
                        break;
                    }
                } else {
                    if (onCallResultListener != null) {
                        onCallResultListener.onDeviceIsEmpty();
                    }
                }
            }

            @Override
            public void onServiceDisconnected(int profile) {
                if (onCallResultListener != null) {
                    onCallResultListener.onDeviceIsEmpty();
                }
            }
        });

        return true;
    }

    private void terminateCall(BluetoothProfile proxy, BluetoothDevice bluetoothDevice) {
        if (bluetoothDevice == null) {
            return;
        }

        try {
            if (methodTerminateCall != null) {
                methodTerminateCall.invoke(proxy, bluetoothDevice, null);
            }
        } catch (Exception e) {
            if (onCallResultListener != null) {
                onCallResultListener.onError(e.getMessage());
            }
        }
    }
}