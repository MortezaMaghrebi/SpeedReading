package com.codestoon.speedreading.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codestoon.speedreading.R;
import com.codestoon.speedreading.models.TestResultModel;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<TestResultModel> history;

    public HistoryAdapter(List<TestResultModel> history) {
        this.history = history;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TestResultModel result = history.get(position);
        int index = position + 1;
        holder.tvIndex.setText("#" + index);
        holder.tvWpm.setText(String.valueOf(result.getWpm()));
        holder.tvDate.setText(result.getDate());
    }

    @Override
    public int getItemCount() {
        return history != null ? history.size() : 0;
    }

    public void updateData(List<TestResultModel> newHistory) {
        this.history = newHistory;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIndex, tvWpm, tvDate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIndex = itemView.findViewById(R.id.tvHistoryIndex);
            tvWpm = itemView.findViewById(R.id.tvHistoryWpm);
            tvDate = itemView.findViewById(R.id.tvHistoryDate);
        }
    }
}