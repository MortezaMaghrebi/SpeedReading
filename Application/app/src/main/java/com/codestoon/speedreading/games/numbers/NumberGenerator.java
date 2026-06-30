package com.codestoon.speedreading.games.numbers;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class NumberGenerator {

    private static final int SIZE = 800;
    private static final int PADDING = 30;

    public static class NumberResult {
        public Bitmap questionImage;
        public Bitmap answerImage;

        public NumberResult(Bitmap questionImage, Bitmap answerImage) {
            this.questionImage = questionImage;
            this.answerImage = answerImage;
        }
    }

    /**
     * تولید تصویر دیفالت - با آیکون اعداد و نوشته‌های بزرگ
     */
    public static Bitmap generateDefaultNumberImage() {
        Bitmap bitmap = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // پس‌زمینه با گرادیانت محو (سبز و آبی بسیار کمرنگ)
        Paint gradientPaint = new Paint();
        android.graphics.LinearGradient gradient = new android.graphics.LinearGradient(
                0, 0, SIZE, SIZE,
                Color.parseColor("#E6F7E6"),  // سبز بسیار کمرنگ
                Color.parseColor("#E6F0FF"),  // آبی بسیار کمرنگ
                android.graphics.Shader.TileMode.CLAMP
        );
        gradientPaint.setShader(gradient);
        canvas.drawRect(0, 0, SIZE, SIZE, gradientPaint);

        // هاله‌های محو (Glow) در پس‌زمینه
        Paint glowPaint = new Paint();
        glowPaint.setStyle(Paint.Style.FILL);
        glowPaint.setShadowLayer(80, 0, 0, Color.parseColor("#152ECC71"));
        glowPaint.setColor(Color.parseColor("#00FFFFFF"));
        canvas.drawCircle(SIZE * 0.7f, SIZE * 0.7f, 300, glowPaint);

        glowPaint.setShadowLayer(100, 0, 0, Color.parseColor("#154285F4"));
        canvas.drawCircle(SIZE * 0.3f, SIZE * 0.25f, 350, glowPaint);

        // رسم آیکون اعداد در مرکز
        drawNumberIcon(canvas);

        // متن اصلی با سایه
        Paint textPaint = new Paint();
        textPaint.setColor(Color.parseColor("#2C3E50"));
        textPaint.setTextSize(56);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setShadowLayer(12, 0, 4, Color.parseColor("#302ECC71"));
        canvas.drawText("آماده شروع هستی؟", SIZE / 2, SIZE - 250, textPaint);

        // متن فرعی
        Paint subTextPaint = new Paint();
        subTextPaint.setColor(Color.parseColor("#5D6D7E"));
        subTextPaint.setTextSize(32);
        subTextPaint.setTextAlign(Paint.Align.CENTER);
        subTextPaint.setTypeface(Typeface.DEFAULT);
        canvas.drawText("دکمه START را بزن", SIZE / 2, SIZE - 200, subTextPaint);

        return bitmap;
    }

    /**
     * رسم آیکون اعداد در مرکز
     */
    private static void drawNumberIcon(Canvas canvas) {
        int iconSize = 200;
        int startX = (SIZE - iconSize) / 2;
        int startY = (SIZE - iconSize) / 2 - 50;
        int cellSize = iconSize / 5;
        int padding = 4;

        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.parseColor("#F0F4F8"));
        bgPaint.setShadowLayer(10, 0, 4, Color.parseColor("#30B0BEC5"));
        canvas.drawRoundRect(startX, startY, startX + iconSize, startY + iconSize, 20, 20, bgPaint);

        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.parseColor("#D0D5DD"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2);
        canvas.drawRoundRect(startX, startY, startX + iconSize, startY + iconSize, 20, 20, borderPaint);

        Paint numberPaint = new Paint();
        numberPaint.setColor(Color.parseColor("#2C3E50"));
        numberPaint.setTextSize(cellSize * 0.6f);
        numberPaint.setTextAlign(Paint.Align.CENTER);
        numberPaint.setTypeface(Typeface.DEFAULT_BOLD);

        // اعداد 1 تا 25 در یک شبکه 5x5
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= 25; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers);

        int index = 0;
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                float x = startX + col * cellSize + cellSize / 2;
                float y = startY + row * cellSize + cellSize / 2;

                // پس‌زمینه هر سلول
                Paint cellBg = new Paint();
                cellBg.setColor(Color.parseColor("#FFFFFF"));
                float left = startX + col * cellSize + padding;
                float top = startY + row * cellSize + padding;
                float right = left + cellSize - padding * 2;
                float bottom = top + cellSize - padding * 2;
                canvas.drawRoundRect(left, top, right, bottom, 6, 6, cellBg);

                int number = numbers.get(index++);
                Paint.FontMetrics fm = numberPaint.getFontMetrics();
                float textY = y - (fm.ascent + fm.descent) / 2;
                canvas.drawText(String.valueOf(number), x, textY, numberPaint);
            }
        }
    }

    /**
     * تولید تصویر اعداد با سطح سختی
     */
    public static NumberResult generateNumberWithResult(int difficulty) {
        int gridSize;
        switch (difficulty) {
            case 1: gridSize = 6; break;   // 36 عدد
            case 2: gridSize = 8; break;   // 64 عدد
            case 3: gridSize = 10; break;  // 100 عدد
            case 4: gridSize = 12; break;  // 144 عدد
            default: gridSize = 6; break;
        }

        int totalNumbers = gridSize * gridSize;
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= totalNumbers; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers);

        Bitmap questionImage = drawNumberGrid(gridSize, numbers, false);
        Bitmap answerImage = drawNumberGrid(gridSize, numbers, true);

        return new NumberResult(questionImage, answerImage);
    }

    /**
     * رسم شبکه اعداد
     */
    private static Bitmap drawNumberGrid(int gridSize, List<Integer> numbers, boolean showSolution) {
        Bitmap bitmap = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        float availableSize = SIZE - 2 * PADDING;
        float cellSize = availableSize / gridSize;
        float offsetX = PADDING;
        float offsetY = PADDING;

        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.parseColor("#D0D5DD"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2);

        Paint numberPaint = new Paint();
        numberPaint.setColor(Color.parseColor("#2C3E50"));
        numberPaint.setTextSize(cellSize * 0.4f);
        numberPaint.setTextAlign(Paint.Align.CENTER);
        numberPaint.setTypeface(Typeface.DEFAULT_BOLD);

        Paint solutionPaint = new Paint();
        solutionPaint.setColor(Color.parseColor("#2ECC71"));
        solutionPaint.setTextSize(cellSize * 0.5f);
        solutionPaint.setTextAlign(Paint.Align.CENTER);
        solutionPaint.setTypeface(Typeface.DEFAULT_BOLD);
        solutionPaint.setShadowLayer(8, 0, 0, Color.parseColor("#4D2ECC71"));

        int index = 0;
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                float x = offsetX + col * cellSize;
                float y = offsetY + row * cellSize;
                int number = numbers.get(index++);

                // پس‌زمینه سلول
                Paint cellBg = new Paint();
                cellBg.setColor(Color.parseColor("#FFFFFF"));
                canvas.drawRect(x, y, x + cellSize, y + cellSize, cellBg);

                // مرز سلول
                canvas.drawRect(x, y, x + cellSize, y + cellSize, borderPaint);

                // عدد
                float centerX = x + cellSize / 2;
                float centerY = y + cellSize / 2;
                Paint.FontMetrics fm = numberPaint.getFontMetrics();
                float textY = centerY - (fm.ascent + fm.descent) / 2;

                if (showSolution && number <= 30) {
                    // نمایش مسیر پاسخ با اعداد سبز رنگ
                    canvas.drawText(String.valueOf(number), centerX, textY, solutionPaint);
                } else {
                    canvas.drawText(String.valueOf(number), centerX, textY, numberPaint);
                }
            }
        }

        return bitmap;
    }
}