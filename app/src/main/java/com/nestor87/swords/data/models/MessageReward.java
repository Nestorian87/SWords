package com.nestor87.swords.data.models;

public class MessageReward {
    int score = 0;
    int hints = 0;
    String title = "Награда";
    boolean isReceived;

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
}
