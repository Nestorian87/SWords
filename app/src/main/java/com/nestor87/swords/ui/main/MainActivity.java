package com.nestor87.swords.ui.main;

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
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.jetradarmobile.snowfall.SnowfallView;
import com.nestor87.swords.BuildConfig;
import com.nestor87.swords.R;
import com.nestor87.swords.data.helpers.DBHelper;
import com.nestor87.swords.ui.dailyReward.DailyRewardDialog;
import com.nestor87.swords.data.managers.DataManager;
import com.nestor87.swords.data.markdownviewCssStyle.VersionCssStyle;
import com.nestor87.swords.data.models.Achievement;
import com.nestor87.swords.data.models.Bonus;
import com.nestor87.swords.data.models.Currencies;
import com.nestor87.swords.data.models.Letter;
import com.nestor87.swords.data.models.MessagesCountResponse;
import com.nestor87.swords.data.models.UsernameAvailabilityResponse;
import com.nestor87.swords.data.models.VersionResponse;
import com.nestor87.swords.data.models.Word;
import com.nestor87.swords.data.network.NetworkService;
import com.nestor87.swords.data.services.NotificationService;
import com.nestor87.swords.ui.BaseActivity;
import com.nestor87.swords.ui.achievements.AchievementsActivity;
import com.nestor87.swords.ui.bestPlayers.BestPlayersActivity;
import com.nestor87.swords.ui.bonuses.BonusesActivity;
import com.nestor87.swords.ui.messages.MessagesActivity;
import com.nestor87.swords.ui.statistics.StatisticsActivity;
import com.nestor87.swords.ui.themeChange.ThemeChangeActivity;
import com.nestor87.swords.utils.SystemUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import br.tiagohm.markdownview.MarkdownView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.nestor87.swords.data.models.Achievement.ACHIEVEMENTS;
import static com.nestor87.swords.utils.SystemUtils.getColorFromTheme;

public class MainActivity extends BaseActivity {

    private TextView scoreTextView, hintsTextView, wordTextView;
    private DataManager dataManager;
    public static Button[] letterButtons;
    private Button eraseButton, mixButton, setWordButton;
    private DBHelper dbHelper;
    private ProgressBar progressBar;
    private ImageButton menuButton, hintsButton, dictionaryButton;
    private SnowfallView snowfallView;

    private int permissionCheck;

    public static final String LOG_TAG = "SWORDS_DEBUG";
    public static final String APP_PREFERENCES_FILE_NAME = "preferences";
    private String lastWordMade = null;
    public static final String accountManagerPassword = "$2y$10$6UiaU230HP2IuSn.QUeyoulrm7YUBvSMv44QThkasbkQQagZvBj3K";
    public static String uuid;

    private ArrayList<Letter> lettersPressed = new ArrayList<>();
    private boolean isDialogShowing = false;
    private boolean isThemePreviewMode = false;

    public static boolean isNewVersionRequested = false;

    private BroadcastReceiver notificationBroadcastReceiver;
    private boolean isNotificationReceiverRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isThemePreviewMode = getIntent().getIntExtra("themePreviewId", -1) != -1;

        if (isThemePreviewMode) {
            setTheme(SystemUtils.getThemeResIdByThemeId(getIntent().getIntExtra("themePreviewId", -1)));
            setNeedToChangeTheme(false);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = getSharedPreferences(APP_PREFERENCES_FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putString("accountId", preferences.getString("accountId", UUID.randomUUID().toString()));
        preferencesEditor.apply();

        uuid = preferences.getString("accountId", "");

        requestPermission();

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

        int currentMonth = Integer.parseInt((new SimpleDateFormat("M")).format(new Date()));
        if (currentMonth < 3 || currentMonth == 12) {
            findViewById(R.id.snowfallView).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.snowfallView).setVisibility(View.GONE);
        }

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

        if (preferences.getString("name", "").equals("")) {
            showNameDialog();
        }


        if (!preferences.getBoolean("isAccountRegistered", false) && !preferences.getString("name", "").equals("")) {
            dataManager.registerAccount();
        }

        if (ACHIEVEMENTS.isEmpty()) {
            Achievement.initAchievements(this);
        }

        if (Bonus.Companion.getBONUSES().isEmpty()) {
            Bonus.Companion.initBonuses();
        }


        eraseButton.setOnLongClickListener(v -> {
            MainActivity.playSound(R.raw.erase, this);
            if (wordTextView.getText().length() == 1) {
                eraseWord(null);
            } else {
                Letter lastLetter = lettersPressed.remove(lettersPressed.size() - 1);
                for (Button letterButton : letterButtons) {
                    if (!letterButton.isEnabled() && letterButton.getText().equals(Character.toString(lastLetter.getSymbol()).toUpperCase())) {
                        Log.i(LOG_TAG, "btn color: " + letterButton.getCurrentTextColor() + "\nlet color: " + lastLetter.getColor() + "\n" + getColorFromTheme(R.attr.redButtonDisabled, MainActivity.this) + "\n" + getColorFromTheme(R.attr.buttonText, MainActivity.this));
                        if (
                                (letterButton.getCurrentTextColor() == getColorFromTheme(R.attr.redButtonDisabled, MainActivity.this) && lastLetter.getColor() == Letter.COLOR_RED)
                                || (letterButton.getCurrentTextColor() == getColorFromTheme(R.attr.blueButtonDisabled, MainActivity.this) && lastLetter.getColor() == Letter.COLOR_BLUE)
                                || (letterButton.getCurrentTextColor() == getColorFromTheme(R.attr.yellowButtonDisabled, MainActivity.this) && lastLetter.getColor() == Letter.COLOR_YELLOW)
                                || (letterButton.getCurrentTextColor() == getColorFromTheme(R.attr.disabled, MainActivity.this) && lastLetter.getColor() == Letter.COLOR_NONE)

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

        checkUnviewedMessages(true);

        notificationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                checkUnviewedMessages(false);
            }
        };
        if (!isNotificationReceiverRegistered) {
            isNotificationReceiverRegistered = true;
            registerReceiver(notificationBroadcastReceiver, new IntentFilter(NotificationService.NOTIFICATION_RECEIVED_ACTION));
        }

        checkBonuses();
    }

    private void requestPermission() {
        permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.icon)
                    .setTitle("???????????????????????????? ????????????????????")
                    .setMessage("?????? ???????????????????? ???????????? ???????? ?????????????????? ????????????????????. ?????? ?????????? ???????????????????????????? ?????? ???????????????????? SWords. ?????????????? ?????? ?? ?????????????????? ????????")
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, which) -> {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 164);
                    })
                    .show();

        } else {
            showDailyRewardDialog();
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
        if (!isNewVersionRequested) {
            isNewVersionRequested = true;

            NetworkService.getInstance().getSWordsApi().getLatestAppVersion(getBearerToken()).enqueue(
                    new Callback<VersionResponse>() {
                        @Override
                        public void onResponse(Call<VersionResponse> call, Response<VersionResponse> response) {
                            if (response.body().getCode() > BuildConfig.VERSION_CODE) {
                                MarkdownView markdownView = new MarkdownView(MainActivity.this);
                                markdownView.addStyleSheet(new VersionCssStyle(MainActivity.this));
                                markdownView.loadMarkdown(response.body().getChanges());
                                markdownView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                                ScrollView scrollView = new ScrollView(MainActivity.this);
                                scrollView.addView(markdownView);

                                scrollView.setLayoutParams(
                                        new ViewGroup.LayoutParams(SystemUtils.dpToPx(350), SystemUtils.dpToPx(30))
                                );

                                TextView titleTextView = new TextView(MainActivity.this);
                                titleTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                titleTextView.setTextSize(22);
                                titleTextView.setPadding(0, 10, 0, 20);
                                titleTextView.setTextColor(getColorFromTheme(R.attr.scoreAndHintsText, MainActivity.this));
                                titleTextView.setTypeface(Typeface.DEFAULT_BOLD);
                                titleTextView.setText("???????????????? ?????????? ???????????? " + response.body().getName());


                                Dialog dialog = new AlertDialog.Builder(MainActivity.this)
                                        .setCustomTitle(titleTextView)
                                        .setView(scrollView)
                                        .setCancelable(false)
                                        .setPositiveButton("C????????????", null)
                                        .create();
                                dialog.setOnShowListener(alertDialog -> {
                                    Button button = ((AlertDialog) alertDialog).getButton(AlertDialog.BUTTON_POSITIVE);
                                    button.setOnClickListener(v -> {
                                        ((Button) button).setText("????????????????????...");
                                        ((Button) button).setEnabled(false);
                                        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                                        Uri uri = Uri.parse("https://github.com/Nestorian87/SWords/raw/master/app/release/app-release.apk");

                                        DownloadManager.Request request = new DownloadManager.Request(uri);
                                        request.setTitle("SWords v" + response.body().getName());
                                        request.setDescription("?????????? ????????????");
                                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
                                        request.setVisibleInDownloadsUi(true);
                                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "SWords-" + response.body().getName() + ".apk");
                                        request.setMimeType(MimeTypeMap.getSingleton().getMimeTypeFromExtension("apk"));
                                        downloadManager.enqueue(request);

                                        BroadcastReceiver onComplete = new BroadcastReceiver() {
                                            public void onReceive(Context context, Intent intent) {
                                                if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                                                    ((Button) button).setText("??????????????");
                                                    ((Button) button).setEnabled(true);
                                                    if (SystemUtils.isSamsung()) {
                                                        Intent intent1 = getPackageManager()
                                                                .getLaunchIntentForPackage("com.sec.android.app.myfiles");
                                                        intent1.setAction("samsung.myfiles.intent.action.LAUNCH_MY_FILES");
                                                        intent1.putExtra("samsung.myfiles.intent.extra.START_PATH",
                                                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
                                                        startActivity(intent1);
                                                    } else {
                                                        startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
                                                    }
                                                    Toast.makeText(MainActivity.this, "???????????????????? ?????????????????? ???????? (" + "SWords-" + response.body().getName() + ".apk" + ")", Toast.LENGTH_LONG).show();

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
                        public void onFailure(Call<VersionResponse> call, Throwable t) {

                        }
                    }
            );
        }
    }

    private void showNameDialog() {
        if (isConnectedToInternet()) {
            if (!isDialogShowing) {
                isDialogShowing = true;
                SharedPreferences preferences = getSharedPreferences(APP_PREFERENCES_FILE_NAME, MODE_PRIVATE);
                SharedPreferences.Editor preferencesEditor = preferences.edit();

                LayoutInflater layoutInflater = LayoutInflater.from(this);
                View nameView = layoutInflater.inflate(R.layout.input_name_dialog, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(nameView);

                if (preferences.getBoolean("isAccountRegistered", false)) {
                    ((TextView) nameView.findViewById(R.id.titleTextView)).setText(R.string.enter_nickname_without_greeting);
                }

                EditText nameEditText = nameView.findViewById(R.id.nameEditText);
                nameEditText.setText(preferences.getString("name", ""));
                builder.setCancelable(false).setNeutralButton("OK", (dialog, which) -> {
                    String nickname = nameEditText.getText().toString().trim();
                    if (nickname.isEmpty()) {
                        Toast.makeText(this, "?????????????? ??????!", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                        isDialogShowing = false;
                        showNameDialog();
                    } else if (nickname.contains(" ")) {
                        Toast.makeText(this, "?????? ???? ???????????? ?????????????????? ????????????????", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                        isDialogShowing = false;
                        showNameDialog();
                    } else {
                        NetworkService.getInstance().getSWordsApi().checkUsernameAvailability("Bearer " + accountManagerPassword, nameEditText.getText().toString()).enqueue(
                                new Callback<UsernameAvailabilityResponse>() {
                                    @Override
                                    public void onResponse(Call<UsernameAvailabilityResponse> call, Response<UsernameAvailabilityResponse> response) {
                                        if (response.body().isAvailable()) {
                                            if (nickname.length() <= 20) {
                                                preferencesEditor.putString("name", nickname);
                                                preferencesEditor.apply();
                                                dataManager.setName(nameEditText.getText().toString());
                                                if (preferences.getBoolean("isAccountRegistered", false)) {
                                                    dataManager.updateAccount();
                                                } else {
                                                    dataManager.registerAccount();

                                                    switch (nameEditText.getText().toString()) {
//                                                        case "Lydmila":
//                                                            dataManager.setScore(20064);
//                                                            dataManager.setHints(377);
//                                                            break;
//                                                        case "Eliz71":
//                                                            dataManager.setScore(2860);
//                                                            dataManager.setHints(57);
//                                                            break;
                                                    }

                                                    dataManager.updateAccount();
                                                }
                                                isDialogShowing = false;
                                            } else {
                                                Toast.makeText(MainActivity.this, "???????????????????????? ?????????? ???????? - 20 ????????????????", Toast.LENGTH_LONG).show();
                                                dialog.cancel();
                                                isDialogShowing = false;
                                                showNameDialog();
                                            }
                                        } else {
                                            Toast.makeText(MainActivity.this, "?????????? ?????? ?????? ????????????????????????. ???????????????? ????????????", Toast.LENGTH_LONG).show();
                                            dialog.cancel();
                                            isDialogShowing = false;
                                            showNameDialog();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<UsernameAvailabilityResponse> call, Throwable t) {
                                        Log.i(LOG_TAG, "failure");
                                        Toast.makeText(MainActivity.this, "?????????????????? ????????????. ?????????????????? ?????????????????????? ?? ??????????????????", Toast.LENGTH_LONG).show();
                                        showNameDialog();
                                    }
                                }
                        );
                    }
                });
                if (preferences.getBoolean("isAccountRegistered", false)) {
                    builder.setNegativeButton("????????????", (dialog, which) -> dialog.cancel());
                }
                builder.show();
            }
        } else {
            Toast.makeText(MainActivity.this, "?????????????????? ????????????. ?????????????????? ?????????????????????? ?? ??????????????????", Toast.LENGTH_LONG).show();
        }
    }

    public void letterButtonOnClick(View view) {
        try {
            MainActivity.playSound(R.raw.click, this);
            Button pressedButton = (Button) view;
            Letter pressedLetter = Letter.getLetter(pressedButton.getText().charAt(0)).clone();
            if (pressedButton.getCurrentTextColor() == getColorFromTheme(R.attr.redButton, MainActivity.this))
                pressedLetter.setColor(Letter.COLOR_RED);
            else if (pressedButton.getCurrentTextColor() == getColorFromTheme(R.attr.blueButton, MainActivity.this))
                pressedLetter.setColor(Letter.COLOR_BLUE);
            else if (pressedButton.getCurrentTextColor() == getColorFromTheme(R.attr.yellowButton, MainActivity.this))
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
                .setTitle("?????????????????????????? ????????")
                .setMessage("???? ?????????? ???????????? ???????????????????? ?????????? ???? 25 ???????????")
                .setPositiveButton("????????????????????", (dialog, which) -> {
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
                }).setNeutralButton("????????????", null)
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

        if (btn.getCurrentTextColor() == getColorFromTheme(R.attr.redButton, MainActivity.this) || btn.getCurrentTextColor() == getColorFromTheme(R.attr.redButtonDisabled, MainActivity.this))
            btn.setTextColor(isEnabled ? getColorFromTheme(R.attr.redButton, MainActivity.this) : getColorFromTheme(R.attr.redButtonDisabled, MainActivity.this));
        else if (btn.getCurrentTextColor() == getColorFromTheme(R.attr.blueButton, MainActivity.this) || btn.getCurrentTextColor() == getColorFromTheme(R.attr.blueButtonDisabled, MainActivity.this))
            btn.setTextColor(isEnabled ? getColorFromTheme(R.attr.blueButton, MainActivity.this) : getColorFromTheme(R.attr.blueButtonDisabled, MainActivity.this));
        else if (btn.getCurrentTextColor() == getColorFromTheme(R.attr.yellowButton, MainActivity.this) || btn.getCurrentTextColor() == getColorFromTheme(R.attr.yellowButtonDisabled, MainActivity.this))
            btn.setTextColor(isEnabled ? getColorFromTheme(R.attr.yellowButton, MainActivity.this) : getColorFromTheme(R.attr.yellowButtonDisabled, MainActivity.this));
        else
            btn.setTextColor(isEnabled ? getColorFromTheme(R.attr.buttonText, MainActivity.this) : getColorFromTheme(R.attr.disabled, MainActivity.this));

        btn.setEnabled(isEnabled);

    }

    public static void buttonSetEnabled(Context context, Button btn, boolean isEnabled) {
        btn.setEnabled(isEnabled);
        btn.setTextColor(isEnabled ? getColorFromTheme(R.attr.buttonText, context) : getColorFromTheme(R.attr.disabled, context));
    }

    public void setWord(View view) {
        MainActivity.playSound(R.raw.set_word, this);
        lastWordMade = dataManager.getWord().getText().toLowerCase();
        dataManager.addWordToStatistics(lastWordMade);
        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(200);
        int wordPrice = dataManager.getWord().getWordPrice();
        dataManager.addScore(wordPrice);
        mixDisabledLetters();
        if (dataManager.getWord().isYellow())
            dataManager.addHints(dataManager.getWord().getText().length());
        eraseWord(null);
        buttonSetEnabled(setWordButton, false);
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
                        try {
                            dataManager.removeHints(1);
                        } catch (IllegalArgumentException e) { }
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
//                                .setMessage("?????????? ?????? ????????????????????")
//                                .setIcon(android.R.drawable.ic_dialog_info)
//                                .setNeutralButton(android.R.string.ok, null).show();
                        Toast.makeText(this, "?????????? ?????? ????????????????????", Toast.LENGTH_SHORT).show();
                        MainActivity.playSound(R.raw.error, this);
                    });
                }
            }).start();
        } else {
            try {
                dataManager.removeHints(1);
            } catch (IllegalArgumentException e) { }
        }
    }

    public void showMenu(View view) {
        if (!isThemePreviewMode) {
            PopupMenu menu = new PopupMenu(this, view);
            menu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.bonuses:
                        startActivity(new Intent(this, BonusesActivity.class));
                        Animatoo.animateSlideLeft(this);
                        return true;
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
                        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        textView.setText("?????????????????? 1 ?????????????????? ??? 5 ??????????");

                        EditText input = new EditText(this);
                        input.setInputType(InputType.TYPE_CLASS_NUMBER);
                        input.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        input.setHint("?????????????? ???????????????????? ??????????????????");

                        TextView customTitleTextView = new TextView(this);
                        customTitleTextView.setText("?????????????? ??????????????????");
                        customTitleTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        customTitleTextView.setTextSize(22);
                        customTitleTextView.setPadding(0, 0, 0, 10);
                        customTitleTextView.setTypeface(Typeface.DEFAULT_BOLD);
                        customTitleTextView.setTextColor(getColorFromTheme(R.attr.scoreAndHintsText, MainActivity.this));

                        LinearLayout layout = new LinearLayout(this);
                        layout.setOrientation(LinearLayout.VERTICAL);
                        layout.setGravity(Gravity.CENTER_HORIZONTAL);
                        layout.addView(textView);
                        layout.addView(input);

                        new AlertDialog.Builder(this)
                                .setCustomTitle(customTitleTextView)
                                .setIcon(R.drawable.hints)
                                .setView(layout)
                                .setPositiveButton(" ????????????", (dialog, which) -> {
                                    try {
                                        int hintsCount = Integer.parseInt(input.getText().toString());
                                        int hintsPrice = hintsCount * 5;


                                        new AlertDialog.Builder(this)
                                                .setTitle("?????????????? ??????????????????")
                                                .setIcon(R.drawable.hints)
                                                .setMessage("???? ?????????? ???????????? ???????????? ?????????????????? (" + hintsCount + ") ???? " + hintsPrice + " ???????????")
                                                .setPositiveButton("????", (dialog1, which1) -> {
                                                    try {
                                                        dataManager.removeScore(hintsPrice);
                                                        MainActivity.playSound(R.raw.buy, this);
                                                        dataManager.addHints(hintsCount);
                                                    } catch (IllegalArgumentException e) {
                                                    }
                                                })
                                                .setNegativeButton("??????", null)
                                                .show();

                                    } catch (Exception e) {
                                        Toast.makeText(this, "?????????? ?????????????? ??????????????", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("???????????? ", null)
                                .show();
                        return true;
                    case R.id.statistics:
                        startActivity(new Intent(this, StatisticsActivity.class));
                        Animatoo.animateSlideLeft(this);
                        return true;
                    case R.id.addWord:
                        String wordToAdd = wordTextView.getText().toString().toLowerCase();
                        if (setWordButton.isEnabled()) {
                            Toast.makeText(MainActivity.this, "?????? ?????????? ?????? ?????????????????????????? ?? ??????????????", Toast.LENGTH_LONG).show();
                        } else if (!wordToAdd.isEmpty()) {
                            new AlertDialog.Builder(this)
                                    .setTitle("?????????? ?????????????????????? ?? ??????????????")
                                    .setMessage("???? ?????????? ???????????? ???????????????????? ???????????????? ?????????? \"" + wordToAdd + "\" ?? ???????????????")
                                    .setPositiveButton("????", (dialog, which) -> {
                                        progressBar.setVisibility(View.VISIBLE);
                                        HashMap<String, String> body = new HashMap<>();
                                        body.put("word", wordToAdd);
                                        body.put("uuid", uuid);
                                        NetworkService.getInstance().getSWordsApi().addWordRequest(getBearerToken(), body).enqueue(
                                                new Callback<Void>() {
                                                    @Override
                                                    public void onResponse(Call<Void> call, Response<Void> response) {
                                                        progressBar.setVisibility(View.INVISIBLE);
                                                        Toast.makeText(MainActivity.this, "??????????????. ???????? ?????????????????????? ?????????? ??????????????????????", Toast.LENGTH_LONG).show();
                                                    }

                                                    @Override
                                                    public void onFailure(Call<Void> call, Throwable t) {
                                                        progressBar.setVisibility(View.INVISIBLE);
                                                        MainActivity.playSound(R.raw.error, MainActivity.this);
                                                        Toast.makeText(MainActivity.this, "?????????????????? ????????????. ?????????????????? ?????????????????????? ?? ??????????????????", Toast.LENGTH_LONG).show();

                                                    }
                                                }
                                        );

                                    }).setNegativeButton("????????????", null)
                                    .setIcon(R.drawable.icon)
                                    .show();
                        } else {
                            Toast.makeText(MainActivity.this, "?????? ????????, ?????????? ???????????????????? ??????????, ?????????????? ?????????????????? ?????? ???? ????????", Toast.LENGTH_LONG).show();
                        }
                        return true;
                    case R.id.changeName:
                        showNameDialog();

                        return true;
                    case R.id.bestPlayers:
                        startActivity(new Intent(this, BestPlayersActivity.class));
                        Animatoo.animateSlideLeft(this);
                        return true;
                    case R.id.exit:
                        finishAffinity();
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
                                    .setTitle("????????????????")
                                    .setMessage(response.body().getMeaning())
                                    .setNeutralButton("OK", null)
                                    .setIcon(R.drawable.dictionary)
                                    .show();
                        }

                        @Override
                        public void onFailure(Call<Word> call, Throwable t) {
                            MainActivity.playSound(R.raw.error, MainActivity.this);
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(MainActivity.this, "?????????????????? ????????????. ?????????????????? ?????????????????????? ?? ??????????????????", Toast.LENGTH_LONG).show();
                        }
                    }
            );
        } else {
            MainActivity.playSound(R.raw.error, this);
            Toast.makeText(this, "?????? ?????????????????????????? ?????????????? ?????? ?????????? ?????????????????? ???????? ???? ???????? ??????????", Toast.LENGTH_SHORT).show();
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

    private void checkUnviewedMessages(boolean isFirstTime) {
        NetworkService.getInstance().getSWordsApi().getUnviewedMessagesCount(getBearerToken(), uuid).enqueue(
                new Callback<MessagesCountResponse>() {
                    @Override
                    public void onResponse(Call<MessagesCountResponse> call, Response<MessagesCountResponse> response) {
                        findViewById(R.id.newMessages).setVisibility(response.body().getCount() > 0 ? View.VISIBLE : View.GONE);
                        findViewById(R.id.newMessages).animate().alpha(response.body().getCount() > 0 ? 1f : 0f).setDuration(isFirstTime ? 0 : 500);

                    }

                    @Override
                    public void onFailure(Call<MessagesCountResponse> call, Throwable t) {

                    }
                }
        );
    }

    public void showMessages(View view) {
        startActivity(new Intent(this, MessagesActivity.class));
        Animatoo.animateSlideLeft(this);
    }

    public static void playSound(int resId, Context context) {
        MediaPlayer mediaPlayer = MediaPlayer.create(context, resId);
        mediaPlayer.setVolume(0.75f, 0.75f);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(mp -> {
            mediaPlayer.release();
        });

    }

    private void showDailyRewardDialog() {
        DailyRewardDialog dailyRewardDialog = new DailyRewardDialog(this);
        dailyRewardDialog.setOnRewardGottenListener(reward -> {
            if (reward.getCurrencyEnum() == Currencies.SCORE) {
                dataManager.addScore(reward.getCount());
            } else if (reward.getCurrencyEnum() == Currencies.HINTS) {
                dataManager.addHints(reward.getCount());
            }
            return true;
        });
        dailyRewardDialog.show();
    }

    private void checkBonuses() {
        List<Bonus> activeBonuses = Bonus.Companion.getActiveBonuses();
        if (!activeBonuses.isEmpty()) {
            findViewById(R.id.bonusActive).setVisibility(View.VISIBLE);
        }
        for (Bonus bonus: activeBonuses) {
            new Handler().postDelayed((Runnable) () -> {
                if (Bonus.Companion.getActiveBonuses().isEmpty()) {
                    findViewById(R.id.bonusActive).animate().alpha(0f).setDuration(300);
                }
            }, bonus.getExpireTime() - System.currentTimeMillis());
        }
    }

    @Override
    public void onBackPressed() {
        if (!isThemePreviewMode) {
            new AlertDialog.Builder(this)
                    .setTitle("?????????????????????????? ????????????")
                    .setMessage("???? ?????????? ???????????? ???????????")
                    .setPositiveButton("?????????? ", (dialog, which) -> {
                        finishAffinity();
                    }).setNegativeButton(" ????????????", null)
                    .setIcon(R.drawable.icon)
                    .show();
        } else {
            Intent intent = new Intent(this, ThemeChangeActivity.class);
            intent.putExtra("selectThemeId", getIntent().getIntExtra("themePreviewId", -1));
            startActivity(intent);
            Animatoo.animateSlideRight(this);
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(notificationBroadcastReceiver);
        super.onDestroy();
    }
}