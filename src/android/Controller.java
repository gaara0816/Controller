package com.mumatech.controller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * This class echoes a string called from JavaScript.
 */
public class Controller extends CordovaPlugin {

    private static final String TAG = Controller.class.getSimpleName();

    private static final String PKG_NAME = "com.mumatech.policyservice";
    private static final String MQTT_ACTION = "com.mumatech.mqtt.START_SERVICE";

    private static final long BIND_OUT_TIME = 3000;

    private boolean bindResult;
    private boolean isConnect;

    private Lock locks = new ReentrantLock(true);
    private Condition bindCondition = locks.newCondition();

    private CallbackContext context;

    private static Controller instance;
    private static Activity cordovaActivity;

    private Messenger mClientMessenger;

    private CallbackContext mCallbackContext;

    @SuppressLint("HandlerLeak")
    public Controller() {
        instance = this;
        mClientMessenger = new Messenger(new Handler() {
            @Override
            public void handleMessage(final Message msg) {
                if (msg != null && msg.arg1 == ConfigHelper.MSG_ID_SERVER) {
                    if (msg.getData() == null) {
                        return;
                    }

                    String command = msg.getData().getString(ConfigHelper.COMMAND_TYPE);
                    Log.d(TAG, "Message from server: " + command);
                    String result = msg.getData().getString(ConfigHelper.COMMAND_RES_DATA);
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        if (ConfigHelper.COMMAND_RES_SUCCESS.equals(jsonObject.getString(ConfigHelper.COMMAND_RES_CODE))) {
                            mCallbackContext.success(jsonObject);
                        } else {
                            mCallbackContext.error(jsonObject);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        context = callbackContext;
        if (!bindResult || !isConnect) {
            // 发起PolicyService绑定
            bindMessengerService();

            locks.lock();
            try {
                bindCondition.await(BIND_OUT_TIME, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                locks.unlock();
            }

            // 根据
            if (!bindResult) {
                callbackContext.error(new JSONObject().put(ControllerError.CODE, ControllerError.CODE_UNINSTALL)
                        .put(ControllerError.MESSAGE, ControllerError.MSG_UNINSTALL));
                return true;
            } else if (!isConnect) {
                callbackContext.error(new JSONObject().put(ControllerError.CODE, ControllerError.CODE_DISCONNECT)
                        .put(ControllerError.MESSAGE, ControllerError.MSG_DISCONNECT));
                return true;
            } else {
                return handler(action, args, callbackContext);
            }
        } else {
            return handler(action, args, callbackContext);
        }
    }

    private boolean handler(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("lock")) {
            String message = args.getString(0);
            this.lock(message, callbackContext);
            return true;
        } else if (action.equals("pause")) {
            String message = args.getString(0);
            this.pause(message, callbackContext);
            return true;
        } else if (action.equals("unLock")) {
            String message = args.getString(0);
            this.unLock(message, callbackContext);
            return true;
        } else if (action.equals("power")) {
            String message = args.getString(0);
            this.power(message, callbackContext);
            return true;
        } else if (action.equals("callJSInit")) {
            return true;
        }
        return false;
    }

    private void lock(String message, CallbackContext callbackContext) {
        mCallbackContext = callbackContext;
        sendMsg(ConfigHelper.LOCK_CMD, null);
    }

    private void pause(String message, CallbackContext callbackContext) {
        mCallbackContext = callbackContext;
        sendMsg(ConfigHelper.PAUSE_CMD, null);
    }

    private void unLock(String message, CallbackContext callbackContext) {
        mCallbackContext = callbackContext;
        sendMsg(ConfigHelper.UNLOCK_CMD, null);
    }

    private void power(String message, CallbackContext callbackContext) {
        mCallbackContext = callbackContext;
        sendMsg(ConfigHelper.POWER_CMD, null);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        cordovaActivity = cordova.getActivity();
        bindMessengerService();
    }

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isConnect) {
            cordovaActivity.unbindService(mMessengerConnection);
        }
        bindResult = false;
        cordovaActivity = null;
        instance = null;
    }

    static void callJSFunction(String function, String content) {
        if (instance == null) {
            return;
        }
        final String format = String.format("%s(%s)", function, content);

        cordovaActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                instance.webView.loadUrl("javascript:" + format);
            }
        });
    }

    //服务端的 Messenger
    private Messenger mServerMessenger;

    private ServiceConnection mMessengerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            mServerMessenger = new Messenger(service);
            isConnect = true;
            locks.lock();
            bindCondition.signal();
            locks.unlock();
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mServerMessenger = null;
            isConnect = false;
            locks.lock();
            bindCondition.signal();
            locks.unlock();
        }
    };

    private void bindMessengerService() {
        Intent intent = new Intent(MQTT_ACTION);
        intent.setPackage(PKG_NAME);
        bindResult = cordovaActivity.bindService(intent, mMessengerConnection, Context.BIND_AUTO_CREATE);
    }

    public void sendMsg(String command, String msgContent) {
        msgContent = TextUtils.isEmpty(msgContent) ? "默认消息" : msgContent;

        Message message = Message.obtain();
        message.arg1 = ConfigHelper.MSG_ID_CLIENT;
        Bundle bundle = new Bundle();
        bundle.putString(ConfigHelper.COMMAND_TYPE, command);
        if (!TextUtils.isEmpty(msgContent)) {
            bundle.putString(ConfigHelper.COMMAND_DATA, msgContent);
        }
        message.setData(bundle);
        message.replyTo = mClientMessenger;     //指定回信人是客户端定义的

        try {
            mServerMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
