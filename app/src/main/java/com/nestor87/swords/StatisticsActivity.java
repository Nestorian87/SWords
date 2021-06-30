package com.nestor87.swords;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.nestor87.swords.AchievementAdapter.dataManager;
import static com.nestor87.swords.MainActivity.APP_PREFERENCES_FILE_NAME;

public class StatisticsActivity extends AppCompatActivity {

    TextView wordsCountTextView, wordOftenTextView, wordLongestTextView, wordAverageLengthTextView, timeInGameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DataManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        wordsCountTextView = findViewById(R.id.wordCount);
        wordOftenTextView = findViewById(R.id.wordOften);
        wordLongestTextView = findViewById(R.id.wordLongest);
        wordAverageLengthTextView = findViewById(R.id.wordAverageLength);
        timeInGameTextView = findViewById(R.id.timeInGame);

        dataManager = new DataManager(findViewById(R.id.score), findViewById(R.id.hints), null, new Button[]{}, new DBHelper(this), this);
        dataManager.loadData();

        String[] statisticsWords = dataManager.getStatisticsWords();

        int wordsCount = statisticsWords.length;
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