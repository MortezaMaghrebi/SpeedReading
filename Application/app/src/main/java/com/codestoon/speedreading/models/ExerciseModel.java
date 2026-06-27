package com.codestoon.speedreading.models;

import java.util.List;

public class ExerciseModel {
    private String id;
    private String title;
    private String description;
    private int iconRes;
    private int videoCount;
    private List<VideoModel> videos;

    public ExerciseModel(String id, String title, String description, int iconRes, int videoCount, List<VideoModel> videos) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.iconRes = iconRes;
        this.videoCount = videoCount;
        this.videos = videos;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getIconRes() { return iconRes; }
    public int getVideoCount() { return videoCount; }
    public List<VideoModel> getVideos() { return videos; }
    public void setVideoCount(int videoCount) { this.videoCount = videoCount; }
    public void setVideos(List<VideoModel> videos) { this.videos = videos; }
}