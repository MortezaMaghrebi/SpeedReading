package com.codestoon.speedreading.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TestResultModel {
    private int wpm;
    private long timestamp;
    private String date;

    public TestResultModel(int wpm) {
        this.wpm = wpm;
        this.timestamp = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        this.date = sdf.format(new Date(timestamp));
    }

    public int getWpm() { return wpm; }
    public long getTimestamp() { return timestamp; }
    public String getDate() { return date; }
}