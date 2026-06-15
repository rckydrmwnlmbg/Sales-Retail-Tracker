package com.example.ui.screens.masterdata

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.BackgroundDark

val NeonCyan = Color(0xFF00E5FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageGoalsScreen(
    viewModel: com.example.ui.viewmodels.MainViewModel,
    onBack: () -> Unit
) {
    val currentGoal by viewModel.currentGoal.collectAsState()
    
    var shopTargetInput by remember { mutableStateOf(currentGoal?.shopTarget?.toLong()?.toString() ?: "") }
    
    // Update input field when data arrives
    LaunchedEffect(currentGoal) {
        if (currentGoal != null && shopTargetInput.isEmpty()) {
            shopTargetInput = currentGoal?.shopTarget?.toLong()?.toString() ?: ""
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Goal Setting", color = MaterialTheme.colorScheme.onBackground) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Monthly Targets", 
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Enter total target for the store, it will automatically distribute to available staff.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    
                    OutlinedTextField(
                        value = shopTargetInput,
                        onValueChange = { shopTargetInput = it },
                        label = { Text("Shop Target (Rp)", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    )
                    
                    Button(
                        onClick = {
                            val target = shopTargetInput.toDoubleOrNull() ?: 0.0
                            viewModel.updateShopTarget(target)
                        },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Calculate & Save", color = Color.White)
                    }
                }
            }

            if (currentGoal != null) {
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val formatter = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("id", "ID")).apply { maximumFractionDigits = 0 }
                        Text("Goal Summary", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                        Text("For Month: ${currentGoal!!.monthYear}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        Text("Target Toko: ${formatter.format(currentGoal!!.shopTarget)}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        Text("Target Grup Regular (60%): ${formatter.format(currentGoal!!.groupTarget)}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        Text("Estimasi Kontribusi Personalmu (50% dari grup): ${formatter.format(currentGoal!!.personalTarget)}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        Text("Estimasi Target Harian: ${formatter.format(currentGoal!!.personalTarget / 26)}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                        
                        Button(
                            onClick = {
                                viewModel.deleteGoal(currentGoal!!)
                                shopTargetInput = ""
                            },
                            modifier = Modifier.padding(top = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                        ) {
                            Text("Delete Goal", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
