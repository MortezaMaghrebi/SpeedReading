package com.codestoon.speedreading.models;

public class GameDifficulty {
    private String id;
    private String name;
    private String nameEn;
    private int level;

    public GameDifficulty(String id, String name, String nameEn, int level) {
        this.id = id;
        this.name = name;
        this.nameEn = nameEn;
        this.level = level;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getNameEn() { return nameEn; }
    public int getLevel() { return level; }
}