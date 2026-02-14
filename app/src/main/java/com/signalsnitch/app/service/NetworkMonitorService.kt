package com.signalsnitch.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.signalsnitch.app.MainActivity
import com.signalsnitch.app.R
import com.signalsnitch.app.data.EventType
import com.signalsnitch.app.data.NetworkEvent
import java.util.Date

class NetworkMonitorService : Service() {

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private var isMonitoring = false
    private var hasCellularNetwork = false

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "network_monitor_channel"
        private const val ALERT_CHANNEL_ID = "network_alert_channel"
        private const val ALERT_NOTIFICATION_ID = 2

        val eventHistory = mutableListOf<NetworkEvent>()

        fun start(context: Context) {
            val intent = Intent(context, NetworkMonitorService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, NetworkMonitorService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createForegroundNotification())
        startMonitoring()
        addEvent(EventType.SERVICE_STARTED, "Network monitoring started")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopMonitoring()
        addEvent(EventType.SERVICE_STOPPED, "Network monitoring stopped")
        super.onDestroy()
    }

    private fun createNotificationChannels() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Foreground service channel (Low importance)
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Network Monitoring",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows that SignalSnitch is actively monitoring"
        }

        // Alert channel (High importance)
        val alertChannel = NotificationChannel(
            ALERT_CHANNEL_ID,
            "Network Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts when cellular connection is lost"
        }

        notificationManager.createNotificationChannel(serviceChannel)
        notificationManager.createNotificationChannel(alertChannel)
    }

    private fun createForegroundNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SignalSnitch is watching...")
            .setContentText("Monitoring cellular network")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun startMonitoring() {
        if (isMonitoring) return

        // Check current cellular status
        checkInitialCellularStatus()

        // Set up network callback
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true) {
                    if (!hasCellularNetwork) {
                        hasCellularNetwork = true
                        addEvent(EventType.RESTORED, "Cellular connection restored")
                    }
                }
            }

            override fun onLost(network: Network) {
                // Check if we still have any cellular network
                val hasAnyCellular = connectivityManager.allNetworks.any { net ->
                    connectivityManager.getNetworkCapabilities(net)
                        ?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
                }

                if (!hasAnyCellular && hasCellularNetwork) {
                    hasCellularNetwork = false
                    addEvent(EventType.LOST, "Cellular connection lost")
                    showLostConnectionAlert()
                }
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    if (!hasCellularNetwork) {
                        hasCellularNetwork = true
                        addEvent(EventType.RESTORED, "Cellular connection restored")
                    }
                }
            }
        }

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        connectivityManager.registerNetworkCallback(request, networkCallback)
        isMonitoring = true
    }

    private fun checkInitialCellularStatus() {
        hasCellularNetwork = connectivityManager.allNetworks.any { network ->
            connectivityManager.getNetworkCapabilities(network)
                ?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
        }
    }

    private fun stopMonitoring() {
        if (isMonitoring) {
            connectivityManager.unregisterNetworkCallback(networkCallback)
            isMonitoring = false
        }
    }

    private fun showLostConnectionAlert() {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setContentTitle("📵 Cellular Connection Lost!")
            .setContentText("You've gone dark - no mobile network")
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(ALERT_NOTIFICATION_ID, notification)
    }

    private fun addEvent(type: EventType, message: String) {
        synchronized(eventHistory) {
            eventHistory.add(0, NetworkEvent(Date(), type, message))
            if (eventHistory.size > 100) {
                eventHistory.removeAt(eventHistory.size - 1)
            }
        }
    }
}
