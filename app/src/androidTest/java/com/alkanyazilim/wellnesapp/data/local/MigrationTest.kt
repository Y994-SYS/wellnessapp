package com.alkanyazilim.wellnesapp.data.local

import android.system.Os
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import java.io.IOException
import org.junit.Rule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import junit.framework.TestCase.assertTrue
import org.junit.runner.RunWith
import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import com.alkanyazilim.wellnesapp.data.local.AppDatabase
import junit.framework.TestCase.assertEquals

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val TEST_DB = "migration-test-db"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    @Throws(IOException::class)
    internal fun migrate4To5_preservesExistingRunAndAddsDefaults() {
        // 1) v4 şemasıyla veritabanı oluştur, migration ÖNCESİ bir koşu kaydı ekle
        //    (o zamanki run_sessions tablosunda goalType/targetDurationSeconds yoktu)
        helper.createDatabase(TEST_DB, 4).apply {
            execSQL(
                """
                INSERT INTO run_sessions (id, startTimeMillis, endTimeMillis, steps, targetSteps, durationSeconds)
                VALUES (1, 1000, 2000, 500, 1000, 600)
                """.trimIndent()
            )
            close()
        }

        // 2) Gerçek MIGRATION_4_5'i çalıştır, Room'un ürettiği şemanın
        //    5.json ile birebir eştiğini de otomatik doğrular (validateDroppedTables = true)
        val db = helper.runMigrationsAndValidate(TEST_DB, 5, true, AppDatabase.MIGRATION_4_5)

        // 3) Eski satır hâlâ okunabiliyor mu, yeni alanlar doğru default'u aldı mı?
        val cursor = db.query("SELECT * FROM run_sessions WHERE id = 1")
        assertTrue("Migration sonrası eski satır kaybolmuş", cursor.moveToFirst())

        val goalTypeIndex = cursor.getColumnIndex("goalType")
        val targetDurationIndex = cursor.getColumnIndex("targetDurationSeconds")

        assertEquals("Yeni alan default değeri yanlış", "STEPS", cursor.getString(goalTypeIndex))
        assertEquals("Yeni alan default değeri yanlış", 0, cursor.getInt(targetDurationIndex))

        cursor.close()
        db.close()
    }

    @Test
    @Throws(IOException::class)
    internal fun migrate5To6_preservesExistingTaskAndAddsStreakDefaults() {
        // 1) v5 şemasıyla veritabanı oluştur, migration ÖNCESİ bir görev kaydı ekle
        helper.createDatabase(TEST_DB, 5).apply {
            execSQL(
                """
                INSERT INTO tasks (id, title, category, isRecurring, createdDate, icon, reminderEnabled, reminderHour, reminderMinute)
                VALUES (1, 'Su ic', 'SAGLIK', 1, '2026-01-01', '💧', 0, NULL, NULL)
                """.trimIndent()
            )
            close()
        }

        // 2) Gerçek MIGRATION_5_6'yı çalıştır
        val db = helper.runMigrationsAndValidate(TEST_DB, 6, true, AppDatabase.MIGRATION_5_6)

        // 3) Eski satır hâlâ okunabiliyor mu?
        val cursor = db.query("SELECT * FROM tasks WHERE id = 1")
        assertTrue("Migration sonrası eski satır kaybolmuş", cursor.moveToFirst())

        // 4) Yeni alanlar doğru default'u aldı mı?
        val currentStreakIndex = cursor.getColumnIndex("currentStreak")
        val bestStreakIndex = cursor.getColumnIndex("bestStreak")

        assertEquals("Yeni alan default değeri yanlış", 0, cursor.getInt(currentStreakIndex))
        assertEquals("Yeni alan default değeri yanlış", 0, cursor.getInt(bestStreakIndex))

        cursor.close()
        db.close()
    }
}
