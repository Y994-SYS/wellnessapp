package com.alkanyazilim.wellnesapp.data.repository

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.aggregate.AggregationResult
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.alkanyazilim.wellnesapp.data.model.DailySteps
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId

class HealthConnectManager(private val context: Context) {

    val healthConnectClient: HealthConnectClient? by lazy {
        if (isAvailable()) HealthConnectClient.getOrCreate(context) else null
    }

    fun isAvailable(): Boolean {
        val status = HealthConnectClient.getSdkStatus(context)
        return status == HealthConnectClient.SDK_AVAILABLE
    }

    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class)
    )

    suspend fun hasAllPermissions(): Boolean {
        val client = healthConnectClient ?: return false
        val granted = client.permissionController.getGrantedPermissions()
        return granted.containsAll(permissions)
    }

    fun requestPermissionsContract() =
        PermissionController.createRequestPermissionResultContract()

    suspend fun readTodaySteps(): Long {
        val client = healthConnectClient ?: return 0L
        val zoneId = ZoneId.systemDefault()
        val startOfDay = LocalDate.now(zoneId).atStartOfDay(zoneId).toInstant()
        val now = Instant.now()

        val response: AggregationResult = client.aggregate(
            AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
            )
        )
        return response[StepsRecord.COUNT_TOTAL] ?: 0L
    }

    suspend fun readStepsForDate(date: LocalDate): Long {
        val client = healthConnectClient ?: return 0L
        val zoneId = ZoneId.systemDefault()
        val startOfDay = date.atStartOfDay(zoneId).toInstant()
        val endOfDay = date.plusDays(1).atStartOfDay(zoneId).toInstant()

        val response: AggregationResult = client.aggregate(
            AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)
            )
        )
        return response[StepsRecord.COUNT_TOTAL] ?: 0L
    }

    suspend fun readStepsForLastDays(days: Int): List<DailySteps> {
        val client = healthConnectClient ?: return emptyList()
        val zoneId = ZoneId.systemDefault()
        val endDate = LocalDate.now(zoneId)
        val startDate = endDate.minusDays((days - 1).toLong())

        val startDateTime = startDate.atStartOfDay()
        val endDateTime = endDate.plusDays(1).atStartOfDay()

        val request = AggregateGroupByPeriodRequest(
            metrics = setOf(StepsRecord.COUNT_TOTAL),
            timeRangeFilter = TimeRangeFilter.between(startDateTime, endDateTime),
            timeRangeSlicer = Period.ofDays(1)
        )

        val response = client.aggregateGroupByPeriod(request)

        return response.map { group ->
            DailySteps(
                date = group.startTime.toLocalDate(),
                steps = group.result[StepsRecord.COUNT_TOTAL] ?: 0L
            )
        }.sortedByDescending { it.date }
    }

    /**
     * Ham StepsRecord kayıtlarını okuyup, her kaydın dakikadaki adım sayısına (kadans)
     * göre "yavaş" veya "tempolu" yürüyüş olarak sınıflandırır.
     * Eşik: dakikada 100 adım ve üzeri tempolu kabul edilir.
     */
    suspend fun readWalkingPaceBreakdown(date: LocalDate): Pair<Long, Long> {
        val client = healthConnectClient ?: return 0L to 0L
        val zoneId = ZoneId.systemDefault()
        val startOfDay = date.atStartOfDay(zoneId).toInstant()
        val endOfDay = date.plusDays(1).atStartOfDay(zoneId).toInstant()

        val request = ReadRecordsRequest(
            recordType = StepsRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)
        )

        val response = client.readRecords(request)

        var slowSteps = 0L
        var briskSteps = 0L
        val briskThreshold = 100.0 // dakikada adım

        for (record in response.records) {
            val durationMinutes = (Duration.between(record.startTime, record.endTime).toMillis() / 60000.0)
                .coerceAtLeast(1.0 / 60.0)
            val cadence = record.count / durationMinutes

            if (cadence >= briskThreshold) {
                briskSteps += record.count
            } else {
                slowSteps += record.count
            }
        }

        return slowSteps to briskSteps
    }
}