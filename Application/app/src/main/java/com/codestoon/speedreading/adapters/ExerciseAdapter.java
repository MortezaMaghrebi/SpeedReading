package com.codestoon.speedreading.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codestoon.speedreading.R;
import com.codestoon.speedreading.models.ExerciseModel;
import com.codestoon.speedreading.models.VideoModel;

import java.util.ArrayList;
import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<ExerciseModel> exercises;
    private OnExerciseClickListener listener;
    private Context context;

    // لیست برای نگهداری وضعیت باز/بسته هر آیتم تمرین (بدون Header)
    private List<Boolean> expandedStates = new ArrayList<>();

    public interface OnExerciseClickListener {
        void onExerciseClick(ExerciseModel exercise);
        void onVideoClick(ExerciseModel exercise, VideoModel video);
    }

    public ExerciseAdapter(List<ExerciseModel> exercises, OnExerciseClickListener listener, Context context) {
        this.exercises = exercises;
        this.listener = listener;
        this.context = context;
        initExpandedStates();
    }

    private void initExpandedStates() {
        expandedStates.clear();
        for (ExerciseModel exercise : exercises) {
            if (!exercise.isHeader()) {
                expandedStates.add(false);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (exercises.get(position).isHeader()) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_exercise_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_exercise, parent, false);
            return new ExerciseViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ExerciseModel exercise = exercises.get(position);

        if (holder instanceof HeaderViewHolder) {
            // Header
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.tvTitle.setText(exercise.getTitle());
            if (exercise.getSubtitle() != null && !exercise.getSubtitle().isEmpty()) {
                headerHolder.tvSubtitle.setVisibility(View.VISIBLE);
                headerHolder.tvSubtitle.setText(exercise.getSubtitle());
            } else {
                headerHolder.tvSubtitle.setVisibility(View.GONE);
            }
        } else if (holder instanceof ExerciseViewHolder) {
            // آیتم تمرین
            ExerciseViewHolder itemHolder = (ExerciseViewHolder) holder;

            // تنظیم اطلاعات اصلی
            itemHolder.ivIcon.setImageResource(exercise.getIconRes());
            itemHolder.tvTitle.setText(exercise.getTitle());
            itemHolder.tvDesc.setText(exercise.getDescription());
            itemHolder.tvBadge.setText(String.valueOf(exercise.getVideoCount()));

            // تنظیم لیست ویدیوها
            List<VideoModel> videos = exercise.getVideos();
            if (videos != null && !videos.isEmpty()) {
                itemHolder.rvVideos.setLayoutManager(new LinearLayoutManager(context));
                VideoAdapter videoAdapter = new VideoAdapter(videos, (video, pos) -> {
                    if (listener != null) {
                        listener.onVideoClick(exercise, video);
                    }
                });
                itemHolder.rvVideos.setAdapter(videoAdapter);

                // پیدا کردن اندیس آیتم در expandedStates
                int itemIndex = getItemIndex(position);
                if (itemIndex >= 0 && itemIndex < expandedStates.size()) {
                    boolean isExpanded = expandedStates.get(itemIndex);
                    itemHolder.layoutVideoContainer.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

                    // تنظیم آیکون فلش
                    if (isExpanded) {
                        itemHolder.ivArrow.setImageResource(R.drawable.ic_arrow_up);
                    } else {
                        itemHolder.ivArrow.setImageResource(R.drawable.ic_arrow_down);
                    }
                    itemHolder.ivArrow.setRotation(0);
                }
            } else {
                itemHolder.layoutVideoContainer.setVisibility(View.GONE);
            }

            // کلیک روی آیتم برای باز/بسته کردن
            itemHolder.itemView.setOnClickListener(v -> {
                int itemIndex = getItemIndex(position);
                if (itemIndex >= 0 && itemIndex < expandedStates.size()) {
                    boolean currentState = expandedStates.get(itemIndex);
                    expandedStates.set(itemIndex, !currentState);

                    boolean newState = expandedStates.get(itemIndex);

                    if (newState) {
                        itemHolder.ivArrow.setImageResource(R.drawable.ic_arrow_up);
                        itemHolder.ivArrow.animate().rotation(0).setDuration(200).start();
                    } else {
                        itemHolder.ivArrow.setImageResource(R.drawable.ic_arrow_down);
                        itemHolder.ivArrow.animate().rotation(0).setDuration(200).start();
                    }

                    if (itemHolder.rvVideos.getAdapter() != null && itemHolder.rvVideos.getAdapter().getItemCount() > 0) {
                        itemHolder.layoutVideoContainer.setVisibility(newState ? View.VISIBLE : View.GONE);
                    }

                    if (listener != null) {
                        listener.onExerciseClick(exercise);
                    }
                }
            });
        }
    }

    // متد برای پیدا کردن اندیس آیتم در لیست expandedStates
    private int getItemIndex(int position) {
        int itemIndex = 0;
        for (int i = 0; i <= position; i++) {
            if (!exercises.get(i).isHeader()) {
                if (i == position) {
                    return itemIndex;
                }
                itemIndex++;
            }
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return exercises != null ? exercises.size() : 0;
    }

    public void updateData(List<ExerciseModel> newExercises) {
        this.exercises = newExercises;
        initExpandedStates();
        notifyDataSetChanged();
    }

    public void updateCount(String id, int count) {
        for (ExerciseModel exercise : exercises) {
            if (!exercise.isHeader() && exercise.getId().equals(id)) {
                exercise.setVideoCount(count);
                break;
            }
        }
        notifyDataSetChanged();
    }

    // ViewHolder برای Header
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvHeaderTitle);
            tvSubtitle = itemView.findViewById(R.id.tvHeaderSubtitle);
        }
    }

    // ViewHolder برای آیتم تمرین
    static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon, ivArrow;
        TextView tvTitle, tvDesc, tvBadge;
        RecyclerView rvVideos;
        LinearLayout layoutVideoContainer;

        ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivExerciseIcon);
            tvTitle = itemView.findViewById(R.id.tvExerciseTitle);
            tvDesc = itemView.findViewById(R.id.tvExerciseDesc);
            tvBadge = itemView.findViewById(R.id.tvExerciseBadge);
            ivArrow = itemView.findViewById(R.id.ivArrow);
            rvVideos = itemView.findViewById(R.id.rvVideos);
            layoutVideoContainer = itemView.findViewById(R.id.layoutVideoContainer);
        }
    }
}