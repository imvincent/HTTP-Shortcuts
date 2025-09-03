package ch.rmy.android.http_shortcuts.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryId
import ch.rmy.android.http_shortcuts.data.enums.SyncSchedule
import ch.rmy.android.http_shortcuts.data.enums.SyncTargetType
import ch.rmy.android.http_shortcuts.data.enums.SyncType

@Entity(tableName = "sync_config")
data class SyncConfig(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "sync_type")
    val type: SyncType,
    @ColumnInfo(name = "target_type")
    val targetType: SyncTargetType = SyncTargetType.FILE,
    @ColumnInfo(name = "target_directory_id")
    val targetDirectoryId: WorkingDirectoryId? = null,
    @ColumnInfo(name = "target_file_name")
    val targetFileName: String? = null,
    @ColumnInfo(name = "target_url")
    val targetUrl: String? = null,
    @ColumnInfo(name = "target_auth_username")
    val targetAuthUsername: String? = null,
    @ColumnInfo(name = "target_auth_password")
    val targetAuthPassword: String? = null,
    @ColumnInfo(name = "schedule")
    val schedule: SyncSchedule,
    @ColumnInfo(name = "file_password")
    val filePassword: String = "",
    @ColumnInfo(name = "replace_local")
    val replaceLocal: Boolean = false,
) {
    val isValid: Boolean
        get() = when (targetType) {
            SyncTargetType.FILE -> !targetFileName.isNullOrEmpty() && !targetDirectoryId.isNullOrEmpty()
            SyncTargetType.URL -> !targetUrl.isNullOrEmpty()
        }
}
