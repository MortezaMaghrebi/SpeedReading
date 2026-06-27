package com.codestoon.speedreading.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codestoon.speedreading.R;
import com.codestoon.speedreading.adapters.GameHistoryAdapter;
import com.codestoon.speedreading.models.GameModel;
import com.codestoon.speedreading.utils.GamePreferencesHelper;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GamesFragment extends Fragment {

    // ====== ویوها ======
    private TextView tvGameTitle, tvLevel, tvTimer, tvBestTime, tvInstruction, tvResult, tvResultDetail;
    private ImageView ivGameDisplay, ivGameAnswer;
    private Button btnStartGame, btnFinishGame, btnNextLevel, btnResetGame;
    private Button btnBackToMenu, btnBackToMenuBottom;
    private LinearLayout layoutGameMenu, layoutGamePlay, layoutResult, layoutChart, layoutAnswer;
    private LineChart chartProgress;
    private RecyclerView rvHistory;
    private GameHistoryAdapter historyAdapter;

    // ====== متغیرهای بازی ======
    private String currentLanguage = "fa";
    private String currentGame = "";
    private int currentLevel = 1;
    private int maxLevel = 10;
    private boolean isGameRunning = false;
    private boolean isGameFinished = false;
    private Handler timerHandler = new Handler();
    private long startTime = 0;
    private double elapsedTime = 0;

    // ====== متغیرهای بازی ماز ======
    private String[] mazeLevels = {"level_1", "level_2", "level_3", "level_4"};

    // ====== متغیرهای بازی اعداد ======
    private int numberGridSize = 7;

    // ====== متغیرهای بازی شمارش کلمه ======
    private String targetWord = "";
    private String fullText = "";
    private int targetCount = 0;

    // ====== متغیرهای بازی عکس مشترک ======
    private int commonImageIndex = 0;
    private int[] imageSet1 = new int[9];
    private int[] imageSet2 = new int[9];

    private boolean isViewReady = false;

    // ====== لیست آیکون‌ها برای بازی عکس ======
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_games, container, false);

        initViews(view);
        setupHistoryRecycler();
        showGameMenu();

        btnStartGame.setOnClickListener(v -> startGame());
        btnFinishGame.setOnClickListener(v -> finishGame());
        btnNextLevel.setOnClickListener(v -> nextLevel());
        btnResetGame.setOnClickListener(v -> resetGame());
        btnBackToMenu.setOnClickListener(v -> backToMenu());
        btnBackToMenuBottom.setOnClickListener(v -> backToMenu());

        isViewReady = true;
        return view;
    }

    private void initViews(View view) {
        tvGameTitle = view.findViewById(R.id.tvGameTitle);
        tvLevel = view.findViewById(R.id.tvLevel);
        tvTimer = view.findViewById(R.id.tvTimer);
        tvBestTime = view.findViewById(R.id.tvBestTime);
        tvInstruction = view.findViewById(R.id.tvInstruction);
        tvResult = view.findViewById(R.id.tvResult);
        tvResultDetail = view.findViewById(R.id.tvResultDetail);
        ivGameDisplay = view.findViewById(R.id.ivGameDisplay);
        ivGameAnswer = view.findViewById(R.id.ivGameAnswer);
        btnStartGame = view.findViewById(R.id.btnStartGame);
        btnFinishGame = view.findViewById(R.id.btnFinishGame);
        btnNextLevel = view.findViewById(R.id.btnNextLevel);
        btnResetGame = view.findViewById(R.id.btnResetGame);
        btnBackToMenu = view.findViewById(R.id.btnBackToMenu);
        btnBackToMenuBottom = view.findViewById(R.id.btnBackToMenuBottom);
        layoutGameMenu = view.findViewById(R.id.layoutGameMenu);
        layoutGamePlay = view.findViewById(R.id.layoutGamePlay);
        layoutResult = view.findViewById(R.id.layoutResult);
        layoutChart = view.findViewById(R.id.layoutChart);
        layoutAnswer = view.findViewById(R.id.layoutAnswer);
        chartProgress = view.findViewById(R.id.chartProgress);
        rvHistory = view.findViewById(R.id.rvHistory);

        // دکمه‌های منو
        view.findViewById(R.id.btnGameMaze).setOnClickListener(v -> selectGame("maze"));
        view.findViewById(R.id.btnGameNumbers).setOnClickListener(v -> selectGame("numbers"));
        view.findViewById(R.id.btnGameWordCount).setOnClickListener(v -> selectGame("word_count"));
        view.findViewById(R.id.btnGameCommonImage).setOnClickListener(v -> selectGame("common_image"));
    }

    private void setupHistoryRecycler() {
        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        historyAdapter = new GameHistoryAdapter(new ArrayList<>());
        rvHistory.setAdapter(historyAdapter);
    }

    private void showGameMenu() {
        layoutGameMenu.setVisibility(View.VISIBLE);
        layoutGamePlay.setVisibility(View.GONE);
        layoutResult.setVisibility(View.GONE);
        layoutChart.setVisibility(View.GONE);
        layoutAnswer.setVisibility(View.GONE);
        btnStartGame.setVisibility(View.GONE);
        btnFinishGame.setVisibility(View.GONE);
        btnNextLevel.setVisibility(View.GONE);
        btnResetGame.setVisibility(View.GONE);
        btnBackToMenu.setVisibility(View.GONE);
        btnBackToMenuBottom.setVisibility(View.GONE);
    }

    private void selectGame(String game) {
        currentGame = game;
        currentLevel = 1;
        resetGame();
        showGameInfo();
    }

    private void showGameInfo() {
        layoutGameMenu.setVisibility(View.GONE);
        layoutGamePlay.setVisibility(View.VISIBLE);
        layoutResult.setVisibility(View.GONE);
        layoutChart.setVisibility(View.GONE);
        layoutAnswer.setVisibility(View.GONE);
        btnStartGame.setVisibility(View.VISIBLE);
        btnFinishGame.setVisibility(View.GONE);
        btnNextLevel.setVisibility(View.GONE);
        btnResetGame.setVisibility(View.GONE);
        btnBackToMenu.setVisibility(View.VISIBLE);
        btnBackToMenuBottom.setVisibility(View.GONE);
        btnBackToMenu.setText("←");
        ivGameDisplay.setImageBitmap(null);
        ivGameAnswer.setImageBitmap(null);

        String gameName = getGameName(currentGame);
        tvGameTitle.setText(gameName);
        tvLevel.setText(getLevelText() + " " + currentLevel + "/" + maxLevel);
        tvTimer.setText("⏱ 00:00");
        tvBestTime.setText(getBestTimeText() + ": " + getBestTime());
        tvInstruction.setText(getGameInstruction(currentGame));
        tvResult.setText("");
        tvResultDetail.setText("");
        setupChartAndHistory();
    }

    private String getGameName(String game) {
        if (currentLanguage.equals("en")) {
            switch (game) {
                case "maze": return "Maze";
                case "numbers": return "Number Hunt";
                case "word_count": return "Word Counter";
                case "common_image": return "Find Common Image";
                default: return "Game";
            }
        } else {
            switch (game) {
                case "maze": return "ماز";
                case "numbers": return "پیدا کردن اعداد";
                case "word_count": return "شمارش کلمه";
                case "common_image": return "پیدا کردن عکس مشترک";
                default: return "بازی";
            }
        }
    }

    private String getLevelText() {
        return currentLanguage.equals("en") ? "Level" : "سطح";
    }

    private String getBestTimeText() {
        return currentLanguage.equals("en") ? "Best Time" : "بهترین زمان";
    }

    private String getGameInstruction(String game) {
        if (currentLanguage.equals("en")) {
            switch (game) {
                case "maze": return "Look at the maze, find the path in your mind, then press Finish";
                case "numbers": return "Find numbers 1 to 49 in order in your mind, then press Finish";
                case "word_count": return "Count how many times the target word appears, then press Finish";
                case "common_image": return "Find the image that appears in both sets, then press Finish";
                default: return "";
            }
        } else {
            switch (game) {
                case "maze": return "به ماز نگاه کن، مسیر را در ذهن پیدا کن، سپس دکمه پایان را بزن";
                case "numbers": return "اعداد ۱ تا ۴۹ را به ترتیب در ذهن پیدا کن، سپس دکمه پایان را بزن";
                case "word_count": return "تعداد تکرار کلمه هدف را پیدا کن، سپس دکمه پایان را بزن";
                case "common_image": return "عکسی که در هر دو مجموعه وجود دارد را پیدا کن، سپس دکمه پایان را بزن";
                default: return "";
            }
        }
    }

    private String getBestTime() {
        List<GameModel> history = GamePreferencesHelper.loadGameHistory(getContext(), currentGame);
        if (history.isEmpty()) return currentLanguage.equals("en") ? "Not played yet" : "هنوز بازی نشده";

        double best = Double.MAX_VALUE;
        for (GameModel game : history) {
            if (game.getLevel() == currentLevel && game.getTime() < best) {
                best = game.getTime();
            }
        }
        if (best == Double.MAX_VALUE) return currentLanguage.equals("en") ? "Not played yet" : "هنوز بازی نشده";
        return String.format("%.2f", best) + "s";
    }

    private void startGame() {
        isGameRunning = true;
        isGameFinished = false;
        startTime = System.currentTimeMillis();
        btnStartGame.setVisibility(View.GONE);
        btnFinishGame.setVisibility(View.VISIBLE);
        btnFinishGame.setText(currentLanguage.equals("en") ? "Finish" : "پایان");
        btnBackToMenuBottom.setVisibility(View.GONE);
        layoutAnswer.setVisibility(View.GONE);
        tvResult.setText("");
        tvResultDetail.setText("");
        elapsedTime = 0;
        updateTimer();

        switch (currentGame) {
            case "maze": startMazeGame(); break;
            case "numbers": startNumbersGame(); break;
            case "word_count": startWordCountGame(); break;
            case "common_image": startCommonImageGame(); break;
        }
    }

    private void backToMenu() {
        isGameRunning = false;
        timerHandler.removeCallbacksAndMessages(null);
        resetGame();
        showGameMenu();
        btnBackToMenu.setVisibility(View.GONE);
    }

    // ============================================================
    // ========== بازی ۱: ماز (Maze) ==========
    // ============================================================
    private void startMazeGame() {
        try {
            int levelIndex = Math.min((currentLevel - 1) / 3, 3);
            String levelFolder = mazeLevels[levelIndex];

            Random rand = new Random();
            int gameIndex = rand.nextInt(3) + 1;

            String questionPath = "games/maze/" + levelFolder + "/question/game" + gameIndex + ".jpg";
            InputStream questionStream = getContext().getAssets().open(questionPath);
            Bitmap questionBitmap = BitmapFactory.decodeStream(questionStream);
            questionStream.close();
            ivGameDisplay.setImageBitmap(questionBitmap);

            String answerPath = "games/maze/" + levelFolder + "/answer/game" + gameIndex + ".jpg";
            InputStream answerStream = getContext().getAssets().open(answerPath);
            Bitmap answerBitmap = BitmapFactory.decodeStream(answerStream);
            answerStream.close();
            ivGameAnswer.setImageBitmap(answerBitmap);

            tvInstruction.setText(currentLanguage.equals("en") ?
                    "Find the path from start (S) to finish (F) in your mind" :
                    "مسیر را از شروع (S) تا پایان (F) در ذهن پیدا کن");

        } catch (IOException e) {
            e.printStackTrace();
            tvResult.setText("Error loading maze image");
        }
    }

    // ============================================================
    // ========== بازی ۲: اعداد (Number Hunt) ==========
    // ============================================================
    private void startNumbersGame() {
        numberGridSize = 7;
        Bitmap numberBitmap = createNumberGridBitmap(numberGridSize);
        ivGameDisplay.setImageBitmap(numberBitmap);
        ivGameAnswer.setImageBitmap(null);

        tvInstruction.setText(currentLanguage.equals("en") ?
                "Find numbers 1 to 49 in order in your mind" :
                "اعداد ۱ تا ۴۹ را به ترتیب در ذهن پیدا کن");
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

    // ============================================================
    // ========== بازی ۳: شمارش کلمه (Word Counter) ==========
    // ============================================================
    private void startWordCountGame() {
        String[] words = {
                "کتاب", "خواندن", "تندخوانی", "تمرین", "مطالعه", "یادگیری",
                "سرعت", "درک", "مطلب", "چشم", "حرکت", "تمرکز"
        };

        Random rand = new Random();
        targetWord = words[rand.nextInt(words.length)];
        targetCount = 0;

        StringBuilder textBuilder = new StringBuilder();
        for (int i = 0; i < 80 + currentLevel * 10; i++) {
            String word = words[rand.nextInt(words.length)];
            textBuilder.append(word).append(" ");
            if (word.equals(targetWord)) {
                targetCount++;
            }
        }
        int extraRepeats = 3 + currentLevel * 2;
        for (int i = 0; i < extraRepeats; i++) {
            textBuilder.append(targetWord).append(" ");
            targetCount++;
        }
        fullText = textBuilder.toString();

        Bitmap textBitmap = createTextBitmap(fullText);
        ivGameDisplay.setImageBitmap(textBitmap);
        ivGameAnswer.setImageBitmap(null);

        tvInstruction.setText(currentLanguage.equals("en") ?
                "Count how many times '" + targetWord + "' appears in the text" :
                "تعداد تکرار کلمه '" + targetWord + "' را در متن پیدا کن");
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

    // ============================================================
    // ========== بازی ۴: پیدا کردن عکس مشترک (Common Image) ==========
    // ============================================================
    private void startCommonImageGame() {
        Random rand = new Random();
        commonImageIndex = rand.nextInt(imageIcons.length);

        Bitmap combinedBitmap = createCommonImageBitmap();
        ivGameDisplay.setImageBitmap(combinedBitmap);
        ivGameAnswer.setImageBitmap(null);

        tvInstruction.setText(currentLanguage.equals("en") ?
                "Find the image that appears in both sets (top and bottom)" :
                "عکسی که در هر دو مجموعه (بالا و پایین) وجود دارد را پیدا کن");
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

        // ایجاد تصویر ترکیبی
        int cellSize = 80;
        int padding = 10;
        int cols = 3;
        int rows = 3;
        int width = cols * cellSize + padding * 2;
        int height = rows * cellSize + padding * 2;

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

        String label1 = currentLanguage.equals("en") ? "Set 1" : "مجموعه ۱";
        String label2 = currentLanguage.equals("en") ? "Set 2" : "مجموعه ۲";
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

            Bitmap iconBitmap = BitmapFactory.decodeResource(getResources(), imageIcons[imageSet[i]]);
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

    // ============================================================
    // ========== متدهای عمومی بازی ==========
    // ============================================================
    private void finishGame() {
        if (!isGameRunning) return;
        isGameRunning = false;
        isGameFinished = true;
        elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0;

        btnFinishGame.setVisibility(View.GONE);
        btnNextLevel.setVisibility(View.VISIBLE);
        btnNextLevel.setText(currentLanguage.equals("en") ? "Next Level" : "سطح بعدی");
        btnResetGame.setVisibility(View.VISIBLE);
        btnResetGame.setText(currentLanguage.equals("en") ? "Reset" : "شروع مجدد");
        btnBackToMenuBottom.setVisibility(View.VISIBLE);
        btnBackToMenuBottom.setText(currentLanguage.equals("en") ? "🔙 Back to Games Menu" : "🔙 برگشت به منوی بازی‌ها");

        // نمایش جواب
        layoutAnswer.setVisibility(View.VISIBLE);

        // ذخیره نتیجه
        GameModel result = new GameModel(currentGame, getGameName(currentGame), currentLevel, elapsedTime);
        GamePreferencesHelper.addGameResult(getContext(), result);

        // نمایش نتیجه
        layoutResult.setVisibility(View.VISIBLE);
        String resultText = currentLanguage.equals("en") ?
                "✅ Time: " + String.format("%.2f", elapsedTime) + " seconds" :
                "✅ زمان: " + String.format("%.2f", elapsedTime) + " ثانیه";
        tvResultDetail.setText(resultText);
        tvResult.setText("");

        // به‌روزرسانی چارت و جدول
        setupChartAndHistory();
        updateBestTime();
    }

    private void nextLevel() {
        if (currentLevel < maxLevel) {
            currentLevel++;
            resetGame();
            showGameInfo();
            startGame();
        } else {
            String msg = currentLanguage.equals("en") ?
                    "🎉 Congratulations! You completed all levels!" :
                    "🎉 تبریک! همه سطوح را کامل کردی!";
            tvResult.setText(msg);
            btnNextLevel.setVisibility(View.GONE);
        }
    }

    private void resetGame() {
        isGameRunning = false;
        isGameFinished = false;
        ivGameDisplay.setImageBitmap(null);
        ivGameAnswer.setImageBitmap(null);
        tvResult.setText("");
        tvResultDetail.setText("");
        tvTimer.setText("⏱ 00:00");
        btnStartGame.setVisibility(View.VISIBLE);
        btnFinishGame.setVisibility(View.GONE);
        btnNextLevel.setVisibility(View.GONE);
        btnResetGame.setVisibility(View.GONE);
        btnBackToMenuBottom.setVisibility(View.GONE);
        layoutAnswer.setVisibility(View.GONE);
        layoutResult.setVisibility(View.GONE);
        btnStartGame.setText(currentLanguage.equals("en") ? "Start Game" : "شروع بازی");
        tvInstruction.setText(getGameInstruction(currentGame));
    }

    private void updateTimer() {
        if (!isGameRunning) return;
        long elapsed = System.currentTimeMillis() - startTime;
        double seconds = elapsed / 1000.0;
        tvTimer.setText(String.format("⏱ %05.2f", seconds));
        timerHandler.postDelayed(this::updateTimer, 50);
    }

    private void updateBestTime() {
        tvBestTime.setText(getBestTimeText() + ": " + getBestTime());
    }

    // ============================================================
    // ========== چارت و تاریخچه ==========
    // ============================================================
    private void setupChartAndHistory() {
        if (getContext() == null || currentGame.isEmpty()) return;

        List<GameModel> history = GamePreferencesHelper.loadGameHistory(getContext(), currentGame);

        if (history.isEmpty()) {
            chartProgress.setVisibility(View.GONE);
            rvHistory.setVisibility(View.GONE);
            return;
        }

        chartProgress.setVisibility(View.VISIBLE);
        rvHistory.setVisibility(View.VISIBLE);
        setupLineChart(history);
        historyAdapter.updateData(history);
    }

    private void setupLineChart(List<GameModel> history) {
        List<Entry> entries = new ArrayList<>();
        double maxTime = 0;

        for (GameModel game : history) {
            if (game.getLevel() <= 10) {
                entries.add(new Entry(game.getLevel(), (float) game.getTime()));
                if (game.getTime() > maxTime) maxTime = game.getTime();
            }
        }

        if (entries.isEmpty()) {
            chartProgress.setVisibility(View.GONE);
            return;
        }

        LineDataSet dataSet = new LineDataSet(entries, currentLanguage.equals("en") ? "Time (s)" : "زمان (ثانیه)");
        dataSet.setColor(Color.parseColor("#4A6CF7"));
        dataSet.setCircleColor(Color.parseColor("#4A6CF7"));
        dataSet.setCircleRadius(4f);
        dataSet.setLineWidth(2.5f);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.parseColor("#6B7A8F"));
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#4A6CF7"));
        dataSet.setFillAlpha(50);
        dataSet.setDrawValues(true);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);

        LineData lineData = new LineData(dataSets);
        chartProgress.setData(lineData);

        XAxis xAxis = chartProgress.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.parseColor("#6B7A8F"));
        xAxis.setTextSize(10f);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(11);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "Lv." + (int) value;
            }
        });

        YAxis yAxisLeft = chartProgress.getAxisLeft();
        yAxisLeft.setTextColor(Color.parseColor("#6B7A8F"));
        yAxisLeft.setTextSize(10f);
        yAxisLeft.setDrawGridLines(true);
        yAxisLeft.setGridColor(Color.parseColor("#EEF3FC"));
        yAxisLeft.setAxisMinimum(0);
        yAxisLeft.setAxisMaximum((float) (maxTime + 5));

        YAxis yAxisRight = chartProgress.getAxisRight();
        yAxisRight.setEnabled(false);

        chartProgress.getDescription().setEnabled(false);
        chartProgress.setTouchEnabled(true);
        chartProgress.setDragEnabled(true);
        chartProgress.setScaleEnabled(true);
        chartProgress.setPinchZoom(true);
        chartProgress.setBackgroundColor(Color.WHITE);
        chartProgress.setExtraOffsets(10, 10, 10, 10);
        chartProgress.invalidate();
    }

    public void onLanguageChanged(String language) {
        if (!isViewReady) return;
        this.currentLanguage = language;

        if (!currentGame.isEmpty()) {
            tvGameTitle.setText(getGameName(currentGame));
            tvInstruction.setText(getGameInstruction(currentGame));
            tvLevel.setText(getLevelText() + " " + currentLevel + "/" + maxLevel);
            tvBestTime.setText(getBestTimeText() + ": " + getBestTime());
            btnStartGame.setText(currentLanguage.equals("en") ? "Start Game" : "شروع بازی");
            btnBackToMenu.setText("←");
            btnBackToMenuBottom.setText(currentLanguage.equals("en") ? "🔙 Back to Games Menu" : "🔙 برگشت به منوی بازی‌ها");
            if (btnFinishGame.getVisibility() == View.VISIBLE) {
                btnFinishGame.setText(currentLanguage.equals("en") ? "Finish" : "پایان");
            }
            if (btnNextLevel.getVisibility() == View.VISIBLE) {
                btnNextLevel.setText(currentLanguage.equals("en") ? "Next Level" : "سطح بعدی");
            }
            if (btnResetGame.getVisibility() == View.VISIBLE) {
                btnResetGame.setText(currentLanguage.equals("en") ? "Reset" : "شروع مجدد");
            }
            setupChartAndHistory();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isViewReady && !currentGame.isEmpty()) {
            setupChartAndHistory();
        }
    }
}