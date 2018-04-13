package com.hrj.childfolioplugin;

import android.content.Intent;
import android.os.Bundle;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * This class echoes a string called from JavaScript.
 */
public class TakeVideo extends CordovaPlugin {

    private static final int CAMERA_RESULT = 0x001;
    private CallbackContext PUBLIC_CALLBACKS = null;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        PUBLIC_CALLBACKS = callbackContext;
        if (action.equals("openVideo")) {
            cordova.setActivityResultCallback(this);
            cordova.getActivity().startActivityForResult(new Intent(cordova.getActivity(), VideoRecorderActivity.class), CAMERA_RESULT);

            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true); // Keep callback

            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == cordova.getActivity().RESULT_OK) {
            Bundle extras = data.getExtras();
            String outPath = extras.getString("output_path");
            PluginResult resultado = new PluginResult(PluginResult.Status.OK, outPath);
            resultado.setKeepCallback(true);
            PUBLIC_CALLBACKS.sendPluginResult(resultado);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
