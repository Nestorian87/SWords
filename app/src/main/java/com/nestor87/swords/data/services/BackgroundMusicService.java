package com.nestor87.swords.data.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.nestor87.swords.data.LoopMediaPlayer;
import com.nestor87.swords.data.network.NetworkService;
import com.nestor87.swords.ui.main.MainActivity;
import com.nestor87.swords.data.models.Player;
import com.nestor87.swords.data.receiver.ScreenOffReceiver;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.nestor87.swords.ui.main.MainActivity.APP_PREFERENCES_FILE_NAME;

public class BackgroundMusicService extends Service {

    LoopMediaPlayer player;
    float volume = 0.5f;
    private BroadcastReceiver screenOffReceiver;
    private final Handler handler = new Handler();
    private Runnable handlerRunnable;

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


        final int interval = 10000;
        SharedPreferences preferences = getApplicationContext().getSharedPreferences(APP_PREFERENCES_FILE_NAME, MODE_PRIVATE);
        handlerRunnable = new Runnable() {
            @Override
            public void run() {
                NetworkService.getInstance().getSWordsApi().setStatusOnline(MainActivity.getBearerToken(), new Player(preferences.getString("accountId", ""))).enqueue(
                        new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {

                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {

                            }
                        }
                );
                handler.postDelayed(this, interval);
            }
        };
        handler.postDelayed(handlerRunnable, interval);
    }

    @Override
    public void onDestroy() {
        player.stop();
        if (screenOffReceiver != null)
            unregisterReceiver(screenOffReceiver);
        handler.removeCallbacks(handlerRunnable);
        super.onDestroy();
    }
}