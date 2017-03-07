package com.ahmedmolawale.gradepointmanager;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import model.DbInfo;


public class GradesTaker extends ActionBarActivity {
    String gradePointScale = "";
    private ContentResolver contentResolver;
    private ViewGroup viewGroup;
    private Spinner spinnerForUnits;
    private String[] spinnerForUnitsData;
    private int childViewCount = 0;
    private String[] details;
    private String matricno;
    private String level;
    private String semester;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grades_taker_layout);

        viewGroup = (ViewGroup) findViewById(R.id.container);
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {

            details = intent.getStringArrayExtra(Intent.EXTRA_TEXT);
            matricno = details[0];
            level = details[1];
            semester = details[2];


        }
        setTitle(level + " - " + semester);

        //trying to get the gradpointscale of the particular user
        String projection[] = {DbInfo.GP_SYSTEM};
        String selection = DbInfo.MATRIC_NO + "=?";
        String selectionArgs[] = {matricno};
        Cursor cursor = getContentResolver().query(GpmContentProvider.CONTENT_URI_USERS, projection, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            gradePointScale = cursor.getString(cursor.getColumnIndex(DbInfo.GP_SYSTEM));
        }
        addABaseChild();
    }


    private void addANewChild() {
        // Instantiate a new "row" view.
        final ViewGroup newView = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.gp_list_item, viewGroup, false);

        spinnerForUnits = (Spinner) newView.findViewById(R.id.spinner_units);
        spinnerForUnitsData = getResources().getStringArray(R.array.units);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(spinnerForUnits.getContext(),
                R.layout.spinner_item, spinnerForUnitsData);


        spinnerForUnits.setAdapter(adapter);

        newView.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Remove the row from its parent (the container view).
                // Because viewGroup has android:animateLayoutChanges set to true,
                // this removal is automatically animated.
                viewGroup.removeView(newView);
                --childViewCount;  //once a view get removed via the cancel button,
                // we need decrement the counter to compliment what happens to the base child


            }
        });


        viewGroup.addView(newView, childViewCount);

    }

    private void addABaseChild() {
        // Instantiate a new "row" view.
        final ViewGroup newView = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.control_item, viewGroup, false);


        newView.findViewById(R.id.add_new_grade).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewGroup.removeView(newView);
                addANewChild();
                viewGroup.addView(newView, ++childViewCount);


            }
        });
        newView.findViewById(R.id.save_grades).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (viewGroup.getChildCount() <= 1) {//we have only the base child here

                    displayMessage("Message", "Please add at least a course.");

                } else {
                    boolean isCorrect = checkIfGradesAreInputedCorrectly();


                    if (isCorrect) {

                        confirmationDialog("Save Grades", "Do you want to save your Grades now?");

                    } else {

                        displayMessage("Error", "Please enter the course codes, units and valid scores (0-100).");
                    }
                }
            }
        });

        // Because mContainerView has android:animateLayoutChanges set to true,
        // adding this view is automatically animated.
        viewGroup.addView(newView, childViewCount);
        //childViewCount++;

    }
    private void confirmationDialog(String type, String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(type);

        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setIcon(android.R.drawable.ic_menu_save);

        builder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with save
                        saveGradesFor(gradePointScale);

                    }
                });
        builder.setNegativeButton(android.R.string.no,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                    }
                });
        AlertDialog alert11 = builder.create(); // very important
        alert11.show();

    }

    private boolean checkIfGradesAreInputedCorrectly() {

        int noOfGrades = viewGroup.getChildCount();
        --noOfGrades;   //the last view in the view group is the base view
        for (int i = 0; i < noOfGrades; i++) {
            View view = viewGroup.getChildAt(i);

            Spinner unit = (Spinner) view.findViewById(R.id.spinner_units);
            EditText courseCode = (EditText) view.findViewById(R.id.course_code);
            EditText courseScore = (EditText)view.findViewById(R.id.course_score);

            try {
                int score = Integer.parseInt(courseScore.getText().toString());

                if (unit.getSelectedItemPosition() == 0
                        || courseCode.getText().toString().equals("") || (score < 0 || score > 100)) {
                    return false;
                } else {
                    continue; //move to the next view in the view group
                }
            } catch (NumberFormatException e) {

                return false;  //invalid score
            }

        }
        return true;
    }

    private void saveGradesFor(String gradePointScale) {
        int noOfGrades = viewGroup.getChildCount();
        --noOfGrades;   //the last view in the view group is the base view
        String gradeObtained = "";
        ArrayList<String> arrayList = new ArrayList<>();

        contentResolver = getContentResolver();
        for (int i = 0; i < noOfGrades; i++) {
            View view = viewGroup.getChildAt(i);
            Spinner unit = (Spinner) view.findViewById(R.id.spinner_units);
            EditText courseCode = (EditText) view.findViewById(R.id.course_code);
            EditText courseScore = (EditText) view.findViewById(R.id.course_score);
            int unitSelected = unit.getSelectedItemPosition();
            //lets get the actual grade
            int score = Integer.parseInt(courseScore.getText().toString());
            switch (gradePointScale) {

                case "4":
                    if (score >= 70) {
                        gradeObtained = "A";
                    } else if (score >= 60 && score <= 69) {
                        gradeObtained = "B";
                    } else if (score >= 50 && score <= 59) {
                        gradeObtained = "C";
                    } else if (score >= 45 && score <= 49) {
                        gradeObtained = "D";
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
            contentValues.put(DbInfo.MATRIC_NO, matricno);
            contentValues.put(DbInfo.COURSE_CODE, courseCode.getText().toString());
            contentValues.put(DbInfo.UNIT, Integer.toString(unitSelected));
            contentValues.put(DbInfo.SCORE, Integer.toString(score));
            contentValues.put(DbInfo.GRADE, gradeObtained);
            contentValues.put(DbInfo.LEVEL, level);
            contentValues.put(DbInfo.SEMESTER, semester);

            //check if the courses entered have been saved or not
            String projection[] = {DbInfo.COURSE_CODE};
            String selection = DbInfo.MATRIC_NO + " =? AND " + DbInfo.COURSE_CODE + " =? AND " + DbInfo.LEVEL
                    + " =?";
            String selectionArgs[] = {matricno, courseCode.getText().toString(), level};
            Cursor cursor = getContentResolver().query(GpmContentProvider.CONTENT_URI_GRADES, projection, selection, selectionArgs, null);

            if (cursor != null && cursor.moveToFirst()) {
                arrayList.add(cursor.getString(cursor.getColumnIndex(DbInfo.COURSE_CODE)));

            } else { //the course is not yet saved
                //save the course
                contentResolver.insert(GpmContentProvider.CONTENT_URI_GRADES, contentValues);
            }
        }
        if (arrayList.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Grades saved successfully.", Toast.LENGTH_SHORT).show();
        } else {
            String message = "";
            for (int j = 0; j < arrayList.size(); j++) {
                message = message + arrayList.get(j) + ",";
            }
            displayMessage("Already Saved", "Your grade in Course(s):\n" + message + " \nhas already been saved.\n" +
                    "You can use the 'View Grades' Option in the Dashboard to edit them if you wish.");

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
