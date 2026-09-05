package com.alkanyazilim.wellnesapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Converters {
    @TypeConverter
    fun fromCategory(category: TaskCategory): String = category.name

    @TypeConverter
    fun toCategory(value: String): TaskCategory = TaskCategory.valueOf(value)
}

@Database(
    entities = [TaskEntity::class, TaskCompletionEntity::class, RunSessionEntity::class],
    version = 6,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun runSessionDao(): RunSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // YENİ: Versiyon 4 -> 5 geçişi. RunSessionEntity'ye iki alan eklendi:
        // goalType (koşu adım mı süre mi hedefiyle başladı) ve
        // targetDurationSeconds (süre hedefliyse kaç saniyeydi). Süre bazlı koşu
        // hedefi özelliği eklenirken bu bilgi geçmişe kaydedilmiyordu — bu
        // migration mevcut kullanıcıların koşu geçmişini SİLMEDEN şemayı genişletir.
        internal val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE run_sessions ADD COLUMN goalType TEXT NOT NULL DEFAULT 'STEPS'")
                db.execSQL("ALTER TABLE run_sessions ADD COLUMN targetDurationSeconds INTEGER NOT NULL DEFAULT 0")
            }
        }


        internal val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN currentStreak INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE tasks ADD COLUMN bestStreak INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val migrations: Array<Migration> = arrayOf(MIGRATION_4_5, MIGRATION_5_6)
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wellnesapp_database"
                )
                    // Gelecekte tanımlanacak gerçek migration'lar buradan uygulanır.
                    // Şu an boş bir dizi geçiliyor, MIGRATION_4_5 gibi bir migration
                    // eklendiğinde otomatik olarak devreye girer.
                    .addMigrations(*migrations)
                    // KRİTİK: Bu artık genel bir "her şeyi sil" güvencesi DEĞİL.
                    // Sadece 1, 2 ve 3 numaralı ESKİ (henüz gerçek kullanıcı yokken
                    // kullanılan) versiyonlardan gelen kurulumlar için veri silmeye
                    // izin veriyor. Versiyon 4 ve sonrası için bu güvence GEÇERLİ
                    // DEĞİL — yukarıdaki migrations listesinde karşılığı olmayan bir
                    // geçiş denenirse (örn. birisi versiyon 5'e migration eklemeyi
                    // unutursa), Room artık veriyi sessizce silmek yerine
                    // IllegalStateException fırlatıp UYGULAMAYI ÇÖKERTİR. Bu kasıtlı:
                    // geliştiriciyi migration yazmaya zorlar, kullanıcı verisini
                    // fark ettirmeden kaybetmesini engeller.
                    .fallbackToDestructiveMigrationFrom(1, 2, 3)
                    // Cihaz eski bir debug build'e geri düşerse (örn. test sırasında
                    // versiyon geriye alınırsa) veriyi güvenle sıfırla — bu, ileri
                    // yönlü (upgrade) geçişleri ETKİLEMEZ, sadece downgrade'de çalışır.
                    .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                    .build().also { INSTANCE = it }
            }
        }
    }
}
