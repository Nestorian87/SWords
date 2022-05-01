package com.nestor87.swords.data.services;

import static com.nestor87.swords.ui.main.MainActivity.APP_PREFERENCES_FILE_NAME;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.nestor87.swords.R;
import com.nestor87.swords.data.models.Player;
import com.nestor87.swords.data.network.NetworkService;
import com.nestor87.swords.data.receiver.ScreenOffReceiver;
import com.nestor87.swords.ui.main.MainActivity;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BackgroundService extends Service {

    private MediaPlayer player;
    private int playerPosition = 0;
    private float volume = 0.4f;
    private BroadcastReceiver screenOffReceiver;
    private final Handler handler = new Handler();
    private Runnable handlerRunnable;
    private Timer timer;
    private boolean isTimerRunning = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getBooleanExtra("pause", false)) {
            playerPosition = player.getCurrentPosition();
            player.pause();
            if (isTimerRunning) {
                timer.cancel();
                isTimerRunning = false;
            }
        } else if (intent.getBooleanExtra("resume", false)) {
            if (!player.isPlaying()) {
                player.start();
                player.seekTo(playerPosition);
            }
            startTimer();
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
        int[] music = new int[] {
                R.raw.swords_theme_1,
                R.raw.swords_theme_2,
                R.raw.swords_theme_3,
                R.raw.swords_theme_4
        };
        final int[] currentMusicIndex = {new Random().nextInt(music.length)};
        player = MediaPlayer.create(this, music[currentMusicIndex[0]]);
        player.setVolume(volume, volume);
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.release();
                int newMusicIndex;
                do {
                    newMusicIndex = new Random().nextInt(music.length);
                } while (currentMusicIndex[0] == newMusicIndex);
                currentMusicIndex[0] = newMusicIndex;
                player = MediaPlayer.create(BackgroundService.this, music[currentMusicIndex[0]]);
                player.setVolume(volume, volume);
                player.setOnCompletionListener(this);
                player.start();
            }
        });

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        screenOffReceiver = new ScreenOffReceiver();
        registerReceiver(screenOffReceiver, filter);

        final int interval = 20000;
        SharedPreferences preferences = getApplicationContext().getSharedPreferences(APP_PREFERENCES_FILE_NAME, MODE_PRIVATE);
        setStatusOnline(preferences.getString("accountId", ""));
        handlerRunnable = new Runnable() {
            @Override
            public void run() {
                setStatusOnline(preferences.getString("accountId", ""));
                handler.postDelayed(this, interval);
            }
        };
        handler.postDelayed(handlerRunnable, interval);

    }

    private void startTimer() {
        if (!isTimerRunning) {
            isTimerRunning = true;
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    SharedPreferences preferences = getApplicationContext().getSharedPreferences(APP_PREFERENCES_FILE_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("minutesInGame", preferences.getInt("minutesInGame", 0) + 1);
                    editor.apply();
                }
            }, 60000, 60000);
        }
    }

    private void setStatusOnline(String accountId) {
        NetworkService.getInstance().getSWordsApi().setStatusOnline(MainActivity.getBearerToken(), new Player(accountId)).enqueue(
                new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {

                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {

                    }
                }
        );
    }

    @Override
    public void onDestroy() {
        player.stop();
        player.release();
        if (screenOffReceiver != null)
            unregisterReceiver(screenOffReceiver);
        handler.removeCallbacks(handlerRunnable);
        super.onDestroy();
    }
}