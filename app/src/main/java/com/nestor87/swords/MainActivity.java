package com.nestor87.swords;

import android.Manifest;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Vibrator;
import androidx.annotation.ColorInt;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;

import java.io.File;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.nestor87.swords.Achievement.ACHIEVEMENTS;
import static com.nestor87.swords.Achievement.HINTS_CURRENCY;
import static com.nestor87.swords.Achievement.HINTS_REDUCE_TRIGGER;
import static com.nestor87.swords.Achievement.SCORE_CURRENCY;
import static com.nestor87.swords.Achievement.SCORE_INCREASE_TRIGGER;
import static com.nestor87.swords.Achievement.WORD_COMPOSING_TRIGGER;

public class MainActivity extends AppCompatActivity {

    private TextView scoreTextView, hintsTextView, wordTextView;
    private DataManager dataManager;
    public static Button[] letterButtons;
    private Button eraseButton, mixButton, setWordButton;
    private DBHelper dbHelper;
    private ProgressBar progressBar;
    private ImageButton menuButton, hintsButton, dictionaryButton;

    private int permissionCheck;

    public static final String LOG_TAG = "SWORDS_DEBUG";
    public static final String APP_PREFERENCES_FILE_NAME = "preferences";
    private String lastWordMade = null;
    public static int startedActivitiesCount = 0;
    public static final String accountManagerPassword = "$2y$10$6UiaU230HP2IuSn.QUeyoulrm7YUBvSMv44QThkasbkQQagZvBj3K";
    public static String uuid;

    private ArrayList<Letter> lettersPressed = new ArrayList<>();
    private boolean isDialogShowing = false;
    private boolean isThemePreviewMode = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isThemePreviewMode = getIntent().getIntExtra("themePreviewId", -1) != -1;

        if (!isThemePreviewMode) {
            DataManager.applyTheme(this);
        } else {
            setTheme(DataManager.getThemeResIdByThemeId(getIntent().getIntExtra("themePreviewId", -1)));
        }
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN  | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        setContentView(R.layout.activity_main);

        requestPermission();


        SharedPreferences preferences = getSharedPreferences(APP_PREFERENCES_FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putString("accountId", preferences.getString("accountId", UUID.randomUUID().toString()));
        preferencesEditor.apply();

        uuid = preferences.getString("accountId", "");



        scoreTextView = findViewById(R.id.score);
        hintsTextView = findViewById(R.id.hints);
        wordTextView = findViewById(R.id.word);
        eraseButton = findViewById(R.id.erase);
        mixButton = findViewById(R.id.mix);
        setWordButton = findViewById(R.id.setWord);
        progressBar = findViewById(R.id.progressBar);
        menuButton = findViewById(R.id.menuButton);
        dictionaryButton = findViewById(R.id.dictionaryButton);
        hintsButton = findViewById(R.id.useHintButton);


        letterButtons = new Button[] {
                findViewById(R.id.letter1), findViewById(R.id.letter2), findViewById(R.id.letter3), findViewById(R.id.letter4),
                findViewById(R.id.letter5), findViewById(R.id.letter6), findViewById(R.id.letter7), findViewById(R.id.letter8), findViewById(R.id.letter9),
                findViewById(R.id.letter10), findViewById(R.id.letter11), findViewById(R.id.letter12), findViewById(R.id.letter13), findViewById(R.id.letter14),
                findViewById(R.id.letter15)
        };

        dbHelper = new DBHelper(this);
        dbHelper.copyDBIfNotExists();
        dataManager = new DataManager(scoreTextView, hintsTextView, wordTextView, letterButtons, dbHelper, this);

        buttonSetEnabled(eraseButton, false);
        dataManager.loadData();

        buttonSetEnabled(setWordButton, false);

        if (preferences.getString("name", "").equals(""))
            dataManager.getAllUsers().observe(this, this::showNameDialog);


        if (!preferences.getBoolean("isAccountRegistered", false) && !preferences.getString("name", "").equals(""))
            dataManager.registerAccount();

        if (ACHIEVEMENTS.isEmpty()) {
            ACHIEVEMENTS.add(new Achievement(this, "score.beginner", "Начинающий", "Заработать {x}", 100, SCORE_INCREASE_TRIGGER, HINTS_CURRENCY, 20));
            ACHIEVEMENTS.add(new Achievement(this, "score.advanced", "Продвинутый", "Заработать {x}", 500, SCORE_INCREASE_TRIGGER, HINTS_CURRENCY, 50));
            ACHIEVEMENTS.add(new Achievement(this, "score.pro", "Профессионал", "Заработать {x}", 2000, SCORE_INCREASE_TRIGGER, HINTS_CURRENCY, 100));
            ACHIEVEMENTS.add(new Achievement(this, "score.master", "Мастер", "Заработать {x}", 8000, SCORE_INCREASE_TRIGGER, HINTS_CURRENCY, 200));
            ACHIEVEMENTS.add(new Achievement(this, "score.expert", "Эксперт", "Заработать {x}", 15000, SCORE_INCREASE_TRIGGER, HINTS_CURRENCY, 400));
            ACHIEVEMENTS.add(new Achievement(this, "score.champion", "Чемпион", "Заработать {x}", 30000, SCORE_INCREASE_TRIGGER, HINTS_CURRENCY, 1000));
            ACHIEVEMENTS.add(new Achievement(this, "score.fan", "Фанат", "Заработать {x}", 50000, SCORE_INCREASE_TRIGGER, HINTS_CURRENCY, 1500));
            ACHIEVEMENTS.add(new Achievement(this, "score.crazy", "Чокнутый", "Заработать {x}", 100000, SCORE_INCREASE_TRIGGER, HINTS_CURRENCY, 2000));

            ACHIEVEMENTS.add(new Achievement(this, "hints.curious", "Любопытный", "Использовать {x}", 50, HINTS_REDUCE_TRIGGER, SCORE_CURRENCY, 200));
            ACHIEVEMENTS.add(new Achievement(this, "hints.veryCurious", "Очень любопытный", "Использовать {x}", 250, HINTS_REDUCE_TRIGGER, SCORE_CURRENCY, 600));
            ACHIEVEMENTS.add(new Achievement(this, "hints.lazy", "Лентяй", "Использовать {x}", 500, HINTS_REDUCE_TRIGGER, SCORE_CURRENCY, 1000));
            ACHIEVEMENTS.add(new Achievement(this, "hints.DStudent", "Двоечник", "Использовать {x}", 1000, HINTS_REDUCE_TRIGGER, SCORE_CURRENCY, 2500));
//            ACHIEVEMENTS.add(new Achievement(this, "hints.DStudent", "Двоечник", "Использовать {x}", 3500, HINTS_REDUCE_TRIGGER, SCORE_CURRENCY, 5000));

//            ACHIEVEMENTS.add(new Achievement(this, "words.1", "1", "Составить {x} {w}", 20, WORD_COMPOSING_TRIGGER, HINTS_CURRENCY, 10));
//            ACHIEVEMENTS.add(new Achievement(this, "words.2", "2", "Составить {x} {w}", 50, WORD_COMPOSING_TRIGGER, SCORE_CURRENCY, 75));
//            ACHIEVEMENTS.add(new Achievement(this, "words.3", "3", "Составить {x} {w}", 100, WORD_COMPOSING_TRIGGER, HINTS_CURRENCY, 30));
//            ACHIEVEMENTS.add(new Achievement(this, "words.4", "4", "Составить {x} {w}", 200, WORD_COMPOSING_TRIGGER, SCORE_CURRENCY, 200));
//            ACHIEVEMENTS.add(new Achievement(this, "words.5", "5", "Составить {x} {w}", 500, WORD_COMPOSING_TRIGGER, HINTS_CURRENCY, 85));
//            ACHIEVEMENTS.add(new Achievement(this, "words.6", "6", "Составить {x} {w}", 750, WORD_COMPOSING_TRIGGER, SCORE_CURRENCY, 500));
//            ACHIEVEMENTS.add(new Achievement(this, "words.7", "7", "Составить {x} {w}", 750, WORD_COMPOSING_TRIGGER, SCORE_CURRENCY, 500));
        }

        eraseButton.setOnLongClickListener(v -> {
            MainActivity.playSound(R.raw.erase, this);
            if (wordTextView.getText().length() == 1) {
                eraseWord(null);
            } else {
                Letter lastLetter = lettersPressed.remove(lettersPressed.size() - 1);
                for (Button letterButton : letterButtons) {
                    if (!letterButton.isEnabled() && letterButton.getText().equals(Character.toString(lastLetter.getSymbol()).toUpperCase())) {
                        Log.i(LOG_TAG, "btn color: " + letterButton.getCurrentTextColor() + "\nlet color: " + lastLetter.getColor() + "\n" + getColorFromTheme(R.attr.redButtonDisabled) + "\n" + getColorFromTheme(R.attr.buttonText));
                        if (
                                (letterButton.getCurrentTextColor() == getColorFromTheme(R.attr.redButtonDisabled) && lastLetter.getColor() == Letter.COLOR_RED)
                                || (letterButton.getCurrentTextColor() == getColorFromTheme(R.attr.blueButtonDisabled) && lastLetter.getColor() == Letter.COLOR_BLUE)
                                || (letterButton.getCurrentTextColor() == getColorFromTheme(R.attr.yellowButtonDisabled) && lastLetter.getColor() == Letter.COLOR_YELLOW)
                                || (letterButton.getCurrentTextColor() == getColorFromTheme(R.attr.disabled) && lastLetter.getColor() == Letter.COLOR_NONE)

                        ) {
                            buttonSetEnabled(letterButton, true);
                        }
                    }
                }
                dataManager.clearWord();
                for (Letter letter : lettersPressed) {
                    dataManager.addLetterToWord(letter);
                }
                buttonSetEnabled(setWordButton, dataManager.getWord().exists(dbHelper));

            }
            return true;
        });


        if (isThemePreviewMode) {
            menuButton.setImageResource(R.drawable.ic_left_arrow);
            menuButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, ThemeChangeActivity.class);
                intent.putExtra("selectThemeId", getIntent().getIntExtra("themePreviewId", -1));
                startActivity(intent);
                Animatoo.animateSlideRight(this);
            });
            for (Button button : letterButtons)  {
                button.setEnabled(false);
            }
            hintsButton.setEnabled(false);
            dictionaryButton.setEnabled(false);
            mixButton.setEnabled(false);
        }
    }

    private void requestPermission() {
        permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.icon)
                    .setTitle("Предоставление разрешения")
                    .setMessage("Для правильной работы игры требуется разрешение. Оно будет использоваться для обновления SWords. Примите его в следуюшем окне")
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, which) -> {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 164);
                    })
                    .show();

        } else {
            checkForNewVersion();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 164:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    checkForNewVersion();
                } else {
                    requestPermission();
                }
                break;

            default:
                break;
        }
    }

    private void checkForNewVersion() {
        NetworkService.getInstance().getSWordsApi().getLatestAppVersion(getBearerToken()).enqueue(
                new Callback<VersionInfo>() {
                    @Override
                    public void onResponse(Call<VersionInfo> call, Response<VersionInfo> response) {
                        if (response.body().getCode() > BuildConfig.VERSION_CODE) {
                            Dialog dialog = new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Доступна новая версия " + response.body().getName())
                                    .setMessage(response.body().getChanges())
                                    .setCancelable(false)
                                    .setPositiveButton("Cкачать", null)
                                    .create();
                            dialog.setOnShowListener(alertDialog -> {
                                Button button = ((AlertDialog) alertDialog).getButton(AlertDialog.BUTTON_POSITIVE);
                                button.setOnClickListener(v -> {
                                    ((Button) button).setText("Скачивание...");
                                    ((Button) button).setEnabled(false);
                                    DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                                    Uri uri = Uri.parse("https://github.com/Nestorian87/SWords/raw/master/app/release/app-release.apk");

                                    DownloadManager.Request request = new DownloadManager.Request(uri);
                                    request.setTitle("SWords v" + response.body().getName());
                                    request.setDescription("Новая версия");
                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
                                    request.setVisibleInDownloadsUi(true);
                                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"SWords-" + response.body().getName() + ".apk");
                                    request.setMimeType(MimeTypeMap.getSingleton().getMimeTypeFromExtension("apk"));
                                    downloadManager.enqueue(request);

                                    BroadcastReceiver onComplete = new BroadcastReceiver() {
                                        public void onReceive(Context context, Intent intent) {
                                            if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                                                ((Button) button).setText("Скачать");
                                                ((Button) button).setEnabled(true);
                                                if (isSamsung()) {
                                                    Intent intent1 = getPackageManager()
                                                            .getLaunchIntentForPackage("com.sec.android.app.myfiles");
                                                    intent1.setAction("samsung.myfiles.intent.action.LAUNCH_MY_FILES");
                                                    intent1.putExtra("samsung.myfiles.intent.extra.START_PATH",
                                                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
                                                    startActivity(intent1);
                                                } else {
                                                    startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
                                                }
                                                Toast.makeText(MainActivity.this, "Установите скачанный файл (" + "SWords-" + response.body().getName() + ".apk" + ")", Toast.LENGTH_LONG).show();

                                                unregisterReceiver(this);
                                            }
                                        }
                                    };
                                    registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

                                });
                            });

                            dialog.show();
                        }
                    }

                    @Override
                    public void onFailure(Call<VersionInfo> call, Throwable t) {

                    }
                }
        );
    }

    public static boolean isSamsung() {
        String manufacturer = Build.MANUFACTURER;
        if (manufacturer != null) return manufacturer.toLowerCase().equals("samsung");
        return false;
    }


    private void showNameDialog(List<Player> allPlayers) {
        if (!isDialogShowing) {
            isDialogShowing = true;
            SharedPreferences preferences = getSharedPreferences(APP_PREFERENCES_FILE_NAME, MODE_PRIVATE);
            SharedPreferences.Editor preferencesEditor = preferences.edit();

            LayoutInflater layoutInflater = LayoutInflater.from(this);
            View nameView = layoutInflater.inflate(R.layout.input_name_dialog, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(nameView);
            EditText nameEditText = nameView.findViewById(R.id.nameEditText);
            nameEditText.setText(preferences.getString("name", ""));
            builder.setCancelable(false).setNeutralButton("OK", (dialog, which) -> {
                if (nameEditText.getText().toString().isEmpty()) {
                    Toast.makeText(this, "Введите ник!", Toast.LENGTH_SHORT).show();
                    dialog.cancel();
                    isDialogShowing = false;
                    if (isConnectedToInternet())
                        dataManager.getAllUsers().observe(this, this::showNameDialog);
                    else
                        Toast.makeText(MainActivity.this, "Произошла ошибка. Проверьте подключение к интернету", Toast.LENGTH_LONG).show();
                } else {
                    boolean isNameDuplicated = false;
                    for (Player player : allPlayers) {
                        if (player.getName().equals(nameEditText.getText().toString())) {
                            isNameDuplicated = true;
                        }
                    }

                    if (!isNameDuplicated) {
                        if (nameEditText.getText().toString().length() <= 20) {
                            preferencesEditor.putString("name", nameEditText.getText().toString());
                            preferencesEditor.apply();
                            dataManager.setName(nameEditText.getText().toString());
                            if (preferences.getBoolean("isAccountRegistered", false)) {
                                dataManager.updateAccount();
                            } else {
                                dataManager.registerAccount();

                                switch (nameEditText.getText().toString()) {
                                    case "Lydmila":
                                        dataManager.setScore(20064);
                                        dataManager.setHints(377);
                                        break;
                                    case "Eliz71":
                                        dataManager.setScore(2860);
                                        dataManager.setHints(57);
                                break;
                                }
                            }
                            isDialogShowing = false;
                        } else {
                            Toast.makeText(this, "Максимальная длина ника - 20 символов", Toast.LENGTH_LONG).show();
                            dialog.cancel();
                            isDialogShowing = false;
                            if (isConnectedToInternet())
                                dataManager.getAllUsers().observe(this, this::showNameDialog);
                            else
                                Toast.makeText(MainActivity.this, "Произошла ошибка. Проверьте подключение к интернету", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this, "Такой ник уже используется. Выберите другой", Toast.LENGTH_LONG).show();
                        dialog.cancel();
                        isDialogShowing = false;
                        if (isConnectedToInternet())
                            dataManager.getAllUsers().observe(this, this::showNameDialog);
                        else
                            Toast.makeText(MainActivity.this, "Произошла ошибка. Проверьте подключение к интернету", Toast.LENGTH_LONG).show();
                    }
                }
            });
            builder.show();
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
            lettersPressed.add(pressedLetter);
            buttonSetEnabled(eraseButton, true);
            buttonSetEnabled(mixButton, false);
            buttonSetEnabled(setWordButton, dataManager.getWord().exists(dbHelper));
        } catch (CloneNotSupportedException e) {}
    }

    public void eraseWord(View view) {
        if (view != null)
            MainActivity.playSound(R.raw.erase, this);
        dataManager.clearWord();
        lettersPressed.clear();
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

    public static @ColorInt int getColorFromTheme(int attr, Resources.Theme theme, Context context) {
        TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(attr, typedValue, true);
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

        Achievement.addProgress(Achievement.WORD_COMPOSING_TRIGGER, 1, this);

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
        if (!isThemePreviewMode) {
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
                                        HashMap<String, String> body = new HashMap<>();
                                        body.put("word", wordToAdd);
                                        body.put("uuid", uuid);
                                        NetworkService.getInstance().getSWordsApi().addWordRequest(getBearerToken(), body).enqueue(
                                                new Callback<Void>() {
                                                    @Override
                                                    public void onResponse(Call<Void> call, Response<Void> response) {
                                                        progressBar.setVisibility(View.INVISIBLE);
                                                        Toast.makeText(MainActivity.this, "Спасибо. Ваше предложение будет рассмотрено", Toast.LENGTH_LONG).show();
                                                    }

                                                    @Override
                                                    public void onFailure(Call<Void> call, Throwable t) {
                                                        progressBar.setVisibility(View.INVISIBLE);
                                                        MainActivity.playSound(R.raw.error, MainActivity.this);
                                                        Toast.makeText(MainActivity.this, "Произошла ошибка. Проверьте подключение к интернету", Toast.LENGTH_LONG).show();

                                                    }
                                                }
                                        );

                                    }).setNegativeButton("Отмена", null)
                                    .setIcon(R.drawable.icon)
                                    .show();
                        } else {
                            Toast.makeText(MainActivity.this, "Для того, чтобы предложить слово, сначала составьте его из букв", Toast.LENGTH_LONG).show();
                        }
                        return true;
                    case R.id.changeName:
                        if (isConnectedToInternet()) {
                            dataManager.getAllUsers().observe(this, this::showNameDialog);
                        } else {
                            Toast.makeText(MainActivity.this, "Произошла ошибка. Проверьте подключение к интернету", Toast.LENGTH_LONG).show();
                        }

                        return true;
                    case R.id.bestPlayers:
                        startActivity(new Intent(this, BestPlayersActivity.class));
                        Animatoo.animateSlideLeft(this);
                        return true;
                    default:
                        return false;
                }
            });
            menu.inflate(R.menu.menu);
            menu.show();
        }
    }

    public void useDictionary(View view) {

        if (lastWordMade != null) {
            progressBar.setVisibility(View.VISIBLE);
            NetworkService.getInstance().getSWordsApi().getWordMeaning(getBearerToken(), lastWordMade).enqueue(
                    new Callback<Word>() {
                        @Override
                        public void onResponse(Call<Word> call, Response<Word> response) {
                            progressBar.setVisibility(View.INVISIBLE);
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Словарик")
                                    .setMessage(response.body().getMeaning())
                                    .setNeutralButton("OK", null)
                                    .setIcon(R.drawable.dictionary)
                                    .show();
                        }

                        @Override
                        public void onFailure(Call<Word> call, Throwable t) {
                            MainActivity.playSound(R.raw.error, MainActivity.this);
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(MainActivity.this, "Произошла ошибка. Проверьте подключение к интернету", Toast.LENGTH_LONG).show();
                        }
                    }
            );
        } else {
            MainActivity.playSound(R.raw.error, this);
            Toast.makeText(this, "Для использования словаря Вам нужно составить хотя бы одно слово", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isConnectedToInternet() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                return true;
            }
        }

        return false;
    }

    public static String getBearerToken() {
        return "Bearer " + uuid;
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
        if (!isThemePreviewMode) {
            new AlertDialog.Builder(this)
                    .setTitle("Подтверждение выхода")
                    .setMessage("Вы точно хотите выйти?")
                    .setPositiveButton("Выход ", (dialog, which) -> {
                        finishAffinity();
                    }).setNegativeButton(" Отмена", null)
                    .setIcon(R.drawable.icon)
                    .show();
        } else {
            Intent intent = new Intent(this, ThemeChangeActivity.class);
            intent.putExtra("selectThemeId", getIntent().getIntExtra("themePreviewId", -1));
            startActivity(intent);
            Animatoo.animateSlideRight(this);
        }
    }
}