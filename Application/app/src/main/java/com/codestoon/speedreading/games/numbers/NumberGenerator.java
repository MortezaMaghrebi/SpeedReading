package com.codestoon.speedreading.games.numbers;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NumberGenerator {

    private static final int SIZE = 800;
    private static final int PADDING = 40;

    /**
     * تولید رنگ‌های با کنتراست بالا و قابل تشخیص
     */
    private static List<Integer> generateDistinctColors(int count) {
        // رنگ‌های با کنتراست بالا و قابل تشخیص
        int[] colorPalette = {
                Color.parseColor("#2ECC71"), // سبز
                Color.parseColor("#D4D40F"), // زرد
                Color.parseColor("#E69E52"), // نارنجی
                //Color.parseColor("#1ABC9C"), // فیروزه‌ای
                Color.parseColor("#54B8EB"), // آبی
                 Color.parseColor("#9B59B6"), // بنفش
                Color.parseColor("#E74C3C"), // قرمز
                Color.parseColor("#194099"),  // آبی تیره

        };

        List<Integer> colors = new ArrayList<>();
        for (int i = 0; i < Math.min(count, colorPalette.length); i++) {
            colors.add(colorPalette[i]);
        }

        // اگر تعداد رنگ‌ها بیشتر از پالت بود، از ابتدا تکرار کن
        while (colors.size() < count) {
            for (int i = 0; i < colorPalette.length && colors.size() < count; i++) {
                // با تغییر جزئی رنگ، رنگ جدیدی بساز
                int color = colorPalette[i];
                int r = Color.red(color) + (colors.size() * 10) % 50;
                int g = Color.green(color) + (colors.size() * 15) % 50;
                int b = Color.blue(color) + (colors.size() * 20) % 50;
                colors.add(Color.rgb(Math.min(r, 255), Math.min(g, 255), Math.min(b, 255)));
            }
        }

        return colors;
    }

    public static class NumberResult {
        public Bitmap questionImage;
        public Bitmap answerImage;

        public NumberResult(Bitmap questionImage, Bitmap answerImage) {
            this.questionImage = questionImage;
            this.answerImage = answerImage;
        }
    }




    /**
     * تولید تصویر دیفالت
     */
    public static Bitmap generateDefaultNumberImage(int totalNumbers) {
        Bitmap bitmap = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // پس‌زمینه با گرادیانت
        Paint gradientPaint = new Paint();
        android.graphics.LinearGradient gradient = new android.graphics.LinearGradient(
                0, 0, SIZE, SIZE,
                Color.parseColor("#E6F7E6"),
                Color.parseColor("#E6F0FF"),
                android.graphics.Shader.TileMode.CLAMP
        );
        gradientPaint.setShader(gradient);
        canvas.drawRect(0, 0, SIZE, SIZE, gradientPaint);

        // هاله‌های محو
        Paint glowPaint = new Paint();
        glowPaint.setStyle(Paint.Style.FILL);
        glowPaint.setShadowLayer(80, 0, 0, Color.parseColor("#152ECC71"));
        glowPaint.setColor(Color.parseColor("#00FFFFFF"));
        canvas.drawCircle(SIZE * 0.7f, SIZE * 0.7f, 300, glowPaint);

        glowPaint.setShadowLayer(100, 0, 0, Color.parseColor("#154285F4"));
        canvas.drawCircle(SIZE * 0.3f, SIZE * 0.25f, 350, glowPaint);

        // رسم آیکون اعداد
        drawNumberIcon(canvas);

        // متن اصلی با عدد واقعی
        Paint textPaint = new Paint();
        textPaint.setColor(Color.parseColor("#2C3E50"));
        textPaint.setTextSize(52);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setShadowLayer(12, 0, 4, Color.parseColor("#302ECC71"));
        canvas.drawText("آماده شروع هستی؟", SIZE / 2, SIZE - 250, textPaint);

        // متن فرعی با عدد واقعی
        Paint subTextPaint = new Paint();
        subTextPaint.setColor(Color.parseColor("#5D6D7E"));
        subTextPaint.setTextSize(32);
        subTextPaint.setTextAlign(Paint.Align.CENTER);
        subTextPaint.setTypeface(Typeface.DEFAULT);
        canvas.drawText("اعداد "+1+" تا " + totalNumbers + " را پیدا کن", SIZE / 2, SIZE - 200, subTextPaint);

        // متن شروع
        Paint startPaint = new Paint();
        startPaint.setColor(Color.parseColor("#4A6CF7"));
        startPaint.setTextSize(28);
        startPaint.setTextAlign(Paint.Align.CENTER);
        startPaint.setTypeface(Typeface.DEFAULT_BOLD);
        canvas.drawText("دکمه START را بزن", SIZE / 2, SIZE - 160, startPaint);

        return bitmap;
    }

    /**
     * رسم آیکون اعداد در مرکز
     */
    private static void drawNumberIcon(Canvas canvas) {
        int iconSize = 200;
        int startX = (SIZE - iconSize) / 2;
        int startY = (SIZE - iconSize) / 2 - 50;
        int cellSize = iconSize / 4;
        int padding = 4;

        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.parseColor("#F0F4F8"));
        bgPaint.setShadowLayer(10, 0, 4, Color.parseColor("#30B0BEC5"));
        canvas.drawRoundRect(startX, startY, startX + iconSize, startY + iconSize, 20, 20, bgPaint);

        Paint numberPaint = new Paint();
        numberPaint.setColor(Color.parseColor("#2C3E50"));
        numberPaint.setTextSize(cellSize * 0.5f);
        numberPaint.setTextAlign(Paint.Align.CENTER);
        numberPaint.setTypeface(Typeface.DEFAULT_BOLD);

        // اعداد 1 تا 16 در یک شبکه 4x4 به صورت به هم ریخته
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= 16; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers);

        int index = 0;
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
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
        int gridSize, totalNumbers, groupSize;

        switch (difficulty) {
            case 1:
                gridSize = 4;
                totalNumbers = 16;
                groupSize = 4;
                break;
            case 2:
                gridSize = 5;
                totalNumbers = 25;
                groupSize = 5;
                break;
            case 3:
                gridSize = 6;
                totalNumbers = 36;
                groupSize = 6;
                break;
            case 4:
                gridSize = 7;
                totalNumbers = 49;
                groupSize = 7;
                break;
            default:
                gridSize = 4;
                totalNumbers = 16;
                groupSize = 4;
                break;
        }

        // ایجاد لیست اعداد و شافل کردن
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= totalNumbers; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers);

        // ایجاد لیست موقعیت‌های تصادفی
        List<int[]> positions = new ArrayList<>();
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                positions.add(new int[]{row, col});
            }
        }
        Collections.shuffle(positions);

        // تولید رنگ‌های قابل تشخیص
        int numberOfGroups = totalNumbers / groupSize;
        List<Integer> distinctColors = generateDistinctColors(numberOfGroups);

        // ساخت تصویر سوال
        Bitmap questionImage = drawNumberGrid(gridSize, numbers, positions, false, groupSize, totalNumbers, distinctColors);

        // ساخت تصویر جواب (با رنگ‌بندی دسته‌بندی شده)
        Bitmap answerImage = drawNumberGrid(gridSize, numbers, positions, true, groupSize, totalNumbers, distinctColors);

        return new NumberResult(questionImage, answerImage);
    }

    /**
     * رسم شبکه اعداد بدون خطوط و با چینش به هم ریخته
     */
    private static Bitmap drawNumberGrid(int gridSize, List<Integer> numbers,
                                         List<int[]> positions, boolean showSolution,
                                         int groupSize, int totalNumbers, List<Integer> distinctColors) {
        Bitmap bitmap = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        float availableSize = SIZE - 2 * PADDING;
        float cellSize = availableSize / gridSize;
        float offsetX = PADDING;
        float offsetY = PADDING;
        float padding = cellSize * 0.08f;

        Paint numberPaint = new Paint();
        numberPaint.setTextAlign(Paint.Align.CENTER);
        numberPaint.setTypeface(Typeface.DEFAULT_BOLD);

        Paint solutionPaint = new Paint();
        solutionPaint.setTextAlign(Paint.Align.CENTER);
        solutionPaint.setTypeface(Typeface.DEFAULT_BOLD);
        solutionPaint.setShadowLayer(6, 0, 0, Color.parseColor("#30FFFFFF"));

        Paint cellBgPaint = new Paint();
        cellBgPaint.setStyle(Paint.Style.FILL);

        int index = 0;
        for (int[] pos : positions) {
            int row = pos[0];
            int col = pos[1];
            int number = numbers.get(index++);

            float x = offsetX + col * cellSize;
            float y = offsetY + row * cellSize;

            if (showSolution) {
                // در حالت جواب: رنگ‌بندی بر اساس دسته‌ها
                int groupIndex = (number - 1) / groupSize;
                int color = distinctColors.get(groupIndex % distinctColors.size());

                // رنگ با شفافیت برای پس‌زمینه
                int bgColor = color & 0x00FFFFFF | 0x25FFFFFF;
                cellBgPaint.setColor(bgColor);
                canvas.drawRoundRect(x + padding, y + padding,
                        x + cellSize - padding, y + cellSize - padding,
                        8, 8, cellBgPaint);

                // رنگ کامل برای متن
                solutionPaint.setColor(color);
                solutionPaint.setTextSize(cellSize * 0.45f);

                float centerX = x + cellSize / 2;
                float centerY = y + cellSize / 2;
                Paint.FontMetrics fm = solutionPaint.getFontMetrics();
                float textY = centerY - (fm.ascent + fm.descent) / 2;
                canvas.drawText(String.valueOf(number), centerX, textY, solutionPaint);

            } else {
                // در حالت سوال: اعداد مشکی روی زمینه سفید
                numberPaint.setColor(Color.parseColor("#2C3E50"));
                numberPaint.setTextSize(cellSize * 0.4f);

                float centerX = x + cellSize / 2;
                float centerY = y + cellSize / 2;
                Paint.FontMetrics fm = numberPaint.getFontMetrics();
                float textY = centerY - (fm.ascent + fm.descent) / 2;
                canvas.drawText(String.valueOf(number), centerX, textY, numberPaint);
            }
        }

        // اضافه کردن حاشیه‌های ظریف
        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.parseColor("#E8EDF2"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1);
        canvas.drawRoundRect(offsetX, offsetY, offsetX + availableSize, offsetY + availableSize, 16, 16, borderPaint);

        return bitmap;
    }
}