package com.mumatech.controller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * This class echoes a string called from JavaScript.
 */
public class Controller extends CordovaPlugin {

    private static final String ACTION = "com.mumatech.controller.ACTION";

    private ISTM8Controller aidlBind;

    private ServiceConnection aidlConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            aidlBind = ISTM8Controller.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            aidlBind = null;
        }
    };

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("lock")) {
            String message = args.getString(0);
            this.lock(message, callbackContext);
            return true;
        } else if (action.equals("unLock")) {
            String message = args.getString(0);
            this.unLock(message, callbackContext);
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

    private void unLock(String message, CallbackContext callbackContext) {

        try {
            aidlBind.unLock();
            callbackContext.success(message);
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
    protected void pluginInitialize() {
        super.pluginInitialize();
        Intent intent = new Intent(ACTION);
        intent.setPackage("com.mumatech.policyservice");
        boolean result = this.cordova.getActivity().getApplicationContext().bindService(intent, aidlConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.cordova.getActivity().unbindService(aidlConnection);
    }
}
