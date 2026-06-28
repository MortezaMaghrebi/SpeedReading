package com.codestoon.speedreading.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codestoon.speedreading.R;
import com.codestoon.speedreading.adapters.GamesExpandableAdapter;
import com.codestoon.speedreading.models.GameDifficulty;
import com.codestoon.speedreading.models.GameItemModel;

import java.util.ArrayList;
import java.util.List;

public class GamesFragment extends Fragment {

    private RecyclerView rvGames;
    private GamesExpandableAdapter adapter;
    private List<GameItemModel> gameList = new ArrayList<>();
    private String currentLanguage = "fa";
    private boolean isViewReady = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_games, container, false);

        rvGames = view.findViewById(R.id.rvGames);
        rvGames.setLayoutManager(new LinearLayoutManager(getContext()));

        loadGames();
        isViewReady = true;

        return view;
    }

    private void loadGames() {
        gameList.clear();

        // بازی ۱: ماز
        List<GameDifficulty> mazeDifficulties = new ArrayList<>();
        mazeDifficulties.add(new GameDifficulty("maze_easy", "آسان", "Easy", 1));
        mazeDifficulties.add(new GameDifficulty("maze_medium", "متوسط", "Medium", 2));
        mazeDifficulties.add(new GameDifficulty("maze_hard", "سخت", "Hard", 3));
        mazeDifficulties.add(new GameDifficulty("maze_very_hard", "خیلی سخت", "Very Hard", 4));
        gameList.add(new GameItemModel("maze", "🧩 ماز", "🧩 Maze", R.drawable.ic_game_maze, mazeDifficulties));

        // بازی ۲: اعداد
        List<GameDifficulty> numberDifficulties = new ArrayList<>();
        numberDifficulties.add(new GameDifficulty("numbers_easy", "آسان", "Easy", 1));
        numberDifficulties.add(new GameDifficulty("numbers_medium", "متوسط", "Medium", 2));
        numberDifficulties.add(new GameDifficulty("numbers_hard", "سخت", "Hard", 3));
        numberDifficulties.add(new GameDifficulty("numbers_very_hard", "خیلی سخت", "Very Hard", 4));
        gameList.add(new GameItemModel("numbers", "🔢 اعداد", "🔢 Numbers", R.drawable.ic_game_numbers, numberDifficulties));

        // بازی ۳: شمارش کلمه
        List<GameDifficulty> wordDifficulties = new ArrayList<>();
        wordDifficulties.add(new GameDifficulty("word_easy", "آسان", "Easy", 1));
        wordDifficulties.add(new GameDifficulty("word_medium", "متوسط", "Medium", 2));
        wordDifficulties.add(new GameDifficulty("word_hard", "سخت", "Hard", 3));
        wordDifficulties.add(new GameDifficulty("word_very_hard", "خیلی سخت", "Very Hard", 4));
        gameList.add(new GameItemModel("word_count", "📝 شمارش کلمه", "📝 Word Counter", R.drawable.ic_game_word, wordDifficulties));

        // بازی ۴: عکس مشترک
        List<GameDifficulty> imageDifficulties = new ArrayList<>();
        imageDifficulties.add(new GameDifficulty("image_easy", "آسان", "Easy", 1));
        imageDifficulties.add(new GameDifficulty("image_medium", "متوسط", "Medium", 2));
        imageDifficulties.add(new GameDifficulty("image_hard", "سخت", "Hard", 3));
        imageDifficulties.add(new GameDifficulty("image_very_hard", "خیلی سخت", "Very Hard", 4));
        gameList.add(new GameItemModel("common_image", "🖼️ عکس مشترک", "🖼️ Common Image", R.drawable.ic_game_image, imageDifficulties));

        if (adapter == null) {
            adapter = new GamesExpandableAdapter(gameList, getContext());
            rvGames.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    public void onLanguageChanged(String language) {
        if (!isViewReady) return;
        this.currentLanguage = language;
        if (adapter != null) {
            adapter.setLanguage(language);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isViewReady) {
            loadGames();
        }
    }
}