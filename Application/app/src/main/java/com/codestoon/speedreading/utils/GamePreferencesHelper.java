package com.codestoon.speedreading.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.codestoon.speedreading.models.GameModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GamePreferencesHelper {
    private static final String PREF_NAME = "GamePrefs";
    private static final String KEY_PREFIX = "game_history_";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void saveGameHistory(Context context, String gameId, List<GameModel> history) {
        Gson gson = new Gson();
        String json = gson.toJson(history);
        getPrefs(context).edit().putString(KEY_PREFIX + gameId, json).apply();
    }

    public static List<GameModel> loadGameHistory(Context context, String gameId) {
        String json = getPrefs(context).getString(KEY_PREFIX + gameId, "");
        if (json.isEmpty()) {
            return new ArrayList<>();
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<GameModel>>(){}.getType();
        List<GameModel> history = gson.fromJson(json, type);
        return history != null ? history : new ArrayList<>();
    }

    public static void addGameResult(Context context, GameModel result) {
        List<GameModel> history = loadGameHistory(context, result.getId());

        // فقط 30 نتیجه آخر را نگه دار
        while (history.size() >= 30) {
            history.remove(0);
        }

        history.add(result);
        saveGameHistory(context, result.getId(), history);
    }

    public static void clearGameHistory(Context context, String gameId) {
        getPrefs(context).edit().remove(KEY_PREFIX + gameId).apply();
    }

    // حذف همه تاریخچه‌های همه بازی‌ها
    public static void clearAllGameHistory(Context context) {
        SharedPreferences prefs = getPrefs(context);
        SharedPreferences.Editor editor = prefs.edit();
        // با توجه به اینکه نمی‌دانیم چه کلیدهایی وجود دارند، همه را پاک می‌کنیم
        editor.clear();
        editor.apply();
    }
}