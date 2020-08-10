package com.example.atlanticbakery;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    public  static  final String DATABASE_NAME = "AKPOS.db";
    public  static  final String TABLE_NAME = "tblorders";
    public  static  final String TABLE_NAME2 = "tblreceived";
    public  static  final String COL_2 = "itemname";
    public  static  final String COL_3 = "price";
    public  static  final String COL_4 = "quantity";
    public  static  final String COL_5 = "discountpercent";
    public  static  final String COL_6 = "totalprice";
    public  static  final String COL_7 = "free";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table tblorders " + "(id INTEGER PRIMARY KEY AUTOINCREMENT,itemname TEXT, quantity FLOAT,price FLOAT, discountpercent FLOAT, totalprice FLOAT, free INTEGER)");
        db.execSQL("create table " + TABLE_NAME2 + "(id INTEGER PRIMARY KEY AUTOINCREMENT,itemname TEXT, quantity FLOAT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME2);
        onCreate(db);
    }

    public boolean insertData(Double quantity, Double discountpercent, Double price, Integer free,Double totalprice, String item_name){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, item_name);
        contentValues.put(COL_3, price);
        contentValues.put(COL_4, quantity);
        contentValues.put(COL_5, discountpercent);
        contentValues.put(COL_6, totalprice);
        contentValues.put(COL_7, free);
        long resultQuery = db.insert(TABLE_NAME,null, contentValues);
        boolean result;
        result = resultQuery != -1;
        return result;
    }

    public Cursor getAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    public Double getPrice(Integer id){
        double resultPrice = 0.00;
        SQLiteDatabase db = this.getReadableDatabase();
        @SuppressLint("Recycle") Cursor result = db.rawQuery("SELECT price FROM " + TABLE_NAME + " WHERE id=" + id + ";", null);
        if(result.moveToFirst()){
            do{
                resultPrice = Double.parseDouble(result.getString(0));
            }
            while (result.moveToNext());
        }
        return resultPrice;
    }

    public Double getSubTotal(){
        double resultSubTotal = 0.00;
        SQLiteDatabase db = this.getReadableDatabase();
        @SuppressLint("Recycle") Cursor result = db.rawQuery("SELECT IFNULL(SUM(totalprice),0) FROM " + TABLE_NAME + ";", null);
        if(result.moveToFirst()){
            do{
                resultSubTotal = Double.parseDouble(result.getString(0));
            }
            while (result.moveToNext());
        }
        return resultSubTotal;
    }

    public Double getSubTotalBefore(){
        double resultSubTotal = 0.00;
        SQLiteDatabase db = this.getReadableDatabase();
        @SuppressLint("Recycle") Cursor result = db.rawQuery("SELECT SUM(quantity * price) [subtotal] FROM " + TABLE_NAME + ";", null);
        if(result.moveToFirst()){
            do{
                resultSubTotal = Double.parseDouble(result.getString(0));
            }
            while (result.moveToNext());
        }
        return resultSubTotal;
    }

    public boolean updateData(String id, Double quantity,Double discountpercent,Double totalprice, Integer free){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("quantity", quantity);
        contentValues.put("discountpercent", discountpercent);
        contentValues.put("totalprice", totalprice);
        contentValues.put("free", free);
        db.update(TABLE_NAME, contentValues, "id = ?", new String[] {id});
        return true;
    }

    public  Integer deleteData(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "id = ?", new  String[] {id});
    }

    public Integer countItems(){
        int resultPrice = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        @SuppressLint("Recycle") Cursor result = db.rawQuery("SELECT COUNT(id) FROM " + TABLE_NAME + ";", null);
        if(result.moveToFirst()){
            do{
                resultPrice = Integer.parseInt(result.getString(0));
            }
            while (result.moveToNext());
        }
        return resultPrice;
    }

    public void truncateTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
        db.execSQL("VACUUM");
    }

    public Cursor getItemandQuantity(){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT itemname,quantity FROM " + TABLE_NAME, null);
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
}
