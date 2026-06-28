package com.codestoon.speedreading.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codestoon.speedreading.R;
import com.codestoon.speedreading.activities.CommonImageGameActivity;
import com.codestoon.speedreading.activities.MazeGameActivity;
import com.codestoon.speedreading.activities.NumbersGameActivity;
import com.codestoon.speedreading.activities.WordCountGameActivity;
import com.codestoon.speedreading.models.GameDifficulty;
import com.codestoon.speedreading.models.GameItemModel;

import java.util.List;

public class GamesExpandableAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_DIFFICULTY = 1;

    private List<GameItemModel> games;
    private List<Boolean> expandedStates;
    private Context context;
    private String currentLanguage = "fa";

    public GamesExpandableAdapter(List<GameItemModel> games, Context context) {
        this.games = games;
        this.context = context;
        this.expandedStates = new java.util.ArrayList<>();
        for (int i = 0; i < games.size(); i++) {
            expandedStates.add(false);
        }
    }

    @Override
    public int getItemViewType(int position) {
        int headerCount = 0;
        for (int i = 0; i < games.size(); i++) {
            if (position == headerCount) {
                return TYPE_HEADER;
            }
            headerCount++;
            if (expandedStates.get(i)) {
                int difficultyCount = games.get(i).getDifficulties().size();
                if (position < headerCount + difficultyCount) {
                    return TYPE_DIFFICULTY;
                }
                headerCount += difficultyCount;
            }
        }
        return TYPE_HEADER;
    }

    @Override
    public int getItemCount() {
        int count = 0;
        for (int i = 0; i < games.size(); i++) {
            count++; // header
            if (expandedStates.get(i)) {
                count += games.get(i).getDifficulties().size();
            }
        }
        return count;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_game_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_game_difficulty, parent, false);
            return new DifficultyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int headerIndex = getHeaderIndex(position);
        int positionInHeader = getPositionInHeader(position);

        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            GameItemModel game = games.get(headerIndex);

            headerHolder.ivIcon.setImageResource(game.getIconRes());
            String title = currentLanguage.equals("fa") ? game.getTitle() : game.getTitleEn();
            headerHolder.tvTitle.setText(title);

            boolean isExpanded = expandedStates.get(headerIndex);
            headerHolder.ivArrow.setImageResource(isExpanded ? R.drawable.ic_arrow_up : R.drawable.ic_arrow_down);

            headerHolder.itemView.setOnClickListener(v -> {
                expandedStates.set(headerIndex, !expandedStates.get(headerIndex));
                notifyDataSetChanged();
            });

        } else if (holder instanceof DifficultyViewHolder) {
            DifficultyViewHolder diffHolder = (DifficultyViewHolder) holder;
            GameItemModel game = games.get(headerIndex);
            GameDifficulty difficulty = game.getDifficulties().get(positionInHeader);

            String diffName = currentLanguage.equals("fa") ? difficulty.getName() : difficulty.getNameEn();
            diffHolder.tvDifficulty.setText(diffName);

            // آیکون بر اساس سطح سختی
            int iconRes = getDifficultyIcon(difficulty.getLevel());
            diffHolder.ivDifficultyIcon.setImageResource(iconRes);

            diffHolder.itemView.setOnClickListener(v -> {
                // باز کردن اکتیویتی بازی با سطح سختی
                Intent intent = getGameIntent(game.getId(), difficulty, diffName, game);
                if (intent != null) {
                    context.startActivity(intent);
                }
            });
        }
    }

    private Intent getGameIntent(String gameId, GameDifficulty difficulty, String diffName, GameItemModel game) {
        Intent intent = null;
        String gameTitle = currentLanguage.equals("fa") ? game.getTitle() : game.getTitleEn();

        switch (gameId) {
            case "maze":
                intent = new Intent(context, MazeGameActivity.class);
                break;
            case "numbers":
                intent = new Intent(context, NumbersGameActivity.class);
                break;
            case "word_count":
                intent = new Intent(context, WordCountGameActivity.class);
                break;
            case "common_image":
                intent = new Intent(context, CommonImageGameActivity.class);
                break;
            default:
                return null;
        }

        intent.putExtra("difficulty_id", difficulty.getId());
        intent.putExtra("difficulty_level", difficulty.getLevel());
        intent.putExtra("difficulty_name", diffName);
        intent.putExtra("game_title", gameTitle);

        return intent;
    }

    private int getHeaderIndex(int position) {
        int count = 0;
        for (int i = 0; i < games.size(); i++) {
            if (position == count) {
                return i;
            }
            count++;
            if (expandedStates.get(i)) {
                int difficultyCount = games.get(i).getDifficulties().size();
                if (position < count + difficultyCount) {
                    return i;
                }
                count += difficultyCount;
            }
        }
        return 0;
    }

    private int getPositionInHeader(int position) {
        int count = 0;
        for (int i = 0; i < games.size(); i++) {
            if (position == count) {
                return -1;
            }
            count++;
            if (expandedStates.get(i)) {
                int difficultyCount = games.get(i).getDifficulties().size();
                if (position < count + difficultyCount) {
                    return position - count;
                }
                count += difficultyCount;
            }
        }
        return -1;
    }

    private int getDifficultyIcon(int level) {
        switch (level) {
            case 1: return R.drawable.ic_difficulty_easy;
            case 2: return R.drawable.ic_difficulty_medium;
            case 3: return R.drawable.ic_difficulty_hard;
            case 4: return R.drawable.ic_difficulty_very_hard;
            default: return R.drawable.ic_difficulty_easy;
        }
    }

    public void setLanguage(String language) {
        this.currentLanguage = language;
        notifyDataSetChanged();
    }

    // ====== ViewHolder ها ======

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon, ivArrow;
        TextView tvTitle;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivGameIcon);
            ivArrow = itemView.findViewById(R.id.ivGameArrow);
            tvTitle = itemView.findViewById(R.id.tvGameTitle);
        }
    }

    static class DifficultyViewHolder extends RecyclerView.ViewHolder {
        ImageView ivDifficultyIcon;
        TextView tvDifficulty;

        DifficultyViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDifficultyIcon = itemView.findViewById(R.id.ivDifficultyIcon);
            tvDifficulty = itemView.findViewById(R.id.tvDifficultyName);
        }
    }
}