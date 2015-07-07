/*
 * This is the source code of Telegram for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package org.telegram.android;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ConnectionsManager;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;
import org.telegram.ui.LaunchActivity;

public class GcmBroadcastReceiver extends BroadcastReceiver {

    public static final int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        FileLog.d("tmessages", "GCM received intent: " + intent);
        FileLog.d("tmessages", "GCM received intent: " + intent.getStringExtra("order"));

        if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")) {
            AndroidUtilities.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    ApplicationLoader.postInitApplication();

                    try {
                        String key = intent.getStringExtra("loc_key");
                        if ("DC_UPDATE".equals(key)) {
                            String data = intent.getStringExtra("custom");
                            JSONObject object = new JSONObject(data);
                            int dc = object.getInt("dc");
                            String addr = object.getString("addr");
                            String[] parts = addr.split(":");
                            if (parts.length != 2) {
                                return;
                            }
                            String ip = parts[0];
                            int port = Integer.parseInt(parts[1]);
                            ConnectionsManager.getInstance().applyDcPushUpdate(dc, ip, port);
                        }
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }

                    if (intent.getStringExtra("order") != null) {
                        String order = intent.getStringExtra("order");
                        try {
                            JSONObject orderId = new JSONObject();
                            orderId.put("test", "test");
                            //TODO : Time difference to be put here
//                            JSONObject orderId = new JSONObject(order);
//                            long date = orderId.getLong("updated_at");
//                            JSONObject timeDiff = new JSONObject();
//                            timeDiff.put("time_difference", System.currentTimeMillis()- date);
//                            MixpanelAPI.getInstance(context, BuildVars.mixpanelToken()).track(MixPanelEvents.ORDER_NOTIFICATION_RECEIVED, timeDiff);

                            NotificationManager mNotificationManager =
                                    (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

                            Notification.Builder builder = new Notification.Builder(context);
                            builder.setTicker(context.getString(R.string.AppName));
                            builder.setSmallIcon(R.drawable.ic_launcher);
//                            builder.setOngoing(false);
                            builder.setAutoCancel(true);
                            builder.setContentTitle("Your new order");
                            builder.setContentText(order);

                            Intent notificationIntent = new Intent(context, LaunchActivity.class);
                            notificationIntent.putExtra("open_my_orders", true);
                            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);

                            builder.setContentIntent(contentIntent);
                            builder.setDefaults(Notification.DEFAULT_ALL);

                            Intent facebookShareIntent = new Intent(context, LaunchActivity.class);
                            facebookShareIntent.setAction("share_on_facebook");
                            facebookShareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            facebookShareIntent.putExtra("content_url", "https://play.google.com/store/apps/details?id=com.instano.genie");
                            facebookShareIntent.putExtra("content_title", "My Order");
                            facebookShareIntent.putExtra("content_description", "Order Description");
                            PendingIntent facebookShareContentIntent = PendingIntent.getActivity(context, 0, facebookShareIntent, 0);

                            builder.addAction(R.drawable.facebook_icon, "Share", facebookShareContentIntent);

                            Intent twitterShareIntent = new Intent(context, LaunchActivity.class);
                            twitterShareIntent.setAction("share_on_twitter");
                            twitterShareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            twitterShareIntent.putExtra("url", "https://play.google.com/store/apps/details?id=com.instano.genie");
                            twitterShareIntent.putExtra("text", "My Order\nOrder Description");
                            PendingIntent twitterShareContentIntent = PendingIntent.getActivity(context, 0, twitterShareIntent, 0);

                            builder.addAction(R.drawable.twitter_icon, "Share", twitterShareContentIntent);

                            mNotificationManager.notify(1, builder.build());
                            
                        } catch (JSONException e) {
                            FileLog.e(BuildVars.TAG, e);
                        }

                    }

                    ConnectionsManager.getInstance().resumeNetworkMaybe();
                }
            });
        } else if (intent.getAction().equals("com.google.android.c2dm.intent.REGISTRATION")) {
            String registration = intent.getStringExtra("registration_id");
            if (intent.getStringExtra("error") != null) {
                FileLog.e("tmessages", "Registration failed, should try again later.");
            } else if (intent.getStringExtra("unregistered") != null) {
                FileLog.e("tmessages", "unregistration done, new messages from the authorized sender will be rejected");
            } else if (registration != null) {
                FileLog.e("tmessages", "registration id = " + registration);
            }
        }

        setResultCode(Activity.RESULT_OK);
    }
}
