package com.codestoon.speedreading.fragments;

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

import com.codestoon.speedreading.R;
import com.codestoon.speedreading.models.TestResultModel;
import com.codestoon.speedreading.utils.AssetsHelper;
import com.codestoon.speedreading.utils.PreferencesHelper;
import com.codestoon.speedreading.utils.WPMCalculator;

import java.util.List;

public class TestsFragment extends Fragment {

    private TextView tvTestText;
    private Button btnTestStart, btnSaveResult;
    private LinearLayout layoutChartBars;
    private TextView tvNoData;
    private TextView tvLanguageInfo;
    private TextView tvChartTitle;

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
        layoutChartBars = view.findViewById(R.id.layoutChartBars);
        tvNoData = view.findViewById(R.id.tvNoData);
        tvLanguageInfo = view.findViewById(R.id.tvLanguageInfo);
        tvChartTitle = view.findViewById(R.id.tvChartTitle);

        // تنظیم حالت اولیه
        showReadyMessage();
        updateLanguageInfo();
        renderChart();

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
            tvChartTitle.setText(R.string.chart_title);
        } else {
            tvChartTitle.setText(R.string.chart_title_en);
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
        btnTestStart.setBackgroundResource(R.drawable.bg_button_secondary);
        btnTestStart.setTextColor(getResources().getColor(R.color.text_primary));
        btnTestStart.setEnabled(true);
        btnSaveResult.setVisibility(View.VISIBLE);
        btnSaveResult.setBackgroundResource(R.drawable.bg_button_success);
        btnSaveResult.setTextColor(getResources().getColor(R.color.white));
        btnSaveResult.setEnabled(true);
        btnSaveResult.setText(getSaveResultText());
        tvTestText.setEnabled(false);
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

        // به‌روزرسانی چارت
        renderChart();
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

    private void renderChart() {
        if (layoutChartBars == null || tvNoData == null || getContext() == null) return;

        List<TestResultModel> history = PreferencesHelper.loadTestHistory(getContext());
        layoutChartBars.removeAllViews();

        if (history.isEmpty()) {
            tvNoData.setVisibility(View.VISIBLE);
            if (currentLanguage.equals("fa")) {
                tvNoData.setText(R.string.test_no_data);
            } else {
                tvNoData.setText(R.string.test_no_data_en);
            }
            return;
        }
        tvNoData.setVisibility(View.GONE);

        int start = Math.max(0, history.size() - 10);
        List<TestResultModel> displayData = history.subList(start, history.size());

        int maxWpm = 100;
        for (TestResultModel result : displayData) {
            if (result.getWpm() > maxWpm) maxWpm = result.getWpm();
        }
        maxWpm = Math.max(maxWpm, 100);

        for (int i = 0; i < displayData.size(); i++) {
            TestResultModel result = displayData.get(i);
            int heightPercent = Math.max((int)((result.getWpm() / (float)maxWpm) * 80), 6);

            LinearLayout wrapper = new LinearLayout(getContext());
            wrapper.setOrientation(LinearLayout.VERTICAL);
            wrapper.setGravity(View.TEXT_ALIGNMENT_CENTER);
            wrapper.setLayoutParams(new LinearLayout.LayoutParams(
                    40, ViewGroup.LayoutParams.MATCH_PARENT));

            View bar = new View(getContext());
            bar.setBackgroundResource(R.drawable.bg_chart_bar);
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                    28, heightPercent);
            barParams.gravity = android.view.Gravity.BOTTOM;
            bar.setLayoutParams(barParams);

            TextView label = new TextView(getContext());
            label.setText("#" + (start + i + 1));
            label.setTextColor(getResources().getColor(R.color.text_light));
            label.setTextSize(11);
            label.setGravity(View.TEXT_ALIGNMENT_CENTER);

            wrapper.addView(bar);
            wrapper.addView(label);
            layoutChartBars.addView(wrapper);
        }

        updateChartTitle();
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
            renderChart();
        }
    }
}