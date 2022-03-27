package com.nestor87.swords.data.models;

public class MessageReward {
    int score = 0;
    int hints = 0;
    String title = "Награда";
    boolean isReceived;
    String sharedPreferencesKey, sharedPreferencesValue, sharedPreferencesType;

    public int getScore() {
        return score;
    }

    public int getHints() {
        return hints;
    }

    public String getTitle() {
        return title;
    }

    public boolean isReceived() {
        return isReceived;
    }

    public boolean hasSharedPreferencesModification() {
        return sharedPreferencesKey != null
                && sharedPreferencesType != null
                && sharedPreferencesValue != null
                && !sharedPreferencesKey.isEmpty()
                && !sharedPreferencesValue.isEmpty()
                && !sharedPreferencesType.isEmpty();
    }

    public String getSharedPreferencesKey() {
        return sharedPreferencesKey;
    }

    public String getSharedPreferencesValue() {
        return sharedPreferencesValue;
    }

    public String getSharedPreferencesType() {
        return sharedPreferencesType;
    }
}
