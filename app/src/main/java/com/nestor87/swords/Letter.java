package com.nestor87.swords;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.google.android.material.internal.ViewUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class Letter implements Cloneable {
    public static final ArrayList<Letter> LETTERS = new ArrayList<>(Arrays.asList(
            new Letter('о', 9.58, true),
            new Letter('е', 8.06, true),
            new Letter('а', 9.86, true),
            new Letter('и', 8.46, true),
            new Letter('н', 7.04),
            new Letter('т', 6.15),
            new Letter('с', 5.11),
            new Letter('р', 6.11),
            new Letter('в', 3.9),
            new Letter('л', 4.28),
            new Letter('к', 5.3),
            new Letter('м', 2.39),
            new Letter('д', 2.39),
            new Letter('п', 3.18),
            new Letter('у', 2.09, true),
            new Letter('я', 1.27, true),
            new Letter('ы', 0.75, true),
            new Letter('ь', 2.24),
            new Letter('г', 1.42),
            new Letter('з', 1.71),
            new Letter('б', 1.62),
            new Letter('ч', 1.39),
            new Letter('й', 0.39),
            new Letter('х', 0.59),
            new Letter('ж', 0.7),
            new Letter('ш', 0.81),
            new Letter('ю', 0.25, true),
            new Letter('ц', 1.14),
            new Letter('щ', 0.55),
            new Letter('э', 0.2, true),
            new Letter('ф', 0.6),
            new Letter('ъ', 0.03),
            new Letter('ё', 0.44, true)
    ));

    public static final int COLOR_NONE = 0, COLOR_RED = 1, COLOR_BLUE = 2, COLOR_YELLOW = 3;

    private char symbol;
    private double frequency;
    private boolean vowel = false;
    private int color = COLOR_NONE;

    public Letter(char symbol, double frequency) {
        this.symbol = Character.toUpperCase(symbol);
        this.frequency = frequency;
    }
    public Letter(char symbol, double frequency, boolean vowel){
        this.symbol = Character.toUpperCase(symbol);
        this.frequency = frequency;
        this.vowel = vowel;
    }

    public char getSymbol() {
        return symbol;
    }

    public double getFrequency() {
        return frequency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Letter letter = (Letter) o;

        return symbol == letter.symbol;
    }


    public static Letter getLetter(char letter){
        letter = Character.toUpperCase(letter);
        Letter result = null;
        for (int i = 0; i < LETTERS.size(); i++) {
            if (LETTERS.get(i).getSymbol() == letter) {
                result = LETTERS.get(i);
                break;
            }
        }
        if (result == null)
            throw new IllegalArgumentException("Not found letter for " + letter);
        else
            return result;
    }

    public static Letter getRandomLetter(ArrayList<Letter> playerLetters) {
        ArrayList<String> playerLettersString = new ArrayList<>();
        for (Letter letter : playerLetters) {
            playerLettersString.add(Character.toString(letter.getSymbol()));
        }
        ArrayList<Letter> letters = new ArrayList<>();

        int playerLettersVowels = 0;
        int playerLettersConsonants = 0;

        for (Letter letter : playerLetters) {
            if (letter.isVowel())
                playerLettersVowels++;
            else
                playerLettersConsonants++;
        }

        for (Letter letter : LETTERS) {
            int letterFrequency = (int) (letter.getFrequency() * 100);
            if (playerLettersString.contains(Character.toString(letter.getSymbol())) && playerLetters.size() > 5) {
                letterFrequency -= (letterFrequency / 12) * Collections.frequency(playerLettersString, Character.toString(letter.getSymbol()));
            }


            if (playerLettersVowels >= playerLettersConsonants) {
                if (!letter.isVowel()) {
                    letterFrequency += letterFrequency / 9;
                }
            } else if (playerLettersConsonants - playerLettersVowels > 1) {
                if (letter.isVowel()) {
                    letterFrequency += letterFrequency / 9;
                }
            }

            for (int i = 0; i < letterFrequency; i++) {
                letters.add(letter);
            }
        }
        Letter randomLetter = null;
        try {
            randomLetter = ((Letter) MainActivity.getRandomElementFromArrayList(letters)).clone();
        } catch (CloneNotSupportedException e) {};

        randomLetter.coloredLetterChance();

        return randomLetter;
    }

    public boolean isVowel() {
        return vowel;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return Character.toString(symbol);
    }

    public int getColor() {
        return color;
    }

    public void coloredLetterChance() {
        int coloredLetterChance = (int) (Math.random() * 100 + 1);

        if (coloredLetterChance <= 5) {
            int colorChance = (int) (Math.random() * 100 + 1);
            if (colorChance >= 50)
                setColor(COLOR_YELLOW);
            else if (colorChance > 15)
                setColor(COLOR_BLUE);
            else
                setColor(COLOR_RED);
        }
    }

    public @ColorInt int getButtonTextColor(Context context) {
        if (color == COLOR_RED)
            return MainActivity.getColorFromTheme(R.attr.redButton, context);
        else if (color == COLOR_BLUE)
            return MainActivity.getColorFromTheme(R.attr.blueButton, context);
        else if (color == COLOR_YELLOW)
            return MainActivity.getColorFromTheme(R.attr.yellowButton, context);
        else
            return MainActivity.getColorFromTheme(R.attr.buttonText, context);
    }


    @NonNull
    @Override
    public Letter clone() throws CloneNotSupportedException {
        return (Letter) super.clone();
    }
}
