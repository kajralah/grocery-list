package com.example.grocerylist;

import android.provider.BaseColumns;

/* Inner class that defines the table contents */
public class GroceryItem implements BaseColumns {
    public static final String TABLE_NAME = "GroceryItem";
    public static final String COLUMN_NAME_NAME = "name";
    public static final String COLUMN_NAME_CATEGORY = "categoryId";
    public static final String COLUMN_NAME_PRICE = "price";
    public static final String COLUMN_NAME_QTY = "quantity";
    public static final String COLUMN_NAME_IMAGE = "img_path";
}