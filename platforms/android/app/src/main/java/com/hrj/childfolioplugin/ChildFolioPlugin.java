package com.hrj.childfolioplugin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class ChildFolioPlugin extends CordovaPlugin {

    private static final int CAMERA_RESULT = 0x001;
    private CallbackContext PUBLIC_CALLBACKS = null;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        PUBLIC_CALLBACKS = callbackContext;
        if (action.equals("openCamera")) {
            String message = args.getString(0);
            // this.coolMethod(message, callbackContext);
            cordova.setActivityResultCallback(this);
            cordova.getActivity().startActivityForResult(new Intent(cordova.getActivity(), TakePhotoActivity.class), CAMERA_RESULT);

            PluginResult pluginResult = new  PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true); // Keep callback

            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == cordova.getActivity().RESULT_OK) {
            Bundle extras = data.getExtras();
            String photos = extras.getString("data");
            tolog(photos);
            PluginResult resultado = new PluginResult(PluginResult.Status.OK, photos);
            resultado.setKeepCallback(true);
            PUBLIC_CALLBACKS.sendPluginResult(resultado);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void tolog(String toLog){
        Context context = cordova.getActivity();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, toLog, duration);
        toast.show();
    }

//    private void coolMethod(String message, CallbackContext callbackContext) {
//        if (message != null && message.length() > 0) {
//            callbackContext.success(message);
//        } else {
//            callbackContext.error("Expected one non-empty string argument.");
//        }
//    }
}
