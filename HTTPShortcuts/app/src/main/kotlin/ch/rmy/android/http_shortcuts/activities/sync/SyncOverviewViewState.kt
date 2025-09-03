package ch.rmy.android.http_shortcuts.activities.sync

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.activities.sync.models.SyncState
import ch.rmy.android.http_shortcuts.data.enums.SyncType

@Stable
data class SyncOverviewViewState(
    val syncType: SyncType?,
    val isConfigValid: Boolean,
    val isSyncing: Boolean = false,
) {
    val syncState: SyncState?
        get() {
            if (syncType == null || !isConfigValid) {
                return null
            }
            return if (isSyncing) {
                SyncState.SYNCING
            } else {
                SyncState.IDLE
            }
        }
}
