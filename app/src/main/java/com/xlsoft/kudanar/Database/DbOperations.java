package com.xlsoft.kudanar.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DbOperations {


    DbHelper dbHelper;

    public DbOperations(Context context) {
        dbHelper = new DbHelper(context);
    }


    public boolean insertRecord(String TableAlpha, String english, String urdu) {

        boolean createSuccessful = false;

        ContentValues values = new ContentValues();

        //  values.put(KEY_ID, information.getId());
        values.put(DbConstants.ENGLISH, english);
        values.put(DbConstants.URDU, urdu);

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        createSuccessful = db.insert(TableAlpha + DbConstants.TABLE, null, values) > 0;

        //db.insertWithOnConflict(TableAlpha + DbConstants.TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

//        db.setTransactionSuccessful();
//        db.endTransaction();

        db.close();

        return createSuccessful;

    }


    public String getRecord(String TableAlpha, String english){

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] tableColumns = new String[] {
                DbConstants.URDU
        };
        String whereClause = DbConstants.ENGLISH+"= ?";
        String[] whereArgs = new String[] {english};

        Cursor c = db.query(TableAlpha+DbConstants.TABLE,
                tableColumns,
                whereClause,
                whereArgs,
                null,
                null,
                null);

        String result="";

        while (c.moveToNext()){

            result=c.getString(c.getColumnIndexOrThrow(DbConstants.URDU));

        }

        return result;

    }




}
