package com.example.grocerylist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;

public class AddItem extends Activity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private SQLiteDatabase database;
    private String image_path;
    private DBHelper helper;
    private Camera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        helper = new DBHelper(this);
        this.database = this.helper.getWritableDatabase();
        setContentView(R.layout.activity_add_item2);
        this.camera = new Camera(this);
        initializeCamera();
        initializeCategories();

        Button submitForm = (Button) findViewById(R.id.btn_add_item);
        submitForm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addItem();
            }
        });
    }

    public void initializeCamera() {
        Button takePhoto = findViewById(R.id.open_camera);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setImage(camera.dispatchTakePictureIntent());
            }
        });
    }

    private void setImage(String image_path) {
        this.image_path = image_path;
        this.camera.setPicture(image_path);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK  && data != null) {
            if (data.getExtras() != null) {
                Bundle extras = data.getExtras();
                ImageView imageView = findViewById(R.id.image);
                Bitmap bitmap = (Bitmap) extras.get("data");
                if (bitmap != null) {
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    private void addItem() {
        EditText name = (EditText) findViewById(R.id.name);
        EditText qty = (EditText) findViewById(R.id.qty);
        EditText price = (EditText) findViewById(R.id.price);
        Spinner categories = (Spinner) findViewById(R.id.spinner);
        EditText newCategory = (EditText) findViewById(R.id.addCategory);
        String image_path = this.image_path;
        Long categoryID = null;

        if (newCategory.length() > 0) {
            ContentValues categoryValues = new ContentValues();
            categoryValues.put(Category.COLUMN_NAME_NAME, newCategory.getText().toString());
            categoryID = database.insert(Category.TABLE_NAME, null, categoryValues);
        } else {
            categoryID = this.helper.getCategoryID(this.database, categories.getSelectedItem().toString());
        }

        ContentValues values = new ContentValues();
        values.put(GroceryItem.COLUMN_NAME_CATEGORY, categoryID.toString());
        values.put(GroceryItem.COLUMN_NAME_IMAGE, image_path);
        values.put(GroceryItem.COLUMN_NAME_PRICE, price.getText().toString());
        values.put(GroceryItem.COLUMN_NAME_QTY, qty.getText().toString());
        values.put(GroceryItem.COLUMN_NAME_NAME, name.getText().toString());
        database.insert(GroceryItem.TABLE_NAME, null, values);
        this.finish();
    }

    private void initializeAddCategoryField() {
        Spinner categories = (Spinner) findViewById(R.id.spinner);
        EditText addCategory = findViewById(R.id.addCategory);
        categories.setVisibility(View.GONE);
        addCategory.setVisibility(View.VISIBLE);
    }

    private void initializeCategories() {
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, android.R.id.text1);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        String categoryTable = Category.TABLE_NAME;

        String tableString = String.format("Table %s:\n", categoryTable);
        Cursor allRows  = this.database.rawQuery("SELECT * FROM " + categoryTable, null);
        if (allRows.moveToFirst() ) {
            do {
                spinnerAdapter.add(allRows.getString(1));
            } while (allRows.moveToNext());
        }
        spinnerAdapter.add("Add category");
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                TextView item = (TextView) view;
                if (item.getText() == "Add category") {
                    initializeAddCategoryField();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}
