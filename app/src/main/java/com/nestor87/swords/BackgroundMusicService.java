package com.nestor87.swords;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.effect.Effect;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class BackgroundMusicService extends Service {

    LoopMediaPlayer player;
    float volume = 0.5f;
    private BroadcastReceiver screenOffReceiver;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getBooleanExtra("pause", false)) {
            player.pause();
        } else if (intent.getBooleanExtra("resume", false)) {
            player.resume();
        }

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        player = LoopMediaPlayer.create(this, volume);

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        screenOffReceiver = new ScreenOffReceiver();
        registerReceiver(screenOffReceiver, filter);
    }

    @Override
    public void onDestroy() {
        player.stop();
        if (screenOffReceiver != null)
            unregisterReceiver(screenOffReceiver);
        super.onDestroy();
    }
}