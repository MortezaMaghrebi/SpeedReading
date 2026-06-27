package com.codestoon.speedreading.models;

public class GameModel {
    private String id;
    private String name;
    private int level;
    private double time; // زمان بر حسب ثانیه
    private long timestamp;
    private String date;

    public GameModel(String id, String name, int level, double time) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.time = time;
        this.timestamp = System.currentTimeMillis();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy/MM/dd - HH:mm", java.util.Locale.getDefault());
        this.date = sdf.format(new java.util.Date(timestamp));
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getLevel() { return level; }
    public double getTime() { return time; }
    public long getTimestamp() { return timestamp; }
    public String getDate() { return date; }
}