package com.codestoon.speedreading.models;

public class VideoModel {
    private String name;
    private String fileName;
    private String thumbPath; // مسیر thumbnail در Assets

    public VideoModel(String name, String fileName, String thumbPath) {
        this.name = name;
        this.fileName = fileName;
        this.thumbPath = thumbPath;
    }

    public String getName() { return name; }
    public String getFileName() { return fileName; }
    public String getThumbPath() { return thumbPath; }
    public void setName(String name) { this.name = name; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setThumbPath(String thumbPath) { this.thumbPath = thumbPath; }
}