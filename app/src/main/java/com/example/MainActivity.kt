package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.BottomNavBar
import com.example.ui.navigation.Screen
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.MyApplication
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.masterdata.ManageColleaguesScreen
import com.example.ui.screens.masterdata.ManageGoalsScreen
import com.example.ui.screens.masterdata.ManageProductsScreen
import com.example.ui.viewmodels.MainViewModel
import com.example.ui.viewmodels.MainViewModelFactory
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.screens.PlaceholderScreen

import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalHazeState = staticCompositionLocalOf<HazeState> { error("No HazeState") }

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
        val context = LocalContext.current
        val app = context.applicationContext as MyApplication
        val prefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val mainViewModel: MainViewModel = viewModel(
            factory = MainViewModelFactory(app.repository, prefs)
        )
        MyApplicationTheme {
          val hazeState = androidx.compose.runtime.remember { HazeState() }
          CompositionLocalProvider(LocalHazeState provides hazeState) {
          val navController = rememberNavController()
          val navBackStackEntry by navController.currentBackStackEntryAsState()
          val currentRoute = navBackStackEntry?.destination?.route
        val isDarkTheme = true

        val darkBackground = androidx.compose.ui.graphics.Brush.verticalGradient(
            colors = listOf(
                androidx.compose.ui.graphics.Color(0xFF0F172A),
                androidx.compose.ui.graphics.Color(0xFF0B1021),
                androidx.compose.ui.graphics.Color(0xFF04060C)
            )
        )
        val lightBackground = androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(
                androidx.compose.ui.graphics.Color(0xFFEEF4FF),
                androidx.compose.ui.graphics.Color(0xFFF5F8FF),
                androidx.compose.ui.graphics.Color(0xFFEBF0FF)
            )
        )

        var showLogBottomSheet by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

        androidx.activity.compose.BackHandler(enabled = showLogBottomSheet) {
            showLogBottomSheet = false
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .haze(state = LocalHazeState.current)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (isDarkTheme) darkBackground else lightBackground)
                ) {
                // Blobs
            Box(modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-40).dp, y = (-20).dp)
                .size(400.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(
                    androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(
                            if (isDarkTheme) androidx.compose.ui.graphics.Color(0x660055FF) else androidx.compose.ui.graphics.Color(0x221565C0),
                            androidx.compose.ui.graphics.Color.Transparent
                        )
                    )
                )
                .blur(if (isDarkTheme) 100.dp else 80.dp)
            )
            Box(modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 40.dp, y = 40.dp)
                .size(350.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(
                    androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(
                            if (isDarkTheme) androidx.compose.ui.graphics.Color(0x5500D4FF) else androidx.compose.ui.graphics.Color(0x152979FF),
                            androidx.compose.ui.graphics.Color.Transparent
                        )
                    )
                )
                .blur(if (isDarkTheme) 90.dp else 60.dp)
            )
            // Extra middle-right electric blue blob
            Box(modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 100.dp, y = (-50).dp)
                .size(250.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(
                    androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(
                            if (isDarkTheme) androidx.compose.ui.graphics.Color(0x447000FF) else androidx.compose.ui.graphics.Color.Transparent,
                            androidx.compose.ui.graphics.Color.Transparent
                        )
                    )
                )
                .blur(80.dp)
            )
          }
        } // Close the Box with .haze()
          
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            bottomBar = {
                // We pass a bottom bar that floats above the background. M3 scaffold padding might affect it,
                // but we can adjust it or use it normally. 
            }
          ) { innerPadding ->
          Box(modifier = Modifier.fillMaxSize()) {

              NavHost(
                  navController = navController,
                  startDestination = Screen.Home.route,
                  modifier = Modifier.fillMaxSize(),
                  enterTransition = {
                      androidx.compose.animation.fadeIn(
                          animationSpec = androidx.compose.animation.core.tween(300)
                      ) + androidx.compose.animation.scaleIn(
                          initialScale = 0.95f,
                          animationSpec = androidx.compose.animation.core.tween(300)
                      )
                  },
                  exitTransition = {
                      androidx.compose.animation.fadeOut(
                          animationSpec = androidx.compose.animation.core.tween(300)
                      ) + androidx.compose.animation.scaleOut(
                          targetScale = 1.05f,
                          animationSpec = androidx.compose.animation.core.tween(300)
                      )
                  },
                  popEnterTransition = {
                      androidx.compose.animation.fadeIn(
                          animationSpec = androidx.compose.animation.core.tween(300)
                      ) + androidx.compose.animation.scaleIn(
                          initialScale = 1.05f,
                          animationSpec = androidx.compose.animation.core.tween(300)
                      )
                  },
                  popExitTransition = {
                      androidx.compose.animation.fadeOut(
                          animationSpec = androidx.compose.animation.core.tween(300)
                      ) + androidx.compose.animation.scaleOut(
                          targetScale = 0.95f,
                          animationSpec = androidx.compose.animation.core.tween(300)
                      )
                  }
              ) {
                  composable(Screen.Home.route) { com.example.ui.screens.home.HomeScreen(mainViewModel, onNavigate = { navController.navigate(it) }) }
                  composable(Screen.Log.route) { com.example.ui.screens.log.LogActivityScreen(mainViewModel) }
                  composable(Screen.Analytics.route) { com.example.ui.screens.analytics.AnalyticsScreen(mainViewModel) }
                  composable(Screen.Journal.route) { com.example.ui.screens.journal.JournalScreen(mainViewModel) }
                  composable(Screen.Export.route) { com.example.ui.screens.export.ExportScreen(mainViewModel, onBack = { navController.popBackStack() }) }
                  composable(Screen.Profile.route) { com.example.ui.screens.ProfileScreen(mainViewModel, onNavigate = { navController.navigate(it) }) }
                  composable(Screen.ManageProducts.route) { ManageProductsScreen(mainViewModel, onBack = { navController.popBackStack() }) }
                  composable(Screen.ManageColleagues.route) { ManageColleaguesScreen(mainViewModel, onBack = { navController.popBackStack() }) }
                  composable(Screen.ManageGoals.route) { ManageGoalsScreen(mainViewModel, onBack = { navController.popBackStack() }) }
                  composable(Screen.Coach.route) { com.example.ui.screens.coach.CoachScreen(mainViewModel, onBack = { navController.popBackStack() }) }
              }
          }
          
          // Close the main app Box
              
          androidx.compose.animation.AnimatedVisibility(
                  visible = showLogBottomSheet,
                  enter = androidx.compose.animation.fadeIn(),
                  exit = androidx.compose.animation.fadeOut()
              ) {
                  Box(
                      modifier = Modifier
                          .fillMaxSize()
                          .clickable(
                              interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                              indication = null,
                              onClick = { showLogBottomSheet = false }
                          )
                          .let {
                              it.hazeChild(
                                  state = LocalHazeState.current,
                                  style = dev.chrisbanes.haze.HazeStyle(
                                      backgroundColor = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f),
                                      tint = dev.chrisbanes.haze.HazeTint(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f)),
                                      blurRadius = 48.dp
                                  )
                              )
                          }
                  ) {
                      Box(
                          modifier = Modifier
                              .align(Alignment.BottomCenter)
                              .fillMaxWidth()
                              .padding(start = 16.dp, end = 16.dp, bottom = 104.dp)
                              .navigationBarsPadding()
                              .heightIn(max = 600.dp)
                              .wrapContentHeight()
                              .clip(androidx.compose.foundation.shape.RoundedCornerShape(32.dp))
                              .background(if (isDarkTheme) androidx.compose.ui.graphics.Color(0xFF04060C).copy(alpha = 0.85f) else androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f))
                              .clickable(
                                  interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                  indication = null,
                                  onClick = {}
                              )
                      ) {
                          com.example.ui.screens.log.LogBottomSheet(
                              viewModel = mainViewModel,
                              onDismiss = { showLogBottomSheet = false }
                          )
                      }
                  }
              }

              // Floating Bottom Nav Bar
              val bottomBarRoutes = listOf(
                  Screen.Home.route,
                  Screen.Analytics.route,
                  Screen.Journal.route,
                  Screen.Profile.route
              )
              if (currentRoute in bottomBarRoutes) {
                  Box(
                      modifier = Modifier
                          .fillMaxSize()
                          .navigationBarsPadding()
                          .padding(bottom = 16.dp),
                      contentAlignment = Alignment.BottomCenter
                  ) {
                      BottomNavBar(
                          currentRoute = currentRoute,
                          onNavigate = { route ->
                              navController.navigate(route) {
                                  popUpTo(navController.graph.startDestinationId) {
                                      saveState = true
                                  }
                                  launchSingleTop = true
                                  restoreState = true
                              }
                          },
                          onLogClick = {
                              showLogBottomSheet = !showLogBottomSheet
                          },
                          hazeState = LocalHazeState.current
                      )
                  }
              }
        }
      }
    }
  }
}
}
}
