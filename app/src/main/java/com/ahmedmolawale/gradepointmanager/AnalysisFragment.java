package com.ahmedmolawale.gradepointmanager;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import model.DbInfo;

/**
 * Created by MOlawale on 8/10/2015.
 */
public class AnalysisFragment extends Fragment {

    private TextView name, matric, level, coursesTaken, gradePointSystem, firstSemesterGP, secondSemesterGP, levelGPA, classOfGrade;
    private String[] dataReceived;
    private String matricReceived, levelReceived;
    private ContentResolver contentResolver;
    private Cursor cursor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v;
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            dataReceived = intent.getStringArrayExtra(Intent.EXTRA_TEXT);
            matricReceived = dataReceived[0];
            levelReceived = dataReceived[1];
        }
        contentResolver = getActivity().getContentResolver();
        String projection[] = {DbInfo.UNIT, DbInfo.GRADE, DbInfo.SEMESTER};
        String selection = DbInfo.MATRIC_NO + "=? AND " + DbInfo.LEVEL + "=?";
        String selectionArgs[] = {matricReceived, levelReceived};
        cursor = contentResolver.query(GpmContentProvider.CONTENT_URI_GRADES, projection, selection, selectionArgs, null);

        //we check if there is any course for this level before displaying
        if (cursor != null && cursor.moveToFirst()) {

            v = inflater.inflate(R.layout.analysis_fragment_layout, container, false);

            name = (TextView) v.findViewById(R.id.name_on_analysis);
            matric = (TextView) v.findViewById(R.id.matric_on_analysis);
            level = (TextView) v.findViewById(R.id.level_on_analysis);
            coursesTaken = (TextView) v.findViewById(R.id.courses_taken_on_analysis);
            firstSemesterGP = (TextView) v.findViewById(R.id.first_semester_gp_analysis);
            secondSemesterGP = (TextView) v.findViewById(R.id.second_semester_gp_on_analysis);
            levelGPA = (TextView) v.findViewById(R.id.level_gpa_on_analysis);
            classOfGrade = (TextView) v.findViewById(R.id.class_of_grade_on_analysis);
            gradePointSystem = (TextView) v.findViewById(R.id.gp_system_on_analysis);
            //lets get the level and matric from the intent of the associated activity

            //lets retrieve the gp system,name, from the user table

            String projection1[] = {DbInfo.NAME, DbInfo.GP_SYSTEM};
            String selection1 = DbInfo.MATRIC_NO + " =?";
            String selectionArgs1[] = {matricReceived};
            Cursor cursor1 = contentResolver.query(GpmContentProvider.CONTENT_URI_USERS, projection1, selection1, selectionArgs1, null);
            if (cursor1 != null && cursor1.moveToFirst()) {
                //set the name, matric,level and gp system
                name.setText(cursor1.getString(cursor1.getColumnIndex(DbInfo.NAME)));
                matric.setText(matricReceived);
                gradePointSystem.setText(cursor1.getString(cursor1.getColumnIndex(DbInfo.GP_SYSTEM)) + " Point Scale");
                level.setText(levelReceived);

            } else {
                Toast.makeText(getActivity(), "An error occurred", Toast.LENGTH_LONG).show();
            }
            //Using the gp system, we do the following
            switch (cursor1.getString(cursor1.getColumnIndex(DbInfo.GP_SYSTEM))) {

                case "4":
                    computeResultForFourPointSystem();
                    break;
                case "5":
                    computeResultForFivePointSystem();
                    break;
                case "7":
                    computeResultForSevenPointSystem();
                    break;


            }
        } else {

            v = inflater.inflate(R.layout.analysis_fragment_nodata, container, false);


        }

        return v;
    }

    private void computeResultForFourPointSystem() {

        //use the field variable cursor to work here

        //we can easily set the courses taken so far here viz:
        coursesTaken.setText(Integer.toString(cursor.getCount()));
        double gradePointForFirstSemester;
        double gradePointForSecondSemester;
        double levelGradePointAverage;

        //for the first semester
        int totalUnitsForFirstSemester = 0;
        double totalPointsForFirstSemester = 0.0;

        //for the second semester
        int totalUnitsForSecondSemester = 0;
        double totalPointsForSecondSemester = 0.0;


        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            String unit = cursor.getString(cursor.getColumnIndex(DbInfo.UNIT));
            String grade = cursor.getString(cursor.getColumnIndex(DbInfo.GRADE));
            String semester = cursor.getString(cursor.getColumnIndex(DbInfo.SEMESTER));
            int[] pointsArray = {4, 3, 2, 1, 0};
            String gradeTypes[] = {"A", "B", "C", "D", "F"};

            switch (semester) {

                case "First Semester":

                    for (int j = 0; j < gradeTypes.length; j++) {
                        if (grade.equals(gradeTypes[j])) {
                            totalPointsForFirstSemester += Integer.parseInt(unit) * pointsArray[j];
                            totalUnitsForFirstSemester += Integer.parseInt(unit);
                            break;

                        } else {
                            continue;
                        }

                    }
                    break;
                case "Second Semester":

                    for (int j = 0; j < gradeTypes.length; j++) {
                        if (grade.equals(gradeTypes[j])) {
                            totalPointsForSecondSemester += Integer.parseInt(unit) * pointsArray[j];
                            totalUnitsForSecondSemester += Integer.parseInt(unit);
                            break;

                        } else {
                            continue;
                        }

                    }

            }


        }
        gradePointForFirstSemester = totalPointsForFirstSemester / totalUnitsForFirstSemester;
        firstSemesterGP.setText(String.format("%.2f", gradePointForFirstSemester));

        gradePointForSecondSemester = totalPointsForSecondSemester / totalUnitsForSecondSemester;
        secondSemesterGP.setText(String.format("%.2f", gradePointForSecondSemester));

        levelGradePointAverage = (totalPointsForFirstSemester + totalPointsForSecondSemester) / (totalUnitsForFirstSemester + totalPointsForSecondSemester);
        levelGPA.setText(String.format("%.2f", levelGradePointAverage));

        if (totalUnitsForFirstSemester == 0) {
            firstSemesterGP.setText("No grades yet.");
        } else {
            gradePointForFirstSemester = totalPointsForFirstSemester / totalUnitsForFirstSemester;

            firstSemesterGP.setText(String.format("%.2f", gradePointForFirstSemester));
        }

        if (totalUnitsForSecondSemester == 0) {
            secondSemesterGP.setText("No Grades Yet.");
        } else {
            gradePointForSecondSemester = totalPointsForSecondSemester / totalUnitsForSecondSemester;
            secondSemesterGP.setText(String.format("%.2f", gradePointForSecondSemester));

        }

        if (totalUnitsForFirstSemester + totalUnitsForSecondSemester > 0) {

            levelGradePointAverage = (totalPointsForFirstSemester + totalPointsForSecondSemester) / (totalUnitsForFirstSemester + totalUnitsForSecondSemester);
            String gpa = String.format("%.2f", levelGradePointAverage);
            levelGPA.setText(gpa);
            String classOfGrade = "";
            if (Double.parseDouble(gpa) >= 3.50)
                classOfGrade = "First Class. Congratulations!!!";
            else if (Double.parseDouble(gpa) >= 3.00 && Double.parseDouble(gpa) <= 3.49)
                classOfGrade = "Second Class Upper. Well done.";
            else if (Double.parseDouble(gpa) >= 2.00 && Double.parseDouble(gpa) <= 2.99)
                classOfGrade = "Second Class Lower.";
            else if (Double.parseDouble(gpa) >= 1.00 && Double.parseDouble(gpa) <= 1.99)
                classOfGrade = "Third Class.";
            else if (Double.parseDouble(gpa) < 1.00)
                classOfGrade = "Fail.";

            this.classOfGrade.setText(classOfGrade);
        } else {
            this.classOfGrade.setText("No Class of Grade yet.");

        }

    }

    private void computeResultForFivePointSystem() {

        //use the field variable cursor to work here

        //we can easily set the courses taken so far here viz:
        coursesTaken.setText(Integer.toString(cursor.getCount()));
        double gradePointForFirstSemester;
        double gradePointForSecondSemester;
        double levelGradePointAverage;

        //for the first semester
        int totalUnitsForFirstSemester = 0;
        double totalPointsForFirstSemester = 0.0;

        //for the second semester
        int totalUnitsForSecondSemester = 0;
        double totalPointsForSecondSemester = 0.0;


        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            String unit = cursor.getString(cursor.getColumnIndex(DbInfo.UNIT));
            String grade = cursor.getString(cursor.getColumnIndex(DbInfo.GRADE));
            String semester = cursor.getString(cursor.getColumnIndex(DbInfo.SEMESTER));
            int[] pointsArray = {5, 4, 3, 2, 1, 0};
            String gradeTypes[] = {"A", "B", "C", "D", "E", "F"};

            switch (semester) {

                case "First Semester":

                    for (int j = 0; j < gradeTypes.length; j++) {
                        if (grade.equals(gradeTypes[j])) {
                            totalPointsForFirstSemester += Integer.parseInt(unit) * pointsArray[j];
                            totalUnitsForFirstSemester += Integer.parseInt(unit);
                            break;

                        } else {
                            continue;
                        }

                    }
                    break;
                case "Second Semester":

                    for (int j = 0; j < gradeTypes.length; j++) {
                        if (grade.equals(gradeTypes[j])) {
                            totalPointsForSecondSemester += Integer.parseInt(unit) * pointsArray[j];
                            totalUnitsForSecondSemester += Integer.parseInt(unit);
                            break;

                        } else {
                            continue;
                        }

                    }

            }


        }
        if (totalUnitsForFirstSemester == 0) {
            firstSemesterGP.setText("No grades yet.");
        } else {
            gradePointForFirstSemester = totalPointsForFirstSemester / totalUnitsForFirstSemester;

            firstSemesterGP.setText(String.format("%.2f", gradePointForFirstSemester));
        }

        if (totalUnitsForSecondSemester == 0) {
            secondSemesterGP.setText("No Grades Yet.");
        } else {
            gradePointForSecondSemester = totalPointsForSecondSemester / totalUnitsForSecondSemester;
            secondSemesterGP.setText(String.format("%.2f", gradePointForSecondSemester));

        }

        if (totalUnitsForFirstSemester + totalUnitsForSecondSemester > 0) {

            levelGradePointAverage = (totalPointsForFirstSemester + totalPointsForSecondSemester) / (totalUnitsForFirstSemester + totalUnitsForSecondSemester);
            String gpa = String.format("%.2f", levelGradePointAverage);
            levelGPA.setText(gpa);

            String classOfGrade;
            if (Double.parseDouble(gpa) >= 4.50)
                classOfGrade = "First Class. Congratulations!!!";
            else if (Double.parseDouble(gpa) >= 3.50 && Double.parseDouble(gpa) <= 4.49)
                classOfGrade = "Second Class (Upper Division).";
            else if (Double.parseDouble(gpa) >= 2.40 && Double.parseDouble(gpa) <= 3.49)
                classOfGrade = "Second Class (Lower Division).";
            else if (Double.parseDouble(gpa) >= 1.50 && Double.parseDouble(gpa) <= 2.39)
                classOfGrade = "Third Class.";
            else if (Double.parseDouble(gpa) >= 1.00 && Double.parseDouble(gpa) <= 1.49)
                classOfGrade = "Pass.";
            else
                classOfGrade = "Fail.";

            this.classOfGrade.setText(classOfGrade);


        } else {//this is not necessary as it implies there is no grades in the database at all which we have taken care of above

            this.classOfGrade.setText("No Class of Grade yet.");
        }


    }

    private void computeResultForSevenPointSystem() {

        //use the field variable cursor to work here

        //we can easily set the courses taken so far here viz:
        coursesTaken.setText(Integer.toString(cursor.getCount()));
        double gradePointForFirstSemester;
        double gradePointForSecondSemester;
        double levelGradePointAverage;

        //for the first semester
        int totalUnitsForFirstSemester = 0;
        double totalPointsForFirstSemester = 0.0;

        //for the second semester
        int totalUnitsForSecondSemester = 0;
        double totalPointsForSecondSemester = 0.0;


        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            String unit = cursor.getString(cursor.getColumnIndex(DbInfo.UNIT));
            String grade = cursor.getString(cursor.getColumnIndex(DbInfo.GRADE));
            String semester = cursor.getString(cursor.getColumnIndex(DbInfo.SEMESTER));
            int[] pointsArray = {7, 6, 5, 4, 3, 2, 1, 0};
            String gradeTypes[] = {"7 pts", "6 pts", "5 pts", "4 pts", "3 pts", "2 pts", "1 pt", "0 pt"};

            switch (semester) {

                case "First Semester":

                    for (int j = 0; j < gradeTypes.length; j++) {
                        if (grade.equals(gradeTypes[j])) {
                            totalPointsForFirstSemester += Integer.parseInt(unit) * pointsArray[j];
                            totalUnitsForFirstSemester += Integer.parseInt(unit);
                            break;

                        } else {
                            continue;
                        }

                    }
                    break;
                case "Second Semester":

                    for (int j = 0; j < gradeTypes.length; j++) {
                        if (grade.equals(gradeTypes[j])) {
                            totalPointsForSecondSemester += Integer.parseInt(unit) * pointsArray[j];
                            totalUnitsForSecondSemester += Integer.parseInt(unit);
                            break;

                        } else {
                            continue;
                        }

                    }

            }


        }
        if (totalUnitsForFirstSemester == 0) {
            firstSemesterGP.setText("No grades yet.");
        } else {
            gradePointForFirstSemester = totalPointsForFirstSemester / totalUnitsForFirstSemester;

            firstSemesterGP.setText(String.format("%.1f", gradePointForFirstSemester));
        }

        if (totalUnitsForSecondSemester == 0) {
            secondSemesterGP.setText("No Grades Yet.");
        } else {
            gradePointForSecondSemester = totalPointsForSecondSemester / totalUnitsForSecondSemester;
            secondSemesterGP.setText(String.format("%.1f", gradePointForSecondSemester));

        }


        if (totalUnitsForFirstSemester + totalUnitsForSecondSemester > 0) {

            levelGradePointAverage = (totalPointsForFirstSemester + totalPointsForSecondSemester) / (totalUnitsForFirstSemester + totalUnitsForSecondSemester);
            String gpa = String.format("%.1f", levelGradePointAverage);
            levelGPA.setText(gpa);

            String classOfGrade;
            if (Double.parseDouble(gpa) >= 6.0)
                classOfGrade = "First Class. Congratulations!!!";
            else if (Double.parseDouble(gpa) >= 4.6 && Double.parseDouble(gpa) <= 5.9)
                classOfGrade = "Second Class (Upper Division).";
            else if (Double.parseDouble(gpa) >= 2.6 && Double.parseDouble(gpa) <= 4.5)
                classOfGrade = "Second Class (Lower Division).";
            else if (Double.parseDouble(gpa) >= 1.6 && Double.parseDouble(gpa) <= 2.5)
                classOfGrade = "Third Class.";
            else
                classOfGrade = "Pass.";


            this.classOfGrade.setText(classOfGrade);

        } else {

            this.classOfGrade.setText("No Class of Grade yet.");

        }
    }
}
