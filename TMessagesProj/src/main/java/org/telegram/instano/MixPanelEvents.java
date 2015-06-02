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
    public static final String USER_PROPERTY_FIRST_NAME = "$first_name";
    public static final String USER_PROPERTY_LAST_NAME = "$last_name";
    public static final String USER_PROPERTY_PHONE = "$phone";

//    LaunchActivity button click
    public static final String LAUNCH_INVITE_FREINDS = "Invite Friends";
    public static final String LAUNCH_SETTINGS = "Settings";
    public static final String LAUNCH_ABOUT_US = "About Us";
    public static final String LAUNCH_CONTACT_US = "Contact Us";

//    SettingsActivity button click
    public static final String SETTINGS_USER_PHONE_NUMBER = "Phone";
    public static final String SETTINGS_USER_NAME = "Name";
    public static final String SETTINGS_NOTIFICATIONS_SOUNDS = "Notification";
    public static final String SETTINGS_SECURITY = "Security";
    public static final String SETTINGS_CHAT_BACKGROUND = "Chat Background";
    public static final String SETTINGS_LANGUAGE = "Language";
    public static final String SETTINGS_ANIMATIONS = "Animations";
    public static final String SETTINGS_USING_MOBILE_DATA = "Mobile Data";
    public static final String SETTINGS_USING_WI_FI = "Wi-Fi";
    public static final String SETTINGS_USING_ROAMING = "Roaming";
    public static final String SETTINGS_SAVE_TO_GALLERY = "Save To Gallery";
    public static final String SETTINGS_MESSAGE_TEXT_SIZE = "Text Size";
    public static final String SETTINGS_SEND_BY_ENTER = "Send By Enter";

//    ProfileActivity button click
    public static final String PROFILE_SENDER_PHONE_NUMBER = "Sender Number";
    public static final String PROFILE_NOTIFICATION_AND_SOUNDS = "Notification And Sounds";
    public static final String PROFILE_SHARED_MEDIA = "Shared Media";


    public static MixpanelAPI api() {
        return MixpanelAPI.getInstance(ApplicationLoader.applicationContext, BuildVars.MIXPANEL_TOKEN);
    }
}
