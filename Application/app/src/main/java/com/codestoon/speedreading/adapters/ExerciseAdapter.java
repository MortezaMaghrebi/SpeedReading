package com.codestoon.speedreading.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codestoon.speedreading.R;
import com.codestoon.speedreading.models.ExerciseModel;

import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ViewHolder> {

    private List<ExerciseModel> exercises;
    private OnExerciseClickListener listener;

    public interface OnExerciseClickListener {
        void onExerciseClick(ExerciseModel exercise);
    }

    public ExerciseAdapter(List<ExerciseModel> exercises, OnExerciseClickListener listener) {
        this.exercises = exercises;
        this.listener = listener;
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
        holder.ivIcon.setImageResource(exercise.getIconRes());
        holder.tvTitle.setText(exercise.getTitle());
        holder.tvDesc.setText(exercise.getDescription());
        holder.tvBadge.setText(String.valueOf(exercise.getVideoCount()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onExerciseClick(exercise);
            }
        });
    }

    @Override
    public int getItemCount() {
        return exercises != null ? exercises.size() : 0;
    }

    // متد جدید برای به‌روز کردن داده‌ها
    public void updateData(List<ExerciseModel> newExercises) {
        this.exercises = newExercises;
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
        ImageView ivIcon;
        TextView tvTitle, tvDesc, tvBadge;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivExerciseIcon);
            tvTitle = itemView.findViewById(R.id.tvExerciseTitle);
            tvDesc = itemView.findViewById(R.id.tvExerciseDesc);
            tvBadge = itemView.findViewById(R.id.tvExerciseBadge);
        }
    }
}