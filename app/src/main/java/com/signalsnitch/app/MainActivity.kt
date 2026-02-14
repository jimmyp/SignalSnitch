package com.signalsnitch.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.signalsnitch.app.data.EventType
import com.signalsnitch.app.data.NetworkEvent
import com.signalsnitch.app.service.NetworkMonitorService
import com.signalsnitch.app.ui.theme.SignalSnitchTheme
import com.signalsnitch.app.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // Handle permission denial if needed
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            SignalSnitchTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val isMonitoring by viewModel.isMonitoring.collectAsState()
    val events by viewModel.events.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = "SignalSnitch",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
        )

        // Status Indicator
        StatusIndicator(isMonitoring, events)

        Spacer(modifier = Modifier.height(32.dp))

        // Start/Stop Button
        MonitorButton(
            isMonitoring = isMonitoring,
            onToggle = { shouldStart ->
                if (shouldStart) {
                    NetworkMonitorService.start(androidx.compose.ui.platform.LocalContext.current)
                    viewModel.setMonitoring(true)
                } else {
                    NetworkMonitorService.stop(androidx.compose.ui.platform.LocalContext.current)
                    viewModel.setMonitoring(false)
                }
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Event History
        EventHistory(events)
    }
}

@Composable
fun StatusIndicator(isMonitoring: Boolean, events: List<NetworkEvent>) {
    val hasCellular = events.firstOrNull()?.type != EventType.LOST

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !isMonitoring -> MaterialTheme.colorScheme.surfaceVariant
                hasCellular -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = when {
                    !isMonitoring -> "⏸️"
                    hasCellular -> "📶"
                    else -> "📵"
                },
                fontSize = 64.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when {
                    !isMonitoring -> "Not Monitoring"
                    hasCellular -> "Connected"
                    else -> "No Signal!"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MonitorButton(isMonitoring: Boolean, onToggle: (Boolean) -> Unit) {
    Button(
        onClick = { onToggle(!isMonitoring) },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isMonitoring)
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = if (isMonitoring) "Stop Monitoring" else "Start Monitoring",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EventHistory(events: List<NetworkEvent>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Event History",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (events.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No events yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    items(events) { event ->
                        EventItem(event)
                    }
                }
            }
        }
    }
}

@Composable
fun EventItem(event: NetworkEvent) {
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = when (event.type) {
                    EventType.LOST -> "❌"
                    EventType.RESTORED -> "✅"
                    EventType.SERVICE_STARTED -> "▶️"
                    EventType.SERVICE_STOPPED -> "⏹️"
                },
                fontSize = 16.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = event.message,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = timeFormat.format(event.timestamp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Divider()
}
