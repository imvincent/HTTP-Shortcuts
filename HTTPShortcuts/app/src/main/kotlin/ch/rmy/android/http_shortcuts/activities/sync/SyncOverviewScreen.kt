package ch.rmy.android.http_shortcuts.activities.sync

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.SavedStateHandle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.sync.models.SyncState
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.data.enums.SyncType
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.navigation.ResultHandler

@Composable
fun SyncOverviewScreen(savedStateHandle: SavedStateHandle) {
    val (viewModel, state) = bindViewModel<SyncOverviewViewState, SyncOverviewViewModel>()

    ResultHandler(savedStateHandle) { result ->
        when (result) {
            NavigationDestination.SyncImport.RESULT_CHANGED,
            NavigationDestination.SyncExport.RESULT_CHANGED,
                -> {
                viewModel.onConfigurationChanged()
            }
        }
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.settings_automatic_import_export),
        floatingActionButton = {
            var previousSyncType by remember {
                mutableStateOf(state?.syncType)
            }
            LaunchedEffect(state?.syncType) {
                if (state?.syncType != null) {
                    previousSyncType = state.syncType
                }
            }
            AnimatedVisibility(
                visible = state?.syncType != null && state.syncState == SyncState.IDLE,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                val syncType = state?.syncType ?: previousSyncType ?: return@AnimatedVisibility
                ExtendedFloatingActionButton(
                    onClick = viewModel::onSyncNowClicked,
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.outline_sync_24),
                            contentDescription = null,
                        )
                    },
                    text = {
                        when (syncType) {
                            SyncType.IMPORT -> Text(
                                text = stringResource(R.string.fab_automatic_import_now),
                            )
                            SyncType.EXPORT -> Text(
                                text = stringResource(R.string.fab_automatic_export_now),
                            )
                        }
                    },
                )
            }
        },
    ) { viewState ->
        SyncOverviewContent(
            viewState,
            onSyncTypeSelected = viewModel::onSyncTypeSelected,
            onConfigureImportClicked = viewModel::onConfigureImportClicked,
            onConfigureExportClicked = viewModel::onConfigureExportClicked,
        )
    }
}
