package com.nestor87.swords.data.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nestor87.swords.data.services.BackgroundService;

public class ScreenOffReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Intent pauseIntent = new Intent(context, BackgroundService.class);
            pauseIntent.putExtra("pause", true);
            context.startService(pauseIntent);
        }

    }
}