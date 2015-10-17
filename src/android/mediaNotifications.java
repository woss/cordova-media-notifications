package io.sevensignals.mediaNotifications;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class mediaNotifications extends CordovaPlugin {
    private static String TAG = mediaNotifications.class.getCanonicalName();

    public static final String EVENTNAME_ERROR = "event name null or empty.";
    java.util.Map<String, BroadcastReceiver> receiverMap = new java.util.HashMap<String, BroadcastReceiver>(10);

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Log.d(TAG, "initialize method");
        String[] elements = {"com.spotify.music.metadatachanged", "com.spotify.music.playbackstatechanged", "com.spotify.music.queuechanged"};
        for (int i = 0; i < elements.length - 1; i++) {
            String name = elements[i];
            createReceiver(name);
        }

    }

    @Override
    public void onPause(boolean multitasking) {
        Log.d(TAG, "onPause");
        super.onPause(multitasking);
    }

    @Override
    public void onResume(boolean multitasking) {
        Log.d(TAG, "onResume");
        super.onResume(multitasking);
    }

    /**
     * @param eventName
     * @param data
     * @throws JSONException
     */
    protected void fireEvent(final String eventName, final Object data) throws JSONException {

        String method;
        if (data != null) {
            method = String.format("window.mediaNotifications.fireEvent( '%s', %s );", eventName, String.valueOf(data));
        } else {
            method = String.format("window.mediaNotifications.fireEvent( '%s', {} );", eventName);
        }
        this.webView.loadUrl("javascript:" + method);
    }

    protected void logString(final String eventName, final String string) throws JSONException {
        String method;
        if (string != null) {
            method = String.format("window.mediaNotifications.log( '%s', %s );", eventName, string);
        } else {
            method = String.format("window.mediaNotifications.log( '%s', %s );", eventName, "");
        }

        this.webView.sendJavascript(method);
    }

    protected void logObject(final String eventName, final Object jsonData) throws JSONException {
        String method;
        if (jsonData != null) {
            method = String.format("window.mediaNotifications.log( '%s', %s );", eventName, String.valueOf(jsonData));
        } else {
            method = String.format("window.mediaNotifications.log( '%s', %s );", eventName, "");
        }

        this.webView.sendJavascript(method);
    }

    /**
     * Use to get the current Cordova Activity
     *
     * @return your Cordova activity
     */
    private Activity getActivity() {

        return this.cordova.getActivity();

    }

    /**
     * @param action          The action to execute.
     * @param args            The exec() arguments.
     * @param callbackContext The callback context used when calling back into JavaScript.
     * @return
     * @throws JSONException
     */
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("addEventListener")) {
            final String eventName = args.getString(0);
            if (eventName == null || eventName.isEmpty()) {
                callbackContext.error(EVENTNAME_ERROR);
            }

            logObject("Processing ", eventName);
            if (!receiverMap.containsKey(eventName)) {
                createReceiver(eventName);
            }
            callbackContext.success();

            return true;
        } else if (action.equals("removeEventListener")) {

            final String eventName = args.getString(0);
            if (eventName == null || eventName.isEmpty()) {
                callbackContext.error(EVENTNAME_ERROR);
                return false;
            }

            BroadcastReceiver r = receiverMap.remove(eventName);

//            if (r != null) {
//
//                unregisterReceiver(r);
//
//
//            }
            callbackContext.success();
            return true;
        }

        return false;
    }

    public BroadcastReceiver createReceiver(final String eventName) {
        Log.d(TAG, "in broadcaster");
        final BroadcastReceiver receiver= new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                final Bundle b = intent.getExtras();
                JSONObject data = new JSONObject();
                try {
                    data.put("trackId", intent.getStringExtra("id"));
                    data.put("artistName", intent.getStringExtra("artist"));
                    data.put("albumName", intent.getStringExtra("album"));
                    data.put("trackName", intent.getStringExtra("track"));
                    data.put("trackLengthInSec", intent.getIntExtra("length", 0));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // parse the JSON passed as a string.
                try {
                    fireEvent(eventName, data);
                    logObject(eventName, data);

                } catch (JSONException e) {
                    // Log.e(TAG, "'userdata' is not a valid json object!");
                }

            }
        };
        getActivity().getApplicationContext().registerReceiver(receiver, new IntentFilter(eventName));

        receiverMap.put(eventName, receiver);
        return receiver;
    }

}
