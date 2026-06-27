package com.codestoon.speedreading.models;

public class ExerciseModel {
    private String id;
    private String title;
    private String description;
    private int iconRes;
    private int videoCount;

    public ExerciseModel(String id, String title, String description, int iconRes, int videoCount) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.iconRes = iconRes;
        this.videoCount = videoCount;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getIconRes() { return iconRes; }
    public int getVideoCount() { return videoCount; }
    public void setVideoCount(int videoCount) { this.videoCount = videoCount; }
}