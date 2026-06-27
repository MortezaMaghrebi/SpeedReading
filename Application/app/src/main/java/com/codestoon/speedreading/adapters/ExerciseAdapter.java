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

import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ViewHolder> {

    private List<ExerciseModel> exercises;
    private OnExerciseClickListener listener;
    private Context context;

    // نگهداری وضعیت باز/بسته بودن هر آیتم
    private boolean[] expandedStates;

    public interface OnExerciseClickListener {
        void onExerciseClick(ExerciseModel exercise);
        void onVideoClick(ExerciseModel exercise, VideoModel video);
    }

    public ExerciseAdapter(List<ExerciseModel> exercises, OnExerciseClickListener listener, Context context) {
        this.exercises = exercises;
        this.listener = listener;
        this.context = context;
        this.expandedStates = new boolean[exercises.size()];
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExerciseModel exercise = exercises.get(position);

        // تنظیم اطلاعات اصلی
        holder.ivIcon.setImageResource(exercise.getIconRes());
        holder.tvTitle.setText(exercise.getTitle());
        holder.tvDesc.setText(exercise.getDescription());
        holder.tvBadge.setText(String.valueOf(exercise.getVideoCount()));

        // تنظیم لیست ویدیوها
        List<VideoModel> videos = exercise.getVideos();
        if (videos != null && !videos.isEmpty()) {
            holder.rvVideos.setLayoutManager(new LinearLayoutManager(context));
            VideoAdapter videoAdapter = new VideoAdapter(videos, (video, pos) -> {
                if (listener != null) {
                    listener.onVideoClick(exercise, video);
                }
            });
            holder.rvVideos.setAdapter(videoAdapter);
            holder.rvVideos.setVisibility(expandedStates[position] ? View.VISIBLE : View.GONE);
        } else {
            holder.rvVideos.setVisibility(View.GONE);
        }

        // کلیک روی آیتم برای باز/بسته کردن
        holder.itemView.setOnClickListener(v -> {
            // تغییر وضعیت باز/بسته
            expandedStates[position] = !expandedStates[position];

            // به‌روزرسانی آیکون فلش
            if (expandedStates[position]) {
                holder.ivArrow.setImageResource(R.drawable.ic_arrow_up);
            } else {
                holder.ivArrow.setImageResource(R.drawable.ic_arrow_down);
            }

            // نمایش/مخفی کردن لیست ویدیوها
            if (holder.rvVideos.getAdapter() != null && holder.rvVideos.getAdapter().getItemCount() > 0) {
                holder.rvVideos.setVisibility(expandedStates[position] ? View.VISIBLE : View.GONE);
            }

            // اطلاع به کلیک‌کننده (اختیاری)
            if (listener != null) {
                listener.onExerciseClick(exercise);
            }
        });

        // تنظیم آیکون فلش بر اساس وضعیت فعلی
        if (expandedStates[position]) {
            holder.ivArrow.setImageResource(R.drawable.ic_arrow_up);
        } else {
            holder.ivArrow.setImageResource(R.drawable.ic_arrow_down);
        }
    }

    @Override
    public int getItemCount() {
        return exercises != null ? exercises.size() : 0;
    }

    // متد برای به‌روز کردن داده‌ها
    public void updateData(List<ExerciseModel> newExercises) {
        this.exercises = newExercises;
        this.expandedStates = new boolean[newExercises.size()];
        notifyDataSetChanged();
    }

    public void updateCount(String id, int count) {
        for (ExerciseModel exercise : exercises) {
            if (exercise.getId().equals(id)) {
                exercise.setVideoCount(count);
                break;
            }
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon, ivArrow;
        TextView tvTitle, tvDesc, tvBadge;
        RecyclerView rvVideos;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivExerciseIcon);
            tvTitle = itemView.findViewById(R.id.tvExerciseTitle);
            tvDesc = itemView.findViewById(R.id.tvExerciseDesc);
            tvBadge = itemView.findViewById(R.id.tvExerciseBadge);
            ivArrow = itemView.findViewById(R.id.ivArrow);
            rvVideos = itemView.findViewById(R.id.rvVideos);
        }
    }
}