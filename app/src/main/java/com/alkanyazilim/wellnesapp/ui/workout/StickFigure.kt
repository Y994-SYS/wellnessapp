package com.alkanyazilim.wellnesapp.ui.workout

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

enum class ExercisePose {
    STANDING, JUMPING_JACK, HIGH_KNEE, JUMP_ROPE, PLANK, PUSHUP, SITUP,
    SQUAT, LUNGE, PULLUP, NECK_STRETCH, ARM_STRETCH, BUTTERFLY,
    HAMSTRING_STRETCH, CAT_COW, CHILD_POSE
}

private data class Pt(val x: Float, val y: Float)

private data class Skeleton(
    val head: Pt, val neck: Pt,
    val shoulderL: Pt, val shoulderR: Pt,
    val hip: Pt,
    val handL: Pt, val handR: Pt,
    val kneeL: Pt, val kneeR: Pt,
    val footL: Pt, val footR: Pt
)

private fun skeletonFor(pose: ExercisePose): Skeleton = when (pose) {
    ExercisePose.STANDING -> Skeleton(
        Pt(50f, 10f), Pt(50f, 20f), Pt(38f, 22f), Pt(62f, 22f), Pt(50f, 52f),
        Pt(28f, 45f), Pt(72f, 45f), Pt(44f, 75f), Pt(56f, 75f), Pt(40f, 97f), Pt(60f, 97f)
    )
    ExercisePose.JUMPING_JACK -> Skeleton(
        Pt(50f, 10f), Pt(50f, 20f), Pt(30f, 22f), Pt(70f, 22f), Pt(50f, 52f),
        Pt(12f, 2f), Pt(88f, 2f), Pt(28f, 78f), Pt(72f, 78f), Pt(10f, 98f), Pt(90f, 98f)
    )
    ExercisePose.HIGH_KNEE -> Skeleton(
        Pt(50f, 10f), Pt(50f, 20f), Pt(38f, 23f), Pt(62f, 23f), Pt(50f, 52f),
        Pt(70f, 38f), Pt(35f, 60f), Pt(65f, 55f), Pt(42f, 78f), Pt(72f, 50f), Pt(48f, 98f)
    )
    ExercisePose.JUMP_ROPE -> Skeleton(
        Pt(50f, 10f), Pt(50f, 20f), Pt(38f, 22f), Pt(62f, 22f), Pt(50f, 50f),
        Pt(30f, 38f), Pt(70f, 38f), Pt(46f, 72f), Pt(54f, 72f), Pt(45f, 95f), Pt(55f, 95f)
    )
    ExercisePose.PLANK -> Skeleton(
        Pt(10f, 35f), Pt(20f, 38f), Pt(22f, 38f), Pt(22f, 38f), Pt(65f, 45f),
        Pt(24f, 72f), Pt(24f, 72f), Pt(85f, 50f), Pt(85f, 50f), Pt(97f, 60f), Pt(97f, 60f)
    )
    ExercisePose.PUSHUP -> Skeleton(
        Pt(10f, 45f), Pt(20f, 47f), Pt(22f, 47f), Pt(22f, 47f), Pt(65f, 50f),
        Pt(24f, 78f), Pt(24f, 78f), Pt(85f, 52f), Pt(85f, 52f), Pt(97f, 58f), Pt(97f, 58f)
    )
    ExercisePose.SITUP -> Skeleton(
        Pt(25f, 48f), Pt(35f, 53f), Pt(35f, 53f), Pt(35f, 53f), Pt(50f, 70f),
        Pt(28f, 38f), Pt(28f, 38f), Pt(65f, 55f), Pt(65f, 55f), Pt(85f, 70f), Pt(85f, 70f)
    )
    ExercisePose.SQUAT -> Skeleton(
        Pt(50f, 15f), Pt(50f, 25f), Pt(38f, 27f), Pt(62f, 27f), Pt(50f, 62f),
        Pt(20f, 45f), Pt(80f, 45f), Pt(35f, 80f), Pt(65f, 80f), Pt(35f, 98f), Pt(65f, 98f)
    )
    ExercisePose.LUNGE -> Skeleton(
        Pt(50f, 12f), Pt(50f, 22f), Pt(40f, 24f), Pt(60f, 24f), Pt(50f, 50f),
        Pt(35f, 45f), Pt(65f, 45f), Pt(35f, 70f), Pt(65f, 85f), Pt(30f, 98f), Pt(80f, 98f)
    )
    ExercisePose.PULLUP -> Skeleton(
        Pt(50f, 15f), Pt(50f, 25f), Pt(38f, 27f), Pt(62f, 27f), Pt(50f, 60f),
        Pt(30f, 5f), Pt(70f, 5f), Pt(45f, 85f), Pt(55f, 85f), Pt(42f, 98f), Pt(58f, 98f)
    )
    ExercisePose.NECK_STRETCH -> Skeleton(
        Pt(60f, 12f), Pt(50f, 22f), Pt(38f, 24f), Pt(62f, 24f), Pt(50f, 55f),
        Pt(30f, 50f), Pt(70f, 50f), Pt(43f, 78f), Pt(57f, 78f), Pt(40f, 98f), Pt(60f, 98f)
    )
    ExercisePose.ARM_STRETCH -> Skeleton(
        Pt(50f, 12f), Pt(50f, 22f), Pt(38f, 24f), Pt(62f, 24f), Pt(50f, 55f),
        Pt(65f, 35f), Pt(45f, 50f), Pt(43f, 78f), Pt(57f, 78f), Pt(40f, 98f), Pt(60f, 98f)
    )
    ExercisePose.BUTTERFLY -> Skeleton(
        Pt(50f, 20f), Pt(50f, 30f), Pt(38f, 32f), Pt(62f, 32f), Pt(50f, 60f),
        Pt(35f, 55f), Pt(65f, 55f), Pt(25f, 65f), Pt(75f, 65f), Pt(50f, 78f), Pt(50f, 78f)
    )
    ExercisePose.HAMSTRING_STRETCH -> Skeleton(
        Pt(30f, 50f), Pt(35f, 55f), Pt(35f, 55f), Pt(35f, 55f), Pt(45f, 65f),
        Pt(70f, 60f), Pt(70f, 60f), Pt(70f, 68f), Pt(70f, 68f), Pt(95f, 68f), Pt(95f, 68f)
    )
    ExercisePose.CAT_COW -> Skeleton(
        Pt(20f, 40f), Pt(30f, 42f), Pt(30f, 42f), Pt(30f, 42f), Pt(75f, 45f),
        Pt(28f, 75f), Pt(28f, 75f), Pt(70f, 75f), Pt(70f, 75f), Pt(85f, 78f), Pt(85f, 78f)
    )
    ExercisePose.CHILD_POSE -> Skeleton(
        Pt(85f, 65f), Pt(75f, 60f), Pt(75f, 60f), Pt(75f, 60f), Pt(30f, 55f),
        Pt(95f, 55f), Pt(95f, 55f), Pt(20f, 70f), Pt(20f, 70f), Pt(15f, 80f), Pt(15f, 80f)
    )
}

/**
 * Pose-tracking (MediaPipe/OpenPose tarzı) fitness uygulamalarında görülen,
 * eklem noktalı ve gradyan dolgulu bir figür stili. Basit tek-renkli çizgi
 * yerine: kalın gradyanlı gövde, beyaz halkalı eklem noktaları, gradyanlı
 * baş, ve zemine oturmuş bir gölge ile daha "kasıtlı tasarlanmış" bir görünüm.
 */
@Composable
fun StickFigure(pose: ExercisePose, color: Color, modifier: Modifier = Modifier) {
    val skeleton = skeletonFor(pose)

    Canvas(modifier = modifier.size(170.dp)) {
        val w = size.width
        val h = size.height
        fun p(pt: Pt) = Offset(pt.x / 100f * w, pt.y / 100f * h)

        val limbStroke = w * 0.042f
        val torsoStroke = w * 0.06f
        val shoulderStroke = w * 0.032f
        val jointOuterRadius = w * 0.05f
        val jointInnerRadius = w * 0.03f
        val headRadius = w * 0.105f

        // Zemin gölgesi — figürü "yere oturtan" yumuşak elips
        drawOval(
            color = color.copy(alpha = 0.10f),
            topLeft = Offset(w * 0.22f, h * 0.945f),
            size = Size(w * 0.56f, h * 0.045f)
        )

        // Kollar (gövdenin altında kalır)
        drawLine(color.copy(alpha = 0.9f), p(skeleton.shoulderL), p(skeleton.handL), limbStroke, cap = StrokeCap.Round)
        drawLine(color.copy(alpha = 0.9f), p(skeleton.shoulderR), p(skeleton.handR), limbStroke, cap = StrokeCap.Round)

        // Bacaklar
        drawLine(color.copy(alpha = 0.9f), p(skeleton.hip), p(skeleton.kneeL), limbStroke, cap = StrokeCap.Round)
        drawLine(color.copy(alpha = 0.9f), p(skeleton.hip), p(skeleton.kneeR), limbStroke, cap = StrokeCap.Round)
        drawLine(color.copy(alpha = 0.9f), p(skeleton.kneeL), p(skeleton.footL), limbStroke, cap = StrokeCap.Round)
        drawLine(color.copy(alpha = 0.9f), p(skeleton.kneeR), p(skeleton.footR), limbStroke, cap = StrokeCap.Round)

        // Omuz genişliği hattı — gövdeye hacim hissi katar
        drawLine(color.copy(alpha = 0.85f), p(skeleton.shoulderL), p(skeleton.shoulderR), shoulderStroke, cap = StrokeCap.Round)

        // Gövde (boyun-kalça) — dikey gradyanlı kalın hat, en belirgin çizgi
        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(color, color.copy(alpha = 0.75f))
            ),
            start = p(skeleton.neck),
            end = p(skeleton.hip),
            strokeWidth = torsoStroke,
            cap = StrokeCap.Round
        )

        // Eklem noktaları — beyaz halka + renkli merkez (pose-tracking görünümü)
        val joints = listOf(
            skeleton.shoulderL, skeleton.shoulderR, skeleton.hip,
            skeleton.kneeL, skeleton.kneeR,
            skeleton.handL, skeleton.handR,
            skeleton.footL, skeleton.footR
        )
        joints.forEach { joint ->
            val center = p(joint)
            drawCircle(color = Color.White, radius = jointOuterRadius, center = center)
            drawCircle(color = color, radius = jointInnerRadius, center = center)
        }

        // Baş — radyal gradyanlı daire + hafif ışık vurgusu
        val headCenter = p(skeleton.head)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color, color.copy(alpha = 0.78f)),
                center = headCenter,
                radius = headRadius
            ),
            radius = headRadius,
            center = headCenter
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.4f),
            radius = headRadius * 0.32f,
            center = Offset(
                headCenter.x - headRadius * 0.32f,
                headCenter.y - headRadius * 0.32f
            )
        )
    }
}