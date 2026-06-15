package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Log : Screen("log")
    object Analytics : Screen("analytics")
    object Export : Screen("export")
    object Journal : Screen("journal")
    object Profile : Screen("profile")
    object ManageProducts : Screen("manage_products")
    object ManageColleagues : Screen("manage_colleagues")
    object ManageGoals : Screen("manage_goals")
    object Coach : Screen("coach")
}
