package com.ahmedmolawale.gradepointmanager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;


public class RecommendedApp extends AppCompatActivity {

    Button recommend;
    private Toolbar toolbar;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recommended_app_header);
        toolbar = (Toolbar) findViewById(R.id.toolbar_recommend);
        setSupportActionBar(toolbar);

        recommend = (Button) findViewById(R.id.recommend);

        recommend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://play.google.com/store/apps/details?id=com.ahmedmolawale.timetablemanager");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);

            }
        });

    }


}
