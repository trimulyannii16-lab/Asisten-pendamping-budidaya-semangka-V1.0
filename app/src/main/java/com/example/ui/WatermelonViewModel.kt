package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil

class WatermelonViewModel(application: Application) : AndroidViewModel(application) {

    private val db = WatermelonDatabase.getDatabase(application)
    private val repository = WatermelonRepository(db.watermelonDao())

    // Database Flows
    val allEvents: StateFlow<List<PlantingEvent>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogs: StateFlow<List<ScoutingLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExpenses: StateFlow<List<ExpenseItem>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCostFromDb: StateFlow<Double> = repository.totalCost
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // --- Tab / Screen state ---
    var activeTab = MutableStateFlow(0) // 0: Beranda, 1: Kalkulator, 2: AI Diagnosis, 3: Biaya & Hasil, 4: Cuaca & Pupuk

    // --- Kalkulator Populasi & Fase Tanaman ---
    var bedLength = MutableStateFlow("100") // meters
    var bedCount = MutableStateFlow("10") // rows
    val rowSpacingMeters = 5.0 // standard
    val holeSpacingMeters = 0.7 // standard 70cm
    val seedsPerPack = 300

    val calculatedPopulation = combine(bedLength, bedCount) { lengthStr, countStr ->
        val length = lengthStr.toDoubleOrNull() ?: 0.0
        val count = countStr.toIntOrNull() ?: 0
        val plantsPerRow = if (length > 0) ceil(length / holeSpacingMeters).toInt() else 0
        plantsPerRow * count
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val calculatedSeedPacksNeeded = calculatedPopulation.map { pop ->
        ceil(pop.toDouble() / seedsPerPack).toInt()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // --- Identifikasi Fase Tanaman berdasarkan Umur ---
    var plantAgeInput = MutableStateFlow("25") // HST default

    val plantPhaseInfo = plantAgeInput.map { ageStr ->
        val age = ageStr.toIntOrNull() ?: 0
        getPhaseDetails(age)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), getPhaseDetails(25))

    // --- Pupuk Dasar calculator ---
    var landAreaInput = MutableStateFlow("1.0") // in Hectares default

    val calculatedBaseFertilizer = landAreaInput.map { areaStr ->
        val area = areaStr.toDoubleOrNull() ?: 0.0
        // Organic: 70 bags/hectare * 20kg per bag = 1400kg = 1.4 tons
        val totalBagsKohe = area * 70
        val totalTonKohe = (totalBagsKohe * 20) / 1000.0

        // Nitrogen (Za/Urea) = 100 kg / hectare
        val ureaKg = area * 100

        // Phosphate = 200 kg / hectare
        val phosphateKg = area * 200

        // Potassium (KCl) = 150 kg / hectare
        val kclKg = area * 150

        // Dolomite = 350 kg / hectare (between 300-400)
        val dolomiteKg = area * 350

        BaseFertilizerResult(
            koheBags = totalBagsKohe,
            koheTons = totalTonKohe,
            ureaKg = ureaKg,
            phosphateKg = phosphateKg,
            kclKg = kclKg,
            dolomiteKg = dolomiteKg
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BaseFertilizerResult())

    // --- AI Disease / Pest Diagnosis ---
    var symptomsInput = MutableStateFlow("")
    var aiDiagnosisResult = MutableStateFlow<String?>(null)
    var isDiagnosisLoading = MutableStateFlow(false)
    var activeIngredientSearchQuery = MutableStateFlow("")
    var selectedPhotoPlaceholder = MutableStateFlow<String?>(null) // "antraknosa", "layu_fusarium", etc.

    // Spraying Schedule recommendation generator
    var sprayingScheduleList = MutableStateFlow<List<SprayingDay>>(emptyList())

    // Price query info
    var productSearchQuery = MutableStateFlow("")
    var productSearchResult = MutableStateFlow<ProductInfo?>(null)

    // --- Capital Cost & Yield Simulator ---
    var totalLandAreaForCost = MutableStateFlow("1") // HA
    var costWage = MutableStateFlow("4000000") // IDR labour
    var costMulsa = MutableStateFlow("1200000") // IDR mulsa & drip hose
    var pricePerKgGradeA = MutableStateFlow("8500") // IDR price A
    var pricePerKgGradeB = MutableStateFlow("5500") // IDR price B
    var estimatedYieldTons = MutableStateFlow("25") // Tons output per Hectare (watermelons average 20-30 Tons/HA)
    var estimatedPercentGradeA = MutableStateFlow("75") // 75% Grade A (>4kg)
    var customExpenseTitle = MutableStateFlow("")
    var customExpenseCategory = MutableStateFlow("Pupuk Kimia")
    var customExpenseQuantity = MutableStateFlow("1")
    var customExpenseUnit = MutableStateFlow("Karung")
    var customExpenseCost = MutableStateFlow("150000")

    val budgetCalculationResult = combine(
        totalLandAreaForCost,
        costWage,
        costMulsa,
        pricePerKgGradeA,
        pricePerKgGradeB,
        estimatedYieldTons,
        estimatedPercentGradeA,
        calculatedSeedPacksNeeded,
        calculatedBaseFertilizer
    ) { params ->
        val area = params[0] as String
        val wage = params[1] as String
        val mulsa = params[2] as String
        val prA = params[3] as String
        val prB = params[4] as String
        val yieldTons = params[5] as String
        val pctA = params[6] as String
        val seedPacks = params[7] as Int
        val baseFert = params[8] as BaseFertilizerResult

        val numArea = area.toDoubleOrNull() ?: 1.0
        val numWage = wage.toDoubleOrNull() ?: 0.0
        val numMulsa = mulsa.toDoubleOrNull() ?: 0.0
        val numPriceA = prA.toDoubleOrNull() ?: 8500.0
        val numPriceB = prB.toDoubleOrNull() ?: 5500.0
        val numYieldTons = (yieldTons.toDoubleOrNull() ?: 25.0) * numArea
        val numPctA = (pctA.toDoubleOrNull() ?: 75.0) / 100.0
        val numPctB = 1.0 - numPctA

        // Calculate Modal (Shopping & Expenses) based on industry standards
        val seedCost = seedPacks * 250000.0 // 250k IDR/pack
        val organicFertCost = baseFert.koheBags * 20000.0 // 20k IDR per bag
        val chemicalFertCost = (baseFert.ureaKg * 6000) + (baseFert.phosphateKg * 8000) + (baseFert.kclKg * 9000)
        val dolomiteCost = baseFert.dolomiteKg * 3000 // 150k per 50kg bag
        val pesticideCost = numArea * 1500000.0 // 1.5M per HA estimate

        val totalCapitalCost = seedCost + organicFertCost + chemicalFertCost + dolomiteCost + pesticideCost + numWage + numMulsa

        // Potential Yield
        val yieldKg = numYieldTons * 1000.0
        val weightGradeA = yieldKg * numPctA
        val weightGradeB = yieldKg * numPctB

        val revenueA = weightGradeA * numPriceA
        val revenueB = weightGradeB * numPriceB
        val totalRevenue = revenueA + revenueB
        val netProfit = totalRevenue - totalCapitalCost

        BudgetResult(
            seedCost = seedCost,
            organicFertCost = organicFertCost,
            chemicalFertCost = chemicalFertCost,
            dolomiteCost = dolomiteCost,
            pesticideCost = pesticideCost,
            otherCost = numWage + numMulsa,
            totalModal = totalCapitalCost,
            tonTotalYield = numYieldTons,
            kgGradeA = weightGradeA,
            kgGradeB = weightGradeB,
            revenueGradeA = revenueA,
            revenueGradeB = revenueB,
            grossRevenue = totalRevenue,
            netProfit = netProfit
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BudgetResult())

    // --- Weather & Agricultural Indicators ---
    var selectedRegion = MutableStateFlow("Banyuwangi, Jawa Timur")
    var lastSyncTime = MutableStateFlow("Tersinkronisasi baru saja")

    val weatherState = selectedRegion.map { region ->
        // Generate agriculture-themed weather details
        val randomTemp = 30.0 + (Random().nextDouble() * 3.5)
        val randomSoilTemp = 27.5 + (Random().nextDouble() * 2.0)
        val randomHumidity = 65 + Random().nextInt(15)
        val randomUV = 5 + Random().nextInt(5)

        val uvDesc = when {
            randomUV <= 2 -> "Rendah"
            randomUV <= 5 -> "Sedang"
            randomUV <= 7 -> "Tinggi"
            else -> "Sangat Tinggi"
        }

        // Bee activity is high if UV high, morning, humidity moderate
        val beeScore = if (randomHumidity in 55..75) 9 else 6
        val insectActivity = if (randomHumidity > 80) {
            "Sedang (Lebah berteduh, Kurangi Kocor)"
        } else {
            "Sangat Aktif (Waktu Sempurna Penyerbukan Buah!)"
        }

        // Forecast listing
        val hourFormat = SimpleDateFormat("HH:00", Locale.getDefault())
        val forecastList = listOf(
            WeatherForecast(hourFormat.format(Date(System.currentTimeMillis() + 3600000)), "Cerah Berawan", String.format("%.1f°C", randomTemp - 1.0), "8/10 Lebah"),
            WeatherForecast(hourFormat.format(Date(System.currentTimeMillis() + 7200000)), "Cerah", String.format("%.1f°C", randomTemp), "9/10 Lebah"),
            WeatherForecast(hourFormat.format(Date(System.currentTimeMillis() + 10800000)), "Hujan Ringan", String.format("%.1f°C", randomTemp - 4.5), "2/10 Lebah"),
            WeatherForecast(hourFormat.format(Date(System.currentTimeMillis() + 14400000)), "Berawan", String.format("%.1f°C", randomTemp - 2.0), "5/10 Lebah"),
            WeatherForecast("Besok", "Cerah Berawan", "31°C", "8/10 Aktif")
        )

        WatermelonWeather(
            regionName = region,
            tempAir = String.format("%.1f°C", randomTemp),
            tempSoil = String.format("%.1f°C", randomSoilTemp),
            humidityStr = "$randomHumidity%",
            oxygenPercent = "20.9%",
            uvIntensity = "$randomUV ($uvDesc)",
            beeActivityScore = "$beeScore/10",
            pollinatorActivityDesc = insectActivity,
            forecast = forecastList
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WatermelonWeather())

    // --- Dosis Pupuk Tabur & Kocor calculator ---
    var totalPopulationInput = MutableStateFlow("5000") // population for local dosing
    var kocorTankVolume = MutableStateFlow("200") // 200L or 100L
    var activePhaseDaysDosing = MutableStateFlow("25") // age in days

    val dosingCalculationResult = combine(
        totalPopulationInput,
        kocorTankVolume,
        activePhaseDaysDosing
    ) { popStr, tankStr, ageStr ->
        val pop = popStr.toIntOrNull() ?: 5000
        val tankVol = tankStr.toDoubleOrNull() ?: 200.0
        val age = ageStr.toIntOrNull() ?: 25

        val phase = getPhaseDetails(age)

        // Industrial Drench calculation:
        // 1 Hectare is approx 10000 plants, and needs 200L drench drum.
        // So for Pop plants, total drench liquid needed = (Pop / 10000) * 200 Liters (or dynamic based on standard: 100-150ml solution per plant).
        val solutionPerPlantMl = when {
            age <= 10 -> 100.0
            age <= 25 -> 150.0
            else -> 200.0 // peak fruit
        }
        val totalDrenchSolutionLiters = (pop * solutionPerPlantMl) / 1000.0
        val totalDrumsRequired = ceil(totalDrenchSolutionLiters / tankVol).toInt()

        // Required ingredients for the phase (NPK 16-16-16, KNO3, Boron, CNG etc.)
        val ingredients = getDosingIngredientsForPhase(pop, age)

        // Soil (Tabur) Fertilizer per plant and total
        val taburGramsPerPlant = when {
            age <= 7 -> 0.0 // no tabur
            age <= 20 -> 10.0 // vegetative 10g per plant
            age <= 35 -> 20.0 // flowering 20g
            age <= 50 -> 30.0 // fruit sizing 30g
            else -> 15.0 // ripening 15g
        }
        val totalTaburKg = (pop * taburGramsPerPlant) / 1000.0

        val taburIngredients = getTaburIngredientsForPhase(pop, age, taburGramsPerPlant)

        DosingResult(
            phaseName = phase.phaseName,
            solutionPerPlantMl = solutionPerPlantMl,
            totalSolutionLiters = totalDrenchSolutionLiters,
            drumsNeeded = totalDrumsRequired,
            drenchIngredients = ingredients,
            taburGramsPerPlant = taburGramsPerPlant,
            totalTaburKg = totalTaburKg,
            taburIngredients = taburIngredients
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DosingResult())

    // Database Actions
    fun createPlanting(blockName: String, population: Int, notes: String) {
        viewModelScope.launch {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = simpleDateFormat.format(Date())
            val harvestCalendar = Calendar.getInstance()
            harvestCalendar.time = Date()
            harvestCalendar.add(Calendar.DAY_OF_YEAR, 75) // typical harvest at 75 days
            val harvestStr = simpleDateFormat.format(harvestCalendar.time)

            val event = PlantingEvent(
                blockName = blockName,
                plantCount = population,
                plantingDate = dateStr,
                targetHarvestDate = harvestStr,
                seedCount = population,
                rowSpacing = rowSpacingMeters,
                holeSpacing = holeSpacingMeters,
                notes = notes
            )
            repository.insertEvent(event)
        }
    }

    fun deletePlanting(event: PlantingEvent) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }

    fun addScoutingEntry(date: String, area: String, pest: String, severity: String, notes: String, tindakan: String) {
        viewModelScope.launch {
            val log = ScoutingLog(
                date = date,
                areaName = area,
                pestHamaObserved = pest,
                severityLevel = severity,
                notes = notes,
                statusTindakan = tindakan
            )
            repository.insertLog(log)
        }
    }

    fun deleteScouting(log: ScoutingLog) {
        viewModelScope.launch {
            repository.deleteLog(log)
        }
    }

    fun addCostExpense(title: String, category: String, cost: Double, qty: Double, unit: String) {
        viewModelScope.launch {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val expense = ExpenseItem(
                title = title,
                category = category,
                cost = cost,
                quantity = qty,
                unit = unit,
                date = simpleDateFormat.format(Date())
            )
            repository.insertExpense(expense)
        }
    }

    fun deleteExpense(item: ExpenseItem) {
        viewModelScope.launch {
            repository.deleteExpense(item)
        }
    }

    // --- Search Active Ingredients or Prices ---
    fun searchActiveIngredient(query: String) {
        activeIngredientSearchQuery.value = query
        if (query.isEmpty()) return
        // Do simulation / local check
        val lowerQuery = query.lowercase()
        val match = when {
            lowerQuery.contains("abamektin") || lowerQuery.contains("abamectin") -> {
                "Abamektin: Bahan aktif insektisida/akarisida kontak-lambun. Target: Thrips, Tungau (Mites), Kutu Daun. Rekomendasi produk: Demolish, Agrimec. Dosis: 0.5-1 ml/L. Rotasi: Imidakloprid (Sistemik)."
            }
            lowerQuery.contains("imidakloprid") || lowerQuery.contains("imidacloprid") -> {
                "Imidakloprid: Insektisida sistemik sistem saraf. Target: Kutu Kebul (Bemisia tabaci), Aphids, Thrips. Rekomendasi produk: Confidor, Regent. Dosis: 1-1.5 ml/L. Rotasi: Abamektin (Kontak)."
            }
            lowerQuery.contains("difenokonazol") || lowerQuery.contains("difenoconazole") -> {
                "Difenokonazol: Fungisida sistemik azol triazol (lokal sistemik). Menghambat biosintesis sterol kapang. Target: Antraknosa (Patek), Bercak Daun (Alternaria). Rekomendasi produk: Amistar Top, Score. Dosis: 1.5-2 ml/L. Rotasi: Propineb/Mankozeb."
            }
            lowerQuery.contains("mankozeb") || lowerQuery.contains("mancozeb") -> {
                "Mankozeb: Fungisida kontak berspektrum luas multi-site inhibitor. Sangat aman dan tidak gampang resisten. Target: Embun Bulu, Antraknosa, Busuk Batang. Rekomendasi produk: Dithane M-45, Yellow Antracol. Dosis: 2-3 gr/L. Rotasi: Difenokonazol."
            }
            lowerQuery.contains("trichoderma") -> {
                "Trichoderma: Bio-fungisida organik (mikroba hidup pelindung akar). Memangsa jamur patogen Fusarium oxysporum di dalam tanah. Wajib diaplikasikan sebagai pupuk dasar atau kocor awal. Jangan dicampur fungisida kimia!"
            }
            else -> "Informasi belum tersedia di database luring. Silakan tanyakan melalui fitur 'AI Asisten' dengan tombol Tanya AI."
        }
        aiDiagnosisResult.value = match
    }

    fun searchProductPrice(query: String) {
        productSearchQuery.value = query
        if (query.isEmpty()) {
            productSearchResult.value = null
            return
        }
        val lower = query.lowercase()
        val result = when {
            lower.contains("demolish") -> ProductInfo("Demolish 18EC", "Insektisida", "Abamektin 18 g/l", "Rp 85.000 - Rp 95.000 per 100ml", "Dosis: 0.5ml per liter air. Sangat ampuh membungkam Thrips keriting.")
            lower.contains("amistar") || lower.contains("amistartop") -> ProductInfo("Amistar Top 325SC", "Fungisida Sistemik", "Azoksistrobin 200 g/l + Difenokonazol 125 g/l", "Rp 150.000 - Rp 165.000 per 100ml", "Dosis: 1 ml/liter. Membuat daun semangka tebal, lebar, hijau sehat bersinar dan bebas patek.")
            lower.contains("antracol") -> ProductInfo("Antracol 70WP", "Fungisida Kontak", "Propineb 70%", "Rp 75.000 - Rp 85.000 per 500gr", "Dosis: 2 gram/liter. Dilengkapi zinc untuk menstimulasi daun muda agar cepat tumbuh kokoh.")
            lower.contains("curacron") -> ProductInfo("Curacron 500EC", "Insektisida Kontak", "Profenofos 500 g/l", "Rp 70.000 - Rp 80.000 per 100ml", "Dosis: 1.5 - 2 ml/liter. Memiliki aroma kuat penyengat lalat buah agar tidak mau menyengat.")
            lower.contains("dolomit") -> ProductInfo("Dolomit Super", "Kapur Pertanian", "Kalsium (CaO) 30% + Magnesium (MgO) 20%", "Rp 35.000 - Rp 50.000 per karung 50kg", "Pupuk penstabil pH tanah asam, mencegah kerontokan bakal buah.")
            lower.contains("sistemik") -> ProductInfo("Mankozeb / Ridomil", "Fungisida Kombinasi", "Mankozeb + Metolaksil", "Rp 110.000 - Rp 130.000 per 500g", "Dosis kocor pencegahan rebah kecambah.")
            else -> ProductInfo(query, "Pestisida Umum", "Bahan Aktif Lokal", "Rp 50.000 - Rp 120.000", "Rentang harga rata-rata berdasarkan survei penyurvei kios tani lokal.")
        }
        productSearchResult.value = result
    }

    // --- Execute Diagnosis via Gemini or Offline Fallback ---
    fun runDiagnosis(customDesc: String? = null) {
        val queryText = customDesc ?: symptomsInput.value
        if (queryText.isEmpty() && selectedPhotoPlaceholder.value == null) return

        isDiagnosisLoading.value = true
        aiDiagnosisResult.value = null

        // Prep description prompt
        val inputPrompt = buildString {
            append("Identifikasi gejala tanaman semangka berikut dan berikan panduan komprehensif:\n")
            if (selectedPhotoPlaceholder.value != null) {
                append("[TIPE ANALISIS: UNGGAH FOTO GEJALA - SIMULASI ATAU REAL]\n")
                append("Gejala Foto: Terdeteksi masalah visual tipe: ${selectedPhotoPlaceholder.value?.uppercase()}\n")
            }
            append("Deskripsi tulisan pengguna: $queryText\n\n")
            append("WAJIB sertakan rincian sbb:\n")
            append("1. Nama serangan hama/penyakit serta penyebab teknisnya.\n")
            append("2. Saran produk pasaran Indonesia dan Bahan Aktifnya.\n")
            append("3. Rotasi bahan aktif agar tidak resisten.\n")
            append("4. Dosis per 16 L air (1 tangki).\n")
            append("5. Panduan interval penyemprotan (cerah: 5 hari sekali, hujan: 3-4 hari sekali).\n")
            append("6. Saran darurat jika terjadi ledakan populasi hama atau ledakan penyebaran patogen.\n")
            append("7. Tawarkan draf jadwal penyemprotan insektisida/fungisida preventif.")
        }

        viewModelScope.launch {
            val responseText = GeminiApiClient.analyzeSymptoms(inputPrompt, null)

            if (responseText == "OF_LINE_FALLBACK") {
                // Perform smart matching offline fallback
                val matchedFallback = lookupOfflineDiagnosis(queryText, selectedPhotoPlaceholder.value)
                aiDiagnosisResult.value = matchedFallback
            } else {
                aiDiagnosisResult.value = responseText
            }

            // Create spraying schedule based on diagnosis
            generateSprayingSchedule(selectedPhotoPlaceholder.value ?: "Pencegahan Umum")
            isDiagnosisLoading.value = false
        }
    }

    private fun generateSprayingSchedule(diagnosisType: String) {
        val days = mutableListOf<SprayingDay>()
        when (diagnosisType.lowercase()) {
            "antraknosa", "patek" -> {
                days.add(SprayingDay("Hari 1 (Sistemik)", "Azoksistrobin + Difenokonazol (Amistar Top)", "16 ml / 16L", "Penyemprotan merata fokus ke daun bawah dan buah muda."))
                days.add(SprayingDay("Hari 4 (Kontak)", "Mankozeb (Dithane M-45)", "32 gram / 16L", "Solfat pelindung kontak luar, mencegah spora menyebar pasca hujan."))
                days.add(SprayingDay("Hari 8 (Sistemik)", "Propamocarb HCl (Infinito)", "30 ml / 16L", "Fisika jaringan tanaman dibebaskan."))
                days.add(SprayingDay("Hari 12 (Kontak)", "Tembaga Hidroksida (Copcide)", "20 gram / 16L", "Sanitasi tuntas daun terinfeksi."))
            }
            "layu_fusarium", "fusarium" -> {
                days.add(SprayingDay("Hari 1 (Kocor Root)", "Karbendazim (Derosal)", "30 ml / tangki", "Siram 200ml per lubang tanam di sekeliling area perakaran."))
                days.add(SprayingDay("Hari 3", "Benomil kocor di perbatasan lubang terdekat", "25 gram / tangki", "Membatasi perluasan mikoriza patogen fusarium."))
                days.add(SprayingDay("Hari 7 (Biologis)", "Trichoderma Sp (Agens Hayati)", "100 gram / drum 100L", "Kocor perakaran agar jamur pelindung merebut teritori."))
            }
            "thrips", "thrip", "daun_keriting" -> {
                days.add(SprayingDay("Hari 1", "Abamektin (Demolish 18EC)", "10 ml / 16L", "Semprot sore hari pukul 16:30, ratakan ke bawah permukaan daun."))
                days.add(SprayingDay("Hari 5", "Imidakloprid (Confidor 200SL)", "15 ml / 16L", "Pemberantasan sistemik tuntas koloni kutu kebul/kebul."))
                days.add(SprayingDay("Hari 10", "Fipronil (Regent 50SC)", "12 ml / 16L", "Mematikan larva thrips di pori-pori tanah dumper."))
            }
            "lalat_buah", "belatung" -> {
                days.add(SprayingDay("Hari 1", "Profenofos (Curacron)", "24 ml / 16L", "Aroma tajam mengusir lalat dewasa menaruh telur."))
                days.add(SprayingDay("Hari 5", "Deltametrin (Decis)", "15 ml / 16L", "Efek knockdown lalat pengganggu yang terbang."))
                days.add(SprayingDay("Hari 10", "Pasang Botol Atraktan Petrogenol", "Aplikasi Fisik", "Pasang 4 titik perangkap di luar gundukan bedengan luar."))
            }
            else -> {
                days.add(SprayingDay("Hari 1 (Pencegahan)", "Mankozeb Kontak (Dithane M-45)", "30 gram / 16L", "Semprot pelindung seluruh bagian bodi pohon pencegah embun bulu."))
                days.add(SprayingDay("Hari 6 (Pencegahan)", "Abamektin Kontak (Demolish)", "8 ml / 16L", "Penjagaan berkala daun keriting akibat serangan Thrips."))
            }
        }
        sprayingScheduleList.value = days
    }

    private fun lookupOfflineDiagnosis(desc: String, photoType: String?): String {
        val q = desc.lowercase()
        val p = photoType?.lowercase()

        return when {
            p == "layu_fusarium" || q.contains("layu") || q.contains("fusarium") || q.contains("tiba-tiba mati") -> {
                """
                🔍 **[HASIL DIAGNOSIS LURING: LAYU FUSARIUM (Fusarium Wilt)]**
                
                **Gejala Mekanis:**
                Batang bergaris coklat kemerahan, layu mendadak di terik matahari siang, segar kembali pada pagi/malam hari. Dalam hitungan hari tanaman kering permanen dan mati busuk kusam.
                
                **1. Produk Pasaran & Bahan Aktif:**
                - Benomil (Produk: Benlate 50WP)
                - Karbendazim (Produk: Derosal 500SC)
                - Produk Hayati: Trichoderma (Produk: Tricogreen, Ansar)
                
                **2. Rotasi Bahan Aktif:**
                Rotasi antara Benomil (Fungisida sistemik golongan Benzimidazol) dengan Difenokonazol+Azoksistrobin (golongan Triazol+Strobilurin) untuk mencegah resistensi jamur tanah.
                
                **3. Dosis Per 16 L Air (Tangki Standar):**
                - Benomil: 24 gram per tangki 16 Liter.
                - Karbendazim: 20 ml per tangki 16 Liter.
                
                **4. Interval Penyemprotan / Kocor:**
                - **Cerah:** 5 hari sekali dikocorkan di area pangkal perakaran.
                - **Hujan:** 3-4 hari sekali jika curah hujan naik drastis (pencegahan penyebaran spora lewat aliran air bedengan).
                
                **5. Saran Darurat (Populasi Meluas / Meledak):**
                - Cabut dan isolasi tanaman layu SEGERA. JANGAN dilempar sembarangan! Bakar jauh dari kebun semangka.
                - Masukkan pupuk dolomit kering sebanyak 2 genggam dan kocor Benomil murni dosis ganda pada tanah bekas tanaman yang dicabut, agar spora jamur tidak berjalan menyusuri selang drip ke tanaman tetangga.
                - Hentikan pemakaian Urea kocor sementara. Kelebihan unsur N memicu jamur berkembang 3x lipat lebih agresif.
                """.trimIndent()
            }
            p == "antraknosa" || q.contains("antraknosa") || q.contains("patek") || q.contains("bercak coklat") || q.contains("busuk buah") -> {
                """
                🔍 **[HASIL DIAGNOSIS LURING: ANTRAKNOSA / PATEK (Anthracnose)]**
                
                **Gejala Mekanis:**
                Bercak lingkaran konsentris gelap berlapis pada buah semangka yang lama-kelamaan cekung membusuk seolah terbakar bara api. Menular sangat cepat di daun tua menjadi kering berlubang.
                
                **1. Produk Pasaran & Bahan Aktif:**
                - Amistar Top 325SC (Bahan Aktif: Azoksistrobin 200g/l + Difenokonazol 125g/l)
                - Antracol 70WP (Bahan Aktif: Propineb 70%)
                - Cabrio Top 60WG (Bahan Aktif: Piraklostrobin 5% + Metiram 55%)
                
                **2. Rotasi Bahan Aktif:**
                Rotasi mingguan antara Amistar Top (Sistemik, bekerja di dalam jaringan tanaman) dengan Antracol (Kontak, melindungi permukaan buah dari spora yang mendarat).
                
                **3. Dosis Per 16 L Air (Tangki Standar):**
                - Amistar Top: 16 - 20 ml per tangki 16 Liter.
                - Antracol: 32 - 40 gram per tangki 16 Liter (sekitar 3 sendok makan penuh).
                
                **4. Interval Penyemprotan:**
                - **Cerah:** 5 hari sekali rimbun menyeluruh.
                - **Hujan:** 3 hari sekali. Air hujan bersifat asam dan menjadi media transportasi utama spora Antraknosa terbang menempel ke buah lain.
                
                **5. Saran Darurat (Populasi Meluas / Meledak):**
                - Potong tangkai buah/daun yang mulai patek, bungkus kantong plastik dan kubur dalam tanah sedalam minimal 50 cm.
                - Gunakan kombinasi fungisida Amistar Top dicampur fungisida kontak Tembaga Hidroksida (Kocide/Copcide) interval 2 hari sekali sebanyak 3 putaran penyemprotan berturut-turut untuk membekukan patek aktif.
                """.trimIndent()
            }
            p == "thrips" || q.contains("thrip") || q.contains("keriting") || q.contains("bawah daun mengkilap") || q.contains("daun melinting") -> {
                """
                🔍 **[HASIL DIAGNOSIS LURING: HAMA THRIPS (Kutu Gurem)]**
                
                **Gejala Mekanis:**
                Tunas ujung pohon kerdil melingkar tidak ingin merambat maju. Sisi bawah daun berwarna kilap keperakan keemasan dan mengkerut kaku melipat ke atas. Menjadi pembawa utama virus gemini (kuning daun kerdil).
                
                **1. Produk Pasaran & Bahan Aktif:**
                - Demolish 18EC / Agrimec (Bahan Aktif: Abamektin 18g/l)
                - Confidor 200SL (Bahan Aktif: Imidakloprid 200g/l)
                - Regent 50SC (Bahan Aktif: Fipronil 50g/l)
                
                **2. Rotasi Bahan Aktif:**
                Rotasi antara Abamektin (Kontak, menusuk hama thrips di balik daun) dengan Imidakloprid (Sistemik, racun mengalir lewat pembuluh tanaman).
                
                **3. Dosis Per 16 L Air (Tangki Standar):**
                - Demolish / Abamektin: 8 - 12 ml per tangki 16 Liter.
                - Confidor: 10 - 15 ml per tangki 16 Liter.
                
                **4. Interval Penyemprotan:**
                - **Cerah:** 5 hari sekali di sore hari.
                - **Hujan:** 4 hari sekali jika ada jeda reda mendung.
                
                **5. Saran Darurat (Ledakan Populasi):**
                - Semprot MUTLAK wajib dilakukan sore menjelang malam hari (pukul 16:30 - 18:30) karena Thrips aktif merangkak keluar sembunyi di malam hari.
                - Tambahkan produk Perekat-Perata-Penembus pestisida (Spreader / Surfaktan) dosis 5-10 ml per tangki air agar racun mampu menembus bulu halus daun yang berkerut dan melarutkan tameng pelindung tubuh thrips.
                """.trimIndent()
            }
            p == "lalat_buah" || q.contains("lalat") || q.contains("buah busuk ada belatung") || q.contains("titik hitam pada buah") -> {
                """
                🔍 **[HASIL DIAGNOSIS LURING: LALAT BUAH (Bactrocera Carambolae)]**
                
                **Gejala Mekanis:**
                Ada bercak suntikan titik hitam kecil di kulit buah semangka muda. Lambat laun wilayah sekitar suntikan melunak gembur berair, membusuk kusam, dan bila dibelah berisi ratusan belatung lalat kecil merusak daging semangka.
                
                **1. Produk Pasaran & Bahan Aktif:**
                - Curacron 500EC (Bahan Aktif: Profenofos 500g/l)
                - Decis 25EC (Bahan Aktif: Deltametrin 25g/l)
                - Atraktan Alami: Petrogenol (Bahan Aktif: Metil Eugenol)
                
                **2. Rotasi Bahan Aktif:**
                Rotasi mingguan antara Profenofos (Organofosfat sistem pengusir uap bau) dengan Deltametrin (kontak knock-down cap kilat).
                
                **3. Dosis Per 16 L Air:**
                - Curacron: 24 - 32 ml per tangki 16 Liter (Aroma menyengat mengusir lalat betina hinggap).
                - Decis: 15 ml per tangki 16 Liter.
                
                **4. Interval Penyemprotan:**
                - **Cerah:** 5 hari sekali rimbun menyasar buah semangka terlentang.
                - **Hujan:** 3-4 hari sekali.
                
                **5. Saran Darurat (Populasi Meluas / Meledak):**
                - Pasang perangkap umpan perekat Metil Eugenol di batas keliling terluar kebun semangka. JANGAN taruh perangkap eugenol di tengah-tengah kebun semangka karena akan memancing kawanan lalat buah liar justru mendarat dan hinggap merusak buah semangka kita.
                - Bungkus semangka muda terpilih yang berukuran bola tenis menggunakan kertas pelindung buah atau daun kering jika populasi benar-benar tak terkendali.
                """.trimIndent()
            }
            else -> {
                """
                🔍 **[HASIL DIAGNOSIS LURING: ANALISIS GEJALA UMUM & PREVENTIF]**
                
                Gejala yang dituliskan menunjukkan gangguan pertumbuhan ringan atau ancaman spora musiman patogen tanaman semangka.
                
                **Pencegahan Penyakit Jamur (Embun Bulu/Busuk Daun):**
                - Gunakan Fungisida Kontak berbahan aktif **Mankozeb** (Produk: Dithane M-45) dosis 30-40 gram per tangki 16 Liter, semprot berkala seminggu sekali.
                
                **Pencegahan Kutu & Tungau:**
                - Gunakan Insektisida Kontak berbahan aktif **Abamektin** (Produk: Demolish) dosis 8 ml per tangki 16 L air di sore hari.
                
                **Panduan Interval:**
                - **Cuaca Cerah:** Semprot pencegahan rutin setiap 5 hari sekali.
                - **Cuaca Hujan Berawan:** Semprot pencegahan setiap 3-4 hari sekali karena jamur menyukai kondisi lembyab basah berlumut.
                
                *Tips Tambahan:* Lakukan penjarangan daun semangka bawah yang terlalu rimbun agar sirkulasi udara di bawah kanopi daun lancar dan permukaan tanah mengering diterobos sinar matahari.
                """.trimIndent()
            }
        }
    }
}

// --- Auxiliary Classes ---

data class BaseFertilizerResult(
    val koheBags: Double = 0.0,
    val koheTons: Double = 0.0,
    val ureaKg: Double = 0.0,
    val phosphateKg: Double = 0.0,
    val kclKg: Double = 0.0,
    val dolomiteKg: Double = 0.0
)

data class PlantPhaseDetail(
    val phaseName: String,
    val rangeHst: String,
    val description: String,
    val checklistFokus: String,
    val nutrientNeeds: String
)

data class BaseFertilizerManualInput(
    val label: String,
    val actualNeedsUnit: String,
    val textExplanation: String
)

data class ProductInfo(
    val name: String,
    val category: String,
    val activeIngredient: String,
    val priceEstimate: String,
    val tips: String
)

data class SprayingDay(
    val dayLabel: String,
    val pesticideName: String,
    val dosePer16L: String,
    val instruction: String
)

data class BudgetResult(
    val seedCost: Double = 0.0,
    val organicFertCost: Double = 0.0,
    val chemicalFertCost: Double = 0.0,
    val dolomiteCost: Double = 0.0,
    val pesticideCost: Double = 0.0,
    val otherCost: Double = 0.0,
    val totalModal: Double = 0.0,
    val tonTotalYield: Double = 0.0,
    val kgGradeA: Double = 0.0,
    val kgGradeB: Double = 0.0,
    val revenueGradeA: Double = 0.0,
    val revenueGradeB: Double = 0.0,
    val grossRevenue: Double = 0.0,
    val netProfit: Double = 0.0
)

data class WeatherForecast(
    val timeLabel: String,
    val condition: String,
    val temp: String,
    val pollinatorScore: String
)

data class WatermelonWeather(
    val regionName: String = "",
    val tempAir: String = "30°C",
    val tempSoil: String = "28°C",
    val humidityStr: String = "75%",
    val oxygenPercent: String = "20.9%",
    val uvIntensity: String = "6 (Tinggi)",
    val beeActivityScore: String = "8/10",
    val pollinatorActivityDesc: String = "Sangat Aktif",
    val forecast: List<WeatherForecast> = emptyList()
)

data class DosingResult(
    val phaseName: String = "",
    val solutionPerPlantMl: Double = 150.0,
    val totalSolutionLiters: Double = 0.0,
    val drumsNeeded: Int = 0,
    val drenchIngredients: List<DosingIngredient> = emptyList(),
    val taburGramsPerPlant: Double = 0.0,
    val totalTaburKg: Double = 0.0,
    val taburIngredients: List<DosingIngredient> = emptyList()
)

data class DosingIngredient(
    val name: String,
    val dosePerPlant: String,
    val totalVolumeKg: String,
    val customInstructions: String
)

// Helper methods for growth phases
private fun getPhaseDetails(age: Int): PlantPhaseDetail {
    return when {
        age <= 7 -> PlantPhaseDetail(
            phaseName = "Fase Perkecambahan (Germination)",
            rangeHst = "0 - 7 HST",
            description = "Biji semangka mulai pecah, muncul akar tunggang, keping daun terbuka mengembang ke permukaan mulsa.",
            checklistFokus = "Fokus kelembaban media semai, proteksi ulat tanah daun muda, pastikan drainase bedengan lancar.",
            nutrientNeeds = "Utamakan nutrisi Fosfat tinggi (P) kocor dosis sangat rendah untuk memicu perkembangan panjang akar tunas."
        )
        age <= 20 -> PlantPhaseDetail(
            phaseName = "Fase Vegetatif Awal (Early Growth)",
            rangeHst = "8 - 20 HST",
            description = "Pohon semangka mulai menjulurkan ranting lateral (sulur cabang), pertambahan helaian daun sangat progresif.",
            checklistFokus = "Lakukan penempatan sulur lurus memanjang ke arah tumpuan mulsa, scouting Thrips bodi rimbun daun muda.",
            nutrientNeeds = "Nitrogen tinggi (N) dibarengi kalsium (CN-G) agar cabang lateral tebal elastis tidak pecah tersenggol angin."
        )
        age <= 35 -> PlantPhaseDetail(
            phaseName = "Fase Pembungaan (Flowering/Generatif Awal)",
            rangeHst = "21 - 35 HST",
            description = "Muncul bunga jantan dan betina di sulur ke-13 ke atas. Lebah penyerbuk mulai berdatangan di pagi hari.",
            checklistFokus = "SANGAT KRITIS: Bantu penyerbukan manual bunga betina (kawinkan) pukul 06:00 - 09:30 pagi jika populasi lebah minim.",
            nutrientNeeds = "Hentikan pemberian pupuk nitrogen tinggi. Naikkan Kalium & Boron (Meroke Karateplus Boroni) mencegah bunga rontok."
        )
        age <= 50 -> PlantPhaseDetail(
            phaseName = "Fase Pembentukan & Pembesaran Buah (Fruit Sizing)",
            rangeHst = "36 - 50 HST",
            description = "Penyeleksian buah semangka terpilih (pertahankan 1-2 buah per tanaman), pembesaran bodi buah sangat cepat.",
            checklistFokus = "Lakukan rotasi balik buah semangka agar tidak busuk sebelah, pasang alas bilah bambu/jerami kering di bawah leher buah.",
            nutrientNeeds = "Kalsium tinggi mencegah buah pecah (cracking) + NPK 16-16-16 dosis kocor puncak, serta kalium tinggi (KNO3 Putih) memicu bobot."
        )
        age <= 70 -> PlantPhaseDetail(
            phaseName = "Fase Pematangan Buah (Fruit Ripening)",
            rangeHst = "51 - 70 HST",
            description = "Proses penimbunan kadar gula (brik kemanisan) pada buah semangka. Sulur tangkai buah mulai mengering kecoklatan.",
            checklistFokus = "Kurangi pengairan air total 1 minggu sebelum panen agar rasa buah manis madu tidak hambar/hanya berair saja.",
            nutrientNeeds = "Fokus pupuk Kalium murni dosis sedang (Provit Maxi / KNO3 Putih) + Silika bubuk untuk mempertebal kekerasan kulit buah."
        )
        else -> PlantPhaseDetail(
            phaseName = "Fase Panen Raya Watermelon",
            rangeHst = "71+ HST",
            description = "Buah semangka siap dipotong dari sulur, bobot panen siap diangkut armada truk pembeli.",
            checklistFokus = "Potong buah semangka dengan menyisakan tangkai berbentuk huruf T sepanjang 3 cm agar buah awat disimpan.",
            nutrientNeeds = "Tidak ada pemupukan aktif lagi. Biarkan tanaman beristirahat siap dibalik tanah gundukan kembali."
        )
    }
}

// Generate drenching ingredients depending on total population and age/phase
private fun getDosingIngredientsForPhase(population: Int, age: Int): List<DosingIngredient> {
    val list = mutableListOf<DosingIngredient>()
    when {
        age <= 7 -> {
            list.add(DosingIngredient("Fosfat Cair (DAP Starter)", "1.5 gram/plant", String.format("%.2f kg", (population * 1.5) / 1000.0), "Kocorkan tipis di sekeliling polybag semai sebelum pindah tanam bedengan."))
            list.add(DosingIngredient("Trichoderma Sp (Agens Hayati)", "2 gram/plant", String.format("%.2f kg", (population * 2.0) / 1000.0), "Sebagai benteng pertahanan alami akar dari serangan rebah kecambah."))
        }
        age <= 20 -> {
            list.add(DosingIngredient("NPK 16-16-16 (Mutiara/Yara)", "3 gram/plant", String.format("%.2f kg", (population * 3.0) / 1000.0), "Merangsang asimilasi fotosintesis daun sulur lateral cepat memanjang."))
            list.add(DosingIngredient("CN-G (Kalsium Nitrat)", "1 gram/plant", String.format("%.2f kg", (population * 1.0) / 1000.0), "Menjaga sel dinding batang pohon elastis kokoh."))
        }
        age <= 35 -> {
            list.add(DosingIngredient("NPK 16-16-16", "4 gram/plant", String.format("%.2f kg", (population * 4.0) / 1000.0), "Nutrisi harian berimbang penopang sulur cabang utama tetap produktif."))
            list.add(DosingIngredient("KNO3 Merah (Kalium Nitrat Awal)", "2 gram/plant", String.format("%.2f kg", (population * 2.0) / 1000.0), "Menghantarkan energi pembungaan prima tanpa rontok dini."))
            list.add(DosingIngredient("Meroke Karateplus Boroni", "0.5 gram/plant", String.format("%.2f kg", (population * 0.5) / 1000.0), "Kandungan boron memperkuat dinding bakal buah agar melekat erat."))
        }
        age <= 50 -> {
            list.add(DosingIngredient("NPK 16-16-16", "6 gram/plant", String.format("%.2f kg", (population * 6.0) / 1000.0), "Dosis puncak pembesaran ukuran daging buah semangka."))
            list.add(DosingIngredient("KNO3 Putih (Kalium Nitrat Tinggi)", "4 gram/plant", String.format("%.2f kg", (population * 4.0) / 1000.0), "Memacu transfer karbohidrat menuju buah sehingga bobot padat."))
            list.add(DosingIngredient("Calsium Fertilizer", "1.5 gram/plant", String.format("%.2f kg", (population * 1.5) / 1000.0), "Mutlak diperlukan agar kulit buah padat tidak pecah mengembung."))
        }
        else -> {
            list.add(DosingIngredient("KNO3 Putih (Kalium Nitrat Tinggi)", "3 gram/plant", String.format("%.2f kg", (population * 3.0) / 1000.0), "Memicu kemayuan brik manis daging buah semangka merah kuning."))
            list.add(DosingIngredient("Silika Bubuk (Silite)", "1 gram/plant", String.format("%.2f kg", (population * 1.0) / 1000.0), "Mempertebal kulit buah melindunginya dari gempuran lalat buah pasca panen."))
            list.add(DosingIngredient("Provit Maxi", "1 gram/plant", String.format("%.2f kg", (population * 1.0) / 1000.0), "Nutrisi fosfat kalium instan lewat air."))
        }
    }
    return list
}

private fun getTaburIngredientsForPhase(population: Int, age: Int, gramsPerPlant: Double): List<DosingIngredient> {
    val list = mutableListOf<DosingIngredient>()
    if (gramsPerPlant <= 0.0) return list

    when {
        age <= 20 -> {
            list.add(DosingIngredient("NPK 16-16-16", "7 gram/plant", String.format("%.2f kg", (population * 7.0) / 1000.0), "Ditabur melingkar sejauh 15cm dari pangkal tanaman lalu ditutup tanah."))
            list.add(DosingIngredient("KNO3 Merah", "3 gram/plant", String.format("%.2f kg", (population * 3.0) / 1000.0), "Membantu meluaskan laju cabang sulur muda."))
        }
        age <= 35 -> {
            list.add(DosingIngredient("NPK 16-16-16", "12 gram/plant", String.format("%.2f kg", (population * 12.0) / 1000.0), "Tabur di tengah bedengan tumpukan tengah."))
            list.add(DosingIngredient("Meroke Karateplus Boroni", "5 gram/plant", String.format("%.2f kg", (population * 5.0) / 1000.0), "Memberi asupan boron dan kalsium pembawa stamina pembungaan."))
            list.add(DosingIngredient("CN-G (Kalsium)", "3 gram/plant", String.format("%.2f kg", (population * 3.0) / 1000.0), "Pondasi kekuatan ranting menjalar."))
        }
        else -> {
            list.add(DosingIngredient("NPK 16-16-16", "15 gram/plant", String.format("%.2f kg", (population * 15.0) / 1000.0), "Tabur di lubang samping terjauh perakaran (jarak 30cm)."))
            list.add(DosingIngredient("KNO3 Putih", "10 gram/plant", String.format("%.2f kg", (population * 10.0) / 1000.0), "Mendongkrak kadar gula dan berat timbangan buah."))
            list.add(DosingIngredient("Silika Bubuk", "5 gram/plant", String.format("%.2f kg", (population * 5.0) / 1000.0), "Ketahanan luar bodi pohon rimbun panen."))
        }
    }
    return list
}
