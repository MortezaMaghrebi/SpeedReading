package com.codestoon.speedreading.games.commonimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class CommonImageGenerator {

    private static final int WIDTH = 500;
    private static final int HEIGHT = 750; // نسبت 1:1.5

    private static final String[] IMAGE_FILES = {
            "circle.png",
            "cloud.png",
            "diamond.png",
            "drop.png",
            "dumbbell.png",
            "eye.png",
            "film.png",
            "fire.png",
            "flask.png",
            "heart.png",
            "leaf.png",
            "moon.png",
            "rainbow.png",
            "snow.png",
            "square.png",
            "star.png",
            "sun.png",
            "tachometer.png",
            "triangle.png",
            "whale.png"
    };

    private static Context context;

    public static class CommonImageResult {
        public Bitmap questionImage;
        public Bitmap answerImage;
        public int commonIndex;

        public CommonImageResult(Bitmap questionImage, Bitmap answerImage, int commonIndex) {
            this.questionImage = questionImage;
            this.answerImage = answerImage;
            this.commonIndex = commonIndex;
        }
    }

    public static void init(Context ctx) {
        context = ctx;
    }

    private static Bitmap loadImageFromAssets(String fileName) {
        if (context == null) {
            return null;
        }
        try {
            InputStream inputStream = context.getAssets().open("games/commonimage/" + fileName);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static List<Bitmap> loadAllImages() {
        List<Bitmap> images = new ArrayList<>();
        for (String fileName : IMAGE_FILES) {
            Bitmap bitmap = loadImageFromAssets(fileName);
            if (bitmap != null) {
                images.add(bitmap);
            }
        }
        return images;
    }

    public static Bitmap generateDefaultImage() {
        Bitmap bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint gradientPaint = new Paint();
        android.graphics.LinearGradient gradient = new android.graphics.LinearGradient(
                0, 0, WIDTH, HEIGHT,
                Color.parseColor("#FFF5E6"),
                Color.parseColor("#E6F0FF"),
                android.graphics.Shader.TileMode.CLAMP
        );
        gradientPaint.setShader(gradient);
        canvas.drawRect(0, 0, WIDTH, HEIGHT, gradientPaint);

        // ====== آیکون بازی در مرکز ======
        int iconSize = 110;
        int centerX = WIDTH / 2;
        int centerY = HEIGHT / 2 - 30;

        Paint circleBg = new Paint();
        circleBg.setColor(Color.parseColor("#E8EDF2"));
        circleBg.setShadowLayer(20, 0, 8, Color.parseColor("#30A0A0A0"));
        canvas.drawCircle(centerX, centerY, iconSize / 2 + 15, circleBg);

        Paint ringPaint = new Paint();
        ringPaint.setColor(Color.parseColor("#4A6CF7"));
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(4);
        canvas.drawCircle(centerX+1, centerY+2, iconSize / 2 + 15, ringPaint);

        Bitmap sampleImage = loadImageFromAssets(IMAGE_FILES[0]);
        if (sampleImage != null) {
            Bitmap scaledIcon = Bitmap.createScaledBitmap(sampleImage, iconSize, iconSize, true);
            canvas.drawBitmap(scaledIcon, centerX - iconSize / 2, centerY - iconSize / 2, null);
        }

        Paint questionPaint = new Paint();
        questionPaint.setColor(Color.parseColor("#4A6CF7"));
        questionPaint.setTextSize(40);
        questionPaint.setTextAlign(Paint.Align.CENTER);
        questionPaint.setTypeface(Typeface.DEFAULT_BOLD);
        canvas.drawText("❓", centerX + iconSize / 2 + 1, centerY - iconSize / 2 - 8, questionPaint);

        // ====== آیکون‌های کوچک دور تا دور ======
        int smallIconSize = 80;
        int smallRadius = 150;
        int smallCount = 8;

        for (int i = 0; i < smallCount; i++) {
            sampleImage = loadImageFromAssets(IMAGE_FILES[i+1]);
            double angle = 2 * Math.PI * i / smallCount;
            float x = (float) (centerX + smallRadius * Math.cos(angle));
            float y = (float) (centerY + smallRadius * Math.sin(angle));

            if (sampleImage != null) {
                Bitmap scaledSmall = Bitmap.createScaledBitmap(sampleImage, smallIconSize, smallIconSize, true);
                canvas.drawBitmap(scaledSmall, x - smallIconSize / 2, y - smallIconSize / 2, null);
            }
        }

        // ====== متن اصلی ======
        Paint textPaint = new Paint();
        textPaint.setColor(Color.parseColor("#2C3E50"));
        textPaint.setTextSize(26);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setShadowLayer(8, 0, 4, Color.parseColor("#30FF8C00"));
        canvas.drawText("عکس مشترک را پیدا کن", WIDTH / 2, HEIGHT - 150, textPaint);

        // ====== متن فرعی ======
        Paint subTextPaint = new Paint();
        subTextPaint.setColor(Color.parseColor("#7B8AA0"));
        subTextPaint.setTextSize(25);
        subTextPaint.setTextAlign(Paint.Align.CENTER);
        subTextPaint.setTypeface(Typeface.DEFAULT);
        canvas.drawText("بین دو مجموعه فقط یک عکس مشترک است", WIDTH / 2, HEIGHT - 110, subTextPaint);

        // ====== دکمه START ======
        Paint startPaint = new Paint();
        startPaint.setColor(Color.parseColor("#4A6CF7"));
        startPaint.setTextSize(17);
        startPaint.setTextAlign(Paint.Align.CENTER);
        startPaint.setTypeface(Typeface.DEFAULT_BOLD);
        canvas.drawText("START", WIDTH / 2, HEIGHT - 80, startPaint);

        // خط زیر START
        Paint linePaint = new Paint();
        linePaint.setColor(Color.parseColor("#4A6CF7"));
        linePaint.setStrokeWidth(2);
        float startX = WIDTH / 2 - 42;
        float startY = HEIGHT - 75;
        canvas.drawLine(startX, startY, startX + 84, startY, linePaint);

        return bitmap;
    }

    public static CommonImageResult generateCommonImageResult(int difficulty) {
        int surroundingCount;
        switch (difficulty) {
            case 1: surroundingCount = 4; break;
            case 2: surroundingCount = 5; break;
            case 3: surroundingCount = 6; break;
            case 4: surroundingCount = 7; break;
            default: surroundingCount = 4; break;
        }

        List<Bitmap> allImages = loadAllImages();
        if (allImages.isEmpty()) {
            return new CommonImageResult(
                    Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888),
                    Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888),
                    0
            );
        }

        Random rand = new Random();

        int commonIndex = rand.nextInt(allImages.size());

        List<Integer> availableIndices = new ArrayList<>();
        for (int i = 0; i < allImages.size(); i++) {
            if (i != commonIndex) {
                availableIndices.add(i);
            }
        }
        Collections.shuffle(availableIndices);

        int totalNeeded = 1 + surroundingCount;

        List<Integer> topSet = new ArrayList<>();
        Set<Integer> usedIndices = new HashSet<>();

        for (int i = 0; i < totalNeeded && i < availableIndices.size(); i++) {
            int index = availableIndices.get(i);
            topSet.add(index);
            usedIndices.add(index);
        }

        Collections.shuffle(availableIndices);
        List<Integer> bottomSet = new ArrayList<>();

        for (int i = 0; i < availableIndices.size() && bottomSet.size() < totalNeeded; i++) {
            int index = availableIndices.get(i);
            if (!usedIndices.contains(index)) {
                bottomSet.add(index);
                usedIndices.add(index);
            }
        }

        if (bottomSet.size() < totalNeeded) {
            Collections.shuffle(availableIndices);
            for (int i = 0; i < availableIndices.size() && bottomSet.size() < totalNeeded; i++) {
                int index = availableIndices.get(i);
                if (!bottomSet.contains(index)) {
                    bottomSet.add(index);
                }
            }
        }

        topSet.add(commonIndex);
        bottomSet.add(commonIndex);

        Collections.shuffle(topSet);
        Collections.shuffle(bottomSet);

        Bitmap questionImage = drawImageGrid(topSet, bottomSet, allImages, false, commonIndex);
        Bitmap answerImage = drawImageGrid(topSet, bottomSet, allImages, true, commonIndex);

        return new CommonImageResult(questionImage, answerImage, commonIndex);
    }

    private static Bitmap drawImageGrid(List<Integer> topSet, List<Integer> bottomSet,
                                        List<Bitmap> allImages, boolean showAnswer, int commonIndex) {
        Bitmap bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        int iconSize = 80;
        int centerSize = 110;
        int radius = 130;
        int topCenterX = WIDTH / 2;
        int topCenterY = HEIGHT / 4;
        int bottomCenterX = WIDTH / 2;
        int bottomCenterY = 3 * HEIGHT / 4;

        Paint answerPaint = new Paint();
        answerPaint.setColor(Color.parseColor("#2ECC71"));
        answerPaint.setStyle(Paint.Style.STROKE);
        answerPaint.setStrokeWidth(5);

        drawSingleSet(canvas, topCenterX, topCenterY, iconSize, centerSize, radius,
                topSet, allImages, showAnswer, commonIndex, answerPaint);

        Paint linePaint = new Paint();
        linePaint.setColor(Color.parseColor("#D0D5DD"));
        linePaint.setStrokeWidth(2);
        canvas.drawLine(30, HEIGHT / 2, WIDTH - 30, HEIGHT / 2, linePaint);

        drawSingleSet(canvas, bottomCenterX, bottomCenterY, iconSize, centerSize, radius,
                bottomSet, allImages, showAnswer, commonIndex, answerPaint);

        if (showAnswer) {
            Paint answerTextPaint = new Paint();
            answerTextPaint.setColor(Color.parseColor("#27AE60"));
            answerTextPaint.setTextSize(18);
            answerTextPaint.setTextAlign(Paint.Align.CENTER);
            answerTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
            String msg = "✅ عکس مشترک پیدا شد!";
            canvas.drawText(msg, WIDTH / 2, HEIGHT - 20, answerTextPaint);
        }

        return bitmap;
    }

    private static void drawSingleSet(Canvas canvas, int centerX, int centerY,
                                      int iconSize, int centerSize, int radius,
                                      List<Integer> imageSet, List<Bitmap> allImages,
                                      boolean showAnswer, int commonIndex, Paint answerPaint) {

        int centerIndex = imageSet.get(0);
        Bitmap centerImage = allImages.get(centerIndex);
        if (centerImage != null) {
            Bitmap scaledCenter = Bitmap.createScaledBitmap(centerImage, centerSize, centerSize, true);
            canvas.drawBitmap(scaledCenter, centerX - centerSize / 2, centerY - centerSize / 2, null);

            if (showAnswer && centerIndex == commonIndex) {
                canvas.drawRect(centerX - centerSize / 2, centerY - centerSize / 2,
                        centerX + centerSize / 2, centerY + centerSize / 2, answerPaint);
            }
        }

        int totalImages = imageSet.size();
        int surroundingTotal = totalImages - 1;

        for (int i = 1; i < totalImages; i++) {
            int index = imageSet.get(i);
            double angle = 2 * Math.PI * (i - 1) / surroundingTotal - Math.PI / 2;
            float x = (float) (centerX + radius * Math.cos(angle));
            float y = (float) (centerY + radius * Math.sin(angle));

            Bitmap image = allImages.get(index);
            if (image != null) {
                Bitmap scaledIcon = Bitmap.createScaledBitmap(image, iconSize, iconSize, true);
                canvas.drawBitmap(scaledIcon, x - iconSize / 2, y - iconSize / 2, null);

                if (showAnswer && index == commonIndex) {
                    canvas.drawRect(x - iconSize / 2, y - iconSize / 2,
                            x + iconSize / 2, y + iconSize / 2, answerPaint);
                }
            }
        }
    }
}