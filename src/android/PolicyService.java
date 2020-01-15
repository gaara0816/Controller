package com.mumatech.controller;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;

import cn.jiguang.analytics.android.api.CountEvent;
import cn.jiguang.analytics.android.api.JAnalyticsInterface;

public class PolicyService extends Service {

    public static final String ACTION_MAIN = "com.mumatech.controller.ACTION_MAIN";
    private  Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context=getApplicationContext();
        JAnalyticsInterface.setDebugMode(true);
        JAnalyticsInterface.init(getApplicationContext());
        Log.d(this.getPackageName(), "onCreate: Service服务启动了");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mServerMessenger.getBinder();
    }

    private Handler mServerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg != null) {
                Intent main = new Intent(ACTION_MAIN);
                Bundle data = msg.getData();
                Date currentTime = new Date();
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String dateString = formatter.format(currentTime);
                CountEvent countEvent = new CountEvent(data.getString(ConfigHelper.COMMAND_TYPE));
                JAnalyticsInterface.onEvent(context, countEvent);
                countEvent.addKeyValue("deviceId", AndroidUtil.getDeviceID(context)+dateString);

                main.putExtra(ConfigHelper.COMMAND_TYPE, data.getString(ConfigHelper.COMMAND_TYPE));
                main.putExtra(ConfigHelper.COMMAND_RES_DATA, data.getString(ConfigHelper.COMMAND_RES_DATA));
                main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    getApplicationContext().startActivity(main);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "请联系工作人员", Toast.LENGTH_SHORT).show();
                }
            }

        }
    };

    private Messenger mServerMessenger = new Messenger(mServerHandler);
}