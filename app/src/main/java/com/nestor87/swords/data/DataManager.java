package com.nestor87.swords.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.nestor87.swords.R;
import com.nestor87.swords.data.models.Achievement;
import com.nestor87.swords.data.models.ComposedWordsRequest;
import com.nestor87.swords.data.models.Letter;
import com.nestor87.swords.data.models.Player;
import com.nestor87.swords.data.models.UpdateUserResponse;
import com.nestor87.swords.data.models.Word;
import com.nestor87.swords.data.network.NetworkService;
import com.nestor87.swords.ui.main.MainActivity;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;
import static com.nestor87.swords.ui.main.MainActivity.APP_PREFERENCES_FILE_NAME;
import static com.nestor87.swords.ui.main.MainActivity.LOG_TAG;
import static com.nestor87.swords.ui.main.MainActivity.getColorFromTheme;
import static com.nestor87.swords.ui.main.MainActivity.uuid;

public class DataManager {

    private int score = 0, hints = 0;
    private TextView scoreTextView, hintsTextView, wordTextView;
    private Word word = new Word("");
    private DBHelper dbHelper;
    private Context context;
    Button[] letterButtons;
    public ArrayList<String> allWords = new ArrayList<>();
    private Word hintWord = null;
    private int hintIndex = 0;
    public ArrayList<Integer> lettersWithHintsIndexes = new ArrayList<>();
    private Player selfPlayer;
    private long lastTimeAccountUpdate = 0;
    private boolean isHintsLoaded = false;

    public DataManager(TextView scoreTextView, TextView hintsTextView, TextView wordTextView, Button[] letterButtons, DBHelper dbHelper, Context context) {
        this.scoreTextView = scoreTextView;
        this.hintsTextView = hintsTextView;
        this.wordTextView = wordTextView;
        this.dbHelper = dbHelper;
        this.context = context;
        this.letterButtons = letterButtons;


        ArrayList<String> words = new ArrayList<>();
        ArrayList<Letter> playerLetters = getPlayerLetters();
        SQLiteDatabase db = dbHelper.openDB();
        Cursor c = db.rawQuery("SELECT word FROM words", null);
        if (c.moveToFirst()) {
            do {
                String word = c.getString(0);
                if (!word.contains("-"))
                    allWords.add(word);
            } while (c.moveToNext());
        }
        c.close();
        db.close();

        this.allWords = allWords;

        SharedPreferences preferences = context.getSharedPreferences(APP_PREFERENCES_FILE_NAME, MODE_PRIVATE);
        selfPlayer = new Player(preferences.getString("accountId", ""), preferences.getString("name", ""), score, hints);
    }

    public int getScore() {
        return score;
    }

    public int getHints() {
        return hints;
    }


    public void setScore(int score) {
        if (score < 0) {
            MainActivity.playSound(R.raw.error, context);
            new AlertDialog.Builder(context).setTitle("Недостаточно очков").setIcon(R.drawable.score)
                    .setMessage("У вас недостаточно очков для совершения этой операции").setNeutralButton(android.R.string.ok, null).show();
            throw new IllegalArgumentException("Score must be greater than 0");
        }

        this.score = score;
        scoreTextView.setText(DataManager.formatNumberToStringWithSpacingDecimalPlaces(score));
        saveData();


    }

    public void addScore(int value) {
        try {
            setScore(getScore() + value);
            Achievement.addProgress(Achievement.SCORE_INCREASE_TRIGGER, value, context);
        } catch (IllegalArgumentException e) {}
    }

    public void removeScore(int value) {
        setScore(getScore() - value);
    }

    public void setHints(int hints) {
        if (hints < 0) {
            MainActivity.playSound(R.raw.error, context);
            new AlertDialog.Builder(context).setTitle("Недостаточно подсказок").setIcon(R.drawable.hints)
                    .setMessage("У вас недостаточно подсказок для совершения этой операции").setNeutralButton(android.R.string.ok, null).show();
            throw new IllegalArgumentException("Hints count must be greater than 0");
        }
        this.hints = hints;
        hintsTextView.setText(DataManager.formatNumberToStringWithSpacingDecimalPlaces(hints));
        isHintsLoaded = true;
        saveData();
    }

    public void addHints(int value) {
        setHints(getHints() + value);
    }

    public void removeHints(int value) {
        try {
            setHints(getHints() - value);
            Achievement.addProgress(Achievement.HINTS_REDUCE_TRIGGER, value, context);
        } catch (IllegalArgumentException e) {}
    }

    public Word getWord() {
        return word;
    }

    public void setWord(String word, int color) {
        this.word = !word.isEmpty() ? new Word(word, color == Letter.COLOR_NONE ? this.word.getColor() : color,
                    this.word.isRed(), this.word.isBlue(), this.word.isYellow()) : new Word("");
        wordTextView.setText(word);


        if (this.word.getColor() == Letter.COLOR_NONE)
            wordTextView.setTextColor(getColorFromTheme(R.attr.wordText, context));
        if (this.word.getColor() == Letter.COLOR_RED)
            wordTextView.setTextColor(getColorFromTheme(R.attr.redButton, context));
        else if (this.word.getColor() == Letter.COLOR_BLUE)
            wordTextView.setTextColor(getColorFromTheme(R.attr.blueButton, context));
        else if (this.word.getColor() == Letter.COLOR_YELLOW)
            wordTextView.setTextColor(getColorFromTheme(R.attr.yellowButton, context));

        if (word.length() > 7)
            wordTextView.setTextSize(27);
        else
            wordTextView.setTextSize(34);
    }

    public void addLetterToWord(Letter letter) {
        setWord(getWord().getText() + letter.getSymbol(), letter.getColor());
    }

    public void clearWord() {
        setWord("", Letter.COLOR_NONE);
    }

    public void setLetterButtons(String[] letters) {
        for (int i = 0; i < letterButtons.length; i++) {
            String letter = Character.toString(letters[i].charAt(0));
            letterButtons[i].setText(letter.toUpperCase());
            int letterColor;
            try {
                letterColor = Integer.parseInt(Character.toString(letters[i].charAt(1)));
            } catch (Exception e) {
               letterColor = Letter.COLOR_NONE;
            }

            if (letterColor == Letter.COLOR_RED)
                letterButtons[i].setTextColor(getColorFromTheme(R.attr.redButton, context));
            else if (letterColor == Letter.COLOR_BLUE)
                letterButtons[i].setTextColor(getColorFromTheme(R.attr.blueButton, context));
            else if (letterColor == Letter.COLOR_YELLOW)
                letterButtons[i].setTextColor(getColorFromTheme(R.attr.yellowButton, context));

        }
        saveData();
    }

    public ArrayList<Letter> getPlayerLetters() {
        ArrayList<Letter> playerLetters = new ArrayList<>();
        for (Button letterButton : letterButtons) {
            playerLetters.add(Letter.getLetter(letterButton.getText().toString().charAt(0)));
        }
        return playerLetters;
    }

    public void loadData() {
        SQLiteDatabase db = dbHelper.openDB();
        Cursor query = db.rawQuery("SELECT * FROM data;", null);
        if (query.moveToFirst()) {
            setScore(query.getInt(query.getColumnIndex("score")));
            setHints(query.getInt(query.getColumnIndex("hints")));
            setLetterButtons(query.getString(query.getColumnIndex("letters")).split(" "));
        }
        query.close();
        db.close();
    }

    public void saveData() {
        SQLiteDatabase db = dbHelper.openDB();
        ContentValues contentValues = new ContentValues();
        contentValues.put("score", score);
        contentValues.put("hints", hints);

        if (letterButtons.length > 0) {
            String letters = "";
            for (Button letterButton : letterButtons) {
                letters += letterButton.getText();
                if (letterButton.getCurrentTextColor() == getColorFromTheme(R.attr.redButton, context))
                    letters += Letter.COLOR_RED + " ";
                else if (letterButton.getCurrentTextColor() == getColorFromTheme(R.attr.blueButton, context))
                    letters += Letter.COLOR_BLUE + " ";
                else if (letterButton.getCurrentTextColor() == getColorFromTheme(R.attr.yellowButton, context))
                    letters += Letter.COLOR_YELLOW + " ";
                else
                    letters += Letter.COLOR_NONE + " ";

            }

            letters = letters.substring(0, letters.length() - 1);
            contentValues.put("letters", letters);
        }
        db.update("data", contentValues, "_ID = 0", null);
        db.close();

        selfPlayer.setScore(score);
        selfPlayer.setHints(hints);

        if (isHintsLoaded) {
            updateAccount();
        }
    }

    public String[] getStatisticsWords() {
        ArrayList<String> words = new ArrayList<>();
        SQLiteDatabase db = dbHelper.openDB();
        Cursor c = db.rawQuery("SELECT word FROM statisticsWords ORDER BY word", null);
        if (c.moveToFirst()) {
            do {
                String word = c.getString(0);
                words.add(word);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return words.toArray(new String[]{});
    }

    public String[] getStatisticsWordsSortedByTime() {
        ArrayList<String> words = new ArrayList<>();
        SQLiteDatabase db = dbHelper.openDB();
        Cursor c = db.rawQuery("SELECT word FROM statisticsWords", null);
        if (c.moveToFirst()) {
            do {
                String word = c.getString(0);
                words.add(word);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return words.toArray(new String[]{});
    }

    public void addWordToStatistics(String word) {
        SQLiteDatabase db = dbHelper.openDB();
        ContentValues contentValues = new ContentValues();
        contentValues.put("word", word + "/" + System.currentTimeMillis());
        db.insert("statisticsWords", null, contentValues);
        db.close();
    }


    public String findWord(ArrayList<Letter> letters, boolean shortest) {
        ((MainActivity) context).runOnUiThread(() -> {
            ((MainActivity) context).findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            ((MainActivity) context).findViewById(R.id.useHintButton).setEnabled(false);
        });

                long time = System.currentTimeMillis();

                ArrayList<Letter> playerLetters = letters;

                if (shortest) {
                    Collections.sort(allWords, (o1, o2) -> {
                        if (o1.length() > o2.length())
                            return 1;
                        else if (o1.length() < o2.length())
                            return -1;
                        else
                            return 0;
                    });
                } else {
                    Collections.sort(allWords, (o1, o2) -> {
                        if (o1.length() < o2.length())
                            return 1;
                        else if (o1.length() > o2.length())
                            return -1;
                        else
                            return 0;
                    });
                }

                for (String word : allWords) {
                    if (!word.contains("-")) {
                        ArrayList<Letter> playerLettersForWord = new ArrayList<>(playerLetters);
                        boolean isWord = true;
                        for (Letter letter : new Word(word).getLetters()) {
                            if (!playerLettersForWord.contains(letter)) {
                                isWord = false;
                                break;
                            } else {
                                playerLettersForWord.remove(letter);
                            }
                        }
                        if (isWord) {
                            ((MainActivity) context).runOnUiThread(() -> {
                                ((MainActivity) context).findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                                ((MainActivity) context).findViewById(R.id.useHintButton).setEnabled(true);
                            });
                            Log.i(LOG_TAG, "Word is " + word + "; findWords time: " + Long.toString(System.currentTimeMillis() - time) + " ms");
                            return word;
                        }

                    }
                }
                ((MainActivity) context).runOnUiThread(() -> {
                    ((MainActivity) context).findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                    ((MainActivity) context).findViewById(R.id.useHintButton).setEnabled(true);
                });
                return null;


    }

    public Word getHintWord() {
        return hintWord;
    }

    public void setHintWord(Word hintWord) {
        this.hintWord = hintWord;
        if (hintWord == null) {
            for (Button letterButton : MainActivity.letterButtons) {

                letterButton.setBackgroundResource(R.drawable.letter_button);
            }
            lettersWithHintsIndexes.clear();
        }
    }

    public int getHintIndex() {
        return hintIndex;
    }

    public void incrementHintIndex() {
        hintIndex++;
    }

    public void resetHintIndex() {
        hintIndex = 0;
    }

    public static void applyTheme(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(APP_PREFERENCES_FILE_NAME, MODE_PRIVATE);
        context.setTheme(getThemeResIdByThemeId(preferences.getInt("currentThemeId", 0)));
    }

    public static void loadWords(Context context) {
        DBHelper dbHelper = new DBHelper(context);

        NetworkService.getInstance().getSWordsApi().getAllWords().enqueue(
                new Callback<List<String>>() {
                    @Override
                    public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                        if (!response.body().isEmpty()) {
                            long startLoadWordsTime = System.currentTimeMillis();
                            SQLiteDatabase db = dbHelper.openDB();

                            db.beginTransaction();
                            db.delete("words", null, null);

                            StringBuilder sql = new StringBuilder("INSERT INTO words (word) VALUES ");
                            for (String word : response.body()) {
                                sql.append("(\"").append(word).append("\"), ");
                            }
                            sql = new StringBuilder(sql.substring(0, sql.length() - 2));
                            Log.i(LOG_TAG, "LOAD_WORDS SQL_PREPARED: " + (System.currentTimeMillis() - startLoadWordsTime) + " ms");
                            db.execSQL(sql.toString());
                            db.setTransactionSuccessful();
                            db.endTransaction();
                            Log.i(LOG_TAG, "LOAD_WORDS: " + (System.currentTimeMillis() - startLoadWordsTime) + " ms");
                            db.close();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<String>> call, Throwable t) {
                        Log.e(MainActivity.LOG_TAG, "ERROR LOAD_WORDS: " + t.getMessage());
                    }
                }
        );
    }


    public void registerAccount() {
        NetworkService.getInstance().getSWordsApi().registerPlayer("Bearer " + MainActivity.accountManagerPassword, selfPlayer).enqueue(
                new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        Log.i(LOG_TAG, "success");
                        SharedPreferences preferences = context.getSharedPreferences(APP_PREFERENCES_FILE_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor preferencesEditor = preferences.edit();
                        preferencesEditor.putBoolean("isAccountRegistered", true);
                        preferencesEditor.apply();
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.i(LOG_TAG, "failure");
                    }
                }
        );
    }

    public void updateAccount() {
        if (System.currentTimeMillis() - lastTimeAccountUpdate > 1000) {
            lastTimeAccountUpdate = System.currentTimeMillis();
            NetworkService.getInstance().getSWordsApi().updateUser(MainActivity.getBearerToken(), selfPlayer).enqueue(
                    new Callback<UpdateUserResponse>() {
                        @Override
                        public void onResponse(Call<UpdateUserResponse> call, Response<UpdateUserResponse> response) {
                            Log.i(LOG_TAG, "success");
                            String[] statisticsWords = getStatisticsWordsSortedByTime();
                            if (statisticsWords.length > response.body().getWordsCount()) {
                                String[] words = Arrays.copyOfRange(statisticsWords, response.body().getWordsCount(), statisticsWords.length);

                                for (int i = 0; i < words.length; i++) {
                                    words[i] = words[i].split("/")[0];
                                }
                                NetworkService.getInstance().getSWordsApi().sendComposedWords(MainActivity.getBearerToken(), new ComposedWordsRequest(uuid, Arrays.asList(words))).enqueue(
                                        new Callback<Void>() {
                                            @Override
                                            public void onResponse(Call<Void> call, Response<Void> response) {
                                                Log.i(LOG_TAG, "success");
                                            }

                                            @Override
                                            public void onFailure(Call<Void> call, Throwable t) {
                                                Log.i(LOG_TAG, "failure");
                                            }
                                        }
                                );
                            }
                        }

                        @Override
                        public void onFailure(Call<UpdateUserResponse> call, Throwable t) {
                            Log.i(LOG_TAG, "failure");
                        }
                    }
            );
        }
    }

    public void setName(String name) {
        selfPlayer.setName(name);
    }

    public static int getThemeResIdByThemeId(int themeId) {
        switch (themeId) {
            case 1:
                return R.style.SWords_dark;
            case 2:
                return R.style.SWords_white;
            case 3:
                return R.style.SWords_darkBlue;
            default:
                return R.style.SWords_standard;
        }
    }

    public int dpToPx(int dp) {
        return (int) Math.round(
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        dp,
                        context.getResources().getDisplayMetrics()
                )
        );
    }

    public static void adjustFontScale(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        if (configuration.fontScale > 1f) {
            configuration.fontScale = 1f;
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getMetrics(metrics);
            metrics.scaledDensity = configuration.fontScale * metrics.density;
            context.getResources().updateConfiguration(configuration, metrics);
        }
    }

    public static String formatNumberToStringWithSpacingDecimalPlaces(int number) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("ru", "RU"));
        return nf.format(number);
    }
}

