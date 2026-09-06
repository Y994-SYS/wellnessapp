package com.alkanyazilim.wellnesapp.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.alkanyazilim.wellnesapp.MainActivity

object StepsWidgetKeys {
    val STEPS_KEY = intPreferencesKey("widget_steps")
    val GOAL_KEY = intPreferencesKey("widget_goal")
}

class StepsWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val steps = prefs[StepsWidgetKeys.STEPS_KEY] ?: 0
            val goal = prefs[StepsWidgetKeys.GOAL_KEY] ?: 10000

            // AppColors.kt ile birebir aynı renkler
            val stepsAccentColor = Color(0xFFFF7043)   // AppColors.StepsAccent
            val surfaceColor = Color(0xFFFFFFFF)        // AppColors.Surface
            val textPrimary = ColorProvider(day = Color(0xFF1C1B1F), night = Color(0xFF1C1B1F))
            val textSecondary = ColorProvider(day = Color(0xFF6E6E76), night = Color(0xFF6E6E76))

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(20.dp)
                    .background(surfaceColor)
                    .padding(16.dp)
                    .clickable(actionStartActivity<MainActivity>()),
                verticalAlignment = Alignment.Vertical.CenterVertically,
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                // Sabit (statik) dış halka — dekoratif, adıma göre dolmuyor
                Box(
                    modifier = GlanceModifier
                        .size(84.dp)
                        .cornerRadius(42.dp)
                        .background(stepsAccentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = GlanceModifier
                            .size(72.dp)
                            .cornerRadius(36.dp)
                            .background(surfaceColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.Horizontal.CenterHorizontally) {
                            Text(
                                text = "$steps",
                                style = TextStyle(
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary,
                                    textAlign = TextAlign.Center
                                )
                            )
                            Text(
                                text = "adım",
                                style = TextStyle(
                                    fontSize = 9.sp,
                                    color = textSecondary
                                )
                            )
                        }
                    }
                }

                Spacer(GlanceModifier.height(8.dp))

                Text(
                    text = "/ $goal hedef",
                    style = TextStyle(
                        fontSize = 11.sp,
                        color = textSecondary
                    )
                )
            }
        }
    }
}

class StepsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StepsWidget()
}