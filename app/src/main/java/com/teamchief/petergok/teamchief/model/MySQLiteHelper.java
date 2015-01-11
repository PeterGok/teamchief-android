package com.teamchief.petergok.teamchief.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.teamchief.petergok.teamchief.model.MessagesTable;

public class MySQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "teamchief.db";
    private static final int DATABASE_VERSION = 2;

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        MessagesTable.onCreate(database);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        MessagesTable.onUpgrade(database, oldVersion, newVersion);
    }
}
