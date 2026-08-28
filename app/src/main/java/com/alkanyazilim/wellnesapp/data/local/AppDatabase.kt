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
    version = 4,
    // DEĞİŞTİ: false -> true. Şema geçmişi artık dosya olarak dışa aktarılıyor
    // (bkz. app/schemas/). Bu, gelecekte yazacağın migration'ları GERÇEK şemaya
    // karşı test edebilmen için gerekli — build.gradle'a şu ayarı ekle:
    //
    // android {
    //     defaultConfig {
    //         javaCompileOptions {
    //             annotationProcessorOptions {
    //                 arguments += mapOf("room.schemaLocation" to "$projectDir/schemas")
    //             }
    //         }
    //     }
    // }
    //
    // Sonra app/schemas/ klasörünü Git'e ekle (silme) — her versiyon için bir
    // JSON dosyası birikir, migration yazarken buna bakarak doğru SQL'i üretirsin.
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun runSessionDao(): RunSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // YENİ: Gelecekteki her şema değişikliği için buraya bir Migration eklenecek.
        // Şu an boş çünkü henüz versiyon 4'ten sonra bir değişiklik yapılmadı.
        //
        // ÖRNEK — versiyon 5'te TaskEntity'ye "priority" adında Int bir alan eklersen:
        //
        // private val MIGRATION_4_5 = object : Migration(4, 5) {
        //     override fun migrate(db: SupportSQLiteDatabase) {
        //         db.execSQL("ALTER TABLE TaskEntity ADD COLUMN priority INTEGER NOT NULL DEFAULT 0")
        //     }
        // }
        //
        // Sonra bu listeye ekle: val migrations = arrayOf(MIGRATION_4_5)
        // ve aşağıdaki .addMigrations(*migrations) satırına ver.
        private val migrations: Array<Migration> = arrayOf()

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