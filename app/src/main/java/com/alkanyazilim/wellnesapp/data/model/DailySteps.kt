package com.alkanyazilim.wellnesapp.data.model

import java.time.LocalDate

data class DailySteps(
    val date: LocalDate,
    val steps: Long
)