package com.nestor87.swords.data.models;

import com.nestor87.swords.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class DailyRewardVariant {
    private Currency currency;
    private Currencies currencyEnum;
    private int rewardCount;
    private int rewardChance;
    
    public DailyRewardVariant(Currencies currency, int rewardCount, int rewardChance) {
        this.rewardCount = rewardCount;
        this.rewardChance = rewardChance;
        this.currencyEnum = currency;

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
                add(new DailyRewardVariant(Currencies.SCORE, 15, 6));
                add(new DailyRewardVariant(Currencies.SCORE, 20, 4));
                add(new DailyRewardVariant(Currencies.HINTS, 5, 5));
                add(new DailyRewardVariant(Currencies.HINTS, 7, 3));
            }}
        );
        put(
            Days.DAY2,
            new ArrayList<DailyRewardVariant>() {{
                add(new DailyRewardVariant(Currencies.SCORE, 20, 6));
                add(new DailyRewardVariant(Currencies.SCORE, 30, 5));
                add(new DailyRewardVariant(Currencies.SCORE, 35, 3));
                add(new DailyRewardVariant(Currencies.HINTS, 7, 4));
                add(new DailyRewardVariant(Currencies.HINTS, 12, 3));
            }}
        );
        put(
                Days.DAY3,
                new ArrayList<DailyRewardVariant>() {{
                    add(new DailyRewardVariant(Currencies.SCORE, 40, 6));
                    add(new DailyRewardVariant(Currencies.SCORE, 55, 4));
                    add(new DailyRewardVariant(Currencies.SCORE, 70, 3));
                    add(new DailyRewardVariant(Currencies.HINTS, 10, 5));
                    add(new DailyRewardVariant(Currencies.HINTS, 15, 3));
                    add(new DailyRewardVariant(Currencies.HINTS, 20, 2));
                }}
        );
        put(
                Days.DAY4,
                new ArrayList<DailyRewardVariant>() {{
                    add(new DailyRewardVariant(Currencies.SCORE, 60, 6));
                    add(new DailyRewardVariant(Currencies.SCORE, 80, 4));
                    add(new DailyRewardVariant(Currencies.SCORE, 100, 3));
                    add(new DailyRewardVariant(Currencies.HINTS, 20, 5));
                    add(new DailyRewardVariant(Currencies.HINTS, 25, 3));
                    add(new DailyRewardVariant(Currencies.HINTS, 35, 1));
                }}
        );
        put(
                Days.DAY5,
                new ArrayList<DailyRewardVariant>() {{
                    add(new DailyRewardVariant(Currencies.SCORE, 100, 7));
                    add(new DailyRewardVariant(Currencies.SCORE, 150, 6));
                    add(new DailyRewardVariant(Currencies.SCORE, 250, 4));
                    add(new DailyRewardVariant(Currencies.HINTS, 30, 7));
                    add(new DailyRewardVariant(Currencies.HINTS, 40, 5));
                    add(new DailyRewardVariant(Currencies.HINTS, 60, 3));
                    add(new DailyRewardVariant(Currencies.HINTS, 100, 1));
                }}
        );
    }};

    public Currency getCurrency() {
        return currency;
    }

    public Currencies getCurrencyEnum() {
        return currencyEnum;
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
