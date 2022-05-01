package com.nestor87.swords.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.nestor87.swords.data.managers.DataManager
import com.nestor87.swords.data.services.BackgroundService
import com.nestor87.swords.utils.SystemUtils

open class BaseActivity : AppCompatActivity() {
    companion object {
        private var startedActivitiesCount = 0
    }

    protected var needToChangeTheme = true

    override fun onCreate(savedInstanceState: Bundle?) {
        if (needToChangeTheme) {
            DataManager.applyTheme(this)
        }
        super.onCreate(savedInstanceState)
        SystemUtils.adjustFontScale(this)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE

    }

    override fun onStart() {
        super.onStart()
        startedActivitiesCount++
        val intent = Intent(this, BackgroundService::class.java)
        intent.putExtra("resume", true)
        startService(intent)
    }

    override fun onStop() {
        startedActivitiesCount--
        if (startedActivitiesCount == 0) {
            val intent = Intent(this, BackgroundService::class.java)
            intent.putExtra("pause", true)
            this.startService(intent)
        }
        super.onStop()
    }
}