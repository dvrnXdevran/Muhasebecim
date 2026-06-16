package com.muhasebecim.data.backup

import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class BackupHttpClient {
    fun uploadBackup(snapshot: BackupSnapshot) {
        val response = request(
            url = endpoint("backup.php"),
            method = "POST",
            body = BackupJson.toJsonString(snapshot)
        )
        val json = JSONObject(response)
        if (!json.optBoolean("success", false)) {
            throw IllegalStateException(json.optString("message", "Sunucu yedegi kabul etmedi"))
        }
    }

    fun downloadLatest(deviceId: String): RemoteBackupInfo {
        val encodedDeviceId = URLEncoder.encode(deviceId, "UTF-8")
        val response = request(
            url = endpoint("latest.php?device_id=$encodedDeviceId"),
            method = "GET",
            body = null
        )
        val json = JSONObject(response)
        if (!json.optBoolean("success", false)) {
            throw IllegalStateException(json.optString("message", "Son yedek alinamadi"))
        }
        if (!json.optBoolean("hasBackup", false)) {
            return RemoteBackupInfo(hasBackup = false, snapshot = null)
        }

        return RemoteBackupInfo(
            hasBackup = true,
            snapshot = BackupJson.fromJsonString(json.getJSONObject("backup").toString())
        )
    }

    private fun endpoint(path: String): String {
        return BackupConfig.SERVER_BASE_URL.trimEnd('/') + "/" + path
    }

    private fun request(url: String, method: String, body: String?): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15000
            readTimeout = 20000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer ${BackupConfig.API_TOKEN}")
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
        }

        try {
            if (body != null) {
                OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(body)
                }
            }

            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                throw IllegalStateException(response.ifBlank { "Sunucu hatasi: HTTP $code" })
            }
            return response
        } finally {
            connection.disconnect()
        }
    }
}
