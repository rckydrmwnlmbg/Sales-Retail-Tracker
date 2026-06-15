package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Products
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    // Colleagues
    @Query("SELECT * FROM colleagues ORDER BY name ASC")
    fun getAllColleagues(): Flow<List<ColleagueEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertColleague(colleague: ColleagueEntity)

    // Activities
    @Query("SELECT * FROM activities ORDER BY timestamp DESC")
    fun getAllActivities(): Flow<List<ActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityEntity)

    // Goals
    @Query("SELECT * FROM goals WHERE monthYear = :monthYear")
    fun getGoalByMonth(monthYear: String): Flow<GoalEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)
}
