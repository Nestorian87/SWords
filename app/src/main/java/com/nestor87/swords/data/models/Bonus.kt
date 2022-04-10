package com.nestor87.swords.data.models

import android.content.SharedPreferences
import com.nestor87.swords.ui.main.MainActivity
import android.content.Context
import androidx.annotation.DrawableRes
import com.nestor87.swords.App
import com.nestor87.swords.R

class Bonus(
    val id: String,
    @DrawableRes
    val icon: Int,
    val name: String,
    val description: String,
    val durationInMinutes: Int,
    val price: Int,
    val priceCurrency: Currency
) {
    private var sharedPreferences: SharedPreferences =
        App.getInstance().getSharedPreferences(MainActivity.APP_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)

    var count: Int
        get() = sharedPreferences.getInt("Bonus.$id.count", 0)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putInt("Bonus.$id.count", value)
            editor.apply()
        }

    val isActive: Boolean
        get() {
            val expireTime = sharedPreferences.getLong("Bonus.$id.expireTime", 0L)
            return expireTime - System.currentTimeMillis() > 1000
        }

    val expireTime
        get() = sharedPreferences.getLong("Bonus.$id.expireTime", 0L)

    fun activate() {
        val editor = sharedPreferences.edit()
        editor.putLong("Bonus.$id.expireTime", System.currentTimeMillis() +  durationInMinutes * 60000)
        editor.apply()
    }

    companion object {
        val BONUSES = mutableListOf<Bonus>()

        fun initBonuses() {
            BONUSES.add(
                Bonus(
                    "doubleScore",
                    R.drawable.score,
                    "Удвоение очков",
                    "Удваивает все получаемые очки, количество которых не превышает 500",
                    60,
                    300,
                    Currency(R.drawable.hints)
                )
            )
            BONUSES.add(
                Bonus(
                    "doubleHints",
                    R.drawable.hints,
                    "Удвоение подсказок",
                    "Удваивает все получаемые подсказки, количество которых не превышает 100",
                    60,
                    2000,
                    Currency(R.drawable.score)
                )
            )
            BONUSES.add(
                Bonus(
                    "colorLetters",
                    R.drawable.icon,
                    "Буквоискатель",
                    "Увеличивает шанс выпадения цветных букв в 2 раза",
                    60,
                    400,
                    Currency(R.drawable.hints)
                )
            )
        }

        fun isActive(id: String): Boolean {
            return BONUSES.find { it.id == id }?.isActive ?: false
        }

        fun getActiveBonuses(): List<Bonus> {
            return BONUSES.filter { it.isActive }
        }
    }
}
