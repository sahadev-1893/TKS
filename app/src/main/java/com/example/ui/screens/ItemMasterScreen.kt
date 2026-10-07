package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.MenuItem
import com.example.ui.theme.*
import com.example.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemMasterScreen(
    items: List<MenuItem>,
    onSaveItem: (Long, String, String, Double, Boolean, () -> Unit) -> Unit,
    onToggleActive: (MenuItem) -> Unit,
    onDeleteItem: (MenuItem) -> Unit
) {
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var showDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<MenuItem?>(null) }
    var itemToDelete by remember { mutableStateOf<MenuItem?>(null) }

    var dialogName by remember { mutableStateOf("") }
    var dialogCategory by remember { mutableStateOf("Chai & Beverages") }
    var dialogRate by remember { mutableStateOf("") }
    var dialogIsActive by remember { mutableStateOf(true) }

    val categories = listOf("All", "Chai & Beverages", "Snacks & Bites", "Bakery & Biscuits", "Others")
    val editCategories = listOf("Chai & Beverages", "Snacks & Bites", "Bakery & Biscuits", "Others")

    val filteredItems = items.filter {
        selectedCategoryFilter == "All" || it.category == selectedCategoryFilter
    }

    fun openAddDialog() {
        itemToEdit = null
        dialogName = ""
        dialogCategory = "Chai & Beverages"
        dialogRate = "15"
        dialogIsActive = true
        showDialog = true
    }

    fun openEditDialog(item: MenuItem) {
        itemToEdit = item
        dialogName = item.name
        dialogCategory = item.category
        dialogRate = item.rate.toInt().toString()
        dialogIsActive = item.isActive
        showDialog = true
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { openAddDialog() },
                containerColor = ChaiPrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("ADD ITEM", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("btn_add_item_master")
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(ChaiBackgroundLight)
                .padding(16.dp)
        ) {
            // Header Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TEA STALL ITEM MASTER",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = ChaiTextPrimary
                    )
                    Text(
                        text = "Manage products, rates & availability",
                        style = MaterialTheme.typography.bodySmall,
                        color = ChaiTextSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ChaiCardWarm
                ) {
                    Text(
                        text = "${items.size} Items",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = ChaiPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategoryFilter == cat,
                        onClick = { selectedCategoryFilter = cat },
                        label = { Text(cat, fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ChaiPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Items List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 72.dp)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isActive) ChaiCardLight else Color(0xFFF0EAE1)
                        ),
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
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (item.isActive) ChaiTextPrimary else ChaiTextSecondary
                                    )
                                    if (!item.isActive) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = Color.LightGray,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "Inactive",
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = item.category,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ChaiTextSecondary
                                )
                            }

                            // Price and actions
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = FormatUtils.formatRupees(item.rate),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black
                                    ),
                                    color = ChaiPrimary
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(onClick = { openEditDialog(item) }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit rate",
                                        tint = ChaiPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Switch(
                                    checked = item.isActive,
                                    onCheckedChange = { onToggleActive(item) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = PaidGreen,
                                        uncheckedTrackColor = Color.LightGray
                                    )
                                )

                                IconButton(onClick = { itemToDelete = item }) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = BalanceRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Item Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = if (itemToEdit == null) "Add Item" else "Edit Item & Rate",
                    fontWeight = FontWeight.Bold,
                    color = ChaiPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = dialogName,
                        onValueChange = { dialogName = it },
                        label = { Text("Item Name *") },
                        placeholder = { Text("e.g. Masala Tea") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = dialogRate,
                        onValueChange = { dialogRate = it },
                        label = { Text("Rate / Price (₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Category:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ChaiTextSecondary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        editCategories.forEach { cat ->
                            FilterChip(
                                selected = dialogCategory == cat,
                                onClick = { dialogCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ChaiPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Active for Billing", fontWeight = FontWeight.Medium)
                        Switch(
                            checked = dialogIsActive,
                            onCheckedChange = { dialogIsActive = it }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val rateVal = dialogRate.toDoubleOrNull() ?: 10.0
                        if (dialogName.isNotBlank()) {
                            val id = itemToEdit?.id ?: 0L
                            onSaveItem(id, dialogName, dialogCategory, rateVal, dialogIsActive) {
                                showDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ChaiPrimary)
                ) {
                    Text("SAVE ITEM", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }

    // Delete confirmation
    itemToDelete?.let { itm ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete Menu Item") },
            text = { Text("Are you sure you want to delete '${itm.name}'? Existing past orders will retain their record.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteItem(itm)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BalanceRed)
                ) {
                    Text("DELETE")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("CANCEL")
                }
            }
        )
    }
}
