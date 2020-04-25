package com.xlsoft.kudanar.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TranslationDb";

    private static final int DATABASE_VERSION = 1;

    public DbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String[] alphabets=DbConstants.ALPHABETS;

        for(int i=0;i<alphabets.length;i++){

            String sql="create table "+alphabets[i]+DbConstants.TABLE+
            "( _id integer primary key,"+
                    DbConstants.ENGLISH+" text,"+
                    DbConstants.URDU+" text"+
                    ");";

            sqLiteDatabase.execSQL(sql);

        }


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
