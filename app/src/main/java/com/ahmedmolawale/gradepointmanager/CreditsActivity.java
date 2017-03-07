package com.ahmedmolawale.gradepointmanager;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

/**
 * Created by MOlawale on 8/28/2015.
 */
public class CreditsActivity extends PreferenceActivity {


    Preference developerPreference, iconDesignerPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_credits);

        developerPreference = findPreference(getString(R.string.pref_developer_key));
        iconDesignerPreference = findPreference(getString(R.string.pref_icon_designer_key));

        developerPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"ahmedmolawale@gmail.com"});
                emailIntent.setType("message/rfc822");
                startActivity(Intent.createChooser(emailIntent,
                        "Email the developer..."));


                return true;
            }
        });
        iconDesignerPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"biodunch@gmail.com"});
                emailIntent.setType("message/rfc822");
                startActivity(Intent.createChooser(emailIntent,
                        "Email the icon designer..."));

                return true;
            }
        });
    }
}
