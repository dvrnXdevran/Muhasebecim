package com.muhasebecim.data.backup

import com.muhasebecim.data.model.GeneralNote
import com.muhasebecim.data.model.GeneralScheduledNote
import com.muhasebecim.data.model.GeneralTransaction
import com.muhasebecim.data.model.GeneralTransactionType
import com.muhasebecim.data.model.Note
import com.muhasebecim.data.model.Person
import com.muhasebecim.data.model.ScheduledNote
import com.muhasebecim.data.model.ScheduledNoteType
import com.muhasebecim.data.model.Transaction
import com.muhasebecim.data.model.TransactionType
import org.json.JSONArray
import org.json.JSONObject

object BackupJson {
    fun toJsonString(snapshot: BackupSnapshot): String = snapshot.toJson().toString()

    fun fromJsonString(json: String): BackupSnapshot = JSONObject(json).toBackupSnapshot()

    fun BackupSnapshot.toJson(): JSONObject {
        return JSONObject()
            .put("backupVersion", backupVersion)
            .put("databaseVersion", databaseVersion)
            .put("createdAt", createdAt)
            .put("appVersion", appVersion)
            .put("deviceLabel", deviceLabel)
            .put("deviceId", deviceId)
            .put("payload", payload.toJson())
    }

    fun JSONObject.toBackupSnapshot(): BackupSnapshot {
        return BackupSnapshot(
            backupVersion = getInt("backupVersion"),
            databaseVersion = getInt("databaseVersion"),
            createdAt = getLong("createdAt"),
            appVersion = getString("appVersion"),
            deviceLabel = getString("deviceLabel"),
            deviceId = optString("deviceId", ""),
            payload = getJSONObject("payload").toBackupPayload()
        )
    }

    private fun BackupPayload.toJson(): JSONObject {
        return JSONObject()
            .put("persons", persons.toJsonArray { it.toJson() })
            .put("transactions", transactions.toJsonArray { it.toJson() })
            .put("notes", notes.toJsonArray { it.toJson() })
            .put("scheduled_notes", scheduledNotes.toJsonArray { it.toJson() })
            .put("general_notes", generalNotes.toJsonArray { it.toJson() })
            .put("general_transactions", generalTransactions.toJsonArray { it.toJson() })
            .put("general_scheduled_notes", generalScheduledNotes.toJsonArray { it.toJson() })
    }

    private fun JSONObject.toBackupPayload(): BackupPayload {
        return BackupPayload(
            persons = getJSONArray("persons").mapObjects { it.toPerson() },
            transactions = getJSONArray("transactions").mapObjects { it.toTransaction() },
            notes = getJSONArray("notes").mapObjects { it.toNote() },
            scheduledNotes = getJSONArray("scheduled_notes").mapObjects { it.toScheduledNote() },
            generalNotes = getJSONArray("general_notes").mapObjects { it.toGeneralNote() },
            generalTransactions = getJSONArray("general_transactions").mapObjects { it.toGeneralTransaction() },
            generalScheduledNotes = getJSONArray("general_scheduled_notes").mapObjects { it.toGeneralScheduledNote() }
        )
    }

    private fun Person.toJson() = JSONObject()
        .put("id", id)
        .put("name", name)
        .put("note", note)
        .put("createdAt", createdAt)

    private fun JSONObject.toPerson() = Person(
        id = getLong("id"),
        name = getString("name"),
        note = optString("note", ""),
        createdAt = getLong("createdAt")
    )

    private fun Transaction.toJson() = JSONObject()
        .put("id", id)
        .put("personId", personId)
        .put("amount", amount)
        .put("description", description)
        .put("type", type.name)
        .put("isPaid", isPaid)
        .put("createdAt", createdAt)

    private fun JSONObject.toTransaction() = Transaction(
        id = getLong("id"),
        personId = getLong("personId"),
        amount = getDouble("amount"),
        description = optString("description", ""),
        type = TransactionType.valueOf(getString("type")),
        isPaid = optBoolean("isPaid", false),
        createdAt = getLong("createdAt")
    )

    private fun Note.toJson() = JSONObject()
        .put("id", id)
        .put("personId", personId)
        .put("content", content)
        .put("createdAt", createdAt)

    private fun JSONObject.toNote() = Note(
        id = getLong("id"),
        personId = getLong("personId"),
        content = getString("content"),
        createdAt = getLong("createdAt")
    )

    private fun ScheduledNote.toJson() = JSONObject()
        .put("id", id)
        .put("personId", personId)
        .put("description", description)
        .put("amount", amount)
        .put("dueDate", dueDate)
        .put("type", type.name)
        .put("isCompleted", isCompleted)
        .put("createdAt", createdAt)

    private fun JSONObject.toScheduledNote() = ScheduledNote(
        id = getLong("id"),
        personId = getLong("personId"),
        description = getString("description"),
        amount = getDouble("amount"),
        dueDate = getLong("dueDate"),
        type = ScheduledNoteType.valueOf(getString("type")),
        isCompleted = optBoolean("isCompleted", false),
        createdAt = getLong("createdAt")
    )

    private fun GeneralNote.toJson() = JSONObject()
        .put("id", id)
        .put("content", content)
        .put("createdAt", createdAt)

    private fun JSONObject.toGeneralNote() = GeneralNote(
        id = getLong("id"),
        content = getString("content"),
        createdAt = getLong("createdAt")
    )

    private fun GeneralTransaction.toJson() = JSONObject()
        .put("id", id)
        .put("amount", amount)
        .put("description", description)
        .put("type", type.name)
        .put("isPaid", isPaid)
        .put("createdAt", createdAt)

    private fun JSONObject.toGeneralTransaction() = GeneralTransaction(
        id = getLong("id"),
        amount = getDouble("amount"),
        description = optString("description", ""),
        type = GeneralTransactionType.valueOf(getString("type")),
        isPaid = optBoolean("isPaid", false),
        createdAt = getLong("createdAt")
    )

    private fun GeneralScheduledNote.toJson() = JSONObject()
        .put("id", id)
        .put("description", description)
        .put("amount", amount)
        .put("dueDate", dueDate)
        .put("type", type.name)
        .put("isCompleted", isCompleted)
        .put("createdAt", createdAt)

    private fun JSONObject.toGeneralScheduledNote() = GeneralScheduledNote(
        id = getLong("id"),
        description = getString("description"),
        amount = getDouble("amount"),
        dueDate = getLong("dueDate"),
        type = ScheduledNoteType.valueOf(getString("type")),
        isCompleted = optBoolean("isCompleted", false),
        createdAt = getLong("createdAt")
    )

    private fun <T> List<T>.toJsonArray(mapper: (T) -> JSONObject): JSONArray {
        val array = JSONArray()
        forEach { array.put(mapper(it)) }
        return array
    }

    private fun <T> JSONArray.mapObjects(mapper: (JSONObject) -> T): List<T> {
        return List(length()) { index -> mapper(getJSONObject(index)) }
    }
}
