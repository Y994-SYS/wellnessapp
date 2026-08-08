package com.alkanyazilim.wellnesapp.data.model

import android.content.Context
import org.json.JSONArray

data class ExerciseInfo(
    val id: String,
    val name: String,
    val nameTr: String,
    val description: String,
    val difficulty: Int,
    val equipment: List<String>,
    val caloriesPerKgPerHour: Double,
    val primaryMuscle: String,
    val primaryMuscleTr: String,
    val secondaryMuscles: List<String>,
    val typicalSetsReps: String,
    val typicalSetsRepsTr: String,
    val executionTips: List<String>,
    val muscleIntensity: Map<String, String>
) {
    val displayName: String
        get() = nameTr.ifBlank {
            name.split("_").joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
        }

    val derivedCategory: String
        get() {
            val cardio = muscleIntensity["cardio"]
            val stretching = muscleIntensity["stretching"]
            val stretchScore = stretching?.drop(1)?.toIntOrNull() ?: 0
            return when {
                cardio != null && cardio.startsWith("P") -> "Kardiyo"
                stretchScore >= 5 -> "Esneme"
                else -> "Güç"
            }
        }
}

object ExerciseDatabaseLoader {
    private var cache: List<ExerciseInfo>? = null

    fun load(context: Context): List<ExerciseInfo> {
        cache?.let { return it }
        val json = context.assets.open("exercises.json").bufferedReader().use { it.readText() }
        val arr = JSONArray(json)
        val list = mutableListOf<ExerciseInfo>()

        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)

            val equipment = obj.optJSONArray("equipment")?.let { eq ->
                (0 until eq.length()).map { eq.getString(it) }
            } ?: emptyList()

            val secondary = obj.optJSONArray("secondary_muscles")?.let { sm ->
                (0 until sm.length()).map { sm.getString(it) }
            } ?: emptyList()

            val tips = obj.optJSONArray("execution_tips")?.let { t ->
                (0 until t.length()).map { t.getString(it) }
            } ?: emptyList()

            val intensityMap = mutableMapOf<String, String>()
            obj.optJSONObject("muscle_intensity")?.let { intensityObj ->
                intensityObj.keys().forEach { key ->
                    intensityMap[key] = intensityObj.getString(key)
                }
            }

            list.add(
                ExerciseInfo(
                    id = obj.optString("id"),
                    name = obj.optString("name"),
                    nameTr = obj.optString("name_tr"),
                    description = obj.optString("description"),
                    difficulty = obj.optInt("difficulty"),
                    equipment = equipment,
                    caloriesPerKgPerHour = obj.optDouble("calories_per_kg_per_hour"),
                    primaryMuscle = obj.optString("primary_muscle"),
                    primaryMuscleTr = obj.optString("primary_muscle_tr"),
                    secondaryMuscles = secondary,
                    typicalSetsReps = obj.optString("typical_sets_reps"),
                    typicalSetsRepsTr = obj.optString("typical_sets_reps_tr"),
                    executionTips = tips,
                    muscleIntensity = intensityMap
                )
            )
        }
        cache = list
        return list
    }

    fun forCategory(context: Context, categoryLabel: String): List<ExerciseInfo> =
        load(context).filter { it.derivedCategory == categoryLabel }
}