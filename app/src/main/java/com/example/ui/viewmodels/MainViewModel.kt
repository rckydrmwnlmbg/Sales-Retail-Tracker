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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainViewModel(private val repository: AppRepository) : ViewModel() {

    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allColleagues: StateFlow<List<ColleagueEntity>> = repository.allColleagues
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allActivities: StateFlow<List<ActivityEntity>> = repository.allActivities
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _themeMode = MutableStateFlow(0) // 0: System, 1: Dark, 2: Light
    val themeMode: StateFlow<Int> = _themeMode.asStateFlow()

    fun setThemeMode(mode: Int) {
        _themeMode.value = mode
    }

    private val _isClockedIn = MutableStateFlow(false)
    val isClockedIn: StateFlow<Boolean> = _isClockedIn.asStateFlow()

    private val _clockInHour = MutableStateFlow<Int?>(null)
    val clockInHour: StateFlow<Int?> = _clockInHour.asStateFlow()

    private val _firstClockInTime = MutableStateFlow<String?>("Belum ada")
    val firstClockInTime: StateFlow<String?> = _firstClockInTime.asStateFlow()

    fun clockIn() {
        _isClockedIn.value = true
        _clockInHour.value = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        if (_firstClockInTime.value == "Belum ada") {
            try {
                val formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy", java.util.Locale("id", "ID"))
                _firstClockInTime.value = java.time.LocalDate.now().format(formatter)
            } catch (e: Throwable) {
                _firstClockInTime.value = "14 Juni 2026"
            }
        }
    }

    fun clockOut() {
        _isClockedIn.value = false
        _clockInHour.value = null
    }

    private val currentMonthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
    
    val currentGoal: StateFlow<GoalEntity?> = repository.getGoalByMonth(currentMonthYear)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    // Derived states for Home Screen
    val personalRevenue: StateFlow<Double> = allActivities.map { activities ->
        activities.filter { it.type == "SALE" && it.creditedToId == null }.sumOf { it.price ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val personalTransactions: StateFlow<Int> = allActivities.map { activities ->
        activities.count { it.type == "SALE" && it.creditedToId == null }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val personalTargetProgress: StateFlow<Float> = combine(personalRevenue, currentGoal) { rev, goal ->
        val target = goal?.personalTarget ?: 100000000.0 // Default 100jt
        if (target > 0) (rev / target).toFloat() else 0f
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0f)
    
    val storeRevenue: StateFlow<Double> = allActivities.map { activities ->
        activities.filter { it.type == "SALE" }.sumOf { it.price ?: 0.0 }
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

    fun addOrUpdateProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.insertProduct(product)
        }
    }
    
    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.insertProduct(product.copy(isActive = !product.isActive))
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
}

class MainViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
