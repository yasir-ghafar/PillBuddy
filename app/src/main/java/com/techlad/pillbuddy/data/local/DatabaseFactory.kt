package com.techlad.pillbuddy.data.local

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.techlad.pillbuddy.data.db.PillBuddyDatabase

/**
 * Android SQLite driver. The schema and queries are generated from `.sq` files and can move to
 * common code with `NativeSqliteDriver` when this app becomes KMM.
 */
fun createPillBuddyDriver(context: Context): SqlDriver {
    return AndroidSqliteDriver(
        schema = PillBuddyDatabase.Schema,
        context = context.applicationContext,
        name = DATABASE_NAME,
        callback = object : AndroidSqliteDriver.Callback(PillBuddyDatabase.Schema) {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                db.setForeignKeyConstraintsEnabled(true)
            }
        },
    )
}

fun openPillBuddyDatabase(context: Context): PillBuddyDatabase {
    return PillBuddyDatabase(createPillBuddyDriver(context))
}

private const val DATABASE_NAME = "pillbuddy.db"
