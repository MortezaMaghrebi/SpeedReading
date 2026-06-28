package com.codestoon.speedreading.activities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.codestoon.speedreading.R;
import com.codestoon.speedreading.models.GameModel;
import com.codestoon.speedreading.utils.GamePreferencesHelper;

import java.util.ArrayList;
import java.util.List;

public class NumbersGameActivity extends AppCompatActivity {

    private TextView tvGameTitle, tvDifficulty, tvTimer, tvInstruction, tvResult, tvResultDetail;
    private ImageView ivQuestion, ivAnswer;
    private Button btnStart, btnFinish, btnNextLevel, btnReset;
    private LinearLayout layoutAnswer, layoutResult;

    private String difficultyId;
    private String difficultyName;
    private int difficultyLevel;
    private String gameTitle;

    private boolean isRunning = false;
    private boolean isFinished = false;
    private Handler timerHandler = new Handler();
    private long startTime = 0;
    private double elapsedTime = 0;

    private int currentLevel = 1;
    private int maxLevel = 5;
    private int gridSize = 7;
    private Bitmap numberBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_numbers_game);

        initViews();
        getIntentData();
        setupUI();
        generateNumbers();

        btnStart.setOnClickListener(v -> startGame());
        btnFinish.setOnClickListener(v -> finishGame());
        btnNextLevel.setOnClickListener(v -> nextLevel());
        btnReset.setOnClickListener(v -> resetGame());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void initViews() {
        tvGameTitle = findViewById(R.id.tvGameTitle);
        tvDifficulty = findViewById(R.id.tvDifficulty);
        tvTimer = findViewById(R.id.tvTimer);
        tvInstruction = findViewById(R.id.tvInstruction);
        tvResult = findViewById(R.id.tvResult);
        tvResultDetail = findViewById(R.id.tvResultDetail);
        ivQuestion = findViewById(R.id.ivQuestion);
        ivAnswer = findViewById(R.id.ivAnswer);
        btnStart = findViewById(R.id.btnStart);
        btnFinish = findViewById(R.id.btnFinish);
        btnNextLevel = findViewById(R.id.btnNextLevel);
        btnReset = findViewById(R.id.btnReset);
        layoutAnswer = findViewById(R.id.layoutAnswer);
        layoutResult = findViewById(R.id.layoutResult);
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
        btnStart.setVisibility(View.VISIBLE);
        btnFinish.setVisibility(View.GONE);
        btnNextLevel.setVisibility(View.GONE);
        btnReset.setVisibility(View.GONE);
        layoutAnswer.setVisibility(View.GONE);
        layoutResult.setVisibility(View.GONE);
    }

    private void generateNumbers() {
        gridSize = 5 + difficultyLevel;
        numberBitmap = createNumberGridBitmap(gridSize);
        ivQuestion.setImageBitmap(numberBitmap);
        int total = gridSize * gridSize;
        tvInstruction.setText("اعداد ۱ تا " + total + " را به ترتیب در ذهن پیدا کن");
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

    private void startGame() {
        isRunning = true;
        isFinished = false;
        startTime = System.currentTimeMillis();
        btnStart.setVisibility(View.GONE);
        btnFinish.setVisibility(View.VISIBLE);
        layoutAnswer.setVisibility(View.GONE);
        layoutResult.setVisibility(View.GONE);
        tvResult.setText("");
        tvResultDetail.setText("");
        updateTimer();
    }

    private void finishGame() {
        if (!isRunning) return;
        isRunning = false;
        isFinished = true;
        elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0;

        btnFinish.setVisibility(View.GONE);
        btnNextLevel.setVisibility(View.VISIBLE);
        btnReset.setVisibility(View.VISIBLE);

        // جواب (اعداد مرتب شده)
        layoutAnswer.setVisibility(View.VISIBLE);
        ivAnswer.setImageBitmap(null);
        TextView tvAnswer = findViewById(R.id.tvAnswerText);
        tvAnswer.setVisibility(View.VISIBLE);
        tvAnswer.setText("✅ اعداد ۱ تا " + (gridSize * gridSize) + " مرتب شدند");

        // ذخیره نتیجه
        GameModel result = new GameModel("numbers", "اعداد", currentLevel, elapsedTime);
        GamePreferencesHelper.addGameResult(this, result);

        // نمایش نتیجه
        layoutResult.setVisibility(View.VISIBLE);
        tvResultDetail.setText("✅ زمان: " + String.format("%.2f", elapsedTime) + " ثانیه");

        stopTimer();
    }

    private void nextLevel() {
        if (currentLevel < maxLevel) {
            currentLevel++;
            resetGame();
            generateNumbers();
            setupUI();
            startGame();
        } else {
            tvResult.setText("🎉 تبریک! همه سطوح را کامل کردی!");
            btnNextLevel.setVisibility(View.GONE);
        }
    }

    private void resetGame() {
        isRunning = false;
        isFinished = false;
        ivQuestion.setImageBitmap(null);
        ivAnswer.setImageBitmap(null);
        tvResult.setText("");
        tvResultDetail.setText("");
        tvTimer.setText("⏱ 00:00");
        btnStart.setVisibility(View.VISIBLE);
        btnFinish.setVisibility(View.GONE);
        btnNextLevel.setVisibility(View.GONE);
        btnReset.setVisibility(View.GONE);
        layoutAnswer.setVisibility(View.GONE);
        layoutResult.setVisibility(View.GONE);
        stopTimer();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }
}