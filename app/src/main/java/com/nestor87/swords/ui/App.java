package com.nestor87.swords.ui;
import android.app.Application;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.nestor87.swords.BuildConfig;
import com.nestor87.swords.data.models.CrashInfo;
import com.nestor87.swords.data.network.NetworkService;
import com.nestor87.swords.ui.main.MainActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class App extends Application {
    private static App singleton;

    public App getInstance(){
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;

        Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, e) -> {
                String crashLogs = Log.getStackTraceString(e);
                if (e.getCause() != null) {
                    crashLogs += Log.getStackTraceString(e.getCause());
                }

                NetworkService.getInstance().getSWordsApi().sendCrashInfo(
                        "Bearer " + MainActivity.accountManagerPassword,
                        new CrashInfo(
                                MainActivity.uuid,
                                crashLogs,
                                Build.VERSION.SDK_INT,
                                BuildConfig.VERSION_CODE,
                                Build.MANUFACTURER + " " + Build.PRODUCT
                        )
                ).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {

                    }
                });
                try {
                    Thread.sleep(700);
                    if (defaultUncaughtExceptionHandler != null) {
                        defaultUncaughtExceptionHandler.uncaughtException(thread, e);
                    }
                } catch (InterruptedException exception) {

                }
        });
    }
}
