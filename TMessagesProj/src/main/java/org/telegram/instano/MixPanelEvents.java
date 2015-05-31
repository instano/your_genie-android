package org.telegram.instano;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.LoginActivity;

/**
 * static helper class for mix panel
 * TODO: run {@link MixpanelAPI#reset()} on sign out
 * identify is called in {@link ChatActivity#onFragmentCreate()}
 * alias is called in {@link LoginActivity#needFinishActivity()} so it is always called before identify
 * Created by vedant on 5/28/15.
 */
public class MixPanelEvents {
    private final String TAG = getClass().getSimpleName();
    public static final String PROPERTY_BUILD_TYPE = "Build Type";
    public static final String FIRST_TIME = "First Time";
    public static final String CHAT_ACTIVITY_OPENED = "Chat Activity Opened";

    public static final String USER_USER_ID = "User user Id";
    public static final String USER_NAME = "User name";
    public static final String USER_PHONE = "User phone";

    public static MixpanelAPI api() {
        return MixpanelAPI.getInstance(ApplicationLoader.applicationContext, BuildVars.MIXPANEL_TOKEN);
    }
}
