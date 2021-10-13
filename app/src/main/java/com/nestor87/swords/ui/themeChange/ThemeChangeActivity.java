package com.nestor87.swords.ui.themeChange;

import android.content.Intent;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;
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
import com.nestor87.swords.data.DBHelper;
import com.nestor87.swords.data.DataManager;
import com.nestor87.swords.ui.main.MainActivity;

import static com.nestor87.swords.ui.main.MainActivity.APP_PREFERENCES_FILE_NAME;
import static com.nestor87.swords.ui.main.MainActivity.getColorFromTheme;

public class ThemeChangeActivity extends AppCompatActivity {

    Spinner themeSpinner;
    Button button, previewButton;
    TextView priceTextView, currentThemeTextView;
    ImageView scoreImageView;
    DataManager dataManager;
    CardView colorCardView1, colorCardView2, colorCardView3, colorCardView4, colorCardView5, colorCardView6, colorCardView7, colorCardView8, colorCardView9;

    int[] themePrices = new int[] {0, 0, 2500, 5000};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DataManager.applyTheme(this);
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
                previewButton.setVisibility(isThemeAvailable ? View.GONE : View.VISIBLE);
                if (!isThemeAvailable) {
                    priceTextView.setText("Цена: " + themePrices[themeId]);
                    scoreImageView.setVisibility(View.VISIBLE);
                } else {
                    priceTextView.setText("");
                    scoreImageView.setVisibility(View.INVISIBLE);
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
        selectedTheme.applyStyle(DataManager.getThemeResIdByThemeId(themeId), true);

        colorCardView1.setCardBackgroundColor(getColorFromTheme(android.R.attr.windowBackground, selectedTheme, this));
        colorCardView2.setCardBackgroundColor(getColorFromTheme(R.attr.buttonBackground, selectedTheme, this));
        colorCardView3.setCardBackgroundColor(getColorFromTheme(R.attr.buttonPressedBackground, selectedTheme, this));
        colorCardView4.setCardBackgroundColor(getColorFromTheme(R.attr.buttonText, selectedTheme, this));
        colorCardView5.setCardBackgroundColor(getColorFromTheme(R.attr.buttonBorder, selectedTheme, this));
        colorCardView6.setCardBackgroundColor(getColorFromTheme(R.attr.scoreAndHintsText, selectedTheme, this));
        colorCardView7.setCardBackgroundColor(getColorFromTheme(R.attr.wordText, selectedTheme, this));
        colorCardView8.setCardBackgroundColor(getColorFromTheme(R.attr.disabled, selectedTheme, this));
        colorCardView9.setCardBackgroundColor(getColorFromTheme(R.attr.hint, selectedTheme, this));
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