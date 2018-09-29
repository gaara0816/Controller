package com.mumatech.controller;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.google.gson.Gson;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class echoes a string called from JavaScript.
 */
public class Controller extends CordovaPlugin {

    private static final String ACTION = "com.mumatech.controller.ACTION";

    private static final long BIND_OUT_TIME = 3000;

    private ISTM8Controller aidlBind;

    private boolean bindResult;
    private boolean isConnect;

    private Lock locks = new ReentrantLock(true);
    private Condition bindCondition = locks.newCondition();
    // private Object lock = new Object();

    private CallbackContext context;
    private TcpClientConnector connector;

    private ServiceConnection aidlConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            aidlBind = ISTM8Controller.Stub.asInterface(service);
            isConnect = true;
            // synchronized (lock) {
            // lock.notify();
            // }
            locks.lock();
            bindCondition.signal();
            locks.unlock();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            aidlBind = null;
            isConnect = false;
            // synchronized (lock) {
            // lock.notify();
            // }
            locks.lock();
            bindCondition.signal();
            locks.unlock();
        }
    };

    private static Controller instance;
    private static Activity cordovaActivity;

    public Controller() {
        instance = this;
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        context = callbackContext;
        if (!bindResult || !isConnect) {
            // 发起PolicyService绑定
            Intent intent = new Intent(ACTION);
            intent.setPackage("com.mumatech.policyservice");
            bindResult = Controller.this.cordova.getActivity().getApplicationContext().bindService(intent,
                    aidlConnection, Context.BIND_AUTO_CREATE);

            // 等待绑定结果，设置超时时间

            // synchronized (lock) {
            // try {
            // lock.wait(3000);
            // } catch (InterruptedException e) {
            // e.printStackTrace();
            // }
            // }

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
        } else if (action.equals("update")) {
            String message = args.getString(0);
            this.update(message, callbackContext);
            return true;
        } else if (action.equals("changeKPadPower")) {
            boolean message = args.getBoolean(0);
            this.changeKPadPower(message, callbackContext);
            return true;
        } else if (action.equals("changePPadSpeakerPower")) {
            boolean message = args.getBoolean(0);
            this.changePPadSpeakerPower(message, callbackContext);
            return true;
        } else if (action.equals("callJSInit")) {
            return true;
        }
        return false;
    }

    private void lock(String message, CallbackContext callbackContext) {
        try {
            aidlBind.lock();
            callbackContext.success(message);
        } catch (RemoteException e) {
            e.printStackTrace();
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void pause(String message, CallbackContext callbackContext) {
        try {
            aidlBind.pause();
            callbackContext.success(message);
        } catch (RemoteException e) {
            e.printStackTrace();
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void unLock(String message, CallbackContext callbackContext) {
        try {
            aidlBind.unLock();
            callbackContext.success(message);
        } catch (RemoteException e) {
            e.printStackTrace();
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void update(String message, CallbackContext callbackContext) {
        try {
            aidlBind.update();
            callbackContext.success(message);
        } catch (RemoteException e) {
            e.printStackTrace();
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void changeKPadPower(boolean message, CallbackContext callbackContext) throws JSONException {
        try {
            aidlBind.changeKPadPower(message);
            callbackContext.success(new JSONObject()
                    .put(ControllerError.CODE, ControllerError.CODE_SUCCESS)
                    .put(ControllerError.MESSAGE, ControllerError.MSG_SUCCESS));
        } catch (RemoteException e) {
            e.printStackTrace();
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void changePPadSpeakerPower(boolean message, CallbackContext callbackContext) throws JSONException {
        try {
            aidlBind.changePPadSpeakerPower(message);
            callbackContext.success(new JSONObject()
                    .put(ControllerError.CODE, ControllerError.CODE_SUCCESS)
                    .put(ControllerError.MESSAGE, ControllerError.MSG_SUCCESS));
        } catch (RemoteException e) {
            e.printStackTrace();
            callbackContext.error("Expected one non-empty string argument.");
        }
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
    }

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
        connector = TcpClientConnector.getInstance();
        connector.setOnConnectLinstener(new TcpClientConnector.ConnectLinstener() {
            @Override
            public void onConnected() {
            }

            @Override
            public void onDisConnected() {

            }

            @Override
            public void onReceiveData(String data) {
                String content = new Gson().toJson(STM8Status.initWithData(data));
                callJSFunction(content);
            }
        });
        connector.creatConnect("localhost", 8688);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isConnect) {
            this.cordova.getActivity().unbindService(aidlConnection);
        }
        bindResult = false;
        if (connector != null) {
            try {
                connector.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        cordovaActivity = null;
        instance = null;
    }

    static void callJSFunction(String content) {
        if (instance == null) {
            return;
        }
        final String format = String.format("showAlert(%s)", content);

        cordovaActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                instance.webView.loadUrl("javascript:" + format);
            }
        });

    }
}
