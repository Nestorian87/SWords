package com.nestor87.swords;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScreenOffReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Intent pauseIntent = new Intent(context, BackgroundMusicService.class);
            pauseIntent.putExtra("pause", true);
            context.startService(pauseIntent);
        }

    }
}