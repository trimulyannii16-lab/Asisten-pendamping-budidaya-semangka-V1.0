package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsistenSemangkaApp(viewModel: WatermelonViewModel) {
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Asisten Semangka",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "🍉",
                            fontSize = 24.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Beranda") },
                    label = { Text("Mulai", fontSize = 11.sp, maxLines = 1) },
                    selected = activeTab == 0,
                    onClick = { viewModel.activeTab.value = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_home")
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Edit, contentDescription = "Kalkulator") },
                    label = { Text("Budidaya", fontSize = 11.sp, maxLines = 1) },
                    selected = activeTab == 1,
                    onClick = { viewModel.activeTab.value = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_calc")
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = "AI Diagnosis") },
                    label = { Text("Diagnosis", fontSize = 11.sp, maxLines = 1) },
                    selected = activeTab == 2,
                    onClick = { viewModel.activeTab.value = 2 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_ai")
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Biaya & Hasil") },
                    label = { Text("Keuangan", fontSize = 11.sp, maxLines = 1) },
                    selected = activeTab == 3,
                    onClick = { viewModel.activeTab.value = 3 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_finance")
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = "Cuaca & Scouting") },
                    label = { Text("Pemantauan", fontSize = 11.sp, maxLines = 1) },
                    selected = activeTab == 4,
                    onClick = { viewModel.activeTab.value = 4 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_scout")
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                0 -> BerandaScreen(viewModel)
                1 -> BudidayaCalculatorsScreen(viewModel)
                2 -> DiagnosisAIScreen(viewModel)
                3 -> FinancialScreen(viewModel)
                4 -> PemantauanScreen(viewModel)
            }
        }
    }
}

// --- SCREEN 1: BERANDA SCREEN (Home, Overview & Planting Calendar) ---
@Composable
fun BerandaScreen(viewModel: WatermelonViewModel) {
    val plantings by viewModel.allEvents.collectAsStateWithLifecycle()
    val totalCost by viewModel.totalCostFromDb.collectAsStateWithLifecycle()
    val weather by viewModel.weatherState.collectAsStateWithLifecycle()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var inputBlockName by remember { mutableStateOf("") }
    var inputPopulation by remember { mutableStateOf("1000") }
    var inputNotes by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Organic Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        "Halo Petani Semangka! 👋",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Asisten cerdas mendampingi kesuksesan panen Semangka Anda dari benih hingga rupiah.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "${weather.regionName}: ${weather.tempAir} | ${weather.pollinatorActivityDesc}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        item {
            // Dashboard summary cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Blok Tanam", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${plantings.size} Aktif", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Belanja Dicatat", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(formatCurrency(totalCost), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                    }
                }
            }
        }

        item {
            // Planting Calendar Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Kalender Tanam & Panen luring",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Catatan jadwal semai & taksiran waktu panen semangka",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                    )
                }
                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_planting_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tanam", fontSize = 12.sp)
                }
            }
        }

        if (plantings.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Belum ada blok tanam semangka",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        Text(
                            "Klik tombol '+ Tanam' di atas untuk mencatat penanaman pertama Anda.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(plantings) { event ->
                PlantingEventCard(event = event, onDelete = { viewModel.deletePlanting(event) })
            }
        }
    }

    // Modal Dialog to Add Planting Event
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Mulai Blok Tanam Semangka Baru", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = inputBlockName,
                        onValueChange = { inputBlockName = it },
                        label = { Text("Nama Blok / Lahan") },
                        placeholder = { Text("Contoh: Sawah Kulon B1") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_planting_name_field")
                    )
                    OutlinedTextField(
                        value = inputPopulation,
                        onValueChange = { inputPopulation = it },
                        label = { Text("Jumlah Populasi Pohon Tanaman") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_planting_pop_field")
                    )
                    
                    // Quick seed calculations helper inside dialog
                    val pop = inputPopulation.toIntOrNull() ?: 0
                    val packs = ceil(pop.toDouble() / 300).toInt()
                    if (pop > 0) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Kebutuhan Benih: $packs pack ($pop biji - @300 biji/pack)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = inputNotes,
                        onValueChange = { inputNotes = it },
                        label = { Text("Catatan Tambahan (opsional)") },
                        placeholder = { Text("Contoh: Varietas Semangka Inul Merah") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pop = inputPopulation.toIntOrNull() ?: 1000
                        if (inputBlockName.isNotBlank()) {
                            viewModel.createPlanting(inputBlockName, pop, inputNotes)
                            inputBlockName = ""
                            inputNotes = ""
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_planting_butt")
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun PlantingEventCard(event: PlantingEvent, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        event.blockName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Uji Jarak Tanam: ${event.rowSpacing}m x ${event.holeSpacing}m",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Rencana Semai", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                    Text(event.plantingDate, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                }
                Column {
                    Text("Estimasi Panen 🍉", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                    Text(event.targetHarvestDate, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.tertiary)
                }
                Column {
                    Text("Populasi", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                    Text("${event.plantCount} Batang", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                }
            }

            if (event.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(0.3f))
                        .padding(8.dp)
                ) {
                    Text(
                        "Catatan: ${event.notes}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.8f)
                    )
                }
            }
        }
    }
}


// --- SCREEN 2: HEBAT BUDIDAYA CALCULATORS (Seed calculator, base fert, soil/drench dosing) ---
@Composable
fun BudidayaCalculatorsScreen(viewModel: WatermelonViewModel) {
    var subTabSelector by remember { mutableStateOf(0) } // 0: Benih & Fase, 1: Pupuk Dasar, 2: Dosis Kocor & Tabur

    val inputTotalPop by viewModel.totalPopulationInput.collectAsStateWithLifecycle()
    val inputTankVol by viewModel.kocorTankVolume.collectAsStateWithLifecycle()
    val inputAgeDosing by viewModel.activePhaseDaysDosing.collectAsStateWithLifecycle()
    val doseResult by viewModel.dosingCalculationResult.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab Headers
        TabRow(
            selectedTabIndex = subTabSelector,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = subTabSelector == 0,
                onClick = { subTabSelector = 0 },
                text = { Text("Benih & Fase", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = subTabSelector == 1,
                onClick = { subTabSelector = 1 },
                text = { Text("Pupuk Dasar", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = subTabSelector == 2,
                onClick = { subTabSelector = 2 },
                text = { Text("Kocor & Tabur", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (subTabSelector) {
                0 -> {
                    // SEED POPULATION + GROWTH PHASE
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Kalkulator Kebutuhan Benih Semangka luring", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Akses luring - Sesuai standar jarak tanam tajuk 5m x jarak lubang 70cm.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                OutlinedTextField(
                                    value = viewModel.bedLength.collectAsStateWithLifecycle().value,
                                    onValueChange = { viewModel.bedLength.value = it },
                                    label = { Text("Panjang Bedengan (Meter) per Lajur") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("calc_bed_length")
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = viewModel.bedCount.collectAsStateWithLifecycle().value,
                                    onValueChange = { viewModel.bedCount.value = it },
                                    label = { Text("Jumlah Lajur Bedengan") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("calc_bed_count")
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                val pop = viewModel.calculatedPopulation.collectAsStateWithLifecycle().value
                                val packs = viewModel.calculatedSeedPacksNeeded.collectAsStateWithLifecycle().value
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("Estimasi Total Populasi Lahan:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                        Text("$pop Batang Tanaman", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Kebutuhan Benih (@1 pack isi 300 biji):", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                        Text("$packs Pack (Bungkus)", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Analisis Umur Tanaman & Fase", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                OutlinedTextField(
                                    value = viewModel.plantAgeInput.collectAsStateWithLifecycle().value,
                                    onValueChange = { viewModel.plantAgeInput.value = it },
                                    label = { Text("Masukkan Umur Tanaman Semangka (HST / Hari Setelah Tanam)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("calc_plant_age")
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                val phase = viewModel.plantPhaseInfo.collectAsStateWithLifecycle().value
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(0.4f))
                                        .padding(14.dp)
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("🍉 ", fontSize = 20.sp)
                                            Text(phase.phaseName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Rentang Umur: ${phase.rangeHst}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(phase.description, fontSize = 12.sp)
                                        
                                        Spacer(modifier = Modifier.height(10.dp))
                                        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(0.15f))
                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text("🎯 Fokus Tindakan:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                        Text(phase.checklistFokus, fontSize = 11.sp)
                                        
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text("🧪 Standar Nutrisi Fase Ini:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                        Text(phase.nutrientNeeds, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // BASE FERTILIZER CHECKLIST
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Komparasi Kebutuhan Pupuk Dasar", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                                Text("Input luas gundukan lahan Anda untuk memposisikan dosis luring berkelanjutan.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                                
                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = viewModel.landAreaInput.collectAsStateWithLifecycle().value,
                                    onValueChange = { viewModel.landAreaInput.value = it },
                                    label = { Text("Luas Lahan Semangka (Hektar)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("calc_land_area")
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                val baseResult = viewModel.calculatedBaseFertilizer.collectAsStateWithLifecycle().value

                                Text("📋 Daft Shopping List Pupuk Dasar:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(8.dp))

                                BasicFertilizerRow("Kohe Organik (Ayam/Kambing)", "${baseResult.koheBags.toInt()} karung (${String.format("%.2f", baseResult.koheTons)} Ton)", "Target standar: 70 karung per Hektar @20kg per karung.")
                                BasicFertilizerRow("Nitrogen (Za / Urea)", "${baseResult.ureaKg.toInt()} kg", "Dosis standar: 100 kg per Hektar.")
                                BasicFertilizerRow("Fosfat (SP36/TSP)", "${baseResult.phosphateKg.toInt()} kg", "Dosis standar: 200 kg per Hektar. (Setara 4 zak 50kg atau 8 zak SP26 25kg)")
                                BasicFertilizerRow("Kalium (KCl)", "${baseResult.kclKg.toInt()} kg", "Dosis standar: 150 kg per Hektar. (Setara 3 zak 50kg)")
                                BasicFertilizerRow("Dolomit (CaCO)", "${baseResult.dolomiteKg.toInt()} kg", "Dosis standar: 300-400 kg per Hektar. Menetralisir keasaman bedengan tanah.")
                            }
                        }
                    }
                }
                2 -> {
                    // SOIL + DRENCH ACCURATE DOSES
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Dosis Kocor Drip & Tabur Per Batang", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = inputTotalPop,
                                    onValueChange = { viewModel.totalPopulationInput.value = it },
                                    label = { Text("Total Populasi Tanaman di Lahan") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("dosing_pop_input")
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = inputTankVol,
                                    onValueChange = { viewModel.kocorTankVolume.value = it },
                                    label = { Text("Kapasitas Tangki/Drum Kocor (Liter)") },
                                    placeholder = { Text("Contoh: 100 atau 200") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("dosing_tank_input")
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = inputAgeDosing,
                                    onValueChange = { viewModel.activePhaseDaysDosing.value = it },
                                    label = { Text("Umur Tanaman (HST)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("dosing_age_input")
                                )
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("🎯 Rekomendasi Kocor & Drip Selang (Umur ${inputAgeDosing} HST):", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("• Rata-rata Air per Batang: ${doseResult.solutionPerPlantMl.toInt()} ml", fontSize = 12.sp)
                                Text("• Total Kebutuhan Air Kocor: ${doseResult.totalSolutionLiters.toInt()} Liter", fontSize = 12.sp)
                                Text("• Jumlah Drum Campuran (${inputTankVol} L): ${doseResult.drumsNeeded} Kali Pengisian", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    item {
                        Text("🧪 Racikan Pupuk Kocor (Standar Industri Semangka):", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    if (doseResult.drenchIngredients.isEmpty()) {
                        item {
                            Text("Tidak memerlukan pemupukan pada fase panen.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                        }
                    } else {
                        items(doseResult.drenchIngredients) { ing ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(ing.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                        Text(ing.customInstructions, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(ing.dosePerPlant, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary)
                                        Text("Total: ${ing.totalVolumeKg}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("🌱 Alternatif Pupuk Tabur Per Batang (NPK 16-16-16 & KNO):", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    if (doseResult.taburIngredients.isEmpty()) {
                        item {
                            Text("Fase perkecambahan tidak memerlukan pupuk tabur keras.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                        }
                    } else {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(0.3f))
                                    .padding(10.dp)
                            ) {
                                Text("Dosis Tabur: ${doseResult.taburGramsPerPlant.toInt()} gram per Batang pohon. Total pupuk sediaan: ${String.format("%.1f", doseResult.totalTaburKg)} kg.", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        items(doseResult.taburIngredients) { ingTabur ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(ingTabur.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                                        Text("Cara: ${ingTabur.customInstructions}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Total: ${ingTabur.totalVolumeKg}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BasicFertilizerRow(fertilizer: String, quantity: String, tip: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(0.08f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(fertilizer, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(quantity, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(tip, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
        }
    }
}


// --- SCREEN 3: DIAGNOSIS AI & INFRASTRUCTURE SEARCH (AI & Active Ingredients) ---
@Composable
fun DiagnosisAIScreen(viewModel: WatermelonViewModel) {
    val symptoms by viewModel.symptomsInput.collectAsStateWithLifecycle()
    val rawResult by viewModel.aiDiagnosisResult.collectAsStateWithLifecycle()
    val isLoading by viewModel.isDiagnosisLoading.collectAsStateWithLifecycle()
    val schedule by viewModel.sprayingScheduleList.collectAsStateWithLifecycle()
    val productQuery by viewModel.productSearchQuery.collectAsStateWithLifecycle()
    val productResult by viewModel.productSearchResult.collectAsStateWithLifecycle()

    var activeSubOption by remember { mutableStateOf(0) } // 0: AI Diagnosa, 1: Kamus Toko Obat & Harga

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = activeSubOption) {
            Tab(selected = activeSubOption == 0, onClick = { activeSubOption = 0 }, text = { Text("Pakar AI & Jadwal", fontSize = 12.sp) })
            Tab(selected = activeSubOption == 1, onClick = { activeSubOption = 1 }, text = { Text("Cari Produk & Toko", fontSize = 12.sp) })
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (activeSubOption == 0) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Temukan Masalah Tanaman Semangka Anda", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                            Text("AI akan mengidentifikasi jenis penyakit/hama beserta dosis per 16 L air dan interval semprot.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                            
                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = symptoms,
                                onValueChange = { viewModel.symptomsInput.value = it },
                                label = { Text("Tulis Gejala atau Ciri Masalah") },
                                placeholder = { Text("Contoh: Daun bawah layu cepat mengering, batang belah mengeluarkan getah merah...") },
                                maxLines = 5,
                                modifier = Modifier.fillMaxWidth().testTag("ai_symptoms_input")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Simulated Plant Medical Photos Selector
                            Text("Simulasi Unggah Foto Gejala Pokok:", fontWeight = FontWeight.Medium, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SymptomPhotoChip("Antraknosa / Patek", viewModel.selectedPhotoPlaceholder.value == "antraknosa") {
                                    viewModel.selectedPhotoPlaceholder.value = if (viewModel.selectedPhotoPlaceholder.value == "antraknosa") null else "antraknosa"
                                }
                                SymptomPhotoChip("Layu Fusarium", viewModel.selectedPhotoPlaceholder.value == "layu_fusarium") {
                                    viewModel.selectedPhotoPlaceholder.value = if (viewModel.selectedPhotoPlaceholder.value == "layu_fusarium") null else "layu_fusarium"
                                }
                                SymptomPhotoChip("Thrips Keriting", viewModel.selectedPhotoPlaceholder.value == "thrips") {
                                    viewModel.selectedPhotoPlaceholder.value = if (viewModel.selectedPhotoPlaceholder.value == "thrips") null else "thrips"
                                }
                                SymptomPhotoChip("Lalat Buah", viewModel.selectedPhotoPlaceholder.value == "lalat_buah") {
                                    viewModel.selectedPhotoPlaceholder.value = if (viewModel.selectedPhotoPlaceholder.value == "lalat_buah") null else "lalat_buah"
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { viewModel.runDiagnosis() },
                                modifier = Modifier.fillMaxWidth().testTag("ai_diagnose_button"),
                                enabled = !isLoading && (symptoms.isNotBlank() || viewModel.selectedPhotoPlaceholder.value != null)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Sedang Menganalisis...")
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Diagnosis dengan Asisten AI")
                                }
                            }
                        }
                    }
                }

                if (rawResult != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📝 LAPORAN REKOMENDASI AI:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                    Text("Online & Offline Valid", fontSize = 10.sp, color = Color.Gray)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    rawResult ?: "",
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    if (schedule.isNotEmpty()) {
                        item {
                            Text("📅 Usulan Kalender Semprot Kontrol (Rotasi Bahan Aktif):", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        items(schedule) { day ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(day.dayLabel, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                        Text(day.dosePer16L, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(day.pesticideName, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                    Text(day.instruction, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                                }
                            }
                        }
                    }
                }
            } else {
                // ACTIVE INGREDIENTS & PRODUCT DICTIONARY
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Database Kamus Toko Obat & Harga Terkini", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                            Text("Cari rincian bahan aktif bodi pestisida, tips keamanan, dan pergerakan harga pasar.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                            
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = productQuery,
                                onValueChange = { viewModel.searchProductPrice(it) },
                                label = { Text("Ketik Nama Merk / Bahan Aktif") },
                                placeholder = { Text("Contoh: Amistar, Demolish, Curacron, Dolomit...") },
                                trailingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("product_search_input")
                            )
                        }
                    }
                }

                if (productResult != null) {
                    item {
                        val result = productResult!!
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                                    Text(result.category, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(result.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Bahan Aktif: ${result.activeIngredient}", fontWeight = FontWeight.Medium, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(0.4f))
                                        .padding(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.tertiary)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Harga Rata-rata Kios: ${result.priceEstimate}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text("💡 Tips Penggunaan:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text(result.tips, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.7f))
                            }
                        }
                    }
                }

                item {
                    Text("💡 Rekomendasi Pintasan Pencarian:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(onClick = { viewModel.searchProductPrice("Amistar Top") }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)) {
                            Text("Amistar Top", fontSize = 11.sp)
                        }
                        Button(onClick = { viewModel.searchProductPrice("Demolish") }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)) {
                            Text("Demolish 18EC", fontSize = 11.sp)
                        }
                        Button(onClick = { viewModel.searchProductPrice("Curacron") }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)) {
                            Text("Curacron 500EC", fontSize = 11.sp)
                        }
                        Button(onClick = { viewModel.searchProductPrice("Dolomit") }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)) {
                            Text("Kapur Dolomit", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SymptomPhotoChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 11.sp) },
        leadingIcon = if (selected) {
            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
        } else null
    )
}


// --- SCREEN 4: BIAYA & HASIL (Financial Planning & Shopping List Expense Tracker) ---
@Composable
fun FinancialScreen(viewModel: WatermelonViewModel) {
    val expenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    val budgetResult by viewModel.budgetCalculationResult.collectAsStateWithLifecycle()

    var showExpenseForm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Kalkulator Biaya Modal & Potensi Hasil", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    Text("Simulasi rincian keuntungan penjualan panen semangka berdasarkan Grade A (>4kg) & Grade B (<3.9kg).", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = viewModel.totalLandAreaForCost.collectAsStateWithLifecycle().value,
                            onValueChange = { viewModel.totalLandAreaForCost.value = it },
                            label = { Text("Luas Lahan (HA)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("fin_area_input")
                        )
                        OutlinedTextField(
                            value = viewModel.estimatedYieldTons.collectAsStateWithLifecycle().value,
                            onValueChange = { viewModel.estimatedYieldTons.value = it },
                            label = { Text("Target Hasil (Ton/HA)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = viewModel.pricePerKgGradeA.collectAsStateWithLifecycle().value,
                            onValueChange = { viewModel.pricePerKgGradeA.value = it },
                            label = { Text("Harga Grade A / kg") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = viewModel.pricePerKgGradeB.collectAsStateWithLifecycle().value,
                            onValueChange = { viewModel.pricePerKgGradeB.value = it },
                            label = { Text("Harga Grade B / kg") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = viewModel.estimatedPercentGradeA.collectAsStateWithLifecycle().value,
                            onValueChange = { viewModel.estimatedPercentGradeA.value = it },
                            label = { Text("Proporsi Grade A (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = viewModel.costWage.collectAsStateWithLifecycle().value,
                            onValueChange = { viewModel.costWage.value = it },
                            label = { Text("Upah Pekerja (IDR)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        item {
            // INCOME STATEMENT RESULTS CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("💰 PROYEKSI LABA RUGI SIMULASI:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    FinanceReportRow("Taksiran Modal (Total)", formatCurrency(budgetResult.totalModal), isBold = true)
                    Text("• Belanja Benih: ${formatCurrency(budgetResult.seedCost)}", fontSize = 11.sp)
                    Text("• Pupuk Dasar Organik: ${formatCurrency(budgetResult.organicFertCost)}", fontSize = 11.sp)
                    Text("• Pupuk Kimia Dosis: ${formatCurrency(budgetResult.chemicalFertCost)}", fontSize = 11.sp)
                    Text("• Kapur Dolomit & Pestisida: ${formatCurrency(budgetResult.dolomiteCost + budgetResult.pesticideCost)}", fontSize = 11.sp)
                    Text("• Biaya Operasional (Pekerja, Mulsa): ${formatCurrency(budgetResult.otherCost)}", fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.12f))
                    Spacer(modifier = Modifier.height(8.dp))

                    FinanceReportRow("Taksiran Total Hasil Panen", "${String.format("%.1f", budgetResult.tonTotalYield)} TON", isBold = true)
                    Text("• Grade A (4kg+): ${String.format("%.0f", budgetResult.kgGradeA)} kg -> ${formatCurrency(budgetResult.revenueGradeA)}", fontSize = 11.sp)
                    Text("• Grade B (<3.9kg): ${String.format("%.0f", budgetResult.kgGradeB)} kg -> ${formatCurrency(budgetResult.revenueGradeB)}", fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.12f))
                    Spacer(modifier = Modifier.height(8.dp))

                    FinanceReportRow("Pendapatan Kotor", formatCurrency(budgetResult.grossRevenue), isBold = true)
                    
                    val profitColor = if (budgetResult.netProfit >= 0) MaterialTheme.colorScheme.primary else Color(0xFFC62828)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🚨 Bersih Bersih (Laba Bersih)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(formatCurrency(budgetResult.netProfit), fontWeight = FontWeight.Bold, fontSize = 17.sp, color = profitColor)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📋 Daftar Belanja Riil Luring:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Button(
                    onClick = { showExpenseForm = true },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_expense_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Catat Belanja", fontSize = 12.sp)
                }
            }
        }

        if (expenses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(0.4f))
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("Belum ada catatan transaksi belanja riil lapangan.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            items(expenses) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                                    Text(item.category, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Qty: ${item.quantity.toInt()} ${item.unit}", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(formatCurrency(item.cost * item.quantity), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFC62828))
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { viewModel.deleteExpense(item) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showExpenseForm) {
        var expenseTitle by remember { mutableStateOf("") }
        var expenseCost by remember { mutableStateOf("") }
        var expenseQty by remember { mutableStateOf("1") }
        var expenseUnit by remember { mutableStateOf("Sachet") }
        var expenseCategory by remember { mutableStateOf("Pestisida") }

        AlertDialog(
            onDismissRequest = { showExpenseForm = false },
            title = { Text("Catat Transaksi Belanja Lapangan", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = expenseTitle,
                        onValueChange = { expenseTitle = it },
                        label = { Text("Keperluan Belanja (Nama Barang)") },
                        placeholder = { Text("Misal: Demolish 100ml") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("exp_title_input")
                    )
                    OutlinedTextField(
                        value = expenseCost,
                        onValueChange = { expenseCost = it },
                        label = { Text("Harga Satuan (IDR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("exp_price_input")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = expenseQty,
                            onValueChange = { expenseQty = it },
                            label = { Text("Jumlah (Qty)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = expenseUnit,
                            onValueChange = { expenseUnit = it },
                            label = { Text("Satuan") },
                            placeholder = { Text("Misal: Botol, Zak") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Category dropdown helper (simple Row selector)
                    Text("Pilih Kategori Kebutuhan:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Pupuk Kimia", "Pestisida", "Benih", "Pekerja", "Mulsa/Selang", "Lainnya").forEach { cat ->
                            FilterChip(
                                selected = expenseCategory == cat,
                                onClick = { expenseCategory = cat },
                                label = { Text(cat, fontSize = 10.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val costVal = expenseCost.toDoubleOrNull() ?: 0.0
                        val qtyVal = expenseQty.toDoubleOrNull() ?: 1.0
                        if (expenseTitle.isNotBlank() && costVal > 0.0) {
                            viewModel.addCostExpense(
                                title = expenseTitle,
                                category = expenseCategory,
                                cost = costVal,
                                qty = qtyVal,
                                unit = expenseUnit
                            )
                            expenseTitle = ""
                            expenseCost = ""
                            showExpenseForm = false
                        }
                    },
                    modifier = Modifier.testTag("exp_confirm_button")
                ) {
                    Text("Catat")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExpenseForm = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun FinanceReportRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(value, fontSize = 13.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
    }
}


// --- SCREEN 5: PEMANTAUAN SCREEN (Simulated Coordinates, Bees activity, Scouting entries DB) ---
@Composable
fun PemantauanScreen(viewModel: WatermelonViewModel) {
    val weather by viewModel.weatherState.collectAsStateWithLifecycle()
    val scoutingLogs by viewModel.allLogs.collectAsStateWithLifecycle()

    var showScoutDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sinkronisasi Koordinat & Kondisi Lapangan", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                    }
                    Text("Mengatur iklim presisi berdasar posisi sawah semangka Anda saat ini.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                    
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = viewModel.selectedRegion.collectAsStateWithLifecycle().value,
                        onValueChange = { viewModel.selectedRegion.value = it },
                        label = { Text("Atur Kabupaten/Kecamatan Sawah") },
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { viewModel.selectedRegion.value = "Kabupaten Banyuwangi, Jatim" }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Simulasi GPS")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("region_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WeatherDetailTile("Suhu Udara", weather.tempAir, Icons.Default.Info, Modifier.weight(1f))
                        WeatherDetailTile("Suhu Tanah", weather.tempSoil, Icons.Default.LocationOn, Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WeatherDetailTile("Humiditas", weather.humidityStr, Icons.Default.Refresh, Modifier.weight(1f))
                        WeatherDetailTile("Kadar Oksigen", weather.oxygenPercent, Icons.Default.Star, Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WeatherDetailTile("Indeks UV", weather.uvIntensity, Icons.Default.Warning, Modifier.weight(1f))
                        WeatherDetailTile("Lebah & Serangga", weather.beeActivityScore, Icons.Default.Check, Modifier.weight(1f))
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.tertiaryContainer.copy(0.3f))
                            .padding(10.dp)
                    ) {
                        Text(
                            "Status Penyerbukan Buah Semangka: ${weather.pollinatorActivityDesc}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        item {
            Text("📊 Prakiraan Cuaca Mendatang (Sistem Cuaca Terpadu):", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        items(weather.forecast) { fCast ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(fCast.timeLabel, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(fCast.condition, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(fCast.temp, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                            Text(fCast.pollinatorScore, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("🐛 Jurnal Pemantauan Lapangan (Scouting)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Antisipasi kemunculan dini hama ulat thrips serangga.", fontSize = 11.sp, color = Color.Gray)
                }
                Button(
                    onClick = { showScoutDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("add_scout_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Catat", fontSize = 12.sp)
                }
            }
        }

        if (scoutingLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(0.4f))
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("Belum ada temuan hama lapangan dicatat.", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            items(scoutingLogs) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(log.pestHamaObserved, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                Text("Lahan: ${log.areaName} | Tanggal: ${log.date}", fontSize = 11.sp, color = Color.Gray)
                            }
                            IconButton(onClick = { viewModel.deleteScouting(log) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Close", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val badgeColor = when (log.severityLevel.lowercase()) {
                                "parah" -> Color(0xFFC62828)
                                "sedang" -> Color(0xFFEF6C00)
                                else -> Color(0xFF2E7D32)
                            }
                            Badge(containerColor = badgeColor.copy(0.12f)) {
                                Text("Severity: ${log.severityLevel.uppercase()}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = badgeColor)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                                Text(log.statusTindakan, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }

                        if (log.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Catatan Gejala: ${log.notes}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.8f))
                        }
                    }
                }
            }
        }
    }

    if (showScoutDialog) {
        var scoutArea by remember { mutableStateOf("") }
        var scoutPest by remember { mutableStateOf("") }
        var scoutSeverity by remember { mutableStateOf("Ringan") }
        var scoutNotes by remember { mutableStateOf("") }
        var scoutTindakan by remember { mutableStateOf("Belum Ditangani") }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        AlertDialog(
            onDismissRequest = { showScoutDialog = false },
            title = { Text("Mulai Catatan Scouting Hama Baru", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = scoutArea,
                        onValueChange = { scoutArea = it },
                        label = { Text("Lokasi Temuan Area Bedengan") },
                        placeholder = { Text("Misal: Blok Barat - Baris 4") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("scout_area_input")
                    )
                    OutlinedTextField(
                        value = scoutPest,
                        onValueChange = { scoutPest = it },
                        label = { Text("Hama / Serangga Terlihat") },
                        placeholder = { Text("Misal: Ulat Grayak Tanaman, Thrips") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("scout_pest_input")
                    )
                    OutlinedTextField(
                        value = scoutNotes,
                        onValueChange = { scoutNotes = it },
                        label = { Text("Catatan Luas & Kerusakan") },
                        placeholder = { Text("Misal: Di bawah helaian daun, merusak sekitar 5 batang") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Kepadatan Hama (Severity):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Ringan", "Sedang", "Parah").forEach { level ->
                            FilterChip(
                                selected = scoutSeverity == level,
                                onClick = { scoutSeverity = level },
                                label = { Text(level) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Text("Tindakan Segera:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Belum Ditangani", "Semprot Pestisida Sore", "Cabut Tanaman", "Pasang Perangkap Kuning").forEach { tind ->
                            FilterChip(
                                selected = scoutTindakan == tind,
                                onClick = { scoutTindakan = tind },
                                label = { Text(tind, fontSize = 10.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (scoutArea.isNotBlank() && scoutPest.isNotBlank()) {
                            viewModel.addScoutingEntry(
                                date = sdf.format(Date()),
                                area = scoutArea,
                                pest = scoutPest,
                                severity = scoutSeverity,
                                notes = scoutNotes,
                                tindakan = scoutTindakan
                            )
                            scoutArea = ""
                            scoutPest = ""
                            scoutNotes = ""
                            showScoutDialog = false
                        }
                    },
                    modifier = Modifier.testTag("scout_confirm_button")
                ) {
                    Text("Catat Scouting")
                }
            },
            dismissButton = {
                TextButton(onClick = { showScoutDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun WeatherDetailTile(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(0.06f))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(label, fontSize = 10.sp, color = Color.Gray)
                Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, overflow = TextOverflow.Ellipsis, maxLines = 1)
            }
        }
    }
}

// --- CURRENCY & HELPER FORMATTER ---
fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    return formatter.format(amount).replace("Rp", "Rp ").replace(",00", "")
}
