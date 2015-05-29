package org.telegram.instano;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;

/**
 * Created by vedant on 5/28/15.
 */
public class MixPanelEvents {
    private final String TAG = getClass().getSimpleName();
    public static final String FIRST_TIME = "First Time";
    public static final String CHAT_ACTIVITY_OPENED = "Chat Activity Opened";

    public static MixpanelAPI api() {
        return MixpanelAPI.getInstance(ApplicationLoader.applicationContext, BuildVars.MIXPANEL_TOKEN);
    }
}
