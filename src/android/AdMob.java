package name.ratson.cordova.admob;

import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.mediation.admob.AdMobExtras;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;


import name.ratson.cordova.admob.interstitial.InterstitialExecutor;

import static com.google.ads.AdRequest.LOGTAG;


/**
 * This class represents the native implementation for the AdMob Cordova plugin.
 * This plugin can be used to request AdMob ads natively via the Google AdMob SDK.
 * The Google AdMob SDK is a dependency for this plugin.
 */
public class AdMob extends CordovaPlugin {

    private HashMap<String, com.rjfun.cordova.admob.AdMobMediation> mediations = new HashMap<String, com.rjfun.cordova.admob.AdMobMediation>();
    /**
     * Common tag used for logging statements.
     */
    private static final String TAG = "AdMob";
    public int shouldLoadAds =1;
    public final AdMobConfig config = new AdMobConfig();

    private InterstitialExecutor interstitialExecutor = null;

    private boolean isGpsAvailable = false;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        isGpsAvailable = (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(cordova.getActivity()) == ConnectionResult.SUCCESS);
        Log.w(TAG, String.format("isGooglePlayServicesAvailable: %s", isGpsAvailable ? "true" : "false"));
    }

    /**
     * This is the main method for the AdMob plugin.  All API calls go through here.
     * This method determines the action, and executes the appropriate call.
     *
     * @param action          The action that the plugin should execute.
     * @param inputs          The input parameters for the action.
     * @param callbackContext The callback context.
     * @return A PluginResult representing the result of the provided action.  A
     * status of INVALID_ACTION is returned if the action is not recognized.
     */
    @Override
    public boolean execute(String action, JSONArray inputs, CallbackContext callbackContext) throws JSONException {

        if (interstitialExecutor == null) {
            interstitialExecutor = new InterstitialExecutor(this);
        }


        PluginResult result = null;

        if (Actions.SET_OPTIONS.equals(action)) {
            JSONObject options = inputs.optJSONObject(0);
            result = executeSetOptions(options, callbackContext);

        } else if (Actions.PREPARE_INTERSTITIAL.equals(action)) {
            JSONObject options = inputs.optJSONObject(0);
            result = interstitialExecutor.prepareAd(options, callbackContext);

        } else if (Actions.CREATE_INTERSTITIAL.equals(action)) {
            JSONObject options = inputs.optJSONObject(0);
            result = interstitialExecutor.createAd(options, callbackContext);

        } else if (Actions.REQUEST_INTERSTITIAL.equals(action)) {
            JSONObject options = inputs.optJSONObject(0);
            result = interstitialExecutor.requestAd(options, callbackContext);

        } else if (Actions.SHOW_INTERSTITIAL.equals(action)) {
            boolean show = inputs.optBoolean(0);
            result = interstitialExecutor.showAd(show, callbackContext);

        } else if(Actions.IS_INTERSTITIAL_READY.equals(action)) {
            result = interstitialExecutor.isReady(callbackContext);

        } else {
            Log.d(TAG, String.format("Invalid action passed: %s", action));
            result = new PluginResult(Status.INVALID_ACTION);
        }

        if (result != null) {
            callbackContext.sendPluginResult(result);
        }

        return true;
    }

    private PluginResult executeSetOptions(JSONObject options, CallbackContext callbackContext) {
        Log.w(TAG, "executeSetOptions");

        config.setOptions(options);

        callbackContext.success();
        return null;
    }

    public AdRequest buildAdRequest() {

        AdRequest.Builder builder = new AdRequest.Builder();



        if(config.adExtras != null) {
            Bundle bundle = new Bundle();
            bundle.putInt("cordova", 1);
            Iterator<String> it = config.adExtras.keys();
            while (it.hasNext()) {
                String key = it.next();
                try {
                    bundle.putString(key,  config.adExtras.get(key).toString());
                } catch (JSONException exception) {
                    Log.w(LOGTAG, String.format("Caught JSON Exception: %s", exception.getMessage()));
                }
            }
            builder = builder.addNetworkExtras( new AdMobExtras(bundle) );
        }

        Iterator<String> it = mediations.keySet().iterator();
        while(it.hasNext()) {
            String key = it.next();
            com.rjfun.cordova.admob.AdMobMediation m = mediations.get(key);
            if(m != null) {
                builder = m.joinAdRequest(builder);
            }
        }


        if (config.contentURL != null) {
               builder.setContentUrl(config.contentURL);
            }

        return builder.build();
    }

//    public AdRequest buildAdRequest() {
//        Log.w("buildAdRequest", "buildAdRequest");
//        if (this.shouldLoadAds == 1) {
//            Log.w("buildAdRequest", "buildAdRequestEntrou");
//            AdRequest.Builder builder = new AdRequest.Builder();
//            if (config.isTesting || isRunningInTestLab()) {
//                builder = builder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR).addTestDevice(getDeviceId());
//            }
//
//            if (config.testDeviceList != null) {
//                Iterator<String> iterator = config.testDeviceList.iterator();
//                while (iterator.hasNext()) {
//                    builder = builder.addTestDevice(iterator.next());
//                }
//            }
//
//            Bundle bundle = new Bundle();
//            bundle.putInt("cordova", 1);
//            if (config.adExtras != null) {
//                Iterator<String> it = config.adExtras.keys();
//                while (it.hasNext()) {
//                    String key = it.next();
//                    try {
//                        bundle.putString(key, config.adExtras.get(key).toString());
//                    } catch (JSONException exception) {
//                        Log.w(TAG, String.format("Caught JSON Exception: %s", exception.getMessage()));
//                    }
//                }
//            }
//            builder = builder.addNetworkExtrasBundle(AdMobAdapter.class, bundle);
//
//
//
//            if (config.contentURL != null) {
//                builder.setContentUrl(config.contentURL);
//            }
//
//            Iterator<String> it = mediations.keySet().iterator();
//            while(it.hasNext()) {
//                String key = it.next();
//                com.rjfun.cordova.admob.AdMobMediation m = mediations.get(key);
//                if(m != null) {
//                    builder = m.joinAdRequest(builder);
//                }
//            }
//
//            return builder.build();
//        } else  return null;
//    }
    @Override
    public void onStart() {
        super.onStart();
       this.shouldLoadAds = 1;
        Log.i("onStart", "onStartonStart");
    }

    @Override
    public void onStop() {
        this.shouldLoadAds = 0;
        Log.i("onStop", "onStoponStop");
        if (interstitialExecutor != null) {
            Log.i("onPause", "destroyinterstitialExecutor");
            interstitialExecutor.destroy();
            interstitialExecutor = null;
        }
    }



    @Override
    public void onPause(boolean multitasking) {
        Log.i("onPause", "destroyinterstitialExecutor");
        onDestroy();
       if (interstitialExecutor != null) {

           interstitialExecutor.destroy();
            interstitialExecutor = null;
       }
        super.onPause(multitasking);
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        isGpsAvailable = (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(cordova.getActivity()) == ConnectionResult.SUCCESS);

    }



    @Override
    public void onDestroy() {
        Log.i("onDestroy", "destroyinterstitialExecutor");
        Iterator<String> it = mediations.keySet().iterator();
        while(it.hasNext()) {
            Log.i("onPause", "1212");
            String key = it.next();
            com.rjfun.cordova.admob.AdMobMediation m = mediations.get(key);
            if(m != null) m.onDestroy();
        }
        if (interstitialExecutor != null) {
            interstitialExecutor.destroy();
            interstitialExecutor = null;
        }

        super.onDestroy();
    }

    @NonNull
    private String getDeviceId() {
        // This will request test ads on the emulator and deviceby passing this hashed device ID.
        String ANDROID_ID = Settings.Secure.getString(cordova.getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
        return md5(ANDROID_ID).toUpperCase();
    }

    private boolean isRunningInTestLab() {
        String testLabSetting = Settings.System.getString(cordova.getActivity().getContentResolver(), "firebase.test.lab");
        return "true".equals(testLabSetting);
    }

    private static String md5(final String s) {
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
        }
        return "";
    }
}
