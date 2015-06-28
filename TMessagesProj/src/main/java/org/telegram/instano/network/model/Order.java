package org.telegram.instano.network.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.FileLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vedant on 6/28/15.
 */
public class Order {
    private final static String TAG = "Order";

    public final String details;

    public static List<Order> parse(JSONArray jsonArray) {
        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                orders.add(parse(jsonObject));
            } catch (JSONException e) {
                FileLog.e(TAG, e);
            }
        }
        return orders;
    }

    public static Order parse(JSONObject jsonObject) throws JSONException {
        return new Order(
                jsonObject.getString("details")
        );
    }

    public Order(String details) {
        this.details = details;
    }
}
