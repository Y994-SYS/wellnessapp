package com.alkanyazilim.wellnesapp.ui.workout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import androidx.compose.runtime.getValue
@Composable
fun ExerciseAnimationOrIcon(
    lottieFileName: String,
    fallbackIcon: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resId = remember(lottieFileName) {
        context.resources.getIdentifier(lottieFileName, "raw", context.packageName)
    }

    if (resId == 0) {
        // Dosya henüz eklenmedi — emoji ikona geri dön
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(fallbackIcon, fontSize = 48.sp)
        }
        return
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(resId))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier
    )
}