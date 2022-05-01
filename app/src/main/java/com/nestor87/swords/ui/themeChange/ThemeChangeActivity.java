package com.nestor87.swords.ui.themeChange;

import android.content.Intent;
import android.content.SharedPreferences;

import androidx.cardview.widget.CardView;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.nestor87.swords.R;
import com.nestor87.swords.data.helpers.DBHelper;
import com.nestor87.swords.data.managers.DataManager;
import com.nestor87.swords.ui.BaseActivity;
import com.nestor87.swords.ui.main.MainActivity;
import com.nestor87.swords.utils.SystemUtils;

import static com.nestor87.swords.ui.main.MainActivity.APP_PREFERENCES_FILE_NAME;
import static com.nestor87.swords.utils.SystemUtils.getColorFromTheme;

public class ThemeChangeActivity extends BaseActivity {

    Spinner themeSpinner;
    Button button, previewButton;
    TextView priceTextView, currentThemeTextView;
    ImageView scoreImageView;
    DataManager dataManager;
    CardView colorCardView1, colorCardView2, colorCardView3, colorCardView4, colorCardView5, colorCardView6, colorCardView7, colorCardView8, colorCardView9;

    int[] themePrices = new int[] {0, 0, 2500, 5000, 4000};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_change);
        themeSpinner = findViewById(R.id.themeSpinner);
        button = findViewById(R.id.button);
        previewButton = findViewById(R.id.button2);
        priceTextView = findViewById(R.id.themePriceTextView);
        scoreImageView = findViewById(R.id.scoreImage2);

        colorCardView1 = findViewById(R.id.cardColor1);
        colorCardView2 = findViewById(R.id.cardColor2);
        colorCardView3 = findViewById(R.id.cardColor3);
        colorCardView4 = findViewById(R.id.cardColor4);
        colorCardView5 = findViewById(R.id.cardColor5);
        colorCardView6 = findViewById(R.id.cardColor6);
        colorCardView7 = findViewById(R.id.cardColor7);
        colorCardView8 = findViewById(R.id.cardColor8);
        colorCardView9 = findViewById(R.id.cardColor9);

        currentThemeTextView = findViewById(R.id.currentThemeTextView);

        button.setVisibility(View.INVISIBLE);
//        MainActivity.buttonSetEnabled(this, button, false);
        dataManager = new DataManager(findViewById(R.id.score), findViewById(R.id.hints), null, new Button[]{}, new DBHelper(this), this);
        dataManager.loadData();
        SharedPreferences preferences = getSharedPreferences(APP_PREFERENCES_FILE_NAME, MODE_PRIVATE);

        themeSpinner.setSelection(preferences.getInt("currentThemeId", 0));

        showColorPalette(preferences.getInt("currentThemeId", 0));


        themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int themeId, long id) {
//                MainActivity.buttonSetEnabled(getApplicationContext(), button, true);
                boolean isThemeAvailable = preferences.getBoolean("theme" + themeId, false) || themePrices[themeId] == 0;
                button.setText(isThemeAvailable ? "Применить" : "Купить");
                button.setVisibility(View.VISIBLE);
                previewButton.setVisibility(isThemeAvailable ? View.GONE : View.VISIBLE);
                if (!isThemeAvailable) {
                    priceTextView.setText("Цена: " + themePrices[themeId]);
                    scoreImageView.setVisibility(View.VISIBLE);
                } else {
                    priceTextView.setText("");
                    scoreImageView.setVisibility(View.INVISIBLE);
                    if (preferences.getInt("currentThemeId", 0) == themeId) {
                        button.setVisibility(View.INVISIBLE);
                    }
                }

                showColorPalette(themeId);
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (getIntent().getIntExtra("selectThemeId", -1) != -1) {
            themeSpinner.setSelection(getIntent().getIntExtra("selectThemeId", -1), false);
        }

        currentThemeTextView.setText(getResources().getStringArray(R.array.themes)[preferences.getInt("currentThemeId", 0)]);


    }

    public void applyTheme(View view) {
        int themeId = themeSpinner.getSelectedItemPosition();
        SharedPreferences preferences = getSharedPreferences(APP_PREFERENCES_FILE_NAME, MODE_PRIVATE);
        if (button.getText().equals("Применить")) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("currentThemeId", themeId);
            editor.apply();
            startActivity(new Intent(this, ThemeChangeActivity.class));
            Animatoo.animateFade(this);
        } else {
            try {
                dataManager.removeScore(themePrices[themeId]);
                MainActivity.playSound(R.raw.buy, this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("theme" + themeId, true);
                editor.apply();
                button.setText("Применить");
                priceTextView.setText("");
                scoreImageView.setVisibility(View.INVISIBLE);
            } catch (IllegalArgumentException e) {}

        }

    }

    private void showColorPalette(int themeId) {
        Resources.Theme selectedTheme = getResources().newTheme();
        selectedTheme.applyStyle(SystemUtils.getThemeResIdByThemeId(themeId), true);

        colorCardView1.setCardBackgroundColor(getColorFromTheme(android.R.attr.windowBackground, selectedTheme));
        colorCardView2.setCardBackgroundColor(getColorFromTheme(R.attr.buttonBackground, selectedTheme));
        colorCardView3.setCardBackgroundColor(getColorFromTheme(R.attr.buttonPressedBackground, selectedTheme));
        colorCardView4.setCardBackgroundColor(getColorFromTheme(R.attr.buttonText, selectedTheme));
        colorCardView5.setCardBackgroundColor(getColorFromTheme(R.attr.buttonBorder, selectedTheme));
        colorCardView6.setCardBackgroundColor(getColorFromTheme(R.attr.scoreAndHintsText, selectedTheme));
        colorCardView7.setCardBackgroundColor(getColorFromTheme(R.attr.wordText, selectedTheme));
        colorCardView8.setCardBackgroundColor(getColorFromTheme(R.attr.disabled, selectedTheme));
        colorCardView9.setCardBackgroundColor(getColorFromTheme(R.attr.hint, selectedTheme));
    }

    public void showPreview(View view) {
        int selectedThemeId = themeSpinner.getSelectedItemPosition();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("themePreviewId", selectedThemeId);
        startActivity(intent);
        Animatoo.animateSlideLeft(this);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        Animatoo.animateSlideRight(this);
    }

    public void goBack(View view) {
        onBackPressed();
    }
}