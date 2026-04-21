package com.jervs.saferhouse.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.jervs.saferhouse.db.AppDatabase
import com.jervs.saferhouse.db.SafetyRepository
import com.jervs.saferhouse.model.SafetyLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HealthCheckManager(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleDailyCheckIn(hour: Int, minute: Int) {
        val intent = Intent(context, CheckInReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 1001, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            if (before(java.util.Calendar.getInstance())) {
                add(java.util.Calendar.DATE, 1)
            }
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
        Log.d("HealthCheck", "Daily check-in scheduled at $hour:$minute")
    }

    class CheckInReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("HealthCheck", "Time for daily check-in!")
            // This would normally trigger a UI prompt. 
            // Since we are doing logic, we'll start a timer. 
            // If no "I'm Okay" signal is received in 30 mins, trigger emergency.
            
            val emergencyManager = EmergencyManager(context)
            // Logic: In a real app, you'd show a notification first.
            // For now, let's simulate a 'pending' state.
            
            val repository by lazy {
                val db = AppDatabase.getDatabase(context)
                SafetyRepository(db.contactDao(), db.logDao())
            }

            CoroutineScope(Dispatchers.IO).launch {
                repository.insertLog(SafetyLog(
                    eventType = "CHECK_IN_PROMPT",
                    status = "PENDING",
                    description = "Daily safety check-in triggered."
                ))
            }
        }
    }
}
