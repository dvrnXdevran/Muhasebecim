package com.muhasebecim

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.muhasebecim.ui.navigation.NavGraph
import com.muhasebecim.ui.navigation.Screen
import com.muhasebecim.ui.theme.AccentBlue
import com.muhasebecim.ui.theme.DarkBackground
import com.muhasebecim.ui.theme.DarkSurface
import com.muhasebecim.ui.theme.MuhasebecimTheme
import com.muhasebecim.ui.theme.TextMuted
import com.muhasebecim.ui.theme.TextPrimary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as MuhasebecimApplication

        setContent {
            MuhasebecimTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                LaunchedEffect(Unit) {
                    app.backupRepository.runAutomaticBackupIfDue()
                }

                val showBottomBar = currentDestination?.route in listOf(
                    Screen.PersonList.route,
                    Screen.GeneralNotes.route,
                    Screen.GeneralLedger.route,
                    Screen.Upcoming.route,
                    Screen.Backup.route
                )

                val bottomItems = listOf(
                    Triple(Screen.PersonList, Icons.Default.People, "Kisiler"),
                    Triple(Screen.GeneralNotes, Icons.Default.Notes, "Notlar"),
                    Triple(Screen.GeneralLedger, Icons.Default.MenuBook, "Genel Defter"),
                    Triple(Screen.Upcoming, Icons.Default.CalendarMonth, "Yaklasanlar"),
                    Triple(Screen.Backup, Icons.Default.SettingsBackupRestore, "Yedek")
                )

                Scaffold(
                    containerColor = DarkBackground,
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(
                                containerColor = DarkSurface,
                                contentColor = TextPrimary
                            ) {
                                bottomItems.forEach { (screen, icon, label) ->
                                    val selected = currentDestination?.hierarchy?.any {
                                        it.route == screen.route
                                    } == true

                                    NavigationBarItem(
                                        icon = {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = label,
                                                tint = if (selected) AccentBlue else TextMuted,
                                                modifier = Modifier
                                                    .padding(vertical = 2.dp)
                                                    .size(26.dp)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = label,
                                                fontSize = 12.sp,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (selected) AccentBlue else TextMuted
                                            )
                                        },
                                        selected = selected,
                                        onClick = {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            indicatorColor = AccentBlue.copy(alpha = 0.14f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { paddingValues ->
                    NavGraph(
                        navController = navController,
                        repository = app.repository,
                        backupRepository = app.backupRepository,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}
