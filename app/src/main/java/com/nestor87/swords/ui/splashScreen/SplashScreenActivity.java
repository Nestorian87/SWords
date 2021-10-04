package com.nestor87.swords.ui.splashScreen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.nestor87.swords.data.DataManager;
import com.nestor87.swords.data.service.BackgroundMusicService;
import com.nestor87.swords.data.service.NotificationService;
import com.nestor87.swords.ui.main.MainActivity;


public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_splash_screen);
        startService(new Intent(this, BackgroundMusicService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                DataManager.loadWords(this);
                startService(new Intent(this, NotificationService.class));
                startActivity(new Intent(this, MainActivity.class));
                Animatoo.animateFade(this);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

    }


}