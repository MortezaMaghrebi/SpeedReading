package com.codestoon.speedreading.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codestoon.speedreading.R;
import com.codestoon.speedreading.models.VideoModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {

    private List<VideoModel> videos;
    private OnVideoClickListener listener;
    private Context context;

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
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_video, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VideoModel video = videos.get(position);
        holder.tvName.setText(video.getName());
        holder.tvFile.setText(video.getFileName());

        // بارگذاری thumbnail
        loadThumbnail(holder, video);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onVideoClick(video, position);
            }
        });
    }

    private void loadThumbnail(ViewHolder holder, VideoModel video) {
        if (video.getThumbPath() != null && context != null) {
            try {
                // بارگذاری thumbnail از Assets
                InputStream inputStream = context.getAssets().open(video.getThumbPath());
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                if (bitmap != null) {
                    holder.ivThumbnail.setImageBitmap(bitmap);
                } else {
                    holder.ivThumbnail.setImageResource(R.drawable.ic_video_placeholder);
                }
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                holder.ivThumbnail.setImageResource(R.drawable.ic_video_placeholder);
            }
        } else {
            // اگر thumbnail وجود نداشت، از تصویر پیش‌فرض استفاده کن
            holder.ivThumbnail.setImageResource(R.drawable.ic_video_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return videos != null ? videos.size() : 0;
    }

    // متد برای به‌روز کردن داده‌ها
    public void updateData(List<VideoModel> newVideos) {
        this.videos = newVideos;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvFile;
        ImageView ivThumbnail;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvVideoName);
            tvFile = itemView.findViewById(R.id.tvVideoFile);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
        }
    }
}