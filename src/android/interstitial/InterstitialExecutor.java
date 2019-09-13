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
    private InterstitialAd interstitialAd;

    public InterstitialExecutor(AdMob plugin) {
        super(plugin);
    }



   @Override
    public String getAdType() {
        return "interstitial";
    }

    public PluginResult prepareAd(JSONObject options, CallbackContext callbackContext) {
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
                if(plugin.shouldLoadAds == 1) {
                    interstitialAd = new InterstitialAd(cordova.getActivity());
                    interstitialAd.setAdUnitId(config.getInterstitialAdUnitId());
                    interstitialAd.setAdListener(new InterstitialListener(InterstitialExecutor.this));
                    Log.w("interstitial", config.getInterstitialAdUnitId());
                    Log.w("teste.shouldLoadAds", String.valueOf(plugin.shouldLoadAds));
                }

                if(plugin.shouldLoadAds == 1) {
                    Log.w("loadAd", "Entrou");
                    interstitialAd.loadAd(plugin.buildAdRequest());
                    delayCallback.success();
                } else
                    delayCallback.isFinished();






            }
        });
        return null;
    }


    public void onStart() {
        //  super.onStart();

        Log.i("onStart", "onStartonStart");
    }


    public void onStop() {

        Log.i("onStop", "onStoponStop");
    }
    /**
     * Parses the createAd interstitial view input parameters and runs the createAd interstitial
     * view action on the UI thread.  If this request is successful, the developer
     * should make the requestAd call to request an ad for the banner.
     *
     * @param options The JSONArray representing input parameters.  This function
     *                expects the first object in the array to be a JSONObject with the
     *                input parameters.
     * @return A PluginResult representing whether or not the banner was created
     * successfully.
     */
    public PluginResult createAd(JSONObject options, CallbackContext callbackContext) {
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
                if(plugin.shouldLoadAds == 1) {
                    interstitialAd = new InterstitialAd(cordova.getActivity());
                    interstitialAd.setAdUnitId(config.getInterstitialAdUnitId());
                    interstitialAd.setAdListener(new InterstitialListener(InterstitialExecutor.this));
                    Log.w("interstitial", config.getInterstitialAdUnitId());
                    Log.w("teste.shouldLoadAds", String.valueOf(plugin.shouldLoadAds));
                }
                if(plugin.shouldLoadAds == 1) {
                    interstitialAd.loadAd(plugin.buildAdRequest());
                    delayCallback.success();
                } else
                    delayCallback.isFinished();


            }
        });
        return null;
    }

    @Override
    public void destroy() {
        if (interstitialAd != null) {
            interstitialAd.setAdListener(null);
            interstitialAd = null;
        }
    }




    public PluginResult requestAd(JSONObject options, CallbackContext callbackContext) {
        CordovaInterface cordova = plugin.cordova;

        plugin.config.setInterstitialOptions(options);

        if (interstitialAd == null) {
            callbackContext.error("interstitialAd is null, call createInterstitialView first");
            return null;
        }

        final CallbackContext delayCallback = callbackContext;
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (interstitialAd == null) {
                    return;
                }
                if(plugin.shouldLoadAds == 1){  interstitialAd.loadAd(plugin.buildAdRequest());}


                delayCallback.success();
            }
        });

        return null;
    }

    public PluginResult showAd(final boolean show, final CallbackContext callbackContext) {
        if (plugin.shouldLoadAds == 1) {
            Log.w("teste.shouldLoadAds", String.valueOf(plugin.shouldLoadAds));
            if (interstitialAd == null) {
                return new PluginResult(PluginResult.Status.ERROR, "interstitialAd is null, call createInterstitialView first.");
            }
            CordovaInterface cordova = plugin.cordova;

            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (interstitialAd == null) {
                        return;
                    }
                    AdMobConfig config = plugin.config;

                    if (interstitialAd.isLoaded() && plugin.shouldLoadAds == 1) {
                        Log.w("Entrou", "Entrou");

                        interstitialAd.show();
                        if (callbackContext != null) {
                            callbackContext.success();
                        }
                    } else if (!config.autoShowInterstitial) {
                        if (callbackContext != null) {
                            callbackContext.error("Interstital not ready yet");
                        }
                    }
                }
            });
        }
            return null;

    }

    public PluginResult isReady(final CallbackContext callbackContext) {
        CordovaInterface cordova = plugin.cordova;

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (interstitialAd != null && interstitialAd.isLoaded() && plugin.shouldLoadAds == 1) {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, true));
                } else {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, false));
                }
            }
        });

        return null;
    }

    public static void pause(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            System.err.format("IOException: %s%n", e);
        }
    }

    boolean shouldAutoShow() {
        return plugin.config.autoShowInterstitial;
    }
}
