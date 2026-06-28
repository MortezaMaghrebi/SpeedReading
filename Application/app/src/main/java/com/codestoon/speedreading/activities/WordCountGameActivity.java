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

import java.util.Random;

public class WordCountGameActivity extends AppCompatActivity {

    private TextView tvGameTitle, tvDifficulty, tvTimer, tvInstruction, tvResult, tvResultDetail;
    private TextView tvAnswerText;
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
    private String targetWord = "";
    private int targetCount = 0;
    private String fullText = "";

    private String[] words = {
            "کتاب", "خواندن", "تندخوانی", "تمرین", "مطالعه", "یادگیری",
            "سرعت", "درک", "مطلب", "چشم", "حرکت", "تمرکز"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_count_game);

        initViews();
        getIntentData();
        setupUI();
        generateText();

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
        tvAnswerText = findViewById(R.id.tvAnswerText);
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
        tvAnswerText.setVisibility(View.GONE);
    }

    private void generateText() {
        Random rand = new Random();
        targetWord = words[rand.nextInt(words.length)];
        targetCount = 0;

        StringBuilder textBuilder = new StringBuilder();
        for (int i = 0; i < 50 + difficultyLevel * 20; i++) {
            String word = words[rand.nextInt(words.length)];
            textBuilder.append(word).append(" ");
            if (word.equals(targetWord)) {
                targetCount++;
            }
        }
        int extraRepeats = 3 + difficultyLevel * 2;
        for (int i = 0; i < extraRepeats; i++) {
            textBuilder.append(targetWord).append(" ");
            targetCount++;
        }
        fullText = textBuilder.toString();

        Bitmap textBitmap = createTextBitmap(fullText);
        ivQuestion.setImageBitmap(textBitmap);

        tvInstruction.setText("تعداد تکرار کلمه '" + targetWord + "' را در متن پیدا کن");
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

        // جواب
        layoutAnswer.setVisibility(View.VISIBLE);
        tvAnswerText.setVisibility(View.VISIBLE);
        tvAnswerText.setText("✅ تعداد تکرار کلمه '" + targetWord + "': " + targetCount + " بار");

        // ذخیره نتیجه
        GameModel result = new GameModel("word_count", "شمارش کلمه", currentLevel, elapsedTime);
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
            generateText();
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
        tvAnswerText.setVisibility(View.GONE);
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