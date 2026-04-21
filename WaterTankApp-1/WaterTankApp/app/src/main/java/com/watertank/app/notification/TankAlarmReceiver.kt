package com.watertank.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TankAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        val title = intent.getStringExtra(NotificationHelper.EXTRA_TITLE) ?: "Tank alert"
        val msg = intent.getStringExtra(NotificationHelper.EXTRA_MESSAGE) ?: ""
        val vib = intent.getBooleanExtra(NotificationHelper.EXTRA_VIBRATE, true)
        NotificationHelper.show(ctx, id = System.currentTimeMillis().toInt(), title, msg, vib)
    }
}
