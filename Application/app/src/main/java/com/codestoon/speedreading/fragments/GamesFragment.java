package com.codestoon.speedreading.fragments;

import android.graphics.Color;
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
import com.codestoon.speedreading.games.GameManager;
import com.codestoon.speedreading.games.base.BaseGame;
import com.codestoon.speedreading.models.GameModel;
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
    private BaseGame currentGame = null;
    private String currentGameId = "";
    private int currentLevel = 1;
    private int maxLevel = 10;
    private boolean isGameRunning = false;
    private boolean isGameFinished = false;
    private Handler timerHandler = new Handler();
    private long startTime = 0;
    private double elapsedTime = 0;

    private boolean isViewReady = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_games, container, false);

        // مقداردهی اولیه GameManager
        GameManager.getInstance().init(getContext());

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

        // ایجاد دکمه‌های منو به صورت داینامیک
        setupGameMenu(view);
    }

    private void setupGameMenu(View view) {
        LinearLayout menuContainer = view.findViewById(R.id.menuContainer);
        menuContainer.removeAllViews();

        // دریافت لیست بازی‌ها از GameManager
        List<BaseGame> games = GameManager.getInstance().getAllGames();

        // ایجاد GridLayout برای دکمه‌ها
        android.widget.GridLayout gridLayout = new android.widget.GridLayout(getContext());
        gridLayout.setColumnCount(2);
        gridLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        for (BaseGame game : games) {
            Button gameButton = new Button(getContext());
            String buttonText = currentLanguage.equals("en") ? game.getGameName() : game.getGameName();
            // برای بازی ماز، name رو از کلاس می‌گیریم
            gameButton.setText(buttonText);
            gameButton.setPadding(20, 20, 20, 20);
            gameButton.setBackgroundResource(R.drawable.bg_game_menu_button);
            gameButton.setTextColor(getResources().getColor(R.color.text_primary));
            gameButton.setTextSize(16);
            gameButton.setTypeface(null, android.graphics.Typeface.BOLD);

            // تنظیم آیکون بر اساس نوع بازی
            String icon = getGameIcon(game.getGameId());
            gameButton.setText(icon + " " + buttonText);

            gameButton.setOnClickListener(v -> {
                selectGame(game.getGameId());
            });

            // تنظیم LayoutParams برای GridLayout
            android.widget.GridLayout.LayoutParams params = new android.widget.GridLayout.LayoutParams();
            params.width = 0;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.columnSpec = android.widget.GridLayout.spec(android.widget.GridLayout.UNDEFINED, 1f);
            params.setMargins(12, 12, 12, 12);
            gameButton.setLayoutParams(params);

            gridLayout.addView(gameButton);
        }

        menuContainer.addView(gridLayout);
    }

    private String getGameIcon(String gameId) {
        switch (gameId) {
            case "maze": return "🧩";
            case "numbers": return "🔢";
            case "word_count": return "📝";
            case "common_image": return "🖼️";
            default: return "🎮";
        }
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

    private void selectGame(String gameId) {
        currentGameId = gameId;
        currentGame = GameManager.getInstance().getGame(gameId);
        currentLevel = 1;
        resetGame();
        showGameInfo();
    }

    private void showGameInfo() {
        if (currentGame == null) return;

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

        String gameName = currentLanguage.equals("en") ? currentGame.getGameName() : currentGame.getGameName();
        tvGameTitle.setText(gameName);
        tvLevel.setText(getLevelText() + " " + currentLevel + "/" + currentGame.getMaxLevel());
        tvTimer.setText("⏱ 00:00");
        tvBestTime.setText(getBestTimeText() + ": " + getBestTime());
        tvInstruction.setText(currentGame.getInstruction(currentLevel, currentLanguage));
        tvResult.setText("");
        tvResultDetail.setText("");
        setupChartAndHistory();
    }

    private String getLevelText() {
        return currentLanguage.equals("en") ? "Level" : "سطح";
    }

    private String getBestTimeText() {
        return currentLanguage.equals("en") ? "Best Time" : "بهترین زمان";
    }

    private String getBestTime() {
        if (currentGame == null) return currentLanguage.equals("en") ? "Not played yet" : "هنوز بازی نشده";

        List<GameModel> history = currentGame.loadHistory(getContext());
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
        if (currentGame == null) return;

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

        currentGame.startGame(currentLevel, ivGameDisplay, tvInstruction);
    }

    private void backToMenu() {
        isGameRunning = false;
        timerHandler.removeCallbacksAndMessages(null);
        resetGame();
        showGameMenu();
        btnBackToMenu.setVisibility(View.GONE);

        // بازسازی منو برای به‌روزرسانی زبان
        setupGameMenu(getView());
    }

    private void finishGame() {
        if (!isGameRunning || currentGame == null) return;

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
        currentGame.showAnswer(ivGameAnswer);

        // ذخیره نتیجه
        GameModel result = currentGame.createGameResult(currentLevel, elapsedTime);
        currentGame.saveResult(getContext(), result);

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
        if (currentGame == null) return;

        if (currentLevel < currentGame.getMaxLevel()) {
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
        if (currentGame != null) {
            tvInstruction.setText(currentGame.getInstruction(currentLevel, currentLanguage));
        }
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

    private void setupChartAndHistory() {
        if (getContext() == null || currentGame == null) return;

        List<GameModel> history = currentGame.loadHistory(getContext());

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

        // بازسازی منو
        if (getView() != null) {
            setupGameMenu(getView());
        }

        if (currentGame != null) {
            String gameName = currentLanguage.equals("en") ? currentGame.getGameName() : currentGame.getGameName();
            tvGameTitle.setText(gameName);
            tvInstruction.setText(currentGame.getInstruction(currentLevel, currentLanguage));
            tvLevel.setText(getLevelText() + " " + currentLevel + "/" + currentGame.getMaxLevel());
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
        if (isViewReady && currentGame != null) {
            setupChartAndHistory();
        }
    }
}