package com.nestor87.swords.data.markdownviewCssStyle;

import android.content.Context;

import com.nestor87.swords.R;
import com.nestor87.swords.ui.main.MainActivity;
import com.nestor87.swords.utils.SystemUtils;

import br.tiagohm.markdownview.css.styles.Github;

public class VersionCssStyle extends Github {
    public VersionCssStyle(Context context) {
                addRule("body",
                "font-size: 1.1em",
                "background-color: " + getColorFromThemeAsHEX(context, android.R.attr.windowBackground),
                "color: " + getColorFromThemeAsHEX(context, R.attr.scoreAndHintsText)
        );
    }

    private String getColorFromThemeAsHEX(Context context, int attr) {
        int colorInt = SystemUtils.getColorFromTheme(attr, context);
        return String.format("#%06X", (0xFFFFFF & colorInt));
    }
}
