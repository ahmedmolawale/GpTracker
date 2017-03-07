package com.ahmedmolawale.gradepointmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import model.DbInfo;

/**
 * Created by MOlawale on 6/24/2015.
 */
public class DashboardActivity extends ActionBarActivity {


    private static final int REQUEST_LOAD_IMAGE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    public static Activity dashboardControl;
    //for the drawer layout
    ListView mDrawerList;
    RelativeLayout mDrawerPane;
    ArrayList<NavItem> mNavItems = new ArrayList<NavItem>();
    String recipientMail;
    private Button currentSession;
    private Button previousSession;
    private Button viewGrades;
    private Button currentCGPA;
    private Button settings;
    private String[] userDetails;
    private String levelChoosen = "100 LEVEL";   //to be used with the previous session guys
    private String semesterChoosen = "First Semester";
    private String matricno;
    private String currentlevel = "";
    private String name;
    private String department;
    private boolean cgpaStatus;
    private String currentPicturePath;
    private SharedPreferences sharedPreferences;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private TextView accountOwner;
    private TextView accountOwnerDept;
    private CircleImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_harm);

        dashboardControl = this;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mNavItems.add(new NavItem("Email Result", "Email result to...", R.mipmap.ic_email));

        mNavItems.add(new NavItem("Settings", "Change your preferences", R.mipmap.ic_settings));
        mNavItems.add(new NavItem("Share App", "Share app with friends", R.mipmap.ic_share));
        mNavItems.add(new NavItem("Recommended Apps", "Apps from the same developer", R.mipmap.ic_recommend));
        mNavItems.add(new NavItem("Feedback", "Send us a message", R.mipmap.ic_feedback));
        mNavItems.add(new NavItem("Help", "You need some help?", R.mipmap.ic_help));
        mNavItems.add(new NavItem("About", "See what the app does", R.mipmap.ic_about));
        mNavItems.add(new NavItem("Credits", "Appreciation goes to...", R.mipmap.ic_credits));


        currentSession = (Button) findViewById(R.id.current_session1);
        previousSession = (Button) findViewById(R.id.previous_session1);
        viewGrades = (Button) findViewById(R.id.view_grades1);
        currentCGPA = (Button) findViewById(R.id.current_cgpa1);
        settings = (Button) findViewById(R.id.settings1);


        Intent intent = getIntent();
        if (intent.hasExtra(SignInScreen.INTENT_OF_LOGIN_WINDOW)) { //coming from the login page

            userDetails = intent.getStringArrayExtra(SignInScreen.INTENT_OF_LOGIN_WINDOW);
            name = userDetails[0];
            matricno = userDetails[1];
            department = userDetails[2];

        }
        if (intent.hasExtra(RegistrationScreen.INTENT_OF_REG_WINDOW)) {//coming from the registration window

            userDetails = intent.getStringArrayExtra(RegistrationScreen.INTENT_OF_REG_WINDOW);
            name = userDetails[0];
            matricno = userDetails[1];
            department = userDetails[2];

        }

        profileImage = (CircleImageView) findViewById(R.id.profile_image1);
        mDrawerList = (ListView) findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mDrawerPane = (RelativeLayout) findViewById(R.id.drawerPane);
        accountOwner = (TextView) findViewById(R.id.accountOwner1);
        accountOwnerDept = (TextView) findViewById(R.id.accountOwnerDept1);
        DrawerListAdapter mDrawerListAdapter = new DrawerListAdapter(this, mNavItems);
        mDrawerList.setAdapter(mDrawerListAdapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedItem(position);
            }
        });
        //lets check if there is anything in the currentpicturepath, with that we set the profileimage accordinly
        if (!sharedPreferences.getString(matricno, "").equals("")) {
            setPic(sharedPreferences.getString(matricno, ""));
        }
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pictureDialog();
            }
        });
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                currentSession.setClickable(false);
                previousSession.setClickable(false);

                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                currentSession.setClickable(true);
                previousSession.setClickable(true);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        accountOwner.setText(name);
        accountOwnerDept.setText(department);

        //setting up the action listener for the individual buttons

        currentSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //we here ask the semester which the end user wanna enter grades for
                //please check for current level also
                String projection[] = {DbInfo.CURRENT_LEVEL};
                String selection = DbInfo.MATRIC_NO + "=?";
                String[] selectionArgs = {matricno};
                Cursor cursor = getContentResolver().query(GpmContentProvider.CONTENT_URI_USERS, projection, selection, selectionArgs, null);
                if (cursor != null && cursor.moveToFirst()) {
                    currentlevel = cursor.getString(cursor.getColumnIndex(DbInfo.CURRENT_LEVEL));

                }
                semesterChoosen = "First Semester";
                chooseASemester(currentlevel);

            }
        });
        previousSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check the current level and display options for the user to pick a level here and semester afterwards
                levelChoosen = "100 LEVEL";
                semesterChoosen = "First Semester";
                displayPossiblePrevSessions();

            }
        });

        //now to the view grade guy
        viewGrades.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //we check the current level and display diff levels
                levelChoosen = "100 LEVEL";   //this saves my life
                displayPossibleLevels("View Grades in...", "View", "DisplayGrades");
            }
        });
        //now the cString.format("%.2f", levelGradePointAverage)
        currentCGPA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                cgpaStatus
                        = sharedPreferences.getBoolean(getString(R.string.pref_checkbox1_key), false);
                if (cgpaStatus) {

                    displayCGPA("Disabled", "This option is disabled.\n Use the settings to enable it.");

                } else {

                    String projection1[] = {DbInfo.GP_SYSTEM};
                    String selection1 = DbInfo.MATRIC_NO + " =?";
                    String selectionArgs1[] = {matricno};
                    Cursor cursor1 = getContentResolver().query(GpmContentProvider.CONTENT_URI_USERS, projection1, selection1, selectionArgs1, null);
                    if (cursor1 != null && cursor1.moveToFirst()) {


                        switch (cursor1.getString(cursor1.getColumnIndex(DbInfo.GP_SYSTEM))) {

                            case "4":
                                calculateCGPAForFourPoint();
                                break;
                            case "5":
                                calculateCGPAForFivePoint();
                                break;
                            case "7":
                                calculateCGPAForSevenPoint();
                                break;
                        }
                    }
                }
            }
        });
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, SettingsActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, matricno);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {

            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            currentPicturePath = cursor.getString(columnIndex);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(matricno, currentPicturePath);
            editor.commit();
            cursor.close();
            setPic(currentPicturePath);
        } else {
            setPic(currentPicturePath);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        mDrawerToggle.syncState();
    }

    private void selectedItem(int position) {
        switch (position) {
            case 0:
                //now to this guy
                mDrawerList.setItemChecked(position, true);
                mDrawerLayout.closeDrawer(mDrawerPane);
                displayPossibleLevels("Email result of...", "Send", "SendEmail");

                break;

            case 1:
                mDrawerList.setItemChecked(position, true);
                mDrawerLayout.closeDrawer(mDrawerPane);
                Intent inte = new Intent(DashboardActivity.this, SettingsActivity.class);
                inte.putExtra(Intent.EXTRA_TEXT, matricno);
                startActivity(inte);

                break;
            case 2:
                //share app
                mDrawerList.setItemChecked(position, true);
                mDrawerLayout.closeDrawer(mDrawerPane);
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Hi friend, Have got this great app, GP Tracker.\n " +
                        "It can save your grades and calculate your CGPA throughout your stay in School. Its a great Companion.\n" +
                        "You can download it from the playstore: http://www.play.google.com");
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, "Share via:"));

                break;
            case 3:
                //recommended apps
                mDrawerList.setItemChecked(position, true);
                mDrawerLayout.closeDrawer(mDrawerPane);
                startActivity(new Intent(DashboardActivity.this, RecommendedApp.class));
                break;
            case 4:
                //feedback
                mDrawerList.setItemChecked(position, true);
                mDrawerLayout.closeDrawer(mDrawerPane);
                startActivity(new Intent(DashboardActivity.this, FeedbackActivity.class));

                break;
            case 5:
                //help activity
                mDrawerList.setItemChecked(position, true);
                mDrawerLayout.closeDrawer(mDrawerPane);
                startActivity(new Intent(DashboardActivity.this, HelpActivity.class));

                break;
            case 6:
                mDrawerList.setItemChecked(position, true);
                mDrawerLayout.closeDrawer(mDrawerPane);
                startActivity(new Intent(DashboardActivity.this, AboutActivity.class));

                break;
            case 7:
                mDrawerList.setItemChecked(position, true);
                mDrawerLayout.closeDrawer(mDrawerPane);
                startActivity(new Intent(DashboardActivity.this, CreditsActivity.class));
                break;
        }
    }

    private void calculateCGPAForFourPoint() {
        String projection[] = {DbInfo.UNIT, DbInfo.GRADE};
        String selection = DbInfo.MATRIC_NO + "=?";
        String selectionArgs[] = {matricno};
        Cursor cursor = getContentResolver().query(GpmContentProvider.CONTENT_URI_GRADES, projection, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            double cgpa;

            int totalUnits = 0;
            double totalPoints = 0.0;


            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                String unit = cursor.getString(cursor.getColumnIndex(DbInfo.UNIT));
                String grade = cursor.getString(cursor.getColumnIndex(DbInfo.GRADE));
                int[] pointsArray = {4, 3, 2, 1, 0};
                String gradeTypes[] = {"A", "B", "C", "D", "F"};

                for (int j = 0; j < gradeTypes.length; j++) {
                    if (grade.equals(gradeTypes[j])) {
                        totalPoints += Integer.parseInt(unit) * pointsArray[j];
                        totalUnits += Integer.parseInt(unit);
                        break;

                    } else {
                        continue;
                    }

                }

            }
            cgpa = totalPoints / totalUnits;
            String cgpaFormat;
            cgpaFormat = String.format("%.2f", cgpa);
            displayCGPA("Comm. Grade Point Average", "Your CGPA is: " + cgpaFormat);

        } else {

            displayCGPA("No data", "You have not entered any grade.\nPlease do so.");
        }
    }

    private void calculateCGPAForFivePoint() {
        String projection[] = {DbInfo.UNIT, DbInfo.GRADE};
        String selection = DbInfo.MATRIC_NO + "=?";
        String selectionArgs[] = {matricno};
        Cursor cursor = getContentResolver().query(GpmContentProvider.CONTENT_URI_GRADES, projection, selection, selectionArgs, null);

        if (cursor != null && cursor.moveToFirst()) {
            double cgpa;

            int totalUnits = 0;
            double totalPoints = 0.0;


            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                String unit = cursor.getString(cursor.getColumnIndex(DbInfo.UNIT));
                String grade = cursor.getString(cursor.getColumnIndex(DbInfo.GRADE));
                int[] pointsArray = {5, 4, 3, 2, 1, 0};
                String gradeTypes[] = {"A", "B", "C", "D", "E", "F"};

                for (int j = 0; j < gradeTypes.length; j++) {
                    if (grade.equals(gradeTypes[j])) {
                        totalPoints += Integer.parseInt(unit) * pointsArray[j];
                        totalUnits += Integer.parseInt(unit);
                        break;

                    } else {
                        continue;
                    }

                }

            }
            cgpa = totalPoints / totalUnits;
            String cgpaFormat = String.format("%.2f", cgpa);

            String classOfGrade;
            if (Double.parseDouble(cgpaFormat) >= 4.50)
                classOfGrade = "First Class. Congratulations!!!";
            else if (Double.parseDouble(cgpaFormat) >= 3.50 && Double.parseDouble(cgpaFormat) <= 4.49)
                classOfGrade = "Second Class (Upper Division).";
            else if (Double.parseDouble(cgpaFormat) >= 2.40 && Double.parseDouble(cgpaFormat) <= 3.49)
                classOfGrade = "Second Class (Lower Division).";
            else if (Double.parseDouble(cgpaFormat) >= 1.50 && Double.parseDouble(cgpaFormat) <= 2.39)
                classOfGrade = "Third Class.";
            else if (Double.parseDouble(cgpaFormat) >= 1.00 && Double.parseDouble(cgpaFormat) <= 1.49)
                classOfGrade = "Pass.";
            else
                classOfGrade = "Fail.";
            displayCGPA("Comm. Grade Point Average", "Your CGPA is: " + cgpaFormat + "\n" + classOfGrade);
        } else {
            displayCGPA("No data", "You have not entered any grade.\nPlease do so.");

        }
    }

    private void calculateCGPAForSevenPoint() {
        String projection[] = {DbInfo.UNIT, DbInfo.GRADE};
        String selection = DbInfo.MATRIC_NO + "=?";
        String selectionArgs[] = {matricno};
        Cursor cursor = getContentResolver().query(GpmContentProvider.CONTENT_URI_GRADES, projection, selection, selectionArgs, null);

        if (cursor != null && cursor.moveToFirst()) {
            double cgpa;

            int totalUnits = 0;
            double totalPoints = 0.0;


            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                String unit = cursor.getString(cursor.getColumnIndex(DbInfo.UNIT));
                String grade = cursor.getString(cursor.getColumnIndex(DbInfo.GRADE));
                int[] pointsArray = {7, 6, 5, 4, 3, 2, 1, 0};
                String gradeTypes[] = {"7 pts", "6 pts", "5 pts", "4 pts", "3 pts", "2 pts", "1 pt", "0 pt"};

                for (int j = 0; j < gradeTypes.length; j++) {
                    if (grade.equals(gradeTypes[j])) {
                        totalPoints += Integer.parseInt(unit) * pointsArray[j];
                        totalUnits += Integer.parseInt(unit);
                        break;

                    } else {
                        continue;
                    }
                }
            }
            cgpa = totalPoints / totalUnits;
            String cgpaFormat = String.format("%.1f", cgpa);

            String classOfGrade;
            if (Double.parseDouble(cgpaFormat) >= 6.0)
                classOfGrade = "First Class. Congratulations!!!";
            else if (Double.parseDouble(cgpaFormat) >= 4.6 && Double.parseDouble(cgpaFormat) <= 5.9)
                classOfGrade = "Second Class (Upper Division).";
            else if (Double.parseDouble(cgpaFormat) >= 2.6 && Double.parseDouble(cgpaFormat) <= 4.5)
                classOfGrade = "Second Class (Lower Division).";
            else if (Double.parseDouble(cgpaFormat) >= 1.6 && Double.parseDouble(cgpaFormat) <= 2.5)
                classOfGrade = "Third Class.";
            else
                classOfGrade = "Pass.";

            displayCGPA("Comm. Grade Point Average", "Your CGPA is: " + cgpaFormat + "\n" + classOfGrade);
        } else {
            displayCGPA("No data", "You have not entered any grade.\nPlease do so.");
        }
    }

    private void displayCGPA(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    @Override
    public void onBackPressed() {
        displayMessage();

    }

    private void chooseASemester(final String level) {


        final CharSequence[] semester = {"First Semester", "Second Semester"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Semester");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Intent intent = new Intent(DashboardActivity.this, GradesTaker.class);
                String[] extraDetails = {matricno, level, semesterChoosen};  //Matric no, level and semester, will be needed when saving grades
                intent.putExtra(Intent.EXTRA_TEXT, extraDetails);
                startActivity(intent);
            }

        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setSingleChoiceItems(semester, 0, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {

                    case 0:
                        semesterChoosen = "First Semester";
                        break;
                    case 1:
                        semesterChoosen = "Second Semester";
                        break;
                }

            }
        }).show();

    }

    private void displayPossiblePrevSessions() {

        //use the current level to display possible prev sessions
        //please check the current level from the database
        final CharSequence[] prevSessions;

        String projection[] = {DbInfo.CURRENT_LEVEL};
        String selection = DbInfo.MATRIC_NO + "=?";
        String[] selectionArgs = {matricno};
        Cursor cursor = getContentResolver().query(GpmContentProvider.CONTENT_URI_USERS, projection, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            currentlevel = cursor.getString(cursor.getColumnIndex(DbInfo.CURRENT_LEVEL));

        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Level");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //you are to display semester options here

                chooseASemester(levelChoosen);
            }

        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        switch (currentlevel) {

            case "100 LEVEL":
                Toast.makeText(getApplicationContext(), "No previous session(s) yet. ", Toast.LENGTH_SHORT).show();
                break;
            case "200 LEVEL":
                prevSessions = new CharSequence[]{"100 LEVEL"};
                builder.setSingleChoiceItems(prevSessions, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {

                            case 0:
                                levelChoosen = "100 LEVEL";
                                break;
                        }

                    }
                }).show();

                break;
            case "300 LEVEL":
                prevSessions = new CharSequence[]{"100 LEVEL", "200 LEVEL"};
                builder.setSingleChoiceItems(prevSessions, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {

                            case 0:
                                levelChoosen = "100 LEVEL";
                                break;
                            case 1:
                                levelChoosen = "200 LEVEL";
                                break;
                        }

                    }
                }).show();

                break;
            case "400 LEVEL":
                prevSessions = new CharSequence[]{"100 LEVEL", "200 LEVEL", "300 LEVEL"};
                builder.setSingleChoiceItems(prevSessions, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {

                            case 0:
                                levelChoosen = "100 LEVEL";
                                break;
                            case 1:
                                levelChoosen = "200 LEVEL";
                                break;
                            case 2:
                                levelChoosen = "300 LEVEL";
                                break;
                        }

                    }
                }).show();
                break;
            case "500 LEVEL":
                prevSessions = new CharSequence[]{"100 LEVEL", "200 LEVEL", "300 LEVEL", "400 LEVEL"};
                builder.setSingleChoiceItems(prevSessions, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {

                            case 0:
                                levelChoosen = "100 LEVEL";
                                break;
                            case 1:
                                levelChoosen = "200 LEVEL";
                                break;
                            case 2:
                                levelChoosen = "300 LEVEL";
                                break;
                            case 3:
                                levelChoosen = "400 LEVEL";
                                break;
                        }

                    }
                }).show();
                break;
            case "600 LEVEL":
                prevSessions = new CharSequence[]{"100 LEVEL", "200 LEVEL", "300 LEVEL", "400 LEVEL", "500 LEVEL"};
                builder.setSingleChoiceItems(prevSessions, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {

                            case 0:
                                levelChoosen = "100 LEVEL";
                                break;
                            case 1:
                                levelChoosen = "200 LEVEL";
                                break;
                            case 2:
                                levelChoosen = "300 LEVEL";
                                break;
                            case 3:
                                levelChoosen = "400 LEVEL";
                                break;
                            case 4:
                                levelChoosen = "500 LEVEL";
                                break;
                        }

                    }
                }).show();
                break;

        }
    }

    private void displayPossibleLevels(String title, String positiveAction, final String whatToDo) {

        final CharSequence[] prevSessions;

        String projection[] = {DbInfo.CURRENT_LEVEL};
        String selection = DbInfo.MATRIC_NO + "=?";
        String[] selectionArgs = {matricno};
        Cursor cursor = getContentResolver().query(GpmContentProvider.CONTENT_URI_USERS, projection, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            currentlevel = cursor.getString(cursor.getColumnIndex(DbInfo.CURRENT_LEVEL));

        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setPositiveButton(positiveAction, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                    if (whatToDo.equals("SendEmail")) {
                        takeRecipientMail();
                    } else {
                        //display a three tabbed activity here
                        displayGradesFor(levelChoosen);
                    }
            }

        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        switch (currentlevel) {
            case "100 LEVEL":
                prevSessions = new CharSequence[]{"100 LEVEL"};
                builder.setSingleChoiceItems(prevSessions, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {

                            case 0:
                                levelChoosen = "100 LEVEL";
                                break;
                        }
                    }
                }).show();

                break;


            case "200 LEVEL":
                prevSessions = new CharSequence[]{"100 LEVEL", "200 LEVEL"};
                builder.setSingleChoiceItems(prevSessions, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {

                            case 0:
                                levelChoosen = "100 LEVEL";
                                break;
                            case 1:
                                levelChoosen = "200 LEVEL";
                                break;
                        }

                    }
                }).show();

                break;
            case "300 LEVEL":
                prevSessions = new CharSequence[]{"100 LEVEL", "200 LEVEL", "300 LEVEL"};
                builder.setSingleChoiceItems(prevSessions, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {

                            case 0:
                                levelChoosen = "100 LEVEL";
                                break;
                            case 1:
                                levelChoosen = "200 LEVEL";
                                break;
                            case 2:
                                levelChoosen = "300 LEVEL";
                                break;
                        }

                    }
                }).show();

                break;
            case "400 LEVEL":
                prevSessions = new CharSequence[]{"100 LEVEL", "200 LEVEL", "300 LEVEL", "400 LEVEL"};
                builder.setSingleChoiceItems(prevSessions, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {

                            case 0:
                                levelChoosen = "100 LEVEL";
                                break;
                            case 1:
                                levelChoosen = "200 LEVEL";
                                break;
                            case 2:
                                levelChoosen = "300 LEVEL";
                                break;
                            case 3:
                                levelChoosen = "400 LEVEL";
                                break;
                        }

                    }
                }).show();
                break;
            case "500 LEVEL":
                prevSessions = new CharSequence[]{"100 LEVEL", "200 LEVEL", "300 LEVEL", "400 LEVEL", "500 LEVEL"};
                builder.setSingleChoiceItems(prevSessions, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {

                            case 0:
                                levelChoosen = "100 LEVEL";
                                break;
                            case 1:
                                levelChoosen = "200 LEVEL";
                                break;
                            case 2:
                                levelChoosen = "300 LEVEL";
                                break;
                            case 3:
                                levelChoosen = "400 LEVEL";
                                break;
                            case 4:
                                levelChoosen = "500 LEVEL";
                                break;
                        }

                    }
                }).show();
                break;
            case "600 LEVEL":
                prevSessions = new CharSequence[]{"100 LEVEL", "200 LEVEL", "300 LEVEL", "400 LEVEL", "500 LEVEL", "600 LEVEL"};
                builder.setSingleChoiceItems(prevSessions, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {

                            case 0:
                                levelChoosen = "100 LEVEL";
                                break;
                            case 1:
                                levelChoosen = "200 LEVEL";
                                break;
                            case 2:
                                levelChoosen = "300 LEVEL";
                                break;
                            case 3:
                                levelChoosen = "400 LEVEL";
                                break;
                            case 4:
                                levelChoosen = "500 LEVEL";
                                break;
                            case 5:
                                levelChoosen = "600 LEVEL";
                                break;
                        }

                    }
                }).show();
                break;

        }
    }

    private void takeRecipientMail() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recipient Mail");
        final AlertDialog dialog = builder.create();
        final EditText input = new EditText(this);

        input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setHint("Recipient Mail");
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                recipientMail = input.getText().toString();
                if (recipientMail.equals("")) {
                    Toast.makeText(getApplicationContext(), "Recipient mail cannot be empty", Toast.LENGTH_SHORT).show();

                } else {
                    sendEmailFor(levelChoosen, recipientMail);

                }

            }

        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();

    }
    private void sendEmailFor(String levelChoosen, String recipientMail) {

        //the subject of the mail should be ...My levelChoosen result
        StringBuilder heading = new StringBuilder();
        heading.append("Name:   " + name);
        heading.append("\nMatric No:   " + matricno);
        heading.append("\nDepartment:  " + department);
        //lets append the gp system also as part of the header
        String projection[] = {DbInfo.GP_SYSTEM};
        String selection = DbInfo.MATRIC_NO + " =?";
        String selectionArgs[] = {matricno};
        Cursor cursor = getContentResolver().query(GpmContentProvider.CONTENT_URI_USERS, projection, selection, selectionArgs, null);
        cursor.moveToFirst();
        String gpSystem = cursor.getString(cursor.getColumnIndex(DbInfo.GP_SYSTEM));
        heading.append("\nGrade Point System:  " + gpSystem + " Point scale");

        //first semester details
        StringBuilder firstSemesterDetails = new StringBuilder();
        firstSemesterDetails.append("\n\t\t\t FIRST SEMESTER");
        firstSemesterDetails.append("\nS/N\t\tCourse\t\tUnit\t\tScore\t\tGrade");
        //we need this to calculate the level grade point average
        int totalUnitsForFirstSemester = 0;
        double totalPointsForFirstSemester = 0.0;
        int totalUnitsForSecondSemester = 0;
        double totalPointsForSecondSemester = 0.0;

        //check the grades table for first semester courses
        String projection1[] = {DbInfo.COURSE_CODE, DbInfo.UNIT, DbInfo.SCORE, DbInfo.GRADE};
        String selection1 = DbInfo.MATRIC_NO + "=? AND " + DbInfo.LEVEL + "=? AND " + DbInfo.SEMESTER + "=?";
        String[] selectionArgs1 = {matricno, levelChoosen, "First Semester"};
        Cursor cursor1 = getContentResolver().query(GpmContentProvider.CONTENT_URI_GRADES, projection1, selection1, selectionArgs1, null);
        if (cursor1 != null && cursor1.moveToFirst()) {

            double gradePoint;

            int[] pointsArray;
            String gradeTypes[];
            for (int k = 0; k < cursor1.getCount(); k++) {
                cursor1.moveToPosition(k);
                String courseCode = cursor1.getString(cursor1.getColumnIndex(DbInfo.COURSE_CODE));
                String unit = cursor1.getString(cursor1.getColumnIndex(DbInfo.UNIT));
                String score = cursor1.getString(cursor1.getColumnIndex(DbInfo.SCORE));
                String grade = cursor1.getString(cursor1.getColumnIndex(DbInfo.GRADE));
                firstSemesterDetails.append(String.format("\n%s\t\t\t%s\t\t\t%s\t\t\t\t%s\t\t\t%s", k + 1, courseCode, unit, score, grade));
                switch (gpSystem) {
                    case "4":
                        pointsArray = new int[]{4, 3, 2, 1, 0};
                        gradeTypes = new String[]{"A", "AB", "BC", "C", "F"};
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
                    case "5":
                        pointsArray = new int[]{5, 4, 3, 2, 1, 0};
                        gradeTypes = new String[]{"A", "B", "C", "D", "E", "F"};
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
                    case "7":
                        pointsArray = new int[]{7, 6, 5, 4, 3, 2, 1, 0};
                        gradeTypes = new String[]{"7 pts", "6 pts", "5 pts", "4 pts", "3 pts", "2 pts", "1 pt", "0 pt"};
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

                }
            }
            ///do the calculations here
            gradePoint = totalPointsForFirstSemester / totalUnitsForFirstSemester;
            if (gpSystem.equals("7")) {
                firstSemesterDetails.append(String.format("\n%s\t%.1f", "First Semester GPA:", gradePoint));
            } else {
                firstSemesterDetails.append(String.format("\n%s\t%.2f", "First Semester GPA:", gradePoint));
            }
        } else {
            firstSemesterDetails.append("\n \t\t\tNo Grades for this semester yet.");
        }

        //second semester details
        StringBuilder secondSemesterDetails = new StringBuilder();
        secondSemesterDetails.append("\n\n\t\t\tSECOND SEMESTER");
        secondSemesterDetails.append("\nS/N\t\tCourse\t\tUnit\t\tScore\t\tGrade");
        //check the grades table for second semester courses
        String projection2[] = {DbInfo.COURSE_CODE, DbInfo.UNIT, DbInfo.SCORE, DbInfo.GRADE};
        String selection2 = DbInfo.MATRIC_NO + "=? AND " + DbInfo.LEVEL + "=? AND " + DbInfo.SEMESTER + "=?";
        String[] selectionArgs2 = {matricno, levelChoosen, "Second Semester"};
        Cursor cursor2 = getContentResolver().query(GpmContentProvider.CONTENT_URI_GRADES, projection2, selection2, selectionArgs2, null);
        if (cursor2 != null && cursor2.moveToFirst()) {
            double gradePoint;//grade point for this particular semester
            int[] pointsArray;
            String gradeTypes[];
            for (int k = 0; k < cursor2.getCount(); k++) {
                cursor2.moveToPosition(k);
                String courseCode = cursor2.getString(cursor2.getColumnIndex(DbInfo.COURSE_CODE));
                String unit = cursor2.getString(cursor2.getColumnIndex(DbInfo.UNIT));
                String score = cursor2.getString(cursor2.getColumnIndex(DbInfo.SCORE));
                String grade = cursor2.getString(cursor2.getColumnIndex(DbInfo.GRADE));
                secondSemesterDetails.append(String.format("\n%s\t\t%s\t\t\t%s\t\t\t\t%s\t\t\t%s", k + 1, courseCode, unit, score, grade));

                switch (gpSystem) {
                    case "4":
                        pointsArray = new int[]{4, 3, 2, 1, 0};
                        gradeTypes = new String[]{"A", "AB", "BC", "C", "F"};
                        for (int j = 0; j < gradeTypes.length; j++) {
                            if (grade.equals(gradeTypes[j])) {
                                totalPointsForSecondSemester += Integer.parseInt(unit) * pointsArray[j];
                                totalUnitsForSecondSemester += Integer.parseInt(unit);
                                break;

                            } else {
                                continue;
                            }

                        }
                        break;
                    case "5":
                        pointsArray = new int[]{5, 4, 3, 2, 1, 0};
                        gradeTypes = new String[]{"A", "B", "C", "D", "E", "F"};
                        for (int j = 0; j < gradeTypes.length; j++) {
                            if (grade.equals(gradeTypes[j])) {
                                totalPointsForSecondSemester += Integer.parseInt(unit) * pointsArray[j];
                                totalUnitsForSecondSemester += Integer.parseInt(unit);
                                break;

                            } else {
                                continue;
                            }

                        }
                        break;
                    case "7":
                        pointsArray = new int[]{7, 6, 5, 4, 3, 2, 1, 0};
                        gradeTypes = new String[]{"7 pts", "6 pts", "5 pts", "4 pts", "3 pts", "2 pts", "1 pt", "0 pt"};
                        for (int j = 0; j < gradeTypes.length; j++) {
                            if (grade.equals(gradeTypes[j])) {
                                totalPointsForSecondSemester += Integer.parseInt(unit) * pointsArray[j];
                                totalUnitsForSecondSemester += Integer.parseInt(unit);
                                break;

                            } else {
                                continue;
                            }
                        }
                        break;
                }

            }
            gradePoint = totalPointsForSecondSemester / totalUnitsForSecondSemester;
            if (gpSystem.equals("7")) {
                secondSemesterDetails.append(String.format("\n%s\t%.1f", "Second Semester GPA:", gradePoint));
            } else {
                secondSemesterDetails.append(String.format("\n%s\t%.2f", "Second Semester GPA:", gradePoint));
            }
        } else {
            secondSemesterDetails.append("\n\t\tNo Grades for this semester yet.");
        }

        //now that we got those in line, lets get the level grade point average using the totalpoint and total units of both semester
        double levelGradePointAverage = (totalPointsForFirstSemester + totalPointsForSecondSemester) / (totalUnitsForFirstSemester + totalUnitsForSecondSemester);
        String levelGPA = "";
        String classOfGrade = "";
        //lets do the class of grade here also
        switch (gpSystem) {
            case "7":
                levelGPA = String.format("\n%s\t%.1f", "Grade Point Average for this level is:", levelGradePointAverage);

                if (Double.parseDouble(String.format("%.1f", levelGradePointAverage)) >= 6.0)
                    classOfGrade = "First Class. Congratulations!!!";
                else if (Double.parseDouble(String.format("%.1f", levelGradePointAverage)) >= 4.6 && Double.parseDouble(String.format("%.2f", levelGradePointAverage)) <= 5.9)
                    classOfGrade = "Second Class (Upper Division).";
                else if (Double.parseDouble(String.format("%.1f", levelGradePointAverage)) >= 2.6 && Double.parseDouble(String.format("%.2f", levelGradePointAverage)) <= 4.5)
                    classOfGrade = "Second Class (Lower Division).";
                else if (Double.parseDouble(String.format("%.1f", levelGradePointAverage)) >= 1.6 && Double.parseDouble(String.format("%.2f", levelGradePointAverage)) <= 2.5)
                    classOfGrade = "Third Class.";
                else
                    classOfGrade = "Pass.";
                break;
            case "5":
                levelGPA = String.format("\n%s\t%.2f", "Grade Point Average for this level is:", levelGradePointAverage);
                if (Double.parseDouble(String.format("%.2f", levelGradePointAverage)) >= 4.50)
                    classOfGrade = "First Class. Congratulations!!!";
                else if (Double.parseDouble(String.format("%.2f", levelGradePointAverage)) >= 3.50 && Double.parseDouble(String.format("%.2f", levelGradePointAverage)) <= 4.49)
                    classOfGrade = "Second Class (Upper Division).";
                else if (Double.parseDouble(String.format("%.2f", levelGradePointAverage)) >= 2.40 && Double.parseDouble(String.format("%.2f", levelGradePointAverage)) <= 3.49)
                    classOfGrade = "Second Class (Lower Division).";
                else if (Double.parseDouble(String.format("%.2f", levelGradePointAverage)) >= 1.50 && Double.parseDouble(String.format("%.2f", levelGradePointAverage)) <= 2.39)
                    classOfGrade = "Third Class.";
                else if (Double.parseDouble(String.format("%.2f", levelGradePointAverage)) >= 1.00 && Double.parseDouble(String.format("%.2f", levelGradePointAverage)) <= 1.49)
                    classOfGrade = "Pass.";
                else
                    classOfGrade = "Fail.";
                break;
            case "4":
                levelGPA = String.format("\n%s\t%.2f", "Grade Point Average for this level is:", levelGradePointAverage);
                classOfGrade = "Learn to calculate this on time please";

                break;

        }

        StringBuilder mailBody = new StringBuilder();
        mailBody.append(heading);
        mailBody.append("\n" + firstSemesterDetails);
        mailBody.append("\n" + secondSemesterDetails);
        mailBody.append("\n" + levelGPA);
        mailBody.append("\n\n" + classOfGrade);
        String mailSubject = "My " + levelChoosen + " result";


        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipientMail});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, mailSubject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, mailBody.toString());
        emailIntent.setType("message/rfc822");
        startActivity(Intent.createChooser(emailIntent,
                "Choose an Email Provider"));


    }

    private void displayGradesFor(String levelOfInterest) {

        //start the gradeviewer activity here bro...no time
        Intent intent = new Intent(DashboardActivity.this, GradeViewer.class);
        String details[] = {matricno, levelOfInterest};
        intent.putExtra(Intent.EXTRA_TEXT, details);
        startActivity(intent);


    }

    private void displayMessage() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Exit");
        builder.setMessage("Do you really want to exit?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                if(SignInScreen.control != null) {
                    SignInScreen.control.finish();
                }
                if(RegistrationScreen.registrationScreen != null) {
                    RegistrationScreen.registrationScreen.finish();
                }
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog alert11 = builder.create(); // very important
        alert11.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        //we now have a navigation drawer
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    private void pictureDialog() {

        CharSequence items[] = {"Take a picture", "Pick from Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Profile picture");

        builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        dialog.dismiss();
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {//to be sure there is a camera app on the phone
                            // Create the File where the photo should go
                            File photoFile = null;
                            try {
                                photoFile = createImageFile();
                            } catch (IOException ex) {
                                // Error occurred while creating the File
                                Toast.makeText(getApplicationContext(), "An IOException occurred", Toast.LENGTH_SHORT).show();
                            }
                            // Continue only if the File was successfully created
                            if (photoFile != null) {
                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                        Uri.fromFile(photoFile));
                                galleryAddPic();
                                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                            }
                        }
                        break;
                    case 1:
                        dialog.dismiss();
                        Intent fromGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(fromGalleryIntent, REQUEST_LOAD_IMAGE);
                        break;


                }

            }
        });
        builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();

    }

    private void setPic(String currentPicturePath) {
        // Get the dimensions of the View
        int targetW = 100;  //we use the raw width here as profileImage.getWidth() does not return the target width when this method is called from the onCreate method;
        int targetH = 100;

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(currentPicturePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        // Determine how much to scale down the image
        int inSampleSize = 1;
        if (photoH > targetH || photoW > targetW) {

            final int halfHeight = photoH / 2;
            final int halfWidth = photoW / 2;
            while ((halfHeight / inSampleSize) > targetH
                    && (halfWidth / inSampleSize) > targetW) {
                inSampleSize *= 2;
            }
        }
        bmOptions.inSampleSize = inSampleSize;

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPicturePath, bmOptions);
        if (bitmap == null) {
            Toast.makeText(getApplicationContext(), "Please select a profile picture.", Toast.LENGTH_SHORT).show();
        } else {
            profileImage.setImageBitmap(bitmap);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        currentPicturePath = image.getAbsolutePath();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(matricno, currentPicturePath);
        editor.commit();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPicturePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
}

