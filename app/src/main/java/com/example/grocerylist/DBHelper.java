package com.example.grocerylist;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.sql.SQLInput;

public class DBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "GroceryList.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE if not exists " + GroceryItem.TABLE_NAME + " (" +
                    GroceryItem._ID + " INTEGER PRIMARY KEY," +
                    GroceryItem.COLUMN_NAME_NAME + " TEXT," +
                    GroceryItem.COLUMN_NAME_QTY + " int," +
                    GroceryItem.COLUMN_NAME_CATEGORY + " int," +
                    GroceryItem.COLUMN_NAME_IMAGE + " TEXT," +
                    GroceryItem.COLUMN_NAME_PRICE + " decimal)";

    public static final String SQL_GET_ENTRIES_FOR_CATEGORY =
            "SELECT * FROM " + GroceryItem.TABLE_NAME + " WHERE " + GroceryItem.COLUMN_NAME_CATEGORY + " = ?";

    public static final String SQL_GET_INFO_FOR_ITEM =
            "SELECT * FROM " + GroceryItem.TABLE_NAME + " WHERE " + GroceryItem.COLUMN_NAME_CATEGORY + " = ? AND " +
                    GroceryItem.COLUMN_NAME_NAME + " =?";

    private static final String SQL_CREATE_CATEGORY =
            "CREATE TABLE if not exists " + Category.TABLE_NAME + " (" +
                    Category._ID + " INTEGER PRIMARY KEY," +
                    Category.COLUMN_NAME_NAME + " TEXT " + ")";

    private static final String SQL_GET_CATEGORY =
            "SELECT " + Category._ID + " FROM " + Category.TABLE_NAME +
                    " WHERE " + Category.COLUMN_NAME_NAME + " = ?";

    private static final String SQL_GET_CATEGORY_NAME =
            "SELECT " + Category.COLUMN_NAME_NAME + " FROM " + Category.TABLE_NAME +
                    " WHERE " + Category._ID + " = ?";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + GroceryItem.TABLE_NAME;

    private static final String SQL_DELETE_ITEM_FROM_CATEGORY =
            "DELETE FROM " + GroceryItem.TABLE_NAME + " WHERE " + GroceryItem.COLUMN_NAME_NAME + " =? AND " +
                    GroceryItem.COLUMN_NAME_CATEGORY + " =?";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_CATEGORY);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void createCategory(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_CATEGORY);
    }

    public Long getCategoryID(SQLiteDatabase db, String name) {
       Cursor allRows = db.rawQuery(SQL_GET_CATEGORY, new String[] {name});
        if (allRows.moveToFirst() ) {
            do {
                return new Long(allRows.getLong(0));
            } while (allRows.moveToNext());
        }
        return null;
    }

    public String getCategoryName(SQLiteDatabase db, String id) {
        Cursor allRows = db.rawQuery(SQL_GET_CATEGORY_NAME, new String[] {id});
        if (allRows.moveToFirst() ) {
            do {
                return allRows.getString(0);
            } while (allRows.moveToNext());
        }
        return null;
    }

    public void removeItemFromDB(SQLiteDatabase db, String itemName, String itemCategory) {
        String categoryId = this.getCategoryID(db, itemCategory).toString();

        db.delete(GroceryItem.TABLE_NAME,GroceryItem.COLUMN_NAME_NAME + " =? AND " +
                GroceryItem.COLUMN_NAME_CATEGORY + " =?", new String[] {itemName, categoryId});
    }

    public void removeCategoryFromDB(SQLiteDatabase db, String itemName) {
        db.delete(Category.TABLE_NAME,Category.COLUMN_NAME_NAME + " =?", new String[] {itemName});
    }
}
