package com.muhasebecim.data.database

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration

object DatabaseMigrations {
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            if (!hasColumn(database, "persons", "note")) {
                database.execSQL("ALTER TABLE persons ADD COLUMN note TEXT NOT NULL DEFAULT ''")
            }

            if (!hasColumn(database, "transactions", "isPaid")) {
                database.execSQL("ALTER TABLE transactions ADD COLUMN isPaid INTEGER NOT NULL DEFAULT 0")
            }

            if (!hasColumn(database, "general_transactions", "isPaid")) {
                database.execSQL("ALTER TABLE general_transactions ADD COLUMN isPaid INTEGER NOT NULL DEFAULT 0")
            }

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `general_notes` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `content` TEXT NOT NULL,
                    `createdAt` INTEGER NOT NULL
                )
                """.trimIndent()
            )

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `general_transactions` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `amount` REAL NOT NULL,
                    `description` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `isPaid` INTEGER NOT NULL,
                    `createdAt` INTEGER NOT NULL
                )
                """.trimIndent()
            )

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `general_scheduled_notes` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `description` TEXT NOT NULL,
                    `amount` REAL NOT NULL,
                    `dueDate` INTEGER NOT NULL,
                    `type` TEXT NOT NULL,
                    `isCompleted` INTEGER NOT NULL,
                    `createdAt` INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_2_3)

    private fun hasColumn(
        database: SupportSQLiteDatabase,
        tableName: String,
        columnName: String
    ): Boolean {
        database.query("PRAGMA table_info(`$tableName`)").use { cursor ->
            val nameIndex = cursor.getColumnIndex("name")
            while (cursor.moveToNext()) {
                if (nameIndex >= 0 && cursor.getString(nameIndex) == columnName) {
                    return true
                }
            }
        }
        return false
    }
}
