package org.telegram.instano;

import android.content.Context;

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

//    IntroActivity items scrolled
public static final String INTROACTIVITY_ITEM_SCROLLED = "Introduction Icons scrolled";

//    Send Messages Event
    public static String SEND_LOCATION_LATITUDE = "Attached Location Latitude";
    public static String SEND_LOCATION_LONGITUDE = "Attached Location Longitude";
    public static final String SEND_MESSAGES_EMOJI= "Sent Emoji";
    public static final String SEND_MESSAGES= "Sent Message";
    public static final String SEND_MESSAGES_ATTACH_PHOTO= "Sent Camera Photo";
    public static final String SEND_MESSAGES_ATTACH_GALLERY = "Sent Gallery";
    public static final String SEND_MESSAGES_ATTACH_VIDEO = "Sent Video";
    public static final String SEND_MESSAGES_ATTACH_FILE= "Sent File";
    public static final String SEND_MESSAGES_ATTACH_LOCATION= "Sent Location";
    public static final String SEND_MESSAGES_ATTACH_SOUND = "Sent Sound File";

//    Received Messages Event
    public static String LOCATION_LATITUDE = "Latitude";
    public static String LOCATION_LONGITUDE = "Longitude";
    public static final String RECEIVED_MESSAGES_EMOJI= "Received Emoji";
    public static final String RECEIVED_MESSAGES= "Received Message";
    public static final String RECEIVED_MESSAGES_ATTACH_PHOTO= "Received Camera Photo";
    public static final String RECEIVED_MESSAGES_ATTACH_GALLERY = "Received Gallery";
    public static final String RECEIVED_ATTACH_VIDEO = "Received Video";
    public static final String RECEIVED_ATTACH_FILE= "Received File";
    public static final String RECEIVED_ATTACH_LOCATION= "Received Location";
    public static final String RECEIVED_MESSAGES_ATTACH_SOUND = "Received Sound File";

    private final String TAG = getClass().getSimpleName();
    public static final String PROPERTY_BUILD_TYPE = "Build Type";
    public static final String FIRST_TIME = "First Time";
    public static final String CHAT_ACTIVITY_OPENED = "Chat Activity Opened";

    public static final String USER_USER_ID = "User user Id";
    public static final String USER_PROPERTY_FIRST_NAME = "$first_name";
    public static final String USER_PROPERTY_LAST_NAME = "$last_name";
    public static final String USER_PROPERTY_PHONE = "$phone";

//    LaunchActivity button click
    public static final String LAUNCH_INVITE_FREINDS = "Launch->Invite Friends";
    public static final String LAUNCH_SETTINGS = "Launch->Settings";
    public static final String LAUNCH_ABOUT_US = "Launch->About Us";
    public static final String LAUNCH_CONTACT_US = "Launch->Contact Us";

//    SettingsActivity button click
    public static final String SETTINGS_USER_PHONE_NUMBER = "Settings->User Phone Number";
    public static final String SETTINGS_USER_NAME = "Settings->User Name";
    public static final String SETTINGS_NOTIFICATIONS_SOUNDS = "Settings->Notification";
    public static final String SETTINGS_SECURITY = "Settings->Security";
    public static final String SETTINGS_CHAT_BACKGROUND = "Settings->Chat Background";
    public static final String SETTINGS_LANGUAGE = "Settings->Language";
    public static final String SETTINGS_ANIMATIONS = "Settings->Animations";
    public static final String SETTINGS_USING_MOBILE_DATA = "Settings->Mobile Data";
    public static final String SETTINGS_USING_WI_FI = "Settings->Wi-Fi";
    public static final String SETTINGS_USING_ROAMING = "Settings->Roaming";
    public static final String SETTINGS_SAVE_TO_GALLERY = "Settings->Save To Gallery";
    public static final String SETTINGS_MESSAGE_TEXT_SIZE = "Settings->Text Size";
    public static final String SETTINGS_SEND_BY_ENTER = "Settings->Send By Enter";

//    ProfileActivity button click
//    public static final String PROFILE_SENDER_PHONE_NUMBER = "Profile->Sender Number";
//    public static final String PROFILE_NOTIFICATION_AND_SOUNDS = "Profile->Notification And Sounds";
//    public static final String PROFILE_SHARED_MEDIA = "Profile->Shared Media";

//    SecuritySettingsActivity button click
    public static final String SECURITY_TWO_STEP_VERIFICATION = "Security->Two-Step Verification";
    public static final String SECURITY_ACTIVE_SESSIONS = "Security->Active Sessions ";
    public static final String SECURITY_SELF_DESTRUCT = "Security->Account Self Destruct ";

//    TwoStepVerificationActivity button click
    public static final String TWO_STEP_VERIFICATION_SET_PASSWORD = "Two-Step Verification->Set Password/Change Password";
    public static final String TWO_STEP_VERIFICATION_SET_RECOVERY_EMAIL = "Two-StepVerification->Set Email/Change Email";
    public static final String TWO_STEP_VERIFICATION_TURN_PASSWORD_OFF = "Two-Step Verification->Turn Off Password/Abort Password";

//    NotificationsSettiongsActivity button click
    public static final String NOTIFICATION_SETTINGS_LED_COLOR = "Notification Settings->Led Color";
    public static final String NOTIFICATION_SETTINGS_VIBRATE = "Notification Settings->Vibrate";
    public static final String NOTIFICATION_SETTINGS_POPUP_NOTIFICATION = "Notification Settings->Pop Up Notification";
    public static final String NOTIFICATION_SETTINGS_SOUND = "Notification Settings->Sound";
    public static final String NOTIFICATION_SETTINGS_RESET_NOTIFICATIONS = "Notification Settings->Reset Notifications";

//    URL clicked in ChatActivity
    public static final String URL_CLICKS = "URL clicks";
    public static final String URL_CLICKED = "URL clicked";

    public static MixpanelAPI api(Context context) {
        if (context == null)
            return api();
        else
            return MixpanelAPI.getInstance(context, BuildVars.mixpanelToken());
    }

    public static MixpanelAPI api() {
        return MixpanelAPI.getInstance(ApplicationLoader.applicationContext, BuildVars.mixpanelToken());
    }

    public static String getId(){
        return MixpanelAPI.getInstance(ApplicationLoader.applicationContext, BuildVars.mixpanelToken()).getPeople().getDistinctId();
    }
}
