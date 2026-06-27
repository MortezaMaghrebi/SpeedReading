package com.codestoon.speedreading.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TestsFragment extends Fragment {

    private TextView tvTestText;
    private Button btnTestStart, btnSaveResult;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tests, container, false);

        tvTestText = view.findViewById(R.id.tvTestText);
        btnTestStart = view.findViewById(R.id.btnTestStart);
        btnSaveResult = view.findViewById(R.id.btnSaveResult);
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
        btnSaveResult.setOnClickListener(v -> saveResult());

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
            btnSaveResult.setText(getSaveResultText());
        } else if (!isRunning) {
            btnTestStart.setText(getStartText());
        }

        String currentText = tvTestText.getText().toString();
        if (isFinished && !isResultSaved && (currentText.contains("نتیجه") || currentText.contains("Result"))) {
            showResult(lastWpm);
        }
    }

    private void updateChartTitle() {
        if (tvChartTitle == null) return;

        if (currentLanguage.equals("fa")) {
            tvChartTitle.setText("پیشرفت تست‌ها (کلمه بر دقیقه)");
        } else {
            tvChartTitle.setText("Test Progress (Words Per Minute)");
        }
    }

    private void updateHistoryTitle() {
        if (tvHistoryTitle == null) return;

        if (currentLanguage.equals("fa")) {
            tvHistoryTitle.setText("تاریخچه تست‌ها");
        } else {
            tvHistoryTitle.setText("Test History");
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

    private String getSaveResultText() {
        return currentLanguage.equals("fa") ?
                "ذخیره نتیجه" :
                "Save Result";
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
        btnSaveResult.setVisibility(View.GONE);
        tvTestText.setEnabled(false);
    }

    private void finishTest() {
        if (!isRunning || tvTestText == null || btnTestStart == null) return;

        isRunning = false;
        isFinished = true;
        isResultSaved = false;

        long elapsed = System.currentTimeMillis() - startTime;
        lastWpm = WPMCalculator.calculateWPM(currentText, elapsed / 1000);
        lastElapsed = elapsed / 1000.0;
        lastWordCount = WPMCalculator.countWords(currentText);

        // نمایش نتیجه بدون ذخیره
        showResult(lastWpm);

        btnTestStart.setText(getRetryText());
        btnTestStart.setBackgroundResource(R.drawable.bg_button_secondary_white);
        btnTestStart.setTextColor(getResources().getColor(R.color.white));
        btnTestStart.setEnabled(true);
        btnSaveResult.setVisibility(View.VISIBLE);
        btnSaveResult.setBackgroundResource(R.drawable.bg_button_success);
        btnSaveResult.setTextColor(getResources().getColor(R.color.white));
        btnSaveResult.setEnabled(true);
        btnSaveResult.setText(getSaveResultText());
        tvTestText.setEnabled(false);

        // فقط نمودار را به‌روز می‌کنیم ولی نتیجه هنوز ذخیره نشده
        renderChartAndHistory();
    }

    private void saveResult() {
        if (!isFinished || isResultSaved) return;

        // ذخیره نتیجه معتبر
        TestResultModel result = new TestResultModel(lastWpm);
        PreferencesHelper.addTestResult(getContext(), result);
        isResultSaved = true;

        // تغییر متن دکمه و غیرفعال کردن آن
        btnSaveResult.setText(currentLanguage.equals("fa") ? "✓ ذخیره شد" : "✓ Saved");
        btnSaveResult.setBackgroundResource(R.drawable.bg_button_success);
        btnSaveResult.setEnabled(false);

        // به‌روزرسانی متن نتیجه با تیک سبز
        showSavedResult(lastWpm);

        // به‌روزرسانی چارت و جدول
        renderChartAndHistory();
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
        btnSaveResult.setVisibility(View.GONE);
        btnSaveResult.setEnabled(true);
        btnSaveResult.setText(getSaveResultText());
        tvTestText.setEnabled(true);

        showReadyMessage();
        updateLanguageInfo();

        // به‌روزرسانی چارت و جدول
        renderChartAndHistory();
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

        // نمایش/مخفی کردن نمودار و جدول
        if (history.isEmpty()) {
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

        chartProgress.setVisibility(View.VISIBLE);
        tvNoData.setVisibility(View.GONE);
        rvHistory.setVisibility(View.VISIBLE);
        tvNoHistory.setVisibility(View.GONE);

        // ========== رسم نمودار LineChart ==========
        setupLineChart(history);

        // ========== نمایش جدول تاریخچه ==========
        historyAdapter.updateData(history);
    }

    private void setupLineChart(List<TestResultModel> history) {
        List<Entry> entries = new ArrayList<>();
        int maxWpm = 0;

        for (int i = 0; i < history.size(); i++) {
            TestResultModel result = history.get(i);
            entries.add(new Entry(i, result.getWpm()));
            if (result.getWpm() > maxWpm) {
                maxWpm = result.getWpm();
            }
        }

        // تنظیم داده‌ها
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
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // منحنی spline

        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);

        LineData lineData = new LineData(dataSets);
        chartProgress.setData(lineData);

        // تنظیم محور X
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

        // تنظیم محور Y
        YAxis yAxisLeft = chartProgress.getAxisLeft();
        yAxisLeft.setTextColor(Color.parseColor("#6B7A8F"));
        yAxisLeft.setTextSize(10f);
        yAxisLeft.setDrawGridLines(true);
        yAxisLeft.setGridColor(Color.parseColor("#EEF3FC"));
        yAxisLeft.setAxisMinimum(0f);

        // تنظیم حداکثر مقدار برای محور Y با کمی فاصله
        int maxY = Math.max(maxWpm + 50, 100);
        yAxisLeft.setAxisMaximum(maxY);

        YAxis yAxisRight = chartProgress.getAxisRight();
        yAxisRight.setEnabled(false);

        // تنظیمات کلی نمودار
        chartProgress.getDescription().setEnabled(false);
        chartProgress.setTouchEnabled(true);
        chartProgress.setDragEnabled(true);
        chartProgress.setScaleEnabled(true);
        chartProgress.setPinchZoom(true);
        chartProgress.setBackgroundColor(Color.WHITE);
        chartProgress.setExtraOffsets(10, 10, 10, 10);

        // تنظیم حداقل نمایش برای دیده شدن
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