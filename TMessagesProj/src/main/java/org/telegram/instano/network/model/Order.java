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
    public final String status;

    public static List<Order> parse(JSONArray jsonArray) {
        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                orders.add(parse(jsonObject));
            } catch (JSONException e) {
                FileLog.fatal(e);
            }
        }
        return orders;
    }

    public static Order parse(JSONObject jsonObject) throws JSONException {
        return new Order(
                jsonObject.getString("details"),
                jsonObject.getString("status")
        );
    }

    public Order(String details, String status) {
        this.details = details;
        this.status = status;
    }
}
