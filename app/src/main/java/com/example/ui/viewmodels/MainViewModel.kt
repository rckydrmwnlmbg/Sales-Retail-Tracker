package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ActivityEntity
import com.example.data.local.entity.ColleagueEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

import android.content.SharedPreferences

class MainViewModel(private val repository: AppRepository, private val prefs: SharedPreferences) : ViewModel() {

    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allColleagues: StateFlow<List<ColleagueEntity>> = repository.allColleagues
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allActivities: StateFlow<List<ActivityEntity>> = repository.allActivities
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        
    val allGoals: StateFlow<List<GoalEntity>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _isClockedIn = MutableStateFlow(false)
    val isClockedIn: StateFlow<Boolean> = _isClockedIn.asStateFlow()

    private val _clockInHour = MutableStateFlow<Int?>(null)
    val clockInHour: StateFlow<Int?> = _clockInHour.asStateFlow()
    
    private val _userName = MutableStateFlow(prefs.getString("USER_NAME", "Ricky") ?: "Ricky")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _jobTitle = MutableStateFlow(prefs.getString("JOB_TITLE", "Sales Associate") ?: "Sales Associate")
    val jobTitle: StateFlow<String> = _jobTitle.asStateFlow()

    private val _workLocation = MutableStateFlow(prefs.getString("WORK_LOCATION", "Tunjungan Plaza") ?: "Tunjungan Plaza")
    val workLocation: StateFlow<String> = _workLocation.asStateFlow()

    private val _shiftPagiTime = MutableStateFlow(prefs.getString("SHIFT_PAGI", "07.30 - 17.00") ?: "07.30 - 17.00")
    val shiftPagiTime: StateFlow<String> = _shiftPagiTime.asStateFlow()

    private val _shiftSiangTime = MutableStateFlow(prefs.getString("SHIFT_SIANG", "14.00 - 22.00") ?: "14.00 - 22.00")
    val shiftSiangTime: StateFlow<String> = _shiftSiangTime.asStateFlow()

    fun updateProfile(name: String, job: String, location: String, shiftPagi: String, shiftSiang: String) {
        _userName.value = name
        _jobTitle.value = job
        _workLocation.value = location
        _shiftPagiTime.value = shiftPagi
        _shiftSiangTime.value = shiftSiang
        prefs.edit()
            .putString("USER_NAME", name)
            .putString("JOB_TITLE", job)
            .putString("WORK_LOCATION", location)
            .putString("SHIFT_PAGI", shiftPagi)
            .putString("SHIFT_SIANG", shiftSiang)
            .apply()
    }

    private val _openRouterApiKey = MutableStateFlow(prefs.getString("OPENROUTER_API_KEY", "") ?: "")
    val openRouterApiKey: StateFlow<String> = _openRouterApiKey.asStateFlow()

    fun updateOpenRouterApiKey(key: String) {
        _openRouterApiKey.value = key
        prefs.edit().putString("OPENROUTER_API_KEY", key).apply()
    }

    private val _supabaseUrl = MutableStateFlow(prefs.getString("SUPABASE_URL", "") ?: "")
    val supabaseUrl: StateFlow<String> = _supabaseUrl.asStateFlow()

    private val _supabaseKey = MutableStateFlow(prefs.getString("SUPABASE_KEY", "") ?: "")
    val supabaseKey: StateFlow<String> = _supabaseKey.asStateFlow()

    fun updateSupabaseCredentials(url: String, key: String) {
        _supabaseUrl.value = url
        _supabaseKey.value = key
        prefs.edit().putString("SUPABASE_URL", url).putString("SUPABASE_KEY", key).apply()
    }

    private val _firstClockInTime = MutableStateFlow<String?>(prefs.getString("FIRST_CLOCK_IN", "Belum ada"))
    val firstClockInTime: StateFlow<String?> = _firstClockInTime.asStateFlow()

    fun clockIn(shiftName: String = "", shiftTime: String = "") {
        _isClockedIn.value = true
        val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        _clockInHour.value = currentHour
        if (_firstClockInTime.value == "Belum ada") {
            try {
                val formatter = SimpleDateFormat("dd MMMM yyyy", java.util.Locale("id", "ID"))
                val timeStr = formatter.format(Date())
                _firstClockInTime.value = timeStr
                prefs.edit().putString("FIRST_CLOCK_IN", timeStr).apply()
            } catch (e: Throwable) {
                // Ignore
            }
        }
        
        // Save to DB
        viewModelScope.launch {
            repository.insertActivity(
                ActivityEntity(
                    type = "CLOCK_IN",
                    notes = if (shiftName.isNotEmpty()) "$shiftName ($shiftTime)" else "Clock In - Hour $currentHour"
                )
            )
        }
    }

    fun clockOut() {
        _isClockedIn.value = false
        _clockInHour.value = null
        
        // Save to DB
        viewModelScope.launch {
            repository.insertActivity(
                ActivityEntity(
                    type = "CLOCK_OUT",
                    notes = "Clock Out"
                )
            )
        }
    }

    private val currentMonthYear = SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault()).format(Date())
    
    val currentGoal: StateFlow<GoalEntity?> = repository.getGoalByMonth(currentMonthYear)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    // Derived states for Home Screen
    val personalRevenue: StateFlow<Double> = allActivities.map { activities ->
        activities.filter { it.type == "SALE" && it.creditedToId == null }.sumOf { it.finalPrice ?: it.price ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val todayPersonalRevenue: StateFlow<Double> = allActivities.map { activities ->
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        
        activities.filter { 
            it.type == "SALE" && it.creditedToId == null && it.timestamp >= startOfDay 
        }.sumOf { it.finalPrice ?: it.price ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val personalTransactions: StateFlow<Int> = allActivities.map { activities ->
        activities.count { it.type == "SALE" && it.creditedToId == null }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val personalTargetProgress: StateFlow<Float> = combine(personalRevenue, currentGoal) { rev, goal ->
        val target = goal?.personalTarget ?: 100000000.0 // Default 100jt
        if (target > 0) (rev / target).toFloat() else 0f
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0f)
    
    val storeRevenue: StateFlow<Double> = allActivities.map { activities ->
        activities.filter { it.type == "SALE" }.sumOf { it.finalPrice ?: it.price ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)
    
    val storeTargetProgress: StateFlow<Float> = combine(storeRevenue, currentGoal) { rev, goal ->
        val target = goal?.shopTarget ?: 500000000.0 // Default 500jt
        if (target > 0) (rev / target).toFloat() else 0f
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0f)


    // App actions
    fun addActivity(activity: ActivityEntity) {
        viewModelScope.launch {
            repository.insertActivity(activity)
        }
    }

    fun deleteActivity(activity: ActivityEntity) {
        viewModelScope.launch {
            repository.deleteActivity(activity)
        }
    }

    fun addOrUpdateProduct(product: ProductEntity, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            val existing = allProducts.value.find { it.code == product.code && it.id != product.id }
            if (existing != null) {
                onError("Produk sudah ada (kode: ${product.code})")
                return@launch
            }
            repository.insertProduct(product)
        }
    }
    
    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.insertProduct(product.copy(isActive = !product.isActive))
        }
    }
    
    fun restoreDataFromCloud(supabaseUrl: String, supabaseKey: String, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                com.example.logic.SupabaseSyncHelper.restoreDataFromCloud(supabaseUrl, supabaseKey, repository)
                onComplete(true, null)
            } catch (e: Exception) {
                onComplete(false, e.message)
            }
        }
    }

    fun addOrUpdateColleague(colleague: ColleagueEntity) {
        viewModelScope.launch {
            repository.insertColleague(colleague)
        }
    }

    fun deleteColleague(colleague: ColleagueEntity) {
        viewModelScope.launch {
            repository.insertColleague(colleague.copy(isActive = !colleague.isActive))
        }
    }

    fun updateShopTarget(target: Double) {
        viewModelScope.launch {
            val regularGroupTarget = target * 0.60
            val personalTarget = regularGroupTarget * 0.50
            val goal = GoalEntity(
                monthYear = currentMonthYear,
                shopTarget = target,
                groupTarget = regularGroupTarget,
                personalTarget = personalTarget
            )
            repository.insertGoal(goal)
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    private val _coachRecommendations = MutableStateFlow<com.example.logic.CoachRecommendations?>(null)
    val coachRecommendations: StateFlow<com.example.logic.CoachRecommendations?> = _coachRecommendations.asStateFlow()

    fun updateCoachRecommendations(rec: com.example.logic.CoachRecommendations) {
        _coachRecommendations.value = rec
    }
}

class MainViewModelFactory(private val repository: AppRepository, private val prefs: SharedPreferences) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, prefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
