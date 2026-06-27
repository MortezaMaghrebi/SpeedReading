package com.codestoon.speedreading.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codestoon.speedreading.R;
import com.codestoon.speedreading.adapters.ExerciseAdapter;
import com.codestoon.speedreading.models.ExerciseModel;
import com.codestoon.speedreading.models.VideoModel;
import com.codestoon.speedreading.utils.AssetsHelper;
import com.codestoon.speedreading.views.VideoFullscreenActivity;

import java.util.ArrayList;
import java.util.List;

public class ExercisesFragment extends Fragment {

    private RecyclerView rvExercises;
    private ExerciseAdapter exerciseAdapter;
    private List<ExerciseModel> exerciseList = new ArrayList<>();
    private String currentLanguage = "fa";

    private boolean isViewReady = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercises, container, false);

        rvExercises = view.findViewById(R.id.rvExercises);
        rvExercises.setLayoutManager(new LinearLayoutManager(getContext()));

        loadExercises();

        isViewReady = true;

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isViewReady) {
            loadExercises();
        }
    }

    private void loadExercises() {
        if (getContext() == null) return;

        List<String> folders = AssetsHelper.getVideoFolders(getContext());

        exerciseList.clear();

        for (String folder : folders) {
            // 1. اضافه کردن Header (با title و subtitle)
            String headerTitle = getTitleForFolder(folder);
            String headerSubtitle = getSubtitleForFolder(folder);
            exerciseList.add(new ExerciseModel(folder + "_header", headerTitle, headerSubtitle));

            // 2. اضافه کردن آیتم تمرین
            String title = getTitleForFolder(folder);
            String desc = getDescForFolder(folder);
            int icon = getIconForFolder(folder);
            List<VideoModel> videos = AssetsHelper.getVideosInFolder(getContext(), folder);
            int count = videos.size();

            ExerciseModel exercise = new ExerciseModel(folder, title, desc, icon, count, videos);
            exerciseList.add(exercise);
        }

        if (exerciseAdapter == null) {
            exerciseAdapter = new ExerciseAdapter(exerciseList, new ExerciseAdapter.OnExerciseClickListener() {
                @Override
                public void onExerciseClick(ExerciseModel exercise) {
                    // نیازی به کاری نیست، فقط برای باز/بسته شدن
                }

                @Override
                public void onVideoClick(ExerciseModel exercise, VideoModel video) {
                    playVideo(exercise.getId(), video.getFileName());
                }
            }, getContext());
            rvExercises.setAdapter(exerciseAdapter);
        } else {
            exerciseAdapter.updateData(exerciseList);
        }
    }

    public void onLanguageChanged(String language) {
        if (!isViewReady) {
            return;
        }

        this.currentLanguage = language;
        loadExercises();
    }

    private void playVideo(String folder, String fileName) {
        if (getContext() == null) return;

        Intent intent = new Intent(getContext(), VideoFullscreenActivity.class);
        intent.putExtra("folder", folder);
        intent.putExtra("fileName", fileName);
        startActivity(intent);
    }

    private String getTitleForFolder(String folder) {
        if (currentLanguage.equals("en")) {
            switch (folder) {
                case "BuildUp": return "Build Up";
                case "EyeTraining": return "Eye Training";
                case "SpeedTraining": return "Speed Training";
                default: return folder;
            }
        } else {
            switch (folder) {
                case "BuildUp": return "بیلد آپ";
                case "EyeTraining": return "تمرینات چشمی";
                case "SpeedTraining": return "تمرینات سرعتی";
                default: return folder;
            }
        }
    }

    private String getDescForFolder(String folder) {
        if (currentLanguage.equals("en")) {
            switch (folder) {
                case "BuildUp": return "Basic exercises";
                case "EyeTraining": return "Eye movement & focus";
                case "SpeedTraining": return "Increase reading speed";
                default: return "";
            }
        } else {
            switch (folder) {
                case "BuildUp": return "تمرینات پایه‌ای";
                case "EyeTraining": return "حرکات چشم و فوکوس";
                case "SpeedTraining": return "افزایش سرعت خواندن";
                default: return "";
            }
        }
    }

    private String getSubtitleForFolder(String folder) {
        if (currentLanguage.equals("en")) {
            switch (folder) {
                case "BuildUp": return "General eye speed exercise";
                case "EyeTraining": return "Choose one based on your speed level";
                case "SpeedTraining": return "Optional exercises for more speed";
                default: return "";
            }
        } else {
            switch (folder) {
                case "BuildUp": return "تمرین عمومی افزایش سرعت چشم";
                case "EyeTraining": return "یکی را بر اساس سرعت سطح خود انتخاب کنید";
                case "SpeedTraining": return "تمرینات اختیاری برای سرعت بیشتر";
                default: return "";
            }
        }
    }

    private int getIconForFolder(String folder) {
        switch (folder) {
            case "BuildUp": return R.drawable.ic_layer;
            case "EyeTraining": return R.drawable.ic_eye;
            case "SpeedTraining": return R.drawable.ic_tachometer;
            default: return R.drawable.ic_film;
        }
    }
}