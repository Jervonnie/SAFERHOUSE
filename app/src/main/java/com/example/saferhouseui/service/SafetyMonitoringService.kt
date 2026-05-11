package com.example.saferhouseui.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.saferhouseui.R
import com.example.saferhouseui.SaferHouseApplication
import com.example.saferhouseui.data.EmergencyManager
import com.example.saferhouseui.data.model.ActivityLog
import com.example.saferhouseui.data.model.EmergencyContact
import kotlinx.coroutines.*
import kotlin.math.sqrt

class SafetyMonitoringService : Service(), SensorEventListener {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var audioAnalyzer: AudioAnalyzer? = null
    private lateinit var emergencyManager: EmergencyManager

    private var emergencyContacts: List<EmergencyContact> = emptyList()

    // Fall detection thresholds
    private val FALL_THRESHOLD = 25.0f // m/s^2 (adjust based on testing)
    private val INACTIVITY_THRESHOLD = 1.0f // m/s^2
    private var lastImpactTime: Long = 0
    private var isWaitingForInactivity = false

    companion object {
        private const val CHANNEL_ID = "SafetyMonitoringChannel"
        private const val NOTIFICATION_ID = 1
        private const val TAG = "SafetyService"
    }

    override fun onCreate() {
        super.onCreate()
        emergencyManager = EmergencyManager(this)
        createNotificationChannel()
        setupSensors()
    }

    private fun setupSensors() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting foreground service: ${e.message}")
            // Fallback
            try {
                startForeground(NOTIFICATION_ID, notification)
            } catch (e2: Exception) {
                Log.e(TAG, "Critical error starting foreground: ${e2.message}")
            }
        }
        
        val app = application as SaferHouseApplication
        
        // Initialize Audio Analyzer if not already done
        if (audioAnalyzer == null) {
            try {
                audioAnalyzer = AudioAnalyzer(this) { distressType ->
                    triggerEmergency("AUDIO_DISTRESS", "Distress detected: $distressType")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize AudioAnalyzer: ${e.message}")
            }
        }
        
        // Log service start
        serviceScope.launch {
            val user = app.database.userDao().getCurrentUser()
            user?.let {
                app.database.activityLogDao().insertLog(
                    ActivityLog(
                        userId = it.id,
                        type = "SYSTEM",
                        description = "Safety monitoring service started"
                    )
                )
                
                // Load contacts
                app.database.emergencyContactDao().getAllContacts().collect { contacts ->
                    emergencyContacts = contacts.filter { it.elderlyId == user.id }
                }

                // Start background loops
                startSafetyCheckLoop(it.id)
                audioAnalyzer?.startMonitoring()
            }
        }
        
        return START_STICKY
    }

    private fun startSafetyCheckLoop(userId: String) {
        val app = application as SaferHouseApplication
        serviceScope.launch {
            while (isActive) {
                // Check if it's time for a safety check-in
                // For now, just a placeholder for the logic
                delay(60000 * 60) // Check every hour
            }
        }
    }

    private fun startAudioMonitoring(userId: String) {
        // TODO: Initialize TFLite and Vosk for background monitoring
        // This will be implemented in the next step
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val gForce = sqrt(x * x + y * y + z * z)

            if (gForce > FALL_THRESHOLD) {
                Log.d(TAG, "Potential fall impact detected: $gForce")
                lastImpactTime = System.currentTimeMillis()
                isWaitingForInactivity = true
            }

            if (isWaitingForInactivity && (System.currentTimeMillis() - lastImpactTime > 5000)) {
                if (gForce < INACTIVITY_THRESHOLD) {
                    Log.d(TAG, "Fall confirmed: Impact followed by inactivity")
                    triggerEmergency("FALL_DETECTED", "A fall was detected via motion sensors.")
                }
                isWaitingForInactivity = false
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun triggerEmergency(type: String, description: String) {
        val app = application as SaferHouseApplication
        serviceScope.launch {
            val user = app.database.userDao().getCurrentUser()
            user?.let {
                app.database.activityLogDao().insertLog(
                    ActivityLog(
                        userId = it.id,
                        type = type,
                        description = description
                    )
                )
                
                // Immediately notify via Broadcast to UI for the 10s countdown
                val intent = Intent("com.example.saferhouseui.EMERGENCY_TRIGGER").apply {
                    putExtra("type", type)
                    putExtra("description", description)
                }
                sendBroadcast(intent)
                
                // If it's a fall or distress, we might want to start escalation 
                // but the UI usually handles the countdown. 
                // For "headless" mode (app not in foreground), we could 
                // implement a separate background countdown here.
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        audioAnalyzer?.stopMonitoring()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Safety Monitoring Active")
            .setContentText("SaferHouse is monitoring for distress signals.")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Ensure this exists or use a generic icon
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Safety Monitoring Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Channel for SaferHouse background monitoring"
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
