package com.nestor87.swords.data.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.nestor87.swords.R;
import com.nestor87.swords.data.DBHelper;
import com.nestor87.swords.data.DataManager;
import com.nestor87.swords.data.models.Message;
import com.nestor87.swords.data.models.Player;
import com.nestor87.swords.data.network.NetworkService;
import com.nestor87.swords.ui.main.MainActivity;
import com.nestor87.swords.ui.messages.MessagesActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationService extends Service {
    int notificationId = 100;
    Timer timer;
    private String CHANNEL_ID;
    private DataManager dataManager;

    public static final String NOTIFICATION_RECEIVED_ACTION = "SWords_NOTIFICATION_RECEIVED_ACTION";
    public static ArrayList<Integer> notCanceledNotificationsIds = new ArrayList<>();

    public NotificationService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences preferences = getSharedPreferences(MainActivity.APP_PREFERENCES_FILE_NAME, MODE_PRIVATE);
        CHANNEL_ID = getString(R.string.app_name) + "NotificationChannelID";
        createNotificationChannel();
        dataManager = new DataManager(
                new TextView(getApplicationContext()), new TextView(getApplicationContext()),
                null, new Button[]{}, new DBHelper(getApplicationContext()), getApplicationContext());
        dataManager.loadData();

        if (timer == null) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    String uuid = preferences.getString("accountId", "");


                        NetworkService.getInstance().getSWordsApi().getUnreceivedMessages(MainActivity.getBearerToken(), new Player(uuid)).enqueue(
                                new Callback<List<Message>>() {
                                    @Override
                                    public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {

                                        for (Message message: response.body()) {
                                            Intent intent = new Intent(NotificationService.this, MessagesActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            PendingIntent pendingIntent = PendingIntent.getActivity(NotificationService.this, 0, intent, 0);

                                            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                                    .setSmallIcon(R.drawable.icon)
                                                    .setContentTitle(message.getTitle())
                                                    .setContentText(message.getBody())
                                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                    .setDefaults(Notification.DEFAULT_ALL)
                                                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon))
                                                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message.getBody()))
                                                    .setContentIntent(pendingIntent)
                                                    .setAutoCancel(false);

                                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                                            notificationManager.notify(notificationId++, builder.build());
                                            notCanceledNotificationsIds.add(notificationId - 1);

                                            if (message.getType().equals(Message.TYPE_WORD_ADDED)) {
                                                DataManager.loadWords(getApplicationContext());
                                            }
                                        }

                                        if (response.body().size() > 0) {
                                            sendBroadcast();
                                        }

                                    }

                                    @Override
                                    public void onFailure(Call<List<Message>> call, Throwable t) {

                                    }
                                }
                        );
                }
            }, 0, 5000);
        }
        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name) + "_NOTIFICATION_CHANNEL";
            String description = "SWords notification channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendBroadcast() {
        Intent intent = new Intent();
        intent.setAction(NOTIFICATION_RECEIVED_ACTION);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(MainActivity.LOG_TAG, "SERVICE destroyed");
        sendBroadcast(new Intent("RestartNotificationService"));
    }

}