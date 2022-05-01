package com.nestor87.swords.data.models;

import java.util.ArrayList;
import java.util.Objects;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.material.snackbar.Snackbar;
import com.nestor87.swords.data.network.NetworkService;
import com.nestor87.swords.ui.achievements.AchievementsActivity;
import com.nestor87.swords.ui.main.MainActivity;
import com.nestor87.swords.R;
import com.nestor87.swords.utils.SystemUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Achievement implements Comparable {
    public static ArrayList<Achievement> ACHIEVEMENTS = new ArrayList();
    public static final int CUSTOM_TRIGGER = 5;
    public static final Currency HINTS_CURRENCY = new Currency((int) R.drawable.hints);
    public static final int HINTS_INCREASE_TRIGGER = 2;
    public static final int HINTS_REDUCE_TRIGGER = 3;
    public static final Currency SCORE_CURRENCY = new Currency((int) R.drawable.score);
    public static final int SCORE_INCREASE_TRIGGER = 0;
    public static final int SCORE_REDUCE_TRIGGER = 1;
    public static final int WORD_COMPOSING_TRIGGER = 4;
    private Context context;
    private int currentProgress;
    private String id;
    private boolean isCompleted;
    private boolean isRewardReceived = true;
    private int maxProgress;
    private int progressTrigger;
    private int rewardCount;
    private Currency rewardCurrency;
    private String task;
    private String title;

    public static int getCurrencyImageByTrigger(int progressTrigger) {
        if (progressTrigger == SCORE_INCREASE_TRIGGER || progressTrigger == SCORE_REDUCE_TRIGGER)
            return R.drawable.score;

        if (progressTrigger == HINTS_INCREASE_TRIGGER || progressTrigger == HINTS_REDUCE_TRIGGER)
            return R.drawable.hints;

        return -1;
    }

    public Achievement(Context context, String id, String title, String task, int maxProgress, int progressTrigger, Currency rewardCurrency, int rewardCount) {
        this.id = id;
        this.title = title;
        this.task = task;
        this.maxProgress = maxProgress;
        this.progressTrigger = progressTrigger;
        this.rewardCurrency = rewardCurrency;
        this.rewardCount = rewardCount;
        SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.APP_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        int i = sharedPreferences.getInt("Progress.Achievement." + id, 0);
        this.currentProgress = i;
        this.isCompleted = i == this.maxProgress;
        this.isRewardReceived = sharedPreferences.getBoolean("IsRewardReceived.Achievement." + id, false);
        this.context = context;
    }

    public String getTitle() {
        return this.title;
    }

    public void addProgress(int count, Context context) {
        this.currentProgress = Math.min(this.currentProgress + count, this.maxProgress);
        SharedPreferences.Editor edit = this.context.getSharedPreferences(MainActivity.APP_PREFERENCES_FILE_NAME, 0).edit();
        edit.putInt("Progress.Achievement." + this.id, this.currentProgress).apply();
        if (this.currentProgress == this.maxProgress && !this.isCompleted) {
            onComplete(context);
        }
    }

    private void onComplete(Context context) {
        this.isCompleted = true;
        MainActivity.playSound(R.raw.achievement, context);
        View word = ((Activity) context).findViewById(android.R.id.content);
        ((Snackbar) Snackbar.make(word, (CharSequence) "Достижение \"" + this.title + "\" выполнено", 0).setAnimationMode(1)).setAction((CharSequence) "Забрать награду", v -> {
            Intent intent = new Intent(context, AchievementsActivity.class);
            intent.putExtra("scrollTo", Achievement.this.id);
            context.startActivity(intent);
            Animatoo.animateSlideRight(context);
        }).setBackgroundTint(SystemUtils.getColorFromTheme(android.R.attr.windowBackground, context)).setTextColor(SystemUtils.getColorFromTheme(R.attr.scoreAndHintsText, context)).setActionTextColor(SystemUtils.getColorFromTheme(R.attr.wordText, context)).show();
    }

    public boolean isCompleted() {
        return this.isCompleted;
    }

    public static void addProgress(int trigger, int count, Context context) {
        for (Achievement achievement : ACHIEVEMENTS) {
            if (achievement.getProgressTrigger() == trigger) {
                achievement.addProgress(count, context);
                NetworkService.getInstance().getSWordsApi().sendAchievement(MainActivity.getBearerToken(), new AchievementRequest(MainActivity.uuid, achievement.getId(), achievement.getCurrentProgress())).enqueue(
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
        }
    }

    public int getCurrentProgress() {
        return this.currentProgress;
    }

    public int getMaxProgress() {
        return this.maxProgress;
    }

    public Currency getRewardCurrency() {
        return this.rewardCurrency;
    }

    public int getProgressTrigger() {
        return this.progressTrigger;
    }

    public String getId() {
        return this.id;
    }

    public String getTask() {
        return this.task;
    }

    public int getRewardCount() {
        return this.rewardCount;
    }

    public boolean isRewardReceived() {
        return this.isRewardReceived;
    }

    public void setRewardReceived(boolean rewardReceived) {
        this.isRewardReceived = rewardReceived;
        SharedPreferences.Editor edit = this.context.getSharedPreferences(MainActivity.APP_PREFERENCES_FILE_NAME, 0).edit();
        edit.putBoolean("IsRewardReceived.Achievement." + this.id, rewardReceived).apply();
    }

    public int getPluralsResource() {
        if (progressTrigger == WORD_COMPOSING_TRIGGER)
            return R.plurals.word;

        return -1;
    }

    public String getGroupName() {
        return id.split("\\.")[0];
    }

    @Override
    public int compareTo(Object o) {
        assert o instanceof Achievement;
        if (!((Achievement) o).getGroupName().equals(this.getGroupName())) {
            throw new IllegalArgumentException("Cannot compare achievements from different groups");
        }
        return this.getMaxProgress() - ((Achievement) o).getMaxProgress();
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
        this.isCompleted = currentProgress == this.maxProgress;
    }

    public void setProgressTrigger(int progressTrigger) {
        this.progressTrigger = progressTrigger;
    }

    public void setRewardCount(int rewardCount) {
        this.rewardCount = rewardCount;
    }

    public void setRewardCurrency(Currency rewardCurrency) {
        this.rewardCurrency = rewardCurrency;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    public static void initAchievements(Context context) {
        ACHIEVEMENTS.add(new Achievement(context, "score.beginner", "Начинающий", "Заработать {x}", 100, SCORE_INCREASE_TRIGGER, HINTS_CURRENCY, 20));
        ACHIEVEMENTS.add(new Achievement(context, "score.advanced", "Продвинутый", "Заработать {x}", 500, SCORE_INCREASE_TRIGGER, HINTS_CURRENCY, 50));
        ACHIEVEMENTS.add(new Achievement(context, "score.pro", "Профессионал", "Заработать {x}", 2000, SCORE_INCREASE_TRIGGER, HINTS_CURRENCY, 100));
        ACHIEVEMENTS.add(new Achievement(context, "score.master", "Мастер", "Заработать {x}", 8000, SCORE_INCREASE_TRIGGER, HINTS_CURRENCY, 200));
        ACHIEVEMENTS.add(new Achievement(context, "score.expert", "Эксперт", "Заработать {x}", 15000, SCORE_INCREASE_TRIGGER, HINTS_CURRENCY, 400));
        ACHIEVEMENTS.add(new Achievement(context, "score.champion", "Чемпион", "Заработать {x}", 30000, SCORE_INCREASE_TRIGGER, HINTS_CURRENCY, 1000));
        ACHIEVEMENTS.add(new Achievement(context, "score.fan", "Фанат", "Заработать {x}", 50000, SCORE_INCREASE_TRIGGER, HINTS_CURRENCY, 1500));
        ACHIEVEMENTS.add(new Achievement(context, "score.crazy", "Чокнутый", "Заработать {x}", 100000, SCORE_INCREASE_TRIGGER, HINTS_CURRENCY, 2000));

        ACHIEVEMENTS.add(new Achievement(context, "hints.curious", "Любопытный", "Использовать {x}", 50, HINTS_REDUCE_TRIGGER, SCORE_CURRENCY, 200));
        ACHIEVEMENTS.add(new Achievement(context, "hints.veryCurious", "Очень любопытный", "Использовать {x}", 250, HINTS_REDUCE_TRIGGER, SCORE_CURRENCY, 600));
        ACHIEVEMENTS.add(new Achievement(context, "hints.lazy", "Лентяй", "Использовать {x}", 500, HINTS_REDUCE_TRIGGER, SCORE_CURRENCY, 1000));
        ACHIEVEMENTS.add(new Achievement(context, "hints.DStudent", "Двоечник", "Использовать {x}", 1000, HINTS_REDUCE_TRIGGER, SCORE_CURRENCY, 2500));
//            ACHIEVEMENTS.add(new Achievement(context, "hints.DStudent", "Двоечник", "Использовать {x}", 3500, HINTS_REDUCE_TRIGGER, SCORE_CURRENCY, 5000));

//            ACHIEVEMENTS.add(new Achievement(context, "words.1", "1", "Составить {x} {w}", 20, WORD_COMPOSING_TRIGGER, HINTS_CURRENCY, 10));
//            ACHIEVEMENTS.add(new Achievement(context, "words.2", "2", "Составить {x} {w}", 50, WORD_COMPOSING_TRIGGER, SCORE_CURRENCY, 75));
//            ACHIEVEMENTS.add(new Achievement(context, "words.3", "3", "Составить {x} {w}", 100, WORD_COMPOSING_TRIGGER, HINTS_CURRENCY, 30));
//            ACHIEVEMENTS.add(new Achievement(context, "words.4", "4", "Составить {x} {w}", 200, WORD_COMPOSING_TRIGGER, SCORE_CURRENCY, 200));
//            ACHIEVEMENTS.add(new Achievement(context, "words.5", "5", "Составить {x} {w}", 500, WORD_COMPOSING_TRIGGER, HINTS_CURRENCY, 85));
//            ACHIEVEMENTS.add(new Achievement(context, "words.6", "6", "Составить {x} {w}", 750, WORD_COMPOSING_TRIGGER, SCORE_CURRENCY, 500));
//            ACHIEVEMENTS.add(new Achievement(context, "words.7", "7", "Составить {x} {w}", 750, WORD_COMPOSING_TRIGGER, SCORE_CURRENCY, 500));

    }
}

