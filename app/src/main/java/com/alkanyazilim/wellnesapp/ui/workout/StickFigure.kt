package com.alkanyazilim.wellnesapp.ui.workout

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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

@Composable
fun StickFigure(pose: ExercisePose, color: Color, modifier: Modifier = Modifier) {
    val skeleton = skeletonFor(pose)

    Canvas(modifier = modifier.size(140.dp)) {
        val w = size.width
        val h = size.height
        fun p(pt: Pt) = Offset(pt.x / 100f * w, pt.y / 100f * h)

        val strokeWidth = w * 0.035f

        drawLine(color = color, start = p(skeleton.neck), end = p(skeleton.shoulderL), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawLine(color = color, start = p(skeleton.neck), end = p(skeleton.shoulderR), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawLine(color = color, start = p(skeleton.shoulderL), end = p(skeleton.handL), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawLine(color = color, start = p(skeleton.shoulderR), end = p(skeleton.handR), strokeWidth = strokeWidth, cap = StrokeCap.Round)

        drawLine(color = color, start = p(skeleton.neck), end = p(skeleton.hip), strokeWidth = strokeWidth, cap = StrokeCap.Round)

        drawLine(color = color, start = p(skeleton.hip), end = p(skeleton.kneeL), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawLine(color = color, start = p(skeleton.hip), end = p(skeleton.kneeR), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawLine(color = color, start = p(skeleton.kneeL), end = p(skeleton.footL), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawLine(color = color, start = p(skeleton.kneeR), end = p(skeleton.footR), strokeWidth = strokeWidth, cap = StrokeCap.Round)

        drawCircle(color = color, radius = w * 0.09f, center = p(skeleton.head))
    }
}