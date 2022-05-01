package com.nestor87.swords.ui.bonuses;


import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Paint;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nestor87.swords.R;
import com.nestor87.swords.data.managers.DataManager;
import com.nestor87.swords.data.models.Bonus;
import com.nestor87.swords.ui.main.MainActivity;
import com.nestor87.swords.utils.SystemUtils;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class BonusesAdapter extends RecyclerView.Adapter<BonusesAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private List<Bonus> bonuses;
    private DataManager dataManager;

    BonusesAdapter(Context context, DataManager dataManager) {
        this.inflater = LayoutInflater.from(context);
        this.bonuses = Bonus.Companion.getBONUSES();
        this.dataManager = dataManager;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.bonus_list_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Bonus bonus = bonuses.get(position);
        holder.icon.setImageResource(bonus.getIcon());
        holder.bonusTitleTextView.setText(bonus.getName());
        holder.bonusDescriptionTextView.setText(bonus.getDescription());

        String duration = bonus.getDurationInMinutes() + " " + inflater.getContext().getResources().getQuantityString(R.plurals.minute, bonus.getDurationInMinutes());
        holder.durationTextView.setText(inflater.getContext().getString(R.string.bonus_duration, duration));

        holder.priceTextView.setText(inflater.getContext().getString(R.string.bonus_price, bonus.getPrice()));
        holder.currencyImageView.setImageResource(bonus.getPriceCurrency().getIcon());
        holder.bonusCountTextView.setText(inflater.getContext().getString(R.string.bonus_count, bonus.getCount()));

        if (bonus.getCount() == 0) {
            holder.useButton.setVisibility(View.GONE);
        }

        if (bonus.isActive()) {
            holder.bonusTitleTextView.setTextColor(SystemUtils.getColorFromTheme(R.attr.hint, inflater.getContext()));
            holder.bonusTitleTextView.setPaintFlags(holder.bonusTitleTextView.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
            holder.clockTextView.setVisibility(View.VISIBLE);
            holder.useButton.setVisibility(View.GONE);

            new CountDownTimer(bonus.getExpireTime() - System.currentTimeMillis(), 500) {

                @Override
                public void onTick(long millis) {
                    holder.clockTextView.setText(
                            String.format(
                                    Locale.getDefault(),
                                    "%02d:%02d:%02d",
                                    TimeUnit.MILLISECONDS.toHours(millis),
                                    TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                                    TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)
                            )
                    );
                }

                @Override
                public void onFinish() {
                    notifyItemChanged(position);
                }
            }.start();

            if (holder.clockTextView.getText().equals("00:00:00")) {
                notifyItemChanged(position);
            }
        }

        holder.useButton.setOnClickListener(view -> {
            if (!bonus.isActive()) {
                if (bonus.getCount() > 0) {
                    bonus.activate();
                    bonus.setCount(bonus.getCount() - 1);
                    notifyItemChanged(position);
                }
            }
        });

        holder.buyButton.setOnClickListener(view -> {
            new AlertDialog.Builder(inflater.getContext())
                    .setTitle(R.string.bonus_purchase_confirmation_dialog_title)
                    .setMessage(R.string.bonus_purchase_confirmation_dialog_message)
                    .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                        try {
                            if (bonus.getPriceCurrency().getIcon() == R.drawable.score) {
                                dataManager.removeScore(bonus.getPrice());
                            } else if (bonus.getPriceCurrency().getIcon() == R.drawable.hints) {
                                dataManager.removeHints(bonus.getPrice());
                            }
                            bonus.setCount(bonus.getCount() + 1);
                            notifyItemChanged(position);
                        } catch (IllegalArgumentException e) { }
                    }).setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                    }).show();
        });


    }

    @Override
    public int getItemCount() {
        return bonuses.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView bonusTitleTextView, bonusDescriptionTextView, durationTextView, priceTextView, bonusCountTextView, clockTextView;
        ImageView currencyImageView, icon;
        Button useButton, buyButton;

        ViewHolder(View view) {
            super(view);
            icon = view.findViewById(R.id.bonusIcon);
            bonusTitleTextView = view.findViewById(R.id.bonusTitle);
            bonusDescriptionTextView = view.findViewById(R.id.bonusDescription);
            durationTextView = view.findViewById(R.id.bonusDuration);
            priceTextView = view.findViewById(R.id.bonusPrice);
            bonusCountTextView = view.findViewById(R.id.bonusCount);
            currencyImageView = view.findViewById(R.id.currencyImageView);
            useButton = view.findViewById(R.id.bonusUseButton);
            buyButton = view.findViewById(R.id.bonusBuyButton);
            clockTextView = view.findViewById(R.id.clockTextView);
        }
    }

}


