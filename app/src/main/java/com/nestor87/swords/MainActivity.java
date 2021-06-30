package com.nestor87.swords;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Vibrator;
import androidx.annotation.ColorInt;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.nestor87.swords.Achievement.ACHIEVEMENTS;
import static com.nestor87.swords.Achievement.HINTS_CURRENCY;
import static com.nestor87.swords.Achievement.HINTS_REDUCE_TRIGGER;
import static com.nestor87.swords.Achievement.SCORE_CURRENCY;
import static com.nestor87.swords.Achievement.SCORE_INCREASE_TRIGGER;

public class MainActivity extends AppCompatActivity {

    private TextView scoreTextView, hintsTextView, wordTextView;
    private DataManager dataManager;
    public static Button[] letterButtons;
    private Button eraseButton, mixButton, setWordButton;
    private DBHelper dbHelper;
    private ProgressBar progressBar;



    public static final String LOG_TAG = "SWORDS_DEBUG";
    public static final String APP_PREFERENCES_FILE_NAME = "preferences";
    private String lastWordMade = null;
    public static int startedActivitiesCount = 0;
    public static final String accountManagerPassword = "$2y$10$6UiaU230HP2IuSn.QUeyoulrm7YUBvSMv44QThkasbkQQagZvBj3K";
    public static String uuid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DataManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN  | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = getSharedPreferences(APP_PREFERENCES_FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putString("accountId", preferences.getString("accountId", UUID.randomUUID().toString()));
        preferencesEditor.apply();

        uuid = preferences.getString("accountId", "");

        if (!preferences.contains("name")) {
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            View nameView = layoutInflater.inflate(R.layout.input_name_dialog, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(nameView);
            EditText nameEditText = nameView.findViewById(R.id.nameEditText);
            builder.setCancelable(false).setNeutralButton("OK", (dialog, which) -> {
                if (nameEditText.getText().toString().isEmpty()) {
                    Toast.makeText(this, "Введите имя!", Toast.LENGTH_SHORT).show();
                    builder.show();
                } else {
                    preferencesEditor.putString("name", nameEditText.getText().toString());
                    preferencesEditor.apply();
                }
            });
            builder.show();
        }

        if (!preferences.getBoolean("isAccountRegistered", false)) {
            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://ananiev-nestor.kl.com.ua/SWords_account_manager.php",
                    response -> {
                        preferencesEditor.putBoolean("isAccountRegistered", true);
                        preferencesEditor.apply();
                    },
                    error -> {

                    }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("add", "true");
                    params.put("name", preferences.getString("name", ""));
                    params.put("uuid", uuid);
                    params.put("password", accountManagerPassword);
                    return params;
                }
            };
            queue.add(stringRequest);
        }

        scoreTextView = findViewById(R.id.score);
        hintsTextView = findViewById(R.id.hints);
        wordTextView = findViewById(R.id.word);
        eraseButton = findViewById(R.id.erase);
        mixButton = findViewById(R.id.mix);
        setWordButton = findViewById(R.id.setWord);
        progressBar = findViewById(R.id.progressBar);

        letterButtons = new Button[] {
                findViewById(R.id.letter1), findViewById(R.id.letter2), findViewById(R.id.letter3), findViewById(R.id.letter4),
                findViewById(R.id.letter5), findViewById(R.id.letter6), findViewById(R.id.letter7), findViewById(R.id.letter8), findViewById(R.id.letter9),
                findViewById(R.id.letter10), findViewById(R.id.letter11), findViewById(R.id.letter12), findViewById(R.id.letter13), findViewById(R.id.letter14),
                findViewById(R.id.letter15)
        };

        dbHelper = new DBHelper(this);
        dbHelper.copyDBIfNotExists();
        dataManager = new DataManager(scoreTextView, hintsTextView, wordTextView, letterButtons, dbHelper, this);

        buttonSetEnabled(eraseButton, false);        dataManager.loadData();

        buttonSetEnabled(setWordButton, false);

        if (ACHIEVEMENTS.isEmpty()) {
            ACHIEVEMENTS.add(new Achievement(this, "score.beginner", "Начинающий", "Заработать {x}", 100, SCORE_INCREASE_TRIGGER, HINTS_CURRENCY, 20));
            ACHIEVEMENTS.add(new Achievement(this, "score.advanced", "Продвинутый", "Заработать {x}", 500, SCORE_INCREASE_TRIGGER, HINTS_CURRENCY, 50));
            ACHIEVEMENTS.add(new Achievement(this, "score.pro", "Профессионал", "Заработать {x}", 2000, SCORE_INCREASE_TRIGGER, HINTS_CURRENCY, 100));
            ACHIEVEMENTS.add(new Achievement(this, "score.master", "Мастер", "Заработать {x}", 8000, SCORE_INCREASE_TRIGGER, HINTS_CURRENCY, 200));
            ACHIEVEMENTS.add(new Achievement(this, "score.expert", "Эксперт", "Заработать {x}", 15000, SCORE_INCREASE_TRIGGER, HINTS_CURRENCY, 400));
            ACHIEVEMENTS.add(new Achievement(this, "score.champion", "Чемпион", "Заработать {x}", 30000, SCORE_INCREASE_TRIGGER, HINTS_CURRENCY, 1000));
//            ACHIEVEMENTS.add(new Achievement(this, "score.crazy", "Ненормальный", "Заработать {x}", 100000, SCORE_INCREASE_TRIGGER, HINTS_CURRENCY, 300));

            ACHIEVEMENTS.add(new Achievement(this, "hints.curious", "Любопытный", "Использовать {x}", 50, HINTS_REDUCE_TRIGGER, SCORE_CURRENCY, 200));
            ACHIEVEMENTS.add(new Achievement(this, "hints.veryCurious", "Очень любопытный", "Использовать {x}", 250, HINTS_REDUCE_TRIGGER, SCORE_CURRENCY, 600));
            ACHIEVEMENTS.add(new Achievement(this, "hints.lazy", "Лентяй", "Использовать {x}", 500, HINTS_REDUCE_TRIGGER, SCORE_CURRENCY, 1000));
            ACHIEVEMENTS.add(new Achievement(this, "hints.DStudent", "Двоечник", "Использовать {x}", 1000, HINTS_REDUCE_TRIGGER, SCORE_CURRENCY, 2500));
        }



    }



    public void letterButtonOnClick(View view) {
        try {
            MainActivity.playSound(R.raw.click, this);
            Button pressedButton = (Button) view;
            Letter pressedLetter = Letter.getLetter(pressedButton.getText().charAt(0)).clone();
            if (pressedButton.getCurrentTextColor() == getColorFromTheme(R.attr.redButton))
                pressedLetter.setColor(Letter.COLOR_RED);
            else if (pressedButton.getCurrentTextColor() == getColorFromTheme(R.attr.blueButton))
                pressedLetter.setColor(Letter.COLOR_BLUE);
            else if (pressedButton.getCurrentTextColor() == getColorFromTheme(R.attr.yellowButton))
                pressedLetter.setColor(Letter.COLOR_YELLOW);

            buttonSetEnabled(pressedButton, false);

            dataManager.addLetterToWord(pressedLetter);
            buttonSetEnabled(eraseButton, true);
            buttonSetEnabled(mixButton, false);
            buttonSetEnabled(setWordButton, dataManager.getWord().exists(dbHelper));
        } catch (CloneNotSupportedException e) {}
    }

    public void eraseWord(View view) {
        if (view != null)
            MainActivity.playSound(R.raw.erase, this);
        dataManager.clearWord();
        for (Button letterButton : letterButtons) {
            buttonSetEnabled(letterButton, true);
        }
        buttonSetEnabled(eraseButton, false);
        buttonSetEnabled(mixButton, true);
        buttonSetEnabled(setWordButton, false);
    }

    public static Object getRandomElementFromArrayList(ArrayList arrayList) {
        return arrayList.get( (int) Math.floor(Math.random() * arrayList.size()) );
    }

    public void mixLetters(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Перемешивание букв")
                .setMessage("Вы точно хотите перемешать буквы за 25 очков?")
                .setPositiveButton("Перемешать", (dialog, which) -> {
                    try {
                        dataManager.removeScore(25);
                        MainActivity.playSound(R.raw.mix, this);
                        progressBar.setVisibility(View.VISIBLE);

                        Word randomWord;
                        do {
                            randomWord = new Word((String) getRandomElementFromArrayList(dataManager.allWords));
                        } while (randomWord.getText().length() > Math.floor(Math.random() * 2) + 3);

                        Log.i(LOG_TAG, "Random word is " + randomWord.getText());

                        ArrayList<Letter> playerLetters = new ArrayList<>(randomWord.getLetters());

                        for (int i = playerLetters.size(); i < 15; i++) {
                            playerLetters.add(Letter.getRandomLetter(playerLetters));
                        }

                        Collections.shuffle(playerLetters);

                        for (int i = 0;  i < 15; i++) {
                            letterButtons[i].setText(Character.toString(playerLetters.get(i).getSymbol()));
                        }

                        dataManager.saveData();
                        dataManager.setHintWord(null);
                        progressBar.setVisibility(View.INVISIBLE);
                    } catch (IllegalArgumentException e) {}
                }).setNeutralButton("Отмена", null)
                .show();
    }

    public void mixDisabledLetters() {
        dataManager.setHintWord(null);
        ArrayList<Letter> playerLetters = new ArrayList<>();
        int disabledLettersCount = 0;
        for (Button letterButton : letterButtons) {
            if (!letterButton.isEnabled()) {
                disabledLettersCount++;
            } else {
                playerLetters.add(Letter.getLetter(letterButton.getText().charAt(0)));
            }
        }

        if (dataManager.findWord(playerLetters, true) != null) {
            Log.i(LOG_TAG, "Word was found in enabled buttons");
            for (Button letterButton : letterButtons) {
                if (!letterButton.isEnabled()) {
                    Letter randomLetter = Letter.getRandomLetter(playerLetters);
                    letterButton.setText(randomLetter.toString());
                    letterButton.setTextColor(randomLetter.getButtonTextColor(this));
                    letterButton.setEnabled(true);

                }
            }
        } else {
            Log.i(LOG_TAG, "Word wasn`t found in enabled buttons");
            Word word = new Word("");
            Collections.shuffle(dataManager.allWords);
            for (String w : dataManager.allWords) {
                if (w.length() <= disabledLettersCount) {
                    word = new Word(w);
                }
            }
            Log.i(LOG_TAG, "Random word with length " + word.getText().length() + " is " + word.getText());
            ArrayList<Letter> wordLetters = word.getLetters();
            for (int i = wordLetters.size(); i < disabledLettersCount; i++) {
                wordLetters.add(Letter.getRandomLetter(playerLetters));
            }
            Collections.shuffle(wordLetters);

            for (int i = 0, j = 0; i < 15; i++) {
                if (!letterButtons[i].isEnabled()) {
                    letterButtons[i].setText(wordLetters.get(j).toString());
                    wordLetters.get(j).coloredLetterChance();
                    letterButtons[i].setTextColor(wordLetters.get(j).getButtonTextColor(this));
                    letterButtons[i].setEnabled(true);
                    j++;
                }
            }
        }
        dataManager.saveData();
    }

    private void buttonSetEnabled(Button btn, boolean isEnabled) {

        if (btn.getCurrentTextColor() == getColorFromTheme(R.attr.redButton) || btn.getCurrentTextColor() == getColorFromTheme(R.attr.redButtonDisabled))
            btn.setTextColor(isEnabled ? getColorFromTheme(R.attr.redButton) : getColorFromTheme(R.attr.redButtonDisabled));
        else if (btn.getCurrentTextColor() == getColorFromTheme(R.attr.blueButton) || btn.getCurrentTextColor() == getColorFromTheme(R.attr.blueButtonDisabled))
            btn.setTextColor(isEnabled ? getColorFromTheme(R.attr.blueButton) : getColorFromTheme(R.attr.blueButtonDisabled));
        else if (btn.getCurrentTextColor() == getColorFromTheme(R.attr.yellowButton) || btn.getCurrentTextColor() == getColorFromTheme(R.attr.yellowButtonDisabled))
            btn.setTextColor(isEnabled ? getColorFromTheme(R.attr.yellowButton) : getColorFromTheme(R.attr.yellowButtonDisabled));
        else
            btn.setTextColor(isEnabled ? getColorFromTheme(R.attr.buttonText) : getColorFromTheme(R.attr.disabled));

//        else {
//            double disabledColorCoefficient = 1.2;
//            if (btn.isEnabled() && !isEnabled) {
//                btn.setTextColor(Color.rgb((int) Math.round(Color.red(btn.getCurrentTextColor()) / disabledColorCoefficient),
//                                (int) Math.round(Color.green(btn.getCurrentTextColor()) / disabledColorCoefficient),
//                                (int) Math.round(Color.blue(btn.getCurrentTextColor()) / disabledColorCoefficient)));
//            } else if (!btn.isEnabled() && isEnabled) {
//                btn.setTextColor(Color.rgb((int) Math.round(Color.red(btn.getCurrentTextColor()) * disabledColorCoefficient),
//                        (int) Math.round(Color.green(btn.getCurrentTextColor()) * disabledColorCoefficient),
//                        (int) Math.round(Color.blue(btn.getCurrentTextColor()) * disabledColorCoefficient)));
//            }
//        }

        btn.setEnabled(isEnabled);

    }

    public static void buttonSetEnabled(Context context, Button btn, boolean isEnabled) {
        btn.setEnabled(isEnabled);
        btn.setTextColor(isEnabled ? getColorFromTheme(R.attr.buttonText, context) : getColorFromTheme(R.attr.disabled, context));
    }

    private @ColorInt int getColorFromTheme(int attr) {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    public static @ColorInt int getColorFromTheme(int attr, Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    public void setWord(View view) {
        MainActivity.playSound(R.raw.set_word, this);
        lastWordMade = dataManager.getWord().getText().toLowerCase();
        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(200);
        int wordPrice = dataManager.getWord().getWordPrice();
        dataManager.addScore(wordPrice);
        mixDisabledLetters();
        if (dataManager.getWord().isYellow())
            dataManager.addHints(dataManager.getWord().getText().length());
        eraseWord(null);
        buttonSetEnabled(setWordButton, false);
        dataManager.addWordToStatistics(lastWordMade);

        Toast wordPriceToast = new Toast(this);
        wordPriceToast.setGravity(Gravity.TOP, 0, 0);
        wordPriceToast.setDuration(Toast.LENGTH_SHORT);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        TextView textView = new TextView(this);
        textView.setText("+ " + wordPrice + " ");
        textView.setTextColor(getColorFromTheme(R.attr.wordText));
        layout.addView(textView);
        ImageView imageView = new ImageView(this);
        imageView.setAdjustViewBounds(true);
        imageView.setMaxWidth(43);
        imageView.setMaxHeight(43);
        imageView.setImageResource(R.drawable.score);
        layout.addView(imageView);
        wordPriceToast.setView(layout);
        wordPriceToast.show();

    }

    public void useHint(View view) {
        if (dataManager.getHints() > 0) {
            new Thread(() -> {
                if (dataManager.getHintWord() == null) {
                    dataManager.setHintWord(new Word(dataManager.findWord(dataManager.getPlayerLetters(), false)));
                    dataManager.resetHintIndex();
                }
                if (dataManager.getHintIndex() < dataManager.getHintWord().getText().length()) {
                    runOnUiThread(() -> {
                       dataManager.removeHints(1);
                    });
                    MainActivity.playSound(R.raw.hint, this);
                    Letter hintLetter = dataManager.getHintWord().getLetters().get(dataManager.getHintIndex());
                    for (int i = 0; i < 15; i++) {
                        Button letterButton = letterButtons[i];
                        if (letterButton.getText().charAt(0) == hintLetter.getSymbol() && !dataManager.lettersWithHintsIndexes.contains(i)) {
                            runOnUiThread(() -> letterButton.setBackgroundResource(R.drawable.letter_button_hint));
                            dataManager.incrementHintIndex();
                            dataManager.lettersWithHintsIndexes.add(i);
                            break;
                        }
                    }

                } else {
                    runOnUiThread(() -> {
//                        new AlertDialog.Builder(this)
//                                .setMessage("Слово уже составлено")
//                                .setIcon(android.R.drawable.ic_dialog_info)
//                                .setNeutralButton(android.R.string.ok, null).show();
                        Toast.makeText(this, "Слово уже составлено", Toast.LENGTH_SHORT).show();
                        MainActivity.playSound(R.raw.error, this);
                    });
                }
            }).start();
        } else {
            try {
                dataManager.removeHints(1);
            } catch (IllegalArgumentException e) {};
        }
    }

    public void showMenu(View view) {
        PopupMenu menu = new PopupMenu(this, view);
        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.themeChange:
                    startActivity(new Intent(this, ThemeChangeActivity.class));
                    Animatoo.animateSlideLeft(this);
                    return true;
//                case R.id.extraScore:
//                    dataManager.addScore(400);
//                    return true;
//                case R.id.extraHints:
//                    dataManager.addHints(100);
//                    return true;
                case R.id.achievementsButton:
                    startActivity(new Intent(this, AchievementsActivity.class));
                    Animatoo.animateSlideLeft(this);
                    return true;
                case R.id.buyHints:
                    TextView textView = new TextView(this);
                    textView.setText("Стоимость 1 подсказки – 5 очков");

                    EditText input = new EditText(this);
                    input.setInputType(InputType.TYPE_CLASS_NUMBER);
                    input.setHint("Введите количество подсказок");

                    LinearLayout layout = new LinearLayout(this);
                    layout.setOrientation(LinearLayout.VERTICAL);
                    layout.setGravity(Gravity.CENTER_HORIZONTAL);
                    layout.addView(textView);
                    layout.addView(input);

                    new AlertDialog.Builder(this)
                            .setTitle("Покупка подсказок")
                            .setIcon(R.drawable.hints)
                            .setView(layout)
                            .setPositiveButton(" Купить", (dialog, which) -> {
                                try {
                                int hintsCount = Integer.parseInt(input.getText().toString());
                                int hintsPrice = hintsCount * 5;



                                    new AlertDialog.Builder(this)
                                            .setTitle("Покупка подсказок")
                                            .setIcon(R.drawable.hints)
                                            .setMessage("Вы точно хотите купить подсказки (" + hintsCount + ") за " + hintsPrice + " очков?")
                                            .setPositiveButton("Да", (dialog1, which1) -> {
                                                try {
                                                    dataManager.removeScore(hintsPrice);
                                                    MainActivity.playSound(R.raw.buy, this);
                                                    dataManager.addHints(hintsCount);
                                                } catch (IllegalArgumentException e) {
                                                }
                                            })
                                            .setNegativeButton("Нет", null)
                                            .show();

                                } catch (Exception e) {
                                    Toast.makeText(this, "Число слишком большое", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Отмена ", null)
                            .show();
                    return true;
                case R.id.statistics:
                    startActivity(new Intent(this, StatisticsActivity.class));
                    Animatoo.animateSlideLeft(this);
                    return true;
                case R.id.addWord:
                    String wordToAdd = wordTextView.getText().toString().toLowerCase();
                    if (setWordButton.isEnabled()) {
                        Toast.makeText(MainActivity.this, "Это слово уже пристутствует в словаре", Toast.LENGTH_LONG).show();
                    } else if (!wordToAdd.isEmpty()) {
                        new AlertDialog.Builder(this)
                                .setTitle("Слово отсутствует в словаре")
                                .setMessage("Вы точно хотите предложить добавить слово \"" + wordToAdd + "\" в словарь?")
                                .setPositiveButton("Да", (dialog, which) -> {
                                    progressBar.setVisibility(View.VISIBLE);
                                    RequestQueue queue = Volley.newRequestQueue(this);
                                    StringRequest stringRequest = new StringRequest(Request.Method.GET, "http://ananiev-nestor.kl.com.ua/addWord.php?word=" + wordToAdd + "&uuid=" + uuid,
                                            response -> {
                                                progressBar.setVisibility(View.INVISIBLE);
                                                Toast.makeText(MainActivity.this, "Спасибо. Ваше предложение будет рассмотрено", Toast.LENGTH_LONG).show();
                                            },
                                            error -> {
                                                MainActivity.playSound(R.raw.error, this);
                                                progressBar.setVisibility(View.INVISIBLE);
                                                Toast.makeText(MainActivity.this, "Произошла ошибка. Проверьте подключение к интернету", Toast.LENGTH_LONG).show();
                                            }
                                    );
                                    queue.add(stringRequest);
                                }).setNegativeButton("Отмена", null)
                                .setIcon(R.drawable.icon)
                                .show();
                    } else {
                        Toast.makeText(MainActivity.this, "Для того, чтобы предложить слово, сначала составьте его из букв", Toast.LENGTH_LONG).show();
                    }
                    return true;
                default:
                    return false;
            }
        });
        menu.inflate(R.menu.menu);
        menu.show();
    }

    public void useDictionary(View view) {

        if (lastWordMade != null) {
            progressBar.setVisibility(View.VISIBLE);
            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, "http://ananiev-nestor.kl.com.ua/meaning.php?word=" + lastWordMade,
                    response -> {
                        progressBar.setVisibility(View.INVISIBLE);
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Словарик")
                                .setMessage(response)
                                .setNeutralButton("OK", null)
                                .setIcon(R.drawable.dictionary)
                                .show();
                    },
                    error -> {
                        MainActivity.playSound(R.raw.error, this);
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(MainActivity.this, "Произошла ошибка. Проверьте подключение к интернету", Toast.LENGTH_LONG).show();
                    }
            );
            queue.add(stringRequest);
        } else {
            MainActivity.playSound(R.raw.error, this);
            Toast.makeText(this, "Для использования словаря Вам нужно составить хотя бы одно слово", Toast.LENGTH_SHORT).show();
        }
    }

    public static void onActivityStop(Context context) {
       startedActivitiesCount--;
       if (startedActivitiesCount == 0) {
           Intent intent = new Intent(context, BackgroundMusicService.class);
           intent.putExtra("pause", true);
           context.startService(intent);
       }

    }

    public static void onActivityStart(Context context) {
        startedActivitiesCount++;

        Intent intent = new Intent(context, BackgroundMusicService.class);
        intent.putExtra("resume", true);
        context.startService(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        onActivityStart(this);
    }

    @Override
    protected void onStop() {
        onActivityStop(this);
        super.onStop();
    }

    public static void playSound(int resId, Context context) {
        MediaPlayer mediaPlayer = MediaPlayer.create(context, resId);
        mediaPlayer.setVolume(0.75f, 0.75f);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(mp -> {
            mediaPlayer.release();
        });

    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                        .setTitle("Подтверждение выхода")
                        .setMessage("Вы точно хотите выйти?")
                        .setPositiveButton("Выход ", (dialog, which) -> {
                            finishAffinity();
                        }).setNegativeButton(" Отмена", null)
                        .setIcon(R.drawable.icon)
                        .show();

    }
}