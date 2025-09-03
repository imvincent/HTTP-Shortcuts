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
import ch.rmy.android.http_shortcuts.data.domains.sync.SyncRepository
import ch.rmy.android.http_shortcuts.data.enums.SyncType
import ch.rmy.android.http_shortcuts.data.settings.UserPreferences
import ch.rmy.android.http_shortcuts.import_export.Exporter
import ch.rmy.android.http_shortcuts.import_export.Importer
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
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
    private val syncRepository: SyncRepository,
    private val importer: Importer,
    private val exporter: Exporter,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val syncType = userPreferences.syncType ?: return Result.success()
        val config = syncRepository.getConfig(syncType)

        delay(1000)

        when (config.type) {
            SyncType.IMPORT -> {
                // TODO
            }
            SyncType.EXPORT -> {
                // TODO
            }
        }
        return Result.success() // TODO
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
