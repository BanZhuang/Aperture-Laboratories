package com.cute.meido.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/*
 建立数据库模块
 */


public class RegularDBHelper extends SQLiteOpenHelper {
    private Context mContext;

    private static final String CREATE_TABEL = "create table RegularInfo(" +
            "id integer primary key autoincrement," +
            "status text," + // X
            "date text," +
            "time text," +
            "location text, " +
            "address text," +
            "premise text," +
            "premiseInfo text," +  // X
            "action text," +
            "actionInfo text)";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABEL);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public RegularDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }


}
