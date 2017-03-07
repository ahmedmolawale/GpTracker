package com.ahmedmolawale.gradepointmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import model.DbInfo;

/**
 * Created by ahmed on 27/01/2016.
 */
public class SignInScreen extends AppCompatActivity {

    private Button sign_in;
    private Button forgotPassword;
    private EditText matricNo;
    private EditText password;
    public static final String INTENT_OF_LOGIN_WINDOW = "datafromloginwindow";
    private ContentResolver contentResolver;
    private View focusView;
    private Button register;
    private Toolbar toolbar;
    private SharedPreferences sharedPreferences;
    private CheckBox keepMeLoggedIn;
    public static SignInScreen control;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_login_header);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        control = this;
        if(sharedPreferences.getBoolean("keep_me_in",false)) {
            Intent intent = new Intent(SignInScreen.this, DashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
            startActivity(intent);
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar_login);
        setSupportActionBar(toolbar);
        contentResolver = getContentResolver();
        sign_in = (Button) findViewById(R.id.sign_in_button);
        forgotPassword = (Button) findViewById(R.id.forgot_password);
        keepMeLoggedIn = (CheckBox) findViewById(R.id.keep_me_in);
        register = (Button) findViewById(R.id.create_account_button);

        matricNo = (EditText) findViewById(R.id.matric_no);
        password = (EditText) findViewById(R.id.password);

        sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeUserMatric();
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInScreen.this, RegistrationScreen.class);
                startActivity(intent);
            }
        });

    }

    private void attemptLogin() {

        // Reset errors.
        matricNo.setError(null);
        password.setError(null);

        // Store values at the time of the login attempt.
        String matric = matricNo.getText().toString();
        String password = this.password.getText().toString();

        boolean cancel = false;
        focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            this.password.setError(getString(R.string.error_invalid_password));
            focusView = this.password;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(matric)) {
            matricNo.setError(getString(R.string.error_field_required));
            focusView = matricNo;
            cancel = true;
        }


        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

            checkForTheUser(matric, password);
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6 ? true : false;
    }

    private void takeUserMatric() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Matric No.");
        builder.setMessage("Please provide your matric no. We shall send your password to your mail.");
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
        input.setHint("Matric No.");
        builder.setView(input);
        // Set up the buttons
        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //check whether user exit

                String[] projection = {DbInfo.PASSWORD, DbInfo.EMAIL};
                String selection = DbInfo.MATRIC_NO + " =?";
                String[] selectionArgs = {input.getText().toString()};
                Cursor cursor
                        = getContentResolver().query(GpmContentProvider.CONTENT_URI_USERS, projection, selection, selectionArgs, null);
                if (cursor != null && cursor.moveToFirst()) {

                    passwordRetrievalConfirmation(input.getText().toString(),
                            cursor.getString(cursor.getColumnIndex(DbInfo.PASSWORD)),
                            cursor.getString(cursor.getColumnIndex(DbInfo.EMAIL)));
                } else {
                    displayAMessage("User Not Found", "User with Matric No.: " + input.getText().toString() + " not found.\nYou can create an account.");
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

    private void passwordRetrievalConfirmation(final String matricNo, final String password, final String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your password would be sent to your mail\n" + email + "\nNote: An active internet connection is required.");
        builder.setTitle("Password Retrieval");
        builder.setCancelable(true);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                ConnectivityManager conn = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = conn.getActiveNetworkInfo();
                if (netInfo != null && netInfo.isConnected()) {


                    new SendPassword().execute(new String[]{matricNo, password, email});
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Not Connected to the internet", Toast.LENGTH_LONG)
                            .show();
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

    private class SendPassword extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            Toast.makeText(getApplicationContext(), "Please wait...", Toast.LENGTH_SHORT).show();

        }

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                GMailSender sender = new GMailSender("gpamanager@gmail.com", "whatismypassword");
                String message = "Hi, trust you are good. You got this mail because you requested it from your GP Tracker account." +
                        "\nPlease see your log-in details below:\n" +
                        " Matric:   " + params[0] + "\nPassword:    " + params[1] + "\nThank you for using GP Manager.";
                String subject = "GP Manager Account Password";
                sender.sendMail(subject, message, "gpamanager@gmail.com", params[2]);
                return "Success";

            } catch (Exception e) {

                return "Failure";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if (result.equals("Success")) {
                Toast.makeText(getApplicationContext(), "Password sent successfully.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "An error occurred while sending password.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkForTheUser(String matric1, String password1) {
        String[] projection = {DbInfo.NAME, DbInfo.DEPARTMENT, DbInfo.EMAIL};
        String selection = DbInfo.MATRIC_NO + " =? AND " + DbInfo.PASSWORD + " =?";
        String[] selectionArgs = {matric1, password1};

        Cursor cursor = contentResolver.query(GpmContentProvider.CONTENT_URI_USERS, projection, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {

            String name = cursor.getString(cursor.getColumnIndex(DbInfo.NAME));
            String department = cursor.getString(cursor.getColumnIndex(DbInfo.DEPARTMENT));
            String userDetails[] = {name, matric1, department};
            Intent intent = new Intent(SignInScreen.this, DashboardActivity.class);
            intent.putExtra(INTENT_OF_LOGIN_WINDOW, userDetails);
            startActivity(intent);
            sharedPreferences.edit().putBoolean("keep_me_in", keepMeLoggedIn.isChecked()).apply();
            Toast.makeText(getApplicationContext(), "Welcome, " + userDetails[0], Toast.LENGTH_LONG).show();
        } else {

            displayAMessage("Error", "Matric No. and Password does not match. ");
        }
    }

    public void displayAMessage(String type, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setTitle(type);
        builder.setCancelable(true);
        builder.setPositiveButton("Try Again...", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
        focusView = matricNo;
        focusView.requestFocus();
    }
}
