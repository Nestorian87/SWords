package com.nestor87.swords.ui.bestPlayers;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.nestor87.swords.R;
import com.nestor87.swords.data.DataManager;
import com.nestor87.swords.data.models.Achievement;
import com.nestor87.swords.data.models.Player;
import com.nestor87.swords.data.models.StatisticsResponse;
import com.nestor87.swords.data.network.NetworkService;
import com.nestor87.swords.ui.achievements.AchievementAdapter;
import com.nestor87.swords.ui.achievements.AchievementsActivity;
import com.nestor87.swords.ui.main.MainActivity;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlayersAdapter extends RecyclerView.Adapter<PlayersAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private List<Player> players;
    private ProgressBar progressBar;

    PlayersAdapter(Context context, List<Player> players, ProgressBar progressBar) {
        this.inflater = LayoutInflater.from(context);
        setPlayers(players);
        this.progressBar = progressBar;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.best_players_list_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Player player = players.get(position);
        holder.nicknameTextView.setText(player.getName());
        holder.scoreTextView.setText(DataManager.formatNumberToStringWithSpacingDecimalPlaces(player.getScore()));
        holder.hintsTextView.setText(DataManager.formatNumberToStringWithSpacingDecimalPlaces(player.getHints()));
        holder.orderNumberTextView.setText(Integer.toString(position + 1));

        SharedPreferences preferences = inflater.getContext().getSharedPreferences(MainActivity.APP_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        boolean isSelfPlayer = preferences.getString("name", "").equals(player.getName());

        if (isSelfPlayer) {
            holder.parentLayout.setBackgroundColor(MainActivity.getColorFromTheme(R.attr.buttonBackground, inflater.getContext()));
        } else {
            holder.parentLayout.setBackgroundColor(MainActivity.getColorFromTheme(android.R.attr.windowBackground, inflater.getContext()));
        }

        holder.nicknameTextView.setTextColor(MainActivity.getColorFromTheme(R.attr.buttonText, inflater.getContext()));

        if (position < 3) {
            holder.orderNumberTextView.setTextColor(MainActivity.getColorFromTheme(R.attr.wordText, inflater.getContext()));
        } else {
            holder.orderNumberTextView.setTextColor(MainActivity.getColorFromTheme(R.attr.buttonText, inflater.getContext()));
        }
        if (position == 0) {
            holder.nicknameTextView.setTextColor(MainActivity.getColorFromTheme(R.attr.redButton, inflater.getContext()));
            holder.orderNumberTextView.setTextColor(MainActivity.getColorFromTheme(R.attr.redButton, inflater.getContext()));
        } else if (position == 1) {
            holder.nicknameTextView.setTextColor(MainActivity.getColorFromTheme(R.attr.blueButton, inflater.getContext()));
            holder.orderNumberTextView.setTextColor(MainActivity.getColorFromTheme(R.attr.blueButton, inflater.getContext()));
        } else if (position == 2) {
            holder.nicknameTextView.setTextColor(MainActivity.getColorFromTheme(R.attr.yellowButton, inflater.getContext()));
            holder.orderNumberTextView.setTextColor(MainActivity.getColorFromTheme(R.attr.yellowButton, inflater.getContext()));
        }

        if (position < 10) {
            holder.nicknameTextView.setTypeface(holder.nicknameTextView.getTypeface(), Typeface.BOLD);
        } else {
            holder.nicknameTextView.setTypeface(holder.nicknameTextView.getTypeface(), Typeface.NORMAL);
        }
//        Toast.makeText(inflater.getContext(), String.valueOf(System.currentTimeMillis() - player.getLastTimeOnline()), Toast.LENGTH_SHORT).show();
        if (System.currentTimeMillis() - player.getLastTimeOnline() <= 25000) {
            holder.isOnline.setCardBackgroundColor(MainActivity.getColorFromTheme(R.attr.hint, inflater.getContext()));
        }  else {
            holder.isOnline.setVisibility(View.GONE);
        }

        holder.parentLayout.setOnClickListener(v -> {
            if (progressBar.getVisibility() == View.GONE) {
                View dialogView = LayoutInflater.from(inflater.getContext()).inflate(R.layout.dialog_profile, null, false);

                progressBar.setVisibility(View.VISIBLE);

                NetworkService.getInstance().getSWordsApi().getUserAchievements(MainActivity.getBearerToken(), player.getName()).enqueue(
                        new Callback<List<Achievement>>() {
                            @Override
                            public void onResponse(Call<List<Achievement>> call, Response<List<Achievement>> response) {
                                List<Achievement> playerAchievements = response.body();

                                for (Achievement playerAchievement : playerAchievements) {
                                    for (Achievement achievement : Achievement.ACHIEVEMENTS) {
                                        if (playerAchievement.getId().equals(achievement.getId())) {
                                            playerAchievement.setTitle(achievement.getTitle());
                                            playerAchievement.setTask(achievement.getTask());
                                            playerAchievement.setMaxProgress(achievement.getMaxProgress());
                                            playerAchievement.setContext(inflater.getContext());
                                            playerAchievement.setProgressTrigger(achievement.getProgressTrigger());
                                            playerAchievement.setRewardCount(achievement.getRewardCount());
                                            playerAchievement.setRewardCurrency(achievement.getRewardCurrency());
                                        }
                                    }
                                }

                                NetworkService.getInstance().getSWordsApi().getUserStatistics(MainActivity.getBearerToken(), player.getName()).enqueue(
                                        new Callback<StatisticsResponse>() {
                                            @SuppressLint("SetTextI18n")
                                            @Override
                                            public void onResponse(Call<StatisticsResponse> call, Response<StatisticsResponse> response) {
                                                StatisticsResponse statistics = response.body();

                                                ((TextView) dialogView.findViewById(R.id.nameTextView)).setText(statistics.getPlayer().getName());
                                                ((TextView) dialogView.findViewById(R.id.scoreTextView)).setText(DataManager.formatNumberToStringWithSpacingDecimalPlaces(statistics.getPlayer().getScore()));
                                                ((TextView) dialogView.findViewById(R.id.hintsTextView)).setText(DataManager.formatNumberToStringWithSpacingDecimalPlaces(statistics.getPlayer().getHints()));

                                                if (System.currentTimeMillis() - statistics.getPlayer().getLastTimeOnline() <= 25000) {
                                                    ((TextView) dialogView.findViewById(R.id.statusTextView)).setText(inflater.getContext().getString(R.string.status_online));
                                                    ((TextView) dialogView.findViewById(R.id.statusTextView)).setTextColor(MainActivity.getColorFromTheme(R.attr.hint, inflater.getContext()));
                                                } else {
                                                    Date dateLastTimeOnline = new Date(statistics.getPlayer().getLastTimeOnline());
                                                    String dateFormat = "";
                                                    if (DateUtils.isToday(dateLastTimeOnline.getTime())) {
                                                        dateFormat = "'сегодня' 'в' HH:mm";
                                                    } else {
                                                        dateFormat = "d MMM 'в' HH:mm";
                                                    }

                                                    String stringDateLastTimeOnline = new SimpleDateFormat(dateFormat, Locale.getDefault()).format(dateLastTimeOnline);
                                                    ((TextView) dialogView.findViewById(R.id.statusTextView)).setText(inflater.getContext().getString(R.string.status_online) + " " + stringDateLastTimeOnline);
                                                    ((TextView) dialogView.findViewById(R.id.statusTextView)).setTextColor(MainActivity.getColorFromTheme(R.attr.scoreAndHintsText, inflater.getContext()));
                                                }



                                                ((TextView) dialogView.findViewById(R.id.wordCount)).setText(statistics.getWordCount());
                                                ((TextView) dialogView.findViewById(R.id.uniqueWordCount)).setText(statistics.getUniqueWordCount());
                                                ((TextView) dialogView.findViewById(R.id.mostFrequentWord)).setText(statistics.getMostFrequentWord());
                                                ((TextView) dialogView.findViewById(R.id.wordAverageLength)).setText(statistics.getAverageWordLength());
                                                ((TextView) dialogView.findViewById(R.id.wordLongest)).setText(statistics.getLongestWord());

                                                RecyclerView recyclerView = dialogView.findViewById(R.id.achievementsList);
                                                AchievementAdapter achievementAdapter = new AchievementAdapter(inflater.getContext(), AchievementsActivity.getAchievementsSortedByGroupName(playerAchievements), R.layout.profile_achievement_list_item);
                                                recyclerView.setAdapter(achievementAdapter);

                                                AlertDialog dialog = new AlertDialog.Builder(inflater.getContext())
                                                        .setView(dialogView)
                                                        .create();

                                                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                                dialog.show();

                                                progressBar.setVisibility(View.GONE);
                                            }

                                            @Override
                                            public void onFailure(Call<StatisticsResponse> call, Throwable t) {
                                                progressBar.setVisibility(View.GONE);
                                                Toast.makeText(inflater.getContext(), "Произошла ошибка. Проверьте подключение к интернету", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                );
                            }

                            @Override
                            public void onFailure(Call<List<Achievement>> call, Throwable t) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(inflater.getContext(), "Произошла ошибка. Проверьте подключение к интернету", Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            }
        });
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nicknameTextView, scoreTextView, hintsTextView, orderNumberTextView;
        ConstraintLayout parentLayout;
        CardView isOnline;

        ViewHolder(View view) {
            super(view);
            nicknameTextView = view.findViewById(R.id.nicknameTextView);
            scoreTextView = view.findViewById(R.id.scoreTextView);
            hintsTextView = view.findViewById(R.id.hintsTextView);
            orderNumberTextView = view.findViewById(R.id.orderNumberTextView);
            parentLayout = view.findViewById(R.id.constraintLayout);
            isOnline = view.findViewById(R.id.isOnline);
        }
    }

}


