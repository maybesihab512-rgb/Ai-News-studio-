package com.ainewsstudio.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ainewsstudio.data.AppViewModel
import com.ainewsstudio.ui.components.LocalAnimations
import com.ainewsstudio.ui.screens.BrandingScreen
import com.ainewsstudio.ui.screens.HistoryScreen
import com.ainewsstudio.ui.screens.HomeScreen
import com.ainewsstudio.ui.screens.PreviewScreen
import com.ainewsstudio.ui.screens.ProfileSetupScreen
import com.ainewsstudio.ui.screens.SettingsScreen
import com.ainewsstudio.ui.theme.Accent
import com.ainewsstudio.ui.theme.Ink
import com.ainewsstudio.ui.theme.Surface1
import com.ainewsstudio.ui.theme.TextHi
import com.ainewsstudio.ui.theme.TextMid

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private const val PREVIEW = "preview"

private val tabs = listOf(
    Tab("home", "হোম", Icons.Filled.Home),
    Tab("history", "ইতিহাস", Icons.Filled.DateRange),
    Tab("branding", "ব্র্যান্ডিং", Icons.Filled.Star),
    Tab("settings", "সেটিংস", Icons.Filled.Settings),
)

/** First launch shows the one-time creator profile setup; after that the normal app. */
@Composable
fun AppRoot(vm: AppViewModel) {
    if (vm.profileDone) MainScaffold(vm) else ProfileSetupScreen(vm)
}

@Composable
private fun MainScaffold(vm: AppViewModel) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route

    CompositionLocalProvider(LocalAnimations provides vm.animations) {
        Scaffold(
            containerColor = Ink,
            bottomBar = {
                if (route != PREVIEW) {
                    NavigationBar(containerColor = Surface1) {
                        tabs.forEach { tab ->
                            NavigationBarItem(
                                selected = route == tab.route,
                                onClick = {
                                    nav.navigate(tab.route) {
                                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(tab.icon, contentDescription = null) },
                                label = { Text(tab.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = TextHi,
                                    selectedTextColor = TextHi,
                                    indicatorColor = Accent.copy(alpha = 0.25f),
                                    unselectedIconColor = TextMid,
                                    unselectedTextColor = TextMid,
                                ),
                            )
                        }
                    }
                }
            },
        ) { pad ->
            NavHost(
                navController = nav,
                startDestination = "home",
                modifier = Modifier.padding(pad),
                enterTransition = { if (vm.animations) fadeIn(tween(180)) else EnterTransition.None },
                exitTransition = { if (vm.animations) fadeOut(tween(120)) else ExitTransition.None },
            ) {
                composable("home") { HomeScreen(vm) { nav.navigate(PREVIEW) } }
                composable("history") { HistoryScreen(vm) { nav.navigate(PREVIEW) } }
                composable("branding") { BrandingScreen(vm) }
                composable("settings") {
                    SettingsScreen(vm) {
                        nav.navigate("branding") {
                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
                composable(PREVIEW) { PreviewScreen(vm) { nav.popBackStack() } }
            }
        }
    }
}
