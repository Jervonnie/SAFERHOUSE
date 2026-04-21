package com.jervs.saferhouse.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import com.jervs.saferhouse.db.AppDatabase
import com.jervs.saferhouse.db.SafetyRepository
import com.jervs.saferhouse.model.SafetyLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BatteryMonitor(private val context: Context) {

    private val repository by lazy {
        val db = AppDatabase.getDatabase(context)
        SafetyRepository(db.contactDao(), db.logDao())
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryPct = level * 100 / scale.toFloat()

            if (batteryPct <= 15) {
                handleLowBattery(batteryPct)
            }
        }
    }

    fun startMonitoring() {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(batteryReceiver, filter)
    }

    fun stopMonitoring() {
        context.unregisterReceiver(batteryReceiver)
    }

    private fun handleLowBattery(percentage: Float) {
        Log.w("BatteryMonitor", "Low battery detected: $percentage%")
        CoroutineScope(Dispatchers.IO).launch {
            repository.insertLog(SafetyLog(
                eventType = "LOW_BATTERY",
                status = "ALERT_SENT",
                description = "Battery level is critically low: $percentage%. Notifying caregiver."
            ))
            
            // Optional: Trigger a specific SMS for low battery
            // val emergencyManager = EmergencyManager(context)
            // emergencyManager.sendLowBatteryAlert(percentage)
        }
    }
}
