package com.nestor87.swords.data.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.nestor87.swords.ui.main.MainActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "SWords";
    private static final int DB_VERSION = 2;
    private Context context;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
//        db.execSQL("CREATE TABLE data (id INTEGER PRIMARY KEY AUTOINCREMENT, score TEXT, hints INTEGER, letters TEXT, words Text);");
//        db.execSQL("CREATE TABLE words (id INTEGER PRIMARY KEY AUTOINCREMENT, word TEXT);");


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1 && newVersion == 2) {
            db.execSQL("CREATE TABLE statisticsWords (word TEXT)");
        }
    }

    private void copyDBFromAssets() {
        try (InputStream inputStream = context.getAssets().open(DB_NAME + ".db")) {
            File outputFile = new File(context.getDatabasePath(DB_NAME).getPath() + ".db");
            outputFile.getParentFile().mkdirs();
            outputFile.createNewFile();
            try (OutputStream outputStream = new FileOutputStream(outputFile, false)) {

                byte[] buf = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, len);
                }
                outputStream.flush();
                Log.i(MainActivity.LOG_TAG, "DB was successfully copied");
            }
        } catch (IOException e) {
//            Log.e("SWordsL", e.getStackTrace());
            e.printStackTrace();
        }
    }

    public void copyDBIfNotExists() {
        if (!isDBExists()) {
            Log.i(MainActivity.LOG_TAG, "DB don`t exists");
            copyDBFromAssets();
        } else {
            Log.i(MainActivity.LOG_TAG, "DB already exists");
        }
    }

    private boolean isDBExists() {
        return new File (context.getDatabasePath(DB_NAME).getPath() + ".db").exists();
//        return false;
    }

    public SQLiteDatabase openDB() {
        return SQLiteDatabase.openDatabase(context.getDatabasePath(DB_NAME).getPath() + ".db", null, SQLiteDatabase.OPEN_READWRITE);
    }

}
