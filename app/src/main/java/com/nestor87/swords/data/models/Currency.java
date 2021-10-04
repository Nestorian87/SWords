package com.nestor87.swords.data.models;

import java.util.Objects;

public class Currency {
    private boolean custom = false;
    private int icon;
    private String text;

    public Currency(int icon2) {
        this.icon = icon2;
    }

    public Currency(String text2) {
        this.text = text2;
        this.custom = true;
    }

    public int getIcon() {
        return this.icon;
    }

    public String getText() {
        return this.text;
    }

    public boolean isCustom() {
        return this.custom;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Currency currency = (Currency) o;
        if (this.icon != currency.icon || !Objects.equals(this.text, currency.text)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.icon), this.text);
    }
}