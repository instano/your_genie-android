package org.telegram.ui;

import android.content.Context;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.android.AndroidUtilities;
import org.telegram.instano.MixPanelEvents;
import org.telegram.instano.network.model.Order;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.LayoutListView;
import org.telegram.ui.Components.SizeNotifierRelativeLayout;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by Rohit on 30/6/15.
 */
public class MyOrdersActivity extends BaseFragment {

    private FrameLayout emptyViewContainer;
    private LayoutListView orderListView;
    private final static int id_chat_compose_panel = 1000;
    private OrderAdapter orderAdapter;
    private List<Order> mOrders;

    public MyOrdersActivity(List<Order> orders) {
        mOrders = orders;
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
    }

    @Override
    public View createView(Context context, LayoutInflater inflater) {
        hasOwnBackground = true;
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle("My orders");
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });


        fragmentView = new SizeNotifierRelativeLayout(context);
        SizeNotifierRelativeLayout contentView = (SizeNotifierRelativeLayout) fragmentView;

        contentView.setBackgroundImage(ApplicationLoader.getCachedWallpaper());

        emptyViewContainer = new FrameLayout(context);
        emptyViewContainer.setPadding(0, 0, 0, AndroidUtilities.dp(48));
        emptyViewContainer.setVisibility(View.VISIBLE);

        contentView.addView(emptyViewContainer);
        RelativeLayout.LayoutParams layoutParams3 = (RelativeLayout.LayoutParams) emptyViewContainer.getLayoutParams();
        layoutParams3.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        layoutParams3.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        emptyViewContainer.setLayoutParams(layoutParams3);
        emptyViewContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                FileLog.d("MyOrders: ","emptyViewContainer.setOnTouchListener");
                return true;
            }
        });

        orderListView = new LayoutListView(context);
        orderListView.setAdapter(orderAdapter = new OrderAdapter(context));
        orderListView.setCacheColorHint(ApplicationLoader.getSelectedColor());
        orderListView.setClipToPadding(false);
        orderListView.setStackFromBottom(false);
        orderListView.setPadding(0, AndroidUtilities.dp(4), 0, AndroidUtilities.dp(3));
        orderListView.setDivider(null);
        orderListView.setDividerHeight(10);
        orderListView.setSelector(R.drawable.transparent);
//        orderListView.setOnItemLongClickListener(onItemLongClickListener);
//        orderListView.setOnItemClickListener(onItemClickListener);
        contentView.addView(orderListView);
        layoutParams3 = (RelativeLayout.LayoutParams) orderListView.getLayoutParams();
        layoutParams3.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
        layoutParams3.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        layoutParams3.setMargins(0,5,15,10);
//        layoutParams3.addRule(RelativeLayout.BELOW, actionBar.getId());
        orderListView.setLayoutParams(layoutParams3);
        orderListView.setEmptyView(emptyViewContainer);
        orderListView.setVisibility(View.VISIBLE);
        for (int i = mOrders.size() -1; i>=0;i--) {
            FileLog.d("MyOrders: ", mOrders.get(i).details);
            orderAdapter.add(mOrders.get(i));
        }

        if (mOrders.size() == 0) {
            TextView emptyView = new TextView(context);
            emptyView.setText("No orders yet...");
            emptyView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            emptyView.setGravity(Gravity.CENTER);
            emptyView.setTextColor(0xffffffff);
            emptyView.setBackgroundResource(ApplicationLoader.isCustomTheme() ? R.drawable.system_black : R.drawable.system_blue);
            emptyView.setPadding(AndroidUtilities.dp(7), AndroidUtilities.dp(1), AndroidUtilities.dp(7), AndroidUtilities.dp(1));
            emptyViewContainer.addView(emptyView);
            FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) emptyView.getLayoutParams();
            layoutParams2.width = FrameLayout.LayoutParams.WRAP_CONTENT;
            layoutParams2.height = FrameLayout.LayoutParams.WRAP_CONTENT;
            layoutParams2.gravity = Gravity.CENTER;
            emptyView.setLayoutParams(layoutParams2);
            emptyView.setVisibility(View.VISIBLE);
            orderListView.setVisibility(View.GONE);
        }
        return fragmentView;
    }

    private class OrderAdapter extends ArrayAdapter<Order> {

        private LayoutInflater mInflater;
        ViewHolder viewHolder ;
        public OrderAdapter(Context context) {
            super(context, 0);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final Order order = getItem(position);
            convertView = mInflater.inflate(R.layout.order_list_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);

            View facebookShare = convertView.findViewById(R.id.facebook_share);
            View twitterShare = convertView.findViewById(R.id.twitter_share);
            View reOrder = convertView.findViewById(R.id.re_order);

            facebookShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MixpanelAPI.getInstance(getContext(), BuildVars.mixpanelToken()).track(MixPanelEvents.FACEBOOK_SHARE_CLICKED, null);
                    ShareDialog shareDialog = new ShareDialog(getParentActivity());
                    ShareLinkContent linkContent = new ShareLinkContent.Builder()
                            .setContentUrl(Uri.parse("https://play.google.com/store/apps/details?id=com.instano.genie"))
                            .setContentTitle("My Order")
                            .setContentDescription("Order Details")
                            .build();
                    shareDialog.show(linkContent);
                }
            });

            twitterShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MixpanelAPI.getInstance(getContext(), BuildVars.mixpanelToken()).track(MixPanelEvents.TWITTER_SHARE_CLICKED, null);
                    try {
                        URL url = new URL("https://play.google.com/store/apps/details?id=com.instano.genie");
                        TweetComposer.Builder builder = new TweetComposer.Builder(getContext())
                                .text("Fabulous shopping experience. \nOrdered: Burger")
                                .url(url);

                        builder.show();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            });

            reOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    JSONObject reOrdered = new JSONObject();
                    try {
                        reOrdered.put("order_id", order.details);
                    } catch (JSONException e) {
                        FileLog.e(BuildVars.TAG, e);
                    }
                    MixpanelAPI.getInstance(getContext(), BuildVars.mixpanelToken()).track(MixPanelEvents.REORDER_CLICKED, reOrdered);

                }
            });

            TextView tv = (TextView) convertView.findViewById(R.id.order);
            tv.setText(viewHolder.setFormattedText(order)+"\nPrice: Rs. 100 \nAddress: Empire Hotel Koramangala");
            return convertView;
        }
    }

    public class ViewHolder{

        public Spannable setFormattedText(Order order){
            String time = new SimpleDateFormat("h:mm a", Locale.US).format(new java.util.Date(System.currentTimeMillis()));
            Spannable messageSpannable = new SpannableString(order.details + '\n' + time);
            messageSpannable.setSpan(new ForegroundColorSpan(R.color.primary), messageSpannable.length() - time.length(),
                    messageSpannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            messageSpannable.setSpan(new RelativeSizeSpan(0.8f), messageSpannable.length() - time.length(),
                    messageSpannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            return  messageSpannable;
        }

        public ViewHolder(View view){

        }
    }
}