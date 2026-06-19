package com.example.ui.screens.log

import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ActivityEntity

import com.example.ui.viewmodels.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogBottomSheet(viewModel: MainViewModel, onDismiss: () -> Unit) {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(1000)
        }
    }
    val formatter = SimpleDateFormat("EEEE, dd MMM yyyy • HH:mm:ss", Locale.getDefault())

    val bodyColor = MaterialTheme.colorScheme.onBackground
    
    var selectedType by remember { mutableStateOf<String?>(null) }
    
    val products by viewModel.allProducts.collectAsState()
    val colleagues by viewModel.allColleagues.collectAsState()
    
    var price by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var discount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<Int?>(null) }
    var creditedToId by remember { mutableStateOf<Int?>(null) }
    var creditedFromId by remember { mutableStateOf<Int?>(null) }
    
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                note = (if (note.isEmpty()) "" else "$note ") + matches[0]
            }
        }
    }
    
    val coroutineScope = rememberCoroutineScope()
    val scrollState = androidx.compose.foundation.rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Catat Aktivitas", color = bodyColor, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(4.dp))
        Text(formatter.format(Date(currentTime)), color = bodyColor.copy(alpha = 0.5f), fontSize = 12.sp, style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"))
        Spacer(modifier = Modifier.height(24.dp))
        
        val types = listOf(
            Triple("Sale", Icons.Outlined.AttachMoney, Color(0xFF4ADE80)),
            Triple("Customer Interest", Icons.Outlined.RemoveRedEye, Color(0xFFFBBF24)),
            Triple("Customer Question", Icons.Outlined.HelpOutline, Color(0xFF3B82F6)),
            Triple("Lost Opportunity", Icons.Outlined.HighlightOff, Color(0xFFF87171)),
            Triple("Product Availability", Icons.Outlined.Inventory2, Color(0xFF2DD4BF)),
            Triple("Quick Note", Icons.Outlined.Description, Color(0xFF9CA3AF)),
            Triple("Voice Note", Icons.Outlined.MicNone, Color(0xFFD946EF)),
            Triple("Learning Note", Icons.Outlined.AutoStories, Color(0xFFF97316))
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val chunks = types.chunked(2)
            chunks.forEach { rowTypes ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowTypes.forEach { (name, icon, strokeColor) ->
                        val isSelected = selectedType == name
                        val bgColor = MaterialTheme.colorScheme.surfaceVariant
                        val activeBgColor = MaterialTheme.colorScheme.primaryContainer
                        val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) activeBgColor else bgColor)
                                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                .clickable { selectedType = name }
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = icon, contentDescription = name, tint = strokeColor, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = name, color = bodyColor, fontSize = 12.sp, fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Medium, maxLines = 2, lineHeight = 14.sp)
                        }
                    }
                }
            }
        }
        
        if (selectedType != null) {
            Spacer(modifier = Modifier.height(24.dp))
            val fieldColors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = bodyColor, unfocusedTextColor = bodyColor,
                focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = bodyColor.copy(alpha = 0.12f),
                unfocusedContainerColor = bodyColor.copy(alpha = 0.06f), focusedContainerColor = bodyColor.copy(alpha = 0.06f),
                unfocusedLabelColor = bodyColor.copy(alpha = 0.5f)
            )

            if (selectedType == "Sale" || selectedType == "Customer Interest" || selectedType == "Lost Opportunity" || selectedType == "Product Availability") {
                var expanded by remember { mutableStateOf(false) }
                var productSearchText by remember { mutableStateOf("") }
                
                LaunchedEffect(selectedProduct) {
                    if (!expanded) {
                        productSearchText = products.find { it.id == selectedProduct }?.name ?: ""
                    }
                }
                
                val filteredProducts = remember(productSearchText, products) {
                    if (productSearchText.isBlank()) products else products.filter { it.name.contains(productSearchText, ignoreCase = true) }
                }

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = productSearchText,
                        onValueChange = { 
                            productSearchText = it
                            if (!expanded) expanded = true
                        },
                        label = { Text("Pilih Produk (Ketik untuk mencari)") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(), colors = fieldColors, shape = RoundedCornerShape(12.dp),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        singleLine = true
                    )
                    if (expanded) {
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { 
                            expanded = false 
                            productSearchText = products.find { it.id == selectedProduct }?.name ?: ""
                        }) {
                            if (filteredProducts.isEmpty()) {
                                DropdownMenuItem(text = { Text("No products found", color = bodyColor.copy(alpha=0.5f)) }, onClick = { })
                            } else {
                                filteredProducts.forEach { prod ->
                                    DropdownMenuItem(
                                        text = { Text(prod.name) }, 
                                        onClick = { 
                                            selectedProduct = prod.id
                                            productSearchText = prod.name
                                            if (selectedType == "Sale") price = prod.normalPrice.toInt().toString()
                                            expanded = false 
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (selectedType == "Sale") {
                OutlinedTextField(
                    value = price, onValueChange = { price = it }, label = { Text("Deal Price per item (Rp)") },
                    modifier = Modifier.fillMaxWidth(), colors = fieldColors, shape = RoundedCornerShape(12.dp), singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantity, onValueChange = { quantity = it }, label = { Text("QTY") },
                        modifier = Modifier.weight(1f), colors = fieldColors, shape = RoundedCornerShape(12.dp), singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = discount, onValueChange = { discount = it }, label = { Text("Discount (%)") },
                        modifier = Modifier.weight(1f), colors = fieldColors, shape = RoundedCornerShape(12.dp), singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                val computedPrice = (price.toDoubleOrNull() ?: 0.0) * (quantity.toIntOrNull() ?: 1) * (1.0 - (discount.toDoubleOrNull() ?: 0.0) / 100.0)
                if (computedPrice > 0.0) {
                    val formattedFinal = java.text.NumberFormat.getIntegerInstance(java.util.Locale("id", "ID")).format(computedPrice)
                    Text(text = "Final Price: Rp $formattedFinal", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(start = 4.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                var colExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = colExpanded, onExpandedChange = { colExpanded = it }) {
                    OutlinedTextField(
                        value = if (creditedToId == null) "Me (Saya Pribadi)" else colleagues.find { it.id == creditedToId }?.name ?: "",
                        onValueChange = {}, readOnly = true, label = { Text("Credit To") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(), colors = fieldColors, shape = RoundedCornerShape(12.dp),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = colExpanded) },
                        enabled = creditedFromId == null
                    )
                    ExposedDropdownMenu(expanded = colExpanded, onDismissRequest = { colExpanded = false }) {
                        DropdownMenuItem(text = { Text("Me (Saya Pribadi)") }, onClick = { creditedToId = null; colExpanded = false })
                        colleagues.forEach { col ->
                            DropdownMenuItem(text = { Text(col.name) }, onClick = { creditedToId = col.id; colExpanded = false })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                var fromExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = fromExpanded, onExpandedChange = { fromExpanded = it }) {
                    OutlinedTextField(
                        value = if (creditedFromId == null) "Me (Saya Pribadi)" else colleagues.find { it.id == creditedFromId }?.name ?: "",
                        onValueChange = {}, readOnly = true, label = { Text("Sale Made By (Credited From)") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(), colors = fieldColors, shape = RoundedCornerShape(12.dp),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromExpanded) },
                        enabled = creditedToId == null
                    )
                    ExposedDropdownMenu(expanded = fromExpanded, onDismissRequest = { fromExpanded = false }) {
                        DropdownMenuItem(text = { Text("Me (Saya Pribadi)") }, onClick = { creditedFromId = null; fromExpanded = false })
                        colleagues.forEach { col ->
                            DropdownMenuItem(text = { Text(col.name) }, onClick = { creditedFromId = col.id; fromExpanded = false })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            OutlinedTextField(
                value = note, onValueChange = { note = it }, label = { Text("Catatan Detail") },
                modifier = Modifier.fillMaxWidth().height(120.dp), colors = fieldColors, shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    IconButton(onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().language)
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Bicara sekarang...")
                        }
                        try {
                            speechLauncher.launch(intent)
                        } catch (e: Exception) {
                            // no speech to text available
                        }
                    }) {
                        Icon(imageVector = Icons.Outlined.Mic, contentDescription = "Voice Input", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { 
                    coroutineScope.launch {
                        val typeEnum = when(selectedType) {
                            "Sale" -> "SALE"
                            "Customer Interest" -> "INTEREST"
                            "Customer Question" -> "QUESTION"
                            "Lost Opportunity" -> "LOST"
                            "Product Availability" -> "AVAILABILITY"
                            "Learning Note" -> "LEARNING"
                            else -> "QUICK_NOTE"
                        }
                        val finalP = (price.toDoubleOrNull() ?: 0.0) * (quantity.toIntOrNull() ?: 1) * (1.0 - (discount.toDoubleOrNull() ?: 0.0) / 100.0)
                        viewModel.addActivity(
                            ActivityEntity(
                                type = typeEnum,
                                timestamp = System.currentTimeMillis(),
                                productId = selectedProduct,
                                price = price.toDoubleOrNull() ?: 0.0,
                                quantity = quantity.toIntOrNull() ?: 1,
                                discount = discount.toDoubleOrNull() ?: 0.0,
                                finalPrice = finalP,
                                creditedToId = creditedToId,
                                creditedFromId = creditedFromId,
                                notes = note
                            )
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF6C63FF), Color(0xFF3B82F6)))), contentAlignment = Alignment.Center) {
                    Text("Simpan", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
