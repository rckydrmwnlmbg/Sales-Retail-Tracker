package com.example.ui.screens.masterdata

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.local.entity.ProductEntity
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.foundation.clickable
import kotlinx.coroutines.launch
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.GlassCard
import com.example.ui.components.glassmorphism
import com.example.ui.theme.BackgroundDark

import com.example.ui.components.EmptyStateView
import androidx.compose.material.icons.outlined.Inventory2

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.Bitmap
import com.example.logic.ProductScanResult
import com.example.logic.ScanBrochureUseCase
import com.example.logic.ScannedProduct
import androidx.compose.material3.CircularProgressIndicator

sealed class ScanUiState {
    object Idle : ScanUiState()
    object Processing : ScanUiState()
    data class Success(val result: ProductScanResult) : ScanUiState()
    data class Error(val message: String) : ScanUiState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageProductsScreen(
    viewModel: com.example.ui.viewmodels.MainViewModel,
    onBack: () -> Unit
) {
    val products by viewModel.allProducts.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var showAddMenu by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var snackbarHostState = remember { SnackbarHostState() }
    
    var scanUiState by remember { mutableStateOf<ScanUiState>(ScanUiState.Idle) }
    var showScanResultDialog by remember { mutableStateOf(false) }

    val openRouterApiKey by viewModel.openRouterApiKey.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            scanUiState = ScanUiState.Processing
            coroutineScope.launch {
                try {
                    val result = ScanBrochureUseCase.scanBrochure(bitmap, openRouterApiKey)
                    scanUiState = ScanUiState.Success(result)
                    if (result.products.isNotEmpty()) {
                        showScanResultDialog = true
                    }
                } catch (e: Exception) {
                    scanUiState = ScanUiState.Idle
                    snackbarHostState.showSnackbar(
                        message = e.message ?: "Terjadi kesalahan saat scan brosur."
                    )
                }
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
             val contentResolver = context.contentResolver
             try {
                 val mimeType = contentResolver.getType(uri)
                 var bitmap: android.graphics.Bitmap? = null
                 if (mimeType == "application/pdf") {
                     val pfd = contentResolver.openFileDescriptor(uri, "r")
                     if (pfd != null) {
                         val pdfRenderer = android.graphics.pdf.PdfRenderer(pfd)
                         if (pdfRenderer.pageCount > 0) {
                             val page = pdfRenderer.openPage(0)
                             bitmap = android.graphics.Bitmap.createBitmap(page.width * 2, page.height * 2, android.graphics.Bitmap.Config.ARGB_8888)
                             bitmap.eraseColor(android.graphics.Color.WHITE)
                             page.render(bitmap, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                             page.close()
                         }
                         pdfRenderer.close()
                         pfd.close()
                     }
                 } else {
                     val inputStream = contentResolver.openInputStream(uri)
                     bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                     inputStream?.close()
                 }

                if(bitmap != null) {
                    scanUiState = ScanUiState.Processing
                    coroutineScope.launch {
                        try {
                            val result = ScanBrochureUseCase.scanBrochure(bitmap, openRouterApiKey)
                            scanUiState = ScanUiState.Success(result)
                            if (result.products.isNotEmpty()) {
                                showScanResultDialog = true
                            }
                        } catch (e: Exception) {
                            scanUiState = ScanUiState.Idle
                            snackbarHostState.showSnackbar(
                                message = e.message ?: "Terjadi kesalahan saat analisa gambar/dokumen."
                            )
                        }
                    }
                } else {
                    coroutineScope.launch { snackbarHostState.showSnackbar("Gagal memproses dokumen/gambar yang dipilih") }
                }
             } catch (e: Exception) {
                 coroutineScope.launch { snackbarHostState.showSnackbar("Gagal memuat dokumen: ${e.message}") }
             }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Izin kamera diperlukan untuk fitur scan brosur.")
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Product Catalog", color = MaterialTheme.colorScheme.onBackground) },
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
                onClick = { showAddMenu = true },
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product", tint = Color.White)
            }
        }
    ) { innerPadding ->
        val activeProducts = products.filter { it.isActive }
        if (activeProducts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                EmptyStateView(
                    icon = Icons.Outlined.Inventory2,
                    title = "Belum Ada Produk",
                    description = "Tambahkan produk Casio yang tersedia di tokomu agar bisa dipilih saat mencatat transaksi",
                    ctaText = "+ Tambah Produk",
                    onCtaClick = { showAddMenu = true }
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
                items(activeProducts) { product ->
                    ProductItem(
                        product = product,
                        onEdit = {
                            selectedProduct = product
                            showDialog = true
                        },
                        onDelete = {
                            viewModel.deleteProduct(product)
                        }
                    )
                }
            }
        }
    }

    if (showAddMenu) {
        ModalBottomSheet(
            onDismissRequest = { showAddMenu = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Tambah Produk", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleLarge)
                
                Card(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text("Add Manual", color = MaterialTheme.colorScheme.onSurface) },
                        supportingContent = { Text("Ketik detail satu per satu", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        leadingContent = { Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable {
                            showAddMenu = false
                            selectedProduct = null
                            showDialog = true
                        }
                    )
                }
                
                Card(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text("Scan Brosur (AI)", color = MaterialTheme.colorScheme.onSurface) },
                        supportingContent = { Text("Ekstrak produk via kamera", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        leadingContent = { Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable {
                            showAddMenu = false
                            permissionLauncher.launch(android.Manifest.permission.CAMERA)
                        }
                    )
                }
                
                Card(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text("Upload Gambar/Brosur", color = MaterialTheme.colorScheme.onSurface) },
                        supportingContent = { Text("Import file katalog produk", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        leadingContent = { Icon(Icons.Default.UploadFile, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable {
                            showAddMenu = false
                            filePickerLauncher.launch("*/*")
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (scanUiState is ScanUiState.Processing) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { }) {
            Card(modifier = Modifier.size(240.dp)) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Menganalisa brosur...", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                    Text("Mohon tunggu sebentar", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    if (showScanResultDialog && scanUiState is ScanUiState.Success) {
        val result = (scanUiState as ScanUiState.Success).result
        ModalBottomSheet(
            onDismissRequest = { showScanResultDialog = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Ditemukan ${result.products.size} Produk", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                Text("Confidence: ${result.confidence}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (result.notes.isNotEmpty()) {
                    Text("Notes: ${result.notes}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        coroutineScope.launch {
                            for (sp in result.products) {
                                viewModel.addOrUpdateProduct(sp.toProductEntity())
                            }
                            snackbarHostState.showSnackbar("Berhasil menambahkan ${result.products.size} produk.")
                            showScanResultDialog = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Simpan Semua Produk")
                }
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(result.products) { sp ->
                        Card {
                            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(sp.productName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                                    Text("${sp.productCode} • ${sp.category}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = {
                                    selectedProduct = sp.toProductEntity()
                                    showScanResultDialog = false
                                    showDialog = true
                                }) {
                                    Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        ProductDialog(
            initialProduct = selectedProduct,
            onDismiss = { showDialog = false },
            onSave = {
                viewModel.addOrUpdateProduct(it)
                showDialog = false
            }
        )
    }
}

@Composable
fun ProductItem(product: ProductEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                Text(text = "Code: ${product.code} | Category: ${product.category}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "Rp ${product.normalPrice}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
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
fun ProductDialog(
    initialProduct: ProductEntity?,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
) {
    var code by remember { mutableStateOf(initialProduct?.code ?: "") }
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var category by remember { mutableStateOf(initialProduct?.category ?: "") }
    var priceStr by remember { mutableStateOf(initialProduct?.normalPrice?.toString() ?: "") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (initialProduct == null) "Add Product" else "Edit Product",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = colors
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = colors
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = colors
                    )
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Price") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = colors
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val price = priceStr.toDoubleOrNull() ?: 0.0
                            val product = ProductEntity(
                                id = initialProduct?.id ?: 0,
                                code = code,
                                name = name,
                                category = category,
                                normalPrice = price,
                                isActive = initialProduct?.isActive ?: true
                            )
                            onSave(product)
                        },
                        enabled = name.isNotBlank() && priceStr.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Save", color = Color.White)
                    }
                }
            }
        }
    }
}
