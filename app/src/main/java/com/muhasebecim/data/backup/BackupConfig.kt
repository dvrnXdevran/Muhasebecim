package com.muhasebecim.data.backup

import com.muhasebecim.BuildConfig

object BackupConfig {
    val SERVER_BASE_URL: String = BuildConfig.BACKUP_SERVER_BASE_URL
    val API_TOKEN: String = BuildConfig.BACKUP_API_TOKEN

    fun isConfigured(): Boolean {
        return SERVER_BASE_URL.startsWith("https://") &&
            SERVER_BASE_URL.isNotBlank() &&
            API_TOKEN.length >= 24
    }
}
