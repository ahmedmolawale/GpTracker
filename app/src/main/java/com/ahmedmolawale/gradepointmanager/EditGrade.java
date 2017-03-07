package com.ahmedmolawale.gradepointmanager;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import model.DbInfo;


public class EditGrade extends ActionBarActivity {

    private ContentResolver contentResolver;
    private Spinner unitSpinner;
    private EditText courseCode;
    private EditText scoreEditText;

    private String[] unitData;
    private String[] receivedData;
    private String matric;
    private String level;  //used this for final verification before updating the grade table
    private String id;  //id on the grade table
    private String unitSelected;
    private Button update, cancel;
    private String initialCourseCodeBeforeUpdate;
    private String courseScore;
    private String gpSystem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_grade_layout);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {

            receivedData = intent.getStringArrayExtra(Intent.EXTRA_TEXT);
            matric = receivedData[0];
            level = receivedData[1];
            id = receivedData[2];

        }
        scoreEditText = (EditText) findViewById(R.id.score_edit);
        unitSpinner = (Spinner) findViewById(R.id.spinner_units_edit);
        courseCode = (EditText) findViewById(R.id.course_code_edit);
        update = (Button) findViewById(R.id.update_on_edit);
        cancel = (Button) findViewById(R.id.cancel_on_edit);

        //get the gpsystem of the user from the user table
        String[] projection = {DbInfo.GP_SYSTEM};
        String selection = DbInfo.MATRIC_NO + "=?";
        String selectionArgs[] = {matric};
        contentResolver = getContentResolver();
        final Cursor cursor = contentResolver.query(GpmContentProvider.CONTENT_URI_USERS, projection, selection, selectionArgs, null);

        if (cursor != null && cursor.moveToFirst()) {

            gpSystem = cursor.getString(cursor.getColumnIndex(DbInfo.GP_SYSTEM));
        }
        unitData = getResources().getStringArray(R.array.units);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                unitSpinner.getContext(), R.layout.spinner_item, unitData);
        unitSpinner.setAdapter(adapter);


        //we need get the current unitSpinner and scoreEditText for this course; we use the id here.
        Uri uri = ContentUris.withAppendedId(GpmContentProvider.CONTENT_URI_GRADES, Long.parseLong(id));
        String projection1[] = {DbInfo.COURSE_CODE, DbInfo.UNIT, DbInfo.SCORE};
        Cursor cursor1 = contentResolver.query(uri, projection1, null, null, null);
        String score = "";
        String unit = "";

        if (cursor1 != null && cursor1.moveToFirst()) {

            initialCourseCodeBeforeUpdate = cursor1.getString(cursor1.getColumnIndex(DbInfo.COURSE_CODE));
            courseCode.setText(initialCourseCodeBeforeUpdate);
            unit = cursor1.getString(cursor1.getColumnIndex(DbInfo.UNIT));
            score = cursor1.getString(cursor1.getColumnIndex(DbInfo.SCORE));
            scoreEditText.setText(score);
        }
        // to set the unit for the spinner
        int indexOfUnitSpinner = 0;
        for (int i = 0; i < unitData.length; i++) {

            if (unit.equals(unitData[i])) {
                indexOfUnitSpinner = i;
                break;

            } else {
                continue;
            }
        }
        unitSpinner.setSelection(indexOfUnitSpinner);

        unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                unitSelected = unitData[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int unitPosition = unitSpinner.getSelectedItemPosition();
                courseScore = scoreEditText.getText().toString();
                if (courseCode.getText().toString().equals("")) {

                    displayMessage("Course Code", "Please enter the Course Code.");

                } else if (unitPosition == 0) {
                    displayMessage("Unit", "Please select a unit for the Course.");

                } else if (courseScore.equals("") || (Integer.parseInt(courseScore) < 0 || Integer.parseInt(courseScore) > 100)) {
                    displayMessage("Score", "Please enter a valid score for the Course.");
                } else {//everthing is fine? lets update then...but before then, we need to check if the course is in the database; end user could be very funny

                    //lets check for that
                    String pro[] = {DbInfo.COURSE_CODE};
                    String sele = DbInfo.MATRIC_NO + "=? AND " + DbInfo.COURSE_CODE + " =? AND " + DbInfo.LEVEL + " =?";
                    String selArgs[] = {matric, courseCode.getText().toString(), level}; //we need the level here cos same course can actually be entered for different level in case of carry over
                    Cursor c = getContentResolver().query(GpmContentProvider.CONTENT_URI_GRADES, pro, sele, selArgs, null);

                    if (c != null && c.moveToFirst()) {
                        if (c.getString(c.getColumnIndex(DbInfo.COURSE_CODE)).equals(initialCourseCodeBeforeUpdate)) {

                            updateNow();
                        } else {

                            displayMessage("Course Found!", "Grade of this new course is already saved before.");
                        }
                    } else {
                        updateNow();
                    }
                }
            }


        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });

    }

    private void updateNow() {
        String gradeObtained = "";
        int score = Integer.parseInt(courseScore.toString());
        switch (gpSystem) {

            case "4":
                if (score >= 70) {
                    gradeObtained = "A";
                } else if (score >= 60 && score <= 69) {
                    gradeObtained = "AB";
                } else if (score >= 50 && score <= 59) {
                    gradeObtained = "B";
                } else if (score >= 45 && score <= 49) {
                    gradeObtained = "BC";
                } else if (score >= 40 && score <= 44) {
                    gradeObtained = "C";
                } else {
                    gradeObtained = "F";
                }
                break;
            case "5":
                if (score >= 70) {
                    gradeObtained = "A";
                } else if (score >= 60 && score <= 69) {
                    gradeObtained = "B";
                } else if (score >= 50 && score <= 59) {
                    gradeObtained = "C";
                } else if (score >= 45 && score <= 49) {
                    gradeObtained = "D";
                } else if (score >= 40 && score <= 44) {
                    gradeObtained = "E";
                } else {
                    gradeObtained = "F";
                }
                break;
            case "7":
                if (score >= 70) {
                    gradeObtained = "7 pts";
                } else if (score >= 65 && score <= 69) {
                    gradeObtained = "6 pts";
                } else if (score >= 60 && score <= 64) {
                    gradeObtained = "5 pts";
                } else if (score >= 55 && score <= 59) {
                    gradeObtained = "4 pts";
                } else if (score >= 50 && score <= 54) {
                    gradeObtained = "3 pts";
                } else if (score >= 45 && score <= 49) {
                    gradeObtained = "2 pts";
                } else if (score >= 40 && score <= 44) {
                    gradeObtained = "1 pt";
                } else {
                    gradeObtained = "0 pt";
                }
                break;

        }


        ContentValues contentValues = new ContentValues();
        contentValues.put(DbInfo.COURSE_CODE, courseCode.getText().toString());
        contentValues.put(DbInfo.UNIT, unitSelected);
        contentValues.put(DbInfo.SCORE, score);
        contentValues.put(DbInfo.GRADE, gradeObtained);

        Uri uri = ContentUris.withAppendedId(GpmContentProvider.CONTENT_URI_GRADES, Long.parseLong(id));
        int update = contentResolver.update(uri, contentValues, null, null);
        GradeViewer.fa.finish();
        finish();
        //get the matric no and level of this particular id and create the gradeviewer
        String[] projection = {DbInfo.MATRIC_NO, DbInfo.LEVEL};
        Cursor cursor2 = contentResolver.query(uri, projection, null, null, null);
        if (cursor2 != null && cursor2.moveToFirst()) {
            String matric = cursor2.getString(cursor2.getColumnIndex(DbInfo.MATRIC_NO));
            String level = cursor2.getString(cursor2.getColumnIndex(DbInfo.LEVEL));
            String data[] = {matric, level};
            Intent intent = new Intent(EditGrade.this, GradeViewer.class);
            intent.putExtra(Intent.EXTRA_TEXT, data);
            startActivity(intent);
            if (update > 0) {
                Toast.makeText(getApplicationContext(), "Update Successful", Toast.LENGTH_SHORT).show();
            }

        }
    }
    private void displayMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }
}