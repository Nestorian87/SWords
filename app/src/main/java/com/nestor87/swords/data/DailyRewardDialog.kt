package com.nestor87.swords.data

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import com.nestor87.swords.R
import com.nestor87.swords.data.models.DailyRewardVariant
import com.nestor87.swords.ui.main.MainActivity
import rubikstudio.library.LuckyWheelView
import rubikstudio.library.model.LuckyItem
import java.text.SimpleDateFormat
import java.util.*

class DailyRewardDialog(private val context: Context) {
    private var reward: DailyRewardVariant? = null
    private var openDate: Date = Date()
    private val SHARED_PREFS_LONG_DATE_LAST_REWARD = "lastDailyRewardReceivedDate"
    private val SHARED_PREFS_INT_REWARD_DAY_LAST = "lastRewardReceivedDay"

    var onRewardGottenListener: ((reward: DailyRewardVariant)->Boolean)? = null

    private fun getDateLastRewardReceived(): Date  {
        val preferences = context.getSharedPreferences(MainActivity.APP_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
        return Date(preferences.getLong(SHARED_PREFS_LONG_DATE_LAST_REWARD, 0))
    }

    private fun getDayOfLastReward(): Int  {
        val preferences = context.getSharedPreferences(MainActivity.APP_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
        return preferences.getInt(SHARED_PREFS_INT_REWARD_DAY_LAST, 0)
    }

    private fun setDateLastRewardReceivedAndDay(date: Date, day: Int) {
        val preferencesEditor = context.getSharedPreferences(MainActivity.APP_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE).edit()
        preferencesEditor.putLong(SHARED_PREFS_LONG_DATE_LAST_REWARD, date.time)
        preferencesEditor.putInt(SHARED_PREFS_INT_REWARD_DAY_LAST, day)
        preferencesEditor.apply()
    }

    fun show() {
        val dialogView: View = LayoutInflater.from(context).inflate(R.layout.dialog_daily_reward, null, false)

        val dayOfLastReward = getDayOfLastReward()
        val dateOfLastRewardReceived = getDateLastRewardReceived()
        var dayOfCurrentReward = DailyRewardVariant.Days.DAY1
        var needToShow = true

        if (dayOfLastReward == 0) {
            dayOfCurrentReward = DailyRewardVariant.Days.DAY1
        } else {
            val yesterday = Date(openDate.time - 86400000)
            if (SimpleDateFormat("d").format(dateOfLastRewardReceived) == SimpleDateFormat("d").format(yesterday)) {
                dayOfCurrentReward = DailyRewardVariant.Days.values().getOrElse(dayOfLastReward) { DailyRewardVariant.Days.DAY1 }
            } else {
                if (SimpleDateFormat("d").format(dateOfLastRewardReceived) == SimpleDateFormat("d").format(openDate)) {
                    needToShow = false
                } else {
                    dayOfCurrentReward = DailyRewardVariant.Days.DAY1
                }
            }
        }

        dialogView.findViewById<TextView>(R.id.dialogTitle).text = context.getString(
            R.string.daily_reward_dialog_title,
            dayOfCurrentReward.ordinal + 1,
            DailyRewardVariant.Days.values().size
        )

        val rewardVariants = DailyRewardVariant.dailyRewardVariants[dayOfCurrentReward]!!
        rewardVariants.shuffle()
        val luckyWheelView: LuckyWheelView = dialogView.findViewById(R.id.luckyWheel)
        val data = ArrayList<LuckyItem>()
        for (reward in rewardVariants) {
            val luckyItem = LuckyItem()
            luckyItem.topText = ""
            luckyItem.secondaryText = if (reward!!.currency != null) Integer.toString(
                reward!!.count
            ) else ""
            luckyItem.secondaryText =
                if (luckyItem.secondaryText.length == 1) "  " + luckyItem.secondaryText else if (luckyItem.secondaryText.length == 2) " " + luckyItem.secondaryText else luckyItem.secondaryText
            luckyItem.icon = if (reward!!.currency != null) reward!!.currency.icon else 0
            luckyItem.color = MainActivity.getColorFromTheme(R.attr.buttonBackground, context)
            data.add(luckyItem)
        }
        luckyWheelView.setData(data)
        dialogView.findViewById<View>(R.id.spinButton).setOnClickListener { button: View? ->
            button?.isEnabled = false
            val rewards =
                ArrayList<DailyRewardVariant?>()
            for (reward in rewardVariants) {
                for (i in 0 until reward!!.chance) {
                    rewards.add(reward)
                }
            }
            reward = MainActivity.getRandomElementFromArrayList(rewards) as DailyRewardVariant
            luckyWheelView.startLuckyWheelWithTargetIndex(
                rewardVariants.indexOf(reward!!)
            )
        }

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        luckyWheelView.setOnRotateWheelListener {
            if (onRewardGottenListener?.invoke(reward!!) == true) {
                makeToastWithIcon("+ ${reward!!.count}", reward!!.currency.icon)
                setDateLastRewardReceivedAndDay(openDate, dayOfCurrentReward.ordinal + 1)
            }
            Handler(Looper.getMainLooper()).post {
                Thread.sleep(1000)
                dialog.cancel()
            }
        }

        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        if (needToShow) {
            dialog.show()
        }

    }

    private fun makeToastWithIcon(text: String, @DrawableRes icon: Int) {
        val toast = Toast(context)
        toast.setGravity(Gravity.TOP, 0, 0)
        toast.duration = Toast.LENGTH_LONG
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.HORIZONTAL
        val textView = TextView(context)
        textView.text = "$text "
        textView.setTextColor(MainActivity.getColorFromTheme(R.attr.wordText, context))
        layout.addView(textView)
        val imageView = ImageView(context)
        imageView.adjustViewBounds = true
        imageView.maxWidth = 43
        imageView.maxHeight = 43
        imageView.setImageResource(icon)
        layout.addView(imageView)
        toast.setView(layout)
        toast.show()
    }
}