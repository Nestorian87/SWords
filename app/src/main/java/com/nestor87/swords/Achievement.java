package com.nestor87.swords;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.material.snackbar.Snackbar;


class Currency {
    private boolean custom = false;
    private int icon;
    private String text;

    public Currency(int icon2) {
        this.icon = icon2;
    }

    public Currency(String text2) {
        this.text = text2;
        this.custom = true;
    }

    public int getIcon() {
        return this.icon;
    }

    public String getText() {
        return this.text;
    }

    public boolean isCustom() {
        return this.custom;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Currency currency = (Currency) o;
        if (this.icon != currency.icon || !Objects.equals(this.text, currency.text)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.icon), this.text);
    }
}


public class Achievement {
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
    private boolean isRewardReceived;
    private int maxProgress;
    private int progressTrigger;
    private int rewardCount;
    private Currency rewardCurrency;
    private String task;
    private String title;

    public static int getCurrencyImageByTrigger(int progressTrigger) {
        if (progressTrigger == 0 || progressTrigger == 1) {
            return R.drawable.score;
        }
        if (progressTrigger == 2 || progressTrigger == 3) {
            return R.drawable.hints;
        }
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
        SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.APP_PREFERENCES_FILE_NAME, 0);
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
        ((Snackbar) Snackbar.make(word, (CharSequence) "Достижение \"" + this.title + "\" выполнено", 0).setAnimationMode(1)).setAction((CharSequence) "Забрать награду", (View.OnClickListener) new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(context, AchievementsActivity.class);
                intent.putExtra("scrollTo", Achievement.this.id);
                context.startActivity(intent);
                Animatoo.animateSlideRight(context);
            }
        }).setBackgroundTint(MainActivity.getColorFromTheme(android.R.attr.windowBackground, context)).setTextColor(MainActivity.getColorFromTheme(R.attr.scoreAndHintsText, context)).setActionTextColor(MainActivity.getColorFromTheme(R.attr.wordText, context)).show();
    }

    public boolean isCompleted() {
        return this.isCompleted;
    }

    public static void addProgress(int trigger, int count, Context context) {
        for (Achievement achievement : ACHIEVEMENTS) {
            if (achievement.getProgressTrigger() == trigger) {
                achievement.addProgress(count, context);
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
}
