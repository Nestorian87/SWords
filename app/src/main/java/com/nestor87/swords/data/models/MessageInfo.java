package com.nestor87.swords.data.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class MessageInfo {
    int id;
    String accountId;
    int received = 0;
    int viewed = 0;
    String dateTime;
    Message message;

    public int getId() {
        return id;
    }

    public boolean isReceived() {
        return received == 1;
    }

    public boolean isViewed() {
        return viewed == 1;
    }

    public String getDateTime() {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateTime);
            return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public Message getMessage() {
        return message;
    }

    public String getAccountId() {
        return accountId;
    }
}
