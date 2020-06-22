package com.example.grocerylist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class CategoryExpandableList extends BaseExpandableListAdapter {

    private final ArrayList<ArrayList<String>> children;
    private Context context;
    private ArrayList<String> groups;
    private LayoutInflater inflater;
    private SharedPreferences savedData;
    private DBHelper dbHelper;

    public CategoryExpandableList(Context context,
                                  ArrayList<String> groups,
                                  ArrayList<ArrayList<String>> children, SharedPreferences savedInstanceState, DBHelper helper) {
        this.context = context;
        this.groups = groups;
        this.children = children;
        this.savedData = savedInstanceState;
        this.dbHelper = helper;
        inflater = LayoutInflater.from( context );
    }

    public Object getChild(int groupPosition, int childPosition) {
        return children.get( groupPosition ).get( childPosition );
    }

    public long getChildId(int groupPosition, int childPosition) {
        return (long)( groupPosition*1024+childPosition );  // Max 1024 children per group
    }

    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View v = null;
        if( convertView != null ) {
            v = convertView;
        } else {
            v = inflater.inflate(R.layout.child_row, parent, false);
        }

        String c = (String)getChild( groupPosition, childPosition );
        TextView text = (TextView)v.findViewById( R.id.childname );
        text.setText(c);

        CheckBox cb = (CheckBox) v.findViewById( R.id.check1);
        Boolean isChecked = false;
        if (savedData != null) {
            isChecked = savedData.getBoolean("" + parent.getId() + c,false);
        }
        cb.setChecked(isChecked);

        addDeleteHandler(groupPosition, v, false);

        ImageView info = (ImageView) v.findViewById(R.id.info);
        info.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            final TextView selectedItem = (TextView) ((LinearLayout) v.getParent()).getChildAt(1);
            final String selectedItemText = selectedItem.getText().toString();

            LinearLayout categoryItem = (LinearLayout) ((ExpandableListView) ((LinearLayout) v.getParent()).getParent()).getChildAt(groupPosition);
            final String categoryName = ((TextView) categoryItem.findViewById(R.id.childname)).getText().toString();

            startDetailsActivity(dbHelper, categoryName, selectedItemText);
            }
        });
        return v;
    }

    private void addDeleteHandler (final int groupPosition, View view, final boolean isParentRow) {
        ImageView delete = (ImageView) view.findViewById(R.id.delete);

        delete.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            TextView selectedItem = null;
            if (isParentRow) {
                selectedItem = (TextView) ((LinearLayout) v.getParent()).getChildAt(0);
            } else {
                selectedItem = (TextView) ((LinearLayout) v.getParent()).getChildAt(1);
            }

            final String selectedItemText = selectedItem.getText().toString();

            LinearLayout categoryItem = (LinearLayout) ((ExpandableListView) ((LinearLayout) v.getParent()).getParent()).getChildAt(groupPosition);
            final String categoryName = ((TextView) categoryItem.findViewById(R.id.childname)).getText().toString();
            builder.setMessage("Do you want to remove?");
            builder.setCancelable(false);
            builder.setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (isParentRow) {
                                dbHelper.removeCategoryFromDB(dbHelper.getWritableDatabase(), selectedItemText);
                            } else {
                                dbHelper.removeItemFromDB(dbHelper.getWritableDatabase(), selectedItemText, categoryName);
                            }
                            removeFromList(selectedItemText, categoryName, isParentRow);
                        }
                    });
            builder.setNegativeButton("No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            }
        });
    }

    public void startDetailsActivity(DBHelper dbHelper, String categoryName, String name) {
        Intent intent = new Intent(this.context, Details.class);
        intent.putExtra("categoryName", categoryName);
        intent.putExtra("name", name);
        this.context.startActivity(intent);
    }

    public int getChildrenCount(int groupPosition) {
        return children.get( groupPosition ).size();
    }

    public Object getGroup(int groupPosition) {
        return groups.get( groupPosition );
    }

    public int getGroupCount() {
        return groups.size();
    }

    public long getGroupId(int groupPosition) {
        return (long)( groupPosition*1024 );  // To be consistent with getChildId
    }

    public void removeFromList(String selectedItemText, String categoryName, Boolean isParentRow) {
        if (isParentRow) {
            int categoryIndex = this.groups.indexOf(categoryName);
            if (categoryIndex != -1 && this.children.get(categoryIndex).size() > 0) {
                this.children.get(categoryIndex).remove(selectedItemText);
            }
            this.groups.remove(categoryIndex);
        } else {
            int categoryIndex = this.groups.indexOf(categoryName);
            if (categoryIndex != -1 && this.children.get(categoryIndex).size() > 0) {
                this.children.get(categoryIndex).remove(selectedItemText);
            }
        }
        notifyDataSetChanged();
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View v = null;
        if( convertView != null )
            v = convertView;
        else
            v = inflater.inflate(R.layout.group_row, parent, false);
        addDeleteHandler(groupPosition, v, true);
        String gt = (String)getGroup( groupPosition );
        TextView colorGroup = (TextView)v.findViewById( R.id.childname );
        if( gt != null )
            colorGroup.setText( gt );
        return v;
    }

    public boolean hasStableIds() {
        return true;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void onGroupCollapsed (int groupPosition) {}
    public void onGroupExpanded(int groupPosition) {}


}
