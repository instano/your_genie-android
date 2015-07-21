/*
 * This is the source code of Telegram for Android v. 1.3.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package org.telegram.instano;

import android.os.Bundle;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.TLRPC;

public class BuildVars {
    public static final String CONTACT_NAME = "Instano Operator";
    public static boolean DEBUG_VERSION = BuildConfig.DEBUG;
    public static int APP_ID = 48523; //obtain your own APP_ID at https://core.telegram.org/api/obtaining_api_id
    public static String APP_HASH = "f8bb05eea6efb7d8fcc9b97ce1a3a581"; //obtain your own APP_HASH at https://core.telegram.org/api/obtaining_api_id
    public static String HOCKEY_APP_HASH = "5d41402abc4b2a76b9719d911017c592";
    public static String GCM_SENDER_ID = "811779802218";
    public static String SEND_LOGS_EMAIL = "vedant.kota@gmail.com";
    public static String BING_SEARCH_KEY = ""; //obtain your own KEY at https://www.bing.com/dev/en-us/dev-center
    public static String TAG = "InstanoDebug";
    public static int USER_ID = 105580237;
    public static int CHAT_ID = 0;
    public static int ENC_ID = 0;
    public static int MESSAGE_ID = 0;
    public static final Bundle args;
    public static String ACTIONBAR_TITLE = "Instano";
    public static TLRPC.User instanoUser;
    public static String PHONE = "7899704294";
    public static final String whatsAppId = "919916780444";
    public static AbstractXMPPConnection connection = null;


    // User{id=105580237, first_name='Genie', last_name='Phone', username='your_genie',
    // access_hash=2290130976748427084, phone='917899704294', photo=org.telegram.messenger.
    // TLRPC$TL_userProfilePhoto@42810730,
    // status=org.telegram.messenger.TLRPC$TL_userStatusOffline@428109f0, inactive=false}


    static {
        args = new Bundle();
        args.putInt("chat_id", BuildVars.CHAT_ID);
        args.putInt("user_id", BuildVars.USER_ID);
        args.putInt("message_id", BuildVars.MESSAGE_ID);
        args.putInt("enc_id", BuildVars.ENC_ID);
    }
    public static TLRPC.User defaultUser() {
        if (instanoUser == null) {
            instanoUser = new TLRPC.User();
            instanoUser.phone = "917899704294";
//            instanoUser.id = 105580237;
//            instanoUser.access_hash = Long.valueOf("2290130976748427084");
            instanoUser.first_name = "Instano";
            instanoUser.last_name = "Operator";
//            instanoUser.status = null;
//            instanoUser.photo = new TLRPC.TL_userProfilePhotoEmpty();
        }
        return instanoUser;
    }

    public static String mixpanelToken() {
        switch (BuildConfig.BUILD_TYPE) {
            case "release" :
            case "beta" :
            case "debug" :
                return "b9c5573bbc40cfd04cb921499e6456e1";
        }
        throw new IllegalStateException("wrong BuildConfig.BUILD_TYPE: " + BuildConfig.BUILD_TYPE);
    }

    public static String apiDomain() {
        return "http://staging.instanoapp.com/v1/";
    }
}
