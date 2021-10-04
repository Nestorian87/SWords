package com.nestor87.swords.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.util.Log;

import com.nestor87.swords.R;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.MODE_PRIVATE;
import static com.nestor87.swords.ui.main.MainActivity.APP_PREFERENCES_FILE_NAME;

public class LoopMediaPlayer {

    public static final String TAG = LoopMediaPlayer.class.getSimpleName();

    private Context mContext = null;
    private int mCounter = 1;

    private MediaPlayer mCurrentPlayer = null;
    private MediaPlayer mNextPlayer = null;
    private MediaPlayer mIntroPlayer = null;
    private MediaPlayer mPausedPlayer = null;
    private float volume;

    SharedPreferences preferences;
    SharedPreferences.Editor preferencesEditor;
    Timer timer;
    
    private final int[] musicFiles = new int[] { R.raw.swords_theme_1, R.raw.swords_theme_2 };

    public static LoopMediaPlayer create(Context context, float volume) {
        return new LoopMediaPlayer(context, volume);
    }

    private LoopMediaPlayer(Context context, float volume) {
        mContext = context;

        preferences = context.getSharedPreferences(APP_PREFERENCES_FILE_NAME, MODE_PRIVATE);
        preferencesEditor = preferences.edit();

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (mPausedPlayer == null) {
                    preferencesEditor.putInt("minutesInGame", preferences.getInt("minutesInGame", 0) + 1);
                    preferencesEditor.apply();
                }
            }
        }, 0, 60000);


        this.volume = volume;



        mIntroPlayer = MediaPlayer.create(mContext, R.raw.swords_intro);
        mCurrentPlayer = MediaPlayer.create(mContext, R.raw.swords_theme_1);
        mIntroPlayer.setVolume(volume, volume);
        mCurrentPlayer.setVolume(volume, volume);
        mIntroPlayer.setOnPreparedListener(mediaPlayer -> mIntroPlayer.start());
        mIntroPlayer.setNextMediaPlayer(mCurrentPlayer);

        createNextMediaPlayer();
    }

    private void createNextMediaPlayer() {
        mNextPlayer = MediaPlayer.create(mContext, musicFiles[new Random().nextInt(musicFiles.length)]);
        mNextPlayer.setVolume(volume, volume);
        mCurrentPlayer.setNextMediaPlayer(mNextPlayer);
        mCurrentPlayer.setOnCompletionListener(onCompletionListener);
    }

    private MediaPlayer.OnCompletionListener onCompletionListener = mediaPlayer -> {
        mediaPlayer.release();
        mCurrentPlayer = mNextPlayer;

        createNextMediaPlayer();

        Log.d(TAG, String.format("Loop #%d", ++mCounter));
    };

    public void stop() {
        if (mCurrentPlayer.isPlaying()) {
            mCurrentPlayer.stop();
            mCurrentPlayer.release();
        }
        if (mNextPlayer.isPlaying()) {
            mNextPlayer.stop();
            mNextPlayer.release();
        }
        if (mIntroPlayer.isPlaying()) {
            mIntroPlayer.stop();
            mIntroPlayer.release();
        }

        timer.cancel();
    }

    public boolean isPlaying() {
        return mCurrentPlayer.isPlaying() || mNextPlayer.isPlaying() || mIntroPlayer.isPlaying();
    }


    public void pause() {
        if (mCurrentPlayer.isPlaying()) {
            mCurrentPlayer.pause();
            mPausedPlayer = mCurrentPlayer;
        }
        if (mNextPlayer.isPlaying()) {
            mNextPlayer.pause();
            mPausedPlayer = mNextPlayer;
        }
        if (mIntroPlayer.isPlaying()) {
            mIntroPlayer.pause();
            mPausedPlayer = mIntroPlayer;
        }
    }

    public void resume() {
        if (mPausedPlayer != null)
            mPausedPlayer.start();
    }

    public void setVolume(float volume) {
        mIntroPlayer.setVolume(volume, volume);
        mCurrentPlayer.setVolume(volume, volume);
        mNextPlayer.setVolume(volume, volume);
    }
}