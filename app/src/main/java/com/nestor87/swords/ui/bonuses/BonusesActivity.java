package com.nestor87.swords.ui.bonuses;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.nestor87.swords.R;
import com.nestor87.swords.data.helpers.DBHelper;
import com.nestor87.swords.data.managers.DataManager;
import com.nestor87.swords.ui.BaseActivity;
import com.nestor87.swords.ui.main.MainActivity;

public class BonusesActivity extends BaseActivity {
    BonusesAdapter bonusesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bonuses);

        init();
    }

    private void init() {
        DataManager dataManager = new DataManager(findViewById(R.id.score), findViewById(R.id.hints), null, new Button[]{}, new DBHelper(this), this);
        dataManager.loadData();
        RecyclerView recyclerView = findViewById(R.id.bonusesList);
        bonusesAdapter = new BonusesAdapter(this, dataManager);
        recyclerView.setAdapter(bonusesAdapter);
    }

    public void goBack(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        Animatoo.animateSlideRight(this);
    }
}