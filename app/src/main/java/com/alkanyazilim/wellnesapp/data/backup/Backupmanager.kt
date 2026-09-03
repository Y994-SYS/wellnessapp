package com.alkanyazilim.wellnesapp.data.backup

import android.content.Context
import android.net.Uri
import com.alkanyazilim.wellnesapp.data.local.AppDatabase
import com.alkanyazilim.wellnesapp.data.local.AppSettingsDataStore
import com.alkanyazilim.wellnesapp.data.local.RunSessionEntity
import com.alkanyazilim.wellnesapp.data.local.TaskCategory
import com.alkanyazilim.wellnesapp.data.local.TaskCompletionEntity
import com.alkanyazilim.wellnesapp.data.local.TaskEntity
import com.alkanyazilim.wellnesapp.data.local.ThemeMode
import com.alkanyazilim.wellnesapp.data.local.UserPreferences
import com.alkanyazilim.wellnesapp.data.local.WaterDataStore
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

/**
 * Uygulamanın tüm yerel verisini (profil, hedefler, görevler, koşu geçmişi,
 * su tüketim kayıtları) tek bir JSON dosyasına aktarır ve geri yükler.
 *
 * Neden JSON + Storage Access Framework (SAF)?
 * - Ekstra bir kütüphane gerektirmez (org.json Android SDK'da hazır gelir).
 * - Kullanıcı dosyayı istediği yere kaydedebilir (Drive, dosya yöneticisi,
 *   kendine e-posta/WhatsApp ile gönderme vb.) — SAF, tek tek depolama izni
 *   istemeden bunu güvenle sağlar.
 * - İnsan tarafından okunabilir olduğu için hata ayıklaması kolaydır.
 */
class BackupManager(private val context: Context) {

    private val taskDao = AppDatabase.getInstance(context).taskDao()
    private val runSessionDao = AppDatabase.getInstance(context).runSessionDao()
    private val settingsStore = AppSettingsDataStore(context)
    private val waterStore = WaterDataStore(context)
    private val userPreferences = UserPreferences(context)

    companion object {
        private const val EXPORT_VERSION = 1
    }

    /** Seçilen [uri]'ye tüm veriyi JSON olarak yazar. */
    suspend fun exportToUri(uri: Uri) {
        val json = buildExportJson()
        val output = context.contentResolver.openOutputStream(uri)
            ?: throw IllegalStateException("Dosya için çıktı akışı açılamadı")
        output.use { it.write(json.toString(2).toByteArray(Charsets.UTF_8)) }
    }

    /** Seçilen [uri]'deki JSON dosyasını okuyup mevcut tüm veriyi bununla değiştirir. */
    suspend fun importFromUri(uri: Uri) {
        val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: throw IllegalStateException("Dosya okunamadı")
        val json = JSONObject(text)
        restoreFromJson(json)
    }

    private suspend fun buildExportJson(): JSONObject {
        val root = JSONObject()
        root.put("exportVersion", EXPORT_VERSION)
        root.put("exportedAtMillis", System.currentTimeMillis())

        // Profil + tema
        val profile = JSONObject()
        profile.put("name", settingsStore.userName.first())
        profile.put("weightKg", settingsStore.userWeightKg.first())
        profile.put("heightCm", settingsStore.userHeightCm.first())
        profile.put("age", settingsStore.userAge.first())
        profile.put("themeMode", settingsStore.themeMode.first().name)
        root.put("profile", profile)

        // Hedefler
        val goals = JSONObject()
        goals.put("stepGoal", userPreferences.stepGoal.first())
        goals.put("waterGoalMl", waterStore.dailyGoal.first())
        goals.put("glassSizeMl", waterStore.glassSize.first())
        root.put("goals", goals)

        // Su tüketim kayıtları (tarih -> ml)
        val waterEntries = JSONObject()
        waterStore.allConsumedEntries.first().forEach { (date, ml) ->
            waterEntries.put(date.toString(), ml)
        }
        root.put("waterConsumption", waterEntries)

        // Görevler
        val tasksArray = JSONArray()
        taskDao.getAllTasks().first().forEach { task ->
            val obj = JSONObject()
            obj.put("id", task.id)
            obj.put("title", task.title)
            obj.put("category", task.category.name)
            obj.put("isRecurring", task.isRecurring)
            obj.put("createdDate", task.createdDate)
            obj.put("icon", task.icon)
            obj.put("reminderEnabled", task.reminderEnabled)
            obj.put("reminderHour", task.reminderHour ?: JSONObject.NULL)
            obj.put("reminderMinute", task.reminderMinute ?: JSONObject.NULL)
            tasksArray.put(obj)
        }
        root.put("tasks", tasksArray)

        // Görev tamamlama kayıtları
        val completionsArray = JSONArray()
        taskDao.getAllCompletionsOnce().forEach { completion ->
            val obj = JSONObject()
            obj.put("taskId", completion.taskId)
            obj.put("date", completion.date)
            obj.put("isCompleted", completion.isCompleted)
            completionsArray.put(obj)
        }
        root.put("taskCompletions", completionsArray)

        // Koşu geçmişi
        val runSessionsArray = JSONArray()
        runSessionDao.getAll().first().forEach { session ->
            val obj = JSONObject()
            obj.put("id", session.id)
            obj.put("startTimeMillis", session.startTimeMillis)
            obj.put("endTimeMillis", session.endTimeMillis)
            obj.put("steps", session.steps)
            obj.put("targetSteps", session.targetSteps)
            obj.put("durationSeconds", session.durationSeconds)
            // YENİ
            obj.put("goalType", session.goalType)
            obj.put("targetDurationSeconds", session.targetDurationSeconds)
            runSessionsArray.put(obj)
        }
        root.put("runSessions", runSessionsArray)

        return root
    }

    private suspend fun restoreFromJson(json: JSONObject) {
        // Profil + tema
        json.optJSONObject("profile")?.let { profile ->
            settingsStore.setProfile(
                name = profile.optString("name", ""),
                weightKg = profile.optInt("weightKg", 70),
                heightCm = profile.optInt("heightCm", 170),
                age = profile.optInt("age", 25)
            )
            val themeName = profile.optString("themeMode", ThemeMode.SYSTEM.name)
            val theme = runCatching { ThemeMode.valueOf(themeName) }.getOrDefault(ThemeMode.SYSTEM)
            settingsStore.setThemeMode(theme)
        }

        // Hedefler
        json.optJSONObject("goals")?.let { goals ->
            if (goals.has("stepGoal")) userPreferences.setStepGoal(goals.getInt("stepGoal"))
            if (goals.has("waterGoalMl")) waterStore.setDailyGoal(goals.getInt("waterGoalMl"))
            if (goals.has("glassSizeMl")) waterStore.setGlassSize(goals.getInt("glassSizeMl"))
        }

        // Su tüketim kayıtları — önce mevcut kayıtları temizle, sonra dosyadakini yaz
        waterStore.clearAllConsumedEntries()
        json.optJSONObject("waterConsumption")?.let { waterEntries ->
            waterEntries.keys().forEach { dateKey ->
                waterStore.setConsumedForDate(dateKey, waterEntries.getInt(dateKey))
            }
        }

        // Görevler — önce temizle, sonra orijinal ID'leriyle geri yükle.
        // Sıralama önemli: önce completions, sonra tasks silinir (foreign-key
        // mantığı gereği önce "çocuk" tablo temizlenir).
        taskDao.deleteAllCompletions()
        taskDao.deleteAllTasks()

        val tasks = mutableListOf<TaskEntity>()
        json.optJSONArray("tasks")?.let { arr ->
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                tasks.add(
                    TaskEntity(
                        id = obj.getInt("id"),
                        title = obj.getString("title"),
                        category = TaskCategory.valueOf(obj.getString("category")),
                        isRecurring = obj.getBoolean("isRecurring"),
                        createdDate = obj.getString("createdDate"),
                        icon = obj.optString("icon", "📝"),
                        reminderEnabled = obj.optBoolean("reminderEnabled", false),
                        reminderHour = if (obj.isNull("reminderHour")) null else obj.getInt("reminderHour"),
                        reminderMinute = if (obj.isNull("reminderMinute")) null else obj.getInt("reminderMinute")
                    )
                )
            }
        }
        if (tasks.isNotEmpty()) taskDao.insertTasksRestore(tasks)

        val completions = mutableListOf<TaskCompletionEntity>()
        json.optJSONArray("taskCompletions")?.let { arr ->
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                completions.add(
                    TaskCompletionEntity(
                        taskId = obj.getInt("taskId"),
                        date = obj.getString("date"),
                        isCompleted = obj.getBoolean("isCompleted")
                    )
                )
            }
        }
        if (completions.isNotEmpty()) taskDao.insertCompletionsRestore(completions)

        // Koşu geçmişi — önce temizle, sonra orijinal ID'leriyle geri yükle
        runSessionDao.deleteAll()
        val sessions = mutableListOf<RunSessionEntity>()
        json.optJSONArray("runSessions")?.let { arr ->
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                sessions.add(
                    RunSessionEntity(
                        id = obj.getInt("id"),
                        startTimeMillis = obj.getLong("startTimeMillis"),
                        endTimeMillis = obj.getLong("endTimeMillis"),
                        steps = obj.getInt("steps"),
                        targetSteps = obj.getInt("targetSteps"),
                        durationSeconds = obj.getInt("durationSeconds"),
                        // YENİ: Eski (v1) yedek dosyalarında bu alanlar yoktu —
                        // optString/optInt ile geriye dönük uyumlu okuyoruz.
                        goalType = obj.optString("goalType", "STEPS"),
                        targetDurationSeconds = obj.optInt("targetDurationSeconds", 0)
                    )
                )
            }
        }
        if (sessions.isNotEmpty()) runSessionDao.insertAllRestore(sessions)
    }
}