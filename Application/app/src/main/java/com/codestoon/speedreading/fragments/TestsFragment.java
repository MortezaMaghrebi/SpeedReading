package com.codestoon.speedreading.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

    private EditText etTestText;
    private Button btnTestStart, btnTestReset;
    private LinearLayout layoutChartBars;
    private TextView tvNoData;
    private TextView tvLanguageInfo;
    private TextView tvChartTitle;

    private String currentText = "";
    private String currentLanguage = "fa";
    private long startTime = 0;
    private boolean isRunning = false;
    private boolean isFinished = false;
    private Handler timerHandler = new Handler();

    // پرچم برای بررسی اینکه آیا View آماده است
    private boolean isViewReady = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tests, container, false);

        etTestText = view.findViewById(R.id.etTestText);
        btnTestStart = view.findViewById(R.id.btnTestStart);
        btnTestReset = view.findViewById(R.id.btnTestReset);
        layoutChartBars = view.findViewById(R.id.layoutChartBars);
        tvNoData = view.findViewById(R.id.tvNoData);
        tvLanguageInfo = view.findViewById(R.id.tvLanguageInfo);
        tvChartTitle = view.findViewById(R.id.tvChartTitle);

        // تنظیم حالت اولیه
        showReadyMessage();
        updateLanguageInfo();
        renderChart();

        btnTestStart.setOnClickListener(v -> handleTestAction());
        btnTestReset.setOnClickListener(v -> resetTest());

        isViewReady = true;

        return view;
    }

    public void onLanguageChanged(String language) {
        // فقط اگر View آماده باشد اجرا کن
        if (!isViewReady) {
            return;
        }

        this.currentLanguage = language;

        if (!isRunning && !isFinished) {
            showReadyMessage();
            updateLanguageInfo();
            updateChartTitle();
        }

        // به‌روز کردن دکمه‌ها
        if (isFinished) {
            btnTestStart.setText(getRetryText());
        } else if (!isRunning) {
            btnTestStart.setText(getStartText());
        }

        // به‌روز کردن نتیجه اگر نمایش داده شده
        String currentText = etTestText.getText().toString();
        if (isFinished && (currentText.contains("نتیجه") || currentText.contains("Result"))) {
            List<TestResultModel> history = PreferencesHelper.loadTestHistory(getContext());
            if (!history.isEmpty()) {
                TestResultModel lastResult = history.get(history.size() - 1);
                showResult(lastResult.getWpm());
            }
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

    private void showReadyMessage() {
        if (etTestText == null) return;

        String message;
        if (currentLanguage.equals("fa")) {
            message = getString(R.string.ready_message_fa);
            etTestText.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        } else {
            message = getString(R.string.ready_message_en);
            etTestText.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
        etTestText.setText("📚 " + message);
        etTestText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        etTestText.setGravity(android.view.Gravity.CENTER);
        adjustTextHeight();
    }

    private void showResult(int wpm) {
        if (etTestText == null) return;

        String resultText;
        if (currentLanguage.equals("fa")) {
            resultText = "✅ " + getString(R.string.test_result, wpm, 0.0, 0);
        } else {
            resultText = "✅ " + getString(R.string.test_result_en, wpm, 0.0, 0);
        }
        etTestText.setText(resultText);
        etTestText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        etTestText.setGravity(android.view.Gravity.CENTER);
        if (currentLanguage.equals("fa")) {
            etTestText.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        } else {
            etTestText.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
        adjustTextHeight();
    }

    private void adjustTextHeight() {
        if (etTestText == null) return;

        etTestText.post(() -> {
            int contentHeight = etTestText.getLineCount() * etTestText.getLineHeight();
            int padding = etTestText.getPaddingTop() + etTestText.getPaddingBottom();
            int totalHeight = contentHeight + padding;

            int minHeight = (int) (200 * getResources().getDisplayMetrics().density);
            if (totalHeight < minHeight) {
                totalHeight = minHeight;
            }

            ViewGroup.LayoutParams params = etTestText.getLayoutParams();
            params.height = totalHeight;
            etTestText.setLayoutParams(params);
        });
    }

    private void handleTestAction() {
        if (isRunning) {
            finishTest();
            return;
        }
        if (isFinished) {
            resetTest();
            return;
        }
        startTest();
    }

    private void startTest() {
        if (etTestText == null || btnTestStart == null) return;

        loadRandomText();
        currentText = etTestText.getText().toString();

        if (currentLanguage.equals("fa")) {
            etTestText.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        } else {
            etTestText.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }

        adjustTextHeight();

        isRunning = true;
        isFinished = false;
        startTime = System.currentTimeMillis();

        btnTestStart.setText(getFinishText());
        btnTestStart.setBackgroundResource(R.drawable.bg_button_success);
        btnTestStart.setTextColor(getResources().getColor(R.color.white));
        btnTestStart.setEnabled(true);
        if (btnTestReset != null) {
            btnTestReset.setVisibility(View.GONE);
        }
        etTestText.setEnabled(false);
    }

    private void finishTest() {
        if (!isRunning || etTestText == null || btnTestStart == null) return;

        isRunning = false;
        isFinished = true;

        long elapsed = System.currentTimeMillis() - startTime;
        int wpm = WPMCalculator.calculateWPM(currentText, elapsed / 1000);
        int wordCount = WPMCalculator.countWords(currentText);

        TestResultModel result = new TestResultModel(wpm);
        PreferencesHelper.addTestResult(getContext(), result);

        String resultText;
        if (currentLanguage.equals("fa")) {
            resultText = "✅ " + getString(R.string.test_result, wpm, elapsed / 1000.0, wordCount);
            etTestText.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        } else {
            resultText = "✅ " + getString(R.string.test_result_en, wpm, elapsed / 1000.0, wordCount);
            etTestText.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
        etTestText.setText(resultText);
        etTestText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        etTestText.setGravity(android.view.Gravity.CENTER);
        adjustTextHeight();

        btnTestStart.setText(getRetryText());
        btnTestStart.setBackgroundResource(R.drawable.bg_button_success);
        btnTestStart.setTextColor(getResources().getColor(R.color.white));
        btnTestStart.setEnabled(true);
        if (btnTestReset != null) {
            btnTestReset.setVisibility(View.GONE);
        }
        etTestText.setEnabled(false);

        renderChart();
    }

    private void resetTest() {
        if (etTestText == null || btnTestStart == null) return;

        isRunning = false;
        isFinished = false;
        startTime = 0;

        btnTestStart.setText(getStartText());
        btnTestStart.setBackgroundResource(R.drawable.bg_button_primary);
        btnTestStart.setTextColor(getResources().getColor(R.color.white));
        btnTestStart.setEnabled(true);
        if (btnTestReset != null) {
            btnTestReset.setVisibility(View.GONE);
        }
        etTestText.setEnabled(true);

        showReadyMessage();
        updateLanguageInfo();
    }

    private void loadRandomText() {
        if (etTestText == null || getContext() == null) return;

        String content = AssetsHelper.getRandomText(getContext(), currentLanguage);
        etTestText.setText(content);

        if (currentLanguage.equals("fa")) {
            etTestText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            etTestText.setGravity(android.view.Gravity.START);
            etTestText.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        } else {
            etTestText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            etTestText.setGravity(android.view.Gravity.START);
            etTestText.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }

        adjustTextHeight();
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