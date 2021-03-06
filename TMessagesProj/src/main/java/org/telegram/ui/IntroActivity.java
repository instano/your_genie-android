/*
 * This is the source code of Telegram for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package org.telegram.ui;

import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.appevents.AppEventsLogger;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.android.AndroidUtilities;
import org.telegram.android.LocaleController;
import org.telegram.instano.MixPanelEvents;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;

import java.util.ArrayList;

public class IntroActivity extends Activity {
    private ViewPager viewPager;
    private ImageView topImage1;
    private ImageView topImage2;
    private ViewGroup bottomPages;
    private int lastPage = 0;
    private boolean justCreated = false;
    private boolean startPressed = false;
    private int[] icons;
    private int[] titles;
    private int[] messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_TMessages);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        contactExists(BuildVars.PHONE);

        if (AndroidUtilities.isTablet()) {
            setContentView(R.layout.intro_layout_tablet);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setContentView(R.layout.intro_layout);
        }

        if (LocaleController.isRTL) {
            icons = new int[] {
                    R.drawable.intro7,
                    R.drawable.intro5,
                    R.drawable.intro4,
                    R.drawable.intro3,
                    R.drawable.intro2,
                    R.drawable.intro1
            };
            titles = new int[] {
                    R.string.Page7Title,
                    R.string.Page5Title,
                    R.string.Page4Title,
                    R.string.Page3Title,
                    R.string.Page2Title,
                    R.string.Page1Title
            };
            messages = new int[] {
                    R.string.Page7Message,
                    R.string.Page5Message,
                    R.string.Page4Message,
                    R.string.Page3Message,
                    R.string.Page2Message,
                    R.string.Page1Message
            };
        } else {
            icons = new int[] {

                    R.drawable.intro1,
                    R.drawable.intro2,
                    R.drawable.intro3,
                    R.drawable.intro4,
                    R.drawable.intro5,
                    R.drawable.intro7

            };
            titles = new int[] {
                    R.string.Page1Title,
                    R.string.Page2Title,
                    R.string.Page3Title,
                    R.string.Page4Title,
                    R.string.Page5Title,
                    R.string.Page7Title
            };
            messages = new int[] {
                    R.string.Page1Message,
                    R.string.Page2Message,
                    R.string.Page3Message,
                    R.string.Page4Message,
                    R.string.Page5Message,
                    R.string.Page7Message
            };
        }
        viewPager = (ViewPager)findViewById(R.id.intro_view_pager);
        TextView startMessagingButton = (TextView) findViewById(R.id.start_messaging_button);
        startMessagingButton.setText(LocaleController.getString("StartMessaging", R.string.GetStarted).toUpperCase());
        if (Build.VERSION.SDK_INT >= 21) {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(new int[] {android.R.attr.state_pressed}, ObjectAnimator.ofFloat(startMessagingButton, "translationZ", AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
            animator.addState(new int[] {}, ObjectAnimator.ofFloat(startMessagingButton, "translationZ", AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
            startMessagingButton.setStateListAnimator(animator);
        }
        topImage1 = (ImageView)findViewById(R.id.icon_image1);
        topImage2 = (ImageView)findViewById(R.id.icon_image2);
        bottomPages = (ViewGroup)findViewById(R.id.bottom_pages);
        topImage2.setVisibility(View.GONE);
        viewPager.setAdapter(new IntroAdapter());
        viewPager.setPageMargin(0);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {
                if (i == ViewPager.SCROLL_STATE_IDLE || i == ViewPager.SCROLL_STATE_SETTLING) {
                    JSONObject data = null;
                    try {
                        data = new JSONObject()
                                .put("current page", getResources().getString(titles[viewPager.getCurrentItem()]));
                    } catch (JSONException e) {
                        FileLog.e(BuildVars.TAG, e);
                    }
                    MixpanelAPI.getInstance(IntroActivity.this, BuildVars.mixpanelToken()).track(MixPanelEvents.INTROACTIVITY_ITEM_SCROLLED, data);
                    if (lastPage != viewPager.getCurrentItem()) {
                        lastPage = viewPager.getCurrentItem();

                        final ImageView fadeoutImage;
                        final ImageView fadeinImage;
                        if (topImage1.getVisibility() == View.VISIBLE) {
                            fadeoutImage = topImage1;
                            fadeinImage = topImage2;

                        } else {
                            fadeoutImage = topImage2;
                            fadeinImage = topImage1;
                        }

                        fadeinImage.bringToFront();
                        fadeinImage.setImageResource(icons[lastPage]);
                        fadeinImage.clearAnimation();
                        fadeoutImage.clearAnimation();


                        Animation outAnimation = AnimationUtils.loadAnimation(IntroActivity.this, R.anim.icon_anim_fade_out);
                        outAnimation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                fadeoutImage.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });

                        Animation inAnimation = AnimationUtils.loadAnimation(IntroActivity.this, R.anim.icon_anim_fade_in);
                        inAnimation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                fadeinImage.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });


                        fadeoutImage.startAnimation(outAnimation);
                        fadeinImage.startAnimation(inAnimation);
                    }
                }
            }
        });

        startMessagingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MixPanelEvents.api(IntroActivity.this).track(MixPanelEvents.GET_STARTED_CLICKED, null);
                if (startPressed) {
                    return;
                }
                startPressed = true;
                Intent intent2 = new Intent(IntroActivity.this, LaunchActivity.class);
                intent2.putExtra("fromIntro", true);
                startActivity(intent2);
                finish();
            }
        });

        justCreated = true;
        MixpanelAPI mixpanelAPI = MixPanelEvents.api(this);
        mixpanelAPI.track(MixPanelEvents.FIRST_TIME, null);
        if (MixPanelEvents.isPackageInstalled("org.telegram.messenger", this))
            mixpanelAPI.track(MixPanelEvents.TELEGRAM_PRESENT, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (justCreated) {
            if (LocaleController.isRTL) {
                viewPager.setCurrentItem(titles.length - 1);
                lastPage = titles.length - 1;
            } else {
                viewPager.setCurrentItem(0);
                lastPage = 0;
            }
            justCreated = false;
        }
//        Utilities.checkForCrashes(this);
//        Utilities.checkForUpdates(this);
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
    }

    private void contactExists(final String number) {
        // number is the phone number
        final long start = System.nanoTime();

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                Uri lookupUri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI,
                        Uri.encode(number));
                String[] mPhoneNumberProjection = { ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME };
                Cursor cur = getContentResolver().query(lookupUri,mPhoneNumberProjection, null, null, null);
                FileLog.d(BuildVars.TAG, "checking contacts : "+ cur.getCount());
                int count = cur.getCount();
                cur.close();

                ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

                if(count == 0) {
                    FileLog.d(BuildVars.TAG, "creating contact");

                    ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                            .build());
                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, BuildVars.CONTACT_NAME)
                            .build());

                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, BuildVars.PHONE)
                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                            .build());

                    try {
                        getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    } catch (OperationApplicationException e) {
                        e.printStackTrace();
                    }
                }
                double time = (System.nanoTime() - start)/1000_000.0;
                FileLog.d(BuildVars.TAG, String.format("time taken to create contact: %.3f", time));
                return null;
            }
        }.execute();
    }

    private class IntroAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = View.inflate(container.getContext(), R.layout.intro_view_layout, null);
            TextView headerTextView = (TextView)view.findViewById(R.id.header_text);
            TextView messageTextView = (TextView)view.findViewById(R.id.message_text);
            container.addView(view, 0);

            headerTextView.setText(getString(titles[position]));
            messageTextView.setText(AndroidUtilities.replaceTags(getString(messages[position])));

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            int count = bottomPages.getChildCount();
            for (int a = 0; a < count; a++) {
                View child = bottomPages.getChildAt(a);
                if (a == position) {
                    child.setBackgroundColor(0xff2ca5e0);
                } else {
                    child.setBackgroundColor(0xffbbbbbb);
                }
            }
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public void finishUpdate(View arg0) {
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            if (observer != null) {
                super.unregisterDataSetObserver(observer);
            }
        }
    }
}
