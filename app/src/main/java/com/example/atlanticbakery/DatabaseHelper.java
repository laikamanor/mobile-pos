package com.example.atlanticbakery;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    public  static  final String DATABASE_NAME = "AKPOS.db";
    public  static  final String TABLE_NAME = "tblorders";
    public  static  final String COL_1 = "id";
    public  static  final String COL_2 = "itemid";
    public  static  final String COL_3 = "quantity";
    public  static  final String COL_4 = "discountpercent";
    public  static  final String COL_5 = "totalprice";
    public  static  final String COL_6 = "free";


    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table tblorders " + "(id INTEGER PRIMARY KEY AUTOINCREMENT,itemid INTEGER, quantity FLOAT, discountpercent FLOAT, totalprice FLOAT, free INTEGER)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(Integer itemid, Integer quantity, Double discountpercent, Double totalprice, Integer free){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, itemid);
        contentValues.put(COL_3, quantity);
        contentValues.put(COL_4, discountpercent);
        contentValues.put(COL_5, totalprice);
        contentValues.put(COL_6, free);
        long resultQuery = db.insert(TABLE_NAME,null, contentValues);
        boolean result = false;
        if(resultQuery == -1){
            result= false;
        }else{
            result = true;
        }
        return result;
    }

    public Cursor getAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        return result;
    }
}
