package com.muhasebecim.data.backup

import android.content.Context
import java.util.UUID

class BackupPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("backup_preferences", Context.MODE_PRIVATE)

    fun getDeviceId(): String {
        val existing = prefs.getString(KEY_DEVICE_ID, null)
        if (!existing.isNullOrBlank()) return existing

        val generated = UUID.randomUUID().toString()
        prefs.edit().putString(KEY_DEVICE_ID, generated).apply()
        return generated
    }

    fun getStatus(): LocalBackupStatus {
        val lastSuccess = prefs.getLong(KEY_LAST_SUCCESS_AT, 0L).takeIf { it > 0L }
        val lastAutoAttempt = prefs.getLong(KEY_LAST_AUTO_ATTEMPT_AT, 0L).takeIf { it > 0L }
        return LocalBackupStatus(
            lastSuccessfulBackupAt = lastSuccess,
            lastAutoBackupAttemptAt = lastAutoAttempt,
            lastStatusMessage = prefs.getString(KEY_STATUS_MESSAGE, null),
            lastErrorMessage = prefs.getString(KEY_ERROR_MESSAGE, null),
            isConfigured = BackupConfig.isConfigured()
        )
    }

    fun markBackupSuccess(createdAt: Long, message: String) {
        prefs.edit()
            .putLong(KEY_LAST_SUCCESS_AT, createdAt)
            .putString(KEY_STATUS_MESSAGE, message)
            .remove(KEY_ERROR_MESSAGE)
            .apply()
    }

    fun markAutoAttempt(at: Long) {
        prefs.edit().putLong(KEY_LAST_AUTO_ATTEMPT_AT, at).apply()
    }

    fun markError(message: String) {
        prefs.edit()
            .putString(KEY_ERROR_MESSAGE, message)
            .putString(KEY_STATUS_MESSAGE, "Yedekleme basarisiz")
            .apply()
    }

    companion object {
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_LAST_SUCCESS_AT = "last_success_at"
        private const val KEY_LAST_AUTO_ATTEMPT_AT = "last_auto_attempt_at"
        private const val KEY_STATUS_MESSAGE = "status_message"
        private const val KEY_ERROR_MESSAGE = "error_message"
    }
}
