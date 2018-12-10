package com.mumatech.controller;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

// import net.vidageek.mirror.dsl.Mirror;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class AndroidUtil {
    private static final String TAG = AndroidUtil.class.getSimpleName();

    public static String blcMac;

    public static String deviceId = null;

    /**
     * Android 6.0 之前（不包括6.0）获取mac地址
     * 必须的权限 <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"></uses-permission>
     *
     * @param context * @return
     */
    public static String getMacDefault(Context context) {
        String mac = "";
        if (context == null) {
            return mac;
        }
        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = null;
        try {
            info = wifi.getConnectionInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (info == null) {
            return null;
        }
        mac = info.getMacAddress();
        if (!TextUtils.isEmpty(mac)) {
            mac = mac.toUpperCase(Locale.ENGLISH);
        }
        return mac;
    }

    /**
     * Android 6.0-Android 7.0 获取mac地址
     */
    public static String getMacAddress() {
        String macSerial = null;
        String str = "";

        try {
            Process pp = Runtime.getRuntime().exec("cat/sys/class/net/wlan0/address");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            while (null != str) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();//去空格
                    break;
                }
            }
        } catch (IOException ex) {
            // 赋予默认值
            ex.printStackTrace();
        }

        return macSerial;
    }

    /**
     * Android 7.0之后获取Mac地址
     * 遍历循环所有的网络接口，找到接口是 wlan0
     * 必须的权限 <uses-permission android:name="android.permission.INTERNET"></uses-permission>
     *
     * @return
     */
    public static String getMacFromHardware() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0"))
                    continue;
                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) return "";
                StringBuilder res1 = new StringBuilder();
                for (Byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }
                if (!TextUtils.isEmpty(res1)) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * Gets the mac address on version >= Marshmallow.
     *
     * @return the mac address
     */
    private static String getMMacAddress() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());

            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02x", (b & 0xFF)) + ":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }

                return res1.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "02:00:00:00:00:00";
    }

    private static String format(String data) {
        return data.replaceAll(":", "").toUpperCase();
    }

    public static String getDeviceID(Context context) {
        // return getIMEI(context);
        return getWlanMAC(context);
    }

    /**
     * 获取mac地址（适配所有Android版本）
     *
     * @return
     */
    public static String getWlanMAC(Context context) {
        if (deviceId == null) {
            String mac = "02:00:00:00:00:00";
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                mac = getMacDefault(context);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                mac = getMacAddress();
            } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
//            mac = getMacFromHardware();
                mac = getMMacAddress();
            }
            deviceId = format(mac);
        }
        return deviceId;
    }

    // public static String getBtAddressViaReflection() {
    //     if (blcMac == null) {
    //         BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    //         Object bluetoothManagerService = new Mirror().on(bluetoothAdapter).get().field("mService");
    //         if (bluetoothManagerService == null) {
    //             Log.w(TAG, "couldn't find bluetoothManagerService");
    //             return blcMac;
    //         }
    //         Object address = new Mirror().on(bluetoothManagerService).invoke().method("getAddress").withoutArgs();
    //         if (address != null && address instanceof String) {
    //             Log.w(TAG, "using reflection to get the BT MAC address: " + address);
    //             blcMac = (String) address;
    //         }
    //     }
    //     return blcMac;
    // }

    /**
     * 获取手机IMEI
     *
     * @param context
     * @return
     */
    @SuppressLint("MissingPermission")
    public static String getIMEI(Context context) {
        String imei;
        //实例化TelephonyManager对象
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        //获取IMEI号
//        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return TODO;
//        } else {
        imei = telephonyManager.getDeviceId();
//        }
        return imei;
    }
}
