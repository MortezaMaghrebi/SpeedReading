package com.codestoon.speedreading.games.wordcount;

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

import java.util.List;
import java.util.Random;

public class WordCountGame extends BaseGame {

    private String targetWord = "";
    private String fullText = "";
    private int targetCount = 0;
    private String[] words = {
            "کتاب", "خواندن", "تندخوانی", "تمرین", "مطالعه", "یادگیری",
            "سرعت", "درک", "مطلب", "چشم", "حرکت", "تمرکز"
    };

    public WordCountGame(Context context) {
        super(context, "word_count", "شمارش کلمه");
    }

    @Override
    public void startGame(int level, ImageView displayView, TextView instructionView) {
        Random rand = new Random();
        targetWord = words[rand.nextInt(words.length)];
        targetCount = 0;

        StringBuilder textBuilder = new StringBuilder();
        for (int i = 0; i < 80 + level * 10; i++) {
            String word = words[rand.nextInt(words.length)];
            textBuilder.append(word).append(" ");
            if (word.equals(targetWord)) {
                targetCount++;
            }
        }
        int extraRepeats = 3 + level * 2;
        for (int i = 0; i < extraRepeats; i++) {
            textBuilder.append(targetWord).append(" ");
            targetCount++;
        }
        fullText = textBuilder.toString();

        Bitmap textBitmap = createTextBitmap(fullText);
        displayView.setImageBitmap(textBitmap);

        if (instructionView != null) {
            String lang = "fa";
            instructionView.setText("تعداد تکرار کلمه '" + targetWord + "' را در متن پیدا کن");
        }
    }

    @Override
    public void showAnswer(ImageView answerView) {
        // نمایش تعداد تکرار
        String answer = "تعداد تکرار کلمه '" + targetWord + "': " + targetCount;
        // برای سادگی، به صورت متن نمایش می‌دهیم
        // در آینده می‌توان به صورت تصویر نمایش داد
    }

    @Override
    public boolean checkAnswer() {
        return true;
    }

    @Override
    public String getInstruction(int level, String language) {
        if (language.equals("en")) {
            return "Count how many times the target word appears in the text";
        } else {
            return "تعداد تکرار کلمه هدف را در متن پیدا کن";
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

    private Bitmap createTextBitmap(String text) {
        int width = 800;
        int height = 500;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(18);
        paint.setAntiAlias(true);

        String[] lines = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        int y = 50;
        int maxWidth = width - 40;

        for (String word : lines) {
            String testLine = currentLine.toString() + word + " ";
            float textWidth = paint.measureText(testLine);
            if (textWidth > maxWidth) {
                canvas.drawText(currentLine.toString(), 20, y, paint);
                currentLine = new StringBuilder(word + " ");
                y += 30;
            } else {
                currentLine.append(word).append(" ");
            }
        }
        if (currentLine.length() > 0) {
            canvas.drawText(currentLine.toString(), 20, y, paint);
        }

        return bitmap;
    }
}