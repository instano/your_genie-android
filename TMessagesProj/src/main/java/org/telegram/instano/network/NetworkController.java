package org.telegram.instano.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.android.volley.AuthFailureError;
import org.telegram.android.volley.RequestQueue;
import org.telegram.android.volley.Response;
import org.telegram.android.volley.VolleyError;
import org.telegram.android.volley.toolbox.JsonArrayRequest;
import org.telegram.android.volley.toolbox.JsonObjectRequest;
import org.telegram.android.volley.toolbox.Volley;
import org.telegram.instano.network.model.Order;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.TLRPC;
import org.telegram.messenger.UserConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rxJava.functions.Action1;

/**
 * Created by vedant on 6/28/15.
 */
public class NetworkController {
    private final String TAG = getClass().getSimpleName();
    private final static String KEY_SESSION_ID = "SessionId";
    private final static String KEY_USER_REGISTERED = "User registered";

    private static NetworkController instance;

    private RequestQueue requestQueue;

    public static NetworkController instance() {
        if (instance == null)
            instance = new NetworkController();
        return instance;
    }

    /**
     * fetches this users' orders from the server asynchronously
     * also, first signs in if required
     * @param callback for the list of orders. null is called, if an error occurred
     */
    public void fetchMyOrders(@NonNull final Action1<List<Order>> callback) {
        if (getSessionId().isEmpty()) {
            registerDevice(new Action1<Boolean>() {
                @Override
                public void call(Boolean success) {
                    if (success)
                        fetchMyOrders(callback);
                    else
                        callback.call(null);
                }
            });
            return; // first sign in
        }
        JsonArrayRequest request = new JsonArrayRequest(
                BuildVars.apiDomain() + "users/orders",
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        FileLog.d(TAG, String.valueOf(response));
                        callback.call(Order.parse(response));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        FileLog.e(TAG, error);
                        // try to refresh session id:
                        if (error.networkResponse != null) {
                            switch (error.networkResponse.statusCode) {
                                case HttpStatus.SC_FORBIDDEN: // device not registered
                                    FileLog.d(TAG, "fetchMyOrders.error.networkResponse.statusCode == HttpStatus.SC_FORBIDDEN");
                                    registerDevice(new Action1<Boolean>() { // first register device
                                        @Override
                                        public void call(Boolean success) {
                                            if (success) {
                                                registerUser(new Action1<Boolean>() { // then register user
                                                    @Override
                                                    public void call(Boolean success) {
                                                        if (success)
                                                            fetchMyOrders(callback); // then fetch orders
                                                        else callback.call(null);
                                                    }
                                                });
                                            } else callback.call(null);
                                        }
                                    });
                                    break;
                                case HttpStatus.SC_NOT_ACCEPTABLE: // user not registered
                                    FileLog.d(TAG, "error.networkResponse.statusCode == HttpStatus.SC_NOT_ACCEPTABLE");
                                    registerUser(new Action1<Boolean>() { // first register user
                                        @Override
                                        public void call(Boolean success) {
                                            if (success)
                                                fetchMyOrders(callback); // then fetch orders
                                            else callback.call(null);
                                        }
                                    });
                                    break;
                                default:
                                    callback.call(null);
                            }
                        } else
                            callback.call(null);
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Session-Id", getSessionId());
                return headers;
            }
        };
        requestQueue.add(request);
    }

    public void registerUserIfNeeded() {
        FileLog.d(TAG, "registerUserIfNeeded");
        if(!getSharedPrefrences().getBoolean(KEY_USER_REGISTERED, false))
            registerUser(null);
    }

    private void registerUser(@Nullable final Action1<Boolean> callback) {
        if (getSessionId().isEmpty()) {
            registerDevice(new Action1<Boolean>() {
                @Override
                public void call(Boolean success) {
                    if (success)
                        registerUser(callback);
                }
            });
            return; // first sign in
        }
        TLRPC.User currentUser = UserConfig.getCurrentUser();
        if (currentUser == null) {
            if (callback != null) {
                callback.call(false);
            }
            return;
        }
        try {
            JsonObjectRequest request = new JsonObjectRequest(
                    BuildVars.apiDomain() + "users",
                    new JSONObject().put("name", currentUser.fullName())
                            .put("telegram_no", currentUser.phone),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            FileLog.d(TAG, "successfully registered: " + response);
                            getSharedPrefrences().edit()
                                    .putBoolean(KEY_USER_REGISTERED, true)
                                    .apply();
                            if (callback != null)
                                callback.call(true);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            FileLog.e(TAG, error);
                            if (error.networkResponse != null && error.networkResponse.statusCode == HttpStatus.SC_FORBIDDEN) { // device not registered
                                FileLog.d(TAG, "registerUser.error.networkResponse.statusCode == HttpStatus.SC_FORBIDDEN");
                                registerDevice(new Action1<Boolean>() {
                                    @Override
                                    public void call(Boolean success) {
                                        if (success)
                                            registerUser(callback);
                                        else if (callback != null)
                                            callback.call(false);
                                    }
                                });
                            } else if (callback != null)
                                callback.call(false);
                        }
                    }
            ){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Session-Id", getSessionId());
                    return headers;
                }
            };
            requestQueue.add(request);
        } catch (JSONException e) {
            FileLog.fatal(e);
        }
    }

    public void registerDevice(@Nullable final Action1<Boolean> callback) {
        try {
            JsonObjectRequest request = new JsonObjectRequest(
                    BuildVars.apiDomain() + "devices",
                    new JSONObject().put("gcm_id", UserConfig.pushString),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                setSessionId(response.getString("session_id"));
                                if (callback != null)
                                    callback.call(true);
                            } catch (JSONException e) {
                                FileLog.e(TAG, e);
                                if (callback != null)
                                    callback.call(false);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            FileLog.e(TAG, error);
                            if (callback != null)
                                callback.call(false);
                        }
                    }
            );
            requestQueue.add(request);
        } catch (JSONException e) {
            FileLog.fatal(e);
        }
    }

    @NonNull
    private String getSessionId() {
        String string = getSharedPrefrences().getString(KEY_SESSION_ID, "");
        FileLog.d(TAG, "getSessionId: " + string);
        return string;
    }

    private void setSessionId(String string) {
        SharedPreferences.Editor editor = getSharedPrefrences().edit();
        FileLog.d(TAG, "getSessionId: " + string);
        editor.putString(KEY_SESSION_ID, string);
        editor.apply();
    }

    private SharedPreferences getSharedPrefrences() {
        Context context = ApplicationLoader.applicationContext;
        return context.getSharedPreferences(getClass().getSimpleName(), Context.MODE_PRIVATE);
    }

    private NetworkController() {
        requestQueue = Volley.newRequestQueue(ApplicationLoader.applicationContext);
    }
}
