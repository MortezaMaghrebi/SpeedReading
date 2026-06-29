package com.codestoon.speedreading.games.maze;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class MazeGenerator {

    private static final int SIZE = 800;
    private static final int PADDING = 30;
    private static final Random random = new Random();

    private static class Cell {
        boolean top = true;
        boolean right = true;
        boolean bottom = true;
        boolean left = true;
        boolean visited = false;
    }

    private static class Point {
        int r, c;
        Point(int r, int c) {
            this.r = r;
            this.c = c;
        }
    }

    public static class MazeResult {
        public Bitmap questionImage;
        public Bitmap answerImage;

        public MazeResult(Bitmap questionImage, Bitmap answerImage) {
            this.questionImage = questionImage;
            this.answerImage = answerImage;
        }
    }

    /**
     * تولید تصویر دیفالت - با آیکون ماز و نوشته‌های بزرگ
     */
    public static Bitmap generateDefaultMazeImage() {
        Bitmap bitmap = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // پس‌زمینه با گرادیانت محو (نارنجی و آبی بسیار کمرنگ)
        Paint gradientPaint = new Paint();
        android.graphics.LinearGradient gradient = new android.graphics.LinearGradient(
                0, 0, SIZE, SIZE,
                Color.parseColor("#FFF5E6"),  // نارنجی بسیار کمرنگ
                Color.parseColor("#E6F0FF"),  // آبی بسیار کمرنگ
                android.graphics.Shader.TileMode.CLAMP
        );
        gradientPaint.setShader(gradient);
        canvas.drawRect(0, 0, SIZE, SIZE, gradientPaint);

        // هاله‌های محو (Glow) در پس‌زمینه
        Paint glowPaint = new Paint();
        glowPaint.setStyle(Paint.Style.FILL);
        glowPaint.setShadowLayer(80, 0, 0, Color.parseColor("#15FF8C00")); // نارنجی محو
        glowPaint.setColor(Color.parseColor("#00FFFFFF"));
        canvas.drawCircle(SIZE * 0.7f, SIZE * 0.7f, 300, glowPaint);

        glowPaint.setShadowLayer(100, 0, 0, Color.parseColor("#154285F4")); // آبی محو
        canvas.drawCircle(SIZE * 0.3f, SIZE * 0.25f, 350, glowPaint);

        // رسم آیکون ماز در مرکز
        drawMazeIcon(canvas);

        // متن اصلی با سایه
        Paint textPaint = new Paint();
        textPaint.setColor(Color.parseColor("#2C3E50"));
        textPaint.setTextSize(56);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setShadowLayer(12, 0, 4, Color.parseColor("#30FF8C00"));
        canvas.drawText("آماده شروع هستی؟", SIZE / 2, SIZE - 250, textPaint);

        // متن فرعی
        Paint subTextPaint = new Paint();
        subTextPaint.setColor(Color.parseColor("#5D6D7E"));
        subTextPaint.setTextSize(32);
        subTextPaint.setTextAlign(Paint.Align.CENTER);
        subTextPaint.setTypeface(Typeface.DEFAULT);
        canvas.drawText("دکمه START را بزن", SIZE / 2, SIZE - 200, subTextPaint);

        // راهنمای S و E با طراحی زیبا
        Paint dotPaint = new Paint();
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setShadowLayer(10, 0, 0, Color.parseColor("#40FF8C00"));

        // نقطه شروع (نارنجی)
        dotPaint.setColor(Color.parseColor("#FF8C00"));
        canvas.drawCircle(SIZE / 2f - 70, SIZE - 85, 22, dotPaint);

        Paint labelPaint = new Paint();
        labelPaint.setColor(Color.WHITE);
        labelPaint.setTextSize(28);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setFakeBoldText(true);
        labelPaint.setShadowLayer(6, 0, 2, Color.parseColor("#60FF8C00"));
        canvas.drawText("S", SIZE / 2f - 70, SIZE - 76, labelPaint);

        // فلش
        Paint arrowPaint = new Paint();
        arrowPaint.setColor(Color.parseColor("#5D6D7E"));
        arrowPaint.setTextSize(40);
        arrowPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("→", SIZE / 2f, SIZE - 78, arrowPaint);

        // نقطه پایان (آبی)
        dotPaint.setColor(Color.parseColor("#4285F4"));
        dotPaint.setShadowLayer(10, 0, 0, Color.parseColor("#404285F4"));
        canvas.drawCircle(SIZE / 2f + 70, SIZE - 85, 22, dotPaint);

        labelPaint.setColor(Color.WHITE);
        labelPaint.setShadowLayer(6, 0, 2, Color.parseColor("#604285F4"));
        canvas.drawText("E", SIZE / 2f + 70, SIZE - 76, labelPaint);


        return bitmap;
    }



    /**
     * تولید الگوی ماز 7x7
     */
    private static boolean[][] generateMazePattern() {
        // true = دیوار وجود دارد، false = دیوار وجود ندارد
        return new boolean[][]{
                {true, false, true, false, true, false, true},
                {true, false, true, false, true, false, true},
                {true, false, false, false, true, false, false},
                {true, true, false, true, false, true, false},
                {false, false, false, true, false, true, false},
                {true, false, true, false, false, false, false},
                {true, false, true, true, true, false, true}
        };
    }

    /**
     * رسم آیکون ماز در مرکز
     */
    private static void drawMazeIcon(Canvas canvas) {
        int iconSize = 180;
        int startX = (SIZE - iconSize) / 2;
        int startY = (SIZE - iconSize) / 2 - 50;
        int cellSize = iconSize / 7;
        int wallThick = 4;

        Paint wallPaint = new Paint();
        wallPaint.setColor(Color.parseColor("#2C3E50"));
        wallPaint.setStrokeWidth(wallThick);
        wallPaint.setStyle(Paint.Style.STROKE);
        wallPaint.setStrokeCap(Paint.Cap.SQUARE);
        wallPaint.setShadowLayer(8, 0, 4, Color.parseColor("#30FF8C00"));

        // یک ماز 7x7 با مسیر مشخص
        boolean[][] walls = generateMazePattern();

        // رسم دیوارها
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 7; c++) {
                float x = startX + c * cellSize;
                float y = startY + r * cellSize;

                // دیوار بالا
                if (r == 0 || walls[r][c]) {
                    canvas.drawLine(x, y, x + cellSize, y, wallPaint);
                }
                // دیوار چپ
                if (c == 0 || walls[r][c]) {
                    canvas.drawLine(x, y, x, y + cellSize, wallPaint);
                }
                // دیوار پایین (برای آخرین ردیف)
                if (r == 6) {
                    canvas.drawLine(x, y + cellSize, x + cellSize, y + cellSize, wallPaint);
                }
                // دیوار راست (برای آخرین ستون)
                if (c == 6) {
                    canvas.drawLine(x + cellSize, y, x + cellSize, y + cellSize, wallPaint);
                }
            }
        }

        // نقطه شروع (S)
        Paint dotPaint = new Paint();
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setColor(Color.parseColor("#FF8C00"));
        dotPaint.setShadowLayer(8, 0, 0, Color.parseColor("#60FF8C00"));
        float sX = startX + cellSize / 2;
        float sY = startY + cellSize / 2;
        canvas.drawCircle(sX, sY, cellSize * 0.25f, dotPaint);

        // نقطه پایان (E)
        dotPaint.setColor(Color.parseColor("#4285F4"));
        dotPaint.setShadowLayer(8, 0, 0, Color.parseColor("#604285F4"));
        float eX = startX + 6 * cellSize + cellSize / 2;
        float eY = startY + 6 * cellSize + cellSize / 2;
        canvas.drawCircle(eX, eY, cellSize * 0.25f, dotPaint);

        // برچسب‌های S و E
        Paint labelPaint = new Paint();
        labelPaint.setColor(Color.WHITE);
        labelPaint.setTextSize(cellSize * 0.3f);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setFakeBoldText(true);
        Paint.FontMetrics fm = labelPaint.getFontMetrics();
        canvas.drawText("S", sX, sY - (fm.ascent + fm.descent) / 2, labelPaint);
        canvas.drawText("E", eX, eY - (fm.ascent + fm.descent) / 2, labelPaint);
    }
    /**
     * تولید ماز با نتیجه (هم سوال و هم جواب)
     */
    public static MazeResult generateMazeWithResult(int difficulty) {
        int rows, cols;
        switch (difficulty) {
            case 1: rows = 8; cols = 8; break;
            case 2: rows = 12; cols = 12; break;
            case 3: rows = 16; cols = 16; break;
            case 4: rows = 18; cols = 18; break;
            default: rows = 8; cols = 8; break;
        }

        Cell[][] grid = generateMazeGrid(rows, cols);
        List<Point> solutionPath = findSolution(grid, rows, cols);

        Bitmap questionImage = drawMaze(grid, rows, cols, solutionPath, false);
        Bitmap answerImage = drawMaze(grid, rows, cols, solutionPath, true);

        return new MazeResult(questionImage, answerImage);
    }

    /**
     * تولید گرید ماز
     */
    private static Cell[][] generateMazeGrid(int rows, int cols) {
        Cell[][] grid = new Cell[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = new Cell();
            }
        }

        dfs(grid, rows, cols, 0, 0);
        grid[0][0].top = false;
        grid[rows - 1][cols - 1].bottom = false;

        return grid;
    }

    /**
     * الگوریتم DFS
     */
    private static void dfs(Cell[][] grid, int rows, int cols, int r, int c) {
        grid[r][c].visited = true;

        List<int[]> neighbors = new ArrayList<>();
        if (r > 0) neighbors.add(new int[]{r - 1, c, 0});
        if (r < rows - 1) neighbors.add(new int[]{r + 1, c, 1});
        if (c > 0) neighbors.add(new int[]{r, c - 1, 2});
        if (c < cols - 1) neighbors.add(new int[]{r, c + 1, 3});

        for (int i = neighbors.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int[] temp = neighbors.get(i);
            neighbors.set(i, neighbors.get(j));
            neighbors.set(j, temp);
        }

        for (int[] nb : neighbors) {
            int nr = nb[0], nc = nb[1], dir = nb[2];
            if (!grid[nr][nc].visited) {
                switch (dir) {
                    case 0:
                        grid[r][c].top = false;
                        grid[nr][nc].bottom = false;
                        break;
                    case 1:
                        grid[r][c].bottom = false;
                        grid[nr][nc].top = false;
                        break;
                    case 2:
                        grid[r][c].left = false;
                        grid[nr][nc].right = false;
                        break;
                    case 3:
                        grid[r][c].right = false;
                        grid[nr][nc].left = false;
                        break;
                }
                dfs(grid, rows, cols, nr, nc);
            }
        }
    }

    /**
     * پیدا کردن مسیر جواب با BFS
     */
    private static List<Point> findSolution(Cell[][] grid, int rows, int cols) {
        boolean[][] visited = new boolean[rows][cols];
        Point[][] parent = new Point[rows][cols];
        Queue<Point> queue = new LinkedList<>();

        queue.add(new Point(0, 0));
        visited[0][0] = true;

        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        while (!queue.isEmpty()) {
            Point current = queue.poll();
            int r = current.r, c = current.c;

            if (r == rows - 1 && c == cols - 1) break;

            for (int i = 0; i < 4; i++) {
                int nr = r + dirs[i][0];
                int nc = c + dirs[i][1];

                if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) continue;
                if (visited[nr][nc]) continue;

                boolean wallOpen = false;
                switch (i) {
                    case 0: wallOpen = !grid[r][c].top; break;
                    case 1: wallOpen = !grid[r][c].bottom; break;
                    case 2: wallOpen = !grid[r][c].left; break;
                    case 3: wallOpen = !grid[r][c].right; break;
                }

                if (wallOpen) {
                    visited[nr][nc] = true;
                    parent[nr][nc] = new Point(r, c);
                    queue.add(new Point(nr, nc));
                }
            }
        }

        List<Point> path = new ArrayList<>();
        Point current = new Point(rows - 1, cols - 1);
        while (current != null) {
            path.add(0, current);
            Point p = parent[current.r][current.c];
            if (p == null) break;
            current = p;
        }

        return path;
    }

    /**
     * رسم ماز
     */
    private static Bitmap drawMaze(Cell[][] grid, int rows, int cols,
                                   List<Point> solutionPath, boolean showSolution) {
        Bitmap bitmap = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        float availableSize = SIZE - 2 * PADDING;
        float cellSize = availableSize / cols;
        float wallThick = Math.max(4, cellSize * 0.15f);
        float offsetX = PADDING;
        float offsetY = PADDING;

        canvas.drawColor(Color.WHITE);

        Paint wallPaint = new Paint();
        wallPaint.setColor(Color.parseColor("#2c3e50"));
        wallPaint.setStrokeWidth(wallThick);
        wallPaint.setStyle(Paint.Style.STROKE);
        wallPaint.setStrokeCap(Paint.Cap.SQUARE);

        // رسم دیوارها
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                float x = offsetX + c * cellSize;
                float y = offsetY + r * cellSize;
                Cell cell = grid[r][c];

                if (cell.top) {
                    canvas.drawLine(x, y, x + cellSize, y, wallPaint);
                }
                if (cell.right) {
                    canvas.drawLine(x + cellSize, y, x + cellSize, y + cellSize, wallPaint);
                }
                if (cell.bottom) {
                    canvas.drawLine(x, y + cellSize, x + cellSize, y + cellSize, wallPaint);
                }
                if (cell.left) {
                    canvas.drawLine(x, y, x, y + cellSize, wallPaint);
                }
            }
        }

        // رسم مسیر جواب
        if (showSolution && solutionPath != null && !solutionPath.isEmpty()) {
            Paint pathPaint = new Paint();
            pathPaint.setColor(Color.parseColor("#2ecc71"));
            pathPaint.setStrokeWidth(Math.max(4, cellSize * 0.2f));
            pathPaint.setStyle(Paint.Style.STROKE);
            pathPaint.setStrokeCap(Paint.Cap.ROUND);
            pathPaint.setStrokeJoin(Paint.Join.ROUND);
            pathPaint.setShadowLayer(8, 0, 0, Color.parseColor("#4D2ecc71"));

            Path path = new Path();
            boolean first = true;
            for (Point p : solutionPath) {
                float x = offsetX + p.c * cellSize + cellSize / 2;
                float y = offsetY + p.r * cellSize + cellSize / 2;
                if (first) {
                    path.moveTo(x, y);
                    first = false;
                } else {
                    path.lineTo(x, y);
                }
            }
            canvas.drawPath(path, pathPaint);
        }

        // نقطه شروع و پایان
        float startX = offsetX + cellSize / 2;
        float startY = offsetY + cellSize / 2;
        float endX = offsetX + (cols - 1) * cellSize + cellSize / 2;
        float endY = offsetY + (rows - 1) * cellSize + cellSize / 2;
        float dotRadius = Math.max(8, cellSize * 0.18f);

        Paint startPaint = new Paint();
        startPaint.setColor(Color.parseColor("#3498db"));
        startPaint.setShadowLayer(12, 0, 0, Color.parseColor("#663498db"));
        canvas.drawCircle(startX, startY, dotRadius, startPaint);

        Paint endPaint = new Paint();
        endPaint.setColor(Color.parseColor("#e74c3c"));
        endPaint.setShadowLayer(12, 0, 0, Color.parseColor("#66e74c3c"));
        canvas.drawCircle(endX, endY, dotRadius, endPaint);

        // برچسب‌ها
        Paint labelPaint = new Paint();
        labelPaint.setColor(Color.WHITE);
        labelPaint.setTextSize(Math.max(14, cellSize * 0.25f));
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setFakeBoldText(true);
        Paint.FontMetrics fm = labelPaint.getFontMetrics();
        float textY = startY - (fm.ascent + fm.descent) / 2;
        canvas.drawText("S", startX, textY, labelPaint);

        float textY2 = endY - (fm.ascent + fm.descent) / 2;
        canvas.drawText("E", endX, textY2, labelPaint);

        return bitmap;
    }
}