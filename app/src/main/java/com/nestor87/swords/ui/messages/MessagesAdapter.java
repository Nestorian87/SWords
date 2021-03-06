package com.nestor87.swords.ui.messages;


import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nestor87.swords.R;
import com.nestor87.swords.data.managers.DataManager;
import com.nestor87.swords.data.models.MessageInfo;
import com.nestor87.swords.data.models.MessageReward;
import com.nestor87.swords.data.models.MessageRewardReceivedRequest;
import com.nestor87.swords.data.network.NetworkService;
import com.nestor87.swords.ui.main.MainActivity;
import com.nestor87.swords.utils.SystemUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private List<MessageInfo> messages;
    private DataManager dataManager;
    private SharedPreferences.Editor sharedPreferencesEditor;

    MessagesAdapter(Context context, List<MessageInfo> messages, DataManager dataManager) {
        this.inflater = LayoutInflater.from(context);
        this.dataManager = dataManager;
        setMessages(messages);
        sharedPreferencesEditor = context.getSharedPreferences(MainActivity.APP_PREFERENCES_FILE_NAME, 0).edit();
    }

    public void setMessages(List<MessageInfo> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.message_list_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MessageInfo message = messages.get(position);
        if (message != null && message.getMessage() != null) {
            holder.titleTextView.setText(message.getMessage().getTitle());
            holder.bodyTextView.setText(message.getMessage().getBody());
            holder.dateTimeTextView.setText(message.getDateTime());

            MessageReward messageReward = message.getMessage().getReward();
            if (messageReward != null && messageReward.getScore() != 0 || messageReward.getHints() != 0 || messageReward.hasSharedPreferencesModification()) {
                holder.rewardGroup.setVisibility(View.VISIBLE);
                holder.scoreRewardCountTextView.setText(SystemUtils.formatBigNumber(messageReward.getScore()));
                holder.hintsRewardCountTextView.setText(SystemUtils.formatBigNumber(messageReward.getHints()));
                holder.rewardTitleTextView.setText(messageReward.getTitle() + ":");

                if (messageReward.getScore() != 0) {
                    holder.scoreGroup.setVisibility(View.VISIBLE);
                } else {
                    holder.scoreGroup.setVisibility(View.GONE);
                }

                if (messageReward.getHints() != 0) {
                    holder.hintsGroup.setVisibility(View.VISIBLE);
                } else {
                    holder.hintsGroup.setVisibility(View.GONE);
                }

                if (messageReward.isReceived()) {
                    holder.getRewardButton.setVisibility(View.GONE);
                } else {
                    holder.getRewardButton.setVisibility(View.VISIBLE);
                    holder.getRewardButton.setOnClickListener(v -> {
                        holder.getRewardButton.setEnabled(false);

                        NetworkService.getInstance().getSWordsApi().setRewardReceived(MainActivity.getBearerToken(), new MessageRewardReceivedRequest(message.getId(), message.getAccountId())).enqueue(
                                new Callback<Void>() {
                                    @Override
                                    public void onResponse(Call<Void> call, Response<Void> response) {
                                        if (messageReward.getScore() != 0) {
                                            if (messageReward.getScore() > 0) {
                                                dataManager.addScore(messageReward.getScore());
                                            } else {
                                                dataManager.removeScore(-messageReward.getScore());
                                            }
                                        }
                                        if (messageReward.getHints() != 0) {
                                            if (messageReward.getHints() > 0) {
                                                dataManager.addHints(messageReward.getHints());
                                            } else {
                                                try {
                                                    dataManager.removeHints(-messageReward.getHints());
                                                } catch (IllegalArgumentException e) { }
                                            }
                                        }
                                        if (messageReward.hasSharedPreferencesModification()) {
                                            try {
                                                if (messageReward.getSharedPreferencesType().equals("int")) {
                                                    sharedPreferencesEditor.putInt(
                                                            messageReward.getSharedPreferencesKey(),
                                                            Integer.parseInt(
                                                                    messageReward.getSharedPreferencesValue()
                                                            )
                                                    ).apply();
                                                } else if (messageReward.getSharedPreferencesType().equals("boolean")) {
                                                    sharedPreferencesEditor.putBoolean(
                                                            messageReward.getSharedPreferencesKey(),
                                                            messageReward.getSharedPreferencesValue().equals("true")
                                                    ).apply();
                                                } else if (messageReward.getSharedPreferencesType().equals("string")) {
                                                    sharedPreferencesEditor.putString(
                                                            messageReward.getSharedPreferencesKey(),
                                                            messageReward.getSharedPreferencesValue()
                                                    ).apply();
                                                }
                                            } catch (NumberFormatException e) {

                                            }
                                        }

                                        MainActivity.playSound(R.raw.buy, inflater.getContext());
                                        holder.getRewardButton.setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onFailure(Call<Void> call, Throwable t) {
                                        Toast.makeText(inflater.getContext(), "?????????????????? ????????????. ?????????????????? ?????????????????????? ?? ??????????????????", Toast.LENGTH_SHORT).show();
                                        holder.getRewardButton.setEnabled(true);
                                    }
                                }
                        );

                    });
                }

            } else {
                holder.rewardGroup.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, bodyTextView, dateTimeTextView, scoreRewardCountTextView, hintsRewardCountTextView, rewardTitleTextView;
        LinearLayout rewardGroup, scoreGroup, hintsGroup;
        Button getRewardButton;

        ViewHolder(View view) {
            super(view);
            titleTextView = view.findViewById(R.id.messageTitle);
            bodyTextView = view.findViewById(R.id.messageBody);
            dateTimeTextView = view.findViewById(R.id.messageDateTime);
            rewardGroup = view.findViewById(R.id.rewardGroup);
            scoreGroup = view.findViewById(R.id.scoreGroup);
            hintsGroup = view.findViewById(R.id.hintsGroup);
            scoreRewardCountTextView = view.findViewById(R.id.rewardScoreCount);
            hintsRewardCountTextView = view.findViewById(R.id.rewardHintsCount);
            rewardTitleTextView = view.findViewById(R.id.rewardTitle);
            getRewardButton = view.findViewById(R.id.getRewardButton);
        }
    }

}


