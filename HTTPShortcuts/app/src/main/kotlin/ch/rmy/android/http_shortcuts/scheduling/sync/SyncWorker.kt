package ch.rmy.android.http_shortcuts.scheduling.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.data.domains.sync.SyncRepository
import ch.rmy.android.http_shortcuts.data.enums.SyncTargetType
import ch.rmy.android.http_shortcuts.data.enums.SyncType
import ch.rmy.android.http_shortcuts.data.models.SyncConfig
import ch.rmy.android.http_shortcuts.data.settings.DeviceLocalPreferences
import ch.rmy.android.http_shortcuts.data.settings.UserPreferences
import ch.rmy.android.http_shortcuts.history.HistoryEvent
import ch.rmy.android.http_shortcuts.history.HistoryEventLogger
import ch.rmy.android.http_shortcuts.import_export.Exporter
import ch.rmy.android.http_shortcuts.import_export.ImportMode
import ch.rmy.android.http_shortcuts.import_export.Importer
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

@HiltWorker
class SyncWorker
@AssistedInject
constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val userPreferences: UserPreferences,
    private val deviceLocalPreferences: DeviceLocalPreferences,
    private val syncRepository: SyncRepository,
    private val importer: Importer,
    private val exporter: Exporter,
    private val historyEventLogger: HistoryEventLogger,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val syncType = userPreferences.syncType ?: return Result.success()
        val config = syncRepository.getConfig(syncType)

        when (config.type) {
            SyncType.IMPORT -> runImport(config)
            SyncType.EXPORT -> runExport(config)
        }
        deviceLocalPreferences.syncErrorCount = 0
        return Result.success()
    }

    private suspend fun runImport(config: SyncConfig) {
        try {
            when (config.targetType) {
                SyncTargetType.FILE -> TODO() // resolve the uri to the real file
                SyncTargetType.URL -> TODO() // download the url into a temp file using an OkHttp client, then import from it
            }

            val status = importer.importFromUri(
                uri = TODO(),
                importMode = ImportMode.MERGE, // TODO: Allow for a "Replace" style import
                password = config.filePassword.takeUnlessEmpty(),
            )



            historyEventLogger.logEvent(
                HistoryEvent.SyncImportSucceed(),
            )
        } catch (e: Exception) {
            incrementAndCheckErrorCount(e)
            deviceLocalPreferences.syncErrorCount++
            historyEventLogger.logEvent(
                HistoryEvent.SyncImportFailed(),
            )
            throw e
        }
    }

    private suspend fun runExport(config: SyncConfig) {
        try {
            when (config.targetType) {
                SyncTargetType.FILE -> TODO() // resolve the uri to the real file
                SyncTargetType.URL -> TODO() // write into a temp file, then upload that file using an OkHttp client
            }

            exporter.exportToUri(
                uri = TODO(),
                password = config.filePassword.takeUnlessEmpty(),
                excludeDefaults = true,
            )

            historyEventLogger.logEvent(
                HistoryEvent.SyncExportSucceed(),
            )
        } catch (e: Exception) {
            incrementAndCheckErrorCount(e)
            historyEventLogger.logEvent(
                HistoryEvent.SyncExportFailed(),
            )
            throw e
        }
    }

    private fun incrementAndCheckErrorCount(e: Exception) {
        if (SINGLE_TAG in tags) {
            return
        }

        // TODO: Don't increment if the exception is a temporary network error
        if (deviceLocalPreferences.syncErrorCount++ > 5) {
            userPreferences.syncType = null
            deviceLocalPreferences.syncErrorCount = 0
        }
    }

    class Starter
    @Inject
    constructor(
        private val context: Context,
    ) {
        fun scheduleRepeating(interval: Duration, requiresNetwork: Boolean) {
            with(WorkManager.getInstance(context)) {
                cancelAllWorkByTag(SCHEDULE_TAG)
                enqueue(
                    PeriodicWorkRequestBuilder<SyncWorker>(interval.toJavaDuration())
                        .addTag(TAG)
                        .addTag(SCHEDULE_TAG)
                        .setInitialDelay(5.minutes.toJavaDuration())
                        .setConstraints(
                            Constraints.Builder()
                                .setRequiresBatteryNotLow(true)
                                .runIf(requiresNetwork) {
                                    setRequiredNetworkType(NetworkType.CONNECTED)
                                }
                                .build(),
                        )
                        .build(),
                )
            }
        }

        fun scheduleNow() {
            with(WorkManager.getInstance(context)) {
                cancelAllWorkByTag(SINGLE_TAG)
                enqueue(
                    OneTimeWorkRequestBuilder<SyncWorker>()
                        .addTag(TAG)
                        .addTag(SINGLE_TAG)
                        .build(),
                )
            }
        }

        fun cancel() {
            with(WorkManager.getInstance(context)) {
                cancelAllWorkByTag(TAG)
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        fun isRunning(): Flow<Boolean> =
            WorkManager.getInstance(context)
                .getWorkInfosByTagFlow(TAG)
                .mapLatest { workInfos ->
                    workInfos.any { it.state == WorkInfo.State.RUNNING }
                }
                .distinctUntilChanged()
    }

    companion object {
        private const val TAG = "sync_worker"
        private const val SCHEDULE_TAG = "scheduled_sync_worker"
        private const val SINGLE_TAG = "single_sync_worker"
    }
}
