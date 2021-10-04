package com.nestor87.swords.data.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nestor87.swords.data.service.NotificationService;

public class RestartServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, NotificationService.class));
    }
}