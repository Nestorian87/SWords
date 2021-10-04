package com.nestor87.swords.data.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nestor87.swords.R;
import com.nestor87.swords.data.DataManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.nestor87.swords.ui.main.MainActivity.accountManagerPassword;
import static com.nestor87.swords.ui.main.MainActivity.uuid;

public class NotificationService extends Service {
    int notificationId = 100;
    Timer timer;

    public NotificationService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (timer == null) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://ananiev-nestor.kl.com.ua/SWords_account_manager.php",
                            response -> {
                                response = response.trim();

                                if (!response.isEmpty()) {
                                    String[] messages = response.split("\n");
                                    for (int i = 0; i < messages.length; i++) {
                                        String title = messages[i].split("\\|")[0];
                                        String body = messages[i].split("\\|")[1];
                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "SWords")
                                                .setSmallIcon(R.drawable.icon)
                                                .setContentTitle(title)
                                                .setContentText(body)
                                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                                .setDefaults(Notification.DEFAULT_ALL)
                                                .setStyle(new NotificationCompat.BigTextStyle().bigText(body));

                                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                                        notificationManager.notify(notificationId++, builder.build());
                                        DataManager.loadWords(getApplicationContext());
                                    }
                                }
                            },
                            error -> {

                            }
                    ) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("receiveMessages", "true");
                            params.put("uuid", uuid);
                            params.put("password", accountManagerPassword);
                            return params;
                        }
                    };
                    queue.add(stringRequest);
                }
            }, 0, 5000);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sendBroadcast(new Intent("RestartNotificationService"));
    }

}