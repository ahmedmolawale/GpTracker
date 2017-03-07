package com.ahmedmolawale.gradepointmanager;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import model.DbInfo;

/**
 * Created by MOlawale on 8/10/2015.
 */
public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private String[] possibleLevels = null;
    private ListPreference listPreferenceForLevel;
    private CheckBoxPreference checkBoxPreferenceForCGPA;
    private Preference deleteAllGradesPreference;
    private Preference deleteAccountPreference;
    private Preference passwordPreference;
    private Preference emailPreference;
    private ContentResolver contentResolver;
    private String matric;
    private String level = "";
    private String password;
    private String email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);


        contentResolver = getContentResolver();
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {

            matric = intent.getStringExtra(Intent.EXTRA_TEXT);

        }
        String projection[] = {DbInfo.PASSWORD, DbInfo.CURRENT_LEVEL, DbInfo.EMAIL};
        String selection = DbInfo.MATRIC_NO + "=?";
        String selectionArgs[] = {matric};

        Cursor cursor = contentResolver.query(GpmContentProvider.CONTENT_URI_USERS, projection, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            level = cursor.getString(cursor.getColumnIndex(DbInfo.CURRENT_LEVEL));
            password = cursor.getString(cursor.getColumnIndex(DbInfo.PASSWORD));
            email = cursor.getString(cursor.getColumnIndex(DbInfo.EMAIL));
        }


        listPreferenceForLevel = (ListPreference) findPreference(getString(R.string.pref_level_key));
        checkBoxPreferenceForCGPA = (CheckBoxPreference) findPreference(getString(R.string.pref_checkbox1_key));
        passwordPreference = findPreference(getString(R.string.pref_password_key));
        emailPreference = findPreference(getString(R.string.pref_email_key));
        deleteAllGradesPreference = findPreference(getString(R.string.pref_delete_grades_key));
        deleteAccountPreference = findPreference(getString(R.string.pref_delete_user_key));

        deleteAllGradesPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                confirmDialog1("Delete", "You sure you want to delete all grades?");
                return true;

            }


        });
        deleteAccountPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                confirmDialog2("Delete", "You sure you want your account?");
                return true;

            }


        });
        passwordPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                inflateForOld("Change Password From...", "Password", "PasswordType");
                return true;
            }
        });
        emailPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                inflateForOld("Change Email From...", "Email", "EmailType");
                return true;
            }
        });

        listPreferenceForLevel.setSummary("Current level is: " + level);
        listPreferenceForLevel.setOnPreferenceChangeListener(this);
        listPreferenceForLevel.setOnPreferenceClickListener(this);
        onPreferenceClick(listPreferenceForLevel);


        checkBoxPreferenceForCGPA.setOnPreferenceChangeListener(this);
        onPreferenceChange(checkBoxPreferenceForCGPA,
                PreferenceManager
                        .getDefaultSharedPreferences(checkBoxPreferenceForCGPA.getContext())
                        .getBoolean(checkBoxPreferenceForCGPA.getKey(), false));


    }

    private void inflateForOld(String title, String hint, final String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
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
        if (type.equals("PasswordType")) {
            input.setRawInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        } else {
            input.setRawInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        }
        input.setHint(hint);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Continue...", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String old = input.getText().toString();
                if (type.equals("PasswordType")) {
                    if (old.equals("")) {
                        Toast.makeText(getApplicationContext(), "Old Password cannot be empty.", Toast.LENGTH_SHORT).show();
                    } else if (old.equals(password)) {
                        dialog.cancel();
                        inflateForNew("Change Password to...", "New Password", "PasswordType");

                    } else {
                        Toast.makeText(getApplicationContext(), "Incorrect Password.", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    if (old.equals("")) {
                        Toast.makeText(getApplicationContext(), "Old Email cannot be empty.", Toast.LENGTH_SHORT).show();
                    } else if (old.equals(email)) {
                        dialog.cancel();
                        inflateForNew("Change Email to...", "New Valid Email", "EmailType");

                    } else {
                        Toast.makeText(getApplicationContext(), "Incorrect Email.", Toast.LENGTH_SHORT).show();
                    }


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

    private void inflateForNew(String title, String hint, final String type) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
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
        if (type.equals("PasswordType")) {
            input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        } else {
            input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        }
        input.setHint(hint);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String newOne = input.getText().toString();
                if (type.equals("PasswordType")) {
                    if (newOne.equals("")) {
                        Toast.makeText(getApplicationContext(), "New Password cannot be empty.", Toast.LENGTH_SHORT).show();
                    } else if (newOne.length() < 6) {
                        Toast.makeText(getApplicationContext(), "New Password cannot be less than six characters.", Toast.LENGTH_SHORT).show();
                    } else {
                        String selection = DbInfo.MATRIC_NO + " =?";
                        String selectionArgs[] = {matric};
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(DbInfo.PASSWORD, newOne);
                        int updated = contentResolver.update(GpmContentProvider.CONTENT_URI_USERS, contentValues, selection, selectionArgs);
                        if (updated > 0) {
                            Toast.makeText(getApplicationContext(), "Password updated Successfully.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Password update not Successfully.", Toast.LENGTH_SHORT).show();
                        }
                    }

                } else {
                    if (newOne.equals("")) {
                        Toast.makeText(getApplicationContext(), "New Email cannot be empty.", Toast.LENGTH_SHORT).show();
                    } else {
                        String selection = DbInfo.MATRIC_NO + " =?";
                        String selectionArgs[] = {matric};
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(DbInfo.EMAIL, newOne);
                        int updated = contentResolver.update(GpmContentProvider.CONTENT_URI_USERS, contentValues, selection, selectionArgs);
                        if (updated > 0) {
                            Toast.makeText(getApplicationContext(), "Email updated Successfully.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Email update not Successfully.", Toast.LENGTH_SHORT).show();
                        }
                    }


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

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String stringValue = newValue.toString();
        if (preference instanceof ListPreference) {
            if (stringValue.equals("Highest Level")) {

                Toast.makeText(getApplicationContext(), "You are at the highest level already.", Toast.LENGTH_SHORT).show();
            } else {

                confirmationMessage(preference, stringValue);

            }
            return true;
        } else if (preference instanceof CheckBoxPreference) {

            if (stringValue.equals("true")) {

                preference.setSummary("Disabled");

            } else {
                preference.setSummary("Enabled");
            }
            return true;
        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference instanceof ListPreference) {

            String projection[] = {DbInfo.CURRENT_LEVEL};
            String selection = DbInfo.MATRIC_NO + "=?";
            String selectionArgs[] = {matric};


            Cursor cursor = contentResolver.query(GpmContentProvider.CONTENT_URI_USERS, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                level = cursor.getString(cursor.getColumnIndex(DbInfo.CURRENT_LEVEL));
            }
            switch (level) {
                case "100 LEVEL":
                    possibleLevels = new String[]{"200 LEVEL", "300 LEVEL", "400 LEVEL", "500 LEVEL", "600 LEVEL"};
                    break;
                case "200 LEVEL":
                    possibleLevels = new String[]{"300 LEVEL", "400 LEVEL", "500 LEVEL", "600 LEVEL"};
                    break;
                case "300 LEVEL":
                    possibleLevels = new String[]{"400 LEVEL", "500 LEVEL", "600 LEVEL"};
                    break;
                case "400 LEVEL":
                    possibleLevels = new String[]{"500 LEVEL", "600 LEVEL"};
                    break;
                case "500 LEVEL":
                    possibleLevels = new String[]{"600 LEVEL"};
                    break;
                case "600 LEVEL":
                    possibleLevels = new String[]{"Highest Level"};
                    break;

            }

            ((ListPreference) preference).setEntryValues(possibleLevels);
            ((ListPreference) preference).setEntries(possibleLevels);

            return true;
        }
        return true;
    }

    private void confirmationMessage(final Preference preference, final String stringValue) {


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation");
        builder.setMessage("You sure your new level is " + stringValue + " ?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                preference.setSummary("Current level is: " + stringValue);
                //update the user table now
                ContentValues contentValues = new ContentValues();
                contentValues.put(DbInfo.CURRENT_LEVEL, stringValue);
                String selection = DbInfo.MATRIC_NO + " =?";
                String selectionArgs[] = {matric};
                int row = contentResolver.update(GpmContentProvider.CONTENT_URI_USERS, contentValues, selection, selectionArgs);
                if (row > 0) {
                    Toast.makeText(getApplicationContext(), "Level updated successfully.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Geezz!! Level could not be updated", Toast.LENGTH_SHORT).show();
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

    private void confirmDialog1(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selection = DbInfo.MATRIC_NO + " =?";
                String[] selectionArgs = {matric};
                int deletedRows = contentResolver.delete(GpmContentProvider.CONTENT_URI_GRADES, selection, selectionArgs);
                if (deletedRows > 0) {
                    Toast.makeText(getApplicationContext(), deletedRows + " grades deleted.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "No grades deleted.", Toast.LENGTH_SHORT).show();
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

    private void confirmDialog2(String title, String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selection = DbInfo.MATRIC_NO + " =?";
                String[] selectionArgs = {matric};
                contentResolver.delete(GpmContentProvider.CONTENT_URI_GRADES, selection, selectionArgs);
                contentResolver.delete(GpmContentProvider.CONTENT_URI_USERS, selection, selectionArgs);
                finish();
                DashboardActivity.dashboardControl.finish();
                //start the main activity
                startActivity(new Intent(SettingsActivity.this, SignInScreen.class));


            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();


    }
}
