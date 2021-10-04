package com.nestor87.swords.ui.bestPlayers;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.nestor87.swords.data.DataManager;
import com.nestor87.swords.ui.main.MainActivity;
import com.nestor87.swords.data.network.NetworkService;
import com.nestor87.swords.data.models.Player;
import com.nestor87.swords.R;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BestPlayersActivity extends AppCompatActivity {
    ProgressBar progressBar;
    PlayersAdapter playersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DataManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_best_players);

        progressBar = findViewById(R.id.progressBar);

        loadPlayers(false);


        final Handler handler = new Handler();
        final int interval = 5000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadPlayers(true);
                handler.postDelayed(this, interval);
            }
        }, interval);
    }

    private void loadPlayers(boolean update) {
        if (!update)
            progressBar.setVisibility(View.VISIBLE);
        NetworkService.getInstance().getSWordsApi().getAllUsers(MainActivity.getBearerToken()).enqueue(
                new Callback<List<Player>>() {
                    @Override
                    public void onResponse(Call<List<Player>> call, Response<List<Player>> response) {
                        progressBar.setVisibility(View.GONE);
                        if (update) {
                            playersAdapter.setPlayers(response.body());
                        } else {
                            initRecyclerView(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Player>> call, Throwable t) {
                        if (!update) {
                            progressBar.setVisibility(View.GONE);
                            new AlertDialog.Builder(BestPlayersActivity.this)
                                    .setIcon(R.drawable.icon)
                                    .setMessage("Проверьте подключение к интернету")
                                    .setTitle("Произошла ошибка")
                                    .setCancelable(false)
                                    .setPositiveButton("Попробовать ещё раз", (dialog, which) -> loadPlayers(false))
                                    .setNegativeButton("Назад", (dialog, which) -> onBackPressed())
                                    .show();
                        }
                    }
                }
        );
    }

    private void initRecyclerView(List<Player> players) {
        RecyclerView recyclerView = findViewById(R.id.bestPlayersList);
        playersAdapter = new PlayersAdapter(BestPlayersActivity.this, players);
        recyclerView.setAdapter(playersAdapter);
    }

    public void goBack(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        Animatoo.animateSlideRight(this);
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
}