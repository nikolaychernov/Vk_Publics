package com.nikolaychernov.vkpublics;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by Nikolay on 26.11.2016.
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}