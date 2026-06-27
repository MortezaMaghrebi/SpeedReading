package com.codestoon.speedreading.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codestoon.speedreading.R;
import com.codestoon.speedreading.adapters.ExerciseAdapter;
import com.codestoon.speedreading.adapters.VideoAdapter;
import com.codestoon.speedreading.models.ExerciseModel;
import com.codestoon.speedreading.models.VideoModel;
import com.codestoon.speedreading.utils.AssetsHelper;
import com.codestoon.speedreading.views.VideoFullscreenActivity;

import java.util.ArrayList;
import java.util.List;

public class ExercisesFragment extends Fragment {

    private RecyclerView rvExercises, rvVideos;
    private LinearLayout layoutVideoList;
    private TextView tvVideoListTitle, tvVideoCount;
    private View btnVideoBack;

    private ExerciseAdapter exerciseAdapter;
    private VideoAdapter videoAdapter;
    private List<ExerciseModel> exerciseList = new ArrayList<>();
    private List<VideoModel> videoList = new ArrayList<>();
    private String currentFolder = "";
    private String currentLanguage = "fa";

    private boolean isViewReady = false;
    private boolean isVideoListVisible = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercises, container, false);

        rvExercises = view.findViewById(R.id.rvExercises);
        rvVideos = view.findViewById(R.id.rvVideos);
        layoutVideoList = view.findViewById(R.id.layoutVideoList);
        tvVideoListTitle = view.findViewById(R.id.tvVideoListTitle);
        tvVideoCount = view.findViewById(R.id.tvVideoCount);
        btnVideoBack = view.findViewById(R.id.btnVideoBack);

        rvExercises.setLayoutManager(new LinearLayoutManager(getContext()));
        rvVideos.setLayoutManager(new LinearLayoutManager(getContext()));

        // همیشه داده‌ها رو بارگذاری کن
        loadExercises();
        setupVideoList();

        isViewReady = true;

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // هر بار که به این فرگمنت برمی‌گردیم، داده‌ها رو به‌روز کن
        if (isViewReady) {
            refreshData();
        }
    }

    private void refreshData() {
        // اگر لیست ویدیو باز است، آن را ببند
        if (isVideoListVisible) {
            hideVideoList();
        }
        // داده‌ها رو دوباره بارگذاری کن
        loadExercises();
    }

    private void loadExercises() {
        if (getContext() == null) return;

        List<String> folders = AssetsHelper.getVideoFolders(getContext());

        exerciseList.clear();
        for (String folder : folders) {
            String title = getTitleForFolder(folder);
            String desc = getDescForFolder(folder);
            int icon = getIconForFolder(folder);
            int count = AssetsHelper.getVideosInFolder(getContext(), folder).size();
            exerciseList.add(new ExerciseModel(folder, title, desc, icon, count));
        }

        if (exerciseAdapter == null) {
            exerciseAdapter = new ExerciseAdapter(exerciseList, exercise -> {
                showVideoList(exercise.getId());
            });
            rvExercises.setAdapter(exerciseAdapter);
        } else {
            exerciseAdapter.updateData(exerciseList);
        }
    }

    private void setupVideoList() {
        if (btnVideoBack == null) return;

        btnVideoBack.setOnClickListener(v -> hideVideoList());

        videoAdapter = new VideoAdapter(videoList, (video, position) -> {
            playVideo(currentFolder, video.getFileName());
        });
        rvVideos.setAdapter(videoAdapter);
    }

    public void onLanguageChanged(String language) {
        if (!isViewReady) {
            return;
        }

        this.currentLanguage = language;

        // اگر لیست ویدیو باز است، عنوان را به‌روز کن
        if (isVideoListVisible && !currentFolder.isEmpty()) {
            String title = getTitleForFolder(currentFolder);
            tvVideoListTitle.setText(title);
            int count = videoList.size();
            String countText = currentLanguage.equals("fa") ?
                    String.format("%d ویدیو", count) :
                    String.format("%d videos", count);
            tvVideoCount.setText(countText);
        }

        // داده‌های تمرینات را با زبان جدید به‌روز کن
        loadExercises();
    }

    private void showVideoList(String folder) {
        if (!isViewReady || getContext() == null) return;

        currentFolder = folder;
        videoList.clear();
        videoList.addAll(AssetsHelper.getVideosInFolder(getContext(), folder));

        String title = getTitleForFolder(folder);
        tvVideoListTitle.setText(title);
        int count = videoList.size();
        String countText = currentLanguage.equals("fa") ?
                String.format("%d ویدیو", count) :
                String.format("%d videos", count);
        tvVideoCount.setText(countText);
        videoAdapter.notifyDataSetChanged();

        rvExercises.setVisibility(View.GONE);
        layoutVideoList.setVisibility(View.VISIBLE);
        isVideoListVisible = true;
    }

    private void hideVideoList() {
        if (!isViewReady) return;

        rvExercises.setVisibility(View.VISIBLE);
        layoutVideoList.setVisibility(View.GONE);
        currentFolder = "";
        isVideoListVisible = false;

        // داده‌ها رو دوباره بارگذاری کن
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

    private int getIconForFolder(String folder) {
        switch (folder) {
            case "BuildUp": return R.drawable.ic_layer;
            case "EyeTraining": return R.drawable.ic_eye;
            case "SpeedTraining": return R.drawable.ic_tachometer;
            default: return R.drawable.ic_film;
        }
    }
}