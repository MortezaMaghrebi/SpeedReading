package com.codestoon.speedreading;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.codestoon.speedreading.fragments.ExercisesFragment;
import com.codestoon.speedreading.fragments.GamesFragment;
import com.codestoon.speedreading.fragments.TestsFragment;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tabExercises, tabTests, tabGames;
    private TextView tvLangFa, tvLangEn;
    private TextView tvAppName;

    private ExercisesFragment exercisesFragment;
    private TestsFragment testsFragment;
    private GamesFragment gamesFragment;

    private String currentLanguage = "fa";
    private int currentTab = R.id.tabExercises;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabExercises = findViewById(R.id.tabExercises);
        tabTests = findViewById(R.id.tabTests);
        tabGames = findViewById(R.id.tabGames);
        tvLangFa = findViewById(R.id.tvLangFa);
        tvLangEn = findViewById(R.id.tvLangEn);
        tvAppName = findViewById(R.id.tvAppName);

        // ایجاد فرگمنت‌ها
        exercisesFragment = new ExercisesFragment();
        testsFragment = new TestsFragment();
        gamesFragment = new GamesFragment();

        setupTabs();
        setupLanguageToggle();

        // تنظیم زبان و تب پیش‌فرض
        setLanguage("fa");
        switchTab(R.id.tabExercises);
    }

    private void setupTabs() {
        tabExercises.setOnClickListener(v -> {
            currentTab = R.id.tabExercises;
            switchTab(v.getId());
        });

        tabTests.setOnClickListener(v -> {
            currentTab = R.id.tabTests;
            switchTab(v.getId());
        });

        tabGames.setOnClickListener(v -> {
            currentTab = R.id.tabGames;
            switchTab(v.getId());
        });
    }

    private void switchTab(int tabId) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        // مخفی کردن همه فرگمنت‌ها
        if (exercisesFragment != null && exercisesFragment.isAdded()) {
            transaction.hide(exercisesFragment);
        }
        if (testsFragment != null && testsFragment.isAdded()) {
            transaction.hide(testsFragment);
        }
        if (gamesFragment != null && gamesFragment.isAdded()) {
            transaction.hide(gamesFragment);
        }

        // ریست کردن همه تب‌ها
        resetTabs();

        if (tabId == R.id.tabExercises) {
            if (!exercisesFragment.isAdded()) {
                transaction.add(R.id.fragmentContainer, exercisesFragment, "ExercisesFragment");
            }
            transaction.show(exercisesFragment);

            tabExercises.setBackgroundResource(R.drawable.bg_tab_active);
            tabExercises.setTextColor(getResources().getColor(R.color.text_primary));
            if (currentLanguage.equals("fa")) {
                tabExercises.setText(R.string.tab_exercises);
            } else {
                tabExercises.setText("Exercises");
            }

        } else if (tabId == R.id.tabTests) {
            if (!testsFragment.isAdded()) {
                transaction.add(R.id.fragmentContainer, testsFragment, "TestsFragment");
            }
            transaction.show(testsFragment);

            tabTests.setBackgroundResource(R.drawable.bg_tab_active);
            tabTests.setTextColor(getResources().getColor(R.color.text_primary));
            if (currentLanguage.equals("fa")) {
                tabTests.setText(R.string.tab_tests);
            } else {
                tabTests.setText("Tests");
            }

        } else if (tabId == R.id.tabGames) {
            if (!gamesFragment.isAdded()) {
                transaction.add(R.id.fragmentContainer, gamesFragment, "GamesFragment");
            }
            transaction.show(gamesFragment);

            tabGames.setBackgroundResource(R.drawable.bg_tab_active);
            tabGames.setTextColor(getResources().getColor(R.color.text_primary));
            if (currentLanguage.equals("fa")) {
                tabGames.setText("بازی‌ها");
            } else {
                tabGames.setText("Games");
            }
        }

        transaction.commitNow();
    }

    private void resetTabs() {
        // ریست کردن تب Exercises
        tabExercises.setBackgroundResource(android.R.color.transparent);
        tabExercises.setTextColor(getResources().getColor(R.color.text_secondary));

        // ریست کردن تب Tests
        tabTests.setBackgroundResource(android.R.color.transparent);
        tabTests.setTextColor(getResources().getColor(R.color.text_secondary));

        // ریست کردن تب Games
        tabGames.setBackgroundResource(android.R.color.transparent);
        tabGames.setTextColor(getResources().getColor(R.color.text_secondary));
    }

    private void setupLanguageToggle() {
        tvLangFa.setOnClickListener(v -> {
            setLanguage("fa");
            tvLangFa.setBackgroundResource(R.drawable.bg_tab_active);
            tvLangFa.setTextColor(getResources().getColor(R.color.text_primary));
            tvLangEn.setBackgroundResource(android.R.color.transparent);
            tvLangEn.setTextColor(getResources().getColor(R.color.text_secondary));
        });

        tvLangEn.setOnClickListener(v -> {
            setLanguage("en");
            tvLangEn.setBackgroundResource(R.drawable.bg_tab_active);
            tvLangEn.setTextColor(getResources().getColor(R.color.text_primary));
            tvLangFa.setBackgroundResource(android.R.color.transparent);
            tvLangFa.setTextColor(getResources().getColor(R.color.text_secondary));
        });
    }

    private void setLanguage(String lang) {
        currentLanguage = lang;

        // تغییر Locale
        Locale locale;
        if ("en".equals(lang)) {
            locale = new Locale("en");
        } else {
            locale = new Locale("fa");
        }
        Locale.setDefault(locale);

        // تغییر جهت layout
        int layoutDirection;
        if ("fa".equals(lang)) {
            layoutDirection = View.LAYOUT_DIRECTION_RTL;
        } else {
            layoutDirection = View.LAYOUT_DIRECTION_LTR;
        }

        // اعمال جهت به Activity
        getWindow().getDecorView().setLayoutDirection(layoutDirection);

        // به‌روز کردن نام برنامه
        if ("en".equals(lang)) {
            tvAppName.setText(R.string.app_name_en);
        } else {
            tvAppName.setText(R.string.app_name);
        }

        // به‌روز کردن تب‌ها
        if ("en".equals(lang)) {
            tabExercises.setText("Exercises");
            tabTests.setText("Tests");
            tabGames.setText("Games");
        } else {
            tabExercises.setText(R.string.tab_exercises);
            tabTests.setText(R.string.tab_tests);
            tabGames.setText(R.string.tab_games);
        }

        // به‌روز کردن فرگمنت‌ها
        if (exercisesFragment != null && exercisesFragment.isAdded()) {
            exercisesFragment.onLanguageChanged(lang);
        }
        if (testsFragment != null && testsFragment.isAdded()) {
            testsFragment.onLanguageChanged(lang);
        }
        if (gamesFragment != null && gamesFragment.isAdded()) {
            gamesFragment.onLanguageChanged(lang);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // در صورت تغییر configuration، جهت را دوباره اعمال کن
        int layoutDirection = "fa".equals(currentLanguage) ?
                View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR;
        getWindow().getDecorView().setLayoutDirection(layoutDirection);
    }
}