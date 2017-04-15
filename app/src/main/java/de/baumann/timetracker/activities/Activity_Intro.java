package de.baumann.timetracker.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.chyrta.onboarder.OnboarderActivity;
import com.chyrta.onboarder.OnboarderPage;

import java.util.ArrayList;
import java.util.List;

import de.baumann.timetracker.R;


public class Activity_Intro extends OnboarderActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<OnboarderPage> onboarderPages = new ArrayList<>();

        // Create your first page
        OnboarderPage onboarderPage1 = new OnboarderPage(getString(R.string.intro1_title), getString(R.string.intro1_text), R.drawable.ic_launcher_big);
        OnboarderPage onboarderPage2 = new OnboarderPage(getString(R.string.intro2_title), getString(R.string.intro2_text), R.drawable.s_1);
        OnboarderPage onboarderPage3 = new OnboarderPage(getString(R.string.intro3_title), getString(R.string.intro3_text), R.drawable.s_2);
        OnboarderPage onboarderPage4 = new OnboarderPage(getString(R.string.intro4_title), getString(R.string.intro4_text), R.drawable.s_3);


        // You can define title and description colors (by default white)
        onboarderPage1.setTitleColor(R.color.colorAccent);
        onboarderPage1.setBackgroundColor(R.color.colorPrimaryDark);
        onboarderPage2.setTitleColor(R.color.colorAccent);
        onboarderPage2.setBackgroundColor(R.color.colorPrimaryDark);
        onboarderPage3.setTitleColor(R.color.colorAccent);
        onboarderPage3.setBackgroundColor(R.color.colorPrimaryDark);
        onboarderPage4.setTitleColor(R.color.colorAccent);
        onboarderPage4.setBackgroundColor(R.color.colorPrimaryDark);

        // Add your pages to the list
        onboarderPages.add(onboarderPage1);
        onboarderPages.add(onboarderPage2);
        onboarderPages.add(onboarderPage3);
        onboarderPages.add(onboarderPage4);

        // And pass your pages to 'setOnboardPagesReady' method
        setActiveIndicatorColor(android.R.color.white);
        setInactiveIndicatorColor(android.R.color.darker_gray);
        shouldDarkenButtonsLayout(true);
        setSkipButtonTitle(getString(R.string.intro_skip));
        setFinishButtonTitle(getString(R.string.intro_finish));
        setOnboardPagesReady(onboarderPages);
    }

    @Override
    public void onSkipButtonPressed() {
        // Optional: by default it skips onboarder to the end
        super.onSkipButtonPressed();
        // Define your actions when the user press 'Skip' button
        finish();
    }

    @Override
    public void onFinishButtonPressed() {
        // Define your actions when the user press 'Finish' button
        PreferenceManager.setDefaultValues(this, R.xml.user_settings, false);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.edit().putBoolean("introShowDo_notShow", false).apply();
        finish();
    }
}
