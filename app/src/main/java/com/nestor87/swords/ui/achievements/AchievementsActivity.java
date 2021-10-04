package com.nestor87.swords.ui.achievements;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.nestor87.swords.data.DataManager;
import com.nestor87.swords.ui.main.MainActivity;
import com.nestor87.swords.R;
import com.nestor87.swords.data.models.Achievement;

import java.util.ArrayList;
import java.util.List;

public class AchievementsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DataManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        RecyclerView recyclerView = findViewById(R.id.achievementsList);
        AchievementAdapter achievementAdapter = new AchievementAdapter( this, getAchievementsSortedByGroupName(Achievement.ACHIEVEMENTS));
        recyclerView.setAdapter(achievementAdapter);

        String scrollToId = getIntent().getStringExtra("scrollTo");
        if (scrollToId != null) {
            for (int i = 0; i < achievementAdapter.getItemCount(); i++) {
                Achievement achievement = achievementAdapter.getSortedAchievements().get(i);

                if (achievement.getId().equals(scrollToId)) {
                    recyclerView.scrollToPosition(i);
                }
            }
        }
    }

    public List<List<Achievement>> getAchievementsSortedByGroupName(List<Achievement> achievements) {
        ArrayList<List<Achievement>> achievementGroups = new ArrayList<>();
        for (Achievement achievement : achievements) {
            boolean isAchievementAdded = false;
            for (List<Achievement> achievementGroup: achievementGroups) {
                if (achievementGroup.get(0).getGroupName().equals(achievement.getGroupName())) {
                    achievementGroup.add(achievement);
                    isAchievementAdded = true;
                    break;
                }
            }
            if (!isAchievementAdded) {
                List<Achievement> group = new ArrayList<>();
                group.add(achievement);
                achievementGroups.add(group);
            }
        }

        return achievementGroups;
    }

    public void goBack(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        Animatoo.animateSlideRight(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        MainActivity.onActivityStart(this);
    }

    @Override
    protected void onStop() {
        MainActivity.onActivityStop(this);
        super.onStop();
    }
}