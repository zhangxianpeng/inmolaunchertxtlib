package com.inmoglass.launcher.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

public class BtUtil {

    private static final int PROFILE_DEFAULT = -1;

    public static final int PROFILE_HEADSET = 0;

    public static final int PROFILE_A2DP = 1;

    public static final int PROFILE_OPP = 2;

    public static final int PROFILE_HID = 3;

    public static final int PROFILE_PANU = 4;

    public static final int PROFILE_NAP = 5;

    public static final int PROFILE_A2DP_SINK = 6;

    public static final int PROFILE_COMPUTER = 7;

    public static final int PROFILE_PHONE = 8;
    public static final int PROFILE_IMAGING = 9;

    public static boolean isInmoRing(String deviceName) {
        if (deviceName == null) {
            return false;
        }

        if (TextUtils.isEmpty(deviceName) || deviceName.equalsIgnoreCase("null")) {
            return false;
        }

        return deviceName.trim().contains("Inmo") && deviceName.trim().contains("Ring");
    }

    public static BluetoothDevice getConectedBluetoothDevice() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Class<BluetoothAdapter> bluetoothAdapterClass = BluetoothAdapter.class;
        // 得到蓝牙状态的方法
        Method method = null;
        try {
            method = bluetoothAdapterClass.getDeclaredMethod("getConnectionState", (Class[]) null);
            // 打开权限
            method.setAccessible(true);
//            int state = (int) method.invoke(bluetoothAdapter, (Object[]) null);
//            if (state == BluetoothAdapter.STATE_CONNECTED) {
            Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : devices) {
                Method isConnectedMethod = BluetoothDevice.class.getDeclaredMethod("isConnected", (Class[]) null);
                method.setAccessible(true);
                boolean isConnected = (boolean) isConnectedMethod.invoke(device, (Object[]) null);
                if (isConnected) {
                    return device;
                }
            }
//            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public static int getDeviceType(BluetoothClass bluetoothClass) {
        if (bluetoothClass == null) {
            return PROFILE_DEFAULT;
        }

        switch (bluetoothClass.getMajorDeviceClass()) {
            case BluetoothClass.Device.Major.COMPUTER:
                return PROFILE_COMPUTER;
            case BluetoothClass.Device.Major.PHONE:
                return PROFILE_PHONE;
            case BluetoothClass.Device.Major.PERIPHERAL:
                return PROFILE_HID;
            case BluetoothClass.Device.Major.IMAGING:
                return PROFILE_IMAGING;
            default:
                if (BtUtil.doesClassMatch(bluetoothClass, PROFILE_HEADSET))
                    return PROFILE_HEADSET;
                else if (BtUtil.doesClassMatch(bluetoothClass, PROFILE_A2DP)) {
                    return PROFILE_A2DP;
                } else {
                    return PROFILE_DEFAULT;
                }
        }
    }

    public static boolean doesClassMatch(BluetoothClass bluetoothClass, int profile) {
        if (profile == PROFILE_A2DP) {
            if (bluetoothClass.hasService(BluetoothClass.Service.RENDER)) {
                return true;
            }
            // By the A2DP spec, sinks must indicate the RENDER service.
            // However we found some that do not (Chordette). So lets also
            // match on some other class bits.
            switch (bluetoothClass.getDeviceClass()) {
                case BluetoothClass.Device.AUDIO_VIDEO_HIFI_AUDIO:
                case BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES:
                case BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER:
                case BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO:
                    return true;
                default:
                    return false;
            }
        } else if (profile == PROFILE_A2DP_SINK) {
            if (bluetoothClass.hasService(BluetoothClass.Service.CAPTURE)) {
                return true;
            }
            // By the A2DP spec, srcs must indicate the CAPTURE service.
            // However if some device that do not, we try to
            // match on some other class bits.
            switch (bluetoothClass.getDeviceClass()) {
                case BluetoothClass.Device.AUDIO_VIDEO_HIFI_AUDIO:
                case BluetoothClass.Device.AUDIO_VIDEO_SET_TOP_BOX:
                case BluetoothClass.Device.AUDIO_VIDEO_VCR:
                    return true;
                default:
                    return false;
            }
        } else if (profile == PROFILE_HEADSET) {
            // The render service class is required by the spec for HFP, so is a
            // pretty good signal
            if (bluetoothClass.hasService(BluetoothClass.Service.RENDER)) {
                return true;
            }
            // Just in case they forgot the render service class
            switch (bluetoothClass.getDeviceClass()) {
                case BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE:
                case BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET:
                case BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO:
                    return true;
                default:
                    return false;
            }
        } else if (profile == PROFILE_OPP) {
            if (bluetoothClass.hasService(BluetoothClass.Service.OBJECT_TRANSFER)) {
                return true;
            }

            switch (bluetoothClass.getDeviceClass()) {
                case BluetoothClass.Device.COMPUTER_UNCATEGORIZED:
                case BluetoothClass.Device.COMPUTER_DESKTOP:
                case BluetoothClass.Device.COMPUTER_SERVER:
                case BluetoothClass.Device.COMPUTER_LAPTOP:
                case BluetoothClass.Device.COMPUTER_HANDHELD_PC_PDA:
                case BluetoothClass.Device.COMPUTER_PALM_SIZE_PC_PDA:
                case BluetoothClass.Device.COMPUTER_WEARABLE:
                case BluetoothClass.Device.PHONE_UNCATEGORIZED:
                case BluetoothClass.Device.PHONE_CELLULAR:
                case BluetoothClass.Device.PHONE_CORDLESS:
                case BluetoothClass.Device.PHONE_SMART:
                case BluetoothClass.Device.PHONE_MODEM_OR_GATEWAY:
                case BluetoothClass.Device.PHONE_ISDN:
                    return true;
                default:
                    return false;
            }
        } else if (profile == PROFILE_HID) {
            return (bluetoothClass.getDeviceClass() & BluetoothClass.Device.Major.PERIPHERAL) == BluetoothClass.Device.Major.PERIPHERAL;
        } else if (profile == PROFILE_PANU || profile == PROFILE_NAP) {
            // No good way to distinguish between the two, based on class bits.
            if (bluetoothClass.hasService(BluetoothClass.Service.NETWORKING)) {
                return true;
            }
            return (bluetoothClass.getDeviceClass() & BluetoothClass.Device.Major.NETWORKING) == BluetoothClass.Device.Major.NETWORKING;
        } else {
            return false;
        }
    }
}
