package com.ahmedmolawale.gradepointmanager;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import model.DbInfo;

;

/**
 * Created by MOlawale on 8/8/2015.
 */
public class FirstSemesterFragment extends ListFragment {

    private ContentResolver contentResolver;
    private String matric;
    private String level;
    private SimpleCursorAdapter simpleCursorAdapter;
    private ListView listView;
    private int itemSelected = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v;
        String details[];
        contentResolver = getActivity().getContentResolver();
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            details = intent.getStringArrayExtra(Intent.EXTRA_TEXT);
            matric = details[0];
            level = details[1];
        }
        //check the grades table for first semester courses
        String projection[] = {DbInfo.ID, DbInfo.COURSE_CODE, DbInfo.UNIT, DbInfo.SCORE, DbInfo.GRADE};
        String selection = DbInfo.MATRIC_NO + "=? AND " + DbInfo.LEVEL + "=? AND " + DbInfo.SEMESTER + "=?";
        String[] selectionArgs = {matric, level, "First Semester"};
        Cursor cursor = contentResolver.query(GpmContentProvider.CONTENT_URI_GRADES, projection, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            v = inflater.inflate(R.layout.grades_layout, container, false);
            String uiBindFrom[] = {DbInfo.COURSE_CODE, DbInfo.UNIT, DbInfo.SCORE, DbInfo.GRADE};
            int uiBindTo[] = {R.id.coursecode, R.id.unit, R.id.score, R.id.grade};
            simpleCursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.grade_item, cursor, uiBindFrom, uiBindTo, 0);
            setListAdapter(simpleCursorAdapter);
            listView = (ListView) v.findViewById(android.R.id.list);
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    createTheOptions(id);
                    return false;
                }
            });
            return v;

        } else {

            v = inflater.inflate(R.layout.grade_layout_nodata, container, false);
            TextView message = (TextView) v.findViewById(R.id.message);
            message.setText("No Grades for this semester yet.");
            return v;
        }


    }

    private void createTheOptions(final long id) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);
        builder.setTitle("Modify");
        CharSequence options[] = {"Edit Grade", "Delete Grade"};

        builder.setSingleChoiceItems(options, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        itemSelected = which;

                        break;
                    case 1:
                        itemSelected = which;


                        break;

                }

            }
        });
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (itemSelected) {

                    case 0:
                        //create an activity to edit the grade
                        Intent intent = new Intent(getActivity(), EditGrade.class);
                        String data[] = {matric, level, Long.toString(id)};
                        intent.putExtra(Intent.EXTRA_TEXT, data);
                        startActivity(intent);
                        break;
                    case 1:
                        Uri newUri = ContentUris.withAppendedId(GpmContentProvider.CONTENT_URI_GRADES, id);
                        contentResolver.delete(newUri, null, null);
                        Toast.makeText(getActivity(), "Grade deleted successfully.", Toast.LENGTH_SHORT).show();
                        //refresh the list after delete
                        String projection[] = {DbInfo.ID, DbInfo.COURSE_CODE, DbInfo.UNIT, DbInfo.SCORE, DbInfo.GRADE};
                        String selection = DbInfo.MATRIC_NO + "=? AND " + DbInfo.LEVEL + "=? AND " + DbInfo.SEMESTER + "=?";
                        String[] selectionArgs = {matric, level, "First Semester"};
                        Cursor cursor = contentResolver.query(GpmContentProvider.CONTENT_URI_GRADES, projection, selection, selectionArgs, null);
                        simpleCursorAdapter.changeCursor(cursor);
                        simpleCursorAdapter.notifyDataSetChanged();
                        break;


                }


            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Toast.makeText(getActivity(), "Long click to Modify", Toast.LENGTH_SHORT).show();
    }
}
