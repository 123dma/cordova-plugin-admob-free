package name.ratson.cordova.admob.interstitial;

import android.util.Log;


import com.google.android.gms.ads.InterstitialAd;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONObject;

import name.ratson.cordova.admob.AbstractExecutor;
import name.ratson.cordova.admob.AdMob;
import name.ratson.cordova.admob.AdMobConfig;
;

public class InterstitialExecutor extends AbstractExecutor {
    /**
     * The interstitial ad to display to the user.
     */
    private static InterstitialAd interstitialAd;

    public InterstitialExecutor(AdMob plugin) {
        super(plugin);
    }
    CordovaPlugin test = new CordovaPlugin();

   @Override
    public String getAdType() {

        return "interstitial";
    }


    public PluginResult prepareAd(JSONObject options, CallbackContext callbackContext, boolean shouldLoadAds) {
        AdMobConfig config = plugin.config;
        CordovaInterface cordova = plugin.cordova;
        config.setInterstitialOptions(options);
        final CallbackContext delayCallback = callbackContext;

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AdMobConfig config = plugin.config;
                CordovaInterface cordova = plugin.cordova;


                destroy();
                if (shouldLoadAds) {
                    interstitialAd = new InterstitialAd(cordova.getActivity());
                    interstitialAd.setAdUnitId(config.getInterstitialAdUnitId());
                    interstitialAd.setAdListener(new InterstitialListener(InterstitialExecutor.this));

                    interstitialAd.loadAd(plugin.buildAdRequest());

                    delayCallback.success();
                } else
                    delayCallback.isFinished();
            }
        });
        return null;
    }

    public static boolean executeClean() {
        plugin.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (interstitialAd != null) {
                    interstitialAd.setAdListener(null);
                    interstitialAd = null;
                    Log.w("clear", "clear");
                }
            }
        });

        return true;
    }



    @Override
    public void destroy() {
        if (interstitialAd != null) {
            interstitialAd.setAdListener(null);
            interstitialAd = null;
        }
    }



//    public static void pause(int ms) {
//        try {
//            Thread.sleep(ms);
//        } catch (InterruptedException e) {
//            System.err.format("IOException: %s%n", e);
//        }
//    }



    public PluginResult showAd(final CallbackContext callbackContext, boolean shouldLoadAds) {
        CordovaInterface cordova = plugin.cordova;
        if (shouldLoadAds) {

            if (interstitialAd == null) {
                return new PluginResult(PluginResult.Status.ERROR, "interstitialAd is null, call createInterstitialView first.");
            }

                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (interstitialAd == null) {
                            return;
                        }

                        if (!plugin.cordova.getActivity().isFinishing() && interstitialAd != null && shouldLoadAds &&  interstitialAd.isLoaded()) {
                            Log.w("cancelou", String.valueOf(plugin.cordova.getActivity().isFinishing()));
                            try{  interstitialAd.show();} catch (Exception w) {}
                        } else if(plugin.cordova.getActivity().isFinishing()){
                            Log.w("cancelou", String.valueOf(plugin.cordova.getActivity().isFinishing()));
                            destroy();

                        }
                        else{
                            Log.d("TAG"," Interstitial not loaded");
                        }


                        callbackContext.success();

                    }
                });

        }
            return null;

    }


}
