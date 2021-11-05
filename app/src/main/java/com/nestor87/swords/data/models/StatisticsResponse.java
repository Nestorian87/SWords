package com.nestor87.swords.data.models;

public class StatisticsResponse {
    private Player player;
    private int wordCount;
    private int uniqueWordCount;
    private String mostFrequentWord;
    private String longestWord;
    private double averageWordLength;

    public Player getPlayer() {
        return player;
    }

    public String getWordCount() {
        return Integer.toString(wordCount);
    }

    public String getAverageWordLength() {
        return Double.toString(averageWordLength);
    }

    public String getUniqueWordCount() {
        return Integer.toString(uniqueWordCount);
    }

    public String getLongestWord() {
        return longestWord == null ? "-" : longestWord;
    }

    public String getMostFrequentWord() {
        return mostFrequentWord == null ? "-" : mostFrequentWord;
    }
}
