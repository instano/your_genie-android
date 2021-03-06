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
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.android.AndroidUtilities;
import org.telegram.android.ContactsController;
import org.telegram.android.LocaleController;
import org.telegram.android.MediaController;
import org.telegram.android.MessageObject;
import org.telegram.android.MessagesController;
import org.telegram.android.MessagesStorage;
import org.telegram.android.NotificationCenter;
import org.telegram.instano.MixPanelEvents;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ConnectionsManager;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;
import org.telegram.messenger.RPCRequest;
import org.telegram.messenger.TLObject;
import org.telegram.messenger.TLRPC;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Adapters.BaseFragmentAdapter;
import org.telegram.ui.AnimationCompat.ViewProxy;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextInfoCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.AvatarUpdater;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.NumberPicker;

import java.util.ArrayList;
import java.util.Locale;

public class SettingsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate, PhotoViewer.PhotoViewerProvider {

    private ListView listView;
    private ListAdapter listAdapter;
    private BackupImageView avatarImage;
    private TextView nameTextView;
    private TextView onlineTextView;
    private ImageView writeButton;
    private AvatarUpdater avatarUpdater = new AvatarUpdater();

    private int overscrollRow;
    private int emptyRow;
    private int numberSectionRow;
    private int numberRow;
    private int usernameRow;
    private int settingsSectionRow;
    private int settingsSectionRow2;
    private int enableAnimationsRow;
    private int notificationRow;
    private int backgroundRow;
    private int languageRow;
    private int privacyRow;
    private int mediaDownloadSection;
    private int mediaDownloadSection2;
    private int mobileDownloadRow;
    private int wifiDownloadRow;
    private int roamingDownloadRow;
    private int saveToGalleryRow;
    private int messagesSectionRow;
    private int messagesSectionRow2;
    private int textSizeRow;
    private int sendByEnterRow;
    private int versionRow;
    private int rowCount;

    private final static int logout = 2;

    private static class LinkMovementMethodMy extends LinkMovementMethod {
        @Override
        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
            try {
                return super.onTouchEvent(widget, buffer, event);
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
            return false;
        }
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        avatarUpdater.parentFragment = this;
        avatarUpdater.delegate = new AvatarUpdater.AvatarUpdaterDelegate() {
            @Override
            public void didUploadedPhoto(TLRPC.InputFile file, TLRPC.PhotoSize small, TLRPC.PhotoSize big) {
                TLRPC.TL_photos_uploadProfilePhoto req = new TLRPC.TL_photos_uploadProfilePhoto();
                req.caption = "";
                req.crop = new TLRPC.TL_inputPhotoCropAuto();
                req.file = file;
                req.geo_point = new TLRPC.TL_inputGeoPointEmpty();
                ConnectionsManager.getInstance().performRpc(req, new RPCRequest.RPCRequestDelegate() {
                    @Override
                    public void run(TLObject response, TLRPC.TL_error error) {
                        if (error == null) {
                            TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
                            if (user == null) {
                                user = UserConfig.getCurrentUser();
                                if (user == null) {
                                    return;
                                }
                                MessagesController.getInstance().putUser(user, false);
                            } else {
                                UserConfig.setCurrentUser(user);
                            }
                            if (user == null) {
                                return;
                            }
                            TLRPC.TL_photos_photo photo = (TLRPC.TL_photos_photo)response;
                            ArrayList<TLRPC.PhotoSize> sizes = photo.photo.sizes;
                            TLRPC.PhotoSize smallSize = FileLoader.getClosestPhotoSizeWithSize(sizes, 100);
                            TLRPC.PhotoSize bigSize = FileLoader.getClosestPhotoSizeWithSize(sizes, 1000);
                            user.photo = new TLRPC.TL_userProfilePhoto();
                            user.photo.photo_id = photo.photo.id;
                            if (smallSize != null) {
                                user.photo.photo_small = smallSize.location;
                            }
                            if (bigSize != null) {
                                user.photo.photo_big = bigSize.location;
                            } else if (smallSize != null) {
                                user.photo.photo_small = smallSize.location;
                            }
                            MessagesStorage.getInstance().clearUserPhotos(user.id);
                            ArrayList<TLRPC.User> users = new ArrayList<>();
                            users.add(user);
                            MessagesStorage.getInstance().putUsersAndChats(users, null, false, true);
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                @Override
                                public void run() {
                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateInterfaces, MessagesController.UPDATE_MASK_ALL);
                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.mainUserInfoChanged);
                                    UserConfig.saveConfig(true);
                                }
                            });
                        }
                    }
                });
            }
        };
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);

        rowCount = 0;
        overscrollRow = rowCount++;
        emptyRow = rowCount++;
        numberSectionRow = rowCount++;
        numberRow = rowCount++;
        usernameRow = rowCount++;
        settingsSectionRow = rowCount++;
        settingsSectionRow2 = rowCount++;
        notificationRow = rowCount++;
        privacyRow = rowCount++;
        backgroundRow = rowCount++;
        languageRow = rowCount++;
        enableAnimationsRow = rowCount++;
        mediaDownloadSection = rowCount++;
        mediaDownloadSection2 = rowCount++;
        mobileDownloadRow = rowCount++;
        wifiDownloadRow = rowCount++;
        roamingDownloadRow = rowCount++;
        saveToGalleryRow = rowCount++;
        messagesSectionRow = rowCount++;
        messagesSectionRow2 = rowCount++;
        textSizeRow = rowCount++;
        sendByEnterRow = rowCount++;

        versionRow = rowCount++;

        MessagesController.getInstance().loadFullUser(UserConfig.getCurrentUser(), classGuid);

        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (avatarImage != null) {
            avatarImage.setImageDrawable(null);
        }
        MessagesController.getInstance().cancelLoadFullUser(UserConfig.getClientUserId());
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateInterfaces);
        avatarUpdater.clear();
    }

    @Override
    public boolean needAddActionBar() {
        return false;
    }

    @Override
    public View createView(final Context context, LayoutInflater inflater) {
        actionBar.setBackgroundColor(AvatarDrawable.getProfileBackColorForId(5));
        actionBar.setItemsBackground(AvatarDrawable.getButtonColorForId(5));
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setExtraHeight(AndroidUtilities.dp(88), false);
        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
//                    Fragment currentFragment = getParentActivity().getFragmentManager().findFragmentByTag("ChatActivity");
//                    FragmentTransaction fragTransaction = getParentActivity().getFragmentManager().beginTransaction();
//                    fragTransaction.detach();
//                    fragTransaction.attach();
//                    fragTransaction.commit();
                    finishFragment();
                } else if (id == logout) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setMessage(LocaleController.getString("AreYouSureLogout", R.string.AreYouSureLogout));
                    builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                    builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.clear().commit();
                            MessagesController.getInstance().unregistedPush();
                            MessagesController.getInstance().logOut();
                            UserConfig.clearConfig();
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.appDidLogout);
                            MessagesStorage.getInstance().cleanUp(false);
                            MessagesController.getInstance().cleanUp();
                            ContactsController.getInstance().deleteAllAppAccounts();
                        }
                    });
                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                    showAlertDialog(builder);
                }
            }
        });
        ActionBarMenu menu = actionBar.createMenu();
        ActionBarMenuItem item = menu.addItem(0, R.drawable.ic_ab_other);
        item.addSubItem(logout, LocaleController.getString("LogOut", R.string.LogOut), 0);

        listAdapter = new ListAdapter(context);

        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        avatarImage = new BackupImageView(context);
        avatarImage.setRoundRadius(AndroidUtilities.dp(30));
        actionBar.addView(avatarImage);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) avatarImage.getLayoutParams();
        layoutParams.gravity = (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.BOTTOM;
        layoutParams.width = AndroidUtilities.dp(60);
        layoutParams.height = AndroidUtilities.dp(60);
        layoutParams.leftMargin = LocaleController.isRTL ? 0 : AndroidUtilities.dp(17);
        layoutParams.rightMargin = LocaleController.isRTL ? AndroidUtilities.dp(17) : 0;
        layoutParams.bottomMargin = AndroidUtilities.dp(22);
        avatarImage.setLayoutParams(layoutParams);
        avatarImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
                if (user.photo != null && user.photo.photo_big != null) {
                    PhotoViewer.getInstance().setParentActivity(getParentActivity());
                    PhotoViewer.getInstance().openPhoto(user.photo.photo_big, SettingsActivity.this);
                }
            }
        });

        nameTextView = new TextView(context);
        nameTextView.setTextColor(0xffffffff);
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        nameTextView.setLines(1);
        nameTextView.setMaxLines(1);
        nameTextView.setSingleLine(true);
        nameTextView.setEllipsize(TextUtils.TruncateAt.END);
        nameTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT));
        nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        actionBar.addView(nameTextView);
        layoutParams = (FrameLayout.LayoutParams) nameTextView.getLayoutParams();
        layoutParams.width = FrameLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.leftMargin = AndroidUtilities.dp(LocaleController.isRTL ? 16 : 97);
        layoutParams.rightMargin = AndroidUtilities.dp(LocaleController.isRTL ? 97 : 16);
        layoutParams.bottomMargin = AndroidUtilities.dp(51);
        layoutParams.gravity = (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.BOTTOM;
        nameTextView.setLayoutParams(layoutParams);

        onlineTextView = new TextView(context);
        onlineTextView.setTextColor(AvatarDrawable.getProfileTextColorForId(5));
        onlineTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        onlineTextView.setLines(1);
        onlineTextView.setMaxLines(1);
        onlineTextView.setSingleLine(true);
        onlineTextView.setEllipsize(TextUtils.TruncateAt.END);
        onlineTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT));
        actionBar.addView(onlineTextView);
        layoutParams = (FrameLayout.LayoutParams) onlineTextView.getLayoutParams();
        layoutParams.width = FrameLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.leftMargin = AndroidUtilities.dp(LocaleController.isRTL ? 16 : 97);
        layoutParams.rightMargin = AndroidUtilities.dp(LocaleController.isRTL ? 97 : 16);
        layoutParams.bottomMargin = AndroidUtilities.dp(30);
        layoutParams.gravity = (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.BOTTOM;
        onlineTextView.setLayoutParams(layoutParams);

        listView = new ListView(context);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setVerticalScrollBarEnabled(false);
        AndroidUtilities.setListViewEdgeEffectColor(listView, AvatarDrawable.getProfileBackColorForId(5));
        frameLayout.addView(listView);
        layoutParams = (FrameLayout.LayoutParams) listView.getLayoutParams();
        layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT;
        layoutParams.gravity = Gravity.TOP;
        listView.setLayoutParams(layoutParams);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                if (i == textSizeRow) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    MixpanelAPI.getInstance(context,BuildVars.mixpanelToken()).track(MixPanelEvents.SETTINGS_MESSAGE_TEXT_SIZE,null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("TextSize", R.string.TextSize));
                    final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                    numberPicker.setMinValue(12);
                    numberPicker.setMaxValue(30);
                    numberPicker.setValue(MessagesController.getInstance().fontSize);
                    builder.setView(numberPicker);
                    builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putInt("fons_size", numberPicker.getValue());
                            MessagesController.getInstance().fontSize = numberPicker.getValue();
                            editor.commit();
                            if (listView != null) {
                                listView.invalidateViews();
                            }
                        }
                    });
                    showAlertDialog(builder);
                } else if (i == enableAnimationsRow) {
                    MixpanelAPI.getInstance(context,BuildVars.mixpanelToken()).track(MixPanelEvents.SETTINGS_ANIMATIONS,null);
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                    boolean animations = preferences.getBoolean("view_animations", true);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("view_animations", !animations);
                    editor.commit();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!animations);
                    }
                } else if (i == notificationRow) {
                    MixpanelAPI.getInstance(context,BuildVars.mixpanelToken()).track(MixPanelEvents.SETTINGS_NOTIFICATIONS_SOUNDS,null);
                    presentFragment(new NotificationsSettingsActivity());
                } else if (i == backgroundRow) {
                    MixpanelAPI.getInstance(context,BuildVars.mixpanelToken()).track(MixPanelEvents.SETTINGS_CHAT_BACKGROUND,null);
                    presentFragment(new WallpapersActivity());
                } else if (i == sendByEnterRow) {
                    MixpanelAPI.getInstance(context,BuildVars.mixpanelToken()).track(MixPanelEvents.SETTINGS_SEND_BY_ENTER,null);
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                    boolean send = preferences.getBoolean("send_by_enter", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("send_by_enter", !send);
                    editor.commit();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!send);
                    }
                } else if (i == saveToGalleryRow) {
                    MixpanelAPI.getInstance(context,BuildVars.mixpanelToken()).track(MixPanelEvents.SETTINGS_SAVE_TO_GALLERY,null);
                    MediaController.getInstance().toggleSaveToGallery();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(MediaController.getInstance().canSaveToGallery());
                    }
                } else if (i == privacyRow) {
                    MixpanelAPI.getInstance(context,BuildVars.mixpanelToken()).track(MixPanelEvents.SETTINGS_SECURITY,null);
                    presentFragment(new SecuritySettingsActivity());
                } else if (i == languageRow) {
                    MixpanelAPI.getInstance(context,BuildVars.mixpanelToken()).track(MixPanelEvents.SETTINGS_LANGUAGE,null);
//                    presentFragment(new LanguageSelectActivity());
//                } else if (i == contactsSortRow) {
//                    if (getParentActivity() == null) {
//                        return;
//                    }
//                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
//                    builder.setTitle(LocaleController.getString("SortBy", R.string.SortBy));
//                    builder.setItems(new CharSequence[]{
//                            LocaleController.getString("Default", R.string.Default),
//                            LocaleController.getString("SortFirstName", R.string.SortFirstName),
//                            LocaleController.getString("SortLastName", R.string.SortLastName)
//                    }, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
//                            SharedPreferences.Editor editor = preferences.edit();
//                            editor.putInt("sortContactsBy", which);
//                            editor.commit();
//                            if (listView != null) {
//                                listView.invalidateViews();
//                            }
//                        }
//                    });
//                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
//                    showAlertDialog(builder);
                } else if (i == wifiDownloadRow || i == mobileDownloadRow || i == roamingDownloadRow) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());

                    int mask = 0;
                    if (i == mobileDownloadRow) {
                        MixpanelAPI.getInstance(context,BuildVars.mixpanelToken()).track(MixPanelEvents.SETTINGS_USING_MOBILE_DATA,null);
                        builder.setTitle(LocaleController.getString("WhenUsingMobileData", R.string.WhenUsingMobileData));
                        mask = MediaController.getInstance().mobileDataDownloadMask;
                    } else if (i == wifiDownloadRow) {
                        MixpanelAPI.getInstance(context,BuildVars.mixpanelToken()).track(MixPanelEvents.SETTINGS_USING_WI_FI,null);
                        builder.setTitle(LocaleController.getString("WhenConnectedOnWiFi", R.string.WhenConnectedOnWiFi));
                        mask = MediaController.getInstance().wifiDownloadMask;
                    } else if (i == roamingDownloadRow) {
                        MixpanelAPI.getInstance(context,BuildVars.mixpanelToken()).track(MixPanelEvents.SETTINGS_USING_ROAMING,null);
                        builder.setTitle(LocaleController.getString("WhenRoaming", R.string.WhenRoaming));
                        mask = MediaController.getInstance().roamingDownloadMask;
                    }
                    builder.setMultiChoiceItems(
                            new CharSequence[]{LocaleController.getString("AttachPhoto", R.string.AttachPhoto), LocaleController.getString("AttachAudio", R.string.AttachAudio), LocaleController.getString("AttachVideo", R.string.AttachVideo), LocaleController.getString("AttachDocument", R.string.AttachDocument)},
                            new boolean[]{(mask & MediaController.AUTODOWNLOAD_MASK_PHOTO) != 0, (mask & MediaController.AUTODOWNLOAD_MASK_AUDIO) != 0, (mask & MediaController.AUTODOWNLOAD_MASK_VIDEO) != 0, (mask & MediaController.AUTODOWNLOAD_MASK_DOCUMENT) != 0},
                            new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                    int mask = 0;
                                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    if (i == mobileDownloadRow) {
                                        mask = MediaController.getInstance().mobileDataDownloadMask;
                                    } else if (i == wifiDownloadRow) {
                                        mask = MediaController.getInstance().wifiDownloadMask;
                                    } else if (i == roamingDownloadRow) {
                                        mask = MediaController.getInstance().roamingDownloadMask;
                                    }

                                    int maskDiff = 0;
                                    if (which == 0) {
                                        maskDiff = MediaController.AUTODOWNLOAD_MASK_PHOTO;
                                    } else if (which == 1) {
                                        maskDiff = MediaController.AUTODOWNLOAD_MASK_AUDIO;
                                    } else if (which == 2) {
                                        maskDiff = MediaController.AUTODOWNLOAD_MASK_VIDEO;
                                    } else if (which == 3) {
                                        maskDiff = MediaController.AUTODOWNLOAD_MASK_DOCUMENT;
                                    }

                                    if (isChecked) {
                                        mask |= maskDiff;
                                    } else {
                                        mask &= ~maskDiff;
                                    }

                                    if (i == mobileDownloadRow) {
                                        editor.putInt("mobileDataDownloadMask", mask);
                                        mask = MediaController.getInstance().mobileDataDownloadMask = mask;
                                    } else if (i == wifiDownloadRow) {
                                        editor.putInt("wifiDownloadMask", mask);
                                        MediaController.getInstance().wifiDownloadMask = mask;
                                    } else if (i == roamingDownloadRow) {
                                        editor.putInt("roamingDownloadMask", mask);
                                        MediaController.getInstance().roamingDownloadMask = mask;
                                    }
                                    editor.commit();
                                    if (listView != null) {
                                        listView.invalidateViews();
                                    }
                                }
                            });
                    builder.setNegativeButton(LocaleController.getString("OK", R.string.OK), null);
                    showAlertDialog(builder);
                } else if (i == usernameRow) {
                    MixpanelAPI.getInstance(context,BuildVars.mixpanelToken()).track(MixPanelEvents.SETTINGS_USER_NAME,null);
                    presentFragment(new ChangeNameActivity());
                } else if (i == numberRow) {
                    MixpanelAPI.getInstance(context,BuildVars.mixpanelToken()).track(MixPanelEvents.SETTINGS_USER_PHONE_NUMBER,null);
                    presentFragment(new ChangePhoneHelpActivity());
                }
            }
        });

        frameLayout.addView(actionBar);

        writeButton = new ImageView(context);
        writeButton.setBackgroundResource(R.drawable.floating_user_states);
        writeButton.setImageResource(R.drawable.floating_camera);
        writeButton.setScaleType(ImageView.ScaleType.CENTER);
        if (Build.VERSION.SDK_INT >= 21) {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(writeButton, "translationZ", AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
            animator.addState(new int[]{}, ObjectAnimator.ofFloat(writeButton, "translationZ", AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
            writeButton.setStateListAnimator(animator);
            writeButton.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, AndroidUtilities.dp(56), AndroidUtilities.dp(56));
                }
            });
        }
        frameLayout.addView(writeButton);
        layoutParams = (FrameLayout.LayoutParams) writeButton.getLayoutParams();
        layoutParams.width = FrameLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.leftMargin = AndroidUtilities.dp(LocaleController.isRTL ? 16 : 0);
        layoutParams.rightMargin = AndroidUtilities.dp(LocaleController.isRTL ? 0 : 16);
        layoutParams.gravity = (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT);
        writeButton.setLayoutParams(layoutParams);
        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getParentActivity() == null) {
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());

                CharSequence[] items;

                TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
                if (user == null) {
                    user = UserConfig.getCurrentUser();
                }
                if (user == null) {
                    return;
                }
                boolean fullMenu = false;
                if (user.photo != null && user.photo.photo_big != null && !(user.photo instanceof TLRPC.TL_userProfilePhotoEmpty)) {
                    items = new CharSequence[]{LocaleController.getString("FromCamera", R.string.FromCamera), LocaleController.getString("FromGalley", R.string.FromGalley), LocaleController.getString("DeletePhoto", R.string.DeletePhoto)};
                    fullMenu = true;
                } else {
                    items = new CharSequence[]{LocaleController.getString("FromCamera", R.string.FromCamera), LocaleController.getString("FromGalley", R.string.FromGalley)};
                }

                final boolean full = fullMenu;
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            avatarUpdater.openCamera();
                        } else if (i == 1) {
                            avatarUpdater.openGallery();
                        } else if (i == 2) {
                            MessagesController.getInstance().deleteUserPhoto(null);
                        }
                    }
                });
                showAlertDialog(builder);
            }
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount == 0) {
                    return;
                }
                int height = 0;
                View child = view.getChildAt(0);
                if (child != null) {
                    if (firstVisibleItem == 0) {
                        height = AndroidUtilities.dp(88) + (child.getTop() < 0 ? child.getTop() : 0);
                    }
                    if (actionBar.getExtraHeight() != height) {
                        actionBar.setExtraHeight(height, true);
                        needLayout();
                    }
                }
            }
        });

        return fragmentView;
    }

    @Override
    protected void onDialogDismiss() {
        MediaController.getInstance().checkAutodownloadSettings();
    }

    @Override
    public void updatePhotoAtIndex(int index) {

    }

    @Override
    public PhotoViewer.PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {
        if (fileLocation == null) {
            return null;
        }
        TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
        if (user != null && user.photo != null && user.photo.photo_big != null) {
            TLRPC.FileLocation photoBig = user.photo.photo_big;
            if (photoBig.local_id == fileLocation.local_id && photoBig.volume_id == fileLocation.volume_id && photoBig.dc_id == fileLocation.dc_id) {
                int coords[] = new int[2];
                avatarImage.getLocationInWindow(coords);
                PhotoViewer.PlaceProviderObject object = new PhotoViewer.PlaceProviderObject();
                object.viewX = coords[0];
                object.viewY = coords[1] - AndroidUtilities.statusBarHeight;
                object.parentView = avatarImage;
                object.imageReceiver = avatarImage.getImageReceiver();
                object.user_id = UserConfig.getClientUserId();
                object.thumb = object.imageReceiver.getBitmap();
                object.size = -1;
                object.radius = avatarImage.getImageReceiver().getRoundRadius();
                return object;
            }
        }
        return null;
    }

    @Override
    public Bitmap getThumbForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {
        return null;
    }

    @Override
    public void willSwitchFromPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) { }

    @Override
    public void willHidePhotoViewer() {
        avatarImage.getImageReceiver().setVisible(true, true);
    }

    @Override
    public boolean isPhotoChecked(int index) { return false; }

    @Override
    public void setPhotoChecked(int index) { }

    @Override
    public void cancelButtonPressed() { }

    @Override
    public void sendButtonPressed(int index) { }

    @Override
    public int getSelectedCount() { return 0; }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        avatarUpdater.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void saveSelfArgs(Bundle args) {
        if (avatarUpdater != null && avatarUpdater.currentPicturePath != null) {
            args.putString("path", avatarUpdater.currentPicturePath);
        }
    }

    @Override
    public void restoreSelfArgs(Bundle args) {
        if (avatarUpdater != null) {
            avatarUpdater.currentPicturePath = args.getString("path");
        }
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.updateInterfaces) {
            int mask = (Integer)args[0];
            if ((mask & MessagesController.UPDATE_MASK_AVATAR) != 0 || (mask & MessagesController.UPDATE_MASK_NAME) != 0) {
                updateUserData();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
        updateUserData();
        fixLayout();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        fixLayout();
    }

    private void needLayout() {
        FrameLayout.LayoutParams layoutParams;
        if (listView != null) {
            layoutParams = (FrameLayout.LayoutParams) listView.getLayoutParams();
            layoutParams.topMargin = (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + AndroidUtilities.getCurrentActionBarHeight();
            listView.setLayoutParams(layoutParams);
        }

        if (avatarImage != null) {
            float diff = actionBar.getExtraHeight() / (float)AndroidUtilities.dp(88);
            float diffm = 1.0f - diff;

            int avatarSize = 42 + (int)(18 * diff);
            int avatarX = 17 + (int)(47 * diffm);
            int avatarY = AndroidUtilities.dp(22) - (int)((AndroidUtilities.dp(22) - (AndroidUtilities.getCurrentActionBarHeight() - AndroidUtilities.dp(42)) / 2) * (1.0f - diff));
            int nameX = 97 + (int)(21 * diffm);
            int nameEndX = 16 + (int)(32 * diffm);
            int nameY = avatarY + AndroidUtilities.dp(29 - 13 * diffm);
            int statusY = avatarY + AndroidUtilities.dp(8 - 7 * diffm);
            float scale = 1.0f - 0.12f * diffm;

            layoutParams = (FrameLayout.LayoutParams) writeButton.getLayoutParams();
            layoutParams.topMargin = (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + AndroidUtilities.getCurrentActionBarHeight() + actionBar.getExtraHeight() - AndroidUtilities.dp(29.5f);
            writeButton.setLayoutParams(layoutParams);
            ViewProxy.setAlpha(writeButton, diff);
            writeButton.setVisibility(diff <= 0.02 ? View.GONE : View.VISIBLE);
            if (writeButton.getVisibility() == View.GONE) {
                writeButton.clearAnimation();
            }

            avatarImage.setRoundRadius(AndroidUtilities.dp(avatarSize / 2));
            layoutParams = (FrameLayout.LayoutParams) avatarImage.getLayoutParams();
            layoutParams.width = AndroidUtilities.dp(avatarSize);
            layoutParams.height = AndroidUtilities.dp(avatarSize);
            layoutParams.leftMargin = LocaleController.isRTL ? 0 : AndroidUtilities.dp(avatarX);
            layoutParams.rightMargin = LocaleController.isRTL ? AndroidUtilities.dp(avatarX) : 0;
            layoutParams.bottomMargin = avatarY;
            avatarImage.setLayoutParams(layoutParams);

            ViewProxy.setPivotX(nameTextView, 0);
            ViewProxy.setPivotY(nameTextView, 0);
            ViewProxy.setScaleX(nameTextView, scale);
            ViewProxy.setScaleY(nameTextView, scale);
            layoutParams = (FrameLayout.LayoutParams) nameTextView.getLayoutParams();
            layoutParams.leftMargin = AndroidUtilities.dp(LocaleController.isRTL ? nameEndX : nameX);
            layoutParams.rightMargin = AndroidUtilities.dp(LocaleController.isRTL ? nameX : nameEndX);
            layoutParams.bottomMargin = nameY;
            nameTextView.setLayoutParams(layoutParams);

            layoutParams = (FrameLayout.LayoutParams) onlineTextView.getLayoutParams();
            layoutParams.leftMargin = AndroidUtilities.dp(LocaleController.isRTL ? nameEndX : nameX);
            layoutParams.rightMargin = AndroidUtilities.dp(LocaleController.isRTL ? nameX : nameEndX);
            layoutParams.bottomMargin = statusY;
            onlineTextView.setLayoutParams(layoutParams);
        }
    }

    private void fixLayout() {
        if (fragmentView == null) {
            return;
        }
        fragmentView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (fragmentView != null) {
                    needLayout();
                    fragmentView.getViewTreeObserver().removeOnPreDrawListener(this);
                }
                return false;
            }
        });
    }

    private void updateUserData() {
        TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
        TLRPC.FileLocation photo = null;
        TLRPC.FileLocation photoBig = null;
        if (user.photo != null) {
            photo = user.photo.photo_small;
            photoBig = user.photo.photo_big;
        }
        AvatarDrawable avatarDrawable = new AvatarDrawable(user, true);
        avatarDrawable.setColor(0xff5c98cd);
        if (avatarImage != null) {
            avatarImage.setImage(photo, "50_50", avatarDrawable);
            avatarImage.getImageReceiver().setVisible(!PhotoViewer.getInstance().isShowingImage(photoBig), false);

            nameTextView.setText(ContactsController.formatName(user.first_name, user.last_name));
            onlineTextView.setText(LocaleController.getString("Online", R.string.Online));

            avatarImage.getImageReceiver().setVisible(!PhotoViewer.getInstance().isShowingImage(photoBig), false);
        }
    }

    private class ListAdapter extends BaseFragmentAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int i) {
            return i == textSizeRow || i == enableAnimationsRow || i == notificationRow || i == backgroundRow ||
                i == numberRow || i == sendByEnterRow || i == privacyRow || i == wifiDownloadRow ||
                i == mobileDownloadRow || i == roamingDownloadRow || i == languageRow || i == usernameRow ||
                i == saveToGalleryRow;
        }

        @Override
        public int getCount() {
            return rowCount;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            int type = getItemViewType(i);
            if (type == 0) {
                if (view == null) {
                    view = new EmptyCell(mContext);
                }
                if (i == overscrollRow) {
                    ((EmptyCell) view).setHeight(88);
                } else {
                    ((EmptyCell) view).setHeight(16);
                }
            } else if (type == 1) {
                if (view == null) {
                    view = new ShadowSectionCell(mContext);
                }
            } else if (type == 2) {
                if (view == null) {
                    view = new TextSettingsCell(mContext);
                }
                TextSettingsCell textCell = (TextSettingsCell) view;
                if (i == textSizeRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                    int size = preferences.getInt("fons_size", AndroidUtilities.isTablet() ? 18 : 16);
                    textCell.setTextAndValue(LocaleController.getString("TextSize", R.string.TextSize), String.format("%d", size), true);
                } else if (i == languageRow) {
                    textCell.setTextAndValue(LocaleController.getString("Language", R.string.Language), LocaleController.getCurrentLanguageName(), true);
//                }  else if (i == contactsSortRow) {
//                    String value;
//                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
//                    int sort = preferences.getInt("sortContactsBy", 0);
//                    if (sort == 0) {
//                        value = LocaleController.getString("Default", R.string.Default);
//                    } else if (sort == 1) {
//                        value = LocaleController.getString("FirstName", R.string.SortFirstName);
//                    } else {
//                        value = LocaleController.getString("LastName", R.string.SortLastName);
//                    }
//                    textCell.setTextAndValue(LocaleController.getString("SortBy", R.string.SortBy), value, true);
                } else if (i == notificationRow) {
                    textCell.setText(LocaleController.getString("NotificationsAndSounds", R.string.NotificationsAndSounds), true);
                } else if (i == backgroundRow) {
                    textCell.setText(LocaleController.getString("ChatBackground", R.string.ChatBackground), true);
                } else if (i == privacyRow) {
                    textCell.setText(LocaleController.getString("SecuritySettings", R.string.PrivacySettings), true);
                }
            } else if (type == 3) {
                if (view == null) {
                    view = new TextCheckCell(mContext);
                }
                TextCheckCell textCell = (TextCheckCell) view;

                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                if (i == enableAnimationsRow) {
                    textCell.setTextAndCheck(LocaleController.getString("EnableAnimations", R.string.EnableAnimations), preferences.getBoolean("view_animations", true), false);
                } else if (i == sendByEnterRow) {
                    textCell.setTextAndCheck(LocaleController.getString("SendByEnter", R.string.SendByEnter), preferences.getBoolean("send_by_enter", false), false);
                } else if (i == saveToGalleryRow) {
                    textCell.setTextAndCheck(LocaleController.getString("SaveToGallerySettings", R.string.SaveToGallerySettings), MediaController.getInstance().canSaveToGallery(), false);
                }
            } else if (type == 4) {
                if (view == null) {
                    view = new HeaderCell(mContext);
                }
                if (i == settingsSectionRow2) {
                    ((HeaderCell) view).setText(LocaleController.getString("SETTINGS", R.string.SETTINGS));
                } else if (i == messagesSectionRow2) {
                    ((HeaderCell) view).setText(LocaleController.getString("MessagesSettings", R.string.MessagesSettings));
                } else if (i == mediaDownloadSection2) {
                    ((HeaderCell) view).setText(LocaleController.getString("AutomaticMediaDownload", R.string.AutomaticMediaDownload));
                } else if (i == numberSectionRow) {
                    ((HeaderCell) view).setText(LocaleController.getString("Info", R.string.Info));
                }
            } else if (type == 5) {
                if (view == null) {
                    view = new TextInfoCell(mContext);
                    try {
                        PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
                        ((TextInfoCell) view).setText(String.format(Locale.US, "Instano for Android v%s (%d)", pInfo.versionName, pInfo.versionCode));
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }
                }
            } else if (type == 6) {
                if (view == null) {
                    view = new TextDetailSettingsCell(mContext);
                }
                TextDetailSettingsCell textCell = (TextDetailSettingsCell) view;

                if (i == mobileDownloadRow || i == wifiDownloadRow || i == roamingDownloadRow) {
                    int mask = 0;
                    String value;
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                    if (i == mobileDownloadRow) {
                        value = LocaleController.getString("WhenUsingMobileData", R.string.WhenUsingMobileData);
                        mask = MediaController.getInstance().mobileDataDownloadMask;
                    } else if (i == wifiDownloadRow) {
                        value = LocaleController.getString("WhenConnectedOnWiFi", R.string.WhenConnectedOnWiFi);
                        mask = MediaController.getInstance().wifiDownloadMask;
                    } else {
                        value = LocaleController.getString("WhenRoaming", R.string.WhenRoaming);
                        mask = MediaController.getInstance().roamingDownloadMask;
                    }
                    String text = "";
                    if ((mask & MediaController.AUTODOWNLOAD_MASK_PHOTO) != 0) {
                        text += LocaleController.getString("AttachPhoto", R.string.AttachPhoto);
                    }
                    if ((mask & MediaController.AUTODOWNLOAD_MASK_AUDIO) != 0) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("AttachAudio", R.string.AttachAudio);
                    }
                    if ((mask & MediaController.AUTODOWNLOAD_MASK_VIDEO) != 0) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("AttachVideo", R.string.AttachVideo);
                    }
                    if ((mask & MediaController.AUTODOWNLOAD_MASK_DOCUMENT) != 0) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("AttachDocument", R.string.AttachDocument);
                    }
                    if (text.length() == 0) {
                        text = LocaleController.getString("NoMediaAutoDownload", R.string.NoMediaAutoDownload);
                    }
                    textCell.setTextAndValue(value, text, true);
                } else if (i == numberRow) {
                    TLRPC.User user = UserConfig.getCurrentUser();
                    String value;
                    if (user != null && user.phone != null && user.phone.length() != 0) {
                        value = PhoneFormat.getInstance().format("+" + user.phone);
                    } else {
                        value = LocaleController.getString("NumberUnknown", R.string.NumberUnknown);
                    }
                    textCell.setTextAndValue(value, LocaleController.getString("Phone", R.string.Phone), true);
                } else if (i == usernameRow) {
                    TLRPC.User user = UserConfig.getCurrentUser();
                    String value;
                    value = ContactsController.formatName(user.first_name, user.last_name);
                    textCell.setTextAndValue(value, LocaleController.getString("Name", R.string.Username), false);
                }
            }
            return view;
        }

        @Override
        public int getItemViewType(int i) {
            if (i == emptyRow || i == overscrollRow) {
                return 0;
            } if (i == settingsSectionRow || i == messagesSectionRow || i == mediaDownloadSection ) {
                return 1;
            } else if (i == enableAnimationsRow || i == sendByEnterRow || i == saveToGalleryRow) {
                return 3;
            } else if (i == notificationRow || i == backgroundRow || i == privacyRow || i == textSizeRow || i == languageRow) {
                return 2;
            } else if (i == versionRow) {
                return 5;
            } else if (i == wifiDownloadRow || i == mobileDownloadRow || i == roamingDownloadRow || i == numberRow || i == usernameRow) {
                return 6;
            } else if (i == settingsSectionRow2 || i == messagesSectionRow2 || i == numberSectionRow || i == mediaDownloadSection2 ) {
                return 4;
            } else {
                return 2;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 8;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }
}
