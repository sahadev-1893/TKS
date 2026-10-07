package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.supabase.SupabaseConfig
import com.example.data.supabase.SupabaseSyncResult
import com.example.data.supabase.SupabaseSyncService
import com.example.ui.theme.*
import com.example.util.FormatUtils
import kotlinx.coroutines.launch

@Composable
fun SupabaseSyncScreen(
    config: SupabaseConfig,
    syncService: SupabaseSyncService
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var projectUrl by remember { mutableStateOf(config.supabaseUrl) }
    var apiKey by remember { mutableStateOf(config.supabaseKey) }
    var autoSyncEnabled by remember { mutableStateOf(config.autoSyncEnabled) }

    var isTestingConnection by remember { mutableStateOf(false) }
    var isSyncingPush by remember { mutableStateOf(false) }
    var isSyncingPull by remember { mutableStateOf(false) }

    var connectionStatusMessage by remember { mutableStateOf<String?>(null) }
    var isConnectedSuccess by remember { mutableStateOf(config.isConnected) }
    var lastSyncTime by remember { mutableStateOf(config.lastSyncTimestamp) }

    var syncSuccessCounts by remember { mutableStateOf<Map<String, Int>?>(null) }
    var syncActionLabel by remember { mutableStateOf("Synced") }
    var syncErrorMessage by remember { mutableStateOf<String?>(null) }

    val sqlScript = remember { SupabaseSyncService.generatePostgresSqlSchema() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChaiBackgroundLight)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isConnectedSuccess) PaidGreenLight else ChaiCardWarm
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isConnectedSuccess) PaidGreen else ChaiSecondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isConnectedSuccess) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                                contentDescription = "Cloud Status",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = if (isConnectedSuccess) "SUPABASE CONNECTED" else "SUPABASE CLOUD DISCONNECTED",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = if (isConnectedSuccess) PaidGreen else ChaiPrimary
                            )
                            Text(
                                text = if (lastSyncTime > 0) "Last synced: ${FormatUtils.formatDateTime(lastSyncTime)}" else "No cloud sync performed yet",
                                style = MaterialTheme.typography.bodySmall,
                                color = ChaiTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = ChaiBorder.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))

                // Auto Sync Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Sync on New Transactions",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = ChaiTextPrimary
                        )
                        Text(
                            text = "Automatically backup orders, payments & ledger to Supabase in the background",
                            style = MaterialTheme.typography.bodySmall,
                            color = ChaiTextSecondary
                        )
                    }
                    Switch(
                        checked = autoSyncEnabled,
                        onCheckedChange = {
                            autoSyncEnabled = it
                            config.autoSyncEnabled = it
                            Toast.makeText(
                                context,
                                if (it) "Auto-sync enabled!" else "Auto-sync disabled",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PaidGreen
                        )
                    )
                }
            }
        }

        // Credentials Configuration Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ChaiCardLight),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SUPABASE PROJECT CREDENTIALS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = ChaiTextSecondary
                    )

                    if (config.hasEnvConfig) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PaidGreenLight
                        ) {
                            Text(
                                text = "Configured in .env",
                                color = PaidGreen,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = projectUrl,
                    onValueChange = { projectUrl = it },
                    label = { Text("Supabase Project URL") },
                    placeholder = { Text("https://your-project.supabase.co") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("supabase_url_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("Supabase Anon / Public API Key") },
                    placeholder = { Text("eyJhbGciOiJIUzI1NiIsInR5cCI6...") },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("supabase_key_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            config.supabaseUrl = projectUrl
                            config.supabaseKey = apiKey
                            isTestingConnection = true
                            connectionStatusMessage = null
                            coroutineScope.launch {
                                val (ok, msg) = syncService.testConnection()
                                isTestingConnection = false
                                isConnectedSuccess = ok
                                connectionStatusMessage = msg
                            }
                        },
                        enabled = !isTestingConnection && projectUrl.isNotBlank() && apiKey.isNotBlank(),
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isTestingConnection) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text("TEST CONNECTION", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            config.supabaseUrl = projectUrl
                            config.supabaseKey = apiKey
                            Toast.makeText(context, "Supabase credentials saved successfully!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ChaiPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("SAVE CONFIG", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                connectionStatusMessage?.let { msg ->
                    Surface(
                        color = if (isConnectedSuccess) PaidGreenLight else BalanceRedLight,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = msg,
                            color = if (isConnectedSuccess) PaidGreen else BalanceRed,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }

        // Live Cloud Synchronization Card (Push & Pull)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ChaiCardLight),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "CLOUD DATA SYNCHRONIZATION",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = ChaiTextSecondary
                )

                Text(
                    text = "Seamless two-way cloud sync between your local SQLite database and your Supabase PostgreSQL cloud backend.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ChaiTextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // PUSH BUTTON
                    Button(
                        onClick = {
                            config.supabaseUrl = projectUrl
                            config.supabaseKey = apiKey
                            isSyncingPush = true
                            syncErrorMessage = null
                            syncSuccessCounts = null
                            coroutineScope.launch {
                                when (val result = syncService.syncAllData()) {
                                    is SupabaseSyncResult.Success -> {
                                        isSyncingPush = false
                                        isConnectedSuccess = true
                                        lastSyncTime = config.lastSyncTimestamp
                                        syncSuccessCounts = result.syncedCounts
                                        syncActionLabel = "Backed up to Cloud"
                                        Toast.makeText(context, "Uploaded to Supabase successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                    is SupabaseSyncResult.Error -> {
                                        isSyncingPush = false
                                        syncErrorMessage = result.error
                                    }
                                }
                            }
                        },
                        enabled = !isSyncingPush && !isSyncingPull && projectUrl.isNotBlank() && apiKey.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_sync_now"),
                        colors = ButtonDefaults.buttonColors(containerColor = PaidGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isSyncingPush) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PUSHING...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PUSH TO CLOUD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    // PULL BUTTON
                    OutlinedButton(
                        onClick = {
                            config.supabaseUrl = projectUrl
                            config.supabaseKey = apiKey
                            isSyncingPull = true
                            syncErrorMessage = null
                            syncSuccessCounts = null
                            coroutineScope.launch {
                                when (val result = syncService.pullAllDataFromCloud()) {
                                    is SupabaseSyncResult.Success -> {
                                        isSyncingPull = false
                                        isConnectedSuccess = true
                                        lastSyncTime = config.lastSyncTimestamp
                                        syncSuccessCounts = result.syncedCounts
                                        syncActionLabel = "Restored from Cloud"
                                        Toast.makeText(context, "Downloaded from Supabase successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                    is SupabaseSyncResult.Error -> {
                                        isSyncingPull = false
                                        syncErrorMessage = result.error
                                    }
                                }
                            }
                        },
                        enabled = !isSyncingPush && !isSyncingPull && projectUrl.isNotBlank() && apiKey.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_pull_cloud"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ChaiPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isSyncingPull) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PULLING...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PULL FROM CLOUD", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                syncSuccessCounts?.let { counts ->
                    Surface(
                        color = PaidGreenLight,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "✅ Data Successfully $syncActionLabel:",
                                fontWeight = FontWeight.Bold,
                                color = PaidGreen,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            counts.forEach { (table, count) ->
                                Text(
                                    text = "• $table: $count records",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ChaiTextPrimary
                                )
                            }
                        }
                    }
                }

                syncErrorMessage?.let { err ->
                    Surface(
                        color = BalanceRedLight,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = err,
                            color = BalanceRed,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        // Supabase SQL Editor Script Setup Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ChaiCardLight),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SUPABASE SQL SCHEMA SCRIPT",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = ChaiTextSecondary
                    )

                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Supabase Schema", sqlScript)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "SQL script copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ChaiPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("COPY SQL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Text(
                    text = "If you haven't created the tables in Supabase yet, copy this script and paste it into your Supabase project's SQL Editor (Dashboard > SQL Editor > New query).",
                    style = MaterialTheme.typography.bodySmall,
                    color = ChaiTextSecondary
                )

                Surface(
                    color = Color(0xFF1E1E1E),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)
                ) {
                    Text(
                        text = sqlScript,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFFD4D4D4),
                        modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState())
                    )
                }
            }
        }
    }
}
