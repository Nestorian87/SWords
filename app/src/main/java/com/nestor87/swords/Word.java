package com.nestor87.swords;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

public class Word {
    private String text;
    private ArrayList<Letter> letters = new ArrayList<>();
    private int color = Letter.COLOR_NONE;
    private boolean red = false, blue = false, yellow = false;
    private String meaning;
    public Word(String text) {
        this.text = text;
        for (char letter : text.toCharArray()) {
            try {
                this.letters.add(Letter.getLetter(letter).clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
    }

    public Word(String text, int color) {
        this(text);
        this.color = color;
        if (color == Letter.COLOR_RED)
            this.red = true;
        if (color == Letter.COLOR_BLUE)
            this.blue = true;
        if (color == Letter.COLOR_YELLOW)
            this.yellow = true;
    }

    public Word(String text, int color, boolean red, boolean blue, boolean yellow) {
        this(text, color);
        this.red = red || this.red;
        this.yellow = yellow || this.yellow;
        this.blue = blue || this.blue;
    }

    public String getText() {
        return text;
    }

    public ArrayList<Letter> getLetters() {
        return letters;
    }

    public int getWordPrice() {
        int wordPrice = 0;

        for (char symbol : text.toCharArray()){
            Letter letter = Letter.getLetter(symbol);
            wordPrice += 5.5 / Math.ceil(letter.getFrequency());
        }
        double wordLengthCoef;
        switch (text.length()) {
            case 2: wordLengthCoef = 0.7; break;
            case 3: wordLengthCoef = 0.8; break;
            case 4: wordLengthCoef = 0.9; break;
            case 5: wordLengthCoef = 1.1; break;
            case 6: wordLengthCoef = 1.3; break;
            case 7: wordLengthCoef = 1.5; break;
            default: wordLengthCoef = 1.7;
        }
        wordPrice = (int) Math.round(wordPrice * wordLengthCoef);

        if (wordPrice <= 0)
            wordPrice = 1;

        if (blue)
            wordPrice *= 2;
        if (red)
            wordPrice *= 3;


        Log.i(MainActivity.LOG_TAG, "Price of the word '" + text + "' is " + wordPrice);
        return wordPrice;

    }

    boolean exists(DBHelper dbHelper) {

        boolean exists = false;
        SQLiteDatabase db = dbHelper.openDB();
//        Log.i(MainActivity.LOG_TAG, "Exists: checking (" + text.toLowerCase() + ")");
        Cursor query = db.rawQuery("SELECT word FROM words WHERE word = ?;", new String[] {text.toLowerCase()});
        if (query.moveToFirst()) {
            exists = true;
//            Log.i(MainActivity.LOG_TAG, "YES");
        } else {
//            Log.i(MainActivity.LOG_TAG, "NO");
        }
        query.close();
        db.close();
        return exists;
    }

    public int getColor() {
        return color;
    }

    public boolean isEmpty() {
        return this.text.isEmpty();
    }

    public boolean isRed() {
        return red;
    }

    public boolean isBlue() {
        return blue;
    }

    public boolean isYellow() {
        return yellow;
    }

    public void setRed(boolean red) {
        this.red = red;
    }

    public void setBlue(boolean blue) {
        this.blue = blue;
    }

    public void setYellow(boolean yellow) {
        this.yellow = yellow;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getMeaning() {
        return meaning;
    }
}
