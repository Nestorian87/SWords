package com.nestor87.swords.data.models;

public class Message {
    public static String TYPE_WORD_ADDED = "word_added";

    String type = "";
    String title = "";
    String body;
    MessageReward reward;

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public MessageReward getReward() {
        return reward;
    }
}
