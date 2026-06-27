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
import com.codestoon.speedreading.fragments.TestsFragment;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tabExercises, tabTests;
    private TextView tvLangFa, tvLangEn;
    private TextView tvAppName;

    private ExercisesFragment exercisesFragment;
    private TestsFragment testsFragment;
    private String currentLanguage = "fa";
    private int currentTab = R.id.tabExercises;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabExercises = findViewById(R.id.tabExercises);
        tabTests = findViewById(R.id.tabTests);
        tvLangFa = findViewById(R.id.tvLangFa);
        tvLangEn = findViewById(R.id.tvLangEn);
        tvAppName = findViewById(R.id.tvAppName);

        // ایجاد فرگمنت‌ها
        exercisesFragment = new ExercisesFragment();
        testsFragment = new TestsFragment();

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
    }

    private void switchTab(int tabId) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        // مخفی کردن همه فرگمنت‌ها
        if (exercisesFragment != null) {
            transaction.hide(exercisesFragment);
        }
        if (testsFragment != null) {
            transaction.hide(testsFragment);
        }

        if (tabId == R.id.tabExercises) {
            if (!exercisesFragment.isAdded()) {
                transaction.add(R.id.fragmentContainer, exercisesFragment, "ExercisesFragment");
            }
            transaction.show(exercisesFragment);

            tabExercises.setBackgroundResource(R.drawable.bg_tab_active);
            tabExercises.setTextColor(getResources().getColor(R.color.text_primary));
            tabTests.setBackgroundResource(android.R.color.transparent);
            tabTests.setTextColor(getResources().getColor(R.color.text_secondary));
        } else {
            if (!testsFragment.isAdded()) {
                transaction.add(R.id.fragmentContainer, testsFragment, "TestsFragment");
            }
            transaction.show(testsFragment);

            tabTests.setBackgroundResource(R.drawable.bg_tab_active);
            tabTests.setTextColor(getResources().getColor(R.color.text_primary));
            tabExercises.setBackgroundResource(android.R.color.transparent);
            tabExercises.setTextColor(getResources().getColor(R.color.text_secondary));
        }

        transaction.commitNow();
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
        } else {
            tabExercises.setText(R.string.tab_exercises);
            tabTests.setText(R.string.tab_tests);
        }

        // به‌روز کردن فرگمنت‌ها
        if (exercisesFragment != null) {
            exercisesFragment.onLanguageChanged(lang);
        }
        if (testsFragment != null) {
            testsFragment.onLanguageChanged(lang);
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