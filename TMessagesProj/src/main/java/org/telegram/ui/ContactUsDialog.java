package org.telegram.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.messenger.R;

public class ContactUsDialog extends Dialog implements View.OnClickListener {

    public ContactUsDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.error_dialog);
        setTitle("Instano");

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
                Intent intent = null;
                try {
                    getContext().getPackageManager().getPackageInfo("com.twitter.android", 0);
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name= Dheeraj Meena"));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                } catch (Exception e) {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/Dheeraj Meena"));
                }
                getContext().startActivity(intent);
                break;
        }
    }


}
