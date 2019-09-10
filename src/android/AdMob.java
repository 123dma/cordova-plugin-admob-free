package name.ratson.cordova.admob;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

import name.ratson.cordova.admob.interstitial.InterstitialExecutor;


/**
 * This class represents the native implementation for the AdMob Cordova plugin.
 * This plugin can be used to request AdMob ads natively via the Google AdMob SDK.
 * The Google AdMob SDK is a dependency for this plugin.
 */
public class AdMob extends CordovaPlugin {
    private InterstitialAd interstitialAd;
    private CallbackContext readyCallbackContext = null;
    /**
     * Common tag used for logging statements.
     */
    private static final String TAG = "AdMob";
    public boolean shouldLoadAds =true;

    public final AdMobConfig config = new AdMobConfig();

    private InterstitialExecutor interstitialExecutor = null;



    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
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
            if (this.shouldLoadAds) {

                JSONObject options = inputs.optJSONObject(0);
                result = interstitialExecutor.prepareAd(options, callbackContext, shouldLoadAds);
            } else return false;
        } else if (Actions.SHOW_INTERSTITIAL.equals(action)) {
            if (this.shouldLoadAds) {
                boolean show = inputs.optBoolean(0);
                result = interstitialExecutor.showAd( callbackContext, shouldLoadAds);
            } else return false;

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
        Log.w("buildAdRequest", "buildAdRequest");
        if (this.shouldLoadAds) {
            Log.w("buildAdRequest", "buildAdRequestEntrou");
            AdRequest.Builder builder = new AdRequest.Builder();
            if (config.isTesting || isRunningInTestLab()) {
                builder = builder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR).addTestDevice(getDeviceId());
            }

            if (config.testDeviceList != null) {
                Iterator<String> iterator = config.testDeviceList.iterator();
                while (iterator.hasNext()) {
                    builder = builder.addTestDevice(iterator.next());
                }
            }

            Bundle bundle = new Bundle();
            bundle.putInt("cordova", 1);
            if (config.adExtras != null) {
                Iterator<String> it = config.adExtras.keys();
                while (it.hasNext()) {
                    String key = it.next();
                    try {
                        bundle.putString(key, config.adExtras.get(key).toString());
                    } catch (JSONException exception) {
                        Log.w(TAG, String.format("Caught JSON Exception: %s", exception.getMessage()));
                    }
                }
            }
            builder = builder.addNetworkExtrasBundle(AdMobAdapter.class, bundle);


            if (config.contentURL != null) {
                builder.setContentUrl(config.contentURL);
            }

            return builder.build();
        } else  return null;
    }

    @Override
    public void onStart() {
        super.onStart();
        shouldLoadAds= true;
        Log.w("shouldLoadAds",  String.valueOf(shouldLoadAds));
    }

    @Override
    public void onStop() {
        shouldLoadAds= false;
        readyCallbackContext = null;
        Log.w("shouldLoadAds",  String.valueOf(shouldLoadAds));
        super.onStop();
    }


    @Override
    public void onPause(boolean multitasking) {
        super.onPause(false);
        interstitialExecutor.executeClean();
        readyCallbackContext = null;
        shouldLoadAds = false;
        Log.w("interstitialAd.onPause", String.valueOf(shouldLoadAds));
    }


    @Override
    public void onDestroy() {
        Log.i("onDestroy", "onDestroyinterstitialExecutor");

        if (interstitialExecutor != null) {
            interstitialExecutor.destroy();
            interstitialExecutor = null;

        }

        super.onDestroy();
    }

    //@NonNull
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
