package com.codestoon.speedreading.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codestoon.speedreading.R;
import com.codestoon.speedreading.models.VideoModel;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {

    private List<VideoModel> videos;
    private OnVideoClickListener listener;

    public interface OnVideoClickListener {
        void onVideoClick(VideoModel video, int position);
    }

    public VideoAdapter(List<VideoModel> videos, OnVideoClickListener listener) {
        this.videos = videos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_video, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VideoModel video = videos.get(position);
        holder.tvName.setText(video.getName());
        holder.tvFile.setText(video.getFileName());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onVideoClick(video, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvFile;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvVideoName);
            tvFile = itemView.findViewById(R.id.tvVideoFile);
        }
    }
}