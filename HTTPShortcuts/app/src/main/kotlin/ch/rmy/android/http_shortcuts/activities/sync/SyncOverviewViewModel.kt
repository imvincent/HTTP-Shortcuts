package ch.rmy.android.http_shortcuts.activities.sync

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.activities.sync.models.SyncState
import ch.rmy.android.http_shortcuts.data.domains.sync.SyncRepository
import ch.rmy.android.http_shortcuts.data.enums.SyncType
import ch.rmy.android.http_shortcuts.data.settings.UserPreferences
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.scheduling.sync.SyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncOverviewViewModel
@Inject
constructor(
    application: Application,
    private val userPreferences: UserPreferences,
    private val syncScheduler: SyncScheduler,
    private val syncRepository: SyncRepository,
) : BaseViewModel<Unit, SyncOverviewViewState>(application) {
    override suspend fun initialize(data: Unit): SyncOverviewViewState {
        val syncType = userPreferences.syncType
        val syncConfig = syncType?.let {
            syncRepository.getConfig(it)
        }

        viewModelScope.launch {
            syncScheduler.isSyncing().collect { isSyncing ->
                updateViewState {
                    copy(isSyncing = isSyncing)
                }
            }
        }

        return SyncOverviewViewState(
            syncType = syncType,
            isConfigValid = syncConfig?.isValid == true,
        )
    }

    fun onSyncTypeSelected(syncType: SyncType?) = runAction {
        userPreferences.syncType = syncType
        val syncConfig = syncType?.let {
            syncRepository.getConfig(syncType)
        }
        updateViewState {
            copy(
                syncType = syncType,
                isConfigValid = syncConfig?.isValid == true,
            )
        }
        syncScheduler.schedule()
    }

    fun onConfigureImportClicked() = runAction {
        navigate(NavigationDestination.SyncImport)
    }

    fun onConfigureExportClicked() = runAction {
        navigate(NavigationDestination.SyncExport)
    }

    fun onConfigurationChanged() = runAction {
        syncScheduler.schedule()
        val syncConfig = getCurrentViewState().syncType?.let {
            syncRepository.getConfig(it)
        }
        updateViewState {
            copy(isConfigValid = syncConfig?.isValid == true)
        }
    }

    fun onSyncNowClicked() = runAction {
        if (getCurrentViewState().syncState == SyncState.IDLE) {
            syncScheduler.syncNow()
        }
    }
}
