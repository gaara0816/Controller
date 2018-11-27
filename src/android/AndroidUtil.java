package com.mumatech.controller;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;


public class AndroidUtil {
    private static final String TAG = AndroidUtil.class.getSimpleName();

    public static String deviceId;

    @TargetApi(Build.VERSION_CODES.M)
    public static String getIMEI(Context context) {
        TelephonyManager TelephonyMgr = (TelephonyManager) context.getSystemService(Activity.TELEPHONY_SERVICE);
        context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE);
        String szIMEI = TelephonyMgr.getDeviceId();
        return szIMEI;
    }

    public static String getDeviceID(Context context) {
        String m_szAndroidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (deviceId == null) {
//            deviceId = m_szAndroidID;
            // deviceId = "90b2b3efc31e05fc";
           deviceId = "7ad9ca49221bcc0c";
        }
        return deviceId;
    }
}
