package com.codestoon.speedreading.activities;

import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codestoon.speedreading.R;
import com.codestoon.speedreading.adapters.GameHistoryAdapter;
import com.codestoon.speedreading.models.GameModel;
import com.codestoon.speedreading.utils.GamePreferencesHelper;
import com.codestoon.speedreading.games.wordcount.WordCountGenerator;
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

public class WordCountGameActivity extends AppCompatActivity {

    // ====== ویوها ======
    private TextView tvGameTitle, tvDifficulty, tvTimer, tvInstruction, tvResultDetail;
    private TextView tvChartTitle, tvHistoryTitle, tvNoData, tvNoHistory;
    private TextView tvTargetWord, tvTextDisplay;
    private Button btnStart, btnFinish, btnRetry;
    private LinearLayout layoutResult, layoutChart, layoutTargetWord;
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

    // ====== متغیرهای بازی ======
    private String targetWord;
    private int targetCount;
    private String fullText;

    // ====== محدودیت‌ها ======
    private static final int MAX_HISTORY_TABLE_ITEMS = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_count_game);

        initViews();
        getIntentData();
        applyLanguage();
        setupUI();

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
        tvResultDetail = findViewById(R.id.tvResultDetail);
        tvChartTitle = findViewById(R.id.tvChartTitle);
        tvHistoryTitle = findViewById(R.id.tvHistoryTitle);
        tvNoData = findViewById(R.id.tvNoData);
        tvNoHistory = findViewById(R.id.tvNoHistory);
        tvTargetWord = findViewById(R.id.tvTargetWord);
        tvTextDisplay = findViewById(R.id.tvTextDisplay);
        btnStart = findViewById(R.id.btnStart);
        btnFinish = findViewById(R.id.btnFinish);
        btnRetry = findViewById(R.id.btnRetry);
        layoutResult = findViewById(R.id.layoutResult);
        layoutChart = findViewById(R.id.layoutChart);
        layoutTargetWord = findViewById(R.id.layoutTargetWord);
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
        tvResultDetail.setText("");
        tvInstruction.setText(getInstructionText());
        tvTargetWord.setText("");
        tvTextDisplay.setText("");

        layoutTargetWord.setVisibility(View.GONE);

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
            return "Find and count the target word, then press Finish";
        } else {
            return "کلمه هدف را پیدا کن و شمارش کن، سپس دکمه پایان را بزن";
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

    /**
     * حذف نیم‌فاصله از کلمه برای نمایش
     */
    private String removeHalfSpace(String word) {
        if (word == null) return "";
        return word.replace("\u200C", "");
    }

    private void startGame() {
        if (isRunning) return;

        isRunning = true;
        isFinished = false;
        startTime = System.currentTimeMillis();

        // تولید محتوای بازی
        WordCountGenerator.WordCountResult result = WordCountGenerator.generateWordCountResult(
                this, currentLanguage, difficultyLevel
        );
        targetWord = result.targetWord;
        targetCount = result.targetCount;
        fullText = result.fullText;

        // نمایش متن سوال
        tvTextDisplay.setVisibility(View.VISIBLE);
        tvTextDisplay.setText(fullText);



        // نمایش کلمه هدف (بدون نیم‌فاصله)
        layoutTargetWord.setVisibility(View.VISIBLE);
        String targetLabel = currentLanguage.equals("en") ? "Target Word: " : "کلمه هدف: ";
        String cleanTargetWord = removeHalfSpace(targetWord);
        tvTargetWord.setText(targetLabel + cleanTargetWord);

        btnStart.setVisibility(View.GONE);
        btnFinish.setVisibility(View.VISIBLE);
        btnFinish.setText(getFinishText());
        btnRetry.setVisibility(View.GONE);
        layoutResult.setVisibility(View.GONE);

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

        // نمایش متن با هایلایت
        String highlightedText = WordCountGenerator.getHighlightedText(fullText, targetWord);
        tvTextDisplay.setText(Html.fromHtml(highlightedText));

        btnFinish.setVisibility(View.GONE);
        btnRetry.setVisibility(View.VISIBLE);
        btnRetry.setText(getRetryText());

        GameModel result = new GameModel("word_count", gameTitle, difficultyLevel, elapsedTime);
        GamePreferencesHelper.addGameResult(this, result);

        // نمایش نتیجه
        layoutResult.setVisibility(View.VISIBLE);
        String cleanTargetWord = removeHalfSpace(targetWord);
        String resultText =
                (currentLanguage.equals("en") ? "Count: " : "تعداد: ") + cleanTargetWord + " = " + targetCount
                        + "\n" + getResultText(elapsedTime);
        tvResultDetail.setText(resultText);

        showChartAndHistory();
        stopTimer();
    }

    private void retryGame() {
        isFinished = false;
        isRunning = false;
        elapsedTime = 0;

        btnRetry.setVisibility(View.GONE);
        layoutResult.setVisibility(View.GONE);

        tvResultDetail.setText("");
        tvTextDisplay.setText("");
        layoutTargetWord.setVisibility(View.GONE);

        btnStart.setVisibility(View.VISIBLE);
        btnStart.setText(getStartText());
        tvInstruction.setText(getInstructionText());
        tvTimer.setText("⏱ 00:00");

        layoutChart.setVisibility(View.VISIBLE);
        showChartAndHistory();
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

    // ============================================================
    // ========== چارت و تاریخچه ==========
    // ============================================================
    private void showChartAndHistory() {
        layoutChart.setVisibility(View.VISIBLE);
        setupChartAndHistory();
    }

    private void setupChartAndHistory() {
        List<GameModel> history = GamePreferencesHelper.loadGameHistory(this, "word_count");

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
            chartTitle = "Word Count Game Time - " + difficultyName + " (" + levelText + " " + difficultyLevel + ")";
            historyTitle = "Word Count History - " + difficultyName + " (" + levelText + " " + difficultyLevel + ")";
        } else {
            chartTitle = "زمان بازی شمارش کلمه - " + difficultyName + " (" + levelText + " " + difficultyLevel + ")";
            historyTitle = "تاریخچه شمارش کلمه - " + difficultyName + " (" + levelText + " " + difficultyLevel + ")";
        }

        tvChartTitle.setText(chartTitle);
        tvHistoryTitle.setText(historyTitle);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showChartAndHistory();
    }
}