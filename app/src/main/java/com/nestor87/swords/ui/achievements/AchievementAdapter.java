package com.nestor87.swords.ui.achievements;


import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nestor87.swords.data.DBHelper;
import com.nestor87.swords.data.DataManager;
import com.nestor87.swords.ui.main.MainActivity;
import com.nestor87.swords.R;
import com.nestor87.swords.data.models.Achievement;

import java.util.ArrayList;
import java.util.List;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private List<List<Achievement>> achievementGroups;
    public Context context;
    public static DataManager dataManager;
    private @LayoutRes int layout;

    public AchievementAdapter(Context context, List<List<Achievement>> achievementGroups, @LayoutRes int layout) {
        this.achievementGroups = achievementGroups;
//        Collections.sort(this.achievements, (o1, o2) -> {
//            int progress1 = (int) ((double) o1.getCurrentProgress() / o1.getMaxProgress() * 100);
//            int progress2 = (int) ((double) o2.getCurrentProgress() / o2.getMaxProgress() * 100);
//
//            return progress2 - progress1;
//        });
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.layout = layout;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        List<Achievement> achievementGroup = achievementGroups.get(position);
        for (Achievement achievement : achievementGroup) {
            if (achievement.isRewardReceived() || layout == R.layout.profile_achievement_list_item && achievement.isCompleted() && achievementGroup.indexOf(achievement) != achievementGroup.size() - 1) {
                continue;
            }

            holder.progressTextView.setText(achievement.getCurrentProgress() + " / " + achievement.getMaxProgress());
            holder.titleTextView.setText(achievement.getTitle());
            holder.progressBar.setMax(achievement.getMaxProgress());
            holder.progressBar.setProgress(achievement.getCurrentProgress());
            String taskString = achievement.getTask().replace("{x}", Integer.toString(achievement.getMaxProgress())) + " ";
            if (achievement.getPluralsResource() != -1)
                taskString = taskString.replace("{w}", context.getResources().getQuantityString(achievement.getPluralsResource(), achievement.getMaxProgress()));
            holder.taskTextView.setText(taskString);

            boolean isTaskHasImage = Achievement.getCurrencyImageByTrigger(achievement.getProgressTrigger()) != -1,
                    isCustomCurrencyReward = achievement.getRewardCurrency().isCustom();

            if (isTaskHasImage) {
                holder.currencyImageView1.setVisibility(View.VISIBLE);
                holder.currencyImageView1.setImageResource(Achievement.getCurrencyImageByTrigger(achievement.getProgressTrigger()));
            } else {
                holder.currencyImageView1.setVisibility(View.GONE);
            }

            if (!isCustomCurrencyReward) {
                holder.currencyImageView2.setVisibility(View.VISIBLE);
                holder.currencyImageView2.setImageResource(achievement.getRewardCurrency().getIcon());
                holder.rewardTextView.setText("Награда: " + Integer.toString(achievement.getRewardCount()) + " ");
            } else {
                holder.rewardTextView.setText("Награда: " + achievement.getRewardCurrency().getText() + " ");
            }

            if (achievement.isCompleted()) {

                holder.titleTextView.setText(holder.titleTextView.getText() + " ✓");
                holder.titleTextView.setTextColor(MainActivity.getColorFromTheme(R.attr.hint, context));
                holder.progressBar.getProgressDrawable().setColorFilter(MainActivity.getColorFromTheme(R.attr.hint, context), PorterDuff.Mode.SRC_IN);
                holder.titleTextView.setTypeface(null, Typeface.BOLD);

                if (!achievement.isRewardReceived() && layout != R.layout.profile_achievement_list_item)
                    holder.rewardButton.setVisibility(View.VISIBLE);
            } else {
                holder.titleTextView.setTextColor(MainActivity.getColorFromTheme(R.attr.scoreAndHintsText, context));
                holder.progressBar.getProgressDrawable().setColorFilter(MainActivity.getColorFromTheme(R.attr.buttonText, context), PorterDuff.Mode.SRC_IN);
                holder.titleTextView.setTypeface(null, Typeface.NORMAL);
            }

            holder.rewardButton.setOnClickListener(v -> {
                MainActivity.playSound(R.raw.buy, context);
                holder.rewardButton.setVisibility(View.GONE);
                dataManager = new DataManager(
                        v.getRootView().findViewById(R.id.score), v.getRootView().findViewById(R.id.hints),
                        null, new Button[]{}, new DBHelper(context), context);
                dataManager.loadData();

                if (achievement.getRewardCurrency().equals(Achievement.SCORE_CURRENCY)) {
                    dataManager.addScore(achievement.getRewardCount());
                } else if (achievement.getRewardCurrency().equals(Achievement.HINTS_CURRENCY)) {
                    dataManager.addHints(achievement.getRewardCount());
                }

                achievement.setRewardReceived(true);
                notifyDataSetChanged();


            });

            holder.stageTextView.setText("Этап " + (achievementGroup.indexOf(achievement) + 1) + " из " + achievementGroup.size());
            if (achievementGroup.indexOf(achievement) == achievementGroup.size() - 1) {
                holder.stageTextView.setTextColor(MainActivity.getColorFromTheme(R.attr.hint, context));
            }
        break;
        }
    }

    @Override
    public int getItemCount() {
        return achievementGroups.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;
        TextView titleTextView, progressTextView, rewardTextView, taskTextView, stageTextView;
        ImageView currencyImageView1, currencyImageView2;
        Button rewardButton;

        ViewHolder(View view) {
            super(view);
            progressBar = view.findViewById(R.id.achievementProgressBar);
            titleTextView = view.findViewById(R.id.textViewTitle);
            progressTextView = view.findViewById(R.id.textViewProgress);
            rewardTextView = view.findViewById(R.id.textViewReward);
            taskTextView = view.findViewById(R.id.textViewTask);
            currencyImageView1 = view.findViewById(R.id.currencyImageView1);
            currencyImageView2 = view.findViewById(R.id.currencyImageView2);
            rewardButton = view.findViewById(R.id.getRewardButton);
            stageTextView = view.findViewById(R.id.stageTextView);
        }
    }

    public List<Achievement> getSortedAchievements() {
        List<Achievement> sortedAchievements = new ArrayList<>();
        for (List<Achievement> achievementGroup : achievementGroups) {
            sortedAchievements.addAll(achievementGroup);
        }
        return sortedAchievements;
    }
}


