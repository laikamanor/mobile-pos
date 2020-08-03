package com.example.atlanticbakery;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.ArrayAdapter;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Received_SQLite extends SQLiteOpenHelper {
    public  static  final String DATABASE_NAME = "AKPOS.db";
    public  static  final String TABLE_NAME = "tblreceived";
    public  static  final String COL_1 = "id";
    public  static  final String COL_2 = "itemname";
    public  static  final String COL_3 = "quantity";
    public Received_SQLite(@Nullable Context context)  {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + "(id INTEGER PRIMARY KEY AUTOINCREMENT,itemname TEXT, quantity FLOAT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String itemname, Double quantity){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, itemname);
        contentValues.put(COL_3, quantity);
        long resultQuery = db.insert(TABLE_NAME,null, contentValues);
        boolean result;
        result = resultQuery != -1;
        return result;
    }

    public boolean updateData(String id, Double quantity){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("quantity", quantity);
        db.update(TABLE_NAME, contentValues, "id = ?", new String[] {id});
        return true;
    }

    public  Integer deleteData(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "id = ?", new  String[] {id});
    }

    public Cursor getAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    public boolean checkItemName(String itemName){
        boolean result;
        SQLiteDatabase db = this.getReadableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery("SELECT " + COL_1 + " FROM " +  TABLE_NAME + " WHERE " + COL_2  + "='" + itemName + "';",null);
        result = (cursor.moveToFirst());
        return result;
    }

    public boolean checkForTableExists(){
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='"+ TABLE_NAME +"'";
        Cursor mCursor = db.rawQuery(sql, null);
        if (mCursor.getCount() > 0) {
            return true;
        }
        mCursor.close();
        return false;
    }

    public void truncateTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
        db.execSQL("VACUUM");
    }
}
