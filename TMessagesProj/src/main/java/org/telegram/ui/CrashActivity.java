package org.telegram.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

import org.telegram.android.LocaleController;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.R;

public class CrashActivity extends Activity {

    final Context context = this ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crashactivity_background);
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(null);

//        ErrorDialog dialog = new ErrorDialog(context);
//        dialog.setCancelable(false);
//        dialog.show();

    }

    @Override
    protected void onResume() {
        super.onResume();
        AlertDialog.Builder builder = new AlertDialog.Builder(CrashActivity.this);
        CharSequence[] items = new CharSequence[]{"Sms", "WhatsApp", "Twitter"};

        final String whatsAppId = BuildVars.whatsAppId;
        final String mobileNumber = "+" + whatsAppId;

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0 :
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", mobileNumber, null)));
                        break;
                    case 1:
                        Uri mUri = Uri.parse("smsto:+" + whatsAppId);
                        Intent whatsApp = new Intent(Intent.ACTION_SENDTO, mUri);
                        whatsApp.putExtra("chat", true);
                        whatsApp.setPackage("com.whatsapp");
                        try {
                            startActivity(whatsApp);
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(CrashActivity.this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 2:
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/instanoapp"));
                        startActivity(browserIntent);
                        break;
                }
            }
        });

        builder.setTitle("Error occured :(");
        builder.show();
        builder.setCancelable(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }


}
