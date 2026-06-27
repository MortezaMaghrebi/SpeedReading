package com.codestoon.speedreading.models;

import java.util.List;

public class ExerciseModel {
    private String id;
    private String title;
    private String description;
    private String subtitle;
    private int iconRes;
    private int videoCount;
    private List<VideoModel> videos;
    private boolean isHeader; // مشخص می‌کند که این آیتم Header است یا خیر

    // سازنده برای Header
    public ExerciseModel(String id, String title, String subtitle) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.isHeader = true;
        this.videoCount = 0;
        this.videos = null;
    }

    // سازنده برای آیتم ویدیو
    public ExerciseModel(String id, String title, String description, int iconRes, int videoCount, List<VideoModel> videos) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.iconRes = iconRes;
        this.videoCount = videoCount;
        this.videos = videos;
        this.isHeader = false;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getSubtitle() { return subtitle; }
    public int getIconRes() { return iconRes; }
    public int getVideoCount() { return videoCount; }
    public List<VideoModel> getVideos() { return videos; }
    public boolean isHeader() { return isHeader; }
    public void setVideoCount(int videoCount) { this.videoCount = videoCount; }
    public void setVideos(List<VideoModel> videos) { this.videos = videos; }
}