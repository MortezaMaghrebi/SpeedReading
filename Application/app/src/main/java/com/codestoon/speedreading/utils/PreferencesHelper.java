package com.codestoon.speedreading.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.codestoon.speedreading.models.TestResultModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PreferencesHelper {
    private static final String PREF_NAME = "SpeedReadingPrefs";
    private static final String KEY_TEST_HISTORY = "test_history";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void saveTestHistory(Context context, List<TestResultModel> history) {
        Gson gson = new Gson();
        String json = gson.toJson(history);
        getPrefs(context).edit().putString(KEY_TEST_HISTORY, json).apply();
    }

    public static List<TestResultModel> loadTestHistory(Context context) {
        String json = getPrefs(context).getString(KEY_TEST_HISTORY, "");
        if (json.isEmpty()) {
            return new ArrayList<>();
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<TestResultModel>>(){}.getType();
        List<TestResultModel> history = gson.fromJson(json, type);
        return history != null ? history : new ArrayList<>();
    }

    public static void addTestResult(Context context, TestResultModel result) {
        List<TestResultModel> history = loadTestHistory(context);
        history.add(result);
        saveTestHistory(context, history);
    }

    public static void clearTestHistory(Context context) {
        getPrefs(context).edit().remove(KEY_TEST_HISTORY).apply();
    }
}