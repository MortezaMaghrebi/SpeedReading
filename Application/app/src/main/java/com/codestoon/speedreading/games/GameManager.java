package com.codestoon.speedreading.games;

import android.content.Context;

import com.codestoon.speedreading.games.base.BaseGame;
import com.codestoon.speedreading.games.commonimage.CommonImageGame;
import com.codestoon.speedreading.games.maze.MazeGame;
import com.codestoon.speedreading.games.numbers.NumbersGame;
import com.codestoon.speedreading.games.wordcount.WordCountGame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameManager {

    private static GameManager instance;
    private Map<String, BaseGame> games = new HashMap<>();
    private List<String> gameIds = new ArrayList<>();

    private GameManager() {}

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    public void init(Context context) {
        // ثبت همه بازی‌ها
        registerGame(new MazeGame(context));
        registerGame(new NumbersGame(context));
        registerGame(new WordCountGame(context));
        registerGame(new CommonImageGame(context));
    }

    private void registerGame(BaseGame game) {
        games.put(game.getGameId(), game);
        gameIds.add(game.getGameId());
    }

    public BaseGame getGame(String gameId) {
        return games.get(gameId);
    }

    public List<String> getGameIds() {
        return gameIds;
    }

    public List<BaseGame> getAllGames() {
        List<BaseGame> result = new ArrayList<>();
        for (String id : gameIds) {
            result.add(games.get(id));
        }
        return result;
    }

    // برای اضافه کردن بازی جدید در آینده
    public void addGame(BaseGame game) {
        registerGame(game);
    }
}