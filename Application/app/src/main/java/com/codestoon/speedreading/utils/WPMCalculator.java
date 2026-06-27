package com.codestoon.speedreading.utils;

public class WPMCalculator {

    public static int calculateWPM(String text, long seconds) {
        if (seconds <= 0) return 0;
        int words = text.trim().split("\\s+").length;
        double minutes = seconds / 60.0;
        if (minutes <= 0) return 0;
        return (int) Math.round(words / minutes);
    }

    public static int countWords(String text) {
        return text.trim().split("\\s+").length;
    }
}