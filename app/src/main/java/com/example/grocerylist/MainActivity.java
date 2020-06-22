package com.example.grocerylist;

import android.app.ExpandableListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


public class MainActivity extends ExpandableListActivity {
    public SQLiteDatabase database;
    RelativeLayout layout;
    boolean isPaused = false;
    Logger log = Logger.getLogger("Grocery list");
    private ExpandableListAdapter mAdapter;
    ExpandableListView expand;
    private static final String LOG_TAG = "CategoryBuild";
    private CategoryExpandableList expListAdapter;
    private ArrayList<String> groupNames;
    private ArrayList<ArrayList<String>> children;
    private DBHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layout = (RelativeLayout) findViewById(R.id.mainLayout);

        FloatingActionButton addItem = (FloatingActionButton) findViewById(R.id.add_item);
        addItem.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                layout.setAlpha(0.2f);
                layout.setBackgroundColor(Color.BLACK);
                isPaused = true;
                startActivityForResult(new Intent(MainActivity.this, AddItem.class), 1);
            }
        });

        helper = new DBHelper(this);
        database = helper.getWritableDatabase();
        helper.onCreate(database);

        groupNames = new ArrayList<String>();
        children = new ArrayList<ArrayList<String>>();
        buildCategories();
        expListAdapter = new CategoryExpandableList(this, groupNames, children, PreferenceManager.getDefaultSharedPreferences(this), helper);
        setListAdapter(expListAdapter);
    }

    public void onResume() {
        if (isPaused) {
            isPaused = false;
            finishActivity(1);
            startActivity(new Intent(MainActivity.this, MainActivity.class));
        }
        super.onResume();
    }

    private ArrayList<String> buildDataForCategory(SQLiteDatabase db, Integer categoryId) {
        ArrayList<String> data = new ArrayList<>();
        Cursor allRows = db.rawQuery(DBHelper.SQL_GET_ENTRIES_FOR_CATEGORY, new String[]{categoryId.toString()});
        if (allRows.moveToFirst()) {
            do {
                data.add(allRows.getString(1));
            } while (allRows.moveToNext());
        }
        return data;
    }

    private void addChild(Integer categoryId, String categoryName) {
        ArrayList<String> dataFromCategory = buildDataForCategory(database, categoryId);
        ArrayList<String> child = new ArrayList<>();

        for (int i = 0; i < dataFromCategory.size(); i++) {
            child.add(dataFromCategory.get(i));
        }

        this.children.add(child);
    }

    public Map<String, Integer> getAllCategories(SQLiteDatabase db) {
        Map<String, Integer> categoriesMap = new HashMap<String, Integer>();
        Cursor allRows = db.rawQuery("SELECT * FROM " + Category.TABLE_NAME, null);
        if (allRows.moveToFirst()) {
            do {
                categoriesMap.put(allRows.getString(1), allRows.getInt(0));
            } while (allRows.moveToNext());
        }
        return categoriesMap;
    }

    private void buildCategories() {
        Map<String, Integer> allCategories = getAllCategories(database);

        List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
        final List<List<Map<String, TextView>>> childData = new ArrayList<List<Map<String, TextView>>>();

        for (Map.Entry<String, Integer> entry : allCategories.entrySet()) {
            this.groupNames.add(entry.getKey());
            addChild(entry.getValue(), entry.getKey());
        }
    }

    public boolean onChildClick(ExpandableListView parent,View v,int groupPosition,int childPosition,
            long id) {
        Log.d(LOG_TAG, "onChildClick: " + childPosition);
        CheckBox cb = (CheckBox) v.findViewById(R.id.check1);
        String childText = ((TextView)v.findViewById( R.id.childname)).getText().toString();


        if (cb != null) {
            cb.toggle();
        }

        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putBoolean("" + parent.getId() + childText, cb.isChecked()).commit();

        return false;
    }
}
