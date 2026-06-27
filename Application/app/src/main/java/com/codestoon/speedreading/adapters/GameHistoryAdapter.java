package com.codestoon.speedreading.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codestoon.speedreading.R;
import com.codestoon.speedreading.models.GameModel;

import java.util.List;

public class GameHistoryAdapter extends RecyclerView.Adapter<GameHistoryAdapter.ViewHolder> {

    private List<GameModel> history;

    public GameHistoryAdapter(List<GameModel> history) {
        this.history = history;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_game_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GameModel game = history.get(position);
        holder.tvLevel.setText("Lv." + game.getLevel());
        holder.tvTime.setText(String.format("%.2fs", game.getTime()));
        holder.tvDate.setText(game.getDate());
    }

    @Override
    public int getItemCount() {
        return history != null ? history.size() : 0;
    }

    public void updateData(List<GameModel> newHistory) {
        this.history = newHistory;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLevel, tvTime, tvDate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLevel = itemView.findViewById(R.id.tvGameLevel);
            tvTime = itemView.findViewById(R.id.tvGameTime);
            tvDate = itemView.findViewById(R.id.tvGameDate);
        }
    }
}