package org.telegram.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.instano.MixPanelEvents;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by Rohit on 8/7/15.
 */
public class InstallReferrerReceiver extends BroadcastReceiver {

    public static final String REFERRER_ID = "referrer";
    @Override
    public void onReceive(Context context, Intent intent) {

        FileLog.d("tmessages", "Referrer intent: " + intent);

        if (intent.getAction().equals("com.android.vending.INSTALL_REFERRER")) {
            FileLog.d("tmessages", "");
            // This intent should have a referrer string attached to it.
            String rawReferrer = intent.getStringExtra("referrer");
            FileLog.d("tmessages",intent.getExtras()+"");
            FileLog.d("tmessages",intent.getStringExtra("referrer"));

            if (intent.getStringExtra("referrer") != null) {

                SharedPreferences.Editor editor = context.getSharedPreferences("referrer", Context.MODE_PRIVATE).edit();
                FileLog.d("InstallReferrerReceiver", "referrer: " + intent.getStringExtra("referrer"));
                editor.putString(REFERRER_ID, intent.getStringExtra("referrer"));
                editor.apply();
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(MixPanelEvents.DISTINCT_ID, intent.getStringExtra("referrer"));
                    MixpanelAPI.getInstance(context, BuildVars.mixpanelToken()).track(MixPanelEvents.REFERRED_INSTALL, jsonObject);
                    FileLog.d("tmessages", "Installed : referrer_id "+intent.getStringExtra("referrer"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (null != rawReferrer)
            {
                //TODO: user the rawReferer for further actions
//                // The string is usually URL Encoded, so we need to decode it.
                String referrer = null;
                try {
                    referrer = URLDecoder.decode(rawReferrer, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
//
                // Log the referrer string.
                FileLog.d("tmessages",
                        "ReferrerReceiver.onReceive(Context, Intent)" +
                                "\nRaw referrer: " + rawReferrer +
                                "\nReferrer: " + referrer);
            }
        }
    }

}
