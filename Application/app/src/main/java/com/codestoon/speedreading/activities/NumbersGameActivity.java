package com.codestoon.speedreading.activities;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
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
import com.codestoon.speedreading.games.numbers.NumberGenerator;
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

public class NumbersGameActivity extends AppCompatActivity {

    // ====== ویوها ======
    private TextView tvGameTitle, tvDifficulty, tvTimer, tvInstruction, tvResult, tvResultDetail;
    private TextView tvChartTitle, tvHistoryTitle, tvNoData, tvNoHistory;
    private ImageView ivNumbers;
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
    private String gameId;

    private boolean isRunning = false;
    private boolean isFinished = false;
    private Handler timerHandler = new Handler();
    private long startTime = 0;
    private double elapsedTime = 0;

    // ====== متغیرهای اعداد ======
    private Bitmap defaultNumberBitmap;
    private Bitmap currentQuestionBitmap;
    private Bitmap currentAnswerBitmap;

    // ====== محدودیت‌ها ======
    private static final int MAX_HISTORY_TABLE_ITEMS = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_numbers_game);

        initViews();
        getIntentData();
        applyLanguage();
        setupUI();
        loadDefaultNumbers();

        showChartAndHistory();

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
        ivNumbers = findViewById(R.id.ivNumbers);
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

        // تنظیم مربعی شدن ImageView
        ivNumbers.post(() -> {
            int width = ivNumbers.getWidth();
            int height = ivNumbers.getHeight();
            if (width > 0 && height > 0) {
                ViewGroup.LayoutParams params = ivNumbers.getLayoutParams();
                int min = Math.min(width, height);
                params.height = min;
                params.width = min;
                ivNumbers.setLayoutParams(params);
            }
        });
    }

    private void getIntentData() {
        difficultyId = getIntent().getStringExtra("difficulty_id");
        difficultyLevel = getIntent().getIntExtra("difficulty_level", 1);
        difficultyName = getIntent().getStringExtra("difficulty_name");
        gameTitle = getIntent().getStringExtra("game_title");
        gameId = getIntent().getStringExtra("game_id");

        String language = getIntent().getStringExtra("language");
        if (language != null && !language.isEmpty()) {
            currentLanguage = language;
        }
    }

    private void setupUI() {
        tvGameTitle.setText(gameTitle);
        tvDifficulty.setText(difficultyName);
        tvTimer.setText("⏱ 00:00");
        tvResult.setText("");
        tvResultDetail.setText("");
        tvInstruction.setText(getInstructionText());

        btnStart.setVisibility(View.VISIBLE);
        btnStart.setText(getStartText());
        btnFinish.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);

        layoutResult.setVisibility(View.GONE);
        layoutChart.setVisibility(View.VISIBLE);
    }

    private void applyLanguage() {
        if (currentLanguage.equals("fa")) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        } else {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
    }

    private String getInstructionText() {
        if (currentLanguage.equals("en")) {
            return "Find the numbers 1 to N in order with your eyes, then press Finish";
        } else {
            return "اعداد ۱ تا N را به ترتیب با چشم پیدا کن، سپس دکمه پایان را بزن";
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

    private String getResultText(double time) {
        if (currentLanguage.equals("en")) {
            return "✅ Time: " + String.format("%.2f", time) + " seconds";
        } else {
            return "✅ زمان: " + String.format("%.2f", time) + " ثانیه";
        }
    }

    private String getNoDataText() {
        if (currentLanguage.equals("en")) {
            return "No data for this difficulty";
        } else {
            return "داده‌ای برای این سطح سختی وجود ندارد";
        }
    }

    private String getNoHistoryText() {
        if (currentLanguage.equals("en")) {
            return "No history for this difficulty";
        } else {
            return "تاریخچه‌ای برای این سطح سختی وجود ندارد";
        }
    }



    private void startGame() {
        if (isRunning) return;

        isRunning = true;
        isFinished = false;
        startTime = System.currentTimeMillis();

        NumberGenerator.NumberResult result = NumberGenerator.generateNumberWithResult(difficultyLevel);
        currentQuestionBitmap = result.questionImage;
        currentAnswerBitmap = result.answerImage;

        ivNumbers.setImageBitmap(currentQuestionBitmap);

        btnStart.setVisibility(View.GONE);
        btnFinish.setVisibility(View.VISIBLE);
        btnFinish.setText(getFinishText());
        btnRetry.setVisibility(View.GONE);
        layoutResult.setVisibility(View.GONE);
        tvResult.setText("");
        tvResultDetail.setText("");
        tvInstruction.setText(getInstructionText());

        layoutChart.setVisibility(View.VISIBLE);

        updateTimer();
    }

    private void finishGame() {
        if (!isRunning) return;

        isRunning = false;
        isFinished = true;
        elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0;

        if (currentAnswerBitmap != null && !currentAnswerBitmap.isRecycled()) {
            ivNumbers.setImageBitmap(currentAnswerBitmap);
        }

        btnFinish.setVisibility(View.GONE);
        btnRetry.setVisibility(View.VISIBLE);
        btnRetry.setText(getRetryText());

        GameModel result = new GameModel("numbers", gameTitle, difficultyLevel, elapsedTime);
        GamePreferencesHelper.addGameResult(this, result);

        layoutResult.setVisibility(View.VISIBLE);
        tvResultDetail.setText(getResultText(elapsedTime));
        tvResult.setText("");

        showChartAndHistory();
        stopTimer();
    }

    private void retryGame() {
        isFinished = false;
        isRunning = false;
        elapsedTime = 0;

        btnRetry.setVisibility(View.GONE);
        layoutResult.setVisibility(View.GONE);
        tvResult.setText("");
        tvResultDetail.setText("");

        loadDefaultNumbers();

        btnStart.setVisibility(View.VISIBLE);
        btnStart.setText(getStartText());
        tvInstruction.setText(getInstructionText());
        tvTimer.setText("⏱ 00:00");

        layoutChart.setVisibility(View.VISIBLE);
        showChartAndHistory();
    }

    private void loadDefaultNumbers() {
        if (defaultNumberBitmap == null || defaultNumberBitmap.isRecycled()) {
            int total = getTotalNumbers(difficultyLevel);
            defaultNumberBitmap = NumberGenerator.generateDefaultNumberImage(total);
        }
        ivNumbers.setImageBitmap(defaultNumberBitmap);
    }

    private int getTotalNumbers(int level) {
        switch (level) {
            case 1: return 16;  // آسان
            case 2: return 25;  // متوسط
            case 3: return 36;  // سخت
            case 4: return 49;  // خیلی سخت
            default: return 16;
        }
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

    private void recycleNumberBitmaps() {
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
    private void showChartAndHistory() {
        layoutChart.setVisibility(View.VISIBLE);
        setupChartAndHistory();
    }

    private void setupChartAndHistory() {
        List<GameModel> history = GamePreferencesHelper.loadGameHistory(this, "numbers");

        List<GameModel> filteredHistory = new ArrayList<>();
        for (GameModel game : history) {
            if (game.getLevel() == difficultyLevel) {
                filteredHistory.add(game);
            }
        }

        int startIndex = Math.max(0, filteredHistory.size() - MAX_HISTORY_TABLE_ITEMS);
        List<GameModel> displayHistory = filteredHistory.subList(startIndex, filteredHistory.size());

        if (displayHistory.isEmpty()) {
            chartProgress.setVisibility(View.GONE);
            tvNoData.setVisibility(View.VISIBLE);
            rvHistory.setVisibility(View.GONE);
            tvNoHistory.setVisibility(View.VISIBLE);

            tvNoData.setText(getNoDataText());
            tvNoHistory.setText(getNoHistoryText());

            updateChartAndHistoryTitles();
            return;
        }

        chartProgress.setVisibility(View.VISIBLE);
        tvNoData.setVisibility(View.GONE);
        rvHistory.setVisibility(View.VISIBLE);
        tvNoHistory.setVisibility(View.GONE);

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
        xAxis.setAxisMinimum(0);
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
        String levelText = currentLanguage.equals("en") ? "Level" : "سطح";
        String chartTitle, historyTitle;

        if (currentLanguage.equals("en")) {
            chartTitle = "Numbers Game Time - " + difficultyName + " (" + levelText + " " + difficultyLevel + ")";
            historyTitle = "Numbers History - " + difficultyName + " (" + levelText + " " + difficultyLevel + ")";
        } else {
            chartTitle = "زمان بازی اعداد - " + difficultyName + " (" + levelText + " " + difficultyLevel + ")";
            historyTitle = "تاریخچه اعداد - " + difficultyName + " (" + levelText + " " + difficultyLevel + ")";
        }

        tvChartTitle.setText(chartTitle);
        tvHistoryTitle.setText(historyTitle);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();

        if (defaultNumberBitmap != null && !defaultNumberBitmap.isRecycled()) {
            defaultNumberBitmap.recycle();
            defaultNumberBitmap = null;
        }
        recycleNumberBitmaps();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showChartAndHistory();
    }
}