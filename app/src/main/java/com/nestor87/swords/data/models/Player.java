package com.nestor87.swords.data.models;

public class Player {
    private String uuid;
    private String name;
    private int score;
    private int hints;
    private long lastTimeOnline;

    public Player(String uuid, String name, int score, int hints) {
        this.uuid = uuid;
        this.name = name;
        this.score = score;
        this.hints = hints;
    }
    public Player(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public int getHints() {
        return hints;
    }

    public int getScore() {
        return score;
    }

    public String getUuid() {
        return uuid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setHints(int hints) {
        this.hints = hints;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public long getLastTimeOnline() {
        return lastTimeOnline;
    }

    public void setLastTimeOnline(long lastTimeOnline) {
        this.lastTimeOnline = lastTimeOnline;
    }
}
