package com.nestor87.swords.data.models;

public class MessageRewardReceivedRequest {
    int messageId;
    String uuid;
    
    public MessageRewardReceivedRequest(int messageId, String uuid) {
        this.messageId = messageId;
        this.uuid = uuid;
    }
}
