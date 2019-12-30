package com.example.myapplication;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class PolicyService extends Service {

    public static final String ACTION_MAIN = "com.mumatech.controller.ACTION_MAIN";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(this.getPackageName(), "onCreate: Service服务启动了");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mServerMessenger.getBinder();
    }

    private Handler mServerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg!=null){
                Intent main = new Intent(ACTION_MAIN);
                Bundle data = msg.getData();
                main.putExtra(ConfigHelper.COMMAND_TYPE, data.getString(ConfigHelper.COMMAND_TYPE));
                main.putExtra(ConfigHelper.COMMAND_RES_DATA, data.getString(ConfigHelper.COMMAND_RES_DATA));
                main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    getApplicationContext().startActivity(main);

                }catch (Exception e){
                    Toast.makeText( getApplicationContext(),"请联系工作人员",Toast.LENGTH_SHORT).show();
                }
            }

        }
    };

    private Messenger mServerMessenger = new Messenger(mServerHandler);
}