package com.example.visioneh.gobang;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by visionEH on 2017/9/7.
 */

public class DBhelper extends SQLiteOpenHelper {
    public DBhelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }
    /**
     *
     * @param db
     * 建表，Chess表，存储存档的棋点的坐标信息
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
     db.execSQL("create table if not exists Chess (" +
             "id integer primary key autoincrement," +
             "left integer," +
             "right integer," +
             "tag integer" +
             ")");
    }

    /**
     * 当再次点击存档时，消除上次存档信息
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         db.execSQL("drop table if exists Chess");
         onCreate(db);
    }
}
