package com.codestoon.speedreading.games.commonimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.ImageView;
import android.widget.TextView;

import com.codestoon.speedreading.R;
import com.codestoon.speedreading.games.base.BaseGame;
import com.codestoon.speedreading.models.GameModel;
import com.codestoon.speedreading.utils.GamePreferencesHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class CommonImageGame extends BaseGame {

    private int commonImageIndex = 0;
    private int[] imageSet1 = new int[9];
    private int[] imageSet2 = new int[9];
    private Bitmap combinedBitmap;

    private int[] imageIcons = {
            R.drawable.ic_star,
            R.drawable.ic_heart,
            R.drawable.ic_circle,
            R.drawable.ic_square,
            R.drawable.ic_triangle,
            R.drawable.ic_diamond,
            R.drawable.ic_bolt,
            R.drawable.ic_cloud,
            R.drawable.ic_moon,
            R.drawable.ic_sun,
            R.drawable.ic_leaf,
            R.drawable.ic_drop,
            R.drawable.ic_fire,
            R.drawable.ic_snow,
            R.drawable.ic_rainbow,
            R.drawable.ic_star_filled
    };

    public CommonImageGame(Context context) {
        super(context, "common_image", "پیدا کردن عکس مشترک");
    }

    @Override
    public void startGame(int level, ImageView displayView, TextView instructionView) {
        Random rand = new Random();
        commonImageIndex = rand.nextInt(imageIcons.length);

        combinedBitmap = createCommonImageBitmap();
        displayView.setImageBitmap(combinedBitmap);

        if (instructionView != null) {
            String lang = "fa";
            instructionView.setText("عکسی که در هر دو مجموعه وجود دارد را پیدا کن");
        }
    }

    @Override
    public void showAnswer(ImageView answerView) {
        // نمایش عکس مشترک
        Bitmap answerBitmap = BitmapFactory.decodeResource(context.getResources(), imageIcons[commonImageIndex]);
        if (answerBitmap != null) {
            Bitmap scaledAnswer = Bitmap.createScaledBitmap(answerBitmap, 200, 200, true);
            answerView.setImageBitmap(scaledAnswer);
        }
    }

    @Override
    public boolean checkAnswer() {
        return true;
    }

    @Override
    public String getInstruction(int level, String language) {
        if (language.equals("en")) {
            return "Find the image that appears in both sets (top and bottom)";
        } else {
            return "عکسی که در هر دو مجموعه (بالا و پایین) وجود دارد را پیدا کن";
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

    private Bitmap createCommonImageBitmap() {
        Random rand = new Random();

        // ساخت مجموعه اول
        Set<Integer> usedImages = new HashSet<>();
        usedImages.add(commonImageIndex);
        imageSet1[0] = commonImageIndex;
        for (int i = 1; i < 9; i++) {
            int img;
            do {
                img = rand.nextInt(imageIcons.length);
            } while (usedImages.contains(img));
            usedImages.add(img);
            imageSet1[i] = img;
        }
        shuffleArray(imageSet1);

        // ساخت مجموعه دوم
        usedImages.clear();
        usedImages.add(commonImageIndex);
        imageSet2[0] = commonImageIndex;
        for (int i = 1; i < 9; i++) {
            int img;
            do {
                img = rand.nextInt(imageIcons.length);
            } while (usedImages.contains(img));
            usedImages.add(img);
            imageSet2[i] = img;
        }
        shuffleArray(imageSet2);

        Bitmap topBitmap = createSingleImageSet(imageSet1);
        Bitmap bottomBitmap = createSingleImageSet(imageSet2);

        int combinedWidth = Math.max(topBitmap.getWidth(), bottomBitmap.getWidth());
        int combinedHeight = topBitmap.getHeight() + bottomBitmap.getHeight() + 40;

        Bitmap combined = Bitmap.createBitmap(combinedWidth, combinedHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(combined);
        canvas.drawColor(Color.WHITE);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(20);
        textPaint.setTextAlign(Paint.Align.CENTER);

        String label1 = "مجموعه ۱";
        String label2 = "مجموعه ۲";
        canvas.drawText(label1, combinedWidth / 2, 30, textPaint);
        canvas.drawBitmap(topBitmap, (combinedWidth - topBitmap.getWidth()) / 2, 40, null);

        Paint linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(2);
        canvas.drawLine(0, topBitmap.getHeight() + 50, combinedWidth, topBitmap.getHeight() + 50, linePaint);

        canvas.drawText(label2, combinedWidth / 2, topBitmap.getHeight() + 70, textPaint);
        canvas.drawBitmap(bottomBitmap, (combinedWidth - bottomBitmap.getWidth()) / 2, topBitmap.getHeight() + 80, null);

        return combined;
    }

    private Bitmap createSingleImageSet(int[] imageSet) {
        int cellSize = 80;
        int padding = 10;
        int cols = 3;
        int rows = 3;
        int width = cols * cellSize + padding * 2;
        int height = rows * cellSize + padding * 2;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.LTGRAY);

        for (int i = 0; i < 9; i++) {
            int row = i / cols;
            int col = i % cols;
            int x = padding + col * cellSize;
            int y = padding + row * cellSize;

            canvas.drawRect(x, y, x + cellSize, y + cellSize, bgPaint);

            Bitmap iconBitmap = BitmapFactory.decodeResource(context.getResources(), imageIcons[imageSet[i]]);
            if (iconBitmap != null) {
                Bitmap scaledIcon = Bitmap.createScaledBitmap(iconBitmap, cellSize - 10, cellSize - 10, true);
                canvas.drawBitmap(scaledIcon, x + 5, y + 5, null);
            }
        }

        return bitmap;
    }

    private void shuffleArray(int[] array) {
        Random rand = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }
}