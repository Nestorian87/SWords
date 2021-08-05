package com.nestor87.swords;

import android.content.Intent;
import android.content.SharedPreferences;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;

import static com.nestor87.swords.MainActivity.APP_PREFERENCES_FILE_NAME;

public class ThemeChangeActivity extends AppCompatActivity {

    Spinner themeSpinner;
    Button button;
    TextView priceTextView;
    ImageView scoreImageView;
    DataManager dataManager;

    int[] themePrices = new int[] {0, 0, 2500, 5000};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DataManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_change);
        themeSpinner = findViewById(R.id.themeSpinner);
        button = findViewById(R.id.button);
        priceTextView = findViewById(R.id.themePriceTextView);
        scoreImageView = findViewById(R.id.scoreImage2);

//        MainActivity.buttonSetEnabled(this, button, false);
        dataManager = new DataManager(findViewById(R.id.score), findViewById(R.id.hints), null, new Button[]{}, new DBHelper(this), this);
        dataManager.loadData();
        SharedPreferences preferences = getSharedPreferences(APP_PREFERENCES_FILE_NAME, MODE_PRIVATE);

        themeSpinner.setSelection(preferences.getInt("currentThemeId", 0));


        themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int themeId, long id) {
//                MainActivity.buttonSetEnabled(getApplicationContext(), button, true);
                boolean isThemeAvailable = preferences.getBoolean("theme" + themeId, false) || themePrices[themeId] == 0;
                button.setText(isThemeAvailable ? "Применить" : "Купить");
                if (!isThemeAvailable) {
                    priceTextView.setText("Цена: " + themePrices[themeId]);
                    scoreImageView.setVisibility(View.VISIBLE);
                } else {
                    priceTextView.setText("");
                    scoreImageView.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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