package com.ahmedmolawale.gradepointmanager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * Created by MOlawale on 8/11/2015.
 */
public class FeedbackActivity extends AppCompatActivity {


    private EditText username, email, message;
    private Spinner feedBackTypeSpinner;
    private CheckBox feedBackCheckBox;
    private Button sendFeedBack;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feedback_header);
        toolbar = (Toolbar) findViewById(R.id.toolbar_feedback);
        setSupportActionBar(toolbar);


        username = (EditText) findViewById(R.id.edit_text_name);
        email = (EditText) findViewById(R.id.edit_text_email);
        message = (EditText) findViewById(R.id.edit_text_feedbackbody);
        feedBackTypeSpinner = (Spinner) findViewById(R.id.spinner_feedback);
        feedBackCheckBox = (CheckBox) findViewById(R.id.checkbox_response);
        sendFeedBack = (Button) findViewById(R.id.button_send_feedback);
        String[] spinnerItems = getResources().getStringArray(
                R.array.feedbacktypelist);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getApplicationContext(), R.layout.spinner_item, spinnerItems);

        feedBackTypeSpinner.setAdapter(adapter);

        sendFeedBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (username.getText().toString().toString().equals("")) {
                    username.setError("Field Required");
                } else if (email.getText().toString().equals("")) {
                    email.setError("Field Required");

                } else if (message.getText().toString().equals("")) {
                    message.setError("Field Required");
                } else {
                    collectAndSendData();
                }
            }
        });

    }

    public void collectAndSendData() {

        String name = username.getText().toString();
        String mail = email.getText().toString();
        String bodyOfMessage = message.getText().toString()
                + "\n Sender Mail: " + mail + "\n Sender Name: " + name;
        String feedBackType = feedBackTypeSpinner.getSelectedItem().toString();
        boolean needResponse = feedBackCheckBox.isChecked();
        if (needResponse) {
            feedBackType = feedBackType + " - Response Needed.";
        }
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"ahmedmolawale@gmail.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, feedBackType);
        emailIntent.putExtra(Intent.EXTRA_TEXT, bodyOfMessage);
        emailIntent.setType("message/rfc822");
        startActivity(Intent.createChooser(emailIntent,
                "Choose an Email Provider"));

    }

}
