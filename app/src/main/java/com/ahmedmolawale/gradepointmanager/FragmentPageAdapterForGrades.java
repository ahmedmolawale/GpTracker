package com.ahmedmolawale.gradepointmanager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by MOlawale on 8/8/2015.
 */
public class FragmentPageAdapterForGrades extends FragmentStatePagerAdapter {


    public FragmentPageAdapterForGrades(FragmentManager fm) {
        super(fm);
    }

    public Fragment getItem(int arg0) {
        // TODO Auto-generated method stub

        switch (arg0) {

            case 0:
                return new FirstSemesterFragment();
            case 1:
                return new SecondSemesterFragment();
            case 2:
                return new AnalysisFragment();

        }
        return null;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return 3;
    }

}

