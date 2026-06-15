package com.example.ui.screens.masterdata

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.ColleagueEntity
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.glassmorphism
import com.example.ui.theme.BackgroundDark

import com.example.ui.components.EmptyStateView
import androidx.compose.material.icons.outlined.Group

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageColleaguesScreen(
    viewModel: com.example.ui.viewmodels.MainViewModel,
    onBack: () -> Unit
) {
    val colleagues by viewModel.allColleagues.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedColleague by remember { mutableStateOf<ColleagueEntity?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Colleague List", color = MaterialTheme.colorScheme.onBackground) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                }
            )
        },
        floatingActionButton = {
            IconButton(
                onClick = {
                    selectedColleague = null
                    showDialog = true
                },
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Colleague", tint = Color.White)
            }
        }
    ) { innerPadding ->
        val activeColleagues = colleagues.filter { it.isActive }
        if (activeColleagues.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                EmptyStateView(
                    icon = Icons.Outlined.Group,
                    title = "Belum Ada Rekan",
                    description = "Tambahkan nama rekan kerjamu untuk keperluan pencatatan attribution penjualan",
                    ctaText = "+ Tambah Rekan",
                    onCtaClick = { 
                        selectedColleague = null
                        showDialog = true 
                    }
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(activeColleagues) { colleague ->
                    ColleagueItem(
                        colleague = colleague,
                        onEdit = {
                            selectedColleague = colleague
                            showDialog = true
                        },
                        onDelete = {
                            viewModel.deleteColleague(colleague)
                        }
                    )
                }
            }
        }
    }

    if (showDialog) {
        ColleagueDialog(
            initialColleague = selectedColleague,
            onDismiss = { showDialog = false },
            onSave = {
                viewModel.addOrUpdateColleague(it)
                showDialog = false
            }
        )
    }
}

@Composable
fun ColleagueItem(colleague: ColleagueEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = colleague.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                Text(text = "Role: ${colleague.role}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurface)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColleagueDialog(
    initialColleague: ColleagueEntity?,
    onDismiss: () -> Unit,
    onSave: (ColleagueEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialColleague?.name ?: "") }
    var role by remember { mutableStateOf(initialColleague?.role ?: "Regular") }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = null
    ) {
        Column(modifier = Modifier.padding(24.dp).navigationBarsPadding()) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outline)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                if (initialColleague == null) "Tambah Rekan" else "Edit Rekan",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Rekan") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            var roleExpanded by remember { mutableStateOf(false) }
            val roles = listOf("Regular", "Senior")
            
            ExposedDropdownMenuBox(
                expanded = roleExpanded,
                onExpandedChange = { roleExpanded = it }
            ) {
                OutlinedTextField(
                    value = role,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Role") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = roleExpanded,
                    onDismissRequest = { roleExpanded = false }
                ) {
                    roles.forEach { r ->
                        DropdownMenuItem(
                            text = { Text(r) },
                            onClick = {
                                role = r
                                roleExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val colleague = ColleagueEntity(
                        id = initialColleague?.id ?: 0,
                        name = name,
                        role = role,
                        isActive = initialColleague?.isActive ?: true
                    )
                    onSave(colleague)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = name.isNotBlank() && role.isNotBlank(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF6C63FF),
                                    Color(0xFF3B82F6)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Simpan",
                        color = Color.White,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        fontSize = androidx.compose.ui.unit.TextUnit(16f, androidx.compose.ui.unit.TextUnitType.Sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
