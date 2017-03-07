package com.ahmedmolawale.gradepointmanager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;


public class HelpActivity extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_header);
        toolbar = (Toolbar) findViewById(R.id.toolbar_help);
        setSupportActionBar(toolbar);

    }

}
