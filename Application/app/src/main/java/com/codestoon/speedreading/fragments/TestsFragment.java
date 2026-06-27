package com.codestoon.speedreading.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codestoon.speedreading.R;
import com.codestoon.speedreading.adapters.HistoryAdapter;
import com.codestoon.speedreading.models.TestResultModel;
import com.codestoon.speedreading.utils.AssetsHelper;
import com.codestoon.speedreading.utils.PreferencesHelper;
import com.codestoon.speedreading.utils.WPMCalculator;
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

public class TestsFragment extends Fragment {

    private ScrollView scrollTests;
    private TextView tvTestText;
    private Button btnTestStart, btnDiscardResult, btnClearHistory;
    private LineChart chartProgress;
    private TextView tvNoData, tvNoHistory;
    private TextView tvLanguageInfo;
    private TextView tvChartTitle, tvHistoryTitle;
    private RecyclerView rvHistory;
    private HistoryAdapter historyAdapter;

    private String currentText = "";
    private String currentLanguage = "fa";
    private long startTime = 0;
    private boolean isRunning = false;
    private boolean isFinished = false;
    private boolean isResultSaved = false;
    private Handler timerHandler = new Handler();

    private int lastWpm = 0;
    private double lastElapsed = 0;
    private int lastWordCount = 0;

    private boolean isViewReady = false;

    // محدودیت تعداد تست‌ها
    private static final int MAX_HISTORY_SIZE = 30;
    private static final int MAX_CHART_ITEMS = 30;
    private static final int MAX_HISTORY_TABLE_ITEMS = 30;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tests, container, false);

        scrollTests = view.findViewById(R.id.scrollTests);
        tvTestText = view.findViewById(R.id.tvTestText);
        btnTestStart = view.findViewById(R.id.btnTestStart);
        btnDiscardResult = view.findViewById(R.id.btnDiscardResult);
        btnClearHistory = view.findViewById(R.id.btnClearHistory);
        chartProgress = view.findViewById(R.id.chartProgress);
        tvNoData = view.findViewById(R.id.tvNoData);
        tvNoHistory = view.findViewById(R.id.tvNoHistory);
        tvLanguageInfo = view.findViewById(R.id.tvLanguageInfo);
        tvChartTitle = view.findViewById(R.id.tvChartTitle);
        tvHistoryTitle = view.findViewById(R.id.tvHistoryTitle);
        rvHistory = view.findViewById(R.id.rvHistory);

        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        historyAdapter = new HistoryAdapter(new ArrayList<>());
        rvHistory.setAdapter(historyAdapter);

        // تنظیم حالت اولیه
        showReadyMessage();
        updateLanguageInfo();
        renderChartAndHistory();

        btnTestStart.setOnClickListener(v -> handleTestAction());
        btnDiscardResult.setOnClickListener(v -> discardResult());
        btnClearHistory.setOnClickListener(v -> showClearHistoryDialog());

        isViewReady = true;

        return view;
    }

    public void onLanguageChanged(String language) {
        if (!isViewReady) {
            return;
        }

        this.currentLanguage = language;

        if (!isRunning && !isFinished) {
            showReadyMessage();
            updateLanguageInfo();
            updateChartTitle();
            updateHistoryTitle();
        }

        if (isFinished) {
            btnTestStart.setText(getRetryText());
            btnDiscardResult.setText(getDiscardText());
        } else if (!isRunning) {
            btnTestStart.setText(getStartText());
        }

        // به‌روزرسانی متن دکمه حذف تاریخچه
        updateClearHistoryButtonText();

        String currentText = tvTestText.getText().toString();
        if (isFinished && isResultSaved && (currentText.contains("نتیجه") || currentText.contains("Result"))) {
            showSavedResult(lastWpm);
        } else if (isFinished && !isResultSaved && (currentText.contains("نتیجه") || currentText.contains("Result"))) {
            showDiscardedResult(lastWpm);
        }
    }

    private void updateChartTitle() {
        if (tvChartTitle == null) return;

        String title;
        if (currentLanguage.equals("fa")) {
            title = "پیشرفت تست‌ها (آخرین " + MAX_CHART_ITEMS + " تست)";
        } else {
            title = "Test Progress (Last " + MAX_CHART_ITEMS + " tests)";
        }
        tvChartTitle.setText(title);
    }

    private void updateHistoryTitle() {
        if (tvHistoryTitle == null) return;

        if (currentLanguage.equals("fa")) {
            tvHistoryTitle.setText("تاریخچه تست‌ها (آخرین " + MAX_HISTORY_TABLE_ITEMS + " تست)");
        } else {
            tvHistoryTitle.setText("Test History (Last " + MAX_HISTORY_TABLE_ITEMS + " tests)");
        }
    }

    private void updateClearHistoryButtonText() {
        if (btnClearHistory == null) return;

        if (currentLanguage.equals("fa")) {
            btnClearHistory.setText("🗑️ حذف همه");
        } else {
            btnClearHistory.setText("🗑️ Clear All");
        }
    }

    private void updateLanguageInfo() {
        if (tvLanguageInfo == null || getContext() == null) return;

        int count = AssetsHelper.getTextCount(getContext(), currentLanguage);
        String text;
        if (currentLanguage.equals("fa")) {
            text = getString(R.string.language_info_fa, count);
        } else {
            text = getString(R.string.language_info_en, count);
        }
        tvLanguageInfo.setText(text);
    }

    private String getStartText() {
        return currentLanguage.equals("fa") ?
                getString(R.string.start_test_fa) :
                getString(R.string.start_test_en);
    }

    private String getFinishText() {
        return currentLanguage.equals("fa") ?
                getString(R.string.finish_reading_fa) :
                getString(R.string.finish_reading_en);
    }

    private String getRetryText() {
        return currentLanguage.equals("fa") ?
                getString(R.string.retry_test_fa) :
                getString(R.string.retry_test_en);
    }

    private String getDiscardText() {
        return currentLanguage.equals("fa") ?
                "عدم ذخیره نتیجه" :
                "Discard Result";
    }

    private void showReadyMessage() {
        if (tvTestText == null) return;

        String message;
        if (currentLanguage.equals("fa")) {
            message = getString(R.string.ready_message_fa);
            tvTestText.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        } else {
            message = getString(R.string.ready_message_en);
            tvTestText.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
        tvTestText.setText("📚 " + message);
        tvTestText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvTestText.setGravity(android.view.Gravity.CENTER);
    }

    private void showResult(int wpm) {
        if (tvTestText == null) return;

        String resultText;
        if (currentLanguage.equals("fa")) {
            resultText = "📊 " + getString(R.string.test_result, wpm, lastElapsed, lastWordCount);
            tvTestText.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        } else {
            resultText = "📊 " + getString(R.string.test_result_en, wpm, lastElapsed, lastWordCount);
            tvTestText.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
        tvTestText.setText(resultText);
        tvTestText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvTestText.setGravity(android.view.Gravity.CENTER);
    }

    private void showSavedResult(int wpm) {
        if (tvTestText == null) return;

        String resultText;
        if (currentLanguage.equals("fa")) {
            resultText = "✅ " + getString(R.string.test_result, wpm, lastElapsed, lastWordCount) + "\n✓ ذخیره شد";
            tvTestText.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        } else {
            resultText = "✅ " + getString(R.string.test_result_en, wpm, lastElapsed, lastWordCount) + "\n✓ Saved";
            tvTestText.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
        tvTestText.setText(resultText);
        tvTestText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvTestText.setGravity(android.view.Gravity.CENTER);
    }

    private void showDiscardedResult(int wpm) {
        if (tvTestText == null) return;

        String resultText;
        if (currentLanguage.equals("fa")) {
            resultText = "❌ " + getString(R.string.test_result, wpm, lastElapsed, lastWordCount) + "\n✗ ذخیره نشد";
            tvTestText.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        } else {
            resultText = "❌ " + getString(R.string.test_result_en, wpm, lastElapsed, lastWordCount) + "\n✗ Not saved";
            tvTestText.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
        tvTestText.setText(resultText);
        tvTestText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvTestText.setGravity(android.view.Gravity.CENTER);
    }

    private void handleTestAction() {
        if (isRunning) {
            finishTest();
        } else if (isFinished) {
            resetTest();
        } else {
            startTest();
        }
    }

    private void startTest() {
        if (tvTestText == null || btnTestStart == null) return;

        loadRandomText();
        currentText = tvTestText.getText().toString();

        if (currentLanguage.equals("fa")) {
            tvTestText.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        } else {
            tvTestText.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }

        isRunning = true;
        isFinished = false;
        isResultSaved = false;
        startTime = System.currentTimeMillis();

        btnTestStart.setText(getFinishText());
        btnTestStart.setBackgroundResource(R.drawable.bg_button_success);
        btnTestStart.setTextColor(getResources().getColor(R.color.white));
        btnTestStart.setEnabled(true);
        btnDiscardResult.setVisibility(View.GONE);
        tvTestText.setEnabled(false);
    }

    private void finishTest() {
        if (!isRunning || tvTestText == null || btnTestStart == null) return;

        isRunning = false;
        isFinished = true;

        long elapsed = System.currentTimeMillis() - startTime;
        lastWpm = WPMCalculator.calculateWPM(currentText, elapsed / 1000);
        lastElapsed = elapsed / 1000.0;
        lastWordCount = WPMCalculator.countWords(currentText);

        // ذخیره خودکار نتیجه
        saveResultAutomatically();

        btnTestStart.setText(getRetryText());
        btnTestStart.setBackgroundResource(R.drawable.bg_button_secondary_white);
        btnTestStart.setTextColor(getResources().getColor(R.color.white));
        btnTestStart.setEnabled(true);
        btnDiscardResult.setVisibility(View.VISIBLE);
        btnDiscardResult.setBackgroundResource(R.drawable.bg_button_danger);
        btnDiscardResult.setTextColor(getResources().getColor(R.color.white));
        btnDiscardResult.setEnabled(true);
        btnDiscardResult.setText(getDiscardText());
        tvTestText.setEnabled(false);

        // اسکرول به بالای صفحه
        scrollToTop();
    }

    private void saveResultAutomatically() {
        List<TestResultModel> history = PreferencesHelper.loadTestHistory(getContext());

        while (history.size() >= MAX_HISTORY_SIZE) {
            history.remove(0);
        }

        TestResultModel result = new TestResultModel(lastWpm);
        history.add(result);
        PreferencesHelper.saveTestHistory(getContext(), history);
        isResultSaved = true;

        showSavedResult(lastWpm);
        renderChartAndHistory();
    }

    private void discardResult() {
        if (!isFinished || !isResultSaved) return;

        List<TestResultModel> history = PreferencesHelper.loadTestHistory(getContext());
        if (!history.isEmpty()) {
            history.remove(history.size() - 1);
            PreferencesHelper.saveTestHistory(getContext(), history);
        }
        isResultSaved = false;

        btnDiscardResult.setText(currentLanguage.equals("fa") ? "✗ حذف شد" : "✗ Discarded");
        btnDiscardResult.setBackgroundResource(R.drawable.bg_button_danger);
        btnDiscardResult.setEnabled(false);

        showDiscardedResult(lastWpm);
        renderChartAndHistory();
    }

    private void showClearHistoryDialog() {
        List<TestResultModel> history = PreferencesHelper.loadTestHistory(getContext());
        if (history.isEmpty()) {
            // اگر تاریخچه خالی است، پیام نمایش بده
            String message = currentLanguage.equals("fa") ?
                    "تاریخچه قبلاً خالی است" :
                    "History is already empty";
            showToast(message);
            return;
        }

        String title, message, positiveText, negativeText;
        if (currentLanguage.equals("fa")) {
            title = "حذف تمام تاریخچه";
            message = "آیا از حذف تمام " + history.size() + " تست ثبت شده اطمینان دارید؟";
            positiveText = "حذف همه";
            negativeText = "انصراف";
        } else {
            title = "Clear All History";
            message = "Are you sure you want to delete all " + history.size() + " recorded tests?";
            positiveText = "Clear All";
            negativeText = "Cancel";
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveText, (dialog, which) -> clearAllHistory())
                .setNegativeButton(negativeText, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void clearAllHistory() {
        PreferencesHelper.clearTestHistory(getContext());
        isResultSaved = false;

        // اگر در حالت پایان تست هستیم، نتیجه فعلی را هم پاک کن
        if (isFinished) {
            isResultSaved = false;
            showDiscardedResult(lastWpm);
            btnDiscardResult.setVisibility(View.GONE);
        }

        renderChartAndHistory();

        String message = currentLanguage.equals("fa") ?
                "تمام تاریخچه حذف شد" :
                "All history cleared";
        showToast(message);
    }

    private void showToast(String message) {
        android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
    }

    private void resetTest() {
        if (tvTestText == null || btnTestStart == null) return;

        isRunning = false;
        isFinished = false;
        isResultSaved = false;
        startTime = 0;
        lastWpm = 0;
        lastElapsed = 0;
        lastWordCount = 0;

        btnTestStart.setText(getStartText());
        btnTestStart.setBackgroundResource(R.drawable.bg_button_primary);
        btnTestStart.setTextColor(getResources().getColor(R.color.white));
        btnTestStart.setEnabled(true);
        btnDiscardResult.setVisibility(View.GONE);
        btnDiscardResult.setEnabled(true);
        btnDiscardResult.setText(getDiscardText());
        tvTestText.setEnabled(true);

        showReadyMessage();
        updateLanguageInfo();

        renderChartAndHistory();
    }

    private void scrollToTop() {
        if (scrollTests != null) {
            scrollTests.post(() -> {
                scrollTests.fullScroll(ScrollView.FOCUS_UP);
                scrollTests.smoothScrollTo(0, 0);
            });
        }
    }

    private void loadRandomText() {
        if (tvTestText == null || getContext() == null) return;

        String content = AssetsHelper.getRandomText(getContext(), currentLanguage);
        tvTestText.setText(content);

        if (currentLanguage.equals("fa")) {
            tvTestText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            tvTestText.setGravity(android.view.Gravity.START);
            tvTestText.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        } else {
            tvTestText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            tvTestText.setGravity(android.view.Gravity.START);
            tvTestText.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
    }

    private void renderChartAndHistory() {
        if (getContext() == null) return;

        List<TestResultModel> history = PreferencesHelper.loadTestHistory(getContext());

        // نمایش/مخفی کردن دکمه حذف تاریخچه
        if (history.isEmpty()) {
            btnClearHistory.setVisibility(View.GONE);
            chartProgress.setVisibility(View.GONE);
            tvNoData.setVisibility(View.VISIBLE);
            rvHistory.setVisibility(View.GONE);
            tvNoHistory.setVisibility(View.VISIBLE);

            if (currentLanguage.equals("fa")) {
                tvNoData.setText(R.string.test_no_data);
                tvNoHistory.setText("هیچ تستی ثبت نشده");
            } else {
                tvNoData.setText(R.string.test_no_data_en);
                tvNoHistory.setText("No tests recorded yet");
            }
            return;
        }

        btnClearHistory.setVisibility(View.VISIBLE);
        updateClearHistoryButtonText();
        chartProgress.setVisibility(View.VISIBLE);
        tvNoData.setVisibility(View.GONE);
        rvHistory.setVisibility(View.VISIBLE);
        tvNoHistory.setVisibility(View.GONE);

        setupLineChart(history);

        int startIndex = Math.max(0, history.size() - MAX_HISTORY_TABLE_ITEMS);
        List<TestResultModel> displayHistory = history.subList(startIndex, history.size());
        historyAdapter.updateData(displayHistory);

        updateHistoryTitle();
        updateChartTitle();
    }

    private void setupLineChart(List<TestResultModel> history) {
        int startIndex = Math.max(0, history.size() - MAX_CHART_ITEMS);
        List<TestResultModel> chartHistory = history.subList(startIndex, history.size());

        List<Entry> entries = new ArrayList<>();
        int maxWpm = 0;

        for (int i = 0; i < chartHistory.size(); i++) {
            TestResultModel result = chartHistory.get(i);
            entries.add(new Entry(i, result.getWpm()));
            if (result.getWpm() > maxWpm) {
                maxWpm = result.getWpm();
            }
        }

        if (entries.isEmpty()) {
            chartProgress.setVisibility(View.GONE);
            tvNoData.setVisibility(View.VISIBLE);
            return;
        }

        LineDataSet dataSet = new LineDataSet(entries, currentLanguage.equals("fa") ? "WPM" : "WPM");
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
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "#" + (int) (value + 1);
            }
        });

        YAxis yAxisLeft = chartProgress.getAxisLeft();
        yAxisLeft.setTextColor(Color.parseColor("#6B7A8F"));
        yAxisLeft.setTextSize(10f);
        yAxisLeft.setDrawGridLines(true);
        yAxisLeft.setGridColor(Color.parseColor("#EEF3FC"));
        yAxisLeft.setAxisMinimum(0f);

        int maxY = Math.max(maxWpm + 50, 100);
        yAxisLeft.setAxisMaximum(maxY);

        YAxis yAxisRight = chartProgress.getAxisRight();
        yAxisRight.setEnabled(false);

        chartProgress.getDescription().setEnabled(false);
        chartProgress.setTouchEnabled(true);
        chartProgress.setDragEnabled(true);
        chartProgress.setScaleEnabled(true);
        chartProgress.setPinchZoom(true);
        chartProgress.setBackgroundColor(Color.WHITE);
        chartProgress.setExtraOffsets(10, 10, 10, 10);
        chartProgress.setVisibleXRangeMinimum(3f);

        chartProgress.invalidate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isViewReady) {
            renderChartAndHistory();
        }
    }
}