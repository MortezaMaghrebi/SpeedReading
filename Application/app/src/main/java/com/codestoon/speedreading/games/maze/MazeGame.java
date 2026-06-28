package com.codestoon.speedreading.games.maze;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.widget.TextView;

import com.codestoon.speedreading.R;
import com.codestoon.speedreading.games.base.BaseGame;
import com.codestoon.speedreading.models.GameModel;
import com.codestoon.speedreading.utils.GamePreferencesHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

public class MazeGame extends BaseGame {

    private String[] mazeLevels = {"level_1", "level_2", "level_3", "level_4"};
    private Bitmap questionBitmap;
    private Bitmap answerBitmap;
    private int currentGameIndex = 0;

    public MazeGame(Context context) {
        super(context, "maze", "ماز");
    }

    @Override
    public void startGame(int level, ImageView displayView, TextView instructionView) {
        try {
            int levelIndex = Math.min((level - 1) / 3, 3);
            String levelFolder = mazeLevels[levelIndex];

            Random rand = new Random();
            int gameIndex = rand.nextInt(3) + 1;
            currentGameIndex = gameIndex;

            String questionPath = "games/maze/" + levelFolder + "/question/game" + gameIndex + ".jpg";
            InputStream questionStream = context.getAssets().open(questionPath);
            questionBitmap = BitmapFactory.decodeStream(questionStream);
            questionStream.close();
            displayView.setImageBitmap(questionBitmap);

            String answerPath = "games/maze/" + levelFolder + "/answer/game" + gameIndex + ".jpg";
            InputStream answerStream = context.getAssets().open(answerPath);
            answerBitmap = BitmapFactory.decodeStream(answerStream);
            answerStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showAnswer(ImageView answerView) {
        if (answerBitmap != null) {
            answerView.setImageBitmap(answerBitmap);
        }
    }

    @Override
    public boolean checkAnswer() {
        return true;
    }

    @Override
    public String getInstruction(int level, String language) {
        if (language.equals("en")) {
            return "Find the path from start (S) to finish (F) in your mind";
        } else {
            return "مسیر را از شروع (S) تا پایان (F) در ذهن پیدا کن";
        }
    }

    @Override
    public int getMaxLevel() {
        return 10;
    }

    @Override
    public String getLevelName(int level, String language) {
        if (language.equals("en")) {
            return "Level " + level;
        } else {
            return "سطح " + level;
        }
    }

    @Override
    public GameModel createGameResult(int level, double time) {
        return new GameModel(gameId, gameName, level, time);
    }

    @Override
    public void saveResult(Context context, GameModel result) {
        GamePreferencesHelper.addGameResult(context, result);
    }

    @Override
    public List<GameModel> loadHistory(Context context) {
        return GamePreferencesHelper.loadGameHistory(context, gameId);
    }
}