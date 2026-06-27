package com.codestoon.speedreading.models;

public class VideoModel {
    private String name;
    private String fileName;

    public VideoModel(String name, String fileName) {
        this.name = name;
        this.fileName = fileName;
    }

    public String getName() { return name; }
    public String getFileName() { return fileName; }
}