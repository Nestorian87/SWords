package com.nestor87.swords.ui.bestPlayers;


import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.nestor87.swords.R;
import com.nestor87.swords.data.models.Player;
import com.nestor87.swords.ui.main.MainActivity;

import java.util.List;

public class PlayersAdapter extends RecyclerView.Adapter<PlayersAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private List<Player> players;

    PlayersAdapter(Context context, List<Player> players) {
        this.inflater = LayoutInflater.from(context);
        setPlayers(players);
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.best_players_list_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Player player = players.get(position);
        holder.nicknameTextView.setText(player.getName());
        holder.scoreTextView.setText(Integer.toString(player.getScore()));
        holder.hintsTextView.setText(Integer.toString(player.getHints()));
        holder.orderNumberTextView.setText(Integer.toString(position + 1));

        holder.nicknameTextView.setTextColor(MainActivity.getColorFromTheme(R.attr.buttonText, inflater.getContext()));

        if (position < 3) {
            holder.orderNumberTextView.setTextColor(MainActivity.getColorFromTheme(R.attr.wordText, inflater.getContext()));
        } else {
            holder.orderNumberTextView.setTextColor(MainActivity.getColorFromTheme(R.attr.buttonText, inflater.getContext()));
        }
        if (position == 0) {
            holder.nicknameTextView.setTextColor(MainActivity.getColorFromTheme(R.attr.redButton, inflater.getContext()));
            holder.orderNumberTextView.setTextColor(MainActivity.getColorFromTheme(R.attr.redButton, inflater.getContext()));
        } else if (position == 1) {
            holder.nicknameTextView.setTextColor(MainActivity.getColorFromTheme(R.attr.blueButton, inflater.getContext()));
            holder.orderNumberTextView.setTextColor(MainActivity.getColorFromTheme(R.attr.blueButton, inflater.getContext()));
        } else if (position == 2) {
            holder.nicknameTextView.setTextColor(MainActivity.getColorFromTheme(R.attr.yellowButton, inflater.getContext()));
            holder.orderNumberTextView.setTextColor(MainActivity.getColorFromTheme(R.attr.yellowButton, inflater.getContext()));
        }

        if (position < 10) {
            holder.nicknameTextView.setTypeface(holder.nicknameTextView.getTypeface(), Typeface.BOLD);
        } else {
            holder.nicknameTextView.setTypeface(holder.nicknameTextView.getTypeface(), Typeface.NORMAL);
        }
//        Toast.makeText(inflater.getContext(), String.valueOf(System.currentTimeMillis() - player.getLastTimeOnline()), Toast.LENGTH_SHORT).show();
        if (System.currentTimeMillis() - player.getLastTimeOnline() <= 15000) {
            holder.isOnline.setCardBackgroundColor(MainActivity.getColorFromTheme(R.attr.hint, inflater.getContext()));
        }  else {
            holder.isOnline.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nicknameTextView, scoreTextView, hintsTextView, orderNumberTextView;
        ConstraintLayout parentLayout;
        CardView isOnline;

        ViewHolder(View view) {
            super(view);
            nicknameTextView = view.findViewById(R.id.nicknameTextView);
            scoreTextView = view.findViewById(R.id.scoreTextView);
            hintsTextView = view.findViewById(R.id.hintsTextView);
            orderNumberTextView = view.findViewById(R.id.orderNumberTextView);
            parentLayout = view.findViewById(R.id.constraintLayout);
            isOnline = view.findViewById(R.id.isOnline);
        }
    }

}


