package com.ahmedmolawale.gradepointmanager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * Created by MOlawale on 8/11/2015.
 */
public class AboutActivity extends AppCompatActivity {


    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_header);
        toolbar = (Toolbar) findViewById(R.id.toolbar_about);
        setSupportActionBar(toolbar);



    }
}


