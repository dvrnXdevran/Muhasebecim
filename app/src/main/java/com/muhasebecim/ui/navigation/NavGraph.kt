package com.muhasebecim.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.muhasebecim.data.backup.BackupRepository
import com.muhasebecim.data.repository.MuhasebecimRepository
import com.muhasebecim.ui.screen.BackupScreen
import com.muhasebecim.ui.screen.GeneralLedgerScreen
import com.muhasebecim.ui.screen.GeneralNotesScreen
import com.muhasebecim.ui.screen.PersonDetailScreen
import com.muhasebecim.ui.screen.PersonListScreen
import com.muhasebecim.ui.screen.UpcomingScreen
import com.muhasebecim.ui.viewmodel.BackupViewModel
import com.muhasebecim.ui.viewmodel.GeneralLedgerViewModel
import com.muhasebecim.ui.viewmodel.PersonDetailViewModel
import com.muhasebecim.ui.viewmodel.PersonListViewModel
import com.muhasebecim.ui.viewmodel.UpcomingViewModel

sealed class Screen(val route: String) {
    data object PersonList : Screen("person_list")
    data object GeneralNotes : Screen("general_notes")
    data object PersonDetail : Screen("person_detail/{personId}") {
        fun createRoute(personId: Long) = "person_detail/$personId"
    }
    data object Upcoming : Screen("upcoming")
    data object GeneralLedger : Screen("general_ledger")
    data object Backup : Screen("backup")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    repository: MuhasebecimRepository,
    backupRepository: BackupRepository,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.PersonList.route,
        modifier = modifier
    ) {
        composable(Screen.PersonList.route) {
            val viewModel: PersonListViewModel = viewModel(
                factory = PersonListViewModel.Factory(repository)
            )
            PersonListScreen(
                viewModel = viewModel,
                onPersonClick = { personId ->
                    navController.navigate(Screen.PersonDetail.createRoute(personId))
                }
            )
        }

        composable(
            route = Screen.PersonDetail.route,
            arguments = listOf(navArgument("personId") { type = NavType.LongType })
        ) { backStackEntry ->
            val personId = backStackEntry.arguments?.getLong("personId") ?: return@composable
            val viewModel: PersonDetailViewModel = viewModel(
                factory = PersonDetailViewModel.Factory(repository, personId)
            )
            PersonDetailScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Upcoming.route) {
            val viewModel: UpcomingViewModel = viewModel(
                factory = UpcomingViewModel.Factory(repository)
            )
            val personListViewModel: PersonListViewModel = viewModel(
                factory = PersonListViewModel.Factory(repository)
            )
            val persons by personListViewModel.persons.collectAsState()
            val personNames = persons.associate { it.id to it.name }

            UpcomingScreen(
                viewModel = viewModel,
                personNames = personNames
            )
        }

        composable(Screen.GeneralNotes.route) {
            val viewModel: GeneralLedgerViewModel = viewModel(
                factory = GeneralLedgerViewModel.Factory(repository)
            )
            GeneralNotesScreen(viewModel = viewModel)
        }

        composable(Screen.GeneralLedger.route) {
            val viewModel: GeneralLedgerViewModel = viewModel(
                factory = GeneralLedgerViewModel.Factory(repository)
            )
            GeneralLedgerScreen(viewModel = viewModel)
        }

        composable(Screen.Backup.route) {
            val viewModel: BackupViewModel = viewModel(
                factory = BackupViewModel.Factory(backupRepository)
            )
            BackupScreen(viewModel = viewModel)
        }
    }
}
