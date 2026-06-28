package com.codestoon.speedreading.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class MazeGameActivity extends AppCompatActivity {

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

    private Bitmap questionBitmap;
    private Bitmap answerBitmap;
    private int currentLevel = 1;
    private int maxLevel = 5;

    private String[] mazeLevels = {"level_1", "level_2", "level_3", "level_4"};
    private int gameIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maze_game);

        initViews();
        getIntentData();
        setupUI();
        loadMaze();

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
        tvInstruction.setText("مسیر را از شروع (S) تا پایان (F) در ذهن پیدا کن");
        tvTimer.setText("⏱ 00:00");
        btnStart.setVisibility(View.VISIBLE);
        btnFinish.setVisibility(View.GONE);
        btnNextLevel.setVisibility(View.GONE);
        btnReset.setVisibility(View.GONE);
        layoutAnswer.setVisibility(View.GONE);
        layoutResult.setVisibility(View.GONE);
    }

    private void loadMaze() {
        try {
            int levelIndex = Math.min((difficultyLevel - 1), 3);
            String levelFolder = mazeLevels[levelIndex];

            Random rand = new Random();
            gameIndex = rand.nextInt(3) + 1;

            String questionPath = "games/maze/" + levelFolder + "/question/game" + gameIndex + ".jpg";
            InputStream questionStream = getAssets().open(questionPath);
            questionBitmap = BitmapFactory.decodeStream(questionStream);
            questionStream.close();
            ivQuestion.setImageBitmap(questionBitmap);

            String answerPath = "games/maze/" + levelFolder + "/answer/game" + gameIndex + ".jpg";
            InputStream answerStream = getAssets().open(answerPath);
            answerBitmap = BitmapFactory.decodeStream(answerStream);
            answerStream.close();

        } catch (IOException e) {
            e.printStackTrace();
            tvInstruction.setText("خطا در بارگذاری ماز");
        }
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

        // نمایش جواب
        layoutAnswer.setVisibility(View.VISIBLE);
        if (answerBitmap != null) {
            ivAnswer.setImageBitmap(answerBitmap);
        }

        // ذخیره نتیجه
        GameModel result = new GameModel("maze", "ماز", currentLevel, elapsedTime);
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
            loadMaze();
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