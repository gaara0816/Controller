package com.mumatech.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class PolicyServiceBroadcastReceiver extends BroadcastReceiver {
    public static final String TAG = PolicyServiceBroadcastReceiver.class.getSimpleName();
    public static final String ACTION_MAIN = "com.mumatech.controller.ACTION_MAIN";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, intent.getAction());
        Intent main = new Intent(ACTION_MAIN);
        main.putExtra(ConfigHelper.COMMAND_TYPE, intent.getStringExtra(ConfigHelper.COMMAND_TYPE));
        main.putExtra(ConfigHelper.COMMAND_RES_DATA, intent.getStringExtra(ConfigHelper.COMMAND_RES_DATA));
        main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(main);
        }catch (Exception e){
            Toast.makeText(context,"请联系工作人员",Toast.LENGTH_SHORT).show();
        }

    }
}
