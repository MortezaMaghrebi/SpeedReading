package com.codestoon.speedreading.games.numbers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.ImageView;
import android.widget.TextView;

import com.codestoon.speedreading.games.base.BaseGame;
import com.codestoon.speedreading.models.GameModel;
import com.codestoon.speedreading.utils.GamePreferencesHelper;

import java.util.ArrayList;
import java.util.List;

public class NumbersGame extends BaseGame {

    private Bitmap numberBitmap;
    private int gridSize = 7;

    public NumbersGame(Context context) {
        super(context, "numbers", "پیدا کردن اعداد");
    }

    @Override
    public void startGame(int level, ImageView displayView, TextView instructionView) {
        gridSize = Math.min(5 + level, 10);
        numberBitmap = createNumberGridBitmap(gridSize);
        displayView.setImageBitmap(numberBitmap);
    }

    @Override
    public void showAnswer(ImageView answerView) {
        // برای بازی اعداد جواب خاصی وجود ندارد
        answerView.setImageBitmap(null);
        answerView.setVisibility(android.view.View.GONE);
    }

    @Override
    public boolean checkAnswer() {
        return true;
    }

    @Override
    public String getInstruction(int level, String language) {
        int total = gridSize * gridSize;
        if (language.equals("en")) {
            return "Find numbers 1 to " + total + " in order in your mind";
        } else {
            return "اعداد ۱ تا " + total + " را به ترتیب در ذهن پیدا کن";
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

    private Bitmap createNumberGridBitmap(int size) {
        int totalNumbers = size * size;
        int cellSize = 60;
        int padding = 10;
        int width = size * cellSize + padding * 2;
        int height = size * cellSize + padding * 2;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= totalNumbers; i++) {
            numbers.add(i);
        }
        java.util.Collections.shuffle(numbers);

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(24);
        paint.setTextAlign(Paint.Align.CENTER);

        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.GRAY);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1);

        int index = 0;
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int x = padding + col * cellSize + cellSize / 2;
                int y = padding + row * cellSize + cellSize / 2;
                int number = numbers.get(index++);

                int left = padding + col * cellSize;
                int top = padding + row * cellSize;
                int right = left + cellSize;
                int bottom = top + cellSize;
                canvas.drawRect(left, top, right, bottom, borderPaint);
                canvas.drawText(String.valueOf(number), x, y + 8, paint);
            }
        }

        return bitmap;
    }
}