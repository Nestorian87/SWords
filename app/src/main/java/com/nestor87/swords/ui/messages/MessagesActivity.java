package com.nestor87.swords.ui.messages;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.nestor87.swords.R;
import com.nestor87.swords.data.DBHelper;
import com.nestor87.swords.data.DataManager;
import com.nestor87.swords.data.models.MessageInfo;
import com.nestor87.swords.data.network.NetworkService;
import com.nestor87.swords.data.services.NotificationService;
import com.nestor87.swords.ui.bestPlayers.BestPlayersActivity;
import com.nestor87.swords.ui.main.MainActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.nestor87.swords.ui.achievements.AchievementAdapter.dataManager;
import static com.nestor87.swords.ui.main.MainActivity.APP_PREFERENCES_FILE_NAME;

public class MessagesActivity extends AppCompatActivity {
    MessagesAdapter messagesAdapter;
    String selfUuid;
    ProgressBar progressBar;
    TextView noMessagesTextView;
    BroadcastReceiver notificationBroadcastReceiver;
    DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DataManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        DataManager.adjustFontScale(this);
        setContentView(R.layout.activity_messages);

        progressBar = findViewById(R.id.progressBar);
        noMessagesTextView = findViewById(R.id.noMessagesTextView);

        SharedPreferences preferences = getSharedPreferences(APP_PREFERENCES_FILE_NAME, MODE_PRIVATE);
        selfUuid = preferences.getString("accountId", "");

        dataManager = new DataManager(findViewById(R.id.score), findViewById(R.id.hints), null, new Button[]{}, new DBHelper(this), this);
        dataManager.loadData();

        loadMessages(false);

        notificationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadMessages(true);
            }
        };
        registerReceiver(notificationBroadcastReceiver, new IntentFilter(NotificationService.NOTIFICATION_RECEIVED_ACTION));

    }

    private void initRecyclerView(List<MessageInfo> messages) {
        RecyclerView recyclerView = findViewById(R.id.messageList);
        messagesAdapter = new MessagesAdapter(this, messages, dataManager);
        recyclerView.setAdapter(messagesAdapter);
    }

    private void loadMessages(boolean update) {
        NetworkService.getInstance().getSWordsApi().getMessages(MainActivity.getBearerToken(), selfUuid).enqueue(
                new Callback<List<MessageInfo>>() {
                    @Override
                    public void onResponse(Call<List<MessageInfo>> call, Response<List<MessageInfo>> response) {
                        progressBar.setVisibility(View.GONE);
                        cancelAllNotifications();
                        if (!update) {
                            initRecyclerView(response.body());
                        } else {
                            messagesAdapter.setMessages(response.body());
                        }

                        noMessagesTextView.setVisibility(response.body().size() == 0 ? View.VISIBLE : View.GONE);

                    }

                    @Override
                    public void onFailure(Call<List<MessageInfo>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        new AlertDialog.Builder(MessagesActivity.this)
                                .setIcon(R.drawable.icon)
                                .setMessage("Проверьте подключение к интернету")
                                .setTitle("Произошла ошибка")
                                .setCancelable(false)
                                .setPositiveButton("Попробовать ещё раз", (dialog, which) -> loadMessages(false))
                                .setNegativeButton("Назад", (dialog, which) -> onBackPressed())
                                .show();
                    }
                }
        );
    }

    private void cancelAllNotifications() {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        for (int id : NotificationService.notCanceledNotificationsIds) {
            notificationManager.cancel(id);
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        Animatoo.animateSlideRight(this);
    }

    public void goBack(View view) {
        onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();
        MainActivity.onActivityStart(this);
    }

    @Override
    protected void onStop() {
        MainActivity.onActivityStop(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(notificationBroadcastReceiver);
        super.onDestroy();
    }
}