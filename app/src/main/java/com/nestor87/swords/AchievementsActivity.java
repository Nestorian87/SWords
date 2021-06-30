package com.nestor87.swords;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;

public class AchievementsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DataManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        RecyclerView recyclerView = findViewById(R.id.achievementsList);
        AchievementAdapter achievementAdapter = new AchievementAdapter( this, Achievement.ACHIEVEMENTS);
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