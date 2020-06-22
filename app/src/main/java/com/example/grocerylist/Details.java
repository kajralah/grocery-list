package com.example.grocerylist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.FileNotFoundException;

public class Details extends Activity {
    LinearLayout layout;
    SQLiteDatabase database;
    DBHelper helper;
    String oldName;
    String oldCategory;
    String image_path = "";
    Camera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details);
        layout = findViewById(R.id.detailsLayout);

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String categoryName = intent.getStringExtra("categoryName");
        helper = new DBHelper(this);
        database = helper.getWritableDatabase();
        this.camera = new Camera(this);
        Long categoryID = helper.getCategoryID(database, categoryName);
        String categoryIDString = "";
        if (categoryID != null ) {
            categoryIDString = categoryID.toString();
        }
        Cursor row = database.rawQuery(DBHelper.SQL_GET_INFO_FOR_ITEM, new String[] {categoryIDString, name});
        if (row.moveToFirst() ) {
            do {
                try {
                    printLine(database, row.getString(1), row.getString(2), helper.getCategoryName(database, row.getString(3)),
                            row.getString(4), row.getString(5));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } while (row.moveToNext());
        }
        initializeCamera();
        Button updateForm = (Button) findViewById(R.id.btn_update);

        updateForm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                updateItem();
            }
        });

        Button cancelForm = (Button) findViewById(R.id.btn_cancel);

        cancelForm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                cancelForm();
            }
        });
    }

   private void printLine(SQLiteDatabase db, String title, String qty, String categoryName, String image, String price) throws FileNotFoundException {
        TextView name  = findViewById(R.id.detailsName);
        name.setText(title);
        this.oldName = title;
        this.oldCategory = categoryName;
        TextView quantity = findViewById(R.id.detailsQty);
        quantity.setText(qty);
       TextView pricing = findViewById(R.id.detailsPrice);
       pricing.setText(price);
       this.camera.setPicture(image);

       Spinner detailsSpinner = findViewById(R.id.detailsSpinner);
       ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, android.R.id.text1);
       detailsSpinner.setAdapter(spinnerAdapter);

       Cursor allRows  = this.database.rawQuery("SELECT * FROM " + Category.TABLE_NAME, null);
       if (allRows.moveToFirst() ) {
           do {
               spinnerAdapter.add(allRows.getString(1));
           } while (allRows.moveToNext());
       }
       spinnerAdapter.add("Add category");
       int position = spinnerAdapter.getPosition(categoryName);
       detailsSpinner.setSelection(position);
       detailsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
           public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
               TextView item = (TextView) view;
               if (item.getText() == "Add category") {
                   Spinner categories = (Spinner) findViewById(R.id.detailsSpinner);
                   EditText addCategory = findViewById(R.id.addCategory);
                   categories.setVisibility(View.GONE);
                   addCategory.setVisibility(View.VISIBLE);
               }
           }
           @Override
           public void onNothingSelected(AdapterView<?> parent) {
           }
       });
    }

    public void initializeCamera() {
        Button takePhoto = findViewById(R.id.open_camera);
        final Activity currentActivity = this;
        takePhoto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Camera camera = new Camera(currentActivity);
                setImagePath(camera.dispatchTakePictureIntent());
            }
        });
    }

    private void setImagePath(String imagePath) {
        this.image_path = imagePath;
    }

    private void cancelForm() {
        this.finish();
    }

    private void updateItem() {
        EditText name = (EditText) findViewById(R.id.detailsName);
        EditText qty = (EditText) findViewById(R.id.detailsQty);
        EditText price = (EditText) findViewById(R.id.detailsPrice);
        Spinner categories = (Spinner) findViewById(R.id.detailsSpinner);
        EditText newCategory = (EditText) findViewById(R.id.addCategory);
        String image_path = this.image_path;
        Long categoryID = null;

        if (newCategory.length() > 0) {
            ContentValues categoryValues = new ContentValues();
            categoryValues.put(Category.COLUMN_NAME_NAME, newCategory.getText().toString());
            categoryID = database.insert(Category.TABLE_NAME, null, categoryValues);
        } else {
            categoryID = helper.getCategoryID(this.database, categories.getSelectedItem().toString());
        }
        String oldCategoryId = helper.getCategoryID(this.database, this.oldCategory).toString();

        ContentValues values = new ContentValues();
        values.put(GroceryItem.COLUMN_NAME_CATEGORY, categoryID.toString());
        values.put(GroceryItem.COLUMN_NAME_IMAGE, image_path);
        values.put(GroceryItem.COLUMN_NAME_PRICE, price.getText().toString());
        values.put(GroceryItem.COLUMN_NAME_QTY, qty.getText().toString());
        values.put(GroceryItem.COLUMN_NAME_NAME, name.getText().toString());
        int cursor = database.update(GroceryItem.TABLE_NAME, values, GroceryItem.COLUMN_NAME_NAME + " =? AND " +
                               GroceryItem.COLUMN_NAME_CATEGORY + " =?", new String[] {this.oldName, oldCategoryId});

        this.finish();
        startActivity(new Intent(this, MainActivity.class));
    }
}