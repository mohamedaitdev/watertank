package com.watertank.app.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.watertank.app.MainActivity
import com.watertank.app.R

object NotificationHelper {

    const val CHANNEL_ALERTS = "tank_alerts"
    const val CHANNEL_REMINDERS = "tank_reminders"

    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_MESSAGE = "extra_message"
    const val EXTRA_VIBRATE = "extra_vibrate"

    fun createChannels(ctx: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = ctx.getSystemService(NotificationManager::class.java) ?: return

        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ALERTS, "Tank alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Tank nearing full / critical events"
                enableVibration(true)
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_REMINDERS, "Reminders", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Scheduled check-in reminders"
            }
        )
    }

    /**
     * Schedule a one-shot alarm. Uses setExactAndAllowWhileIdle when the runtime permission
     * is available; otherwise falls back to setAndAllowWhileIdle.
     */
    fun scheduleAt(
        ctx: Context,
        triggerAtMillis: Long,
        title: String,
        message: String,
        vibrate: Boolean = true,
        requestCode: Int = (triggerAtMillis / 1000).toInt()
    ) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(ctx, TankAlarmReceiver::class.java).apply {
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_MESSAGE, message)
            putExtra(EXTRA_VIBRATE, vibrate)
        }
        val pi = PendingIntent.getBroadcast(
            ctx, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) am.canScheduleExactAlarms() else true
        if (canExact) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        }
    }

    fun show(ctx: Context, id: Int, title: String, message: String, vibrate: Boolean) {
        val nm = ContextCompat.getSystemService(ctx, NotificationManager::class.java) ?: return
        val tapIntent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPi = PendingIntent.getActivity(
            ctx, id, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(ctx, CHANNEL_ALERTS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPi)
        if (vibrate) builder.setVibrate(longArrayOf(0, 300, 150, 300))
        nm.notify(id, builder.build())
    }
}
