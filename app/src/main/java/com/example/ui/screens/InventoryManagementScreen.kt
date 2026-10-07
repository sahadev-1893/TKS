package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.InventoryConsumptionLog
import com.example.data.entity.InventoryItem
import com.example.ui.theme.*
import com.example.util.FormatUtils
import com.example.util.WhatsAppUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryManagementScreen(
    items: List<InventoryItem>,
    lowStockItems: List<InventoryItem>,
    logs: List<InventoryConsumptionLog>,
    onRecordConsumption: (Long, Double, String, () -> Unit) -> Unit,
    onRecordRestock: (Long, Double, String, () -> Unit) -> Unit,
    onSaveItem: (Long, String, String, Double, Double, Double, () -> Unit) -> Unit,
    onDeleteItem: (InventoryItem) -> Unit
) {
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(0) } // 0: Raw Materials, 1: Consumption Log
    var showConsumptionDialog by remember { mutableStateOf(false) }
    var showRestockDialog by remember { mutableStateOf(false) }
    var showAddItemDialog by remember { mutableStateOf(false) }

    var selectedItemForAction by remember { mutableStateOf<InventoryItem?>(null) }
    var quantityInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }

    // Add / Edit Item fields
    var editItem by remember { mutableStateOf<InventoryItem?>(null) }
    var dialogName by remember { mutableStateOf("") }
    var dialogUnit by remember { mutableStateOf("kg") }
    var dialogStock by remember { mutableStateOf("10") }
    var dialogThreshold by remember { mutableStateOf("3") }
    var dialogCost by remember { mutableStateOf("50") }

    fun openConsumptionFor(item: InventoryItem) {
        selectedItemForAction = item
        quantityInput = ""
        notesInput = "Daily tea stall consumption"
        showConsumptionDialog = true
    }

    fun openRestockFor(item: InventoryItem) {
        selectedItemForAction = item
        quantityInput = ""
        notesInput = "Fresh morning delivery"
        showRestockDialog = true
    }

    fun openEditItem(item: InventoryItem) {
        editItem = item
        dialogName = item.name
        dialogUnit = item.unit
        dialogStock = item.currentStock.toString()
        dialogThreshold = item.lowStockThreshold.toString()
        dialogCost = item.costPerUnit.toString()
        showAddItemDialog = true
    }

    fun openAddNewItem() {
        editItem = null
        dialogName = ""
        dialogUnit = "kg"
        dialogStock = "10"
        dialogThreshold = "3"
        dialogCost = "50"
        showAddItemDialog = true
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { openAddNewItem() },
                containerColor = ChaiPrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("NEW MATERIAL", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("btn_new_inventory_item")
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(ChaiBackgroundLight)
        ) {
            // Low Stock Urgent Alert Banner (Alerts for Milk, Tea Leaf, Sugar, etc.)
            if (lowStockItems.isNotEmpty()) {
                Surface(
                    color = BalanceRedLight,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Alert",
                                    tint = BalanceRed,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "LOW STOCK ALERT (${lowStockItems.size} ITEMS)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                    color = BalanceRed
                                )
                            }

                            Button(
                                onClick = {
                                    val sb = StringBuilder()
                                    sb.append("☕ *TUNA KAKA TEA STALL*\n")
                                    sb.append("📦 *RAW MATERIAL URGENT REQUISITION*\n\n")
                                    sb.append("Hello, we urgently need delivery of the following supplies:\n\n")
                                    lowStockItems.forEach { itm ->
                                        val needed = (itm.lowStockThreshold * 2) - itm.currentStock
                                        sb.append("• *${itm.name}*: Need ~${String.format("%.1f", needed)} ${itm.unit} (Current: ${itm.currentStock} ${itm.unit})\n")
                                    }
                                    sb.append("\nPlease deliver as soon as possible today.\nThank you!\n*Tuna Kaka Tea Stall*")
                                    WhatsAppUtils.shareToWhatsApp(context, sb.toString())
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("WHATSAPP SUPPLIER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        lowStockItems.forEach { itm ->
                            Text(
                                text = "⚠️ ${itm.name}: only ${itm.currentStock} ${itm.unit} left (Alert threshold: ${itm.lowStockThreshold} ${itm.unit})",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = BalanceRed
                            )
                        }
                    }
                }
            }

            // Tab bar: Raw Materials vs Consumption Log
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = ChaiSurfaceLight,
                contentColor = ChaiPrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("RAW MATERIALS (${items.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("CONSUMPTION LOG (${logs.size})", fontWeight = FontWeight.Bold) }
                )
            }

            if (selectedTab == 0) {
                // Raw Materials List with Quick Consume & Restock
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        val isLow = item.isLowStock
                        val progress = if (item.lowStockThreshold > 0) {
                            (item.currentStock / (item.lowStockThreshold * 2.5)).toFloat().coerceIn(0f, 1f)
                        } else 1f

                        val icon = when {
                            item.name.contains("Milk", ignoreCase = true) -> Icons.Default.LocalDrink
                            item.name.contains("Tea", ignoreCase = true) -> Icons.Default.Eco
                            item.name.contains("Sugar", ignoreCase = true) -> Icons.Default.Grain
                            item.name.contains("Cup", ignoreCase = true) -> Icons.Default.Coffee
                            else -> Icons.Default.Inventory
                        }

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isLow) BalanceRedLight else ChaiCardLight
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth().testTag("inventory_item_${item.id}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(if (isLow) BalanceRed.copy(alpha = 0.15f) else ChaiCardWarm),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = item.name,
                                                tint = if (isLow) BalanceRed else ChaiPrimary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(
                                                text = item.name,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = ChaiTextPrimary
                                            )
                                            Text(
                                                text = "Alert at: ${item.lowStockThreshold} ${item.unit}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = ChaiTextSecondary
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${item.currentStock} ${item.unit}",
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                            color = if (isLow) BalanceRed else ChaiTextPrimary
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (isLow) BalanceRed else PaidGreen
                                        ) {
                                            Text(
                                                text = if (isLow) "LOW STOCK" else "IN STOCK",
                                                color = Color.White,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Stock Level Progress Bar
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (isLow) BalanceRed else PaidGreen,
                                    trackColor = ChaiBorder
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = ChaiBorder.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(10.dp))

                                // Quick Actions: RECORD CONSUMPTION and RESTOCK
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { openConsumptionFor(item) },
                                        modifier = Modifier.weight(1.3f).height(40.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ChaiSecondary),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("RECORD USED", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { openRestockFor(item) },
                                        modifier = Modifier.weight(1.3f).height(40.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PaidGreen),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("RESTOCK (+)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    IconButton(
                                        onClick = { openEditItem(item) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = ChaiPrimary, modifier = Modifier.size(18.dp))
                                    }

                                    IconButton(
                                        onClick = { onDeleteItem(item) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = BalanceRed, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Consumption & Restock Log History
                if (logs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No inventory activity logged yet", color = ChaiTextSecondary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(logs, key = { it.id }) { log ->
                            val isConsumption = log.type == "CONSUMPTION"
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = ChaiCardLight),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(if (isConsumption) ChaiSecondary.copy(alpha = 0.15f) else PaidGreenLight),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isConsumption) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                                                contentDescription = log.type,
                                                tint = if (isConsumption) ChaiSecondary else PaidGreen,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(
                                                text = log.itemName,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = ChaiTextPrimary
                                            )
                                            Text(
                                                text = "${FormatUtils.formatDateTime(log.date)} • ${if (isConsumption) "Used" else "Restocked"}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = ChaiTextSecondary
                                            )
                                            if (log.notes.isNotBlank()) {
                                                Text(
                                                    text = log.notes,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = ChaiTextSecondary
                                                )
                                            }
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = (if (isConsumption) "- " else "+ ") + "${log.quantity} ${log.unit}",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                            color = if (isConsumption) ChaiSecondary else PaidGreen
                                        )
                                        Text(
                                            text = "Bal: ${log.remainingStockAfter} ${log.unit}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ChaiTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Record Consumption Dialog
    if (showConsumptionDialog && selectedItemForAction != null) {
        val item = selectedItemForAction!!
        AlertDialog(
            onDismissRequest = { showConsumptionDialog = false },
            title = {
                Text(
                    text = "Record Daily Used: ${item.name}",
                    fontWeight = FontWeight.Bold,
                    color = ChaiSecondary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Current Stock: ${item.currentStock} ${item.unit}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    OutlinedTextField(
                        value = quantityInput,
                        onValueChange = { quantityInput = it },
                        label = { Text("Quantity Used (${item.unit}) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("Notes (e.g. Morning batch)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = quantityInput.toDoubleOrNull() ?: 0.0
                        if (qty > 0) {
                            onRecordConsumption(item.id, qty, notesInput.trim()) {
                                showConsumptionDialog = false
                                Toast.makeText(context, "Recorded ${qty} ${item.unit} consumption", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ChaiSecondary)
                ) {
                    Text("RECORD CONSUMPTION", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConsumptionDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }

    // Record Restock Dialog
    if (showRestockDialog && selectedItemForAction != null) {
        val item = selectedItemForAction!!
        AlertDialog(
            onDismissRequest = { showRestockDialog = false },
            title = {
                Text(
                    text = "Restock Fresh Supply: ${item.name}",
                    fontWeight = FontWeight.Bold,
                    color = PaidGreen
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Current Stock: ${item.currentStock} ${item.unit}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    OutlinedTextField(
                        value = quantityInput,
                        onValueChange = { quantityInput = it },
                        label = { Text("Quantity Added (${item.unit}) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("Supplier / Invoice Notes") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = quantityInput.toDoubleOrNull() ?: 0.0
                        if (qty > 0) {
                            onRecordRestock(item.id, qty, notesInput.trim()) {
                                showRestockDialog = false
                                Toast.makeText(context, "Restocked ${qty} ${item.unit} successfully!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PaidGreen)
                ) {
                    Text("ADD RESTOCK", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestockDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }

    // Add / Edit Inventory Item Dialog
    if (showAddItemDialog) {
        AlertDialog(
            onDismissRequest = { showAddItemDialog = false },
            title = {
                Text(
                    text = if (editItem == null) "New Raw Material" else "Edit Raw Material",
                    fontWeight = FontWeight.Bold,
                    color = ChaiPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dialogName,
                        onValueChange = { dialogName = it },
                        label = { Text("Material Name *") },
                        placeholder = { Text("e.g. Buffalo Milk, Cardamom") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = dialogUnit,
                        onValueChange = { dialogUnit = it },
                        label = { Text("Unit (L, kg, g, pcs) *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = dialogStock,
                        onValueChange = { dialogStock = it },
                        label = { Text("Current Stock *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = dialogThreshold,
                        onValueChange = { dialogThreshold = it },
                        label = { Text("Low Stock Alert Threshold *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val stock = dialogStock.toDoubleOrNull() ?: 10.0
                        val threshold = dialogThreshold.toDoubleOrNull() ?: 3.0
                        val cost = dialogCost.toDoubleOrNull() ?: 50.0
                        if (dialogName.isNotBlank()) {
                            val id = editItem?.id ?: 0L
                            onSaveItem(id, dialogName, dialogUnit, stock, threshold, cost) {
                                showAddItemDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ChaiPrimary)
                ) {
                    Text("SAVE ITEM", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddItemDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
}
