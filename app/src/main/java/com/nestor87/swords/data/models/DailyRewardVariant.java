package com.nestor87.swords.data.models;

import com.nestor87.swords.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class DailyRewardVariant {
    private Currency currency;
    private int rewardCount;
    private int rewardChance;
    
    public DailyRewardVariant(Currencies currency, int rewardCount, int rewardChance) {
        this.rewardCount = rewardCount;
        this.rewardChance = rewardChance;

        if (currency == Currencies.SCORE) {
            this.currency = new Currency(R.drawable.score);
        } else if (currency == Currencies.HINTS) {
            this.currency = new Currency(R.drawable.hints);
        } else if (currency == Currencies.NOTHING) {
            this.currency = null;
        }
    }

    public enum Days {
            DAY1,
            DAY2,
            DAY3,
            DAY4,
            DAY5
    }
    public static HashMap<Days, ArrayList<DailyRewardVariant>> dailyRewardVariants = new HashMap<Days, ArrayList<DailyRewardVariant>>() {{
        put(
            Days.DAY1,
            new ArrayList<DailyRewardVariant>() {{
                add(new DailyRewardVariant(Currencies.SCORE, 10, 6));
                add(new DailyRewardVariant(Currencies.SCORE, 15, 3));
                add(new DailyRewardVariant(Currencies.HINTS, 2, 5));
                add(new DailyRewardVariant(Currencies.HINTS, 5, 2));
            }}
        );
        put(
            Days.DAY2,
            new ArrayList<DailyRewardVariant>() {{
                add(new DailyRewardVariant(Currencies.SCORE, 10, 6));
                add(new DailyRewardVariant(Currencies.SCORE, 15, 4));
                add(new DailyRewardVariant(Currencies.SCORE, 20, 3));
                add(new DailyRewardVariant(Currencies.HINTS, 5, 5));
                add(new DailyRewardVariant(Currencies.HINTS, 8, 3));
            }}
        );
        put(
                Days.DAY3,
                new ArrayList<DailyRewardVariant>() {{
                    add(new DailyRewardVariant(Currencies.SCORE, 10, 6));
                    add(new DailyRewardVariant(Currencies.SCORE, 20, 4));
                    add(new DailyRewardVariant(Currencies.SCORE, 30, 3));
                    add(new DailyRewardVariant(Currencies.HINTS, 5, 5));
                    add(new DailyRewardVariant(Currencies.HINTS, 10, 3));
                    add(new DailyRewardVariant(Currencies.HINTS, 15, 2));
                }}
        );

    }};

    public Currency getCurrency() {
        return currency;
    }

    public int getCount() {
        return rewardCount;
    }

    public int getChance() {
        return rewardChance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DailyRewardVariant that = (DailyRewardVariant) o;
        return rewardCount == that.rewardCount &&
                rewardChance == that.rewardChance &&
                Objects.equals(currency, that.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currency, rewardCount, rewardChance);
    }
}
