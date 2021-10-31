package com.nestor87.swords.data.models;

import java.util.List;

public class ComposedWordsRequest {
    private String uuid;
    private List<String> composedWords;

    public ComposedWordsRequest(String uuid, List<String> composedWords) {
        this.uuid = uuid;
        this.composedWords = composedWords;
    }
}
