package org.telegram.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;

public class ContactUsActivity extends Activity {

    final Context context = this ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//
//        ContactUsDialog dialog = new ContactUsDialog(context);
//        dialog.setCancelable(true);
//        dialog.show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }


}
