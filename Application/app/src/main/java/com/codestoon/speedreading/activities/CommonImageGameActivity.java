package com.codestoon.speedreading.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class CommonImageGameActivity extends AppCompatActivity {

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
    private int commonImageIndex = 0;
    private int[] imageSet1 = new int[9];
    private int[] imageSet2 = new int[9];
    private Bitmap combinedBitmap;

    private int[] imageIcons = {
            R.drawable.ic_star,
            R.drawable.ic_heart,
            R.drawable.ic_circle,
            R.drawable.ic_square,
            R.drawable.ic_triangle,
            R.drawable.ic_diamond,
            R.drawable.ic_bolt,
            R.drawable.ic_cloud,
            R.drawable.ic_moon,
            R.drawable.ic_sun,
            R.drawable.ic_leaf,
            R.drawable.ic_drop,
            R.drawable.ic_fire,
            R.drawable.ic_snow,
            R.drawable.ic_rainbow,
            R.drawable.ic_star_filled
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_image_game);

        initViews();
        getIntentData();
        setupUI();
        generateImages();

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
        tvInstruction.setText("عکسی که در هر دو مجموعه وجود دارد را پیدا کن");
    }

    private void generateImages() {
        Random rand = new Random();
        commonImageIndex = rand.nextInt(imageIcons.length);

        // ساخت مجموعه اول
        Set<Integer> usedImages = new HashSet<>();
        usedImages.add(commonImageIndex);
        imageSet1[0] = commonImageIndex;
        for (int i = 1; i < 9; i++) {
            int img;
            do {
                img = rand.nextInt(imageIcons.length);
            } while (usedImages.contains(img));
            usedImages.add(img);
            imageSet1[i] = img;
        }
        shuffleArray(imageSet1);

        // ساخت مجموعه دوم
        usedImages.clear();
        usedImages.add(commonImageIndex);
        imageSet2[0] = commonImageIndex;
        for (int i = 1; i < 9; i++) {
            int img;
            do {
                img = rand.nextInt(imageIcons.length);
            } while (usedImages.contains(img));
            usedImages.add(img);
            imageSet2[i] = img;
        }
        shuffleArray(imageSet2);

        combinedBitmap = createCombinedBitmap();
        ivQuestion.setImageBitmap(combinedBitmap);
    }

    private Bitmap createCombinedBitmap() {
        Bitmap topBitmap = createSingleImageSet(imageSet1);
        Bitmap bottomBitmap = createSingleImageSet(imageSet2);

        int combinedWidth = Math.max(topBitmap.getWidth(), bottomBitmap.getWidth());
        int combinedHeight = topBitmap.getHeight() + bottomBitmap.getHeight() + 40;

        Bitmap combined = Bitmap.createBitmap(combinedWidth, combinedHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(combined);
        canvas.drawColor(Color.WHITE);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(20);
        textPaint.setTextAlign(Paint.Align.CENTER);

        canvas.drawText("مجموعه ۱", combinedWidth / 2, 30, textPaint);
        canvas.drawBitmap(topBitmap, (combinedWidth - topBitmap.getWidth()) / 2, 40, null);

        Paint linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(2);
        canvas.drawLine(0, topBitmap.getHeight() + 50, combinedWidth, topBitmap.getHeight() + 50, linePaint);

        canvas.drawText("مجموعه ۲", combinedWidth / 2, topBitmap.getHeight() + 70, textPaint);
        canvas.drawBitmap(bottomBitmap, (combinedWidth - bottomBitmap.getWidth()) / 2, topBitmap.getHeight() + 80, null);

        return combined;
    }

    private Bitmap createSingleImageSet(int[] imageSet) {
        int cellSize = 80;
        int padding = 10;
        int cols = 3;
        int rows = 3;
        int width = cols * cellSize + padding * 2;
        int height = rows * cellSize + padding * 2;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.LTGRAY);

        for (int i = 0; i < 9; i++) {
            int row = i / cols;
            int col = i % cols;
            int x = padding + col * cellSize;
            int y = padding + row * cellSize;

            canvas.drawRect(x, y, x + cellSize, y + cellSize, bgPaint);

            Bitmap iconBitmap = BitmapFactory.decodeResource(getResources(), imageIcons[imageSet[i]]);
            if (iconBitmap != null) {
                Bitmap scaledIcon = Bitmap.createScaledBitmap(iconBitmap, cellSize - 10, cellSize - 10, true);
                canvas.drawBitmap(scaledIcon, x + 5, y + 5, null);
            }
        }

        return bitmap;
    }

    private void shuffleArray(int[] array) {
        Random rand = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int temp = array[i];
            array[i] = array[j];
            array[j] = temp;
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

        // جواب
        layoutAnswer.setVisibility(View.VISIBLE);
        tvAnswerText.setVisibility(View.VISIBLE);
        tvAnswerText.setText("✅ عکس مشترک پیدا شد!");

        // نمایش عکس مشترک
        Bitmap answerBitmap = BitmapFactory.decodeResource(getResources(), imageIcons[commonImageIndex]);
        if (answerBitmap != null) {
            Bitmap scaledAnswer = Bitmap.createScaledBitmap(answerBitmap, 150, 150, true);
            ivAnswer.setImageBitmap(scaledAnswer);
        }

        // ذخیره نتیجه
        GameModel result = new GameModel("common_image", "عکس مشترک", currentLevel, elapsedTime);
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
            generateImages();
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