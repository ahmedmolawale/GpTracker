package com.ahmedmolawale.gradepointmanager;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;


public class GradeViewer extends ActionBarActivity implements ActionBar.TabListener {

    ActionBar actionBar;
    ViewPager viewPager;
    FragmentPageAdapterForGrades fragmentPageAdapterForGrades;
    public static Activity fa;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grade_viewer_layout);

        fa = this;
        Intent intent = getIntent();
        if(intent !=null &&intent.hasExtra(Intent.EXTRA_TEXT)){

            String details[]=intent.getStringArrayExtra(Intent.EXTRA_TEXT);
            setTitle(details[1]);
        }

        viewPager = (ViewPager) findViewById(R.id.pagerforgrader);
        fragmentPageAdapterForGrades = new FragmentPageAdapterForGrades(getSupportFragmentManager());

        actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.addTab(actionBar.newTab().setText("First").setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText("Second").setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText("Analysis").setTabListener(this));
        viewPager.setAdapter(fragmentPageAdapterForGrades);
        viewPager.getAdapter().notifyDataSetChanged();
        viewPager.setOffscreenPageLimit(1);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                viewPager.getAdapter().notifyDataSetChanged();
                actionBar.setSelectedNavigationItem(position);

            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                // TODO Auto-generated method stub


            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                // TODO Auto-generated method stub
                viewPager.getAdapter().notifyDataSetChanged();

            }
        });


    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {

        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }
}





