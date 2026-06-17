package com.example.ui.screens.log

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.hazeSource
import com.example.data.local.entity.ActivityEntity
import com.example.ui.components.GlassmorphicCard
import com.example.ui.viewmodels.MainViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

val NeonCyan = Color(0xFF00E5FF)
val ElectricMagenta = Color(0xFFD500F9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogActivityScreen(viewModel: MainViewModel) {
    val onBg = MaterialTheme.colorScheme.onBackground

    var selectedTab by remember { mutableStateOf("Sale") }
    var nonSaleCategory by remember { mutableStateOf<String?>(null) }
    
    var isSaving by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var savedTimestamp by remember { mutableStateOf("") }
    
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    
    val products by viewModel.allProducts.collectAsState()
    val colleagues by viewModel.allColleagues.collectAsState()

    // Sale Form State
    var saleProductId by remember { mutableStateOf<Int?>(null) }
    var salePrice by remember { mutableStateOf("") }
    var saleCreditedToId by remember { mutableStateOf<Int?>(null) } // null = Me
    var saleCreditedFromId by remember { mutableStateOf<Int?>(null) } // null = Me
    var saleNotes by remember { mutableStateOf("") }

    // Non-Sale Form State
    var nsProductId by remember { mutableStateOf<Int?>(null) }
    var nsNotes by remember { mutableStateOf("") }
    var nsCategoryDropdown by remember { mutableStateOf("") }
    var nsCustomerType by remember { mutableStateOf("") }
    var nsTopic by remember { mutableStateOf("") }
    var nsLearningContext by remember { mutableStateOf("") }
    
    val localHazeState = com.example.LocalHazeState.current

    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = innerPadding.calculateTopPadding() + 24.dp, bottom = 200.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { LogHeader() }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(onBg.copy(alpha = 0.05f))
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val saleSelected = selectedTab == "Sale"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (saleSelected) NeonCyan.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { haptic.performHapticFeedback(HapticFeedbackType.LongPress); selectedTab = "Sale" },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.AttachMoney, contentDescription = null, tint = if (saleSelected) NeonCyan else onBg.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sale", color = if (saleSelected) NeonCyan else onBg.copy(alpha = 0.5f), fontWeight = if (saleSelected) FontWeight.Bold else FontWeight.Medium)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (!saleSelected) ElectricMagenta.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { haptic.performHapticFeedback(HapticFeedbackType.LongPress); selectedTab = "Non-Sale" },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, tint = if (!saleSelected) ElectricMagenta else onBg.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Non-Sale", color = if (!saleSelected) ElectricMagenta else onBg.copy(alpha = 0.5f), fontWeight = if (!saleSelected) FontWeight.Bold else FontWeight.Medium)
                        }
                    }
                }
            }

            item {
                AnimatedContent(
                    targetState = selectedTab,
                    label = "form_transition"
                ) { tab ->
                    if (tab == "Sale") {
                        SaleForm(
                            products = products,
                            colleagues = colleagues,
                            productId = saleProductId,
                            onProductChange = { 
                                saleProductId = it.id
                                salePrice = it.normalPrice.toInt().toString()
                            },
                            price = salePrice,
                            onPriceChange = { salePrice = it },
                            creditedToId = saleCreditedToId,
                            onCreditedToChange = { saleCreditedToId = it },
                            creditedFromId = saleCreditedFromId,
                            onCreditedFromChange = { saleCreditedFromId = it },
                            notes = saleNotes,
                            onNotesChange = { saleNotes = it }
                        )
                    } else {
                        NonSaleForm(
                            category = nonSaleCategory,
                            onCategoryChange = { nonSaleCategory = it.takeIf { c -> c.isNotBlank() } },
                            products = products,
                            productId = nsProductId,
                            onProductChange = { nsProductId = it },
                            notes = nsNotes,
                            onNotesChange = { nsNotes = it },
                            dropdownProp = nsCategoryDropdown,
                            onDropdownChange = { nsCategoryDropdown = it },
                            customerType = nsCustomerType,
                            onCustomerTypeChange = { nsCustomerType = it },
                            topic = nsTopic,
                            onTopicChange = { nsTopic = it },
                            context = nsLearningContext,
                            onContextChange = { nsLearningContext = it }
                        )
                    }
                }
            }

            if (selectedTab == "Sale" || nonSaleCategory != null) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { 
                            if (isSaving || showSuccess) return@Button
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isSaving = true
                            
                            val timestamp = System.currentTimeMillis()
                            val formatter = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())
                            savedTimestamp = formatter.format(Date(timestamp))

                            // Construct Entity
                            val entity = if (selectedTab == "Sale") {
                                ActivityEntity(
                                    type = "SALE",
                                    timestamp = timestamp,
                                    productId = saleProductId,
                                    price = salePrice.toDoubleOrNull() ?: 0.0,
                                    creditedToId = saleCreditedToId,
                                    creditedFromId = saleCreditedFromId,
                                    notes = saleNotes
                                )
                            } else {
                                val typeEnum = when(nonSaleCategory) {
                                    "Customer Interest" -> "INTEREST"
                                    "Customer Question" -> "QUESTION"
                                    "Lost Opportunity" -> "LOST"
                                    "Product Availability" -> "AVAILABILITY"
                                    "Quick Note" -> "QUICK_NOTE"
                                    "Voice Note" -> "QUICK_NOTE" // Simplified
                                    "Photo Note" -> "QUICK_NOTE" // Simplified
                                    "Learning Note" -> "LEARNING"
                                    else -> "QUICK_NOTE"
                                }
                                ActivityEntity(
                                    type = typeEnum,
                                    timestamp = timestamp,
                                    productId = nsProductId,
                                    notes = nsNotes,
                                    customerType = nsCustomerType.takeIf { it.isNotBlank() },
                                    questionCategory = nsCategoryDropdown.takeIf { nonSaleCategory == "Customer Question" },
                                    lostReason = nsCategoryDropdown.takeIf { nonSaleCategory == "Lost Opportunity" },
                                    availabilityStatus = nsCategoryDropdown.takeIf { nonSaleCategory == "Product Availability" },
                                    topic = nsTopic.takeIf { nonSaleCategory == "Learning Note" },
                                    learningContext = nsLearningContext.takeIf { nonSaleCategory == "Learning Note" },
                                    learningLesson = nsNotes.takeIf { nonSaleCategory == "Learning Note" }
                                )
                            }

                            coroutineScope.launch {
                                viewModel.addActivity(entity)
                                isSaving = false
                                showSuccess = true
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                delay(2500)
                                showSuccess = false
                                
                                // Reset fields
                                saleProductId = null; salePrice = ""; saleNotes = ""; saleCreditedToId = null
                                nsProductId = null; nsNotes = ""; nsCategoryDropdown = ""; nsCustomerType = ""; nsTopic = ""; nsLearningContext = ""
                                nonSaleCategory = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showSuccess) Color(0xFF00C853) else if (selectedTab == "Sale") NeonCyan else ElectricMagenta
                        )
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else if (showSuccess) {
                            // Evidence Pulse Feedback
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Shield, contentDescription = "Success", tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Evidenced: $savedTimestamp", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text(text = "Save to Database", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ... helper forms below ...

@Composable
fun LogHeader() {
    val onBg = MaterialTheme.colorScheme.onBackground
    Column {
        Text("LOG NEW", color = NeonCyan.copy(alpha = 0.8f), letterSpacing = 1.5.sp, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text("Activity", color = onBg, fontWeight = FontWeight.Bold, fontSize = 32.sp)
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleForm(
    products: List<com.example.data.local.entity.ProductEntity>, colleagues: List<com.example.data.local.entity.ColleagueEntity>,
    productId: Int?, onProductChange: (com.example.data.local.entity.ProductEntity) -> Unit,
    price: String, onPriceChange: (String) -> Unit,
    creditedToId: Int?, onCreditedToChange: (Int?) -> Unit,
    creditedFromId: Int?, onCreditedFromChange: (Int?) -> Unit,
    notes: String, onNotesChange: (String) -> Unit
) {
    var productDropdownExpanded by remember { mutableStateOf(false) }
    var colleagueDropdownExpanded by remember { mutableStateOf(false) }
    var creditedFromDropdownExpanded by remember { mutableStateOf(false) }
    val formatter = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
    val onBg = MaterialTheme.colorScheme.onBackground

    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Transaction Details", color = onBg, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            val colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = onBg, unfocusedTextColor = onBg,
                focusedBorderColor = NeonCyan, unfocusedBorderColor = onBg.copy(alpha = 0.5f),
                unfocusedLabelColor = onBg.copy(alpha = 0.7f), focusedLabelColor = NeonCyan, cursorColor = NeonCyan
            )

            // Product Dropdown
            ExposedDropdownMenuBox(expanded = productDropdownExpanded, onExpandedChange = { productDropdownExpanded = it }) {
                OutlinedTextField(
                    value = products.find { it.id == productId }?.name ?: "",
                    onValueChange = {}, readOnly = true, label = { Text("Product Selection") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(), colors = colors,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productDropdownExpanded) }
                )
                ExposedDropdownMenu(expanded = productDropdownExpanded, onDismissRequest = { productDropdownExpanded = false }) {
                    products.forEach { prod ->
                        DropdownMenuItem(text = { Text(prod.name) }, onClick = { onProductChange(prod); productDropdownExpanded = false })
                    }
                }
            }

            // Price
            OutlinedTextField(
                value = price, onValueChange = onPriceChange, label = { Text("Deal Price (Rp)") },
                modifier = Modifier.fillMaxWidth(), colors = colors, singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
            )

            // Credited To
            ExposedDropdownMenuBox(expanded = colleagueDropdownExpanded, onExpandedChange = { colleagueDropdownExpanded = it }) {
                OutlinedTextField(
                    value = if (creditedToId == null) "Me / Current User" else colleagues.find { it.id == creditedToId }?.name ?: "",
                    onValueChange = {}, readOnly = true, label = { Text("Credited To") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(), colors = colors,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = colleagueDropdownExpanded) },
                    enabled = creditedFromId == null // Cannot credit to someone else if someone credited to me
                )
                ExposedDropdownMenu(expanded = colleagueDropdownExpanded, onDismissRequest = { colleagueDropdownExpanded = false }) {
                    DropdownMenuItem(text = { Text("Me / Current User") }, onClick = { onCreditedToChange(null); colleagueDropdownExpanded = false })
                    colleagues.forEach { col ->
                        DropdownMenuItem(text = { Text(col.name) }, onClick = { onCreditedToChange(col.id); colleagueDropdownExpanded = false })
                    }
                }
            }
            
            // Credited From
            ExposedDropdownMenuBox(expanded = creditedFromDropdownExpanded, onExpandedChange = { creditedFromDropdownExpanded = it }) {
                OutlinedTextField(
                    value = if (creditedFromId == null) "Me / Current User" else colleagues.find { it.id == creditedFromId }?.name ?: "",
                    onValueChange = {}, readOnly = true, label = { Text("Sale Made By (Credited From)") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(), colors = colors,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = creditedFromDropdownExpanded) },
                    enabled = creditedToId == null // Cannot have someone credit to me if I am crediting to someone else
                )
                ExposedDropdownMenu(expanded = creditedFromDropdownExpanded, onDismissRequest = { creditedFromDropdownExpanded = false }) {
                    DropdownMenuItem(text = { Text("Me / Current User") }, onClick = { onCreditedFromChange(null); creditedFromDropdownExpanded = false })
                    colleagues.forEach { col ->
                        DropdownMenuItem(text = { Text(col.name) }, onClick = { onCreditedFromChange(col.id); creditedFromDropdownExpanded = false })
                    }
                }
            }

            // Timestamp
            OutlinedTextField(
                value = formatter.format(Date()), onValueChange = {}, label = { Text("Timestamp (Tercatat Otomatis)") }, readOnly = true, modifier = Modifier.fillMaxWidth(), colors = colors
            )

            // Notes
            OutlinedTextField(
                value = notes, onValueChange = onNotesChange, label = { Text("Additional Notes (Optional)") }, modifier = Modifier.fillMaxWidth().height(80.dp), colors = colors
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NonSaleForm(
    category: String?, onCategoryChange: (String) -> Unit,
    products: List<com.example.data.local.entity.ProductEntity>,
    productId: Int?, onProductChange: (Int) -> Unit,
    notes: String, onNotesChange: (String) -> Unit,
    dropdownProp: String, onDropdownChange: (String) -> Unit,
    customerType: String, onCustomerTypeChange: (String) -> Unit,
    topic: String, onTopicChange: (String) -> Unit,
    context: String, onContextChange: (String) -> Unit
) {
    if (category == null) {
        val onBg = MaterialTheme.colorScheme.onBackground
        val categories = listOf(
            "Customer Interest" to Icons.Outlined.FavoriteBorder,
            "Customer Question" to Icons.Outlined.HelpOutline,
            "Lost Opportunity" to Icons.Outlined.MoneyOff,
            "Product Availability" to Icons.Outlined.Inventory2,
            "Quick Note" to Icons.Outlined.Notes,
            "Voice Note" to Icons.Outlined.MicNone,
            "Photo Note" to Icons.Outlined.CameraAlt,
            "Learning Note" to Icons.Outlined.LightbulbCircle
        )
        LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.height(260.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { (cat, icon) ->
                Card(
                    onClick = { onCategoryChange(cat) },
                    modifier = Modifier.height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = onBg.copy(alpha=0.05f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, onBg.copy(alpha = 0.1f))
                ) {
                    Row(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(icon, contentDescription = null, tint = ElectricMagenta, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(cat, color = onBg, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                }
            }
        }
        return
    }

    val onBg = MaterialTheme.colorScheme.onBackground

    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(category, color = onBg, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                TextButton(onClick = { onCategoryChange("") }) {
                    Text("Change", color = ElectricMagenta, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            val colors = OutlinedTextFieldDefaults.colors(focusedTextColor = onBg, unfocusedTextColor = onBg, focusedBorderColor = ElectricMagenta, unfocusedBorderColor = onBg.copy(alpha = 0.5f), unfocusedLabelColor = onBg.copy(alpha = 0.7f), focusedLabelColor = ElectricMagenta)

            var dropdownExpanded by remember { mutableStateOf(false) }

            // Dynamic Fields based on category
            if (category == "Customer Interest" || category == "Customer Question" || category == "Lost Opportunity" || category == "Product Availability") {
                ExposedDropdownMenuBox(expanded = dropdownExpanded, onExpandedChange = { dropdownExpanded = it }) {
                    OutlinedTextField(
                        value = products.find { it.id == productId }?.name ?: "", onValueChange = {}, readOnly = true, label = { Text(if(category=="Customer Question") "Product (Optional)" else "Product Selection") }, modifier = Modifier.fillMaxWidth().menuAnchor(), colors = colors, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) }
                    )
                    ExposedDropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }) {
                        products.forEach { prod -> DropdownMenuItem(text = { Text(prod.name) }, onClick = { onProductChange(prod.id); dropdownExpanded = false }) }
                    }
                }
            }
            if (category == "Customer Interest") {
                OutlinedTextField(value = customerType, onValueChange = onCustomerTypeChange, label = { Text("Customer Type") }, modifier = Modifier.fillMaxWidth(), colors = colors)
            }
            if (category == "Customer Question" || category == "Lost Opportunity" || category == "Product Availability") {
                var catExpanded by remember { mutableStateOf(false) }
                val options = when(category) {
                    "Customer Question" -> listOf("Water Resistance", "Battery-Solar", "Bluetooth", "Warranty", "Lainnya")
                    "Lost Opportunity" -> listOf("Terlalu Mahal", "Bandingkan", "Stok Habis", "Lainnya")
                    else -> listOf("Tersedia", "Menipis", "Habis")
                }
                ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = it }) {
                    OutlinedTextField(
                        value = dropdownProp, onValueChange = {}, readOnly = true, label = { Text("Category/Status") }, modifier = Modifier.fillMaxWidth().menuAnchor(), colors = colors, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) }
                    )
                    ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                        options.forEach { opt -> DropdownMenuItem(text = { Text(opt) }, onClick = { onDropdownChange(opt); catExpanded = false }) }
                    }
                }
            }
            if (category == "Learning Note") {
                OutlinedTextField(value = topic, onValueChange = onTopicChange, label = { Text("Topic") }, modifier = Modifier.fillMaxWidth(), colors = colors)
                OutlinedTextField(value = context, onValueChange = onContextChange, label = { Text("Context/Situation") }, modifier = Modifier.fillMaxWidth(), colors = colors)
            }
            
            OutlinedTextField(value = notes, onValueChange = onNotesChange, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth().height(80.dp), colors = colors)
        }
    }
}
