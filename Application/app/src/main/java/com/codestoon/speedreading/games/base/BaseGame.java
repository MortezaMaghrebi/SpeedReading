package com.codestoon.speedreading.games.base;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.codestoon.speedreading.models.GameModel;

import java.util.List;

public abstract class BaseGame {

    protected String gameId;
    protected String gameName;
    protected Context context;

    public BaseGame(Context context, String gameId, String gameName) {
        this.context = context;
        this.gameId = gameId;
        this.gameName = gameName;
    }

    public String getGameId() {
        return gameId;
    }

    public String getGameName() {
        return gameName;
    }

    // هر بازی باید این متدها را پیاده‌سازی کند
    public abstract void startGame(int level, ImageView displayView, TextView instructionView);
    public abstract void showAnswer(ImageView answerView);
    public abstract boolean checkAnswer();
    public abstract String getInstruction(int level, String language);
    public abstract int getMaxLevel();
    public abstract String getLevelName(int level, String language);
    public abstract GameModel createGameResult(int level, double time);

    // متد برای ذخیره نتیجه
    public void saveResult(Context context, GameModel result) {
        // در کلاس‌های فرزند می‌توانند override کنند
    }

    // متد برای بارگذاری تاریخچه
    public List<GameModel> loadHistory(Context context) {
        return new java.util.ArrayList<>();
    }
}