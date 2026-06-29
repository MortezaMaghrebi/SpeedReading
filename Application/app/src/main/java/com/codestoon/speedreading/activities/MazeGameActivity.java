package com.codestoon.speedreading.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codestoon.speedreading.R;
import com.codestoon.speedreading.adapters.GameHistoryAdapter;
import com.codestoon.speedreading.models.GameModel;
import com.codestoon.speedreading.utils.GamePreferencesHelper;
import com.codestoon.speedreading.games.maze.MazeGenerator;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

public class MazeGameActivity extends AppCompatActivity {

    // ====== ویوها ======
    private TextView tvGameTitle, tvDifficulty, tvTimer, tvInstruction, tvResult, tvResultDetail;
    private TextView tvChartTitle, tvHistoryTitle, tvNoData, tvNoHistory;
    private ImageView ivMaze;
    private Button btnStart, btnFinish, btnRetry;
    private LinearLayout layoutResult, layoutChart;
    private LineChart chartProgress;
    private RecyclerView rvHistory;
    private GameHistoryAdapter historyAdapter;

    // ====== متغیرهای بازی ======
    private String currentLanguage = "fa";
    private String difficultyId;
    private String difficultyName;
    private int difficultyLevel;
    private String gameTitle;

    private boolean isRunning = false;
    private boolean isFinished = false;
    private Handler timerHandler = new Handler();
    private long startTime = 0;
    private double elapsedTime = 0;

    // ====== متغیرهای ماز ======
    private Bitmap defaultMazeBitmap;
    private Bitmap currentQuestionBitmap;
    private Bitmap currentAnswerBitmap;

    // ====== محدودیت‌ها ======
    private static final int MAX_HISTORY_TABLE_ITEMS = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maze_game);

        initViews();
        getIntentData();
        setupUI();
        loadDefaultMaze();
        setupChartAndHistory();

        btnStart.setOnClickListener(v -> startGame());
        btnFinish.setOnClickListener(v -> finishGame());
        btnRetry.setOnClickListener(v -> retryGame());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void initViews() {
        tvGameTitle = findViewById(R.id.tvGameTitle);
        tvDifficulty = findViewById(R.id.tvDifficulty);
        tvTimer = findViewById(R.id.tvTimer);
        tvInstruction = findViewById(R.id.tvInstruction);
        tvResult = findViewById(R.id.tvResult);
        tvResultDetail = findViewById(R.id.tvResultDetail);
        tvChartTitle = findViewById(R.id.tvChartTitle);
        tvHistoryTitle = findViewById(R.id.tvHistoryTitle);
        tvNoData = findViewById(R.id.tvNoData);
        tvNoHistory = findViewById(R.id.tvNoHistory);
        ivMaze = findViewById(R.id.ivMaze);
        btnStart = findViewById(R.id.btnStart);
        btnFinish = findViewById(R.id.btnFinish);
        btnRetry = findViewById(R.id.btnRetry);
        layoutResult = findViewById(R.id.layoutResult);
        layoutChart = findViewById(R.id.layoutChart);
        chartProgress = findViewById(R.id.chartProgress);
        rvHistory = findViewById(R.id.rvHistory);

        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new GameHistoryAdapter(new ArrayList<>());
        rvHistory.setAdapter(historyAdapter);
    }

    private void getIntentData() {
        difficultyId = getIntent().getStringExtra("difficulty_id");
        difficultyLevel = getIntent().getIntExtra("difficulty_level", 1);
        difficultyName = getIntent().getStringExtra("difficulty_name");
        gameTitle = getIntent().getStringExtra("game_title");
    }

    private void setupUI() {
        tvGameTitle.setText(gameTitle);
        tvDifficulty.setText(difficultyName);
        tvTimer.setText("⏱ 00:00");
        tvInstruction.setText(getInstructionText());
        tvResult.setText("");
        tvResultDetail.setText("");

        btnStart.setVisibility(View.VISIBLE);
        btnStart.setText(getStartText());
        btnFinish.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);

        layoutResult.setVisibility(View.GONE);
        layoutChart.setVisibility(View.GONE);
    }

    private String getInstructionText() {
        if (currentLanguage.equals("en")) {
            return "Find the path from start (S) to finish (F), then press Finish";
        } else {
            return "مسیر را از شروع (S) تا پایان (F) پیدا کن، سپس دکمه پایان را بزن";
        }
    }

    private String getStartText() {
        return currentLanguage.equals("en") ? "Start" : "شروع";
    }

    private String getFinishText() {
        return currentLanguage.equals("en") ? "Finish" : "پایان";
    }

    private String getRetryText() {
        return currentLanguage.equals("en") ? "Retry" : "تست مجدد";
    }

    private String getResetText() {
        return currentLanguage.equals("en") ? "Reset" : "شروع مجدد";
    }

    private String getResultText(double time) {
        if (currentLanguage.equals("en")) {
            return "✅ Time: " + String.format("%.2f", time) + " seconds";
        } else {
            return "✅ زمان: " + String.format("%.2f", time) + " ثانیه";
        }
    }

    /**
     * بارگذاری تصویر دیفالت
     */
    private void loadDefaultMaze() {
        if (defaultMazeBitmap == null || defaultMazeBitmap.isRecycled()) {
            defaultMazeBitmap = MazeGenerator.generateDefaultMazeImage();
        }
        ivMaze.setImageBitmap(defaultMazeBitmap);
    }

    /**
     * شروع بازی
     */
    private void startGame() {
        if (isRunning) return;

        isRunning = true;
        isFinished = false;
        startTime = System.currentTimeMillis();

        // تولید ماز جدید
        MazeGenerator.MazeResult result = MazeGenerator.generateMazeWithResult(difficultyLevel);
        currentQuestionBitmap = result.questionImage;
        currentAnswerBitmap = result.answerImage;

        ivMaze.setImageBitmap(currentQuestionBitmap);

        btnStart.setVisibility(View.GONE);
        btnFinish.setVisibility(View.VISIBLE);
        btnFinish.setText(getFinishText());
        btnRetry.setVisibility(View.GONE);
        layoutResult.setVisibility(View.GONE);
        layoutChart.setVisibility(View.GONE);
        tvResult.setText("");
        tvResultDetail.setText("");
        tvInstruction.setText(getInstructionText());

        updateTimer();
    }

    /**
     * پایان بازی - نمایش جواب
     */
    private void finishGame() {
        if (!isRunning) return;

        isRunning = false;
        isFinished = true;
        elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0;

        // نمایش جواب همان ماز
        if (currentAnswerBitmap != null && !currentAnswerBitmap.isRecycled()) {
            ivMaze.setImageBitmap(currentAnswerBitmap);
        }

        btnFinish.setVisibility(View.GONE);
        btnRetry.setVisibility(View.VISIBLE);
        btnRetry.setText(getRetryText());
        //btnReset.setVisibility(View.VISIBLE);
        //btnReset.setText(getResetText());

        // ذخیره نتیجه
        GameModel result = new GameModel("maze", gameTitle, difficultyLevel, elapsedTime);
        GamePreferencesHelper.addGameResult(this, result);

        // نمایش نتیجه
        layoutResult.setVisibility(View.VISIBLE);
        tvResultDetail.setText(getResultText(elapsedTime));
        tvResult.setText("");

        setupChartAndHistory();
        stopTimer();
    }

    /**
     * تست مجدد
     */
    private void retryGame() {
        isFinished = false;
        isRunning = false;
        elapsedTime = 0;

        btnRetry.setVisibility(View.GONE);
        layoutResult.setVisibility(View.GONE);
        layoutChart.setVisibility(View.GONE);
        tvResult.setText("");
        tvResultDetail.setText("");

        // تولید ماز جدید
        //MazeGenerator.MazeResult result = MazeGenerator.generateMazeWithResult(difficultyLevel);
        //currentQuestionBitmap = result.questionImage;
        //currentAnswerBitmap = result.answerImage;
        //ivMaze.setImageBitmap(currentQuestionBitmap);
        loadDefaultMaze();

        btnStart.setVisibility(View.VISIBLE);
        btnStart.setText(getStartText());
        tvInstruction.setText(getInstructionText());
        tvTimer.setText("⏱ 00:00");
    }


    private void updateTimer() {
        if (!isRunning) return;
        long elapsed = System.currentTimeMillis() - startTime;
        double seconds = elapsed / 1000.0;
        tvTimer.setText(String.format("⏱ %05.2f", seconds));
        timerHandler.postDelayed(this::updateTimer, 50);
    }

    private void stopTimer() {
        timerHandler.removeCallbacksAndMessages(null);
    }

    private void recycleMazeBitmaps() {
        if (currentQuestionBitmap != null && !currentQuestionBitmap.isRecycled()) {
            currentQuestionBitmap.recycle();
            currentQuestionBitmap = null;
        }
        if (currentAnswerBitmap != null && !currentAnswerBitmap.isRecycled()) {
            currentAnswerBitmap.recycle();
            currentAnswerBitmap = null;
        }
    }

    // ============================================================
    // ========== چارت و تاریخچه ==========
    // ============================================================
    private void setupChartAndHistory() {
        List<GameModel> history = GamePreferencesHelper.loadGameHistory(this, "maze");

        List<GameModel> filteredHistory = new ArrayList<>();
        for (GameModel game : history) {
            if (game.getLevel() == difficultyLevel) {
                filteredHistory.add(game);
            }
        }

        if (filteredHistory.isEmpty()) {
            chartProgress.setVisibility(View.GONE);
            tvNoData.setVisibility(View.VISIBLE);
            rvHistory.setVisibility(View.GONE);
            tvNoHistory.setVisibility(View.VISIBLE);

            if (currentLanguage.equals("en")) {
                tvNoData.setText("No data for this difficulty");
                tvNoHistory.setText("No history for this difficulty");
            } else {
                tvNoData.setText("داده‌ای برای این سطح سختی وجود ندارد");
                tvNoHistory.setText("تاریخچه‌ای برای این سطح سختی وجود ندارد");
            }
            return;
        }

        chartProgress.setVisibility(View.VISIBLE);
        tvNoData.setVisibility(View.GONE);
        rvHistory.setVisibility(View.VISIBLE);
        tvNoHistory.setVisibility(View.GONE);

        int startIndex = Math.max(0, filteredHistory.size() - MAX_HISTORY_TABLE_ITEMS);
        List<GameModel> displayHistory = filteredHistory.subList(startIndex, filteredHistory.size());

        setupLineChart(displayHistory);
        historyAdapter.updateData(displayHistory);
        updateChartAndHistoryTitles();
    }

    private void setupLineChart(List<GameModel> history) {
        List<Entry> entries = new ArrayList<>();
        double maxTime = 0;

        for (int i = 0; i < history.size(); i++) {
            GameModel game = history.get(i);
            entries.add(new Entry(i, (float) game.getTime()));
            if (game.getTime() > maxTime) maxTime = game.getTime();
        }

        if (entries.isEmpty()) {
            chartProgress.setVisibility(View.GONE);
            tvNoData.setVisibility(View.VISIBLE);
            return;
        }

        String label = currentLanguage.equals("en") ? "Time (s)" : "زمان (ثانیه)";
        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(getResources().getColor(R.color.primary_blue));
        dataSet.setCircleColor(getResources().getColor(R.color.primary_blue));
        dataSet.setCircleRadius(4f);
        dataSet.setLineWidth(2.5f);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(getResources().getColor(R.color.text_secondary));
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(getResources().getColor(R.color.primary_blue));
        dataSet.setFillAlpha(50);
        dataSet.setDrawValues(true);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);

        LineData lineData = new LineData(dataSets);
        chartProgress.setData(lineData);

        XAxis xAxis = chartProgress.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(getResources().getColor(R.color.text_secondary));
        xAxis.setTextSize(10f);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "#" + (int) (value + 1);
            }
        });

        YAxis yAxisLeft = chartProgress.getAxisLeft();
        yAxisLeft.setTextColor(getResources().getColor(R.color.text_secondary));
        yAxisLeft.setTextSize(10f);
        yAxisLeft.setDrawGridLines(true);
        yAxisLeft.setGridColor(getResources().getColor(R.color.border_light));
        yAxisLeft.setAxisMinimum(0);
        yAxisLeft.setAxisMaximum((float) (maxTime + 5));

        YAxis yAxisRight = chartProgress.getAxisRight();
        yAxisRight.setEnabled(false);

        chartProgress.getDescription().setEnabled(false);
        chartProgress.setTouchEnabled(true);
        chartProgress.setDragEnabled(true);
        chartProgress.setScaleEnabled(true);
        chartProgress.setPinchZoom(true);
        chartProgress.setBackgroundColor(getResources().getColor(R.color.white));
        chartProgress.setExtraOffsets(10, 10, 10, 10);
        chartProgress.invalidate();
    }

    private void updateChartAndHistoryTitles() {
        String difficulty = currentLanguage.equals("en") ? difficultyName : difficultyName;
        String level = currentLanguage.equals("en") ? "Level" : "سطح";

        if (currentLanguage.equals("en")) {
            tvChartTitle.setText("Maze Progress - " + difficulty + " (" + level + " " + difficultyLevel + ")");
            tvHistoryTitle.setText("Maze History - " + difficulty + " (" + level + " " + difficultyLevel + ")");
        } else {
            tvChartTitle.setText("پیشرفت ماز - " + difficulty + " (" + level + " " + difficultyLevel + ")");
            tvHistoryTitle.setText("تاریخچه ماز - " + difficulty + " (" + level + " " + difficultyLevel + ")");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();

        if (defaultMazeBitmap != null && !defaultMazeBitmap.isRecycled()) {
            defaultMazeBitmap.recycle();
            defaultMazeBitmap = null;
        }
        recycleMazeBitmaps();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFinished) {
            setupChartAndHistory();
        }
    }
}