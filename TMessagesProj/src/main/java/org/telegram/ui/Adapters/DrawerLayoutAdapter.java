/*
 * This is the source code of Telegram for Android v. 1.7.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package org.telegram.ui.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.telegram.android.LocaleController;
import org.telegram.android.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.Cells.DrawerActionCell;
import org.telegram.ui.Cells.DrawerProfileCell;
import org.telegram.ui.Cells.EmptyCell;

public class DrawerLayoutAdapter extends BaseAdapter {

    private Context mContext;

    public DrawerLayoutAdapter(Context context) {
        mContext = context;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int i) {
        return !(i == 0 || i == 1 /*|| i == 5*/);
    }

    @Override
    public int getCount() {
        return UserConfig.isClientActivated() ? 6 : 0;
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
        return true;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        int type = getItemViewType(i);
        if (type == 0) {
            if (view == null) {
                view = new DrawerProfileCell(mContext);
            }
            ((DrawerProfileCell) view).setUser(MessagesController.getInstance().getUser(UserConfig.getClientUserId()));
        } else if (type == 1) {
            if (view == null) {
                view = new EmptyCell(mContext, 8);
            }
//        } else if (type == 2) {
//            if (view == null) {
//                view = new DividerCell(mContext);
//            }
        } else if (type == 2) {
            if (view == null) {
                view = new DrawerActionCell(mContext);
            }
            DrawerActionCell actionCell = (DrawerActionCell) view;
//            if (i == 2) {
//                actionCell.setTextAndIcon(LocaleController.getString("NewGroup", R.string.NewGroup), R.drawable.menu_newgroup);
//            } else if (i == 3) {
//                actionCell.setTextAndIcon(LocaleController.getString("NewSecretChat", R.string.NewSecretChat), R.drawable.menu_secret);
//            } else if (i == 4) {
//                actionCell.setTextAndIcon(LocaleController.getString("NewBroadcastList", R.string.NewBroadcastList), R.drawable.menu_broadcast);
//            } else if (i == 6) {
//                actionCell.setTextAndIcon(LocaleController.getString("Contacts", R.string.Contacts), R.drawable.menu_contacts);
//            } else
//            if(i==2){
//                actionCell.setTextAndIcon("My Orders",R.drawable.ic_ab_doc);
//            }
//            else
            if (i == 2) {
                actionCell.setTextAndIcon(LocaleController.getString("InviteFriend", R.string.InviteFriends), R.drawable.menu_invite);
            } else if (i == 3) {
                actionCell.setTextAndIcon(LocaleController.getString("Settings", R.string.Settings), R.drawable.menu_settings);
            } else if (i == 4) {
                actionCell.setTextAndIcon("Contact Us",R.drawable.menu_help);
            }
//            else if (i == 4) {
//                actionCell.setTextAndIcon("About Us", R.drawable.menu_help);
//            }
        }

        return view;
    }

    @Override
    public int getItemViewType(int i) {
        if (i == 0) {
            return 0; // profile
        } else if (i == 1) {
            return 1; // empty cell
//        } else if (i == 5) {
//            return 2; // divider
        }
        return 2; // action cell (i.e. clickable icon)
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public boolean isEmpty() {
        return !UserConfig.isClientActivated();
    }
}
