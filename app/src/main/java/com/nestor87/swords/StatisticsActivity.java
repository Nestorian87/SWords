package com.nestor87.swords;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.nestor87.swords.AchievementAdapter.dataManager;
import static com.nestor87.swords.MainActivity.APP_PREFERENCES_FILE_NAME;

public class StatisticsActivity extends AppCompatActivity {

    TextView wordsCountTextView, wordOftenTextView, wordLongestTextView, wordAverageLengthTextView, timeInGameTextView, uniqueWordsCountTextView, todayWordsCountTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DataManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        wordsCountTextView = findViewById(R.id.wordCount);
        uniqueWordsCountTextView = findViewById(R.id.uniqueWordCount);
        todayWordsCountTextView = findViewById(R.id.todayWordCount);
        wordOftenTextView = findViewById(R.id.wordOften);
        wordLongestTextView = findViewById(R.id.wordLongest);
        wordAverageLengthTextView = findViewById(R.id.wordAverageLength);
        timeInGameTextView = findViewById(R.id.timeInGame);

        dataManager = new DataManager(findViewById(R.id.score), findViewById(R.id.hints), null, new Button[]{}, new DBHelper(this), this);
        dataManager.loadData();

        String[] statisticsWords = dataManager.getStatisticsWords();

        int todayWordsCount = 0;

        for (int i = 0; i < statisticsWords.length; i++) {
            if (statisticsWords[i].split("/").length == 2) {
                long timestamp = Long.parseLong(statisticsWords[i].split("/")[1]);
                long millisecondsInDay = 86400000;
                if (System.currentTimeMillis() - timestamp <= millisecondsInDay) {
                    todayWordsCount++;
                }
                statisticsWords[i] = statisticsWords[i].split("/")[0];
            }
        }

        int wordsCount = statisticsWords.length;

        int uniqueWordsCount = new HashSet<String>(Arrays.asList(statisticsWords)).size();

        String wordOften = null,
               lastWord = null;
        int wordOftenCount = 1,
            maxWordOftenCount = 1;
        String longestWord = null;
        double wordAverageLength = 0;

        for (String word : statisticsWords) {

            if (lastWord != null) {
                if (lastWord.equals(word))
                    wordOftenCount++;
                else
                    wordOftenCount = 1;

                if (wordOftenCount > maxWordOftenCount) {
                    maxWordOftenCount = wordOftenCount;
                    wordOften = word;
                }
            }

            if (longestWord == null)
                longestWord = word;
            else {
                if (lastWord.length() > longestWord.length()) {
                    longestWord = lastWord;
                }
            }

            lastWord = word;
            wordAverageLength += word.length();


        }

        if (wordsCount > 0) {
            wordAverageLength /= (double) wordsCount;
        }

        int minutesInGame = getSharedPreferences(APP_PREFERENCES_FILE_NAME, MODE_PRIVATE).getInt("minutesInGame", 0);

        wordsCountTextView.setText(wordsCountTextView.getText() + Integer.toString(wordsCount));
        uniqueWordsCountTextView.setText(uniqueWordsCountTextView.getText() + Integer.toString(uniqueWordsCount));
        todayWordsCountTextView.setText(todayWordsCountTextView.getText() + Integer.toString(todayWordsCount));
        wordOftenTextView.setText(wordOftenTextView.getText() + (wordOften == null ? "-" : wordOften));
        wordLongestTextView.setText(wordLongestTextView.getText() + (longestWord == null ? "-" : longestWord));
        wordAverageLengthTextView.setText(wordAverageLengthTextView.getText() + String.format(Locale.US, "%.2f", wordAverageLength));
        timeInGameTextView.setText(timeInGameTextView.getText().toString() + minutesInGame / 60 + " ч " + minutesInGame % 60 + " мин");

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        Animatoo.animateSlideRight(this);
    }

    public void goBack(View view) {
        onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();
        MainActivity.onActivityStart(this);
    }

    @Override
    protected void onStop() {
        MainActivity.onActivityStop(this);
        super.onStop();
    }
}