package com.nestor87.swords.utils

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.TypedValue
import android.view.WindowManager
import androidx.annotation.ColorInt
import com.nestor87.swords.App
import com.nestor87.swords.R
import java.text.NumberFormat
import java.util.*
import kotlin.math.roundToInt

class SystemUtils {
    companion object {

        @JvmStatic
        fun dpToPx(dp: Int): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp.toFloat(),
                App.getInstance().resources.displayMetrics
            ).roundToInt()
        }

        @JvmStatic
        fun getThemeResIdByThemeId(themeId: Int): Int {
            return when (themeId) {
                1 -> R.style.SWords_dark
                2 -> R.style.SWords_white
                3 -> R.style.SWords_darkBlue
                4 -> R.style.SWords_spring
                else -> R.style.SWords_standard
            }
        }

        @JvmStatic
        fun adjustFontScale(context: Context) {
            val configuration = context.resources.configuration
            if (configuration.fontScale > 1f) {
                configuration.fontScale = 1f
                val metrics = context.resources.displayMetrics
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                wm.defaultDisplay.getMetrics(metrics)
                metrics.scaledDensity = configuration.fontScale * metrics.density
                context.resources.updateConfiguration(configuration, metrics)
            }
        }

        @JvmStatic
        fun formatBigNumber(number: Int): String? {
            val nf = NumberFormat.getInstance(Locale("ru", "RU"))
            return nf.format(number.toLong())
        }

        @JvmStatic
        fun isSamsung(): Boolean {
            val manufacturer = Build.MANUFACTURER
            return manufacturer?.equals("samsung", ignoreCase = true) ?: false
        }

        @JvmStatic
        @ColorInt
        fun getColorFromTheme(attr: Int, context: Context): Int {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(attr, typedValue, true)
            return typedValue.data
        }

        @JvmStatic
        @ColorInt
        fun getColorFromTheme(attr: Int, theme: Resources.Theme): Int {
            val typedValue = TypedValue()
            theme.resolveAttribute(attr, typedValue, true)
            return typedValue.data
        }

    }
}