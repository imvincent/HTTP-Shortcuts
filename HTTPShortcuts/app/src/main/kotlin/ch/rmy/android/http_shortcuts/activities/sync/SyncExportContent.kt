package ch.rmy.android.http_shortcuts.activities.sync

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.sync.components.PasswordProtection
import ch.rmy.android.http_shortcuts.activities.sync.components.SyncScheduleSelector
import ch.rmy.android.http_shortcuts.components.SelectionField
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.VerticalSpacer
import ch.rmy.android.http_shortcuts.data.enums.SyncSchedule
import ch.rmy.android.http_shortcuts.data.enums.SyncTargetType

@Composable
fun SyncExportContent(
    viewState: SyncExportViewState,
    onScheduleChanged: (SyncSchedule) -> Unit,
    onFilePasswordChanged: (String) -> Unit,
    onTargetTypeChanged: (SyncTargetType) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.MEDIUM)
            .verticalScroll(rememberScrollState()),
    ) {
        SyncScheduleSelector(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(R.string.label_export_schedule),
            syncSchedule = viewState.schedule,
            onSyncScheduleChanged = onScheduleChanged,
        )

        VerticalSpacer(Spacing.SMALL)

        HorizontalDivider()

        VerticalSpacer(Spacing.SMALL)

        PasswordProtection(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(R.string.label_export_protect_with_password),
            password = viewState.password,
            onPasswordChanged = onFilePasswordChanged,
        )

        VerticalSpacer(Spacing.SMALL)

        HorizontalDivider()

        VerticalSpacer(Spacing.SMALL)

        SelectionField(
            title = stringResource(R.string.label_export_target),
            selectedKey = viewState.targetType,
            items = listOf(
                SyncTargetType.FILE to stringResource(R.string.option_sync_export_target_file),
                SyncTargetType.URL to stringResource(R.string.option_sync_export_target_url),
            ),
            onItemSelected = onTargetTypeChanged,
        )

        AnimatedVisibility(visible = viewState.targetType == SyncTargetType.FILE) {
            // TODO: Directory picker

            // TODO: File name input field
        }

        AnimatedVisibility(visible = viewState.targetType == SyncTargetType.URL) {
            // TODO: URL field

            // TODO: username field

            // TODO: password field
        }
    }
}
