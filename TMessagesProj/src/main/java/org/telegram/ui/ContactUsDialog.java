package org.telegram.ui;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.messenger.R;

import java.util.ArrayList;
import java.util.List;

public class ContactUsDialog extends Dialog implements View.OnClickListener {

    public ContactUsDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.error_dialog);
        setTitle("Contact");

        Button buttonSendSms = (Button) findViewById(R.id.buttonSendSms);
        Button buttonSendWhatsapp = (Button) findViewById(R.id.buttonSendWhatsapp);
        Button buttonSendTwitter = (Button) findViewById(R.id.buttonSendTwitter);
        buttonSendTwitter.setOnClickListener(this);
        buttonSendWhatsapp.setOnClickListener(this);
        buttonSendSms.setOnClickListener(this);

        TextView tv = (TextView)findViewById(R.id.ErrorText);
        tv.setText(R.string.Contact);
    }

    @Override
    public void onClick(View v) {
        String whatsAppId = "919916780444";
        String mobileNumber = "+" + whatsAppId;
        switch (v.getId()) {
            case R.id.buttonSendSms:
                getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", mobileNumber, null)));
                break;

            case R.id.buttonSendWhatsapp:
                Uri mUri = Uri.parse("smsto:+" + whatsAppId);
                Intent whatsApp = new Intent(Intent.ACTION_SENDTO, mUri);
                whatsApp.putExtra("chat", true);
                whatsApp.setPackage("com.whatsapp");
                try {
                    getContext().startActivity(whatsApp);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getContext(), "WhatsApp not installed", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.buttonSendTwitter:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/instanoapp"));
                getContext().startActivity(browserIntent);
                break;
        }
    }


}
