package com.codestoon.speedreading.models;

import java.util.List;

public class GameItemModel {
    private String id;
    private String title;
    private String titleEn;
    private int iconRes;
    private List<GameDifficulty> difficulties;

    public GameItemModel(String id, String title, String titleEn, int iconRes, List<GameDifficulty> difficulties) {
        this.id = id;
        this.title = title;
        this.titleEn = titleEn;
        this.iconRes = iconRes;
        this.difficulties = difficulties;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getTitleEn() { return titleEn; }
    public int getIconRes() { return iconRes; }
    public List<GameDifficulty> getDifficulties() { return difficulties; }
}