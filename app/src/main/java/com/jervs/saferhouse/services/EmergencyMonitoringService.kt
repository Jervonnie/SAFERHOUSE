package com.jervs.saferhouse.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.jervs.saferhouse.MainActivity
import com.jervs.saferhouse.R
import com.jervs.saferhouse.utils.AudioDetector
import com.jervs.saferhouse.utils.BatteryMonitor
import com.jervs.saferhouse.utils.EmergencyManager
import com.jervs.saferhouse.utils.HealthCheckManager
import kotlin.math.sqrt

class EmergencyMonitoringService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var emergencyManager: EmergencyManager
    private lateinit var audioDetector: AudioDetector
    private lateinit var batteryMonitor: BatteryMonitor
    private lateinit var healthCheckManager: HealthCheckManager
    
    // Fall Detection Thresholds
    private val IMPACT_THRESHOLD = 30.0f // High acceleration (impact)
    private val LAYING_THRESHOLD = 2.0f   // Low acceleration (laying still)
    private var isWaitingForInactivity = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        emergencyManager = EmergencyManager(this)
        batteryMonitor = BatteryMonitor(this)
        healthCheckManager = HealthCheckManager(this)
        
        audioDetector = AudioDetector(this) {
            Log.d("EmergencyService", "Keyword detected! Triggering emergency...")
            triggerAlertSequence()
        }
        
        startForegroundService()
        registerSensors()
        audioDetector.startListening()
        batteryMonitor.startMonitoring()
        
        // Schedule a default health check-in (e.g., 9:00 AM)
        healthCheckManager.scheduleDailyCheckIn(9, 0)
    }

    private fun startForegroundService() {
        val channelId = "SaferHouse_Service_Channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "SaferHouse Monitoring",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("SaferHouse is Active")
            .setContentText("Monitoring for falls and emergencies...")
            .setSmallIcon(android.R.drawable.ic_menu_compass) // Placeholder icon
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
    }

    private fun registerSensors() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val acceleration = sqrt(x * x + y * y + z * z)
            
            if (!isWaitingForInactivity && acceleration > IMPACT_THRESHOLD) {
                Log.d("FallDetection", "Impact detected: $acceleration")
                isWaitingForInactivity = true
                checkInactivity()
            }
        }
    }

    private fun checkInactivity() {
        // Wait for 5 seconds to see if the user is still/inactive
        handler.postDelayed({
            // In a real scenario, we'd check if the user is moving again
            // For now, if no movement cancels it, we trigger alert
            triggerAlertSequence()
            isWaitingForInactivity = false
        }, 5000) 
    }

    private fun triggerAlertSequence() {
        Log.d("FallDetection", "Triggering Alert Sequence")
        // This will be linked to the Confirmation UI (Phase 6)
        // For now, it calls the EmergencyManager directly
        emergencyManager.triggerEmergency()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        audioDetector.stopListening()
        batteryMonitor.stopMonitoring()
    }
}
