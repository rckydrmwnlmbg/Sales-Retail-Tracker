package com.example.data.repository

import com.example.data.local.dao.AppDao
import com.example.data.local.entity.ActivityEntity
import com.example.data.local.entity.ColleagueEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.ProductEntity

class AppRepository(private val appDao: AppDao) {
    val allProducts = appDao.getAllProducts()
    val allColleagues = appDao.getAllColleagues()
    val allActivities = appDao.getAllActivities()
    val allGoals = appDao.getAllGoals()

    suspend fun insertProduct(product: ProductEntity) = appDao.insertProduct(product)
    suspend fun insertColleague(colleague: ColleagueEntity) = appDao.insertColleague(colleague)
    suspend fun insertActivity(activity: ActivityEntity) = appDao.insertActivity(activity)
    suspend fun deleteActivity(activity: ActivityEntity) = appDao.deleteActivity(activity)
    
    fun getGoalByMonth(monthYear: String) = appDao.getGoalByMonth(monthYear)
    suspend fun insertGoal(goal: GoalEntity) = appDao.insertGoal(goal)
    suspend fun deleteGoal(goal: GoalEntity) = appDao.deleteGoal(goal)
}
