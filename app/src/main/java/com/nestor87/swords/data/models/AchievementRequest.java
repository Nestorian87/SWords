package com.nestor87.swords.data.models;

public class AchievementRequest {
    private String uuid;
    private String achievementId;
    private int achievementProgress;

    public AchievementRequest(String uuid, String achievementId, int achievementProgress) {
        this.uuid = uuid;
        this.achievementId = achievementId;
        this.achievementProgress = achievementProgress;
    }
}
