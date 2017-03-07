package com.ahmedmolawale.gradepointmanager;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import model.DbInfo;

/**
 * Created by ahmed on 27/01/2016.
 */
public class RegistrationScreen extends AppCompatActivity {

    private Button createAnAccount;
    private EditText name;
    private EditText matricno;
    private EditText password;
    private EditText confirmPassword;
    private EditText department;
    private EditText email;
    private String currentLevel = "100 LEVEL";
    private ContentResolver contentResolver;
    private Toolbar toolbar;
    private View focusView;
    private String gradePointScale = "4";
    public static RegistrationScreen registrationScreen;

    public static final String INTENT_OF_REG_WINDOW = "datafromregistrcationscreen";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_reg_header);
        registrationScreen = this;
        toolbar = (Toolbar) findViewById(R.id.toolbar_reg);
        setSupportActionBar(toolbar);



//        final Activity activity = this;
//        final View content = findViewById(android.R.id.content).getRootView();
//        if (content.getWidth() > 0) {
//            Bitmap image = BlurBuilder.blur(content);
//
//            getWindow().setBackgroundDrawable(new BitmapDrawable(getResources(), image));
//        } else {
//            content.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                @Override
//                public void onGlobalLayout() {
//                    Bitmap image = BlurBuilder.blur(content);
//                    getWindow().setBackgroundDrawable(new BitmapDrawable(getResources(), image));
//                }
//            });
//        }
        contentResolver = getContentResolver();
        name = (EditText) findViewById(R.id.name_new);
        matricno = (EditText) findViewById(R.id.matric_no_new);
        password = (EditText) findViewById(R.id.password_new);
        confirmPassword = (EditText) findViewById(R.id.confirm_password_new);
        department = (EditText) findViewById(R.id.dept_new);
        email = (EditText) findViewById(R.id.email_new);
        email.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.register || id == EditorInfo.IME_NULL) {
                    attemptReg();
                    return true;
                }
                return false;
            }
        });

        createAnAccount = (Button) findViewById(R.id.create_new_button);

        createAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptReg();
            }
        });


    }

    private void attemptReg() {

        // Reset errors.
        name.setError(null);
        matricno.setError(null);
        password.setError(null);
        confirmPassword.setError(null);
        department.setError(null);
        email.setError(null);

        // Store values at the time of the login attempt.
        String name = this.name.getText().toString();
        String matricNo = this.matricno.getText().toString();
        String password = this.password.getText().toString();
        String confirmPassword = this.confirmPassword.getText().toString();
        String dept = department.getText().toString();
        String email = this.email.getText().toString();

        boolean cancel = false;
        if (TextUtils.isEmpty(name)) {
            this.name.setError(getString(R.string.error_field_required));
            focusView = this.name;
            cancel = true;
        }

        if (TextUtils.isEmpty(matricNo)) {
            this.matricno.setError(getString(R.string.error_field_required));
            focusView = this.matricno;
            cancel = true;
        }

        //check for empty email
        if (TextUtils.isEmpty(email)) {
            this.email.setError(getString(R.string.error_field_required));
            focusView = this.email;
            cancel = true;
        }
        // Check for a valid email address.
        else if (!Utility.validateEmail(email)) {
            this.email.setError(getString(R.string.error_invalid_email));
            focusView = this.email;
            cancel = true;
        }
        //check for empty password
        if (TextUtils.isEmpty(password)) {
            this.password.setError(getString(R.string.error_field_required));
            focusView = this.password;
            cancel = true;
        }
        //de-activating things
        // Check for a valid password, if the user entered one.
        else if (password.length() < 6) {
            this.password.setError(getString(R.string.error_invalid_password));
            focusView = this.password;
            cancel = true;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            this.confirmPassword.setError(getString(R.string.error_field_required));
            focusView = this.confirmPassword;
            cancel = true;

        } else if (!confirmPassword.equals(password)) {
            this.confirmPassword.setError(getString(R.string.error_field_match));
            focusView = this.confirmPassword;
            cancel = true;

        }

        if (TextUtils.isEmpty(dept)) {
            this.department.setError(getString(R.string.error_field_required));
            focusView = this.department;
            cancel = true;
        }
        //check
        if (cancel) {
            // There was an error; don't attempt reg and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            if (checkIfUserExist(matricno.getText().toString())) {

                displayMessage("User Exist", "A user with Matric No " + matricno.getText().toString() + " already exist.\nPlease use your Matric No");

            } else {
                displayGradePointScaleChoice();
            }
        }
    }

    public void displayMessage(String type, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setTitle(type);
        builder.setCancelable(true);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    private boolean checkIfUserExist(String matricNo) {

        try {

            String selection = DbInfo.MATRIC_NO + " =?";
            String[] selectionArgs = {matricNo};
            Cursor cursor = contentResolver.query(GpmContentProvider.CONTENT_URI_USERS, null, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {

                return true;

            } else {

                return false;
            }

        } catch (Exception e) {

        }
        return false;

    }

    private void displayLevelOptions() {
        final CharSequence[] level = {"100 LEVEL", "200 LEVEL",
                "300 LEVEL", "400 LEVEL", "500 LEVEL", "600 LEVEL"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Your Current Level");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                prepareDataToSave();

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setSingleChoiceItems(level, 0, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {

                    case 0:
                        currentLevel = "100 LEVEL";
                        break;
                    case 1:
                        currentLevel = "200 LEVEL";
                        break;
                    case 2:
                        currentLevel = "300 LEVEL";
                        break;
                    case 3:
                        currentLevel = "400 LEVEL";
                        break;
                    case 4:
                        currentLevel = "500 LEVEL";
                        break;
                    case 5:
                        currentLevel = "600 LEVEL";
                        break;
                    default:
                        break;

                }

            }
        });
        AlertDialog alert11 = builder.create();
        alert11.show();
    }

    public void displayGradePointScaleChoice() {

        final CharSequence[] scales = {"4 - Point Scale", "5 - Point Scale",
                "7 - Point Scale"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Grade Point System");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                displayLevelOptions();

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setSingleChoiceItems(scales, 0, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {

                    case 0:
                        gradePointScale = "4";
                        break;
                    case 1:
                        gradePointScale = "5";
                        break;
                    case 2:
                        gradePointScale = "7";
                        break;
                }
            }
        }).show();

    }

    private void prepareDataToSave() {

        ContentValues contentValues = new ContentValues();
        contentValues.put(DbInfo.NAME, name.getText().toString());
        contentValues.put(DbInfo.MATRIC_NO, matricno.getText().toString());
        contentValues.put(DbInfo.PASSWORD, password.getText().toString());
        contentValues.put(DbInfo.DEPARTMENT, department.getText().toString());
        contentValues.put(DbInfo.EMAIL, email.getText().toString());
        contentValues.put(DbInfo.GP_SYSTEM, gradePointScale);
        contentValues.put(DbInfo.CURRENT_LEVEL, currentLevel);

        try {

            contentResolver.insert(GpmContentProvider.CONTENT_URI_USERS, contentValues);
            openUpTheDashboard();
        } catch (Exception e) {

        }


    }

    private void openUpTheDashboard() {

        Intent intent = new Intent(RegistrationScreen.this, DashboardActivity.class);
        String userDetails[] = {name.getText().toString(), matricno.getText().toString(), department.getText().toString()};
        intent.putExtra(INTENT_OF_REG_WINDOW, userDetails);
        startActivity(intent);
        Toast.makeText(getApplicationContext(), "Welcome, " + name.getText().toString(), Toast.LENGTH_SHORT).show();

    }

}
